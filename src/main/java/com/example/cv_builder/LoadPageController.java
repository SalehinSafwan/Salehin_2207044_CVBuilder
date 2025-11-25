package com.example.cv_builder;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class LoadPageController {

    @FXML private TableView<CVTableData> cvTable;
    @FXML private TableColumn<CVTableData, Integer> idColumn;
    @FXML private TableColumn<CVTableData, String> nameColumn;
    @FXML private TableColumn<CVTableData, String> emailColumn;
    @FXML private TableColumn<CVTableData, String> phoneColumn;
    @FXML private TableColumn<CVTableData, String> addressColumn;
    @FXML private TableColumn<CVTableData, String> educationColumn;
    @FXML private TableColumn<CVTableData, String> skillsColumn;
    @FXML private Button loadButton;
    @FXML private Button deleteButton;

    private ObservableList<CVTableData> cvDataList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        phoneColumn.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
        addressColumn.setCellValueFactory(cellData -> cellData.getValue().addressProperty());
        educationColumn.setCellValueFactory(cellData -> cellData.getValue().educationProperty());
        skillsColumn.setCellValueFactory(cellData -> cellData.getValue().skillsProperty());

        cvTable.setItems(cvDataList);

        loadAllCVs();
    }

    private void loadAllCVs() {
        Task<List<CVTableData>> task = new Task<>() {
            @Override
            protected List<CVTableData> call() throws Exception {
                List<CVINFO> cvList = DBUtil.getAllCVs();
                return cvList.stream()
                        .map(cv -> {
                            int id = getCVIdFromDatabase(cv);
                            String eduSummary = cv.getEduInfo().isEmpty() ? "None" : 
                                cv.getEduInfo().get(0).getUniversity() + (cv.getEduInfo().size() > 1 ? " +" + (cv.getEduInfo().size()-1) : "");
                            String skillsSummary = cv.getSkill().isEmpty() ? "None" : 
                                String.join(", ", cv.getSkill().size() > 3 ? cv.getSkill().subList(0, 3) : cv.getSkill()) + 
                                (cv.getSkill().size() > 3 ? "..." : "");
                            return new CVTableData(
                                    id,
                                    cv.getFullName(),
                                    cv.getEmail(),
                                    cv.getPhoneNumber(),
                                    cv.getAddress(),
                                    eduSummary,
                                    skillsSummary
                            );
                        })
                        .toList();
            }
        };

        task.setOnSucceeded(e -> {
            cvDataList.setAll(task.getValue());
        });

        task.setOnFailed(e -> {
            showError("Database Error", "Failed to load CVs from database: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private int getCVIdFromDatabase(CVINFO cv) {
        try {
            String query = "SELECT id FROM cv_info WHERE full_name = ? AND email = ? ORDER BY created_at DESC LIMIT 1";
            try (var conn = DBUtil.connect();
                 var pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, cv.getFullName());
                pstmt.setString(2, cv.getEmail());
                var rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @FXML
    void handleLoadCV(ActionEvent event) {
        CVTableData selected = cvTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            showWarning("No Selection", "Please select a CV to load.");
            return;
        }

        loadButton.setDisable(true);
        
        Task<CVINFO> task = new Task<>() {
            @Override
            protected CVINFO call() throws Exception {
                return DBUtil.getCVById(selected.getId());
            }
        };

        task.setOnSucceeded(e -> {
            CVINFO cv = task.getValue();
            if (cv != null) {
                navigateToInfoEntryWithData(event, cv, selected.getId());
            } else {
                showError("Load Error", "CV not found in database.");
                loadButton.setDisable(false);
            }
        });

        task.setOnFailed(e -> {
            showError("Database Error", "Failed to load CV: " + task.getException().getMessage());
            loadButton.setDisable(false);
        });

        new Thread(task).start();
    }

    private void navigateToInfoEntryWithData(ActionEvent event, CVINFO cv, int cvId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("InfoEntry.fxml"));
            Parent root = loader.load();

            InfoEntryController controller = loader.getController();
            controller.loadCV(cvId, cv);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1300, 850));
            stage.setTitle("Edit CV - Information Entry");
            stage.show();

        } catch (Exception e) {
            showError("Navigation Error", "Failed to load Info Entry page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleDeleteCV(ActionEvent event) {
        CVTableData selected = cvTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            showWarning("No Selection", "Please select a CV to delete.");
            return;
        }

        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete CV: " + selected.getName());
        confirmAlert.setContentText("Are you sure you want to delete this CV? This action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteButton.setDisable(true);

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    DBUtil.deleteCV(selected.getId());
                    return null;
                }
            };

            task.setOnSucceeded(e -> {
                cvDataList.remove(selected);
                showInfo("Success", "CV deleted successfully!");
                deleteButton.setDisable(false);
            });

            task.setOnFailed(e -> {
                showError("Database Error", "Failed to delete CV: " + task.getException().getMessage());
                deleteButton.setDisable(false);
            });

            new Thread(task).start();
        }
    }

    @FXML
    void handleBackToHome(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Homepage.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1300, 850));
            stage.setTitle("CV Builder");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class CVTableData {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty email;
        private final SimpleStringProperty phone;
        private final SimpleStringProperty address;
        private final SimpleStringProperty education;
        private final SimpleStringProperty skills;

        public CVTableData(int id, String name, String email, String phone, String address, String education, String skills) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.email = new SimpleStringProperty(email);
            this.phone = new SimpleStringProperty(phone);
            this.address = new SimpleStringProperty(address);
            this.education = new SimpleStringProperty(education);
            this.skills = new SimpleStringProperty(skills);
        }

        public int getId() { return id.get(); }
        public SimpleIntegerProperty idProperty() { return id; }

        public String getName() { return name.get(); }
        public SimpleStringProperty nameProperty() { return name; }

        public String getEmail() { return email.get(); }
        public SimpleStringProperty emailProperty() { return email; }

        public String getPhone() { return phone.get(); }
        public SimpleStringProperty phoneProperty() { return phone; }
        
        public String getAddress() { return address.get(); }
        public SimpleStringProperty addressProperty() { return address; }
        
        public String getEducation() { return education.get(); }
        public SimpleStringProperty educationProperty() { return education; }
        
        public String getSkills() { return skills.get(); }
        public SimpleStringProperty skillsProperty() { return skills; }
    }
}
