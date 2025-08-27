package com.project.mog.dto.shop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountInfoDto {
    private Integer deposit;
    private String coupon;
    private Integer availableDeposit;
}
