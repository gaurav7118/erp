/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.machinemanagement;

import com.krawler.common.admin.Company;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.AssetDetails;
import com.krawler.hql.accounting.MasterItem;
import com.krawler.hql.accounting.SequenceFormat;
import com.krawler.hql.accounting.Vendor;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.mrp.workcentremanagement.WorkCentre;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.util.Date;
import java.text.DateFormat;
import java.util.*;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 *
 * @author krawler
 */
public class AccMachineManagementDAOImpl extends BaseDAO implements AccMachineManagementDAO {

    public KwlReturnObject saveMachineMaster(Map<String, Object> dataMap) throws ServiceException {

        List list = new ArrayList();
        String id = "";
        try {
            if (dataMap.containsKey("id")) {
                id = (String) dataMap.get("id");
            }

            Machine machine = new Machine();
            if (StringUtil.isNullOrEmpty(id)) {

                if (dataMap.containsKey("createdon")) {
                    machine.setCreatedon((Long) dataMap.get("createdon"));
                }

            } else {
                machine = (Machine) get(Machine.class, id);
            }

            if (dataMap.containsKey("machinename")) {
                machine.setMachineName((String) dataMap.get("machinename"));
            }
            if (dataMap.containsKey("machineid")) {
                machine.setMachineID((String) dataMap.get("machineid"));
            }
            if (dataMap.containsKey("machineserialno")) {
                machine.setMachineSerialNo((String) dataMap.get("machineserialno"));
            }

            if (dataMap.containsKey("machineoperatingcapacity")) {
                machine.setMachineOperatingCapacity(Double.parseDouble(dataMap.get("machineoperatingcapacity").toString()));
            }
            if (dataMap.containsKey("machineusescount")) {
                machine.setMachineUsesCount(Double.parseDouble(dataMap.get("machineusescount").toString()));
            }

            if (dataMap.containsKey("dateofinstallation")) {
                machine.setDateOfInstallation((Date) dataMap.get("dateofinstallation"));
            }
            if (dataMap.containsKey("dateofpurchase")) {
                machine.setDateOfPurchase((Date) dataMap.get("dateofpurchase"));
            }
            if (dataMap.containsKey("insuranceduedate")) {
                machine.setInsuranceDueDate((Date) dataMap.get("insuranceduedate"));
            }

            if (dataMap.containsKey("ageofmachine")) {
                machine.setAgeOfMachine((String) dataMap.get("ageofmachine"));
            }
            if (dataMap.containsKey("ismachineonlease")) {

                boolean ismachineonlease = Boolean.parseBoolean(dataMap.get("ismachineonlease").toString());
                machine.setIsMachineOnLease(ismachineonlease);
                if (ismachineonlease) {
                    if (dataMap.containsKey("startdateoflease")) {
                        machine.setStartDateOfLease((Date) dataMap.get("startdateoflease"));
                    }
                    if (dataMap.containsKey("enddateoflease")) {
                        machine.setEndDateOfLease((Date) dataMap.get("enddateoflease"));
                    }
                    if (dataMap.containsKey("leaseyears")) {
                        machine.setLeaseYears(Double.parseDouble(dataMap.get("leaseyears").toString()));
                    }
                    if (dataMap.containsKey("machineprice")) {
                        machine.setMachinePrice(Double.parseDouble(dataMap.get("machineprice").toString()));
                    }
                    if (dataMap.containsKey("depreciationmethod")) {
                        machine.setDepreciationMethod(Byte.parseByte(dataMap.get("depreciationmethod").toString()));
                    }
                    if (dataMap.containsKey("depreciationrate")) {
                        machine.setDepreciationRate(Double.parseDouble(dataMap.get("depreciationrate").toString()));
                    }
                } else {
                    byte defVal = 1;
                    machine.setEndDateOfLease(null);
                    machine.setStartDateOfLease(null);
                    machine.setDepreciationMethod(defVal);
                    machine.setDepreciationRate(0);
                    machine.setMachinePrice(0);
                    machine.setLeaseYears(0);
                }

            }

            if (dataMap.containsKey("autogenerated")) {
                boolean isautogenerated = Boolean.parseBoolean(dataMap.get("autogenerated").toString());
                machine.setAutoGenerated(isautogenerated);
            }

            if (dataMap.containsKey(Constants.companyid) && dataMap.get(Constants.companyid) != null) {
                Company company = (Company) get(Company.class, (String) dataMap.get(Constants.companyid));
                machine.setCompany(company);
            }
            if (dataMap.containsKey(Constants.SEQFORMAT)) {
                machine.setSeqformat((SequenceFormat) get(SequenceFormat.class, (String) dataMap.get(Constants.SEQFORMAT)));
            }
            if (dataMap.containsKey(Constants.SEQNUMBER)) {
                machine.setSeqnumber(Integer.parseInt(dataMap.get(Constants.SEQNUMBER).toString()));
            }

            if (dataMap.containsKey(Constants.DATEPREFIX) && dataMap.get(Constants.DATEPREFIX) != null) {
                machine.setDatePreffixValue((String) dataMap.get(Constants.DATEPREFIX));
            }
            if (dataMap.containsKey(Constants.DATEAFTERPREFIX) && dataMap.get(Constants.DATEAFTERPREFIX) != null) {
                machine.setDateAfterPreffixValue((String) dataMap.get(Constants.DATEAFTERPREFIX));
            }
            if (dataMap.containsKey(Constants.DATESUFFIX) && dataMap.get(Constants.DATESUFFIX) != null) {
                machine.setDateSuffixValue((String) dataMap.get(Constants.DATESUFFIX));
            }
            if (dataMap.containsKey("shifttiming") && dataMap.get("shifttiming") != null) {
                machine.setShifttiming((String) dataMap.get("shifttiming"));
            }

            if (dataMap.containsKey("vendorid")) {

                Vendor vendor = dataMap.get("vendorid") == null ? null : (Vendor) get(Vendor.class, (String) dataMap.get("vendorid"));
                machine.setVendor(vendor);
            }

            if (dataMap.containsKey("purchaseaccount")) {
                Account paccount = dataMap.get("purchaseaccount") == null ? null : (Account) get(Account.class, (String) dataMap.get("purchaseaccount"));
                machine.setPurchaseAccount(paccount);
//                if (paccount != null) {
//                    String usedin = paccount.getUsedIn();
//                    paccount.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Product_Purchase_Account));
//                }
            }
            if (dataMap.containsKey("issubstitutemachine")) {
                boolean isSubstituteMachine = Boolean.parseBoolean(dataMap.get("issubstitutemachine").toString());
                machine.setIsSubstitute(isSubstituteMachine);
            }
            if (dataMap.containsKey("isassetmachine")) {
                machine.setIsAsset(Boolean.parseBoolean(dataMap.get("isassetmachine").toString()));
            } else {
                machine.setIsAsset(false);
            }
            
            if (dataMap.containsKey("accmachinecustomdataref") && dataMap.get("accmachinecustomdataref") != null) {
                MachineCustomData machineCustomData = null;
                machineCustomData = (MachineCustomData) get(MachineCustomData.class, (String) dataMap.get("accmachinecustomdataref"));
                machine.setAccMachineCustomData(machineCustomData);
            }
            saveOrUpdate(machine);
            list.add(machine);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(" saveMachineMaster : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, null, null, list, list.size());

    }
    public KwlReturnObject saveMachineProcessMapping(Map<String, Object> dataMap) throws ServiceException {

        List list = new ArrayList();
        try {
            MachineProcessMapping machineProcessMapping = new MachineProcessMapping();
            if (dataMap.containsKey("processid")) {
                MasterItem process = dataMap.get("processid") == null ? null : (MasterItem) get(MasterItem.class, (String) dataMap.get("processid"));
                machineProcessMapping.setProcessID(process);
            }
            if (dataMap.containsKey("machineid")) {
                Machine machine = dataMap.get("machineid") == null ? null : (Machine) get(Machine.class, (String) dataMap.get("machineid"));
                machineProcessMapping.setMachineID(machine);
            }
            if (dataMap.containsKey(Constants.companyid) && dataMap.get(Constants.companyid) != null) {
                Company company = (Company) get(Company.class, (String) dataMap.get(Constants.companyid));
                machineProcessMapping.setCompany(company);
            }
            saveOrUpdate(machineProcessMapping);
            list.add(machineProcessMapping);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(" saveMachineMaster : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, null, null, list, list.size());

    }
    public KwlReturnObject saveMachineWorkCenterMapping(Map<String, Object> dataMap) throws ServiceException {

        List list = new ArrayList();
        try {
            MachineWorkCenterMapping machineWorkCenterMapping = new MachineWorkCenterMapping();
            if (dataMap.containsKey("workcenter")) {
                WorkCentre workCentre = dataMap.get("workcenter") == null ? null : (WorkCentre) get(WorkCentre.class, (String) dataMap.get("workcenter"));
                machineWorkCenterMapping.setWorkCenterID(workCentre);
            }
            if (dataMap.containsKey("machineid")) {
                Machine machine = dataMap.get("machineid") == null ? null : (Machine) get(Machine.class, (String) dataMap.get("machineid"));
                machineWorkCenterMapping.setMachineID(machine);
            }
            if (dataMap.containsKey(Constants.companyid) && dataMap.get(Constants.companyid) != null) {
                Company company = (Company) get(Company.class, (String) dataMap.get(Constants.companyid));
                machineWorkCenterMapping.setCompany(company);
            }
            saveOrUpdate(machineWorkCenterMapping);
            list.add(machineWorkCenterMapping);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(" saveMachineMaster : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, null, null, list, list.size());

    }
    public KwlReturnObject saveMachineAssetMapping(Map<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            MachineAssetMapping machineAssetMapping = new MachineAssetMapping();
            if (dataMap.containsKey("assetdetailId")) {
                AssetDetails assetDetails = dataMap.get("assetdetailId") == null ? null : (AssetDetails) get(AssetDetails.class, (String) dataMap.get("assetdetailId"));
                machineAssetMapping.setAssetDetails(assetDetails);
            }
            if (dataMap.containsKey("machineid")) {
                Machine machine = dataMap.get("machineid") == null ? null : (Machine) get(Machine.class, (String) dataMap.get("machineid"));
                machineAssetMapping.setMachine(machine);
            }
            if (dataMap.containsKey(Constants.companyid) && dataMap.get(Constants.companyid) != null) {
                Company company = (Company) get(Company.class, (String) dataMap.get(Constants.companyid));
                machineAssetMapping.setCompany(company);
            }
            saveOrUpdate(machineAssetMapping);
            list.add(machineAssetMapping);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(" saveMachineMaster : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, null, null, list, list.size());

    }
    
    public KwlReturnObject updateMachineSyncableFlag(Map<String, Object> dataMap) throws ServiceException {
        JSONArray machineIdsArray=null;
        List list = new ArrayList();
        if (dataMap.containsKey("ids")) {
            machineIdsArray = (JSONArray) dataMap.get("ids");
        }
        String ids = "";
        for (int i = 0; i < machineIdsArray.length(); i++) {
            ids += "'" + machineIdsArray.optString(i) + "',";
        }
        String query = "update machine set syncable='T' where id in (" + ids.substring(0, ids.length() - 1) + ")";
        int count = executeSQLUpdate(query);
        return new KwlReturnObject(true, "", null, list, count);

    }
    public KwlReturnObject getMachineAssetMaintenaceDetails(Map<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        List paramList = new ArrayList();
        try {
            String hql = "";
            String conditionHql = "";
            
            if (requestParams.containsKey("assetdetailsid")) {
                conditionHql += " obj.assetDetails.id= ? ";
                paramList.add((String)requestParams.get("assetdetailsid"));
            }
            hql += " SELECT obj.assetMaintenanceSchedulerObject from AssetMaintenanceScheduler obj where " + conditionHql;
           list= executeQuery(hql, paramList.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getMachineAssetMapping", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
       
    }
    public KwlReturnObject saveSubstituteMachineMapping(Map<String, Object> dataMap) throws ServiceException {

        List list = new ArrayList();
        try {
            SubstituteMachineMapping substituteMachineMapping = new SubstituteMachineMapping();
            if (dataMap.containsKey("activeid")) {
                Machine activeMachine = dataMap.get("activeid") == null ? null : (Machine) get(Machine.class, (String) dataMap.get("activeid"));
                substituteMachineMapping.setActiveMachineID(activeMachine);
            }
            if (dataMap.containsKey("machineid")) {
                Machine machine = dataMap.get("machineid") == null ? null : (Machine) get(Machine.class, (String) dataMap.get("machineid"));
                substituteMachineMapping.setSubstituteMachineID(machine);
            }
            if (dataMap.containsKey(Constants.companyid) && dataMap.get(Constants.companyid) != null) {
                Company company = (Company) get(Company.class, (String) dataMap.get(Constants.companyid));
                substituteMachineMapping.setCompany(company);
            }
            saveOrUpdate(substituteMachineMapping);
            list.add(substituteMachineMapping);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(" saveMachineMaster : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, null, null, list, list.size());

    }
    public KwlReturnObject checkActiveMachineMapping(Map<String, Object> dataMap) throws ServiceException {

        List list = new ArrayList();
        ArrayList params1 = new ArrayList();
        String condition="";
        try {
            SubstituteMachineMapping substituteMachineMapping = new SubstituteMachineMapping();
            if (dataMap.containsKey("companyid")) {
                params1.add(dataMap.get("companyid"));
                condition+=" where company.companyID= ?";
            }
            if (dataMap.containsKey("id")) {
                params1.add(dataMap.get("id"));
               condition+=" and activeMachineID.ID=?";
            }
            String selQuery = "from SubstituteMachineMapping "+condition;
            list = executeQuery(selQuery, params1.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE(" saveMachineMaster : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, null, null, list, list.size());

    }
    public KwlReturnObject deleteMachineMasterPermanently(Map<String, Object> dataMap) throws ServiceException {

        ArrayList params1 = new ArrayList();
        String id = "", delQuery1 = "", delQuery2 = "",delQuery3="",delQuery4="",delQuery5="",delQuery6="";
        int numRows1 = 0;
        try {

            if (dataMap.containsKey("companyid")) {
                params1.add(dataMap.get("companyid"));
            }
            if(dataMap.containsKey("id")){
                params1.add(dataMap.get("id"));

            }
            delQuery1 = "delete from  machine_process_mapping where company = ? and machineid=?";
            numRows1 = executeSQLUpdate(delQuery1, params1.toArray());

            delQuery2 = "delete from  substitute_machine_mapping where company = ? and substitutemachineid=?";
            numRows1 = executeSQLUpdate(delQuery2, params1.toArray());

            delQuery3 = "delete from machine_work_center_mapping where company = ? and machineid=?";
            numRows1 = numRows1 + executeSQLUpdate(delQuery3, params1.toArray());
            
            delQuery4 = "delete from machine_asset_mapping where company = ? and machine=?";
            numRows1 = numRows1 + executeSQLUpdate(delQuery4, params1.toArray());

            delQuery5 = "delete from machinecustomdata where company = ? and machineId=?";
            numRows1 = numRows1 + executeSQLUpdate(delQuery5, params1.toArray());
            
             String delQuery = "delete from machinemanratio  where company = ? and id = ? ";
             numRows1 = numRows1 + executeSQLUpdate(delQuery, params1.toArray());
             
            delQuery6 = "delete from machine where company = ? and id=?";
            numRows1 = numRows1 + executeSQLUpdate(delQuery6, params1.toArray());

        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteMachineMasterPermanently : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, "Machine(s) has been deleted successfully.", null, null, numRows1);

    }
    public KwlReturnObject deleteMachineMaster(Map<String, Object> dataMap) throws ServiceException {
        ArrayList params1 = new ArrayList();
        String delQuery1 = "";
        int numRows1 = 0;
        try {
            if (dataMap.containsKey("companyid")) {
                params1.add(dataMap.get("companyid"));
            }
            if (dataMap.containsKey("id")) {
                params1.add(dataMap.get("id"));
            }
            delQuery1 = "update machine set deleted='T' where company = ? and id=?";
            numRows1 = executeSQLUpdate(delQuery1, params1.toArray());

        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteMachineMaster : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, "Machine(s) has been deleted successfully.", null, null, numRows1);

    }
    public KwlReturnObject deleteMachineProcessMapping(Map<String, Object> dataMap) throws ServiceException {
        ArrayList params1 = new ArrayList();
        String id = "", delQuery1 = "";
        int numRows1 = 0;
        try {
            if (dataMap.containsKey("machineid")) {
                params1.add(dataMap.get("machineid"));
                delQuery1 = "delete from  machine_process_mapping where machineid=?";
                numRows1 = executeSQLUpdate(delQuery1, params1.toArray());
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteMachineMaster : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, "Machine(s) has been deleted successfully.", null, null, numRows1);

    }

    public KwlReturnObject deleteSubstituteMachineMapping(Map<String, Object> dataMap) throws ServiceException {
        ArrayList params1 = new ArrayList();
        String id = "", delQuery1 = "";
        int numRows1 = 0;
        try {
            if (dataMap.containsKey("machineid")) {
                params1.add(dataMap.get("machineid"));
            }
            if (dataMap.containsKey("companyid")) {
                params1.add(dataMap.get("companyid"));
            }
            delQuery1 = "delete from  substitute_machine_mapping where substitutemachineid=? and company=?";
            numRows1 = executeSQLUpdate(delQuery1, params1.toArray());

        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteMachineMaster : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, "Substitute Machine Mapping has been deleted successfully.", null, null, numRows1);

    }

    public KwlReturnObject deleteMachineWorkCenterMapping(Map<String, Object> dataMap) throws ServiceException {
        ArrayList params1 = new ArrayList();
        String id = "", delQuery1 = "";
        int numRows1 = 0;
        try {
            if (dataMap.containsKey("machineid")) {
                params1.add(dataMap.get("machineid"));
            }
            if (dataMap.containsKey("companyid")) {
                params1.add(dataMap.get("companyid"));
            }
            delQuery1 = "delete from  machine_work_center_mapping where machineid=? and company=?";
            numRows1 = executeSQLUpdate(delQuery1, params1.toArray());

        } catch (Exception ex) {
            throw ServiceException.FAILURE("delete Machine Work Center Mappping : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, "Machine  Work Center Mapping has been deleted successfully.", null, null, numRows1);

    }
    public KwlReturnObject deleteMachineAssetMapping(Map<String, Object> dataMap) throws ServiceException {
        ArrayList params1 = new ArrayList();
        String id = "", delQuery1 = "";
        int numRows1 = 0;
        try {
            if (dataMap.containsKey("machineid")) {
                params1.add(dataMap.get("machineid"));
            }
            if (dataMap.containsKey("companyid")) {
                params1.add(dataMap.get("companyid"));
            }
            delQuery1 = "delete from  machine_asset_mapping where machine=? and company=?";
            numRows1 = executeSQLUpdate(delQuery1, params1.toArray());

        } catch (Exception ex) {
            throw ServiceException.FAILURE("delete Machine Work Center Mappping : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Machine  Work Center Mapping has been deleted successfully.", null, null, numRows1);
    }
    public KwlReturnObject getMachineMasterData(Map<String, Object> requestParams) throws ServiceException {
        String condition = "", joinCondition = "";
        String start = "";
        String limit = "";
        String moduleId="";
        List list = new ArrayList();
        try {

            ArrayList params = new ArrayList();
            String machineids="";
            if (requestParams.containsKey("machineids")) {
                machineids = requestParams.get("machineids").toString();
            }
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String companyid = (String) requestParams.get("companyid");
            params.add(companyid);

            String startDate = (String) requestParams.get(Constants.REQ_startdate);
            String endDate = (String) requestParams.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                condition += " and (mch.dateOfPurchase >=? and mch.dateOfPurchase <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }

            if (requestParams.containsKey("moduleid")) {
                moduleId = (String) requestParams.get("moduleid");
            }
            if (requestParams.containsKey("isactivemachine")) {
                condition += " and mch.isSubstitute='F'";
            }
            if (requestParams.containsKey("issubstitutemachine")) {
                condition += " and mch.isSubstitute='T'";
            }
            if (requestParams.containsKey("isleasemachine")) {
                condition += " and mch.isMachineOnLease='T'";
            }
            if (requestParams.containsKey("syncable")) {
                condition += " and mch.syncable='F'";
            }
            if (requestParams.containsKey("workcenterid")) {
                joinCondition += " INNER JOIN mch.machineWorkCenterMappingDetails mwm ";
                condition += " and mwm.workCenterID.ID= ? ";
                params.add((String) requestParams.get("workcenterid"));
            }
             if (!StringUtil.isNullOrEmpty(machineids)) {
                machineids = AccountingManager.getFilterInString(machineids);
                condition += " and mch.ID in " + machineids + "  ";
            }

            String ss = (requestParams.containsKey("ss") && requestParams.get("ss") != null) ? (String) requestParams.get("ss") : "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"mch.machineName", "mch.machineID"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 2);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery;
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
                        tableArray.add("customer"); //this table array used to identified wheather join exists on table or not                         
                        Map<String, Object> map = buildSqlDefaultFieldAdvSearch(defaultSearchFieldArray, params, moduleId, tableArray, filterConjuctionCriteria);
                        joinCondition += " LEFT JOIN mch.machineWorkCenterMappingDetails mwc ";
                        joinCondition += " LEFT JOIN mch.machineProcessMappingDetails mpd ";
                        searchDefaultFieldSQL = (String) (map.containsKey("condition") ? map.get("condition") : "");
                        searchDefaultFieldSQL = searchDefaultFieldSQL.replaceAll("machineRef.machineWorkCenterMappingDetails", "mwc");
                        searchDefaultFieldSQL = searchDefaultFieldSQL.replaceAll("machineRef.machineProcessMappingDetails", "mpd");
                        searchDefaultFieldSQL = searchDefaultFieldSQL.replaceAll("machineRef", "mch");
                    }
                    if (customSearchFieldArray.length() > 0) {
                        /*
                         Advance Search For Custom fields
                         */
                        requestParams.put(Constants.Searchjson, Searchjson);
                        requestParams.put(Constants.appendCase, appendCase);
                        requestParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(requestParams, false).get(Constants.myResult));
                        if (mySearchFilterString.contains("c.MachineCustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("c.MachineCustomData", "mch.accMachineCustomData");
                        }
                        StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                    }
                    mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
                }
            }

            String selQuery = "select DISTINCT mch from  Machine mch " + joinCondition + "where mch.company.companyID= ? " + condition + mySearchFilterString;
            
            if (requestParams.containsKey(Constants.start) && requestParams.get(Constants.start) != null && requestParams.containsKey(Constants.limit) && requestParams.get(Constants.limit) != null) {
                start = (String) requestParams.get(Constants.start);
                limit = (String) requestParams.get(Constants.limit);
            }

            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeQueryPaging(selQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            } else {
                list = executeQuery(selQuery, params.toArray());
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("getMachineMasterData : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, null, null, list, list.size());

    }
    
    @Override
    public KwlReturnObject saveMachineCost(Map<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            MachineCost resourceCost = new MachineCost();
            if (dataMap.containsKey("resourceCostId")) {
                resourceCost = dataMap.get("resourceCostId") == null ? null : (MachineCost) get(MachineCost.class, (String) dataMap.get("resourceCostId"));
            }
            if (dataMap.containsKey("labourId")) {
                Machine machine = dataMap.get("labourId") == null ? null : (Machine) get(Machine.class, (String) dataMap.get("labourId"));
                resourceCost.setMachine(machine);
            }
            if (dataMap.containsKey("resourcecost")) {
                String cost = (String) dataMap.get("resourcecost");
                resourceCost.setMachinecost(cost);
            }
            if (dataMap.containsKey("effectivedate")) {
                Date date = (Date) dataMap.get("effectivedate");
                resourceCost.setEffectivedate(date);
            }
            if (dataMap.containsKey("company") && dataMap.get("company") != null) {
                resourceCost.setCompany((Company) get(Company.class, (String) dataMap.get("company")));
            }
            saveOrUpdate(resourceCost);
            list.add(resourceCost);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(" saveMachineCost : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject deleteMachineCost(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result = null;
        ArrayList params = new ArrayList();
        String conditionSql = "";
        try {
            if (requestParams.containsKey("companyId")) {
                String companyId = "";
                companyId = requestParams.get("companyId").toString();
                params.add(companyId);
            }
            if (requestParams.containsKey("resourcecostid")) {
                conditionSql += " and ID=?";
                params.add(requestParams.get("resourcecostid").toString());
            }
            String query = "delete from MachineCost where company.companyID=? " + conditionSql;
            int num = executeUpdate(query, params.toArray());
            result = new KwlReturnObject(true, null, null, null, num);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return result;
    }
    
    @Override
    public KwlReturnObject getMachineCostSQL(Map<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result = null;
        ArrayList params = new ArrayList();
        String conditionSql = "";
        List list=null;
        String query = "",groupconditionSql="";
        try {
            if (requestParams.containsKey("labourId")) {
                conditionSql += " r.machine=?";
                params.add(requestParams.get("labourId").toString());
            }
            if (requestParams.containsKey("companyId")) {
                conditionSql += (conditionSql.length() > 0) ? " and r.company=? " : " r.company=? ";
                params.add(requestParams.get("companyId").toString());
            }
            if (requestParams.containsKey("workDate")) {
                conditionSql += " and r.effectivedate <= ?  ORDER BY r.effectivedate DESC ";
                params.add((Date) requestParams.get("workDate"));
            } else {
                if (requestParams.containsKey("maxdate")) {
                    conditionSql += " ORDER BY r.effectivedate DESC LIMIT 1";
                } else if (!requestParams.containsKey("maxdate")) {
                    conditionSql += " ORDER BY r.effectivedate DESC";
                    groupconditionSql += " GROUP BY machine ";
                }
            }
            if (requestParams.containsKey("labourId")) {
                query = "SELECT * FROM machinecost r WHERE " + conditionSql;
            } else {
                query = "SELECT * FROM (SELECT * FROM machinecost r WHERE " + conditionSql + " ) as t " + groupconditionSql;
            }
            
            list = executeSQLQuery(query, params.toArray());
            
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return result;
    }
    
    @Override
    public KwlReturnObject getMachineCost(Map<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result = null;
        ArrayList params = new ArrayList();
        String conditionSql = "";
        List list=null;
        try {
            if (requestParams.containsKey("labourId")) {
                conditionSql += " r.machine.ID=?";
                params.add(requestParams.get("labourId").toString());
            }
            
            //Companyid
            
            if(requestParams.containsKey("workDate")){
                conditionSql += " and r.effectivedate <= ?  ORDER BY r.effectivedate DESC ";
                params.add((Date)requestParams.get("workDate"));
            }           
            if (requestParams.containsKey("maxdate")) {
                conditionSql += " ORDER BY r.effectivedate DESC LIMIT 1";
            } 
            String query = "From MachineCost r where " + conditionSql;
            if (requestParams.containsKey("workDate") || requestParams.containsKey("maxdate")) {
                //Limit is not supported by HQL so added paging parameters
                list = executeQueryPaging(query, params.toArray(), new Integer[]{0,1});
            } else {

                list = executeQuery(query, params.toArray());
            }
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return result;
    }
    
    public KwlReturnObject getActiveSubstituteMachines(Map<String, Object> requestParams) throws ServiceException {

        List list = new ArrayList();
        try {
            String condition = "";
            ArrayList params = new ArrayList();
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String companyid = (String) requestParams.get("companyid");
            params.add(companyid);

            if (requestParams.containsKey("isactivemachine")) {
                condition = " and isSubstitute='F'";
            }

            String selQuery = "from Machine where company.companyID= ? " + condition;
            list = executeQuery(selQuery, params.toArray());

        } catch (Exception ex) {
            throw ServiceException.FAILURE("getActiveSubstituteMachines : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, null, null, list, list.size());

    }

    public KwlReturnObject isMachineIDAlreadyPresent(Map<String, Object> requestParams) throws ServiceException {

        List list = new ArrayList();
        try {
            String condition = "";
            ArrayList params = new ArrayList();
//            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String companyid = (String) requestParams.get("companyid");
            params.add(companyid);

            if (requestParams.containsKey("machineid")) {
                condition = " and machineID=?";
                params.add(requestParams.get("machineid"));
            }
            if (requestParams.containsKey("id") && !StringUtil.isNullOrEmpty((String) requestParams.get("id"))) {
                condition += " and ID != ? ";
                params.add((String) requestParams.get("id"));
            }
            
            String selQuery = "from Machine where company.companyID= ? " + condition;
            list = executeQuery(selQuery, params.toArray());

        } catch (Exception ex) {
            throw ServiceException.FAILURE("MachineIDAlreadyPresent : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, null, null, list, list.size());

    }
    
    public KwlReturnObject getWCforMachine(Map<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String condition = "";
            ArrayList params = new ArrayList();
            
            if (dataMap.containsKey("machineid")) {
                condition = " where mwc.machineID.ID=?";
                params.add(dataMap.get("machineid"));
            }
            
            String selQuery = "select mwc.ID from MachineWorkCenterMapping mwc " + condition;
            list = executeQuery(selQuery, params.toArray());
            
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public KwlReturnObject getWOforMachine(Map<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String condition = "";
            ArrayList params = new ArrayList();
            
            if (dataMap.containsKey("machineid")) {
                condition = " where mwo.machineid.ID=?";
                params.add(dataMap.get("machineid"));
            }
            
            String selQuery = "select mwo.id from WorkOrderMachineMapping mwo " + condition;
            list = executeQuery(selQuery, params.toArray());
            
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public KwlReturnObject getRTforMachine(Map<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String condition = "";
            ArrayList params = new ArrayList();
            
            if (dataMap.containsKey("machineid")) {
                condition = " where mrt.machineid.ID=?";
                params.add(dataMap.get("machineid"));
            }
            
            String selQuery = "select mrt.id from RoutingTemplateMachineMapping mrt " + condition;
            list = executeQuery(selQuery, params.toArray());
            
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public KwlReturnObject getMachineCombo(Map<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String companyId = "";
        List list = null;
        int count = 0;
        try {
            if (requestParams.containsKey("companyId")) {
                companyId = requestParams.get("companyId").toString();
                params.add(companyId);
            }
            String query = "select id,machinename,machineid from machine where company=?";
            list = executeSQLQuery(query, params.toArray());
            
            
            if (requestParams.containsKey("workcenterid")) {  // to fetch machine for a specific workcentre
                params.clear();
                String tempArr[] = requestParams.get("workcenterid").toString().split(",");
                String tempStr = "";
                for (int cnt = 0; cnt < tempArr.length; cnt++) {
                    if (cnt == 0) {
                        tempStr += "'" +tempArr[cnt] +"'";
                    } else {
                        tempStr += ",'" +tempArr[cnt] +"'";
                    }
                }
                query = "select DISTINCT(mwcm.machineID.ID),mwcm.machineID.machineName,mwcm.machineID.machineID from MachineWorkCenterMapping mwcm where mwcm.workCenterID.ID in ("+tempStr+")";
//                params.add(tempStr);
                list = executeQuery(query);
            }
            
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("MachineIDAlreadyPresent : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    
     @Override
    public KwlReturnObject saveMachineManRatio(MachineManRatio machineManRatio) throws ServiceException {
        List list = new ArrayList();
        try {
            saveOrUpdate(machineManRatio);
            list.add(machineManRatio);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveMachineManRatio : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject getMachineManRatio(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            String query = "from MachineManRatio ";
            return buildNExecuteQuery(query, requestParams);

        } catch (Exception ex) {
            throw ServiceException.FAILURE("getMachineManRatio : " + ex.getMessage(), ex);
        }

    }

    @Override
    public KwlReturnObject deleteMachineManRatio(String companyID, String MachineManRatioId) throws ServiceException {
        int numRows = 0;
        try {
            String delQuery = "delete from MachineManRatio r where r.company.companyID = ? and r.ID = ? ";
            numRows = executeUpdate(delQuery, new Object[]{companyID, MachineManRatioId});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteMachineManRatio : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Machine Man ratio deleted successfully.", null, null, numRows);
    } 
}
