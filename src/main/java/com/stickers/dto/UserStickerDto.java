package com.stickers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStickerDto {
    private Integer id;
    private String imageUrl;
    private String category;
    private Boolean isPublished;
    private String status;
    private String adminNote;
    private LocalDateTime createdAt;
}

