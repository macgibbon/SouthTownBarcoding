package worksheets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static worksheets.Util.delDirTree;
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
import app.MainController;
import javafx.application.Platform;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class GuiTest extends MainApp {

    private File testFolder;

    @Start
    public void onStart(Stage primaryStage) throws Exception {
        testFolder = new File(System.getProperty("user.home"), "testbarcodes");

        delDirTree(testFolder);
        String key = MainController.LAST_USED_FOLDER;
      
        super.start(primaryStage);
        // assert folder doesn't exist
    }

    @Stop
    void onStop() throws Exception {
        delay(4);
        super.stop();
    }

    @AfterAll
    public static void cleanup() {
        // Platform.exit();
    }

    @Test
    void testLoad(FxRobot robot) throws Exception {
        delay(2);
        robot.clickOn("File");
        robot.clickOn("Open Inventory Report");
  

    }




    void testUncaughtExceptionHandler() {
        Throwable error = null;
        try {
            Platform.runLater(() -> {
;        
            });

        } catch (Throwable t) {
            error = t;
            t.printStackTrace();
        }
        assertTrue(error == null);
    }

   
 

}