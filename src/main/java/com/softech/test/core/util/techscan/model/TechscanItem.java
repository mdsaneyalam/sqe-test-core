package com.softech.test.core.util.techscan.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Bean for storing known call types used in TechScan validation
 *
 * @author Uladzimir_Kazimirchyk
 */

public class TechscanItem {
    @JsonProperty("name")
    private String name;

    @JsonProperty("pattern")
    private String pattern;

    @JsonProperty("type")
    private String category;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
