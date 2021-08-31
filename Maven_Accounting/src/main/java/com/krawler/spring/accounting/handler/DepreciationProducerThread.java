/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.spring.accounting.handler;

import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DepreciationProducerThread implements Runnable {

    private BlockingQueue<JSONObject> queue;
    private List<Map<String, Object>> list = new ArrayList();
    

    public DepreciationProducerThread(BlockingQueue<JSONObject> q) {
        this.queue = q;
    }
    
    public void add(Map<String, Object> requestParams) {
        try {
            list.clear();
            list.add(requestParams);
        } catch (Exception ex) {
            Logger.getLogger(DepreciationProducerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run() {
        String detail = "";
        JSONArray jArr = new JSONArray();
        try {
            Map<String, Object> requestParams = list.get(0);
            List<String>  AssetIDList=null;
            List<String>  unOrderedAssetIDList=null;
            List<String> restrictPostDateBeforeInstDate = null;
            if(requestParams.containsKey("assetdetailIDList") && requestParams.get("assetdetailIDList")!=null){
             AssetIDList = (List<String>)requestParams.get("assetdetailIDList");
            }
            if(requestParams.containsKey("unOrderedAssetIDList") && requestParams.get("unOrderedAssetIDList")!=null){
             unOrderedAssetIDList= (List<String>)requestParams.get("unOrderedAssetIDList");
            }
            if (requestParams.containsKey("restrictPostDateBeforeInstDate") && requestParams.get("restrictPostDateBeforeInstDate") != null) {
                restrictPostDateBeforeInstDate = (List<String>) requestParams.get("restrictPostDateBeforeInstDate");
            }
            if(requestParams.containsKey("detail") && requestParams.get("detail")!=null){
                detail = requestParams.get("detail").toString();
                if(!StringUtil.isNullOrEmpty(detail)){
                    jArr = new JSONArray(detail);
                }
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!((AssetIDList != null && AssetIDList.contains(jobj.get("assetDetailsId"))) || (unOrderedAssetIDList != null && unOrderedAssetIDList.contains(jobj.get("assetDetailsId"))) || (restrictPostDateBeforeInstDate != null && restrictPostDateBeforeInstDate.contains(jobj.get("assetDetailsId"))))) {
                    queue.put(jobj);
                }
            }
            JSONObject jobj = new JSONObject();
            jobj.put(Constants.END_OF_DEPRECIATION_QUEUE, true);
            queue.put(jobj);
        } catch (JSONException | InterruptedException ex) {
            Logger.getLogger(DepreciationProducerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}