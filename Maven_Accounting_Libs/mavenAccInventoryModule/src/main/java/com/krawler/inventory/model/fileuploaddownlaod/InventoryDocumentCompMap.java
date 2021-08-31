/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.fileuploaddownlaod;

import com.krawler.common.admin.Company;
import com.krawler.common.util.InventoryModules;

/**
 *
 * @author krawler
 */
public class InventoryDocumentCompMap {
 
    
    private String ID;
    private InventoryDocuments document;
    private String moduleWiseId;
    private Company company;
    private InventoryModules module;
    
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public InventoryDocuments getDocument() {
        return document;
    }

    public void setDocument(InventoryDocuments document) {
        this.document = document;
    }

    public String getModuleWiseId() {
        return moduleWiseId;
    }

    public void setModuleWiseId(String moduleWiseId) {
        this.moduleWiseId = moduleWiseId;
    }

    public InventoryModules getModule() {
        return module;
    }

    public void setModule(InventoryModules module) {
        this.module = module;
    }
    
}
