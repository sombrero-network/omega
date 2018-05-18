package cy.agorise.graphenej.interfaces;

import cy.agorise.graphenej.models.BaseResponse;
import javafx.scene.control.Label;

/**
 * Interface to be implemented by any listener to network errors.
 */
public interface NodeErrorListener {
    void onError(BaseResponse.Error error);
}
