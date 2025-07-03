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

import java.util.List;

/**
 * אדפטר להצגת פעילויות ברשימה בצורה מעוצבת ומודרנית
 */
public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ActivityViewHolder> {

    private List<ActivityModel> activityList;
    private OnActivityClickListener listener;

    // ממשק לתקשורת עם לחצני הפעולה (עריכה, מחיקה)
    public interface OnActivityClickListener {
        void onEdit(ActivityModel activity);
        void onDelete(ActivityModel activity);
    }

    public ActivitiesAdapter(List<ActivityModel> activityList, OnActivityClickListener listener) {
        this.activityList = activityList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // יצירת תצוגת פריט חדש מרכיב ה-XML המעוצב
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityModel activity = activityList.get(position);
        holder.bind(activity);
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    // עדכון הרשימה והתרעננות של RecyclerView
    public void setData(List<ActivityModel> newList) {
        activityList = newList;
        notifyDataSetChanged();
    }

    // מחזיק תצוגת פריט יחיד
    class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDomain, tvDescription, tvAges, tvMonth;
        Button btnEdit, btnDelete;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvActivityName);
            tvDomain = itemView.findViewById(R.id.tvDomain);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAges = itemView.findViewById(R.id.tvAges);
            tvMonth = itemView.findViewById(R.id.tvMonth);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(ActivityModel activity) {
            tvName.setText(activity.getName());
            tvDomain.setText("תחום: " + activity.getDomain());
            tvDescription.setText("תיאור: " + activity.getDescription());
            tvAges.setText("גילאים: " + activity.getMinAge() + " - " + activity.getMaxAge());
            tvMonth.setText("חודש: " + activity.getMonth());

            btnEdit.setOnClickListener(v -> listener.onEdit(activity));
            btnDelete.setOnClickListener(v -> listener.onDelete(activity));
        }
    }
}
