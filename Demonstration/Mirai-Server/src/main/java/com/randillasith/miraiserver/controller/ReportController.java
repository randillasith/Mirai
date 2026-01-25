package com.randillasith.miraiserver.controller;

import com.randillasith.miraiserver.model.ParkingHistoryEntity;
import com.randillasith.miraiserver.repository.ParkingHistoryRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
            m.put("totalTime",
                    ((long) m.getOrDefault("totalTime", 0L)) + h.durationSeconds);
            m.put("totalCost",
                    ((double) m.getOrDefault("totalCost", 0.0)) + h.cost);
        }

        return new ArrayList<>(map.values());
    }


    @GetMapping("/overview")
    public Map<String, Object> overview() {

        Map<String, Object> res = new HashMap<>();

        LocalDateTime today = LocalDate.now().atStartOfDay();

        List<ParkingHistoryEntity> list =
                repo.findByExitTimeAfter(today);

        double revenue = list.stream().mapToDouble(h -> h.cost).sum();
        long vehicles = list.size();
        long totalTime = list.stream().mapToLong(h -> h.durationSeconds).sum();

        res.put("todayRevenue", revenue);
        res.put("totalVehicles", vehicles);
        res.put("avgDuration", vehicles == 0 ? 0 : totalTime / vehicles);
        res.put("avgCost", vehicles == 0 ? 0 : revenue / vehicles);

        return res;
    }


    // 🔹 Detailed history per vehicle
    @GetMapping("/history")
    public List<ParkingHistoryEntity> history(@RequestParam String vehicle) {
        return repo.findByVehicleId(vehicle);
    }
}
