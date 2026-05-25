package com.tokoonline.service;

import com.tokoonline.dto.OrderRequest;
import com.tokoonline.dto.OrderResponse;
import com.tokoonline.model.Order;
import java.util.List;

public interface OrderService {
    OrderResponse createOrder(Long userId, OrderRequest request);
    OrderResponse getOrderById(Long id);
    List<OrderResponse> getOrdersByUser(Long userId);
    List<OrderResponse> getAllOrders();
    OrderResponse updateOrderStatus(Long id, Order.OrderStatus status);
    void cancelOrder(Long id);
}
