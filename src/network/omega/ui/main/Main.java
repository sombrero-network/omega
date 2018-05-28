package network.omega.ui.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import library.assistant.util.LibraryAssistantUtil;
import network.omega.ui.preferences.ManageLocalStorage;
import ch.qos.logback.classic.Logger;
import network.omega.ui.utils.LoggerUtils;

import java.util.logging.Level;

public class Main extends Application {
    
    public static Color BACKGROUND_FILL = Color.valueOf("#2A2E37");
    
    // init logger in user settings folder
    public static Logger logger = LoggerUtils.createLoggerFor("main",
            ManageLocalStorage.applicationDir.resolve("omega.log").toFile().getAbsolutePath());
    
    @Override
    public void start(Stage stage) throws Exception {
        
        // if we have a password file already created in local storage with just
        // load the main app
        
        if (ManageLocalStorage.passwordFileExists()) {
            Parent parent = FXMLLoader.load(getClass().getResource("/network/omega/ui/main/main.fxml"));
            Stage stage1 = new Stage(StageStyle.DECORATED);
            stage1.setTitle("OMEGA Governance");
            Scene scene = new Scene(parent);
            scene.setFill(Main.BACKGROUND_FILL);
            LibraryAssistantUtil.setSceneStyle(scene, this);
            stage1.setScene(scene);
            stage1.show();
            LibraryAssistantUtil.setStageIcon(stage1);
            return;
        }
        
        // Omega graphics :
        // https://github.com/omegacoinnetwork/omegacoin/tree/master/src/qt/res/icons
        
        // load login form
        Parent root = FXMLLoader.load(getClass().getResource("/network/omega/ui/login/login.fxml"));
        Scene scene = new Scene(root);
        scene.setFill(BACKGROUND_FILL);
        LibraryAssistantUtil.setSceneStyle(scene, this);
        stage.setScene(scene);
        stage.show();
        stage.setTitle("OMEGA Governance");
        
        LibraryAssistantUtil.setStageIcon(stage);
        
        // new Thread(() -> {
        // DatabaseHandler.getInstance();
        // }).start();
        
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
