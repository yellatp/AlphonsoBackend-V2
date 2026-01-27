package com.alphonso.Interviewer_Service.RequestDTO;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Component
@ConfigurationProperties(prefix="app")
@Data
public class AppProperties {
    private String timezone = "Asia/Kolkata";
    
}