package worksheets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import app.InventoryReport;
import app.Model;

class TestWorksheetReading {

	static Path worksheetsPath = Path.of("spreadsheets");

	
	public static void main(String[] args) {
		try {
			File firstworksheet = Files.list(worksheetsPath)
					.sorted()
					.findFirst()
					.get()
					.toFile();
			System.out.println("Reading " + firstworksheet.getName());
			
			Model model = Model.getInstance();
			InventoryReport inventoryReport = new InventoryReport(firstworksheet);
			model.productLabels.setAll(inventoryReport.productLabels);
			List productLabels = model.productLabels;
			assertTrue(productLabels.size()>1);
		} catch (Throwable e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	

}
