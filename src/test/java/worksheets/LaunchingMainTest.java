package worksheets;

import static worksheets.Util.delay;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import app.Launcher;
import app.MainApp;

class LaunchingMainTest extends MainApp {

    // For code coverage of main method
    @Test
    void testMainLauncher() throws IOException {
        Launcher launcher = new Launcher();
        Throwable t = null;
        try {
            Thread launcherThread = new Thread(() ->  shutdown());
            launcherThread.start();
            Launcher.main(new String[0]);   
           } catch (Throwable t1) {
               t1.printStackTrace();
            t = t1;
        }
        assert (t == null);
    }
  

    private void shutdown() {
        delay(2);
        MainApp.close();
    }
       
   
  
}
