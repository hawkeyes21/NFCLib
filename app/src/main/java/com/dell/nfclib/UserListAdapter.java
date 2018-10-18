package com.dell.nfclib;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder>
{
    List<userDetails> list;
    public UserListAdapter(List<userDetails> list)
    {
        this.list = list;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.user_list_layout, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position)
    {
        userDetails details = list.get(position);

        holder.userName.setText(details.getFullName());
        holder.userDesignation.setText(details.getDesignation());
        holder.userEmail.setText(details.getEmail());
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, userEmail, userDesignation, userId;
        public UserViewHolder(@NonNull View itemView)
        {
            super(itemView);
            userName = itemView.findViewById(R.id.userNameTextView);
            userEmail = itemView.findViewById(R.id.userEmailTextView);
            userDesignation = itemView.findViewById(R.id.userDesignationTextView);
        }
    }
}
