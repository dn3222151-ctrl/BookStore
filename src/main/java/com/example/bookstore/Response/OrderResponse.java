package com.example.bookstore.Response;

import com.example.bookstore.Entity.Orders;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private int id;
    private Integer userId;
    private String username;
    private String fullName;
    private LocalDateTime orderDate;
    private double totalAmount;
    private String status;
    private String shippingAddress;
    private int totalItems;

    public OrderResponse(Orders order) {
        if (order != null) {
            this.id = order.getId();
            if (order.getUser() != null) {
                this.userId = order.getUser().getId();
                this.username = order.getUser().getUsername();
                this.fullName = order.getUser().getFullName();
            }
            this.orderDate = order.getOrderDate();
            this.totalAmount = order.getTotalAmount();
            this.status = order.getStatus();
            this.shippingAddress = order.getShippingAddress();
            this.totalItems = order.getOrderDetails() != null ? order.getOrderDetails().size() : 0;
        }
    }
}

