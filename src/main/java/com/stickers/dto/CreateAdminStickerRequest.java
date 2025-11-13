package com.stickers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAdminStickerRequest {
    private String name;
    private String imageUrl;
    private String category; // Optional - for reference
    private BigDecimal price;
    private String currency; // Currency code (USD, INR, etc.)
    private Boolean isPublished; // Not used for template stickers, but kept for consistency
    private Integer templateId; // Optional - can be null for admin-created stickers
}

