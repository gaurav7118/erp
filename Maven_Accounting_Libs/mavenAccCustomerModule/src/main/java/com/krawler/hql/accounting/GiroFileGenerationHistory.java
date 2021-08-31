/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import java.util.Date;

public class GiroFileGenerationHistory{
    private String ID;
    private Date generationDate;
    private Company company;
    private String fileName;
    private int count ;
    private int Bank ;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getGenerationDate() {
        return generationDate;
    }

    public void setGenerationDate(Date generationDate) {
        this.generationDate = generationDate;
    }

    public int getBank() {
        return Bank;
    }

    public void setBank(int Bank) {
        this.Bank = Bank;
    }
    
    
}