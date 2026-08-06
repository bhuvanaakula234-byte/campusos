package com.campusos.controller;

import com.campusos.dto.AuthRequest;
import com.campusos.dto.AuthResponse;
import com.campusos.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        return authService.authenticate(request)
                .map(token -> ResponseEntity.ok(new AuthResponse(token)))
                .orElseGet(() -> ResponseEntity.status(401).body("Invalid credentials"));
    }

    @PostMapping("/login-cookie")
    public ResponseEntity<?> loginCookie(@RequestBody AuthRequest request) {
        return authService.authenticate(request)
                .map(token -> {
                    ResponseCookie cookie = ResponseCookie.from("campusos_token", token)
                            .httpOnly(true)
                            .path("/")
                            .maxAge(3600)
                            .sameSite("Lax")
                            .build();
                    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(new AuthResponse(token));
                })
                .orElseGet(() -> ResponseEntity.status(401).body("Invalid credentials"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        var user = authService.registerUser(request.getEmail(), request.getPassword(), request.getFullName());
        return ResponseEntity.ok(user.getId());
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) return ResponseEntity.ok().body(null);
        Object p = authentication.getPrincipal();
        if (p instanceof com.campusos.model.UserEntity) {
            com.campusos.model.UserEntity u = (com.campusos.model.UserEntity) p;
            var roles = u.getRoles().stream().map(r -> r.getName()).toList();
            return ResponseEntity.ok(java.util.Map.of(
                    "id", u.getId(),
                    "email", u.getEmail(),
                    "fullName", u.getFullName(),
                    "roles", roles
            ));
        }
        return ResponseEntity.ok().body(null);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = ResponseCookie.from("campusos_token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body("ok");
    }
}
