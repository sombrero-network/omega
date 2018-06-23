package network.omega.ui.resource.vm;

import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.LibTorrent;
import com.frostwire.jlibtorrent.SessionManager;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.alerts.AddTorrentAlert;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.AlertType;
import com.frostwire.jlibtorrent.alerts.BlockFinishedAlert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

/**
 * @author gubatron
 * @author aldenml
 */
public final class JLibtorrentTest {

    public static String copyDllFromJarIntoTempFolder(String name) throws IOException {
        InputStream in = JLibtorrentTest.class.getResourceAsStream(name);
        byte[] buffer = new byte[1024];
        int read = -1;
        File temp = File.createTempFile(name, "");
        FileOutputStream fos = new FileOutputStream(temp);

        while((read = in.read(buffer)) != -1) {
            fos.write(buffer, 0, read);
        }
        fos.close();
        in.close();
        //System.out.println(temp.getAbsolutePath());
        //System.load(temp.getAbsolutePath());
        return temp.getAbsolutePath();
    }

    public static void main(String[] args) throws Throwable {

        System.setProperty("jlibtorrent.jni.path",
                copyDllFromJarIntoTempFolder("/jlibtorrent/windows/x86_64/jlibtorrent-1.2.0.16.dll"));



        // comment this line for a real application
        args = new String[]{"C:\\repos\\omega-governance-sombero\\vms\\ubuntu-16.04-x64.torrent"};

        File torrentFile = new File(args[0]);

        System.out.println("Using libtorrent version: " + LibTorrent.version());

        final SessionManager s = new SessionManager();

        final CountDownLatch signal = new CountDownLatch(1);

        s.addListener(new AlertListener() {
            @Override
            public int[] types() {
                return null;
            }

            @Override
            public void alert(Alert<?> alert) {
                AlertType type = alert.type();

                switch (type) {
                case ADD_TORRENT:
                    System.out.println("Torrent added");
                    ((AddTorrentAlert) alert).handle().resume();
                    break;
                case BLOCK_FINISHED:
                    BlockFinishedAlert a = (BlockFinishedAlert) alert;
                    int p = (int) (a.handle().status().progress() * 100);
                    System.out.println("Progress: " + p + " for torrent name: " + a.torrentName());
                    System.out.println(s.stats().totalDownload());
                    break;
                case TORRENT_FINISHED:
                    System.out.println("Torrent finished");
                    signal.countDown();
                    break;
                }
            }
        });

        s.start();

        TorrentInfo ti = new TorrentInfo(torrentFile);
        //s.download(ti, torrentFile.getParentFile());
        s.download(ti, new File("C:\\repos\\omega-governance-sombero\\vms\\test"));

        signal.await();

        s.stop();
    }
}