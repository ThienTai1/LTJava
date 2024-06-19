package com.hutech.demo.controller;

import com.hutech.demo.model.Voucher;
import com.hutech.demo.service.CategoryService;

import com.hutech.demo.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/vouchers")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;
    @Autowired
    private CategoryService categoryService;  // Đảm bảo bạn đã inject CategoryService
    
    
    // Display a list of all vouchers
    @GetMapping
    public String showvoucherList(Model model) {
        model.addAttribute("vouchers", voucherService.getAllVouchers());
        return "/vouchers/voucher-list";
    }

    // For adding a new voucher
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("voucher", new Voucher());
        model.addAttribute("categories", categoryService.getAllCategories());  // Load categories
        return "/vouchers/add-voucher";
    }

    @PostMapping("/add")
    public String addVoucher(@Valid Voucher voucher,
                             BindingResult result,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("voucher", voucher);
            return "/vouchers/add-voucher";
        }
        voucherService.addVoucher(voucher);
        return "redirect:/vouchers";
    }

    // For editing a voucher
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Voucher voucher = voucherService.getVoucherById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid voucher Id:" + id));
        model.addAttribute("voucher", voucher);
        model.addAttribute("categories", categoryService.getAllCategories());  // Load categories
        return "/vouchers/update-voucher";
    }
    
    // Process the form for updating a voucher
    @PostMapping("/update/{id}")
    public String updateVoucher(@PathVariable Long id,
                                @Valid Voucher voucher,
                                BindingResult result,
                                Model model) {
        if (result.hasErrors()) {
            voucher.setId(id); // set id to keep it in the form in case of errors
            return "/vouchers/update-voucher";
        }
        voucherService.updateVoucher(voucher);
        return "redirect:/vouchers";
    }


    @GetMapping("/voucher/{id}")
    public String getVoucherDetails(@PathVariable("id") Long id, Model model) {
        Optional<Voucher> voucherOptional = voucherService.getVoucherById(id);
        if (voucherOptional.isEmpty()) {
            return "redirect:/vouchers"; // Redirect to vouchers list if voucher not found
        }
        Voucher voucher = voucherOptional.get();
        model.addAttribute("voucher", voucher);
        return "vouchers/display"; // Ensure this matches the actual template name and location
    }
}
