package com.stickers.service;

import com.stickers.dto.StickerDto;
import com.stickers.dto.TemplateDto;
import com.stickers.dto.UserSearchDto;
import com.stickers.entity.Category;
import com.stickers.entity.Sticker;
import com.stickers.entity.Template;
import com.stickers.entity.User;
import com.stickers.entity.UserCreatedSticker;
import com.stickers.repository.CategoryRepository;
import com.stickers.repository.StickerRepository;
import com.stickers.repository.TemplateRepository;
import com.stickers.repository.UserCreatedStickerRepository;
import com.stickers.repository.StickerLikeRepository;
import com.stickers.repository.UserRepository;
import com.stickers.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TemplateService {
    @Autowired
    private TemplateRepository templateRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private StickerRepository stickerRepository;
    
    @Autowired
    private UserCreatedStickerRepository userCreatedStickerRepository;
    
    @Autowired
    private StickerLikeRepository likeRepository;
    
    @Autowired
    private LikeService likeService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserRepository userRepository;
    
    public List<TemplateDto> getAllTemplates(Integer categoryId, Boolean trending) {
        List<Template> templates;
        if (trending != null && trending) {
            templates = templateRepository.findByIsTrendingTrue();
        } else if (categoryId != null) {
            templates = templateRepository.findByCategoryId(categoryId);
        } else {
            templates = templateRepository.findAll();
        }
        
        Map<Integer, Category> categoryMap = categoryRepository.findAll().stream()
            .collect(Collectors.toMap(Category::getId, cat -> cat));
        
        return templates.stream()
            .map(t -> {
                Category category = t.getCategoryId() != null ? categoryMap.get(t.getCategoryId()) : null;
                return new TemplateDto(
                    t.getId(),
                    t.getTitle(),
                    t.getImageUrl(),
                    t.getIsTrending(),
                    category != null ? category.getName() : null,
                    t.getCategoryId()
                );
            })
            .collect(Collectors.toList());
    }
    
    public Optional<TemplateDto> getTemplateById(Integer id) {
        return templateRepository.findById(id)
            .map(t -> {
                Category category = t.getCategoryId() != null 
                    ? categoryRepository.findById(t.getCategoryId()).orElse(null) 
                    : null;
                return new TemplateDto(
                    t.getId(),
                    t.getTitle(),
                    t.getImageUrl(),
                    t.getIsTrending(),
                    category != null ? category.getName() : null,
                    t.getCategoryId()
                );
            });
    }
    
    public List<StickerDto> getStickersByTemplate(Integer templateId, String authHeader) {
        // Only get published template stickers
        List<Sticker> stickers = stickerRepository.findByTemplateIdAndIsPublishedTrue(templateId);
        Template template = templateRepository.findById(templateId).orElse(null);
        String templateTitle = template != null ? template.getTitle() : null;
        String resolvedCategoryName = null;
        if (template != null && template.getCategoryId() != null) {
            resolvedCategoryName = categoryRepository.findById(template.getCategoryId())
                .map(Category::getName)
                .orElse(null);
        }
        final String categoryName = resolvedCategoryName;
        
        Integer userId = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                userId = jwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                // User not authenticated, userId remains null
            }
        }
        
        final Integer finalUserId = userId;
        List<StickerDto> results = new java.util.ArrayList<>();
        
        stickers.stream()
            .filter(s -> s.getIsPublished() == null || s.getIsPublished())
            .map(s -> {
                long likeCount = likeService.getLikeCount(s.getId(), "template");
                boolean isLiked = finalUserId != null && likeService.isLiked(finalUserId, s.getId(), "template");
                
                StickerDto dto = new StickerDto();
                dto.setId(s.getId());
                dto.setTemplate_id(s.getTemplateId());
                dto.setName(s.getName());
                dto.setImage_url(s.getImageUrl());
                dto.setColors(s.getColors());
                dto.setFinishes(s.getFinishes());
                dto.setPrice(s.getPrice());
                dto.setCurrency(s.getCurrency() != null ? s.getCurrency() : "USD");
                dto.setTemplate_title(templateTitle);
                dto.setSticker_type("template");
                dto.setLike_count(likeCount);
                dto.setIs_liked(isLiked);
                return dto;
            })
            .forEach(results::add);
        
        if (categoryName != null && !categoryName.isBlank()) {
            List<UserCreatedSticker> userStickers = userCreatedStickerRepository
                .findByCategoryIgnoreCaseAndIsPublishedTrue(categoryName);
            userStickers.stream()
                .map(sticker -> {
                    long likeCount = likeService.getLikeCount(sticker.getId(), "user_created");
                    boolean isLiked = finalUserId != null && likeService.isLiked(finalUserId, sticker.getId(), "user_created");
                    StickerDto dto = new StickerDto();
                    dto.setId(sticker.getId());
                    dto.setTemplate_id(null);
                    dto.setName(sticker.getName());
                    dto.setImage_url(sticker.getImageUrl());
                    dto.setColors(null);
                    dto.setFinishes(null);
                    dto.setPrice(sticker.getPrice());
                    dto.setCurrency(sticker.getCurrency() != null ? sticker.getCurrency() : "USD");
                    dto.setTemplate_title(categoryName);
                    dto.setSticker_type("user_created");
                    dto.setLike_count(likeCount);
                    dto.setIs_liked(isLiked);
                    return dto;
                })
                .forEach(results::add);
        }
        
        return results;
    }
    
    public List<StickerDto> getStickersByTemplateTitle(String title, String authHeader) {
        List<Sticker> stickers = stickerRepository.findByTemplateTitle(title);
        String templateTitle = title;
        
        Integer userId = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                userId = jwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                // User not authenticated, userId remains null
            }
        }
        
        final Integer finalUserId = userId;
        List<StickerDto> results = new java.util.ArrayList<>();
        
        stickers.stream()
            .filter(s -> s.getIsPublished() == null || s.getIsPublished())
            .map(s -> {
                long likeCount = likeService.getLikeCount(s.getId(), "template");
                boolean isLiked = finalUserId != null && likeService.isLiked(finalUserId, s.getId(), "template");
                
                StickerDto dto = new StickerDto();
                dto.setId(s.getId());
                dto.setTemplate_id(s.getTemplateId());
                dto.setName(s.getName());
                dto.setImage_url(s.getImageUrl());
                dto.setColors(s.getColors());
                dto.setFinishes(s.getFinishes());
                dto.setPrice(s.getPrice());
                dto.setCurrency(s.getCurrency() != null ? s.getCurrency() : "USD");
                dto.setTemplate_title(templateTitle);
                dto.setSticker_type("template");
                dto.setLike_count(likeCount);
                dto.setIs_liked(isLiked);
                return dto;
            })
            .forEach(results::add);
        
        if (title != null && !title.isBlank()) {
            final String categoryForUser = title;
            List<UserCreatedSticker> userStickers = userCreatedStickerRepository
                .findByCategoryIgnoreCaseAndIsPublishedTrue(title);
            userStickers.stream()
                .map(sticker -> {
                    long likeCount = likeService.getLikeCount(sticker.getId(), "user_created");
                    boolean isLiked = finalUserId != null && likeService.isLiked(finalUserId, sticker.getId(), "user_created");
                    StickerDto dto = new StickerDto();
                    dto.setId(sticker.getId());
                    dto.setTemplate_id(null);
                    dto.setName(sticker.getName());
                    dto.setImage_url(sticker.getImageUrl());
                    dto.setColors(null);
                    dto.setFinishes(null);
                    dto.setPrice(sticker.getPrice());
                    dto.setTemplate_title(categoryForUser);
                    dto.setSticker_type("user_created");
                    dto.setLike_count(likeCount);
                    dto.setIs_liked(isLiked);
                    return dto;
                })
                .forEach(results::add);
        }
        
        return results;
    }
    
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    public List<TemplateDto> searchTemplates(String query) {
        List<Template> templates = templateRepository.searchByTitle(query);
        Map<Integer, Category> categoryMap = categoryRepository.findAll().stream()
            .collect(Collectors.toMap(Category::getId, cat -> cat));
        
        return templates.stream()
            .map(t -> {
                Category category = t.getCategoryId() != null ? categoryMap.get(t.getCategoryId()) : null;
                return new TemplateDto(
                    t.getId(),
                    t.getTitle(),
                    t.getImageUrl(),
                    t.getIsTrending(),
                    category != null ? category.getName() : null,
                    t.getCategoryId()
                );
            })
            .collect(Collectors.toList());
    }
    
    public List<StickerDto> searchStickers(String query) {
        List<Sticker> stickers = stickerRepository.searchByName(query);
        Map<Integer, Template> templateMap = templateRepository.findAll().stream()
            .collect(Collectors.toMap(Template::getId, t -> t));
        
        return stickers.stream()
            .filter(s -> s.getIsPublished() == null || s.getIsPublished())
            .map(s -> {
                Template template = s.getTemplateId() != null ? templateMap.get(s.getTemplateId()) : null;
                StickerDto dto = new StickerDto();
                dto.setId(s.getId());
                dto.setTemplate_id(s.getTemplateId());
                dto.setName(s.getName());
                dto.setImage_url(s.getImageUrl());
                dto.setColors(s.getColors());
                dto.setFinishes(s.getFinishes());
                dto.setPrice(s.getPrice());
                dto.setTemplate_title(template != null ? template.getTitle() : null);
                dto.setSticker_type("template");
                dto.setLike_count(0L);
                dto.setIs_liked(false);
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    public List<UserSearchDto> searchUsers(String query) {
        List<User> users = userRepository.searchByNameOrUsername(query);
        return users.stream()
            .map(user -> new UserSearchDto(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getProfileImageUrl()
            ))
            .collect(Collectors.toList());
    }
    
    public List<Category> searchCategories(String query) {
        return categoryRepository.searchByName(query);
    }
    
    public List<StickerDto> getTrendingStickers(String authHeader) {
        // Get top liked stickers from both template and user_created types
        List<Object[]> topTemplateStickersData = likeRepository.findTopStickersByLikes(10, "template");
        List<Object[]> topUserCreatedStickersData = likeRepository.findTopStickersByLikes(10, "user_created");
        
        Integer userId = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                userId = jwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                // User not authenticated, userId remains null
            }
        }
        final Integer finalUserId = userId;
        
        // Create a list to hold all stickers with their like counts and types
        List<StickerWithLikeInfo> allStickers = new java.util.ArrayList<>();
        
        // Process template stickers
        for (Object[] result : topTemplateStickersData) {
            Object stickerIdObj = result[0];
            Integer stickerId;
            Long likeCount = 0L;
            
            // Native query returns: [sticker_id, like_count]
            if (stickerIdObj instanceof Number) {
                stickerId = ((Number) stickerIdObj).intValue();
            } else {
                stickerId = Integer.valueOf(stickerIdObj.toString());
            }
            
            // Get like count from result (it's already in the query result as COUNT(*))
            if (result.length > 1) {
                Object likeCountObj = result[1];
                if (likeCountObj instanceof Number) {
                    likeCount = ((Number) likeCountObj).longValue();
                } else if (likeCountObj != null) {
                    likeCount = Long.valueOf(likeCountObj.toString());
                }
            } else {
                // If like count not in result, fetch it
                likeCount = likeRepository.countByStickerIdAndStickerType(stickerId, "template");
            }
            
            Optional<Sticker> stickerOpt = stickerRepository.findById(stickerId);
            if (stickerOpt.isPresent()) {
                Sticker sticker = stickerOpt.get();
                // Only include published stickers
                if (sticker.getIsPublished() == null || sticker.getIsPublished()) {
                    // Get template title
                    String templateTitle = null;
                    if (sticker.getTemplateId() != null) {
                        Optional<Template> templateOpt = templateRepository.findById(sticker.getTemplateId());
                        if (templateOpt.isPresent()) {
                            templateTitle = templateOpt.get().getTitle();
                        }
                    }
                    
                    allStickers.add(new StickerWithLikeInfo(
                        sticker.getId(),
                        sticker.getTemplateId(),
                        sticker.getName(),
                        sticker.getImageUrl(),
                        sticker.getColors(),
                        sticker.getFinishes(),
                        sticker.getPrice(),
                        sticker.getCurrency() != null ? sticker.getCurrency() : "USD",
                        templateTitle,
                        likeCount,
                        "template"
                    ));
                }
            }
        }
        
        // Process user-created stickers
        for (Object[] result : topUserCreatedStickersData) {
            Object stickerIdObj = result[0];
            Integer stickerId;
            Long likeCount = 0L;
            
            // Native query returns: [sticker_id, like_count]
            if (stickerIdObj instanceof Number) {
                stickerId = ((Number) stickerIdObj).intValue();
            } else {
                stickerId = Integer.valueOf(stickerIdObj.toString());
            }
            
            // Get like count from result (it's already in the query result as COUNT(*))
            if (result.length > 1) {
                Object likeCountObj = result[1];
                if (likeCountObj instanceof Number) {
                    likeCount = ((Number) likeCountObj).longValue();
                } else if (likeCountObj != null) {
                    likeCount = Long.valueOf(likeCountObj.toString());
                }
            } else {
                // If like count not in result, fetch it
                likeCount = likeRepository.countByStickerIdAndStickerType(stickerId, "user_created");
            }
            
            Optional<UserCreatedSticker> stickerOpt = userCreatedStickerRepository.findById(stickerId);
            if (stickerOpt.isPresent()) {
                UserCreatedSticker sticker = stickerOpt.get();
                if (sticker.getIsPublished() != null && sticker.getIsPublished()) {
                    allStickers.add(new StickerWithLikeInfo(
                        sticker.getId(),
                        null, // templateId
                        sticker.getName(),
                        sticker.getImageUrl(),
                        null, // colors
                        null, // finishes
                        sticker.getPrice(),
                        sticker.getCurrency() != null ? sticker.getCurrency() : "USD",
                        sticker.getCategory(), // templateTitle
                        likeCount,
                        "user_created"
                    ));
                }
            }
        }
        
        // Sort by like count (descending) and take top 5
        List<StickerWithLikeInfo> top5Stickers = allStickers.stream()
            .sorted((a, b) -> Long.compare(b.likeCount, a.likeCount))
            .limit(5)
            .collect(Collectors.toList());
        
        // Convert to StickerDto
        return top5Stickers.stream()
            .map(info -> {
                boolean isLiked = false;
                if (finalUserId != null) {
                    try {
                        isLiked = likeService.isLiked(finalUserId, info.id, info.type);
                    } catch (Exception e) {
                        // Ignore errors
                    }
                }
                
                StickerDto dto = new StickerDto();
                dto.setId(info.id);
                dto.setTemplate_id(info.templateId);
                dto.setName(info.name);
                dto.setImage_url(info.imageUrl);
                dto.setColors(info.colors);
                dto.setFinishes(info.finishes);
                dto.setPrice(info.price);
                dto.setCurrency(info.currency != null ? info.currency : "USD");
                dto.setTemplate_title(info.templateTitle);
                dto.setSticker_type(info.type); // Set sticker type
                dto.setLike_count(info.likeCount);
                dto.setIs_liked(isLiked);
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    // Helper class to hold sticker info with like count
    private static class StickerWithLikeInfo {
        Integer id;
        Integer templateId;
        String name;
        String imageUrl;
        List<String> colors;
        List<String> finishes;
        java.math.BigDecimal price;
        String currency;
        String templateTitle;
        Long likeCount;
        String type;
        
        StickerWithLikeInfo(Integer id, Integer templateId, String name, String imageUrl,
                           List<String> colors, List<String> finishes, java.math.BigDecimal price,
                           String currency, String templateTitle, Long likeCount, String type) {
            this.id = id;
            this.templateId = templateId;
            this.name = name;
            this.imageUrl = imageUrl;
            this.colors = colors;
            this.finishes = finishes;
            this.price = price;
            this.currency = currency;
            this.templateTitle = templateTitle;
            this.likeCount = likeCount;
            this.type = type;
        }
    }
}

