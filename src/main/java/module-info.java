module southtown {
	exports app;
	opens app;

	requires transitive com.google.zxing;
	requires com.google.zxing.javase;
	requires transitive java.desktop;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.fxml;
	requires transitive javafx.graphics;
	requires javafx.swing;
	requires transitive java.logging;
	requires transitive org.apache.poi.poi;
	requires transitive java.prefs;
	
}