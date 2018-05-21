package network.omega.ui.preferences;

import network.omega.ui.faucet.KeysGenerator;
import network.omega.ui.utils.RandomPassword;
import sun.security.krb5.internal.crypto.Des;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ManageLocalStorage {
    private Preferences prefs;

    static String userHome = System.getProperty("user.home");
    static Path applicationDir = Paths.get(userHome, ".omegagovernance");
    static Desktop ds;

    public static String username;
    public static String password;

    public static void openPasswordFileFolder(){
        if(Desktop.isDesktopSupported()){
            ds = Desktop.getDesktop();
            try {
                //Path passwordFile = applicationDir.resolve("password.dat");
                ds.open(applicationDir.toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void readPasswordFile(){
        List<String> list = new ArrayList<>();

        try (Stream<String> stream = Files.lines(applicationDir.resolve("password.dat"))) {
            list = stream.collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }

        username = list.get(0);
        password = list.get(1);
    }

    public static Boolean passwordFileExists(){
        Path passwordFile = applicationDir.resolve("password.dat");
        if(passwordFile.toFile().exists()){
            return true;
        }
        return false;
    }

    public static void createStorageDirectory(){
        try {
            if(!Files.exists(applicationDir)) {
                Files.createDirectories(applicationDir);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void storePasswordFile(KeysGenerator kg){
        createStorageDirectory();
        Path passwordFile = applicationDir.resolve("password.dat");
        // Files.newBufferedWriter() uses UTF-8 encoding by default
        try (BufferedWriter writer = Files.newBufferedWriter(passwordFile)){
            writer.write(kg.account_name + "\n");
            writer.write(kg.password + "\n");
        }catch (IOException e){};
    }



    public void setPreference() {
        // This will define a node in which the preferences can be stored
        prefs = Preferences.userRoot().node(this.getClass().getName());
        String ID1 = "Test1";
        String ID2 = "Test2";
        String ID3 = "Test3";

        // First we will get the values
        // Define a boolean value
        System.out.println(prefs.getBoolean(ID1, true));
        // Define a string with default "Hello World
        System.out.println(prefs.get(ID2, "Hello World"));
        // Define a integer with default 50
        System.out.println(prefs.getInt(ID3, 50));

        // now set the values
        prefs.putBoolean(ID1, false);
        prefs.put(ID2, "Hello Europa");
        prefs.putInt(ID3, 45);

        // Delete the preference settings for the first value
        prefs.remove(ID1);

    }

    public static void main(String[] args) {
        ManageLocalStorage test = new ManageLocalStorage();
        KeysGenerator kg = KeysGenerator.generate("u-18", RandomPassword.newPassword(70));
        test.storePasswordFile(kg);
        //test.setPreference();
        //System.out.println(test.prefs.absolutePath());

    }
}