package app;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class CsvEditor {
    
    public  void showCsvEditorDialog(Window owner, File defaultProductsFile) throws IOException {

            URL resource = MainApp.class.getResource("CsvEditorDialog.fxml");
            FXMLLoader loader = new FXMLLoader(resource);
            BorderPane root = (BorderPane) loader.load();
            CsvEditorController controller = loader.getController();
            controller.setDefaultsFile(defaultProductsFile);
            Stage dialog = new Stage();
            dialog.initOwner(owner);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("CSV Editor");
           
            Scene scene = new Scene(root);
            scene.getStylesheets().add(MainApp.class.getResource("/app/styles.css").toExternalForm());
            dialog.setScene(scene);
            Window dialogowner = dialog.getOwner();
            double width =dialogowner.getWidth();
            double height = dialogowner.getHeight();
            double x = dialogowner.getX();
            double y = dialogowner.getY();
            double margin = .045;
            
            dialog.setX(x + (width * (margin/2.0)));
            dialog.setY(y + (height * (margin/2.0)));
            dialog.setWidth( width * (1-margin));
            dialog.setHeight( height * (1-margin));
            dialog.showAndWait();
       
    }   

}
