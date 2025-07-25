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

/**
 * Fragment עבור מדריך להצגת הפעילויות שהוא אחראי עליהן
 * כולל כפתורים לצפייה במשתתפים והעלאת תמונות לכל פעילות
 */
public class GuideActivitiesFragment extends Fragment {

    private RecyclerView recyclerView;
    private GuideActivitiesAdapter adapter;
    private final List<ActivityModel> activityList = new ArrayList<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference activitiesRef = db.collection("activities");

    /**
     * יצירת התצוגה של המסך (כולל RecyclerView)
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guide_activities, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewActivities);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // יצירת Adapter עם האזנה ללחיצות על כפתורי הפעילות
        adapter = new GuideActivitiesAdapter(activityList, new GuideActivitiesAdapter.OnParticipantsClickListener() {
            @Override
            public void onParticipantsClick(String activityId, String activityName) {
                openParticipantsListFragment(activityId, activityName); // צפייה במשתתפים
            }

            @Override
            public void onUploadPhotosClick(String activityId) {
                openUploadPhotosFragment(activityId); // העלאת תמונות
            }
        });

        recyclerView.setAdapter(adapter);
        loadActivities(); // טעינת הפעילויות מהמדריך המחובר

        return view;
    }

    /**
     * טעינת פעילויות שהמדריך הנוכחי אחראי עליהן
     */
    private void loadActivities() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "המשתמש אינו מחובר", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentGuideUid = currentUser.getUid();

        // שליפת רשימת הפעילויות המשויכות למדריך מתוך users
        db.collection("users")
                .whereEqualTo("uid", currentGuideUid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(getContext(), "מדריך לא נמצא", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // שליפת רשימת שמות הפעילויות מתוך מסמך המשתמש
                    List<String> guideActivities = (List<String>) querySnapshot.getDocuments().get(0).get("activities");
                    if (guideActivities == null || guideActivities.isEmpty()) {
                        Toast.makeText(getContext(), "לא נמצאו פעילויות למדריך", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // שליפת כל הפעילויות ובדיקת התאמה לפי שם
                    activitiesRef.get()
                            .addOnSuccessListener(activitySnapshot -> {
                                activityList.clear();
                                for (QueryDocumentSnapshot doc : activitySnapshot) {
                                    ActivityModel activity = doc.toObject(ActivityModel.class);
                                    activity.setId(doc.getId());

                                    // רק פעילויות שהמדריך אחראי להן
                                    if (guideActivities.contains(activity.getName())) {
                                        activityList.add(activity);
                                    }
                                }
                                adapter.notifyDataSetChanged(); // רענון התצוגה
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "שגיאה בטעינת פעילויות", Toast.LENGTH_SHORT).show();
                                Log.e("GuideActivitiesFragment", "שגיאה בטעינת activities", e);
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה בטעינת פרטי מדריך", Toast.LENGTH_SHORT).show();
                    Log.e("GuideActivitiesFragment", "שגיאה בטעינת המדריך", e);
                });
    }

    /**
     * מעבר לפרגמנט הצגת רשימת משתתפים של פעילות
     */
    private void openParticipantsListFragment(String activityId, String activityName) {
        ParticipantsListFragment fragment = new ParticipantsListFragment();
        Bundle args = new Bundle();
        args.putString("activityId", activityId);
        args.putString("activityName", activityName);
        fragment.setArguments(args);

        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * מעבר לפרגמנט העלאת תמונות לפעילות
     */
    private void openUploadPhotosFragment(String activityId) {
        UploadPhotosFragment fragment = new UploadPhotosFragment();
        Bundle args = new Bundle();
        args.putString("activityId", activityId);
        fragment.setArguments(args);

        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
