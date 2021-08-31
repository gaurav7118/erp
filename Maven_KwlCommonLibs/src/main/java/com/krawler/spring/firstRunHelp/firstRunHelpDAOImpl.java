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
package com.krawler.spring.firstRunHelp;

import com.krawler.common.service.ServiceException;
import com.krawler.common.admin.EditHelp;
import com.krawler.common.dao.BaseDAO;
import com.krawler.spring.common.KwlReturnObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.hibernate.HibernateException;

/**
 *
 * @author Karthik
 */
public class firstRunHelpDAOImpl extends BaseDAO implements firstRunHelpDAO {

    public KwlReturnObject getModule() throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String Hql = "select DISTINCT c.modulename,c.modeid from com.krawler.common.admin.EditHelp c";
            ll = executeQuery( Hql);
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("firstRunHelpDAOImpl.getModule", e);
        }
        return new KwlReturnObject(true, "001", "", ll, dl);
    }

    public KwlReturnObject getComponents(String modName) throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String Hql = "select c from com.krawler.common.admin.EditHelp c where c.modeid=? order by (c.id * 1)";
            ll = executeQuery( Hql, new Object[]{modName});
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("firstRunHelpDAOImpl.getComponents", e);
        }
        return new KwlReturnObject(true, "001", "", ll, dl);

    }

    public KwlReturnObject getCompDetails(String modName) throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String Hql = "select c from com.krawler.common.admin.EditHelp c where c.id=?";
            ll = executeQuery( Hql, new Object[]{modName});
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("firstRunHelpDAOImpl.getCompDetails", e);
        }
        return new KwlReturnObject(true, "001", "", ll, dl);
    }

    public KwlReturnObject saveCompDetails(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = new ArrayList();
        int dl = 0;
        try {
            if (requestParams.containsKey("id") && !requestParams.get("id").toString().equals("")) {
                EditHelp help = (EditHelp) load(EditHelp.class, requestParams.get("id").toString());
                if (requestParams.containsKey("title") && !requestParams.get("title").toString().equals("")) {
                    help.setTitle(requestParams.get("title").toString());
                }
                if (requestParams.containsKey("desc") && !requestParams.get("desc").toString().equals("")) {
                    help.setDescp(requestParams.get("desc").toString());
                }
                save(help);
                ll.add(help);
            }
        } catch (HibernateException e) {
            throw ServiceException.FAILURE("firstRunHelpDAOImpl.saveCompDetails", e);
        }
        return new KwlReturnObject(true, "001", "", ll, dl);
    }
}
