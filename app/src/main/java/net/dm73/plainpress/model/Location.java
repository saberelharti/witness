package net.dm73.plainpress.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Location {

    private String city;
    private String country;
    private double latitude;
    private double longitude;
    private String value;

    public Location() {
    }

    public Location(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Location(String city, String country, double latitude, double longitude) {
        this.city = city;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Location(String city, String country, double latitude, double longitude, String value) {
        this.city = city;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.value = value;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
