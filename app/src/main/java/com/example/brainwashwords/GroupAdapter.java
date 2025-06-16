package com.example.brainwashwords; // החבילה שבה נמצאת המחלקה

import android.content.Intent; // מאפשר מעבר בין מסכים (Activities)
import android.view.LayoutInflater; // מאפשר "ניפוח" קובץ XML לקוד Java
import android.view.View; // בסיס לכל רכיב גרפי
import android.view.ViewGroup; // קבוצה של Views – מייצגת את ההורה ברשימה
import android.widget.TextView; // רכיב להצגת טקסט

import androidx.annotation.NonNull; // אנוטציה למניעת שגיאות null
import androidx.recyclerview.widget.RecyclerView; // תצוגת רשימה עם יעילות גבוהה

import java.util.List; // ממשק שמייצג רשימה של פריטים

/**
 * GroupAdapter – מתאם ל־RecyclerView שמציג קבוצות מילים.
 * כל קבוצה מוצגת כפריט נפרד ברשימה, ובלחיצה עליה – עוברים למסך המילים שלה.
 */
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    // רשימת קבוצות המילים שמוצגות ברשימה
    private final List<Group> groupList;

    /**
     * בנאי שמקבל את הרשימה של הקבוצות להצגה.
     *
     * @param groupList רשימת קבוצות מסוג Group.
     */
    public GroupAdapter(List<Group> groupList) {
        this.groupList = groupList;
    }

    /**
     * יוצר View חדש לכל שורה ברשימה על סמך layout שנקרא recycler_view_row.
     *
     * @param parent   ההורה של ה-View (בד"כ RecyclerView).
     * @param viewType סוג התצוגה (כאן יש רק סוג אחד).
     * @return אובייקט חדש מסוג GroupViewHolder.
     */
    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ניפוח קובץ ה-XML של כל שורה והפיכתו לאובייקט View
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_row, parent, false);

        // מחזיר אובייקט GroupViewHolder שעוטף את ה-View שנבנה
        return new GroupViewHolder(view);
    }

    /**
     * קושר את הנתונים מהקבוצה הספציפית לתוך ה־ViewHolder.
     *
     * @param holder   אובייקט ה־ViewHolder.
     * @param position מיקום הקבוצה ברשימה.
     */
    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        // לוקחים את הקבוצה לפי המיקום
        Group currentGroup = groupList.get(position);

        // מציגים את שם הקבוצה ונותנים לה יכולת לחיצה
        holder.bind(currentGroup);
    }

    /**
     * מחזיר את מספר הפריטים ברשימה (לצורך יצירת שורות).
     *
     * @return מספר הקבוצות ברשימה.
     */
    @Override
    public int getItemCount() {
        return groupList.size();
    }

    /**
     * מחלקה פנימית שאחראית על תצוגת כל פריט (שורה אחת) ברשימה.
     */
    static class GroupViewHolder extends RecyclerView.ViewHolder {

        // רכיב להצגת שם הקבוצה
        private final TextView textView;

        /**
         * בנאי שמאתחל את רכיבי ה־ViewHolder.
         *
         * @param itemView תצוגת השורה שנבנתה מה־XML.
         */
        GroupViewHolder(View itemView) {
            super(itemView);

            // מחפש את רכיב ה-TextView בתוך התצוגה
            textView = itemView.findViewById(R.id.name);
        }

        /**
         * קושר קבוצה לתוך ה־View – מציג שם ומאזין ללחיצה.
         *
         * @param group אובייקט הקבוצה להצגה.
         */
        void bind(Group group) {
            // מציג את שם הקבוצה בטקסט
            textView.setText(group.getName());

            // מאזין ללחיצה על כל השורה (itemView)
            itemView.setOnClickListener(v -> {
                // יוצר Intent שמעביר למסך של המילים בקבוצה
                Intent intent = new Intent(v.getContext(), words.class);

                // מעביר את שם הקבוצה למסך הבא כ־extra
                intent.putExtra("workoutName", group.getName());

                // מתחיל את המסך הבא (words.class)
                v.getContext().startActivity(intent);
            });
        }
    }
}
