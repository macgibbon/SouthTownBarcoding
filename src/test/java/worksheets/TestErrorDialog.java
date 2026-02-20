package worksheets;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import app.MainApp;

class TestErrorDialog {

	// For code coverage of main method
	@Test
	void testMainLauncher() throws IOException {
		new MainApp().showErrorDialog(null, null);
	
}

}
