package com.example.cv_builder;

public class Education {
    private String timespan, university, degree;
    private double cgpa;

    public Education(String timespan, String university, String degree, double cgpa) {
        this.timespan = timespan;
        this.university = university;
        this.degree = degree;
        this.cgpa = cgpa;
    }
    public String getTimespan() {      return timespan;    }
    public String getUniversity() { return university; }
    public String getDegree() { return degree; }
    public double getCgpa() { return cgpa; }

}
