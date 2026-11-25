package com.example.bookstore.Response;

import com.example.bookstore.Entity.Books;
import com.example.bookstore.Entity.Categories;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BooksReponse {

    private int id;
    private int categoryId;
    private String categoryName;
    private String title;
    private String author;
    private double price;
    private int quantity;
    private String description;
    private String imageUrl;
    private Categories categories;

    public BooksReponse(Books books) {
        if (books != null) {
            this.id = books.getId();
            this.title = books.getTitle();
            this.author = books.getAuthor();
            this.price = books.getPrice();
            this.quantity = books.getQuantity();
            this.description = books.getDescription();
            this.imageUrl = books.getImageUrl();
            this.categories = books.getCategories();
            if (books.getCategories() != null) {
                this.categoryId = books.getCategories().getId();
                this.categoryName = books.getCategories().getName();
            }
        }
    }
}
