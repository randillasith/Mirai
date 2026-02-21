package com.randillasith.miraiserver.controller;

import com.randillasith.miraiserver.store.ParkingStore;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class BookingController {

    private static final int TOTAL_SLOTS = 2;

    @PostMapping("/book")
    public String book(@RequestParam String vehicle,
                       @RequestParam int slot) {

        vehicle = vehicle.toUpperCase();

        if (slot != 1 && slot != 2) return "INVALID_SLOT";

        // 1) If vehicle already inside, no booking needed (block)
        if (ParkingStore.activeSessions.containsKey(vehicle)) {
            return "DENIED|ALREADY_INSIDE";
        }

        // 2) If vehicle already booked in another slot, block
        if (vehicle.equals(ParkingStore.slot1BookedVehicle) || vehicle.equals(ParkingStore.slot2BookedVehicle)) {
            return "DENIED|ALREADY_BOOKED";
        }

        // 3) Capacity check (this fixes your issue)
        int in = ParkingStore.activeSessions.size();
        int booked = 0;
        if (ParkingStore.slot1BookedVehicle != null) booked++;
        if (ParkingStore.slot2BookedVehicle != null) booked++;

        // If already full (IN + BOOKED >= TOTAL), deny
        if (in + booked >= TOTAL_SLOTS) {
            return "FULL|PARKING_FULL";
        }

        // 4) Slot-specific checks
        if (slot == 1) {
            if (ParkingStore.slot1Occ) return "DENIED|SLOT_OCCUPIED";
            if (ParkingStore.slot1BookedVehicle != null) return "DENIED|SLOT_ALREADY_BOOKED";

            ParkingStore.slot1BookedVehicle = vehicle;
            ParkingStore.slot1Booked = true;
            ParkingStore.slot1State = "BOOKED";
            return "BOOKED";
        }

        // slot == 2
        if (ParkingStore.slot2Occ) return "DENIED|SLOT_OCCUPIED";
        if (ParkingStore.slot2BookedVehicle != null) return "DENIED|SLOT_ALREADY_BOOKED";

        ParkingStore.slot2BookedVehicle = vehicle;
        ParkingStore.slot2Booked = true;
        ParkingStore.slot2State = "BOOKED";
        return "BOOKED";
    }

    @PostMapping("/cancel")
    public String cancel(@RequestParam int slot,
                         @RequestParam String vehicle) {

        vehicle = vehicle.toUpperCase();

        if (slot != 1 && slot != 2) return "INVALID_SLOT";

        if (slot == 1) {
            if (ParkingStore.slot1BookedVehicle == null) return "NOT_BOOKED";
            if (!vehicle.equals(ParkingStore.slot1BookedVehicle)) return "NOT_YOUR_BOOKING";

            // If physically occupied, don't cancel (optional safety)
            if (ParkingStore.slot1Occ) return "DENIED|SLOT_OCCUPIED";

            ParkingStore.slot1BookedVehicle = null;
            ParkingStore.slot1Booked = false;
            ParkingStore.slot1State = "FREE";
            return "CANCELLED";
        }

        // slot == 2
        if (ParkingStore.slot2BookedVehicle == null) return "NOT_BOOKED";
        if (!vehicle.equals(ParkingStore.slot2BookedVehicle)) return "NOT_YOUR_BOOKING";
        if (ParkingStore.slot2Occ) return "DENIED|SLOT_OCCUPIED";

        ParkingStore.slot2BookedVehicle = null;
        ParkingStore.slot2Booked = false;
        ParkingStore.slot2State = "FREE";
        return "CANCELLED";
    }

    // For your UI "Current Bookings" table
    @GetMapping("/bookings")
    public List<Map<String, Object>> bookings() {
        List<Map<String, Object>> list = new ArrayList<>();

        if (ParkingStore.slot1BookedVehicle != null) {
            Map<String, Object> b = new HashMap<>();
            b.put("slot", 1);
            b.put("vehicle", ParkingStore.slot1BookedVehicle);
            b.put("state", ParkingStore.slot1State);
            b.put("occupied", ParkingStore.slot1Occ);
            list.add(b);
        }

        if (ParkingStore.slot2BookedVehicle != null) {
            Map<String, Object> b = new HashMap<>();
            b.put("slot", 2);
            b.put("vehicle", ParkingStore.slot2BookedVehicle);
            b.put("state", ParkingStore.slot2State);
            b.put("occupied", ParkingStore.slot2Occ);
            list.add(b);
        }

        return list;
    }
}