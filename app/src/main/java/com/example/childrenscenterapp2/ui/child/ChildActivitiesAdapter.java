package com.example.childrenscenterapp2.ui.child;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.data.models.ActivityModel;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * {@code ChildActivitiesAdapter} – אדפטר מותאם להצגת רשימת פעילויות עבור ילדים.
 * <p>
 * תפקיד המחלקה:
 * <ul>
 *   <li>מציגה רשימת פעילויות מותאמות לילדים ב-RecyclerView.</li>
 *   <li>מאפשרת רישום לפעילויות תוך בדיקת הגבלות:
 *       <ul>
 *         <li>בדיקה אם ההרשמה פתוחה לפעילות.</li>
 *         <li>מניעת רישום כפול לאותה פעילות.</li>
 *         <li>מניעת רישום לפעילות בתחום זהה שכבר נרשם אליו.</li>
 *         <li>מניעת התנגשות ימים עם פעילויות אחרות שהילד רשום אליהן.</li>
 *       </ul>
 *   </li>
 *   <li>מעדכנת את הנתונים הן באוסף הפעילויות והן באוסף ההרשמות של המשתמש.</li>
 * </ul>
 */
public class ChildActivitiesAdapter extends RecyclerView.Adapter<ChildActivitiesAdapter.ActivityViewHolder> {

    /** רשימת הפעילויות להצגה */
    private List<ActivityModel> activities;

    /** מזהה ושם ילד אופציונליים לשימוש במצב override (למשל הורה שמרשם ילד אחר) */
    private String overrideChildId = null;
    private String overrideChildName = null;

    /** האם להציג כפתור הרשמה */
    private boolean showRegisterButton = true;

    /**
     * בנאי האדפטר.
     *
     * @param activities         רשימת הפעילויות להצגה.
     * @param showRegisterButton האם להציג את כפתור ההרשמה.
     */
    public ChildActivitiesAdapter(List<ActivityModel> activities, boolean showRegisterButton) {
        this.activities = activities;
        this.showRegisterButton = showRegisterButton;
    }

    /**
     * הגדרת Override של מזהה ושם הילד במקרה של הרשמה מטעם אחר.
     *
     * @param childId   מזהה הילד.
     * @param childName שם הילד.
     */
    public void setChildOverride(String childId, String childName) {
        this.overrideChildId = childId;
        this.overrideChildName = childName;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_child, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityModel activity = activities.get(position);

        // הצגת פרטי הפעילות
        holder.tvName.setText(activity.getName());
        holder.tvDomain.setText("תחום: " + activity.getDomain());
        String daysString = android.text.TextUtils.join(", ", activity.getDays());
        holder.tvDays.setText("ימים: " + daysString);
        holder.tvAge.setText("גיל מתאים: " + activity.getMinAge() + "-" + activity.getMaxAge());

        if (showRegisterButton) {
            holder.btnRegister.setVisibility(View.VISIBLE);
            holder.btnRegister.setOnClickListener(v -> {

                // ✅ בדיקה אם ההרשמה פתוחה
                if (!activity.isRegistrationOpen()) {
                    Toast.makeText(holder.itemView.getContext(),
                            "ההרשמה לפעילות זו עדיין סגורה על ידי המנהל.", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser == null) {
                    Toast.makeText(holder.itemView.getContext(), "⚠️ לא מחובר למערכת", Toast.LENGTH_SHORT).show();
                    return;
                }

                // שימוש ב-override אם קיים, אחרת משתמש מחובר
                String childId = (overrideChildId != null) ? overrideChildId : currentUser.getUid();
                String childName = (overrideChildName != null) ? overrideChildName : currentUser.getEmail();

                String activityId = activity.getId();
                String domain = activity.getDomain();
                List<String> newDays = activity.getDays();

                if (activityId == null || newDays == null) {
                    Toast.makeText(holder.itemView.getContext(), "⚠️ שגיאה בנתוני פעילות", Toast.LENGTH_SHORT).show();
                    return;
                }

                // בדיקת האם הילד כבר רשום לפעילות בתחום זהה
                db.collection("users")
                        .document(childId)
                        .collection("registrations")
                        .get()
                        .addOnSuccessListener(regSnapshot -> {
                            boolean alreadyInDomain = false;


                            // בדיקה אם הילד כבר רשום לפעילות הזו עצמה
                            db.collection("activities")
                                    .document(activityId)
                                    .collection("registrations")
                                    .whereEqualTo("childId", childId)
                                    .get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        if (!querySnapshot.isEmpty()) {
                                            Toast.makeText(holder.itemView.getContext(),
                                                    "⚠️ כבר נרשמת לפעילות זו",
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            // בדיקת חפיפה בימים עם פעילויות אחרות
                                            db.collection("users")
                                                    .document(childId)
                                                    .collection("registrations")
                                                    .get()
                                                    .addOnSuccessListener(userRegs -> {
                                                        final boolean[] hasConflict = {false};
                                                        final int[] checksDone = {0};
                                                        int totalChecks = userRegs.size();

                                                        if (totalChecks == 0) {
                                                            registerActivity(db, holder, currentUser, activity, childId);
                                                            return;
                                                        }

                                                        for (DocumentSnapshot doc : userRegs.getDocuments()) {
                                                            String existingActivityId = doc.getString("activityId");

                                                            if (existingActivityId == null) {
                                                                checksDone[0]++;
                                                                if (checksDone[0] == totalChecks && !hasConflict[0]) {
                                                                    registerActivity(db, holder, currentUser, activity, childId);
                                                                }
                                                                continue;
                                                            }

                                                            db.collection("activities")
                                                                    .document(existingActivityId)
                                                                    .get()
                                                                    .addOnSuccessListener(activityDoc -> {
                                                                        List<String> existingDays = (List<String>) activityDoc.get("days");

                                                                        if (existingDays != null) {
                                                                            for (String day : existingDays) {
                                                                                if (newDays.contains(day)) {
                                                                                    hasConflict[0] = true;
                                                                                    Toast.makeText(holder.itemView.getContext(),
                                                                                            "⚠️ יש חפיפה בימים עם פעילות אחרת",
                                                                                            Toast.LENGTH_SHORT).show();
                                                                                    break;
                                                                                }
                                                                            }
                                                                        }

                                                                        checksDone[0]++;
                                                                        if (checksDone[0] == totalChecks && !hasConflict[0]) {
                                                                            registerActivity(db, holder, currentUser, activity, childId);
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        checksDone[0]++;
                                                                        if (checksDone[0] == totalChecks && !hasConflict[0]) {
                                                                            registerActivity(db, holder, currentUser, activity, childId);
                                                                        }
                                                                    });
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(holder.itemView.getContext(),
                                                                "שגיאה בבדיקת הרשמות קודמות", Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(e -> showError(holder, "שגיאה בבדיקת הרשמה"));
                        })
                        .addOnFailureListener(e -> showError(holder, "שגיאה בבדיקת תחומים קודמים"));
            });
        } else {
            holder.btnRegister.setVisibility(View.GONE);
        }
    }

    /**
     * הצגת הודעת שגיאה קצרה.
     *
     * @param holder  ViewHolder הנוכחי.
     * @param message הודעת השגיאה.
     */
    private void showError(ActivityViewHolder holder, String message) {
        Toast.makeText(holder.itemView.getContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * מבצע רישום של ילד לפעילות ומעדכן את הנתונים הן באוסף הפעילויות והן באוסף המשתמש.
     *
     * @param db       מופע Firestore.
     * @param holder   ViewHolder להצגת ההודעה.
     * @param user     המשתמש המחובר.
     * @param activity הפעילות לרישום.
     * @param childId  מזהה הילד הנרשם.
     */
    private void registerActivity(FirebaseFirestore db, ActivityViewHolder holder, FirebaseUser user,
                                  ActivityModel activity, String childId) {

        Map<String, Object> registrationData = new HashMap<>();
        registrationData.put("childId", childId);
        registrationData.put("childName", user.getEmail());
        registrationData.put("registeredAt", Timestamp.now());

        db.collection("activities")
                .document(activity.getId())
                .collection("registrations")
                .add(registrationData)
                .addOnSuccessListener(documentReference -> {

                    Map<String, Object> userReg = new HashMap<>();
                    userReg.put("activityId", activity.getId());
                    userReg.put("domain", activity.getDomain());
                    userReg.put("days", activity.getDays());
                    userReg.put("timestamp", Timestamp.now());

                    db.collection("users")
                            .document(childId)
                            .collection("registrations")
                            .document(activity.getId())
                            .set(userReg)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(holder.itemView.getContext(), "✅ נרשמת בהצלחה!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(holder.itemView.getContext(), "שגיאה בשמירה אצל המשתמש", Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(holder.itemView.getContext(), "שגיאה בהרשמה", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    /**
     * עדכון רשימת הפעילויות באדפטר.
     *
     * @param newList רשימת פעילויות חדשה.
     */
    public void setData(List<ActivityModel> newList) {
        this.activities = newList;
        notifyDataSetChanged();
    }

    /**
     * {@code ActivityViewHolder} – מחלקה פנימית להצגת פרטי פעילות ב-RecyclerView.
     */
    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDomain, tvDays, tvAge;
        Button btnRegister;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDomain = itemView.findViewById(R.id.tvDomain);
            tvDays = itemView.findViewById(R.id.tvDays);
            tvAge = itemView.findViewById(R.id.tvAge);
            btnRegister = itemView.findViewById(R.id.btnRegister);
        }
    }
}
