/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class DefaultHeaderModuleJoinReference {
    private String id;                 //do not put direct uuid() method in insert query instead of this put generated UUID 
    private String module;             //this is module id of module which has advance serch  
    private String refModule;          //refModule should not be duplicate for same module
    private String refModuleTableName; //table name of refModule
    private String joinQuery;          //Join query always will be on refModuleTableName
    private String parentRefModule;    //when parentRefModule and module is same then in insert query  parentRefModule will be null
    private String parentRefModuleTableName; //when parentRefModule and module is same then in insert query  parentRefModuleTableName will be null

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJoinQuery() {
        return joinQuery;
    }

    public void setJoinQuery(String joinQuery) {
        this.joinQuery = joinQuery;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getParentRefModule() {
        return parentRefModule;
    }

    public void setParentRefModule(String parentRefModule) {
        this.parentRefModule = parentRefModule;
    }

    public String getParentRefModuleTableName() {
        return parentRefModuleTableName;
    }

    public void setParentRefModuleTableName(String parentRefModuleTableName) {
        this.parentRefModuleTableName = parentRefModuleTableName;
    }

    public String getRefModule() {
        return refModule;
    }

    public void setRefModule(String refModule) {
        this.refModule = refModule;
    }

    public String getRefModuleTableName() {
        return refModuleTableName;
    }

    public void setRefModuleTableName(String refModuleTableName) {
        this.refModuleTableName = refModuleTableName;
    }
    
}

