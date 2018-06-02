package network.omega.ui.resource.vm;

import java.util.concurrent.Callable;

public class StartImage implements Callable<String> {
    
    public StartImage() {
        
    }
    
    @Override
    public String call() throws Exception {
        Thread.sleep(4000);
        return null;
    }
}
