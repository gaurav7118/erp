/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.handler;

import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Group;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.common.KwlReturnObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.krawler.hql.accounting.Templatepnl;
import com.krawler.utils.json.base.JSONObject;
import com.krawler.utils.json.base.JSONArray;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 * @author krawler
 */
public class CompanySetupThread implements Runnable {

    private List<Map<String, Object>> list = new ArrayList();
    private accAccountDAO accAccountDAOobj;
    private HibernateTransactionManager txnManager;

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void add(Map<String, Object> requestParams) {
        try {
            list.clear();
            list.add(requestParams);
        } catch (Exception ex) {
            Logger.getLogger(CompanySetupThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CST_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            boolean isAdminSubdomain = false;
            Map<String, Object> requestParams = list.get(0);
            if(requestParams.containsKey("isAdminSubdomain") && requestParams.get("isAdminSubdomain")!=null){
                isAdminSubdomain = Boolean.parseBoolean(requestParams.get("isAdminSubdomain").toString());
            }
            String companyid = (String) requestParams.get(Constants.companyKey);
            String countryid = "", synctemplateid = "";
            if(requestParams.containsKey("countryid") && requestParams.get("countryid")!=null){
                countryid = (String) requestParams.get("countryid");
            }
            if(requestParams.containsKey("synctemplateid") && requestParams.get("synctemplateid")!=null){
                synctemplateid = (String) requestParams.get("synctemplateid");
            }
            
//            if(isAdminSubdomain){
            addDefaultCustomLayout(countryid, companyid, synctemplateid);
//            }else{
//                addDefaultCustomLayout(Constants.INDONESIAN_COUNTRYID, companyid, synctemplateid);
//            }
            // Update account's default groups to newly created company precific default groups. 
            updateAccountDefaultGroup(requestParams);
            
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            Logger.getLogger(CompanySetupThread.class.getName()).log(Level.INFO, ex.getMessage());
        }
    }

    private boolean addDefaultCustomLayout(String countryID, String companyid, String id) {
        boolean success = true;
        List templatelist = null;
        try {
            KwlReturnObject templateReturn = accAccountDAOobj.getDefaultCustomTemplate(countryID, id);
            templatelist = templateReturn.getEntityList();
            if (templatelist != null && !templatelist.isEmpty()) {
                for (Object object : templatelist) {
                    Object[] templateArr = (Object[]) object;
                    String templateID = templateArr[0].toString();
                    String templateName = templateArr[1].toString();
                    int templateid = Integer.parseInt(templateArr[2].toString());
                    int status = Integer.parseInt(templateArr[4].toString());
                    int templatetype = Integer.parseInt(templateArr[5].toString());
                    String templatetitle = templateArr[6].toString();
                    Map<String, Object> params = new HashMap<>();
                    params.put("name", templateName);
                    params.put("templateid", templateid);
                    params.put("templatetitle", templatetitle);
                    params.put("templatetype", templatetype);
                    params.put("companyid", companyid);
                    params.put("status", status);
                    KwlReturnObject result = accAccountDAOobj.updatePnLTemplate(params);
                    Templatepnl templatepnl = (Templatepnl) result.getEntityList().get(0);
                    if (templatepnl != null) {
                        params.clear();
                        params.put("templateid", templateID);
                        params.put("companyid", companyid);
                        success = accAccountDAOobj.copyDefaultCustomLayout(params, templatepnl);
                    }
                }
            }
        } catch (Exception ex) {
            success = false;
            Logger.getLogger(CompanySetupThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return success;
        }
    }
    
    private int updateAccountDefaultGroup(Map<String, Object> requestParams) {
        int resCount=0;
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            JSONArray accGroupDetails = new JSONArray();
            if(requestParams.containsKey("accGroupDetails") && requestParams.get("accGroupDetails")!=null){
                accGroupDetails = (JSONArray) requestParams.get("accGroupDetails");
}
            for (int i = 0; i < accGroupDetails.length(); i++) {
                JSONObject grJObj = accGroupDetails.getJSONObject(i);
                HashMap<String, Object> reqParams = new HashMap<>();
                reqParams.put("oldgroupid", grJObj.optString("grpOldId"));
                reqParams.put("newgroupid", grJObj.optString("newGrpId"));
                reqParams.put("companyid", companyid);
                int rowNum = accAccountDAOobj.updateAccountDefaultGroup(reqParams);
                resCount+=rowNum;
            }
        } catch (Exception ex) {
            Logger.getLogger(CompanySetupThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return resCount;
        }
    }
}
