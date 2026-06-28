package com.gadgetshop.backend_api.repository;

import com.gadgetshop.backend_api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 🆕 ดึงเฉพาะสินค้าที่ยังไม่ถูกลบ (active = true) — สำหรับหน้าร้าน/ตาราง
    List<Product> findByActiveTrue();
}