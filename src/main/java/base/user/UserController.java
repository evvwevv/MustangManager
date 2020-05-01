package base.user;

import base.course.Course;
import base.flowchart.Flowchart;
import base.flowchart.FlowchartRepository;
import base.quarter.Quarter;
import base.quarter.QuarterRepository;
import base.security.CurrentUser;
import base.year.Year;
import base.year.YearRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FlowchartRepository flowchartRepository;

    @Autowired
    private YearRepository yearRepository;

    @Autowired
    private QuarterRepository quarterRepository;

    @GetMapping
    public ResponseEntity<User> getCurUser(@CurrentUser UserDetails curUser) {
        if (userRepository.findByEmail(curUser.getUsername()) != null) {
            final User user = userRepository.findByEmail(curUser.getUsername());

            return new ResponseEntity<>(user, HttpStatus.OK);

        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<User> find(@PathVariable long id, @CurrentUser UserDetails curUser) {
        final User reqUser = userRepository.findOne(id);
        if (reqUser == null || curUser == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (User.isAdmin(curUser) || curUser.getUsername().equals(reqUser.getEmail())) {
            return new ResponseEntity<>(reqUser, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @RequestMapping("/all")
    public ResponseEntity<List<User>> getUsers(@CurrentUser UserDetails curUser) {
        final ArrayList<User> users = new ArrayList<>();
        if (curUser != null && User.isAdmin(curUser)) {
            userRepository.findAll().forEach(users::add);
        }
        return (curUser != null && User.isAdmin(curUser)) ? new ResponseEntity<>(users, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User reqUser) {
        if (userRepository.findByEmail(reqUser.getEmail()) == null) {
            final User user = new User();
            user.setEmail(reqUser.getEmail());
            user.setFirstname(reqUser.getFirstname());
            user.setLastname(reqUser.getLastname());
            user.setPassword(new BCryptPasswordEncoder().encode(reqUser.getPassword()));
            userRepository.save(user);

            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable Long id, @CurrentUser UserDetails curUser) {
        User user = userRepository.findByEmail(curUser.getUsername());
        if (user != null && User.isAdmin(curUser)) {
            User removeUser = userRepository.findOne(id);
            List<Flowchart> flowcharts = removeUser.getFlowcharts();
            while (!flowcharts.isEmpty()) {
                List<Year> years = flowcharts.get(0).getYears();
                while (!years.isEmpty()) {
                    List<Quarter> quarters = years.get(0).getQuarters();
                    while (!quarters.isEmpty()) {
                        List<Course> courses = quarters.get(0).getCourses();
                        while (!courses.isEmpty()) {
                            Course curCourse = courses.get(0);
                            quarters.get(0).removeCourse(curCourse);
                            curCourse.removeQuarter(quarters.get(0));
                        }
                        Quarter curQuarter = quarters.get(0);
                        years.get(0).removeQuarter(0);
                        quarterRepository.delete(curQuarter);

                    }
                    Year curYear = years.get(0);
                    flowcharts.get(0).removeYear(years.get(0));
                    yearRepository.delete(curYear);
                }
                Flowchart flowchart = flowcharts.get(0);
                user.removeFlowchart(flowchart);
                flowcharts.remove(flowchart);
                flowchartRepository.delete(flowchart);
            }
            userRepository.delete(id);
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody User newUser, @CurrentUser UserDetails curUser) {
        final User reqUser = userRepository.findOne(id);
        if (reqUser != null && reqUser.getEmail().equals(curUser.getUsername())) {
            reqUser.setEmail(newUser.getEmail());
            reqUser.setFirstname(newUser.getFirstname());
            reqUser.setLastname(newUser.getLastname());
            reqUser.setPassword(new BCryptPasswordEncoder().encode(newUser.getPassword()));
            return new ResponseEntity<>(userRepository.save(reqUser), HttpStatus.OK);
        }
        return (reqUser != null && !reqUser.getEmail().equals(curUser.getUsername())) ? new ResponseEntity<>(HttpStatus.UNAUTHORIZED) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}