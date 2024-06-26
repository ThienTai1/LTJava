package com.hutech.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double price;
    @Min(0)
    private int quantity;
    private String description;
    private String imageProduct;

    private Boolean isDelete = false;
    private Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}

