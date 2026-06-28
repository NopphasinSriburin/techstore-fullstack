package com.gadgetshop.backend_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price; // ใช้ BigDecimal สำหรับเก็บราคาสินค้า เพื่อความแม่นยำเรื่องทศนิยม

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    // 🖼️ รูปหลัก — โชว์ในการ์ด/ตาราง (เหมือนเดิม)
    @Column(name = "image_url")
    private String imageUrl;

    // 🆕 รูปเสริมหลายรูป — โชว์ในหน้ารายละเอียด
    // เก็บใน table แยกชื่อ product_images (Hibernate สร้างให้อัตโนมัติ)
    // ใช้ EAGER เพื่อให้รูปโหลดมาพร้อมสินค้าเสมอ (กัน lazy loading exception
    // ตอน frontend เรียก getAllProducts แล้วต้องการ images ทันที)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "product_images",
        joinColumns = @JoinColumn(name = "product_id")
    )
    @Column(name = "image_url", length = 500)
    private List<String> images = new ArrayList<>();

    // เชื่อมความสัมพันธ์: Products หลายชิ้น สามารถอยู่ใน Category เดียวกันได้
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false) // สร้างคอลัมน์ category_id เป็น Foreign Key เชื่อมไปตาราง categories
    private Category category;
}