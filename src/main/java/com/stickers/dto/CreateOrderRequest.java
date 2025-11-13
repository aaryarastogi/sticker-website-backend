package com.stickers.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class CreateOrderRequest {
    private Integer user_id;
    private BigDecimal amount;
    private String currency;
    private String order_type; // CUSTOM_STICKER, CART_PURCHASE
    private List<Map<String, Object>> items; // Order items details
}


