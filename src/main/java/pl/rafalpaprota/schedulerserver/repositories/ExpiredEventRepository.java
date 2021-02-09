package pl.rafalpaprota.schedulerserver.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.rafalpaprota.schedulerserver.model.ExpiredEvent;
import pl.rafalpaprota.schedulerserver.model.User;

import java.util.List;

@Repository
public interface ExpiredEventRepository extends CrudRepository<ExpiredEvent, Long> {
    List<ExpiredEvent> findAllByUser(User user);
}
