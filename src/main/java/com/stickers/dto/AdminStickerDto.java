package com.stickers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminStickerDto {
    private Integer id;
    private String name;
    private String imageUrl;
    private String category;
    private BigDecimal price;
    private String creatorType; // "stickkery" or "user"
    private String creatorName; // "Stickkery" or user's name
    private Integer creatorId; // null for Stickkery, userId for user-created
    private String stickerType; // "template" or "user_created"
    private Boolean isPublished; // Only for user-created stickers
    private String status;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

