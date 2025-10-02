package com.project.mog.repository.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<RefundEntity, Long> {
    
    // 판매자별 환불 목록 조회 (페이징)
    Page<RefundEntity> findBySellerId(Long sellerId, Pageable pageable);
    
    // 판매자별 환불 목록 조회 (전체)
    List<RefundEntity> findBySellerId(Long sellerId);
    
    // 판매자별 상태별 환불 조회
    List<RefundEntity> findBySellerIdAndStatus(Long sellerId, RefundEntity.RefundStatus status);
    
    // 환불 상세 조회
    Optional<RefundEntity> findByRefundId(Long refundId);
    
    // 판매자별 환불 상세 조회
    Optional<RefundEntity> findByRefundIdAndSellerId(Long refundId, Long sellerId);
    
    // 주문별 환불 조회
    List<RefundEntity> findByOrderId(Long orderId);
    
    // 고객별 환불 조회
    List<RefundEntity> findByCustomerId(Long customerId);
    
    // 환불 상태별 카운트
    Long countBySellerIdAndStatus(Long sellerId, RefundEntity.RefundStatus status);
    
    // 판매자별 전체 환불 카운트
    Long countBySellerId(Long sellerId);
    
    // 검색 기능
    @Query("SELECT r FROM RefundEntity r WHERE r.sellerId = :sellerId AND " +
           "(r.customerName LIKE %:keyword% OR r.productName LIKE %:keyword%)")
    Page<RefundEntity> findBySellerIdAndKeyword(@Param("sellerId") Long sellerId, 
                                               @Param("keyword") String keyword, 
                                               Pageable pageable);
}
