package com.example.childrenscenterapp2.data.models;

import com.google.firebase.Timestamp;
import java.util.List;

/**
 * מודל הרשמה של משתמש לפעילות.
 * משמש לשמירת פרטי ההרשמה ב-Firebase וב-SQLite המקומי.
 */
public class RegistrationForUserModel {

    private String id;  // מזהה המסמך (Document ID) – ייחודי לכל הרשמה
    private String activityId; // מזהה הפעילות שאליה המשתמש נרשם
    private List<String> days; // רשימת ימים שבהם מתקיימת הפעילות
    private String domain; // תחום הפעילות (מדע, חברה, יצירה וכו')
    private Timestamp timestamp; // תאריך ושעה של ההרשמה

    /**
     * קונסטרקטור ריק נדרש לשימוש על ידי Firebase בעת שליפת הנתונים.
     */
    public RegistrationForUserModel() {
        // קונסטרקטור ריק ל-Firebase
    }

    // ================== Getters & Setters ==================

    /**
     * מחזיר את מזהה המסמך הייחודי.
     * @return id של ההרשמה
     */
    public String getId() {
        return id;
    }

    /**
     * מגדיר את מזהה המסמך הייחודי.
     * @param id מזהה המסמך לשמירה
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * מחזיר את מזהה הפעילות שאליה המשתמש נרשם.
     * @return activityId
     */
    public String getActivityId() {
        return activityId;
    }

    /**
     * מגדיר את מזהה הפעילות שאליה המשתמש נרשם.
     * @param activityId מזהה הפעילות
     */
    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    /**
     * מחזיר את רשימת הימים שבהם מתקיימת הפעילות.
     * @return רשימת ימים
     */
    public List<String> getDays() {
        return days;
    }

    /**
     * מגדיר את רשימת הימים שבהם מתקיימת הפעילות.
     * @param days רשימת ימים לשמירה
     */
    public void setDays(List<String> days) {
        this.days = days;
    }

    /**
     * מחזיר את תחום הפעילות (מדע, חברה, יצירה).
     * @return domain של הפעילות
     */
    public String getDomain() {
        return domain;
    }

    /**
     * מגדיר את תחום הפעילות (מדע, חברה, יצירה).
     * @param domain תחום הפעילות
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * מחזיר את תאריך ושעת ההרשמה.
     * @return Timestamp של ההרשמה
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * מגדיר את תאריך ושעת ההרשמה.
     * @param timestamp תאריך ושעה לשמירה
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
