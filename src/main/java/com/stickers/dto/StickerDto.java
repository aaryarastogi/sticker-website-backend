package com.stickers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StickerDto {
    private Integer id;
    private Integer template_id;
    private String name;
    private String image_url;
    private List<String> colors;
    private List<String> finishes;
    private BigDecimal price;
    private String currency; // Currency code (USD, INR, etc.)
    private String template_title;
    private String sticker_type; // "template" or "user_created"
    private Long like_count;
    private Boolean is_liked;
    
    // Constructor without like fields for backward compatibility
    public StickerDto(Integer id, Integer template_id, String name, String image_url, 
                     List<String> colors, List<String> finishes, BigDecimal price, String template_title) {
        this.id = id;
        this.template_id = template_id;
        this.name = name;
        this.image_url = image_url;
        this.colors = colors;
        this.finishes = finishes;
        this.price = price;
        this.template_title = template_title;
        this.like_count = 0L;
        this.is_liked = false;
    }
}

