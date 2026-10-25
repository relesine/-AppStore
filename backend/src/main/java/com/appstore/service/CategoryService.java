package com.appstore.service;

import com.appstore.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    List<Category> getSubCategories(Long parentId);
    void addCategory(Category category);
    void updateCategory(Category category);
    void deleteCategory(Long id);
}
