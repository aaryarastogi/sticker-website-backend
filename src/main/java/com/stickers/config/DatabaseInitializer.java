package com.stickers.config;

import com.stickers.entity.Category;
import com.stickers.entity.Sticker;
import com.stickers.entity.Template;
import com.stickers.repository.CategoryRepository;
import com.stickers.repository.StickerRepository;
import com.stickers.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class DatabaseInitializer implements CommandLineRunner {
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private TemplateRepository templateRepository;
    
    @Autowired
    private StickerRepository stickerRepository;
    
    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            seedCategories();
        }
        if (templateRepository.count() == 0) {
            seedTemplates();
        }
        if (stickerRepository.count() == 0) {
            seedStickers();
        } else {
            updateExistingStickersWithFinishes();
        }
    }
    
    private void seedCategories() {
        List<String> categoryNames = Arrays.asList(
            "Oval", "Circle Sticker", "Illustration", "Waxing", "Splatter",
            "Smile", "Santa Claus", "Spelling", "Mickey Mouse Template", "Funny",
            "Paint", "Volunteer", "Unicorn", "Safety", "Personalized",
            "Christmas", "Valentine", "Sport"
        );
        
        for (String name : categoryNames) {
            Category category = new Category();
            category.setName(name);
            categoryRepository.save(category);
        }
    }
    
    private void seedTemplates() {
        List<Category> categories = categoryRepository.findAll();
        java.util.Map<String, Category> categoryMap = new java.util.HashMap<>();
        for (Category cat : categories) {
            categoryMap.put(cat.getName(), cat);
        }
        
        List<Object[]> templates = Arrays.asList(
            new Object[]{"Safety Sticker", "https://template.canva.com/EAEVzH0z3xs/2/0/400w-DkzNIfSkjOg.jpg", "Safety", true},
            new Object[]{"Personalized Sticker", "https://template.canva.com/EAEVzPoOheA/5/0/400w-2fxPudgb_YU.jpg", "Personalized", true},
            new Object[]{"Christmas Sticker", "https://template.canva.com/EAELAsw9ajc/2/0/400w-_0IlWcZP25s.jpg", "Christmas", true},
            new Object[]{"Valentine's Day Sticker", "https://template.canva.com/EAE1NypPdUc/3/0/400w-HU9xbqWQJf0.jpg", "Valentine", false},
            new Object[]{"Sport Sticker", "https://template.canva.com/EADzBjUHBoM/1/0/400w-vcb5-2iejVI.jpg", "Sport", false},
            new Object[]{"Funny Sticker", "https://template.canva.com/EAEVzH0z3xs/2/0/400w-DkzNIfSkjOg.jpg", "Funny", true},
            new Object[]{"Unicorn Sticker", "https://template.canva.com/EAEVzPoOheA/5/0/400w-2fxPudgb_YU.jpg", "Unicorn", false},
            new Object[]{"Smile Sticker", "https://template.canva.com/EAELAsw9ajc/2/0/400w-_0IlWcZP25s.jpg", "Smile", true},
            new Object[]{"Circle Sticker", "https://template.canva.com/EAE1NypPdUc/3/0/400w-HU9xbqWQJf0.jpg", "Circle Sticker", false},
            new Object[]{"Oval Sticker", "https://template.canva.com/EADzBjUHBoM/1/0/400w-vcb5-2iejVI.jpg", "Oval", false}
        );
        
        for (Object[] templateData : templates) {
            Template template = new Template();
            template.setTitle((String) templateData[0]);
            template.setImageUrl((String) templateData[1]);
            Category category = categoryMap.get((String) templateData[2]);
            template.setCategoryId(category != null ? category.getId() : null);
            template.setIsTrending((Boolean) templateData[3]);
            templateRepository.save(template);
        }
    }
    
    private void seedStickers() {
        List<Template> templates = templateRepository.findAll();
        Random random = new Random();
        List<String> finishTypes = Arrays.asList("matte", "glossy", "metal");
        List<List<String>> colorSets = Arrays.asList(
            Arrays.asList("#9D3DD9", "#3D9DD9", "#F4D956"),
            Arrays.asList("#FF6B6B", "#4ECDC4", "#FFE66D"),
            Arrays.asList("#95E1D3", "#F38181", "#AA96DA"),
            Arrays.asList("#FFA07A", "#20B2AA", "#FFD700"),
            Arrays.asList("#FF69B4", "#00CED1", "#FFA500"),
            Arrays.asList("#9370DB", "#00FA9A", "#FF1493")
        );
        
        for (Template template : templates) {
            int stickerCount = 4 + random.nextInt(2); // 4-5 stickers per template
            
            for (int i = 1; i <= stickerCount; i++) {
                Sticker sticker = new Sticker();
                sticker.setTemplateId(template.getId());
                sticker.setName(template.getTitle() + " Variant " + i);
                sticker.setImageUrl("https://template.canva.com/EAEVzH0z3xs/2/0/400w-DkzNIfSkjOg.jpg");
                sticker.setColors(colorSets.get(random.nextInt(colorSets.size())));
                
                List<String> finishes = new ArrayList<>();
                if (random.nextDouble() < 0.6) {
                    int numFinishes = 1 + random.nextInt(3);
                    List<String> shuffled = new ArrayList<>(finishTypes);
                    java.util.Collections.shuffle(shuffled);
                    finishes = shuffled.subList(0, Math.min(numFinishes, shuffled.size()));
                }
                sticker.setFinishes(finishes);
                
                sticker.setPrice(new BigDecimal(900 + random.nextInt(200))); // 900-1100
                stickerRepository.save(sticker);
            }
        }
    }
    
    private void updateExistingStickersWithFinishes() {
        List<Sticker> stickers = stickerRepository.findAll();
        Random random = new Random();
        List<String> finishTypes = Arrays.asList("matte", "glossy", "metal");
        
        for (Sticker sticker : stickers) {
            if (sticker.getFinishes() == null || sticker.getFinishes().isEmpty()) {
                List<String> finishes = new ArrayList<>();
                if (random.nextDouble() < 0.6) {
                    int numFinishes = 1 + random.nextInt(3);
                    List<String> shuffled = new ArrayList<>(finishTypes);
                    java.util.Collections.shuffle(shuffled);
                    finishes = shuffled.subList(0, Math.min(numFinishes, shuffled.size()));
                }
                sticker.setFinishes(finishes);
                stickerRepository.save(sticker);
            }
        }
    }
}

