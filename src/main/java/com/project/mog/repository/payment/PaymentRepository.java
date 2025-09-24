package com.project.mog.repository.payment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.mog.repository.users.UsersEntity;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    
    Optional<PaymentEntity> findByMerchantUid(String merchantUid);
    
    Optional<PaymentEntity> findByImpUid(String impUid);
    
    List<PaymentEntity> findByUser(UsersEntity user);
    
    List<PaymentEntity> findByPaymentStatus(String paymentStatus);
    
    List<PaymentEntity> deleteByUser(UsersEntity user);
}
