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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;

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
    //基于token，不需要csrf
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 启用CORS并禁用CSRF
        http
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 公开路径
                        .requestMatchers("/login", "/register", "/files/download/**").permitAll()

                        // 特殊路径：用户更新接口（多角色可访问）
                        .requestMatchers("/user/update","/user/selectAll","/user/selectById/**","/user/selectPage", "/songlist/selectPage",
                        "/user/changePwd").hasAnyRole("USER", "ADMIN", "SINGER")

                        // 多角色可访问的模块
                        .requestMatchers("/music/**", "/files/upload", "/tags/**", "/notice/**", "/songlist/**").hasAnyRole("USER", "ADMIN", "SINGER")

                        // ADMIN专属路径
                        .requestMatchers("/admin/**", "/user/**").hasRole("ADMIN")

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


//    // 配置CORS
//    @Bean
//    CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration config = new CorsConfiguration();
//        // 使用安全域名列表替代通配符*
//        config.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
//        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Content-Disposition"));
//        config.setAllowCredentials(true);  // 启用凭证携带
//        config.setMaxAge(3600L);
//        // 暴露更多响应头（文件下载必需）
//        config.setExposedHeaders(Arrays.asList("Content-Disposition", "Authorization", "Content-Length"));
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//        return source;
//    }

}