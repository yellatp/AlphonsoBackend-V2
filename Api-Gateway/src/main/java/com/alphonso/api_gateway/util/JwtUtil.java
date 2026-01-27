package com.alphonso.api_gateway.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.alphonso.api_gateway.DTO.UserDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	private final SecretKey secretKey;

	public JwtUtil(@Value("${jwt.secret}") String base64Secret) {
		this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(base64Secret));

	}

	public Jws<Claims> validateToken(String token) {
		return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
	}

	public UserDTO validateTokenAndGetUser(String token) {
		try {
			if (token.startsWith("Bearer ")) {
				token = token.substring(7);
			}

			Jws<Claims> claimsJws = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);

			Claims claims = claimsJws.getBody();

			String email = claims.getSubject();
			String firstName = claims.get("firstName", String.class);
			System.out.println(firstName);
			Long userId = claims.get("uid", Long.class);
			Object rolesObj = claims.get("roles");

			List<String> roles = new ArrayList<>();
			if (rolesObj instanceof String) {
				roles.add((String) rolesObj);
			} else if (rolesObj instanceof List<?>) {
				roles = ((List<?>) rolesObj).stream().map(Object::toString).collect(Collectors.toList());
			}

			UserDTO user = new UserDTO();
			user.setId(userId);
			user.setFirstName(firstName);
			user.setEmail(email);
			user.setRoles(roles.isEmpty() ? null : roles.get(0)); // assuming single role
			return user;

		} catch (JwtException | IllegalArgumentException e) {
			System.err.println("Invalid JWT Token: " + e.getMessage());
			return null;
		}
	}

	public String extractUsername(String token) {
		return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
	}

	public List<String> extractRoles(String token) {
		Claims claims = parseClaims(token);
		Object rolesObj = claims.get("roles");

		if (rolesObj == null) {
			return Collections.emptyList();
		}

		if (rolesObj instanceof List<?>) {
			return ((List<?>) rolesObj).stream().filter(Objects::nonNull).map(Object::toString)
					.collect(Collectors.toList());
		} else if (rolesObj instanceof String) {
			String rolesStr = (String) rolesObj;
			if (rolesStr.contains(",")) {
				return Arrays.stream(rolesStr.split(",")).map(String::trim).collect(Collectors.toList());
			} else {
				return Collections.singletonList(rolesStr.trim());
			}
		}

		return Collections.emptyList();
	}

	private Claims parseClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getExpiration();
	}

	public Claims validateTokenAndGetClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
	}

	public boolean isTokenValid(String token, String username) {
		String extracted = extractUsername(token);
		return extracted.equals(username) && !isTokenExpired(token);
	}

}
