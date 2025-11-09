package com.stickers.service;

import com.stickers.dto.StickerDetailDto;
import com.stickers.entity.Sticker;
import com.stickers.entity.UserCreatedSticker;
import com.stickers.entity.User;
import com.stickers.entity.Template;
import com.stickers.repository.StickerRepository;
import com.stickers.repository.UserCreatedStickerRepository;
import com.stickers.repository.UserRepository;
import com.stickers.repository.TemplateRepository;
import com.stickers.repository.StickerLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StickerDetailService {
    
    @Autowired
    private StickerRepository stickerRepository;
    
    @Autowired
    private UserCreatedStickerRepository userCreatedStickerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TemplateRepository templateRepository;
    
    @Autowired
    private StickerLikeRepository likeRepository;
    
    public StickerDetailDto getStickerDetail(Integer stickerId, String stickerType, Integer userId) {
        StickerDetailDto dto = new StickerDetailDto();
        
        if ("template".equals(stickerType)) {
            // Template sticker
            Optional<Sticker> stickerOpt = stickerRepository.findById(stickerId);
            if (stickerOpt.isEmpty()) {
                throw new IllegalArgumentException("Sticker not found");
            }
            
            Sticker sticker = stickerOpt.get();
            Template template = null;
            if (sticker.getTemplateId() != null) {
                template = templateRepository.findById(sticker.getTemplateId()).orElse(null);
            }
            
            dto.setId(sticker.getId());
            dto.setName(sticker.getName());
            dto.setImageUrl(sticker.getImageUrl());
            dto.setColors(sticker.getColors());
            dto.setFinishes(sticker.getFinishes());
            dto.setPrice(sticker.getPrice());
            dto.setTemplateTitle(template != null ? template.getTitle() : null);
            dto.setTemplateId(sticker.getTemplateId());
            dto.setStickerType("template");
            dto.setDesignedBy("Stickkery");
            dto.setCreatorId(null);
            dto.setCreatorUsername(null);
            
        } else {
            // User-created sticker
            Optional<UserCreatedSticker> stickerOpt = userCreatedStickerRepository.findById(stickerId);
            if (stickerOpt.isEmpty()) {
                throw new IllegalArgumentException("Sticker not found");
            }
            
            UserCreatedSticker sticker = stickerOpt.get();
            Optional<User> userOpt = userRepository.findById(sticker.getUserId());
            User creator = userOpt.orElse(null);
            
            dto.setId(sticker.getId());
            dto.setName(sticker.getName());
            dto.setImageUrl(sticker.getImageUrl());
            dto.setColors(null);
            dto.setFinishes(null);
            dto.setPrice(sticker.getPrice());
            dto.setTemplateTitle(sticker.getCategory());
            dto.setTemplateId(null);
            dto.setStickerType("user_created");
            dto.setDesignedBy(creator != null ? creator.getName() : "Unknown");
            dto.setCreatorId(sticker.getUserId());
            dto.setCreatorUsername(creator != null ? creator.getUsername() : null);
            dto.setSpecifications(sticker.getSpecifications());
            dto.setCategory(sticker.getCategory());
        }
        
        // Get like count and isLiked status
        long likeCount = likeRepository.countByStickerIdAndStickerType(stickerId, stickerType);
        boolean isLiked = userId != null && likeRepository.existsByUserIdAndStickerIdAndStickerType(userId, stickerId, stickerType);
        
        dto.setLikeCount(likeCount);
        dto.setIsLiked(isLiked);
        
        return dto;
    }
}

