package com.example.bookstore.Response;

import com.example.bookstore.Entity.Books;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private Books book;
    private int quantity;

    public double getSubtotal() {
        return book != null ? book.getPrice() * quantity : 0.0;
    }
}

