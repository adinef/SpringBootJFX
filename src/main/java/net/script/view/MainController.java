package net.script.view;

import com.jfoenix.controls.*;
import com.jfoenix.controls.cells.editors.IntegerTextFieldEditorBuilder;
import com.jfoenix.controls.cells.editors.TextFieldEditorBuilder;
import com.jfoenix.controls.cells.editors.base.EditorNodeBuilder;
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
        JFXTreeTableColumn<PersonDto, Long> idCol = prepColumn(
                "Id",
                (c) -> c.getValue().getValue().getId().asObject(),
                (e) -> onEdit(e, (p, nV) -> p.getId().setValue(nV)),
                new IntegerTextFieldEditorBuilder()
        );
        JFXTreeTableColumn<PersonDto, String> nameCol = prepColumn(
                "Name",
                (c) -> c.getValue().getValue().getName(),
                (e) -> onEdit(e, (p, nV) -> p.getName().setValue(nV)),
                new TextFieldEditorBuilder()
        );
        JFXTreeTableColumn<PersonDto, String> lastNameCol = prepColumn(
                "LastName",
                (c) -> c.getValue().getValue().getLastName(),
                (e) -> onEdit(e, (p, nV) -> p.getLastName().setValue(nV)),
                new TextFieldEditorBuilder()
        );
        //DATA LOADING, MAY TAKE LONG:
        fetchDataFromDb(
                () -> extractFromResult(personRepository::findAll),
                Arrays.asList(idCol, nameCol, lastNameCol)
        );
        //END OF DATA LOADING
        CommonFXUtils.noDataPopup(
                "Data loaded",
                "Data loaded successfully.",
                button.getScene()
        );
    }

    private void fetchDataFromDb(Supplier<ObservableList<PersonDto>> personDtoSupplier,
                                 List<JFXTreeTableColumn<PersonDto, ?>> cols) {
        peoplePopulated = personDtoSupplier.get();
        Platform.runLater(
                () -> this.populateTableView(peoplePopulated, cols)
        );
    }

    private <T> void onEdit(TreeTableColumn.CellEditEvent<PersonDto, T> e, BiConsumer<PersonDto, T> propSet) {
        PersonDto personDto = e.getTreeTableView().getTreeItem(e.getTreeTablePosition().getRow()).getValue();
        propSet.accept(personDto, e.getNewValue());
        // Instant saving
        Person personMapped = this.mapper.mapToPerson(personDto);
        this.personRepository.save(personMapped);
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

    private <T> JFXTreeTableColumn<PersonDto, T> prepColumn(
            String name,
            Callback<TreeTableColumn.CellDataFeatures<PersonDto, T>, ObservableValue<T>> callback,
            EventHandler<TreeTableColumn.CellEditEvent<PersonDto, T>> onEdit,
            EditorNodeBuilder nodeEditorBuilder) {
        JFXTreeTableColumn<PersonDto, T> col = new JFXTreeTableColumn<>(name);
        col.setPrefWidth(200);
        col.setCellValueFactory(callback);
        col.setCellFactory((TreeTableColumn<PersonDto, T> param) -> new GenericEditableTreeTableCell<>(
                nodeEditorBuilder));
        col.setOnEditCommit(onEdit);
        col.setEditable(true);
        return col;
    }

    private void populateTableView(ObservableList<PersonDto> items, List<JFXTreeTableColumn<PersonDto, ?>> columns) {
        final TreeItem<PersonDto> root = new RecursiveTreeItem<>(items, RecursiveTreeObject::getChildren);
        tableView.setRoot(root);
        tableView.setShowRoot(false);
        tableView.getColumns().setAll(columns);
    }
}
