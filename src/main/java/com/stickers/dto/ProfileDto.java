package com.stickers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto {
    private Integer id;
    private String name;
    private String username;
    private String email;
    private String profileImageUrl;
    private Long stickerCount;
    private Long likesCount;
}


