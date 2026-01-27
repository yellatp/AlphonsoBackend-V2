package com.alphonso.profile_service.Security;

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
            	.requestMatchers("/api/profile/quiz-passed").permitAll()
            	.requestMatchers("/api/profile/candidates/assessment-status").permitAll()
                .requestMatchers("/api/profile/**").hasAnyRole("CANDIDATE", "INTERVIEWER", "ADMIN")
                
                .anyRequest().authenticated()
            )

            .addFilterBefore(gatewayHeaderAuthenticationFilter, 
                             UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
