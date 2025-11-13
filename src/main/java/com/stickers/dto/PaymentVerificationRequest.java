package com.stickers.dto;

import lombok.Data;

@Data
public class PaymentVerificationRequest {
    private String razorpay_order_id;
    private String razorpay_payment_id;
    private String razorpay_signature;
    private Integer user_id;
}


