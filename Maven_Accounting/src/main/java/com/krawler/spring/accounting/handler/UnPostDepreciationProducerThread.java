/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

/**
 *
 * @author krawler
 */
public class UnPostDepreciationProducerThread implements Runnable {

    private BlockingQueue<JSONObject> queue;
    private List<Map<String, Object>> list = new ArrayList();

    public UnPostDepreciationProducerThread(BlockingQueue<JSONObject> q) {
        this.queue = q;
    }

    public void add(Map<String, Object> requestParams) {
        try {
            list.clear();
            list.add(requestParams);
        } catch (Exception ex) {
            Logger.getLogger(UnPostDepreciationProducerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        String detail = "";
        JSONArray jArr = new JSONArray();
        List<String>  unOrderedAssetIDList=null;
        List<String>  lockYearAssetList=null;
        try {
            Map<String, Object> requestParams = list.get(0);
            if (requestParams.containsKey("detail") && requestParams.get("detail") != null) {
                detail = requestParams.get("detail").toString();
                if (!StringUtil.isNullOrEmpty(detail)) {
                    jArr = new JSONArray(detail);
                }
            }
            if(requestParams.containsKey("unOrderedAssetIDList") && requestParams.get("unOrderedAssetIDList")!=null){
             unOrderedAssetIDList= (List<String>)requestParams.get("unOrderedAssetIDList");
            }
            if(requestParams.containsKey("lockYearAssetList") && requestParams.get("lockYearAssetList")!=null){
             lockYearAssetList= (List<String>)requestParams.get("lockYearAssetList");
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                   if (!((unOrderedAssetIDList!=null && unOrderedAssetIDList.contains(jobj.get("assetDetailsId"))) || (lockYearAssetList!=null && lockYearAssetList.contains(jobj.get("assetDetailsId"))))) {
                    queue.put(jobj);
                }
            }
            JSONObject jobj = new JSONObject();
            jobj.put(Constants.END_OF_DEPRECIATION_QUEUE, true);
            queue.put(jobj);
        } catch (JSONException | InterruptedException ex) {
            Logger.getLogger(UnPostDepreciationProducerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
