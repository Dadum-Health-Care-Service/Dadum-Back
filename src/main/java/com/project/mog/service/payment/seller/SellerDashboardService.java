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

    // ===== 매출 분석 메서드들 =====
    
    /**
     * 매출 요약 데이터 조회
     */
    public Map<String, Object> getSalesSummary(Long sellerId, String period, String category) {
        Map<String, Object> summary = new HashMap<>();
        
        // 기간 설정
        LocalDateTime[] dateRange = getDateRange(period);
        LocalDateTime startDate = dateRange[0];
        LocalDateTime endDate = dateRange[1];
        
        // 해당 기간의 주문 조회
        List<OrderEntity> orders = getOrdersByPeriodAndCategory(sellerId, startDate, endDate, category);
        
        // 총 매출
        BigDecimal totalSales = orders.stream()
                .map(order -> BigDecimal.valueOf(order.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 총 주문 수
        int totalOrders = orders.size();
        
        // 평균 주문 금액
        BigDecimal averageOrderValue = totalOrders > 0 ? 
                totalSales.divide(BigDecimal.valueOf(totalOrders), 2, java.math.RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
        
        // 성장률 (임시로 0% 설정 - 실제로는 이전 기간과 비교)
        double growthRate = 0.0;
        
        summary.put("totalSales", totalSales);
        summary.put("totalOrders", totalOrders);
        summary.put("averageOrderValue", averageOrderValue);
        summary.put("growthRate", growthRate);
        
        return summary;
    }
    
    /**
     * 일별 매출 데이터 조회
     */
    public Map<String, Object> getDailySales(Long sellerId, String period, String category) {
        Map<String, Object> result = new HashMap<>();
        
        // 기간 설정
        LocalDateTime[] dateRange = getDateRange(period);
        LocalDateTime startDate = dateRange[0];
        LocalDateTime endDate = dateRange[1];
        
        // 해당 기간의 주문 조회
        List<OrderEntity> orders = getOrdersByPeriodAndCategory(sellerId, startDate, endDate, category);
        
        // 일별로 그룹화
        Map<LocalDate, List<OrderEntity>> dailyOrders = orders.stream()
                .collect(java.util.stream.Collectors.groupingBy(order -> order.getCreatedAt().toLocalDate()));
        
        // 일별 매출 데이터 생성
        List<Map<String, Object>> dailySales = new java.util.ArrayList<>();
        LocalDate currentDate = startDate.toLocalDate();
        
        while (!currentDate.isAfter(endDate.toLocalDate())) {
            List<OrderEntity> dayOrders = dailyOrders.getOrDefault(currentDate, new java.util.ArrayList<>());
            
            BigDecimal daySales = dayOrders.stream()
                    .map(order -> BigDecimal.valueOf(order.getTotalAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", currentDate.toString());
            dayData.put("sales", daySales);
            dayData.put("orders", dayOrders.size());
            
            dailySales.add(dayData);
            currentDate = currentDate.plusDays(1);
        }
        
        result.put("dailySales", dailySales);
        return result;
    }
    
    /**
     * 카테고리별 매출 데이터 조회
     */
    public Map<String, Object> getCategorySales(Long sellerId, String period, String category) {
        Map<String, Object> result = new HashMap<>();
        
        // 기간 설정
        LocalDateTime[] dateRange = getDateRange(period);
        LocalDateTime startDate = dateRange[0];
        LocalDateTime endDate = dateRange[1];
        
        // 해당 기간의 주문 조회
        List<OrderEntity> orders = getOrdersByPeriodAndCategory(sellerId, startDate, endDate, category);
        
        // 카테고리별로 그룹화
        Map<String, List<OrderEntity>> categoryOrders = orders.stream()
                .collect(java.util.stream.Collectors.groupingBy(OrderEntity::getProductCategory));
        
        // 카테고리별 매출 데이터 생성
        List<Map<String, Object>> categorySales = new java.util.ArrayList<>();
        BigDecimal totalSales = orders.stream()
                .map(order -> BigDecimal.valueOf(order.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        for (Map.Entry<String, List<OrderEntity>> entry : categoryOrders.entrySet()) {
            String categoryName = entry.getKey();
            List<OrderEntity> categoryOrderList = entry.getValue();
            
            BigDecimal categoryTotal = categoryOrderList.stream()
                    .map(order -> BigDecimal.valueOf(order.getTotalAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            double percentage = totalSales.compareTo(BigDecimal.ZERO) > 0 ? 
                    categoryTotal.divide(totalSales, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue() : 0.0;
            
            Map<String, Object> categoryData = new HashMap<>();
            categoryData.put("category", categoryName);
            categoryData.put("name", getCategoryDisplayName(categoryName));
            categoryData.put("sales", categoryTotal);
            categoryData.put("percentage", Math.round(percentage * 100.0) / 100.0);
            
            categorySales.add(categoryData);
        }
        
        result.put("categorySales", categorySales);
        return result;
    }
    
    /**
     * 인기 상품 데이터 조회
     */
    public Map<String, Object> getTopProducts(Long sellerId, String period, String category, int limit) {
        Map<String, Object> result = new HashMap<>();
        
        // 기간 설정
        LocalDateTime[] dateRange = getDateRange(period);
        LocalDateTime startDate = dateRange[0];
        LocalDateTime endDate = dateRange[1];
        
        // 해당 기간의 주문 조회
        List<OrderEntity> orders = getOrdersByPeriodAndCategory(sellerId, startDate, endDate, category);
        
        // 상품별로 그룹화
        Map<String, List<OrderEntity>> productOrders = orders.stream()
                .collect(java.util.stream.Collectors.groupingBy(OrderEntity::getProductName));
        
        // 상품별 매출 데이터 생성 및 정렬
        List<Map<String, Object>> topProducts = productOrders.entrySet().stream()
                .map(entry -> {
                    String productName = entry.getKey();
                    List<OrderEntity> productOrderList = entry.getValue();
                    
                    BigDecimal productSales = productOrderList.stream()
                            .map(order -> BigDecimal.valueOf(order.getTotalAmount()))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    Map<String, Object> productData = new HashMap<>();
                    productData.put("id", productName.hashCode()); // 임시 ID
                    productData.put("name", productName);
                    productData.put("sales", productSales);
                    productData.put("orders", productOrderList.size());
                    
                    return productData;
                })
                .sorted((a, b) -> {
                    BigDecimal salesA = (BigDecimal) a.get("sales");
                    BigDecimal salesB = (BigDecimal) b.get("sales");
                    return salesB.compareTo(salesA);
                })
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
        
        result.put("topProducts", topProducts);
        return result;
    }
    
    // ===== 헬퍼 메서드들 =====
    
    /**
     * 기간에 따른 날짜 범위 계산
     */
    private LocalDateTime[] getDateRange(String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;
        LocalDateTime endDate = now;
        
        switch (period) {
            case "7days":
                startDate = now.minusDays(7);
                break;
            case "30days":
                startDate = now.minusDays(30);
                break;
            case "90days":
                startDate = now.minusDays(90);
                break;
            case "1year":
                startDate = now.minusYears(1);
                break;
            default:
                startDate = now.minusDays(7);
        }
        
        return new LocalDateTime[]{startDate, endDate};
    }
    
    /**
     * 기간과 카테고리에 따른 주문 조회
     */
    private List<OrderEntity> getOrdersByPeriodAndCategory(Long sellerId, LocalDateTime startDate, LocalDateTime endDate, String category) {
        // 실제 구현에서는 OrderRepository에 적절한 메서드가 필요
        // 임시로 모든 주문을 조회하고 필터링
        List<OrderEntity> allOrders = orderRepository.findBySellerIdAndCreatedAtBetween(sellerId, startDate, endDate);
        
        if ("all".equals(category)) {
            return allOrders;
        } else {
            return allOrders.stream()
                    .filter(order -> category.equals(order.getProductCategory()))
                    .collect(java.util.stream.Collectors.toList());
        }
    }
    
    /**
     * 카테고리 표시명 반환
     */
    private String getCategoryDisplayName(String category) {
        switch (category) {
            case "equipment":
                return "운동기구";
            case "clothing":
                return "운동복";
            case "supplement":
                return "보충제";
            default:
                return category;
        }
    }
}
