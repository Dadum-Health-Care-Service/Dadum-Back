package com.project.mog.service.payment.dto;

import java.time.LocalDateTime;

import com.project.mog.repository.payment.OrderEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {
    
    private Long orderId;
    private String orderNumber;
    private String productName;
    private String productCategory;
    private Integer quantity;
    private Integer totalAmount;
    private String orderStatus;
    private String shippingAddress;
    // shippingPhone은 UsersEntity.phoneNum 사용 (기본값)
    private String orderNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 사용자 정보 추가
    private String buyerName;      // UsersEntity.usersName
    private String buyerPhone;     // UsersEntity.phoneNum
    
    public static OrderResponseDto fromEntity(OrderEntity entity) {
        return OrderResponseDto.builder()
                .orderId(entity.getOrderId())
                .orderNumber(entity.getOrderNumber())
                .productName(entity.getProductName())
                .productCategory(entity.getProductCategory())
                .quantity(entity.getQuantity())
                .totalAmount(entity.getTotalAmount())
                .orderStatus(entity.getOrderStatus())
                .shippingAddress(entity.getShippingAddress())
                .orderNotes(entity.getOrderNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .buyerName(entity.getUser().getUsersName())
                .buyerPhone(entity.getUser().getPhoneNum())
                .build();
    }
}
