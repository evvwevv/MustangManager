package base.year;

import base.flowchart.Flowchart;
import base.quarter.Quarter;
import base.quarter.QuarterName;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Year {

    private static final int NUM_Q = 4;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private boolean showSummer;
    @ManyToOne(fetch = FetchType.LAZY)
    private Flowchart flowchart;
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "year")
    private List<Quarter> quarters;

    public Year() {
        quarters = new ArrayList<>();
    }

    public Year(String name, boolean showSummer, Flowchart flowchart) {
        quarters = new ArrayList<>();
        this.name = name;
        this.showSummer = showSummer;
        this.quarters = new ArrayList<>();
        this.flowchart = flowchart;
        initializeQuarters();
    }

    public void initializeQuarters() {
        for (int curQ = 0; curQ < NUM_Q - 1; ++curQ) {
            Quarter quarter = new Quarter();
            quarter.setQuarterName(QuarterName.values()[curQ % (QuarterName.values().length - 1)]);
            quarter.setYear(this);
            quarters.add(quarter);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getShowSummer() {
        return showSummer;
    }

    public void setShowSummer(boolean showSummer) {
        this.showSummer = showSummer;
    }

    @JsonIgnore
    public Flowchart getFlowchart() {
        return this.flowchart;
    }

    public void setFlowchart(Flowchart flowchart) {
        this.flowchart = flowchart;
    }

    public List<Quarter> getQuarters() {
        return quarters;
    }

    public void setQuarters(List<Quarter> quarters) {
        this.quarters = quarters;
    }

    public void addQuarter(Quarter quarter) {
        quarters.add(quarter);
    }

    public void removeQuarter(int index) {
        quarters.remove(index);
    }

    public boolean hasQuarter(long id) {
        return quarters.stream()
                .anyMatch(quarter -> quarter.getId() == id);
    }
}

