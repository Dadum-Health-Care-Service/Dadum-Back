package com.project.mog.dto.shop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String category;
    private Integer price;
    private Integer originalPrice;
    private String image;
    private String badge;
    private String description;
    private String detailedDescription;
}
