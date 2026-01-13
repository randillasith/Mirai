package com.randillasith.miraiserver.store;

import com.randillasith.miraiserver.model.Session;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ParkingStore {

    // Active parking sessions (uid -> session)

    public static final Map<String, Session> activeSessions = new ConcurrentHashMap<>();
    public static Map<String, LocalDateTime> activeBookings = new ConcurrentHashMap<>();

    // Parking slots
    public static boolean slot1Occ = false;
    public static boolean slot2Occ = false;

    public static String slot1State = "FREE";
    public static String slot2State = "FREE";
    // -------- SLOT BOOKING FLAGS --------
    public static boolean slot1Booked = false;   // 🔥 ADD THIS
    public static boolean slot2Booked = false;

    // Rate
    public static double ratePerSecond = 1.0;


}
