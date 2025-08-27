package com.project.mog.dto.shop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryInfoDto {
    private String name;
    private String contact1;
    private String contact2;
    private String address;
    private String detailAddress;
    private String zipCode;
    private String deliveryMessage;
    private String depositorName;
    private String deliveryType; // "domestic" or "overseas"
    private String addressType; // "home", "company", "recent", "list", "new"
}
