package com.example.cv_builder;

import javafx.application.Platform;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Optional;

public class InfoEntryController {
    @FXML private TextField emailTextField;
    @FXML private TextField phoneTextField;
    @FXML private TextArea addressTextField;
    @FXML private TextField nameTextField;
    @FXML private Button ProceedButton;

    @FXML private TableView<Education> edtable;
    @FXML private TableColumn<Education, String > range,uni,deg;
    @FXML private TableColumn<Education,Float> cgpa;
    @FXML private TextField tspan, varsity, subject,cg;

    @FXML private TableView<WorkExperience> worktable;
    @FXML private TableColumn<WorkExperience,String> company, jobtitle, timeline,description;
    @FXML private TextField place, rank, time;
    @FXML private TextArea desc;

    @FXML private TableView<String> skilltable;
    @FXML private TableColumn<String,String> skill;
    @FXML private TextField sk;

    private final ObservableList<Education> data = FXCollections.observableArrayList();
    private final ObservableList<WorkExperience> dat2 =  FXCollections.observableArrayList();
    private final ObservableList<String> skdat = FXCollections.observableArrayList();
    
    private int currentCVId = -1;


    @FXML public void initialize(){
        DBUtil.createTables();
        
        setupEmailField();
        setupPhoneField();
        setupValidation();
        range.setCellValueFactory(new PropertyValueFactory<>("timespan"));
        uni.setCellValueFactory(new PropertyValueFactory<>("university"));
        deg.setCellValueFactory(new PropertyValueFactory<>("degree"));
        cgpa.setCellValueFactory(new PropertyValueFactory<>("cgpa"));
             edtable.setItems(data);
        company.setCellValueFactory(new PropertyValueFactory<>("company"));
        jobtitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        timeline.setCellValueFactory(new PropertyValueFactory<>("timeline"));
        description.setCellValueFactory(new PropertyValueFactory<>("description"));
                 worktable.setItems(dat2);
        skill.setCellValueFactory(celldata -> new SimpleStringProperty(celldata.getValue()));
                skilltable.setItems(skdat);
        
        setupDeleteHandlers();
    }
    
    private void setupDeleteHandlers() {
        edtable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                Education selected = edtable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    deleteEducationEntry(selected);
                }
            }
        });
        
        worktable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                WorkExperience selected = worktable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    deleteWorkEntry(selected);
                }
            }
        });
        
        skilltable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                String selected = skilltable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    deleteSkillEntry(selected);
                }
            }
        });
    }
    
    private void deleteEducationEntry(Education edu) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Education Entry");
        confirm.setContentText("Are you sure you want to delete this education entry?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            data.remove(edu);
            
            if (currentCVId > 0) {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        DBUtil.deleteEducation(currentCVId, edu);
                        return null;
                    }
                };
                
                task.setOnFailed(e -> showError("Database Error", "Failed to delete education from database."));
                new Thread(task).start();
            }
        }
    }
    
    private void deleteWorkEntry(WorkExperience work) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Work Experience Entry");
        confirm.setContentText("Are you sure you want to delete this work experience entry?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            dat2.remove(work);
            
            if (currentCVId > 0) {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        DBUtil.deleteWorkExperience(currentCVId, work);
                        return null;
                    }
                };
                
                task.setOnFailed(e -> showError("Database Error", "Failed to delete work experience from database."));
                new Thread(task).start();
            }
        }
    }
    
    private void deleteSkillEntry(String skill) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Skill");
        confirm.setContentText("Are you sure you want to delete this skill?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            skdat.remove(skill);
            
            if (currentCVId > 0) {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        DBUtil.deleteSkill(currentCVId, skill);
                        return null;
                    }
                };
                
                task.setOnFailed(e -> showError("Database Error", "Failed to delete skill from database."));
                new Thread(task).start();
            }
        }
    }
    
    public void loadCV(int cvId, CVINFO cv) {
        this.currentCVId = cvId;
        
        nameTextField.setText(cv.getFullName());
        phoneTextField.setText(cv.getPhoneNumber());
        emailTextField.setText(cv.getEmail());
        addressTextField.setText(cv.getAddress());
        
        data.clear();
        data.addAll(cv.getEduInfo());
        
        dat2.clear();
        dat2.addAll(cv.getWorkInfo());
        
        skdat.clear();
        skdat.addAll(cv.getSkill());
    }

    @FXML public void addSkill(){
        try{
            String input = sk.getText().trim();
            if(!input.isEmpty()){
                skdat.add(input);
                sk.clear();
            }
        }catch (Exception e){}
    }

    @FXML public void addWork(){
        try {
            dat2.add(new WorkExperience(place.getText(),rank.getText(),time.getText(),desc.getText()));
                    place.clear(); rank.clear(); time.clear(); desc.clear();
        } catch (Exception ex){}
    }

    @FXML public void addEducation(){
        try {
            String timespanText = tspan.getText().trim();
            String universityText = varsity.getText().trim();
            String degreeText = subject.getText().trim();
            String cgpaText = cg.getText().trim();
            
            if (timespanText.isEmpty() || universityText.isEmpty() || degreeText.isEmpty() || cgpaText.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Missing Information");
                alert.setHeaderText(null);
                alert.setContentText("Please fill all education fields.");
                alert.showAndWait();
                return;
            }
            
            double cgpaValue = Double.parseDouble(cgpaText);
            Education newEdu = new Education(timespanText, universityText, degreeText, cgpaValue);
            data.add(newEdu);
            
            tspan.clear();
            varsity.clear();
            subject.clear();
            cg.clear();
        } catch (NumberFormatException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid CGPA");
            alert.setHeaderText(null);
            alert.setContentText("CGPA must be a valid number.");
            alert.showAndWait();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to add education: " + ex.getMessage());
            alert.showAndWait();
            ex.printStackTrace();
        }

    }

    private void setupValidation() {
        ProceedButton.setDisable(true);
        Runnable validateFields = () -> {
            boolean allFilled = !nameTextField.getText().trim().isEmpty()
                    && !emailTextField.getText().trim().isEmpty()
                    && !phoneTextField.getText().trim().isEmpty()
                    && !addressTextField.getText().trim().isEmpty();
            ProceedButton.setDisable(!allFilled);
        };

        nameTextField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        emailTextField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        phoneTextField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        addressTextField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());

    }

    private void setupEmailField() {
        emailTextField.textProperty().addListener((obs, oldValue, newValue) -> {if (!newValue.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
            emailTextField.setStyle("-fx-background-color: red;");
        } else {
            emailTextField.setStyle(null);
        }});
    }

    private void setupPhoneField() {
        TextFormatter<String> numericFormatter = new TextFormatter<>(change -> {
            String text = change.getControlNewText();
            return text.matches("[+]*[0-9]*") ? change : null;
        });
        phoneTextField.setTextFormatter(numericFormatter);
    }

    void ShowSucces(){
        new Alert(Alert.AlertType.INFORMATION,"CV saved successfully!",ButtonType.OK).show();
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

    private void go(ActionEvent event, String fxml){
        ProceedButton.setDisable(true);
        
        CVINFO cv = new CVINFO(
            nameTextField.getText(),
            phoneTextField.getText(),
            emailTextField.getText(),
            addressTextField.getText(),
            new ArrayList<>(skilltable.getItems()),
            new ArrayList<>(edtable.getItems()), 
            new ArrayList<>(worktable.getItems())
        );

        Task<Integer> saveTask = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                if (currentCVId > 0) {
                    DBUtil.updateCV(currentCVId, cv, 
                                   new ArrayList<>(edtable.getItems()),
                                   new ArrayList<>(worktable.getItems()),
                                   new ArrayList<>(skilltable.getItems()));
                    return currentCVId;
                } else {
                    return DBUtil.insertCV(cv, 
                                          new ArrayList<>(edtable.getItems()),
                                          new ArrayList<>(worktable.getItems()),
                                          new ArrayList<>(skilltable.getItems()));
                }
            }
        };

        saveTask.setOnSucceeded(e -> {
            Integer savedCVId = saveTask.getValue();
            currentCVId = savedCVId;
            
            ShowSucces();
            
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
                Parent root = loader.load();

                PreviewPageController controller = loader.getController();
                controller.setCVInfo(cv);

                Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root, 700, 900));
                stage.setTitle("CV Preview");
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Navigation Error", "Failed to open preview page: " + ex.getMessage());
            }
            
            ProceedButton.setDisable(false);
        });

        saveTask.setOnFailed(e -> {
            Throwable exception = saveTask.getException();
            exception.printStackTrace();
            showError("Database Error", "Failed to save CV to database: " + exception.getMessage());
            ProceedButton.setDisable(false);
        });

        new Thread(saveTask).start();
    }

    @FXML void ProceedToPreview (ActionEvent event){go(event, "PreviewPage.fxml");}
    
    @FXML void BackToHome(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Homepage.fxml"));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1300, 850));
            stage.setTitle("CV Builder");
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
