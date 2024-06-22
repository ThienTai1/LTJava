package com.hutech.demo.controller;

import com.hutech.demo.model.CartItem;
import com.hutech.demo.model.Order;
import com.hutech.demo.service.CartService;
import com.hutech.demo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private final OrderService orderService;
    @Autowired
    private final CartService cartService;

    public OrderController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @GetMapping("/checkout")
    public String checkout() {
        return "/cart/checkout";
    }

    @PostMapping("/submit")
    public String submitOrder(String customerName) {
        List<CartItem> cartItems = cartService.getCartItems();
        if (cartItems.isEmpty()) {
            return "redirect:/cart"; // Redirect if cart is empty
        }
        orderService.createOrder(customerName, cartItems);
        return "redirect:/order/confirmation";
    }

    @GetMapping("/confirmation")
    public String orderConfirmation(Model model) {
        model.addAttribute("message", "Your order has been successfully placed.");
        return "cart/order-confirmation";
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
