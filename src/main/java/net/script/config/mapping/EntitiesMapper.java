package net.script.config.mapping;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import net.script.data.dto.PersonDto;
import net.script.data.entities.Person;
import org.springframework.stereotype.Component;

@Component
public class EntitiesMapper {

    public PersonDto mapToPersonDto(Person e) {
        return new PersonDto(
                new SimpleLongProperty(e.getId()),
                new SimpleStringProperty(e.getName()),
                new SimpleStringProperty(e.getLastName())
        );
    }

    public Person mapToPerson(PersonDto e) {
        return new Person(
                e.getId().get(),
                e.getName().get(),
                e.getLastName().get()
        );
    }
}
