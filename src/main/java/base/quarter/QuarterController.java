package base.quarter;

import base.course.Course;
import base.course.CourseRepository;
import base.flowchart.Flowchart;
import base.flowchart.FlowchartRepository;
import base.security.CurrentUser;
import base.user.User;
import base.user.UserRepository;
import base.year.Year;
import base.year.YearRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/quarter")
public class QuarterController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FlowchartRepository flowchartRepository;

    @Autowired
    private QuarterRepository quarterRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private YearRepository yearRepository;

    @GetMapping
    public ResponseEntity<List<Quarter>> listAll(@CurrentUser UserDetails curUser) {
        final ArrayList<Quarter> quarters = new ArrayList<>();
        if (User.isAdmin(curUser)) {
            quarterRepository.findAll().forEach(quarters::add);
        }
        return User.isAdmin(curUser) ? new ResponseEntity<>(quarters, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("{id}")
    public ResponseEntity<Quarter> find(@PathVariable Long id, @CurrentUser UserDetails curUser) {
        final User user = userRepository.findByEmail(curUser.getUsername());

        if (user != null && (User.isAdmin(curUser) || user.hasQuarter(id))) {
            Quarter quarter = quarterRepository.findOne(id);
            if (quarter != null) {
                return new ResponseEntity<>(quarterRepository.findOne(id), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @PostMapping
    public ResponseEntity<Quarter> create(@CurrentUser UserDetails curUser, @RequestBody Quarter input) {
        final User user = userRepository.findByEmail(curUser.getUsername());

        if (user != null) {
            Quarter quarter = new Quarter();
            quarter.setQuarterName(input.getQuarterName());
            //temporary, will need to be reworked
            Flowchart flowchart = flowchartRepository.findByUser(user).get(0);
            Year year = flowchart.getYears().get(0);
            quarter.setYear(year);

            year.addQuarter(quarter);

            quarterRepository.save(quarter);

            return new ResponseEntity<>(quarter, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable Long id, @CurrentUser UserDetails curUser) {
        if (userRepository.findByEmail(curUser.getUsername()).hasQuarter(id)) {
            Quarter quarter = quarterRepository.findOne(id);
            Year year = quarter.getYear();
            List<Course> courses = quarter.getCourses();
            while (!courses.isEmpty()) {
                Course curCourse = courses.get(0);
                quarter.removeCourse(courses.get(0));
                curCourse.removeQuarter(quarter);
            }
            year.getQuarters().remove(quarter);
            quarterRepository.delete(id);
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<Quarter> update(@PathVariable Long id, @RequestBody Quarter input, @CurrentUser UserDetails curUser) {
        Quarter quarter = quarterRepository.findOne(id);
        User user = userRepository.findByEmail(curUser.getUsername());

        if (quarter == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (user != null && user.hasQuarter(id)) {
            quarter.setQuarterName(input.getQuarterName());
            quarterRepository.save(quarter);

            return new ResponseEntity<>(quarter, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PutMapping("/deleteCourse/{id}")
    public ResponseEntity<Quarter> deleteCourse(@CurrentUser UserDetails curUser, @RequestBody Course input, @PathVariable long id) {
        final User user = userRepository.findByEmail(curUser.getUsername());
        final Quarter quarter = quarterRepository.findOne(id);

        if (user.hasQuarter(id)) {
            Course course = courseRepository.findByName(input.getName());

            quarter.removeCourse(course);
            course.removeQuarter(quarter);

            quarterRepository.save(quarter);

            return new ResponseEntity<>(quarter, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PutMapping("/addCourse/{id}")
    public ResponseEntity<Quarter> addCourse(@CurrentUser UserDetails curUser, @RequestBody Course input, @PathVariable long id) {
        final User user = userRepository.findByEmail(curUser.getUsername());
        final Quarter quarter = quarterRepository.findOne(id);

        if (user.hasQuarter(id)) {
            Course course = courseRepository.findByName(input.getName());

            if(!quarter.hasCourse(course)) {
                quarter.addCourse(course);
                course.addQuarter(quarter);

                quarterRepository.save(quarter);

                return new ResponseEntity<>(quarter, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }

        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
