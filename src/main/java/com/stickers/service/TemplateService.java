package com.stickers.service;

import com.stickers.dto.StickerDto;
import com.stickers.dto.TemplateDto;
import com.stickers.entity.Category;
import com.stickers.entity.Sticker;
import com.stickers.entity.Template;
import com.stickers.repository.CategoryRepository;
import com.stickers.repository.StickerRepository;
import com.stickers.repository.TemplateRepository;
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
    
    public List<StickerDto> getStickersByTemplate(Integer templateId) {
        List<Sticker> stickers = stickerRepository.findByTemplateId(templateId);
        Template template = templateRepository.findById(templateId).orElse(null);
        String templateTitle = template != null ? template.getTitle() : null;
        
        return stickers.stream()
            .map(s -> new StickerDto(
                s.getId(),
                s.getTemplateId(),
                s.getName(),
                s.getImageUrl(),
                s.getColors(),
                s.getFinishes(),
                s.getPrice(),
                templateTitle
            ))
            .collect(Collectors.toList());
    }
    
    public List<StickerDto> getStickersByTemplateTitle(String title) {
        List<Sticker> stickers = stickerRepository.findByTemplateTitle(title);
        String templateTitle = title;
        
        return stickers.stream()
            .map(s -> new StickerDto(
                s.getId(),
                s.getTemplateId(),
                s.getName(),
                s.getImageUrl(),
                s.getColors(),
                s.getFinishes(),
                s.getPrice(),
                templateTitle
            ))
            .collect(Collectors.toList());
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
            .map(s -> {
                Template template = s.getTemplateId() != null ? templateMap.get(s.getTemplateId()) : null;
                return new StickerDto(
                    s.getId(),
                    s.getTemplateId(),
                    s.getName(),
                    s.getImageUrl(),
                    s.getColors(),
                    s.getFinishes(),
                    s.getPrice(),
                    template != null ? template.getTitle() : null
                );
            })
            .collect(Collectors.toList());
    }
}

