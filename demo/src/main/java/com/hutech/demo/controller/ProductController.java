package com.hutech.demo.controller;

import com.hutech.demo.model.Product;
import com.hutech.demo.service.CategoryService;
import com.hutech.demo.service.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;  // Đảm bảo bạn đã inject CategoryService


    // Display a list of all products
    @GetMapping
    public String showProductList(Model model, Authentication authentication) {
        List<Product> products;
        boolean isAdminOrEmployee = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ADMIN") || role.equals("EMPLOYEE"));

        if (isAdminOrEmployee) {
            products = productService.getAllNotDeletedProducts();

        } else {
            products = productService.getAllActiveAndNotDeletedProducts();
        }

        model.addAttribute("products", products);
        return "/products/product-list";
    }

    // For adding a new product
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());  // Load categories
        return "/products/add-product";
    }

    // Process the form for adding a new product
    @PostMapping("/add")
    public String addProduct(@Valid Product product,
                             BindingResult result,
                             @RequestParam("image") MultipartFile imageProduct,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("product", product);
            return "/products/add-product";
        }
        if (!imageProduct.isEmpty()) {
            try {
                String imageName = saveImageStatic(imageProduct);
                product.setImageProduct("/images/" +imageName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        productService.addProduct(product);
        return "redirect:/products";
    }

    private String saveImageStatic(MultipartFile image) throws IOException {
        File saveFile = new ClassPathResource("static/images").getFile();
        String fileName = UUID.randomUUID()+ "." + StringUtils.getFilenameExtension(image.getOriginalFilename());
        Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + fileName);
        Files.copy(image.getInputStream(), path);
        return fileName;
    }

    // For editing a product
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());  // Load categories
        return "/products/update-product";
    }
    // Process the form for updating a product
    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Long id,
                                @Valid Product product,
                                BindingResult result,
                                @RequestParam("image") MultipartFile imageProduct,
                                Model model) {
        if (result.hasErrors()) {
            product.setId(id); // set id to keep it in the form in case of errors
            return "/products/update-product";
        }
        if (!imageProduct.isEmpty()) {
            try {

                String imageName = saveImageStatic(imageProduct);
                product.setImageProduct("/images/" + imageName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        productService.updateProduct(product);
        return "redirect:/products";
    }

    @GetMapping("/product/{id}")
    public String getProductDetails(@PathVariable("id") Long id, Model model) {
        Optional<Product> productOptional = productService.getProductById(id);
        if (productOptional.isEmpty()) {
            return "redirect:/products"; // Redirect to products list if product not found
        }
        Product product = productOptional.get();
        model.addAttribute("product", product);
        return "products/display"; // Ensure this matches the actual template name and location
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProductById(id);
        return "redirect:/products";
    }

    @GetMapping("/toggleActive/{id}")
    public String toggleProductActive(@PathVariable("id") Long id) {
        productService.toggleProductActive(id);
        return "redirect:/products";
    }

}
