package com.example.childrenscenterapp2.data.models;

/**
 * מייצג משתמש במערכת (חניך, מדריך, הורה וכו')
 */
public class User {
    public String uid;
    public String name;
    public String email;
    public String type; // למשל: admin, parent, child, guide, coordinator
    public String specialization; // ✅ תחום ההתמחות – רק למדריך

    // 🔧 קונסטרקטור ריק – חובה ל־Firebase
    public User() {
    }

    // ✅ קונסטרקטור רגיל ללא התמחות
    public User(String uid, String name, String email, String type) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.type = type;
        this.specialization = ""; // ברירת מחדל
    }

    // ✅ קונסטרקטור עם התמחות – למדריכים
    public User(String uid, String name, String email, String type, String specialization) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.type = type;
        this.specialization = specialization;
    }
}
