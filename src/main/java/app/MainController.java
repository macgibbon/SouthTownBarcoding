package app;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
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

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public class MainController {

	private static final Object[] EMPTYARGS = new Object[0];

	public static final String LAST_USED_FOLDER = "lastUsedFolder";

	@FXML
	private Label messageLabel;

	@FXML
	private Label contentLabel;

	@FXML
	private Label spreadsheetNameLabel;

	@FXML
	private TextField barcodeField;

	@FXML
	private ImageView barcodeView;

	@FXML
	private TextField weightField;

	@FXML
	private TextField productCodeField;
	
	@FXML
	private TextField descriptionField;
	
	@FXML
	private TextField groupField;

	@FXML
	private TableView<ProductLabel> tableView;

	@FXML
	private TabPane tabpane;

	@FXML
	private Button printbutton;

	private Model model;

	private FileChooser fileChooser;

	private static final String PRINTER_NAME = "Zebra"; // <- change to part or full name of your printer
	public static final String COMMA_DELIMITER = ",";
	
	TreeMap<Integer,ProductId> productIdMap;

	@FXML
	private void initialize() {
		model = Model.getInstance();

		tableView.setItems(model.productLabels);

		tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		List<TableColumn<ProductLabel, String>> tcList = Stream.of(ProductLabel.class.getRecordComponents())
				.map(rc -> rc.getName()).map(name -> capitalizeFirstLetter(name))
				.map(name -> new TableColumn<ProductLabel, String>(name)).collect(Collectors.toList());
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
		printbutton.disableProperty().bind(Bindings.isEmpty(tableView.getSelectionModel().getSelectedItems()));

		File defaultProductsFile = new File(model.currentDefaults, "defaultProducts.csv");
		productIdMap = loadDefaultProductFiles(defaultProductsFile);
	}

	private TreeMap<Integer, ProductId> loadDefaultProductFiles(File defaultProductsFile) {
		TreeMap<Integer, ProductId> productMap = new TreeMap<>();
		if (defaultProductsFile.exists()) {
			try {
				try (BufferedReader br = new BufferedReader(new FileReader(defaultProductsFile))) {
					String headers = br.readLine();
					String line;
					while ((line = br.readLine()) != null) {
						String dequotedLine = line.replaceAll("['\"]", "");
						String[] values = dequotedLine.split(COMMA_DELIMITER);
						int id = Integer.parseUnsignedInt(values[0]);
						String group = values[1];
						String description = values[2];
						String fulltext = group + " " + description;
						ProductId pid = ProductId.createProductId(id, fulltext);
						productMap.put(id, pid);
					}
				}
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}
		return productMap;
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
	private void onLookupClicked(ActionEvent event) {
		Dialog<ProductId> dialog = new Dialog<>();
		dialog.setTitle("Product Id Dialog");
		dialog.setHeaderText("Press the appropriate button.");

		// 2. Set the button types (OK and Cancel)
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);

		// 3. Create the custom layout for the content
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		int cols = 4;
		Integer[] productIds = productIdMap.keySet().toArray(new Integer[0]);
		
		for (int i= 0; i<productIds.length; i++) {
			Integer pid = productIds[i];
			ProductId productId = productIdMap.get(pid);
			String text = productId.productGroup() + " " +  productId.description();
			Button button = new Button(text);
			button.setUserData(productId);
			grid.add(button, i % cols, i / cols);
		}

		// 4. Add the layout to the dialog pane
		dialog.getDialogPane().setContent(grid);

		// 5. Focus the username field by default
//		Platform.runLater(() -> username.requestFocus());
		
		EventHandler<ActionEvent> buttonFilter = new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent args) {
		    	Button button = (Button) args.getTarget();
		    	ProductId pid = (ProductId) button.getUserData();
		    	if (pid != null) {		    
		    		args.consume();
		    		dialog.setResult(pid);
		    		dialog.close();
		    	}
		     }
		};
		
		dialog.getDialogPane().addEventFilter(ActionEvent.ACTION, buttonFilter);
		weightField.setOnKeyPressed(keyevent -> {
			if (keyevent.getCode() == KeyCode.ENTER) {
				boolean isValid = false;
				double w = 0.0;
				try {
					w = Double.parseDouble(weightField.getText());
					isValid = ((w > 0.0) && (w < 100.0));
					if (isValid) {
						String barcode = getBarCodeContent(weightField.getText(), productCodeField.getText());
						String group =groupField.getText();
						String description = descriptionField.getText();
						String weight = weightField.getText();
						Platform.runLater(()-> weightField.requestFocus());
						Platform.runLater(()-> weightField.clear());
						Platform.runLater(() -> printZebra(barcode, group, weight + " lb", description));
					}
				} catch (Throwable t) {
				}
			}
		});
//
		// 6. Convert the result to a Pair when the login button is clicked
//		dialog.setResultConverter(dialogButton -> {
//				return null;
//		});
//
		// 7. Show the dialog and handle the result
		Optional<ProductId> result = dialog.showAndWait();
		result.ifPresent(pid -> {
			String pidStr = String.format("%06d", pid.id());
			productCodeField.setText(pidStr);		
			groupField.setText(pid.productGroup().toString());
			descriptionField.setText(pid.description());
			Platform.runLater(()-> weightField.requestFocus());
			Platform.runLater(()-> weightField.clear());
		});
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
		File lastUsedDirectory = new File(
				model.preferences.get(LAST_USED_FOLDER, Path.of("spreadsheets").toFile().getAbsolutePath()));
		fileChooser = new FileChooser();
		if (lastUsedDirectory.exists())
			fileChooser.setInitialDirectory(lastUsedDirectory);
		// Show the save file dialog
		File file = fileChooser.showOpenDialog((Stage) tableView.getScene().getWindow());
		if (file != null) {
			model.preferences.put(LAST_USED_FOLDER, file.getParent());
			model.setWorksheetForLabels(file);
			spreadsheetNameLabel.setText(file.getAbsolutePath());
		}
		tabpane.getSelectionModel().select(1);
		tableView.getSelectionModel().selectAll();
		Platform.runLater(() -> printbutton.requestFocus());
	}

	@FXML
	private void printSelected(ActionEvent event) {
		ObservableList<ProductLabel> selectedLabels = tableView.getSelectionModel().getSelectedItems();
		selectedLabels.stream()
				// .limit(2l)
				.forEach(label -> {
					String barcode = getBarCodeContent(label.weight(), label.productId());
					try {
						printZebra(barcode, label.group().toString(), label.weight() + " lb", label.description());
					} catch (Throwable e) {
						throw new RuntimeException(e);
					}
				});
		tableView.getSelectionModel().clearSelection();
	}

	@FXML
	private void onPrintZebraClicked(ActionEvent event) throws PrintException {
		String content = getBarcodeContent();
		String weightStr = weightField.getText() + "lb";
		String productStr = "Product " + productCodeField.getText();
		String group = "Unknown";
		printZebra(content, group, weightStr, productStr);
	}

	private void printZebra(String content, String group, String weightStr, String description) {
		try {
			String zpl = formatString(78, group, description, weightStr, content);
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
			job.print(doc, attrs);
		} catch (Throwable t) {
			t.printStackTrace();
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
		String weightStr = weightField.getText();
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
				+ "^CF,20\n" // set default font 0 to 30 dots high
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

	public String capitalizeFirstLetter(String original) {
		if (original == null || original.length() == 0) {
			return original;
		}
		return original.substring(0, 1).toUpperCase() + original.substring(1);
	}
}