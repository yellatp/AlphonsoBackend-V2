package com.alphonso.moodle_employer_service.Moodle.Config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "moodle")
public class MoodleConfig {
    private String baseUrl;
    private String token;
    private String format;
    private Long defaultRoleId;
}
