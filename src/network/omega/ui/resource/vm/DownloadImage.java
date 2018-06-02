package network.omega.ui.resource.vm;

import network.omega.ui.resource.ResourceItem;

import java.util.List;
import java.util.concurrent.Callable;

public class DownloadImage implements Callable<String> {
    
    List<ResourceItem> selectedResources;
    
    public DownloadImage(List<ResourceItem> selectedResources) {
        this.selectedResources = selectedResources;
    }
    
    @Override
    public String call() throws Exception {
        Thread.sleep(4000);
        // for(ResourceItem cr : selectedResources){
        // System.out.println(cr.rd.ramMinSpeed);
        // }
        return null;
    }
}
