/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.spring.accounting.customreports;
import com.krawler.common.admin.Modules;
import com.krawler.common.admin.User;
import com.krawler.common.admin.ModuleCategory;
/**
 *
 * @author krawler
 */
public class AccCustomReportsMeasuresFields implements java.io.Serializable {
    
    private String measurefieldid;
    private String measurefieldname;
    private String measurefielddisplayname;
    private Long createdon;
    private Long updatedon;
    private String xtype;
    private ModuleCategory measurefieldmodulecategory;
    private Modules measurefieldmodule;
    private boolean iscustomreport;
    private String dataIndex;  
    private boolean isreadonly;  
    private boolean allowinotherapplication;

    public AccCustomReportsMeasuresFields() {
    }

    public String getMeasurefieldid() {
        return measurefieldid;
    }

    public void setMeasurefieldid(String measurefieldid) {
        this.measurefieldid = measurefieldid;
    }

    public String getMeasurefieldname() {
        return measurefieldname;
    }

    public void setMeasurefieldname(String measurefieldname) {
        this.measurefieldname = measurefieldname;
    }

    public String getMeasurefielddisplayname() {
        return measurefielddisplayname;
    }

    public void setMeasurefielddisplayname(String measurefielddisplayname) {
        this.measurefielddisplayname = measurefielddisplayname;
    }

    public Long getCreatedon() {
        return createdon;
    }

    public void setCreatedon(Long createdon) {
        this.createdon = createdon;
    }

    public Long getUpdatedon() {
        return updatedon;
    }

    public void setUpdatedon(Long updatedon) {
        this.updatedon = updatedon;
    }
    
    public String getXtype() {
        return xtype;
    }

    public void setXtype(String xtype) {
        this.xtype = xtype;
    }

    public ModuleCategory getMeasurefieldmodulecategory() {
        return measurefieldmodulecategory;
    }

    public void setMeasurefieldmodulecategory(ModuleCategory measurefieldmodulecategory) {
        this.measurefieldmodulecategory = measurefieldmodulecategory;
    }

    public Modules getMeasurefieldmodule() {
        return measurefieldmodule;
    }

    public void setMeasurefieldmodule(Modules measurefieldmodule) {
        this.measurefieldmodule = measurefieldmodule;
    }
    
    public String getDataIndex() {
        return dataIndex;
    }

    public void setDataIndex(String dataIndex) {
        this.dataIndex = dataIndex;
    }
       
    public boolean isIsreadonly() {
        return isreadonly;
    }

    public void setIsreadonly(boolean isreadonly) {
        this.isreadonly = isreadonly;
    }
    
    public boolean isAllowinotherapplication() {
        return allowinotherapplication;
    }

    public void setAllowinotherapplication(boolean allowinotherapplication) {
        this.allowinotherapplication = allowinotherapplication;
    }
    
    public boolean isIscustomreport() {
        return iscustomreport;
    }

    public void setIscustomreport(boolean iscustomreport) {
        this.iscustomreport = iscustomreport;
    }
}
