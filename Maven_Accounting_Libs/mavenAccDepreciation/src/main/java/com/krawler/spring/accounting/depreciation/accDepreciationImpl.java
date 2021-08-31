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

import com.krawler.common.admin.Company;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.Asset;
import com.krawler.hql.accounting.DepreciationDetail;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.spring.common.KwlReturnObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author krawler
 */
public class accDepreciationImpl extends BaseDAO implements accDepreciationDAO {

    public KwlReturnObject addDepreciationDetail(HashMap<String, Object> ddMap) throws ServiceException {
        List list = new ArrayList();
        try {
            DepreciationDetail dd = new DepreciationDetail();
            dd = buildDepreciation(dd, ddMap);
            save(dd);
            list.add(dd);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addDepreciationDetail : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Depreciation Detail has been added successfully", null, list, list.size());
    }

    public KwlReturnObject updateDepreciationDetail(HashMap<String, Object> ddMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String id = (String) ddMap.get("id");
            DepreciationDetail dd = (DepreciationDetail) get(DepreciationDetail.class, id);
            if (dd != null) {
                dd = buildDepreciation(dd, ddMap);
                saveOrUpdate(dd);
            }
            list.add(dd);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateDepreciationDetail : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Depreciation Detail has been updated successfully", null, list, list.size());
    }

    public DepreciationDetail buildDepreciation(DepreciationDetail dd, HashMap<String, Object> ddMap) {
        if (ddMap.containsKey("period")) {
            dd.setPeriod((Integer) ddMap.get("period"));
        }
        if (ddMap.containsKey("accountid")) {
            Account account = ddMap.get("accountid") == null ? null : (Account) get(Account.class, (String) ddMap.get("accountid"));
            dd.setAccount(account);
        }
        if (ddMap.containsKey("jeid")) {
            JournalEntry je = ddMap.get("jeid") == null ? null : (JournalEntry) get(JournalEntry.class, (String) ddMap.get("jeid"));
            dd.setJournalEntry(je);
        }
        if (ddMap.containsKey("companyid")) {
            Company company = ddMap.get("companyid") == null ? null : (Company) get(Company.class, (String) ddMap.get("companyid"));
            dd.setCompany(company);
        }
        if (ddMap.containsKey("periodamount")) {
            dd.setPeriodAmount((Double) ddMap.get("periodamount"));
        }
        if (ddMap.containsKey("accamount")) {
            dd.setAccumulatedAmount((Double) ddMap.get("accamount"));
        }
        if (ddMap.containsKey("netbookvalue")) {
            dd.setNetBookValue((Double) ddMap.get("netbookvalue"));
        }
        return dd;
    }

    public KwlReturnObject getDepreciationFromJE(String jeid, String companyid) throws ServiceException {
        String selQuery = "from DepreciationDetail where journalEntry.ID=? and company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{jeid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getDepreciation(HashMap<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from DepreciationDetail ";

        if (filterParams.containsKey("period")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "period=?";
            params.add(filterParams.get("period"));
        }
        if (filterParams.containsKey("jeid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "journalEntry.ID=?";
            params.add(filterParams.get("jeid"));
        }
        if (filterParams.containsKey("accountid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "account.ID=?";
            params.add(filterParams.get("accountid"));
        }
        if (filterParams.containsKey("companyid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "company.companyID=?";
            params.add(filterParams.get("companyid"));
        }

        condition += (condition.length() == 0 ? " where " : " and ") + "journalEntry.deleted=false";

        query += condition;
//        query="from DepreciationDetail where journalEntry.ID=? and company.companyID=?";
//        query="from DepreciationDetail where period=? and account.ID=? and company.companyID=?";
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    @Override
    public KwlReturnObject deleteDepreciationJE(String je) throws ServiceException {
        String selQuery = "delete from DepreciationDetail where journalEntry.ID=?";
        int count = executeUpdate( selQuery, new Object[]{je});
        return new KwlReturnObject(true, "", null, null, count);
    }

    public KwlReturnObject addAssetDetail(HashMap<String, Object> assetMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String id = (String) assetMap.get("id");
            Asset ass = (Asset) get(Asset.class, id);
            if (ass != null) {
                ass = buildAsset(ass, assetMap);
                saveOrUpdate(ass);
            } else {
                ass = buildAsset(new Asset(), assetMap);
                save(ass);
            }
            list.add(ass);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addAssetDetail : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Asset Detail has been saved/updated successfully", null, list, list.size());
    }

    public Asset buildAsset(Asset ass, HashMap<String, Object> assetMap) {
        if (assetMap.containsKey("id")) {
            Account account = assetMap.get("id") == null ? null : (Account) get(Account.class, (String) assetMap.get("id"));
            ass.setAccount(account);
        }
        if (assetMap.containsKey("purchaseJe")) {
            JournalEntry je = assetMap.get("purchaseJe") == null ? null : (JournalEntry) get(JournalEntry.class, (String) assetMap.get("purchaseJe"));
            ass.setPurchaseJe(je);
        }
        if (assetMap.containsKey("deleteJe")) {
            JournalEntry je = assetMap.get("deleteJe") == null ? null : (JournalEntry) get(JournalEntry.class, (String) assetMap.get("deleteJe"));
            ass.setDeleteJe(je);
        }
        if (assetMap.containsKey("companyid")) {
            Company company = assetMap.get("companyid") == null ? null : (Company) get(Company.class, (String) assetMap.get("companyid"));
            ass.setCompany(company);
        }
        if (assetMap.containsKey("isSale")) {
            ass.setIsSale((Boolean) assetMap.get("isSale"));
        }
        if (assetMap.containsKey("isWriteOff")) {
            ass.setIsWriteOff((Boolean) assetMap.get("isWriteOff"));
        }
        if (assetMap.containsKey("depreciationMethod")) {
            if (assetMap.get("depreciationMethod") != null) {
                ass.setDepreciationMethod(Integer.parseInt(assetMap.get("depreciationMethod").toString()));
            }
        }
        return ass;
    }

    public KwlReturnObject getAsset(HashMap<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from Asset ";

        if (filterParams.containsKey("id")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "account.ID=?";
            params.add(filterParams.get("id"));
        }
        if (filterParams.containsKey("purchaseJe")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "purchaseJe.ID=?";
            params.add(filterParams.get("purchaseJe"));
        }
        if (filterParams.containsKey("deleteJe")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "deleteJe.ID=?";
            params.add(filterParams.get("deleteJe"));
        }
        if (filterParams.containsKey("companyid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "company.companyID=?";
            params.add(filterParams.get("companyid"));
        }

        query += condition;
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
}
