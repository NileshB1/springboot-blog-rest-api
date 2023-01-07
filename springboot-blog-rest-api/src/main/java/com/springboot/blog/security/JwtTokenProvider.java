package com.springboot.blog.security;

import com.springboot.blog.exception.BlogAPIException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
    @Value("${app.jwt-secret}")
    private String jwtSecret;
    @Value("${app-jwt-expiration-milliseconds}")
    private long jwtExpirationDate;

    // generate JTW token
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        System.out.println("#### generateToken() username is: "+ username);
        Date currDate = new Date();
        Date expiredDate = new Date(currDate.getTime() + jwtExpirationDate);

        String jwtToken = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expiredDate)
                .signWith(key())
                .compact();
        System.out.println("#### generateToken() returning jwtToken "+ jwtToken);
        return jwtToken;
    }

    private Key key() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)
        );
    }

    // Get username from JWT token
    public String getUsername(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        String username = claims.getSubject();
        System.out.println("#### [getUsername] Found username is: "+ username);
        return username;
    }

    // validate JWT token
    public boolean validateToken(String token) {
        boolean isValidToken = false;
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parse(token);
            isValidToken = true;
        } catch (MalformedJwtException ex) {
            isValidToken = false;
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            isValidToken = false;
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            isValidToken = false;
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            isValidToken = false;
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "JWT claim String is empty");
        } catch (Exception exp) {
            isValidToken = false;
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Unknown exception while validation JWT token");
        }
        System.out.println("#### [validateToken] is token valid: "+ isValidToken);
        return isValidToken;
    }
}
