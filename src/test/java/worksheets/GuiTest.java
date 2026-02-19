
package worksheets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static worksheets.Util.delDirTree;
import static worksheets.Util.delay;
import static worksheets.Util.reflectiveGetField;

import java.io.File;
import java.nio.file.Path;

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
import app.ProductGroup;
import app.ProductId;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class GuiTest extends MainApp {

    private File testFolder;
    Model model;

    @Start
    public void onStart(Stage primaryStage) throws Exception {
        testFolder = new File(System.getProperty("user.home"), "testbarcodes");

        delDirTree(testFolder);
        String key = MainController.LAST_USED_FOLDER;

        super.start(primaryStage);
        model = (Model) reflectiveGetField(controller, "model");
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
    public void testAll(FxRobot robot) {
        testBatchMode(robot);
        testManualModeButtons(robot);
        testManualModeKeypadEntry(robot);       
        testBadDefaultFolder(robot);
        testSpecialCasesForCodeCoverage();        
    }
    
   
    public void testSpecialCasesForCodeCoverage() {
        Exception exc = null;
        try {
            controller.loadDefaultProductFiles(new File("notThere.csv"));
        } catch (Exception e) {
            exc = e;
        }
        assertTrue(exc == null, "No exception on missing default products file!"); 
        String nullresult = controller.capitalizeFirstLetter(null);
        assertTrue(nullresult == null);
        String emptyResult = controller.capitalizeFirstLetter("");
        assertTrue(emptyResult.equals(""));
        delay(2);
        
        
        ProductId pid = ProductId.createProductId(100000, "Chicken tenders");
        assertTrue(pid.productGroup() == ProductGroup.__);
    }

 
    public void testManualModeKeypadEntry(FxRobot robot) {
        robot.clickOn("Manual");

        robot.clickOn("Lookup Product Id");
        delay(2);
        robot.clickOn("Fresh Pork Hot Italian Sausage Patties");
        delay(2);
        String description = ((TextField) reflectiveGetField(controller, "descriptionField")).getText();
        assertEquals(description, "Hot Italian Sausage Patties", "Incorrect Descripton");
        String productId = ((TextField) reflectiveGetField(controller, "productCodeField")).getText();
        assertEquals(productId, "854698", "Incorrect Descripton");

        // Normal case
        robot.push(KeyCode.NUMPAD1);
        robot.push(KeyCode.NUMPAD2);
        robot.push(KeyCode.PERIOD);
        robot.push(KeyCode.NUMPAD3);
        robot.push(KeyCode.NUMPAD4);
        robot.push(KeyCode.NUMPAD6);
        robot.push(KeyCode.ENTER);

        // Negative weight case
        robot.push(KeyCode.MINUS);
        robot.push(KeyCode.NUMPAD2);
        robot.push(KeyCode.PERIOD);
        robot.push(KeyCode.NUMPAD3);
        robot.push(KeyCode.NUMPAD4);
        robot.push(KeyCode.NUMPAD5);
        robot.push(KeyCode.ENTER);
        delay(4);
        robot.clickOn("Close");
        delay(2);

        robot.clickOn("Lookup Product Id");
        delay(2);
        robot.clickOn("Beef Brisket Point");
        delay(2);
        String description2 = ((TextField) reflectiveGetField(controller, "descriptionField")).getText();
        assertEquals(description2, "Brisket Point", "Incorrect Descripton");
        String productId2 = ((TextField) reflectiveGetField(controller, "productCodeField")).getText();
        assertEquals(productId2, "002003", "Incorrect Descripton");

        // Overweight case
        robot.push(KeyCode.NUMPAD1);
        robot.push(KeyCode.NUMPAD0);
        robot.push(KeyCode.NUMPAD1);
        robot.push(KeyCode.ENTER);
        delay(2);
        robot.clickOn("Close");
        delay(2);
       

    }

   
    public void testManualModeButtons(FxRobot robot) {
        robot.clickOn("Manual");
        delay(2);
        robot.clickOn("Lookup Product Id");
        robot.clickOn("Beef Ground Beef");
        delay(2);
        String description = ((TextField) reflectiveGetField(controller, "descriptionField")).getText();
        assertEquals(description, "Ground Beef", "Incorrect Descripton");
        String productId = ((TextField) reflectiveGetField(controller, "productCodeField")).getText();
        assertEquals(productId, "002086", "Incorrect Descripton");

        delay(2);

        robot.clickOn("Generate");
        delay(2);
        robot.clickOn("Close");
        delay(2);

        robot.clickOn("Manual");

        robot.clickOn("Lookup Product Id");
        robot.clickOn("Beef Sirloin Tip Roast");
        delay(2);
        String description2 = ((TextField) reflectiveGetField(controller, "descriptionField")).getText();
        assertEquals(description2, "Sirloin Tip Roast", "Incorrect Descripton");
        String productId2 = ((TextField) reflectiveGetField(controller, "productCodeField")).getText();
        assertEquals(productId2, "002093", "Incorrect Descripton");
        robot.push(KeyCode.NUMPAD1);
        robot.push(KeyCode.NUMPAD2);
        robot.push(KeyCode.PERIOD);
        robot.push(KeyCode.NUMPAD3);
        robot.push(KeyCode.NUMPAD4);
        robot.push(KeyCode.NUMPAD5);
        robot.clickOn("Generate");
        delay(2);
        robot.clickOn("Print Windows Printer");
        delay(2);
        robot.press(KeyCode.SHIFT);
        robot.press(KeyCode.TAB);
        robot.release(KeyCode.TAB);
        robot.release(KeyCode.SHIFT);
        robot.push(KeyCode.ENTER);

        robot.clickOn("Print Windows Printer");
        delay(2);
        robot.push(KeyCode.ENTER);
        delay(2);

        robot.push(KeyCode.T);
        robot.push(KeyCode.E);
        robot.push(KeyCode.S);
        robot.push(KeyCode.T);
        robot.push(KeyCode.PERIOD);
        robot.push(KeyCode.P);
        robot.push(KeyCode.D);
        robot.push(KeyCode.F);
        robot.push(KeyCode.ENTER);
        robot.press(KeyCode.Y);

        delay(3);
        robot.clickOn("Print Zebra Printer");
        delay(2);

        robot.clickOn("Manual");
        robot.clickOn("Lookup Product Id");
        robot.clickOn("Cancel");
        delay(2);
       

    }

   
    public void testBatchMode(FxRobot robot) {
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
        delay(2);
        robot.clickOn("Fresh Pork");
        robot.clickOn("Print Selected Labels");
        boolean isFirstPrinted = model.productLabels.get(0).printed.get();
        assertTrue(isFirstPrinted, "Printed Lable is not checked!");
        delay(2);
      
    }
    
   
    public void testBadDefaultFolder(FxRobot robot) {
        String currentLastFolder = model.preferences.get(controller.LAST_USED_FOLDER, Path.of("spreadsheets").toFile().getAbsolutePath());
        try {
          model.preferences.put("controller.LAST_USED_FOLDER", "notReal");
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
          delay(2);
        } finally {
            model.preferences.put(controller.LAST_USED_FOLDER, currentLastFolder);
           
        }
       
        
    }

  
}