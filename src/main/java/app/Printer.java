package app;

import java.nio.charset.StandardCharsets;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;

public class Printer {
	
	private String printerName;
	
	public Printer(String printerName) {
		super();
		this.printerName = printerName;
	}

	private String formatString(int xoffset, String group, String productCode, String weight, String upcCode) {
		String zpl = "^XA\n" // use default font
				+ "^CF,20\n" // set default font 0 to 30 dots high
				+ "^FO" + Integer.toString(xoffset) + ",30,0" // field origin x,y, "
				+ "^FD" + group // string to print
				+ "^FS" + "^FO" + Integer.toString(xoffset) + ",70,0" // field origin x,y, "// end of field
				+ "^FD" + productCode // string to print
				+ "^FS\n" // end of field
				+ "^BY3,2,150\n" // barcode module width, wide bar ratio, barcode height
				+ "^FO" + Integer.toString(80) + ",120,2" // barcode field origin x,y, "
				+ "^BUN,120,Y,N,N,N\n" // barcode UPC-A, height 100, print interpretation line, print above, no check
										// digit
				+ "^FD" + upcCode // barcode data + end of field
				+ "^FS\n" + "^FO" + Integer.toString(xoffset) + ",280,0" // field origin x,y, "// end of field
				+ "^FD" + weight // string to print
				+ "^XZ\n";
		// end of label format
		return zpl;
	}
	
	public PrintService findPrintService() {
		PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
		for (PrintService s : services) {
			if (s.getName().toLowerCase().contains(printerName.toLowerCase())) {
				return s;
			}
		}
		throw new RuntimeException("Print service for " + printerName + " not found!");
	}
	
	public void print(String content, String group, String weightStr, String description) {
	    try {
			String zpl = formatString(78, group, description, weightStr, content);
			PrintService ps = findPrintService();
			
			DocPrintJob job = ps.createPrintJob();
			byte[] bytes = zpl.getBytes(StandardCharsets.UTF_8);
			Doc doc = new SimpleDoc(bytes, DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
			PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
			attrs.add(new Copies(1));
			job.print(doc, attrs);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}


}
