package com.DailyBook.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/public")
    public String publicEndpoint(){
        return "hello public !!!";
    }

    @GetMapping("/protected")
    public String protectedEndpoint(){
        return "hello protected janata !!!";
    }
}
