package com.caucraft.mciguiv3.launch.gameinstance;

import java.time.Instant;

/**
 *
 * @author caucow
 */
public class LaunchInfo {
    
    private final String user;
    private final String version;
    private Instant startTime;
    private Instant closeTime;
    
    public LaunchInfo(String user, String version) {
        this.user = user;
        this.version = version;
    }
    
    public void setStartTime() {
        Instant time = Instant.now();
        if (startTime != null) {
            throw new IllegalStateException("Launch info already has a start time set.");
        }
        this.startTime = time;
    }
    
    public void setCloseTime() {
        Instant time = Instant.now();
        if (closeTime != null) {
            throw new IllegalStateException("Launch info already has a start time set.");
        }
        this.closeTime = time;
    }
    
    public String getName() {
        return String.format("%s (%s)", user, version);
    }
    
    public String getUsername() {
        return user;
    }
    
    public String getGameVersion() {
        return version;
    }
    
    public Instant getStartTime() {
        return startTime;
    }
    
    public Instant getCloseTime() {
        return closeTime;
    }
    
    public String toString() {
        return getName();
    }
    
}
