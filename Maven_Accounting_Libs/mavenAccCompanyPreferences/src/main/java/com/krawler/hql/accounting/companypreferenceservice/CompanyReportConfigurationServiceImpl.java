/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting.companypreferenceservice;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.FieldParams;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.companypref.util.CompanyReportConfigConstants;
import com.krawler.hql.accounting.CompanyReportConfiguration;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.CompanyReportConfigurationDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.krawler.utils.json.base.XML;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class CompanyReportConfigurationServiceImpl implements CompanyReportConfigurationService {

    private Map<String, String> glDynamicQueryMap;
    private Map<String, String> soaDynamicQueryMap;
    private Map<String, String> arDynamicQueryMap;
    private CompanyReportConfigurationDAO companyReportConfigurationdao;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accAccountDAO accAccountDAOobj;

    public void setCompanyReportConfigurationdao(CompanyReportConfigurationDAO companyReportConfigurationdao) {
        this.companyReportConfigurationdao = companyReportConfigurationdao;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setAccAccountDAOobj(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public Map<String, String> getGLDynamicQueryMap() {
        if (glDynamicQueryMap == null) {
            glDynamicQueryMap = new HashMap<String, String>();
            glDynamicQueryMap.put("INV_REF_ID", CompanyReportConfigConstants.GL_INV_REF_ID);
            glDynamicQueryMap.put("GR_REF_ID", CompanyReportConfigConstants.GL_GR_REF_ID);
            glDynamicQueryMap.put("CN_REF_ID", CompanyReportConfigConstants.GL_CN_REF_ID);
            glDynamicQueryMap.put("DN_REF_ID", CompanyReportConfigConstants.GL_DN_REF_ID);
            glDynamicQueryMap.put("RECEIPT_REF_ID", CompanyReportConfigConstants.GL_RECEIPT_REF_ID);
            glDynamicQueryMap.put("PAYMENT_REF_ID", CompanyReportConfigConstants.GL_PAYMENT_REF_ID);
            glDynamicQueryMap.put("OTHERS_REF_ID", CompanyReportConfigConstants.GL_OTHERS_REF_ID);
            glDynamicQueryMap.put("GL_CURRENCY_SYMBOL", CompanyReportConfigConstants.GL_CURR_SYMBOL);
            glDynamicQueryMap.put("INV_CASH_TRANSACTION", CompanyReportConfigConstants.GL_INV_CASH_TRANSACTION);
            glDynamicQueryMap.put("GR_CASH_TRANSACTION", CompanyReportConfigConstants.GL_GR_CASH_TRANSACTION);
            glDynamicQueryMap.put("CN_CASH_TRANSACTION", CompanyReportConfigConstants.GL_CN_CASH_TRANSACTION);
            glDynamicQueryMap.put("DN_CASH_TRANSACTION", CompanyReportConfigConstants.GL_DN_CASH_TRANSACTION);
            glDynamicQueryMap.put("RECEIPT_CASH_TRANSACTION", CompanyReportConfigConstants.GL_RECEIPT_CASH_TRANSACTION);
            glDynamicQueryMap.put("PAYMENT_CASH_TRANSACTION", CompanyReportConfigConstants.GL_PAYMENT_CASH_TRANSACTION);
            glDynamicQueryMap.put("OTHERS_CASH_TRANSACTION", CompanyReportConfigConstants.GL_OTHERS_CASH_TRANSACTION);
            glDynamicQueryMap.put("acc_doublemovement", CompanyReportConfigConstants.GL_ACCOUNT_INFO);
            glDynamicQueryMap.put("d_date", CompanyReportConfigConstants.GL_ENTRY_DATE);
            glDynamicQueryMap.put("INV_DETAILS_description", CompanyReportConfigConstants.GL_INV_DETAILS_DESCRIPTION);
            glDynamicQueryMap.put("INV_CAP_description", CompanyReportConfigConstants.GL_INV_CAP_DESCRIPTION);
            glDynamicQueryMap.put("INV_ROUNDING_description", CompanyReportConfigConstants.GL_INV_ROUNDING_DESCRIPTION);
            glDynamicQueryMap.put("INV_TERMS_description", CompanyReportConfigConstants.GL_INV_TERMS_DESCRIPTION);
            glDynamicQueryMap.put("GR_CAP_description", CompanyReportConfigConstants.GL_GR_CAP_DESCRIPTION);
            glDynamicQueryMap.put("GR_DETAILS_description", CompanyReportConfigConstants.GL_GR_DETAILS_DESCRIPTION);
            glDynamicQueryMap.put("GR_EXP_DETAILS_description", CompanyReportConfigConstants.GL_GR_EXP_DETAILS_DESCRIPTION);
            glDynamicQueryMap.put("GR_ROUNDING_description", CompanyReportConfigConstants.GL_GR_ROUNDING_DESCRIPTION);
            glDynamicQueryMap.put("GR_TERMS_description", CompanyReportConfigConstants.GL_GR_TERMS_DESCRIPTION);
            glDynamicQueryMap.put("CN_description", CompanyReportConfigConstants.GL_CN_DESCRIPTION);
            glDynamicQueryMap.put("DN_description", CompanyReportConfigConstants.GL_DN_DESCRIPTION);
            glDynamicQueryMap.put("RECEIPT_description", CompanyReportConfigConstants.GL_RECEIPTS_DESCRIPTION);
            glDynamicQueryMap.put("PAYMENT_description", CompanyReportConfigConstants.GL_PAYMENT_DESCRIPTION);
            glDynamicQueryMap.put("OTHERS_description", CompanyReportConfigConstants.GL_OTHERS_DESCRIPTION);
            glDynamicQueryMap.put("entryno", CompanyReportConfigConstants.GL_ENTRY_NO);
            glDynamicQueryMap.put("exchangeratefortransaction", CompanyReportConfigConstants.GL_EXCHANGE_RATE);
            glDynamicQueryMap.put("d_transactionAmount", CompanyReportConfigConstants.GL_D_AMOUNT);
            glDynamicQueryMap.put("d_amount", CompanyReportConfigConstants.GL_D_AMOUNT_IN_BASE);
            glDynamicQueryMap.put("c_transactionAmount", CompanyReportConfigConstants.GL_C_AMOUNT);
            glDynamicQueryMap.put("c_amount", CompanyReportConfigConstants.GL_C_AMOUNT_IN_BASE);
            glDynamicQueryMap.put("INV_memo", CompanyReportConfigConstants.GL_INV_MEMO);
            glDynamicQueryMap.put("GR_memo", CompanyReportConfigConstants.GL_GR_MEMO);
            glDynamicQueryMap.put("CN_memo", CompanyReportConfigConstants.GL_CN_MEMO);
            glDynamicQueryMap.put("DN_memo", CompanyReportConfigConstants.GL_DN_MEMO);
            glDynamicQueryMap.put("RECEIPT_memo", CompanyReportConfigConstants.GL_RECEIPT_MEMO);
            glDynamicQueryMap.put("PAYMENT_memo", CompanyReportConfigConstants.GL_PAYMENT_MEMO);
            glDynamicQueryMap.put("OTHERS_memo", CompanyReportConfigConstants.GL_OTHERS_MEMO);
//            glDynamicQueryMap.put("custom_data", CompanyReportConfigConstants.GL_CUSTOM_DATA);
            glDynamicQueryMap.put("INV_refno", CompanyReportConfigConstants.GL_INV_REF_NO);
            glDynamicQueryMap.put("GR_refno", CompanyReportConfigConstants.GL_GR_REF_NO);
            glDynamicQueryMap.put("CN_refno", CompanyReportConfigConstants.GL_CN_REF_NO);
            glDynamicQueryMap.put("DN_refno", CompanyReportConfigConstants.GL_DN_REF_NO);
            glDynamicQueryMap.put("RECEIPT_refno", CompanyReportConfigConstants.GL_RECEIPT_REF_NO);
            glDynamicQueryMap.put("PAYMENT_refno", CompanyReportConfigConstants.GL_PAYMENT_REF_NO);
            glDynamicQueryMap.put("OTHERS_refno", CompanyReportConfigConstants.GL_OTHERS_REF_NO);
            glDynamicQueryMap.put("INV_DETAILS_line_description", CompanyReportConfigConstants.GL_INV_DETAILS_line_description);
            glDynamicQueryMap.put("INV_CAP_line_description", CompanyReportConfigConstants.GL_INV_CAP_line_description);
            glDynamicQueryMap.put("INV_ROUNDING_line_description", CompanyReportConfigConstants.GL_INV_ROUNDING_line_description);
            glDynamicQueryMap.put("INV_TERMS_line_description", CompanyReportConfigConstants.GL_INV_TERMS_line_description);
            glDynamicQueryMap.put("GR_CAP_line_description", CompanyReportConfigConstants.GL_GR_CAP_line_description);
            glDynamicQueryMap.put("GR_DETAILS_line_description", CompanyReportConfigConstants.GL_GR_DETAILS_line_description);
            glDynamicQueryMap.put("GR_EXP_DETAILS_line_description", CompanyReportConfigConstants.GL_GR_EXP_DETAILS_line_description);
            glDynamicQueryMap.put("GR_ROUNDING_line_description", CompanyReportConfigConstants.GL_GR_ROUNDING_line_description);
            glDynamicQueryMap.put("GR_TERMS_line_description", CompanyReportConfigConstants.GL_GR_TERMS_line_description);
            glDynamicQueryMap.put("CN_line_description", CompanyReportConfigConstants.GL_CN_line_description);
            glDynamicQueryMap.put("DN_line_description", CompanyReportConfigConstants.GL_DN_line_description);
            glDynamicQueryMap.put("RECEIPT_line_description", CompanyReportConfigConstants.GL_RECEIPT_line_description);
            glDynamicQueryMap.put("PAYMENT_line_description", CompanyReportConfigConstants.GL_PAYMENT_line_description);
            glDynamicQueryMap.put("OTHERS_line_description", CompanyReportConfigConstants.GL_OTHERS_line_description);
            glDynamicQueryMap.put("INV_DETAILS_gstcode", CompanyReportConfigConstants.GL_INV_DETAILS_GST_CODE);
            glDynamicQueryMap.put("INV_CAP_gstcode", CompanyReportConfigConstants.GL_INV_CAP_GST_CODE);
            glDynamicQueryMap.put("INV_ROUNDING_gstcode", CompanyReportConfigConstants.GL_INV_ROUNDING_GST_CODE);
            glDynamicQueryMap.put("INV_TERMS_gstcode", CompanyReportConfigConstants.GL_INV_TERMS_GST_CODE);
            glDynamicQueryMap.put("GR_CAP_gstcode", CompanyReportConfigConstants.GL_GR_CAP_GST_CODE);
            glDynamicQueryMap.put("GR_DETAILS_gstcode", CompanyReportConfigConstants.GL_GR_DETAILS_GST_CODE);
            glDynamicQueryMap.put("GR_EXP_DETAILS_gstcode", CompanyReportConfigConstants.GL_GR_EXP_DETAILS_GST_CODE);
            glDynamicQueryMap.put("GR_ROUNDING_gstcode", CompanyReportConfigConstants.GL_GR_ROUNDING_GST_CODE);
            glDynamicQueryMap.put("GR_TERMS_gstcode", CompanyReportConfigConstants.GL_GR_TERMS_GST_CODE);
            glDynamicQueryMap.put("CN_gstcode", CompanyReportConfigConstants.GL_CN_GST_CODE);
            glDynamicQueryMap.put("DN_gstcode", CompanyReportConfigConstants.GL_DN_GST_CODE);
            glDynamicQueryMap.put("RECEIPT_gstcode", CompanyReportConfigConstants.GL_RECEIPT_GST_CODE);
            glDynamicQueryMap.put("PAYMENT_gstcode", CompanyReportConfigConstants.GL_PAYMENT_GST_CODE);
            glDynamicQueryMap.put("OTHERS_gstcode", CompanyReportConfigConstants.GL_OTHERS_GST_CODE);
            glDynamicQueryMap.put("INV_doctype", CompanyReportConfigConstants.GL_INV_TYPE);
            glDynamicQueryMap.put("GR_doctype", CompanyReportConfigConstants.GL_GR_TYPE);
            glDynamicQueryMap.put("CN_doctype", CompanyReportConfigConstants.GL_CN_TYPE);
            glDynamicQueryMap.put("DN_doctype", CompanyReportConfigConstants.GL_DN_TYPE);
            glDynamicQueryMap.put("RECEIPT_doctype", CompanyReportConfigConstants.GL_RECEIPT_TYPE);
            glDynamicQueryMap.put("PAYMENT_doctype", CompanyReportConfigConstants.GL_PAYMENT_TYPE);
            glDynamicQueryMap.put("OTHERS_doctype", CompanyReportConfigConstants.GL_OTHERS_TYPE);
            glDynamicQueryMap.put("INV_paymi", CompanyReportConfigConstants.GL_INV_PAY_MI);
            glDynamicQueryMap.put("GR_paymi", CompanyReportConfigConstants.GL_GR_PAY_MI);
            glDynamicQueryMap.put("CN_paymi", CompanyReportConfigConstants.GL_CN_PAY_MI);
            glDynamicQueryMap.put("DN_paymi", CompanyReportConfigConstants.GL_DN_PAY_MI);
            glDynamicQueryMap.put("RECEIPT_paymi", CompanyReportConfigConstants.GL_RECEIPT_PAY_MI);
            glDynamicQueryMap.put("PAYMENT_paymi", CompanyReportConfigConstants.GL_PAYMENT_PAY_MI);
            glDynamicQueryMap.put("OTHERS_paymi", CompanyReportConfigConstants.GL_OTHERS_PAY_MI);
            glDynamicQueryMap.put("INV_salesperson", CompanyReportConfigConstants.GL_INV_SALES_PERSON_NAME);
            glDynamicQueryMap.put("GR_salesperson", CompanyReportConfigConstants.GL_GR_SALES_PERSON_NAME);
            glDynamicQueryMap.put("CN_salesperson", CompanyReportConfigConstants.GL_CN_SALES_PERSON_NAME);
            glDynamicQueryMap.put("DN_salesperson", CompanyReportConfigConstants.GL_DN_SALES_PERSON_NAME);
            glDynamicQueryMap.put("RECEIPT_salesperson", CompanyReportConfigConstants.GL_RECEIPT_SALES_PERSON_NAME);
            glDynamicQueryMap.put("PAYMENT_salesperson", CompanyReportConfigConstants.GL_PAYMENT_SALES_PERSON_NAME);
            glDynamicQueryMap.put("OTHERS_salesperson", CompanyReportConfigConstants.GL_OTHERS_SALES_PERSON_NAME);
            glDynamicQueryMap.put("INV_personname", CompanyReportConfigConstants.GL_INV_PERSON_NAME);
            glDynamicQueryMap.put("GR_personname", CompanyReportConfigConstants.GL_GR_PERSON_NAME);
            glDynamicQueryMap.put("CN_personname", CompanyReportConfigConstants.GL_CN_PERSON_NAME);
            glDynamicQueryMap.put("DN_personname", CompanyReportConfigConstants.GL_DN_PERSON_NAME);
            glDynamicQueryMap.put("RECEIPT_personname", CompanyReportConfigConstants.GL_RECEIPT_PERSON_NAME);
            glDynamicQueryMap.put("PAYMENT_personname", CompanyReportConfigConstants.GL_PAYMENT_PERSON_NAME);
            glDynamicQueryMap.put("OTHERS_personname", CompanyReportConfigConstants.GL_OTHERS_PERSON_NAME);
            glDynamicQueryMap.put("INV_personcode", CompanyReportConfigConstants.GL_INV_PERSON_CODE);
            glDynamicQueryMap.put("GR_personcode", CompanyReportConfigConstants.GL_GR_PERSON_CODE);
            glDynamicQueryMap.put("CN_personcode", CompanyReportConfigConstants.GL_CN_PERSON_CODE);
            glDynamicQueryMap.put("DN_personcode", CompanyReportConfigConstants.GL_DN_PERSON_CODE);
            glDynamicQueryMap.put("RECEIPT_personcode", CompanyReportConfigConstants.GL_RECEIPT_PERSON_CODE);
            glDynamicQueryMap.put("PAYMENT_personcode", CompanyReportConfigConstants.GL_PAYMENT_PERSON_CODE);
            glDynamicQueryMap.put("OTHERS_personcode", CompanyReportConfigConstants.GL_OTHERS_PERSON_CODE);
            glDynamicQueryMap.put("balance", CompanyReportConfigConstants.GL_BALANCE);
            glDynamicQueryMap.put("txncurrency", CompanyReportConfigConstants.GL_TXN_CURRENCY);
            glDynamicQueryMap.put("RECEIPT_WO_REF_ID", CompanyReportConfigConstants.GL_RECEIPT_REF_ID);
            glDynamicQueryMap.put("RECEIPT_WO_CASH_TRANSACTION", CompanyReportConfigConstants.GL_RECEIPT_CASH_TRANSACTION);
            glDynamicQueryMap.put("RECEIPT_WO_description", CompanyReportConfigConstants.GL_RECEIPTS_DESCRIPTION);
            glDynamicQueryMap.put("RECEIPT_WO_memo", CompanyReportConfigConstants.GL_RECEIPT_MEMO);
            glDynamicQueryMap.put("RECEIPT_WO_refno", CompanyReportConfigConstants.GL_RECEIPT_REF_NO);
            glDynamicQueryMap.put("RECEIPT_WO_line_description", CompanyReportConfigConstants.GL_RECEIPT_line_description);
            glDynamicQueryMap.put("RECEIPT_WO_gstcode", CompanyReportConfigConstants.GL_RECEIPT_GST_CODE);
            glDynamicQueryMap.put("RECEIPT_WO_doctype", CompanyReportConfigConstants.GL_RECEIPT_WO_TYPE);
            glDynamicQueryMap.put("RECEIPT_WO_paymi", CompanyReportConfigConstants.GL_RECEIPT_PAY_MI);
            glDynamicQueryMap.put("RECEIPT_WO_salesperson", CompanyReportConfigConstants.GL_RECEIPT_SALES_PERSON_NAME);
            glDynamicQueryMap.put("RECEIPT_WO_personname", CompanyReportConfigConstants.GL_RECEIPT_PERSON_NAME);
            glDynamicQueryMap.put("RECEIPT_WO_personcode", CompanyReportConfigConstants.GL_RECEIPT_PERSON_CODE);

            glDynamicQueryMap.put("JE_INV_REVAL_REF_ID", CompanyReportConfigConstants.GL_INV_REF_ID);
            glDynamicQueryMap.put("JE_INV_REVAL_CASH_TRANSACTION", CompanyReportConfigConstants.GL_INV_CASH_TRANSACTION);
            glDynamicQueryMap.put("JE_INV_REVAL_description", CompanyReportConfigConstants.GL_INV_CAP_DESCRIPTION);
            glDynamicQueryMap.put("JE_INV_REVAL_memo", CompanyReportConfigConstants.GL_INV_MEMO);
            glDynamicQueryMap.put("JE_INV_REVAL_refno", CompanyReportConfigConstants.GL_INV_REF_NO);
            glDynamicQueryMap.put("JE_INV_REVAL_line_description", CompanyReportConfigConstants.GL_INV_CAP_line_description);
            glDynamicQueryMap.put("JE_INV_REVAL_gstcode", CompanyReportConfigConstants.GL_INV_CAP_GST_CODE);
            glDynamicQueryMap.put("JE_INV_REVAL_doctype", CompanyReportConfigConstants.GL_INV_TYPE);
            glDynamicQueryMap.put("JE_INV_REVAL_paymi", CompanyReportConfigConstants.GL_INV_PAY_MI);
            glDynamicQueryMap.put("JE_INV_REVAL_salesperson", CompanyReportConfigConstants.GL_INV_SALES_PERSON_NAME);
            glDynamicQueryMap.put("JE_INV_REVAL_personname", CompanyReportConfigConstants.GL_INV_PERSON_NAME);
            glDynamicQueryMap.put("JE_INV_REVAL_personcode", CompanyReportConfigConstants.GL_INV_PERSON_CODE);
            glDynamicQueryMap.put("JE_GR_REVAL_REF_ID", CompanyReportConfigConstants.GL_GR_REF_ID);
            glDynamicQueryMap.put("JE_GR_REVAL_CASH_TRANSACTION", CompanyReportConfigConstants.GL_GR_CASH_TRANSACTION);
            glDynamicQueryMap.put("JE_GR_REVAL_description", CompanyReportConfigConstants.GL_GR_CAP_DESCRIPTION);
            glDynamicQueryMap.put("JE_GR_REVAL_memo", CompanyReportConfigConstants.GL_GR_MEMO);
            glDynamicQueryMap.put("JE_GR_REVAL_refno", CompanyReportConfigConstants.GL_GR_REF_NO);
            glDynamicQueryMap.put("JE_GR_REVAL_line_description", CompanyReportConfigConstants.GL_GR_CAP_line_description);
            glDynamicQueryMap.put("JE_GR_REVAL_gstcode", CompanyReportConfigConstants.GL_GR_CAP_GST_CODE);
            glDynamicQueryMap.put("JE_GR_REVAL_doctype", CompanyReportConfigConstants.GL_GR_TYPE);
            glDynamicQueryMap.put("JE_GR_REVAL_paymi", CompanyReportConfigConstants.GL_GR_PAY_MI);
            glDynamicQueryMap.put("JE_GR_REVAL_salesperson", CompanyReportConfigConstants.GL_GR_SALES_PERSON_NAME);
            glDynamicQueryMap.put("JE_GR_REVAL_personname", CompanyReportConfigConstants.GL_GR_PERSON_NAME);
            glDynamicQueryMap.put("JE_GR_REVAL_personcode", CompanyReportConfigConstants.GL_GR_PERSON_CODE);
            glDynamicQueryMap.put("SA_REF_ID", CompanyReportConfigConstants.GL_SA_REF_ID);
            glDynamicQueryMap.put("SA_CASH_TRANSACTION", CompanyReportConfigConstants.GL_SA_CASH_TRANSACTION);
            glDynamicQueryMap.put("SA_description", CompanyReportConfigConstants.GL_SA_DESCRIPTION);
            glDynamicQueryMap.put("SA_memo", CompanyReportConfigConstants.GL_SA_MEMO);
            glDynamicQueryMap.put("SA_refno", CompanyReportConfigConstants.GL_SA_REF_NO);
            glDynamicQueryMap.put("SA_line_description", CompanyReportConfigConstants.GL_SA_line_description);
            glDynamicQueryMap.put("SA_gstcode", CompanyReportConfigConstants.GL_SA_GST_CODE);
            glDynamicQueryMap.put("SA_doctype", CompanyReportConfigConstants.GL_SA_TYPE);
            glDynamicQueryMap.put("SA_paymi", CompanyReportConfigConstants.GL_SA_PAY_MI);
            glDynamicQueryMap.put("SA_salesperson", CompanyReportConfigConstants.GL_SA_SALES_PERSON_NAME);
            glDynamicQueryMap.put("SA_personname", CompanyReportConfigConstants.GL_SA_PERSON_NAME);
            glDynamicQueryMap.put("SA_personcode", CompanyReportConfigConstants.GL_SA_PERSON_CODE);
            glDynamicQueryMap.put("SR_REF_ID", CompanyReportConfigConstants.GL_SR_REF_ID);
            glDynamicQueryMap.put("SR_CASH_TRANSACTION", CompanyReportConfigConstants.GL_SR_CASH_TRANSACTION);
            glDynamicQueryMap.put("SR_description", CompanyReportConfigConstants.GL_SR_DESCRIPTION);
            glDynamicQueryMap.put("SR_memo", CompanyReportConfigConstants.GL_SR_MEMO);
            glDynamicQueryMap.put("SR_refno", CompanyReportConfigConstants.GL_SR_REF_NO);
            glDynamicQueryMap.put("SR_line_description", CompanyReportConfigConstants.GL_SR_line_description);
            glDynamicQueryMap.put("SR_gstcode", CompanyReportConfigConstants.GL_SR_GST_CODE);
            glDynamicQueryMap.put("SR_doctype", CompanyReportConfigConstants.GL_SR_TYPE);
            glDynamicQueryMap.put("SR_paymi", CompanyReportConfigConstants.GL_SR_PAY_MI);
            glDynamicQueryMap.put("SR_salesperson", CompanyReportConfigConstants.GL_SR_SALES_PERSON_NAME);
            glDynamicQueryMap.put("SR_personname", CompanyReportConfigConstants.GL_SR_PERSON_NAME);
            glDynamicQueryMap.put("SR_personcode", CompanyReportConfigConstants.GL_SR_PERSON_CODE);
            glDynamicQueryMap.put("PR_REF_ID", CompanyReportConfigConstants.GL_PR_REF_ID);
            glDynamicQueryMap.put("PR_CASH_TRANSACTION", CompanyReportConfigConstants.GL_PR_CASH_TRANSACTION);
            glDynamicQueryMap.put("PR_description", CompanyReportConfigConstants.GL_PR_DESCRIPTION);
            glDynamicQueryMap.put("PR_memo", CompanyReportConfigConstants.GL_PR_MEMO);
            glDynamicQueryMap.put("PR_refno", CompanyReportConfigConstants.GL_PR_REF_NO);
            glDynamicQueryMap.put("PR_line_description", CompanyReportConfigConstants.GL_PR_line_description);
            glDynamicQueryMap.put("PR_gstcode", CompanyReportConfigConstants.GL_PR_GST_CODE);
            glDynamicQueryMap.put("PR_doctype", CompanyReportConfigConstants.GL_PR_TYPE);
            glDynamicQueryMap.put("PR_paymi", CompanyReportConfigConstants.GL_PR_PAY_MI);
            glDynamicQueryMap.put("PR_salesperson", CompanyReportConfigConstants.GL_PR_SALES_PERSON_NAME);
            glDynamicQueryMap.put("PR_personname", CompanyReportConfigConstants.GL_PR_PERSON_NAME);
            glDynamicQueryMap.put("PR_personcode", CompanyReportConfigConstants.GL_PR_PERSON_CODE);
            glDynamicQueryMap.put("DO_REF_ID", CompanyReportConfigConstants.GL_DO_REF_ID);
            glDynamicQueryMap.put("DO_CASH_TRANSACTION", CompanyReportConfigConstants.GL_DO_CASH_TRANSACTION);
            glDynamicQueryMap.put("DO_description", CompanyReportConfigConstants.GL_DO_DESCRIPTION);
            glDynamicQueryMap.put("DO_memo", CompanyReportConfigConstants.GL_DO_MEMO);
            glDynamicQueryMap.put("DO_refno", CompanyReportConfigConstants.GL_DO_REF_NO);
            glDynamicQueryMap.put("DO_line_description", CompanyReportConfigConstants.GL_DO_line_description);
            glDynamicQueryMap.put("DO_gstcode", CompanyReportConfigConstants.GL_DO_GST_CODE);
            glDynamicQueryMap.put("DO_doctype", CompanyReportConfigConstants.GL_DO_TYPE);
            glDynamicQueryMap.put("DO_paymi", CompanyReportConfigConstants.GL_DO_PAY_MI);
            glDynamicQueryMap.put("DO_salesperson", CompanyReportConfigConstants.GL_DO_SALES_PERSON_NAME);
            glDynamicQueryMap.put("DO_personname", CompanyReportConfigConstants.GL_DO_PERSON_NAME);
            glDynamicQueryMap.put("DO_personcode", CompanyReportConfigConstants.GL_DO_PERSON_CODE);
            glDynamicQueryMap.put("GRO_REF_ID", CompanyReportConfigConstants.GL_GRO_REF_ID);
            glDynamicQueryMap.put("GRO_CASH_TRANSACTION", CompanyReportConfigConstants.GL_GRO_CASH_TRANSACTION);
            glDynamicQueryMap.put("GRO_description", CompanyReportConfigConstants.GL_GRO_DESCRIPTION);
            glDynamicQueryMap.put("GRO_memo", CompanyReportConfigConstants.GL_GRO_MEMO);
            glDynamicQueryMap.put("GRO_refno", CompanyReportConfigConstants.GL_GRO_REF_NO);
            glDynamicQueryMap.put("GRO_line_description", CompanyReportConfigConstants.GL_GRO_line_description);
            glDynamicQueryMap.put("GRO_gstcode", CompanyReportConfigConstants.GL_GRO_GST_CODE);
            glDynamicQueryMap.put("GRO_doctype", CompanyReportConfigConstants.GL_GRO_TYPE);
            glDynamicQueryMap.put("GRO_paymi", CompanyReportConfigConstants.GL_GRO_PAY_MI);
            glDynamicQueryMap.put("GRO_salesperson", CompanyReportConfigConstants.GL_GRO_SALES_PERSON_NAME);
            glDynamicQueryMap.put("GRO_personname", CompanyReportConfigConstants.GL_GRO_PERSON_NAME);
            glDynamicQueryMap.put("GRO_personcode", CompanyReportConfigConstants.GL_GRO_PERSON_CODE);
            
        }
        return glDynamicQueryMap;
    }
    public Map<String, String> getSOADynamicQueryMap() {
        soaDynamicQueryMap =null;
        if (soaDynamicQueryMap == null) {
            soaDynamicQueryMap = new HashMap<String, String>();
            
            
            // Invoice Main
            
            
            soaDynamicQueryMap.put("INV_MAIN_AR_Type", CompanyReportConfigConstants.INV_MAIN_AR_Type);
            soaDynamicQueryMap.put("INV_MAIN_AR_Cust_Id", CompanyReportConfigConstants.INV_MAIN_CUST_ID);
            soaDynamicQueryMap.put("INV_MAIN_AR_Cust_Name", CompanyReportConfigConstants.INV_MAIN_CUST_NAME);
            soaDynamicQueryMap.put("INV_MAIN_AR_Cust_Code", CompanyReportConfigConstants.INV_MAIN_CUST_accCode);
            soaDynamicQueryMap.put("INV_MAIN_AR_Cust_Curr", CompanyReportConfigConstants.INV_MAIN_AR_Cust_Curr);
            soaDynamicQueryMap.put("INV_MAIN_AR_Cust_Alise", CompanyReportConfigConstants.INV_MAIN_AR_Cust_Alise);
            soaDynamicQueryMap.put("INV_MAIN_AR_Term_Name", CompanyReportConfigConstants.INV_MAIN_AR_Term_Name);
            soaDynamicQueryMap.put("INV_MAIN_AR_REF_ID", CompanyReportConfigConstants.INV_MAIN_REF_ID);
            soaDynamicQueryMap.put("INV_MAIN_AR_JEID", CompanyReportConfigConstants.INV_MAIN_JEID);
            soaDynamicQueryMap.put("INV_MAIN_AR_OPN_JEID", CompanyReportConfigConstants.INV_MAIN_AR_OPN_JEID);
            soaDynamicQueryMap.put("INV_MAIN_AR_Amt", CompanyReportConfigConstants.INV_MAIN_AR_Amt);
            soaDynamicQueryMap.put("INV_MAIN_AR_Amt_Base", CompanyReportConfigConstants.INV_MAIN_AR_Amt_Base);
            soaDynamicQueryMap.put("INV_MAIN_AR_OPN_Amt", CompanyReportConfigConstants.INV_MAIN_AR_OPN_Amt);
            soaDynamicQueryMap.put("INV_MAIN_AR_OPN_Amt_Base", CompanyReportConfigConstants.INV_MAIN_AR_OPN_Amt_Base);
            soaDynamicQueryMap.put("INV_MAIN_AR_Without_Inventry", CompanyReportConfigConstants.INV_MAIN_AR_Without_Inventry);
            soaDynamicQueryMap.put("INV_MAIN_AR_JE_Createdon", CompanyReportConfigConstants.INV_MAIN_JE_ENTRYDATE);
            soaDynamicQueryMap.put("INV_MAIN_AR_OPN_JE_Createdon", CompanyReportConfigConstants.INV_MAIN_AR_OPN_JE_Createdon);
            soaDynamicQueryMap.put("INV_MAIN_AR_Doc_Createdon", CompanyReportConfigConstants.INV_MAIN_AR_Doc_Createdon);
//            soaDynamicQueryMap.put("INV_MAIN_AR_knockOffAmount", CompanyReportConfigConstants.INV_MAIN_AR_KNOCK_OFF_AMT);
//            soaDynamicQueryMap.put("INV_MAIN_AR__knockOffAmountInBase", CompanyReportConfigConstants.INV_MAIN_AR_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("INV_MAIN_AR_Opening_Trans", CompanyReportConfigConstants.INV_MAIN_AR_Opening_Trans);
            soaDynamicQueryMap.put("INV_MAIN_AR_Company_Id", CompanyReportConfigConstants.INV_MAIN_AR_Company_Id);
            soaDynamicQueryMap.put("INV_MAIN_AR_Company_Name", CompanyReportConfigConstants.INV_MAIN_AR_Company_Name);
            soaDynamicQueryMap.put("INV_MAIN_AR_DOC_NUMBER", CompanyReportConfigConstants.INV_MAIN_DOC_NUMBER);
            soaDynamicQueryMap.put("INV_MAIN_AR_Trans_Curr", CompanyReportConfigConstants.INV_MAIN_CURR_ID);
            soaDynamicQueryMap.put("INV_MAIN_AR_Trans_CurrSymbol", CompanyReportConfigConstants.INV_MAIN_CURR_SYMBOL);
            soaDynamicQueryMap.put("INV_MAIN_AR_Trans_CurrName", CompanyReportConfigConstants.INV_MAIN_AR_Trans_CurrName);
            soaDynamicQueryMap.put("INV_MAIN_AR_Ext_Curr_Rate", CompanyReportConfigConstants.INV_MAIN_AR_Ext_Curr_Rate);
            soaDynamicQueryMap.put("INV_MAIN_AR_ExcahgeRate", CompanyReportConfigConstants.INV_MAIN_AR_ExcahgeRate);
            soaDynamicQueryMap.put("INV_MAIN_AR_OPN_Ext_Curr_Rate", CompanyReportConfigConstants.INV_MAIN_AR_OPN_Ext_Curr_Rate);
            soaDynamicQueryMap.put("INV_MAIN_AR_OPN_ExcahgeRate", CompanyReportConfigConstants.INV_MAIN_AR_OPN_ExcahgeRate);
            soaDynamicQueryMap.put("INV_MAIN_AR_Ship_Date", CompanyReportConfigConstants.INV_MAIN_AR_Ship_Date);
            soaDynamicQueryMap.put("INV_MAIN_AR_Due_Date", CompanyReportConfigConstants.INV_MAIN_AR_Due_Date);
            soaDynamicQueryMap.put("INV_MAIN_AR_JE_ENTRYNO", CompanyReportConfigConstants.INV_MAIN_JE_ENTRYNO);
            soaDynamicQueryMap.put("INV_MAIN_AR_OPN_JE_ENTRYNO", CompanyReportConfigConstants.INV_MAIN_OPN_JE_ENTRYNO);
            soaDynamicQueryMap.put("INV_MAIN_AR_MEMO", CompanyReportConfigConstants.INV_MAIN_AR_MEMO);
            soaDynamicQueryMap.put("INV_MAIN_AR_Sale_Per_Name", CompanyReportConfigConstants.INV_MAIN_AR_Sale_Per_Name);
            soaDynamicQueryMap.put("INV_MAIN_AR_Sale_Per_Code", CompanyReportConfigConstants.INV_MAIN_AR_Sale_Per_Code);
            soaDynamicQueryMap.put("INV_MAIN_AR_Sale_Per_Id", CompanyReportConfigConstants.INV_MAIN_AR_Sale_Per_Id);
            soaDynamicQueryMap.put("INV_MAIN_AR_knockOffAmount", CompanyReportConfigConstants.INV_MAIN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("INV_MAIN_AR_knockOffAmountInBase", CompanyReportConfigConstants.INV_MAIN_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("INV_MAIN_AR_OPN_knockOffAmount", CompanyReportConfigConstants.INV_MAIN_OPN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("INV_MAIN_AR_OPN_knockOffAmountInBase", CompanyReportConfigConstants.INV_MAIN_OPN_KNOCK_OFF_AMT_BASE);
            
            
            ///Invoice 
            
            soaDynamicQueryMap.put("INV_AR_Type", CompanyReportConfigConstants.INV_Type);
            soaDynamicQueryMap.put("INV_AR_Cust_Id", CompanyReportConfigConstants.CUST_ID);
            soaDynamicQueryMap.put("INV_AR_Cust_Name", CompanyReportConfigConstants.CUST_NAME);
            soaDynamicQueryMap.put("INV_AR_Cust_Code", CompanyReportConfigConstants.CUST_accCode);
            soaDynamicQueryMap.put("INV_AR_Cust_Curr", CompanyReportConfigConstants.Cust_Curr);
            soaDynamicQueryMap.put("INV_AR_Cust_Alise", CompanyReportConfigConstants.Cust_Alise);
            soaDynamicQueryMap.put("INV_AR_Term_Name", CompanyReportConfigConstants.INV_AR_Term_Name);
            soaDynamicQueryMap.put("INV_AR_REF_ID", CompanyReportConfigConstants.INV_REF_ID);
            soaDynamicQueryMap.put("INV_AR_JEID", CompanyReportConfigConstants.INV_JEID);
            soaDynamicQueryMap.put("INV_AR_OPN_JEID", CompanyReportConfigConstants.INV_AR_OPN_JEID);
            soaDynamicQueryMap.put("INV_AR_Amt", CompanyReportConfigConstants.INV_AR_Amt);
            soaDynamicQueryMap.put("INV_AR_Amt_Base", CompanyReportConfigConstants.INV_AR_Amt_Base);
            soaDynamicQueryMap.put("INV_AR_OPN_Amt", CompanyReportConfigConstants.INV_AR_OPN_Amt);
            soaDynamicQueryMap.put("INV_AR_OPN_Amt_Base", CompanyReportConfigConstants.INV_AR_OPN_Amt_Base);
            soaDynamicQueryMap.put("INV_AR_Without_Inventry", CompanyReportConfigConstants.INV_AR_Without_Inventry);
            soaDynamicQueryMap.put("INV_AR_JE_Createdon", CompanyReportConfigConstants.INV_JE_ENTRYDATE);
            soaDynamicQueryMap.put("INV_AR_OPN_JE_Createdon", CompanyReportConfigConstants.INV_AR_OPN_JE_Createdon);
            soaDynamicQueryMap.put("INV_AR_Doc_Createdon", CompanyReportConfigConstants.INV_AR_Doc_Createdon);
            soaDynamicQueryMap.put("INV_AR_Opening_Trans", CompanyReportConfigConstants.INV_AR_Opening_Trans);
            soaDynamicQueryMap.put("INV_AR_Company_Id", CompanyReportConfigConstants.INV_AR_Company_Id);
            soaDynamicQueryMap.put("INV_AR_Company_Name", CompanyReportConfigConstants.INV_AR_Company_Name);
            soaDynamicQueryMap.put("INV_AR_DOC_NUMBER", CompanyReportConfigConstants.INV_DOC_NUMBER);
            soaDynamicQueryMap.put("INV_AR_Trans_Curr", CompanyReportConfigConstants.INV_CURR_ID);
            soaDynamicQueryMap.put("INV_AR_Trans_CurrSymbol", CompanyReportConfigConstants.INV_CURR_SYMBOL);
            soaDynamicQueryMap.put("INV_AR_Trans_CurrName", CompanyReportConfigConstants.INV_AR_Trans_CurrName);
            soaDynamicQueryMap.put("INV_AR_Ext_Curr_Rate", CompanyReportConfigConstants.INV_AR_Ext_Curr_Rate);
            soaDynamicQueryMap.put("INV_AR_ExcahgeRate", CompanyReportConfigConstants.INV_AR_ExcahgeRate);
            soaDynamicQueryMap.put("INV_AR_OPN_Ext_Curr_Rate", CompanyReportConfigConstants.INV_AR_OPN_Ext_Curr_Rate);
            soaDynamicQueryMap.put("INV_AR_OPN_ExcahgeRate", CompanyReportConfigConstants.INV_AR_OPN_ExcahgeRate);
            soaDynamicQueryMap.put("INV_AR_Ship_Date", CompanyReportConfigConstants.INV_AR_Ship_Date);
            soaDynamicQueryMap.put("INV_AR_Due_Date", CompanyReportConfigConstants.INV_AR_Due_Date);
            soaDynamicQueryMap.put("INV_AR_JE_ENTRYNO", CompanyReportConfigConstants.INV_JE_ENTRYNO);
            soaDynamicQueryMap.put("INV_AR_OPN_JE_ENTRYNO", CompanyReportConfigConstants.INV_OPN_JE_ENTRYNO);
            soaDynamicQueryMap.put("INV_AR_MEMO", CompanyReportConfigConstants.INV_AR_MEMO);
            soaDynamicQueryMap.put("INV_AR_Sale_Per_Name", CompanyReportConfigConstants.INV_AR_Sale_Per_Name);
            soaDynamicQueryMap.put("INV_AR_Sale_Per_Code", CompanyReportConfigConstants.INV_AR_Sale_Per_Code);
            soaDynamicQueryMap.put("INV_AR_Sale_Per_Id", CompanyReportConfigConstants.INV_AR_Sale_Per_Id);
            soaDynamicQueryMap.put("INV_AR_knockOffAmount", CompanyReportConfigConstants.INV_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("INV_AR_knockOffAmountInBase", CompanyReportConfigConstants.INV_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("INV_AR_OPN_knockOffAmount", CompanyReportConfigConstants.INV_OPN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("INV_AR_OPN_knockOffAmountInBase", CompanyReportConfigConstants.INV_OPN_KNOCK_OFF_AMT_BASE);
            
            
            // Knock Off
            soaDynamicQueryMap.put("INV_AR_CND_knockOffAmount", CompanyReportConfigConstants.INV_CND_KNOCK_OFF_AMT);            
            soaDynamicQueryMap.put("INV_AR_CND_knockOffAmountInBase", CompanyReportConfigConstants.INV_CND_KNOCK_OFF_AMT_BASE);            
            soaDynamicQueryMap.put("INV_AR_RD_knockOffAmount", CompanyReportConfigConstants.INV_RD_KNOCK_OFF_AMT);            
            soaDynamicQueryMap.put("INV_AR_RD_knockOffAmountInBase", CompanyReportConfigConstants.INV_RD_KNOCK_OFF_AMT_BASE);            
            soaDynamicQueryMap.put("INV_AR_LDR_knockOffAmount", CompanyReportConfigConstants.INV_LDR_KNOCK_OFF_AMT);            
            soaDynamicQueryMap.put("INV_AR_LDR_knockOffAmountInBase", CompanyReportConfigConstants.INV_LDR_KNOCK_OFF_AMT_BASE);            
            soaDynamicQueryMap.put("INV_AR_WRITEOFF_knockOffAmount", CompanyReportConfigConstants.INV_WRITEOFF_KNOCK_OFF_AMT);            
            soaDynamicQueryMap.put("INV_AR_WRITEOFF_knockOffAmountInBase", CompanyReportConfigConstants.INV_WRITEOFF_KNOCK_OFF_AMT_BASE);            
           
            // Knock Off For Opening Document 
            soaDynamicQueryMap.put("INV_AR_CND_OPN_knockOffAmount", CompanyReportConfigConstants.INV_CND_OPN_KNOCK_OFF_AMT);            
            soaDynamicQueryMap.put("INV_AR_CND_OPN_knockOffAmountInBase", CompanyReportConfigConstants.INV_CND_OPN_KNOCK_OFF_AMT_BASE);            
            soaDynamicQueryMap.put("INV_AR_RD_OPN_knockOffAmount", CompanyReportConfigConstants.INV_RD_OPN_KNOCK_OFF_AMT);            
            soaDynamicQueryMap.put("INV_AR_RD_OPN_knockOffAmountInBase", CompanyReportConfigConstants.INV_RD_OPN_KNOCK_OFF_AMT_BASE);            
            soaDynamicQueryMap.put("INV_AR_LDR_OPN_knockOffAmount", CompanyReportConfigConstants.INV_LDR_OPN_KNOCK_OFF_AMT);            
            soaDynamicQueryMap.put("INV_AR_LDR_OPN_knockOffAmountInBase", CompanyReportConfigConstants.INV_LDR_OPN_KNOCK_OFF_AMT_BASE);            
            soaDynamicQueryMap.put("INV_AR_WRITEOFF_OPN_knockOffAmount", CompanyReportConfigConstants.INV_WRITEOFF_OPN_KNOCK_OFF_AMT);            
            soaDynamicQueryMap.put("INV_AR_WRITEOFF_OPN_knockOffAmountInBase", CompanyReportConfigConstants.INV_WRITEOFF_OPN_KNOCK_OFF_AMT_BASE);            
           
           
            //CN Main
            
            soaDynamicQueryMap.put("CN_MAIN_AR_Type", CompanyReportConfigConstants.CN_MAIN_AR_Type);
            soaDynamicQueryMap.put("CN_MAIN_AR_Cust_Id", CompanyReportConfigConstants.CN_MAIN_CUST_ID);
            soaDynamicQueryMap.put("CN_MAIN_AR_Cust_Name", CompanyReportConfigConstants.CN_MAIN_CUST_NAME);
            soaDynamicQueryMap.put("CN_MAIN_AR_Cust_Code", CompanyReportConfigConstants.CN_MAIN_CUST_accCode);
            soaDynamicQueryMap.put("CN_MAIN_AR_Cust_Curr", CompanyReportConfigConstants.CN_MAIN_AR_Cust_Curr);
            soaDynamicQueryMap.put("CN_MAIN_AR_Cust_Alise", CompanyReportConfigConstants.CN_MAIN_AR_Cust_Alise);
            soaDynamicQueryMap.put("CN_MAIN_AR_Term_Name", CompanyReportConfigConstants.CN_MAIN_AR_Term_Name);
            soaDynamicQueryMap.put("CN_MAIN_AR_REF_ID", CompanyReportConfigConstants.CN_MAIN_REF_ID);
            soaDynamicQueryMap.put("CN_MAIN_AR_JEID", CompanyReportConfigConstants.CN_MAIN_JEID);
            soaDynamicQueryMap.put("CN_MAIN_AR_OPN_JEID", CompanyReportConfigConstants.CN_MAIN_AR_OPN_JEID);
            soaDynamicQueryMap.put("CN_MAIN_AR_Amt", CompanyReportConfigConstants.CN_MAIN_AR_Amt);
            soaDynamicQueryMap.put("CN_MAIN_AR_Amt_Base", CompanyReportConfigConstants.CN_MAIN_AR_Amt_Base);
            soaDynamicQueryMap.put("CN_MAIN_AR_OPN_Amt", CompanyReportConfigConstants.CN_MAIN_AR_OPN_Amt);
            soaDynamicQueryMap.put("CN_MAIN_AR_OPN_Amt_Base", CompanyReportConfigConstants.CN_MAIN_AR_OPN_Amt_Base);
            soaDynamicQueryMap.put("CN_MAIN_AR_Without_Inventry", CompanyReportConfigConstants.CN_MAIN_AR_Without_Inventry);
            soaDynamicQueryMap.put("CN_MAIN_AR_JE_Createdon", CompanyReportConfigConstants.CN_MAIN_JE_ENTRYDATE);
            soaDynamicQueryMap.put("CN_MAIN_AR_OPN_JE_Createdon", CompanyReportConfigConstants.CN_MAIN_AR_OPN_JE_Createdon);
            soaDynamicQueryMap.put("CN_MAIN_AR_Doc_Createdon", CompanyReportConfigConstants.CN_MAIN_AR_Doc_Createdon);
            soaDynamicQueryMap.put("CN_MAIN_AR_Opening_Trans", CompanyReportConfigConstants.CN_MAIN_AR_Opening_Trans);
            soaDynamicQueryMap.put("CN_MAIN_AR_Company_Id", CompanyReportConfigConstants.CN_MAIN_AR_Company_Id);
            soaDynamicQueryMap.put("CN_MAIN_AR_Company_Name", CompanyReportConfigConstants.CN_MAIN_AR_Company_Name);
            soaDynamicQueryMap.put("CN_MAIN_AR_DOC_NUMBER", CompanyReportConfigConstants.CN_MAIN_DOC_NUMBER);
            soaDynamicQueryMap.put("CN_MAIN_AR_Trans_Curr", CompanyReportConfigConstants.CN_MAIN_CURR_ID);
            soaDynamicQueryMap.put("CN_MAIN_AR_Trans_CurrSymbol", CompanyReportConfigConstants.CN_MAIN_CURR_SYMBOL);
            soaDynamicQueryMap.put("CN_MAIN_AR_Trans_CurrName", CompanyReportConfigConstants.CN_MAIN_AR_Trans_CurrName);
            soaDynamicQueryMap.put("CN_MAIN_AR_Ext_Curr_Rate", CompanyReportConfigConstants.CN_MAIN_AR_Ext_Curr_Rate);
            soaDynamicQueryMap.put("CN_MAIN_AR_ExcahgeRate", CompanyReportConfigConstants.CN_MAIN_AR_ExcahgeRate);
            soaDynamicQueryMap.put("CN_MAIN_AR_OPN_Ext_Curr_Rate", CompanyReportConfigConstants.CN_MAIN_AR_OPN_Ext_Curr_Rate);
            soaDynamicQueryMap.put("CN_MAIN_AR_OPN_ExcahgeRate", CompanyReportConfigConstants.CN_MAIN_AR_OPN_ExcahgeRate);
            soaDynamicQueryMap.put("CN_MAIN_AR_Ship_Date", CompanyReportConfigConstants.CN_MAIN_AR_Ship_Date);
            soaDynamicQueryMap.put("CN_MAIN_AR_Due_Date", CompanyReportConfigConstants.CN_MAIN_AR_Due_Date);
            soaDynamicQueryMap.put("CN_MAIN_AR_JE_ENTRYNO", CompanyReportConfigConstants.CN_MAIN_JE_ENTRYNO);
            soaDynamicQueryMap.put("CN_MAIN_AR_OPN_JE_ENTRYNO", CompanyReportConfigConstants.CN_MAIN_OPN_JE_ENTRYNO);
            soaDynamicQueryMap.put("CN_MAIN_AR_MEMO", CompanyReportConfigConstants.CN_MAIN_AR_MEMO);
            soaDynamicQueryMap.put("CN_MAIN_AR_Sale_Per_Name", CompanyReportConfigConstants.CN_MAIN_AR_Sale_Per_Name);
            soaDynamicQueryMap.put("CN_MAIN_AR_Sale_Per_Code", CompanyReportConfigConstants.CN_MAIN_AR_Sale_Per_Code);
            soaDynamicQueryMap.put("CN_MAIN_AR_Sale_Per_Id", CompanyReportConfigConstants.CN_MAIN_AR_Sale_Per_Id);
            soaDynamicQueryMap.put("CN_MAIN_AR_knockOffAmount", CompanyReportConfigConstants.CN_MAIN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("CN_MAIN_AR_knockOffAmountInBase", CompanyReportConfigConstants.CN_MAIN_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("CN_MAIN_AR_OPN_knockOffAmount", CompanyReportConfigConstants.CN_MAIN_OPN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("CN_MAIN_AR_OPN_knockOffAmountInBase", CompanyReportConfigConstants.CN_MAIN_OPN_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("CN_MAIN_AR_NOTE_TYPE", CompanyReportConfigConstants.CN_MAIN_AR_CNTYPE);
            
            
            ///CN 
            
            soaDynamicQueryMap.put("CN_AR_Type", CompanyReportConfigConstants.CN_AR_Type);
            soaDynamicQueryMap.put("CN_AR_Cust_Id", CompanyReportConfigConstants.CUST_ID);
            soaDynamicQueryMap.put("CN_AR_Cust_Name", CompanyReportConfigConstants.CUST_NAME);
            soaDynamicQueryMap.put("CN_AR_Cust_Code", CompanyReportConfigConstants.CUST_accCode);
            soaDynamicQueryMap.put("CN_AR_Cust_Curr", CompanyReportConfigConstants.Cust_Curr);
            soaDynamicQueryMap.put("CN_AR_Cust_Alise", CompanyReportConfigConstants.Cust_Alise);
            soaDynamicQueryMap.put("CN_AR_Term_Name", CompanyReportConfigConstants.CN_AR_Term_Name);
            soaDynamicQueryMap.put("CN_AR_REF_ID", CompanyReportConfigConstants.CN_REF_ID);
            soaDynamicQueryMap.put("CN_AR_JEID", CompanyReportConfigConstants.CN_JEID);
            soaDynamicQueryMap.put("CN_AR_OPN_JEID", CompanyReportConfigConstants.CN_AR_OPN_JEID);
            soaDynamicQueryMap.put("CN_AR_Amt", CompanyReportConfigConstants.CN_AR_Amt);
            soaDynamicQueryMap.put("CN_AR_Amt_Base", CompanyReportConfigConstants.CN_AR_Amt_Base);
            soaDynamicQueryMap.put("CN_AR_OPN_Amt", CompanyReportConfigConstants.CN_AR_OPN_Amt);
            soaDynamicQueryMap.put("CN_AR_OPN_Amt_Base", CompanyReportConfigConstants.CN_AR_OPN_Amt_Base);
            soaDynamicQueryMap.put("CN_AR_Without_Inventry", CompanyReportConfigConstants.CN_AR_Without_Inventry);
            soaDynamicQueryMap.put("CN_AR_JE_Createdon", CompanyReportConfigConstants.CN_JE_ENTRYDATE);
            soaDynamicQueryMap.put("CN_AR_OPN_JE_Createdon", CompanyReportConfigConstants.CN_AR_OPN_JE_Createdon);
            soaDynamicQueryMap.put("CN_AR_Doc_Createdon", CompanyReportConfigConstants.CN_AR_Doc_Createdon);
            soaDynamicQueryMap.put("CN_AR_Opening_Trans", CompanyReportConfigConstants.CN_AR_Opening_Trans);
            soaDynamicQueryMap.put("CN_AR_Company_Id", CompanyReportConfigConstants.CN_AR_Company_Id);
            soaDynamicQueryMap.put("CN_AR_Company_Name", CompanyReportConfigConstants.CN_AR_Company_Name);
            soaDynamicQueryMap.put("CN_AR_DOC_NUMBER", CompanyReportConfigConstants.CN_DOC_NUMBER);
            soaDynamicQueryMap.put("CN_AR_Trans_Curr", CompanyReportConfigConstants.CN_CURR_ID);
            soaDynamicQueryMap.put("CN_AR_Trans_CurrSymbol", CompanyReportConfigConstants.CN_CURR_SYMBOL);
            soaDynamicQueryMap.put("CN_AR_Trans_CurrName", CompanyReportConfigConstants.CN_AR_Trans_CurrName);
            soaDynamicQueryMap.put("CN_AR_Ext_Curr_Rate", CompanyReportConfigConstants.CN_AR_Ext_Curr_Rate);
            soaDynamicQueryMap.put("CN_AR_ExcahgeRate", CompanyReportConfigConstants.CN_AR_ExcahgeRate);
            soaDynamicQueryMap.put("CN_AR_OPN_Ext_Curr_Rate", CompanyReportConfigConstants.CN_AR_OPN_Ext_Curr_Rate);
            soaDynamicQueryMap.put("CN_AR_OPN_ExcahgeRate", CompanyReportConfigConstants.CN_AR_OPN_ExcahgeRate);
            soaDynamicQueryMap.put("CN_AR_Ship_Date", CompanyReportConfigConstants.CN_AR_Ship_Date);
            soaDynamicQueryMap.put("CN_AR_Due_Date", CompanyReportConfigConstants.CN_AR_Due_Date);
            soaDynamicQueryMap.put("CN_AR_JE_ENTRYNO", CompanyReportConfigConstants.CN_JE_ENTRYNO);
            soaDynamicQueryMap.put("CN_AR_OPN_JE_ENTRYNO", CompanyReportConfigConstants.CN_OPN_JE_ENTRYNO);
            soaDynamicQueryMap.put("CN_AR_MEMO", CompanyReportConfigConstants.CN_AR_MEMO);
            soaDynamicQueryMap.put("CN_AR_Sale_Per_Name", CompanyReportConfigConstants.CN_AR_Sale_Per_Name);
            soaDynamicQueryMap.put("CN_AR_Sale_Per_Code", CompanyReportConfigConstants.CN_AR_Sale_Per_Code);
            soaDynamicQueryMap.put("CN_AR_Sale_Per_Id", CompanyReportConfigConstants.CN_AR_Sale_Per_Id);
            soaDynamicQueryMap.put("CN_AR_knockOffAmount", CompanyReportConfigConstants.CN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("CN_AR_knockOffAmountInBase", CompanyReportConfigConstants.CN_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("CN_AR_OPN_knockOffAmount", CompanyReportConfigConstants.CN_OPN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("CN_AR_OPN_knockOffAmountInBase", CompanyReportConfigConstants.CN_OPN_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("CN_AR_NOTE_TYPE", CompanyReportConfigConstants.CN_AR_CNTYPE);
            
            soaDynamicQueryMap.put("CN_FRX_Cust_Id", CompanyReportConfigConstants.CUST_ID);
            soaDynamicQueryMap.put("CN_FRX_Cust_Name", CompanyReportConfigConstants.CUST_NAME);
            soaDynamicQueryMap.put("CN_FRX_Cust_code", CompanyReportConfigConstants.CUST_accCode);
            soaDynamicQueryMap.put("CN_FRX_knockOffAmount", CompanyReportConfigConstants.CN_FRX_KNOCK_OFF_AMT);            
            soaDynamicQueryMap.put("CN_FRX_knockOffAmountInBase", CompanyReportConfigConstants.CN_FRX_KNOCK_OFF_AMT_BASE); 
            soaDynamicQueryMap.put("CN_FRX_Type", "'Credit Note' as type");
            soaDynamicQueryMap.put("CN_FRX_REF_ID", CompanyReportConfigConstants.CN_FRX_REF_ID);
            soaDynamicQueryMap.put("CN_FRX_DOC_NUMBER", CompanyReportConfigConstants.CN_FRX_DOC_NUMBER);
            soaDynamicQueryMap.put("CN_FRX_JEID", CompanyReportConfigConstants.CN_FRX_JEID);
            soaDynamicQueryMap.put("CN_FRX_MEMO", CompanyReportConfigConstants.CN_FRX_MEMO);
            soaDynamicQueryMap.put("CN_FRX_Trans_Curr", CompanyReportConfigConstants.CN_FRX_CURR_ID);
            soaDynamicQueryMap.put("CN_FRX_Trans_CurrSymbol", CompanyReportConfigConstants.CN_FRX_CURR_SYMBOL);
            soaDynamicQueryMap.put("CN_FRX_JE_ENTRYNO", CompanyReportConfigConstants.CN_FRX_JE_ENTRYNO);
            soaDynamicQueryMap.put("CN_FRX_JE_Createdon", CompanyReportConfigConstants.CN_FRX_JE_ENTRYDATE);
            soaDynamicQueryMap.put("CN_FRX_Ext_Curr_Rate", CompanyReportConfigConstants.CN_FRX_JE_EXT_CURR_RATE);
 
            soaDynamicQueryMap.put("CN_FRX_amountDue", CompanyReportConfigConstants.CN_FRX_AMOUNT_DUE);
            soaDynamicQueryMap.put("CN_FRX_debitAmountInBase", CompanyReportConfigConstants.CN_FRX_DEBIT_AMT_BASE);            
            soaDynamicQueryMap.put("CN_FRX_transactionAmountInBase", CompanyReportConfigConstants.CN_FRX_TRANSACTION_AMT_BASE);            
            soaDynamicQueryMap.put("CN_FRX_debitAmount", CompanyReportConfigConstants.CN_FRX_DEBIT_AMT);
            soaDynamicQueryMap.put("CN_FRX_creditAmountInBase", CompanyReportConfigConstants.CN_FRX_CREDIT_AMT_BASE);            
            soaDynamicQueryMap.put("CN_FRX_creditAmount", CompanyReportConfigConstants.CN_FRX_CREDIT_AMT);
 
            
            // Knock Off
            soaDynamicQueryMap.put("CN_AR_LDR_knockOffAmount", CompanyReportConfigConstants.CN_LDR_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("CN_AR_LDR_knockOffAmountInBase", CompanyReportConfigConstants.CN_LDR_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("CN_AR_CNP_knockOffAmount", CompanyReportConfigConstants.CN_CNP_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("CN_AR_CNP_knockOffAmountInBase", CompanyReportConfigConstants.CN_CNP_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("CN_AR_DIS_knockOffAmount", CompanyReportConfigConstants.CN_DIS_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("CN_AR_DIS_knockOffAmountInBase", CompanyReportConfigConstants.CN_DIS_KNOCK_OFF_AMT_BASE);

            // Knock Off Opening
            soaDynamicQueryMap.put("CN_AR_LDR_OPN_knockOffAmount", CompanyReportConfigConstants.CN_LDR_OPN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("CN_AR_LDR_OPN_knockOffAmountInBase", CompanyReportConfigConstants.CN_LDR_OPN_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("CN_AR_CNP_OPN_knockOffAmount", CompanyReportConfigConstants.CN_CNP_OPN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("CN_AR_CNP_OPN_knockOffAmountInBase", CompanyReportConfigConstants.CN_CNP_OPN_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("CN_AR_DIS_OPN_knockOffAmount", CompanyReportConfigConstants.CN_DIS_OPN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("CN_AR_DIS_OPN_knockOffAmountInBase", CompanyReportConfigConstants.CN_DIS_OPN_KNOCK_OFF_AMT_BASE);
            
            //DN Main
            
            soaDynamicQueryMap.put("DN_MAIN_AR_Type", CompanyReportConfigConstants.DN_MAIN_AR_Type);
            soaDynamicQueryMap.put("DN_MAIN_AR_Cust_Id", CompanyReportConfigConstants.DN_MAIN_CUST_ID);
            soaDynamicQueryMap.put("DN_MAIN_AR_Cust_Name", CompanyReportConfigConstants.DN_MAIN_CUST_NAME);
            soaDynamicQueryMap.put("DN_MAIN_AR_Cust_Code", CompanyReportConfigConstants.DN_MAIN_CUST_accCode);
            soaDynamicQueryMap.put("DN_MAIN_AR_Cust_Curr", CompanyReportConfigConstants.DN_MAIN_AR_Cust_Curr);
            soaDynamicQueryMap.put("DN_MAIN_AR_Cust_Alise", CompanyReportConfigConstants.DN_MAIN_AR_Cust_Alise);
            soaDynamicQueryMap.put("DN_MAIN_AR_Term_Name", CompanyReportConfigConstants.DN_MAIN_AR_Term_Name);
            soaDynamicQueryMap.put("DN_MAIN_AR_REF_ID", CompanyReportConfigConstants.DN_MAIN_REF_ID);
            soaDynamicQueryMap.put("DN_MAIN_AR_JEID", CompanyReportConfigConstants.DN_MAIN_JEID);
            soaDynamicQueryMap.put("DN_MAIN_AR_OPN_JEID", CompanyReportConfigConstants.DN_MAIN_AR_OPN_JEID);
            soaDynamicQueryMap.put("DN_MAIN_AR_Amt", CompanyReportConfigConstants.DN_MAIN_AR_Amt);
            soaDynamicQueryMap.put("DN_MAIN_AR_Amt_Base", CompanyReportConfigConstants.DN_MAIN_AR_Amt_Base);
            soaDynamicQueryMap.put("DN_MAIN_AR_OPN_Amt", CompanyReportConfigConstants.DN_MAIN_AR_OPN_Amt);
            soaDynamicQueryMap.put("DN_MAIN_AR_OPN_Amt_Base", CompanyReportConfigConstants.DN_MAIN_AR_OPN_Amt_Base);
            soaDynamicQueryMap.put("DN_MAIN_AR_Without_Inventry", CompanyReportConfigConstants.DN_MAIN_AR_Without_Inventry);
            soaDynamicQueryMap.put("DN_MAIN_AR_JE_Createdon", CompanyReportConfigConstants.DN_MAIN_JE_ENTRYDATE);
            soaDynamicQueryMap.put("DN_MAIN_AR_OPN_JE_Createdon", CompanyReportConfigConstants.DN_MAIN_AR_OPN_JE_Createdon);
            soaDynamicQueryMap.put("DN_MAIN_AR_Doc_Createdon", CompanyReportConfigConstants.DN_MAIN_AR_Doc_Createdon);
//            soaDynamicQueryMap.put("DN_MAIN_AR_knockOffAmount", CompanyReportConfigConstants.DN_MAIN_AR_KNOCK_OFF_AMT);
//            soaDynamicQueryMap.put("DN_MAIN_AR_knockOffAmountInBase", CompanyReportConfigConstants.DN_MAIN_AR_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("DN_MAIN_AR_Opening_Trans", CompanyReportConfigConstants.DN_MAIN_AR_Opening_Trans);
            soaDynamicQueryMap.put("DN_MAIN_AR_Company_Id", CompanyReportConfigConstants.DN_MAIN_AR_Company_Id);
            soaDynamicQueryMap.put("DN_MAIN_AR_Company_Name", CompanyReportConfigConstants.DN_MAIN_AR_Company_Name);
            soaDynamicQueryMap.put("DN_MAIN_AR_DOC_NUMBER", CompanyReportConfigConstants.DN_MAIN_DOC_NUMBER);
            soaDynamicQueryMap.put("DN_MAIN_AR_Trans_Curr", CompanyReportConfigConstants.DN_MAIN_CURR_ID);
            soaDynamicQueryMap.put("DN_MAIN_AR_Trans_CurrSymbol", CompanyReportConfigConstants.DN_MAIN_CURR_SYMBOL);
            soaDynamicQueryMap.put("DN_MAIN_AR_Trans_CurrName", CompanyReportConfigConstants.DN_MAIN_AR_Trans_CurrName);
            soaDynamicQueryMap.put("DN_MAIN_AR_Ext_Curr_Rate", CompanyReportConfigConstants.DN_MAIN_AR_Ext_Curr_Rate);
            soaDynamicQueryMap.put("DN_MAIN_AR_ExcahgeRate", CompanyReportConfigConstants.DN_MAIN_AR_ExcahgeRate);
            soaDynamicQueryMap.put("DN_MAIN_AR_OPN_Ext_Curr_Rate", CompanyReportConfigConstants.DN_MAIN_AR_OPN_Ext_Curr_Rate);
            soaDynamicQueryMap.put("DN_MAIN_AR_OPN_ExcahgeRate", CompanyReportConfigConstants.DN_MAIN_AR_OPN_ExcahgeRate);
            soaDynamicQueryMap.put("DN_MAIN_AR_Ship_Date", CompanyReportConfigConstants.DN_MAIN_AR_Ship_Date);
            soaDynamicQueryMap.put("DN_MAIN_AR_Due_Date", CompanyReportConfigConstants.DN_MAIN_AR_Due_Date);
            soaDynamicQueryMap.put("DN_MAIN_AR_JE_ENTRYNO", CompanyReportConfigConstants.DN_MAIN_JE_ENTRYNO);
            soaDynamicQueryMap.put("DN_MAIN_AR_OPN_JE_ENTRYNO", CompanyReportConfigConstants.DN_MAIN_OPN_JE_ENTRYNO);
            soaDynamicQueryMap.put("DN_MAIN_AR_MEMO", CompanyReportConfigConstants.DN_MAIN_AR_MEMO);
            soaDynamicQueryMap.put("DN_MAIN_AR_Sale_Per_Name", CompanyReportConfigConstants.DN_MAIN_AR_Sale_Per_Name);
            soaDynamicQueryMap.put("DN_MAIN_AR_Sale_Per_Code", CompanyReportConfigConstants.DN_MAIN_AR_Sale_Per_Code);
            soaDynamicQueryMap.put("DN_MAIN_AR_Sale_Per_Id", CompanyReportConfigConstants.DN_MAIN_AR_Sale_Per_Id);
            soaDynamicQueryMap.put("DN_MAIN_AR_knockOffAmount", CompanyReportConfigConstants.DN_MAIN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("DN_MAIN_AR_knockOffAmountInBase", CompanyReportConfigConstants.DN_MAIN_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("DN_MAIN_AR_OPN_knockOffAmount", CompanyReportConfigConstants.DN_MAIN_OPN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("DN_MAIN_AR_OPN_knockOffAmountInBase", CompanyReportConfigConstants.DN_MAIN_OPN_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("DN_MAIN_AR_NOTE_TYPE", CompanyReportConfigConstants.DN_MAIN_AR_CNTYPE);
            
            
            ///DN 
            
            soaDynamicQueryMap.put("DN_AR_Type", CompanyReportConfigConstants.DN_AR_Type);
            soaDynamicQueryMap.put("DN_AR_Cust_Id", CompanyReportConfigConstants.CUST_ID);
            soaDynamicQueryMap.put("DN_AR_Cust_Name", CompanyReportConfigConstants.CUST_NAME);
            soaDynamicQueryMap.put("DN_AR_Cust_Code", CompanyReportConfigConstants.CUST_accCode);
            soaDynamicQueryMap.put("DN_AR_Cust_Curr", CompanyReportConfigConstants.Cust_Curr);
            soaDynamicQueryMap.put("DN_AR_Cust_Alise", CompanyReportConfigConstants.Cust_Alise);
            soaDynamicQueryMap.put("DN_AR_Term_Name", CompanyReportConfigConstants.DN_AR_Term_Name);
            soaDynamicQueryMap.put("DN_AR_REF_ID", CompanyReportConfigConstants.DN_REF_ID);
            soaDynamicQueryMap.put("DN_AR_JEID", CompanyReportConfigConstants.DN_JEID);
            soaDynamicQueryMap.put("DN_AR_OPN_JEID", CompanyReportConfigConstants.DN_AR_OPN_JEID);
            soaDynamicQueryMap.put("DN_AR_Amt", CompanyReportConfigConstants.DN_AR_Amt);
            soaDynamicQueryMap.put("DN_AR_Amt_Base", CompanyReportConfigConstants.DN_AR_Amt_Base);
            soaDynamicQueryMap.put("DN_AR_OPN_Amt", CompanyReportConfigConstants.DN_AR_OPN_Amt);
            soaDynamicQueryMap.put("DN_AR_OPN_Amt_Base", CompanyReportConfigConstants.DN_AR_OPN_Amt_Base);
            soaDynamicQueryMap.put("DN_AR_Without_Inventry", CompanyReportConfigConstants.DN_AR_Without_Inventry);
            soaDynamicQueryMap.put("DN_AR_JE_Createdon", CompanyReportConfigConstants.DN_JE_ENTRYDATE);
            soaDynamicQueryMap.put("DN_AR_OPN_JE_Createdon", CompanyReportConfigConstants.DN_AR_OPN_JE_Createdon);
            soaDynamicQueryMap.put("DN_AR_Doc_Createdon", CompanyReportConfigConstants.DN_AR_Doc_Createdon);
            soaDynamicQueryMap.put("DN_AR_Opening_Trans", CompanyReportConfigConstants.DN_AR_Opening_Trans);
            soaDynamicQueryMap.put("DN_AR_Company_Id", CompanyReportConfigConstants.DN_AR_Company_Id);
            soaDynamicQueryMap.put("DN_AR_Company_Name", CompanyReportConfigConstants.DN_AR_Company_Name);
            soaDynamicQueryMap.put("DN_AR_DOC_NUMBER", CompanyReportConfigConstants.DN_DOC_NUMBER);
            soaDynamicQueryMap.put("DN_AR_Trans_Curr", CompanyReportConfigConstants.DN_CURR_ID);
            soaDynamicQueryMap.put("DN_AR_Trans_CurrSymbol", CompanyReportConfigConstants.DN_CURR_SYMBOL);
            soaDynamicQueryMap.put("DN_AR_Trans_CurrName", CompanyReportConfigConstants.DN_AR_Trans_CurrName);
            soaDynamicQueryMap.put("DN_AR_Ext_Curr_Rate", CompanyReportConfigConstants.DN_AR_Ext_Curr_Rate);
            soaDynamicQueryMap.put("DN_AR_ExcahgeRate", CompanyReportConfigConstants.DN_AR_ExcahgeRate);
            soaDynamicQueryMap.put("DN_AR_OPN_Ext_Curr_Rate", CompanyReportConfigConstants.DN_AR_OPN_Ext_Curr_Rate);
            soaDynamicQueryMap.put("DN_AR_OPN_ExcahgeRate", CompanyReportConfigConstants.DN_AR_OPN_ExcahgeRate);
            soaDynamicQueryMap.put("DN_AR_Ship_Date", CompanyReportConfigConstants.DN_AR_Ship_Date);
            soaDynamicQueryMap.put("DN_AR_Due_Date", CompanyReportConfigConstants.DN_AR_Due_Date);
            soaDynamicQueryMap.put("DN_AR_JE_ENTRYNO", CompanyReportConfigConstants.DN_JE_ENTRYNO);
            soaDynamicQueryMap.put("DN_AR_OPN_JE_ENTRYNO", CompanyReportConfigConstants.DN_OPN_JE_ENTRYNO);
            soaDynamicQueryMap.put("DN_AR_MEMO", CompanyReportConfigConstants.DN_AR_MEMO);
            soaDynamicQueryMap.put("DN_AR_Sale_Per_Name", CompanyReportConfigConstants.DN_AR_Sale_Per_Name);
            soaDynamicQueryMap.put("DN_AR_Sale_Per_Code", CompanyReportConfigConstants.DN_AR_Sale_Per_Code);
            soaDynamicQueryMap.put("DN_AR_Sale_Per_Id", CompanyReportConfigConstants.DN_AR_Sale_Per_Id);
            soaDynamicQueryMap.put("DN_AR_knockOffAmount", CompanyReportConfigConstants.DN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("DN_AR_knockOffAmountInBase", CompanyReportConfigConstants.DN_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("DN_AR_OPN_knockOffAmount", CompanyReportConfigConstants.DN_OPN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("DN_AR_OPN_knockOffAmountInBase", CompanyReportConfigConstants.DN_OPN_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("DN_AR_NOTE_TYPE", CompanyReportConfigConstants.DN_AR_CNTYPE);
            
            // Knock Off
            
            soaDynamicQueryMap.put("DN_AR_LDR_KNOCK_OFF_AMT ", CompanyReportConfigConstants.DN_LDR_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("DN_AR_LDR_KNOCK_OFF_AMT_BASE ", CompanyReportConfigConstants.DN_LDR_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("DN_AR_DNP_knockOffAmount", CompanyReportConfigConstants.DN_DNP_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("DN_AR_DNP_knockOffAmountInBase", CompanyReportConfigConstants.DN_DNP_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("DN_AR_DIS_knockOffAmount", CompanyReportConfigConstants.DN_DIS_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("DN_AR_DIS_knockOffAmountInBase", CompanyReportConfigConstants.DN_DIS_KNOCK_OFF_AMT_BASE);
            
            // Knock Off Opening
            
            soaDynamicQueryMap.put("DN_AR_LDR_OPN_KNOCK_OFF_AMT ", CompanyReportConfigConstants.DN_LDR_OPN_KNOCK_OFF_AMT );
            soaDynamicQueryMap.put("DN_AR_LDR_OPN_KNOCK_OFF_AMT_BASE ", CompanyReportConfigConstants.DN_LDR_OPN_KNOCK_OFF_AMT_BASE );
            soaDynamicQueryMap.put("DN_AR_DNP_OPN_knockOffAmount", CompanyReportConfigConstants.DN_DNP_OPN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("DN_AR_DNP_OPN_knockOffAmountInBase", CompanyReportConfigConstants.DN_DNP_OPN_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("DN_AR_DIS_OPN_knockOffAmount", CompanyReportConfigConstants.DN_DIS_OPN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("DN_AR_DIS_OPN_knockOffAmountInBase", CompanyReportConfigConstants.DN_DIS_OPN_KNOCK_OFF_AMT_BASE);
            
            
            //Payment Made
            
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_Type", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Type);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_Cust_Id", CompanyReportConfigConstants.PAYMENT_MAIN_CUST_ID);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_Cust_Name", CompanyReportConfigConstants.PAYMENT_MAIN_CUST_NAME);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_Cust_Code", CompanyReportConfigConstants.PAYMENT_MAIN_CUST_accCode);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_Cust_Curr", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Cust_Curr);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_Cust_Alise", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Cust_Alise);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_Term_Name", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Term_Name);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_REF_ID", CompanyReportConfigConstants.PAYMENT_MAIN_REF_ID);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_JEID", CompanyReportConfigConstants.PAYMENT_MAIN_JEID);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_Amt", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Amt);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_Amt_Base", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Amt_Base);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_Without_Inventry", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Without_Inventry);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_JE_Createdon", CompanyReportConfigConstants.PAYMENT_MAIN_JE_ENTRYDATE);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_Doc_Createdon", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Doc_Createdon);
//            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_knockOffAmount", CompanyReportConfigConstants.PAYMENT_MAIN_AR_KNOCK_OFF_AMT);
//            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_knockOffAmountInBase", CompanyReportConfigConstants.PAYMENT_MAIN_AR_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_Opening_Trans", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Opening_Trans);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_Company_Id", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Company_Id);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_Company_Name", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Company_Name);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_DOC_NUMBER", CompanyReportConfigConstants.PAYMENT_MAIN_DOC_NUMBER);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_Trans_Curr", CompanyReportConfigConstants.PAYMENT_MAIN_CURR_ID);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_Trans_CurrSymbol", CompanyReportConfigConstants.PAYMENT_MAIN_CURR_SYMBOL);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_Trans_CurrName", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Trans_CurrName);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_Ext_Curr_Rate", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Ext_Curr_Rate);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_ExcahgeRate", CompanyReportConfigConstants.PAYMENT_MAIN_AR_ExcahgeRate);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_Ship_Date", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Ship_Date);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_Due_Date", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Due_Date);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_JE_ENTRYNO", CompanyReportConfigConstants.PAYMENT_MAIN_JE_ENTRYNO);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_MEMO", CompanyReportConfigConstants.PAYMENT_MAIN_AR_MEMO);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_Sale_Per_Name", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Sale_Per_Name);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_Sale_Per_Code", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Sale_Per_Code);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_Sale_Per_Id", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Sale_Per_Id);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_knockOffAmount", CompanyReportConfigConstants.PAYMENT_MAIN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("PAYMENT_MAIN_AR_knockOffAmountInBase", CompanyReportConfigConstants.PAYMENT_MAIN_KNOCK_OFF_AMT_BASE);
            
            
            ///PAYMENT 
            
            soaDynamicQueryMap.put("PAYMENT_AR_Type", CompanyReportConfigConstants.PAYMENT_AR_Type);
            soaDynamicQueryMap.put("PAYMENT_AR_Cust_Id", CompanyReportConfigConstants.CUST_ID);
            soaDynamicQueryMap.put("PAYMENT_AR_Cust_Name", CompanyReportConfigConstants.CUST_NAME);
            soaDynamicQueryMap.put("PAYMENT_AR_Cust_Code", CompanyReportConfigConstants.CUST_accCode);
            soaDynamicQueryMap.put("PAYMENT_AR_Cust_Curr", CompanyReportConfigConstants.Cust_Curr);
            soaDynamicQueryMap.put("PAYMENT_AR_Cust_Alise", CompanyReportConfigConstants.Cust_Alise);
            soaDynamicQueryMap.put("PAYMENT_AR_Term_Name", CompanyReportConfigConstants.PAYMENT_AR_Term_Name);
            soaDynamicQueryMap.put("PAYMENT_AR_REF_ID", CompanyReportConfigConstants.PAYMENT_REF_ID);
            soaDynamicQueryMap.put("PAYMENT_AR_JEID", CompanyReportConfigConstants.PAYMENT_JEID);
            soaDynamicQueryMap.put("PAYMENT_AR_Amt", CompanyReportConfigConstants.PAYMENT_AR_Amt);
            soaDynamicQueryMap.put("PAYMENT_AR_Amt_Base", CompanyReportConfigConstants.PAYMENT_AR_Amt_Base);
            soaDynamicQueryMap.put("PAYMENT_AR_Without_Inventry", CompanyReportConfigConstants.PAYMENT_AR_Without_Inventry);
            soaDynamicQueryMap.put("PAYMENT_AR_JE_Createdon", CompanyReportConfigConstants.PAYMENT_JE_ENTRYDATE);
            soaDynamicQueryMap.put("PAYMENT_AR_Doc_Createdon", CompanyReportConfigConstants.PAYMENT_AR_Doc_Createdon);
            soaDynamicQueryMap.put("PAYMENT_AR_Opening_Trans", CompanyReportConfigConstants.PAYMENT_AR_Opening_Trans);
            soaDynamicQueryMap.put("PAYMENT_AR_Company_Id", CompanyReportConfigConstants.PAYMENT_AR_Company_Id);
            soaDynamicQueryMap.put("PAYMENT_AR_Company_Name", CompanyReportConfigConstants.PAYMENT_AR_Company_Name);
            soaDynamicQueryMap.put("PAYMENT_AR_DOC_NUMBER", CompanyReportConfigConstants.PAYMENT_DOC_NUMBER);
            soaDynamicQueryMap.put("PAYMENT_AR_Trans_Curr", CompanyReportConfigConstants.PAYMENT_CURR_ID);
            soaDynamicQueryMap.put("PAYMENT_AR_Trans_CurrSymbol", CompanyReportConfigConstants.PAYMENT_CURR_SYMBOL);
            soaDynamicQueryMap.put("PAYMENT_AR_Trans_CurrName", CompanyReportConfigConstants.PAYMENT_AR_Trans_CurrName);
            soaDynamicQueryMap.put("PAYMENT_AR_Ext_Curr_Rate", CompanyReportConfigConstants.PAYMENT_AR_Ext_Curr_Rate);
            soaDynamicQueryMap.put("PAYMENT_AR_ExcahgeRate", CompanyReportConfigConstants.PAYMENT_AR_ExcahgeRate);
            soaDynamicQueryMap.put("PAYMENT_AR_Ship_Date", CompanyReportConfigConstants.PAYMENT_AR_Ship_Date);
            soaDynamicQueryMap.put("PAYMENT_AR_Due_Date", CompanyReportConfigConstants.PAYMENT_AR_Due_Date);
            soaDynamicQueryMap.put("PAYMENT_AR_JE_ENTRYNO", CompanyReportConfigConstants.PAYMENT_JE_ENTRYNO);
            soaDynamicQueryMap.put("PAYMENT_AR_MEMO", CompanyReportConfigConstants.PAYMENT_AR_MEMO);
            soaDynamicQueryMap.put("PAYMENT_AR_Sale_Per_Name", CompanyReportConfigConstants.PAYMENT_AR_Sale_Per_Name);
            soaDynamicQueryMap.put("PAYMENT_AR_Sale_Per_Code", CompanyReportConfigConstants.PAYMENT_AR_Sale_Per_Code);
            soaDynamicQueryMap.put("PAYMENT_AR_Sale_Per_Id", CompanyReportConfigConstants.PAYMENT_AR_Sale_Per_Id);
            soaDynamicQueryMap.put("PAYMENT_AR_knockOffAmount", CompanyReportConfigConstants.PAYMENT_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("PAYMENT_AR_knockOffAmountInBase", CompanyReportConfigConstants.PAYMENT_KNOCK_OFF_AMT_BASE);
            
            // Knock Off
            
            soaDynamicQueryMap.put("PAYMENT_AR_CNP_KNOCK_OFF_AMT ", CompanyReportConfigConstants.PAYMENT_CNP_KNOCK_OFF_AMT );
            soaDynamicQueryMap.put("PAYMENT_AR_CNP_knockOffAmountInBase", CompanyReportConfigConstants.PAYMENT_CNP_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("PAYMENT_AR_LDP_knockOffAmount", CompanyReportConfigConstants.PAYMENT_LDP_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("PAYMENT_AR_LDP_knockOffAmountInBase", CompanyReportConfigConstants.PAYMENT_LDP_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("PAYMENT_AR_LDAP_knockOffAmount", CompanyReportConfigConstants.PAYMENT_LDAP_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("PAYMENT_AR_LDAP_knockOffAmountInBase", CompanyReportConfigConstants.PAYMENT_LDAP_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("PAYMENT_AR_LDPCN_knockOffAmount", CompanyReportConfigConstants.PAYMENT_LDPCN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("PAYMENT_AR_LDPCN_knockOffAmountInBase", CompanyReportConfigConstants.PAYMENT_LDPCN_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("PAYMENT_AR_PO_knockOffAmount", CompanyReportConfigConstants.PAYMENT_PO_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("PAYMENT_AR_PO_knockOffAmountInBase", CompanyReportConfigConstants.PAYMENT_PO_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("PAYMENT_AR_SR_knockOffAmount", CompanyReportConfigConstants.PAYMENT_SR_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("PAYMENT_AR_SR_knockOffAmountInBase", CompanyReportConfigConstants.PAYMENT_SR_KNOCK_OFF_AMT_BASE);
            
            
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_type", CompanyReportConfigConstants.PAYMENT_DISHONOURED_TYPE);
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_accId", CompanyReportConfigConstants.CUST_ID);
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_accName", CompanyReportConfigConstants.CUST_NAME);
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_accCode", CompanyReportConfigConstants.CUST_accCode);
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_billid", CompanyReportConfigConstants.PAYMENT_REF_ID);
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_invoiceNumber", CompanyReportConfigConstants.PAYMENT_DOC_NUMBER);
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_jeId", CompanyReportConfigConstants.PAYMENT_DISHONOURED_JEID);
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_memo", CompanyReportConfigConstants.PAYMENT_MEMO);
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_currencyid", CompanyReportConfigConstants.PAYMENT_CURR_ID);
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_currencysymbol", CompanyReportConfigConstants.PAYMENT_CURR_SYMBOL);
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_currencycode", CompanyReportConfigConstants.PAYMENT_CURR_CODE);
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_jeEntryNumber", CompanyReportConfigConstants.PAYMENT_JE_ENTRYNO);
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_jeEntryDate", CompanyReportConfigConstants.PAYMENT_JE_ENTRYDATE);
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_jeEntryExternalCurrencyRate", CompanyReportConfigConstants.PAYMENT_JE_EXT_CURR_RATE);
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_ExternalCurrencyRate", CompanyReportConfigConstants.PAYMENT_EXT_CURR_RATE);
//            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_OPN_jeEntryNumber", CompanyReportConfigConstants.PAYMENT_OPN_JE_ENTRYNO);
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_OPN_jeEntryDate", CompanyReportConfigConstants.PAYMENT_OPN_JE_ENTRYDATE);
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_OPN_jeEntryExternalCurrencyRate", CompanyReportConfigConstants.PAYMENT_OPN_JE_EXT_CURR_RATE);
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_amountDue", CompanyReportConfigConstants.PAYMENT_AMOUNT_DUE);
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_debitAmountInBase", CompanyReportConfigConstants.PAYMENT_DISHONOURED_DEBIT_AMT_BASE);            
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_transactionAmountInBase", CompanyReportConfigConstants.PAYMENT_DISHONOURED_TRANSACTION_AMT_BASE);            
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_knockOffAmountInBase", CompanyReportConfigConstants.PAYMENT_KNOCK_OFF_AMT_BASE);            
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_knockOffAmount", CompanyReportConfigConstants.PAYMENT_KNOCK_OFF_AMT);            
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_debitAmount", CompanyReportConfigConstants.PAYMENT_DISHONOURED_DEBIT_AMT);
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_creditAmountInBase", CompanyReportConfigConstants.PAYMENT_DISHONOURED_CREDIT_AMT_BASE);            
            soaDynamicQueryMap.put("PAYMENT_DISHONOURED_creditAmount", CompanyReportConfigConstants.PAYMENT_DISHONOURED_CREDIT_AMT);
            
             //Receipt Made
            
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_Type", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Type);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_Cust_Id", CompanyReportConfigConstants.RECEIPT_MAIN_CUST_ID);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_Cust_Name", CompanyReportConfigConstants.RECEIPT_MAIN_CUST_NAME);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_Cust_Code", CompanyReportConfigConstants.RECEIPT_MAIN_CUST_accCode);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_Cust_Curr", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Cust_Curr);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_Cust_Alise", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Cust_Alise);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_Term_Name", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Term_Name);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_REF_ID", CompanyReportConfigConstants.RECEIPT_MAIN_REF_ID);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_JEID", CompanyReportConfigConstants.RECEIPT_MAIN_JEID);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_OPN_JEID", CompanyReportConfigConstants.RECEIPT_MAIN_AR_OPN_JEID);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_Amt", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Amt);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_Amt_Base", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Amt_Base);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_OPN_Amt", CompanyReportConfigConstants.RECEIPT_MAIN_AR_OPN_Amt);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_OPN_Amt_Base", CompanyReportConfigConstants.RECEIPT_MAIN_AR_OPN_Amt_Base);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_Without_Inventry", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Without_Inventry);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_JE_Createdon", CompanyReportConfigConstants.RECEIPT_MAIN_JE_ENTRYDATE);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_OPN_JE_Createdon", CompanyReportConfigConstants.RECEIPT_MAIN_AR_OPN_JE_Createdon);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_Doc_Createdon", CompanyReportConfigConstants.RECEIPT_MAIN_AR_RECEIPT_Createdon);
//            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_knockOffAmount", CompanyReportConfigConstants.RECEIPT_MAIN_AR_KNOCK_OFF_AMT);
//            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_MAIN_AR_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_Opening_Trans", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Opening_rc);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_Company_Id", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Company_Id);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_Company_Name", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Company_Name);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_DOC_NUMBER", CompanyReportConfigConstants.RECEIPT_MAIN_DOC_NUMBER);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_Trans_Curr", CompanyReportConfigConstants.RECEIPT_MAIN_CURR_ID);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_Trans_CurrSymbol", CompanyReportConfigConstants.RECEIPT_MAIN_CURR_SYMBOL);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_Trans_CurrName", CompanyReportConfigConstants.RECEIPT_MAIN_AR_RECEIPT_CurrName);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_Ext_Curr_Rate", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Ext_Curr_Rate);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_ExcahgeRate", CompanyReportConfigConstants.RECEIPT_MAIN_AR_ExcahgeRate);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_OPN_Ext_Curr_Rate", CompanyReportConfigConstants.RECEIPT_MAIN_AR_OPN_Ext_Curr_Rate);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_OPN_ExcahgeRate", CompanyReportConfigConstants.RECEIPT_MAIN_AR_OPN_ExcahgeRate);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_Ship_Date", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Ship_Date);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_Due_Date", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Due_Date);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_JE_ENTRYNO", CompanyReportConfigConstants.RECEIPT_MAIN_JE_ENTRYNO);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_OPN_JE_ENTRYNO", CompanyReportConfigConstants.RECEIPT_MAIN_OPN_JE_ENTRYNO);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_MEMO", CompanyReportConfigConstants.RECEIPT_MAIN_AR_MEMO);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_Sale_Per_Name", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Sale_Per_Name);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_Sale_Per_Code", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Sale_Per_Code);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_Sale_Per_Id", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Sale_Per_Id);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_knockOffAmount", CompanyReportConfigConstants.RECEIPT_MAIN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_MAIN_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_OPN_knockOffAmount", CompanyReportConfigConstants.RECEIPT_MAIN_OPN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("RECEIPT_MAIN_AR_OPN_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_MAIN_OPN_KNOCK_OFF_AMT_BASE);
            
            
            ///RECEIPT 
            
            soaDynamicQueryMap.put("RECEIPT_AR_Type", CompanyReportConfigConstants.RECEIPT_AR_Type);
            soaDynamicQueryMap.put("RECEIPT_AR_Cust_Id", CompanyReportConfigConstants.CUST_ID);
            soaDynamicQueryMap.put("RECEIPT_AR_Cust_Name", CompanyReportConfigConstants.CUST_NAME);
            soaDynamicQueryMap.put("RECEIPT_AR_Cust_Code", CompanyReportConfigConstants.CUST_accCode);
            soaDynamicQueryMap.put("RECEIPT_AR_Cust_Curr", CompanyReportConfigConstants.Cust_Curr);
            soaDynamicQueryMap.put("RECEIPT_AR_Cust_Alise", CompanyReportConfigConstants.Cust_Alise);
            soaDynamicQueryMap.put("RECEIPT_AR_Term_Name", CompanyReportConfigConstants.RECEIPT_AR_Term_Name);
            soaDynamicQueryMap.put("RECEIPT_AR_REF_ID", CompanyReportConfigConstants.RECEIPT_REF_ID);
            soaDynamicQueryMap.put("RECEIPT_AR_JEID", CompanyReportConfigConstants.RECEIPT_JEID);
            soaDynamicQueryMap.put("RECEIPT_AR_OPN_JEID", CompanyReportConfigConstants.RECEIPT_AR_OPN_JEID);
            soaDynamicQueryMap.put("RECEIPT_AR_Amt", CompanyReportConfigConstants.RECEIPT_AR_Amt);
            soaDynamicQueryMap.put("RECEIPT_AR_Amt_Base", CompanyReportConfigConstants.RECEIPT_AR_Amt_Base);
            soaDynamicQueryMap.put("RECEIPT_AR_OPN_Amt", CompanyReportConfigConstants.RECEIPT_AR_OPN_Amt);
            soaDynamicQueryMap.put("RECEIPT_AR_OPN_Amt_Base", CompanyReportConfigConstants.RECEIPT_AR_OPN_Amt_Base);
            soaDynamicQueryMap.put("RECEIPT_AR_Without_Inventry", CompanyReportConfigConstants.RECEIPT_AR_Without_Inventry);
            soaDynamicQueryMap.put("RECEIPT_AR_JE_Createdon", CompanyReportConfigConstants.RECEIPT_JE_ENTRYDATE);
            soaDynamicQueryMap.put("RECEIPT_AR_OPN_JE_Createdon", CompanyReportConfigConstants.RECEIPT_AR_OPN_JE_Createdon);
            soaDynamicQueryMap.put("RECEIPT_AR_Doc_Createdon", CompanyReportConfigConstants.RECEIPT_AR_RECEIPT_Createdon);
            soaDynamicQueryMap.put("RECEIPT_AR_Opening_Trans", CompanyReportConfigConstants.RECEIPT_AR_Opening_rc);
            soaDynamicQueryMap.put("RECEIPT_AR_Company_Id", CompanyReportConfigConstants.RECEIPT_AR_Company_Id);
            soaDynamicQueryMap.put("RECEIPT_AR_Company_Name", CompanyReportConfigConstants.RECEIPT_AR_Company_Name);
            soaDynamicQueryMap.put("RECEIPT_AR_DOC_NUMBER", CompanyReportConfigConstants.RECEIPT_DOC_NUMBER);
            soaDynamicQueryMap.put("RECEIPT_AR_Trans_Curr", CompanyReportConfigConstants.RECEIPT_CURR_ID);
            soaDynamicQueryMap.put("RECEIPT_AR_Trans_CurrSymbol", CompanyReportConfigConstants.RECEIPT_CURR_SYMBOL);
            soaDynamicQueryMap.put("RECEIPT_AR_Trans_CurrName", CompanyReportConfigConstants.RECEIPT_AR_CurrName);
            soaDynamicQueryMap.put("RECEIPT_AR_Ext_Curr_Rate", CompanyReportConfigConstants.RECEIPT_AR_Ext_Curr_Rate);
            soaDynamicQueryMap.put("RECEIPT_AR_ExcahgeRate", CompanyReportConfigConstants.RECEIPT_AR_ExcahgeRate);
            soaDynamicQueryMap.put("RECEIPT_AR_OPN_Ext_Curr_Rate", CompanyReportConfigConstants.RECEIPT_AR_OPN_Ext_Curr_Rate);
            soaDynamicQueryMap.put("RECEIPT_AR_OPN_ExcahgeRate", CompanyReportConfigConstants.RECEIPT_AR_OPN_ExcahgeRate);
            soaDynamicQueryMap.put("RECEIPT_AR_Ship_Date", CompanyReportConfigConstants.RECEIPT_AR_Ship_Date);
            soaDynamicQueryMap.put("RECEIPT_AR_Due_Date", CompanyReportConfigConstants.RECEIPT_AR_Due_Date);
            soaDynamicQueryMap.put("RECEIPT_AR_JE_ENTRYNO", CompanyReportConfigConstants.RECEIPT_JE_ENTRYNO);
            soaDynamicQueryMap.put("RECEIPT_AR_OPN_JE_ENTRYNO", CompanyReportConfigConstants.RECEIPT_OPN_JE_ENTRYNO);
            soaDynamicQueryMap.put("RECEIPT_AR_MEMO", CompanyReportConfigConstants.RECEIPT_AR_MEMO);
            soaDynamicQueryMap.put("RECEIPT_AR_Sale_Per_Name", CompanyReportConfigConstants.RECEIPT_AR_Sale_Per_Name);
            soaDynamicQueryMap.put("RECEIPT_AR_Sale_Per_Code", CompanyReportConfigConstants.RECEIPT_AR_Sale_Per_Code);
            soaDynamicQueryMap.put("RECEIPT_AR_Sale_Per_Id", CompanyReportConfigConstants.RECEIPT_AR_Sale_Per_Id);
            soaDynamicQueryMap.put("RECEIPT_AR_knockOffAmount", CompanyReportConfigConstants.RECEIPT_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("RECEIPT_AR_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("RECEIPT_AR_OPN_knockOffAmount", CompanyReportConfigConstants.RECEIPT_OPN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("RECEIPT_AR_OPN_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_OPN_KNOCK_OFF_AMT_BASE);
            
            //LDR
            
            
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_Type", CompanyReportConfigConstants.RECEIPT_AR_Type);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_Cust_Id", CompanyReportConfigConstants.CUST_ID);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_Cust_Name", CompanyReportConfigConstants.CUST_NAME);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_Cust_Code", CompanyReportConfigConstants.CUST_accCode);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_Cust_Curr", CompanyReportConfigConstants.Cust_Curr);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_Cust_Alise", CompanyReportConfigConstants.Cust_Alise);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_Term_Name", CompanyReportConfigConstants.RECEIPT_AR_Term_Name);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_REF_ID", CompanyReportConfigConstants.RECEIPT_REF_ID);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_JEID", CompanyReportConfigConstants.RECEIPT_JEID);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_Amt", CompanyReportConfigConstants.RECEIPT_LDR_AR_Amt);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_Amt_Base", CompanyReportConfigConstants.RECEIPT_LDR_AR_Amt_Base);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_Without_Inventry", CompanyReportConfigConstants.RECEIPT_AR_Without_Inventry);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_JE_Createdon", CompanyReportConfigConstants.RECEIPT_JE_ENTRYDATE);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_Doc_Createdon", CompanyReportConfigConstants.RECEIPT_AR_RECEIPT_Createdon);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_Opening_Trans", CompanyReportConfigConstants.RECEIPT_AR_Opening_rc);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_Company_Id", CompanyReportConfigConstants.RECEIPT_AR_Company_Id);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_Company_Name", CompanyReportConfigConstants.RECEIPT_AR_Company_Name);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_DOC_NUMBER", CompanyReportConfigConstants.RECEIPT_DOC_NUMBER);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_Trans_Curr", CompanyReportConfigConstants.RECEIPT_CURR_ID);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_Trans_CurrSymbol", CompanyReportConfigConstants.RECEIPT_CURR_SYMBOL);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_Trans_CurrName", CompanyReportConfigConstants.RECEIPT_AR_CurrName);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_Ext_Curr_Rate", CompanyReportConfigConstants.RECEIPT_AR_Ext_Curr_Rate);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_ExcahgeRate", CompanyReportConfigConstants.RECEIPT_AR_ExcahgeRate);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_Ship_Date", CompanyReportConfigConstants.RECEIPT_AR_Ship_Date);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_Due_Date", CompanyReportConfigConstants.RECEIPT_AR_Due_Date);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_JE_ENTRYNO", CompanyReportConfigConstants.RECEIPT_JE_ENTRYNO);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_MEMO", CompanyReportConfigConstants.RECEIPT_AR_MEMO);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_Sale_Per_Name", CompanyReportConfigConstants.RECEIPT_AR_Sale_Per_Name);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_Sale_Per_Code", CompanyReportConfigConstants.RECEIPT_AR_Sale_Per_Code);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_Sale_Per_Id", CompanyReportConfigConstants.RECEIPT_AR_Sale_Per_Id);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_knockOffAmount", CompanyReportConfigConstants.RECEIPT_LDR_AR_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("RECEIPT_LDR_AR_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_LDR_AR_KNOCK_OFF_AMT_BASE);
            
            
            // Knock Off
            
            soaDynamicQueryMap.put("RECEIPT_AR_LP_knockOffAmount", CompanyReportConfigConstants.RECEIPT_LP_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("RECEIPT_AR_LP_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_LP_KNOCK_OFF_AMT_BASE);
//            soaDynamicQueryMap.put("RECEIPT_AR_LDR_knockOffAmount", CompanyReportConfigConstants.RECEIPT_AR_LDR_KNOCK_OFF_AMT);
//            soaDynamicQueryMap.put("RECEIPT_AR_LDR_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_AR_LDR_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("RECEIPT_AR_RD_knockOffAmount", CompanyReportConfigConstants.RECEIPT_RD_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("RECEIPT_AR_RD_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_RD_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("RECEIPT_AR_RDP_knockOffAmount", CompanyReportConfigConstants.RECEIPT_RDP_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("RECEIPT_AR_RDP_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_RDP_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("RECEIPT_AR_ADV_knockOffAmount", CompanyReportConfigConstants.RECEIPT_ADV_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("RECEIPT_AR_ADV_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_ADV_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("RECEIPT_AR_RWO_knockOffAmount", CompanyReportConfigConstants.RECEIPT_RWO_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("RECEIPT_AR_RWO_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_RWO_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("RECEIPT_AR_SI_knockOffAmount", CompanyReportConfigConstants.RECEIPT_SI_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("RECEIPT_AR_SI_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_SI_KNOCK_OFF_AMT_BASE);
            
            
            // Knock Off Opening
            
            soaDynamicQueryMap.put("RECEIPT_AR_LP_OPN_knockOffAmount", CompanyReportConfigConstants.RECEIPT_AR_LP_OPN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("RECEIPT_AR_LP_OPN_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_AR_LP_OPN_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("RECEIPT_AR_LDR_OPN_knockOffAmount", CompanyReportConfigConstants.RECEIPT_AR_LDR_OPN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("RECEIPT_AR_LDR_OPN_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_AR_LDR_OPN_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("RECEIPT_AR_RD_OPN_knockOffAmount", CompanyReportConfigConstants.RECEIPT_AR_RD_OPN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("RECEIPT_AR_RD_OPN_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_AR_RD_OPN_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("RECEIPT_AR_RDP_OPN_knockOffAmount", CompanyReportConfigConstants.RECEIPT_AR_RDP_OPN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("RECEIPT_AR_RDP_OPN_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_AR_RDP_OPN_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("RECEIPT_AR_ADV_OPN_knockOffAmount", CompanyReportConfigConstants.RECEIPT_AR_ADV_OPN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("RECEIPT_AR_ADV_OPN_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_AR_ADV_OPN_KNOCK_OFF_AMT_BASE);
            soaDynamicQueryMap.put("RECEIPT_AR_RWO_OPN_knockOffAmount", CompanyReportConfigConstants.RECEIPT_AR_RWO_OPN_KNOCK_OFF_AMT);
            soaDynamicQueryMap.put("RECEIPT_AR_RWO_OPN_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_AR_RWO_OPN_KNOCK_OFF_AMT_BASE);
//            soaDynamicQueryMap.put("RECEIPT_AR_SI_OPN_knockOffAmount", CompanyReportConfigConstants.RECEIPT_AR_SI_OPN_KNOCK_OFF_AMT);
//            soaDynamicQueryMap.put("RECEIPT_AR_SI_OPN_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_AR_SI_OPN_KNOCK_OFF_AMT_BASE);
            
            // Receipt for Fetch All Option
            
            soaDynamicQueryMap.put("RECEIPT_ALL_type", CompanyReportConfigConstants.RECEIPT_ALL_TYPE);
            soaDynamicQueryMap.put("RECEIPT_ALL_accId", CompanyReportConfigConstants.CUST_ID);
            soaDynamicQueryMap.put("RECEIPT_ALL_accName", CompanyReportConfigConstants.CUST_NAME);
            soaDynamicQueryMap.put("RECEIPT_ALL_accCode", CompanyReportConfigConstants.CUST_accCode);
            soaDynamicQueryMap.put("RECEIPT_ALL_billid", CompanyReportConfigConstants.RECEIPT_ALL_REF_ID);
            soaDynamicQueryMap.put("RECEIPT_ALL_invoiceNumber", CompanyReportConfigConstants.RECEIPT_ALL_DOC_NUMBER);
            soaDynamicQueryMap.put("RECEIPT_ALL_jeId", CompanyReportConfigConstants.RECEIPT_ALL_JEID);
            soaDynamicQueryMap.put("RECEIPT_ALL_memo", CompanyReportConfigConstants.RECEIPT_ALL_MEMO);
            soaDynamicQueryMap.put("RECEIPT_ALL_currencyid", CompanyReportConfigConstants.RECEIPT_ALL_CURR_ID);
            soaDynamicQueryMap.put("RECEIPT_ALL_currencysymbol", CompanyReportConfigConstants.RECEIPT_ALL_CURR_SYMBOL);
            soaDynamicQueryMap.put("RECEIPT_ALL_currencycode", CompanyReportConfigConstants.RECEIPT_ALL_CURR_CODE);
            soaDynamicQueryMap.put("RECEIPT_ALL_jeEntryNumber", CompanyReportConfigConstants.RECEIPT_ALL_JE_ENTRYNO);
            soaDynamicQueryMap.put("RECEIPT_ALL_jeEntryDate", CompanyReportConfigConstants.RECEIPT_ALL_JE_ENTRYDATE);
            soaDynamicQueryMap.put("RECEIPT_ALL_jeEntryExternalCurrencyRate", CompanyReportConfigConstants.RECEIPT_ALL_JE_EXT_CURR_RATE);
            soaDynamicQueryMap.put("RECEIPT_ALL_ExternalCurrencyRate", CompanyReportConfigConstants.RECEIPT_ALL_EXT_CURR_RATE);
            soaDynamicQueryMap.put("RECEIPT_ALL_OPN_jeEntryNumber", CompanyReportConfigConstants.RECEIPT_ALL_OPN_JE_ENTRYNO);
            soaDynamicQueryMap.put("RECEIPT_ALL_OPN_jeEntryDate", CompanyReportConfigConstants.RECEIPT_ALL_OPN_JE_ENTRYDATE);
            soaDynamicQueryMap.put("RECEIPT_ALL_OPN_jeEntryExternalCurrencyRate", CompanyReportConfigConstants.RECEIPT_ALL_OPN_JE_EXT_CURR_RATE);
            soaDynamicQueryMap.put("RECEIPT_ALL_OPN_ExternalCurrencyRate", CompanyReportConfigConstants.RECEIPT_ALL_OPN_EXT_CURR_RATE);
            soaDynamicQueryMap.put("RECEIPT_ALL_amountDue", CompanyReportConfigConstants.RECEIPT_ALL_AMOUNT_DUE);
            soaDynamicQueryMap.put("RECEIPT_ALL_debitAmountInBase", CompanyReportConfigConstants.RECEIPT_ALL_DEBIT_AMT_BASE);            
            soaDynamicQueryMap.put("RECEIPT_ALL_OPN_debitAmountInBase", CompanyReportConfigConstants.RECEIPT_ALL_OPN_DEBIT_AMT_BASE);            
            soaDynamicQueryMap.put("RECEIPT_ALL_transactionAmountInBase", CompanyReportConfigConstants.RECEIPT_ALL_TRANSACTION_AMT_BASE);            
            soaDynamicQueryMap.put("RECEIPT_ALL_OPN_transactionAmountInBase", CompanyReportConfigConstants.RECEIPT_ALL_OPN_TRANSACTION_AMT_BASE);            
            soaDynamicQueryMap.put("RECEIPT_ALL_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_ALL_KNOCK_OFF_AMT_BASE);            
            soaDynamicQueryMap.put("RECEIPT_ALL_OPN_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_ALL_OPN_KNOCK_OFF_AMT_BASE);            
            soaDynamicQueryMap.put("RECEIPT_ALL_knockOffAmount", CompanyReportConfigConstants.RECEIPT_ALL_KNOCK_OFF_AMT);            
            soaDynamicQueryMap.put("RECEIPT_ALL_OPN_knockOffAmount", CompanyReportConfigConstants.RECEIPT_ALL_OPN_KNOCK_OFF_AMT);            
            soaDynamicQueryMap.put("RECEIPT_ALL_debitAmount", CompanyReportConfigConstants.RECEIPT_ALL_DEBIT_AMT);
            soaDynamicQueryMap.put("RECEIPT_ALL_OPN_debitAmount", CompanyReportConfigConstants.RECEIPT_ALL_OPN_DEBIT_AMT);
            soaDynamicQueryMap.put("RECEIPT_ALL_creditAmountInBase", CompanyReportConfigConstants.RECEIPT_ALL_CREDIT_AMT_BASE);            
            soaDynamicQueryMap.put("RECEIPT_ALL_OPN_creditAmountInBase", CompanyReportConfigConstants.RECEIPT_ALL_OPN_CREDIT_AMT_BASE);            
            soaDynamicQueryMap.put("RECEIPT_ALL_creditAmount", CompanyReportConfigConstants.RECEIPT_ALL_CREDIT_AMT);
            soaDynamicQueryMap.put("RECEIPT_ALL_OPN_creditAmount", CompanyReportConfigConstants.RECEIPT_ALL_OPN_CREDIT_AMT);
            
            //ERM-744
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_type", CompanyReportConfigConstants.RECEIPT_DISHONOURED_TYPE);
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_accId", CompanyReportConfigConstants.CUST_ID);
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_accName", CompanyReportConfigConstants.CUST_NAME);
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_accCode", CompanyReportConfigConstants.CUST_accCode);
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_billid", CompanyReportConfigConstants.RECEIPT_REF_ID);
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_invoiceNumber", CompanyReportConfigConstants.RECEIPT_DOC_NUMBER);
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_jeId", CompanyReportConfigConstants.RECEIPT_DISHONOURED_JEID);
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_memo", CompanyReportConfigConstants.RECEIPT_MEMO);
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_currencyid", CompanyReportConfigConstants.RECEIPT_CURR_ID);
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_currencysymbol", CompanyReportConfigConstants.RECEIPT_CURR_SYMBOL);
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_currencycode", CompanyReportConfigConstants.RECEIPT_CURR_CODE);
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_jeEntryNumber", CompanyReportConfigConstants.RECEIPT_JE_ENTRYNO);
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_jeEntryDate", CompanyReportConfigConstants.RECEIPT_JE_ENTRYDATE);
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_jeEntryExternalCurrencyRate", CompanyReportConfigConstants.RECEIPT_JE_EXT_CURR_RATE);
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_ExternalCurrencyRate", CompanyReportConfigConstants.RECEIPT_EXT_CURR_RATE);
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_OPN_jeEntryNumber", CompanyReportConfigConstants.RECEIPT_OPN_JE_ENTRYNO);
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_OPN_jeEntryDate", CompanyReportConfigConstants.RECEIPT_OPN_JE_ENTRYDATE);
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_OPN_jeEntryExternalCurrencyRate", CompanyReportConfigConstants.RECEIPT_OPN_JE_EXT_CURR_RATE);
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_OPN_ExternalCurrencyRate", CompanyReportConfigConstants.RECEIPT_OPN_EXT_CURR_RATE);
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_amountDue", CompanyReportConfigConstants.RECEIPT_AMOUNT_DUE);
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_debitAmountInBase", CompanyReportConfigConstants.RECEIPT_DISHONOURED_DEBIT_AMT_BASE);            
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_OPN_debitAmountInBase", CompanyReportConfigConstants.RECEIPT_DISHONOURED_OPN_DEBIT_AMT_BASE);            
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_transactionAmountInBase", CompanyReportConfigConstants.RECEIPT_DISHONOURED_TRANSACTION_AMT_BASE);            
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_OPN_transactionAmountInBase", CompanyReportConfigConstants.RECEIPT_DISHONOURED_OPN_TRANSACTION_AMT_BASE);            
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_DISHONOURED_KNOCK_OFF_AMT_BASE);            
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_OPN_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_DISHONOURED_OPN_KNOCK_OFF_AMT_BASE);            
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_knockOffAmount", CompanyReportConfigConstants.RECEIPT_DISHONOURED_KNOCK_OFF_AMT);            
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_OPN_knockOffAmount", CompanyReportConfigConstants.RECEIPT_DISHONOURED_OPN_KNOCK_OFF_AMT);            
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_debitAmount", CompanyReportConfigConstants.RECEIPT_DISHONOURED_DEBIT_AMT);
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_OPN_debitAmount", CompanyReportConfigConstants.RECEIPT_DISHONOURED_OPN_DEBIT_AMT);
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_creditAmountInBase", CompanyReportConfigConstants.RECEIPT_DISHONOURED_CREDIT_AMT_BASE);            
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_OPN_creditAmountInBase", CompanyReportConfigConstants.RECEIPT_OPN_CREDIT_AMT_BASE);            
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_creditAmount", CompanyReportConfigConstants.RECEIPT_DISHONOURED_CREDIT_AMT);
            soaDynamicQueryMap.put("RECEIPT_DISHONOURED_OPN_creditAmount", CompanyReportConfigConstants.RECEIPT_DISHONOURED_OPN_CREDIT_AMT);
            
         }
        return soaDynamicQueryMap;
    }

    
    public Map<String, String> getARDynamicQueryMap() {
        arDynamicQueryMap =null;
     
         if (arDynamicQueryMap == null) {
            arDynamicQueryMap = new HashMap<String, String>();
            
            
            // Invoice Main
            
            
            arDynamicQueryMap.put("INV_MAIN_AR_Type", CompanyReportConfigConstants.INV_MAIN_AR_Type);
            arDynamicQueryMap.put("INV_MAIN_AR_Cust_Id", CompanyReportConfigConstants.INV_MAIN_CUST_ID);
            arDynamicQueryMap.put("INV_MAIN_AR_Cust_Name", CompanyReportConfigConstants.INV_MAIN_CUST_NAME);
            arDynamicQueryMap.put("INV_MAIN_AR_Cust_Code", CompanyReportConfigConstants.INV_MAIN_CUST_accCode);
            arDynamicQueryMap.put("INV_MAIN_AR_Cust_Curr", CompanyReportConfigConstants.INV_MAIN_AR_Cust_Curr);
            arDynamicQueryMap.put("INV_MAIN_AR_Cust_Alise", CompanyReportConfigConstants.INV_MAIN_AR_Cust_Alise);
            arDynamicQueryMap.put("INV_MAIN_AR_Term_Name", CompanyReportConfigConstants.INV_MAIN_AR_Term_Name);
            arDynamicQueryMap.put("INV_MAIN_AR_REF_ID", CompanyReportConfigConstants.INV_MAIN_REF_ID);
            arDynamicQueryMap.put("INV_MAIN_AR_JEID", CompanyReportConfigConstants.INV_MAIN_JEID);
            arDynamicQueryMap.put("INV_MAIN_AR_OPN_JEID", CompanyReportConfigConstants.INV_MAIN_AR_OPN_JEID);
            arDynamicQueryMap.put("INV_MAIN_AR_Amt", CompanyReportConfigConstants.INV_MAIN_AR_Amt);
            arDynamicQueryMap.put("INV_MAIN_AR_Amt_Base", CompanyReportConfigConstants.INV_MAIN_AR_Amt_Base);
            arDynamicQueryMap.put("INV_MAIN_AR_OPN_Amt", CompanyReportConfigConstants.INV_MAIN_AR_OPN_Amt);
            arDynamicQueryMap.put("INV_MAIN_AR_OPN_Amt_Base", CompanyReportConfigConstants.INV_MAIN_AR_OPN_Amt_Base);
            arDynamicQueryMap.put("INV_MAIN_AR_Without_Inventry", CompanyReportConfigConstants.INV_MAIN_AR_Without_Inventry);
            arDynamicQueryMap.put("INV_MAIN_AR_JE_Createdon", CompanyReportConfigConstants.INV_MAIN_JE_ENTRYDATE);
            arDynamicQueryMap.put("INV_MAIN_AR_OPN_JE_Createdon", CompanyReportConfigConstants.INV_MAIN_AR_OPN_JE_Createdon);
            arDynamicQueryMap.put("INV_MAIN_AR_Doc_Createdon", CompanyReportConfigConstants.INV_MAIN_AR_Doc_Createdon);
//            arDynamicQueryMap.put("INV_MAIN_AR_knockOffAmount", CompanyReportConfigConstants.INV_MAIN_AR_KNOCK_OFF_AMT);
//            arDynamicQueryMap.put("INV_MAIN_AR__knockOffAmountInBase", CompanyReportConfigConstants.INV_MAIN_AR_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("INV_MAIN_AR_Opening_Trans", CompanyReportConfigConstants.INV_MAIN_AR_Opening_Trans);
            arDynamicQueryMap.put("INV_MAIN_AR_Company_Id", CompanyReportConfigConstants.INV_MAIN_AR_Company_Id);
            arDynamicQueryMap.put("INV_MAIN_AR_Company_Name", CompanyReportConfigConstants.INV_MAIN_AR_Company_Name);
            arDynamicQueryMap.put("INV_MAIN_AR_DOC_NUMBER", CompanyReportConfigConstants.INV_MAIN_DOC_NUMBER);
            arDynamicQueryMap.put("INV_MAIN_AR_Trans_Curr", CompanyReportConfigConstants.INV_MAIN_CURR_ID);
            arDynamicQueryMap.put("INV_MAIN_AR_Trans_CurrSymbol", CompanyReportConfigConstants.INV_MAIN_CURR_SYMBOL);
            arDynamicQueryMap.put("INV_MAIN_AR_Trans_CurrName", CompanyReportConfigConstants.INV_MAIN_AR_Trans_CurrName);
            arDynamicQueryMap.put("INV_MAIN_AR_Ext_Curr_Rate", CompanyReportConfigConstants.INV_MAIN_AR_Ext_Curr_Rate);
            arDynamicQueryMap.put("INV_MAIN_AR_ExcahgeRate", CompanyReportConfigConstants.INV_MAIN_AR_ExcahgeRate);
            arDynamicQueryMap.put("INV_MAIN_AR_OPN_Ext_Curr_Rate", CompanyReportConfigConstants.INV_MAIN_AR_OPN_Ext_Curr_Rate);
            arDynamicQueryMap.put("INV_MAIN_AR_OPN_ExcahgeRate", CompanyReportConfigConstants.INV_MAIN_AR_OPN_ExcahgeRate);
            arDynamicQueryMap.put("INV_MAIN_AR_Ship_Date", CompanyReportConfigConstants.INV_MAIN_AR_Ship_Date);
            arDynamicQueryMap.put("INV_MAIN_AR_Due_Date", CompanyReportConfigConstants.INV_MAIN_AR_Due_Date);
            arDynamicQueryMap.put("INV_MAIN_AR_JE_ENTRYNO", CompanyReportConfigConstants.INV_MAIN_JE_ENTRYNO);
            arDynamicQueryMap.put("INV_MAIN_AR_OPN_JE_ENTRYNO", CompanyReportConfigConstants.INV_MAIN_OPN_JE_ENTRYNO);
            arDynamicQueryMap.put("INV_MAIN_AR_MEMO", CompanyReportConfigConstants.INV_MAIN_AR_MEMO);
            arDynamicQueryMap.put("INV_MAIN_AR_Sale_Per_Name", CompanyReportConfigConstants.INV_MAIN_AR_Sale_Per_Name);
            arDynamicQueryMap.put("INV_MAIN_AR_Sale_Per_Code", CompanyReportConfigConstants.INV_MAIN_AR_Sale_Per_Code);
            arDynamicQueryMap.put("INV_MAIN_AR_Sale_Per_Id", CompanyReportConfigConstants.INV_MAIN_AR_Sale_Per_Id);
            arDynamicQueryMap.put("INV_MAIN_AR_knockOffAmount", CompanyReportConfigConstants.INV_MAIN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("INV_MAIN_AR_knockOffAmountInBase", CompanyReportConfigConstants.INV_MAIN_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("INV_MAIN_AR_OPN_knockOffAmount", CompanyReportConfigConstants.INV_MAIN_OPN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("INV_MAIN_AR_OPN_knockOffAmountInBase", CompanyReportConfigConstants.INV_MAIN_OPN_KNOCK_OFF_AMT_BASE);
            
            
            ///Invoice 
            
            arDynamicQueryMap.put("INV_AR_Type", CompanyReportConfigConstants.INV_Type);
            arDynamicQueryMap.put("INV_AR_Cust_Id", CompanyReportConfigConstants.CUST_ID);
            arDynamicQueryMap.put("INV_AR_Cust_Name", CompanyReportConfigConstants.CUST_NAME);
            arDynamicQueryMap.put("INV_AR_Cust_Code", CompanyReportConfigConstants.CUST_accCode);
            arDynamicQueryMap.put("INV_AR_Cust_Curr", CompanyReportConfigConstants.Cust_Curr);
            arDynamicQueryMap.put("INV_AR_Cust_Alise", CompanyReportConfigConstants.Cust_Alise);
            arDynamicQueryMap.put("INV_AR_Term_Name", CompanyReportConfigConstants.INV_AR_Term_Name);
            arDynamicQueryMap.put("INV_AR_REF_ID", CompanyReportConfigConstants.INV_REF_ID);
            arDynamicQueryMap.put("INV_AR_JEID", CompanyReportConfigConstants.INV_JEID);
            arDynamicQueryMap.put("INV_AR_OPN_JEID", CompanyReportConfigConstants.INV_AR_OPN_JEID);
            arDynamicQueryMap.put("INV_AR_Amt", CompanyReportConfigConstants.INV_AR_Amt);
            arDynamicQueryMap.put("INV_AR_Amt_Base", CompanyReportConfigConstants.INV_AR_Amt_Base);
            arDynamicQueryMap.put("INV_AR_OPN_Amt", CompanyReportConfigConstants.INV_AR_OPN_Amt);
            arDynamicQueryMap.put("INV_AR_OPN_Amt_Base", CompanyReportConfigConstants.INV_AR_OPN_Amt_Base);
            arDynamicQueryMap.put("INV_AR_Without_Inventry", CompanyReportConfigConstants.INV_AR_Without_Inventry);
            arDynamicQueryMap.put("INV_AR_JE_Createdon", CompanyReportConfigConstants.INV_JE_ENTRYDATE);
            arDynamicQueryMap.put("INV_AR_OPN_JE_Createdon", CompanyReportConfigConstants.INV_AR_OPN_JE_Createdon);
            arDynamicQueryMap.put("INV_AR_Doc_Createdon", CompanyReportConfigConstants.INV_AR_Doc_Createdon);
            arDynamicQueryMap.put("INV_AR_Opening_Trans", CompanyReportConfigConstants.INV_AR_Opening_Trans);
            arDynamicQueryMap.put("INV_AR_Company_Id", CompanyReportConfigConstants.INV_AR_Company_Id);
            arDynamicQueryMap.put("INV_AR_Company_Name", CompanyReportConfigConstants.INV_AR_Company_Name);
            arDynamicQueryMap.put("INV_AR_DOC_NUMBER", CompanyReportConfigConstants.INV_DOC_NUMBER);
            arDynamicQueryMap.put("INV_AR_Trans_Curr", CompanyReportConfigConstants.INV_CURR_ID);
            arDynamicQueryMap.put("INV_AR_Trans_CurrSymbol", CompanyReportConfigConstants.INV_CURR_SYMBOL);
            arDynamicQueryMap.put("INV_AR_Trans_CurrName", CompanyReportConfigConstants.INV_AR_Trans_CurrName);
            arDynamicQueryMap.put("INV_AR_Ext_Curr_Rate", CompanyReportConfigConstants.INV_AR_Ext_Curr_Rate);
            arDynamicQueryMap.put("INV_AR_ExcahgeRate", CompanyReportConfigConstants.INV_AR_ExcahgeRate);
            arDynamicQueryMap.put("INV_AR_OPN_Ext_Curr_Rate", CompanyReportConfigConstants.INV_AR_OPN_Ext_Curr_Rate);
            arDynamicQueryMap.put("INV_AR_OPN_ExcahgeRate", CompanyReportConfigConstants.INV_AR_OPN_ExcahgeRate);
            arDynamicQueryMap.put("INV_AR_Ship_Date", CompanyReportConfigConstants.INV_AR_Ship_Date);
            arDynamicQueryMap.put("INV_AR_Due_Date", CompanyReportConfigConstants.INV_AR_Due_Date);
            arDynamicQueryMap.put("INV_AR_JE_ENTRYNO", CompanyReportConfigConstants.INV_JE_ENTRYNO);
            arDynamicQueryMap.put("INV_AR_OPN_JE_ENTRYNO", CompanyReportConfigConstants.INV_OPN_JE_ENTRYNO);
            arDynamicQueryMap.put("INV_AR_MEMO", CompanyReportConfigConstants.INV_AR_MEMO);
            arDynamicQueryMap.put("INV_AR_Sale_Per_Name", CompanyReportConfigConstants.INV_AR_Sale_Per_Name);
            arDynamicQueryMap.put("INV_AR_Sale_Per_Code", CompanyReportConfigConstants.INV_AR_Sale_Per_Code);
            arDynamicQueryMap.put("INV_AR_Sale_Per_Id", CompanyReportConfigConstants.INV_AR_Sale_Per_Id);
            arDynamicQueryMap.put("INV_AR_knockOffAmount", CompanyReportConfigConstants.INV_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("INV_AR_knockOffAmountInBase", CompanyReportConfigConstants.INV_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("INV_AR_OPN_knockOffAmount", CompanyReportConfigConstants.INV_OPN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("INV_AR_OPN_knockOffAmountInBase", CompanyReportConfigConstants.INV_OPN_KNOCK_OFF_AMT_BASE);
            
            
            // Knock Off
            arDynamicQueryMap.put("INV_AR_CND_knockOffAmount", CompanyReportConfigConstants.INV_CND_KNOCK_OFF_AMT);            
            arDynamicQueryMap.put("INV_AR_CND_knockOffAmountInBase", CompanyReportConfigConstants.INV_CND_KNOCK_OFF_AMT_BASE);            
            arDynamicQueryMap.put("INV_AR_RD_knockOffAmount", CompanyReportConfigConstants.INV_RD_KNOCK_OFF_AMT);            
            arDynamicQueryMap.put("INV_AR_RD_knockOffAmountInBase", CompanyReportConfigConstants.INV_RD_KNOCK_OFF_AMT_BASE);            
            arDynamicQueryMap.put("INV_AR_LDR_knockOffAmount", CompanyReportConfigConstants.INV_LDR_KNOCK_OFF_AMT);            
            arDynamicQueryMap.put("INV_AR_LDR_knockOffAmountInBase", CompanyReportConfigConstants.INV_LDR_KNOCK_OFF_AMT_BASE);            
            arDynamicQueryMap.put("INV_AR_WRITEOFF_knockOffAmount", CompanyReportConfigConstants.INV_WRITEOFF_KNOCK_OFF_AMT);            
            arDynamicQueryMap.put("INV_AR_WRITEOFF_knockOffAmountInBase", CompanyReportConfigConstants.INV_WRITEOFF_KNOCK_OFF_AMT_BASE);            
           
            // Knock Off For Opening Document 
            arDynamicQueryMap.put("INV_AR_CND_OPN_knockOffAmount", CompanyReportConfigConstants.INV_CND_OPN_KNOCK_OFF_AMT);            
            arDynamicQueryMap.put("INV_AR_CND_OPN_knockOffAmountInBase", CompanyReportConfigConstants.INV_CND_OPN_KNOCK_OFF_AMT_BASE);            
            arDynamicQueryMap.put("INV_AR_RD_OPN_knockOffAmount", CompanyReportConfigConstants.INV_RD_OPN_KNOCK_OFF_AMT);            
            arDynamicQueryMap.put("INV_AR_RD_OPN_knockOffAmountInBase", CompanyReportConfigConstants.INV_RD_OPN_KNOCK_OFF_AMT_BASE);            
            arDynamicQueryMap.put("INV_AR_LDR_OPN_knockOffAmount", CompanyReportConfigConstants.INV_LDR_OPN_KNOCK_OFF_AMT);            
            arDynamicQueryMap.put("INV_AR_LDR_OPN_knockOffAmountInBase", CompanyReportConfigConstants.INV_LDR_OPN_KNOCK_OFF_AMT_BASE);            
            arDynamicQueryMap.put("INV_AR_WRITEOFF_OPN_knockOffAmount", CompanyReportConfigConstants.INV_WRITEOFF_OPN_KNOCK_OFF_AMT);            
            arDynamicQueryMap.put("INV_AR_WRITEOFF_OPN_knockOffAmountInBase", CompanyReportConfigConstants.INV_WRITEOFF_OPN_KNOCK_OFF_AMT_BASE);            
           
           
            //CN Main
            
            arDynamicQueryMap.put("CN_MAIN_AR_Type", CompanyReportConfigConstants.CN_MAIN_AR_Type);
            arDynamicQueryMap.put("CN_MAIN_AR_Cust_Id", CompanyReportConfigConstants.CN_MAIN_CUST_ID);
            arDynamicQueryMap.put("CN_MAIN_AR_Cust_Name", CompanyReportConfigConstants.CN_MAIN_CUST_NAME);
            arDynamicQueryMap.put("CN_MAIN_AR_Cust_Code", CompanyReportConfigConstants.CN_MAIN_CUST_accCode);
            arDynamicQueryMap.put("CN_MAIN_AR_Cust_Curr", CompanyReportConfigConstants.CN_MAIN_AR_Cust_Curr);
            arDynamicQueryMap.put("CN_MAIN_AR_Cust_Alise", CompanyReportConfigConstants.CN_MAIN_AR_Cust_Alise);
            arDynamicQueryMap.put("CN_MAIN_AR_Term_Name", CompanyReportConfigConstants.CN_MAIN_AR_Term_Name);
            arDynamicQueryMap.put("CN_MAIN_AR_REF_ID", CompanyReportConfigConstants.CN_MAIN_REF_ID);
            arDynamicQueryMap.put("CN_MAIN_AR_JEID", CompanyReportConfigConstants.CN_MAIN_JEID);
            arDynamicQueryMap.put("CN_MAIN_AR_OPN_JEID", CompanyReportConfigConstants.CN_MAIN_AR_OPN_JEID);
            arDynamicQueryMap.put("CN_MAIN_AR_Amt", CompanyReportConfigConstants.CN_MAIN_AR_Amt);
            arDynamicQueryMap.put("CN_MAIN_AR_Amt_Base", CompanyReportConfigConstants.CN_MAIN_AR_Amt_Base);
            arDynamicQueryMap.put("CN_MAIN_AR_OPN_Amt", CompanyReportConfigConstants.CN_MAIN_AR_OPN_Amt);
            arDynamicQueryMap.put("CN_MAIN_AR_OPN_Amt_Base", CompanyReportConfigConstants.CN_MAIN_AR_OPN_Amt_Base);
            arDynamicQueryMap.put("CN_MAIN_AR_Without_Inventry", CompanyReportConfigConstants.CN_MAIN_AR_Without_Inventry);
            arDynamicQueryMap.put("CN_MAIN_AR_JE_Createdon", CompanyReportConfigConstants.CN_MAIN_JE_ENTRYDATE);
            arDynamicQueryMap.put("CN_MAIN_AR_OPN_JE_Createdon", CompanyReportConfigConstants.CN_MAIN_AR_OPN_JE_Createdon);
            arDynamicQueryMap.put("CN_MAIN_AR_Doc_Createdon", CompanyReportConfigConstants.CN_MAIN_AR_Doc_Createdon);
            arDynamicQueryMap.put("CN_MAIN_AR_Opening_Trans", CompanyReportConfigConstants.CN_MAIN_AR_Opening_Trans);
            arDynamicQueryMap.put("CN_MAIN_AR_Company_Id", CompanyReportConfigConstants.CN_MAIN_AR_Company_Id);
            arDynamicQueryMap.put("CN_MAIN_AR_Company_Name", CompanyReportConfigConstants.CN_MAIN_AR_Company_Name);
            arDynamicQueryMap.put("CN_MAIN_AR_DOC_NUMBER", CompanyReportConfigConstants.CN_MAIN_DOC_NUMBER);
            arDynamicQueryMap.put("CN_MAIN_AR_Trans_Curr", CompanyReportConfigConstants.CN_MAIN_CURR_ID);
            arDynamicQueryMap.put("CN_MAIN_AR_Trans_CurrSymbol", CompanyReportConfigConstants.CN_MAIN_CURR_SYMBOL);
            arDynamicQueryMap.put("CN_MAIN_AR_Trans_CurrName", CompanyReportConfigConstants.CN_MAIN_AR_Trans_CurrName);
            arDynamicQueryMap.put("CN_MAIN_AR_Ext_Curr_Rate", CompanyReportConfigConstants.CN_MAIN_AR_Ext_Curr_Rate);
            arDynamicQueryMap.put("CN_MAIN_AR_ExcahgeRate", CompanyReportConfigConstants.CN_MAIN_AR_ExcahgeRate);
            arDynamicQueryMap.put("CN_MAIN_AR_OPN_Ext_Curr_Rate", CompanyReportConfigConstants.CN_MAIN_AR_OPN_Ext_Curr_Rate);
            arDynamicQueryMap.put("CN_MAIN_AR_OPN_ExcahgeRate", CompanyReportConfigConstants.CN_MAIN_AR_OPN_ExcahgeRate);
            arDynamicQueryMap.put("CN_MAIN_AR_Ship_Date", CompanyReportConfigConstants.CN_MAIN_AR_Ship_Date);
            arDynamicQueryMap.put("CN_MAIN_AR_Due_Date", CompanyReportConfigConstants.CN_MAIN_AR_Due_Date);
            arDynamicQueryMap.put("CN_MAIN_AR_JE_ENTRYNO", CompanyReportConfigConstants.CN_MAIN_JE_ENTRYNO);
            arDynamicQueryMap.put("CN_MAIN_AR_OPN_JE_ENTRYNO", CompanyReportConfigConstants.CN_MAIN_OPN_JE_ENTRYNO);
            arDynamicQueryMap.put("CN_MAIN_AR_MEMO", CompanyReportConfigConstants.CN_MAIN_AR_MEMO);
            arDynamicQueryMap.put("CN_MAIN_AR_Sale_Per_Name", CompanyReportConfigConstants.CN_MAIN_AR_Sale_Per_Name);
            arDynamicQueryMap.put("CN_MAIN_AR_Sale_Per_Code", CompanyReportConfigConstants.CN_MAIN_AR_Sale_Per_Code);
            arDynamicQueryMap.put("CN_MAIN_AR_Sale_Per_Id", CompanyReportConfigConstants.CN_MAIN_AR_Sale_Per_Id);
            arDynamicQueryMap.put("CN_MAIN_AR_knockOffAmount", CompanyReportConfigConstants.CN_MAIN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("CN_MAIN_AR_knockOffAmountInBase", CompanyReportConfigConstants.CN_MAIN_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("CN_MAIN_AR_OPN_knockOffAmount", CompanyReportConfigConstants.CN_MAIN_OPN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("CN_MAIN_AR_OPN_knockOffAmountInBase", CompanyReportConfigConstants.CN_MAIN_OPN_KNOCK_OFF_AMT_BASE);
            
            
            ///CN 
            
            arDynamicQueryMap.put("CN_AR_Type", CompanyReportConfigConstants.CN_AR_Type);
            arDynamicQueryMap.put("CN_AR_Cust_Id", CompanyReportConfigConstants.CUST_ID);
            arDynamicQueryMap.put("CN_AR_Cust_Name", CompanyReportConfigConstants.CUST_NAME);
            arDynamicQueryMap.put("CN_AR_Cust_Code", CompanyReportConfigConstants.CUST_accCode);
            arDynamicQueryMap.put("CN_AR_Cust_Curr", CompanyReportConfigConstants.Cust_Curr);
            arDynamicQueryMap.put("CN_AR_Cust_Alise", CompanyReportConfigConstants.Cust_Alise);
            arDynamicQueryMap.put("CN_AR_Term_Name", CompanyReportConfigConstants.CN_AR_Term_Name);
            arDynamicQueryMap.put("CN_AR_REF_ID", CompanyReportConfigConstants.CN_REF_ID);
            arDynamicQueryMap.put("CN_AR_JEID", CompanyReportConfigConstants.CN_JEID);
            arDynamicQueryMap.put("CN_AR_OPN_JEID", CompanyReportConfigConstants.CN_AR_OPN_JEID);
            arDynamicQueryMap.put("CN_AR_Amt", CompanyReportConfigConstants.CN_AR_Amt);
            arDynamicQueryMap.put("CN_AR_Amt_Base", CompanyReportConfigConstants.CN_AR_Amt_Base);
            arDynamicQueryMap.put("CN_AR_OPN_Amt", CompanyReportConfigConstants.CN_AR_OPN_Amt);
            arDynamicQueryMap.put("CN_AR_OPN_Amt_Base", CompanyReportConfigConstants.CN_AR_OPN_Amt_Base);
            arDynamicQueryMap.put("CN_AR_Without_Inventry", CompanyReportConfigConstants.CN_AR_Without_Inventry);
            arDynamicQueryMap.put("CN_AR_JE_Createdon", CompanyReportConfigConstants.CN_JE_ENTRYDATE);
            arDynamicQueryMap.put("CN_AR_OPN_JE_Createdon", CompanyReportConfigConstants.CN_AR_OPN_JE_Createdon);
            arDynamicQueryMap.put("CN_AR_Doc_Createdon", CompanyReportConfigConstants.CN_AR_Doc_Createdon);
            arDynamicQueryMap.put("CN_AR_Opening_Trans", CompanyReportConfigConstants.CN_AR_Opening_Trans);
            arDynamicQueryMap.put("CN_AR_Company_Id", CompanyReportConfigConstants.CN_AR_Company_Id);
            arDynamicQueryMap.put("CN_AR_Company_Name", CompanyReportConfigConstants.CN_AR_Company_Name);
            arDynamicQueryMap.put("CN_AR_DOC_NUMBER", CompanyReportConfigConstants.CN_DOC_NUMBER);
            arDynamicQueryMap.put("CN_AR_Trans_Curr", CompanyReportConfigConstants.CN_CURR_ID);
            arDynamicQueryMap.put("CN_AR_Trans_CurrSymbol", CompanyReportConfigConstants.CN_CURR_SYMBOL);
            arDynamicQueryMap.put("CN_AR_Trans_CurrName", CompanyReportConfigConstants.CN_AR_Trans_CurrName);
            arDynamicQueryMap.put("CN_AR_Ext_Curr_Rate", CompanyReportConfigConstants.CN_AR_Ext_Curr_Rate);
            arDynamicQueryMap.put("CN_AR_ExcahgeRate", CompanyReportConfigConstants.CN_AR_ExcahgeRate);
            arDynamicQueryMap.put("CN_AR_OPN_Ext_Curr_Rate", CompanyReportConfigConstants.CN_AR_OPN_Ext_Curr_Rate);
            arDynamicQueryMap.put("CN_AR_OPN_ExcahgeRate", CompanyReportConfigConstants.CN_AR_OPN_ExcahgeRate);
            arDynamicQueryMap.put("CN_AR_Ship_Date", CompanyReportConfigConstants.CN_AR_Ship_Date);
            arDynamicQueryMap.put("CN_AR_Due_Date", CompanyReportConfigConstants.CN_AR_Due_Date);
            arDynamicQueryMap.put("CN_AR_JE_ENTRYNO", CompanyReportConfigConstants.CN_JE_ENTRYNO);
            arDynamicQueryMap.put("CN_AR_OPN_JE_ENTRYNO", CompanyReportConfigConstants.CN_OPN_JE_ENTRYNO);
            arDynamicQueryMap.put("CN_AR_MEMO", CompanyReportConfigConstants.CN_AR_MEMO);
            arDynamicQueryMap.put("CN_AR_Sale_Per_Name", CompanyReportConfigConstants.CN_AR_Sale_Per_Name);
            arDynamicQueryMap.put("CN_AR_Sale_Per_Code", CompanyReportConfigConstants.CN_AR_Sale_Per_Code);
            arDynamicQueryMap.put("CN_AR_Sale_Per_Id", CompanyReportConfigConstants.CN_AR_Sale_Per_Id);
            arDynamicQueryMap.put("CN_AR_knockOffAmount", CompanyReportConfigConstants.CN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("CN_AR_knockOffAmountInBase", CompanyReportConfigConstants.CN_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("CN_AR_OPN_knockOffAmount", CompanyReportConfigConstants.CN_OPN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("CN_AR_OPN_knockOffAmountInBase", CompanyReportConfigConstants.CN_OPN_KNOCK_OFF_AMT_BASE);
            
            // Knock Off
            arDynamicQueryMap.put("CN_AR_LDR_knockOffAmount", CompanyReportConfigConstants.CN_LDR_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("CN_AR_LDR_knockOffAmountInBase", CompanyReportConfigConstants.CN_LDR_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("CN_AR_CNP_knockOffAmount", CompanyReportConfigConstants.CN_CNP_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("CN_AR_CNP_knockOffAmountInBase", CompanyReportConfigConstants.CN_CNP_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("CN_AR_DIS_knockOffAmount", CompanyReportConfigConstants.CN_DIS_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("CN_AR_DIS_knockOffAmountInBase", CompanyReportConfigConstants.CN_DIS_KNOCK_OFF_AMT_BASE);

            // Knock Off Opening
            arDynamicQueryMap.put("CN_AR_LDR_OPN_knockOffAmount", CompanyReportConfigConstants.CN_LDR_OPN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("CN_AR_LDR_OPN_knockOffAmountInBase", CompanyReportConfigConstants.CN_LDR_OPN_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("CN_AR_CNP_OPN_knockOffAmount", CompanyReportConfigConstants.CN_CNP_OPN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("CN_AR_CNP_OPN_knockOffAmountInBase", CompanyReportConfigConstants.CN_CNP_OPN_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("CN_AR_DIS_OPN_knockOffAmount", CompanyReportConfigConstants.CN_DIS_OPN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("CN_AR_DIS_OPN_knockOffAmountInBase", CompanyReportConfigConstants.CN_DIS_OPN_KNOCK_OFF_AMT_BASE);
            
            //DN Main
            
            arDynamicQueryMap.put("DN_MAIN_AR_Type", CompanyReportConfigConstants.DN_MAIN_AR_Type);
            arDynamicQueryMap.put("DN_MAIN_AR_Cust_Id", CompanyReportConfigConstants.DN_MAIN_CUST_ID);
            arDynamicQueryMap.put("DN_MAIN_AR_Cust_Name", CompanyReportConfigConstants.DN_MAIN_CUST_NAME);
            arDynamicQueryMap.put("DN_MAIN_AR_Cust_Code", CompanyReportConfigConstants.DN_MAIN_CUST_accCode);
            arDynamicQueryMap.put("DN_MAIN_AR_Cust_Curr", CompanyReportConfigConstants.DN_MAIN_AR_Cust_Curr);
            arDynamicQueryMap.put("DN_MAIN_AR_Cust_Alise", CompanyReportConfigConstants.DN_MAIN_AR_Cust_Alise);
            arDynamicQueryMap.put("DN_MAIN_AR_Term_Name", CompanyReportConfigConstants.DN_MAIN_AR_Term_Name);
            arDynamicQueryMap.put("DN_MAIN_AR_REF_ID", CompanyReportConfigConstants.DN_MAIN_REF_ID);
            arDynamicQueryMap.put("DN_MAIN_AR_JEID", CompanyReportConfigConstants.DN_MAIN_JEID);
            arDynamicQueryMap.put("DN_MAIN_AR_OPN_JEID", CompanyReportConfigConstants.DN_MAIN_AR_OPN_JEID);
            arDynamicQueryMap.put("DN_MAIN_AR_Amt", CompanyReportConfigConstants.DN_MAIN_AR_Amt);
            arDynamicQueryMap.put("DN_MAIN_AR_Amt_Base", CompanyReportConfigConstants.DN_MAIN_AR_Amt_Base);
            arDynamicQueryMap.put("DN_MAIN_AR_OPN_Amt", CompanyReportConfigConstants.DN_MAIN_AR_OPN_Amt);
            arDynamicQueryMap.put("DN_MAIN_AR_OPN_Amt_Base", CompanyReportConfigConstants.DN_MAIN_AR_OPN_Amt_Base);
            arDynamicQueryMap.put("DN_MAIN_AR_Without_Inventry", CompanyReportConfigConstants.DN_MAIN_AR_Without_Inventry);
            arDynamicQueryMap.put("DN_MAIN_AR_JE_Createdon", CompanyReportConfigConstants.DN_MAIN_JE_ENTRYDATE);
            arDynamicQueryMap.put("DN_MAIN_AR_OPN_JE_Createdon", CompanyReportConfigConstants.DN_MAIN_AR_OPN_JE_Createdon);
            arDynamicQueryMap.put("DN_MAIN_AR_Doc_Createdon", CompanyReportConfigConstants.DN_MAIN_AR_Doc_Createdon);
//            arDynamicQueryMap.put("DN_MAIN_AR_knockOffAmount", CompanyReportConfigConstants.DN_MAIN_AR_KNOCK_OFF_AMT);
//            arDynamicQueryMap.put("DN_MAIN_AR_knockOffAmountInBase", CompanyReportConfigConstants.DN_MAIN_AR_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("DN_MAIN_AR_Opening_Trans", CompanyReportConfigConstants.DN_MAIN_AR_Opening_Trans);
            arDynamicQueryMap.put("DN_MAIN_AR_Company_Id", CompanyReportConfigConstants.DN_MAIN_AR_Company_Id);
            arDynamicQueryMap.put("DN_MAIN_AR_Company_Name", CompanyReportConfigConstants.DN_MAIN_AR_Company_Name);
            arDynamicQueryMap.put("DN_MAIN_AR_DOC_NUMBER", CompanyReportConfigConstants.DN_MAIN_DOC_NUMBER);
            arDynamicQueryMap.put("DN_MAIN_AR_Trans_Curr", CompanyReportConfigConstants.DN_MAIN_CURR_ID);
            arDynamicQueryMap.put("DN_MAIN_AR_Trans_CurrSymbol", CompanyReportConfigConstants.DN_MAIN_CURR_SYMBOL);
            arDynamicQueryMap.put("DN_MAIN_AR_Trans_CurrName", CompanyReportConfigConstants.DN_MAIN_AR_Trans_CurrName);
            arDynamicQueryMap.put("DN_MAIN_AR_Ext_Curr_Rate", CompanyReportConfigConstants.DN_MAIN_AR_Ext_Curr_Rate);
            arDynamicQueryMap.put("DN_MAIN_AR_ExcahgeRate", CompanyReportConfigConstants.DN_MAIN_AR_ExcahgeRate);
            arDynamicQueryMap.put("DN_MAIN_AR_OPN_Ext_Curr_Rate", CompanyReportConfigConstants.DN_MAIN_AR_OPN_Ext_Curr_Rate);
            arDynamicQueryMap.put("DN_MAIN_AR_OPN_ExcahgeRate", CompanyReportConfigConstants.DN_MAIN_AR_OPN_ExcahgeRate);
            arDynamicQueryMap.put("DN_MAIN_AR_Ship_Date", CompanyReportConfigConstants.DN_MAIN_AR_Ship_Date);
            arDynamicQueryMap.put("DN_MAIN_AR_Due_Date", CompanyReportConfigConstants.DN_MAIN_AR_Due_Date);
            arDynamicQueryMap.put("DN_MAIN_AR_JE_ENTRYNO", CompanyReportConfigConstants.DN_MAIN_JE_ENTRYNO);
            arDynamicQueryMap.put("DN_MAIN_AR_OPN_JE_ENTRYNO", CompanyReportConfigConstants.DN_MAIN_OPN_JE_ENTRYNO);
            arDynamicQueryMap.put("DN_MAIN_AR_MEMO", CompanyReportConfigConstants.DN_MAIN_AR_MEMO);
            arDynamicQueryMap.put("DN_MAIN_AR_Sale_Per_Name", CompanyReportConfigConstants.DN_MAIN_AR_Sale_Per_Name);
            arDynamicQueryMap.put("DN_MAIN_AR_Sale_Per_Code", CompanyReportConfigConstants.DN_MAIN_AR_Sale_Per_Code);
            arDynamicQueryMap.put("DN_MAIN_AR_Sale_Per_Id", CompanyReportConfigConstants.DN_MAIN_AR_Sale_Per_Id);
            arDynamicQueryMap.put("DN_MAIN_AR_knockOffAmount", CompanyReportConfigConstants.DN_MAIN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("DN_MAIN_AR_knockOffAmountInBase", CompanyReportConfigConstants.DN_MAIN_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("DN_MAIN_AR_OPN_knockOffAmount", CompanyReportConfigConstants.DN_MAIN_OPN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("DN_MAIN_AR_OPN_knockOffAmountInBase", CompanyReportConfigConstants.DN_MAIN_OPN_KNOCK_OFF_AMT_BASE);
            
            
            ///DN 
            
            arDynamicQueryMap.put("DN_AR_Type", CompanyReportConfigConstants.DN_AR_Type);
            arDynamicQueryMap.put("DN_AR_Cust_Id", CompanyReportConfigConstants.CUST_ID);
            arDynamicQueryMap.put("DN_AR_Cust_Name", CompanyReportConfigConstants.CUST_NAME);
            arDynamicQueryMap.put("DN_AR_Cust_Code", CompanyReportConfigConstants.CUST_accCode);
            arDynamicQueryMap.put("DN_AR_Cust_Curr", CompanyReportConfigConstants.Cust_Curr);
            arDynamicQueryMap.put("DN_AR_Cust_Alise", CompanyReportConfigConstants.Cust_Alise);
            arDynamicQueryMap.put("DN_AR_Term_Name", CompanyReportConfigConstants.DN_AR_Term_Name);
            arDynamicQueryMap.put("DN_AR_REF_ID", CompanyReportConfigConstants.DN_REF_ID);
            arDynamicQueryMap.put("DN_AR_JEID", CompanyReportConfigConstants.DN_JEID);
            arDynamicQueryMap.put("DN_AR_OPN_JEID", CompanyReportConfigConstants.DN_AR_OPN_JEID);
            arDynamicQueryMap.put("DN_AR_Amt", CompanyReportConfigConstants.DN_AR_Amt);
            arDynamicQueryMap.put("DN_AR_Amt_Base", CompanyReportConfigConstants.DN_AR_Amt_Base);
            arDynamicQueryMap.put("DN_AR_OPN_Amt", CompanyReportConfigConstants.DN_AR_OPN_Amt);
            arDynamicQueryMap.put("DN_AR_OPN_Amt_Base", CompanyReportConfigConstants.DN_AR_OPN_Amt_Base);
            arDynamicQueryMap.put("DN_AR_Without_Inventry", CompanyReportConfigConstants.DN_AR_Without_Inventry);
            arDynamicQueryMap.put("DN_AR_JE_Createdon", CompanyReportConfigConstants.DN_JE_ENTRYDATE);
            arDynamicQueryMap.put("DN_AR_OPN_JE_Createdon", CompanyReportConfigConstants.DN_AR_OPN_JE_Createdon);
            arDynamicQueryMap.put("DN_AR_Doc_Createdon", CompanyReportConfigConstants.DN_AR_Doc_Createdon);
            arDynamicQueryMap.put("DN_AR_Opening_Trans", CompanyReportConfigConstants.DN_AR_Opening_Trans);
            arDynamicQueryMap.put("DN_AR_Company_Id", CompanyReportConfigConstants.DN_AR_Company_Id);
            arDynamicQueryMap.put("DN_AR_Company_Name", CompanyReportConfigConstants.DN_AR_Company_Name);
            arDynamicQueryMap.put("DN_AR_DOC_NUMBER", CompanyReportConfigConstants.DN_DOC_NUMBER);
            arDynamicQueryMap.put("DN_AR_Trans_Curr", CompanyReportConfigConstants.DN_CURR_ID);
            arDynamicQueryMap.put("DN_AR_Trans_CurrSymbol", CompanyReportConfigConstants.DN_CURR_SYMBOL);
            arDynamicQueryMap.put("DN_AR_Trans_CurrName", CompanyReportConfigConstants.DN_AR_Trans_CurrName);
            arDynamicQueryMap.put("DN_AR_Ext_Curr_Rate", CompanyReportConfigConstants.DN_AR_Ext_Curr_Rate);
            arDynamicQueryMap.put("DN_AR_ExcahgeRate", CompanyReportConfigConstants.DN_AR_ExcahgeRate);
            arDynamicQueryMap.put("DN_AR_OPN_Ext_Curr_Rate", CompanyReportConfigConstants.DN_AR_OPN_Ext_Curr_Rate);
            arDynamicQueryMap.put("DN_AR_OPN_ExcahgeRate", CompanyReportConfigConstants.DN_AR_OPN_ExcahgeRate);
            arDynamicQueryMap.put("DN_AR_Ship_Date", CompanyReportConfigConstants.DN_AR_Ship_Date);
            arDynamicQueryMap.put("DN_AR_Due_Date", CompanyReportConfigConstants.DN_AR_Due_Date);
            arDynamicQueryMap.put("DN_AR_JE_ENTRYNO", CompanyReportConfigConstants.DN_JE_ENTRYNO);
            arDynamicQueryMap.put("DN_AR_OPN_JE_ENTRYNO", CompanyReportConfigConstants.DN_OPN_JE_ENTRYNO);
            arDynamicQueryMap.put("DN_AR_MEMO", CompanyReportConfigConstants.DN_AR_MEMO);
            arDynamicQueryMap.put("DN_AR_Sale_Per_Name", CompanyReportConfigConstants.DN_AR_Sale_Per_Name);
            arDynamicQueryMap.put("DN_AR_Sale_Per_Code", CompanyReportConfigConstants.DN_AR_Sale_Per_Code);
            arDynamicQueryMap.put("DN_AR_Sale_Per_Id", CompanyReportConfigConstants.DN_AR_Sale_Per_Id);
            arDynamicQueryMap.put("DN_AR_knockOffAmount", CompanyReportConfigConstants.DN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("DN_AR_knockOffAmountInBase", CompanyReportConfigConstants.DN_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("DN_AR_OPN_knockOffAmount", CompanyReportConfigConstants.DN_OPN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("DN_AR_OPN_knockOffAmountInBase", CompanyReportConfigConstants.DN_OPN_KNOCK_OFF_AMT_BASE);
            
            // Knock Off
            
            arDynamicQueryMap.put("DN_AR_LDR_KNOCK_OFF_AMT ", CompanyReportConfigConstants.DN_LDR_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("DN_AR_LDR_KNOCK_OFF_AMT_BASE ", CompanyReportConfigConstants.DN_LDR_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("DN_AR_DNP_knockOffAmount", CompanyReportConfigConstants.DN_DNP_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("DN_AR_DNP_knockOffAmountInBase", CompanyReportConfigConstants.DN_DNP_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("DN_AR_DIS_knockOffAmount", CompanyReportConfigConstants.DN_DIS_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("DN_AR_DIS_knockOffAmountInBase", CompanyReportConfigConstants.DN_DIS_KNOCK_OFF_AMT_BASE);
            
            // Knock Off Opening
            
            arDynamicQueryMap.put("DN_AR_LDR_OPN_KNOCK_OFF_AMT ", CompanyReportConfigConstants.DN_LDR_OPN_KNOCK_OFF_AMT );
            arDynamicQueryMap.put("DN_AR_LDR_OPN_KNOCK_OFF_AMT_BASE ", CompanyReportConfigConstants.DN_LDR_OPN_KNOCK_OFF_AMT_BASE );
            arDynamicQueryMap.put("DN_AR_DNP_OPN_knockOffAmount", CompanyReportConfigConstants.DN_DNP_OPN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("DN_AR_DNP_OPN_knockOffAmountInBase", CompanyReportConfigConstants.DN_DNP_OPN_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("DN_AR_DIS_OPN_knockOffAmount", CompanyReportConfigConstants.DN_DIS_OPN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("DN_AR_DIS_OPN_knockOffAmountInBase", CompanyReportConfigConstants.DN_DIS_OPN_KNOCK_OFF_AMT_BASE);
            
            
            //Payment Made
            
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_Type", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Type);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_Cust_Id", CompanyReportConfigConstants.PAYMENT_MAIN_CUST_ID);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_Cust_Name", CompanyReportConfigConstants.PAYMENT_MAIN_CUST_NAME);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_Cust_Code", CompanyReportConfigConstants.PAYMENT_MAIN_CUST_accCode);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_Cust_Curr", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Cust_Curr);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_Cust_Alise", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Cust_Alise);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_Term_Name", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Term_Name);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_REF_ID", CompanyReportConfigConstants.PAYMENT_MAIN_REF_ID);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_JEID", CompanyReportConfigConstants.PAYMENT_MAIN_JEID);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_Amt", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Amt);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_Amt_Base", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Amt_Base);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_Without_Inventry", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Without_Inventry);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_JE_Createdon", CompanyReportConfigConstants.PAYMENT_MAIN_JE_ENTRYDATE);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_Doc_Createdon", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Doc_Createdon);
//            arDynamicQueryMap.put("PAYMENT_MAIN_AR_knockOffAmount", CompanyReportConfigConstants.PAYMENT_MAIN_AR_KNOCK_OFF_AMT);
//            arDynamicQueryMap.put("PAYMENT_MAIN_AR_knockOffAmountInBase", CompanyReportConfigConstants.PAYMENT_MAIN_AR_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_Opening_Trans", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Opening_Trans);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_Company_Id", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Company_Id);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_Company_Name", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Company_Name);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_DOC_NUMBER", CompanyReportConfigConstants.PAYMENT_MAIN_DOC_NUMBER);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_Trans_Curr", CompanyReportConfigConstants.PAYMENT_MAIN_CURR_ID);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_Trans_CurrSymbol", CompanyReportConfigConstants.PAYMENT_MAIN_CURR_SYMBOL);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_Trans_CurrName", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Trans_CurrName);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_Ext_Curr_Rate", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Ext_Curr_Rate);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_ExcahgeRate", CompanyReportConfigConstants.PAYMENT_MAIN_AR_ExcahgeRate);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_Ship_Date", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Ship_Date);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_Due_Date", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Due_Date);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_JE_ENTRYNO", CompanyReportConfigConstants.PAYMENT_MAIN_JE_ENTRYNO);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_MEMO", CompanyReportConfigConstants.PAYMENT_MAIN_AR_MEMO);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_Sale_Per_Name", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Sale_Per_Name);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_Sale_Per_Code", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Sale_Per_Code);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_Sale_Per_Id", CompanyReportConfigConstants.PAYMENT_MAIN_AR_Sale_Per_Id);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_knockOffAmount", CompanyReportConfigConstants.PAYMENT_MAIN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("PAYMENT_MAIN_AR_knockOffAmountInBase", CompanyReportConfigConstants.PAYMENT_MAIN_KNOCK_OFF_AMT_BASE);
            
            
            ///PAYMENT 
            
            arDynamicQueryMap.put("PAYMENT_AR_Type", CompanyReportConfigConstants.PAYMENT_AR_Type);
            arDynamicQueryMap.put("PAYMENT_AR_Cust_Id", CompanyReportConfigConstants.CUST_ID);
            arDynamicQueryMap.put("PAYMENT_AR_Cust_Name", CompanyReportConfigConstants.CUST_NAME);
            arDynamicQueryMap.put("PAYMENT_AR_Cust_Code", CompanyReportConfigConstants.CUST_accCode);
            arDynamicQueryMap.put("PAYMENT_AR_Cust_Curr", CompanyReportConfigConstants.Cust_Curr);
            arDynamicQueryMap.put("PAYMENT_AR_Cust_Alise", CompanyReportConfigConstants.Cust_Alise);
            arDynamicQueryMap.put("PAYMENT_AR_Term_Name", CompanyReportConfigConstants.PAYMENT_AR_Term_Name);
            arDynamicQueryMap.put("PAYMENT_AR_REF_ID", CompanyReportConfigConstants.PAYMENT_REF_ID);
            arDynamicQueryMap.put("PAYMENT_AR_JEID", CompanyReportConfigConstants.PAYMENT_JEID);
            arDynamicQueryMap.put("PAYMENT_AR_Amt", CompanyReportConfigConstants.PAYMENT_AR_Amt);
            arDynamicQueryMap.put("PAYMENT_AR_Amt_Base", CompanyReportConfigConstants.PAYMENT_AR_Amt_Base);
            arDynamicQueryMap.put("PAYMENT_AR_Without_Inventry", CompanyReportConfigConstants.PAYMENT_AR_Without_Inventry);
            arDynamicQueryMap.put("PAYMENT_AR_JE_Createdon", CompanyReportConfigConstants.PAYMENT_JE_ENTRYDATE);
            arDynamicQueryMap.put("PAYMENT_AR_Doc_Createdon", CompanyReportConfigConstants.PAYMENT_AR_Doc_Createdon);
            arDynamicQueryMap.put("PAYMENT_AR_Opening_Trans", CompanyReportConfigConstants.PAYMENT_AR_Opening_Trans);
            arDynamicQueryMap.put("PAYMENT_AR_Company_Id", CompanyReportConfigConstants.PAYMENT_AR_Company_Id);
            arDynamicQueryMap.put("PAYMENT_AR_Company_Name", CompanyReportConfigConstants.PAYMENT_AR_Company_Name);
            arDynamicQueryMap.put("PAYMENT_AR_DOC_NUMBER", CompanyReportConfigConstants.PAYMENT_DOC_NUMBER);
            arDynamicQueryMap.put("PAYMENT_AR_Trans_Curr", CompanyReportConfigConstants.PAYMENT_CURR_ID);
            arDynamicQueryMap.put("PAYMENT_AR_Trans_CurrSymbol", CompanyReportConfigConstants.PAYMENT_CURR_SYMBOL);
            arDynamicQueryMap.put("PAYMENT_AR_Trans_CurrName", CompanyReportConfigConstants.PAYMENT_AR_Trans_CurrName);
            arDynamicQueryMap.put("PAYMENT_AR_Ext_Curr_Rate", CompanyReportConfigConstants.PAYMENT_AR_Ext_Curr_Rate);
            arDynamicQueryMap.put("PAYMENT_AR_ExcahgeRate", CompanyReportConfigConstants.PAYMENT_AR_ExcahgeRate);
            arDynamicQueryMap.put("PAYMENT_AR_Ship_Date", CompanyReportConfigConstants.PAYMENT_AR_Ship_Date);
            arDynamicQueryMap.put("PAYMENT_AR_Due_Date", CompanyReportConfigConstants.PAYMENT_AR_Due_Date);
            arDynamicQueryMap.put("PAYMENT_AR_JE_ENTRYNO", CompanyReportConfigConstants.PAYMENT_JE_ENTRYNO);
            arDynamicQueryMap.put("PAYMENT_AR_MEMO", CompanyReportConfigConstants.PAYMENT_AR_MEMO);
            arDynamicQueryMap.put("PAYMENT_AR_Sale_Per_Name", CompanyReportConfigConstants.PAYMENT_AR_Sale_Per_Name);
            arDynamicQueryMap.put("PAYMENT_AR_Sale_Per_Code", CompanyReportConfigConstants.PAYMENT_AR_Sale_Per_Code);
            arDynamicQueryMap.put("PAYMENT_AR_Sale_Per_Id", CompanyReportConfigConstants.PAYMENT_AR_Sale_Per_Id);
            arDynamicQueryMap.put("PAYMENT_AR_knockOffAmount", CompanyReportConfigConstants.PAYMENT_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("PAYMENT_AR_knockOffAmountInBase", CompanyReportConfigConstants.PAYMENT_KNOCK_OFF_AMT_BASE);
            
            // Knock Off
            
            arDynamicQueryMap.put("PAYMENT_AR_CNP_KNOCK_OFF_AMT ", CompanyReportConfigConstants.PAYMENT_CNP_KNOCK_OFF_AMT );
            arDynamicQueryMap.put("PAYMENT_AR_CNP_knockOffAmountInBase", CompanyReportConfigConstants.PAYMENT_CNP_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("PAYMENT_AR_LDP_knockOffAmount", CompanyReportConfigConstants.PAYMENT_LDP_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("PAYMENT_AR_LDP_knockOffAmountInBase", CompanyReportConfigConstants.PAYMENT_LDP_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("PAYMENT_AR_LDAP_knockOffAmount", CompanyReportConfigConstants.PAYMENT_LDAP_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("PAYMENT_AR_LDAP_knockOffAmountInBase", CompanyReportConfigConstants.PAYMENT_LDAP_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("PAYMENT_AR_LDPCN_knockOffAmount", CompanyReportConfigConstants.PAYMENT_LDPCN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("PAYMENT_AR_LDPCN_knockOffAmountInBase", CompanyReportConfigConstants.PAYMENT_LDPCN_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("PAYMENT_AR_PO_knockOffAmount", CompanyReportConfigConstants.PAYMENT_PO_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("PAYMENT_AR_PO_knockOffAmountInBase", CompanyReportConfigConstants.PAYMENT_PO_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("PAYMENT_AR_SR_knockOffAmount", CompanyReportConfigConstants.PAYMENT_SR_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("PAYMENT_AR_SR_knockOffAmountInBase", CompanyReportConfigConstants.PAYMENT_SR_KNOCK_OFF_AMT_BASE);
            
            
             //Receipt Made
            
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_Type", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Type);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_Cust_Id", CompanyReportConfigConstants.RECEIPT_MAIN_CUST_ID);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_Cust_Name", CompanyReportConfigConstants.RECEIPT_MAIN_CUST_NAME);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_Cust_Code", CompanyReportConfigConstants.RECEIPT_MAIN_CUST_accCode);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_Cust_Curr", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Cust_Curr);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_Cust_Alise", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Cust_Alise);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_Term_Name", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Term_Name);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_REF_ID", CompanyReportConfigConstants.RECEIPT_MAIN_REF_ID);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_JEID", CompanyReportConfigConstants.RECEIPT_MAIN_JEID);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_OPN_JEID", CompanyReportConfigConstants.RECEIPT_MAIN_AR_OPN_JEID);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_Amt", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Amt);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_Amt_Base", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Amt_Base);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_OPN_Amt", CompanyReportConfigConstants.RECEIPT_MAIN_AR_OPN_Amt);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_OPN_Amt_Base", CompanyReportConfigConstants.RECEIPT_MAIN_AR_OPN_Amt_Base);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_Without_Inventry", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Without_Inventry);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_JE_Createdon", CompanyReportConfigConstants.RECEIPT_MAIN_JE_ENTRYDATE);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_OPN_JE_Createdon", CompanyReportConfigConstants.RECEIPT_MAIN_AR_OPN_JE_Createdon);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_Doc_Createdon", CompanyReportConfigConstants.RECEIPT_MAIN_AR_RECEIPT_Createdon);
//            arDynamicQueryMap.put("RECEIPT_MAIN_AR_knockOffAmount", CompanyReportConfigConstants.RECEIPT_MAIN_AR_KNOCK_OFF_AMT);
//            arDynamicQueryMap.put("RECEIPT_MAIN_AR_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_MAIN_AR_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_Opening_Trans", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Opening_rc);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_Company_Id", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Company_Id);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_Company_Name", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Company_Name);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_DOC_NUMBER", CompanyReportConfigConstants.RECEIPT_MAIN_DOC_NUMBER);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_Trans_Curr", CompanyReportConfigConstants.RECEIPT_MAIN_CURR_ID);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_Trans_CurrSymbol", CompanyReportConfigConstants.RECEIPT_MAIN_CURR_SYMBOL);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_Trans_CurrName", CompanyReportConfigConstants.RECEIPT_MAIN_AR_RECEIPT_CurrName);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_Ext_Curr_Rate", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Ext_Curr_Rate);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_ExcahgeRate", CompanyReportConfigConstants.RECEIPT_MAIN_AR_ExcahgeRate);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_OPN_Ext_Curr_Rate", CompanyReportConfigConstants.RECEIPT_MAIN_AR_OPN_Ext_Curr_Rate);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_OPN_ExcahgeRate", CompanyReportConfigConstants.RECEIPT_MAIN_AR_OPN_ExcahgeRate);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_Ship_Date", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Ship_Date);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_Due_Date", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Due_Date);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_JE_ENTRYNO", CompanyReportConfigConstants.RECEIPT_MAIN_JE_ENTRYNO);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_OPN_JE_ENTRYNO", CompanyReportConfigConstants.RECEIPT_MAIN_OPN_JE_ENTRYNO);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_MEMO", CompanyReportConfigConstants.RECEIPT_MAIN_AR_MEMO);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_Sale_Per_Name", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Sale_Per_Name);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_Sale_Per_Code", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Sale_Per_Code);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_Sale_Per_Id", CompanyReportConfigConstants.RECEIPT_MAIN_AR_Sale_Per_Id);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_knockOffAmount", CompanyReportConfigConstants.RECEIPT_MAIN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_MAIN_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_OPN_knockOffAmount", CompanyReportConfigConstants.RECEIPT_MAIN_OPN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("RECEIPT_MAIN_AR_OPN_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_MAIN_OPN_KNOCK_OFF_AMT_BASE);
            
            
            ///RECEIPT 
            
            arDynamicQueryMap.put("RECEIPT_AR_Type", CompanyReportConfigConstants.RECEIPT_AR_Type);
            arDynamicQueryMap.put("RECEIPT_AR_Cust_Id", CompanyReportConfigConstants.CUST_ID);
            arDynamicQueryMap.put("RECEIPT_AR_Cust_Name", CompanyReportConfigConstants.CUST_NAME);
            arDynamicQueryMap.put("RECEIPT_AR_Cust_Code", CompanyReportConfigConstants.CUST_accCode);
            arDynamicQueryMap.put("RECEIPT_AR_Cust_Curr", CompanyReportConfigConstants.Cust_Curr);
            arDynamicQueryMap.put("RECEIPT_AR_Cust_Alise", CompanyReportConfigConstants.Cust_Alise);
            arDynamicQueryMap.put("RECEIPT_AR_Term_Name", CompanyReportConfigConstants.RECEIPT_AR_Term_Name);
            arDynamicQueryMap.put("RECEIPT_AR_REF_ID", CompanyReportConfigConstants.RECEIPT_REF_ID);
            arDynamicQueryMap.put("RECEIPT_AR_JEID", CompanyReportConfigConstants.RECEIPT_JEID);
            arDynamicQueryMap.put("RECEIPT_AR_OPN_JEID", CompanyReportConfigConstants.RECEIPT_AR_OPN_JEID);
            arDynamicQueryMap.put("RECEIPT_AR_Amt", CompanyReportConfigConstants.RECEIPT_AR_Amt);
            arDynamicQueryMap.put("RECEIPT_AR_Amt_Base", CompanyReportConfigConstants.RECEIPT_AR_Amt_Base);
            arDynamicQueryMap.put("RECEIPT_AR_OPN_Amt", CompanyReportConfigConstants.RECEIPT_AR_OPN_Amt);
            arDynamicQueryMap.put("RECEIPT_AR_OPN_Amt_Base", CompanyReportConfigConstants.RECEIPT_AR_OPN_Amt_Base);
            arDynamicQueryMap.put("RECEIPT_AR_Without_Inventry", CompanyReportConfigConstants.RECEIPT_AR_Without_Inventry);
            arDynamicQueryMap.put("RECEIPT_AR_JE_Createdon", CompanyReportConfigConstants.RECEIPT_JE_ENTRYDATE);
            arDynamicQueryMap.put("RECEIPT_AR_OPN_JE_Createdon", CompanyReportConfigConstants.RECEIPT_AR_OPN_JE_Createdon);
            arDynamicQueryMap.put("RECEIPT_AR_Doc_Createdon", CompanyReportConfigConstants.RECEIPT_AR_RECEIPT_Createdon);
            arDynamicQueryMap.put("RECEIPT_AR_Opening_Trans", CompanyReportConfigConstants.RECEIPT_AR_Opening_rc);
            arDynamicQueryMap.put("RECEIPT_AR_Company_Id", CompanyReportConfigConstants.RECEIPT_AR_Company_Id);
            arDynamicQueryMap.put("RECEIPT_AR_Company_Name", CompanyReportConfigConstants.RECEIPT_AR_Company_Name);
            arDynamicQueryMap.put("RECEIPT_AR_DOC_NUMBER", CompanyReportConfigConstants.RECEIPT_DOC_NUMBER);
            arDynamicQueryMap.put("RECEIPT_AR_Trans_Curr", CompanyReportConfigConstants.RECEIPT_CURR_ID);
            arDynamicQueryMap.put("RECEIPT_AR_Trans_CurrSymbol", CompanyReportConfigConstants.RECEIPT_CURR_SYMBOL);
            arDynamicQueryMap.put("RECEIPT_AR_Trans_CurrName", CompanyReportConfigConstants.RECEIPT_AR_CurrName);
            arDynamicQueryMap.put("RECEIPT_AR_Ext_Curr_Rate", CompanyReportConfigConstants.RECEIPT_AR_Ext_Curr_Rate);
            arDynamicQueryMap.put("RECEIPT_AR_ExcahgeRate", CompanyReportConfigConstants.RECEIPT_AR_ExcahgeRate);
            arDynamicQueryMap.put("RECEIPT_AR_OPN_Ext_Curr_Rate", CompanyReportConfigConstants.RECEIPT_AR_OPN_Ext_Curr_Rate);
            arDynamicQueryMap.put("RECEIPT_AR_OPN_ExcahgeRate", CompanyReportConfigConstants.RECEIPT_AR_OPN_ExcahgeRate);
            arDynamicQueryMap.put("RECEIPT_AR_Ship_Date", CompanyReportConfigConstants.RECEIPT_AR_Ship_Date);
            arDynamicQueryMap.put("RECEIPT_AR_Due_Date", CompanyReportConfigConstants.RECEIPT_AR_Due_Date);
            arDynamicQueryMap.put("RECEIPT_AR_JE_ENTRYNO", CompanyReportConfigConstants.RECEIPT_JE_ENTRYNO);
            arDynamicQueryMap.put("RECEIPT_AR_OPN_JE_ENTRYNO", CompanyReportConfigConstants.RECEIPT_OPN_JE_ENTRYNO);
            arDynamicQueryMap.put("RECEIPT_AR_MEMO", CompanyReportConfigConstants.RECEIPT_AR_MEMO);
            arDynamicQueryMap.put("RECEIPT_AR_Sale_Per_Name", CompanyReportConfigConstants.RECEIPT_AR_Sale_Per_Name);
            arDynamicQueryMap.put("RECEIPT_AR_Sale_Per_Code", CompanyReportConfigConstants.RECEIPT_AR_Sale_Per_Code);
            arDynamicQueryMap.put("RECEIPT_AR_Sale_Per_Id", CompanyReportConfigConstants.RECEIPT_AR_Sale_Per_Id);
            arDynamicQueryMap.put("RECEIPT_AR_knockOffAmount", CompanyReportConfigConstants.RECEIPT_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("RECEIPT_AR_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("RECEIPT_AR_OPN_knockOffAmount", CompanyReportConfigConstants.RECEIPT_OPN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("RECEIPT_AR_OPN_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_OPN_KNOCK_OFF_AMT_BASE);
            
            //LDR
            
            
            arDynamicQueryMap.put("RECEIPT_LDR_AR_Type", CompanyReportConfigConstants.RECEIPT_AR_Type);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_Cust_Id", CompanyReportConfigConstants.CUST_ID);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_Cust_Name", CompanyReportConfigConstants.CUST_NAME);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_Cust_Code", CompanyReportConfigConstants.CUST_accCode);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_Cust_Curr", CompanyReportConfigConstants.Cust_Curr);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_Cust_Alise", CompanyReportConfigConstants.Cust_Alise);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_Term_Name", CompanyReportConfigConstants.RECEIPT_AR_Term_Name);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_REF_ID", CompanyReportConfigConstants.RECEIPT_REF_ID);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_JEID", CompanyReportConfigConstants.RECEIPT_JEID);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_Amt", CompanyReportConfigConstants.RECEIPT_LDR_AR_Amt);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_Amt_Base", CompanyReportConfigConstants.RECEIPT_LDR_AR_Amt_Base);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_Without_Inventry", CompanyReportConfigConstants.RECEIPT_AR_Without_Inventry);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_JE_Createdon", CompanyReportConfigConstants.RECEIPT_JE_ENTRYDATE);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_Doc_Createdon", CompanyReportConfigConstants.RECEIPT_AR_RECEIPT_Createdon);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_Opening_Trans", CompanyReportConfigConstants.RECEIPT_AR_Opening_rc);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_Company_Id", CompanyReportConfigConstants.RECEIPT_AR_Company_Id);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_Company_Name", CompanyReportConfigConstants.RECEIPT_AR_Company_Name);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_DOC_NUMBER", CompanyReportConfigConstants.RECEIPT_DOC_NUMBER);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_Trans_Curr", CompanyReportConfigConstants.RECEIPT_CURR_ID);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_Trans_CurrSymbol", CompanyReportConfigConstants.RECEIPT_CURR_SYMBOL);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_Trans_CurrName", CompanyReportConfigConstants.RECEIPT_AR_CurrName);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_Ext_Curr_Rate", CompanyReportConfigConstants.RECEIPT_AR_Ext_Curr_Rate);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_ExcahgeRate", CompanyReportConfigConstants.RECEIPT_AR_ExcahgeRate);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_Ship_Date", CompanyReportConfigConstants.RECEIPT_AR_Ship_Date);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_Due_Date", CompanyReportConfigConstants.RECEIPT_AR_Due_Date);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_JE_ENTRYNO", CompanyReportConfigConstants.RECEIPT_JE_ENTRYNO);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_MEMO", CompanyReportConfigConstants.RECEIPT_AR_MEMO);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_Sale_Per_Name", CompanyReportConfigConstants.RECEIPT_AR_Sale_Per_Name);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_Sale_Per_Code", CompanyReportConfigConstants.RECEIPT_AR_Sale_Per_Code);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_Sale_Per_Id", CompanyReportConfigConstants.RECEIPT_AR_Sale_Per_Id);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_knockOffAmount", CompanyReportConfigConstants.RECEIPT_LDR_AR_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("RECEIPT_LDR_AR_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_LDR_AR_KNOCK_OFF_AMT_BASE);
            
            
            // Knock Off
            
            arDynamicQueryMap.put("RECEIPT_AR_LP_knockOffAmount", CompanyReportConfigConstants.RECEIPT_LP_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("RECEIPT_AR_LP_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_LP_KNOCK_OFF_AMT_BASE);
//            arDynamicQueryMap.put("RECEIPT_AR_LDR_knockOffAmount", CompanyReportConfigConstants.RECEIPT_AR_LDR_KNOCK_OFF_AMT);
//            arDynamicQueryMap.put("RECEIPT_AR_LDR_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_AR_LDR_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("RECEIPT_AR_RD_knockOffAmount", CompanyReportConfigConstants.RECEIPT_RD_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("RECEIPT_AR_RD_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_RD_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("RECEIPT_AR_RDP_knockOffAmount", CompanyReportConfigConstants.RECEIPT_RDP_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("RECEIPT_AR_RDP_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_RDP_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("RECEIPT_AR_ADV_knockOffAmount", CompanyReportConfigConstants.RECEIPT_ADV_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("RECEIPT_AR_ADV_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_ADV_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("RECEIPT_AR_RWO_knockOffAmount", CompanyReportConfigConstants.RECEIPT_RWO_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("RECEIPT_AR_RWO_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_RWO_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("RECEIPT_AR_SI_knockOffAmount", CompanyReportConfigConstants.RECEIPT_SI_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("RECEIPT_AR_SI_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_SI_KNOCK_OFF_AMT_BASE);
            
            
            // Knock Off Opening
            
            arDynamicQueryMap.put("RECEIPT_AR_LP_OPN_knockOffAmount", CompanyReportConfigConstants.RECEIPT_AR_LP_OPN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("RECEIPT_AR_LP_OPN_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_AR_LP_OPN_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("RECEIPT_AR_LDR_OPN_knockOffAmount", CompanyReportConfigConstants.RECEIPT_AR_LDR_OPN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("RECEIPT_AR_LDR_OPN_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_AR_LDR_OPN_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("RECEIPT_AR_RD_OPN_knockOffAmount", CompanyReportConfigConstants.RECEIPT_AR_RD_OPN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("RECEIPT_AR_RD_OPN_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_AR_RD_OPN_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("RECEIPT_AR_RDP_OPN_knockOffAmount", CompanyReportConfigConstants.RECEIPT_AR_RDP_OPN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("RECEIPT_AR_RDP_OPN_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_AR_RDP_OPN_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("RECEIPT_AR_ADV_OPN_knockOffAmount", CompanyReportConfigConstants.RECEIPT_AR_ADV_OPN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("RECEIPT_AR_ADV_OPN_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_AR_ADV_OPN_KNOCK_OFF_AMT_BASE);
            arDynamicQueryMap.put("RECEIPT_AR_RWO_OPN_knockOffAmount", CompanyReportConfigConstants.RECEIPT_AR_RWO_OPN_KNOCK_OFF_AMT);
            arDynamicQueryMap.put("RECEIPT_AR_RWO_OPN_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_AR_RWO_OPN_KNOCK_OFF_AMT_BASE);
//            arDynamicQueryMap.put("RECEIPT_AR_SI_OPN_knockOffAmount", CompanyReportConfigConstants.RECEIPT_AR_SI_OPN_KNOCK_OFF_AMT);
//            arDynamicQueryMap.put("RECEIPT_AR_SI_OPN_knockOffAmountInBase", CompanyReportConfigConstants.RECEIPT_AR_SI_OPN_KNOCK_OFF_AMT_BASE);
            
            
            
            
         }
        return arDynamicQueryMap;
        
    }
    
    private String getDerivedQueryForGL(JSONArray jArr, String module, String submodule, String userSessionId, Map<String, String> fieldParamMap) throws JSONException {
        StringBuilder queryBuilder = new StringBuilder();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = new JSONObject(jArr.getString(i));
            if (!jobj.optString("groupinfo", "").equals("GROUP") && (jobj.has("isconfigureCustomdata") && jobj.getBoolean("isconfigureCustomdata") || jobj.has("isshowinview") && jobj.getBoolean("isshowinview"))) {
                if (jobj.has("iscd") && jobj.getBoolean("iscd")) {
                    String customDataTable = null;
                    if(jobj.getInt("customcolumn") == 1){
                        customDataTable = "jedcd";
                    }else{
                        customDataTable = "jecd";
                    }
                    if (fieldParamMap.containsKey(module + "_" + jobj.getString("title"))) {
                        if (jobj.has("isdd") && jobj.getBoolean("isdd")) {
                            queryBuilder.append("GROUP_CONCAT(distinct fetch_custom_data_dd("+customDataTable+"." + fieldParamMap.get(module + "_" + jobj.getString("title")) + ")) as cd_" + jobj.getString("title").replaceAll("\\W", "_")).append(",");
                            } else {
                            queryBuilder.append("GROUP_CONCAT(distinct if("+customDataTable+"."  + fieldParamMap.get(module + "_" + jobj.getString("title")) + " = '',null," +customDataTable+"." + fieldParamMap.get(module + "_" + jobj.getString("title")) + ")) as cd_" + jobj.getString("title").replaceAll("\\W", "_")).append(",");
                        }
                    } else {
                        queryBuilder.append("null as cd_" + jobj.getString("title").replaceAll("\\W", "_")).append(",");

                    }
                } else {
                    if (jobj.optString("groupinfo", "").equals("DERIVED")) {
                        queryBuilder.append(getGLDynamicQueryMap().get(module + "_" + jobj.getString("header"))).append(",");
                    } else if (jobj.optString("groupinfo", "").equals("MULTIPLE") ) {
                        queryBuilder.append(getGLDynamicQueryMap().get(module + "_" + (StringUtil.isNullOrEmpty(submodule) ? "" : (submodule + "_" )) + jobj.getString("header"))).append(",");

                    } else {
                        queryBuilder.append(getGLDynamicQueryMap().get(jobj.getString("header"))).append(",");
                    }
                }
            }
        }
        String extraAttrQuery = "'" + userSessionId + "'";
        List<String> extraAttrList = new ArrayList<String>();
        extraAttrList.add("REF_ID");
        extraAttrList.add("CASH_TRANSACTION");
        extraAttrList.add("GL_CURRENCY_SYMBOL"); //Adding this params to get transaction currency symbol.
        for (String extraAttr : extraAttrList) {
            if (extraAttr.equals("GL_CURRENCY_SYMBOL")) {
                extraAttrQuery += "," + getGLDynamicQueryMap().get(extraAttr);
            } else {
                extraAttrQuery += "," + getGLDynamicQueryMap().get(module + "_" + extraAttr);
            }
        }

        String query = "SELECT " + CompanyReportConfigConstants.GL_SELECT_NON_CONFIG_FIELDS + "," + extraAttrQuery + "," + queryBuilder.toString();

        query = query.substring(0, query.lastIndexOf(","));

        return query;
    }
    
    
    private String getGenericDerivedQuery(JSONArray jArr, String module, String submodule, boolean isopening, Map<String, String> queryMap) throws JSONException {
        StringBuilder queryBuilder = new StringBuilder();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = new JSONObject(jArr.getString(i));
            if (jobj.optString("groupinfo", "").equals("DERIVED")) {
                queryBuilder.append(queryMap.get(module + "_" + jobj.getString("header"))).append(",");
            } else if (jobj.optString("groupinfo", "").equals("DERIVED_OPN")) {
                if(isopening){
                    queryBuilder.append(queryMap.get(module + "_OPN_" + jobj.getString("header"))).append(",");
                }
                else{
                    queryBuilder.append(queryMap.get(module +"_" + jobj.getString("header"))).append(",");
                }

            } 
            else if (jobj.optString("groupinfo", "").equals("MULTIPLE_OPN")) {
                if(isopening){
                    queryBuilder.append(queryMap.containsKey(module +"_"+submodule+ "_OPN_" + jobj.getString("header")) ? queryMap.get(module +"_"+submodule+ "_OPN_" + jobj.getString("header")) : queryMap.get(module +"_OPN_" + jobj.getString("header"))).append(",");
                }
                else{
                    queryBuilder.append(queryMap.containsKey(module +"_"+submodule+"_"+ jobj.getString("header")) ? queryMap.get(module +"_"+submodule+"_"+ jobj.getString("header")) : queryMap.get(module+"_" +jobj.getString("header"))).append(",");
                }
            }
            
            else {
                queryBuilder.append(queryMap.get(jobj.getString("header"))).append(",");
            }
        }
        String query = "SELECT " + queryBuilder.toString();

        query = query.substring(0, query.lastIndexOf(","));

        return query;
    }
    private String getDerivedQueryForSOA(JSONArray jArr, String module, String submodule, boolean isopening) throws JSONException {
        return getGenericDerivedQuery(jArr, module, submodule, isopening, getSOADynamicQueryMap());
    }
    private String getDerivedQueryForAR(JSONArray jArr, String module, String submodule, boolean isopening) throws JSONException {
        return getGenericDerivedQuery(jArr, module, submodule, isopening, getARDynamicQueryMap());
    }

    public int showCustomFieldsDimensionInDescForGL(String companyid) throws ServiceException, JSONException {
        int showCustomFieldDimension = 2;
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put(Constants.companyid, companyid);
        paramsMap.put(Constants.REPORT_TYPE, Constants.COMPANY_REPORT_CONFIG_GL);
        paramsMap.put("type", "description");
        paramsMap.put("onlyVisible", true);
        JSONObject configObj = getTypeFormatByReportType(paramsMap);
        JSONArray configArray = configObj.getJSONArray("data");
        if (configArray != null && configArray.length() > 0) {
            showCustomFieldDimension = -1;
        }
        for (int i = 0; i < configArray.length(); i++) {
            JSONObject valueObj = configArray.getJSONObject(i);
            if (valueObj.has("isvisible") && valueObj.getBoolean("isvisible") && valueObj.has("header") && valueObj.getString("header").equals("dimensions")) {
                if (showCustomFieldDimension == 1) {
                    showCustomFieldDimension = 2;
                } else {
                    showCustomFieldDimension = 0;
                }
            } else if (valueObj.has("isvisible") && valueObj.getBoolean("isvisible") && valueObj.has("header") && valueObj.getString("header").equals("custom_data")) {
                if (showCustomFieldDimension == 0) {
                    showCustomFieldDimension = 2;
                } else {
                    showCustomFieldDimension = 1;
                }
            }
        }
        return showCustomFieldDimension;
    }

    public JSONObject getTypeFormatByReportType(HashMap<String,Object> paramsMap) throws ServiceException, JSONException {
        JSONArray fileJsonArray = new JSONArray();
        JSONObject fileObj = new JSONObject();
        JSONObject visibleObj = new JSONObject();
        JSONArray configArray = new JSONArray();
        JSONArray finalJsonArray = new JSONArray();
        JSONObject finalJsonObj = new JSONObject();
        JSONObject configobj=new JSONObject();
//        Reader fileReader = null;
        String companyID="";
        String reportType="";
        String type="";
        Boolean onlyVisible=false;
        Boolean isExport=false;
        Boolean fromExpander=false;
        String fileType = (paramsMap != null && paramsMap.containsKey("filetype"))? (String) paramsMap.get("filetype"):" ";
        
            String filePath = null;
            
            if (paramsMap.containsKey(Constants.companyid) && paramsMap.get(Constants.companyid) != null) {
                companyID = (String) paramsMap.get(Constants.companyid);
            }
            if (paramsMap.containsKey(Constants.REPORT_TYPE) && paramsMap.get(Constants.REPORT_TYPE) != null) {
                reportType = (String) paramsMap.get(Constants.REPORT_TYPE);
            }
            if (paramsMap.containsKey("type") && paramsMap.get("type") != null) {
                type = (String) paramsMap.get("type");
            }
            if (paramsMap.containsKey("onlyVisible") && paramsMap.get("onlyVisible") != null) {
                onlyVisible = (Boolean) paramsMap.get("onlyVisible");
            }
            if (paramsMap.containsKey("isExport") && paramsMap.get("isExport") != null) {
                isExport = (Boolean) paramsMap.get("isExport");
            }
            if (paramsMap.containsKey("isFromExpander") && paramsMap.get("isFromExpander") != null) {
                fromExpander = (Boolean) paramsMap.get("isFromExpander");
            }
            if (reportType.equals(Constants.COMPANY_REPORT_CONFIG_GL)) {
            if (fileType.equals("detailedPDF")) {
                filePath = "/report/template/ReportConfig_GL_PDF_Template.xml";
            } else {
                filePath = "/report/template/ReportConfig_GL_Template.xml";
            }
            } else if (reportType.equals(Constants.COMPANY_REPORT_CONFIG_SOA)) {
                filePath = "/report/template/ReportConfig_SOA_Template.xml";
            } else if (reportType.equals(Constants.COMPANY_REPORT_CONFIG_AR)) {
                filePath = "/report/template/ReportConfig_AR_Template.xml";
            }
            try (InputStream is = getClass().getResourceAsStream(filePath);
                Reader fileReader = new InputStreamReader(is);
                BufferedReader bufReader = new BufferedReader(fileReader);) {
            StringBuilder sb = new StringBuilder();
            String line = bufReader.readLine();
            while (line != null) {
                sb.append(line).append("\n");
                line = bufReader.readLine();
            }
            String xml2String = sb.toString();
            JSONObject jobj = XML.toJSONObject(xml2String);
            fileJsonArray = jobj.getJSONArray(type);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CompanyReportConfigurationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CompanyReportConfigurationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (reportType.equals(Constants.COMPANY_REPORT_CONFIG_GL)) {
            /*
             Get Custom/Dimension field 
             */
            HashMap<String, Object> fieldParamRequestMap = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList();
            ArrayList filter_values = new ArrayList();
            filter_names.add("companyid");
            filter_names.add("INmoduleid");
            filter_values.add(companyID);
            filter_values.add(Constants.Acc_Invoice_ModuleId + "," + Constants.Acc_Vendor_Invoice_ModuleId + "," + Constants.Acc_Debit_Note_ModuleId + "," + Constants.Acc_Credit_Note_ModuleId + "," + Constants.Acc_Receive_Payment_ModuleId + "," + Constants.Acc_Make_Payment_ModuleId + "," + Constants.Acc_GENERAL_LEDGER_ModuleId + "," + Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId + "," + Constants.Acc_FixedAssets_GoodsReceipt_ModuleId + "," + Constants.Acc_FixedAssets_DisposalInvoice_ModuleId);
            fieldParamRequestMap.put("filter_names", filter_names);
            fieldParamRequestMap.put("filter_values", filter_values);
            KwlReturnObject returnObj = accAccountDAOobj.getFieldParams(fieldParamRequestMap);
            Map<String, FieldParams> fpMap = new HashMap<String, FieldParams>();
            Map<String, String> moduleMap = new HashMap<String, String>();

            for (int i = 0; i < returnObj.getEntityList().size(); i++) {
                FieldParams fp = (FieldParams) returnObj.getEntityList().get(i);
                if (moduleMap.containsKey(fp.getFieldlabel())) {
                    moduleMap.put(fp.getFieldlabel(), moduleMap.get(fp.getFieldlabel()) + ", " + StringUtil.getModuleName(String.valueOf(fp.getModuleid())));
                } else {
                    fpMap.put(fp.getFieldlabel(), fp);
                    moduleMap.put(fp.getFieldlabel(), StringUtil.getModuleName(String.valueOf(fp.getModuleid())));
                }
            }

            Map<String, Object> dataMap = new HashMap<>();
            Map<String, Object> dataMap1 = new HashMap<>();
            CompanyReportConfiguration companyReportConfig = null;
            KwlReturnObject companyConfigObj = companyReportConfigurationdao.getCompanyReportConfiguration(companyID, reportType);
            if (companyConfigObj != null && companyConfigObj.getEntityList() != null && !companyConfigObj.getEntityList().isEmpty()) {
                companyReportConfig = (CompanyReportConfiguration) companyConfigObj.getEntityList().get(0);
                if (!StringUtil.isNullOrEmpty(companyReportConfig.getFormat())) {
                    configArray = new JSONArray(companyReportConfig.getFormat());
                    for (int i = 0; i < configArray.length(); i++) {
                        JSONObject tempObj = configArray.getJSONObject(i);
                        configobj = new JSONObject();
                        configobj.put("id", companyReportConfig.getID());
                        configobj.put("header", tempObj.optString("header").replaceAll("\\W", "_"));
                        if (tempObj.has("isconfigureCustomdata")) {
                            dataMap.put(configobj.optString("header"), tempObj);
                        }
                        if (tempObj.has("isshowinview")) {
                            dataMap1.put(configobj.optString("header"), tempObj);
                        }
                    }
                }
                finalJsonObj.put("isconfigsaved", true);
            }

            for (int i = 0; i < fileJsonArray.length(); i++) {
                fileObj = fileJsonArray.getJSONObject(i);
                configobj = new JSONObject();
                configobj.put("id", companyReportConfig == null ? fileObj.optString("id") : companyReportConfig.getID());
                configobj.put("header", fileObj.optString("header").replaceAll("\\W", "_"));
                configobj.put("title", fileObj.optString("title"));
                configobj.put("iscd", fileObj.optBoolean("iscd"));
                configobj.put("customcolumn", fileObj.optInt("customcolumn", 1));
                configobj.put("isdd", fileObj.optBoolean("isdd"));
                configobj.put("fieldType", fileObj.optString("fieldType"));
                configobj.put("groupinfo", fileObj.optString("groupinfo"));
                configobj.put("isManadatoryField", fileObj.optBoolean("isManadatoryField", false));
                configobj.put("forexpander", fileObj.optBoolean("forexpander", false));
                configobj.put("align", fileObj.optString("align", "left"));
                configobj.put("isconfigureCustomdata", fileObj.optBoolean("isconfigureCustomdata", false));
                configobj.put("isshowinview", fileObj.optBoolean("isshowinview", false));
                if (!fileObj.optBoolean("isManadatoryField", false)) { // If isMandatoryField then don't update view and export check.
                    if (!dataMap.isEmpty() && dataMap.containsKey(fileObj.optString("header"))) {
                        JSONObject temp = (JSONObject) dataMap.get(fileObj.optString("header"));
                        if (fileType.equals("detailedPDF")) {
                            configobj.put("isconfigureCustomdata", fileObj.optBoolean("isconfigureCustomdata", false));
                        } else {
                            configobj.put("isconfigureCustomdata", temp.optBoolean("isconfigureCustomdata", false));
                        }
                    }
                    if (!dataMap1.isEmpty() && dataMap1.containsKey(fileObj.optString("header"))) {
                        JSONObject temp = (JSONObject) dataMap1.get(fileObj.optString("header"));
                        configobj.put("isshowinview", temp.optBoolean("isshowinview", false));
                    }
                }
                finalJsonArray.put(configobj);
            }

            /*
             Adding Custom or Dimension Field data
             */
            JSONObject fieldObj = null;
            for (String key : fpMap.keySet()) {
                fieldObj = new JSONObject();
                FieldParams fp = fpMap.get(key);
                fieldObj.put("id", companyReportConfig == null ? "" : companyReportConfig.getID());
                fieldObj.put("header", "cd_" + fp.getFieldlabel().replaceAll("\\W", "_"));
                fieldObj.put("title", fp.getFieldlabel());
                fieldObj.put("iscd", true);
                fieldObj.put("customcolumn", fp.getCustomcolumn());
                fieldObj.put("isdd", fp.getFieldtype() == 7 || fp.getFieldtype() == 4 ? true : false);
                fieldObj.put("module", moduleMap.get(key));
                fieldObj.put("fieldType", (fp.getCustomcolumn() == 1 ? "Line Level- " : "Global- ") + "Custom Field/Dimension");
                fieldObj.put("forexpander", true);
                fieldObj.put("isconfigureCustomdata", false);
                fieldObj.put("isshowinview", false);
                for (int i = 0; i < configArray.length(); i++) {
                    if (configArray.getJSONObject(i).has("title") && fp.getFieldlabel().equals(configArray.getJSONObject(i).optString("title"))) {
                        fieldObj.put("isconfigureCustomdata", configArray.getJSONObject(i).has("isconfigureCustomdata") ? configArray.getJSONObject(i).get("isconfigureCustomdata") : false);
                        fieldObj.put("isshowinview", configArray.getJSONObject(i).has("isshowinview") ? configArray.getJSONObject(i).get("isshowinview") : false);
                    }
                }
                finalJsonArray.put(fieldObj);
            }

            if (onlyVisible) {
                JSONArray visibleArray = new JSONArray();
                for (int i = 0; i < finalJsonArray.length(); i++) {
                    JSONObject tempObj = finalJsonArray.getJSONObject(i);
                    if (fromExpander) {
                        if (tempObj.has("isshowinview") && tempObj.getBoolean("isshowinview")) {
                            visibleArray.put(tempObj);
                        }
                    } else {
                        if (tempObj.has("isconfigureCustomdata") && tempObj.getBoolean("isconfigureCustomdata")) {
                            visibleArray.put(tempObj);
                        }
                    }
                }
                visibleObj.put("data", visibleArray);
                return visibleObj;
            }
            finalJsonObj.put("data", finalJsonArray);
        } else {
            finalJsonObj.put("data", fileJsonArray);
        }

        return finalJsonObj;
    }
    
    public JSONObject pouplateSelectStatementForGL(JSONObject jobj, String userSessionId, String companyid) throws JSONException, ServiceException {
        Map params = new HashMap();
        params.put("filetype", jobj.optString("filetype"));
        boolean fromExpander=jobj.optBoolean("isFromExpander");
        JSONObject attrobject = getExportConfigData(companyid, Constants.COMPANY_REPORT_CONFIG_GL, true,fromExpander, params);
        JSONArray formatArray = attrobject.getJSONArray(Constants.RES_data);
        HashMap<String, Object> fieldParamRequestMap = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList();
        ArrayList filter_values = new ArrayList();
        filter_names.add("companyid");
        filter_values.add(companyid);
        fieldParamRequestMap.put("filter_names", filter_names);
        fieldParamRequestMap.put("filter_values", filter_values);
        KwlReturnObject returnObj = accAccountDAOobj.getFieldParams(fieldParamRequestMap);
        Map<String, String> fieldParamMap = new HashMap<String, String>();
        if (returnObj != null && returnObj.getEntityList() != null && !returnObj.getEntityList().isEmpty()) {
            for (int i = 0; i < returnObj.getEntityList().size(); i++) {
                FieldParams fp = (FieldParams) returnObj.getEntityList().get(i);
                if (fp.getModuleid() == Constants.Acc_Invoice_ModuleId && !StringUtil.isNullOrEmpty(fp.getFieldlabel()) && fp.getColnum() != 0) {
                    fieldParamMap.put("INV_" + fp.getFieldlabel(), "col" + fp.getColnum());
                } else if (fp.getModuleid() == Constants.Acc_GENERAL_LEDGER_ModuleId && !StringUtil.isNullOrEmpty(fp.getFieldlabel()) && fp.getColnum() != 0) {
                    fieldParamMap.put("OTHERS_" + fp.getFieldlabel(), "col" + fp.getColnum());
                } else if (fp.getModuleid() == Constants.Acc_Credit_Note_ModuleId && !StringUtil.isNullOrEmpty(fp.getFieldlabel()) && fp.getColnum() != 0) {
                    fieldParamMap.put("CN_" + fp.getFieldlabel(), "col" + fp.getColnum());
                } else if (fp.getModuleid() == Constants.Acc_Vendor_Invoice_ModuleId && !StringUtil.isNullOrEmpty(fp.getFieldlabel()) && fp.getColnum() != 0) {
                    fieldParamMap.put("GR_" + fp.getFieldlabel(), "col" + fp.getColnum());
                } else if (fp.getModuleid() == Constants.Acc_Debit_Note_ModuleId && !StringUtil.isNullOrEmpty(fp.getFieldlabel()) && fp.getColnum() != 0) {
                    fieldParamMap.put("DN_" + fp.getFieldlabel(), "col" + fp.getColnum());
                } else if (fp.getModuleid() == Constants.Acc_Receive_Payment_ModuleId && !StringUtil.isNullOrEmpty(fp.getFieldlabel()) && fp.getColnum() != 0) {
                    fieldParamMap.put("RECEIPT_" + fp.getFieldlabel(), "col" + fp.getColnum());
                } else if (fp.getModuleid() == Constants.Acc_Make_Payment_ModuleId && !StringUtil.isNullOrEmpty(fp.getFieldlabel()) && fp.getColnum() != 0) {
                    fieldParamMap.put("PAYMENT_" + fp.getFieldlabel(), "col" + fp.getColnum());
                }
            }
        }
        jobj.put("invoicedetailsselect", getDerivedQueryForGL(formatArray, "INV", "DETAILS", userSessionId, fieldParamMap));
        jobj.put("invoicecapselect", getDerivedQueryForGL(formatArray, "INV", "CAP", userSessionId, fieldParamMap));
        jobj.put("invoiceroundingselect", getDerivedQueryForGL(formatArray, "INV", "ROUNDING", userSessionId, fieldParamMap));
        jobj.put("invoicetermselect", getDerivedQueryForGL(formatArray, "INV", "TERMS", userSessionId, fieldParamMap));
        jobj.put("grdetailsselect", getDerivedQueryForGL(formatArray, "GR", "DETAILS", userSessionId, fieldParamMap));
        jobj.put("grexpdetailsselect", getDerivedQueryForGL(formatArray, "GR", "EXP_DETAILS", userSessionId, fieldParamMap));
        jobj.put("grcapselect", getDerivedQueryForGL(formatArray, "GR", "CAP", userSessionId, fieldParamMap));
        jobj.put("grroundingselect", getDerivedQueryForGL(formatArray, "GR", "ROUNDING", userSessionId, fieldParamMap));
        jobj.put("grtermselect", getDerivedQueryForGL(formatArray, "GR", "TERMS", userSessionId, fieldParamMap));
        jobj.put("cnselect", getDerivedQueryForGL(formatArray, "CN", null, userSessionId, fieldParamMap));
        jobj.put("dnselect", getDerivedQueryForGL(formatArray, "DN", null, userSessionId, fieldParamMap));
        jobj.put("receiptselect", getDerivedQueryForGL(formatArray, "RECEIPT", null, userSessionId, fieldParamMap));
        jobj.put("receiptwoselect", getDerivedQueryForGL(formatArray, "RECEIPT_WO", null, userSessionId, fieldParamMap));
        jobj.put("paymentselect", getDerivedQueryForGL(formatArray, "PAYMENT", null, userSessionId, fieldParamMap));
        jobj.put("othersselect", getDerivedQueryForGL(formatArray, "OTHERS", null, userSessionId, fieldParamMap));
        jobj.put("jeinvrevalselect", getDerivedQueryForGL(formatArray, "JE_INV_REVAL", null, userSessionId, fieldParamMap));
        jobj.put("jegrrevalselect", getDerivedQueryForGL(formatArray, "JE_GR_REVAL", null, userSessionId, fieldParamMap));
        jobj.put("saselect", getDerivedQueryForGL(formatArray, "SA", null, userSessionId, fieldParamMap));
        jobj.put("srselect", getDerivedQueryForGL(formatArray, "SR", null, userSessionId, fieldParamMap));
        jobj.put("prselect", getDerivedQueryForGL(formatArray, "PR", null, userSessionId, fieldParamMap));
        jobj.put("doselect", getDerivedQueryForGL(formatArray, "DO", null, userSessionId, fieldParamMap));
        jobj.put("groselect", getDerivedQueryForGL(formatArray, "GRO", null, userSessionId, fieldParamMap));
        return jobj;
    }
    public JSONObject pouplateSelectStatementForSOA(JSONObject jobj, String companyid) throws JSONException, ServiceException {
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put(Constants.companyid, companyid);
        paramsMap.put(Constants.REPORT_TYPE, Constants.COMPANY_REPORT_CONFIG_AR);
        paramsMap.put("type", Constants.globalFields);
        paramsMap.put("onlyVisible", false);
        JSONObject attrobject = getTypeFormatByReportType(paramsMap);
        JSONArray formatArray = attrobject.getJSONArray(Constants.RES_data);
        jobj.put("invselect", getDerivedQueryForSOA(formatArray, "INV_AR",null, false));
        jobj.put("invcndselect", getDerivedQueryForSOA(formatArray, "INV_AR","CND", false));
        jobj.put("invrdselect", getDerivedQueryForSOA(formatArray, "INV_AR","RD", false));
        jobj.put("invldrselect", getDerivedQueryForSOA(formatArray, "INV_AR","LDR", false));
        jobj.put("invwoselect", getDerivedQueryForSOA(formatArray, "INV_AR","WRITEOFF", false));
        jobj.put("invmainselect", getDerivedQueryForSOA(formatArray, "INV_MAIN_AR",null, false));

        jobj.put("invopnselect", getDerivedQueryForSOA(formatArray, "INV_AR",null, true));
        jobj.put("invopnCNDselect", getDerivedQueryForSOA(formatArray, "INV_AR","CND", true));
        jobj.put("invopnRDselect", getDerivedQueryForSOA(formatArray, "INV_AR","RD", true));
        jobj.put("invopnLDRselect", getDerivedQueryForSOA(formatArray, "INV_AR","LDR", true));
        jobj.put("invopnWOselect", getDerivedQueryForSOA(formatArray, "INV_AR","WRITEOFF", true));
        jobj.put("invopnmainselect", getDerivedQueryForSOA(formatArray, "INV_MAIN_AR",null, true));
        
        
        jobj.put("dnselect", getDerivedQueryForSOA(formatArray, "DN_AR", null, false));
        jobj.put("dnmainselect", getDerivedQueryForSOA(formatArray, "DN_MAIN_AR",null, false));
        jobj.put("dnLDRselect", getDerivedQueryForSOA(formatArray, "DN_AR", "LDR", false));
        jobj.put("dnDNPselect", getDerivedQueryForSOA(formatArray, "DN_AR", "DNP", false));
        jobj.put("dnDISselect", getDerivedQueryForSOA(formatArray, "DN_AR", "DIS", false));
     
        jobj.put("dnopnmainselect", getDerivedQueryForSOA(formatArray, "DN_MAIN_AR",null, true));
        jobj.put("dnopnselect", getDerivedQueryForSOA(formatArray, "DN_AR", null, true));
        jobj.put("dnopnLDRselect", getDerivedQueryForSOA(formatArray, "DN_AR", "LDR", true));
        jobj.put("dnopnDNPselect", getDerivedQueryForSOA(formatArray, "DN_AR", "DNP", true));
        jobj.put("dnopnDISselect", getDerivedQueryForSOA(formatArray, "DN_AR", "DIS", true));
//        
//        
        jobj.put("cnselect", getDerivedQueryForSOA(formatArray, "CN_AR", null, false));
        jobj.put("cnmainselect", getDerivedQueryForSOA(formatArray, "CN_MAIN_AR",null, false));
        jobj.put("cnLDRselect", getDerivedQueryForSOA(formatArray, "CN_AR", "LDR", false));
        jobj.put("cnCNPselect", getDerivedQueryForSOA(formatArray, "CN_AR", "CNP", false));
        jobj.put("cnDISselect", getDerivedQueryForSOA(formatArray, "CN_AR", "DIS", false));
        jobj.put("cnFRXselect", getDerivedQueryForSOA(formatArray, "CN_FRX", null, false));
//        jobj.put("cnFRXselect", getDerivedQueryForSOA(formatArray, "CN_FRX", null, false));
        
        jobj.put("cnopnselect", getDerivedQueryForSOA(formatArray, "CN_AR", null, true));
        jobj.put("cnopnmainselect", getDerivedQueryForSOA(formatArray, "CN_MAIN_AR",null, true));
        jobj.put("cnopnLDRselect", getDerivedQueryForSOA(formatArray, "CN_AR", "LDR", true));
        jobj.put("cnopnCNPselect", getDerivedQueryForSOA(formatArray, "CN_AR", "CNP", true));
        jobj.put("cnopnDISselect", getDerivedQueryForSOA(formatArray, "CN_AR", "DIS", true));
        
        

        
        jobj.put("receiptselect", getDerivedQueryForSOA(formatArray, "RECEIPT_AR", null, false));
        jobj.put("receiptmainselect", getDerivedQueryForSOA(formatArray, "RECEIPT_MAIN_AR", null, false));
        jobj.put("receiptLPselect", getDerivedQueryForSOA(formatArray, "RECEIPT_AR", "LP", false));
        jobj.put("receiptLDRselect", getDerivedQueryForSOA(formatArray, "RECEIPT_LDR_AR", null, false));
        jobj.put("receiptRDselect", getDerivedQueryForSOA(formatArray, "RECEIPT_AR", "RD", false));
        jobj.put("receiptRDPselect", getDerivedQueryForSOA(formatArray, "RECEIPT_AR", "RDP", false));
        jobj.put("receiptADVselect", getDerivedQueryForSOA(formatArray, "RECEIPT_AR", "ADV", false));
        jobj.put("receiptRWOselect", getDerivedQueryForSOA(formatArray, "RECEIPT_AR", "RWO", false));
        jobj.put("receiptSIselect", getDerivedQueryForSOA(formatArray, "RECEIPT_AR", "SI", false));
        jobj.put("receiptAllSelectStatement", getDerivedQueryForSOA(formatArray, "RECEIPT_ALL", null, false));

        jobj.put("receiptopnmainselect", getDerivedQueryForSOA(formatArray, "RECEIPT_MAIN_AR", null, true));
        jobj.put("receiptopnselect", getDerivedQueryForSOA(formatArray, "RECEIPT_AR", null, true));
        jobj.put("receiptopnLPselect", getDerivedQueryForSOA(formatArray, "RECEIPT_AR", "LP", true));
        jobj.put("receiptopnLDRselect", getDerivedQueryForSOA(formatArray, "RECEIPT_AR", "LDR", true));
//        jobj.put("receiptopnLDRselect", getDerivedQueryForSOA(formatArray, "RECEIPT_LDR", null, true));
        jobj.put("receiptopnRDselect", getDerivedQueryForSOA(formatArray, "RECEIPT_AR", "RD", true));
        jobj.put("receiptopnRDPselect", getDerivedQueryForSOA(formatArray, "RECEIPT_AR", "RDP", true));
        jobj.put("receiptopnADVselect", getDerivedQueryForSOA(formatArray, "RECEIPT_AR", "ADV", true));
        jobj.put("receiptopnRWOselect", getDerivedQueryForSOA(formatArray, "RECEIPT_AR", "RWO", true));
        jobj.put("receiptdishonouredselect", getDerivedQueryForSOA(formatArray, "RECEIPT_DISHONOURED", null, false));           //ERM-744

        
        jobj.put("paymentselect", getDerivedQueryForSOA(formatArray, "PAYMENT_AR", null, false));   
        jobj.put("paymentmainselect", getDerivedQueryForSOA(formatArray, "PAYMENT_MAIN_AR", null, false));   
        jobj.put("paymentCNPselect", getDerivedQueryForSOA(formatArray, "PAYMENT_AR", "CNP", false));   
        jobj.put("paymentLDPselect", getDerivedQueryForSOA(formatArray, "PAYMENT_AR", "LDP", false));   
        jobj.put("paymentLDAPselect", getDerivedQueryForSOA(formatArray, "PAYMENT_AR", "LDAP", false));   
        jobj.put("paymentLDPCNselect", getDerivedQueryForSOA(formatArray, "PAYMENT_AR", "LDPCN", false));   
        jobj.put("paymentPOselect", getDerivedQueryForSOA(formatArray, "PAYMENT_AR", "PO", false));  
        jobj.put("paymentSRselect", getDerivedQueryForSOA(formatArray, "PAYMENT_AR", "SR", false));   
        jobj.put("paymentdishonouredselect", getDerivedQueryForSOA(formatArray, "PAYMENT_DISHONOURED", null, false));           //ERM-744        
        return jobj;
    }
    
    public JSONObject pouplateSelectStatementForAR(JSONObject jobj, String companyid) throws JSONException, ServiceException {
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put(Constants.companyid, companyid);
        paramsMap.put(Constants.REPORT_TYPE, Constants.COMPANY_REPORT_CONFIG_AR);
        paramsMap.put("type", Constants.globalFields);
        paramsMap.put("onlyVisible", false);
        JSONObject attrobject = getTypeFormatByReportType(paramsMap);
        JSONArray formatArray = attrobject.getJSONArray(Constants.RES_data);
        jobj.put("invselect", getDerivedQueryForAR(formatArray, "INV_AR",null, false));
        jobj.put("invcndselect", getDerivedQueryForAR(formatArray, "INV_AR","CND", false));
        jobj.put("invrdselect", getDerivedQueryForAR(formatArray, "INV_AR","RD", false));
        jobj.put("invldrselect", getDerivedQueryForAR(formatArray, "INV_AR","LDR", false));
        jobj.put("invwoselect", getDerivedQueryForAR(formatArray, "INV_AR","WRITEOFF", false));
        jobj.put("invmainselect", getDerivedQueryForAR(formatArray, "INV_MAIN_AR",null, false));

        jobj.put("invopnselect", getDerivedQueryForAR(formatArray, "INV_AR",null, true));
        jobj.put("invopnCNDselect", getDerivedQueryForAR(formatArray, "INV_AR","CND", true));
        jobj.put("invopnRDselect", getDerivedQueryForAR(formatArray, "INV_AR","RD", true));
        jobj.put("invopnLDRselect", getDerivedQueryForAR(formatArray, "INV_AR","LDR", true));
        jobj.put("invopnWOselect", getDerivedQueryForAR(formatArray, "INV_AR","WRITEOFF", true));
        jobj.put("invopnmainselect", getDerivedQueryForAR(formatArray, "INV_MAIN_AR",null, true));
        
        
        jobj.put("dnselect", getDerivedQueryForAR(formatArray, "DN_AR", null, false));
        jobj.put("dnmainselect", getDerivedQueryForAR(formatArray, "DN_MAIN_AR",null, false));
        jobj.put("dnLDRselect", getDerivedQueryForAR(formatArray, "DN_AR", "LDR", false));
        jobj.put("dnDNPselect", getDerivedQueryForAR(formatArray, "DN_AR", "DNP", false));
        jobj.put("dnDISselect", getDerivedQueryForAR(formatArray, "DN_AR", "DIS", false));
     
        jobj.put("dnopnmainselect", getDerivedQueryForAR(formatArray, "DN_MAIN_AR",null, true));
        jobj.put("dnopnselect", getDerivedQueryForAR(formatArray, "DN_AR", null, true));
        jobj.put("dnopnLDRselect", getDerivedQueryForAR(formatArray, "DN_AR", "LDR", true));
        jobj.put("dnopnDNPselect", getDerivedQueryForAR(formatArray, "DN_AR", "DNP", true));
        jobj.put("dnopnDISselect", getDerivedQueryForAR(formatArray, "DN_AR", "DIS", true));
//        
//        
        jobj.put("cnselect", getDerivedQueryForAR(formatArray, "CN_AR", null, false));
        jobj.put("cnmainselect", getDerivedQueryForAR(formatArray, "CN_MAIN_AR",null, false));
        jobj.put("cnLDRselect", getDerivedQueryForAR(formatArray, "CN_AR", "LDR", false));
        jobj.put("cnCNPselect", getDerivedQueryForAR(formatArray, "CN_AR", "CNP", false));
        jobj.put("cnDISselect", getDerivedQueryForAR(formatArray, "CN_AR", "DIS", false));
        
//        jobj.put("cnFRXselect", getDerivedQueryForAR(formatArray, "CN_FRX", null, false));
        
        jobj.put("cnopnselect", getDerivedQueryForAR(formatArray, "CN_AR", null, true));
        jobj.put("cnopnmainselect", getDerivedQueryForAR(formatArray, "CN_MAIN_AR",null, true));
        jobj.put("cnopnLDRselect", getDerivedQueryForAR(formatArray, "CN_AR", "LDR", true));
        jobj.put("cnopnCNPselect", getDerivedQueryForAR(formatArray, "CN_AR", "CNP", true));
        jobj.put("cnopnDISselect", getDerivedQueryForAR(formatArray, "CN_AR", "DIS", true));
        
        

        
        jobj.put("receiptselect", getDerivedQueryForAR(formatArray, "RECEIPT_AR", null, false));
        jobj.put("receiptmainselect", getDerivedQueryForAR(formatArray, "RECEIPT_MAIN_AR", null, false));
        jobj.put("receiptLPselect", getDerivedQueryForAR(formatArray, "RECEIPT_AR", "LP", false));
        jobj.put("receiptLDRselect", getDerivedQueryForAR(formatArray, "RECEIPT_LDR_AR", null, false));
        jobj.put("receiptRDselect", getDerivedQueryForAR(formatArray, "RECEIPT_AR", "RD", false));
        jobj.put("receiptRDPselect", getDerivedQueryForAR(formatArray, "RECEIPT_AR", "RDP", false));
        jobj.put("receiptADVselect", getDerivedQueryForAR(formatArray, "RECEIPT_AR", "ADV", false));
        jobj.put("receiptRWOselect", getDerivedQueryForAR(formatArray, "RECEIPT_AR", "RWO", false));
        jobj.put("receiptSIselect", getDerivedQueryForAR(formatArray, "RECEIPT_AR", "SI", false));


        jobj.put("receiptopnmainselect", getDerivedQueryForAR(formatArray, "RECEIPT_MAIN_AR", null, true));
        jobj.put("receiptopnselect", getDerivedQueryForAR(formatArray, "RECEIPT_AR", null, true));
        jobj.put("receiptopnLPselect", getDerivedQueryForAR(formatArray, "RECEIPT_AR", "LP", true));
        jobj.put("receiptopnLDRselect", getDerivedQueryForAR(formatArray, "RECEIPT_AR", "LDR", true));
//        jobj.put("receiptopnLDRselect", getDerivedQueryForAR(formatArray, "RECEIPT_LDR", null, true));
        jobj.put("receiptopnRDselect", getDerivedQueryForAR(formatArray, "RECEIPT_AR", "RD", true));
        jobj.put("receiptopnRDPselect", getDerivedQueryForAR(formatArray, "RECEIPT_AR", "RDP", true));
        jobj.put("receiptopnADVselect", getDerivedQueryForAR(formatArray, "RECEIPT_AR", "ADV", true));
        jobj.put("receiptopnRWOselect", getDerivedQueryForAR(formatArray, "RECEIPT_AR", "RWO", true));
        

        
        jobj.put("paymentselect", getDerivedQueryForAR(formatArray, "PAYMENT_AR", null, false));   
        jobj.put("paymentmainselect", getDerivedQueryForAR(formatArray, "PAYMENT_MAIN_AR", null, false));   
        jobj.put("paymentCNPselect", getDerivedQueryForAR(formatArray, "PAYMENT_AR", "CNP", false));   
        jobj.put("paymentLDPselect", getDerivedQueryForAR(formatArray, "PAYMENT_AR", "LDP", false));   
        jobj.put("paymentLDAPselect", getDerivedQueryForAR(formatArray, "PAYMENT_AR", "LDAP", false));   
        jobj.put("paymentLDPCNselect", getDerivedQueryForAR(formatArray, "PAYMENT_AR", "LDPCN", false));   
        jobj.put("paymentPOselect", getDerivedQueryForAR(formatArray, "PAYMENT_AR", "PO", false));  
        jobj.put("paymentSRselect", getDerivedQueryForAR(formatArray, "PAYMENT_AR", "SR", false));   
        
        return jobj;
    }
    
    public Map<String, String> getPropertiesForExport(String companyid, String reportType, String groupType, boolean configuredCustomData,boolean isExport,boolean isFromExpander) throws JSONException, ServiceException {
        Map<String, String> attrMap = new HashMap<String, String>();
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put(Constants.companyid, companyid);
        paramsMap.put(Constants.REPORT_TYPE, reportType);
        paramsMap.put("type", groupType);
        paramsMap.put("onlyVisible", configuredCustomData);
        paramsMap.put("isExport", isExport);
        paramsMap.put("isFromExpander", isFromExpander);
        JSONObject formatobj = getTypeFormatByReportType(paramsMap);
        JSONArray formatArray = formatobj.getJSONArray("data");
        StringBuilder align = new StringBuilder();
        StringBuilder header = new StringBuilder();
        StringBuilder title = new StringBuilder();
        JSONObject tempObj = null;
        if (formatArray.length() > 0) {
            for (int i = 0; i < formatArray.length(); i++) {
                tempObj = formatArray.getJSONObject(i);

                if (tempObj.optString("forexpander", "").equalsIgnoreCase("true") || isExport) {
                    if (tempObj.has("header")) {
                        header.append(tempObj.getString("header").replaceAll("\\W", "_")).append(",");
                    }
                    if (tempObj.has("align")) {
                        align.append(tempObj.getString("align")).append(",");
                    } else {
                        align.append("none").append(",");
                    }
                    if (tempObj.has("title")) {
                        title.append(tempObj.getString("title")).append(",");
                    }
                }

            }

            align.deleteCharAt(align.lastIndexOf(","));
            header.deleteCharAt(header.lastIndexOf(","));
            title.deleteCharAt(title.lastIndexOf(","));
        }
        String titleStr = title.toString();
        if (!StringUtil.isNullOrEmpty("title") && titleStr.contains("$$basecurrency$$")) {
            KwlReturnObject obj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) obj.getEntityList().get(0);
            titleStr = titleStr.replace("$$basecurrency$$", (isExport ||isFromExpander)?company.getCurrency().getCurrencyCode():company.getCurrency().getSymbol());
        }

        attrMap.put("header", header.toString());
        attrMap.put("align", align.toString());
        attrMap.put("title", titleStr);
        return attrMap;
    }

    @Override
    public void saveGLConfiguration(JSONObject paramObj, String userSessionId, String companyId) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        String dataArray = paramObj.optString("dataArray");
        String id = paramObj.optString("id");
        JSONArray formatArray = new JSONArray(dataArray);
        Map<String, Object> saveDataMap = new HashMap<String, Object>();
        saveDataMap.put(Constants.companyKey, companyId);
        saveDataMap.put("type", Constants.COMPANY_REPORT_CONFIG_GL);
        saveDataMap.put("format", formatArray);
        saveDataMap.put("id", id);
        companyReportConfigurationdao.saveCompanyReportConfiguration(saveDataMap);
    }

    @Override
    public JSONObject getExportConfigData(String companyid, String reportType, boolean onlyVisible,boolean fromExpander, Map params) throws ServiceException, JSONException {
        JSONObject configData = new JSONObject();
        JSONArray configArray = new JSONArray();
        JSONArray customtypeArray = new JSONArray();
        KwlReturnObject obj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company company = (Company) obj.getEntityList().get(0);
        String companyCurrCode= company.getCurrency().getCurrencyCode();
//        JSONArray fieldArray = getTypeFormatByReportType(companyid, reportType, Constants.globalFields, onlyVisible);
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put(Constants.companyid, companyid);
        paramsMap.put(Constants.REPORT_TYPE, reportType);
        paramsMap.put("type", Constants.globalFields);
        paramsMap.put("onlyVisible", onlyVisible);
        paramsMap.put("isFromExpander", fromExpander);
        paramsMap.put("filetype",(params != null && params.containsKey("filetype")) ? params.get("filetype") : "");
        JSONObject fieldobJSONObject = getTypeFormatByReportType(paramsMap);
        JSONArray fieldArray = fieldobJSONObject.getJSONArray("data");
        String fieldArrJson = fieldArray.toString();
        fieldArrJson = fieldArrJson.replace("$$basecurrency$$", companyCurrCode);
        fieldArray = new JSONArray(fieldArrJson);
        configData.put("data", fieldArray);
        configData.put("isconfigsaved", fieldobJSONObject.optBoolean("isconfigsaved"));
        configData.put("id", fieldobJSONObject.optString("id"));
        return configData;
    }
}