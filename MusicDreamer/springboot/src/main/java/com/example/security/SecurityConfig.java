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
                        // 公开路径
                        .requestMatchers("/login", "/register", "/files/download/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()

                        // 特殊路径：用户更新接口（多角色可访问）
                        .requestMatchers("/user/update").hasAnyRole("USER","ADMIN","SINGER")

                        // 多角色可访问的模块
                        .requestMatchers("/music/**", "/files/upload", "/tags/**", "/notice/**","/songlist/**"
                        ).hasAnyRole("USER", "ADMIN", "SINGER")

                        // ADMIN专属路径
                        .requestMatchers("/admin/**","/user/**").hasRole("ADMIN")

                        // 其他请求需要认证
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
