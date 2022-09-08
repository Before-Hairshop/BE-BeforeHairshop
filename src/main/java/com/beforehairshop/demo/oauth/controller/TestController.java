package com.beforehairshop.demo.oauth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping({"", "/"})
    public String home() {
        return "index";
    }
}
