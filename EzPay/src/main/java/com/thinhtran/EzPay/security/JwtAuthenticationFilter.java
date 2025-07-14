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
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean shouldSkip = path != null && (
                path.startsWith("/v1/api/auth") || 
                path.startsWith("/api/auth") || 
                path.startsWith("/swagger-ui") || 
                path.startsWith("/v3/api-docs")
        );
        
        if (shouldSkip) {
            System.out.println(">>> Filter SKIPPED for public path: " + path);
        }
        
        return shouldSkip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        System.out.println(">>> JWT Filter called: " + path);

        // 🚫 Bỏ qua filter cho các path công khai
        if (path != null && (path.startsWith("/v1/api/auth") || 
                            path.startsWith("/api/auth") || 
                            path.startsWith("/swagger-ui") || 
                            path.startsWith("/v3/api-docs"))) {
            System.out.println(">>> Bypassing JWT filter for public path: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println(">>> Processing JWT authentication for: " + path);

        try {
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (header == null || !header.startsWith("Bearer ")) {
                System.out.println(">>> No valid Authorization header found");
                filterChain.doFilter(request, response);
                return;
            }

            String token = header.substring(7).trim(); // Trim to remove extra spaces
            if (token.isEmpty()) {
                System.out.println(">>> Empty token found");
                filterChain.doFilter(request, response);
                return;
            }

            if (!jwtProvider.validateToken(token)) {
                System.out.println(">>> Invalid token");
                filterChain.doFilter(request, response);
                return;
            }

            String username = jwtProvider.getUsernameFromToken(token);
            System.out.println(">>> Valid token for user: " + username);
            
            var userOpt = userRepository.findByUserName(username);
            if (userOpt.isEmpty()) {
                System.out.println(">>> User not found: " + username);
                filterChain.doFilter(request, response);
                return;
            }

            var user = userOpt.get();
            var auth = new UsernamePasswordAuthenticationToken(
                    user, null, null
            );
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
            System.out.println(">>> Authentication set for user: " + username);

        } catch (Exception e) {
            // Log the exception and continue without authentication
            System.err.println(">>> JWT Authentication error: " + e.getMessage());
            // Don't set authentication in case of any error
        }

        filterChain.doFilter(request, response);
    }
}
