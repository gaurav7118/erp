/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.inspection;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import java.util.List;

/**
 *
 * @author Vipin Gupta
 */
public interface TemplateService {

    public InspectionTemplate getInspectionTemplate(String templateId) throws ServiceException;

    public InspectionTemplate getInspectionTemplateByName(Company company, String templateName) throws ServiceException;

    public List<InspectionTemplate> getInspectionTemplateList(Company company, String searchString, Paging paging) throws ServiceException;

    public InspectionArea getInspectionArea(String areaId) throws ServiceException;

    public InspectionArea getInspectionAreaByName(InspectionTemplate inspectionTemplate, String areaName) throws ServiceException;

    public List<InspectionArea> getInspectionAreaList(InspectionTemplate inspectionTemplate, String searchString, Paging paging) throws ServiceException;

    public void deleteInspectionTemplate(InspectionTemplate inspectionTemplate) throws ServiceException;

    public void deleteInspectionArea(InspectionArea inspectionArea) throws ServiceException;

    public void addTemplate(InspectionTemplate iTemplate) throws ServiceException;

    public void updateTemplate(InspectionTemplate iTemplate) throws ServiceException;

    public void updateInspectionArea(InspectionArea iArea) throws ServiceException;

    public void addInspectionArea(InspectionArea iArea) throws ServiceException;
}
