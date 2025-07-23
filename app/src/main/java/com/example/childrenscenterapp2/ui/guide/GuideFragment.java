package com.example.childrenscenterapp2.ui.guide;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.data.models.ActivityModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class GuideFragment extends Fragment {

    private RecyclerView recyclerViewActivities;
    private GuideActivitiesAdapter adapter;
    private List<ActivityModel> activityList = new ArrayList<>();

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

        adapter = new GuideActivitiesAdapter(activityList, (activityId, activityName) -> {
            openParticipantsListFragment(activityId, activityName);
        });
        recyclerViewActivities.setAdapter(adapter);

        loadActivities();

        return view;
    }

    private void loadActivities() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "המשתמש אינו מחובר", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentGuideUid = currentUser.getUid();

        // שלב ראשון: שליפת המדריך לפי ה־UID כדי לקבל את רשימת הפעילויות שלו
        FirebaseFirestore.getInstance().collection("users")  // או "guides" לפי המבנה שלך
                .whereEqualTo("uid", currentGuideUid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(getContext(), "מדריך לא נמצא", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<String> guideActivities = (List<String>) querySnapshot.getDocuments().get(0).get("activities");
                    if (guideActivities == null || guideActivities.isEmpty()) {
                        Toast.makeText(getContext(), "לא נמצאו פעילויות למדריך", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // שלב שני: שליפת כל הפעילויות במסד ואז סינון לפי רשימת השמות של המדריך
                    activitiesRef.get()
                            .addOnSuccessListener(activitySnapshot -> {
                                activityList.clear();
                                for (QueryDocumentSnapshot doc : activitySnapshot) {
                                    ActivityModel activity = doc.toObject(ActivityModel.class);
                                    activity.setId(doc.getId());

                                    if (guideActivities.contains(activity.getName())) {
                                        activityList.add(activity);
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "שגיאה בטעינת פעילויות", Toast.LENGTH_SHORT).show();
                                Log.e("GuideFragment", "שגיאה בטעינת activities", e);
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה בטעינת פרטי מדריך", Toast.LENGTH_SHORT).show();
                    Log.e("GuideFragment", "שגיאה בטעינת המדריך", e);
                });
    }

    private void openParticipantsListFragment(String activityId, String activityName) {
        ParticipantsListFragment fragment = new ParticipantsListFragment();
        Bundle args = new Bundle();
        args.putString("activityId", activityId);
        args.putString("activityName", activityName);
        fragment.setArguments(args);

        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment); // ודא שיש container כזה ב־Activity הראשית שלך
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
