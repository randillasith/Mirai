package com.randillasith.miraiserver.controller;

import com.randillasith.miraiserver.store.ParkingStore;
import com.randillasith.miraiserver.model.Session;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class StatusController {

    @GetMapping("/status")
    public Map<String, Object> status() {

        Map<String, Object> res = new HashMap<>();

        // slot status
        res.put("slot1Occ", ParkingStore.slot1Occ);
        res.put("slot2Occ", ParkingStore.slot2Occ);

        // rate
        res.put("ratePerSecond", ParkingStore.ratePerSecond);

        // active vehicles
        List<Map<String, Object>> active = new ArrayList<>();

        for (Session s : ParkingStore.activeSessions.values()) {
            long sec = Duration.between(s.startTime, LocalDateTime.now()).getSeconds();
            if (sec < 0) sec = 0;

            double cost = sec * ParkingStore.ratePerSecond;

            Map<String, Object> v = new HashMap<>();
            v.put("vehicle", s.vehicle);
            v.put("ownerName", "");
            v.put("duration", sec);
            v.put("cost", String.format("%.2f", cost));

            active.add(v);
        }

        res.put("active", active);
        return res;
    }
}
