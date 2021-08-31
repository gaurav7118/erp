package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

/**
 *
 * @author Atul
 */
public class CustomizeAgedDuration {

    private String id;
    private int fromDuration;
    private int toDuration;
    private Company company;

    public CustomizeAgedDuration() {
    }

    public int getFromDuration() {
        return fromDuration;
    }

    public void setFromDuration(int fromDuration) {
        this.fromDuration = fromDuration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getToDuration() {
        return toDuration;
    }

    public void setToDuration(int toDuration) {
        this.toDuration = toDuration;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
