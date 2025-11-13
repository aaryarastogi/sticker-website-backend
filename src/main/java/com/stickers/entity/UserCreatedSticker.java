package com.stickers.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_created_stickers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedSticker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "user_id", nullable = false)
    @JsonProperty("user_id")
    private Integer userId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String category;
    
    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    @JsonProperty("image_url")
    private String imageUrl;
    
    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode specifications;
    
    @Column(columnDefinition = "DECIMAL(10, 2)")
    private BigDecimal price;
    
    @Column(name = "currency", length = 3)
    private String currency = "USD"; // Default to USD for backward compatibility
    
    @Column(name = "is_published")
    @JsonProperty("is_published")
    private Boolean isPublished = false;
    
    @Column(name = "status", nullable = false)
    private String status = "PENDING";
    
    @Column(name = "admin_note", columnDefinition = "TEXT")
    @JsonProperty("admin_note")
    private String adminNote;
    
    @Column(name = "created_at")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

