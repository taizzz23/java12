package com.nguyenhuutai.example304.repository;

import com.nguyenhuutai.example304.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
