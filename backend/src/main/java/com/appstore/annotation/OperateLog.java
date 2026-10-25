package com.appstore.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作审计注解（v1.3 企业级增强）
 * <p>
 * 标注在 Controller 方法上，由 {@link com.appstore.aspect.OperateLogAspect}
 * 在请求完成后自动将"操作人-时间-模块-动作-结果"写入 audit.log 审计文件。
 * <p>
 * 未标注的 Controller 写操作（POST/PUT/DELETE）也会被切面自动兜底记录
 * （模块取类简名、动作取方法名），保证审计无遗漏。
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperateLog {

    /** 业务模块，如：用户 */
    String module();

    /** 操作描述，如：登录、审核应用 */
    String action();

    /** 是否记录请求参数（默认记录；含敏感信息的接口可关闭） */
    boolean recordArgs() default true;
}
