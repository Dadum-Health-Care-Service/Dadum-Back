package com.project.mog.service.shop;

import com.project.mog.dto.shop.ProductDto;
import com.project.mog.repository.shop.ProductEntity;
import com.project.mog.repository.shop.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    public List<ProductDto> getAllProducts() {
        List<ProductEntity> entities = productRepository.findAllActiveProducts();
        return entities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public ProductDto getProductById(Long id) {
        Optional<ProductEntity> entity = productRepository.findActiveProductById(id);
        return entity.map(this::convertToDto).orElse(null);
    }
    
    public List<ProductDto> getProductsByCategory(String category) {
        List<ProductEntity> entities;
        if ("all".equals(category)) {
            entities = productRepository.findAllActiveProducts();
        } else {
            entities = productRepository.findActiveProductsByCategory(category);
        }
        return entities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private ProductDto convertToDto(ProductEntity entity) {
        // 이미지 URL 처리 개선
        String imageUrl = getImageUrl(entity);
        String detailFile = getDetailFile(entity);
        
        return ProductDto.builder()
                .id(entity.getProductId())
                .name(entity.getProductName())
                .category(entity.getCategory())
                .price(entity.getPrice().intValue())
                .originalPrice(entity.getPrice().intValue()) // 원가 정보가 없으므로 동일하게 설정
                .image(imageUrl)
                .badge("NEW") // 기본 배지 설정
                .description(entity.getDescription())
                .detailedDescription(entity.getDescription()) // 상세 설명이 없으므로 설명과 동일하게 설정
                .stock(entity.getStock()) // 재고 정보 추가
                .detailFile(detailFile)
                .detailFileType(entity.getDetailFileType())
                .build();
    }
    
    private String getImageUrl(ProductEntity entity) {
        // 1. imageUrl이 있고 유효한 URL인 경우
        if (entity.getImageUrl() != null && !entity.getImageUrl().trim().isEmpty()) {
            return entity.getImageUrl();
        }
        
        // 2. imageData가 있는 경우 (Base64 데이터)
        if (entity.getImageData() != null && !entity.getImageData().trim().isEmpty()) {
            return entity.getImageData();
        }
        
        // 3. 기본 이미지 반환 (Unsplash의 무료 이미지)
        return getDefaultImageByCategory(entity.getCategory());
    }
    
    private String getDefaultImageByCategory(String category) {
        switch (category != null ? category.toLowerCase() : "") {
            case "equipment":
                return "https://picsum.photos/500/500?random=1";
            case "clothing":
                return "https://picsum.photos/500/500?random=2";
            case "supplement":
                return "https://picsum.photos/500/500?random=3";
            default:
                return "https://picsum.photos/500/500?random=4";
        }
    }
    
    private String getDetailFile(ProductEntity entity) {
        // 1. detailFileUrl이 있는 경우
        if (entity.getDetailFileUrl() != null && !entity.getDetailFileUrl().trim().isEmpty()) {
            return entity.getDetailFileUrl();
        }
        
        // 2. detailFileData가 있는 경우 (Base64 데이터)
        if (entity.getDetailFileData() != null && !entity.getDetailFileData().trim().isEmpty()) {
            return entity.getDetailFileData();
        }
        
        // 3. 상세정보 파일이 없는 경우
        return null;
    }
}
