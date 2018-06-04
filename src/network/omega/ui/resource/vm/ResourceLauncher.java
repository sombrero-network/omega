package network.omega.ui.resource.vm;

import com.google.common.util.concurrent.AtomicDouble;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.text.Text;
import network.omega.ui.resource.ResourceItem;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class ResourceLauncher {
    ProgressBar pb;
    Label description;
    
    public String getCurrentDescription() {
        return description.getText();
    }
    
    private void runTask(Future<String> result, Callable<String> t, ExecutorService es, Label description)
            throws ExecutionException, InterruptedException {
        result = es.submit(t);
        String r = result.get();
        if (r != null) {
            Platform.runLater(() -> {
                description.setText(r);
            });
        }
    }
    
    public void setDescription(Boolean isVisible, String descr) {
        Platform.runLater(() -> {
            description.setText(descr);
            pb.setVisible(isVisible);
            description.setVisible(isVisible);
        });
    }
    
    private void updateProgressAfterStepCompleted(ProgressBar pb, AtomicDouble currentProgress,
            AtomicDouble totalWorkSteps) {
        Platform.runLater(() -> {
            // System.out.println(currentProgress.addAndGet(1.0d)/
            // totalWorkSteps.get());
            pb.setProgress(currentProgress.addAndGet(1.0d) / totalWorkSteps.get());
        });
    }
    
    public void launchResources(ProgressBar pb, Label description, List<ResourceItem> selectedResources)
            throws ExecutionException, InterruptedException {
        this.description = description;
        this.pb = pb;
        
        final AtomicDouble totalWorkSteps = new AtomicDouble(4.0);
        final AtomicDouble currentProgress = new AtomicDouble(0.0);
        Future<String> result = null;
        ExecutorService es = Executors.newSingleThreadExecutor();
        
        Platform.runLater(() -> {
            // System.out.println(currentProgress.addAndGet(1.0d)/
            // totalWorkSteps.get());
            pb.setProgress(currentProgress.get() / totalWorkSteps.get());
        });
        
        // download torrent image
        setDescription(true, "Downloading");
        DownloadImage t1 = new DownloadImage(selectedResources, this);
        runTask(result, t1, es, description);
        updateProgressAfterStepCompleted(pb, currentProgress, totalWorkSteps);
        
        // start virtualbox image
        setDescription(true, "Starting images...");
        StartImage t2 = new StartImage();
        runTask(result, t2, es, description);
        updateProgressAfterStepCompleted(pb, currentProgress, totalWorkSteps);
        
        // start validate image by running validator inside running VM
        setDescription(true, "Validating images...");
        ValidateImage t3 = new ValidateImage();
        runTask(result, t3, es, description);
        updateProgressAfterStepCompleted(pb, currentProgress, totalWorkSteps);
        
        // hibernate VM's RAM into file
        setDescription(true, "Hibernating images...");
        SuspendImage t4 = new SuspendImage();
        runTask(result, t4, es, description);
        updateProgressAfterStepCompleted(pb, currentProgress, totalWorkSteps);
        
        es.shutdown();
        
        setDescription(false, "");
    }
    
    public void deleteResourcesWithImages() {
        
    }
}
