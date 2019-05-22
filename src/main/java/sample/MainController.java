package sample;

import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import sample.data.entities.Person;
import sample.data.repositories.PersonRepository;
import sample.utils.CommonFXUtils;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Supplier;

@Controller
public class MainController implements Initializable {

    @Autowired
    private PersonRepository personRepository;

    @FXML
    private GridPane grid;

    @FXML
    private JFXButton button;

    @FXML
    private JFXTreeTableView tableView;

    @FXML
    private VBox vBox;

    @FXML
    public void initialize() {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.initialize();
    }

    @FXML
    public void loadData() {
        JFXTreeTableColumn<Person, String> idCol = prepColumn(
                "Id",
                (c) -> new SimpleStringProperty(c.getValue().getValue().getId().toString())
        );
        idCol.setPrefWidth(200);
        JFXTreeTableColumn<Person, String> nameCol = prepColumn(
                "Name",
                (c) -> new SimpleStringProperty(c.getValue().getValue().getName())
        );
        nameCol.setPrefWidth(200);
        JFXTreeTableColumn<Person, String> lastNameCol = prepColumn(
                "LastName",
                (c) -> new SimpleStringProperty(c.getValue().getValue().getLastName())
        );
        lastNameCol.setPrefWidth(200);
        //DATA LOADING, MAY TAKE LONG:
        JFXSpinner spinner = new JFXSpinner();
        grid.getChildren().add(spinner);
        ObservableList<Person> items = extractFromResult(personRepository::findAll);
        this.populateTableView(items, idCol, nameCol, lastNameCol);
        grid.getChildren().remove(spinner);
        //END OF DATA LOADING
        CommonFXUtils.noDataPopup(
                "Data loaded",
                "Data loaded successfully.",
                button.getScene()
        );
    }

    private ObservableList<Person> extractFromResult(Supplier<Iterable<Person>> supplier) {
        List<Person> elems = new LinkedList<>();
        supplier
                .get()
                .forEach(elems::add);
        return FXCollections.observableList(elems);
    }

    private JFXTreeTableColumn<Person, String> prepColumn(
            String name,
            Callback<TreeTableColumn.CellDataFeatures<Person, String>, ObservableValue<String>> callback) {
        JFXTreeTableColumn<Person, String> col = new JFXTreeTableColumn<>(name);
        col.setCellValueFactory(callback);
        return col;
    }

    private void populateTableView(ObservableList<Person> items, JFXTreeTableColumn<Person, String>... columns) {
        final TreeItem<Person> root = new RecursiveTreeItem<>(items, RecursiveTreeObject::getChildren);
        tableView.setRoot(root);
        tableView.setShowRoot(false);
        tableView.getColumns().setAll(columns);
    }
}
