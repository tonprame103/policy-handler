package com.jsontool.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration — Spring Boot 4 / Spring Framework 7
 *
 * Spring Boot 4: ไม่ใช้ WebSecurityConfigurerAdapter อีกต่อไป
 * CORS ตั้งค่าผ่าน WebMvcConfigurer bean แทน
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
