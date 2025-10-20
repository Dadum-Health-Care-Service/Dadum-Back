package com.project.mog.dto.shop;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상품 DTO")
public class ProductDto {
    @Schema(hidden = true)
    private Long id;
    private String name;
    private String category;
    private Integer price;
    private Integer originalPrice;
    private String image;
    private String badge;
    private String description;
    private String detailedDescription;
    private Integer stock;
}
