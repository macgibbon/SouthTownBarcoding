package worksheets;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import app.MainApp;

class TestErrorDialog {

	// For code coverage of main method
	@Test
	void testMainLauncher() throws IOException {	    
	    Throwable exc = null;
        try {
            new MainApp().showErrorDialog(null, null);
        } catch (Throwable t) {
            exc = t;
        }
        assertTrue(exc == null);		
	
}

}
