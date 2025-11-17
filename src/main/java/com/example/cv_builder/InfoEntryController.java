package com.example.cv_builder;

import javafx.beans.property.SimpleSetProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

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


    @FXML public void initialize(){
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
            data.add(new Education(tspan.getText(),subject.getText(),varsity.getText(),Double.parseDouble(cg.getText())));
            tspan.clear();
            varsity.clear();
            subject.clear();
            cg.clear();
        } catch (Exception ex) {}

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


    private void go(ActionEvent event, String fxml){
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1300,850));
            stage.setTitle("CV Preview");
            stage.show();
        }
        catch(Exception e) {e.printStackTrace();}
    }

    @FXML void ProceedToPreview (ActionEvent event){go(event, "PreviewPage.fxml");}
}
