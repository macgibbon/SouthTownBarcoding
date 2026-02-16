package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Callback;

public class Model {


	private static final Model instance = new Model();

	public static Model getInstance() {
		return instance;
	}

	// Define the extractor callback
	Callback<ProductLabel, Observable[]> extractor = pl -> new Observable[] {
	    pl.printedProperty(),
	
	};
	public ObservableList<ProductLabel> productLabels = FXCollections.observableArrayList(extractor);


	public File appDir;
	public File currentDefaults;
	public Preferences preferences;

	private Model() {
		super();
		loadDefaults();
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
		try (Stream<Path> stream = Files.walk(sourcePath)) {
			stream.forEach(source -> copy(sourcePath, targetPath, source));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void loadDefaults() {
		File userHome = new File(System.getProperty("user.home"));
		System.out.println("User home =" + userHome.getAbsolutePath());
		File userDir = new File(System.getProperty("user.dir"));
		System.out.println("User dir =" + userDir.getAbsolutePath());
		File javaDir = new File(System.getProperty("java.home"));
		System.out.println("Java home =" + javaDir.getAbsolutePath());
		appDir = new File(userHome, ".barcoder");
		if (!appDir.exists()) {
			appDir.mkdirs();
		}
		currentDefaults = new File(appDir, "currentDefaults");
		currentDefaults.mkdirs();
		Path defaultPath = new File(javaDir,"defaults").toPath();
		if (defaultPath.toFile().exists()) {
			deepCopy(defaultPath, currentDefaults.toPath());
		}
		else {
			Path altPath = Path.of("./defaults");
			if (altPath.toFile().exists()) {
				deepCopy(altPath, currentDefaults.toPath());
			}
		}
		preferences = Preferences.userNodeForPackage(getClass());
		
			
	}
}
