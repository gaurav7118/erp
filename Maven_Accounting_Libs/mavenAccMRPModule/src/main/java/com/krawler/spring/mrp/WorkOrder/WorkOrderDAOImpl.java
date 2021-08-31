/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.WorkOrder;

import com.krawler.accounting.utils.KWLErrorMsgs;
import com.krawler.common.admin.*;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.mrp.labormanagement.Labour;
import com.krawler.spring.mrp.machinemanagement.Machine;
import com.krawler.spring.mrp.routingmanagement.RoutingTemplate;
import com.krawler.spring.mrp.workcentremanagement.WorkCentre;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

/**
 *
 * @author krawler
 */
public class WorkOrderDAOImpl extends BaseDAO implements WorkOrderDAO{

    @Override
    public KwlReturnObject saveWorkOrder(JSONObject jobj) throws ServiceException {
        List ll = new ArrayList();
        int dl = 0;
        boolean isEdit = false;
        try {
            DateFormat df = authHandler.getDateOnlyFormat();  
            WorkOrder workOrder = null;
            String id = "";
            if (jobj.has("isEdit")) {
                isEdit = jobj.getBoolean("isEdit");
            }
            if (isEdit) {
                if (jobj.has("id")) {
                    id = jobj.getString("id");
                }
                workOrder = (WorkOrder) get(WorkOrder.class, id);
            } else {
                workOrder = new WorkOrder();
                id = UUID.randomUUID().toString();
//                workOrder.setID(id);
            }
//            if (jobj.has("companyid")) {
//                String companyid = jobj.getString("companyid");
//                workOrder.setCompany((Company) get(Company.class, companyid));
//            }
            if (jobj.has("workordername")) {
                String woName = jobj.getString("workordername");
                workOrder.setWorkOrderName(woName);
            }
            if (jobj.has("workorderid")) {
                String woName = jobj.getString("workorderid");
                workOrder.setWorkOrderID(woName);
            }
            if (jobj.has(WorkOrder.SEQUENCEFORMAT)) {
                String tempStr = jobj.getString(WorkOrder.SEQUENCEFORMAT);
                workOrder.setSeqformat((SequenceFormat) get(SequenceFormat.class, tempStr));
            }
            if (jobj.has("fromLinkCombo") && !StringUtil.isNullOrEmpty(jobj.getString("fromLinkCombo"))) {

                int fromLinkCombo = Integer.parseInt(jobj.getString("fromLinkCombo"));
                if (jobj.has("linkDocNo") && !StringUtil.isNullOrEmpty(jobj.getString("linkDocNo"))) {
                    String linkDocNo = jobj.getString("linkDocNo");
                    if (fromLinkCombo == WorkOrder.SALESORDER) {
                        SalesOrder so = (SalesOrder) get(SalesOrder.class, linkDocNo);
                        if (so != null) {
                            workOrder.setSalesOrder(so);
                             workOrder.setSalesContractID(null);
                        }
                    } else if (fromLinkCombo == WorkOrder.SALESCONTRACT) {
                        Contract co = (Contract) get(Contract.class, linkDocNo);
                        if (co != null) {
                            workOrder.setSalesContractID(co);
                            workOrder.setSalesOrder(null);
                        }
                    }
                     workOrder.setFromlinktype(fromLinkCombo);
                }
            }
            if (jobj.has("dateofdelivery")) {
                Date DOD = df.parse(jobj.get("dateofdelivery").toString()); 
                workOrder.setDateOfDelivery(DOD);
            }
            if (jobj.has("workorderdate") && !StringUtil.isNullOrEmpty(jobj.getString("workorderdate"))) {
                Date WOD = df.parse(jobj.get("workorderdate").toString());
                workOrder.setWorkOrderDate(WOD);
            }
            if (jobj.has("routecode")) {
                String tempStr = jobj.getString("routecode");
                workOrder.setRouteCode(tempStr);
            }
            if (jobj.has("workordertype")) {
                String tempStr = jobj.getString("workordertype");
                workOrder.setWorkOrderType((MasterItem) get(MasterItem.class, tempStr));
            }
            if (jobj.has("orderWarehouse")) {
                String tempStr = jobj.getString("orderWarehouse");
                workOrder.setOrderWarehouse((InventoryWarehouse) get(InventoryWarehouse.class, tempStr));
            }
            if (jobj.has("orderLocation")) {
                String tempStr = jobj.getString("orderLocation");
                workOrder.setOrderLocation((InventoryLocation) get(InventoryLocation.class, tempStr));
            }
            if (jobj.has("workorderstatus")) {
                String tempStr = jobj.getString("workorderstatus");
                workOrder.setWorkOrderStatus((MasterItem) get(MasterItem.class, tempStr));
            }
            if (jobj.has("productid")) {
                String tempStr = jobj.getString("productid");
                workOrder.setProductID((Product) get(Product.class, tempStr));
            }
            if (jobj.has("routetemplateid")) {
                String tempStr = jobj.getString("routetemplateid");
                workOrder.setRouteTemplate((RoutingTemplate) get(RoutingTemplate.class, tempStr));
            }
            if (jobj.has("companyid")) {
                String tempStr = jobj.getString("companyid");
                workOrder.setCompany((Company) get(Company.class, tempStr));
            }
            if (jobj.has("customer")) {
                String tempStr = jobj.getString("customer");
                workOrder.setCustomer((Customer) get(Customer.class, tempStr));
            }
            if (jobj.has("routingtype") && !StringUtil.isNullOrEmpty((String) jobj.getString("routingtype"))) {
                workOrder.setRoutingMasterType(Integer.parseInt((String) jobj.getString("routingtype")));
            }
            if (jobj.has(workOrder.PROJECTID)) {
                workOrder.setProjectId((String)jobj.getString(workOrder.PROJECTID));
            }
            if (jobj.has(workOrder.MATERIALID)) {
                String tempStr = jobj.getString(workOrder.MATERIALID);
                workOrder.setBomid((BOMDetail) get(BOMDetail.class, tempStr));
            }
            if (jobj.has("quantity")) {
                String tempStr = jobj.getString("quantity");
                workOrder.setQuantity(Double.parseDouble(tempStr));
            }
            if (jobj.has(Constants.DATEPREFIX) && !StringUtil.isNullOrEmpty((String) jobj.get(Constants.DATEPREFIX))) {
                workOrder.setDatePreffixValue((String) jobj.get(Constants.DATEPREFIX));
            }
            if (jobj.has(Constants.DATEAFTERPREFIX) && !StringUtil.isNullOrEmpty((String) jobj.get(Constants.DATEAFTERPREFIX))) {
                workOrder.setDateAfterPreffixValue((String) jobj.get(Constants.DATEAFTERPREFIX));
            }
            if (jobj.has(Constants.DATESUFFIX) && !StringUtil.isNullOrEmpty((String) jobj.get(Constants.DATESUFFIX))) {
                workOrder.setDateSuffixValue((String) jobj.get(Constants.DATESUFFIX));
            }
            if (jobj.has(Constants.SEQNUMBER) && !StringUtil.isNullOrEmpty((String) jobj.get(Constants.SEQNUMBER))) {
                workOrder.setSeqnumber(Integer.parseInt((String) jobj.get(Constants.SEQNUMBER)));
            }
            if (jobj.has("autogenerated") && ((Boolean) jobj.get("autogenerated"))) {
                workOrder.setAutoGenerated((Boolean) jobj.get("autogenerated"));
            }
            if (!isEdit) {
                if (jobj.has("createdby")) {
                    String tempStr = jobj.getString("createdby");
                    workOrder.setCreatedBy((User) get(User.class, tempStr));
                }
            }
            if (jobj.has("modifiedby")) {
                String tempStr = jobj.getString("modifiedby");
                workOrder.setModifiedBy((User) get(User.class, tempStr));
            }
            if (jobj.has("accworkordercustomdataref") && !StringUtil.isNullOrEmpty(jobj.get("accworkordercustomdataref").toString())) {
                WorkOrderCustomData workOrderCustomData = (WorkOrderCustomData) get(WorkOrderCustomData.class, jobj.get("accworkordercustomdataref").toString());
                workOrder.setAccWorkOrderCustomData(workOrderCustomData);
            }
             if (jobj.has("closewoje")) {
                String tempStr = jobj.getString("closewoje");
                workOrder.setCloseWOJE((JournalEntry) get(JournalEntry.class, tempStr));
            }
             if (jobj.has("assemblyJedid")) {
                String tempStr = jobj.getString("assemblyJedid");
                workOrder.setTotalinventoryJEdetail((JournalEntryDetail) get(JournalEntryDetail.class, tempStr));
            }
            saveOrUpdate(workOrder);
            ll.add(workOrder);
        } catch (Exception ex) {
            logger.warn(ex.getMessage(), ex);
            throw ServiceException.FAILURE("WorkOrderDAOImpl:SaveWorkOrder " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, "", ll, dl);
    }
    
     @Override
    public KwlReturnObject getWorkOrders(Map<String, Object> requestParams) throws ServiceException {
        List list = Collections.EMPTY_LIST;
        List countList = Collections.EMPTY_LIST;
        ArrayList paramList = new ArrayList();
        int totalWOs=0;
        try {
            
             DateFormat df=authHandler.getDateOnlyFormat();
            String hql = "";
            String CountHql = "";
            String conditionHql = "";
            String moduleId = "";
            String joinCondition = "";
            String customerComboValue="";
            String customerComboValueForQuery="";
             int start = 0;
            int limit = 30;
            boolean pagingFlag = false;
            if (requestParams.containsKey("start") && requestParams.containsKey("limit") && !StringUtil.isNullOrEmpty(requestParams.get("start").toString())) {
                start = Integer.parseInt(requestParams.get("start").toString());
                limit = Integer.parseInt(requestParams.get("limit").toString());
                pagingFlag = true;
            }
            boolean isWorkOrdersForCombo = false;
            if (requestParams.containsKey("isWorkOrdersForCombo") && !StringUtil.isNullOrEmpty(requestParams.get("isWorkOrdersForCombo").toString())) {
                isWorkOrdersForCombo = Boolean.parseBoolean(requestParams.get("isWorkOrdersForCombo").toString());
            }
            if (requestParams.containsKey("companyid") && !StringUtil.isNullObject(requestParams.get("companyid"))) {
                conditionHql += " wo.company.companyID= ? ";
                paramList.add((String) requestParams.get("companyid"));
            }
            if (requestParams.containsKey(WorkOrder.WORKORDERID) && !StringUtil.isNullOrEmpty((String) requestParams.get(WorkOrder.WORKORDERID))) {
                conditionHql += " and wo.workOrderID= ? ";
                paramList.add((String) requestParams.get(WorkOrder.WORKORDERID));
            }
            
            if (!StringUtil.isNullOrEmpty((String) requestParams.get("customerComboValue"))) {

                customerComboValue = (String) requestParams.get("customerComboValue");
                if(customerComboValue.contains(","))
                {
                    String[] customerComboValueArray=customerComboValue.split(",");
                    for(String customerComboValueIndividual:customerComboValueArray){
                    customerComboValueForQuery+=customerComboValueForQuery==""?"'"+customerComboValueIndividual+"'":","+"'"+customerComboValueIndividual+"'";
                    }
                }else{
                    customerComboValueForQuery+="'"+customerComboValue+"'";
                }
                conditionHql += " and wo.customer.id IN (";
                conditionHql +=customerComboValueForQuery;
                conditionHql+=")";
            }
            if (requestParams.containsKey("woid") && !StringUtil.isNullOrEmpty((String) requestParams.get("woid"))) {
                conditionHql += " and wo.ID != ? ";
                paramList.add((String) requestParams.get("woid"));
            }
            if (requestParams.containsKey("wostatus") && !StringUtil.isNullOrEmpty(requestParams.get("wostatus").toString())) {
                conditionHql += " and wo.workOrderStatus.ID= ? ";
                paramList.add(requestParams.get("wostatus").toString());
            }
            if (requestParams.containsKey("restrictwoclosedstatus") && !StringUtil.isNullOrEmpty(requestParams.get("restrictwoclosedstatus").toString())) {
                conditionHql += " and wo.workOrderStatus.ID != ? ";
                paramList.add(requestParams.get("restrictwoclosedstatus").toString());
            }
            if (requestParams.containsKey("moduleid")) {
                moduleId = (String) requestParams.get("moduleid");
            }
            String ss = (requestParams.containsKey("ss") && requestParams.get("ss") != null) ? (String) requestParams.get("ss") : "";
            if(isWorkOrdersForCombo && (requestParams.containsKey("query") && requestParams.get("query") != null)){
                ss = (String) requestParams.get("query");
            }
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"wo.workOrderName", "wo.workOrderID","wo.productID.name","wo.productID.productid"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(paramList, ss, 4);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                conditionHql += searchQuery;
            }
            if (requestParams.containsKey(Constants.REQ_startdate) && !StringUtil.isNullObject(requestParams.get(Constants.REQ_startdate))) {
                Date startDate = df.parse((String) requestParams.get(Constants.REQ_startdate));
                conditionHql += " and wo.workOrderDate >= ? ";
                paramList.add(startDate);
            }
            if (requestParams.containsKey(Constants.REQ_enddate) && !StringUtil.isNullObject(requestParams.get(Constants.REQ_enddate))) {
                Date endDate = df.parse((String) requestParams.get(Constants.REQ_enddate));
                conditionHql += " and wo.workOrderDate <= ? ";
                paramList.add(endDate);
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
            String searchDefaultFieldHQL = "";
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
                        joinCondition += " LEFT JOIN wo.labourmapping lwm ";
                        joinCondition += " LEFT JOIN wo.machinemapping mwm ";
                        joinCondition += " LEFT JOIN wo.workcentermapping wwm ";
                        joinCondition += " LEFT JOIN wo.componentDetails cwm ";
                        searchDefaultFieldHQL = (String) (map.containsKey("condition") ? map.get("condition") : "");
                        searchDefaultFieldHQL = searchDefaultFieldHQL.replaceAll("workorderRef.labourmapping", "lwm");
                        searchDefaultFieldHQL = searchDefaultFieldHQL.replaceAll("workorderRef.machinemapping", "mwm");
                        searchDefaultFieldHQL = searchDefaultFieldHQL.replaceAll("workorderRef.workcentermapping", "wwm");
                        searchDefaultFieldHQL = searchDefaultFieldHQL.replaceAll("workorderRef.componentDetails", "cwm");
                        searchDefaultFieldHQL = searchDefaultFieldHQL.replaceAll("workorderRef", "wo");

                    }
                    if (customSearchFieldArray.length() > 0) {
                        /*
                         Advance Search For Custom fields
                         */
                        requestParams.put(Constants.Searchjson, Searchjson);
                        requestParams.put(Constants.appendCase, appendCase);
                        requestParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(requestParams, false).get(Constants.myResult));
                        if (mySearchFilterString.contains("c.WorkOrderCustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("c.WorkOrderCustomData", "wo.accWorkOrderCustomData");
                        }
                        StringUtil.insertParamAdvanceSearchString1(paramList, Searchjson);
                    }
                    mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldHQL, mySearchFilterString, filterConjuctionCriteria);
                }
            }
            
           
            
            hql += " select distinct wo from WorkOrder wo "+joinCondition +"where " + conditionHql + mySearchFilterString;
            CountHql=" select count(distinct wo) from WorkOrder wo "+joinCondition +"where " + conditionHql + mySearchFilterString;
            countList = executeQuery(CountHql, paramList.toArray());
           
            if (pagingFlag) {
                list = executeQueryPaging(hql, paramList.toArray(), new Integer[]{start, limit});
            } else {
                list = executeQuery(hql, paramList.toArray());
            }
            
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccJobWorkDaoImpl.getJobWorkOrders", ex);
        }
        return new KwlReturnObject(true, "", null, list, Integer.parseInt(countList.get(0).toString()));
    }
    public KwlReturnObject getWorkOrderMachineMapping(Map<String, Object> requestParams) throws ServiceException {
        List list = Collections.EMPTY_LIST;
        List paramList = new ArrayList();
        try {
            String hql = "";
            String conditionHql = "";
            
            if (requestParams.containsKey("machineid")) {
                conditionHql += " workOrderMachineMapping.machineid.ID= ? ";
                paramList.add((String)requestParams.get("machineid"));
            }
            hql += " SELECT workOrderMachineMapping.workorderid from WorkOrderMachineMapping workOrderMachineMapping where " + conditionHql;
           list= executeQuery(hql, paramList.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getWorkOrderMachineMapping", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
     
     @Override
    public KwlReturnObject deleteWorkOrders(Map<String, Object> requestParams) throws ServiceException {
       List list = Collections.EMPTY_LIST;
        List paramList = new ArrayList();
        try {
            String hql = "";
            String conditionHql = "";
            if (requestParams.containsKey(WorkOrder.COMPANYID) && !StringUtil.isNullObject(requestParams.get(WorkOrder.COMPANYID))) {
                conditionHql += " wo.company.companyID= ? ";
                paramList.add((String)requestParams.get(WorkOrder.COMPANYID));
            }
            if (requestParams.containsKey(WorkOrder.WOID) && !StringUtil.isNullObject(requestParams.get(WorkOrder.WOID))) {
                conditionHql += " and wo.id= ? ";
                paramList.add((String)requestParams.get(WorkOrder.WOID));
            }
            hql += "delete from WorkOrder wo where " + conditionHql;
           int count= executeUpdate(hql, paramList.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccJobWorkDaoImpl.deleteJobWorkOrders", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
     
     @Override
     public KwlReturnObject getSOSCCombo(Map<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String companyId = "";
        String customer = "";
        String linkfrom = "";
        List list = null;
        int count = 0;
        try {
            String query = "";
            if (requestParams.containsKey(WorkOrder.COMPANYID)) {
                companyId = requestParams.get(WorkOrder.COMPANYID).toString();
                params.add(companyId);
            }
            if (requestParams.containsKey(WorkOrder.CUSTOMERID)) {
                customer = requestParams.get(WorkOrder.CUSTOMERID).toString();
                params.add(customer);
            }
            if (requestParams.containsKey("linkfrom")) {
                linkfrom = requestParams.get("linkfrom").toString();
            }
            /*sales order is approved then only fetch value in combo while creating work order(Checked : approvestatuslevel='11')*/
            if (linkfrom.equals("1")) {
                query = "select id,sonumber from salesorder where approvestatuslevel='11' and company=? and customer = ? and (isopen='T' and issoclosed='F')";
            } else {
                query = "select id,contractnumber from contract where company=? and customer = ?";
            }
            list = executeSQLQuery(query, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("NOt FOUND: " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getWorkorderFromSOandProduct(Map<String, Object> requestParams) throws ServiceException {

        ArrayList params = new ArrayList();
        String companyId = "";
        String customer = "";
        String linkfrom = "";
        String salesorderid = "";
        String productid = "";
        List list = null;
        int count = 0;
        try {
            String query = "";
            if (requestParams.containsKey(WorkOrder.COMPANYID)) {
                companyId = requestParams.get(WorkOrder.COMPANYID).toString();
                params.add(companyId);
            }
//            if (requestParams.containsKey(WorkOrder.CUSTOMERID)) {
//                customer = requestParams.get(WorkOrder.CUSTOMERID).toString();
//                params.add(customer);
//            }
            if (requestParams.containsKey("linkfrom")) {
                linkfrom = requestParams.get("linkfrom").toString();
            }
            if (requestParams.containsKey("id")) {
                salesorderid = requestParams.get("id").toString();
                params.add(salesorderid);
            }
            if (requestParams.containsKey("productid")) {
                productid = requestParams.get("productid").toString();
                params.add(productid);
            }
            if (linkfrom.equals("1")) {
                query = "select id,quantity from workorder where company=? and salesorder = ? and productid = ?";
            }
            if (!StringUtil.isNullOrEmpty(query)) {
                list = executeSQLQuery(query, params.toArray());
                count = list.size();
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("NOt FOUND: " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject saveWorkOrderLabourMapping(Map<String, Object> LabourMappingDataMap) throws ServiceException {

        List ll = new ArrayList();
        Set<WorkOrderLabourMapping> mappingSet = new HashSet();
        WorkOrderLabourMapping workorderlabourmapping = null;
        try {
            WorkOrder workordeObj = (WorkOrder) LabourMappingDataMap.get("workorderObj");
            String workorderid = workordeObj.getID();
            String[] loburIDs = LabourMappingDataMap.get(WorkOrder.LABOURID).toString().split(",");
            for (int i = 0; i < loburIDs.length; i++) {
                if (!StringUtil.isNullOrEmpty(loburIDs[i])) {
                    workorderlabourmapping = new WorkOrderLabourMapping();

                    Labour labour = (Labour) get(Labour.class, loburIDs[i]);
                    if (labour != null) {
                        workorderlabourmapping.setLabourid(labour);
                    }
                    WorkOrder workorder = (WorkOrder) get(WorkOrder.class, workorderid);
                    if (workorder != null) {
                        workorderlabourmapping.setWorkorderid(workorder);
                    }
                    mappingSet.add(workorderlabourmapping);
                }
            }
            workordeObj.setLabourmapping(mappingSet);
            save(workordeObj);
            ll.add(mappingSet);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("WorkOrderDAOImpl.saveWorkOrderLabourMapping: " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, ll, ll.size());
    }
    
    @Override
    public KwlReturnObject saveWorkOrderMachineMapping(Map<String, Object> machineMappingDataMap) throws ServiceException {

        List ll = new ArrayList();
        Set<WorkOrderMachineMapping> mappingSet = new HashSet();
        WorkOrderMachineMapping workorderMachineMapping = null;
        try {
            WorkOrder workordeObj = (WorkOrder) machineMappingDataMap.get("workorderObj");
            String workorderid = workordeObj.getID();
            String[] machineIDs = machineMappingDataMap.get(WorkOrder.MACHINEID).toString().split(",");
            for (int i = 0; i < machineIDs.length; i++) {
                if (!StringUtil.isNullOrEmpty(machineIDs[i])) {
                    workorderMachineMapping = new WorkOrderMachineMapping();

                    Machine machine = (Machine) get(Machine.class, machineIDs[i]);
                    if (machine != null) {
                        workorderMachineMapping.setMachineid(machine);
                    }
                    WorkOrder workorder = (WorkOrder) get(WorkOrder.class, workorderid);
                    if (workorder != null) {
                        workorderMachineMapping.setWorkorderid(workorder);
                    }
                    mappingSet.add(workorderMachineMapping);
                }
            }
            workordeObj.setMachinemapping(mappingSet);
            save(workordeObj);
            ll.add(mappingSet);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("WorkOrderDAOImpl.saveWorkOrderLabourMapping: " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, ll, ll.size());
    }
    
    @Override
    public KwlReturnObject saveWorkOrderWorkCenterMapping(Map<String, Object> workcenterMappingDataMap) throws ServiceException {
        List ll = new ArrayList();
        Set<WorkOrderWorkCenterMapping> mappingSet = new HashSet();
        WorkOrderWorkCenterMapping workorderWorkcetnermapping = null;
        try {
            WorkOrder workordeObj = (WorkOrder) workcenterMappingDataMap.get("workorderObj");
            String workorderid = workordeObj.getID();
            String[] workcenterids = workcenterMappingDataMap.get(WorkOrder.WORKCENTREID).toString().split(",");
            for (int i = 0; i < workcenterids.length; i++) {
                if (!StringUtil.isNullOrEmpty(workcenterids[i])) {
                    workorderWorkcetnermapping = new WorkOrderWorkCenterMapping();

                    WorkCentre workcenter = (WorkCentre) get(WorkCentre.class, workcenterids[i]);
                    if (workcenter != null) {
                        workorderWorkcetnermapping.setWorkcentreid(workcenter);
                    }
                    WorkOrder workorder = (WorkOrder) get(WorkOrder.class, workorderid);
                    if (workorder != null) {
                        workorderWorkcetnermapping.setWorkorderid(workorder);
                    }
                    mappingSet.add(workorderWorkcetnermapping);
                }
            }
            workordeObj.setWorkcentermapping(mappingSet);
            save(workordeObj);
            ll.add(mappingSet);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("WorkOrderDAOImpl.saveWorkOrderLabourMapping: " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, ll, ll.size());
    }
    
    @Override
    public KwlReturnObject deleteWorkOrderMappings(Map<String, Object> deleteParams) throws ServiceException {
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
            throw ServiceException.FAILURE("WorkOrderDAOImpl.deleteWorkOrderLabourMapping: " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, ll, count);
    }
    @Override
    public KwlReturnObject deleteWorkOrder(Map<String, Object> dataMap) throws ServiceException {
        ArrayList params1 = new ArrayList();
        String delQuery1 = "";
        int numRows1 = 0;
        try {
            if (dataMap.containsKey(WorkOrder.COMPANYID)) {
                params1.add(dataMap.get(WorkOrder.COMPANYID));
            }
            if (dataMap.containsKey(WorkOrder.WOID)) {
                params1.add(dataMap.get(WorkOrder.WOID));
            }
            delQuery1 = "update workorder set deleted='T' where company = ? and id=?";
            numRows1 = executeSQLUpdate(delQuery1, params1.toArray());

        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteWork Orders: " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, "Work Order(s) has been deleted successfully.", null, null, numRows1);

    }
    @Override
    public KwlReturnObject deleteWorkOrderPermanently(Map<String, Object> dataMap) throws ServiceException {

        ArrayList params1 = new ArrayList();
        String id = "", delQuery1 = "", delQuery2 = "",delQuery3="",delQuery4="",delQuery5="";
        int numRows1 = 0;
        try {

            if (dataMap.containsKey(WorkOrder.COMPANYID)) {
                params1.add(dataMap.get(WorkOrder.COMPANYID));
            }
            if(dataMap.containsKey(WorkOrder.WOID)){
                params1.add(dataMap.get(WorkOrder.WOID));

            }
            delQuery5 = "delete from workorder where company = ? and id=?";
            numRows1 = numRows1 + executeSQLUpdate(delQuery5, params1.toArray());

        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteWorkOrderPermanently : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, "Work Order(s) has been deleted successfully.", null, null, numRows1);

    }
    
    @Override
    public KwlReturnObject getProductsForCombo(HashMap<String, Object> dataMap, boolean isSalesOrderLinked) throws ServiceException {
        List list = null;
        List paramList = new ArrayList();
        try {
            String query="";
            String str = "";
            String id = "";
            if (isSalesOrderLinked) {
                query = "select sod.product ,sod.quantity from SalesOrderDetail sod ";
                id = dataMap.get(WorkOrder.SALESORDERID).toString();
                str = " where sod.salesOrder.ID = ? and sod.isLineItemClosed='F' and sod.balanceqty > 0 ";
                paramList.add(id);
            } else {
                query = "select sod.product,sod.quantity from ContractDetail sod ";
                id = dataMap.get(WorkOrder.SALESCONTRACTID).toString();
                str = " where sod.contract.ID = ? ";
                paramList.add(id);
            }
            query += str;
            list = executeQuery(query, paramList.toArray());
            
            
            
        } catch(Exception ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null,list, list.size());
    }
    @Override
    public KwlReturnObject getMachineForWorkOrder(HashMap<String, Object> dataMap) throws ServiceException {
        List list = null;
        List paramList = new ArrayList();
        try {
            String query = "";
            if (dataMap.containsKey(WorkOrder.COMPANYID) && (dataMap.get(WorkOrder.COMPANYID) != null)) {
            }
            if (dataMap.containsKey(WorkOrder.WOID) && (dataMap.get(WorkOrder.WOID) != null)) {
                paramList.add(dataMap.get(WorkOrder.WOID));
            }
             String condition="";
             if(dataMap.containsKey("isExpanderDetails") && dataMap.get("isExpanderDetails").toString() != null && Boolean.parseBoolean(dataMap.get("isExpanderDetails").toString())){
                 condition=" order by womm.machineid.machineName DESC";
             }
            query = " select womm.machineid.ID, womm.machineid.machineName from WorkOrderMachineMapping womm where womm.workorderid.ID = ? "+ condition;
            list = executeQuery(query, paramList.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null,list, list.size());
    }
    @Override
    public KwlReturnObject getLabourForWorkOrder(HashMap<String, Object> dataMap) throws ServiceException {
        List list = null;
        List paramList = new ArrayList();
        try {
            String query = "";
            String conditon="";
            if (dataMap.containsKey(WorkOrder.COMPANYID) && (dataMap.get(WorkOrder.COMPANYID) != null)) {
            }
            if (dataMap.containsKey(WorkOrder.WOID) && (dataMap.get(WorkOrder.WOID) != null)) {
                paramList.add(dataMap.get(WorkOrder.WOID));
            }
            if(dataMap.containsKey("isExpanderDetails") && dataMap.get("isExpanderDetails").toString() != null && Boolean.parseBoolean(dataMap.get("isExpanderDetails").toString())){
                conditon=" order by womm.labourid.fname ASC";
            }
            query = " select womm.labourid.ID, womm.labourid.fname, womm.labourid.lname from WorkOrderLabourMapping womm where womm.workorderid.ID = ? "+conditon;
            list = executeQuery(query, paramList.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null,list, list.size());
    }
    @Override
    public KwlReturnObject getWorkCentreForWorkOrder(HashMap<String, Object> dataMap) throws ServiceException {
        List list = null;
        List paramList = new ArrayList();
        try {
            String query = "";
            if (dataMap.containsKey(WorkOrder.COMPANYID) && (dataMap.get(WorkOrder.COMPANYID) != null)) {
            }
            if (dataMap.containsKey(WorkOrder.WOID) && (dataMap.get(WorkOrder.WOID) != null)) {
                paramList.add(dataMap.get(WorkOrder.WOID));
            }
            query = " select womm.workcentreid.ID, womm.workcentreid.name from WorkOrderWorkCenterMapping womm where womm.workorderid.ID = ? ";
            list = executeQuery(query, paramList.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null,list, list.size());
    }
    /**
     * Function to save WorkOrderComponentDetails
     * @param requestParams = Parameters
     * @return = List of WorkOrderComponentDetails
     * @throws ServiceException
     */
    public KwlReturnObject saveWorkOrderComponentDetails(Map<String, Object> requestMap) throws ServiceException {
        List ll = new ArrayList();
        Set<WorkOrderComponentDetails> workOrderComponentDetailSet = new HashSet();
        try {
            if (requestMap.containsKey("details") && requestMap.get("details") != null) {
                
                WorkOrder workOrder = (WorkOrder) requestMap.get("workorderObj");
                if (workOrder != null) {
                    String detailString = (String) requestMap.get("details").toString();
                    JSONArray details = new JSONArray( detailString );
                    if (details != null && details.length() > 0) {
                        for (int i = 0; i < details.length(); i++) {
                            JSONObject jsonObject = details.getJSONObject(i);
                            if (!StringUtil.isNullOrEmpty(jsonObject.optString("productid", ""))) {
                                WorkOrderComponentDetails componentDetails = null;
                                if (!StringUtil.isNullOrEmpty(jsonObject.optString("wodetailid",""))) {
                                    componentDetails = (WorkOrderComponentDetails) get(WorkOrderComponentDetails.class, jsonObject.getString("wodetailid"));
                                } else {
                                    componentDetails = new WorkOrderComponentDetails();
                                }
                                componentDetails.setWorkOrder(workOrder);
                                if (!StringUtil.isNullOrEmpty(jsonObject.optString("productid", ""))) {
                                    componentDetails.setProduct((Product) get(Product.class, jsonObject.getString("productid")));
                                }
                                if (!StringUtil.isNullOrEmpty(jsonObject.optString("parentproductid", ""))) {
                                    componentDetails.setParentProduct((Product) get(Product.class, jsonObject.getString("parentproductid")));
                                }   
                                if (!StringUtil.isNullOrEmpty(jsonObject.optString("initialpurchaseprice", ""))) {
                                    double initialpurchaseprice = Double.parseDouble(jsonObject.getString("initialpurchaseprice"));
                                    componentDetails.setInitialPurchasePrice(initialpurchaseprice);
                                }
                                if (!StringUtil.isNullOrEmpty(jsonObject.optString("requiredquantity", ""))) {
                                    double requiredquantity = authHandler.roundQuantity((Double.parseDouble(jsonObject.getString("requiredquantity"))), workOrder.getCompany().getCompanyID());
                                    componentDetails.setRequiredQuantity(requiredquantity);
                                }
                                if (!StringUtil.isNullOrEmpty(jsonObject.optString("blockquantity", ""))) {
                                    double blockquantity = authHandler.roundQuantity((Double.parseDouble(jsonObject.getString("blockquantity"))), workOrder.getCompany().getCompanyID());
                                    componentDetails.setBlockQuantity(blockquantity);
                                    if (blockquantity > 0) {
                                        componentDetails.setBlockedFromCA(true);
                                    }
                                }
                                if (!StringUtil.isNullOrEmpty(jsonObject.optString("availablequantity", ""))) {
                                    double availablequantity = authHandler.roundQuantity((Double.parseDouble(jsonObject.getString("availablequantity"))), workOrder.getCompany().getCompanyID());
                                    componentDetails.setAvailableQuantity(availablequantity);
                                }
                                if (!StringUtil.isNullOrEmpty(jsonObject.optString("minpercentquantity", ""))) {
                                    double availablequantity = Double.parseDouble(jsonObject.getString("minpercentquantity"));
                                    componentDetails.setMinpercent(availablequantity);
                                }
                                if (!StringUtil.isNullOrEmpty(jsonObject.optString("batchdetails", ""))) {
                                    componentDetails.setBlockDetails(jsonObject.getString("batchdetails"));
                                }
                                save(componentDetails);
                                workOrderComponentDetailSet.add(componentDetails);
                            }
                        }
                        /**
                         * for selecting Final finished product at task level in Project plan window (PM) need to add Final finished product entry in WorkOrderComponentDetails table.
                         */
                        if (requestMap.containsKey("isEdit") && requestMap.get("isEdit") != null && Boolean.parseBoolean(requestMap.get("isEdit").toString()) == false) {


                            if (!StringUtil.isNullOrEmpty(workOrder.getProductID().getID())) {
                                WorkOrderComponentDetails componentDetails = null;
                                componentDetails = new WorkOrderComponentDetails();
                                componentDetails.setWorkOrder(workOrder);
                                componentDetails.setProduct((Product) get(Product.class, workOrder.getProductID().getID()));
                                componentDetails.setParentProduct(null);
                                componentDetails.setInitialPurchasePrice(0);
                                componentDetails.setRequiredQuantity(workOrder.getQuantity());
                                componentDetails.setBlockQuantity(0);
                                componentDetails.setAvailableQuantity(workOrder.getProductID().getAvailableQuantity());
                                componentDetails.setMinpercent(100);
                                save(componentDetails);
                                workOrderComponentDetailSet.add(componentDetails);
                            }
                        }
                    }
                }
            }
            ll.add(workOrderComponentDetailSet);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("WorkOrderDAOImpl.saveWorkOrderComponentDetails: " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, ll, ll.size());
    }
    
    @Override
    public KwlReturnObject deleteWorkOrderBatchSerialDetails(Map<String, Object> requestParams) throws ServiceException {
        String  delQuery1 = "", delQuerypb = "";
        int numtotal = 0;
        String batchserialids = "", batchids = "";
        String serialmapids = "", docids = "";
        String batchmapids = "", consignbatchmapid = "", consignserialmapid = "";
        boolean isConsignment = false;
        boolean isEditConsumption = false;
        boolean isEdit =false;
        boolean isDeletePermanent = false;
        boolean isWOStart =false;
        String workorderid= null;
        if(requestParams.containsKey("workorderid")){
                workorderid=(String) requestParams.get("workorderid");
        }
        if (requestParams.containsKey("isDeletePermanent")) {
            isDeletePermanent = (Boolean) requestParams.get("isDeletePermanent");
        }
        if(requestParams.containsKey("workorderdetailid")){
                docids=(String) requestParams.get("workorderdetailid");
        }
        if(requestParams.containsKey("isEditConsumption")){
                isEditConsumption=Boolean.parseBoolean(requestParams.get("isEditConsumption").toString());
        }
        if(requestParams.containsKey("isEdit")){
                isEdit=Boolean.parseBoolean(requestParams.get("isEdit").toString());
        }
        if(requestParams.containsKey("isWOStart")){
                isWOStart=Boolean.parseBoolean(requestParams.get("isWOStart").toString());
        }
        if (!StringUtil.isNullOrEmpty(workorderid)) {
            ArrayList params13 = new ArrayList();
            params13.add(requestParams.get("companyid"));
            params13.add(workorderid);
            if (requestParams.containsKey("isConsignment") && requestParams.get("isConsignment") != null) {
                isConsignment = Boolean.parseBoolean(requestParams.get("isConsignment").toString());
            }
//        String myquery3 = "select id from sodetails where salesorder in (select id from salesorder where company = ? and id=?)";
           
            /*
             * If Work Order is Started and this call for Edit case of work order then fetch workordercomponentdetailid for only inventory part product whoes block quantity is 0. 
             * No need to Fetch workordercomponentdetailid for Assembly product and Inventory part product whoes block not 0.(This is implemented to handle Data corruption.it was previouly delete all entry from lbdm)
             */
            String myquery3 = "";
            if (isEdit && isWOStart) {
                myquery3 = " select wocd.id from workordercomponentdetail wocd inner join workorder wo on wo.id=wocd.workorder inner join product p on p.id=wocd.product where wo.company =? and wo.id=? and wocd.blockquantity=0 and p.producttype='" + Constants.INVENTORY_PART + "'";
            } else {
                myquery3 = " select wocd.id from workordercomponentdetail wocd inner join workorder wo on wo.id=wocd.workorder where wo.company =? and wo.id=?";
            }
 
            
            List listBatch = executeSQLQuery(myquery3, params13.toArray());
            Iterator itrBatch = listBatch.iterator();
            while (itrBatch.hasNext()) {
                String batchstring = itrBatch.next().toString();
                docids += "'" + batchstring + "',";
            }
            if (!StringUtil.isNullOrEmpty(docids)) {
                docids = docids.substring(0, docids.length() - 1);
            }
        }else if (!StringUtil.isNullOrEmpty(docids)) {
            docids= "'" + docids +"'" ; // for specific to component detail
        }
        
        if (!StringUtil.isNullOrEmpty(docids)) {
            ArrayList params14 = new ArrayList();
            /**
             * skip block quantity LBDM entry.
             */
            String myquery4 = " select batchmapid,id from locationbatchdocumentmapping where documentid in (" + docids + ") and isconsignment='F' ";
            String myquery5 = " select serialid,id from serialdocumentmapping where documentid in (" + docids + ") and isconsignment='F' ";
            

            List list4 = executeSQLQuery(myquery4, params14.toArray());
            Iterator itr4 = list4.iterator();
            while (itr4.hasNext()) { 
                Object[] objArr = (Object[]) itr4.next();
                LocationBatchDocumentMapping locationBatchDocumentMapping = (LocationBatchDocumentMapping) get(LocationBatchDocumentMapping.class, (String) objArr[1]);
                if (locationBatchDocumentMapping != null) {
                    HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
//                    batchUpdateQtyMap.put("qty", locationBatchDocumentMapping.getQuantity());
                    batchUpdateQtyMap.put("id", locationBatchDocumentMapping.getBatchmapid().getId());

//                    batchUpdateQtyMap.put("isForconsignment", false);
//                    batchUpdateQtyMap.put("consignquantity", 0.0);
                    if (isDeletePermanent) {
                        if (locationBatchDocumentMapping.getTransactiontype() == Constants.MRP_WorkOrderBlockQuantityTransactionType) {
                            /**
                             * executed when product for workorder is not consumed.
                             */
                            batchUpdateQtyMap.put("lockquantity", -locationBatchDocumentMapping.getQuantity());
                            batchUpdateQtyMap.put("quantity", 0.0);
                        } else {
                            /**
                             * executed when product for workorder is consumed.
                             */
                            double quantity = locationBatchDocumentMapping.getTransactiontype() == Constants.Acc_Goods_Receipt_ModuleId ? (-locationBatchDocumentMapping.getQuantity()) : locationBatchDocumentMapping.getQuantity();
                            batchUpdateQtyMap.put("quantity", quantity);
                            batchUpdateQtyMap.put("lockquantity",  0.0);
                        }
                    } else if (isEditConsumption) {
                        /**
                         * Not to Operate on Lock quantity as Lock quantity
                         * removed while consuming product.
                         */
                        batchUpdateQtyMap.put("lockquantity",  0.0);
                        /**
                         * revert DO and GRN transaction type entries for existing batch.
                         */
                        double quantity = locationBatchDocumentMapping.getTransactiontype() == Constants.Acc_Goods_Receipt_ModuleId ? (-locationBatchDocumentMapping.getQuantity()) : locationBatchDocumentMapping.getQuantity();
                        batchUpdateQtyMap.put("quantity", quantity);
                    } else {
                        if (!(isEdit && isWOStart)) {
                            double quantity = 0.0;
                            batchUpdateQtyMap.put("lockquantity", -locationBatchDocumentMapping.getQuantity());
                            batchUpdateQtyMap.put("quantity", quantity);//need to pass 0 (In Edit case it was reducing quantiy from quantitydue)
                        }
                    }
                    saveBatchAmountDue(batchUpdateQtyMap);
                }
                batchmapids += "'" + objArr[0] + "',";

            }
            if (!StringUtil.isNullOrEmpty(batchmapids)) {
                batchmapids = batchmapids.substring(0, batchmapids.length() - 1);
            }
            list4 = executeSQLQuery(myquery5, params14.toArray());
            itr4 = list4.iterator();
            while (itr4.hasNext()) {
                Object[] objArr = (Object[]) itr4.next();
                SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) get(SerialDocumentMapping.class, (String) objArr[1]);
                if (serialDocumentMapping != null) {
                    HashMap<String, Object> serialUpdateQtyMap = new HashMap<String, Object>();
//                    serialUpdateQtyMap.put("qty", "1");
                    serialUpdateQtyMap.put("id", serialDocumentMapping.getSerialid().getId());
//                    serialUpdateQtyMap.put("isForconsignment", false);
//                    serialUpdateQtyMap.put("consignquantity", 0.0);
                    serialUpdateQtyMap.put("lockquantity", "-1");
                    saveSerialAmountDue(serialUpdateQtyMap);
                }
                serialmapids += "'" + objArr[0] + "',";
            }
            String serialDocumentMappingId = getSerialDocumentIds(list4);
            if (!StringUtil.isNullOrEmpty(serialDocumentMappingId)) {
                serialDocumentMappingId = serialDocumentMappingId.substring(0, serialDocumentMappingId.length() - 1);
                ArrayList params1 = new ArrayList();
                delQuery1 = "delete  from serialcustomdata where serialdocumentmappingid in (" + serialDocumentMappingId + ")";
                int numRows1 = executeSQLUpdate(delQuery1, params1.toArray());
            }
            if (!StringUtil.isNullOrEmpty(serialmapids)) {
                serialmapids = serialmapids.substring(0, serialmapids.length() - 1);
            }
            
            ArrayList params15 = new ArrayList();
            delQuerypb = "delete  from locationbatchdocumentmapping where documentid in (" + docids + ") and isconsignment='F' ";
            int numRows = executeSQLUpdate(delQuerypb, params15.toArray());

            delQuerypb = "delete  from serialdocumentmapping where documentid in (" + docids + ") and isconsignment='F' ";
            numRows = executeSQLUpdate(delQuerypb, params15.toArray());
        }
        
        return new KwlReturnObject(true, "Lock quantity has been relased successfully.", null, null, numtotal);
    }
    public String getSerialDocumentIds(List list) {
        String serialDocument = "";
        String serialDocumentMappingId = "";
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object[] objArr = (Object[]) itr.next();
            for (int i = 0; i < objArr.length; i++) {
                SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) get(SerialDocumentMapping.class, (String) objArr[i]);
                if (serialDocumentMapping != null) {
                    serialDocument = serialDocumentMapping.getId().toString();
                    serialDocumentMappingId += "'" + serialDocument + "',";

                }
            }

        }
        return serialDocumentMappingId;
    }
    
    public void saveSerialAmountDue(HashMap<String, Object> productSerialMap) throws ServiceException {
        try {
            NewBatchSerial newBatchSerial = new NewBatchSerial();
            String itemID = (String) productSerialMap.get("id");
            if (productSerialMap.containsKey("id") && !StringUtil.isNullOrEmpty(itemID)) {
                newBatchSerial = (NewBatchSerial) get(NewBatchSerial.class, itemID);
                Double lockqty = Double.parseDouble((String) productSerialMap.get("lockquantity"));
//                Double itemQty = Double.parseDouble((String)productSerialMap.get("qty"));
//                newBatchSerial.setQuantitydue(newBatchSerial.getQuantitydue() + itemQty);
//                Double consignquantity = (Double) productSerialMap.get("consignquantity");
//                newBatchSerial.setConsignquantity(consignquantity);
//                newBatchSerial.setIsForconsignment(Boolean.parseBoolean(productSerialMap.get("isForconsignmentitemID").toString()));
                newBatchSerial.setLockquantity(newBatchSerial.getLockquantity() + lockqty);
            }
            saveOrUpdate(newBatchSerial);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.saveSerialAmountDue : " + ex.getMessage(), ex);
        }
    }
    
    public void saveBatchAmountDue(HashMap<String, Object> productbatchMap) throws ServiceException {
        try {
            NewProductBatch productBatch = new NewProductBatch();
            String itemID = (String) productbatchMap.get("id");
            if (productbatchMap.containsKey("id") && !StringUtil.isNullOrEmpty(itemID)) {
                productBatch = (NewProductBatch) get(NewProductBatch.class, itemID);
                Double itemQty = (Double) productbatchMap.get("quantity");
                Double lockqty = (Double) productbatchMap.get("lockquantity");
                if(productBatch.getLockquantity() + lockqty >= 0){
                    productBatch.setLockquantity(productBatch.getLockquantity() + lockqty);
                }
                productBatch.setQuantitydue(authHandler.roundQuantity((productBatch.getQuantitydue() + itemQty),productBatch.getCompany().getCompanyID()));
//                Double consignquantity= (Double )productbatchMap.get("consignquantity");
//                productBatch.setConsignquantity(consignquantity);
//                productBatch.setIsForconsignment(Boolean.parseBoolean(productbatchMap.get("isForconsignment").toString()));

            }
            saveOrUpdate(productBatch);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.saveBatchAmountDue : " + ex.getMessage(), ex);
        }
    }
     /**
     * Function to get WorkOrderComponentDetails
     * @param requestParams = Parameters
     * @return = List of WorkOrderComponentDetails
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getWorkOrderComponentDetails(Map<String, Object> requestParams) throws ServiceException {
        List list = Collections.EMPTY_LIST;
        ArrayList paramList = new ArrayList();      
        try {
            String hql = "";
            String conditionHql = "";
            boolean isForCompAvailablity=false;
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullObject(requestParams.get("companyid"))) {
                conditionHql += " wocd.workOrder.company.companyID= ? ";
                paramList.add((String) requestParams.get("companyid"));
            }
            if (requestParams.containsKey("bills") && requestParams.get("bills") != null && !StringUtil.isNullOrEmpty(requestParams.get("bills").toString())) {
                if(StringUtil.isNullOrEmpty(conditionHql)){
                    conditionHql += " where wocd.workOrder.ID= ? ";
                } else {
                    conditionHql += " and wocd.workOrder.ID= ? ";
                }
                paramList.add((String) requestParams.get("bills"));
            }else if (requestParams.containsKey("projectId") && requestParams.get("projectId") != null && !StringUtil.isNullOrEmpty(requestParams.get("projectId").toString())) {
                if(StringUtil.isNullOrEmpty(conditionHql)){
                    conditionHql += " where wocd.workOrder.projectId = ? ";
                } else {
                    conditionHql += " and wocd.workOrder.projectId = ? ";
                }
                paramList.add((String) requestParams.get("projectId"));
            }
            if(requestParams.containsKey("productId") && requestParams.get("productId") != null && !StringUtil.isNullOrEmpty(requestParams.get("productId").toString())){
                if(StringUtil.isNullOrEmpty(conditionHql)){
                    conditionHql += " where wocd.product.ID = ? ";
                }else{
                    conditionHql += " and wocd.product.ID = ? ";
                }
                paramList.add((String) requestParams.get("productId"));
            }            
            if (requestParams.containsKey("isFromRejectedItemListReport")) {
                conditionHql += " and wocd.rejectedQuantity > ? ";
                paramList.add(0.0d);
            }
            if (requestParams.containsKey("isForCompAvailablity") && requestParams.get("isForCompAvailablity") != null && !StringUtil.isNullOrEmpty(requestParams.get("isForCompAvailablity").toString())) {
                isForCompAvailablity = Boolean.parseBoolean(requestParams.get("isForCompAvailablity").toString());
                if (isForCompAvailablity) {
                    conditionHql += " and wocd.parentProduct <> null  ";
                }

            }
            if (requestParams.containsKey(Constants.parentProduct) && requestParams.get(Constants.parentProduct) != null) {
                String parentProductID = ((Product) requestParams.get(Constants.parentProduct)).getID();
                conditionHql += " and wocd.parentProduct.ID=? ";
                paramList.add(parentProductID);
            }
            
            //ERP-35176 : Quick Search on Product ID, Product Name, WO ID, WO Name
            if(requestParams.containsKey("ss") && requestParams.get("ss") != null){
                conditionHql += " AND (wocd.product.productid LIKE ? OR wocd.product.name LIKE ? OR wocd.workOrder.workOrderID LIKE ? OR wocd.workOrder.workOrderName LIKE ?) ";
                paramList.add("%"+ (String) requestParams.get("ss") + "%");
                paramList.add("%"+ (String) requestParams.get("ss") + "%");
                paramList.add("%"+ (String) requestParams.get("ss") + "%");
                paramList.add("%"+ (String) requestParams.get("ss") + "%");
            }
            //ERP-35176 : From & To Date Filter applied
            if(requestParams.containsKey("startdate") && requestParams.get("startdate") != null && requestParams.containsKey("enddate") && requestParams.get("enddate") != null){
                String startDate = (String) requestParams.get(Constants.REQ_startdate);
                String endDate = (String) requestParams.get(Constants.REQ_enddate);        
                DateFormat df = (DateFormat) requestParams.get(Constants.df);
                conditionHql += " AND wocd.workOrder.workOrderDate >= ? AND wocd.workOrder.workOrderDate <= ?";    
                paramList.add(df.parse(startDate));
                paramList.add(df.parse(endDate));
            }
            
            hql += " from WorkOrderComponentDetails wocd where " + conditionHql;
            list = executeQuery(hql, paramList.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("WorkOrderDAOImpl.getWorkOrderComponentDetails", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
   
    @Override
    public KwlReturnObject deleteWorkOrderCustomData(Map<String, Object> deleteParams) throws ServiceException {
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
            throw ServiceException.FAILURE("WorkOrderDAOImpl.deleteWorkOrderCustomData: " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, ll, count);
    }
    
    @Override
    public  KwlReturnObject getPOforWOdetail(HashMap<String, Object> params) throws ServiceException {
        List ll = new ArrayList();
        int count = 0;
        ArrayList<String> paramsList = new ArrayList();
        try {
            String hql = "";
            String condition = "";
                if (params.containsKey("wodetailid") && params.containsKey("wodetailid")) {
                    condition = " where pod.workorderdetailid = ? ";
                    paramsList.add(params.get("wodetailid").toString());
                }
                hql = "select pod.purchaseorder from podetails pod " + condition;
                ll = executeSQLQuery(hql, paramsList.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("WorkOrderDAOImpl.deleteWorkOrderCustomData: " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, ll, ll.size());
    }
    @Override
    public  KwlReturnObject getPReqforWOdetail(HashMap<String, Object> params) throws ServiceException {
        List ll = new ArrayList();
        int count = 0;
        ArrayList<String> paramsList = new ArrayList();
        try {
            String hql = "";
            String condition = "";
                if (params.containsKey("wodetailid") && params.containsKey("wodetailid")) {
                    condition = " where pod.workorderdetailid = ? ";
                    paramsList.add(params.get("wodetailid").toString());
                }
                hql = "select pod.purchaserequisition from purchaserequisitiondetail pod " + condition;
                ll = executeSQLQuery(hql, paramsList.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("WorkOrderDAOImpl.deleteWorkOrderCustomData: " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, ll, ll.size());
    }
    
    public KwlReturnObject getWorkOrderCombo(Map<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String companyId = "";
        if (requestParams.containsKey("companyId")) {
            companyId = requestParams.get("companyId").toString();
            params.add(companyId);
        }
        String query = "select id,workordername,workorderid from workorder where company=?";
        List list = executeSQLQuery(query, params.toArray());
        int count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    public KwlReturnObject getWODetailfromProductandWO(HashMap<String,Object> reqMap) throws ServiceException {
         List ll = new ArrayList();
        int count = 0;
        ArrayList<String> paramsList = new ArrayList();
        try {
            String hql = "";
            String condition = "";
            if (reqMap.containsKey("companyid") && reqMap.containsKey("companyid")) {
                condition += " where wocd.workOrder.company.companyID= ? ";
                paramsList.add(reqMap.get("companyid").toString());
            }
            if (reqMap.containsKey("productid") && reqMap.containsKey("productid")) {
                if (StringUtil.isNullOrEmpty(condition)) {
                    condition += " where wocd.product.ID = ? ";
                } else {
                    condition += " and wocd.product.ID = ? ";
                }
                paramsList.add(reqMap.get("productid").toString());
            }
            /**
             * Quick Search on Product ID, Product Name.
             */
            if (reqMap.containsKey("ss")) {
                condition += " and wocd.product.productid LIKE ? or wocd.product.name LIKE ? ";
                paramsList.add("%" + reqMap.get("ss").toString() + "%");
                paramsList.add("%" + reqMap.get("ss").toString() + "%");
            }
            
            if (reqMap.containsKey("woid") && reqMap.containsKey("woid")) {
                condition += " and wocd.workOrder.ID = ? ";
                paramsList.add(reqMap.get("woid").toString());
            }
            if (reqMap.containsKey("parentproductid") && reqMap.containsKey("parentproductid")) {  // add a filter for parent product id
                condition += " and wocd.parentProduct.ID = ? ";
                paramsList.add(reqMap.get("parentproductid").toString());
            }
            hql = "select wocd from WorkOrderComponentDetails wocd " + condition;
            ll = executeQuery(hql, paramsList.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("WorkOrderDAOImpl.deleteWorkOrderCustomData: " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, ll, count);
    }
    
    @Override
    public KwlReturnObject getCheckListDetails(Map<String, Object> requestParams) throws ServiceException {
        List list = Collections.EMPTY_LIST;
        ArrayList paramList = new ArrayList();
        try {
            String hql = "", subhql = "";
            String conditionHql = "";
            boolean isGroup = false;
            if (requestParams.containsKey("isGroup") && !StringUtil.isNullObject(requestParams.get("isGroup"))) {
                isGroup = Boolean.parseBoolean(requestParams.get("isGroup").toString());
            }
            if(isGroup){
                subhql = " distinct qc.qcgroup.id, qc.qcgroup.value ";
            }else{
                subhql = " qc ";
            }
            
            if (requestParams.containsKey("companyId") && !StringUtil.isNullObject(requestParams.get("companyId"))) {
                conditionHql += " where qc.company.companyID= ? ";
                paramList.add((String) requestParams.get("companyId"));
            }
            if (requestParams.containsKey("productid") && !StringUtil.isNullObject(requestParams.get("productid"))) {
                conditionHql += " and (qc.product.ID= ? ) ";
                paramList.add((String) requestParams.get("productid"));
            }
            if (requestParams.containsKey("groupid") && !StringUtil.isNullObject(requestParams.get("groupid"))) {
                conditionHql += " and qc.qcgroup.ID= ? ";
                paramList.add((String) requestParams.get("groupid"));
            }
            hql += "select "+ subhql +" from QualityControl qc " + conditionHql;
            list = executeQuery(hql, paramList.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("WorkOrderDaoImpl.getCheckListDetails", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject getWOStatusidFromDefaultID(HashMap<String, Object> requestParams) throws ServiceException {  // function to fetch Wo status id from its default status id
        List list = Collections.EMPTY_LIST;
        ArrayList paramList = new ArrayList();
        try {
            String hql = "", subhql = "";
            String conditionHql = "";
            
            if (requestParams.containsKey("defaultStatusId") && !StringUtil.isNullObject(requestParams.get("defaultStatusId"))) {
                conditionHql += " where mi.defaultMasterItem.ID= ? ";
                paramList.add((String) requestParams.get("defaultStatusId"));
            }
            if (requestParams.containsKey("companyId") && !StringUtil.isNullObject(requestParams.get("companyId"))) {
                conditionHql += " and mi.company.companyID= ? ";
                paramList.add((String) requestParams.get("companyId"));
            }
            hql += "select mi from MasterItem mi " + conditionHql;
            list = executeQuery(hql, paramList.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("WorkOrderDaoImpl.getWOStatusidFromDefaultID", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getCostOfManufacturingDetails(Map<String, Object> requestParams) throws ServiceException {
        List list = Collections.EMPTY_LIST;
        ArrayList paramList = new ArrayList();
        try {
            String hql = "";
            String conditionHql = "";
            String joinCondition = "";
            String groupBy = "";
            
            int start = 0;
            int limit = 30;
            if (requestParams.containsKey("start") && requestParams.containsKey("limit") 
                    && !StringUtil.isNullOrEmpty(requestParams.get("start").toString()) && !StringUtil.isNullOrEmpty(requestParams.get("limit").toString())) {
                start = Integer.parseInt(requestParams.get("start").toString());
                limit = Integer.parseInt(requestParams.get("limit").toString());
            }

            if (requestParams.containsKey("companyid") && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
                conditionHql += " and wo.company = ? ";
                paramList.add((String) requestParams.get("companyid"));
            }
            
            if (requestParams.containsKey("productIds") && !StringUtil.isNullOrEmpty(requestParams.get("productIds").toString())) {
                conditionHql += " and wo.productid in ("+(String) requestParams.get("productIds")+") ";
            }

            if (requestParams.containsKey("WOstatus") && !StringUtil.isNullOrEmpty(requestParams.get("WOstatus").toString())) {
                conditionHql += " and wo.workorderstatus = ? ";
                paramList.add(requestParams.get("WOstatus").toString());
            }

            if (requestParams.containsKey(Constants.REQ_startdate) && !StringUtil.isNullObject(requestParams.get(Constants.REQ_startdate))) {
                Date startDate = (Date) requestParams.get(Constants.REQ_startdate);
                conditionHql += " and wo.workorderdate >= ? ";
                paramList.add(startDate);
            }

            if (requestParams.containsKey(Constants.REQ_enddate) && !StringUtil.isNullObject(requestParams.get(Constants.REQ_enddate))) {
                Date endDate = (Date) requestParams.get(Constants.REQ_enddate);
                conditionHql += " and wo.workorderdate <= ? ";
                paramList.add(endDate);
            }
            int colnum = (int) requestParams.get("productcolnum");
            conditionHql += " and pcd.col" + colnum + " IS NOT NULL ";

            joinCondition += " inner join workorder wo on p.id=wo.productid inner join accproductcustomdata pcd on p.id=pcd.productId ";
            groupBy += " group by p.id ";
            //costofproduct & quantity is not used from query output. Get these separately.
            hql = " select p.id, p.name, 20000.0 as costofproduct, sum(wo.quantity), wo.workorderstatus,pcd.col"+colnum+" from product p " + joinCondition 
                    + " where wo.deleted != 'T' " + conditionHql + groupBy;
            list = executeSQLQueryPaging(hql, paramList.toArray(),new Integer[]{start, limit});
        } catch (NumberFormatException | ServiceException ex) {
            throw ServiceException.FAILURE("WorkOrderDAOImpl.getCostOfManufacturingDetails", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getCostCategoryExpenseAmount(Map<String, Object> requestParams) throws ServiceException {
        List list = Collections.EMPTY_LIST;
        ArrayList paramList = new ArrayList();
        try {
            String hql = "";
            String conditionHql = "";
            String joinCondition = "";
            String groupBy = "";

            conditionHql += " je.company = ? ";
            paramList.add((String) requestParams.get("companyid"));

            if (requestParams.containsKey("fcdids") && !StringUtil.isNullObject(requestParams.get("fcdids")) && !StringUtil.isNullOrEmpty(requestParams.get("fcdids").toString())) {
                conditionHql += " and fcd.id in ("+(String) requestParams.get("fcdids")+") ";
            }

            if (requestParams.containsKey(Constants.REQ_startdate) && !StringUtil.isNullObject(requestParams.get(Constants.REQ_startdate))  && !StringUtil.isNullOrEmpty(requestParams.get(Constants.REQ_startdate).toString())) {
                Date startDate = (Date) requestParams.get(Constants.REQ_startdate);
                conditionHql += " and je.entrydate >= ? ";
                paramList.add(startDate);
            }

            if (requestParams.containsKey(Constants.REQ_enddate) && !StringUtil.isNullObject(requestParams.get(Constants.REQ_enddate)) && !StringUtil.isNullOrEmpty(requestParams.get(Constants.REQ_enddate).toString())) {
                Date endDate = (Date) requestParams.get(Constants.REQ_enddate);
                conditionHql += " and je.entrydate <= ? ";
                paramList.add(endDate);
            }
            int colnum=(int)requestParams.get("accountcolnum");
            joinCondition += " inner join journalentry je on je.id=jed.journalEntry "
                    + "inner join goodsreceipt gr on gr.journalentry=je.id "
                    + " inner join account ac on jed.account=ac.id "
                    + " inner join accountcustomdata accust on accust.accountId=ac.id "
                    + " inner join fieldcombodata fcd on FIND_IN_SET(fcd.id,accust.col"+colnum+")";
            groupBy += " group by fcd.value ";
            hql = "select fcd.value, sum(if(jed.debit='T', jed.amountinbase, -jed.amountinbase)) as expenseamount from jedetail jed " 
                    + joinCondition + " where "
//                    + " je.deleteflag!='T' " 
                    + conditionHql + groupBy;

            list = executeSQLQuery(hql, paramList.toArray());
        } catch (NumberFormatException | ServiceException ex) {
            throw ServiceException.FAILURE("WorkOrderDAOImpl.getCostOfManufacturingDetails", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
}
