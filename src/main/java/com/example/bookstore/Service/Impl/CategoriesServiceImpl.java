package com.example.bookstore.Service.Impl;

import com.example.bookstore.Entity.Categories;
import com.example.bookstore.Repository.CategoriesRepository;
import com.example.bookstore.Service.CategoriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CategoriesServiceImpl implements CategoriesService {

    @Autowired
    CategoriesRepository categoriesRepository;

    @Override
    public List<Categories> getAllCategories() {
        return categoriesRepository.findAll();
    }

    @Override
    public Categories getCategoryById(Integer id) {
        return categoriesRepository.findById(id).orElse(null);
    }

    @Override
    public void addCategories(Categories categories) {
        categoriesRepository.save(categories);
    }

    @Override
    public void updateCategories(Categories categories) {
        categoriesRepository.save(categories);
    }

    @Override
    public void deleteCategories(Integer id) {
        categoriesRepository.deleteById(id);
    }

    @Override
    public List<Categories> searchByName(String name) {
        return categoriesRepository.findByNameContainingIgnoreCase(name);
    }
}
