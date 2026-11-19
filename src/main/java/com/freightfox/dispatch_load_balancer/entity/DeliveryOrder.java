package com.freightfox.dispatch_load_balancer.entity;

import com.freightfox.dispatch_load_balancer.entity.enums.OrderPriority;
import jakarta.persistence.*;

import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "delivery_orders"
// indexes = {
// @Index(name = "idx_order_priority", columnList = "priority")
// }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor 
@Builder 
public class DeliveryOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true, length = 50)
    private String orderId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(name = "package_weight", nullable = false, precision = 12, scale = 3)
    private BigDecimal packageWeight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrderPriority priority;
}
