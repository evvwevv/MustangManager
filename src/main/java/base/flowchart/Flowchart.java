package base.flowchart;

import base.user.User;
import base.year.Year;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Flowchart {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "flowchart")
    private List<Year> years;

    public Flowchart() {
        years = new ArrayList<>();
        this.years.add(new Year("Freshman", false, this));
        this.years.add(new Year("Sophomore", false, this));
        this.years.add(new Year("Junior", false, this));
        this.years.add(new Year("Senior", false, this));
    }

    //for debugging
    public Flowchart(String errorMessage) {
        name = errorMessage;
    }

    // Getters
    public Long getId() {
        return id;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Year> getYears() {
        return years;
    }

    public void setYears(List<Year> years) {
        this.years = years;
    }

    @JsonIgnore
    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Quarters
    public void addYear(Year year) {
        years.add(year);
    }

    public void removeYear(Year year) {
        years.remove(year);
    }

    public boolean ownsYear(Year year) {
        return years.stream()
                .anyMatch(y -> y.getId() == year.getId());
    }

    public boolean hasYear(long id) {
        return years.stream()
                .anyMatch(year -> year.getId() == id);
    }
}
