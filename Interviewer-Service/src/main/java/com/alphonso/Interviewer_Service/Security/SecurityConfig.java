package com.alphonso.Interviewer_Service.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final GatewayHeaderAuthenticationFilter gatewayHeaderAuthenticationFilter;

    public SecurityConfig(GatewayHeaderAuthenticationFilter filter) {
        this.gatewayHeaderAuthenticationFilter = filter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf().disable()
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/interviewer/upcomingInterviews", "/api/interviewer/todayInterviews").hasAnyRole("INTERVIEWER", "CANDIDATE", "ADMIN")
                .requestMatchers("/api/interviewer/feedback/**").hasAnyRole("INTERVIEWER", "CANDIDATE", "ADMIN")
                .requestMatchers("/api/interviewer/**").hasAnyRole("INTERVIEWER", "ADMIN")
                .anyRequest().authenticated()
            )

            .addFilterBefore(gatewayHeaderAuthenticationFilter, 
                             UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
