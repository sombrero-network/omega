package network.omega.ui.faucet;


import network.omega.ui.preferences.ManageLocalStorage;
import network.omega.ui.utils.RandomPassword;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
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
    public static void main(String[] args) {
        RegisterUsingFaucet.register("u-17");
    }

    public static Boolean register(String username) {
        Client client = null;
        try {
            //SSLContext sc = SSLContext.getInstance("SSL");//Java 6
            SSLContext sc = SSLContext.getInstance("TLSv1");//Java 8
            System.setProperty("https.protocols", "TLSv1");//Java 8


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



            //will get 201 when successfull registration, everything else are errors of some sort
            if(response.getStatus() == 201){
                client.close();
                //save password and username with public keys to wss into file
                ManageLocalStorage.storePasswordFile(kg);
                return true;
            }else{
                client.close();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if(client != null){
                client.close();
            }
        }
        return false;
    }
}