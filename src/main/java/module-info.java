module southtown {
	exports app;
	opens app;

	requires com.google.zxing;
	requires com.google.zxing.javase;
	requires java.desktop;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires javafx.swing;
}