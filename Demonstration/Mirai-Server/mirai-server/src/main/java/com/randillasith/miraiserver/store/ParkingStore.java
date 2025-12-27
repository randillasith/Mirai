package com.randillasith.miraiserver.store;

import com.randillasith.miraiserver.model.*;

import java.util.*;

public class ParkingStore {

    public static final Map<String, Session> activeSessions = new HashMap<>();
    public static final List<ParkingRecord> history = new ArrayList<>();
    public static final Map<String, RegVehicle> registeredVehicles = new HashMap<>();

    public static boolean slot1Occ = false;
    public static boolean slot2Occ = false;

    public static double priceFirstBlock = 5.0;
    public static double priceSecondBlock = 10.0;
    public static double priceLongRate = 50.0;
}
