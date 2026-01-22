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

        //  Unknown vehicle
        if (!vehicleService.exists(vehicle)) {
            return "DENIED|Unknown Vehicle";
        }

        //  Track session by VEHICLE (not UID)
        Session existing = ParkingStore.activeSessions.get(vehicle);

        /* ===================== ENTRY ===================== */
        if (existing == null) {

            int in = ParkingStore.activeSessions.size();

            //  Count BOOKED SLOTS
            int bookedSlots = 0;
            if (ParkingStore.slot1BookedVehicle != null) bookedSlots++;
            if (ParkingStore.slot2BookedVehicle != null) bookedSlots++;

            boolean isBookedVehicle =
                    vehicle.equals(ParkingStore.slot1BookedVehicle) ||
                            vehicle.equals(ParkingStore.slot2BookedVehicle);

            // FULL when (IN + BOOKED >= TOTAL_SLOTS)
            if (in + bookedSlots >= TOTAL_SLOTS) {
                if (!isBookedVehicle) {
                    return "FULL|Parking Full";
                }
            }



            //  Consume booking if this vehicle was booked
            if (vehicle.equals(ParkingStore.slot1BookedVehicle)) {
                ParkingStore.slot1BookedVehicle = null;
                ParkingStore.slot1State = "OCCUPIED";
            }

            if (vehicle.equals(ParkingStore.slot2BookedVehicle)) {
                ParkingStore.slot2BookedVehicle = null;
                ParkingStore.slot2State = "OCCUPIED";
            }

            ParkingStore.activeBookings.remove(vehicle); // safety cleanup

            // Create new session
            Session s = new Session();
            s.uid = uid;
            s.vehicle = vehicle;
            s.startReader = reader;
            s.startTime = now;

            ParkingStore.activeSessions.put(vehicle, s);

            return "OK_IN|" + vehicleService.getOwner(vehicle);
        }

        /* ===================== EXIT ===================== */

        long durationSeconds =
                Duration.between(existing.startTime, now).getSeconds();

        if (durationSeconds < 0) durationSeconds = 0;

        double cost = durationSeconds * ParkingStore.ratePerSecond;

        ParkingHistoryEntity h = new ParkingHistoryEntity();
        h.uid = existing.uid;
        h.vehicleId = existing.vehicle;
        h.entryTime = existing.startTime;
        h.exitTime = now;
        h.durationSeconds = durationSeconds;
        h.cost = cost;

        historyRepository.save(h);

        ParkingStore.activeSessions.remove(vehicle);

        return "OK_OUT|Rs " + String.format("%.2f", cost);
    }
}
