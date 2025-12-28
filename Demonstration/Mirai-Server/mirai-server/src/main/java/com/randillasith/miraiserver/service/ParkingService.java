package com.randillasith.miraiserver.service;

import com.randillasith.miraiserver.model.ParkingHistoryEntity;
import com.randillasith.miraiserver.model.Session;
import com.randillasith.miraiserver.repository.ParkingHistoryRepository;
import com.randillasith.miraiserver.store.ParkingStore;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ParkingService {

    private final VehicleService vehicleService;
    private final ParkingHistoryRepository historyRepository;

    public ParkingService(
            VehicleService vehicleService,
            ParkingHistoryRepository historyRepository
    ) {
        this.vehicleService = vehicleService;
        this.historyRepository = historyRepository;
    }

    /**
     * Handles RFID scan (ENTRY or EXIT)
     */
    public String handleScan(String uid, int reader, String vehicle) {

        vehicle = vehicle.toUpperCase();
        LocalDateTime now = LocalDateTime.now();

        // ❌ Vehicle not registered
        if (!vehicleService.exists(vehicle)) {
            return "DENIED|Unknown Vehicle";
        }

        // Check if this UID is already inside
        Session existing = ParkingStore.activeSessions.get(uid);

        /* ================= ENTRY ================= */
        if (existing == null) {

            Session s = new Session();
            s.uid = uid;
            s.vehicle = vehicle;
            s.startReader = reader;
            s.startTime = now;

            ParkingStore.activeSessions.put(uid, s);

            // Update slot status (simple logic)
            ParkingStore.slot1Occ = true;

            return "OK_IN|" + vehicleService.getOwner(vehicle);
        }

        /* ================= EXIT ================= */
        long durationSeconds =
                Duration.between(existing.startTime, now).getSeconds();

        if (durationSeconds < 0) durationSeconds = 0;

        double cost = durationSeconds * ParkingStore.ratePerSecond;

        // Save completed session to DB
        ParkingHistoryEntity h = new ParkingHistoryEntity();
        h.uid = uid;
        h.vehicleId = existing.vehicle;
        h.entryTime = existing.startTime;
        h.exitTime = now;
        h.durationSeconds = durationSeconds;
        h.cost = cost;

        historyRepository.save(h);

        // Remove from active sessions
        ParkingStore.activeSessions.remove(uid);

        // Update slot status
        ParkingStore.slot1Occ = false;

        return "OK_OUT|Rs " + cost;
    }
}
