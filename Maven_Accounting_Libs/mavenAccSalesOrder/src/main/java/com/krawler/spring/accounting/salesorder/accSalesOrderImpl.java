/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.spring.accounting.salesorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krawler.accounting.integration.common.IntegrationConstants;
import com.krawler.common.admin.*;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.BillingSalesOrderDetail;
import com.krawler.inventory.model.inspection.InspectionForm;
import com.krawler.inventory.model.inspection.InspectionTemplate;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.MessageSource;

/**
 *
 * @author krawler
 */
public class accSalesOrderImpl extends BaseDAO implements accSalesOrderDAO {
    
    private MessageSource messageSource;
    
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public KwlReturnObject getSalesOrderCount(String orderno, String companyid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = "from SalesOrder where salesOrderNumber=? and company.companyID=? AND isDraft='F'";    //SDP-13487 - Do not check duplicate in Draft Report. Because Multiple draft records having empty entry no.
        list = executeQuery( q, new Object[]{orderno, companyid});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject getContractCount(String orderno, String companyid,boolean isEdit,String contractId) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q="";
        if (isEdit) {
            q = "from Contract where contractNumber=? and company.companyID=? and ID!=?";
            list = executeQuery( q, new Object[]{orderno, companyid,contractId});
        } else {
            q = "from Contract where contractNumber=? and company.companyID=?";
            list = executeQuery( q, new Object[]{orderno, companyid});
        }
        
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject getContractDates(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        Date endDate = null;
        String Contractid = "";
        String condition = "";
        ArrayList params = new ArrayList();

        Contractid = (String) dataMap.get("contractid");
        params.add(Contractid);

        if (dataMap.containsKey("enddate") && dataMap.get("enddate") != null) {
            endDate = (Date) dataMap.get("enddate");
            condition = " and Date(enddate) >= Date(?) ";
            params.add(endDate);
        }

        DateFormat df = (DateFormat) dataMap.get(Constants.df);

        String q = "select id from contractdates where contract=? " + condition;
        list = executeSQLQuery( q, params.toArray());
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject deleteContractDates(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String Contractid = "";
        ArrayList params = new ArrayList();

        Contractid = (String) dataMap.get("contractid");
        params.add(Contractid);

        String q = "delete from contractdates where contract=?";
        count = executeSQLUpdate( q, params.toArray());
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getContractInvoice(Contract contract) throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "select invoice from invoicecontractmapping where contract=?";
            list = executeSQLQuery( query, new Object[]{contract.getID()});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getVendorDnPayment:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getContractDO(Contract contract) throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "select docontractmapping.deliveryorder,donumber from docontractmapping inner join deliveryorder on deliveryorder.id=docontractmapping.deliveryorder where contract=?";
            list = executeSQLQuery( query, new Object[]{contract.getID()});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getContractDO:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getContractStrtendDates(String contract) throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "select startdate,enddate from contractdates where contract=? order by enddate desc limit 1";
            list = executeSQLQuery( query, new Object[]{contract});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getVendorDnPayment:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject saveContractDates(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            ContractDates contractDates = new ContractDates();
            if (dataMap.containsKey("enddate") && dataMap.get("enddate") != null && !dataMap.get("enddate").toString().equals("")) {
                contractDates.setEnddate((Date) dataMap.get("enddate"));
            }
            if (dataMap.containsKey("startdate") && dataMap.get("startdate") != null && !dataMap.get("startdate").toString().equals("")) {
                contractDates.setStartdate((Date) dataMap.get("startdate"));
            }
            if (dataMap.containsKey("contractid")) {
                Contract contract = dataMap.get("contractid") == null ? null : (Contract) get(Contract.class, (String) dataMap.get("contractid"));
                contractDates.setContract(contract);
            }
            saveOrUpdate(contractDates);
            list.add(contractDates);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.saveContractDates : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public int checkConsignmentApprovalRules(String ruleId, String requestorid, String warehouseid, String locations, String approverids, String companyid) throws ServiceException {
        int flag = -1;
        try {
            int count = -1;
            int locCount = -1;
            String[] locationids = null;
            if (!StringUtil.isNullOrEmpty(locations)) {
                locationids = locations.split(",");
                locCount = locationids.length;
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            filter_names.add("company.companyID");
            filter_params.add(companyid);
            filter_names.add("requester.userID");
            filter_params.add(requestorid);
            filter_names.add("inventoryWarehouse.id");
            filter_params.add(warehouseid);
            requestParams.put(Constants.filterNamesKey, filter_names);
            requestParams.put(Constants.filterParamsKey, filter_params);
            KwlReturnObject result = getConsignmentApprovalRules(requestParams);
            List<ConsignmentRequestApprovalRule> list = result.getEntityList();
//            int count = result.getRecordTotalCount();

            Set<InventoryLocation> allLocationSet = new HashSet<InventoryLocation>();
            for (ConsignmentRequestApprovalRule consignmentRequestApprovalRule : list) {
                if (!consignmentRequestApprovalRule.getID().equals(ruleId)) {
                    Set<InventoryLocation> locationSet = consignmentRequestApprovalRule.getInventoryLocationsSet();
                    allLocationSet.addAll(locationSet);
                }
            }
            boolean locationContains = false;

            for (String locationId : locationids) {
                InventoryLocation il = getInventoryLocationById(locationId);
                if (allLocationSet.contains(il)) {
                    locationContains = true;
                    locCount--;
                }
            }
            if (count == 0 && locCount == 0) {
                //Exact duplicate
                flag = 0;
            } else if (locationContains) {
                //User has removed existing value
                flag = 3;
            } else {
                // New record
                flag = -1;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("SaveConsignmentApprovalRules : " + ex.getMessage(), ex);
        }
        return flag;
    }

    private InventoryLocation getInventoryLocationById(String locationId) {
        return (InventoryLocation) get(InventoryLocation.class, locationId);
    }

    @Override
    public void saveConsignmentApprovalRules(ConsignmentRequestApprovalRule approvalRule, String id, String locations) throws ServiceException {
        try {
            if (!StringUtil.isNullOrEmpty(locations)) {
                String query = "delete from ConsignmentRequestLocationMapping c where c.consignmentrequest.ID = ?";
                int numRows = executeUpdate( query, new Object[]{id});
                saveOrUpdate(approvalRule);
                String[] locationids = locations.split(",");
                for (String locationid : locationids) {
                    ConsignmentRequestLocationMapping requestLocationMapping = new ConsignmentRequestLocationMapping();
                    requestLocationMapping.setConsignmentrequest(approvalRule);
                    InventoryLocation locationObj = (InventoryLocation) get(InventoryLocation.class, locationid);
                    requestLocationMapping.setInventorylocation(locationObj);

                    save(requestLocationMapping);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("SaveConsignmentApprovalRules : " + ex.getMessage(), ex);
        }
    }

    @Override
    public KwlReturnObject getConsignmentApprovalRules(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from ConsignmentRequestApprovalRule";
        return buildNExecuteQuery( query, requestParams);
    }

    @Override
    public KwlReturnObject getConsignmentRequestLocationMapping(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from ConsignmentRequestLocationMapping";
        return buildNExecuteQuery( query, requestParams);
    }
    
    @Override
    public KwlReturnObject getConsignmentRequestApproverList(String ruleid) throws ServiceException {
        String query = "select crm.approver from ConsignmentRequestApproverMapping crm where crm.consignmentRequestRule.ID= ?  " ;//and crm.approver.userID != '0796ad1c-b33c-11e3-986d-7777670e1453'
        List list = executeQuery( query, new Object[]{ruleid});
        int count=list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    @Override
    public KwlReturnObject deleteConsignmentRequestApproverMapping(String ruleid) throws ServiceException {
        String delQuery = "Delete from ConsignmentRequestApproverMapping  crm where crm.consignmentRequestRule.ID = ?";
        int numRows = executeUpdate( delQuery, new Object[]{ruleid});
        return new KwlReturnObject(true, "Consignment user mapping deleted successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject deleteConsignmentApprovalRules(String companyid, String ruleid) throws ServiceException {
        String query = "delete from ConsignmentRequestLocationMapping c where c.consignmentrequest.ID = ?";
        int numRows = executeUpdate( query, new Object[]{ruleid});
        
        query = "delete from ConsignmentRequestApproverMapping c where c.consignmentRequestRule.ID = ?";
        numRows = executeUpdate( query, new Object[]{ruleid});

        String delQuery = "delete from ConsignmentRequestApprovalRule c where c.company.companyID = ? and ID = ?";
        numRows = executeUpdate( delQuery, new Object[]{companyid, ruleid});
        return new KwlReturnObject(true, "Consignment rule deleted successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject saveRepeateSalesOrderInfo(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            RepeatedSalesOrder rSalesOrder = new RepeatedSalesOrder();
            if (dataMap.containsKey("id")) {
                rSalesOrder = (RepeatedSalesOrder) get(RepeatedSalesOrder.class, (String) dataMap.get("id"));
            }

            if (dataMap.containsKey("intervalType")) {
                rSalesOrder.setIntervalType((String) dataMap.get("intervalType"));
            }
            if (dataMap.containsKey("intervalUnit")) {
                rSalesOrder.setIntervalUnit((Integer) dataMap.get("intervalUnit"));
            }
            if (dataMap.containsKey("NoOfpost")) {
                rSalesOrder.setNoOfSOpost(((Integer) dataMap.get("NoOfpost")));
            }
            if (dataMap.containsKey("NoOfRemainpost")) {
                rSalesOrder.setNoOfRemainSOpost((Integer) dataMap.get("NoOfRemainpost"));
            }
            if (dataMap.containsKey("startDate")) {
                rSalesOrder.setStartDate((Date) dataMap.get("startDate"));
            }
            if (dataMap.containsKey("nextDate")) {
                rSalesOrder.setNextDate((Date) dataMap.get("nextDate"));
            }
            if (dataMap.containsKey("expireDate")) {
                rSalesOrder.setExpireDate((Date) dataMap.get("expireDate"));
            }
            if (dataMap.containsKey("isactivate")) {
                rSalesOrder.setIsActivate((Boolean)dataMap.get("isactivate"));
            } 
            if (dataMap.containsKey("ispendingapproval")) {
                rSalesOrder.setIspendingapproval((Boolean)dataMap.get("ispendingapproval"));
            }
            if (dataMap.containsKey("approver")) {
                rSalesOrder.setApprover((String) dataMap.get("approver"));
            } else {
                rSalesOrder.setApprover("");
            }
            if (dataMap.containsKey("prevDate")) {
                rSalesOrder.setPrevDate((Date) dataMap.get("prevDate"));
            }

            saveOrUpdate(rSalesOrder);
            list.add(rSalesOrder);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveRepeateInvoiceInfo : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public int saveCustomDataForRecurringSO(String New_SO_ID, String Old_SO_ID, boolean SO_OR_SOD) throws ServiceException {
        int NoOFRecords = 0;
        String query = "";
        String conditionSQL = "";
        String selectColumns = "";
        String tableName = "";
        String column = "";
        try {
            for (int i = Constants.Custom_Column_Combo_start + 1; i <= (Constants.Custom_Column_Combo_start + Constants.Custom_Column_Combo_limit); i++) {
                conditionSQL = conditionSQL + "salcust.col" + i + ",";
                selectColumns = selectColumns + "col" + i + ",";
            }
            
            for (int i = Constants.Custom_Column_Master_start + 1; i <= (Constants.Custom_Column_Master_start + Constants.Custom_Column_Master_limit); i++) {
                conditionSQL = conditionSQL + "salcust.col" + i + ",";
                selectColumns = selectColumns + "col" + i + ",";
            }

            for (int i = Constants.Custom_Column_User_start + 1; i <= (Constants.Custom_Column_User_start + Constants.Custom_Column_User_limit); i++) {
                conditionSQL = conditionSQL + "salcust.col" + i + ",";
                selectColumns = selectColumns + "col" + i + ",";
            }

            for (int i = Constants.Custom_Column_Normal_start + 1; i <= (Constants.Custom_Column_Normal_start + Constants.Custom_Column_Normal_limit); i++) {
                conditionSQL = conditionSQL + "salcust.col" + i + ",";
                selectColumns = selectColumns + "col" + i + ",";
            }
            
            for (int i = Constants.Custom_Column_Check_start + 1; i <= (Constants.Custom_Column_Check_start + Constants.Custom_Column_Check_limit); i++) {
                conditionSQL = conditionSQL + "salcust.col" + i + ",";
                selectColumns = selectColumns + "col" + i + ",";
            }

            conditionSQL += "salcust.company,salcust.moduleId,salcust.deleted";
            selectColumns += "company,moduleId,deleted";
            
            if (SO_OR_SOD) {
                tableName = "salesordercustomdata";
                column = "salcust.soID";
                selectColumns = "soID," + selectColumns;
            }
            if (!SO_OR_SOD) {
                tableName = "salesorderdetailcustomdata";
                column = "salcust.soDetailID";
                selectColumns = "soDetailID," + selectColumns;
            }
            query += "insert into " + tableName + " ("+selectColumns+") (select '" + New_SO_ID + "'," + conditionSQL + " from " + tableName + " as salcust where " + column + "='" + Old_SO_ID + "')";
            NoOFRecords = executeSQLUpdate( query, new String[]{});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("savSalesOrderCustomData : " + ex.getMessage(), ex);
        }
        return NoOFRecords;

    }

    public KwlReturnObject updateSalesOrder(JSONObject json, HashSet details) throws ServiceException {
        List list = new ArrayList();
        try {
            String SalesOrderid = json.getString("SOid");
            SalesOrder salesOrder = (SalesOrder) get(SalesOrder.class, SalesOrderid);
            if (salesOrder != null) {
                if (json.has("repeateid")) {
                    salesOrder.setRepeateSO((RepeatedSalesOrder) get(RepeatedSalesOrder.class, json.getString("repeateid")));
                }
                if (json.has("parentid")) {
                    SalesOrder salesorder = (SalesOrder) get(SalesOrder.class, json.getString("parentid"));
                    salesOrder.setParentSO(salesorder);
                }
                if (json.has("salesordercustomdataref")) {
                    SalesOrderCustomData cmp = (json.get("salesordercustomdataref") == null ? null : (SalesOrderCustomData) get(SalesOrderCustomData.class, (String) json.get("salesordercustomdataref")));
                    salesOrder.setSoCustomData(cmp);
                }
                if (details != null) {
                    if (!details.isEmpty()) {
                        salesOrder.setRows(details);
                    }
                }
                if (json.has(IntegrationConstants.totalShippingCost)) {
                    double totalShippingCost = (json.get(IntegrationConstants.totalShippingCost) == null ? null : json.optDouble(IntegrationConstants.totalShippingCost));
                    salesOrder.setTotalShippingCost(totalShippingCost);
                }

                saveOrUpdate(salesOrder);
            }
            list.add(SalesOrderid);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("accInvoiceImpl.updateInvoice:" + ex, ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject saveSalesOrder(HashMap<String, Object> dataMap) throws ServiceException,AccountingException{
        List list = new ArrayList();
        String companyid = "";
        try {
            String soid = (String) dataMap.get("id");
            if (dataMap.containsKey("companyid")) {
                companyid = (String) dataMap.get("companyid");
            }
            SalesOrder salesOrder = new SalesOrder();
            if (StringUtil.isNullOrEmpty(soid)) {
                salesOrder.setDeleted(false);
                if (dataMap.containsKey("createdby")) {
                    User createdby = dataMap.get("createdby") == null ? null : (User) get(User.class, (String) dataMap.get("createdby"));
                    salesOrder.setCreatedby(createdby);
                }
                if (dataMap.containsKey("modifiedby")) {
                    User modifiedby = dataMap.get("modifiedby") == null ? null : (User) get(User.class, (String) dataMap.get("modifiedby"));
                    salesOrder.setModifiedby(modifiedby);
                }
                if (dataMap.containsKey("createdon")) {
                    salesOrder.setCreatedon((Long) dataMap.get("createdon"));
                }
                if (dataMap.containsKey("updatedon")) {
                    salesOrder.setUpdatedon((Long) dataMap.get("updatedon"));
                }
            } else {
                salesOrder = (SalesOrder) get(SalesOrder.class, soid);
                salesOrder.setDeleted(false);
                if (dataMap.containsKey("modifiedby")) {
                    User modifiedby = dataMap.get("modifiedby") == null ? null : (User) get(User.class, (String) dataMap.get("modifiedby"));
                    salesOrder.setModifiedby(modifiedby);
                }
                if (dataMap.containsKey("updatedon")) {
                    salesOrder.setUpdatedon((Long) dataMap.get("updatedon"));
                }
            }

            if (dataMap.containsKey(Constants.SEQFORMAT)) {
                salesOrder.setSeqformat((SequenceFormat) get(SequenceFormat.class, (String) dataMap.get(Constants.SEQFORMAT)));
            }
            if (dataMap.containsKey(Constants.SEQNUMBER) && dataMap.get(Constants.SEQNUMBER)!=null && !StringUtil.isNullOrEmpty(dataMap.get(Constants.SEQNUMBER).toString())) {
                salesOrder.setSeqnumber(Integer.parseInt(dataMap.get(Constants.SEQNUMBER).toString()));
            }

            if (dataMap.containsKey(Constants.DATEPREFIX) && dataMap.get(Constants.DATEPREFIX) != null) {
                salesOrder.setDatePreffixValue((String) dataMap.get(Constants.DATEPREFIX));
            }
            if (dataMap.containsKey(Constants.DATEAFTERPREFIX) && dataMap.get(Constants.DATEAFTERPREFIX) != null) {
                salesOrder.setDateAfterPreffixValue((String) dataMap.get(Constants.DATEAFTERPREFIX));
            }
            if (dataMap.containsKey(Constants.DATESUFFIX) && dataMap.get(Constants.DATESUFFIX) != null) {
                salesOrder.setDateSuffixValue((String) dataMap.get(Constants.DATESUFFIX));
            }
            if (dataMap.containsKey("isOpeningBalanceOrder") && dataMap.get("isOpeningBalanceOrder") != null) {
                salesOrder.setIsOpeningBalanceSO((Boolean) dataMap.get("isOpeningBalanceOrder"));
                if ((Boolean) dataMap.get("isOpeningBalanceOrder")) {
                    salesOrder.setApprovestatuslevel(11);
                }
            }


            if (dataMap.containsKey("islockQuantityflag") && dataMap.get("islockQuantityflag") != null) {
                salesOrder.setLockquantityflag((Boolean) dataMap.get("islockQuantityflag"));
            }

            if (dataMap.containsKey("entrynumber")) {
                salesOrder.setSalesOrderNumber((String) dataMap.get("entrynumber"));
            }
            if (dataMap.containsKey("autogenerated")) {
                salesOrder.setAutoGenerated((Boolean) dataMap.get("autogenerated"));
            }
            if (dataMap.containsKey("memo")) {
                salesOrder.setMemo((String) dataMap.get("memo"));
            }
            if (dataMap.containsKey("perDiscount")) {
                salesOrder.setPerDiscount((Boolean) dataMap.get("perDiscount"));
            }
            if (dataMap.containsKey("discount")) {
                salesOrder.setDiscount((Double) dataMap.get("discount"));
            }
            if (dataMap.containsKey("posttext")) {
                salesOrder.setPostText((String) dataMap.get("posttext"));
            }
            if (dataMap.containsKey("formtypeid")) {
                salesOrder.setFormtype((String) dataMap.get("formtypeid"));
            }
            if (dataMap.containsKey("customerporefno") && dataMap.get("customerporefno") != null) {
                salesOrder.setCustomerPORefNo(dataMap.get("customerporefno").toString());
            }

            if (dataMap.containsKey("profitMargin") && dataMap.get("profitMargin") != null) {
                salesOrder.setTotalProfitMargin(Double.parseDouble((String) dataMap.get("profitMargin")));
            }

            if (dataMap.containsKey("profitMarginPercent") && dataMap.get("profitMarginPercent") != null) {
                salesOrder.setTotalProfitMarginPercent(Double.parseDouble((String) dataMap.get("profitMarginPercent")));
            }
            if (dataMap.containsKey("totalamountinbase") && dataMap.get("totalamountinbase") != null) {
                salesOrder.setTotalamountinbase(authHandler.round(Double.valueOf(dataMap.get("totalamountinbase").toString()), companyid));
            }

            if (dataMap.containsKey("totalamount")  && dataMap.get("totalamount") != null) { // SO amount
                salesOrder.setTotalamount(authHandler.round(Double.valueOf(dataMap.get("totalamount").toString()), companyid));
            }
              
            if (dataMap.containsKey(Constants.roundingadjustmentamountinbase) && dataMap.get(Constants.roundingadjustmentamountinbase) != null) {
                salesOrder.setRoundingadjustmentamountinbase(Double.valueOf(dataMap.get(Constants.roundingadjustmentamountinbase).toString()));
            }

            if (dataMap.containsKey(Constants.roundingadjustmentamount) && dataMap.get(Constants.roundingadjustmentamount) != null) { // quotation amount
                salesOrder.setRoundingadjustmentamount(Double.valueOf(dataMap.get(Constants.roundingadjustmentamount).toString()));
            }
            if (dataMap.containsKey(Constants.IsRoundingAdjustmentApplied) && dataMap.get(Constants.IsRoundingAdjustmentApplied) != null) {  // If New GST Appliled
                salesOrder.setIsRoundingAdjustmentApplied((Boolean) dataMap.get(Constants.IsRoundingAdjustmentApplied));
            }
            
            if (dataMap.containsKey("discountinbase")  && dataMap.get("discountinbase") != null) { // Discount in Base
                salesOrder.setDiscountinbase(authHandler.round(Double.valueOf(dataMap.get("discountinbase").toString()), companyid));
            }
            
            if (dataMap.containsKey("totallineleveldiscount")  && dataMap.get("totallineleveldiscount") != null) { // Discount
                salesOrder.setTotallineleveldiscount(authHandler.round(Double.valueOf(dataMap.get("totallineleveldiscount").toString()), companyid));
            }
            
            if (dataMap.containsKey("billto")) {
                if (dataMap.get("billto") != null) {
                    salesOrder.setBillTo((String) dataMap.get("billto"));
                }
            }
            if (dataMap.containsKey("shipaddress")) {
                if (dataMap.get("shipaddress") != null) {
                    salesOrder.setShipTo((String) dataMap.get("shipaddress"));
                }
            }
            if (dataMap.containsKey("customerid")) {
                Customer customer = dataMap.get("customerid") == null ? null : (Customer) get(Customer.class, (String) dataMap.get("customerid"));
                salesOrder.setCustomer(customer);
            }
            if (dataMap.containsKey("orderdate")) {
                salesOrder.setOrderDate((Date) dataMap.get("orderdate"));
            }
            if (dataMap.containsKey("duedate")) {
                salesOrder.setDueDate((Date) dataMap.get("duedate"));
            }
            if (dataMap.containsKey("shipdate")) {
                salesOrder.setShipdate((Date) dataMap.get("shipdate"));
            }
            if (dataMap.containsKey("shipvia")) {
                salesOrder.setShipvia((String) dataMap.get("shipvia"));
            }
            if (dataMap.containsKey("fob")) {
                salesOrder.setFob((String) dataMap.get("fob"));
            }
            if (dataMap.containsKey("taxid")) {
                Tax tax = dataMap.get("taxid") == null ? null : (Tax) get(Tax.class, (String) dataMap.get("taxid"));
                salesOrder.setTax(tax);
            }
            if (dataMap.containsKey("costCenterId")) {
                CostCenter costCenter = dataMap.get("costCenterId") == null ? null : (CostCenter) get(CostCenter.class, (String) dataMap.get("costCenterId"));
                salesOrder.setCostcenter(costCenter);
            } else {
                salesOrder.setCostcenter(null);
            }
            if (dataMap.containsKey("companyid")) {
                Company company = dataMap.get("companyid") == null ? null : (Company) get(Company.class, (String) dataMap.get("companyid"));
                salesOrder.setCompany(company);
            }
            if (dataMap.containsKey("parentid")) {
                SalesOrder salesorder = dataMap.get("parentid") == null ? null : (SalesOrder) get(SalesOrder.class, (String) dataMap.get("parentid"));
                salesOrder.setParentSO(salesorder);
            }
            if (dataMap.containsKey("sodetails")) {
                if (dataMap.get("sodetails") != null) {
                    salesOrder.setRows((Set<SalesOrderDetail>) dataMap.get("sodetails"));
                }
            }
            if (dataMap.containsKey("currencyid")) {
                salesOrder.setCurrency((KWLCurrency) get(KWLCurrency.class, (String) dataMap.get("currencyid")));
            }
            if (dataMap.containsKey("shipLength")) {
                salesOrder.setShiplength(Double.parseDouble((String) dataMap.get("shipLength")));
            }

            if (dataMap.containsKey("invoicetype")) {
                salesOrder.setInvoicetype((String) dataMap.get("invoicetype"));
            }
            if (dataMap.containsKey("isfavourite")) {
                if (dataMap.get("isfavourite") != null) {
                    salesOrder.setFavourite(Boolean.parseBoolean(dataMap.get("isfavourite").toString()));
                }
            }

            if (dataMap.containsKey("isLeaseFixedAsset") && dataMap.get("isLeaseFixedAsset") != null) {
                if ((Boolean) dataMap.get("isLeaseFixedAsset")) {
                    salesOrder.setLeaseOrMaintenanceSO(1);
                }
            }

            if (dataMap.containsKey("isLinkedFromMaintenanceNumber") && dataMap.get("isLinkedFromMaintenanceNumber") != null && !(Boolean) dataMap.get("isLeaseFixedAsset")) {  //maintainace is only for normal SO so not for Lease SO
                if ((Boolean) dataMap.get("isLinkedFromMaintenanceNumber")) {
                    salesOrder.setLeaseOrMaintenanceSO(2);

                    Maintenance maintenance = (Maintenance) get(Maintenance.class, (String) dataMap.get("maintenanceId"));
                    salesOrder.setMaintenance(maintenance);
                } else {
                    salesOrder.setMaintenance(null);
                    salesOrder.setLeaseOrMaintenanceSO(0);
                }
            }

            if (dataMap.containsKey("IsReplacementSO") && dataMap.get("IsReplacementSO") != null) {
                salesOrder.setIsReplacementSO((Boolean) dataMap.get("IsReplacementSO"));
            }

            if (dataMap.containsKey(Constants.MARKED_PRINTED)) {
                if (dataMap.get(Constants.MARKED_PRINTED) != null) {
                    salesOrder.setPrinted(Boolean.parseBoolean(dataMap.get(Constants.MARKED_PRINTED).toString()));
                }
            }
            if (dataMap.containsKey("gstIncluded") && dataMap.get("gstIncluded") != null) {
                salesOrder.setGstIncluded((Boolean) dataMap.get("gstIncluded"));
            }
            if (dataMap.containsKey("salesordercustomdataref")) {
                SalesOrderCustomData cmp = (dataMap.get("salesordercustomdataref") == null ? null : (SalesOrderCustomData) get(SalesOrderCustomData.class, (String) dataMap.get("salesordercustomdataref")));
                salesOrder.setSoCustomData(cmp);
            }
            if (dataMap.containsKey("salesPerson")) {
                MasterItem mi = (dataMap.get("salesPerson") == null ? null : (MasterItem) get(MasterItem.class, (String) dataMap.get("salesPerson")));
                salesOrder.setSalesperson(mi);
            }
            if (dataMap.containsKey("termid")) {
                Term term = (dataMap.get("termid") == null ? null : (Term) get(Term.class, (String) dataMap.get("termid")));
                salesOrder.setTerm(term);
            }
            if (dataMap.containsKey("billshipAddressid")) {
                BillingShippingAddresses bsa = dataMap.get("billshipAddressid") == null ? null : (BillingShippingAddresses) get(BillingShippingAddresses.class, (String) dataMap.get("billshipAddressid"));
                salesOrder.setBillingShippingAddresses(bsa);
            }
            if (dataMap.containsKey("contractid")) {
                Contract contract = dataMap.get("contractid") == null ? null : (Contract) get(Contract.class, (String) dataMap.get("contractid"));
                salesOrder.setContract(contract);
            }
            if (dataMap.containsKey("isConsignment")) {

                salesOrder.setIsconsignment((Boolean) dataMap.get("isConsignment"));
                if ((Boolean) dataMap.get("isConsignment")) {
                    salesOrder.setLeaseOrMaintenanceSO(3);  //for consignment so it should be 3
                }
            }
            if (dataMap.containsKey("isMRPSalesOrder")) {
                salesOrder.setIsMRPSalesOrder((Boolean) dataMap.get("isMRPSalesOrder"));
            }
            
            if (dataMap.containsKey("isdropshipchecked")) {
                salesOrder.setIsDropshipDocument((Boolean) dataMap.get("isdropshipchecked"));
                salesOrder.setLinkflag(1);/*--Restricting dropship SO for linking in DO-- */
            }
             
            if (dataMap.containsKey("isJobWorkOrderReciever")) {
                salesOrder.setIsJobWorkOrder((Boolean) dataMap.get("isJobWorkOrderReciever"));
            }
       
            if (dataMap.containsKey("externalCurrencyRate")) {
                salesOrder.setExternalCurrencyRate((Double) dataMap.get("externalCurrencyRate"));
            }
            if (dataMap.containsKey(Constants.termsincludegst)) {
                salesOrder.setTermsincludegst((Boolean) dataMap.get(Constants.termsincludegst));
            }            
            if (dataMap.containsKey("custWarehouse") && dataMap.get("custWarehouse") != null) {
                InventoryWarehouse warehouse = new InventoryWarehouse();
                warehouse = (InventoryWarehouse) get(InventoryWarehouse.class, (String) dataMap.get("custWarehouse"));
                if (warehouse != null) {
                    salesOrder.setCustWarehouse(warehouse);
                }
            }
            
            if (dataMap.containsKey("autoapproveflag") && dataMap.get("autoapproveflag") != null) {
                salesOrder.setAutoapproveflag((Boolean) dataMap.get("autoapproveflag"));
            }
            
            if (dataMap.containsKey("requestWarehouse") && dataMap.get("requestWarehouse") != null) {
                InventoryWarehouse warehouse = (InventoryWarehouse) get(InventoryWarehouse.class, (String) dataMap.get("requestWarehouse"));
                if (warehouse != null) {
                    salesOrder.setRequestWarehouse(warehouse);
                }
            }
            
            if (dataMap.containsKey("requestLocation") && dataMap.get("requestLocation") != null) {
                InventoryLocation location = (InventoryLocation) get(InventoryLocation.class, (String) dataMap.get("requestLocation"));
                if (location != null) {
                    salesOrder.setRequestLocation(location);
                }
            }          
            if (dataMap.containsKey("movementtype") && dataMap.get("movementtype") != null) {
                MasterItem movementType = null;
                movementType = (MasterItem) get(MasterItem.class, (String) dataMap.get("movementtype"));
                if (movementType != null) {
                    salesOrder.setMovementType(movementType);
                }
            }
            if (dataMap.containsKey("todate") && dataMap.get("todate") != null) {
                salesOrder.setTodate((Date) dataMap.get("todate"));
            }
            if (dataMap.containsKey("fromdate") && dataMap.get("fromdate") != null) {
                salesOrder.setFromdate((Date) dataMap.get("fromdate"));
            }
            salesOrder.setTemplateid((Projreport_Template) get(Projreport_Template.class, Constants.HEADER_IMAGE_TEMPLATE_ID));
            if (dataMap.containsKey(Constants.generatedSource)&& dataMap.get(Constants.generatedSource) != null) {
                salesOrder.setGeneratedSource((Integer) dataMap.get(Constants.generatedSource));
            }
            if (dataMap.containsKey("gstapplicable") && dataMap.get("gstapplicable") != null) {  // If New GST Appliled
                salesOrder.setIsIndGSTApplied((Boolean) dataMap.get("gstapplicable"));
            }
            // Set isDraft flag in sales order
            if (dataMap.containsKey("isDraft") && dataMap.get("isDraft") != null) {  // If Save As Draft
                salesOrder.setIsDraft((Boolean) dataMap.get("isDraft"));
            }
            if (dataMap.containsKey(Constants.isApplyTaxToTerms) && dataMap.get(Constants.isApplyTaxToTerms) != null) {  // If Save As Draft
                salesOrder.setApplyTaxToTerms((Boolean) dataMap.get(Constants.isApplyTaxToTerms));
            }
            if (dataMap.containsKey(Constants.RCMApplicable) && dataMap.get(Constants.RCMApplicable) != null) { 
                salesOrder.setRcmapplicable((Boolean) dataMap.get(Constants.RCMApplicable));
            }
            if (dataMap.containsKey(Constants.isMerchantExporter) && dataMap.get(Constants.isMerchantExporter) != null) { 
                salesOrder.setIsMerchantExporter((Boolean) dataMap.get(Constants.isMerchantExporter));
            }
            if (dataMap.containsKey("isLinkedPOBlocked") && dataMap.get("isLinkedPOBlocked") != null) {
                salesOrder.setLinkedPOBlocked((Boolean) dataMap.get("isLinkedPOBlocked"));
            }
            saveOrUpdate(salesOrder);
            list.add(salesOrder);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveSalesOrder : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject saveSalesOrderDetails(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String sodid = (String) dataMap.get("id");

            SalesOrderDetail salesOrderDetail = new SalesOrderDetail();
            if (!StringUtil.isNullOrEmpty(sodid)) {
                salesOrderDetail = (SalesOrderDetail) get(SalesOrderDetail.class, sodid);
            }
            
            SalesOrder salesOrder=null;
            if (dataMap.containsKey("soid")) {
                salesOrder = dataMap.get("soid") == null ? null : (SalesOrder) get(SalesOrder.class, (String) dataMap.get("soid"));
                salesOrderDetail.setSalesOrder(salesOrder);
            }
            if (dataMap.containsKey("srno")) {
                salesOrderDetail.setSrno((Integer) dataMap.get("srno"));
            }
            if (dataMap.containsKey("rate")) {
                salesOrderDetail.setRate((Double) dataMap.get("rate"));
            }
            if (dataMap.containsKey("rateIncludingGst")) {
                salesOrderDetail.setRateincludegst((Double) dataMap.get("rateIncludingGst"));
            }

            if (dataMap.containsKey("quantity")) {
                salesOrderDetail.setQuantity((Double) dataMap.get("quantity"));
            }
            if (dataMap.containsKey("approvalquantity")) {
                salesOrderDetail.setApprovedQuantity((Double) dataMap.get("approvalquantity"));
            }
            if (dataMap.containsKey("uomid")) {
                salesOrderDetail.setUom((UnitOfMeasure) get(UnitOfMeasure.class, dataMap.get("uomid").toString()));
            }
            if (dataMap.containsKey("baseuomquantity") && dataMap.get("baseuomquantity") != null && dataMap.get("baseuomquantity") != "") {
                salesOrderDetail.setBaseuomquantity((Double) dataMap.get("baseuomquantity"));
//            } else {
//                if (dataMap.containsKey("quantity")) {
//                    salesOrderDetail.setBaseuomquantity((Double) dataMap.get("quantity"));
//                }
            }
            if (dataMap.containsKey("baseuomrate") && dataMap.get("baseuomrate") != null && dataMap.get("baseuomrate") != "") {
                salesOrderDetail.setBaseuomrate((Double) dataMap.get("baseuomrate"));
//            } else {
//                salesOrderDetail.setBaseuomrate(1);
            }if (dataMap.containsKey("balanceqty") && dataMap.get("balanceqty") != null && dataMap.get("balanceqty") != "") {
                salesOrderDetail.setBalanceqty((Double) dataMap.get("balanceqty"));
            }

            if (dataMap.containsKey("lockquantity") && dataMap.get("lockquantity") != null && dataMap.get("lockquantity") != "" && salesOrder!=null && salesOrder.isAutoapproveflag()==false) {
                salesOrderDetail.setLockquantity((Double) dataMap.get("lockquantity"));
            }

            if (dataMap.containsKey("lockquantitydue") && dataMap.get("lockquantitydue") != null && dataMap.get("lockquantitydue") != "" && salesOrder!=null && salesOrder.isAutoapproveflag()==false) {
                salesOrderDetail.setLockquantitydue((Double) dataMap.get("lockquantitydue"));
            }

            if (dataMap.containsKey("lockQuantityInSelectedUOM") && dataMap.get("lockQuantityInSelectedUOM") != null && dataMap.get("lockQuantityInSelectedUOM") != "" && salesOrder!=null && salesOrder.isAutoapproveflag()==false) {
                salesOrderDetail.setLockQuantityInSelectedUOM((Double) dataMap.get("lockQuantityInSelectedUOM"));
            }

            if (dataMap.containsKey("remark")) {
                salesOrderDetail.setRemark(StringUtil.DecodeText(StringUtil.isNullOrEmpty((String) dataMap.get("remark")) ? "" : (String) dataMap.get("remark")));
            }
            if (dataMap.containsKey("dependentType")) {
                salesOrderDetail.setDependentType((String) dataMap.get("dependentType"));
            }
            if (dataMap.containsKey("inouttime")) {
                salesOrderDetail.setInouttime((String) dataMap.get("inouttime"));
            }
            if (dataMap.containsKey("showquantity")) {
                salesOrderDetail.setShowquantity((String) dataMap.get("showquantity"));
            }
            if (dataMap.containsKey("bomid")) {
                BOMDetail bomObj = (dataMap.get("bomid") == null ? null : (BOMDetail) get(BOMDetail.class, (String) dataMap.get("bomid")));
                salesOrderDetail.setBomcode(bomObj);
            }
            if (dataMap.containsKey("desc")) {
                salesOrderDetail.setDescription((String) dataMap.get("desc"));
            }
            if (dataMap.containsKey("discount")) {
                salesOrderDetail.setDiscount((Double) dataMap.get("discount"));
            }
            if (dataMap.containsKey("discountispercent")) {
                salesOrderDetail.setDiscountispercent((Integer) dataMap.get("discountispercent"));
            } 
//            else {
//                salesOrderDetail.setDiscountispercent(1);
//            }
            if (dataMap.containsKey("productid")) {
                Product product = dataMap.get("productid") == null ? null : (Product) get(Product.class, (String) dataMap.get("productid"));
                salesOrderDetail.setProduct(product);
            }
            if (dataMap.containsKey("companyid")) {
                Company company = dataMap.get("companyid") == null ? null : (Company) get(Company.class, (String) dataMap.get("companyid"));
                salesOrderDetail.setCompany(company);
            }
            if (dataMap.containsKey("salesordercustomdataref")) {
                SalesOrderDetailsCustomData cmp = (dataMap.get("salesordercustomdataref") == null ? null : (SalesOrderDetailsCustomData) get(SalesOrderDetailsCustomData.class, (String) dataMap.get("salesordercustomdataref")));
                salesOrderDetail.setSoDetailCustomData(cmp);
            }
            if (dataMap.containsKey("rowtaxid")) {
                Tax rowtax = (dataMap.get("rowtaxid") == null ? null : (Tax) get(Tax.class, (String) dataMap.get("rowtaxid")));
                salesOrderDetail.setTax(rowtax);
            }
            if (dataMap.containsKey("rowTaxAmount")) {
                double rowTaxAmount = (Double) dataMap.get("rowTaxAmount");
                salesOrderDetail.setRowTaxAmount(rowTaxAmount);
            }

            if (dataMap.containsKey("quotationdetailid")) {
                QuotationDetail quotationdetail = dataMap.get("quotationdetailid") == null ? null : (QuotationDetail) get(QuotationDetail.class, (String) dataMap.get("quotationdetailid"));
                salesOrderDetail.setQuotationDetail(quotationdetail);
            }
            
             if (dataMap.containsKey("sourcepurchaseorderdetailid") && dataMap.get("sourcepurchaseorderdetailid") != null) {
                salesOrderDetail.setSourcePurchaseOrderDetailsid((String) dataMap.get("sourcepurchaseorderdetailid"));
            }

            if (dataMap.containsKey("productreplacementDetailId") && dataMap.get("productreplacementDetailId") != null) {
                ProductReplacementDetail productReplacementDetail = dataMap.get("productreplacementDetailId") == null ? null : (ProductReplacementDetail) get(ProductReplacementDetail.class, (String) dataMap.get("productreplacementDetailId"));
                salesOrderDetail.setProductReplacementDetail(productReplacementDetail);
            }

            if (dataMap.containsKey("PurchaseOrderDetailID") && dataMap.get("PurchaseOrderDetailID") != null) {
                salesOrderDetail.setPurchaseorderdetailid((String) dataMap.get("PurchaseOrderDetailID"));
            }
            if (dataMap.containsKey("MRPContractDetailsID") && dataMap.get("MRPContractDetailsID") != null) {
                salesOrderDetail.setMrpcontractdetailid((String) dataMap.get("MRPContractDetailsID"));
            }
            if (dataMap.containsKey("invstoreid")) {
                salesOrderDetail.setInvstoreid((String) dataMap.get("invstoreid"));
            } else {
                salesOrderDetail.setInvstoreid("");
            }
            if (dataMap.containsKey("invlocationid")) {
                salesOrderDetail.setInvlocid((String) dataMap.get("invlocationid"));
            } else {
                salesOrderDetail.setInvlocid("");
            }
            if (dataMap.containsKey("priceSource") && dataMap.get("priceSource") != null) {
                salesOrderDetail.setPriceSource((String) dataMap.get("priceSource"));
            }
            if (dataMap.containsKey("pricingbandmasterid") && dataMap.get("pricingbandmasterid") != null) {
                salesOrderDetail.setPricingBandMasterid((String) dataMap.get("pricingbandmasterid"));
            }
            if (dataMap.containsKey("requestpendingapproval") && dataMap.get("requestpendingapproval") != null) {
                salesOrderDetail.setRequestApprovalStatus((RequestApprovalStatus) dataMap.get("requestpendingapproval"));
            }
            if (dataMap.containsKey("approver") && dataMap.get("approver") != null) {
                salesOrderDetail.setApproverSet((Set<User>) dataMap.get("approver"));
            }
            if (dataMap.containsKey("recTermAmount") && !StringUtil.isNullOrEmpty(dataMap.get("recTermAmount").toString())) {
                 double recTermAmount = Double.parseDouble(dataMap.get("recTermAmount").toString());
                salesOrderDetail.setRowtermamount(recTermAmount);
            }
            if (dataMap.containsKey("OtherTermNonTaxableAmount") && !StringUtil.isNullOrEmpty(dataMap.get("OtherTermNonTaxableAmount").toString())) {
                 double OtherTermNonTaxableAmount = Double.parseDouble(dataMap.get("OtherTermNonTaxableAmount").toString());
                salesOrderDetail.setOtherTermNonTaxableAmount(OtherTermNonTaxableAmount);
            }
            if (dataMap.containsKey("lineleveltermamount") && !StringUtil.isNullOrEmpty(dataMap.get("lineleveltermamount").toString())) {
                 double lineleveltermamount = Double.parseDouble(dataMap.get("lineleveltermamount").toString());
                salesOrderDetail.setLineLevelTermAmount(lineleveltermamount);
            }
            if (dataMap.containsKey("jobOrderItem") && dataMap.get("jobOrderItem") != null) {
                salesOrderDetail.setJobOrderItem(dataMap.get("jobOrderItem") != null ? (Boolean) dataMap.get("jobOrderItem") : false);
            }
            if (dataMap.containsKey("jobOrderItemNumber") && dataMap.get("jobOrderItemNumber") != null) {
                salesOrderDetail.setJobOrderItemNumber((String) dataMap.get("jobOrderItemNumber"));
            }
            if (dataMap.containsKey("discountjson") && dataMap.get("discountjson") != null) {
                salesOrderDetail.setDiscountJson((String) dataMap.get("discountjson"));
            }
            //set inspection template
            if (dataMap.containsKey("inspectionTemplate") && dataMap.get("inspectionTemplate") != null) {
                InspectionTemplate inspectionTemplate = dataMap.get("inspectionTemplate") == null ? null : (InspectionTemplate) get(InspectionTemplate.class, (String) dataMap.get("inspectionTemplate"));
                salesOrderDetail.setInspectionTemplate(inspectionTemplate);
            }
            //set inspection form
            if (dataMap.containsKey("inspectionFormId") && dataMap.get("inspectionFormId") != null) {
                InspectionForm inspectionForm = dataMap.get("inspectionFormId") == null ? null : (InspectionForm) get(InspectionForm.class, (String) dataMap.get("inspectionFormId"));
                salesOrderDetail.setInspectionForm(inspectionForm);
            }
            if (dataMap.containsKey(Constants.isUserModifiedTaxAmount) && dataMap.get(Constants.isUserModifiedTaxAmount) != null) {
                salesOrderDetail.setIsUserModifiedTaxAmount((boolean) dataMap.get(Constants.isUserModifiedTaxAmount));
            }
            
            saveOrUpdate(salesOrderDetail);
            list.add(salesOrderDetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveSalesOrderDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject saveSalesOrderDetailsVendorMapping(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String sodid = (String) dataMap.get("id");
            SODetailsVendorMapping soDetailsVendorMapping = null;
            if (!StringUtil.isNullOrEmpty(sodid)) {
                soDetailsVendorMapping = (SODetailsVendorMapping) get(SODetailsVendorMapping.class, sodid);
            }
            if (soDetailsVendorMapping == null) {
                soDetailsVendorMapping = new SODetailsVendorMapping();
                soDetailsVendorMapping.setID(sodid);
            }
            if (dataMap.containsKey("vendorid")) {
                Vendor vendor = dataMap.get("vendorid") == null ? null : (Vendor) get(Vendor.class, (String) dataMap.get("vendorid"));
                soDetailsVendorMapping.setVendor(vendor);
            }
            if (dataMap.containsKey("vendorunitcost")) {
                soDetailsVendorMapping.setUnitcost((Double) dataMap.get("vendorunitcost"));
            }
            if (dataMap.containsKey("vendorcurrexchangerate")) {
                soDetailsVendorMapping.setExchangerate((Double) dataMap.get("vendorcurrexchangerate"));
            }
            if (dataMap.containsKey("totalcost")) {
                soDetailsVendorMapping.setTotalcost((Double) dataMap.get("totalcost"));
            }
            saveOrUpdate(soDetailsVendorMapping);
            list.add(soDetailsVendorMapping);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveSalesOrderDetailsVendorMapping : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject saveQuotationDetailsVendorMapping(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String qdid = (String) dataMap.get("id");
            QuotationDetailsVendorMapping quotDetailsVendorMapping = null;
            if (!StringUtil.isNullOrEmpty(qdid)) {
                quotDetailsVendorMapping = (QuotationDetailsVendorMapping) get(QuotationDetailsVendorMapping.class, qdid);
            }
            if (quotDetailsVendorMapping == null) {
                quotDetailsVendorMapping = new QuotationDetailsVendorMapping();
                quotDetailsVendorMapping.setID(qdid);
            }
            if (dataMap.containsKey("vendorid")) {
                Vendor vendor = dataMap.get("vendorid") == null ? null : (Vendor) get(Vendor.class, (String) dataMap.get("vendorid"));
                quotDetailsVendorMapping.setVendor(vendor);
            }
            if (dataMap.containsKey("vendorunitcost")) {
                quotDetailsVendorMapping.setUnitcost((Double) dataMap.get("vendorunitcost"));
            }
            if (dataMap.containsKey("vendorcurrexchangerate")) {
                quotDetailsVendorMapping.setExchangerate((Double) dataMap.get("vendorcurrexchangerate"));
            }
            if (dataMap.containsKey("totalcost")) {
                quotDetailsVendorMapping.setTotalcost((Double) dataMap.get("totalcost"));
            }
            saveOrUpdate(quotDetailsVendorMapping);
            list.add(quotDetailsVendorMapping);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveQuotationDetailsVendorMapping : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject UpdateObject(Object object) throws ServiceException {
        List list = new ArrayList();
        try {
            if (object != null) {
                saveOrUpdate(object);
                list.add(object);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("UpdateObject : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
//    public KwlReturnObject  UpdateConsignmentRequestLockQuantity(SalesOrderDetail salesOrderDetail) throws ServiceException{
//        List list = new ArrayList();
//        try {
//            if(salesOrderDetail!=null){
//                saveOrUpdate(salesOrderDetail);
//                list.add(salesOrderDetail);
//            }
//        } catch (Exception ex) {
//            throw ServiceException.FAILURE("saveQuotationDetailsVendorMapping : " + ex.getMessage(), ex);
//        }
//        return new KwlReturnObject(true, null, null, list, list.size());
//    }

    public KwlReturnObject saveAssemblySubProdmapping(HashMap<String, Object> assemblyMap) throws ServiceException {
        List list = new ArrayList();
        try {
            AssemblySubProductMapping assembly = new AssemblySubProductMapping();
            if (assemblyMap.containsKey("id")) {
                assembly = assemblyMap.get("id") == null ? null : (AssemblySubProductMapping) get(AssemblySubProductMapping.class, (String) assemblyMap.get("id"));
            }
            if (assemblyMap.containsKey("productid")) {
                Product Pproduct = assemblyMap.get("productid") == null ? null : (Product) get(Product.class, (String) assemblyMap.get("productid"));
                assembly.setProduct(Pproduct);
            }
            if (assemblyMap.containsKey("subproductid")) {
                Product Sproduct = assemblyMap.get("subproductid") == null ? null : (Product) get(Product.class, (String) assemblyMap.get("subproductid"));
                assembly.setSubproducts(Sproduct);
            }
            if (assemblyMap.containsKey("quantity")) {
                assembly.setQuantity((Double) assemblyMap.get("quantity"));
            }
            save(assembly);
            list.add(assembly);
        } catch (Exception e) {
            throw ServiceException.FAILURE("saveProductAssemblyMapping : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "SubProduct Assembly mapping has been added successfully", null, list, list.size());
    }

    public KwlReturnObject getSalesOrders(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            DateFormat df = (DateFormat) request.get(Constants.df);
            String start = (String) request.get(Constants.start);
            String limit = (String) request.get(Constants.limit);
            String ss = (String) request.get(Constants.ss);
            boolean exceptFlagINV = false;
            boolean exceptFlagORD = false;
            boolean isLeaseSO = false;
            boolean isConsignment = false;
            boolean isMRPSalesOrder = false;
            boolean isJobWorkOrderReciever = false;
            boolean orderforcontract = false;
            ArrayList params = new ArrayList();
            String orderQuery="";
         
            if (request.containsKey("exceptFlagINV") && request.get("exceptFlagINV") != null) {
                exceptFlagINV = Boolean.parseBoolean((String) request.get("exceptFlagINV"));
            }
            
            if (request.containsKey("isLeaseFixedAsset") && request.get("isLeaseFixedAsset") != null) {
                isLeaseSO = (Boolean) request.get("isLeaseFixedAsset");
            }
            if (request.containsKey("isMRPSalesOrder") && request.get("isMRPSalesOrder") != null) {
                isMRPSalesOrder = (Boolean) request.get("isMRPSalesOrder");
            }
            if (request.containsKey("isJobWorkOrderReciever") && request.get("isJobWorkOrderReciever") != null) {
                isJobWorkOrderReciever = (Boolean) request.get("isJobWorkOrderReciever");
            }
            if (request.containsKey("isConsignment") && request.get("isConsignment") != null) {
                isConsignment = (Boolean) request.get("isConsignment");
                if (isConsignment) {
                    orderQuery = "order by orderDate desc,salesOrderNumber desc";
            }
            }
            if (request.containsKey("orderforcontract") && request.get("orderforcontract") != null) {
                orderforcontract = (Boolean) request.get("orderforcontract");
            }

            if (request.containsKey("exceptFlagORD") && request.get("exceptFlagORD") != null) {
                exceptFlagORD = Boolean.parseBoolean((String) request.get("exceptFlagORD"));
            }
            /*----When Generating PO from SO & add more option is true in Account preferences then  linkTransactionId is Sales Order id through which PO is being created-----*/
            String linkTransactionId = "";
            if (request.containsKey("linkTransactionId") && request.get("linkTransactionId") != null) {
                linkTransactionId = (String) request.get("linkTransactionId");
            }

            params.add((String) request.get(Constants.companyKey));
            String condition = " where deleted=false and company.companyID=?";
            if(isConsignment){
                condition += " and freeze=false ";
            }
            if (!StringUtil.isNullOrEmpty(ss)) {
                for (int i = 0; i < 3; i++) {
                    params.add("%" + ss + "%");
                }
                condition += " and ( salesOrderNumber like ? or so.memo like ? or so.customer.name like ? )";
            }
            String costCenterId = (String) request.get(CCConstants.REQ_costCenterId);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                params.add(costCenterId);
                condition += " and so.costcenter.ID=?";
            }
            String customerId = (String) request.get(CCConstants.REQ_customerId);
            if (!StringUtil.isNullOrEmpty(customerId)) {
                params.add(customerId);
                condition += " and so.customer.ID=?";
            }

            if (exceptFlagINV) {
            condition += " and ( so.linkflag = 0 or so.linkflag = 1 ) and so.isopen='T' and so.isSOClosed='F' ";
            }

            if (exceptFlagORD) {
                condition += " and ( so.linkflag = 0 or so.linkflag = 2 ) and so.isopen='T' and so.isSOClosed='F' ";
            }
            if(orderforcontract && request.containsKey("dropDown")){ // lease contract should show only open
                condition += " and so.isopen='T' ";
            } else if(orderforcontract){    
                condition += " AND so.linkflag = 0 AND so.isopen='T' AND so.isSOClosed='F' ";   //ERP-41234 : Only Partial or Fully opened SO will be available for Contract
            }
            if (request.containsKey("includingGSTFilter") && request.get("includingGSTFilter") != null) {
                condition += " and so.gstIncluded = ?";
                params.add((Boolean) request.get("includingGSTFilter"));
            }
            if (isLeaseSO) {
                condition += " and so.leaseOrMaintenanceSO=1 ";

            } else if (orderforcontract) {// return sales order for creating normal sales contrcat
                condition += " and (so.leaseOrMaintenanceSO=0) ";// only normal SOs which are not maintenance or lease so
            } else if (isConsignment) {// return sales order for creating normal sales contrcat
                condition += " and (so.isconsignment='T' and so.leaseOrMaintenanceSO=3) ";// only normal SOs which are not maintenance or lease so
            } 
//            else  if(isMRPSalesOrder){
//                condition += " and  so.isMRPSalesOrder='T' ";
//            } 
            else  if(isJobWorkOrderReciever){
                condition += " and  so.isJobWorkOrder='T' ";
            } else {
                condition += " and (so.leaseOrMaintenanceSO=0 or so.leaseOrMaintenanceSO=2)   ";
            }
            /*
            * Done, as Job Work Orders are also shown in Normal SOs List.
            */ 
            if (isJobWorkOrderReciever) {
                 condition += " and  so.isJobWorkOrder='T' ";
            } else {
                 condition += " and  so.isJobWorkOrder='F' ";
            }
            if (request.containsKey("custWarehouse") && request.get("custWarehouse") != null) {
                String custWarehouse = (String) request.get("custWarehouse");
                if (isConsignment && !StringUtil.isNullOrEmpty(custWarehouse)) {
                    params.add(custWarehouse);
                    condition += " and so.custWarehouse.id = ? ";
                }
            }
            if (request.containsKey("movementtype") && request.get("movementtype") != null) {
                String movementtype = (String) request.get("movementtype");
                if (isConsignment && !StringUtil.isNullOrEmpty(movementtype)) {
                    params.add(movementtype);
                    condition += " and so.MovementType.ID = ? ";
                }
            }
            if (orderforcontract) {
                condition += " and so.contract.ID IS NULL ";
            }
            //fetch isDraft flag from request
            boolean isDraft = false;
            if (request.containsKey(Constants.isDraft) && request.get(Constants.isDraft) != null) {
                isDraft = (Boolean) request.get(Constants.isDraft);
            }
            //Append isDraft condition in query for fetching particular transactions
            if (isDraft) {
                condition += " and so.isDraft = true ";
            } else {
                params.add(false);
                condition += " and so.isDraft = ? ";
            }
         
//            if (exceptFlagORD) {
//                condition += " and ( so.linkflag = 0 or so.linkflag = 2 ) ";
//            }
            if (request.containsKey("currencyfilterfortrans") && request.get("currencyfilterfortrans") != null) {
                String currencyfilterfortrans = (String) request.get("currencyfilterfortrans");
                if (!StringUtil.isNullOrEmpty(currencyfilterfortrans)) {
                    condition += " and so.currency.currencyID = ?";
                    params.add(currencyfilterfortrans);
                }
            }
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                condition += " and (so.orderDate >=? and so.orderDate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if (request.containsKey("billId") && request.get("billId") != null) {
                String billid = (String) request.get("billId");
                if (!StringUtil.isNullOrEmpty(billid)) {
                    if(billid.contains(",")){
                        String soids=AccountingManager.getFilterInString(billid);
                        condition += " and so.ID in "+soids;
                    } else {
                        condition += " and so.ID=? ";
                        params.add(billid);
                    }
                }
            }
            String orderBy = "";
            if (request.containsKey("dir") && request.containsKey("sort")&&!isConsignment) {
                String Col_Name = request.get("sort").toString();
                String Col_Dir = request.get("dir").toString();
                orderBy += "order by so.salesOrderNumber " + Col_Dir;

            }
              /*
             * Fetching SO in PO if SO is not disabled for PO
             */
            if (request.containsKey("requestModuleid") && request.get("requestModuleid") != null && request.get("requestModuleid").equals(Constants.Acc_Purchase_Order_ModuleId)) {
                condition += " and so.disabledSOforPO= 'F' ";
            }
            condition += " and so.pendingapproval = 0 and so.approvestatuslevel = 11 and so.istemplate != 2 ";
            String query = "from SalesOrder so" + condition+orderBy;
            query+=orderQuery;
            list = executeQuery( query, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
            /*When addmore option is enable from Account preferences & Generating PO from SO then 
             linking SO record(SO->PO) will be available in those 10 records
             */
            if (!StringUtil.isNullOrEmpty(linkTransactionId)) {
                List transactionList = new ArrayList();
                params.clear();
                params.add(linkTransactionId);

                query = "from SalesOrder so where so.ID=?";
                transactionList = executeQuery(query, params.toArray());
                if(transactionList.size()>0 && !list.contains(transactionList.get(0))){
                   list.set(list.size() - 1, transactionList.get(0));  
                }
               
            }
            
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCustomerDAOImpl.getSalesOrders:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getReplacementRequests(HashMap<String, Object> request) throws ServiceException {
        String condition = "";
        List params = new ArrayList();
        String companyId = (String) request.get("companyId");

        params.add(companyId);

        if (request.containsKey("customerId") && request.get("customerId") != null) {
            condition += " and pr.customer.id=?";
            params.add((String) request.get("customerId"));
        }
        boolean isNormalContract = false;
        if (request.containsKey("isNormalContract") && request.get("isNormalContract") != null) {
            isNormalContract = (Boolean) request.get("isNormalContract");
        }
        condition += " and pr.salesContractReplacement=?";
        params.add(isNormalContract);

        String query = "From ProductReplacement pr where pr.company.companyID=?" + condition + "Order By pr.replacementRequestNumber desc";
        List list = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getMaintenanceRequests(HashMap<String, Object> request) throws ServiceException {
        String condition = "";
        List params = new ArrayList();
        String companyId = (String) request.get("companyId");

        params.add(companyId);

        if (request.containsKey("customerId") && !StringUtil.isNullOrEmpty((String) request.get("customerId"))) {
            condition += " and pr.customer.id=?";
            params.add((String) request.get("customerId"));
        }

        if (request.containsKey("exclusedClosed") && request.get("exclusedClosed") != null) {
            condition += " and (pr.closed=?";
            params.add(!(Boolean) request.get("exclusedClosed"));
        }
        if (request.containsKey("soId") && request.get("soId") != null && request.containsKey("maintenanceIdForSo") && request.get("maintenanceIdForSo") != null && request.get("maintenanceIdForSo") != "") {
            String maintenanceIdForSo = (String) request.get("maintenanceIdForSo");
            condition += " or pr.closed= 1 and pr.id = ?";
            params.add(maintenanceIdForSo);
        }
        if (request.containsKey("exclusedClosed") && request.get("exclusedClosed") != null) {
            condition += " )";
        }

        boolean isNormalContract = false;
        if (request.containsKey("isNormalContract") && request.get("isNormalContract") != null) {
            isNormalContract = (Boolean) request.get("isNormalContract");
            condition += " and pr.salesContractMaintenance=?";
            params.add(isNormalContract);
        }

        String query = "From Maintenance pr where pr.company.companyID=?" + condition;
        List list = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public String[] columSort(String Col_Name, String Col_Dir) throws ServiceException {
        String[] String_Sort = new String[3];
        if (Col_Name.equals("personname")) {
            String_Sort[0] = " order by name " + Col_Dir;
            String_Sort[1] = ",customer.name";
            String_Sort[2] = ",customer.name ";
        } else if (Col_Name.equals("billno")) {
            String_Sort[0] = " order by sonumber " + Col_Dir;
            String_Sort[1] = ",salesorder.sonumber ";
            String_Sort[2] = ", billingsalesorder.sonumber ";

        } else if (Col_Name.equals("date")) {
            String_Sort[0] = " order by orderdate " + Col_Dir;
            String_Sort[1] = ", salesorder.orderdate";
            String_Sort[2] = ", billingsalesorder.orderdate";

        } else if (Col_Name.equals("duedate")) {
            String_Sort[0] = " order by duedate " + Col_Dir;
            String_Sort[1] = ", salesorder.duedate  ";
            String_Sort[2] = ", billingsalesorder.duedate ";

        } else if (Col_Name.equals("salespersonname")) {
            String_Sort[0] = " order by value " + Col_Dir;
            String_Sort[1] = ",masteritem.value ";
            String_Sort[2] = ", billingsalesorder.salesperson ";
        } else if (Col_Name.equals("aliasname")){
            String_Sort[0] = " order by aliasname " + Col_Dir;
            String_Sort[1] = ", customer.aliasname ";
            String_Sort[2] = ", customer.aliasname ";
        } else if (Col_Name.equals("startDate")){
            String_Sort[0] = " order by RSO.startdate " + Col_Dir;
            String_Sort[1] = ", RSO.startdate ";
            String_Sort[2] = ", RSO.startdate ";
        } else if (Col_Name.equals("expireDate")){
            String_Sort[0] = " order by RSO.expiredate " + Col_Dir;
            String_Sort[1] = ", RSO.expiredate ";
            String_Sort[2] = ", RSO.expiredate ";
        } else if (Col_Name.equals("nextDate")){
            String_Sort[0] = " order by RSO.nextdate " + Col_Dir;
            String_Sort[1] = ", RSO.nextdate ";
            String_Sort[2] = ", RSO.nextdate ";
        } else if (Col_Name.equals("NoOfpost")){
            String_Sort[0] = " order by RSO.noofsopost " + Col_Dir;
            String_Sort[1] = ", RSO.noofsopost ";
            String_Sort[2] = ", RSO.noofsopost ";
        }  else if (Col_Name.equals("shipdate")){
            String_Sort[0] = " order by shipdate " + Col_Dir;
            String_Sort[1] = ", salesorder.shipdate";
            String_Sort[2] = ", billingsalesorder.shipdate";
        } else {
            String_Sort[0] = " order by orderdate " + Col_Dir;
            String_Sort[1] = ", salesorder.orderdate";
            String_Sort[2] = ", billingsalesorder.orderdate";

        }
        return String_Sort;

    }

   public KwlReturnObject getSalesOrdersMerged(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            DateFormat df = (DateFormat) request.get(Constants.df);
            String start = (String) request.get(Constants.start);
            String limit = (String) request.get(Constants.limit);
            boolean isRepeateSalesOrder = true;
            boolean ispendingAproval = false;
            boolean issopoclosed = false;
            boolean isLeaseSO = false;
            boolean isConsignment = false;
            boolean isMRPSalesOrder = false;
            boolean isJobWorkOrderReciever = false;
            String moduleid = "";
             String requestStatus="";
            if (request.containsKey(Constants.moduleid) && request.get(Constants.moduleid) != null) {
                moduleid = request.get(Constants.moduleid).toString();
            }
            String userID = "";
            boolean isenableSalesPersonAgentFlow=false;
            if (request.containsKey("enablesalespersonagentflow") && request.get("enablesalespersonagentflow") != null && !StringUtil.isNullOrEmpty(request.get("enablesalespersonagentflow").toString())) {
                isenableSalesPersonAgentFlow = Boolean.parseBoolean(request.get("enablesalespersonagentflow").toString());
            }
            if (isenableSalesPersonAgentFlow) {
                if (request.containsKey("userid") && request.get("userid") != null && !StringUtil.isNullOrEmpty(request.get("userid").toString())) {
                    userID = (String) request.get("userid");
                }
            }
            if (request.containsKey("isLeaseFixedAsset") && request.get("isLeaseFixedAsset") != null) {
                isLeaseSO = (Boolean) request.get("isLeaseFixedAsset");
            }
            if (request.containsKey("isConsignment") && request.get("isConsignment") != null) {
                isConsignment = (Boolean) request.get("isConsignment");
            }
            if (request.containsKey("requestStatus") && request.get("requestStatus") != null) {
                requestStatus = (String) request.get("requestStatus");
            }
            if (request.containsKey("isMRPSalesOrder") && request.get("isMRPSalesOrder") != null) {
                isMRPSalesOrder = (Boolean) request.get("isMRPSalesOrder");
            }
            if (request.containsKey("isJobWorkOrderReciever") && request.get("isJobWorkOrderReciever") != null) {
                isJobWorkOrderReciever = (Boolean) request.get("isJobWorkOrderReciever");
            }
            String productid = "";
            if (request.containsKey(Constants.productid) && request.get(Constants.productid) != null) {
                productid = (String) request.get(Constants.productid);
            }

            String productCategoryid = "";
            if (request.containsKey(Constants.productCategoryid) && request.get(Constants.productCategoryid) != null) {
                productCategoryid = (String) request.get(Constants.productCategoryid);
            }

            String newcustomerid = "";
            if (request.containsKey(Constants.newcustomerid) && request.get(Constants.newcustomerid) != null) {
                newcustomerid = (String) request.get(Constants.newcustomerid);
            }

            String customerCategoryid = "";
            if (request.containsKey(Constants.customerCategoryid) && request.get(Constants.customerCategoryid) != null) {
                customerCategoryid = (String) request.get(Constants.customerCategoryid);
            }
            String billDate = "";
            if (request.containsKey(Constants.BillDate)) {
                billDate = (String) request.get(Constants.BillDate);
            }

            boolean checkSOForCustomer = false;
            if (request.containsKey(Constants.checksoforcustomer) && request.get(Constants.checksoforcustomer) != null) {
                checkSOForCustomer = (Boolean) request.get(Constants.checksoforcustomer);
            }
            String ss = (String) request.get(Constants.ss);

            String userDepartment = "";
            if (request.containsKey("userDepartment") && request.get("userDepartment") != null) {
                userDepartment = (String) request.get("userDepartment");
            }
            if (request.containsKey("ispendingAproval") && request.get("ispendingAproval") != null) {
                ispendingAproval = Boolean.FALSE.parseBoolean(String.valueOf(request.get("ispendingAproval")));
            }
            if (request.containsKey("issopoclosed") && request.get("issopoclosed") != null) {
                issopoclosed = Boolean.FALSE.parseBoolean(String.valueOf(request.get("issopoclosed")));
            }
            String statusFilter = "";
            if (request.containsKey("status") && request.get("status") != null) {
                statusFilter = request.get("status").toString();
            }
            ArrayList params = new ArrayList();
            ArrayList paramsWithoutInv = new ArrayList();
            params.add((String) request.get(Constants.companyKey));
            String companyId = (String) request.get(Constants.companyKey);
            paramsWithoutInv.add((String) request.get(Constants.companyKey));
            
            boolean pendingapproval = false;
            if(request.get("pendingapproval")!=null){
                pendingapproval =(Boolean) request.get("pendingapproval");
            }
            boolean isfavourite = false;
            boolean isprinted = false;
            boolean isForTemplate = false;
            boolean isCustomFieldAdvSearch = false;
            boolean isOpeningBalanceOrder = false;
            boolean deleted = Boolean.parseBoolean((String) request.get("deleted"));
            boolean nondeleted = Boolean.parseBoolean((String) request.get("nondeleted"));
            String billId = "";
            if (request.containsKey("isForTemplate") && request.get("isForTemplate") != null) {
                isForTemplate = Boolean.parseBoolean(request.get("isForTemplate").toString());
            }

            if (request.get(Constants.MARKED_FAVOURITE) != null) {
                isfavourite = Boolean.parseBoolean((String) request.get(Constants.MARKED_FAVOURITE));
            }
            if (request.get(Constants.MARKED_PRINTED) != null) {
                isprinted = Boolean.parseBoolean((String) request.get(Constants.MARKED_PRINTED));
            }
            if (request.get("isOpeningBalanceOrder") != null) {
                isOpeningBalanceOrder = (Boolean) request.get("isOpeningBalanceOrder");
            }            
            String conditionSQL = "";
            String conditionSQLWithoutInv = "";
            if (nondeleted) {
                conditionSQL = "  where salesorder.deleteflag='F' and salesorder.company=? ";
                conditionSQLWithoutInv = "where billingsalesorder.deleteflag='F' and billingsalesorder.company=? "; //ERP-12858
            } else if (deleted) {

                conditionSQL += " where salesorder.deleteflag='T' and salesorder.company=? ";
                conditionSQLWithoutInv += " billingsalesorder.deleteflag='T' and salesorder.company=? ";
            } else if (!StringUtil.isNullOrEmpty(statusFilter)&&"Open".equals(statusFilter)){
                 conditionSQL += " where salesorder.deleteflag='F' AND salesorder.isopen='T' and salesorder.company=?  ";
                conditionSQLWithoutInv += " where billingsalesorder.deleteflag='F' and billingsalesorder.company=? ";
            } else {
                // String condition = " where deleted=false and company.companyID=?";
                conditionSQL += " where salesorder.company=?";
                conditionSQLWithoutInv += "where billingsalesorder.company=?";
            }
            if (request.containsKey("linknumber") && request.get("linknumber") != null && !request.get("linknumber").toString().equals("")) {
                conditionSQL += " and salesorder.sonumber = ? ";
                params.add(request.get("linknumber"));
            }
            //fetch isDraft flag from request
            boolean isDraft = false;
            if (request.containsKey(Constants.isDraft) && request.get(Constants.isDraft) != null) {
                isDraft = (Boolean) request.get(Constants.isDraft);
            }
            //Append isDraft condition in query for fetching particular transactions
            if (isDraft) {
                conditionSQL += " and salesorder.isdraft = true ";
            } else {
                params.add(false);
                conditionSQL += " and salesorder.isdraft = ? ";
            }
            String searchJoin = "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"salesorder.sonumber", "salesorder.memo", "customer.name", "customer.aliasname",
                    "bsaddr.billingaddress", "bsaddr.billingcountry", "bsaddr.billingstate", "bsaddr.billingcity", "bsaddr.billingemail", "bsaddr.billingpostal",
                    "bsaddr.shippingaddress", "bsaddr.shippingCountry", "bsaddr.shippingstate", "bsaddr.shippingcity", "bsaddr.shippingemail", "bsaddr.shippingpostal", "product.name", "product.productid"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 18); 
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                conditionSQL += searchQuery;
                searchJoin = " inner join sodetails on sodetails.salesorder = salesorder.id "
                        + " inner join billingshippingaddresses bsaddr on bsaddr.id=salesorder.billingshippingaddresses "
                        + " inner join product on sodetails.product = product.id ";
                searchcol = new String[]{"billingsalesorder.sonumber", "billingsalesorder.memo", "customer.name", "customer.aliasname"};
                SearchStringMap = StringUtil.insertParamSearchStringMap(paramsWithoutInv, ss, 4);
                StringUtil.insertParamSearchString(SearchStringMap);
                searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                conditionSQLWithoutInv += searchQuery;

//                for (int i = 0; i < 3; i++) {
//                    params.add(ss + "%");
//                    paramsWithoutInv.add(ss + "%");
//                }
////                condition += " and ( salesOrderNumber like ? or so.memo like ? or so.customer.name like ? )";
//                conditionSQL += " and ( salesorder.sonumber like ? or salesorder.memo like ? or customer.name like ? )";
//                conditionSQLWithoutInv += " and ( billingsalesorder.sonumber like ? or billingsalesorder.memo like ? or customer.name like ? )";
            }
            /**
             * Fetch those record which has at least one job Order item
             */
            if (request.containsKey("joborderitem") && request.get("joborderitem") != null) {
                boolean joborderitem = (Boolean) request.get("joborderitem");
                if (joborderitem) {
                    searchJoin = " inner join sodetails on sodetails.salesorder = salesorder.id ";
                    conditionSQL += " and sodetails.joborderitem =true ";
                }
            }
            String innerQuery = "";
            if (!StringUtil.isNullOrEmpty(productid)) {
                params.add(productid);
                innerQuery = " inner join sodetails on sodetails.salesorder = salesorder.id ";
                conditionSQL += " and sodetails.product = ? ";
                conditionSQLWithoutInv += " and  billingsalesorder.customer = '' ";

            }

            if (!StringUtil.isNullOrEmpty(productCategoryid)) {
                params.add(productCategoryid);
                innerQuery = " inner join sodetails on sodetails.salesorder = salesorder.id ";
                conditionSQL += " and sodetails.product in (select productid from productcategorymapping where productcategory = ?) ";
                conditionSQLWithoutInv += " and  billingsalesorder.customer = '' ";
            }
 
            if (!StringUtil.isNullOrEmpty(newcustomerid)) {
                if (newcustomerid.contains(",")) {
                    newcustomerid = AccountingManager.getFilterInString(newcustomerid);
                    conditionSQL += " and salesorder.customer IN" + newcustomerid;
                    conditionSQLWithoutInv += " and billingsalesorder.customer IN" + newcustomerid;
                } else {
                    params.add(newcustomerid);
                    paramsWithoutInv.add(newcustomerid);
                    conditionSQL += " and salesorder.customer = ? ";
                    conditionSQLWithoutInv += " and  billingsalesorder.customer = ? ";
                }
            }

            if (!StringUtil.isNullOrEmpty(customerCategoryid) && !StringUtil.equal(customerCategoryid, "-1") && !StringUtil.equal(customerCategoryid, "All")) {
                params.add(customerCategoryid);
                paramsWithoutInv.add(customerCategoryid);
                conditionSQL += " and salesorder.customer in (select customerid from customercategorymapping where customercategory = ?)  ";
                conditionSQLWithoutInv += " and billingsalesorder.customer in (select customerid from customercategorymapping where customercategory = ?)  ";
            }

            if (request.containsKey("billId")) {
                billId = (String) request.get("billId");
            }

            if (!StringUtil.isNullOrEmpty(billId) && !checkSOForCustomer) {
                params.add(billId);
                paramsWithoutInv.add(billId);
                conditionSQL += " and salesorder.id=? ";
                conditionSQLWithoutInv += " and billingsalesorder.id=? ";
            }

            if (!StringUtil.isNullOrEmpty(billId) && checkSOForCustomer) {  // Check any SO is created for Customer
                params.add(billId);
                conditionSQL += " and salesorder.id<>? ";
            }

            String costCenterId = (String) request.get(CCConstants.REQ_costCenterId);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                params.add(costCenterId);
                paramsWithoutInv.add(costCenterId);
                // condition += " and so.costcenter.ID=?";
                conditionSQL += " and costcenter.id=?";
                conditionSQLWithoutInv += " and costcenter.id=?";
            }
            String startDate = (String) request.get(Constants.REQ_startdate)!=null ? StringUtil.DecodeText((String) request.get(Constants.REQ_startdate)) : null;
            String endDate = (String) request.get(Constants.REQ_enddate)!=null ? StringUtil.DecodeText((String) request.get(Constants.REQ_enddate)) : null;
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                conditionSQL += " and (salesorder.orderdate >=? and salesorder.orderdate <=?)";
                conditionSQLWithoutInv += " and (billingsalesorder.orderdate >=? and billingsalesorder.orderdate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
                paramsWithoutInv.add(df.parse(startDate));
                paramsWithoutInv.add(df.parse(endDate));
            }        
            if (request.containsKey(Constants.isRepeatedFlag) && request.get(Constants.isRepeatedFlag) != null) {
                if (Boolean.parseBoolean((String) request.get(Constants.isRepeatedFlag))) {
//                    condition += " and repeateInvoice is not null";
                    //conditionSQL += " and salesorder.repeateso  is not null";
                    innerQuery=" inner join repeatedsalesorders on repeatedsalesorders.id = salesorder.repeateso  ";
                    if(ispendingAproval){   //Pending Approval Records
                        conditionSQL += " and (salesorder.repeateso is not null and repeatedsalesorders.ispendingapproval='T') ";
                    } else {
                        conditionSQL += " and (salesorder.repeateso is not null and repeatedsalesorders.ispendingapproval='F') ";
                    }
                    isRepeateSalesOrder = false;
                }
            }
            if (isfavourite) {
                conditionSQL += " and salesorder.favouriteflag=true ";
                conditionSQLWithoutInv += " and billingsalesorder.favouriteflag=true ";
            }
       
            if (request.containsKey(Constants.generatedSource) && request.get(Constants.generatedSource) != null) {
                conditionSQL += " and salesorder.generatedsource = ? ";
                params.add((Integer) (request.get(Constants.generatedSource)));
            }
            
            if (isLeaseSO) {
                conditionSQL += " and salesorder.leaseOrMaintenanceSO=1 ";
            } else if (isConsignment) {
                conditionSQL += " and (salesorder.isconsignment='T'  and  salesorder.leaseOrMaintenanceSO=3) ";
                if (!StringUtil.isNullOrEmpty(requestStatus) && "Open".equals(requestStatus)) {
                    conditionSQL += " AND salesorder.deleteflag='F' AND salesorder.freezeflag='F'  AND salesorder.isconsignment='T' AND salesorder.issoclosed='F' AND salesorder.isopen='T' ";
                } else if (!StringUtil.isNullOrEmpty(requestStatus) && "Closed".equals(requestStatus)) {
                    conditionSQL += " AND salesorder.deleteflag='F' AND salesorder.isconsignment='T' AND (salesorder.freezeflag='T' OR salesorder.isopen='F') ";
                }
            } else {
                conditionSQL += " and (salesorder.leaseOrMaintenanceSO=0 or salesorder.leaseOrMaintenanceSO=2)";
            }
            
//             if(isMRPSalesOrder){
//                conditionSQL += " and  salesorder.ismrpsalesorder='T' ";
//             }else{
//                conditionSQL += " and  salesorder.ismrpsalesorder='F' ";
//             }  
             if(issopoclosed){
             conditionSQL+=" and salesorder.issoclosed='F' ";
             }
             
             if(isJobWorkOrderReciever){
                conditionSQL += " and  salesorder.isjobworkorder='T' ";
             }else{
                conditionSQL += " and  salesorder.isjobworkorder='F' ";
             }  


            if (request.containsKey("custWarehouse") && request.get("custWarehouse") != null) {
                String custWarehouse = (String) request.get("custWarehouse");
                if (isConsignment && !StringUtil.isNullOrEmpty(custWarehouse)) {
                    params.add(custWarehouse);
                    conditionSQL += " and so.custWarehouse = ?";
                }
            }

            if (isprinted) {
                conditionSQL += " and salesorder.printedflag=true ";
            }

            if (!StringUtil.isNullOrEmpty(billDate)) {
                conditionSQL += " and (salesorder.orderdate =? )";
                params.add(df.parse(billDate));
            }

            if (request.containsKey("salesPersonFilterFlag") && (Boolean) request.get("salesPersonFilterFlag") && request.get("userId") != null) {
                String userId = (String) request.get("userId");
                if (!StringUtil.isNullOrEmpty(userId)) {
                    DataFilteringModule dataFilteringModule = null;
                    MasterItem masterItem = null;
                    List<DataFilteringModule> dataFilteringModuleList = new ArrayList<DataFilteringModule>();
                    List<MasterItem> masterItems = new ArrayList<MasterItem>();

                    dataFilteringModuleList = find("from DataFilteringModule where user.userID='" + userId + "' and company.companyID='" + companyId + "'");
                    masterItems = find("from MasterItem where user='" + userId + "' and company.companyID='" + companyId + "' and masterGroup.ID='" + 15 + "'");
                    if (!dataFilteringModuleList.isEmpty()) {
                        dataFilteringModule = dataFilteringModuleList.get(0);
                    }
//                    if (!masterItems.isEmpty()) {
//                        masterItem = masterItems.get(0);
//                    }
                    if ((dataFilteringModule != null && !dataFilteringModule.isSalesOrder()) || (dataFilteringModule != null && !dataFilteringModule.isSalesOrder() && masterItem != null)) {
                        conditionSQL += " and ( ";
                    }

                    if (dataFilteringModule != null && !dataFilteringModule.isSalesOrder()) {
                        params.add(dataFilteringModule.getUser().getUserID());
                        conditionSQL += "salesorder.createdby=? ";
                    }

                    if (dataFilteringModule != null && !dataFilteringModule.isSalesOrder() && !masterItems.isEmpty()) {
                        String qMarks = "";
                        for (MasterItem item : masterItems) {
                            qMarks += "?,";
                            params.add(item.getID());
                        }
                        qMarks = qMarks.substring(0, qMarks.length() - 1);
                        conditionSQL += " or salesorder.salesperson in (" + qMarks + ")";
                    }
                    if ((dataFilteringModule != null && !dataFilteringModule.isSalesOrder()) || (dataFilteringModule != null && !dataFilteringModule.isSalesOrder() && masterItem != null)) {
                        conditionSQL += " ) ";
                    }
                }
            }

            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                innerQuery += " inner join users on users.userid = salesorder.createdby ";
                conditionSQL += " and users.department = ? ";
                params.add(userDepartment);
            }

            if (!isForTemplate) {
                //Ignore SOs created as only templates
                conditionSQL += " and salesorder.istemplate != 2 ";
                conditionSQLWithoutInv += " and billingsalesorder.istemplate != 2 ";

                if (pendingapproval) {
                    params.add(11);
                    conditionSQL += " and salesorder.approvestatuslevel != ? ";
                } else {
                    params.add(11);
                    conditionSQL += " and salesorder.approvestatuslevel = ? ";
                }
            }
            /*
             * Getting Blocked/Unblocked Documnets of SO for PO
             */
            boolean blockedDocuments = Boolean.parseBoolean((String) request.get("blockedDocuments"));
            boolean unblockedDocuments = Boolean.parseBoolean((String) request.get("unblockedDocuments"));
            if (blockedDocuments) {
                conditionSQL += " and salesorder.disabledsoforpo = 'T' ";
            }
            if (unblockedDocuments) {
                conditionSQL += " and salesorder.disabledsoforpo = 'F' ";
            }

            if (request.containsKey("currencyfilterfortrans") && request.get("currencyfilterfortrans") != null) {
                String currencyfilterfortrans = (String) request.get("currencyfilterfortrans");
                if (!StringUtil.isNullOrEmpty(currencyfilterfortrans)) {
                    params.add(currencyfilterfortrans);
                    paramsWithoutInv.add(currencyfilterfortrans);
                    conditionSQL += " and salesorder.currency = ? ";
                    conditionSQLWithoutInv += " and  billingsalesorder.currency = ? ";
                }
            }
            
            String appendCase = "and";
            String Searchjson = "";
            String mySearchFilterString = "";
            String searchDefaultFieldSQL = "";
            String joinString = "";
            String joinString1 = "";
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (request.containsKey("filterConjuctionCriteria") && request.get("filterConjuctionCriteria") != null) {
                if (request.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            if (request.containsKey("searchJson") && request.get("searchJson") != null) {
                Searchjson = StringUtil.DecodeText(request.get("searchJson").toString());

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
                        Map<String, Object> map = buildSqlDefaultFieldAdvSearch(defaultSearchFieldArray, params, moduleid, tableArray, filterConjuctionCriteria);
                        searchJoin += map.containsKey("searchjoin") ? map.get("searchjoin") : "";
                        searchJoin += " left join solinking on solinking.docid=salesorder.id and solinking.sourceflag = 1 ";
                        searchDefaultFieldSQL = (String) (map.containsKey("condition") ? map.get("condition") : "");
                    }

                    if (customSearchFieldArray.length() > 0) {   //Advance search case for Custome field
                        isCustomFieldAdvSearch = true;
                        request.put(Constants.Searchjson, Searchjson);
                        request.put(Constants.appendCase, appendCase);
                        request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(request, true).get(Constants.myResult));
//                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
//                    joinString = " inner join purchaseordercustomdata on purchaseordercustomdata.poID=purchaseorder.purchaseordercustomdataref ";
                        String innerJoinOnDetailTable = "";
                        if (!searchJoin.contains("sodetails.salesorder")) {
                            innerJoinOnDetailTable = " inner join sodetails on sodetails.salesorder=salesorder.id ";
                        }
                        boolean isInnerJoinAppend = false;
                        if (mySearchFilterString.contains("salesordercustomdata")) {
                            joinString1 = " inner join salesordercustomdata on salesordercustomdata.soID=salesorder.salesordercustomdataref ";
                        }
                        if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "salesorderdetailcustomdata");
                            joinString1 += innerJoinOnDetailTable + " left join salesorderdetailcustomdata on sodetails.id=salesorderdetailcustomdata.soDetailID ";
                            isInnerJoinAppend = true;
                        }
                        if (mySearchFilterString.contains("CustomerCustomData")) {
                            joinString1 += " left join customercustomdata  on customercustomdata.customerId=salesorder.customer ";
                            mySearchFilterString = mySearchFilterString.replaceAll("CustomerCustomData", "customercustomdata");
                        }
                        if (mySearchFilterString.contains("AccJEDetailsProductCustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "sodetailproductcustomdata");
                            joinString1 += (isInnerJoinAppend ? "" : innerJoinOnDetailTable) + " left join sodetailproductcustomdata on sodetails.id=sodetailproductcustomdata.soDetailID ";
                            isInnerJoinAppend = true;
                        }
                        //product custom data
                        if (mySearchFilterString.contains("accproductcustomdata")) {
                            joinString1 += (isInnerJoinAppend ? "" : innerJoinOnDetailTable) + " left join product on product.id=sodetails.product left join accproductcustomdata on accproductcustomdata.productId=product.id ";
                        }
                        StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
//                    StringUtil.insertParamAdvanceSearchString1(paramsSQLWithoutInv, Searchjson);
                    }
                    mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
                }
            }
            
            String orderBy = "";
            String sort_Col = "";
            String sort_Col1 = "";
            String joinString2 = "";
            String[] stringSort = null;
            if (request.containsKey("dir") && request.containsKey("sort")) {
                String Col_Name = request.get("sort").toString();
                String Col_Dir = request.get("dir").toString();
                stringSort = columSort(Col_Name, Col_Dir);
                orderBy += stringSort[0];
                sort_Col += stringSort[1];
                sort_Col1 += stringSort[2];
                if(request.get("sort").toString().equals("salespersonname")){
                   joinString2 += "  left join masteritem on masteritem.id = salesorder.salesperson  ";
                }

            } else {
                if (isConsignment) {
                    stringSort = columSort("billno", "desc");
                    orderBy = " order by orderdate desc,sonumber desc " ;
                    sort_Col +=", salesorder.orderdate " + stringSort[1] ;
                    sort_Col1 +=  ", billingsalesorder.orderdate " + stringSort[2] ;
                } else {
                    orderBy = " order by orderdate desc ";
                    sort_Col += ", salesorder.orderdate ";
                    sort_Col1 += ", billingsalesorder.orderdate ";
                }
            }
            String salesPersonMappingQuery = "";
//            if (isenableSalesPersonAgentFlow  && !StringUtil.isNullOrEmpty(userID)) {
//                salesPersonMappingQuery = " left join salespersonmapping spm on spm.customerid=salesorder.customer  left join masteritem  mst on mst.id=spm.salesperson ";
//                joinString1 += salesPersonMappingQuery;
//                conditionSQL += " and ((mst.user= '" + userID + "' or mst.user is null  and customer.isavailableonlytosalespersons='T' ) or  (customer.isavailableonlytosalespersons='F')) ";
//            }
            if (isenableSalesPersonAgentFlow && !StringUtil.isNullOrEmpty(userID)) {//this block is executed only when owners restriction feature is on 
                String salesPersonID = (String) request.get("salesPersonid");
                String salespersonQuery = "";
                 if (!StringUtil.isNullOrEmpty(salesPersonID)) {
                   salesPersonID= AccountingManager.getFilterInString(salesPersonID);
                    salespersonQuery = "  salesorder.salesperson in " + salesPersonID + " or ";
                }
                
                conditionSQL += " and ( " + salespersonQuery + "  salesorder.createdby='" + userID + "' or salesorder.salesperson is null  ) ";
            }
            String mysqlQuery = " select DISTINCT salesorder.id, 'false' as withoutinventory " + sort_Col + " from salesorder "
                    + "inner join customer on customer.id = salesorder.customer "
                    + "left join repeatedsalesorders RSO on RSO.id = salesorder.repeateso "
                    + searchJoin + innerQuery + joinString1 + joinString2
                    + "left join costcenter on costcenter.id = salesorder.costcenter " + conditionSQL + mySearchFilterString;
            if (!(isCustomFieldAdvSearch || isOpeningBalanceOrder) && isRepeateSalesOrder) {
                mysqlQuery += "union"
                        + " select billingsalesorder.id, 'true' as withoutinventory " + sort_Col1 + " from billingsalesorder "
                        + "inner join customer on customer.id = billingsalesorder.customer "
                        + "left join costcenter on costcenter.id = billingsalesorder.costcenter " + conditionSQLWithoutInv;

                params.addAll(paramsWithoutInv);
            }
            mysqlQuery += orderBy;
            //list = executeQuery( query, params.toArray());
            list = executeSQLQuery( mysqlQuery, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                //list = executeQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
                list = executeSQLQueryPaging( mysqlQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCustomerDAOImpl.getSalesOrders:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getOpeningBalanceSalesOrders(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        int count = 0;

        String start = (String) requestParams.get(Constants.start);
        String limit = (String) requestParams.get(Constants.limit);

        String companyId = (String) requestParams.get("companyid");
        String condition = "";
        List params = new ArrayList();
        params.add(companyId);

        if (requestParams.containsKey("customerid")) {
            String vendorId = (String) requestParams.get("customerid");
            condition += " and so.customer.ID=? ";
            params.add(vendorId);
        }

        String query = "from SalesOrder so where so.isOpeningBalanceSO=true AND so.deleted=false AND so.company.companyID=?" + condition;
        list = executeQuery( query, params.toArray());
        count = list.size();

        if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
            list = executeQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
        }

        return new KwlReturnObject(true, null, null, list, count);
    }

    @Override
    public KwlReturnObject getSOContainingProject(HashMap<String, Object> dataMap) throws ServiceException {
        String companyId = (String) dataMap.get("companyid");
        String searchString = (String) dataMap.get("searchString");
        Date startDate = (Date) dataMap.get("startDate");
        Date endDate = (Date) dataMap.get("endDate");

        String mysqlQuery = " select salesorder.id, 'false' as withoutinventory,salesorder.orderdate from salesorder "
                + "inner join customer on customer.id = salesorder.customer "
                + " inner join salesordercustomdata on salesordercustomdata.soID=salesorder.salesordercustomdataref "
                + " where salesorder.deleteflag='F' and salesorder.company=?"
                + " and (salesorder.orderdate >=? and salesorder.orderdate <=?)" + searchString;


        List list = executeSQLQuery( mysqlQuery, new Object[]{companyId, startDate, endDate});

        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject deleteSalesOrder(String soid, String companyid) throws ServiceException {
        //This code used for update the link flag of quotation
        ArrayList updateLinkFlagList = new ArrayList();
        updateLinkFlagList.add(soid);
        String upadteQuotation = "update quotation as q inner join  quotationdetails as qd inner join sodetails  sod on  q.id=qd.quotation and sod.quotationdetail=qd.id set linkflag=0 where sod.salesorder=?";
        int result = executeSQLUpdate( upadteQuotation, updateLinkFlagList.toArray());

        String query = "update SalesOrder set deleted=true where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{soid, companyid});
        return new KwlReturnObject(true, "Sales Order has been deleted successfully.", null, null, numRows);
    }
        
    @Override
    public KwlReturnObject deleteAssetDetailsLinkedWithSO(HashMap<String, Object> requestParams) throws ServiceException {
        int numtotal = 0;
        try {
            if (requestParams.containsKey("invoiceid") && requestParams.containsKey("companyid")) {
                boolean deleteMappingAlso = false;

                if (requestParams.containsKey("deleteMappingAlso")) {
                    deleteMappingAlso = (Boolean) requestParams.get("deleteMappingAlso");
                }

                int numRows = 0;
                ArrayList params8 = new ArrayList();
                params8.add(requestParams.get("companyid"));
                params8.add(requestParams.get("invoiceid"));

                // Deleting data from asset details table

                String assetQuery = "SELECT ad.id FROM salesorder so "
                        + "INNER JOIN  sodetails sod ON so.id=sod.salesorder "
                        + "INNER JOIN assetdetailsinvdetailmapping amp ON sod.id=amp.invoicedetailid "
                        + "INNER JOIN assetdetail ad on ad.id=amp.assetdetails "
                        + "WHERE amp.moduleid=20 AND ad.islinkedtoleaseso=true AND so.company=? and so.id=?";

                List assetList = executeSQLQuery( assetQuery, params8.toArray());
                Iterator assetItr = assetList.iterator();

                while (assetItr.hasNext()) {
                    ArrayList assetParams = new ArrayList();

                    String assetId = assetItr.next().toString();

                    assetParams.add(assetId);
                    assetParams.add(requestParams.get("companyid"));

                    String assupdateQuery = "UPDATE assetdetail SET islinkedtoleaseso=0 WHERE id=? AND company=?";
                    numRows = executeSQLUpdate( assupdateQuery, assetParams.toArray());
                }

//                if (deleteMappingAlso) {

//                String myquery = "select id from sodetails where salesorder in (select id from salesorder where company =? and id=?)";
                String myquery = " select sod.id from sodetails sod inner join salesorder so on so.id=sod.salesorder where so.company =? and so.id=?";
                List list = executeSQLQuery( myquery, params8.toArray());
                Iterator itr = list.iterator();
                String idStrings = "";
                while (itr.hasNext()) {

                    String invdid = itr.next().toString();
                    idStrings += "'" + invdid + "',";
                }
                if (!StringUtil.isNullOrEmpty(idStrings)) {
                    idStrings = idStrings.substring(0, idStrings.length() - 1);
                }


                // Deleting data from assetdetailsinvdetailmapping

                if (!StringUtil.isNullOrEmpty(idStrings)) {
                    ArrayList assetParams = new ArrayList();
                    assetParams.add(requestParams.get("companyid"));

                    String assetMapDelQuery = "DELETE FROM assetdetailsinvdetailmapping WHERE invoicedetailid IN (" + idStrings + ") and moduleid=20 and company=?";
                    numRows = executeSQLUpdate( assetMapDelQuery, assetParams.toArray());
                }
//                }
            }


        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot delete Sales Order as its referance child field is not deleted.", ex);//+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Sales Order has been deleted successfully.", null, null, numtotal);
    }

    //function for deleting sales order permananetly      
    @Override
    public KwlReturnObject deleteSalesOrdersPermanent(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            String delQuery1 = "", delQuery2 = "", delQuery3 = "", delQuery4 = "", delQuery5 = "", delQuery6;
            int numtotal = 0;
            if (requestParams.containsKey("soid") && requestParams.containsKey("companyid")) {


                boolean isLeaseFixedAsset = false;
                if (requestParams.containsKey("isLeaseFixedAsset") && requestParams.get("isLeaseFixedAsset") != null) {
                    isLeaseFixedAsset = (Boolean) requestParams.get("isLeaseFixedAsset");
                }

                ArrayList params8 = new ArrayList();
                params8.add(requestParams.get("soid"));
                params8.add(requestParams.get("companyid"));

                if (isLeaseFixedAsset) {

                    String assetQuery = "SELECT ad.id FROM salesorder so "
                            + "INNER JOIN  sodetails sod ON so.id=sod.salesorder "
                            + "INNER JOIN assetdetailsinvdetailmapping amp ON sod.id=amp.invoicedetailid "
                            + "INNER JOIN assetdetail ad on ad.id=amp.assetdetails "
                            + "WHERE amp.moduleid=20 AND ad.islinkedtoleaseso=true and so.id=? AND so.company=?";

                    List assetList = executeSQLQuery( assetQuery, params8.toArray());
                    Iterator assetItr = assetList.iterator();

                    while (assetItr.hasNext()) {
                        ArrayList assetParams = new ArrayList();

                        String assetId = assetItr.next().toString();

                        assetParams.add(assetId);
                        assetParams.add(requestParams.get("companyid"));

                        String assupdateQuery = "UPDATE assetdetail SET islinkedtoleaseso=0 WHERE id=? AND company=?";
                        int numRows = executeSQLUpdate( assupdateQuery, assetParams.toArray());
                    }
                }

                //This code used for update the link flag of quotation
                ArrayList updateLinkFlagList = new ArrayList();
                updateLinkFlagList.add(requestParams.get("soid"));
                String upadteQuotation = "update quotation as q inner join  quotationdetails as qd inner join sodetails  sod on  q.id=qd.quotation and sod.quotationdetail=qd.id set linkflag=0 , isopen='T' where sod.salesorder=?";
                int result = executeSQLUpdate( upadteQuotation, updateLinkFlagList.toArray());

//                String myquery = "select id from sodetails where salesorder in (select id from salesorder where id=? and company = ?)";
                String myquery = " select sod.id from sodetails sod inner join salesorder so on so.id=sod.salesorder where so.id=? and so.company =?";
                List list = executeSQLQuery( myquery, params8.toArray());
                Iterator itr = list.iterator();
                String idStrings = "";
                while (itr.hasNext()) {


                    String invdid = itr.next().toString();
                    idStrings += "'" + invdid + "',";
                }
                if (!StringUtil.isNullOrEmpty(idStrings)) {
                    idStrings = idStrings.substring(0, idStrings.length() - 1);
                }
                deleteGstTaxClassDetails(idStrings);
                // Deleting data from assetdetailsinvdetailmapping
                if (!StringUtil.isNullOrEmpty(idStrings) && isLeaseFixedAsset) {
                    ArrayList assetParams = new ArrayList();
                    assetParams.add(requestParams.get("companyid"));

                    String assetMapDelQuery = "DELETE FROM assetdetailsinvdetailmapping WHERE invoicedetailid IN (" + idStrings + ") and moduleid=20 and company=?";
                    int numRows = executeSQLUpdate( assetMapDelQuery, assetParams.toArray());
                }

                ArrayList params5 = new ArrayList();
                params5.add(requestParams.get("soid"));
                params5.add(requestParams.get("companyid"));
//                delQuery5 = "delete from sodetails where salesorder in (select id from salesorder where id=? and company = ?)";
                delQuery5 = "delete sod from sodetails  sod inner join salesorder so on so.id=sod.salesorder where so.id=? and so.company=?";
                int numRows5 = executeSQLUpdate( delQuery5, params5.toArray());



                ArrayList params9 = new ArrayList();
                params9.add(requestParams.get("soid"));
                params9.add(requestParams.get("companyid"));
                String myquery1 = "select id from salesorder where id=? and company = ?";
                List list1 = executeSQLQuery( myquery1, params9.toArray());
                Iterator itr1 = list1.iterator();
                String journalent = "";
                while (itr1.hasNext()) {

                    String jeidi = itr1.next().toString();
                    journalent += "'" + jeidi + "',";
                }
                if (!StringUtil.isNullOrEmpty(journalent)) {
                    journalent = journalent.substring(0, journalent.length() - 1);
                }

                
                ArrayList params1 = new ArrayList();
                delQuery1 = "delete  from salesorderdetailcustomdata where soDetailID in (" + idStrings + ")";
                int numRows1 = executeSQLUpdate( delQuery1, params1.toArray());

                ArrayList params6 = new ArrayList();
                params6.add(requestParams.get("companyid"));
                params6.add(requestParams.get("soid"));
                String salesorderID = (String) requestParams.get("soid");
                int numRows8 = 0;
                
                deleteGstDocHistoryDetails(salesorderID);
                
                String delQuery8 = "delete from salesordertermmap where salesorder=?";
                numRows8 = executeSQLUpdate( delQuery8, new Object[]{salesorderID});
            
                delQuery6 = "delete from salesorder where company = ? and id=?";
                int numRows6 = executeSQLUpdate( delQuery6, params6.toArray());


                ArrayList params2 = new ArrayList();
                delQuery2 = "delete  from salesordercustomdata where soID in (" + journalent + ")";
                int numRows2 = executeSQLUpdate( delQuery2, params2.toArray());

                ArrayList params7 = new ArrayList();
                String delQuery7 = "delete  from sodetailsvendormapping where id in (" + idStrings + ")";
                int numRows7 = executeSQLUpdate( delQuery7, params7.toArray());

                numtotal = numRows1 + numRows2 + numRows5 + numRows6 + numRows7 + numRows8;
            }

            return new KwlReturnObject(true, "Sales Order has been deleted successfully.", null, null, numtotal);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot delete Sales Order as its referance child field is not deleted.", ex);//+ex.getMessage(), ex);
        }

    }
    //after deleting consigment request we should have to release locked quantity from SO

    public KwlReturnObject deleteSalesOrdersBatchSerialDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String delQuery = "", delQuery1 = "", delQuery2 = "", delQuery3 = "", delQuerypb = "", delQuery5 = "", delQuery6, delQuery15 = "", delQueryBatch = "", delQuerySerail = "", delQueryBmap = "", delQueryBs = "";
        int numtotal = 0, numRows5 = 0;
        String batchserialids = "", batchids = "";
        String serialmapids = "", docids = "";
        String batchmapids = "", consignbatchmapid = "", consignserialmapid = "";
        boolean isConsignment = false;
        ArrayList params13 = new ArrayList();
        params13.add(requestParams.get("companyid"));
        params13.add(requestParams.get("soid"));
        if (requestParams.containsKey("isConsignment") && requestParams.get("isConsignment") != null) {
            isConsignment = Boolean.parseBoolean(requestParams.get("isConsignment").toString());
        }
//        String myquery3 = "select id from sodetails where salesorder in (select id from salesorder where company = ? and id=?)";
        String myquery3 = " select sod.id from sodetails sod inner join salesorder so on so.id=sod.salesorder where so.company =? and so.id=?";
        List listBatch = executeSQLQuery( myquery3, params13.toArray());
        Iterator itrBatch = listBatch.iterator();
        while (itrBatch.hasNext()) {
            String batchstring = itrBatch.next().toString();
            docids += "'" + batchstring + "',";
        }
        if (!StringUtil.isNullOrEmpty(docids)) {
            docids = docids.substring(0, docids.length() - 1);
        }
        if (!StringUtil.isNullOrEmpty(docids)) {
            ArrayList params14 = new ArrayList();
            String myquery4 = " select batchmapid,id from locationbatchdocumentmapping where documentid in (" + docids + ") and isconsignment='F'";
            String myquery5 = " select serialid,id from serialdocumentmapping where documentid in (" + docids + ") and isconsignment='F' ";


            List list4 = executeSQLQuery( myquery4, params14.toArray());
            Iterator itr4 = list4.iterator();
            while (itr4.hasNext()) {
                Object[] objArr = (Object[]) itr4.next();
                LocationBatchDocumentMapping locationBatchDocumentMapping = (LocationBatchDocumentMapping) get(LocationBatchDocumentMapping.class, (String) objArr[1]);
                if (locationBatchDocumentMapping != null && locationBatchDocumentMapping.getBatchmapid()!=null) {
                    HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
//                    batchUpdateQtyMap.put("qty", locationBatchDocumentMapping.getQuantity());
                    batchUpdateQtyMap.put("id", locationBatchDocumentMapping.getBatchmapid().getId());

//                    batchUpdateQtyMap.put("isForconsignment", false);
//                    batchUpdateQtyMap.put("consignquantity", 0.0);
                    batchUpdateQtyMap.put("lockquantity", -locationBatchDocumentMapping.getQuantity());
                    saveBatchAmountDue(batchUpdateQtyMap);
                }
                batchmapids += "'" + objArr[0] + "',";

            }
            if (!StringUtil.isNullOrEmpty(batchmapids)) {
                batchmapids = batchmapids.substring(0, batchmapids.length() - 1);
            }
            list4 = executeSQLQuery( myquery5, params14.toArray());
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
                int numRows1 = executeSQLUpdate( delQuery1, params1.toArray());
            }
            if (!StringUtil.isNullOrEmpty(serialmapids)) {
                serialmapids = serialmapids.substring(0, serialmapids.length() - 1);
            }
        }
        ArrayList params15 = new ArrayList();
        delQuerypb = "delete  from locationbatchdocumentmapping where documentid in (" + docids + ") and isconsignment='F' ";
        int numRows = executeSQLUpdate( delQuerypb, params15.toArray());

        delQuerypb = "delete  from serialdocumentmapping where documentid in (" + docids + ") and isconsignment='F' ";
        numRows = executeSQLUpdate( delQuerypb, params15.toArray());

        return new KwlReturnObject(true, "Delivery Order has been deleted successfully.", null, null, numtotal);
    }

    
    public void releseSODBatchLockQuantity(HashMap<String, Object> requestParams) throws ServiceException {
        String documentid = (String) requestParams.get("documentid");
        if (!StringUtil.isNullOrEmpty(documentid)) {
            double quantity = (Double) requestParams.get("rejectedQuantity");
            String batchQuery = " select batchmapid,id from locationbatchdocumentmapping where documentid in ( '" + documentid + "' ) and isconsignment='F'";
            List list = executeSQLQuery( batchQuery);
            Iterator itr = list.iterator();
            while (itr.hasNext() && quantity > 0) {
                Object[] objArr = (Object[]) itr.next();
                double lockquantity = 0.0;
                LocationBatchDocumentMapping locationBatchDocumentMapping = (LocationBatchDocumentMapping) get(LocationBatchDocumentMapping.class, (String) objArr[1]);
                if (locationBatchDocumentMapping != null && locationBatchDocumentMapping.getBatchmapid() != null && locationBatchDocumentMapping.getBatchmapid().getLockquantity() > 0) {
                    double batchlockquantity = locationBatchDocumentMapping.getQuantity();
                    if (quantity > batchlockquantity == true) {
                        lockquantity = batchlockquantity;
                        quantity = quantity - lockquantity;
                    } else {
                        lockquantity = quantity;
                        quantity = quantity - lockquantity;
                    }
          
                    NewProductBatch productBatch=locationBatchDocumentMapping.getBatchmapid();
                    productBatch.setLockquantity(productBatch.getLockquantity() - lockquantity);
                    //productBatch.setQuantitydue(productBatch.getQuantitydue() - lockquantity);

                    String serialQuery = " select sdm.id as serialmapid,nbs.id from newbatchserial nbs inner join serialdocumentmapping sdm on sdm.serialid=nbs.id where sdm.documentid in ( '" + documentid + "' ) and nbs.batch in ( '" + locationBatchDocumentMapping.getBatchmapid().getId() + "') and nbs.lockquantity > 0 and sdm.isconsignment='F' ";
                    List seriallist = executeSQLQuery( serialQuery);
                    Iterator serialitr = seriallist.iterator();
                    while (serialitr.hasNext() && lockquantity > 0) {//frees the lockquantity from that batch for batch count i.e lockquantity.
                        Object[] serialObjArr = (Object[]) serialitr.next();
                        NewBatchSerial newBatchSerial = (NewBatchSerial) get(NewBatchSerial.class, (String) serialObjArr[1]);
                        if (newBatchSerial != null && newBatchSerial.getLockquantity() > 0) {
                            newBatchSerial.setLockquantity(0);
                            lockquantity--;
                        }
                    }
                }
            }
        }
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
//                newBatchSerial.setIsForconsignment(Boolean.parseBoolean(productSerialMap.get("isForconsignment").toString()));
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
//                Double itemQty = (Double) productbatchMap.get("qty");
                Double lockqty = (Double) productbatchMap.get("lockquantity");
                productBatch.setLockquantity(productBatch.getLockquantity() + lockqty);
//                productBatch.setQuantitydue(productBatch.getQuantitydue() + itemQty);
//                Double consignquantity= (Double )productbatchMap.get("consignquantity");
//                productBatch.setConsignquantity(consignquantity);
//                productBatch.setIsForconsignment(Boolean.parseBoolean(productbatchMap.get("isForconsignment").toString()));

            }
            saveOrUpdate(productBatch);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.saveBatchAmountDue : " + ex.getMessage(), ex);
        }
    }
     public void updateSerialslockQuantity(HashMap<String, Object> productSerialMap) throws ServiceException {
        try {
            NewBatchSerial newBatchSerial = new NewBatchSerial();
            String itemID = (String) productSerialMap.get("id");
            if (productSerialMap.containsKey("id") && !StringUtil.isNullOrEmpty(itemID)) {
                newBatchSerial = (NewBatchSerial) get(NewBatchSerial.class, itemID);
                Double lockqty = Double.parseDouble((String) productSerialMap.get("lockquantity"));
                newBatchSerial.setLockquantity(lockqty);
            }
            saveOrUpdate(newBatchSerial);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.saveSerialAmountDue : " + ex.getMessage(), ex);
        }
    }
    public void updatebatchlockQuantity(HashMap<String, Object> productbatchMap) throws ServiceException {

        try {
            NewProductBatch productBatch = new NewProductBatch();
            String itemID = (String) productbatchMap.get("id");
            if (productbatchMap.containsKey("id") && !StringUtil.isNullOrEmpty(itemID)) {
                productBatch = (NewProductBatch) get(NewProductBatch.class, itemID);
                Double lockqty = (Double) productbatchMap.get("lockquantity");
                productBatch.setLockquantity(productBatch.getLockquantity()+lockqty);
            }
            saveOrUpdate(productBatch);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.saveBatchAmountDue : " + ex.getMessage(), ex);
        }
    }

    public KwlReturnObject getRepeateSalesOrder(HashMap<String, Object> requestParams) throws ServiceException {
        Date currentDate = new Date();
        String query = "from SalesOrder where repeateSO is not null and (repeateSO.isActivate=true and repeateSO.ispendingapproval=false) and repeateSO.startDate<= ? and repeateSO.nextDate <= ? and (repeateSO.expireDate is null or repeateSO.expireDate >= ?)";
        List list = executeQuery( query, new Object[]{currentDate, currentDate,currentDate});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getRepeateSalesOrderDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String parentSOId = (String) requestParams.get("parentSOId");
        String query = "from SalesOrder where parentSO.ID = ? ";
        List list = executeQuery( query, new Object[]{parentSOId});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getRepeateSalesOrderDetailsForExpander(HashMap<String, Object> requestParams) throws ServiceException {
        String parentSOId = (String) requestParams.get("parentSOId");
        String companyid = (String) requestParams.get("companyid");
        String selQuery = "SELECT id,sonumber from salesorder where company =? and parentso = ?";
        List list = executeSQLQuery(selQuery, new Object[]{companyid,parentSOId});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getSodForProduct(HashMap<String, Object> request) throws ServiceException {
        List list = new ArrayList();
        try {

            DateFormat df = (DateFormat) request.get(Constants.df);
            ArrayList params = new ArrayList();
            String productId = (String) request.get("productId");
            String companyid = (String) request.get("companyid");
            params.add(productId);
            params.add(companyid);
            params.add(false);
            params.add(false);
            params.add(false);
            params.add(11);
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            String condition = "";
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                condition += " and (sod.salesOrder.orderDate >=? and sod.salesOrder.orderDate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            String query = " select sod from SalesOrderDetail sod where sod.product.ID=? and sod.company.companyID=? and "
                    + " sod.salesOrder.deleted= ? and sod.salesOrder.isOpeningBalanceSO= ?  and sod.salesOrder.isconsignment= ? "
                    + " and sod.salesOrder.istemplate != 2  and sod.salesOrder.approvestatuslevel = ? "+condition;// and pod.salesOrder.deleted=false";
            list = executeQuery( query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getSodForProduct : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getSO_Product(Map<String, Object> requestMap) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String productId = "";
        String companyId = "";
        boolean isApproved=false;
        DateFormat df = null;
        String startdate = "";
        String enddate = "";
        if (requestMap.containsKey("productId")) {
            productId = requestMap.get("productId").toString();
            params.add(productId);
        }
        if (requestMap.containsKey("companyId")) {
            companyId = requestMap.get("companyId").toString();
            params.add(companyId);
        }
        if (requestMap.containsKey("df")) {
            try {
                df = (DateFormat) requestMap.get("df");
                if (requestMap.containsKey("startdate")) {
                    startdate = requestMap.get("startdate").toString();
                }
                if (requestMap.containsKey("enddate")) {
                    enddate = requestMap.get("enddate").toString();
                }
                condition += " and (sod.salesOrder.orderDate >=? and sod.salesOrder.orderDate <=?)";
                params.add(df.parse(startdate));
                params.add(df.parse(enddate));
            } catch (ParseException ex) {
                Logger.getLogger(accSalesOrderImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (requestMap.containsKey("isApproved")) {
            isApproved = Boolean.parseBoolean(requestMap.get("isApproved").toString());
            if (isApproved) {
                condition += " and sod.salesOrder.approvestatuslevel=?";
                params.add(11);
            }
        }
        String q = "from SalesOrderDetail sod where product.ID=? and sod.company.companyID=? and sod.salesOrder.deleted=false" + condition;
        list = executeQuery(q, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getAssemblySubProductlist(String productid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from ProductAssembly where product.ID=?";
        list = executeQuery( q, new Object[]{productid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    //for feting serial no of batch

    public KwlReturnObject getSerialForBatch(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from BatchSerial";
        return buildNExecuteQuery( query, requestParams);
    }
//for checking sales order used in invoice or not

    public KwlReturnObject getSOforinvoice(String soid, String companyid, boolean includeSoftDeletedSO) throws ServiceException {
        List list = new ArrayList();
        String attachSoftDeleteString = "";
        if (!includeSoftDeletedSO) {
            attachSoftDeleteString += " and ge.invoice.deleted=false ";
        }
        String q = "from InvoiceDetail ge where ge.salesorderdetail.salesOrder.ID = ? and ge.company.companyID=? " + attachSoftDeleteString;
        list = executeQuery( q, new Object[]{soid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    //select consignmentapprovalstatus from newbatchserial where id in(select serialid from serialdocumentmapping where documentid="ff8080814d2e74fd014d2e7f41950008");

    public KwlReturnObject getSerialsFormDocumentid(String soid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from SerialDocumentMapping sd where sd.documentid=? and sd.serialid.company.companyID=? ";//from NewBatchSerial bs where bs.id in(
        list = executeQuery( q, new Object[]{soid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    //for checking sales order used in invoice or not

    public KwlReturnObject getDOforinvoice(String soid, String companyid, boolean includeSoftDeletedDO) throws ServiceException {
        List list = new ArrayList();
        String attachSoftDeleteString = "";
        if (!includeSoftDeletedDO) {
            attachSoftDeleteString += " and de.deliveryOrder.deleted=false ";
        }
        String q = "from DeliveryOrderDetail de where de.sodetails.salesOrder.ID = ? and de.company.companyID=? " + attachSoftDeleteString;
        list = executeQuery( q, new Object[]{soid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

//for cheching Customer Quotation used in invoice or not
    public KwlReturnObject getQTforinvoice(String qid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from InvoiceDetail ge where ge.quotationDetail.quotation.ID= ? and ge.company.companyID=? and ge.invoice.deleted=false";
        list = executeQuery( q, new Object[]{qid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getSOforQT(String qid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from SalesOrderDetail sod where sod.quotationDetail.quotation.ID= ? and sod.company.companyID=? and sod.salesOrder.deleted=false";
        list = executeQuery( q, new Object[]{qid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getPOforSO(String qid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "select salesorderdetailid from podetails po where po.salesorderdetailid in (select sod.id from salesorder as so  inner join sodetails as sod on sod.salesorder=so.id where so.id= ? and so.company= ?)";
        list = executeSQLQuery( q, new Object[]{qid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getSalesOrderDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from SalesOrderDetail";
        return buildNExecuteQuery( query, requestParams);
    }
    
    @Override
    public KwlReturnObject getSalesOrderByProduct(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String companyid = (String) request.get(Constants.companyKey);
            String start = (String) request.get(Constants.start);
            String limit = (String) request.get(Constants.limit);
            DateFormat df = (DateFormat) request.get(Constants.df);
            String productid = (String) request.get("productid");
            String productCategoryid = "";
            if (request.containsKey(Constants.productCategoryid) && request.get(Constants.productCategoryid) != null) {
                productCategoryid = (String) request.get(Constants.productCategoryid);
            }
            String ss = (String) request.get(Constants.ss);
            
            ArrayList params = new ArrayList();

            String conditionSQL = "";
            String conditionSQLForNotLease = "";
            params.add(companyid);
            if (!StringUtil.isNullOrEmpty(productid) && !StringUtil.equal(productid, "-1") && !StringUtil.equal(productid, "All")) {
                productid = AccountingManager.getFilterInString(productid);
                conditionSQL += " and sodetails.product in " + productid + "  ";
            }

            if (!StringUtil.isNullOrEmpty(productCategoryid) && !StringUtil.equal(productCategoryid, "-1")) {
                params.add(productCategoryid);
                conditionSQL += " and sodetails.product in (select productid from productcategorymapping where productcategory = ?)  ";
            }

            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                conditionSQL += " and (salesorder.orderdate >=? and salesorder.orderdate <=?) ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            
            if (StringUtil.isNullOrEmpty(ss) == false) {
                for (int i = 0; i < 4; i++) {
                    params.add("%" +ss + "%");
                }
                conditionSQL += " and (salesorder.sonumber like ? or product.name like ? or sodetails.description like ? or customer.name like ? ) ";
            }
            //condition for Non Lease sales order records
              conditionSQLForNotLease += " and salesorder.leaseOrMaintenanceSO=0 ";
             
            String mysqlQuery = "select salesorder.id,  'false' as withoutinventory, salesorder.createdon , sodetails.id as sodid   from salesorder  "
                    + " inner join sodetails on sodetails.salesorder = salesorder.id "
                    + " inner join customer on customer.id = salesorder.customer "
                    + " inner join product on product.id = sodetails.product "
                    + " where salesorder.company = ? and salesorder.deleteflag='F' and salesorder.approvestatuslevel=11 and salesorder.isdraft = false " + conditionSQL + conditionSQLForNotLease + " "
                    + "order by customer.name, salesorder.sonumber asc";
            list = executeSQLQuery( mysqlQuery, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeSQLQueryPaging( mysqlQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accSalesOrderImpl.getSalesOrderByProduct:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }
    
    public KwlReturnObject getCRPendingApprovalSalesOrderDetails(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            DateFormat df = (DateFormat) request.get(Constants.df);
            String start = (String) request.get(Constants.start);
            String limit = (String) request.get(Constants.limit);
            String ss = (String) request.get(Constants.ss);
            String requestorid = (String) request.get("requestorid");
            String storeids = (String) request.get("storeids");
            boolean isRejectedItemsOnly = (Boolean) request.get("isRejectedItemsOnly");
            ArrayList params = new ArrayList();
            params.add((String) request.get(Constants.companyKey));
            String condition = " and sod.salesOrder.deleted=false and sod.company.companyID=?";
            
            if (!StringUtil.isNullOrEmpty(ss)) {
                for (int i = 0; i <2; i++) {
                    params.add(ss + "%");
                }
                condition += " and ( sod.salesOrder.salesOrderNumber like ? or sod.product.name like ? )";
            }
//           
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                condition += " and (sod.salesOrder.orderDate >=? and sod.salesOrder.orderDate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }//,map.batchmapid  , LocationBatchDocumentMapping map sod.ID=map.documentid  and
            
            if (!StringUtil.isNullOrEmpty(storeids)) {
                String storeidsrec = AccountingManager.getFilterInString(storeids);
                condition +=  " and sdmap.serialid.batch.warehouse.id in " + storeidsrec + " ";
            }
            if (!StringUtil.isNullOrEmpty(requestorid)) {
                condition += "and (approver.userID= ? or sod.salesOrder.createdby.userID= ? )";//.serialid
                params.add(requestorid);
                params.add(requestorid);
            }
            if (isRejectedItemsOnly) {
                condition += " and sdmap.requestApprovalStatus=? ";  //serialid.
                params.add(RequestApprovalStatus.REJECTED);
            } else {
                condition += " and sod.salesOrder.freeze=false and sdmap.requestApprovalStatus=? ";  //serialid.
                params.add(RequestApprovalStatus.PENDING);
            }
            condition += " and sod.salesOrder.istemplate != 2  and sod.salesOrder.isconsignment='T' ";//INNER JOIN sdmap.approverSet approver
            String query = "select distinct sod,sdmap from SerialDocumentMapping sdmap INNER JOIN sdmap.approverSet approver,SalesOrderDetail sod where  sod.ID=sdmap.documentid and sdmap.documentid is not null " + condition;           
           list = executeQuery( query, params.toArray());    
           count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {  //sod.salesOrder.pendingapproval= 0 and
                list = executeQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getCRPendingApprovalRequestDetails: " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
//    public KwlReturnObject getCRPendingApprovalSalesOrderDetailsModified(HashMap<String, Object> request) throws ServiceException {
//        List list = null;
//        int count = 0;
//        try {
//            DateFormat df = (DateFormat) request.get(Constants.df);
//            String start = (String) request.get(Constants.start);
//            String limit = (String) request.get(Constants.limit);
//            String ss = (String) request.get(Constants.ss);
//            String requestorid = (String) request.get("requestorid");
//            String storeids = (String) request.get("storeids");
//            boolean isRejectedItemsOnly = (Boolean) request.get("isRejectedItemsOnly");
//            ArrayList locationMappingparams = new ArrayList();
//            ArrayList serialMappingparams = new ArrayList();
//            ArrayList params = new ArrayList();
//            
//            locationMappingparams.add((String) request.get(Constants.companyKey));
//            serialMappingparams.add((String) request.get(Constants.companyKey));
////            String condition = " and sod.salesOrder.deleted=false and sod.company.companyID=?";
//            String locationMappingcondition = " and so.deleteflag=false and so.company= ? ";
//            String serialMappingcondition = " and so.deleteflag=false and so.company= ? ";
//            
//            if (!StringUtil.isNullOrEmpty(ss)) {
//                for (int i = 0; i <2; i++) {
//                    locationMappingparams.add(ss + "%");
//                    serialMappingparams.add(ss + "%");
//                }
////                condition += " and ( sod.salesOrder.salesOrderNumber like ? or sod.product.name like ? )";
//                locationMappingcondition += " and ( so.sonumber like ? or p.name like ? )";
//                serialMappingcondition += " and ( so.sonumber like ? or p.name like ? )";
//            }
////           
//            String startDate = (String) request.get(Constants.REQ_startdate);
//            String endDate = (String) request.get(Constants.REQ_enddate);
//            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
////                condition += " and (sod.salesOrder.orderDate >=? and sod.salesOrder.orderDate <=?)";
//                locationMappingcondition += " and ( so.orderdate >= ? and so.orderdate <= ? )";
//                serialMappingcondition += " and ( so.orderdate >= ? and so.orderdate <= ? )";
//                locationMappingparams.add(df.parse(startDate));
//                locationMappingparams.add(df.parse(endDate));
//                serialMappingparams.add(df.parse(startDate));
//                serialMappingparams.add(df.parse(endDate));
//            }//,map.batchmapid  , LocationBatchDocumentMapping map sod.ID=map.documentid  and
//            
//            if (!StringUtil.isNullOrEmpty(storeids)) {
//                String storeidsrec = AccountingManager.getFilterInString(storeids);
//                locationMappingcondition +=  " and( store.id in " + storeidsrec + " or so.requestwarehouse in " + storeidsrec + " ) ";
//                serialMappingcondition +=  " and( store.id in " + storeidsrec + " or so.requestwarehouse in " + storeidsrec + " ) ";
//            }
//            if (!StringUtil.isNullOrEmpty(requestorid)) {
//                locationMappingcondition += "and (sodamp.approver= ? or so.createdby= ? )";//.serialid  or lbdamp.approver= ?
//                locationMappingparams.add(requestorid);
////                locationMappingparams.add(requestorid);
//                locationMappingparams.add(requestorid);
//                serialMappingcondition += "and (sodamp.approver= ? or sdamp.approver= ? or so.createdby= ? )";//.serialid
//                serialMappingparams.add(requestorid);
//                serialMappingparams.add(requestorid);
//                serialMappingparams.add(requestorid);
//            }
//            if (isRejectedItemsOnly) {
//                locationMappingcondition += " and (sod.rejectedbasequantity > 0 )";  //serialid. lbdmap.consignmentapprovalstatus=? or lbdmap.rejectedbasequantity > 0 or
////                locationMappingparams.add(2);//RequestApprovalStatus.REJECTED 
//                serialMappingcondition += " and ( sdmap.consignmentapprovalstatus=? )";  //serialid.or sod.rejectedbasequantity > 0 
//                serialMappingparams.add(2);//RequestApprovalStatus.REJECTED 
////                params.add(2);
//            } else {
//                locationMappingcondition += " and so.freezeflag='F' and ( ((sod.approvedbasequantity+sod.rejectedbasequantity) < sod.baseuomquantity) ) ";  //sod.consignmentapprovalstatus= ?  serialid.  lbdmap.consignmentapprovalstatus=? or ((lbdmap.approvedbasequantity+lbdmap.rejectedbasequantity) <= lbdmap.quantity) or
////                locationMappingparams.add(0);//RequestApprovalStatus.PENDING
////                locationMappingparams.add(0);
//                serialMappingcondition += " and so.freezeflag='F' and ( sdmap.consignmentapprovalstatus=? ) ";  //serialid.  or sod.consignmentapprovalstatus= ? 
//                serialMappingparams.add(0);//RequestApprovalStatus.PENDING
////                serialMappingparams.add(0);
//            }
//            
//            params.addAll(locationMappingparams);
//            params.addAll(serialMappingparams);
//            
////            condition += " and so.istemplate != 2  and so.isconsignment='T' ";
//             
////            String query = " select distinct sod.id as sodid,sdmap.id as mapid,lbdmap.id as locmapid from sodetails sod "
////                    + " inner join salesorder so on sod.salesorder=so.id "
////                    + " left join serialdocumentmapping sdmap on sod.id=sdmap.documentid "
////                    + " left join serialdocumentapprovermapping sdamp on sdmap.id=sdamp.serialdocumentmapping "
////                    + " left join locationbatchdocumentmapping lbdmap on sod.id=lbdmap.documentid "
////                    + " left join locationbatchdocumentapprovermapping lbdamp on lbdmap.id=lbdamp.locationmapping "
////                    + " left join sodetailsapprovermapping sodamp on sod.id=sodamp.sodetails "
////                    + " left join newbatchserial nbs on  sdmap.serialid=nbs.id "
////                    + " left join newproductbatch productbatch on nbs.batch=productbatch.id "
//////                    + " left join users user1 on sodamp.approver=user1.userid "
//////                    + " left join users user2 on sdmap.approver=user2.userid "
//////                    + " left join users user3 on so.createdby=user3.userid "
////                    + " left join inventorywarehouse store on productbatch.warehouse=store.id "
////                    + " left join product p on p.id=sod.product "
////                    + " where so.istemplate != 2  and so.isconsignment='T'  and  nbs.id is null "+ condition;
//            
//                         //Queary for Locationmapping
//            String query = " select distinct sod.id as sodid, null as mapid,null as locmapid from sodetails sod  "
//                    + " inner join salesorder so on sod.salesorder=so.id  "
//                    + " left join serialdocumentmapping sdmap on sod.id=sdmap.documentid  "
////                    + " left join serialdocumentapprovermapping sdamp on sdmap.id=sdamp.serialdocumentmapping  "
////                    + " left join locationbatchdocumentmapping lbdmap on sod.id=lbdmap.documentid  "
////                    + " left join locationbatchdocumentapprovermapping lbdamp on lbdmap.id=lbdamp.locationmapping  "
//                    + " left join sodetailsapprovermapping sodamp on sod.id=sodamp.sodetails  "
//                    + " left join newbatchserial nbs on  sdmap.serialid=nbs.id "
//                    + " left join newproductbatch productbatch on nbs.batch=productbatch.id "
//                    + " left join inventorywarehouse store on productbatch.warehouse=store.id  "
//                    + " left join product p on p.id=sod.product  where so.istemplate != 2  "
//                    + " and so.isconsignment='T' and nbs.id is null "+locationMappingcondition
//                    + " union "  //Queary for SerialDocument Mapping
//                    +"  select distinct sod.id as sodid,sdmap.id as mapid, null as locmapid from sodetails sod "
//                    + " inner join salesorder so on sod.salesorder=so.id  "
//                    + " left join serialdocumentmapping sdmap on sod.id=sdmap.documentid "
//                    + " left join serialdocumentapprovermapping sdamp on sdmap.id=sdamp.serialdocumentmapping "
//                    + " left join sodetailsapprovermapping sodamp on sod.id=sodamp.sodetails  "
//                    + " left join newbatchserial nbs on  sdmap.serialid=nbs.id  "
//                    + " left join newproductbatch productbatch on nbs.batch=productbatch.id  "
//                    + " left join inventorywarehouse store on productbatch.warehouse=store.id  "
//                    + " left join product p on p.id=sod.product  where so.istemplate != 2  "
//                    + " and so.isconsignment='T' and nbs.id is not null "+serialMappingcondition;
//                    
//            
//           //INNER JOIN sdmap.approverSet approver
//                       
//            //"select distinct sod from SalesOrderDetail sod,SerialDocumentMapping sdmap INNER JOIN sdmap.approverSet approver  where ( ( sod.ID=sdmap.documentid and sdmap.documentid is not null ) or ( sod.salesOrder.autoapproveflag='T' ) ) " 
//           list = executeSQLQuery( query, params.toArray());    
//           count = list.size();
//            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {  //sod.salesOrder.pendingapproval= 0 and
//                list = executeSQLQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
//            }
//        } catch (Exception ex) {
//            throw ServiceException.FAILURE("accSalesOrderImpl.getCRPendingApprovalRequestDetails: " + ex.getMessage(), ex);
//        }
//        return new KwlReturnObject(true, "", null, list, count);
//    }
    public KwlReturnObject getCRPendingApprovalSalesOrderDetailsModified(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            DateFormat df = (DateFormat) request.get(Constants.df);
            String start = (String) request.get(Constants.start);
            String limit = (String) request.get(Constants.limit);
            String ss = (String) request.get(Constants.ss);
            String requestorid = (String) request.get("requestorid");
            String storeids = (String) request.get("storeids");
            String orderBy="";
            boolean isRejectedItemsOnly = (Boolean) request.get("isRejectedItemsOnly");
//            ArrayList locationMappingparams = new ArrayList();
            ArrayList params = new ArrayList();
            
            params.add((String) request.get(Constants.companyKey));        
            String locationMappingcondition = " and so.deleteflag=false and so.company= ? ";
            
            if (!StringUtil.isNullOrEmpty(ss)) {
                for (int i = 0; i <2; i++) {
                    params.add(ss + "%");
                }
//                condition += " and ( sod.salesOrder.salesOrderNumber like ? or sod.product.name like ? )";
                locationMappingcondition += " and ( so.sonumber like ? or p.name like ? )";
//                serialMappingcondition += " and ( so.sonumber like ? or p.name like ? )";
            }
//           
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
//                condition += " and (sod.salesOrder.orderDate >=? and sod.salesOrder.orderDate <=?)";
                locationMappingcondition += " and ( so.orderdate >= ? and so.orderdate <= ? )";       
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
//                serialMappingparams.add(df.parse(startDate));
//                serialMappingparams.add(df.parse(endDate));
            }//,map.batchmapid  , LocationBatchDocumentMapping map sod.ID=map.documentid  and
            
            if (!StringUtil.isNullOrEmpty(storeids)) {
                String storeidsrec = AccountingManager.getFilterInString(storeids);
                locationMappingcondition +=  " and so.requestwarehouse in " + storeidsrec + " "; //( store.id in " + storeidsrec + "
//                serialMappingcondition +=  " and( store.id in " + storeidsrec + " or so.requestwarehouse in " + storeidsrec + " ) ";
            }
            if (!StringUtil.isNullOrEmpty(requestorid)) {
                locationMappingcondition += "and (sodamp.approver= ? or ( so.createdby= ? and sodamp.approver is not null))";//.serialid  or lbdamp.approver= ?
                params.add(requestorid);
                params.add(requestorid);        
            }
            if (isRejectedItemsOnly) {
                locationMappingcondition += " and (sod.rejectedbasequantity > 0 )";  //serialid. lbdmap.consignmentapprovalstatus=? or lbdmap.rejectedbasequantity > 0 or
            } else {
                locationMappingcondition += " and so.freezeflag='F' and ( ((sod.approvedbasequantity+sod.rejectedbasequantity) < sod.baseuomquantity) ) ";  //sod.consignmentapprovalstatus= ?  serialid.  lbdmap.consignmentapprovalstatus=? or ((lbdmap.approvedbasequantity+lbdmap.rejectedbasequantity) <= lbdmap.quantity) or
            }
            
//            params.addAll(locationMappingparams);
                       orderBy="order by so.createdon desc,so.sonumber desc";
                        
                        
            String query = " select distinct sod.id as sodid from sodetails sod  "
                    + " inner join salesorder so on sod.salesorder=so.id  "
                    + " left join sodetailsapprovermapping sodamp on sod.id=sodamp.sodetails  "
                    + " left join product p on p.id=sod.product  where so.istemplate != 2  "
                    + " and so.isconsignment='T' "+locationMappingcondition+orderBy;
           list = executeSQLQuery( query, params.toArray());    
           count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {  //sod.salesOrder.pendingapproval= 0 and
                list = executeSQLQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getCRPendingApprovalRequestDetails: " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject getSalesOrderDetailsVendorMapping(String sodid) throws ServiceException {
        List list = new ArrayList();
        String q = "from SODetailsVendorMapping sod where sod.ID= ? ";
        list = executeQuery( q, new Object[]{sodid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getQuotationDetailsVendorMapping(String qdid) throws ServiceException {
        List list = new ArrayList();
        String q = "from QuotationDetailsVendorMapping sod where sod.ID= ? ";
        list = executeQuery( q, new Object[]{qdid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public int DelRepeateSOMemo(String repeateid) throws ServiceException {
        String query = "DELETE FROM RepeatedJEMemo RM WHERE RM.RepeatedSOID= ? ";
        int numRows = executeUpdate( query, new Object[]{repeateid});
        return numRows;
    }

    public KwlReturnObject saveRepeateSOMemo(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            RepeatedJEMemo rJEMemo = new RepeatedJEMemo();
            if (dataMap.containsKey("id")) {
                rJEMemo = (RepeatedJEMemo) get(RepeatedJEMemo.class, (String) dataMap.get("id"));
            }

            if (dataMap.containsKey("RepeatedSOID")) {
                rJEMemo.setRepeatedSOID((String) dataMap.get("RepeatedSOID"));
            }
            if (dataMap.containsKey("no")) {
                rJEMemo.setCount((Integer) dataMap.get("no"));
            }
            if (dataMap.containsKey("memo")) {
                rJEMemo.setMemo((String) dataMap.get("memo"));
            }
            saveOrUpdate(rJEMemo);
            list.add(rJEMemo);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveRepeateJEMemo : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject getOutstandingSalesOrders(HashMap<String, Object> request) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {

            DateFormat df = (DateFormat) request.get(Constants.df);
            String start = (String) request.get(Constants.start);
            String limit = (String) request.get(Constants.limit);
            String ss = (String) request.get(Constants.ss);
            boolean isfavourite = false;
            boolean isOpeningBalanceOrder = false;
            boolean isConsignment = false;
            String moduleid = "";
            String companyId = (String) request.get(Constants.companyKey);
            if (request.containsKey(Constants.moduleid) && request.get(Constants.moduleid) != null) {
                moduleid = request.get(Constants.moduleid).toString();
            }
            if (request.get(Constants.MARKED_FAVOURITE) != null) {
                isfavourite = Boolean.parseBoolean((String) request.get(Constants.MARKED_FAVOURITE));
            }
            boolean isJobWorkOrderReciever = false;
            if (request.containsKey("isJobWorkOrderReciever") && request.get("isJobWorkOrderReciever") != null) {
                isJobWorkOrderReciever = (Boolean) request.get("isJobWorkOrderReciever");
            }
            if (request.get("isOpeningBalanceOrder") != null) {
                isOpeningBalanceOrder = (Boolean) request.get("isOpeningBalanceOrder");
            }
            if (request.containsKey("isConsignment") && request.get("isConsignment") != null) {
                isConsignment = (Boolean) request.get("isConsignment");
            }
            boolean pendingapproval = (Boolean) request.get("pendingapproval");
            String newcustomerid = "";
            if (request.containsKey(Constants.newcustomerid) && request.get(Constants.newcustomerid) != null) {
                newcustomerid = (String) request.get(Constants.newcustomerid);
            }
            String productid = "";
            if (request.containsKey(Constants.productid) && request.get(Constants.productid) != null) {
                productid = (String) request.get(Constants.productid);
            }
            /**
             * This flag is true when we select Outstanding SO filter in Sales Order Report. 
             */
            boolean isOutstanding = false;
            if (request.containsKey("isOutstanding") && request.get("isOutstanding") != null) {
                isOutstanding = (Boolean) request.get("isOutstanding");
            }
            /**
             * This flag is true when we select Outstanding SO(s) for Invoicing filter in Sales Order Report. 
             */
            boolean isPendingInvoiced = false;
            if (request.containsKey("isPendingInvoiced") && request.get("isPendingInvoiced") != null) {
                isPendingInvoiced = (Boolean) request.get("isPendingInvoiced");
            }
            boolean isOutstandingproduct = false;
            if (request.containsKey("isOuststandingproduct") && request.get("isOuststandingproduct") != null) {
                isOutstandingproduct = (Boolean) request.get("isOuststandingproduct");
            }
            boolean checkServiceProductFlag = false;
            if (request.containsKey("checkServiceProductFlag") && request.get("checkServiceProductFlag") != null) {
                checkServiceProductFlag = (Boolean) request.get("checkServiceProductFlag");
            }
            String userID = "";
            boolean isenableSalesPersonAgentFlow = false;
            if (request.containsKey("enablesalespersonagentflow") && request.get("enablesalespersonagentflow") != null && !StringUtil.isNullOrEmpty(request.get("enablesalespersonagentflow").toString())) {
                isenableSalesPersonAgentFlow = Boolean.parseBoolean(request.get("enablesalespersonagentflow").toString());
            }
            if (isenableSalesPersonAgentFlow) {
                if (request.containsKey("userid") && request.get("userid") != null && !StringUtil.isNullOrEmpty(request.get("userid").toString())) {
                    userID = (String) request.get("userid");
                }
            }
            ArrayList params = new ArrayList();
            ArrayList paramsWithTradingFlow = new ArrayList();
            params.add((String) request.get(Constants.companyKey));
            paramsWithTradingFlow.add((String) request.get(Constants.companyKey));

            String condition = " where deleted=false and company.companyID=?";
            String conditionSQL = " where salesorder.deleteflag='F' and salesorder.company = ?";

               if (!StringUtil.isNullOrEmpty(ss)) {
                for (int i = 0; i < 5; i++) {
                       params.add("%" + ss + "%");
                       paramsWithTradingFlow.add("%" + ss + "%");
                }
                condition += " and (salesOrderNumber like ? or salesorder.memo like ? or customer.name like ? )";
                conditionSQL += " and (salesorder.sonumber like ? or salesorder.memo like ? or customer.name like ? or product.name like ? or product.productid like ? )";
            }

            //// query based on CostCenter parameter
            String costCenterId = (String) request.get(CCConstants.REQ_costCenterId);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                params.add(costCenterId);
                paramsWithTradingFlow.add(costCenterId);

                condition += " and salesorder.costcenter.ID=?";
                conditionSQL += " and salesorder.costcenter=?";
            }
            if (!StringUtil.isNullOrEmpty(newcustomerid)) {
                params.add(newcustomerid);
                paramsWithTradingFlow.add(newcustomerid);
                condition += " and salesorder.customer = ? ";
                conditionSQL += " and  salesorder.customer = ? ";

            }
            //// query based on start date & end date parameters
            String startDate = (String) request.get(Constants.REQ_startdate)!=null ? StringUtil.DecodeText((String) request.get(Constants.REQ_startdate)) : null;
            String endDate = (String) request.get(Constants.REQ_enddate)!=null ? StringUtil.DecodeText((String) request.get(Constants.REQ_enddate)) : null;
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                condition += " and (salesorder.orderDate >=? and salesorder.orderDate <=? )";
                conditionSQL += " and (salesorder.orderdate >=? and salesorder.orderdate <=? )";

                params.add(df.parse(startDate));
                params.add(df.parse(endDate));

                paramsWithTradingFlow.add(df.parse(startDate));
                paramsWithTradingFlow.add(df.parse(endDate));
            }

            if (request.containsKey("salesPersonFilterFlag") && (Boolean) request.get("salesPersonFilterFlag") && request.get("userId") != null) {
               String userId = (String) request.get("userId");
                if (!StringUtil.isNullOrEmpty(userId)) {
                    DataFilteringModule dataFilteringModule = null;
                    MasterItem masterItem = null;
                    List<DataFilteringModule> dataFilteringModuleList = new ArrayList<DataFilteringModule>();
                    List<MasterItem> masterItems = new ArrayList<MasterItem>();

                    dataFilteringModuleList = find("from DataFilteringModule where user.userID='" + userId + "' and company.companyID='" + companyId + "'");
                    masterItems = find("from MasterItem where user='" + userId + "' and company.companyID='" + companyId + "' and masterGroup.ID='" + 15 + "'");
                    if (!dataFilteringModuleList.isEmpty()) {
                        dataFilteringModule = dataFilteringModuleList.get(0);
                    }
                    if (!masterItems.isEmpty()) {
                        masterItem = masterItems.get(0);
                    }
                    if ((dataFilteringModule != null && !dataFilteringModule.isSalesOrder()) || (dataFilteringModule != null && !dataFilteringModule.isSalesOrder() && masterItem != null)) {
                        conditionSQL += " and ( ";
                    }

                    if (dataFilteringModule != null && !dataFilteringModule.isSalesOrder()) {
                        paramsWithTradingFlow.add(dataFilteringModule.getUser().getUserID());
                        conditionSQL += "salesorder.createdby=? ";
                    }

                    if (dataFilteringModule != null && !dataFilteringModule.isSalesOrder() && masterItem != null) {
                        paramsWithTradingFlow.add(masterItem.getID());
                        conditionSQL += " or salesorder.salesperson=? ";
                    }
                    if ((dataFilteringModule != null && !dataFilteringModule.isSalesOrder()) || (dataFilteringModule != null && !dataFilteringModule.isSalesOrder() && masterItem != null)) {
                        conditionSQL += " ) ";
                    }
                }
            }
            //            }
            if (isenableSalesPersonAgentFlow && !StringUtil.isNullOrEmpty(userID)) {//this block is executed only when owners restriction feature is on 
                String salesPersonID = (String) request.get("salesPersonid");
                String salespersonQuery = "";
                 if (!StringUtil.isNullOrEmpty(salesPersonID)) {
                   salesPersonID= AccountingManager.getFilterInString(salesPersonID);
                    salespersonQuery = "  salesorder.salesperson in " + salesPersonID + " or ";
                }
                
                conditionSQL += " and ( " + salespersonQuery + "  salesorder.createdby='" + userID + "' or salesorder.salesperson is null  ) ";
            }
//            paramsWithTradingFlow.add((String) request.get(Constants.companyKey));

//            String companyId=(String) request.get(Constants.companyKey);
//            if(request.containsKey("salesPersonFilterFlag")&&(Boolean)request.get("salesPersonFilterFlag")&&request.get("userId")!=null){
//                        String userId = (String) request.get("userId");
//                        if(!StringUtil.isNullOrEmpty(userId)){
//                            DataFilteringModule dataFilteringModule =null;
//                            MasterItem masterItem =null;
//                            List<DataFilteringModule> dataFilteringModuleList=new ArrayList<DataFilteringModule>();
//                            List<MasterItem> masterItems=new ArrayList<MasterItem>();
//             
//                            dataFilteringModuleList=find("from DataFilteringModule where user.userID='"+userId+"' and company.companyID='"+companyId+"'");
//                            masterItems=find("from MasterItem where user='"+userId+"' and company.companyID='"+companyId+"' and masterGroup.ID='"+15+"'");
//                            if(!dataFilteringModuleList.isEmpty()){
//                                dataFilteringModule = dataFilteringModuleList.get(0);
//                            }
//                            if(!masterItems.isEmpty()){
//                                masterItem=masterItems.get(0);
//                            }
//                            if((dataFilteringModule != null&&dataFilteringModule.isSalesOrder())||(dataFilteringModule != null&&dataFilteringModule.isSalesOrder()&&masterItem!=null)){
//                                conditionSQL += " and ( ";
//                            }
//                            
//                            if(dataFilteringModule != null&&dataFilteringModule.isSalesOrder()){
//                                paramsWithTradingFlow.add(dataFilteringModule.getUser().getUserID());
//                                conditionSQL += "salesorder.createdby=? ";
//                            }
//                            
//                            if(dataFilteringModule != null&&dataFilteringModule.isSalesOrder()&&masterItem!=null){
//                                paramsWithTradingFlow.add(masterItem.getID());
//                                conditionSQL += " or salesorder.salesperson=? ";
//                            }
//                            if((dataFilteringModule != null&&dataFilteringModule.isSalesOrder())||(dataFilteringModule != null&&dataFilteringModule.isSalesOrder()&&masterItem!=null)){
//                                conditionSQL += " ) ";
//                            }
//                    } 
//            }      
            if (isfavourite) {
                conditionSQL += " and salesorder.favouriteflag=1 ";
            }

            if (isOpeningBalanceOrder) {
                conditionSQL += " and salesorder.isopeningbalenceso=true ";
            } else {
                conditionSQL += " and salesorder.isopeningbalenceso=false ";
            }

            /* pendingapproval->false when called from main tab
             approvestatuslevel=11 ->Not pending for approval
             approvestatuslevel!=11->Pending for approval
             */
            if (pendingapproval) {
                paramsWithTradingFlow.add(11);
                conditionSQL += " and salesorder.approvestatuslevel != ? ";
            } else {
                paramsWithTradingFlow.add(11);
                conditionSQL += " and salesorder.approvestatuslevel = ? ";
            }
           
                    
            if (isOutstanding || isOutstandingproduct) {
                StringBuilder conditionString = new StringBuilder();
                conditionString.append(" and (sod.balanceqty != 0 ");//Fetching only those SO's whose balance qty > 0 
                //ERM-1123:service item to be checked if undelivered service item is checked from companypreferences
                if (!checkServiceProductFlag) {
                    conditionString.append(" and product.producttype != '4efb0286-5627-102d-8de6-001cc0794cfa' ");
                }
                conditionString.append(" )");
                conditionSQL += conditionString.toString();
            }
            if(isPendingInvoiced){
                conditionSQL += " and (salesorder.linkflag = 0 or salesorder.linkflag = 1) and salesorder.isopen='T'";//Fetching only those SO's which are still available for invoicing, it may be partially invoiced or not invoiced at all.
            }
 
            if (isJobWorkOrderReciever) {
                conditionSQL += " and  salesorder.isjobworkorder='T' ";
            } else {
                conditionSQL += " and  salesorder.isjobworkorder='F' ";
            }
            //fetch isDraft flag from request
            boolean isDraft = false;
            if (request.containsKey(Constants.isDraft) && request.get(Constants.isDraft) != null) {
                isDraft = (Boolean) request.get(Constants.isDraft);
            }
            //Append isDraft condition in query for fetching particular transactions
            if (isDraft) {
                conditionSQL += " and salesorder.isdraft = true ";
            } else {
                conditionSQL += " and salesorder.isdraft = false ";
            }
            /* Manually closed SO is not fetching in outstanding SO report*/
            conditionSQL += " and salesorder.issoclosed = 'F' ";

            //Ignore POs created as only templates.
            conditionSQL += " and salesorder.istemplate != 2 ";
            
            if (!StringUtil.isNullOrEmpty(productid)) {
                conditionSQL += " and sod.product = '" + productid + "' ";
            }
            
            String searchJoin = "";
            boolean isCustomFieldAdvSearch = false;
            String appendCase = "and";
            String Searchjson = "";
            String mySearchFilterString = "";
            String searchDefaultFieldSQL = "";
            String joinString1 = "";
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (request.containsKey("filterConjuctionCriteria") && request.get("filterConjuctionCriteria") != null) {
                if (request.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            if (request.containsKey("searchJson") && request.get("searchJson") != null) {
                Searchjson = StringUtil.DecodeText(request.get("searchJson").toString());

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
                        Map<String, Object> map = buildSqlDefaultFieldAdvSearch(defaultSearchFieldArray, paramsWithTradingFlow, moduleid, tableArray, filterConjuctionCriteria);
                        searchJoin += map.containsKey("searchjoin") ? map.get("searchjoin") : "";
                        searchJoin += " left join solinking on solinking.docid=salesorder.id and solinking.sourceflag = 1 ";
                        searchDefaultFieldSQL = (String) (map.containsKey("condition") ? map.get("condition") : "");
                    }

                    if (customSearchFieldArray.length() > 0) {   //Advance search case for Custome field
                        isCustomFieldAdvSearch = true;
                        request.put(Constants.Searchjson, Searchjson);
                        request.put(Constants.appendCase, appendCase);
                        request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(request, true).get(Constants.myResult));
                        String innerJoinOnDetailTable = "";
                        if (!searchJoin.contains("sodetails.salesorder")) {
                            innerJoinOnDetailTable = " inner join sodetails on sodetails.salesorder=salesorder.id ";
                        }
                        boolean isInnerJoinAppend = false;
                        if (mySearchFilterString.contains("salesordercustomdata")) {
                            joinString1 = " inner join salesordercustomdata on salesordercustomdata.soID=salesorder.salesordercustomdataref ";
                        }
                        if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "salesorderdetailcustomdata");
                            joinString1 += innerJoinOnDetailTable + " left join salesorderdetailcustomdata on sodetails.id=salesorderdetailcustomdata.soDetailID ";
                            isInnerJoinAppend = true;
                        }
                        if (mySearchFilterString.contains("CustomerCustomData")) {
                            joinString1 += " left join customercustomdata  on customercustomdata.customerId=salesorder.customer ";
                            mySearchFilterString = mySearchFilterString.replaceAll("CustomerCustomData", "customercustomdata");
                        }
                        if (mySearchFilterString.contains("AccJEDetailsProductCustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "sodetailproductcustomdata");
                            joinString1 += (isInnerJoinAppend ? "" : innerJoinOnDetailTable) + " left join sodetailproductcustomdata on sodetails.id=sodetailproductcustomdata.soDetailID ";
                            isInnerJoinAppend = true;
                        }
                        //product custom data
                        if (mySearchFilterString.contains("accproductcustomdata")) {
                            joinString1 += (isInnerJoinAppend ? "" : innerJoinOnDetailTable) + " left join accproductcustomdata on accproductcustomdata.productId=product.id ";
                        }
                        StringUtil.insertParamAdvanceSearchString1(paramsWithTradingFlow, Searchjson);
                    }
                    mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
                }
            }
            
            String orderBy = "";
            String sort_Col = "";
            String sort_Col1 = "";
            String joinString2 = "";
            String[] stringSort = null;
            if (request.containsKey("dir") && request.containsKey("sort")) {
                String Col_Name = request.get("sort").toString();
                String Col_Dir = request.get("dir").toString();
                stringSort = columSort(Col_Name, Col_Dir);
                orderBy += stringSort[0];
                sort_Col += stringSort[1];
                sort_Col1 += stringSort[2];
                if(request.get("sort").toString().equals("salespersonname")){
                   joinString2 += "  left join masteritem on masteritem.id = salesorder.salesperson  ";
                }

            } else {
                if (isConsignment) {
                    stringSort = columSort("billno", "desc");
                    orderBy = " order by orderdate desc,sonumber desc " ;
                    sort_Col +=", salesorder.orderdate " + stringSort[1] ;
                    sort_Col1 +=  ", billingsalesorder.orderdate " + stringSort[2] ;
                } else {
                    orderBy = " order by orderdate desc ";
                    sort_Col += ", salesorder.orderdate ";
                    sort_Col1 += ", billingsalesorder.orderdate ";
                }
            }

            Boolean isTradingFlow = (Boolean) request.get("isTradingFlow");
            String query = "";
            if (isPendingInvoiced) {
                query = " select distinct salesorder.id, 'false' as WithoutInventory, salesorder.orderdate " + sort_Col + " from sodetails as sod "
                        + " inner join salesorder on sod.salesorder = salesorder.id "
                        + " inner join product on sod.product = product.id "
                        + " inner join customer on customer.id = salesorder.customer "
                        + " left join costcenter on costcenter.id = salesorder.costcenter "
                        + " left outer join invoicedetails as invd on invd.salesorderdetail = sod.id "
                        + " left outer join invoice as inv on invd.invoice = inv.id "
                        + joinString1 + joinString2 + conditionSQL + mySearchFilterString
                        + " and ( inv.invoicenumber is null or (inv.deleteflag='F') or (inv.invoicenumber is not null and inv.deleteflag='T')) "
                        + orderBy;
            } else if (isTradingFlow == true) {
                query = " select distinct salesorder.id, 'false' as WithoutInventory, salesorder.orderdate " + sort_Col + " from sodetails as sod "
                        + " inner join salesorder on sod.salesorder = salesorder.id "
                        + " inner join product on sod.product = product.id "
                        + " inner join customer on customer.id = salesorder.customer "
                        + " left join costcenter on costcenter.id = salesorder.costcenter "
                        + " left outer join dodetails as dod on dod.sodetails = sod.id "
                        + " left outer join deliveryorder as do on dod.deliveryorder = do.id "
                        + " left outer join invoicedetails as invd on invd.salesorderdetail = sod.id "
                        + " left outer join invoice as inv on invd.invoice = inv.id "
                        + " left JOIN srdetails as srd on srd.dodetails=dod.id " + joinString1 + joinString2 + conditionSQL + mySearchFilterString
                        + " and ( do.donumber is null or (do.deleteflag='F' and dod.actualquantity != dod.deliveredquantity) or (do.donumber is not null and do.deleteflag='T') "
                        + " or inv.invoicenumber is null or (inv.invoicenumber is not null and inv.deleteflag='T' ) ) " + orderBy;
//                        + " AND salesorder.id not in ( "
//                        + " select salesorder.id "
//                        + " from dodetails as dod "
//                        + "	inner join deliveryorder as do on dod.deliveryorder = do.id "
//                        + "	inner join sodetails as sod on dod.sodetails = sod.id "
//                        + "	inner join salesorder as so on sod.salesorder = salesorder.id "
//                        + " where salesorder.company = ? and do.deleteflag='F' and dod.actualquantity = dod.deliveredquantity )";
            } else {
                query = " select distinct salesorder.id, 'false' as WithoutInventory, salesorder.orderdate " + sort_Col + " from sodetails as sod "
                        + " inner join salesorder on sod.salesorder = salesorder.id "
                        + " inner join product on sod.product = product.id "
                        + " inner join customer on customer.id = salesorder.customer "
                        + " left join costcenter on costcenter.id = salesorder.costcenter "
                        + " left outer join invoicedetails as invd on invd.salesorderdetail = sod.id "
                        + " left outer join invoice as inv on invd.invoice = inv.id " + joinString1 + joinString2 + conditionSQL + mySearchFilterString
                        + " and ( inv.invoicenumber is null or (inv.invoicenumber is not null and inv.deleteflag='T' ) )"
                        + " AND salesorder.id not in (  "
                        + " select salesorder.id "
                        + " from invoicedetails as invd "
                        + " inner join invoice as inv on invd.invoice = inv.id "
                        + " inner join sodetails as sod on invd.salesorderdetail = sod.id "
                        + " inner join salesorder as so on sod.salesorder = salesorder.id  "
                        + " where salesorder.company = ? and inv.deleteflag='F' )" + orderBy;
                paramsWithTradingFlow.add((String) request.get(Constants.companyKey));
            }
//            System.out.println("getOutstandingPurchaseOrders - query:"+query);            
            list = executeSQLQuery( query, paramsWithTradingFlow.toArray());
            count = list.size();
            if (!StringUtil.isNullOrEmpty(start) && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeSQLQueryPaging( query, paramsWithTradingFlow.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("accSalesOrderImpl.getOutstandingSalesOrders : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    // Billing Sales Order
    public KwlReturnObject getBillingSalesOrderDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from BillingSalesOrderDetail";
        return buildNExecuteQuery( query, requestParams);
    }

    public KwlReturnObject getBillingSalesOrderCount(String orderno, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from BillingSalesOrder where salesOrderNumber=? and company.companyID=?";
        list = executeQuery( q, new Object[]{orderno, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject saveBillingSalesOrder(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String bsoid = (String) dataMap.get("id");

            BillingSalesOrder salesOrder = new BillingSalesOrder();
            if (StringUtil.isNullOrEmpty(bsoid)) {
                salesOrder.setDeleted(false);
            } else {
                salesOrder = (BillingSalesOrder) get(BillingSalesOrder.class, bsoid);
            }
            if (dataMap.containsKey(Constants.SEQFORMAT)) {
                salesOrder.setSeqformat((SequenceFormat) get(SequenceFormat.class, (String) dataMap.get(Constants.SEQFORMAT)));
            }
            if (dataMap.containsKey(Constants.SEQNUMBER)) {
                salesOrder.setSeqnumber(Integer.parseInt(dataMap.get(Constants.SEQNUMBER).toString()));
            }
            if (dataMap.containsKey("entrynumber")) {
                salesOrder.setSalesOrderNumber((String) dataMap.get("entrynumber"));
            }
            if (dataMap.containsKey("autogenerated")) {
                salesOrder.setAutoGenerated((Boolean) dataMap.get("autogenerated"));
            }
            if (dataMap.containsKey("memo")) {
                salesOrder.setMemo((String) dataMap.get("memo"));
            }
            if (dataMap.containsKey("posttext")) {
                salesOrder.setPostText((String) dataMap.get("posttext"));
            }
            if (dataMap.containsKey("customerid")) {
                Customer customer = dataMap.get("customerid") == null ? null : (Customer) get(Customer.class, (String) dataMap.get("customerid"));
                salesOrder.setCustomer(customer);
            }
            if (dataMap.containsKey("orderdate")) {
                salesOrder.setOrderDate((Date) dataMap.get("orderdate"));
            }
            if (dataMap.containsKey("duedate")) {
                salesOrder.setDueDate((Date) dataMap.get("duedate"));
            }
            if (dataMap.containsKey("perDiscount")) {
                salesOrder.setPerDiscount((Boolean) dataMap.get("perDiscount"));
            }
            if (dataMap.containsKey("discount")) {
                salesOrder.setDiscount((Double) dataMap.get("discount"));
            }
            if (dataMap.containsKey("shipdate")) {
                salesOrder.setShipdate((Date) dataMap.get("shipdate"));
            }
            if (dataMap.containsKey("shipvia")) {
                salesOrder.setShipvia((String) dataMap.get("shipvia"));
            }
            if (dataMap.containsKey("fob")) {
                salesOrder.setFob((String) dataMap.get("fob"));
            }
//            if (dataMap.containsKey("credito")) {
//                Account account = dataMap.get("credito")==null?null:(Account) get(Account.class, (String) dataMap.get("credito"));
//                salesOrder.setCreditTo(account);
//            }
            if (dataMap.containsKey("taxid")) {
                Tax tax = dataMap.get("taxid") == null ? null : (Tax) get(Tax.class, (String) dataMap.get("taxid"));
                salesOrder.setTax(tax);
            }
            if (dataMap.containsKey("costCenterId")) {
                CostCenter costCenter = dataMap.get("costCenterId") == null ? null : (CostCenter) get(CostCenter.class, (String) dataMap.get("costCenterId"));
                salesOrder.setCostcenter(costCenter);
            } else {
                salesOrder.setCostcenter(null);
            }
            if (dataMap.containsKey("companyid")) {
                Company company = dataMap.get("companyid") == null ? null : (Company) get(Company.class, (String) dataMap.get("companyid"));
                salesOrder.setCompany(company);
            }
            if (dataMap.containsKey("sodetails")) {
                if (dataMap.get("sodetails") != null) {
                    salesOrder.setRows((Set<BillingSalesOrderDetail>) dataMap.get("sodetails"));
                }
            }
            if (dataMap.containsKey("billto")) {
                if (dataMap.get("billto") != null) {
                    salesOrder.setBillTo((String) dataMap.get("billto"));
                }
            }
            if (dataMap.containsKey("shipaddress")) {
                if (dataMap.get("shipaddress") != null) {
                    salesOrder.setShipTo((String) dataMap.get("shipaddress"));
                }
            }

            if (dataMap.containsKey("isfavourite")) {
                if (dataMap.get("isfavourite") != null) {
                    salesOrder.setFavourite(Boolean.parseBoolean(dataMap.get("isfavourite").toString()));
                }
            }
            if (dataMap.containsKey("termid")) {
                Term term = (dataMap.get("termid") == null ? null : (Term) get(Term.class, (String) dataMap.get("termid")));
                salesOrder.setTerm(term);
            }
            if (dataMap.containsKey("currencyid")) {
                salesOrder.setCurrency((KWLCurrency) get(KWLCurrency.class, (String) dataMap.get("currencyid")));
            }
            if (dataMap.containsKey("salesPerson")) {
                MasterItem mi = (dataMap.get("salesPerson") == null ? null : (MasterItem) get(MasterItem.class, (String) dataMap.get("salesPerson")));
                salesOrder.setSalesperson(mi);
            }
            salesOrder.setTemplateid((Projreport_Template) get(Projreport_Template.class, Constants.HEADER_IMAGE_TEMPLATE_ID));



            saveOrUpdate(salesOrder);
            list.add(salesOrder);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveBillingSalesOrder : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject saveBillingSalesOrderDetails(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String bsodid = (String) dataMap.get("id");

            BillingSalesOrderDetail salesOrderDetail = new BillingSalesOrderDetail();
            if (!StringUtil.isNullOrEmpty(bsodid)) {
                salesOrderDetail = (BillingSalesOrderDetail) get(BillingSalesOrderDetail.class, bsodid);
            }

            if (dataMap.containsKey("soid")) {
                BillingSalesOrder salesOrder = dataMap.get("soid") == null ? null : (BillingSalesOrder) get(BillingSalesOrder.class, (String) dataMap.get("soid"));
                salesOrderDetail.setSalesOrder(salesOrder);
            }
            if (dataMap.containsKey("rate")) {
                salesOrderDetail.setRate((Double) dataMap.get("rate"));
            }
            if (dataMap.containsKey("srno")) {
                salesOrderDetail.setSrno((Integer) dataMap.get("srno"));
            }
            if (dataMap.containsKey("quantity")) {
                salesOrderDetail.setQuantity((Double) dataMap.get("quantity"));
            }
            if (dataMap.containsKey("remark")) {
                salesOrderDetail.setRemark(StringUtil.DecodeText(StringUtil.isNullOrEmpty((String) dataMap.get("remark")) ? "" : (String) dataMap.get("remark")));
            }
            if (dataMap.containsKey("discount")) {
                salesOrderDetail.setDiscount((Double) dataMap.get("discount"));
            }
            if (dataMap.containsKey("discountispercent")) {
                salesOrderDetail.setDiscountispercent((Integer) dataMap.get("discountispercent"));
            } else {
                salesOrderDetail.setDiscountispercent(1);
            }
            if (dataMap.containsKey("productdetail")) {
                salesOrderDetail.setProductDetail((String) dataMap.get("productdetail"));
            }
            if (dataMap.containsKey("credito")) {
                Account account = dataMap.get("credito") == null ? null : (Account) get(Account.class, (String) dataMap.get("credito"));
                salesOrderDetail.setCreditTo(account);
            }
//            if (dataMap.containsKey("rowTaxAmount")) {
//                double rowTaxAmount = (Double) dataMap.get("rowTaxAmount");
//                salesOrderDetail.setRowTaxAmount(rowTaxAmount);
//            }
            if (dataMap.containsKey("companyid")) {
                Company company = dataMap.get("companyid") == null ? null : (Company) get(Company.class, (String) dataMap.get("companyid"));
                salesOrderDetail.setCompany(company);
            }
            if (dataMap.containsKey("rowtaxid")) {
                Tax rowtax = (dataMap.get("rowtaxid") == null ? null : (Tax) get(Tax.class, (String) dataMap.get("rowtaxid")));
                salesOrderDetail.setTax(rowtax);
            }
            saveOrUpdate(salesOrderDetail);
            list.add(salesOrderDetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveBillingSalesOrderDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject getBillingSalesOrders(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            DateFormat df = (DateFormat) request.get(Constants.df);
            String start = (String) request.get(Constants.start);
            String limit = (String) request.get(Constants.limit);
            String ss = (String) request.get(Constants.ss);
            ArrayList params = new ArrayList();
            params.add((String) request.get(Constants.companyKey));
            String condition = " where deleted=false and company.companyID=?";
            if (!StringUtil.isNullOrEmpty(ss)) {
                for (int i = 0; i < 3; i++) {
                    params.add(ss + "%");
                }
                condition += " and ( salesOrderNumber like ? or so.memo like ? or so.customer.name like ? )";
            }
            String costCenterId = (String) request.get(CCConstants.REQ_costCenterId);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                params.add(costCenterId);
                condition += " and so.costcenter.ID=?";
            }
            String customerId = (String) request.get(CCConstants.REQ_customerId);
            if (!StringUtil.isNullOrEmpty(customerId)) {
                params.add(customerId);
                condition += " and so.customer.ID=?";
            }
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                condition += " and (so.orderDate >=? and so.orderDate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }

            condition += " and so.pendingapproval= 0 and so.istemplate != 2 ";
            String query = "from BillingSalesOrder so" + condition;
            list = executeQuery( query, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCustomerDAOImpl.getBillingSalesOrders: " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject deleteBillingSalesOrder(String soid, String companyid) throws ServiceException {
        String query = "update BillingSalesOrder set deleted=true where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{soid, companyid});
        return new KwlReturnObject(true, "Billing Sales Order has been deleted successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject deleteSalesOrderDetails(String soid, String companyid, boolean isLeaseFixedAsset,boolean isConsignment,boolean isJobWorkOrderReciever) throws ServiceException, AccountingException {
        try {
            ArrayList params8 = new ArrayList();
            params8.add(soid);
            params8.add(companyid);
//            String myquery = " select id from sodetails where salesorder in (select id from salesorder where id=? and company = ?) ";
            String myquery = "  select sod.id from sodetails sod inner join salesorder so on so.id=sod.salesorder where so.id=? and so.company =?";
            List list = executeSQLQuery( myquery, params8.toArray());
            Iterator itr = list.iterator();
            String idStrings = "";
            while (itr.hasNext()) {
                String invdid = itr.next().toString();
                idStrings += "'" + invdid + "',";
            }
            if (!StringUtil.isNullOrEmpty(idStrings)) {
                idStrings = idStrings.substring(0, idStrings.length() - 1);
            }
            ArrayList params1 = new ArrayList();
            String deletecustomdetails = "delete  from salesorderdetailcustomdata where soDetailID in (" + idStrings + ")";
            int numRows1 = executeSQLUpdate( deletecustomdetails, params1.toArray());
            String delQuery = "delete from SalesOrderDetail sod where sod.salesOrder.ID=? and sod.company.companyID=?";
            int numRows = executeUpdate( delQuery, new Object[]{soid, companyid});
            deleteGstTaxClassDetails(idStrings);
            return new KwlReturnObject(true, "Goods Receipt Details has been deleted successfully.", null, null, numRows);
        } catch (Exception ex) {
            if (isLeaseFixedAsset) {
                throw new AccountingException("Cannot Edit Lease Order as it is used in some Transaction already.");
            } else if (isJobWorkOrderReciever) {
                throw new AccountingException("Cannot Edit Job Work Order as it is used in some Transaction already.");
            } else if(isConsignment){
                throw new AccountingException("Cannot Edit Consignment Request as it is used in some Transaction already.");

            } else {
                throw new AccountingException("Cannot Edit Sales Order as it is used in some Transaction already.");

            }
        }
    }

    @Override
    public KwlReturnObject deleteBillingSalesOrderDetails(String soid, String companyid) throws ServiceException {
        try {
            String delQuery = "delete from BillingSalesOrderDetail bsd where bsd.salesOrder.ID=? and bsd.company.companyID=?";
            int numRows = executeUpdate( delQuery, new Object[]{soid, companyid});
            return new KwlReturnObject(true, "Goods Receipt Details has been deleted successfully.", null, null, numRows);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot Edit Sales Order as it is used in Customer Invoice already.", ex);//+ex.getMessage(), ex);  
        }
    }

    public KwlReturnObject saveQuotation(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        String companyid = "";
        try {
            String soid = (String) dataMap.get("id");
            if (dataMap.containsKey("currencyid")) {
                companyid = (String) dataMap.get("companyid");
            }
            Quotation quotation = new Quotation();
            if (StringUtil.isNullOrEmpty(soid)) {
                quotation.setDeleted(false);
                if (dataMap.containsKey("createdby")) {
                    User createdby = dataMap.get("createdby") == null ? null : (User) get(User.class, (String) dataMap.get("createdby"));
                    quotation.setCreatedby(createdby);
                }
                if (dataMap.containsKey("modifiedby")) {
                    User modifiedby = dataMap.get("modifiedby") == null ? null : (User) get(User.class, (String) dataMap.get("modifiedby"));
                    quotation.setModifiedby(modifiedby);
                }
                if (dataMap.containsKey("createdon")) {
                    quotation.setCreatedon((Long) dataMap.get("createdon"));
                }
                if (dataMap.containsKey("updatedon")) {
                    quotation.setUpdatedon((Long) dataMap.get("updatedon"));
                }
            } else {
                quotation = (Quotation) get(Quotation.class, soid);
                if (dataMap.containsKey("modifiedby")) {
                    User modifiedby = dataMap.get("modifiedby") == null ? null : (User) get(User.class, (String) dataMap.get("modifiedby"));
                    quotation.setModifiedby(modifiedby);
                }
                if (dataMap.containsKey("updatedon")) {
                    quotation.setUpdatedon((Long) dataMap.get("updatedon"));
                }
            }
            if (dataMap.containsKey(Constants.SEQFORMAT)) {
                quotation.setSeqformat((SequenceFormat) get(SequenceFormat.class, (String) dataMap.get(Constants.SEQFORMAT)));
            }
            if (dataMap.containsKey(Constants.SEQNUMBER)) {
                quotation.setSeqnumber(Integer.parseInt(dataMap.get(Constants.SEQNUMBER).toString()));
            }
            if (dataMap.containsKey(Constants.DATEPREFIX) && dataMap.get(Constants.DATEPREFIX) != null) {
                quotation.setDatePreffixValue((String) dataMap.get(Constants.DATEPREFIX));
            }
            if (dataMap.containsKey(Constants.DATEAFTERPREFIX) && dataMap.get(Constants.DATEAFTERPREFIX) != null) {
                quotation.setDateAfterPreffixValue((String) dataMap.get(Constants.DATEAFTERPREFIX));
            }
            if (dataMap.containsKey(Constants.isApplyTaxToTerms) && dataMap.get(Constants.isApplyTaxToTerms) != null) {  // If Save As Draft
                quotation.setApplyTaxToTerms((Boolean) dataMap.get(Constants.isApplyTaxToTerms));
            }
            if (dataMap.containsKey(Constants.DATESUFFIX) && dataMap.get(Constants.DATESUFFIX) != null) {
                quotation.setDateSuffixValue((String) dataMap.get(Constants.DATESUFFIX));
            }
            if (dataMap.containsKey("shipLength")) {
                quotation.setShiplength(Double.parseDouble((String) dataMap.get("shipLength")));
            }

            if (dataMap.containsKey("invoicetype")) {
                quotation.setInvoicetype((String) dataMap.get("invoicetype"));
            }
            if (dataMap.containsKey("entrynumber")) {
                quotation.setquotationNumber((String) dataMap.get("entrynumber"));
            }
            if (dataMap.containsKey("autogenerated")) {
                quotation.setAutoGenerated((Boolean) dataMap.get("autogenerated"));
            }
            if (dataMap.containsKey("perDiscount")) {
                quotation.setPerDiscount((Boolean) dataMap.get("perDiscount"));
            }
            if (dataMap.containsKey("discount")) {
                quotation.setDiscount((Double) dataMap.get("discount"));
            }
            if (dataMap.containsKey("memo")) {
                quotation.setMemo((String) dataMap.get("memo"));
            }
            if (dataMap.containsKey("posttext")) {
                quotation.setPostText((String) dataMap.get("posttext"));
            }
            if (dataMap.containsKey("customerid")) {
                Customer customer = dataMap.get("customerid") == null ? null : (Customer) get(Customer.class, (String) dataMap.get("customerid"));
                quotation.setCustomer(customer);
            }
            if (dataMap.containsKey("termid")) {
                Term term = (dataMap.get("termid") == null ? null : (Term) get(Term.class, (String) dataMap.get("termid")));
                quotation.setTerm(term);
            }
            if (dataMap.containsKey("salesPerson")) {
                MasterItem salesPerson = dataMap.get("salesPerson") == null ? null : (MasterItem) get(MasterItem.class, (String) dataMap.get("salesPerson"));
                quotation.setSalesperson(salesPerson);
            }
            if (dataMap.containsKey("orderdate")) {
                quotation.setQuotationDate((Date) dataMap.get("orderdate"));
            }
            if (dataMap.containsKey("duedate")) {
                quotation.setDueDate((Date) dataMap.get("duedate"));
            }
            if (dataMap.containsKey("shipdate")) {
                quotation.setShipdate((Date) dataMap.get("shipdate"));
            }
            if (dataMap.containsKey("validdate")&& dataMap.get("validdate")!=null) {
                quotation.setValiddate((Date) dataMap.get("validdate"));
            }
            if (dataMap.containsKey("shipvia")) {
                quotation.setShipvia((String) dataMap.get("shipvia"));
            }
            if (dataMap.containsKey("shippingterm")) {
                quotation.setShippingTerm((String) dataMap.get("shippingterm"));
            }
            if (dataMap.containsKey("fob")) {
                quotation.setFob((String) dataMap.get("fob"));
            }
            if (dataMap.containsKey("taxid")) {
                Tax tax = dataMap.get("taxid") == null ? null : (Tax) get(Tax.class, (String) dataMap.get("taxid"));
                quotation.setTax(tax);
            }

            if (dataMap.containsKey("isfavourite")) {
                if (dataMap.get("isfavourite") != null) {
                    quotation.setFavourite(Boolean.parseBoolean(dataMap.get("isfavourite").toString()));
                }
            }

            if (dataMap.containsKey("isLeaseFixedAsset") && dataMap.get("isLeaseFixedAsset") != null) {
                quotation.setLeaseQuotation((Boolean) dataMap.get("isLeaseFixedAsset"));
            }

            if (dataMap.containsKey("customerporefno") && dataMap.get("customerporefno") != null) {
                quotation.setCustomerPORefNo(dataMap.get("customerporefno").toString());
            }

            if (dataMap.containsKey("profitMargin") && dataMap.get("profitMargin") != null) {
                quotation.setTotalProfitMargin(Double.parseDouble((String) dataMap.get("profitMargin")));
            }

            if (dataMap.containsKey("profitMarginPercent") && dataMap.get("profitMarginPercent") != null) {
                quotation.setTotalProfitMarginPercent(Double.parseDouble((String) dataMap.get("profitMarginPercent")));
            }

            if (dataMap.containsKey("quotationamountinbase") && dataMap.get("quotationamountinbase") != null) {
                quotation.setQuotationamountinbase(authHandler.round(Double.valueOf(dataMap.get("quotationamountinbase").toString()), companyid));
            }

            if (dataMap.containsKey("quotationamount")  && dataMap.get("quotationamount") != null) { // quotation amount
                quotation.setQuotationamount(authHandler.round(Double.valueOf(dataMap.get("quotationamount").toString()), companyid));
            }
                
            if (dataMap.containsKey(Constants.roundingadjustmentamountinbase) && dataMap.get(Constants.roundingadjustmentamountinbase) != null) {
                quotation.setRoundingadjustmentamountinbase(Double.valueOf(dataMap.get(Constants.roundingadjustmentamountinbase).toString()));
            }

            if (dataMap.containsKey(Constants.roundingadjustmentamount) && dataMap.get(Constants.roundingadjustmentamount) != null) { // quotation amount
                quotation.setRoundingadjustmentamount(Double.valueOf(dataMap.get(Constants.roundingadjustmentamount).toString()));
            }
            if (dataMap.containsKey(Constants.IsRoundingAdjustmentApplied) && dataMap.get(Constants.IsRoundingAdjustmentApplied) != null) {  // If New GST Appliled
                quotation.setIsRoundingAdjustmentApplied((Boolean) dataMap.get(Constants.IsRoundingAdjustmentApplied));
            }
            
            if (dataMap.containsKey("discountinbase")  && dataMap.get("discountinbase") != null) { // Discount
                quotation.setDiscountinbase(authHandler.round(Double.valueOf(dataMap.get("discountinbase").toString()), companyid));
            }
            if (dataMap.containsKey("totallineleveldiscount")  && dataMap.get("totallineleveldiscount") != null) { // Discount
                quotation.setTotallineleveldiscount(authHandler.round(Double.valueOf(dataMap.get("totallineleveldiscount").toString()), companyid));
            }
            if (dataMap.containsKey("contractid") && dataMap.get("contractid") != null) {
                Contract contract = (Contract) get(Contract.class, (String) dataMap.get("contractid"));
                quotation.setContract(contract);
            }

            if (dataMap.containsKey(Constants.MARKED_PRINTED)) {
                if (dataMap.get(Constants.MARKED_PRINTED) != null) {
                    quotation.setPrinted(Boolean.parseBoolean(dataMap.get(Constants.MARKED_PRINTED).toString()));
                }
            }

            if (dataMap.containsKey("billto")) {
                if (dataMap.get("billto") != null) {
                    quotation.setBillTo((String) dataMap.get("billto"));
                }
            }
            if (dataMap.containsKey("shipaddress")) {
                if (dataMap.get("shipaddress") != null) {
                    quotation.setShipTo((String) dataMap.get("shipaddress"));
                }
            }

            if (dataMap.containsKey("istemplate")) {
                quotation.setIstemplate((Integer) dataMap.get("istemplate"));
            } else {
                quotation.setIstemplate(0);
            }

            if (dataMap.containsKey("companyid")) {
                Company company = dataMap.get("companyid") == null ? null : (Company) get(Company.class, (String) dataMap.get("companyid"));
                quotation.setCompany(company);
            }


            if (dataMap.containsKey("sodetails")) {
                if (dataMap.get("sodetails") != null) {
                    quotation.setRows((Set<QuotationDetail>) dataMap.get("sodetails"));
                }
            }
            if (dataMap.containsKey("currencyid")) {
                quotation.setCurrency((KWLCurrency) get(KWLCurrency.class, (String) dataMap.get("currencyid")));
            }
            if (dataMap.containsKey("billshipAddressid")) {
                BillingShippingAddresses bsa = dataMap.get("billshipAddressid") == null ? null : (BillingShippingAddresses) get(BillingShippingAddresses.class, (String) dataMap.get("billshipAddressid"));
                quotation.setBillingShippingAddresses(bsa);
            }
            if (dataMap.containsKey("gstIncluded") && dataMap.get("gstIncluded") != null) {
                quotation.setGstIncluded((Boolean) dataMap.get("gstIncluded"));
            }
            if (dataMap.containsKey("externalCurrencyRate")) {
                quotation.setExternalCurrencyRate((Double) dataMap.get("externalCurrencyRate"));
            }
            if (dataMap.containsKey(Constants.termsincludegst)) {
                quotation.setTermsincludegst((Boolean) dataMap.get(Constants.termsincludegst));
            }
            if (dataMap.containsKey("crmquoatationid")) {
                quotation.setCrmquoteid((String) dataMap.get("crmquoatationid"));
            }
            if (dataMap.containsKey("quotationtype")) {
                quotation.setQuotationType((Integer) dataMap.get("quotationtype"));
            }
            if (dataMap.containsKey("maintenanceid")) {
                quotation.setMaintenance((String) dataMap.get("maintenanceid"));
            }
            if (dataMap.containsKey("approvestatuslevel")) {
                    quotation.setApprovestatuslevel((Integer) dataMap.get("approvestatuslevel"));
            }
            if (dataMap.containsKey("isDraft") && dataMap.get("isDraft")!=null) {
                    quotation.setDraft((Boolean)dataMap.get("isDraft"));
            }
            if (dataMap.containsKey("formtypeid") && dataMap.get("formtypeid")!=null) {
                    quotation.setFormtype((String)dataMap.get("formtypeid"));
            }
         
            if (dataMap.containsKey("isreserveStockQuantity") && dataMap.get("isreserveStockQuantity") != null) {
                quotation.setReserveStockQuantityFlag((Boolean) dataMap.get("isreserveStockQuantity"));
            }
            if (dataMap.containsKey("gstapplicable") && dataMap.get("gstapplicable") != null) {  // If New GST Appliled
                quotation.setIsIndGSTApplied((Boolean) dataMap.get("gstapplicable"));
            }
            if (dataMap.containsKey(Constants.RCMApplicable) && dataMap.get(Constants.RCMApplicable) != null) {  // If RCM applicable
                quotation.setRcmapplicable((Boolean) dataMap.get(Constants.RCMApplicable));
            }
            if (dataMap.containsKey(Constants.isMerchantExporter) && dataMap.get(Constants.isMerchantExporter) != null) {  // If RCM applicable
                quotation.setIsMerchantExporter((Boolean) dataMap.get(Constants.isMerchantExporter));
            }
            quotation.setTemplateid((Projreport_Template) get(Projreport_Template.class, Constants.HEADER_IMAGE_TEMPLATE_ID));
            saveOrUpdate(quotation);
            list.add(quotation);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveQuotation : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject saveQuotationVersion(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String soid = (String) dataMap.get("id");
            QuotationVersion quotation = new QuotationVersion();
            if (StringUtil.isNullOrEmpty(soid)) {
                quotation.setDeleted(false);
                if (dataMap.containsKey("createdby")) {
                    User createdby = dataMap.get("createdby") == null ? null : (User) get(User.class, (String) dataMap.get("createdby"));
                    quotation.setCreatedby(createdby);
                }
                if (dataMap.containsKey("modifiedby")) {
                    User modifiedby = dataMap.get("modifiedby") == null ? null : (User) get(User.class, (String) dataMap.get("modifiedby"));
                    quotation.setModifiedby(modifiedby);
                }
                if (dataMap.containsKey("createdon")) {
                    quotation.setCreatedon((Long) dataMap.get("createdon"));
                }
                if (dataMap.containsKey("updatedon")) {
                    quotation.setUpdatedon((Long) dataMap.get("updatedon"));
                }
            }
            if (dataMap.containsKey(Constants.SEQFORMAT)) {
                quotation.setSeqformat((SequenceFormat) get(SequenceFormat.class, (String) dataMap.get(Constants.SEQFORMAT)));
            }
            if (dataMap.containsKey(Constants.SEQNUMBER)) {
                quotation.setSeqnumber(Integer.parseInt(dataMap.get(Constants.SEQNUMBER).toString()));
            }
            if (dataMap.containsKey(Constants.DATEPREFIX) && dataMap.get(Constants.DATEPREFIX) != null) {
                quotation.setDatePreffixValue((String) dataMap.get(Constants.DATEPREFIX));
            }
            if (dataMap.containsKey(Constants.DATESUFFIX) && dataMap.get(Constants.DATESUFFIX) != null) {
                quotation.setDateSuffixValue((String) dataMap.get(Constants.DATESUFFIX));
            }
            if (dataMap.containsKey("shipLength")) {
                quotation.setShiplength((Double) dataMap.get("shipLength"));
            }
            if (dataMap.containsKey("invoicetype")) {
                quotation.setInvoicetype((String) dataMap.get("invoicetype"));
            }
            if (dataMap.containsKey("entrynumber")) {
                quotation.setquotationNumber((String) dataMap.get("entrynumber"));
            }
            if (dataMap.containsKey("autogenerated")) {
                quotation.setAutoGenerated((Boolean) dataMap.get("autogenerated"));
            }
            if (dataMap.containsKey("perDiscount")) {
                quotation.setPerDiscount((Boolean) dataMap.get("perDiscount"));
            }
            if (dataMap.containsKey("discount")) {
                quotation.setDiscount((Double) dataMap.get("discount"));
            }
            if (dataMap.containsKey("memo")) {
                quotation.setMemo((String) dataMap.get("memo"));
            }
            if (dataMap.containsKey("posttext")) {
                quotation.setPostText((String) dataMap.get("posttext"));
            }
            if (dataMap.containsKey("customerid")) {
                Customer customer = dataMap.get("customerid") == null ? null : (Customer) get(Customer.class, (String) dataMap.get("customerid"));
                quotation.setCustomer(customer);
            }
            if (dataMap.containsKey("salesPerson")) {
                MasterItem salesPerson = dataMap.get("salesPerson") == null ? null : (MasterItem) get(MasterItem.class, (String) dataMap.get("salesPerson"));
                quotation.setSalesperson(salesPerson);
            }
            if (dataMap.containsKey("quotationID")) {
                Quotation qo = dataMap.get("quotationID") == null ? null : (Quotation) get(Quotation.class, (String) dataMap.get("quotationID"));
                quotation.setQuotation(qo);
            }
            if (dataMap.containsKey("orderdate")) {
                quotation.setQuotationDate((Date) dataMap.get("orderdate"));
            }
            if (dataMap.containsKey("duedate")) {
                quotation.setDueDate((Date) dataMap.get("duedate"));
            }
            if (dataMap.containsKey("shipdate")) {
                quotation.setShipdate((Date) dataMap.get("shipdate"));
            }
            if (dataMap.containsKey("validdate")) {
                quotation.setValiddate((Date) dataMap.get("validdate"));
            }
            if (dataMap.containsKey("shipvia")) {
                quotation.setShipvia((String) dataMap.get("shipvia"));
            }
            if (dataMap.containsKey("fob")) {
                quotation.setFob((String) dataMap.get("fob"));
            }
            if (dataMap.containsKey("taxid")) {
                Tax tax = dataMap.get("taxid") == null ? null : (Tax) get(Tax.class, (String) dataMap.get("taxid"));
                quotation.setTax(tax);
            }
            if (dataMap.containsKey("isfavourite")) {
                if (dataMap.get("isfavourite") != null) {
                    quotation.setFavourite(Boolean.parseBoolean(dataMap.get("isfavourite").toString()));
                }
            }
            if (dataMap.containsKey("isLeaseFixedAsset") && dataMap.get("isLeaseFixedAsset") != null) {
                quotation.setLeaseQuotation((Boolean) dataMap.get("isLeaseFixedAsset"));
            }

            if (dataMap.containsKey("contractid") && dataMap.get("contractid") != null) {
                Contract contract = (Contract) get(Contract.class, (String) dataMap.get("contractid"));
                quotation.setContract(contract);
            }
            if (dataMap.containsKey(Constants.MARKED_PRINTED)) {
                if (dataMap.get(Constants.MARKED_PRINTED) != null) {
                    quotation.setPrinted(Boolean.parseBoolean(dataMap.get(Constants.MARKED_PRINTED).toString()));
                }
            }
            if (dataMap.containsKey("billto")) {
                if (dataMap.get("billto") != null) {
                    quotation.setBillTo((String) dataMap.get("billto"));
                }
            }
            if (dataMap.containsKey("shipaddress")) {
                if (dataMap.get("shipaddress") != null) {
                    quotation.setShipTo((String) dataMap.get("shipaddress"));
                }
            }
            if (dataMap.containsKey("istemplate")) {
                quotation.setIstemplate((Integer) dataMap.get("istemplate"));
            } else {
                quotation.setIstemplate(0);
            }
            if (dataMap.containsKey("companyid")) {
                Company company = dataMap.get("companyid") == null ? null : (Company) get(Company.class, (String) dataMap.get("companyid"));
                quotation.setCompany(company);
            }
            if (dataMap.containsKey("createdon")) {
                quotation.setCreatedon((Long) dataMap.get("createdon"));
            }
            if (dataMap.containsKey("updatedon")) {
                quotation.setUpdatedon((Long) dataMap.get("updatedon"));
            }
            if (dataMap.containsKey("createdby")) {
                User createdby = dataMap.get("createdby") == null ? null : (User) get(User.class, (String) dataMap.get("createdby"));
                quotation.setCreatedby(createdby);
            }
            if (dataMap.containsKey("modifiedby")) {
                User modifiedby = dataMap.get("modifiedby") == null ? null : (User) get(User.class, (String) dataMap.get("modifiedby"));
                quotation.setModifiedby(modifiedby);
            }
            if (dataMap.containsKey("sodetails")) {
                if (dataMap.get("sodetails") != null) {
                    quotation.setRows((Set<QuotationVersionDetail>) dataMap.get("sodetails"));
                }
            }
            if (dataMap.containsKey("currencyid")) {
                quotation.setCurrency((KWLCurrency) get(KWLCurrency.class, (String) dataMap.get("currencyid")));
            }
            if (dataMap.containsKey("billshipAddressid")) {
                BillingShippingAddresses bsa = dataMap.get("billshipAddressid") == null ? null : (BillingShippingAddresses) get(BillingShippingAddresses.class, (String) dataMap.get("billshipAddressid"));
                quotation.setBillingShippingAddresses(bsa);
            }
            if (dataMap.containsKey("gstIncluded") && dataMap.get("gstIncluded") != null) {
                quotation.setGstIncluded((Boolean) dataMap.get("gstIncluded"));
            }
            if (dataMap.containsKey("externalCurrencyRate")) {
                quotation.setExternalCurrencyRate((Double) dataMap.get("externalCurrencyRate"));
            }
            if (dataMap.containsKey("version")) {
                quotation.setVersion((String) dataMap.get("version"));
            }
            quotation.setTemplateid((Projreport_Template) get(Projreport_Template.class, Constants.HEADER_IMAGE_TEMPLATE_ID));
            saveOrUpdate(quotation);
            list.add(quotation);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveQuotation : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject getSalesOrderLinkedWithCQ(String invoiceId, String companyId) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(invoiceId);
        params.add(companyId);
        String query = "select distinct salesorder from sodetails pd inner join quotationdetails vqd on pd.quotationdetail = vqd.id "
                + "where vqd.quotation = ? and vqd.company = ?";

        List list = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getSILinkedWithCQ(String invoiceId, String companyId) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(invoiceId);
        params.add(companyId);
        String query = "select distinct invoice from invoicedetails ind inner join quotationdetails dod on ind.quotationdetail = dod.id "
                + "where dod.quotation= ? and dod.company=?";

        List list = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject saveQuotationDetails(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String sodid = (String) dataMap.get("id");

            QuotationDetail quotationDetail = new QuotationDetail();
            if (!StringUtil.isNullOrEmpty(sodid)) {
                quotationDetail = (QuotationDetail) get(QuotationDetail.class, sodid);
            }

            if (dataMap.containsKey("soid")) {
                Quotation quotation = dataMap.get("soid") == null ? null : (Quotation) get(Quotation.class, (String) dataMap.get("soid"));
                quotationDetail.setQuotation(quotation);
            }
            if (dataMap.containsKey("srno")) {
                quotationDetail.setSrno((Integer) dataMap.get("srno"));
            }
            if (dataMap.containsKey("desc")) {
                quotationDetail.setDescription((String) dataMap.get("desc"));
            }
            if (dataMap.containsKey("rate")) {
                quotationDetail.setRate((Double) dataMap.get("rate"));
            }
            if (dataMap.containsKey("rateIncludingGst")) {
                quotationDetail.setRateincludegst((Double) dataMap.get("rateIncludingGst"));
            }
            if (dataMap.containsKey("quantity")) {
                quotationDetail.setQuantity((Double) dataMap.get("quantity"));
            }
            if (dataMap.containsKey("uomid")) {
                quotationDetail.setUom((UnitOfMeasure) get(UnitOfMeasure.class, dataMap.get("uomid").toString()));
            }
            if (dataMap.containsKey("baseuomquantity") && dataMap.get("baseuomquantity") != null && dataMap.get("baseuomquantity") != "") {
                quotationDetail.setBaseuomquantity((Double) dataMap.get("baseuomquantity"));
//            } else {
//                if (dataMap.containsKey("quantity")) {
//                    quotationDetail.setBaseuomquantity((Double) dataMap.get("quantity"));
//                }
            }
            if (dataMap.containsKey("baseuomrate") && dataMap.get("baseuomrate") != null && dataMap.get("baseuomrate") != "") {
                quotationDetail.setBaseuomrate((Double) dataMap.get("baseuomrate"));
//            } else {
//                quotationDetail.setBaseuomrate(1);
            }
            if (dataMap.containsKey("remark")) {
                quotationDetail.setRemark(StringUtil.DecodeText(StringUtil.isNullOrEmpty((String) dataMap.get("remark")) ? "" : (String) dataMap.get("remark")));
            }
            if (dataMap.containsKey("dependentType")) {
                quotationDetail.setDependentType((String) dataMap.get("dependentType"));
            }
            if (dataMap.containsKey("inouttime")) {
                quotationDetail.setInouttime((String) dataMap.get("inouttime"));
            }
            if (dataMap.containsKey("showquantity")) {
                quotationDetail.setShowquantity((String) dataMap.get("showquantity"));
            }
            if (dataMap.containsKey("discount")) {
                quotationDetail.setDiscount((Double) dataMap.get("discount"));
            }
            if (dataMap.containsKey("productid")) {
                Product product = dataMap.get("productid") == null ? null : (Product) get(Product.class, (String) dataMap.get("productid"));
                quotationDetail.setProduct(product);
            }
            if (dataMap.containsKey("productreplacementDetailId")) {
                ProductReplacementDetail productReplacementDetail = dataMap.get("productreplacementDetailId") == null ? null : (ProductReplacementDetail) get(ProductReplacementDetail.class, (String) dataMap.get("productreplacementDetailId"));
                quotationDetail.setProductReplacementDetail(productReplacementDetail);
            }
            if (dataMap.containsKey("companyid")) {
                Company company = dataMap.get("companyid") == null ? null : (Company) get(Company.class, (String) dataMap.get("companyid"));
                quotationDetail.setCompany(company);
            }
            if (dataMap.containsKey("discountispercent")) {
                quotationDetail.setDiscountispercent((Integer) dataMap.get("discountispercent"));
            } else {
                quotationDetail.setDiscountispercent(1);
            }
            if (dataMap.containsKey("vendorquotationdetails")) {
                quotationDetail.setVendorquotationdetails((String) dataMap.get("vendorquotationdetails"));
            }
            if (dataMap.containsKey("rowTaxAmount")) {
                double rowTaxAmount = (Double) dataMap.get("rowTaxAmount");
                quotationDetail.setRowTaxAmount(rowTaxAmount);
            }
            if (dataMap.containsKey("rowtaxid")) {
                Tax rowtax = (dataMap.get("rowtaxid") == null ? null : (Tax) get(Tax.class, (String) dataMap.get("rowtaxid")));
                quotationDetail.setTax(rowtax);
            }
            if (dataMap.containsKey("invstoreid")) {
                quotationDetail.setInvstoreid((String) dataMap.get("invstoreid"));
            } else {
                quotationDetail.setInvstoreid("");
            }
            if (dataMap.containsKey("invlocationid")) {
                quotationDetail.setInvlocid((String) dataMap.get("invlocationid"));
            } else {
                quotationDetail.setInvlocid("");
            }
            if (dataMap.containsKey("priceSource") && dataMap.get("priceSource") != null) {
                quotationDetail.setPriceSource((String) dataMap.get("priceSource"));
            }
            if (dataMap.containsKey("pricingbandmasterid") && dataMap.get("pricingbandmasterid") != null) {
                quotationDetail.setPricingBandMasterid((String) dataMap.get("pricingbandmasterid"));
            }
            if (dataMap.containsKey("discountjson") && dataMap.get("discountjson") != null) {
                quotationDetail.setDiscountJson((String) dataMap.get("discountjson"));
            }
            if (dataMap.containsKey("recTermAmount") && !StringUtil.isNullOrEmpty(dataMap.get("recTermAmount").toString())) {
                double recTermAmount = Double.parseDouble(dataMap.get("recTermAmount").toString());
                quotationDetail.setRowtermamount(recTermAmount);
            }
            if (dataMap.containsKey("OtherTermNonTaxableAmount") && !StringUtil.isNullOrEmpty(dataMap.get("OtherTermNonTaxableAmount").toString())) {
                double OtherTermNonTaxableAmount = Double.parseDouble(dataMap.get("OtherTermNonTaxableAmount").toString());
                quotationDetail.setOtherTermNonTaxableAmount(OtherTermNonTaxableAmount);
            }
            if (dataMap.containsKey(Constants.isUserModifiedTaxAmount) && dataMap.get(Constants.isUserModifiedTaxAmount) != null) {
                quotationDetail.setIsUserModifiedTaxAmount((boolean) dataMap.get(Constants.isUserModifiedTaxAmount));
            }
            
            saveOrUpdate(quotationDetail);
            list.add(quotationDetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveQuotationDetail : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject saveQuotationVersionDetails(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String sodid = (String) dataMap.get("id");

            QuotationVersionDetail quotationDetail = new QuotationVersionDetail();
            if (!StringUtil.isNullOrEmpty(sodid)) {
                quotationDetail = (QuotationVersionDetail) get(QuotationVersionDetail.class, sodid);
            }

            if (dataMap.containsKey("soid")) {
                QuotationVersion quotation = dataMap.get("soid") == null ? null : (QuotationVersion) get(QuotationVersion.class, (String) dataMap.get("soid"));
                quotationDetail.setQuotationversion(quotation);
            }
            if (dataMap.containsKey("srno")) {
                quotationDetail.setSrno((Integer) dataMap.get("srno"));
            }
            if (dataMap.containsKey("desc")) {
                quotationDetail.setDescription((String) dataMap.get("desc"));
            }
            if (dataMap.containsKey("rate")) {
                quotationDetail.setRate((Double) dataMap.get("rate"));
            }
            if (dataMap.containsKey("quantity")) {
                quotationDetail.setQuantity((Double) dataMap.get("quantity"));
            }
            if (dataMap.containsKey("uomid")) {
                quotationDetail.setUom((UnitOfMeasure) get(UnitOfMeasure.class, dataMap.get("uomid").toString()));
            }
            if (dataMap.containsKey("baseuomquantity") && dataMap.get("baseuomquantity") != null && dataMap.get("baseuomquantity") != "") {
                quotationDetail.setBaseuomquantity((Double) dataMap.get("baseuomquantity"));

            }
            if (dataMap.containsKey("baseuomrate") && dataMap.get("baseuomrate") != null && dataMap.get("baseuomrate") != "") {
                quotationDetail.setBaseuomrate((Double) dataMap.get("baseuomrate"));
            }
            if (dataMap.containsKey("remark")) {
                quotationDetail.setRemark(StringUtil.DecodeText(StringUtil.isNullOrEmpty((String) dataMap.get("remark")) ? "" : (String) dataMap.get("remark")));
            }
            if (dataMap.containsKey("dependentType")) {
                quotationDetail.setDependentType((String) dataMap.get("dependentType"));
            }
            if (dataMap.containsKey("inouttime")) {
                quotationDetail.setInouttime((String) dataMap.get("inouttime"));
            }
            if (dataMap.containsKey("showquantity")) {
                quotationDetail.setShowquantity((String) dataMap.get("showquantity"));
            }
            if (dataMap.containsKey("discount")) {
                quotationDetail.setDiscount((Double) dataMap.get("discount"));
            }
            if (dataMap.containsKey("productid")) {
                Product product = dataMap.get("productid") == null ? null : (Product) get(Product.class, (String) dataMap.get("productid"));
                quotationDetail.setProduct(product);
            }
            if (dataMap.containsKey("productreplacementDetailId")) {
                ProductReplacementDetail productReplacementDetail = dataMap.get("productreplacementDetailId") == null ? null : (ProductReplacementDetail) get(ProductReplacementDetail.class, (String) dataMap.get("productreplacementDetailId"));
                quotationDetail.setProductReplacementDetail(productReplacementDetail);
            }
            if (dataMap.containsKey("companyid")) {
                Company company = dataMap.get("companyid") == null ? null : (Company) get(Company.class, (String) dataMap.get("companyid"));
                quotationDetail.setCompany(company);
            }
            if (dataMap.containsKey("discountispercent")) {
                quotationDetail.setDiscountispercent((Integer) dataMap.get("discountispercent"));
            } else {
                quotationDetail.setDiscountispercent(1);
            }
            if (dataMap.containsKey("vendorquotationdetails")) {
                quotationDetail.setVendorquotationdetails((String) dataMap.get("vendorquotationdetails"));
            }
            if (dataMap.containsKey("rowTaxAmount")) {
                double rowTaxAmount = (Double) dataMap.get("rowTaxAmount");
                quotationDetail.setRowTaxAmount(rowTaxAmount);
            }
            if (dataMap.containsKey("rowtaxid")) {
                Tax rowtax = (dataMap.get("rowtaxid") == null ? null : (Tax) get(Tax.class, (String) dataMap.get("rowtaxid")));
                quotationDetail.setTax(rowtax);
            }
            if (dataMap.containsKey("invstoreid")) {
                quotationDetail.setInvstoreid((String) dataMap.get("invstoreid"));
            } else {
                quotationDetail.setInvstoreid("");
            }
            if (dataMap.containsKey("invlocationid")) {
                quotationDetail.setInvlocid((String) dataMap.get("invlocationid"));
            } else {
                quotationDetail.setInvlocid("");
            }
            saveOrUpdate(quotationDetail);
            list.add(quotationDetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveQuotationDetail : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public String columSortCustomerQutation(String Col_Name, String Col_Dir) throws ServiceException {
        String String_Sort = "";
        if (Col_Name.equals("personname")) {
            String_Sort = " order by q.customer.name " + Col_Dir;
        } else if (Col_Name.equals("billno")) {
            String_Sort = " order by q.quotationNumber " + Col_Dir;
        } else if (Col_Name.equals("date")) {
            String_Sort = " order by q.quotationDate " + Col_Dir;
        }
        return String_Sort;

    }
    
    public String sortColumnCustomerQutation(String Col_Name, String Col_Dir) throws ServiceException {
        String String_Sort = "";
        if (Col_Name.equals("personname")) {
            String_Sort = " order by customer.name " + Col_Dir;
        } else if (Col_Name.equals("aliasname")) {
            String_Sort = " order by customer.aliasname " + Col_Dir;
        } else if (Col_Name.equals("billno")) {
            String_Sort = " order by quotation.quotationnumber " + Col_Dir;
        } else if (Col_Name.equals("date")) {
            String_Sort = " order by quotation.quotationdate " + Col_Dir;
        } else if (Col_Name.equals("salespersonname")) {
            String_Sort = " order by masteritem.value " + Col_Dir;
        }
        return String_Sort;

    }

    public KwlReturnObject getQuotations(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            DateFormat df = (DateFormat) request.get(Constants.df);
            DateFormat datef = authHandler.getDateOnlyFormat();
            String start = (String) request.get(Constants.start);
            String limit = (String) request.get(Constants.limit);
            String ss = (String) request.get(Constants.ss);
            boolean isfavourite = false;
            boolean linkFlagInSO = false;
            boolean linkFlagInInv = false;
            boolean isLeaseFixedAsset = false;
            boolean isForTemplate = false;
            String moduleid = "";
            if (request.containsKey(Constants.moduleid) && request.get(Constants.moduleid) != null) {
                moduleid = request.get(Constants.moduleid).toString();
            }
            
            boolean isDraft = false;
            if (request.containsKey("isDraft") && request.get("isDraft") != null) {
                isDraft = (Boolean) request.get("isDraft");
            }

            String userID = "";
            boolean isenableSalesPersonAgentFlow=false;
            if (request.containsKey("enablesalespersonagentflow") && request.get("enablesalespersonagentflow") != null && !StringUtil.isNullOrEmpty(request.get("enablesalespersonagentflow").toString())) {
                isenableSalesPersonAgentFlow = Boolean.parseBoolean(request.get("enablesalespersonagentflow").toString());
            }
            if (isenableSalesPersonAgentFlow) {
                if (request.containsKey("userid") && request.get("userid") != null && !StringUtil.isNullOrEmpty(request.get("userid").toString())) {
                    userID = (String) request.get("userid");
                }
            }
            if (request.containsKey("linkFlagInSO") && request.get("linkFlagInSO") != null) {
                linkFlagInSO = Boolean.parseBoolean(request.get("linkFlagInSO").toString());
            }
            if (request.containsKey("linkFlagInInv") && request.get("linkFlagInInv") != null) {
                linkFlagInInv = Boolean.parseBoolean(request.get("linkFlagInInv").toString());
            }
            if (request.get("isLeaseFixedAsset") != null) {
                isLeaseFixedAsset = (Boolean) request.get(Constants.isLeaseFixedAsset);
            }
            if (request.get("isfavourite") != null) {
                isfavourite = Boolean.parseBoolean((String) request.get(Constants.MARKED_FAVOURITE));
            }
           
            /*------customerQuotationsWithInvoiceAndDOStatus parameter comes when we apply linking filter in Quotation Report----- */
            int customerQuotationsWithInvoiceAndDOStatus = (request.containsKey("customerQuotationsWithInvoiceAndDOStatus") && request.get("customerQuotationsWithInvoiceAndDOStatus") != null) ? Integer.parseInt(request.get("customerQuotationsWithInvoiceAndDOStatus").toString()) : 0;

            boolean isprinted = false;
            if (request.get(Constants.MARKED_PRINTED) != null) {
                isprinted = Boolean.parseBoolean((String) request.get(Constants.MARKED_PRINTED));
            }
            String customerCategoryid = "";
            if (request.containsKey(Constants.customerCategoryid) && request.get(Constants.customerCategoryid) != null) {
                customerCategoryid = (String) request.get(Constants.customerCategoryid);
            }
            boolean deleted = Boolean.parseBoolean((String) request.get("deleted"));
            boolean nondeleted = Boolean.parseBoolean((String) request.get("nondeleted"));
            String userDepartment = "";
            if (request.containsKey("userDepartment") && request.get("userDepartment") != null) {
                userDepartment = (String) request.get("userDepartment");
            }
            if (request.containsKey("isForTemplate") && request.get("isForTemplate") != null) {
                isForTemplate = Boolean.parseBoolean(request.get("isForTemplate").toString());
            }

            String condition = "";
            ArrayList params = new ArrayList();
            if (nondeleted) {
                params.add((String) request.get(Constants.companyKey));
                //condition = " where q.deleted=false and q.company.companyID=?";
                condition = " where quotation.deleteflag='F' and quotation.company=?";
            } else if (deleted) {
                params.add((String) request.get(Constants.companyKey));
                //   condition += " where q.deleted=true and q.company.companyID=?";
                condition += " where quotation.deleteflag='T' and quotation.company=?";
            } else {
                params.add((String) request.get(Constants.companyKey));
                //condition += " where q.company.companyID=?";
                condition += " where quotation.company=?";
            }
            
            //Ignore records created as only templates.
            if (!isForTemplate) {
                condition += " and quotation.istemplate != ? ";
                params.add(2);
            }
            
            if (request.containsKey("crmquoatationid") && request.get("crmquoatationid") != null) {
                condition += " and quotation.crmquoteid = ?";
                params.add((String) request.get("crmquoatationid"));
            }
            
            if (request.containsKey("entrynumber") && request.get("entrynumber") != null) {
                condition += " and quotation.quotationNumber = ?";
                params.add((String) request.get("entrynumber"));
            }

            String billDate = "";
            if (request.containsKey(Constants.BillDate)) {
                billDate = (String) request.get(Constants.BillDate);
            }
            boolean validflag = false;
            if (request.get("validflag") != null) {
                validflag = Boolean.parseBoolean((String) request.get(Constants.ValidFlag));
            }

            //  String condition = " where q.deleted=false and q.company.companyID=?";
            if (request.get("archieve")!=null) {
                if ((Integer) request.get("archieve") == 0) {
                    //condition += " and q.archieve = 0 ";
                    condition += " and quotation.archieve = 0 ";
                } else if ((Integer) request.get("archieve") == 1) {
                    // condition += " and q.archieve = 1 ";
                    condition += " and quotation.archieve = 1 ";
                }
            }
            if (isfavourite) {
                // condition += " and .favourite = true ";
                condition += " and quotation.favouriteflag = 1 ";
            }
            if (isLeaseFixedAsset) {
                //condition += " and q.leaseQuotation = true ";
                condition += " and quotation.isleasequotation = 1 ";
            } else {
                // condition += " and q.leaseQuotation = false ";
                condition += " and quotation.isleasequotation = 0 ";
            }
            if (isprinted) {
                //condition += " and q.printedflag = true ";
                condition += " and quotation.printedflag = 0 ";
            }
            if (validflag) {
                try {
                    params.add(df.parse(billDate));
                } catch (ParseException ex) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(Long.valueOf(billDate).longValue());
                    Date transDate = cal.getTime();
                    String date=datef.format(transDate);
                    try{
                        transDate=datef.parse(date);
                    }catch(ParseException e){
                        transDate=cal.getTime();
                    }
                    params.add(transDate);
                }
                //condition += " and (q.validdate>=? or q.validdate is NULL) ";
                condition += " and (quotation.validdate>=? or quotation.validdate is null) ";
            }

            if (request.get(Constants.productid) != null && !request.get(Constants.productid).toString().equals("")) {
                // condition += " and q.ID in (select qd.quotation.ID from QuotationDetail qd where qd.product.ID ='" + request.get(Constants.productid).toString() + "' )";
                condition += " and quotation.id in (select qd.quotation from quotationdetails qd where qd.product ='" + request.get(Constants.productid).toString() + "' )";
            }

            if (request.get(Constants.productCategoryid) != null) {
                String productCategory = (String) request.get(Constants.productCategoryid);
                if (!StringUtil.isNullOrEmpty(productCategory)) {
                    //condition += " and q.ID in (select qd.quotation.ID from QuotationDetail qd where qd.product.ID in ( select pcm.productID from ProductCategoryMapping pcm where pcm.productCategory='" +productCategory+ "' ))";
                    condition += " and quotation.id in (select qd.quotation from quotationdetails qd where qd.product in ( select pcm.productid from productcategorymapping pcm where pcm.productcategory='" + productCategory + "' ))";
                }
            }

            String searchJoin = "";
            String joinstring = "";
            //Ignore QNs saved as only templates.
            //condition += " and q.istemplate != 2 ";
            if (request.containsKey("pendingapproval") && request.get("pendingapproval") != null) {
                if (Boolean.parseBoolean(request.get("pendingapproval").toString())) {
                    params.add(11);
                    condition += " and quotation.approvestatuslevel != ? ";
                } else {
                    params.add(11);
                    condition += " and quotation.approvestatuslevel = ? ";
                }
            }
             if (isDraft) {
                condition += " and quotation.isdraft = true ";
            } else {
                params.add(false);
                condition += " and quotation.isdraft = ? ";
            }
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"quotation.quotationnumber", "quotation.memo", "customer.name", "customer.aliasname", "product.name", "product.productid"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 6);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery;
                searchJoin = " inner join quotationdetails on quotationdetails.quotation = quotation.id "
                        + " inner join product on quotationdetails.product= product.id "
                        + "inner join customer on quotation.customer= customer.id";
            }

            if (request.containsKey("includingGSTFilter") && request.get("includingGSTFilter") != null) {
                //condition += " and q.gstIncluded = ?";
                condition += " and quotation.gstincluded = ?";
                //params.add((Boolean) request.get("includingGSTFilter"));
                params.add(request.get("includingGSTFilter"));
            }
            if (request.containsKey("linknumber") && request.get("linknumber") != null && !request.get("linknumber").toString().equals("")) {
                condition += " and quotation.quotationnumber = ? ";
                params.add(request.get("linknumber"));
            }
//            if (!StringUtil.isNullOrEmpty(ss)) {
//                for (int i = 0; i < 3; i++) {
//                    params.add(ss + "%");
//                }
//                condition += " and ( QuotationNumber like ? or q.memo like ? or q.customer.name like ? )";
//            }
            String companyId = (String) request.get(Constants.companyKey);
            if (request.containsKey("salesPersonFilterFlag") && (Boolean) request.get("salesPersonFilterFlag") && request.get("userId") != null) {
                String userId = (String) request.get("userId");
                if (!StringUtil.isNullOrEmpty(userId)) {
                    DataFilteringModule dataFilteringModule = null;
                    MasterItem masterItem = null;
                    List<DataFilteringModule> dataFilteringModuleList = new ArrayList<DataFilteringModule>();
                    List<MasterItem> masterItems = new ArrayList<MasterItem>();

                    dataFilteringModuleList = find("from DataFilteringModule where user.userID='" + userId + "' and company.companyID='" + companyId + "'");
                    masterItems = find("from MasterItem where user='" + userId + "' and company.companyID='" + companyId + "' and masterGroup.ID='" + 15 + "'");
                    if (!dataFilteringModuleList.isEmpty()) {
                        dataFilteringModule = dataFilteringModuleList.get(0);
                    }
//                    if (!masterItems.isEmpty()) {
//                        masterItem = masterItems.get(0);
//                    }
                    if ((dataFilteringModule != null && !dataFilteringModule.isCustomerQuotation()) || (dataFilteringModule != null && !dataFilteringModule.isCustomerQuotation() && masterItem != null)) {
                        condition += " and ( ";
                    }

                    if (dataFilteringModule != null && !dataFilteringModule.isCustomerQuotation()) {
                        params.add(dataFilteringModule.getUser().getUserID());
                        condition += "quotation.createdby=? ";
                    }

                    if (dataFilteringModule != null && !dataFilteringModule.isCustomerQuotation() && !masterItems.isEmpty()) {
                        String qMarks = "";
                        for (MasterItem item : masterItems) {
                            qMarks += "?,";
                            params.add(item.getID());
                        }
                        qMarks = qMarks.substring(0, qMarks.length() - 1);
                        condition += " or quotation.salesperson in ("+qMarks+") ";
                    }
                    if ((dataFilteringModule != null && !dataFilteringModule.isCustomerQuotation()) || (dataFilteringModule != null && !dataFilteringModule.isCustomerQuotation() && masterItem != null)) {
                        condition += " ) ";
                    }
                }
            }
            if (linkFlagInInv) {
                // condition += " and ( q.linkflag = 0 or q.linkflag = 1 ) and q.quotationType=0";// only normal quotation should be go in Linking combo in case of Invoice form 
                condition += " and ( quotation.linkflag = 0 or quotation.linkflag = 1 ) and quotation.quotationtype=0 and quotation.isopen='T'";// only normal quotation should be go in Linking combo in case of Invoice form
            }
            if (linkFlagInSO) {
                //condition += " and ( q.linkflag = 0 or q.linkflag = 2 ) ";
                condition += " and ( quotation.linkflag = 0 or quotation.linkflag = 2 ) and quotation.isopen='T'";
            }
            String customerId = (String) request.get(Constants.REQ_customerId);
            if (!StringUtil.isNullOrEmpty(customerId)) {
                if (customerId.contains(",")) {
                    customerId = AccountingManager.getFilterInString(customerId);
                    condition += " and quotation.customer IN" + customerId;
                } else {
                    params.add(customerId);
                //condition += " and q.customer.ID=?";
                condition += " and quotation.customer=?";
                }
            }
            if (!StringUtil.isNullOrEmpty(customerCategoryid) && !StringUtil.equal(customerCategoryid, "-1") && !StringUtil.equal(customerCategoryid, "All")) {
                //condition += " and q.customer.ID in (select ccm.customerID from CustomerCategoryMapping ccm where ccm.customerCategory = '" +customerCategoryid+ "' )  ";
                condition += " and quotation.customer in (select ccm.customerid from customercategorymapping ccm where ccm.customercategory = '" + customerCategoryid + "' )  ";
            }
            if (request.containsKey("billId")) {
                String billId = (String) request.get("billId");
                if (!StringUtil.isNullOrEmpty(billId)) {
                    params.add(billId);
                    //condition += " and q.ID=? ";
                    condition += " and quotation.id=? ";
                }
            }
            String currencyId = (String) request.get("currencyid");
            if (!StringUtil.isNullOrEmpty(currencyId)) {
                params.add(currencyId);
                //condition += " and q.currency.currencyID=?";
                condition += " and quotation.currency=?";
            }
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                //condition += " and (q.quotationDate >=? and q.quotationDate <=?)";
                condition += " and (quotation.quotationdate >=? and quotation.quotationdate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }

            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                joinstring += " inner join users on users.userid = quotation.createdby ";
                condition += " and users.department = ? ";
                params.add(userDepartment);
            }

            String appendCase = "and";
            String mySearchFilterString = "";
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (request.containsKey("filterConjuctionCriteria") && request.get("filterConjuctionCriteria") != null) {
                if (request.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            String Searchjson = "";
            String searchDefaultFieldSQL="";
            if (request.containsKey("searchJson") && request.get("searchJson") != null) {
                Searchjson = request.get("searchJson").toString();

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
                        Map<String, Object> map = buildSqlDefaultFieldAdvSearch(defaultSearchFieldArray, params, moduleid, tableArray, filterConjuctionCriteria);
                        searchJoin += map.containsKey("searchjoin") ? map.get("searchjoin") : "";
                        searchDefaultFieldSQL = (String) (map.containsKey("condition") ? map.get("condition") : "");
                        searchJoin += " left join cqlinking on cqlinking.docid=quotation.id and cqlinking.sourceflag = 1 ";
                    }

                    if (customSearchFieldArray.length() > 0) {   //Advance search case for Custome field
                        request.put(Constants.Searchjson, Searchjson);
                        request.put(Constants.appendCase, appendCase);
                        request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(request, true).get(Constants.myResult));
//                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
//                    joinString = " inner join salesreturncustomdata on salesreturncustomdata.salesreturnid=salesreturn.accsalesreturncustomdataref ";
                        mySearchFilterString = mySearchFilterString.replaceAll("QuotationCustomData", "quotationcustomdata");
                        String innerJoinOnDetailTable = "";
                        if (!searchJoin.contains("quotationdetails.quotation")) {
                            innerJoinOnDetailTable = "inner join quotationdetails on quotationdetails.quotation=quotation.id";
                        }
                        boolean isInnerJoinAppend = false;
                        if (mySearchFilterString.contains("quotationcustomdata")) {
                            joinstring = "inner join quotationcustomdata on quotationcustomdata.quotationid=quotation.accquotationcustomdataref ";
                        }
                        if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "quotationdetailscustomdata");
                            joinstring += innerJoinOnDetailTable + " left join quotationdetailscustomdata on quotationdetails.id=quotationdetailscustomdata.quotationdetailsid ";
                            isInnerJoinAppend = true;
                        }
                        if (mySearchFilterString.contains("CustomerCustomData")) {
                            joinstring += " left join customercustomdata  on customercustomdata.customerId=quotation.customer ";
                            mySearchFilterString = mySearchFilterString.replaceAll("CustomerCustomData", "customercustomdata");
                        }
                        if (mySearchFilterString.contains("AccJEDetailsProductCustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "cqdetailproductcustomdata");
                            joinstring += (isInnerJoinAppend ? "" : innerJoinOnDetailTable) + " left join cqdetailproductcustomdata on quotationdetails.id=cqdetailproductcustomdata.cqDetailID ";
                            isInnerJoinAppend = true;
                        }
                        //product custom data
                        if (mySearchFilterString.contains("accproductcustomdata")) {
                            joinstring += (isInnerJoinAppend ? "" : innerJoinOnDetailTable) + " left join product on product.id=quotationdetails.product left join accproductcustomdata on accproductcustomdata.productId=product.id";
                        }
                        StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                    }
                    mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
                }
            }
            String orderBy = "";
            String stringSort = "";
            String joinstring1 = "";
            boolean isAlreadyCustomerInnerJoinPresentInQuery=false;
            if (request.containsKey("dir") && request.containsKey("sort")) {
                String Col_Name = request.get("sort").toString();
                String Col_Dir = request.get("dir").toString();
                if(StringUtil.isNullOrEmpty(searchJoin) && (Col_Name.equals("personname") || Col_Name.equals("aliasname"))){
                    joinstring += " inner join customer on customer.id=quotation.customer ";
                    isAlreadyCustomerInnerJoinPresentInQuery=true;
                }
                if(Col_Name.equals("salespersonname")){
                    joinstring1 += " left join masteritem on masteritem.id = quotation.salesperson ";
                }
                stringSort = sortColumnCustomerQutation(Col_Name, Col_Dir);
                orderBy += stringSort;
                
            } else {
                orderBy = " order by quotation.quotationdate desc";
            }
            String salesPersonMappingQuery = "";
//           if (isenableSalesPersonAgentFlow && !StringUtil.isNullOrEmpty(userID)) {
//                
//                if(!isAlreadyCustomerInnerJoinPresentInQuery){
//                  joinstring += " inner join customer on customer.id=quotation.customer ";
//                }
//                salesPersonMappingQuery = " left join salespersonmapping spm on spm.customerid=quotation.customer  left join masteritem  mst on mst.id=spm.salesperson ";
//                joinstring+=salesPersonMappingQuery;
//                mySearchFilterString += " and ((mst.user= '" + userID + "' or mst.user is null  and customer.isavailableonlytosalespersons='T' ) or  (customer.isavailableonlytosalespersons='F')) ";
//            }
            if (isenableSalesPersonAgentFlow && !StringUtil.isNullOrEmpty(userID)) {//this block is executed only when owners restriction feature is on 
                String salesPersonID = (String) request.get("salesPersonid");
                String salespersonQuery = "";
                 if (!StringUtil.isNullOrEmpty(salesPersonID)) {
                   salesPersonID= AccountingManager.getFilterInString(salesPersonID);
                    salespersonQuery = "  quotation.salesperson in " + salesPersonID + " or ";
                }
                
                condition += " and ( " + salespersonQuery + "  quotation.createdby='" + userID + "' or quotation.salesperson is null  ) ";
            }
            String query = "select DISTINCT quotation.id from quotation " + searchJoin + joinstring + joinstring1 + condition + mySearchFilterString;
            query += orderBy;
            list = executeSQLQuery( query, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false && customerQuotationsWithInvoiceAndDOStatus==0) {
                //list = executeQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
                list = executeSQLQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getQuotations:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getVersionQuotations(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            DateFormat df = (DateFormat) request.get(Constants.df);
            DateFormat datef=authHandler.getDateOnlyFormat();
            String start = (String) request.get(Constants.start);
            String limit = (String) request.get(Constants.limit);
            String ss = (String) request.get(Constants.ss);
            boolean isfavourite = false;
            boolean linkFlagInSO = false;
            boolean linkFlagInInv = false;
            boolean isLeaseFixedAsset = false;
            if (request.containsKey("linkFlagInSO") && request.get("linkFlagInSO") != null) {
                linkFlagInSO = Boolean.parseBoolean((String) request.get("linkFlagInSO"));
            }
            if (request.containsKey("linkFlagInInv") && request.get("linkFlagInInv") != null) {
                linkFlagInInv = Boolean.parseBoolean((String) request.get("linkFlagInInv"));
            }
            if (request.get("isLeaseFixedAsset") != null) {
                isLeaseFixedAsset = (Boolean) request.get(Constants.isLeaseFixedAsset);
            }
            if (request.get("isfavourite") != null) {
                isfavourite = Boolean.parseBoolean((String) request.get(Constants.MARKED_FAVOURITE));
            }
            boolean isprinted = false;
            if (request.get(Constants.MARKED_PRINTED) != null) {
                isprinted = Boolean.parseBoolean((String) request.get(Constants.MARKED_PRINTED));
            }
            String customerCategoryid = "";
            if (request.containsKey(Constants.customerCategoryid) && request.get(Constants.customerCategoryid) != null) {
                customerCategoryid = (String) request.get(Constants.customerCategoryid);
            }
            boolean deleted = Boolean.parseBoolean((String) request.get("deleted"));
            boolean nondeleted = Boolean.parseBoolean((String) request.get("nondeleted"));

            String condition = "";
            ArrayList params = new ArrayList();
            if (nondeleted) {
                params.add((String) request.get(Constants.companyKey));
                condition = " where quotationversion.deleteflag='F' and quotationversion.company=?";
            } else if (deleted) {
                params.add((String) request.get(Constants.companyKey));
                condition += " where quotationversion.deleteflag='T' and quotationversion.company=?";
            } else {
                params.add((String) request.get(Constants.companyKey));
                condition += " where quotationversion.company=?";
            }

            String billDate = "";
            if (request.containsKey(Constants.BillDate)) {
                billDate = (String) request.get(Constants.BillDate);
            }
            boolean validflag = false;
            if (request.get("validflag") != null) {
                validflag = Boolean.parseBoolean((String) request.get(Constants.ValidFlag));
            }
            if (request.get("archieve") != null) {
                if ((Integer) request.get("archieve") == 0) {
                    condition += " and quotationversion.archieve = 0 ";
                } else if ((Integer) request.get("archieve") == 1) {
                    condition += " and quotationversion.archieve = 1 ";
                }
            }

            if (isfavourite) {
                condition += " and quotation.favouriteflag = 1 ";
            }
            if (isLeaseFixedAsset) {
                condition += " and quotationversion.isleasequotation = 1 ";
            } else {
                condition += " and quotationversion.isleasequotation = 0 ";
            }
            if (isprinted) {
                condition += " and quotationversion.printedflag = 0 ";
            }
            if (validflag) {
                try {
                    params.add(df.parse(billDate));
                } catch (ParseException ex) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(Long.valueOf(billDate).longValue());
                    Date transactionDate = cal.getTime();
                    String date=datef.format(transactionDate);
                    try{
                        transactionDate=datef.parse(date);
                    }catch(ParseException e){
                        transactionDate=cal.getTime();
                    }
                    params.add(transactionDate);
                }
                condition += " and (quotationversion.validdate>=? or quotationversion.validdate is null) ";
            }

            if (request.get(Constants.productid) != null && !request.get(Constants.productid).toString().equals("")) {
                condition += " and quotationversion.id in (select qd.quotationversion from quotationversiondetails qd where qd.product ='" + request.get(Constants.productid).toString() + "' )";
            }

            if (request.get(Constants.productCategoryid) != null) {
                String productCategory = (String) request.get(Constants.productCategoryid);
                if (!StringUtil.isNullOrEmpty(productCategory)) {
                    condition += " and quotationversion.id in (select qd.quotationversion from quotationversiondetails qd where qd.product in ( select pcm.productid from productcategorymapping pcm where pcm.productcategory='" + productCategory + "' ))";
                }
            }

            String searchJoin = "";
            String joinstring = "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"quotationversion.quotationnumber", "quotationversion.memo", "customer.name", "product.name", "product.productid"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 5); 
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery;
                searchJoin = " inner join quotationversiondetails on quotationversiondetails.quotationversion = quotationversion.id "
                        + " inner join product on quotationversiondetails.product= product.id "
                        + "inner join customer on quotationversion.customer= customer.id";
            }

            if (request.containsKey("includingGSTFilter") && request.get("includingGSTFilter") != null) {
                condition += " and quotationversion.gstincluded = ?";
                params.add(request.get("includingGSTFilter"));
            }
            String companyId = (String) request.get(Constants.companyKey);
            if (request.containsKey("salesPersonFilterFlag") && (Boolean) request.get("salesPersonFilterFlag") && request.get("userId") != null) {
                String userId = (String) request.get("userId");
                if (!StringUtil.isNullOrEmpty(userId)) {
                    DataFilteringModule dataFilteringModule = null;
                    MasterItem masterItem = null;
                    List<DataFilteringModule> dataFilteringModuleList = new ArrayList<DataFilteringModule>();
                    List<MasterItem> masterItems = new ArrayList<MasterItem>();

                    dataFilteringModuleList = find("from DataFilteringModule where user.userID='" + userId + "' and company.companyID='" + companyId + "'");
                    masterItems = find("from MasterItem where user='" + userId + "' and company.companyID='" + companyId + "' and masterGroup.ID='" + 15 + "'");
                    if (!dataFilteringModuleList.isEmpty()) {
                        dataFilteringModule = dataFilteringModuleList.get(0);
                    }
                    if (!masterItems.isEmpty()) {
                        masterItem = masterItems.get(0);
                    }
                    if ((dataFilteringModule != null && dataFilteringModule.isCustomerQuotation()) || (dataFilteringModule != null && dataFilteringModule.isCustomerQuotation() && masterItem != null)) {
                        condition += " and ( ";
                    }

                    if (dataFilteringModule != null && dataFilteringModule.isCustomerQuotation()) {
                        params.add(dataFilteringModule.getUser().getUserID());
                        condition += "quotationversion.createdby=? ";
                    }

                    if (dataFilteringModule != null && dataFilteringModule.isCustomerQuotation() && masterItem != null) {
                        params.add(masterItem.getID());
                        condition += " or quotationversion.salesperson=? ";
                    }
                    if ((dataFilteringModule != null && dataFilteringModule.isCustomerQuotation()) || (dataFilteringModule != null && dataFilteringModule.isCustomerQuotation() && masterItem != null)) {
                        condition += " ) ";
                    }
                }
            }
            if (linkFlagInInv) {
                condition += " and ( quotationversion.linkflag = 0 or quotationversion.linkflag = 1 ) and quotationversion.quotationtype=0";// only normal quotation should be go in Linking combo in case of Invoice form
            }
            if (linkFlagInSO) {
                condition += " and ( quotationversion.linkflag = 0 or quotationversion.linkflag = 2 ) ";
            }
            String customerId = (String) request.get(Constants.REQ_customerId);
            if (!StringUtil.isNullOrEmpty(customerId)) {
                params.add(customerId);
                condition += " and quotationversion.customer=?";
            }
            if (!StringUtil.isNullOrEmpty(customerCategoryid) && !StringUtil.equal(customerCategoryid, "-1") && !StringUtil.equal(customerCategoryid, "All")) {
                condition += " and quotationversion.customer in (select ccm.customerid from customercategorymapping ccm where ccm.customercategory = '" + customerCategoryid + "' )  ";
            }
            if (request.containsKey("billId")) {
                String billId = (String) request.get("billId");
                if (!StringUtil.isNullOrEmpty(billId)) {
                    params.add(billId);
                    condition += " and quotationversion.id=? ";
                }
            }
            if (request.containsKey("versionid")) {
                String versionid = (String) request.get("versionid");
                if (!StringUtil.isNullOrEmpty(versionid)) {
                    params.add(versionid);
                    condition += " and quotationversion.quotation=? ";
                }
            }
            String currencyId = (String) request.get("currencyid");
            if (!StringUtil.isNullOrEmpty(currencyId)) {
                params.add(currencyId);
                condition += " and quotationversion.currency=?";
            }
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                condition += " and (quotationversion.quotationdate >=? and quotationversion.quotationdate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }

            String appendCase = "and";
            String mySearchFilterString = "";
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (request.containsKey("filterConjuctionCriteria") && request.get("filterConjuctionCriteria") != null) {
                if (request.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            String Searchjson = "";
            if (request.containsKey("searchJson") && request.get("searchJson") != null) {
                Searchjson = request.get("searchJson").toString();

                if (!StringUtil.isNullOrEmpty(Searchjson)) {
                    request.put(Constants.Searchjson, Searchjson);
                    request.put(Constants.appendCase, appendCase);
                    request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                    joinstring = "inner join quotationversioncustomdata on quotationversioncustomdata.quotationid=quotationversion.accquotationcustomdataref ";
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                }
            }
            String orderBy = "";
            String stringSort = "";
            if (request.containsKey("dir") && request.containsKey("sort")) {
                String Col_Name = request.get("sort").toString();
                String Col_Dir = request.get("dir").toString();
                stringSort = columSortCustomerQutation(Col_Name, Col_Dir);
                orderBy += stringSort;
            } else {
                orderBy = " order by quotationversion.quotationdate desc";
            }
            String query = "select quotationversion.id from quotationversion " + searchJoin + joinstring + condition + mySearchFilterString;
            query += orderBy;
            list = executeSQLQuery( query, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeSQLQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getVersionQuotations:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject getQuotationDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from QuotationDetail";
        return buildNExecuteQuery( query, requestParams);
    }

    public KwlReturnObject getQuotationVersionDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from QuotationVersionDetail";
        return buildNExecuteQuery( query, requestParams);
    }

    public KwlReturnObject getVendorQuotationDetails(String vqid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "select vqd.vendorquotation,vq.quotationnumber, vq.quotationdate from vendorquotationdetails vqd inner join vendorquotation vq on vq.id=vqd.vendorquotation where vqd.id=?  and vqd.company=?";
        list = executeSQLQuery( q, new Object[]{vqid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject deleteQuotation(String qid, String companyid) throws ServiceException {
        String query = "update Quotation set deleted=true where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{qid, companyid});
        return new KwlReturnObject(true, "Quotation has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deleteQuotationVersion(String versionID, String companyid) throws ServiceException {
        String query = "update QuotationVersion set deleted=true where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{versionID, companyid});
        return new KwlReturnObject(true, "Quotation Version has been deleted successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject updateCQLinkflag(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            Quotation quotation = (Quotation) requestParams.get("quotation");
            int num = Integer.parseInt((String) requestParams.get("value"));
            Boolean isopen =requestParams.get("isOpen")!=null?(Boolean)requestParams.get("isOpen"): false;
            quotation.setLinkflag(num);
            quotation.setIsopen(isopen);
            saveOrUpdate(quotation);
        } catch (Exception ex) { 
            throw ServiceException.FAILURE("accSalesOrderImpl.updateQuotationLinkflag:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }

    public KwlReturnObject deleteQuotationsPermanent(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            String delQuery1 = "", delQuery2 = "", delQuery3 = "", delQuery4 = "", delQuery5 = "", delQuery6;
            int numtotal = 0;
            if (requestParams.containsKey("qid") && requestParams.containsKey("companyid")) {

                ArrayList params8 = new ArrayList();
                params8.add(requestParams.get("qid"));
                params8.add(requestParams.get("companyid"));
//                String myquery = " select id from quotationdetails where quotation in (select id from quotation where id=? and company = ?) ";
                String myquery = "select qd.id from quotationdetails qd inner join quotation q on qd.quotation =q.id where q.id=? and q.company = ? ";
                List list = executeSQLQuery( myquery, params8.toArray());
                Iterator itr = list.iterator();
                String idStrings = "";
                while (itr.hasNext()) {


                    String invdid = itr.next().toString();
                    idStrings += "'" + invdid + "',";
                }
                if (!StringUtil.isNullOrEmpty(idStrings)) {
                    idStrings = idStrings.substring(0, idStrings.length() - 1);
                }
                deleteGstTaxClassDetails(idStrings);
                ArrayList params5 = new ArrayList();
                params5.add(requestParams.get("qid"));
                params5.add(requestParams.get("companyid"));
                delQuery5 = " delete from quotationdetails where quotation in (select id from quotation where id=? and company = ?) ";
                int numRows5 = executeSQLUpdate( delQuery5, params5.toArray());



                ArrayList params9 = new ArrayList();
                params9.add(requestParams.get("qid"));
                params9.add(requestParams.get("companyid"));
                String myquery1 = " select id, billingshippingaddresses from quotation where id=? and company = ?";
                List list1 = executeSQLQuery( myquery1, params9.toArray());
                Iterator itr1 = list1.iterator();
                String journalent = "";
                String billingShippingIds = "";
                while (itr1.hasNext()) {
                    Object[] row = (Object[]) itr1.next();
                    String jeidi = row[0].toString();
                    journalent += "'" + jeidi + "',";
                    if (row[1] != null) {
                        String billShipId = row[1].toString();
                        if (!StringUtil.isNullOrEmpty(billShipId)) {
                            billingShippingIds += "'" + billShipId + "',";
                        }
                    }
                }
                if (!StringUtil.isNullOrEmpty(journalent)) {
                    journalent = journalent.substring(0, journalent.length() - 1);
                }
                if (!StringUtil.isNullOrEmpty(billingShippingIds)) {
                    billingShippingIds = billingShippingIds.substring(0, billingShippingIds.length() - 1);
                }


                ArrayList params1 = new ArrayList();
//           params1.add(requestParams.get("companyid"));
//           params1.add(requestParams.get("companyid"));
//           params1.add(requestParams.get("soid"));
                int numRows1 = 0;
                if(!StringUtil.isNullOrEmpty(idStrings)){
                    delQuery1 = "delete  from quotationdetailscustomdata where quotationdetailsid in (" + idStrings + ")";
                    numRows1 = executeSQLUpdate( delQuery1, params1.toArray());
                }
                ArrayList params6 = new ArrayList();
                params6.add(requestParams.get("companyid"));
                params6.add(requestParams.get("qid"));
                String customerQuotationID = (String) requestParams.get("qid");
                int numRows8 = 0;

                String delQuery8 = "delete from quotationtermmap where quotation=?";
                numRows8 = executeSQLUpdate( delQuery8, new Object[]{customerQuotationID});
                deleteGstDocHistoryDetails(customerQuotationID);
   
                delQuery6 = "delete from quotation where company = ? and id=?";
                int numRows6 = executeSQLUpdate( delQuery6, params6.toArray());

                if (!StringUtil.isNullOrEmpty(billingShippingIds)) {
                    delQuery3 = "delete  from billingshippingaddresses where id in (" + billingShippingIds + ")";
                    int numRows4 = executeSQLUpdate( delQuery3);
                }
                ArrayList params2 = new ArrayList();
//           params2.add(requestParams.get("companyid"));
//           params2.add(requestParams.get("invoiceid"));
                int numRows2 = 0;
                if(!StringUtil.isNullOrEmpty(journalent)){
                    delQuery2 = "delete  from quotationcustomdata where quotationId in (" + journalent + ")";
                    numRows2 = executeSQLUpdate( delQuery2, params2.toArray());
                }

                int numRows7 = 0;
                if(!StringUtil.isNullOrEmpty(idStrings)){
                    ArrayList params7 = new ArrayList();
                    String delQuery7 = "delete  from quotationdetailsvendormapping where id in (" + idStrings + ")";
                    numRows7 = executeSQLUpdate( delQuery7, params7.toArray());
                }

                numtotal = numRows1 + numRows2 + numRows5 + numRows6 + numRows7 + numRows8;
            }

            return new KwlReturnObject(true, "Quotation has been deleted successfully.", null, null, numtotal);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot delete Quotation as its reference child field is not deleted.", ex);//+ex.getMessage(), ex);
        }

    }

    public KwlReturnObject deleteQuotationVersionsPermanent(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            String delQuery1 = "", delQuery2 = "", delQuery3 = "", delQuery4 = "", delQuery5 = "";
            int numtotal = 0;
            if (requestParams.containsKey("versionid") && requestParams.containsKey("companyid")) {

                ArrayList params1 = new ArrayList();
                params1.add(requestParams.get("versionid"));
                params1.add(requestParams.get("companyid"));
//                String myquery = " select id from quotationversiondetails where quotationversion in (select id from quotationversion where id=? and company = ?) ";
                String myquery = "  select qvd.id from quotationversiondetails qvd inner join quotationversion qv on qvd.quotationversion=qv.id where qv.id=? and qv.company = ? ";
                List list = executeSQLQuery( myquery, params1.toArray());
                Iterator itr = list.iterator();
                String idStrings = "";
                while (itr.hasNext()) {


                    String invdid = itr.next().toString();
                    idStrings += "'" + invdid + "',";
                }
                if (!StringUtil.isNullOrEmpty(idStrings)) {
                    idStrings = idStrings.substring(0, idStrings.length() - 1);
                }
                if (list.size() > 0 && list != null) {
                    ArrayList params2 = new ArrayList();
                    params2.add(requestParams.get("versionid"));
                    params2.add(requestParams.get("companyid"));
                    delQuery1 = " delete from quotationversiondetails where quotationversion in (select id from quotationversion where id=? and company = ?) ";
                    int numRows1 = executeSQLUpdate( delQuery1, params2.toArray());


                    ArrayList params3 = new ArrayList();
                    params3.add(requestParams.get("companyid"));
                    params3.add(requestParams.get("versionid"));
                    delQuery2 = "delete from quotationversion where company = ? and id=?";
                    int numRows2 = executeSQLUpdate( delQuery2, params3.toArray());

                    ArrayList params4 = new ArrayList();
                    params4.add(requestParams.get("companyid"));
                    params4.add(requestParams.get("versionid"));
                    delQuery3 = "delete from quotationversioncustomdata where company = ? and quotationId=?";
                    int numRows3 = executeSQLUpdate( delQuery3, params4.toArray());


                    ArrayList params5 = new ArrayList();
                    delQuery4 = " delete from quotationversiondetailscustomdata  where quotationdetailsid in (" + idStrings + ")";
                    int numRows4 = executeSQLUpdate( delQuery4, params5.toArray());


                    numtotal = numRows1 + +numRows2 + numRows3 + numRows4;
                }
            }

            return new KwlReturnObject(true, "Quotation Version has been deleted successfully.", null, null, numtotal);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot delete Quotation Version  as its reference child field is not deleted.", ex);//+ex.getMessage(), ex);
        }

    }

    public KwlReturnObject archieveQuotation(String qid, String companyid) throws ServiceException {
        String query = "update Quotation set archieve=1 where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{qid, companyid});
        return new KwlReturnObject(true, "Quotation has been archieved successfully.", null, null, numRows);
    }

    public KwlReturnObject unArchieveQuotation(String qid, String companyid) throws ServiceException {
        String query = "update Quotation set archieve=0 where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{qid, companyid});
        return new KwlReturnObject(true, "Quotation has been archieved successfully.", null, null, numRows);
    }

    public KwlReturnObject getQuotationCount(String qno, String companyid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = "from Quotation where quotationNumber=? and company.companyID=? AND isDraft='F'";  //SDP-13487 - Do not check duplicate in Draft Report. Because Multiple draft records having empty entry no.
        list = executeQuery( q, new Object[]{qno, companyid});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }

    public int approvePendingOrder(String qid, boolean isbilling, String companyid, String userid) throws ServiceException {

        int approveLevel = 0;
        User user = (User) get(User.class, userid);

        if (isbilling) {
            BillingSalesOrder so = (BillingSalesOrder) get(BillingSalesOrder.class, qid);
            approveLevel = so.getPendingapproval();
            if (so.getPendingapproval() < so.getApprovallevel()) {
                so.setPendingapproval((so.getPendingapproval() + 1));
            } else {
                so.setPendingapproval(Constants.APPROVED);
            }

            so.setApprover(user);
        } else {
            SalesOrder so = (SalesOrder) get(SalesOrder.class, qid);
            approveLevel = so.getPendingapproval();
            if (so.getPendingapproval() < so.getApprovallevel()) {
                so.setPendingapproval((so.getPendingapproval() + 1));
            } else {
                so.setPendingapproval(Constants.APPROVED);
            }
            so.setApprover(user);
        }

//        String query = "update SalesOrder set pendingapproval = 0 where ID=? and company.companyID=?";
//        if(isbilling) {
//            query = "update BillingSalesOrder set pendingapproval = 0 where ID=? and company.companyID=?";
//        }
//        int numRows = executeUpdate( query, new Object[]{qid, companyid});
        return approveLevel;//new KwlReturnObject(true, "Purchase order has been updated successfully.", null, null, 1);
    }

    public int pendingApprovalOrdersCount(String companyid) throws ServiceException {
        String query = "select * from ("
                + " select id from salesorder where deleteflag = 'F' and pendingapproval != 0 and company = ? "
                + " union "
                + " select id from billingsalesorder where deleteflag = 'F' and pendingapproval != 0 and company = ? "
                + ") as test";

        List list = executeSQLQuery( query, new Object[]{companyid, companyid});

        int count = list.size();

        return count;
    }

    @Override
    public KwlReturnObject saveQuotationTermMap(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            QuotationTermMap termmap = new QuotationTermMap();
            Quotation quotation = null;
            if (dataMap.containsKey("term") && dataMap.containsKey("quotationID")) {
                quotation = (Quotation) get(Quotation.class, (String) dataMap.get("quotationID"));
                InvoiceTermsSales term = (InvoiceTermsSales) get(InvoiceTermsSales.class, (String) dataMap.get("term"));
                List<QuotationTermMap> listTermMap = find("from QuotationTermMap where quotation.ID = '" + quotation.getID() + "' and term.id = '" + term.getId() + "'");
                if (listTermMap.size() > 0) {
                    termmap = listTermMap.get(0);
                }
                termmap.setTerm(term);
            }

            if (dataMap.containsKey("termamount")) {
                termmap.setTermamount((Double) dataMap.get("termamount"));
            }
            if (dataMap.containsKey("termtaxamount")) {
                termmap.setTermtaxamount((Double) dataMap.get("termtaxamount"));
            }
            if (dataMap.containsKey("termtaxamountinbase")) {
                termmap.setTermtaxamountinbase((Double) dataMap.get("termtaxamountinbase"));
            }
            if (dataMap.containsKey("termAmountExcludingTax")) {
                termmap.setTermAmountExcludingTax((Double) dataMap.get("termAmountExcludingTax"));
            }
            if (dataMap.containsKey("termAmountExcludingTaxInBase")) {
                termmap.setTermAmountExcludingTaxInBase((Double) dataMap.get("termAmountExcludingTaxInBase"));
            }
            if (dataMap.containsKey("termamountinbase")) {
                termmap.setTermamountinbase((Double) dataMap.get("termamountinbase"));
            }
            if (dataMap.containsKey("termtax") && dataMap.get("termtax") != null) {
                Tax termtax = (Tax) get(Tax.class, (String) dataMap.get("termtax"));
                termmap.setTermtax(termtax);
            }
            if (dataMap.containsKey("termpercentage")) {
                termmap.setPercentage((Double) dataMap.get("termpercentage"));
            }
            if (dataMap.containsKey("quotationID") && quotation != null) {
                termmap.setQuotation(quotation);
            }
//            if (dataMap.containsKey("term")) {
//                InvoiceTermsSales term = (InvoiceTermsSales) get(InvoiceTermsSales.class, (String) dataMap.get("term"));
//                termmap.setTerm(term);
//            }
            if (dataMap.containsKey("userid")) {
                User userid = (User) get(User.class, (String) dataMap.get("userid"));
                termmap.setCreator(userid);
            }
            if (dataMap.containsKey("createdon")) {
                termmap.setCreatedOn(((Date) dataMap.get("creationdate")).getTime());
            }
            saveOrUpdate(termmap);
            list.add(termmap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.saveQuotationTermMap : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public KwlReturnObject updateQuotationTermMap(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            QuotationTermMap termmap = new QuotationTermMap();
            
            if (dataMap.containsKey("quotationtermid")) {
                termmap = (QuotationTermMap) get(QuotationTermMap.class, (String) dataMap.get("quotationtermid"));
            }
            
            if(termmap != null){
                if (dataMap.containsKey("termamount")) {
                    termmap.setTermamount((Double) dataMap.get("termamount"));
                }
                if (dataMap.containsKey("termtaxamount")) {
                    termmap.setTermtaxamount((Double) dataMap.get("termtaxamount"));
                }
                if (dataMap.containsKey("termtaxamountinbase")) {
                    termmap.setTermtaxamountinbase((Double) dataMap.get("termtaxamountinbase"));
                }
                if (dataMap.containsKey("termAmountExcludingTax")) {
                    termmap.setTermAmountExcludingTax((Double) dataMap.get("termAmountExcludingTax"));
                }
                if (dataMap.containsKey("termAmountExcludingTaxInBase")) {
                    termmap.setTermAmountExcludingTaxInBase((Double) dataMap.get("termAmountExcludingTaxInBase"));
                }
                if (dataMap.containsKey("termamountinbase")) {
                    termmap.setTermamountinbase((Double) dataMap.get("termamountinbase"));
                }
                if (dataMap.containsKey("termtax") && dataMap.get("termtax") != null) {
                    Tax termtax = (Tax) get(Tax.class, (String) dataMap.get("termtax"));
                    termmap.setTermtax(termtax);
                }
//                if (dataMap.containsKey("termpercentage")) {
//                    termmap.setPercentage((Double) dataMap.get("termpercentage"));
//                }
//                if (dataMap.containsKey("quotationID") && quotation != null) {
//                    termmap.setQuotation(quotation);
//                }
//                if (dataMap.containsKey("term")) {
//                    InvoiceTermsSales term = (InvoiceTermsSales) get(InvoiceTermsSales.class, (String) dataMap.get("term"));
//                    termmap.setTerm(term);
//                }
//                if (dataMap.containsKey("userid")) {
//                    User userid = (User) get(User.class, (String) dataMap.get("userid"));
//                    termmap.setCreator(userid);
//                }
//                if (dataMap.containsKey("createdon")) {
//                    termmap.setCreatedOn(((Date) dataMap.get("creationdate")).getTime());
//                }
                saveOrUpdate(termmap);
                list.add(termmap);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.saveQuotationTermMap : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public KwlReturnObject deleteQuotationTermMap(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String quotationID = hm.get("quotationid").toString();
            String query = "delete from quotationtermmap where quotation = ?";
            executeSQLUpdate( query, new Object[]{quotationID});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.deleteQuotationTermMap:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject deleteSOTermMap(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String quotationID = null;
            if(hm.containsKey("soid")) {
                quotationID = hm.get("soid").toString();
            }
            String query = "delete from salesordertermmap where salesorder = ?";
            executeSQLUpdate( query, new Object[]{quotationID});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.deleteSOTermMap:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getQuotationTermMap(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String quotationID = hm.get("quotation").toString();
            String query = "from QuotationTermMap where quotation.ID = ?";
            list = executeQuery( query, new Object[]{quotationID});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getQuotationTermMap:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject approvePendingCustomerQuotation(String cqID, String companyid, int status) throws ServiceException {
        String query = "update Quotation set approvestatuslevel = ? where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{status, cqID, companyid});
        return new KwlReturnObject(true, "Customer Quotation has been updated successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject approvePendingSalesOrder(String soID, String companyid, int status) throws ServiceException {
        String query = "update SalesOrder set approvestatuslevel = ? where ID=? and company.companyID=?";
        int numRows = executeUpdate( query, new Object[]{status, soID, companyid});
        return new KwlReturnObject(true, "Sales Order has been updated successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject rejectPendingCustomerQuotation(String cqID, String companyid) throws ServiceException {
        try {
            String query = "update Quotation set deleted=true,approvestatuslevel = (-approvestatuslevel) where ID=? and company.companyID=?";
            int numRows = executeUpdate( query, new Object[]{cqID, companyid});
            return new KwlReturnObject(true, "Customer Quotation has been rejected successfully.", null, null, numRows);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.rejectPendingCustomerQuotation : " + ex.getMessage(), ex);
        }
    }

    @Override
    public KwlReturnObject rejectPendingSalesOrder(String soID, String companyid) throws ServiceException {
        try {
            String query = "update SalesOrder set deleted=true,approvestatuslevel = (-approvestatuslevel) where ID=? and company.companyID=?";
            int numRows = executeUpdate( query, new Object[]{soID, companyid});
            return new KwlReturnObject(true, "Sales Order has been rejected successfully.", null, null, numRows);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.rejectPendingSalesOrder : " + ex.getMessage(), ex);
        }
    }
    @Override
    public KwlReturnObject deleteBatchDetailsAfterRejectPendingSalesOrder(String soID, String companyid) throws ServiceException {
        int numtotal=0;
        ArrayList params13 = new ArrayList();
        params13.add(companyid);
        params13.add(soID);
        String docids="";
        String batchmapids="",serialmapids="";
        try {
            String myquery3 = " select sod.id from sodetails sod inner join salesorder so on so.id=sod.salesorder where so.company =? and so.id=?";
        List listBatch = executeSQLQuery( myquery3, params13.toArray());
        Iterator itrBatch = listBatch.iterator();
        while (itrBatch.hasNext()) {
            String batchstring = itrBatch.next().toString();
            docids += "'" + batchstring + "',";
        }
        if (!StringUtil.isNullOrEmpty(docids)) {
            docids = docids.substring(0, docids.length() - 1);
        }
        if (!StringUtil.isNullOrEmpty(docids)) {
            ArrayList params14 = new ArrayList();
            String myquery4 = " select batchmapid,id from locationbatchdocumentmapping where documentid in (" + docids + ") and isconsignment='F'";
            String myquery5 = " select serialid,id from serialdocumentmapping where documentid in (" + docids + ") and isconsignment='F' ";
            List list4 = executeSQLQuery( myquery4, params14.toArray());
            Iterator itr4 = list4.iterator();
            while (itr4.hasNext()) {
                Object[] objArr = (Object[]) itr4.next();
                LocationBatchDocumentMapping locationBatchDocumentMapping = (LocationBatchDocumentMapping) get(LocationBatchDocumentMapping.class, (String) objArr[1]);
                if (locationBatchDocumentMapping != null && locationBatchDocumentMapping.getBatchmapid()!=null) {
                    HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                    batchUpdateQtyMap.put("id", locationBatchDocumentMapping.getBatchmapid().getId());
                    batchUpdateQtyMap.put("lockquantity", -locationBatchDocumentMapping.getQuantity());
                    saveBatchAmountDue(batchUpdateQtyMap);
                }
            }
            list4 = executeSQLQuery( myquery5, params14.toArray());
            itr4 = list4.iterator();
            while (itr4.hasNext()) {
                Object[] objArr = (Object[]) itr4.next();
                SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) get(SerialDocumentMapping.class, (String) objArr[1]);
                if (serialDocumentMapping != null) {
                    HashMap<String, Object> serialUpdateQtyMap = new HashMap<String, Object>();
                    serialUpdateQtyMap.put("id", serialDocumentMapping.getSerialid().getId());
                    serialUpdateQtyMap.put("lockquantity", "-1");
                    saveSerialAmountDue(serialUpdateQtyMap);
                }
            }
        }
        return new KwlReturnObject(true, "BatchDetails of Pending SalesOrder deleted successfully.", null, null, numtotal);
        }catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.deleteBatchDetailsAfterRejectPendingSalesOrder : " + ex.getMessage(), ex);
        }
    }

    @Override
    public KwlReturnObject saveSalesOrderTermMap(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            SalesOrderTermMap termmap = new SalesOrderTermMap();
            SalesOrder salesOrder = null;
            if (dataMap.containsKey("term") && dataMap.containsKey("salesOrderID")) {
                salesOrder = (SalesOrder) get(SalesOrder.class, (String) dataMap.get("salesOrderID"));
                InvoiceTermsSales term = (InvoiceTermsSales) get(InvoiceTermsSales.class, (String) dataMap.get("term"));
                List<SalesOrderTermMap> listTermMap = find("from SalesOrderTermMap where salesOrder.ID = '" + salesOrder.getID() + "' and term.id = '" + term.getId() + "'");
                if (listTermMap.size() > 0) {
                    termmap = listTermMap.get(0);
                }
                termmap.setTerm(term);
            }
            if (dataMap.containsKey("termamount")) {
                termmap.setTermamount((Double) dataMap.get("termamount"));
            }
            if (dataMap.containsKey("termtaxamount")) {
                termmap.setTermtaxamount((Double) dataMap.get("termtaxamount"));
            }
            if (dataMap.containsKey("termtaxamountinbase")) {
                termmap.setTermtaxamountinbase((Double) dataMap.get("termtaxamountinbase"));
            }
            if (dataMap.containsKey("termAmountExcludingTax")) {
                termmap.setTermAmountExcludingTax((Double) dataMap.get("termAmountExcludingTax"));
            }
            if (dataMap.containsKey("termAmountExcludingTaxInBase")) {
                termmap.setTermAmountExcludingTaxInBase((Double) dataMap.get("termAmountExcludingTaxInBase"));
            }
            if (dataMap.containsKey("termamountinbase")) {
                termmap.setTermamountinbase((Double) dataMap.get("termamountinbase"));
            }
            if (dataMap.containsKey("termtax") && dataMap.get("termtax") != null) {
                Tax termtax = (Tax) get(Tax.class, (String) dataMap.get("termtax"));
                termmap.setTermtax(termtax);
            }
            if (dataMap.containsKey("termpercentage")) {
                termmap.setPercentage((Double) dataMap.get("termpercentage"));
            }
            if (dataMap.containsKey("salesOrderID") && salesOrder != null) {
                termmap.setSalesOrder(salesOrder);
            }

            if (dataMap.containsKey("userid")) {
                User userid = (User) get(User.class, (String) dataMap.get("userid"));
                termmap.setCreator(userid);
            }
            if (dataMap.containsKey("createdon")) {
                termmap.setCreatedOn(((Date) dataMap.get("creationdate")).getTime());
            }
            saveOrUpdate(termmap);
            list.add(termmap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.saveSalesOrderTermMap : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public KwlReturnObject updateSalesOrderTermMap(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            SalesOrderTermMap termmap = new SalesOrderTermMap();
            
            if (dataMap.containsKey("ordertermid")) {
                termmap = (SalesOrderTermMap) get(SalesOrderTermMap.class, (String) dataMap.get("ordertermid"));
            }
            
            if(termmap != null){
                if (dataMap.containsKey("termamount")) {
                    termmap.setTermamount((Double) dataMap.get("termamount"));
                }
                if (dataMap.containsKey("termtaxamount")) {
                    termmap.setTermtaxamount((Double) dataMap.get("termtaxamount"));
                }
                if (dataMap.containsKey("termtaxamountinbase")) {
                    termmap.setTermtaxamountinbase((Double) dataMap.get("termtaxamountinbase"));
                }
                if (dataMap.containsKey("termAmountExcludingTax")) {
                    termmap.setTermAmountExcludingTax((Double) dataMap.get("termAmountExcludingTax"));
                }
                if (dataMap.containsKey("termAmountExcludingTaxInBase")) {
                    termmap.setTermAmountExcludingTaxInBase((Double) dataMap.get("termAmountExcludingTaxInBase"));
                }
                if (dataMap.containsKey("termamountinbase")) {
                    termmap.setTermamountinbase((Double) dataMap.get("termamountinbase"));
                }
                if (dataMap.containsKey("termtax") && dataMap.get("termtax") != null) {
                    Tax termtax = (Tax) get(Tax.class, (String) dataMap.get("termtax"));
                    termmap.setTermtax(termtax);
                }
//                if (dataMap.containsKey("termpercentage")) {
//                    termmap.setPercentage((Double) dataMap.get("termpercentage"));
//                }
//                if (dataMap.containsKey("quotationID") && quotation != null) {
//                    termmap.setQuotation(quotation);
//                }
//                if (dataMap.containsKey("term")) {
//                    InvoiceTermsSales term = (InvoiceTermsSales) get(InvoiceTermsSales.class, (String) dataMap.get("term"));
//                    termmap.setTerm(term);
//                }
//                if (dataMap.containsKey("userid")) {
//                    User userid = (User) get(User.class, (String) dataMap.get("userid"));
//                    termmap.setCreator(userid);
//                }
//                if (dataMap.containsKey("createdon")) {
//                    termmap.setCreatedOn(((Date) dataMap.get("creationdate")).getTime());
//                }
                saveOrUpdate(termmap);
                list.add(termmap);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.saveQuotationTermMap : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject getSalesOrderTermMap(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String salesOrderID = hm.get("salesOrder").toString();
            String query = "from SalesOrderTermMap where salesOrder.ID = ?";
            list = executeQuery( query, new Object[]{salesOrderID});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getSalesOrderTermMap:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject updateQuotationCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            String quotationRefId = (String) requestParams.get("accquotationcustomdataref");
            Quotation quotation = (Quotation) get(Quotation.class, quotationRefId);
            if (requestParams.containsKey("accquotationcustomdataref")) {
                QuotationCustomData quotationCustomData = null;
                quotationCustomData = (QuotationCustomData) get(QuotationCustomData.class, (String) requestParams.get("accquotationcustomdataref"));
                quotation.setQuotationCustomData(quotationCustomData);
            }
            saveOrUpdate(quotation);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.updateQuotationCustomData:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }

    @Override
    public KwlReturnObject updateQuotationVersionCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            String quotationRefId = (String) requestParams.get("accquotationcustomdataref");
            QuotationVersion quotation = (QuotationVersion) get(QuotationVersion.class, quotationRefId);
            if (requestParams.containsKey("accquotationcustomdataref")) {
                QuotationVersionCustomData quotationCustomData = null;
                quotationCustomData = (QuotationVersionCustomData) get(QuotationVersionCustomData.class, (String) requestParams.get("accquotationcustomdataref"));
                quotation.setQuotationCustomData(quotationCustomData);
            }
            saveOrUpdate(quotation);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.updateQuotationCustomData:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }

    @Override
    public KwlReturnObject updateQuotationDetailsCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            String quotationRefId = (String) requestParams.get("qdetailscustomdataref");
            QuotationDetail quotationDetail = (QuotationDetail) get(QuotationDetail.class, quotationRefId);
            if (requestParams.containsKey("qdetailscustomdataref")) {
                QuotationDetailCustomData quotationDetailCustomData = null;
                quotationDetailCustomData = (QuotationDetailCustomData) get(QuotationDetailCustomData.class, (String) requestParams.get("qdetailscustomdataref"));
                quotationDetail.setQuotationDetailCustomData(quotationDetailCustomData);
            }
            saveOrUpdate(quotationDetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.updateQuotationCustomData:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }

    @Override
    public KwlReturnObject updateQuotationDetailsProductCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            String quotationRefId = (String) requestParams.get("qdetailscustomdataref");
            QuotationDetail quotationDetail = (QuotationDetail) get(QuotationDetail.class, quotationRefId);
            if (requestParams.containsKey("qdetailscustomdataref")) {
                QuotationDetailsProductCustomData quotationDetailProductCustomData = null;
                quotationDetailProductCustomData = (QuotationDetailsProductCustomData) get(QuotationDetailsProductCustomData.class, (String) requestParams.get("qdetailscustomdataref"));
                quotationDetail.setQuotationDetailProductCustomData(quotationDetailProductCustomData);
            }
            saveOrUpdate(quotationDetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.updateQuotationDetailsProductCustomData:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }
    @Override
    public KwlReturnObject updateQuotationVersionDetailsCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            String quotationRefId = (String) requestParams.get("qdetailscustomdataref");
            QuotationVersionDetail quotationDetail = (QuotationVersionDetail) get(QuotationVersionDetail.class, quotationRefId);
            if (requestParams.containsKey("qdetailscustomdataref")) {
                QuotationVersionDetailCustomData quotationDetailCustomData = null;
                quotationDetailCustomData = (QuotationVersionDetailCustomData) get(QuotationVersionDetailCustomData.class, (String) requestParams.get("qdetailscustomdataref"));
                quotationDetail.setQuotationDetailCustomData(quotationDetailCustomData);
            }
            saveOrUpdate(quotationDetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.updateQuotationCustomData:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }

    @Override
    public KwlReturnObject deleteQuotationDetails(String quotationid, String companyid) throws ServiceException {
        //Delete Goods Receipt Details
        try {
            ArrayList params8 = new ArrayList();
            params8.add(quotationid); 
            params8.add(companyid);
//            String myquery = " select id from quotationdetails where quotation in (select id from quotation where id=? and company = ?) ";
            String myquery = "select qd.id from quotationdetails qd inner join quotation q on qd.quotation =q.id where q.id=? and q.company = ?";
            List list = executeSQLQuery( myquery, params8.toArray());
            Iterator itr = list.iterator();
            String idStrings = "";
            while (itr.hasNext()) {
                String invdid = itr.next().toString();
                idStrings += "'" + invdid + "',";
            }
            int numRows = 0;
            if (!StringUtil.isNullOrEmpty(idStrings)) {
                idStrings = idStrings.substring(0, idStrings.length() - 1);
                ArrayList params1 = new ArrayList();
                String deletecustomdetails = "delete  from quotationdetailscustomdata where quotationdetailsid in (" + idStrings + ")";
                int numRows1 = executeSQLUpdate(deletecustomdetails, params1.toArray());
                String deleteproductcustomdetails = "delete  from cqdetailproductcustomdata where cqDetailID in (" + idStrings + ")";
                int numRows2 = executeSQLUpdate(deleteproductcustomdetails, params1.toArray());
                String delQuery = "delete from QuotationDetail qd where qd.quotation.ID=? and qd.company.companyID=?";
                numRows = executeUpdate(delQuery, new Object[]{quotationid, companyid});
                deleteGstTaxClassDetails(idStrings);
            }
            return new KwlReturnObject(true, "Quotation Details has been deleted successfully.", null, null, numRows);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot Edit Quotation.", ex);
        }
    }
    public void deleteGstTaxClassDetails(String docrefid) throws ServiceException {
        if (!StringUtil.isNullOrEmpty(docrefid)) {
            String delQuery = " delete from gsttaxclasshistory where refdocid IN (" + docrefid + ")";
            executeSQLUpdate(delQuery);
        }
    }

    public void deleteGstDocHistoryDetails(String docrefid) throws ServiceException {
        if (!StringUtil.isNullOrEmpty(docrefid)) {
            String delQuery = " delete from gstdocumenthistory where refdocid=?";
            executeSQLUpdate(delQuery, new Object[]{docrefid});
        }
    }
    @Override
    public KwlReturnObject updateProductReplacementDetails(HashMap<String, Object> requestMap) throws ServiceException {

        List list = new ArrayList();

        String productReplacementDetailId = (String) requestMap.get("productReplacementDetailId");

        ProductReplacementDetail productReplacementDetail = (ProductReplacementDetail) get(ProductReplacementDetail.class, productReplacementDetailId);

        productReplacementDetail = buildProductReplacementDetail(productReplacementDetail, requestMap);

        saveOrUpdate(productReplacementDetail);
        list.add(productReplacementDetail);
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    private ProductReplacementDetail buildProductReplacementDetail(ProductReplacementDetail productReplacementDetail, HashMap<String, Object> requestMap) {

        if (requestMap.containsKey("companyId") && requestMap.get("companyId") != null) {
            String companyId = (String) requestMap.get("companyId");
            Company company = (Company) get(Company.class, companyId);
            productReplacementDetail.setCompany(company);
        }

        if (requestMap.containsKey("totalReplacedQuantity") && requestMap.get("totalReplacedQuantity") != null) {
            productReplacementDetail.setReplacedQuantity((Double) requestMap.get("totalReplacedQuantity"));
        }

        return productReplacementDetail;
    }

    @Override
    public KwlReturnObject saveProductMaintenance(HashMap<String, Object> requestMap) throws ServiceException {

        List list = new ArrayList();
        Maintenance maintenance = new Maintenance();

        maintenance = buildProductMaintenance(maintenance, requestMap);

        save(maintenance);
        list.add(maintenance);
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject updateProductMaintenance(HashMap<String, Object> requestMap) throws ServiceException {

        List list = new ArrayList();

        String maintenanceId = (String) requestMap.get("maintenanceId");

        Maintenance maintenance = (Maintenance) get(Maintenance.class, maintenanceId);

        maintenance = buildProductMaintenance(maintenance, requestMap);

        saveOrUpdate(maintenance);
        list.add(maintenance);
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject changeContractStatus(String contractid, String companyid) throws ServiceException {

        List list = new ArrayList();

        Contract contract = (Contract) get(Contract.class, contractid);
        if (contract != null) {
            contract.setCstatus(2);
            saveOrUpdate(contract);
            list.add(contract);

        }
        return new KwlReturnObject(true, null, null, list, list.size());

    }

    @Override
    public KwlReturnObject changeContractSRStatus(String contractid, int statusid) throws ServiceException {

        List list = new ArrayList();

        Contract contract = (Contract) get(Contract.class, contractid);
        if (contract != null) {
            contract.setSrstatus(statusid);
            saveOrUpdate(contract);
            list.add(contract);

        }
        return new KwlReturnObject(true, null, null, list, list.size());

    }

    private Maintenance buildProductMaintenance(Maintenance maintenance, HashMap<String, Object> requestMap) {

        if (requestMap.containsKey("maintainanceid") && requestMap.get("maintainanceid") != null) {
            String maintainanceid = (String) requestMap.get("maintainanceid");
            maintenance.setId(maintainanceid);
        }

        if (requestMap.containsKey("companyId") && requestMap.get("companyId") != null) {
            String companyId = (String) requestMap.get("companyId");

            Company company = (Company) get(Company.class, companyId);
            maintenance.setCompany(company);
        }

        if (requestMap.containsKey("customerId") && requestMap.get("customerId") != null) {
            Customer customer = (Customer) get(Customer.class, (String) requestMap.get("customerId"));
            maintenance.setCustomer(customer);
        }

        if (requestMap.containsKey("contractId") && requestMap.get("contractId") != null) {
            Contract contract = (Contract) get(Contract.class, (String) requestMap.get("contractId"));
            maintenance.setContract(contract);
        }

        if (requestMap.containsKey("isClosed") && requestMap.get("isClosed") != null) {
            maintenance.setClosed((Boolean) requestMap.get("isClosed"));
        }

        if (requestMap.containsKey("isSalesContractMaintenance") && requestMap.get("isSalesContractMaintenance") != null) {
            maintenance.setSalesContractMaintenance((Boolean) requestMap.get("isSalesContractMaintenance"));
        }

        if (requestMap.containsKey("maintenanceNumber") && requestMap.get("maintenanceNumber") != null) {
            maintenance.setMaintenanceNumber((String) requestMap.get("maintenanceNumber"));
        }

        if (requestMap.containsKey("maintainanceamt") && requestMap.get("maintainanceamt") != null) {
            maintenance.setMaintenanceAmount((Double) requestMap.get("maintainanceamt"));
        }

        return maintenance;
    }

    @Override
    public KwlReturnObject saveProductReplacement(HashMap<String, Object> requestMap) throws ServiceException {

        List list = new ArrayList();
        ProductReplacement productReplacement = new ProductReplacement();

        productReplacement = buildProductReplacement(productReplacement, requestMap);

        save(productReplacement);
        list.add(productReplacement);
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject updateProductReplacement(HashMap<String, Object> requestMap) throws ServiceException {

        List list = new ArrayList();

        ProductReplacement productReplacement = null;
        if(requestMap.containsKey("productReplacement")){
            productReplacement = (ProductReplacement)requestMap.get("productReplacement");
        }
        else{
            String productReplacementId = (String) requestMap.get("productReplacementId");
            productReplacement = (ProductReplacement) get(ProductReplacement.class, productReplacementId);
        }
        productReplacement = buildProductReplacement(productReplacement, requestMap);

        saveOrUpdate(productReplacement);
        list.add(productReplacement);
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public ProductReplacement buildProductReplacement(ProductReplacement productReplacement, HashMap<String, Object> requestMap) {

        if (requestMap.containsKey("replacementid") && requestMap.get("replacementid") != null) {
            String replacementid = (String) requestMap.get("replacementid");
            productReplacement.setId(replacementid);
        }

        if (requestMap.containsKey("companyId") && requestMap.get("companyId") != null) {
            String companyId = (String) requestMap.get("companyId");
            Company company = (Company) get(Company.class, companyId);
            productReplacement.setCompany(company);
        }

        if (requestMap.containsKey("replacementRequestNumber") && requestMap.get("replacementRequestNumber") != null) {
            productReplacement.setReplacementRequestNumber((String) requestMap.get("replacementRequestNumber"));
        }

        if (requestMap.containsKey("productReplacementDetails") && requestMap.get("productReplacementDetails") != null) {
            Set<ProductReplacementDetail> productReplacementDetails = (Set<ProductReplacementDetail>) requestMap.get("productReplacementDetails");
            productReplacement.setProductReplacementDetails(productReplacementDetails);
        }

        if (requestMap.containsKey("customerId") && requestMap.get("customerId") != null) {
            Customer customer = (Customer) get(Customer.class, (String) requestMap.get("customerId"));
            productReplacement.setCustomer(customer);
        }
        
        if (requestMap.containsKey("isClosed") && requestMap.get("isClosed") != null) {
            productReplacement.setClosed((Boolean) requestMap.get("isClosed"));
        }

        if (requestMap.containsKey("contractId") && requestMap.get("contractId") != null) {
            Contract contract = (Contract) get(Contract.class, (String) requestMap.get("contractId"));
            productReplacement.setContract(contract);
        }

        if (requestMap.containsKey("isSalesContractReplacement") && requestMap.get("isSalesContractReplacement") != null) {
            productReplacement.setSalesContractReplacement((Boolean) requestMap.get("isSalesContractReplacement"));
        }
        if(requestMap.containsKey("description") && requestMap.get("description")!=null){
            productReplacement.setDescription((String) requestMap.get("description"));
        }

        return productReplacement;
    }

    @Override
    public KwlReturnObject getSalesOrderEditCount(String entryNumber, String companyid, String soid) throws ServiceException {
        try {
            List list = new ArrayList();
            int count = 0;
            String q = "from SalesOrder where salesOrderNumber=? and company.companyID=? and ID!=? AND isDraft='F'";    //SDP-13487 - Do not check duplicate in Draft Report. Because Multiple draft records having empty entry no.
            list = executeQuery( q, new Object[]{entryNumber, companyid, soid});
            count = list.size();
            return new KwlReturnObject(true, "", null, list, count);

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getSalesOrderEditCount:" + ex.getMessage(), ex);
        }
    }

    @Override
    public KwlReturnObject getEditQuotationCount(String entryNumber, String companyid, String quotationId) throws ServiceException {
        try {
            List list = new ArrayList();
            int count = 0;
            String q = "from Quotation where quotationNumber=? and company.companyID=? and ID!=? AND isDraft='F'";  //SDP-13487 - Do not check duplicate in Draft Report. Because Multiple draft records having empty entry no.
            list = executeQuery( q, new Object[]{entryNumber, companyid, quotationId});
            count = list.size();
            return new KwlReturnObject(true, "", null, list, count);

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getEditQuotationCount:" + ex.getMessage(), ex);
        }
    }

    @Override
    public KwlReturnObject getQuotationVersionCount(String quotationid, String companyid) throws ServiceException {
        try {
            List list = new ArrayList();
            int count = 0;
            String q = "from QuotationVersion where quotation.ID=? and company.companyID=?";
            list = executeQuery( q, new Object[]{quotationid, companyid});
            count = list.size();
            return new KwlReturnObject(true, "", null, list, count);

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getEditQuotationCount:" + ex.getMessage(), ex);
        }
    }

    @Override
    public KwlReturnObject saveContract(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String soid = (String) dataMap.get("id");

            Contract contract = new Contract();
            if (StringUtil.isNullOrEmpty(soid)) {
//                contract.setDeleted(false);
                contract.setSrstatus(1);
                contract.setCstatus(1);
                if (dataMap.containsKey("createdby")) {
                    User createdby = dataMap.get("createdby") == null ? null : (User) get(User.class, (String) dataMap.get("createdby"));
                    contract.setCreatedby(createdby);
                }
                if (dataMap.containsKey("modifiedby")) {
                    User modifiedby = dataMap.get("modifiedby") == null ? null : (User) get(User.class, (String) dataMap.get("modifiedby"));
                    contract.setModifiedby(modifiedby);
                }
                if (dataMap.containsKey("createdon")) {
                    contract.setCreatedon((Long) dataMap.get("createdon"));
                }
                if (dataMap.containsKey("updatedon")) {
                    contract.setUpdatedon((Long) dataMap.get("updatedon"));
                }
            } else {
                contract = (Contract) get(Contract.class, soid);
                if (dataMap.containsKey("modifiedby")) {
                    User modifiedby = dataMap.get("modifiedby") == null ? null : (User) get(User.class, (String) dataMap.get("modifiedby"));
                    contract.setModifiedby(modifiedby);
                }
                if (dataMap.containsKey("updatedon")) {
                    contract.setUpdatedon((Long) dataMap.get("updatedon"));
                }
            }

            if (dataMap.containsKey(Constants.SEQFORMAT)) {
                contract.setSeqformat((SequenceFormat) get(SequenceFormat.class, (String) dataMap.get(Constants.SEQFORMAT)));
            }
            if (dataMap.containsKey(Constants.SEQNUMBER)) {
                contract.setSeqnumber(Integer.parseInt(dataMap.get(Constants.SEQNUMBER).toString()));
            }
            if (dataMap.containsKey(Constants.DATEPREFIX) && dataMap.get(Constants.DATEPREFIX) != null) {
                contract.setDatePreffixValue((String) dataMap.get(Constants.DATEPREFIX));
            }
            if (dataMap.containsKey(Constants.DATEAFTERPREFIX) && dataMap.get(Constants.DATEAFTERPREFIX) != null) {
                contract.setDateAfterPreffixValue((String) dataMap.get(Constants.DATEAFTERPREFIX));
            }
            if (dataMap.containsKey(Constants.DATESUFFIX) && dataMap.get(Constants.DATESUFFIX) != null) {
                contract.setDateSuffixValue((String) dataMap.get(Constants.DATESUFFIX));
            }
            if (dataMap.containsKey("agreedservices")) {
                contract.setAgreedServices((Integer) dataMap.get("agreedservices"));
            }
            if (dataMap.containsKey("isNormalContract") && dataMap.get("isNormalContract") != null) {
                contract.setNormalContract((Boolean) dataMap.get("isNormalContract"));
            }
            if (dataMap.containsKey("contractStatus") && dataMap.get("contractStatus") != null) {
                contract.setCstatus((Integer) dataMap.get("contractStatus"));
            }
            if (dataMap.containsKey("termtype")) {
                contract.setTermType((String) dataMap.get("termtype"));
            }
            if (dataMap.containsKey("numberOfPeriods") && dataMap.get("numberOfPeriods") != null) {
                contract.setNumberOfPeriods(Integer.parseInt((String) dataMap.get("numberOfPeriods")));
            }
            if (dataMap.containsKey("frequencyType") && dataMap.get("frequencyType") != null) {
                contract.setInvoiceFrequency((String) dataMap.get("frequencyType"));
            }
            if (dataMap.containsKey("termvalue")) {
                contract.setTermValue(Integer.parseInt(dataMap.get("termvalue").toString()));
            }
            if (dataMap.containsKey("createdon")) {
                contract.setCreatedon((Long) dataMap.get("createdon"));
            }

            if (dataMap.containsKey("updatedon")) {

                contract.setUpdatedon((Long) dataMap.get("updatedon"));
            }

//            if (dataMap.containsKey("isOpeningBalanceOrder") && dataMap.get("isOpeningBalanceOrder") != null) {
//                contract.setIsOpeningBalanceSO((Boolean) dataMap.get("isOpeningBalanceOrder"));
//            }

            if (dataMap.containsKey("entrynumber")) {
                contract.setContractNumber((String) dataMap.get("entrynumber"));
            }
            if (dataMap.containsKey("salesorder")) {
                contract.setSalesOrderNumber((String) dataMap.get("salesorder"));
            }
            if (dataMap.containsKey("sono")) {  //ERP-30712 : New Column added to save SO No.
                contract.setSoNo((String) dataMap.get("sono"));
            }
            if (dataMap.containsKey("autogenerated")) {
                contract.setAutoGenerated((Boolean) dataMap.get("autogenerated"));
            }
            if (dataMap.containsKey("memo")) {
                contract.setMemo((String) dataMap.get("memo"));
            }
            if (dataMap.containsKey("contactperson")) {
                contract.setContactPerson((String) dataMap.get("contactperson"));
            }
            if (dataMap.containsKey("leaseAmount")) {
                contract.setAmount((Double) dataMap.get("leaseAmount"));
            }
            if (dataMap.containsKey("securityDeposite")) {
                contract.setDepositAmount((Double) dataMap.get("securityDeposite"));
            }
//            if (dataMap.containsKey("perDiscount")) {
//            	contract.setPerDiscount((Boolean) dataMap.get("perDiscount"));
//            }
//            if (dataMap.containsKey("discount")) {
//            	contract.setDiscount((Double) dataMap.get("discount"));
//            }
//            if (dataMap.containsKey("posttext")) {
//                contract.setPostText((String) dataMap.get("posttext"));
//            }
//            if(dataMap.containsKey("billto")){
//                if (dataMap.get("billto") != null) {
//                    contract.setBillTo((String) dataMap.get("billto"));
//                }
//            }
//            if(dataMap.containsKey("shipaddress")){
//                if (dataMap.get("shipaddress") != null) {
//                    contract.setShipTo((String) dataMap.get("shipaddress"));
//                }
//            }
            if (dataMap.containsKey("customerid")) {
                Customer customer = dataMap.get("customerid") == null ? null : (Customer) get(Customer.class, (String) dataMap.get("customerid"));
                contract.setCustomer(customer);
            }
            if (dataMap.containsKey("startdate") && dataMap.get("startdate") != null && !dataMap.get("startdate").toString().equals("")) {
                contract.setOrderDate((Date) dataMap.get("startdate"));
            }
            if (dataMap.containsKey("signinDate") && dataMap.get("signinDate") != null && !dataMap.get("signinDate").toString().equals("")) {
                contract.setSignDate((Date) dataMap.get("signinDate"));
            }
            if (dataMap.containsKey("enddate") && dataMap.get("enddate") != null && !dataMap.get("enddate").toString().equals("")) {
                contract.setEndDate((Date) dataMap.get("enddate"));
            }
            if (dataMap.containsKey("originalendDate") && dataMap.get("originalendDate") != null && !dataMap.get("originalendDate").toString().equals("")) {
                contract.setOriginalEndDate((Date) dataMap.get("originalendDate"));
            }
            if (dataMap.containsKey("moveindate") && dataMap.get("moveindate") != null && !dataMap.get("moveindate").toString().equals("")) {
                contract.setMoveDate((Date) dataMap.get("moveindate"));
            }
            if (dataMap.containsKey("moveoutdate") && dataMap.get("moveoutdate") != null && !dataMap.get("moveoutdate").toString().equals("")) {
                contract.setMoveOutDate((Date) dataMap.get("moveoutdate"));
            }
//            if (dataMap.containsKey("shipdate")) {
//            	contract.setShipdate((Date) dataMap.get("shipdate"));
//            }
//            if (dataMap.containsKey("shipvia")) {
//                contract.setShipvia((String) dataMap.get("shipvia"));
//            }
            if (dataMap.containsKey("emailid")) {
                contract.setEmailID((String) dataMap.get("emailid"));
            }
//            if (dataMap.containsKey("taxid")) {
//                Tax tax = dataMap.get("taxid")==null?null:(Tax) get(Tax.class, (String) dataMap.get("taxid"));
//                contract.setTax(tax);
//            }
//            if (dataMap.containsKey("costCenterId")) {
//                CostCenter costCenter = dataMap.get("costCenterId")==null?null:(CostCenter)get(CostCenter.class, (String) dataMap.get("costCenterId"));
//                contract.setCostcenter(costCenter);
//            }
            if (dataMap.containsKey("companyid")) {
                Company company = dataMap.get("companyid") == null ? null : (Company) get(Company.class, (String) dataMap.get("companyid"));
                contract.setCompany(company);
            }
            if (dataMap.containsKey("parentid")) {
                Contract contract1 = dataMap.get("parentid") == null ? null : (Contract) get(Contract.class, (String) dataMap.get("parentid"));
                contract.setParentContract(contract1);
            }
            if (dataMap.containsKey("sodetails")) {
                if (dataMap.get("sodetails") != null) {
                    contract.setRows((Set<ContractDetail>) dataMap.get("sodetails"));
                }
            }
            if (dataMap.containsKey("currencyid")) {
                contract.setCurrency((KWLCurrency) get(KWLCurrency.class, (String) dataMap.get("currencyid")));
            }

//            if (dataMap.containsKey("isfavourite")) {
//                if (dataMap.get("isfavourite") != null) {
//                    contract.setFavourite(Boolean.parseBoolean(dataMap.get("isfavourite").toString()));
//                }
//            }
//                    
//            if (dataMap.containsKey(Constants.MARKED_PRINTED)) {
//                if (dataMap.get(Constants.MARKED_PRINTED) != null) {
//                    contract.setPrinted(Boolean.parseBoolean(dataMap.get(Constants.MARKED_PRINTED).toString()));
//                }
//            }
            if (dataMap.containsKey("contractcustomdataref")) {
                ContractCustomData cmp = (dataMap.get("contractcustomdataref") == null ? null : (ContractCustomData) get(ContractCustomData.class, (String) dataMap.get("contractcustomdataref")));
                contract.setContractCustomData(cmp);
            }
//            if(dataMap.containsKey("leaseStatus")){
//                MasterItem mi=(dataMap.get("leaseStatus")==null?null:(MasterItem) get(MasterItem.class, (String) dataMap.get("leaseStatus")));
//                contract.setCstatus(mi);
//            }
//            if(dataMap.containsKey("leaseStatus")){
//                int status=Integer.parseInt((String)dataMap.get("leaseStatus"));
////                MasterItem mi=(dataMap.get("leaseStatus")==null?null:(MasterItem) get(MasterItem.class, (String) dataMap.get("leaseStatus")));
//                contract.setCstatus(status);
//            }
//            if(dataMap.containsKey("termid")){
//                Term term = (dataMap.get("termid")==null?null:(Term) get(Term.class, (String) dataMap.get("termid")));
//                contract.setTerm(term);
//            }
//            if (dataMap.containsKey("billshipAddressid")) {
//                BillingShippingAddresses bsa = dataMap.get("billshipAddressid")==null?null:(BillingShippingAddresses) get(BillingShippingAddresses.class, (String) dataMap.get("billshipAddressid"));
//                contract.setBillingShippingAddresses(bsa);
//            }

//             contract.setTemplateid((Projreport_Template) get(Projreport_Template.class, Constants.HEADER_IMAGE_TEMPLATE_ID));
            saveOrUpdate(contract);
            list.add(contract);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveSalesOrder : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject saveContractDetails(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String contratctid = (String) dataMap.get("id");

            ContractDetail contractDetail = new ContractDetail();
            if (!StringUtil.isNullOrEmpty(contratctid)) {
                contractDetail = (ContractDetail) get(ContractDetail.class, contratctid);
            }

            if (dataMap.containsKey("soid")) {
                Contract contract = dataMap.get("soid") == null ? null : (Contract) get(Contract.class, (String) dataMap.get("soid"));
                contractDetail.setContract(contract);
            }
            if (dataMap.containsKey("srno")) {
                contractDetail.setSrno((Integer) dataMap.get("srno"));
            }
            if (dataMap.containsKey("rate")) {
                contractDetail.setRate((Double) dataMap.get("rate"));
            }
            if (dataMap.containsKey("unitPricePerInvoice") && dataMap.get("unitPricePerInvoice") != null) {
                contractDetail.setUnitPricePerInvoice((Double) dataMap.get("unitPricePerInvoice"));
            }
            if (dataMap.containsKey("quantity")) {
                contractDetail.setQuantity((Double) dataMap.get("quantity"));
            }
            if (dataMap.containsKey("uomid")) {
                contractDetail.setUom((UnitOfMeasure) get(UnitOfMeasure.class, dataMap.get("uomid").toString()));
            }
            if (dataMap.containsKey("baseuomquantity") && dataMap.get("baseuomquantity") != null && dataMap.get("baseuomquantity") != "") {
                contractDetail.setBaseuomquantity((Double) dataMap.get("baseuomquantity"));
//            } else {
//                if (dataMap.containsKey("quantity")) {
//                    salesOrderDetail.setBaseuomquantity((Double) dataMap.get("quantity"));
//                }
            }
            if (dataMap.containsKey("baseuomrate") && dataMap.get("baseuomrate") != null && dataMap.get("baseuomrate") != "") {
                contractDetail.setBaseuomrate((Double) dataMap.get("baseuomrate"));
//            } else {
//                salesOrderDetail.setBaseuomrate(1);
            }
            if (dataMap.containsKey("remark")) {
                contractDetail.setRemark(StringUtil.DecodeText(StringUtil.isNullOrEmpty((String) dataMap.get("remark")) ? "" : (String) dataMap.get("remark")));
            }
            if (dataMap.containsKey("desc")) {
                contractDetail.setDescription((String) dataMap.get("desc"));
            }
            if (dataMap.containsKey("discount")) {
                contractDetail.setDiscount((Double) dataMap.get("discount"));
            }
            if (dataMap.containsKey("discountispercent")) {
                contractDetail.setDiscountispercent((Integer) dataMap.get("discountispercent"));
            } else {
                contractDetail.setDiscountispercent(1);
            }
            if (dataMap.containsKey("productid")) {
                Product product = dataMap.get("productid") == null ? null : (Product) get(Product.class, (String) dataMap.get("productid"));
                contractDetail.setProduct(product);
            }
            if (dataMap.containsKey("companyid")) {
                Company company = dataMap.get("companyid") == null ? null : (Company) get(Company.class, (String) dataMap.get("companyid"));
                contractDetail.setCompany(company);
            }
//            if(dataMap.containsKey("salesordercustomdataref")){
//                SalesOrderDetailsCustomData cmp = (dataMap.get("salesordercustomdataref")==null?null:(SalesOrderDetailsCustomData) get(SalesOrderDetailsCustomData.class, (String) dataMap.get("salesordercustomdataref")));
//                contractDetail.setSoDetailCustomData(cmp);
//            }
            if (dataMap.containsKey("rowtaxid")) {
                Tax rowtax = (dataMap.get("rowtaxid") == null ? null : (Tax) get(Tax.class, (String) dataMap.get("rowtaxid")));
                contractDetail.setTax(rowtax);
            }
            if (dataMap.containsKey("rowTaxAmount")) {
                double rowTaxAmount = (Double) dataMap.get("rowTaxAmount");
                contractDetail.setRowTaxAmount(rowTaxAmount);
            }
            if (dataMap.containsKey("rowTermAmount")) {
                double rowTermAmount = (Double) dataMap.get("rowTermAmount");
                contractDetail.setRowTermAmount(rowTermAmount);
            }

//            if (dataMap.containsKey("quotationdetailid")) {
//                QuotationDetail quotationdetail = dataMap.get("quotationdetailid")==null?null:(QuotationDetail) get(QuotationDetail.class, (String) dataMap.get("quotationdetailid"));
//                contractDetail.s(quotationdetail);
//            }
            saveOrUpdate(contractDetail);
            list.add(contractDetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveSalesOrderDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    @Override
    public KwlReturnObject updateContractReference(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            String contractRefId = (String) requestParams.get("contractcustomdataref");
            ContractDetail contractDetail = (ContractDetail) get(ContractDetail.class, contractRefId);
            if (requestParams.containsKey("contractcustomdataref")) {
                ContractDetailCustomData contractDetailCustomData = null;
                contractDetailCustomData = (ContractDetailCustomData) get(ContractDetailCustomData.class, (String) requestParams.get("contractcustomdataref"));
                contractDetail.setContractdetailcustomdata(contractDetailCustomData);
            }
            saveOrUpdate(contractDetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.updateContractReference:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }

    public KwlReturnObject saveContractServiceDetails(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String serviceDetailid = (String) dataMap.get("id");

            ServiceDetail serviceDetail = null;
            if (!StringUtil.isNullOrEmpty(serviceDetailid)) {
                serviceDetail = (ServiceDetail) get(ServiceDetail.class, serviceDetailid);
            }
            if (serviceDetail == null) {
                serviceDetail = new ServiceDetail();
            }
            if (dataMap.containsKey("serviceDate")) {
                serviceDetail.setServiceDate((Date) dataMap.get("serviceDate"));
            }

            if (dataMap.containsKey("soid")) {
                Contract contract = dataMap.get("soid") == null ? null : (Contract) get(Contract.class, (String) dataMap.get("soid"));
                serviceDetail.setContract(contract);
            }
            if (dataMap.containsKey("companyid")) {
                Company company = dataMap.get("companyid") == null ? null : (Company) get(Company.class, (String) dataMap.get("companyid"));
                serviceDetail.setCompany(company);
            }
//            
//            }
            saveOrUpdate(serviceDetail);
            list.add(serviceDetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveSalesOrderDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject deleteContractDetails(String coid, String companyid) throws ServiceException {
        try {
            String delQuery = "delete from ContractDetail cod where cod.contract.ID=? and cod.company.companyID=?";
            int numRows = executeUpdate( delQuery, new Object[]{coid, companyid});
            return new KwlReturnObject(true, "Contract Details has been deleted successfully.", null, null, numRows);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot Edit Contract.", ex);//+ex.getMessage(), ex);as it is used in Customer Invoice already
        }
    }

    @Override
    public List selectContractDetails(String coid, String companyid) throws ServiceException {
        try {
            String delQuery = "select id from contractdetails cod where cod.contract=? and cod.company=?";
            List li = executeSQLQuery(delQuery, new Object[]{coid, companyid});
            return li;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot Edit Contract.", ex);//+ex.getMessage(), ex);as it is used in Customer Invoice already
        }
    }
     @Override
    public KwlReturnObject deleteContractDetailTermsMap(String coid, String companyid) throws ServiceException {
        try {
            String delQuery = "delete codtm.* from contractdetailtermsmap codtm where codtm.contractdetail=?";
            int numRows = executeSQLUpdate(delQuery, new Object[]{coid});
            return new KwlReturnObject(true, "Contract Detail Terms Map has been deleted successfully.", null, null,numRows);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot Edit Contract.", ex);//+ex.getMessage(), ex);as it is used in Customer Invoice already
        }
    }
    @Override
    public KwlReturnObject deleteServiceDetails(String coid, String companyid, String deletedRecs) throws ServiceException {
        try {
            String delQuery = "";
            int numRows = 0;
            if (!StringUtil.isNullOrEmpty(deletedRecs)) {
                String deletedRec = AccountingManager.getFilterInString(deletedRecs);
                String condition = " and sd.ID in " + deletedRec + " ";
                delQuery = "delete from ServiceDetail sd where sd.contract.ID=? and sd.company.companyID=? " + condition;
                numRows = executeUpdate( delQuery, new Object[]{coid, companyid});
            }


            return new KwlReturnObject(true, "Contract Details has been deleted successfully.", null, null, numRows);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot Edit Contract.", ex);//+ex.getMessage(), ex);as it is used in Customer Invoice already
        }
    }

    @Override
    public KwlReturnObject deletecontractMaintenanceSchedule(String coid, String companyid) throws ServiceException {
        try {
            String delQuery = "delete from AssetMaintenanceSchedulerObject where contractId=? and scheduleType=1 and company.companyID=?"; 
            int numRows = executeUpdate( delQuery, new Object[]{coid,companyid});
            return new KwlReturnObject(true, "Maintenance Schedule Details has been deleted successfully.", null, null, numRows);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot Delete Contract.", ex);
        }
    }
    @Override
    public KwlReturnObject deletecontractFiles(String coid) throws ServiceException {
        try {
            String delQuery = "delete from ContractFiles where contractid=? ";//and sd.company.companyID=?
            int numRows = executeUpdate( delQuery, new Object[]{coid});
            return new KwlReturnObject(true, "Contract Details has been deleted successfully.", null, null, numRows);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot Edit Contract.", ex);//+ex.getMessage(), ex);as it is used in Customer Invoice already
        }
    }

    @Override
    public KwlReturnObject getReplacementAndMaintenance(String coid) throws ServiceException {
        String selQuery1 = "select * from productreplacement where contract=?";
        List list1 = executeSQLQuery( selQuery1, new Object[]{coid});
        String selQuery2 = "select * from maintenance where contract=?";
        List list2 = executeSQLQuery( selQuery2, new Object[]{coid});
        List list = new ArrayList<Object>();
        list.addAll(list1);
        list.addAll(list2);
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getInvoiceAndDeliveryOrderOfContract(String coid) throws ServiceException {
        String selQuery1 = "select * from docontractmapping where contract=?";
        List list1 = executeSQLQuery( selQuery1, new Object[]{coid});
        String selQuery2 = "select * from invoicecontractmapping where contract=?";
        List list2 = executeSQLQuery( selQuery2, new Object[]{coid});
        List list = new ArrayList<Object>();
        list.addAll(list1);
        list.addAll(list2);
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject updateContractFiles(String contractid, String fileid) throws ServiceException {
        try {
            String delQuery = "update ContractFiles set contractid=? where id=?";
            int numRows = executeUpdate( delQuery, new Object[]{contractid, fileid});
            return new KwlReturnObject(true, "Contract Files Details has been Updated successfully.", null, null, numRows);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot Edit Contract.", ex);//+ex.getMessage(), ex);as it is used in Customer Invoice already
        }
    }

    @Override
    public KwlReturnObject getContractOrders(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        int count=0;
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            ArrayList params = new ArrayList();
            params.add(companyid);
            String condition = "";
            int viewFilter =0;               // 0 -All, 1= Active ,2=Terminated
            String start = (String) requestParams.get(Constants.start);
            String limit = (String) requestParams.get(Constants.limit);
            String startDate = (String) requestParams.get(Constants.REQ_startdate);
            String endDate = (String) requestParams.get(Constants.REQ_enddate);
            if (requestParams.containsKey("viewfilter") && !StringUtil.isNullOrEmpty(requestParams.get("viewfilter").toString())) {
                viewFilter = Integer.parseInt(requestParams.get("viewfilter").toString());
            }

            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                condition += " and (contr.orderDate >=? and contr.orderDate <=?) ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }

            String ss = (String) requestParams.get("ss");
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"contr.contractNumber", "contr.customer.name", "contr.contactPerson", "contr.soNo"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 4);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery;
            }


            if (requestParams.containsKey("isNormalContract") && requestParams.get("isNormalContract") != null) {
                condition += " and contr.normalContract=?";
                params.add((Boolean) requestParams.get("isNormalContract"));
            }

            if (viewFilter == 1 || viewFilter == 2) {
                condition += " and contr.cstatus=?";
                params.add(viewFilter);
            }

            String query = " from Contract contr where contr.company.companyID = ? " + condition;

            list = executeQuery(query, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeQueryPaging(query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl : getContractOrders " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getProductReplacementsLinkedWithSalesOrder(HashMap<String, Object> dataMap) throws ServiceException {

        String soId = (String) dataMap.get("salesOrderId");
        List params = new ArrayList();
        params.add(soId);
        String hql = "SELECT pr.id FROM salesorder so INNER JOIN sodetails sod ON sod.salesorder = so.id "
                + "INNER JOIN productreplacementdetail prd ON prd.id=sod.productreplacementdetail "
                + "INNER JOIN productreplacement pr ON pr.id=prd.productreplacement WHERE so.id=?";

        List list = executeSQLQuery( hql, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getContractOrderDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = " from ContractDetail ";
        return buildNExecuteQuery( query, requestParams);
    }

    @Override
    public KwlReturnObject getContractAgreedServices(HashMap<String, Object> requestParams) throws ServiceException {
        String query = " from ServiceDetail ";
        return buildNExecuteQuery( query, requestParams);
    }

    @Override
    public KwlReturnObject getContractFiles(HashMap<String, Object> requestParams) throws ServiceException {
        String query = " from ContractFiles ";
        return buildNExecuteQuery( query, requestParams);
    }

    @Override
    public KwlReturnObject getContractDetails(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String contractid = (String) requestParams.get("contractid");
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(contractid);

            String query = " from Contract where company.companyID = ? and ID = ? ";

            list = executeQuery( query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl : getContractDetails " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getContractOtherDetails(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String contractid = (String) requestParams.get("contractid");
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(contractid);

            String query = " from Contract where company.companyID = ? and ID = ? ";

            list = executeQuery( query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl : getContractOtherDetails " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getContractInvoiceDetails(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String contractid = (String) requestParams.get("contractid");
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(contractid);

            String query = " from InvoiceContractMapping where company.companyID = ? and contract.ID = ? ";

            list = executeQuery( query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl : getContractInvoiceDetails " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getContractNormalInvoiceDetails(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String contractid = (String) requestParams.get("contractid");
            ArrayList params = new ArrayList();

//            int leaseormaintenancesoFlag = 1;
//            
//            /*
//             * For normal contract only normal contracts should come
//             */
//            
//            if(requestParams.containsKey("isNormalContract") && requestParams.get("isNormalContract") != null && (Boolean) requestParams.get("isNormalContract")){
//                leaseormaintenancesoFlag = 0;
//            }

            Contract contract = (Contract) get(Contract.class, contractid);

            params.add(companyid);
            params.add(contractid);

            String mysqlQuery = " select invContrMap.id from invoicecontractmapping invContrMap "
                    + " left join deliveryorder do on do.id = invContrMap.deliveryorder "
                    + " left join dodetails dod on dod.deliveryorder = do.id "
                    + " left join sodetails sod on sod.id = dod.sodetails "
                    + " left join salesorder so on so.id = sod.salesorder "
                    + " where ((so.isreplacementso = 0 and (so.leaseormaintenanceso = 1 or so.leaseormaintenanceso = 0)) or (invContrMap.deliveryorder is null)) and invContrMap.company = ? and invContrMap.contract = ? ";


            String unionQuery = "select invContrMap.id from invoicecontractmapping invContrMap "
                    + "INNER JOIN invoice inv ON inv.id=invContrMap.invoice "
                    + "INNER JOIN invoicedetails invd ON invd.invoice=inv.id "
                    + "INNER JOIN sodetails sod ON invd.salesorderdetail=sod.id "
                    + "INNER JOIN salesorder so ON so.id=sod.salesorder "
                    + "WHERE so.isreplacementso = 0 and so.leaseormaintenanceso = 0 and invContrMap.company = ? and invContrMap.contract = ?";


            if (contract.isNormalContract()) {// in case of sales contract
                mysqlQuery = mysqlQuery + " UNION " + unionQuery;
                params.add(companyid);
                params.add(contractid);
            }


            list = executeSQLQuery( mysqlQuery, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl : getContractInvoiceDetails " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getContractReplacementInvoiceDetails(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String contractid = (String) requestParams.get("contractid");
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(contractid);

            Contract contract = (Contract) get(Contract.class, contractid);

            String mysqlQuery = " select invContrMap.id from invoicecontractmapping invContrMap "
                    + " inner join deliveryorder do on do.id = invContrMap.deliveryorder "
                    + " inner join dodetails dod on dod.deliveryorder = do.id "
                    + " inner join sodetails sod on sod.id = dod.sodetails "
                    + " inner join salesorder so on so.id = sod.salesorder "
                    + " where so.isreplacementso = 1 and invContrMap.company = ? and invContrMap.contract = ? ";


            String unionQuery = "select invContrMap.id from invoicecontractmapping invContrMap "
                    + "INNER JOIN invoice inv ON inv.id=invContrMap.invoice "
                    + "INNER JOIN invoicedetails invd ON invd.invoice=inv.id "
                    + "INNER JOIN sodetails sod ON invd.salesorderdetail=sod.id "
                    + "INNER JOIN salesorder so ON so.id=sod.salesorder "
                    + "WHERE so.isreplacementso = 1 and so.leaseormaintenanceso = 0 and invContrMap.company = ? and invContrMap.contract = ?";

            if (contract.isNormalContract()) {// if contract is normal sales contract
                mysqlQuery = mysqlQuery + " UNION " + unionQuery;
                params.add(companyid);
                params.add(contractid);
            }

            list = executeSQLQuery( mysqlQuery, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl : getContractReplacementInvoiceDetails " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getContractMaintenanceInvoiceDetails(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String contractid = (String) requestParams.get("contractid");
            ArrayList params = new ArrayList();

            params.add(companyid);
            params.add(contractid);
            params.add(contractid);

            Contract contract = (Contract) get(Contract.class, contractid);


            String mysqlQuery = " select inv.id from invoice inv  "
                    + " inner join invoicedetails invd on invd.invoice = inv.id  "
                    + " left join sodetails sod1 on sod1.id = invd.salesorderdetail  "
                    + " left join salesorder so1 on so1.id = sod1.salesorder "
                    + " left join maintenance mt1 on mt1.id = so1.maintenance "
                    + " left join dodetails dod2 on dod2.id = invd.deliveryorderdetail  "
                    + " left join sodetails sod2 on sod2.id = dod2.sodetails  "
                    + " left join salesorder so2 on so2.id = sod2.salesorder  "
                    + " left join maintenance mt2 on mt2.id = so2.maintenance "
                    + " where ((so1.isreplacementso = 0 and so1.leaseormaintenanceso=2) or (so2.isreplacementso = 0 and so2.leaseormaintenanceso=2)) "
                    + " and inv.company = ? "
                    + " and (mt1.contract = ? or mt2.contract = ?) ";

            String unionQuery = "SELECT inv.id from invoice inv "
                    + "INNER JOIN invoicedetails invd on invd.invoice = inv.id "
                    + "INNER JOIN sodetails sod ON invd.salesorderdetail = sod.id "
                    + "INNER JOIN salesorder so ON so.id=sod.salesorder "
                    + "INNER JOIN maintenance mn ON so.maintenance=mn.id "
                    + "WHERE so.isreplacementso=0 AND so.leaseormaintenanceso=2 and so.company = ? AND mn.contract=?";


            if (contract.isNormalContract()) {// if normal sales contrcat
                mysqlQuery = mysqlQuery + " UNION " + unionQuery;
                params.add(companyid);
                params.add(contractid);
            }

            list = executeSQLQuery( mysqlQuery, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl : getContractMaintenanceInvoiceDetails " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getContractNormalDOItemDetails(HashMap<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        int totalCount = 0;
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String contractid = (String) requestParams.get("contractid");
            ArrayList params = new ArrayList();

            params.add(companyid);
            params.add(contractid);

            boolean isNormalContract = false;

            if (requestParams.containsKey("isNormalContract") && requestParams.get("isNormalContract") != null) {
                isNormalContract = (Boolean) requestParams.get("isNormalContract");
            }

            String mysqlQuery = "";

            mysqlQuery = " select dod.id as dodid from dodetails dod "
                    + " inner join deliveryorder do on do.id = dod.deliveryorder "
                    + " inner join docontractmapping doContractMap on doContractMap.deliveryorder = do.id "
                    + " inner join product p on p.id = dod.product "
                    + " left join sodetails sod on sod.id = dod.sodetails "
                    + " left join salesorder so on so.id = sod.salesorder "
                    + " where so.isreplacementso = 0 and dod.company = ? and doContractMap.contract = ? ";


            String unionQuery = "SELECT dod.id as dodid from dodetails dod "
                    + "INNER JOIN invoicedetails invd ON dod.cidetails = invd.id "
                    + "INNER JOIN sodetails sod ON sod.id=invd.salesorderdetail "
                    + "INNER JOIN salesorder so ON so.id=sod.salesorder "
                    + "WHERE so.isreplacementso=0 AND so.company=? AND so.contract=?";


            if (isNormalContract) {
                mysqlQuery = mysqlQuery + " UNION " + unionQuery;
                params.add(companyid);
                params.add(contractid);
            }

            returnList = executeSQLQuery( mysqlQuery, params.toArray());
            totalCount = returnList.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl : getContractNormalDOItemDetails " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, totalCount);
    }

    @Override
    public KwlReturnObject getContractNormalDOItemDetailsRow(HashMap<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        int totalCount = 0;
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String contractid = (String) requestParams.get("contractid");
            String pid = (String) requestParams.get("pid");
            String doid = (String) requestParams.get("doid");
            ArrayList params = new ArrayList();

            // For Product Details
            params.add(companyid);
            params.add(contractid);
            params.add(doid);
            params.add(pid);

//            // For Asset Details
//            params.add(companyid);
//            params.add(contractid);
//            params.add(doid);
//            params.add(pid);

//            String mysqlQuery = " select bsr.id as serialID, bsr.name as serialName, pb.name as batchName, bsr.exptodate as warrentyExpireyDate from batchserial bsr "
//                    + " inner join productbatch pb on pb.id = bsr.batch "
//                    + " inner join dodetails dod on dod.batch = bsr.batch "
//                    + " inner join deliveryorder do on do.id = dod.deliveryorder "
//                    + " inner join docontractmapping doContractMap on doContractMap.deliveryorder = do.id "
//                    + " inner join product p on p.id = dod.product "
//                    + " where do.company = ? and doContractMap.contract = ? and do.id = ? and p.id = ? "
//                    + " union "
//                    + " select bsr.id as serialID, bsr.name as serialName, pb.name as batchName, bsr.exptodate as warrentyExpireyDate from batchserial bsr "
//                    + " inner join productbatch pb on pb.id = bsr.batch "
//                    + " inner join assetdetail asd on asd.batch = bsr.batch "
//                    + " inner join assetdetailsinvdetailmapping asdInvdMap on asdInvdMap.assetdetails = asd.id "
//                    + " inner join dodetails dod on dod.id = asdInvdMap.invoicedetailid "
//                    + " inner join deliveryorder do on do.id = dod.deliveryorder "
//                    + " inner join docontractmapping doContractMap on doContractMap.deliveryorder = do.id "
//                    + " inner join product p on p.id = dod.product "
//                    + " where do.company = ? and doContractMap.contract = ? and do.id = ? and p.id = ? ";

              String mysqlQuery = "select bsr.id as serialID, bsr.serialname as serialName, pb.batchname as batchName, sdm.exptodate as warrentyExpireyDate, bsr.exptodate as vendorWarrentyDate from newbatchserial bsr "
                    + " left join newproductbatch pb on pb.id = bsr.batch "
                    + " inner join serialdocumentmapping sdm on sdm.serialid=bsr.id "
                    + " INNER JOIN  dodetails dod on sdm.documentid=dod.id "
                    + " inner join deliveryorder do on do.id = dod.deliveryorder "
                    + " inner join docontractmapping doContractMap on doContractMap.deliveryorder = do.id "
                    + " inner join product p on p.id = dod.product"
                    + " where do.company = ? and doContractMap.contract = ? and do.id = ? and p.id = ? ";
             
            returnList = executeSQLQuery( mysqlQuery, params.toArray());
            totalCount = returnList.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl : getContractNormalDOItemDetailsRow " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, totalCount);
    }

    @Override
    public KwlReturnObject getContractReplacementDOItemDetails(HashMap<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        int totalCount = 0;
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String contractid = (String) requestParams.get("contractid");
            ArrayList params = new ArrayList();

            params.add(companyid);
            params.add(contractid);

            Contract contract = (Contract) get(Contract.class, contractid);


            String mysqlQuery = " select prd.productreplacement, dod.id as dodid from dodetails dod "
                    + " inner join deliveryorder do on do.id = dod.deliveryorder "
                    + " inner join docontractmapping doContractMap on doContractMap.deliveryorder = do.id "
                    + " inner join product p on p.id = dod.product "
                    + " inner join sodetails sod on sod.id = dod.sodetails "
                    + " inner join salesorder so on so.id = sod.salesorder "
                    + " inner join productreplacementdetail prd on prd.id = sod.productreplacementdetail "
                    + " where so.isreplacementso = 1 and dod.company = ? and doContractMap.contract = ? ";


            String unionQuery = "SELECT prd.productreplacement, dod.id as dodid from dodetails dod "
                    + "INNER JOIN invoicedetails invd ON dod.cidetails = invd.id "
                    + "INNER JOIN sodetails sod ON sod.id=invd.salesorderdetail "
                    + "INNER JOIN salesorder so ON so.id=sod.salesorder "
                    + "INNER JOIN productreplacementdetail prd on prd.id = sod.productreplacementdetail "
                    + "WHERE so.isreplacementso=1 AND so.company=? AND so.contract=?";

            if (contract.isNormalContract()) {
                mysqlQuery = mysqlQuery + " UNION " + unionQuery;
                params.add(companyid);
                params.add(contractid);
            }

            returnList = executeSQLQuery( mysqlQuery, params.toArray());
            totalCount = returnList.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl : getContractReplacementItemDetails " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, totalCount);
    }

    @Override
    public KwlReturnObject getContractReplacementDOItemDetailsRow(HashMap<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        int totalCount = 0;
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String contractid = (String) requestParams.get("contractid");
            String pid = (String) requestParams.get("pid");
            String productReplacementID = (String) requestParams.get("productReplacementID");
            ArrayList params = new ArrayList();

            params.add(companyid);
            params.add(contractid);
            params.add(productReplacementID);
//            params.add(pid);

            String mysqlQuery = " select bsr.id as serialID, bsr.serialname as serialName, pb.batchname as batchName, bsr.exptodate as warrentyExpireyDate from replacementproductbatchdetailsmapping rpbdetailMap "
                    + " inner join  newbatchserial bsr on bsr.id =  rpbdetailMap.batchserial "
                    + " left join newproductbatch pb on pb.id = bsr.batch "
                    + " inner join productreplacement pr on pr.id = rpbdetailMap.productreplacement "
                    + " where rpbdetailMap.company = ? and pr.contract = ? and pr.id = ? ";

            returnList = executeSQLQuery( mysqlQuery, params.toArray());
            totalCount = returnList.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl : getContractReplacementDOItemDetailsRow " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, totalCount);
    }

    @Override
    public KwlReturnObject getContractSalesReturnDetails(HashMap<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        int totalCount = 0;
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String contractid = (String) requestParams.get("contractid");
            ArrayList params = new ArrayList();

            params.add(companyid);
            params.add(contractid);

            String query = " from SalesReturn where company.companyID = ? and contract.ID = ? ";

            returnList = executeQuery( query, params.toArray());
            totalCount = returnList.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl : getContractSalesReturnDetails " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, totalCount);
    }

    @Override
    public KwlReturnObject getCustomerContractsFromCRMAccountID(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        try {
            String companyid = (String) requestParams.get("companyid");
            params.add(companyid); 
            String customerID = (String) requestParams.get("crmaccountid");
            String contractid = (String) requestParams.get("contractid");
            String condition = "";
            if (!StringUtil.isNullOrEmpty(customerID)) {  // Get details by Account Id
                params.add(customerID);
                condition += " and customer.crmaccountid = ? ";
            }
            if (!StringUtil.isNullOrEmpty(contractid)) {  // Get details by contractid Id
                params.add(contractid);
                condition += " and ID = ? ";
            }
            String query = " from Contract where company.companyID = ? " + condition;

            list = executeQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl : getContractDetails " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getCustomerContractsAgreementDetails(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String customerid = (String) requestParams.get("customerid");
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(customerid);

            String query = " from Contract where company.companyID = ? and customer.ID = ? ";

            list = executeQuery( query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl : getCustomerContractsAgreementDetails " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    

    @Override
    public KwlReturnObject getCustomerContractCostAgreementDetails(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String customerid = (String) requestParams.get("customerid");
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(customerid);

            String query = " from Contract where company.companyID = ? and customer.ID = ? ";

            list = executeQuery( query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl : getCustomerContractsAgreementDetails " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getContractsOfCompany(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String customerid = (String) requestParams.get("customerid");
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(customerid);

            String query = " from Contract where company.companyID = ? and customer.ID = ? ";

            list = executeQuery( query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl : getContractsOfCompany " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getNextServiceDateOfContract(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String contractid = (String) requestParams.get("contractid");
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(contractid);

            String query = " select sd.servicedate  from contract contr  "
                    + " inner join servicedetails sd on sd.contract = contr.id "
                    + " where contr.company = ? and contr.id = ? and Date(now()) <= Date(sd.servicedate) "
                    + "order by sd.servicedate asc limit 1 ";

            list = executeSQLQuery( query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl : getNextServiceDateOfContract " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getPreviousServiceDateOfContract(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String contractid = (String) requestParams.get("contractid");
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(contractid);

            String query = " select sd.servicedate  from contract contr "
                    + " inner join servicedetails sd on sd.contract = contr.id "
                    + " where contr.company = ? and contr.id = ? and Date(now()) > Date(sd.servicedate) "
                    + "order by sd.servicedate desc limit 1 ";

            list = executeSQLQuery( query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl : getPreviousServiceDateOfContract " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getSalesPersons(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            ArrayList params = new ArrayList();
            params.add(companyid);

            String query = " from MasterItem "
                    + " where masterGroup.ID = 15 and company.companyID = ? ";
            
            if(requestParams.containsKey("user") && requestParams.get("user")!=null) {
                query += " and user.userID = ?";
                params.add((String)requestParams.get("user"));
            }

            list = executeQuery( query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl : getSalesPersons " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getActiveContracts(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String conditionSQL = "";
            String innerSQL = "";
            ArrayList params = new ArrayList();

            String companyid = (String) requestParams.get(Constants.companyKey);
            if (!StringUtil.isNullOrEmpty(companyid)) {
                conditionSQL += " and contr.company = ? ";
                params.add(companyid);
            }

            String salesPersonID = (String) requestParams.get("salesPersonID");
            if (!StringUtil.isNullOrEmpty(salesPersonID)) {
                innerSQL += " inner join masteritem mi on mi.id = so.salesperson ";
                conditionSQL += " and mi.id = ? ";
                params.add(salesPersonID);
            }

            String mysqlQuery = " select contr.id from contract contr "
                    + " inner join salesorder so on so.id = contr.sonumber " + innerSQL
                    + " where cstatus !=3 " + conditionSQL;

            list = executeSQLQuery( mysqlQuery, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl : getActiveContracts " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public int setContractExpiryStatus(String contractIDs, String companyid) throws ServiceException {
        int updateCount = 0;
        try {
            ArrayList params = new ArrayList();

            String conditionSQL = " where company = ? ";
            params.add(companyid);

            if (!StringUtil.isNullOrEmpty(contractIDs)) {
                contractIDs = AccountingManager.getFilterInString(contractIDs);
                conditionSQL += " and id in " + contractIDs + " ";
            }

            String mysqlUpdateQuery = " update contract set cstatus = 3 " + conditionSQL;

            updateCount = executeSQLUpdate( mysqlUpdateQuery, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl : setContractExpiryStatus " + ex.getMessage(), ex);
        }
        return updateCount;
    }

    @Override
    public boolean checkQuotationLinkedInSo(String companyid, String qid) throws ServiceException {
        try {
            ArrayList params = new ArrayList();
            List list = new ArrayList();
            params.add(companyid);
            params.add(qid);
//            String mysqlquery = "select * from sodetails where company=? and quotationdetail in (select id from quotationdetails where quotation = ?)";
            String mysqlquery = "select sod.* from sodetails sod inner join quotationdetails qd on sod.quotationdetail=qd.id where sod.company=? and qd.quotation = ?";
            list = executeSQLQuery( mysqlquery, params.toArray());
            if (list.size() > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl : checkQuotationLinkedInSo " + ex.getMessage(), ex);
        }
    }

    public boolean checkSoLinkedInContract(String companyid, String soid) throws ServiceException {
        try {
            ArrayList params = new ArrayList();
            List list = new ArrayList();
            params.add(companyid);
            params.add(soid);
            String mysqlquery = "select * from contract where company=? and sonumber=?";
            list = executeSQLQuery( mysqlquery, params.toArray());
            if (list.size() > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl : checkSoLinkedInContract " + ex.getMessage(), ex);
        }
    }

    @Override
    public KwlReturnObject getCQ_Product(String productid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from QuotationDetail qd where qd.product.ID=? and qd.company.companyID=? "; /*
         * and qd.quotation.deleted=false "
         */
        list = executeQuery( q, new Object[]{productid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public void openMaintenance(String soid, String companyid, String maintenaceId) throws ServiceException {
        try {
            if (!StringUtil.isNullOrEmpty(maintenaceId)) {   // if selected sales order has maintenance 
                ArrayList params = new ArrayList();
                params.add(maintenaceId);
                params.add(companyid);
                String query = "update maintenance set isclosed = 0 where id = ? and company = ?";  // update status of that maintenance from close to open 
                int numRows = executeSQLUpdate( query, params.toArray());
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl : unCloseMaintenance " + ex.getMessage(), ex);
        }
    }

    public KwlReturnObject getCutomer(String name, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = " from Customer where name = ? and company.companyID = ? ";

        list = executeQuery( q, new Object[]{name, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getTerm(String term, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = " from Term where company.companyID=? and  termname = ? ";

        list = executeQuery( q, new Object[]{companyid, term});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getTermMappedToCrmQuotation(HashMap<String, Object> params) throws ServiceException {
        List list = new ArrayList();
        List paramValues = new ArrayList();
        String q = " from Term";

        paramValues.add(params.get("companyid"));
        q += "  where company.companyID = ? ";

        if (params.containsKey("crmtermid")) {
            paramValues.add(params.get("crmtermid"));
            q += " and crmtermid = ? ";
        }

        list = executeQuery(q, paramValues.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getProductReplacement(String productreplacement, String product) throws ServiceException {
        List list = new ArrayList();
        String q = "from ProductReplacementDetail where productReplacement.id=? and product.ID=?";

        list = executeQuery( q, new Object[]{productreplacement, product});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getPendingConsignmentRequests(String companyid,String prodidStrings) throws ServiceException {

        KwlReturnObject retObj = new KwlReturnObject(false, null, null, null, 0);
        List<String> dataList = new ArrayList();
        List params = new ArrayList();
        List<SalesOrder> soList = new ArrayList();
        String condnsql="";
        if (!StringUtil.isNullOrEmpty(prodidStrings)) {
            prodidStrings = prodidStrings.substring(0, prodidStrings.length() - 1);
            condnsql+= " and sod.product in (" + prodidStrings + ") ";
        }

        String qry = " SELECT DISTINCT so.id FROM salesorder so INNER JOIN  sodetails sod ON sod.salesorder=so.id WHERE so.company= ? "
                + " AND so.isconsignment='T' AND so.lockquantityflag=1  AND sod.lockquantitydue > 0 "+condnsql
                + " AND so.fromdate is NOT NULL AND so.fromdate >= ? "
                + " ORDER BY sod.product,so.fromdate ";

        params.add(companyid);
        params.add(new Date());

        try {
            dataList = executeSQLQuery( qry, params.toArray());

            if (!dataList.isEmpty() && dataList != null) {
                for (int i = 0; i < dataList.size(); i++) {
                    String salesorderid = dataList.get(i);
                    if (!StringUtil.isNullOrEmpty(salesorderid)) {
                        SalesOrder so = (SalesOrder) get(SalesOrder.class, salesorderid);
                        if (so != null) {
                            soList.add(so);
                        }
                    }
                }
                retObj = new KwlReturnObject(true, null, null, soList, soList.size());
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.getStackTrace();
            Logger.getLogger(accSalesOrderImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return retObj;
        }

    }
      public KwlReturnObject updatebatchlockQuantity(String productid, String documentid, String companyid) throws ServiceException {
        try {
            String docids = "";
             List params14 = new ArrayList();
            if (!StringUtil.isNullOrEmpty(documentid)) {
                docids += "'" + documentid + "',";
}
            if (!StringUtil.isNullOrEmpty(docids)) {
                docids = docids.substring(0, docids.length() - 1);
            }
            if (!StringUtil.isNullOrEmpty(docids)) {
                String myquery4 = " select batchmapid,id from locationbatchdocumentmapping where documentid in (" + docids + ") and isconsignment='F'";
                List list4 = executeSQLQuery( myquery4, params14.toArray());
                Iterator itr4 = list4.iterator();
                while (itr4.hasNext()) {
                    Object[] objArr = (Object[]) itr4.next();
                    LocationBatchDocumentMapping locationBatchDocumentMapping = (LocationBatchDocumentMapping) get(LocationBatchDocumentMapping.class, (String) objArr[1]);
                    if (locationBatchDocumentMapping != null) {
                        HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                        batchUpdateQtyMap.put("id", locationBatchDocumentMapping.getBatchmapid().getId());
                        batchUpdateQtyMap.put("lockquantity", -(locationBatchDocumentMapping.getQuantity()));
                        updatebatchlockQuantity(batchUpdateQtyMap);
//                        saveBatchAmountDue(batchUpdateQtyMap);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.updatebatchlockQuantity:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }
      public KwlReturnObject updateSerialslockQuantity(String productid, String documentid, String companyid) throws ServiceException {
        try {
            String docids = "";
       
            if (!StringUtil.isNullOrEmpty(documentid)) {
                docids += "'" + documentid + "',";
            }
            if (!StringUtil.isNullOrEmpty(docids)) {
                docids = docids.substring(0, docids.length() - 1);
            }
            if (!StringUtil.isNullOrEmpty(docids)) {
              ArrayList params14 = new ArrayList();
                String myquery4 = " select serialid,id from serialdocumentmapping where documentid in (" + docids + ") ";
                List list4 = executeSQLQuery( myquery4, params14.toArray());
                Iterator itr4 = list4.iterator();
                while (itr4.hasNext()) {
                    Object[] objArr = (Object[]) itr4.next();
                    SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) get(SerialDocumentMapping.class, (String) objArr[1]);
                    if (serialDocumentMapping != null) {
                        HashMap<String, Object> serialUpdateQtyMap = new HashMap<String, Object>();
                        serialUpdateQtyMap.put("id", serialDocumentMapping.getSerialid().getId());
                        serialUpdateQtyMap.put("lockquantity","0");
                        updateSerialslockQuantity(serialUpdateQtyMap);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.updatebatchlockQuantity:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }
    @Override
       public KwlReturnObject activateDeactivateRecurringSO(String repeateid, boolean isactivate) throws ServiceException {
        RepeatedSalesOrder rp = null;
        try {
            rp = (RepeatedSalesOrder) get(RepeatedSalesOrder.class, repeateid);
            rp.setIsActivate(!isactivate);
        } catch (Exception e) {
            Logger.getLogger(accSalesOrderImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return new KwlReturnObject(true, "Recurring SO has been updated successfully.", null, null, 0);
    }
    @Override
       public KwlReturnObject approveRecurringSO(String repeateid, boolean ispendingapproval) throws ServiceException {
        RepeatedSalesOrder rp = null;
        try {
            rp = (RepeatedSalesOrder) get(RepeatedSalesOrder.class, repeateid);
            rp.setIspendingapproval(ispendingapproval);
        } catch (Exception e) {
            Logger.getLogger(accSalesOrderImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return new KwlReturnObject(true, "Recurring SO has been approved successfully.", null, null, 0);
    }
     public Object getUserObject(String id) throws ServiceException {
        Object obj = null;
        try {
            obj = get(User.class, id);
        } catch (Exception e) {
            Logger.getLogger(accSalesOrderImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return obj;
    }
    public KwlReturnObject getRepeateSONo(Date prevDate) throws ServiceException {
        String query = "FROM SalesOrder WHERE repeateSO is not null and (repeateSO.isActivate=true and repeateSO.ispendingapproval=false) and ((repeateSO.prevDate = ? and repeateSO.nextDate <= repeateSO.expireDate) ";
        //getting repeate invoices for which prev date will be updated to today's date after repeated invoice creation 
        query += " or (repeateSO.nextDate=? and repeateSO.intervalUnit=1 and repeateSO.intervalType='day')) ";
        List list = executeQuery( query, new Object[]{prevDate, prevDate});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject updateLeaseOrder(String contractid, String companyid) throws ServiceException {
        try {
            String delQuery = "update SalesOrder set contract=NULL where contract.ID=? and company.companyID=?";
            int numRows = executeUpdate( delQuery, new Object[]{contractid, companyid});
            return new KwlReturnObject(true, "salesorder has been Updated successfully.", null, null, numRows);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot delete Contract.", ex);//+ex.getMessage(), ex);as it is used in Customer Invoice already
        }
    }

    public KwlReturnObject deleteContracts(String coid, String companyid) throws ServiceException {
        try {
            String delQuery1 = "", delQuery2 = "", delQuery3 = "", delQuery4 = "", delQuery5 = "", delQuery6;
            int numtotal = 0;
            if (!StringUtil.isNullOrEmpty(coid) && !StringUtil.isNullOrEmpty(companyid)) {
                ArrayList params5 = new ArrayList();
                params5.add(coid);
                params5.add(companyid);
                delQuery5 = " delete from contractdetails where contract in (select id from contract where id=? and company = ?) ";
                int numRows5 = executeSQLUpdate( delQuery5, params5.toArray());

                String deldate = "delete from contractdates where contract = ?";
                ArrayList datelist = new ArrayList();
                datelist.add(coid);
                int numRows10 = executeSQLUpdate( deldate, datelist.toArray());

                ArrayList params6 = new ArrayList();
                params6.add(companyid);
                params6.add(coid);
                delQuery6 = "delete from contract where company = ? and id=?";
                int numRows6 = executeSQLUpdate( delQuery6, params6.toArray());
               
                ArrayList params2 = new ArrayList();
                params2.add(coid);
                delQuery2 = "delete  from contractcustomdata where contractid=?";
                int numRows2 = executeSQLUpdate( delQuery2, params2.toArray());
            }

            return new KwlReturnObject(true, "Contract has been deleted successfully.", null, null, numtotal);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot delete Contract as its referance child field is not deleted.", ex);//+ex.getMessage(), ex);
        }
    }

    @Override
     public synchronized String updateSOEntryNumberForNewSO(Map<String, Object> seqNumberMap) {
        String documnetNumber="";
        try {
             documnetNumber = seqNumberMap.containsKey(Constants.AUTO_ENTRYNUMBER) ? (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER) : "";
            int seqNumber = 0;
            if(seqNumberMap.containsKey(Constants.SEQNUMBER) && !StringUtil.isNullObject(seqNumberMap.get(Constants.SEQNUMBER)) && !StringUtil.isNullOrEmpty(seqNumberMap.get(Constants.SEQNUMBER).toString())){
               seqNumber= Integer.parseInt(seqNumberMap.get(Constants.SEQNUMBER).toString());
            }
            boolean isDraft = false;
            if(seqNumberMap.containsKey(Constants.isDraft) && seqNumberMap.get(Constants.isDraft)!=null){
                isDraft = (Boolean)seqNumberMap.get(Constants.isDraft);  
                if(isDraft){
                    documnetNumber = "";    //SDP-13487 : Set SO no.empty
                    seqNumber = 0;
                }
            }
            String datePrefix = seqNumberMap.containsKey(Constants.DATEPREFIX) ? (String)seqNumberMap.get(Constants.DATEPREFIX) : "";
            String dateafterPrefix = seqNumberMap.containsKey(Constants.DATEAFTERPREFIX) ? (String)seqNumberMap.get(Constants.DATEAFTERPREFIX) : "";
            String dateSuffix = seqNumberMap.containsKey(Constants.DATESUFFIX) ? (String)seqNumberMap.get(Constants.DATESUFFIX) : "";
            String sequenceFormatID = seqNumberMap.containsKey(Constants.SEQUENCEFORMATID) ? (String)seqNumberMap.get(Constants.SEQUENCEFORMATID) : "";
            String documentID = seqNumberMap.containsKey(Constants.DOCUMENTID) ? (String)seqNumberMap.get(Constants.DOCUMENTID) : "";
            String companyID = seqNumberMap.containsKey(Constants.companyKey) ? (String)seqNumberMap.get(Constants.companyKey) : "";
            String query = "update SalesOrder set salesOrderNumber = ?,seqnumber=?,datePreffixValue=?, dateAfterPreffixValue=?, dateSuffixValue=?,seqformat.ID=? where ID=? and company.companyID=?";
            int numRows = executeUpdate( query, new Object[]{documnetNumber,seqNumber,datePrefix,dateafterPrefix,dateSuffix,sequenceFormatID, documentID, companyID});
        } catch (Exception e) {
            System.out.println(e);
        }
        return documnetNumber;
    }

    @Override
     public synchronized String updateCQEntryNumberForNewCQ(Map<String, Object> seqNumberMap) {
        String documnetNumber="";
        try { 
            documnetNumber = seqNumberMap.containsKey(Constants.AUTO_ENTRYNUMBER) ? (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER) : "";
            int seqNumber = 0;
            if(seqNumberMap.containsKey(Constants.SEQNUMBER) && !StringUtil.isNullObject(seqNumberMap.get(Constants.SEQNUMBER)) && !StringUtil.isNullOrEmpty(seqNumberMap.get(Constants.SEQNUMBER).toString())){
               seqNumber= Integer.parseInt(seqNumberMap.get(Constants.SEQNUMBER).toString());
            }
            boolean isDraft = false;
            if(seqNumberMap.containsKey(Constants.isDraft) && seqNumberMap.get(Constants.isDraft)!=null){
                isDraft = (Boolean)seqNumberMap.get(Constants.isDraft);  
                if(isDraft){
                    documnetNumber = "";    //SDP-13487 : Set CQ No.empty
                    seqNumber = 0;
                }
            }
            String datePrefix = seqNumberMap.containsKey(Constants.DATEPREFIX) ? (String)seqNumberMap.get(Constants.DATEPREFIX) : "";
            String dateafterPrefix = seqNumberMap.containsKey(Constants.DATEAFTERPREFIX) ? (String)seqNumberMap.get(Constants.DATEAFTERPREFIX) : "";
            String dateSuffix = seqNumberMap.containsKey(Constants.DATESUFFIX) ? (String)seqNumberMap.get(Constants.DATESUFFIX) : "";
            String sequenceFormatID = seqNumberMap.containsKey(Constants.SEQUENCEFORMATID) ? (String)seqNumberMap.get(Constants.SEQUENCEFORMATID) : "";
            String documentID = seqNumberMap.containsKey(Constants.DOCUMENTID) ? (String)seqNumberMap.get(Constants.DOCUMENTID) : "";
            String companyID = seqNumberMap.containsKey(Constants.companyKey) ? (String)seqNumberMap.get(Constants.companyKey) : "";
            String query = "update Quotation set quotationNumber = ?,seqnumber=?,datePreffixValue=?, dateAfterPreffixValue=?, dateSuffixValue=?,seqformat.ID=? where ID=? and company.companyID=?";
            int numRows = executeUpdate( query, new Object[]{documnetNumber,seqNumber,datePrefix,dateafterPrefix,dateSuffix,sequenceFormatID, documentID, companyID});
        } catch (Exception e) {
            System.out.println(e);
        }
        return documnetNumber;
    }

    @Override
     public synchronized String updatePREntryNumberForNewPR(Map<String, Object> seqNumberMap) {
         String documnetNumber="";
        try {
            documnetNumber = seqNumberMap.containsKey(Constants.AUTO_ENTRYNUMBER) ? (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER) : "";
            int seqNumber = 0;
            if(seqNumberMap.containsKey(Constants.SEQNUMBER) && !StringUtil.isNullObject(seqNumberMap.get(Constants.SEQNUMBER)) && !StringUtil.isNullOrEmpty(seqNumberMap.get(Constants.SEQNUMBER).toString())){
               seqNumber= Integer.parseInt(seqNumberMap.get(Constants.SEQNUMBER).toString());
            }
            String datePrefix = seqNumberMap.containsKey(Constants.DATEPREFIX) ? (String)seqNumberMap.get(Constants.DATEPREFIX) : "";
            String dateafterPrefix = seqNumberMap.containsKey(Constants.DATEAFTERPREFIX) ? (String)seqNumberMap.get(Constants.DATEAFTERPREFIX) : "";
            String dateSuffix = seqNumberMap.containsKey(Constants.DATESUFFIX) ? (String)seqNumberMap.get(Constants.DATESUFFIX) : "";
            String sequenceFormatID = seqNumberMap.containsKey(Constants.SEQUENCEFORMATID) ? (String)seqNumberMap.get(Constants.SEQUENCEFORMATID) : "";
            String documentID = seqNumberMap.containsKey(Constants.DOCUMENTID) ? (String)seqNumberMap.get(Constants.DOCUMENTID) : "";
            String companyID = seqNumberMap.containsKey(Constants.companyKey) ? (String)seqNumberMap.get(Constants.companyKey) : "";
            String query = "update PurchaseReturn set purchaseReturnNumber = ?,seqnumber=?,datePreffixValue=?, dateAfterPreffixValue=?, dateSuffixValue=?,seqformat.ID=? where ID=? and company.companyID=?";
            int numRows = executeUpdate( query, new Object[]{documnetNumber,seqNumber,datePrefix,dateafterPrefix,dateSuffix,sequenceFormatID, documentID, companyID});
        } catch (Exception e) {
            System.out.println(e);
        }
        return documnetNumber;
    }

    @Override
     public synchronized String updateDNEntryNumberForNewPR(Map<String, Object> seqNumberMap) {
         String documnetNumber="";
        try {
            documnetNumber = seqNumberMap.containsKey(Constants.AUTO_ENTRYNUMBER) ? (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER) : "";
            int seqNumber = 0;
            if (seqNumberMap.containsKey(Constants.SEQNUMBER) && !StringUtil.isNullObject(seqNumberMap.get(Constants.SEQNUMBER)) && !StringUtil.isNullOrEmpty(seqNumberMap.get(Constants.SEQNUMBER).toString())) {
                seqNumber = Integer.parseInt(seqNumberMap.get(Constants.SEQNUMBER).toString());
            }
            String datePrefix = seqNumberMap.containsKey(Constants.DATEPREFIX) ? (String) seqNumberMap.get(Constants.DATEPREFIX) : "";
            String dateafterPrefix = seqNumberMap.containsKey(Constants.DATEAFTERPREFIX) ? (String)seqNumberMap.get(Constants.DATEAFTERPREFIX) : "";
            String dateSuffix = seqNumberMap.containsKey(Constants.DATESUFFIX) ? (String) seqNumberMap.get(Constants.DATESUFFIX) : "";
            String sequenceFormatID = seqNumberMap.containsKey(Constants.SEQUENCEFORMATID) ? (String) seqNumberMap.get(Constants.SEQUENCEFORMATID) : "";
            String documentID = seqNumberMap.containsKey(Constants.DOCUMENTID) ? (String) seqNumberMap.get(Constants.DOCUMENTID) : "";
            String companyID = seqNumberMap.containsKey(Constants.companyKey) ? (String) seqNumberMap.get(Constants.companyKey) : "";
            String query = "update DebitNote set debitNoteNumber = ?,seqnumber=?,datePreffixValue=?, dateAfterPreffixValue=?, dateSuffixValue=?,seqformat.ID=? where ID=? and company.companyID=?";
            int numRows = executeUpdate( query, new Object[]{documnetNumber,seqNumber,datePrefix,dateafterPrefix,dateSuffix,sequenceFormatID, documentID, companyID});
        } catch (Exception e) {
            System.out.println(e);
        }
        return documnetNumber;
    }

    @Override
    public synchronized String updateSREntryNumberForNewSR(Map<String, Object> seqNumberMap) {
        String documnetNumber = "";
        try {
            documnetNumber = seqNumberMap.containsKey(Constants.AUTO_ENTRYNUMBER) ? (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER) : "";
            int seqNumber = 0;
            if (seqNumberMap.containsKey(Constants.SEQNUMBER) && !StringUtil.isNullObject(seqNumberMap.get(Constants.SEQNUMBER)) && !StringUtil.isNullOrEmpty(seqNumberMap.get(Constants.SEQNUMBER).toString())) {
                seqNumber = Integer.parseInt(seqNumberMap.get(Constants.SEQNUMBER).toString());
            }
            String datePrefix = seqNumberMap.containsKey(Constants.DATEPREFIX) ? (String) seqNumberMap.get(Constants.DATEPREFIX) : "";
            String dateafterPrefix = seqNumberMap.containsKey(Constants.DATEAFTERPREFIX) ? (String)seqNumberMap.get(Constants.DATEAFTERPREFIX) : "";
            String dateSuffix = seqNumberMap.containsKey(Constants.DATESUFFIX) ? (String) seqNumberMap.get(Constants.DATESUFFIX) : "";
            String sequenceFormatID = seqNumberMap.containsKey(Constants.SEQUENCEFORMATID) ? (String) seqNumberMap.get(Constants.SEQUENCEFORMATID) : "";
            String documentID = seqNumberMap.containsKey(Constants.DOCUMENTID) ? (String) seqNumberMap.get(Constants.DOCUMENTID) : "";
            String companyID = seqNumberMap.containsKey(Constants.companyKey) ? (String) seqNumberMap.get(Constants.companyKey) : "";
            String query = "update SalesReturn set salesReturnNumber = ?,seqnumber=?,datePreffixValue=?, dateAfterPreffixValue=?, dateSuffixValue=?,seqformat.ID=? where ID=? and company.companyID=?";
            int numRows = executeUpdate(query, new Object[]{documnetNumber, seqNumber,datePrefix, dateafterPrefix, dateSuffix, sequenceFormatID, documentID, companyID});
        } catch (Exception e) {
            System.out.println(e);
        }
        return documnetNumber;
    }
    
    @Override
    public void updateConsignmentEntryNumber(String billid,String billno, String companyid){
        try {
//            String query = "update in_consignment set transactionno =? where company=? and documentid in(select id from srdetails where salesreturn=?)";
            String query = "update in_consignment join srdetails srd on srd.id=in_consignment.documentid set transactionno =?  where in_consignment.company=? and srd.salesreturn=?";
            int numRows = executeSQLUpdate( query, new Object[]{billno,companyid,billid});
            query = "UPDATE  in_stockmovement  SET transactionno=?  WHERE  transaction_module = 9 AND company = ? AND modulerefid = ?";
            numRows = executeSQLUpdate( query, new Object[]{billno,companyid,billid});
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
     public synchronized String updateCNEntryNumberForNewSR(Map<String, Object> seqNumberMap) {
        String documnetNumber = "";
        try {
            documnetNumber = seqNumberMap.containsKey(Constants.AUTO_ENTRYNUMBER) ? (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER) : "";
            int seqNumber = 0;
            if (seqNumberMap.containsKey(Constants.SEQNUMBER) && !StringUtil.isNullObject(seqNumberMap.get(Constants.SEQNUMBER)) && !StringUtil.isNullOrEmpty(seqNumberMap.get(Constants.SEQNUMBER).toString())) {
                seqNumber = Integer.parseInt(seqNumberMap.get(Constants.SEQNUMBER).toString());
            }
            String datePrefix = seqNumberMap.containsKey(Constants.DATEPREFIX) ? (String) seqNumberMap.get(Constants.DATEPREFIX) : "";
            String dateafterPrefix = seqNumberMap.containsKey(Constants.DATEAFTERPREFIX) ? (String)seqNumberMap.get(Constants.DATEAFTERPREFIX) : "";
            String dateSuffix = seqNumberMap.containsKey(Constants.DATESUFFIX) ? (String) seqNumberMap.get(Constants.DATESUFFIX) : "";
            String sequenceFormatID = seqNumberMap.containsKey(Constants.SEQUENCEFORMATID) ? (String) seqNumberMap.get(Constants.SEQUENCEFORMATID) : "";
            String documentID = seqNumberMap.containsKey(Constants.DOCUMENTID) ? (String) seqNumberMap.get(Constants.DOCUMENTID) : "";
            String companyID = seqNumberMap.containsKey(Constants.companyKey) ? (String) seqNumberMap.get(Constants.companyKey) : "";
            String query = "update CreditNote set creditNoteNumber = ?,seqnumber=?,datePreffixValue=?, dateAfterPreffixValue=?, dateSuffixValue=?,seqformat.ID=? where ID=? and company.companyID=?";
            int numRows = executeUpdate( query, new Object[]{documnetNumber,seqNumber,datePrefix,dateafterPrefix,dateSuffix,sequenceFormatID, documentID, companyID});
        } catch (Exception e) {
            System.out.println(e);
        }
        return documnetNumber;
    }
            
     
     public KwlReturnObject getSalesByCustomer(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String companyid = (String) request.get(Constants.companyKey);
            String start = (String) request.get(Constants.start);
            String limit = (String) request.get(Constants.limit);
            DateFormat df = (DateFormat) request.get(Constants.df);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) get(CompanyAccountPreferences.class, companyid);
            Calendar startFinYearCal = Calendar.getInstance();
            Calendar endFinYearCal = Calendar.getInstance();
            startFinYearCal.setTime(pref.getFinancialYearFrom());
            endFinYearCal.setTime(pref.getFinancialYearFrom());
            endFinYearCal.add(Calendar.YEAR, 1);
            String customerid = (String) request.get(Constants.customerid);
            String productid = (String) request.get(Constants.productid);

            String productCategoryid = "";
            if (request.containsKey(Constants.productCategoryid) && request.get(Constants.productCategoryid) != null) {
                productCategoryid = (String) request.get(Constants.productCategoryid);
            }

            String prodFilterCustid = (String) request.get(Constants.prodfiltercustid);
            String salesPersonid = (String) request.get(Constants.salesPersonid);
            if (customerid == null) {
                customerid = (String) request.get(Constants.accid);
            }

            String customerCategoryid = "";
            if (request.containsKey(Constants.customerCategoryid) && request.get(Constants.customerCategoryid) != null) {
                customerCategoryid = (String) request.get(Constants.customerCategoryid);
            }
            String ss = (String) request.get(Constants.ss);
            String cashAccount = pref.getCashAccount().getID();
            boolean creditonly = false;
            creditonly = Boolean.parseBoolean((String) request.get(Constants.creditonly));

            ArrayList params = new ArrayList();

            String conditionSQL = "";
            String conditionSQLForNotLease = "";

            params.add(companyid);

            if ( request.get("userid") != null) {//request.containsKey("salesPersonFilterFlag") && (Boolean) request.get("salesPersonFilterFlag")
                String userId = (String) request.get("userid");
                if (!StringUtil.isNullOrEmpty(userId)) {
                    DataFilteringModule dataFilteringModule = null;
                    MasterItem masterItem = null;
                    List<DataFilteringModule> dataFilteringModuleList = new ArrayList<DataFilteringModule>();
                    List<MasterItem> masterItems = new ArrayList<MasterItem>();

                    dataFilteringModuleList = find("from DataFilteringModule where user.userID='" + userId + "' and company.companyID='" + companyid + "'");
                    masterItems = find("from MasterItem where user='" + userId + "' and company.companyID='" + companyid + "' and masterGroup.ID='" + 15 + "'");
                    if (!dataFilteringModuleList.isEmpty()) {
                        dataFilteringModule = dataFilteringModuleList.get(0);
                    }
                    if (!masterItems.isEmpty()) {
                        masterItem = masterItems.get(0);
                    }
                   if ((dataFilteringModule != null && !dataFilteringModule.isSalesOrder()) || (dataFilteringModule != null && !dataFilteringModule.isSalesOrder() && masterItem != null)) {
                        conditionSQL += " and ( ";
                    }
                    if (dataFilteringModule != null && !dataFilteringModule.isSalesOrder()) {
                        params.add(dataFilteringModule.getUser().getUserID());
                        conditionSQL += "salesorder.createdby=? ";
                    }

                    if (dataFilteringModule != null && !dataFilteringModule.isSalesOrder() && masterItem != null) {
                        params.add(masterItem.getID());
                        conditionSQL += " or salesorder.salesperson=? ";
                        }

                    if ((dataFilteringModule != null && !dataFilteringModule.isSalesOrder()) || (dataFilteringModule != null && !dataFilteringModule.isSalesOrder() && masterItem != null)) {
                        conditionSQL += " ) ";
                    }
                    }
                    }
            /*
             For Block Quantity Report
             */
            boolean isBlockQtyReport = false;
            if (request.get("isBlockQtyReport") != null) {
                isBlockQtyReport = Boolean.parseBoolean(request.get("isBlockQtyReport").toString());
            }
            /**
             * Get the product ids of assembled product that have so with block
             * quantity that contains the current product as sub product.
             */
            List productIdList = null;
            String asseblyProductId = null;
            if (isBlockQtyReport) {
                String sqlQuery = "select distinct product from lockassemblyquantitymapping where subproducts=? ";
                productIdList = executeSQLQuery(sqlQuery, new Object[]{productid});
                asseblyProductId = org.springframework.util.StringUtils.collectionToDelimitedString(productIdList, ",");
            }
            
            if (!StringUtil.isNullOrEmpty(productid) && !StringUtil.equal(productid, "-1") && !StringUtil.equal(productid, "All")) {
                productid+=(isBlockQtyReport && !StringUtil.isNullOrEmpty(asseblyProductId))?","+asseblyProductId:" ";
                productid = AccountingManager.getFilterInString(productid);
                conditionSQL += " and sodetails.product in " + productid + "  ";
            }

            if (!StringUtil.isNullOrEmpty(productCategoryid) && !StringUtil.equal(productCategoryid, "-1")) {
                params.add(productCategoryid);
                conditionSQL += " and sodetails.product in (select productid from productcategorymapping where productcategory = ?)  ";
            }

            if (!StringUtil.isNullOrEmpty(prodFilterCustid) && !StringUtil.equal(prodFilterCustid, "-1") && !StringUtil.equal(prodFilterCustid, "All")) {
                prodFilterCustid = AccountingManager.getFilterInString(prodFilterCustid);
                conditionSQL += " and salesorder.customer in " + prodFilterCustid + "  ";
            }

            if (!StringUtil.isNullOrEmpty(customerCategoryid) && !StringUtil.equal(customerCategoryid, "-1") && !StringUtil.equal(customerCategoryid, "All")) {
                params.add(customerCategoryid);
                conditionSQL += " and salesorder.customer in (select customerid from customercategorymapping where customercategory = ?)  ";
            }

            if (!StringUtil.isNullOrEmpty(salesPersonid) && !StringUtil.equal(salesPersonid, "-1") && !StringUtil.equal(salesPersonid, "All")) {
                salesPersonid = AccountingManager.getFilterInString(salesPersonid);
                conditionSQL += " and salesorder.salesperson in " + salesPersonid + "  ";
            } else if (StringUtil.equal(salesPersonid, "All")) {
                conditionSQL += " and salesorder.salesperson is not null ";
            }
            
            int salesOrderTypeId = -1;
            if (request.containsKey("salesOrderTypeId") && request.get("salesOrderTypeId") != null) {
                /**
                 * leaseOrMaintenanceSO == 0 means it is a normal SO
                 * leaseOrMaintenanceSO == 1 means this is a Lease SO,
                 * leaseOrMaintenanceSO == 2 means this is an maintenance SO,
                 * leaseOrMaintenanceSO == 3 means it is a consignment SO.
                 */
                salesOrderTypeId = Integer.parseInt(request.get("salesOrderTypeId").toString()); // Sales Order Report By Block Quantity (ERP-30445)
            }
            if (isBlockQtyReport) {
                params.add(1);
                if (salesOrderTypeId == Constants.CONSIGNMENT_SALES_ORDER_TYPE) {
                    conditionSQL += " and salesorder.lockquantityflag = ? ";
                } else {
                    conditionSQL += " and salesorder.lockquantityflag = ? and sodetails.lockquantity>0";
                }
                
            }
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                conditionSQL += " and (salesorder.orderdate >=? and salesorder.orderdate <=?) ";
                params.add(df.parse(startDate));//requires date in long
                params.add(df.parse(endDate));
            }


            if (StringUtil.isNullOrEmpty(ss) == false) {
                for (int i = 0; i < 4; i++) {
                    params.add("%" + ss + "%");
                }
                conditionSQL += " and (salesorder.sonumber like ? or customer.name like ? or product.name like ? or sodetails.description like ?) ";
            }
            //condition for Non Lease sales order records
            if (salesOrderTypeId != -1) {
                conditionSQLForNotLease = " and salesorder.leaseOrMaintenanceSO= " + salesOrderTypeId;
            } else {
                conditionSQLForNotLease = " and salesorder.leaseOrMaintenanceSO=0 ";
            }
            String appendCase = "and";
            String mySearchFilterString = "";
            String joinString = "";
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (request.containsKey("filterConjuctionCriteria") && request.get("filterConjuctionCriteria") != null) {
                if (request.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            String Searchjson = "";
            if (request.containsKey("searchJson") && request.get("searchJson") != null) {
                Searchjson = request.get("searchJson").toString();
                if (!StringUtil.isNullOrEmpty(Searchjson)) {
                    request.put(Constants.Searchjson, Searchjson);
                    request.put(Constants.appendCase, appendCase);
                    request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "salesorderdetailcustomdata");
                    joinString = " left join salesordercustomdata on salesordercustomdata.soID=salesorder.id ";
                    joinString += " left join salesorderdetailcustomdata on salesorderdetailcustomdata.soDetailID=sodetails.salesorderdetailcustomdataref ";
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                }
            }
            String mysqlQuery = "select salesorder.id, 'false' as withoutinventory, salesorder.orderdate , sodetails.id as invid   from salesorder  "
                    + " inner join sodetails on sodetails.salesorder = salesorder.id "
                    + " inner join customer on customer.id = salesorder.customer "
                    + " inner join product on product.id = sodetails.product "+joinString
                    + " where salesorder.company = ? and salesorder.deleteflag='F' and salesorder.approvestatuslevel = 11 and salesorder.isdraft = false " + conditionSQL + conditionSQLForNotLease + mySearchFilterString + " "
                    + "order by salesorder.orderdate desc";




            list = executeSQLQuery( mysqlQuery, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeSQLQueryPaging( mysqlQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }

        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accSalesOrderImpl.getInvoices:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }
     
    public KwlReturnObject getLineLevelDiscountSumOfPartialInvoice(String linkdocid, String companyid) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(linkdocid);
        String query = "SELECT invd.partamount,sod.discount,sod.discountispercent FROM invoicedetails invd INNER JOIN sodetails sod on sod.id=invd.salesorderdetail WHERE invd.salesorderdetail=?";

        List list = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getSODFromQD(String QDid) throws ServiceException {
        String selQuery = "from SalesOrderDetail sod  where sod.quotationDetail.ID = ? and sod.salesOrder.deleted = false";
        List list = executeQuery( selQuery, new Object[]{QDid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getQuotationsForCQScript(HashMap<String, Object> requestParams) throws ServiceException {
        String companyid = (String) requestParams.get("companyid");
        String linkflag = (String) requestParams.get("linkflag");
        String selQuery = "select id from quotation where company=?  and linkflag=? and deleteflag='F'";
        List list = executeSQLQuery( selQuery, new Object[]{companyid,linkflag});
        return new KwlReturnObject(true, "", null, list, list.size());
    }    
    
    public KwlReturnObject getCompanyList() throws ServiceException {
        String selQuery = "select companyid from company where deleteflag=0 order by createdon desc";
        List list = executeSQLQuery( selQuery, new Object[]{});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
     public synchronized String updatePREntryNumberForNA(String prid, String entrynumber) {
        try {
            String query = "update PurchaseReturn set purchaseReturnNumber = ? where ID=?";
            int numRows = executeUpdate( query, new Object[]{entrynumber, prid});
        } catch (Exception e) {
            System.out.println(e);
        }
        return entrynumber;
    }

    @Override
     public synchronized String updateDNEntryNumberForNA(String dnid, String dnnumber) {
        try {
            String query = "update DebitNote set debitNoteNumber = ? where ID=?";
            int numRows = executeUpdate( query, new Object[]{dnnumber, dnid});
        } catch (Exception e) {
            System.out.println(e);
        }
        return dnnumber;
    }

    @Override
     public synchronized String updateCQEntryNumberForNA(String cqid, String entrynumber) {
        try {
            String query = "update Quotation set quotationNumber = ? where ID=?";
            int numRows = executeUpdate( query, new Object[]{entrynumber,cqid});
        } catch (Exception e) {
            System.out.println(e);
        }
        return entrynumber;
    }

    @Override
     public synchronized String updateSREntryNumberForNA(String srid, String entrynumber) {
        try {
            String query = "update SalesReturn set salesReturnNumber = ? where ID=?";
            int numRows = executeUpdate( query, new Object[]{entrynumber,srid});
        } catch (Exception e) {
            System.out.println(e);
        }
        return entrynumber;
    }

    @Override
     public synchronized String updateCNEntryNumberForNA(String cnid, String entrynumber) {
        try {
            String query = "update CreditNote set creditNoteNumber = ? where ID=?";
            int numRows = executeUpdate( query, new Object[]{entrynumber,cnid});
        } catch (Exception e) {
            System.out.println(e);
        }
        return entrynumber;
    }

    @Override
     public synchronized String updateSOEntryNumberForNA(String soid,String entrynumber) {
        try {
            String query = "update SalesOrder set salesOrderNumber = ? where ID=?";
            int numRows = executeUpdate( query, new Object[]{entrynumber,soid});
        } catch (Exception e) {
            System.out.println(e);
        }
        return entrynumber;
    }
    public KwlReturnObject getAllQuotaionsByCompanyid(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(requestParams.get("companyid"));
        String hqlQuery = "from Quotation quo where quo.company.companyID=? ";
        list = executeQuery( hqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
}    
    @Override
    public KwlReturnObject getAllGlobalQuotaionsOfInvoiceTerms(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(requestParams.get("companyid"));
        String sqlQuery = "select q.id as qid,qtm.id as qtmid,q.tax,qtm.termamount,tl.percent,qtm.term  from quotationtermmap qtm  inner join quotation q on q.id=qtm.quotation  inner join taxlist tl on tl.tax=q.tax  where qtm.termamount != 0 and q.tax is not null and q.company=? ";
        list = executeSQLQuery( sqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }    
    
    @Override
    public KwlReturnObject getAllGlobalSalesOrderOfInvoiceTerms(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(requestParams.get("companyid"));
        String sqlQuery = "select o.id as oid,otm.id as otmid,o.tax,otm.termamount,tl.percent,otm.term  from salesordertermmap otm  inner join salesorder o on o.id=otm.salesorder  inner join taxlist tl on tl.tax=o.tax  where otm.termamount != 0 and o.tax is not null and o.company= ? ";
        list = executeSQLQuery( sqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getAllLineLevelQuotaionsOfInvoiceTerms(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(requestParams.get("companyid"));
        String sqlQuery = "SELECT q.id,q.quotationnumber FROM quotation q INNER JOIN quotationtermmap qtm on qtm.quotation=q.id WHERE q.company=? and q.applytaxtoterms='T' and qtm.termamount != 0 group by q.id";
        list = executeSQLQuery( sqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public boolean updateQuotationAmount(Quotation quotation, JSONObject json) throws ServiceException {
        boolean success = true;
        try {
            String companyid = json.optString("companyid");
            if (quotation != null) {
                if (json.has("quotationamount")) { // quotation amount
                    quotation.setQuotationamount(authHandler.round(json.optDouble("quotationamount", 0.0), companyid));
                }
                if (json.has("quotationamountinbase")) { // quotation amount in base
                    quotation.setQuotationamountinbase(authHandler.round(json.optDouble("quotationamountinbase", 0.0), companyid));
                }
                if (json.has("discountinbase")) { // Discount in base
                    quotation.setDiscountinbase(authHandler.round(json.optDouble("discountinbase", 0.0), companyid));
                }
                if (json.has("totallineleveldiscount")) { //  Total Discount
                    quotation.setTotallineleveldiscount(authHandler.round(json.optDouble("totallineleveldiscount", 0.0), companyid));
                }
                saveOrUpdate(quotation);
            }
        } catch (Exception ex) {
            success = false;
            System.out.println("accSalesOrderImpl.updateQuotation:" + ex.getMessage());
        }
        return success;
    }
    
    @Override
    public KwlReturnObject getAllSalesOrderByCompanyid(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(requestParams.get("companyid"));
        String hqlQuery = "from SalesOrder so where so.company.companyID=? ";
        list = executeQuery( hqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
} 
    @Override
    public boolean updateSalesOrderAmount(SalesOrder so, JSONObject json) throws ServiceException {
        boolean success = true;
        try {
            String companyid = json.optString("companyid");
            if (so != null) {
                if (json.has("totalamount")) { // Total SO amount
                    so.setTotalamount(authHandler.round(json.optDouble("totalamount", 0.0), companyid));
                }
                if (json.has("totalamountinbase")) { // Total amount in base
                    so.setTotalamountinbase(authHandler.round(json.optDouble("totalamountinbase", 0.0), companyid));
                }
                if (json.has("discountinbase")) { // Discount in base
                    so.setDiscountinbase(authHandler.round(json.optDouble("discountinbase", 0.0), companyid));
                }
                if (json.has("totallineleveldiscount")) { //  Total Discount
                    so.setTotallineleveldiscount(authHandler.round(json.optDouble("totallineleveldiscount", 0.0), companyid));
                }
                saveOrUpdate(so);
            }
        } catch (Exception ex) {
            success = false;
            System.out.println("accSalesOrderImpl.updateSalesOrderAmount:" + ex.getMessage());
        }
        return success;
    }
    
    // function to check whether batch is blocked in salesorder or not
    public boolean getSalesorderBatchStatus(String documentid, String companyId) throws ServiceException {
        boolean isbatchlockedinSO = false;
        int result=0;
        ArrayList params = new ArrayList();
        params.add(documentid);
        String query = "select id from locationbatchdocumentmapping where documentid=?";
        List list = executeSQLQuery( query, params.toArray());
         result = list.size();
         if(result>0){
             isbatchlockedinSO=true;
}    
        return isbatchlockedinSO;
    }
// function to check whether batch is blocked in salesorder or not
    public boolean getSalesorderSerialStatus(String documentid, String companyId) throws ServiceException {
        boolean isSeriallockedinSO = false;
        int result=0;
        ArrayList params = new ArrayList();
        params.add(documentid);
        String query = "select id from serialdocumentmapping where documentid=?";
        List list = executeSQLQuery( query, params.toArray());
         result = list.size();
         if(result>0){
             isSeriallockedinSO=true;
         }
        return isSeriallockedinSO;
    }

    @Override
    public KwlReturnObject saveSalesOrderLinking(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String soid = (String) hm.get("docid");
            SalesOrderLinking solinking = new SalesOrderLinking();
            if (hm.containsKey("docid")) {
                SalesOrder salesorder = (SalesOrder) get(SalesOrder.class, soid);
                solinking.setDocID(salesorder);
            }
            if (hm.containsKey("moduleid")) {
                solinking.setModuleID((Integer) hm.get("moduleid"));
            }
            if (hm.containsKey("linkeddocid")) {
                solinking.setLinkedDocID((String) hm.get("linkeddocid"));
            }
            if (hm.containsKey("linkeddocno")) {
                solinking.setLinkedDocNo((String) hm.get("linkeddocno"));
            }
            if (hm.containsKey("sourceflag")) {
                solinking.setSourceFlag((Integer) hm.get("sourceflag"));
            }
            saveOrUpdate(solinking);
            list.add(solinking);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.saveVILinking : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject saveQuotationLinking(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        try {
            String cqid = (String) hm.get("docid");
            QuotationLinking quotationlinking = new QuotationLinking();
            if (hm.containsKey("docid")) {
                Quotation quotation = (Quotation) get(Quotation.class, cqid);
                quotationlinking.setDocID(quotation);
}    
            if (hm.containsKey("moduleid")) {
                quotationlinking.setModuleID((Integer) hm.get("moduleid"));
            }
            if (hm.containsKey("linkeddocid")) {
                quotationlinking.setLinkedDocID((String) hm.get("linkeddocid"));
            }
            if (hm.containsKey("linkeddocno")) {
                quotationlinking.setLinkedDocNo((String) hm.get("linkeddocno"));
            }
            if (hm.containsKey("sourceflag")) {
                quotationlinking.setSourceFlag((Integer) hm.get("sourceflag"));
            }
            saveOrUpdate(quotationlinking);
            list.add(quotationlinking);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptImpl.saveVILinking : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject deleteLinkingInformationOfSO(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        int numRows1 = 0, nuRows2 = 0, numRows3=0, numRowsTotal = 0;
        String hqlQuery="";
         List list = null;
        try {
            if (requestParams.containsKey("unlinkflag") && requestParams.get("unlinkflag") != null && Boolean.parseBoolean(requestParams.get("unlinkflag").toString())) {
                int type = -1;
                if (requestParams.containsKey("type") && requestParams.get("type") != null) {
                    type = Integer.parseInt(requestParams.get("type").toString());
                }
                params.add(requestParams.get("linkedTransactionID"));
                params.add(requestParams.get("soid"));
                if (type == 0 || type == 1 || type == 4 || type == 2 || type == 3) {
                    if (type == 0) {
                        hqlQuery = "delete from InvoiceLinking inv where inv.DocID.ID=? and inv.LinkedDocID=?";
                        numRows1 = executeUpdate( hqlQuery, params.toArray());
                    } else if (type == 1) { // SO linked in DO
                        hqlQuery = "delete from DeliveryOrderLinking do where do.DocID.ID=? and do.LinkedDocID=?";
                        numRows1 = executeUpdate( hqlQuery, params.toArray());
                    } else if (type == 4) { // CQ linked in SO
                        hqlQuery = "delete from QuotationLinking cq where cq.DocID.ID=? and cq.LinkedDocID=? ";
                        numRows1 = executeUpdate( hqlQuery, params.toArray());
                    } else if (type == 2 || type == 3) {//PO->SO or SO->PO
                        hqlQuery = "delete from PurchaseOrderLinking po where po.DocID.ID=? and po.LinkedDocID=? ";
                        numRows1 = executeUpdate( hqlQuery, params.toArray());
                    }
                    hqlQuery = "delete from SalesOrderLinking so where so.LinkedDocID=? and so.DocID.ID=?";
                    nuRows2 = executeUpdate( hqlQuery, params.toArray());
                } else if (type == 5) {
                    /* 
                     type=5 to delete advancepayment and So linking 
                     */
                    //SO->Receipt
                    params = new ArrayList();
                    params.add(requestParams.get("soid"));
                    params.add(Constants.Acc_Receive_Payment_ModuleId);
                    List parameters = new ArrayList();
                    parameters.add(requestParams.get("soid"));
                    String selQuery="select linkeddocno from solinking  where moduleid=16 and docid=?";
                    list = executeSQLQuery(selQuery , parameters.toArray());
                    
                    hqlQuery = "delete from SalesOrderLinking so where so.DocID.ID=? and so.ModuleID=? ";
                    numRows1 = executeUpdate(hqlQuery, params.toArray());
                }
            } else {
                params.add(requestParams.get("soid"));

                String delQuery = "delete from QuotationLinking cq where cq.LinkedDocID=?";
                numRows1 = executeUpdate( delQuery, params.toArray());

                delQuery = "delete from PurchaseOrderLinking po where po.LinkedDocID=?";
                numRows3 = executeUpdate( delQuery, params.toArray());

                delQuery = "delete from SalesOrderLinking so where so.DocID.ID=?";
                nuRows2 = executeUpdate( delQuery, params.toArray());

                numRowsTotal = numRows1 + nuRows2 + numRows3;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("", ex);//+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, numRowsTotal);
    }
    public KwlReturnObject getPurchaseOrderDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from PurchaseOrderDetail";
        return buildNExecuteQuery( query, requestParams);
    }
    public KwlReturnObject getSalesOrdersLinkedInInvoice(String invoiceid, String companyid) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            // Type=6 SO->SI
            String selQuery = "select distinct sod.salesOrder,6 from InvoiceDetail invd inner join invd.salesorderdetail sod where invd.invoice.ID=? and invd.company.companyID=? and sod.salesOrder.deleted=false";
            list = executeQuery( selQuery, new Object[]{invoiceid, companyid});
            if (list != null) {
                count = list.size();
            }
        } catch (Exception ex) {
            System.out.println("Exception: getSalesOrderLinkedInInvoice " + ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject getContractsDO(Map<String, Object> params) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            //first checked for the contract SO, then is DO created by linking that SO
            String selQuery = "select distinct docntmap.id from salesorder so inner join docontractmapping docntmap where so.id=docntmap.salesorder and so.deleteflag=false";
            List parameters = new ArrayList();
            String condition = "";
            
            if(params.containsKey("contractid")) {
                parameters.add((String) params.get("contractid"));
                condition += " and so.contract=?";
            }
            
            list = executeSQLQuery(selQuery + condition, parameters.toArray());
            count = list.size();
            if(!list.isEmpty()) {
                return new KwlReturnObject(true, "", null, list, count);
            }
            
            //second checked for the contract SO, then is SI created by linking that SO, and then is created DO by linking that SI in DO
            selQuery = "select distinct dolinking.id from invoicecontractmapping invcntmap inner join dolinking dolinking where invcntmap.invoice=dolinking.linkeddocid and invcntmap.contract=?";
            list = executeSQLQuery(selQuery, parameters.toArray());
            count = list.size();
        } catch (Exception ex) {
            System.out.println("Exception: getContractsDO " + ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getVQLinkedWithCQ(HashMap<String, Object> request) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {
            String companyid = "", quotationid = "";
            if (request.containsKey("quotationid") && request.containsKey("companyid")) {
                companyid = (String) request.get("companyid");
                quotationid = (String) request.get("quotationid");
            }
            if (!StringUtil.isNullOrEmpty(quotationid) && !StringUtil.isNullOrEmpty(companyid)) {
                String hqlQuery = "select DISTINCT vendorquotation.id   from vendorquotation inner join vendorquotationdetails on vendorquotationdetails.vendorquotation = vendorquotation.id inner join quotationdetails on quotationdetails.vendorquotationdetails = vendorquotationdetails.id inner join quotation on quotationdetails.quotation = quotation.id\n"
                        + "where vendorquotation.company=? and quotation.id= ?";
                list = executeSQLQuery( hqlQuery, new Object[]{companyid, quotationid});
                if (list != null && list.size() > 0) {
                    count = list.size();
                }
            }
        } catch (Exception ex) {
            System.out.println("Exception: getVQLinkedWithCQ " + ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject checkQuotationLinkedWithAnotherInvoice(String quotationid) throws ServiceException {
        List list = new ArrayList();
      
        String selQuery = "select count(*) from InvoiceLinking inv  where inv.LinkedDocID = ?";
        list = executeQuery( selQuery, new Object[]{quotationid});
        return new KwlReturnObject(true, "", null, list, list.size());

    }

    @Override
    public KwlReturnObject checkQuotationLinkedWithAnotherSalesOrder(String quotationid) throws ServiceException {
        List list = new ArrayList();
      
        String selQuery = "select count(*) from SalesOrderLinking so  where so.LinkedDocID = ?";
        list = executeQuery( selQuery, new Object[]{quotationid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getLinkedInvoiceWithCQ(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String companyid = request.get("companyid").toString();
            String query = "select invd.ID from InvoiceDetail invd  inner join invd.quotationDetail qod  where invd.company.companyID=?";


            list = executeQuery( query, new Object[]{companyid});
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getLinkedInvoiceWithCQ:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getLinkedSalesOrderWithCQ(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String companyid = request.get("companyid").toString();
            String query = "select sod.ID from SalesOrderDetail sod  inner join sod.quotationDetail qod  where sod.company.companyID=?";

            list = executeQuery( query, new Object[]{companyid});
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getLinkedSalesOrderWithCQ:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    
  @Override
        public int updateToNullRepeatedSOOfSalesOrder(String invoiceid, String repeateid) throws ServiceException {
        String query = "UPDATE salesorder SET repeateso=null WHERE id=? AND repeateso=?";
        int numRows = executeSQLUpdate( query, new Object[]{invoiceid, repeateid});
        return numRows;
    }

    @Override
        public int deleteRepeatedSO(String repeateid) throws ServiceException {
        String query = "DELETE FROM repeatedsalesorders WHERE id=?";
        int numRows = executeSQLUpdate( query, new Object[]{repeateid});
        return numRows;
    }

    @Override
    public KwlReturnObject getLinkedInvoiceWithSO(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String companyid = request.get("companyid").toString();
            String query = "select invd.ID from InvoiceDetail invd  inner join invd.salesorderdetail sod  where invd.company.companyID=?";


            list = executeQuery( query, new Object[]{companyid});
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getLinkedInvoiceWithSO:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getLinkedDeliveryOrderWithSO(HashMap<String, Object> request) throws ServiceException {
           List list = null;
        int count = 0;
        try {
            String companyid = request.get("companyid").toString();
            String query = "select dod.ID from DeliveryOrderDetail dod  inner join dod.sodetails sod  where dod.company.companyID=?";


            list = executeQuery( query, new Object[]{companyid});
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getLinkedDeliveryOrderWithSO:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getLinkedDeliveryOrderWithInvoice(HashMap<String, Object> request) throws ServiceException {
                 List list = null;
        int count = 0;
        try {
            String companyid = request.get("companyid").toString();
            String query = "select dod.ID from DeliveryOrderDetail dod  inner join dod.cidetails invd  where dod.company.companyID=?";


            list = executeQuery( query, new Object[]{companyid});
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getLinkedDeliveryOrderWithInvoice:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getLinkedSalesReturnWithInvoice(HashMap<String, Object> request) throws ServiceException {
                 List list = null;
        int count = 0;
        try {
            String companyid = request.get("companyid").toString();
            String query = "select srd.ID from SalesReturnDetail srd  inner join srd.cidetails invd  where srd.company.companyID=?";


            list = executeQuery( query, new Object[]{companyid});
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getLinkedSalesReturnWithInvoice:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    @Override
    public KwlReturnObject getLinkedSalesReturnQuantityWithInvoice(String companyid,String rowId) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String query = "from SalesReturnDetail srd  where srd.company.companyID=? AND srd.cidetails.ID=? ";
            list = executeQuery(query, new Object[]{companyid, rowId});
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getLinkedSalesReturnWithInvoice:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getLinkedInvoicesWithDeliveryOrder(HashMap<String, Object> request) throws ServiceException {
           List list = null;
        int count = 0;
        try {
            String companyid = request.get("companyid").toString();
            String query = "select invd.ID from InvoiceDetail invd  inner join invd.deliveryOrderDetail dod  where invd.company.companyID=?";


            list = executeQuery( query, new Object[]{companyid});
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getLinkedInvoicesWithDeliveryOrder:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getLinkedSalesReturnWithDeliveryOrder(HashMap<String, Object> request) throws ServiceException {
         List list = null;
        int count = 0;
        try {
            String companyid = request.get("companyid").toString();
            String query = "select srd.ID from SalesReturnDetail srd  inner join srd.dodetails dod  where srd.company.companyID=?";


            list = executeQuery( query, new Object[]{companyid});
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getLinkedSalesReturnWithDeliveryOrder:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getCreditNoteLinkedWithInvoice(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String companyid = request.get("companyid").toString();
            String query = "select cnd.ID from CreditNoteDetail cnd  inner join cnd.invoice inv  where cnd.company.companyID=?";


            list = executeQuery( query, new Object[]{companyid});
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getCreditNoteLinkedWithInvoice:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    /**
     * Description : Method is used to Update Entry in linking information table
     * for Purchase Order If any Sales Order linked with Purchase Order
     *
     * @param <request> :-used to get parameters for which you want to update
     * entry in linking information table
     *
     * @return : success message & no of rows updated
     */
    
    @Override
    public KwlReturnObject updateEntryInPurchaseOrderLinkingTable(HashMap<String, Object> request) throws ServiceException {

        String newitemID = UUID.randomUUID().toString();
        String linkeddocid = (String) request.get("linkeddocid");
        String docid = (String) request.get("docid");
        int moduleid = (Integer) request.get("moduleid");
        int sourceFlag = (Integer) request.get("sourceflag");
        String linkeddocno = (String) request.get("linkeddocno");

        String query = "insert into  polinking(id,docid,linkeddocid,linkeddocno,moduleid,sourceflag) values(" + '"' + newitemID + '"' + ',' + '"' + docid + '"' + ',' + '"' + linkeddocid + '"' + ',' + '"' + linkeddocno + '"' + ',' + '"' + moduleid + '"' + ',' + '"' + sourceFlag + '"' + ")";
        int numRows = executeSQLUpdate( query, new String[]{});
        return new KwlReturnObject(true, "Purchase Order Linking has been saved successfully.", null, null, numRows);
}
    
    /**
     * Description : Method is used to get Purchase Order Number
     *
     * @param <purchaseOrderID> :-Purchase Order ID
     *
     *
     * @return :Purchase Order No
     */
    
    @Override
    public KwlReturnObject getPurchaseOrderNumber(String purchaseOrderID) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(purchaseOrderID);

        String hqlQuery = "select po.purchaseOrderNumber from PurchaseOrder po where po.ID=?";
        list = executeQuery( hqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    /**
     * Description : Method is used to get PO ID, PO Number & salesorderDetail
     * ID linked with PO
     *
     * @param <poDetailID> :-Purchase Order Detail ID
     *
     *
     * @return : Array of object
     */
    
    @Override
    public KwlReturnObject getPODetail(String poDetailID) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(poDetailID);
        String hqlQuery = "select po.ID, po.purchaseOrderNumber, podetail.salesorderdetailid from PurchaseOrderDetail podetail inner join podetail.purchaseOrder po where podetail.ID=?";
        list = executeQuery( hqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    
    /**
     * Description : Method is used to Update Entry in linking information table
     * for Vendor Quotation If any Customer Quotation linked with Vendor
     * Quotation
     *
     * @param <request> :-used to get parameters for which you want to update
     * entry in linking information table
     *
     * @return : success message & no of rows updated
     */
    
    @Override
    public KwlReturnObject updateEntryInVendorQuotationLinkingTable(HashMap<String, Object> request) throws ServiceException {
      
        String newitemID = UUID.randomUUID().toString();
        String linkeddocid = (String) request.get("linkeddocid");
        String docid = (String) request.get("docid");
        int moduleid = (Integer) request.get("moduleid");
        int sourceFlag = (Integer) request.get("sourceflag");
        String linkeddocno = (String) request.get("linkeddocno");

        String query = "insert into  vqlinking(id,docid,linkeddocid,linkeddocno,moduleid,sourceflag) values(" + '"' + newitemID + '"' + ',' + '"' + docid + '"' + ',' + '"' + linkeddocid + '"' + ',' + '"' + linkeddocno + '"' + ',' + '"' + moduleid + '"' + ',' + '"' + sourceFlag + '"' + ")";
        int numRows = executeSQLUpdate(query, new String[]{});
        return new KwlReturnObject(true, "Vendor Quotation Linking has been saved successfully.", null, null, numRows);
    }

    /**
     * Description : Method is used to get Vendor Quotation Number
     *
     * @param <purchaseOrderID> :- Vendor Quotation ID
     *
     *
     * @return : Vendor Quotation Number
     */
    
    @Override
    public KwlReturnObject getVendorQuotationNumber(String vendorQuotationID) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(vendorQuotationID);

        String hqlQuery = "select vq.quotationNumber from VendorQuotation vq where vq.ID=?";
        list = executeQuery(hqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    /**
     * Description : Method is used to delete Entry from linking information
     * table of Vendor Quotation & Customer Quotation If any Customer Quotation
     * linked with Vendor Quotation
     *
     * @param <requestParams> :-used to get parameters for which you want to
     * delete entry in linking information table
     *
     * @return : Total no of rows deleted
     */
    
    @Override
    public KwlReturnObject deleteLinkingInformationOfCQ(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        int numRows1 = 0, nuRows2 = 0, numRowsTotal = 0;
        try {

            String delQuery = "";
            if (requestParams.containsKey("unlinkflag") && requestParams.get("unlinkflag") != null && Boolean.parseBoolean(requestParams.get("unlinkflag").toString())) {
                int type = -1;
                if (requestParams.containsKey("type") && requestParams.get("type") != null) {
                    type = Integer.parseInt(requestParams.get("type").toString());
                }
                params.add(requestParams.get("qid"));
                params.add(requestParams.get("linkedTransactionID"));

                if (type == 5) {//VQ->CQ
                    delQuery = "delete from VendorQuotationLinking vql where vql.LinkedDocID=? and vql.DocID.ID=?";
                    numRows1 = executeUpdate(delQuery, params.toArray());

                    delQuery = "delete from QuotationLinking cql where  cql.DocID.ID=? and  cql.LinkedDocID=? ";
                    nuRows2 = executeUpdate(delQuery, params.toArray());
                }


            } else {
                params.add(requestParams.get("qid"));
                delQuery = "delete from VendorQuotationLinking vql where vql.LinkedDocID=?";
                numRows1 = executeUpdate(delQuery, params.toArray());

                delQuery = "delete from QuotationLinking cq where cq.DocID.ID=?";
                nuRows2 = executeUpdate(delQuery, params.toArray());
            }


            numRowsTotal = numRows1 + nuRows2;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("", ex);
        }
        return new KwlReturnObject(true, "", null, null, numRowsTotal);
    }
    /*
     * function to save Sales Order Status for Purchase Order
     */
    
    @Override
    public KwlReturnObject saveSalesOrderStatusForPO(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        Locale locale = null;
        String message = "";
        try {
            String soid = (String) hm.get("salesOrderID");
            String status = (String) hm.get("status");
            String salesOrderNo = (String) hm.get("salesOrderNo");
            if(hm.containsKey("locale")){
             locale = (Locale)hm.get("locale");
            }
    
            SalesOrder salesorder = (SalesOrder) get(SalesOrder.class, soid);
            if (status.equals("Open")) {
                salesorder.setDisabledSOforPO(true);
                message = messageSource.getMessage("acc.wtfTrans.so", null, locale) + " "+salesOrderNo +" "+ messageSource.getMessage("acc.so.hasbeenblockedforPurchaseOrder", null, locale);
            } else {
                salesorder.setDisabledSOforPO(false);
                message = messageSource.getMessage("acc.wtfTrans.so", null, locale) + " "+salesOrderNo +" "+ messageSource.getMessage("acc.so.hasbeenunblockedforPurchaseOrder", null, locale);
            }

            saveOrUpdate(salesorder);
            list.add(salesorder);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.saveSalesOrderStatusForPO : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, message, null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getContractFromDOContractMapping(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        try {
            String companyid = (String) requestParams.get("companyid");
            String contractid = (String) requestParams.get("contractid");
            params.add(contractid);
            params.add(companyid);
            String query1 = " select contract from docontractmapping where contract=? and company=?";
            list = executeSQLQuery(query1, params.toArray());

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getContractFromDOContractMapping : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getAccountContractDetails(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        try {
            String defaultSortCriteria = " order by createdon desc ";
            JSONArray jArr = new JSONArray();
            String companyID = (String) requestParams.get("companyid");
            params.add(companyID);
            String ss = requestParams.containsKey("ss") ? (String) requestParams.get("ss") : "";
            String dir = requestParams.containsKey("dir") ? (String) requestParams.get("dir") : "";
            String sort = requestParams.containsKey("sort") ? (String) requestParams.get("sort") : "";
            int start = requestParams.containsKey("start") ? (Integer) requestParams.get("start") : 0;
            int limit = requestParams.containsKey("limit") ? (Integer) requestParams.get("limit") : 25;
            String accountid = (String) requestParams.get("crmaccountid");

            String searchStringQuery = "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                searchStringQuery = " and (contractNumber like '%" + ss + "%' or customer.name like '%" + ss + "%' ) ";
            }
            String columnSort[] = null;
            String innerJoinQuery = "";
            if ((!StringUtil.isNullOrEmpty(dir)) && (!StringUtil.isNullOrEmpty(sort))) {
                columnSort = getQueryForSortingContracts(sort, dir);
                defaultSortCriteria = columnSort[0];
                innerJoinQuery += (!StringUtil.isNullOrEmpty(columnSort[1])) ? columnSort[1] : "";
            }
            String condition = "";
            if (!StringUtil.isNullOrEmpty(accountid)) {
                condition += " and customer.crmaccountid in (" + accountid + ") ";
            }

            String query = " from Contract where company.companyID = ? " + innerJoinQuery + condition + searchStringQuery + defaultSortCriteria;
            list = executeQueryPaging(query, params.toArray(), new Integer[]{start, limit});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getAccountContractDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public String[] getQueryForSortingContracts(String columnnane, String order) {
        String query[] = new String[2];
        String orderToSort = order;   // Asc or Desc 
        if (columnnane.equals("contractid")) {
            query[0] = " order by contractNumber " + orderToSort;
        }
        return query;
    }
    
    @Override
    public KwlReturnObject getContractProductList(String contractid, String companyid) throws ServiceException {
        List list = new ArrayList();
        try {
            String mysqlQuery = "select product.name,product.id,IF(contract = ?, quantity, 0),isasset,IF(contract = ?, 'true', 'false'), IF(contract = ?, contract, '') from contractdetails right join product on contractdetails.product=product.id where product.company=? and product.isasset='F' group by product.id";
            list = executeSQLQuery(mysqlQuery, new Object[]{contractid, contractid, contractid, companyid});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getAccountContractDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public JSONArray getBatchSerialByProductID(String productId, String contractid) throws ServiceException {
        BatchSerial batchSerial = null;
        JSONArray jarray = new JSONArray();

        String query = "SELECT bsr.serialname from newbatchserial bsr  "
                + " inner join serialdocumentmapping sdm on sdm.serialid=bsr.id "
                + " INNER JOIN  dodetails do on sdm.documentid=do.id "
                + " INNER JOIN  docontractmapping dcm on do.deliveryorder=dcm.deliveryorder "
                + " where dcm.contract= ? and bsr.product= ? and bsr.quantitydue=0"; //After Returning DO Serial no should not be present in CRM for replacement
                                                                                      //i.e quantity due becomes 1 in ERP

        String query1 = "SELECT bsr.serialname from newbatchserial bsr "
                + " inner join serialdocumentmapping sdm on sdm.serialid=bsr.id "
                + " INNER JOIN assetdetail asd on sdm.documentid=asd.id "
                + " INNER JOIN assetdetailsinvdetailmapping asdm on asd.id=asdm.assetdetails"
                + " INNER JOIN dodetails do on asdm.invoicedetailid =do.id"
                + " INNER JOIN docontractmapping dcm on  do.deliveryorder=dcm.deliveryorder WHERE dcm.contract=?  AND bsr.product=? and sdm.transactiontype in(40,27,67)";//for fixed asset serials whose do is created
        List params = new ArrayList();
        params.add(contractid);
        params.add(productId);
        List l1 = executeSQLQuery(query, params.toArray());
        List l2 = executeSQLQuery(query1, params.toArray());
        List l3 = new ArrayList();
        l3.addAll(l1);
        l3.addAll(l2);

        if (!l3.isEmpty()) {
            for (int i = 0; i < l3.size(); i++) {
                String SerialNumber = (String) l3.get(i);

                JSONArray jtemparray = new JSONArray();
                jtemparray.put(SerialNumber);
                jarray.put(jtemparray);
            }
        }
        return jarray;
    }
    
    /**
     * Description : Method is used to get the outstanding product quantity for Sales Order
     * @param <request> used to get parameters
     * @return : Map
     */
    @Override
    public Map<String, Double> getOutstandingQuantityCountForProductMap(Map<String, Object> request) throws ServiceException {
        Map<String, Double> productIDMap = new HashMap<>();
        List list = new ArrayList();
        try {
            String companyid = (String) request.get("companyid");
            String productIds = (String) request.get("productIds");
            String sodetailidquery = "select  sodetails.product,sum(sodetails.baseuomquantity) from sodetails inner join salesorder on sodetails.salesorder=salesorder.id where sodetails.product in (" + productIds + ") and salesorder.company=? and "
                    + " salesorder.deleteflag= ? and salesorder.isopeningbalenceso= ?  and salesorder.isconsignment= ? and "
                    + "salesorder.issoclosed= 'F' and sodetails.islineitemclosed='F' "
                    + " and salesorder.istemplate != 2  and salesorder.approvestatuslevel = ? group by sodetails.product";

            list = executeSQLQuery(sodetailidquery, new Object[]{companyid, 'F', request.get("isopeningbalenceso"), request.get("isconsignment"), 11});
            Map<String, Double> actualQtyMap = new HashMap<>();
            if (list != null && !list.isEmpty()) {
                for (Object object : list) {
                    Object[] actualQtyArr = (Object[]) object;
                    if (actualQtyArr[0] != null && actualQtyArr[1] != null) {
                        actualQtyMap.put(actualQtyArr[0].toString(),authHandler.roundQuantity(Double.parseDouble(actualQtyArr[1].toString()), companyid) );
                    }
                }
            }
            /*
             * From Sales Order Detailid its delivered count from deliveryorder and linked invoice
             * count
             */
            String condition = "select sodetails.id from sodetails inner join salesorder on sodetails.salesorder=salesorder.id "
                    + "where sodetails.product in (" + productIds + ") and salesorder.company=? and "
                    + " salesorder.deleteflag='F' and salesorder.isopeningbalenceso=?  and salesorder.isconsignment=? "
                    + " and salesorder.istemplate != 2  and salesorder.approvestatuslevel=11 "
                    + " and  salesorder.issoclosed= 'F' and sodetails.islineitemclosed='F'";

            String sqlquery = "select dodetails.product, sum(dodetails.baseuomdeliveredquantity) from dodetails inner join deliveryorder on dodetails.deliveryorder=deliveryorder.id "
                    + "where deliveryorder.deleteflag='F' and dodetails.sodetails in(" + condition + " ) group by dodetails.product "
                    + "union "
                    + "select  dodetails.product,sum(dodetails.baseuomdeliveredquantity) from dodetails inner join deliveryorder on dodetails.deliveryorder=deliveryorder.id "
                    + "where deliveryorder.deleteflag='F' and dodetails.cidetails in "
                    + " ( select invoicedetails.id from invoicedetails inner join invoice on invoicedetails.invoice=invoice.id "
                    + "where invoice.deleteflag='F' and invoicedetails.salesorderdetail in(" + condition + " )) group by dodetails.product ";

            list = executeSQLQuery(sqlquery, new Object[]{companyid, request.get("isopeningbalenceso"), request.get("isconsignment"), companyid, request.get("isopeningbalenceso"), request.get("isconsignment")});
            Map<String, Double> deliveryQtyMap = new HashMap<>();
            if (list != null && !list.isEmpty()) {
                for (Object object : list) {
                    Object[] actualQtyArr = (Object[]) object;
                    if (actualQtyArr[0] != null && actualQtyArr[1] != null) {
                        /*union used in DO query so multiple results can be returned, so need to add all quantities. */
                        if(deliveryQtyMap.containsKey(actualQtyArr[0].toString())){
                            double qty=Double.parseDouble(actualQtyArr[1].toString());
                             deliveryQtyMap.put(actualQtyArr[0].toString(),authHandler.roundQuantity((deliveryQtyMap.get(actualQtyArr[0].toString())+qty), companyid));
                        }else{
                        deliveryQtyMap.put(actualQtyArr[0].toString(),authHandler.roundQuantity(Double.parseDouble(actualQtyArr[1].toString()), companyid));
                    }
                }
            }
            }
            for (Map.Entry<String, Double> entry : actualQtyMap.entrySet()) {
                String productID = entry.getKey();
                double actualQty = entry.getValue();
                double  deliveredQty= 0;
                if (deliveryQtyMap.containsKey(productID)) {
                    deliveredQty = deliveryQtyMap.get(productID);
                }
                double count = 0;
                if (deliveredQty < actualQty) {
                    count = actualQty - deliveredQty;
                }
                productIDMap.put(productID, authHandler.roundQuantity(count, companyid));
            }
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getOutstandingQuantityCountForProductMap : " + ex.getMessage(), ex);
        }
        return productIDMap;
    }
    
    /**
     * Following method return id's of DeliveryOrder by companyid and productid
     *
     * @param contractid
     * @param companyid
     * @return
     * @throws ServiceException
     */
    @Override
    public List<String> getDelivereyOrderID(String contractid, String companyid) throws ServiceException {
        String sqlquery = "select dc.deliveryorder from docontractmapping dc inner join deliveryorder do ON dc.deliveryorder=do.id WHERE do.deleteflag=? AND dc.contract=? AND dc.company=?";
        List list = executeSQLQuery(sqlquery, new Object[]{"F", contractid, companyid});
        return list;
    }
    
    @Override
    public KwlReturnObject getSalesOrderDetailsForPriceVariance(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String companyid = (String) request.get(Constants.companyKey);
            DateFormat df = (DateFormat) request.get(Constants.df);
            String productid = (String) request.get(Constants.productid);

            String productCategoryid = "";
            if (request.containsKey(Constants.productCategoryid) && request.get(Constants.productCategoryid) != null) {
                productCategoryid = (String) request.get(Constants.productCategoryid);
            }
            String ss = (String) request.get(Constants.ss);

            ArrayList params = new ArrayList();
            String conditionSQL = "";

            params.add(companyid);
            if (!StringUtil.isNullOrEmpty(productid) && !StringUtil.equal(productid, "-1") && !StringUtil.equal(productid, "All")) {
                productid = AccountingManager.getFilterInString(productid);
                conditionSQL += " and sodetails.product in " + productid + "  ";
            }

            if (!StringUtil.isNullOrEmpty(productCategoryid) && !StringUtil.equal(productCategoryid, "-1")) {
                params.add(productCategoryid);
                conditionSQL += " and sodetails.product in (select productid from productcategorymapping where productcategory = ?)  ";
            }

            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                conditionSQL += " and (salesorder.orderdate >=? and salesorder.orderdate <=?) ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"salesorder.sonumber", "customer.name", "product.name"};
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 3); 
                StringUtil.insertParamSearchString(SearchStringMap);
                conditionSQL += searchQuery;
            }

            String mysqlQuery = "select salesorder.id soid, sodetails.id as sodid   from salesorder "
                    + " inner join sodetails on sodetails.salesorder = salesorder.id "
                    + " inner join customer on customer.id = salesorder.customer "
                    + " inner join product on product.id = sodetails.product "
                    + " where salesorder.company = ? and salesorder.deleteflag='F' and salesorder.pendingapproval=0 and salesorder.isdraft = false " + conditionSQL
                    + " order by salesorder.createdon desc";
            list = executeSQLQuery(mysqlQuery, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accSalesOrderImpl.getSalesOrderDetailsForPriceVariance:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }
    
    @Override
    public NewBatchSerial getBatchSerialByName(String productId, String SerialNumber) throws ServiceException {
        NewBatchSerial batchSerial = null;
        String query = "select bsr.id from newbatchserial bsr"
                + " where bsr.product = ? and bsr.serialname=? ";

        List params = new ArrayList();
        params.add(productId);
        params.add(SerialNumber);

        List ll = executeSQLQuery(query, params.toArray());

        if (!ll.isEmpty()) {

            String batchSerialId = (String) ll.get(0);
            batchSerial = (NewBatchSerial) get(NewBatchSerial.class, batchSerialId);
        }
        return batchSerial;
    }
    
        
    @Override
    public List deleteProductReplacement(String companyID, String replacementid, String replacementNumber) throws ServiceException{
        ArrayList returnList = new ArrayList();
        String linkedTransaction = "";
        String deletedTransaction = "";
        if (!StringUtil.isNullOrEmpty(replacementid)) {
            String getSOQuery = " from SalesOrderDetail so  where so.productReplacementDetail.id in ( select prd.id from ProductReplacementDetail prd where prd.productReplacement.id = ? ) and so.company.companyID=? ";
            List soList = executeQuery( getSOQuery, new Object[]{replacementid, companyID});

            if (!soList.isEmpty()) {
                linkedTransaction = replacementNumber;
            }
            if (soList.isEmpty()) {
                String getQuoQuery1 = " from QuotationDetail so  where so.productReplacementDetail.id in  ( select prd.id from ProductReplacementDetail prd where prd.productReplacement.id = ? ) and so.company.companyID=? ";
                List QList1 = executeQuery(getQuoQuery1, new Object[]{replacementid, companyID});
                if (!QList1.isEmpty()) {
                    linkedTransaction = replacementNumber;
                }
            }
            // delete if not linked
            if (StringUtil.isNullOrEmpty(linkedTransaction)) {
                deletedTransaction = replacementid;
                String delQuery1 = " delete from replacementproductbatchdetailsmapping where productreplacement=? and company=? ";
                executeSQLUpdate( delQuery1, new Object[]{replacementid, companyID});

                String delQuery2 = " delete from productreplacementdetail where productreplacement=? and company=? ";
                executeSQLUpdate(delQuery2, new Object[]{replacementid, companyID});

                String delQuery3 = " delete from productreplacement where id=? and company=? ";
                executeSQLUpdate(delQuery3, new Object[]{replacementid, companyID});
            }
        }

        returnList.add(linkedTransaction);
        returnList.add(deletedTransaction);
        return returnList;
//        return linkedTransaction;        
    }
    
    @Override
    public List getProductReplacementByReplacementNumber(String replacementNumber, String companyId) throws ServiceException {
        String query = "from ProductReplacement pr where pr.replacementRequestNumber=? and pr.company.companyID=?";
        List ll = executeQuery(query, new Object[]{replacementNumber, companyId});
        return ll;
    }

    @Override
    public List getProductMaintenanceByReplacementNumber(String maintenanceNumber, String companyId) throws ServiceException {
        String query = "from Maintenance mn where mn.maintenanceNumber=? and mn.company.companyID=?";
        List ll = executeQuery(query, new Object[]{maintenanceNumber, companyId});
        return ll;
    }
    
    @Override
    public List deleteProductMaintenence(String companyID, String maintainanceid, String maintenanceNumber) throws ServiceException {
        ArrayList returnList = new ArrayList();
        String linkedTransaction = "";
        String deletedTransaction = "";
        if (!StringUtil.isNullOrEmpty(maintainanceid)) {
            String getSOQuery = "from SalesOrder so  where so.maintenance.id = ? and so.company.companyID=? ";
            List soList = executeQuery(getSOQuery, new Object[]{maintainanceid, companyID});

            if (!soList.isEmpty()) {
                linkedTransaction = maintenanceNumber;
            }

            // delete if not linked
            if (StringUtil.isNullOrEmpty(linkedTransaction)) {
                deletedTransaction = maintainanceid;
                String delQuery = " delete from Maintenance where company.companyID = ? and id = ? ";
                executeUpdate(delQuery, new Object[]{companyID, maintainanceid});
            }
        }

        returnList.add(linkedTransaction);
        returnList.add(deletedTransaction);
        return returnList;
//        return linkedTransaction;
    }

    /* Fetching VQ details by ID*/
    @Override
    public List getVQdetails(String vqdetailID, String companyid) throws ServiceException {

        String hqlQuery = "from VendorQuotationDetail vqd  where vqd.ID = ? and vqd.company.companyID=?";
        List returnList = executeQuery(hqlQuery, new Object[]{vqdetailID, companyid});
        return returnList;
    }

    /* Fetching PO details by ID*/
    @Override
    public List getPOdetails(String poDetailsID, String companyid) throws ServiceException {

        String hqlQuery = "from PurchaseOrderDetail pod  where pod.ID = ? and pod.company.companyID=?";
        List returnList = executeQuery(hqlQuery, new Object[]{poDetailsID, companyid});
        return returnList;
    }
    
    @Override
    public KwlReturnObject saveQuotationDetailsTermMap(HashMap<String, Object> dataMap) throws ServiceException{
        List list = new ArrayList();
        try {
            QuotationDetailTermMap termmap = new QuotationDetailTermMap();

            if (dataMap.containsKey("id")) {
                termmap = (QuotationDetailTermMap) get(QuotationDetailTermMap.class, (String) dataMap.get("id"));
            }
            if (dataMap.containsKey("termamount") && dataMap.get("termamount")!=null && !StringUtil.isNullOrEmpty(dataMap.get("termamount").toString())) {
                termmap.setTermamount(Double.parseDouble(dataMap.get("termamount").toString()));
            }
            if (dataMap.containsKey("termpercentage") && dataMap.get("termpercentage")!=null && !StringUtil.isNullOrEmpty(dataMap.get("termpercentage").toString())) {
                termmap.setPercentage(Double.parseDouble(dataMap.get("termpercentage").toString()));
            }
            if (dataMap.containsKey("assessablevalue") && dataMap.get("assessablevalue")!=null && !StringUtil.isNullOrEmpty(dataMap.get("assessablevalue").toString())) {
                termmap.setAssessablevalue(Double.parseDouble(dataMap.get("assessablevalue").toString()));
            }
            if (dataMap.containsKey("quotationDetailID") && dataMap.get("quotationDetailID")!=null && !StringUtil.isNullOrEmpty(dataMap.get("quotationDetailID").toString())) {
                QuotationDetail quotationDetail = (QuotationDetail) get(QuotationDetail.class, (String) dataMap.get("quotationDetailID"));
                termmap.setQuotationDetail(quotationDetail);
            }
            if (dataMap.containsKey("term") && dataMap.get("term")!=null && !StringUtil.isNullOrEmpty(dataMap.get("term").toString())) {
                LineLevelTerms term = (LineLevelTerms) get(LineLevelTerms.class, (String) dataMap.get("term"));
                termmap.setTerm(term);
            }
            if (dataMap.containsKey("userid") && dataMap.get("userid")!=null && !StringUtil.isNullOrEmpty(dataMap.get("userid").toString())) {
                User userid = (User) get(User.class, (String) dataMap.get("userid"));
                termmap.setCreator(userid);
            }
            if (dataMap.containsKey("createdon") && dataMap.get("createdon")!=null && !StringUtil.isNullOrEmpty(dataMap.get("createdon").toString())) {
                termmap.setCreatedOn(((Date) dataMap.get("createdon")).getTime());
            }
            if (dataMap.containsKey("product") && dataMap.get("product")!=null && !StringUtil.isNullOrEmpty(dataMap.get("product").toString())) {
                Product product = (Product) get(Product.class, (String) dataMap.get("product"));
                termmap.setProduct(product);
            }
            if (dataMap.containsKey("purchasevalueorsalevalue") && dataMap.get("purchasevalueorsalevalue")!=null && !StringUtil.isNullOrEmpty(dataMap.get("purchasevalueorsalevalue").toString())) {
                termmap.setPurchaseValueOrSaleValue(Double.parseDouble(dataMap.get("purchasevalueorsalevalue").toString()));
            }
            if (dataMap.containsKey("deductionorabatementpercent") && dataMap.get("deductionorabatementpercent")!=null && !StringUtil.isNullOrEmpty(dataMap.get("deductionorabatementpercent").toString())) {
                termmap.setDeductionOrAbatementPercent(Double.parseDouble(dataMap.get("deductionorabatementpercent").toString()));
            }
            if (dataMap.containsKey("taxtype") && dataMap.get("taxtype")!=null && !StringUtil.isNullOrEmpty(dataMap.get("taxtype").toString())) {
                termmap.setTaxType(Integer.parseInt(dataMap.get("taxtype").toString()));
            }
            if (dataMap.containsKey("isDefault")) {
                termmap.setIsGSTApplied(Boolean.parseBoolean(dataMap.get("isDefault").toString()));
            }
            if (dataMap.containsKey("productentitytermid")) {
                EntitybasedLineLevelTermRate term = (EntitybasedLineLevelTermRate) get(EntitybasedLineLevelTermRate.class, (String) dataMap.get("productentitytermid"));
                termmap.setEntitybasedLineLevelTermRate(term);
            }

            saveOrUpdate(termmap);
            list.add(termmap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.saveQuotationDetailsTermMap : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getQuotationDetailTermMap(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        List param = new ArrayList();
        try {
            String query = "from QuotationDetailTermMap ";
            String condition="";
            String orderby=" order by term.termSequence ";
            if(hm.containsKey("quotationDetailId") && hm.get("quotationDetailId") != null){
                String InvoiceDetailid = hm.get("quotationDetailId").toString();
                condition += " quotationDetail.ID = ? ";
                param.add(InvoiceDetailid);
            }
            if(hm.containsKey("termtype") && hm.get("termtype") != null){
                if(!StringUtil.isNullOrEmpty(condition)){
                    condition +=" and ";
                }
                condition += " term.termType = ? ";
                param.add(hm.get("termtype"));
            }
            if(!StringUtil.isNullOrEmpty(condition)){
                query += " where "+condition ;
            }
            if(hm.containsKey("orderbyadditionaltax") && hm.get("orderbyadditionaltax") != null){
                orderby += " , term.isAdditionalTax ";
            }
            orderby += " ASC ";
            query += orderby;
            list = executeQuery( query,param.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getQuotationDetailTermMap:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject saveSalesOrderDetailsTermMap(HashMap<String, Object> dataMap) throws ServiceException{
        List list = new ArrayList();
        try {
            SalesOrderDetailTermMap termmap = new SalesOrderDetailTermMap();

            if (dataMap.containsKey("id")) {
                termmap = (SalesOrderDetailTermMap) get(SalesOrderDetailTermMap.class, (String) dataMap.get("id"));
            }
            if (dataMap.containsKey("termamount") && dataMap.get("termamount")!=null && !StringUtil.isNullOrEmpty(dataMap.get("termamount").toString())) {
                termmap.setTermamount(Double.parseDouble(dataMap.get("termamount").toString()));
            }
            if (dataMap.containsKey("termpercentage") && dataMap.get("termpercentage")!=null && !StringUtil.isNullOrEmpty(dataMap.get("termpercentage").toString())) {
                termmap.setPercentage(Double.parseDouble(dataMap.get("termpercentage").toString()));
            }
            if (dataMap.containsKey("termpercentage") && dataMap.get("termpercentage")!=null && !StringUtil.isNullOrEmpty(dataMap.get("termpercentage").toString())) {
                termmap.setPercentage(Double.parseDouble(dataMap.get("termpercentage").toString()));
            }
            if (dataMap.containsKey("assessablevalue") && dataMap.get("assessablevalue")!=null && !StringUtil.isNullOrEmpty(dataMap.get("assessablevalue").toString())) {
                termmap.setAssessablevalue(Double.parseDouble(dataMap.get("assessablevalue").toString()));
            }
            if (dataMap.containsKey("salesOrderDetailID") && dataMap.get("salesOrderDetailID")!=null && !StringUtil.isNullOrEmpty(dataMap.get("salesOrderDetailID").toString())) {
                SalesOrderDetail salesOrderDetail = (SalesOrderDetail) get(SalesOrderDetail.class, (String) dataMap.get("salesOrderDetailID"));
                termmap.setSalesOrderDetail(salesOrderDetail);
            }
            if (dataMap.containsKey("term") && dataMap.get("term")!=null && !StringUtil.isNullOrEmpty(dataMap.get("term").toString())) {
                LineLevelTerms term = (LineLevelTerms) get(LineLevelTerms.class, (String) dataMap.get("term"));
                termmap.setTerm(term);
            }
            if (dataMap.containsKey("userid") && dataMap.get("userid")!=null && !StringUtil.isNullOrEmpty(dataMap.get("userid").toString())) {
                User userid = (User) get(User.class, (String) dataMap.get("userid"));
                termmap.setCreator(userid);
            }
            if (dataMap.containsKey("createdon") && dataMap.get("createdon")!=null && !StringUtil.isNullOrEmpty(dataMap.get("createdon").toString())) {
                termmap.setCreatedOn(((Date) dataMap.get("createdon")).getTime());
            }
        if (dataMap.containsKey("product") && dataMap.get("product")!=null && !StringUtil.isNullOrEmpty(dataMap.get("product").toString())) {
                Product product = (Product) get(Product.class, (String) dataMap.get("product"));
                termmap.setProduct(product);
            }
            if (dataMap.containsKey("purchasevalueorsalevalue") && dataMap.get("purchasevalueorsalevalue")!=null && !StringUtil.isNullOrEmpty(dataMap.get("purchasevalueorsalevalue").toString())) {
                termmap.setPurchaseValueOrSaleValue(Double.parseDouble(dataMap.get("purchasevalueorsalevalue").toString()));
            }
            if (dataMap.containsKey("deductionorabatementpercent") && dataMap.get("deductionorabatementpercent")!=null && !StringUtil.isNullOrEmpty(dataMap.get("deductionorabatementpercent").toString())) {
                termmap.setDeductionOrAbatementPercent(Double.parseDouble(dataMap.get("deductionorabatementpercent").toString()));
            }
            if (dataMap.containsKey("taxtype") && dataMap.get("taxtype")!=null && !StringUtil.isNullOrEmpty(dataMap.get("taxtype").toString())) {
                termmap.setTaxType(Integer.parseInt(dataMap.get("taxtype").toString()));
            }
            if (dataMap.containsKey("isDefault")) {
                termmap.setIsGSTApplied(Boolean.parseBoolean(dataMap.get("isDefault").toString()));
            }
            if (dataMap.containsKey("productentitytermid")) {
                EntitybasedLineLevelTermRate term = (EntitybasedLineLevelTermRate) get(EntitybasedLineLevelTermRate.class, (String) dataMap.get("productentitytermid"));
                termmap.setEntitybasedLineLevelTermRate(term);
            }

            saveOrUpdate(termmap);
            list.add(termmap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.saveSalesOrderDetailsTermMap : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    @Override
    public KwlReturnObject saveContractDetailsTermsMap(HashMap<String, Object> dataMap) throws ServiceException{
        List list = new ArrayList();
        try {
            ContractDetailTermsMap termmap = new ContractDetailTermsMap();

            if (dataMap.containsKey("id")) {
                termmap = (ContractDetailTermsMap) get(ContractDetailTermsMap.class, (String) dataMap.get("id"));
            }
            if (dataMap.containsKey("termamount") && dataMap.get("termamount")!=null && !StringUtil.isNullOrEmpty(dataMap.get("termamount").toString())) {
                termmap.setTermamount(Double.parseDouble(dataMap.get("termamount").toString()));
            }
            if (dataMap.containsKey("termpercentage") && dataMap.get("termpercentage")!=null && !StringUtil.isNullOrEmpty(dataMap.get("termpercentage").toString())) {
                termmap.setPercentage(Double.parseDouble(dataMap.get("termpercentage").toString()));            
            }
            if (dataMap.containsKey("assessablevalue") && dataMap.get("assessablevalue")!=null && !StringUtil.isNullOrEmpty(dataMap.get("assessablevalue").toString())) {
                termmap.setAssessablevalue(Double.parseDouble(dataMap.get("assessablevalue").toString()));
            }
            if (dataMap.containsKey("contractDetailID") && dataMap.get("contractDetailID")!=null && !StringUtil.isNullOrEmpty(dataMap.get("contractDetailID").toString())) {
                ContractDetail contractDetail = (ContractDetail) get(ContractDetail.class, (String) dataMap.get("contractDetailID"));
                termmap.setContractdetail(contractDetail);
            }
           if (dataMap.containsKey("userid") && dataMap.get("userid")!=null && !StringUtil.isNullOrEmpty(dataMap.get("userid").toString())) {
                User userid = (User) get(User.class, (String) dataMap.get("userid"));
                termmap.setCreator(userid);
            }
            if (dataMap.containsKey("createdon") && dataMap.get("createdon")!=null && !StringUtil.isNullOrEmpty(dataMap.get("createdon").toString())) {
                termmap.setCreatedOn(((Date) dataMap.get("createdon")).getTime());
            }       
            if (dataMap.containsKey("purchasevalueorsalevalue") && dataMap.get("purchasevalueorsalevalue")!=null && !StringUtil.isNullOrEmpty(dataMap.get("purchasevalueorsalevalue").toString())) {
                termmap.setPurchaseValueOrSaleValue(Double.parseDouble(dataMap.get("purchasevalueorsalevalue").toString()));
            }
            if (dataMap.containsKey("deductionorabatementpercent") && dataMap.get("deductionorabatementpercent")!=null && !StringUtil.isNullOrEmpty(dataMap.get("deductionorabatementpercent").toString())) {
                termmap.setDeductionOrAbatementPercent(Double.parseDouble(dataMap.get("deductionorabatementpercent").toString()));
            }
            if (dataMap.containsKey("taxtype") && dataMap.get("taxtype")!=null && !StringUtil.isNullOrEmpty(dataMap.get("taxtype").toString())) {
                termmap.setTaxType(Integer.parseInt(dataMap.get("taxtype").toString()));
            }
            if (dataMap.containsKey("isDefault")) {
                termmap.setIsGSTApplied(Boolean.parseBoolean(dataMap.get("isDefault").toString()));
            }
            if (dataMap.containsKey("productentitytermid")) {
                EntitybasedLineLevelTermRate term = (EntitybasedLineLevelTermRate) get(EntitybasedLineLevelTermRate.class, (String) dataMap.get("productentitytermid"));
                termmap.setEntitybasedLineLevelTermRate(term);
            }

            saveOrUpdate(termmap);
            list.add(termmap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.saveContractDetailTermsMap : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
     @Override
    public KwlReturnObject getContractDetailTermsMap(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        List param = new ArrayList();
        try {
            String query = "from ContractDetailTermsMap ";
            String condition = "";            
            if (hm.containsKey("contractDetailId") && hm.get("contractDetailId") != null) {
                String SODetailid = hm.get("contractDetailId").toString();
                condition += " contractdetail.ID = ? ";
                param.add(SODetailid);
            }                      
            if (!StringUtil.isNullOrEmpty(condition)) {
                query += " where " + condition;
            }                   
            list = executeQuery(query, param.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getContractDetailTermsMap:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject getSalesOrderDetailTermMap(HashMap<String, Object> hm) throws ServiceException {
        List list = new ArrayList();
        List param = new ArrayList();
        try {
            String query = "from SalesOrderDetailTermMap ";
            String condition = "";
            String orderby = " order by term.termSequence ";
            if (hm.containsKey("salesOrderDetailId") && hm.get("salesOrderDetailId") != null) {
                String SODetailid = hm.get("salesOrderDetailId").toString();
                condition += " salesOrderDetail.ID = ? ";
                param.add(SODetailid);
            }
            if (hm.containsKey("productid") && hm.get("productid") != null) {
                if (!StringUtil.isNullOrEmpty(condition)) {
                    condition += " and ";
                }
                condition += " product.ID = ? ";
                param.add(hm.get("productid"));
            }
            if (hm.containsKey("termtype") && hm.get("termtype") != null) {
                if (!StringUtil.isNullOrEmpty(condition)) {
                    condition += " and ";
                }
                condition += " term.termType = ? ";
                param.add(hm.get("termtype"));
            }
            if (!StringUtil.isNullOrEmpty(condition)) {
                query += " where " + condition;
            }
            if (hm.containsKey("orderbyadditionaltax") && hm.get("orderbyadditionaltax") != null) {
                orderby += " , term.isAdditionalTax ";
            }
            orderby += " ASC ";
            query += orderby;
            list = executeQuery(query, param.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getSalesOrderDetailTermMap:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    /**
     * Get GSt Rule Present or Not
     * @param mapData
     * @return
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject getGSTRuleSetupForImportSalesOrder(HashMap<String, Object> mapData) throws ServiceException {
        List<Object> listData = new ArrayList<Object>();
        List<Object> dateData = new ArrayList<Object>();
        String productcategory = "", condtion = "";
        String datecondition="";
        boolean salesOrPurchase = false;
        String defaultquery = "From EntitybasedLineLevelTermRate eltr ";
        if (mapData.containsKey("isProdCategoryPresent") && mapData.containsKey("productcategory")) {
            /**
             * If Product category present then fetch data using mapping table
             */
            boolean isProdCategoryPresent = (Boolean) mapData.get("isProdCategoryPresent");
            if (isProdCategoryPresent) {
                defaultquery = "select eltr From ProductCategoryGstRulesMappping pcgm inner join pcgm.entitybasedLineLevelTermRate eltr ";
                condtion += " where pcgm.prodCategory.id = ? ";
                productcategory = mapData.get("productcategory").toString();
                listData.add(productcategory);
            }
        }
        if (mapData.containsKey("salesOrPurchase")) {

            if (condtion.trim().length() > 1) {
                condtion += " and eltr.lineLevelTerms.salesOrPurchase= ? ";
            } else {
                condtion = " where eltr.lineLevelTerms.salesOrPurchase= ? ";
            }
            salesOrPurchase = Boolean.parseBoolean(mapData.get("salesOrPurchase").toString());
            listData.add(salesOrPurchase);
        }
        /**
         * linelevelterms
         */
        if (mapData.containsKey("linelevelterms") && mapData.get("linelevelterms")!=null) {
            condtion += " and eltr.lineLevelTerms.id= ? ";
            datecondition += " and eltr1.lineLevelTerms.id=? ";
            listData.add(mapData.get("linelevelterms").toString());
            dateData.add(mapData.get("linelevelterms").toString());
        }

        if (mapData.containsKey("entity")) {
            if (condtion.trim().length() > 1) {
                condtion += " and eltr.entity.id=? ";
            } else {
                condtion += " where eltr.entity.id=? ";
            }
            listData.add((String) mapData.get("entity"));
        }
        if (mapData.containsKey("todimension1")) {
            if (condtion.trim().length() > 1) {
                condtion += " and eltr.shippedLoc1.id=? ";
                datecondition += " and eltr1.shippedLoc1.id=? ";
            }
            listData.add((String) mapData.get("todimension1"));
            dateData.add((String) mapData.get("todimension1"));
        }
        if (mapData.containsKey("todimension2")) {
            if (condtion.trim().length() > 1) {
                condtion += " and eltr.shippedLoc2.id=? ";
                datecondition += " and eltr1.shippedLoc2.id=? ";
            }
            listData.add((String) mapData.get("todimension2"));
            dateData.add((String) mapData.get("todimension2"));
        }
        if (mapData.containsKey("todimension3")) {
            if (condtion.trim().length() > 1) {
                condtion += " and eltr.shippedLoc3.id=? ";
                datecondition += " and eltr1.shippedLoc3.id=? ";
            }
            listData.add((String) mapData.get("todimension3"));
            dateData.add((String) mapData.get("todimension3"));
        }
        if (mapData.containsKey("todimension4")) {
            if (condtion.trim().length() > 1) {
                condtion += " and eltr.shippedLoc4.id=? ";
                datecondition += " and eltr1.shippedLoc4.id=? ";
            }
            listData.add((String) mapData.get("todimension4"));
            dateData.add((String) mapData.get("todimension4"));
        }
        if (mapData.containsKey("todimension5")) {
            if (condtion.trim().length() > 1) {
                condtion += " and eltr.shippedLoc5.id=? ";
                datecondition += " and eltr1.shippedLoc5.id=? ";
            }
            listData.add((String) mapData.get("todimension5"));
            dateData.add((String) mapData.get("todimension5"));
        }
        if (mapData.containsKey("applieddate")) {
            if (condtion.trim().length() > 1) {
                condtion += " and eltr.appliedDate = (select max(eltr1.appliedDate) from ProductCategoryGstRulesMappping pcd1 inner join  "
                        + "pcd1.entitybasedLineLevelTermRate eltr1 where eltr1.appliedDate<=? and eltr1.id=pcd1.entitybasedLineLevelTermRate.id and eltr1.lineLevelTerms.company.companyID=? "
                        + "and eltr1.entity.id=? and pcd1.prodCategory.id = ? and eltr1.lineLevelTerms.salesOrPurchase= ? "
                        + datecondition + ") ";
            }
            listData.add((Date) mapData.get("applieddate"));
            listData.add((String) mapData.get("companyid"));
            listData.add((String) mapData.get("entity"));
            listData.add(productcategory);
            listData.add(salesOrPurchase);
            if(!StringUtil.isNullOrEmpty(datecondition)){
                listData.addAll(dateData);
            }
        }
        if (mapData.containsKey("colnum")) {
            int colnum = (int) mapData.get("colnum");
            condtion += " group by eltr.id order by eltr.shippedLoc" + colnum;
        } else {
            condtion += " group by eltr.id order by eltr.lineLevelTerms.termSequence ASC ";
        }
        String q = defaultquery + condtion;
        List list = executeQuery(q, listData.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
      /* Method is used to close Sales Order manually*/
    @Override
    public KwlReturnObject closeDocument(HashMap<String, Object> requestParams) throws ServiceException {
        String message = "";
        Locale locale = null;
        try {
            SalesOrder salesOrder = (SalesOrder) requestParams.get("salesOrder");
            boolean soCloseFlag = (Boolean) requestParams.get("closeFlag");
            if (requestParams.containsKey("locale")) {
                locale = (Locale) requestParams.get("locale");
            }
            salesOrder.setIsSOClosed(soCloseFlag);
            saveOrUpdate(salesOrder);
            message = messageSource.getMessage("acc.invoiceList.closesomanually", null, locale);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.closeDocument:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, message, null, null, 0);
    }

    /* Method is used to close line level product manually*/
    @Override
    public KwlReturnObject closeLineItem(HashMap<String, Object> requestParams) throws ServiceException {
        String message = "";
        try {
            SalesOrderDetail salesOrderDetail = (SalesOrderDetail) requestParams.get("salesorderDetail");
            salesOrderDetail.setIsLineItemClosed(true);
            saveOrUpdate(salesOrderDetail);
            message = "Selected Line Item has been Closed Manually.";
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.closeDocument:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, message, null, null, 0);
    }
    /* Method is reject quntity at line level product manually*/
    @Override
    public KwlReturnObject rejectLineItem(HashMap<String, Object> requestParams) throws ServiceException {
        String message = "";
        try {
            SalesOrderDetail salesOrderDetail = (SalesOrderDetail) requestParams.get("salesorderDetail");
            salesOrderDetail.setIsLineItemRejected(true);
            if(requestParams.containsKey("reason") && !StringUtil.isNullOrEmpty( (String)requestParams.get("reason"))){
                salesOrderDetail.setRejectionreason((String)requestParams.get("reason"));
            }
            saveOrUpdate(salesOrderDetail);
            message = "Selected Line Item has been Rejected.";
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.rejectLineItem:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, message, null, null, 0);
    }

    
     /* Method is used to check whether SO is used in DO or not i.e SO->DO or SO->SI->DO*/
    @Override
    public KwlReturnObject checkWhetherSOIsUsedInDOOrNot(String soDetailID, String companyid) throws ServiceException {
        List list = new ArrayList();
        try {
            String query ="select dodetails.id from dodetails inner join deliveryorder on dodetails.deliveryorder=deliveryorder.id "
                    + "where  deliveryorder.company=? and deliveryorder.deleteflag='F' and deliveryorder.approvestatuslevel=11 and dodetails.cidetails in "
                    + " ( select invoicedetails.id from invoicedetails inner join invoice on invoicedetails.invoice=invoice.id "
                    + "where invoice.company=? and invoice.deleteflag='F' and invoicedetails.salesorderdetail=? )";
            list = executeSQLQuery(query, new Object[]{companyid, companyid, soDetailID});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.checkWhetherSOIsUsedInDOOrNot:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public Map<String, SalesOrderInfo> getSalesOrderList(List<String> invoiceIDLIST) {
        Map<String, SalesOrderInfo> invoiceMap = new HashMap<>();
        if (invoiceIDLIST != null && !invoiceIDLIST.isEmpty()) {
            try {
                String query = "select  so.ID, so, "
                        + " so.company, so.customer, so.currency "
                        + " from SalesOrder so "
                        + " where so.ID in (:invoiceIDList)";
                List<List> values = new ArrayList<>();
                values.add(invoiceIDLIST);
                List<Object[]> results = executeCollectionQuery( query, Collections.singletonList("invoiceIDList"), values);
                
                if (results != null) {
                    for (Object[] result : results) {
                        String invID = (String) result[0];
                        SalesOrderInfo info = new SalesOrderInfo();
                        info.setSalesOrderID(invID);
                        info.setSalesOrder((SalesOrder) result[1]);
                        info.setCompany((Company) result[2]);
                        info.setCustomer((Customer) result[3]);
                        info.setCurrency((KWLCurrency) result[4]);
                        invoiceMap.put(invID, info);
                    }
                }
            } catch (ServiceException ex) {
                Logger.getLogger(accSalesOrderImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return invoiceMap;
    }
    /* Get SO with or without DO/invoice or with DO+Invoice both*/

    @Override
    public KwlReturnObject getRelevantSalesOrderLinkingWise(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {
            String sqlQuery = "";
            String start = (String) requestParams.get(Constants.start);
            String limit = (String) requestParams.get(Constants.limit);

            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String ss = (String) requestParams.get(Constants.ss);

            int orderLinkedWithDocType = (Integer) requestParams.get("orderLinkedWithDocType");

            boolean isLeaseSO = false;
            boolean isConsignment = false;
            boolean isMRPSalesOrder = false;
            boolean isJobWorkOrderReciever = false;
            boolean isDraft = false;
            String companyId = (String) requestParams.get(Constants.companyKey);

            boolean pendingapproval = (Boolean) requestParams.get("pendingapproval");
            String newcustomerid = "";
            if (requestParams.containsKey(Constants.newcustomerid) && requestParams.get(Constants.newcustomerid) != null) {
                newcustomerid = (String) requestParams.get(Constants.newcustomerid);
            }
            if (requestParams.containsKey("isJobWorkOrderReciever") && requestParams.get("isJobWorkOrderReciever") != null) {
                isJobWorkOrderReciever = (Boolean) requestParams.get("isJobWorkOrderReciever");
            }
            if (requestParams.containsKey(Constants.isDraft) && requestParams.get(Constants.isDraft) != null) {
                isDraft = (Boolean) requestParams.get(Constants.isDraft);
            }
            String conditionSQL = "";
            ArrayList paramsWithTradingFlow = new ArrayList();
            ArrayList params = new ArrayList();
            ArrayList searchParams = new ArrayList();

            params.add((String) requestParams.get(Constants.companyKey));
            paramsWithTradingFlow.add((String) requestParams.get(Constants.companyKey));

            conditionSQL = " where salesorder.deleteflag='F' and salesorder.company=?";
            
            /**
             * added SalesPerson and User ID for view Filter in SO.
             */
            String userID = "";
            boolean isenableSalesPersonAgentFlow = false;
            if (requestParams.containsKey("enablesalespersonagentflow") && requestParams.get("enablesalespersonagentflow") != null && !StringUtil.isNullOrEmpty(requestParams.get("enablesalespersonagentflow").toString())) {
                isenableSalesPersonAgentFlow = Boolean.parseBoolean(requestParams.get("enablesalespersonagentflow").toString());
            }
            if (isenableSalesPersonAgentFlow) {
                if (requestParams.containsKey("userid") && requestParams.get("userid") != null && !StringUtil.isNullOrEmpty(requestParams.get("userid").toString())) {
                    userID = (String) requestParams.get("userid");
                }
            }
            String userDepartment = "";
            if (requestParams.containsKey("userDepartment") && requestParams.get("userDepartment") != null) {
                userDepartment = (String) requestParams.get("userDepartment");
            }
            /**
             * SDP-14217, To Check Permission for view Records Other Users.
             */
            if (requestParams.containsKey("salesPersonFilterFlag") && (Boolean) requestParams.get("salesPersonFilterFlag") && requestParams.get("userId") != null) {
                String userId = (String) requestParams.get("userId");
                if (!StringUtil.isNullOrEmpty(userId)) {
                    DataFilteringModule dataFilteringModule = null;
                    MasterItem masterItem = null;
                    List<DataFilteringModule> dataFilteringModuleList = new ArrayList<DataFilteringModule>();
                    List<MasterItem> masterItems = new ArrayList<MasterItem>();

                    dataFilteringModuleList = find("from DataFilteringModule where user.userID='" + userId + "' and company.companyID='" + companyId + "'");
                    masterItems = find("from MasterItem where user='" + userId + "' and company.companyID='" + companyId + "' and masterGroup.ID='" + 15 + "'");
                    if (!dataFilteringModuleList.isEmpty()) {
                        dataFilteringModule = dataFilteringModuleList.get(0);
                    }
                    if ((dataFilteringModule != null && !dataFilteringModule.isSalesOrder()) || (dataFilteringModule != null && !dataFilteringModule.isSalesOrder() && masterItem != null)) {
                        conditionSQL += " and ( ";
                    }

                    if (dataFilteringModule != null && !dataFilteringModule.isSalesOrder()) {
                        params.add(dataFilteringModule.getUser().getUserID());
                        conditionSQL += "salesorder.createdby=? ";
                    }

                    if (dataFilteringModule != null && !dataFilteringModule.isSalesOrder() && !masterItems.isEmpty()) {
                        String qMarks = "";
                        for (MasterItem item : masterItems) {
                            qMarks += "?,";
                            params.add(item.getID());
                        }
                        qMarks = qMarks.substring(0, qMarks.length() - 1);
                        conditionSQL += " or salesorder.salesperson in (" + qMarks + ")";
                    }
                    if ((dataFilteringModule != null && !dataFilteringModule.isSalesOrder()) || (dataFilteringModule != null && !dataFilteringModule.isSalesOrder() && masterItem != null)) {
                        conditionSQL += " ) ";
                    }
                }
            }
            
//            if (!StringUtil.isNullOrEmpty(ss)) {
//                for (int i = 0; i < 5; i++) {
//
//                    params.add("%" + ss + "%");
//                    paramsWithTradingFlow.add("%" + ss + "%");
//                }
//
//                conditionSQL += " and (salesorder.sonumber like ? or salesorder.memo like ? or customer.name like ? or product.name like ? or product.productid like ? )";
//            }

            //// query based on CostCenter parameter
            String costCenterId = (String) requestParams.get(CCConstants.REQ_costCenterId);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {

                params.add(costCenterId);
                paramsWithTradingFlow.add(costCenterId);

                conditionSQL += " and salesorder.costcenter=?";
            }
            if (!StringUtil.isNullOrEmpty(newcustomerid)) {

                params.add(newcustomerid);
                paramsWithTradingFlow.add(newcustomerid);

                conditionSQL += " and  salesorder.customer = ? ";

            }

            String customerCategoryid = "";
            if (requestParams.containsKey(Constants.customerCategoryid) && requestParams.get(Constants.customerCategoryid) != null) {
                customerCategoryid = (String) requestParams.get(Constants.customerCategoryid);
            }
            if (!StringUtil.isNullOrEmpty(customerCategoryid) && !StringUtil.equal(customerCategoryid, "-1") && !StringUtil.equal(customerCategoryid, "All")) {

                params.add(customerCategoryid);
                paramsWithTradingFlow.add(customerCategoryid);

                conditionSQL += " and salesorder.customer in (select customerid from customercategorymapping where customercategory = ?)  ";

            }

            if (isLeaseSO) {
                conditionSQL += " and salesorder.leaseOrMaintenanceSO=1 ";
            } else if (isConsignment) {
                conditionSQL += " and (salesorder.isconsignment='T'  and  salesorder.leaseOrMaintenanceSO=3) ";
            } else {
                conditionSQL += " and (salesorder.leaseOrMaintenanceSO=0 or salesorder.leaseOrMaintenanceSO=2)";
            }

            if (isMRPSalesOrder) {
                conditionSQL += " and  salesorder.ismrpsalesorder='T' ";
            } else {
                conditionSQL += " and  salesorder.ismrpsalesorder='F' ";
            }

            if (pendingapproval) {
                conditionSQL += " and salesorder.pendingapproval != 0  and salesorder.approvestatuslevel != 11";
            } else {
                conditionSQL += " and salesorder.pendingapproval= 0  and salesorder.approvestatuslevel = 11";
            }
            if (isDraft) {
                conditionSQL += " and salesorder.isdraft = true ";
            } else {
                conditionSQL += " and salesorder.isdraft = false ";
            }
            if (isJobWorkOrderReciever) {
                conditionSQL += " and  salesorder.isjobworkorder='T' ";
            } else {
                conditionSQL += " and  salesorder.isjobworkorder='F' ";
            }
            //Ignore POs created as only templates.
            conditionSQL += " and salesorder.istemplate != 2 ";
            if (orderLinkedWithDocType == 12) {
                conditionSQL += " and  salesorder.linkflag = 0 ";

            }

            //// query based on start date & end date parameters
            String startDate = (String) requestParams.get(Constants.REQ_startdate)!=null ? StringUtil.DecodeText((String) requestParams.get(Constants.REQ_startdate)) : null;
            String endDate = (String) requestParams.get(Constants.REQ_enddate)!=null ? StringUtil.DecodeText((String) requestParams.get(Constants.REQ_enddate)) : null;
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {

                conditionSQL += " and (salesorder.orderdate >=? and salesorder.orderdate <=? )";

                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
                paramsWithTradingFlow.add(df.parse(startDate));
                paramsWithTradingFlow.add(df.parse(endDate));

            }

            String productid = "";
            if (requestParams.containsKey(Constants.productid) && requestParams.get(Constants.productid) != null) {
                productid = (String) requestParams.get(Constants.productid);
            }

            String productCategoryid = "";
            if (requestParams.containsKey(Constants.productCategoryid) && requestParams.get(Constants.productCategoryid) != null) {
                productCategoryid = (String) requestParams.get(Constants.productCategoryid);
            }

            String innerQuery = "";
            if (!StringUtil.isNullOrEmpty(productid)) {

                params.add(productid);
                paramsWithTradingFlow.add(productid);
                innerQuery = " inner join sodetails on sodetails.salesorder = salesorder.id ";
                conditionSQL += " and sodetails.product = ? ";

            }

            if (!StringUtil.isNullOrEmpty(productCategoryid)) {

                params.add(productCategoryid);
                paramsWithTradingFlow.add(productCategoryid);
                innerQuery = " inner join sodetails on sodetails.salesorder = salesorder.id ";
                conditionSQL += " and sodetails.product in (select productid from productcategorymapping where productcategory = ?) ";

            }

            if (orderLinkedWithDocType == 13 || orderLinkedWithDocType == 14) {

                params.add((String) requestParams.get(Constants.companyKey));

            }

            if (requestParams.containsKey("linknumber") && requestParams.get("linknumber") != null && !requestParams.get("linknumber").toString().equals("")) {
                conditionSQL += " and salesorder.sonumber = ? ";
                params.add(requestParams.get("linknumber"));
                paramsWithTradingFlow.add(requestParams.get("linknumber"));
            }

            String moduleid = "";
            if (requestParams.containsKey(Constants.moduleid) && requestParams.get(Constants.moduleid) != null) {
                moduleid = requestParams.get(Constants.moduleid).toString();
            }
            
            String appendCase = "and";
            String Searchjson = "";
            String mySearchFilterString = "";
            String searchDefaultFieldSQL = "";
            String joinString1 = "";
            String searchJoin = "";

            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"salesorder.sonumber", "salesorder.memo", "customer.name", "customer.aliasname",
                    "bsaddr.billingaddress", "bsaddr.billingcountry", "bsaddr.billingstate", "bsaddr.billingcity", "bsaddr.billingemail", "bsaddr.billingpostal",
                    "bsaddr.shippingaddress", "bsaddr.shippingCountry", "bsaddr.shippingstate", "bsaddr.shippingcity", "bsaddr.shippingemail", "bsaddr.shippingpostal", "product.name", "product.productid"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(searchParams, ss, 18);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                conditionSQL += searchQuery;
                searchJoin += " inner join sodetails on sodetails.salesorder = salesorder.id "
                        + " inner join billingshippingaddresses bsaddr on bsaddr.id=salesorder.billingshippingaddresses "
                        + " inner join product on sodetails.product = product.id "
                        + " inner join customer on customer.id = salesorder.customer ";
            }

            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (requestParams.containsKey("filterConjuctionCriteria") && requestParams.get("filterConjuctionCriteria") != null) {
                if (requestParams.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            String linkSearchDocument = "";
            String linkQuery = "";
            if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null) {
                Searchjson = StringUtil.DecodeText(requestParams.get("searchJson").toString());

                if (!StringUtil.isNullOrEmpty(Searchjson)) {
                    JSONObject serachJobj = new JSONObject(Searchjson);
                    JSONArray customSearchFieldArray = new JSONArray();
                    JSONArray defaultSearchFieldArray = new JSONArray();
                    StringUtil.seperateCostomAndDefaultSerachJson(serachJobj, customSearchFieldArray, defaultSearchFieldArray);
                    if (defaultSearchFieldArray.length() > 0) {

                        for (int i = 0; i < defaultSearchFieldArray.length(); i++) {
                            JSONObject jsonobj = defaultSearchFieldArray.getJSONObject(i);
                            linkSearchDocument = StringUtil.DecodeText(jsonobj.getString("columnheader"));
                            if (linkSearchDocument.equalsIgnoreCase("CQ No.") || linkSearchDocument.equalsIgnoreCase("PO No.")) {
                                linkQuery = "left join solinking on solinking.docid=salesorder.id";
                                break;
                            }

                        }
                        /*
                         Advance Search For Default Form fields
                         */
                        ArrayList tableArray = new ArrayList();
                        tableArray.add("customer"); //this table array used to identified wheather join exists on table or not                         
                        Map<String, Object> map = buildSqlDefaultFieldAdvSearch(defaultSearchFieldArray, searchParams, moduleid, tableArray, filterConjuctionCriteria);
                        searchJoin += map.containsKey("searchjoin") ? map.get("searchjoin") : "";
                        searchJoin += " left join solinking sol on sol.docid=salesorder.id and sol.sourceflag = 1 ";
                        searchDefaultFieldSQL = (String) (map.containsKey("condition") ? map.get("condition") : "");
                    }

                    if (customSearchFieldArray.length() > 0) {   //Advance search case for Custome field

                        requestParams.put(Constants.Searchjson, Searchjson);
                        requestParams.put(Constants.appendCase, appendCase);
                        requestParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(requestParams, true).get(Constants.myResult));
                        String innerJoinOnDetailTable = " inner join sodetails on sodetails.salesorder=salesorder.id ";
                        boolean isInnerJoinAppend = false;
                        if (mySearchFilterString.contains("salesordercustomdata")) {
                            joinString1 = " inner join salesordercustomdata on salesordercustomdata.soID=salesorder.salesordercustomdataref ";
                        }
                        if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "salesorderdetailcustomdata");
                            joinString1 += innerJoinOnDetailTable + " left join salesorderdetailcustomdata on sodetails.id=salesorderdetailcustomdata.soDetailID ";
                            isInnerJoinAppend = true;
                        }
                        if (mySearchFilterString.contains("CustomerCustomData")) {
                            joinString1 += " left join customercustomdata  on customercustomdata.customerId=salesorder.customer ";
                            mySearchFilterString = mySearchFilterString.replaceAll("CustomerCustomData", "customercustomdata");
                        }
                        if (mySearchFilterString.contains("AccJEDetailsProductCustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "sodetailproductcustomdata");
                            joinString1 += (isInnerJoinAppend ? "" : innerJoinOnDetailTable) + " left join sodetailproductcustomdata on sodetails.id=sodetailproductcustomdata.soDetailID ";
                            isInnerJoinAppend = true;
                        }
                        //product custom data
                        if (mySearchFilterString.contains("accproductcustomdata")) {
                            joinString1 += (isInnerJoinAppend ? "" : innerJoinOnDetailTable) + " left join product on product.id=sodetails.product left join accproductcustomdata on accproductcustomdata.productId=product.id ";
                        }
                        StringUtil.insertParamAdvanceSearchString1(searchParams, Searchjson);
                    }
                    mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
                }
            }

            String mySearchFilterString1 = "";
            if (linkQuery != "") {
                mySearchFilterString1 = mySearchFilterString;
                mySearchFilterString = "";
            }
            /**
             * this block is executed only when owners restriction feature is on.  
             */
            if (isenableSalesPersonAgentFlow && !StringUtil.isNullOrEmpty(userID)) {//
                String salesPersonID = (String) requestParams.get("salesPersonid");
                String salespersonQuery = "";
                if (!StringUtil.isNullOrEmpty(salesPersonID)) {
                    salesPersonID = AccountingManager.getFilterInString(salesPersonID);
                    salespersonQuery = "  salesorder.salesperson in " + salesPersonID + " or ";
                }
                conditionSQL += " and ( " + salespersonQuery + "  salesorder.createdby='" + userID + "' or salesorder.salesperson is null  ) ";
            }
            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                innerQuery += " inner join users on users.userid = salesorder.createdby ";
                conditionSQL += " and users.department =  '"+ userDepartment + "' ";
            }
            
            
            String orderBy = "";
            String sort_Col = "";
            String sort_Col1 = "";
            String joinString2 = "";
            String[] stringSort = null;
            if (requestParams.containsKey("dir") && requestParams.containsKey("sort")) {
                String Col_Name = requestParams.get("sort").toString();
                String Col_Dir = requestParams.get("dir").toString();
                stringSort = columSort(Col_Name, Col_Dir);
                orderBy += stringSort[0];
                sort_Col += stringSort[1];
                sort_Col1 += stringSort[2];
                if(requestParams.get("sort").toString().equals("salespersonname")){
                    joinString2 += "  left join masteritem on masteritem.id = salesorder.salesperson  ";
                }else if((requestParams.get("sort").toString().equals("personname") || requestParams.get("sort").toString().equals("aliasname")) && StringUtil.isNullOrEmpty(ss)){
                    joinString2 += " inner join customer on customer.id = salesorder.customer ";
                }

            } else {
                if (isConsignment) {
                    stringSort = columSort("billno", "desc");
                    orderBy = " order by orderdate desc,sonumber desc " ;
                    sort_Col +=", salesorder.orderdate " + stringSort[1] ;
                    sort_Col1 +=  ", billingsalesorder.orderdate " + stringSort[2] ;
                } else {
                    orderBy = " order by orderdate desc ";
                    sort_Col += ", salesorder.orderdate ";
                    sort_Col1 += ", billingsalesorder.orderdate ";
                }
            }
            
            if (orderLinkedWithDocType != 15) {
                if (linkQuery != "") {
                    mySearchFilterString1 += orderBy;
                } else {
                    mySearchFilterString += orderBy;
                }
            }
            
            params.addAll(searchParams);

            if (orderLinkedWithDocType == 12) {

                /*Query for Get SO linked without DO or Invoice */
                sqlQuery = "select   DISTINCT salesorder.id, 'false' as withoutinventory , salesorder.orderdate " + sort_Col + " from salesorder " + linkQuery + innerQuery + searchJoin + joinString1 + joinString2 + conditionSQL + ((StringUtil.isNullOrEmpty(linkQuery))?mySearchFilterString:mySearchFilterString1);

            } else if (orderLinkedWithDocType == 13) {

                /*Query for Get SO linked with Invoice only 
                 ie SO->SI
                 */
                sqlQuery = "select   DISTINCT salesorder.id, 'false' as withoutinventory , salesorder.orderdate " + sort_Col + " from salesorder \n"
                        + innerQuery + searchJoin + joinString1 + joinString2
                        + "inner join solinking on salesorder.id=solinking.docid \n"
                        + " inner join invoicelinking invl on invl.docid  =solinking.linkeddocid \n"
                        + conditionSQL + " and invl.docid not in (select dol.linkeddocid from dolinking dol inner join deliveryorder do on do.id=dol.docid where do.company=? ) " + mySearchFilterString;
                if (linkQuery != "") {
                    sqlQuery += "and  salesorder.id IN(select salesorder.id from salesorder \n"
                            + "inner join solinking on solinking.docid=salesorder.id and solinking.sourceflag=1\n"
                            + conditionSQL + mySearchFilterString1 + ")";

                    params.add((String) requestParams.get(Constants.companyKey));
                    params.add(df.parse(startDate));
                    params.add(df.parse(endDate));
                }

            } else if (orderLinkedWithDocType == 14) {

                /*Query for Get SO linked with DO only
                 I.e SO->DO
                 */
                sqlQuery = "select   DISTINCT salesorder.id, 'false' as withoutinventory , salesorder.orderdate " + sort_Col + " from salesorder  \n"
                        + innerQuery + searchJoin + joinString1 + joinString2
                        + "inner join solinking on salesorder.id=solinking.docid \n"
                        + " inner join dolinking dol on dol.docid  =solinking.linkeddocid \n"
                        + conditionSQL + " and dol.docid not in (select invl.linkeddocid from invoicelinking invl inner join invoice inv on inv.id=invl.docid where inv.company=? ) " + mySearchFilterString;
                /* If Advanced search is applied for linked parent document no*/
                if (linkQuery != "") {
                    sqlQuery += "and  salesorder.id IN(select salesorder.id from salesorder \n"
                            + "inner join solinking on solinking.docid=salesorder.id and solinking.sourceflag=1\n"
                            + conditionSQL + mySearchFilterString1 + ")";

                    params.add((String) requestParams.get(Constants.companyKey));
                    params.add(df.parse(startDate));
                    params.add(df.parse(endDate));
                }
            } else if (orderLinkedWithDocType == 15) {

                /*Query for Get SO linked with DO and Invoice
                
                 i.e SO->DO>SI or SO->SI->DO
                 */
                sqlQuery = "select  DISTINCT salesorder.id, 'false' as withoutinventory , salesorder.orderdate " + sort_Col + " from salesorder \n"
                        + innerQuery + searchJoin + joinString1 + joinString2
                        + "inner join solinking on salesorder.id=solinking.docid \n"
                        + " inner join dolinking dol on dol.docid  =solinking.linkeddocid \n"
                        + " inner join  invoicelinking invl on invl.linkeddocid = dol.docid \n"
                        + conditionSQL + " " + mySearchFilterString;

                if (linkQuery != "") {
                    sqlQuery += "and  salesorder.id IN(select salesorder.id from salesorder \n"
                            + "inner join solinking on solinking.docid=salesorder.id and solinking.sourceflag=1\n"
                            + conditionSQL + mySearchFilterString1 + ")";

                    params.add((String) requestParams.get(Constants.companyKey));
                    params.add(df.parse(startDate));
                    params.add(df.parse(endDate));
                    params.addAll(searchParams);
                }

                sqlQuery += "\n"
                        + "union"
                        + "\n"
                        + "( select  DISTINCT salesorder.id, 'false' as withoutinventory , salesorder.orderdate " + sort_Col + " from salesorder \n"
                        + innerQuery + searchJoin + joinString1 + joinString2
                        + "inner join solinking on salesorder.id=solinking.docid \n"
                        + "inner join  invoicelinking invl on invl.docid = solinking.linkeddocid  \n"
                        + "inner join dolinking dol on dol.docid  =invl.linkeddocid  \n"
                        + conditionSQL + " " + mySearchFilterString;

                    params.add((String) requestParams.get(Constants.companyKey));
                    params.add(df.parse(startDate));
                    params.add(df.parse(endDate));
                    params.addAll(searchParams);

                if (linkQuery != "") {
                    sqlQuery += "and  salesorder.id IN(select salesorder.id from salesorder \n"
                            + "inner join solinking on solinking.docid=salesorder.id and solinking.sourceflag=1\n"
                            + conditionSQL + mySearchFilterString1 + ")";

                    params.add((String) requestParams.get(Constants.companyKey));
                    params.add(df.parse(startDate));
                    params.add(df.parse(endDate));
                    params.addAll(searchParams);
                }
                
                sqlQuery += orderBy;
                sqlQuery += " ) ";

            }

            list = executeSQLQuery(sqlQuery, params.toArray());
            count = list.size();
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                list = executeSQLQueryPaging(sqlQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.checkWhetherSOIsUsedInDOOrNot:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);

    }
    
    @Override
    public KwlReturnObject getMarginCostForCrossLinkedTransactions(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        try {
            String conditionSQL = "";
            String innerSQL = "";
            String columns = "";
            String table = "";
            boolean isPOLinked = false;
            if (requestParams.containsKey("isPOLinked") && requestParams.get("isPOLinked") != null) {
                isPOLinked = (Boolean) requestParams.get("isPOLinked");
            }
            boolean isCQLinked = false;
            if (requestParams.containsKey("isCQLinked") && requestParams.get("isCQLinked") != null) {
                isCQLinked = (Boolean) requestParams.get("isCQLinked");
            }

            if (requestParams.containsKey("qDetailID") && requestParams.get("qDetailID") != null) { // if creating SO linked with CQ (linked with VQ) OR editing CQ (linked with VQ)
                String qDetailID = (String) requestParams.get("qDetailID");
                conditionSQL += (conditionSQL.isEmpty() ? "where" : "and") + " qd.id = ? ";

                columns = " vqd.rate, vq.externalcurrencyrate, vq.currency ";
                table = " vendorquotationdetails vqd ";
                innerSQL += " inner join quotationdetails qd on qd.vendorquotationdetails = vqd.id ";
                innerSQL += " inner join vendorquotation vq on vq.id = vqd.vendorquotation ";

                params.add(qDetailID);
            } else if (requestParams.containsKey("soDetailID") && requestParams.get("soDetailID") != null) {
                String soDetailID = (String) requestParams.get("soDetailID");
                conditionSQL += (conditionSQL.isEmpty() ? "where" : "and") + " sod.id = ? ";

                if (isPOLinked) { // if editing SO linked with PO
                    columns = " pod.rate, po.externalcurrencyrate, po.currency ";
                    table = " podetails pod ";
                    innerSQL += " inner join sodetails sod on sod.purchaseorderdetailid = pod.id ";
                    innerSQL += " inner join purchaseorder po on po.id = pod.purchaseorder ";
                } else { // if editing SO linked with CQ (linked with VQ)
                    columns = " vqd.rate, vq.externalcurrencyrate, vq.currency ";
                    table = " vendorquotationdetails vqd ";
                    innerSQL += " inner join quotationdetails qd on qd.vendorquotationdetails = vqd.id ";
                    innerSQL += " inner join sodetails sod on sod.quotationdetail = qd.id ";
                    innerSQL += " inner join vendorquotation vq on vq.id = vqd.vendorquotation ";
                }

                params.add(soDetailID);
            } else if (requestParams.containsKey("invDetailID") && requestParams.get("invDetailID") != null) {
                String invDetailID = (String) requestParams.get("invDetailID");
                conditionSQL += (conditionSQL.isEmpty() ? "where" : "and") + " invd.id = ? ";

                if (isPOLinked) { // if editing SI linked with SO (linked with PO)
                    columns = " pod.rate, po.externalcurrencyrate, po.currency ";
                    table = " podetails pod ";
                    innerSQL += " inner join sodetails sod on sod.purchaseorderdetailid = pod.id ";
                    innerSQL += " inner join invoicedetails invd on invd.salesorderdetail = sod.id ";
                    innerSQL += " inner join purchaseorder po on po.id = pod.purchaseorder ";
                } else if (isCQLinked) { // SI linked to CQ (linked with VQ)
                    columns = " vqd.rate, vq.externalcurrencyrate, vq.currency ";
                    table = " vendorquotationdetails vqd ";
                    innerSQL += " inner join quotationdetails qd on qd.vendorquotationdetails = vqd.id ";
                    innerSQL += " inner join invoicedetails invd on invd.quotationdetail = qd.id ";
                    innerSQL += " inner join vendorquotation vq on vq.id = vqd.vendorquotation ";
                } else { // if editing SI linked with SO (linked with CQ (linked with VQ))
                    columns = " vqd.rate, vq.externalcurrencyrate, vq.currency ";
                    table = " vendorquotationdetails vqd ";
                    innerSQL += " inner join quotationdetails qd on qd.vendorquotationdetails = vqd.id ";
                    innerSQL += " inner join sodetails sod on sod.quotationdetail = qd.id ";
                    innerSQL += " inner join invoicedetails invd on invd.salesorderdetail = sod.id ";
                    innerSQL += " inner join vendorquotation vq on vq.id = vqd.vendorquotation ";
                }

                params.add(invDetailID);
            }

            String query = "select " + columns + " from " + table + innerSQL + conditionSQL;
            list = executeSQLQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getMarginCostForCrossLinkedTransactions:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public Map<String, String> getApprovalStatusofSO(String company, Date frmDate, Date toDate,String requestStatus) throws ServiceException {
        Map<String, String> sttsmap = new HashMap<>();
        List<Object[]> list = new ArrayList();
        ArrayList params = new ArrayList();
        String condQuery = "";
        String conditionalQuery="";
        if (frmDate != null && toDate != null) {
            condQuery = " and (s.orderDate >=? and s.orderDate <=? ) ";
        }
        if ("Rejected".equals(requestStatus)) {
            conditionalQuery = " where tb.rjqty=tb.quantity ";
        }
        String query = "SELECT (CASE "
                + " WHEN (rjqty=quantity) THEN 'rejected' "
                + " WHEN (rjqty+apprqty=quantity)  THEN 'Approved' "
                + " WHEN (rjqty+apprqty=0)  THEN 'Pending Approval' "
                + " WHEN (rjqty+apprqty<quantity)  THEN 'Partially Approved' "
                + " END) AS approvalstatus,sonumber FROM (SELECT SUM(rejectedbasequantity) as rjqty,SUM(approvedbasequantity) AS apprqty,SUM(baseuomquantity) AS quantity,s.sonumber "
                + " FROM sodetails dtl "
                + " INNER JOIN salesorder s ON s.id=dtl.salesorder AND s.company=dtl.company "
                + " WHERE dtl.company=? " + condQuery + "  GROUP BY  salesorder) AS tb "+ conditionalQuery;

        params.add(company);
        if (frmDate!=null && toDate!=null) {
            params.add(frmDate);
            params.add(toDate);
        }

        list = executeSQLQuery(query, params.toArray());
        if (list != null) {
            for (Object[] result : list) {
                String approvalStatus = result[0] != null ? (String) result[0] : "";
                String soNumber = result[1] != null ? (String) result[1] : "";
                sttsmap.put(soNumber, approvalStatus);
            }
        }
        return sttsmap;
    }

    @Override
    public String validateToedit(String formRecord, String billid, boolean isConsignment, Company company) {
        String msg = "";
        ArrayList params = new ArrayList();
        ArrayList params1 = new ArrayList();
        
        boolean isValid = true;
        try {

            String query = " SELECT * from in_consignment WHERE company=? AND modulerefid=? ";
            params.add(company.getCompanyID());
            params.add(billid);

            List list = executeSQLQuery(query, params.toArray());
//            List<Object[]> dataList = executeSQLQuery(query, params.toArray());
            if (list != null&&list.size()>0) {
                msg = "Return request is already sent for QA so you can not edit it.";
                isValid=false;
                return msg;
}

            String selQuery = "SELECT sr.id,sr.srnumber,dtl.product,nb.id AS nbid,nb.quantitydue,lcm.quantity,lcm.transactiontype FROM salesreturn sr "
                    + " INNER JOIN srdetails dtl ON dtl.salesreturn=sr.id "
                    + " INNER JOIN product p ON p.id=dtl.product AND p.isSerialForProduct='F' "
                    + " INNER JOIN locationbatchdocumentmapping lcm ON lcm.documentid=dtl.id "
                    + " INNER JOIN newproductbatch nb ON nb.id=lcm.batchmapid "
                    + " WHERE sr.company=? and nb.quantitydue<lcm.quantity  AND p.isSerialForProduct='F' AND lcm.transactiontype=53 "
                    + " AND sr.id=? AND lcm.isconsignment='F'";

            params = new ArrayList();
            params.add(company.getCompanyID());
            params.add(billid);
            list = executeSQLQuery(selQuery, params.toArray());
            if (list != null&&list.size()>0) {
                msg = " Return stock is already used in some other transaction, so you can not edit it. ";
                isValid=false;
                return msg;
            }

            String selQuery1 = " SELECT sr.id,sr.srnumber,dtl.product,nb.id AS nbid,nb.quantitydue,lcm.transactiontype FROM salesreturn sr "
                    + " INNER JOIN srdetails dtl ON dtl.salesreturn=sr.id "
                    + " INNER JOIN product p ON p.id=dtl.product AND p.isSerialForProduct='T' "
                    + " INNER JOIN serialdocumentmapping lcm ON lcm.documentid=dtl.id "
                    + " INNER JOIN newbatchserial nb ON nb.id=lcm.serialid "
                    + " WHERE sr.company=? and (nb.quantitydue=0 or nb.lockquantity=1)  AND p.isSerialForProduct='T' AND lcm.isconsignment='F' "
                    + " AND lcm.transactiontype=53 AND  sr.id=? ";

            params = new ArrayList();
            params.add(company.getCompanyID());
            params.add(billid);
            list = executeSQLQuery(selQuery1, params.toArray());
            if (list != null&&list.size()>0) {
                msg = " Return stock is already used in some other transaction, so you can not edit it. ";
                isValid=false;
                return msg;
            }
            
           
            String selstoksm = " SELECT sm.transactionno,sm.product,sm.transaction_date,sm.createdon,smdtl.serialnames,smdtl.batchname FROM in_stockmovement sm  "
                    + " INNER JOIN in_sm_detail  smdtl ON sm.id=smdtl.stockmovement "
                    + " INNER JOIN product p ON p.id=sm.product AND p.isSerialForProduct='T' "
                    + " WHERE sm.company=? AND sm.modulerefid=?  ";

            params1.add(company.getCompanyID());
            params1.add(billid);
            
             List<Object[]> dataList = executeSQLQuery(selstoksm, params1.toArray());
          
            if (dataList != null&&dataList.size()>0) {
                
                for (Object[] result : dataList) {
                    String transactionNo = result[0] != null ? (String) result[0] : "";
                    String product = result[1] != null ? (String) result[1] : "";
                    String transactionDate = result[2] != null ?""+ result[2] : "";
                    String createdon = result[3] != null ? ""+result[3] : "";
                    String serialNames = result[4] != null ? (String) result[4] : "";
                    String batchName = result[5] != null ? (String) result[5] : "";

                    String condiQuery = "";
                    if (!StringUtil.isNullOrEmpty(batchName)) {
                        condiQuery = " AND smdtl.batchname='" + batchName + "' ";
                    }
                    if (!StringUtil.isNullOrEmpty(serialNames)) {
                        String[] srArray = serialNames.split(",");
                        for (String sr : srArray) {
                            ArrayList parms = new ArrayList();
                            String seleSm = " SELECT sm.transactionno,sm.product,sm.transaction_date,sm.createdon,smdtl.serialnames,smdtl.batchname FROM in_stockmovement sm "
                                    + " INNER JOIN in_sm_detail  smdtl ON sm.id=smdtl.stockmovement "
                                    + " INNER JOIN product p ON p.id=sm.product AND p.isSerialForProduct='T' "
                                    + " WHERE sm.company=? AND sm.product=? AND sm.createdon>? AND smdtl.serialnames LIKE '%" + sr + "%' " + condiQuery;

                            parms.add(company.getCompanyID());
//                            parms.add(billid);
                            parms.add(product);
                            parms.add(createdon);
                            
                            List smList = executeSQLQuery(seleSm, parms.toArray());
                            if (smList != null && smList.size() > 0) {
                                msg = " Return stock is already used in some other transaction, so you can not edit it. ";
                                isValid = false;
                            }
                        }
                    }

                }
            }

        } catch (ServiceException ex) {
            Logger.getLogger(accSalesOrderImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return msg;
    }

        /*  Get Sales Order detail for bulk Invoice */
    @Override
    public KwlReturnObject getSalesOrderDetailsForBulkInvoices(List soId, String companyId) throws ServiceException {
        List list = null;
        List newList=new ArrayList();
        int count = 0;
        try {
            
            String query = "from SalesOrderDetail where salesOrder.ID =? and company.companyID=? "; //" order by inv.customerEntry.account.id, inv.invoiceNumber";            
            for(int i= 0; i < soId.size(); i++) {
                String soIdstr = (String) soId.get(i);
                list = executeQuery( query, new Object[]{soIdstr, companyId});
                for (int j = 0; j < list.size(); j++) {
                    newList.add(list.get(j));

                }
            }
         
            count = newList.size();
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accInvoiceImpl.getSalesOrderDetailsForBulkInvoices:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, newList, count);
    }

       /*  Get Sales Order detail for individual Invoice */
    @Override
    public KwlReturnObject getSalesOrderDetails(String soId, String companyId) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String query = "from SalesOrderDetail where salesOrder.ID =? and company.companyID=? "; //" order by inv.customerEntry.account.id, inv.invoiceNumber";            
            list = executeQuery(query, new Object[]{soId, companyId});
            count = list.size();
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accInvoiceImpl.getSalesOrderDetails:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }
    @Override
    public double getSOStatusOnBalanceQty(String soid, String companyId) throws ServiceException {
        List list = null;
        Double count = 0d;
        try {
            String query = "select sum(sod.balanceqty) from salesorder so inner join sodetails sod on so.id=sod.salesorder where sod.salesorder=? and sod.balanceqty > 0 and so.company=? group by so.id";
            list = executeSQLQuery(query, new Object[]{soid, companyId});
            if (list != null && !list.isEmpty()) {
                for (int j = 0; j < list.size(); j++) {
                    count += (Double) list.get(j);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accSalesOrderImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accSalesOrderImpl.getSOStatusOnBalanceQty:" + ex.getMessage(), ex);
        }
        return authHandler.roundQuantity(count, companyId);
    }

  
    /* Uses:-Fetch Sales order that is not linked with any one child document
     i.e SO must not be linked with DO or Invoice
     */
    @Override
    public KwlReturnObject getUnInvoicedSalesOrders(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            DateFormat df = (DateFormat) request.get(Constants.df);
            String start = (String) request.get(Constants.start);
            String limit = (String) request.get(Constants.limit);
            boolean isLeaseSO = false;
            boolean isConsignment = false;
            boolean isMRPSalesOrder = false;
            boolean isJobWorkOrderReciever = false;
            String userID = "";
     
            boolean bulkInv = false;
            if (request.get("bulkInv") != null) {
                bulkInv = (Boolean) request.get("bulkInv");
            }
            boolean isenableSalesPersonAgentFlow=false;
            if (request.containsKey("enablesalespersonagentflow") && request.get("enablesalespersonagentflow") != null && !StringUtil.isNullOrEmpty(request.get("enablesalespersonagentflow").toString())) {
                isenableSalesPersonAgentFlow = Boolean.parseBoolean(request.get("enablesalespersonagentflow").toString());
            }
            if (isenableSalesPersonAgentFlow) {
                if (request.containsKey("userid") && request.get("userid") != null && !StringUtil.isNullOrEmpty(request.get("userid").toString())) {
                    userID = (String) request.get("userid");
                }
            }

            String customerCategoryid = "";
            if (request.containsKey(Constants.customerCategoryid) && request.get(Constants.customerCategoryid) != null) {
                customerCategoryid = (String) request.get(Constants.customerCategoryid);
            }
            String billDate = "";
            if (request.containsKey(Constants.BillDate)) {
                billDate = (String) request.get(Constants.BillDate);
            }

            boolean checkSOForCustomer = false;
            if (request.containsKey(Constants.checksoforcustomer) && request.get(Constants.checksoforcustomer) != null) {
                checkSOForCustomer = (Boolean) request.get(Constants.checksoforcustomer);
            }
            String ss = (String) request.get(Constants.ss);
  
            ArrayList params = new ArrayList();
            ArrayList paramsWithoutInv = new ArrayList();
            params.add((String) request.get(Constants.companyKey));
            
            paramsWithoutInv.add((String) request.get(Constants.companyKey));
            
          
              if (request.containsKey("isLeaseFixedAsset") && request.get("isLeaseFixedAsset") != null) {
                isLeaseSO = (Boolean) request.get("isLeaseFixedAsset");
            }
            if (request.containsKey("isConsignment") && request.get("isConsignment") != null) {
                isConsignment = (Boolean) request.get("isConsignment");
            }
            
            if (request.containsKey("isMRPSalesOrder") && request.get("isMRPSalesOrder") != null) {
                isMRPSalesOrder = (Boolean) request.get("isMRPSalesOrder");
            }
            if (request.containsKey("isJobWorkOrderReciever") && request.get("isJobWorkOrderReciever") != null) {
                isJobWorkOrderReciever = (Boolean) request.get("isJobWorkOrderReciever");
            }
            
            String billId = "";
         
                  
            String conditionSQL = "";
            String conditionSQLWithoutInv = "";
  
               
                conditionSQL += " where salesorder.company=?";
      

            /* Code for Quick Search */
            String searchJoin = "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"salesorder.sonumber", "salesorder.memo", "customer.name", "customer.aliasname",
                    "bsaddr.billingaddress", "bsaddr.billingcountry", "bsaddr.billingstate", "bsaddr.billingcity", "bsaddr.billingemail", "bsaddr.billingpostal",
                    "bsaddr.shippingaddress", "bsaddr.shippingCountry", "bsaddr.shippingstate", "bsaddr.shippingcity", "bsaddr.shippingemail", "bsaddr.shippingpostal", "product.name", "product.productid"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 18);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                conditionSQL += searchQuery;
                searchJoin = " inner join sodetails on sodetails.salesorder = salesorder.id "
                        + " inner join billingshippingaddresses bsaddr on bsaddr.id=salesorder.billingshippingaddresses "
                        + " inner join product on sodetails.product = product.id ";  
            }


           /*  Code for Customer category filter*/
            if (!StringUtil.isNullOrEmpty(customerCategoryid) && !StringUtil.equal(customerCategoryid, "-1") && !StringUtil.equal(customerCategoryid, "All")) {
                params.add(customerCategoryid);
                paramsWithoutInv.add(customerCategoryid);
                conditionSQL += " and salesorder.customer in (select customerid from customercategorymapping where customercategory = ?)  ";
                conditionSQLWithoutInv += " and billingsalesorder.customer in (select customerid from customercategorymapping where customercategory = ?)  ";
            }

            if (request.containsKey("billId")) {
                billId = (String) request.get("billId");
            }

            if (!StringUtil.isNullOrEmpty(billId) && !checkSOForCustomer) {
                params.add(billId);
                paramsWithoutInv.add(billId);
                conditionSQL += " and salesorder.id=? ";
                conditionSQLWithoutInv += " and billingsalesorder.id=? ";
            }

            if (!StringUtil.isNullOrEmpty(billId) && checkSOForCustomer) {  // Check any SO is created for Customer
                params.add(billId);
                conditionSQL += " and salesorder.id<>? ";
            }

                      
            if (!StringUtil.isNullOrEmpty(billDate)) {
                conditionSQL += " and (salesorder.orderdate =? )";
                params.add(df.parse(billDate));
            }

            if (isLeaseSO) {
                conditionSQL += " and salesorder.leaseOrMaintenanceSO=1 ";
            } else if (isConsignment) {
                conditionSQL += " and (salesorder.isconsignment='T'  and  salesorder.leaseOrMaintenanceSO=3) ";
            } else {
                conditionSQL += " and (salesorder.leaseOrMaintenanceSO=0 or salesorder.leaseOrMaintenanceSO=2)";
            }

            if (isMRPSalesOrder) {
                conditionSQL += " and  salesorder.ismrpsalesorder='T' ";
            } else {
                conditionSQL += " and  salesorder.ismrpsalesorder='F' ";
            }
            if (isJobWorkOrderReciever) {
                conditionSQL += " and  salesorder.isjobworkorder='T' ";
            } else {
                conditionSQL += " and  salesorder.isjobworkorder='F' ";
            }
                         
            /* SO must not be linked with DO & not closed manually also */
            if (bulkInv) {
                conditionSQL += " and salesorder.linkflag!=2 and salesorder.issoclosed='F' and  salesorder.isopen='T' ";
            }
           
            /* Only approved SO should be shown in report*/
            params.add(11);
            conditionSQL += " and salesorder.approvestatuslevel = ? ";
     
            if (isenableSalesPersonAgentFlow && !StringUtil.isNullOrEmpty(userID)) {//this block is executed only when owners restriction feature is on 
                String salesPersonID = (String) request.get("salesPersonid");
                String salespersonQuery = "";
                 if (!StringUtil.isNullOrEmpty(salesPersonID)) {
                   salesPersonID= AccountingManager.getFilterInString(salesPersonID);
                    salespersonQuery = "  salesorder.salesperson in " + salesPersonID + " or ";
                }
                
                conditionSQL += " and ( " + salespersonQuery + "  salesorder.createdby='" + userID + "' or salesorder.salesperson is null  ) ";
            }
            String mysqlQuery = " select DISTINCT salesorder.id, 'false' as withoutinventory  from salesorder "
                    + "inner join customer on customer.id = salesorder.customer "
                    + "left join repeatedsalesorders RSO on RSO.id = salesorder.repeateso "
                    +searchJoin + conditionSQL;
            
            list = executeSQLQuery( mysqlQuery, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                
                list = executeSQLQueryPaging( mysqlQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCustomerDAOImpl.getSalesOrders:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
   
    /**
     * Method is used to get the delivered quantity for the sales order 
     * @param salesOrderDetailID
     * @param companyID
     * @return
     * @throws ServiceException
     */
    public KwlReturnObject getDeliveredQuantityForSalesOrder(String salesOrderDetailID, String companyID) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        int quantityDigit = Constants.QUANTITY_DIGIT_AFTER_DECIMAL;
        if (Constants.CompanyPreferencePrecisionMap.containsKey(companyID)) {
            quantityDigit = (Integer) Constants.CompanyPreferencePrecisionMap.get(companyID).get(Constants.quantitydecimalforcompany);
        }
        
        String sqlQuery = "select IFNULL(sum(FORMAT(dod.baseuomdeliveredquantity,"+quantityDigit+")),0) as deliveredQty from "
                + "dodetails dod,sodetails sod,deliveryorder do "
                + "where sod.id = dod.sodetails and do.id = dod.deliveryorder and do.deleteflag = 'F' and dod.company = ? and sod.id = ? having deliveredQty <> 0 "
                + "union all"
                + " select IFNULL(sum(FORMAT(dod.baseuomdeliveredquantity,"+quantityDigit+")),0) as deliveredQty "
                + "from dodetails dod,invoicedetails invd,deliveryorder do,sodetails sod "
                + "where do.id = dod.deliveryorder and sod.id = invd.salesorderdetail and do.deleteflag = 'F' and dod.company = ? and sod.id = ? having deliveredQty <> 0";

        list = executeSQLQuery(sqlQuery, new Object[]{companyID, salesOrderDetailID, companyID, salesOrderDetailID});
        if (list != null) {
            count = list.size();
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    
  
      
    /*----Return List of id of SO linked with Quotation----*/
    public List getSalesOrderLinkedWithQuotation(String linkedid) throws ServiceException {
        ArrayList paramslist = new ArrayList();
        List list = new ArrayList();
        List returnList = new ArrayList();
        String salesOrderIdsList = "";

        try {
            String selQuery = "select sol.DocID.ID from SalesOrderLinking sol  WHERE sol.LinkedDocID= ?";
            paramslist.add(linkedid);
            list = executeQuery(selQuery, paramslist.toArray());
            if (list.size() > 0) {

                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    String salesOrderId = itr.next().toString();
                    salesOrderIdsList += salesOrderId + ",";

                }
            }
            if (!StringUtil.isNullOrEmpty(salesOrderIdsList)) {
                salesOrderIdsList = salesOrderIdsList.substring(0, salesOrderIdsList.length() - 1);
            }

            returnList.add(salesOrderIdsList);

        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return returnList;
    }
    
    /*----Return List of id of DO linked with Invoice/SO----*/
    public List getDeliveryOrderLinkedWithSourceDocument(String linkedid) throws ServiceException {
        ArrayList paramslist = new ArrayList();
        List list = new ArrayList();
        List returnList = new ArrayList();
        String deliveryOrderIdList = "";

        try {
            String selQuery = "select dol.DocID.ID from DeliveryOrderLinking dol  WHERE dol.LinkedDocID= ?";
            paramslist.add(linkedid);
            list = executeQuery(selQuery, paramslist.toArray());
            if (list.size() > 0) {

                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    String salesOrderId = itr.next().toString();
                    deliveryOrderIdList += salesOrderId + ",";

                }
            }
            if (!StringUtil.isNullOrEmpty(deliveryOrderIdList)) {
                deliveryOrderIdList = deliveryOrderIdList.substring(0, deliveryOrderIdList.length() - 1);
            }

            returnList.add(deliveryOrderIdList);

        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return returnList;
    }
    
        
     /*----Return List of id of Invoice linked with CQ/DO/SO----*/
    public List getInvoiceLinkedWithSourceDocument(String linkedid) throws ServiceException {
        ArrayList paramslist = new ArrayList();
        List list = new ArrayList();
        List returnList = new ArrayList();
        String invoiceIdList = "";

        try {
            String selQuery = "select invl.DocID.ID from InvoiceLinking invl  WHERE invl.LinkedDocID= ?";
            paramslist.add(linkedid);
            list = executeQuery(selQuery, paramslist.toArray());
            if (list.size() > 0) {

                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    String salesOrderId = itr.next().toString();
                    invoiceIdList += salesOrderId + ",";

                }
            }

            if (!StringUtil.isNullOrEmpty(invoiceIdList)) {
                invoiceIdList = invoiceIdList.substring(0, invoiceIdList.length() - 1);
            }

            returnList.add(invoiceIdList);

        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return returnList;
    }
   
    public KwlReturnObject saveContractService(HashMap<String, Object> hashMap) throws ServiceException {
        ArrayList paramslist = new ArrayList();
        List list = new ArrayList();
        int updatecount=0;
        try {
            String selQuery = "update assetmaintenancescheduler set startdate=?,enddate=? where id=? and company=?";
            paramslist.add((Date)hashMap.get("startdate"));
            paramslist.add((Date)hashMap.get("enddate"));
            paramslist.add(hashMap.get("activityid").toString());
            paramslist.add(hashMap.get(Constants.companyKey).toString());
            updatecount=executeSQLUpdate(selQuery, paramslist.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, updatecount);
    }
    
    @Override
    public KwlReturnObject getMappedFilesResult(Map<String, Object> requestParams) throws ServiceException {
         List list = null;
        try {
            String condition = "";
            ArrayList params = new ArrayList();
            String companyid = (String) requestParams.get("companyid");
            params.add(companyid);
            
            if (requestParams.containsKey("id") && requestParams.get("id") != null) {
                condition += " and id = ? ";
                params.add((String) requestParams.get("id"));
            }
            
            String selQuery = "select id, documentid from invoicedoccompmaptemporary where company = ?" + condition;
            list = executeSQLQuery(selQuery, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getMappedFilesResult : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject SaveUpdateObject(Object object) throws ServiceException {
        List list = new ArrayList();
        try {
            if (object != null) {
                saveOrUpdate(object);
                list.add(object);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("UpdateObject : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public void deleteTemporaryMappedFiles(String savedFilesMappingId, String companyid) throws ServiceException {
        int deletedRecords=0;
        List params = new ArrayList();
        try {
            if (!StringUtil.isNullOrEmpty(companyid) && !StringUtil.isNullOrEmpty(savedFilesMappingId)) {
                params.add(savedFilesMappingId);
                params.add(companyid);
                String query = "delete from invoicedoccompmaptemporary where id = ? and company = ? ";
                deletedRecords = executeSQLUpdate(query,params.toArray());
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteTemporaryMappedFiles", ex);
        }
    }
    @Override
    public int savePurchaseOrderStatusForSO(JSONObject params) throws ServiceException {
        String query = "";
        String purchaseOrderId = params.optString("purchaseOrderID", "");
        String status = params.optString("status", "closed");
        int updatedRecordCnt = 0;
        List queryParams = new ArrayList();
        try {
            if (!StringUtil.isNullOrEmpty(purchaseOrderId)) {
                query += "update purchaseorder set disabledpoforso = ? where id = ? ";
                if(status.equalsIgnoreCase("open")){
                    queryParams.add("T");
                }else{
                    queryParams.add("F");
                }
                queryParams.add(purchaseOrderId);
                updatedRecordCnt = executeSQLUpdate(query, queryParams.toArray());
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("savePurchaseOrderStatusForSO", ex);
        }
        return updatedRecordCnt;
    }
    
    @Override
    public KwlReturnObject getLinkedPO(JSONObject request) throws ServiceException {
        List list = null;
        int count = 0;
        ArrayList paramList = new ArrayList();
        try {
            String soId = request.optString("docid","");
            String query = "select linkeddocid from solinking where moduleid=18 and docid=?";
            paramList.add(soId);
            list = executeSQLQuery(query, paramList.toArray());
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getLinkedPO:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject getLinkedDocByModuleId(JSONObject paramJobj) throws ServiceException {
        List list = null;
        int count = 0;
        String condition = "";
        ArrayList paramList = new ArrayList();
        try {
            String soId = paramJobj.optString("docid", "");
            String moduleid = paramJobj.optString("moduleid", "");
            boolean excludeTempDeletedReceipt = paramJobj.optBoolean("excludeTempDeletedReceipt", false);
            String query = "select distinct ad.id, ad.amountdue, ad.amount,re.id,re.receiptnumber,ad.receipt from receiptadvancedetail ad "
                    + "inner join receipt re on re.id = ad.receipt "
                    + "where  ad.id in ( select linkeddocid from solinking where docid in ("+ soId +") and moduleid = ? ) ";
            paramList.add(moduleid);
            if(excludeTempDeletedReceipt){
                condition += " and re.deleteflag = 'F' ";
            }
            condition += "  order by re.createdon asc ";
            if (!StringUtil.isNullOrEmpty(condition)) {
                query += condition;
            }
            list = executeSQLQuery(query, paramList.toArray());
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.getLinkedDocByModuleId:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject checklinkingofTransactions(String moduleid, String billids) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            ArrayList params = new ArrayList();
            billids = AccountingManager.getFilterInString(billids);
            params.add(moduleid);
            String mysqlQuery = "select sourcetransactionid,sourcemodule,destinationtransactionid,destinationmodule from groupcompany_transactionmapping where destinationtransactionid IN " + billids + " and sourcemodule=? and sourcetransactionid IS NOT  NULL and sourcetransactionid!=''";

            list = executeSQLQuery(mysqlQuery, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderImpl.checklinkingofTransactions:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
      @Override
    public KwlReturnObject checkEntryForReceiptInLinking(String moduleName, String docid) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(docid);
//        String hqlQuery = "from "+moduleName+"Linking as linkmodinfo where linkmodinfo.DocID.ID=? and (linkmodinfo.SourceFlag=1";
//        if(moduleName==Constants.Acc_GoodsReceipt_modulename || moduleName==Constants.Acc_PurchaseInvoice_modulename){
//            hqlQuery+= " and moduleid="+ Constants.Acc_Vendor_Invoice_ModuleId +" or (linkmodinfo.SourceFlag=1 and moduleid="+Constants.Acc_Vendor_Invoice_ModuleId+")";
//        }
//        hqlQuery+= ")";
         String hqlQuery = "from "+moduleName+"Linking as linkmodinfo where linkmodinfo.DocID.ID=? and (linkmodinfo.SourceFlag=1  and moduleid="+ Constants.Acc_Receive_Payment_ModuleId+")";
        
        list = executeQuery( hqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
}
 

        /*@Override
    public List getSalesReturnVatDetails(String companyid) {
        List list = null;
        String query = "SELECT acc.`name`,its.account,srtm.percentage,srtm.assessablevalue  FROM srdetails srd INNER JOIN salesreturndetailtermmap srtm ON srtm.salesreturndetail = srd.id "
                + "INNER JOIN invoicetermssales its ON srtm.term=its.id INNER JOIN account acc ON its.account = acc.id "
                + " WHERE  srd.company =? AND (its.termtype = 1 OR its.termtype = 3)  ORDER BY its.account ;";

        try {
            list = executeSQLQuery(query, new Object[]{companyid});

        } catch (ServiceException ex) {
            Logger.getLogger(accSalesOrderImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }*/
