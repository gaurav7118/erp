/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

/**
 *
 * @author krawler
 */
public class GroupCompanyTermMapping {
    
    private String ID;
    private String sourceCompany;
    private String destinationCompany;
    private String sourceTermName;
    private String sourceTermId;
    private String destinationTermName;
    private String destinationTermId;

    public String getDestinationTermId() {
        return destinationTermId;
    }

    public void setDestinationTermId(String destinationTermId) {
        this.destinationTermId = destinationTermId;
    }

    public String getSourceTermId() {
        return sourceTermId;
    }

    public void setSourceTermId(String sourceTermId) {
        this.sourceTermId = sourceTermId;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getDestinationCompany() {
        return destinationCompany;
    }

    public void setDestinationCompany(String destinationCompany) {
        this.destinationCompany = destinationCompany;
    }

    public String getDestinationTermName() {
        return destinationTermName;
    }

    public void setDestinationTermName(String destinationTermName) {
        this.destinationTermName = destinationTermName;
    }

    public String getSourceTermName() {
        return sourceTermName;
    }

    public void setSourceTermName(String sourceTermName) {
        this.sourceTermName = sourceTermName;
    }

    public String getSourceCompany() {
        return sourceCompany;
    }

    public void setSourceCompany(String sourceCompany) {
        this.sourceCompany = sourceCompany;
    }

}
