/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.creditnote;

import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.spring.accounting.bankreconciliation.accBankReconciliationDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.discount.accDiscountDAO;
import com.krawler.spring.accounting.invoice.accInvoiceCMN;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class accCreditNoteServiceImplCMN implements accCreditNoteServiceCMN,MessageSourceAware {

    private accCreditNoteDAO accCreditNoteDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accTaxDAO accTaxObj;
    private fieldDataManager fieldDataManagercntrl;
    private String successView;
    private accAccountDAO accAccountDAOobj;
    public ImportHandler importHandler;
    public accCustomerDAO accCustomerDAOObj;
    private authHandlerDAO authHandlerDAOObj;
    
    private accJournalEntryDAO accJournalEntryobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private auditTrailDAO auditTrailObj;
    private MessageSource messageSource;
    public accBankReconciliationDAO accBankReconciliationDAOObj;
    private accCreditNoteService accCreditNoteService;
    private accDebitNoteDAO accDebitNoteobj;
    private accInvoiceCMN accInvoiceCommon;
    private accInvoiceDAO accInvoiceDAOObj;
    private accPaymentDAO accPaymentDAOobj;
    private exportMPXDAOImpl exportDaoObj;
    private HibernateTransactionManager txnManager;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private accDiscountDAO accDiscountobj;
    
    public class SortCreditNoteDetail implements Comparator<CreditNoteDetail> {

        @Override
        public int compare(CreditNoteDetail CND1, CreditNoteDetail CND2) {
            if (CND1.getSrno() > CND2.getSrno()) {
                return 1;
            } else {
                return -1;
            }
        }
    }
    
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    public void setaccDiscountDAO(accDiscountDAO accDiscountobj) {
        this.accDiscountobj = accDiscountobj;
    }
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    
    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }
    public void setAccInvoiceDAO(accInvoiceDAO accInvoiceDAOObj) {
        this.accInvoiceDAOObj = accInvoiceDAOObj;
    }
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    public void setAccInvoiceCommon(accInvoiceCMN accInvoiceCommon) {
        this.accInvoiceCommon = accInvoiceCommon;
    }
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setAccBankReconciliationDAOObj(accBankReconciliationDAO accBankReconciliationDAOObj) {
        this.accBankReconciliationDAOObj = accBankReconciliationDAOObj;
    }

    public void setaccCreditNoteService(accCreditNoteService accCreditNoteService) {
        this.accCreditNoteService = accCreditNoteService;
    }

    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDAOobj) {
        this.accCreditNoteDAOobj = accCreditNoteDAOobj;
    }
    
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setAccCustomerDAO(accCustomerDAO accCustomerDAOObj) {
        this.accCustomerDAOObj = accCustomerDAOObj;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }

    @Override
    public JSONObject getCreditNoteRow(HttpServletRequest request, String[] billids) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String[] creditNote = (billids == null) ? request.getParameter("bills").split(",") : billids;
            int i = 0;
            boolean cnAgainstVI = false;//This flag is used to show the link information where CN against VI(Malasian GST)
            boolean isExport = false;
            if (request.getAttribute(Constants.isExport) != null) {
                isExport = (boolean) request.getAttribute(Constants.isExport);
            }
            boolean isForReport = false;
            String dtype = request.getParameter("dtype");
            if (!StringUtil.isNullOrEmpty(dtype) && dtype.equals("report")) {
                isForReport = true;
            }
            JSONArray jArr = new JSONArray();
            DateFormat df = authHandler.getDateOnlyFormat();
            DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(sessionHandlerImpl.getDateFormatID(request), sessionHandlerImpl.getTimeZoneDifference(request), true);
            HashMap<String, Object> cnRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("creditNote.ID");
            order_by.add("srno");
            order_type.add("asc");
            cnRequestParams.put("filter_names", filter_names);
            cnRequestParams.put("filter_params", filter_params);
            cnRequestParams.put("order_by", order_by);
            cnRequestParams.put("order_type", order_type);

            while (creditNote != null && i < creditNote.length) {
                KwlReturnObject result = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), creditNote[i]);
                CreditNote cn = (CreditNote) result.getEntityList().get(0);
                filter_params.clear();
                filter_params.add(cn.getID());
                cnAgainstVI = cn.getCntype() == 4 ? true : false;
                KwlReturnObject grdresult = accCreditNoteDAOobj.getCreditNoteDetails(cnRequestParams);
                Iterator itr = grdresult.getEntityList().iterator();
                List<CreditNoteDetail> creditNoteDetailList=grdresult.getEntityList();
                boolean cnusedflag = true;
                if (cn.getCntype() != 3 && cn.getCntype() != 4 && cn.isOtherwise() && cn.getCnamount() == cn.getCnamountdue()) {
                    cnusedflag = false;
                }
//                while (itr.hasNext()) {
                for (CreditNoteDetail row :creditNoteDetailList) {
//                    CreditNoteDetail row = (CreditNoteDetail) itr.next();
                    if (!cnusedflag) {
                        break;
                    }
                    JSONObject obj = new JSONObject();
                    Invoice invObj = row.getInvoice();
                    GoodsReceipt grObj = row.getGoodsReceipt();
                    if (invObj != null || grObj != null) {
                        Double invoiceAmount = 0d;
                        Date invoiceCreationDate = null;
                        invoiceCreationDate = cnAgainstVI ? grObj.getCreationDate() : invObj.getCreationDate();
                        if ((cnAgainstVI ? grObj.isIsOpeningBalenceInvoice() : invObj.isIsOpeningBalenceInvoice())) {
                            invoiceAmount = cnAgainstVI ? grObj.getOriginalOpeningBalanceAmount() : invObj.getOriginalOpeningBalanceAmount();
                            obj.put("isOpeningInvoice",true);
                        } else {
                            invoiceAmount = cnAgainstVI ? grObj.getVendorEntry().getAmount() : invObj.getCustomerEntry().getAmount();
                            obj.put("isOpeningInvoice",false);
                        }
                        obj.put("invcreationdate", df.format(invoiceCreationDate));
                        obj.put("invcreationdateinuserdateformat", formatter.format(invoiceCreationDate));
                        obj.put("invduedate", cnAgainstVI ? df.format(row.getGoodsReceipt().getDueDate()) : df.format(row.getInvoice().getDueDate()));
                        obj.put("invduedateinuserdateformat", cnAgainstVI ? formatter.format(row.getGoodsReceipt().getDueDate()) : formatter.format(row.getInvoice().getDueDate()));
                        obj.put("invamountdue", cnAgainstVI ? (grObj.isIsOpeningBalenceInvoice() ? grObj.getOpeningBalanceAmountDue() : grObj.getInvoiceamountdue()) : (invObj.isIsOpeningBalenceInvoice() ? invObj.getOpeningBalanceAmountDue() : invObj.getInvoiceamountdue()));
                        obj.put("invamount", invoiceAmount);
                        obj.put("withoutinventory", false);
                        obj.put(Constants.billid, cn.getID());
                        obj.put("billno", cn.getCreditNoteNumber());
                        obj.put("srno", row.getSrno());
                        obj.put("rowid", row.getID());
                        obj.put("productid", cnAgainstVI ? (row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getInventory().getProduct().getID()) : (row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInventory().getProduct().getID()));
                        obj.put("productdetail", cnAgainstVI ? (row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getInventory().getProduct().getName()) : (row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInventory().getProduct().getName()));
                        obj.put("unitname", cnAgainstVI ? ((row.getInventory() != null && row.getInventory().getUom() != null) ? row.getInventory().getUom().getNameEmptyforNA() : (row.getGoodsReceiptRow() != null && row.getGoodsReceiptRow().getInventory() != null && row.getGoodsReceiptRow().getInventory().getProduct() != null && row.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure() != null) ? row.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA() : "") : (row.getInventory() != null ? row.getInventory().getUom().getNameEmptyforNA() : row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA()));
                        obj.put("desc", cnAgainstVI ? (row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getInventory().getProduct().getDescription()) : (row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInventory().getProduct().getDescription()));
                        obj.put("invstore", (StringUtil.isNullOrEmpty(row.getInvstoreid())) ? "" : row.getInvstoreid());
                        obj.put("invlocation", (StringUtil.isNullOrEmpty(row.getInvlocid())) ? "" : row.getInvlocid());
                        //                    obj.put("remark", row.getRemark());
                        obj.put("currencysymbol", cnAgainstVI ? ((grObj.getCurrency() != null ? (grObj.getCurrency().getSymbol()) : ((cn.getCurrency() == null ? currency.getSymbol() : cn.getCurrency().getSymbol())))) : ((invObj.getCurrency() != null ? (invObj.getCurrency().getSymbol()) : ((cn.getCurrency() == null ? currency.getSymbol() : cn.getCurrency().getSymbol())))));
                        obj.put("currencycode", cnAgainstVI ? ((grObj.getCurrency() != null ? (grObj.getCurrency().getCurrencyCode()) : ((cn.getCurrency() == null ? currency.getCurrencyCode() : cn.getCurrency().getCurrencyCode())))) : ((invObj.getCurrency() != null ? (invObj.getCurrency().getCurrencyCode()) : ((cn.getCurrency() == null ? currency.getCurrencyCode() : cn.getCurrency().getCurrencyCode())))));
                        obj.put("currencyname", cnAgainstVI ? ((grObj.getCurrency() != null ? (grObj.getCurrency().getName()) : ((cn.getCurrency() == null ? currency.getName() : cn.getCurrency().getName())))) : ((invObj.getCurrency() != null ? (invObj.getCurrency().getName()) : ((cn.getCurrency() == null ? currency.getName() : cn.getCurrency().getName())))));
                        obj.put("currencycodeforinvoice", cnAgainstVI ? ((grObj.getCurrency() != null ? (grObj.getCurrency().getCurrencyCode()) : ((cn.getCurrency() == null ? currency.getCurrencyCode() : cn.getCurrency().getCurrencyCode())))) : ((invObj.getCurrency() != null ? (invObj.getCurrency().getCurrencyCode()) : ((cn.getCurrency() == null ? currency.getCurrencyCode() : cn.getCurrency().getCurrencyCode())))));
                        if (cn.isOtherwise() && row.getPaidinvflag() != 1) {
                            obj.put("transectionid", cnAgainstVI ? (row.getGoodsReceipt() == null ? "" : row.getGoodsReceipt().getID()) : (row.getInvoice() == null ? "" : row.getInvoice().getID()));
                            obj.put("transectionno", cnAgainstVI ? (row.getGoodsReceipt() == null ? "" : row.getGoodsReceipt().getGoodsReceiptNumber()) : (row.getInvoice() == null ? "" : row.getInvoice().getInvoiceNumber()));
                            obj.put(Constants.memo, cnAgainstVI ? (row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getGoodsReceipt().getMemo()) : (row.getInvoice() == null ? "" : row.getInvoice().getMemo()));
                        } else {
                            obj.put("transectionid", cnAgainstVI ? (row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getGoodsReceipt().getID()) : (row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInvoice().getID()));
                            obj.put("transectionno", cnAgainstVI ? (row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getGoodsReceipt().getGoodsReceiptNumber()) : (row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInvoice().getInvoiceNumber()));
                            obj.put(Constants.memo, cnAgainstVI ? (row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getGoodsReceipt().getMemo()) : (row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInvoice().getMemo()));
                        }
                        obj.put("otherwise", cn.isOtherwise());
                        Discount disc = row.getDiscount();
                        if (disc != null) {
                            obj.put("discount", disc.getAmountinInvCurrency());
                            obj.put("paidAmountinTransactionCurrency", disc.getDiscountValue());
                        } else {
                            obj.put("discount", 0);
                            obj.put("paidAmountinTransactionCurrency", 0);
                        }

                        obj.put("exchangeratefortransaction", row.getExchangeRateForTransaction());
                        obj.put("quantity", row.getQuantity());
                        obj.put("taxamount", row.getTaxAmount());
                        obj.put("amounttoadjust", row.getAmountToAdjust());
                        obj.put("taxamounttoadjust", row.getTaxAmountToAdjust());
                        obj.put("adjustedamount", row.getAdjustedAmount());
                        obj.put("paidinvflag", row.getPaidinvflag());
                        obj.put("cntype", cn.getCntype());
                        obj.put("remark", !StringUtil.isNullOrEmpty(row.getRemark()) ? row.getRemark() : "");
                        obj.put("grlinkdate", row.getInvoiceLinkDate() != null ? df.format(row.getInvoiceLinkDate()) : "");//Linking invoice date in report CN,DN
                        jArr.put(obj);
                    } else if (!StringUtil.isNullOrEmpty(row.getDebitNoteId())) {// End of Main If
                        result = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), row.getDebitNoteId());
                        DebitNote debitNote = (DebitNote) result.getEntityList().get(0);
                        if (debitNote != null) {
                            Double invoiceAmount = 0d;
                            Date invoiceCreationDate = null;
                            invoiceCreationDate = debitNote.getCreationDate();
                            if (debitNote.isIsOpeningBalenceDN()) {
                                invoiceAmount = debitNote.getOriginalOpeningBalanceBaseAmount();
                                obj.put("isOpeningDnCn", true);
                            } else {
                                invoiceAmount = debitNote.getVendorEntry().getAmount();
                            }
                            obj.put("invcreationdate", df.format(invoiceCreationDate));
                            obj.put("invcreationdateinuserdateformat", formatter.format(invoiceCreationDate));
                            obj.put("invamountdue", debitNote.isIsOpeningBalenceDN() ? debitNote.getOpeningBalanceAmountDue() : debitNote.getDnamountdue());
                            obj.put("invamount", invoiceAmount);
                            obj.put("withoutinventory", false);
                            obj.put(Constants.billid, cn.getID());
                            obj.put("billno", cn.getCreditNoteNumber());
                            obj.put("srno", row.getSrno());
                            obj.put("rowid", row.getID());
                            obj.put("isCnDndetails", true);
                            obj.put("debitNoteId", row.getID());
                            obj.put("currencysymbol", (debitNote.getCurrency() != null ? (debitNote.getCurrency().getSymbol()) : ((cn.getCurrency() == null ? currency.getSymbol() : cn.getCurrency().getSymbol()))));
                            obj.put("currencycode", (debitNote.getCurrency() != null ? (debitNote.getCurrency().getCurrencyCode()) : ((cn.getCurrency() == null ? currency.getCurrencyCode() : cn.getCurrency().getCurrencyCode()))));
                            obj.put("currencyname", (debitNote.getCurrency() != null ? (debitNote.getCurrency().getName()) : ((cn.getCurrency() == null ? currency.getName() : cn.getCurrency().getName()))));
                            obj.put("currencycodeforinvoice", (debitNote.getCurrency() != null ? (debitNote.getCurrency().getCurrencyCode()) : ((cn.getCurrency() == null ? currency.getCurrencyCode() : cn.getCurrency().getCurrencyCode()))));
                            obj.put("transectionid", debitNote == null ? "" : debitNote.getID());
                            obj.put("transectionno", debitNote == null ? "" : debitNote.getDebitNoteNumber());
                            obj.put(Constants.memo, debitNote == null ? "" : debitNote.getMemo());

                            obj.put("otherwise", cn.isOtherwise());
                            Discount disc = row.getDiscount();
                            if (disc != null) {
                                obj.put("discount", disc.getAmountinInvCurrency());
                                obj.put("paidAmountinTransactionCurrency", disc.getDiscountValue());
                            } else {
                                obj.put("discount", 0);
                                obj.put("paidAmountinTransactionCurrency", 0);
                            }

                            obj.put("exchangeratefortransaction", row.getExchangeRateForTransaction());
                            obj.put("quantity", row.getQuantity());
                            obj.put("taxamount", row.getTaxAmount());
                            obj.put("amounttoadjust", row.getAmountToAdjust());
                            obj.put("taxamounttoadjust", row.getTaxAmountToAdjust());
                            obj.put("adjustedamount", row.getAdjustedAmount());
                            obj.put("paidinvflag", row.getPaidinvflag());
                            obj.put("cntype", cn.getCntype());
                            obj.put("remark", !StringUtil.isNullOrEmpty(row.getRemark()) ? row.getRemark() : "");
                            obj.put("grlinkdate", row.getInvoiceLinkDate() != null ? df.format(row.getInvoiceLinkDate()) : "");//Linking invoice date in report CN,DN
                            jArr.put(obj);
                        }
                    }
                }//End of While
                JSONObject paramJobj=StringUtil.convertRequestToJsonObject(request);
                paramJobj.put("companyid", companyid);
                getAccountDetailsForCreditNote(cn, jArr, isExport, isForReport,paramJobj);//I have written this function to get account details on expander click of credit note in CN report.
                i++;
                JSONArray sortedArray = new JSONArray();
                sortedArray = authHandler.sortJson(jArr);
                if (sortedArray.length() == jArr.length()) {
                    jArr = sortedArray;
                }
                jobj.put(Constants.data, jArr);
            }//End of outer  While
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCreditNoteRows : " + ex.getMessage(), ex);
        }
        return jobj;
    }
    
    @Override
    public JSONObject getCreditNoteRows(JSONObject paramJobj, String[] billids) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            String companyid = paramJobj.optString("companyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), paramJobj.getString(Constants.companyKey));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            /**
             * Get Country ID from Company
             */
            KwlReturnObject comp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) comp.getEntityList().get(0);
            int countryid = Integer.parseInt(company.getCountry().getID());
            
            String[] creditNote = (billids == null) ? paramJobj.optString("bills").split(",") : billids;
            int i = 0;
            String description="";
            boolean cnAgainstVI = false;//This flag is used to show the link information where CN against VI(Malasian GST)
            boolean isExport = false;
            if (paramJobj.optString(Constants.isExport, null) != null) {
                isExport = Boolean.parseBoolean(paramJobj.getString(Constants.isExport));
            }
            boolean isForReport = false;
            String dtype = paramJobj.optString("dtype");
            if (!StringUtil.isNullOrEmpty(dtype) && dtype.equals("report")) {
                isForReport = true;
            }
            boolean isEdit = paramJobj.optBoolean(Constants.isEdit);//isEdit flag to show all product in form grid.
            JSONArray jArr = new JSONArray();
            DateFormat df = authHandler.getDateOnlyFormat();    //ERP-20961
            DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(paramJobj.getString(Constants.dateformatid), paramJobj.optString(Constants.timezonedifference), true);
            HashMap<String, Object> cnRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("creditNote.ID");
            order_by.add("srno");
            order_type.add("asc");
            cnRequestParams.put("filter_names", filter_names);
            cnRequestParams.put("filter_params", filter_params);
            cnRequestParams.put("order_by", order_by);
            cnRequestParams.put("order_type", order_type);

            while (creditNote != null && i < creditNote.length) {
                KwlReturnObject result = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), creditNote[i]);
                CreditNote cn = (CreditNote) result.getEntityList().get(0);
                filter_params.clear();
                filter_params.add(cn.getID());
                cnAgainstVI = cn.getCntype() == 4 ? true : false;
                //CnType =5 is for Credit note Against Vendor for Gst
                if (cn.getCntype() == Constants.CreditNoteForOvercharge) {
                    getCreditNoteForOverchargeRows(paramJobj,cn,jArr);
                    jobj.put(Constants.data, jArr);
                    i++;
                } else if (cn.getCntype() != 5) {
                KwlReturnObject grdresult = accCreditNoteDAOobj.getCreditNoteDetails(cnRequestParams);
                Iterator itr = grdresult.getEntityList().iterator();
                List<CreditNoteDetail> creditNoteDetailList=grdresult.getEntityList();
                boolean cnusedflag = true;
                if (cn.getCntype() != 3 && cn.getCntype() != 4 && cn.isOtherwise() && cn.getCnamount() == cn.getCnamountdue()) {
                    cnusedflag = false;
                }
//                while (itr.hasNext()) {
                for (CreditNoteDetail row:creditNoteDetailList) {
//                    CreditNoteDetail row = (CreditNoteDetail) itr.next();
                    if (!cnusedflag) {
                        break;
                    }
                    JSONObject obj = new JSONObject();
                    Invoice invObj = row.getInvoice();
                    GoodsReceipt grObj = row.getGoodsReceipt();
                    if (invObj != null || grObj != null) {
                        Double invoiceAmount = 0d;
                        Date invoiceCreationDate = null;
                        invoiceCreationDate = cnAgainstVI ? grObj.getCreationDate() : invObj.getCreationDate();
                        if ((cnAgainstVI ? grObj.isIsOpeningBalenceInvoice() : invObj.isIsOpeningBalenceInvoice())) {
                            invoiceAmount = cnAgainstVI ? grObj.getOriginalOpeningBalanceAmount() : invObj.getOriginalOpeningBalanceAmount();
                            obj.put("isOpeningInvoice",true);
                        } else {
//                            invoiceCreationDate = cnAgainstVI ? grObj.getJournalEntry().getEntryDate() : invObj.getJournalEntry().getEntryDate();
                            invoiceAmount = cnAgainstVI ? grObj.getVendorEntry().getAmount() : invObj.getCustomerEntry().getAmount();
                            obj.put("isOpeningInvoice",false);
                        }
                        obj.put("invcreationdate", df.format(invoiceCreationDate));
                        obj.put("invcreationdateinuserdateformat", formatter.format(invoiceCreationDate));
                        obj.put("invduedate", cnAgainstVI ? df.format(row.getGoodsReceipt().getDueDate()) : df.format(row.getInvoice().getDueDate()));
                        obj.put("invduedateinuserdateformat", cnAgainstVI ? formatter.format(row.getGoodsReceipt().getDueDate()) : formatter.format(row.getInvoice().getDueDate()));
                        obj.put("invamountdue", cnAgainstVI ? (grObj.isIsOpeningBalenceInvoice() ? grObj.getOpeningBalanceAmountDue() : grObj.getInvoiceamountdue()) : (invObj.isIsOpeningBalenceInvoice() ? invObj.getOpeningBalanceAmountDue() : invObj.getInvoiceamountdue()));
                        obj.put("invamount", invoiceAmount);
                        obj.put("withoutinventory", false);
                        obj.put(Constants.billid, cn.getID());
                        obj.put("billno", cn.getCreditNoteNumber());
                        obj.put("srno", row.getSrno());
                        obj.put("rowid", row.getID());
                        obj.put("productid", cnAgainstVI ? (row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getInventory().getProduct().getID()) : (row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInventory().getProduct().getID()));
                        obj.put("productdetail", cnAgainstVI ? (row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getInventory().getProduct().getName()) : (row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInventory().getProduct().getName()));
                        obj.put("unitname", cnAgainstVI ? ((row.getInventory() != null && row.getInventory().getUom() != null) ? row.getInventory().getUom().getNameEmptyforNA() : (row.getGoodsReceiptRow() != null && row.getGoodsReceiptRow().getInventory() != null && row.getGoodsReceiptRow().getInventory().getProduct() != null && row.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure() != null) ? row.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA() : "") : (row.getInventory() != null ? row.getInventory().getUom().getNameEmptyforNA() : row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA()));
                        obj.put("desc", cnAgainstVI ? (row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getInventory().getProduct().getDescription()) : (row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInventory().getProduct().getDescription()));
                        obj.put("invstore", (StringUtil.isNullOrEmpty(row.getInvstoreid())) ? "" : row.getInvstoreid());
                        obj.put("invlocation", (StringUtil.isNullOrEmpty(row.getInvlocid())) ? "" : row.getInvlocid());
                        //                    obj.put("remark", row.getRemark());
                        obj.put("currencysymbol", cnAgainstVI ? ((grObj.getCurrency() != null ? (grObj.getCurrency().getSymbol()) : ((cn.getCurrency() == null ? currency.getSymbol() : cn.getCurrency().getSymbol())))) : ((invObj.getCurrency() != null ? (invObj.getCurrency().getSymbol()) : ((cn.getCurrency() == null ? currency.getSymbol() : cn.getCurrency().getSymbol())))));
                        obj.put("currencycode", cnAgainstVI ? ((grObj.getCurrency() != null ? (grObj.getCurrency().getCurrencyCode()) : ((cn.getCurrency() == null ? currency.getCurrencyCode() : cn.getCurrency().getCurrencyCode())))) : ((invObj.getCurrency() != null ? (invObj.getCurrency().getCurrencyCode()) : ((cn.getCurrency() == null ? currency.getCurrencyCode() : cn.getCurrency().getCurrencyCode())))));
                        obj.put("currencyname", cnAgainstVI ? ((grObj.getCurrency() != null ? (grObj.getCurrency().getName()) : ((cn.getCurrency() == null ? currency.getName() : cn.getCurrency().getName())))) : ((invObj.getCurrency() != null ? (invObj.getCurrency().getName()) : ((cn.getCurrency() == null ? currency.getName() : cn.getCurrency().getName())))));
                        obj.put("currencycodeforinvoice", cnAgainstVI ? ((grObj.getCurrency() != null ? (grObj.getCurrency().getCurrencyCode()) : ((cn.getCurrency() == null ? currency.getCurrencyCode() : cn.getCurrency().getCurrencyCode())))) : ((invObj.getCurrency() != null ? (invObj.getCurrency().getCurrencyCode()) : ((cn.getCurrency() == null ? currency.getCurrencyCode() : cn.getCurrency().getCurrencyCode())))));
                        if (cn.isOtherwise() && row.getPaidinvflag() != 1) {
                            obj.put("transectionid", cnAgainstVI ? (row.getGoodsReceipt() == null ? "" : row.getGoodsReceipt().getID()) : (row.getInvoice() == null ? "" : row.getInvoice().getID()));
                            obj.put("transectionno", cnAgainstVI ? (row.getGoodsReceipt() == null ? "" : row.getGoodsReceipt().getGoodsReceiptNumber()) : (row.getInvoice() == null ? "" : row.getInvoice().getInvoiceNumber()));
                            obj.put(Constants.memo, cnAgainstVI ? (row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getGoodsReceipt().getMemo()) : (row.getInvoice() == null ? "" : row.getInvoice().getMemo()));
                        } else {
                            obj.put("transectionid", cnAgainstVI ? (row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getGoodsReceipt().getID()) : (row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInvoice().getID()));
                            obj.put("transectionno", cnAgainstVI ? (row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getGoodsReceipt().getGoodsReceiptNumber()) : (row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInvoice().getInvoiceNumber()));
                            obj.put(Constants.memo, cnAgainstVI ? (row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getGoodsReceipt().getMemo()) : (row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInvoice().getMemo()));
                        }
                        obj.put("otherwise", cn.isOtherwise());
                        Discount disc = row.getDiscount();
                        if (disc != null) {
                            obj.put("discount", disc.getAmountinInvCurrency());
                            obj.put("paidAmountinTransactionCurrency", disc.getDiscountValue());
                        } else {
                            obj.put("discount", 0);
                            obj.put("paidAmountinTransactionCurrency", 0);
                        }

                        obj.put("exchangeratefortransaction", row.getExchangeRateForTransaction());
                        obj.put("quantity", row.getQuantity());
                        
                        if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                            obj.put("invoicetaxamount", row.getTaxAmount());
                            obj.put("discountType", disc.isInPercent());
                        } else {
                            obj.put("taxamount", row.getTaxAmount());
                        }

                        obj.put("amounttoadjust", row.getAmountToAdjust());
                        obj.put("taxamounttoadjust", row.getTaxAmountToAdjust());
                        obj.put("adjustedamount", row.getAdjustedAmount());
                        obj.put("paidinvflag", row.getPaidinvflag());
                        obj.put("cntype", cn.getCntype());
                        obj.put("remark", !StringUtil.isNullOrEmpty(row.getRemark()) ? row.getRemark() : "");
                        obj.put("grlinkdate", row.getInvoiceLinkDate() != null ? df.format(row.getInvoiceLinkDate()) : "");//Linking invoice date in report CN,DN
                        jArr.put(obj);
                    } else if (!StringUtil.isNullOrEmpty(row.getDebitNoteId())) {// End of Main If
                        result = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), row.getDebitNoteId());
                        DebitNote debitNote = (DebitNote) result.getEntityList().get(0);
                        if (debitNote != null) {
                            Double invoiceAmount = 0d;
                            Date invoiceCreationDate = null;
                            invoiceCreationDate = debitNote.getCreationDate();
                            if (debitNote.isIsOpeningBalenceDN()) {
                                invoiceAmount = debitNote.getOriginalOpeningBalanceBaseAmount();
                                obj.put("isOpeningDnCn", true);
                            } else {
//                                invoiceCreationDate = debitNote.getJournalEntry().getEntryDate();
                                invoiceAmount = debitNote.getVendorEntry().getAmount();
                            }
                            obj.put("invcreationdate", df.format(invoiceCreationDate));
                            obj.put("invcreationdateinuserdateformat", formatter.format(invoiceCreationDate));
                            obj.put("invamountdue", debitNote.isIsOpeningBalenceDN() ? debitNote.getOpeningBalanceAmountDue() : debitNote.getDnamountdue());
                            obj.put("invamount", invoiceAmount);
                            obj.put("withoutinventory", false);
                            obj.put(Constants.billid, cn.getID());
                            obj.put("billno", cn.getCreditNoteNumber());
                            obj.put("srno", row.getSrno());
                            obj.put("rowid", row.getID());
                            obj.put("isCnDndetails", true);
                            obj.put("debitNoteId", row.getID());
                            obj.put("currencysymbol", (debitNote.getCurrency() != null ? (debitNote.getCurrency().getSymbol()) : ((cn.getCurrency() == null ? currency.getSymbol() : cn.getCurrency().getSymbol()))));
                            obj.put("currencycode", (debitNote.getCurrency() != null ? (debitNote.getCurrency().getCurrencyCode()) : ((cn.getCurrency() == null ? currency.getCurrencyCode() : cn.getCurrency().getCurrencyCode()))));
                            obj.put("currencyname", (debitNote.getCurrency() != null ? (debitNote.getCurrency().getName()) : ((cn.getCurrency() == null ? currency.getName() : cn.getCurrency().getName()))));
                            obj.put("currencycodeforinvoice", (debitNote.getCurrency() != null ? (debitNote.getCurrency().getCurrencyCode()) : ((cn.getCurrency() == null ? currency.getCurrencyCode() : cn.getCurrency().getCurrencyCode()))));
                            obj.put("transectionid", debitNote == null ? "" : debitNote.getID());
                            obj.put("transectionno", debitNote == null ? "" : debitNote.getDebitNoteNumber());
                            obj.put(Constants.memo, debitNote == null ? "" : debitNote.getMemo());
                            obj.put("otherwise", cn.isOtherwise());
                            Discount disc = row.getDiscount();
                            if (disc != null) {
                                obj.put("discount", disc.getAmountinInvCurrency());
                                obj.put("paidAmountinTransactionCurrency", disc.getDiscountValue());
                            } else {
                                obj.put("discount", 0);
                                obj.put("paidAmountinTransactionCurrency", 0);
                            }

                            obj.put("exchangeratefortransaction", row.getExchangeRateForTransaction());
                            obj.put("quantity", row.getQuantity());
                            if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                                obj.put("invoicetaxamount", row.getTaxAmount());
                                obj.put("discountType", disc.isInPercent());
                            } else {
                                obj.put("taxamount", row.getTaxAmount());
                            }
                            obj.put("amounttoadjust", row.getAmountToAdjust());
                            obj.put("taxamounttoadjust", row.getTaxAmountToAdjust());
                            obj.put("adjustedamount", row.getAdjustedAmount());
                            obj.put("paidinvflag", row.getPaidinvflag());
                            obj.put("cntype", cn.getCntype());
                            obj.put("remark", !StringUtil.isNullOrEmpty(row.getRemark()) ? row.getRemark() : "");
                            obj.put("grlinkdate", row.getInvoiceLinkDate() != null ? df.format(row.getInvoiceLinkDate()) : "");//Linking invoice date in report CN,DN
                            jArr.put(obj);
                        }

                    }
                }//End of While
                getAccountDetailsForCreditNote(cn, jArr, isExport, isForReport,paramJobj);//I have written this function to get account details on expander click of credit note in CN report.
                i++;
                JSONArray sortedArray = new JSONArray();
                sortedArray = authHandler.sortJson(jArr);
                if (sortedArray.length() == jArr.length()) {
                    jArr = sortedArray;
                }
                jobj.put(Constants.data, jArr);
                } else {

                    filter_params.clear();
                    filter_params.add(cn.getID());
                    KwlReturnObject grdresult = accCreditNoteDAOobj.getCreditNoteDetailsGst(cnRequestParams);
                    Iterator itr = grdresult.getEntityList().iterator();
            
                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(cn.getCompany().getCompanyID(), Constants.Acc_Credit_Note_ModuleId));
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

                
                    String goodsReceiptId = "";
                    while (itr.hasNext()) {
                        CreditNoteAgainstVendorGst row = (CreditNoteAgainstVendorGst) itr.next();
                        GoodsReceipt grObj = row.getVidetails().getGoodsReceipt();
                        if (goodsReceiptId.equals(grObj.getID()) && !isEdit) {//This is temporary changes. Need to show product in Debit Note for Overcharge/Undercharge.
                            continue;
                        }
                        JSONObject obj = new JSONObject();
                        obj.put("billid", cn.getID());
                        obj.put("billno", cn.getCreditNoteNumber());
                        obj.put("externalcurrencyrate", cn.getExternalCurrencyRate());
                        obj.put("srno", row.getSrno());
                        obj.put("rowid", row.getID());
                        obj.put("currencysymbol", (cn.getCurrency() == null ? currency.getSymbol() : cn.getCurrency().getSymbol()));
                        obj.put("productid", row.getProduct().getID());
                        obj.put("productname", row.getProduct().getName());
                        obj.put("baseuomname", row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA());

                        if (!StringUtil.isNullOrEmpty(row.getDescription())) {
                            description = row.getDescription();
                        } else if (!StringUtil.isNullOrEmpty(row.getProduct().getDescription())) {
                            description = row.getProduct().getDescription();
                        } else {
                            description = "";
                        }
                       
                        obj.put("desc", StringUtil.DecodeText(description));

                        obj.put("type", row.getProduct().getProducttype() == null ? "" : row.getProduct().getProducttype().getName());
                        obj.put("transectionid", grObj.getID());
                        obj.put("transectionno", grObj.getGoodsReceiptNumber());
//                        obj.put("invcreationdate", df.format(grObj.getJournalEntry().getEntryDate()));
                        obj.put("invcreationdate", df.format(grObj.getCreationDate()));
                        obj.put("invamountdue", grObj.getInvoiceamountdue());
                        obj.put("invamount", grObj.getInvoiceamountdue());
                        obj.put("grlinkdate", cn.getCreationDate()!=null ? df.format(cn.getCreationDate()) : "");
                        obj.put("invduedate", df.format(grObj.getDueDate()));
                        obj.put("pid", row.getProduct().getProductid());
                        obj.put("memo", row.getRemark());
                        obj.put("reason", (row.getReason() != null && !isExport) ? row.getReason().getID() : "");
                        obj.put("prtaxid", (row.getTax() != null) ? row.getTax().getID() : "");
                        obj.put("taxamount", row.getRowTaxAmount());
                        obj.put("taxamountforlinking", row.getRowTaxAmount());
                        obj.put("discountispercent", row.getDiscountispercent());
                        obj.put("prdiscount", row.getDiscount());
                        obj.put("quantity", row.getActualQuantity());
                        obj.put("dquantity", row.getReturnQuantity());
                        /*
                         * For Malaysian Company Set rowid ,linkedid linkto while save credit note
                         */
                            if (cn.getCntype() == 5) {
                                if (row.getVidetails() != null) {
                                    obj.put("linkto", row.getVidetails().getGoodsReceipt().getGoodsReceiptNumber());
                                    obj.put("linkid", row.getVidetails().getGoodsReceipt().getID());
                                    obj.put("rowid", row.getVidetails().getID());
                                    obj.put("savedrowid", row.getVidetails().getGoodsReceipt());
                                    obj.put("linktype", 1);
                                } else {
                                    obj.put("linkto", "");
                                    obj.put("linkid", "");
                                    obj.put("linktype", -1);
                                }
                                obj.put("cntype", cn.getCntype());
                            }
                        // obj.put("isNoteAlso", cn.);
                        double baseuomrate = row.getBaseuomrate();
                        if (row.getUom() != null) {
                            obj.put("uomid", row.getUom().getID());
                        } else {
                            obj.put("uomid", row.getProduct().getUnitOfMeasure() != null ? row.getProduct().getUnitOfMeasure().getID() : "");
                        }
                        obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(row.getReturnQuantity(), baseuomrate, companyid));
                        obj.put("baseuomrate", baseuomrate);
                        obj.put("copyquantity", row.getReturnQuantity());
                        obj.put("description", StringUtil.DecodeText(description));
                        obj.put("remark", row.getRemark());
                        obj.put("rate", row.getRate());
                        /**
                         * get line level Term Details for CREDIT note Edit and view case
                         */
                        if (countryid == Constants.USA_country_id) { // Fetch  term details of Product
                            JSONObject json = new JSONObject();
                            json.put("creditnotedetail", row.getID());
                            KwlReturnObject result6 = accCreditNoteDAOobj.getCreditNoteDetailTermMap(json);
                            if (result6.getEntityList() != null && result6.getEntityList().size() > 0 && result6.getEntityList().get(0) != null) {
                                ArrayList<CreditNoteDetailTermMap> CNTermDetailMap = (ArrayList<CreditNoteDetailTermMap>) result6.getEntityList();
                                JSONArray DNTermJsonArry = new JSONArray();
                                double termAccount = 0.0;
                                for (CreditNoteDetailTermMap CNTermsMapObj : CNTermDetailMap) {
                                    JSONObject CNTermJsonObj = new JSONObject();
                                    CNTermJsonObj.put("id", CNTermsMapObj.getId());
                                    CNTermJsonObj.put("termid", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getId());
                                    CNTermJsonObj.put("productentitytermid", CNTermsMapObj.getEntitybasedLineLevelTermRate() != null ? CNTermsMapObj.getEntitybasedLineLevelTermRate().getId() : "");
                                    CNTermJsonObj.put("isDefault", CNTermsMapObj.isIsGSTApplied());
                                    CNTermJsonObj.put("term", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getTerm());
                                    CNTermJsonObj.put("formula", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getFormula());
                                    CNTermJsonObj.put("formulaids", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getFormula());
                                    CNTermJsonObj.put("termpercentage", CNTermsMapObj.getPercentage());
                                    CNTermJsonObj.put("originalTermPercentage", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getPercentage()); // For Service Tax Abatemnt calculation
                                    CNTermJsonObj.put("termamount", CNTermsMapObj.getTermamount());
                                    CNTermJsonObj.put("glaccountname", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getAccountName());
                                    CNTermJsonObj.put("glaccount", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getID());
                                    CNTermJsonObj.put("IsOtherTermTaxable", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().isOtherTermTaxable());
                                    CNTermJsonObj.put("sign", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getSign());
                                    CNTermJsonObj.put("purchasevalueorsalevalue", CNTermsMapObj.getPurchaseValueOrSaleValue());
                                    CNTermJsonObj.put("deductionorabatementpercent", CNTermsMapObj.getDeductionOrAbatementPercent());
                                    CNTermJsonObj.put("assessablevalue", CNTermsMapObj.getAssessablevalue());
                                    CNTermJsonObj.put("taxtype", CNTermsMapObj.getTaxType());
                                    CNTermJsonObj.put("taxvalue", CNTermsMapObj.getPercentage());
                                    CNTermJsonObj.put("termtype", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getTermType());
                                    CNTermJsonObj.put("termsequence", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getTermSequence());
                                    CNTermJsonObj.put("formType", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getFormType());
                                    CNTermJsonObj.put("accountid", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getID());
                                    if (CNTermsMapObj.getEntitybasedLineLevelTermRate() != null && CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms() != null && CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getPayableAccount() != null) {     //SDP-12993
                                        CNTermJsonObj.put("payableaccountid", (CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getPayableAccount().getID()));
                                    } else {
                                        CNTermJsonObj.put("payableaccountid", "");
                                    }
                                    DNTermJsonArry.put(CNTermJsonObj);
                                    termAccount += CNTermsMapObj.getTermamount();
                                }
                                obj.put("LineTermdetails", DNTermJsonArry.toString());
                                obj.put("recTermAmount", termAccount);
                                //obj.put("amountwithtax", noteTaxEntry.getAmount() + termAccount);
                            }
                        }
                        //For Detail Export
                        if (formatter != null) {
                            obj.put("invcreationdateinuserdateformat", formatter.format(grObj.getCreationDate()));
                            obj.put("invduedateinuserdateformat", formatter.format(grObj.getDueDate()));
                        }
                        obj.put("currencycodeforinvoice", grObj.getCurrency().getCurrencyCode());
                        
                        // ## Get Custom Field Data 
                        Map<String, Object> variableMap = new HashMap<String, Object>();
                        HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                        ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                        if (row.getJedid() != null) {
                            Detailfilter_names.add(Constants.Acc_jedetailId);
                            Detailfilter_params.add(row.getJedid().getID());
                            invDetailRequestParams.put("filter_names", Detailfilter_names);
                            invDetailRequestParams.put("filter_params", Detailfilter_params);
                            KwlReturnObject idcustresult = accCreditNoteDAOobj.geCreditNoteCustomData(invDetailRequestParams);
                            if (idcustresult.getEntityList().size() > 0) {
                                AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                                AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                                if (jeDetailCustom != null) {
                                    boolean getlineItemDetailsflag = (paramJobj.optString("getlineItemDetailsflag", null) != null) ? Boolean.FALSE.parseBoolean((String) paramJobj.get("getlineItemDetailsflag")) : false;
                                    JSONObject params = new JSONObject();
                                    params.put(Constants.isExport, isExport);
                                    params.put("isForReport", isForReport);
                                    params.put(Constants.isdefaultHeaderMap, paramJobj.optBoolean(Constants.isdefaultHeaderMap, false));
                                    fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                                }
                            }
                        }
                        
                        goodsReceiptId = grObj.getID();
                        jArr.put(obj);
                    }
                    i++;
                    jobj.put(Constants.data, jArr);
                }
            }//End of outer  While
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCreditNoteRows : " + ex.getMessage(), ex);
        }

        return jobj;
    }
    
    public void getCreditNoteForOverchargeRows(JSONObject paramJobj, CreditNote cn, JSONArray jArr) throws JSONException, ServiceException, SessionExpiredException {
        try {
            String companyid = paramJobj.optString(Constants.companyKey);
            DateFormat df = authHandler.getDateOnlyFormat();    //ERP-20961
            DateFormat userdf = null;
            if (paramJobj.has("userdateformatter") && paramJobj.get("userdateformatter") != null) {
                userdf = (DateFormat) paramJobj.get("userdateformatter");
            }
            if (paramJobj.has(Constants.userdf) && paramJobj.get(Constants.userdf) != null) {
                userdf = (DateFormat) paramJobj.get(Constants.userdf);
            }
            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<>();
            HashMap<String, String> customDateFieldMap = new HashMap<>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(cn.getCompany().getCompanyID(), Constants.Acc_Credit_Note_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            
            boolean isForReport = paramJobj.optBoolean(Constants.isForReport);
            boolean isExport = paramJobj.optBoolean(Constants.isExport);
            boolean isEdit = paramJobj.optBoolean(Constants.isEdit);//isEdit flag to show all product in form grid.
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), paramJobj.getString(Constants.companyKey));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            /**
             * Get countryid from company
             */
            KwlReturnObject comp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) comp.getEntityList().get(0);
            int countryid = Integer.parseInt(company.getCountry().getID());
            
            Set<CreditNoteAgainstVendorGst> cnDetails = cn.getRowsGst();
            String description = "";
            String invoiceId = "";
            for (CreditNoteAgainstVendorGst row : cnDetails) {
                Invoice invoice = row.getInvoiceDetail().getInvoice();
                if(invoice.getID().equalsIgnoreCase(invoiceId) && !isEdit){//This is temporary changes. Need to show product in Credit Note for Overcharge/Undercharge.
                    continue;
                }
                JSONObject obj = new JSONObject();
                obj.put("billid", cn.getID());
                obj.put("billno", cn.getCreditNoteNumber());
                obj.put("externalcurrencyrate", cn.getExternalCurrencyRate());
                obj.put("srno", row.getSrno());
                obj.put("rowid", row.getID());
                obj.put("currencysymbol", (cn.getCurrency() == null ? currency.getSymbol() : cn.getCurrency().getSymbol()));
                obj.put("productid", row.getProduct().getID());
                obj.put("productname", row.getProduct().getName());
                obj.put("baseuomname", row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA());

                if (!StringUtil.isNullOrEmpty(row.getDescription())) {
                    description = row.getDescription();
                } else if (!StringUtil.isNullOrEmpty(row.getProduct().getDescription())) {
                    description = row.getProduct().getDescription();
                } else {
                    description = "";
                }
                obj.put("desc", StringUtil.DecodeText(description));
                obj.put("type", row.getProduct().getProducttype() == null ? "" : row.getProduct().getProducttype().getName());
                obj.put("transectionid", invoice.getID());
                obj.put("transectionno", invoice.getInvoiceNumber());
                obj.put("invcreationdate", df.format(invoice.getCreationDate()));
                obj.put("invamountdue", invoice.getInvoiceamountdue());
                obj.put("invamount", invoice.getInvoiceamountdue());
                obj.put("grlinkdate", cn.getCreationDate() != null ? df.format(cn.getCreationDate()) : "");
                obj.put("invduedate", df.format(invoice.getDueDate()));
                obj.put("pid", row.getProduct().getProductid());
                obj.put("memo", row.getRemark());
                obj.put("reason", (row.getReason() != null && !isExport) ? row.getReason().getID() : "");
                obj.put("prtaxid", (row.getTax() != null) ? row.getTax().getID() : "");
                obj.put("taxamount", row.getRowTaxAmount());
                obj.put("taxamountforlinking", row.getRowTaxAmount());
                obj.put("discountispercent", row.getDiscountispercent());
                obj.put("prdiscount", row.getDiscount());
                obj.put("quantity", row.getActualQuantity());
                obj.put("dquantity", row.getReturnQuantity());
                obj.put("linkto", row.getInvoiceDetail().getInvoice().getInvoiceNumber());
                obj.put("linkid", row.getInvoiceDetail().getInvoice().getID());
                obj.put("rowid", row.getInvoiceDetail().getID());
                obj.put("linktype", 1);
                obj.put("includeprotax", (row.getTax() != null));
                obj.put("cntype", cn.getCntype());
                
                double baseuomrate = row.getBaseuomrate();
                if (row.getUom() != null) {
                    obj.put("uomid", row.getUom().getID());
                } else {
                    obj.put("uomid", row.getProduct().getUnitOfMeasure() != null ? row.getProduct().getUnitOfMeasure().getID() : "");
                }
                obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(row.getReturnQuantity(), baseuomrate, companyid));
                obj.put("baseuomrate", baseuomrate);
                obj.put("copyquantity", row.getReturnQuantity());
                obj.put("description", StringUtil.DecodeText(description));
                obj.put("remark", row.getRemark());
                obj.put("rate", row.getRate());
                /**
                 * get line level Term Details for Credit note in Edit and View case
                 */
                if (countryid == Constants.USA_country_id) { // Fetch  term details of Product
                    JSONObject json = new JSONObject();
                    json.put("creditnotedetail", row.getID());
                    KwlReturnObject result6 = accCreditNoteDAOobj.getCreditNoteDetailTermMap(json);
                    if (result6.getEntityList() != null && result6.getEntityList().size() > 0 && result6.getEntityList().get(0) != null) {
                        ArrayList<CreditNoteDetailTermMap> CNTermDetailMap = (ArrayList<CreditNoteDetailTermMap>) result6.getEntityList();
                        JSONArray DNTermJsonArry = new JSONArray();
                        double termAccount = 0.0;
                        for (CreditNoteDetailTermMap CNTermsMapObj : CNTermDetailMap) {
                            JSONObject CNTermJsonObj = new JSONObject();
                            CNTermJsonObj.put("id", CNTermsMapObj.getId());
                            CNTermJsonObj.put("termid", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getId());
                            CNTermJsonObj.put("productentitytermid", CNTermsMapObj.getEntitybasedLineLevelTermRate() != null ? CNTermsMapObj.getEntitybasedLineLevelTermRate().getId() : "");
                            CNTermJsonObj.put("isDefault", CNTermsMapObj.isIsGSTApplied());
                            CNTermJsonObj.put("term", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getTerm());
                            CNTermJsonObj.put("formula", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getFormula());
                            CNTermJsonObj.put("formulaids", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getFormula());
                            CNTermJsonObj.put("termpercentage", CNTermsMapObj.getPercentage());
                            CNTermJsonObj.put("originalTermPercentage", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getPercentage()); // For Service Tax Abatemnt calculation
                            CNTermJsonObj.put("termamount", CNTermsMapObj.getTermamount());
                            CNTermJsonObj.put("glaccountname", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getAccountName());
                            CNTermJsonObj.put("glaccount", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getID());
                            CNTermJsonObj.put("IsOtherTermTaxable", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().isOtherTermTaxable());
                            CNTermJsonObj.put("sign", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getSign());
                            CNTermJsonObj.put("purchasevalueorsalevalue", CNTermsMapObj.getPurchaseValueOrSaleValue());
                            CNTermJsonObj.put("deductionorabatementpercent", CNTermsMapObj.getDeductionOrAbatementPercent());
                            CNTermJsonObj.put("assessablevalue", CNTermsMapObj.getAssessablevalue());
                            CNTermJsonObj.put("taxtype", CNTermsMapObj.getTaxType());
                            CNTermJsonObj.put("taxvalue", CNTermsMapObj.getPercentage());
                            CNTermJsonObj.put("termtype", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getTermType());
                            CNTermJsonObj.put("termsequence", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getTermSequence());
                            CNTermJsonObj.put("formType", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getFormType());
                            CNTermJsonObj.put("accountid", CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getID());
                            if (CNTermsMapObj.getEntitybasedLineLevelTermRate() != null && CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms() != null && CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getPayableAccount() != null) {     //SDP-12993
                                CNTermJsonObj.put("payableaccountid", (CNTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getPayableAccount().getID()));
                            } else {
                                CNTermJsonObj.put("payableaccountid", "");
                            }
                            DNTermJsonArry.put(CNTermJsonObj);
                            termAccount += CNTermsMapObj.getTermamount();
                        }
                        obj.put("LineTermdetails", DNTermJsonArry.toString());
                        obj.put("recTermAmount", termAccount);
                        //obj.put("amountwithtax", noteTaxEntry.getAmount() + termAccount);
                    }
                }
                //For Detail Export
                if (userdf != null) {
                    obj.put("invcreationdateinuserdateformat", userdf.format(invoice.getCreationDate()));
                    obj.put("invduedateinuserdateformat", userdf.format(invoice.getDueDate()));
                }
                obj.put("currencycodeforinvoice", invoice.getCurrency().getCurrencyCode());

                //Get Custom Field Data 
                Map<String, Object> variableMap = new HashMap<>();
                HashMap<String, Object> invDetailRequestParams = new HashMap<>();
                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                if (row.getJedid() != null) {
                    Detailfilter_names.add(Constants.Acc_jedetailId);
                    Detailfilter_params.add(row.getJedid().getID());
                    invDetailRequestParams.put("filter_names", Detailfilter_names);
                    invDetailRequestParams.put("filter_params", Detailfilter_params);
                    KwlReturnObject idcustresult = accCreditNoteDAOobj.geCreditNoteCustomData(invDetailRequestParams);
                    if (idcustresult.getEntityList().size() > 0) {
                        AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                        if (jeDetailCustom != null) {
                            JSONObject params = new JSONObject();
                            params.put(Constants.isExport, isExport);
                            params.put(Constants.isForReport, isForReport);
                            params.put(Constants.isdefaultHeaderMap, paramJobj.optBoolean(Constants.isdefaultHeaderMap, false));
                            fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                        }
                    }
                }
                invoiceId = invoice.getID();

                jArr.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCreditNoteServiceImplCMN.getCreditNoteForOverchargeRows" + ex.getMessage(), ex);
        }
    }
    
     public void getAccountDetailsForCreditNote(CreditNote cn, JSONArray jArr,boolean isExport,boolean isForReport,JSONObject paramJobj) throws ServiceException {
        try {
            Set<JournalEntryDetail> jedDetails = cn.getJournalEntry() != null ? cn.getJournalEntry().getDetails() : new HashSet(0);
            String companyid = paramJobj.optString("companyid");
            if (jedDetails.size() > 0) {
                HashMap<String, Object> fieldrequestParams = new HashMap();
                HashMap<String, String> customFieldMap = new HashMap<String, String>();
                DateFormat userDateFormat=null;
                if(paramJobj.has(Constants.userdateformat)){
                    userDateFormat=new SimpleDateFormat(String.valueOf(paramJobj.get(Constants.userdateformat)));
                }
                HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(cn.getCompany().getCompanyID(), Constants.Acc_Credit_Note_ModuleId));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

                Iterator<JournalEntryDetail> jeDetailIte = jedDetails.iterator();
                while (jeDetailIte.hasNext()) {
                    JournalEntryDetail jedetail = jeDetailIte.next();
                    if (jedetail.getAccount() != null && jedetail.getAccount().getMastertypevalue() != 4 && !jedetail.isIsSeparated()) {//4 for not including tax as a account.
                        JSONObject obj = new JSONObject();
                        if (jedetail.getID().equalsIgnoreCase(cn.getCustomerEntry().getID())) {
                            continue;
                        }
                        obj.put(Constants.billid, cn.getID());
                        if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                            obj.put("account", !StringUtil.isNullOrEmpty(jedetail.getAccount().getName()) ? jedetail.getAccount().getID() : "-");
                            obj.put("accountValue", !StringUtil.isNullOrEmpty(jedetail.getAccount().getName()) ? jedetail.getAccount().getName() : "-");
                            obj.put("desc", jedetail.getDescription() != null ? StringUtil.DecodeText(jedetail.getDescription()) : "-");
                        } else {
                            obj.put("accountname", !StringUtil.isNullOrEmpty(jedetail.getAccount().getName()) ? jedetail.getAccount().getName() : "-");
                            obj.put("description", jedetail.getDescription() != null ? StringUtil.DecodeText(jedetail.getDescription()) : "-");
                        }
                        
                        double amount = authHandler.round(jedetail.getAmount(), companyid);
                        amount=authHandler.round(amount, companyid);
                        if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                            obj.put("amount", amount);
                        } else if (cn.isIncludingGST()) {//CN with includingGST
                            obj.put("totalamount", cn.getCnamount());
                        } else {
                            obj.put("totalamount", amount);
                        }
                        obj.put("isIncludingGst", cn.isIncludingGST());
                        obj.put("totalamountforaccount", amount);
                        obj.put("currencysymbol", cn.getCurrency() != null ? cn.getCurrency().getSymbol() : "");
                        obj.put("isaccountdetails", true);
                        obj.put("taxpercent", 0);
                        obj.put("taxamount", 0);
                        obj.put("taxamountforaccount", 0);
                        obj.put("debit", jedetail.isDebit() ? "Debit" : "Credit");
                        obj.put("cntype", cn.getCntype());
                        String jeDetailId = jedetail.getID();
                        String jeDetailaccid = jedetail.getAccount().getID();
                        /*
                        ERP-34829
                        * when credit note is created against vendor for malaysian country then (Tax Amount) is not fetched for xls,pdf file display(ERP-34829)  .
                        * For Export to xls,pdf Fuctionality if condition will true  only for malaysian country (cn.getCntype() == 5).
                        * if block written to get  line level tax (product tax) of particular product in JSONObject obj with key (taxamount) for dispaying it into xls and pdf.
             
                         */
                        if (cn.getCntype() == 5) {
                            Set<CreditNoteAgainstVendorGst> Taxset = cn.getRowsGst() != null ? cn.getRowsGst() : new HashSet(0);
                            Iterator<CreditNoteAgainstVendorGst> taxIte = Taxset.iterator();
                            while (taxIte.hasNext()) {
                                CreditNoteAgainstVendorGst txEntry = taxIte.next();                               
                                JournalEntryDetail taxJeDetailIdObj = txEntry.getJedid();
                                String taxJeDetailId = "";
                                if (taxJeDetailIdObj != null) {
                                    taxJeDetailId = taxJeDetailIdObj.getID();
                                }
                                String productSalesAccountId = txEntry.getProduct() != null ? txEntry.getProduct().getPurchaseAccount().getID() : "";
                                 if (StringUtil.equal(jeDetailId, taxJeDetailId) && StringUtil.equal(jeDetailaccid, productSalesAccountId)) {
                                    double txAmount = authHandler.round(txEntry.getRowTaxAmount(), companyid);
                                    obj.put("taxamount", txAmount);
                                    obj.put("taxamountforaccount", txAmount);
                                    if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                                        obj.put("amountwithtax", amount + txAmount);
                                    }
                                    obj.put("srNoForRow", txEntry.getSrno());
                                    if (cn.isIncludingGST()) {//CN with includingGST
                                        obj.put("totalamount", cn.getCnamount());
                                    } else {
                                        obj.put("totalamount", amount);
                                    }
                                }

                            }
                         } else {
                        Set<CreditNoteTaxEntry> Taxset = cn.getCnTaxEntryDetails() != null ? cn.getCnTaxEntryDetails() : new HashSet(0);
                        Iterator<CreditNoteTaxEntry> taxIte = Taxset.iterator();
                        while (taxIte.hasNext()) {
                            CreditNoteTaxEntry txEntry = taxIte.next();
                            double taxpercent = 0.0d;
                            String taxJeDetailId = txEntry.getTotalJED() != null ? txEntry.getTotalJED().getID() : "";
                            String taxJeDetailaccid = txEntry.getAccount() != null ? txEntry.getAccount().getID() : "";
                            obj.put("reason", txEntry.getReason() == null ? "" : txEntry.getReason().getValue());
                            if (StringUtil.equal(jeDetailId, taxJeDetailId) && StringUtil.equal(jeDetailaccid, taxJeDetailaccid) && txEntry.isIsForDetailsAccount()) {
                                if (txEntry.getTax() != null) {
//                                    KwlReturnObject perresult = accTaxObj.getTaxPercent(cn.getCompany().getCompanyID(), cn.getJournalEntry().getEntryDate(), txEntry.getTax().getID());
                                    KwlReturnObject perresult = accTaxObj.getTaxPercent(cn.getCompany().getCompanyID(), cn.getCreationDate(), txEntry.getTax().getID());
                                    taxpercent = (Double) perresult.getEntityList().get(0);
                                }
                                obj.put("taxpercent", taxpercent);
                                double txAmount = authHandler.round(txEntry.getTaxamount(), companyid);
                                obj.put("taxamount", txAmount);
                                obj.put("taxamountforaccount", txAmount);
                                if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                                    obj.put("amountwithtax", amount + txAmount);
                                }
                                
                                obj.put("srNoForRow", txEntry.getSrNoForRow());
                                if (cn.isIncludingGST()) {//ERP-26949 #2
                                    obj.put("totalamount", txEntry.getAmount());
                                } else {
                                    obj.put("totalamount", amount);
                                }
                            }
                        }
                        }
                        // ## Get Custom Field Data 
                        Map<String, Object> variableMap = new HashMap<String, Object>();
                        HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                        ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                        Detailfilter_names.add(Constants.Acc_jedetailId);
                        Detailfilter_params.add(jedetail.getID());
                        invDetailRequestParams.put("filter_names", Detailfilter_names);
                        invDetailRequestParams.put("filter_params", Detailfilter_params);
                        KwlReturnObject idcustresult = accCreditNoteDAOobj.geCreditNoteCustomData(invDetailRequestParams);
                        if (idcustresult.getEntityList().size() > 0) {
                            AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                            AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                            if (jeDetailCustom != null) {
                                boolean getlineItemDetailsflag = (paramJobj.optString("getlineItemDetailsflag", null) != null) ? Boolean.FALSE.parseBoolean((String) paramJobj.get("getlineItemDetailsflag")) : false;
                                JSONObject params = new JSONObject();
                                params.put(Constants.isExport, isExport);
                                params.put("isForReport", isForReport);
                                params.put(Constants.userdf,userDateFormat);
                                params.put(Constants.isdefaultHeaderMap,paramJobj.optBoolean(Constants.isdefaultHeaderMap, false));
                                if (!getlineItemDetailsflag) {
                                    fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                                } else {
                                    fieldDataManagercntrl.getLineLevelCustomDataWithKey(variableMap, customFieldMap, customDateFieldMap, obj, params);
                                }
                            }
                        }
                        jArr.put(obj);
                    }
                }
            }else if(cn.isIsOpeningBalenceCN()){
                JSONObject obj = new JSONObject();
                obj.put(Constants.billid, cn.getID());
                double amount = authHandler.round(cn.getCnamount(), companyid);
                amount = authHandler.round(amount, companyid);
                if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                    obj.put("account", cn.getAccount() != null ? cn.getAccount().getID() : "-");
                    obj.put("accountValue", cn.getAccount() != null ? cn.getAccount().getAccountName() : "-");
                    obj.put("desc", cn.getMemo() != null ? StringUtil.DecodeText(cn.getMemo()) : "-");
                    obj.put("amount", amount);
                    obj.put("amountwithtax", amount);//because taxamount is 0;
                } else {
                    obj.put("accountname", cn.getAccount() != null ? cn.getAccount().getAccountName() : "-");
                    obj.put("description", cn.getMemo() != null ? StringUtil.DecodeText(cn.getMemo()) : "-");
                    obj.put("totalamount", amount);
                }
                obj.put("totalamountforaccount", amount);
                obj.put("currencysymbol", cn.getCurrency() != null ? cn.getCurrency().getSymbol() : "");
                obj.put("isaccountdetails", true);
                obj.put("taxpercent", 0);
                obj.put("taxamount", 0);
                obj.put("taxamountforaccount", 0);
                obj.put("debit", "Debit");
                obj.put("srNoForRow", 1);
                obj.put("isOpeningDnCn", true);
                
                jArr.put(obj);
                
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getAccountDetailsForCreditNote : " + ex.getMessage(), ex);
        }
    }
     
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, SessionExpiredException.class, JSONException.class, AccountingException.class})
    public JSONObject deleteCreditNotesPermanentJSON(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        try {
            String linkedNotes = deleteCreditNotesPermanent(paramJobj);
            if (!StringUtil.isNullOrEmpty(linkedNotes)) {
                linkedNotes = linkedNotes.substring(0, linkedNotes.length() - 1);
            }
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedNotes)) {
                msg = messageSource.getMessage("acc.creditN.dels", null, Locale.forLanguageTag(paramJobj.optString("language")));   
            } else {
                msg = messageSource.getMessage("acc.ob.CNExcept", null,Locale.forLanguageTag(paramJobj.optString("language"))) + " " + linkedNotes+ " " + messageSource.getMessage("acc.field.deletedsuccessfully", null, Locale.forLanguageTag(paramJobj.optString("language"))) + " " + messageSource.getMessage("acc.field.usedintransactionorlockingperiod", null, Locale.forLanguageTag(paramJobj.optString("language")));   //"Sales Order has been deleted successfully;

            }
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCreditNoteServiceImplCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + (ex.getMessage() != null ? ex.getMessage() : ex.getCause().getMessage());
            Logger.getLogger(accCreditNoteServiceImplCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteServiceImplCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    public String deleteCreditNotesPermanent(JSONObject paramJobj) throws SessionExpiredException, AccountingException, ServiceException, ParseException {
        String linkedNotes = "";
        boolean isMassDelete = true; //flag for bulk delete
        try {
            JSONArray jArr = new JSONArray(paramJobj.optString(Constants.RES_data, "[{}]"));
            String companyid = paramJobj.optString(Constants.companyKey);
            /**
             * Get countryid from company
             */
            KwlReturnObject comp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) comp.getEntityList().get(0);
            int countryid = Integer.parseInt(company.getCountry().getID());
            String cnid = "", cnno = "", entryno = "";
            if (jArr.length() == 1) {
                isMassDelete = false;
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                cnid = StringUtil.DecodeText(jobj.optString("noteid"));
                CreditNote creditNote = (CreditNote) kwlCommonTablesDAOObj.getClassObject(CreditNote.class.getName(), cnid);
                cnno = jobj.getString("noteno");
                entryno = jobj.optString("entryno", "");
                Date entryDateForLock = null;
                DateFormat dateFormatForLock = authHandler.getDateOnlyFormat();
                if (jobj.has("date")) {
                    entryDateForLock = dateFormatForLock.parse(jobj.getString("date"));
                }
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("cnid", cnid);
                requestParams.put(Constants.companyKey, companyid);
                requestParams.put("cnno", cnno);
                if (!StringUtil.isNullOrEmpty(cnid)) {

                    if (entryDateForLock != null) {
                        requestParams.put("entrydate", entryDateForLock);
                        requestParams.put("df", dateFormatForLock);
                    }
                    /**
                     * Method to check the payment is Reconciled or not
                     * according to its JE id
                     */
                    requestParams.put("jeid", creditNote.getJournalEntry() != null ? creditNote.getJournalEntry().getID() : "");
                    boolean isReconciledFlag = accBankReconciliationDAOObj.isRecordReconciled(requestParams);
                    if (isReconciledFlag) {
                        linkedNotes += creditNote.getCreditNoteNumber() + ",";
                        if (isMassDelete) { //if bulk delete then only append document no
                            continue;
                        } else { //if single document delete then throw exception with proper message
                            if (!StringUtil.isNullOrEmpty(linkedNotes)) {
                                linkedNotes = linkedNotes.substring(0, linkedNotes.length() - 1);
                            }
                            throw new AccountingException(messageSource.getMessage("acc.reconcilation.Cannotdeletepayment", null, Locale.forLanguageTag(paramJobj.optString("language"))) + " " + "<b>" + linkedNotes + " " + "</b>" + messageSource.getMessage("acc.reconcilation.asitisreconciled", null, Locale.forLanguageTag(paramJobj.optString("language"))));
                        }
                    }
                    // check for is CN Linked with Payment.
                    boolean isNoteLinkedWithPayment = accCreditNoteService.isNoteLinkedWithPayment(cnid);
                    boolean isNoteLinkedWithAdvancePayment = accCreditNoteService.isNoteLinkedWithAdvancePayment(cnid);
                    if (isNoteLinkedWithPayment || isNoteLinkedWithAdvancePayment) {
                        //throw new AccountingException(messageSource.getMessage("acc.field.SelectedCreditNoteisLinkedWithPaymentsoitcannotbedelete", null, Locale.forLanguageTag(paramJobj.optString("language"))));
                        linkedNotes += creditNote.getCreditNoteNumber() + ",";
                        continue;
                    }

                    if (accCreditNoteService.isCreditNotelinkedInDebitNote(cnid, companyid) == true) {
                        linkedNotes += creditNote.getCreditNoteNumber() + ",";
                        continue;
                    }
                    // check for is CN created from Sales Return

                    if (creditNote.getSalesReturn() != null) {
                        //throw new AccountingException("Credit Note : "+creditNote.getCreditNoteNumber()+" is created from sales return so please delete Sales Return for deleting it");
                        linkedNotes += creditNote.getCreditNoteNumber() + ",";
                        continue;
                    }
                    // delete foreign gain loss JE
                    List resultJe = accCreditNoteDAOobj.getForeignGainLossJE(cnid, companyid);
                    if (resultJe.size() > 0 && resultJe.get(0) != null) {
                        Iterator itr = resultJe.iterator();
                        while (itr.hasNext()) {
                            Object object = itr.next();
                            String jeid = object != null ? object.toString() : "";
                            deleteJEArray(jeid, companyid);
                        }
                    }
                    /**
                     * Delete Credit Note Overcharge/ Undercharge Line Level Term(Tax) details
                     * ERP-36971
                     */
                    if (countryid == Constants.USA_country_id && (creditNote.getCntype() == 5 || creditNote.getCntype() == Constants.CreditNoteForOvercharge )) {
                        accCreditNoteDAOobj.deleteCreditNoteDetailTermMapAgainstDebitNote(cnid, companyid);
                    }
                    //CnType =5 is for Credit note Against Vendor for Gst
                    if (creditNote.getCntype() == 5) {
                        accJournalEntryobj.permanentDeleteCreditNoteAgainstVendorGst(cnid, companyid);
                    }
                    if (creditNote.getCntype() == Constants.CreditNoteForOvercharge) {
                        accCreditNoteDAOobj.deleteCreditNoteForOverchargeDetails(cnid, companyid);//Credit Note For Overcharge.
                    }
                    accJournalEntryobj.permanentDeleteJournalEntryDetailReval(cnid, companyid);
                    accJournalEntryobj.permanentDeleteJournalEntryReval(cnid, companyid);
                    
                    KwlReturnObject result;
                    //Delete Realised JE which was posted aginst Invoice
                    if (creditNote != null && creditNote.getRows() != null) {
                        Set<CreditNoteDetail> creditNoteDetailSet = creditNote.getRows();
                        for (CreditNoteDetail creditNoteDetail : creditNoteDetailSet) {
                            /**
                             * When we create Credit Note against Sales Invoice
                             * and if Sales invoice is reevaluated then the reval
                             * je id i.e realised je id is saved in
                             * revaljeidinvoice column.And if we created Credit
                             * Note otherwise and then it is reevaluated and then
                             * we externally link it with Sales Invoice then the
                             * realised JE id is saved in revaljeid column in
                             * cndetails table.
                             */
                            String revalJEID = "";
                            if (!StringUtil.isNullOrEmpty(creditNoteDetail.getRevalJeIdInvoice())) {
                                /**
                                 * If Invoice is reevaluated.
                                 */
                                revalJEID = creditNoteDetail.getRevalJeIdInvoice();
                                result = accJournalEntryobj.deleteJEDtails(revalJEID, companyid);// For realised JE
                                result = accJournalEntryobj.deleteJE(revalJEID, companyid);
                            }
                            if (!StringUtil.isNullOrEmpty(creditNoteDetail.getRevalJeId())) {
                                /**
                                 * If CN is reevaluated.
                                 */
                                revalJEID = creditNoteDetail.getRevalJeId();
                                result = accJournalEntryobj.deleteJEDtails(revalJEID, companyid);// For realised JE
                                result = accJournalEntryobj.deleteJE(revalJEID, companyid);
                            }
                        }
                    }
                    if (creditNote.getApprovestatuslevel() == 11) {//For pending approval CN we did not need to update invoice amount. It is only needed for Cn whose approval level is 11.
                        accCreditNoteService.updateOpeningInvoiceAmountDue(cnid, companyid);
                    }
                    updateDebitNoteAmountDue(cnid, companyid);

                    /*
                     * Before deleting CreditNoteDetail Keeping id of Invoice
                     * utlized in Credit Note
                     */
                    Set<String> invoiceIDSet = new HashSet<>();
                    if (creditNote.getApprovestatuslevel() == 11 && !creditNote.isDeleted()) {
                        for (CreditNoteDetail cnd : creditNote.getRows()) {
                            if (cnd.getInvoice() != null) {
                                invoiceIDSet.add(cnd.getInvoice().getID());
                            }
                        }
                    }
                    //Delete Rouding JEs if created against SI
                    String roundingJENo = "";
                    String roundingIDs = "";
                    String invIDs = "";
                    for (String invID : invoiceIDSet) {
                        invIDs = invID + ",";
                    }
                    if (!StringUtil.isNullOrEmpty(invIDs)) {
                        invIDs = invIDs.substring(0, invIDs.length() - 1);
                    }
                    KwlReturnObject jeResult = accJournalEntryobj.getRoundingJournalEntryByGRIds(invIDs, companyid);
                    List<JournalEntry> jeList = jeResult.getEntityList();
                    for (JournalEntry roundingJE : jeList) {
                        roundingJENo = roundingJE.getEntryNumber() + ",";
                        roundingIDs = roundingJE.getID() + ",";
                        deleteJEArray(roundingJE.getID(), companyid);
                    }

                    if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                        roundingJENo = roundingJENo.substring(0, roundingJENo.length() - 1);
                    }
                    if (!StringUtil.isNullOrEmpty(roundingIDs)) {
                        roundingIDs = roundingIDs.substring(0, roundingIDs.length() - 1);
                    }
                    accCreditNoteDAOobj.deleteLinkingInformationOfCN(requestParams);
                    /*ERP-40734
                     *To check that the Debit note is belongs to Locked accoundting period
                     *If it is belong to locked accounting period it will throw the exception
                     */
                    try {
                    accCreditNoteDAOobj.deleteCreditNotesPermanent(requestParams);
                    } catch (AccountingException ex) {
                        linkedNotes += creditNote.getCreditNoteNumber() + ",";
                        continue;
                    }
                   
                    StringBuffer journalEntryMsg = new StringBuffer();
                    if (!StringUtil.isNullOrEmpty(entryno)) {
                        journalEntryMsg.append(" along with the JE No. " + entryno);
                    }
                    
                    if (creditNote.getCntype() == Constants.CreditNoteForOvercharge) {
                        JSONObject jObj = new JSONObject();
                        jObj.put("cnid", creditNote.getID());
                        jObj.put(Constants.companyid, paramJobj.opt(Constants.companyKey));
                        accCreditNoteDAOobj.deleteCreditNoteOverchargeAmountLinking(jObj);
                    }
                    Map<String, Object> auditRequestParams = new HashMap<String, Object>();
                    auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
                    auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
                    auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));

                    auditTrailObj.insertAuditLog(AuditAction.CREDIT_NOTE_DELETED, "User " + paramJobj.optString(Constants.userfullname) + " has deleted Credit Note pemananetly " + cnno + journalEntryMsg.toString(), auditRequestParams, cnid);

                    if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                        auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_DELETED, "User " + paramJobj.optString(Constants.userfullname) + " has deleted Credit Note " + cnno + " Permanently. So Rounding JE No. " + roundingJENo + " deleted.", auditRequestParams, roundingIDs);
                    }
                }
            }
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp", null, Locale.forLanguageTag(paramJobj.optString("language"))));
        }
        return linkedNotes;
    }

    @Override
    public HashMap<String, Object> getCreditNoteMap(HttpServletRequest request) throws SessionExpiredException, UnsupportedEncodingException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put(Constants.ss, request.getParameter(Constants.ss));
        //checking filetype to print all records for csv,print & pdf
        if (StringUtil.isNullOrEmpty(request.getParameter("filetype"))) {
            requestParams.put(Constants.start, request.getParameter(Constants.start));
            requestParams.put(Constants.limit, request.getParameter(Constants.limit));
        }
        requestParams.put(CCConstants.REQ_costCenterId, request.getParameter(CCConstants.REQ_costCenterId));
        if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.REQ_startdate)) && !StringUtil.isNullOrEmpty(request.getParameter(Constants.REQ_enddate))) {
            requestParams.put(Constants.REQ_startdate, StringUtil.DecodeText(request.getParameter(Constants.REQ_startdate)));
            requestParams.put(Constants.REQ_enddate, StringUtil.DecodeText(request.getParameter(Constants.REQ_enddate)));
        }
        requestParams.put(Constants.Acc_Search_Json, request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
        requestParams.put(Constants.moduleid, request.getParameter(Constants.moduleid));
        requestParams.put(CCConstants.accid, request.getParameter(CCConstants.accid));
        requestParams.put(Constants.userdf, authHandler.getUserDateFormatter(request));
        requestParams.put("vendorid", request.getParameter("vendorid"));
        requestParams.put("customerid", request.getParameter("customerid"));
        int cntype = StringUtil.isNullOrEmpty(request.getParameter("cntype")) ? 1 : Integer.parseInt(request.getParameter("cntype"));
        requestParams.put("cntype", cntype);
        requestParams.put("noteid", request.getParameter("noteid"));
        requestParams.put("deleted", request.getParameter("deleted"));
        requestParams.put("nondeleted", request.getParameter("nondeleted"));
        requestParams.put("pendingapproval", request.getParameter("pendingapproval"));
        requestParams.put("currencyfilterfortrans", request.getParameter("currencyfilterfortrans"));
        int transactiontype = StringUtil.isNullOrEmpty(request.getParameter("transactiontype")) ? 1 : Integer.parseInt(request.getParameter("transactiontype"));
        requestParams.put("transactiontype", transactiontype);
        requestParams.put("upperLimitDate", request.getParameter("upperLimitDate") == null ? "" : request.getParameter("upperLimitDate"));
        if (request.getParameter("isReceipt") != null) {
            requestParams.put("isReceipt", request.getParameter("isReceipt"));
        }
        if (!StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))) {
            requestParams.put("browsertz", sessionHandlerImpl.getBrowserTZ(request));
        }
        return requestParams;
    }

    @Override
    public void getCNDetails(HashSet<CreditNoteDetail> cndetails, String companyId) throws ServiceException {

        KwlReturnObject result = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
        Company company = (Company) result.getEntityList().get(0);

        CreditNoteDetail row = new CreditNoteDetail();
        String CreditNoteDetailID = StringUtil.generateUUID();
        row.setID(CreditNoteDetailID);
        row.setSrno(1);
        row.setTotalDiscount(0.00);
        row.setCompany(company);
        row.setMemo("");
        cndetails.add(row);
    }

    @Override
    public void deleteJEArray(String oldjeid, String companyid) throws ServiceException, AccountingException, SessionExpiredException {
        try {      //delete old invoice
            JournalEntryDetail jed = null;
            if (!StringUtil.isNullOrEmpty(oldjeid)) {
                KwlReturnObject result = accJournalEntryobj.getJournalEntryDetail(oldjeid, companyid);
                List list = result.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    jed = (JournalEntryDetail) itr.next();
                    //Sagar - No need to revert entry from optimized table as entries are already reverted from calling main function in edit case.
                    result = accJournalEntryobj.deleteJournalEntryDetailRow(jed.getID(), companyid);
                }
                result = accJournalEntryobj.permanentDeleteJournalEntry(oldjeid, companyid);
                KwlReturnObject jedresult1 = accJournalEntryobj.deleteJECustomData(oldjeid);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    /*
     * Update Debit Note amount due in case of CN deletion.
     */
    public void updateDebitNoteAmountDue(String creditNoteId, String companyId) throws JSONException, ServiceException {
        KwlReturnObject cnObj = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), creditNoteId);
        if (!cnObj.getEntityList().isEmpty()) {
            CreditNote creditNote = (CreditNote) cnObj.getEntityList().get(0);
            Set<CreditNoteDetail> creditNoteDetails = creditNote.getRows();
            if (creditNoteDetails != null && !creditNote.isDeleted()) { // if credit note already temporary deleted then amountdue already updated. No need to update amountdue again for permament delete. 
                Iterator itr = creditNoteDetails.iterator();
                while (itr.hasNext()) {
                    CreditNoteDetail creditNoteDetail = (CreditNoteDetail) itr.next();
                    if (!StringUtil.isNullOrEmpty(creditNoteDetail.getDebitNoteId())) {
                        KwlReturnObject userResult = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), creditNoteDetail.getDebitNoteId());
                        DebitNote debitNote = (DebitNote) userResult.getEntityList().get(0);
                        if (debitNote != null && !debitNote.isNormalDN() && debitNote.isIsOpeningBalenceDN()) {
                            double amountPaid = creditNoteDetail.getDiscount().getAmountinInvCurrency();
                            HashMap<String, Object> requestParams = new HashMap();
                            requestParams.put(Constants.companyid, companyId);
                            requestParams.put(Constants.globalCurrencyKey, debitNote.getCompany().getCurrency().getCurrencyID());
                            double externalCurrencyRate = 0d;
                            externalCurrencyRate = debitNote.getExchangeRateForOpeningTransaction();
                            String fromcurrencyid = debitNote.getCurrency().getCurrencyID();
                            KwlReturnObject bAmt = null;
                            if (debitNote.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountPaid, fromcurrencyid, debitNote.getCreationDate(), externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountPaid, fromcurrencyid, debitNote.getCreationDate(), externalCurrencyRate);
                            }
                            double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);
                            double debitNoteAmountDue = debitNote.getOpeningBalanceAmountDue();
                            debitNoteAmountDue += amountPaid;

                            HashMap<String, Object> dnhm = new HashMap<>();
                            dnhm.put("dnid", debitNote.getID());
                            dnhm.put(Constants.companyKey, debitNote.getCompany().getCompanyID());
                            dnhm.put("dnamountdue", debitNoteAmountDue);
                            dnhm.put("dnamountinbase", debitNote.getOpeningBalanceBaseAmountDue() + totalBaseAmountDue);
                            if (debitNoteAmountDue != 0) {
                                dnhm.put("amountduedate", "");
                            }
                            accDebitNoteobj.updateDebitNote(dnhm);

                        } else if (debitNote != null && debitNote.isNormalDN() && !debitNote.isIsOpeningBalenceDN()) {
                            double amountPaid = creditNoteDetail.getDiscount().getAmountinInvCurrency();
                            double debitNoteAmountDue = debitNote.getDnamountdue();
                            debitNoteAmountDue += amountPaid;
                            HashMap<String, Object> dnhm = new HashMap<>();
                            dnhm.put("dnid", debitNote.getID());
                            dnhm.put(Constants.companyKey, debitNote.getCompany().getCompanyID());
                            dnhm.put("dnamountdue", debitNoteAmountDue);
                            HashMap<String, Object> requestParams = new HashMap();
                            requestParams.put(Constants.companyid, companyId);
                            requestParams.put(Constants.globalCurrencyKey, debitNote.getCompany().getCurrency().getCurrencyID());
                            String fromcurrencyid = debitNote.getCurrency().getCurrencyID();
                            JournalEntry je = debitNote.getJournalEntry();
                            if (je != null) {
                                KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, debitNoteAmountDue, fromcurrencyid, debitNote.getCreationDate(), je.getExternalCurrencyRate());
                                double invoiceamountdueinbase = (Double) baseAmount.getEntityList().get(0);
                                dnhm.put("dnamountinbase", invoiceamountdueinbase);
                            }
                            if (debitNoteAmountDue != 0) {
                                dnhm.put("amountduedate", "");
                            }
                            accDebitNoteobj.updateDebitNote(dnhm);
                        }
                    }

                }
            }
        }
    }

    public String deleteCreditNotesPermanent(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException, ParseException {
        String linkedNotes = "";
        boolean isMassDelete = true; //flag for bulk delete
        try {
            JSONArray jArr = new JSONArray(request.getParameter(Constants.RES_data));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String cnid = "", cnno = "", entryno = "";
            if (jArr.length() == 1) {
                isMassDelete = false;
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                cnid = StringUtil.DecodeText(jobj.optString("noteid"));
                CreditNote creditNote = (CreditNote) kwlCommonTablesDAOObj.getClassObject(CreditNote.class.getName(), cnid);
                cnno = jobj.getString("noteno");
                entryno = jobj.optString("entryno", "");
                Date entryDateForLock = null;
                DateFormat dateFormatForLock = authHandler.getDateOnlyFormat();
                if (jobj.has("date")) {
                    entryDateForLock = dateFormatForLock.parse(jobj.getString("date"));
                }
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("cnid", cnid);
                requestParams.put(Constants.companyKey, companyid);
                requestParams.put("cnno", cnno);
                if (!StringUtil.isNullOrEmpty(cnid)) {

                    if (entryDateForLock != null) {
                        requestParams.put("entrydate", entryDateForLock);
                        requestParams.put("df", dateFormatForLock);
                    }
                    /**
                     * Method to check the payment is Reconciled or not
                     * according to its JE id
                     */
                    requestParams.put("jeid", creditNote.getJournalEntry() != null ? creditNote.getJournalEntry().getID() : "");
                    boolean isReconciledFlag = accBankReconciliationDAOObj.isRecordReconciled(requestParams);
                    if (isReconciledFlag) {
                        linkedNotes += creditNote.getCreditNoteNumber() + ",";
                        if (isMassDelete) { //if bulk delete then only append document no
                            continue;
                        } else { //if single document delete then throw exception with proper message
                            if (!StringUtil.isNullOrEmpty(linkedNotes)) {
                                linkedNotes = linkedNotes.substring(0, linkedNotes.length() - 1);
                            }
                            throw new AccountingException(messageSource.getMessage("acc.reconcilation.Cannotdeletepayment", null, RequestContextUtils.getLocale(request)) + " " + "<b>" + linkedNotes + " " + "</b>" + messageSource.getMessage("acc.reconcilation.asitisreconciled", null, RequestContextUtils.getLocale(request)));
                        }
                    }
                    // check for is CN Linked with Payment.
                    boolean isNoteLinkedWithPayment = accCreditNoteService.isNoteLinkedWithPayment(cnid);
                    boolean isNoteLinkedWithAdvancePayment = accCreditNoteService.isNoteLinkedWithAdvancePayment(cnid);
                    if (isNoteLinkedWithPayment || isNoteLinkedWithAdvancePayment) {
                        //throw new AccountingException(messageSource.getMessage("acc.field.SelectedCreditNoteisLinkedWithPaymentsoitcannotbedelete", null, RequestContextUtils.getLocale(request)));
                        linkedNotes += creditNote.getCreditNoteNumber() + ",";
                        continue;
                    }

                    if (accCreditNoteService.isCreditNotelinkedInDebitNote(cnid, companyid) == true) {
                        linkedNotes += creditNote.getCreditNoteNumber() + ",";
                        continue;
                    }
                    // check for is CN created from Sales Return

                    if (creditNote.getSalesReturn() != null) {
                        //throw new AccountingException("Credit Note : "+creditNote.getCreditNoteNumber()+" is created from sales return so please delete Sales Return for deleting it");
                        linkedNotes += creditNote.getCreditNoteNumber() + ",";
                        continue;
                    }
                    // delete foreign gain loss JE
                    List resultJe = accCreditNoteDAOobj.getForeignGainLossJE(cnid, companyid);
                    if (resultJe.size() > 0 && resultJe.get(0) != null) {
                        Iterator itr = resultJe.iterator();
                        while (itr.hasNext()) {
                            Object object = itr.next();
                            String jeid = object != null ? object.toString() : "";
                            deleteJEArray(jeid, companyid);
                        }
                    }

                    //CnType =5 is for Credit note Against Vendor for Gst
                    if (creditNote.getCntype() == 5) {
                        accJournalEntryobj.permanentDeleteCreditNoteAgainstVendorGst(cnid, companyid);
                    }
                    if (creditNote.getCntype() == Constants.CreditNoteForOvercharge) {
                        accCreditNoteDAOobj.deleteCreditNoteForOverchargeDetails(cnid, companyid);//Credit Note For Overcharge.
                    }
                    accJournalEntryobj.permanentDeleteJournalEntryDetailReval(cnid, companyid);
                    accJournalEntryobj.permanentDeleteJournalEntryReval(cnid, companyid);
                    if (creditNote.getApprovestatuslevel() == 11) {//For pending approval CN we did not need to update invoice amount. It is only needed for Cn whose approval level is 11.
                        accCreditNoteService.updateOpeningInvoiceAmountDue(cnid, companyid);
                    }
                    updateDebitNoteAmountDue(cnid, companyid);

                    /*
                     * Before deleting CreditNoteDetail Keeping id of Invoice
                     * utlized in Credit Note
                     */
                    Set<String> invoiceIDSet = new HashSet<>();
                    if (creditNote.getApprovestatuslevel() == 11 && !creditNote.isDeleted()) {
                        for (CreditNoteDetail cnd : creditNote.getRows()) {
                            if (cnd.getInvoice() != null) {
                                invoiceIDSet.add(cnd.getInvoice().getID());
                            }
                        }
                    }
                    //Delete Rouding JEs if created against SI
                    String roundingJENo = "";
                    String roundingIDs = "";
                    String invIDs = "";
                    for (String invID : invoiceIDSet) {
                        invIDs = invID + ",";
                    }
                    if (!StringUtil.isNullOrEmpty(invIDs)) {
                        invIDs = invIDs.substring(0, invIDs.length() - 1);
                    }
                    KwlReturnObject jeResult = accJournalEntryobj.getRoundingJournalEntryByGRIds(invIDs, companyid);
                    List<JournalEntry> jeList = jeResult.getEntityList();
                    for (JournalEntry roundingJE : jeList) {
                        roundingJENo = roundingJE.getEntryNumber() + ",";
                        roundingIDs = roundingJE.getID() + ",";
                        deleteJEArray(roundingJE.getID(), companyid);
                    }

                    if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                        roundingJENo = roundingJENo.substring(0, roundingJENo.length() - 1);
                    }
                    if (!StringUtil.isNullOrEmpty(roundingIDs)) {
                        roundingIDs = roundingIDs.substring(0, roundingIDs.length() - 1);
                    }
                    accCreditNoteDAOobj.deleteLinkingInformationOfCN(requestParams);
                    accCreditNoteDAOobj.deleteCreditNotesPermanent(requestParams);
                    StringBuffer journalEntryMsg = new StringBuffer();
                    if (!StringUtil.isNullOrEmpty(entryno)) {
                        journalEntryMsg.append(" along with the JE No. " + entryno);
                    }
                    auditTrailObj.insertAuditLog(AuditAction.CREDIT_NOTE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Credit Note pemananetly " + cnno + journalEntryMsg.toString(), request, cnid);

                    if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                        auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Credit Note " + cnno + " Permanently. So Rounding JE No. " + roundingJENo + " deleted.", request, roundingIDs);
                    }
                }
            }
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)));
        }
        return linkedNotes;
    }

    @Override
    public List unlinkCreditNoteFromDebitNote(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        List result = new ArrayList();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            String cnid = request.getParameter("cnid");

            KwlReturnObject cnKWLObj = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), cnid);
            CreditNote cn = (CreditNote) cnKWLObj.getEntityList().get(0);

            String cnnumber = cn.getCreditNoteNumber();

            String linkedDebitNoteIds = "";
            String linkedDebitNotenos = "";
            List<String> linkedDetailDn = new ArrayList();
            JSONArray linkJSONArray = request.getParameter("linkdetails") != null ? new JSONArray(request.getParameter("linkdetails")) : new JSONArray();
            for (int k = 0; k < linkJSONArray.length(); k++) {//creating a hash map with payment and their linked invoice
                JSONObject jSONObject = linkJSONArray.getJSONObject(k);
                String linkId = jSONObject.optString("linkdetailid", "");
                linkedDetailDn.add(linkId);
            }

            boolean allInvoicesUnlinked = false;
            if (linkJSONArray.length() == 0) {
                allInvoicesUnlinked = true;
            }
            double cnExternalCurrencyRate = 1d;
            boolean isopeningBalanceCN = cn.isIsOpeningBalenceCN();
            Date cnCreationDate = null;
            cnCreationDate = cn.getCreationDate();
            if (isopeningBalanceCN) {
                cnExternalCurrencyRate = cn.getExchangeRateForOpeningTransaction();
            } else {
                cnExternalCurrencyRate = cn.getJournalEntry().getExternalCurrencyRate();
            }

            String linkedDetailIDs = "";
            for (String dnID : linkedDetailDn) {
                linkedDetailIDs = linkedDetailIDs.concat("'").concat(dnID).concat("',");
            }
            if (!StringUtil.isNullOrEmpty(linkedDetailIDs.toString())) {
                linkedDetailIDs = linkedDetailIDs.substring(0, linkedDetailIDs.length() - 1);
            }

            // get list of debit note which are un-linked
            KwlReturnObject cndetailResult = accCreditNoteDAOobj.getDeletedLinkedDebitNotes(cn, linkedDetailIDs, companyid);
            List<CreditNoteDetail> details = cndetailResult.getEntityList();
            Double totalAmountUsedByInvoices = 0.0;
            for (CreditNoteDetail creditNoteDetail : details) {

                cnKWLObj = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), creditNoteDetail.getDebitNoteId());
                DebitNote debitNote = (DebitNote) cnKWLObj.getEntityList().get(0);


                if (debitNote != null && !debitNote.isNormalDN() && debitNote.isIsOpeningBalenceDN()) {
                    double amountPaid = creditNoteDetail.getDiscount().getAmountinInvCurrency();
                    totalAmountUsedByInvoices += creditNoteDetail.getDiscount().getDiscount();
                    HashMap<String, Object> requestParams = new HashMap();
                    requestParams.put(Constants.companyid, companyid);
                    requestParams.put(Constants.globalCurrencyKey, debitNote.getCompany().getCurrency().getCurrencyID());
                    double externalCurrencyRate = 0d;
                    externalCurrencyRate = debitNote.getExchangeRateForOpeningTransaction();
                    String fromcurrencyid = debitNote.getCurrency().getCurrencyID();
                    KwlReturnObject bAmt = null;
                    if (debitNote.isConversionRateFromCurrencyToBase()) {
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountPaid, fromcurrencyid, debitNote.getCreationDate(), externalCurrencyRate);
                    } else {
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountPaid, fromcurrencyid, debitNote.getCreationDate(), externalCurrencyRate);
                    }
                    double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);
                    double debitNoteAmountDue = debitNote.getOpeningBalanceAmountDue();
                    debitNoteAmountDue += amountPaid;

                    HashMap<String, Object> dnhm = new HashMap<>();
                    dnhm.put("dnid", debitNote.getID());
                    dnhm.put(Constants.companyKey, company.getCompanyID());
                    dnhm.put("openingBalanceAmountDue", debitNoteAmountDue);
                    dnhm.put(Constants.openingBalanceBaseAmountDue, debitNote.getOriginalOpeningBalanceBaseAmount() + totalBaseAmountDue);
                    dnhm.put("dnamountdue", debitNote.getDnamountdue() + amountPaid);
                    if (debitNoteAmountDue != 0) {
                        dnhm.put("amountduedate", "");
                    }
                    accDebitNoteobj.updateDebitNote(dnhm);
                    linkedDebitNotenos += debitNote.getDebitNoteNumber() + ",";
                } else if (debitNote != null && debitNote.isNormalDN() && !debitNote.isIsOpeningBalenceDN()) {
                    double amountPaid = creditNoteDetail.getDiscount().getAmountinInvCurrency();
                    totalAmountUsedByInvoices += creditNoteDetail.getDiscount().getDiscount();
                    double debitNoteAmountDue = debitNote.getDnamountdue();
                    debitNoteAmountDue += amountPaid;
                    HashMap<String, Object> dnhm = new HashMap<>();
                    dnhm.put("dnid", debitNote.getID());
                    dnhm.put(Constants.companyKey, company.getCompanyID());
                    dnhm.put("dnamountdue", debitNoteAmountDue);
                    if (debitNoteAmountDue != 0) {
                        dnhm.put("amountduedate", "");
                    }
                    accDebitNoteobj.updateDebitNote(dnhm);
                    linkedDebitNotenos += debitNote.getDebitNoteNumber() + ",";
                }

                // check if Forex/Gain Loss JE generated. If yes then need to delete JE too
                if (creditNoteDetail.getLinkedGainLossJE() != null && !creditNoteDetail.getLinkedGainLossJE().isEmpty()) {
                    deleteJEArray(creditNoteDetail.getLinkedGainLossJE(), companyid);
                }

                //Deletng Relasiesd JE for Unlinked Invoice
                if (creditNoteDetail != null && !StringUtil.isNullOrEmpty(creditNoteDetail.getRevalJeId())) {
                    accJournalEntryobj.deleteJEDtails(creditNoteDetail.getRevalJeId(), companyid);
                    accJournalEntryobj.deleteJE(creditNoteDetail.getRevalJeId(), companyid);
                }
                if (creditNoteDetail != null && !StringUtil.isNullOrEmpty(creditNoteDetail.getRevalJeIdInvoice())) {
                    accJournalEntryobj.deleteJEDtails(creditNoteDetail.getRevalJeIdInvoice(), companyid);
                    accJournalEntryobj.deleteJE(creditNoteDetail.getRevalJeIdInvoice(), companyid);
                }
            }

            if (!StringUtil.isNullOrEmpty(linkedDebitNotenos)) {
                linkedDebitNotenos = linkedDebitNotenos.substring(0, linkedDebitNotenos.length() - 1);
            }

            // Update credit note details
            HashMap<String, Object> credithm = new HashMap();
            Double cnAmountDue = cn.getOpeningBalanceAmountDue();
            cnAmountDue = cnAmountDue + totalAmountUsedByInvoices;
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put(Constants.companyid, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(Constants.globalCurrencyKey, sessionHandlerImpl.getCurrencyID(request));
            String fromcurrencyid = cn.getCurrency().getCurrencyID();
            KwlReturnObject bAmt = null;
            if (isopeningBalanceCN && cn.isConversionRateFromCurrencyToBase()) {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, cnAmountDue, fromcurrencyid, cnCreationDate, cnExternalCurrencyRate);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, cnAmountDue, fromcurrencyid, cnCreationDate, cnExternalCurrencyRate);
            }
            double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);

            credithm.put("cnid", cn.getID());
            credithm.put("cnamountdue", cnAmountDue);
            credithm.put("openingBalanceAmountDue", cnAmountDue);
            credithm.put(Constants.openingBalanceBaseAmountDue, totalBaseAmountDue);
            credithm.put("openflag", (cnAmountDue) <= 0 ? false : true);


            accCreditNoteDAOobj.deleteSelectedLinkedDebitNotes(cn.getID(), linkedDetailIDs, companyid, "");

            // If all invoices linked to CN are un-linked, created one entry for cn details 
            HashSet<CreditNoteDetail> cndetails = new HashSet<CreditNoteDetail>();
            if (allInvoicesUnlinked) {
                getCNDetails(cndetails, companyid);
                for (CreditNoteDetail cndetail : cndetails) {
                    cndetail.setCreditNote(cn);
                }
                credithm.put("cnid", cn.getID());
                credithm.put("cndetails", cndetails);
            }
            /*
             * Deleting Linking information of CN while unlinking transaction
             */
            accCreditNoteDAOobj.deleteLinkingInformationOfCNAgainstDN(credithm);
            accCreditNoteDAOobj.updateCreditNote(credithm);
            if (!StringUtil.isNullOrEmpty(linkedDebitNotenos)) {
                auditTrailObj.insertAuditLog(AuditAction.LINKEDRECEIPT, "User " + sessionHandlerImpl.getUserFullName(request) + " has Unlinked Credit Note " + cnnumber + " from Debit Note(s) " + linkedDebitNotenos, request, linkedDebitNoteIds);
            }

        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteServiceImplCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @Override
    public List unlinkCreditNoteFromTransactions(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        List result = new ArrayList();
        try {
            //Unlink Invoices
            result = unlinkCreditNoteFromInvoice(request);
            //Unlink Debit Notes
            result = unlinkCreditNoteFromDebitNote(request);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteServiceImplCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public void deleteTemporaryInvoicesEntries(JSONArray jSONArrayAgainstInvoice, String companyid) {
        try {
            for (int i = 0; i < jSONArrayAgainstInvoice.length(); i++) {
                JSONObject invoiceJobj = jSONArrayAgainstInvoice.getJSONObject(i);
                String invoiceId = invoiceJobj.getString("documentid");
                accPaymentDAOobj.deleteUsedInvoiceOrCheque(invoiceId, companyid);
            }
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteServiceImplCMN.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public List unlinkCreditNoteFromInvoice(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        List result = new ArrayList();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String gcurrenyid = sessionHandlerImpl.getCurrencyID(request);
            String cnid = request.getParameter("cnid");

            KwlReturnObject cnKWLObj = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), cnid);
            CreditNote cn = (CreditNote) cnKWLObj.getEntityList().get(0);

            String cnnumber = cn.getCreditNoteNumber();
            String linkedInvoiceids = "";
            String linkedInvoicenos = "";

            //Get details of those invoices which are still linked to CN
            List<String> linkedDetailInvoice = new ArrayList();
            JSONArray linkJSONArray = request.getParameter("linkdetails") != null ? new JSONArray(request.getParameter("linkdetails")) : new JSONArray();
            for (int k = 0; k < linkJSONArray.length(); k++) {
                JSONObject jSONObject = linkJSONArray.getJSONObject(k);
                String linkId = jSONObject.optString("linkdetailid", "");
                linkedDetailInvoice.add(linkId);
            }

            String linkedDetailIDs = "";
            for (String invID : linkedDetailInvoice) {
                linkedDetailIDs = linkedDetailIDs.concat("'").concat(invID).concat("',");
            }
            if (!StringUtil.isNullOrEmpty(linkedDetailIDs.toString())) {
                linkedDetailIDs = linkedDetailIDs.substring(0, linkedDetailIDs.length() - 1);
            }

            boolean allInvoicesUnlinked = false;
            if (linkJSONArray.length() == 0) {
                allInvoicesUnlinked = true;
            }
            double cnExternalCurrencyRate = 1d;
            boolean isopeningBalanceCN = cn.isIsOpeningBalenceCN();
            Date cnCreationDate = null;
            cnCreationDate = cn.getCreationDate();
            if (isopeningBalanceCN) {
                cnExternalCurrencyRate = cn.getExchangeRateForOpeningTransaction();
            } else {
                cnExternalCurrencyRate = cn.getJournalEntry().getExternalCurrencyRate();
            }

            // get list of invoices which are un-linked
            KwlReturnObject cndetailResult = accCreditNoteDAOobj.getDeletedLinkedInvoices(cn, linkedDetailIDs, companyid);
            List<CreditNoteDetail> details = cndetailResult.getEntityList();
            Double totalAmountUsedByInvoices = 0.0;

            Set<String> invoiceIDSet = new HashSet<>();
            //update invocie amount due after unlinking
            for (CreditNoteDetail creditNoteDetail : details) {
                if (creditNoteDetail.getInvoice() != null && !creditNoteDetail.getInvoice().isNormalInvoice() && creditNoteDetail.getInvoice().isIsOpeningBalenceInvoice()) {
                    double amountPaid = creditNoteDetail.getDiscount().getAmountinInvCurrency();
                    totalAmountUsedByInvoices += creditNoteDetail.getDiscount().getDiscount();
                    Invoice invObj = creditNoteDetail.getInvoice();
                    HashMap<String, Object> requestParams = new HashMap();
                    requestParams.put(Constants.companyid, companyid);
                    requestParams.put(Constants.globalCurrencyKey, invObj.getCompany().getCurrency().getCurrencyID());
                    double externalCurrencyRate = 0d;
                    externalCurrencyRate = invObj.getExchangeRateForOpeningTransaction();
                    String fromcurrencyid = invObj.getCurrency().getCurrencyID();
                    KwlReturnObject bAmt = null;
                    if (invObj.isConversionRateFromCurrencyToBase()) {
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountPaid, fromcurrencyid, invObj.getCreationDate(), externalCurrencyRate);
                    } else {
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountPaid, fromcurrencyid, invObj.getCreationDate(), externalCurrencyRate);
                    }
                    double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);
                    double invoiceAmountDue = invObj.getOpeningBalanceAmountDue();
                    invoiceAmountDue += amountPaid;
                    JSONObject invjson = new JSONObject();
                    invjson.put("invoiceid", invObj.getID());
                    invjson.put(Constants.companyKey, companyid);
                    invjson.put("openingBalanceAmountDue", invoiceAmountDue);
                    invjson.put(Constants.openingBalanceBaseAmountDue, invObj.getOpeningBalanceBaseAmountDue() + totalBaseAmountDue);
                    if (invoiceAmountDue != 0) {
                        invjson.put("amountduedate", "");
                    }
                    accInvoiceDAOObj.updateInvoice(invjson, null);
                    linkedInvoicenos += invObj.getInvoiceNumber() + ",";
                    invoiceIDSet.add(invObj.getID());
                } else if (creditNoteDetail.getInvoice() != null && creditNoteDetail.getInvoice().isNormalInvoice() && !creditNoteDetail.getInvoice().isIsOpeningBalenceInvoice()) {
                    Invoice invoice = creditNoteDetail.getInvoice();
                    //Amount of invoice  in invoice currency
                    double amountPaid = creditNoteDetail.getDiscount().getAmountinInvCurrency();

                    //Amount of invoice  in base currency
                    double amountPaidInBase = 0;
                    HashMap<String, Object> requestParams = new HashMap();
                    requestParams.put(Constants.companyid, companyid);
                    requestParams.put("gcurrencyid", gcurrenyid);
//                KwlReturnObject grAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountPaid, invoice.getCurrency().getCurrencyID(), invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                    KwlReturnObject grAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountPaid, invoice.getCurrency().getCurrencyID(), invoice.getCreationDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                    if (grAmtInBaseResult != null) {
                        amountPaidInBase += authHandler.round((Double) grAmtInBaseResult.getEntityList().get(0), companyid);
                    }
                    double invoiceAmountDue = invoice.getInvoiceamountdue() + amountPaid;
                    double invoiceAmountDueInbase = invoice.getInvoiceAmountDueInBase() + amountPaidInBase;

                    JSONObject invjson = new JSONObject();
                    invjson.put("invoiceid", invoice.getID());
                    invjson.put(Constants.companyKey, companyid);
                    invjson.put(Constants.invoiceamountdue, invoiceAmountDue);
                    invjson.put(Constants.invoiceamountdueinbase, invoiceAmountDueInbase);
                    if (invoiceAmountDue != 0) {
                        invjson.put("amountduedate", "");
                    }
                    accInvoiceDAOObj.updateInvoice(invjson, null);

                    totalAmountUsedByInvoices += creditNoteDetail.getDiscount().getDiscount();
                    linkedInvoicenos += invoice.getInvoiceNumber() + ",";
                    invoiceIDSet.add(invoice.getID());
                }

                // check if Forex/Gain Loss JE generated. If yes then need to delete JE too
                if (creditNoteDetail.getLinkedGainLossJE() != null && !creditNoteDetail.getLinkedGainLossJE().isEmpty()) {
                    deleteJEArray(creditNoteDetail.getLinkedGainLossJE(), companyid);
                }

                //Deletng Relasiesd JE for Unlinked Invoice
                if (creditNoteDetail != null && !StringUtil.isNullOrEmpty(creditNoteDetail.getRevalJeId())) {
                    accJournalEntryobj.deleteJEDtails(creditNoteDetail.getRevalJeId(), companyid);
                    accJournalEntryobj.deleteJE(creditNoteDetail.getRevalJeId(), companyid);
                }
                if (creditNoteDetail != null && !StringUtil.isNullOrEmpty(creditNoteDetail.getRevalJeIdInvoice())) {
                    accJournalEntryobj.deleteJEDtails(creditNoteDetail.getRevalJeIdInvoice(), companyid);
                    accJournalEntryobj.deleteJE(creditNoteDetail.getRevalJeIdInvoice(), companyid);
                }
            }

            if (!StringUtil.isNullOrEmpty(linkedInvoicenos)) {
                linkedInvoicenos = linkedInvoicenos.substring(0, linkedInvoicenos.length() - 1);
            }

            //Delete Rouding JEs if created against SI
            String roundingJENo = "";
            String roundingIDs = "";
            String invIDs = "";
            for (String invID : invoiceIDSet) {
                invIDs = invID + ",";
            }
            if (!StringUtil.isNullOrEmpty(invIDs)) {
                invIDs = invIDs.substring(0, invIDs.length() - 1);
            }
            KwlReturnObject jeResult = accJournalEntryobj.getRoundingJournalEntryByGRIds(invIDs, companyid);
            List<JournalEntry> jeList = jeResult.getEntityList();
            for (JournalEntry roundingJE : jeList) {
                roundingJENo = roundingJE.getEntryNumber() + ",";
                roundingIDs = roundingJE.getID() + ",";
                deleteJEArray(roundingJE.getID(), companyid);
            }

            if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                roundingJENo = roundingJENo.substring(0, roundingJENo.length() - 1);
            }
            if (!StringUtil.isNullOrEmpty(roundingIDs)) {
                roundingIDs = roundingIDs.substring(0, roundingIDs.length() - 1);
            }
            // Update credit note details
            HashMap<String, Object> credithm = new HashMap();
            Double cnAmountDue = cn.getOpeningBalanceAmountDue();
            cnAmountDue = cnAmountDue + totalAmountUsedByInvoices;
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put(Constants.companyid, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(Constants.globalCurrencyKey, sessionHandlerImpl.getCurrencyID(request));
            String fromcurrencyid = cn.getCurrency().getCurrencyID();
            KwlReturnObject bAmt = null;
            if (isopeningBalanceCN && cn.isConversionRateFromCurrencyToBase()) {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, cnAmountDue, fromcurrencyid, cnCreationDate, cnExternalCurrencyRate);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, cnAmountDue, fromcurrencyid, cnCreationDate, cnExternalCurrencyRate);
            }
            double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);

            credithm.put("cnid", cn.getID());
            credithm.put("cnamountdue", cnAmountDue);
            credithm.put("openingBalanceAmountDue", cnAmountDue);
            credithm.put(Constants.openingBalanceBaseAmountDue, totalBaseAmountDue);
            credithm.put("openflag", (cnAmountDue) <= 0 ? false : true);


            accCreditNoteDAOobj.deleteSelectedLinkedInvoices(cn.getID(), linkedDetailIDs, companyid, "");

            // If all invoices linked to CN are un-linked, created one entry for cn details 
            HashSet<CreditNoteDetail> cndetails = new HashSet<CreditNoteDetail>();
            if (allInvoicesUnlinked) {
                getCNDetails(cndetails, companyid);
                for (CreditNoteDetail cndetail : cndetails) {
                    cndetail.setCreditNote(cn);
                }
                credithm.put("cnid", cn.getID());
                credithm.put("cndetails", cndetails);
            }
            /*
             * Deleting Linking information of CN while unlinking transaction
             */
            accCreditNoteDAOobj.deleteLinkingInformationOfCN(credithm);
            accCreditNoteDAOobj.updateCreditNote(credithm);
            if (!StringUtil.isNullOrEmpty(linkedInvoicenos)) {
                auditTrailObj.insertAuditLog(AuditAction.LINKEDRECEIPT, "User " + sessionHandlerImpl.getUserFullName(request) + " has Unlinked Credit Note " + cnnumber + " from Sales Invoice(s) " + linkedInvoicenos, request, linkedInvoiceids);
            }
            if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " " + messageSource.getMessage("acc.roundingje.unlinkedcn", null, RequestContextUtils.getLocale(request)) + " " + cnnumber + " " + messageSource.getMessage("acc.roundingje.fromsalesinvoice", null, RequestContextUtils.getLocale(request)) + " " + linkedInvoicenos + "." + messageSource.getMessage("acc.roundingje.roundingje", null, RequestContextUtils.getLocale(request)) + " " + roundingJENo + messageSource.getMessage("acc.roundingje.roundingjedelted", null, RequestContextUtils.getLocale(request)) + ".", request, roundingIDs);
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @Override
    public JSONObject getCreditNoteLinkedDocumnets(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONArray jArray = new JSONArray();
        DateFormat df = authHandler.getOnlyDateFormat(request);
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String crNoteId = request.getParameter("noteId");
            KwlReturnObject result = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), crNoteId);
            CreditNote cn = (CreditNote) result.getEntityList().get(0);

            if (cn != null) {
                Set<CreditNoteDetail> rows = cn.getRows();
                Set<CreditNoteDetail> Creditnotedetails = new TreeSet<CreditNoteDetail>(new SortCreditNoteDetail());
                Creditnotedetails.addAll(rows);

                for (CreditNoteDetail detail : Creditnotedetails) {
                    JSONObject obj = new JSONObject();
                    if (detail.getInvoice() != null) {
                        obj.put("linkdetailid", detail.getID());

                        /*
                         * Checked Null for Invoice linked with Credit Note at
                         * the time of edit Credit Note which was created
                         * without selection any Invoice
                         */

                        obj.put("billid", detail.getInvoice() != null ? detail.getInvoice().getID() : "");
                        obj.put("billno", detail.getInvoice() != null ? detail.getInvoice().getInvoiceNumber() : "");
                        obj.put("documentid", detail.getInvoice() != null ? detail.getInvoice().getID() : "");
                        obj.put("documentno", detail.getInvoice() != null ? detail.getInvoice().getInvoiceNumber() : "");

                        obj.put("type", "Invoice");
                        if (detail.getInvoice() != null) {
                            obj.put("taxamount", detail.getInvoice().getTaxEntry() == null ? 0 : detail.getInvoice().getTaxEntry().getAmount());
                        }


                        Discount disc = detail.getDiscount();
                        double exchangeratefortransaction = detail.getExchangeRateForTransaction();
                        double invoiceReturnedAmt = 0d;
                        if (disc != null) {
                            obj.put("linkamount", authHandler.round(disc.getDiscountValue(), companyid));
                            invoiceReturnedAmt = disc.getAmountinInvCurrency();
                        } else {
                            obj.put("linkamount", 0);
                        }
                        obj.put("linkingdate", detail.getInvoiceLinkDate() != null ? df.format(detail.getInvoiceLinkDate()) : "");
                        List ll = null;
                        double amountDueOriginal = 0;
                        if (detail.getInvoice() != null && detail.getInvoice().isIsOpeningBalenceInvoice() && !detail.getInvoice().isNormalInvoice()) {
                            ll = new ArrayList();
                            ll.add(detail.getInvoice() != null ? detail.getInvoice().getOpeningBalanceAmountDue() : 0);
                            ll.add(0.0);
                            ll.add(0.0);
                            ll.add(detail.getInvoice() != null ? detail.getInvoice().getOpeningBalanceAmountDue() : 0);
                            amountDueOriginal = detail.getInvoice() != null ? detail.getInvoice().getOriginalOpeningBalanceAmount() : 0;
                            obj.put("amount", detail.getInvoice() != null ? detail.getInvoice().getOriginalOpeningBalanceAmount() : 0);
                        } else {
                            if (Constants.InvoiceAmountDueFlag && detail.getInvoice() != null) {
                                ll = accInvoiceCommon.getInvoiceDiscountAmountInfo(requestParams, detail.getInvoice());
                            } else {
                                if (detail.getInvoice() != null) {
                                    ll = accInvoiceCommon.getAmountDue_Discount(requestParams, detail.getInvoice());
                                }
                            }
                            amountDueOriginal = detail.getInvoice() != null ? detail.getInvoice().getCustomerEntry().getAmount() : 0;
                            obj.put("amount", detail.getInvoice() != null ? detail.getInvoice().getCustomerEntry().getAmount() : 0);
                        }

                        double amountdue = detail.getInvoice() != null ? (Double) ll.get(3) + invoiceReturnedAmt : 0;// added invoiceReturnedAmt to show original value which was at time of creation.
                        amountdue = amountdue * exchangeratefortransaction;
                        obj.put("amountdue", authHandler.round(amountdue, companyid));
                        obj.put("amountDueOriginal", authHandler.round(amountDueOriginal, companyid));
                        obj.put("exchangeratefortransaction", exchangeratefortransaction);
                        obj.put("currencysymbol", detail.getInvoice() != null ? detail.getInvoice().getCurrency().getSymbol() : "");
                        obj.put("currencysymboltransaction", detail.getInvoice() != null ? detail.getInvoice().getCurrency().getSymbol() : "");
                        obj.put("currencysymbolpayment", cn.getCurrency().getSymbol());
                        if (detail.getInvoice() != null) {
                            obj.put("invoicedate", df.format(detail.getInvoice().getCreationDate()));
                            //Credit Note Rec Name is date .
                            obj.put("date", df.format(detail.getInvoice().getCreationDate()));
                        }
                        jArray.put(obj);
                    } else if (!StringUtil.isNullOrEmpty(detail.getDebitNoteId())) {

                        result = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), detail.getDebitNoteId());
                        DebitNote debitNote = (DebitNote) result.getEntityList().get(0);

                        obj.put("linkdetailid", detail.getID());

                        /*
                         * Checked Null for Invoice linked with Credit Note at
                         * the time of edit Credit Note which was created
                         * without selection any Invoice
                         */

                        obj.put("billid", debitNote != null ? debitNote.getID() : "");
                        obj.put("billno", debitNote != null ? debitNote.getDebitNoteNumber() : "");

                        obj.put("documentid", debitNote != null ? debitNote.getID() : "");
                        obj.put("documentno", debitNote != null ? debitNote.getDebitNoteNumber() : "");
                        obj.put("documentType", 3);// 3 - Debit Note
                        obj.put("type", "Debit Note");
                        Set<DebitNoteTaxEntry> dntaxEnteries = debitNote.getDnTaxEntryDetails();
                        double taxAmount = 0.0;
                        for (DebitNoteTaxEntry debitNoteTaxEntry : dntaxEnteries) {
                            taxAmount += debitNoteTaxEntry.getAmount();
                        }

                        obj.put("taxamount", taxAmount);
                        Discount disc = detail.getDiscount();
                        double exchangeratefortransaction = detail.getExchangeRateForTransaction();
                        double invoiceReturnedAmt = 0d;
                        if (disc != null) {
                            obj.put("linkamount", authHandler.round(disc.getDiscountValue(), companyid));
                            invoiceReturnedAmt = disc.getAmountinInvCurrency();
                        } else {
                            obj.put("linkamount", 0);
                        }
                        obj.put("linkingdate", detail.getInvoiceLinkDate() != null ? df.format(detail.getInvoiceLinkDate()) : "");
                        double amountDueOriginal = 0;
                        amountDueOriginal = debitNote.isOtherwise() ? debitNote.getDnamount() : debitNote.getVendorEntry().getAmount();
                        obj.put("amount", debitNote.isOtherwise() ? debitNote.getDnamount() : debitNote.getVendorEntry().getAmount());

                        double amountdue = debitNote.getDnamountdue() + invoiceReturnedAmt;// added invoiceReturnedAmt to show original value which was at time of creation.
                        amountdue = amountdue * exchangeratefortransaction;
                        obj.put("amountdue", authHandler.round(amountdue, companyid));
                        obj.put("amountDueOriginal", authHandler.round(amountDueOriginal, companyid));
                        obj.put("exchangeratefortransaction", exchangeratefortransaction);
                        obj.put("currencysymbol", debitNote != null ? debitNote.getCurrency().getSymbol() : "");
                        obj.put("currencysymboltransaction", debitNote != null ? debitNote.getCurrency().getSymbol() : "");
                        obj.put("currencysymbolpayment", cn.getCurrency().getSymbol());
                        if (debitNote != null) {
                            obj.put("invoicedate", df.format(debitNote.getCreationDate()));
                            //Credit Note Rec Name is date .
                            obj.put("date", df.format(debitNote.getCreationDate()));
                        }
                        jArray.put(obj);
                    }
                }
            }
            jobj.put(Constants.RES_data, jArray);
        } catch (JSONException ex) {
            Logger.getLogger(accCreditNoteControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return jobj;
    }

    public List linkCreditNoteToDebitNote(HttpServletRequest request, JSONArray noteArray, String creditNoteId, Boolean isInsertAudTrail, Map<String, Object> counterMap) throws ServiceException, SessionExpiredException {
        List result = new ArrayList();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String cnid = "";
            int counter = 0;
            if (!StringUtil.isNullOrEmpty(creditNoteId)) {
                cnid = creditNoteId;
            } else {
                cnid = request.getParameter("cnid");
            }
//            //Commented because of ERP-36411
//            String linkingdate = (String) request.getParameter("linkingdate");
            DateFormat dateformat = authHandler.getDateOnlyFormat();
//            if (!StringUtil.isNullOrEmpty(linkingdate)) {
//                try {
//                    maxLinkingDate = dateformat.parse(linkingdate);
//                } catch (ParseException ex) {
//                    Logger.getLogger(accCreditNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
            String entryNumber = request.getParameter("number");
            for (int k = 0; k < noteArray.length(); k++) {
                JSONObject jobj = noteArray.getJSONObject(k);

                if (StringUtil.isNullOrEmpty(jobj.getString("documentid"))) {
                    continue;
                }

                double typeFigure = 0d;
                int typeOfFigure = 1;
                double usedcnamount = jobj.optDouble("linkamount", 0.0d);
                if (usedcnamount == 0) {
                    continue;
                }

                if (!StringUtil.isNullOrEmpty(jobj.optString("typeFigure"))) {
                    typeFigure = jobj.optDouble("typeFigure", 0.0);
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("typeOfFigure"))) {
                    typeOfFigure = jobj.optInt("typeOfFigure", 1);
                }
                KwlReturnObject grresult = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), jobj.getString("documentid"));
                DebitNote debitNote = (DebitNote) grresult.getEntityList().get(0);

                KwlReturnObject cnObj = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), cnid);
                CreditNote creditNote = (CreditNote) cnObj.getEntityList().get(0);

                grresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) grresult.getEntityList().get(0);

                Set<CreditNoteDetail> newcndetails = new HashSet<CreditNoteDetail>();
                double cnamountdue = creditNote.getCnamountdue();
                if (!creditNote.isOpenflag() || cnamountdue <= 0) {
                    throw new AccountingException(messageSource.getMessage("acc.field.CreditNotehasbeenalreadyutilized.", null, RequestContextUtils.getLocale(request)));
                }

                double amountReceived = usedcnamount;           //amount of DN 
                double amountReceivedConverted = usedcnamount;
                double adjustedRate = 1;

                if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(debitNote.getCurrency().getCurrencyID()) && !debitNote.getCurrency().getCurrencyID().equals(creditNote.getCurrency().getCurrencyID())) {
                    adjustedRate = Double.parseDouble(jobj.get("exchangeratefortransaction").toString());
                    amountReceivedConverted = amountReceived / adjustedRate;
                    amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
                } else {
                    amountReceivedConverted = authHandler.round(amountReceived, companyid);
                }
                Date maxDate = null;
                Date maxLinkingDate = null;
                String linkingdate = null;
                if (jobj.has("linkingdate") && jobj.get("linkingdate") != null) {
                    linkingdate = (String) jobj.get("linkingdate");
                    if (!StringUtil.isNullOrEmpty(linkingdate)) {
                        try {
                            maxLinkingDate = dateformat.parse(linkingdate);
                        } catch (ParseException ex) {
                            Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                CreditNoteDetail cndetailObj = new CreditNoteDetail();
                if (creditNote.getCntype() != 3 && creditNote.isOpenflag() && creditNote.getCnamount() == creditNote.getCnamountdue()) {//If CN is used for first time.
                    Set<CreditNoteDetail> cndetails = (Set<CreditNoteDetail>) creditNote.getRows();
                    Iterator itr = cndetails.iterator();
                    while (itr.hasNext()) {
                        cndetailObj = (CreditNoteDetail) itr.next();

                        if (debitNote != null) {
                            cndetailObj.setDebitNoteId(debitNote.getID());
                            /*
                             * code to save linking date of CN and Invoice. This
                             * linking date used while calculating due amount of
                             * CN in Aged/SOA report
                             */
                            Date linkingDate = new Date();
                            Date invDate = debitNote.getCreationDate();
                            Date cnDate = creditNote.getCreationDate();
                            if (maxLinkingDate != null) {
                                maxDate = maxLinkingDate;
                            } else {
                                List<Date> datelist = new ArrayList<Date>();
                                datelist.add(linkingDate);
                                datelist.add(invDate);
                                datelist.add(cnDate);
                                Collections.sort(datelist); //Sort the Date object & get the dates in ASC order. Pick-up the last record as Max Date
                                maxDate = datelist.get(datelist.size() - 1);
                            }
                            cndetailObj.setInvoiceLinkDate(maxDate);
                        }
                        //change
                        double invoiceOriginalAmt = 0d;
                        if (!debitNote.isNormalDN() && debitNote.isIsOpeningBalenceDN()) {
                            invoiceOriginalAmt = debitNote.getDnamountdue();
                        } else {
                            invoiceOriginalAmt = debitNote.getVendorEntry().getAmount();
                        }
                        JSONObject discjson = new JSONObject();
                        discjson.put("discount", usedcnamount);         //amount in DN currency
                        discjson.put("amountinInvCurrency", amountReceivedConverted);
                        discjson.put("inpercent", false);
                        discjson.put("originalamount", invoiceOriginalAmt);//(Double) bAmt.getEntityList().get(0));
                        discjson.put(Constants.companyKey, companyid);
                        discjson.put("typeOfFigure", typeOfFigure);
                        discjson.put("typeFigure", typeFigure);
                        KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                        Discount discount = (Discount) dscresult.getEntityList().get(0);
                        cndetailObj.setDiscount(discount);
                        cndetailObj.setExchangeRateForTransaction(adjustedRate);
                        newcndetails.add(cndetailObj);
                    }
                } else {
                    Set<CreditNoteDetail> cndetails = (Set<CreditNoteDetail>) creditNote.getRows();
                    Iterator itr = cndetails.iterator();
                    int i = 0;
                    while (itr.hasNext()) {
                        cndetailObj = (CreditNoteDetail) itr.next();
                        newcndetails.add(cndetailObj);
                        i++;
                    }

                    cndetailObj = new CreditNoteDetail();

                    if (debitNote != null) {
                        cndetailObj.setDebitNoteId(debitNote.getID());
                        /*
                         * code to save linking date of CN and Invoice. This
                         * linking date used while calculating due amount of CN
                         * in Aged/SOA report
                         */
                        Date linkingDate = new Date();
                        Date invDate = debitNote.getCreationDate();
                        Date cnDate = creditNote.getCreationDate();
                        if (maxLinkingDate != null) {
                            maxDate = maxLinkingDate;
                        } else {
                            List<Date> datelist = new ArrayList<Date>();
                            datelist.add(linkingDate);
                            datelist.add(invDate);
                            datelist.add(cnDate);
                            Collections.sort(datelist); //Sort the Date object & get the dates in ASC order. Pick-up the last record as Max Date
                            maxDate = datelist.get(datelist.size() - 1);
                        }
                        cndetailObj.setInvoiceLinkDate(maxDate);
                    }

                    cndetailObj.setSrno(i + 1);
                    cndetailObj.setTotalDiscount(0.00);
                    cndetailObj.setCompany(company);
                    cndetailObj.setMemo("");
                    cndetailObj.setCreditNote(creditNote);
                    cndetailObj.setID(UUID.randomUUID().toString());

                    double invoiceOriginalAmt = 0d;
                    if (debitNote.isNormalDN()) {
                        invoiceOriginalAmt = debitNote.getVendorEntry().getAmount();
                    } else {// for only opening balance Invoices
                        invoiceOriginalAmt = debitNote.getDnamountdue();
                    }

                    JSONObject discjson = new JSONObject();
                    discjson.put("discount", usedcnamount);
                    discjson.put("amountinInvCurrency", amountReceivedConverted);        //amount in DN currency
                    discjson.put("inpercent", false);
                    discjson.put("originalamount", invoiceOriginalAmt);//(Double) bAmt.getEntityList().get(0));
                    discjson.put(Constants.companyKey, companyid);
                    discjson.put("typeOfFigure", typeOfFigure);
                    discjson.put("typeFigure", typeFigure);
                    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                    Discount discount = (Discount) dscresult.getEntityList().get(0);
                    cndetailObj.setDiscount(discount);
                    cndetailObj.setExchangeRateForTransaction(adjustedRate);
                    newcndetails.add(cndetailObj);
                }

                double amountDue = cnamountdue - usedcnamount;
                HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                requestParams.put(Constants.companyid, sessionHandlerImpl.getCompanyid(request));
                requestParams.put(Constants.globalCurrencyKey, sessionHandlerImpl.getCurrencyID(request));
                double externalCurrencyRate = 1d;
                boolean isopeningBalanceCN = creditNote.isIsOpeningBalenceCN();
                Date cnCreationDate = null;
                cnCreationDate = creditNote.getCreationDate();
                if (!creditNote.isNormalCN() && creditNote.isIsOpeningBalenceCN()) {
                    externalCurrencyRate = creditNote.getExchangeRateForOpeningTransaction();
                } else {
                    externalCurrencyRate = creditNote.getJournalEntry().getExternalCurrencyRate();
                }
                String fromcurrencyid = creditNote.getCurrency().getCurrencyID();
                KwlReturnObject bAmt = null;
                if (isopeningBalanceCN && creditNote.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                }
                double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);
                HashMap<String, Object> credithm = new HashMap<String, Object>();
                credithm.put("cndetails", newcndetails);
                credithm.put("cnid", creditNote.getID());
                credithm.put("cnamountdue", amountDue);
                credithm.put("openingBalanceAmountDue", amountDue);
                credithm.put(Constants.openingBalanceBaseAmountDue, totalBaseAmountDue);
                credithm.put("openflag", (cnamountdue - usedcnamount) <= 0 ? false : true);

                if (request.getAttribute("entrynumber") != null) {
                    credithm.put("entrynumber", request.getAttribute("entrynumber"));
                }
                if (request.getAttribute("autogenerated") != null) {
                    credithm.put("autogenerated", request.getAttribute("autogenerated"));

                }
                if (request.getAttribute("seqformat") != null) {
                    credithm.put(Constants.SEQFORMAT, request.getAttribute("seqformat"));

                }
                if (request.getAttribute("seqnumber") != null) {
                    credithm.put(Constants.SEQNUMBER, request.getAttribute("seqnumber"));

                }
                if (request.getAttribute(Constants.DATEPREFIX) != null) {
                    credithm.put(Constants.DATEPREFIX, request.getAttribute(Constants.DATEPREFIX));
                }
                if (request.getAttribute(Constants.DATESUFFIX) != null) {
                    credithm.put(Constants.DATESUFFIX, request.getAttribute(Constants.DATESUFFIX));
                }
                KwlReturnObject result1 = accCreditNoteDAOobj.updateCreditNote(credithm);

                // Update Invoice base amount due. We have to consider Invoice currency rate to calculate.
                externalCurrencyRate = 1d;
                boolean isopeningBalanceINV = debitNote.isIsOpeningBalenceDN();
                Date noteCreationDate = null;
                noteCreationDate = creditNote.getCreationDate();
                externalCurrencyRate = isopeningBalanceCN ? creditNote.getExchangeRateForOpeningTransaction() : creditNote.getJournalEntry().getExternalCurrencyRate();
                fromcurrencyid = creditNote.getCurrency().getCurrencyID();
                if (isopeningBalanceINV && debitNote.isConversionRateFromCurrencyToBase()) {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, usedcnamount, fromcurrencyid, noteCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, usedcnamount, fromcurrencyid, noteCreationDate, externalCurrencyRate);
                }
                totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);

                /*
                 * Store the date on which the amount due has been set to zero
                 */

                KwlReturnObject noteResult = updateDebitNoteAmountDueAndReturnResult(debitNote, company, amountReceivedConverted, totalBaseAmountDue);
                if (isInsertAudTrail) {
                    auditTrailObj.insertAuditLog(AuditAction.CREDIT_NOTE_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has linked Credit Note " + creditNote.getCreditNoteNumber() + " with Debit Note " + debitNote.getDebitNoteNumber() + ".", request, creditNote.getID());
                }

                /*
                 * Start gains/loss calculation Calculate Gains/Loss if Invoice
                 * exchange rate changed at the time of linking with CN
                 */
                KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
                if (preferences.getForeignexchange() == null) {
                    throw new AccountingException(messageSource.getMessage("acc.common.forex", null, RequestContextUtils.getLocale(request)));
                }
                if (isopeningBalanceCN && creditNote.isConversionRateFromCurrencyToBase()) {
                    externalCurrencyRate = 1 / externalCurrencyRate;
                    externalCurrencyRate = externalCurrencyRate;
                }
                Map<String, Object> mapForForexGainLoss = new HashMap<>();
                mapForForexGainLoss.put("cn", creditNote);
                mapForForexGainLoss.put("dn", debitNote);
                mapForForexGainLoss.put("basecurreny", sessionHandlerImpl.getCurrencyID(request));
                mapForForexGainLoss.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
                mapForForexGainLoss.put("creationdate", creditNote.getCreationDate());
                mapForForexGainLoss.put("exchangeratefortransaction", Double.parseDouble(jobj.optString("exchangeratefortransaction", "1")));
                mapForForexGainLoss.put("recinvamount", usedcnamount);
                mapForForexGainLoss.put("externalcurrencyrate", externalCurrencyRate);
                mapForForexGainLoss.put(Constants.RES_DATEFORMAT, authHandler.getDateOnlyFormat());
                double amountDiff = checkFxGainLossOnLinkDebitNote(mapForForexGainLoss);
                if (amountDiff != 0 && preferences.getForeignexchange() != null && Math.abs(amountDiff) >= 0.000001) {//Math.abs(amountDiff) < .0000001 Added this because in case of revaluation the diff in the rates was less than .0000001 and the amount calculation was going in Exachange rate account. 
                    boolean rateDecreased = false;
                    if (amountDiff < 0) {
                        rateDecreased = true;
                    }
                    JournalEntry journalEntry = null;
                    Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                    String jeentryNumber = null;
                    boolean jeautogenflag = false;
                    String jeSeqFormatId = "";
                    String datePrefix = "";
                    String dateAfterPrefix = "";
                    String dateSuffix = "";
                    Date entryDate = null;
                    if (maxLinkingDate != null) {
                        entryDate = new Date(maxLinkingDate.getTime());
                    } else {
                        entryDate = new Date();
                    }
                    synchronized (this) {
                        HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                        JEFormatParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                        JEFormatParams.put("modulename", "autojournalentry");
                        JEFormatParams.put(Constants.companyKey, companyid);
                        JEFormatParams.put("isdefaultFormat", true);

                        KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                        SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                        int sequence = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
                        String nextAutoNUmber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                        datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                        dateAfterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);//Date After Prefix Part
                        dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                        sequence = sequence + counter;
                        String number = "" + sequence;
                        String action = "" + (sequence - counter);
                        nextAutoNUmber.replaceAll(action, number);
                        jeentryNumber = nextAutoNUmber.replaceAll(action, number);  //next auto generated number
                        jeSeqFormatId = format.getID();
                        jeautogenflag = true;

                        jeDataMap.put("entrynumber", jeentryNumber);
                        jeDataMap.put("autogenerated", jeautogenflag);
                        jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                        jeDataMap.put(Constants.SEQNUMBER, number);
                        jeDataMap.put(Constants.DATEPREFIX, datePrefix);
                        jeDataMap.put(Constants.DATEAFTERPREFIX, dateAfterPrefix);
                        jeDataMap.put(Constants.DATESUFFIX, dateSuffix);
                    }
                    jeDataMap.put("entrydate", dateformat.parse(linkingdate));
                    jeDataMap.put(Constants.companyKey, companyid);
                    jeDataMap.put(Constants.memo, "Exchange Gains/Loss posted against Credit Note '" + creditNote.getCreditNoteNumber() + "' linked to Debit Note '" + debitNote.getDebitNoteNumber() + "'");
                    jeDataMap.put("currencyid", creditNote.getCurrency().getCurrencyID());
                    jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
                    jeDataMap.put("isexchangegainslossje", true);
                    jeDataMap.put("transactionId", creditNote.getID());
                    jeDataMap.put("transactionModuleid", Constants.Acc_Credit_Note_ModuleId);
                    journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
                    accJournalEntryobj.saveJournalEntryByObject(journalEntry);

                    boolean isDebit = rateDecreased ? true : false;
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", 1);
                    jedjson.put(Constants.companyKey, companyid);
                    jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                    jedjson.put("accountid", preferences.getForeignexchange().getID());
                    jedjson.put("debit", isDebit);
                    jedjson.put("jeid", journalEntry.getID());
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    Set<JournalEntryDetail> detail = new HashSet();
                    detail.add(jed);

                    jedjson = new JSONObject();
                    jedjson.put("srno", 2);
                    jedjson.put(Constants.companyKey, companyid);
                    jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                    jedjson.put("accountid", debitNote.getAccount().getID());
                    jedjson.put("debit", !isDebit);
                    jedjson.put("jeid", journalEntry.getID());
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    detail.add(jed);
                    journalEntry.setDetails(detail);
                    accJournalEntryobj.saveJournalEntryDetailsSet(detail);

                    // Save link JE iformation in DN details
                    cndetailObj.setLinkedGainLossJE(journalEntry.getID());
                    newcndetails.add(cndetailObj);
                    credithm.put("cndetails", newcndetails);
                    KwlReturnObject result2 = accCreditNoteDAOobj.updateCreditNote(credithm);
                    counter++;
                }
                // End Gains/Loss Calculation

                //JE For Receipt which is of Opening Type
                double exchangeRateforTransaction = jobj.optDouble("exchangeratefortransaction", 1.0);
                if (counterMap.containsKey("counter")) {
                    counter = (Integer) counterMap.get("counter");
                }
                counterMap.put("counter", counter);
                if (creditNote != null && (creditNote.isIsOpeningBalenceCN() || creditNote.isOtherwise())) {
                    String basecurrency = sessionHandlerImpl.getCurrencyID(request);
                    double finalAmountReval = ReevalJournalEntryForCreditNote(request, creditNote, amountReceived, exchangeRateforTransaction);
                    if (finalAmountReval != 0) {
                        /**
                         * added transactionID and transactionModuleID to
                         * Realised JE.
                         */
                        counterMap.put("transactionModuleid", creditNote.isIsOpeningBalenceCN() ? (creditNote.iscNForCustomer() ? Constants.Acc_opening_Customer_CreditNote : Constants.Acc_opening_Vendor_CreditNote) : Constants.Acc_Credit_Note_ModuleId);
                        counterMap.put("transactionId", creditNote.getID());
                        String revaljeid = PostJEFORReevaluation(request, -(finalAmountReval), companyid, preferences, basecurrency, cndetailObj.getRevalJeId(), counterMap);
                        cndetailObj.setRevalJeId(revaljeid);
                    }
                }
                //JE For Debit which is Linked to Receipt
                if (debitNote != null) {
                    double finalAmountReval = ReevalJournalEntryForDebitNote(request, debitNote, amountReceived, exchangeRateforTransaction);
                    if (finalAmountReval != 0) {
                        String basecurrency = sessionHandlerImpl.getCurrencyID(request);
                        /**
                         * added transactionID and transactionModuleID to
                         * Realised JE.
                         */
                        counterMap.put("transactionModuleid", debitNote.isIsOpeningBalenceDN() ? (debitNote.isdNForVendor() ? Constants.Acc_opening_Vendor_DebitNote : Constants.Acc_opening_Customer_DebitNote) : Constants.Acc_Debit_Note_ModuleId);
                        counterMap.put("transactionId", debitNote.getID());
                        String revaljeid = PostJEFORReevaluation(request, finalAmountReval, companyid, preferences, basecurrency, cndetailObj.getRevalJeIdInvoice(), counterMap);
                        cndetailObj.setRevalJeIdInvoice(revaljeid);

                    }
                }
                if (debitNote != null) {

                    HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                    requestParamsLinking.put("linkeddocid", debitNote.getID());
                    requestParamsLinking.put("docid", cnid);
                    requestParamsLinking.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                    requestParamsLinking.put("linkeddocno", debitNote.getDebitNoteNumber());
                    requestParamsLinking.put("sourceflag", 1);
                    KwlReturnObject result3 = accCreditNoteDAOobj.saveCreditNoteLinking(requestParamsLinking);
                    /*
                     * saving linking informaion of Purchase Invoice while
                     * linking with DebitNote
                     */
                    requestParamsLinking.put("linkeddocid", cnid);
                    requestParamsLinking.put("docid", debitNote.getID());
                    requestParamsLinking.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                    requestParamsLinking.put("linkeddocno", entryNumber);
                    requestParamsLinking.put("sourceflag", 0);
                    result1 = accDebitNoteobj.saveDebitNoteLinking(requestParamsLinking);
                }
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return result;
    }

    public double getForexGainLossForCreditNote(Map<String, Object> requestParams) throws ServiceException, SessionExpiredException {
        double amount = 0, actualAmount = 0;
        try {
            String basecurrency = requestParams.get("basecurreny").toString();
            String companyid = requestParams.get(Constants.companyKey).toString();
            DateFormat dateformat = (DateFormat) requestParams.get(Constants.RES_DATEFORMAT);
            HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put(Constants.companyKey, companyid);
            GlobalParams.put(Constants.globalCurrencyKey, basecurrency);
            GlobalParams.put(Constants.RES_DATEFORMAT, dateformat);
            Date creationDate = (Date) requestParams.get("creationdate");
            CreditNote cn = (CreditNote) requestParams.get("cn");
            String currencyid = cn.getCurrency().getCurrencyID();
            Invoice gr = (Invoice) requestParams.get("invoice");
            double exchangeratefortransaction = (double) requestParams.get("exchangeratefortransaction");
            double recinvamount = (double) requestParams.get("recinvamount");
            double ratio = 0;
            double newrate = 0.0;
            boolean revalFlag = false;

            boolean isopeningBalancePayment = cn.isIsOpeningBalenceCN();
            boolean isConversionRateFromCurrencyToBase = cn.isConversionRateFromCurrencyToBase();
            double externalCurrencyRate = (double) requestParams.get("externalcurrencyrate");
            double exchangeRate = 0d;
            Date goodsReceiptCreationDate = null;
            if (gr.isNormalInvoice()) {
                exchangeRate = gr.getJournalEntry().getExternalCurrencyRate();
            } else {
                exchangeRate = gr.getExchangeRateForOpeningTransaction();
                if (gr.isConversionRateFromCurrencyToBase()) {
                    exchangeRate = 1 / exchangeRate;
                }
            }
            goodsReceiptCreationDate = gr.getCreationDate();


            HashMap<String, Object> invoiceId = new HashMap<String, Object>();
            invoiceId.put("invoiceid", gr.getID());
            invoiceId.put(Constants.companyKey, companyid);
            KwlReturnObject result = null;
            result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
            RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
            if (history != null) {
                exchangeRate = history.getEvalrate();
                newrate = exchangeratefortransaction;
                revalFlag = true;
            }
            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            String currid = currency.getCurrencyID();
            if (gr.getCurrency() != null) {
                currid = gr.getCurrency().getCurrencyID();
            }

            KwlReturnObject bAmt = null;
            if (currid.equalsIgnoreCase(currencyid)) {
                double paymentExternalCurrencyRate = externalCurrencyRate;
                if (exchangeRate != paymentExternalCurrencyRate && !revalFlag) {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate, paymentExternalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                }
            } else {
                double paymentExternalCurrencyRate = externalCurrencyRate;
                if (exchangeRate != paymentExternalCurrencyRate && !revalFlag) {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate, paymentExternalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                }
            }
            double oldrate = (Double) bAmt.getEntityList().get(0);
            if (exchangeratefortransaction != oldrate && exchangeratefortransaction != 0.0 && Math.abs(exchangeratefortransaction - oldrate) >= 0.000001) {
                newrate = exchangeratefortransaction;
                ratio = oldrate - newrate;
                amount = (recinvamount - (recinvamount / newrate) * oldrate) / newrate;
                KwlReturnObject bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, amount, currencyid, creationDate, newrate);
                actualAmount += (Double) bAmtActual.getEntityList().get(0);
            } else {
                if (currid.equalsIgnoreCase(currencyid)) {
                    double paymentExternalCurrencyRate = externalCurrencyRate;
                    if (exchangeRate != paymentExternalCurrencyRate && !revalFlag) {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate, paymentExternalCurrencyRate);
                    } else {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                    }
                } else {
                    double paymentExternalCurrencyRate = externalCurrencyRate;
                    if (exchangeRate != paymentExternalCurrencyRate && !revalFlag) {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate, paymentExternalCurrencyRate);
                    } else {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                    }
                }
                if (!revalFlag) {
                    newrate = (Double) bAmt.getEntityList().get(0);
                }
                if (Math.abs(exchangeratefortransaction - oldrate) >= 0.000001) {
                    ratio = oldrate - newrate;
                }
                amount = recinvamount * ratio;
                KwlReturnObject bAmtActual = null;
                if (isopeningBalancePayment && isConversionRateFromCurrencyToBase) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, amount, currencyid, creationDate, externalCurrencyRate);
                } else {
                    bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, amount, currencyid, creationDate, externalCurrencyRate);
                }
                actualAmount += (Double) bAmtActual.getEntityList().get(0);
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("getForexGainLossFordebitNote : " + ex.getMessage(), ex);
        }
        return (actualAmount);
    }

    public double checkFxGainLossOnLinkDebitNote(Map<String, Object> requestParams) throws ServiceException, SessionExpiredException {
        double amount = 0, actualAmount = 0;
        try {
            String basecurrency = requestParams.get("basecurreny").toString();
            String companyid = requestParams.get(Constants.companyKey).toString();
            DateFormat dateformat = (DateFormat) requestParams.get(Constants.RES_DATEFORMAT);
            HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put(Constants.companyKey, companyid);
            GlobalParams.put(Constants.globalCurrencyKey, basecurrency);
            GlobalParams.put(Constants.RES_DATEFORMAT, dateformat);
            Date creationDate = (Date) requestParams.get("creationdate");
            CreditNote cn = (CreditNote) requestParams.get("cn");
            String currencyid = cn.getCurrency().getCurrencyID();
            DebitNote debitNote = (DebitNote) requestParams.get("dn");
            double exchangeratefortransaction = (double) requestParams.get("exchangeratefortransaction");
            double recinvamount = (double) requestParams.get("recinvamount");
            double ratio = 0;
            double newrate = 0.0;
            boolean revalFlag = false;

            boolean isopeningBalancePayment = cn.isIsOpeningBalenceCN();
            boolean isConversionRateFromCurrencyToBase = cn.isConversionRateFromCurrencyToBase();
            double externalCurrencyRate = (double) requestParams.get("externalcurrencyrate");
            double exchangeRate = 0d;
            Date goodsReceiptCreationDate = null;
            if (debitNote.isNormalDN()) {
                exchangeRate = debitNote.getJournalEntry().getExternalCurrencyRate();
            } else {
                exchangeRate = debitNote.getExchangeRateForOpeningTransaction();
                if (debitNote.isConversionRateFromCurrencyToBase()) {
                    exchangeRate = 1 / exchangeRate;
                }
            }
            goodsReceiptCreationDate = debitNote.getCreationDate();

            HashMap<String, Object> invoiceId = new HashMap<String, Object>();
            invoiceId.put("invoiceid", debitNote.getID());
            invoiceId.put(Constants.companyKey, companyid);
            KwlReturnObject result = null;
            result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
            RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
            if (history != null) {
                exchangeRate = history.getEvalrate();
                newrate = exchangeratefortransaction;
                revalFlag = true;
            }
            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            String currid = currency.getCurrencyID();
            if (debitNote.getCurrency() != null) {
                currid = debitNote.getCurrency().getCurrencyID();
            }

            KwlReturnObject bAmt = null;
            if (currid.equalsIgnoreCase(currencyid)) {
                double paymentExternalCurrencyRate = externalCurrencyRate;
                if (exchangeRate != paymentExternalCurrencyRate && !revalFlag) {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate, paymentExternalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                }
            } else {
                double paymentExternalCurrencyRate = externalCurrencyRate;
                if (exchangeRate != paymentExternalCurrencyRate && !revalFlag) {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate, paymentExternalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                }
            }
            double oldrate = (Double) bAmt.getEntityList().get(0);
            if (exchangeratefortransaction != oldrate && exchangeratefortransaction != 0.0 && Math.abs(exchangeratefortransaction - oldrate) >= 0.000001) {
                newrate = exchangeratefortransaction;
                ratio = oldrate - newrate;
                amount = (recinvamount - (recinvamount / newrate) * oldrate) / newrate;
                KwlReturnObject bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, amount, currencyid, creationDate, newrate);
                actualAmount += (Double) bAmtActual.getEntityList().get(0);
            } else {
                if (currid.equalsIgnoreCase(currencyid)) {
                    double paymentExternalCurrencyRate = externalCurrencyRate;
                    if (exchangeRate != paymentExternalCurrencyRate && !revalFlag) {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate, paymentExternalCurrencyRate);
                    } else {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                    }
                } else {
                    double paymentExternalCurrencyRate = externalCurrencyRate;
                    if (exchangeRate != paymentExternalCurrencyRate && !revalFlag) {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate, paymentExternalCurrencyRate);
                    } else {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                    }
                }
                if (!revalFlag) {
                    newrate = (Double) bAmt.getEntityList().get(0);
                }
                if (Math.abs(exchangeratefortransaction - oldrate) >= 0.000001) {
                    ratio = oldrate - newrate;
                }
                amount = recinvamount * ratio;
                KwlReturnObject bAmtActual = null;
                if (isopeningBalancePayment && isConversionRateFromCurrencyToBase) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, amount, currencyid, creationDate, externalCurrencyRate);
                } else {
                    bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, amount, currencyid, creationDate, externalCurrencyRate);
                }
                actualAmount += (Double) bAmtActual.getEntityList().get(0);
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("getForexGainLossFordebitNote : " + ex.getMessage(), ex);
        }
        return (actualAmount);
    }

    public double checkFxGainLossOnLinkInvoices(Invoice gr, double newInvoiceExchageRate, double paymentExchangeRate, double recinvamount, String paymentCurrency, String baseCurrency, String companyid) throws ServiceException {
        double amount = 0;
        HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
        GlobalParams.put(Constants.companyKey, companyid);
        GlobalParams.put(Constants.globalCurrencyKey, baseCurrency);
        double goodsReceiptExchangeRate = 0d;
        Date goodsReceiptCreationDate = null;
        boolean isopeningBalanceInvoice = gr.isIsOpeningBalenceInvoice();
        if (gr.isNormalInvoice()) {
            goodsReceiptExchangeRate = gr.getJournalEntry().getExternalCurrencyRate();
        } else {
            if (gr.isConversionRateFromCurrencyToBase()) {
                goodsReceiptExchangeRate = 1 / gr.getExchangeRateForOpeningTransaction();
                goodsReceiptExchangeRate = authHandler.round(goodsReceiptExchangeRate, companyid);
            } else {
                goodsReceiptExchangeRate = gr.getExchangeRateForOpeningTransaction();
            }
        }
        goodsReceiptCreationDate = gr.getCreationDate();

        boolean revalFlag = false;

        Map<String, Object> invoiceId = new HashMap<>();
        invoiceId.put("invoiceid", gr.getID());
        invoiceId.put(Constants.companyKey, companyid);
        KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
        RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
        if (history != null) {
            goodsReceiptExchangeRate = history.getEvalrate();
            revalFlag = true;
        }
        String currid = gr.getCurrency().getCurrencyID();
        KwlReturnObject bAmt = null;
        if (currid.equalsIgnoreCase(paymentCurrency)) {
            if (history == null && isopeningBalanceInvoice && gr.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, paymentCurrency, goodsReceiptCreationDate, goodsReceiptExchangeRate);
            } else {
                double paymentExternalCurrencyRate = paymentExchangeRate;
                if (goodsReceiptExchangeRate != paymentExternalCurrencyRate && !revalFlag) {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, paymentCurrency, goodsReceiptCreationDate, goodsReceiptExchangeRate, paymentExternalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(GlobalParams, 1.0, currid, paymentCurrency, goodsReceiptCreationDate, goodsReceiptExchangeRate);
                }
            }
        } else {
            if (history == null && isopeningBalanceInvoice && gr.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, paymentCurrency, goodsReceiptCreationDate, goodsReceiptExchangeRate);
            } else {
                double paymentExternalCurrencyRate = paymentExchangeRate;
                if (goodsReceiptExchangeRate != paymentExternalCurrencyRate && !revalFlag) {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, paymentCurrency, goodsReceiptCreationDate, goodsReceiptExchangeRate, paymentExternalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(GlobalParams, 1.0, currid, paymentCurrency, goodsReceiptCreationDate, goodsReceiptExchangeRate);
                }
            }
        }
        double oldrate = (Double) bAmt.getEntityList().get(0);
        double newrate = 0.0;
        double ratio = 0;
        if (newInvoiceExchageRate != oldrate && newInvoiceExchageRate != 0.0
                && Math.abs(newInvoiceExchageRate - oldrate) >= 0.000001) {
            newrate = newInvoiceExchageRate;
            ratio = oldrate - newrate;
            amount = (recinvamount - (recinvamount / newrate) * oldrate);
        }
        return amount;
    }


    /*
     * Revalaution Entery for Invoices
     */
    public double ReevalJournalEntryForInvoice(HttpServletRequest request, Invoice invoice, double linkInvoiceAmount, double exchangeratefortransaction) throws SessionExpiredException, ServiceException, AccountingException {
        double finalAmountReval = 0;
        try {
            String basecurrency = sessionHandlerImpl.getCurrencyID(request);
            double ratio = 0;
            double amountReval = 0;
            String revalId = null;
            Date tranDate = null;
            double exchangeRate = 0.0;
            double exchangeRateReval = 0.0;
            double amountdue = linkInvoiceAmount;
            HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            GlobalParams.put(Constants.globalCurrencyKey, basecurrency);
            GlobalParams.put(Constants.RES_DATEFORMAT, authHandler.getDateOnlyFormat(request));
            Date creationDate = invoice.getCreationDate();
            boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
            tranDate = invoice.getCreationDate();
            if (!invoice.isNormalInvoice()) {
                exchangeRate = invoice.getExchangeRateForOpeningTransaction();
                exchangeRateReval = exchangeRate;
            } else {
                exchangeRate = invoice.getJournalEntry().getExternalCurrencyRate();
                exchangeRateReval = exchangeRate;
            }
            HashMap<String, Object> invoiceId = new HashMap<>();
            invoiceId.put("invoiceid", invoice.getID());
            invoiceId.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
            //Checking the document entery in revalution history if any for current rate
            KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
            RevaluationHistory revalueationHistory = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
            if (revalueationHistory != null) {
                exchangeRateReval = revalueationHistory.getEvalrate();
            }

            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            String currid = currency.getCurrencyID();
            if (invoice.getCurrency() != null) {
                currid = invoice.getCurrency().getCurrencyID();
            }
            KwlReturnObject bAmt = null;
            if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            }

            double oldrate = (Double) bAmt.getEntityList().get(0);
            if (revalueationHistory == null && isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            }

            double newrate = (Double) bAmt.getEntityList().get(0);
            ratio = oldrate - newrate;
            if (Math.abs(exchangeratefortransaction - newrate) <= 0.000001) {
                exchangeratefortransaction = newrate;
            }
            double amountdueNew = amountdue / exchangeratefortransaction;
            amountdueNew = Math.round(amountdueNew * 1000) / 1000d;
            amountReval = ratio * amountdueNew;
            finalAmountReval = finalAmountReval + amountReval;
        } catch (SessionExpiredException | ServiceException e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return finalAmountReval;
    }

    public double ReevalJournalEntryForDebitNote(HttpServletRequest request, DebitNote debitNote, double linkInvoiceAmount, double exchangeratefortransaction) throws SessionExpiredException, ServiceException, AccountingException {
        double finalAmountReval = 0;
        try {
            String basecurrency = sessionHandlerImpl.getCurrencyID(request);
            double ratio = 0;
            double amountReval = 0;
            Date tranDate = null;
            double exchangeRate = 0.0;
            double exchangeRateReval = 0.0;
            double amountdue = linkInvoiceAmount;
            Map<String, Object> GlobalParams = new HashMap<>();
            GlobalParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            GlobalParams.put(Constants.globalCurrencyKey, basecurrency);
            GlobalParams.put(Constants.RES_DATEFORMAT, authHandler.getDateOnlyFormat());
            Date creationDate = debitNote.getCreationDate();
            boolean isopeningBalanceInvoice = debitNote.isIsOpeningBalenceDN();
            tranDate = debitNote.getCreationDate();
            if (!debitNote.isNormalDN()) {
                exchangeRate = debitNote.getExchangeRateForOpeningTransaction();
                exchangeRateReval = exchangeRate;
            } else {
                exchangeRate = debitNote.getJournalEntry().getExternalCurrencyRate();
                exchangeRateReval = exchangeRate;
//            tranDate = debitNote.getJournalEntry().getEntryDate();
            }
            HashMap<String, Object> invoiceId = new HashMap<>();
            invoiceId.put("invoiceid", debitNote.getID());
            invoiceId.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
            //Checking the document entery in revalution history if any for current rate
            KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
            RevaluationHistory revalueationHistory = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
            if (revalueationHistory != null) {
                exchangeRateReval = revalueationHistory.getEvalrate();
            }

            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            String currid = currency.getCurrencyID();
            if (debitNote.getCurrency() != null) {
                currid = debitNote.getCurrency().getCurrencyID();
            }
            KwlReturnObject bAmt = null;
            if (isopeningBalanceInvoice && debitNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            }

            double oldrate = (Double) bAmt.getEntityList().get(0);
            if (revalueationHistory == null && isopeningBalanceInvoice && debitNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            }

            double newrate = (Double) bAmt.getEntityList().get(0);
            ratio = oldrate - newrate;
            if (Math.abs(exchangeratefortransaction - newrate) <= 0.000001) {
                exchangeratefortransaction = newrate;
            }
            double amountdueNew = amountdue / exchangeratefortransaction;
            amountdueNew = Math.round(amountdueNew * 1000) / 1000d;
            amountReval = ratio * amountdueNew;
            finalAmountReval = finalAmountReval + amountReval;
        } catch (SessionExpiredException | ServiceException e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return finalAmountReval;
    }

    /*
     * Revalaution Entery for Credit Note
     */
    public double ReevalJournalEntryForCreditNote(HttpServletRequest request, CreditNote creditNote, double linkInvoiceAmount, double exchangeratefortransaction) throws SessionExpiredException, ServiceException, AccountingException {

        double finalAmountReval = 0;
        try {
            String basecurrency = sessionHandlerImpl.getCurrencyID(request);
            double ratio = 0;
            double amountReval = 0;
            String revalId = null;
            Date tranDate = null;
            double exchangeRate = 0.0;
            double exchangeRateReval = 0.0;
            double amountdue = linkInvoiceAmount;
            Map<String, Object> GlobalParams = new HashMap<>();
            GlobalParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            GlobalParams.put(Constants.globalCurrencyKey, basecurrency);
            GlobalParams.put(Constants.RES_DATEFORMAT, authHandler.getDateOnlyFormat(request));
            Date creationDate = creditNote.getCreationDate();
            boolean isopeningBalanceInvoice = creditNote.isIsOpeningBalenceCN();
            tranDate = creditNote.getCreationDate();
            if (!creditNote.isNormalCN()) {
                exchangeRate = creditNote.getExchangeRateForOpeningTransaction();
                exchangeRateReval = exchangeRate;
            } else {
                exchangeRate = creditNote.getJournalEntry().getExternalCurrencyRate();
                exchangeRateReval = exchangeRate;
            }
            HashMap<String, Object> invoiceId = new HashMap<>();
            invoiceId.put("invoiceid", creditNote.getID());
            invoiceId.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
            //Checking the document entery in revalution history if any for current rate
            KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
            RevaluationHistory revalueationHistory = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
            if (revalueationHistory != null) {
                exchangeRateReval = revalueationHistory.getEvalrate();
            }

            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            String currid = currency.getCurrencyID();
            if (creditNote.getCurrency() != null) {
                currid = creditNote.getCurrency().getCurrencyID();
            }
            KwlReturnObject bAmt = null;
            if (isopeningBalanceInvoice && creditNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            }

            double oldrate = (Double) bAmt.getEntityList().get(0);
            if (revalueationHistory == null && isopeningBalanceInvoice && creditNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            }

            double newrate = (Double) bAmt.getEntityList().get(0);
            ratio = oldrate - newrate;
            if (Math.abs(exchangeratefortransaction - newrate) <= 0.000001) {
                exchangeratefortransaction = newrate;
            }
            double amountdueNew = amountdue / exchangeratefortransaction;
            amountdueNew = Math.round(amountdueNew * 1000) / 1000d;
            amountReval = ratio * amountdueNew;
            finalAmountReval = finalAmountReval + amountReval;
        } catch (SessionExpiredException | ServiceException e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return finalAmountReval;
    }

    public String PostJEFORReevaluation(HttpServletRequest request, double finalAmountReval, String companyid, CompanyAccountPreferences preferences, String basecurrency, String oldRevaluationJE, Map<String, Object> dataMap) {
        String jeid = "";
        try {
            String jeentryNumber = "";
            String jeSeqFormatId = "";
            String jeIntegerPart = "";
            String datePrefix = "";
            String dateAfterPrefix = "";
            String dateSuffix = "";
            DateFormat df = authHandler.getDateOnlyFormat();
            /**
             * added Link Date to Realised JE. while link Otherwise CN/DN to
             * Reevaluated Invoice.
             */
            String creationDate = !StringUtil.isNullObject(request.getParameter("linkingdate")) ? request.getParameter("linkingdate") : request.getParameter("creationdate");
            Date jeCreationDate = StringUtil.isNullOrEmpty(creationDate) ? new Date() : df.parse(creationDate);
            boolean jeautogenflag = false;
            int counter = (Integer) dataMap.get("counter");
            synchronized (this) {
                HashMap<String, Object> JEFormatParams = new HashMap<>();
                JEFormatParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                JEFormatParams.put(Constants.companyKey, companyid);
                JEFormatParams.put("isdefaultFormat", true);

                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, jeCreationDate);
                jeautogenflag = true;
                if (StringUtil.isNullOrEmpty(oldRevaluationJE)) {
                    String nextAutoNoTemp = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    int sequence = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
                    datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    dateAfterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);//Date After Prefix Part
                    dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    sequence = sequence + counter;
                    String number = "" + sequence;
                    String action = "" + (sequence - counter);
                    nextAutoNoTemp.replaceAll(action, number);
                    jeentryNumber = nextAutoNoTemp.replaceAll(action, number);  //next auto generated number
                    jeIntegerPart = String.valueOf(sequence);
                    jeSeqFormatId = format.getID();
                    counter++;
                    dataMap.put("counter", counter);
                } else if (!StringUtil.isNullOrEmpty(oldRevaluationJE)) {
                    KwlReturnObject result = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), oldRevaluationJE);
                    JournalEntry entry = (JournalEntry) result.getEntityList().get(0);
                    jeid = entry.getID();
                    jeentryNumber = entry.getEntryNumber();
                    jeSeqFormatId = entry.getSeqformat().getID();
                    jeIntegerPart = String.valueOf(entry.getSeqnumber());
                    datePrefix = entry.getDatePreffixValue();
                    dateAfterPrefix = entry.getDateAfterPreffixValue();
                    dateSuffix = entry.getDateSuffixValue();
                    result = accJournalEntryobj.deleteJEDtails(oldRevaluationJE, companyid);
                    result = accJournalEntryobj.deleteJE(oldRevaluationJE, companyid);
                }
            }
            boolean creditDebitFlag = true;
            if (finalAmountReval < 0) {
                finalAmountReval = -(finalAmountReval);
                creditDebitFlag = false;
            }

            Map<String, Object> jeDataMapReval = AccountingManager.getGlobalParams(request);
            jeDataMapReval.put("entrynumber", jeentryNumber);
            jeDataMapReval.put("autogenerated", jeautogenflag);
            jeDataMapReval.put(Constants.SEQFORMAT, jeSeqFormatId);
            jeDataMapReval.put(Constants.SEQNUMBER, jeIntegerPart);
            jeDataMapReval.put(Constants.DATEPREFIX, datePrefix);
            jeDataMapReval.put(Constants.DATEAFTERPREFIX, dateAfterPrefix);
            jeDataMapReval.put(Constants.DATESUFFIX, dateSuffix);
            jeDataMapReval.put("entrydate", jeCreationDate);
            jeDataMapReval.put(Constants.companyKey, companyid);
            jeDataMapReval.put("currencyid", basecurrency);
            jeDataMapReval.put("isReval", 2);
            jeDataMapReval.put("transactionModuleid", dataMap.containsKey("transactionModuleid") ? dataMap.get("transactionModuleid") : 0);
            jeDataMapReval.put("transactionId", dataMap.get("transactionId"));    
            Set jedetailsReval = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMapReval);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            jeDataMapReval.put("jeid", jeid);
            JSONObject jedjsonreval = new JSONObject();
            jedjsonreval.put("srno", jedetailsReval.size() + 1);
            jedjsonreval.put(Constants.companyKey, companyid);
            jedjsonreval.put("amount", finalAmountReval);//rateDecreased?(-1*amountDiff):
            jedjsonreval.put("accountid", preferences.getForeignexchange().getID());
            jedjsonreval.put("debit", creditDebitFlag ? true : false);
            jedjsonreval.put("jeid", jeid);
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjsonreval);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetailsReval.add(jed);
            /*
             * Featching Custom field/Dimension Data from Company prefrences.
             */
            String customfield = "";
            String lineleveldimensions = "";
            KwlReturnObject result = accJournalEntryobj.getRevaluationJECustomData(companyid);
            RevaluationJECustomData revaluationJECustomData = (result != null && result.getEntityList().size() > 0 && result.getEntityList().get(0) != null) ? (RevaluationJECustomData) result.getEntityList().get(0) : null;
            if (revaluationJECustomData != null) {
                customfield = revaluationJECustomData.getCustomfield();
                lineleveldimensions = revaluationJECustomData.getLineleveldimensions();
            }

            /*
             * Make dimensions entry
             */
            setDimensionForRevalJEDetail(lineleveldimensions, jed);
            String unrealised_accid = "";
            if (preferences.getUnrealisedgainloss() != null) {
                unrealised_accid = preferences.getUnrealisedgainloss().getID();
            } else {
                throw new AccountingException(messageSource.getMessage("acc.field.NoUnrealisedGain/Lossaccountfound", null, RequestContextUtils.getLocale(request)));
            }
            jedjsonreval = new JSONObject();
            jedjsonreval.put(Constants.companyKey, companyid);
            jedjsonreval.put("srno", jedetailsReval.size() + 1);
            jedjsonreval.put("amount", finalAmountReval);
            jedjsonreval.put("accountid", unrealised_accid);
            jedjsonreval.put("debit", creditDebitFlag ? false : true);
            jedjsonreval.put("jeid", jeid);
            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjsonreval);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetailsReval.add(jed);
            /*
             * Make dimensions entry
             */
            setDimensionForRevalJEDetail(lineleveldimensions, jed);

            jeDataMapReval.put("jedetails", jedetailsReval);
            jeDataMapReval.put("externalCurrencyRate", 0.0);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMapReval);
            /*
             * Make custom field entry
             */
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", jeid);
                customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    Map<String, Object> customjeDataMap = new HashMap<>();
                    customjeDataMap.put("accjecustomdataref", jeid);
                    customjeDataMap.put("jeid", jeid);
                    customjeDataMap.put("istemplate", journalEntry.getIstemplate());
                    customjeDataMap.put("isReval", journalEntry.getIsReval());
                    accJournalEntryobj.updateCustomFieldJournalEntry(customjeDataMap);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jeid;
    }

    /**
     * Description :This method is used to save Dimension For Reval JEDetail
     */
    public void setDimensionForRevalJEDetail(String lineleveldimensions, JournalEntryDetail jed) {
        try {
            if (!StringUtil.isNullOrEmpty(lineleveldimensions)) {
                JSONArray jcustomarray = new JSONArray(lineleveldimensions);
                HashMap<String, Object> customrequestParams = new HashMap<>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                customrequestParams.put("modulerecid", jed.getID());
                customrequestParams.put("recdetailId", jed.getID());
                customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                customrequestParams.put("companyid", jed.getCompany().getCompanyID());
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    JSONObject jedjsonreval = new JSONObject();
                    jedjsonreval.put("accjedetailcustomdata", jed.getID());
                    jedjsonreval.put("jedid", jed.getID());
                    accJournalEntryobj.updateJournalEntryDetails(jedjsonreval);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
     * Function to update invoice amount due and return KwlReturnObject
     */
    public KwlReturnObject updateInvoiceAmountDueAndReturnResult(Invoice invoice, Company company, double amountReceivedForInvoice, double amountReceivedInBaseCurrencyForInvoice) throws JSONException, ServiceException {
        KwlReturnObject result = null;
        if (invoice != null) {
            double invoiceAmountDue = invoice.getOpeningBalanceAmountDue();
            invoiceAmountDue -= amountReceivedForInvoice;
            JSONObject invjson = new JSONObject();
            invjson.put("invoiceid", invoice.getID());
            invjson.put(Constants.companyKey, company.getCompanyID());
            invjson.put("openingBalanceAmountDue", invoiceAmountDue);
            invjson.put(Constants.openingBalanceBaseAmountDue, invoice.getOriginalOpeningBalanceBaseAmount() - amountReceivedInBaseCurrencyForInvoice);
            invjson.put(Constants.invoiceamountdue, invoice.getInvoiceamountdue() - amountReceivedForInvoice);
            invjson.put(Constants.invoiceamountdueinbase, invoice.getInvoiceAmountDueInBase() - amountReceivedInBaseCurrencyForInvoice);
            result = accInvoiceDAOObj.updateInvoice(invjson, null);
        }
        return result;
    }


    /*
     * Function to update debit note amount due and return KwlReturnObject
     */
    public KwlReturnObject updateDebitNoteAmountDueAndReturnResult(DebitNote debitNote, Company company, double amountReceivedForInvoice, double amountReceivedInBaseCurrencyForInvoice) throws JSONException, ServiceException {
        KwlReturnObject result = null;
        if (debitNote != null) {
            double debitNoteAmountDue = debitNote.getOpeningBalanceAmountDue();
            debitNoteAmountDue -= amountReceivedForInvoice;
            HashMap<String, Object> dnhm = new HashMap<>();
            dnhm.put("dnid", debitNote.getID());
            dnhm.put(Constants.companyKey, company.getCompanyID());
            dnhm.put("openingBalanceAmountDue", debitNoteAmountDue);
            dnhm.put(Constants.openingBalanceBaseAmountDue, debitNote.getOriginalOpeningBalanceBaseAmount() - amountReceivedInBaseCurrencyForInvoice);
            dnhm.put("dnamountdue", debitNote.getDnamountdue() - amountReceivedForInvoice);
            result = accDebitNoteobj.updateDebitNote(dnhm);
        }
        return result;
    }

    /*
     * Update invoice amount due in case of CN deletion.
     */
    public void updateOpeningInvoiceAmountDue(String creditNoteId, String companyId) throws JSONException, ServiceException {

        KwlReturnObject cnObj = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), creditNoteId);
        if (!cnObj.getEntityList().isEmpty()) {
            CreditNote creditNote = (CreditNote) cnObj.getEntityList().get(0);
            Set<CreditNoteDetail> creditNoteDetails = creditNote.getRows();
            if (creditNoteDetails != null && !creditNote.isDeleted()) { // if credit note already temporary deleted then amountdue already updated. No need to update amountdue again for permament delete. 
                Iterator itr = creditNoteDetails.iterator();
                while (itr.hasNext()) {
                    CreditNoteDetail creditNoteDetail = (CreditNoteDetail) itr.next();
                    if (creditNoteDetail.getInvoice() != null && !creditNoteDetail.getInvoice().isNormalInvoice() && creditNoteDetail.getInvoice().isIsOpeningBalenceInvoice()) {
                        double amountPaid = creditNoteDetail.getDiscount().getAmountinInvCurrency();
                        Invoice invObj = creditNoteDetail.getInvoice();
                        HashMap<String, Object> requestParams = new HashMap();
                        requestParams.put(Constants.companyid, companyId);
                        requestParams.put(Constants.globalCurrencyKey, invObj.getCompany().getCurrency().getCurrencyID());
                        double externalCurrencyRate = 0d;
                        externalCurrencyRate = invObj.getExchangeRateForOpeningTransaction();
                        String fromcurrencyid = invObj.getCurrency().getCurrencyID();
                        KwlReturnObject bAmt = null;
                        if (invObj.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountPaid, fromcurrencyid, invObj.getCreationDate(), externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountPaid, fromcurrencyid, invObj.getCreationDate(), externalCurrencyRate);
                        }
                        double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);
                        double invoiceAmountDue = invObj.getOpeningBalanceAmountDue();
                        invoiceAmountDue += amountPaid;
                        JSONObject invjson = new JSONObject();
                        invjson.put("invoiceid", invObj.getID());
                        invjson.put(Constants.companyKey, companyId);
                        invjson.put("openingBalanceAmountDue", invoiceAmountDue);
                        if (invoiceAmountDue != 0) {
                            invjson.put("amountduedate", "");
                        }
                        invjson.put(Constants.openingBalanceBaseAmountDue, invObj.getOpeningBalanceBaseAmountDue() + totalBaseAmountDue);
                        accInvoiceDAOObj.updateInvoice(invjson, null);
                    } else if (creditNoteDetail.getInvoice() != null && creditNoteDetail.getInvoice().isNormalInvoice() && !creditNoteDetail.getInvoice().isIsOpeningBalenceInvoice()) {
                        double amountPaid = creditNoteDetail.getDiscount().getAmountinInvCurrency();
                        Invoice invoice = creditNoteDetail.getInvoice();
                        double invoiceAmountDue = invoice.getInvoiceamountdue();
                        invoiceAmountDue += amountPaid;
                        JSONObject invjson = new JSONObject();
                        invjson.put("invoiceid", invoice.getID());
                        invjson.put(Constants.companyKey, companyId);
                        invjson.put(Constants.invoiceamountdue, invoiceAmountDue);
                        HashMap<String, Object> requestParams = new HashMap();
                        requestParams.put(Constants.companyid, companyId);
                        requestParams.put(Constants.globalCurrencyKey, invoice.getCompany().getCurrency().getCurrencyID());
                        String fromcurrencyid = invoice.getCurrency().getCurrencyID();
                        KwlReturnObject bAmt = null;
                        JournalEntry je = invoice.getJournalEntry();
                        if (je != null) {
                            KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, invoiceAmountDue, fromcurrencyid, invoice.getCreationDate(), je.getExternalCurrencyRate());
                            double invoiceamountdueinbase = (Double) baseAmount.getEntityList().get(0);
                            invjson.put(Constants.invoiceamountdueinbase, invoiceamountdueinbase);
                        }
                        if (invoiceAmountDue != 0) {
                            invjson.put("amountduedate", "");
                        }
                        accInvoiceDAOObj.updateInvoice(invjson, null);
                    }
                }
            }
        }
    }

    @Override
    public List linkCreditNote(HttpServletRequest request, String creditNoteId, Boolean isInsertAudTrail) throws ServiceException, SessionExpiredException, JSONException, AccountingException {
        List result = new ArrayList();
        List linkedInvoicesList = new ArrayList();
        List linkedNotesList = new ArrayList();
        try {
            JSONArray invoiceArray = new JSONArray();
            JSONArray noteArray = new JSONArray();
            JSONArray linkJSONArray = request.getParameter("linkdetails") != null ? new JSONArray(request.getParameter("linkdetails")) : new JSONArray();
            Map<String, Object> counterMap = new HashMap<>();
            counterMap.put("counter", 0);
            for (int i = 0; i < linkJSONArray.length(); i++) {
                JSONObject obj = linkJSONArray.getJSONObject(i);
                int documenttype = Integer.parseInt(obj.optString("documentType"));
                if (documenttype == Constants.CreditNoteOtherwise && obj.optDouble("linkamount", 0.0) != 0) {
                    invoiceArray.put(obj);
                } else if (documenttype == Constants.CreditNoteAgainstDebitNote && obj.optDouble("linkamount", 0.0) != 0) {
                    noteArray.put(obj);
                }
            }

            if (invoiceArray.length() > 0) {
                linkedInvoicesList = linkCreditNoteToInvoices(request, invoiceArray, creditNoteId, isInsertAudTrail, counterMap);
            }
            if (noteArray.length() > 0) {
                linkedNotesList = linkCreditNoteToDebitNote(request, noteArray, creditNoteId, isInsertAudTrail, counterMap);
            }

        } catch (AccountingException ex) {
            throw new AccountingException(ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        result.addAll(linkedInvoicesList);
        result.addAll(linkedNotesList);
        return result;
    }

    public List linkCreditNoteToInvoices(HttpServletRequest request, JSONArray invoiceArray, String creditNoteId, Boolean isInsertAudTrail, Map<String, Object> counterMap) throws ServiceException, SessionExpiredException, AccountingException {
        List result = new ArrayList();
        String companyid = sessionHandlerImpl.getCompanyid(request);
        DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
        def1.setName("DB_Tx");
        def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus status = null;
        try {
            String cnid = "";
            int counter = 0;
            if (!StringUtil.isNullOrEmpty(creditNoteId)) {
                cnid = creditNoteId;
            } else {
                cnid = request.getParameter("cnid");
            }
            Date maxLinkingDate = null;
             //Commented because of ERP-36411
//            String linkingdate = (String) request.getParameter("linkingdate");
            DateFormat dateformat = authHandler.getDateOnlyFormat();
//            if (!StringUtil.isNullOrEmpty(linkingdate)) {
//                try {
//                    maxLinkingDate = dateformat.parse(linkingdate);
//                } catch (ParseException ex) {
//                    Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
            String entryNumber = request.getParameter("number");
            String amounts[] = request.getParameter("amounts").split(",");
            for (int k = 0; k < invoiceArray.length(); k++) {
                JSONObject jobj = invoiceArray.getJSONObject(k);

                if (StringUtil.isNullOrEmpty(jobj.getString("documentid"))) {
                    continue;
                }
                double typeFigure = 0.0;
                int typeOfFigure = 1;
                double usedcnamount = jobj.optDouble("linkamount", 0.0d);
                double usedcnamountInBase = 0.0d;
//                if (!StringUtil.isNullOrEmpty(amounts[k])) {
//                    usedcnamount = Double.parseDouble((String) amounts[k]);
//                } else {
//                    usedcnamount = 0;
//                }
                if (usedcnamount == 0) {
                    continue;
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("typeFigure"))) {
                    typeFigure = jobj.optDouble("typeFigure", 0.0);
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("typeOfFigure"))) {
                    typeOfFigure = jobj.optInt("typeOfFigure", 1);
                }

                try {
                    synchronized (this) {
                        /*
                         * Checks duplicate number for simultaneous transactions
                         */
                        status = txnManager.getTransaction(def1);
                        KwlReturnObject resultInv1 = accPaymentDAOobj.getInvoiceInTemp(jobj.getString("documentid"), companyid, Constants.Acc_Vendor_Invoice_ModuleId);
                        if (resultInv1.getRecordTotalCount() > 0) {
                            throw new AccountingException("Selected invoice is already in process, please try after sometime.");
                        } else {
                            accPaymentDAOobj.insertInvoiceOrCheque(jobj.getString("documentid"), companyid, Constants.Acc_Vendor_Invoice_ModuleId, "");
                        }
                        txnManager.commit(status);
                    }
                } catch (Exception ex) {
                    txnManager.rollback(status);
                    throw new AccountingException(ex.getMessage(), ex);
                }

                KwlReturnObject grresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), jobj.getString("documentid"));
                Invoice invObj = (Invoice) grresult.getEntityList().get(0);

                KwlReturnObject cnObj = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), cnid);
                CreditNote creditNote = (CreditNote) cnObj.getEntityList().get(0);

                grresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) grresult.getEntityList().get(0);

                Set<CreditNoteDetail> newcndetails = new HashSet<CreditNoteDetail>();
                double cnamountdue = creditNote.getCnamountdue();
                double invoiceAmountDue = 0; // Opening transaction record
                if (invObj.isIsOpeningBalenceInvoice() && !invObj.isNormalInvoice()) {
                    invoiceAmountDue = invObj.getOpeningBalanceAmountDue();
                } else {
                    invoiceAmountDue = invObj.getInvoiceamountdue();
                }
                if (!creditNote.isOpenflag() || cnamountdue <= 0) {
                    throw new AccountingException(messageSource.getMessage("acc.field.CreditNotehasbeenalreadyutilized.", null, RequestContextUtils.getLocale(request)));
                }
                /**
                 * SDP-13710 For Credit Note Link Transaction, Invoice Amount
                 * Due Converted into Base Currency.
                 */
                HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                requestParams.put(Constants.companyid, sessionHandlerImpl.getCompanyid(request));
                requestParams.put(Constants.globalCurrencyKey, sessionHandlerImpl.getCurrencyID(request));
                KwlReturnObject bAmt = null;
                if (!invObj.isIsOpeningBalenceInvoice()) {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, invoiceAmountDue, creditNote.getCurrency().getCurrencyID(), invObj.getCreationDate(), invObj.getJournalEntry().getExternalCurrencyRate());
                } else {
                    double exchangeRateForInvoice = invObj.getExchangeRateForOpeningTransaction();
                    if (invObj.isConversionRateFromCurrencyToBase()) {
                        exchangeRateForInvoice = 1 / exchangeRateForInvoice;
                    }
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, invoiceAmountDue, creditNote.getCurrency().getCurrencyID(), invObj.getCreationDate(), exchangeRateForInvoice);
                }
                invoiceAmountDue = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                /**
                 * Enter Amount Converted into Base Currency.
                 */
                double exchangeRateForCN = 0.0;
                if (!creditNote.isIsOpeningBalenceCN()) {
                    exchangeRateForCN = creditNote.getExternalCurrencyRate();
                } else {
                    exchangeRateForCN = creditNote.getExchangeRateForOpeningTransaction();
                    if (creditNote.isConversionRateFromCurrencyToBase()) {
                        exchangeRateForCN = 1 / exchangeRateForCN;
                    }
                }
                /**
                 * if ExchangeRateForCN is 1 then no need to convert into Base
                 * Currency.
                 */
                if (exchangeRateForCN == 1.0) {
                    usedcnamountInBase = usedcnamount;
                } else {
                    KwlReturnObject bAmtUsedAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, usedcnamount, creditNote.getCurrency().getCurrencyID(), creditNote.getCreationDate(), exchangeRateForCN);
                    usedcnamountInBase = authHandler.round((Double) bAmtUsedAmount.getEntityList().get(0), companyid);
                }
                /**
                 * I have comment this code due to some scenario are interrupt.
                 * below condition for checks simultaneous transaction, 
                 */
//                if (invoiceAmountDue < usedcnamountInBase) {
//                    throw new AccountingException(messageSource.getMessage("acc.field.alreadyknock_off", null, RequestContextUtils.getLocale(request)));
//                }
                double amountReceived = usedcnamount;           //amount of DN 
                double amountReceivedConverted = usedcnamount;
                double adjustedRate = 1;
                double exchangeRateforTransaction = jobj.optDouble("exchangeratefortransaction", 1.0);
                if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(invObj.getCurrency().getCurrencyID()) && !invObj.getCurrency().getCurrencyID().equals(creditNote.getCurrency().getCurrencyID())) {
                    // adjusted exchange rate used to handle case like ERP-34884
                    adjustedRate = exchangeRateforTransaction;
                    if (jobj.optDouble("amountdue", 0) != 0 && jobj.optDouble("amountDueOriginal", 0) != 0) {
                        adjustedRate = jobj.optDouble("amountdue", 0) / jobj.optDouble("amountDueOriginal", 0);
                    }
                    amountReceivedConverted = amountReceived / adjustedRate;
                    amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
                } else {
                    amountReceivedConverted = authHandler.round(amountReceived, companyid);
                }
                Date maxDate = null;
                String linkingdate = null;
                if (jobj.has("linkingdate") && jobj.get("linkingdate") != null) {
                    linkingdate = (String) jobj.get("linkingdate");
                    if (!StringUtil.isNullOrEmpty(linkingdate)) {
                        try {
                            maxLinkingDate = dateformat.parse(linkingdate);
                        } catch (ParseException ex) {
                            Logger.getLogger(accCreditNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                
                //For invoices and otherwise cases
                if ((creditNote.getCntype() == CreditNote.CREDITNOTE_AGAINST_INVOICE || creditNote.getCntype() == CreditNote.CREDITNOTE_OTHERWISE) && typeFigure ==0.0) {
                    typeFigure = jobj.optDouble("typeFigure", usedcnamount);
                }
                CreditNoteDetail cndetailObj = new CreditNoteDetail();
                if (creditNote.getCntype() != 3 && creditNote.getCntype() != Constants.CreditNoteForOvercharge && creditNote.isOpenflag() && creditNote.getCnamount() == creditNote.getCnamountdue()) {//If CN is used for first time.
                    Set<CreditNoteDetail> cndetails = (Set<CreditNoteDetail>) creditNote.getRows();
                    Iterator itr = cndetails.iterator();
                    while (itr.hasNext()) {
                        cndetailObj = (CreditNoteDetail) itr.next();

                        if (invObj != null) {
                            cndetailObj.setInvoice(invObj);
                            /*
                             * code to save linking date of CN and Invoice. This
                             * linking date used while calculating due amount of
                             * CN in Aged/SOA report
                             */
                            Date linkingDate = new Date();
                            Date invDate = invObj.getCreationDate();
                            Date cnDate = creditNote.getCreationDate();
                            if (maxLinkingDate != null) {
                                maxDate = maxLinkingDate;
                            } else {
                                List<Date> datelist = new ArrayList<Date>();
                                datelist.add(linkingDate);
                                datelist.add(invDate);
                                datelist.add(cnDate);
                                Collections.sort(datelist); //Sort the Date object & get the dates in ASC order. Pick-up the last record as Max Date
                                maxDate = datelist.get(datelist.size() - 1);
                            }
                            cndetailObj.setInvoiceLinkDate(maxDate);
                        }
                        //change
                        double invoiceOriginalAmt = 0d;
                        if (!invObj.isNormalInvoice() && invObj.isIsOpeningBalenceInvoice()) {
                            invoiceOriginalAmt = invObj.getOriginalOpeningBalanceAmount();
                        } else {
                            invoiceOriginalAmt = invObj.getCustomerEntry().getAmount();
                        }
                        JSONObject discjson = new JSONObject();
                        discjson.put("discount", usedcnamount);         //amount in DN currency
                        discjson.put("amountinInvCurrency", amountReceivedConverted);
                        discjson.put("inpercent", false);
                        discjson.put("originalamount", invoiceOriginalAmt);//(Double) bAmt.getEntityList().get(0));
                        discjson.put(Constants.companyKey, companyid);
                        discjson.put("typeOfFigure", typeOfFigure);
                        discjson.put("typeFigure", typeFigure);
                        KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                        Discount discount = (Discount) dscresult.getEntityList().get(0);
                        cndetailObj.setDiscount(discount);
                        cndetailObj.setExchangeRateForTransaction(exchangeRateforTransaction);
                        newcndetails.add(cndetailObj);
                    }
                } else {
                    Set<CreditNoteDetail> cndetails = (Set<CreditNoteDetail>) creditNote.getRows();
                    Iterator itr = cndetails.iterator();
                    int i = 0;
                    while (itr.hasNext()) {
                        cndetailObj = (CreditNoteDetail) itr.next();
                        newcndetails.add(cndetailObj);
                        i++;
                    }

                    cndetailObj = new CreditNoteDetail();

                    if (invObj != null) {
                        cndetailObj.setInvoice(invObj);
                        /*
                         * code to save linking date of CN and Invoice. This
                         * linking date used while calculating due amount of CN
                         * in Aged/SOA report
                         */
                        Date linkingDate = new Date();
                        Date invDate = invObj.getCreationDate();
                        Date cnDate = creditNote.getCreationDate();
                        if (maxLinkingDate != null) {
                            maxDate = maxLinkingDate;
                        } else {
                            List<Date> datelist = new ArrayList<Date>();
                            datelist.add(linkingDate);
                            datelist.add(invDate);
                            datelist.add(cnDate);
                            Collections.sort(datelist); //Sort the Date object & get the dates in ASC order. Pick-up the last record as Max Date
                            maxDate = datelist.get(datelist.size() - 1);
                        }
                        cndetailObj.setInvoiceLinkDate(maxDate);
                    }

                    cndetailObj.setSrno(i + 1);
                    cndetailObj.setTotalDiscount(0.00);
                    cndetailObj.setCompany(company);
                    cndetailObj.setMemo("");
                    cndetailObj.setCreditNote(creditNote);
                    cndetailObj.setID(UUID.randomUUID().toString());

                    double invoiceOriginalAmt = 0d;
                    if (invObj.isNormalInvoice()) {
                        invoiceOriginalAmt = invObj.getCustomerEntry().getAmount();
                    } else {// for only opening balance Invoices
                        invoiceOriginalAmt = invObj.getOriginalOpeningBalanceAmount();
                    }

                    JSONObject discjson = new JSONObject();
                    discjson.put("discount", usedcnamount);
                    discjson.put("amountinInvCurrency", amountReceivedConverted);        //amount in DN currency
                    discjson.put("inpercent", false);
                    discjson.put("originalamount", invoiceOriginalAmt);//(Double) bAmt.getEntityList().get(0));
                    discjson.put(Constants.companyKey, companyid);
                    discjson.put("typeOfFigure", typeOfFigure);
                    discjson.put("typeFigure", typeFigure);
                    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                    Discount discount = (Discount) dscresult.getEntityList().get(0);
                    cndetailObj.setDiscount(discount);
                    cndetailObj.setExchangeRateForTransaction(exchangeRateforTransaction);
                    newcndetails.add(cndetailObj);
                }

                double amountDue = cnamountdue - usedcnamount;
                double externalCurrencyRate = 1d;
                boolean isopeningBalanceCN = creditNote.isIsOpeningBalenceCN();
                Date cnCreationDate = null;
                cnCreationDate = creditNote.getCreationDate();
                if (!creditNote.isNormalCN() && creditNote.isIsOpeningBalenceCN()) {
                    externalCurrencyRate = creditNote.getExchangeRateForOpeningTransaction();
                } else {
                    externalCurrencyRate = creditNote.getJournalEntry().getExternalCurrencyRate();
                }
                String fromcurrencyid = creditNote.getCurrency().getCurrencyID();
                if (isopeningBalanceCN && creditNote.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                }
                double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);
                HashMap<String, Object> credithm = new HashMap<String, Object>();
                credithm.put("cndetails", newcndetails);
                credithm.put("cnid", creditNote.getID());
                credithm.put("cnamountdue", amountDue);
                credithm.put("openingBalanceAmountDue", amountDue);
                credithm.put(Constants.openingBalanceBaseAmountDue, totalBaseAmountDue);
                credithm.put("openflag", (cnamountdue - usedcnamount) <= 0 ? false : true);

                if (request.getAttribute("entrynumber") != null) {
                    credithm.put("entrynumber", request.getAttribute("entrynumber"));
                }
                if (request.getAttribute("autogenerated") != null) {
                    credithm.put("autogenerated", request.getAttribute("autogenerated"));

                }
                if (request.getAttribute("seqformat") != null) {
                    credithm.put(Constants.SEQFORMAT, request.getAttribute("seqformat"));

                }
                if (request.getAttribute("seqnumber") != null) {
                    credithm.put(Constants.SEQNUMBER, request.getAttribute("seqnumber"));

                }
                if (request.getAttribute(Constants.DATEPREFIX) != null) {
                    credithm.put(Constants.DATEPREFIX, request.getAttribute(Constants.DATEPREFIX));
                }
                if (request.getAttribute(Constants.DATESUFFIX) != null) {
                    credithm.put(Constants.DATESUFFIX, request.getAttribute(Constants.DATESUFFIX));
                }

                KwlReturnObject result1 = accCreditNoteDAOobj.updateCreditNote(credithm);

                // Update Invoice base amount due. We have to consider Invoice currency rate to calculate.
                externalCurrencyRate = 1d;
                boolean isopeningBalanceINV = invObj.isIsOpeningBalenceInvoice();
                Date noteCreationDate = null;
                noteCreationDate = creditNote.getCreationDate();
                if (!invObj.isNormalInvoice() && isopeningBalanceINV) {
                    externalCurrencyRate = isopeningBalanceCN ? creditNote.getExchangeRateForOpeningTransaction() : creditNote.getJournalEntry().getExternalCurrencyRate();
                } else {
                    externalCurrencyRate = isopeningBalanceCN ? creditNote.getExchangeRateForOpeningTransaction() : creditNote.getJournalEntry().getExternalCurrencyRate();
                }
                fromcurrencyid = creditNote.getCurrency().getCurrencyID();
                if (isopeningBalanceINV && invObj.isConversionRateFromCurrencyToBase()) {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, usedcnamount, fromcurrencyid, noteCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, usedcnamount, fromcurrencyid, noteCreationDate, externalCurrencyRate);
                }
                totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);

                /*
                 * Store the date on which the amount due has been set to zero
                 */

                KwlReturnObject invoiceResult = updateInvoiceAmountDueAndReturnResult(invObj, company, amountReceivedConverted, totalBaseAmountDue);
                if (invoiceResult != null && invoiceResult.getEntityList() != null && invoiceResult.getEntityList().size() > 0) {
                    Invoice inv = (Invoice) invoiceResult.getEntityList().get(0);
                    if (inv.isIsOpeningBalenceInvoice() && inv.getOpeningBalanceAmountDue() == 0) {
                        try {
                            HashMap<String, Object> dataMap = new HashMap<String, Object>();
                            if (creditNote.isIsOpeningBalenceCN() && creditNote != null && creditNote.getCreationDate() != null) {
                                dataMap.put("amountduedate", maxDate);
                                accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(inv, dataMap);
                            } else if (!creditNote.isIsOpeningBalenceCN() && creditNote != null && creditNote.getJournalEntry() != null && creditNote.getJournalEntry().getCreatedOn() != null) {
                                dataMap.put("amountduedate", maxDate);
                                accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(inv, dataMap);
                            }
                        } catch (Exception ex) {
                            System.out.println("" + ex.getMessage());
                        }
                    } else if (inv.getInvoiceamountdue() == 0) {
                        try {
                            DateFormat df = authHandler.getDateFormatter(request);
                            HashMap<String, Object> dataMap = new HashMap<String, Object>();
                            if (creditNote.isIsOpeningBalenceCN() && creditNote != null && creditNote.getCreationDate() != null) {
                                dataMap.put("amountduedate", maxDate);
                                accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(inv, dataMap);
                            } else if (!creditNote.isIsOpeningBalenceCN() && creditNote != null && creditNote.getJournalEntry() != null && creditNote.getJournalEntry().getCreatedOn() != null) {
                                dataMap.put("amountduedate", maxDate);
                                accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(inv, dataMap);
                            }
                        } catch (Exception ex) {
                            System.out.println("" + ex.getMessage());
                        }
                    }
                }
                if (isInsertAudTrail) {
                    auditTrailObj.insertAuditLog(AuditAction.CREDIT_NOTE_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has linked Credit Note " + creditNote.getCreditNoteNumber() + " with Customer Invoice " + invObj.getInvoiceNumber() + ".", request, creditNote.getID());
                }

                /*
                 * Start gains/loss calculation Calculate Gains/Loss if Invoice
                 * exchange rate changed at the time of linking with CN
                 */
                String creditid = request.getParameter("noteid");
                KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
                if (preferences.getForeignexchange() == null) {
                    throw new AccountingException(messageSource.getMessage("acc.common.forex", null, RequestContextUtils.getLocale(request)));
                }
                if (isopeningBalanceCN && creditNote.isConversionRateFromCurrencyToBase()) {
                    externalCurrencyRate = 1 / externalCurrencyRate;
                }
                Map<String, Object> mapForForexGainLoss = new HashMap<>();
                mapForForexGainLoss.put("cn", creditNote);
                mapForForexGainLoss.put("invoice", invObj);
                mapForForexGainLoss.put("basecurreny", sessionHandlerImpl.getCurrencyID(request));
                mapForForexGainLoss.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
                mapForForexGainLoss.put("creationdate", creditNote.getCreationDate());
                mapForForexGainLoss.put("exchangeratefortransaction", exchangeRateforTransaction);
                mapForForexGainLoss.put("recinvamount", usedcnamount);
                mapForForexGainLoss.put("externalcurrencyrate", externalCurrencyRate);
                mapForForexGainLoss.put(Constants.RES_DATEFORMAT, authHandler.getDateOnlyFormat());
                double amountDiff = getForexGainLossForCreditNote(mapForForexGainLoss);
                if (amountDiff != 0 && preferences.getForeignexchange() != null && Math.abs(amountDiff) >= 0.000001) {//Math.abs(amountDiff) < .0000001 Added this because in case of revaluation the diff in the rates was less than .0000001 and the amount calculation was going in Exachange rate account. 
                    boolean rateDecreased = false;
                    if (amountDiff < 0) {
                        rateDecreased = true;
                    }
                    JournalEntry journalEntry = null;
                    Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                    String jeentryNumber = null;
                    boolean jeautogenflag = false;
                    String jeSeqFormatId = "";
                    String datePrefix = "";
                    String dateAfterPrefix = "";
                    String dateSuffix = "";
                    Date entryDate = null;
                    if (maxLinkingDate != null) {
                        entryDate = new Date(maxLinkingDate.getTime());
                    } else {
                        entryDate = new Date();
                    }
                    synchronized (this) {
                        HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                        JEFormatParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                        JEFormatParams.put("modulename", "autojournalentry");
                        JEFormatParams.put(Constants.companyKey, companyid);
                        JEFormatParams.put("isdefaultFormat", true);

                        KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                        SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                        int sequence = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
                        String nextAutoNUmber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                        datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                        dateAfterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);//Date After Prefix Part
                        dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                        sequence = sequence + counter;
                        String number = "" + sequence;
                        String action = "" + (sequence - counter);
                        nextAutoNUmber.replaceAll(action, number);
                        jeentryNumber = nextAutoNUmber.replaceAll(action, number);  //next auto generated number
                        jeSeqFormatId = format.getID();
                        jeautogenflag = true;

                        jeDataMap.put("entrynumber", jeentryNumber);
                        jeDataMap.put("autogenerated", jeautogenflag);
                        jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                        jeDataMap.put(Constants.SEQNUMBER, number);
                        jeDataMap.put(Constants.DATEPREFIX, datePrefix);
                        jeDataMap.put(Constants.DATEAFTERPREFIX, dateAfterPrefix);
                        jeDataMap.put(Constants.DATESUFFIX, dateSuffix);
                    }
                    jeDataMap.put("entrydate", dateformat.parse(linkingdate)); //SDP-2944
                    jeDataMap.put(Constants.companyKey, companyid);
                    jeDataMap.put(Constants.memo, "Exchange Gains/Loss posted against Advance Receipt '" + creditNote.getCreditNoteNumber() + "' linked to Invoice '" + invObj.getInvoiceNumber() + "'");
                    jeDataMap.put("currencyid", creditNote.getCurrency().getCurrencyID());
                    jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
                    jeDataMap.put("isexchangegainslossje", true);
                    jeDataMap.put("transactionId", creditNote.getID());
                    jeDataMap.put("transactionModuleid", Constants.Acc_Credit_Note_ModuleId);
                    journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
                    accJournalEntryobj.saveJournalEntryByObject(journalEntry);

                    boolean isDebit = rateDecreased ? true : false;
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", 1);
                    jedjson.put(Constants.companyKey, companyid);
                    jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                    jedjson.put("accountid", preferences.getForeignexchange().getID());
                    jedjson.put("debit", isDebit);
                    jedjson.put("jeid", journalEntry.getID());
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    Set<JournalEntryDetail> detail = new HashSet();
                    detail.add(jed);

                    jedjson = new JSONObject();
                    jedjson.put("srno", 2);
                    jedjson.put(Constants.companyKey, companyid);
                    jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                    jedjson.put("accountid", invObj.getAccount().getID());
                    jedjson.put("debit", !isDebit);
                    jedjson.put("jeid", journalEntry.getID());
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    detail.add(jed);
                    journalEntry.setDetails(detail);
                    accJournalEntryobj.saveJournalEntryDetailsSet(detail);

                    // Save link JE iformation in DN details
                    cndetailObj.setLinkedGainLossJE(journalEntry.getID());
//                newcndetails.add(cndetailObj);
                    credithm.put("cndetails", newcndetails);
                    KwlReturnObject result2 = accCreditNoteDAOobj.updateCreditNote(credithm);
                    counter++;
                }
                // End Gains/Loss Calculation

                //JE For Receipt which is of Opening Type
                if (counterMap.containsKey("counter")) {
                    counter = (Integer) counterMap.get("counter");
                }
                counterMap.put("counter", counter);
                if (creditNote != null && (creditNote.isIsOpeningBalenceCN() || creditNote.isOtherwise())) {
                    String basecurrency = sessionHandlerImpl.getCurrencyID(request);
                    double finalAmountReval = ReevalJournalEntryForCreditNote(request, creditNote, amountReceived, exchangeRateforTransaction);
                    if (finalAmountReval != 0) {
                        /**
                         * added transactionID and transactionModuleID to
                         * Realised JE.
                         */
                        counterMap.put("transactionModuleid", creditNote.isIsOpeningBalenceCN() ? (creditNote.iscNForCustomer() ? Constants.Acc_opening_Customer_CreditNote : Constants.Acc_opening_Vendor_CreditNote)  : Constants.Acc_Credit_Note_ModuleId);
                        counterMap.put("transactionId", creditNote.getID());
                        String revaljeid = PostJEFORReevaluation(request, -(finalAmountReval), companyid, preferences, basecurrency, cndetailObj.getRevalJeId(), counterMap);
                        cndetailObj.setRevalJeId(revaljeid);
                    }
                }
                //JE For Debit which is Linked to Receipt
                if (invObj != null) {
                    double finalAmountReval = ReevalJournalEntryForInvoice(request, invObj, amountReceived, exchangeRateforTransaction);
                    if (finalAmountReval != 0) {
                        String basecurrency = sessionHandlerImpl.getCurrencyID(request);
                        /**
                         * added transactionID and transactionModuleID to
                         * Realised JE.
                         */
                        counterMap.put("transactionModuleid", invObj.isIsOpeningBalenceInvoice() ? Constants.Acc_opening_Sales_Invoice : Constants.Acc_Invoice_ModuleId);
                        counterMap.put("transactionId", invObj.getID());
                        String revaljeid = PostJEFORReevaluation(request, finalAmountReval, companyid, preferences, basecurrency, cndetailObj.getRevalJeIdInvoice(), counterMap);
                        cndetailObj.setRevalJeIdInvoice(revaljeid);

                    }
                }
                if (invObj != null) {

                    HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                    requestParamsLinking.put("linkeddocid", cnid);
                    requestParamsLinking.put("docid", invObj.getID());
                    requestParamsLinking.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                    requestParamsLinking.put("linkeddocno", entryNumber);
                    requestParamsLinking.put("sourceflag", 0);
                    KwlReturnObject result3 = accInvoiceDAOObj.saveInvoiceLinking(requestParamsLinking);


                    /*
                     * saving linking informaion of Purchase Invoice while
                     * linking with DebitNote
                     */

                    requestParamsLinking.put("linkeddocid", invObj.getID());
                    requestParamsLinking.put("docid", cnid);
                    requestParamsLinking.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                    requestParamsLinking.put("linkeddocno", invObj.getInvoiceNumber());
                    requestParamsLinking.put("sourceflag", 1);
                    result1 = accCreditNoteDAOobj.saveCreditNoteLinking(requestParamsLinking);
                }
            }
        } catch (AccountingException ex) {
            throw new AccountingException(ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            status = txnManager.getTransaction(def1);
            deleteTemporaryInvoicesEntries(invoiceArray, companyid);
            txnManager.commit(status);
        }
        return result;
    }

    @Override
    public JSONArray exportCreditNoteWithDetails(HttpServletRequest request, HttpServletResponse response, JSONArray dataArray) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        JSONArray finalArray = new JSONArray();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            if (paramJobj.has("ss") && !StringUtil.isNullOrEmpty(paramJobj.optString("ss"))) {
                paramJobj.put("ss", StringUtil.DecodeText(paramJobj.optString("ss")));
            }
            boolean isForReport = false;
            String dtype = paramJobj.optString("dtype", "");
            if (!StringUtil.isNullOrEmpty(dtype) && dtype.equals("report")) {
                isForReport = true;
            }
            paramJobj.put("isForReport", isForReport);
            HashMap dataHashMap = accCreditNoteService.getCreditNoteCommonCode(paramJobj);
            DataJArr = (JSONArray) dataHashMap.get(Constants.RES_data);
            int cnCount = 0;
            for (int i = 0; i < DataJArr.length(); i++) {
                JSONObject obj = DataJArr.getJSONObject(i);
                String billid = obj.optString("noteid", "");
                KwlReturnObject cnResult = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), billid);
                CreditNote CN = (CreditNote) cnResult.getEntityList().get(0);
                finalArray.put(obj);
                JSONObject temp = null;
                if (CN.getCntype() == 5 || CN.getCntype() == Constants.CreditNoteForOvercharge) {
                    temp = getCreditNoteRows(paramJobj, billid.split(","));
                } else {
                    temp = getCreditNoteRow(request, billid.split(","));
                }

                JSONArray jArray = (JSONArray) temp.get(Constants.RES_data);
                int accDetailCount = 0;
                int invDetailCount = 0;
                for (int j = 0; j < jArray.length(); j++) {
                    JSONObject row = jArray.getJSONObject(j);
                    exportDaoObj.editJsonKeyForExcelFile(row, Constants.Acc_Credit_Note_ModuleId);
                    if (row.optBoolean("isaccountdetails", false)) {
                        row.put("srnoforaccount", accDetailCount + 1);
                        row.put("taxamount", "");
                        row.put("reason", "");
                        accDetailCount++;
                        finalArray.put(row);
                    } else {
                        row.put("srno", invDetailCount + 1);
                        row.put("currencycode", "");
                        row.put(Constants.memo, "");
                        finalArray.put(row);
                        invDetailCount++;
                    }
                }
                cnCount++;
            }
            jobj.put(Constants.RES_data, finalArray);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return finalArray;
    }

    @Override
    public JSONArray addTotalsForPrint(HttpServletRequest request, JSONArray DataJArr) throws JSONException, SessionExpiredException {
        int reportID = request.getParameter("reportID") == null ? 0 : Integer.parseInt(request.getParameter("reportID"));
        String accountIds = StringUtil.isNullOrEmpty(request.getParameter("cnAccountIds")) ? "" : request.getParameter("cnAccountIds");
        String accIDArray[] = new String[accountIds.length()];

        String companyid = sessionHandlerImpl.getCompanyid(request);
        if (reportID == Constants.CREDIT_NOTE_WITH_ACCOUNT && !StringUtil.isNullOrEmpty(accountIds)) {
            accIDArray = accountIds.split(",");
        }
        JSONObject accTotal = new JSONObject();
        double totalDiscountAmount = 0;
        double totalTaxAmount = 0;
        double totalTermAmount = 0;
        double totalAmountBeforeGST = 0;
        double totalAmount = 0;
        double totalBaseAmount = 0;
        double totalAmountDue = 0;
        double totalAmountDueInBase = 0;
        for (int i = 0; i < DataJArr.length(); i++) {
            JSONObject obj = (JSONObject) DataJArr.get(i);
            totalDiscountAmount += obj.optDouble("discount", 0);
            totalTaxAmount += obj.optDouble("taxamount", 0);
            totalTermAmount += obj.optDouble("termamount", 0);
            totalAmountBeforeGST += obj.optDouble("amountbeforegst", 0);
            totalAmount += obj.optDouble("amount", 0);
            totalBaseAmount += obj.optDouble("amountinbase", 0);
            totalAmountDue += obj.optDouble("amountdue", 0);
            totalAmountDueInBase += obj.optDouble("amountdueinbase", 0);

            if (reportID == Constants.CREDIT_NOTE_WITH_ACCOUNT) {
                for (String accId : accIDArray) {
                    double tempTotal = accTotal.optDouble(accId, 0);
                    accTotal.put(accId, tempTotal + obj.optDouble(accId + "_Amount", 0));
                }

            }
        }

        JSONObject obj = new JSONObject();
        obj.put("noteno", "Total");
        obj.put("discount", authHandler.round(totalDiscountAmount, companyid));
        obj.put("taxamount", authHandler.round(totalTaxAmount, companyid));
        obj.put("termamount", authHandler.round(totalTermAmount, companyid));
        obj.put("amountbeforegst", authHandler.round(totalAmountBeforeGST, companyid));
        obj.put("amount", authHandler.round(totalAmount, companyid));
        obj.put("amountinbase", authHandler.round(totalBaseAmount, companyid));
        obj.put("amountdue", authHandler.round(totalAmountDue, companyid));
        obj.put("amountdueinbase", authHandler.round(totalAmountDueInBase, companyid));
        if (reportID == Constants.CREDIT_NOTE_WITH_ACCOUNT) {
            for (String accId : accIDArray) {
                double tempTotal = accTotal.optDouble(accId, 0);
                obj.put(accId + "_Amount", authHandler.round(tempTotal, companyid));
            }
        }
        DataJArr.put(obj);

        return DataJArr;
    }
    
}
