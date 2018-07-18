package com.softech.test.core.util.techscan.model;

/**
 * Bean describing whitelist entry used for TechScan validation
 *
 * @author Uladzimir_Kazimirchyk
 */
public class WhiteListEntry {

    private String entry;

    private String approvedBy;

    private String approvedUntil;

    private String notes;

    private String description;

    private String businessgroup;

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getApprovedUntil() {
        return approvedUntil;
    }

    public void setApprovedUntil(String approvedUntil) {
        this.approvedUntil = approvedUntil;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBusinessgroup() {
        return businessgroup;
    }

    public void setBusinessgroup(String businessgroup) {
        this.businessgroup = businessgroup;
    }

    public String toString() {
        return entry + "::" + approvedBy + "::" + approvedUntil + "::" + notes + "::"
                + description + "::" + businessgroup;
    }
}
