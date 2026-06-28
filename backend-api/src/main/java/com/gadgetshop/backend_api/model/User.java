package com.gadgetshop.backend_api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users") // กำหนดชื่อตารางในฐานข้อมูล
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // ในโปรเจกต์เริ่มต้นนี้ขอบันทึกเป็น Plain Text ก่อนเพื่อให้เข้าใจง่ายครับ

    @Column(nullable = false)
    private String role; // "CUSTOMER" หรือ "ADMIN"

    // ─── Getters and Setters ───
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}