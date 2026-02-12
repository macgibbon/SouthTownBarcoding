package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Model {

	private static final int DESCRIPTIONCOLUMN = 12;
	private static final int IDCOLUMN = 3;
	private static final int SERIALCOLUMN = 2;
	private static final int WEIGHTCOLUMN = 10;

	private static final Model instance = new Model();

	public static Model getInstance() {
		return instance;
	}

	public ObservableList<ProductLabel> productLabels = FXCollections.observableArrayList();

	public File appDir;
	public File currentDefaults;
	public Preferences preferences;

	private Model() {
		super();
		loadDefaults();
	}

	private ArrayList<Row> getFirstWorksheetRows(HSSFWorkbook wb) {
		System.out.println("Workbook has " + wb.getNumberOfSheets() + " sheets");
		HSSFSheet firstSheet = wb.getSheetAt(0);
		ArrayList<Row> worksheetRows = new ArrayList<>();
		firstSheet.forEach(r -> worksheetRows.add(r));
		return worksheetRows;
	}

	private TreeMap<Integer, ProductId> getProductHeaderRowPositions(List<Row> worksheetRows) {
		TreeMap<Integer, ProductId> productTypeRowIndexes = new TreeMap<>();
		// get list of rows with product id and description header
		for (int i = 0; i < worksheetRows.size(); i++) {
			HSSFRow row = (HSSFRow) worksheetRows.get(i);
			HSSFCell productIdcell = row.getCell(IDCOLUMN);
			HSSFCell productNameCell = row.getCell(DESCRIPTIONCOLUMN);
			if (isProductIdRow(productIdcell, productNameCell)) {
				String description = productNameCell.getStringCellValue();
				int productId = Integer.parseInt(productIdcell.getStringCellValue());
				productTypeRowIndexes.put(i, ProductId.createProductId(productId, description));
			}
		}
		return productTypeRowIndexes;
	}

	private ArrayList<ProductLabel> getProductLabels(TreeMap<Integer, ProductId> productHeaderPositions,
			List<List<String>> productWeights) {
		if (productHeaderPositions.size() != productWeights.size())
			throw new RuntimeException("Mismatch in product header positions and product positions");
		ArrayList<ProductLabel> productLabels = new ArrayList<ProductLabel>();
		ProductId[] productIds = productHeaderPositions.values().toArray(new ProductId[0]);
		for (int i = 0; i < productWeights.size(); i++) {
			ProductId productId = productIds[i];
			ProductGroup productGroup = productIds[i].productGroup();
			String description = productIds[i].description();
			List<String> productWeightList = productWeights.get(i);
			for (String weight : productWeightList) {
				ProductLabel productLabel = new ProductLabel(productGroup, productId.id().toString(),
						description.trim(), weight);
				productLabels.add(productLabel);
			}
		}
		return productLabels;
	}

	private List<String> getProductRows(List<Row> rawRows) {
		DecimalFormat df = new DecimalFormat("#.00");
		List<String> productRows = rawRows.stream()
				.filter(rawRow -> isProductWeightRow((HSSFCell) rawRow.getCell(SERIALCOLUMN),
						(HSSFCell) rawRow.getCell(WEIGHTCOLUMN)))
				.map(rawRow -> (HSSFCell) rawRow.getCell(WEIGHTCOLUMN))
				.map(cell -> df.format(cell.getNumericCellValue())).toList();
		return productRows;
	}

	private ArrayList<ProductInterval> getProductWeightRowIntervals(
			TreeMap<Integer, ProductId> productHeaderPositions) {
		List<Integer> rowPositions = productHeaderPositions.keySet().stream().toList();
		ArrayList<ProductInterval> productIntervals = new ArrayList<>();
		for (int i = 0; i < rowPositions.size() - 1; i++) {
			ProductInterval productInterval = new ProductInterval(rowPositions.get(i), rowPositions.get(i + 1));
			productIntervals.add(productInterval);
		}
		return productIntervals;
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

	boolean isProductWeightRow(HSSFCell serialCell, HSSFCell weightCell) {

		if ((serialCell == null) || (serialCell.getCellType() != CellType.STRING))
			return false;

		if ((weightCell == null) || (weightCell.getCellType() != CellType.NUMERIC))
			return false;

		try {
			Integer.parseInt(serialCell.getStringCellValue());
		} catch (NumberFormatException nfe) {
			return false;
		}

		try {
			Double.parseDouble(serialCell.getStringCellValue());
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public void setWorksheetForLabels(File firstworksheet) throws IOException, FileNotFoundException {
		HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(firstworksheet));
		try {
			final List<Row> worksheetRows = getFirstWorksheetRows(wb);
			TreeMap<Integer, ProductId> productHeaderPositions = getProductHeaderRowPositions(worksheetRows);
			List<ProductInterval> productIntervals = getProductWeightRowIntervals(productHeaderPositions);
			// special case for last product interval terminated by last worksheet row
			if (!productIntervals.isEmpty())
				productIntervals.add(new ProductInterval(productIntervals.getLast().endExclusive(), worksheetRows.size()));
			List<List<String>> productWeights = productIntervals.stream()
					.map(pi -> getProductRows(worksheetRows.subList(pi.beginInclusive(), pi.endExclusive())))
					.toList();

			productLabels.setAll(getProductLabels(productHeaderPositions, productWeights));
			System.out.println("done");
		} finally {
			wb.close();
		}
	}

	public static void copy(Path sourcePath, Path targetPath, Path source) {
		Path target = targetPath.resolve(sourcePath.relativize(source));
		if (!(target.toFile().exists())) {
			try {
				Files.copy(source, target);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void deepCopy(Path sourcePath, Path targetPath) {
		try (Stream<Path> stream = Files.walk(sourcePath)) {
			stream.forEach(source -> copy(sourcePath, targetPath, source));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void loadDefaults() {
		File userHome = new File(System.getProperty("user.home"));
		System.out.println("User home =" + userHome.getAbsolutePath());
		File userDir = new File(System.getProperty("user.dir"));
		System.out.println("User dir =" + userDir.getAbsolutePath());
		File javaDir = new File(System.getProperty("java.home"));
		System.out.println("Java home =" + javaDir.getAbsolutePath());
		appDir = new File(userHome, ".barcoder");
		if (!appDir.exists()) {
			appDir.mkdirs();
		}
		currentDefaults = new File(appDir, "currentDefaults");
		currentDefaults.mkdirs();
		Path defaultPath = new File(javaDir,"defaults").toPath();
		if (defaultPath.toFile().exists()) {
			deepCopy(defaultPath, currentDefaults.toPath());
		}
		else {
			Path altPath = Path.of("./defaults");
			if (altPath.toFile().exists()) {
				deepCopy(altPath, currentDefaults.toPath());
			}
		}
		preferences = Preferences.userNodeForPackage(getClass());
		
			
	}
}
