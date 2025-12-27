package com.randillasith.miraiserver.controller;

import com.randillasith.miraiserver.model.Session;
import com.randillasith.miraiserver.service.ParkingService;
import com.randillasith.miraiserver.store.ParkingStore;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
public class StatusController {

    private final ParkingService parkingService;

    public StatusController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {

        Map<String, Object> res = new HashMap<>();

        // Slots
        res.put("slot1Occ", ParkingStore.slot1Occ);
        res.put("slot2Occ", ParkingStore.slot2Occ);

        // Rate
        res.put("ratePerSecond", ParkingStore.ratePerSecond);

        // Active sessions with LIVE COST
        List<Map<String, Object>> active = new ArrayList<>();

        for (Map.Entry<String, Session> e : ParkingStore.activeSessions.entrySet()) {
            Session s = e.getValue();

            long sec = Duration.between(s.startTime, LocalDateTime.now()).getSeconds();
            double cost = parkingService.calculateCost(sec);

            Map<String, Object> obj = new HashMap<>();
            obj.put("uid", e.getKey());
            obj.put("vehicle", s.vehicle);
            obj.put("duration", sec);
            obj.put("cost", cost);

            active.add(obj);
        }

        res.put("active", active);
        return res;
    }
}
