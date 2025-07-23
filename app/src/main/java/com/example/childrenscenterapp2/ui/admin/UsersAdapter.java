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
 * Adapter להצגת משתמשים עם כפתור מחיקה
 */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private final List<User> userList;
    private final OnUserDeleteListener deleteListener;

    // ממשק למחיקת משתמש
    public interface OnUserDeleteListener {
        void onDelete(User user);
    }

    // בנאי עם listener למחיקה
    public UsersAdapter(List<User> userList, OnUserDeleteListener deleteListener) {
        this.userList = userList;
        this.deleteListener = deleteListener;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtEmail, txtType;
        Button btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtEmail = itemView.findViewById(R.id.txtEmail);
            txtType = itemView.findViewById(R.id.txtType);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.txtName.setText(user.getName());
        holder.txtEmail.setText(user.getEmail());
        holder.txtType.setText(user.getType());

        // לחיצה על מחיקה
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
