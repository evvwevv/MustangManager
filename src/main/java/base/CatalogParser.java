package base;

import base.course.Course;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class CatalogParser {

    private static final CatalogParser instance = new CatalogParser();
    private static final Logger LOGGER = Logger.getLogger(CatalogParser.class.getName());
    private static final String CATALOG_URL = "http://catalog.calpoly.edu/coursesaz/";
    private static final String TERMS_SEARCH = "Term Typically Offered: ";
    private static final String PRE_SEARCH = "Prerequisite: ";
    private static final String DEP_CLASS = ".sitemaplink";
    private static final String COURSE_CLASS = ".courseblock";
    private static final String COURSE_TITLE_CLASS = ".courseblocktitle";
    private static final String STRONG_TAG = "strong";
    private static final String DIV_TAG = "div";
    private static final String P_TAG = "p";
    private static final String SPAN_TAG = "span";

    private CatalogParser() {
    }

    public static CatalogParser getInstance() {
        return instance;
    }

    public List<String> getDepartments() {
        List<String> departments = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(CATALOG_URL).get();
            Elements depElements = doc.select(DEP_CLASS);
            for (Element depElement : depElements) {
                String department = depElement.text().split("[\\(\\)]")[1].toLowerCase();
                departments.add(department);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

        return departments;
    }

    public List<Course> getCourses(String department) {
        List<Course> courses = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(CATALOG_URL + department).get();

            Elements courseElements = doc.select(COURSE_CLASS);
            for (Element courseElement : courseElements) {
                Course course = new Course();
                String[] courseName = extractCourseName(courseElement);
                course.setName(courseName[0] + courseName[1]);
                course.setPrefix(courseName[0]);
                course.setSuffix(courseName[1]);
                course.setTitle(extractCourseTitle(courseElement));
                course.setUnits(extractCourseUnits(courseElement));
                course.setPrerequisites(extractCoursePrerequisites(courseElement));
                course.setDescription(extractCourseDescription(courseElement));
                course.setTermsOffered(extractCourseTermsOffered(courseElement));

                courses.add(course);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

        return courses;
    }

    private String[] extractCourseName(Element courseElement) {
        Elements nameElement = courseElement.select(COURSE_TITLE_CLASS).select(STRONG_TAG);

        return nameElement.text().split("[/^(.*?)./]")[0].split("\u00a0");
    }

    private String extractCourseTitle(Element courseElement) {
        Elements titleElement = courseElement.select(COURSE_TITLE_CLASS).select(STRONG_TAG);

        return titleElement.text().split("\\.")[1].trim();
    }

    private String extractCourseUnits(Element courseElement) {
        Elements unitElement = courseElement.select(COURSE_TITLE_CLASS).select(STRONG_TAG).select(SPAN_TAG);

        return unitElement.text().split(" ")[0];
    }

    private String extractCoursePrerequisites(Element courseElement) {
        Elements preElement = courseElement.select(DIV_TAG).get(1).select("p:contains(" + PRE_SEARCH + ")");

        String coursePrerequisites;
        if (preElement.size() == 0) {
            coursePrerequisites = "";
        } else {
            coursePrerequisites = preElement.text();
        }

        return coursePrerequisites;
    }

    private String extractCourseDescription(Element courseElement) {
        Elements descElement = courseElement.select(DIV_TAG).get(2).select(P_TAG);

        return descElement.text();
    }

    private String extractCourseTermsOffered(Element courseElement) {
        Elements offElement = courseElement.select(DIV_TAG).get(1).select("p:contains(" + TERMS_SEARCH + ")");

        String courseTermsOffered;
        if (offElement.size() == 0) {
            courseTermsOffered = "";
        } else {
            courseTermsOffered = offElement.text();
            courseTermsOffered = courseTermsOffered.substring(TERMS_SEARCH.length(), courseTermsOffered.length());
        }

        return courseTermsOffered;
    }

    public String parsePrefix(String name) {
        return name.replaceAll("\\d*$", "");
    }

    public String parseSuffix(String name) {
        return name.replaceAll("^\\D*", "");
    }
}