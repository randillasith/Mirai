package com.randillasith.miraiserver.controller;

import com.randillasith.miraiserver.model.VehicleEntity;
import com.randillasith.miraiserver.service.VehicleService;
import com.randillasith.miraiserver.store.ParkingStore;
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
    @RestController
    @RequestMapping("/api")
    public class RfidWriteController {

        @PostMapping("/rfid/write")
        public String requestWrite(@RequestParam String vehicle) {

            if (ParkingStore.pendingWriteVehicle != null) {
                return "BUSY";
            }

            ParkingStore.pendingWriteVehicle = vehicle.toUpperCase();
            return "WRITE_ARMED";
        }

        @GetMapping("/rfid/pending")
        public String getPendingWrite() {

            if (ParkingStore.pendingWriteVehicle == null) {
                return "NONE";
            }

            return "WRITE:" + ParkingStore.pendingWriteVehicle;
        }

        @PostMapping("/rfid/clear")
        public void clearPendingWrite() {
            ParkingStore.pendingWriteVehicle = null;
        }
        @PostMapping("/api/rfid/cancel")
        public void cancelWrite() {
            ParkingStore.pendingWriteVehicle = null;
        }

    }

    private void check(HttpSession session) {
        Boolean ok = (Boolean) session.getAttribute("ADMIN");
        if (ok == null || !ok) {
            throw new RuntimeException("Unauthorized");
        }
    }

}
