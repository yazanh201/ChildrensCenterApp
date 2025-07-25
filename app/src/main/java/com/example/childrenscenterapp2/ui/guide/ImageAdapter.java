package com.example.childrenscenterapp2.ui.guide;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;

import java.util.List;

/**
 * Adapter להצגת תמונות (URI) בתוך RecyclerView
 * משמש להעלאה או תצוגה מקדימה של תמונות בפעילות
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private List<Uri> imageUris; // רשימת כתובות התמונות (URI)

    /**
     * בנאי של האדפטר - מקבל רשימת URI של תמונות
     */
    public ImageAdapter(List<Uri> imageUris) {
        this.imageUris = imageUris;
    }

    /**
     * יצירת ViewHolder חדש לפריט תצוגת תמונה
     */
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_preview, parent, false); // פריסה של קובץ XML מתאים
        return new ImageViewHolder(view);
    }

    /**
     * קישור URI של תמונה ל־ImageView בתוך הפריט
     */
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri uri = imageUris.get(position);
        holder.imageView.setImageURI(uri);
    }

    /**
     * מספר התמונות להצגה
     */
    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    /**
     * ViewHolder עבור פריט בודד של תמונה
     */
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewPhoto); // הפניה ל־ImageView מתוך ה־XML
        }
    }
}
