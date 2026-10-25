package com.appstore.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * XSS防护工具单元测试
 * 覆盖：script/iframe等危险标签清除、事件属性清除、javascript协议清除、尖括号转义
 */
class XssUtilTest {

    @Test
    @DisplayName("script标签及其内容被清除")
    void clean_shouldRemoveScriptTag() {
        String result = XssUtil.clean("hello<script>alert('xss')</script>world");
        assertFalse(result.contains("script"));
        assertTrue(result.contains("hello"));
        assertTrue(result.contains("world"));
    }

    @Test
    @DisplayName("onclick等事件属性被清除")
    void clean_shouldRemoveEventAttributes() {
        String result = XssUtil.clean("<div onclick=\"alert(1)\">内容</div>");
        assertFalse(result.toLowerCase().contains("onclick"));
    }

    @Test
    @DisplayName("javascript伪协议被清除")
    void clean_shouldRemoveJavascriptProtocol() {
        String result = XssUtil.clean("<a href='javascript:alert(1)'>链接</a>");
        assertFalse(result.toLowerCase().contains("javascript"));
    }

    @Test
    @DisplayName("剩余尖括号被转义为HTML实体")
    void clean_shouldEscapeAngleBrackets() {
        String result = XssUtil.clean("<img src=x>");
        assertFalse(result.contains("<img"));
        assertTrue(result.contains("&lt;"));
    }

    @Test
    @DisplayName("空值安全返回")
    void clean_shouldHandleNullAndEmpty() {
        assertNull(XssUtil.clean(null));
        assertEquals("", XssUtil.clean(""));
    }
}
