package com.stickers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchDto {
    private Integer id;
    private String name;
    private String username;
    @JsonProperty("profile_image_url")
    private String profileImageUrl;
}

