package graphenej.api;

import cy.agorise.graphenej.Asset;
import cy.agorise.graphenej.api.GetAccountByName;
import cy.agorise.graphenej.api.LookupAccounts;
import cy.agorise.graphenej.api.LookupAssetSymbols;
import cy.agorise.graphenej.api.android.NodeConnection;
import cy.agorise.graphenej.errors.RepeatedRequestIdException;
import cy.agorise.graphenej.interfaces.NodeErrorListener;
import cy.agorise.graphenej.interfaces.WitnessResponseListener;
import cy.agorise.graphenej.models.BaseResponse;
import cy.agorise.graphenej.models.WitnessResponse;
import javafx.scene.control.Label;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SombreroNetworkNodeAPITest extends BaseSombreroApiTest {
    
    // used for private API calls
    NodeConnection nodeConnection;
    
    @Before
    public void init() {
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl("wss://testnet.sombrero.network/ws");
    }
    
    @Test
    public void testGetAccountByNameRequest() {
        String ACCOUNT_NAME = "u-1";
        nodeConnection.connect("", "", false, mErrorListener);
        
        System.out.println("Adding GetAccountByName here");
        try {
            nodeConnection.addRequestHandler(new GetAccountByName(ACCOUNT_NAME, false, new WitnessResponseListener() {
                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("GetAccountByName.onSuccess");
                }
                
                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("GetAccountByName.onError. Msg: " + error.message);
                }
            }));
        } catch (RepeatedRequestIdException e) {
            System.out.println("RepeatedRequestIdException. Msg: " + e.getMessage());
        }

        Timer timer = new Timer();
        timer.schedule(releaseTask, 7000);
        try {
            // Holding this thread while we get update notifications
            synchronized (this) {
                wait();
            }
        } catch (InterruptedException e) {
            System.out.println("InterruptedException. Msg: " + e.getMessage());
        }
    }
    
    @Test
    public void lookupAccount() {
        mWebSocket.addListener(new LookupAccounts("u-1", 50, true, new WitnessResponseListener() {
            @Override
            public void onSuccess(WitnessResponse response) {
                System.out.println("onSuccess");
                
            }
            
            @Override
            public void onError(BaseResponse.Error error) {
                System.out.println("onError");
            }
        }));
    }
    
    private NodeErrorListener mErrorListener = new NodeErrorListener() {
        @Override
        public void onError(BaseResponse.Error error) {
            System.out.println("onError");
        }
    };


    /**
     * Task that will release the worker thread, effectively terminating this
     * test
     */
    private TimerTask releaseTask = new TimerTask() {
        @Override public void run() {
            System.out.println("Releasing lock!");
            synchronized (SombreroNetworkNodeAPITest.this) {
                SombreroNetworkNodeAPITest.this.notifyAll();
            }
        }
    };
}
