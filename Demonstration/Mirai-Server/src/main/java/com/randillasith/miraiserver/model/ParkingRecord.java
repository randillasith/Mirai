package com.randillasith.miraiserver.model;

import java.time.LocalDateTime;

public class ParkingRecord {
    public String uid;
    public String vehicle;
    public LocalDateTime startTime;
    public LocalDateTime endTime;
    public int startReader;
    public int endReader;
    public long durationSec;
    public double cost;
}
