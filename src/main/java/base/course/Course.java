package base.course;

import base.quarter.Quarter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String prefix;
    private String suffix;
    private String name;
    private String title;
    private String units;
    @Column(columnDefinition = "TEXT")
    private String prerequisites;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String termsOffered;
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<Quarter> quarters;

    public Course() {
        quarters = new ArrayList<>();
    }

    // Getters
    public Long getId() {
        return id;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTermsOffered() {
        return termsOffered;
    }

    public void setTermsOffered(String termsOffered) {
        this.termsOffered = termsOffered;
    }

    public void setQuarters(List<Quarter> quarters) {
        this.quarters = quarters;
    }

    @JsonIgnore
    public List<Quarter> getQuarters() {
        return this.quarters;
    }

    // Quarters
    public void addQuarter(Quarter quarter) {
        quarters.add(quarter);
    }

    public void removeQuarter(Quarter quarter) {
        quarters.remove(quarter);
    }
}
