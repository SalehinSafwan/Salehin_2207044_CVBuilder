package com.example.cv_builder;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

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

}
