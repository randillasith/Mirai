package com.randillasith.miraiserver.controller;

import com.randillasith.miraiserver.store.ParkingStore;
import com.randillasith.miraiserver.model.Session;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api")
public class StatusController {

    @GetMapping("/status")
    public Map<String, Object> status() {

        Map<String, Object> res = new HashMap<>();

        // ✅ slot STATES (FREE / BOOKED / OCCUPIED)
        res.put("slot1State", ParkingStore.slot1State);
        res.put("slot2State", ParkingStore.slot2State);

        // rate
        res.put("ratePerSecond", ParkingStore.ratePerSecond);

        // active vehicles
        List<Map<String, Object>> active = new ArrayList<>();

        Map<String, Object> fire = new HashMap<>();
        fire.put("active", ParkingStore.fireActive);
        fire.put("gas", ParkingStore.fireGas);
        fire.put("reason", ParkingStore.fireReason);
        fire.put("device", ParkingStore.fireDevice);
        fire.put("lastUpdate", String.valueOf(ParkingStore.fireLastUpdate));
        res.put("fire", fire);


        for (Session s : ParkingStore.activeSessions.values()) {
            long sec = Duration
                    .between(s.startTime, LocalDateTime.now())
                    .getSeconds();

            if (sec < 0) sec = 0;

            double cost = sec * ParkingStore.ratePerSecond;

            Map<String, Object> v = new HashMap<>();
            v.put("vehicle", s.vehicle);
            v.put("duration", sec);
            v.put("cost", String.format("%.2f", cost));

            active.add(v);
        }

        res.put("active", active);
        return res;
    }
}


