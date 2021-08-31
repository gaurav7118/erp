/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 * This object stores mapping of columns for import from a file via a script.
 * Also stores DateFormat to be used for import.
 */
public class ImportFileColumnMapping {
    private String ID;//UUID
    
    /*  header of column in import file (for fieldMappingType = 0 or 1)
        NULL when fieldMappingType = 2 or 3 or 4
    */
    private String fileHeader;
    
    /*  id of field in DefaultHeader (default field) or FieldParams (custom field) which is to be mapped with the column in file (for fieldMappingType = 0 or 1)
        id of field in DefaultHeader (default field) or FieldParams (custom field) for which default value is to be used (for fieldMappingType = 2 or 3)
        NULL when fieldMappingType = 4
    */
    private String systemHeaderID;
    
    /*  This is a flag. Following are different values of fieldMappingType and their meanings
        0 - Default Fields mapped with file header
        1 - Custom Field mapped with file header
        2 - Default Field mapped to a default value which is to be assigned to it
        3 - Custom Field mapped to a default value which is to be assigned to it
        4 - DateFormat to be used for import
        Never NULL
    */
    private int fieldMappingType;
    
    /*  defaultValue field 
        stores default value of a field in case of fieldMappingType 2 or 3
        stores DateFormat in case of fieldMappingType 4
        stores NULL when fieldMappingType 0 or 1
    */
    private String defaultValue;
    
    private Company company;//company for which import is to be done
    private Modules module;//module in which import is to be done

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getFileHeader() {
        return fileHeader;
    }

    public void setFileHeader(String fileHeader) {
        this.fileHeader = fileHeader;
    }

    public String getSystemHeaderID() {
        return systemHeaderID;
    }

    public void setSystemHeaderID(String systemHeaderID) {
        this.systemHeaderID = systemHeaderID;
    }

    public int getFieldMappingType() {
        return fieldMappingType;
    }

    public void setFieldMappingType(int fieldMappingType) {
        this.fieldMappingType = fieldMappingType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Modules getModule() {
        return module;
    }

    public void setModule(Modules module) {
        this.module = module;
    }
    
    
}
