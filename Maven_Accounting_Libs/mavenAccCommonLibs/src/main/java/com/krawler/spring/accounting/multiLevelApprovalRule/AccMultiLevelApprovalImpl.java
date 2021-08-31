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
package com.krawler.spring.accounting.multiLevelApprovalRule;

import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.KwlReturnObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author sagar
 */
public class AccMultiLevelApprovalImpl extends BaseDAO implements AccMultiLevelApprovalDAO {

    public KwlReturnObject saveMultiApprovalRule(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String companid = dataMap.get("companyid").toString();
            String level = dataMap.get("level").toString();
            String rule = dataMap.get("rule").toString();
            int moduleid = Integer.parseInt(dataMap.get("moduleid").toString());
            
            String flowid = dataMap.get("ruleid")!=null?dataMap.get("ruleid").toString():"";
            boolean isEdit=!StringUtil.isNullOrEmpty(flowid);
            if(StringUtil.isNullOrEmpty(flowid)){
                flowid = UUID.randomUUID().toString();
            }
            boolean hasapprover = Boolean.parseBoolean(dataMap.get("hasapprover").toString());
            char deptwiseapprover = (char)dataMap.get("deptwiseapprover");
            int appliedupon = Integer.parseInt(dataMap.get("appliedupon").toString());
            String creator = dataMap.get("creator").toString();
            String discountrule = dataMap.get("discountrule").toString();
            ArrayList params = new ArrayList();
            String query = "";
            if(isEdit){
                params.add(level);
                params.add(rule);
                params.add(hasapprover);
                params.add(appliedupon);
                params.add(creator);
                params.add(discountrule);
                params.add(deptwiseapprover);
                params.add(flowid);
                query = "UPDATE multilevelapprovalrule SET level=?, rule=?, hasapprover=?, appliedupon=?, creator=?, discountrule=? ,deptwiseapprover=? WHERE id=?";
                // Delete Approval Rule-Approver mapping
                String subQuery = "DELETE FROM multilevelapprovalruletargetusers WHERE ruleid=?";
                executeSQLUpdate(subQuery, new Object[]{flowid});
            }else{
                params.add(flowid);
                params.add(level);
                params.add(rule);
                params.add(companid);
                params.add(hasapprover);
                params.add(moduleid);
                params.add(appliedupon);
                params.add(creator);
                params.add(discountrule);
                params.add(deptwiseapprover);
                query = "insert into multilevelapprovalrule(id,level,rule,companyid,hasapprover, moduleid ,appliedupon, creator, discountrule,deptwiseapprover ) values(?,?,?,?,?,?,?,?,?,?)";
            }
            
            executeSQLUpdate(query, params.toArray());
            if (hasapprover) {
                String approver = dataMap.get("approver").toString();
                if (!StringUtil.isNullOrEmpty(approver)) {
                    String subQuery = "insert into multilevelapprovalruletargetusers(id,ruleid,userid) value (?,?,?)";
                    String[] approverArr = approver.split(",");
                    for (int cnt = 0; cnt < approverArr.length; cnt++) {
                        executeSQLUpdate(subQuery, new Object[]{UUID.randomUUID().toString(), flowid, approverArr[cnt]});
                    }
                }
            }
            list.add(flowid);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveMultiApprovalRule : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject deleteMultiApprovalRule(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String flowid = dataMap.get("id").toString();
            ArrayList params = new ArrayList();
            params.add(flowid);
            String query = "delete from multilevelapprovalrule where id = ?";
            executeSQLUpdate(query, params.toArray());
            list.add(flowid);
            String subQuery = "delete from multilevelapprovalruletargetusers where ruleid=?";
            executeSQLUpdate(subQuery, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteMultiApprovalRule : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject getMultiApprovalRuleData(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        int count=0;
        boolean isPaging=false;
        try {
            ArrayList params = new ArrayList();
            int start=0;
            int limit=0;
            String companyid = dataMap.get("companyid").toString();
            if (dataMap.containsKey("start") && dataMap.containsKey("limit")) {
                start = Integer.parseInt(dataMap.get("start").toString());
                limit = Integer.parseInt(dataMap.get("limit").toString());
                isPaging=true;
            }
            String condition = " where companyid = ?";
            params.add(companyid);
            if (dataMap.containsKey("id")) {
                condition += " and id = ?";
                params.add(dataMap.get("id"));
            }
            if (dataMap.containsKey("level")) {
                condition += " and level = ?";
                params.add(dataMap.get("level"));
            }
            if (dataMap.containsKey("moduleid")) {
                condition += " and moduleid = ?";
                params.add(dataMap.get("moduleid"));
            }
            if (dataMap.containsKey("appliedupon") && dataMap.get("appliedupon")!=null) {
                condition += " and appliedupon = ?";
                params.add(dataMap.get("appliedupon"));
            }

            String query = "select id, level, rule, hasapprover, moduleid, appliedupon, creator, discountrule,deptwiseapprover from multilevelapprovalrule " + condition;
            query += " order by level desc";
            list = executeSQLQuery(query, params.toArray());
            count=list.size();
            if (isPaging) {
                list = executeSQLQueryPaging(query, params.toArray(), new Integer[]{start, limit});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getMultiApprovalRuleData : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }
    
    public KwlReturnObject getApprovalRuleTargetUsers(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        String ruleID = "", userDepartment = "";
        try {
            String userDepartmentQuery = "";

            if (dataMap.containsKey("ruleid") && dataMap.get("ruleid") != null) {
                ruleID = (String) dataMap.get("ruleid");
                params.add(ruleID);
            }
            if (dataMap.containsKey("userdepartment") && dataMap.get("userdepartment") != null) {
                userDepartmentQuery = " and u.department=?";
                userDepartment = (String) dataMap.get("userdepartment");
                params.add(userDepartment);
            }
//            String query = "select userid,fname,lname, emailid from users where userid in (select userid from multilevelapprovalruletargetusers where ruleid = ?)";
            String query = "select u.userid,u.fname,u.lname, u.emailid from users u inner join multilevelapprovalruletargetusers mlu on u.userid=mlu.userid where mlu.ruleid = ?" + userDepartmentQuery;
            list = executeSQLQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getApprovalRuleTargetUsers : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public boolean checkIfTransactionIsPendingForApproval(int moduleId, int level,String companyid) throws ServiceException {
        boolean isRecordExists=false;
        KwlReturnObject ruleResult=null;
        if ((moduleId == Constants.Acc_Goods_Receipt_ModuleId)) {
            ruleResult = getGRExistsPendingAtLevel(level,companyid);
        } else if ((moduleId == Constants.Acc_Delivery_Order_ModuleId)) {
            ruleResult = getDOExistsPendingAtLevel(level,companyid);
        } else if ((moduleId == Constants.Acc_GENERAL_LEDGER_ModuleId)) {
            ruleResult = getJEExistsPendingAtLevel(level,companyid);
        } else if ((moduleId == Constants.Acc_Customer_Quotation_ModuleId)) {
            ruleResult = getCQExistsPendingAtLevel(level,companyid);
        } else if ((moduleId == Constants.Acc_Vendor_Quotation_ModuleId)) {
            ruleResult = getVQExistsPendingAtLevel(level,companyid);
        } else if ((moduleId == Constants.Acc_Purchase_Requisition_ModuleId)) {
            ruleResult = getPRExistsPendingAtLevel(level,companyid);
        } else if ((moduleId == Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId)) {
            ruleResult = getFixedAssetPRExistsPendingAtLevel(level,companyid);
        } else if ((moduleId == Constants.Acc_Sales_Order_ModuleId)) {
            ruleResult = getSOExistsPendingAtLevel(level,companyid);
        } else if ((moduleId == Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId)) {
            ruleResult = getFixedAssetVQExistsPendingAtLevel(level,companyid);
        } else if ((moduleId == Constants.Acc_Purchase_Order_ModuleId)) {
            ruleResult = getPOExistsPendingAtLevel(level,companyid);
        } else if ((moduleId == Constants.Acc_FixedAssets_Purchase_Order_ModuleId)) {
            ruleResult = getFixedAssetPOExistsPendingAtLevel(level,companyid);
        } else if ((moduleId == Constants.Acc_Invoice_ModuleId)) {
            ruleResult = getCustomerInvoiceExistsPendingAtLevel(level,companyid);
        } else if ((moduleId == Constants.Acc_Vendor_Invoice_ModuleId)) {
            ruleResult = getVendorInvoiceExistsPendingAtLevel(level,companyid);
        } else if ((moduleId == Constants.Acc_Credit_Note_ModuleId)) {
            ruleResult = getCreditNoteExistsPendingAtLevel(level,companyid);
        } else if ((moduleId == Constants.Acc_Debit_Note_ModuleId)) {
            ruleResult = getDebitNoteExistsPendingAtLevel(level,companyid);        
        } else if ((moduleId == Constants.Acc_Make_Payment_ModuleId)) {
            ruleResult = getMakePaymentExistsPendingAtLevel(level,companyid);        
        } else if ((moduleId == Constants.Acc_Receive_Payment_ModuleId)) {
            ruleResult = getReceivePaymentExistsPendingAtLevel(level,companyid);
        }
        if(ruleResult!=null){
            if(ruleResult.getEntityList().size()!=0){
                isRecordExists=true;
            }
        }
        return isRecordExists;
    }
    public KwlReturnObject checkDepartmentWiseApprover(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        String ruleid = "", companyid = "";
        String buildQuery = "";
        boolean checkDeptWiseApprover=false;
        try {
            if (dataMap.containsKey("checkdeptwiseapprover") && dataMap.get("checkdeptwiseapprover") != null) {
                checkDeptWiseApprover = Boolean.parseBoolean(dataMap.get("checkdeptwiseapprover").toString());
                if (checkDeptWiseApprover) {
                    buildQuery = " deptwiseapprover='T'";
                } else {
                    buildQuery = " deptwiseapprover='F'";
                }
            } else {
                buildQuery = " deptwiseapprover='F'";
            }
            if (dataMap.containsKey("companyid") && dataMap.get("companyid") != null) {
                companyid = (String) dataMap.get("companyid");
                params.add(companyid);
                buildQuery = buildQuery + " and companyid=?";
            }

            if (dataMap.containsKey("ruleid") && dataMap.get("ruleid") != null) {
                ruleid = (String) dataMap.get("ruleid");
                params.add(ruleid);
                buildQuery = buildQuery + " and id=?";
            }

            String query = "select deptwiseapprover from multilevelapprovalrule where " + buildQuery;
            list = executeSQLQuery(query, params.toArray());

        } catch (Exception ex) {
            throw ServiceException.FAILURE("getRequisitionFlowTargetUsers : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    public KwlReturnObject getGRExistsPendingAtLevel(int level,String companyId) throws ServiceException{
        List list = new ArrayList();
        String query= "Select GRO.ID From GoodsReceiptOrder GRO where GRO.approvestatuslevel = ? and GRO.deleted = False and GRO.isconsignment='F' and GRO.company.companyID=?";
        list = executeQuery(query, new Object[]{level,companyId});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public KwlReturnObject getDOExistsPendingAtLevel(int level,String companyId) throws ServiceException{
        List list = new ArrayList();
        String query= "Select DO.ID From DeliveryOrder DO where DO.approvestatuslevel = ? and DO.deleted = False and DO.company.companyID=?";
        list = executeQuery(query, new Object[]{level,companyId});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public KwlReturnObject getJEExistsPendingAtLevel(int level,String companyId) throws ServiceException{
        List list = new ArrayList();
        String query= "Select JE.ID From JournalEntry JE where JE.approvestatuslevel = ? and JE.deleted = False and JE.company.companyID=?";
        list = executeQuery(query, new Object[]{level,companyId});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public KwlReturnObject getCQExistsPendingAtLevel(int level,String companyId) throws ServiceException{
        List list = new ArrayList();
        String query= "Select CQ.ID From Quotation CQ where CQ.approvestatuslevel = ? and CQ.deleted = False and CQ.company.companyID=?";
        list = executeQuery(query, new Object[]{level,companyId});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public KwlReturnObject getVQExistsPendingAtLevel(int level,String companyId) throws ServiceException{
        List list = new ArrayList();
        String query= "Select VQ.ID From VendorQuotation VQ where VQ.approvestatuslevel = ? and VQ.fixedAssetVQ=false and VQ.deleted = False and VQ.company.companyID=?";
        list = executeQuery(query, new Object[]{level,companyId});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public KwlReturnObject getPRExistsPendingAtLevel(int level,String companyId) throws ServiceException{
        List list = new ArrayList();
        String query= "Select PR.ID From PurchaseRequisition PR where PR.approvestatuslevel = ? and PR.fixedAssetPurchaseRequisition = false and PR.deleted = False and PR.company.companyID=?";
        list = executeQuery(query, new Object[]{level,companyId});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public KwlReturnObject getFixedAssetPRExistsPendingAtLevel(int level,String companyId) throws ServiceException{
        List list = new ArrayList();
        String query= "Select PR.ID From PurchaseRequisition PR where PR.approvestatuslevel = ? and PR.fixedAssetPurchaseRequisition = true and PR.deleted = False and PR.company.companyID=?";
        list = executeQuery(query, new Object[]{level,companyId});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public KwlReturnObject getSOExistsPendingAtLevel(int level,String companyId) throws ServiceException{
        List list = new ArrayList();
        String query= "Select SO.ID From SalesOrder SO where SO.approvestatuslevel = ? and SO.deleted = False and SO.company.companyID=?";
        list = executeQuery(query, new Object[]{level,companyId});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public KwlReturnObject getFixedAssetVQExistsPendingAtLevel(int level,String companyId) throws ServiceException{
        List list = new ArrayList();
        String query= "Select VQ.ID From VendorQuotation VQ where VQ.approvestatuslevel = ? and VQ.fixedAssetVQ = true and VQ.deleted = False and VQ.company.companyID=?";
        list = executeQuery(query, new Object[]{level,companyId});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public KwlReturnObject getPOExistsPendingAtLevel(int level,String companyId) throws ServiceException{
        List list = new ArrayList();
        String query= "Select PO.ID From PurchaseOrder PO where PO.approvestatuslevel = ? and PO.fixedAssetPO = false and PO.deleted = False and PO.company.companyID=?";
        list = executeQuery(query, new Object[]{level,companyId});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public KwlReturnObject getFixedAssetPOExistsPendingAtLevel(int level,String companyId) throws ServiceException{
        List list = new ArrayList();
        String query= "Select PO.ID From PurchaseOrder PO where PO.approvestatuslevel = ? and PO.fixedAssetPO = true and PO.deleted = False and PO.company.companyID=?";
        list = executeQuery(query, new Object[]{level,companyId});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public KwlReturnObject getCustomerInvoiceExistsPendingAtLevel(int level,String companyId) throws ServiceException{
        List list = new ArrayList();
        String query= "Select I.ID From Invoice I where I.approvestatuslevel = ? and I.fixedAssetInvoice = false and I.fixedAssetLeaseInvoice=false and I.isconsignment='F' and I.deleted = false and I.company.companyID=?";
        list = executeQuery(query, new Object[]{level,companyId});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public KwlReturnObject getVendorInvoiceExistsPendingAtLevel(int level,String companyId) throws ServiceException{
        List list = new ArrayList();
        String query= "Select GR.ID From GoodsReceipt GR where GR.approvestatuslevel = ? and GR.isconsignment='F' and GR.fixedAssetInvoice=false and GR.deleted = false and GR.company.companyID=? and GR.istemplate != 2 ";
        list = executeQuery(query, new Object[]{level,companyId});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public KwlReturnObject getCreditNoteExistsPendingAtLevel(int level,String companyId) throws ServiceException{
        List list = new ArrayList();
        String query= "Select CN.ID From CreditNote CN where CN.approvestatuslevel = ? and CN.isOpeningBalenceCN=false and CN.deleted = false and CN.company.companyID=? ";
        list = executeQuery(query, new Object[]{level,companyId});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public KwlReturnObject getDebitNoteExistsPendingAtLevel(int level,String companyId) throws ServiceException{
        List list = new ArrayList();
        String query= "Select DN.ID From DebitNote DN where DN.approvestatuslevel = ? and DN.isOpeningBalenceDN=false and DN.deleted = false and DN.company.companyID=? ";
        list = executeQuery(query, new Object[]{level,companyId});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public KwlReturnObject getMakePaymentExistsPendingAtLevel(int level,String companyId) throws ServiceException{
        List list = new ArrayList();
        String query= "Select py.ID From Payment py where py.approvestatuslevel = ? and py.isOpeningBalencePayment=false and py.deleted = false and py.company.companyID=? ";
        list = executeQuery(query, new Object[]{level,companyId});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public KwlReturnObject getReceivePaymentExistsPendingAtLevel(int level,String companyId) throws ServiceException{
        List list = new ArrayList();
        String query= "Select rp.ID From Receipt rp where rp.approvestatuslevel = ? and rp.isOpeningBalenceReceipt=false and rp.deleted = false and rp.company.companyID=? ";
        list = executeQuery(query, new Object[]{level,companyId});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
}
