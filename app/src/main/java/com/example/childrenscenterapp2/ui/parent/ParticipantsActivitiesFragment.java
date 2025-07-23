package com.example.childrenscenterapp2.ui.parent;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ParticipantsActivitiesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ActivitiesForChildAdapter adapter;
    private List<String> activityNames = new ArrayList<>();
    private String childUid;

    public ParticipantsActivitiesFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_participants_activities, container, false);
        recyclerView = view.findViewById(R.id.rvActivitiesForChild);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ActivitiesForChildAdapter(activityNames, null);
        recyclerView.setAdapter(adapter);

        if (getArguments() != null) {
            if (getArguments().containsKey("childUid")) {
                childUid = getArguments().getString("childUid");
                adapter.setChildId(childUid);
                Log.d("ActivitiesDebug", "Child UID: " + childUid);
                loadActivitiesForChild(childUid);

            } else if (getArguments().containsKey("activityIds")) {
                List<String> ids = getArguments().getStringArrayList("activityIds");
                if (ids != null && !ids.isEmpty()) {
                    activityNames.clear();
                    activityNames.addAll(ids);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "❌ אין פעילויות להצגה", Toast.LENGTH_SHORT).show();
                }
            }

        } else {
            Toast.makeText(getContext(), "❌ לא נבחרו נתונים", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void loadActivitiesForChild(String childUid) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(childUid)
                .collection("registrations")
                .get()
                .addOnSuccessListener(snapshot -> {
                    activityNames.clear();

                    if (snapshot.isEmpty()) {
                        Toast.makeText(getContext(), "❌ אין פעילויות משויכות", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    for (DocumentSnapshot doc : snapshot) {
                        String activityId = doc.getString("activityId");
                        String domain = doc.getString("domain");

                        if (activityId != null && domain != null) {
                            String display = domain + "@" + activityId;
                            if (!activityNames.contains(display)) {
                                activityNames.add(display);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "❌ שגיאה בטעינת הנתונים", Toast.LENGTH_SHORT).show();
                });
    }
}
