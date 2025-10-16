package com.project.mog.service.payment.seller;

import com.project.mog.repository.payment.OrderEntity;
import com.project.mog.repository.payment.OrderRepository;
import com.project.mog.repository.payment.PaymentRepository;
import com.project.mog.repository.shop.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SellerDashboardService {

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private ProductRepository productRepository;

    /**
     * 판매자 대시보드 통계 데이터 조회
     */
    public Map<String, Object> getDashboardStats(Long sellerId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 오늘 날짜
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        
        // 이번 달
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        LocalDateTime startOfMonth = firstDayOfMonth.atStartOfDay();
        LocalDateTime endOfMonth = lastDayOfMonth.atTime(23, 59, 59);
        
        // 오늘 주문 수
        Long todayOrders = orderRepository.countBySellerIdAndCreatedAtBetween(sellerId, startOfDay, endOfDay);
        
        // 이번 달 주문 수
        Long monthlyOrders = orderRepository.countBySellerIdAndCreatedAtBetween(sellerId, startOfMonth, endOfMonth);
        
        // 오늘 매출 (OrderEntity의 totalAmount 합계)
        List<OrderEntity> todayOrdersList = orderRepository.findBySellerIdAndCreatedAtBetween(sellerId, startOfDay, endOfDay);
        BigDecimal todayRevenue = todayOrdersList.stream()
                .map(order -> BigDecimal.valueOf(order.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 이번 달 매출
        List<OrderEntity> monthlyOrdersList = orderRepository.findBySellerIdAndCreatedAtBetween(sellerId, startOfMonth, endOfMonth);
        BigDecimal monthlyRevenue = monthlyOrdersList.stream()
                .map(order -> BigDecimal.valueOf(order.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 총 상품 수
        Long totalProducts = productRepository.countBySellerId(sellerId);
        
        // 활성 상품 수 (재고가 있는 상품)
        Long activeProducts = productRepository.countBySellerIdAndStockGreaterThan(sellerId, 0);
        
        // 대기 중인 주문 수
        Long pendingOrders = orderRepository.countBySellerIdAndOrderStatus(sellerId, "PENDING");
        
        // 완료된 주문 수
        Long completedOrders = orderRepository.countBySellerIdAndOrderStatus(sellerId, "COMPLETED");
        
        stats.put("todayOrders", todayOrders);
        stats.put("monthlyOrders", monthlyOrders);
        stats.put("todayRevenue", todayRevenue);
        stats.put("monthlyRevenue", monthlyRevenue);
        stats.put("totalProducts", totalProducts);
        stats.put("activeProducts", activeProducts);
        stats.put("pendingOrders", pendingOrders);
        stats.put("completedOrders", completedOrders);
        
        return stats;
    }
}
