package net.script.data.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import net.script.data.entities.Person;

@Repository
public interface PersonRepository extends CrudRepository<Person, Long> {
}
