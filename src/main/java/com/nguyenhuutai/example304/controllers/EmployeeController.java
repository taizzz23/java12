package com.nguyenhuutai.example304.controllers;

import com.nguyenhuutai.example304.model.*;
import com.nguyenhuutai.example304.security.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/employee")
@CrossOrigin(origins = "*", maxAge = 3600)
public class EmployeeController {

    private final OrderService orderService;
    private final CoffeeTableService tableService;
    private final BillService billService;
    private final ProductService productService;

    public EmployeeController(OrderService orderService, CoffeeTableService tableService,
            BillService billService, ProductService productService) {
        this.orderService = orderService;
        this.tableService = tableService;
        this.billService = billService;
        this.productService = productService;
    }

    // === GET ALL ORDERS ===
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    // === CREATE ORDER ===
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(
            @RequestParam Long tableId,
            @RequestParam Long employeeId) {
        try {
            Order order = orderService.createOrder(tableId, employeeId);

            // ✅ THÊM: Khi tạo order → đánh dấu bàn OCCUPIED
            tableService.updateTableStatus(tableId, CoffeeTable.TableStatus.OCCUPIED);

            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // === ADD ORDER ITEM ===
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @PostMapping("/orders/{orderId}/items")
    public ResponseEntity<Order> addOrderItem(
            @PathVariable Long orderId,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        try {
            Order order = orderService.addItemToOrder(orderId, productId, quantity);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // === UPDATE ORDER STATUS ===
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam Order.OrderStatus status) {
        try {
            Order order = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // === GET PENDING ORDERS ===
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @GetMapping("/orders/pending")
    public ResponseEntity<List<Order>> getPendingOrders() {
        List<Order> orders = orderService.getPendingOrders();
        return ResponseEntity.ok(orders);
    }

    // === GET ORDERS BY TABLE ===
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @GetMapping("/orders/table/{tableId}")
    public ResponseEntity<List<Order>> getOrdersByTable(@PathVariable Long tableId) {
        List<Order> orders = orderService.getOrdersByTable(tableId);
        return ResponseEntity.ok(orders);
    }

    // === TABLE MANAGEMENT ===
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @GetMapping("/tables")
    public ResponseEntity<List<CoffeeTable>> getAllTables() {
        List<CoffeeTable> tables = tableService.getAllTables();
        return ResponseEntity.ok(tables);
    }

    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    @PostMapping("/tables")
    public ResponseEntity<CoffeeTable> createTable(
            @RequestParam String name,
            @RequestParam(required = false) Integer number,
            @RequestParam(required = false) Integer capacity) {
        try {
            CoffeeTable table = new CoffeeTable();
            table.setTableName(name);
            table.setNumber(number != null ? number : 1);
            table.setCapacity(capacity != null ? capacity : 4);
            table.setStatus(CoffeeTable.TableStatus.FREE);
            return ResponseEntity.ok(tableService.createTable(table));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @PatchMapping("/tables/{tableId}/status")
    public ResponseEntity<CoffeeTable> updateTableStatus(
            @PathVariable Long tableId,
            @RequestParam CoffeeTable.TableStatus status) {
        try {
            CoffeeTable table = tableService.updateTableStatus(tableId, status);
            return ResponseEntity.ok(table);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // === PAYMENT ===
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @PostMapping("/orders/{orderId}/pay")
    public ResponseEntity<?> payOrder(
            @PathVariable Long orderId,
            @RequestParam Bill.PaymentMethod paymentMethod) {
        try {
            // 1️⃣ Tạo hóa đơn
            Bill bill = billService.createBill(orderId, paymentMethod);

            // 2️⃣ Lấy bàn từ order
            Long tableId = bill.getOrder().getTable().getId();

            // ✅ 3️⃣ GIỮ BÀN Ở TRẠNG THÁI OCCUPIED (KHÔNG ĐỔI VỀ FREE)
            tableService.updateTableStatus(tableId, CoffeeTable.TableStatus.OCCUPIED);

            return ResponseEntity.ok(bill);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Lỗi khi thanh toán: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long orderId) {
        try {
            orderService.deleteOrder(orderId);
            return ResponseEntity.ok("Đã xoá đơn hàng ID = " + orderId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi xoá đơn hàng: " + e.getMessage());
        }
    }
}
