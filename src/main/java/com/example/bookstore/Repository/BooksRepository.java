package com.example.bookstore.Repository;

import com.example.bookstore.Entity.Books;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BooksRepository extends JpaRepository<Books, Integer> {
    List<Books> findByTitleContainingIgnoreCase(String title);
    List<Books> findByCategoriesId(Integer categoryId);
    List<Books> findByTitleContainingIgnoreCaseAndCategoriesId(String title, Integer categoryId);
}
