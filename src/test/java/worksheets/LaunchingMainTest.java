package worksheets;

import static worksheets.Util.delay;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import app.Launcher;
import app.MainApp;

class LaunchingMainTest  {

    // For code coverage of main method
    @Test
    void testMainLauncher() throws IOException {
        delay(10);
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
        delay(2);
    }
  

    private void shutdown() {
        delay(4);
        MainApp.close();System.out.println("shutdown");
        
    }
       
   
  
}
