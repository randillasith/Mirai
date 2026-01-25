package com.randillasith.miraiserver.controller;

import com.randillasith.miraiserver.store.ParkingStore;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RateController {

    @GetMapping("/setRate")
    public String setRate(@RequestParam double rate) {
        if (rate < 0) rate = 0;
        ParkingStore.ratePerSecond = rate;
        return "OK";
    }
}
