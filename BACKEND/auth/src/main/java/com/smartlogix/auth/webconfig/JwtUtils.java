package com.smartlogix.auth.webconfig;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {

    @Autowired
    private JwtProperties jwtProperties;

    public String generateToken(String email, String rol, Long userId) {
        Algorithm algorithm = Algorithm.HMAC256(jwtProperties.getSecret());
        return JWT.create()
                .withSubject(email)
                .withClaim("role", rol)
                .withClaim("userId", userId)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
                .sign(algorithm);
    }

    public boolean validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtProperties.getSecret());
            JWT.require(algorithm).build().verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        DecodedJWT decoded = JWT.decode(token);
        return decoded.getSubject();
    }

    public String getRoleFromToken(String token) {
        DecodedJWT decoded = JWT.decode(token);
        return decoded.getClaim("role").asString();
    }
}
