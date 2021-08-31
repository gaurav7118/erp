/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

/**
 *
 * @author krawler
 */
public class ExportCompanypref {

    private String id;
    private String displayname; // Name of  Setting display in Account Preferences
    private String colnumname; // Column Name 
    private String gettermethod_name; // Getter Method Name 
    private String namepasstosavefunction;  // Setting  Name that we pass to save function of account preference 
    private boolean isfromextraorcomapnypref; // 'T' - CompanyAccountPreferences 'F'-ExtraCompanyPreferences
    private boolean accountfield; // 'T'- If this is account setting 'F'- Not an account Setting
    private String dataindex; // Name of field in Wtf.account.companyAccountPref
    private String validatetype; // Type of field
    private String fullclassname; // Class full Name
    private String exportclassgetter; // Method for Requiered export Name
    private String importclassgetter;// Method to get id of record

    public String getColnumname() {
        return colnumname;
    }

    public String getDisplayname() {
        return displayname;
    }

    public String getGettermethod_name() {
        return gettermethod_name;
    }

    public String getId() {
        return id;
    }

    public boolean isIsfromextraorcomapnypref() {
        return isfromextraorcomapnypref;
    }

    public void setColnumname(String colnumname) {
        this.colnumname = colnumname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public void setGettermethod_name(String gettermethod_name) {
        this.gettermethod_name = gettermethod_name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIsfromextraorcomapnypref(boolean isfromextraorcomapnypref) {
        this.isfromextraorcomapnypref = isfromextraorcomapnypref;
    }

    public String getNamepasstosavefunction() {
        return namepasstosavefunction;
    }

    public void setNamepasstosavefunction(String namepasstosavefunction) {
        this.namepasstosavefunction = namepasstosavefunction;
    }

    public boolean isAccountfield() {
        return accountfield;
    }

    public void setAccountfield(boolean accountfield) {
        this.accountfield = accountfield;
    }

    public String getDataindex() {
        return dataindex;
    }

    public void setDataindex(String dataindex) {
        this.dataindex = dataindex;
    }

    public String getValidatetype() {
        return validatetype;
    }

    public void setValidatetype(String validatetype) {
        this.validatetype = validatetype;
    }

    public String getExportclassgetter() {
        return exportclassgetter;
    }

    public void setExportclassgetter(String exportclassgetter) {
        this.exportclassgetter = exportclassgetter;
    }

    public String getFullclassname() {
        return fullclassname;
    }

    public void setFullclassname(String fullclassname) {
        this.fullclassname = fullclassname;
    }

    public String getImportclassgetter() {
        return importclassgetter;
    }

    public void setImportclassgetter(String importclassgetter) {
        this.importclassgetter = importclassgetter;
    }
}
