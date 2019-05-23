package net.script.view;

import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Platform;
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
import net.script.config.mapping.EntitiesMapper;
import net.script.data.dto.PersonDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import net.script.data.entities.Person;
import net.script.data.repositories.PersonRepository;
import net.script.utils.CommonFXUtils;

import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Controller
public class MainController implements Initializable {

    private final PersonRepository personRepository;
    private final EntitiesMapper mapper;

    @FXML
    private GridPane grid;

    @FXML
    private JFXButton button;

    @FXML
    private JFXTreeTableView<PersonDto> tableView;

    @FXML
    private VBox vBox;

    @Autowired
    public MainController(PersonRepository personRepository, EntitiesMapper mapper) {
        this.personRepository = personRepository;
        this.mapper = mapper;
    }

    @FXML
    public void initialize() {
        // initialize your data here
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.initialize();
    }

    @FXML
    public void loadData() {
        JFXTreeTableColumn<PersonDto, String> idCol = prepColumn(
                "Id",
                (c) -> c.getValue().getValue().getId()
        );
        idCol.setPrefWidth(200);
        JFXTreeTableColumn<PersonDto, String> nameCol = prepColumn(
                "Name",
                (c) -> c.getValue().getValue().getName()
        );
        nameCol.setPrefWidth(200);
        JFXTreeTableColumn<PersonDto, String> lastNameCol = prepColumn(
                "LastName",
                (c) -> c.getValue().getValue().getLastName()
        );
        lastNameCol.setPrefWidth(200);
        //DATA LOADING, MAY TAKE LONG:
        JFXSpinner spinner = new JFXSpinner();
        spinner.setRadius(20);
        grid.getChildren().add(spinner);
        ObservableList<PersonDto> items = extractFromResult(personRepository::findAll);
        items.forEach(System.out::println);
        Platform.runLater(
                () -> this.populateTableView(items, Arrays.asList(idCol, nameCol, lastNameCol))
        );
        grid.getChildren().remove(spinner);
        //END OF DATA LOADING
        CommonFXUtils.noDataPopup(
                "Data loaded",
                "Data loaded successfully.",
                button.getScene()
        );
    }

    private ObservableList<PersonDto> extractFromResult(Supplier<Iterable<Person>> supplier) {
        List<PersonDto> elems = new LinkedList<>();
        supplier
                .get()
                .forEach((e) -> {
                    elems.add(this.mapper.mapToPersonDto(e));
                });
        return FXCollections.observableList(elems);
    }

    private JFXTreeTableColumn<PersonDto, String> prepColumn(
            String name,
            Callback<TreeTableColumn.CellDataFeatures<PersonDto, String>, ObservableValue<String>> callback) {
        JFXTreeTableColumn<PersonDto, String> col = new JFXTreeTableColumn<>(name);
        col.setCellValueFactory(callback);
        return col;
    }

    private void populateTableView(ObservableList<PersonDto> items, List<JFXTreeTableColumn<PersonDto, String>> columns) {
        final TreeItem<PersonDto> root = new RecursiveTreeItem<>(items, RecursiveTreeObject::getChildren);
        tableView.setRoot(root);
        tableView.setShowRoot(false);
        tableView.getColumns().setAll(columns);
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
