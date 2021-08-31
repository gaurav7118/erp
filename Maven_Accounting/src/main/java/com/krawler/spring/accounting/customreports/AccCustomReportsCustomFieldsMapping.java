/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.customreports;

/**
 *
 * @author Krawler
 */
public class AccCustomReportsCustomFieldsMapping implements java.io.Serializable{
    private String id;
    private String module;
    private String defaultheaderid;
    private boolean islineitem;
    private boolean isDefaultItem;
    private String mappingFor;

    public String getMappingFor() {
        return mappingFor;
    }

    public void setMappingFor(String mappingFor) {
        this.mappingFor = mappingFor;
    }

    public String getDefaultheaderid() {
        return defaultheaderid;
    }

    public void setDefaultheaderid(String defaultheaderid) {
        this.defaultheaderid = defaultheaderid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isIslineitem() {
        return islineitem;
    }

    public void setIslineitem(boolean islineitem) {
        this.islineitem = islineitem;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }
    
    public boolean isIsDefaultItem() {
        return isDefaultItem;
    }

    public void setIsDefaultItem(boolean isDefaultItem) {
        this.isDefaultItem = isDefaultItem;
    }
}
