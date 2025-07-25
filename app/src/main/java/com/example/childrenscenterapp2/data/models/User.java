package com.example.childrenscenterapp2.data.models;

/**
 * מודל מייצג משתמש במערכת מרכז הילדים.
 * תומך בסוגי משתמשים שונים: מנהל, הורה, ילד, מדריך ורכז.
 * כולל נתונים כלליים + שדות מיוחדים למדריך ולהורה.
 */
public class User {

    private String uid;             // מזהה ייחודי של המשתמש (Firebase UID)
    private String name;            // שם המשתמש
    private String email;           // כתובת אימייל של המשתמש
    private String type;            // סוג המשתמש: admin, parent, child, guide, coordinator
    private String specialization;  // תחום התמחות (רלוונטי למדריך)
    private String idNumber;        // מספר ת"ז (רלוונטי להורה וילד)

    /**
     * 🔧 קונסטרקטור ריק – חובה ל-Firebase בעת שליפת נתונים.
     */
    public User() {
    }

    /**
     * ✅ קונסטרקטור כללי – מתאים לכל סוגי המשתמשים.
     *
     * @param uid מזהה ייחודי של המשתמש (UID מ-Firebase)
     * @param name שם המשתמש
     * @param email כתובת אימייל
     * @param type סוג המשתמש (admin, parent, child, guide, coordinator)
     * @param specialization תחום התמחות (למדריך בלבד, אחרת ריק)
     * @param idNumber מספר ת"ז (להורה/ילד בלבד, אחרת ריק)
     */
    public User(String uid, String name, String email, String type, String specialization, String idNumber) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.type = type;
        this.specialization = specialization != null ? specialization : "";
        this.idNumber = idNumber != null ? idNumber : "";
    }

    /**
     * ✅ קונסטרקטור מינימלי – יוצר משתמש רגיל ללא שדות נוספים.
     */
    public User(String uid, String name, String email, String type) {
        this(uid, name, email, type, "", "");
    }

    /**
     * ✅ קונסטרקטור סטטי ליצירת מדריך.
     *
     * @param uid UID של המדריך
     * @param name שם המדריך
     * @param email אימייל
     * @param specialization תחום התמחות
     * @return אובייקט User מסוג מדריך
     */
    public static User createGuide(String uid, String name, String email, String specialization) {
        return new User(uid, name, email, "מדריך", specialization, "");
    }

    /**
     * ✅ קונסטרקטור סטטי ליצירת הורה.
     *
     * @param uid UID של ההורה
     * @param name שם ההורה
     * @param email אימייל
     * @param idNumber מספר ת"ז
     * @return אובייקט User מסוג הורה
     */
    public static User createParent(String uid, String name, String email, String idNumber) {
        return new User(uid, name, email, "הורה", "", idNumber);
    }

    /**
     * ✅ קונסטרקטור סטטי ליצירת ילד.
     *
     * @param uid UID של הילד
     * @param name שם הילד
     * @param email אימייל
     * @param idNumber מספר ת"ז
     * @return אובייקט User מסוג ילד
     */
    public static User createChild(String uid, String name, String email, String idNumber) {
        return new User(uid, name, email, "ילד", "", idNumber);
    }

    // ==================== Getters ו-Setters ====================

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
