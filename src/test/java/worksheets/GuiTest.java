
package worksheets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static worksheets.Util.delDirTree;
import static worksheets.Util.delay;
import static worksheets.Util.reflectiveGetField;

import java.io.File;
import java.nio.file.Path;

import javax.print.PrintService;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.framework.junit5.Stop;

import app.Barcode;
import app.MainApp;
import app.Model;
import app.Printer;
import app.ProductGroup;
import app.ProductId;
import app.ProductLabel;
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
        testBatchModeSingleProduct(robot);
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

        Printer printer = new Printer("NotPresentPrinter");
        Exception expected = null;
        try {
            PrintService printService = printer.findPrintService();
        } catch (Exception e) {
            expected = e;
        }
        assertTrue(expected != null, "Did not fail on imaginary printer!");

        Barcode tobigbarcode = new Barcode("1.0", "00123456");
        Barcode minusbarcode = new Barcode("-11.0", "00123456");
        expected = null;
        try {
            minusbarcode.content();
        } catch (Exception e) {
            expected = e;
        }
        assertTrue(expected != null, "Did not fail on negative wt!");
        Barcode minusidbarcode = new Barcode("11.0", "-12345");
        expected = null;
        try {
            minusidbarcode.content();
        } catch (Exception e) {
            expected = e;
        }
        assertTrue(expected != null, "Did not fail on negative product id!");

        Barcode tobigwtbarcode = new Barcode("100.0", "00123456");
        expected = null;
        try {
            tobigwtbarcode.content();
        } catch (Exception e) {
            expected = e;
        }
        assertTrue(expected != null, "Did not fail on too big product id!");
        
        Barcode badIdcode = new Barcode("1.0", "ABCD");
        expected = null;
        try {
            badIdcode.content();
        } catch (Exception e) {
            expected = e;
        }
        assertTrue(expected != null, "Did not fail on bad product id!");

        ProductLabel badLabel = new ProductLabel(ProductGroup.Beef, "000001", "meat", "1.0", false);
        String w = badLabel.getWeight();
        badLabel.setPrinted(true);
        boolean printed = badLabel.getPrinted();
        assertTrue(printed);
        badLabel.setPrinted(true);

        File userHome = new File(System.getProperty("user.home"));

        File appDir = new File(userHome, ".barcoder");

        expected = null;
        try {
            File currentDefaults = new File(appDir, "currentDefaults");
            MainApp.deepCopy(currentDefaults.toPath(), Path.of("Z:/"));
        } catch (Exception e) {
            expected = e;
        }
        assertTrue(expected != null, "Did not fail on imaginary path!");

        ProductId id = ProductId.createProductId(1, "chicken tenders");
        
        
    }

    public void testManualModeKeypadEntry(FxRobot robot) {
        robot.clickOn("Manual");

        robot.clickOn("Lookup Pork Product Id");
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

        robot.clickOn("Lookup Beef Product Id");
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
        robot.clickOn("Lookup Beef Product Id");
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

        robot.clickOn("Lookup Pork Product Id");
        robot.clickOn("Fresh Pork Ground");
        delay(2);
        String description2 = ((TextField) reflectiveGetField(controller, "descriptionField")).getText();
        assertEquals(description2, "Ground", "Incorrect Descripton");
        String productId2 = ((TextField) reflectiveGetField(controller, "productCodeField")).getText();
        assertEquals(productId2, "005041", "Incorrect Descripton");
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
      
        
        String currentLastFolder = model.preferences.get(controller.LAST_USED_FOLDER, Path.of("spreadsheets").toFile().getAbsolutePath());
        File testPrintFile = new File(currentLastFolder + "/test.pdf");
        if (testPrintFile.exists())
            testPrintFile.delete();

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
        delay(2);
 

        delay(3);
        robot.clickOn("Print Zebra Printer");
        delay(2);

        robot.clickOn("Manual");
        robot.clickOn("Lookup Beef Product Id");
        robot.clickOn("Cancel");
        delay(2);
        robot.clickOn("Manual");
        delay(2);
        robot.clickOn("File");
        robot.clickOn("Open Inventory Report for 1 Beef Product");
        delay(2);
        robot.clickOn("Cancel");

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
        robot.clickOn("Print Selected");
        boolean isFirstPrinted = model.productLabels.get(0).printed.get();
        assertTrue(isFirstPrinted, "Printed Lable is not checked!");
        robot.clickOn("Print Next 20 Unprinted");
        robot.clickOn("Print Next 40 Unprinted");
        delay(2);
    }

    
    private void testBatchModeSingleProduct(FxRobot robot) {
        delay(2);
        robot.clickOn("File");
        robot.clickOn("Open Inventory Report for 1 Beef Product");        
        delay(2);
        robot.clickOn("Beef Chuck Eye Steak");
        robot.push(KeyCode.T);
        robot.push(KeyCode.E);
        robot.push(KeyCode.S);
        robot.push(KeyCode.T);
        robot.push(KeyCode.DIGIT1);
        robot.push(KeyCode.PERIOD);
        robot.push(KeyCode.X);
        robot.push(KeyCode.L);
        robot.push(KeyCode.S);
        robot.push(KeyCode.ENTER);
        delay(2);
        robot.clickOn("Chuck Eye Steak");
        robot.clickOn("Print All Unprinted");
        boolean isFirstPrinted = model.productLabels.get(0).printed.get();
        assertTrue(isFirstPrinted, "Printed Lable is not checked!");
        delay(2);// TODO Auto-generated method stub
        robot.clickOn("Print All Unprinted");  // second print to force filter through all paths
    }

    public void testBadDefaultFolder(FxRobot robot) {
        String currentLastFolder = model.preferences.get(controller.LAST_USED_FOLDER, Path.of("spreadsheets").toFile().getAbsolutePath());
        try {
            model.preferences.put(controller.LAST_USED_FOLDER, "notReal");
            delay(2);
            robot.clickOn("File");
            robot.clickOn("Open Inventory Report for 1 Pork Product");
            robot.clickOn("Fresh Pork Ground");
            robot.push(KeyCode.TAB);
            robot.push(KeyCode.TAB);
            robot.push(KeyCode.ENTER);
            delay(2);
        } finally {
            model.preferences.put(controller.LAST_USED_FOLDER, currentLastFolder);
        }
        
        currentLastFolder = model.preferences.get(controller.LAST_USED_FOLDER, Path.of("spreadsheets").toFile().getAbsolutePath());
        try {
            model.preferences.put(controller.LAST_USED_FOLDER, "notReal");
            delay(2);
            robot.clickOn("File");
            robot.clickOn("Open Inventory Report");
            robot.push(KeyCode.TAB);
            robot.push(KeyCode.TAB);
            robot.push(KeyCode.ENTER);
            delay(2);
        } finally {
            model.preferences.put(controller.LAST_USED_FOLDER, currentLastFolder);
        }
    }

}