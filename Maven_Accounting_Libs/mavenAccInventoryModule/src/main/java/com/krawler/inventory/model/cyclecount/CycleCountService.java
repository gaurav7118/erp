/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.cyclecount;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Paging;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.frequency.Frequency;
import com.krawler.inventory.model.store.Store;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vipin Gupta
 */
public interface CycleCountService {

    public CycleCountCalendar getCycleCountCalendar(Company company, Date date) throws ServiceException;

    public List<CycleCountCalendar> getCycleCountCalendarForMonth(Company company, Date date) throws ServiceException;

    public List<CycleCountCalendar> getCycleCountCalendarForDate(Company company, Date date) throws ServiceException;

    public List<CycleCountCalendar> getDefaultCalendarForMonth(Company company, Date date) throws ServiceException;

    public CycleCountCalendar getDefaultCalendar(Company company, Date date) throws ServiceException;

    public void addOrUpdateCCCalendar(CycleCountCalendar cycleCountCalendar) throws ServiceException;

    /**
     * This method gives all Frequency Objects of Systems
     *
     * @return Map contains Key as id of Frequency and Value as Frequency Object
     * @throws ServiceException
     */
    public Map<Integer, Frequency> getAllFrequencyMap() throws ServiceException;

    /**
     *
     * @param products
     * @param store
     * @return Map with productid as Key and system quantity for the Product as
     * Value
     * @throws ServiceException
     */
    public Map<String, Double> getProductSystemQty(List<String> products, Store store) throws ServiceException;

    /**
     * This method gives products for given business which matches the frequency
     * for cycle count calendar which are countable
     *
     * @param company
     * @param date
     * @return List of object Array, Object Array contains product fields value
     * [0]- ProductUUID, [1]-ProductCode, [2]-ProductName,[3]-
     * isBatchForProduct,[4]-isSerialForProduct, [5]- CasingUomName,
     * [6]-InnerUomName,[7]-StockUomName, [8]-CasingUomValue,[9]- InnerUomValue,
     * [10]- StockUomValue, [11]-isRowForProduct, [12]-isRackForProduct,
     * [13]-isBinForProduct
     *
     * @throws ServiceException
     */
    public List<Object[]> getCycleCountProducts(Company company, Date date) throws ServiceException;
    
    public List<Object[]> getCycleCountExtraProducts(Company company, Date date) throws ServiceException;
    
    public List<Object[]> getCycleCountDraftExtraProducts(Company company, Date date) throws ServiceException;

    public List<CycleCount> getCycleCountReport(Store store, Date businessDate, String searchString, Paging paging) throws ServiceException;

    public List<CycleCount> getCycleCountReport(Company company, Store store, Date fromDate, Date toDate, String searchString, Paging paging,Map<String, Object> requestParams) throws ServiceException, JSONException, ParseException;

    public void addCycleCountRequest(User user, CycleCount cycleCountRequest) throws ServiceException;

    public void addStockAdjustmentForCycleCount(User user, CycleCount cycleCountRequest, List<CycleCountAdjustment> ccAdjustmentList, String stockAdjustmentNo, Store qaStore, Store repairStore, List<MovedSerialMailDetail> movedSerialmailDetails, ExtraCompanyPreferences ecp,Map<String, Object> globalParams, Map<String, String> serialSkuMap) throws ServiceException;

    /**
     * This method delete last cycle count entry for a particular product, store
     * and cyclecount businessdate if exists.
     *
     * @param product (required)
     * @param store (required)
     * @param businessDate (required)
     * @param isDraft
     * @throws ServiceException , IllegalArgumentException
     */
    public void removeCycleCountForProduct(Product product, Store store, Date businessDate, boolean isDraft) throws ServiceException;

    /**
     * This method is overridden with argument isDraft has always false.
     *
     * @param product (required)
     * @param store (required)
     * @param businessDate (required)
     * @see removeCycleCountForProduct(Product product, Store store, Date
     * businessDate, boolean isDraft)
     * @throws ServiceException , IllegalArgumentException
     */
    public void removeCycleCountForProduct(Product product, Store store, Date businessDate) throws ServiceException;

    /**
     * This method remove all cycle count draft for a particular store and
     * cyclecount businessdate if exists.
     *
     * @param store (required)
     * @param businessDate (required)
     * @throws ServiceException , IllegalArgumentException
     */
    public void removeAllCycleCountDraft(Store store, Date businessDate) throws ServiceException;

    /**
     * This method gives total item count in store consider all cyclecounts for
     * selected business date for a company.
     *
     * @param company
     * @param businessDate
     * @param paging @returnList of object Array, Object Array contains product
     * fields value [0]-storeCode, [1]-storeDesc,[2]-itemCount,
     * @throws ServiceException
     */
    public List<Object[]> getAllCycleCountStatusReport(String companyId, Date businessDate, Paging paging) throws ServiceException;

    public List<Object[]> getCycleCountStatusReport(String userId, Date businessDate, Paging paging) throws ServiceException;

    public boolean isCycleCountDoneForProduct(String storeId, Date businessDate, String productId) throws ServiceException;

    public boolean isCycleCountDone(String storeId, Date businessDate) throws ServiceException;

    public Date getLastCycleCountDate(String storeId) throws ServiceException;

    public List<CycleCountAdjustment> getCycleCountCurrentAdjustment(CycleCount cyclecount) throws ServiceException;

    public void sendMovedSerialsMail(User user, List<MovedSerialMailDetail> movedSerialmailDetails, Store cycleCountStore, String cycleCountTransactionNo);
    
    public List<Object[]> getCycleCountDraftList(String userId, String searchString, Paging paging,ExtraCompanyPreferences ecp) throws ServiceException;

    public List<CycleCount> getCycleCountDraftList(String storeId, Date businessDate) throws ServiceException;
    
    public void updateCCProductJArray(List<Object[]> ccProducts, Map<Product, Double> dateWiseStockMap, Map<String, CycleCount> ccDraftMap, JSONArray jArray, Map<Product, Double> currentProductStockMap) throws JSONException ;
     
    public JSONObject addCycleCountRequest(JSONObject paramJobj) throws JSONException ;

    public JSONArray getCycleCountReport(JSONObject paramJobj,HashMap<String,Object> requestParams);

}
