package app;

public enum ProductGroup {
	Beef, Fresh_Pork, Smoked_Pork, __;

	@Override
	public String toString() {		
		return super.toString().replace('_', ' ').trim();
	}	

}
