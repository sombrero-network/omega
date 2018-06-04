package network.omega.ui.resource.vm;

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import network.omega.ui.resource.ResourceItem;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DownloadImage implements Callable<String> {
    
    List<ResourceItem> selectedResources;
    ResourceLauncher resourceLauncher;
    
    String baseDesription;
    
    public DownloadImage(List<ResourceItem> selectedResources, ResourceLauncher resourceLauncher) {
        this.selectedResources = selectedResources;
        this.resourceLauncher = resourceLauncher;
        this.baseDesription = resourceLauncher.getCurrentDescription();
    }
    
    @Override
    public String call() throws Exception {
        // Thread.sleep(4000);
        List<ResourceItem> uniqueTypes = getResourcesOfUniqueTypes(selectedResources);
        for (ResourceItem currentUniqueType : uniqueTypes) {
            
            File resFilesRoot = new File(
                    currentUniqueType.vm.getValue() + File.separator + currentUniqueType.getName());
            Files.createDirectories(resFilesRoot.toPath());
            
            System.out.println(resFilesRoot);
            // download the torrent file
            // http://releases.ubuntu.com/16.04/ubuntu-16.04.4-desktop-amd64.iso.torrent
            URL torrentUrl = new URL("http://releases.ubuntu.com/16.04/ubuntu-16.04.4-desktop-amd64.iso.torrent");
            String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
            URLConnection conn = torrentUrl.openConnection();
            conn.setRequestProperty("User-Agent", USER_AGENT);
            InputStream is = conn.getInputStream();
            File torrentFile = new File(
                    resFilesRoot.getAbsolutePath() + File.separator + "ubuntu-16.04.4-desktop-amd64.iso.torrent");
            FileUtils.copyInputStreamToFile(is, torrentFile);
            is.close();
            
            // start torrent
            Client client = new Client(InetAddress.getLocalHost(),
                    SharedTorrent.fromFile(torrentFile, new File(resFilesRoot.getAbsolutePath())));
            client.addObserver(new Observer() {
                @Override
                public void update(Observable observable, Object data) {
                    Client client = (Client) observable;
                    BigDecimal progress = new BigDecimal(client.getTorrent().getCompletion());
                    BigDecimal roundedProgress = progress.setScale(0, BigDecimal.ROUND_HALF_EVEN);
                    resourceLauncher.setDescription(true, baseDesription + " - '" + currentUniqueType.name.getValue()
                            + "' - " + roundedProgress + " %");
                }
            });
            client.download();
            client.waitForCompletion();
        }
        
        return null;
    }
    
    /**
     * https://stackoverflow.com/questions/23699371/java-8-distinct-by-property
     * 
     * @param keyExtractor
     * @param <T>
     * @return
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        final Set<Object> seen = new HashSet<>();
        return t -> seen.add(keyExtractor.apply(t));
    }
    
    private List<ResourceItem> getResourcesOfUniqueTypes(List<ResourceItem> selectedResources) {
        return selectedResources.stream().filter(distinctByKey(r -> r.getName())).collect(Collectors.toList());
    }
}
