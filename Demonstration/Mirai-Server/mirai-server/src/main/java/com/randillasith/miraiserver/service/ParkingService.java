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

    // 🔢 total parking slots
    private static final int TOTAL_SLOTS = 2;

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
    public synchronized String handleScan(String uid, int reader, String vehicle) {

        vehicle = vehicle.toUpperCase();
        LocalDateTime now = LocalDateTime.now();

        // Vehicle not registered
        if (!vehicleService.exists(vehicle)) {
            return "DENIED|Unknown Vehicle";
        }

        // Check if UID already inside (EXIT case)
        Session existing = ParkingStore.activeSessions.get(uid);

        /* ================= ENTRY ================= */
        if (existing == null) {

            int usedSlots =
                    ParkingStore.activeSessions.size()
                            + ParkingStore.activeBookings.size();

            if (usedSlots >= TOTAL_SLOTS) {
                return "FULL|Parking Full";
            }

            // ❗ Block non-booked vehicles if bookings exist
            if (!ParkingStore.activeBookings.isEmpty()
                    && !ParkingStore.activeBookings.containsKey(vehicle)) {
                return "DENIED|Slot Reserved";
            }

            // Consume booking if exists
            ParkingStore.activeBookings.remove(vehicle);

            Session s = new Session();
            s.uid = uid;
            s.vehicle = vehicle;
            s.startReader = reader;
            s.startTime = now;

            ParkingStore.activeSessions.put(uid, s);
            updateSlots();

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

        // Remove active session
        ParkingStore.activeSessions.remove(uid);

        // Update slot flags safely
        // updateSlots();

        return "OK_OUT|Rs " + String.format("%.2f", cost);
    }

    /**
     * Updates slot occupancy based on active sessions count
     */

    private void updateSlots() {
        int used =
                ParkingStore.activeSessions.size()
                        + ParkingStore.activeBookings.size();

        ParkingStore.slot1Occ = used >= 1;
        ParkingStore.slot2Occ = used >= 2;
    }


}
