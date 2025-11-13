package com.stickers.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.stickers.dto.CreateOrderRequest;
import com.stickers.dto.OrderResponse;
import com.stickers.dto.PaymentVerificationRequest;
import com.stickers.entity.Order;
import com.stickers.repository.OrderRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PaymentService {
    
    @Value("${razorpay.key.id}")
    private String razorpayKeyId;
    
    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;
    
    @Value("${razorpay.mode:TEST}")
    private String razorpayMode;
    
    private final OrderRepository orderRepository;
    
    public PaymentService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    public OrderResponse createOrder(CreateOrderRequest request) throws RazorpayException {
        try {
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            
            // Convert amount to paise (smallest currency unit)
            // Razorpay expects amount in smallest currency unit
            // For INR: 1 rupee = 100 paise
            // For USD: 1 dollar = 100 cents
            long amountInSmallestUnit = convertToSmallestUnit(request.getAmount(), request.getCurrency());
            
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInSmallestUnit);
            orderRequest.put("currency", request.getCurrency());
            orderRequest.put("receipt", generateOrderNumber());
            orderRequest.put("notes", new JSONObject()
                .put("order_type", request.getOrder_type())
                .put("user_id", request.getUser_id().toString())
            );
            
            com.razorpay.Order razorpayOrder = razorpay.orders.create(orderRequest);
            
            // Save order to database
            Order order = new Order();
            order.setUserId(request.getUser_id());
            order.setOrderNumber(generateOrderNumber());
            order.setRazorpayOrderId(razorpayOrder.get("id"));
            order.setAmount(request.getAmount());
            order.setCurrency(request.getCurrency());
            order.setStatus("PENDING");
            order.setOrderType(request.getOrder_type());
            order.setOrderData(convertItemsToJson(request.getItems()));
            
            order = orderRepository.save(order);
            
            OrderResponse response = new OrderResponse();
            response.setId(order.getId());
            response.setOrderNumber(order.getOrderNumber());
            response.setRazorpayOrderId(order.getRazorpayOrderId());
            response.setAmount(order.getAmount());
            response.setCurrency(order.getCurrency());
            response.setStatus(order.getStatus());
            response.setOrderType(order.getOrderType());
            response.setCreatedAt(order.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return response;
        } catch (RazorpayException e) {
            throw new RuntimeException("Failed to create Razorpay order: " + e.getMessage(), e);
        }
    }
    
    @Transactional
    public boolean verifyPayment(PaymentVerificationRequest request) {
        try {
            // Find order by razorpay_order_id
            Order order = orderRepository.findByRazorpayOrderId(request.getRazorpay_order_id())
                .orElseThrow(() -> new RuntimeException("Order not found"));
            
            // Verify signature
            String generatedSignature = generateSignature(
                request.getRazorpay_order_id(),
                request.getRazorpay_payment_id(),
                razorpayKeySecret
            );
            
            boolean isValid = generatedSignature.equals(request.getRazorpay_signature());
            
            if (isValid) {
                // Update order status
                order.setRazorpayPaymentId(request.getRazorpay_payment_id());
                order.setRazorpaySignature(request.getRazorpay_signature());
                order.setStatus("PAID");
                order.setPaidAt(LocalDateTime.now());
                orderRepository.save(order);
                return true;
            } else {
                order.setStatus("FAILED");
                orderRepository.save(order);
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException("Payment verification failed: " + e.getMessage(), e);
        }
    }
    
    public List<OrderResponse> getUserOrders(Integer userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        return convertToResponse(order);
    }
    
    private String generateSignature(String orderId, String paymentId, String secret) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }
    
    private long convertToSmallestUnit(java.math.BigDecimal amount, String currency) {
        // Convert to smallest currency unit
        // INR: 1 rupee = 100 paise
        // USD, EUR, GBP, etc.: 1 unit = 100 cents
        int multiplier = 100;
        return amount.multiply(java.math.BigDecimal.valueOf(multiplier)).longValue();
    }
    
    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + 
               "-" + System.currentTimeMillis();
    }
    
    private String convertItemsToJson(List<java.util.Map<String, Object>> items) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(items);
        } catch (Exception e) {
            return "[]";
        }
    }
    
    private OrderResponse convertToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setRazorpayOrderId(order.getRazorpayOrderId());
        response.setRazorpayPaymentId(order.getRazorpayPaymentId());
        response.setAmount(order.getAmount());
        response.setCurrency(order.getCurrency());
        response.setStatus(order.getStatus());
        response.setOrderType(order.getOrderType());
        response.setCreatedAt(order.getCreatedAt() != null ? order.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        response.setPaidAt(order.getPaidAt() != null ? order.getPaidAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        
        // Parse order data to include items
        try {
            if (order.getOrderData() != null && !order.getOrderData().isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> orderItems = mapper.readValue(
                    order.getOrderData(), 
                    new TypeReference<List<Map<String, Object>>>() {}
                );
                response.setOrderData(orderItems);
            }
        } catch (Exception e) {
            // If parsing fails, set empty list
            response.setOrderData(new java.util.ArrayList<>());
        }
        
        return response;
    }
    
    public String getRazorpayKeyId() {
        return razorpayKeyId;
    }
}

