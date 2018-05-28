package network.omega.ui.main.toolbar;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import library.assistant.util.LibraryAssistantUtil;
import network.omega.ui.main.MainController;

import java.net.URL;
import java.util.ResourceBundle;

public class ToolbarController implements Initializable {
    
    @FXML
    private VBox rootVBox;
    
    private MainController parentController;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
    
    @FXML
    private void provideResource(ActionEvent event) {
        // LibraryAssistantUtil.loadWindow(getClass().getResource("/library/assistant/ui/addmember/member_add.fxml"),
        // "Add New Member", null);
        parentController.handleAddMyResource(null);
    }
    
    @FXML
    private void consumeResource(ActionEvent event) {
        // LibraryAssistantUtil.loadWindow(getClass().getResource("/library/assistant/ui/addbook/add_book.fxml"),
        // "Add New Book", null);
    }
    
    @FXML
    private void loadSettings(ActionEvent event) {
        LibraryAssistantUtil.loadWindow(getClass().getResource("/network/omega/ui/settings/settings.fxml"), "Settings",
                null);
    }
    
    public void setParentController(MainController controller) {
        this.parentController = controller;
    }
    
}
