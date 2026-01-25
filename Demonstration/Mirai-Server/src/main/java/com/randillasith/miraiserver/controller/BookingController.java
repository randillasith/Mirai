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

        if (slot == 1) {
          if (ParkingStore.slot1BookedVehicle != null) return "ALREADY_BOOKED";
            ParkingStore.slot1BookedVehicle = vehicle;
            ParkingStore.slot1State = "BOOKED";
        }

        if (slot == 2) {
            if (ParkingStore.slot2BookedVehicle != null) return "ALREADY_BOOKED";
            ParkingStore.slot2BookedVehicle = vehicle;
            ParkingStore.slot2State = "BOOKED";
        }

        return "BOOKED";
    }

}
