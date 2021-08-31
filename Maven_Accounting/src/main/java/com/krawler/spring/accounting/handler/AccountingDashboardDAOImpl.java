/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.handler;

import com.krawler.common.admin.User;
import com.krawler.common.admin.UserPreferences;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class AccountingDashboardDAOImpl extends BaseDAO implements AccountingDashboardDAO {

    @Override
    public KwlReturnObject insertWidgetIntoState(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = new ArrayList();
        int dl = 0;
        try {
            String userId = requestParams.get("userid").toString();
            String wid = requestParams.get("wid").toString();
            String colno = requestParams.get("colno").toString();
            String columnToUpdate = "col" + colno;
            widgetManagement wmObj = null;
            String query = "FROM widgetManagement wm WHERE wm.user.userID =?";
            List lst = executeQuery(query, new Object[]{userId});
            Iterator ite = lst.iterator();
            while (ite.hasNext()) {
                wmObj = (widgetManagement) ite.next();
            }
            JSONObject jobj = new JSONObject(wmObj.getWidgetstate());
            JSONObject check = getColumnPositionInWidgetState(jobj, wid);
            if (!check.getBoolean("present")) {
                JSONObject empty = new JSONObject();
                empty.put("id", wid);
                jobj.append(columnToUpdate, empty);
                wmObj.setWidgetstate(jobj.toString());
            }
            saveOrUpdate(wmObj);
            ll.add(wmObj);
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("AccountingDashboardDAOImpl.insertWidgetIntoState : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "002", "", ll, dl);
    }

    @Override
    public KwlReturnObject removeWidgetFromState(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = new ArrayList();
        int dl = 0;
        try {
            String wid = requestParams.get("wid").toString();
            String userId = requestParams.get("userid").toString();
            widgetManagement wmObj = new widgetManagement();
            JSONObject empty = new JSONObject();
            String query = "FROM widgetManagement wm WHERE wm.user.userID =?";
            List lst = executeQuery(query, new Object[]{userId});
            Iterator ite = lst.iterator();
            while (ite.hasNext()) {
                wmObj = (widgetManagement) ite.next();
                empty = new JSONObject(wmObj.getWidgetstate());
            }
            JSONObject _state = getColumnPositionInWidgetState(empty, wid);
            String column = _state.getString("column");
            JSONObject jobj = deleteFromWidgetStateJson(empty, "id", wid, column);

            // update WidgetManagement Table
            wmObj.setWidgetstate(jobj.toString());

            saveOrUpdate(wmObj);
            ll.add(wmObj);
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("AccountingDashboardDAOImpl.removeWidgetFromState : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "002", "", ll, dl);
    }

    @Override
    public KwlReturnObject changeWidgetState(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = new ArrayList();
        int dl = 0;
        try {
            String wid = requestParams.get("wid").toString();
            String userId = requestParams.get("userid").toString();
            String colno = requestParams.get("colno").toString();
            int position = Integer.parseInt(requestParams.get("position").toString());
            String columnToEdit = "col" + colno;

            widgetManagement wmObj = null;
            String query = "FROM widgetManagement wm WHERE wm.user.userID =?";
            List lst = executeQuery(query, new Object[]{userId});
            Iterator ite = lst.iterator();
            while (ite.hasNext()) {
                wmObj = (widgetManagement) ite.next();
            }

            JSONObject jobj = new JSONObject(wmObj.getWidgetstate());
            JSONObject previous_details = getColumnPositionInWidgetState(jobj, wid);

            String pre_column = previous_details.getString("column");
            int pre_position = previous_details.getInt("position");
            if (pre_position < position && columnToEdit.equals(pre_column)) {
                position--;
            }
            jobj = deleteFromWidgetStateJson(jobj, "id", wid, pre_column);
            com.krawler.utils.json.base.JSONArray jobj_col = jobj.getJSONArray(columnToEdit);
            JSONObject empty = new JSONObject();
            empty.put("id", wid);
            jobj_col = insertIntoJsonArray(jobj_col, position, empty);
            jobj.put(columnToEdit, jobj_col);

            wmObj.setWidgetstate(jobj.toString());
            saveOrUpdate(wmObj);

            ll.add(wmObj);
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("AccountingDashboardDAOImpl.changeWidgetState : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "002", "", ll, dl);
    }

    private JSONArray insertIntoJsonArray(JSONArray jArr, int position, JSONObject newjObj) throws JSONException {
        JSONArray toReturn = new JSONArray();
        Boolean added = false;
        for (int i = 0; i < jArr.length(); i++) {
            if (i == position) {
                toReturn.put(newjObj);
                added = true;

            }
            JSONObject empty = jArr.getJSONObject(i);
            toReturn.put(empty);
        }
        if (!added) {
            toReturn.put(newjObj);
        }
        return toReturn;
    }

    private JSONObject getColumnPositionInWidgetState(JSONObject jobj, String wid) throws JSONException {
        JSONObject toReturn = new JSONObject();
        for (int i = 1; i <= 3; i++) {
            String column = "col" + String.valueOf(i);
            com.krawler.utils.json.base.JSONArray jArr = jobj.getJSONArray(column);
            for (int j = 0; j < jArr.length(); j++) {
                JSONObject empty = jArr.getJSONObject(j);
                if (empty.get("id").toString().equals(wid)) {
                    toReturn.put("column", column);
                    toReturn.put("position", j);
                    toReturn.put("present", true);
                    return toReturn;
                }
            }
        }
        toReturn.put("present", false);
        return toReturn;
    }

    private JSONObject deleteFromWidgetStateJson(JSONObject jobj, String key, String value, String column) throws JSONException {
        com.krawler.utils.json.base.JSONArray jobj_col = deleteFromWidgetStateJsonArray(jobj.getJSONArray(column), key, value);
        jobj.put(column, jobj_col);
        return jobj;
    }

    private com.krawler.utils.json.base.JSONArray deleteFromWidgetStateJsonArray(com.krawler.utils.json.base.JSONArray jArr, String key, String value) throws JSONException {
        com.krawler.utils.json.base.JSONArray toReturn = new com.krawler.utils.json.base.JSONArray();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject empty = jArr.getJSONObject(i);
            if (!empty.get("id").toString().equals(value)) {
                toReturn.put(empty);
            }
        }
        return toReturn;
    }

    @Override
    public KwlReturnObject getWidgetStatus(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = new ArrayList();
        int dl = 0;
        try {
            JSONObject empty = new JSONObject();
            String userId = requestParams.get("userid").toString();
            User userObj = (User) get(User.class, userId);
            String roleid = requestParams.get("roleid").toString();
            String query = "FROM widgetManagement as wm WHERE wm.user = ?";
            List lst = executeQuery(query, new Object[]{userObj});
            if (lst == null || lst.isEmpty()) {
                empty = insertDefaultWidgetState(roleid);
                widgetManagement wmObj = new widgetManagement();
                wmObj.setWidgetstate(empty.toString());
                wmObj.setUser(userObj);
                wmObj.setModifiedon(new Date());
                save(wmObj);
                ll.add(wmObj);
                dl = ll.size();
            } else {
                query = "FROM widgetManagement as wm WHERE wm.user = ?";
                ll = executeQuery(query, new Object[]{userObj});
                dl = ll.size();
            }
        } catch (ServiceException e) {
            throw ServiceException.FAILURE("AccountingDashboardDAOImpl.getWidgetStatus : " + e.getMessage(), e);
        } catch (Exception e) {
            throw ServiceException.FAILURE("AccountingDashboardDAOImpl.getWidgetStatus : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "002", "", ll, dl);
    }

    public static JSONObject insertDefaultWidgetState(String roleid) throws ServiceException {
        JSONObject jobj = null;
        try {
            String col1 = "";
            String col2 = "";
            String col3 = "";
            col2 = "{'id':'purchasemgntwidget_drag'},{'id':'masterswidget_drag'}";
            col1 = "{'id':'salesbillingwidget_drag'},{'id':'financialstmtwidget_drag'},{'id':'accountmgntwidget_drag'}";
            col3 = "{'id':'transactionreportwidget_drag'},{'id':'updateswidget_drag'}";
            jobj = new JSONObject("{'col1':" + (!col1.equals("") ? "[" + col1 + "]" : "[]") + ",'col2':" + (!col2.equals("") ? "[" + col2 + "]" : "[]") + ",'col3':" + (!col3.equals("") ? "[" + col3 + "]" : "[]") + "}");
        } catch (JSONException e) {
            throw ServiceException.FAILURE("AccountingDashboardDAOImpl.insertDefaultWidgetState : " + e.getMessage(), e);
        }
        return jobj;
    }
        /**
     *
     * @param *requestParams*
     * @return = Return KwlReturnObject with data
     * @throws ServiceException
     */    
    @Override
    public KwlReturnObject getPendingApprovalDetails(HashMap<String, Object> requestParams) throws ServiceException { //function to check delivery order used in sales return
        List returnlist = new ArrayList();
        ArrayList params = new ArrayList();
        int count = 0;
        String condition = "";
        try {
            String companyId = (String) requestParams.get("companyid");
            String ss = (String) requestParams.get(Constants.ss);
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String startDate = (String) requestParams.get(Constants.REQ_startdate);
            String endDate = (String) requestParams.get(Constants.REQ_enddate);
            boolean dateFilterActive = false;
            /*
             * excludeRejectedRecords is used for checking pending documents before form 03 generation
             * On rejection of any document, it is deleted temporarily.
             */ 
            boolean excludeRejectedRecords = requestParams.get("excludeRejectedRecords")!=null?Boolean.parseBoolean((String)requestParams.get("excludeRejectedRecords")) :false;
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                dateFilterActive = true;
            }
            String start = "";
            String limit = "";
            if (requestParams.containsKey(Constants.start) && requestParams.get(Constants.start) != null && requestParams.containsKey(Constants.limit) && requestParams.get(Constants.limit) != null) {
                start = (String) requestParams.get(Constants.start);
                limit = (String) requestParams.get(Constants.limit);
            }
            if (!StringUtil.isNullOrEmpty(ss)) {
                condition = "'%"+ss+"%'";
            }

            String query = "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                query = "SELECT * FROM (";
            }
            String userDepartment = "";
            if (requestParams.containsKey("userDepartment") && requestParams.get("userDepartment") != null) {
                userDepartment = (String) requestParams.get("userDepartment");
            }
            //Sales Invoice Module
            /**
             * Getting customer from for which SO is created but as union is applied on all the query so appended column in all the select.ERP-38444
             */
            query += "select inv.id, inv.deleteflag, inv.invoiceNumber as number , (SELECT entryno from journalentry where id=inv.journalentry) as jeno, inv.createdon as date, 'Sales Invoices' as module, '2' as moduleid, inv.customer as person  from invoice inv inner join journalentry je on inv.journalentry=je.id ";
            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                query += " inner join users on users.userid = inv.createdby and users.department = ?";
                params.add(userDepartment);
            }
            query += " where inv.pendingapproval='1'  and inv.company=?";
            params.add(companyId);
            if(dateFilterActive){
                query += " and je.entrydate >= ? and je.entrydate <= ? ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if(excludeRejectedRecords){
                query += " and inv.deleteflag = 'F' ";
            }
            query += " union ";

            //JE Module
            query += "select je.id, je.deleteflag, je.entryno as number , je.entryno as jeno, je.createdon as date, 'Journal Entry' as module, '24' as moduleid, '' as person from journalentry je ";
            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                query += " inner join users on users.userid = je.createdby and users.department = ?";
                params.add(userDepartment);
            }
            query += "WHERE ( je.approvestatuslevel BETWEEN 1 AND 10 or je.approvestatuslevel<0 ) and je.entryno!= '' and je.company=? "; 
            params.add(companyId);
            if(dateFilterActive){
                query += " and je.entrydate >= ? and je.entrydate <= ? ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if(excludeRejectedRecords){
                query += " and je.deleteflag = 'F' ";
            }
            query += " union ";
            
            //Purchase Invoice Module
            query += "select gr.id, gr.deleteflag, gr.grnumber as number , (SELECT entryno from journalentry where id=gr.journalentry) as jeno, gr.createdon as date, 'Purchase Invoices' as module, '6' as moduleid, gr.vendor as person  from goodsreceipt gr inner join journalentry je on je.id=gr.journalentry ";
            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                query += " inner join users on users.userid = gr.createdby and users.department = ? ";
                params.add(userDepartment);
            }
            query += "where gr.pendingapproval='1'  and gr.company=? ";
            params.add(companyId);
            if(dateFilterActive){
                query += " and je.entrydate >= ? and je.entrydate <= ? ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if(excludeRejectedRecords){
                query += " and gr.deleteflag = 'F' ";
            }
            query += " union ";
            
            //Purchase Order Module
            query += "select po.id, po.deleteflag, po.ponumber as number , '' as jeno, po.createdon as date, 'Purchase Order' as module, '18' as moduleid, po.vendor as person  from purchaseorder po ";
            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                query += " inner join users on users.userid = po.createdby and users.department = ? ";
                params.add(userDepartment);
            }
            query += " where po.approvestatuslevel !='11' and po.isfixedassetpo='0' and po.company=? ";
            params.add(companyId);
            if(dateFilterActive){
                query += " and po.orderdate >= ? and po.orderdate <= ? ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if(excludeRejectedRecords){
                query += " and po.deleteflag = 'F' ";
            }
            query += " union ";
            
            //Sales Order Module
            query += "select so.id, so.deleteflag, so.sonumber as number , '' as jeno, so.createdon as date, 'Sales Order' as module, '20' as moduleid, so.customer as person  from salesorder so ";
            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                query += " inner join users on users.userid = so.createdby and users.department = ? ";
                params.add(userDepartment);
            }
            query += " where so.approvestatuslevel!='11'  and so.company=?";
            params.add(companyId);
            if(dateFilterActive){
                query += " and so.orderdate >= ? and so.orderdate <= ? ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if(excludeRejectedRecords){
                query += " and so.deleteflag = 'F' ";
            }
            query += " union ";
            
            //Purchase Requisition Module
            query += "select pr.id, pr.deleteflag, pr.prnumber as number , '' as jeno, pr.requisitiondate as date, 'Purchase Requisition' as module, '32' as moduleid, pr.vendor  from purchaserequisition pr ";
            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                query += " inner join users on users.userid = pr.createdby and users.department = ? ";
                params.add(userDepartment);
            }
            query += " where pr.approvestatuslevel!='11'  and pr.isfixedassetpurchaserequisition='0' and pr.company=?";
            params.add(companyId);
            if(dateFilterActive){
                query += " and pr.requisitiondate >= ? and pr.requisitiondate <= ? ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if(excludeRejectedRecords){
                query += " and pr.deleteflag = 'F' ";
            }
            query += " union ";
            
            //Customer Quotation Module
            query += "select cq.id, cq.deleteflag, cq.quotationnumber as number , '' as jeno, cq.quotationdate as date, 'Customer Quotation' as module, '22' as moduleid, cq.customer as person  from quotation cq ";
            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                query += " inner join users on users.userid = cq.createdby and users.department = ? ";
                params.add(userDepartment);
            }
            query += " where cq.approvestatuslevel!='11'  and cq.company=?";
            params.add(companyId);
            if(dateFilterActive){
                query += " and cq.quotationdate >= ? and cq.quotationdate <= ? ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if(excludeRejectedRecords){
                query += " and cq.deleteflag = 'F' ";
            }
            query += " union ";
            
            //Vendor Quotation Module
            query += "select vq.id, vq.deleteflag, vq.quotationnumber as number , '' as jeno, vq.quotationdate as date, 'Vendor Quotation' as module, '23' as moduleid, vq.vendor as person  from vendorquotation vq ";
            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                query += " inner join users on users.userid = vq.createdby and users.department = ? ";
                params.add(userDepartment);
            }
            query += " where vq.approvestatuslevel!='11' and vq.isfixedassetvq='0' and vq.company=?";
            params.add(companyId);
            if(dateFilterActive){
                query += " and vq.quotationdate >= ? and vq.quotationdate <= ? ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if(excludeRejectedRecords){
                query += " and vq.deleteflag = 'F' ";
            }
            query += " union ";
            
            //Goods Receipt Order Module
            query += "select gro.id, gro.deleteflag, gro.gronumber as number , '' as jeno, gro.grorderdate as date, 'Goods Receipt Order' as module, '28' as moduleid, gro.vendor as person  from grorder gro ";
            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                query += " inner join users on users.userid = gro.createdby and users.department = ? ";
                params.add(userDepartment);
            }
            query += " where gro.approvestatuslevel!='11'  and gro.company=? ";
            params.add(companyId);
            if(dateFilterActive){
                query += " and gro.grorderdate >= ? and gro.grorderdate <= ? ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if(excludeRejectedRecords){
                query += " and gro.deleteflag = 'F' ";
            }
            query += " union ";
            
            //Delivery Order Module
            query += "select do.id, do.deleteflag, do.donumber as number , '' as jeno, do.orderdate as date, 'Delivery Order' as module, '27' as moduleid, do.customer as person  from deliveryorder do ";
            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                query += " inner join users on users.userid = do.createdby and users.department = ? ";
                params.add(userDepartment);
            }
            query += " where do.approvestatuslevel!='11'  and do.company=? ";
            params.add(companyId);
            
            if(dateFilterActive){
                query += " and do.orderdate >= ? and do.orderdate <= ? ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if(excludeRejectedRecords){
                query += " and do.deleteflag = 'F' ";
            }
            query += " union ";
            
            //Asset Purchase Requisition Module
            query += "select pr.id, pr.deleteflag, pr.prnumber as number , '' as jeno, pr.requisitiondate as date, 'Asset Purchase Requisition' as module, '87' as moduleid, pr.vendor as person  from purchaserequisition pr ";
            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                query += " inner join users on users.userid = pr.createdby and users.department = ? ";
                params.add(userDepartment);
            }
            query += " where pr.approvestatuslevel!='11' and pr.isfixedassetpurchaserequisition='1'  and pr.company=? ";
            params.add(companyId);            
            if(dateFilterActive){
                query += " and pr.requisitiondate >= ? and pr.requisitiondate <= ? ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if(excludeRejectedRecords){
                query += " and pr.deleteflag = 'F' ";
            }
            query += " union ";
            
            //Asset Vendor Quotation Module
            query += "select vq.id, vq.deleteflag, vq.quotationnumber as number , '' as jeno, vq.quotationdate as date, 'Asset Vendor Quotation' as module, '89' as moduleid, vq.vendor as person  from vendorquotation vq ";
            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                query += " inner join users on users.userid = vq.createdby and users.department = ? ";
                params.add(userDepartment);
            }
            query += " where vq.approvestatuslevel!='11' and vq.isfixedassetvq='1' and vq.company=? ";
            params.add(companyId);
            if(dateFilterActive){
                query += " and vq.quotationdate >= ? and vq.quotationdate <= ? ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if(excludeRejectedRecords){
                query += " and vq.deleteflag = 'F' ";
            }
            query += " union ";
            
            //Asset Purchase Order Module
            query += "select po.id, po.deleteflag, po.ponumber as number , '' as jeno, po.orderdate as date, 'Asset Purchase Order' as module, '90' as moduleid, po.vendor as person   from purchaseorder po ";
            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                query += " inner join users on users.userid = po.createdby and users.department = ? ";
                params.add(userDepartment);
            }
            query += " where po.approvestatuslevel !='11' and po.isfixedassetpo='1' and po.company=? ";
            params.add(companyId);
            if(dateFilterActive){
                query += " and po.orderdate >= ? and po.orderdate <= ? ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if(excludeRejectedRecords){
                query += " and po.deleteflag = 'F' ";
            }
            query += " union ";
            
            //Receive Payment
            query += "select rp.id, rp.deleteflag, rp.receiptnumber as number , (SELECT entryno from journalentry where id=rp.journalentry) as jeno, rp.createdon as date, 'Receive Payment' as module, '16' as moduleid, '' as person  from receipt rp inner join journalentry je on je.id=rp.journalentry ";
            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                query += " inner join users on users.userid = rp.createdby and users.department = ? ";
                params.add(userDepartment);
            }
            query += " where rp.approvestatuslevel !='11' and rp.company=? ";
            params.add(companyId);
            if(dateFilterActive){
                query += " and je.entrydate >= ? and je.entrydate <= ? ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if(excludeRejectedRecords){
                query += " and rp.deleteflag = 'F' ";
            }
            query += " union ";
            
            //Make Payment
            query += "select mp.id, mp.deleteflag, mp.paymentnumber as number , (SELECT entryno from journalentry where id=mp.journalentry) as jeno, mp.createdon as date, 'Make Payment' as module, '14' as moduleid, '' as person  from payment mp inner join journalentry je on je.id=mp.journalentry ";
            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                query += " inner join users on users.userid = mp.createdby and users.department = ? ";
                params.add(userDepartment);
            }
            query += " where mp.approvestatuslevel !='11' and mp.company=? ";
            params.add(companyId);
            if(dateFilterActive){
                query += " and je.entrydate >= ? and je.entrydate <= ? ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if(excludeRejectedRecords){
                query += " and mp.deleteflag = 'F' ";
            }
            query += " union ";
            
            //Credit Note
            query += "select cn.id, cn.deleteflag, cn.cnnumber as number , (SELECT entryno from journalentry where id=cn.journalentry) as jeno, cn.createdon as date, 'Credit Note' as module, '12' as moduleid, '' as person  from creditnote cn inner join journalentry je on je.id=cn.journalentry ";
            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                query += " inner join users on users.userid = cn.createdby and users.department = ? ";
                params.add(userDepartment);
            }
            query += " where cn.approvestatuslevel !='11' and cn.company=? ";
            params.add(companyId);
            if(dateFilterActive){
                query += " and je.entrydate >= ? and je.entrydate <= ? ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if(excludeRejectedRecords){
                query += " and cn.deleteflag = 'F' ";
            }
            query += " union ";
            
            //Debit Note
            query += "select dn.id, dn.deleteflag, dn.dnnumber as number , (SELECT entryno from journalentry where id=dn.journalentry) as jeno, dn.createdon as date, 'Debit Note' as module, '10' as moduleid, '' as person  from debitnote dn inner join journalentry je on je.id=dn.journalentry ";
            if (!StringUtil.isNullOrEmpty(userDepartment)) {
                query += " inner join users on users.userid = dn.createdby and users.department = ? ";
                params.add(userDepartment);
            }
            query += " where dn.approvestatuslevel !='11' and dn.company=? ";
            params.add(companyId);
            if(dateFilterActive){
                query += " and je.entrydate >= ? and je.entrydate <= ? ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if(excludeRejectedRecords){
                query += " and dn.deleteflag = 'F' ";
            }
            if (!StringUtil.isNullOrEmpty(ss)) {
                query += " ) as t WHERE t.number LIKE " + condition;
            }
            returnlist = executeSQLQuery(query, params.toArray());
            count = returnlist.size();
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                returnlist = executeSQLQueryPaging(query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountingDashboardDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("AccountingDashboardDAOImpl.getPendingApprovalDetails:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnlist, count);
    } 
    
    /**
     * Method is used to get draft document(s) from overall system. Currently we are checking Cash/Credit Sales and Customer Quotation.
     * @param requestParams
     * @return
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getDraftDocuments(HashMap<String, Object> requestParams) throws ServiceException {
        List returnlist = new ArrayList();
        ArrayList params = new ArrayList();
        int count = 0;
        try {
            String companyId = (String) requestParams.get("companyid");
            String ss = (String) requestParams.get(Constants.ss);
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String startDate = (String) requestParams.get(Constants.REQ_startdate);
            String endDate = (String) requestParams.get(Constants.REQ_enddate);
            StringBuilder queryBuilder = new StringBuilder();
            //Sales Invoice Module
            queryBuilder.append("select inv.id, inv.deleteflag, inv.invoiceNumber as number, 'Sales Invoices' as module from invoice inv inner join journalentry je on inv.journalentry=je.id where inv.company=? and inv.isdraft = true ");
            params.add(companyId);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                queryBuilder.append(" and je.entrydate >= ? and je.entrydate <= ? ");
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            queryBuilder.append(" union ");
            //Customer Quotation Module
            queryBuilder.append("select cq.id, cq.deleteflag, cq.quotationnumber as number , 'Customer Quotation' as module from quotation cq where cq.company=? and cq.isdraft = true ");
            params.add(companyId);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                queryBuilder.append(" and cq.quotationdate >= ? and cq.quotationdate <= ? ");
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            returnlist = executeSQLQuery(queryBuilder.toString(), params.toArray());
            count = returnlist.size();
        } catch (Exception ex) {
            Logger.getLogger(AccountingDashboardDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("AccountingDashboardDAOImpl.getDraftDocuments:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnlist, count);
    }
    /**
     * method used to get recurring sales invoices,Purchase invoice,make payments 
     * whose date fall under the given year.
     * @param requestParams
     * @return
     * @throws ServiceException 
     */
    public KwlReturnObject getrecurringdocuments(HashMap<String, Object> requestParams) throws ServiceException {
        List resultList = new ArrayList();
        ArrayList params = new ArrayList();
        int count = 0;
        try {
            String companyId = (String) requestParams.get("companyid");
            String ss = (String) requestParams.get(Constants.ss);
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String startDate = (String) requestParams.get(Constants.REQ_startdate);
            String endDate = (String) requestParams.get(Constants.REQ_enddate);
            StringBuilder invoicqueryBuilder = new StringBuilder();
            StringBuilder grqueryBuilder = new StringBuilder();
            StringBuilder mpqueryBuilder = new StringBuilder();
            //Sales Invoice Module
            params.add(companyId);
            invoicqueryBuilder.append("select (RINV.noofinvoicespost - RINV.noofremaininvoicespost) from invoice INV inner join repeatedinvoices RINV on INV.repeateinvoice = RINV.id where company=? and (INV.repeateinvoice is not null and RINV.isactivate='T')  and (INV.repeateinvoice is not null and RINV.ispendingapproval='F')");
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                invoicqueryBuilder.append(" and RINV.startdate >= ? and RINV.expiredate <= ? ");
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            //Purchase Invoice Module
            grqueryBuilder.append("select (RINV.noofinvoicespost - RINV.noofremaininvoicespost) from goodsreceipt gr inner join repeatedinvoices RINV on gr.repeateinvoice = RINV.id where company=? and (gr.repeateinvoice is not null and RINV.isactivate='T')  and (gr.repeateinvoice is not null and RINV.ispendingapproval='F')");
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                grqueryBuilder.append(" and RINV.startdate >= ? and RINV.expiredate <= ? ");
            }
            //Make Payment Module
            mpqueryBuilder.append("select (RP.noofjepost - RP.noofremainjepost) from payment P inner join repeatedpayment RP on P.repeatpayment = RP.id where company=? and (P.repeatpayment is not null and RP.isactivate='T')  and (P.repeatpayment is not null and RP.ispendingapproval='F')");
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                mpqueryBuilder.append(" and RP.startdate >= ? and RP.expiredate <= ? ");
            }

            resultList = executeSQLQuery(invoicqueryBuilder.toString(), params.toArray());
            if (resultList != null && !resultList.isEmpty() && resultList.get(0) != null) {
                Iterator recInvItr = resultList.iterator();
                while(recInvItr.hasNext()) {
                    count +=Integer.parseInt(recInvItr.next().toString());
                }
            }
            List grresultList = executeSQLQuery(grqueryBuilder.toString(), params.toArray());
            if(grresultList!= null && !grresultList.isEmpty() && grresultList.get(0)!=null){
                Iterator recGrItr = grresultList.iterator();
                while(recGrItr.hasNext()) {
                    count +=Integer.parseInt(recGrItr.next().toString());
                }
            }
            List mpresultList = executeSQLQuery(mpqueryBuilder.toString(), params.toArray());
            if (mpresultList != null && !mpresultList.isEmpty() && mpresultList.get(0) != null) {
                Iterator recMPItr = mpresultList.iterator();
                while(recMPItr.hasNext()) {
                    count += Integer.parseInt(recMPItr.next().toString());
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountingDashboardDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("AccountingDashboardDAOImpl.getrecurringdocuments:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, resultList, count);
    }
    
    @Override
    public boolean saveUserPreferencesOptions(UserPreferences userPreferences) throws ServiceException {
        boolean success = false;
        try {
            if (!StringUtil.isNullObject(userPreferences)) {
                saveOrUpdate(userPreferences);
                success = true;
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountingDashboardDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("AccountingDashboardDAOImpl.saveUserInitialShowHelpTextMsgPreferences:" + ex.getMessage(), ex);
        }
        
        return success;
    }
}
