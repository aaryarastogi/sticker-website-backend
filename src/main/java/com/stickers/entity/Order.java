package com.stickers.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "user_id", nullable = false)
    @JsonProperty("user_id")
    private Integer userId;
    
    @Column(name = "order_number", unique = true, nullable = false)
    @JsonProperty("order_number")
    private String orderNumber;
    
    @Column(name = "razorpay_order_id", unique = true)
    @JsonProperty("razorpay_order_id")
    private String razorpayOrderId;
    
    @Column(name = "razorpay_payment_id")
    @JsonProperty("razorpay_payment_id")
    private String razorpayPaymentId;
    
    @Column(name = "razorpay_signature")
    private String razorpaySignature;
    
    @Column(name = "amount", nullable = false, columnDefinition = "DECIMAL(10, 2)")
    private BigDecimal amount;
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    
    @Column(name = "status", nullable = false, length = 50)
    private String status; // PENDING, PAID, FAILED, REFUNDED
    
    @Column(name = "order_type", nullable = false, length = 50)
    @JsonProperty("order_type")
    private String orderType; // CUSTOM_STICKER, CART_PURCHASE
    
    @Column(name = "order_data", columnDefinition = "TEXT")
    @JsonProperty("order_data")
    private String orderData; // JSON string of order items
    
    @Column(name = "created_at")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "paid_at")
    @JsonProperty("paid_at")
    private LocalDateTime paidAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if ("PAID".equals(status) && paidAt == null) {
            paidAt = LocalDateTime.now();
        }
    }
}


