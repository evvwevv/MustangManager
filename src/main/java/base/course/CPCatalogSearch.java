package base.course;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CPCatalogSearch {

    private static final String CP_CATALOG_SEARCH = "https://cp-catalog.herokuapp.com/course/";
    private static final String CP_CATALOG_SEARCH_PREFIX = "https://cp-catalog.herokuapp.com/course/prefix/";
    private static CPCatalogSearch instance = new CPCatalogSearch();

    private CPCatalogSearch() {
    }

    public static CPCatalogSearch getInstance() {
        return instance;
    }

    public Course search(String name) throws IOException {
        URL url = new URL(CP_CATALOG_SEARCH + name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        JSONObject json = new JSONObject(readAll(reader));

        Course course = new Course();
        course.setPrefix(json.get("prefix").toString());
        course.setSuffix(json.get("suffix").toString());
        course.setName(json.get("name").toString());
        course.setTitle(json.get("title").toString());
        course.setUnits(json.get("units").toString());
        course.setPrerequisites(json.get("prerequisites").toString());
        course.setDescription(json.get("description").toString());
        course.setTermsOffered(json.get("termsOffered").toString());

        return course;
    }

    public List<Course> searchPrefix(String prefix) throws IOException {
        URL url = new URL(CP_CATALOG_SEARCH_PREFIX + prefix);
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        JSONObject json = new JSONObject(readAll(reader));
        Iterator<String> keys = json.keys();

        List<Course> courses = new ArrayList<>();
        while (keys.hasNext()) {
            String key = keys.next();

            System.out.println(key);
        }

        return courses;
    }

    private String readAll(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = reader.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
