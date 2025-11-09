package com.stickers.controller;

import com.stickers.dto.StickerDetailDto;
import com.stickers.service.StickerDetailService;
import com.stickers.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stickers")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"},
        allowCredentials = "true")
public class StickerDetailController {
    
    @Autowired
    private StickerDetailService stickerDetailService;
    
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
    
    @GetMapping("/{stickerId}")
    public ResponseEntity<?> getStickerDetail(
            @PathVariable Integer stickerId,
            @RequestParam String type,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Integer userId = getUserIdFromAuthHeader(authHeader);
            StickerDetailDto sticker = stickerDetailService.getStickerDetail(stickerId, type, userId);
            return ResponseEntity.ok(sticker);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch sticker details: " + e.getMessage()));
        }
    }
}

