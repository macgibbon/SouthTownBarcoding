package app;

public enum ProductGroup {
	Beef, Fresh_Pork, Pork, Fully_Cooked_Beef, __;

	@Override
	public String toString() {		
		return super.toString().replace('_', ' ').trim();
	}	

}
