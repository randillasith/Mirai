package com.randillasith.miraiserver.service;

import com.randillasith.miraiserver.model.VehicleEntity;
import com.randillasith.miraiserver.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    // Add or update vehicle
    public void save(String vehicleId, String ownerName) {
        VehicleEntity v = new VehicleEntity();
        v.vehicleId = vehicleId;
        v.ownerName = ownerName;
        vehicleRepository.save(v);
    }

    // Check if vehicle exists
    public boolean exists(String vehicleId) {
        return vehicleRepository.existsById(vehicleId);
    }

    // Get owner name
    public String getOwner(String vehicleId) {
        Optional<VehicleEntity> v = vehicleRepository.findById(vehicleId);
        return v.map(vehicle -> vehicle.ownerName).orElse(null);
    }

    // List all vehicles (admin page)
    public List<VehicleEntity> findAll() {
        return vehicleRepository.findAll();
    }

    // Delete vehicle
    public void delete(String vehicleId) {
        vehicleRepository.deleteById(vehicleId);
    }
}
