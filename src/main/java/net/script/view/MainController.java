package net.script.view;

import com.jfoenix.controls.*;
import com.jfoenix.controls.cells.editors.TextFieldEditorBuilder;
import com.jfoenix.controls.cells.editors.base.GenericEditableTreeTableCell;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Controller
public class MainController implements Initializable {

    private final PersonRepository personRepository;
    private final EntitiesMapper mapper;
    private ObservableList<PersonDto> peoplePopulated = FXCollections.emptyObservableList();

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
        this.tableView.setEditable(true);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.initialize();
    }

    @FXML
    private void loadData() {
        JFXTreeTableColumn<PersonDto, String> idCol = prepColumn(
                "Id",
                (c) -> c.getValue().getValue().getId(),
                (e) -> onEdit(e, (p, nV) -> p.getId().setValue(nV))
        );
        idCol.setPrefWidth(200);
        JFXTreeTableColumn<PersonDto, String> nameCol = prepColumn(
                "Name",
                (c) -> c.getValue().getValue().getName(),
                (e) -> onEdit(e, (p, nV) -> p.getName().setValue(nV))
        );
        nameCol.setPrefWidth(200);
        JFXTreeTableColumn<PersonDto, String> lastNameCol = prepColumn(
                "LastName",
                (c) -> c.getValue().getValue().getLastName(),
                (e) -> onEdit(e, (p, nV) -> p.getLastName().setValue(nV))
        );
        lastNameCol.setPrefWidth(200);
        //DATA LOADING, MAY TAKE LONG:
        JFXSpinner spinner = new JFXSpinner();
        spinner.setRadius(20);
        grid.getChildren().add(spinner);
        peoplePopulated = extractFromResult(personRepository::findAll);
        Platform.runLater(
                () -> this.populateTableView(peoplePopulated, Arrays.asList(idCol, nameCol, lastNameCol))
        );
        grid.getChildren().remove(spinner);
        //END OF DATA LOADING
        CommonFXUtils.noDataPopup(
                "Data loaded",
                "Data loaded successfully.",
                button.getScene()
        );
    }

    private void onEdit(TreeTableColumn.CellEditEvent<PersonDto, String> e, BiConsumer<PersonDto, String> propSet) {
        PersonDto personDto = e.getTreeTableView().getTreeItem(e.getTreeTablePosition().getRow()).getValue();
        propSet.accept(personDto, e.getNewValue());
    }

    @FXML
    private void saveAll() {
        List<Person> peopleList = new LinkedList<>();
        this.peoplePopulated.forEach(
                (e) -> peopleList.add(this.mapper.mapToPerson(e))
        );
        this.personRepository.saveAll(peopleList);
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
            Callback<TreeTableColumn.CellDataFeatures<PersonDto, String>, ObservableValue<String>> callback,
            EventHandler<TreeTableColumn.CellEditEvent<PersonDto, String>> onEdit) {
        JFXTreeTableColumn<PersonDto, String> col = new JFXTreeTableColumn<>(name);
        col.setCellValueFactory(callback);
        col.setCellFactory((TreeTableColumn<PersonDto, String> param) -> new GenericEditableTreeTableCell<>(
                new TextFieldEditorBuilder()));
        col.setOnEditCommit(onEdit);
        col.setEditable(true);
        return col;
    }

    private void populateTableView(ObservableList<PersonDto> items, List<JFXTreeTableColumn<PersonDto, String>> columns) {
        final TreeItem<PersonDto> root = new RecursiveTreeItem<>(items, RecursiveTreeObject::getChildren);
        tableView.setRoot(root);
        tableView.setShowRoot(false);
        tableView.getColumns().setAll(columns);
    }
}
