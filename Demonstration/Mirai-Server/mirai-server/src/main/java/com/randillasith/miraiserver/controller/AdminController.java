package com.randillasith.miraiserver.controller;

import com.randillasith.miraiserver.model.RegVehicle;
import com.randillasith.miraiserver.store.ParkingStore;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    // Get all vehicles
    @GetMapping("/vehicles")
    public Collection<RegVehicle> getVehicles() {
        return ParkingStore.registeredVehicles.values();
    }

    // Add or update vehicle
    @GetMapping("/add")
    public String addVehicle(
            @RequestParam String veh,
            @RequestParam(required = false) String name) {

        RegVehicle rv = new RegVehicle();
        rv.vehicle = veh.toUpperCase();
        rv.name = name == null ? "" : name;

        ParkingStore.registeredVehicles.put(rv.vehicle, rv);
        return "OK";
    }

    // Remove vehicle
    @GetMapping("/remove")
    public String removeVehicle(@RequestParam String veh) {
        ParkingStore.registeredVehicles.remove(veh.toUpperCase());
        return "OK";
    }
}
