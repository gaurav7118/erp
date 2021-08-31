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
package com.krawler.spring.accounting.loan;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Pandurang
 */
public interface accLoanDAO {
    
    public KwlReturnObject getDisbursementCount(String orderno, String companyid) throws ServiceException;
    
    public KwlReturnObject saveLoanDisbursement(Map<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getDisbursement(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getDisbursementJEs(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getRepaymentSheduleDetails(Map<String, Object> requestParams) throws ServiceException;
    
    
    public KwlReturnObject deleteRepaymetDetails(String disbursementid, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteDisbursementsPermanent(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getRepaymentSheduleDetailsForPayment(Map<String, Object> requestParams) throws ServiceException;
    
    public int saveFileMapping(Map<String,Object> filemap) throws ServiceException;
    
    
    public KwlReturnObject getTemporarySavedFiles(Map<String,Object> filemap) throws ServiceException;
    
}
