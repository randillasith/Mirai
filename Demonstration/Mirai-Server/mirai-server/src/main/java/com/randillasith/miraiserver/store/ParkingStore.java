package com.randillasith.miraiserver.store;

import com.randillasith.miraiserver.model.*;

import java.util.*;

public class ParkingStore {

    // Active parking sessions
    public static final Map<String, Session> activeSessions = new HashMap<>();

    // Parking history
    public static final List<ParkingRecord> history = new ArrayList<>();

    // Registered vehicles
    public static final Map<String, RegVehicle> registeredVehicles = new HashMap<>();

    // Slot occupancy
    public static boolean slot1Occ = false;
    public static boolean slot2Occ = false;

    // 💰 NEW SIMPLE COST LOGIC
    public static double ratePerSecond = 1.0; // Rs per second (changeable)
}
