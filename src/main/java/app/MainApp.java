package app;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainApp extends Application {

	// expose these to code coverage tests
	protected static Stage currentStage;
	protected MainController controller;

	private final Logger logger = Logger.getLogger(MainApp.class.getName());
	private FileHandler fh = null;
	private File appDir;

	@Override
	public void start(Stage primaryStage) throws Exception {
		Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> showError(t, e));
		File userDir = new File(System.getProperty("user.home"));
		appDir = new File(userDir, ".fertilizer");
		if (!appDir.exists()) {
			appDir.mkdirs();
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		fh = new FileHandler(appDir.getAbsolutePath() + "/" + formatter.format(LocalDateTime.now()) + ".log", 1000000l,
				1, true);
		fh.setFormatter(new SimpleFormatter());
		logger.addHandler(fh);
		logger.setUseParentHandlers(false);
		currentStage = primaryStage;
		URL resource = MainApp.class.getResource("MainView.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		BorderPane root = (BorderPane) loader.load();
		controller = loader.getController();

		primaryStage.setTitle("Weight Embedding Barcoder");
		Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
		double width = primScreenBounds.getWidth();
		double height = primScreenBounds.getHeight();
		primaryStage.setX(width / 8.0);
		primaryStage.setY(height / 8.0);
		Scene scene = new Scene(root, width * 0.75, height * 0.75);
		scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
		primaryStage.setScene(scene);
		// let all commits and invalidation events be processed before we start

		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

	protected void showError(Thread t, Throwable e) {
		getCause(e).printStackTrace();
		Platform.runLater(() -> {
			showErrorDialog(t, e);
		});
	}

	protected void showErrorDialog(Thread t, Throwable e) {
		try {
			logger.log(Level.SEVERE, "Exception in App", getCause(e));
			// Create a new dialog
			// Create a dialog to display the exception
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Unhandled Exception");
			alert.setHeaderText("An unhandled exception occurred");

			// Create a TextArea to display the stack trace
			TextArea textArea = new TextArea();
			textArea.setEditable(false);
			textArea.setWrapText(true);

			// Get the stack trace as a string
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			getCause(e).printStackTrace(pw);
			String stackTrace = sw.toString();

			// Set the stack trace in the TextArea
			textArea.setText(stackTrace);

			BorderPane pane = new BorderPane();
			pane.setCenter(textArea);

			final Stage stage = new Stage();

			double x = currentStage.getX() + 75.0;
			double y = currentStage.getY() + 75.0;
			stage.setX(x);
			stage.setY(y);
			stage.initModality(Modality.NONE);
			Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
			double width = primScreenBounds.getWidth();
			double height = primScreenBounds.getHeight();

			Scene scene = new Scene(pane, width * 0.65, height * 0.65);
			stage.setScene(scene);
			stage.show();
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

	protected static void close() {
		Platform.runLater(() -> currentStage.close());
	}
}