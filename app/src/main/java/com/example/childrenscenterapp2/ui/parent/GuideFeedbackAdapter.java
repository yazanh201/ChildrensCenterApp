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

public class GuideFeedbackAdapter extends RecyclerView.Adapter<GuideFeedbackAdapter.FeedbackViewHolder> {

    private List<Map<String, Object>> feedbackList;

    public GuideFeedbackAdapter(List<Map<String, Object>> feedbackList) {
        this.feedbackList = feedbackList;
    }

    public void setFeedbackList(List<Map<String, Object>> newList) {
        this.feedbackList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_guide_feedback, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        Map<String, Object> feedback = feedbackList.get(position);

        String childName = (String) feedback.get("childName");
        String comment = (String) feedback.get("feedbackComment");
        Object scoreObj = feedback.get("feedbackScore");

        String scoreStr = (scoreObj instanceof Number) ? String.valueOf(scoreObj) : "לא זמין";

        holder.tvChildName.setText("ילד: " + (childName != null ? childName : "לא ידוע"));
        holder.tvScore.setText("ציון: " + scoreStr);
        holder.tvComment.setText("הערה: " + (comment != null ? comment : "אין"));

        Log.d("GuideFeedbackAdapter", "🧩 שורה #" + position + " | feedback = " + feedback);
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

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
