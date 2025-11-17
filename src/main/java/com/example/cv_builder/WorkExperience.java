package com.example.cv_builder;

public class WorkExperience {
    private String company, title, timeline, description;

    public WorkExperience(String company, String title, String timeline, String description) {
        this.company = company;
        this.title = title;
        this.timeline = timeline;
        this.description = description;
    }

    public String toString() {
        return company + "\t\t"+ timeline + "\n\n" + title + "\n" + description + "\n\n";
    }

    public String getCompany() { return company; }
    public String getTitle() { return title; }
    public String getTimeline() { return timeline; }
    public String getDescription() { return description; }
}
