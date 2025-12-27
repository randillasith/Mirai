package com.randillasith.miraiserver.store;

import com.randillasith.miraiserver.model.Session;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ParkingStore {

    // Active parking sessions (uid -> session)
    public static final Map<String, Session> activeSessions = new ConcurrentHashMap<>();

    // Parking slots
    public static boolean slot1Occ = false;
    public static boolean slot2Occ = false;

    // Rate
    public static double ratePerSecond = 1.0;

    // ✅ ADD THIS METHOD
    public static Collection<Session> getAll() {
        return activeSessions.values();
    }
}
