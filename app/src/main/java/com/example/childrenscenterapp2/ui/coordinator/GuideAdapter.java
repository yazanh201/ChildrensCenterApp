package com.example.childrenscenterapp2.ui.coordinator;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Adapter לרשימת מדריכים - שולף ישירות את השדות ממסד הנתונים ללא שימוש במחלקת User.
 */
public class GuideAdapter extends RecyclerView.Adapter<GuideAdapter.GuideViewHolder> {

    private final List<DocumentSnapshot> guideDocs;
    private final Context context;

    public GuideAdapter(List<DocumentSnapshot> guideDocs, Context context) {
        this.guideDocs = guideDocs;
        this.context = context;
    }

    @NonNull
    @Override
    public GuideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_guide, parent, false);
        return new GuideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuideViewHolder holder, int position) {
        DocumentSnapshot doc = guideDocs.get(position);

        String uid = doc.getId();
        String name = doc.getString("name");
        String specialization = doc.getString("specialization");
        String startDate = doc.getString("startDate");
        List<String> activities = (List<String>) doc.get("activities");

        holder.tvName.setText("שם: " + (name != null ? name : ""));
        holder.tvSpecialization.setText("תחום: " + (specialization != null ? specialization : ""));
        holder.tvStartDate.setText("תאריך התחלה: " + (startDate != null ? startDate : "לא נבחר"));
        holder.tvActivities.setText("פעילויות: " + (activities != null && !activities.isEmpty() ? String.join(", ", activities) : "לא צוינו"));

        // כפתור לקביעת תאריך התחלה
        holder.btnSetStartDate.setOnClickListener(v -> showDatePickerAndSave(holder, uid));

        //  כפתור מחיקה עם דיאלוג אישור
        holder.btnDeleteGuide.setOnClickListener(v -> showDeleteConfirmationDialog(uid, position));
    }

    private void showDatePickerAndSave(GuideViewHolder holder, String uid) {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(context,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    String selectedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime());

                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(uid)
                            .update("startDate", selectedDate)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "תאריך התחלה נשמר", Toast.LENGTH_SHORT).show();
                                holder.tvStartDate.setText("תאריך התחלה: " + selectedDate);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(context, "שגיאה בשמירת תאריך", Toast.LENGTH_SHORT).show());
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePicker.show();
    }

    // פונקציית דיאלוג אישור מחיקה
    private void showDeleteConfirmationDialog(String uid, int position) {
        new AlertDialog.Builder(context)
                .setTitle("אישור מחיקה")
                .setMessage(" האם אתה בטוח שברצונך למחוק את המדריך הזה?")
                .setPositiveButton("כן, מחק", (dialog, which) -> {
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(uid)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "המדריך נמחק", Toast.LENGTH_SHORT).show();
                                guideDocs.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, guideDocs.size());
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(context, "שגיאה במחיקה", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("ביטול", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public int getItemCount() {
        return guideDocs.size();
    }

    public void setGuideDocuments(List<DocumentSnapshot> newDocs) {
        guideDocs.clear();
        guideDocs.addAll(newDocs);
        notifyDataSetChanged();
    }

    public static class GuideViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSpecialization, tvStartDate, tvActivities;
        Button btnSetStartDate, btnDeleteGuide;

        public GuideViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvGuideName);
            tvSpecialization = itemView.findViewById(R.id.tvGuideSpecialization);
            tvStartDate = itemView.findViewById(R.id.tvGuideStartDate);
            tvActivities = itemView.findViewById(R.id.tvGuideActivities);
            btnSetStartDate = itemView.findViewById(R.id.btnSetStartDate);
            btnDeleteGuide = itemView.findViewById(R.id.btnDeleteGuide);
        }
    }
}
