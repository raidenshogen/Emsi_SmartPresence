package com.example.emsismartpresence;

public class SchoolLocation {
    private String name;
    private String address;
    private double lat;
    private double lng;

    public SchoolLocation(String name, String address, double lat, double lng) {
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

    public String getName() { return name; }
    public String getAddress() { return address; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
}