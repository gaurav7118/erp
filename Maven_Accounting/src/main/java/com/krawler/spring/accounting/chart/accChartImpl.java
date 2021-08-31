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

package com.krawler.spring.accounting.chart;

import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 *
 * @author krawler
 */
public class accChartImpl extends BaseDAO implements accChartDAO {

    @Override
    public KwlReturnObject getTopCustomerCharData(HashMap requestParam) throws ServiceException {
            ArrayList params = new ArrayList();
            List returnList = new ArrayList();
            params.add(requestParam.get("companyid"));
            Boolean flag = (Boolean) requestParam.get("withinventory");
            String query = "";
            if (flag) {
                query = "select sum(inv.customerEntry.amount/inv.exchangeRateDetail.exchangeRate) as invoiceamount," + "acc.name, " + "acc.id" + " from Invoice as inv " + ", JournalEntryDetail as jed, Account as acc where inv.deleted=false and jed.ID=inv.customerEntry.ID and jed.account.ID=acc.ID" + " and inv.company.companyID=?  group by acc.name ";
            } else {
                query = "select sum(inv.customerEntry.amount/inv.exchangeRateDetail.exchangeRate) as invoiceamount," + "acc.name, " + "acc.id" + " from BillingInvoice as inv " + ", JournalEntryDetail as jed, Account as acc where inv.deleted=false and jed.ID=inv.customerEntry.ID and jed.account.ID=acc.ID" + " and inv.company.companyID=?  group by acc.name ";
            }
            returnList = executeQuery( query, params.toArray());
       
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getTopVendorsChartData(HashMap requestParam) throws ServiceException {
            ArrayList params = new ArrayList();
            List returnList = new ArrayList();
            params.add(requestParam.get("companyid"));
            String query = "select sum(inv.vendorEntry.amount/inv.exchangeRateDetail.exchangeRate) as invoiceamount," +
                    "acc.name, "+
                    " acc.id"+
                    " from GoodsReceipt as inv "+
                    ", JournalEntryDetail as jed, Account as acc where inv.deleted=false and jed.ID=inv.vendorEntry.ID and jed.account.ID=acc.ID"+
                    " and inv.company.companyID=?  group by acc.name ";
            returnList = executeQuery( query, params.toArray());

        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

}
