package net.script.view.managers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import net.script.config.mapping.EntitiesMapper;
import net.script.data.dto.PersonDto;
import net.script.data.entities.Person;
import net.script.data.repositories.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
public class ReadDatabaseTask extends Service<List<PersonDto>> {

    private Supplier<List<PersonDto>> supplier;
    private PersonRepository repository;
    private EntitiesMapper mapper;

    public ReadDatabaseTask(Supplier<List<PersonDto>> personDtoSupplier, EntitiesMapper entitiesMapper) {
        this.supplier = personDtoSupplier;
        this.mapper = entitiesMapper;
    }

    public ReadDatabaseTask(PersonRepository personRepository, EntitiesMapper entitiesMapper) {
        this.repository = personRepository;
        this.mapper = entitiesMapper;
    }

    @Override
    protected Task<List<PersonDto>> createTask() {
        return new Task<List<PersonDto>>() {
            @Override
            protected List<PersonDto> call() throws Exception {
                log.info("Entering getAll call.");
                if (supplier != null) {
                    return supplier.get();
                } else {
                    Iterable<Person> allPeople = repository.findAll();
                    List<PersonDto> list =  new ArrayList<>();
                    allPeople.forEach((e) -> list.add(mapper.mapToPersonDto(e)));
                    log.info("Returning all people's list.");
                    return list;
                }
            }
        };
    }
}

