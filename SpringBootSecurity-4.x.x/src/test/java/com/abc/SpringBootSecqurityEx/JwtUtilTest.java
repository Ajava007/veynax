package com.abc.SpringBootSecqurityEx;

import com.abc.SpringBootSecqurityEx.secqurity.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    @Test
    void generateAndValidateToken() {
        JwtUtil util = new JwtUtil();
        ReflectionTestUtils.setField(util, "SECRET_KEY", "VGhpc0lzQVN1cGVyU2VjdXJlSldUVFNjcmV0S2V5Rm9yU3ByaW5nQm9vdDIwMjZNeVNlY3JldEtleUZvclRlc3RpbmcxMjM=");
        ReflectionTestUtils.setField(util, "EXPIRATION_TIME", 3600000L);
        util.validateConfiguration();
        String token = util.generateToken("testuser");
        assertNotNull(token);
        assertTrue(util.validateToken(token));
        assertEquals("testuser", util.extractUsername(token));
    }
}
