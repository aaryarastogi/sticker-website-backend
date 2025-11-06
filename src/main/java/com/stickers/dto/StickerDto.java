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
    private String template_title;
}

