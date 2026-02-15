package app;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;

public class ProductLabel  {

	public ReadOnlyObjectWrapper<ProductGroup> group;

	public ReadOnlyStringProperty productId;
	public ReadOnlyStringProperty description; 
	public ReadOnlyStringProperty weight;
	public SimpleBooleanProperty printed;
	
	public ProductLabel(ProductGroup group, String productId, String description, String weight, boolean printed ) {
		this.group = new ReadOnlyObjectWrapper<ProductGroup>(group);
		this.productId = new ReadOnlyStringWrapper(productId);
		this.description = new ReadOnlyStringWrapper(description);
		this.weight = new ReadOnlyStringWrapper(weight);
		this.printed = new SimpleBooleanProperty(printed);
	}	
	
	public ProductGroup getGroup() {
		return group.get();
	}
	
	public String getProductId() {
		return productId.get();
	}

	
	public String getDescription() {
		return description.get();
	}

	
	public String getWeight() {
		return weight.get();
	}


	public Boolean getPrinted() {
		return printed.get();
	}


	public void setPrinted(Boolean printed) {
		this.printed.set(printed);
	}


	
}
