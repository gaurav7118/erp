/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.spring.mrp.routingmanagement;

import com.krawler.common.admin.AccCustomData;

public class RoutingTemplateCustomData extends AccCustomData{
    
    private String routingTemplateId;
    private RoutingTemplate routingTemplate;
    private String moduleId;

    public String getRoutingTemplateId() {
        return routingTemplateId;
    }

    public void setRoutingTemplateId(String routingTemplateId) {
        this.routingTemplateId = routingTemplateId;
    }

    public RoutingTemplate getRoutingTemplate() {
        return routingTemplate;
    }

    public void setRoutingTemplate(RoutingTemplate routingTemplate) {
        this.routingTemplate = routingTemplate;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
}