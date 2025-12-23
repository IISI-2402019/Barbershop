package com.barbershop.controller;

import com.barbershop.model.Stylist;
import com.barbershop.repository.StylistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stylists")
public class StylistController {

    @Autowired
    private StylistRepository stylistRepository;

    @GetMapping
    public List<Stylist> getAllStylists() {
        return stylistRepository.findAll();
    }
}
