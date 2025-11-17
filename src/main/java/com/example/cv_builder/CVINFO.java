package com.example.cv_builder;

import java.util.ArrayList;

public class CVINFO {
            private String FullName, PhoneNumber, Email, Address;
            private ArrayList<String> Skill;
            private ArrayList<Education>  EduInfo;
            private ArrayList<WorkExperience> WorkInfo;

            public CVINFO(String fullName, String phoneNumber, String email, String address, ArrayList<String> skill, ArrayList<Education> eduInfo, ArrayList<WorkExperience> workInfo) {
                    this.FullName = fullName;
                    this.PhoneNumber = phoneNumber;
                    this.Email = email;
                    this.Address = address;
                    this.Skill = skill;
                    this.EduInfo = eduInfo;
                    this.WorkInfo = workInfo;
            }
            public String getFullName() { return FullName; }
            public String getPhoneNumber() { return PhoneNumber; }
    public String getEmail() { return Email; }
    public String getAddress() { return Address; }
    public ArrayList<String> getSkill() { return Skill; }
    public ArrayList<Education> getEduInfo() { return EduInfo; }
    public ArrayList<WorkExperience> getWorkInfo() { return WorkInfo; }


}
