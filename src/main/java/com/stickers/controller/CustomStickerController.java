package com.stickers.controller;

import com.stickers.dto.CustomStickerRequest;
import com.stickers.dto.StickerWithLikesDto;
import com.stickers.entity.UserCreatedSticker;
import com.stickers.service.CustomStickerService;
import com.stickers.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/custom-stickers")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"}, 
             allowCredentials = "true")
public class CustomStickerController {
    @Autowired
    private CustomStickerService stickerService;
    
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
    
    @PostMapping
    public ResponseEntity<?> createSticker(@RequestBody CustomStickerRequest request) {
        try {
            if (request.getUser_id() == null || request.getName() == null || 
                request.getCategory() == null || request.getImage_url() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Missing required fields: user_id, name, category, image_url"));
            }
            
            UserCreatedSticker sticker = stickerService.createSticker(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(sticker);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create sticker", "message", e.getMessage()));
        }
    }
    
    @GetMapping("/published")
    public ResponseEntity<?> getPublishedStickers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Integer userId = getUserIdFromAuthHeader(authHeader);
            List<StickerWithLikesDto> stickers = stickerService.getPublishedStickersWithLikes(userId);
            return ResponseEntity.ok(stickers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch published stickers", "message", e.getMessage()));
        }
    }
    
    @GetMapping("/my-stickers/{userId}")
    public ResponseEntity<?> getMyStickers(
            @PathVariable Integer userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Integer currentUserId = getUserIdFromAuthHeader(authHeader);
            List<StickerWithLikesDto> stickers = stickerService.getUserStickersWithLikes(userId, currentUserId);
            return ResponseEntity.ok(stickers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch stickers", "message", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSticker(@PathVariable Integer id, @RequestParam Integer user_id) {
        try {
            stickerService.deleteSticker(id, user_id);
            return ResponseEntity.ok(Map.of("message", "Sticker deleted successfully"));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to delete sticker", "message", e.getMessage()));
        }
    }
    
    @PatchMapping("/{id}/publish")
    public ResponseEntity<?> updatePublishStatus(@PathVariable Integer id, 
                                                 @RequestBody Map<String, Object> request) {
        try {
            Integer userId = null;
            Boolean isPublished = null;
            
            if (request.containsKey("user_id")) {
                userId = Integer.valueOf(request.get("user_id").toString());
            }
            if (request.containsKey("is_published")) {
                isPublished = Boolean.valueOf(request.get("is_published").toString());
            }
            
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "user_id is required"));
            }
            
            UserCreatedSticker sticker = stickerService.updatePublishStatus(id, userId, isPublished);
            return ResponseEntity.ok(sticker);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update publish status", "message", e.getMessage()));
        }
    }
}

