package com.gadgetshop.backend_api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * จัดการ JWT: สร้าง token ตอน login และถอดข้อมูล/ตรวจสอบความถูกต้อง
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs
    ) {
        // secret เก็บเป็น Base64 ใน properties แปลงกลับเป็น bytes ก่อนสร้าง key
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    /** สร้าง token โดยฝัง username (subject) + role + userId ไว้ข้างใน */
    public String generateToken(String username, String role, Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /** ดึง username จาก token */
    public String getUsername(String token) {
        return parse(token).getSubject();
    }

    /** ดึง role จาก token */
    public String getRole(String token) {
        return parse(token).get("role", String.class);
    }

    /** ตรวจว่า token ถูกต้องและยังไม่หมดอายุ */
    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}