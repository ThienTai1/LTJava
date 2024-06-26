package com.hutech.demo.repository;

import com.hutech.demo.model.Category;
import com.hutech.demo.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {


    List<Voucher> findByCategory(Category category);

    @Query("SELECT v FROM Voucher v WHERE v.id IN :voucherIds")
    List<Voucher> findAllById(@Param("voucherIds") List<Long> voucherIds);

    @Query("SELECT v FROM Voucher v WHERE v.category.id IN :categoryIds")
    List<Voucher> findByCategoryIdIn(@Param("categoryIds") List<Long> categoryIds);
}
