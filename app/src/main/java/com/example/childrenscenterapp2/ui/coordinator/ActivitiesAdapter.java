package com.example.childrenscenterapp2.ui.coordinator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.data.models.ActivityModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code ActivitiesAdapter} - אדפטר מותאם אישית עבור {@link RecyclerView} להצגת רשימת פעילויות.
 * <p>
 * תפקידו של האדפטר:
 * <ul>
 *   <li>להמיר אובייקטי {@link ActivityModel} לפריטי תצוגה (ViewHolder).</li>
 *   <li>לאפשר עריכה ומחיקה של פעילות באמצעות ממשק {@link OnActivityClickListener}.</li>
 *   <li>להציג סטטיסטיקות כגון מספר משתתפים ודירוג ממוצע לכל פעילות.</li>
 *   <li>לבצע מיון של הרשימה על פי קריטריונים שונים (מספר משתתפים, דירוג ממוצע).</li>
 * </ul>
 */
public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ActivityViewHolder> {

    /** הרשימה הנוכחית שמוצגת ב-RecyclerView */
    private List<ActivityModel> activityList;

    /** עותק מלא של הרשימה המקורית לשחזור הנתונים במידת הצורך */
    private List<ActivityModel> fullList;

    /** מאזין לאירועי עריכה/מחיקה */
    private OnActivityClickListener listener;

    /** מפה לשמירת מספר משתתפים לפי מזהה פעילות */
    private final Map<String, Integer> participantCounts = new HashMap<>();

    /** מפה לשמירת דירוג ממוצע לפי מזהה פעילות */
    private final Map<String, Double> averageScores = new HashMap<>();

    /**
     * ממשק תקשורת עם ה-Activity/Fragment שמכיל את האדפטר.
     * משמש להעברת אירועים כגון עריכה ומחיקה של פעילות.
     */
    public interface OnActivityClickListener {
        /**
         * מופעל כאשר נלחץ כפתור עריכה על פעילות.
         * @param activity האובייקט של הפעילות הנבחרת
         */
        void onEdit(ActivityModel activity);

        /**
         * מופעל כאשר נלחץ כפתור מחיקה על פעילות.
         * @param activity האובייקט של הפעילות הנבחרת
         */
        void onDelete(ActivityModel activity);
    }

    /**
     * בנאי לאדפטר.
     *
     * @param activityList רשימת פעילויות ראשונית.
     * @param listener מאזין לאירועי עריכה/מחיקה.
     */
    public ActivitiesAdapter(List<ActivityModel> activityList, OnActivityClickListener listener) {
        this.activityList = activityList;
        this.fullList = new ArrayList<>(activityList); // שמירת עותק מלא לשחזור עתידי
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // יצירת ViewHolder מתוך קובץ ה-XML של פריט הפעילות
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        // קישור הנתונים של פעילות מסוימת ל-ViewHolder
        ActivityModel activity = activityList.get(position);
        holder.bind(activity);
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    /**
     * עדכון הנתונים המוצגים באדפטר.
     * @param newList רשימה חדשה של פעילויות להחלפת הנתונים הקיימים.
     */
    public void setData(List<ActivityModel> newList) {
        this.activityList = newList;
        this.fullList = new ArrayList<>(newList); // שמירת עותק מלא לעבודה עתידית
        notifyDataSetChanged();
    }

    /**
     * עדכון סטטיסטיקות עבור פעילות מסוימת (משתמש חיצוני יכול לקרוא לפונקציה זו).
     *
     * @param activityId מזהה פעילות.
     * @param participants מספר המשתתפים בפעילות.
     * @param avgScore הדירוג הממוצע של הפעילות.
     */
    public void updateStatsForActivity(String activityId, int participants, double avgScore) {
        participantCounts.put(activityId, participants);
        averageScores.put(activityId, avgScore);
        notifyDataSetChanged();
    }

    /**
     * מיון הרשימה לפי מספר משתתפים בסדר יורד (מהרב למעט).
     */
    public void sortByParticipantsDescending() {
        activityList.sort((a1, a2) -> {
            int p1 = participantCounts.getOrDefault(a1.getId(), 0);
            int p2 = participantCounts.getOrDefault(a2.getId(), 0);
            return Integer.compare(p2, p1); // גדול -> קטן
        });
        notifyDataSetChanged();
    }

    /**
     * מיון לפי דירוג ממוצע בסדר יורד והגבלת התוצאה למספר מקסימלי של פריטים.
     *
     * @param topLimit מספר מקסימלי של פעילויות להציג (לדוגמה: 10).
     */
    public void sortByTopAverageScore(int topLimit) {
        activityList.sort((a1, a2) -> {
            double s1 = averageScores.getOrDefault(a1.getId(), 0.0);
            double s2 = averageScores.getOrDefault(a2.getId(), 0.0);
            return Double.compare(s2, s1); // גבוה -> נמוך
        });

        // שמירה רק על topLimit הפעילויות הראשונות
        if (activityList.size() > topLimit) {
            activityList = new ArrayList<>(activityList.subList(0, topLimit));
        }

        notifyDataSetChanged();
    }

    /**
     * איפוס הרשימה למצב המקורי (מוחק מיון או סינון שנעשה).
     */
    public void resetData() {
        this.activityList = new ArrayList<>(fullList);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder פנימי שמנהל את תצוגת הפריט הבודד ברשימה.
     */
    class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDomain, tvMonth, tvParticipants, tvAverageScore;
        Button btnEdit, btnDelete;

        /**
         * בנאי ל-ViewHolder. מקשר בין רכיבי ה-XML לשדות המחלקה.
         * @param itemView התצוגה הבודדת של הפריט.
         */
        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDomain = itemView.findViewById(R.id.tvDomain);
            tvMonth = itemView.findViewById(R.id.tvMonth);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            tvParticipants = itemView.findViewById(R.id.tvParticipantsCount);
            tvAverageScore = itemView.findViewById(R.id.tvFeedbackAvg);
        }

        /**
         * קישור הנתונים של {@link ActivityModel} לתצוגה.
         *
         * @param activity האובייקט שמכיל את פרטי הפעילות.
         */
        public void bind(ActivityModel activity) {
            tvName.setText(activity.getName());
            tvDomain.setText("תחום: " + activity.getDomain());
            tvMonth.setText("חודש: " + activity.getMonth());

            // אירועי לחיצה לעריכה ומחיקה
            btnEdit.setOnClickListener(v -> listener.onEdit(activity));
            btnDelete.setOnClickListener(v -> listener.onDelete(activity));

            // שליפת נתוני הסטטיסטיקות והצגתם
            int participants = participantCounts.getOrDefault(activity.getId(), 0);
            double avgScore = averageScores.getOrDefault(activity.getId(), 0.0);

            tvParticipants.setText("משתתפים: " + participants);
            tvAverageScore.setText("דירוג ממוצע: " + String.format("%.1f", avgScore));
        }
    }
}
