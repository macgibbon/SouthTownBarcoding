package app;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public class MainController {

	private static final Object[] EMPTYARGS = new Object[0];

	@FXML
	private Label messageLabel;

	@FXML
	private Label contentLabel;

	@FXML
	private TextField barcodeField;

	@FXML
	private ImageView barcodeView;

	@FXML
	private TextField gs1WeightField;

	@FXML
	private TextField productCodeField;

	@FXML
	private TableView<ProductLabel> tableView;
	
	@FXML
	private TabPane tabpane;

	private Model model;

	private FileChooser fileChooser;
	
	private static final String PRINTER_NAME = "Zebra"; // <- change to part or full name of your printer

	@FXML
	private void initialize() {
		model = Model.getInstance();
		tableView.setItems(model.productLabels);
		List<TableColumn<ProductLabel, String>> tcList = Stream.of(ProductLabel.class.getRecordComponents())
				.map(rc -> rc.getName()).map(name -> new TableColumn<ProductLabel, String>(name))
				.collect(Collectors.toList());
		for (int i = 0; i < tcList.size(); i++) {
			TableColumn<ProductLabel, String> aTableColumn = tcList.get(i);
			final int col = i;
			aTableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
			aTableColumn.setPrefWidth(200.0);
			aTableColumn.setCellValueFactory(cellData -> {
				
				try {
					Method accessor = ProductLabel.class.getRecordComponents()[col].getAccessor();
					String cellValue = accessor.invoke(cellData.getValue(), EMPTYARGS).toString();
					return new ReadOnlyStringWrapper(cellValue);
				} catch (Throwable e) {
					return new ReadOnlyStringWrapper(e.getMessage());
				}
			});
		}
		tableView.getColumns().setAll(FXCollections.observableList(tcList));
	}

	@FXML
	private void onGenerateClicked(ActionEvent event) {
		try {
			String content = getBarcodeContent();
			handleUPCEmbedded(content);
		} catch (WriterException we) {
			messageLabel.setText("Error generating barcode: " + we.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			messageLabel.setText("Unexpected error: " + ex.getMessage());
		}
	}

	@FXML
	private void onPrintWindowsClicked(ActionEvent event) {
		try {
			String content = getBarcodeContent();
			handleUPCEmbedded(content);
		} catch (WriterException we) {		
			messageLabel.setText("Error generating barcode: " + we.getMessage());
		} catch (Exception ex) {
			messageLabel.setText("Unexpected error: " + ex.getMessage());
		}
		Image image = barcodeView.getImage();
		if (image == null) {
			messageLabel.setText("No barcode to print. Generate one first.");
			return;
		}

		PrinterJob job = PrinterJob.createPrinterJob();
		if (job == null) {
			messageLabel.setText("No printer job available.");
			return;
		}

		// Show print dialog attached to the current window
		Window window = messageLabel.getScene().getWindow();
		boolean proceed = job.showPrintDialog(window);
		if (!proceed) {
			messageLabel.setText("Print cancelled.");
			return;
		}

		// Print the image node (ImageView)
		boolean printed = job.printPage(barcodeView);
		if (printed) {
			job.endJob();
			messageLabel.setText("Printed successfully.");
		} else {
			messageLabel.setText("Printing failed.");
		}
	}

	@FXML
	private void openReport(ActionEvent event) throws FileNotFoundException, IOException {
		File lastUsedDirectory = Path.of("spreadsheets").toFile();
		fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(lastUsedDirectory);
		// Show the save file dialog
		File file = fileChooser.showOpenDialog((Stage) tableView.getScene().getWindow());
		if (file != null) {
			model.setWorksheetForLabels(file);
		}
		tabpane.getSelectionModel().select(1);
	}	

	@FXML
	private void onPrintZebraClicked(ActionEvent event) {
		String content = getBarcodeContent();
		String weightStr = gs1WeightField.getText();
		String productStr = productCodeField.getText();
		String zpl = formatString(75, "Unknown", "Product " + productStr, weightStr + " lb", content);
		PrintService ps = findPrintService(PRINTER_NAME);
		if (ps == null) {
			System.err.println("Printer matching '" + PRINTER_NAME + "' not found.");
			System.err.println("Available printers:");
			for (PrintService p : PrintServiceLookup.lookupPrintServices(null, null)) {
				System.err.println("  - " + p.getName());
			}
			return;
		}
		DocPrintJob job = ps.createPrintJob();
		byte[] bytes = zpl.getBytes(StandardCharsets.UTF_8);
		Doc doc = new SimpleDoc(bytes, DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
		PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
		attrs.add(new Copies(1));
		try {
			job.print(doc, attrs);
			System.out.println("ZPL sent to printer: " + ps.getName());
		} catch (PrintException e) {
			System.err.println("Print failed: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void handleUPCEmbedded(String content) throws WriterException {
		try {
			generateAndShowBarcode(content, BarcodeFormat.UPC_A, 360, 120);
			messageLabel.setText("UPC-A (weight) generated: " + content);
			contentLabel.setText(content);
		} catch (IllegalArgumentException iae) {
			contentLabel.setText("");
			messageLabel.setText(iae.getMessage());
		}
	}

	private String getBarcodeContent() {
		String weightStr = gs1WeightField.getText();
		String productStr = productCodeField.getText();
		return getBarCodeContent(weightStr, productStr);
	}

	private String getBarCodeContent(String weightStr, String productStr) {
		BigDecimal weight = null;
		try {
			weight = new BigDecimal(weightStr);
			if (weight.signum() < 0) {
				throw new IllegalArgumentException("Weight must be non-negative.");
			}

		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("Invalid weight format.");
		}
		// Multiply by 10^decimals and round to integer (should be exact for typical
		// inputs)
		BigDecimal scaled = weight.movePointRight(3).setScale(0, RoundingMode.HALF_UP);
		String formattedWeight = String.format("%05d", scaled.toBigInteger());

		BigDecimal productCode = null;
		try {
			productCode = new BigDecimal(productStr);
			if (productCode.signum() <= 0) {
				throw new IllegalArgumentException("Product code must a positive integer. ");
			}

		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("Invalid product code format.");
		}

		String formattedProductCode = String.format("%06d", productCode.toBigInteger());
		// Build 11-digit payload: ns + manu(5) + weight(5)
		String content = formattedProductCode + formattedWeight; // length should be 11
		if (content.length() != 11 || !content.matches("\\d{11}")) {
			throw new IllegalArgumentException("Constructed UPC-A payload invalid: " + content);
		}
		return content;
	}

	// Helper to generate barcode using ZXing and set it into the ImageView
	private void generateAndShowBarcode(String text, com.google.zxing.BarcodeFormat format, int width, int height)
			throws WriterException {
		Map<EncodeHintType, Object> hints = new HashMap<>();
		// minimal margin
		hints.put(EncodeHintType.MARGIN, 1);
		BitMatrix bitMatrix = new MultiFormatWriter().encode(text, format, width, height, hints);
		BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
		Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
		barcodeView.setImage(fxImage);
	}

	private String formatString(int xoffset, String group, String productCode, String weight, String upcCode) {
		String zpl = "^XA\n" // use default font
				+ "^CF,25\n" // set default font 0 to 30 dots high
				+ "^FO" + Integer.toString(xoffset) + ",30,0" // field origin x,y, "
				+ "^FD" + group // string to print
				+ "^FS" + "^FO" + Integer.toString(xoffset) + ",70,0" // field origin x,y, "// end of field
				+ "^FD" + productCode // string to print
				+ "^FS\n" // end of field
				+ "^BY3,2,150\n" // barcode module width, wide bar ratio, barcode height
				+ "^FO" + Integer.toString(80) + ",120,2" // barcode field origin x,y, "
				+ "^BUN,120,Y,N,N,N\n" // barcode UPC-A, height 100, print interpretation line, print above, no check
										// digit
				+ "^FD" + upcCode // barcode data + end of field
				+ "^FS\n" + "^FO" + Integer.toString(xoffset) + ",280,0" // field origin x,y, "// end of field
				+ "^FD" + weight // string to print
				+ "^XZ\n";
		// end of label format
		return zpl;
	}

	private PrintService findPrintService(String namePart) {
		PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
		for (PrintService s : services) {
			if (s.getName().toLowerCase().contains(namePart.toLowerCase())) {
				return s;
			}
		}
		return null;
	}
}