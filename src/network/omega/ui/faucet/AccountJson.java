package network.omega.ui.faucet;

public class AccountJson {
    
    public String name;
    // owner_key: owner_private
    // .toPublicKey()
    // .toPublicKeyString(),
    public String owner_key;
    // active_key: active_private
    // .toPublicKey()
    // .toPublicKeyString(),
    public String active_key;
    // memo_key: active_private
    // .toPublicKey()
    // .toPublicKeyString(),
    public String memo_key;
    // refcode: refcode,
    public String refcode;
    // referrer: referrer
    public String referrer;
    
    public AccountJson(String name, String owner_key, String active_key, String memo_key, String refcode,
            String referrer) {
        this.name = name;
        this.owner_key = owner_key;
        this.active_key = active_key;
        this.memo_key = memo_key;
        this.refcode = refcode;
        this.referrer = referrer;
    }
    
    public String json() {
        // "{\"account\":{\"name\":\"u-17\",\"owner_key\":\"BTS6maJw69mP92BJnbHsojDforXxoJMoc7NuJip1KCC3bdRAqsy3b\",\"active_key\":\"BTS5KutynafCPJBHLVAMC3NaefZ5wyCjZTemuboHP3rKaUVKxb66K\",\"memo_key\":\"BTS5KutynafCPJBHLVAMC3NaefZ5wyCjZTemuboHP3rKaUVKxb66K\",\"refcode\":null,\"referrer\":null}}"
        
        return "{\"account\": {" + "\"name\":\"" + this.name + "\"," + "\"owner_key\":\"" + this.owner_key + "\","
                + "\"active_key\":\"" + this.active_key + "\"," + "\"memo_key\":\"" + this.memo_key + "\","
                + "\"refcode\":null," + "\"referrer\":null" + "}}";
    }
}
