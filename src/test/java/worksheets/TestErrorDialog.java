package worksheets;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import app.MainApp;

class TestErrorDialog {

	
	@Test
	void testErrorInErrorDialog() throws IOException {	    
	    Throwable exc = null;
        try {
            new MainApp().showErrorDialog(null, null);
        } catch (Throwable t) {
            exc = t;
        }
        assertTrue(exc == null);		
	
}

}
