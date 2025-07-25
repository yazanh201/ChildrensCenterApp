package com.example.childrenscenterapp2.ui.parent;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;

import java.util.List;

/**
 * Adapter להצגת רשימת תמונות מסוג Bitmap ב־RecyclerView.
 * משמש להורים כדי לצפות בתמונות מתוך פעילויות הילדים.
 */
public class PhotoDisplayAdapter extends RecyclerView.Adapter<PhotoDisplayAdapter.PhotoViewHolder> {

    private List<Bitmap> photos; // רשימת תמונות להצגה

    // בנאי שמקבל את רשימת התמונות
    public PhotoDisplayAdapter(List<Bitmap> photos) {
        this.photos = photos;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // יצירת ViewHolder עבור כל תמונה - משתמש בפריסה item_photo_display
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo_display, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        // הגדרת התמונה למיקום הנוכחי
        holder.imageView.setImageBitmap(photos.get(position));
    }

    @Override
    public int getItemCount() {
        return photos.size(); // מספר התמונות להצגה
    }

    // ViewHolder שמחזיק את ImageView שבו נטען ה-Bitmap
    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewPhoto); // ודא ש-ID קיים ב-layout
        }
    }
}
