package com.pup.pupsecurity.model;

public class InfoDataModel {

    private String latitude;
    private String longitude;
    private String contact;
    private String image;
    private String time;
    private String token;

    public InfoDataModel(String latitude, String longitude, String contact, String image, String time, String token) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.contact = contact;
        this.image = image;
        this.time = time;
        this.token = token;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public InfoDataModel(String latitude, String longitude, String contact, String image, String time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.contact = contact;
        this.image = image;
        this.time = time;
    }
}
