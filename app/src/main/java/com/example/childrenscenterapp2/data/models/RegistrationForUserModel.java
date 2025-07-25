package com.example.childrenscenterapp2.data.models;

import com.google.firebase.Timestamp;
import java.util.List;

public class RegistrationForUserModel {

    private String id;  // מזהה המסמך (Document ID)
    private String activityId;
    private List<String> days;
    private String domain;
    private Timestamp timestamp;

    public RegistrationForUserModel() {
        // קונסטרקטור ריק ל-Firebase
    }

    // Getters & Setters

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

    public List<String> getDays() {
        return days;
    }
    public void setDays(List<String> days) {
        this.days = days;
    }

    public String getDomain() {
        return domain;
    }
    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
