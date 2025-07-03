package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * 跨域配置 - 增强版（支持音频资源跨域操作）
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 基本跨域设置
        config.addAllowedOriginPattern("*"); // 使用OriginPattern代替固定*，支持Spring 5.3+
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true); // 允许携带凭证（如cookies）

        // 针对音频资源的特殊设置
        config.addExposedHeader("Content-Disposition"); // 暴露内容处置头
        config.addExposedHeader("Content-Length");
        config.addExposedHeader("Content-Type");

        // 设置预检查请求的有效期（秒）
        config.setMaxAge(3600L);

        // 对所有接口和音频资源路径应用配置
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}