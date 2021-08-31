/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.packaging.impl;

import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.UOMNature;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.inventory.model.packaging.PackagingDAO;
import com.krawler.spring.common.KwlReturnObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vipin Gupta
 */
public class PackagingDAOImpl extends BaseDAO implements PackagingDAO {

    @Override
    public void saveOrUpdate(Object object) {
        try {
            save(object);
        } catch (ServiceException ex) {
            Logger.getLogger(PackagingDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Packaging getPackaging(String packagingId) {
        return (Packaging) get(Packaging.class, packagingId);
    }

    @Override
    public KwlReturnObject getProductBaseUOMRate(HashMap<String, Object> request) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {
            String query = "from UOMSchema";
            String uomschematypeid = (String) request.get("uomschematypeid");
            String currentuomid = (String) request.get("currentuomid");
            boolean carryin = request.containsKey("carryin") ? (Boolean) request.get("carryin") : false;
            String companyid = (String) request.get("companyid");
            String condition = " where company.companyID=?  ";
            ArrayList params = new ArrayList();
            params.add(companyid);
            if (!StringUtil.isNullOrEmpty(currentuomid)) {
                condition += " and uomnature=? ";
                if (carryin) {//for Purchas=ture ans Sales=Flase
                    params.add(UOMNature.Transfer);
                    condition += " and transferuom.ID=? ";
                    params.add(currentuomid);
                } else {
                    params.add(UOMNature.Stock);
                    condition += " and orderuom.ID=? ";
                    params.add(currentuomid);
                }
            }
            if (!StringUtil.isNullOrEmpty(uomschematypeid)) {
                condition += " and uomschematype.ID=? ";
                params.add(uomschematypeid);
            }
            query += condition;
            list = executeQuery( query, params.toArray());
            count = list.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("getPrice : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
}
