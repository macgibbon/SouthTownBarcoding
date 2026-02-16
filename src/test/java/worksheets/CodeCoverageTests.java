package worksheets;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import app.MainApp;

class CodeCoverageTests {

 
    @Test
    void testGetCause() {
        Throwable t = new RuntimeException(new NullPointerException());
        Throwable nestedException = MainApp.getCause(t);
        assertTrue(nestedException instanceof NullPointerException);
    }
    
   
  
}
