package com.randillasith.miraiserver.controller;

import com.randillasith.miraiserver.model.ParkingHistoryEntity;
import com.randillasith.miraiserver.repository.ParkingHistoryRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ParkingHistoryRepository repo;

    public ReportController(ParkingHistoryRepository repo) {
        this.repo = repo;
    }

    // 🔹 Vehicle-wise summary
    @GetMapping("/summary")
    public List<Map<String, Object>> summary() {

        List<ParkingHistoryEntity> all = repo.findAll();
        Map<String, Map<String, Object>> map = new HashMap<>();

        for (ParkingHistoryEntity h : all) {
            map.putIfAbsent(h.vehicleId, new HashMap<>());

            Map<String, Object> m = map.get(h.vehicleId);

            m.put("vehicle", h.vehicleId);
            m.put("entries", ((int) m.getOrDefault("entries", 0)) + 1);
            m.put("totalDuration",
                    ((long) m.getOrDefault("totalDuration", 0L)) + h.durationSeconds);
            m.put("totalCost",
                    ((double) m.getOrDefault("totalCost", 0.0)) + h.cost);
        }

        return new ArrayList<>(map.values());
    }

    // 🔹 Detailed history per vehicle
    @GetMapping("/history")
    public List<ParkingHistoryEntity> history(@RequestParam String vehicle) {
        return repo.findByVehicleId(vehicle);
    }
}
