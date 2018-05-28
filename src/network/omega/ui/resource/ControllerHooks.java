package network.omega.ui.resource;

import javafx.stage.Stage;

public interface ControllerHooks {
    void close();
    
    void toFront();
    
    void setStage(Stage stage);
}
