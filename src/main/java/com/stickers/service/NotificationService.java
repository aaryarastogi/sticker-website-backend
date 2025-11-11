package com.stickers.service;

import com.stickers.entity.Notification;
import com.stickers.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    public List<Notification> getUserNotifications(Integer userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public List<Notification> getUnreadNotifications(Integer userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }
    
    public long getUnreadCount(Integer userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
    
    @Transactional
    public Notification createNotification(Integer userId, Integer fromUserId, Integer stickerId, String type, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        // Use 0 for admin/system actions (fromUserId is null)
        notification.setFromUserId(fromUserId != null ? fromUserId : 0);
        notification.setStickerId(stickerId);
        notification.setType(type);
        notification.setMessage(message != null ? message : "");
        notification.setIsRead(false);
        return notificationRepository.save(notification);
    }
    
    @Transactional
    public void markAsRead(Integer notificationId, Integer userId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        
        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized");
        }
        
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
    
    @Transactional
    public void markAllAsRead(Integer userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        for (Notification notification : notifications) {
            notification.setIsRead(true);
        }
        notificationRepository.saveAll(notifications);
    }

    public List<Notification> getNotificationsByStickerId(Integer stickerId) {
        return notificationRepository.findByStickerId(stickerId);
    }

    @Transactional
    public void deleteNotifications(List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return;
        }
        notificationRepository.deleteAll(notifications);
    }
}

