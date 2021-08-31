/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.customreports;

/**
 *
 * @author Krawler
 */
public class CrossModuleLinkingDetails implements java.io.Serializable {

    private String id;
    private String module;
    private String linkedmodule;
    private boolean allowcrossmodule;

    public String getLinkedmodule() {
        return linkedmodule;
    }

    public void setLinkedmodule(String linkedmodule) {
        this.linkedmodule = linkedmodule;
    }

    public boolean isAllowcrossmodule() {
        return allowcrossmodule;
    }

    public void setAllowcrossmodule(boolean allowcrossmodule) {
        this.allowcrossmodule = allowcrossmodule;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

}
