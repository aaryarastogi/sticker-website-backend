package com.stickers.repository;

import com.stickers.entity.UserCreatedSticker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserCreatedStickerRepository extends JpaRepository<UserCreatedSticker, Integer> {
    List<UserCreatedSticker> findByUserId(Integer userId);
    List<UserCreatedSticker> findByIsPublishedTrue();
    @Query("SELECT ucs FROM UserCreatedSticker ucs WHERE ucs.userId = :userId AND ucs.id = :id")
    UserCreatedSticker findByUserIdAndId(Integer userId, Integer id);
}

