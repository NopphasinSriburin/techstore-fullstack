package com.gadgetshop.backend_api.repository;

import com.gadgetshop.backend_api.model.Order;
import com.gadgetshop.backend_api.model.OrderItem;
import com.gadgetshop.backend_api.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    private final ProductRepository productRepository;

    // รายชื่อสถานะที่อนุญาต — กันแอดมินส่งค่ามั่ว
    private static final Set<String> ALLOWED_STATUS =
            Set.of("PENDING", "PAID", "SHIPPED", "COMPLETED", "CANCELLED");

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional // 🛡️ ป้องกันข้อมูลพัง ถ้าตัดสต็อกชิ้นใดชิ้นหนึ่งไม่ผ่าน ระบบจะสั่งยกเลิกทั้งหมด (Rollback) ทันที
    public Order createOrder(Order order) {
        order.setOrderDate(LocalDateTime.now()); // บันทึกเวลาปัจจุบันที่สั่งซื้อ
        order.setStatus("PENDING");              // 🆕 ออเดอร์ใหม่เริ่มที่ "รอชำระเงิน" เสมอ

        // 🔄 ลูปตรวจสอบและตัดสต็อกสินค้าทีละชิ้นในตะกร้า
        for (OrderItem item : order.getItems()) {
            // ดึงข้อมูลสินค้าล่าสุดจากตารางหลักมาเช็กสต็อก
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("❌ ไม่พบสินค้าไอดี: " + item.getProduct().getId()));

            // ตรวจสอบว่าในคลังพอขายไหม
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("❌ สินค้า " + product.getName() + " มีจำนวนไม่พอในคลัง (คงเหลือ " + product.getStockQuantity() + " ชิ้น)");
            }

            // 🎯 คำนวณตัดสต็อกชิ้นที่ผ่าน
            int newStock = product.getStockQuantity() - item.getQuantity();
            product.setStockQuantity(newStock);

            // เซฟจำนวนสต็อกใหม่กลับลงฐานข้อมูลสินค้า
            productRepository.save(product);

            // ยัดข้อมูลสินค้า และราคาปัจจุบัน เข้าไปในแถว OrderItem
            item.setProduct(product);
            item.setPrice(product.getPrice());
        }

        // คำนวณยอดรวมของออเดอร์จากราคาสินค้าและจำนวน
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            totalPrice = totalPrice.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.setTotalPrice(totalPrice);

        // เซฟข้อมูลออเดอร์ลงตารางออเดอร์
        return orderRepository.save(order);
    }

    // ➕ ดึงออเดอร์ทั้งหมด (ใหม่สุดขึ้นก่อน) — สำหรับแอดมิน
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    public List<Order> getOrdersByCustomerUsername(String customerUsername) {
        return orderRepository.findByCustomerUsernameOrderByOrderDateDesc(customerUsername);
    }

    // 🆕 เปลี่ยนสถานะออเดอร์ (สำหรับแอดมิน)
    @Transactional
    public Order updateStatus(Long orderId, String newStatus) {
        if (newStatus == null || !ALLOWED_STATUS.contains(newStatus.toUpperCase())) {
            throw new RuntimeException("❌ สถานะไม่ถูกต้อง: " + newStatus);
        }
        newStatus = newStatus.toUpperCase();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("❌ ไม่พบออเดอร์ไอดี: " + orderId));

        String oldStatus = order.getStatus();

        // 🔄 ถ้าเปลี่ยนเป็น "ยกเลิก" และก่อนหน้านี้ยังไม่ได้ยกเลิก → คืนสต็อกกลับ
        if ("CANCELLED".equals(newStatus) && !"CANCELLED".equals(oldStatus)) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product != null) {
                    Product fresh = productRepository.findById(product.getId()).orElse(null);
                    if (fresh != null) {
                        fresh.setStockQuantity(fresh.getStockQuantity() + item.getQuantity());
                        productRepository.save(fresh);
                    }
                }
            }
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    // 🆕 ลูกค้าแนบสลิปแจ้งชำระเงิน
    @Transactional
    public Order attachSlip(Long orderId, String slipUrl, String requesterUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("❌ ไม่พบออเดอร์ไอดี: " + orderId));

        if (requesterUsername != null && !requesterUsername.equals(order.getCustomerUsername())) {
            throw new RuntimeException("❌ ไม่มีสิทธิ์แนบสลิปออเดอร์นี้");
        }
        if ("COMPLETED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new RuntimeException("❌ ออเดอร์นี้ปิดแล้ว ไม่สามารถแนบสลิปได้");
        }
        if (slipUrl == null || slipUrl.isBlank()) {
            throw new RuntimeException("❌ ไม่พบ URL สลิป");
        }

        order.setSlipUrl(slipUrl);
        order.setPaidAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    // 🆕 ลูกค้ายกเลิกออเดอร์ตัวเอง (ได้เฉพาะตอน PENDING)
    @Transactional
    public Order cancelByCustomer(Long orderId, String requesterUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("❌ ไม่พบออเดอร์ไอดี: " + orderId));

        // เช็คว่าเป็นออเดอร์ของลูกค้าคนนี้จริง
        if (requesterUsername == null || !requesterUsername.equals(order.getCustomerUsername())) {
            throw new RuntimeException("❌ ไม่มีสิทธิ์ยกเลิกออเดอร์นี้");
        }

        // ยกเลิกได้เฉพาะตอนรอชำระ (PENDING) เท่านั้น
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("❌ ยกเลิกได้เฉพาะออเดอร์ที่รอชำระเงินเท่านั้น หากชำระแล้วกรุณาติดต่อแอดมิน");
        }

        // คืนสต็อกกลับ
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product != null) {
                Product fresh = productRepository.findById(product.getId()).orElse(null);
                if (fresh != null) {
                    fresh.setStockQuantity(fresh.getStockQuantity() + item.getQuantity());
                    productRepository.save(fresh);
                }
            }
        }

        order.setStatus("CANCELLED");
        return orderRepository.save(order);
    }
}