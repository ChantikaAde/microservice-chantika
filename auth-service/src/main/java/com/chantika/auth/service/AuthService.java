package com.chantika.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.chantika.auth.dto.LoginRequest;
import com.chantika.auth.dto.RegisterRequest;
import com.chantika.auth.model.User;
import com.chantika.auth.repository.UserRepository;
import com.chantika.auth.security.JwtUtil;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    // LOGIN
    public String authenticate(LoginRequest loginRequest) {

        User user = userRepository.findByUsername(
                loginRequest.getUsername()
        ).orElseThrow(() ->
                new RuntimeException(
                        "Username tidak ditemukan"
                )
        );

        boolean passwordMatch =
                passwordEncoder.matches(
                        loginRequest.getPassword(),
                        user.getPassword()
                );

        if (!passwordMatch) {

            throw new RuntimeException(
                    "Password salah"
            );
        }

        // USER DETAILS + ROLE
        UserDetails userDetails =

                new org.springframework.security.core.userdetails.User(

                        user.getUsername(),

                        user.getPassword(),

                        java.util.Collections.singletonList(

                                new SimpleGrantedAuthority(
                                        "ROLE_" + user.getRole()
                                )
                        )
                );

        return jwtUtil.generateToken(userDetails);
    }

    // REGISTER
    public User register(RegisterRequest registerRequest) {

        if (userRepository.existsByUsername(
                registerRequest.getUsername()
        )) {

            throw new RuntimeException(
                    "Username sudah terdaftar!"
            );
        }

        if (userRepository.existsByEmail(
                registerRequest.getEmail()
        )) {

            throw new RuntimeException(
                    "Email sudah terdaftar!"
            );
        }

        User user = new User();

        user.setUsername(registerRequest.getUsername());

        user.setEmail(registerRequest.getEmail());

        user.setPassword(
                passwordEncoder.encode(
                        registerRequest.getPassword()
                )
        );

        user.setFullName(
                registerRequest.getFullName()
        );

        // DEFAULT ROLE USER
        user.setRole("USER");

        user.setIsActive(true);

        return userRepository.save(user);
    }

    // GET CURRENT USER
    public User getCurrentUser(String username) {

        return userRepository.findByUsername(username)
                .orElseThrow(() ->

                        new RuntimeException(
                                "User tidak ditemukan"
                        )
                );
    }

    // VALIDATE TOKEN
    public Boolean validateToken(String token) {

        return jwtUtil.validateToken(token);
    }
}