package com.nguyenhuutai.example304.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    private double amount;

    private String method; // CASH, MOMO, CREDIT
    private String status; // PENDING, SUCCESS, FAILED

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "qr_url")
    private String qrUrl;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
