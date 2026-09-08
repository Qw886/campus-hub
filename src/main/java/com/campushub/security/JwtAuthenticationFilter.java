package com.campushub.security;

import com.campushub.common.exception.BusinessException;
import com.campushub.service.JwtService;
import com.campushub.service.UserService;
import com.campushub.vo.UserProfileResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final SecurityResponseWriter responseWriter;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserService userService,
            SecurityResponseWriter responseWriter
    ) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.responseWriter = responseWriter;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        // 没带凭证：交给后续访问规则决定是否允许
        if (authorization == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (!authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
                throw new BusinessException(401, "登录凭证格式错误");
            }

            String token = authorization.substring(7).trim();
            Long userId = jwtService.parseUserId(token);

            UserProfileResponse user = userService.getCurrentUser(userId);

            var authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + user.getRole())
            );

            var authentication =
                    UsernamePasswordAuthenticationToken.authenticated(
                            user,
                            null,
                            authorities
                    );

            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

        } catch (BusinessException exception) {
            SecurityContextHolder.clearContext();
            responseWriter.writeError(
                    response,
                    exception.getCode(),
                    exception.getMessage()
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}