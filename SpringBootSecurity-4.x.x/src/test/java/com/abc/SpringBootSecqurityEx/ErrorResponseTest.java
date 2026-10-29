package com.abc.SpringBootSecqurityEx;

import com.abc.SpringBootSecqurityEx.util.ErrorResponse;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {
    @Test
    void fieldErrorsPopulated() {
        ErrorResponse<Object> er = new ErrorResponse<>();
        er.setFieldErrors(Map.of("username", "required"));
        assertEquals("required", er.getFieldErrors().get("username"));
    }
}
