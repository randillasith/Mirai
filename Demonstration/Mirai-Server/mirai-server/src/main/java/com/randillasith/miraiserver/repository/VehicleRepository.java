package com.randillasith.miraiserver.repository;

import com.randillasith.miraiserver.model.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository
        extends JpaRepository<VehicleEntity, String> {
}
