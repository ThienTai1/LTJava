package com.hutech.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double price;
    private String address;
    private String number;
    private String email;
    private String note;
    private  String thanhtoan;
    @JoinColumn(name = "user_id")
    private String name;

    @OneToMany(mappedBy = "order")
    private List<OrderDetails> orderDetails;
}
