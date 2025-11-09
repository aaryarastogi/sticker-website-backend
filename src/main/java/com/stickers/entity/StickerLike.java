package com.stickers.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@Entity
@Table(name = "sticker_likes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "sticker_id", "sticker_type"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StickerLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "user_id", nullable = false)
    @JsonProperty("user_id")
    private Integer userId;
    
    @Column(name = "sticker_id", nullable = false)
    @JsonProperty("sticker_id")
    private Integer stickerId;
    
    @Column(name = "sticker_type", nullable = false)
    @JsonProperty("sticker_type")
    private String stickerType; // "template" or "user_created"
    
    @Column(name = "created_at")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

