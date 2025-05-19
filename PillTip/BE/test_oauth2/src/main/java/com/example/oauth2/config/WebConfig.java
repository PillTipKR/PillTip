// PillTip\BE\src\main\java\com\example\oauth2\config\WebConfig.java
// author : mireutale
// date : 2025-05-19
// description : 앱, 웹 설정
package com.example.oauth2.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해 CORS(Cross-Origin Resource Sharing) 설정 -> 모바일 앱에서 API 접근 가능
                .allowedOrigins("*") // 실제 운영 환경에서는 특정 도메인으로 제한해야 합니다 -> 추후 수정사항
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
} 