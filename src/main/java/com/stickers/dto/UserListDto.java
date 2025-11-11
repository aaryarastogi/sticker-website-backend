package com.stickers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListDto {
    private Integer id;
    private String name;
    private String username;
    private String email;
    private String profileImageUrl;
    private Long stickerCount;
    private LocalDateTime createdAt;
    private Boolean isActive;
}

