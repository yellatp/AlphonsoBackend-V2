package com.alphonso.api_gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import com.alphonso.api_gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;


@Slf4j
@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
    	super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    public static class Config{
    	
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
        	
            String path = exchange.getRequest().getURI().getPath();
           
            
            if (path.startsWith("/api/user/") || path.startsWith("/api/otp/")) {
            	
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);
            
            try {
                Claims claims = jwtUtil.validateTokenAndGetClaims(token);
                
                // Extract email from subject claim
                String email = claims.getSubject();
                if (email == null || email.isEmpty()) {
                    Object subClaim = claims.get("sub");
                    if (subClaim != null) {
                        email = subClaim.toString();
                    }
                }
                if (email == null || email.isEmpty()) {
                    log.error("JWT token missing subject (email). All claims: {}", claims.keySet());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                // Safely extract uid (JWT may store as Long, Integer, or Number)
                Long uid = null;
                Object uidObj = claims.get("uid");
                if (uidObj instanceof Number) {
                    uid = ((Number) uidObj).longValue();
                } else if (uidObj != null) {
                    try {
                        uid = Long.valueOf(uidObj.toString());
                    } catch (NumberFormatException ignored) {
                        uid = null;
                    }
                }
                if (uid == null) {
                    log.error("JWT token missing or invalid 'uid' claim. All claims: {}", claims.keySet());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                String firstName = claims.get("firstName", String.class);
                if (firstName == null) {
                    firstName = "";
                }
                String rolesHeader = String.join(",", jwtUtil.extractRoles(token));

                log.info("JWT Token parsed successfully - Subject: {}, UID: {}", email, uid);

                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(exchange.getRequest().mutate()
                                .header("X-User-Id", String.valueOf(uid))
                                .header("X-Email", email)
                                .header("X-First-Name", firstName)
                                .header("X-Roles", rolesHeader)
                                .build())
                        .build();
                
                log.debug("Added headers - X-Email: {}, X-User-Id: {}", email, uid);
                
                return chain.filter(modifiedExchange);

            } 
            catch (Exception e) {
                log.error("JWT validation failed: {}", e.getMessage(), e);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
           
        };
    }

    @SuppressWarnings("unused")
	private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
        log.warn("Auth error: {}", err);
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}
