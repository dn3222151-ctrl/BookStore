package com.example.bookstore.Service;

import com.example.bookstore.Entity.Categories;

import java.util.List;

public interface CategoriesService {

    public List<Categories> getAllCategories();

    public Categories getCategoryById(Integer id);

    public void addCategories(Categories categories);

    public void updateCategories(Categories categories);

    public void deleteCategories(Integer id);

    public List<Categories> searchByName(String name);
}
