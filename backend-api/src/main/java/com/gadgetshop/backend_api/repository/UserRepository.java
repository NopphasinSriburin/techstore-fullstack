package com.gadgetshop.backend_api.repository;

import com.gadgetshop.backend_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    // 🔍 สร้างฟังก์ชันค้นหา User ด้วยชื่อบัญชีผู้ใช้
    User findByUsername(String username);

    User findByUsernameOrEmail(String username, String email);
}