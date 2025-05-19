// PillTip\BE\src\main\java\com\example\oauth2\config\WebConfig.java
// author : mireutale
// date : 2025-05-19
// description : 앱, 웹 설정
package com.oauth2.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해 CORS(Cross-Origin Resource Sharing) 설정 -> 모바일 앱에서 API 접근 가능
                .allowedOriginPatterns("*") // allowedOrigins 대신 allowedOriginPatterns 사용
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
} 