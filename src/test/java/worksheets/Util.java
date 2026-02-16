package worksheets;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import app.MainController;
import javafx.collections.ObservableList;

public class Util {

    public static void reflectiveSet(Object badModel, MainController controller, String name) {
        try {
            Field field = controller.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(controller, badModel);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object reflectiveGetField(Object controller, String name) {
        try {
            Field field = controller.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Method reflectiveGetMethod(Object controller, String name) {
        try {
            Method method = Stream.of(controller.getClass().getDeclaredMethods())
                    .filter(m -> m.getName().equals(name))
                    .findAny().get();
            method.setAccessible(true);
                    
            return method;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    

    public static void delay(int secs) {
        try {
            Thread.sleep(secs * 1000);
        } catch (InterruptedException e) {
        }
    }

    public static void pressGlobalExitKey() throws AWTException {
        java.awt.Robot awtRobot = new java.awt.Robot();
        awtRobot.keyPress(KeyEvent.VK_ALT);
        awtRobot.keyPress(KeyEvent.VK_F4);
        awtRobot.keyRelease(KeyEvent.VK_F4);
        awtRobot.keyRelease(KeyEvent.VK_ALT);
    }

    public static void pressGlobalEnterKey() throws AWTException {
        java.awt.Robot awtRobot = new java.awt.Robot();
        awtRobot.keyPress(KeyEvent.VK_ENTER);
        awtRobot.keyRelease(KeyEvent.VK_ENTER);
    }

    public static void delDirTree(File testFolder) throws IOException {
        Path testPath = testFolder.toPath();
        if (!(testPath.toFile().exists()))
            testPath.toFile().mkdirs();
        Files.walk(testPath).filter(path -> path.toFile().isFile())
                .filter(path -> !(path.toString().contains("openjfx")))
                .filter(path -> !(path.toString().contains(".log")))
                .map(path -> path.toFile())
                .forEach(file -> deleteFile(file));

    }

    private static void deleteFile(File file) {
     //   System.out.println(file.getAbsolutePath());
        file.delete();
    }

}
