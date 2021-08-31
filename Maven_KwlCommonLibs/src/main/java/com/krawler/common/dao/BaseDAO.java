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
package com.krawler.common.dao;

import com.krawler.common.admin.DefaultHeader;
import com.krawler.common.admin.DefaultHeaderModuleJoinReference;
import com.krawler.common.query.Clause;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.BaseStringUtil;
import com.krawler.common.util.Constants;
import com.krawler.common.util.DataInvalidateException;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * @author Johnson
 *
 * This Class should be ex   tended by all the DAO's.
 */
public class BaseDAO extends HibernateDaoSupport {

    private JdbcTemplate jdbcTemplate;
    private HibernateTransactionManager txnManager;

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Immediately loads the object using the given class identity
     *
     * @param entityClass class for object to load
     * @param id unique identifier of persistent object
     * @return the persistent object
     */
    public Object get(Class entityClass, Serializable id) {
        return getHibernateTemplate().get(entityClass, id);
    }

    /**
     * Saves the Hibernate Entity.
     *
     * @param entity The entity to persist
     * @return identity of persistent object
     */
    public Serializable save(Object entity) throws ServiceException {
        return getHibernateTemplate().save(entity);
    }

    /**
     * K
     *
     * Saves all the entities of a specific type in the provided collection
     *
     * @param entities
     */
    public void saveAll(Collection entities) throws ServiceException {
        getHibernateTemplate().saveOrUpdateAll(entities);
    }

    /**
     * Saves or Updates the Hibernate Entity.
     *
     * @param entity The entity to persist
     */
    public void saveOrUpdate(Object entity) throws ServiceException {
        getHibernateTemplate().saveOrUpdate(entity);
    }

    /**
     * Updates the Hibernate Entity.
     *
     * @param entity The entity to persist
     */
    public void update(Object entity) throws ServiceException {
        getHibernateTemplate().update(entity);
    }

    /**
     * Executes the provided HQL query after applying the provided parameters
     * and returns the result
     *
     * @param hql The query to execute
     * @param params Query Paramters
     * @return List
     */
    public List executeQuery(String hql, Object[] params) throws ServiceException {
        List results = null;
        results = getHibernateTemplate().find(hql, params);
        return results;
    }

    /**
     * Executes the provided HQL query after applying the provided parameter and
     * returns the result
     *
     * @param hql The query to execute
     * @param param Query Paramter
     * @return List
     */
    public List executeQuery(String hql, Object param) throws ServiceException {
        Object[] params = {param};
        return executeQuery(hql, params);
    }

    /**
     * Executes the provided HQL query
     *
     * @param hql The query to execute
     * @return List
     */
    public List executeQuery(String hql) throws ServiceException {
        return executeQuery(hql, null);
    }

    /**
     * Executes the provided HQL query after applying the provided collection
     * parameter and
     *
     * returns the result
     *
     * @param hql The query to execute
     * @param paramnames Collection Query Paramters names
     * @param params Collection Query Paramters values
     * @return List
     */
    public List executeCollectionQuery(final String hql, final List<String> paramnames, final List<List> params) throws ServiceException {
        List results = null;
        results = getHibernateTemplate().executeFind(new HibernateCallback() {

            public Object doInHibernate(Session session) {
                Query query = session.createQuery(hql);
                for (int i = 0; i < paramnames.size(); i++) {
                    query.setParameterList(paramnames.get(i), params.get(i));
                }
                return query.list();
            }
        });
        return results;
    }

    /**
     * Executes the provided HQL query after applying the provided named params
     * parameter
     * 
     * @param hql Hql query
     * @param params Named parameters
     * @return List
     */
    public List executeCollectionQuery(final String hql, final Map<String, Object> params) {
        return executeCollectionQuery(hql, params, (com.krawler.common.util.Paging)null);
    }
    
    /**
     * Desc:This method is used to exxute HQL DML statemt with Collection.
     * @param hql
     * @param params
     * @return int
     */
     public int executeUpdateCollectionQuery(final String hql, final Map<String, Object> params) {
        return executeUpdateCollectionQuery(hql, params, (com.krawler.common.util.Paging)null);
    }
    /**
     * Executes the provided HQL query after applying the provided named params
     * parameter
     * 
     * @param hql Hql query
     * @param params Named parameters
     * @param paging paging for limited data
     * @return List
     */
    public List executeCollectionQuery(final String hql, final Map<String, Object> params, final com.krawler.common.util.Paging paging) {
        List results = getHibernateTemplate().executeFind(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                Query query = session.createQuery(hql);
                for (Map.Entry<String, Object> entrySet : params.entrySet()) {
                    Object value = entrySet.getValue();
                    if(value instanceof Collection){
                        query.setParameterList(entrySet.getKey(), (Collection)value);
                    }else{
                        query.setParameter(entrySet.getKey(), value);
                    }
                    
                }
                if (paging != null && paging.isValid()) {
                    query.setFirstResult(paging.getOffset());
                    query.setMaxResults(paging.getLimit());
                }
                return query.list();
            }
        });
        return results;
    }
    
    /**
     * Description: Executes the provided HQL query for DML statement with Collection data
     * parameter
     * @param hql Hql query
     * @param params Named parameters
     * @param paging paging for limited data
     * @return int
     */
    public int executeUpdateCollectionQuery(final String hql, final Map<String, Object> params, final com.krawler.common.util.Paging paging) {
        int numRow = 0;
        numRow = (Integer) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) {
                Query query = session.createQuery(hql);
                for (Map.Entry<String, Object> entrySet : params.entrySet()) {
                    Object value = entrySet.getValue();
                    if(value instanceof Collection){
                        query.setParameterList(entrySet.getKey(), (Collection)value);
                    }else{
                        query.setParameter(entrySet.getKey(), value);
                    }
                }
                if (paging != null && paging.isValid()) {
                    query.setFirstResult(paging.getOffset());
                    query.setMaxResults(paging.getLimit());
                }
                return query.executeUpdate();
            }
        });
        return numRow;
    }
    
    /**
     * Executes the provided SQL query after applying the provided named params
     * parameter
     * 
     * @param sql Sql query
     * @param params Named parameters
     * @param paging paging for limited data
     * @return List
     */
    public List executeCollectionSqlQuery(final String sql, final Map<String, Object> params, final com.krawler.common.util.Paging paging) {
        List results = getHibernateTemplate().executeFind(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                Query query = session.createSQLQuery(sql);
                for (Map.Entry<String, Object> entrySet : params.entrySet()) {
                    Object value = entrySet.getValue();
                    if(value instanceof Collection){
                        query.setParameterList(entrySet.getKey(), (Collection)value);
                    }else{
                        query.setParameter(entrySet.getKey(), value);
                    }
                    
                }
                if (paging != null && paging.isValid()) {
                    query.setFirstResult(paging.getOffset());
                    query.setMaxResults(paging.getLimit());
                }
                return query.list();
            }
        });
        return results;
    }

    /**
     * Executes a limit select query using the provided query, parameters and
     * limits. Limits are provided using the pagingParam parameter.
     *
     * @param hql The query to execute
     * @param params Query Paramter
     * @param pagingParam Limit paramters. The first entry is the lower limit
     * and the second is the upper limit
     * @return List
     */
    public List executeQueryPaging(final String hql, final Object[] params, final Integer[] pagingParam) throws ServiceException {
        List results = null;
        results = getHibernateTemplate().executeFind(new HibernateCallback() {

            public Object doInHibernate(Session session) {
                Query query = session.createQuery(hql);

                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        query.setParameter(i, params[i]);
                    }
                }
                query.setFirstResult(pagingParam[0]);
                query.setMaxResults(pagingParam[1]);

                return query.list();
            }
        });
        return results;
    }

    public List executeSQLQueryPaging(String hql,
            Object[] params, com.krawler.common.util.Paging paging) throws ServiceException {
        return executeSQLQueryPaging(hql, params, new Integer[]{paging.getOffset(), paging.getLimit()});
    }

    public List executeSQLQueryPaging(final String hql,
            final Object[] params, final Integer[] pagingParam) throws ServiceException {
        List results = null;
        results = getHibernateTemplate().executeFind(new HibernateCallback() {

            public Object doInHibernate(Session session) {
                SQLQuery query = session.createSQLQuery(hql);
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        query.setParameter(i, params[i]);
                    }
                }
                query.setFirstResult(pagingParam[0]);
                query.setMaxResults(pagingParam[1]);
                return query.list();
            }
        });
        return results;
    }

    /**
     * Executes a limit select query using the provided query and limits. Limits
     * are provided using the pagingParam parameter.
     *
     * @param hql The query to execute
     * @param pagingParam Limit paramters. The first entry is the lower limit
     * and the second is the upper limit
     * @return List
     */
    public List executeQueryPaging(String hql, Integer[] pagingParam) throws ServiceException {
        return executeQueryPaging(hql, null, pagingParam);
    }

    /**
     * Executes an update query using the provided hql and query parameters
     *
     * @param hql Query to execute
     * @param params the query paramters
     * @return List
     */
    public int executeUpdate(final String hql, final Object[] params) throws ServiceException {
        int numRow = 0;
        numRow = (Integer) getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session session) {
                int numRows = 0;
                Query query = session.createQuery(hql);
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        query.setParameter(i, params[i]);
                    }
                }
                numRows = query.executeUpdate();
                return numRows;
            }
        });
        return numRow;
    }
    
    public List executeQuery(final String hql, final Object param, final Map<String, Object> namedParams) throws ServiceException {
        Object[] params = {param};
        return executeQuery(hql, params, namedParams);
    }

    public List executeQuery(final String hql, final Object[] params, final Map<String, Object> namedParams) throws ServiceException {
        return getHibernateTemplate().executeFind(new HibernateCallback() {

            public Object doInHibernate(Session session) {
                Query query = session.createQuery(hql);
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        query.setParameter(i, params[i]);
                    }
                }
                if (namedParams != null) {
                    for (Map.Entry<String, Object> entry : namedParams.entrySet()) {
                        Object value = entry.getValue();
                        if (value != null) {
                            if (value instanceof Collection) {
                                query.setParameterList(entry.getKey(), (Collection) value);
                            } else if (entry.getValue().getClass().isArray()) {
                                query.setParameterList(entry.getKey(), (Object[]) value);
                            } else {
                                query.setParameter(entry.getKey(), entry.getValue());
                            }
                        } else {
                            query.setParameter(entry.getKey(), entry.getValue());
                        }
                    }
                }
                return query.list();
            }
        });
    }
    
    public List executeSQLQuery(final String hql, final Object[] params, final Map<String, Object> namedParams) throws ServiceException {
        return getHibernateTemplate().executeFind(new HibernateCallback() {

            public Object doInHibernate(Session session) {
                Query query = session.createSQLQuery(hql);
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        query.setParameter(i, params[i]);
                    }
                }
                if (namedParams != null) {
                    for (Map.Entry<String, Object> entry : namedParams.entrySet()) {
                        Object value = entry.getValue();
                        if (value != null) {
                            if (value instanceof Collection) {
                                query.setParameterList(entry.getKey(), (Collection) value);
                            } else if (entry.getValue().getClass().isArray()) {
                                query.setParameterList(entry.getKey(), (Object[]) value);
                            } else {
                                query.setParameter(entry.getKey(), entry.getValue());
                            }
                        } else {
                            query.setParameter(entry.getKey(), entry.getValue());
                        }
                    }
                }
                return query.list();
            }
        });
    }

    public List executeSQLQuery(final String hql, final Object[] params) throws ServiceException {
        return getHibernateTemplate().executeFind(new HibernateCallback() {

            public Object doInHibernate(Session session) {
                Query query = session.createSQLQuery(hql);
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        query.setParameter(i, params[i]);
                    }
                }
                return query.list();
            }
        });
    }

    public List executeSQLQuery(String hql,
            Object param) throws ServiceException {
        Object[] params = {param};
        return executeSQLQuery(hql, params);
    }

    public List executeSQLQuery(final String hql) throws ServiceException {
        return executeSQLQuery(hql, null);
    }

    /**
     * Executes an update query using the provided hql and query parameters
     *
     * @param hql Query to execute
     * @param params the query paramters
     * @return List
     */
    public int executeUpdate(final String hql, final Object[] params, final Map<String, Object> namedParams) throws ServiceException {
        int numRow = 0;
        numRow = (Integer) getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session session) {
                int numRows = 0;
                Query query = session.createQuery(hql);
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        query.setParameter(i, params[i]);
                    }
                }
                if (namedParams != null) {
                    for (Map.Entry<String, Object> entry : namedParams.entrySet()) {
                        Object value = entry.getValue();
                        if (value != null) {
                            if (value instanceof Collection) {
                                query.setParameterList(entry.getKey(), (Collection) value);
                            } else if (entry.getValue().getClass().isArray()) {
                                query.setParameterList(entry.getKey(), (Object[]) value);
                            } else {
                                query.setParameter(entry.getKey(), entry.getValue());
                            }
                        } else {
                            query.setParameter(entry.getKey(), entry.getValue());
                        }
                    }
                }
                numRows = query.executeUpdate();
                return numRows;
            }
        });
        return numRow;
    }

    /**
     * Executes an update query using the provided hql, query parameters and
     * paging parameters
     *
     * @param hql Query to execute
     * @param params Query parameters
     * @param pagingParam paging parameters
     * @return List
     */
    public int executeUpdatePaging(final String hql, final Object[] params, final Integer[] pagingParam) throws ServiceException {
        int numRow = 0;
        numRow = (Integer) getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session session) {
                int numRows = 0;
                Query query = session.createQuery(hql);
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        query.setParameter(i, params[i]);
                    }
                }
                query.setFirstResult(pagingParam[0]);
                query.setMaxResults(pagingParam[1]);
                numRows = query.executeUpdate();
                session.flush();
                session.clear();
                return numRows;
            }
        });

        return numRow;
    }

    /**
     * Executes an update query using the provided hql and query parameter
     *
     * @param hql Query to execute
     * @param param Query Parameter
     * @return List
     */
    public int executeUpdate(String hql, Object param) throws ServiceException {
        Object[] params = {param};
        return executeUpdate(hql, params);
    }

    /**
     * Executes an update query using the provided hql and query parameter
     *
     * @param sql Query to execute
     * @return List
     */
    public int executeUpdate(String sql) throws ServiceException {
        return executeUpdate(sql, null);
    }

    /**
     * @param requestParams
     * @param classstr
     * @param primarykey
     * @return
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws DataInvalidateException
     */
    public Object setterMethod(HashMap<String, Object> requestParams, String classstr, String primarykey)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException,
            NoSuchMethodException, IllegalArgumentException, InvocationTargetException, DataInvalidateException, ServiceException {
        Object obj = null;
        Class cl = Class.forName(classstr);
        if (requestParams.get(primarykey) != null) {
            obj = get(cl, requestParams.get(primarykey).toString());
            if (obj == null) {
                obj = cl.newInstance();
            }
        } else {
            obj = cl.newInstance();
            Method setter = cl.getMethod("set" + primarykey, String.class);
            String id = UUID.randomUUID().toString();
            setter.invoke(obj, id);
        }
        boolean isBooleanTypeField = false;
        for (Object key : requestParams.keySet()) {
            Class rettype = null;
            try {
                rettype = cl.getMethod("get" + key).getReturnType();
            } catch (NoSuchMethodException ex) {// if imported column is of boolean type
                rettype = cl.getMethod("is" + key).getReturnType();
                isBooleanTypeField = true;
            }
            Method setter = cl.getMethod("set" + key, rettype);
            if (requestParams.get(key) != null) {
                if (rettype.isPrimitive() || rettype.equals(String.class) || rettype.equals(Date.class) || rettype.equals(Integer.class) || rettype.equals(Boolean.class)) {
                    if (rettype.getSimpleName().equals("boolean") && isBooleanTypeField) {
                        boolean value = false;
                        if (requestParams.get(key).toString().equalsIgnoreCase("FALSE")) {
                            value = false;
                        } else if (requestParams.get(key).toString().equalsIgnoreCase("TRUE")) {
                            value = true;
                        } else {
                            throw new DataInvalidateException("Ambiguous Value Found for " + key + " Column");
                        }
                        setter.invoke(obj, value);
                    } else {
                        setter.invoke(obj, requestParams.get(key));
                    }
                } else {
                    setter.invoke(obj, get(rettype, requestParams.get(key).toString()));
                }
            }
        }
        save(obj);

        return obj;
    }

    /**
     * @param queryParams
     * @param allFlag
     * @return
     */
    public KwlReturnObject getTableData(HashMap<String, Object> queryParams, boolean allFlag) throws ServiceException {
        List ll = null;
        int dl = 0;
        String tableName = queryParams.get("table_name").toString();
        String userListParam = queryParams.get("userlist_param").toString();
        String userListVal = queryParams.get("userlist_value").toString();
        ArrayList filter_names = (ArrayList) queryParams.get("filter_names");
        ArrayList filter_values = (ArrayList) queryParams.get("filter_values");
        ArrayList order_by = null;
        ArrayList order_type = null;
        if (queryParams.containsKey("order_by")) {
            order_by = (ArrayList) queryParams.get("order_by");
        }
        if (queryParams.containsKey("order_type")) {
            order_type = (ArrayList) queryParams.get("order_type");
        }

        String Hql = "select c from " + tableName + " c ";
        String filterQuery = BaseStringUtil.filterQuery(filter_names, "where") + " and " + userListParam + " in ("
                + userListVal + ")";
        Hql += filterQuery;

        String orderQuery = BaseStringUtil.orderQuery(order_by, order_type);
        Hql += orderQuery;

        ll = executeQuery(Hql, filter_values.toArray());
        dl = ll.size();
        if (!allFlag) {
            int start = Integer.parseInt(queryParams.get("start").toString());
            int limit = Integer.parseInt(queryParams.get("limit").toString());
            ll = executeQueryPaging(Hql, filter_values.toArray(), new Integer[]{start, limit});
        }

        return new KwlReturnObject(true, "002", "", ll, dl);
    }

    /**
     * @param criteria
     * @return
     */
    public List findByCriteria(DetachedCriteria criteria) throws ServiceException {
        return getHibernateTemplate().findByCriteria(criteria);
    }

    public List executeNativeQuery(String query) throws ServiceException {
        return executeNativeQuery(query, new Object[]{});
    }

    public List executeNativeQuery(String query, Object param) throws ServiceException {
        return executeNativeQuery(query, new Object[]{param});
    }

    /**
     * executes the native SQL query
     *
     * @param query the given query string
     * @param params the parameters to pass in the query
     * @return the list of records (rows)
     */
    public List executeNativeQuery(String query, Object[] params) throws ServiceException {
        HibernateCallback hcb = new HibernateCallback() {

            private String sql;
            private Object[] params;

            public HibernateCallback setQuery(String sql, Object[] params) {
                this.sql = sql;
                this.params = params;
                return this;
            }

            @Override
            public List doInHibernate(Session sn) throws HibernateException, SQLException {
                Query q = sn.createSQLQuery(sql);
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        q.setParameter(i, params[i]);
                    }
                }
                return q.list();
            }
        }.setQuery(query, params);

        return getHibernateTemplate().executeFind(hcb);
    }

    /**
     * executes the native SQL Update Query
     *
     * @param query the given query string
     * @param params the parameters to pass in the query
     * @return the list of records (rows)
     */
    public Object executeNativeUpdate(String query, Object[] params) throws ServiceException {
        HibernateCallback hcb = new HibernateCallback() {

            private String sql;
            private Object[] params;

            public HibernateCallback setQuery(String sql, Object[] params) {
                this.sql = sql;
                this.params = params;
                return this;
            }

            @Override
            public Object doInHibernate(Session sn) throws HibernateException, SQLException {
                Query q = sn.createSQLQuery(sql);
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        q.setParameter(i, params[i]);
                    }
                }
                return q.executeUpdate();
            }
        }.setQuery(query, params);

        return getHibernateTemplate().execute(hcb);
    }

    /**
     * Removes a entity from persistent storage permanently
     *
     * @param entity the object to remove
     */
    public void delete(Object entity) throws ServiceException {
        getHibernateTemplate().delete(entity);
    }

    public void flush() throws ServiceException {
        getHibernateTemplate().flush();
    }

    public void clear() throws ServiceException {
        getHibernateTemplate().clear();
    }

    /**
     * @param query
     * @param obj
     * @return
     */
    public List find(String query, Object[] obj) throws ServiceException {
        return getHibernateTemplate().find(query, obj);
    }

    public SqlRowSet queryForRowSetJDBC(String query, Object[] params) throws ServiceException {
        return jdbcTemplate.queryForRowSet(query, params);
    }

    /**
     *
     * @param <T>
     * @param sql
     * @param args
     * @param rowMapper
     * @return
     */
//    public <T extends Object> List<T> queryJDBC(String sql, Object[] args, RowMapper<T> rowMapper) {
//        return jdbcTemplate.query(sql, args, rowMapper);
//    }
    public int updateJDBC(String sql, Object[] args) throws ServiceException {
        return jdbcTemplate.update(sql, args);
    }

    public int queryForIntJDBC(String sql, Object[] args) throws ServiceException {
        return jdbcTemplate.queryForInt(sql, args);
    }

    public Object executeNativeUpdate(String query) throws ServiceException {
        return executeNativeUpdate(query, null);
    }

    public String buildQuery(String query, Clause[] clauses) throws ServiceException {
        if (clauses != null) {
            Arrays.sort(clauses);
            for (int i = 0; i < clauses.length; i++) {
                query += clauses[i].getQueryString();
            }
        }

        return query;
    }

    public void commit(String defName, int propagationBehavior, int isolationLevel) throws ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName(defName);
        def.setPropagationBehavior(propagationBehavior);
        if (isolationLevel != 0) {
            def.setIsolationLevel(isolationLevel);
        }
        txnManager.commit(txnManager.getTransaction(def));
    }

    public void rollback(String defName, int propagationBehavior, int isolationLevel) throws ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName(defName);
        def.setPropagationBehavior(propagationBehavior);
        if (isolationLevel != 0) {
            def.setIsolationLevel(isolationLevel);
        }
        txnManager.rollback(txnManager.getTransaction(def));
    }

    public KwlReturnObject buildNExecuteQuery(String initialQuery, HashMap<String, Object> requestParams) throws ServiceException {
        return buildNExecuteQuery(initialQuery, requestParams, "");
    }

    public KwlReturnObject buildNExecuteQuery(String initialQuery, HashMap<String, Object> requestParams, String quickSearch) throws ServiceException {
        List list = new ArrayList();

        String filter = "";
        //Get Filter String
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
        if (requestParams.containsKey("filter_names") && requestParams.containsKey("filter_params")) {
            filter_names = (ArrayList) requestParams.get("filter_names");
            filter_params = (ArrayList) requestParams.get("filter_params");
            //if(filter_names.size() != filter_params.size()) { //throw "size not same" exception}
            filter = StringUtil.filterQuery(filter_names, "where");
            int ind = filter.indexOf("("); // Insert in/not in params in filter query.
            if (ind > -1) {
                int index = Integer.valueOf(filter.substring(ind + 1, filter.indexOf(")")));
                filter = filter.replaceAll("(" + index + ")", filter_params.get(index).toString());
                filter_params.remove(index);
            }
        }

        String query = initialQuery + filter;
        //Add Advance Search Filter
        String conditionalQuery = "";
        if (requestParams.containsKey("ss") && !StringUtil.isNullOrEmpty(requestParams.get("ss").toString())) {
            String[] ssfieldnames = (String[]) requestParams.get("ss_names");
            String ss = (String) requestParams.get("ss");
            conditionalQuery = StringUtil.getSearchString(ss, (StringUtil.isNullOrEmpty(filter) ? " where " : " and "), ssfieldnames);
            try {
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(filter_params, ss, 1);
                StringUtil.insertParamSearchString(SearchStringMap);
            } catch (SQLException ex) {
                Logger.getLogger(StringUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
            query += conditionalQuery;
        }
        //Add Quick Search Filter
        if (!StringUtil.isNullOrEmpty(quickSearch)) {
            query += ((StringUtil.isNullOrEmpty(filter) && StringUtil.isNullOrEmpty(conditionalQuery)) ? " where " : " and ") + quickSearch;
        }

        //Add Order By Clause
        if (requestParams.get("order_by") != null && requestParams.get("order_type") != null) {
            ArrayList orderby = new ArrayList((List<String>) requestParams.get("order_by"));
            ArrayList ordertype = new ArrayList((List<Object>) requestParams.get("order_type"));
            //if(filter_names.size() != filter_params.size()) { //throw "size not same" exception}
            query += StringUtil.orderQuery(orderby, ordertype);
        }

        list = executeQuery(query, filter_params.toArray());
        int count = list.size();

        //Execute Paging Query
        boolean allflag = false;
        if (requestParams.containsKey("allflag") && requestParams.get("allflag") != null) {
            allflag = Boolean.parseBoolean(requestParams.get("allflag").toString());
        }
        if (!allflag) {
            if (requestParams.containsKey("start") && requestParams.get("start") != null && requestParams.containsKey("limit") && requestParams.get("limit") != null) {
                int start = Integer.parseInt(requestParams.get("start").toString());
                int limit = Integer.parseInt(requestParams.get("limit").toString());
                list = executeQueryPaging(query, filter_params.toArray(), new Integer[]{start, limit});
            }
        }

        return new KwlReturnObject(true, null, null, list, count);
    }

    public void deleteAll(Collection entities) throws ServiceException {
        getHibernateTemplate().deleteAll(entities);
    }

    public List find(String query) {
        return getHibernateTemplate().find(query);
    }

    public List executeQueryPaging(String hql,
            Object[] params, com.krawler.common.util.Paging paging) throws ServiceException {
        return executeQueryPaging(hql, params, new Integer[]{paging.getOffset(), paging.getLimit()});
    }

    public Object load(Class entityClass, Serializable id) {
        return getHibernateTemplate().load(entityClass, id);
    }
    public Map<String, Object> buildSqlDefaultFieldAdvSearch(JSONArray defaultSearchFieldArray, ArrayList params, String moduleid, ArrayList tableArray, String filterConjuctionCriteria) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            String conditionSQL = "";
            String searchJoin = "";

            String query = "from DefaultHeaderModuleJoinReference where module=?";
            List<DefaultHeaderModuleJoinReference> refHeader = executeQuery(query, moduleid);

            for (int i = 0; i < defaultSearchFieldArray.length(); i++) {
                JSONObject jsonobj = defaultSearchFieldArray.getJSONObject(i);

                String fieldId = jsonobj.getString("column");
                String searchText = jsonobj.getString(Constants.searchText).trim();

                String headerTableName = "";
                String headercolumnName = "";
                String refModule = "";
                String xtype = "";
                query = "from DefaultHeader where id=?";
                List<DefaultHeader> headerlist = executeQuery(query, fieldId);
                if (headerlist.size() > 0) {
                    DefaultHeader header = headerlist.get(0);
                    xtype = header.getXtype();
                    headerTableName = header.getDbTableName();
                    headercolumnName = header.getDbcolumnname();
                    String refTableName=header.getReftablename();
                    String refTableColumn=header.getReftabledatacolumn();
                    boolean isLineItem=header.isIslineitem();
                    String moduleTable = getModuleMainTable(moduleid);
                                        /*
                     Handle HQL search query 
                     */
                    if (isHqlQuery(moduleid)) {
                        headercolumnName = StringUtil.getRecordNameOfDefaultheader(header);
                        headerTableName = headerTableName + "Ref";
                    } else /*
                     Search JE Number from Payment,Note ,Invoice Reports
                     Need to search from JE table instead of respective transaction report
                     */ if ((xtype.equalsIgnoreCase("1") && (StringUtil.isNullOrEmpty(headerTableName) || (!StringUtil.isNullOrEmpty(refTableName) && !StringUtil.isNullOrEmpty(refTableColumn)))) || ((!StringUtil.isNullOrEmpty(refTableColumn)) && (refTableColumn.contains("(") && refTableColumn.contains(")")))) {   // Search Field from reference module
                        headerTableName = refTableName;
                        headercolumnName = refTableColumn;
                    } 
                    
                    if(!isHqlQuery(moduleid) && !StringUtil.isNullOrEmpty(moduleTable)){
                        if (isLineItem && !StringUtil.isNullOrEmpty(header.getDbTableName()) && !StringUtil.isNullOrEmpty(header.getReftablefk())) {
                            searchJoin += " left join " + header.getDbTableName() + " on " + header.getDbTableName() + "." + moduleTable + " = " + moduleTable + "." + header.getReftablefk() + " ";
                            
                            if (isLineItem && !StringUtil.isNullOrEmpty(header.getReftablename())) {
                                searchJoin += " left join " + header.getReftablename() + " on " + header.getReftablename() + "." + header.getReftablefk() + " = " + header.getDbTableName() + "." + header.getDbcolumnname() + " ";
                            }
                        }
                    }

                    refModule = header.getModule().getId();
                    
                } else {
                    if (moduleid.equalsIgnoreCase("" + Constants.Acc_Make_Payment_ModuleId) || moduleid.equalsIgnoreCase("" + Constants.Acc_Receive_Payment_ModuleId)) {
                        if (fieldId.equalsIgnoreCase("1234") && moduleid.equalsIgnoreCase("" + Constants.Acc_Make_Payment_ModuleId)) {
                            headercolumnName = "masterAgent.ID";
                            headerTableName = "gr";
                            refModule = "" + Constants.Acc_Make_Payment_ModuleId;
                            xtype = "" + 4;
                        } else if (fieldId.equalsIgnoreCase("1234") && moduleid.equalsIgnoreCase("" + Constants.Acc_Receive_Payment_ModuleId)) {
                            headercolumnName = "masterSalesPerson.ID";
                            headerTableName = "inv";
                            refModule = "" + Constants.Acc_Receive_Payment_ModuleId;
                            xtype = "" + 4;
                        }
                    }
                }
                
                if (!moduleid.equalsIgnoreCase(refModule)) { //for same module no need to add any of join 
                    searchJoin += StringUtil.getHeaderReferenceJoin(refHeader, tableArray, refModule);
                    for (DefaultHeaderModuleJoinReference reference : refHeader) { //used for finding where condition
                        if (reference.getRefModule().equalsIgnoreCase(refModule)) {
                            String refTableName = reference.getRefModuleTableName();
                            conditionSQL=StringUtil.getDefaultHeaderConditionString(conditionSQL,refTableName,headercolumnName,xtype,searchText,filterConjuctionCriteria,params);
                        }
                    }
                } else {
                    conditionSQL=StringUtil.getDefaultHeaderConditionString(conditionSQL,headerTableName,headercolumnName,xtype,searchText,filterConjuctionCriteria,params);
                }
                if (!conditionSQL.equals("")) {         // when conjuction criteria applied for multiple fields then need to append
                conditionSQL += ")";
                }
            }
            if (!conditionSQL.equals("")) {
                conditionSQL += ")";
            }
            map.put("searchjoin", searchJoin);
            map.put("condition", conditionSQL);

        } catch (Exception ex) {
            Logger.getLogger(StringUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return map;
    }
    
    public String getModuleMainTable(String moduleId) {
        String modulemaintable = "";
        switch (moduleId) {
            case Constants.Acc_Sales_Order_ModuleId+"":
                modulemaintable = "salesorder";
                break;
            case Constants.Acc_Purchase_Order_ModuleId+"":
                modulemaintable = "purchaseorder";
                break;
            case Constants.Acc_Purchase_Requisition_ModuleId+"":
                modulemaintable = "purchaserequisition";
                break;
            case Constants.Acc_Goods_Receipt_ModuleId+"":
                modulemaintable = "grorder";
                break;
            case Constants.Acc_Delivery_Order_ModuleId+"":
                modulemaintable = "deliveryorder";
                break;
            case Constants.Acc_Invoice_ModuleId+"":
                modulemaintable = "invoice";
                break;
            case Constants.Acc_Vendor_Invoice_ModuleId+"":
                modulemaintable = "goodsreceipt";
                break;
            case Constants.Acc_Sales_Return_ModuleId+"":
                modulemaintable = "salesreturn";
                break;
            case Constants.Acc_Purchase_Return_ModuleId+"":
                modulemaintable = "purchasereturn";
                break;
            case Constants.Acc_Customer_Quotation_ModuleId+"":
                modulemaintable = "quotation";
                break;
            case Constants.Acc_Product_Master_ModuleId+"":
                modulemaintable = "product";
                break;
        }
        return modulemaintable;

    }
    
    /*
    Function used to add those module for which HQL query are used to fetch data
    */
    public boolean isHqlQuery(String moduleid) {
        if (moduleid.equalsIgnoreCase("" + Constants.Acc_Make_Payment_ModuleId) || moduleid.equalsIgnoreCase("" + Constants.Acc_Receive_Payment_ModuleId)
                || moduleid.equalsIgnoreCase(Constants.Vendor_MODULE_UUID) || moduleid.equalsIgnoreCase(Constants.CUSTOMER_MODULE_UUID)
                || moduleid.equalsIgnoreCase("" + Constants.Acc_Product_Master_ModuleId) || moduleid.equalsIgnoreCase("" + Constants.Acc_GENERAL_LEDGER_ModuleId)
                || moduleid.equalsIgnoreCase(Constants.Account_ModuleId) || moduleid.equalsIgnoreCase("" + Constants.Labour_Master) || moduleid.equalsIgnoreCase("" + Constants.MRP_Machine_Management_ModuleId)
                || moduleid.equalsIgnoreCase("" + Constants.MRP_WORK_CENTRE_MODULEID) || moduleid.equalsIgnoreCase("" + Constants.MRP_WORK_ORDER_MODULEID ) || moduleid.equalsIgnoreCase("" + Constants.MRP_JOB_WORK_MODULEID)
                || moduleid.equalsIgnoreCase("" + Constants.MRP_RouteCode) || moduleid.equalsIgnoreCase("" + Constants.MRP_Contract)) {
            return true;
        } else {
            return false;
        }
    }
    public int executeSQLUpdate(final String hql, final Object[] params)
            throws ServiceException {
        int numRow = 0;
        numRow = (Integer) getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session session) {
                int numRows = 0;
                Query query = session.createSQLQuery(hql);
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        query.setParameter(i, params[i]);
                    }
                }
                numRows = query.executeUpdate();
                return numRows;
            }
        });
        return numRow;
        
    }

    public int executeSQLUpdate(String hql, Object param)
            throws ServiceException {
        Object[] params = {param};
        return executeSQLUpdate( hql, params);
    }

    public int executeSQLUpdate( String sql)
            throws ServiceException {
        return executeSQLUpdate( sql, null);
    }
    
    public Object merge(Object entity) throws ServiceException {
        return getHibernateTemplate().merge(entity);
    }
    
    public List findByNamedParam(String queryString, String paramName, Object value) throws ServiceException {
        return getHibernateTemplate().findByNamedParam(queryString, paramName, value);
    }
    
    public void evictObj(Object currentSessionObject) {
        getHibernateTemplate().evict(currentSessionObject);
    }
    
    public Object executeQueryWithProjection(final Class c, final String[] columnNames, final Map<String, Object> paramMap) throws ServiceException {
        Object results = null;
        final Map<String, String> aliasMap = (paramMap.containsKey("aliasMap") && paramMap.get("aliasMap") instanceof Map) ? (Map<String, String>) paramMap.get("aliasMap") : null;
        results = getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session session) {
                
                Criteria  criteria  = session.createCriteria(c);
                if (aliasMap != null) {
                    paramMap.remove("aliasMap");
                    for (Map.Entry<String, String> entry : aliasMap.entrySet()) {
                        criteria.createAlias(entry.getKey(), entry.getValue());
                    }
                }
                for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                    criteria.add(Restrictions.eq( entry.getKey(), entry.getValue()));
                }
                ProjectionList pList = Projections.projectionList();
                for (String projection : columnNames) {
                    pList.add(Projections.property(projection));
                }
                    return criteria.setProjection(pList).uniqueResult();
//                return criteria.setProjection(pList).list();
            }
        });
// Single field like boolean returns a Class Cast Exception as a single field cant be converted to Object-Array hence returning simply Object 
//        Object[] res = (Object[]) results; 
        return results;
    }

    /**
     * This method will be used to get collection using projections.
     *
     * @param c Class object
     * @param columnNames get column specific data
     * @param paramMap Condition which needs to be added in query.
     * @return
     * @throws ServiceException
     */
    public List executeCollectionQueryWithProjections(final Class c, final String[] columnNames, final Map<String, Object> paramMap) throws ServiceException {
        List results = (List) getHibernateTemplate().execute(new HibernateCallback() {

            @Override
            public List doInHibernate(Session session) {
                Criteria criteria = session.createCriteria(c);
                for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                    criteria.add(Restrictions.eq(entry.getKey(), entry.getValue()));
                }
                ProjectionList pList = Projections.projectionList();
                for (String projection : columnNames) {
                    pList.add(Projections.property(projection));
                }
                return criteria.setProjection(pList).list();
            }
        });
        return results;
    }
}
