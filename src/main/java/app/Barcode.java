package app;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class Barcode {
	
	String weight;
	String product;
	
	
	public Barcode(String weight, String product) {
		super();
		this.weight = weight;
		this.product = product;
	}
	
	public String content() {
		BigDecimal weightd = null;
		try {
			weightd = new BigDecimal(weight);
			if (weightd.signum() < 0) {
				throw new IllegalArgumentException("Weight must be non-negative.");
			}

		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("Invalid weight format.");
		}
		// Multiply by 10^decimals and round to integer (should be exact for typical
		// inputs)
		BigDecimal scaled = weightd.movePointRight(3).setScale(0, RoundingMode.HALF_UP);
		String formattedWeight = String.format("%05d", scaled.toBigInteger());

		BigDecimal productCode = null;
		try {
			productCode = new BigDecimal(product);
			if (productCode.signum() <= 0) {
				throw new IllegalArgumentException("Product code must a positive integer. ");
			}
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("Invalid product code format.");
		}

		String formattedProductCode = String.format("%06d", productCode.toBigInteger());
		// Build 11-digit payload: ns + manu(5) + weight(5)
		String content = formattedProductCode + formattedWeight; // length should be 11
		if (content.length() != 11 || !content.matches("\\d{11}")) {
			throw new IllegalArgumentException("Constructed UPC-A payload invalid: " + content);
		}
		return content;
	}

	// Helper to generate barcode using ZXing and set it into the ImageView
	public Image image( com.google.zxing.BarcodeFormat format, int width, int height)
			throws WriterException {
		String text = content();
		Map<EncodeHintType, Object> hints = new HashMap<>();
		// minimal margin
		hints.put(EncodeHintType.MARGIN, 1);
		BitMatrix bitMatrix = new MultiFormatWriter().encode(text, format, width, height, hints);
		BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
		Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
		return fxImage;
		
	}

}
