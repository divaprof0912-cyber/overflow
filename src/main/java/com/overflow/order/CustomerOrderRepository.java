package com.overflow.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findAllByCustomer_IdOrderByCreatedAtDesc(Long customerId);
    Optional<CustomerOrder> findByIdAndCustomer_Id(Long id, Long customerId);
    List<CustomerOrder> findAllByPartner_IdOrderByCreatedAtDesc(Long partnerId);
}
