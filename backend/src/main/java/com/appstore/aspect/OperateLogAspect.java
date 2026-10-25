package com.appstore.aspect;

import com.appstore.annotation.OperateLog;
import com.appstore.exception.BusinessException;
import com.appstore.util.UserContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作审计切面（v1.3 企业级增强）
 * <p>
 * 拦截所有 Controller 的写操作（POST/PUT/DELETE）以及标注了 {@link OperateLog} 的方法，
 * 统一输出结构化审计记录到 OPERATE_AUDIT logger（落到 logs/audit.log，保留90天）。
 * <p>
 * 设计要点：
 * <ul>
 *   <li>审计记录采用 JSON 单行格式，可直接被 ELK/Splunk 等采集解析</li>
 *   <li>审计异常绝不影响业务主流程（任何内部错误静默降级）</li>
 *   <li>密码等敏感字段自动脱敏；上传文件只记录文件名与大小</li>
 * </ul>
 */
@Slf4j
@Aspect
@Component
public class OperateLogAspect {

    private static final Logger AUDIT = LoggerFactory.getLogger("OPERATE_AUDIT");

    private static final String POINT =
            "(execution(* com.appstore.controller..*(..)) && ("
                    + "!execution(* com.appstore.controller..*.get*(..))"
                    + "))"
                    + " || @annotation(com.appstore.annotation.OperateLog)";

    /** 仅审计这些写方法；GET 查询不记，防止日志淹没 */
    private static final List<String> AUDIT_METHODS = Arrays.asList("POST", "PUT", "DELETE");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around(POINT)
    public Object recordAudit(ProceedingJoinPoint point) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        Throwable error = null;
        try {
            result = point.proceed();
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            try {
                writeAudit(point, result, error, System.currentTimeMillis() - start);
            } catch (Exception e) {
                // 审计失败绝不影响业务
                log.debug("审计记录失败", e);
            }
        }
        return result;
    }

    private void writeAudit(ProceedingJoinPoint point, Object result, Throwable error, long costMs) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs == null ? null : attrs.getRequest();

        MethodSignature signature = (MethodSignature) point.getSignature();
        OperateLog anno = signature.getMethod().getAnnotation(OperateLog.class);

        String module;
        String action;
        boolean recordArgs;
        if (anno != null) {
            module = anno.module();
            action = anno.action();
            recordArgs = anno.recordArgs();
        } else {
            module = point.getTarget().getClass().getSimpleName()
                    .replace("Controller", "");
            action = signature.getName();
            recordArgs = true;
        }

        // 仅审计写操作（注解显式标注的除外，注解可标在 GET 上用于特殊审计）
        boolean explicit = anno != null;
        if (!explicit && request != null
                && !AUDIT_METHODS.contains(request.getMethod().toUpperCase())) {
            return;
        }

        Object userId = request == null ? null : request.getAttribute(UserContext.USER_ID);
        Object username = request == null ? null : request.getAttribute(UserContext.USERNAME);

        // 结果码：业务异常的code或200
        int status; // 200=成功；其他=失败
        if (error == null) {
            status = 200;
        } else if (error instanceof BusinessException) {
            status = ((BusinessException) error).getCode() == null ? 500 : ((BusinessException) error).getCode();
        } else {
            status = 500;
        }

        StringBuilder line = new StringBuilder(256);
        line.append("{")
                .append("\"time\":\"").append(java.time.LocalDateTime.now()).append("\",")
                .append("\"userId\":").append(userId == null ? "null" : userId).append(",")
                .append("\"username\":").append(json(username)).append(",")
                .append("\"module\":\"").append(module).append("\",")
                .append("\"action\":\"").append(action).append("\",")
                .append("\"method\":\"").append(request == null ? "-" : request.getMethod()).append("\",")
                .append("\"uri\":\"").append(request == null ? "-" : request.getRequestURI()).append("\",")
                .append("\"ip\":\"").append(clientIp(request)).append("\",")
                .append("\"args\":").append(recordArgs ? buildArgs(point.getArgs()) : "null").append(",")
                .append("\"result\":").append(status).append(",")
                .append("\"costMs\":").append(costMs).append(",")
                .append(error == null ? "\"error\":null" :
                        "\"error\":\"" + escape(String.valueOf(error.getMessage())) + "\"")
                .append("}");

        AUDIT.info("{}", line);
    }

    /** 构造脱敏后的请求参数摘要 */
    private String buildArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "null";
        }
        try {
            List<Object> safe = Arrays.stream(args)
                    .map(this::toSafeArg)
                    .collect(Collectors.toList());
            String json = objectMapper.writeValueAsString(safe);
            if (json.length() > 1024) {
                json = json.substring(0, 1024) + "...(truncated)";
            }
            return json;
        } catch (Exception e) {
            return "\"deserialize-failed\"";
        }
    }

    /** 敏感字段脱敏 + 文件对象摘要化 */
    private Object toSafeArg(Object arg) {
        if (arg == null || arg instanceof HttpServletRequest || arg instanceof HttpServletResponse
                || arg instanceof org.springframework.core.io.Resource) {
            return arg == null ? null : arg.getClass().getSimpleName();
        }
        if (arg instanceof MultipartFile) {
            MultipartFile f = (MultipartFile) arg;
            return String.format("[file name=%s size=%d]", f.getOriginalFilename(), f.getSize());
        }
        if (arg instanceof String || arg instanceof Number || arg instanceof Boolean) {
            return arg;
        }
        // POJO：脱敏敏感字段
        try {
            Map<String, Object> map = objectMapper.convertValue(arg, new TypeReference<Map<String, Object>>() {});
            sanitize(map);
            return map;
        } catch (Exception e) {
            return arg.getClass().getSimpleName();
        }
    }

    private void sanitize(Map<String, Object> map) {
        for (String key : new String[]{"password", "oldPassword", "newPassword", "passwordHash", "secret"}) {
            if (map.containsKey(key)) {
                map.put(key, "******");
            }
        }
        if (map.containsKey("icon") && map.get("icon") instanceof MultipartFile) {
            MultipartFile f = (MultipartFile) map.get("icon");
            map.put("icon", String.format("[file name=%s size=%d]", f.getOriginalFilename(), f.getSize()));
        }
    }

    private String json(Object value) {
        if (value == null) {
            return "null";
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "\"" + escape(String.valueOf(value)) + "\"";
        }
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    private String clientIp(HttpServletRequest request) {
        if (request == null) {
            return "-";
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip == null ? "-" : ip.split(",")[0].trim();
    }
}
