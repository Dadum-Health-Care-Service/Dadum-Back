package com.project.mog.dto.shop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPageDto {
    private ProductDto product;
    private OrdererInfoDto ordererInfo;
    private DeliveryInfoDto deliveryInfo;
    private DiscountInfoDto discountInfo;
}
