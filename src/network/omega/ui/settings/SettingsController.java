package network.omega.ui.settings;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import network.omega.ui.preferences.ManageLocalStorage;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    
    @FXML
    private JFXTextField nDaysWithoutFine;
    @FXML
    private JFXTextField finePerDay;
    @FXML
    private JFXTextField username;
    @FXML
    private JFXPasswordField password;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //initDefaultValues();
    }    
    

    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        ((Stage)nDaysWithoutFine.getScene().getWindow()).close();
    }


    @FXML
    public void handleOpenPassLocationAction(ActionEvent actionEvent) {
        ManageLocalStorage.openPasswordFileFolder();
    }


}
