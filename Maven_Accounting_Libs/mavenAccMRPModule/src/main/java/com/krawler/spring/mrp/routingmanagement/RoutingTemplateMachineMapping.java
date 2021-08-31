/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.routingmanagement;

import com.krawler.spring.mrp.WorkOrder.WorkOrder;
import com.krawler.spring.mrp.machinemanagement.Machine;

/**
 *
 * @author krawler
 */
public class RoutingTemplateMachineMapping {
    private String id;
    private RoutingTemplate routingtemplate;
    private Machine machineid;
    public static final String POJONAME="RoutingTemplateMachineMapping";
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

    public Machine getMachineid() {
        return machineid;
    }

    public void setMachineid(Machine machineid) {
        this.machineid = machineid;
    }
    
   
}
