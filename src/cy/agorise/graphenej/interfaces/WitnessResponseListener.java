package cy.agorise.graphenej.interfaces;

import cy.agorise.graphenej.models.BaseResponse;
import cy.agorise.graphenej.models.WitnessResponse;
import javafx.scene.control.Label;

/**
 * Class used to represent any listener to network requests.
 */
public interface WitnessResponseListener {
    
    void onSuccess(WitnessResponse response);
    
    void onError(BaseResponse.Error error);
    
}
