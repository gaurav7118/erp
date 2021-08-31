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
package com.krawler.spring.accounting.uom.service;

import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.UOMSchema;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.uom.accUomController;
import com.krawler.spring.accounting.uom.accUomDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author krawler
 */
public class AccUomServiceImpl implements AccUomService {

    private accUomDAO accUomObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;

    public void setaccUomDAO(accUomDAO accUomObj) {
        this.accUomObj = accUomObj;
    }

    public AccountingHandlerDAO getAccountingHandlerDAOobj() {
        return accountingHandlerDAOobj;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
    
    public JSONObject getUnitOfMeasure(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            
            boolean doNotShowNAUomName = !StringUtil.isNullOrEmpty(request.getParameter("doNotShowNAUomName")) ? Boolean.parseBoolean(request.getParameter("doNotShowNAUomName")) : false;
            if (doNotShowNAUomName) {
                requestParams.put("doNotShowNAUomName", doNotShowNAUomName);
            }
            
            KwlReturnObject result = accUomObj.getUnitOfMeasure(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();

            JSONArray DataJArr = getUoMJson(request, list);
            jobj.put("data", DataJArr);
            jobj.put("count", count);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccUomServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }//getUnitOfMeasure

    public JSONArray getUoMJson(HttpServletRequest request, List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                UnitOfMeasure uom = (UnitOfMeasure) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("uomid", uom.getID());
                obj.put("uomname", uom.getNameEmptyforNA());
                obj.put("precision", uom.getAllowedPrecision());
                obj.put("uomtype", uom.getType());
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getUoMJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    @Override
    public JSONObject getISUOMSchemaConfiguredandUsed(Map<String, Object> requestData) throws ServiceException {
        JSONObject obj = new JSONObject();
        try {
            String uomschematype = "";
            String companyId = "";
            if (requestData.containsKey("uomschematype")) {
                uomschematype = (String) requestData.get("uomschematype");
            }
            if (requestData.containsKey("companyid")) {
                companyId = (String) requestData.get("companyid");
            }
            int count = accUomObj.searchUoMTypeUsedinProduct(uomschematype, companyId);
            obj.put("count", count);

        } catch (Exception ex) {
            throw ServiceException.FAILURE("getUoMJson : " + ex.getMessage(), ex);
        }
        return obj;
    }
    
    @Override
    public JSONArray getUoMJson(JSONObject paramjobj, List<UnitOfMeasure> list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            for (UnitOfMeasure uom : list) {
                JSONObject obj = new JSONObject();
                obj.put("uomid", uom.getID());
                obj.put("uomname", uom.getNameEmptyforNA());
                obj.put("precision", uom.getAllowedPrecision());
                obj.put("uomtype", uom.getType());
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getUoMJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    /**
     * method moved to service layer,
     * method used to get the UOMs which are mapped to particular UOM schema.
     */
    public JSONArray getPurchaseUOMSchemaJson(HashMap requestParams, List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                UOMSchema  uomSchema = (UOMSchema) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("rowid", uomSchema.getID());
                obj.put("purchaseuom", uomSchema.getPurchaseuom()!=null?uomSchema.getPurchaseuom().getID():"");
                obj.put("uomid", uomSchema.getPurchaseuom()!=null?uomSchema.getPurchaseuom().getID():"");
                obj.put("salesuom", uomSchema.getSalesuom()!=null?uomSchema.getSalesuom().getID():"");
                obj.put("orderuom", uomSchema.getOrderuom()!=null?uomSchema.getOrderuom().getID():"");
                obj.put("transferuom", uomSchema.getTransferuom()!=null?uomSchema.getTransferuom().getID():"");
                obj.put("purchaseuomname", uomSchema.getPurchaseuom()!=null?uomSchema.getPurchaseuom().getNameEmptyforNA():"");
                obj.put("uomname", uomSchema.getPurchaseuom()!=null?uomSchema.getPurchaseuom().getNameEmptyforNA():"");
                obj.put("precision", uomSchema.getPurchaseuom()!=null?uomSchema.getPurchaseuom().getAllowedPrecision():"");
                obj.put("equalsign", "=");
                obj.put("baseuom", uomSchema.getBaseuom()!=null?uomSchema.getBaseuom().getID():"");
                obj.put("baseuomrate", uomSchema.getBaseuomrate());
                obj.put("rateperuom", uomSchema.getRateperuom());
                obj.put("uomschematype", uomSchema.getUomschematype().getID());
                obj.put("uomnature", uomSchema.getUomnature());              
                obj.put("purchaseuomquantiy",1);              
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getUoMJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public JSONArray getSalesUOMSchemaJson(HashMap requestParams, List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                UOMSchema  uomSchema = (UOMSchema) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("rowid", uomSchema.getID());
                obj.put("purchaseuom", uomSchema.getPurchaseuom()!=null?uomSchema.getPurchaseuom().getID():"");
                obj.put("salesuom", uomSchema.getSalesuom()!=null?uomSchema.getSalesuom().getID():"");
                obj.put("uomid", uomSchema.getSalesuom()!=null?uomSchema.getSalesuom().getID():"");
                obj.put("uomname", uomSchema.getSalesuom()!=null?uomSchema.getSalesuom().getNameEmptyforNA():"");
                obj.put("purchaseuomname", uomSchema.getPurchaseuom()!=null?uomSchema.getPurchaseuom().getNameEmptyforNA():"");
                obj.put("precision", uomSchema.getSalesuom()!=null?uomSchema.getSalesuom().getAllowedPrecision():"");
                obj.put("baseuomrate", uomSchema.getBaseuomrate());
                obj.put("rateperuom", uomSchema.getRateperuom());
                obj.put("equalsign", "=");
                obj.put("baseuom", uomSchema.getBaseuom()!=null?uomSchema.getBaseuom().getID():"");
                obj.put("uomschematype", uomSchema.getUomschematype().getID());
                obj.put("uomnature", uomSchema.getUomnature());
                obj.put("purchaseuomquantiy",1);      
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getUoMJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    public JSONArray getOrderUOMSchemaJson(HashMap requestParams, List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                UOMSchema uomSchema = (UOMSchema) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("rowid", uomSchema.getID());
                obj.put("purchaseuom", uomSchema.getPurchaseuom() != null ? uomSchema.getPurchaseuom().getID() : "");
                obj.put("salesuom", uomSchema.getSalesuom() != null ? uomSchema.getSalesuom().getID() : "");
                obj.put("orderuom", uomSchema.getOrderuom() != null ? uomSchema.getOrderuom().getID() : "");
                obj.put("transferuom", uomSchema.getTransferuom() != null ? uomSchema.getTransferuom().getID() : "");
                obj.put("purchaseuomname", uomSchema.getPurchaseuom() != null ? uomSchema.getPurchaseuom().getNameEmptyforNA() : "");
                obj.put("baseuomrate", uomSchema.getBaseuomrate());
                obj.put("rateperuom", uomSchema.getRateperuom());
                obj.put("equalsign", "=");
                obj.put("baseuom", uomSchema.getBaseuom() != null ? uomSchema.getBaseuom().getID() : "");
                obj.put("uomschematype", uomSchema.getUomschematype().getID());
                obj.put("uomnature", uomSchema.getUomnature());
                obj.put("purchaseuomquantiy", 1);
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getUoMJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public JSONArray getTransferUOMSchemaJson(HashMap requestParams, List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                UOMSchema uomSchema = (UOMSchema) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("rowid", uomSchema.getID());
                obj.put("purchaseuom", uomSchema.getPurchaseuom() != null ? uomSchema.getPurchaseuom().getID() : "");
                obj.put("salesuom", uomSchema.getSalesuom() != null ? uomSchema.getSalesuom().getID() : "");
                obj.put("orderuom", uomSchema.getOrderuom() != null ? uomSchema.getOrderuom().getID() : "");
                obj.put("transferuom", uomSchema.getTransferuom() != null ? uomSchema.getTransferuom().getID() : "");
                obj.put("purchaseuomname", uomSchema.getPurchaseuom() != null ? uomSchema.getPurchaseuom().getNameEmptyforNA() : "");
                obj.put("baseuomrate", uomSchema.getBaseuomrate());
                obj.put("rateperuom", uomSchema.getRateperuom());
                obj.put("equalsign", "=");
                obj.put("baseuom", uomSchema.getBaseuom() != null ? uomSchema.getBaseuom().getID() : "");
                obj.put("uomschematype", uomSchema.getUomschematype().getID());
                obj.put("uomnature", uomSchema.getUomnature());
                obj.put("purchaseuomquantiy", 1);
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getUoMJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public JSONArray getUnitOfMeasureOfProductUOMSchemaJSON(HttpServletRequest request, List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                String uomid = (String) itr.next();
                KwlReturnObject uomresult = accountingHandlerDAOobj.getObject(UnitOfMeasure.class.getName(), uomid);
                UnitOfMeasure uom = (UnitOfMeasure) uomresult.getEntityList().get(0);
                JSONObject obj = new JSONObject();
                obj.put("uomid", uom.getID());
                obj.put("uomname", uom.getName());
                obj.put("precision", uom.getAllowedPrecision());
                obj.put("uomtype", uom.getType());
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getUnitOfMeasureOfProductUOMSchemaJSON : " + ex.getMessage(), ex);
        }
        return jArr;
    }
}//class
