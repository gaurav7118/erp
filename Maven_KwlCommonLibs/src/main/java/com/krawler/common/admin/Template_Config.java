/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class Template_Config {

    private String id;
    private Projreport_Template templateId;
    private String fieldAttribJson;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Projreport_Template getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Projreport_Template templateId) {
        this.templateId = templateId;
    }

    public String getFieldAttribJson() {
        return fieldAttribJson;
    }

    public void setFieldAttribJson(String fieldAttribJson) {
        this.fieldAttribJson = fieldAttribJson;
    }
}
