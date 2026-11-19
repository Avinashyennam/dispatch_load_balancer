
package com.freightfox.dispatch_load_balancer.dto.response;

import com.freightfox.dispatch_load_balancer.entity.enums.OrderPriority;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignedOrderResponse {
    String orderId;
    Double latitude;
    Double longitude;
    String address;
    BigDecimal packageWeight;
    OrderPriority priority;
}
