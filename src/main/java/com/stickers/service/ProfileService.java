package com.stickers.service;

import com.stickers.dto.ProfileDto;
import com.stickers.dto.ProfileUpdateRequest;
import com.stickers.entity.User;
import com.stickers.repository.UserRepository;
import com.stickers.repository.UserCreatedStickerRepository;
import com.stickers.repository.StickerLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProfileService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserCreatedStickerRepository stickerRepository;
    
    @Autowired
    private StickerLikeRepository likeRepository;
    
    public ProfileDto getProfile(Integer userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Long stickerCount = stickerRepository.countByUserId(userId);
        // Calculate total likes on user's created stickers
        Long likesCount = calculateTotalLikes(userId);
        
        return new ProfileDto(
            user.getId(),
            user.getName(),
            user.getUsername(),
            user.getEmail(),
            user.getProfileImageUrl(),
            stickerCount,
            likesCount
        );
    }
    
    public ProfileDto getPublicProfile(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Long stickerCount = stickerRepository.countByUserId(user.getId());
        // Calculate total likes on user's created stickers
        Long likesCount = calculateTotalLikes(user.getId());
        
        return new ProfileDto(
            user.getId(),
            user.getName(),
            user.getUsername(),
            user.getEmail(),
            user.getProfileImageUrl(),
            stickerCount,
            likesCount
        );
    }
    
    @Transactional
    public ProfileDto updateProfile(Integer userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }
        
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            // Check if username is already taken by another user
            Optional<User> existingUser = userRepository.findByUsername(request.getUsername());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new IllegalArgumentException("Username already taken");
            }
            user.setUsername(request.getUsername());
        }
        
        user = userRepository.save(user);
        
        Long stickerCount = stickerRepository.countByUserId(userId);
        // Calculate total likes on user's created stickers
        Long likesCount = calculateTotalLikes(userId);
        
        return new ProfileDto(
            user.getId(),
            user.getName(),
            user.getUsername(),
            user.getEmail(),
            user.getProfileImageUrl(),
            stickerCount,
            likesCount
        );
    }
    
    private Long calculateTotalLikes(Integer userId) {
        List<com.stickers.entity.UserCreatedSticker> userStickers = stickerRepository.findByUserId(userId);
        return userStickers.stream()
            .mapToLong(sticker -> likeRepository.countByStickerIdAndStickerType(sticker.getId(), "user_created"))
            .sum();
    }
}


