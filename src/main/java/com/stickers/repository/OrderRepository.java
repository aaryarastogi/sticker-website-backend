package com.stickers.repository;

import com.stickers.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByUserIdOrderByCreatedAtDesc(Integer userId);
    List<Order> findByStatus(String status);
    List<Order> findAllByOrderByCreatedAtDesc();
}


