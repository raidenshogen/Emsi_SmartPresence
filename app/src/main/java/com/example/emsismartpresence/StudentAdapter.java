package com.example.emsismartpresence;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {
    private List<Student> students;

    public StudentAdapter(List<Student> students) {
        this.students = students;
    }
    public Student getItem(int position) {
        return students.get(position);
    }
    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_row, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = students.get(position);
        holder.bind(student);
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView studentName;
        CheckBox checkPresent, checkAbsent;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.studentName);
            checkPresent = itemView.findViewById(R.id.checkPresent);
            checkAbsent = itemView.findViewById(R.id.checkAbsent);
        }

        void bind(Student student) {
            studentName.setText(student.getName());

            // Set checkbox states based on the student model
            checkPresent.setChecked(student.isPresent());
            checkAbsent.setChecked(student.isAbsent());

            // Remove previous listeners to prevent multiple calls
            checkPresent.setOnCheckedChangeListener(null);
            checkAbsent.setOnCheckedChangeListener(null);

            // Add new listeners
            checkPresent.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    checkAbsent.setChecked(false);
                    student.setPresent(true);
                    student.setAbsent(false);
                    student.setStatus("Present");
                } else if (!checkAbsent.isChecked()) {
                    student.setPresent(false);
                    student.setStatus(null);
                }
            });

            checkAbsent.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    checkPresent.setChecked(false);
                    student.setAbsent(true);
                    student.setPresent(false);
                    student.setStatus("Absent");
                } else if (!checkPresent.isChecked()) {
                    student.setAbsent(false);
                    student.setStatus(null);
                }
            });
        }
    }

}