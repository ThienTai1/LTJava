package com.hutech.demo.repository;

import com.hutech.demo.model.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetails, Long> {
    List<OrderDetails> findByOrderId(Long orderId);

    void deleteByOrderId(Long orderId);
}
