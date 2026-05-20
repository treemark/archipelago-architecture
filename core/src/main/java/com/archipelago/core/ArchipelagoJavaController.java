package com.archipelago.core;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/java")
public class ArchipelagoJavaController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Java!";
    }

    @GetMapping("/status")
    public Status getStatus() {
        return new Status("UP", "Java and Kotlin both supported");
    }

    public record Status(String status, String message) {}
}
