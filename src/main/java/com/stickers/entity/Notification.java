package com.stickers.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "user_id", nullable = false)
    @JsonProperty("user_id")
    private Integer userId;
    
    @Column(name = "from_user_id", nullable = false)
    @JsonProperty("from_user_id")
    private Integer fromUserId;
    
    @Column(name = "sticker_id")
    @JsonProperty("sticker_id")
    private Integer stickerId;
    
    @Column(nullable = false)
    private String type; // "like", "comment", etc.
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "is_read", nullable = false)
    @JsonProperty("is_read")
    private Boolean isRead = false;
    
    @Column(name = "created_at")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isRead == null) {
            isRead = false;
        }
    }
}

