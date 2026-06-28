package com.gadgetshop.backend_api.repository;

import com.gadgetshop.backend_api.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // สืบทอดคำสั่งเซฟอัตโนมัติ ไม่ต้องเขียนอะไรเพิ่มในนี้ครับ

    List<Order> findByCustomerUsernameOrderByOrderDateDesc(String customerUsername);

    // 🆕 ดึงออเดอร์ทั้งหมดเรียงจากใหม่ไปเก่า (สำหรับหน้าแอดมิน)
    List<Order> findAllByOrderByOrderDateDesc();
}