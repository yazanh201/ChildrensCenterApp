package com.example.childrenscenterapp2.ui.guide;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.data.models.ActivityModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class GuideFragment extends Fragment {

    private RecyclerView recyclerViewActivities;
    private GuideActivitiesAdapter adapter;
    private List<ActivityModel> activityList = new ArrayList<>();

    // Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference activitiesRef = db.collection("activities");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_guide_home, container, false);

        recyclerViewActivities = view.findViewById(R.id.recyclerViewActivities);
        recyclerViewActivities.setLayoutManager(new LinearLayoutManager(getContext()));

        // יוצר אדפטר בלי פרמטרים מיותרים
        adapter = new GuideActivitiesAdapter(activityList);
        recyclerViewActivities.setAdapter(adapter);

        loadActivities();

        return view;
    }

    private void loadActivities() {
        activitiesRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    activityList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        ActivityModel activity = doc.toObject(ActivityModel.class);
                        activityList.add(activity);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("GuideFragment", "Error loading activities", e));
    }
}
