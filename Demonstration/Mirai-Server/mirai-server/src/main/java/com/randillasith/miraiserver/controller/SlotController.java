package com.randillasith.miraiserver.controller;

import com.randillasith.miraiserver.store.ParkingStore;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SlotController {

    @GetMapping("/slot")
    public String updateSlot(
            @RequestParam int slot,
            @RequestParam int occ) {

        boolean occupied = occ == 1;

        if (slot == 1) ParkingStore.slot1Occ = occupied;
        if (slot == 2) ParkingStore.slot2Occ = occupied;

        return "OK";
    }
}
