package com.stickers.repository;

import com.stickers.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Integer> {
    List<Template> findByCategoryId(Integer categoryId);
    List<Template> findByIsTrendingTrue();
    @Query("SELECT t FROM Template t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Template> searchByTitle(@Param("query") String query);
}

