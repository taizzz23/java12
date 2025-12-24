package com.nguyenhuutai.example304.security.services;

import com.nguyenhuutai.example304.model.Promotion;
import com.nguyenhuutai.example304.model.PromotionProduct;
import com.nguyenhuutai.example304.model.Product;
import com.nguyenhuutai.example304.repository.PromotionRepository;
import com.nguyenhuutai.example304.repository.PromotionProductRepository;
import com.nguyenhuutai.example304.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionProductRepository promotionProductRepository;
    private final ProductRepository productRepository;

    public PromotionService(PromotionRepository promotionRepository,
                          PromotionProductRepository promotionProductRepository,
                          ProductRepository productRepository) {
        this.promotionRepository = promotionRepository;
        this.promotionProductRepository = promotionProductRepository;
        this.productRepository = productRepository;
    }

    public Promotion createPromotion(Promotion promotion) {
        return promotionRepository.save(promotion);
    }

    public Promotion updatePromotion(Long id, Promotion promotionDetails) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        promotion.setName(promotionDetails.getName());
        promotion.setDiscountPercentage(promotionDetails.getDiscountPercentage());
        promotion.setDiscountAmount(promotionDetails.getDiscountAmount());
        promotion.setStartDate(promotionDetails.getStartDate());
        promotion.setEndDate(promotionDetails.getEndDate());
        promotion.setIsActive(promotionDetails.getIsActive());

        return promotionRepository.save(promotion);
    }

    public boolean deletePromotion(Long id) {
        if (promotionRepository.existsById(id)) {
            // Xóa tất cả các product liên kết với promotion
            promotionProductRepository.deleteByPromotionId(id);
            promotionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean addProductToPromotion(Long promotionId, Long productId) {
        Optional<Promotion> promotion = promotionRepository.findById(promotionId);
        Optional<Product> product = productRepository.findById(productId);

        if (promotion.isPresent() && product.isPresent()) {
            // Kiểm tra xem đã tồn tại chưa
            boolean exists = promotionProductRepository.existsByPromotionIdAndProductId(promotionId, productId);
            if (!exists) {
                PromotionProduct promotionProduct = new PromotionProduct(promotion.get(), product.get());
                promotionProductRepository.save(promotionProduct);
                return true;
            }
        }
        return false;
    }

    public boolean removeProductFromPromotion(Long promotionId, Long productId) {
        promotionProductRepository.deleteByPromotionIdAndProductId(promotionId, productId);
        return true;
    }

    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    public List<Promotion> getActivePromotions() {
        return promotionRepository.findByIsActiveTrue();
    }

    public List<Promotion> getCurrentPromotions() {
        return promotionRepository.findActivePromotions(LocalDate.now());
    }

    public List<PromotionProduct> getPromotionProducts(Long promotionId) {
        return promotionProductRepository.findByPromotionId(promotionId);
    }
}