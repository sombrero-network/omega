package network.omega.ui.faucet;


import network.omega.ui.preferences.ManageLocalStorage;
import network.omega.ui.utils.RandomPassword;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

public class RegisterUsingFaucet {
    public final static String UNKNOWN_FAUCET_ERROR = "Registration is not possible\nat the moment.";
    public final static String PREMIUM_USERNAME_REG_NOT_ALLOWED_ON_FAUCET = "Premium names registration is not\n" +
            "allowed. Please include '0-9', '-' or '.'\n" +
            "character in your name";
    public final static String USERNAME_DOESNT_MATCH_VALIDATION_REGEX = "Possible chars: 'a-z' , 0-9, '.' and '-'.\n" +
            "Must start/end with: 'a-z' , 0-9.\n" +
            "Must be 3+ characters long.";

    public static void main(String[] args) {
        RegisterUsingFaucet.register("u-17");
    }

    private static String getErrorMessageFromJSONObjectWithJSONArray(JSONObject errorObj, String arrayName){
        JSONArray errorsArray = errorObj.getJSONArray(arrayName);
        for (int i = 0; i < errorsArray.length(); i++) {
            String currentErrorObj = (String) errorsArray.get(i);
            if (currentErrorObj != null && currentErrorObj != "") {
                return currentErrorObj;
            }
        }
        return UNKNOWN_FAUCET_ERROR;
    }

    public static String register(String username) {
        Client client = null;
        try {
            SSLContext sc = SSLContext.getInstance("SSL");//Java 6
            //SSLContext sc = SSLContext.getInstance("TLSv1");//Java 8
            //System.setProperty("https.protocols", "TLSv1");//Java 8


            TrustManager[] trustAllCerts = { new InsecureTrustManager() };
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HostnameVerifier allHostsValid = new InsecureHostnameVerifier();

            client = ClientBuilder.newBuilder().sslContext(sc).hostnameVerifier(allHostsValid).build();

//            HttpAuthenticationFeature feature = HttpAuthenticationFeature.universalBuilder()
//                    .credentialsForBasic(username, password).credentials(username, password).build();
//            client.register(feature);

            //create keys for username and password
            KeysGenerator kg = KeysGenerator.generate(username, RandomPassword.newPassword(70));
            String jsonStr = new AccountJson(kg.account_name,kg.owner_private.publicKey ,kg.active_private.publicKey,kg.active_private.publicKey,"","").json();



            //PUT request, if need uncomment it
            //final Response response = client
            //.target("https://localhost:7002/VaquarKhanWeb/employee/api/v1/informations")
            //.request().put(Entity.entity(input, MediaType.APPLICATION_JSON), Response.class);
            //GET Request
            Invocation.Builder reqBuilder = client
                    .target("https://testnet.sombrero.network/api/v1/accounts")
                    .request(MediaType.APPLICATION_JSON_TYPE).accept("application/json").header("Content-type", "application/json ; ");
            Entity jsonEntity = Entity.json(jsonStr);


            final Response response = reqBuilder.post(jsonEntity);

             //will get code 422 when trying registering with existing user
//            The 422 (Unprocessable Entity) status code means the server understands the content type of the
//            request entity (hence a 415(Unsupported Media Type) status code is inappropriate), and the syntax
//            of the request entity is correct (thus a 400 (Bad Request) status code is inappropriate) but
//            was unable to process th

            //String output = response.readEntity(String.class);
//            System.out.println("Output from Server .... \n");
//            System.out.println(output);
            String output = response.readEntity(String.class);

            //will get 201 when successfull registration, everything else are errors of some sort
            if(response.getStatus() == 201) {
                client.close();
                //save password and username with public keys to wss into file
                ManageLocalStorage.storePasswordFile(kg);
                return null;
            }else if(response.getStatus() == 422){
                if(output.startsWith("{\"error\"")){
                    //parse json string
                    final JSONObject obj = new JSONObject(output);
                    if(obj instanceof JSONObject){
                        final JSONObject errorObj = obj.getJSONObject("error");
                        if(errorObj != null){
                            if(errorObj.has("base")) {
                                String errorMessage = getErrorMessageFromJSONObjectWithJSONArray(errorObj, "base");
                                if(errorMessage.startsWith("Premium names registration is not supported")){
                                    return PREMIUM_USERNAME_REG_NOT_ALLOWED_ON_FAUCET;
                                }else{
                                    return errorMessage;
                                }
                            }
                            if(errorObj.has("name")) {
//{"name":["Only lowercase alphanumeric characters, dashes and periods. Must start with a letter and cannot end with a dash."]}
                                //most likely mean that  regular expression set to validate username on faucet
                                ///home/c/CLionProjects/faucet/app/models/bts_account.rb
                                //doesn't match to regular expression in LoginController
                                String errorMessage = getErrorMessageFromJSONObjectWithJSONArray(errorObj, "name");
                                if(errorMessage.startsWith("Premium names registration is not supported")){
                                    return USERNAME_DOESNT_MATCH_VALIDATION_REGEX;
                                }else{
                                    return errorMessage;
                                }
                            }
                        }
                    }else{
                        client.close();
                        return UNKNOWN_FAUCET_ERROR;
                    }
                }
            }else{
                client.close();
                return UNKNOWN_FAUCET_ERROR;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if(client != null){
                client.close();
                return UNKNOWN_FAUCET_ERROR;
            }
        }
        return UNKNOWN_FAUCET_ERROR;
    }
}