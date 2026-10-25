package com.appstore.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT工具单元测试
 * 覆盖：Token生成、解析、校验、过期失效
 */
class JwtUtilTest {

    private JwtUtil buildJwtUtil(long expirationMillis) {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "unit-test-secret-key-2024-very-long-secret-key-for-jwt-token");
        ReflectionTestUtils.setField(jwtUtil, "expiration", expirationMillis);
        return jwtUtil;
    }

    @Test
    @DisplayName("生成Token后可正确解析用户信息")
    void generateAndParseToken() {
        JwtUtil jwtUtil = buildJwtUtil(86400000L);
        String token = jwtUtil.generateToken(7L, "tester", 2);

        assertNotNull(token);
        assertEquals(7L, jwtUtil.getUserIdFromToken(token));
        assertEquals("tester", jwtUtil.getUsernameFromToken(token));
        assertEquals(2, jwtUtil.getUserTypeFromToken(token));
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    @DisplayName("篡改Token后校验失败")
    void validateToken_shouldRejectTampered() {
        JwtUtil jwtUtil = buildJwtUtil(86400000L);
        String token = jwtUtil.generateToken(7L, "tester", 2);
        String tampered = token.substring(0, token.length() - 4) + "XXXX";

        assertFalse(jwtUtil.validateToken(tampered));
        assertNull(jwtUtil.getUserIdFromToken(tampered));
    }

    @Test
    @DisplayName("过期Token校验失败")
    void validateToken_shouldRejectExpired() throws InterruptedException {
        JwtUtil jwtUtil = buildJwtUtil(-1000L); // 已过期1秒
        String token = jwtUtil.generateToken(7L, "tester", 2);
        Thread.sleep(50);
        assertFalse(jwtUtil.validateToken(token));
    }
}
