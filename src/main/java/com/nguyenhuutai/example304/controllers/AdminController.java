package com.nguyenhuutai.example304.controllers;

import com.nguyenhuutai.example304.model.*;
import com.nguyenhuutai.example304.security.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;


@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final PromotionService promotionService;
    private final ReportService reportService;
    private final FileStorageService fileStorageService;

    public AdminController(ProductService productService, CategoryService categoryService,
                          PromotionService promotionService, ReportService reportService,
                          FileStorageService fileStorageService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.promotionService = promotionService;
        this.reportService = reportService;
        this.fileStorageService = fileStorageService;
    }

    // === PRODUCT MANAGEMENT ===
    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam Double price,
            @RequestParam Long categoryId,
            @RequestParam Integer stockQuantity,
            @RequestParam(required = false) MultipartFile image) {
        try {
            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                imageUrl = fileStorageService.storeFile(image);
            }
            
            Product product = productService.createProduct(
                name, description, BigDecimal.valueOf(price), 
                categoryId, stockQuantity, imageUrl
            );
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam Double price,
            @RequestParam Long categoryId,
            @RequestParam Integer stockQuantity,
            @RequestParam(required = false) MultipartFile image) {
        try {
            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                imageUrl = fileStorageService.storeFile(image);
            }
            
            Product product = productService.updateProduct(
                id, name, description, BigDecimal.valueOf(price), 
                categoryId, stockQuantity, imageUrl
            );
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    // === CATEGORY MANAGEMENT ===
    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam(required = false) MultipartFile image) {
        try {
            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                imageUrl = fileStorageService.storeFile(image);
            }
            
            Category category = categoryService.createCategory(name, description, imageUrl);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // === PROMOTION MANAGEMENT ===
    @PostMapping("/promotions")
    public ResponseEntity<Promotion> createPromotion(@RequestBody Promotion promotion) {
        Promotion created = promotionService.createPromotion(promotion);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/promotions/{promotionId}/products/{productId}")
    public ResponseEntity<?> addProductToPromotion(
            @PathVariable Long promotionId, 
            @PathVariable Long productId) {
        boolean added = promotionService.addProductToPromotion(promotionId, productId);
        return added ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    // === REPORTS ===
    @GetMapping("/reports/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardReport() {
        Map<String, Object> report = reportService.getDashboardReport();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        // Parse dates and get report
        Map<String, Object> report = reportService.getRevenueReport(
            LocalDateTime.parse(startDate), LocalDateTime.parse(endDate)
        );
        return ResponseEntity.ok(report);
    }
    // === GET PRODUCTS ===
@GetMapping("/products")
public ResponseEntity<List<Product>> getAllProducts() {
    try {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    } catch (Exception e) {
        return ResponseEntity.badRequest().build();
    }
}

@GetMapping("/products/{id}")
public ResponseEntity<Product> getProductById(@PathVariable Long id) {
    try {
        Product product = productService.getProductById(id);
        return product != null ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
    } catch (Exception e) {
        return ResponseEntity.notFound().build();
    }
}

// === GET CATEGORIES ===
@GetMapping("/categories")
public ResponseEntity<List<Category>> getAllCategories() {
    try {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    } catch (Exception e) {
        return ResponseEntity.badRequest().build();
    }
}

@GetMapping("/categories/{id}")
public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
    try {
        Optional<Category> category = categoryService.getCategoryById(id);
        return category.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    } catch (Exception e) {
        return ResponseEntity.notFound().build();
    }
}
// === GET PROMOTIONS ===
@GetMapping("/promotions")
public ResponseEntity<List<Promotion>> getAllPromotions() {
    try {
        List<Promotion> promotions = promotionService.getAllPromotions();
        return ResponseEntity.ok(promotions);
    } catch (Exception e) {
        return ResponseEntity.badRequest().build();
    }
}
}