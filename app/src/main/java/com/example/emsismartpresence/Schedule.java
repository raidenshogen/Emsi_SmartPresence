package com.example.emsismartpresence;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Schedule extends AppCompatActivity implements ScheduleAdapter.OnScheduleItemClickListener {

    private TextInputEditText courseCodeInput;
    private Spinner daySpinner;
    private TextInputEditText startsession;
    private TextInputEditText endsession;
    private TextInputEditText descriptionInput;
    private FirebaseFirestore db;
    private List<ScheduleItem> scheduleItems;
    private ScheduleAdapter scheduleAdapter;
    private RecyclerView scheduleRecyclerView;
    private ArrayAdapter<String> dayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        courseCodeInput = findViewById(R.id.courseCodeInput);
        daySpinner = findViewById(R.id.daySpinner);
        startsession = findViewById(R.id.startTimeInput);
        endsession = findViewById(R.id.endTimeInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        Button addScheduleButton = findViewById(R.id.addScheduleButton);
        scheduleRecyclerView = findViewById(R.id.scheduleRecyclerView);

        // Setup day spinner
        setupDaySpinner();

        // Setup time input with time picker dialog
        setupStartTimeInput();
        setupEndTimeInput();

        // Initialize schedule items and RecyclerView
        scheduleItems = new ArrayList<>();
        scheduleAdapter = new ScheduleAdapter(this, scheduleItems, this);
        scheduleRecyclerView.setAdapter(scheduleAdapter);

        // Set the layout manager with appropriate span count for grid layout
        int spanCount = getResources().getConfiguration().orientation ==
                android.content.res.Configuration.ORIENTATION_LANDSCAPE ? 2 : 1;
        scheduleRecyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));

        // Set click listeners
        addScheduleButton.setOnClickListener(v -> addSchedule());

        // Load existing schedules
        loadSchedules();
    }

    private void setupStartTimeInput() {
        startsession.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(Schedule.this,
                    (view, hourOfDay, selectedMinute) -> {
                        startsession.setText(String.format(Locale.getDefault(), "%02d:%02d",
                                hourOfDay, selectedMinute));
                    }, hour, minute, true);
            timePickerDialog.show();
        });
    }
    private void setupEndTimeInput() {
        endsession.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(Schedule.this,
                    (view, hourOfDay, selectedMinute) -> {
                        endsession.setText(String.format(Locale.getDefault(), "%02d:%02d",
                                hourOfDay, selectedMinute));
                    }, hour, minute, true);
            timePickerDialog.show();
        });
    }

    private void setupDaySpinner() {
        List<String> days = Arrays.asList(
                "Monday", "Tuesday", "Wednesday",
                "Thursday", "Friday", "Saturday", "Sunday");

        dayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                days);

        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);
    }

    private boolean isValidTimeFormat(String time) {
        return time.matches("([01]?[0-9]|2[0-3]):[0-5][0-9]");
    }

    private void addSchedule() {
        String module = courseCodeInput.getText().toString().trim();
        String day = daySpinner.getSelectedItem().toString();
        String end = endsession.getText().toString().trim();
        String start = startsession.getText().toString().trim();

        String description = descriptionInput.getText().toString().trim();

        if (module.isEmpty() || end.isEmpty()||start.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidTimeFormat(end)||!isValidTimeFormat(start)) {
            Toast.makeText(this, "Please use HH:MM time format", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> schedule = new HashMap<>();
        schedule.put("module", module);
        schedule.put("day", day);
        schedule.put("start_session", start);
        schedule.put("end_session", end);
        schedule.put("description", description);
        schedule.put("professorId", FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Show loading indicator
        Toast.makeText(this, "Adding schedule...", Toast.LENGTH_SHORT).show();

        db.collection("schedules")
                .add(schedule)
                .addOnSuccessListener(documentReference -> {
                    // Add the new schedule to the local list for immediate UI update
                    ScheduleItem newItem = new ScheduleItem(
                            documentReference.getId(),
                            module,
                            day,
                            start,
                            end,
                            description
                    );
                    scheduleItems.add(newItem);
                    scheduleAdapter.notifyItemInserted(scheduleItems.size() - 1);

                    Toast.makeText(Schedule.this,
                            "Schedule added successfully", Toast.LENGTH_SHORT).show();
                    clearInputs();
                })
                .addOnFailureListener(e -> Toast.makeText(Schedule.this,
                        "Error adding schedule: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadSchedules() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("schedules")
                .whereEqualTo("professorId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    scheduleItems.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String id = document.getId();
                        String courseCode = document.getString("module");
                        String day = document.getString("day");
                        String startSession = document.getString("start_session");
                        String endSession = document.getString("end_session");
                        String description = document.getString("description");

                        ScheduleItem item = new ScheduleItem(id, courseCode, day, startSession, endSession, description);
                        scheduleItems.add(item);
                    }

                    // Update the UI
                    scheduleAdapter.notifyDataSetChanged();

                    // Show empty state if needed
                    if (scheduleItems.isEmpty()) {
                        Toast.makeText(Schedule.this,
                                "No schedules found. Add your first schedule!",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Schedule.this,
                            "Error loading schedules: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDeleteClick(int position) {
        if (position >= 0 && position < scheduleItems.size()) {
            showDeleteDialog(position);
        }
    }

    private void showDeleteDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Schedule")
                .setMessage("Are you sure you want to delete this schedule?")
                .setPositiveButton("Delete", (dialog, which) -> deleteSchedule(position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteSchedule(int position) {
        String scheduleId = scheduleItems.get(position).getId();

        db.collection("schedules").document(scheduleId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Schedule.this,
                            "Schedule deleted", Toast.LENGTH_SHORT).show();
                    // Remove the item locally for immediate UI update
                    scheduleItems.remove(position);
                    scheduleAdapter.notifyItemRemoved(position);
                    scheduleAdapter.notifyItemRangeChanged(position, scheduleItems.size());

                    // Show empty state if needed
                    if (scheduleItems.isEmpty()) {
                        Toast.makeText(Schedule.this,
                                "No schedules found. Add your first schedule!",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(Schedule.this,
                        "Error deleting schedule: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void clearInputs() {
        courseCodeInput.setText("");
        startsession.setText("");
        endsession.setText("");
        descriptionInput.setText("");
        daySpinner.setSelection(0);
        courseCodeInput.requestFocus();
    }
}