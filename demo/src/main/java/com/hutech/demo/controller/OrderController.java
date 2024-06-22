package com.hutech.demo.controller;

import com.hutech.demo.Config.VNPayConfig;
import com.hutech.demo.model.CartItem;
import com.hutech.demo.model.Order;
import com.hutech.demo.model.OrderDetails;
import com.hutech.demo.repository.IUserRepository;
import com.hutech.demo.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private final OrderService orderService;
    @Autowired
    private final CartService cartService;
    @Autowired
    private UserService userService;
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private VNPayService vnPayService;

    private Long status = Long.valueOf(0);

    public OrderController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @GetMapping("/checkout")
    public String checkout() {
        return "/cart/checkout";
    }
    @GetMapping
    public String listorder(Model model) {
        List<Order> orders = orderService.getAllOrder();
        model.addAttribute("orders", orders);
        return "/orders/orders-list";
    }

    @PostMapping("/submit")
    public String submitOrder(String note, String address, String thanhtoan , HttpServletRequest request) {
        List<CartItem> cartItems = cartService.getCartItems();
        if (cartItems.isEmpty()) {
            return "redirect:/cart"; // Redirect if cart is empty
        }
        if(Objects.equals(thanhtoan, "VNPay")){

            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            Order or=   orderService.createOrder(cartItems,note,address,userService.getNumberUser(),userService.getEmailUser(),thanhtoan, userService.getUserId());
            status= or.getId();
                boolean emailResponse = emailService.sendEmail(userService.getEmailUser(), "xin chao");
            return "redirect:" + orderService.getVnp_PayUrl(baseUrl, VNPayConfig.getIpAddress(request));
        }
        else{
            Order or=  orderService.createOrder(cartItems,note,address,userService.getNumberUser(),userService.getEmailUser(),thanhtoan, userService.getUserId());

            //   emailService.sendEmail(userService.getEmailUser(), "xin chao");
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
    public String OrderDetail(@PathVariable Long id, Model model)
    {

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
