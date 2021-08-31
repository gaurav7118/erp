/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.gst.services.gstr2;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.List;

/**
 *
 * @author krawler
 */
public interface GSTR2Dao {
    public List getInvoiceDataWithDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException;
    public List getCNDNWithInvoiceDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException;
    public List getHSNWiseInvoiceDataWithDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException;
    public List getTDSTCSITCDetails(JSONObject reqParams) throws ServiceException;
    public List getAdvanceDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException;
    public List getDNAgainstVendor(JSONObject reqParams) throws ServiceException, JSONException ;
    public KwlReturnObject saveGSTR2JSON(JSONObject params);
    public KwlReturnObject getImportedGSTR2AData(JSONObject reqParams);
    public List getCNAgainstVendor(JSONObject reqParams) throws ServiceException, JSONException;
    public List getGSTMissingDN(JSONObject reqParams) throws ServiceException, JSONException;
    public List getGSTMissingPurchaseOrder(JSONObject reqParams) throws ServiceException, JSONException;
    public List getGSTMissingGRN(JSONObject reqParams) throws ServiceException, JSONException;
    public List getITCJournalEntryDetails(JSONObject reqParams) throws ServiceException;
}
