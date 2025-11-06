package com.stickers.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CustomStickerRequest {
    private Integer user_id;
    private String name;
    private String category;
    private String image_url;
    private JsonNode specifications;
    private BigDecimal price;
    private Boolean is_published;
}

