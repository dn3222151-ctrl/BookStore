package com.example.bookstore.Request;


import com.example.bookstore.Entity.Categories;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest {

    private int id;

    private int categoryId;

    private String title;

    private String author;

    private double price;

    private int quantity;

    private String description;

    private String imageUrl;

    private Categories categories;
}
