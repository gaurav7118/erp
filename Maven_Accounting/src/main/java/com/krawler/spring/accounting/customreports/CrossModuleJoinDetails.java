/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.customreports;

/**
 *
 * @author Krawler
 */
public class CrossModuleJoinDetails implements java.io.Serializable {

    private String id;
    private String module;
    private String linkedModule;
    private String linkedModuleDBTableName;
    private String fromTable;
    private String joinTable;
    private String mainTable;

    public String getLinkedModule() {
        return linkedModule;
    }

    public void setLinkedModule(String linkedModule) {
        this.linkedModule = linkedModule;
    }

    public String getLinkedModuleDBTableName() {
        return linkedModuleDBTableName;
    }

    public void setLinkedModuleDBTableName(String linkedModuleDBTableName) {
        this.linkedModuleDBTableName = linkedModuleDBTableName;
    }

    public String getFromTable() {
        return fromTable;
    }

    public void setFromTable(String fromTable) {
        this.fromTable = fromTable;
    }

    public String getJoinTable() {
        return joinTable;
    }

    public void setJoinTable(String joinTable) {
        this.joinTable = joinTable;
    }

    public String getMainTable() {
        return mainTable;
    }

    public void setMainTable(String mainTable) {
        this.mainTable = mainTable;
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
