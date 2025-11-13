package com.stickers.controller;

import com.stickers.dto.CreateOrderRequest;
import com.stickers.dto.OrderResponse;
import com.stickers.dto.PaymentVerificationRequest;
import com.stickers.service.PaymentService;
import com.stickers.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"}, 
             allowCredentials = "true")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private Integer getUserIdFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            String token = authHeader.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }
    
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Verify user authentication
            Integer userIdFromToken = getUserIdFromAuthHeader(authHeader);
            if (userIdFromToken == null || !userIdFromToken.equals(request.getUser_id())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
            }
            
            if (request.getUser_id() == null || request.getAmount() == null || 
                request.getCurrency() == null || request.getOrder_type() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Missing required fields"));
            }
            
            OrderResponse order = paymentService.createOrder(request);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create order", "message", e.getMessage()));
        }
    }
    
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationRequest request,
                                          @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Verify user authentication
            Integer userIdFromToken = getUserIdFromAuthHeader(authHeader);
            if (userIdFromToken == null || !userIdFromToken.equals(request.getUser_id())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
            }
            
            if (request.getRazorpay_order_id() == null || request.getRazorpay_payment_id() == null ||
                request.getRazorpay_signature() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Missing payment details"));
            }
            
            boolean isValid = paymentService.verifyPayment(request);
            
            if (isValid) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Payment verified successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", "Payment verification failed"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Payment verification failed", "message", e.getMessage()));
        }
    }
    
    @GetMapping("/orders")
    public ResponseEntity<?> getUserOrders(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Integer userId = getUserIdFromAuthHeader(authHeader);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
            }
            
            List<OrderResponse> orders = paymentService.getUserOrders(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch orders", "message", e.getMessage()));
        }
    }
    
    @GetMapping("/key")
    public ResponseEntity<?> getRazorpayKey() {
        try {
            String keyId = paymentService.getRazorpayKeyId();
            return ResponseEntity.ok(Map.of("key_id", keyId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get Razorpay key"));
        }
    }
}


