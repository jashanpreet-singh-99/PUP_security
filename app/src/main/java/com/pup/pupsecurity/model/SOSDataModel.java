package com.pup.pupsecurity.model;

import java.io.Serializable;

public class SOSDataModel implements Serializable {

    private String id;
    private String contact;
    private String latitude;
    private String longitude;
    private String image;
    private int completed;
    private String extraMessage;
    private String time;
    private String type;
    private String[] deployedGuard;

    public SOSDataModel(String id, String contact, String latitude, String longitude, String image, int completed, String extraMessage, String time, String type, String[] deployedGuard) {
        this.id = id;
        this.contact = contact;
        this.latitude = latitude;
        this.longitude = longitude;
        this.image = image;
        this.completed = completed;
        this.extraMessage = extraMessage;
        this.time = time;
        this.type = type;
        this.deployedGuard = deployedGuard;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getCompleted() {
        return completed;
    }

    public void setCompleted(int completed) {
        this.completed = completed;
    }

    public String getExtraMessage() {
        return extraMessage;
    }

    public void setExtraMessage(String extraMessage) {
        this.extraMessage = extraMessage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[] getDeployedGuard() {
        return deployedGuard;
    }

    public void setDeployedGuard(String[] deployedGuard) {
        this.deployedGuard = deployedGuard;
    }
}
