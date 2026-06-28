package com.gadgetshop.backend_api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders") // ใช้ชื่อ orders เติม s เพื่อไม่ให้ซ้ำกับคำสั่ง SQL
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime orderDate;
    private BigDecimal totalPrice;
    private String customerUsername;

    // สถานะออเดอร์: PENDING, PAID, SHIPPED, COMPLETED, CANCELLED
    @Column(nullable = false)
    private String status = "PENDING";

    // 🆕 URL รูปสลิปที่ลูกค้าแนบ (null = ยังไม่แจ้งชำระ)
    @Column(name = "slip_url", length = 500)
    private String slipUrl;

    // 🆕 เวลาที่ลูกค้าแจ้งชำระเงิน (แนบสลิป)
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // 🔗 ความสัมพันธ์แบบ One-to-Many: 1 ออเดอร์ มีสินค้าข้างในได้หลายรายการ
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id") // ไปสร้างคอลัมน์ order_id ที่ตาราง order_items เพื่อผูกกัน
    private List<OrderItem> items;

    // --- Getter / Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getCustomerUsername() { return customerUsername; }
    public void setCustomerUsername(String customerUsername) { this.customerUsername = customerUsername; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSlipUrl() { return slipUrl; }
    public void setSlipUrl(String slipUrl) { this.slipUrl = slipUrl; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}