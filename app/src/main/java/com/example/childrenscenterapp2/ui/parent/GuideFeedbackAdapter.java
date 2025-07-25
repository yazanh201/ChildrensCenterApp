package com.example.childrenscenterapp2.ui.parent;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;

import java.util.List;
import java.util.Map;

/**
 * Adapter להצגת רשימת משובים שניתנו על־ידי מדריכים (Guide Feedback) עבור כל ילד בפעילות.
 * כל שורה מכילה: שם הילד, ציון, והערה מהמדריך.
 */
public class GuideFeedbackAdapter extends RecyclerView.Adapter<GuideFeedbackAdapter.FeedbackViewHolder> {

    private List<Map<String, Object>> feedbackList; // רשימת המשובים שנשלפו מה-Firestore

    /**
     * בנאי – מקבל את רשימת המשובים להצגה.
     * @param feedbackList רשימת map-ים עם המידע לכל משוב
     */
    public GuideFeedbackAdapter(List<Map<String, Object>> feedbackList) {
        this.feedbackList = feedbackList;
    }

    /**
     * עדכון הרשימה לאחר שליפה חדשה מה-Database
     * @param newList רשימה חדשה של משובים
     */
    public void setFeedbackList(List<Map<String, Object>> newList) {
        this.feedbackList = newList;
        notifyDataSetChanged();
    }

    /**
     * יצירת ViewHolder חדש לכל כרטיס משוב
     */
    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_guide_feedback, parent, false);
        return new FeedbackViewHolder(view);
    }

    /**
     * קישור הנתונים מה־Map לתצוגת הכרטיס הספציפי
     */
    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        Map<String, Object> feedback = feedbackList.get(position);

        // שליפת נתונים מה-Map
        String childName = (String) feedback.get("childName");
        String comment = (String) feedback.get("feedbackComment");
        Object scoreObj = feedback.get("feedbackScore");

        // המרה לציון כתוב (למקרה שהערך לא מספר)
        String scoreStr = (scoreObj instanceof Number) ? String.valueOf(scoreObj) : "לא זמין";

        // הצגת המידע
        holder.tvChildName.setText("ילד: " + (childName != null ? childName : "לא ידוע"));
        holder.tvScore.setText("ציון: " + scoreStr);
        holder.tvComment.setText("הערה: " + (comment != null ? comment : "אין"));

        // לוג דיבאגינג
        Log.d("GuideFeedbackAdapter", "🧩 שורה #" + position + " | feedback = " + feedback);
    }

    /**
     * מספר הכרטיסים ברשימה
     */
    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    /**
     * מחזיק התצוגה עבור כל כרטיס משוב בודד
     */
    static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        TextView tvChildName, tvScore, tvComment;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChildName = itemView.findViewById(R.id.tvChildName);
            tvScore = itemView.findViewById(R.id.tvFeedbackScore);
            tvComment = itemView.findViewById(R.id.tvFeedbackComment);
        }
    }
}
