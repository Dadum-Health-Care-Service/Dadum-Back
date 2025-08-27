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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PAYMENTS")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PAYMENT_ID")
    private Long paymentId;
    
    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private UsersEntity user;
    
    @Column(name = "MERCHANT_UID", nullable = false, unique = true)
    private String merchantUid;
    
    @Column(name = "IMP_UID", nullable = false)
    private String impUid;
    
    @Column(name = "PRODUCT_NAME", nullable = false)
    private String productName;
    
    @Column(name = "AMOUNT", nullable = false)
    private Integer amount;
    
    @Column(name = "PAYMENT_METHOD", nullable = false)
    private String paymentMethod;
    
    @Column(name = "PAYMENT_STATUS", nullable = false)
    private String paymentStatus; // SUCCESS, FAILED, CANCELLED
    
    // 사용자 정보는 UsersEntity 참조로 대체 (중복 제거)
    // @Column(name = "BUYER_EMAIL") - UsersEntity.email 사용
    // @Column(name = "BUYER_NAME") - UsersEntity.usersName 사용  
    // @Column(name = "BUYER_TEL") - UsersEntity.phoneNum 사용
    
    @Column(name = "FAIL_REASON")
    private String failReason;
    
    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
