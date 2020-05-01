package base.flowchart;

import base.course.Course;
import base.quarter.Quarter;
import base.quarter.QuarterRepository;
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
@RequestMapping("/flowchart")
public class FlowchartController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FlowchartRepository flowchartRepository;

    @Autowired
    private YearRepository yearRepository;

    @Autowired
    private QuarterRepository quarterRepository;

    @GetMapping
    public ResponseEntity<List<Flowchart>> listAll(@CurrentUser UserDetails curUser) {
        List<Flowchart> flowcharts = new ArrayList<>();
        if (User.isAdmin(curUser)) {
            flowchartRepository.findAll().forEach(flowcharts::add);
        }
        return User.isAdmin(curUser) ? new ResponseEntity<>(flowcharts, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("{id}")
    public ResponseEntity<Flowchart> find(@PathVariable Long id, @CurrentUser UserDetails curUser) {
        User user = userRepository.findByEmail(curUser.getUsername());
        if (user != null && (User.isAdmin(curUser) || user.hasFlowchart(id))) {
            Flowchart flowchart = flowchartRepository.findOne(id);
            if (flowchart == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(flowchart, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @PostMapping
    public ResponseEntity<Flowchart> create(@CurrentUser UserDetails curUser, @RequestBody Flowchart input) {
        User user = userRepository.findByEmail(curUser.getUsername());
        if (user != null) {
            Flowchart flowchart = new Flowchart();
            flowchart.setName(input.getName());
            flowchart.setUser(user);

            user.addFlowchart(flowchart);

            flowchartRepository.save(flowchart);

            return new ResponseEntity<>(flowchart, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable Long id, @CurrentUser UserDetails curUser) {
        User user = userRepository.findByEmail(curUser.getUsername());
        if (user != null && user.hasFlowchart(id)) {
            Flowchart flowchart = flowchartRepository.findOne(id);
            List<Year> years = flowchart.getYears();
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
                flowchart.removeYear(years.get(0));
                yearRepository.delete(curYear);
            }
            user.removeFlowchart(flowchart);
            flowchartRepository.delete(id);
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<Flowchart> update(@PathVariable Long id, @RequestBody Flowchart input, @CurrentUser UserDetails curUser) {
        User user = userRepository.findByEmail(curUser.getUsername());
        Flowchart flowchart = flowchartRepository.findOne(id);

        if (flowchart == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (user != null && user.hasFlowchart(id)) {
            flowchart.setName(input.getName());
            flowchartRepository.save(flowchart);
            return new ResponseEntity<>(flowchart, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

}
