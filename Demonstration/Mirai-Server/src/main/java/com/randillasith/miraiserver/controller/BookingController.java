package com.randillasith.miraiserver.controller;


import com.randillasith.miraiserver.store.ParkingStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.time.LocalDateTime;
@RestController
@RequestMapping("/api")
public class BookingController {

    @PostMapping("/book")
    public String book(@RequestParam String vehicle,
                       @RequestParam int slot) {

        vehicle = vehicle.toUpperCase();

        if (slot != 1 && slot != 2) return "INVALID_SLOT";

        if (slot == 1) {
            if (ParkingStore.slot1Occ) return "SLOT_OCCUPIED";          // ✅ block booking
            if (ParkingStore.slot1BookedVehicle != null) return "ALREADY_BOOKED";

            ParkingStore.slot1BookedVehicle = vehicle;
            ParkingStore.slot1Booked = true;                           // ✅ keep consistent
            ParkingStore.slot1State = "BOOKED";
            return "BOOKED";
        }

        // slot == 2
        if (ParkingStore.slot2Occ) return "SLOT_OCCUPIED";              // ✅ block booking
        if (ParkingStore.slot2BookedVehicle != null) return "ALREADY_BOOKED";

        ParkingStore.slot2BookedVehicle = vehicle;
        ParkingStore.slot2Booked = true;                                // ✅ keep consistent
        ParkingStore.slot2State = "BOOKED";
        return "BOOKED";
    }
    @PostMapping("/cancel")
    public String cancel(@RequestParam int slot,
                         @RequestParam String vehicle) {

        vehicle = vehicle.toUpperCase();

        if (slot != 1 && slot != 2) return "INVALID_SLOT";

        if (slot == 1) {
            if (ParkingStore.slot1Occ) return "SLOT_OCCUPIED"; // don't cancel if physically occupied
            if (ParkingStore.slot1BookedVehicle == null) return "NOT_BOOKED";
            if (!vehicle.equals(ParkingStore.slot1BookedVehicle)) return "NOT_YOUR_BOOKING";

            // ✅ clear booking
            ParkingStore.slot1BookedVehicle = null;
            ParkingStore.slot1Booked = false;
            ParkingStore.slot1State = "FREE";
            return "CANCELLED";
        }

        // slot == 2
        if (ParkingStore.slot2Occ) return "SLOT_OCCUPIED";
        if (ParkingStore.slot2BookedVehicle == null) return "NOT_BOOKED";
        if (!vehicle.equals(ParkingStore.slot2BookedVehicle)) return "NOT_YOUR_BOOKING";

        ParkingStore.slot2BookedVehicle = null;
        ParkingStore.slot2Booked = false;
        ParkingStore.slot2State = "FREE";
        return "CANCELLED";
    }
    @GetMapping("/bookings")
    public List<Map<String, Object>> getBookings() {
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
