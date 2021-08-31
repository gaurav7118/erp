/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.handler;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AssetDetails;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class UnPostDepreciationConsumerThread implements Runnable {

    private BlockingQueue<JSONObject> queue;
    private List<Map<String, Object>> list = new ArrayList();

    private accProductDAO accProductObj;
    private auditTrailDAO auditTrailObj;

    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public UnPostDepreciationConsumerThread(BlockingQueue<JSONObject> q) {
        this.queue = q;
    }

    public void add(Map<String, Object> requestParams) {
        try {
            list.clear();
            list.add(requestParams);
        } catch (Exception ex) {
            Logger.getLogger(UnPostDepreciationConsumerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        try {
            Map<String, Object> requestParams = list.get(0);
            deleteAssetDepreciation(requestParams);

        } catch (InterruptedException ex) {
            Logger.getLogger(UnPostDepreciationConsumerThread.class.getName()).log(Level.INFO, ex.getMessage());
        }
    }

    public void deleteAssetDepreciation(Map<String, Object> requestParams) throws InterruptedException {

        List<AssetDetails> assetdetailList = new ArrayList();
        String companyid = "";
        DateFormat df = null;
        String userfullname = "", reqHeader = "", remoteIPAddress = "", userid = "";;
        try {
            if (requestParams.containsKey("assetdetailList") && requestParams.get("assetdetailList") != null) {
                assetdetailList = (List<AssetDetails>) requestParams.get("assetdetailList");
            }
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                companyid = requestParams.get("companyid").toString();
            }
            if (requestParams.containsKey("df") && requestParams.get("df") != null) {
                df = (DateFormat) requestParams.get("df");
            }
            if (requestParams.containsKey("userfullname") && requestParams.get("userfullname") != null) {
                userfullname = requestParams.get("userfullname").toString();
            }
            if (requestParams.containsKey("reqHeader") && requestParams.get("reqHeader") != null) {
                reqHeader = requestParams.get("reqHeader").toString();
            }
            if (requestParams.containsKey("remoteIPAddress") && requestParams.get("remoteIPAddress") != null) {
                remoteIPAddress = requestParams.get("remoteIPAddress").toString();
            }
            if (requestParams.containsKey("userid") && requestParams.get("userid") != null) {
                userid = requestParams.get("userid").toString();
            }
            Map<String, Object> auditRequestParams = new HashMap<>();
            auditRequestParams.put(Constants.reqHeader, reqHeader);
            auditRequestParams.put(Constants.remoteIPAddress, remoteIPAddress);
            auditRequestParams.put(Constants.useridKey, userid);
            JSONObject jobj = null;
            String jeids = "";
            List<String> journalEntryList = new ArrayList();
            Map<String, Object> paramsMap = new HashMap<>();

            while ((jobj = queue.take()) != null && !(jobj.has(Constants.END_OF_DEPRECIATION_QUEUE) && Boolean.parseBoolean(jobj.getString(Constants.END_OF_DEPRECIATION_QUEUE)))) {
                for (AssetDetails ad : assetdetailList) {
                    if (jobj.getString("assetDetailsId").equals(ad.getId()) && ad != null) {
                        String assetValue = "";
                        assetValue = ad.getAssetId();
                        String je = jobj.getString("jeid");
                        String jeNo = jobj.getString("jeno");
                        journalEntryList.add(je);
                        jeids += "'" + je + "'" + ",";

                        /* Insert entry into Audit Trail*/
                        Date depreciationDate = null;
                        String buildMsg = "";
                        if (jobj.has("fromyear") && !StringUtil.isNullOrEmpty(jobj.getString("fromyear"))) {
                            buildMsg = " for the Year " + jobj.getString("fromyear");
                        } else if (jobj.has("frommonth") && !StringUtil.isNullOrEmpty(jobj.getString("frommonth"))) {
                            try {
                                depreciationDate = df.parse(jobj.getString("frommonth"));
                                buildMsg = " for the month of  " + df.format(depreciationDate);
                            } catch (Exception ex) {
                                buildMsg = " for the month of  " + assetValue;
                            }
                        }
                        auditTrailObj.insertAuditLog(AuditAction.UNPOSTED_DEPRECIATION, "User " + userfullname + " has Unposted Depreciation for Asset ID " + assetValue + buildMsg+" with JE number "+jeNo,
                                auditRequestParams, assetValue);
                    }
                }
            }

            /*
             To delete all the asset depreciation from AssetDepreciationDetail table
             */
            if (!journalEntryList.isEmpty()) {
                paramsMap.put("journalEntryList", journalEntryList);
                // delete depreciation detail
                accProductObj.deleteAssetDepreciationDetails(paramsMap);

                if (!StringUtil.isNullOrEmpty(jeids) && jeids.length() > 0) {
                    jeids = jeids.substring(0, jeids.length() - 1);
                    paramsMap.put("jeids", jeids);
                }
                paramsMap.put("companyId", companyid);
                //Delete Journal Entry Posted For Depreciation
                accProductObj.deleteAssetDepreciationJE(paramsMap);
            }
        } catch (ServiceException | JSONException | InterruptedException ex) {
            Logger.getLogger(UnPostDepreciationConsumerThread.class.getName()).log(Level.INFO, ex.getMessage());
        }

    }

}
