package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;

public class CsvEditorController {
    
    private Path currentFilePath;
    private String[] headers;
    private ObservableList<ObservableList<String>> csvData;
    private static final String COMMA_DELIMITER = ",";

 
    @FXML
    private TableView<ObservableList<String>> csvTableView;
   
    @FXML
    private void initialize() {
        csvData = FXCollections.observableArrayList();
    }

    private void loadCsvFile(File file) throws IOException {
            csvData.clear();
            csvTableView.getColumns().clear();

            List<String[]> rows = parseCsvFile(file);
            
            if (rows.isEmpty()) {              
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
        } 
    }

    @FXML
    private void handleSaveCsv(ActionEvent event) throws IOException {
        saveCsvFile(currentFilePath.toFile());
        showInfo("Success", "CSV file saved successfully");
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

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setDefaultsFile(File defaultProductsFile) throws IOException {
        currentFilePath = defaultProductsFile.toPath();
        loadCsvFile(defaultProductsFile);
    }
}