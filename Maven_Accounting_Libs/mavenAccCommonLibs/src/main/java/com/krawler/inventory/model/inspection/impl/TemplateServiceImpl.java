/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.inspection.impl;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.inventory.model.inspection.*;
import java.util.List;

/**
 *
 * @author Vipin Gupta
 */
public class TemplateServiceImpl implements TemplateService {

    private TemplateDAO templateDAO;

    public void setTemplateDAO(TemplateDAO templateDAO) {
        this.templateDAO = templateDAO;
    }

    @Override
    public InspectionTemplate getInspectionTemplate(String templateId) throws ServiceException {
        InspectionTemplate inspectionTemplate = null;
        if (!StringUtil.isNullOrEmpty(templateId)) {
            inspectionTemplate = templateDAO.getInspectionTemplate(templateId);
        }
        return inspectionTemplate;
    }

    @Override
    public InspectionTemplate getInspectionTemplateByName(Company company, String templateName) throws ServiceException {
        if (company == null) {
            throw new TemplateException("company is null or empty");
        }
        if (StringUtil.isNullOrEmpty(templateName)) {
            throw new TemplateException("Template name is null or empty");
        }
        return templateDAO.getInspectionTemplateByName(company, templateName);
    }

    @Override
    public List<InspectionTemplate> getInspectionTemplateList(Company company, String searchString, Paging paging) throws ServiceException {
        if (company == null) {
            throw new TemplateException("company is null or empty");
        }
        return templateDAO.getInspectionTemplateList(company, searchString, paging);
    }

    @Override
    public InspectionArea getInspectionArea(String areaId) throws ServiceException {
        InspectionArea ia = null;
        if (!StringUtil.isNullOrEmpty(areaId)) {
            ia = templateDAO.getInspectionArea(areaId);;
        }
        return ia;
    }

    @Override
    public InspectionArea getInspectionAreaByName(InspectionTemplate inspectionTemplate, String areaName) throws ServiceException{
        if (inspectionTemplate == null) {
            throw new TemplateException("Inspection Template cannot be empty");
        }
        return templateDAO.getInspectionAreaByName(inspectionTemplate, areaName);
    }

    @Override
    public List<InspectionArea> getInspectionAreaList(InspectionTemplate inspectionTemplate, String searchString, Paging paging) throws ServiceException {
        if (inspectionTemplate == null) {
            throw new TemplateException("Inspection template is null or empty");
        }
        return templateDAO.getInspectionAreaList(inspectionTemplate, searchString, paging);
    }

    @Override
    public void deleteInspectionTemplate(InspectionTemplate inspectionTemplate) throws ServiceException {
        if (inspectionTemplate == null) {
            throw new TemplateException("Inspection template is null or empty");
        }
        templateDAO.deleteMapingWithProduct(inspectionTemplate);
        templateDAO.delete(inspectionTemplate);
    }

    @Override
    public void deleteInspectionArea(InspectionArea inspectionArea) throws ServiceException {
        if (inspectionArea == null) {
            throw new TemplateException("Inspection Area is null or empty");
        }
        templateDAO.delete(inspectionArea);
    }

    @Override
    public void addTemplate(InspectionTemplate inspectionTemplate) throws ServiceException {
        if (inspectionTemplate == null) {
            throw new TemplateException("Inspection Template cannot be empty");
        }
        InspectionTemplate existingIT = getInspectionTemplateByName(inspectionTemplate.getCompany(), inspectionTemplate.getName());
        if (existingIT != null) {
            throw new TemplateException("Inspection Template  <b>"+inspectionTemplate.getName()+"</b> is already exists");
        }
        templateDAO.saveOrUpdate(inspectionTemplate);
    }

    @Override
    public void updateTemplate(InspectionTemplate inspectionTemplate) throws ServiceException {
        if (inspectionTemplate == null) {
            throw new TemplateException("Inspection Template cannot be empty");
        }
        InspectionTemplate existingIT = getOtherInspectionTemplateByName(inspectionTemplate);
        if (existingIT != null && !(existingIT.getId().equals(inspectionTemplate.getId()))) {
            throw new TemplateException("Inspection Template <b>"+inspectionTemplate.getName()+"</b> already exists");
        }
        templateDAO.saveOrUpdate(inspectionTemplate);
    }

    @Override
    public void updateInspectionArea(InspectionArea iArea) throws ServiceException {
        if (iArea == null) {
            throw new TemplateException("Inspection Area cannot be empty");
        }
        InspectionArea existingIA = getOtherInspectionAreaByName(iArea);
        if (existingIA != null && !(existingIA.getId().equalsIgnoreCase(iArea.getId()))) {
            throw new TemplateException("Inspection Area <b>"+iArea.getName()+"</b> already exists");
        }
        templateDAO.saveOrUpdate(iArea);
    }

    @Override
    public void addInspectionArea(InspectionArea iArea) throws ServiceException {
        if (iArea == null) {
            throw new TemplateException("Inspection Area cannot be empty");
        }
        InspectionArea existingIA = getInspectionAreaByName(iArea.getInspectionTemplate(), iArea.getName());
        if (existingIA != null) {
            throw new TemplateException("Inspection Area <b>"+iArea.getName()+"</b> already exists");
        }
        templateDAO.saveOrUpdate(iArea);
    }

    private InspectionTemplate getOtherInspectionTemplateByName(InspectionTemplate inspectionTemplate) throws ServiceException{
        if (inspectionTemplate == null) {
            throw new TemplateException("Inspection Template cannot be empty");
        }
        if (inspectionTemplate.getCompany() == null) {
            throw new TemplateException("Inspection Template company not be empty");
        }
        if (StringUtil.isNullOrEmpty(inspectionTemplate.getId())) {
            throw new TemplateException("Inspection Template id cannot be empty");
        }
        return templateDAO.getOtherInspectionTemplateByName(inspectionTemplate);
    }
    
    private InspectionArea getOtherInspectionAreaByName(InspectionArea inspectionArea) throws ServiceException{
        if (inspectionArea == null) {
            throw new TemplateException("Inspection Area cannot be empty");
        }
        if (inspectionArea.getInspectionTemplate() == null) {
            throw new TemplateException("Inspection Area template not be empty");
        }
        if (StringUtil.isNullOrEmpty(inspectionArea.getId())) {
            throw new TemplateException("Inspection Area id cannot be empty");
        }
        return templateDAO.getOtherInspectionAreaByName(inspectionArea);
    }
}
