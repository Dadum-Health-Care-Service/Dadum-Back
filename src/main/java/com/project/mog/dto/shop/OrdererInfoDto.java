package com.project.mog.dto.shop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdererInfoDto {
    private String name;
    private String email;
    private String phone;
    private String address;
    private String detailAddress;
    private String zipCode;
}
