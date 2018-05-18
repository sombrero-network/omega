package network.omega.ui.login;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import library.assistant.util.LibraryAssistantUtil;
import network.omega.ui.main.Main;
import network.omega.ui.main.MainController;
import network.omega.ui.preferences.ManageLocalStorage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PasswordBackupNotifyController implements Initializable {


    @FXML
    private AnchorPane rootPane;

    @FXML
    public void handleOpenPassLocationAction(ActionEvent actionEvent) {
        ManageLocalStorage.openPasswordFileFolder();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rootPane.setOpacity(0);
        fadeIn();
    }

    private void fadeIn() {
        FadeTransition ft = new FadeTransition();
        ft.setDuration(Duration.millis(500));
        ft.setNode(rootPane);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            }
        });
        ft.play();
    }

    private void closeStage() {
        ((Stage) rootPane.getScene().getWindow()).close();
    }

    void loadMain() {
        try {
            Parent parent = FXMLLoader.load(getClass().getResource("/network/omega/ui/main/main.fxml"));
            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setTitle("OMEGA Governance");
            Scene scene = new Scene(parent);
            scene.setFill(Main.BACKGROUND_FILL);
            stage.setScene(scene);
            stage.show();
            LibraryAssistantUtil.setStageIcon(stage);
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void continueAction(ActionEvent actionEvent) {
        closeStage();
        loadMain();
    }
}
