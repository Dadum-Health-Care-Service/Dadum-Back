package com.project.mog.service.payment.seller;

import com.project.mog.repository.shop.ProductEntity;
import com.project.mog.repository.shop.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductManagementService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * 판매자의 활성 상품 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<ProductEntity> getProductsBySeller(Long sellerId, Pageable pageable) {
        List<ProductEntity> allProducts = productRepository.findBySellerId(sellerId).stream()
                .filter(p -> p.getIsActive() == true)
                .collect(java.util.stream.Collectors.toList());
        
        // PageImpl로 변환하여 반환
        return new org.springframework.data.domain.PageImpl<>(allProducts, pageable, allProducts.size());
    }

    /**
     * 판매자의 모든 상품 조회 (전체, 활성/비활성 포함)
     */
    @Transactional(readOnly = true)
    public List<ProductEntity> getAllProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    /**
     * 상품 상세 조회
     */
    @Transactional(readOnly = true)
    public Optional<ProductEntity> getProductById(Long productId, Long sellerId) {
        return productRepository.findByProductId(productId)
                .filter(product -> product.getSellerId().equals(sellerId));
    }

    /**
     * 상품 등록
     */
    public ProductEntity createProduct(Long sellerId, String productName, String description, 
                                     BigDecimal price, Integer stock, String category, String imageUrl, String imageData,
                                     String detailFileData, String detailFileType) {
        ProductEntity product = ProductEntity.builder()
                .sellerId(sellerId)
                .productName(productName)
                .description(description)
                .price(price)
                .stock(stock)
                .category(category)
                .imageUrl(imageUrl)
                .imageData(imageData)
                .detailFileData(detailFileData)
                .detailFileType(detailFileType)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        return productRepository.save(product);
    }

    /**
     * 상품 수정
     */
    public ProductEntity updateProduct(Long productId, Long sellerId, String productName, 
                                     String description, BigDecimal price, Integer stock, 
                                     String category, String imageUrl, String imageData, 
                                     String detailFileData, String detailFileType, Boolean isActive) {
        ProductEntity product = productRepository.findByProductId(productId)
                .filter(p -> p.getSellerId().equals(sellerId))
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        
        product.setProductName(productName);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        product.setCategory(category);
        product.setImageUrl(imageUrl);
        product.setImageData(imageData);
        product.setDetailFileData(detailFileData);
        product.setDetailFileType(detailFileType);
        product.setIsActive(isActive);
        product.setUpdatedAt(LocalDateTime.now());
        
        return productRepository.save(product);
    }

    /**
     * 상품 삭제 (소프트 삭제)
     */
    public void deleteProduct(Long productId, Long sellerId) {
        ProductEntity product = productRepository.findByProductId(productId)
                .filter(p -> p.getSellerId().equals(sellerId))
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        
        product.setIsActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    /**
     * 상품 재고 수정
     */
    public ProductEntity updateStock(Long productId, Long sellerId, Integer newStock) {
        ProductEntity product = productRepository.findByProductId(productId)
                .filter(p -> p.getSellerId().equals(sellerId))
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        
        product.setStock(newStock);
        product.setUpdatedAt(LocalDateTime.now());
        
        return productRepository.save(product);
    }

    /**
     * 카테고리별 상품 조회
     */
    @Transactional(readOnly = true)
    public List<ProductEntity> getProductsByCategory(Long sellerId, String category) {
        return productRepository.findBySellerIdAndCategory(sellerId, category);
    }

    /**
     * 상품명으로 검색
     */
    @Transactional(readOnly = true)
    public List<ProductEntity> searchProducts(Long sellerId, String keyword) {
        return productRepository.findBySellerIdAndProductNameContaining(sellerId, keyword);
    }

    /**
     * 모든 활성 상품 조회 (Shop용)
     */
    @Transactional(readOnly = true)
    public List<ProductEntity> getAllActiveProducts() {
        return productRepository.findAll().stream()
                .filter(p -> p.getIsActive() == true)
                .collect(java.util.stream.Collectors.toList());
    }
}
