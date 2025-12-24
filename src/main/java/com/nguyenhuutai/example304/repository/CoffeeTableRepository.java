package com.nguyenhuutai.example304.repository;

import com.nguyenhuutai.example304.model.CoffeeTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CoffeeTableRepository extends JpaRepository<CoffeeTable, Long> {
    List<CoffeeTable> findByStatus(CoffeeTable.TableStatus status);
    CoffeeTable findByNumber(Integer number);
    List<CoffeeTable> findByCapacityGreaterThanEqual(Integer capacity);
}