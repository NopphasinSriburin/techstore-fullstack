package com.gadgetshop.backend_api.controller;

import com.gadgetshop.backend_api.model.ShopSetting;
import com.gadgetshop.backend_api.repository.ShopSettingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ข้อมูลร้าน (เลขบัญชี/QR สำหรับลูกค้าโอน)
 * - GET  /api/shop-setting        : ทุกคนดูได้ (ลูกค้าต้องเห็นตอนจะโอน)
 * - PUT  /api/admin/shop-setting  : เฉพาะแอดมินแก้ได้
 */
@RestController
@RequestMapping("/api")
public class ShopSettingController {

    private final ShopSettingRepository repo;

    public ShopSettingController(ShopSettingRepository repo) {
        this.repo = repo;
    }

    // ลูกค้าดูข้อมูลบัญชี/QR
    @GetMapping("/shop-setting")
    public ResponseEntity<?> getSetting() {
        ShopSetting setting = repo.findById(1L).orElseGet(() -> {
            ShopSetting s = new ShopSetting();
            s.setId(1L);
            return s; // คืนค่าว่างถ้าแอดมินยังไม่ตั้งค่า (ยังไม่เซฟ)
        });
        return ResponseEntity.ok(setting);
    }

    // แอดมินตั้งค่า/แก้ไข
    @PutMapping("/admin/shop-setting")
    public ResponseEntity<?> updateSetting(@RequestBody ShopSetting body) {
        try {
            ShopSetting setting = repo.findById(1L).orElseGet(() -> {
                ShopSetting s = new ShopSetting();
                s.setId(1L);
                return s;
            });
            setting.setBankName(body.getBankName());
            setting.setAccountName(body.getAccountName());
            setting.setAccountNumber(body.getAccountNumber());
            setting.setQrUrl(body.getQrUrl());
            return ResponseEntity.ok(repo.save(setting));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("บันทึกไม่สำเร็จ: " + e.getMessage());
        }
    }
}