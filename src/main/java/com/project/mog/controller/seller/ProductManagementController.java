package com.project.mog.controller.seller;

import com.project.mog.repository.shop.ProductEntity;
import com.project.mog.repository.users.UsersRepository;
import com.project.mog.security.jwt.JwtUtil;
import com.project.mog.service.payment.seller.ProductManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/seller/products")
@Tag(name = "상품 관리", description = "판매자 상품 관리 관련 API")
public class ProductManagementController {

    @Autowired
    private ProductManagementService productManagementService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UsersRepository usersRepository;

    @Operation(summary = "상품 목록 조회", description = "판매자의 상품 목록을 페이징으로 조회합니다.")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getProducts(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            
            // 사용자 이메일로 판매자 ID 조회
            Long sellerId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ProductEntity> products = productManagementService.getProductsBySeller(sellerId, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("products", products.getContent());
            response.put("totalPages", products.getTotalPages());
            response.put("currentPage", products.getNumber());
            response.put("totalElements", products.getTotalElements());
            response.put("size", products.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("상품 목록 조회 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보를 조회합니다.")
    @GetMapping("/{productId}")
    public ResponseEntity<ProductEntity> getProduct(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @PathVariable Long productId) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            
            Long sellerId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            
            Optional<ProductEntity> product = productManagementService.getProductById(productId, sellerId);
            return product.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "상품 등록", description = "새로운 상품을 등록합니다.")
    @PostMapping
    public ResponseEntity<ProductEntity> createProduct(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> productData) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            
            Long sellerId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            
            String productName = (String) productData.get("name");
            String description = (String) productData.get("description");
            BigDecimal price = new BigDecimal(productData.get("price").toString());
            Integer stock = Integer.valueOf(productData.get("stock").toString());
            String category = (String) productData.get("category");
            String imageUrl = (String) productData.get("imageUrl");
            String imageData = (String) productData.get("imageData");
            
            ProductEntity product = productManagementService.createProduct(
                sellerId, productName, description, price, stock, category, imageUrl, imageData);
            
            return ResponseEntity.ok(product);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "상품 수정", description = "기존 상품 정보를 수정합니다.")
    @PutMapping("/{productId}")
    public ResponseEntity<ProductEntity> updateProduct(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @PathVariable Long productId,
            @RequestBody Map<String, Object> productData) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            
            Long sellerId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            
            String productName = (String) productData.get("name");
            String description = (String) productData.get("description");
            BigDecimal price = new BigDecimal(productData.get("price").toString());
            Integer stock = Integer.valueOf(productData.get("stock").toString());
            String category = (String) productData.get("category");
            String imageUrl = (String) productData.get("imageUrl");
            String imageData = (String) productData.get("imageData");
            Boolean isActive = Boolean.valueOf(productData.get("isActive").toString());
            
            ProductEntity product = productManagementService.updateProduct(
                productId, sellerId, productName, description, price, stock, category, imageUrl, imageData, isActive);
            
            return ResponseEntity.ok(product);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다 (소프트 삭제).")
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @PathVariable Long productId) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            
            Long sellerId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            
            productManagementService.deleteProduct(productId, sellerId);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "재고 수정", description = "상품의 재고를 수정합니다.")
    @PutMapping("/{productId}/stock")
    public ResponseEntity<ProductEntity> updateStock(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @PathVariable Long productId,
            @RequestBody Map<String, Object> stockData) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            
            Long sellerId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            Integer newStock = Integer.valueOf(stockData.get("stock").toString());
            
            ProductEntity product = productManagementService.updateStock(productId, sellerId, newStock);
            return ResponseEntity.ok(product);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "카테고리별 상품 조회", description = "특정 카테고리의 상품을 조회합니다.")
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductEntity>> getProductsByCategory(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @PathVariable String category) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            
            Long sellerId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            
            List<ProductEntity> products = productManagementService.getProductsByCategory(sellerId, category);
            return ResponseEntity.ok(products);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "상품 검색", description = "상품명으로 상품을 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<List<ProductEntity>> searchProducts(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @RequestParam String keyword) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUserEmail(token);
            
            Long sellerId = usersRepository.findByEmail(userEmail)
                    .map(user -> user.getUsersId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
            
            List<ProductEntity> products = productManagementService.searchProducts(sellerId, keyword);
            return ResponseEntity.ok(products);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "모든 활성 상품 조회 (Shop용)", description = "Shop에서 표시할 모든 활성 상품을 조회합니다.")
    @GetMapping("/public/active")
    public ResponseEntity<List<ProductEntity>> getAllActiveProducts() {
        try {
            List<ProductEntity> products = productManagementService.getAllActiveProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
