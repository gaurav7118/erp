/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.gst;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.FieldComboData;
import com.krawler.common.admin.FieldParams;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.List;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.GstFormGenerationHistory;
import com.krawler.hql.accounting.MasterItem;
import com.krawler.hql.accounting.MultiEntityMapping;
import java.text.DateFormat;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author krawler
 */
public class AccGstDAOImpl extends BaseDAO implements AccGstDAO {

    @Override
    public KwlReturnObject getGstFormGenerationHistory(Map<String, Object> map) {
        List list = new ArrayList();
        int count=0;
        try {
            ArrayList params = new ArrayList();
            DateFormat df = (DateFormat) map.get(Constants.df);
            StringBuilder condition = new StringBuilder();
            String query = "from GstFormGenerationHistory ";
            String orderBy = "";
            
            if (map.containsKey("companyid") && map.get("companyid") != null) {
                String companyid = map.get("companyid").toString();
                condition.append(" where company.companyID=? ");
                params.add(companyid);
            }
            if (map.containsKey("id") && map.get("id") != null) {
                String id = map.get("id").toString();
                if (condition.indexOf("where") >= 0) {
                    condition.append(" and ID=?");
                } else {
                    condition.append(" where ID=? ");
                }
                params.add(id);
            }
            if (map.containsKey("startdate") && map.get("startdate") != null && map.containsKey("enddate") && map.get("enddate") != null) {
                Date startdate = df.parse(map.get("startdate").toString());
                Date enddate = df.parse(map.get("enddate").toString());
                if (condition.indexOf("where") >= 0) {
                    condition.append(" and generationDate >=? and generationDate <=?");
                } else {
                    condition.append(" where generationDate >=? and generationDate <=?");
                }
                params.add(startdate);
                params.add(enddate);
            }
            if (map.containsKey(Constants.multiEntityId) && map.get(Constants.multiEntityId) != null) {
                MultiEntityMapping entityMapping = getMultiEntityObjectFromEntityId(map);//To get MultiEntityMapping Object from multiEntityId(FieldComoData)
                if (entityMapping != null) {
                    condition.append(" and entityMapping.id = ?");
                    params.add(entityMapping.getId());
                }
            }
            if (map.containsKey(Constants.multiEntityValue) && map.get(Constants.multiEntityValue) != null) {
                KwlReturnObject entityMappingObj = getEntityDetails(map);//To get MultiEntityMapping Object from multiEntityValue(Master Item)
                MultiEntityMapping entityMapping = (MultiEntityMapping) entityMappingObj.getEntityList().get(0);
                if (entityMapping != null) {
                    condition.append(" and entityMapping.multiEntity.value = ?");
                    params.add(entityMapping.getMultiEntity().getValue());
                }
            }
            
            orderBy = " order by startDate DESC ";
            if (map.containsKey(Constants.isMultiEntity) && Boolean.parseBoolean((String) map.get(Constants.isMultiEntity))) {
                /*
                 * If MultiEntity is activated then GstFormGenerationHistory order by multiEntity and start date.
                 */
                orderBy = " order by entityMapping.multiEntity.value,startDate DESC";
            } else if (map.containsKey("searchForMaxStartDate") && map.get("searchForMaxStartDate") != null) {
                boolean searchForMaxStartDate = Boolean.parseBoolean(map.get("searchForMaxStartDate").toString());
                if (searchForMaxStartDate) {
                    orderBy = " order by startDate DESC limit 1 ";
                }
            }
            query += condition.toString();
            query += orderBy;
            list = executeQuery(query, params.toArray());
            if (list != null && !list.isEmpty()) {
                count = list.size();
            }
            String start = "";
            String limit = "";
            if (map.containsKey(Constants.start) && map.get(Constants.start) != null && map.containsKey(Constants.limit) && map.get(Constants.limit) != null) {
                start = (String) map.get(Constants.start);
                limit = (String) map.get(Constants.limit);
            }
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeQueryPaging(query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            Logger.getLogger(AccGstDAOImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    /**
     *
     * @param requestMap
     * @return GstFormGenerationHistory list
     * @throws com.krawler.common.service.ServiceException
     */
    @Override
    public List saveGstFormGenerationHistory(Map<String, Object> requestMap) throws ServiceException {
        List list = new ArrayList();
        try {
            GstFormGenerationHistory formGenerationHistory = new GstFormGenerationHistory();
            if (requestMap.containsKey(Constants.companyKey) && requestMap.get(Constants.companyKey) != null) {
                Company company = (Company) get(Company.class, (String) requestMap.get(Constants.companyKey));
                formGenerationHistory.setCompany(company);
            }
            if (requestMap.containsKey(Constants.userid) && requestMap.get(Constants.userid) != null) {
                User user = (User) get(User.class, (String) requestMap.get(Constants.userid));
                formGenerationHistory.setUser(user);
            }
            if (requestMap.containsKey("startDate") && requestMap.get("startDate") != null) {
                formGenerationHistory.setStartDate((Date) requestMap.get("startDate"));
            }
            if (requestMap.containsKey("endDate") && requestMap.get("endDate") != null) {
                formGenerationHistory.setEndDate((Date) requestMap.get("endDate"));
            }
            if (requestMap.containsKey("fileName") && requestMap.get("fileName") != null) {
                formGenerationHistory.setFileName(requestMap.get("fileName").toString());
            }
            if (requestMap.containsKey("generationDate") && requestMap.get("generationDate") != null) {
                formGenerationHistory.setGenerationDate((Date) requestMap.get("generationDate"));
            }
            /*
            *Save Entity wise GstFormGenerationHistory when Multi Entity activated
            */
            if (requestMap.containsKey(Constants.multiEntityValue) && !StringUtil.isNullOrEmpty((String) requestMap.get(Constants.multiEntityValue))) {
                KwlReturnObject entityMappingObj = getEntityDetails(requestMap);//To get MultiEntityMapping Object from multiEntityValue(Master Item)
                MultiEntityMapping entityMapping = (MultiEntityMapping) entityMappingObj.getEntityList().get(0);
                if (entityMapping != null) {
                    formGenerationHistory.setEntityMapping(entityMapping);
                }
            }
            /**
             * Default value of gstGuideVersion = 1 for new generated GST Form 03 as per 8th March 2018
             * & 0 for Dec 2017 & before.
             *
             */
            formGenerationHistory.setGstGuideVersion(Constants.GSTGuideMarch2018_Version);
            save(formGenerationHistory);
            list.add(formGenerationHistory);
        } catch (Exception ex) {
            Logger.getLogger(AccGstDAOImpl.class.getName()).log(Level.INFO, ex.getMessage());
            throw ServiceException.FAILURE("getBankReconciliationHistoryRows : " + ex.getMessage(), ex);
        }
        return list;
    }
    /**
     * @param requestMap
     * @return :KwlReturnObject
     * @throws com.krawler.common.service.ServiceException
     */
    @Override
    public KwlReturnObject saveEntityMapping(Map<String, Object> requestMap) throws ServiceException {
        List list = new ArrayList();
        try {
            MultiEntityMapping multiEntityMapping = null;
            Boolean isEdit = false;
            String id = "";
            String auditUpdatedRecMsg = "";
            String entityName = "";
                        if (requestMap.containsKey(Constants.isEdit)) {
                isEdit = (Boolean) requestMap.get(Constants.isEdit);
            }
            if(requestMap.containsKey("id")){
                id = (String) requestMap.get("id");
            }
            if(isEdit){
                multiEntityMapping = (MultiEntityMapping) get(MultiEntityMapping.class, id);
                entityName = multiEntityMapping.getMultiEntity().getValue();
            }else{
                multiEntityMapping = new MultiEntityMapping();
                entityName = (String) requestMap.get("multiEntity");
            }
            
            if (requestMap.containsKey("multiEntityId") && requestMap.get("multiEntityId") != null) {
                FieldComboData comboData = (FieldComboData) get(FieldComboData.class, (String)requestMap.get("multiEntityId"));
                if (isEdit && !multiEntityMapping.getMultiEntity().getId().equals(requestMap.get("multiEntityId"))) {
                    auditUpdatedRecMsg += "<br>[Field : Entity : Old Value = " + multiEntityMapping.getMultiEntity().getValue() + ", New Value = " + requestMap.get("multiEntity") + "]";
                }
                multiEntityMapping.setMultiEntity(comboData);
            }
            if (requestMap.containsKey("multiEntitygstno") && requestMap.get("multiEntitygstno") != null) {
                if (isEdit && multiEntityMapping.getGstNumber() != null && !multiEntityMapping.getGstNumber().equals(requestMap.get("multiEntitygstno"))) {
                    auditUpdatedRecMsg += "<br>[Field : Trade Register Number : Old Value = " + multiEntityMapping.getGstNumber() + ", New Value = " + requestMap.get("multiEntitygstno") + "]";
                }                
                multiEntityMapping.setGstNumber((String) requestMap.get("multiEntitygstno"));
            }
            if (requestMap.containsKey("multiEntitytaxNumber") && requestMap.get("multiEntitytaxNumber") != null) {
                if (isEdit && multiEntityMapping.getTaxNumber() != null && !multiEntityMapping.getTaxNumber().equals(requestMap.get("multiEntitytaxNumber"))) {
                    auditUpdatedRecMsg += "<br>[Field : Tax Number : Old Value = " + multiEntityMapping.getTaxNumber() + ", New Value = " + requestMap.get("multiEntitytaxNumber") + "]";
                }
                multiEntityMapping.setTaxNumber((String) requestMap.get("multiEntitytaxNumber"));
            }
            if (requestMap.containsKey("multiEntitycompanybrn") && requestMap.get("multiEntitycompanybrn") != null) {
                if (isEdit && multiEntityMapping.getCompanyBRN() != null && !multiEntityMapping.getCompanyBRN().equals(requestMap.get("multiEntitycompanybrn"))) {
                    auditUpdatedRecMsg += "<br>[Field : Company BRN : Old Value = " + multiEntityMapping.getCompanyBRN() + ", New Value = " + requestMap.get("multiEntitycompanybrn") + "]";
                }
                multiEntityMapping.setCompanyBRN((String) requestMap.get("multiEntitycompanybrn"));
            }
            if (requestMap.containsKey(Constants.companyid) && requestMap.get(Constants.companyid) != null) {
                Company company = (Company) get(Company.class, (String) requestMap.get(Constants.companyid));
                multiEntityMapping.setCompany(company);
            }
            
            if (requestMap.containsKey("industryCodeId") && requestMap.get("industryCodeId") != null) {
                String str = (String) requestMap.get("industryCodeId");
                if (!StringUtil.isNullOrEmpty(str)&& !str.equals("-1")) {//None = -1
                    MasterItem masterItem = (MasterItem) get(MasterItem.class, str);
                    String industryCode = multiEntityMapping.getIndustryCode() != null ? multiEntityMapping.getIndustryCode().getValue() : "None";
                    if (isEdit && masterItem != null && !industryCode.equals(masterItem.getValue())) {
                        auditUpdatedRecMsg += "<br>[Field : Primary MSIC Code : Old Value = " + industryCode + ", New Value = " + masterItem.getValue() + "]";
                    }
                    multiEntityMapping.setIndustryCode(masterItem);
                } else {
                    multiEntityMapping.setIndustryCode(null);
                }
            }
            if (requestMap.containsKey("gstSubmissionPeriod") && requestMap.get("gstSubmissionPeriod") != null) {
                if (isEdit && multiEntityMapping.getGstSubmissionPeriod() != (int) requestMap.get("gstSubmissionPeriod")) {
                    String oldVal = multiEntityMapping.getGstSubmissionPeriod() == 0 ? "Monthly " : "Quarterly";
                    String newVal = (int) requestMap.get("gstSubmissionPeriod") == 0 ? "Monthly " : "Quarterly";
                    auditUpdatedRecMsg += "<br>[Field : GST Submission Period : Old Value = " + oldVal + ", New Value = " + newVal + "]";
                }
                multiEntityMapping.setGstSubmissionPeriod((int) requestMap.get("gstSubmissionPeriod"));
            }
            
            saveOrUpdate(multiEntityMapping);
            list.add(multiEntityMapping);
            list.add(auditUpdatedRecMsg);
            list.add(entityName);
        } catch (Exception ex) {
            Logger.getLogger(AccGstDAOImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    /**
     *
     * @param requestMap
     * @return :KwlReturnObject of Multi Entity Details
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getEntityDetails(Map<String, Object> requestMap) throws ServiceException {
        ArrayList params = new ArrayList();
        List list = null;
        try {
            String condition = "";
            if (requestMap.containsKey(Constants.companyid)) {
                params.add(requestMap.get(Constants.companyid));
            }
            if (requestMap.containsKey(Constants.multiEntityId) && requestMap.get(Constants.multiEntityId) != null) {
                condition += " and multiEntity.id = ?";
                params.add(requestMap.get(Constants.multiEntityId));
            }
            if (requestMap.containsKey(Constants.multiEntityValue) && requestMap.get(Constants.multiEntityValue) != null) {
                condition += " and multiEntity.value = ?";
                params.add(requestMap.get(Constants.multiEntityValue));
            }
            String hql = "from MultiEntityMapping where company.companyID = ? " + condition;
            list = executeQuery(hql, params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(AccGstDAOImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    /**
     *
     * @param requestParams
     * @return Multi Entity List
     * @throws ServiceException
     */
    @Override
    public List getMultiEntityForCombo(Map<String,Object> requestParams) throws ServiceException {
        List<FieldParams> list = null;
        try {
            ArrayList params = new ArrayList();
            if(requestParams.containsKey(Constants.companyid)){
                params.add(requestParams.get(Constants.companyid));
            }
            String hql = " select cd from FieldComboData cd inner join cd.field fp where fp.company.companyID =? and fp.GSTConfigType = 1 group by cd.value order by cd.itemsequence";
            list = executeQuery(hql,params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(AccGstDAOImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return list;
    }
    
    /**
     *
     * @param requestParams
     * @return :KwlReturnObject
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject deleteEntityMapping(Map<String,Object>requestParams) throws ServiceException{
        ArrayList params = new ArrayList();
        int count = 0;
        try{
            if(requestParams.containsKey(Constants.companyid)){
                params.add(requestParams.get(Constants.companyid));
            }
            if(requestParams.containsKey("id")){
                params.add(requestParams.get("id"));
            }
            String hql = "delete from MultiEntityMapping where company.companyID=? and id=?";
            count = executeUpdate(hql, params.toArray());
            
        }catch(Exception ex){
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, null, count);
    }
    
    /**
     *
     * @param requestParams
     * @return return MultiEntityMapping Object from multiEntityId(FieldComoData)
     */
    public MultiEntityMapping getMultiEntityObjectFromEntityId(Map<String,Object> requestParams){
        MultiEntityMapping entityMappingObj = null;
        try{
            ArrayList params = new ArrayList();
            if(requestParams.containsKey(Constants.companyid)){
                params.add(requestParams.get(Constants.companyid));
            }
            if(requestParams.containsKey(Constants.multiEntityId)){
                params.add(requestParams.get(Constants.multiEntityId));
            }
            String query = " from MultiEntityMapping where company.companyID =? and multiEntity.id = ?";
            entityMappingObj = (MultiEntityMapping) executeQuery(query, params.toArray()).get(0);
        }catch(Exception ex){
            Logger.getLogger(AccGstDAOImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return entityMappingObj;
    }
    
    @Override
    public KwlReturnObject getGSTFormGenerationHistoryDetails(Map<String, Object> map) throws ServiceException {
        List list = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            StringBuilder condition = new StringBuilder();
            String query = "from GstFormGenerationHistory ";
            String multiEntityValue = "";
            String companyid = map.get("companyid").toString();
            condition.append(" where company.companyID=? ");
            params.add(companyid);

            if (map.containsKey(Constants.multiEntityValue) && !StringUtil.isNullOrEmpty((String) map.get(Constants.multiEntityValue))) {
                KwlReturnObject entityMappingObj = getEntityDetails(map);//To get MultiEntityMapping Object from multiEntityValue(Master Item)
                MultiEntityMapping entityMapping = (MultiEntityMapping) entityMappingObj.getEntityList().get(0);
                if (entityMapping != null) {
                    multiEntityValue = entityMapping.getMultiEntity().getValue();
                    condition.append(" and entityMapping.multiEntity.value = ?");
                    params.add(multiEntityValue);
                }
            }
            if (map.containsKey("endDate") && map.get("endDate") != null) {
                /*
                * Target is to select end date of already ganarated history where the requested end date by user should fall in between 
                * start date and end date of history.
                * Ex : If user select 01-03-2017 as end date in request, then resultant end date from table will be 31-03-2017(Monthly Basis) as this date
                * is greater than requested date.
                */
                
                condition.append(" and endDate = (select min(endDate) from GstFormGenerationHistory gst where gst.company.companyID = ? and gst.endDate >= ? ");
                params.add(companyid);
                params.add(map.get("endDate"));
                if (!StringUtil.isNullOrEmpty(multiEntityValue)) {
                    condition.append(" and gst.entityMapping.multiEntity.value = ? )");
                    params.add(multiEntityValue);
                } else {
                    condition.append(" )");
                }
            }
            query += condition.toString();
            list = executeQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccGstDAOImpl.getGSTFormGenerationHistoryDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
   /**
     *Function to execute query to delete GST Form 03 Generation History
     * @param requestParams
     * @return :KwlReturnObject
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject deleteGSTFileGenerationHistory(Map<String,Object>requestParams) throws ServiceException{
        ArrayList params = new ArrayList();
        int count = 0;
            if (requestParams.containsKey(Constants.companyid) && requestParams.get(Constants.companyid) != null) {
                params.add(requestParams.get(Constants.companyid));
            }
            if(requestParams.containsKey("id") && requestParams.get("id")!=null){
                params.add(requestParams.get("id"));
            }
            String hql = "delete from GstFormGenerationHistory where company.companyID=? and ID=?";
            count = executeUpdate(hql, params.toArray());
        return new KwlReturnObject(true, null, null, null, count);
    }
    /**
     *
     * @param requestParams
     * @return return GST Form 5 submission details Object
     */
    public KwlReturnObject getGSTForm5Details(Map<String, Object> requestParams) {
        List list = null;
        try {
            ArrayList params = new ArrayList();
            if (requestParams.containsKey(Constants.companyid)) {
                params.add(requestParams.get(Constants.companyid));
            }
            String query = " from GstForm5eSubmissionDetails where company.companyID =?";
            list = executeQuery(query, params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(AccGstDAOImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getTransactionListingDetails(Map<String, Object> requestParams) {
        List list = null;
        String filter = "";
        try {
            ArrayList params = new ArrayList();
            if (requestParams.containsKey(Constants.companyid)) {
                params.add(requestParams.get(Constants.companyid));
            }
            if (requestParams.containsKey("startDate") && requestParams.containsKey("endDate")) {
                filter =" AND dtPeriodStart>=? AND dtPeriodEnd<=? ";
                params.add((Date)requestParams.get("startDate"));
                params.add((Date)requestParams.get("endDate"));
            }
            String query = " from GSTTransactionListingSubmissionDetails where company.companyID =? "+filter+" Order by currentChunk";
            list = executeQuery(query, params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(AccGstDAOImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

}
