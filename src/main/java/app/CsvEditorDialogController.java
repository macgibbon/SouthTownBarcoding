package app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CsvEditorDialogController {

    @FXML
    private Label filePathLabel;

    @FXML
    private TableView<ObservableList<String>> csvTableView;

    private Path currentFilePath;
    private String[] headers;
    private ObservableList<ObservableList<String>> csvData;
    private static final String COMMA_DELIMITER = ",";

    @FXML
    private void initialize() {
        csvData = FXCollections.observableArrayList();
    }

    @FXML
    private void handleLoadCsv(ActionEvent event) {
        Window window = csvTableView.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open CSV File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        // Remember last used folder
        String lastFolder = Model.getInstance().preferences.get("csvLastFolder", 
            System.getProperty("user.home"));
        fileChooser.setInitialDirectory(new File(lastFolder));

        File selectedFile = fileChooser.showOpenDialog(window);
        if (selectedFile != null) {
            currentFilePath = selectedFile.toPath();
            Model.getInstance().preferences.put("csvLastFolder", selectedFile.getParent());
            loadCsvFile(selectedFile);
        }
    }

    private void loadCsvFile(File file) {
        try {
            csvData.clear();
            csvTableView.getColumns().clear();

            List<String[]> rows = parseCsvFile(file);
            
            if (rows.isEmpty()) {
                filePathLabel.setText(file.getAbsolutePath());
                return;
            }

            // First row is headers
            headers = rows.get(0);
            
            // Create table columns dynamically
            for (int col = 0; col < headers.length; col++) {
                final int colIndex = col;
                TableColumn<ObservableList<String>, String> column = 
                    new TableColumn<>(headers[col]);
                
                column.setCellValueFactory(cellData -> 
                    new javafx.beans.property.ReadOnlyObjectWrapper<>(
                        cellData.getValue().get(colIndex)
                    )
                );
                
                column.setCellFactory(TextFieldTableCell.forTableColumn());
                
                column.setOnEditCommit(event -> {
                    event.getRowValue().set(colIndex, event.getNewValue());
                });
                
                column.setPrefWidth(120.0);
                column.setResizable(true);
                csvTableView.getColumns().add(column);
            }

            // Load data rows (skip header row)
            for (int i = 1; i < rows.size(); i++) {
                String[] rowData = rows.get(i);
                ObservableList<String> rowList = FXCollections.observableArrayList(rowData);
                csvData.add(rowList);
            }

            csvTableView.setItems(csvData);
            filePathLabel.setText(file.getAbsolutePath());

        } catch (Exception e) {
            showError("Error loading CSV file", e.getMessage());
        }
    }

    private List<String[]> parseCsvFile(File file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = parseCSVLine(line);
                rows.add(values);
            }
        }
        return rows;
    }

    /**
     * Parse a single CSV line, handling quoted fields with embedded commas
     */
    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (insideQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Escaped quote
                    currentField.append('"');
                    i++;
                } else {
                    // Toggle quote state
                    insideQuotes = !insideQuotes;
                }
            } else if (c == ',' && !insideQuotes) {
                // Field delimiter
                fields.add(currentField.toString().trim());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        fields.add(currentField.toString().trim());
        return fields.toArray(new String[0]);
    }

    @FXML
    private void handleAddRow(ActionEvent event) {
        if (headers == null || headers.length == 0) {
            showError("No CSV loaded", "Please load a CSV file first");
            return;
        }

        ObservableList<String> newRow = FXCollections.observableArrayList();
        for (int i = 0; i < headers.length; i++) {
            newRow.add("");
        }
        csvData.add(newRow);
        csvTableView.scrollTo(csvData.size() - 1);
    }

    @FXML
    private void handleDeleteRow(ActionEvent event) {
        int selectedIndex = csvTableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            csvData.remove(selectedIndex);
        } else {
            showError("No selection", "Please select a row to delete");
        }
    }

    @FXML
    private void handleClearAll(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear All Data");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("This will delete all rows. This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            csvData.clear();
        }
    }

    @FXML
    private void handleSaveCsv(ActionEvent event) {
        if (currentFilePath == null) {
            showError("No file selected", "Please load a CSV file first");
            return;
        }

        try {
            saveCsvFile(currentFilePath.toFile());
            showInfo("Success", "CSV file saved successfully");
        } catch (Exception e) {
            showError("Error saving CSV file", e.getMessage());
        }
    }

    private void saveCsvFile(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Write headers
            writer.println(String.join(COMMA_DELIMITER, headers));

            // Write data rows
            for (ObservableList<String> row : csvData) {
                String csvLine = row.stream()
                    .map(this::escapeCsvField)
                    .collect(java.util.stream.Collectors.joining(COMMA_DELIMITER));
                writer.println(csvLine);
            }
            writer.flush();
        }
    }

    /**
     * Escape CSV field values that contain special characters
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        
        if (field.contains(COMMA_DELIMITER) || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        javafx.stage.Stage stage = (javafx.stage.Stage) csvTableView.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}