package com.example.childrenscenterapp2.ui.parent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewPhotosFragment extends Fragment {

    private static final String ARG_ACTIVITY_ID = "activityId";

    private String activityId;
    private RecyclerView recyclerView;
    private PhotoDisplayAdapter adapter;
    private List<Bitmap> imageList = new ArrayList<>();

    public static ViewPhotosFragment newInstance(String activityId) {
        ViewPhotosFragment fragment = new ViewPhotosFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ACTIVITY_ID, activityId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_photos, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewPhotos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL, false));

        adapter = new PhotoDisplayAdapter(imageList);
        recyclerView.setAdapter(adapter);

        if (getArguments() != null) {
            activityId = getArguments().getString(ARG_ACTIVITY_ID);
            loadPhotosFromFirestore();
        }

        return view;
    }

    private void loadPhotosFromFirestore() {
        FirebaseFirestore.getInstance()
                .collection("activities")
                .document(activityId)
                .collection("photos")
                .get()
                .addOnSuccessListener(snapshot -> {
                    imageList.clear();
                    Log.d("ViewPhotosFragment", "📸 סך הכל מסמכים שנמצאו: " + snapshot.size());

                    for (QueryDocumentSnapshot doc : snapshot) {
                        String base64 = doc.getString("imageBase64");
                        if (base64 == null || base64.isEmpty()) {
                            Log.w("ViewPhotosFragment", "⚠️ שדה imageBase64 ריק או חסר במסמך: " + doc.getId());
                            continue;
                        }

                        try {
                            byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                            if (bitmap != null) {
                                imageList.add(bitmap);
                                Log.d("ViewPhotosFragment", "✅ תמונה הוספה לרשימה מתוך: " + doc.getId());
                            } else {
                                Log.e("ViewPhotosFragment", "❌ Bitmap יצא null עבור מסמך: " + doc.getId());
                            }
                        } catch (Exception e) {
                            Log.e("ViewPhotosFragment", "❌ שגיאה בפענוח Base64 במסמך: " + doc.getId(), e);
                        }
                    }

                    if (imageList.isEmpty()) {
                        Toast.makeText(getContext(), "לא נמצאו תמונות להצגה", Toast.LENGTH_SHORT).show();
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "❌ שגיאה בטעינת תמונות מהמאגר", Toast.LENGTH_SHORT).show();
                    Log.e("ViewPhotosFragment", "🔥 שגיאה בטעינת תמונות מ-Firestore", e);
                });
    }

}
