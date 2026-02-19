package app;

import java.util.prefs.Preferences;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Callback;

public class Model {

    private static final Model instance = new Model();

    public static Model getInstance() {
        return instance;
    }

    public Preferences preferences;
    // Define the extractor callback
    Callback<ProductLabel, Observable[]> extractor = pl -> new Observable[] { pl.printedProperty()};
    public ObservableList<ProductLabel> productLabels = FXCollections.observableArrayList(extractor);

    private Model() {
        super();
        preferences = Preferences.userNodeForPackage(getClass());
    }

}
