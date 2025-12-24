package com.nguyenhuutai.example304.repository;

import com.nguyenhuutai.example304.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByArchiveFalse();
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByStockQuantityLessThan(Integer quantity);
}