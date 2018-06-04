package network.omega.ui.resource;

import com.jfoenix.controls.JFXTextField;
import cy.agorise.graphenej.Asset;
import cy.agorise.graphenej.api.android.NodeConnection;
import cy.agorise.graphenej.interfaces.NodeErrorListener;
import cy.agorise.graphenej.models.BaseResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import network.omega.ui.main.Main;
import network.omega.ui.main.MainController;
import network.omega.ui.preferences.ManageLocalStorage;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
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
    
    @FXML
    private Label selectDiskLabel;
    
    @FXML
    private ListView disksList;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Label installedVBoxVersion;
    
    private Tooltip errTooltip = new Tooltip();
    
    private Stage stage = null;
    
    public MainController mc;
    
    SystemInfo si = new SystemInfo();
    HardwareAbstractionLayer hal = si.getHardware();
    OperatingSystem os = si.getOperatingSystem();
    
    String vBoxInstalledVersion = null;
    
    protected static NodeConnection nodeConnection;
    public HashMap<String, Asset> resourceTypesFetched;
    protected List<Asset> filteredResourceTypesBySearchedText = new ArrayList<>();
    
    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        vBoxInstalledVersion = VBoxManager.getVirtualBoxInstalledVersion(hal);
        if (vBoxInstalledVersion != null) {
            installedVBoxVersion.setVisible(true);
            installedVBoxVersion.setText("VirtualBox v" + vBoxInstalledVersion);
        } else {
            installedVBoxVersion.setVisible(false);
        }
        selectDiskLabel.setText("Select the disk\nto store virtual\nmachine at");
        // update the available disks list to select only one
        updateDisksList(disksList);
        
        // configure error tooltip
        errTooltip.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                errTooltip.hide();
            }
        });
        Image image = new Image(getClass().getResourceAsStream("/if_stock_error-next_94134.png"));
        errTooltip.setGraphic(new ImageView(image));
    }
    
    public Path getRootPath(FileStore fs) throws IOException {
        Path media = Paths.get("/media");
        if (media.isAbsolute() && Files.exists(media)) { // Linux
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(media)) {
                for (Path p : stream) {
                    if (Files.getFileStore(p).equals(fs)) {
                        return p;
                    }
                }
            }
        } else { // Windows
            IOException ex = null;
            for (Path p : FileSystems.getDefault().getRootDirectories()) {
                try {
                    if (Files.getFileStore(p).equals(fs)) {
                        return p;
                    }
                } catch (IOException e) {
                    ex = e;
                }
            }
            if (ex != null) {
                throw ex;
            }
        }
        return null;
    }
    
    private List<FileStore> currentDisksList = new ArrayList<>();
    private List<OSFileStore> currentDisksListMacLin = new ArrayList<>();
    
    private void updateDisksList(ListView disksList) {
        // returns pathnames for files and directory
        // File[] paths;
        // FileSystemView fsv = FileSystemView.getFileSystemView();
        // paths = File.listRoots();
        
        // for each pathname in pathname array
        currentDisksList.clear();
        List<String> disks = new ArrayList<>();
        
        if (com.sun.jna.Platform.isWindows()) {
            for (Path root : FileSystems.getDefault().getRootDirectories()) {
                // prints file and directory paths
                try {
                    FileStore currentStore = Files.getFileStore(root);
                    long currentStoreFreeSpaceInGB = currentStore.getUsableSpace() / 1024l / 1024l / 1024l;
                    if (currentStoreFreeSpaceInGB > 10) {
                        String diskName = "";
                        if (currentStore.name().contentEquals("")) {
                            diskName = getRootPath(currentStore).toFile().getAbsolutePath();
                        } else {
                            diskName = currentStore.name() + " - "
                                    + getRootPath(currentStore).toFile().getAbsolutePath();
                        }
                        disks.add(diskName + " - " + currentStoreFreeSpaceInGB + " GB Free ");
                        currentDisksList.add(currentStore);
                        // System.out.println("Drive Name: " + path);
                        // System.out.println("Description: " +
                        // fsv.getSystemTypeDescription(path));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Main.logger.error("IOException", e);
                }
            }
        } else {
            // https://github.com/oshi/oshi/blob/master/oshi-core/src/test/java/oshi/SystemInfoTest.java
            OSFileStore[] fsArray = os.getFileSystem().getFileStores();
            for (OSFileStore fs : fsArray) {
                long usable = fs.getUsableSpace();//
                long total = fs.getTotalSpace();
                String mountPoint = fs.getMount();
                String name = fs.getName();
                long currentStoreFreeSpaceInGB = fs.getUsableSpace() / 1024l / 1024l / 1024l;
                if (currentStoreFreeSpaceInGB > 10) {
                    String diskName = "";
                    if (fs.getName().contentEquals("")) {
                        diskName = fs.getMount();
                    } else {
                        diskName = fs.getName() + " - " + fs.getMount();
                    }
                    disks.add(diskName + " - " + currentStoreFreeSpaceInGB + " GB Free ");
                    currentDisksListMacLin.add(fs);
                    // System.out.println("Drive Name: " + path);
                    // System.out.println("Description: " +
                    // fsv.getSystemTypeDescription(path));
                }
                
                // System.out.format(
                // " %s (%s) [%s] %s of %s free (%.1f%%) is %s "
                // + (fs.getLogicalVolume() != null &&
                // fs.getLogicalVolume().length() > 0 ? "[%s]" : "%s")
                // + " and is mounted at %s%n",
                // fs.getName(), fs.getDescription().isEmpty() ? "file system" :
                // fs.getDescription(), fs.getType(),
                // FormatUtil.formatBytes(usable),
                // FormatUtil.formatBytes(fs.getTotalSpace()), 100d * usable /
                // total,
                // fs.getVolume(), fs.getLogicalVolume(), fs.getMount());
            }
        }
        disksList.setItems(FXCollections.observableList(disks));
    }
    
    public ResourceDescription getResourceDescriptionForCurrentlySelectedType() {
        String selectedAsset = (String) searchResultResources.getSelectionModel().getSelectedItem();
        if (selectedAsset != null) {
            for (Asset a : filteredResourceTypesBySearchedText) {
                ResourceDescription rd = new ResourceDescription();
                rd.parse(a);
                if (selectedAsset.contains(rd.name)) {
                    return rd;
                }
            }
        }
        return null;
    }
    
    public Object getCurrentlySelectedDisk() {
        Object selectedDisk = null;
        int selectedIndex = disksList.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            if (com.sun.jna.Platform.isWindows()) {
                selectedDisk = currentDisksList.get(selectedIndex);
            } else {
                selectedDisk = currentDisksListMacLin.get(selectedIndex);
            }
            if (selectedDisk != null) {
                return selectedDisk;
            } else {
                return null;
            }
        }
        return null;
    }
    
    public String getVMStorageDirAbsolutePath(Object selectedDisk) {
        String result = null;
        if (selectedDisk instanceof FileStore) {
            try {
                result = getRootPath((FileStore) selectedDisk).toFile().getAbsolutePath();
                if (result.contentEquals("C:\\")) {
                    // override to user home path
                    result = ManageLocalStorage.applicationDir.toFile().getAbsolutePath();
                } else {
                    // create new folder at path
                    result = result + ".omegagovernance";
                }
            } catch (IOException e) {
                e.printStackTrace();
                Main.logger.error("IOException", e);
            }
        } else {
            result = ((OSFileStore) selectedDisk).getMount();
            if (result.length() < 4) {
                if (ManageLocalStorage.applicationDir.toFile().getAbsolutePath().startsWith(result)) {
                    result = ManageLocalStorage.applicationDir.toFile().getAbsolutePath();
                }
            } else {
                if (ManageLocalStorage.applicationDir.toFile().getAbsolutePath()
                        .startsWith(result.substring(0, result.length() - 1))) {
                    result = ManageLocalStorage.applicationDir.toFile().getAbsolutePath();
                }
            }
            result = result + ".omegagovernance";
        }
        return result;
    }
    
    public String diskSizeIsValid(ResourceDescription aggregateResourceRequirement, Object currentlySelectedDisk) {
        // apart from disk space reuired VDI images we also require to support
        // RAM hibernation
        float diskSpaceRequiredOnHost = aggregateResourceRequirement.diskSize * 1000f
                + aggregateResourceRequirement.ramSize;
        
        if (currentlySelectedDisk instanceof FileStore) {
            // windows OS disk
            try {
                if (diskSpaceRequiredOnHost <= (((FileStore) currentlySelectedDisk).getUsableSpace() / 1024.0f
                        / 1024.0f)) {
                    System.out.println("Disk: " + diskSpaceRequiredOnHost + " <= "
                            + (((FileStore) currentlySelectedDisk).getUsableSpace() / 1024.0f / 1024.0f));
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Main.logger.error("IOException", e);
            }
        } else {
            // mac or linux OS disk
            if (diskSpaceRequiredOnHost <= (((OSFileStore) currentlySelectedDisk).getUsableSpace() / 1024.0f
                    / 1024.0f)) {
                System.out.println("Disk: " + diskSpaceRequiredOnHost + " <= "
                        + (((OSFileStore) currentlySelectedDisk).getUsableSpace() / 1024.0f / 1024.0f));
                return null;
            }
        }
        return diskSpaceRequiredOnHost + " MB of free disk needed\n";
    }
    
    public String ramSizeIsValid(ResourceDescription aggregateResourceRequirement, HardwareAbstractionLayer hal) {
        // 512 MB is a minimum required to run VirtualBox itself
        float ramRequiredOnHost = aggregateResourceRequirement.ramSize + 512f;
        
        float availableMemoryGB = hal.getMemory().getAvailable() / 1024.0f / 1024.0f / 1024f;
        if (ramRequiredOnHost <= availableMemoryGB * 1000f) {
            System.out.println("RAM: " + ramRequiredOnHost + " <= " + availableMemoryGB * 1000f);
            return null;
        }
        return ramRequiredOnHost + " MB of free RAM needed\n";
    }
    
    public String cpuCoresNumIsValid(ResourceDescription aggregateResourceRequirement, HardwareAbstractionLayer hal) {
        long logicalCores = hal.getProcessor().getLogicalProcessorCount();
        // 1 core is a minimum required to run VirtualBox itself
        float coresRequiredOnHost = aggregateResourceRequirement.numberOfCores;
        
        if (coresRequiredOnHost <= logicalCores) {
            System.out.println("CPU: " + coresRequiredOnHost + " <= " + logicalCores);
            return null;
        }
        return "Minimum of " + coresRequiredOnHost + " CPU cores needed\n";
    }
    
    public String isValid(ResourceDescription rdAggregatedRequirement) {
        StringBuilder result = new StringBuilder();
        Boolean isValid = false;
        // get selected resource type and json of that type
        // searchResultResources
        // ResourceDescription rd =
        // getResourceDescriptionForCurrentlySelectedType();
        
        // validate selection made to resource type json data - disk size, ram
        // and core number.
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        
        // do validation
        
        if (vBoxInstalledVersion == null) {
            result.append("Please install VirtualBox v" + VBoxManager.MIN_VBOX_VERSION + " or later\n");
        }
        
        if (rdAggregatedRequirement == null) {
            result.append("Select the resource type\n");
        }
        Object currentlySelectedDisk = getCurrentlySelectedDisk();
        
        if (currentlySelectedDisk == null) {
            result.append("Select the disk\n");
        }
        if (currentlySelectedDisk != null && rdAggregatedRequirement != null && vBoxInstalledVersion != null) {
            if (validateJustResourceType(result, rdAggregatedRequirement, hal, currentlySelectedDisk) == null) {
                // validation passed
                return null;
            }
        }
        return result.toString();
    }
    
    public StringBuilder validateJustResourceType(StringBuilder result,
            ResourceDescription aggregatedResourceToValidate, HardwareAbstractionLayer hal,
            Object currentlySelectedDisk) {
        // disk check
        String diskSizeIsValid = diskSizeIsValid(aggregatedResourceToValidate, currentlySelectedDisk);
        if (diskSizeIsValid != null) {
            result.append(diskSizeIsValid);
        }
        // ram check
        String ramSizeIsValid = ramSizeIsValid(aggregatedResourceToValidate, hal);
        if (ramSizeIsValid != null) {
            result.append(ramSizeIsValid);
        }
        // cpu check
        String cpuCoresNumIsValid = cpuCoresNumIsValid(aggregatedResourceToValidate, hal);
        if (cpuCoresNumIsValid != null) {
            result.append(cpuCoresNumIsValid);
        }
        if (diskSizeIsValid == null && ramSizeIsValid == null && cpuCoresNumIsValid == null) {
            return null;
        }
        return result;
    }
    
    public void addResource(ActionEvent actionEvent) {
        
        // get all already added resource to the "provided resources" table
        ResourceDescription rdAggregatedRequirement = getResourceDescriptionForCurrentlySelectedType();
        for (ResourceItem cr : mc.providerResourcesList) {
            rdAggregatedRequirement = rdAggregatedRequirement.add(cr.rd);
        }
        
        String isValidMsg = isValid(rdAggregatedRequirement);
        
        if (isValidMsg == null) {
            // update resources table
            System.out.println("Valid.");
            ResourceDescription rd = getResourceDescriptionForCurrentlySelectedType();
            String selectedDisk = getVMStorageDirAbsolutePath(getCurrentlySelectedDisk());
            mc.providerResourcesList.add(new ResourceItem(rd.name, rd.descriptionOS,
                    new Integer(rd.numberOfCores).toString(), new Double(rd.ramSize / 1000.0d).toString() + "GB",
                    new Double(rd.diskSize).toString() + "GB", selectedDisk, rd));
        } else {
            // show invalid message details
            if (stage != null) {
                System.out.println(isValidMsg);
                // create a tooltip
                errTooltip.setText(isValidMsg);
                saveButton.setTooltip(errTooltip);
                errTooltip.setAutoHide(true);
                Point2D p = saveButton.localToScene(0.0, 0.0);
                errTooltip.show(stage,
                        p.getX() + saveButton.getScene().getX() + saveButton.getScene().getWindow().getX(),
                        p.getY() + saveButton.getScene().getY() + saveButton.getScene().getWindow().getY());
            }
        }
    }
    
    @Override
    public void close() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        mc.openedAddResourceForms--;
        stage.close();
    }
    
    @Override
    public void toFront() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        // stage.show();
        stage.toFront();
    }
    
    public void cancel(ActionEvent actionEvent) {
        close();
    }
    
    public void updateTypesList() {
        String currentSearchText = searchResourceType.getText();
        Platform.runLater(() -> {
            // create filtered set of types from fetched collection
            filteredResourceTypesBySearchedText.clear();
            for (Asset currentType : resourceTypesFetched.values()) {
                if (currentType.getAssetOptions().getDescription().toLowerCase()
                        .contains(currentSearchText.toLowerCase())
                        || currentType.getSymbol().toLowerCase().contains(currentSearchText.toLowerCase())) {
                    filteredResourceTypesBySearchedText.add(currentType);
                }
            }
            
            // update fx component list with filtered collection
            
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
                    ResourceDescription rd = getResourceDescriptionForCurrentlySelectedType();
                    if (rd != null) {
                        resourceDescription.setText(rd.toString());
                    }
                }
            });
        });
    }
    
    private long MILLIS_TO_PASS_BETWEEN_KEYS_STROKES = 500L;
    private long inputLocked = 0L;
    private ScheduledThreadPoolExecutor ex = new ScheduledThreadPoolExecutor(1);
    
    public void onSearchResourceInputChange(KeyEvent keyEvent) {
        // String currentSearchText = ((JFXTextField)
        // keyEvent.getSource()).getText() + keyEvent.getCharacter();
        // don't run if specific time is not passed since last key stroke
        if (System.currentTimeMillis() >= inputLocked) {
            inputLocked = System.currentTimeMillis() + MILLIS_TO_PASS_BETWEEN_KEYS_STROKES;
            // System.out.println(currentSearchText);
            // fetchResourceTypes(currentSearchText, 100);
            
            // start thread that will run once just after input unlocked
            ex.schedule(() -> updateTypesList(), MILLIS_TO_PASS_BETWEEN_KEYS_STROKES, TimeUnit.MILLISECONDS);
        }
    }
    
    private NodeErrorListener mErrorListener = new NodeErrorListener() {
        @Override
        public void onError(BaseResponse.Error error) {
            System.out.println("onError");
        }
    };
    
}
