package worksheets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static worksheets.Util.delDirTree;
import static worksheets.Util.delay;
import static worksheets.Util.reflectiveGetField;
import static worksheets.Util.reflectiveGetMethod;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.util.Pair;

@ExtendWith(ApplicationExtension.class)
public class GuiTest extends MainApp {

    private File testFolder;

    @Start
    public void onStart(Stage primaryStage) throws Exception {
        testFolder = new File(System.getProperty("user.home"), ".fertilizer");

        delDirTree(testFolder);
        String key = MainController.LAST_USED_FOLDER;
        File registryFolder = new File(Preferences.userNodeForPackage(app.Model.class).get(key, testFolder.toString()));
        delDirTree(registryFolder);

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
    void testSaveAndLoad(FxRobot robot) throws Exception {
        delay(2);
        robot.clickOn("File");
  

    }



    @Test
    void testRequirementEntry(FxRobot robot) {
        robot.clickOn("Solution");
        robot.doubleClickOn("18.00");
        robot.push(KeyCode.DIGIT3);
        robot.push(KeyCode.DIGIT6);
        robot.push(KeyCode.DIGIT1);
        robot.push(KeyCode.ENTER);
        // solve assert solution changed
    }

    @Test
    void testRelationshipEntry(FxRobot robot) {
        robot.clickOn("Solution");
        delay(1);
        robot.doubleClickOn("EQ");
        robot.push(KeyCode.SPACE);
        robot.push(KeyCode.DOWN);
        robot.push(KeyCode.ENTER);

        delay(1);
        robot.clickOn("GEQ"); // solve assert solution changed
    }

    @Test
    void testInfeasibleEntry(FxRobot robot) {
        robot.clickOn("Solution");
        robot.doubleClickOn("18.00");
        robot.push(KeyCode.DIGIT9);
        robot.push(KeyCode.DIGIT6);
        robot.push(KeyCode.DIGIT1);
        robot.push(KeyCode.ENTER);
        delay(1);
        // solve assert solution changed
    }
    
    @Test
    void testRoundingCornerCase(FxRobot robot) {
        robot.clickOn("Solution");
        robot.clickOn("Relationship");
        robot.push(KeyCode.RIGHT);
        robot.push(KeyCode.RIGHT);
        robot.push(KeyCode.SPACE);
        delay(1);
        robot.push(KeyCode.SPACE);
        delay(1);
        robot.push(KeyCode.DOWN);
        robot.push(KeyCode.SPACE);
        robot.push(KeyCode.ENTER);
        
        robot.clickOn("Relationship");
        robot.push(KeyCode.RIGHT);
        robot.push(KeyCode.RIGHT);
        robot.push(KeyCode.DOWN);
        robot.push(KeyCode.SPACE);
        robot.push(KeyCode.DIGIT2);
        robot.push(KeyCode.DIGIT1);
        robot.push(KeyCode.ENTER);

        robot.clickOn("Relationship");
        robot.push(KeyCode.RIGHT);
        robot.push(KeyCode.RIGHT);
        robot.push(KeyCode.RIGHT);
        robot.push(KeyCode.DOWN);
        robot.push(KeyCode.SPACE);
        robot.push(KeyCode.DIGIT0);
        robot.push(KeyCode.ENTER);
      
        delay(1);
        robot.clickOn("Relationship");
        robot.push(KeyCode.RIGHT);
        robot.push(KeyCode.RIGHT);
        robot.push(KeyCode.RIGHT);
        robot.push(KeyCode.RIGHT);
        robot.push(KeyCode.DOWN);
        robot.push(KeyCode.SPACE);
        robot.push(KeyCode.DIGIT0);
        robot.push(KeyCode.ENTER);
      


        delay(1);
        delay(10);
        // solve assert solution changed
    }

   

    // tests for code coverage
    @Test
    void testBadEntry(FxRobot robot) {
        robot.clickOn("Solution");
        robot.doubleClickOn("0.24");
        robot.push(KeyCode.Z);
        robot.push(KeyCode.Z);
        robot.push(KeyCode.ENTER);
    }

    // tests for code coverage
    @Test
    void testEnables(FxRobot robot) {
        robot.doubleClickOn("true");
        robot.push(KeyCode.SPACE);
    }
    
    // tests for code coverage
    @Test
    void testBatchWt(FxRobot robot) {
        robot.clickOn("Work Order");
        robot.doubleClickOn("8000.0");
        robot.push(KeyCode.DIGIT9);
        robot.push(KeyCode.DIGIT0);
        robot.push(KeyCode.DIGIT0);
        robot.push(KeyCode.DIGIT0);
        robot.push(KeyCode.ENTER);
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