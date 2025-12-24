package com.nguyenhuutai.example304.security.services;

import com.nguyenhuutai.example304.model.Product;
import com.nguyenhuutai.example304.model.Category;
import com.nguyenhuutai.example304.repository.ProductRepository;
import com.nguyenhuutai.example304.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          FileStorageService fileStorageService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.fileStorageService = fileStorageService;
    }

    // === CREATE PRODUCT ===
    public Product createProduct(String name, String description, BigDecimal price,
                               Long categoryId, Integer stockQuantity, String imageUrl) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(category);
        product.setStockQuantity(stockQuantity);
        product.setImageUrl(imageUrl);
        product.setArchive(false);

        return productRepository.save(product);
    }

    // === UPDATE PRODUCT ===
    public Product updateProduct(Long id, String name, String description, BigDecimal price,
                               Long categoryId, Integer stockQuantity, String imageUrl) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(category);
        product.setStockQuantity(stockQuantity);
        if (imageUrl != null) {
            product.setImageUrl(imageUrl);
        }

        return productRepository.save(product);
    }

    // === DELETE PRODUCT (XÓA KHỎI DB + XÓA FILE ẢNH) ===
    public boolean deleteProduct(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();

            // Xóa ảnh nếu có
            if (product.getImageUrl() != null) {
                try {
                    fileStorageService.deleteFile(product.getImageUrl());
                } catch (Exception e) {
                    System.out.println("⚠️ Không thể xóa file ảnh: " + e.getMessage());
                }
            }

            // Xóa sản phẩm khỏi database
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // === GET ALL PRODUCTS ===
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public List<Product> getAvailableProducts() {
        return productRepository.findByArchiveFalse();
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    // === CATEGORY METHODS ===
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
 