package com.stickers.dto;

import com.stickers.entity.UserCreatedSticker;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StickerWithLikesDto {
    private Integer id;
    @JsonProperty("user_id")
    private Integer userId;
    private String name;
    private String category;
    @JsonProperty("image_url")
    private String imageUrl;
    private Object specifications;
    private java.math.BigDecimal price;
    @JsonProperty("is_published")
    private Boolean isPublished;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;
    @JsonProperty("like_count")
    private Long likeCount;
    @JsonProperty("is_liked")
    private Boolean isLiked;

    public static StickerWithLikesDto fromEntity(UserCreatedSticker sticker, Long likeCount, Boolean isLiked) {
        StickerWithLikesDto dto = new StickerWithLikesDto();
        dto.setId(sticker.getId());
        dto.setUserId(sticker.getUserId());
        dto.setName(sticker.getName());
        dto.setCategory(sticker.getCategory());
        dto.setImageUrl(sticker.getImageUrl());
        dto.setSpecifications(sticker.getSpecifications());
        dto.setPrice(sticker.getPrice());
        dto.setIsPublished(sticker.getIsPublished());
        dto.setCreatedAt(sticker.getCreatedAt() != null ? sticker.getCreatedAt().toString() : null);
        dto.setUpdatedAt(sticker.getUpdatedAt() != null ? sticker.getUpdatedAt().toString() : null);
        dto.setLikeCount(likeCount != null ? likeCount : 0L);
        dto.setIsLiked(isLiked != null ? isLiked : false);
        return dto;
    }
}
