/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.jobwork;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.InventoryLocation;
import com.krawler.common.admin.User;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.SequenceFormat;
import com.krawler.hql.accounting.Vendor;
import com.krawler.inventory.model.ist.impl.InterStoreTransferDAOImpl;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.mrp.WorkOrder.WorkOrder;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class AccJobWorkDaoImpl extends BaseDAO implements AccJobWorkDao {

    @Override
    public KwlReturnObject saveJobWork(Map<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        JobWork jobwork = new JobWork();
        try {
            if (dataMap.containsKey(JobWork.ID) && !StringUtil.isNullOrEmpty((String) dataMap.get(JobWork.ID)) && !StringUtil.isNullOrEmpty(dataMap.get(JobWork.ID).toString())) {
                jobwork = (JobWork) get(JobWork.class, (String) dataMap.get(JobWork.ID));

            } else {
                if (dataMap.containsKey(JobWork.USERID) && !StringUtil.isNullOrEmpty((String) dataMap.get(JobWork.USERID))) {
                    User user = (User) get(User.class, (String) dataMap.get(JobWork.USERID));
                    if (user != null) {
                        jobwork.setCreator(user);
                    }
                }

            }
            if (dataMap.containsKey(JobWork.JOBORDERNAME) && !StringUtil.isNullOrEmpty((String) dataMap.get(JobWork.JOBORDERNAME))) {
                jobwork.setJobordername((String) dataMap.get(JobWork.JOBORDERNAME));
            }
            if (dataMap.containsKey(JobWork.SEQUENCEFORMAT) && !StringUtil.isNullOrEmpty((String) dataMap.get(JobWork.SEQUENCEFORMAT))) {

                SequenceFormat sf = (SequenceFormat) get(SequenceFormat.class, (String) dataMap.get(JobWork.SEQUENCEFORMAT));
                if (sf != null) {
                    jobwork.setSeqformat(sf);
                }
            }
            if (dataMap.containsKey(JobWork.JOBORDERNUMBER) && !StringUtil.isNullOrEmpty((String) dataMap.get(JobWork.JOBORDERNUMBER))) {
                jobwork.setJobordernumber((String) dataMap.get(JobWork.JOBORDERNUMBER));
            }
            if (dataMap.containsKey(JobWork.JOBWORKDATE) && !StringUtil.isNullObject(dataMap.get(JobWork.JOBWORKDATE))) {
                jobwork.setJobworkdate((Date) dataMap.get(JobWork.JOBWORKDATE));
            }
            if (dataMap.containsKey(JobWork.DATEOFDELIVERY) && !StringUtil.isNullObject(dataMap.get(JobWork.DATEOFDELIVERY))) {
                jobwork.setDateofdelivery((Date) dataMap.get(JobWork.DATEOFDELIVERY));
            }
            if (dataMap.containsKey(JobWork.VENDORID) && !StringUtil.isNullOrEmpty((String) dataMap.get(JobWork.VENDORID))) {

                Vendor ven = (Vendor) get(Vendor.class, (String) dataMap.get(JobWork.VENDORID));
                if (ven != null) {
                    jobwork.setVendorid(ven);
                }
            }
            if (dataMap.containsKey(JobWork.DATEOFSHIPMENT) && !StringUtil.isNullObject(dataMap.get(JobWork.DATEOFSHIPMENT))) {
                jobwork.setDateofshipment((Date) dataMap.get(JobWork.DATEOFSHIPMENT));
            }
            if (dataMap.containsKey(JobWork.EXCISEDUTYCHARGES) && !StringUtil.isNullOrEmpty((dataMap.get(JobWork.EXCISEDUTYCHARGES).toString()))) {
                jobwork.setExcisedutychargees((Double) dataMap.get(JobWork.EXCISEDUTYCHARGES));
            }
            if (dataMap.containsKey(JobWork.JOBWORKLOCATIONID) && !StringUtil.isNullOrEmpty((String) dataMap.get(JobWork.JOBWORKLOCATIONID))) {
                InventoryLocation loc = (InventoryLocation) get(InventoryLocation.class, (String) dataMap.get(JobWork.JOBWORKLOCATIONID));
                if (loc != null) {
                    jobwork.setJobworklocation(loc);
                }
            }
            if (dataMap.containsKey(JobWork.SHIPMENTROUTE) && !StringUtil.isNullOrEmpty((String) dataMap.get(JobWork.SHIPMENTROUTE))) {
                jobwork.setShipmentroute((String) dataMap.get(JobWork.SHIPMENTROUTE));
            }
            if (dataMap.containsKey(JobWork.GATEPASS) && !StringUtil.isNullOrEmpty((String) dataMap.get(JobWork.GATEPASS))) {
                jobwork.setGatepass((String) dataMap.get(JobWork.GATEPASS));
            }

            if (dataMap.containsKey(JobWork.OTHERREMARKS) && !StringUtil.isNullOrEmpty((String) dataMap.get(JobWork.OTHERREMARKS))) {
                jobwork.setOtherremarks((String) dataMap.get(JobWork.OTHERREMARKS));
            }
            if (dataMap.containsKey(JobWork.COMPANYID) && !StringUtil.isNullOrEmpty((String) dataMap.get(JobWork.COMPANYID))) {
                Company comp = (Company) get(Company.class, (String) dataMap.get(JobWork.COMPANYID));
                if (comp != null) {
                    jobwork.setCompanyid(comp);
                }
            }

            if (dataMap.containsKey(JobWork.PRODUCTID) && !StringUtil.isNullOrEmpty((String) dataMap.get(JobWork.PRODUCTID))) {
                Product prod = (Product) get(Product.class, (String) dataMap.get(JobWork.PRODUCTID));
                if (prod != null) {
                    jobwork.setProductid(prod);
                }
            }
            if (dataMap.containsKey(JobWork.WORKORDERID) && !StringUtil.isNullOrEmpty((String) dataMap.get(JobWork.WORKORDERID))) {
                WorkOrder prod = (WorkOrder) get(WorkOrder.class, (String) dataMap.get(JobWork.WORKORDERID));
                if (prod != null) {
                    jobwork.setWorkorderid(prod);
                }
            }
            if (dataMap.containsKey(JobWork.PRODUCTQUANTITY) && !StringUtil.isNullOrEmpty(dataMap.get(JobWork.PRODUCTQUANTITY).toString())) {
                jobwork.setProductquantity((Double) dataMap.get(JobWork.PRODUCTQUANTITY));
            }
            if (dataMap.containsKey(JobWork.USERID) && !StringUtil.isNullOrEmpty((String) dataMap.get(JobWork.USERID))) {
                User user = (User) get(User.class, (String) dataMap.get(JobWork.USERID));
                if (user != null) {
                    jobwork.setModifiedby(user);
                }
            }
            if (dataMap.containsKey(Constants.DATEPREFIX) && !StringUtil.isNullOrEmpty((String) dataMap.get(Constants.DATEPREFIX))) {
                jobwork.setDatePreffixValue((String) dataMap.get(Constants.DATEPREFIX));
            }
            if (dataMap.containsKey(Constants.DATEAFTERPREFIX) && !StringUtil.isNullOrEmpty((String) dataMap.get(Constants.DATEAFTERPREFIX))) {
                jobwork.setDateAfterPreffixValue((String) dataMap.get(Constants.DATEAFTERPREFIX));
            }
            if (dataMap.containsKey(Constants.DATESUFFIX) && !StringUtil.isNullOrEmpty((String) dataMap.get(Constants.DATESUFFIX))) {
                jobwork.setDateSuffixValue((String) dataMap.get(Constants.DATESUFFIX));
            }
            if (dataMap.containsKey(Constants.SEQNUMBER) && !StringUtil.isNullOrEmpty((String) dataMap.get(Constants.SEQNUMBER))) {
                jobwork.setSeqnumber(Integer.parseInt((String) dataMap.get(Constants.SEQNUMBER)));
            }
            if (dataMap.containsKey("autogenerated") && ((Boolean) dataMap.get("autogenerated"))) {
                jobwork.setAutoGenerated((Boolean) dataMap.get("autogenerated"));
            }
            if(dataMap.containsKey(JobWork.ACCJOBWORKCUSTOMDATAREF) && !StringUtil.isNullOrEmpty((String) dataMap.get(JobWork.ACCJOBWORKCUSTOMDATAREF))){
                JobWorkCustomData jobWorkCustomData = (JobWorkCustomData) get(JobWorkCustomData.class, jobwork.getId());
                jobwork.setAccJobWorkCustomData(jobWorkCustomData);
            }

            saveOrUpdate(jobwork);
            list.add(jobwork);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccJobWorkDaoImpl.saveJobWork", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject saveForecastTemplate(Map<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            ForecastTemplate forecastTemplate = null;
            if (dataMap.containsKey(JobWork.ID) && dataMap.get(JobWork.ID) != null) {
                forecastTemplate = (ForecastTemplate) get(ForecastTemplate.class, dataMap.get(JobWork.ID).toString());
            } else {
                forecastTemplate = new ForecastTemplate();
            }
            if (dataMap.containsKey(ForecastTemplate.TITLE) && dataMap.get(ForecastTemplate.TITLE) != null) {
                forecastTemplate.setTitle((String) dataMap.get(ForecastTemplate.TITLE));
            }
            if (dataMap.containsKey(ForecastTemplate.FORECASTID) && dataMap.get(ForecastTemplate.FORECASTID) != null) {
                forecastTemplate.setForecastId((String) dataMap.get(ForecastTemplate.FORECASTID));
            }
            if (dataMap.containsKey(ForecastTemplate.FORECASTYEARHISTORY) && dataMap.get(ForecastTemplate.FORECASTYEARHISTORY) != null) {
                forecastTemplate.setForecastYearHistory((String) dataMap.get(ForecastTemplate.FORECASTYEARHISTORY));
            }
            if (dataMap.containsKey(ForecastTemplate.FORECASTTYPE) && dataMap.get(ForecastTemplate.FORECASTTYPE) != null) {
                forecastTemplate.setForecastType((String) dataMap.get(ForecastTemplate.FORECASTTYPE));
            }
            if (dataMap.containsKey(ForecastTemplate.FORECASTMETHOD) && dataMap.get(ForecastTemplate.FORECASTMETHOD) != null) {
                forecastTemplate.setForecastMethod((String) dataMap.get(ForecastTemplate.FORECASTMETHOD));
            }
            if (dataMap.containsKey("createdon") && dataMap.get("createdon") != null) {
                forecastTemplate.setCreatedOn((Date) dataMap.get("createdon"));
            }
            if (dataMap.containsKey("updatedon") && dataMap.get("updatedon") != null) {
                forecastTemplate.setModifiedOn((Date) dataMap.get("updatedon"));
            }
            if (dataMap.containsKey(ForecastTemplate.FORECASTYEAR) && dataMap.get(ForecastTemplate.FORECASTYEAR) != null) {
                forecastTemplate.setForecastYear((Date) dataMap.get(ForecastTemplate.FORECASTYEAR));
            }
            if (dataMap.containsKey("createdby") && dataMap.get("createdby") != null) {
                forecastTemplate.setCreatedby((User) get(User.class, (String) dataMap.get("createdby")));
            }
            if (dataMap.containsKey("modifiedby") && dataMap.get("modifiedby") != null) {
                forecastTemplate.setModifiedby((User) get(User.class, (String) dataMap.get("modifiedby")));
            }
            if (dataMap.containsKey(Constants.companyid) && dataMap.get(Constants.companyid) != null) {
                Company company = (Company) get(Company.class, (String) dataMap.get(Constants.companyid));
                forecastTemplate.setCompany(company);
            }
            saveOrUpdate(forecastTemplate);
            list.add(forecastTemplate);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccJobWorkDaoImpl.saveForecastTemplate", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject saveForecastTemplateProductMapping(Map<String, Object> dataMap) throws ServiceException {

        List list = new ArrayList();
        try {
            Set<ForecastProductMapping> forecastProductMappings = new HashSet<>();
            ForecastTemplate forecastTemplate = null;
            if (dataMap.containsKey("product")) {
                String[] product = dataMap.get("product").toString().split(",");

                if (dataMap.containsKey("forecastTemplate")) {
                    forecastTemplate = (ForecastTemplate) dataMap.get("forecastTemplate");
                }
                for (int i = 0; i < product.length; i++) {
                    String productId = product[i];
                    ForecastProductMapping forecastProductMapping = new ForecastProductMapping();
                    forecastProductMapping.setForecastTemplate(forecastTemplate);
                    Product p = (Product) get(Product.class, productId);
                    if (p != null) {
                        forecastProductMapping.setProduct(p);
                    }
                    if (dataMap.containsKey(Constants.companyid) && dataMap.get(Constants.companyid) != null) {
                        Company company = (Company) get(Company.class, (String) dataMap.get(Constants.companyid));
                        forecastProductMapping.setCompany(company);
                    }
                    forecastProductMappings.add(forecastProductMapping);
                }
                forecastTemplate.setForecastProductMappings(forecastProductMappings);
                saveOrUpdate(forecastTemplate);
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE(" saveForecastTemplateProductMapping : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, null, null, list, list.size());

    }

    public KwlReturnObject getForecastTemplate(Map<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result = null;
        ArrayList params = new ArrayList();
        String companyId = "";
        String moduleId = "";
        boolean syncable = false;
        String conditionSql = "";
        String joinCondition = "";
        try {
            if (requestParams.containsKey("companyId")) {
                companyId = requestParams.get("companyId").toString();
                params.add(companyId);
            }
            if (requestParams.containsKey("billId")) {
                conditionSql += " and f.ID=?";
                params.add(requestParams.get("billId").toString());

            }
            if (requestParams.containsKey("moduleid")) {
                moduleId = (String) requestParams.get("moduleid");
            }
            if (requestParams.containsKey("ss") && requestParams.get("ss") != null) {
                String ss = requestParams.get("ss").toString();
                ss = ss.replaceAll("%", "////");
                ss = ss.replaceAll("_", "////");
                if (!StringUtil.isNullOrEmpty(ss)) {
                    String[] searchcol = new String[]{"f.forecastId","f.title","f.forecastYearHistory"};
                    Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 3);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    conditionSql += searchQuery;
                }
            }

            String query = "select distinct f from ForecastTemplate f " + joinCondition + " where f.company.companyID=? " + conditionSql;
            List list = executeQuery(query, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return result;
    }

    @Override
    public KwlReturnObject getJobWorkOrders(Map<String, Object> requestParams) throws ServiceException {
        List list = Collections.EMPTY_LIST;
        ArrayList paramList = new ArrayList();
        int totalCount = 0;
        try {
            String hql = "";
            String conditionHql = "";
            String moduleId = "";

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
            if (requestParams.containsKey(JobWork.COMPANYID) && !StringUtil.isNullOrEmpty((String) requestParams.get(JobWork.COMPANYID))) {
                conditionHql += " jbw.companyid.companyID= ? ";
                paramList.add((String) requestParams.get(JobWork.COMPANYID));
            }
            if (requestParams.containsKey(Constants.ss) && !StringUtil.isNullOrEmpty((String) requestParams.get(Constants.ss))) {
                String ss = requestParams.get("ss").toString();
                ss = ss.replaceAll("%", "////");
                ss = ss.replaceAll("_", "////");
                if (!StringUtil.isNullOrEmpty(ss)) {
                    String[] searchcol = new String[]{"jbw.jobordernumber", "jbw.jobordername"};
                    Map SearchStringMap = StringUtil.insertParamSearchStringMap(paramList, ss, 2);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    conditionHql += searchQuery;
                }
            }
            if (requestParams.containsKey(Constants.REQ_startdate) && !StringUtil.isNullObject(requestParams.get(Constants.REQ_startdate))) {
                Date startDate = (Date) requestParams.get(Constants.REQ_startdate);
                conditionHql += " and jbw.jobworkdate >= ? ";
                paramList.add(startDate);
            }
            if (requestParams.containsKey(Constants.REQ_enddate) && !StringUtil.isNullObject(requestParams.get(Constants.REQ_enddate))) {
                Date endDate = (Date) requestParams.get(Constants.REQ_enddate);
                conditionHql += " and jbw.jobworkdate <= ? ";
                paramList.add(endDate);
            }
            if (requestParams.containsKey(JobWork.JOBORDERNUMBER) && !StringUtil.isNullOrEmpty((String) requestParams.get(JobWork.JOBORDERNUMBER))) {
                conditionHql += " and jbw.jobordernumber= ? ";
                paramList.add((String) requestParams.get(JobWork.JOBORDERNUMBER));
            }
            if (requestParams.containsKey(JobWork.WORKORDERID) && !StringUtil.isNullOrEmpty((String) requestParams.get(JobWork.WORKORDERID))) {
                conditionHql += " and jbw.workorderid.ID= ? ";
                paramList.add((String) requestParams.get(JobWork.WORKORDERID));
            }
            if (requestParams.containsKey(JobWork.ID) && !StringUtil.isNullOrEmpty((String) requestParams.get(JobWork.ID))) {
                conditionHql += " and jbw.id != ? ";
                paramList.add((String) requestParams.get(JobWork.ID));
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
                        searchDefaultFieldSQL = (String) (map.containsKey("condition") ? map.get("condition") : "");
                        searchDefaultFieldSQL = searchDefaultFieldSQL.replaceAll("mrp_job_orderRef", "jbw");
                    }
                    if (customSearchFieldArray.length() > 0) {
                        /*
                         Advance Search For Custom fields
                         */
                        requestParams.put(Constants.Searchjson, Searchjson);
                        requestParams.put(Constants.appendCase, appendCase);
                        requestParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(requestParams, false).get(Constants.myResult));
                        if (mySearchFilterString.contains("c.JobWorkCustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("c.JobWorkCustomData", "jbw.accJobWorkCustomData");
                        }
                        StringUtil.insertParamAdvanceSearchString1(paramList, Searchjson);
                    }
                    mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
                }
            }

            hql = "select distinct jbw from JobWork jbw where " + conditionHql + mySearchFilterString;
            StringBuilder totalCountHql = new StringBuilder("select distinct jbw from JobWork jbw where " + conditionHql + mySearchFilterString);
            totalCount = executeQuery(totalCountHql.toString(), paramList.toArray()).size();
            if (pagingFlag) {
                list = executeQueryPaging(hql, paramList.toArray(), new Integer[]{start, limit});
            } else {
                list = executeQuery(hql, paramList.toArray());
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccJobWorkDaoImpl.getJobWorkOrders", ex);
        }
        return new KwlReturnObject(true, "", null, list, totalCount);
    }

    @Override
    public KwlReturnObject deleteJobWorkOrdersPerm(Map<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        List paramList = new ArrayList();
        try {
            String hql = "";
            String conditionHql = "";
            if (requestParams.containsKey(JobWork.COMPANYID) && !StringUtil.isNullOrEmpty((String) requestParams.get(JobWork.COMPANYID))) {
                conditionHql += " jbw.companyid.companyID= ? ";
                paramList.add((String) requestParams.get(JobWork.COMPANYID));
            }
            if (requestParams.containsKey(JobWork.ID) && !StringUtil.isNullOrEmpty((String) requestParams.get(JobWork.ID))) {
                conditionHql += " and jbw.id= ? ";
                paramList.add((String) requestParams.get(JobWork.ID));
            }

            hql += "delete from JobWork jbw where " + conditionHql;
            int count = executeUpdate(hql, paramList.toArray());
            list.add(count);    //ERP-30663 : Add count to identify whether record is deleted or not.
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccJobWorkDaoImpl.deleteJobWorkOrders", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject deleteJobWorkOrdersTemp(Map<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        List paramList = new ArrayList();
        int count = 0;
        try {

            if (requestParams.containsKey(JobWork.ID) && !StringUtil.isNullOrEmpty((String) requestParams.get(JobWork.ID))) {
                JobWork jobwork = (JobWork) get(JobWork.class, (String) requestParams.get(JobWork.ID));
                if (jobwork != null) {
                    jobwork.setDeleted(true);
                    count = 1;
                    list.add(count);    //ERP-30663 : Add count to identify whether record is deleted or not.
                }
            }
            
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccJobWorkDaoImpl.deleteJobWorkOrders", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    /*
     Function to return yearwise qty
     */

    public double getSalesQtyforModules(Map<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result = null;
        ArrayList params = new ArrayList();
        String companyId = "";
        String moduleId = "";
        double qty = 0d;
        boolean syncable = false;
        String conditionSql = "";
        String joinCondition = "";
        try {
            if (requestParams.containsKey("companyId")) {
                companyId = requestParams.get("companyId").toString();
                params.add(companyId);
            }
            if (requestParams.containsKey("prductid")) {
                conditionSql += " and sod.product=?";
                params.add(requestParams.get("prductid").toString());

            }
            if (requestParams.containsKey("moduleid")) {
                moduleId = (String) requestParams.get("moduleid");
            }
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String startDate = (String) requestParams.get(Constants.REQ_startdate);
            String endDate = (String) requestParams.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                conditionSql += " and (so.orderdate >=? and so.orderdate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            String query = "select sum(sod.quantity) from sodetails sod inner join salesorder so on so.id=sod.salesorder where sod.company=? " + conditionSql;
            List list = executeSQLQuery(query, params.toArray());
            if (list.size() > 0 && list.get(0) != null) {
                Double qua = Double.parseDouble(list.get(0).toString());
                qty = qua;
            }
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return qty;
    }

    public double getInvoiceQtyforModules(Map<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result = null;
        ArrayList params = new ArrayList();
        String companyId = "";
        String moduleId = "";
        double qty = 0d;
        boolean syncable = false;
        String conditionSql = "";
        String joinCondition = "";
        try {
            if (requestParams.containsKey("companyId")) {
                companyId = requestParams.get("companyId").toString();
                params.add(companyId);
            }
            if (requestParams.containsKey("prductid")) {
                conditionSql += " and inv.product=?";
                params.add(requestParams.get("prductid").toString());

            }
            if (requestParams.containsKey("moduleid")) {
                moduleId = (String) requestParams.get("moduleid");
            }
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String startDate = (String) requestParams.get(Constants.REQ_startdate);
            String endDate = (String) requestParams.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                conditionSql += " and (je.entrydate >=? and je.entrydate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            String query = "select sum(inv.actquantity) from inventory inv inner join invoicedetails invd on inv.id=invd.id inner join invoice i on i.id=invd.invoice "
                    + "left join journalentry je on je.id=i.journalentry where invd.company=? " + conditionSql;
            List list = executeSQLQuery(query, params.toArray());
            if (list.size() > 0 && list.get(0) != null) {
                Double qua = Double.parseDouble(list.get(0).toString());
                qty = qua;
            }
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return qty;
    }

    public double getDeliveryOrderQtyforModules(Map<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result = null;
        ArrayList params = new ArrayList();
        String companyId = "";
        String moduleId = "";
        double qty = 0d;
        boolean syncable = false;
        String conditionSql = "";
        String joinCondition = "";
        try {
            if (requestParams.containsKey("companyId")) {
                companyId = requestParams.get("companyId").toString();
                params.add(companyId);
            }
            if (requestParams.containsKey("prductid")) {
                conditionSql += " and dod.product=?";
                params.add(requestParams.get("prductid").toString());

            }
            if (requestParams.containsKey("moduleid")) {
                moduleId = (String) requestParams.get("moduleid");
            }
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String startDate = (String) requestParams.get(Constants.REQ_startdate);
            String endDate = (String) requestParams.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                conditionSql += " and (do.orderdate >=? and do.orderdate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            String query = "select sum(dod.deliveredquantity) from dodetails dod inner join deliveryorder do on do.id=dod.deliveryorder where dod.company=? " + conditionSql;
            List list = executeSQLQuery(query, params.toArray());
            if (list.size() > 0 && list.get(0) != null) {
                Double qua = Double.parseDouble(list.get(0).toString());
                qty = qua;
            }
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return qty;
    }

    public KwlReturnObject getForecastNumberCount(Map<String, Object> requestParams) throws ServiceException {
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
            String query = "from ForecastTemplate where forecastId=? and company.companyID=?" + condition;
            list = executeQuery(query, params.toArray());
            count = list.size();
            return new KwlReturnObject(true, "", null, list, count);

        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    /**
     *
     * @param dataMap = Contains forecast id to delete all entry for mapping
     * @return
     * @throws ServiceException
     */
    public KwlReturnObject deleteForecastTemplateProductMapping(Map<String, Object> dataMap) throws ServiceException {
        ArrayList params1 = new ArrayList();
        String id = "", delQuery1 = "";
        int numRows1 = 0;
        try {
            if (dataMap.containsKey("forecastTemplateId")) {
                params1.add(dataMap.get("forecastTemplateId"));
                delQuery1 = "delete from  forecastproductmapping where forecasttemplate=?";
                numRows1 = executeSQLUpdate(delQuery1, params1.toArray());
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteforecastproductmapping : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "forecast product mapping has been deleted successfully.", null, null, numRows1);

    }

    /**
     *
     * @param requestParams = forecast id and company id
     * @return
     * @Desc : delete forecast records
     * @throws ServiceException
     */
    public KwlReturnObject deleteForecast(Map<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result = null;
        ArrayList params = new ArrayList();
        String companyId = "";
        String conditionSql = "";
        try {
            if (requestParams.containsKey("companyId")) {
                companyId = requestParams.get("companyId").toString();
                params.add(companyId);
            }
            if (requestParams.containsKey("billId")) {
                conditionSql += " and ID=?";
                params.add(requestParams.get("billId").toString());
            }
            String mappedquery = "delete from ForecastProductMapping where company.companyID=? and forecastTemplate.ID=?";
            int mappednum = executeUpdate(mappedquery, params.toArray());

            String query = "delete from ForecastTemplate where company.companyID=? " + conditionSql;
            int num = executeUpdate(query, params.toArray());
            result = new KwlReturnObject(true, null, null, null, num);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return result;
    }
    /**
     *
     * @param requestParams
     * @return = Get Stock In data from Job work IN
     * @throws ServiceException
     */
    public KwlReturnObject getChallanReport(Map<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result = null;
        ArrayList params = new ArrayList();
        String companyId = "";
        String moduleId = "";
        double qty = 0d;
        boolean syncable = false;
        String conditionSql = "";
        String joinCondition = "";
        String innerconditionSql = "";
         String mySearchFilterString = " ";
         String searchJson = "";
         String appendCase = "and";
         String advancesearch="";
        try {

            if (requestParams.containsKey("moduleid")) {
                moduleId = (String) requestParams.get("moduleid");
            }
            if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null) {
                searchJson = requestParams.get("searchJson").toString();
            }
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (requestParams.containsKey("filterConjuctionCriteria") && requestParams.get("filterConjuctionCriteria") != null) {
                if (requestParams.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            
            
            if (requestParams.containsKey("companyId")) {
                companyId = requestParams.get("companyId").toString();
                params.add(companyId);
            }
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String startDate = (String) requestParams.get(Constants.REQ_startdate);
            String endDate = (String) requestParams.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                innerconditionSql += " and (sa.bussinessdate >=? and sa.bussinessdate <=?)";
                params.add(df.format(new Date(startDate)));
                params.add(df.format(new Date(endDate)));
            }
            String stockinCondition = "";
            if (requestParams.containsKey("customerid")) {
                innerconditionSql += " and so.customer=?";
                params.add(requestParams.get("customerid").toString());

            }
            if (requestParams.containsKey("ss") && requestParams.get("ss") != null) {
                String ss = requestParams.get("ss").toString();
                if (!StringUtil.isNullOrEmpty(ss)) {
                    ss = ss.replaceAll("%", "////");
                    ss = ss.replaceAll("_", "////");
                    if (!StringUtil.isNullOrEmpty(ss)) {
                        String[] searchcol = new String[]{"sad.batchname"};
                        Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 1);
                        StringUtil.insertParamSearchString(SearchStringMap);
                        String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                        innerconditionSql += searchQuery;
                    }
                }
            }
            if (requestParams.containsKey("companyId")) {
                companyId = requestParams.get("companyId").toString();
                params.add(companyId);
            }
            if (requestParams.containsKey("productid")) {
                conditionSql += " and p.id=?";
                params.add(requestParams.get("productid").toString());

            }
            if (!StringUtil.isNullOrEmpty(searchJson)) {                    
                    JSONObject serachJobj = new JSONObject(searchJson);
                    JSONArray customSearchFieldArray=new JSONArray();
                    JSONArray defaultSearchFieldArray=new JSONArray();  
                    StringUtil.seperateCostomAndDefaultSerachJson(serachJobj,customSearchFieldArray,defaultSearchFieldArray);
                    if (customSearchFieldArray.length() > 0) {
                        JSONObject tempjson=new JSONObject();
                        tempjson.put(Constants.root, customSearchFieldArray);
                        requestParams.put(Constants.Searchjson, tempjson.toString());
                        requestParams.put(Constants.appendCase, appendCase);
                        requestParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(requestParams, true).get(Constants.myResult));
                        mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "accproductcustomdata");
                        mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "accproductcustomdata");
                        mySearchFilterString = mySearchFilterString.replaceAll("accproductcustomdata", "accproductcustomdata");
                        advancesearch=" inner join accproductcustomdata on p.accproductcustomdataref=accproductcustomdata.productId ";
                        conditionSql+=mySearchFilterString;
                        StringUtil.insertParamAdvanceSearchString1(params, tempjson.toString());
                    }
                   
            }

                        String inventoryStockIN = " select p.id,p.productid , t1.bussinessdate, t1.seqno,t1.location,t1.store,t1.batchname , t1.finalquantity ,t1.name,t1.seqno,t1.jobworkorder, p.name as product, p.producttype,t1.sonumber,t1.stock,t1.salesOrderId  from product p   "
                    + advancesearch 
                    + "  inner join "
                    + " (select sa.id,sa.bussinessdate, sa.store,sa.seqno, sa.product,sad.batchname,sad.location,sad.finalquantity,c.name,sad.jobworkorder,so.sonumber,sa.id as stock,so.id as salesOrderId from in_stockadjustment sa "
                    + " inner join in_sa_detail sad on sa.id = sad.stockadjustment "
                    + " left join salesorder so on so.id = sad.jobworkorder  "
                    + " left join  customer c on so.customer=c.id  "          
                    + " where  sa.company = ? and sa.adjustment_type='Stock IN' " +innerconditionSql+" order by sa.bussinessdate asc ) as t1 on t1.product = p.id  "
                    + " where p.deleteflag = 'F' and p.isasset!='1' and p.company = ? and p.producttype  in  ('a839448c-7646-11e6-9648-14dda97925bd') " +conditionSql + " order by t1.bussinessdate ";
                        

            List stockinlist = executeSQLQuery(inventoryStockIN, params.toArray());

            result = new KwlReturnObject(true, null, null, stockinlist, stockinlist.size());
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return result;
    }
    /**
     *
     * @param requestParams
     * @return = Get Stock In data from Job work IN
     * @throws ServiceException
     */
    public KwlReturnObject getJWProductSummaryReportIN(Map<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result = null;
        ArrayList params = new ArrayList();
        ArrayList innerparams = new ArrayList();
        String companyId = "";
        String moduleId = "";
        double qty = 0d;
        boolean syncable = false;
        String conditionSql = "";
        String innerconditionSql = "";
        String joinCondition = "";
        try {

            

            if (requestParams.containsKey("moduleid")) {
                moduleId = (String) requestParams.get("moduleid");
            }
            if (requestParams.containsKey("companyId")) {
                companyId = requestParams.get("companyId").toString();
                params.add(companyId);
            }
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String startDate = (String) requestParams.get(Constants.REQ_startdate);
            String endDate = (String) requestParams.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                innerconditionSql += " and (sa.bussinessdate >=? and sa.bussinessdate <=?)";
                params.add(df.format(new Date(startDate)));
                params.add(df.format(new Date(endDate)));
            }
            String stockinCondition = "";
            if (requestParams.containsKey("customerid")) {
                innerconditionSql += " and so.customer=?";
                params.add(requestParams.get("customerid").toString());

            }
            if (requestParams.containsKey("ss") && requestParams.get("ss") != null) {
                String ss = requestParams.get("ss").toString();
                if (!StringUtil.isNullOrEmpty(ss)) {
                    ss = ss.replaceAll("%", "////");
                    ss = ss.replaceAll("_", "////");
                    if (!StringUtil.isNullOrEmpty(ss)) {
                        String[] searchcol = new String[]{"sad.batchname"};
                        Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 1);
                        StringUtil.insertParamSearchString(SearchStringMap);
                        String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                        innerconditionSql += searchQuery;
                    }
                }
            }
            if (requestParams.containsKey("companyId")) {
                companyId = requestParams.get("companyId").toString();
                params.add(companyId);
            }
            if (requestParams.containsKey("productid")) {
                conditionSql += " and p.id=?";
                params.add(requestParams.get("productid").toString());

            }
            String inventoryStockIN = " select p.id,p.productid , t1.bussinessdate, t1.seqno,t1.location,t1.store,group_concat(t1.batchname) as batchnames, sum(t1.finalquantity) as total ,t1.name,t1.seqno,Group_Concat(t1.jobworkorder) as jobworkorders, p.name as product, p.producttype from product p   "
                    + "  left join "
                    + " (select sa.id,sa.bussinessdate, sa.store,sa.seqno, sa.product,sad.batchname,sad.location,sad.finalquantity,c.name,sad.jobworkorder from in_stockadjustment sa "
                    + " inner join in_sa_detail sad on sa.id = sad.stockadjustment "
                    + " left join salesorder so on so.id = sad.jobworkorder  "
                    + " left join  customer c on so.customer=c.id  "
                    + " where  sa.company = ? and sa.adjustment_type='Stock IN' " +innerconditionSql+" ) as t1 on t1.product = p.id  "
                    + " where p.deleteflag = 'F' and p.isasset!='1' and p.company = ? and p.producttype  in  ('a839448c-7646-11e6-9648-14dda97925bd') " +conditionSql+" group by p.id  ";

            List stockinlist = executeSQLQuery(inventoryStockIN, params.toArray());
            result = new KwlReturnObject(true, null, null, stockinlist, stockinlist.size());
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return result;
    }
        
     /**
     *
     * @param requestParams
     * @return = Return consume details for product
     * @throws ServiceException
     */
    public KwlReturnObject getConsumeQtyForProduct(Map<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result = null;
        ArrayList params = new ArrayList();
        String companyId = "";
        String moduleId = "";
        String invoiceid = "";
        String batchName = "";
        String jobworkorder = "";
        double qty = 0d;
        boolean syncable = false;
        boolean jobWorkOderInAged = false;
        String conditionSql = "";
        String joinCondition = "";
        try {

            if (requestParams.containsKey("jobWorkOderInAged") && requestParams.get("jobWorkOderInAged") != null) {
                jobWorkOderInAged = Boolean.parseBoolean(requestParams.get("jobWorkOderInAged").toString());
            }
            if (requestParams.containsKey("productid")) {
                params.add(requestParams.get("productid").toString());

            }
            if (requestParams.containsKey("companyId")) {
                companyId = requestParams.get("companyId").toString();
                params.add(companyId);
            }
            if (requestParams.containsKey("customerid")) {
                conditionSql += " and do.customer=?";
                params.add(requestParams.get("customerid").toString());

            }

            if (requestParams.containsKey("jobworkorder")) {
                conditionSql += " and so.id=?";
                jobworkorder = requestParams.get("jobworkorder").toString();
                params.add(jobworkorder);
            }

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String startDate = requestParams.get("startdate") != null ? requestParams.get("startdate").toString() : "";
            String endDate = requestParams.get("enddate") != null ? requestParams.get("enddate").toString() : "";
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                conditionSql += " and (do.orderdate >=? and do.orderdate <=?) ";
                params.add(df.format(new Date(startDate)));
                params.add(df.format(new Date(endDate)));
            }
            
            if(requestParams.containsKey("batchName")) {
                conditionSql += " and npb.batchname=? ";
                batchName = requestParams.get("batchName").toString();
                params.add(batchName);
            }
            
            String consumeq = "select lbdm.quantity,pas.actualquantity,dodm.approvedquantity from dodetails dod"
                    + " inner join deliveryorder do on do.id=dod.deliveryorder "
                    + " inner join bomdetail bomd on bomd.id=dod.bomcode "
                    + " left join productassembly pas on pas.bomdetail=bomd.id "
                    + " left join locationbatchdocumentmapping lbdm on dod.id=lbdm.documentid "
                    + " left join newproductbatch npb on lbdm.batchmapid=npb.id "
                    + " left join sodetails sod on sod.id=dod.sodetails "
                    + " left join dodqcistmapping dodm on dodm.dodetailid=dod.id "
                    + " left join salesorder so on so.id=sod.salesorder "
                    + " where pas.subproducts =? and do.company=? and pas.product=dod.product " + conditionSql
                    
                    + " UNION ALL "
                    
                    + " select lbdm.quantity,pas.actualquantity,dodm.approvedquantity from dodetails dod "
                    + " inner join deliveryorder do on do.id=dod.deliveryorder "
                    + " inner join invoicedetails vdt ON vdt.id=dod.cidetails "
                    + " inner join bomdetail bomd on bomd.id=dod.bomcode "
                    + " inner join productassembly pas on pas.bomdetail=bomd.id "
                    + " inner join locationbatchdocumentmapping lbdm on dod.id=lbdm.documentid "
                    + " inner join newproductbatch npb on lbdm.batchmapid=npb.id "
                    + " inner join sodetails sod on sod.id=vdt.salesorderdetail "
                    + " inner join salesorder so on so.id=sod.salesorder "
                    + " left join dodqcistmapping dodm on dodm.dodetailid=dod.id "
                    + " where pas.subproducts =? and do.company=? and pas.product=dod.product " + conditionSql;

            params.addAll(params);
            List stockoutlist = executeSQLQuery(consumeq, params.toArray());

            result = new KwlReturnObject(true, null, null, stockoutlist, stockoutlist.size());
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return result;
    }
    /**
     *
     * @param requestParams
     * @return = Return consume details for product
     * @throws ServiceException
     */
    public KwlReturnObject getConsumeChallan(Map<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result = null;
        ArrayList params = new ArrayList();
        String companyId = "";
        String moduleId = "";
        String invoiceid = "";
        String batchName = "";
        String jobworkorder="";
        double qty = 0d;
        boolean syncable = false;
        String conditionSql = "";
        String joinCondition = "";
        try {
            if (requestParams.containsKey("productid")) {
                params.add(requestParams.get("productid").toString());

            }
            if (requestParams.containsKey("companyId")) {
                companyId = requestParams.get("companyId").toString();
                params.add(companyId);
            }
            if (requestParams.containsKey("batchName")) {
                batchName = requestParams.get("batchName").toString();
                params.add( batchName );
            }
            if (requestParams.containsKey("jobworkorder")) {
                conditionSql+=" and so.id=?";
                jobworkorder = requestParams.get("jobworkorder").toString();
                params.add(jobworkorder);
            }
            if (requestParams.containsKey("moduleid")) {
                moduleId = (String) requestParams.get("moduleid");
            }
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String startDate = requestParams.get("startdatelong") != null ? requestParams.get("startdatelong").toString() : "";
            String endDate = requestParams.get("enddatelong") != null ? requestParams.get("enddatelong").toString() : "";
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                conditionSql += " and (inv.createdon >=? and inv.createdon <=?)";
                params.add(startDate);
                params.add(endDate);
            }
            String stockinCondition = "";
            if (requestParams.containsKey("customerid")) {
                conditionSql += " and do.customer=?";
                params.add(requestParams.get("customerid").toString());

            }
                if (requestParams.containsKey("invoiceid")) {
                    invoiceid = (String) requestParams.get("invoiceid");
                    conditionSql += " and inv.id=?";
                    params.add(invoiceid);
                }
            String consumeq = "select lbdm.quantity,inv.invoicenumber,inv.createdon,pas.actualquantity from invoicedetails invd inner join  dodetails dod on invd.deliveryorderdetail=dod.id "
                        + " inner join invoice inv on inv.id=invd.invoice "
                        + " inner join deliveryorder do on do.id=dod.deliveryorder "
                        + " inner join bomdetail bomd on bomd.id=dod.bomcode left join productassembly pas on pas.bomdetail=bomd.id "
                        + " left join locationbatchdocumentmapping lbdm on dod.id=lbdm.documentid "
                        + " left join newproductbatch npb on lbdm.batchmapid=npb.id "
                        + " left join sodetails sod on sod.id=dod.sodetails "
                        + " left join salesorder so on so.id=sod.salesorder "
                        + " left join inventory invent on invent.id=invd.id "
                        + " where pas.subproducts =? and do.company=? and pas.product=dod.product and npb.batchname = ? " + conditionSql
                    
                        + " UNION "
                    
                        + " select lbdm.quantity,inv.invoicenumber,inv.createdon,pas.actualquantity from invoicedetails invd inner join  dodetails dod on dod.cidetails=invd.id "
                        + " inner join invoice inv on inv.id=invd.invoice "
                        + " inner join deliveryorder do on do.id=dod.deliveryorder "
                        + " inner join bomdetail bomd on bomd.id=dod.bomcode left join productassembly pas on pas.bomdetail=bomd.id "
                        + " left join locationbatchdocumentmapping lbdm on dod.id=lbdm.documentid "
                        + " left join newproductbatch npb on lbdm.batchmapid=npb.id "
                        + " left join sodetails sod on sod.id=invd.salesorderdetail "
                        + " left join salesorder so on so.id=sod.salesorder "
                        + " left join inventory invent on invent.id=invd.id "
                    + " where pas.subproducts =? and do.company=? and pas.product=dod.product and npb.batchname = ? " + conditionSql 
                    ;
                params.addAll(params);
            List stockoutlist = executeSQLQuery(consumeq, params.toArray());

            result = new KwlReturnObject(true, null, null, stockoutlist, stockoutlist.size());
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return result;
    }
    /**
     *
     * @param requestParams
     * @return = Return consume details for product
     * @throws ServiceException
     */
    public KwlReturnObject getJWProductSummaryReportOUT(Map<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result = null;
        ArrayList params = new ArrayList();
        String companyId = "";
        String moduleId = "";
        String invoiceid = "";
        String batchName = "";
        String jobworkorder="";
        double qty = 0d;
        boolean syncable = false;
        String conditionSql = "";
        String joinCondition = "";
        try {
            if (requestParams.containsKey("productid")) {
                params.add(requestParams.get("productid").toString());

            }
            if (requestParams.containsKey("companyId")) {
                companyId = requestParams.get("companyId").toString();
                params.add(companyId);
            }
            if (requestParams.containsKey("batchName")) {
                batchName = requestParams.get("batchName").toString();
                batchName = batchName.replaceAll("'", "''");
                batchName = batchName.replace("\\", "\\\\");
                batchName = batchName.replaceAll(",", "','");
                batchName = "'" + batchName + "'";
            }
            if (requestParams.containsKey("jobworkorder")) {
                jobworkorder = requestParams.get("jobworkorder").toString();
                 jobworkorder = jobworkorder.replaceAll(",", "','");
                jobworkorder = "'" + jobworkorder + "'";
                conditionSql+=" and so.id in ( " + jobworkorder + " ) ";
            }
            if (requestParams.containsKey("moduleid")) {
                moduleId = (String) requestParams.get("moduleid");
            }
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String startDate = requestParams.get("startdatelong").toString();
            String endDate = requestParams.get("enddatelong").toString();
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                conditionSql += " and (inv.createdon >=? and inv.createdon <=?)";
                params.add(startDate);
                params.add(endDate);
            }
            String stockinCondition = "";
            if (requestParams.containsKey("customerid")) {
                conditionSql += " and do.customer=?";
                params.add(requestParams.get("customerid").toString());

            }
            if (requestParams.containsKey("invoiceid")) {
                invoiceid = (String) requestParams.get("invoiceid");
                conditionSql += " and inv.id=?";
                params.add(invoiceid);
            }
            String consumeq = "select sum(lbdm.quantity),inv.invoicenumber,inv.createdon,pas.actualquantity from invoicedetails invd inner join  dodetails dod on invd.deliveryorderdetail=dod.id "
                    + " inner join invoice inv on inv.id=invd.invoice "
                    + " inner join deliveryorder do on do.id=dod.deliveryorder "
                    + " inner join bomdetail bomd on bomd.id=dod.bomcode left join productassembly pas on pas.bomdetail=bomd.id "
                    + " left join locationbatchdocumentmapping lbdm on dod.id=lbdm.documentid "
                    + " left join newproductbatch npb on lbdm.batchmapid=npb.id "
                    + " left join sodetails sod on sod.id=dod.sodetails "
                    + " left join salesorder so on so.id=sod.salesorder "
                    + " left join inventory invent on invent.id=invd.id "
                    + " where pas.subproducts =? and do.company=? and pas.product=dod.product and npb.batchname in ( "+batchName+ " ) " + conditionSql
                    
                    + " UNION "
                    
                    + " select sum(lbdm.quantity),inv.invoicenumber,inv.createdon,pas.actualquantity from invoicedetails invd inner join  dodetails dod on dod.cidetails=invd.id "
                    + " inner join invoice inv on inv.id=invd.invoice "
                    + " inner join deliveryorder do on do.id=dod.deliveryorder "
                    + " inner join bomdetail bomd on bomd.id=dod.bomcode left join productassembly pas on pas.bomdetail=bomd.id "
                    + " left join locationbatchdocumentmapping lbdm on dod.id=lbdm.documentid "
                    + " left join newproductbatch npb on lbdm.batchmapid=npb.id "
                    + " left join sodetails sod on sod.id=invd.salesorderdetail "
                    + " left join salesorder so on so.id=sod.salesorder "
                    + " left join inventory invent on invent.id=invd.id "
                    + " where pas.subproducts =? and do.company=? and pas.product=dod.product and npb.batchname in ( "+batchName+ " ) " + conditionSql
                    ;
            params.addAll(params);
            List stockoutlist = executeSQLQuery(consumeq, params.toArray());

            result = new KwlReturnObject(true, null, null, stockoutlist, stockoutlist.size());
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return result;
    }
}
