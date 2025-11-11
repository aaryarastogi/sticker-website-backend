package com.stickers.repository;

import com.stickers.entity.Sticker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StickerRepository extends JpaRepository<Sticker, Integer> {
    List<Sticker> findByTemplateId(Integer templateId);
    List<Sticker> findByTemplateIdAndIsPublishedTrue(Integer templateId);
    List<Sticker> findByTemplateIdIn(List<Integer> templateIds);
    @Query("SELECT s FROM Sticker s WHERE s.templateId IN " +
           "(SELECT t.id FROM Template t WHERE LOWER(t.title) = LOWER(:title)) AND (s.isPublished IS NULL OR s.isPublished = true)")
    List<Sticker> findByTemplateTitle(@Param("title") String title);
    @Query("SELECT s FROM Sticker s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) AND (s.isPublished IS NULL OR s.isPublished = true)")
    List<Sticker> searchByName(@Param("query") String query);
}

