package com.example.bookstore.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private Integer userId;
    private String customerName;
    private String customerPhone;
    private String shippingAddress;
    private String notes;
}

