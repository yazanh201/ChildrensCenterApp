package com.example.childrenscenterapp2.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.data.models.User;

import java.util.List;

/**
 * Adapter להצגת רשימת משתמשים ב-RecyclerView,
 * כולל כפתור מחיקה עבור כל משתמש
 */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private final List<User> userList; // רשימת המשתמשים
    private final OnUserDeleteListener deleteListener; // ממשק לפעולת מחיקה

    /**
     * ממשק למחיקת משתמש - מועבר מבחוץ (למשל מתוך Fragment)
     */
    public interface OnUserDeleteListener {
        void onDelete(User user);
    }

    /**
     * בנאי - מקבל רשימת משתמשים ומאזין למחיקה
     */
    public UsersAdapter(List<User> userList, OnUserDeleteListener deleteListener) {
        this.userList = userList;
        this.deleteListener = deleteListener;
    }

    /**
     * ViewHolder פנימי שמחזיק את רכיבי התצוגה של כל פריט
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtEmail, txtType;
        Button btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);   // שם המשתמש
            txtEmail = itemView.findViewById(R.id.txtEmail); // אימייל
            txtType = itemView.findViewById(R.id.txtType);   // סוג המשתמש (guide, parent וכו')
            btnDelete = itemView.findViewById(R.id.btnDelete); // כפתור מחיקה
        }
    }

    /**
     * יצירת ViewHolder חדש (קריאה אוטומטית כשנדרש)
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false); // שימוש ב־item_user.xml
        return new UserViewHolder(view);
    }

    /**
     * קישור נתוני המשתמש לרכיבי התצוגה
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.txtName.setText(user.getName());
        holder.txtEmail.setText(user.getEmail());
        holder.txtType.setText(user.getType());

        // טיפול בלחיצה על כפתור מחיקה
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(user);
            }
        });
    }

    /**
     * מחזיר את מספר המשתמשים ברשימה
     */
    @Override
    public int getItemCount() {
        return userList.size();
    }
}
