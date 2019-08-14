package com.newstrange.loginrecord;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    private String name, hour, date, entering;

    public User() {
    }

    public User(String name, String hour, String date, String entering) {
        this.name = name;
        this.hour = hour;
        this.date = date;
        this.entering = entering;
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

    public String getEntering() {
        return entering;
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

    public void setEntering(String entering) {
        this.entering = entering;
    }

    @Override
    public String toString() {
        return getName() + " - " + getHour() + " - " + getDate() + " - " + getEntering();

    }
}
