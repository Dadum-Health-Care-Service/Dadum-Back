package com.project.mog.repository.payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.mog.repository.users.UsersEntity;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    
    Optional<OrderEntity> findByOrderNumber(String orderNumber);
    
    List<OrderEntity> findByUser(UsersEntity user);
    
    List<OrderEntity> findByOrderStatus(String orderStatus);
    
    List<OrderEntity> findByUserOrderByCreatedAtDesc(UsersEntity user);
    
    List<OrderEntity> deleteByUser(UsersEntity user);
    
    // 판매자 대시보드용 메서드들
    Long countBySellerIdAndCreatedAtBetween(Long sellerId, LocalDateTime start, LocalDateTime end);
    
    Long countBySellerIdAndOrderStatus(Long sellerId, String orderStatus);
    
    // 판매자 ID로 주문 조회
    List<OrderEntity> findBySellerId(Long sellerId);
    
    Page<OrderEntity> findBySellerId(Long sellerId, Pageable pageable);
    
    // 판매자 ID와 날짜 범위로 주문 조회
    List<OrderEntity> findBySellerIdAndCreatedAtBetween(Long sellerId, LocalDateTime start, LocalDateTime end);
    
    // 판매자 ID와 주문 상태로 조회
    List<OrderEntity> findBySellerIdAndOrderStatus(Long sellerId, String orderStatus);
    
    // 판매자 ID로 주문 조회 (최신순)
    List<OrderEntity> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
    
    // 주문 ID로 조회
    Optional<OrderEntity> findByOrderId(Long orderId);
    
    // 사용자 ID로 주문 조회 (최신순)
    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // 사용자 ID로 주문 조회 (페이징)
    Page<OrderEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // 사용자 ID와 주문 상태로 조회
    List<OrderEntity> findByUserIdAndOrderStatusOrderByCreatedAtDesc(Long userId, String orderStatus);
}
