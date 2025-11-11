package com.stickers.controller;

import com.stickers.dto.StickerDto;
import com.stickers.dto.TemplateDto;
import com.stickers.entity.Category;
import com.stickers.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"}, 
             allowCredentials = "true")
public class TemplateController {
    @Autowired
    private TemplateService templateService;
    
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        try {
            // Return published categories, or categories that don't have isPublished set (for backward compatibility)
            List<Category> categories = templateService.getAllCategories();
            List<Category> publishedCategories = categories.stream()
                .filter(category -> {
                    // If isPublished is null (old categories), show them for backward compatibility
                    // Otherwise, only show if isPublished is true
                    return category.getIsPublished() == null || category.getIsPublished();
                })
                .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(publishedCategories);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam String q) {
        try {
            if (q == null || q.trim().isEmpty()) {
                Map<String, Object> empty = new HashMap<>();
                empty.put("stickers", List.of());
                empty.put("templates", List.of());
                empty.put("users", List.of());
                return ResponseEntity.ok(empty);
            }
            
            List<StickerDto> stickers = templateService.searchStickers(q);
            List<TemplateDto> templates = templateService.searchTemplates(q);
            List<com.stickers.dto.UserSearchDto> users = templateService.searchUsers(q);
            
            Map<String, Object> result = new HashMap<>();
            result.put("stickers", stickers);
            result.put("templates", templates);
            result.put("users", users);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<TemplateDto>> getTemplates(
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Boolean trending) {
        try {
            List<TemplateDto> templates = templateService.getAllTemplates(category, trending);
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/{identifier}/stickers")
    public ResponseEntity<List<StickerDto>> getStickersByTemplate(
            @PathVariable String identifier,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            List<StickerDto> stickers;
            if (identifier.matches("\\d+")) {
                // Numeric ID
                stickers = templateService.getStickersByTemplate(Integer.parseInt(identifier), authHeader);
            } else {
                // Template title (URL decoded automatically)
                stickers = templateService.getStickersByTemplateTitle(identifier, authHeader);
            }
            return ResponseEntity.ok(stickers);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TemplateDto> getTemplate(@PathVariable Integer id) {
        try {
            return templateService.getTemplateById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/trending/stickers")
    public ResponseEntity<List<StickerDto>> getTrendingStickers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            List<StickerDto> stickers = templateService.getTrendingStickers(authHeader);
            return ResponseEntity.ok(stickers);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}

