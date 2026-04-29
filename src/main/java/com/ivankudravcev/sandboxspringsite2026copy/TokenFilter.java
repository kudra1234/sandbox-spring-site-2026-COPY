package com.ivankudravcev.sandboxspringsite2026copy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TokenFilter.class);

    @Autowired
    private JwtCore jwtCore;

    @Autowired
    @Lazy
    private UserDetailsService userDetailsService;

    // Матчеры для URL, которые требуют аутентификации
    private final RequestMatcher securedMatcher = new AntPathRequestMatcher("/secured/users", "GET");
    private final RequestMatcher createOrderMatcher = new AntPathRequestMatcher("/auth/createOrder", "POST");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (requiresAuthentication(request)) {
            try {
                String header = request.getHeader("Authorization");

                // Проверяем наличие и корректность заголовка
                if (header != null && header.startsWith("Bearer ")) {
                    String token = header.substring(7);

                    if (!token.isEmpty()) {
                        validateTokenAndSetAuthentication(token);
                    } else {
                        log.warn("Empty token provided");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                } else {
                    log.debug("No Bearer token found in request to {}", request.getRequestURI());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            } catch (Exception e) {
                log.error("Authentication error: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean requiresAuthentication(HttpServletRequest request) {
        return securedMatcher.matches(request) || createOrderMatcher.matches(request);
    }

    private void validateTokenAndSetAuthentication(String token) {
        try {
            long expiresAt = jwtCore.getExpiresAccessToken(token);

            if (System.currentTimeMillis() >= expiresAt) {
                throw new RuntimeException("Token has expired");
            }

            String userName = jwtCore.getUserName(token);

            if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userName);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authenticated user: {}", userName);
            }
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }
}