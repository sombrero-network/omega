package network.omega.ui.resource.vm;

import java.util.concurrent.Callable;

public class ValidateImage implements Callable<String> {
    
    public ValidateImage() {
        
    }
    
    @Override
    public String call() throws Exception {
        Thread.sleep(5000);
        return null;
    }
}
