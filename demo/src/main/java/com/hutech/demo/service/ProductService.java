package com.hutech.demo.service;

import com.hutech.demo.model.Product;
import com.hutech.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    private final ProductRepository productRepository;

    // Retrieve all products that are not soft deleted
    public List<Product> getAllNotDeletedProducts() {
        return productRepository.findAllNotDeleted();
    }

    // Retrieve all products that are active and not deleted
    public List<Product> getAllActiveAndNotDeletedProducts() {
        return productRepository.findAllActiveAndNotDeleted();
    }

    // Retrieve all soft deleted products
    public List<Product> getAllDeletedProducts() {
        return productRepository.findAllDeleted();
    }

    // Retrieve a product by its id
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // Add a new product to the database
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    // Update an existing product
    public Product updateProduct(@NotNull Product product) {
        Product existingProduct = productRepository.findById(product.getId())
                .orElseThrow(() -> new IllegalStateException("Product with ID " + product.getId() + " does not exist."));
        existingProduct.setName(product.getName());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setQuantity(product.getQuantity());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setImageProduct(product.getImageProduct());
        return productRepository.save(existingProduct);
    }

    // Soft delete a product by its id
    public void deleteProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Product with ID " + id + " does not exist."));
        product.setIsDelete(true);
        productRepository.save(product);
    }

    // Active - Deactive Product
    public void toggleProductActive(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new IllegalStateException("Product not found"));
        product.setIsActive(!product.getIsActive());
        productRepository.save(product);
    }

}
