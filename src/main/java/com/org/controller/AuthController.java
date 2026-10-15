package com.org.controller;

import com.org.apimodels.AuthResponse;
import com.org.apimodels.LoginRequest;
import com.org.apimodels.RefreshTokenRequest;
import com.org.apimodels.RegisterRequest;
import com.org.entity.User;
import com.org.service.AuthService;
import com.org.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private AuthService authService;

    @PostMapping("/register/email")
    public ResponseEntity<User> registerWithEmail(@RequestBody RegisterRequest request) {
        User user = userService.registerWithEmail(request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/register/phone")
    public ResponseEntity<User> registerWithPhone(@RequestBody RegisterRequest request) {
        User user = userService.registerWithPhone(request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String deviceId = httpRequest.getHeader("X-Device-ID");
        String deviceName = httpRequest.getHeader("X-Device-Name");
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthResponse response = authService.login(request, deviceId, deviceName, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request, 
                                                      HttpServletRequest httpRequest) {
        String deviceId = httpRequest.getHeader("X-Device-ID");
        String deviceName = httpRequest.getHeader("X-Device-Name");
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthResponse response = authService.refreshToken(request, deviceId, deviceName, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
