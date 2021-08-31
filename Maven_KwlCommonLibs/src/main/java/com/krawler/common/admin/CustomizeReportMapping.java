/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class CustomizeReportMapping {

    String id;
    int moduleId;
    int reportId;
    boolean hidden;
    String dataHeader;
    String dataIndex;
    boolean formField;
    boolean reportField;
    boolean manadatoryField;
    boolean userManadatoryField;
    boolean isForProductandService;
    boolean readOnlyField;
    boolean lineField;
    String fieldLabelText;
    String customFieldId;
    Company company;
    User user;
    CustomizeReportHeader customizeReportHeader;
    Set<ProductFieldsAndModulesMapping> modulesMapping=new HashSet(0); //to save multiple module against each field in product master .
   
    public Set<ProductFieldsAndModulesMapping> getModulesMapping() {
        return modulesMapping;
    }

    public void setModulesMapping(Set<ProductFieldsAndModulesMapping> modulesMapping) {
        this.modulesMapping = modulesMapping;
    }
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getCustomFieldId() {
        return customFieldId;
    }

    public void setCustomFieldId(String customFieldId) {
        this.customFieldId = customFieldId;
    }

    public CustomizeReportHeader getCustomizeReportHeader() {
        return customizeReportHeader;
    }

    public void setCustomizeReportHeader(CustomizeReportHeader customizeReportHeader) {
        this.customizeReportHeader = customizeReportHeader;
    }

    public String getDataIndex() {
        return dataIndex;
    }

    public void setDataIndex(String dataIndex) {
        this.dataIndex = dataIndex;
    }

    public String getDataHeader() {
        return dataHeader;
    }

    public void setDataHeader(String dataHeader) {
        this.dataHeader = dataHeader;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isReadOnlyField() {
        return readOnlyField;
    }

    public void setReadOnlyField(boolean readOnlyField) {
        this.readOnlyField = readOnlyField;
    }

    public String getFieldLabelText() {
        return fieldLabelText;
    }

    public void setFieldLabelText(String fieldLabelText) {
        this.fieldLabelText = fieldLabelText;
    }

    public boolean isLineField() {
        return lineField;
    }

    public void setLineField(boolean lineField) {
        this.lineField = lineField;
    }

    public boolean isUserManadatoryField() {
        return userManadatoryField;
    }

    public void setUserManadatoryField(boolean userManadatoryField) {
        this.userManadatoryField = userManadatoryField;
    }
        
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isFormField() {
        return formField;
    }

    public void setFormField(boolean formField) {
        this.formField = formField;
    }

    public boolean isReportField() {
        return reportField;
    }

    public void setReportField(boolean reportField) {
        this.reportField = reportField;
    }
    
    public boolean isManadatoryField() {
        return manadatoryField;
    }

    public void setManadatoryField(boolean manadatoryField) {
        this.manadatoryField = manadatoryField;
    }

    public boolean isIsForProductandService() {
        return isForProductandService;
    }

    public void setIsForProductandService(boolean isForProductandService) {
        this.isForProductandService = isForProductandService;
    }
    
}
