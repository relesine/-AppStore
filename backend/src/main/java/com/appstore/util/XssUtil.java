package com.appstore.util;

/**
 * XSS防护工具类
 * 用于过滤用户输入中的危险HTML标签与脚本，防止存储型XSS攻击
 */
public class XssUtil {

    /** 需要过滤的危险标签 */
    private static final String[] DANGEROUS_TAGS = {
            "script", "iframe", "object", "embed", "link", "meta",
            "style", "form", "input", "textarea", "button", "select",
            "option", "svg", "math"
    };

    private XssUtil() {
    }

    /**
     * 过滤XSS危险内容：
     * 1. 去除 <script> 等危险标签及其内容
     * 2. 去除 javascript: / on* 事件属性
     * 3. 将 < > 转义为安全字符（防止拼接型XSS）
     *
     * @param input 原始输入
     * @return 过滤后的安全文本
     */
    public static String clean(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String result = input;
        // 1. 去除危险标签及其内容（不区分大小写，含属性）
        for (String tag : DANGEROUS_TAGS) {
            result = result.replaceAll("(?is)<" + tag + "[^>]*>.*?</" + tag + "\\s*>", "");
            result = result.replaceAll("(?is)<" + tag + "[^>]*/?>", "");
        }
        // 2. 去除 javascript: 协议
        result = result.replaceAll("(?i)javascript\\s*:", "");
        // 3. 去除 on* 事件属性
        result = result.replaceAll("(?is)\\son[a-z]+\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)", " ");
        // 4. 转义剩余尖括号，防止拼接型XSS
        result = result.replace("<", "&lt;").replace(">", "&gt;");
        return result;
    }
}
