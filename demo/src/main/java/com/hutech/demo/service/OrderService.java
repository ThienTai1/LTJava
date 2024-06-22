package com.hutech.demo.service;

import com.hutech.demo.model.CartItem;
import com.hutech.demo.model.Order;
import com.hutech.demo.model.OrderDetails;
import com.hutech.demo.repository.OrderDetailRepository;
import com.hutech.demo.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    @Autowired
    private final OrderRepository orderRepository;
    @Autowired
    private final OrderDetailRepository orderDetailRepository;
    @Autowired
    private final CartService cartService;

    @Transactional
    public Order createOrder(String customerName, List<CartItem> cartItems) {
        Order order = new Order();
        order.setCustomerName(customerName);
        order = orderRepository.save(order);

        for (CartItem item : cartItems) {
            OrderDetails detail = new OrderDetails();
            detail.setOrder(order);
            detail.setProduct(item.getProduct());
            detail.setQuantity(item.getQuantity());
            orderDetailRepository.save(detail);
        }
        cartService.clearCart();
        return order;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public BigDecimal calculateTotalRevenue() {
        List<Order> orders = orderRepository.findAll();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Order order : orders) {
            for (OrderDetails details : order.getOrderDetails()) {
                BigDecimal price = BigDecimal.valueOf(details.getProduct().getPrice());
                int quantity = details.getQuantity();
                totalRevenue = totalRevenue.add(price.multiply(BigDecimal.valueOf(quantity)));
            }
        }
        return totalRevenue;
    }
}
