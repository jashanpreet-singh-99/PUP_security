package com.pup.pupsecurity.model;

public class UserData {

    private String name;
    private String image;
    private String contact;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public UserData(String name, String image, String contact) {
        this.name = name;
        this.image = image;
        this.contact = contact;
    }
}
