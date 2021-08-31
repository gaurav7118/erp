/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.vendorpayment;

import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.AdvanceDetail;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import org.springframework.context.MessageSource;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public interface AccVendorPaymentServiceDAO {
    public List<JSONObject> getPaymentsJsonNew(HashMap<String, Object> requestParams, List list, List<JSONObject> jsonlist) throws ServiceException;

    public JSONArray getPaymentDetailJsonNew(HashMap<String, Object> requestParams) throws ServiceException;
    
    public JSONArray getTDSMasterRates(HashMap<String, Object> requestParams) throws ServiceException;
    
    public int deleteTDSMasterRates(HashMap<String, Object> requestParams) throws ServiceException,AccountingException;
    
    public JSONArray getAdvanceDetailsAgainstVendor(HashMap<String, Object> requestParams) throws ServiceException;

    public JSONObject repeatPayment();

    public JSONArray getSalesReturnJson(JSONArray jsonArray, List listcq, KWLCurrency currency, DateFormat userdf, String companyid);
    
    public JSONArray getMPDetailsItemJSONNew(JSONObject requestJobj, String SOID, HashMap<String, Object> paramMap) throws SessionExpiredException, ServiceException, JSONException;
    
    public JSONArray getMPDetailsItemJSON(JSONObject requestJobj, String companyid, String SOID,HashMap<String, Integer> FieldMap, HashMap<String, String> replaceFieldMap) 
            throws SessionExpiredException, ServiceException, JSONException;
    
    public void getAdvancePaymentCustomData(HashMap<String, Object> requestParams, AdvanceDetail advanceDetail, JSONObject obj) throws ServiceException;
    
    public JSONObject getPaymentsJSON(JSONObject paramJObj);
    
    public JSONObject getDocumentsForLinkingWithDebitNoteJSON(JSONObject paramJobj);
    
    public JSONArray getGoodsReceiptsForPayment(JSONObject paramJobj, JSONArray DataJArr) throws ServiceException;
    
    public JSONArray getCreditNoteMergedForPayment(JSONObject paramJobj, JSONArray DataJArr) throws ServiceException;
    
    public JSONArray getCreditNoteMergedJsonForPayment(JSONObject paramJobj, List list, JSONArray jArr, HashSet cnList, boolean isEdit) throws ServiceException ;
    public JSONArray getSalesPaymentKnockOffJSON(Map<String, Object> invoiceRequestParams) throws ServiceException, JSONException;
}
