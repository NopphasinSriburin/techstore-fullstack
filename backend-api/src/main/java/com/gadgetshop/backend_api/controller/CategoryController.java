package com.gadgetshop.backend_api.controller;

import com.gadgetshop.backend_api.model.Category;
import com.gadgetshop.backend_api.repository.CategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * จัดการหมวดหมู่สินค้า
 * - GET  /api/categories        : ทุกคนดูได้ (ใช้เติม dropdown ในฟอร์ม)
 * - POST /api/categories        : เฉพาะแอดมิน (สร้างหมวดหมู่ใหม่)
 * - DELETE /api/categories/{id} : เฉพาะแอดมิน
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // ดึงหมวดหมู่ทั้งหมด
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // สร้างหมวดหมู่ใหม่
    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        try {
            if (category.getName() == null || category.getName().isBlank()) {
                return ResponseEntity.badRequest().body("❌ กรุณากรอกชื่อหมวดหมู่");
            }
            Category saved = categoryRepository.save(category);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("ไม่สามารถสร้างหมวดหมู่ได้: " + e.getMessage());
        }
    }

    // ลบหมวดหมู่
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("❌ ไม่พบหมวดหมู่ไอดี: " + id));
            categoryRepository.delete(category);
            return ResponseEntity.ok().body("🗑️ ลบหมวดหมู่เรียบร้อย");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("ไม่สามารถลบหมวดหมู่ได้ (อาจมีสินค้าใช้อยู่): " + e.getMessage());
        }
    }
}