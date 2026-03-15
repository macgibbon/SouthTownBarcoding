package app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
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
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

public class MainApp extends Application {

    // expose these to code coverage tests
    protected static Stage currentStage;
    protected MainController controller;

    private final Logger logger = Logger.getLogger(MainApp.class.getName());
    private FileHandler fh = null;
    private File appDir;
    public File currentDefaults;
    SimpleStringProperty version = new SimpleStringProperty("Version 0.00");

    @Override
    public void start(Stage primaryStage) throws FileNotFoundException {
        File userHome = new File(System.getProperty("user.home"));
        try {
            
            Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> showError(t, e));
            appDir = new File(userHome, ".barcoder");
            if (!appDir.exists()) {
                appDir.mkdirs();
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            fh = new FileHandler(appDir.getAbsolutePath() + "/" + formatter.format(LocalDateTime.now()) + ".log", 1000000l, 1, true);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setUseParentHandlers(false);
            loadDefaults();           
            version.set("Weight Embedding Barcoder " + System.getProperty("jpackage.app-version", "Development"));
           
            currentStage = primaryStage;
            URL resource = MainApp.class.getResource("MainView.fxml");
            FXMLLoader loader = new FXMLLoader(resource);
            BorderPane root = (BorderPane) loader.load();
            controller = loader.getController();
            controller.setCurrentDefaults(currentDefaults);
            primaryStage.titleProperty().bind(version);
            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            double width = primScreenBounds.getWidth();
            double height = primScreenBounds.getHeight();
            primaryStage.setX(width / 20.0);
            primaryStage.setY(height / 20.0);
            Scene scene = new Scene(root, width * 0.9, height * 0.9);
            scene.getStylesheets().add(getClass().getResource("/app/styles.css").toExternalForm());
            primaryStage.setScene(scene);
            // let all commits and invalidation events be processed before we start

            primaryStage.show();
        } catch (Exception t) {
         //   t.printStackTrace();  
            File file = new File(userHome+ "/.barcoder/StartupCrash.log");
            file.getParentFile().mkdirs();
            try (PrintStream fos = new PrintStream(file)) {
                t.printStackTrace(fos);
            };
        }
        finally {
            fh.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    protected void showError(Thread t, Throwable e) {
        Platform.runLater(() -> {
            showErrorDialog(t, e);
        });
    }

    public void showErrorDialog(Thread t, Throwable e) {
        try {
            ErrorDisplay erroDisplay = new ErrorDisplay(t, e, currentStage, logger);
            erroDisplay.show();
        } catch (Throwable x) {
            logger.log(Level.SEVERE, "Exception in showErrorDialog", ErrorDisplay.getCause(x));
        }
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

    public static void deepCopy(Path sourcePath, Path targetPath) {
        try {
            try (Stream<Path> stream = Files.walk(sourcePath)) {
                stream.forEach(source -> copy(sourcePath, targetPath, source));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void loadDefaults() throws Exception {
        File javaDir = new File(System.getProperty("java.home"));
        currentDefaults = new File(appDir, "currentDefaults");
        currentDefaults.mkdirs();
        Path defaultPath = new File(javaDir, "defaults").toPath();
        List<Path> defaultPaths = List.of(Path.of("./defaults"), defaultPath);
        defaultPaths.stream().findFirst().ifPresent(path -> deepCopy(path, currentDefaults.toPath()));
    }

   
}