package com.randillasith.miraiserver.controller;

import com.randillasith.miraiserver.store.ParkingStore;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SlotController {

    @GetMapping("/slot")
    public void updateSlot(
            @RequestParam int slot,
            @RequestParam boolean occupied
    ) {
        System.out.println("Slot update received: slot=" + slot + " occupied=" + occupied);

        if (slot == 1) {
            ParkingStore.slot1Occ = occupied;
            ParkingStore.slot1State = occupied ? "OCCUPIED" : "FREE";
        } else if (slot == 2) {
            ParkingStore.slot2Occ = occupied;
            ParkingStore.slot2State = occupied ? "OCCUPIED" : "FREE";
        }
    }
}

