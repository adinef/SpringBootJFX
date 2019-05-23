package net.script.view;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.cells.editors.IntegerTextFieldEditorBuilder;
import com.jfoenix.controls.cells.editors.TextFieldEditorBuilder;
import com.jfoenix.controls.cells.editors.base.EditorNodeBuilder;
import com.jfoenix.controls.cells.editors.base.GenericEditableTreeTableCell;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import net.script.Main;
import net.script.config.mapping.EntitiesMapper;
import net.script.data.dto.PersonDto;
import net.script.data.entities.Person;
import net.script.data.repositories.PersonRepository;
import net.script.utils.CommonFXUtils;
import net.script.view.services.ReadDatabaseTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

@Controller
@Slf4j
public class MainController implements Initializable {

    private boolean isFullscreen;

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
        Service<List<PersonDto>> readAllTask = new ReadDatabaseTask(this.personRepository, mapper);
        readAllTask.setOnSucceeded((e) -> {
            this.peoplePopulated = FXCollections.observableList(readAllTask.getValue());
            this.populateTableView(this.peoplePopulated, Arrays.asList(idCol, nameCol, lastNameCol));
            CommonFXUtils.noDataPopup(
                    "Data loaded",
                    "Data loaded successfully.",
                    button.getScene()
            );
        });
        readAllTask.setOnFailed((e) -> {
            log.error(readAllTask.getMessage() + readAllTask.getException().getMessage());
            CommonFXUtils.noDataPopup(
                    "Data not loaded",
                    "Error occurred during data load time. Check the logs for more information.",
                    button.getScene()
            );
        });
        readAllTask.start();
        //END OF DATA LOADING
    }

    private <T> void onEdit(TreeTableColumn.CellEditEvent<PersonDto, T> e, BiConsumer<PersonDto, T> propSet) {
        PersonDto personDto = e.getTreeTableView().getTreeItem(e.getTreeTablePosition().getRow()).getValue();
        propSet.accept(personDto, e.getNewValue());
        // Instant saving
        Platform.runLater(() -> {
            Person personMapped = this.mapper.mapToPerson(personDto);
            this.personRepository.save(personMapped);
        });
    }

    @FXML
    private void saveAll() {
        List<Person> peopleList = new LinkedList<>();
        this.peoplePopulated.forEach(
                (e) -> peopleList.add(this.mapper.mapToPerson(e))
        );
        this.personRepository.saveAll(peopleList);
    }

    private <T> JFXTreeTableColumn<PersonDto, T> prepColumn(
            String name,
            Callback<TreeTableColumn.CellDataFeatures<PersonDto, T>, ObservableValue<T>> callback,
            EventHandler<TreeTableColumn.CellEditEvent<PersonDto, T>> onEdit,
            EditorNodeBuilder<? extends java.io.Serializable> nodeEditorBuilder) {
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

    @FXML
    private void restoreWindow() {
        Main.getCurrentStage().setFullScreen(!isFullscreen);
        isFullscreen = !isFullscreen;
    }

    @FXML
    private void minimizeWindow() {
        Main.getCurrentStage().setIconified(true);
    }

    @FXML
    private void closeWindow() {
        Main.getCurrentStage().close();
        Platform.exit();
    }

    public void barClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount()  == 2) {
            restoreWindow();
        }
    }

    @FXML
    private void about() {
        CommonFXUtils.noDataPopup(
                "Author",
                "Project created by Adrian Fijałkowski on MIT Licence. " +
                "Feel free to use it for your own purposes.",
                this.button.getScene()
        );
    }
}
