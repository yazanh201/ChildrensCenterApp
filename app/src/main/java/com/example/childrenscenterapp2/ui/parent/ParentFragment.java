package com.example.childrenscenterapp2.ui.parent;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.childrenscenterapp2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.List;

public class ParentFragment extends Fragment {

    private FirebaseFirestore firestore;

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

    // פתיחת ChildActivitiesFragment עבור חיפוש פעילויות עם סינון
    private void openChildActivitiesSearch() {
        Fragment fragment = new com.example.childrenscenterapp2.ui.child.ChildActivitiesFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isParentView", true); // אופציונלי, אם רוצים לשנות טקסטים בהתאם
        fragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    // === המשך פונקציונליות עבור כפתור שני (לא נוגע בזה) ===

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
}
