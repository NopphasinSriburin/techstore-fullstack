package com.gadgetshop.backend_api.controller;

import com.gadgetshop.backend_api.model.User;
import com.gadgetshop.backend_api.repository.UserRepository;
import com.gadgetshop.backend_api.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
// หมายเหตุ: ลบ @CrossOrigin ออก เพราะ CORS จัดการรวมที่ SecurityConfig แล้ว
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // 📝 1. สมัครสมาชิก — เข้ารหัสรหัสผ่านก่อนบันทึก
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return ResponseEntity.badRequest().body("❌ ชื่อผู้ใช้งานนี้ถูกใช้ไปแล้ว กรุณาเปลี่ยนใหม่");
        }

        // กันไม่ให้ตั้ง role แปลกๆ ผ่าน request — รับเฉพาะ CUSTOMER / ADMIN
        String role = "ADMIN".equalsIgnoreCase(user.getRole()) ? "ADMIN" : "CUSTOMER";
        user.setRole(role);

        // 🔐 เข้ารหัสรหัสผ่านด้วย BCrypt ก่อนเก็บลงฐานข้อมูล
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);

        // ไม่ส่ง password (ที่ถูก hash) กลับไป
        savedUser.setPassword(null);
        return ResponseEntity.ok(savedUser);
    }

    // 🔑 2. เข้าสู่ระบบ — เทียบ BCrypt แล้วคืน JWT
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {
        String username = loginRequest.getUsername() == null ? null : loginRequest.getUsername().trim();
        String password = loginRequest.getPassword() == null ? null : loginRequest.getPassword().trim();

        User user = userRepository.findByUsernameOrEmail(username, username);

        if (user == null) {
            return ResponseEntity.status(404).body("❌ ไม่พบชื่อผู้ใช้งานหรืออีเมลนี้ในระบบ");
        }

        // 🔐 เทียบรหัสผ่านที่กรอก กับ hash ในฐานข้อมูล
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body("❌ รหัสผ่านไม่ถูกต้อง");
        }

        // สร้าง token ฝัง username + role + id
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getId());

        // คืน token + ข้อมูล user (ไม่รวม password) ให้หน้าบ้านเก็บ
        Map<String, Object> body = new HashMap<>();
        body.put("token", token);
        body.put("id", user.getId());
        body.put("username", user.getUsername());
        body.put("email", user.getEmail());
        body.put("role", user.getRole());

        return ResponseEntity.ok(body);
    }
}