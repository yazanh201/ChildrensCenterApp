package com.example.childrenscenterapp2.ui.guide;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;

import com.example.childrenscenterapp2.R;

/**
 * מחלקה סטטית המציגה דיאלוג להזנת משוב (דירוג + תגובה מילולית)
 * משמשת מדריכים שרוצים לתת פידבק למשתתף
 */
public class DialogFeedbackInput {

    /**
     * ממשק המאפשר לקלוט את התוצאה מהמשתמש
     */
    public interface FeedbackCallback {
        void onFeedbackSubmitted(float score, String comment);
    }

    /**
     * מציג דיאלוג עם שדות להזנת משוב:
     * - דירוג (RatingBar)
     * - הערה חופשית  (EditText)
     *
     * @param context  הקונטקסט שבו מוצג הדיאלוג (לרוב Activity או Fragment)
     * @param callback פונקציה שתקבל את הנתונים שנשלחו אם המשתמש לחץ על "שלח"
     */
    public static void showDialog(Context context, FeedbackCallback callback) {
        // ניפוח תצוגת הדיאלוג מתוך קובץ XML
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_feedback_input, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText etComment = dialogView.findViewById(R.id.etFeedbackComment);

        // יצירת דיאלוג עם כפתורי שליחה וביטול
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("הוסף משוב")
                .setView(dialogView)
                .setPositiveButton("שלח", (d, which) -> {
                    float rating = ratingBar.getRating();
                    String comment = etComment.getText().toString().trim();
                    if (callback != null) {
                        callback.onFeedbackSubmitted(rating, comment);
                    }
                })
                .setNegativeButton("בטל", null)
                .create();

        dialog.show(); // הצגת הדיאלוג בפועל
    }
}
