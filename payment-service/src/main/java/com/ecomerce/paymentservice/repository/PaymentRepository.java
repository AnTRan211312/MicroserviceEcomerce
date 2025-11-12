package com.ecomerce.paymentservice.repository;

import com.ecomerce.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {
    List<Payment> findByUserId(Long userId);
    List<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByVnpayTxnRef(String vnpayTxnRef);
    Optional<Payment> findByVnpayTransactionNo(String vnpayTransactionNo);
}

