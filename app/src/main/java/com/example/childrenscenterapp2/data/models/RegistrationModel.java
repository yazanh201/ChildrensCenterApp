package com.example.childrenscenterapp2.data.models;

import com.google.firebase.Timestamp;

/**
 * מודל הרשמה לפעילות במרכז הילדים.
 * מייצג את הנתונים של הרשמת ילד לפעילות, כולל פרטי הילד, ציונים ומשוב מההורים.
 * משמש לשמירה בשליפה מ-Firestore וב-SQLite.
 */
public class RegistrationModel {

    private String id; // מזהה ייחודי של ההרשמה
    private String activityId; // מזהה הפעילות אליה ההרשמה שייכת
    private String childId; // מזהה הילד שנרשם
    private String childName; // שם הילד שנרשם
    private String parentComment; // הערת הורה על הפעילות
    private int parentScore; // ציון שההורה נתן לפעילות
    private String feedbackComment; // הערת משוב מהמדריך או המערכת
    private int feedbackScore; // ציון משוב מהפעילות
    private Object registeredAt; // תאריך ושעת ההרשמה (יכול להיות Timestamp או String)

    /**
     * קונסטרקטור ריק נדרש על ידי Firestore לשליפה אוטומטית של אובייקטים.
     */
    public RegistrationModel() {}

    /**
     * קונסטרקטור מלא ליצירת הרשמה עם כל השדות.
     *
     * @param id מזהה ההרשמה
     * @param activityId מזהה הפעילות
     * @param childId מזהה הילד
     * @param childName שם הילד
     * @param parentComment הערת הורה
     * @param parentScore ציון הורה
     * @param feedbackComment הערת משוב
     * @param feedbackScore ציון משוב
     * @param registeredAt תאריך הרשמה (Object/Firebase Timestamp)
     */
    public RegistrationModel(String id, String activityId, String childId, String childName,
                             String parentComment, int parentScore,
                             String feedbackComment, int feedbackScore,
                             Object registeredAt) {
        this.id = id;
        this.activityId = activityId;
        this.childId = childId;
        this.childName = childName;
        this.parentComment = parentComment;
        this.parentScore = parentScore;
        this.feedbackComment = feedbackComment;
        this.feedbackScore = feedbackScore;
        this.registeredAt = registeredAt;
    }

    // ===================== Getters ו-Setters =====================

    /**
     * @return מזהה ההרשמה הייחודי
     */
    public String getId() {
        return id;
    }

    /**
     * @param id מזהה ההרשמה הייחודי לשמירה
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return מזהה הפעילות אליה ההרשמה שייכת
     */
    public String getActivityId() {
        return activityId;
    }

    /**
     * @param activityId מזהה הפעילות לשמירה
     */
    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    /**
     * @return מזהה הילד שנרשם
     */
    public String getChildId() {
        return childId;
    }

    /**
     * @param childId מזהה הילד לשמירה
     */
    public void setChildId(String childId) {
        this.childId = childId;
    }

    /**
     * @return שם הילד שנרשם
     */
    public String getChildName() {
        return childName;
    }

    /**
     * @param childName שם הילד לשמירה
     */
    public void setChildName(String childName) {
        this.childName = childName;
    }

    /**
     * @return הערת ההורה על הפעילות
     */
    public String getParentComment() {
        return parentComment;
    }

    /**
     * @param parentComment הערת הורה לשמירה
     */
    public void setParentComment(String parentComment) {
        this.parentComment = parentComment;
    }

    /**
     * @return הציון שההורה נתן לפעילות
     */
    public int getParentScore() {
        return parentScore;
    }

    /**
     * @param parentScore ציון הורה לשמירה
     */
    public void setParentScore(int parentScore) {
        this.parentScore = parentScore;
    }

    /**
     * @return הערת המשוב על הפעילות
     */
    public String getFeedbackComment() {
        return feedbackComment;
    }

    /**
     * @param feedbackComment הערת משוב לשמירה
     */
    public void setFeedbackComment(String feedbackComment) {
        this.feedbackComment = feedbackComment;
    }

    /**
     * @return הציון שניתן למשוב
     */
    public int getFeedbackScore() {
        return feedbackScore;
    }

    /**
     * @param feedbackScore ציון משוב לשמירה
     */
    public void setFeedbackScore(int feedbackScore) {
        this.feedbackScore = feedbackScore;
    }

    /**
     * @return תאריך ושעת ההרשמה (יכול להיות Timestamp)
     */
    public Object getRegisteredAt() {
        return registeredAt;
    }

    /**
     * @param registeredAt תאריך ושעת ההרשמה לשמירה
     */
    public void setRegisteredAt(Object registeredAt) {
        this.registeredAt = registeredAt;
    }
}
