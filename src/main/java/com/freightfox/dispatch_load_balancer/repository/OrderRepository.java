package com.freightfox.dispatch_load_balancer.repository;

import com.freightfox.dispatch_load_balancer.entity.DeliveryOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<DeliveryOrder, Long> {
    Optional<DeliveryOrder> findByOrderId(String orderId);
    List<DeliveryOrder> findAllByOrderIdIn(List<String> orderIds);
    boolean existsByOrderId(String orderId);
}
