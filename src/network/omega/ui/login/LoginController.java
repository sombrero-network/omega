package network.omega.ui.login;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.api.LookupAccounts;
import cy.agorise.graphenej.api.android.NodeConnection;
import cy.agorise.graphenej.errors.RepeatedRequestIdException;
import cy.agorise.graphenej.interfaces.NodeErrorListener;
import cy.agorise.graphenej.interfaces.WitnessResponseListener;
import cy.agorise.graphenej.models.BaseResponse;
import cy.agorise.graphenej.models.WitnessResponse;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import library.assistant.settings.Preferences;
import library.assistant.ui.main.MainController;
import library.assistant.util.LibraryAssistantUtil;
import network.omega.ui.faucet.RegisterUsingFaucet;
import network.omega.ui.main.Main;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginController implements Initializable {
    
    @FXML
    private AnchorPane rootPane;
    
    @FXML
    private JFXTextField username;
    
    @FXML
    private JFXButton registerButtonId;
    
    @FXML
    private JFXButton quitButtonId;
    // @FXML
    // private JFXPasswordField password;
    
    @FXML
    private JFXTextArea importantMessage;
    
    @FXML
    private Label errorMessage;
    
    Preferences preference;
    
    protected static NodeConnection nodeConnection;
    private static Tooltip t = new Tooltip();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // preference = Preferences.getPreferences();
            nodeConnection = NodeConnection.getInstance();
            nodeConnection.addNodeUrl("wss://testnet.sombrero.network/ws");
            
            // init error message state
            updateErrorMessage("", false, false, false);
        } catch (Exception e) {
            
        }
    }
    
    public void updateErrorMessage(String message, Boolean tooltipIsVisible, Boolean messageIsVisible,
            Boolean errorStyleTextFieldVisible) {
        if (errorStyleTextFieldVisible) {
            username.getStyleClass().add("wrong-credentials");
        } else {
            username.getStyleClass().remove("wrong-credentials");
        }
        if (!tooltipIsVisible) {
            username.setTooltip(null);
        } else {
            t.setText(message);
            username.setTooltip(t);
        }
        errorMessage.setVisible(messageIsVisible);
        errorMessage.setText(message);
        System.out.println(message);
    }
    
    @FXML
    private void handleLoginButtonAction(ActionEvent event) {
        
        // do registration call to faucet with https://testnet.sombrero.network/
        // api call
        // get callback from server that registration went fine and save
        // credentials into local file
        // login into witness wss:// with valid credentials saved into local
        // file and load main app
        // in main view get and display username information by doing wss:// api
        // call
        
        // IMPORTANT: If you forget your pass phrase and username you will be
        // unable to access your account,
        // we cannot reset or restore your password! Make sure you memorize or
        // write down your password or save
        // the locally stored file with those <link to the file on local OS>
        
        // prevent typing of invalid character
        if (username.getText().toLowerCase().matches("[a-z]+(?:[a-z0-9\\-\\.])*[a-z0-9]")
                && username.getText().length() >= 3) {
            // check if user exists on the network from wss://
            try {
                nodeConnection.connect(null, null, false, mErrorListener);
                nodeConnection.addRequestHandler(
                        new LookupAccounts(username.getText().toLowerCase(), 50, true, new WitnessResponseListener() {
                            @Override
                            public void onSuccess(WitnessResponse response) {
                                List<UserAccount> existingUsers = (List<UserAccount>) response.result;
                                for (UserAccount u : existingUsers) {
                                    if (username.getText().toLowerCase().contentEquals(u.getName())) {
                                        // https://stackoverflow.com/questions/17850191/why-am-i-getting-java-lang-illegalstateexception-not-on-fx-application-thread/17851019#17851019
                                        Platform.runLater(() -> {
                                            updateErrorMessage("Existing user try another username", true, true, true);
                                        });
                                        return;
                                    }
                                }
                                
                                // if username is correct for registration we
                                // can call the faucet
                                String registrationCallResult = RegisterUsingFaucet
                                        .register(username.getText().toLowerCase());
                                if (registrationCallResult == null) {
                                    // on success username validation before
                                    // faucet call
                                    username.getStyleClass().remove("wrong-credentials");
                                    username.resetValidation();
                                    Platform.runLater(() -> {
                                        updateErrorMessage("success", false, false, false);
                                    });
                                    // fade out of registration/login ui view
                                    // and fade into password backup
                                    // notification view
                                    fadeOut();
                                    
                                } else {
                                    Platform.runLater(() -> {
                                        updateErrorMessage(registrationCallResult, true, true, true);
                                    });
                                }
                            }
                            
                            @Override
                            public void onError(BaseResponse.Error error) {
                                System.out.println("onError");
                            }
                        }));
            } catch (RepeatedRequestIdException e) {
                System.out.println("RepeatedRequestIdException. Msg: " + e.getMessage());
            }
        } else {
            Platform.runLater(() -> {
                updateErrorMessage(RegisterUsingFaucet.USERNAME_DOESNT_MATCH_VALIDATION_REGEX, true, true, true);
            });
        }
        
        // String pword = DigestUtils.shaHex(password.getText());
        // String password = "a";
        
        // if (uname.equals(preference.getUsername()) &&
        // pword.equals(preference.getPassword())) {
        // closeStage();
        // loadMain();
        // } else {
        // username.getStyleClass().add("wrong-credentials");
        // password.getStyleClass().add("wrong-credentials");
        // }
        
    }
    
    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        System.exit(0);
    }
    
    private void closeStage() {
        ((Stage) username.getScene().getWindow()).close();
    }
    
    void loadMain() {
        try {
            Parent parent = FXMLLoader.load(getClass().getResource("/library/assistant/ui/main/main.fxml"));
            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setTitle("Omega governance");
            Scene scene = new Scene(parent);
            LibraryAssistantUtil.setSceneStyle(scene, this);
            stage.setScene(scene);
            stage.show();
            LibraryAssistantUtil.setStageIcon(stage);
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void fadeOut() {
        FadeTransition ft = new FadeTransition();
        ft.setDuration(Duration.millis(200));
        ft.setNode(rootPane);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadNextScene();
            }
        });
        ft.play();
    }
    
    private void loadNextScene() {
        try {
            Parent secondView = (AnchorPane) FXMLLoader
                    .load(getClass().getResource("/network/omega/ui/login/passwordBackupNotifiy.fxml"));
            Scene newScene = new Scene(secondView);
            LibraryAssistantUtil.setSceneStyle(newScene, this);
            newScene.setFill(Main.BACKGROUND_FILL);
            Stage currentStage = (Stage) rootPane.getScene().getWindow();
            currentStage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace();
            Main.logger.error("IOException", e);
        }
    }
    
    private NodeErrorListener mErrorListener = new NodeErrorListener() {
        @Override
        public void onError(BaseResponse.Error error) {
            System.out.println("onError");
        }
    };
}
