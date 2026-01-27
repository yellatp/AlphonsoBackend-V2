package com.alphonso.user_service.Util;

import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenGenerator {
	private final SecretKey secretKey;
	private final long expirationMs;

	public TokenGenerator(@Value("${jwt.secret}") String base64Secret,
			@Value("${jwt.expiration:86400000}") long expirationMs) {
		this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(base64Secret));
		this.expirationMs = expirationMs;
	}

	public String generateToken(String email, Long userId, String firstName, String lastName,String role) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + expirationMs);

		//System.out.println(secretKey);
		//System.out.println(firstName);
		return Jwts.builder().setSubject(email)
							.claim("firstName", firstName)
							.claim("lastName", lastName)
							.claim("uid", userId).claim("roles", role).setIssuedAt(now)
				.setExpiration(exp).signWith(secretKey).compact();
	}
}
