package com.hutech.demo.service;

import com.hutech.demo.model.Voucher;
import com.hutech.demo.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class VoucherService {
    private final VoucherRepository voucherRepository;
    // Retrieve all Vouchers from the database
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    // Retrieve a Voucher by its id
    public Optional<Voucher> getVoucherById(Long id) {
        return voucherRepository.findById(id);
    }

    // Add a new Voucher to the database
    public Voucher addVoucher(Voucher voucher) {
        return voucherRepository.save(voucher);
    }

    // Update an existing Voucher
    public Voucher updateVoucher(@NotNull Voucher Voucher) {
        Voucher existingVoucher = voucherRepository.findById(Voucher.getId())
                .orElseThrow(() -> new IllegalStateException("Voucher with ID " + Voucher.getId() + " does not exist."));
        existingVoucher.setCode(Voucher.getCode());
        existingVoucher.setAmount(Voucher.getAmount());
        existingVoucher.setDiscount(Voucher.getDiscount());
        existingVoucher.setStartDate(Voucher.getStartDate());
        existingVoucher.setEndDate(Voucher.getEndDate());
        existingVoucher.setCategory(Voucher.getCategory());
        return voucherRepository.save(existingVoucher);
    }

    // Delete a Voucher by its id
    public void deleteVoucherById(Long id) {
        if (!voucherRepository.existsById(id)) {
            throw new IllegalStateException("Voucher with ID " + id + " does not exist.");
        }
        voucherRepository.deleteById(id);
    }
}
