package com.barbershop.controller;

import com.barbershop.model.Service;
import com.barbershop.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "*")
public class ServiceController {

    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping
    public List<Service> getAllServices() {
        return serviceRepository.findAll();
    }

    @PostMapping
    public Service createService(@RequestBody Service service) {
        return serviceRepository.save(service);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Service> updateService(@PathVariable Long id, @RequestBody Service serviceDetails) {
        return serviceRepository.findById(id)
                .map(service -> {
                    service.setName(serviceDetails.getName());
                    service.setDurationHours(serviceDetails.getDurationHours());
                    service.setPrice(serviceDetails.getPrice());
                    service.setIsPriceStartingFrom(serviceDetails.getIsPriceStartingFrom());
                    return ResponseEntity.ok(serviceRepository.save(service));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        return serviceRepository.findById(id)
                .map(service -> {
                    serviceRepository.delete(service);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
