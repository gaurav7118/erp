/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.ws.service;

import com.krawler.common.service.ServiceException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;

public interface ContractService {

    public JSONObject getContractDetails(JSONObject jobj) throws ServiceException, JSONException;

    public JSONObject getContractTermDetails(JSONObject jobj) throws ServiceException, JSONException;

    public JSONObject getContractInvoiceDetails(JSONObject jobj) throws ServiceException, JSONException;

    public JSONObject getContractAgreementDetails(JSONObject jobj) throws ServiceException, JSONException;

    public JSONObject getAccountContractDetails(JSONObject jobj) throws ServiceException, JSONException;

    public JSONObject getContractNormalDOItem(JSONObject jobj) throws ServiceException, JSONException;

    public JSONObject getContractReplacementDOItem(JSONObject jobj) throws ServiceException, JSONException;
    
    public JSONObject getAttachDocuments(JSONObject jobj) throws ServiceException, JSONException;
    
    public JSONObject saveContractService(JSONObject jobj) throws ServiceException, JSONException;
}
