package com.project.mog.service.payment.dto;

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
public class PaymentRequestDto {
    
    private String merchantUid;
    private String impUid;
    private String productName;
    private Integer amount;
    private String paymentMethod;
    // 사용자 정보는 UsersEntity에서 가져오므로 제거
    // private String buyerEmail; - UsersEntity.email 사용
    // private String buyerName; - UsersEntity.usersName 사용
    // private String buyerTel; - UsersEntity.phoneNum 사용
    private String productCategory;
    private Integer quantity;
    private String shippingAddress;
    // private String shippingPhone; - UsersEntity.phoneNum 사용 (기본값)
    private String orderNotes;
}
