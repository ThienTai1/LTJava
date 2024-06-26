package com.hutech.demo.controller;

import com.hutech.demo.model.User;
import com.hutech.demo.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller // Đánh dấu lớp này là một Controller trong Spring MVC.
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "Home/index"; // Assuming "index.html" or "index.html" exists in resources/templates
    }
}
