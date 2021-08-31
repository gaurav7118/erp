/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class ProductFieldsAndModulesMapping {
    
    private String id;
    private Integer moduleid; 
    private  CustomizeReportMapping fieldid;//id of CustomizeReportMapping 

  
    public CustomizeReportMapping getFieldid() {
        return fieldid;
    }

    public void setFieldid(CustomizeReportMapping fieldid) {
        this.fieldid = fieldid;
    }

    public Integer getModuleid() {
        return moduleid;
    }

    public void setModuleid(Integer moduleid) {
        this.moduleid = moduleid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
}
