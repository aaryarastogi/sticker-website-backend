package com.stickers.service;

import com.stickers.dto.CustomStickerRequest;
import com.stickers.dto.StickerWithLikesDto;
import com.stickers.entity.UserCreatedSticker;
import com.stickers.repository.UserCreatedStickerRepository;
import com.stickers.repository.StickerLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomStickerService {
    @Autowired
    private UserCreatedStickerRepository stickerRepository;
    
    @Autowired
    private StickerLikeRepository likeRepository;
    
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
    
    public List<StickerWithLikesDto> getPublishedStickersWithLikes(Integer userId) {
        List<UserCreatedSticker> stickers = stickerRepository.findByIsPublishedTrue();
        return stickers.stream()
            .map(sticker -> {
                Long likeCount = likeRepository.countByStickerIdAndStickerType(sticker.getId(), "user_created");
                Boolean isLiked = userId != null && likeRepository.existsByUserIdAndStickerIdAndStickerType(userId, sticker.getId(), "user_created");
                return StickerWithLikesDto.fromEntity(sticker, likeCount, isLiked);
            })
            .collect(Collectors.toList());
    }
    
    public List<UserCreatedSticker> getUserStickers(Integer userId) {
        return stickerRepository.findByUserId(userId);
    }
    
    public List<StickerWithLikesDto> getUserStickersWithLikes(Integer userId, Integer currentUserId) {
        List<UserCreatedSticker> stickers = stickerRepository.findByUserId(userId);
        return stickers.stream()
            .map(sticker -> {
                Long likeCount = likeRepository.countByStickerIdAndStickerType(sticker.getId(), "user_created");
                Boolean isLiked = currentUserId != null && likeRepository.existsByUserIdAndStickerIdAndStickerType(currentUserId, sticker.getId(), "user_created");
                return StickerWithLikesDto.fromEntity(sticker, likeCount, isLiked);
            })
            .collect(Collectors.toList());
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

