package com.example.emsismartpresence;

/**
 * Model class for schedule items
 */
public class ScheduleItem {


    private String id;
    private String Module;
    private String day;


    private String start_session;
    private String end_session;
    private String description;

    public ScheduleItem() {
        // Required empty constructor for Firebase
    }

    public ScheduleItem(String module, String day, String id, String start_session, String end_session, String description) {
        Module = module;
        this.day = day;
        this.id = id;
        this.start_session = start_session;
        this.end_session = end_session;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModule() {
        return Module;
    }

    public void setModule(String module) {
        Module = module;
    }

    public String getStart_session() {
        return start_session;
    }

    public void setStart_session(String start_session) {
        this.start_session = start_session;
    }

    public String getEnd_session() {
        return end_session;
    }

    public void setEnd_session(String end_session) {
        this.end_session = end_session;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }




    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
