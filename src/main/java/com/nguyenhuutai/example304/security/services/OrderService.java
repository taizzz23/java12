package com.nguyenhuutai.example304.security.services;

import com.nguyenhuutai.example304.model.*;
import com.nguyenhuutai.example304.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CoffeeTableRepository tableRepository;
    private final ProductRepository productRepository;
    private final WebSocketService webSocketService;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
            CoffeeTableRepository tableRepository, ProductRepository productRepository,
            WebSocketService webSocketService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.tableRepository = tableRepository;
        this.productRepository = productRepository;
        this.webSocketService = webSocketService;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order createOrder(Long tableId, Long employeeId) {
        CoffeeTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));
        Order order = new Order(table, employeeId);
        Order savedOrder = orderRepository.save(order);

        table.setStatus(CoffeeTable.TableStatus.OCCUPIED);
        tableRepository.save(table);

        webSocketService.notifyTableStatus(tableId, "OCCUPIED");
        return savedOrder;
    }

    public Order addItemToOrder(Long orderId, Long productId, Integer quantity) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }

        OrderItem orderItem = new OrderItem(order, product, quantity, product.getPrice());
        orderItemRepository.save(orderItem);

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        updateOrderTotal(orderId);
        webSocketService.notifyOrderUpdate(order);

        return orderRepository.findById(orderId).orElse(null);
    }

    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        webSocketService.notifyOrderUpdate(updatedOrder);
        return updatedOrder;
    }

    public void updateOrderTotal(Long orderId) {
        Double total = orderItemRepository.getTotalAmountByOrderId(orderId);
        if (total != null) {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                order.setTotalAmount(BigDecimal.valueOf(total));
                orderRepository.save(order);
            }
        }
    }

    public List<Order> getPendingOrders() {
        return orderRepository.findByStatus(Order.OrderStatus.PENDING);
    }

    public List<Order> getOrdersByTable(Long tableId) {
        return orderRepository.findByTableId(tableId);
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Long tableId = order.getTable().getId();

        // Xóa order items trước
        orderItemRepository.deleteByOrderId(id);

        // Xóa order
        orderRepository.deleteById(id);

        // Cập nhật trạng thái bàn thành FREE
        CoffeeTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));

        table.setStatus(CoffeeTable.TableStatus.FREE);
        tableRepository.save(table);

        // Gửi WebSocket update cho FE
        webSocketService.notifyTableStatus(tableId, "FREE");
    }

    public Order payOrder(Long orderId, String paymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 1) Cập nhật trạng thái đơn
        order.setStatus(Order.OrderStatus.PAID);
        order.setPaymentMethod(paymentMethod);
        Order paidOrder = orderRepository.save(order);

        // 2) KHÔNG SET FREE — theo yêu cầu của bạn
        CoffeeTable table = order.getTable();
        table.setStatus(CoffeeTable.TableStatus.OCCUPIED); // ❗ giữ OCCUPIED
        tableRepository.save(table);

        // 3) Gửi WebSocket về FE
        webSocketService.notifyOrderUpdate(paidOrder);
        webSocketService.notifyTableStatus(table.getId(), "OCCUPIED");

        return paidOrder;
    }

}
