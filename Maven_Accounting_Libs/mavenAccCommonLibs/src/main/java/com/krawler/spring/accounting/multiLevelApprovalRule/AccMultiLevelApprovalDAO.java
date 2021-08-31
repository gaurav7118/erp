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
package com.krawler.spring.accounting.multiLevelApprovalRule;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;

public interface AccMultiLevelApprovalDAO {

    /*
     * Multi Level Approval Flow
     */
    public KwlReturnObject getMultiApprovalRuleData(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getApprovalRuleTargetUsers(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject saveMultiApprovalRule(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject deleteMultiApprovalRule(HashMap<String, Object> dataMap) throws ServiceException;
    
    public boolean checkIfTransactionIsPendingForApproval(int moduleid, int level,String companyid) throws ServiceException; 
    
    public KwlReturnObject checkDepartmentWiseApprover(HashMap<String, Object> dataMap) throws ServiceException;    

}
