package com.nguyenhuutai.example304.security.services;

import com.nguyenhuutai.example304.repository.BillRepository;
import com.nguyenhuutai.example304.repository.OrderRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportService {

    private final BillRepository billRepository;
    private final OrderRepository orderRepository;

    public ReportService(BillRepository billRepository, OrderRepository orderRepository) {
        this.billRepository = billRepository;
        this.orderRepository = orderRepository;
    }

    public Map<String, Object> getDashboardReport() {
        Map<String, Object> report = new HashMap<>();
        
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        
        // Doanh thu hôm nay
        Double todayRevenue = billRepository.getTotalRevenueByDateRange(todayStart, todayEnd);
        report.put("todayRevenue", todayRevenue != null ? todayRevenue : 0.0);
        
        // Tổng số đơn hàng hôm nay - SỬA Ở ĐÂY
        long todayOrders = orderRepository.findByCreatedAtBetween(todayStart, todayEnd).size();
        report.put("todayOrders", todayOrders);
        
        // Đơn hàng đang chờ xử lý - SỬA Ở ĐÂY
        long pendingOrders = orderRepository.findByStatus(com.nguyenhuutai.example304.model.Order.OrderStatus.PENDING).size();
        report.put("pendingOrders", pendingOrders);
        
        return report;
    }

    public Map<String, Object> getRevenueReport(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> report = new HashMap<>();
        
        Double totalRevenue = billRepository.getTotalRevenueByDateRange(startDate, endDate);
        report.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        
        // SỬA Ở ĐÂY - chuyển từ int sang long
        long totalBills = billRepository.findByIssuedAtBetween(startDate, endDate).size();
        report.put("totalBills", totalBills);
        
        return report;
    }
}