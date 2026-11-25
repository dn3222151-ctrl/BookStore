package com.example.bookstore.Service;

import com.example.bookstore.Entity.Books;
import com.example.bookstore.Request.BookRequest;
import com.example.bookstore.Response.BooksReponse;

import java.util.List;

public interface BooksService {

    List<BooksReponse> getAllBooks();

    Books getById(Integer id);

    void addBooks(BookRequest bookRequest);

    void updateBooks(BookRequest bookRequest);

    void deleteBooks(Integer id);

    List<Books> searchTitle(String title);

    List<Books> getBooksByCategory(Integer categoryId);

    List<Books> searchAndFilter(String keyword, Integer categoryId);
}
