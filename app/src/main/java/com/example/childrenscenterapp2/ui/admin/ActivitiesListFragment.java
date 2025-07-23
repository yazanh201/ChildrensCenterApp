package com.example.childrenscenterapp2.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActivitiesListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ActivitiesSimpleAdapter adapter;
    private final List<Map<String, Object>> activityList = new ArrayList<>();

    public ActivitiesListFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activities_list_admin, container, false);

        recyclerView = view.findViewById(R.id.recyclerActivities);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // יצירת האדפטר עם הנתונים (Map) ומנהל ה־Fragment
        adapter = new ActivitiesSimpleAdapter(activityList, requireActivity().getSupportFragmentManager());
        recyclerView.setAdapter(adapter);

        fetchActivitiesFromFirebase();
        return view;
    }

    private void fetchActivitiesFromFirebase() {
        FirebaseFirestore.getInstance().collection("activities")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    activityList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Map<String, Object> data = doc.getData();
                        data.put("id", doc.getId()); // שומרים את מזהה המסמך
                        activityList.add(data);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "שגיאה בטעינה: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
