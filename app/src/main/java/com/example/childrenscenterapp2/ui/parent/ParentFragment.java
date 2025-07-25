package com.example.childrenscenterapp2.ui.parent;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.ui.home.HomeFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.List;
import androidx.appcompat.app.AlertDialog;

/**
 * Fragment המהווה את עמוד הבית של הורה.
 * מאפשר להורה לצפות בפעילויות של ילדיו או לחפש פעילויות לפי יום/תחום.
 */
public class ParentFragment extends Fragment {

    private FirebaseFirestore firestore;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // מאפשר תפריט ⋮ עם כפתור התנתקות
    }

    /**
     * יצירת ממשק המשתמש של הורה – עם כפתורים לחיפוש פעילויות ולצפייה בפעילויות של הילד.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_parent, container, false);
        firestore = FirebaseFirestore.getInstance();

        // כפתור להצגת כל הפעילויות עם סינון (ChildActivitiesFragment)
        Button btnShowActivities = view.findViewById(R.id.btnShowActivities);
        btnShowActivities.setOnClickListener(v -> openChildActivitiesSearch());

        // כפתור להצגת פעילויות של ילד בודד (ParticipantsActivitiesFragment)
        Button btnChildActivities = view.findViewById(R.id.btnChildActivities);
        btnChildActivities.setOnClickListener(v -> fetchParentIdAndChildren());

        return view;
    }

    /**
     * מעבר למסך חיפוש פעילויות לפי גיל, תחום ויום (בממשק הילד)
     */
    private void openChildActivitiesSearch() {
        Fragment fragment = new com.example.childrenscenterapp2.ui.child.ChildActivitiesFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isParentView", true); // מעביר פרמטר שזו גישה של הורה
        fragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * שליפת תעודת הזהות של ההורה כדי לאתר את כל ילדיו במערכת.
     */
    private void fetchParentIdAndChildren() {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestore.collection("users").document(currentUid).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String idNumber = snapshot.getString("idNumber");
                        if (idNumber != null && !idNumber.isEmpty()) {
                            fetchChildrenByIdNumber(idNumber);
                        } else {
                            Toast.makeText(getContext(), "❌ לא נמצאה תעודת זהות", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "❌ שגיאה בשליפת פרטי המשתמש", Toast.LENGTH_SHORT).show());
    }

    /**
     * שאילתת Firestore למציאת כל המשתמשים מסוג "ילד" עם אותה תעודת זהות של ההורה.
     */
    private void fetchChildrenByIdNumber(String parentIdNumber) {
        firestore.collection("users")
                .whereEqualTo("type", "ילד")
                .whereEqualTo("idNumber", parentIdNumber)
                .get()
                .addOnSuccessListener(childrenSnapshots -> {
                    List<DocumentSnapshot> children = childrenSnapshots.getDocuments();

                    if (children.size() == 1) {
                        openActivitiesForChild(children.get(0).getId());
                    } else if (children.size() > 1) {
                        showChildrenSelectionDialog(children);
                    } else {
                        Toast.makeText(getContext(), "❌ לא נמצאו ילדים משויכים", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "❌ שגיאה בשליפת הילדים", Toast.LENGTH_SHORT).show());
    }

    /**
     * מציג דיאלוג לבחירת ילד מתוך רשימת הילדים, אם יש יותר מילד אחד.
     */
    private void showChildrenSelectionDialog(List<DocumentSnapshot> children) {
        String[] childNames = new String[children.size()];
        for (int i = 0; i < children.size(); i++) {
            childNames[i] = children.get(i).getString("name");
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("בחר ילד")
                .setItems(childNames, (dialog, which) -> {
                    String selectedChildId = children.get(which).getId();
                    openActivitiesForChild(selectedChildId);
                })
                .show();
    }

    /**
     * טעינת Fragment להצגת הפעילויות של ילד מסוים.
     */
    private void openActivitiesForChild(String childUid) {
        Fragment fragment = new ParticipantsActivitiesFragment();
        Bundle bundle = new Bundle();
        bundle.putString("childUid", childUid);
        fragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    // === פונקציונליות התנתקות מהחשבון ===

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_logout, menu); // טוען את תפריט ⋮
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * טיפול בהתנתקות – ניקוי פרטי התחברות, חזרה לעמוד הבית.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();

            SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            prefs.edit().clear().apply();

            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new HomeFragment());
            transaction.commit();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
