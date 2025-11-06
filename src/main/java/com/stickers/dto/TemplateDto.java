package com.stickers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDto {
    private Integer id;
    private String title;
    private String image_url;
    private Boolean is_trending;
    private String category_name;
    private Integer category_id;
}

