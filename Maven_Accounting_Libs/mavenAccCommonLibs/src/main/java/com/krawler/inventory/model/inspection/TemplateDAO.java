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
public interface TemplateDAO {

    public InspectionTemplate getInspectionTemplate(String templateId) throws ServiceException;

    public InspectionTemplate getInspectionTemplateByName(Company company, String templateName) throws ServiceException;

    public List<InspectionTemplate> getInspectionTemplateList(Company company, String searchString, Paging paging) throws ServiceException;

    public void saveOrUpdate(Object object) throws ServiceException;

    public void delete(Object object) throws ServiceException;

    public InspectionArea getInspectionArea(String areaId) throws ServiceException;

    public List<InspectionArea> getInspectionAreaList(InspectionTemplate inspectionTemplate, String searchString, Paging paging) throws ServiceException;

    public InspectionArea getInspectionAreaByName(InspectionTemplate inspectionTemplate, String areaName) throws ServiceException;

    public InspectionTemplate getOtherInspectionTemplateByName(InspectionTemplate inspectionTemplate) throws ServiceException;

    public InspectionArea getOtherInspectionAreaByName(InspectionArea inspectionArea) throws ServiceException;

    public void deleteMapingWithProduct(InspectionTemplate inspectionTemplate) throws ServiceException;
}
