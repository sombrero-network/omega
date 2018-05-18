package graphenej.api;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;
import cy.agorise.graphenej.test.NaiveSSLContext;
import org.junit.Before;

import javax.net.ssl.SSLContext;

/**
 * Base class that every test that involves any communication with the API must extend
 */

public class BaseApiTest {
    protected String NODE_URL = System.getenv("NODE_URL");

    protected SSLContext context;
    protected WebSocket mWebSocket;

    @Before
    public void setUp() throws Exception {
        context = NaiveSSLContext.getInstance("TLS");
        WebSocketFactory factory = new WebSocketFactory();

        // Set the custom SSL context.
        factory.setSSLContext(context);

        mWebSocket = factory.createSocket(NODE_URL);
    }

}
