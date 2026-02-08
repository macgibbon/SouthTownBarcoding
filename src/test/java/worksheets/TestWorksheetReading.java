package worksheets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import app.Model;

class TestWorksheetReading {

	Path worksheetsPath = Path.of("spreadsheets");

	@Test
	void test() {
		try {
			File firstworksheet = Files.list(worksheetsPath)
					.sorted()
					.findFirst()
					.get()
					.toFile();
			System.out.println("Reading " + firstworksheet.getName());
			
			Model model = Model.getInstance();
			model.setWorksheetForLabels(firstworksheet);
			List productLabels = model.productLabels;
			assertTrue(productLabels.size()>1);
		} catch (Throwable e) {
			fail(e.getMessage());
			e.printStackTrace();
		}

	}

	

}
