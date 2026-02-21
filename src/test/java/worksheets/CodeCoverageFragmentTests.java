package worksheets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static worksheets.Util.delDirTree;
import static worksheets.Util.delay;
import static worksheets.Util.reflectiveGetField;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.print.PrintService;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.framework.junit5.Stop;

import app.Barcode;
import app.InventoryReport;
import app.MainApp;
import app.Model;
import app.Printer;
import app.ProductGroup;
import app.ProductId;
import app.ProductLabel;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class CodeCoverageFragmentTests extends MainApp {

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
    public void testSpecialCasesForCodeCoverage() throws FileNotFoundException, IOException {
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

        Printer printer2 = new Printer("Zebra");
        expected = null;
        try {
            printer2.print(null, null, null, null);
        } catch (Exception e) {
            expected = e;
        }
        assertTrue(expected != null, "Did not fail on imaginary printer!");
       
        
        
        expected = null;
        try {
            Barcode tobigbarcode = new Barcode("1.0", "00123456");
            tobigbarcode.content();
        } catch (Exception e) {
            expected = e;
        }
        assertTrue(expected == null, "failed on tobig product id rather than truncating to 6 digits!");
          
        expected = null;
        try {
            Barcode minusbarcode = new Barcode("-11.0", "00123456");
            minusbarcode.content();
        } catch (Exception e) {
            expected = e;
        }
        assertTrue(expected != null, "Did not fail on negative wt!");
      
        expected = null;
        try {
            Barcode minusidbarcode = new Barcode("11.0", "-12345");
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
        assertTrue(badLabel.getPrinted(),"setting printer boolean on label failes");
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

        TreeMap<Integer, ProductId> testMap = new TreeMap<>();
        testMap.put(1, new ProductId(1, ProductGroup.Beef, "sticks"));
        List<List<String>> emptyList = new ArrayList<>();

        InventoryReport report = new InventoryReport(new File("spreadsheets/test.xls"));
   
        expected = null;
        try {
            ArrayList<ProductLabel> labels = report.getProductLabels(testMap, emptyList);
        } catch (Exception e) {
            expected = e;
        }
        assertTrue(expected != null, "Did not fail on Inventory report size mismatch!");
        try {
        InventoryReport report2 = new InventoryReport(new File("spreadsheets/test2.xls"));
        } catch (Exception e) {
        	expected = e;
        }
        assertTrue(expected != null);
        
		try {
			InventoryReport report3 = new InventoryReport(new File("./spreadsheets/Test2.xls"));
		} catch (RuntimeException e) {
			expected = e;
		}
		assertTrue(expected != null);
		
		expected = null;
		try {
			InventoryReport report4 = new InventoryReport(new File("./spreadsheets/Test3.xls"));
		} catch (RuntimeException e) {
			expected = e;
		}
		assertTrue(expected != null);
		
		expected = null;
		try {
			InventoryReport report5 = new InventoryReport(new File("./spreadsheets/Test4.xls"));
		} catch (RuntimeException e) {
			expected = e;
		}
		assertTrue(expected != null);
		
		expected = null;
		try {
			InventoryReport report6 = new InventoryReport(new File("./spreadsheets/Test5.xls"));
		} catch (RuntimeException e) {
			expected = e;
		}
		assertTrue(expected == null);
	
		
    }

   
    
    
}
