package com.example.childrenscenterapp2.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childrenscenterapp2.R;
import com.example.childrenscenterapp2.data.models.User;

import java.util.List;

/**
 * Adapter להצגת משתמשים (name, email, type)
 */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private final List<User> userList;

    public UsersAdapter(List<User> userList) {
        this.userList = userList;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtEmail, txtType;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtEmail = itemView.findViewById(R.id.txtEmail);
            txtType = itemView.findViewById(R.id.txtType);
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
        holder.txtName.setText(user.name);
        holder.txtEmail.setText(user.email);
        holder.txtType.setText(user.type);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
