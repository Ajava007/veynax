package com.abc.SpringBootSecqurityEx;

import com.abc.SpringBootSecqurityEx.dtos.RegisterRequest;
import com.abc.SpringBootSecqurityEx.repository.UserRepository;
import com.abc.SpringBootSecqurityEx.secqurity.JwtUtil;
import com.abc.SpringBootSecqurityEx.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceRegistrationTest {
    @Test
    void duplicateUsernameThrows() {
        UserRepository repo = mock(UserRepository.class);
        when(repo.existsByUserName("existing")).thenReturn(true);
        AuthService svc = new AuthService(null, mock(JwtUtil.class), repo, mock(PasswordEncoder.class));
        RegisterRequest req = new RegisterRequest();
        req.setUsername("existing");
        req.setEmail("e@e.com");
        req.setPassword("password123");
        assertThrows(IllegalArgumentException.class, () -> svc.register(req));
    }
}
