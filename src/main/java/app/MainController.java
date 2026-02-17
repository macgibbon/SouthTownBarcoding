package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
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
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public class MainController {

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

    TreeMap<Integer, ProductId> productIdMap;
    final Printer printer = new Printer(PRINTER_NAME);

    @FXML
    private void initialize() {
        model = Model.getInstance();

        tableView.setItems(model.productLabels);


        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        final int printedColumn = 3;
        List<TableColumn<ProductLabel, ?>> tcList = Stream.of(ProductLabel.class.getFields()).limit(printedColumn).map(rc -> rc.getName())
                .map(name -> capitalizeFirstLetter(name)).map(name -> new TableColumn<ProductLabel, String>(name)).map(tc -> {
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
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
        return productMap;
    }

    @FXML
    private void onGenerateClicked(ActionEvent event) {
        try {
            String weightStr = weightField.getText();
            String productStr = productCodeField.getText();
            Barcode barcodeWithWeight = new Barcode(weightStr, productStr);
            handleUPCEmbedded(barcodeWithWeight);
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

        for (int i = 0; i < productIds.length; i++) {
            Integer pid = productIds[i];
            ProductId productId = productIdMap.get(pid);
            String text = productId.productGroup() + " " + productId.description();
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
                var pid =  button.getUserData();
                if (pid != null) {
                    args.consume();
                    dialog.setResult((ProductId) pid);
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
                        Barcode bc = new Barcode(weightField.getText(), productCodeField.getText());
                        String barcode = bc.content();
                        String group = groupField.getText();
                        String description = descriptionField.getText();
                        String weight = weightField.getText();
                        Platform.runLater(() -> weightField.requestFocus());
                        Platform.runLater(() -> weightField.clear());
                        Platform.runLater(() -> printer.print(barcode, group, weight + " lb", description));
                    }
                } catch (Throwable t) {
                }
            }
        });

   		dialog.setResultConverter(dialogButton ->  null);

        Optional<ProductId> result = dialog.showAndWait();
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
    private void onPrintWindowsClicked(ActionEvent event) {
        try {
            String weightStr = weightField.getText();
            String productStr = productCodeField.getText();
            Barcode barcodeWithWeight = new Barcode(weightStr, productStr);

            handleUPCEmbedded(barcodeWithWeight);
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
        tableView.getSelectionModel().selectAll();
        Platform.runLater(() -> printbutton.requestFocus());
    }

    @FXML
    private void printSelected(ActionEvent event) {
        ObservableList<Integer> selectedRows = tableView.getSelectionModel().getSelectedIndices();
        selectedRows.stream()
            .forEach(selected -> {
                ProductLabel label = tableView.getItems().get(selected);
                Barcode barcodeWithWeight = new Barcode(label.weight.get(), label.productId.get());
                try {
                    printer.print(barcodeWithWeight.content(), label.group.get().toString(), label.weight.get() + " lb", label.description.get());
                    label.printed.setValue(true);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
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
        try {
            Image image = bc.image(BarcodeFormat.UPC_A, 360, 120);
            barcodeView.setImage(image);
            messageLabel.setText("UPC-A (weight) generated: " + bc.content());
            contentLabel.setText(bc.content());
        } catch (IllegalArgumentException iae) {
            contentLabel.setText("");
            messageLabel.setText(iae.getMessage());
        }
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
}