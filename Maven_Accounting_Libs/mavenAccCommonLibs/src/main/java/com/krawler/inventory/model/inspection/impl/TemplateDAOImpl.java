/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.inspection.impl;

import com.krawler.common.admin.Company;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.inventory.model.inspection.InspectionArea;
import com.krawler.inventory.model.inspection.InspectionTemplate;
import com.krawler.inventory.model.inspection.TemplateDAO;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Vipin Gupta
 */
public class TemplateDAOImpl extends BaseDAO implements TemplateDAO {

    @Override
    public InspectionTemplate getInspectionTemplate(String templateId) throws ServiceException {
        InspectionTemplate inspectionTemplate = null;
        if (!StringUtil.isNullOrEmpty(templateId)) {
            inspectionTemplate = (InspectionTemplate) get(InspectionTemplate.class, templateId);
        }
        return inspectionTemplate;
    }

    @Override
    public InspectionTemplate getInspectionTemplateByName(Company company, String templateName) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM InspectionTemplate WHERE company = ? AND name = ? ");
        List params = new ArrayList();
        params.add(company);
        params.add(templateName);
        List list = executeQueryPaging(hql.toString(), params.toArray(), new Paging(0, 1));
        InspectionTemplate inspectionTemplate = null;
        if (!list.isEmpty()) {
            inspectionTemplate = (InspectionTemplate) list.get(0);
        }
        return inspectionTemplate;
    }

    @Override
    public List<InspectionTemplate> getInspectionTemplateList(Company company, String searchString, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM InspectionTemplate WHERE company = ? ");
        List params = new ArrayList();
        params.add(company);
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND name LIKE ? ");
            params.add("%" + searchString + "%");
        }
        List list = executeQuery(hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            if (totalCount > paging.getLimit()) {
                list = executeQueryPaging(hql.toString(), params.toArray(), paging);
            }
        }
        return list;
    }

    @Override
    public void saveOrUpdate(Object object) throws ServiceException {
        super.saveOrUpdate(object);
    }

    @Override
    public void delete(Object object) throws ServiceException {
        super.delete(object);
    }

    @Override
    public InspectionArea getInspectionArea(String areaId) throws ServiceException {
        InspectionArea ia = null;
        if (!StringUtil.isNullOrEmpty(areaId)) {
            ia = (InspectionArea) get(InspectionArea.class, areaId);
        }
        return ia;
    }

    @Override
    public List<InspectionArea> getInspectionAreaList(InspectionTemplate inspectionTemplate, String searchString, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM InspectionArea WHERE inspectionTemplate = ? ");
        List params = new ArrayList();
        params.add(inspectionTemplate);
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND name LIKE ? ");
            params.add("%" + searchString + "%");
        }
        List list = executeQuery(hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null && paging.isValid()) {
            paging.setTotalRecord(totalCount);
            if (totalCount > paging.getLimit()) {
                list = executeQueryPaging(hql.toString(), params.toArray(), paging);
            }
        }
        return list;
    }

    @Override
    public InspectionArea getInspectionAreaByName(InspectionTemplate inspectionTemplate, String areaName) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM InspectionArea WHERE inspectionTemplate = ? AND name = ? ");
        List params = new ArrayList();
        params.add(inspectionTemplate);
        params.add(areaName);
        List list = executeQueryPaging(hql.toString(), params.toArray(), new Paging(0, 1));
        InspectionArea inspectionArea = null;
        if (!list.isEmpty()) {
            inspectionArea = (InspectionArea) list.get(0);
        }
        return inspectionArea;
    }

    @Override
    public InspectionTemplate getOtherInspectionTemplateByName(InspectionTemplate inspectionTemplate) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM InspectionTemplate WHERE company = ? AND name = ? AND id <> ? ");
        List params = new ArrayList();
        params.add(inspectionTemplate.getCompany());
        params.add(inspectionTemplate.getName());
        params.add(inspectionTemplate.getId());
        List list = executeQueryPaging(hql.toString(), params.toArray(), new Paging(0, 1));
        InspectionTemplate otherIT = null;
        if (!list.isEmpty()) {
            otherIT = (InspectionTemplate) list.get(0);
        }
        return otherIT;
    }

    @Override
    public InspectionArea getOtherInspectionAreaByName(InspectionArea inspectionArea) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM InspectionArea WHERE inspectionTemplate = ? AND name = ? AND id <> ? ");
        List params = new ArrayList();
        params.add(inspectionArea.getInspectionTemplate());
        params.add(inspectionArea.getName());
        params.add(inspectionArea.getId());
        List list = executeQueryPaging(hql.toString(), params.toArray(), new Paging(0, 1));
        InspectionArea otherIA = null;
        if (!list.isEmpty()) {
            otherIA = (InspectionArea) list.get(0);
        }
        return otherIA;
    }

    @Override
    public void deleteMapingWithProduct(InspectionTemplate inspectionTemplate) throws ServiceException {
        String hql = "UPDATE Product SET inspectionTemplate = NULL WHERE inspectionTemplate = ? ";
        List params = new ArrayList();
        params.add(inspectionTemplate);
        executeUpdate(hql, params.toArray());
    }
}
