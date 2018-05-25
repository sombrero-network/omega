package network.omega.ui.resource;

import oshi.hardware.HardwareAbstractionLayer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class VBoxManager {
    public static String MIN_VBOX_VERSION = "5.2.8";

    private static String virtualBoxVersionGetMainPart(String versionInstalled) {
        //5.2.8r121009
        String[] l1 = versionInstalled.split("r");
        String main = l1[0];
        return main;
        //String[] l2 = main.split("\\.");


        //String dev = l1[1];
        //return false;
    }

    /**
     * https://stackoverflow.com/questions/6701948/efficient-way-to-compare-version-strings-in-java
     * Compares two version strings.
     * <p>
     * Use this instead of String.compareTo() for a non-lexicographical
     * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
     *
     * @param str1 a string of ordinal numbers separated by decimal points.
     * @param str2 a string of ordinal numbers separated by decimal points.
     * @return The result is a negative integer if str1 is _numerically_ less than str2.
     * The result is a positive integer if str1 is _numerically_ greater than str2.
     * The result is zero if the strings are _numerically_ equal.
     * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
     */
    public static int versionCompare(String str1, String str2) {
        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i = 0;
        // set index to first non-equal ordinal or length of shortest version string
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }
        // compare first non-equal ordinal number
        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }
        // the strings are equal or one string is a substring of the other
        // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
        return Integer.signum(vals1.length - vals2.length);
    }

    public static String getVirtualBoxInstalledVersion(HardwareAbstractionLayer hal) {

        //linux, mac default paths
        //https://stackoverflow.com/questions/7363391/on-os-x-using-virtualboxs-command-line-interface-how-can-i-instruct-a-vm-to-o
        //https://websiteforstudents.com/install-virtualbox-latest-on-ubuntu-16-04-lts-17-04-17-10/
        //sudo sh -c 'echo "deb http://download.virtualbox.org/virtualbox/debian $(lsb_release -sc) contrib" >> /etc/apt/sources.list.d/virtualbox.list'
        //sudo apt-get update
        //sudo apt-get -y install gcc make linux-headers-$(uname -r) dkms
        //sudo apt-get install virtualbox-5.2
        //sudo apt-get remove virtualbox virtualbox-5.1 virtualbox-5.2
        if (com.sun.jna.Platform.isLinux() || com.sun.jna.Platform.isMac()) {
            try {
                Process p = Runtime.getRuntime().exec("VBoxManage -v");
                InputStream is = p.getInputStream();
                //1
                String version = new Scanner(is, "utf-8").useDelimiter("\n").next();
                String installedVersion  = virtualBoxVersionGetMainPart(version);
                int comparisonResult = versionCompare(MIN_VBOX_VERSION, installedVersion);
                if (comparisonResult <= 0) {
                    return installedVersion;
                }
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("VBox is not installed.");
            }
        }
        //win default paths
        //TODO: feature to change path in app Settings and set to default otherwise
        String vboxManagerPath = null;
        String winPath1 = "C:\\Program Files\\Oracle\\VirtualBox\\VBoxManage.exe";
        String winPath2 = "C:\\Program Files (x86)\\Oracle\\VirtualBox\\VBoxManage.exe";
        if (com.sun.jna.Platform.isWindows()) {
            if (new File(winPath1).exists()) {
                vboxManagerPath = winPath1;
            } else if (new File(winPath2).exists()) {
                vboxManagerPath = winPath2;
            }
        }
        if (vboxManagerPath != null) {
            try {
                Process p = Runtime.getRuntime().exec(vboxManagerPath + " -v");
                InputStream is = p.getInputStream();
                //1
                String version = new Scanner(is, "utf-8").useDelimiter("\n").next();

                //2
//                    InputStreamReader isr = new InputStreamReader(is);
//                    BufferedReader br = new BufferedReader(isr);
//                    br.lines().collect(Collectors.joining("\n"));

                //3
//                    String line = null;
//                    while( (line = br.readLine()) != null){
//                        System.out.println(line);
//                    }
                String installedVersion = virtualBoxVersionGetMainPart(version);
                int comparisonResult = versionCompare(MIN_VBOX_VERSION, installedVersion);
                if (comparisonResult <= 0) {
                    return installedVersion;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }






        //mac default paths

        return null;
    }
}
