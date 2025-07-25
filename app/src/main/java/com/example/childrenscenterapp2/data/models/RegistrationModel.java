package com.example.childrenscenterapp2.data.models;

import com.google.firebase.Timestamp;

public class RegistrationModel {
    private String id;
    private String activityId;
    private String childId;
    private String childName;
    private String parentComment;
    private int parentScore;
    private String feedbackComment;
    private int feedbackScore;
    private Object registeredAt; // ✅ היה String → הפכנו ל־Object או Timestamp

    // ✅ קונסטרקטור ריק (נדרש על ידי Firestore)
    public RegistrationModel() {}

    // ✅ קונסטרקטור עם שדות אם תרצה להשתמש
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

    // ✅ Getters ו־Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getChildId() {
        return childId;
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public String getParentComment() {
        return parentComment;
    }

    public void setParentComment(String parentComment) {
        this.parentComment = parentComment;
    }

    public int getParentScore() {
        return parentScore;
    }

    public void setParentScore(int parentScore) {
        this.parentScore = parentScore;
    }

    public String getFeedbackComment() {
        return feedbackComment;
    }

    public void setFeedbackComment(String feedbackComment) {
        this.feedbackComment = feedbackComment;
    }

    public int getFeedbackScore() {
        return feedbackScore;
    }

    public void setFeedbackScore(int feedbackScore) {
        this.feedbackScore = feedbackScore;
    }

    public Object getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Object registeredAt) {
        this.registeredAt = registeredAt;
    }
}
