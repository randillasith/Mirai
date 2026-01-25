package com.randillasith.miraiserver.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_history")
public class ParkingHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String uid;
    public String vehicleId;

    public LocalDateTime entryTime;
    public LocalDateTime exitTime;

    public long durationSeconds;
    public double cost;
}
