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
    public String book(@RequestParam String vehicle) {

        vehicle = vehicle.toUpperCase();

        int used =
                ParkingStore.activeSessions.size()
                        + ParkingStore.activeBookings.size();

        if (used >= 2) {
            return "FULL";
        }

        if (ParkingStore.activeBookings.containsKey(vehicle)) {
            return "ALREADY_BOOKED";
        }

        ParkingStore.activeBookings.put(vehicle, LocalDateTime.now());

        updateSlotStates();
        return "BOOKED";
    }

    private void updateSlotStates() {
        int sessions = ParkingStore.activeSessions.size();
        int bookings = ParkingStore.activeBookings.size();

        if (sessions >= 1) ParkingStore.slot1State = "OCCUPIED";
        else if (bookings >= 1) ParkingStore.slot1State = "BOOKED";
        else ParkingStore.slot1State = "FREE";

        if (sessions + bookings >= 2) {
            ParkingStore.slot2State =
                    (sessions >= 2) ? "OCCUPIED" : "BOOKED";
        } else {
            ParkingStore.slot2State = "FREE";
        }
    }

}
