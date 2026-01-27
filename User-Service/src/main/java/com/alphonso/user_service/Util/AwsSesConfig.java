package com.alphonso.user_service.Util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;

@Configuration
public class AwsSesConfig {

    @Value("${aws.region:ap-south-1}")
    private String awsRegion;

    @Value("${aws.ses.enabled:false}")
    private boolean sesEnabled;

    @Bean
    public SesClient sesClient() {
        if (!sesEnabled) {
            //System.out.println("AWS SES disabled — running in dev mode (using Gmail SMTP)");
            return null;
        }

       // System.out.println("AWS SES enabled — region: " + awsRegion);
        return SesClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
    }
}

