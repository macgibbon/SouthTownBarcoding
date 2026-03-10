
package worksheets;

import static worksheets.Util.delay;

import java.io.File;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.framework.junit5.Stop;

import app.MainApp;
import app.Model;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class FailOnStartTest extends MainApp {


    Model model;
    File testFolder;

    @Start
    public void onStart(Stage primaryStage) throws Exception {     
        super.start(primaryStage);

    }

    @Stop
    void onStop() throws Exception {
        delay(4);
        super.stop();
    }

    @AfterAll
    public static void cleanup() {
        MainApp.close();
    }

    @Test
    public void testfailOnstart(FxRobot robot) {
      
    }

    @Override
    public void loadDefaults() throws Exception {
        throw new RuntimeException("Test Exception in start");
    }


}