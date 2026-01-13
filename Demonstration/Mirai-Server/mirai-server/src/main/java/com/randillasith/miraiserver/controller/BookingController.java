package com.randillasith.miraiserver.controller;

import com.randillasith.miraiserver.store.ParkingStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
@RestController
@RequestMapping("/api")
public class BookingController {

    @PostMapping("/book")
    public String book(@RequestParam String vehicle,
                       @RequestParam int slot) {

        vehicle = vehicle.toUpperCase();

        // ❌ Do NOT count bookings as capacity
        int occupied = 0;
        if (ParkingStore.slot1Occ) occupied++;
        if (ParkingStore.slot2Occ) occupied++;

        if (occupied >= 2) {
            return "FULL";
        }

        if (ParkingStore.activeBookings.containsKey(vehicle)) {
            return "ALREADY_BOOKED";
        }

        ParkingStore.activeBookings.put(vehicle, LocalDateTime.now());

        // ✅ Set slot to BOOKED explicitly
        if (slot == 1 && !ParkingStore.slot1Occ) {
            ParkingStore.slot1Booked = true;
            ParkingStore.slot1State = "BOOKED";
        } else if (slot == 2 && !ParkingStore.slot2Occ) {
            ParkingStore.slot2Booked = true;
            ParkingStore.slot2State = "BOOKED";
        }

        return "BOOKED";
    }
}
