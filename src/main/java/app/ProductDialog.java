package app;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

public class ProductDialog {
    
    private Dialog<ProductId> dialog;

    public ProductDialog( List<ProductId> productIdList, Window owner) {
        super();
        dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("Product Id Dialog");
        dialog.setHeaderText("Press the appropriate button.");

        // 2. Set the button types (OK and Cancel)
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/app/styles.css").toExternalForm());

        // 3. Create the custom layout for the content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));
        int cols = 4;
     
        for (int i = 0; i < productIdList.size(); i++) {
   
            ProductId productId = productIdList.get(i);
            String text = productId.productGroup() + " " + productId.description();
            Label label = new Label(Integer.toString(productId.id()));
            label.getStyleClass().add("id-label");
            Button button = new Button(text,label);
            button.setUserData(productId);
            grid.add(button, i % cols, i / cols);
        }
        dialog.getDialogPane().setContent(grid);
       
        EventHandler<ActionEvent> buttonFilter = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent args) {
                Button button = (Button) args.getTarget();
                var pid = button.getUserData();
                if (pid != null) {
                    args.consume();
                    dialog.setResult((ProductId) pid);
                    dialog.close();
                }
            }
        };
        dialog.getDialogPane().addEventFilter(ActionEvent.ACTION, buttonFilter);
       
        dialog.setResultConverter(dialogButton -> null);
        
       
    }
    
    public Optional<ProductId> showAndWait() {
        Window dialogowner = dialog.getOwner();
        double width =dialogowner.getWidth();
        double height = dialogowner.getHeight();
        double x = dialogowner.getX();
        double y = dialogowner.getY();
        double margin = .045;
        
        dialog.setX(x + (width * (margin/2.0)));
        dialog.setY(y + (height * (margin/2.0)));
        dialog.setWidth( width * (1-margin));
        dialog.setHeight(height * (1-margin));
        return dialog.showAndWait();
    }

}
