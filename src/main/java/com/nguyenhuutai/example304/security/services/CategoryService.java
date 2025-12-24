package com.nguyenhuutai.example304.security.services;

import com.nguyenhuutai.example304.model.Category;
import com.nguyenhuutai.example304.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category createCategory(String name, String description, String imageUrl) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setImageUrl(imageUrl);

        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, String name, String description, String imageUrl) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setName(name);
        category.setDescription(description);
        if (imageUrl != null) {
            category.setImageUrl(imageUrl);
        }

        return categoryRepository.save(category);
    }

    public boolean deleteCategory(Long id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
        
    }
}