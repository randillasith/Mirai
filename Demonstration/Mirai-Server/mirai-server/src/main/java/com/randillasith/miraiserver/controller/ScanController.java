package com.randillasith.miraiserver.controller;

import com.randillasith.miraiserver.service.ParkingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ScanController {

    private final ParkingService parkingService;

    public ScanController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    /**
     * Called by ESP32 when RFID card is scanned
     *
     * Example:
     * /api/scan?uid=AB12CD34&reader=1&text=VH001
     */
    @GetMapping("/scan")
    public String scan(
            @RequestParam String uid,
            @RequestParam int reader,
            @RequestParam String text
    ) {
        return parkingService.handleScan(uid, reader, text);
    }
}
