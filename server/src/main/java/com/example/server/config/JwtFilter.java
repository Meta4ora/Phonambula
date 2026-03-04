package com.example.server.config;

import com.example.server.model.User;
import com.example.server.service.JwtService;
import com.example.server.service.UserService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    public JwtFilter(JwtService jwtService, @Lazy UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                String login = jwtService.extractLogin(token);

                if (login != null && !login.isEmpty()) {
                    // Проверяем, что токен не истек
                    if (!jwtService.isTokenExpired(token)) {
                        // Загружаем пользователя из базы, чтобы убедиться, что он существует
                        Optional<User> userOpt = userService.findByLogin(login);

                        if (userOpt.isPresent()) {
                            UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(
                                            login,
                                            null,
                                            List.of()
                                    );
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        }
                    }
                }
            } catch (Exception e) {
                // Если токен невалидный, просто не устанавливаем аутентификацию
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}