package com.randillasith.miraiserver.controller;

import com.randillasith.miraiserver.store.ParkingStore;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SlotController {

    // Accept BOTH GET and POST
    @RequestMapping(value = "/slot", method = {RequestMethod.GET, RequestMethod.POST})
    public String updateSlot(
            @RequestParam("slot") int slot,
            @RequestParam("occupied") String occupied
    ) {
        // Safely convert occupied value
        boolean isOccupied =
                occupied.equalsIgnoreCase("true") ||
                        occupied.equals("1");

        System.out.println(
                "ULTRASONIC → slot=" + slot + " occupied=" + isOccupied
        );

        if (slot == 1) {
            ParkingStore.slot1Occ = isOccupied;
            ParkingStore.slot1State = isOccupied ? "OCCUPIED" : "FREE";
        } else if (slot == 2) {
            ParkingStore.slot2Occ = isOccupied;
            ParkingStore.slot2State = isOccupied ? "OCCUPIED" : "FREE";
        }

        return "OK";
    }
}
