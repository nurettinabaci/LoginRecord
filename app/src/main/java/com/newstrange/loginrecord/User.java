package com.newstrange.loginrecord;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    private String name, hour, date, entry;

    public User() {
    }

    public User(String name, String hour, String date, String entry) {
        this.name = name;
        this.hour = hour;
        this.date = date;
        this.entry = entry;
    }

    public String getName() {
        return name;
    }

    public String getHour() {
        return hour;
    }

    public String getDate() {
        return date;
    }

    public String getEntry() {
        return entry;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    @Override
    public String toString() {
        return getName() + " - " + getHour() + " - " + getDate() + " - " + getEntry();

    }
}
