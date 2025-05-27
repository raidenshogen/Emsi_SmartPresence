package com.example.emsismartpresence;

public class Student {
    private String name, group, year, site, status;
    private boolean isPresent, isAbsent;

    public Student(String name, String group, String year, String site) {
        this.name = name;
        this.group = group;
        this.year = year;
        this.site = site;
        this.isPresent = false;
        this.isAbsent = false;
    }

    // Getters and Setters
    public String getName() { return name; }
    public String getGroup() { return group; }
    public String getYear() { return year; }
    public String getSite() { return site; }
    public String getStatus() { return status; }
    public boolean isPresent() { return isPresent; }
    public boolean isAbsent() { return isAbsent; }

    public void setStatus(String status) { this.status = status; }
    public void setPresent(boolean present) { isPresent = present; }
    public void setAbsent(boolean absent) { isAbsent = absent; }
}

