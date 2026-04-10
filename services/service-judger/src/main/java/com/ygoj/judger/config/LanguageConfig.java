package com.ygoj.judger.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "language")
public class LanguageConfig {
    private Map<String, LanguageItem> language;

    @Data
    public static class LanguageItem {
        private String compile;
        private String run;
        private String fileName;
        private String image;
    }
}
