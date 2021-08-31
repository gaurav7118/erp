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
package com.krawler.spring.accounting.term;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;

/**
 *
 * @author krawler
 */
public interface accTermDAO {

    public KwlReturnObject addTerm(HashMap<String, Object> termMap) throws ServiceException;

    public KwlReturnObject updateTerm(HashMap<String, Object> termMap) throws ServiceException;
    
    public KwlReturnObject updateSrNoTerm(HashMap<String, Object> termMap) throws ServiceException;

    public KwlReturnObject deleteTerm(String termid, String companyid) throws ServiceException;

    public KwlReturnObject getTerm(HashMap<String, Object> filterParams) throws ServiceException;

    public void copyTerms(String companyid) throws ServiceException;
    
    public KwlReturnObject getPOTerm(String termid, String companyid) throws ServiceException;
    
    public KwlReturnObject getSOTerm(String termid, String companyid) throws ServiceException; 
    
    public KwlReturnObject getPITerm(String termid, String companyid) throws ServiceException; 
    
    public KwlReturnObject getSITerm(String termid, String companyid) throws ServiceException; 
    
    public KwlReturnObject getVQTerm(String termid, String companyid) throws ServiceException; 
    
    public KwlReturnObject getCQTerm(String termid, String companyid) throws ServiceException; 
    
    public KwlReturnObject getCustomerTerm(String termid, String companyid) throws ServiceException; 
    
    public KwlReturnObject getVendorTerm(String termid, String companyid) throws ServiceException; 
    /**
     * Get Linelevel terms Details
     * @param nObject
     * @return
     * @throws ServiceException 
     */
    public KwlReturnObject getGSTTermDetails(JSONObject nObject) throws ServiceException;
    
}
