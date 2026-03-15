package app;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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

         
            // First row is headers
            //headers 
            
            String headerLine = Files.lines(file.toPath())
                    .findFirst().get();
            headers = headerLine.replaceAll("['\"]", "").split(COMMA_DELIMITER);
            
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
            csvData = parseCsvFile(file);
            csvTableView.setItems(csvData);
    }

    private ObservableList<ObservableList<String>> parseCsvFile(File file) throws IOException {
        List<ObservableList<String>>  content = Files.lines(file.toPath())
              .skip(1)
              .map(line -> {
                  String dequotedLine = line.replaceAll("['\"]", "");
                  String[] values = dequotedLine.split(COMMA_DELIMITER);
                  ObservableList<String> rowValues = FXCollections.observableArrayList(values);
                  return rowValues;
              })
              .collect(Collectors.toList());
        ObservableList<ObservableList<String>> csvData = FXCollections.observableArrayList(content);
        return csvData;      
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
    public String escapeCsvField(String field) {
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