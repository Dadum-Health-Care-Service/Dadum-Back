package com.project.mog.repository.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.mog.repository.users.UsersEntity;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    
    Optional<PaymentEntity> findByMerchantUid(String merchantUid);
    
    Optional<PaymentEntity> findByImpUid(String impUid);
    
    List<PaymentEntity> findByUser(UsersEntity user);
    
    List<PaymentEntity> findByPaymentStatus(String paymentStatus);
    
    List<PaymentEntity> deleteByUser(UsersEntity user);
    
    // 판매자 대시보드용 메서드
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentEntity p " +
           "WHERE p.paymentStatus = 'SUCCESS' " +
           "AND p.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalAmountByDateRange(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);
}
