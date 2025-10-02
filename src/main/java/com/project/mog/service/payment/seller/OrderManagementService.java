package com.project.mog.service.payment.seller;

import com.project.mog.repository.payment.OrderEntity;
import com.project.mog.repository.payment.OrderRepository;
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
public class OrderManagementService {

    @Autowired
    private OrderRepository orderRepository;

    /**
     * 판매자의 모든 주문 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<OrderEntity> getOrdersBySeller(Long sellerId, Pageable pageable) {
        return orderRepository.findBySellerId(sellerId, pageable);
    }

    /**
     * 판매자의 모든 주문 조회 (전체)
     */
    @Transactional(readOnly = true)
    public List<OrderEntity> getAllOrdersBySeller(Long sellerId) {
        return orderRepository.findBySellerId(sellerId);
    }

    /**
     * 주문 상세 조회
     */
    @Transactional(readOnly = true)
    public Optional<OrderEntity> getOrderById(Long orderId, Long sellerId) {
        return orderRepository.findByOrderId(orderId)
                .filter(order -> order.getSellerId().equals(sellerId));
    }

    /**
     * 주문 상태별 조회
     */
    @Transactional(readOnly = true)
    public List<OrderEntity> getOrdersByStatus(Long sellerId, String orderStatus) {
        return orderRepository.findBySellerIdAndOrderStatus(sellerId, orderStatus);
    }

    /**
     * 날짜 범위별 주문 조회
     */
    @Transactional(readOnly = true)
    public List<OrderEntity> getOrdersByDateRange(Long sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findBySellerIdAndCreatedAtBetween(sellerId, startDate, endDate);
    }

    /**
     * 주문 상태 업데이트
     */
    public OrderEntity updateOrderStatus(Long orderId, Long sellerId, String newStatus) {
        OrderEntity order = orderRepository.findByOrderId(orderId)
                .filter(o -> o.getSellerId().equals(sellerId))
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        order.setOrderStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        
        return orderRepository.save(order);
    }

    /**
     * 주문 배송지 업데이트
     */
    public OrderEntity updateShippingAddress(Long orderId, Long sellerId, String shippingAddress) {
        OrderEntity order = orderRepository.findByOrderId(orderId)
                .filter(o -> o.getSellerId().equals(sellerId))
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        order.setShippingAddress(shippingAddress);
        order.setUpdatedAt(LocalDateTime.now());
        
        return orderRepository.save(order);
    }

    /**
     * 주문 메모 업데이트
     */
    public OrderEntity updateOrderNotes(Long orderId, Long sellerId, String orderNotes) {
        OrderEntity order = orderRepository.findByOrderId(orderId)
                .filter(o -> o.getSellerId().equals(sellerId))
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        order.setOrderNotes(orderNotes);
        order.setUpdatedAt(LocalDateTime.now());
        
        return orderRepository.save(order);
    }

    /**
     * 주문 취소
     */
    public OrderEntity cancelOrder(Long orderId, Long sellerId, String cancelReason) {
        OrderEntity order = orderRepository.findByOrderId(orderId)
                .filter(o -> o.getSellerId().equals(sellerId))
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        // 취소 가능한 상태인지 확인
        if (!"PENDING".equals(order.getOrderStatus()) && !"CONFIRMED".equals(order.getOrderStatus())) {
            throw new IllegalStateException("취소할 수 없는 주문 상태입니다.");
        }
        
        order.setOrderStatus("CANCELLED");
        order.setOrderNotes(order.getOrderNotes() + " [취소사유: " + cancelReason + "]");
        order.setUpdatedAt(LocalDateTime.now());
        
        return orderRepository.save(order);
    }

    /**
     * 최근 주문 조회 (최근 N개)
     */
    @Transactional(readOnly = true)
    public List<OrderEntity> getRecentOrders(Long sellerId, int limit) {
        return orderRepository.findBySellerIdOrderByCreatedAtDesc(sellerId)
                .stream()
                .limit(limit)
                .toList();
    }
}
