package com.example.bookstore.Repository;

import com.example.bookstore.Entity.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriesRepository extends JpaRepository<Categories, Integer> {

    public List<Categories> findByNameContainingIgnoreCase(String name);
}
