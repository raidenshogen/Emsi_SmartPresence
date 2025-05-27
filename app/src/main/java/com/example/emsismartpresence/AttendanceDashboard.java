package com.example.emsismartpresence;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AttendanceDashboard extends AppCompatActivity {

    private EditText editDate;
    private Spinner spinnerYear, spinnerGroup,spinnerSite;
    private RecyclerView recyclerViewStudents;
    private Button btnDownloadPDF;

    private List<Student> allStudents = new ArrayList<>();
    private List<Student> filteredStudents = new ArrayList<>();
    private StudentAdapter adapter;
    private static final String SHEET_ID = "1b7PJf7QVBEVZPkBcFEal1NF9ZFJWdmZ8hITHprrVXsA";
    private static final String RANGE = "StudentsList!A2:E100";  // A2 to skip header row
    private static final String API_KEY = "AIzaSyCk_jvQgGG9oA9lF38jn2khtQE24_D4MVY";

    private static final String SHEETS_API_URL =
            "https://sheets.googleapis.com/v4/spreadsheets/" + SHEET_ID + "/values/" + RANGE + "?key=" + API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is authenticated
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            // Not signed in, launch the Signin activity
            startActivity(new Intent(this, Signin.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_attendance_dashboard);

        editDate = findViewById(R.id.editDate);
        recyclerViewStudents = findViewById(R.id.recyclerViewStudents);
        btnDownloadPDF = findViewById(R.id.btnDownloadPDF);

        // Initialize RecyclerView
        recyclerViewStudents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(filteredStudents);
        recyclerViewStudents.setAdapter(adapter);

        // Load data from Google Sheets
        fetchStudentsFromSheet();

        // Set up filters
        // Year Adapter
        spinnerYear = findViewById(R.id.spinnerYear);
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Year 1", "Year 2", "Year 3", "Year 4", "Year 5"});
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        spinnerGroup = findViewById(R.id.spinnerGroup);
        ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Group 5", "Group 4", "Group 3","Group 2", "Group 1"});
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGroup.setAdapter(groupAdapter);

        spinnerSite = findViewById(R.id.spinnerSite);
        ArrayAdapter<String> siteAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"SITE Oranges", "SITE Roudani", "SITE Centre", "SITE Maarif"});
        siteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSite.setAdapter(siteAdapter);

// Set up listeners

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: do nothing or reset filters
            }
        });
        spinnerGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional
            }
        });

        spinnerSite.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional
            }
        });

        btnDownloadPDF.setOnClickListener(v -> exportAttendanceToPDF());
        fetchStudentsFromSheet();
    }

    // This method is no longer needed as we're using fetchStudentsFromSheet()
    // Kept for reference or fallback if needed
    private void loadStudentData() {
        // Show a loading indicator
        Toast.makeText(this, "Loading sample data...", Toast.LENGTH_SHORT).show();

        // Simulate network delay
        new android.os.Handler().postDelayed(() -> {
            allStudents.clear();
            allStudents.add(new Student("Ahmed", "Group 1", "Year 1", "SITE Centre"));
            allStudents.add(new Student("Fatima", "Group 2", "Year 1", "SITE Oranges"));
            allStudents.add(new Student("Ali", "Group 3", "Year 1", "SITE Roudani"));

            runOnUiThread(this::applyFilters);
        }, 1000);
    }

    private void applyFilters() {
        String selectedYear = spinnerYear.getSelectedItem().toString();
        String selectedGroup = spinnerGroup.getSelectedItem().toString();
        String selectedSite = spinnerSite.getSelectedItem().toString();

        filteredStudents.clear();
        for (Student student : allStudents) {
            if (student.getYear().equals(selectedYear) &&
                    student.getGroup().equals(selectedGroup) &&
                    student.getSite().equals(selectedSite)) {
                filteredStudents.add(student);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private List<Student> filterSelectedStudents() {
        List<Student> selectedStudents = new ArrayList<>();
        for (int i = 0; i < adapter.getItemCount(); i++) {
            Student student = adapter.getItem(i);
            if (student.isPresent()) {
                student.setStatus("Present");
                selectedStudents.add(student);
            } else if (student.isAbsent()) {
                student.setStatus("Absent");
                selectedStudents.add(student);
            }
        }
        return selectedStudents;
    }

    private void exportAttendanceToPDF() {
        File dir = new File(getExternalFilesDir(null), "Attendance");
        if (!dir.exists()) dir.mkdirs();

        String fileName = "attendance_" + System.currentTimeMillis() + ".pdf";
        File file = new File(dir, fileName);

        try {
            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Attendance Report"));
            document.add(new Paragraph("Date: " + editDate.getText().toString()));

            float[] columnWidths = {100F, 100F, 100F, 100F, 100F};
            Table table = new Table(columnWidths);
            table.addCell("Name");
            table.addCell("Group");
            table.addCell("Year");
            table.addCell("Site");
            table.addCell("Status");

            List<Student> selectedStudents = filterSelectedStudents();
            for (Student student : selectedStudents) {
                table.addCell(student.getName());
                table.addCell(student.getGroup());
                table.addCell(student.getYear());
                table.addCell(student.getSite());
                table.addCell(student.getStatus());
            }

            document.add(table);
            document.close();

            Toast.makeText(this, "PDF saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "PDF creation failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchStudentsFromSheet() {
        runOnUiThread(() ->
            Toast.makeText(this, "Loading student data...", Toast.LENGTH_SHORT).show()
        );

        Log.d("AttendanceDashboard", "Attempting to fetch data from: " + SHEETS_API_URL);

        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(SHEETS_API_URL)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d("AttendanceDashboard", "Response code: " + response.code());
                Log.d("AttendanceDashboard", "Response message: " + response.message());

                if (response.isSuccessful() && !responseBody.isEmpty()) {
                    Log.d("AttendanceDashboard", "Response body: " + responseBody);
                    parseStudentData(responseBody);
                } else {
                    String errorMsg = "Failed to load data. Code: " + response.code() +
                                    "\nMessage: " + response.message() +
                                    "\nResponse: " + responseBody;
                    Log.e("AttendanceDashboard", errorMsg);

                    runOnUiThread(() -> {
                        Toast.makeText(AttendanceDashboard.this,
                            "Error loading data. Check logs for details. Using sample data.",
                            Toast.LENGTH_LONG).show();
                        loadStudentData();
                    });
                }
            } catch (IOException e) {
                String errorMsg = "Network error: " + e.getMessage();
                Log.e("AttendanceDashboard", errorMsg, e);

                runOnUiThread(() -> {
                    Toast.makeText(AttendanceDashboard.this,
                        "Network error: " + e.getMessage() +
                        "\nUsing sample data.",
                        Toast.LENGTH_LONG).show();
                    loadStudentData();
                });
            }
        }).start();
    }

   private void parseStudentData(String responseBody) {
    try {
        JSONObject json = new JSONObject(responseBody);
        JSONArray values = json.getJSONArray("values");

        // Clear existing list
        allStudents.clear();
        int validRows = 0;

        // Skip header (start from i = 1)
        for (int i = 1; i < values.length(); i++) {
            try {
                JSONArray row = values.getJSONArray(i);
                if (row.length() >= 5) {  // Make sure we have all required columns
                    // Column order: 0: Name, 1: Email, 2: Year, 3: Group, 4: Site
                    String name = row.optString(0).trim();
                    String year = row.optString(2).trim();
                    String group = row.optString(3).trim();
                    String site = row.optString(4).trim();

                    // Only add if name is not empty
                    if (!name.isEmpty()) {
                        allStudents.add(new Student(name, group, year, site));
                        validRows++;
                        Log.d("AttendanceDashboard", "Added student: " + name);
                    }
                }
            } catch (JSONException e) {
                Log.e("AttendanceDashboard", "Error parsing row " + i, e);
                // Continue with next row if there's an error
            }
        }

        final int finalValidRows = validRows;
        runOnUiThread(() -> {
            if (allStudents.isEmpty()) {
                Toast.makeText(AttendanceDashboard.this,
                    "No valid student data found. Using sample data.",
                    Toast.LENGTH_LONG).show();
                loadStudentData();
            } else {
                Toast.makeText(AttendanceDashboard.this,
                    "Loaded " + finalValidRows + " students",
                    Toast.LENGTH_SHORT).show();
                applyFilters();
            }
        });

    } catch (JSONException e) {
        Log.e("AttendanceDashboard", "Error parsing response", e);
        runOnUiThread(() -> {
            Toast.makeText(AttendanceDashboard.this,
                "Error parsing data. Using sample data.",
                Toast.LENGTH_LONG).show();
            loadStudentData();
        });
    }
}

}
