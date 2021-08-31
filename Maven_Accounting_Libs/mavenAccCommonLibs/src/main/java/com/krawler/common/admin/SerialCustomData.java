/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;


import java.util.Date;

/**
 *
 * @author krawler
 */
public class SerialCustomData extends AccCustomData{

       private String serialDocumentMappingId;
    private SerialDocumentMapping serialDocumentMapping;

    public SerialDocumentMapping getSerialDocumentMapping() {
        return serialDocumentMapping;
    }

    public void setSerialDocumentMapping(SerialDocumentMapping serialDocumentMapping) {
        this.serialDocumentMapping = serialDocumentMapping;
    }
    private String moduleId;

    public String getSerialDocumentMappingId() {
        return serialDocumentMappingId;
    }

    public void setSerialDocumentMappingId(String serialDocumentMappingId) {
        this.serialDocumentMappingId = serialDocumentMappingId;
    }



    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }


  
    
}
