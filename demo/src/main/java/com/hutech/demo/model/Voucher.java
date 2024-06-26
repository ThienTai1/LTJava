package com.hutech.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vouchers")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private Integer amount;
    private Double discount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    public String getStatus() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startDate)) {
            return "Coming Soon";
        } else if (now.isAfter(endDate)) {
            return "Out of Time";
        } else {
            return "Available";
        }
    }
}
