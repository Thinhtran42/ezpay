package com.thinhtran.EzPay.security;

import com.thinhtran.EzPay.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        System.out.println(">>> Jwt Filter called: " + request.getRequestURI());

        String path = request.getRequestURI();

        // 🚫 Bỏ qua filter cho các path công khai
        if (path != null && (path.startsWith("/v1/api/auth") || path.startsWith("/api/auth") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs"))) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (header == null || !header.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = header.substring(7).trim(); // Trim to remove extra spaces
            if (token.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }

            if (!jwtProvider.validateToken(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            String username = jwtProvider.getUsernameFromToken(token);
            var userOpt = userRepository.findByUserName(username);
            if (userOpt.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }

            var user = userOpt.get();
            var auth = new UsernamePasswordAuthenticationToken(
                    user, null, null
            );
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            // Log the exception and continue without authentication
            System.err.println("JWT Authentication error: " + e.getMessage());
            // Don't set authentication in case of any error
        }

        filterChain.doFilter(request, response);
    }
}
