package com.randillasith.miraiserver.model;

import java.time.LocalDateTime;

public class Session {

    public String uid;        // RFID UID
    public String vehicle;    // Vehicle ID (VH001)
    public int startReader;   // Entry reader
    public LocalDateTime startTime;

    public Session() {
    }
}
