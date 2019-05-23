package net.script.config.mapping;

import javafx.beans.property.SimpleStringProperty;
import net.script.data.dto.PersonDto;
import net.script.data.entities.Person;
import org.springframework.stereotype.Component;

@Component
public class EntitiesMapper {

    public PersonDto mapToPersonDto(Person e) {
        return new PersonDto(
                new SimpleStringProperty(e.getId().toString()),
                new SimpleStringProperty(e.getName()),
                new SimpleStringProperty(e.getLastName())
        );
    }

    public Person mapToPerson(PersonDto e) {
        return new Person(
                Long.parseLong(e.getId().get()),
                e.getName().get(),
                e.getLastName().get()
        );
    }
}
