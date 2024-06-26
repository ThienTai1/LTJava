package com.hutech.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private  JavaMailSender mailSender;

    public  boolean sendEmail(String userEmail, String link) {
        try {

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("ldthanhtrung.2106@gmail.com"); // Thay bằng email của bạn
            message.setTo(userEmail);
            message.setSubject("Đơn Hàng Của Bạn");
            message.setText(link);

            mailSender.send(message);
            return true;
        } catch (Exception e) {
           // System.out.println("Error occurred while sending email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}