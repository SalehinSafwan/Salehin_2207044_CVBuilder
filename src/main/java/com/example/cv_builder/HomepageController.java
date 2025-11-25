package com.example.cv_builder;

import com.almasb.fxgl.entity.action.Action;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HomepageController {
    private void go(ActionEvent event, String fxml){
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1300,850));
            stage.setTitle("Information Tab");
            stage.show();
        }
        catch(Exception e) {e.printStackTrace();}
    }

@FXML void OpenInfoEntryPage (ActionEvent event){go(event, "InfoEntry.fxml");}
    @FXML void OpenLoadPage (ActionEvent event){go(event, "LoadPage.fxml");}
    @FXML void ExitProgram(ActionEvent event){System.exit(0);}
}
