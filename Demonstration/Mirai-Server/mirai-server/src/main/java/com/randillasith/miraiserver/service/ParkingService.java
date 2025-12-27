package com.randillasith.miraiserver.service;

import com.randillasith.miraiserver.model.Session;
import com.randillasith.miraiserver.store.ParkingStore;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ParkingService {

    private final VehicleService vehicleService;

    public ParkingService(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    public String handleScan(String uid, int reader, String vehicle) {

        vehicle = vehicle.toUpperCase();

        // 🔴 Check DB (NOT memory)
        if (!vehicleService.exists(vehicle)) {
            return "DENIED|Unknown";
        }

        LocalDateTime now = LocalDateTime.now();
        Session existing = ParkingStore.activeSessions.get(uid);

        // 🟢 ENTRY
        if (existing == null) {
            Session s = new Session();
            s.startTime = now;
            s.startReader = reader;
            s.vehicle = vehicle;

            ParkingStore.activeSessions.put(uid, s);

            return "OK_IN|" + vehicleService.getOwner(vehicle);
        }

        // 🔵 EXIT
        long seconds = Duration.between(existing.startTime, now).getSeconds();
        ParkingStore.activeSessions.remove(uid);

        return "OK_OUT|" + seconds + "s";
    }
}
