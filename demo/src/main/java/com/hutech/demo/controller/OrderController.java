package com.hutech.demo.controller;

import com.hutech.demo.Config.VNPayConfig;
import com.hutech.demo.model.*;
import com.hutech.demo.repository.IUserRepository;
import com.hutech.demo.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private final OrderService orderService;
    @Autowired
    private final CartService cartService;
    @Autowired
    private final VoucherService voucherService;
    @Autowired
    private UserService userService;
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private VNPayService vnPayService;

    private Long status = Long.valueOf(0);

    public OrderController(OrderService orderService, CartService cartService, VoucherService voucherService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.voucherService = voucherService;
    }

    @GetMapping("/checkout")
    public String checkout(Model model, @RequestParam(required = false) List<Long> selectedVoucherIds) {
        List<Voucher> vouchers = voucherService.getAllVouchers();
        model.addAttribute("vouchers", vouchers);
        List<CartItem> cartItems = cartService.getCartItems();

        Map<Long, Double> categoryDiscountMap = new HashMap<>();
        if (selectedVoucherIds != null) {
            List<Voucher> selectedVouchers = voucherService.getVouchersByIds(selectedVoucherIds);
            categoryDiscountMap = selectedVouchers.stream()
                    .collect(Collectors.toMap(voucher -> voucher.getCategory().getId(), Voucher::getDiscount));
            model.addAttribute("selectedVouchers", selectedVouchers);
        }
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("categoryDiscountMap", categoryDiscountMap);
        return "/cart/checkout";
    }

    @PostMapping("/chooseVoucher")
    public String chooseVoucher(@RequestParam("categoryIds") List<Long> categoryIds, Model model) {
        List<Voucher> vouchers = voucherService.getVouchersByCategoryIds(categoryIds);
        model.addAttribute("vouchers", vouchers);
        return "cart/chooseVoucher";
    }

    @PostMapping("/checkout")
    public String applyVouchers(@RequestParam(name = "voucherIds", required = false) List<Long> voucherIds, Model model) {
        List<CartItem> cartItems = cartService.getCartItems();
        model.addAttribute("cartItems", cartItems);

        Map<Long, Double> categoryDiscountMap = new HashMap<>();
        List<Voucher> selectedVouchers = new ArrayList<>();
        if (voucherIds != null) {
            selectedVouchers = voucherService.getVouchersByIds(voucherIds);
            categoryDiscountMap = selectedVouchers.stream()
                    .collect(Collectors.toMap(voucher -> voucher.getCategory().getId(), Voucher::getDiscount));

            for (Voucher voucher : selectedVouchers) {
                if ("Available".equals(voucher.getStatus()) && voucher.getAmount() > 0) {
                    voucher.setAmount(voucher.getAmount() - 1);
                    voucherService.save(voucher);  // Save the updated voucher
                }
            }
        }

        model.addAttribute("selectedVouchers", selectedVouchers);
        model.addAttribute("categoryDiscountMap", categoryDiscountMap);
        return "cart/checkout";
    }

    @GetMapping
    public String listorder(Model model) {
        List<Order> orders = orderService.getAllOrder();
        model.addAttribute("orders", orders);
        return "/orders/orders-list";
    }

    @PostMapping("/submit")
    public String submitOrder(String note, String address, String thanhtoan, @RequestParam(name = "voucherIds", required = false) List<Long> voucherIds, HttpServletRequest request) {
        List<CartItem> cartItems = cartService.getCartItems();
        if (cartItems.isEmpty()) {
            return "redirect:/cart"; // Redirect if cart is empty
        }

        // Apply the voucher discounts if any are available
        Map<Long, Double> discountedPrices = new HashMap<>();
        if (voucherIds != null) {
            Map<Long, Voucher> selectedVouchers = new HashMap<>();
            for (Long voucherId : voucherIds) {
                Optional<Voucher> voucherOpt = voucherService.getVoucherById(voucherId);
                voucherOpt.ifPresent(voucher -> {
                    if ("Available".equals(voucher.getStatus())) {
                        Long categoryId = voucher.getCategory().getId();
                        if (!selectedVouchers.containsKey(categoryId)) {
                            selectedVouchers.put(categoryId, voucher);
                        }
                    }
                });
            }

            // Apply the discounts to CartItems
            for (CartItem item : cartItems) {
                Long categoryId = item.getProduct().getCategory().getId();
                double discountedPrice = item.getProduct().getPrice();
                if (selectedVouchers.containsKey(categoryId)) {
                    Voucher voucher = selectedVouchers.get(categoryId);
                    double discount = voucher.getDiscount() / 100.0;
                    discountedPrice = item.getProduct().getPrice() * (1 - discount);
                    System.out.println("Discount applied: " + discount + " - New discounted price: " + discountedPrice);
                }
                discountedPrices.put(item.getProduct().getId(), discountedPrice);
            }
        }

        if (Objects.equals(thanhtoan, "VNPay")) {
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            Order or = orderService.createOrder(cartItems, note, address, userService.getNumberUser(), userService.getEmailUser(), thanhtoan, userService.getUserId(), discountedPrices);
            status = or.getId();
            emailService.sendEmail(userService.getEmailUser(), "xin chao");
            return "redirect:" + orderService.getVnp_PayUrl(baseUrl, VNPayConfig.getIpAddress(request));
        } else {
            Order or = orderService.createOrder(cartItems, note, address, userService.getNumberUser(), userService.getEmailUser(), thanhtoan, userService.getUserId(), discountedPrices);
            return "redirect:/order/confirmation";
        }
    }


    @GetMapping("/confirmation")
    public String orderConfirmation(Model model) {
        model.addAttribute("message", "Your order has been successfully placed.");
        return "cart/order-confirmation";
    }

    @GetMapping("/vnpay-payment")
    public String GetMapping(HttpServletRequest request, Model model) {
        try {
            int paymentStatus = orderService.orderReturn(request);

            String orderInfo = request.getParameter("vnp_OrderInfo");
            String paymentTime = request.getParameter("vnp_PayDate");
            String transactionId = request.getParameter("vnp_TransactionNo");
            String totalPrice = request.getParameter("vnp_Amount");

            model.addAttribute("orderId", orderInfo);
            model.addAttribute("totalPrice", totalPrice);
            model.addAttribute("paymentTime", paymentTime);
            model.addAttribute("transactionId", transactionId);

            if (paymentStatus == 1) {
                return "ordersuccess";
            } else {
                orderService.deleteOrder(status);
                return "orderfail";
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Add proper logging here
            return "redirect:/error";
        }
    }

    @GetMapping("/detail/{id}")
    public String OrderDetail(@PathVariable Long id, Model model) {
        List<OrderDetails> orders = orderService.getOrderDetialById(id);
        model.addAttribute("orderdetial", orders);
        return "/orders/order-detail";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/revenue")
    public String getRevenueStatistics(Model model) {
        List<Order> orders = orderService.getAllOrders(); // Ensure this method returns all orders
        double totalRevenue = orders.stream()
                .flatMap(order -> order.getOrderDetails().stream())
                .mapToDouble(detail -> detail.getQuantity() * detail.getProduct().getPrice())
                .sum();
        model.addAttribute("orders", orders);
        model.addAttribute("totalRevenue", totalRevenue);
        return "revenueStatistic/revenue";
    }
}
