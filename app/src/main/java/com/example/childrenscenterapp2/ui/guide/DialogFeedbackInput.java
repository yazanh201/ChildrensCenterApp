package com.example.childrenscenterapp2.ui.guide;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;

import com.example.childrenscenterapp2.R;

public class DialogFeedbackInput {

    public interface FeedbackCallback {
        void onFeedbackSubmitted(float score, String comment);
    }

    public static void showDialog(Context context, FeedbackCallback callback) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_feedback_input, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText etComment = dialogView.findViewById(R.id.etFeedbackComment);

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

        dialog.show();
    }
}
