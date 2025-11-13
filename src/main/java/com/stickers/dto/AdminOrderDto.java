package com.stickers.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class AdminOrderDto {
    private Integer id;
    
    @JsonProperty("order_number")
    private String orderNumber;
    
    @JsonProperty("razorpay_order_id")
    private String razorpayOrderId;
    
    @JsonProperty("razorpay_payment_id")
    private String razorpayPaymentId;
    
    @JsonProperty("user_id")
    private Integer userId;
    
    @JsonProperty("user_name")
    private String userName;
    
    @JsonProperty("user_email")
    private String userEmail;
    
    private BigDecimal amount;
    private String currency;
    private String status;
    
    @JsonProperty("order_type")
    private String orderType;
    
    @JsonProperty("order_data")
    private List<Map<String, Object>> orderData;
    
    @JsonProperty("item_count")
    private Integer itemCount;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    @JsonProperty("paid_at")
    private String paidAt;
}

