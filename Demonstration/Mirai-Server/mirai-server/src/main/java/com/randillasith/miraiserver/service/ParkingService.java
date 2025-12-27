package com.randillasith.miraiserver.service;

import com.randillasith.miraiserver.model.*;
import com.randillasith.miraiserver.store.ParkingStore;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ParkingService {

    // 🔹 ENTRY / EXIT HANDLER
    public String handleScan(String uid, int reader, String vehicle) {

        RegVehicle rv = ParkingStore.registeredVehicles.get(vehicle.toUpperCase());
        if (rv == null) {
            return "DENIED|Unknown";
        }

        LocalDateTime now = LocalDateTime.now();
        Session existing = ParkingStore.activeSessions.get(uid);

        // ENTRY
        if (existing == null) {
            Session s = new Session();
            s.startTime = now;
            s.startReader = reader;
            s.vehicle = vehicle;

            ParkingStore.activeSessions.put(uid, s);
            return "OK_IN|" + rv.name;
        }

        // EXIT
        long sec = Duration.between(existing.startTime, now).getSeconds();
        double cost = calculateCost(sec);

        ParkingRecord r = new ParkingRecord();
        r.uid = uid;
        r.vehicle = existing.vehicle;
        r.startTime = existing.startTime;
        r.endTime = now;
        r.startReader = existing.startReader;
        r.endReader = reader;
        r.durationSec = sec;
        r.cost = cost;

        ParkingStore.history.add(r);
        ParkingStore.activeSessions.remove(uid);

        return "OK_OUT|" + rv.name + "|" + cost;
    }

    // 💰 SIMPLE COST = time × rate
    public double calculateCost(long seconds) {
        if (seconds < 0) seconds = 0;
        return seconds * ParkingStore.ratePerSecond;
    }
}
