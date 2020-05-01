package base.course;

import base.CatalogParser;
import base.flowchart.FlowchartRepository;
import base.quarter.QuarterRepository;
import base.security.CurrentUser;
import base.user.User;
import base.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/course")
public class CourseController {

    private static final Logger LOGGER = Logger.getLogger(CourseController.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FlowchartRepository flowchartRepository;

    @Autowired
    private QuarterRepository quarterRepository;

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping
    public ResponseEntity<List<Course>> listAll() {
        List<Course> courses = new ArrayList<>();
        courseRepository.findAll().forEach(courses::add);
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }

    @PostMapping("search")
    public ResponseEntity<List<Course>> findCourse(@RequestBody Course input) {
        input.setName(input.getName().replaceAll("\\s+","").toUpperCase());

        List<Course> courses = courseRepository.findByNameStartingWith(input.getName());

        return courses == null ? new ResponseEntity<>(HttpStatus.NOT_FOUND) : new ResponseEntity<>(courses, HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<Course> find(@PathVariable Long id) {
        Course course = courseRepository.findOne(id);
        return course == null ? new ResponseEntity<>(HttpStatus.NOT_FOUND) : new ResponseEntity<>(course, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Course> create(@RequestBody Course input, @CurrentUser UserDetails curUser) {
        Course course = new Course();
        if (User.isAdmin(curUser)) {
            course.setPrefix(input.getPrefix());
            course.setSuffix(input.getSuffix());
            course.setName(input.getName());
            course.setTitle(input.getTitle());
            course.setUnits(input.getUnits());
            course.setPrerequisites(input.getPrerequisites());
            course.setDescription(input.getDescription());
            course.setTermsOffered(input.getTermsOffered());
        }

        return !User.isAdmin(curUser) ? new ResponseEntity<>(HttpStatus.UNAUTHORIZED) :
                new ResponseEntity<>(courseRepository.save(course), HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable Long id, @CurrentUser UserDetails curUser) {
        if (User.isAdmin(curUser)) {
            courseRepository.delete(id);
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<Course> update(@PathVariable Long id, @RequestBody Course input, @CurrentUser UserDetails curUser) {
        Course course = courseRepository.findOne(id);

        if (course != null && User.isAdmin(curUser)) {
            course.setPrefix(input.getPrefix());
            course.setSuffix(input.getSuffix());
            course.setName(input.getName());
            course.setTitle(input.getTitle());
            course.setUnits(input.getUnits());
            course.setPrerequisites(input.getPrerequisites());
            course.setDescription(input.getDescription());
            course.setTermsOffered(input.getTermsOffered());
        }
        if (!User.isAdmin(curUser)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } else if (course == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(courseRepository.save(course), HttpStatus.OK);
        }
    }

}
