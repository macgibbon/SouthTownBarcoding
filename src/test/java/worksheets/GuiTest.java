package worksheets;

import static worksheets.Util.reflectiveGetField;
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
import app.Model;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
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
        robot.push(KeyCode.T);
        robot.push(KeyCode.E);
        robot.push(KeyCode.S);
        robot.push(KeyCode.T);
        robot.push(KeyCode.PERIOD);
        robot.push(KeyCode.X);
        robot.push(KeyCode.L);
        robot.push(KeyCode.S);
        robot.push(KeyCode.ENTER);
        delay(4);
        robot.clickOn("Fresh Pork");
        robot.clickOn("Print Selected Labels");
        Model model = (Model) reflectiveGetField(controller, "model");
        boolean isFirstPrinted = model.productLabels.get(0).printed.get();
        assertTrue(isFirstPrinted, "Printed Lable is not checked!");


     
        
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