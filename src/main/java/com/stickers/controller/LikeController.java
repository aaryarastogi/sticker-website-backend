package com.stickers.controller;

import com.stickers.service.LikeService;
import com.stickers.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/likes")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"},
        allowCredentials = "true")
public class LikeController {
    
    @Autowired
    private LikeService likeService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private Integer getUserIdFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization token is missing or invalid");
        }
        String token = authHeader.substring(7);
        return jwtUtil.getUserIdFromToken(token);
    }
    
    @PostMapping("/toggle/{stickerId}")
    public ResponseEntity<?> toggleLike(
            @PathVariable Integer stickerId,
            @RequestParam(defaultValue = "user_created") String stickerType,
            @RequestHeader(value = "Authorization") String authHeader) {
        try {
            Integer userId = getUserIdFromAuthHeader(authHeader);
            boolean isLiked = likeService.toggleLike(userId, stickerId, stickerType);
            long likeCount = likeService.getLikeCount(stickerId, stickerType);
            
            return ResponseEntity.ok(Map.of(
                "isLiked", isLiked,
                "likeCount", likeCount
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to toggle like: " + e.getMessage()));
        }
    }
    
    @GetMapping("/check/{stickerId}")
    public ResponseEntity<?> checkLike(
            @PathVariable Integer stickerId,
            @RequestParam(defaultValue = "user_created") String stickerType,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Integer userId = null;
            boolean isLiked = false;
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    userId = getUserIdFromAuthHeader(authHeader);
                    isLiked = likeService.isLiked(userId, stickerId, stickerType);
                } catch (Exception e) {
                    // User not authenticated, isLiked remains false
                }
            }
            
            long likeCount = likeService.getLikeCount(stickerId, stickerType);
            
            return ResponseEntity.ok(Map.of(
                "isLiked", isLiked,
                "likeCount", likeCount
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to check like: " + e.getMessage()));
        }
    }
    
    @GetMapping("/count/{stickerId}")
    public ResponseEntity<?> getLikeCount(
            @PathVariable Integer stickerId,
            @RequestParam(defaultValue = "user_created") String stickerType) {
        try {
            long likeCount = likeService.getLikeCount(stickerId, stickerType);
            return ResponseEntity.ok(Map.of("likeCount", likeCount));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to get like count: " + e.getMessage()));
        }
    }
}

