package network.omega.ui.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jfoenix.controls.*;
import com.jfoenix.effects.JFXDepthManager;
import com.jfoenix.transitions.hamburger.HamburgerSlideCloseTransition;
import cy.agorise.graphenej.Asset;
import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.api.GetAccountByName;
import cy.agorise.graphenej.api.ListAssets;
import cy.agorise.graphenej.api.LookupAccounts;
import cy.agorise.graphenej.api.android.NodeConnection;
import cy.agorise.graphenej.errors.RepeatedRequestIdException;
import cy.agorise.graphenej.interfaces.NodeErrorListener;
import cy.agorise.graphenej.interfaces.WitnessResponseListener;
import cy.agorise.graphenej.models.AccountProperties;
import cy.agorise.graphenej.models.BaseResponse;
import cy.agorise.graphenej.models.WitnessResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import library.assistant.alert.AlertMaker;
import library.assistant.database.DataHelper;
import library.assistant.database.DatabaseHandler;
import library.assistant.ui.callback.BookReturnCallback;
import library.assistant.ui.issuedlist.IssuedListController;
import library.assistant.util.LibraryAssistantUtil;
import network.omega.ui.main.toolbar.ToolbarController;
import network.omega.ui.preferences.ManageLocalStorage;
import network.omega.ui.resource.ResourceController;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController implements Initializable, BookReturnCallback {
    
    private static final String BOOK_NOT_AVAILABLE = "Not Available";
    private static final String NO_SUCH_BOOK_AVAILABLE = "No Such Book Available";
    private static final String NO_SUCH_MEMBER_AVAILABLE = "No Such Member Available";
    private static final String BOOK_AVAILABLE = "Available";
    
    private Boolean isReadyForSubmission = false;
    private DatabaseHandler databaseHandler;
    private PieChart bookChart;
    private PieChart memberChart;
    
    @FXML
    private TextField bookIDInput;
    @FXML
    private Text bookName;
    @FXML
    private Text bookAuthor;
    @FXML
    private Text bookStatus;
    @FXML
    private TextField memberIDInput;
    @FXML
    private Text memberName;
    @FXML
    private Text memberMobile;
    @FXML
    private JFXTextField bookID;
    @FXML
    private StackPane rootPane;
    @FXML
    private JFXHamburger hamburger;
    @FXML
    private JFXDrawer drawer;
    @FXML
    private Text memberNameHolder;
    @FXML
    private Text memberEmailHolder;
    @FXML
    private Text memberContactHolder;
    @FXML
    private Text bookNameHolder;
    @FXML
    private Text bookAuthorHolder;
    @FXML
    private Text bookPublisherHolder;
    @FXML
    private Text issueDateHolder;
    @FXML
    private Text numberDaysHolder;
    @FXML
    private Text fineInfoHolder;
    @FXML
    private AnchorPane rootAnchorPane;
    @FXML
    private JFXButton renewButton;
    @FXML
    private JFXButton submissionButton;
    @FXML
    private HBox submissionDataContainer;
    @FXML
    private StackPane bookInfoContainer;
    @FXML
    private StackPane memberInfoContainer;
    @FXML
    private Tab bookIssueTab;
    @FXML
    private Tab renewTab;
    @FXML
    private JFXTabPane mainTabPane;
    
    @FXML
    private Label testJsonContainer;
    
    NodeConnection nodeConnection;
    
    private NodeErrorListener mErrorListener = new NodeErrorListener() {
        @Override
        public void onError(BaseResponse.Error error) {
            System.out.println("onError");
        }
    };
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // databaseHandler = DatabaseHandler.getInstance();
        
        initDrawer();
        // initGraphs();
        
        // get username and password from the file
        ManageLocalStorage.readPasswordFile();
        String username = ManageLocalStorage.username;
        String password = ManageLocalStorage.password;
        
        // do test call to wss://
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl("wss://testnet.sombrero.network/ws");
        nodeConnection.connect(username, password, false, mErrorListener);
        try {
            nodeConnection.addRequestHandler(
                    new GetAccountByName(username.toLowerCase(), true, new WitnessResponseListener() {
                        @Override
                        public void onSuccess(WitnessResponse response) {
                            AccountProperties user = (AccountProperties) response.result;
                            
                            Platform.runLater(() -> {
                                testJsonContainer
                                        .setText("You just made a call to witness server with your user credentials\n"
                                                + "using websocket secure protocol. The returned user id by the server was : "
                                                + user.id);
                            });
                        }
                        
                        @Override
                        public void onError(BaseResponse.Error error) {
                            AlertMaker.showErrorMessage("Wrong username and password",
                                    "Please correct yout password.dat file.");
                        }
                    }));
        } catch (RepeatedRequestIdException e) {
            e.printStackTrace();
        }
        
    }
    
    @FXML
    private void loadBookInfo(ActionEvent event) {
        clearBookCache();
        enableDisableGraph(false);
        
        String id = bookIDInput.getText();
        ResultSet rs = DataHelper.getBookInfoWithIssueData(id);
        Boolean flag = false;
        try {
            if (rs.next()) {
                String bName = rs.getString("title");
                String bAuthor = rs.getString("author");
                Boolean bStatus = rs.getBoolean("isAvail");
                Timestamp issuedOn = rs.getTimestamp("issueTime");
                
                bookName.setText(bName);
                bookAuthor.setText(bAuthor);
                String status = (bStatus) ? BOOK_AVAILABLE
                        : String.format("Issued on %s",
                                LibraryAssistantUtil.getDateString(new Date(issuedOn.getTime())));
                if (!bStatus) {
                    bookStatus.getStyleClass().add("not-available");
                } else {
                    bookStatus.getStyleClass().remove("not-available");
                }
                bookStatus.setText(status);
                
                flag = true;
            }
            
            if (!flag) {
                bookName.setText(NO_SUCH_BOOK_AVAILABLE);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    void clearBookCache() {
        bookName.setText("");
        bookAuthor.setText("");
        bookStatus.setText("");
    }
    
    void clearMemberCache() {
        memberName.setText("");
        memberMobile.setText("");
    }
    
    @FXML
    private void loadMemberInfo(ActionEvent event) {
        clearMemberCache();
        enableDisableGraph(false);
        
        String id = memberIDInput.getText();
        String qu = "SELECT * FROM MEMBER WHERE id = '" + id + "'";
        ResultSet rs = databaseHandler.execQuery(qu);
        Boolean flag = false;
        try {
            while (rs.next()) {
                String mName = rs.getString("name");
                String mMobile = rs.getString("mobile");
                
                memberName.setText(mName);
                memberMobile.setText(mMobile);
                
                flag = true;
            }
            
            if (!flag) {
                memberName.setText(NO_SUCH_MEMBER_AVAILABLE);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    private void loadIssueOperation(ActionEvent event) {
        if (checkForIssueValidity()) {
            JFXButton btn = new JFXButton("Okay!");
            AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn), "Invalid Input", null);
            return;
        }
        if (bookStatus.getText().equals(BOOK_NOT_AVAILABLE)) {
            JFXButton btn = new JFXButton("Okay!");
            JFXButton viewDetails = new JFXButton("View Details");
            viewDetails.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
                String bookToBeLoaded = bookIDInput.getText();
                bookID.setText(bookToBeLoaded);
                bookID.fireEvent(new ActionEvent());
                mainTabPane.getSelectionModel().select(renewTab);
            });
            AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn, viewDetails),
                    "Already issued book", "This book is already issued. Cant process issue request");
            return;
        }
        
        String memberID = memberIDInput.getText();
        String bookID = bookIDInput.getText();
        
        JFXButton yesButton = new JFXButton("YES");
        yesButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event1) -> {
            String str = "INSERT INTO ISSUE(memberID,bookID) VALUES (" + "'" + memberID + "'," + "'" + bookID + "')";
            String str2 = "UPDATE BOOK SET isAvail = false WHERE id = '" + bookID + "'";
            System.out.println(str + " and " + str2);
            
            if (databaseHandler.execAction(str) && databaseHandler.execAction(str2)) {
                JFXButton button = new JFXButton("Done!");
                AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(button), "Book Issue Complete",
                        null);
                refreshGraphs();
            } else {
                JFXButton button = new JFXButton("Okay.I'll Check");
                AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(button), "Issue Operation Failed",
                        null);
            }
            clearIssueEntries();
        });
        JFXButton noButton = new JFXButton("NO");
        noButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event1) -> {
            JFXButton button = new JFXButton("That's Okay");
            AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(button), "Issue Cancelled", null);
            clearIssueEntries();
        });
        AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(yesButton, noButton), "Confirm Issue",
                "Are you sure want to issue the book " + bookName.getText() + " to " + memberName.getText() + " ?");
    }
    
    @FXML
    private void loadBookInfo2(ActionEvent event) {
        clearEntries();
        ObservableList<String> issueData = FXCollections.observableArrayList();
        isReadyForSubmission = false;
        
        try {
            String id = bookID.getText();
            String myQuery = "SELECT ISSUE.bookID, ISSUE.memberID, ISSUE.issueTime, ISSUE.renew_count,\n"
                    + "MEMBER.name, MEMBER.mobile, MEMBER.email,\n" + "BOOK.title, BOOK.author, BOOK.publisher\n"
                    + "FROM ISSUE\n" + "LEFT JOIN MEMBER\n" + "ON ISSUE.memberID=MEMBER.ID\n" + "LEFT JOIN BOOK\n"
                    + "ON ISSUE.bookID=BOOK.ID\n" + "WHERE ISSUE.bookID='" + id + "'";
            ResultSet rs = databaseHandler.execQuery(myQuery);
            if (rs.next()) {
                memberNameHolder.setText(rs.getString("name"));
                memberContactHolder.setText(rs.getString("mobile"));
                memberEmailHolder.setText(rs.getString("email"));
                
                bookNameHolder.setText(rs.getString("title"));
                bookAuthorHolder.setText(rs.getString("author"));
                bookPublisherHolder.setText(rs.getString("publisher"));
                
                Timestamp mIssueTime = rs.getTimestamp("issueTime");
                Date dateOfIssue = new Date(mIssueTime.getTime());
                issueDateHolder.setText(LibraryAssistantUtil.formatDateTimeString(dateOfIssue));
                Long timeElapsed = System.currentTimeMillis() - mIssueTime.getTime();
                Long days = TimeUnit.DAYS.convert(timeElapsed, TimeUnit.MILLISECONDS) + 1;
                String daysElapsed = String.format("Used %d days", days);
                numberDaysHolder.setText(daysElapsed);
                Float fine = LibraryAssistantUtil.getFineAmount(days.intValue());
                if (fine > 0) {
                    fineInfoHolder
                            .setText(String.format("Fine : %.2f", LibraryAssistantUtil.getFineAmount(days.intValue())));
                } else {
                    fineInfoHolder.setText("");
                }
                
                isReadyForSubmission = true;
                disableEnableControls(true);
                submissionDataContainer.setOpacity(1);
            } else {
                JFXButton button = new JFXButton("Okay.I'll Check");
                AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(button),
                        "No such Book Exists in Issue Database", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    @FXML
    private void loadSubmissionOp(ActionEvent event) {
        if (!isReadyForSubmission) {
            JFXButton btn = new JFXButton("Okay!");
            AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn),
                    "Please select a book to submit", "Cant simply submit a null book :-)");
            return;
        }
        
        JFXButton yesButton = new JFXButton("YES, Please");
        yesButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent ev) -> {
            String id = bookID.getText();
            String ac1 = "DELETE FROM ISSUE WHERE BOOKID = '" + id + "'";
            String ac2 = "UPDATE BOOK SET ISAVAIL = TRUE WHERE ID = '" + id + "'";
            
            if (databaseHandler.execAction(ac1) && databaseHandler.execAction(ac2)) {
                JFXButton btn = new JFXButton("Done!");
                AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn), "Book has been submitted",
                        null);
                disableEnableControls(false);
                submissionDataContainer.setOpacity(0);
            } else {
                JFXButton btn = new JFXButton("Okay.I'll Check");
                AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn),
                        "Submission Has Been Failed", null);
            }
        });
        JFXButton noButton = new JFXButton("No, Cancel");
        noButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent ev) -> {
            JFXButton btn = new JFXButton("Okay!");
            AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn),
                    "Submission Operation cancelled", null);
        });
        
        AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(yesButton, noButton),
                "Confirm Submission Operation", "Are you sure want to return the book ?");
    }
    
    @FXML
    private void loadRenewOp(ActionEvent event) {
        if (!isReadyForSubmission) {
            JFXButton btn = new JFXButton("Okay!");
            AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn), "Please select a book to renew",
                    null);
            return;
        }
        JFXButton yesButton = new JFXButton("YES, Please");
        yesButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event1) -> {
            String ac = "UPDATE ISSUE SET issueTime = CURRENT_TIMESTAMP, renew_count = renew_count+1 WHERE BOOKID = '"
                    + bookID.getText() + "'";
            System.out.println(ac);
            if (databaseHandler.execAction(ac)) {
                JFXButton btn = new JFXButton("Alright!");
                AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn), "Book Has Been Renewed",
                        null);
                disableEnableControls(false);
                submissionDataContainer.setOpacity(0);
            } else {
                JFXButton btn = new JFXButton("Okay!");
                AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn), "Renew Has Been Failed",
                        null);
            }
        });
        JFXButton noButton = new JFXButton("No, Don't!");
        noButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event1) -> {
            JFXButton btn = new JFXButton("Okay!");
            AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn), "Renew Operation cancelled",
                    null);
        });
        AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(yesButton, noButton),
                "Confirm Renew Operation", "Are you sure want to renew the book ?");
    }
    
    private Stage getStage() {
        return (Stage) rootPane.getScene().getWindow();
    }
    
    @FXML
    private void handleMenuClose(ActionEvent event) {
        getStage().close();
    }
    
    @FXML
    private void handleMenuAddBook(ActionEvent event) {
        LibraryAssistantUtil.loadWindow(getClass().getResource("/library/assistant/ui/addbook/add_book.fxml"),
                "Add New Book", null);
    }
    
    @FXML
    private void handleMenuAddMember(ActionEvent event) {
        LibraryAssistantUtil.loadWindow(getClass().getResource("/library/assistant/ui/addmember/member_add.fxml"),
                "Add New Member", null);
    }
    
    @FXML
    private void handleMenuViewBook(ActionEvent event) {
        LibraryAssistantUtil.loadWindow(getClass().getResource("/library/assistant/ui/listbook/book_list.fxml"),
                "Book List", null);
    }
    
    @FXML
    private void handleAboutMenu(ActionEvent event) {
        LibraryAssistantUtil.loadWindow(getClass().getResource("/network/omega/ui/about/about.fxml"),
                "About Omega Governance", null);
    }
    
    @FXML
    private void handleMenuSettings(ActionEvent event) {
        LibraryAssistantUtil.loadWindow(getClass().getResource("/network/omega/ui/settings/settings.fxml"), "Settings",
                null);
    }
    
    @FXML
    private void handleMenuViewMemberList(ActionEvent event) {
        LibraryAssistantUtil.loadWindow(getClass().getResource("/library/assistant/ui/listmember/member_list.fxml"),
                "Member List", null);
    }
    
    @FXML
    private void handleIssuedList(ActionEvent event) {
        Object controller = LibraryAssistantUtil.loadWindow(
                getClass().getResource("/library/assistant/ui/issuedlist/issued_list.fxml"), "Issued Book List", null);
        if (controller != null) {
            IssuedListController cont = (IssuedListController) controller;
            cont.setBookReturnCallback(this);
        }
    }
    
    @FXML
    private void handleMenuFullScreen(ActionEvent event) {
        Stage stage = getStage();
        stage.setFullScreen(!stage.isFullScreen());
    }
    
    private void initDrawer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/network/omega/ui/main/toolbar/toolbar.fxml"));
            VBox toolbar = loader.load();
            drawer.setSidePane(toolbar);
            ToolbarController controller = loader.getController();
            controller.setParentController(this);
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        HamburgerSlideCloseTransition task = new HamburgerSlideCloseTransition(hamburger);
        task.setRate(-1);
        hamburger.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event event) -> {
            drawer.toggle();
        });
        drawer.setOnDrawerOpening((event) -> {
            task.setRate(task.getRate() * -1);
            task.play();
            drawer.toFront();
        });
        drawer.setOnDrawerClosed((event) -> {
            drawer.toBack();
            task.setRate(task.getRate() * -1);
            task.play();
        });
    }
    
    private void clearEntries() {
        memberNameHolder.setText("");
        memberEmailHolder.setText("");
        memberContactHolder.setText("");
        
        bookNameHolder.setText("");
        bookAuthorHolder.setText("");
        bookPublisherHolder.setText("");
        
        issueDateHolder.setText("");
        numberDaysHolder.setText("");
        fineInfoHolder.setText("");
        
        disableEnableControls(false);
        submissionDataContainer.setOpacity(0);
    }
    
    private void disableEnableControls(Boolean enableFlag) {
        if (enableFlag) {
            renewButton.setDisable(false);
            submissionButton.setDisable(false);
        } else {
            renewButton.setDisable(true);
            submissionButton.setDisable(true);
        }
    }
    
    private void clearIssueEntries() {
        bookIDInput.clear();
        memberIDInput.clear();
        bookName.setText("");
        bookAuthor.setText("");
        bookStatus.setText("");
        memberMobile.setText("");
        memberName.setText("");
        enableDisableGraph(true);
    }
    
    private void initGraphs() {
        bookChart = new PieChart(databaseHandler.getBookGraphStatistics());
        memberChart = new PieChart(databaseHandler.getMemberGraphStatistics());
        bookInfoContainer.getChildren().add(bookChart);
        memberInfoContainer.getChildren().add(memberChart);
        
        bookIssueTab.setOnSelectionChanged((Event event) -> {
            clearIssueEntries();
            if (bookIssueTab.isSelected()) {
                refreshGraphs();
            }
        });
    }
    
    private void refreshGraphs() {
        bookChart.setData(databaseHandler.getBookGraphStatistics());
        memberChart.setData(databaseHandler.getMemberGraphStatistics());
    }
    
    private void enableDisableGraph(Boolean status) {
        if (status) {
            bookChart.setOpacity(1);
            memberChart.setOpacity(1);
        } else {
            bookChart.setOpacity(0);
            memberChart.setOpacity(0);
        }
    }
    
    private boolean checkForIssueValidity() {
        bookIDInput.fireEvent(new ActionEvent());
        memberIDInput.fireEvent(new ActionEvent());
        return bookIDInput.getText().isEmpty() || memberIDInput.getText().isEmpty() || memberName.getText().isEmpty()
                || bookName.getText().isEmpty() || bookName.getText().equals(NO_SUCH_BOOK_AVAILABLE)
                || memberName.getText().equals(NO_SUCH_MEMBER_AVAILABLE);
    }
    
    @Override
    public void loadBookReturn(String bookID) {
        this.bookID.setText(bookID);
        mainTabPane.getSelectionModel().select(renewTab);
        loadBookInfo2(null);
        getStage().toFront();
        if (drawer.isShown()) {
            drawer.close();
        }
    }
    
    public HashMap<String, Asset> resourceTypesFetched;
    ResourceController rc = null;
    public int openedAddResourceForms = 0;
    
    @FXML
    public void handleAddMyResource(ActionEvent actionEvent) {
        if (openedAddResourceForms == 0) {
            rc = (ResourceController) LibraryAssistantUtil.loadWindowClosable(
                    getClass().getResource("/network/omega/ui/resource/resource.fxml"), "Add Resource", null);
            rc.mc = this;
            // cache all resources types in RAM
            if (!wssIsLocked()) {
                lockWss();
                resourceTypesFetched = new HashMap<>();
                nodeConnection = NodeConnection.getInstance();
                nodeConnection.addNodeUrl("wss://testnet.sombrero.network/ws");
                fetchResourceTypes("ubuntu", 100, resourceTypesFetched);
                rc.resourceTypesFetched = resourceTypesFetched;
            } else {
                rc.resourceTypesFetched = resourceTypesFetched;
                rc.updateTypesList();
            }
            openedAddResourceForms++;
        }
        rc.toFront();
    }
    
    public long MILLIS_TO_PASS_BETWEEN_WSS_CONNECTIONS = 3L * 60L * 1000L; // 3
                                                                           // *
                                                                           // 60
                                                                           // *
                                                                           // 1000
                                                                           // =
                                                                           // 3
                                                                           // min
    public long wssLocked = 0L;
    
    public void lockWss() {
        wssLocked = System.currentTimeMillis() + MILLIS_TO_PASS_BETWEEN_WSS_CONNECTIONS;
    }
    
    public boolean wssIsLocked() {
        if (System.currentTimeMillis() < wssLocked) {
            return true;
        }
        return false;
    }
    
    public void fetchResourceTypes(String searchKeyWord, int limit, HashMap<String, Asset> resourceTypesFetched) {
        System.out.println(searchKeyWord);
        nodeConnection.connect(null, null, false, mErrorListener);
        Asset LOWER_BOUND_ASSET = new Asset("1.3.0");
        try {
            nodeConnection.addRequestHandler(
                    new ListAssets(LOWER_BOUND_ASSET.getSymbol(), limit, true, new WitnessResponseListener() {
                        @Override
                        public void onSuccess(WitnessResponse response) {
                            System.out.println("onSuccess");
                            List<Asset> assets = (List<Asset>) response.result;
                            
                            if (searchKeyWord != null) {
                                for (Asset a : assets) {
                                    // System.out.println(a.getSymbol());
                                    if (a.getAssetOptions().getDescription().contains(searchKeyWord)) {
                                        resourceTypesFetched.put(a.getSymbol(), a);
                                    }
                                }
                            } else {
                                for (Asset a : assets) {
                                    resourceTypesFetched.put(a.getSymbol(), a);
                                }
                            }
                            
                            // update list initially to show all types
                            if (rc != null) {
                                rc.updateTypesList();
                            }
                            
                        }
                        
                        @Override
                        public void onError(BaseResponse.Error error) {
                            System.out.println("onError");
                        }
                    }));
        } catch (RepeatedRequestIdException e) {
            e.printStackTrace();
        }
    }
}
