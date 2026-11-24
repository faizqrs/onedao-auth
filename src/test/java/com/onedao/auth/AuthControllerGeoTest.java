package com.onedao.auth;

import com.onedao.auth.controller.AuthController;
import com.onedao.auth.dto.SignupRequest;
import com.onedao.auth.repository.UserRepository;
import com.onedao.auth.security.JwtUtil;
import com.onedao.auth.security.TokenBlacklist;
import com.onedao.auth.service.GeoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerGeoTest {

    @Test
    void shouldRejectSignupFromBlockedCountry() {
        UserRepository users = mock(UserRepository.class);
        GeoService geo = mock(GeoService.class);
        when(geo.resolveCountry(anyString())).thenReturn("IR");
        when(geo.isBlocked("IR")).thenReturn(true);

        AuthController controller = new AuthController(
                users, new BCryptPasswordEncoder(), mock(JwtUtil.class),
                mock(TokenBlacklist.class), geo, true, 5);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.signup(
                        new SignupRequest("blocked_test_01", "blocked_test_01@mail.com", "Test@123", "Standard User"),
                        new MockHttpServletRequest()));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(users, never()).save(any());
    }
}