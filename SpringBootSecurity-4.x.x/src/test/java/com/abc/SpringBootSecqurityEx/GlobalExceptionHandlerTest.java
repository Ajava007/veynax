package com.abc.SpringBootSecqurityEx;

import com.abc.SpringBootSecqurityEx.util.ErrorResponse;
import com.abc.SpringBootSecqurityEx.util.GlobalExceptionHandler;
import com.abc.SpringBootSecqurityEx.util.NotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void notFoundReturns404() {
        ResponseEntity<ErrorResponse<Object>> r = handler.handleNotFound(new NotFoundException("x"));
        assertEquals(HttpStatus.NOT_FOUND, r.getStatusCode());
        assertEquals(404, r.getBody().getStatus_code());
    }

    @Test
    void entityNotFoundReturns404() {
        ResponseEntity<ErrorResponse<Object>> r = handler.handleEntityNotFound(new EntityNotFoundException("y"));
        assertEquals(HttpStatus.NOT_FOUND, r.getStatusCode());
    }

    @Test
    void illegalArgumentReturns400() {
        ResponseEntity<ErrorResponse<Object>> r = handler.handleIllegalArgument(new IllegalArgumentException("bad"));
        assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
    }

    @Test
    void illegalStateReturns409() {
        ResponseEntity<ErrorResponse<Object>> r = handler.handleIllegalState(new IllegalStateException("invalid transition"));
        assertEquals(HttpStatus.CONFLICT, r.getStatusCode());
    }

    @Test
    void genericExceptionReturns500() {
        ResponseEntity<ErrorResponse<Object>> r = handler.handleGeneric(new RuntimeException("boom"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r.getStatusCode());
    }
}
