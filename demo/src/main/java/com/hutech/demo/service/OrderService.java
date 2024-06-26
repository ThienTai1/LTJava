package com.hutech.demo.service;

import com.hutech.demo.Config.VNPayConfig;
import com.hutech.demo.model.CartItem;
import com.hutech.demo.model.Order;
import com.hutech.demo.model.OrderDetails;
import com.hutech.demo.model.Product;
import com.hutech.demo.repository.IUserRepository;
import com.hutech.demo.repository.OrderDetailRepository;
import com.hutech.demo.repository.OrderRepository;
import com.hutech.demo.repository.ProductRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

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
    @Autowired
    private Environment env;
    @Autowired
    private IUserRepository userRepository;
    private double total1 =0;
    @Autowired
    private JavaMailSender emailSender;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserService userService;
    @Autowired
    private ProductRepository productRepository;


    @Transactional
    public Order createOrder(List<CartItem> cartItems, String note, String address,
                             String number, String email, String thanhtoan, String name, Map<Long, Double> discountedPrices) {
        String sp = "";
        Order order = new Order();
        order.setNote(note);
        order.setNumber(number);
        order.setEmail(email);
        order.setThanhtoan(thanhtoan);
        order.setAddress(address);
        order.setName(name);
        double total = 0;
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            int newStock = product.getQuantity() - item.getQuantity();
            if (newStock < 0) {
                throw new IllegalArgumentException("Not enough stock for product: " + product.getId());
            }
            product.setQuantity(newStock);
            productRepository.save(product);
            OrderDetails detail = new OrderDetails();
            detail.setOrder(order);
            detail.setProduct(item.getProduct());
            detail.setQuantity(item.getQuantity());
            detail.setProductName(item.getProduct().getName());

            double itemPrice = discountedPrices.getOrDefault(product.getId(), product.getPrice());
            detail.setPrice(itemPrice); // Use discounted price or original price
            total += itemPrice * item.getQuantity();

            sp += "<tr>";
            sp += "<td> " + detail.getProductName() + "</td>";
            sp += "<td> " + detail.getQuantity() + "</td>";
            sp += "<td> " + detail.getPrice() + "</td>";
            sp += "</tr>";

            orderDetailRepository.save(detail);
        }

        total1 = total;
        order.setPrice(total);
        order = orderRepository.save(order);

        // Email functionality (commented out for clarity)
        // cartService.clearCart();
        return order;
    }


    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // 'true' indicates that it is HTML content
        emailSender.send(message);
    }


    public  void deleteOrder(Long Orderid){
        if (!orderRepository.existsById(Orderid)) {
            throw new IllegalStateException("Product with ID " + Orderid + " does not exist.");
        }

        orderDetailRepository.deleteByOrderId(Orderid);
        orderRepository.deleteById(Orderid);
    }

    public String getVnp_PayUrl(String urlReturn, String idAddr) {
        try {
            String vnp_Version = "2.1.0";
            String vnp_Command = "pay";
            String vnp_TxnRef = VNPayConfig.getRandomNumber(8);
            String vnp_IpAddr =  idAddr;                                 //"127.0.0.1"; Địa chỉ IP, nếu cần thiết
            String vnp_TmnCode = VNPayConfig.vnp_TmnCode;
            String orderType = "order-type";
            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf((int) total1 * 10000)); // Kiểm tra biến total1 ở đây
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", "donhang1");
            vnp_Params.put("vnp_OrderType", orderType);

            String locate = "vn";
            vnp_Params.put("vnp_Locale", locate);

            urlReturn += VNPayConfig.vnp_Returnurl;
            vnp_Params.put("vnp_ReturnUrl", urlReturn);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr); // Bổ sung nếu cần thiết

            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnpCreateDate = formatter.format(calendar.getTime());
            vnp_Params.put("vnp_CreateDate", vnpCreateDate);
            calendar.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(calendar.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    //Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    try {
                        hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                        //Build query
                        query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                        query.append('=');
                        query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            String queryUrl = query.toString();
            String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
            String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + queryUrl;
            return paymentUrl;


        } catch (Exception ex) {
            ex.printStackTrace();
            return ""; // Xn
        }
    }

    public List<Order> getAllOrder() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> findOrdersByName(String name) {
        return orderRepository.findByName(name);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<OrderDetails> getOrderDetialById(Long id) {
        return orderDetailRepository.findByOrderId(id);
    }
    public int orderReturn(HttpServletRequest request){
        Map fields = new HashMap();
        for (Enumeration params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = null;
            String fieldValue = null;
            try {
                fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII.toString());
                fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }
        String signValue = VNPayConfig.hashAllFields(fields);
        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(request.getParameter("vnp_TransactionStatus"))) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return -1;
        }
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
