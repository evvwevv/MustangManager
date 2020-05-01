package base.quarter;

import base.year.Year;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface QuarterRepository extends CrudRepository<Quarter, Long> {
    List<Quarter> findByYear(Year year);
}
