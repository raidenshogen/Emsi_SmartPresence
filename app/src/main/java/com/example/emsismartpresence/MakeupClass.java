package com.example.emsismartpresence;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MakeupClass extends AppCompatActivity {

    private EditText courseCodeInput;
    private EditText dateInput;
    private EditText timeInput;
    private EditText reasonInput;
    private Button scheduleMakeupButton;
    private ListView makeupClassListView;
    private FirebaseFirestore db;
    private ArrayList<String> makeupClassList;
    private ArrayAdapter<String> adapter;
    final Calendar myCalendar = Calendar.getInstance();
    final Calendar timeCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_makeup_class);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        courseCodeInput = findViewById(R.id.courseCodeInput);
        dateInput = findViewById(R.id.dateInput);
        dateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new DatePickerDialog
                new DatePickerDialog(
                        MakeupClass.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                // Update the Calendar object with the selected date
                                myCalendar.set(Calendar.YEAR, year);
                                myCalendar.set(Calendar.MONTH, month);
                                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                // Format the selected date as "dd-MMM-yyyy"
                                String dateFormatPattern = "dd-MMM-yyyy";
                                SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatPattern, Locale.US);
                                String formattedDate = dateFormat.format(myCalendar.getTime());

                                // Set the formatted date in the EditText
                                dateInput.setText(formattedDate);
                            }
                        },
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)
                ).show();
            }
        });
        timeInput = findViewById(R.id.timeInput);
        timeInput.setFocusable(false);
        timeInput.setOnClickListener(v -> showTimePicker());

        reasonInput = findViewById(R.id.reasonInput);
        scheduleMakeupButton = findViewById(R.id.scheduleMakeupButton);
        makeupClassListView = findViewById(R.id.makeupClassListView);

        // Initialize makeup class list
        makeupClassList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, makeupClassList);
        makeupClassListView.setAdapter(adapter);

        // Set click listener
        scheduleMakeupButton.setOnClickListener(v -> scheduleMakeupClass());

        // Load existing makeup classes
        loadMakeupClasses();
    }

    private void scheduleMakeupClass() {
        String courseCode = courseCodeInput.getText().toString().trim();
        String date = dateInput.getText().toString().trim();
        String time = timeInput.getText().toString().trim();
        String reason = reasonInput.getText().toString().trim();

        if (courseCode.isEmpty() || date.isEmpty() || time.isEmpty() || reason.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> makeupClass = new HashMap<>();
        makeupClass.put("courseCode", courseCode);
        makeupClass.put("date", date);
        makeupClass.put("time", time);
        makeupClass.put("reason", reason);
        makeupClass.put("professorId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        makeupClass.put("status", "scheduled");

        db.collection("makeupClasses")
                .add(makeupClass)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(MakeupClass.this, "Makeup class scheduled successfully",
                            Toast.LENGTH_SHORT).show();
                    clearInputs();
                    loadMakeupClasses();
                })
                .addOnFailureListener(e -> Toast.makeText(MakeupClass.this,
                        "Error scheduling makeup class", Toast.LENGTH_SHORT).show());
    }

    private void loadMakeupClasses() {
        db.collection("makeupClasses")
                .whereEqualTo("professorId", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    makeupClassList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String makeupClassText = document.getString("courseCode") + " - " +
                                document.getString("date") + " at " +
                                document.getString("time") + "\nReason: " +
                                document.getString("reason");
                        makeupClassList.add(makeupClassText);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(MakeupClass.this,
                        "Error loading makeup classes", Toast.LENGTH_SHORT).show());
    }

    private void showTimePicker() {
        // Get current time
        int hour = timeCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = timeCalendar.get(Calendar.MINUTE);

        // Create and show TimePickerDialog
        new TimePickerDialog(
            this,
            (view, hourOfDay, minute1) -> {
                // Update the timeCalendar with selected time
                timeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                timeCalendar.set(Calendar.MINUTE, minute1);
                
                // Format the time as 24-hour format (HH:mm)
                String timeFormat = "HH:mm";
                SimpleDateFormat sdf = new SimpleDateFormat(timeFormat, Locale.getDefault());
                String formattedTime = sdf.format(timeCalendar.getTime());
                
                // Set the formatted time to the timeInput field
                timeInput.setText(formattedTime);
            },
            hour,
            minute,
            true // 24-hour format
        ).show();
    }

    private void clearInputs() {
        courseCodeInput.setText("");
        dateInput.setText("");
        timeInput.setText("");
        reasonInput.setText("");
    }
}