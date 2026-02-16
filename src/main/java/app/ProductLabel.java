package app;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

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
		this.printed = new SimpleBooleanProperty(printed) {

			@Override
			public void addListener(InvalidationListener listener) {
				// TODO Auto-generated method stub
				super.addListener(listener);
			}

			@Override
			public void removeListener(InvalidationListener listener) {
				// TODO Auto-generated method stub
				super.removeListener(listener);
			}

			@Override
			public void addListener(ChangeListener<? super Boolean> listener) {
				// TODO Auto-generated method stub
				super.addListener(listener);
			}

			@Override
			public void removeListener(ChangeListener<? super Boolean> listener) {
				// TODO Auto-generated method stub
				super.removeListener(listener);
			}

			@Override
			protected void fireValueChangedEvent() {
				// TODO Auto-generated method stub
				super.fireValueChangedEvent();
			}

			@Override
			protected void invalidated() {
				// TODO Auto-generated method stub
				super.invalidated();
			}

			@Override
			public boolean get() {
				// TODO Auto-generated method stub
				return super.get();
			}

			@Override
			public void set(boolean newValue) {
				// TODO Auto-generated method stub
				super.set(newValue);
			}

			@Override
			public boolean isBound() {
				// TODO Auto-generated method stub
				return super.isBound();
			}

			@Override
			public void bind(ObservableValue<? extends Boolean> rawObservable) {
				// TODO Auto-generated method stub
				super.bind(rawObservable);
			}

			@Override
			public void unbind() {
				// TODO Auto-generated method stub
				super.unbind();
			}

			@Override
			public String toString() {
				// TODO Auto-generated method stub
				return super.toString();
			}

			@Override
			public void setValue(Boolean v) {
				// TODO Auto-generated method stub
				super.setValue(v);
			}
			
		};
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
		if (printed)
			System.out.println("printed");
		this.printed.set(printed);
	}

	 public BooleanProperty printedProperty() { return printed; }

	
}
