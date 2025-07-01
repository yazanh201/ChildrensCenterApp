package com.example.childrenscenterapp2.data.models;

/**
 * מייצג משתמש במערכת (חניך, מדריך, הורה וכו')
 */
public class User {
    public String uid;
    public String name;
    public String email;
    public String type; // למשל: admin, parent, child, guide, coordinator

    public User() {
        // נדרש על ידי Firebase
    }

    public User(String uid, String name, String email, String type) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.type = type;
    }
}
