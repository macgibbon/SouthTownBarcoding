module southtown {
	exports app;
	opens app;

	requires com.google.zxing;
	requires com.google.zxing.javase;
	requires java.desktop;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.fxml;
	requires transitive javafx.graphics;
	requires javafx.swing;
	requires java.logging;
	requires transitive org.apache.poi.poi;
	
}