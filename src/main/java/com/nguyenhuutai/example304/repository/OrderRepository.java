package com.nguyenhuutai.example304.repository;

import com.nguyenhuutai.example304.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(Order.OrderStatus status);
    List<Order> findByTableId(Long tableId);
    List<Order> findByEmployeeId(Long employeeId);
    
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Order> findByTableIdAndStatus(Long tableId, Order.OrderStatus status);
}