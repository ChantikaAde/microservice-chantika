package com.chantika.auth.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

        @Override
        protected void doFilterInternal(
                @NonNull HttpServletRequest request,
                @NonNull HttpServletResponse response,
                @NonNull FilterChain filterChain
        ) throws ServletException, IOException {

        // ===== LEWATI ACTUATOR =====
        String path = request.getServletPath();

        if (path.startsWith("/actuator")) {
                filterChain.doFilter(request, response);
                return;
        }

        final String authHeader =
                request.getHeader("Authorization");

        String jwt = null;
        String username = null;

        if (
                authHeader != null
                &&
                authHeader.startsWith("Bearer ")
        ) {

                jwt = authHeader.substring(7);

                try {

                username =
                        jwtUtil.extractUsername(jwt);

                } catch (Exception e) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
                }
        }

        if (
                username != null
                &&
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        == null
        ) {

                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(
                                        username
                                );

                if (
                        jwtUtil.validateToken(
                                jwt,
                                userDetails
                        )
                ) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authToken);
                }
        }

        filterChain.doFilter(
                request,
                response
        );
        }
        }