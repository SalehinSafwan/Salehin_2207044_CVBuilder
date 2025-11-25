package com.example.cv_builder;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.util.stream.Collectors;

public class PreviewPageController {

    @FXML
          private Label workExperienceTable;
    @FXML private Label educationTable;
    @FXML private Label skillTable;

    @FXML private Label name, email, phone, address;

    public void setCVInfo(CVINFO cv) {
        name.setText(cv.getFullName());
        email.setText(cv.getEmail());
        phone.setText(cv.getPhoneNumber());
        address.setText(cv.getAddress());

        skillTable.setText(String.join(", " , cv.getSkill()));

        String setEduinfo = cv.getEduInfo().stream().map(Education::toString).collect(Collectors.joining("\n"));
        educationTable.setText(setEduinfo);

        String setWorkExp = cv.getWorkInfo().stream().map(WorkExperience::toString).collect(Collectors.joining("\n"));
        workExperienceTable.setText(setWorkExp);
    }
    
    @FXML
    public void BackToHome(ActionEvent event) {
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
