package com.stickers.service;

import com.stickers.entity.StickerLike;
import com.stickers.entity.Notification;
import com.stickers.entity.UserCreatedSticker;
import com.stickers.entity.Sticker;
import com.stickers.entity.User;
import com.stickers.repository.StickerLikeRepository;
import com.stickers.repository.NotificationRepository;
import com.stickers.repository.UserCreatedStickerRepository;
import com.stickers.repository.StickerRepository;
import com.stickers.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LikeService {
    
    @Autowired
    private StickerLikeRepository likeRepository;
    
    @Autowired
    private UserCreatedStickerRepository userCreatedStickerRepository;
    
    @Autowired
    private StickerRepository stickerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Transactional
    public boolean toggleLike(Integer userId, Integer stickerId, String stickerType) {
        // Validate sticker type
        if (!stickerType.equals("template") && !stickerType.equals("user_created")) {
            throw new IllegalArgumentException("Invalid sticker type. Must be 'template' or 'user_created'");
        }
        
        // Check if sticker exists
        if (stickerType.equals("user_created")) {
            Optional<UserCreatedSticker> stickerOpt = userCreatedStickerRepository.findById(stickerId);
            if (stickerOpt.isEmpty()) {
                throw new IllegalArgumentException("Sticker not found");
            }
        } else {
            Optional<Sticker> stickerOpt = stickerRepository.findById(stickerId);
            if (stickerOpt.isEmpty()) {
                throw new IllegalArgumentException("Sticker not found");
            }
        }
        
        boolean isLiked = likeRepository.existsByUserIdAndStickerIdAndStickerType(userId, stickerId, stickerType);
        
        if (isLiked) {
            // Unlike
            Optional<StickerLike> likeOpt = likeRepository.findByUserIdAndStickerIdAndStickerType(userId, stickerId, stickerType);
            if (likeOpt.isPresent()) {
                likeRepository.delete(likeOpt.get());
                return false; // Not liked anymore
            }
        } else {
            // Like
            StickerLike like = new StickerLike();
            like.setUserId(userId);
            like.setStickerId(stickerId);
            like.setStickerType(stickerType);
            likeRepository.save(like);
            
            // Create notification only for user_created stickers (they have owners)
            if (stickerType.equals("user_created")) {
                Optional<UserCreatedSticker> stickerOpt = userCreatedStickerRepository.findById(stickerId);
                if (stickerOpt.isPresent()) {
                    UserCreatedSticker sticker = stickerOpt.get();
                    // Create notification if user is liking someone else's sticker
                    if (!sticker.getUserId().equals(userId)) {
                        Optional<User> fromUserOpt = userRepository.findById(userId);
                        Optional<User> toUserOpt = userRepository.findById(sticker.getUserId());
                        
                        if (fromUserOpt.isPresent() && toUserOpt.isPresent()) {
                            User fromUser = fromUserOpt.get();
                            User toUser = toUserOpt.get();
                            
                            Notification notification = new Notification();
                            notification.setUserId(toUser.getId());
                            notification.setFromUserId(fromUser.getId());
                            notification.setStickerId(stickerId);
                            notification.setType("like");
                            notification.setMessage(fromUser.getUsername() + " liked your sticker: " + sticker.getName());
                            notificationRepository.save(notification);
                        }
                    }
                }
            }
            
            return true; // Liked
        }
        
        return false;
    }
    
    public boolean isLiked(Integer userId, Integer stickerId, String stickerType) {
        return likeRepository.existsByUserIdAndStickerIdAndStickerType(userId, stickerId, stickerType);
    }
    
    public long getLikeCount(Integer stickerId, String stickerType) {
        return likeRepository.countByStickerIdAndStickerType(stickerId, stickerType);
    }
}

