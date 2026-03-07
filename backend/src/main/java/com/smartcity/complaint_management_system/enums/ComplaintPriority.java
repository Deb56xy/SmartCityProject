package com.smartcity.complaint_management_system.enums;

public enum ComplaintPriority {

    HIGH(3),
    LOW(1),
    MEDIUM(2);

    public final int level;

    ComplaintPriority(int level) {
        this.level = level;
    }

    public static ComplaintPriority getByName(String name) {
        for(ComplaintPriority c: ComplaintPriority.values()) {
            if(c.name().equalsIgnoreCase(name))
                return c;
        }
        throw new IllegalArgumentException("Invalid priority");
    }

    public static ComplaintPriority getByLevel(int level) {

        if(level <= 0) {
            level = 1;
        }
        for(ComplaintPriority c : ComplaintPriority.values()) {
            if(c.level == level) {
                return c;
            }
        }
        throw new IllegalArgumentException("Invalid priority");
    }
}
