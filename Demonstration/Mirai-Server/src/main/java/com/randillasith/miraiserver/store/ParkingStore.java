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
    public static volatile String pendingWriteVehicle = null;


    // Fire status
    public static volatile boolean fireActive = false;
    public static volatile int fireGas = 0;
    public static volatile String fireReason = "";
    public static volatile String fireDevice = "";
    public static volatile LocalDateTime fireLastUpdate = null;


    // Parking slots
    public static boolean slot1Occ = false;
    public static boolean slot2Occ = false;

    public static String slot1State = "FREE";
    public static String slot2State = "FREE";

    // -------- SLOT BOOKING FLAGS --------
    public static boolean slot1Booked = false;
    public static boolean slot2Booked = false;

    public static String slot1BookedVehicle = null;
    public static String slot2BookedVehicle = null;


    // Rate
    public static double ratePerSecond = 1.0;


}
