package worksheets;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.junit.jupiter.api.Test;

class TestWorksheetReading {

	private static final int DESCRIPTIONCOLUMN = 12;
	private static final int IDCOLUMN = 3;
	Path worksheetsPath = Path.of("spreadsheets");

	@Test
	void test() {
		try {
			File firstworksheet = Files.list(worksheetsPath).findFirst().get().toFile();
			System.out.println("Reading " + firstworksheet.getName());
			TreeMap<Integer, ProductId> productTypeRowIndexes = new TreeMap<>();
			ArrayList<Row> rows = new ArrayList<>();
			ArrayList<ProductInterval> productIntervals = new ArrayList<>();

			HSSFWorkbook wb = new HSSFWorkbook(Files.newInputStream(firstworksheet.toPath()));
			try {
				System.out.println("Workbook has " + wb.getNumberOfSheets() + " sheets");
				HSSFSheet firstSheet = wb.getSheetAt(0);
				
				firstSheet.forEach(r -> rows.add(r));

				for (int i = 0; i < rows.size(); i++) {
					HSSFRow row = (HSSFRow) rows.get(i);
					HSSFCell productIdcell = row.getCell(IDCOLUMN);
					HSSFCell productNameCell = row.getCell(DESCRIPTIONCOLUMN);
					if (isProductIdRow(productIdcell, productNameCell)) {
						String description = productNameCell.getStringCellValue();
						int productId = Integer.parseInt(productIdcell.getStringCellValue());
						productTypeRowIndexes.put(i, new ProductId(productId, description));
					}
				}
				List<Integer> rowPositions = productTypeRowIndexes.keySet().stream().toList();
				for (int i = 0; i < rowPositions.size()-1; i++) {
					ProductInterval productInterval = new ProductInterval(rowPositions.get(i), rowPositions.get(i+1));
					productIntervals.add(productInterval);
				}
				productIntervals.add(new ProductInterval(rowPositions.get(rowPositions.size()-1), rows.size()-1));
				
			} finally {
				wb.close();
			}
		} catch (Throwable e) {
			fail(e.getMessage());
			e.printStackTrace();
		}

	}

	boolean isProductIdRow(HSSFCell productIdcell, HSSFCell productNameCell) {

		if ((productIdcell == null) || (productIdcell.getCellType() != CellType.STRING))
			return false;

		if ((productNameCell == null) || (productNameCell.getCellType() != CellType.STRING))
			return false;

		try {
			Integer.parseInt(productIdcell.getStringCellValue());
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

}
