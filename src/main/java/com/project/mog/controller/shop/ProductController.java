package com.project.mog.controller.shop;

import com.project.mog.dto.shop.ProductDto;
import com.project.mog.service.shop.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/shop/products")
@RequiredArgsConstructor
@Tag(name = "상품 관리", description = "상품 관련 API")
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping
    @Operation(summary = "상품 목록 조회", description = "카테고리별 상품 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<List<ProductDto>> getAllProducts(
            @Parameter(description = "카테고리", example = "all") @RequestParam(required = false, defaultValue = "all") String category) {
        
        List<ProductDto> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    public ResponseEntity<ProductDto> getProductById(@Parameter(description = "상품 ID", example = "1") @PathVariable Long id) {
        ProductDto product = productService.getProductById(id);
        
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(product);
    }
}
