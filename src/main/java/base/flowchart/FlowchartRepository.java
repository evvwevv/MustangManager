package base.flowchart;

import base.user.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FlowchartRepository extends CrudRepository<Flowchart, Long> {
    List<Flowchart> findByUser(User user);
}
