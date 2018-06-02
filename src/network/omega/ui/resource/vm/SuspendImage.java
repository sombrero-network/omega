package network.omega.ui.resource.vm;

import java.util.concurrent.Callable;

public class SuspendImage implements Callable<String> {
    
    public SuspendImage() {
        
    }
    
    @Override
    public String call() throws Exception {
        Thread.sleep(3000);
        return null;
    }
}
