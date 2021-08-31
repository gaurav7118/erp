/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.accounting.integration.avalara;

import com.krawler.accounting.integration.common.IntegrationCommonServiceImpl;
import com.krawler.accounting.integration.common.IntegrationConstants;
import com.krawler.accounting.integration.common.IntegrationUtil;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.lowagie.text.pdf.codec.Base64;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class AvalaraIntegrationUtil extends IntegrationUtil {

    public Map<String, String> createHeadersMap(JSONObject integrationAccountDetails) {
        Map<String, String> headersMap = new HashMap<>();
        String accountNumber = integrationAccountDetails.optString(IntegrationConstants.accountNumber);
        String licenseKey = integrationAccountDetails.optString(IntegrationConstants.licenseKey);
        String authorization = "Basic " + Base64.encodeBytes((accountNumber + ":" + licenseKey).getBytes());//Authorization header for authentication
        headersMap.put("Authorization", authorization);
        headersMap.put("X-Avalara-Client", IntegrationConstants.x_Avalara_Client);
        headersMap.put("X-Avalara-UID", IntegrationConstants.x_Avalara_UID);
        headersMap.put("Content-type", "application/json");
        headersMap.put("accept", "text/json");
        return headersMap;
    }

    public Map<String, Object> createOtherReqPropertiesMap() {
        Map<String, Object> otherReqPropertiesMap = new HashMap();
        otherReqPropertiesMap.put(IntegrationConstants.doOutput, true);
        return otherReqPropertiesMap;
    }

    /**
     * Below method creates payload JSON for CreateOrAdjustTransaction request
     *
     * @param requestJobj
     * @param integrationAccountDetails
     * @return
     * @throws JSONException
     */
    public JSONObject getPayloadForCreateOrAdjustTransaction(JSONObject requestJobj, JSONObject integrationAccountDetails) throws JSONException, AccountingException {
        JSONObject returnJobj = new JSONObject();
        String productRecord = requestJobj.optString(Constants.detail);
        if (!StringUtil.isNullOrEmpty(productRecord)) {
            JSONObject returnJobjTemp = new JSONObject();
            JSONObject lineDetailsJobj = getLineDetails(requestJobj, productRecord);
            JSONArray lineDetailsJarr = lineDetailsJobj.optJSONArray(Constants.RES_data) != null ? lineDetailsJobj.optJSONArray(Constants.RES_data) : new JSONArray();
            if (lineDetailsJarr.length() != 0) {
                returnJobjTemp.put("lines", lineDetailsJarr);//JSONArray containing line details of transaction
                returnJobjTemp.put("companyCode", integrationAccountDetails.optString(IntegrationConstants.userName));

                boolean isCommit = requestJobj.optBoolean(IntegrationConstants.commit, false);
                returnJobjTemp.put(IntegrationConstants.commit, isCommit);//Flag to indicate whether taxes are to be committed on AvaTax side or not
                
                /**
                 * All Transaction at Avalara side perform in base currency
                 * So we pass baseCurrencyCode As currencyCode for all avalara transaction
                 */
                String baseCurrencyCode = requestJobj.optString("baseCurrencyCode");
                returnJobjTemp.put("currencyCode", baseCurrencyCode);
                String exchangeRate = requestJobj.optString("exchangeRate", null);
                returnJobjTemp.put("exchangeRate", exchangeRate);
            
                String moduleid = requestJobj.optString(Constants.moduleid);
                if (StringUtil.equal(moduleid, String.valueOf(Constants.Acc_Invoice_ModuleId))) {//For CI
                    if (isCommit) {
                        returnJobjTemp.put(Constants.type, "SalesInvoice");
                    } else {
                        returnJobjTemp.put(Constants.type, "SalesOrder");
                    }
                } else if (StringUtil.equal(moduleid, String.valueOf(Constants.Acc_Sales_Return_ModuleId))) {//For SR
                    if (isCommit) {
                        returnJobjTemp.put(Constants.type, "ReturnInvoice");
                    } else {
                        returnJobjTemp.put(Constants.type, "ReturnOrder");
                    }
                } else {//For DO, SO, and CQ
                    returnJobjTemp.put(Constants.type, "SalesOrder");
                }

                try {
                    DateFormat df = authHandler.getDateOnlyFormat();
                    DateFormat df1 = new SimpleDateFormat(Constants.yyyyMMdd);
                    String billdate = df1.format(df.parse(requestJobj.optString(Constants.BillDate)));
                    returnJobjTemp.put("date", billdate);//Transaction date
                } catch (SessionExpiredException | ParseException ex) {
                    Logger.getLogger(IntegrationCommonServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    throw new AccountingException("Error while processing data before Avalara Operation call.");
                }

                JSONObject addressesJobj = requestJobj.optJSONObject("addressesJson");
                returnJobjTemp.put("addresses", addressesJobj);//Addresses to be used for tax calculation

                String customerCode = requestJobj.optString("customerCode");
                if (!StringUtil.isNullOrEmpty(customerCode)) {//Customer Code
                    returnJobjTemp.put("customerCode", customerCode);
                }

                String salesPersonCode = requestJobj.optString("salesPersonCode");
                if (!StringUtil.isNullOrEmpty(salesPersonCode)) {//Sales Person Code
                    returnJobjTemp.put("salespersonCode", salesPersonCode);
                }

                String exemptionCode = requestJobj.optString(IntegrationConstants.avalaraExemptionCode);
                if (!StringUtil.isNullOrEmpty(exemptionCode) && !StringUtil.equalIgnoreCase(exemptionCode, Constants.NONE)) {//Exemption code which is selected by user in the dimension 'AvaTax Exemption Code'
                    returnJobjTemp.put("customerUsageType", exemptionCode);
                }

                if (requestJobj.has(IntegrationConstants.avalaraDocCode)) {//Doc-Code is unique identifier for a transaction on AvaTax side
                    returnJobjTemp.put(IntegrationConstants.code, requestJobj.optString(IntegrationConstants.avalaraDocCode));
                }

                if (requestJobj.has("jeNumber")) {//we send Journal Entry number as a reference on AvaTax side
                    returnJobjTemp.put("referenceCode", requestJobj.optString("jeNumber"));
                } else if (requestJobj.has(Constants.billno)) {//If JE number is not available then, document number is used as reference
                    returnJobjTemp.put("referenceCode", requestJobj.optString(Constants.billno));
                }

                //If tax override is required, then we create taxOverride JSON as per AvaTax API
                //Tax Override is required when we want to use taxes from an earlier date in a transaction, for example in link cases
                if (!StringUtil.isNullOrEmpty(requestJobj.optString(IntegrationConstants.taxOverrideDocId))) {
                    JSONObject taxOverrideJobj = getTaxOverrideJson(requestJobj, moduleid);
                    returnJobjTemp.put("taxOverride", taxOverrideJobj);
                }
            }

            returnJobj.put("createTransactionModel", returnJobjTemp);
        }

        return returnJobj;
    }

    /**
     * Below method creates Line level details JSON Array to be sent to AvaTax
     * side as a part of payload JSON
     *
     * @param requestJobj
     * @param productRecord
     * @return
     * @throws JSONException
     */
    public JSONObject getLineDetails(JSONObject requestJobj, String productRecord) throws JSONException {
        JSONObject returnJobj = new JSONObject();
        String moduleid = requestJobj.optString(Constants.moduleid);
        JSONArray productRecordJarr = new JSONArray(productRecord);
        boolean isCommit = requestJobj.optBoolean(IntegrationConstants.commit, false);
        double exchangeRate = requestJobj.optDouble("exchangeRate", 1.0D);
        JSONArray lineItemsJarr = new JSONArray();
        for (int i = 0; i < productRecordJarr.length(); i++) {
            JSONObject productRecordJobj = productRecordJarr.optJSONObject(i);
            double quantity = productRecordJobj.optDouble(Constants.quantity, 0);
            if (StringUtil.equal(moduleid, String.valueOf(Constants.Acc_Delivery_Order_ModuleId)) || StringUtil.equal(moduleid, String.valueOf(Constants.Acc_Sales_Return_ModuleId))) {
                quantity = productRecordJobj.optDouble(Constants.dquantity, 0);
            }
            double amountWithoutTax = 0;
            if (productRecordJobj.has("amountwithouttax") && productRecordJobj.optDouble("amountwithouttax", 0) != 0) {//in case of CQ, SO and CI
                amountWithoutTax = productRecordJobj.optDouble("amountwithouttax");
            } else if (productRecordJobj.has("amountWithoutTax") && productRecordJobj.optDouble("amountWithoutTax", 0) != 0) {//In case of DO
                amountWithoutTax = productRecordJobj.optDouble("amountWithoutTax");
            } else {//For CQ import
                double rate = productRecordJobj.optDouble("rate", 0);
                boolean isPercentDiscount = (productRecordJobj.optInt("discountispercent", 1) == 1);
                double discountValue = productRecordJobj.optDouble("prdiscount", 0);
                if (isPercentDiscount) {
                    amountWithoutTax = (rate * quantity * (100D - discountValue)) / 100D;
                } else {
                    amountWithoutTax = (rate * quantity) - discountValue;
                }
            }
            String productCode = productRecordJobj.optString("pid");//Product Code
            String producDescription = "";
            if (productRecordJobj.has("desc") && !StringUtil.isNullOrEmpty(productRecordJobj.optString("desc"))) {//in case of CQ, SO and CI
                producDescription = productRecordJobj.optString("desc");
            } else if (productRecordJobj.has("description") && !StringUtil.isNullOrEmpty(productRecordJobj.optString("description"))) {//In case of DO
                producDescription = productRecordJobj.optString("description");
            }
            String productTaxCode = productRecordJobj.optString(IntegrationConstants.avalaraProductTaxCode);//Product Tax Class assigned to product in Product Master

            JSONObject lineItemJobj = new JSONObject();
            lineItemJobj.put("number", String.valueOf(i + 1));//Row number
            lineItemJobj.put("itemCode", productCode);//Product Code
            lineItemJobj.put("quantity", quantity);
            lineItemJobj.put("description", producDescription);//Product Description
            if (StringUtil.equal(moduleid, String.valueOf(Constants.Acc_Sales_Return_ModuleId))) {
                //multiply by -1 in case of sales return to make amount negative as required by AvaTax
                if (isCommit) {
                    lineItemJobj.put("amount", (amountWithoutTax * exchangeRate) * (-1.0D));
                } else {
                    lineItemJobj.put("amount", amountWithoutTax * (-1.0D));
                }
            } else {
                if (isCommit) {
                    lineItemJobj.put("amount", (amountWithoutTax * exchangeRate));
                } else {
                    lineItemJobj.put("amount", amountWithoutTax);
                }
            }
            if (!StringUtil.isNullOrEmpty(productTaxCode)) {//Product Tax Class assigned to product in Product Master
                lineItemJobj.put("taxCode", productTaxCode);
            }
            //If tax override is required, then we create taxOverride JSON as per AvaTax API
            //Tax Override is required when we want to use taxes from an earlier date in a transaction, for example in link cases
            if (!StringUtil.isNullOrEmpty(productRecordJobj.optString(IntegrationConstants.taxOverrideDocId))) {
                JSONObject taxOverrideJobj = getTaxOverrideJson(productRecordJobj, moduleid);
                lineItemJobj.put("taxOverride", taxOverrideJobj);
                JSONObject addressesJobj = productRecordJobj.optJSONObject("addressesJson");
                lineItemJobj.put("addresses", addressesJobj);
                String exemptionCode = productRecordJobj.optString(IntegrationConstants.avalaraExemptionCode);
                if (!StringUtil.isNullOrEmpty(exemptionCode)) {
                    lineItemJobj.put("customerUsageType", exemptionCode);
                }
            }
            lineItemsJarr.put(lineItemJobj);
        }
        returnJobj.put(Constants.RES_data, lineItemsJarr);
        return returnJobj;
    }

    /**
     * Below method creates details JSON for tax override
     *
     * @param requestJobj
     * @param moduleid
     * @return
     * @throws JSONException
     */
    public JSONObject getTaxOverrideJson(JSONObject requestJobj, String moduleid) throws JSONException {
        JSONObject returnJobj = new JSONObject();

        returnJobj.put("taxDate", requestJobj.optString(IntegrationConstants.taxOverrideDate));
        returnJobj.put(Constants.type, requestJobj.optString(IntegrationConstants.taxOverrideType));
        returnJobj.put("reason", requestJobj.optString(IntegrationConstants.taxOverrideReason));

        return returnJobj;
    }

    public JSONObject getPayloadForBillingAddressValidation(JSONObject requestJobj) throws JSONException {
        JSONObject addressDetailsJobj = new JSONObject(requestJobj.optString(IntegrationConstants.addressesForValidationWithAvalara));
        JSONObject returnJobj = new JSONObject();
        returnJobj.put("textCase", "Upper");
        returnJobj.put("line1", addressDetailsJobj.optString("billingAliasName"));
        returnJobj.put("line2", addressDetailsJobj.optString("billingAddress"));
        returnJobj.put("city", addressDetailsJobj.optString("billingCity"));
        returnJobj.put("region", addressDetailsJobj.optString("billingState"));
        returnJobj.put("country", addressDetailsJobj.optString("billingCountry"));
        returnJobj.put("postalCode", addressDetailsJobj.optString("billingPostal"));
        return returnJobj;
    }

    public JSONObject getPayloadForShippingAddressValidation(JSONObject requestJobj) throws JSONException {
        JSONObject addressDetailsJobj = new JSONObject(requestJobj.optString(IntegrationConstants.addressesForValidationWithAvalara));
        JSONObject returnJobj = new JSONObject();
        returnJobj.put("textCase", "Upper");
        returnJobj.put("line1", addressDetailsJobj.optString("shippingAliasName"));
        returnJobj.put("line2", addressDetailsJobj.optString("shippingAddress"));
        returnJobj.put("city", addressDetailsJobj.optString("shippingCity"));
        returnJobj.put("region", addressDetailsJobj.optString("shippingState"));
        returnJobj.put("country", addressDetailsJobj.optString("shippingCountry"));
        returnJobj.put("postalCode", addressDetailsJobj.optString("shippingPostal"));
        return returnJobj;
    }

    /**
     * Following method creates request JSON for cancelTax call to AvaTax REST
     * service
     *
     * @param requestJobj
     * @param integrationAccountDetails
     * @return
     * @throws JSONException
     */
    public JSONObject getPayloadForCancelTax(JSONObject requestJobj, JSONObject integrationAccountDetails) throws JSONException {
        JSONObject returnJobj = new JSONObject();

        returnJobj.put(IntegrationConstants.code, requestJobj.optString("CancelCode"));

        return returnJobj;
    }

    /**
     * Following method creates request JSON for ChangeDocCode call to AvaTax
     * REST service
     *
     * @param paramsJobj
     * @param integrationAccountDetails
     * @return
     * @throws JSONException
     */
    public JSONObject getPayloadForChangeDocCode(JSONObject paramsJobj, JSONObject integrationAccountDetails) throws JSONException {
        JSONObject returnJobj = new JSONObject();

        returnJobj.put("newCode", paramsJobj.optString("newDocCode"));

        return returnJobj;
    }

    /**
     * Below method creates payload JSON for create-item request
     *
     * @param paramsJobj
     * @param integrationAccountDetails
     * @return
     * @throws JSONException
     */
    public JSONArray getPayloadForCreateItems(JSONObject paramsJobj, JSONObject integrationAccountDetails) throws JSONException {
        JSONArray returnJarr = new JSONArray();
        JSONArray productJarr = paramsJobj.optJSONArray("productsJarr");
        if (productJarr != null) {
            for (int i = 0; i < productJarr.length(); i++) {
                JSONObject productJobj = productJarr.optJSONObject(i);
                JSONObject tempJobj = getSingleItemJson(productJobj);
                returnJarr.put(tempJobj);
            }
        }
        return returnJarr;
    }

    /**
     * Below method creates JSON for an item to be created/updated on AvaTax
     * side
     *
     * @param productJobj
     * @return
     * @throws JSONException
     */
    private JSONObject getSingleItemJson(JSONObject productJobj) throws JSONException {
        JSONObject returnJobj = new JSONObject();
        returnJobj.put("itemCode", productJobj.optString("pid"));
        String description = productJobj.optString("description");
        returnJobj.put("description", StringUtil.isNullOrEmpty(description) ? "NA" : description);
        String productTaxCode = productJobj.optString(IntegrationConstants.avalaraProductTaxCode);
        returnJobj.put("taxCode", productTaxCode);
        return returnJobj;
    }

    /**
     * Below method creates payload JSON for update-item request
     *
     * @param paramsJobj
     * @param integrationAccountDetails
     * @return
     * @throws JSONException
     */
    public JSONObject getPayloadForUpdateItem(JSONObject paramsJobj, JSONObject integrationAccountDetails) throws JSONException {
        JSONObject returnJobj = new JSONObject();
        JSONArray productJarr = new JSONArray(paramsJobj.optString(Constants.detail));
        JSONObject productJobj = (productJarr != null && productJarr.length() != 0) ? productJarr.optJSONObject(0) : null;
        if (productJobj != null) {
            returnJobj = getSingleItemJson(productJobj);
            returnJobj.put(Constants.Acc_id, paramsJobj.optString("avalaraItemId"));
            JSONObject configJson = integrationAccountDetails.optJSONObject(IntegrationConstants.configJson);
            String avalaraCompanyId = configJson != null ? configJson.optString("avalaraCompanyId") : null;
            returnJobj.put("companyId", avalaraCompanyId);
        }
        return returnJobj;
    }

    /**
     * Method to convert Tax Details received from AvaTax service into a
     * JSONArray which is readable by Deskera service This method reads the Tax
     * data received from Avalara's getTax method call and puts the values
     * against Deskera's keys in another JSON
     *
     * @param taxResponseJobj -> response JSONObject received from Avalara's
     * getTax method call
     * @param paramsJobj -> contains 'productid'
     * @param integrationAccountDetails -> contains integration settings
     * @return JSONArray
     * @throws JSONException
     */
    public JSONArray processTaxDetails(JSONObject taxResponseJobj, JSONObject paramsJobj, JSONObject integrationAccountDetails) throws JSONException {
        boolean isCommit = paramsJobj.optBoolean(IntegrationConstants.commit, false);
        JSONArray prodTermJarr = new JSONArray();
        double exchangeRate = paramsJobj.optDouble("exchangeRate",1.0);
        JSONObject integrationConfigJson = integrationAccountDetails.optJSONObject(IntegrationConstants.configJson);
        String taxAccountId = integrationConfigJson.optString("taxAccountId");
        String moduleid = paramsJobj.optString(Constants.moduleid);
        JSONArray productRecordJarr = new JSONArray(paramsJobj.optString(Constants.detail));

        JSONArray taxLinesJarr = taxResponseJobj.optJSONArray("lines");
        for (int j = 0; j < taxLinesJarr.length(); j++) {
            JSONObject lineTermJobj = new JSONObject();
            JSONArray lineTermDetailsJarr = new JSONArray();
            JSONObject taxLineJobj = taxLinesJarr.optJSONObject(j);
            JSONArray taxDetailsJarr = taxLineJobj.optJSONArray("details");
            for (int i = 0; i < taxDetailsJarr.length(); i++) {
                JSONObject taxDetailsJobj = taxDetailsJarr.optJSONObject(i);
                /**
                 * Check to exclude records with zero taxable amount. This is
                 * because zero amounts were reflecting in JE posted for invoice
                 * and zero taxes are not required to be shown Zero taxable
                 * amount is received in case of tax exemption
                 */  
                if (taxDetailsJobj.optDouble("taxableAmount", 0.0D) != 0.0D) {
                    JSONObject lineTermDetailsJobj = new JSONObject();
                    lineTermDetailsJobj.put("taxtype", 1);
                    lineTermDetailsJobj.put("taxtypeName", "Percentage");
                    lineTermDetailsJobj.put("taxvalue", StringUtil.roundDoubleTo(taxDetailsJobj.optDouble("rate") * 100, 8));
                    lineTermDetailsJobj.put("originalTermPercentage", StringUtil.roundDoubleTo(taxDetailsJobj.optDouble("rate") * 100, 8));
                    lineTermDetailsJobj.put("termpercentage", StringUtil.roundDoubleTo(taxDetailsJobj.optDouble("rate") * 100, 8));
                    if (StringUtil.equal(moduleid, String.valueOf(Constants.Acc_Sales_Return_ModuleId))) {
                        lineTermDetailsJobj.put("assessablevalue", (taxDetailsJobj.optDouble("taxableAmount") * (-1.0D)/exchangeRate));
                        lineTermDetailsJobj.put("nonTaxableAmount", (taxDetailsJobj.optDouble("nonTaxableAmount") * (-1.0D)/exchangeRate));
                    } else {
                        lineTermDetailsJobj.put("assessablevalue", (taxDetailsJobj.optDouble("taxableAmount")/exchangeRate));
                        lineTermDetailsJobj.put("nonTaxableAmount", (taxDetailsJobj.optDouble("nonTaxableAmount")/exchangeRate));
                    }
                    if (StringUtil.equal(moduleid, String.valueOf(Constants.Acc_Sales_Return_ModuleId))) {
                        lineTermDetailsJobj.put("termamount", (taxDetailsJobj.optDouble("tax") * (-1.0D)/exchangeRate));
                    } else {
                        lineTermDetailsJobj.put("termamount", (taxDetailsJobj.optDouble("tax")/exchangeRate));
                    }
                    lineTermDetailsJobj.put("term", taxDetailsJobj.optString("taxName"));
                    lineTermDetailsJobj.put("juristype", taxDetailsJobj.optString("jurisType"));
                    lineTermDetailsJobj.put("juriscode", taxDetailsJobj.optString("jurisCode"));
                    lineTermDetailsJobj.put("jurisname", taxDetailsJobj.optString("jurisName"));
                    lineTermDetailsJobj.put("termtype", 7);//TermType = 7 for GST
                    lineTermDetailsJobj.put("sign", 1);
                    lineTermDetailsJobj.put("formulaids", "Basic");
                    lineTermDetailsJobj.put("formula", "Basic");
                    lineTermDetailsJobj.put("glaccount", taxAccountId);
                    lineTermDetailsJobj.put("accountid", taxAccountId);
                    lineTermDetailsJobj.put("payableaccountid", taxAccountId);
                    lineTermDetailsJarr.put(lineTermDetailsJobj);
                }                
            }
            lineTermJobj.put("LineTermdetails", lineTermDetailsJarr);
            lineTermJobj.put("LineTermAmount", (taxLineJobj.optDouble("tax", 0)/exchangeRate));//Totol tax amount for product row
            lineTermJobj.put("lineAmount", (taxLineJobj.optDouble("lineAmount", 0)/exchangeRate));//Totol tax amount for product row
            lineTermJobj.put("taxableAmount", (taxLineJobj.optDouble("taxableAmount", 0)/exchangeRate));//Totol tax amount for product row
            int lineNumber = taxLineJobj.getInt("lineNumber");
            JSONObject productRecrodJobj = productRecordJarr.optJSONObject(lineNumber - 1);
            lineTermJobj.put(Constants.productid, productRecrodJobj.optString(Constants.productid));
            lineTermJobj.put("rowIndex", productRecrodJobj.optString("rowIndex"));
            lineTermJobj.put("lineNumber", lineNumber);
            prodTermJarr.put(lineTermJobj);
        }
        return prodTermJarr;
    }
        }
