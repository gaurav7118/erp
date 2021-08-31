/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

import com.krawler.common.admin.Company;

/**
 *
 * @author Atul
 */
public class ModuleTemplate {

    private String templateId;
    private String templateName;
    private int moduleId;   // defined for different modules like CI, VI etc. in constants.java or wtfsettings.java 
    private String moduleRecordId;// ids of CI/VI/PO/SO/VQ/CQ etc modules tables.
    private String companyUnitid;
    private boolean populateproductintemp;
    private boolean populatecustomerintemp;
    private boolean isdefaulttemplate; //true if it is set as default template for the module refer ERM-80
    private boolean populateautodointemp;

    public boolean isIsdefaulttemplate() {
        return isdefaulttemplate;
    }

    public void setIsdefaulttemplate(boolean isdefaulttemplate) {
        this.isdefaulttemplate = isdefaulttemplate;
    }
    
    public boolean isPopulatecustomerintemp() {
        return populatecustomerintemp;
    }

    public void setPopulatecustomerintemp(boolean populatecustomerintemp) {
        this.populatecustomerintemp = populatecustomerintemp;
    }

    public boolean isPopulateproductintemp() {
        return populateproductintemp;
    }

    public void setPopulateproductintemp(boolean populateproductintemp) {
        this.populateproductintemp = populateproductintemp;
    }

    public String getCompanyUnitid() {
        return companyUnitid;
    }

    public void setCompanyUnitid(String companyUnitid) {
        this.companyUnitid = companyUnitid;
    }
    private Company company;

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleRecordId() {
        return moduleRecordId;
    }

    public void setModuleRecordId(String moduleRecordId) {
        this.moduleRecordId = moduleRecordId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public boolean isPopulateautodointemp() {
        return populateautodointemp;
}

    public void setPopulateautodointemp(boolean populateautodointemp) {
        this.populateautodointemp = populateautodointemp;
    }
    
}
