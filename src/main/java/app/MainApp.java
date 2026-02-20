package app;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
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

public class MainApp extends Application {

    // expose these to code coverage tests
    protected static Stage currentStage;
    protected MainController controller;

    private final Logger logger = Logger.getLogger(MainApp.class.getName());
    private FileHandler fh = null;
    private File appDir;
    public File currentDefaults;

    @Override
    public void start(Stage primaryStage) throws Exception {
        File userHome = new File(System.getProperty("user.home"));
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> showError(t, e));
        appDir = new File(userHome, ".barcoder");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        loadDefaults();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        fh = new FileHandler(appDir.getAbsolutePath() + "/" + formatter.format(LocalDateTime.now()) + ".log", 1000000l, 1, true);
        fh.setFormatter(new SimpleFormatter());
        logger.addHandler(fh);
        logger.setUseParentHandlers(false);
        currentStage = primaryStage;
        URL resource = MainApp.class.getResource("MainView.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        BorderPane root = (BorderPane) loader.load();
        controller = loader.getController();
        controller.setCurrentDefaults(currentDefaults);

        primaryStage.setTitle("Weight Embedding Barcoder");
        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        double width = primScreenBounds.getWidth();
        double height = primScreenBounds.getHeight();
        primaryStage.setX(width / 8.0);
        primaryStage.setY(height / 8.0);
        Scene scene = new Scene(root, width * 0.75, height * 0.75);
        scene.getStylesheets().add(getClass().getResource("/app/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        // let all commits and invalidation events be processed before we start

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    protected void showError(Thread t, Throwable e) {

        Platform.runLater(() -> {
            showErrorDialog(t, e);
        });
    }

    protected void showErrorDialog(Thread t, Throwable e) {
        try {
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
        } catch (Throwable x) {
            logger.log(Level.SEVERE, "Exception in showErrorDialog", getCause(x));
        }
    }

    public static Throwable getCause(Throwable x) {
        Throwable t = x;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    public static void close() {
        Platform.runLater(() -> currentStage.close());
    }

    public static void copy(Path sourcePath, Path targetPath, Path source) {
        Path target = targetPath.resolve(sourcePath.relativize(source));
        if (!(target.toFile().exists())) {
            try {
                Files.copy(source, target);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void deepCopy(Path sourcePath, Path targetPath)  {
        try {
        try (Stream<Path> stream = Files.walk(sourcePath)) {
            stream.forEach(source -> copy(sourcePath, targetPath, source));
        }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }   
    
    private void loadDefaults() throws Exception {
        File javaDir = new File(System.getProperty("java.home"));
        currentDefaults = new File(appDir, "currentDefaults");
        currentDefaults.mkdirs();
        Path defaultPath = new File(javaDir, "defaults").toPath();
        List<Path> defaultPaths = List.of(Path.of("./defaults"), defaultPath);
        defaultPaths.stream()
                .findFirst()
                .ifPresent(path ->deepCopy(path, currentDefaults.toPath()));
    }

}