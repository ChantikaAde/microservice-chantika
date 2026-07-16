package com.chantika.produk.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                // DISABLE CSRF
                .csrf(csrf -> csrf.disable())

                // STATELESS JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // AUTHORIZATION
                .authorizeHttpRequests(auth -> auth

                        // H2 CONSOLE
                        .requestMatchers(
                                "/h2-console/**"
                        ).permitAll()

                        // ACTUATOR & PROMETHEUS
                        .requestMatchers(
                                "/actuator/**"
                        ).permitAll()

                        // USER + ADMIN
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/produk/**"
                        )
                        .hasAnyRole(
                                "USER",
                                "ADMIN"
                        )

                        // ADMIN ONLY
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/produk/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/produk/**"
                        )
                        .hasRole("ADMIN")

                        // OTHER
                        .anyRequest()
                        .authenticated()
                )

                // H2 FRAME
                .headers(headers ->
                        headers.frameOptions(
                                frame -> frame.disable()
                        )
                )

                // JWT FILTER
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}