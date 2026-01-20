package app;

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

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Window;

public class MainController {

	@FXML
	private Label messageLabel;
	
	@FXML 
	private Label contentLabel;

	@FXML
	private TextField barcodeField;

	@FXML
	private ImageView barcodeView;

	@FXML
	private TextField gs1WeightField;

	@FXML
	private TextField productCodeField;

	@FXML
	private void initialize() {
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
		try {
			handleUPCEmbedded();
		} catch (WriterException we) {
			we.printStackTrace();
			messageLabel.setText("Error generating barcode: " + we.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			messageLabel.setText("Unexpected error: " + ex.getMessage());
		}
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
		String weightStr = gs1WeightField.getText();
		BigDecimal weight = null;
		try {
			weight = new BigDecimal(weightStr);
			if (weight.signum() < 0) {
				messageLabel.setText("Weight must be non-negative.");
				return;
			}

		} catch (NumberFormatException nfe) {
			messageLabel.setText("Invalid weight format.");
			return;
		}
		// Multiply by 10^decimals and round to integer (should be exact for typical
		// inputs)
		BigDecimal scaled = weight.movePointRight(2).setScale(0, RoundingMode.HALF_UP);
		String formattedWeight = String.format("%05d", scaled.toBigInteger());

		String productStr = productCodeField.getText();
		BigDecimal productCode = null;
		try {
			productCode = new BigDecimal(productStr);
			if (productCode.signum() <= 0) {
				messageLabel.setText("Product code must a positive integer. ");
				return;
			}

		} catch (NumberFormatException nfe) {
			messageLabel.setText("Invalid product code format.");
			return;
		}

		String formattedProductCode = String.format("%06d", productCode.toBigInteger());
		int productcheck = computeUPCACheckDigit(formattedProductCode);
		
		int weightcheck = computeUPCACheckDigit(formattedWeight);
		// Build 11-digit payload: ns + manu(5) + weight(5)
		String content = formattedProductCode + formattedWeight; // length should be 11
		if (content.length() != 11 || !content.matches("\\d{11}")) {
			messageLabel.setText("Constructed UPC-A payload invalid: " + content);
			return;
		}
		
		generateAndShowBarcode(content, BarcodeFormat.UPC_A, 360, 120);
		messageLabel.setText("UPC-A (weight) generated: " + content);
		contentLabel.setText(content);
	}

	
	// Helper to generate barcode using ZXing and set it into the ImageView
	private void generateAndShowBarcode(String text, com.google.zxing.BarcodeFormat format, int width, int height)
			throws WriterException {
		Map<EncodeHintType, Object> hints = new HashMap<>();
		// minimal margin
		hints.put(EncodeHintType.MARGIN, 1);
		BitMatrix bitMatrix = new MultiFormatWriter().encode(text, format, width, height, hints);
		BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
		Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
		barcodeView.setImage(fxImage);
	}

	// Compute UPC-A check digit for 11-digit string
	private int computeUPCACheckDigit(String digits) {
		
		int sumOdd = 0; // positions 1,3,5,7,9,11 (index 0,2,...,10)
		int sumEven = 0; // positions 2,4,6,8,10 (index 1,3,5,7,9)
		for (int i = 0; i < digits.length(); i++) {
			int d = digits.charAt(i) - '0';
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

	

}