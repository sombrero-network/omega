package network.omega.ui.resource;

import com.jfoenix.controls.JFXTextField;
import cy.agorise.graphenej.Asset;
import cy.agorise.graphenej.api.ListAssets;
import cy.agorise.graphenej.api.android.NodeConnection;
import cy.agorise.graphenej.errors.RepeatedRequestIdException;
import cy.agorise.graphenej.interfaces.NodeErrorListener;
import cy.agorise.graphenej.interfaces.WitnessResponseListener;
import cy.agorise.graphenej.models.BaseResponse;
import cy.agorise.graphenej.models.WitnessResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import network.omega.ui.main.MainController;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ResourceController implements Initializable, ControllerHooks {

    @FXML
    private StackPane rootPane;

    @FXML
    private JFXTextField searchResourceType;

    @FXML
    private ListView searchResultResources;

    @FXML
    private TextArea resourceDescription;

    public MainController mc;

    protected static NodeConnection nodeConnection;
    public HashMap<String, Asset> resourceTypesFetched;
    protected List<Asset> filteredResourceTypesBySearchedText = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }



    public void addResource(ActionEvent actionEvent) {
        //get selected resource

        //update my resources table

    }

    @Override
    public void close(){
        Stage stage = (Stage) rootPane.getScene().getWindow();
        mc.openedAddResourceForms--;
        stage.close();
    }

    @Override
    public void toFront() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        //stage.show();
        stage.toFront();
    }

    public void cancel(ActionEvent actionEvent) {
        close();
    }

    public void updateTypesList(){
        String currentSearchText = searchResourceType.getText();
        Platform.runLater(() -> {
            //create filtered set of types from fetched collection
            filteredResourceTypesBySearchedText.clear();
            for(Asset currentType : resourceTypesFetched.values()){
                if(currentType.getAssetOptions().getDescription().toLowerCase().contains(currentSearchText.toLowerCase())
                        ||
                        currentType.getSymbol().toLowerCase().contains(currentSearchText.toLowerCase())){
                    filteredResourceTypesBySearchedText.add(currentType);
                }
            }

            //update fx component list with filtered collection

            List<String> justResourceTypesNames = new ArrayList<>();
            for (Asset a : filteredResourceTypesBySearchedText) {
                ResourceDescription rd = new ResourceDescription();
                rd.parse(a);
                justResourceTypesNames.add(rd.name);
            }
            searchResultResources.setItems(FXCollections.observableList(justResourceTypesNames));
            searchResultResources.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    String selectedAsset = (String)searchResultResources.getSelectionModel().getSelectedItem();
                    for (Asset a : filteredResourceTypesBySearchedText) {
                        ResourceDescription rd = new ResourceDescription();
                        rd.parse(a);
                        if(selectedAsset.contains(rd.name)){
                            resourceDescription.setText(rd.toString());
                        }
                    }
                }
            });
        });
    }

    private long MILLIS_TO_PASS_BETWEEN_KEYS_STROKES = 500L;
    private long inputLocked = 0L;
    private ScheduledThreadPoolExecutor ex = new ScheduledThreadPoolExecutor(1);
    public void onSearchResourceInputChange(KeyEvent keyEvent) {
        //String currentSearchText = ((JFXTextField) keyEvent.getSource()).getText() + keyEvent.getCharacter();
        //don't run if specific time is not passed since last key stroke
        if(System.currentTimeMillis() >= inputLocked) {
            inputLocked = System.currentTimeMillis() + MILLIS_TO_PASS_BETWEEN_KEYS_STROKES;
            //System.out.println(currentSearchText);
            //fetchResourceTypes(currentSearchText, 100);

            //start thread that will run once just after input unlocked
            ex.schedule(() -> updateTypesList(),
                    MILLIS_TO_PASS_BETWEEN_KEYS_STROKES, TimeUnit.MILLISECONDS);
        }
    }

    private NodeErrorListener mErrorListener = new NodeErrorListener() {
        @Override
        public void onError(BaseResponse.Error error) {
            System.out.println("onError");
        }
    };

}
