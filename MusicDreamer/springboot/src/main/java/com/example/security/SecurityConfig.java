package com.example.security;

import com.example.jwt.CustomFilter;
import com.example.jwt.JwtAuthenticationTokenFilter;
import com.example.security.handler.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //基于token，不需要csrf
        http.csrf(csrf -> csrf.disable())
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 基于token，不需要session
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/login","/register","/files/download/**").permitAll() // 放行api
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN")
                        .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
//                .formLogin(form -> form.loginPage("/login")
//                        .successHandler(new MyAuthenticationSuccessHandler())
//                        .failureHandler(new MyAuthenticationFailureHandler()))
//                .logout(logout -> logout.logoutUrl("/login")
//                        .logoutSuccessHandler(new MyLogoutSuccessHandler()))
//                .exceptionHandling(exception -> {
//                    exception.authenticationEntryPoint(new MyAuthenticationEntryPoint());})
//                .sessionManagement(session -> {
//                    session.maximumSessions(1).expiredSessionStrategy(new MySessionInformationExpiredStrategy());})
//                .exceptionHandling(exception -> {
//                    exception.accessDeniedHandler(new MyAccessDeniedHandler());});

        return http.build();
    }


}
