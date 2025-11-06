package com.stickers.service;

import com.stickers.dto.CustomStickerRequest;
import com.stickers.entity.UserCreatedSticker;
import com.stickers.repository.UserCreatedStickerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class CustomStickerService {
    @Autowired
    private UserCreatedStickerRepository stickerRepository;
    
    @Transactional
    public UserCreatedSticker createSticker(CustomStickerRequest request) {
        UserCreatedSticker sticker = new UserCreatedSticker();
        sticker.setUserId(request.getUser_id());
        sticker.setName(request.getName());
        sticker.setCategory(request.getCategory());
        sticker.setImageUrl(request.getImage_url());
        sticker.setSpecifications(request.getSpecifications());
        sticker.setPrice(request.getPrice() != null ? request.getPrice() : java.math.BigDecimal.ZERO);
        sticker.setIsPublished(request.getIs_published() != null ? request.getIs_published() : false);
        
        return stickerRepository.save(sticker);
    }
    
    public List<UserCreatedSticker> getPublishedStickers() {
        return stickerRepository.findByIsPublishedTrue();
    }
    
    public List<UserCreatedSticker> getUserStickers(Integer userId) {
        return stickerRepository.findByUserId(userId);
    }
    
    @Transactional
    public void deleteSticker(Integer id, Integer userId) {
        UserCreatedSticker sticker = stickerRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Sticker not found"));
        
        if (!sticker.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Not authorized to delete this sticker");
        }
        
        stickerRepository.delete(sticker);
    }
    
    @Transactional
    public UserCreatedSticker updatePublishStatus(Integer id, Integer userId, Boolean isPublished) {
        UserCreatedSticker sticker = stickerRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Sticker not found"));
        
        if (!sticker.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Not authorized to update this sticker");
        }
        
        sticker.setIsPublished(isPublished);
        return stickerRepository.save(sticker);
    }
}

