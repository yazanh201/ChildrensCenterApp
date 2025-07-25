package com.example.childrenscenterapp2.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;

import java.util.List;
import java.util.Map;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {

    // רשימת כל הביקורות (feedbacks), כל פריט הוא Map של שדות מהמסמך
    private List<Map<String, Object>> feedbackList;

    // בנאי – מקבל את רשימת הביקורות להצגה
    public FeedbackAdapter(List<Map<String, Object>> feedbackList) {
        this.feedbackList = feedbackList;
    }

    // יוצרת ViewHolder חדש ע"י ניפוח (inflate) של תצוגת שורה מתוך קובץ XML
    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feedback_row, parent, false);
        return new FeedbackViewHolder(view);
    }

    // מציגה את תוכן הביקורת עבור מיקום מסוים ברשימה
    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        // מקבל את הביקורת לפי אינדקס
        Map<String, Object> feedback = feedbackList.get(position);

        // שם הילד
        String childName = String.valueOf(feedback.get("childName"));

        // תגובת מדריך + ציון
        String guideComment = String.valueOf(feedback.get("feedbackComment"));
        String guideScore = feedback.get("feedbackScore") != null ? feedback.get("feedbackScore").toString() : null;
        if (guideComment == null || guideComment.equals("null")) guideComment = "אין תגובה";
        if (guideScore == null || guideScore.equals("null")) guideScore = "-";

        // תגובת הורה + ציון
        String parentComment = String.valueOf(feedback.get("parentComment"));
        String parentScore = feedback.get("parentScore") != null ? feedback.get("parentScore").toString() : null;
        if (parentComment == null || parentComment.equals("null")) parentComment = "אין תגובה";
        if (parentScore == null || parentScore.equals("null")) parentScore = "-";

        // עדכון התצוגה בשורת הביקורת
        holder.tvChildName.setText(" ילד: " + childName);
        holder.tvGuideFeedback.setText("📝 " + guideComment + "\n⭐ ציון: " + guideScore + "/10");
        holder.tvParentFeedback.setText("📝 " + parentComment + "\n⭐ ציון: " + parentScore + "/10");
    }

    // מחזיר את מספר הביקורות ברשימה
    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    // ViewHolder פנימי – מחזיק את הרפרנסים ל־TextView-ים של כל שורה
    public static class FeedbackViewHolder extends RecyclerView.ViewHolder {

        TextView tvChildName, tvGuideFeedback, tvParentFeedback;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChildName = itemView.findViewById(R.id.tvChildName);
            tvGuideFeedback = itemView.findViewById(R.id.tvGuideFeedback);
            tvParentFeedback = itemView.findViewById(R.id.tvParentFeedback);
        }
    }
}
