package com.example.emsismartpresence;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private final List<ScheduleItem> scheduleItems;
    private final Context context;
    private final OnScheduleItemClickListener listener;

    public interface OnScheduleItemClickListener {
        void onDeleteClick(int position);
    }

    public ScheduleAdapter(Context context, List<ScheduleItem> scheduleItems, OnScheduleItemClickListener listener) {
        this.context = context;
        this.scheduleItems = scheduleItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        ScheduleItem item = scheduleItems.get(position);
        
        // Display course number (position + 1) instead of module code
        String courseNumber = String.format("Course_%02d", position + 1);
        holder.courseCodeText.setText(courseNumber);
        
        holder.dayText.setText(item.getDay());
        
        // Format the time display
        String timeText = String.format("%s - %s", 
            item.getStart_session() != null ? item.getStart_session() : "", 
            item.getEnd_session() != null ? item.getEnd_session() : "");
        holder.timeText.setText(timeText);

        // Handle description - show if available, hide if empty
        String description = item.getDescription();
        if (description != null && !description.isEmpty()) {
            holder.descriptionText.setText(description);
            holder.descriptionText.setVisibility(View.VISIBLE);
        } else {
            holder.descriptionText.setVisibility(View.GONE);
        }

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return scheduleItems.size();
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView courseCodeText;
        TextView dayText;
        TextView timeText;
        TextView descriptionText;
        ImageButton deleteButton;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            courseCodeText = itemView.findViewById(R.id.courseCodeText);
            dayText = itemView.findViewById(R.id.dayText);
            timeText = itemView.findViewById(R.id.timeText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
