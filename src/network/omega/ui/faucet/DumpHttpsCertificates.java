package network.omega.ui.faucet;

import javax.net.ssl.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;

public class DumpHttpsCertificates {
    
    public static void main(String[] args) {
        DumpHttpsCertificates ca = new DumpHttpsCertificates();
        ca.call();
    }
    
    public void call() {
        URL url;
        try {
            url = new URL("https://testnet.sombrero.network/api/v1/accounts");
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.connect();
            
            // dumpl all cert info
            print_https_cert(con);
            
            // dump all the content
            print_content(con);
            
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void print_https_cert(HttpsURLConnection con) {
        
        if (con != null) {
            
            try {
                
                System.out.println("Response Code : " + con.getResponseCode());
                System.out.println("Cipher Suite : " + con.getCipherSuite());
                System.out.println("\n");
                
                Certificate[] certs = con.getServerCertificates();
                for (Certificate cert : certs) {
                    System.out.println("Cert Type : " + cert.getType());
                    System.out.println("Cert Hash Code : " + cert.hashCode());
                    System.out.println("Cert Public Key Algorithm : " + cert.getPublicKey().getAlgorithm());
                    System.out.println("Cert Public Key Format : " + cert.getPublicKey().getFormat());
                    System.out.println("\n");
                }
                
            } catch (SSLPeerUnverifiedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
        
    }
    
    private void print_content(HttpsURLConnection con) {
        if (con != null) {
            
            try {
                
                System.out.println("****** Content of the URL ********");
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                
                String input;
                
                while ((input = br.readLine()) != null) {
                    System.out.println(input);
                }
                br.close();
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
        
    }
}
