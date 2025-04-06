package com.example.brainwashwords;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {
    private final List<Group> groupList;

    public GroupAdapter(List<Group> groupList) {
        this.groupList = groupList;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_row, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group currentGroup = groupList.get(position);
        holder.bind(currentGroup);
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        GroupViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.name);
        }


        // TODO : handle on click group
        void bind(Group group) {
            textView.setText(group.getName());

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), words.class);
                v.getContext().startActivity(intent);
            });
        }
    }
}
