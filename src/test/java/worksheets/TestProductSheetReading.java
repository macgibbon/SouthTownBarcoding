package worksheets;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import app.MainController;
import app.ProductId;

class TestProductSheetReading {

	static Path worksheetsPath = Path.of("spreadsheets");

	static Path defaultsPath = Path.of("defaults");

    private static Map<Integer, ProductId> productMap;
	
	public static void main(String[] args) {
		try {
		    File currentDefaults = new File(defaultsPath.toFile(), "defaultProducts.csv"); 
		    productMap = MainController.loadDefaultProductFiles(currentDefaults);		    
		    
		    File manualProductSheet = new File(worksheetsPath.toFile(), "Meat Product Numbers.csv");		   
			List<String> listedProducts = Files.readAllLines(manualProductSheet.toPath());
			listedProducts.stream()
			    .skip(1)
	            .map(row -> row.split(","))
	            .filter(rowStrings -> isProductRow(rowStrings) )
	            .filter(rowStrings -> isMissing(rowStrings))
	            .forEach(rowStrings -> System.out.println(rowStrings[0] + " " + rowStrings[1]));
			
		} catch (Throwable e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}


    private static boolean isMissing(String[] rowStrings) {
       int pid = Integer.parseInt(rowStrings[0]);
       ProductId productId = productMap.get(pid);
       if (productId == null)
           return true;
       else
           return false;
    }


    private static boolean isProductRow(String[] rowStrings) {
        // TODO Auto-generated method stub
        if (rowStrings.length != 2)
            return false;
        if ((rowStrings[0] == null) || (rowStrings[1] == null))
            return false;
        if ((rowStrings[0].length() == 0) || (rowStrings[1].length() == 0))
            return false;
        return true;
    }



	

}
