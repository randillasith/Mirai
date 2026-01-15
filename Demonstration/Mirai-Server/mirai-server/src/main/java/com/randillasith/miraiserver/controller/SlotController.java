package com.randillasith.miraiserver.controller;

import com.randillasith.miraiserver.store.ParkingStore;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SlotController {

    @RequestMapping(value = "/slot", method = {RequestMethod.GET, RequestMethod.POST})
    public String updateSlot(
            @RequestParam int slot,
            @RequestParam(value = "occ", required = false) String occ,
            @RequestParam(value = "occupied", required = false) String occupied
    ) {

        String value = (occ != null) ? occ : occupied;
        if (value == null) return "MISSING";

        boolean isOccupied =
                value.equals("1") ||
                        value.equalsIgnoreCase("true");

        if (slot == 1) {
            ParkingStore.slot1Occ = isOccupied;

            if (isOccupied) {
                // 🔥 VEHICLE ARRIVED → CONSUME BOOKING
                ParkingStore.slot1Booked = false;
                ParkingStore.slot1State = "OCCUPIED";
            } else {
                ParkingStore.slot1State =
                        ParkingStore.slot1Booked ? "BOOKED" : "FREE";
            }
        }

        if (slot == 2) {
            ParkingStore.slot2Occ = isOccupied;

            if (isOccupied) {
                ParkingStore.slot2Booked = false;
                ParkingStore.slot2State = "OCCUPIED";
            } else {
                ParkingStore.slot2State =
                        ParkingStore.slot2Booked ? "BOOKED" : "FREE";
            }
        }

        return "OK";
    }
}
