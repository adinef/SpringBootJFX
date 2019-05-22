package sample.data.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import sample.data.entities.Person;

@Repository
public interface PersonRepository extends CrudRepository<Person, Long> {
}
