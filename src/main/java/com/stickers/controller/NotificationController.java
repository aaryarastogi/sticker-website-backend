package com.stickers.controller;

import com.stickers.service.NotificationService;
import com.stickers.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"},
        allowCredentials = "true")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private Integer getUserIdFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization token is missing or invalid");
        }
        String token = authHeader.substring(7);
        return jwtUtil.getUserIdFromToken(token);
    }
    
    @GetMapping
    public ResponseEntity<?> getNotifications(@RequestHeader(value = "Authorization") String authHeader) {
        try {
            Integer userId = getUserIdFromAuthHeader(authHeader);
            return ResponseEntity.ok(notificationService.getUserNotifications(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to fetch notifications: " + e.getMessage()));
        }
    }
    
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(@RequestHeader(value = "Authorization") String authHeader) {
        try {
            Integer userId = getUserIdFromAuthHeader(authHeader);
            return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to fetch unread notifications: " + e.getMessage()));
        }
    }
    
    @GetMapping("/unread/count")
    public ResponseEntity<?> getUnreadCount(@RequestHeader(value = "Authorization") String authHeader) {
        try {
            Integer userId = getUserIdFromAuthHeader(authHeader);
            long count = notificationService.getUnreadCount(userId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to get unread count: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Integer notificationId,
            @RequestHeader(value = "Authorization") String authHeader) {
        try {
            Integer userId = getUserIdFromAuthHeader(authHeader);
            notificationService.markAsRead(notificationId, userId);
            return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to mark notification as read: " + e.getMessage()));
        }
    }
    
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(@RequestHeader(value = "Authorization") String authHeader) {
        try {
            Integer userId = getUserIdFromAuthHeader(authHeader);
            notificationService.markAllAsRead(userId);
            return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to mark all notifications as read: " + e.getMessage()));
        }
    }
}

