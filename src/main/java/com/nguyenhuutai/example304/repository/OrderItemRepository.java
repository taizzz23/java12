package com.nguyenhuutai.example304.repository;

import org.springframework.transaction.annotation.Transactional;

import com.nguyenhuutai.example304.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
    List<OrderItem> findOrderItemsByOrderId(Long orderId);

    @Query("SELECT SUM(oi.subtotal) FROM OrderItem oi WHERE oi.order.id = :orderId")
    Double getTotalAmountByOrderId(Long orderId);

    @Modifying
    @Transactional
    @Query("DELETE FROM OrderItem oi WHERE oi.order.id = :orderId")

    void deleteByOrderId(Long orderId);
}