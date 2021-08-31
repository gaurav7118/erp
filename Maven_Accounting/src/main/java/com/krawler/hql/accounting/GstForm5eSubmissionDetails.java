/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import java.util.Date;

/**
 *
 * @author krawler
 */
public class GstForm5eSubmissionDetails {
    
    private String ID;
    private Date dtPeriodStart;
    private Date dtPeriodEnd;
    private int status;// 0  Authentication Pending | 1  Success | 2  Failure | 3 Aborted By User | 
    private String response;
    private String responseCode; 
    private String messageCode; 
    private Date eSubmissionDate;
    private String eSubmissionJSON; 
    private String entity; 
    private Company company; 

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
    
    public Date getDtPeriodStart() {
        return dtPeriodStart;
    }

    public void setDtPeriodStart(Date dtPeriodStart) {
        this.dtPeriodStart = dtPeriodStart;
    }

    public Date getDtPeriodEnd() {
        return dtPeriodEnd;
    }

    public void setDtPeriodEnd(Date dtPeriodEnd) {
        this.dtPeriodEnd = dtPeriodEnd;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    
    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(String messageCode) {
        this.messageCode = messageCode;
    }
    
    public Date geteSubmissionDate() {
        return eSubmissionDate;
    }

    public void seteSubmissionDate(Date eSubmissionDate) {
        this.eSubmissionDate = eSubmissionDate;
    }

    public String geteSubmissionJSON() {
        return eSubmissionJSON;
    }
    
    public void seteSubmissionJSON(String eSubmissionJSON) {
        this.eSubmissionJSON = eSubmissionJSON;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }
    
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
}
