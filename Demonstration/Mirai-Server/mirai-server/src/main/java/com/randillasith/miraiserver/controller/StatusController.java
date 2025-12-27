package com.randillasith.miraiserver.controller;

import com.randillasith.miraiserver.model.Session;
import com.randillasith.miraiserver.store.ParkingStore;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class StatusController {

    @GetMapping("/status")
    public Map<String, Object> status() {

        Map<String, Object> res = new HashMap<>();

        // Slot status
        res.put("slot1Occ", ParkingStore.slot1Occ);
        res.put("slot2Occ", ParkingStore.slot2Occ);

        // Prices
        res.put("priceFirstBlock", ParkingStore.priceFirstBlock);
        res.put("priceSecondBlock", ParkingStore.priceSecondBlock);
        res.put("priceLongRate", ParkingStore.priceLongRate);

        // Active sessions
        List<Map<String, Object>> active = new ArrayList<>();

        for (Map.Entry<String, Session> e : ParkingStore.activeSessions.entrySet()) {
            Map<String, Object> s = new HashMap<>();
            s.put("uid", e.getKey());
            s.put("vehicle", e.getValue().vehicle);
            s.put("reader", e.getValue().startReader);
            active.add(s);
        }

        res.put("active", active);

        return res;
    }
}
