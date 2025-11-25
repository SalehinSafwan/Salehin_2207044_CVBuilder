package com.example.cv_builder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBUtil {
    private static final String DB_URL = "jdbc:sqlite:cvbuilder.db";

    public static Connection connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }
        Connection conn = DriverManager.getConnection(DB_URL);
        conn.createStatement().execute("PRAGMA foreign_keys = ON");
        return conn;
    }

    public static void createTables() {
        String createCVInfoTable = """
            CREATE TABLE IF NOT EXISTS cv_info (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                full_name TEXT NOT NULL,
                phone_number TEXT NOT NULL,
                email TEXT NOT NULL,
                address TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createEducationTable = """
            CREATE TABLE IF NOT EXISTS education (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cv_id INTEGER NOT NULL,
                timespan TEXT NOT NULL,
                university TEXT NOT NULL,
                degree TEXT NOT NULL,
                cgpa REAL NOT NULL,
                FOREIGN KEY (cv_id) REFERENCES cv_info(id) ON DELETE CASCADE
            )
        """;

        String createWorkExperienceTable = """
            CREATE TABLE IF NOT EXISTS work_experience (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cv_id INTEGER NOT NULL,
                company TEXT NOT NULL,
                title TEXT NOT NULL,
                timeline TEXT NOT NULL,
                description TEXT NOT NULL,
                FOREIGN KEY (cv_id) REFERENCES cv_info(id) ON DELETE CASCADE
            )
        """;

        String createSkillsTable = """
            CREATE TABLE IF NOT EXISTS skills (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cv_id INTEGER NOT NULL,
                skill_name TEXT NOT NULL,
                FOREIGN KEY (cv_id) REFERENCES cv_info(id) ON DELETE CASCADE
            )
        """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(createCVInfoTable);
            stmt.execute(createEducationTable);
            stmt.execute(createWorkExperienceTable);
            stmt.execute(createSkillsTable);
            
            System.out.println("Database tables created successfully!");
            
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static int insertCV(CVINFO cv, List<Education> eduList, List<WorkExperience> workList, List<String> skills) throws SQLException {
        String insertCVSQL = "INSERT INTO cv_info (full_name, phone_number, email, address) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            
            try {
                // Insert main CV info
                int cvId;
                try (PreparedStatement pstmt = conn.prepareStatement(insertCVSQL, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, cv.getFullName());
                    pstmt.setString(2, cv.getPhoneNumber());
                    pstmt.setString(3, cv.getEmail());
                    pstmt.setString(4, cv.getAddress());
                    pstmt.executeUpdate();
                    
                    ResultSet rs = pstmt.getGeneratedKeys();
                    if (rs.next()) {
                        cvId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to get generated CV ID");
                    }
                }
                
                // Insert education records
                if (eduList != null && !eduList.isEmpty()) {
                    String insertEduSQL = "INSERT INTO education (cv_id, timespan, university, degree, cgpa) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(insertEduSQL)) {
                        for (Education edu : eduList) {
                            pstmt.setInt(1, cvId);
                            pstmt.setString(2, edu.getTimespan());
                            pstmt.setString(3, edu.getUniversity());
                            pstmt.setString(4, edu.getDegree());
                            pstmt.setDouble(5, edu.getCgpa());
                            pstmt.addBatch();
                        }
                        pstmt.executeBatch();
                    }
                }
                
                // Insert work experience records
                if (workList != null && !workList.isEmpty()) {
                    String insertWorkSQL = "INSERT INTO work_experience (cv_id, company, title, timeline, description) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(insertWorkSQL)) {
                        for (WorkExperience work : workList) {
                            pstmt.setInt(1, cvId);
                            pstmt.setString(2, work.getCompany());
                            pstmt.setString(3, work.getTitle());
                            pstmt.setString(4, work.getTimeline());
                            pstmt.setString(5, work.getDescription());
                            pstmt.addBatch();
                        }
                        pstmt.executeBatch();
                    }
                }
                
                // Insert skills
                if (skills != null && !skills.isEmpty()) {
                    String insertSkillSQL = "INSERT INTO skills (cv_id, skill_name) VALUES (?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(insertSkillSQL)) {
                        for (String skill : skills) {
                            pstmt.setInt(1, cvId);
                            pstmt.setString(2, skill);
                            pstmt.addBatch();
                        }
                        pstmt.executeBatch();
                    }
                }
                
                conn.commit();
                return cvId;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public static List<CVINFO> getAllCVs() throws SQLException {
        List<CVINFO> cvList = new ArrayList<>();
        String query = "SELECT id, full_name, phone_number, email, address FROM cv_info ORDER BY created_at DESC";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                int cvId = rs.getInt("id");
                String fullName = rs.getString("full_name");
                String phoneNumber = rs.getString("phone_number");
                String email = rs.getString("email");
                String address = rs.getString("address");
                
                // Get related data
                List<Education> eduList = getEducationByCV(cvId);
                List<WorkExperience> workList = getWorkByCV(cvId);
                List<String> skills = getSkillsByCV(cvId);
                
                CVINFO cv = new CVINFO(fullName, phoneNumber, email, address, 
                                      new ArrayList<>(skills), 
                                      new ArrayList<>(eduList), 
                                      new ArrayList<>(workList));
                
                cvList.add(cv);
            }
        }
        
        return cvList;
    }

    public static CVINFO getCVById(int cvId) throws SQLException {
        String query = "SELECT full_name, phone_number, email, address FROM cv_info WHERE id = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, cvId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String fullName = rs.getString("full_name");
                String phoneNumber = rs.getString("phone_number");
                String email = rs.getString("email");
                String address = rs.getString("address");
                
                List<Education> eduList = getEducationByCV(cvId);
                List<WorkExperience> workList = getWorkByCV(cvId);
                List<String> skills = getSkillsByCV(cvId);
                
                return new CVINFO(fullName, phoneNumber, email, address, 
                                new ArrayList<>(skills), 
                                new ArrayList<>(eduList), 
                                new ArrayList<>(workList));
            }
        }
        
        return null;
    }

    public static List<Education> getEducationByCV(int cvId) throws SQLException {
        List<Education> eduList = new ArrayList<>();
        String query = "SELECT timespan, university, degree, cgpa FROM education WHERE cv_id = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, cvId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Education edu = new Education(
                    rs.getString("timespan"),
                    rs.getString("university"),
                    rs.getString("degree"),
                    rs.getDouble("cgpa")
                );
                eduList.add(edu);
            }
        }
        
        return eduList;
    }

    public static List<WorkExperience> getWorkByCV(int cvId) throws SQLException {
        List<WorkExperience> workList = new ArrayList<>();
        String query = "SELECT company, title, timeline, description FROM work_experience WHERE cv_id = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, cvId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                WorkExperience work = new WorkExperience(
                    rs.getString("company"),
                    rs.getString("title"),
                    rs.getString("timeline"),
                    rs.getString("description")
                );
                workList.add(work);
            }
        }
        
        return workList;
    }

    public static List<String> getSkillsByCV(int cvId) throws SQLException {
        List<String> skills = new ArrayList<>();
        String query = "SELECT skill_name FROM skills WHERE cv_id = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, cvId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                skills.add(rs.getString("skill_name"));
            }
        }
        
        return skills;
    }

    public static void updateCV(int cvId, CVINFO cv, List<Education> eduList, List<WorkExperience> workList, List<String> skills) throws SQLException {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            
            try {
                // Update main CV info
                String updateCVSQL = "UPDATE cv_info SET full_name = ?, phone_number = ?, email = ?, address = ? WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateCVSQL)) {
                    pstmt.setString(1, cv.getFullName());
                    pstmt.setString(2, cv.getPhoneNumber());
                    pstmt.setString(3, cv.getEmail());
                    pstmt.setString(4, cv.getAddress());
                    pstmt.setInt(5, cvId);
                    pstmt.executeUpdate();
                }
                
                // Delete existing related records
                deleteRelatedRecords(conn, cvId);
                
                // Insert new education records
                if (eduList != null && !eduList.isEmpty()) {
                    String insertEduSQL = "INSERT INTO education (cv_id, timespan, university, degree, cgpa) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(insertEduSQL)) {
                        for (Education edu : eduList) {
                            pstmt.setInt(1, cvId);
                            pstmt.setString(2, edu.getTimespan());
                            pstmt.setString(3, edu.getUniversity());
                            pstmt.setString(4, edu.getDegree());
                            pstmt.setDouble(5, edu.getCgpa());
                            pstmt.addBatch();
                        }
                        pstmt.executeBatch();
                    }
                }
                
                // Insert new work experience records
                if (workList != null && !workList.isEmpty()) {
                    String insertWorkSQL = "INSERT INTO work_experience (cv_id, company, title, timeline, description) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(insertWorkSQL)) {
                        for (WorkExperience work : workList) {
                            pstmt.setInt(1, cvId);
                            pstmt.setString(2, work.getCompany());
                            pstmt.setString(3, work.getTitle());
                            pstmt.setString(4, work.getTimeline());
                            pstmt.setString(5, work.getDescription());
                            pstmt.addBatch();
                        }
                        pstmt.executeBatch();
                    }
                }
                
                // Insert new skills
                if (skills != null && !skills.isEmpty()) {
                    String insertSkillSQL = "INSERT INTO skills (cv_id, skill_name) VALUES (?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(insertSkillSQL)) {
                        for (String skill : skills) {
                            pstmt.setInt(1, cvId);
                            pstmt.setString(2, skill);
                            pstmt.addBatch();
                        }
                        pstmt.executeBatch();
                    }
                }
                
                conn.commit();
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public static void deleteCV(int cvId) throws SQLException {
        String deleteSQL = "DELETE FROM cv_info WHERE id = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            
            pstmt.setInt(1, cvId);
            pstmt.executeUpdate();
        }
    }

    private static void deleteRelatedRecords(Connection conn, int cvId) throws SQLException {
        String deleteEdu = "DELETE FROM education WHERE cv_id = ?";
        String deleteWork = "DELETE FROM work_experience WHERE cv_id = ?";
        String deleteSkills = "DELETE FROM skills WHERE cv_id = ?";
        
        try (PreparedStatement pstmt1 = conn.prepareStatement(deleteEdu);
             PreparedStatement pstmt2 = conn.prepareStatement(deleteWork);
             PreparedStatement pstmt3 = conn.prepareStatement(deleteSkills)) {
            
            pstmt1.setInt(1, cvId);
            pstmt1.executeUpdate();
            
            pstmt2.setInt(1, cvId);
            pstmt2.executeUpdate();
            
            pstmt3.setInt(1, cvId);
            pstmt3.executeUpdate();
        }
    }

    public static void deleteEducation(int cvId, Education edu) throws SQLException {
        String deleteSQL = "DELETE FROM education WHERE cv_id = ? AND timespan = ? AND university = ? AND degree = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            
            pstmt.setInt(1, cvId);
            pstmt.setString(2, edu.getTimespan());
            pstmt.setString(3, edu.getUniversity());
            pstmt.setString(4, edu.getDegree());
            pstmt.executeUpdate();
        }
    }

    public static void deleteWorkExperience(int cvId, WorkExperience work) throws SQLException {
        String deleteSQL = "DELETE FROM work_experience WHERE cv_id = ? AND company = ? AND title = ? AND timeline = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            
            pstmt.setInt(1, cvId);
            pstmt.setString(2, work.getCompany());
            pstmt.setString(3, work.getTitle());
            pstmt.setString(4, work.getTimeline());
            pstmt.executeUpdate();
        }
    }

    public static void deleteSkill(int cvId, String skill) throws SQLException {
        String deleteSQL = "DELETE FROM skills WHERE cv_id = ? AND skill_name = ? LIMIT 1";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            
            pstmt.setInt(1, cvId);
            pstmt.setString(2, skill);
            pstmt.executeUpdate();
        }
    }
}
