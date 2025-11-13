package com.stickers.controller;

import com.stickers.dto.UserListDto;
import com.stickers.dto.UserDetailDto;
import com.stickers.dto.UpdateUserStatusRequest;
import com.stickers.dto.AdminStickerDto;
import com.stickers.dto.CreateAdminStickerRequest;
import com.stickers.entity.User;
import com.stickers.entity.Sticker;
import com.stickers.entity.UserCreatedSticker;
import com.stickers.repository.UserRepository;
import com.stickers.repository.UserCreatedStickerRepository;
import com.stickers.repository.StickerRepository;
import com.stickers.repository.TemplateRepository;
import com.stickers.repository.CategoryRepository;
import com.stickers.repository.StickerLikeRepository;
import com.stickers.repository.OrderRepository;
import com.stickers.dto.ReviewStickerRequest;
import com.stickers.dto.AdminOrderDto;
import com.stickers.entity.Order;
import com.stickers.service.CustomStickerService;
import com.stickers.service.NotificationService;
import com.stickers.util.JwtUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stickers.entity.Template;
import com.stickers.entity.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"}, 
             allowCredentials = "true")
public class AdminController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserCreatedStickerRepository userCreatedStickerRepository;
    
    @Autowired
    private StickerRepository stickerRepository;
    
    @Autowired
    private TemplateRepository templateRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private StickerLikeRepository stickerLikeRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CustomStickerService customStickerService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private com.stickers.service.EmailService emailService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @GetMapping("/users")
    public ResponseEntity<List<UserListDto>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            List<UserListDto> userDtos = users.stream()
                .map(user -> {
                    UserListDto dto = new UserListDto();
                    dto.setId(user.getId());
                    dto.setName(user.getName());
                    dto.setUsername(user.getUsername());
                    dto.setEmail(user.getEmail());
                    dto.setProfileImageUrl(user.getProfileImageUrl());
                    dto.setCreatedAt(user.getCreatedAt());
                    dto.setIsActive(user.getIsActive() != null ? user.getIsActive() : true);
                    // Get sticker count for this user
                    long stickerCount = userCreatedStickerRepository.countByUserId(user.getId());
                    dto.setStickerCount(stickerCount);
                    return dto;
                })
                .collect(Collectors.toList());
            return ResponseEntity.ok(userDtos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDetailDto> getUserById(@PathVariable Integer userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                UserDetailDto dto = new UserDetailDto();
                dto.setId(user.getId());
                dto.setName(user.getName());
                dto.setUsername(user.getUsername());
                dto.setEmail(user.getEmail());
                dto.setProfileImageUrl(user.getProfileImageUrl());
                dto.setCreatedAt(user.getCreatedAt());
                dto.setUpdatedAt(user.getUpdatedAt());
                dto.setIsActive(user.getIsActive() != null ? user.getIsActive() : true);
                // Get sticker count for this user
                long stickerCount = userCreatedStickerRepository.countByUserId(user.getId());
                dto.setStickerCount(stickerCount);
                // Get user's stickers
                List<com.stickers.entity.UserCreatedSticker> stickers = userCreatedStickerRepository.findByUserId(user.getId());
                dto.setStickers(stickers.stream()
                    .map(sticker -> {
                        com.stickers.dto.UserStickerDto stickerDto = new com.stickers.dto.UserStickerDto();
                        stickerDto.setId(sticker.getId());
                        stickerDto.setImageUrl(sticker.getImageUrl());
                        stickerDto.setCategory(sticker.getCategory());
                        stickerDto.setIsPublished(sticker.getIsPublished());
                        stickerDto.setStatus(sticker.getStatus());
                        stickerDto.setAdminNote(sticker.getAdminNote());
                        stickerDto.setCreatedAt(sticker.getCreatedAt());
                        return stickerDto;
                    })
                    .collect(Collectors.toList()));
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<UserListDto> updateUserStatus(@PathVariable Integer userId, @RequestBody UpdateUserStatusRequest request) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                boolean previousStatus = user.getIsActive() != null ? user.getIsActive() : true;
                boolean newStatus = request.getIsActive();
                
                user.setIsActive(newStatus);
                user = userRepository.save(user);
                
                // Send email notification if status changed
                String userEmail = user.getEmail();
                String userName = user.getName() != null ? user.getName() : user.getUsername();
                
                if (userEmail != null && !userEmail.isEmpty() && previousStatus != newStatus) {
                    try {
                        if (!newStatus) {
                            // Account was deactivated
                            emailService.sendAccountDeactivatedEmail(userEmail, userName);
                            
                            // Also create a notification
                            notificationService.createNotification(
                                user.getId(),
                                0, // Admin/system action
                                null,
                                "account_deactivated",
                                "Your account has been deactivated by an administrator. Please contact support if you believe this was an error."
                            );
                        } else {
                            // Account was activated/reactivated
                            emailService.sendAccountActivatedEmail(userEmail, userName);
                            
                            // Also create a notification
                            notificationService.createNotification(
                                user.getId(),
                                0, // Admin/system action
                                null,
                                "account_activated",
                                "Your account has been reactivated. You can now log in and access all features."
                            );
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to send account status change email: " + e.getMessage());
                        e.printStackTrace();
                        // Don't fail the request if email fails
                    }
                }
                
                UserListDto dto = new UserListDto();
                dto.setId(user.getId());
                dto.setName(user.getName());
                dto.setUsername(user.getUsername());
                dto.setEmail(user.getEmail());
                dto.setProfileImageUrl(user.getProfileImageUrl());
                dto.setCreatedAt(user.getCreatedAt());
                dto.setIsActive(user.getIsActive());
                long stickerCount = userCreatedStickerRepository.countByUserId(user.getId());
                dto.setStickerCount(stickerCount);
                
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    @PostMapping("/users/{userId}/warn")
    public ResponseEntity<?> warnUser(@PathVariable Integer userId,
                                     @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Integer adminId = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    adminId = jwtUtil.getUserIdFromToken(token);
                } catch (Exception ignored) {
                    // Ignore token parsing errors
                }
            }
            
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "User not found", "message", "User with ID " + userId + " does not exist."));
            }
            
            User user = userOpt.get();
            String userEmail = user.getEmail();
            String userName = user.getName() != null ? user.getName() : user.getUsername();
            
            // Create notification for the user
            String notificationMessage = "You have been warned by an administrator. " +
                "If you continue with the same activity or violate our guidelines again, your account will be permanently disabled.";
            
            try {
                notificationService.createNotification(
                    user.getId(),
                    adminId != null ? adminId : 0,
                    null, // No sticker ID for user warnings
                    "user_warning",
                    notificationMessage
                );
            } catch (Exception e) {
                System.err.println("Failed to create notification: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Send email notification
            if (userEmail != null && !userEmail.isEmpty()) {
                try {
                    emailService.sendUserWarningEmail(userEmail, userName);
                } catch (Exception e) {
                    System.err.println("Failed to send warning email: " + e.getMessage());
                    e.printStackTrace();
                    // Don't fail the request if email fails
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "User warned successfully",
                "userId", userId,
                "userName", userName,
                "emailSent", userEmail != null && !userEmail.isEmpty()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to warn user", "message", e.getMessage()));
        }
    }
    
    @GetMapping("/stickers")
    public ResponseEntity<List<AdminStickerDto>> getAllStickers() {
        try {
            List<AdminStickerDto> allStickers = new java.util.ArrayList<>();
            
            // Fetch all template stickers (created by Stickkery)
            List<Sticker> templateStickers = stickerRepository.findAll();
            // Create a map of template categories for quick lookup
            Map<Integer, String> categoryMap = new java.util.HashMap<>();
            List<Template> templates = templateRepository.findAll();
            for (Template template : templates) {
                if (template.getCategoryId() != null) {
                    Optional<Category> categoryOpt = categoryRepository.findById(template.getCategoryId());
                    if (categoryOpt.isPresent()) {
                        categoryMap.put(template.getId(), categoryOpt.get().getName());
                    }
                }
            }
            
            for (Sticker sticker : templateStickers) {
                AdminStickerDto dto = new AdminStickerDto();
                dto.setId(sticker.getId());
                dto.setName(sticker.getName());
                dto.setImageUrl(sticker.getImageUrl());
                
                // Get category from template
                String categoryName = "Uncategorized";
                if (sticker.getTemplateId() != null) {
                    categoryName = categoryMap.getOrDefault(sticker.getTemplateId(), "Uncategorized");
                }
                dto.setCategory(categoryName);
                
                dto.setPrice(sticker.getPrice());
                dto.setCurrency(sticker.getCurrency() != null ? sticker.getCurrency() : "USD");
                dto.setCreatorType("stickkery");
                dto.setCreatorName("Stickkery");
                dto.setCreatorId(null);
                dto.setStickerType("template");
                dto.setIsPublished(sticker.getIsPublished() != null ? sticker.getIsPublished() : true);
                dto.setStatus(sticker.getIsPublished() != null && sticker.getIsPublished() ? "APPROVED" : "UNPUBLISHED");
                dto.setAdminNote(null);
                dto.setCreatedAt(sticker.getCreatedAt());
                dto.setUpdatedAt(sticker.getUpdatedAt());
                allStickers.add(dto);
            }
            
            // Fetch all user-created stickers
            List<UserCreatedSticker> userStickers = userCreatedStickerRepository.findAll();
            for (UserCreatedSticker sticker : userStickers) {
                AdminStickerDto dto = new AdminStickerDto();
                dto.setId(sticker.getId());
                dto.setName(sticker.getName());
                dto.setImageUrl(sticker.getImageUrl());
                dto.setCategory(sticker.getCategory());
                dto.setPrice(sticker.getPrice());
                // Get currency - if null, try to get from order data
                String storedCurrency = sticker.getCurrency();
                if (storedCurrency == null || storedCurrency.trim().isEmpty()) {
                    // Try to get currency from the order if available
                    // Find orders for this user with CUSTOM_STICKER type around the sticker creation time
                    try {
                        List<Order> userOrders = orderRepository.findByUserIdOrderByCreatedAtDesc(sticker.getUserId());
                        for (Order order : userOrders) {
                            if ("CUSTOM_STICKER".equals(order.getOrderType()) && 
                                order.getCreatedAt() != null && 
                                sticker.getCreatedAt() != null &&
                                Math.abs(java.time.Duration.between(order.getCreatedAt(), sticker.getCreatedAt()).toMinutes()) < 5) {
                                // Found a matching order within 5 minutes of sticker creation
                                storedCurrency = order.getCurrency();
                                break;
                            }
                        }
                    } catch (Exception e) {
                        // If order lookup fails, continue with default
                    }
                    // If still null, default to USD
                    if (storedCurrency == null || storedCurrency.trim().isEmpty()) {
                        storedCurrency = "USD";
                    }
                }
                dto.setCurrency(storedCurrency);
                dto.setCreatorType("user");
                
                // Get user information
                Optional<User> userOpt = userRepository.findById(sticker.getUserId());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    dto.setCreatorName(user.getName() != null ? user.getName() : user.getUsername());
                } else {
                    dto.setCreatorName("Unknown User");
                }
                dto.setCreatorId(sticker.getUserId());
                dto.setStickerType("user_created");
                dto.setIsPublished(sticker.getIsPublished());
                dto.setStatus(sticker.getStatus());
                dto.setAdminNote(sticker.getAdminNote());
                dto.setCreatedAt(sticker.getCreatedAt());
                dto.setUpdatedAt(sticker.getUpdatedAt());
                allStickers.add(dto);
            }
            
            // Sort by created date (newest first)
            allStickers.sort((a, b) -> {
                if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            });
            
            return ResponseEntity.ok(allStickers);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    @PostMapping("/stickers")
    public ResponseEntity<?> createSticker(@RequestBody CreateAdminStickerRequest request) {
        try {
            // Validate required fields
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.status(400)
                    .body(Map.of("error", "Name is required", "message", "Sticker name cannot be empty"));
            }
            if (request.getImageUrl() == null || request.getImageUrl().trim().isEmpty()) {
                return ResponseEntity.status(400)
                    .body(Map.of("error", "Image URL is required", "message", "Image URL cannot be empty"));
            }
            
            Integer templateId = request.getTemplateId();
            String categoryName = request.getCategory();
            
            // If templateId is not provided, category must be provided
            if (templateId == null) {
                if (categoryName == null || categoryName.trim().isEmpty()) {
                    return ResponseEntity.status(400)
                        .body(Map.of("error", "Category or Template ID required", 
                                     "message", "Either templateId or category must be provided to create a sticker."));
                }
            }
            
            // If templateId is not provided but category is provided, find or create template for that category
            if (templateId == null && categoryName != null && !categoryName.trim().isEmpty()) {
                // Find category by name (case-insensitive)
                Optional<Category> categoryOpt = categoryRepository.findByNameIgnoreCase(categoryName.trim());
                
                if (categoryOpt.isPresent()) {
                    Category category = categoryOpt.get();
                    Integer categoryId = category.getId();
                    
                    // Find existing template for this category
                    List<Template> existingTemplates = templateRepository.findByCategoryId(categoryId);
                    
                    if (!existingTemplates.isEmpty()) {
                        // Use the first template found for this category
                        templateId = existingTemplates.get(0).getId();
                    } else {
                        // Create a new template for this category
                        Template newTemplate = new Template();
                        newTemplate.setTitle(categoryName.trim());
                        newTemplate.setImageUrl(request.getImageUrl().trim()); // Use sticker image as template image
                        newTemplate.setCategoryId(categoryId);
                        newTemplate.setIsTrending(false);
                        newTemplate = templateRepository.save(newTemplate);
                        templateId = newTemplate.getId();
                    }
                } else {
                    // Category doesn't exist, return error
                    return ResponseEntity.status(400)
                        .body(Map.of("error", "Category not found", 
                                     "message", "Category '" + categoryName + "' does not exist. Please create the category first."));
                }
            }
            
            // Create new Sticker entity
            Sticker sticker = new Sticker();
            sticker.setName(request.getName().trim());
            sticker.setImageUrl(request.getImageUrl().trim());
            sticker.setPrice(request.getPrice() != null ? request.getPrice() : java.math.BigDecimal.ZERO);
            sticker.setCurrency(request.getCurrency() != null && !request.getCurrency().trim().isEmpty() 
                ? request.getCurrency().trim().toUpperCase() 
                : "USD");
            sticker.setTemplateId(templateId);
            sticker.setIsPublished(true); // Admin-created stickers are published by default
            
            // Save sticker
            sticker = stickerRepository.save(sticker);
            
            // Get category name for response
            String finalCategoryName = "Uncategorized";
            if (templateId != null) {
                Optional<Template> templateOpt = templateRepository.findById(templateId);
                if (templateOpt.isPresent()) {
                    Template template = templateOpt.get();
                    if (template.getCategoryId() != null) {
                        Optional<Category> categoryOpt = categoryRepository.findById(template.getCategoryId());
                        if (categoryOpt.isPresent()) {
                            finalCategoryName = categoryOpt.get().getName();
                        }
                    }
                }
            } else if (categoryName != null && !categoryName.trim().isEmpty()) {
                finalCategoryName = categoryName.trim();
            }
            
            // Create response DTO
            AdminStickerDto dto = new AdminStickerDto();
            dto.setId(sticker.getId());
            dto.setName(sticker.getName());
            dto.setImageUrl(sticker.getImageUrl());
            dto.setCategory(finalCategoryName);
            dto.setPrice(sticker.getPrice());
            dto.setCurrency(sticker.getCurrency() != null ? sticker.getCurrency() : "USD");
            dto.setCreatorType("stickkery");
            dto.setCreatorName("Stickkery");
            dto.setCreatorId(null);
            dto.setStickerType("template");
            dto.setIsPublished(sticker.getIsPublished() != null ? sticker.getIsPublished() : true);
            dto.setStatus(sticker.getIsPublished() != null && sticker.getIsPublished() ? "APPROVED" : "UNPUBLISHED");
            dto.setAdminNote(null);
            dto.setCreatedAt(sticker.getCreatedAt());
            dto.setUpdatedAt(sticker.getUpdatedAt());
            
            return ResponseEntity.status(201).body(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to create sticker", "message", e.getMessage()));
        }
    }
    
    @PostMapping("/stickers/{stickerId}/review")
    public ResponseEntity<?> reviewSticker(@PathVariable Integer stickerId,
                                           @RequestBody ReviewStickerRequest request,
                                           @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Integer adminId = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    adminId = jwtUtil.getUserIdFromToken(token);
                } catch (Exception ignored) {
                    // Ignore token parsing errors; notification will fall back to 0 as fromUserId
                }
            }
            
            UserCreatedSticker sticker = customStickerService.reviewSticker(stickerId, request.getStatus(), request.getNote());
            
            // Notify the sticker owner
            if (sticker.getUserId() != null) {
                String categoryName = (sticker.getCategory() != null && !sticker.getCategory().isBlank())
                    ? sticker.getCategory()
                    : "the catalog";
                String normalizedStatus = sticker.getStatus();
                String message;
                if ("APPROVED".equalsIgnoreCase(normalizedStatus)) {
                    message = String.format("Your custom sticker \"%s\" has been approved and published in %s.",
                        sticker.getName(), categoryName);
                } else {
                    String note = request.getNote();
                    if (note != null && !note.trim().isEmpty()) {
                        message = String.format("Your custom sticker \"%s\" was rejected: %s", sticker.getName(), note.trim());
                    } else {
                        message = String.format("Your custom sticker \"%s\" was rejected.", sticker.getName());
                    }
                }
                notificationService.createNotification(
                    sticker.getUserId(),
                    adminId != null ? adminId : 0,
                    sticker.getId(),
                    "APPROVED".equalsIgnoreCase(sticker.getStatus()) ? "sticker_approved" : "sticker_rejected",
                    message
                );
                
                // Send email notification if sticker is rejected
                if ("REJECTED".equalsIgnoreCase(normalizedStatus)) {
                    Optional<User> userOpt = userRepository.findById(sticker.getUserId());
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        String userEmail = user.getEmail();
                        String userName = user.getName() != null ? user.getName() : user.getUsername();
                        
                        if (userEmail != null && !userEmail.isEmpty()) {
                            try {
                                emailService.sendStickerRejectedEmail(
                                    userEmail,
                                    userName,
                                    sticker.getName(),
                                    request.getNote()
                                );
                            } catch (Exception e) {
                                System.err.println("Failed to send rejection email: " + e.getMessage());
                                e.printStackTrace();
                                // Don't fail the request if email fails
                            }
                        }
                    }
                }
            }
            
            // Build response DTO
            AdminStickerDto dto = new AdminStickerDto();
            dto.setId(sticker.getId());
            dto.setName(sticker.getName());
            dto.setImageUrl(sticker.getImageUrl());
            dto.setCategory(sticker.getCategory());
            dto.setPrice(sticker.getPrice());
            dto.setCurrency(sticker.getCurrency() != null ? sticker.getCurrency() : "USD");
            dto.setCreatorType("user");
            Optional<User> stickerOwner = userRepository.findById(sticker.getUserId());
            dto.setCreatorName(stickerOwner.map(user -> user.getName() != null ? user.getName() : user.getUsername()).orElse("Unknown User"));
            dto.setCreatorId(sticker.getUserId());
            dto.setStickerType("user_created");
            dto.setIsPublished(sticker.getIsPublished());
            dto.setStatus(sticker.getStatus());
            dto.setAdminNote(sticker.getAdminNote());
            dto.setCreatedAt(sticker.getCreatedAt());
            dto.setUpdatedAt(sticker.getUpdatedAt());
            
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to review sticker", "message", e.getMessage()));
        }
    }
    
    @PutMapping("/stickers/{stickerId}")
    public ResponseEntity<?> updateSticker(@PathVariable Integer stickerId, 
                                           @RequestBody CreateAdminStickerRequest request) {
        try {
            // Validate required fields
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.status(400)
                    .body(Map.of("error", "Name is required", "message", "Sticker name cannot be empty"));
            }
            if (request.getImageUrl() == null || request.getImageUrl().trim().isEmpty()) {
                return ResponseEntity.status(400)
                    .body(Map.of("error", "Image URL is required", "message", "Image URL cannot be empty"));
            }
            
            // Try to find as template sticker first (only allow editing Stickkery-created stickers)
            Optional<Sticker> templateStickerOpt = stickerRepository.findById(stickerId);
            
            if (templateStickerOpt.isPresent()) {
                // It's a template sticker - update it
                Sticker sticker = templateStickerOpt.get();
                
                Integer templateId = request.getTemplateId();
                String categoryName = request.getCategory();
                
                // If category is provided and different from current, update template
                if (categoryName != null && !categoryName.trim().isEmpty()) {
                    Optional<Category> categoryOpt = categoryRepository.findByNameIgnoreCase(categoryName.trim());
                    
                    if (categoryOpt.isPresent()) {
                        Category category = categoryOpt.get();
                        Integer categoryId = category.getId();
                        
                        // Check if sticker's current template needs to be updated or if we need a new template
                        Optional<Template> currentTemplateOpt = templateRepository.findById(sticker.getTemplateId());
                        boolean needsTemplateUpdate = false;
                        
                        if (currentTemplateOpt.isPresent()) {
                            Template currentTemplate = currentTemplateOpt.get();
                            // If category changed, update template or find/create new one
                            if (currentTemplate.getCategoryId() == null || !currentTemplate.getCategoryId().equals(categoryId)) {
                                needsTemplateUpdate = true;
                            }
                        } else {
                            needsTemplateUpdate = true;
                        }
                        
                        if (needsTemplateUpdate) {
                            // Find existing template for this category
                            List<Template> existingTemplates = templateRepository.findByCategoryId(categoryId);
                            
                            if (!existingTemplates.isEmpty()) {
                                // Use the first template found for this category
                                templateId = existingTemplates.get(0).getId();
                            } else {
                                // Create a new template for this category
                                Template newTemplate = new Template();
                                newTemplate.setTitle(categoryName.trim());
                                newTemplate.setImageUrl(request.getImageUrl().trim());
                                newTemplate.setCategoryId(categoryId);
                                newTemplate.setIsTrending(false);
                                newTemplate = templateRepository.save(newTemplate);
                                templateId = newTemplate.getId();
                            }
                        } else {
                            // Keep current template
                            templateId = sticker.getTemplateId();
                        }
                    } else {
                        return ResponseEntity.status(400)
                            .body(Map.of("error", "Category not found", 
                                         "message", "Category '" + categoryName + "' does not exist. Please create the category first."));
                    }
                } else if (templateId == null) {
                    // If no category and no templateId provided, keep current template
                    templateId = sticker.getTemplateId();
                }
                
                // Update sticker fields
                sticker.setName(request.getName().trim());
                sticker.setImageUrl(request.getImageUrl().trim());
                sticker.setPrice(request.getPrice() != null ? request.getPrice() : java.math.BigDecimal.ZERO);
                if (request.getCurrency() != null && !request.getCurrency().trim().isEmpty()) {
                    sticker.setCurrency(request.getCurrency().trim().toUpperCase());
                }
                if (templateId != null) {
                    sticker.setTemplateId(templateId);
                }
                
                // Save updated sticker
                sticker = stickerRepository.save(sticker);
                
                // Get category name for response
                String finalCategoryName = "Uncategorized";
                if (sticker.getTemplateId() != null) {
                    Optional<Template> templateOpt = templateRepository.findById(sticker.getTemplateId());
                    if (templateOpt.isPresent()) {
                        Template template = templateOpt.get();
                        if (template.getCategoryId() != null) {
                            Optional<Category> categoryOpt = categoryRepository.findById(template.getCategoryId());
                            if (categoryOpt.isPresent()) {
                                finalCategoryName = categoryOpt.get().getName();
                            }
                        }
                    }
                } else if (categoryName != null && !categoryName.trim().isEmpty()) {
                    finalCategoryName = categoryName.trim();
                }
                
                // Create response DTO
                AdminStickerDto dto = new AdminStickerDto();
                dto.setId(sticker.getId());
                dto.setName(sticker.getName());
                dto.setImageUrl(sticker.getImageUrl());
                dto.setCategory(finalCategoryName);
                dto.setPrice(sticker.getPrice());
                dto.setCurrency(sticker.getCurrency() != null ? sticker.getCurrency() : "USD");
                dto.setCreatorType("stickkery");
                dto.setCreatorName("Stickkery");
                dto.setCreatorId(null);
                dto.setStickerType("template");
                dto.setIsPublished(null);
                dto.setStatus("APPROVED");
                dto.setAdminNote(null);
                dto.setCreatedAt(sticker.getCreatedAt());
                dto.setUpdatedAt(sticker.getUpdatedAt());
                
                return ResponseEntity.ok(dto);
            } else {
                // Only allow editing Stickkery-created stickers (template stickers)
                // User-created stickers should not be editable through admin panel
                return ResponseEntity.status(403)
                    .body(Map.of("error", "Cannot edit user-created stickers", 
                                 "message", "Only stickers created by Stickkery can be edited through the admin panel."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to update sticker", "message", e.getMessage()));
        }
    }
    
    @PatchMapping("/stickers/{stickerId}/unpublish")
    public ResponseEntity<?> unpublishSticker(@PathVariable Integer stickerId) {
        try {
            // Try to find as template sticker first
            Optional<Sticker> templateStickerOpt = stickerRepository.findById(stickerId);
            
            if (templateStickerOpt.isPresent()) {
                // It's a template sticker (Stickkery-created)
                Sticker sticker = templateStickerOpt.get();
                sticker.setIsPublished(false);
                sticker = stickerRepository.save(sticker);
                
                // Get category name for response
                String categoryName = "Uncategorized";
                if (sticker.getTemplateId() != null) {
                    Optional<Template> templateOpt = templateRepository.findById(sticker.getTemplateId());
                    if (templateOpt.isPresent()) {
                        Template template = templateOpt.get();
                        if (template.getCategoryId() != null) {
                            Optional<Category> categoryOpt = categoryRepository.findById(template.getCategoryId());
                            if (categoryOpt.isPresent()) {
                                categoryName = categoryOpt.get().getName();
                            }
                        }
                    }
                }
                
                AdminStickerDto dto = new AdminStickerDto();
                dto.setId(sticker.getId());
                dto.setName(sticker.getName());
                dto.setImageUrl(sticker.getImageUrl());
                dto.setCategory(categoryName);
                dto.setPrice(sticker.getPrice());
                dto.setCurrency(sticker.getCurrency() != null ? sticker.getCurrency() : "USD");
                dto.setCreatorType("stickkery");
                dto.setCreatorName("Stickkery");
                dto.setCreatorId(null);
                dto.setStickerType("template");
                dto.setIsPublished(false);
                dto.setStatus("UNPUBLISHED");
                dto.setAdminNote(null);
                dto.setCreatedAt(sticker.getCreatedAt());
                dto.setUpdatedAt(sticker.getUpdatedAt());
                
                return ResponseEntity.ok(dto);
            } else {
                // Try to find as user-created sticker
                Optional<UserCreatedSticker> userStickerOpt = userCreatedStickerRepository.findById(stickerId);
                if (userStickerOpt.isPresent()) {
                    UserCreatedSticker sticker = userStickerOpt.get();
                    sticker.setIsPublished(false);
                    sticker = userCreatedStickerRepository.save(sticker);
                    
                    // Get user information
                    Optional<User> userOpt = userRepository.findById(sticker.getUserId());
                    String creatorName = "Unknown User";
                    String userEmail = null;
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        creatorName = user.getName() != null ? user.getName() : user.getUsername();
                        userEmail = user.getEmail();
                    }
                    
                    // Create notification for the user
                    try {
                        String notificationMessage = String.format(
                            "Your sticker '%s' has been unpublished by an administrator. " +
                            "It remains in your profile and in our database, and may be published again in the future.",
                            sticker.getName()
                        );
                        notificationService.createNotification(
                            sticker.getUserId(),
                            null, // fromUserId is null for admin actions
                            sticker.getId(),
                            "sticker_unpublished",
                            notificationMessage
                        );
                    } catch (Exception e) {
                        System.err.println("Failed to create notification: " + e.getMessage());
                        e.printStackTrace();
                    }
                    
                    // Send email notification to the user
                    if (userEmail != null && !userEmail.isEmpty()) {
                        try {
                            emailService.sendStickerUnpublishedEmail(
                                userEmail,
                                creatorName,
                                sticker.getName()
                            );
                        } catch (Exception e) {
                            System.err.println("Failed to send email notification: " + e.getMessage());
                            e.printStackTrace();
                            // Don't fail the request if email fails
                        }
                    }
                    
                    AdminStickerDto dto = new AdminStickerDto();
                    dto.setId(sticker.getId());
                    dto.setName(sticker.getName());
                    dto.setImageUrl(sticker.getImageUrl());
                    dto.setCategory(sticker.getCategory());
                    dto.setPrice(sticker.getPrice());
                    dto.setCurrency(sticker.getCurrency() != null ? sticker.getCurrency() : "USD");
                    dto.setCreatorType("user");
                    dto.setCreatorName(creatorName);
                    dto.setCreatorId(sticker.getUserId());
                    dto.setStickerType("user_created");
                    dto.setIsPublished(false);
                    dto.setStatus(sticker.getStatus());
                    dto.setAdminNote(sticker.getAdminNote());
                    dto.setCreatedAt(sticker.getCreatedAt());
                    dto.setUpdatedAt(sticker.getUpdatedAt());
                    
                    return ResponseEntity.ok(dto);
                } else {
                    // Sticker not found
                    return ResponseEntity.status(404)
                        .body(Map.of("error", "Sticker not found", 
                                     "message", "Sticker with ID " + stickerId + " does not exist."));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to unpublish sticker", 
                             "message", e.getMessage()));
        }
    }
    
    @PatchMapping("/stickers/{stickerId}/publish")
    public ResponseEntity<?> publishSticker(@PathVariable Integer stickerId) {
        try {
            // Try to find as template sticker first
            Optional<Sticker> templateStickerOpt = stickerRepository.findById(stickerId);
            
            if (templateStickerOpt.isPresent()) {
                // It's a template sticker (Stickkery-created)
                Sticker sticker = templateStickerOpt.get();
                sticker.setIsPublished(true);
                sticker = stickerRepository.save(sticker);
                
                // Get category name for response
                String categoryName = "Uncategorized";
                if (sticker.getTemplateId() != null) {
                    Optional<Template> templateOpt = templateRepository.findById(sticker.getTemplateId());
                    if (templateOpt.isPresent()) {
                        Template template = templateOpt.get();
                        if (template.getCategoryId() != null) {
                            Optional<Category> categoryOpt = categoryRepository.findById(template.getCategoryId());
                            if (categoryOpt.isPresent()) {
                                categoryName = categoryOpt.get().getName();
                            }
                        }
                    }
                }
                
                AdminStickerDto dto = new AdminStickerDto();
                dto.setId(sticker.getId());
                dto.setName(sticker.getName());
                dto.setImageUrl(sticker.getImageUrl());
                dto.setCategory(categoryName);
                dto.setPrice(sticker.getPrice());
                dto.setCurrency(sticker.getCurrency() != null ? sticker.getCurrency() : "USD");
                dto.setCreatorType("stickkery");
                dto.setCreatorName("Stickkery");
                dto.setCreatorId(null);
                dto.setStickerType("template");
                dto.setIsPublished(true);
                dto.setStatus("APPROVED");
                dto.setAdminNote(null);
                dto.setCreatedAt(sticker.getCreatedAt());
                dto.setUpdatedAt(sticker.getUpdatedAt());
                
                return ResponseEntity.ok(dto);
            } else {
                // Try to find as user-created sticker
                Optional<UserCreatedSticker> userStickerOpt = userCreatedStickerRepository.findById(stickerId);
                if (userStickerOpt.isPresent()) {
                    UserCreatedSticker sticker = userStickerOpt.get();
                    // Only publish if status is APPROVED
                    if (!"APPROVED".equalsIgnoreCase(sticker.getStatus())) {
                        return ResponseEntity.status(400)
                            .body(Map.of("error", "Cannot publish sticker", 
                                         "message", "User-created stickers can only be published if they are approved. Current status: " + sticker.getStatus()));
                    }
                    sticker.setIsPublished(true);
                    sticker = userCreatedStickerRepository.save(sticker);
                    
                    // Get user information
                    Optional<User> userOpt = userRepository.findById(sticker.getUserId());
                    String creatorName = "Unknown User";
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        creatorName = user.getName() != null ? user.getName() : user.getUsername();
                    }
                    
                    AdminStickerDto dto = new AdminStickerDto();
                    dto.setId(sticker.getId());
                    dto.setName(sticker.getName());
                    dto.setImageUrl(sticker.getImageUrl());
                    dto.setCategory(sticker.getCategory());
                    dto.setPrice(sticker.getPrice());
                    dto.setCurrency(sticker.getCurrency() != null ? sticker.getCurrency() : "USD");
                    dto.setCreatorType("user");
                    dto.setCreatorName(creatorName);
                    dto.setCreatorId(sticker.getUserId());
                    dto.setStickerType("user_created");
                    dto.setIsPublished(true);
                    dto.setStatus(sticker.getStatus());
                    dto.setAdminNote(sticker.getAdminNote());
                    dto.setCreatedAt(sticker.getCreatedAt());
                    dto.setUpdatedAt(sticker.getUpdatedAt());
                    
                    return ResponseEntity.ok(dto);
                } else {
                    // Sticker not found
                    return ResponseEntity.status(404)
                        .body(Map.of("error", "Sticker not found", 
                                     "message", "Sticker with ID " + stickerId + " does not exist."));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to publish sticker", 
                             "message", e.getMessage()));
        }
    }
    
    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@RequestBody Map<String, Object> request,
                                          @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Verify admin authentication
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized", "message", "Admin authentication required"));
            }
            
            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized", "message", "Invalid or expired token"));
            }
            
            Integer userId = jwtUtil.getUserIdFromToken(token);
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent() || !userOpt.get().getIsAdmin()) {
                return ResponseEntity.status(403)
                    .body(Map.of("error", "Forbidden", "message", "Admin access required"));
            }
            
            String name = (String) request.get("name");
            String imageUrl = (String) request.get("imageUrl");
            Boolean isPublished = request.get("isPublished") != null 
                ? Boolean.valueOf(request.get("isPublished").toString()) 
                : false;
            
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.status(400)
                    .body(Map.of("error", "Validation error", "message", "Category name is required"));
            }
            
            // Check if category with same name already exists
            Optional<Category> existingCategory = categoryRepository.findByNameIgnoreCase(name.trim());
            if (existingCategory.isPresent()) {
                return ResponseEntity.status(400)
                    .body(Map.of("error", "Duplicate category", "message", "Category with this name already exists"));
            }
            
            Category category = new Category();
            category.setName(name.trim());
            category.setImageUrl(imageUrl);
            category.setIsPublished(isPublished);
            
            Category savedCategory = categoryRepository.save(category);
            
            return ResponseEntity.ok(Map.of(
                "message", "Category created successfully",
                "category", savedCategory
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to create category", "message", e.getMessage()));
        }
    }
    
    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<?> updateCategory(@PathVariable Integer categoryId,
                                           @RequestBody Map<String, Object> request,
                                           @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Verify admin authentication
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized", "message", "Admin authentication required"));
            }
            
            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized", "message", "Invalid or expired token"));
            }
            
            Integer userId = jwtUtil.getUserIdFromToken(token);
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent() || !userOpt.get().getIsAdmin()) {
                return ResponseEntity.status(403)
                    .body(Map.of("error", "Forbidden", "message", "Admin access required"));
            }
            
            Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
            if (!categoryOpt.isPresent()) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "Category not found"));
            }
            
            Category category = categoryOpt.get();
            
            // Update name if provided
            if (request.containsKey("name")) {
                String name = (String) request.get("name");
                if (name != null && !name.trim().isEmpty()) {
                    // Check if another category with this name exists
                    Optional<Category> existingCategory = categoryRepository.findByNameIgnoreCase(name.trim());
                    if (existingCategory.isPresent() && !existingCategory.get().getId().equals(categoryId)) {
                        return ResponseEntity.status(400)
                            .body(Map.of("error", "Duplicate category", "message", "Category with this name already exists"));
                    }
                    category.setName(name.trim());
                }
            }
            
            // Update image URL if provided (can be null to remove image)
            if (request.containsKey("imageUrl")) {
                Object imageUrlObj = request.get("imageUrl");
                String imageUrl = imageUrlObj != null && !imageUrlObj.toString().trim().isEmpty() 
                    ? imageUrlObj.toString().trim() 
                    : null;
                category.setImageUrl(imageUrl);
            }
            
            // Update published status if provided
            if (request.containsKey("isPublished")) {
                Boolean isPublished = Boolean.valueOf(request.get("isPublished").toString());
                category.setIsPublished(isPublished);
            }
            
            Category updatedCategory = categoryRepository.save(category);
            
            return ResponseEntity.ok(Map.of(
                "message", "Category updated successfully",
                "category", updatedCategory
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to update category", "message", e.getMessage()));
        }
    }
    
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        try {
            List<Category> categories = categoryRepository.findAll();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/categories/used")
    public ResponseEntity<List<Category>> getUsedCategories() {
        try {
            // Get all templates to see which categories are actually used
            List<Template> templates = templateRepository.findAll();
            List<Integer> usedCategoryIds = templates.stream()
                .map(Template::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
            
            // Get only categories that are used by templates
            List<Category> usedCategories = categoryRepository.findAllById(usedCategoryIds);
            return ResponseEntity.ok(usedCategories);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer categoryId, 
                                            @RequestParam(required = false, defaultValue = "false") Boolean deleteTemplates) {
        try {
            Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
            if (!categoryOpt.isPresent()) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "Category not found"));
            }
            
            // Check if category is used by any template
            List<Template> templatesUsingCategory = templateRepository.findAll().stream()
                .filter(template -> template.getCategoryId() != null && template.getCategoryId().equals(categoryId))
                .collect(Collectors.toList());
            
            if (!templatesUsingCategory.isEmpty() && !deleteTemplates) {
                return ResponseEntity.status(400)
                    .body(Map.of("error", "Cannot delete category", 
                                 "message", "Category is being used by " + templatesUsingCategory.size() + " template(s). " +
                                           "Use deleteTemplates=true to delete category and all its templates.",
                                 "templateCount", templatesUsingCategory.size()));
            }
            
            // If deleteTemplates is true, delete all templates using this category first
            if (deleteTemplates && !templatesUsingCategory.isEmpty()) {
                // Also delete all stickers associated with these templates
                for (Template template : templatesUsingCategory) {
                    List<Sticker> stickers = stickerRepository.findByTemplateId(template.getId());
                    if (!stickers.isEmpty()) {
                        stickerRepository.deleteAll(stickers);
                    }
                }
                // Delete templates
                templateRepository.deleteAll(templatesUsingCategory);
            }
            
            // Delete the category
            categoryRepository.deleteById(categoryId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Category deleted successfully");
            if (deleteTemplates && !templatesUsingCategory.isEmpty()) {
                response.put("deletedTemplates", templatesUsingCategory.size());
                response.put("deletedTemplateTitles", templatesUsingCategory.stream()
                    .map(Template::getTitle)
                    .collect(Collectors.toList()));
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to delete category", "message", e.getMessage()));
        }
    }
    
    @DeleteMapping("/categories/cleanup/unused")
    public ResponseEntity<?> deleteUnusedCategories() {
        try {
            // Get all templates to see which categories are actually used
            List<Template> templates = templateRepository.findAll();
            List<Integer> usedCategoryIds = templates.stream()
                .map(Template::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
            
            // Get all categories
            List<Category> allCategories = categoryRepository.findAll();
            
            // Find unused categories
            List<Category> unusedCategories = allCategories.stream()
                .filter(category -> !usedCategoryIds.contains(category.getId()))
                .collect(Collectors.toList());
            
            // Delete unused categories
            if (!unusedCategories.isEmpty()) {
                List<Integer> unusedIds = unusedCategories.stream()
                    .map(Category::getId)
                    .collect(Collectors.toList());
                categoryRepository.deleteAllById(unusedIds);
                
                return ResponseEntity.ok(Map.of(
                    "message", "Unused categories deleted successfully",
                    "deletedCount", unusedIds.size(),
                    "deletedCategories", unusedCategories.stream()
                        .map(Category::getName)
                        .collect(Collectors.toList())
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "message", "No unused categories found",
                    "deletedCount", 0
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to delete unused categories", "message", e.getMessage()));
        }
    }
    
    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders() {
        try {
            List<Order> orders = orderRepository.findAll();
            // Sort by created date descending
            orders.sort((a, b) -> {
                if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            });
            List<AdminOrderDto> orderDtos = orders.stream().map(order -> {
                AdminOrderDto dto = new AdminOrderDto();
                dto.setId(order.getId());
                dto.setOrderNumber(order.getOrderNumber());
                dto.setRazorpayOrderId(order.getRazorpayOrderId());
                dto.setRazorpayPaymentId(order.getRazorpayPaymentId());
                dto.setUserId(order.getUserId());
                dto.setAmount(order.getAmount());
                dto.setCurrency(order.getCurrency());
                dto.setStatus(order.getStatus());
                dto.setOrderType(order.getOrderType());
                dto.setCreatedAt(order.getCreatedAt() != null ? order.getCreatedAt().toString() : null);
                dto.setPaidAt(order.getPaidAt() != null ? order.getPaidAt().toString() : null);
                
                // Get user information
                Optional<User> userOpt = userRepository.findById(order.getUserId());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    dto.setUserName(user.getName());
                    dto.setUserEmail(user.getEmail());
                }
                
                // Parse order data to get item count
                try {
                    if (order.getOrderData() != null && !order.getOrderData().isEmpty()) {
                        ObjectMapper mapper = new ObjectMapper();
                        List<Map<String, Object>> orderItems = mapper.readValue(
                            order.getOrderData(), 
                            new TypeReference<List<Map<String, Object>>>() {}
                        );
                        dto.setOrderData(orderItems);
                        dto.setItemCount(orderItems.size());
                    } else {
                        dto.setItemCount(0);
                    }
                } catch (Exception e) {
                    dto.setItemCount(0);
                }
                
                return dto;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(orderDtos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to fetch orders", "message", e.getMessage()));
        }
    }
}

