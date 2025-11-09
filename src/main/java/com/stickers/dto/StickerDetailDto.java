package com.stickers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StickerDetailDto {
    private Integer id;
    private String name;
    @JsonProperty("image_url")
    private String imageUrl;
    private List<String> colors;
    private List<String> finishes;
    private BigDecimal price;
    @JsonProperty("template_title")
    private String templateTitle;
    @JsonProperty("template_id")
    private Integer templateId;
    @JsonProperty("sticker_type")
    private String stickerType; // "template" or "user_created"
    @JsonProperty("designed_by")
    private String designedBy; // User name or "Stickkery"
    @JsonProperty("creator_id")
    private Integer creatorId;
    @JsonProperty("creator_username")
    private String creatorUsername;
    @JsonProperty("like_count")
    private Long likeCount;
    @JsonProperty("is_liked")
    private Boolean isLiked;
    private Object specifications; // For user-created stickers
    private String category; // For user-created stickers
}

