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
public class AccCustomReportsDefaultFieldsMapping implements java.io.Serializable {

    private String id;
    private String defaultfieldid;
    private String defaultheaderid;
    private boolean isSelectDataIndex;
    
    public AccCustomReportsDefaultFieldsMapping() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDefaultfieldid() {
        return defaultfieldid;
    }

    public void setDefaultfieldid(String defaultfieldid) {
        this.defaultfieldid = defaultfieldid;
    }

    public String getDefaultheaderid() {
        return defaultheaderid;
    }

    public void setDefaultheaderid(String defaultheaderid) {
        this.defaultheaderid = defaultheaderid;
    }

    public boolean isIsSelectDataIndex() {
        return isSelectDataIndex;
    }

    public void setIsSelectDataIndex(boolean isSelectDataIndex) {
        this.isSelectDataIndex = isSelectDataIndex;
    }

}
