package com.stickers.repository;

import com.stickers.entity.StickerLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StickerLikeRepository extends JpaRepository<StickerLike, Integer> {
    Optional<StickerLike> findByUserIdAndStickerIdAndStickerType(Integer userId, Integer stickerId, String stickerType);
    boolean existsByUserIdAndStickerIdAndStickerType(Integer userId, Integer stickerId, String stickerType);
    long countByStickerIdAndStickerType(Integer stickerId, String stickerType);
    List<StickerLike> findByStickerIdAndStickerType(Integer stickerId, String stickerType);
    
    @Query(value = "SELECT sticker_id, COUNT(*) as like_count FROM sticker_likes WHERE sticker_type = :stickerType GROUP BY sticker_id ORDER BY like_count DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopStickersByLikes(int limit, String stickerType);
    
    @Query(value = "SELECT sticker_id, sticker_type, COUNT(*) as like_count FROM sticker_likes GROUP BY sticker_id, sticker_type ORDER BY like_count DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopStickersByLikesAllTypes(int limit);
    
    @Query("SELECT sl.stickerId, COUNT(sl) as likeCount FROM StickerLike sl WHERE sl.stickerType = :stickerType GROUP BY sl.stickerId ORDER BY likeCount DESC")
    List<Object[]> findAllStickersByLikes(String stickerType);
}

