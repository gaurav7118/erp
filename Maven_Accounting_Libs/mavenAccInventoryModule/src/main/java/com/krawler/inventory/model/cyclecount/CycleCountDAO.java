/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.cyclecount;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.frequency.Frequency;
import com.krawler.inventory.model.store.Store;
import com.krawler.utils.json.base.JSONException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vipin Gupta
 */
public interface CycleCountDAO {

    public Object getObject(Class className, Serializable object);

    public List<CycleCountCalendar> getCycleCountCalendarForMonth(Company company, Date fromDate, Date toDate) throws ServiceException;

    public void saveOrUpdate(Object object) throws ServiceException;

    public List<Frequency> getAllFrequencies() throws ServiceException;

    public CycleCountCalendar getCycleCountCalendar(Company company, Date date) throws ServiceException;

    public List<CycleCount> getCycleCountReport(Store store, Date businessDate, String searchString, Paging paging) throws ServiceException;

    public List<CycleCount> getCycleCountReport(Company company, Store store, Date fromDate, Date toDate, String searchString, Paging paging,Map<String, Object> requestParams) throws ServiceException, JSONException, ParseException;

    public List<Object[]> getCCCalendarProducts(Company company, Date date);
    
    public List<Object[]> getCCCalendarExtraProducts(Company company, Date date) throws ServiceException;
    
    public List<Object[]> getCycleCountDraftExtraProducts(Company company, Date date) throws ServiceException;

    public Map<String, Double> getProductsSystemQty(List<String> productList, Store store);

    public void removeCycleCountForProduct(Product product, Store store, Date businessDate, boolean isDraft) throws ServiceException;

    public List<Object[]> getAllCycleCountStatusReport(String companyId, Date businessDate, Paging paging) throws ServiceException;

    public List<Object[]> getCycleCountStatusReport(String userId, Date businessDate, Paging paging) throws ServiceException;

    public boolean isCycleCountDone(String storeId, Date businessDate) throws ServiceException;

    public boolean isCycleCountDoneForProduct(String storeId, Date businessDate, String productId) throws ServiceException;

    public Date getLastCycleCountDate(String storeId) throws ServiceException;

    public List<Object[]> getCycleCountDraftList(String userId, String searchString, Paging paging,String inspectionStore,String repairStore) throws ServiceException;

    public List<CycleCount> getCycleCountDraftItemList(String storeId, Date businessDate) throws ServiceException;
}
