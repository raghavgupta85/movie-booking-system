package com.example.booking.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ProfileController {

    @GetMapping("/api/me")
    public Map<String, Object> me(Authentication auth) {
        List<String> roles = auth.getAuthorities().stream().map(a -> a.getAuthority()).toList();
        return Map.of("email", auth.getName(), "roles", roles);
    }
}
