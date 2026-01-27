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
                
                // Log all claims for debugging
                log.info("JWT Token parsed successfully");
                log.info("  - Subject (getSubject()): {}", email);
                log.info("  - All claims keys: {}", claims.keySet());
                log.info("  - UID: {}", claims.get("uid", Long.class));
                log.info("  - Roles: {}", claims.get("roles"));
                log.info("  - FirstName: {}", claims.get("firstName", String.class));
                
                // Try alternative ways to get email if subject is null
                if (email == null || email.isEmpty()) {
                    // Try getting from claims map directly
                    Object subClaim = claims.get("sub");
                    if (subClaim != null) {
                        email = subClaim.toString();
                        log.warn("Subject was null, but found 'sub' claim: {}", email);
                    } else {
                        log.error("JWT token missing subject claim (email). All claims: {}", claims);
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }
                }
                
                // Final validation
                if (email == null || email.isEmpty()) {
                    log.error("JWT token missing email after all attempts. Claims: {}", claims);
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(exchange.getRequest().mutate()
                                .header("X-User-Id", String.valueOf(claims.get("uid", Long.class)))
                                .header("X-Email", email)
                                .header("X-First-Name", claims.get("firstName", String.class) != null ? claims.get("firstName", String.class) : "")
                                .header("X-Roles", String.join(",", jwtUtil.extractRoles(token)))
                                .build())
                        .build();
                
                log.debug("Added headers - X-Email: {}, X-User-Id: {}", email, claims.get("uid", Long.class));
                
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
