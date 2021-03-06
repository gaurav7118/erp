/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.routingmanagement;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.BOMDetail;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.SequenceFormat;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.mrp.WorkOrder.WorkOrder;
import com.krawler.spring.mrp.jobwork.JobWork;
import com.krawler.spring.mrp.labormanagement.Labour;
import com.krawler.spring.mrp.machinemanagement.Machine;
import com.krawler.spring.mrp.workcentremanagement.WorkCentre;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class AccRoutingManagementDaoImpl extends BaseDAO implements AccRoutingManagementDao {

    @Override
    public KwlReturnObject saveRoutingTemplate(Map<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        RoutingTemplate routingtemplate = null;
        try {
            if (dataMap.containsKey(RoutingTemplate.RTID) && !StringUtil.isNullOrEmpty((String) dataMap.get(RoutingTemplate.RTID)) && !StringUtil.isNullOrEmpty(dataMap.get(RoutingTemplate.RTID).toString())) {
                routingtemplate = (RoutingTemplate) get(RoutingTemplate.class, (String) dataMap.get(RoutingTemplate.RTID));

            } else {
                routingtemplate = new RoutingTemplate();
                if (dataMap.containsKey(RoutingTemplate.USERID) && !StringUtil.isNullOrEmpty((String) dataMap.get(RoutingTemplate.USERID))) {
                    User user = (User) get(User.class, (String) dataMap.get(RoutingTemplate.USERID));
                    if (user != null) {
                        routingtemplate.setCreatedby(user);
                    }
                }
                if (dataMap.containsKey(RoutingTemplate.CREATEDON) && !StringUtil.isNullObject((Date) dataMap.get(RoutingTemplate.CREATEDON))) {
                    routingtemplate.setCreatedOn((Date) dataMap.get(RoutingTemplate.CREATEDON));
                }
            }
            if (dataMap.containsKey("routecode") && dataMap.get("routecode") != null) {
                routingtemplate.setRoutecode((String) dataMap.get("routecode"));
            }
            if (dataMap.containsKey(Constants.SEQFORMAT) && dataMap.get(Constants.SEQFORMAT) != null) {   //sometimes sequenceformat may have null
                routingtemplate.setSeqformat((SequenceFormat) get(SequenceFormat.class, (String) dataMap.get(Constants.SEQFORMAT)));
            }
            if (dataMap.containsKey(Constants.SEQNUMBER)) {
                routingtemplate.setSeqnumber(Integer.parseInt(dataMap.get(Constants.SEQNUMBER).toString()));
            }
            if (dataMap.containsKey(Constants.DATEPREFIX) && dataMap.get(Constants.DATEPREFIX) != null) {
                routingtemplate.setDatePreffixValue((String) dataMap.get(Constants.DATEPREFIX));
            }
            if (dataMap.containsKey(Constants.DATEAFTERPREFIX) && dataMap.get(Constants.DATEAFTERPREFIX) != null) {
                routingtemplate.setDateAfterPreffixValue((String) dataMap.get(Constants.DATEAFTERPREFIX));
            }
            if (dataMap.containsKey(Constants.DATESUFFIX) && dataMap.get(Constants.DATESUFFIX) != null) {
                routingtemplate.setDateSuffixValue((String) dataMap.get(Constants.DATESUFFIX));
            }
            if (dataMap.containsKey("autogenerated") && dataMap.get("autogenerated")!=null) {
                routingtemplate.setAutoGenerated((Boolean) dataMap.get("autogenerated"));
            }
            if (dataMap.containsKey(RoutingTemplate.WORKORDER) && !StringUtil.isNullOrEmpty((String) dataMap.get(RoutingTemplate.WORKORDER))) {
                WorkOrder wo = (WorkOrder) get(WorkOrder.class, (String) dataMap.get(RoutingTemplate.WORKORDER));
                if (wo != null) {
                    routingtemplate.setWorkOrder(wo);
                }
            }
            if (dataMap.containsKey("isroutingcode") && dataMap.get("isroutingcode") != null) {
                routingtemplate.setIsRoutingCode((Boolean) dataMap.get("isroutingcode"));
            } else {
                routingtemplate.setIsRoutingCode(false);
            }
            if (dataMap.containsKey(RoutingTemplate.RTNAME) && !StringUtil.isNullOrEmpty((String) dataMap.get(RoutingTemplate.RTNAME))) {
                routingtemplate.setName((String) dataMap.get(RoutingTemplate.RTNAME));
            }
            if (dataMap.containsKey(RoutingTemplate.PROJECTID) && !StringUtil.isNullOrEmpty((String) dataMap.get(RoutingTemplate.PROJECTID))) {
                routingtemplate.setProjectId((String) dataMap.get(RoutingTemplate.PROJECTID));
            }
            if (dataMap.containsKey(RoutingTemplate.BOMID) && !StringUtil.isNullOrEmpty((String) dataMap.get(RoutingTemplate.BOMID))) {
                BOMDetail bom = (BOMDetail) get(BOMDetail.class, (String) dataMap.get(RoutingTemplate.BOMID));
                if (bom != null) {
                    routingtemplate.setBomid(bom);
                }
            }
            if (dataMap.containsKey(RoutingTemplate.WORKCENTER) && !StringUtil.isNullOrEmpty((String) dataMap.get(RoutingTemplate.WORKCENTER))) {
                WorkCentre wc = (WorkCentre) get(WorkCentre.class, (String) dataMap.get(RoutingTemplate.WORKCENTER));
                if (wc != null) {
                    routingtemplate.setWorkCenter(wc);
                }
            }
            if (dataMap.containsKey(RoutingTemplate.DURATIONTYPE) && !StringUtil.isNullOrEmpty(dataMap.get(RoutingTemplate.DURATIONTYPE).toString())) {
                routingtemplate.setDurationType((Integer) dataMap.get(RoutingTemplate.DURATIONTYPE));
            }
            if (dataMap.containsKey(RoutingTemplate.DURATION) && !StringUtil.isNullOrEmpty(dataMap.get(RoutingTemplate.DURATION).toString())) {
                routingtemplate.setDuration(Integer.parseInt((String) dataMap.get(RoutingTemplate.DURATION)));
            }
            if (dataMap.containsKey(RoutingTemplate.USERID) && !StringUtil.isNullOrEmpty((String) dataMap.get(RoutingTemplate.USERID))) {
                User user = (User) get(User.class, (String) dataMap.get(RoutingTemplate.USERID));
                if (user != null) {
                    routingtemplate.setModifiedby(user);
                }
            }
            if (dataMap.containsKey(JobWork.COMPANYID) && !StringUtil.isNullOrEmpty((String) dataMap.get(JobWork.COMPANYID))) {
                Company comp = (Company) get(Company.class, (String) dataMap.get(JobWork.COMPANYID));
                if (comp != null) {
                    routingtemplate.setCompany(comp);
                }
            }
            if (dataMap.containsKey(RoutingTemplate.CREATEDON) && !StringUtil.isNullObject((Date) dataMap.get(RoutingTemplate.CREATEDON))) {
                routingtemplate.setUpdatedOn((Date) dataMap.get(RoutingTemplate.CREATEDON));
            }
              if (dataMap.containsKey("productid") && !StringUtil.isNullObject((String) dataMap.get("productid"))) {
                Product product = (Product) get(Product.class, (String) dataMap.get("productid"));
                if (product != null) {
                    routingtemplate.setProduct(product);
                }
            }
            if (dataMap.containsKey(RoutingTemplate.PARENTRTID) && !StringUtil.isNullObject((String) dataMap.get(RoutingTemplate.PARENTRTID))) {
                RoutingTemplate routingTemplate = (RoutingTemplate) get(RoutingTemplate.class, (String) dataMap.get(RoutingTemplate.PARENTRTID));
                if (routingTemplate != null) {
                    routingtemplate.setParentId(routingTemplate);
                }
            }
            if (dataMap.containsKey(RoutingTemplate.ACCROUTINGTEMPLATECUSTOMDATA) && !StringUtil.isNullOrEmpty((String) dataMap.get(RoutingTemplate.ACCROUTINGTEMPLATECUSTOMDATA))) {
                RoutingTemplateCustomData routingTemplateCustomData = (RoutingTemplateCustomData) get(RoutingTemplateCustomData.class, routingtemplate.getId());
                routingtemplate.setAccRoutingTemplateCustomData(routingTemplateCustomData);
            }
            saveOrUpdate(routingtemplate);
            list.add(routingtemplate);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccRoutingManagementDaoImpl.saveRoutingTemplate", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject saveRoutingTemplateMachineMapping(Map<String, Object> machineMappingDataMap) throws ServiceException {
        List ll = new ArrayList();
        Set<RoutingTemplateMachineMapping> mappingSet = new HashSet();
        RoutingTemplateMachineMapping rtmachinemapping = null;
        try {
            RoutingTemplate routingTemplate = (RoutingTemplate) machineMappingDataMap.get("routingtemplateObj");
            String rtid = routingTemplate.getId();
            String[] machineIDs = machineMappingDataMap.get(RoutingTemplate.MACHINEMAPPING).toString().split(",");
            for (int i = 0; i < machineIDs.length; i++) {
                if (!StringUtil.isNullOrEmpty(machineIDs[i])) {
                    rtmachinemapping = new RoutingTemplateMachineMapping();

                    Machine machine = (Machine) get(Machine.class, machineIDs[i]);
                    if (machine != null) {
                        rtmachinemapping.setMachineid(machine);
                    }
                    RoutingTemplate rt = (RoutingTemplate) get(RoutingTemplate.class, rtid);
                    if (rt != null) {
                        rtmachinemapping.setRoutingtemplate(rt);
                    }
                    mappingSet.add(rtmachinemapping);
                }
            }
            routingTemplate.setMachinemapping(mappingSet);
            save(routingTemplate);
            ll.add(mappingSet);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("WorkOrderDAOImpl.saveWorkOrderLabourMapping: " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, ll, ll.size());
    }

    @Override
    public KwlReturnObject saveRoutingTemplateLabourMapping(Map<String, Object> LabourMappingDataMap) throws ServiceException {
        List ll = new ArrayList();
        Set<RoutingTemplateLabourMapping> mappingSet = new HashSet();
        RoutingTemplateLabourMapping rtlabourmapping = null;
        try {
            RoutingTemplate routingTemplate = (RoutingTemplate) LabourMappingDataMap.get("routingtemplateObj");
            String rtid = routingTemplate.getId();
            String[] labourIDs = LabourMappingDataMap.get(RoutingTemplate.LABOURMAPPING).toString().split(",");
            for (int i = 0; i < labourIDs.length; i++) {
                if (!StringUtil.isNullOrEmpty(labourIDs[i])) {
                    rtlabourmapping = new RoutingTemplateLabourMapping();

                    Labour labour = (Labour) get(Labour.class, labourIDs[i]);
                    if (labour != null) {
                        rtlabourmapping.setLabourid(labour);
                    }
                    RoutingTemplate rt = (RoutingTemplate) get(RoutingTemplate.class, rtid);
                    if (rt != null) {
                        rtlabourmapping.setRoutingtemplate(rt);
                    }
                    mappingSet.add(rtlabourmapping);
                }
            }
            routingTemplate.setLabourmapping(mappingSet);
            save(routingTemplate);
            ll.add(mappingSet);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("WorkOrderDAOImpl.saveWorkOrderLabourMapping: " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, ll, ll.size());
    }

    @Override
    public KwlReturnObject deleteRoutingTemplateMappings(Map<String, Object> deleteParams) throws ServiceException {
        List ll = new ArrayList();
        List params = new ArrayList();
        int count = 0;
        try {
            String hql = "";
            String condition = "";
            if (deleteParams.containsKey(WorkOrder.POJO)) {
                String pojoname = deleteParams.get(WorkOrder.POJO).toString();
                hql = " DELETE from " + pojoname;

                if (deleteParams.containsKey(WorkOrder.ATTRIBUTE) && deleteParams.containsKey(WorkOrder.WOID)) {
                    String conditionAttribute = deleteParams.get(WorkOrder.ATTRIBUTE).toString();
                    condition = " where " + conditionAttribute + "= ?";
                    params.add(deleteParams.get(WorkOrder.WOID));
                }
                hql = hql + condition;
                count = executeUpdate(hql, params.toArray());
                ll.add(count);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccRoutingManagementDaoImpl.deleteRoutingTemplateMappings: " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, ll, count);
    }

    @Override
    public KwlReturnObject getRoutingTemplates(Map<String, Object> requestParams) throws ServiceException {
        List list = Collections.EMPTY_LIST;
        List countList = Collections.EMPTY_LIST;
        ArrayList paramList = new ArrayList();
        try {
            String hql = "";
            String countHql = "";
            String conditionHql = " where ";
            String joinCondition = "";
            String moduleId = "";
            Boolean checkDuplicateName=false;

            int start = 0;
            int limit = 30;
            boolean pagingFlag = false;
            if (requestParams.containsKey("start") && requestParams.containsKey("limit") && !StringUtil.isNullOrEmpty(requestParams.get("start").toString())) {
                start = Integer.parseInt(requestParams.get("start").toString());
                limit = Integer.parseInt(requestParams.get("limit").toString());
                pagingFlag = true;
            }
            if (requestParams.containsKey("moduleid")) {
                moduleId = (String) requestParams.get("moduleid");
            }
            if (requestParams.containsKey("checkDuplicateName")) {
                checkDuplicateName = (Boolean) requestParams.get("checkDuplicateName");
            }
            if (requestParams.containsKey(RoutingTemplate.COMPANYID) && !StringUtil.isNullOrEmpty((String) requestParams.get(RoutingTemplate.COMPANYID))) {
                conditionHql += " rt.company.companyID= ? ";
                paramList.add((String) requestParams.get(RoutingTemplate.COMPANYID));
            }
            if (requestParams.containsKey(RoutingTemplate.BOMID) && !StringUtil.isNullOrEmpty((String) requestParams.get(RoutingTemplate.BOMID))) {
                conditionHql += " and rt.bomid.ID= ? ";
                paramList.add((String) requestParams.get(RoutingTemplate.BOMID));
            }
            if (requestParams.containsKey(RoutingTemplate.RTID) && !StringUtil.isNullOrEmpty((String) requestParams.get(RoutingTemplate.RTID))) {
                conditionHql += " and rt.id != ? ";
                paramList.add((String) requestParams.get(RoutingTemplate.RTID));
            }
            if (requestParams.containsKey(RoutingTemplate.RTNAME) && !StringUtil.isNullOrEmpty((String) requestParams.get(RoutingTemplate.RTNAME))) {
                conditionHql += " and rt.name= ? ";
                paramList.add((String) requestParams.get(RoutingTemplate.RTNAME));
            }
            if (requestParams.containsKey(Constants.ss) && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.ss))) {
                String ss = requestParams.get("ss").toString();
                ss = ss.replaceAll("%", "////");
                ss = ss.replaceAll("_", "////");
                if (!StringUtil.isNullOrEmpty(ss)) {
                    String[] searchcol = new String[]{"rt.name"};
                    Map SearchStringMap = StringUtil.insertParamSearchStringMap(paramList, ss, 1);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    conditionHql += searchQuery;
                }
            }
            if (requestParams.containsKey("isRoutingCode") && requestParams.get("isRoutingCode") != null) {
                boolean isRoutingCode = Boolean.parseBoolean(requestParams.get("isRoutingCode").toString());
                if (isRoutingCode) {
                    conditionHql += " and rt.isRoutingCode=true";
                } else {
                    conditionHql += " and rt.isRoutingCode=false";
                }
            }
            if (requestParams.containsKey(Constants.REQ_startdate) && !StringUtil.isNullObject(requestParams.get(Constants.REQ_startdate)) && requestParams.containsKey(Constants.REQ_enddate) && !StringUtil.isNullObject(requestParams.get(Constants.REQ_enddate))) {
                Date startDate = (Date) requestParams.get(Constants.REQ_startdate);
                Date endDate = (Date) requestParams.get(Constants.REQ_enddate);
                conditionHql += " and (rt.createdOn >= ? and rt.createdOn <= ? ) ";
                paramList.add(startDate);
                paramList.add(endDate);
            }
                if (!checkDuplicateName) {
                    conditionHql += " and rt.parentId is NULL "; 
                }
            /*
             Advance Search Component
             */
            String appendCase = "and";
            String mySearchFilterString = "";
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (requestParams.containsKey("filterConjuctionCriteria") && requestParams.get("filterConjuctionCriteria") != null) {
                if (requestParams.get("filterConjuctionCriteria").toString().trim().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            String Searchjson = "";
            String searchDefaultFieldSQL = "";
            if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null) {
                Searchjson = requestParams.get("searchJson").toString();
                if (!StringUtil.isNullOrEmpty(Searchjson)) {
                    JSONObject serachJobj = new JSONObject(Searchjson);
                    JSONArray customSearchFieldArray = new JSONArray();
                    JSONArray defaultSearchFieldArray = new JSONArray();
                    StringUtil.seperateCostomAndDefaultSerachJson(serachJobj, customSearchFieldArray, defaultSearchFieldArray);
                    if (defaultSearchFieldArray.length() > 0) {
                        /*
                         Advance Search For Default Form fields
                         */
                        ArrayList tableArray = new ArrayList();
                        Map<String, Object> map = buildSqlDefaultFieldAdvSearch(defaultSearchFieldArray, paramList, moduleId, tableArray, filterConjuctionCriteria);
                        joinCondition += " LEFT JOIN rt.machinemapping rtm ";
                        joinCondition += " LEFT JOIN rt.labourmapping rtl ";
                        
                        searchDefaultFieldSQL = (String) (map.containsKey("condition") ? map.get("condition") : "");
                        searchDefaultFieldSQL = searchDefaultFieldSQL.replaceAll("routing_templateRef.machinemapping", "rtm");
                        searchDefaultFieldSQL = searchDefaultFieldSQL.replaceAll("routing_templateRef.labourmapping", "rtl");
                        searchDefaultFieldSQL = searchDefaultFieldSQL.replaceAll("routing_templateRef", "rt");
                    }
                    if (customSearchFieldArray.length() > 0) {
                        /*
                         Advance Search For Custom fields
                         */
                        requestParams.put(Constants.Searchjson, Searchjson);
                        requestParams.put(Constants.appendCase, appendCase);
                        requestParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(requestParams, false).get(Constants.myResult));
                        if (mySearchFilterString.contains("c.RoutingTemplateCustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("c.RoutingTemplateCustomData", "rt.accRoutingTemplateCustomData");
                        }
                        StringUtil.insertParamAdvanceSearchString1(paramList, Searchjson);
                    }
                    mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
                }
            }
            hql = "select distinct rt from RoutingTemplate rt " + joinCondition + conditionHql + mySearchFilterString;
            countHql = "select distinct rt from RoutingTemplate rt " + joinCondition + conditionHql + mySearchFilterString;
            if (pagingFlag) {
                list = executeQueryPaging(hql, paramList.toArray(), new Integer[]{start, limit});
            } else {
                list = executeQuery(hql, paramList.toArray());
            }
            countList = executeQuery(hql, paramList.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccRoutingManagementDaoImpl.getRoutingTemplates", ex);
        }
        return new KwlReturnObject(true, "", null, list, countList.size());
    }
    /**
     * 
     * @param requestParams
     * @return
     * @throws ServiceException 
     * @Desc : Return RC Count for Sequence Format
     */
    public KwlReturnObject getRCNumberCount(Map<String, Object> requestParams) throws ServiceException {
        try {
            List list = new ArrayList();
            String billid = "";
            String entryNumber = "";
            String companyId = "";
            int count = 0;
            String condition = "";
            ArrayList params = new ArrayList();
            if (requestParams.containsKey("entryNumber")) {
                entryNumber = requestParams.get("entryNumber").toString();
                params.add(entryNumber);
            }
            if (requestParams.containsKey("companyId")) {
                companyId = requestParams.get("companyId").toString();
                params.add(companyId);
            }

            if (requestParams.containsKey("billid")) {
                billid = requestParams.get("billid").toString();
                condition = " and ID!=?";
                params.add(billid);
            }
            String query = "from RoutingTemplate where routecode=? and company.companyID=?" + condition;
            list = executeQuery(query, params.toArray());
            count = list.size();
            return new KwlReturnObject(true, "", null, list, count);

        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    @Override
    public KwlReturnObject deleteRoutingTemplatePerm(Map<String, Object> requestParams) throws ServiceException {
        List list = Collections.EMPTY_LIST;
        List paramList = new ArrayList();
        try {
            String hql = "";
            String conditionHql = "";
            if (requestParams.containsKey(JobWork.COMPANYID) && !StringUtil.isNullOrEmpty((String) requestParams.get(JobWork.COMPANYID))) {
                conditionHql += " rt.company.companyID= ? ";
                paramList.add((String) requestParams.get(JobWork.COMPANYID));
            }
            if (requestParams.containsKey(JobWork.ID) && !StringUtil.isNullOrEmpty((String) requestParams.get(JobWork.ID))) {
                conditionHql += " and rt.id= ? ";
                paramList.add((String) requestParams.get(JobWork.ID));
            }

            hql += "delete from RoutingTemplate rt where " + conditionHql;
            int count = executeUpdate(hql, paramList.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccRoutingManagementDaoImpl.deleteRoutingTemplatePerm", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject deleteRoutingTemplateCustomData(Map<String, Object> requestParams) throws ServiceException {
        List list = Collections.EMPTY_LIST;
        List paramList = new ArrayList();
        try {
            String hql = "";
            String conditionHql = "";
            if (requestParams.containsKey(JobWork.COMPANYID) && !StringUtil.isNullOrEmpty((String) requestParams.get(JobWork.COMPANYID))) {
                conditionHql += " rt.company.companyID= ? ";
                paramList.add((String) requestParams.get(JobWork.COMPANYID));
            }
            if (requestParams.containsKey(JobWork.ID) && !StringUtil.isNullOrEmpty((String) requestParams.get(JobWork.ID))) {
                conditionHql += " and routingTemplate.id= ? ";
                paramList.add((String) requestParams.get(JobWork.ID));
            }

            hql += "delete from RoutingTemplateCustomData rt where " + conditionHql;
            int count = executeUpdate(hql, paramList.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccRoutingManagementDaoImpl.deleteRoutingTemplatePerm", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getChildTemplate(Map<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String parentid = "";
        List list = null;
        int count = 0;
        try {
            if (requestParams.containsKey("parentid")) {
                parentid = requestParams.get("parentid").toString();
                params.add(parentid);
            }
            String query = "select id from routing_template where parentid=?";
            list = executeSQLQuery(query, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("LabourIDAlreadyPresent : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
}
