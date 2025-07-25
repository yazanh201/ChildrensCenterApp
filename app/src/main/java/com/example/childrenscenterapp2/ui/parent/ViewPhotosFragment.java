package com.example.childrenscenterapp2.ui.parent;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.childrenscenterapp2.R;
import com.google.firebase.firestore.*;

import java.util.*;

/**
 * פרגמנט להצגת תמונות מפוענחות של פעילות מסוימת (לפי activityId),
 * כולל אפשרות לסינון לפי תאריך.
 */
public class ViewPhotosFragment extends Fragment {

    private static final String ARG_ACTIVITY_ID = "activityId";
    private String activityId;

    private RecyclerView recyclerView;
    private PhotoDisplayAdapter adapter;
    private List<Bitmap> imageList = new ArrayList<>(); // תמונות מוצגות
    private FirebaseFirestore firestore;
    private Button btnSelectDate;

    // יצירת מופע של הפרגמנט עם מזהה פעילות
    public static ViewPhotosFragment newInstance(String activityId) {
        ViewPhotosFragment fragment = new ViewPhotosFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ACTIVITY_ID, activityId);
        fragment.setArguments(args);
        return fragment;
    }

    // יצירת View וטעינת כל התמונות כברירת מחדל
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_photos, container, false);

        firestore = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.recyclerViewPhotos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PhotoDisplayAdapter(imageList);
        recyclerView.setAdapter(adapter);

        btnSelectDate = view.findViewById(R.id.btnSelectDate);
        btnSelectDate.setOnClickListener(v -> openDatePicker());

        // אם הגיעו פרמטרים - טען את התמונות
        if (getArguments() != null) {
            activityId = getArguments().getString(ARG_ACTIVITY_ID);
            loadAllPhotos(); // ברירת מחדל - הצגת כל התמונות
        }

        return view;
    }

    // פתיחת דיאלוג לבחירת תאריך להצגת תמונות מאותו יום
    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    btnSelectDate.setText("תמונות מתאריך: " + dayOfMonth + "/" + (month + 1) + "/" + year);
                    fetchPhotosByDate(selected.getTime());
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    // שליפה של כל התמונות בפעילות
    private void loadAllPhotos() {
        firestore.collection("activities")
                .document(activityId)
                .collection("photos")
                .get()
                .addOnSuccessListener(this::handleSnapshot)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "❌ שגיאה בטעינת התמונות", Toast.LENGTH_SHORT).show();
                    Log.e("ViewPhotosFragment", "🔥 שגיאה בטעינה", e);
                });
    }

    // שליפה של תמונות לפי תאריך מסוים (בין חצות עד חצות למחרת)
    private void fetchPhotosByDate(Date selectedDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfDay = cal.getTime();

        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date endOfDay = cal.getTime();

        firestore.collection("activities")
                .document(activityId)
                .collection("photos")
                .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                .whereLessThan("timestamp", endOfDay)
                .get()
                .addOnSuccessListener(this::handleSnapshot)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "❌ שגיאה בסינון התמונות", Toast.LENGTH_SHORT).show();
                    Log.e("ViewPhotosFragment", "⚠️ שגיאה בסינון", e);
                });
    }

    // טיפול בתוצאות של תמונות: המרה מ־Base64 ל־Bitmap והצגה
    private void handleSnapshot(QuerySnapshot snapshot) {
        imageList.clear();
        for (DocumentSnapshot doc : snapshot) {
            String base64 = doc.getString("imageBase64");
            if (base64 == null || base64.isEmpty()) continue;

            try {
                byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                if (bitmap != null) {
                    imageList.add(bitmap);
                }
            } catch (Exception e) {
                Log.e("ViewPhotosFragment", "❌ שגיאה בפענוח תמונה", e);
            }
        }

        if (imageList.isEmpty()) {
            Toast.makeText(getContext(), "לא נמצאו תמונות לתאריך זה", Toast.LENGTH_SHORT).show();
        }

        adapter.notifyDataSetChanged(); // רענון הרשימה
    }
}
