package com.randillasith.miraiserver.controller;

import com.randillasith.miraiserver.model.VehicleEntity;
import com.randillasith.miraiserver.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final VehicleService vehicleService;


    public AdminController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    // Add / update vehicle
    @GetMapping("/add")
    public String addVehicle(
            @RequestParam String vehicle,
            @RequestParam String name) {

        vehicleService.save(vehicle.toUpperCase(), name);
        return "OK";
    }

    // List vehicles
    @GetMapping("/list")
    public List<VehicleEntity> listVehicles() {
        return vehicleService.findAll();
    }

    // Remove vehicle
    @GetMapping("/delete")
    public String deleteVehicle(@RequestParam String vehicle) {
        vehicleService.delete(vehicle.toUpperCase());
        return "OK";
    }
    private void check(HttpSession session) {
        Boolean ok = (Boolean) session.getAttribute("ADMIN");
        if (ok == null || !ok) {
            throw new RuntimeException("Unauthorized");
        }
    }

}
