package com.gadgetshop.backend_api.controller;

import com.gadgetshop.backend_api.model.Product;
import com.gadgetshop.backend_api.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
// ลบ @CrossOrigin ออก — CORS จัดการรวมที่ SecurityConfig แล้ว
public class ProductController {

    private final ProductRepository productRepository;

    // โฟลเดอร์เก็บรูปในเครื่อง (จะถูกสร้างอัตโนมัติถ้ายังไม่มี)
    private static final String UPLOAD_DIR = "uploads";

    // ชนิดไฟล์รูปที่อนุญาต
    private static final List<String> ALLOWED_TYPES =
            List.of("image/jpeg", "image/png", "image/webp", "image/gif");

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ดึงสินค้าทั้งหมด
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // 🆕 ดึงสินค้าตัวเดียว (สำหรับหน้ารายละเอียด)
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body("❌ ไม่พบสินค้าไอดี: " + id));
    }

    // เพิ่มสินค้าใหม่
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        try {
            Product newProduct = productRepository.save(product);
            return ResponseEntity.ok(newProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("ไม่สามารถเพิ่มสินค้าได้: " + e.getMessage());
        }
    }

    // แก้ไขสินค้า — รองรับ images list ด้วย
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("❌ ไม่พบสินค้าไอดี: " + id));

            product.setName(productDetails.getName());
            product.setPrice(productDetails.getPrice());
            product.setStockQuantity(productDetails.getStockQuantity());
            product.setDescription(productDetails.getDescription());
            product.setImageUrl(productDetails.getImageUrl());

            // 🆕 อัปเดตรูปเสริม (ถ้าส่งมา)
            if (productDetails.getImages() != null) {
                product.setImages(productDetails.getImages());
            }

            Product updatedProduct = productRepository.save(product);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("ไม่สามารถอัปเดตสินค้าได้: " + e.getMessage());
        }
    }

    // ลบสินค้า
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("❌ ไม่พบสินค้าไอดี: " + id));
            productRepository.delete(product);
            return ResponseEntity.ok().body("🗑️ ลบสินค้าเรียบร้อยแล้ว");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("ไม่สามารถลบสินค้าได้: " + e.getMessage());
        }
    }

    // 🆕 ───────────────────────────────────────────────
    //  อัปโหลดรูปได้ทีละหลายไฟล์ — คืน URL array กลับไป
    //  POST /api/products/upload  (form-data, key = "files")
    //  อยู่ใต้ /api/products/** → SecurityConfig ล็อก ADMIN ไว้แล้ว
    // ───────────────────────────────────────────────
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImages(@RequestParam("files") MultipartFile[] files) {
        try {
            // สร้างโฟลเดอร์ uploads ถ้ายังไม่มี
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            List<String> urls = new ArrayList<>();

            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;

                // ตรวจชนิดไฟล์ — รับเฉพาะรูป
                String contentType = file.getContentType();
                if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
                    return ResponseEntity.badRequest()
                            .body("❌ รองรับเฉพาะไฟล์รูป (jpg, png, webp, gif): " + file.getOriginalFilename());
                }

                // ตั้งชื่อไฟล์ใหม่กันชนกัน: uuid + นามสกุลเดิม
                String original = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
                String ext = "";
                int dot = original.lastIndexOf('.');
                if (dot >= 0) ext = original.substring(dot);
                String newName = UUID.randomUUID().toString().replace("-", "") + ext;

                // เซฟไฟล์ลงโฟลเดอร์
                Path target = uploadPath.resolve(newName);
                Files.copy(file.getInputStream(), target);

                // URL ที่ frontend เอาไปใช้ได้ (Spring serve ผ่าน /uploads/**)
                urls.add("http://localhost:8081/uploads/" + newName);
            }

            // คืน { "urls": [...] }
            return ResponseEntity.ok(Map.of("urls", urls));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("❌ อัปโหลดไฟล์ไม่สำเร็จ: " + e.getMessage());
        }
    }
}