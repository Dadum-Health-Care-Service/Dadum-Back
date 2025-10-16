package com.project.mog.repository.shop;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    
    Optional<ProductEntity> findByProductId(Long productId);
    
    List<ProductEntity> findBySellerId(Long sellerId);
    
    Page<ProductEntity> findBySellerId(Long sellerId, Pageable pageable);
    
    @Query("SELECT p FROM ProductEntity p WHERE p.sellerId = :sellerId AND p.isActive = true")
    Page<ProductEntity> findActiveProductsBySeller(@Param("sellerId") Long sellerId, Pageable pageable);
    
    List<ProductEntity> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
    
    // 판매자 대시보드용 메서드들
    Long countBySellerId(Long sellerId);
    
    Long countBySellerIdAndStockGreaterThan(Long sellerId, Integer stock);
    
    // 카테고리별 상품 조회
    List<ProductEntity> findBySellerIdAndCategory(Long sellerId, String category);
    
    // 상품명으로 검색
    @Query("SELECT p FROM ProductEntity p WHERE p.sellerId = :sellerId AND p.productName LIKE %:keyword%")
    List<ProductEntity> findBySellerIdAndProductNameContaining(@Param("sellerId") Long sellerId, 
                                                               @Param("keyword") String keyword);
}
