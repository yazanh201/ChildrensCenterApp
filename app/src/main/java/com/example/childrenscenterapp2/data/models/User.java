package com.example.childrenscenterapp2.data.models;

public class User {
    private String uid;
    private String name;
    private String email;
    private String type; // admin, parent, child, guide, coordinator
    private String specialization; // למדריך
    private String idNumber; // להורה

    // 🔧 קונסטרקטור ריק – חובה ל־Firebase
    public User() {
    }

    // ✅ קונסטרקטור כללי – מתאים לכל סוגי המשתמשים
    public User(String uid, String name, String email, String type, String specialization, String idNumber) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.type = type;
        this.specialization = specialization != null ? specialization : "";
        this.idNumber = idNumber != null ? idNumber : "";
    }

    // ✅ קונסטרקטור מינימלי – למשתמשים רגילים
    public User(String uid, String name, String email, String type) {
        this(uid, name, email, type, "", "");
    }

    // ✅ קונסטרקטור למדריך
    public static User createGuide(String uid, String name, String email, String specialization) {
        return new User(uid, name, email, "מדריך", specialization, "");
    }

    // ✅ קונסטרקטור להורה
    public static User createParent(String uid, String name, String email, String idNumber) {
        return new User(uid, name, email, "הורה", "", idNumber);
    }
    // ✅ קונסטרקטור לילד
    public static User createChild(String uid, String name, String email, String idNumber) {
        return new User(uid, name, email, "ילד", "", idNumber);
    }


    // 🟦 Getters ו־Setters

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }
}
