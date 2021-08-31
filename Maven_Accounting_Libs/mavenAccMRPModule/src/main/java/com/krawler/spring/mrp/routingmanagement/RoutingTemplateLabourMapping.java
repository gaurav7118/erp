/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.routingmanagement;

import com.krawler.spring.mrp.labormanagement.Labour;

/**
 *
 * @author krawler
 */
public class RoutingTemplateLabourMapping {
    private String id;
    private RoutingTemplate routingtemplate;
    private Labour labourid;
    public static final String POJONAME="RoutingTemplateLabourMapping";
    public static final String ATTRIBUTENAME="routingtemplate.id";
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RoutingTemplate getRoutingtemplate() {
        return routingtemplate;
    }

    public void setRoutingtemplate(RoutingTemplate routingtemplate) {
        this.routingtemplate = routingtemplate;
    }

    public Labour getLabourid() {
        return labourid;
    }

    public void setLabourid(Labour labourid) {
        this.labourid = labourid;
    }
}
