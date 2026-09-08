package com.campushub.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import com.campushub.security.JwtAuthenticationFilter;
import com.campushub.security.SecurityResponseWriter;
import com.campushub.service.JwtService;
import com.campushub.service.UserService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService,
            UserService userService,
            SecurityResponseWriter responseWriter
    ) throws Exception {

        http.csrf(csrf -> csrf.disable());

        http.sessionManagement(session ->
                session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS
                )
        );

        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());
        http.logout(logout -> logout.disable());

        http.authorizeHttpRequests(authorize -> authorize
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/users/register",
                        "/api/users/login"
                ).permitAll()
                .anyRequest().authenticated()
        );

        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, exception) ->
                        responseWriter.writeError(
                                response,
                                401,
                                "请先登录或重新登录"
                        )
                )
                .accessDeniedHandler((request, response, exception) ->
                        responseWriter.writeError(
                                response,
                                403,
                                "没有访问权限"
                        )
                )
        );

        JwtAuthenticationFilter jwtFilter =
                new JwtAuthenticationFilter(
                        jwtService,
                        userService,
                        responseWriter
                );

        http.addFilterBefore(
                jwtFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }
}