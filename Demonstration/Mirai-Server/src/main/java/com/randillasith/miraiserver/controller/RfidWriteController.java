package com.randillasith.miraiserver.controller;

import com.randillasith.miraiserver.store.ParkingStore;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rfid")
public class RfidWriteController {

    @PostMapping("/write")
    public String requestWrite(@RequestParam String vehicle) {
        if (ParkingStore.pendingWriteVehicle != null) return "BUSY";
        ParkingStore.pendingWriteVehicle = vehicle.toUpperCase();
        return "WRITE_ARMED";
    }

    @GetMapping("/pending")
    public String getPendingWrite() {
        return (ParkingStore.pendingWriteVehicle == null)
                ? "NONE"
                : "WRITE:" + ParkingStore.pendingWriteVehicle;
    }

    @PostMapping("/clear")
    public void clearPendingWrite() {
        ParkingStore.pendingWriteVehicle = null;
    }

    @PostMapping("/cancel")
    public void cancelWrite() {
        ParkingStore.pendingWriteVehicle = null;
    }
}
