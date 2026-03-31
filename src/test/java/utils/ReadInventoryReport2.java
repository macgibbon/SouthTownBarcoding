package utils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import app.InventoryReport;
import app.Model;

class ReadInventoryReport2 {

	static Path worksheetsPath = Path.of("spreadsheets");

	
	public static void main(String[] args) {
		try {
			File firstworksheet = Files.list(worksheetsPath)
					.sorted()
					.findFirst()
					.get()
					.toFile();
			firstworksheet = new File(worksheetsPath.toFile(),"south town 1.xls");
			System.out.println("Reading " + firstworksheet.getName());
			
			Model model = Model.getInstance();
			InventoryReport inventoryReport = new InventoryReport(firstworksheet);
			model.productLabels.setAll(inventoryReport.productLabels);
			var productLabels = model.productLabels;
			assertTrue(productLabels.size()>1);
		} catch (Throwable e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	

}
