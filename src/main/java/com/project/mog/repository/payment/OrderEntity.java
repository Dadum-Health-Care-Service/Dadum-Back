package com.project.mog.repository.payment;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.project.mog.repository.users.UsersEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ORDERS")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ORDER_ID")
    private Long orderId;
    
    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private UsersEntity user;
    
    @OneToOne
    @JoinColumn(name = "PAYMENT_ID")
    private PaymentEntity payment;
    
    @Column(name = "ORDER_NUMBER", nullable = false, unique = true)
    private String orderNumber;
    
    @Column(name = "PRODUCT_NAME", nullable = false)
    private String productName;
    
    @Column(name = "PRODUCT_CATEGORY")
    private String productCategory;
    
    @Column(name = "QUANTITY", nullable = false)
    private Integer quantity;
    
    @Column(name = "TOTAL_AMOUNT", nullable = false)
    private Integer totalAmount;
    
    @Column(name = "ORDER_STATUS", nullable = false)
    private String orderStatus; // PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    
    @Column(name = "SELLER_ID")
    private Long sellerId;
    
    @Column(name = "SHIPPING_ADDRESS")
    private String shippingAddress;
    
    // 배송 전화번호는 UsersEntity.phoneNum과 동일한 경우가 많음
    // @Column(name = "SHIPPING_PHONE") - UsersEntity.phoneNum 사용 (기본값)
    
    @Column(name = "ORDER_NOTES")
    private String orderNotes;
    
    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
}
