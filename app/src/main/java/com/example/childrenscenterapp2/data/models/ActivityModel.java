package com.example.childrenscenterapp2.data.models;

import com.google.firebase.Timestamp;

import java.util.List;
import java.util.Map;

/**
 * מודל פעילות במרכז הילדים – כולל תכונות עבור Firestore + SQLite
 */
public class ActivityModel {

    private String id; // מזהה ייחודי לפעילות
    private String name; // שם הפעילות
    private String description; // תיאור הפעילות
    private String domain; // תחום: מדע, חברה, יצירה
    private int minAge; // גיל מינימלי
    private int maxAge; // גיל מקסימלי
    private List<String> days; // ימים בשבוע (כגון: ראשון, שלישי)
    private int maxParticipants; // מספר משתתפים מקסימלי
    private Timestamp createdAt; // תאריך יצירה
    private boolean isOneTime; // האם חד פעמית
    private boolean approved; // האם מאושרת (פעילות רגילה = true, חד פעמית = false עד אישור)
    private String guideName; // מזהה המדריך המשויך
    private String month; // לדוגמה: 07-2025

    private transient Map<String, Object> metadata;

    // קונסטרקטור ריק נדרש על ידי Firebase
    public ActivityModel() {}

    // קונסטרקטור מלא
    public ActivityModel(String id, String name, String description, String domain,
                         int minAge, int maxAge, List<String> days, int maxParticipants,
                         Timestamp createdAt, boolean isOneTime, boolean approved,
                         String guideName, String month) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.domain = domain;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.days = days;
        this.maxParticipants = maxParticipants;
        this.createdAt = createdAt;
        this.isOneTime = isOneTime;
        this.approved = approved;
        this.guideName = guideName;  // ← שורה זו צריכה להתאים לפרמטר
        this.month = month;
    }



    // קונסטרקטור חלקי – לשימוש ב-SQLite כשאין את כל השדות
    public ActivityModel(String id, String name, String description, String domain,
                         int minAge, int maxAge, List<String> days, int maxParticipants) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.domain = domain;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.days = days;
        this.maxParticipants = maxParticipants;

        // ערכים ברירת מחדל לשדות שאינם רלוונטיים כאן
        this.createdAt = null;
        this.isOneTime = false;
        this.approved = true;
        this.guideName = null;
        this.month = null;
    }


    // Getters ו-Setters (חובה עבור Firebase ו-SQLite)

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getMinAge() {
        return minAge;
    }

    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public List<String> getDays() {
        return days;
    }

    public void setDays(List<String> days) {
        this.days = days;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isOneTime() {
        return isOneTime;
    }

    public void setOneTime(boolean oneTime) {
        isOneTime = oneTime;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getGuideName() {
        return guideName;
    }

    public void setGuideName(String guideId) {
        this.guideName = guideId;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }


    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
