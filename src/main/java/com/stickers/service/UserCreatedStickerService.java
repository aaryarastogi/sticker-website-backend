package com.stickers.service;

import com.stickers.entity.UserCreatedSticker;
import com.stickers.repository.UserCreatedStickerRepository;
import com.stickers.repository.StickerLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserCreatedStickerService {
    
    @Autowired
    private UserCreatedStickerRepository stickerRepository;
    
    @Autowired
    private StickerLikeRepository likeRepository;
    
    public List<UserCreatedSticker> getTopLikedStickers(int limit) {
        // Get top stickers by like count
        List<Object[]> topStickers = likeRepository.findTopStickersByLikes(limit, "user_created");
        
        return topStickers.stream()
            .map(result -> {
                Object stickerIdObj = result[0];
                Integer stickerId;
                if (stickerIdObj instanceof Number) {
                    stickerId = ((Number) stickerIdObj).intValue();
                } else {
                    stickerId = Integer.valueOf(stickerIdObj.toString());
                }
                return stickerRepository.findById(stickerId).orElse(null);
            })
            .filter(sticker -> sticker != null && sticker.getIsPublished() != null && sticker.getIsPublished())
            .collect(Collectors.toList());
    }
    
    public long getTotalLikesForUser(Integer userId) {
        List<UserCreatedSticker> userStickers = stickerRepository.findByUserId(userId);
        return userStickers.stream()
            .mapToLong(sticker -> likeRepository.countByStickerIdAndStickerType(sticker.getId(), "user_created"))
            .sum();
    }
}

