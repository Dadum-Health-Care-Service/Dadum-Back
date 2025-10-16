package com.project.mog.service.payment.seller;

import com.project.mog.repository.payment.RefundEntity;
import com.project.mog.repository.payment.RefundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RefundManagementService {

    @Autowired
    private RefundRepository refundRepository;

    /**
     * 판매자의 환불 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<RefundEntity> getRefundsBySeller(Long sellerId, Pageable pageable) {
        return refundRepository.findBySellerId(sellerId, pageable);
    }

    /**
     * 판매자의 환불 목록 조회 (전체)
     */
    @Transactional(readOnly = true)
    public List<RefundEntity> getAllRefundsBySeller(Long sellerId) {
        return refundRepository.findBySellerId(sellerId);
    }

    /**
     * 환불 상세 조회
     */
    @Transactional(readOnly = true)
    public Optional<RefundEntity> getRefundById(Long refundId, Long sellerId) {
        return refundRepository.findByRefundIdAndSellerId(refundId, sellerId);
    }

    /**
     * 상태별 환불 조회
     */
    @Transactional(readOnly = true)
    public List<RefundEntity> getRefundsByStatus(Long sellerId, RefundEntity.RefundStatus status) {
        return refundRepository.findBySellerIdAndStatus(sellerId, status);
    }

    /**
     * 환불 승인
     */
    public RefundEntity approveRefund(Long refundId, Long sellerId, String adminComment) {
        RefundEntity refund = refundRepository.findByRefundIdAndSellerId(refundId, sellerId)
                .orElseThrow(() -> new IllegalArgumentException("환불을 찾을 수 없습니다."));
        
        if (refund.getStatus() != RefundEntity.RefundStatus.PENDING) {
            throw new IllegalStateException("대기중인 환불만 승인할 수 있습니다.");
        }
        
        refund.setStatus(RefundEntity.RefundStatus.APPROVED);
        refund.setAdminComment(adminComment);
        refund.setProcessedAt(LocalDateTime.now());
        
        return refundRepository.save(refund);
    }

    /**
     * 환불 거부
     */
    public RefundEntity rejectRefund(Long refundId, Long sellerId, String adminComment) {
        RefundEntity refund = refundRepository.findByRefundIdAndSellerId(refundId, sellerId)
                .orElseThrow(() -> new IllegalArgumentException("환불을 찾을 수 없습니다."));
        
        if (refund.getStatus() != RefundEntity.RefundStatus.PENDING) {
            throw new IllegalStateException("대기중인 환불만 거부할 수 있습니다.");
        }
        
        refund.setStatus(RefundEntity.RefundStatus.REJECTED);
        refund.setAdminComment(adminComment);
        refund.setProcessedAt(LocalDateTime.now());
        
        return refundRepository.save(refund);
    }

    /**
     * 환불 처리중으로 변경
     */
    public RefundEntity processRefund(Long refundId, Long sellerId) {
        RefundEntity refund = refundRepository.findByRefundIdAndSellerId(refundId, sellerId)
                .orElseThrow(() -> new IllegalArgumentException("환불을 찾을 수 없습니다."));
        
        if (refund.getStatus() != RefundEntity.RefundStatus.PENDING) {
            throw new IllegalStateException("대기중인 환불만 처리할 수 있습니다.");
        }
        
        refund.setStatus(RefundEntity.RefundStatus.PROCESSING);
        refund.setProcessedAt(LocalDateTime.now());
        
        return refundRepository.save(refund);
    }

    /**
     * 환불 통계 조회
     */
    @Transactional(readOnly = true)
    public RefundStatistics getRefundStatistics(Long sellerId) {
        Long totalRefunds = refundRepository.countBySellerId(sellerId);
        Long pendingRefunds = refundRepository.countBySellerIdAndStatus(sellerId, RefundEntity.RefundStatus.PENDING);
        Long approvedRefunds = refundRepository.countBySellerIdAndStatus(sellerId, RefundEntity.RefundStatus.APPROVED);
        Long rejectedRefunds = refundRepository.countBySellerIdAndStatus(sellerId, RefundEntity.RefundStatus.REJECTED);
        
        return RefundStatistics.builder()
                .totalRefunds(totalRefunds)
                .pendingRefunds(pendingRefunds)
                .approvedRefunds(approvedRefunds)
                .rejectedRefunds(rejectedRefunds)
                .build();
    }

    /**
     * 환불 통계 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class RefundStatistics {
        private Long totalRefunds;
        private Long pendingRefunds;
        private Long approvedRefunds;
        private Long rejectedRefunds;
    }
}
