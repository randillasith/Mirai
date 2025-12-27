package com.randillasith.miraiserver.controller;

import com.randillasith.miraiserver.store.ParkingStore;
import com.randillasith.miraiserver.model.Session;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class StatusController {

    @GetMapping("/api/status")
    public Map<String, Object> status() {

        Map<String, Object> res = new HashMap<>();

        res.put("slot1Occ", ParkingStore.slot1Occ);
        res.put("slot2Occ", ParkingStore.slot2Occ);
        res.put("ratePerSecond", ParkingStore.ratePerSecond);

        List<Map<String, Object>> active =
                ParkingStore.getAll().stream().map(s -> {
                    Map<String, Object> m = new HashMap<>();
                    long sec = Duration.between(s.startTime, LocalDateTime.now()).getSeconds();
                    m.put("uid", s.uid);
                    m.put("vehicle", s.vehicle);
                    m.put("duration", sec);
                    m.put("cost", sec * ParkingStore.ratePerSecond);
                    return m;
                }).collect(Collectors.toList());

        res.put("active", active);
        return res;
    }
}
