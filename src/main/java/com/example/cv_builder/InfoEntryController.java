package com.example.cv_builder;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;

public class InfoEntryController {
    @FXML private TextField emailTextField;
    @FXML private TextField phoneTextField;
    @FXML private TextArea addressTextField;
    @FXML private TextField nameTextField;

    @FXML public void initialize(){
        setupEmailField();
        setupPhoneField();
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
            return text.matches("[0-9+]*") ? change : null;
        });
        phoneTextField.setTextFormatter(numericFormatter);
    }
}
