package app;


import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.print.PrinterJob;
import javafx.stage.Window;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

public class MainController {

    @FXML
    private Label messageLabel;

    @FXML
    private TextField barcodeField;

    @FXML
    private ImageView barcodeView;

    @FXML
    private TextField upcNumberSystem;

    @FXML
    private TextField manufacturerField;

    @FXML
    private TextField upcWeightField;

    @FXML
    private javafx.scene.layout.HBox upcEmbeddedBox;

    @FXML
    private javafx.scene.layout.HBox gs1Box;

    @FXML
    private TextField gtinField;

    @FXML
    private TextField gs1WeightField;

    @FXML
    private ChoiceBox<Integer> gs1DecimalsChoice;

    @FXML
    private void initialize() {


        // Set sensible defaults for embedded controls
        upcNumberSystem.setText("2"); // 2 often used for variable-weight UPCs

        // GS1 decimals choice (0..6), default 3
        for (int i = 0; i <= 6; i++) {
            gs1DecimalsChoice.getItems().add(i);
        }
        gs1DecimalsChoice.setValue(3);
    }

    @FXML
    private void onGenerateClicked(ActionEvent event) {
    	try {
                handleUPCEmbedded();

        } catch (WriterException we) {
            we.printStackTrace();
            messageLabel.setText("Error generating barcode: " + we.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            messageLabel.setText("Unexpected error: " + ex.getMessage());
        }
    }

    @FXML
    private void onPrintClicked(ActionEvent event) {
        Image image = barcodeView.getImage();
        if (image == null) {
            messageLabel.setText("No barcode to print. Generate one first.");
            return;
        }

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            messageLabel.setText("No printer job available.");
            return;
        }

        // Show print dialog attached to the current window
        Window window = messageLabel.getScene().getWindow();
        boolean proceed = job.showPrintDialog(window);
        if (!proceed) {
            messageLabel.setText("Print cancelled.");
            return;
        }

        // Print the image node (ImageView)
        boolean printed = job.printPage(barcodeView);
        if (printed) {
            job.endJob();
            messageLabel.setText("Printed successfully.");
        } else {
            messageLabel.setText("Printing failed.");
        }
    }

    private void handleUPCEmbedded() throws WriterException {
        String ns = upcNumberSystem.getText();
        if (ns == null || ns.isBlank()) ns = "2";
        ns = ns.trim();
        if (!ns.matches("\\d")) {
            messageLabel.setText("Number system must be a single digit (0-9).");
            return;
        }

        String manu = manufacturerField.getText();
        if (manu == null) manu = "";
        manu = manu.trim();
        if (!manu.matches("\\d{1,5}")) {
            messageLabel.setText("Manufacturer must be 1 to 5 digits.");
            return;
        }
        manu = leftPad(manu, 5);

        String weightStr = upcWeightField.getText();
        if (weightStr == null) weightStr = "";
        weightStr = weightStr.trim();
        if (!weightStr.matches("\\d{1,5}")) {
            messageLabel.setText("Weight must be 1 to 5 digits (grams).");
            return;
        }
        weightStr = leftPad(weightStr, 5);

        // Build 11-digit payload: ns + manu(5) + weight(5)
        String payload = ns + manu + weightStr; // length should be 11
        if (payload.length() != 11 || !payload.matches("\\d{11}")) {
            messageLabel.setText("Constructed UPC-A payload invalid: " + payload);
            return;
        }
        int check = computeUPCACheckDigit(payload);
        String content = payload + check; // full 12-digit UPC-A
        generateAndShowBarcode(content, BarcodeFormat.UPC_A, 360, 120);
        messageLabel.setText("UPC-A (weight) generated: " + content);
    }

    private void handleGS1() throws WriterException {
        // Read and normalize GTIN
        String gtin = gtinField.getText();
        if (gtin == null) gtin = "";
        gtin = gtin.trim();
        if (!gtin.matches("\\d{1,14}")) {
            messageLabel.setText("GTIN must be 1 to 14 digits.");
            return;
        }
        gtin = leftPad(gtin, 14); // pad to 14 digits

        // Read decimals for AI 310n
        Integer decimals = gs1DecimalsChoice.getValue();
        if (decimals == null || decimals < 0 || decimals > 6) {
            messageLabel.setText("Decimals must be between 0 and 6.");
            return;
        }

        // Read weight in kilograms (decimal allowed)
        String weightKgStr = gs1WeightField.getText();
        if (weightKgStr == null || weightKgStr.isBlank()) {
            messageLabel.setText("Enter a weight in kilograms (e.g. 1.234).");
            return;
        }
        weightKgStr = weightKgStr.trim();
        BigDecimal weightKg;
        try {
            weightKg = new BigDecimal(weightKgStr);
            if (weightKg.signum() < 0) {
                messageLabel.setText("Weight must be non-negative.");
                return;
            }
        } catch (NumberFormatException nfe) {
            messageLabel.setText("Invalid weight format.");
            return;
        }

        // Multiply by 10^decimals and round to integer (should be exact for typical inputs)
        BigDecimal scaled = weightKg.movePointRight(decimals).setScale(0, RoundingMode.HALF_UP);
        long weightInt;
        try {
            weightInt = scaled.longValueExact();
        } catch (ArithmeticException ae) {
            messageLabel.setText("Weight value out of range or not an exact integer after scaling.");
            return;
        }

        // AI 310n requires 6-digit numeric field (leading zeros)
        if (weightInt < 0 || weightInt > 999999L) {
            messageLabel.setText("Scaled weight must be between 0 and 999999 for AI 310n.");
            return;
        }
        String weightField = String.format("%06d", weightInt);

        // Build GS1-128 payload:
        // FNC1 prefix + (01)GTIN + (310n)weight
        // For encoding we use the FNC1 character '\u00f1' as the initial char ZXing recognizes.
        // The content passed to ZXing: FNC1 + "01" + gtin14 + "310" + decimals + weight6
        String fnc1 = "\u00f1";
        String ai01 = "01";
        String ai310prefix = "310" + decimals; // e.g., 3103
        String content = fnc1 + ai01 + gtin + ai310prefix + weightField;

        // Generate CODE_128; GS1-128 is represented by CODE_128 with FNC1 prefix.
        generateAndShowBarcode(content, BarcodeFormat.CODE_128, 480, 120);
        messageLabel.setText("GS1-128 generated (AI01 + AI310" + decimals + "): GTIN=" + gtin + " weightScaled=" + weightField);
    }

    // Helper to generate barcode using ZXing and set it into the ImageView
    private void generateAndShowBarcode(String text, com.google.zxing.BarcodeFormat format, int width, int height) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        // minimal margin
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix bitMatrix = new MultiFormatWriter().encode(text, format, width, height, hints);
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
        barcodeView.setImage(fxImage);
    }

    // Compute UPC-A check digit for 11-digit string
    private int computeUPCACheckDigit(String digits11) {
        if (digits11 == null || digits11.length() != 11 || !digits11.matches("\\d{11}")) {
            throw new IllegalArgumentException("UPC-A payload must be 11 digits");
        }
        int sumOdd = 0;  // positions 1,3,5,7,9,11 (index 0,2,...,10)
        int sumEven = 0; // positions 2,4,6,8,10 (index 1,3,5,7,9)
        for (int i = 0; i < 11; i++) {
            int d = digits11.charAt(i) - '0';
            if ((i % 2) == 0) {
                sumOdd += d;
            } else {
                sumEven += d;
            }
        }
        int total = sumOdd * 3 + sumEven;
        int mod = total % 10;
        return (10 - mod) % 10;
    }

    // Left pad with zeros to requested length
    private String leftPad(String s, int len) {
        if (s == null) s = "";
        if (s.length() >= len) return s;
        StringBuilder sb = new StringBuilder();
        for (int i = s.length(); i < len; i++) sb.append('0');
        sb.append(s);
        return sb.toString();
    }
    
	
}