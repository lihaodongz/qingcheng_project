package com.qingcheng.pojo.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Orders {
    private Order order;
    private List<OrderItem> orderItems;
}
