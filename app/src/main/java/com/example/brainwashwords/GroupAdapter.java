package com.example.brainwashwords;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter for displaying a list of workout groups in a RecyclerView.
 * Each item represents a group of words and leads to a new screen when clicked.
 */
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private final List<Group> groupList;

    /**
     * Constructor for the adapter.
     *
     * @param groupList A list of Group objects to be displayed.
     */
    public GroupAdapter(List<Group> groupList) {
        this.groupList = groupList;
    }

    /**
     * Inflates the layout for each item in the RecyclerView.
     *
     * @param parent   The parent view group.
     * @param viewType The type of view (not used here as we have one layout).
     * @return A new ViewHolder for the item.
     */
    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // יוצרים View חדש על בסיס layout בשם recycler_view_row
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_row, parent, false);
        return new GroupViewHolder(view);
    }

    /**
     * Binds data from a Group object to the ViewHolder.
     *
     * @param holder   The ViewHolder to bind data to.
     * @param position The position in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group currentGroup = groupList.get(position); // מקבלים את הקבוצה הנוכחית
        holder.bind(currentGroup); // מציגים אותה ב-ViewHolder
    }

    /**
     * Returns the number of items in the list.
     *
     * @return The size of groupList.
     */
    @Override
    public int getItemCount() {
        return groupList.size();
    }

    /**
     * ViewHolder class to manage individual group items in the list.
     */
    static class GroupViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        /**
         * Constructor for the ViewHolder.
         *
         * @param itemView The inflated view for the item.
         */
        GroupViewHolder(View itemView) {
            super(itemView);
            // מחפש את ה-TextView שבתוך layout בשם recycler_view_row
            textView = itemView.findViewById(R.id.name);
        }

        /**
         * Binds a Group object to the view, setting its name and click behavior.
         *
         * @param group The group object containing the name.
         */
        void bind(Group group) {
            // מגדיר את שם הקבוצה בטקסט
            textView.setText(group.getName());

            // מאזין ללחיצה על כל ה-item – מעביר את המשתמש למסך המילים של הקבוצה הזו
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), words.class);
                intent.putExtra("workoutName", group.getName()); // מעביר את שם הקבוצה למסך הבא
                v.getContext().startActivity(intent); // מפעיל את מסך words
            });
        }
    }
}
