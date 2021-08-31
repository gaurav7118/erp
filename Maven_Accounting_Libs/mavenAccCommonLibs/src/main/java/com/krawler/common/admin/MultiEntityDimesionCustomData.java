/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

import com.krawler.common.admin.AccCustomData;
import com.krawler.common.admin.FieldComboData;

/**
 *
 * @author krawler
 */
public class MultiEntityDimesionCustomData extends AccCustomData{
    
    private String fcdId;
    private FieldComboData fieldComboData; 
    private String moduleId;

    public FieldComboData getFieldComboData() {
        return fieldComboData;
    }

    public void setFieldComboData(FieldComboData fieldComboData) {
        this.fieldComboData = fieldComboData;
    }
        
    public String getFcdId() {
        return fcdId;
    }

    public void setFcdId(String fcdId) {
        this.fcdId = fcdId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
    
}
