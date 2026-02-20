package worksheets;

import static worksheets.Util.delay;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;

import org.junit.jupiter.api.Test;

import app.Launcher;
import app.MainApp;

class LaunchingMainTest {

    // For code coverage of main method
    @Test
    void testMainLauncher() throws IOException {
        delay(10);
        Launcher launcher = new Launcher();
        Throwable t = null;
        int x = (int) (Math.random() * 1000000.0);
        System.setProperty("user.home", "C:/Test/Southtown/Barcode/" + x);

        try {
            Thread launcherThread = new Thread(() -> shutdown());
            launcherThread.start();
            Launcher.main(new String[0]);
        } catch (Throwable t1) {
            t1.printStackTrace();
            t = t1;
        } finally {

        }
        assert (t == null);
        delay(2);
    }

    private void shutdown() {
        delay(4);
        MainApp.close();
        delay(10);
        File userHomeTestDir = new File(System.getProperty("user.home")).getParentFile();
        try {
            Files.walk(userHomeTestDir.toPath())
                .sorted(Comparator.reverseOrder()) // Sort in reverse order (files before folders)
                .forEach(path -> path.toFile().deleteOnExit());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("shutdown");

    }

}
