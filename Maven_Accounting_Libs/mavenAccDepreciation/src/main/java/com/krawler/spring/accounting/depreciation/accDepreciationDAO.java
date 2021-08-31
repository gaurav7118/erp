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
package com.krawler.spring.accounting.depreciation;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;

/**
 *
 * @author krawler
 */
public interface accDepreciationDAO {

    public KwlReturnObject addDepreciationDetail(HashMap<String, Object> ddMap) throws ServiceException;

    public KwlReturnObject updateDepreciationDetail(HashMap<String, Object> ddMap) throws ServiceException;

    public KwlReturnObject getDepreciation(HashMap<String, Object> filterParams) throws ServiceException;

    public KwlReturnObject getDepreciationFromJE(String jeid, String companyid) throws ServiceException;

    public KwlReturnObject deleteDepreciationJE(String je) throws ServiceException;

    public KwlReturnObject addAssetDetail(HashMap<String, Object> ddMap) throws ServiceException;

    public KwlReturnObject getAsset(HashMap<String, Object> filterParams) throws ServiceException;
}
