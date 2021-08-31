
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import java.util.Date;

/**
 *
 * @author krawler
 */
public class GstFormGenerationHistory {

    private String ID;
    private Date generationDate;
    private Date startDate;
    private Date endDate;
    private User user;
    private Company company;
    private String fileName;
    private MultiEntityMapping entityMapping;//To save entity wise GST form generation history when multiEntity is activated from System control
    private int gstGuideVersion;//0 - For Dec2017 & before, 1 - For 8th March2018 guide.

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Date getGenerationDate() {
        return generationDate;
    }

    public void setGenerationDate(Date generationDate) {
        this.generationDate = generationDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public MultiEntityMapping getEntityMapping() {
        return entityMapping;
    }

    public void setEntityMapping(MultiEntityMapping entityMapping) {
        this.entityMapping = entityMapping;
    }

    public int getGstGuideVersion() {
        return gstGuideVersion;
    }

    public void setGstGuideVersion(int gstGuideVersion) {
        this.gstGuideVersion = gstGuideVersion;
    }

}
