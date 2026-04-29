package com.ivankudravcev.sandboxspringsite2026copy;


import com.ivankudravcev.sandboxspringsite2026copy.entity.User;
import com.ivankudravcev.sandboxspringsite2026copy.service.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtCore {

    @Value("${jwt.access.secret}")
    private String accessSecret;
    @Value("${jwt.refresh.secret}")
    private String refreshSecret;
    @Value("${jwt.access.expiration}")
    private int accessLifetime;
    @Value("${jwt.refresh.expiration}")
    private long refreshLifetime;
    @Autowired
    private UserRepository userRepository;


    private SecretKey getSigningAccessKey() {
        byte[] decode = Base64.getDecoder().decode(accessSecret);
        return Keys.hmacShaKeyFor(decode);
    };
    private SecretKey getEncryptRefreshKey() {
        byte[] keyBytes = refreshSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            keyBytes = padded;
        } else if (keyBytes.length > 32) {
            keyBytes = Arrays.copyOf(keyBytes, 32);
        }
        return new SecretKeySpec(keyBytes, "AES");
    }

    public String generateRefreshToken(Authentication authentication){
        String userName = "";
        if (authentication.getPrincipal() instanceof UserDetailsImpl ){
            userName = ((UserDetailsImpl) authentication.getPrincipal()).getUsername();
        }else if (authentication.getPrincipal() instanceof OAuth2User){
            Optional<User> userRepositoryByEmail = userRepository.findByEmail(((OAuth2User) authentication.getPrincipal()).getAttributes().get("email").toString());
            userName = userRepositoryByEmail.get().getUsername();
        }

        return Jwts.builder()
                .subject(userName)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshLifetime))
                .encryptWith(getEncryptRefreshKey(),Jwts.KEY.A256GCMKW, Jwts.ENC.A256GCM)
                .compact();
    }

    public long getExpiresAccessToken(String accessToken){
        return Jwts.parser()
                .verifyWith(getSigningAccessKey())
                .build()
                .parseSignedClaims(accessToken)
                .getPayload().getExpiration().getTime();
    }
    public long getExpiresRefreshToken(String refreshSecret){
        return Jwts.parser()
                .decryptWith(getEncryptRefreshKey())
                .build()
                .parseEncryptedClaims(refreshSecret)
                .getPayload().getExpiration().getTime();
    }


    public String getUserName(String accessToken){
        return Jwts.parser()
                .verifyWith(getSigningAccessKey())
                .build()
                .parseSignedClaims(accessToken)
                .getPayload().getSubject();
    }

    public String generateAccessToken(String refreshToken){
        Claims payload = Jwts.parser()
                .decryptWith(getEncryptRefreshKey())
                .build()
                .parseEncryptedClaims(refreshToken)
                .getPayload();
        return Jwts.builder()
                .subject(payload.getSubject())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessLifetime))
                .signWith(getSigningAccessKey())
                .compact();
    }
}
