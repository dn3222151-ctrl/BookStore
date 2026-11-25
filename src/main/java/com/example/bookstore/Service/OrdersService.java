package com.example.bookstore.Service;

import com.example.bookstore.Entity.Orders;

import java.util.List;
import java.util.Map;

public interface OrdersService {

    List<Orders> getAllOrders();

    List<Orders> getOrdersByStatus(String status);

    Orders getOrderById(Integer id);

    List<Orders> getOrdersByUserId(Integer userId);

    Orders createOrder(Integer userId, String shippingAddress, Map<Integer, Integer> cartItems);

    void updateOrderStatus(Integer orderId, String newStatus);

    void cancelOrder(Integer orderId);
}

