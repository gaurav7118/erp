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
package com.krawler.spring.archive;

import java.lang.reflect.InvocationTargetException;
import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnMsg;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 *
 * @author Karthik
 */
public class archiveHandlerDAOImpl implements archiveHandlerDAO {

    private HibernateTemplate hibernateTemplate;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.hibernateTemplate = new HibernateTemplate(sessionFactory);
    }

    public KwlReturnObject archiveGlobal(JSONObject jo) throws ServiceException {
        int dl = 0;
        List ll = new ArrayList();
        Object validFlag = null;
        try {
            Class cl = Class.forName(jo.getString("classname"));
            Object invoker = hibernateTemplate.get(cl, jo.getString("id"));
            if (invoker != null) {
                Field field = invoker.getClass().getDeclaredField("isarchive");
                Class type = field.getType();
                Class arguments[] = new Class[]{type};
                java.lang.reflect.Method objMethod;
                objMethod = cl.getMethod("setIsarchive", arguments);
                Object[] obj = new Object[]{Boolean.parseBoolean(jo.getString("archive"))};
                objMethod.invoke(invoker, obj);
                objMethod = cl.getMethod("getValidflag");
                validFlag = objMethod.invoke(invoker);
                hibernateTemplate.update(invoker);
            }
            ll.add(validFlag);
        } catch (IllegalAccessException ex) {
            throw ServiceException.FAILURE("archiveHandlerDAOImpl.archiveGlobal : " + ex.getMessage(), ex);
        } catch (IllegalArgumentException ex) {
            throw ServiceException.FAILURE("archiveHandlerDAOImpl.archiveGlobal : " + ex.getMessage(), ex);
        } catch (InvocationTargetException ex) {
            throw ServiceException.FAILURE("archiveHandlerDAOImpl.archiveGlobal : " + ex.getMessage(), ex);
        } catch (NoSuchFieldException ex) {
            throw ServiceException.FAILURE("archiveHandlerDAOImpl.archiveGlobal : " + ex.getMessage(), ex);
        } catch (ClassNotFoundException ex) {
            throw ServiceException.FAILURE("archiveHandlerDAOImpl.archiveGlobal : " + ex.getMessage(), ex);
        } catch (NoSuchMethodException ex) {
            throw ServiceException.FAILURE("archiveHandlerDAOImpl.archiveGlobal : " + ex.getMessage(), ex);
        } catch (SecurityException ex) {
            throw ServiceException.FAILURE("archiveHandlerDAOImpl.archiveGlobal : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("archiveHandlerDAOImpl.archiveGlobal : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }
}
