/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.discount;

import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface accDiscountService {

    public JSONObject getDiscountMaster(Map requestParam) throws ServiceException;
    
    public JSONObject getDiscountsAndTerms(Map requestParam) throws ServiceException;

    public JSONObject saveDiscountMaster(Map requestParam) throws ServiceException, JSONException,AccountingException;
}
