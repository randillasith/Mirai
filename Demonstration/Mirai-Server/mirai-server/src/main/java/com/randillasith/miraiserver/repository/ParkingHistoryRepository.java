package com.randillasith.miraiserver.repository;

import com.randillasith.miraiserver.model.ParkingHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ParkingHistoryRepository
        extends JpaRepository<ParkingHistoryEntity, Long> {

    List<ParkingHistoryEntity> findByVehicleId(String vehicleId);

    List<ParkingHistoryEntity> findByExitTimeAfter(LocalDateTime today);
}
