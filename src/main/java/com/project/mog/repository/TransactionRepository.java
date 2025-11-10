package com.project.mog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.mog.repository.transaction.Transaction;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // 거래 ID로 찾기
    Transaction findByTransactionId(String transactionId);
    
    // 사용자별 거래 목록
    List<Transaction> findByUserIdOrderByCreatedAtDesc(String userId);
    
    // 사용자별 거래 목록 (페이징)
    Page<Transaction> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    // 이상거래만 조회
    List<Transaction> findByIsAnomalyTrueOrderByCreatedAtDesc();
    
    // 이상거래만 조회 (페이징)
    Page<Transaction> findByIsAnomalyTrueOrderByCreatedAtDesc(Pageable pageable);
    
    // 위험도별 조회
    List<Transaction> findByRiskScoreGreaterThanEqualOrderByRiskScoreDesc(Double riskScore);
    
    // 날짜 범위별 조회
    List<Transaction> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    // 사용자별 날짜 범위 조회
    List<Transaction> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        String userId, LocalDateTime startDate, LocalDateTime endDate);
    
    // 사용자별 이상거래 조회 (페이징)
    Page<Transaction> findByUserIdAndIsAnomalyTrueOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    // 사용자별 정상거래 조회 (페이징)
    Page<Transaction> findByUserIdAndIsAnomalyFalseOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    // 정상거래만 조회 (페이징)
    Page<Transaction> findByIsAnomalyFalseOrderByCreatedAtDesc(Pageable pageable);
    
    // 모든 거래 조회 (생성일 기준 내림차순)
    Page<Transaction> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // 통계 쿼리들
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.isAnomaly = true")
    Long countAnomalies();
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.isAnomaly = false")
    Long countNormalTransactions();
    
    @Query("SELECT AVG(t.riskScore) FROM Transaction t WHERE t.riskScore IS NOT NULL")
    Double getAverageRiskScore();
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.userId = :userId AND t.isAnomaly = true")
    Long countUserAnomalies(@Param("userId") String userId);
    
    @Query("SELECT t FROM Transaction t WHERE t.riskScore >= :minRisk ORDER BY t.riskScore DESC")
    List<Transaction> findHighRiskTransactions(@Param("minRisk") Double minRisk);
    
    // 시간대별 통계
    @Query("SELECT t.hour, COUNT(t) FROM Transaction t GROUP BY t.hour ORDER BY t.hour")
    List<Object[]> getTransactionCountByHour();
    
    // 요일별 통계
    @Query("SELECT t.dayOfWeek, COUNT(t) FROM Transaction t GROUP BY t.dayOfWeek ORDER BY t.dayOfWeek")
    List<Object[]> getTransactionCountByDayOfWeek();
    
    // 위험도 분포
    @Query("SELECT " +
           "CASE " +
           "WHEN t.riskScore >= 80 THEN 'HIGH' " +
           "WHEN t.riskScore >= 60 THEN 'MEDIUM' " +
           "WHEN t.riskScore >= 40 THEN 'LOW' " +
           "ELSE 'SAFE' " +
           "END, COUNT(t) " +
           "FROM Transaction t " +
           "WHERE t.riskScore IS NOT NULL " +
           "GROUP BY " +
           "CASE " +
           "WHEN t.riskScore >= 80 THEN 'HIGH' " +
           "WHEN t.riskScore >= 60 THEN 'MEDIUM' " +
           "WHEN t.riskScore >= 40 THEN 'LOW' " +
           "ELSE 'SAFE' " +
           "END")
    List<Object[]> getRiskDistribution();
    
    // 이상거래 개수 조회
    Long countByIsAnomalyTrue();
    
    // 정상거래 개수 조회
    Long countByIsAnomalyFalse();
}
