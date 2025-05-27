package com.example.emsismartpresence;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MarkAttendance extends AppCompatActivity {

    private EditText studentIdInput;
    private EditText courseCodeInput;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        studentIdInput = findViewById(R.id.studentIdInput);
        courseCodeInput = findViewById(R.id.courseCodeInput);
        Button markAttendanceButton = findViewById(R.id.markAttendanceButton);

        markAttendanceButton.setOnClickListener(v -> markAttendance());
    }

    private void markAttendance() {
        String studentId = studentIdInput.getText().toString().trim();
        String courseCode = courseCodeInput.getText().toString().trim();

        if (studentId.isEmpty() || courseCode.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current date and time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDateTime = sdf.format(new Date());

        // Create attendance record
        Map<String, Object> attendanceRecord = new HashMap<>();
        attendanceRecord.put("studentId", studentId);
        attendanceRecord.put("courseCode", courseCode);
        attendanceRecord.put("timestamp", currentDateTime);
        attendanceRecord.put("professorId", mAuth.getCurrentUser().getUid());

        // Save to Firestore
        db.collection("attendance")
                .add(attendanceRecord)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(MarkAttendance.this, "Attendance marked successfully", Toast.LENGTH_SHORT).show();
                    // Clear inputs
                    studentIdInput.setText("");
                    courseCodeInput.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MarkAttendance.this, "Error marking attendance: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }
}