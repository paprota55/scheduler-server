package pl.rafalpaprota.schedulerserver.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.rafalpaprota.schedulerserver.model.User;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    User findByLogin(String login);

    User deleteByLogin(String login);
}
