package com.gadgetshop.backend_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity                  // บอก Java ว่า คลาสนี้คือตารางใน Database นะ
@Table(name = "categories") // ตั้งชื่อตารางใน Database ว่า "categories"
@Getter                  // ใช้ Lombok เพื่อสร้างฟังก์ชันดึงข้อมูล (getId, getName) ให้เราอัตโนมัติ
@Setter                  // ใช้ Lombok เพื่อสร้างฟังก์ชันแก้ไขข้อมูล (setName, setDescription) ให้เราอัตโนมัติ
public class Category {

    @Id // บอกว่าเป็น Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ให้มันรันเลข ID เพิ่มขึ้นทีละ 1 อัตโนมัติ (1, 2, 3, ...)
    private Long id;

    @Column(nullable = false, length = 100) // ห้ามเป็นค่าว่าง และยาวไม่เกิน 100 ตัวอักษร
    private String name;

    @Column(columnDefinition = "TEXT") // เก็บข้อความขนาดยาว (รายละเอียดหมวดหมู่)
    private String description;
}