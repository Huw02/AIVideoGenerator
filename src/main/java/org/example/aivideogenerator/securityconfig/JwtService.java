package org.example.aivideogenerator.securityconfig;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;
    private long expirationMs = 1000 * 60 * 60; // 1 hour

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // fx 10 timer
                .signWith(key)
                .compact();
    }
}
