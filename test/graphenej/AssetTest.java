package graphenej;

import cy.agorise.graphenej.Asset;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;

/**
 * Created by nelson on 12/24/16.
 */
public class AssetTest {
    
    // @Test
    public void equals() throws Exception {
        Asset bts = new Asset("1.3.0");
        Asset bitUSD = new Asset("1.3.121");
        assertNotEquals("Different assets should not be equal", bts, bitUSD);
    }
    
}