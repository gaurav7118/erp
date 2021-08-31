/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.spring.accounting.customreports;

/**
 *
 * @author krawler
 */
public class AccCustomReportsMeasuresFieldsMapping implements java.io.Serializable {
    
    private String id;
    private String measurefieldid;
    private String defaultheaderid;
    private boolean isDataIndex;
    
    public boolean isIsDataIndex() {
        return isDataIndex;
    }

    public void setIsDataIndex(boolean isDataIndex) {
        this.isDataIndex = isDataIndex;
    }
    
    public boolean getIsDataIndex() {
        return isDataIndex;
    }
    
    public AccCustomReportsMeasuresFieldsMapping() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMeasurefieldid() {
        return measurefieldid;
    }

    public void setMeasurefieldid(String measurefieldid) {
        this.measurefieldid = measurefieldid;
    }

    public String getDefaultheaderid() {
        return defaultheaderid;
    }

    public void setDefaultheaderid(String defaultheaderid) {
        this.defaultheaderid = defaultheaderid;
    }
    
    
    
}
