package com.randillasith.miraiserver.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vehicles")
public class VehicleEntity {

    @Id
    @Column(length = 20)
    public String vehicleId;

    public String ownerName;
}
