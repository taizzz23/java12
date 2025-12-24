package com.nguyenhuutai.example304.controllers;

import com.nguyenhuutai.example304.model.*;
import com.nguyenhuutai.example304.security.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CustomerController {

    private final OrderService orderService;
    private final ProductService productService;
    private final CoffeeTableService tableService;
    private final WebSocketService webSocketService;

    public CustomerController(OrderService orderService, ProductService productService,
                            CoffeeTableService tableService, WebSocketService webSocketService) {
        this.orderService = orderService;
        this.productService = productService;
        this.tableService = tableService;
        this.webSocketService = webSocketService;
    }

    // === MENU ===
    @GetMapping("/menu")
    public ResponseEntity<List<Product>> getMenu() {
        List<Product> products = productService.getAvailableProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        List<Category> categories = productService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    // === ORDER CREATION ===
    @PostMapping("/orders")
    public ResponseEntity<Order> createCustomerOrder(
            @RequestParam Long tableId,
            @RequestBody List<OrderItemRequest> items) {
        try {
            // For customer orders, we use a special employee ID or null
            Order order = orderService.createOrder(tableId, null);
            
            // Add items to order
            for (OrderItemRequest item : items) {
                orderService.addItemToOrder(order.getId(), item.getProductId(), item.getQuantity());
            }
            
            // Notify employees via WebSocket
            webSocketService.notifyNewOrder(order);
            
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // === TABLE STATUS ===
    @GetMapping("/tables/available")
    public ResponseEntity<List<CoffeeTable>> getAvailableTables() {
        List<CoffeeTable> tables = tableService.getAvailableTables();
        return ResponseEntity.ok(tables);
    }

    // DTO for order item request
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;

        // Getters and Setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}