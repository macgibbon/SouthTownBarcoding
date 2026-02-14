package worksheets;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import app.InventoryReport;
import app.Model;
import app.ProductId;
import app.ProductLabel;

class MapProductIds {

	Path worksheetsPath = Path.of("spreadsheets");

	@Test
	void test() {
		try {
			
			List<ProductId> allproductIds = Files.list(worksheetsPath)
				.map(path -> path.toFile())
				.map(file -> {			
					List<ProductLabel> labels = loadFileLables(file);
				
					List<ProductId> productIds = labels.stream()
					.map(label -> new ProductId(Integer.valueOf(label.productId()), label.group(), label.description()))
					.distinct()
					.toList();	
				return productIds;				
			})
			.flatMap( list -> list.stream())
			.distinct()
			.sorted((pid1, pid2) -> Integer.compare(pid1.id(), pid2.id()))
			.toList();		

			System.out.println(allproductIds);
			File dir =worksheetsPath.toFile().getParentFile();
			File productIds = new File(dir, "defaultProducts.csv");
			
			try (PrintWriter writer = new PrintWriter(productIds)) {
				writer.println("Product Id,Product Group,Description");
				for (ProductId productId : allproductIds) {
					writer.print('"');
					writer.print(productId.id());
					writer.print('"');
					writer.print(',');
					writer.print('"');
					writer.print(productId.productGroup());
					writer.print('"');
					writer.print(',');
					writer.print('"');
					writer.print(productId.description());
					writer.print('"');
					writer.println();
				}			
			}		
		} catch (Throwable e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	private List<ProductLabel> loadFileLables(File file) {
		Model model = Model.getInstance();
		model.productLabels.clear();
		try {
			InventoryReport inventoryReport = new InventoryReport(file);
			model.productLabels.setAll(inventoryReport.productLabels);
		} catch (Throwable e) {			
			e.printStackTrace();
		} 
		List<ProductLabel> labels = model.productLabels;
		return labels;
	}

	
	
	

}
