/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class CustomizeReportHeader {

    String id;
    String dataIndex;// in case of Form Field it will contain value of "id" of that field to hide/show that field. 
    boolean formField;
    boolean reportField;
    //boolean lineField;
    int lineField;
    boolean manadatoryField;
    boolean userManadatoryField;
    String dataHeader;
    int moduleId;
    int reportId;
    String parentid;
    DefaultHeader defaultheader;

    public DefaultHeader getDefaultheader() {
        return defaultheader;
    }

    public void setDefaultheader(DefaultHeader defaultheader) {
        this.defaultheader = defaultheader;
    }
    public String getDataHeader() {
        return dataHeader;
    }

    public void setDataHeader(String dataHeader) {
        this.dataHeader = dataHeader;
    }

    public String getDataIndex() {
        return dataIndex;
    }

    public void setDataIndex(String dataIndex) {
        this.dataIndex = dataIndex;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isFormField() {
        return formField;
    }

    public void setFormField(boolean formField) {
        this.formField = formField;
    }

    public boolean isManadatoryField() {
        return manadatoryField;
    }

    public void setManadatoryField(boolean manadatoryField) {
        this.manadatoryField = manadatoryField;
    }

//    public int isLineField() {
//        return lineField;
//    }
    public int isLineField() {
        return lineField;
    }

//    public void setLineField(boolean lineField) {
//        this.lineField = lineField;
//    }
    public void setLineField(int lineField) {
        this.lineField = lineField;
    }

    public boolean isReportField() {
        return reportField;
    }

    public void setReportField(boolean reportField) {
        this.reportField = reportField;
    }
    
    public boolean isUserManadatoryField() {
        return userManadatoryField;
    }

    public void setUserManadatoryField(boolean userManadatoryField) {
        this.userManadatoryField = userManadatoryField;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public int getReportId() {
        return reportId;
    }

   public void setReportId(int reportId) {
        this.reportId = reportId;
    }
   
   public String getParentid() {
        return parentid;
    }
   
    public void setParentid(String parentid) {
        this.parentid = parentid;
    }
}
