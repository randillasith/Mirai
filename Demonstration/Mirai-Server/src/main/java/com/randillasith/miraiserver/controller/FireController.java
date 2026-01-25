package com.randillasith.miraiserver.controller;

import com.randillasith.miraiserver.store.ParkingStore;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FireController {

    // ESP32 calls:
    // /api/fire?state=1&gas=2100&reason=MQ2_START&device=gate-01
    // /api/fire?state=1&gas=2200&reason=MQ2_ACTIVE&device=gate-01
    // /api/fire?state=0&gas=1200&reason=MQ2_CLEAR&device=gate-01
    @RequestMapping(value = "/fire", method = {RequestMethod.GET, RequestMethod.POST})
    public Map<String, Object> fire(
            @RequestParam int state,
            @RequestParam(required = false) Integer gas,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String device
    ) {
        ParkingStore.fireActive = (state == 1);
        ParkingStore.fireGas = (gas == null) ? 0 : gas;
        ParkingStore.fireReason = (reason == null) ? "" : reason;
        ParkingStore.fireDevice = (device == null) ? "" : device;
        ParkingStore.fireLastUpdate = LocalDateTime.now();

        Map<String, Object> res = new HashMap<>();
        res.put("ok", true);
        res.put("active", ParkingStore.fireActive);
        res.put("gas", ParkingStore.fireGas);
        res.put("reason", ParkingStore.fireReason);
        res.put("device", ParkingStore.fireDevice);
        res.put("lastUpdate", String.valueOf(ParkingStore.fireLastUpdate));
        return res;
    }
}
