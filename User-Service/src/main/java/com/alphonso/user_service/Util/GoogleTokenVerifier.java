package com.alphonso.user_service.Util;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Component
public class GoogleTokenVerifier {
    private final RestTemplate rest;

    public GoogleTokenVerifier() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);  // 5 seconds connection timeout
        factory.setReadTimeout(10000);    // 10 seconds read timeout
        this.rest = new RestTemplate(factory);
    }

    public Map<String, Object> verify(String idToken) {
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            @SuppressWarnings("unchecked")
            Map<String, Object> response = rest.getForObject(url, Map.class);
            if (response == null) {
                throw new RuntimeException("Google token verification returned null response");
            }
            return response;
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to verify Google token: " + e.getMessage(), e);
        }
    }
}
