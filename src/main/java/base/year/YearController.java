package base.year;

import base.course.Course;
import base.course.CourseRepository;
import base.flowchart.Flowchart;
import base.flowchart.FlowchartRepository;
import base.quarter.Quarter;
import base.quarter.QuarterName;
import base.quarter.QuarterRepository;
import base.security.CurrentUser;
import base.user.User;
import base.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/year")
public class YearController {

    @Autowired
    private YearRepository yearRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FlowchartRepository flowchartRepository;

    @Autowired
    private QuarterRepository quarterRepository;

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping("/curUserYears")
    public ResponseEntity<List<Year>> getCurYears(@CurrentUser UserDetails curUser) {
        User user = userRepository.findByEmail(curUser.getUsername());

        return (user != null) ? new ResponseEntity<>(user.getFlowcharts().get(0).getYears(), HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("{id}")
    public ResponseEntity<Year> find(@PathVariable Long id, @CurrentUser UserDetails curUser) {
        User user = userRepository.findByEmail(curUser.getUsername());
        if (user != null && (User.isAdmin(curUser) || user.hasYear(id))) {
            Year year = yearRepository.findOne(id);
            if (year != null) {
                return new ResponseEntity<>(year, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @PostMapping
    public ResponseEntity<Year> create(@CurrentUser UserDetails curUser, @RequestBody Year input) {
        User user = userRepository.findByEmail(curUser.getUsername());
        if (user != null) {
            Year year = new Year();
            year.setName(input.getName());
            year.setShowSummer(input.getShowSummer());
            year.setFlowchart(input.getFlowchart());
            Flowchart flowchart = flowchartRepository.findOne(input.getFlowchart().getId());
            flowchart.addYear(year);

            yearRepository.save(year);

            return new ResponseEntity<>(year, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable Long id, @CurrentUser UserDetails curUser) {
        User user = userRepository.findByEmail(curUser.getUsername());
        if (user != null && user.hasYear(id)) {
            Year year = yearRepository.findOne(id);
            List<Quarter> quarters = year.getQuarters();
            while (!quarters.isEmpty()) {
                List<Course> courses = quarters.get(0).getCourses();
                while (!courses.isEmpty()) {
                    Course curCourse = courses.get(0);
                    quarters.get(0).removeCourse(curCourse);
                    curCourse.removeQuarter(quarters.get(0));
                }
                Quarter curQuarter = quarters.get(0);
                year.removeQuarter(0);
                quarterRepository.delete(curQuarter);

            }
            Flowchart flowchart = year.getFlowchart();
            flowchart.removeYear(year);
            yearRepository.delete(year);
        }
    }

    @PutMapping("clear/{id}")
    public ResponseEntity<Year> clearCourses(@PathVariable Long id, @CurrentUser UserDetails curUser) {
        User user = userRepository.findByEmail(curUser.getUsername());
        Year year = yearRepository.findOne(id);
        if (year == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (user != null && user.hasYear(id)) {
            List<Quarter> quarters = year.getQuarters();
            for (int i = 0; i < quarters.size(); i++) {
                List<Course> courses = quarters.get(i).getCourses();
                while (!courses.isEmpty()) {
                    Course curCourse = courses.get(0);
                    quarters.get(i).removeCourse(curCourse);
                    curCourse.removeQuarter(quarters.get(i));
                }
            }
            userRepository.save(user);
            return new ResponseEntity<>(year, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<Year> update(@PathVariable Long id, @RequestBody Year input, @CurrentUser UserDetails curUser) {
        User user = userRepository.findByEmail(curUser.getUsername());
        Year year = yearRepository.findOne(id);

        if (year == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (user != null && user.hasYear(id)) {
            year.setName(input.getName());
            year.setShowSummer(input.getShowSummer());
            year.setFlowchart(input.getFlowchart());
            return new ResponseEntity<>(year, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PutMapping("rename/{id}")
    public ResponseEntity<Year> rename(@PathVariable Long id, @RequestBody Year input, @CurrentUser UserDetails curUser) {
        User user = userRepository.findByEmail(curUser.getUsername());
        Year year = yearRepository.findOne(id);
        if (year == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (user != null && user.hasYear(id)) {
            year.setName(input.getName());
            yearRepository.save(year);
            return new ResponseEntity<>(year, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PutMapping("toggleSummer/{id}")
    public ResponseEntity<Year> toggleSummer(@PathVariable Long id, @CurrentUser UserDetails curUser) {
        User user = userRepository.findByEmail(curUser.getUsername());
        Year year = yearRepository.findOne(id);

        if (year == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (user != null && user.hasYear(id)) {
            if (year.getShowSummer()) {
                year.setShowSummer(false);
                Long quarterId = year.getQuarters().get(3).getId();
                Quarter quarter = quarterRepository.findOne(quarterId);
                List<Course> courses = quarter.getCourses();
                while (!courses.isEmpty()) {
                    Course curCourse = courses.get(0);
                    quarter.removeCourse(courses.get(0));
                    curCourse.removeQuarter(quarter);
                }
                year.removeQuarter(3);
                quarterRepository.delete(quarterId);
            } else {
                year.setShowSummer(true);
                Quarter quarter = new Quarter();
                quarter.setQuarterName(QuarterName.SUMMER);
                quarter.setYear(year);
                year.addQuarter(quarter);
                quarterRepository.save(quarter);
            }
            return new ResponseEntity<>(year, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PutMapping("addYear/{id}")
    public ResponseEntity<Flowchart> addYear(@PathVariable Long id, @RequestBody Year input, @CurrentUser UserDetails curUser) {
        User user = userRepository.findByEmail(curUser.getUsername());
        Flowchart flowchart = flowchartRepository.findOne(id);
        if (flowchart == null) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else if (user != null && user.hasFlowchart(id)) {
            Year year = new Year();
            year.setName(input.getName());
            year.setShowSummer(false);
            year.setFlowchart(flowchart);
            year.initializeQuarters();
            flowchart.addYear(year);
            yearRepository.save(year);
            return new ResponseEntity<>(flowchart, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @PutMapping("removeYear/{flowchartId}")
    public ResponseEntity<Flowchart> removeYear(@PathVariable Long flowchartId, @RequestBody Year input, @CurrentUser UserDetails curUser) {
        User user = userRepository.findByEmail(curUser.getUsername());
        Flowchart flowchart = flowchartRepository.findOne(flowchartId);
        Year year = yearRepository.findOne(input.getId());
        if (flowchart == null || year == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (user != null && user.hasFlowchart(flowchartId) && user.hasYear(input.getId())) {
            List<Quarter> quarters = year.getQuarters();
            while (!quarters.isEmpty()) {
                List<Course> courses = quarters.get(0).getCourses();
                while (!courses.isEmpty()) {
                    Course curCourse = courses.get(0);
                    quarters.get(0).removeCourse(curCourse);
                    curCourse.removeQuarter(quarters.get(0));
                }
                Quarter curQuarter = quarters.get(0);
                year.removeQuarter(0);
                quarterRepository.delete(curQuarter);

            }
            flowchart.removeYear(year);
            yearRepository.delete(year);
            return new ResponseEntity<>(flowchart, HttpStatus.OK);

        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
}