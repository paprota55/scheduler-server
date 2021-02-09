package pl.rafalpaprota.schedulerserver.repositories;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.rafalpaprota.schedulerserver.model.Block;
import pl.rafalpaprota.schedulerserver.model.User;

import java.util.List;

@Repository
public interface BlockRepository extends CrudRepository<Block, Long> {
    Block findByBlockNameAndUser(String blockName, User user);

    List<Block> findAllByUser(User user);

    void deleteBlockByBlockNameAndUser(String blockName, User user);

}
