package com.chantika.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chantika.auth.dto.AuthResponse;
import com.chantika.auth.dto.LoginRequest;
import com.chantika.auth.dto.RegisterRequest;
import com.chantika.auth.model.User;
import com.chantika.auth.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {

        try {

            String token =
                    authService.authenticate(loginRequest);

            User user =
                    authService.getCurrentUser(
                            loginRequest.getUsername()
                    );

            AuthResponse response =
                    new AuthResponse(
                            token,
                            "Bearer",
                            user.getUsername(),
                            user.getEmail(),
                            user.getRole(),
                            86400000L
                    );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Login gagal: " + e.getMessage());
        }
    }

    // REGISTER
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest registerRequest
    ) {

        try {

            User user =
                    authService.register(registerRequest);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(user);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // VALIDATE TOKEN
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(
            @RequestHeader("Authorization")
            String authHeader
    ) {

        try {

            if (
                    authHeader != null
                    &&
                    authHeader.startsWith("Bearer ")
            ) {

                String token =
                        authHeader.substring(7);

                Boolean isValid =
                        authService.validateToken(token);

                return ResponseEntity.ok(isValid);
            }

            return ResponseEntity
                    .badRequest()
                    .body("Invalid token format");

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Token invalid");
        }
    }

    // USER TEST
    @GetMapping("/user/test")
    public ResponseEntity<?> userTest() {

        return ResponseEntity.ok(
                "USER endpoint berhasil diakses"
        );
    }

    // ADMIN TEST
    @GetMapping("/admin/test")
    public ResponseEntity<?> adminTest() {

        return ResponseEntity.ok(
                "ADMIN endpoint berhasil diakses"
        );
    }
}