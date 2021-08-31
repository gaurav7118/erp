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

import com.krawler.common.admin.Company;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.DefaultTerm;
import com.krawler.hql.accounting.DiscountMaster;
import com.krawler.hql.accounting.Term;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author krawler
 */
public class accTermImpl extends BaseDAO implements accTermDAO {

    public KwlReturnObject addTerm(HashMap<String, Object> termMap) throws ServiceException {
        List list = new ArrayList();
        try {
            Term term = new Term();
            term = buildTerm(term, termMap);
            save(term);
            list.add(term);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addTerm : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Term has been added successfully", null, list, list.size());
    }

    public KwlReturnObject updateTerm(HashMap<String, Object> termMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String termid = (String) termMap.get("termid");
            Term term = (Term) get(Term.class, termid);
            if (term != null) {
                term = buildTerm(term, termMap);
                saveOrUpdate(term);
            }
            list.add(term);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateTerm : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Term has been updated successfully", null, list, list.size());
    }

    @Override
    public KwlReturnObject updateSrNoTerm(HashMap<String, Object> termSrMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String termid = (String) termSrMap.get("termid");
            Term term = (Term) get(Term.class, termid);
            if (term != null) {
                term = buildSrNoTerm(term, termSrMap);
                saveOrUpdate(term);
            }
            list.add(term);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateTerm : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Term has been updated successfully", null, list, list.size());
    }

    public Term buildTerm(Term term, HashMap<String, Object> termMap) {
        if (termMap.containsKey("srno")) {
            term.setSrno((Integer) termMap.get("srno"));
        }
        if (termMap.containsKey("termdays")) {
            term.setTermdays((Integer) termMap.get("termdays"));
        }
        if (termMap.containsKey("termname")) {
            term.setTermname((String) termMap.get("termname"));
        }
        if (termMap.containsKey("isdefaultcreditterm")) {
            term.setIsdefault((Boolean) termMap.get("isdefaultcreditterm"));
        }
        if (termMap.containsKey("crmtermid")) {
            term.setCrmtermid((String) termMap.get("crmtermid"));
        }
        if (termMap.containsKey("companyid")) {
            Company company = termMap.get("companyid") == null ? null : (Company) get(Company.class, (String) termMap.get("companyid"));
            term.setCompany(company);
        }
        if (termMap.containsKey("discountname")) {
            term.setDiscountName((DiscountMaster) termMap.get("discountname"));
        }
        if (termMap.containsKey("applicabledays") && termMap.get("applicabledays") != null) {
            term.setApplicableDays((int)termMap.get("applicabledays"));
        } else {
            term.setApplicableDays(-1);
        }
        
        return term;
    }
    
    public Term buildSrNoTerm(Term term, HashMap<String, Object> termMap) {

        if (termMap.containsKey("srno")) {
            term.setSrno((Integer) termMap.get("srno"));
        }

        return term;
    }

    

    public KwlReturnObject getTerm(HashMap<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from Term ";

        try {
            if (filterParams.containsKey("companyid")) {
                condition += (condition.length() == 0 ? " where " : " and ") + "company.companyID=?";
                params.add(filterParams.get("companyid"));
            }
            if (filterParams.containsKey("isdefault")) {
                condition += (condition.length() == 0 ? " where " : " and ") + "isdefault = ?";
                params.add(filterParams.get("isdefault"));
            }
            if (filterParams.containsKey("termdays")) {
                condition += (condition.length() == 0 ? " where " : " and ") + "termdays = ?";
                params.add(filterParams.get("termdays"));
            }
            if (filterParams.containsKey("termname")) {
                condition += (condition.length() == 0 ? " where " : " and ") + "termname = ?";
                params.add(filterParams.get("termname"));
            }
            if (filterParams.containsKey("crmtermid")) {
                condition += (condition.length() == 0 ? " where " : " and ") + " crmtermid = ? ";
                params.add(filterParams.get("crmtermid"));
            }
            if (filterParams.containsKey("discountname")) {
                condition += (condition.length() == 0 ? " where " : " and ") + " discountName = ? ";
                params.add(filterParams.get("discountname"));
            } 
//            else {
//                condition += (condition.length() == 0 ? " where " : " and ") + " discountName is null ";
//            }
            if (filterParams.containsKey("applicabledays")) {
                condition += (condition.length() == 0 ? " where " : " and ") + " applicableDays = ? ";
                params.add(filterParams.get("applicabledays"));
            }
//            else {
//                condition += (condition.length() == 0 ? " where " : " and ") + " applicableDays = -1 ";
//            }
            if (filterParams.containsKey("cash_Invoice")) {
                if ((filterParams.get("cash_Invoice").toString()).equalsIgnoreCase("false")) {
                    condition += " and termdays != -1";
                }
            }
            if (filterParams.containsKey("ss") && filterParams.get("ss") != null) {
                String ss = filterParams.get("ss").toString();
                ss = ss.replaceAll("%", "////");		// issue for search '% and _'
                ss = ss.replaceAll("_", "////");
                if (!StringUtil.isNullOrEmpty(ss)) {
                    String[] searchcol = new String[]{"termname"};
                    Map map = StringUtil.insertParamSearchStringMap(params, ss, 1);
                    StringUtil.insertParamSearchString(map);
                    String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    condition += searchQuery;
                }
            }

            query += condition + " order by srno";
            returnList = executeQuery(query, params.toArray());
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateTerm : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject deleteTerm(String termid, String companyid) throws ServiceException {
        String delQuery = "delete from Term c where c.ID=? and c.company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{termid, companyid});
        return new KwlReturnObject(true, "Term entry has been deleted successfully.", null, null, numRows);
    }

//    @Transactional(propagation = Propagation.REQUIRED)
    public void copyTerms(String companyid) throws ServiceException {
        try {
            String query = "from DefaultTerm";
            List list = executeQuery( query);
            Iterator iter = list.iterator();
            Company company = (Company) get(Company.class, companyid);
            while (iter.hasNext()) {
                DefaultTerm dt = (DefaultTerm) iter.next();
                Term term = new Term();
                term.setCompany(company);
                term.setTermdays(dt.getTermdays());
                term.setTermname(dt.getTermname());
                save(term);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("copyTerms : " + ex.getMessage(), ex);
        }
    }
    
    public KwlReturnObject getPOTerm(String termid, String companyid) throws ServiceException{
        List list = new ArrayList();
        String q = "from PurchaseOrder po where po.term.ID=? and po.company.companyID=?";
        list = executeQuery( q, new Object[]{termid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getSOTerm(String termid, String companyid) throws ServiceException{
        List list = new ArrayList();
        String q = "from SalesOrder so where so.term.ID=? and so.company.companyID=?";
        list = executeQuery( q, new Object[]{termid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getPITerm(String termid, String companyid) throws ServiceException{
        List list = new ArrayList();
        String q = "from GoodsReceipt pi where pi.termid.ID=? and pi.company.companyID=?";
        list = executeQuery( q, new Object[]{termid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getSITerm(String termid, String companyid) throws ServiceException{
        List list = new ArrayList();
        String q = "from Invoice inv where inv.termid.ID=? and inv.company.companyID=?";
        list = executeQuery( q, new Object[]{termid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    } 
    
    public KwlReturnObject getVQTerm(String termid, String companyid) throws ServiceException{
        List list = new ArrayList();
        String q = "from VendorQuotation vq where vq.term.ID=? and vq.company.companyID=?";
        list = executeQuery( q, new Object[]{termid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    } 
    
    public KwlReturnObject getCQTerm(String termid, String companyid) throws ServiceException{
        List list = new ArrayList();
        String q = "from Quotation q where q.term.ID=? and q.company.companyID=?";
        list = executeQuery( q, new Object[]{termid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getCustomerTerm(String termid, String companyid) throws ServiceException{
        List list = new ArrayList();
        String q = "from Customer c where c.creditTerm.ID=? and c.company.companyID=?";
        list = executeQuery( q, new Object[]{termid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getVendorTerm(String termid, String companyid) throws ServiceException{
        List list = new ArrayList();
        String q = "from Vendor v where v.debitTerm.ID=? and v.company.companyID=?";
        list = executeQuery( q, new Object[]{termid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    /**
     * ERM-507.
     *Get Line level term details
     * @param nObject
     * @return
     * @Desc : Get GST Term information
     * @throws ServiceException
     */
    public KwlReturnObject getGSTTermDetails(JSONObject nObject) throws ServiceException {
        ArrayList params = new ArrayList();
        String company = nObject.optString("companyid");
        params.add(company);
        String condition = "";
        if (nObject.has("termname") && !StringUtil.isNullOrEmpty(nObject.optString("termname"))) {
            condition += " and term in (" + nObject.optString("termname") + ")";
//            params.add(nObject.optString("termname"));
}
        if (nObject.has("termType")) {
            condition += " and termType=?";
            params.add(nObject.optInt("termType"));
        }
        if (nObject.has("isInput")) {
            condition += " and salesOrPurchase=?";
            params.add(nObject.optBoolean("isInput"));
        }
        if (nObject.has("defaultTermId")) {
            condition += " and defaultTerms.id=?";
            params.add(nObject.optString("defaultTermId"));
        }
        String selectOnlyTerm = "";
        if (nObject.optBoolean("onlyTermTaxName", false)) {
            selectOnlyTerm = "select term " ;
        }
        String query = selectOnlyTerm + " from LineLevelTerms where company.companyID = ? " + condition;
        List list = executeQuery(query, params.toArray());
        return new KwlReturnObject(true, null, null, list, list.size());
    }
}
