package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.print.PrintException;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public class MainController {

    public static final String LAST_USED_FOLDER = "lastUsedFolder";
    public static final String COMMA_DELIMITER = ",";
 
    @FXML
    private Label messageLabel, contentLabel;

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
    
    @FXML
    private Tab manualtab;

    private Model model;

    private FileChooser fileChooser;
 

    private static final String PRINTER_NAME = "Zebra"; // <- change to part or full name of your printer
  
    TreeMap<Integer, ProductId> productIdMap;
    final Printer printer = new Printer(PRINTER_NAME);
	private File currentDefaults;

    @FXML
    private void initialize() throws FileNotFoundException, IOException {
        model = Model.getInstance();

        tableView.setItems(model.productLabels);

        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        final int printedColumn = 4;
        List<TableColumn<ProductLabel, ?>> tcList = Stream.of(ProductLabel.class.getFields())
                .limit(printedColumn)
                .map(rc -> rc.getName())
                .map(name -> capitalizeFirstLetter(name))
                .map(name -> new TableColumn<ProductLabel, String>(name))
                .map(tc -> {
                    tc.setCellValueFactory(new PropertyValueFactory<>(tc.getText()));
                    tc.setPrefWidth(200.0);
                    tc.setEditable(false);
                    return tc;
                })
                .collect(Collectors.toList());

        TableColumn<ProductLabel, Boolean> printedTableColumn = new TableColumn<ProductLabel, Boolean>("Printed");
        printedTableColumn.setCellFactory(CheckBoxTableCell.forTableColumn(printedTableColumn));
        printedTableColumn.setPrefWidth(100.0);
        printedTableColumn.setCellValueFactory(new PropertyValueFactory<>("printed"));
        printedTableColumn.setEditable(true);
        tcList.add(printedTableColumn);
        tableView.getColumns().setAll(FXCollections.observableList(tcList));
        tableView.setEditable(true);
        printbutton.disableProperty().bind(Bindings.isEmpty(tableView.getSelectionModel().getSelectedItems()));
        weightField.setOnKeyPressed(keyevent -> {
            if (keyevent.getCode() == KeyCode.ENTER) {
                double w = 0.0;
                w = Double.parseDouble(weightField.getText());
                if (w <= 0.0) {
                    Platform.runLater(() -> weightField.selectAll());                   
                    throw new IllegalArgumentException("Weight of " + w + " not allowed, must be greater than zero!");
                }
                if (w >= 100.0) {
                    Platform.runLater(() -> weightField.selectAll());                     
                    throw new IllegalArgumentException("Weight of " + w + " too large!");
                }
                Barcode bc = new Barcode(weightField.getText(), productCodeField.getText());
                String barcode = bc.content();
                String group = groupField.getText();
                String description = descriptionField.getText();
                String weight = weightField.getText();
                Platform.runLater(() -> weightField.requestFocus());
                Platform.runLater(() -> weightField.clear());
                Platform.runLater(() -> printer.print(barcode, group, weight + " lb", description));
            }
        });    
    }

    @FXML
    private void onGenerateClicked(ActionEvent event) throws WriterException {
        String weightStr = weightField.getText();
        String productStr = productCodeField.getText();
        Barcode barcodeWithWeight = new Barcode(weightStr, productStr);
        handleUPCEmbedded(barcodeWithWeight);
    }

    @FXML
    private void onLookupBeefClicked(ActionEvent event) {
    	var productToSearchMap = filterBeef(productIdMap);
            lookupProduct(productToSearchMap);
    }
    
    @FXML
    private void onLookupPorkClicked(ActionEvent event) {
    	var productToSearchMap = filterNotBeef(productIdMap);
        lookupProduct(productToSearchMap);
    }
    
    @FXML
    private void editProductsClicked(ActionEvent event) throws IOException {
        File defaultProductsFile = new File(currentDefaults, "defaultProducts001.csv");
        Window window = messageLabel.getScene().getWindow();
        new CsvEditor().showCsvEditorDialog(window, defaultProductsFile);    
    }

    private static boolean isBeef(ProductGroup group) {
    	return ((group == ProductGroup.Beef) || (group == ProductGroup.Fully_Cooked_Beef));
    }
    private static boolean isNotBeef(ProductGroup group) {
       return !isBeef(group);
    }
      
	private void lookupProduct(List<ProductId> productToSearchMap) {
		Window window = messageLabel.getScene().getWindow();
        Optional<ProductId> result = new ProductDialog(productToSearchMap,window).showAndWait();
        result.ifPresent(pid -> {
            String pidStr = String.format("%06d", pid.id());
            int length = pidStr.length();
            if (length > 6)
                pidStr = pidStr.substring(length - 6);
            productCodeField.setText(pidStr);
            groupField.setText(pid.productGroup().toString());
            descriptionField.setText(pid.description());
            Platform.runLater(() -> weightField.requestFocus());
            Platform.runLater(() -> weightField.clear());
        });
	}

    @FXML
    private void onPrintWindowsClicked(ActionEvent event) throws WriterException {
        String weightStr = weightField.getText();
        String productStr = productCodeField.getText();
        Barcode barcodeWithWeight = new Barcode(weightStr, productStr);

        handleUPCEmbedded(barcodeWithWeight);
        PrinterJob job = PrinterJob.createPrinterJob();

        // Show print dialog attached to the current window
        Window window = messageLabel.getScene().getWindow();

        boolean proceed = job.showPrintDialog(window);
        if (!proceed) {
            messageLabel.setText("Print cancelled.");
            return;
        }

        // Print the image node (ImageView)
        try {
            job.printPage(barcodeView);
        } finally {
            job.endJob();
        }

    }
    
    @FXML
    private void openReportFor1Pork(ActionEvent event) throws FileNotFoundException, IOException {    	
    	 openReportFor(filterNotBeef(productIdMap));
    }

    public static List<ProductId> filterNotBeef(TreeMap<Integer, ProductId> pidMap) {
        var productToSearchMap = pidMap.values()
    	     		.stream()
    	     		.sorted((e1,e2) -> sortProducts(e1,e2))
    	    		.filter(entry -> isNotBeef(entry.productGroup())) 
    	    		.collect(Collectors.toList());
        return productToSearchMap;
    }

    private static int sortProducts( ProductId id1, ProductId id2) {
       int groupCompare = id1.productGroup().compareTo(id2.productGroup());
       if (groupCompare == 0)
           return Integer.compare(id1.id(), id2.id());
       else
           return groupCompare;        
    }

    protected void openReportFor(List<ProductId> productToSearchMap) throws IOException, FileNotFoundException {
        Window window = messageLabel.getScene().getWindow();
         Optional<ProductId> result = new ProductDialog(productToSearchMap,window).showAndWait();
         if (result.isPresent()) {
             String pickedPid = result.get().id().toString();
             File lastUsedDirectory = new File(model.preferences.get(LAST_USED_FOLDER, Path.of("spreadsheets").toFile().getAbsolutePath()));
             fileChooser = new FileChooser();
             if (lastUsedDirectory.exists())
                 fileChooser.setInitialDirectory(lastUsedDirectory);
             // Show the save file dialog
             File file = fileChooser.showOpenDialog((Stage) tableView.getScene().getWindow());
             if (file != null) {
                 model.preferences.put(LAST_USED_FOLDER, file.getParent());
                 InventoryReport inventoryReport = new InventoryReport(file);
                 ArrayList<ProductLabel> pidLabels = inventoryReport.productLabels.stream()
                     .filter(label -> label.productId.get().equals(pickedPid))
                     .collect(Collectors.toCollection(ArrayList::new));
                 model.productLabels.setAll(pidLabels);
                 spreadsheetNameLabel.setText(file.getAbsolutePath());
             }
             tabpane.getSelectionModel().select(1);            
         }
    }

    @FXML
    private void openReportFor1Beef(ActionEvent event) throws FileNotFoundException, IOException {       
    	openReportFor(filterBeef(productIdMap));
    }

    public static List<ProductId> filterBeef(TreeMap<Integer, ProductId> pidMap) {
        var productToSearchMap = pidMap.values()
    	     		.stream()    	
    	    		.filter(entry -> isBeef(entry.productGroup())) 
    	    		.sorted((e1,e2) -> sortProducts(e1,e2))
    	    		.collect(Collectors.toList());
        return productToSearchMap;
    }
    
    @FXML
    private void printNext20(ActionEvent event) throws FileNotFoundException, IOException {
       tableView.getItems().stream()
            .filter(label -> !label.getPrinted())
            .limit(20)       
            .forEach(label -> {                
                Barcode barcodeWithWeight = new Barcode(label.weight.get(), label.productId.get());               
                 printer.print(barcodeWithWeight.content(), label.group.get().toString(), label.weight.get() + " lb", label.description.get());
                 label.printed.setValue(true);
            });
    }
    
    @FXML
    private void printNext40(ActionEvent event) throws FileNotFoundException, IOException {
        tableView.getItems().stream()
        .filter(label -> !label.getPrinted())
        .limit(40)       
        .forEach(label -> {                
            Barcode barcodeWithWeight = new Barcode(label.weight.get(), label.productId.get());               
             printer.print(barcodeWithWeight.content(), label.group.get().toString(), label.weight.get() + " lb", label.description.get());
             label.printed.setValue(true);
        });
    }
    
    @FXML
    private void printAll(ActionEvent event) throws FileNotFoundException, IOException {
        tableView.getItems().stream()
        .filter(label -> !label.getPrinted())     
        .forEach(label -> {                
            Barcode barcodeWithWeight = new Barcode(label.weight.get(), label.productId.get());               
             printer.print(barcodeWithWeight.content(), label.group.get().toString(), label.weight.get() + " lb", label.description.get());
             label.printed.setValue(true);
        });
    }
    
    @FXML
    private void openReport(ActionEvent event) throws FileNotFoundException, IOException {
        File lastUsedDirectory = new File(model.preferences.get(LAST_USED_FOLDER, Path.of("spreadsheets").toFile().getAbsolutePath()));
        fileChooser = new FileChooser();
        if (lastUsedDirectory.exists())
            fileChooser.setInitialDirectory(lastUsedDirectory);
        // Show the save file dialog
        File file = fileChooser.showOpenDialog((Stage) tableView.getScene().getWindow());
        if (file != null) {
            model.preferences.put(LAST_USED_FOLDER, file.getParent());
            InventoryReport inventoryReport = new InventoryReport(file);
            model.productLabels.setAll(inventoryReport.productLabels);
            spreadsheetNameLabel.setText(file.getAbsolutePath());
        }
        tabpane.getSelectionModel().select(1);
     //   tableView.getSelectionModel().selectAll();
      //  Platform.runLater(() -> printbutton.requestFocus());
    }

    @FXML
    private void printSelected(ActionEvent event) {
        ObservableList<Integer> selectedRows = tableView.getSelectionModel().getSelectedIndices();
        selectedRows.stream()
            .forEach(selected -> {
                ProductLabel label = tableView.getItems().get(selected);
                Barcode barcodeWithWeight = new Barcode(label.weight.get(), label.productId.get());
                printer.print(barcodeWithWeight.content(), label.group.get().toString(), label.weight.get() + " lb", label.description.get());
                label.printed.setValue(true);
            });
    }

    @FXML
    private void onPrintZebraClicked(ActionEvent event) throws PrintException {
        String content = getBarcodeContent();
        String weightStr = weightField.getText() + "lb";
        String productStr = "Product " + productCodeField.getText();
        String group = "Unknown";
        printer.print(content, group, weightStr, productStr);
    }

    private void handleUPCEmbedded(Barcode bc) throws WriterException {
        Image image = bc.image(BarcodeFormat.UPC_A, 360, 120);
        barcodeView.setImage(image);
        messageLabel.setText("UPC-A (weight) generated: " + bc.content());
        contentLabel.setText(bc.content());
    }

    private String getBarcodeContent() {
        String weightStr = weightField.getText();
        String productStr = productCodeField.getText();
        Barcode barcodeWithWeight = new Barcode(weightStr, productStr);
        return barcodeWithWeight.content();
    }

    public String capitalizeFirstLetter(String original) {
        if (original == null || original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    public void setCurrentDefaults(File currentDefaults) throws IOException {
    	this.currentDefaults = currentDefaults;
        File defaultProductsFile = new File(currentDefaults, "defaultProducts001.csv");
        productIdMap = loadDefaultProductFiles(defaultProductsFile);     
    }
    
    public static TreeMap<Integer, ProductId> loadDefaultProductFiles(File defaultProductsFile) throws FileNotFoundException, IOException {
        TreeMap<Integer, ProductId> productMap = new TreeMap<>();
        if (defaultProductsFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(defaultProductsFile))) {
                @SuppressWarnings("unused")
                String headers = br.readLine();
                String line;
                while ((line = br.readLine()) != null) {
                    String dequotedLine = line.replaceAll("['\"]", "");
                    String[] values = dequotedLine.split(COMMA_DELIMITER);
                    int id = Integer.parseUnsignedInt(values[0]);
                    String group = values[1].replace(' ', '_');
                    String description = values[2];
                    ProductGroup pg = null;
                    try {
                        pg = ProductGroup.valueOf(group);
                    } catch (IllegalArgumentException e) {
                        pg = ProductGroup.__;
                    }
                    ProductId pid = new ProductId(id, pg, description);
                    productMap.put(id, pid);
                }
            }
        }
        return productMap;
    }
}