/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.workcentremanagement;

import com.krawler.accounting.utils.KWLErrorMsgs;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.CostCenter;
import com.krawler.common.admin.InventoryWarehouse;
import com.krawler.common.admin.User;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.mrp.labormanagement.Labour;
import com.krawler.spring.mrp.labormanagement.LabourCustomData;
import com.krawler.spring.mrp.labormanagement.LabourWorkCentreMapping;
import com.krawler.spring.mrp.machinemanagement.Machine;
import com.krawler.spring.mrp.machinemanagement.MachineWorkCenterMapping;
import com.krawler.spring.mrp.workcentremanagement.WorkCentre;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.*;

/**
 *
 * @author krawler
 */
public class WorkCentreDAOImpl extends BaseDAO implements WorkCentreDAO{

    @Override
    public KwlReturnObject saveWorkCentre(JSONObject jobj) throws ServiceException {
        List ll = new ArrayList();
        int dl = 0;
        boolean isEdit = false;
        try {
            DateFormat df = authHandler.getDateOnlyFormat();  
            WorkCentre workCentre = null;
            String id = "";
            if (jobj.has("isEdit")) {
                isEdit = jobj.getBoolean("isEdit");
            }
            if(isEdit) {
                if (jobj.has(WorkCentre.WCID)) {
                    id = jobj.getString("id");
                }
                workCentre = (WorkCentre) get(WorkCentre.class, id);
            } else {
                workCentre = new WorkCentre();
                id = UUID.randomUUID().toString();
//                workOrder.setID(id);
            }
            if (jobj.has(WorkCentre.SEQUENCEFORMAT)) {
                String tempStr = jobj.getString(WorkCentre.SEQUENCEFORMAT);
                workCentre.setSeqformat((SequenceFormat) get(SequenceFormat.class, tempStr));
            }
            if (jobj.has(WorkCentre.WORKCENTRENAME)) {
                String tempStr = jobj.getString(WorkCentre.WORKCENTRENAME);
                workCentre.setName(tempStr);
            }
            if (jobj.has(WorkCentre.WORKCENTREID)) {
                String tempStr = jobj.getString(WorkCentre.WORKCENTREID);
                workCentre.setWorkcenterid(tempStr);
            }
            if (jobj.has(workCentre.WORKCENTERLOCATIONID)) {
                String tempStr = jobj.getString(workCentre.WORKCENTERLOCATIONID);
                workCentre.setWorkcenterlocation((MasterItem) get(MasterItem.class, tempStr));
            }
            if (jobj.has(workCentre.WORKCENTRECAPACITY)) {
                double tempStr = jobj.optDouble(workCentre.WORKCENTRECAPACITY,0.0);
                workCentre.setWorkcentercapacity(tempStr);
            }
            if (jobj.has(workCentre.WORKTYPEID)) {
                String tempStr = jobj.getString(workCentre.WORKTYPEID);
                workCentre.setWorktype((MasterItem) get(MasterItem.class, tempStr));
            }
            if (jobj.has(workCentre.WAREHOUSEID)) {
                String tempStr = jobj.getString(workCentre.WAREHOUSEID);
                workCentre.setWarehouseid((InventoryWarehouse) get(InventoryWarehouse.class, tempStr));
            }
            if (jobj.has(workCentre.WORKCENTREMANAGERID)) {
                String tempStr = jobj.getString(workCentre.WORKCENTREMANAGERID);
                workCentre.setWorkcentermanager((MasterItem) get(MasterItem.class, tempStr));
            }
            if (jobj.has(workCentre.COMPANYID)) {
                String tempStr = jobj.getString(workCentre.COMPANYID);
                workCentre.setCompany((Company) get(Company.class, tempStr));
            }
            if (jobj.has(workCentre.COSTCENTERID)) {
                String tempStr = jobj.getString(workCentre.COSTCENTERID);
                workCentre.setCostcenter((CostCenter) get(CostCenter.class, tempStr));
            }
            if (!isEdit) {
                if (jobj.has(WorkCentre.CREATEDBYID)) {
                    String tempStr = jobj.getString(WorkCentre.CREATEDBYID);
                    workCentre.setCreatedby((User) get(User.class, tempStr));
                }
            }
            if (jobj.has(WorkCentre.MODIFIEDBYID)) {
                String tempStr = jobj.getString(WorkCentre.MODIFIEDBYID);
                workCentre.setModifiedby((User) get(User.class, tempStr));
            }
            if (jobj.has(workCentre.WORKCENTRETYPEID)) {
                String tempStr = jobj.getString(workCentre.WORKCENTRETYPEID);
                workCentre.setWorkcentertype((MasterItem) get(MasterItem.class, tempStr));
            }
            if (jobj.has(Constants.DATEPREFIX) && !StringUtil.isNullOrEmpty((String) jobj.get(Constants.DATEPREFIX))) {
                workCentre.setDatePreffixValue((String) jobj.get(Constants.DATEPREFIX));
            }
            if (jobj.has(Constants.DATEAFTERPREFIX) && !StringUtil.isNullOrEmpty((String) jobj.get(Constants.DATEAFTERPREFIX))) {
                workCentre.setDateAfterPreffixValue((String) jobj.get(Constants.DATEAFTERPREFIX));
            }
            if (jobj.has(Constants.DATESUFFIX) && !StringUtil.isNullOrEmpty((String) jobj.get(Constants.DATESUFFIX))) {
                workCentre.setDateSuffixValue((String) jobj.get(Constants.DATESUFFIX));
            }
            if (jobj.has(Constants.SEQNUMBER) && !StringUtil.isNullOrEmpty((String) jobj.get(Constants.SEQNUMBER))) {
                workCentre.setSeqnumber(Integer.parseInt((String) jobj.get(Constants.SEQNUMBER)));
            }
            if (jobj.has("autogenerated") && ((Boolean) jobj.get("autogenerated"))) {
                workCentre.setAutoGenerated((Boolean) jobj.get("autogenerated"));
            }
            if (jobj.has("accworkcentrecustomdataref")) {
                WorkCentreCustomData workCentreCustomData = null;
                workCentreCustomData = (WorkCentreCustomData) get(WorkCentreCustomData.class, (String) jobj.get("accworkcentrecustomdataref"));
                workCentre.setAccWorkCentreCustomData(workCentreCustomData);
            }
            saveOrUpdate(workCentre);
            ll.add(workCentre);
        } catch (Exception ex) {
            logger.warn(ex.getMessage(), ex);
            throw ServiceException.FAILURE("WorkOrderDAOImpl:SaveWorkOrder " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, "", ll, dl);
    }
    
     @Override
    public KwlReturnObject getWorkCentres(Map<String, Object> requestParams) throws ServiceException {
        List list = Collections.EMPTY_LIST;
        List countList = Collections.EMPTY_LIST;
        ArrayList paramList = new ArrayList();
        String start = "";
        String limit = "";
        String moduleId = "";
        String condition = "", joinCondition = "";
        try {
            String hql = "";
            String countHql = "";
            String conditionHql = "";
            String workcetnerids = "";
            if (requestParams.containsKey("workcenterids")) {
                workcetnerids = requestParams.get("workcenterids").toString();
            }
            if (requestParams.containsKey("companyid") && !StringUtil.isNullObject(requestParams.get("companyid"))) {
                conditionHql += " wc.company.companyID= ? ";
                paramList.add((String)requestParams.get("companyid"));
            }
            if (!StringUtil.isNullOrEmpty(workcetnerids)) {
                //to show workcetner which is used in current workorder
                workcetnerids = AccountingManager.getFilterInString(workcetnerids);
                conditionHql += " and wc.ID in " + workcetnerids + "  ";
            }
            if (requestParams.containsKey(WorkCentre.WORKCENTREID) && !StringUtil.isNullOrEmpty((String) requestParams.get(WorkCentre.WORKCENTREID))) {
                conditionHql += " and wc.workcenterid= ? ";
                paramList.add((String) requestParams.get(WorkCentre.WORKCENTREID));
            }
            if (requestParams.containsKey("wcid") && !StringUtil.isNullOrEmpty((String) requestParams.get("wcid"))) {
                conditionHql += " and wc.ID != ? ";
                paramList.add((String) requestParams.get("wcid"));
            }
            
            String selectedIds = (String) requestParams.get("ids");//Geting only Selected records for Export.
            if (!StringUtil.isNullOrEmpty(selectedIds)) {
                selectedIds = AccountingManager.getFilterInString(selectedIds);
                condition += " and wc.ID in " + selectedIds + " ";
            }
            
            String ss = (requestParams.containsKey("ss") && requestParams.get("ss") != null) ? (String) requestParams.get("ss") : "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"wc.name", "wc.workcenterid"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(paramList, ss, 2);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery;
            }
            if (requestParams.containsKey("moduleid")) {
                moduleId = (String) requestParams.get("moduleid");
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
                        joinCondition += " LEFT JOIN wc.machineworkcentremappings mcwc ";
                        joinCondition += " LEFT JOIN wc.labourworkcentremappings lwc ";
                        joinCondition += " LEFT JOIN wc.productworkcentremappings pwc ";
                        joinCondition += " LEFT JOIN wc.materialworkcentremappings mtwm ";
                        
                        searchDefaultFieldSQL = (String) (map.containsKey("condition") ? map.get("condition") : "");
                        searchDefaultFieldSQL = searchDefaultFieldSQL.replaceAll("workcenterRef.machineworkcentremappings", "mcwc");
                        searchDefaultFieldSQL = searchDefaultFieldSQL.replaceAll("workcenterRef.labourworkcentremappings", "lwc");
                        searchDefaultFieldSQL = searchDefaultFieldSQL.replaceAll("workcenterRef.productworkcentremappings", "pwc");
                        searchDefaultFieldSQL = searchDefaultFieldSQL.replaceAll("workcenterRef.materialworkcentremappings", "mtwm");
                        searchDefaultFieldSQL = searchDefaultFieldSQL.replaceAll("workcenterRef", "wc");
                    }
                    if (customSearchFieldArray.length() > 0) {
                        /*
                         Advance Search For Custom fields
                         */
                        requestParams.put(Constants.Searchjson, Searchjson);
                        requestParams.put(Constants.appendCase, appendCase);
                        requestParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(requestParams, false).get(Constants.myResult));
                        if (mySearchFilterString.contains("c.WorkCentreCustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("c.WorkCentreCustomData", "wc.accWorkCentreCustomData");
                        }
                        StringUtil.insertParamAdvanceSearchString1(paramList, Searchjson);
                    }
                    mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
                }
            }
            hql += " select distinct wc from WorkCentre wc " + joinCondition + " where " + conditionHql + condition + mySearchFilterString;
            countHql += " select distinct wc.ID from WorkCentre wc " + joinCondition + " where " + conditionHql + condition + mySearchFilterString;
            if (requestParams.containsKey(WorkCentre.PRODUCTID) && !StringUtil.isNullOrEmpty((String) requestParams.get(WorkCentre.PRODUCTID))) {
                conditionHql += " and wc.workcenterid= ? ";
                paramList.clear();
                paramList.add((String) requestParams.get(WorkCentre.PRODUCTID));
                hql = "select pwcm.workCenterID from ProductWorkCentreMapping pwcm where pwcm.productid.id = ? and pwcm.workCenterID.deleted='F'"; /* Fetched values of work centre which are not deleted temp. */
            }
            
            
            if (requestParams.containsKey(Constants.start) && requestParams.get(Constants.start) != null && requestParams.containsKey(Constants.limit) && requestParams.get(Constants.limit) != null) {
                start = (String) requestParams.get(Constants.start);
                limit = (String) requestParams.get(Constants.limit);
            }

            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeQueryPaging(hql, paramList.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            } else {
                list = executeQuery(hql, paramList.toArray());
            }
            countList = executeQuery(hql, paramList.toArray());
            
            
            
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccJobWorkDaoImpl.getJobWorkOrders", ex);
        }
        return new KwlReturnObject(true, "", null, list, countList.size());
    }
     
     @Override
    public KwlReturnObject deleteWorkCentres(Map<String, Object> requestParams) throws ServiceException {
       List list = Collections.EMPTY_LIST;
        List paramList = new ArrayList();
        try {
            String hql = "";
            String conditionHql = "";
            if (requestParams.containsKey(WorkCentre.COMPANYID) && !StringUtil.isNullObject(requestParams.get(WorkCentre.COMPANYID))) {
                conditionHql += " wc.company.companyID= ? ";
                paramList.add((String)requestParams.get(WorkCentre.COMPANYID));
            }
            if (requestParams.containsKey(WorkCentre.WCID) && !StringUtil.isNullObject(requestParams.get(WorkCentre.WCID))) {
                conditionHql += " and wc.id= ? ";
                paramList.add((String)requestParams.get(WorkCentre.WCID));
            }
            hql += "delete from WorkCentre wc where " + conditionHql;
           int count= executeUpdate(hql, paramList.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccJobWorkDaoImpl.deleteJobWorkOrders", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getWorkCentreCombo(Map<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String companyId = "";
        if (requestParams.containsKey("companyId")) {
            companyId = requestParams.get("companyId").toString();
            params.add(companyId);
        }
        String query = "select id,name,workcenterid from workcenter where company=?";
        List list = executeSQLQuery(query, params.toArray());
        int count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    @Override
    public Set<LabourWorkCentreMapping> getLabourWCMapping(Map<String, Object>  request) throws ServiceException {
        Set<LabourWorkCentreMapping> labourWorkCentreMappings = new HashSet<LabourWorkCentreMapping>();
        LabourWorkCentreMapping labourWorkCentreMapping = null;
        String labourId = "";
        if (!StringUtil.isNullObject(request.get(WorkCentre.LABOURID))) {
            labourId = request.get(WorkCentre.LABOURID).toString();
            String [] labourIdArr = labourId.split(",");
            String wcid = request.get(WorkCentre.WCID).toString();
            String companyid = request.get(WorkCentre.COMPANYID).toString();
            for (int count = 0; count< labourIdArr.length; count++) {
                labourWorkCentreMapping = new LabourWorkCentreMapping();
                labourWorkCentreMapping.setLabour((Labour) get(Labour.class, (String) labourIdArr[count]));
                labourWorkCentreMapping.setWorkCentre((WorkCentre) get(WorkCentre.class, wcid));
                labourWorkCentreMapping.setCompany((Company) get(Company.class, companyid));
                labourWorkCentreMappings.add(labourWorkCentreMapping);
            }
        }

        return labourWorkCentreMappings;
    }
    @Override
    public Set<MachineWorkCenterMapping> getMachineWCMapping(Map<String, Object>  request) throws ServiceException {
        Set<MachineWorkCenterMapping> machineWorkCentreMappings = new HashSet<MachineWorkCenterMapping>();
        MachineWorkCenterMapping machineWorkCentreMapping = null;
        String machineId = "";
        if (!StringUtil.isNullObject(request.get(WorkCentre.MACHINEID))) {
            machineId = request.get(WorkCentre.MACHINEID).toString();
            String [] machineIdArr = machineId.split(",");
            String wcid = request.get(WorkCentre.WCID).toString();
            String companyid = request.get(WorkCentre.COMPANYID).toString();
            for (int count = 0; count < machineIdArr.length; count++) {
                machineWorkCentreMapping = new MachineWorkCenterMapping();
                machineWorkCentreMapping.setMachineID((Machine) get(Machine.class, (String) machineIdArr[count]));
                machineWorkCentreMapping.setWorkCenterID((WorkCentre) get(WorkCentre.class, wcid));
                machineWorkCentreMapping.setCompany((Company) get(Company.class, companyid));
                machineWorkCentreMappings.add(machineWorkCentreMapping);
            }
        }

        return machineWorkCentreMappings;
    }
    @Override
    public Set<ProductWorkCentreMapping> getProductWCMapping(Map<String, Object>  request) throws ServiceException {
        Set<ProductWorkCentreMapping> productWorkCentreMappings = new HashSet<ProductWorkCentreMapping>();
        ProductWorkCentreMapping productWorkCentreMapping = null;
        String productId = "";
        if (!StringUtil.isNullObject(request.get(WorkCentre.PRODUCTID))) {
            productId = request.get(WorkCentre.PRODUCTID).toString();
            String [] productIdArr = productId.split(",");
            String wcid = request.get(WorkCentre.WCID).toString();
            String companyid = request.get(WorkCentre.COMPANYID).toString();
            for (int count = 0; count < productIdArr.length; count++) {
                productWorkCentreMapping = new ProductWorkCentreMapping();
                productWorkCentreMapping.setProductid((Product) get(Product.class, (String) productIdArr[count]));
                productWorkCentreMapping.setWorkCenterID((WorkCentre) get(WorkCentre.class, wcid));
                productWorkCentreMapping.setCompanyid((Company) get(Company.class, companyid));
                productWorkCentreMappings.add(productWorkCentreMapping);
            }
        }

        return productWorkCentreMappings;
    }
    @Override
    public Set<MaterialWorkCentreMapping> getMaterialWCMapping(Map<String, Object>  request) throws ServiceException {
        Set<MaterialWorkCentreMapping> materialWorkCentreMappings = new HashSet<MaterialWorkCentreMapping>();
        MaterialWorkCentreMapping materialWorkCentreMapping = null;
        String materialId = "";
        if (!StringUtil.isNullObject(request.get(WorkCentre.MATERIALID))) {
            materialId = request.get(WorkCentre.MATERIALID).toString();
            String [] materialIdArr = materialId.split(",");
            String wcid = request.get(WorkCentre.WCID).toString();
            String companyid = request.get(WorkCentre.COMPANYID).toString();
            for (int count = 0; count < materialIdArr.length; count++) {
                materialWorkCentreMapping = new MaterialWorkCentreMapping();
                materialWorkCentreMapping.setBomid((BOMDetail) get(BOMDetail.class, (String) materialIdArr[count]));
                materialWorkCentreMapping.setWorkCenterID((WorkCentre) get(WorkCentre.class, wcid));
                materialWorkCentreMapping.setCompanyid((Company) get(Company.class, companyid));
                materialWorkCentreMappings.add(materialWorkCentreMapping);
            }
        }

        return materialWorkCentreMappings;
    }
    @Override
    public KwlReturnObject deletelabourWorkCentreMappings(Map<String, Object> requestParams) throws ServiceException {
       List list = Collections.EMPTY_LIST;
        List paramList = new ArrayList();
        try {
            String hql = "";
            String conditionHql = "";
            if (requestParams.containsKey(WorkCentre.WCID) && !StringUtil.isNullObject(requestParams.get(WorkCentre.WCID))) {
                conditionHql += " lwcmap.workCentre.ID = ? ";
                paramList.add((String)requestParams.get(WorkCentre.WCID));
            }
            hql += "delete from LabourWorkCentreMapping lwcmap where " + conditionHql;
           int count= executeUpdate(hql, paramList.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccJobWorkDaoImpl.deleteJobWorkOrders", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject deleteMachineWorkCentreMappings(Map<String, Object> requestParams) throws ServiceException {
       List list = Collections.EMPTY_LIST;
        List paramList = new ArrayList();
        try {
            String hql = "";
            String conditionHql = "";
            if (requestParams.containsKey(WorkCentre.WCID) && !StringUtil.isNullObject(requestParams.get(WorkCentre.WCID))) {
                conditionHql += " mwcmap.workCenterID.ID = ? ";
                paramList.add((String)requestParams.get(WorkCentre.WCID));
            }
            hql += "delete from MachineWorkCenterMapping mwcmap where " + conditionHql;
           int count= executeUpdate(hql, paramList.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccJobWorkDaoImpl.deleteJobWorkOrders", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject deleteProductWorkCentreMappings(Map<String, Object> requestParams) throws ServiceException {
       List list = Collections.EMPTY_LIST;
        List paramList = new ArrayList();
        try {
            String hql = "";
            String conditionHql = "";
            if (requestParams.containsKey(WorkCentre.WCID) && !StringUtil.isNullObject(requestParams.get(WorkCentre.WCID))) {
                conditionHql += " pwcmap.workCenterID.ID = ? ";
                paramList.add((String)requestParams.get(WorkCentre.WCID));
            }
            hql += "delete from ProductWorkCentreMapping pwcmap where " + conditionHql;
           int count= executeUpdate(hql, paramList.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccJobWorkDaoImpl.deleteJobWorkOrders", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject deleteMaterialWorkCentreMappings(Map<String, Object> requestParams) throws ServiceException {
       List list = Collections.EMPTY_LIST;
        List paramList = new ArrayList();
        try {
            String hql = "";
            String conditionHql = "";
            if (requestParams.containsKey(WorkCentre.WCID) && !StringUtil.isNullObject(requestParams.get(WorkCentre.WCID))) {
                conditionHql += " matwcmap.workCenterID.ID = ? ";
                paramList.add((String)requestParams.get(WorkCentre.WCID));
            }
            hql += "delete from MaterialWorkCentreMapping matwcmap where " + conditionHql;
           int count= executeUpdate(hql, paramList.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccJobWorkDaoImpl.deleteJobWorkOrders", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject saveWorkCentreMappings(Map<String, Object> requestParams) throws ServiceException {
        try {
            
        WorkCentre workCentre = null;
            if (requestParams.containsKey(WorkCentre.WCID) && requestParams.get(WorkCentre.WCID) != null) {
                workCentre = (WorkCentre) get(WorkCentre.class, requestParams.get(WorkCentre.WCID).toString());
            }
            if (requestParams.containsKey(WorkCentre.LABOURWCMAP) && requestParams.get(WorkCentre.LABOURWCMAP) != null) {
                 Set<LabourWorkCentreMapping> labourWCMapping = (Set<LabourWorkCentreMapping>) requestParams.get(WorkCentre.LABOURWCMAP);
                workCentre.setLabourworkcentremappings(labourWCMapping);
            }
            if (requestParams.containsKey(WorkCentre.MACHINEWCMAP) && requestParams.get(WorkCentre.MACHINEWCMAP) != null) {
                 Set<MachineWorkCenterMapping> machineWCMapping = (Set<MachineWorkCenterMapping>) requestParams.get(WorkCentre.MACHINEWCMAP);
                workCentre.setMachineworkcentremappings(machineWCMapping);
            }
            if (requestParams.containsKey(WorkCentre.PRODUCTWCMAP) && requestParams.get(WorkCentre.PRODUCTWCMAP) != null) {
                 Set<ProductWorkCentreMapping> productWCMapping = (Set<ProductWorkCentreMapping>) requestParams.get(WorkCentre.PRODUCTWCMAP);
                workCentre.setProductworkcentremappings(productWCMapping);
            }
            if (requestParams.containsKey(WorkCentre.MATERIALWCMAP) && requestParams.get(WorkCentre.MATERIALWCMAP) != null) {
                 Set<MaterialWorkCentreMapping> materialWCMapping = (Set<MaterialWorkCentreMapping>) requestParams.get(WorkCentre.MATERIALWCMAP);
                workCentre.setMaterialworkcentremappings(materialWCMapping);
            }
            saveOrUpdate(workCentre);
        }  catch(Exception ex) {
            throw ServiceException.FAILURE("AccJobWorkDaoImpl.deleteJobWorkOrders", ex);
        }
        return new KwlReturnObject(true, null, "", null, 0);
    }
    
    @Override
    public KwlReturnObject deleteWorkCentre(Map<String, Object> dataMap) throws ServiceException {
        ArrayList params1 = new ArrayList();
        String delQuery1 = "";
        int numRows1 = 0;
        try {
            if (dataMap.containsKey(WorkCentre.COMPANYID)) {
                params1.add(dataMap.get(WorkCentre.COMPANYID));
            }
            if (dataMap.containsKey(WorkCentre.WCID)) {
                params1.add(dataMap.get(WorkCentre.WCID));
            }
            delQuery1 = "update workcenter set deleted='T' where company = ? and id=?";
            numRows1 = executeSQLUpdate(delQuery1, params1.toArray());

        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteMachineMaster : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, "Work Centre(s) has been deleted successfully.", null, null, numRows1);

    }
    @Override
    public KwlReturnObject deleteWorkCentrePermanently(Map<String, Object> dataMap) throws ServiceException {

        ArrayList params1 = new ArrayList();
        String id = "", delQuery1 = "", delQuery2 = "",delQuery3="",delQuery4="",delQuery5="";
        int numRows1 = 0;
        String custCondition="";
        try {

            if (dataMap.containsKey(WorkCentre.COMPANYID)) {
                params1.add(dataMap.get(WorkCentre.COMPANYID));
            }
            if(dataMap.containsKey(WorkCentre.WCID)){
                params1.add(dataMap.get(WorkCentre.WCID));

            }
            delQuery5 = "delete from workcenter where company = ? and id=?";
            numRows1 = numRows1 + executeSQLUpdate(delQuery5, params1.toArray());

        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteWorkCentrePermanently : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, "Work Centre(s) has been deleted successfully.", null, null, numRows1);

    }
    
    @Override
    public KwlReturnObject deleteWorkCentreCustomData(Map<String, Object> requestParams) throws ServiceException {
        List list = Collections.EMPTY_LIST;
        List paramList = new ArrayList();
        try {
            String hql = "";
            if (requestParams.containsKey(WorkCentre.WCID) && !StringUtil.isNullObject(requestParams.get(WorkCentre.WCID))) {
                paramList.add((String) requestParams.get(WorkCentre.WCID));
            }
            hql += "delete from WorkCentreCustomData w where w.workCentre.ID = ?";
            int count = executeUpdate(hql, paramList.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccJobWorkDaoImpl.deleteWorkCentreCustomData", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getWOforWorkCentre(Map<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String condition = "";
            ArrayList params = new ArrayList();
            
            if (dataMap.containsKey("id")) {
                condition = " where wowc.workcentreid.ID=?";
                params.add(dataMap.get("workcentreid"));
            }
            
            String selQuery = "select wowc.id from WorkOrderWorkCenterMapping wowc " + condition;
            list = executeQuery(selQuery, params.toArray());
            
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public KwlReturnObject getRTforWorkCentre(Map<String, Object> dataMap) throws ServiceException {
            List list = new ArrayList();
        try {
            String condition = "";
            ArrayList params = new ArrayList();
            
            if (dataMap.containsKey("id")) {
                condition = " where rt.workcenter=?";
                params.add(dataMap.get("workcentreid"));
            }
            
            String selQuery = "select rt.id from routing_template rt " + condition;
            list = executeSQLQuery(selQuery, params.toArray());
            
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
        //throw new UnsupportedOperationException("Not supported yet.");
    }

}
