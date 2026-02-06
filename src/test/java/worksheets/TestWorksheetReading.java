package worksheets;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TreeMap;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.junit.jupiter.api.Test;

class TestWorksheetReading {

	Path worksheetsPath = Path.of("spreadsheets");

	@Test
	void test() {
		try {
			File firstworksheet = Files.list(worksheetsPath).findFirst().get().toFile();
			System.out.println("Reading " + firstworksheet.getName());
			TreeMap<Integer, ProductId> productTypeRowIndexes = new TreeMap<>();
			HSSFWorkbook wb = new HSSFWorkbook(Files.newInputStream(firstworksheet.toPath()));
			try {
				System.out.println("Workbook has " + wb.getNumberOfSheets() + " sheets");
				HSSFSheet firstSheet = wb.getSheetAt(0);
				ArrayList<Row> rows = new ArrayList<>();
				firstSheet.forEach(r -> rows.add(r));
				
				for (int i = 0; i < rows.size(); i++) {
					HSSFRow row = (HSSFRow) rows.get(i);
					HSSFCell productIdcell = row.getCell(3);
					if ((productIdcell == null) || (productIdcell.getCellType() != CellType.STRING))
						continue;

					HSSFCell productNameCell = row.getCell(12);
					if ((productNameCell == null) || (productNameCell.getCellType() != CellType.STRING))
						continue;
					
					String description = productNameCell.getStringCellValue();
					try {
						Integer.parseInt(productIdcell.getStringCellValue());
					} catch (NumberFormatException nfe) {
						continue;
					}
					int productId = Integer.parseInt(productIdcell.getStringCellValue());
					productTypeRowIndexes.put(i, new ProductId(productId, description));
				}
			} finally {
				wb.close();
			}
		} catch (Throwable e) {
			fail(e.getMessage());
			e.printStackTrace();
		}

	}
	
	

}
