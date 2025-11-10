package com.project.mog.service.shop;

import com.project.mog.dto.shop.*;
import com.project.mog.repository.users.UsersEntity;
import com.project.mog.repository.users.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderPageService {
    
    private final UsersRepository usersRepository;
    private final ProductService productService;
    
    // 실제로는 데이터베이스에서 가져올 예정
    public OrderPageDto getOrderPageData(Long productId, String userId) {
        // 상품 정보 (ProductService에서 가져오기)
        ProductDto product = productService.getProductById(productId);
        
        // 주문자 정보 (실제 회원 정보에서 가져오기)
        OrdererInfoDto ordererInfo = getOrdererInfo(userId);
        
        // 배송 정보 (실제 회원 정보를 기반으로 설정)
        DeliveryInfoDto deliveryInfo = getDefaultDeliveryInfo(userId);
        
        // 할인 정보 (실제로는 사용자의 예치금, 쿠폰 정보에서 가져올 예정)
        DiscountInfoDto discountInfo = getDiscountInfo(userId);
        
        return OrderPageDto.builder()
                .product(product)
                .ordererInfo(ordererInfo)
                .deliveryInfo(deliveryInfo)
                .discountInfo(discountInfo)
                .build();
    }
    
    private OrdererInfoDto getOrdererInfo(String userId) {
        try {
            // userId가 숫자인 경우 (usersId)
            if (userId.matches("\\d+")) {
                Long usersId = Long.parseLong(userId);
                UsersEntity user = usersRepository.findById(usersId).orElse(null);
                
                if (user != null) {
                    return OrdererInfoDto.builder()
                            .name(user.getUsersName())
                            .email(user.getEmail())
                            .phone(user.getPhoneNum())
                            .address("") // 실제로는 별도 주소 테이블에서 가져와야 함
                            .detailAddress("")
                            .zipCode("")
                            .build();
                }
            } else {
                // userId가 이메일인 경우
                UsersEntity user = usersRepository.findByEmail(userId).orElse(null);
                
                if (user != null) {
                    return OrdererInfoDto.builder()
                            .name(user.getUsersName())
                            .email(user.getEmail())
                            .phone(user.getPhoneNum())
                            .address("") // 실제로는 별도 주소 테이블에서 가져와야 함
                            .detailAddress("")
                            .zipCode("")
                            .build();
                }
            }
        } catch (Exception e) {
            // 에러 발생 시 로그 출력
            System.err.println("회원 정보 조회 실패: " + e.getMessage());
        }
        
        // 회원 정보를 찾을 수 없는 경우 기본값 반환
        return OrdererInfoDto.builder()
                .name("")
                .email("")
                .phone("")
                .address("")
                .detailAddress("")
                .zipCode("")
                .build();
    }
    
    private DeliveryInfoDto getDefaultDeliveryInfo(String userId) {
        try {
            // 주문자 정보와 동일하게 설정 (실제로는 별도 배송지 테이블에서 가져와야 함)
            OrdererInfoDto ordererInfo = getOrdererInfo(userId);
            
            return DeliveryInfoDto.builder()
                    .name(ordererInfo.getName())
                    .contact1(ordererInfo.getPhone())
                    .contact2("")
                    .address(ordererInfo.getAddress())
                    .detailAddress(ordererInfo.getDetailAddress())
                    .zipCode(ordererInfo.getZipCode())
                    .deliveryMessage("")
                    .depositorName("")
                    .deliveryType("domestic")
                    .addressType("home")
                    .build();
        } catch (Exception e) {
            // 에러 발생 시 기본값 반환
            return DeliveryInfoDto.builder()
                    .name("")
                    .contact1("")
                    .contact2("")
                    .address("")
                    .detailAddress("")
                    .zipCode("")
                    .deliveryMessage("")
                    .depositorName("")
                    .deliveryType("domestic")
                    .addressType("home")
                    .build();
        }
    }
    
    private DiscountInfoDto getDiscountInfo(String userId) {
        // 실제로는 사용자의 예치금, 쿠폰 정보에서 가져올 예정
        // 임시로 하드코딩된 데이터 반환
        return DiscountInfoDto.builder()
                .deposit(0)
                .coupon("")
                .availableDeposit(0)
                .build();
    }
}
