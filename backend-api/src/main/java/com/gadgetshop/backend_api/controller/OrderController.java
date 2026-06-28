package com.gadgetshop.backend_api.controller;

import com.gadgetshop.backend_api.model.Order;
import com.gadgetshop.backend_api.repository.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ─────────────────────────────────────────────
    //  ลูกค้า: สร้างออเดอร์ + ดูออเดอร์ของตัวเอง
    // ─────────────────────────────────────────────

    @PostMapping("/orders") // POST สร้างรายการสั่งซื้อใหม่
    public ResponseEntity<?> checkout(@RequestBody Order order) {
        try {
            Order savedOrder = orderService.createOrder(order);
            return ResponseEntity.ok(savedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders(@RequestParam(required = false) String customerUsername) {
        try {
            java.util.List<Order> orders = customerUsername == null || customerUsername.isBlank()
                    ? orderService.getAllOrders()
                    : orderService.getOrdersByCustomerUsername(customerUsername.trim());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("เกิดข้อผิดพลาด: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    //  แอดมิน: ดูออเดอร์ทั้งหมด + เปลี่ยนสถานะ
    //  path /api/admin/** ถูกล็อกให้เฉพาะ ADMIN ที่ SecurityConfig
    // ─────────────────────────────────────────────

    // 🆕 ดึงออเดอร์ทั้งหมดในระบบ (ใหม่สุดก่อน)
    @GetMapping("/admin/orders")
    public ResponseEntity<?> getAllOrdersForAdmin() {
        try {
            return ResponseEntity.ok(orderService.getAllOrders());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("เกิดข้อผิดพลาด: " + e.getMessage());
        }
    }

    // 🆕 เปลี่ยนสถานะออเดอร์
    // body: { "status": "PAID" }
    @PutMapping("/admin/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        try {
            String status = body.get("status");
            Order updated = orderService.updateStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 🆕 ลูกค้าแนบสลิปแจ้งชำระเงิน
    // PUT /api/orders/{id}/slip  body: { "slipUrl": "...", "customerUsername": "..." }
    @PutMapping("/orders/{id}/slip")
    public ResponseEntity<?> attachSlip(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        try {
            String slipUrl = body.get("slipUrl");
            String username = body.get("customerUsername");
            Order updated = orderService.attachSlip(id, slipUrl, username);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 🆕 ลูกค้ายกเลิกออเดอร์ตัวเอง (เฉพาะ PENDING)
    // PUT /api/orders/{id}/cancel  body: { "customerUsername": "..." }
    @PutMapping("/orders/{id}/cancel")
    public ResponseEntity<?> cancelByCustomer(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        try {
            String username = body.get("customerUsername");
            Order cancelled = orderService.cancelByCustomer(id, username);
            return ResponseEntity.ok(cancelled);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}