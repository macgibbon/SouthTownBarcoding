package app;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;
import java.util.logging.Level;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ErrorDisplay {

    @SuppressWarnings("unused")
    private Thread t;
    private Throwable e;
    private Stage currentStage;
    private Logger logger;

    public ErrorDisplay(Thread t, Throwable e, Stage currentStage, Logger logger) {

        this.t = t;
        this.e = e;
        this.currentStage = currentStage;
        this.logger = logger;
    }

    public void show() {
        Throwable cause = getCause(e);
        logger.log(Level.SEVERE, "Exception in App", cause);

        // Create a TextArea to display the stack trace
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);

        // Get the stack trace as a string
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        cause.printStackTrace(pw);
        String stackTrace = sw.toString();

        // Set the stack trace in the TextArea
        textArea.setText(stackTrace);

        BorderPane pane = new BorderPane();
        pane.setId("exceptionpane");
        pane.setCenter(textArea);
        VBox bottomBox = new VBox();
        bottomBox.setAlignment(Pos.CENTER);
        Button closeButton = new Button("Close");
        bottomBox.getChildren().add(closeButton);
        pane.setBottom(bottomBox);
        Label titleLabel = new Label(cause.getMessage());
        pane.setTop(titleLabel);

        final Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);

        double x = currentStage.getX() + 75.0;
        double y = currentStage.getY() + 75.0;

        stage.setX(x);
        stage.setY(y);
        stage.initModality(Modality.NONE);
        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        double width = primScreenBounds.getWidth();
        double height = primScreenBounds.getHeight();

        Scene scene = new Scene(pane, width * 0.65, height * 0.65);
        scene.getStylesheets().add(getClass().getResource("/app/styles.css").toExternalForm());
        closeButton.setOnAction(event -> stage.close());
        stage.setScene(scene);
        stage.show();
        Platform.runLater(() -> closeButton.requestFocus());
    }

    public static Throwable getCause(Throwable x) {
        Throwable t = x;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }
}
