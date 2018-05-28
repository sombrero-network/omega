package network.omega.ui.faucet;

import cy.agorise.graphenej.Address;
import cy.agorise.graphenej.BrainKey;
import cy.agorise.graphenej.FromSeed;

public class KeysGenerator {
    
    public String account_name;
    public Keys owner_private;
    public Keys active_private;
    public String password;
    
    public Keys generateKeyFromPassword(String accountName, String role, String password) {
        String seed = accountName + role + password;
        Keys keys = new Keys();
        FromSeed privateKeyFromSeed = new FromSeed(seed, 0);
        
        Address publicAddress = privateKeyFromSeed.getPublicAddress(Address.BITSHARES_PREFIX);
        
        /// home/c/CLionProjects/bitshares-ui/node_modules/bitsharesjs/es/ecc/src/PrivateKey.js
        /// : 44
        // keys.privateKey = PrivateKey.fromSeed(seed);
        keys.privateKey = privateKeyFromSeed.getPrivateKey();
        
        // keys.publicKey = privKey.toPublicKey().toString();
        keys.publicKey = publicAddress.toString();
        
        return keys;
    }
    
    public static KeysGenerator generate(String accountName, String password) {
        KeysGenerator result = new KeysGenerator();
        
        /// home/c/CLionProjects/bitshares-ui/app/actions/WalletActions.js : 56
        result.account_name = accountName;
        result.active_private = result.generateKeyFromPassword(accountName, "active", password);
        result.owner_private = result.generateKeyFromPassword(accountName, "owner", password);
        result.password = password;
        
        System.out.println("create account: " + accountName);
        System.out.println("password: " + password);
        System.out.println("new active pubkey: " + result.active_private.publicKey);
        System.out.println("new owner pubkey: " + result.owner_private.publicKey);
        
        // BTS8NSgCFztfEEhS2eceToShc9hDBfKjL73PoXxgnmshhthzXBCKM
        // BTS4wTAhj7NmzFKvhSyJZc5GfzNdiwzYdk5oRKvCAM9aUajT78Sec
        return result;
    }
}
