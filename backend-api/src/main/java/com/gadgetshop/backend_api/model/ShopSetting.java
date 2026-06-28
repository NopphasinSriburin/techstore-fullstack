package com.gadgetshop.backend_api.model;

import jakarta.persistence.*;

/**
 * เก็บการตั้งค่าร้านแบบ key-value (เลขบัญชี, ชื่อบัญชี, ธนาคาร, URL รูป QR)
 * มีแถวเดียวพอ (id = 1) แอดมินแก้ได้
 */
@Entity
@Table(name = "shop_settings")
public class ShopSetting {

    @Id
    private Long id = 1L;   // ใช้แถวเดียว fix id = 1

    @Column(name = "bank_name")
    private String bankName;         // เช่น "ธนาคารกสิกรไทย"

    @Column(name = "account_name")
    private String accountName;      // ชื่อบัญชี

    @Column(name = "account_number")
    private String accountNumber;    // เลขบัญชี

    @Column(name = "qr_url", length = 500)
    private String qrUrl;            // URL รูป QR พร้อมเพย์ (อัปผ่านระบบ upload)

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getQrUrl() { return qrUrl; }
    public void setQrUrl(String qrUrl) { this.qrUrl = qrUrl; }
}