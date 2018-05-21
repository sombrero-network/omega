package network.omega.ui.about;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import library.assistant.alert.AlertMaker;
import library.assistant.util.LibraryAssistantUtil;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

public class AboutController implements Initializable {

    private static final String LINK_1 = "https://github.com/omegacoinnetwork";
    private static final String LINK_2 = "http://omegacoin.network/";
    private static final String LINK_3 = "https://discord.gg/bfMpDQP";
    private static final String LINK_4 = "https://github.com/sombrero-network";

    @FXML
    private AnchorPane rootPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //AlertMaker.showTrayMessage(String.format("Hello %s!", System.getProperty("user.name")), "Thanks for trying out Library Assistant");

    }

    private void loadWebpage(String url) {
        try {
            if(System.getProperty("os.name").toLowerCase().contains("linux")) {
                String address = new URI(url).toURL().toExternalForm();
                Runtime.getRuntime().exec("x-www-browser " +  address);
            }else {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (IOException | URISyntaxException e1) {
            e1.printStackTrace();
            handleWebpageLoadException(url);
        }
    }

    private void handleWebpageLoadException(String url) {
        WebView browser = new WebView();
        WebEngine webEngine = browser.getEngine();
        webEngine.load(url);
        Stage stage = new Stage();
        Scene scene = new Scene(new StackPane(browser));
        stage.setScene(scene);
        stage.setTitle("Genuine Coder");
        stage.show();
        LibraryAssistantUtil.setStageIcon(stage);
    }

    @FXML
    private void loadLink1(ActionEvent event) {
        loadWebpage(LINK_1);
    }

    @FXML
    private void loadLink2(ActionEvent event) {
        loadWebpage(LINK_2);
    }

    @FXML
    private void loadLink3(ActionEvent event) {
        loadWebpage(LINK_3);
    }
    @FXML
    private void loadLink4(ActionEvent event) {
        loadWebpage(LINK_4);
    }
}
