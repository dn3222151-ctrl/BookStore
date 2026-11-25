package com.example.bookstore.Service.Impl;

import com.example.bookstore.Entity.Books;
import com.example.bookstore.Entity.Categories;
import com.example.bookstore.Repository.BooksRepository;
import com.example.bookstore.Repository.CategoriesRepository;
import com.example.bookstore.Request.BookRequest;
import com.example.bookstore.Response.BooksReponse;
import com.example.bookstore.Service.BooksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BooksServiceImpl implements BooksService {

    @Autowired
    private BooksRepository booksRepository;

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Override
    public List<BooksReponse> getAllBooks() {
        return booksRepository.findAll().stream().map(BooksReponse::new).toList();
    }

    @Override
    public Books getById(Integer id) {
        return booksRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void addBooks(BookRequest bookRequest) {
        Books book = new Books();
        book.setTitle(bookRequest.getTitle());
        book.setAuthor(bookRequest.getAuthor());
        book.setPrice(bookRequest.getPrice());
        book.setQuantity(bookRequest.getQuantity());
        book.setDescription(bookRequest.getDescription());
        book.setImageUrl(bookRequest.getImageUrl());

        if (bookRequest.getCategoryId() > 0) {
            Categories category = categoriesRepository.findById(bookRequest.getCategoryId()).orElse(null);
            book.setCategories(category);
        }

        booksRepository.save(book);
    }

    @Override
    @Transactional
    public void updateBooks(BookRequest bookRequest) {
        Books book = booksRepository.findById(bookRequest.getId()).orElse(null);
        if (book != null) {
            book.setTitle(bookRequest.getTitle());
            book.setAuthor(bookRequest.getAuthor());
            book.setPrice(bookRequest.getPrice());
            book.setQuantity(bookRequest.getQuantity());
            book.setDescription(bookRequest.getDescription());
            if (bookRequest.getImageUrl() != null && !bookRequest.getImageUrl().trim().isEmpty()) {
                book.setImageUrl(bookRequest.getImageUrl());
            }

            if (bookRequest.getCategoryId() > 0) {
                Categories category = categoriesRepository.findById(bookRequest.getCategoryId()).orElse(null);
                book.setCategories(category);
            }
            booksRepository.save(book);
        }
    }

    @Override
    @Transactional
    public void deleteBooks(Integer id) {
        booksRepository.deleteById(id);
    }

    @Override
    public List<Books> searchTitle(String title) {
        return booksRepository.findByTitleContainingIgnoreCase(title);
    }

    @Override
    public List<Books> getBooksByCategory(Integer categoryId) {
        return booksRepository.findByCategoriesId(categoryId);
    }

    @Override
    public List<Books> searchAndFilter(String keyword, Integer categoryId) {
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasCategory = categoryId != null && categoryId > 0;

        if (hasKeyword && hasCategory) {
            return booksRepository.findByTitleContainingIgnoreCaseAndCategoriesId(keyword.trim(), categoryId);
        } else if (hasKeyword) {
            return booksRepository.findByTitleContainingIgnoreCase(keyword.trim());
        } else if (hasCategory) {
            return booksRepository.findByCategoriesId(categoryId);
        } else {
            return booksRepository.findAll();
        }
    }
}
