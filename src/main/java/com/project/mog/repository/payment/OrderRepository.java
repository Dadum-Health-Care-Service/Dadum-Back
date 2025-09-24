package com.project.mog.repository.payment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.mog.repository.users.UsersEntity;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    
    Optional<OrderEntity> findByOrderNumber(String orderNumber);
    
    List<OrderEntity> findByUser(UsersEntity user);
    
    List<OrderEntity> findByOrderStatus(String orderStatus);
    
    List<OrderEntity> findByUserOrderByCreatedAtDesc(UsersEntity user);
    
    List<OrderEntity> deleteByUser(UsersEntity user);
}
