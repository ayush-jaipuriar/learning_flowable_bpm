package com.example.loanapplication.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/simple-test")
public class SimpleTestController {

    @GetMapping
    public String test() {
        return "Application is running!";
    }
}
