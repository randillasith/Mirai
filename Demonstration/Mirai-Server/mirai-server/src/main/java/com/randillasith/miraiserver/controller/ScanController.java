package com.randillasith.miraiserver.controller;

import com.randillasith.miraiserver.service.ParkingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ScanController {

    private final ParkingService parkingService;

    public ScanController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @GetMapping("/scan")
    public String scan(
            @RequestParam String uid,
            @RequestParam int reader,
            @RequestParam String text) {

        return parkingService.handleScan(uid, reader, text);
    }
}
