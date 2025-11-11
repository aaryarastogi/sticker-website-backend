package com.stickers.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "stickers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sticker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "template_id")
    private Integer templateId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl;
    
    @Column(name = "colors")
    @Convert(converter = StringListConverter.class)
    private List<String> colors;
    
    @Column(name = "finishes")
    @Convert(converter = StringListConverter.class)
    private List<String> finishes;
    
    @Column(columnDefinition = "DECIMAL(10, 2)")
    private BigDecimal price;
    
    @Column(name = "is_published")
    private Boolean isPublished = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
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

