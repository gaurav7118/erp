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
package com.krawler.customFieldMaster;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;

/**
 *
 * @author krawler
 */
public interface fieldDataManagerDAO {

    /**
     *
     * @param params
     * @return
     */
    public List getCustomColumnFormulae(Object[] params);

    /**
     * @param requestParams
     * @return
     * @throws ServiceException
     */
    public KwlReturnObject setCustomData(HashMap<String, Object> requestParams) throws ServiceException;

    /**
     * @param hql
     * @param params
     * @return
     */
    public List executeQuery(String hql, Object[] params) throws ServiceException;

    /**
     * @param entityClass
     * @param id
     * @return
     */
    public Object get(Class entityClass, Serializable id);
}
