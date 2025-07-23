package com.example.childrenscenterapp2.ui.guide;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadPhotosFragment extends Fragment {

    private static final int PICK_IMAGES_REQUEST = 1;

    private List<Uri> imageUris = new ArrayList<>();
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private String activityId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_photos, container, false);

        activityId = getArguments() != null ? getArguments().getString("activityId") : null;

        Button btnSelectImages = view.findViewById(R.id.btnSelectImages);
        Button btnUpload = view.findViewById(R.id.btnUploadToFirestore);
        recyclerView = view.findViewById(R.id.recyclerViewImages);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        imageAdapter = new ImageAdapter(imageUris);
        recyclerView.setAdapter(imageAdapter);

        btnSelectImages.setOnClickListener(v -> openImagePicker());
        btnUpload.setOnClickListener(v -> uploadImagesToFirestore());

        return view;
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "בחר תמונות"), PICK_IMAGES_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUris.clear();
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    imageUris.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                imageUris.add(data.getData());
            }
            imageAdapter.notifyDataSetChanged();
        }
    }

    private void uploadImagesToFirestore() {
        if (imageUris.isEmpty()) {
            Toast.makeText(getContext(), "לא נבחרו תמונות", Toast.LENGTH_SHORT).show();
            return;
        }

        if (activityId == null) {
            Toast.makeText(getContext(), "שגיאה: activityId לא נמצא", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        for (Uri uri : imageUris) {
            String base64 = uriToBase64(uri);
            if (base64 != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("imageBase64", base64);

                data.put("timestamp", FieldValue.serverTimestamp());

                firestore.collection("activities")
                        .document(activityId)
                        .collection("photos")
                        .add(data)
                        .addOnSuccessListener(doc -> {
                            Toast.makeText(getContext(), "תמונה נשמרה", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "שגיאה בשמירה", Toast.LENGTH_SHORT).show();
                        });
            }
        }

        imageUris.clear();
        imageAdapter.notifyDataSetChanged();
    }

    private String uriToBase64(Uri uri) {
        try (InputStream inputStream = getActivity().getContentResolver().openInputStream(uri)) {
            byte[] bytes = getBytes(inputStream);
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
