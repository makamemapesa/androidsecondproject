package com.example.mysecondproject;

public class Emergency {
    public int id;
    public String reporterEmail;
    public double lat, lng;
    public String status, assignedDriverEmail;
    public String address;

    public Emergency(int i, String e, double la, double lo, String s, String a, String addr) {
        id = i; 
        reporterEmail = e; 
        lat = la; 
        lng = lo;
        status = s; 
        assignedDriverEmail = a;
        address = addr;
    }

    // Constructor for backward compatibility
    public Emergency(int i, String e, double la, double lo, String s, String a) {
        this(i, e, la, lo, s, a, "Address not available");
    }
}