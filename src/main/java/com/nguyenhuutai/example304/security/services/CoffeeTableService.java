package com.nguyenhuutai.example304.security.services;

import com.nguyenhuutai.example304.model.CoffeeTable;
import com.nguyenhuutai.example304.repository.CoffeeTableRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CoffeeTableService {

    private final CoffeeTableRepository tableRepository;

    public CoffeeTableService(CoffeeTableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    public List<CoffeeTable> getAllTables() {
        return tableRepository.findAll();
    }

    public List<CoffeeTable> getAvailableTables() {
        return tableRepository.findByStatus(CoffeeTable.TableStatus.FREE);
    }

    public CoffeeTable updateTableStatus(Long tableId, CoffeeTable.TableStatus status) {
        CoffeeTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));
        table.setStatus(status);
        return tableRepository.save(table);
    }

    // ✅ Thêm hàm này để tạo bàn mới
    public CoffeeTable createTable(CoffeeTable table) {
        return tableRepository.save(table);
    }
}
