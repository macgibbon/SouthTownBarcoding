package app;

import static app.ProductGroup.*;

public record ProductId(Integer id, ProductGroup productGroup, String description) {

	public static ProductId createProductId(Integer id, String fullDescription) {
		for (int i = 0; i < values().length; i++) {
			ProductGroup group = values()[i];
			if (fullDescription.startsWith(group.toString()))
				return new ProductId(id, group, fullDescription.substring(group.toString().length()));
		}
		return new ProductId(id, ProductGroup.__, fullDescription);
	}
}