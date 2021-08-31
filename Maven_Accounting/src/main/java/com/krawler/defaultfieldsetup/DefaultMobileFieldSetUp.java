
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.defaultfieldsetup;

import com.krawler.common.admin.Modules;

/**
 *
 * @author krawler
 */
public class DefaultMobileFieldSetUp {
    
    private String id;
    private Modules moduleid;
    private String summaryreportjson;
    private String detailreportjson;
    private String formfieldjson;

    public String getDetailreportjson() {
        return detailreportjson;
    }

    public void setDetailreportjson(String detailreportjson) {
        this.detailreportjson = detailreportjson;
    }

    public String getFormfieldjson() {
        return formfieldjson;
    }

    public void setFormfieldjson(String formfieldjson) {
        this.formfieldjson = formfieldjson;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSummaryreportjson() {
        return summaryreportjson;
    }

    public void setSummaryreportjson(String summaryreportjson) {
        this.summaryreportjson = summaryreportjson;
    }

    public Modules getModuleid() {
        return moduleid;
    }

    public void setModuleid(Modules moduleid) {
        this.moduleid = moduleid;
    }
    
    
}
