package worksheets;

import static worksheets.Util.delay;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

import app.Launcher;
import app.MainApp;
import app.Model;

class LaunchingMainTest extends MainApp {

    // For code coverage of main method
    @Test
    void testMainLauncher() throws IOException {
        File userDir = new File(System.getProperty("user.home"));
        File appDir = new File(userDir,".fertilizer");
        if (appDir.exists()) {
            Files.list(appDir.toPath()).forEach( path -> path.toFile().delete());
            appDir.delete();
        }
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
    
    
    // Test console stack trace when Gui framework is not available for dialog.
    @Test
    void testShowingStackTrackForExceptionInErrorDialog() {
        showErrorDialog( new Thread(), new Exception());
    }
  

    private void shutdown() {
        delay(17);
        MainApp.close();
    }
       
    
    @Test
    void testModelSingleton() {
        Model model = Model.getInstance();
        Model model2 = Model.getInstance();
        assert( model == model2);
        
        Launcher launcher = new Launcher();
    }
  
}
