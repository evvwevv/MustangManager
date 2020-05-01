package base;

import base.course.Course;
import base.course.CourseRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.List;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);

        loadCourses(ctx);
    }

    private static void loadCourses(ApplicationContext ctx) {
        CourseRepository cr = (CourseRepository) ctx.getBean("courseRepository");

        if (cr.findByName("CPE123") == null) {
            CatalogParser cp = CatalogParser.getInstance();
            List<String> departments = cp.getDepartments();

            for (String department : departments) {
                List<Course> courses = cp.getCourses(department);
                cr.save(courses);
            }
        }
    }

}
