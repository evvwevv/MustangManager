package base.quarter;

import base.course.Course;
import base.year.Year;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Quarter {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private QuarterName quarterName;
    @ManyToOne(fetch = FetchType.LAZY)
    private Year year;
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, mappedBy = "quarters")
    private List<Course> courses;

    public Quarter() {
        courses = new ArrayList<>();
    }

    // Getters
    public Long getId() {
        return id;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public QuarterName getQuarterName() {
        return quarterName;
    }

    public void setQuarterName(QuarterName quarterName) {
        this.quarterName = quarterName;
    }

    public List<Course> getCourses() {
        return courses;
    }

    @JsonIgnore
    public Year getYear() {
        return year;
    }

    public void setYear(Year year) {
        this.year = year;
    }

    // Courses
    public void addCourse(Course course) {
        courses.add(course);
    }

    public void removeCourse(Course course) {
        courses.remove(course);
    }

    public boolean hasCourse(Course course) {
        return courses.contains(course);
    }
}
