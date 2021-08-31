package com.krawler.spring.accounting.vendorpayment;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.CommonIndonesianNumberToWords;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.bankreconciliation.accBankReconciliationDAO;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customDesign.CustomDesignerConstants;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptCMN;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.handler.CommonFunctions;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.journalentry.JournalEntryConstants;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.payment.accPaymentService;
import com.krawler.spring.accounting.repeatedtransaction.accRepeateInvoice;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendorpayment.service.AccVendorPaymentModuleService;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.exportFuctionality.ExportRecordHandler;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.NoSuchMessageException;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.StringUtils;
import com.krawler.spring.mainaccounting.service.AccMainAccountingService;
import com.krawler.spring.accounting.goodsreceipt.GoodsReceiptCMNConstants;
import static com.krawler.spring.accounting.goodsreceipt.GoodsReceiptCMNConstants.COUNT;
import static com.krawler.spring.accounting.goodsreceipt.GoodsReceiptCMNConstants.DATA;
import com.krawler.spring.accounting.creditnote.accCreditNoteController;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.creditnote.accCreditNoteService;
import com.krawler.spring.accounting.goodsreceipt.*;

/**
 *
 * @author krawler
 */
public class AccVendorPaymentServiceImpl implements AccVendorPaymentServiceDAO, MessageSourceAware{

    private accAccountDAO accAccountDAOobj;
    private authHandlerDAO authHandlerDAOObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accVendorPaymentDAO accVendorPaymentobj;
    private accBankReconciliationDAO accBankReconciliationObj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accTaxDAO accTaxObj;
    private accGoodsReceiptCMN accGoodsReceiptCMN;
    private EnglishNumberToWords EnglishNumberToWordsOjb = new EnglishNumberToWords();
    private CommonIndonesianNumberToWords IndonesianNumberToWordsOjb = new CommonIndonesianNumberToWords();
    private AccCommonTablesDAO accCommonTablesDAO;
    private accJournalEntryDAO accJournalEntryobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private accPaymentDAO accPaymentDAOobj;
    private accPaymentService accPaymentService;
    private fieldDataManager fieldDataManagercntrl;
    private HibernateTransactionManager txnManager;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private CustomDesignDAO customDesignDAOObj;
    private MessageSource messageSource;
    private AccMainAccountingService accMainAccountingService;
    private AccVendorPaymentModuleService accVendorPaymentModuleServiceObj;
    private accCreditNoteDAO accCreditNoteDAOobj;
    private accCreditNoteService accCreditNoteService;
    private AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAOObj;

    /**
     * @param accVendorPaymentModuleServiceObj the
     * accVendorPaymentModuleServiceObj to set
     */
    public void setAccVendorPaymentModuleServiceObj(AccVendorPaymentModuleService accVendorPaymentModuleServiceObj) {
        this.accVendorPaymentModuleServiceObj = accVendorPaymentModuleServiceObj;
    }
    public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDAOobj) {
        this.accCreditNoteDAOobj = accCreditNoteDAOobj;
    }

    public void setaccCreditNoteService(accCreditNoteService accCreditNoteService) {
        this.accCreditNoteService = accCreditNoteService;
    }
    public void setAccGoodsReceiptServiceDAOObj(AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAOObj) {
        this.accGoodsReceiptServiceDAOObj = accGoodsReceiptServiceDAOObj;
    }
    
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }

    public void setaccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentobj) {
        this.accVendorPaymentobj = accVendorPaymentobj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setaccBankReconciliationDAO(accBankReconciliationDAO accBankReconciliationObj) {
        this.accBankReconciliationObj = accBankReconciliationObj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public void setaccGoodsReceiptCMN(accGoodsReceiptCMN accGoodsReceiptCMN) {
        this.accGoodsReceiptCMN = accGoodsReceiptCMN;
    }

    public void setAccJournalEntryobj(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setAccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setAccCompanyPreferencesObj(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }

    public void setAccPaymentService(accPaymentService accPaymentService) {
        this.accPaymentService = accPaymentService;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setAccGoodsReceiptobj(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }

    public void setcustomDesignDAO(CustomDesignDAO customDesignDAOObj) {
        this.customDesignDAOObj = customDesignDAOObj;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    public void setAccMainAccountingService(AccMainAccountingService accMainAccountingService) {
        this.accMainAccountingService = accMainAccountingService;
    }
    
    public class EnglishNumberToWords {

        private final String[] tensNames = {
            "", " Ten", " Twenty", " Thirty", " Forty", " Fifty", " Sixty", " Seventy", " Eighty", " Ninety"
        };
        private final String[] numNames = {
            "", " One", " Two", " Three", " Four", " Five", " Six", " Seven", " Eight", " Nine", " Ten", " Eleven", " Twelve",
            " Thirteen", " Fourteen", " Fifteen", " Sixteen", " Seventeen", " Eighteen", " Nineteen"
        };

        private String convertLessThanOneThousand(int number) {
            String soFar;
            if (number % 100 < 20) {
                soFar = numNames[number % 100];
                number /= 100;
            } else {
                soFar = numNames[number % 10];
                number /= 10;
                soFar = tensNames[number % 10] + soFar;
                number /= 10;
            }
            if (number == 0) {
                return soFar;
            }
            return numNames[number] + " Hundred" + soFar;
        }

        private String convertLessThanOneThousandWithHypen(int number) {
            String soFar;
            if (number % 100 < 20) {
                soFar = numNames[number % 100];
                number /= 100;
            } else {
                soFar = numNames[number % 10];
                number /= 10;
                if(!soFar.isEmpty()){
                soFar = tensNames[number % 10]+"-"+ soFar.replaceFirst(" ", "");
                } else {
                soFar = tensNames[number % 10] + soFar;
                }
                number /= 10;
            }
            if (number == 0) {
                return soFar;
            }
            return numNames[number] + " Hundred" + soFar;
        } 
        private String convertLessOne(int number, KWLCurrency currency) {
            String soFar;
            String val = currency.getAfterDecimalName();
            if (number % 100 < 20) {
                soFar = numNames[number % 100];
                number /= 100;
            } else {
                soFar = numNames[number % 10];
                number /= 10;
                if(Integer.parseInt(currency.getCurrencyID()) == Constants.CountryUSCurrencyId){
                    soFar = tensNames[number % 10] +"-"+ soFar.replaceFirst(" ", "");
                }else {
                soFar = tensNames[number % 10] + soFar;
                }
                number /= 10;
            }
            if (number == 0) {
                return " And " + soFar + " " + val;
            }
            return " And " + numNames[number] + " " + val + soFar;
        }

        public String convert(Double number, KWLCurrency currency, int countryLanguageId) {
            String answer = "";
            if (number == 0) {
                return "Zero";
            }

            if (countryLanguageId == Constants.OtherCountryLanguageId) { // For universal conversion of amount in words. i.e. in Billion,trillion etc
                answer = universalConvert(number, currency);
            } else if (countryLanguageId == Constants.CountryIndiaLanguageId) { // For Indian word format.ie. in lakhs, crores
                answer = indianConvert(number, currency);
            }
            return answer;
        }

        public String universalConvert(Double number, KWLCurrency currency) {

            boolean isNegative = false;
            if (number < 0) {
                isNegative = true;
                number = -1 * number;
            }
            String snumber = Double.toString(number);
            String mask = "000000000000.00";
            DecimalFormat df = new DecimalFormat(mask);
            snumber = df.format(number);
            int billions = Integer.parseInt(snumber.substring(0, 3));
            int millions = Integer.parseInt(snumber.substring(3, 6));
            int hundredThousands = Integer.parseInt(snumber.substring(6, 9));
            int thousands = Integer.parseInt(snumber.substring(9, 12));
            int fractions = Integer.parseInt(snumber.substring(13, 15));
            String tradBillions;
            switch (billions) {
                case 0:
                    tradBillions = "";
                    break;
                case 1:
                    tradBillions = convertLessThanOneThousand(billions) + " Billion ";
                    break;
                default:
                    tradBillions = convertLessThanOneThousand(billions) + " Billion ";
            }
            String result = tradBillions;

            String tradMillions;
            switch (millions) {
                case 0:
                    tradMillions = "";
                    break;
                case 1:
                    tradMillions = convertLessThanOneThousand(millions) + " Million ";
                    break;
                default:
                    tradMillions = convertLessThanOneThousand(millions) + " Million ";
            }
            result = result + tradMillions;

            String tradHundredThousands;
            switch (hundredThousands) {
                case 0:
                    tradHundredThousands = "";
                    break;
                case 1:
                    tradHundredThousands = "One Thousand ";
                    break;
                default:
                    if (Integer.parseInt(currency.getCurrencyID()) == Constants.CountryUSCurrencyId) {
                        tradHundredThousands = convertLessThanOneThousandWithHypen(hundredThousands) + " Thousand ";
                    } else {
                        tradHundredThousands = convertLessThanOneThousand(hundredThousands) + " Thousand ";
                    }
            }
            result = result + tradHundredThousands;
            String tradThousand;
            if (Integer.parseInt(currency.getCurrencyID()) == Constants.CountryUSCurrencyId) {
                tradThousand = convertLessThanOneThousandWithHypen(thousands);
            } else {
            tradThousand = convertLessThanOneThousand(thousands);
            }
            result = result + tradThousand;
            String paises;
            switch (fractions) {
                case 0:
                    paises = "";
                    break;
                default:
                    paises = convertLessOne(fractions, currency);
            }
            if (Integer.parseInt(currency.getCurrencyID()) == Constants.CountryUSCurrencyId) {
                result = result + " Dollars " + paises;
            } else {
            result = result + paises; //to be done later
            }
            result = result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
            if (isNegative) {
                result = "Minus " + result;
            }
//            result = result.substring(0, 1).toUpperCase() + result.substring(1).toLowerCase(); // Make first letter of operand capital.
            return result;
        }

        public String indianConvert(Double number, KWLCurrency currency) {
            boolean isNegative = false;
            if (number < 0) {
                isNegative = true;
                number = -1 * number;
            }
            String snumber = Double.toString(number);
            String mask = "000000000000000.00";  //ERP-17681
            DecimalFormat df = new DecimalFormat(mask);
            snumber = df.format(number);

            Long n = Long.parseLong(snumber.substring(0, 15));
            int fractions = Integer.parseInt(snumber.split("\\.").length != 0 ? snumber.split("\\.")[1] : "0");
            if (n == 0) {
                return "Zero";
            }
            String arr1[] = {"One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
            String arr2[] = {"Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};
            String unit[] = {"Neel", "Kharab", "Arab", "Crore", "Lakh", "Thousand", "Hundred", ""};
            long factor[] = {100000000000l, 100000000000l, 1000000000, 10000000, 100000, 1000, 100, 1};
            String answer = "", paises = "";
            if (n < 0) {
                answer = "Minus";
                n = -n;
            }
            int quotient, units, tens;
            for (int i = 0; i < factor.length; i++) {
                quotient = (int) (n / factor[i]);
                if (quotient > 0) {
                    if (quotient < 20) {
                        answer = answer + " " + arr1[quotient - 1];
                    } else {
                        units = quotient % 10;
                        tens = quotient / 10;
                        if (units > 0) {
                            answer = answer + " " + arr2[tens - 2] + " " + arr1[units - 1];
                        } else {
                            answer = answer + " " + arr2[tens - 2] + " ";
                        }
                    }
                    answer = answer + " " + unit[i];
                }
                n = n % factor[i];
            }
            switch (fractions) {
                case 0:
                    paises = "";
                    break;
                default:
                    paises = convertLessOne(fractions, currency);
            }
            answer = answer + paises; //to be done later
            return answer.trim();
        }
    }
   
    @Override
    public List<JSONObject> getPaymentsJsonNew(HashMap<String, Object> requestParams, List list, List<JSONObject> jsonlist) throws ServiceException {
        //JSONObject jobj = new JSONObject();
        //JSONArray JArr = new JSONArray();
        try {
            String approvalStatus = "";
            HashMap<String, Object> rRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            String companyid = (String) requestParams.get("companyid");
            String currencyid = (String) requestParams.get("gcurrencyid");
            String vendorEmailId = "";
            String onlyBillingAdddressData = "";

            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            String userName = (String) requestParams.get("userName");
            String userIdForPending = (String) requestParams.get("userid");
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            Boolean isApprovalPendingReport = requestParams.containsKey("ispendingAproval") ? Boolean.parseBoolean(requestParams.get("ispendingAproval").toString()) : false;
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Make_Payment_ModuleId, 1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

            HashMap<String, Object> fieldrequestParamsGlobalLevel = new HashMap();
            HashMap<String, String> customFieldMapGlobalLevel = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMapGlobalLevel = new HashMap<String, String>();
            fieldrequestParamsGlobalLevel.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParamsGlobalLevel.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Make_Payment_ModuleId));
            HashMap<String, String> replaceFieldMapGlobalLevel = new HashMap<String, String>();
            HashMap<String, Integer> FieldMapGlobalLevel = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParamsGlobalLevel, replaceFieldMapGlobalLevel, customFieldMapGlobalLevel, customDateFieldMapGlobalLevel);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat userdf = (DateFormat) requestParams.get(Constants.userdf);
            DateFormat onlyDateDF = userdf;
            if (requestParams.containsKey(Constants.onlydateformat)) {
                onlyDateDF = (DateFormat) requestParams.get(Constants.onlydateformat);
            }
//            DecimalFormat f = new DecimalFormat("##.00");
            Iterator itr = list.iterator();

            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            
            Boolean indiaCheck=(extraCompanyPreferences!=null && extraCompanyPreferences.getCompany().getCountry()!=null)?(extraCompanyPreferences.getCompany().getCountry().getID().equals(""+Constants.indian_country_id)):false; // India Country Check 
            
            while (itr.hasNext()) {

                /*
                 * If you are modifying in this method then you will need to
                 * modify on accReportsController.java - getIBGEntryJson()
                 * method AND on AccReportsServiceImpl.java getPaymentAmount()
                 * method
                 */

                JSONArray jArr1 = new JSONArray();
                Object[] row = (Object[]) itr.next();
                Payment payment = (Payment) row[0];
                Account acc = (Account) row[1];
                JSONObject obj = new JSONObject();
                obj.put("withoutinventory", false);
//                KwlReturnObject vendorresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), acc.getID());
                Customer customer = null;
                boolean isLinkedInvoiceClaimed = false;
                if (payment.getCustomer() != null) {
                    KwlReturnObject custResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), payment.getCustomer());
                    customer = (Customer) custResult.getEntityList().get(0);
                }

                Vendor vendor = payment.getVendor();

                //params to send to get billing address
                HashMap<String, Object> addressParams = new HashMap<String, Object>();
                addressParams.put("companyid", companyid);
                addressParams.put("isDefaultAddress", true); //always true to get defaultaddress
                obj.put("isactive", true);
                if (vendor != null) {
                    addressParams.put("isBillingAddress", true); //true to get billing address
                    addressParams.put("vendorid", vendor.getID());
                    obj.put("address", accountingHandlerDAOobj.getVendorAddress(addressParams));
                    obj.put("personemail", vendor.getEmail());
                    HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                    addrRequestParams.put("vendorid", payment.getVendor().getID());
                    addrRequestParams.put("companyid", companyid);
                    addrRequestParams.put("isBillingAddress", true);
                    addrRequestParams.put("isDefaultAddress", true);
                    obj.put("billaddress", accountingHandlerDAOobj.getVendorAddress(addrRequestParams)); //document designer
                    VendorAddressDetails vendorAddressDetail = accountingHandlerDAOobj.getVendorAddressObj(addrRequestParams);
                    vendorEmailId = vendorAddressDetail != null ? vendorAddressDetail.getEmailID() : "";
                    onlyBillingAdddressData = vendorAddressDetail != null ? vendorAddressDetail.getAddress() : "";
                    obj.put("billingEmail", vendorEmailId);
                    obj.put("onlyBillingAdddressData", onlyBillingAdddressData);//To show address on US check
                    addrRequestParams.put("isBillingAddress", false);
                    obj.put("shipaddress", accountingHandlerDAOobj.getVendorAddress(addrRequestParams)); //document designer
                    obj.put("isactive", vendor.isActivate());
                } else if (payment.getCustomer() != null) {        //document designer
                    HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                    addrRequestParams.put("customerid", customer.getID());
                    addrRequestParams.put("companyid", companyid);
                    addrRequestParams.put("isBillingAddress", true);
                    addrRequestParams.put("isDefaultAddress", true);
                    CustomerAddressDetails customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addrRequestParams);
                    String customerEmailId = customerAddressDetails != null ? customerAddressDetails.getEmailID() : "";
                    onlyBillingAdddressData = customerAddressDetails != null ? customerAddressDetails.getAddress(): "";
                    obj.put("billingEmail", customerEmailId);
                    obj.put("onlyBillingAdddressData", onlyBillingAdddressData);//To show address on US check
                    obj.put("billaddress", accountingHandlerDAOobj.getCustomerAddress(addrRequestParams));//document designer
                    obj.put("shipaddress", accountingHandlerDAOobj.getCustomerAddress(addrRequestParams));//document designer
                    obj.put("isactive", customer.isActivate());
                } else {
                    obj.put("address", "");
                    obj.put("personemail", "");
                    obj.put("billaddress", "");//document designer
                    obj.put("shipaddress", "");//document designer
                }
//                Vendor vendor = (Vendor) session.get(Vendor.class, acc.getID());
//                if(vendor!=null)
//                   obj.put("address", vendor.getAddress());
//                        obj.put("address", vendor.getAddress());
                String jeNumber = payment.getJournalEntry().getEntryNumber();
                String jeIds = payment.getJournalEntry().getID();
                String jeIdEntryDate = onlyDateDF.format(payment.getJournalEntry().getEntryDate());
                if (payment.getJournalEntryForBankCharges() != null) {
                    jeNumber += "," + payment.getJournalEntryForBankCharges().getEntryNumber();
                    jeIds += "," + payment.getJournalEntryForBankCharges().getID();
                    jeIdEntryDate += "," + onlyDateDF.format(payment.getJournalEntryForBankCharges().getEntryDate());
                }
                if (payment.getJournalEntryForBankInterest() != null) {
                    jeNumber += "," + payment.getJournalEntryForBankInterest().getEntryNumber();
                    jeIds += "," + payment.getJournalEntryForBankInterest().getID();
                    jeIdEntryDate += "," + onlyDateDF.format(payment.getJournalEntryForBankInterest().getEntryDate());
                }
                HashMap<String, Object> map = new HashMap<>();
                map.put("companyid", payment.getCompany().getCompanyID());
                map.put("badDebtType", 0);
                if (payment.getLinkDetailPayments() != null && !payment.getLinkDetailPayments().isEmpty()) {
                    Set<LinkDetailPayment> linkedDetailPayList = payment.getLinkDetailPayments();
                    for (LinkDetailPayment ldprow : linkedDetailPayList) {
                        if (!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                            jeIds += "," + ldprow.getLinkedGainLossJE();
                            KwlReturnObject linkedje = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), ldprow.getLinkedGainLossJE());
                            JournalEntry linkedJEObject = (JournalEntry) linkedje.getEntityList().get(0);
                            jeNumber += "," + linkedJEObject.getEntryNumber();
                            jeIdEntryDate += "," + onlyDateDF.format(linkedJEObject.getEntryDate());
                        }
                        // Checking whether at least one invoice linked to payment is claimed. If found, system will not allow to delete/edit such payment.
                        if (ldprow.getGoodsReceipt().getBadDebtType() == 1 || ldprow.getGoodsReceipt().getBadDebtType() == 2) {
                            map.put("invoiceid", ldprow.getGoodsReceipt().getID());
                            KwlReturnObject result = accGoodsReceiptobj.getBadDebtPurchaseInvoiceMappingForGoodsReceipt(map);
                            List<BadDebtPurchaseInvoiceMapping> maplist = result.getEntityList();
                            if (maplist != null && !maplist.isEmpty()) {
                                BadDebtPurchaseInvoiceMapping mapping = maplist.get(0);
//                                if (!isLinkedInvoiceClaimed && mapping.getBadDebtClaimedDate().getTime() > payment.getJournalEntry().getEntryDate().getTime()) {
                                if (!isLinkedInvoiceClaimed && mapping.getBadDebtClaimedDate().getTime() > payment.getCreationDate().getTime()) {
                                    isLinkedInvoiceClaimed = true;
                                    obj.put("isLinkedInvoiceIsClaimed", isLinkedInvoiceClaimed);
                                }
                            }
                        }
                    }
                }
                if (payment.getLinkDetailPaymentToCreditNote() != null && !payment.getLinkDetailPaymentToCreditNote().isEmpty()) {
                    Set<LinkDetailPaymentToCreditNote> linkDetailPmtToCn = payment.getLinkDetailPaymentToCreditNote();
                    for (LinkDetailPaymentToCreditNote ldprow : linkDetailPmtToCn) {
                        if (!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                            jeIds += "," + ldprow.getLinkedGainLossJE();
                            KwlReturnObject linkedje = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), ldprow.getLinkedGainLossJE());
                            JournalEntry linkedJEObject = (JournalEntry) linkedje.getEntityList().get(0);
                            jeNumber += "," + linkedJEObject.getEntryNumber();
                            jeIdEntryDate += "," + onlyDateDF.format(linkedJEObject.getEntryDate());
                        }
                    }
                }
                if (payment.getLinkDetailPaymentsToAdvancePayment() != null && !payment.getLinkDetailPaymentsToAdvancePayment().isEmpty()) {
                    Set<LinkDetailPaymentToAdvancePayment> linkDetailPmtToAdv = payment.getLinkDetailPaymentsToAdvancePayment();
                    for (LinkDetailPaymentToAdvancePayment ldprow : linkDetailPmtToAdv) {
                        if (!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                            jeIds += "," + ldprow.getLinkedGainLossJE();
                            KwlReturnObject linkedje = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), ldprow.getLinkedGainLossJE());
                            JournalEntry linkedJEObject = (JournalEntry) linkedje.getEntityList().get(0);
                            jeNumber += "," + linkedJEObject.getEntryNumber();
                            jeIdEntryDate += "," + onlyDateDF.format(linkedJEObject.getEntryDate());
                        }
                    }
                }
                // Checking whether at least one invoice linked to payment is claimed. If found, system will not allow to delete/edit such payment.
                if (payment.getRows() != null && !payment.getRows().isEmpty()) {
                    for (PaymentDetail detail : payment.getRows()) {
                        if (detail.getGoodsReceipt().getBadDebtType() == 1 || detail.getGoodsReceipt().getBadDebtType() == 2) {
                            map.put("invoiceid", detail.getGoodsReceipt().getID());
                            KwlReturnObject result = accGoodsReceiptobj.getBadDebtPurchaseInvoiceMappingForGoodsReceipt(map);
                            List<BadDebtPurchaseInvoiceMapping> maplist = result.getEntityList();
                            if (maplist != null && !maplist.isEmpty()) {
                                BadDebtPurchaseInvoiceMapping mapping = maplist.get(0);
//                                if (!isLinkedInvoiceClaimed && mapping.getBadDebtClaimedDate().getTime() > payment.getJournalEntry().getEntryDate().getTime()) {
                                if (!isLinkedInvoiceClaimed && mapping.getBadDebtClaimedDate().getTime() > payment.getCreationDate().getTime()) {
                                    isLinkedInvoiceClaimed = true;
                                    obj.put("isLinkedInvoiceIsClaimed", isLinkedInvoiceClaimed);
                                }
                            }
                        }
                    }
                }
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("invoiceID", payment.getID());
                hashMap.put("companyid", payment.getCompany().getCompanyID());
                KwlReturnObject object = accVendorPaymentobj.getinvoiceDocuments(hashMap);
                int attachemntcount = object.getRecordTotalCount();
                obj.put("attachment", attachemntcount);
                obj.put("ischequeprinted", payment.isChequeprinted());
                obj.put("billid", payment.getID());
                obj.put("companyid", payment.getCompany().getCompanyID());
                obj.put("companyname", payment.getCompany().getCompanyName());
                obj.put("entryno", jeNumber);
                obj.put("journalentryid", jeIds);
                obj.put("journalentrydate", jeIdEntryDate);
                obj.put("isadvancepayment", payment.isIsadvancepayment());
                obj.put("ismanydbcr", payment.isIsmanydbcr());
                obj.put("isprinted", payment.isPrinted());
                obj.put("isEmailSent", payment.isIsEmailSent());
                obj.put("bankCharges", payment.getBankChargesAmount());
                obj.put("bankChargesCmb", payment.getBankChargesAccount() != null ? payment.getBankChargesAccount().getID() : "");
                obj.put("bankChargesAccCode", payment.getBankChargesAccount() != null ? payment.getBankChargesAccount().getAcccode() != null ? payment.getBankChargesAccount().getAcccode() : "" : "");
                obj.put("bankChargesAccName", payment.getBankChargesAccount() != null ? payment.getBankChargesAccount().getName() != null ? payment.getBankChargesAccount().getName() : "" : "");
                obj.put("bankInterestAccCode", payment.getBankInterestAccount() != null ? payment.getBankInterestAccount().getAcccode() != null ? payment.getBankInterestAccount().getAcccode() : "" : "");
                obj.put("bankInterestAccName", payment.getBankInterestAccount() != null ? payment.getBankInterestAccount().getName() != null ? payment.getBankInterestAccount().getName() : "" : "");
                obj.put("bankInterest", payment.getBankInterestAmount());
                obj.put("bankInterestCmb", payment.getBankInterestAccount() != null ? payment.getBankInterestAccount().getID() : "");
                obj.put("paidToCmb", payment.getPaidTo() == null ? "" : payment.getPaidTo().getID());
                obj.put("paidto", payment.getPaidTo() != null ? payment.getPaidTo().getValue() : "");  //to show the paid to option in grid
                obj.put(Constants.SEQUENCEFORMATID, payment.getSeqformat() == null ? "" : payment.getSeqformat().getID());
                boolean advanceUsed = false;
                if (payment.getAdvanceid() != null && !payment.getAdvanceid().isDeleted()) {
                    rRequestParams.clear();
                    filter_names.clear();
                    filter_params.clear();
                    filter_names.add("payment.ID");
                    filter_params.add(payment.getAdvanceid().getID());
                    rRequestParams.put("filter_names", filter_names);
                    rRequestParams.put("filter_params", filter_params);
                    KwlReturnObject grdresult = accVendorPaymentobj.getPaymentDetails(rRequestParams);
                    advanceUsed = grdresult.getEntityList().size() > 0 ? true : false;
                }
                Payment paymentObject = null;
                if (payment.getInvoiceAdvCndnType() == 2 || payment.getInvoiceAdvCndnType() == 1) {
                    paymentObject = accVendorPaymentobj.getPaymentObject(payment);
                    if (paymentObject != null) {
                        obj.put("cndnid", paymentObject.getID());
                    }
                } else if (payment.getInvoiceAdvCndnType() == 3) {
                    obj.put("cndnid", payment.getID());
                }
                obj.put("invoiceadvcndntype", payment.getInvoiceAdvCndnType());
                obj.put("cndnAndInvoiceId", !StringUtil.isNullOrEmpty(payment.getCndnAndInvoiceId()) ? payment.getCndnAndInvoiceId() : "");

                obj.put("advanceUsed", advanceUsed);
                obj.put("advanceid", (payment.getAdvanceid() != null && !payment.getAdvanceid().isDeleted()) ? payment.getAdvanceid().getID() : "");
                obj.put("advanceamount", payment.getAdvanceamount());
                obj.put("receipttype", payment.getReceipttype());
                obj.put("paymentwindowtype", payment.getPaymentWindowType());
                obj.put("personid", (vendor != null) ? vendor.getID() : acc.getID());
//                obj.put("customervendorname", (vendor!=null)? vendor.getName() : (customer!=null)? customer.getName():"");
                obj.put("billno", payment.getPaymentNumber());
                JSONObject jObj = extraCompanyPreferences.getColumnPref() != null ? new JSONObject(extraCompanyPreferences.getColumnPref()) : new JSONObject();
                boolean isPostingDateCheck = false;
                if (!StringUtil.isNullObject(jObj) && jObj.has(Constants.IS_POSTING_DATE_CHECK) && jObj.get(Constants.IS_POSTING_DATE_CHECK) != null && jObj.optBoolean(Constants.IS_POSTING_DATE_CHECK, false)) {
                    isPostingDateCheck = true;
                }
                if (isPostingDateCheck) {
                    obj.put("billdate", df.format(payment.getCreationDate()));//receiptdate
                    obj.put("billdateinUserFormat", userdf.format(payment.getCreationDate()));//receiptdate
                } else {
                    obj.put("billdate", df.format(payment.getJournalEntry().getEntryDate()));//receiptdate
                    obj.put("billdateinUserFormat", userdf.format(payment.getJournalEntry().getEntryDate()));//receiptdate
                }

                rRequestParams.clear();
                filter_names.clear();
                filter_params.clear();
                filter_names.add("payment.ID");
                filter_params.add(payment.getID());
                rRequestParams.put("filter_names", filter_names);
                rRequestParams.put("filter_params", filter_params);
                KwlReturnObject pdoresult = accVendorPaymentobj.getPaymentDetailOtherwise(rRequestParams);
                List<PaymentDetailOtherwise> list1 = pdoresult.getEntityList();
                Iterator pdoRow = list1.iterator();

                Iterator itrRow = payment.getRows().iterator();
                double amount = 0, totaltaxamount = 0;
                double linkedAmountDue = 0, amountDueInBase = 0, totaltdsamount = 0;
                if(indiaCheck){
                totaltdsamount = accVendorPaymentModuleServiceObj.getTotalTDSAmount(payment);
                }
                obj.put("totaltdsamount", authHandler.formattedAmount(totaltdsamount, companyid));
                obj.put("disableOtherwiseLinking", true);
                String recordsHavingAdvancePaymentsAsRefund = "";
                int receiptsCountHavingAdvancePaymentsAsRefund = 0;
                if (payment.getAdvanceDetails() != null && !payment.getAdvanceDetails().isEmpty()) {
                    for (AdvanceDetail advanceDetail : payment.getAdvanceDetails()) {
                        linkedAmountDue += advanceDetail.getAmountDue();
//                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, advanceDetail.getAmountDue(), payment.getCurrency().getCurrencyID(), payment.getJournalEntry().getEntryDate(), payment.getJournalEntry().getExternalCurrencyRate());
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, advanceDetail.getAmountDue(), payment.getCurrency().getCurrencyID(), payment.getCreationDate(), payment.getJournalEntry().getExternalCurrencyRate());
                        amountDueInBase += (Double) bAmt.getEntityList().get(0);
                        if (linkedAmountDue <= 0) {
                            obj.put("disableOtherwiseLinking", true);
                        } else {
                            obj.put("disableOtherwiseLinking", false);
                        }

                        if (payment.getVendor() != null) {
                            List<Object[]> resultList = accVendorPaymentobj.getAdvancePaymentUsedInRefundReceipt(advanceDetail.getId());
                            for (int i = 0; i < resultList.size(); i++) {
                                Object[] objArray = (Object[]) resultList.get(i);
                                String receiptNumber = objArray[0].toString();
                                recordsHavingAdvancePaymentsAsRefund += "#" + receiptNumber + ",";
                                receiptsCountHavingAdvancePaymentsAsRefund++;
                            }
                            resultList = accVendorPaymentobj.getAdvancePaymentLinkedWithRefundReceipt(payment.getID(), companyid);
                            for (int i = 0; i < resultList.size(); i++) {
                                Object[] objArray = (Object[]) resultList.get(i);
                                String receiptNumber = objArray[1].toString();
                                recordsHavingAdvancePaymentsAsRefund += "#" + receiptNumber + ",";
                                receiptsCountHavingAdvancePaymentsAsRefund++;
                            }
                        }
                    }
                }
                if (receiptsCountHavingAdvancePaymentsAsRefund != 0) {
                    obj.put("isAdvancePaymentUsedAsRefund", true);
                    recordsHavingAdvancePaymentsAsRefund = recordsHavingAdvancePaymentsAsRefund.substring(0, recordsHavingAdvancePaymentsAsRefund.length() - 1);
                } else {
                    obj.put("isAdvancePaymentUsedAsRefund", false);
                }
                obj.put("recordsHavingAdvancePaymentsAsRefund", recordsHavingAdvancePaymentsAsRefund);
                
                // 'isRefundTransaction' is used to identify transaction is for refund
                if (payment.getPaymentWindowType() == 2 && payment.getCustomer() != null && payment.getAdvanceDetails() != null && !payment.getAdvanceDetails().isEmpty()) {
                    obj.put("isRefundTransaction", true);
                    // 'personid' need to fetch advance payment transaction in case of linking to refund transaction
                    obj.put("personid", (customer != null) ? customer.getID() : acc.getID());
                } else {
                    obj.put("isRefundTransaction", false);
                }
//                /*
//                 * Invoice used in Make payment.
//                 */
//                if (!payment.getRows().isEmpty()) { 
//                    amount = payment.getDepositAmount();
//                    obj.put("isLinked", true);
//                } else if (pdoRow != null && list1.size() > 0) { 
//                    /*
//                     * Make Payment Against GL
//                     */
//                    amount = payment.getDepositAmount();
//                    obj.put("isLinked", false);
//                }else if(!payment.getCreditNotePaymentDetails().isEmpty()){
//                    /*
//                     * Credit Note used in Make Payment
//                     */
//                    amount = payment.getDepositAmount();
//                    obj.put("isLinked", true);
//                }else {
//                    /*
//                     * Make Payment Against Advance payment
//                     */
//                    amount = payment.getDepositAmount();
//                    obj.put("isLinked", false);
//                }
                
                if (!payment.getRows().isEmpty()) { // Payment Details - Against Invoice
                    amount = payment.getDepositAmount();
                    obj.put("otherwise", false);
                } else if (pdoRow != null && list1.size() > 0) { // Payment Details Otherwise case
                    amount = payment.getDepositAmount();
                    obj.put("otherwise", true);
                } else {
                    amount = payment.getDepositAmount();
                    obj.put("otherwise", true);
                }
                
                /*
                 * If Make payment is link to credit note,invoice,advance payment.
                 */
                boolean isTDSApplied = false;
                if (payment.getAdvanceDetails() != null && !payment.getAdvanceDetails().isEmpty()) { // Payment Details - Against Invoice
                    Set<AdvanceDetail> advances = payment.getAdvanceDetails();
                    for(AdvanceDetail advance : advances){
                        if(advance.getTdsdetails() != null && advance.getTdsdetails().size() > 0){
                            isTDSApplied = true;
                        }
                    }
                    if ((payment.getLinkDetailPayments() != null && !payment.getLinkDetailPayments().isEmpty()) || (payment.getLinkDetailPaymentToCreditNote() != null && !payment.getLinkDetailPaymentToCreditNote().isEmpty()) || (payment.getLinkDetailPaymentsToAdvancePayment() != null && !payment.getLinkDetailPaymentsToAdvancePayment().isEmpty())) {
                        obj.put("isLinked", true);
                    } else {
                        obj.put("isLinked", false);
                    }
                }
                if (payment.getBankChargesAmount() > 0 && payment.getJournalEntryForBankCharges() == null) {
                    amount -= payment.getBankChargesAmount();
                }
                if (payment.getBankInterestAmount() > 0 && payment.getJournalEntryForBankInterest() == null) {
                    amount -= payment.getBankInterestAmount();
                }
                
                amount+=totaltdsamount;  // ERP-28527 :TDS consider as Direct tax so it include in total amount.
                amount = authHandler.round(amount, companyid);
                obj.put("amount", authHandler.formattedAmount(amount, companyid));
                obj.put("amountInWords", (payment.getCurrency() == null ? currency.getName() : payment.getCurrency().getName()) + " " + EnglishNumberToWordsOjb.convert(amount, (payment.getCurrency() == null ? currency : payment.getCurrency()), countryLanguageId) + " Only.");
                /*
                 * If Advance Payment mark as Dishonoured  then amount due set as 0
                 */
                if (payment.getAdvanceDetails() != null && !payment.getAdvanceDetails().isEmpty()&&payment.isIsDishonouredCheque()) {
                    obj.put("paymentamountdue", authHandler.formattedAmount(0, companyid));
                    obj.put("paymentamountdueinbase", authHandler.formattedAmount(0, companyid));
                }else{
                    obj.put("paymentamountdue", authHandler.formattedAmount(linkedAmountDue, companyid));
                    obj.put("paymentamountdueinbase", authHandler.formattedAmount(amountDueInBase, companyid));
                }


                String paycurrencyid = (payment.getCurrency() == null ? currency.getCurrencyID() : payment.getCurrency().getCurrencyID());
//                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, paycurrencyid, payment.getJournalEntry().getEntryDate(), payment.getJournalEntry().getExternalCurrencyRate());
                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, paycurrencyid, payment.getCreationDate(), payment.getJournalEntry().getExternalCurrencyRate());
                double amountinbase = (Double) bAmt.getEntityList().get(0);
                obj.put("amountinbase", authHandler.formattedAmount(amountinbase, companyid));


                KwlReturnObject result = accVendorPaymentobj.getPaymentVendorNames(companyid, payment.getID());
                List vNameList = result.getEntityList();
                Iterator vNamesItr = vNameList.iterator();
                String vendorNames = "";
                while (vNamesItr.hasNext()) {
                    String tempName = URLEncoder.encode((String) vNamesItr.next(), "UTF-8");
                    vendorNames += tempName;
                    vendorNames += ",";
                }
                vendorNames = vendorNames.substring(0, Math.max(0, vendorNames.length() - 1));
                vendorNames = StringUtil.DecodeText(vendorNames);
                obj.put("personname",  (vendor == null && customer == null) ? vendorNames : ((vendor != null) ? vendor.getName() : (customer != null) ? customer.getName() : ""));    //Used decoder to avoid '+' symbol at white/empty space between words. 
                obj.put("personcode",  (vendor == null && customer == null) ? "" : ((vendor != null) ? (vendor.getAcccode() == null ? "" : vendor.getAcccode()) : (customer != null) ? (customer.getAcccode() == null ? "" : customer.getAcccode()) : ""));    //Used decoder to avoid '+' symbol at white/empty space between words. 
                obj.put("accountcode",  (vendor == null && customer == null) ? "" : ((vendor != null) ? (vendor.getAccount() == null ? "" : (vendor.getAccount().getAccountCode() == null) ? "" : vendor.getAccount().getAccountCode()) : (customer != null) ? (customer.getAccount() == null ? "" : (customer.getAccount().getAccountCode()) == null ? "" : customer.getAccount().getAccountCode()) : ""));
                obj.put("accountname",  (vendor == null && customer == null) ? "" : ((vendor != null) ? (vendor.getAccount() == null ? "" : (vendor.getAccount().getAccountName() == null) ? "" : vendor.getAccount().getAccountName()) : (customer != null) ? (customer.getAccount() == null ? "" : (customer.getAccount().getAccountName() == null) ? "" : customer.getAccount().getAccountName()) : ""));

                if (extraCompanyPreferences != null && extraCompanyPreferences.getCompany().getCountry().getID().equals(String.valueOf(Constants.indian_country_id))) {
                    //To Check payment number is allow to COPY or not.
                    int istaxpaid = accVendorPaymentobj.isFromTaxPayment(companyid, payment.getPaymentNumber());
                    if (istaxpaid > 0) {
                        obj.put("AllowToEditCopy", false);
                    } else {
                        obj.put("AllowToEditCopy", true);
                    }
                    if(payment.getAdvanceDetails() != null ){
                       for(AdvanceDetail AdvObj : payment.getAdvanceDetails()){//As Payment Object contains only one Advance Detail Object.
                           obj.put("IsTDSAmtUsedInGoodsReceipt", AdvObj.isIstdsamountusedingoodsreceipt());
                       }
                    }
                    //To Check Whether Current Payment is TDS Payment or not, If so then donot allow to copy.
                    HashMap<String, Object> requestmap = new HashMap<String, Object>();
                    requestmap.put("companyID", payment.getCompany() != null ? payment.getCompany().getCompanyID() : "");
                    requestmap.put("paymentID", payment.getID());
                    int isFromTDSPaymentCnt = accVendorPaymentobj.isFromTDSPayment(requestmap);
                    if (isFromTDSPaymentCnt > 0) {
                        obj.put("tdsPaymentJsonFlag", true);
                    } else {
                        obj.put("tdsPaymentJsonFlag", false);
                    }
                    obj.put("rcmApplicable", payment.isRcmApplicable());
                    obj.put("advanceToVendor", payment.isAdvanceToVendor());
                }
                //refer ticket ERP-10777
                addressParams.put("isBillingAddress", true);    //true to get billing address
                if (vendor == null && customer == null) {
                    obj.put("personaddress",  StringUtil.DecodeText(""));
                } else if (vendor != null) {
                    addressParams.put("vendorid", vendor.getID());
                    obj.put("personaddress",  StringUtil.DecodeText(accountingHandlerDAOobj.getVendorAddress(addressParams)));    //Used decoder to avoid '+' symbol at white/empty space between words. 
                } else {
                    addressParams.put("customerid", customer.getID());
                    obj.put("personaddress",  StringUtil.DecodeText(accountingHandlerDAOobj.getCustomerAddress(addressParams)));    //Used decoder to avoid '+' symbol at white/empty space between words.
                }

                if (customer != null) {
                    addressParams.put("customerid", customer.getID());
                } else if (vendor != null) {
                    addressParams.put("vendorid", vendor.getID());
                }
                obj.put("personaddresswithPostalcode",  StringUtil.DecodeText((vendor == null && customer == null) ? "" : ((vendor != null) ? (accountingHandlerDAOobj.getTotalVendorAddress(addressParams)) : (customer != null) ? (accountingHandlerDAOobj.getTotalCustomerAddress(addressParams)) : "")));    //for document designer Used decoder to avoid '+' symbol at white/empty space between words. 
                obj.put("createdby", payment.getCreatedby() == null ? "" : StringUtil.getFullName(payment.getCreatedby()));
                obj.put("memo", payment.getMemo());
                if (payment.isIsDishonouredCheque()) {
                    obj.put("dishonoured","Cancelled/Dishonored");
                }
                obj.put("cinno", payment.getCinNo());
                obj.put("tdsApplicable", isTDSApplied?"Yes":"No");// Value will be set after provious code is done - India Compliance
                obj.put("challanGenerated", "No");// Value will be set after provious code is done - India Compliance 
                obj.put("deleted", payment.isDeleted());
                obj.put("currencysymbol", (payment.getCurrency() == null ? currency.getSymbol() : payment.getCurrency().getSymbol()));
                if (payment.getExternalCurrencyRate() == 0) {
//                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, 1.0d, payment.getCurrency().getCurrencyID(), payment.getJournalEntry().getEntryDate(), payment.getJournalEntry().getExternalCurrencyRate());
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, 1.0d, payment.getCurrency().getCurrencyID(), payment.getCreationDate(), payment.getJournalEntry().getExternalCurrencyRate());
                    obj.put("externalcurrencyrate", 1 / (Double) bAmt.getEntityList().get(0));
                } else {
                    obj.put("externalcurrencyrate", payment.getExternalCurrencyRate());
                }
                obj.put("paymentmethod", (payment.getPayDetail() == null ? "" : payment.getPayDetail().getPaymentMethod().getMethodName()));
                obj.put("paymentaccount", (payment.getPayDetail() == null ? "" : payment.getPayDetail().getPaymentMethod().getAccount().getName()));//Document Designer
                obj.put("paymentaccountid", (payment.getPayDetail() == null ? "" : payment.getPayDetail().getPaymentMethod().getAccount().getID()));
                obj.put("paymentaccountnumber", (payment.getPayDetail() == null ? "" : payment.getPayDetail().getPaymentMethod().getAccount().getName()));//Document Designer
                obj.put("paymentaccountcode", (payment.getPayDetail() == null ? "" : payment.getPayDetail().getPaymentMethod().getAccount().getAcccode()));//Document Designer
                obj.put("chequenumber", payment.getPayDetail() == null ? "" : (payment.getPayDetail().getPaymentMethod().getDetailType() == PaymentMethod.TYPE_BANK) ? (payment.getPayDetail().getCheque() != null ? payment.getPayDetail().getCheque().getChequeNo() : "") : "");
                obj.put("chequesequenceformatid", payment.getPayDetail() == null ? "" : (payment.getPayDetail().getPaymentMethod().getDetailType() == PaymentMethod.TYPE_BANK) ? (payment.getPayDetail().getCheque() != null ? (payment.getPayDetail().getCheque().getSeqformat()!=null?payment.getPayDetail().getCheque().getSeqformat().getId():"") : "") : "");
                obj.put("chequedate", payment.getPayDetail() == null ? "" : (payment.getPayDetail().getPaymentMethod().getDetailType() == PaymentMethod.TYPE_BANK) ? (payment.getPayDetail().getCheque() != null ? userdf.format(payment.getPayDetail().getCheque().getDueDate()) : "") : "");
                obj.put("chequedateforprint", payment.getPayDetail() == null ? "" : (payment.getPayDetail().getPaymentMethod().getDetailType() == PaymentMethod.TYPE_BANK) ? (payment.getPayDetail().getCheque() != null ? df.format(payment.getPayDetail().getCheque().getDueDate()) : "") : "");
                obj.put("chequedescription", payment.getPayDetail() == null ? "" : (payment.getPayDetail().getPaymentMethod().getDetailType() == PaymentMethod.TYPE_BANK) ? (payment.getPayDetail().getCheque() != null ? (payment.getPayDetail().getCheque().getDescription() != null ? payment.getPayDetail().getCheque().getDescription() : "") : "") : "");
                obj.put("payee", payment.getPayee());
                obj.put("paymentWithSalesReturn", (payment.getSalesReturn() == null ? "" : payment.getSalesReturn().isIsPayment()));
                obj.put("exciseunit", payment.getExciseunit());
                obj.put("currencyid", (payment.getCurrency() == null ? currency.getCurrencyID() : payment.getCurrency().getCurrencyID()));
                obj.put("currencycode", (payment.getCurrency() == null ? currency.getCurrencyCode() : payment.getCurrency().getCurrencyCode()));
                obj.put("currencyname", (payment.getCurrency() == null ? currency.getName() : payment.getCurrency().getName()));
                obj.put("currencysymbol", (payment.getCurrency() == null ? (currency.getSymbol() == null ? "" : currency.getSymbol()) : (payment.getCurrency().getSymbol() == null ? "" : payment.getCurrency().getSymbol())));
                obj.put("methodid", (payment.getPayDetail() == null ? "" : payment.getPayDetail().getPaymentMethod().getID()));
                obj.put("detailtype", (payment.getPayDetail() == null ? "" : payment.getPayDetail().getPaymentMethod().getDetailType()));
                if (isApprovalPendingReport) {
                    obj = accPaymentService.getMakePaymentApprovalPendingJsonData(obj, amountinbase, payment.getApprovestatuslevel(), companyid, userIdForPending, userName);
                } else {
                   if ( payment.getApprovestatuslevel() == 11) {
                        obj.put("approvalstatusinfo", "Approved");
                    }
                }

                if (payment.getPayDetail() != null) {
                    try {
                        obj.put("expirydate", (payment.getPayDetail().getCard() == null ? "" : df.format(payment.getPayDetail().getCard().getExpiryDate())));
                    } catch (IllegalArgumentException ae) {
                        obj.put("expirydate", "");
                    }
                    obj.put("refdetail", (payment.getPayDetail().getCard() == null ? (payment.getPayDetail().getCheque() == null ? "" : payment.getPayDetail().getCheque().getDescription()) : payment.getPayDetail().getCard().getCardType()));

//                if (payment.getPayDetail() != null) {
                    obj.put("refno", (payment.getPayDetail().getCard() == null ? (payment.getPayDetail().getCheque() == null ? "" : payment.getPayDetail().getCheque().getChequeNo()) : payment.getPayDetail().getCard().getRefNo()));
                    obj.put("refname", (payment.getPayDetail().getCard() == null ? (payment.getPayDetail().getCheque() == null ? "" : payment.getPayDetail().getCheque().getBankName()) : payment.getPayDetail().getCard().getCardHolder()));
                    obj.put("bankname", (payment.getPayDetail().getCard() == null ? (payment.getPayDetail().getCheque() == null ? "" : (payment.getPayDetail().getCheque().getBankMasterItem() == null ? payment.getPayDetail().getCheque().getBankName() : payment.getPayDetail().getCheque().getBankMasterItem().getValue())) : payment.getPayDetail().getCard().getCardHolder()));
                    if (payment.getPayDetail().getCard() != null) {
                        obj.put("refcardno", payment.getPayDetail().getCard().getCardNo());
//                        obj.put("refexpdate", payment.getPayDetail().getCard().getExpiryDate());
                    }
//                }
                }
                obj.put("clearancedate", "");
                obj.put("paymentstatus", false);
                if (payment.getPayDetail() != null) {
                    KwlReturnObject clearanceDate = accBankReconciliationObj.getBRfromJE(payment.getJournalEntry().getID(), payment.getCompany().getCompanyID(), false);
                    if (clearanceDate != null && clearanceDate.getEntityList() != null && clearanceDate.getEntityList().size() > 0) {
                        BankReconciliationDetail brd = (BankReconciliationDetail) clearanceDate.getEntityList().get(0);
                        if (brd.getBankReconciliation().getClearanceDate() != null) {
                            obj.put("clearancedate", df.format(brd.getBankReconciliation().getClearanceDate()));
                            obj.put("paymentstatus", true);
                        }
                    }
                }

                if (payment.isIBGTypeTransaction()) {
                    if (payment.getCimbreceivingbankdetails() != null) {               // Details for CIMB bank
                        obj.put("ibgDetailsID", payment.getCimbreceivingbankdetails().getId());
                    } else if (payment.getIbgreceivingbankdetails() != null) {       // Details for DBS bank
                        obj.put("isIBGTypeTransaction", payment.isIBGTypeTransaction());
                        obj.put("ibgDetailsID", payment.getIbgreceivingbankdetails().getId());
                        obj.put("ibgCode", payment.getIbgCode());
                    }
                }
                Set<PaymentDetailOtherwise> paymentDetailsOtherwise = payment.getPaymentDetailOtherwises();
                for (PaymentDetailOtherwise PDO : paymentDetailsOtherwise) { // Tax amount of payment against GL is added.
                    if (PDO.getTaxamount() != 0) {
//                        totaltaxamount += PDO.getTaxamount();
                        if (PDO.isIsdebit()) {
                            totaltaxamount += PDO.getTaxamount();
                        } else {
                            totaltaxamount = totaltaxamount - PDO.getTaxamount();
                        }
                    }
                }
                totaltaxamount = authHandler.round(totaltaxamount, companyid);
                obj.put("totaltaxamount", authHandler.formattedAmount(totaltaxamount, companyid));
                obj.put("amountBeforeTax", authHandler.formattedAmount((amount - totaltaxamount), companyid));

                RepeatedPayment repeatedpayment = payment.getRepeatedPayment();
                obj.put("isRepeated", repeatedpayment == null ? false : true);
                if (repeatedpayment != null) {
                    obj.put("repeateid", repeatedpayment.getId());
                    obj.put("interval", repeatedpayment.getIntervalUnit());
                    obj.put("intervalType", repeatedpayment.getIntervalType());
                    SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
                    obj.put("NoOfPaymentpost", repeatedpayment.getNoOfpaymentspost());
                    obj.put("NoOfRemainPaymentpost", repeatedpayment.getNoOfRemainpaymentspost());
                    obj.put("startDate", sdf.format(repeatedpayment.getStartDate()));
                    obj.put("nextDate", sdf.format(repeatedpayment.getNextDate()));
                    obj.put("expireDate", repeatedpayment.getExpireDate() == null ? "" : sdf.format(repeatedpayment.getExpireDate()));
                    obj.put("isactivate", repeatedpayment.isIsActivate());
                    obj.put("ispendingapproval", repeatedpayment.isIspendingapproval());
                    obj.put("approver", repeatedpayment.getApprover());
                    requestParams.put("parentPaymentId", payment.getID());
                    KwlReturnObject details = accVendorPaymentobj.getRepeatePaymentDetails(requestParams);
                    List detailsList = details.getEntityList();
                    obj.put("childCount", detailsList.size());
                }
                obj.put("repeateid", payment.getRepeatedPayment() != null ? payment.getRepeatedPayment().getId() : "");
                obj.put("parentid", payment.getParentPayment()!= null ? payment.getParentPayment().getID(): "");
                if (payment.getRepeatedPayment() != null && payment.getPaymentWindowType() == Constants.Make_Payment_against_GL_Code) {
                    obj.put("chequeOption", payment.getRepeatedPayment().isAutoGenerateChequeNumber());
                } else {
                    obj.put("chequeOption", false);
                }
                obj.put("isDishonouredCheque", payment.isIsDishonouredCheque());
                Map<String, Object> variableMap = new HashMap<String, Object>();
                HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                Detailfilter_names.add("companyid");
                Detailfilter_params.add(payment.getCompany().getCompanyID());
                Detailfilter_names.add("journalentryId");
                Detailfilter_params.add(payment.getJournalEntry().getID());
                Detailfilter_names.add("moduleId");
                Detailfilter_params.add(Constants.Acc_Make_Payment_ModuleId + "");
                invDetailRequestParams.put("filter_names", Detailfilter_names);
                invDetailRequestParams.put("filter_params", Detailfilter_params);
                KwlReturnObject idcustresult = accVendorPaymentobj.getVendorPaymentGlobalCustomData(invDetailRequestParams);
                if (idcustresult.getEntityList().size() > 0) {
                    AccJECustomData jeCustom = (AccJECustomData) idcustresult.getEntityList().get(0);
                    AccountingManager.setCustomColumnValues(jeCustom, FieldMapGlobalLevel, replaceFieldMapGlobalLevel, variableMap);
                    for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                        String coldata = varEntry.getValue().toString();
                        if (customFieldMapGlobalLevel.containsKey(varEntry.getKey())) {
                            //   boolean isExport = (request.getAttribute("isExport") == null) ? false : true;
                            String value = "";
                            String Ids[] = coldata.split(",");
                            for (int i = 0; i < Ids.length; i++) {
                                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), Ids[i]);
                                FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                if (fieldComboData != null) {
                                    if (fieldComboData.getField().getFieldtype() == 12 || fieldComboData.getField().getFieldtype() == 7) {
                                        value += Ids[i] != null ? Ids[i] + "," : ",";
                                    } else {
                                        value += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                                        obj.put("Dimension_" + fieldComboData.getField().getFieldlabel(), fieldComboData.getValue() != null ? fieldComboData.getValue() : ""); //to differentiate custom field and dimension in sms payment templates.
                                    }

                                }
                            }
                            if (!StringUtil.isNullOrEmpty(value)) {
                                value = value.substring(0, value.length() - 1);
                            }
                            obj.put(varEntry.getKey(), value);
                        } else if (customDateFieldMapGlobalLevel.containsKey(varEntry.getKey())) {
                            obj.put(varEntry.getKey(), coldata);
                        } else {
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                obj.put(varEntry.getKey(), coldata);
                            }
                        }
                    }
                }
                jsonlist.add(obj);
            }
            //jobj.put("data", JArr);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getPaymentsJson : " + ex.getMessage(), ex);
        }
        return jsonlist;
    }

    /**
     * Description : Below Method is used to fetch TDS Rates in given Period.
     * @param (HashMap<String, Object> requestParams) used to get request parameters 
     * @return JSONArray of TDS Rates
     * @throws ServiceException
     */
    public JSONArray getTDSMasterRates(HashMap<String, Object> requestParams) throws ServiceException {
        JSONArray JArr = new JSONArray();
        JSONObject jSONObject = new JSONObject();
        try {
            List<MasterItem> listNOP = accCommonTablesDAO.getMasterItemsForNatureOfPayment(requestParams);
            if (listNOP != null) {
                for (MasterItem row : listNOP) {
                    jSONObject.put(row.getDefaultMasterItem().getID(), row.getID());
                }
            }
            List<TDSRate> list = accCommonTablesDAO.getTDSMasterRates(requestParams);
            if (list != null ) {
                for (TDSRate row : list) {
                    JSONObject obj = new JSONObject();
                    if (!StringUtil.isNullOrEmpty(row.getNatureofpayment())) {
                        KwlReturnObject masterItemResult = accountingHandlerDAOobj.getObject(DefaultMasterItem.class.getName(), row.getNatureofpayment());
                        DefaultMasterItem masterItem = (DefaultMasterItem) masterItemResult.getEntityList().get(0);
                        if (masterItem != null) {
                            obj.put("natureofpayment", masterItem.getCode() +" - "+masterItem.getValue());
                        }
                        if (jSONObject.length()>0 && jSONObject.has(row.getNatureofpayment())) {
                            obj.put("natureofpaymentid", jSONObject.getString(row.getNatureofpayment()));
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(row.getDeducteetype())) {
                        KwlReturnObject masterItemResult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), row.getDeducteetype());
                        MasterItem masterItem = (MasterItem) masterItemResult.getEntityList().get(0);
                        if (masterItem != null) {
                            obj.put("deducteetype", masterItem.getValue());
                        }
                        obj.put("deducteetypeid", row.getDeducteetype());
                    }
                    if (!StringUtil.isNullOrEmpty(row.getResidentialstatus())) {
                        if (row.getResidentialstatus().equalsIgnoreCase("0")) {
                            obj.put("residentialstatus", "Resident");
                        } else if (row.getResidentialstatus().equalsIgnoreCase("1")) {
                            obj.put("residentialstatus", "Non-Resident");
                        }
                        obj.put("residentialstatusid", row.getResidentialstatus());
                    }
                    obj.put("tdsratefromdate", row.getFromdate());
                    obj.put("tdsratetodate", row.getTodate());
                    obj.put("tdsrate", row.getRate());
                    obj.put("basicexemptionpertransaction", row.getBasicexemptionpertransaction());
                    obj.put("basicexemptionperannum", row.getBasicexemptionperannum());
                    obj.put("tdsid", row.getId());
                    obj.put("tdsrateifpannotavailable", row.getTdsrateifpannotavailable());
                    obj.put("fromamount", row.getFromamount());
                    obj.put("toamount", row.getToamount());
                    JArr.put(obj);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentController.getPaymentRowsJson : " + ex.getMessage(), ex);
        }
        return JArr;
    }
    
    /**
     * Description : Below Method is used to delete TDS Rates in given Period.
     * @param requestParams used to get request parameters 
     * @return number of rows Deleted
     * @throws ServiceException
     * @throws com.krawler.hql.accounting.AccountingException
     */
    @Override
    public int deleteTDSMasterRates(HashMap<String, Object> requestParams) throws ServiceException ,AccountingException{
        boolean isValidToDelete = true;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Delete_RSO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        int deleteCnt = 0;
        try {
            status = txnManager.getTransaction(def);
            String tdsID = "";
            String companyid = (String) requestParams.get(Constants.companyKey);
            JSONArray dataArray = new JSONArray((String) requestParams.get("data"));
            ArrayList<String> resList = new ArrayList<String>();
            HashMap<String, String> hmData = new HashMap<String, String>();
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject obj = dataArray.getJSONObject(i);
                resList.add("'" + obj.getString("tdsid") + "'");
            }
            if (!resList.isEmpty()) {
                tdsID = StringUtils.collectionToCommaDelimitedString(resList);
                if (!StringUtil.isNullOrEmpty(tdsID)) {
                    hmData.put("tdsID", tdsID);
                    hmData.put("companyid", companyid);
                    //Function To check whether selected TDS Master Rate Record(s) is used in Transaction(Advance Payment).
                    KwlReturnObject Advancecount = accVendorPaymentobj.ISTDSMasterRatesUsedInAdvancePayment(hmData);
                    if (Advancecount.getRecordTotalCount() > 0) {
                        isValidToDelete = false;
                        throw new AccountingException("Selected record(s) is currently used in the transaction(s). So it cannot be deleted.");
                    } else {
                        //Function To check whether selected TDS Master Rate Record(s) is used in Transaction(Advance Payment).
                        KwlReturnObject PIcount = accVendorPaymentobj.ISTDSMasterRatesUsedInPI(hmData);
                        if (PIcount.getRecordTotalCount() > 0) {
                            isValidToDelete = false;
                            throw new AccountingException("Selected record(s) is currently used in the transaction(s). So it cannot be deleted.");
                        }
                    }
                    if (isValidToDelete) {//Delete Master Rate Record(s).
                        hmData.put("tdsID", tdsID);
                        KwlReturnObject deleterowscount = accVendorPaymentobj.deleteTDSMasterRates(hmData);
                        deleteCnt = deleterowscount.getRecordTotalCount();
                        if (deleteCnt > 0) {
                        }
                    }
                }
                txnManager.commit(status);
            }
        } catch (TransactionException | JSONException | NumberFormatException | ServiceException | NoSuchMessageException ex) {
            txnManager.rollback(status);
            Logger.getLogger(AccVendorPaymentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("AccVendorPaymentServiceImpl: deleteTDSMasterRates " + ex.getMessage(), ex);
        }
        return deleteCnt;
    }
    
    public JSONArray getPaymentDetailJsonNew(HashMap<String, Object> requestParams) throws ServiceException {
        JSONArray JArr = new JSONArray();
        try {
            String companyid = (String) requestParams.get("companyid");
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            double taxPercent = 0;
            DateFormat df = (DateFormat) requestParams.get("dateformat");
            DateFormat userdf = (DateFormat) requestParams.get("userdateformat");
            String[] payment = null;
            if (requestParams.containsKey("bills")) {
                payment = (String[]) requestParams.get("bills");
            }else if (requestParams.containsKey("billid") && requestParams.get("billid")!= null && !StringUtil.isNullOrEmpty(requestParams.get("billid").toString())) {
                payment = (requestParams.get("billid").toString()).split(",");
            }
//            boolean isVendorPaymentEdit = Boolean.parseBoolean((String) requestParams.get("isReceiptEdit"));
            int i = 0;
            HashMap<String, Object> pRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("payment.ID");
            order_by.add("srno");
            order_type.add("asc");
            pRequestParams.put("filter_names", filter_names);
            pRequestParams.put("filter_params", filter_params);
            pRequestParams.put("order_by", order_by);
            pRequestParams.put("order_type", order_type);
            JSONArray jArr = new JSONArray();
            while (payment != null && i < payment.length) {
//                Payment re = (Payment) session.get(Payment.class, payment[i]);
                JSONArray innerJArr = new JSONArray();
                KwlReturnObject presult = accountingHandlerDAOobj.getObject(Payment.class.getName(), payment[i]);
                Payment re = (Payment) presult.getEntityList().get(0);
//                Iterator itr = re.getRows().iterator();
                filter_params.clear();
                filter_params.add(re.getID());
                KwlReturnObject grdresult = accVendorPaymentobj.getPaymentDetails(pRequestParams);
                Iterator itr = grdresult.getEntityList().iterator();
                
                HashMap<String, Object> fieldrequestParams = new HashMap();
                HashMap<String, String> customFieldMap = new HashMap<String, String>();
                HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                HashMap<String, String> customRichTextMap = new HashMap<String, String>();
                HashMap<String, Integer> customRefColMap = new HashMap<String, Integer>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(re.getCompany().getCompanyID(), Constants.Acc_Make_Payment_ModuleId, 1));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap,customRichTextMap,customRefColMap);

                getPaymentDetailArray(re, innerJArr, currency, taxPercent, customFieldMap, FieldMap, replaceFieldMap, itr, requestParams, customDateFieldMap,customRichTextMap);
                JSONObject jSONObject = new JSONObject();
                if (innerJArr.length() != 0) {
                    jSONObject.put("type", Constants.PaymentAgainstInvoice);
                    jSONObject.put("billid", re.getID());
                    jSONObject.put("paymentWindowType", re.getPaymentWindowType());
                    jSONObject.put("typedata", innerJArr);
                    jSONObject.put("srNoForRow",innerJArr.getJSONObject(0).optString("srNoForRow"));
                    JArr.put(jSONObject);
                }
                JSONArray innerJArrGL = new JSONArray();
                getPaymentGLArray(re, innerJArrGL, currency, taxPercent, customFieldMap, customDateFieldMap, FieldMap, replaceFieldMap,requestParams,customRichTextMap);
                JSONObject jSONObjectGL = new JSONObject();
                if (innerJArrGL.length() != 0) {
                    jSONObjectGL.put("type", Constants.GLPayment);
                    jSONObjectGL.put("billid", re.getID());
                    jSONObjectGL.put("paymentWindowType", re.getPaymentWindowType());
                    jSONObjectGL.put("typedata", innerJArrGL);
                    jSONObjectGL.put("srNoForRow",innerJArrGL.getJSONObject(0).optString("srNoForRow"));
                    JArr.put(jSONObjectGL);
                }
                JSONArray innerJArrCNDN = new JSONArray();
                getPaymentCNDNArray(re, innerJArrCNDN, currency, requestParams, customFieldMap, customDateFieldMap, FieldMap, replaceFieldMap,customRichTextMap);
                JSONObject jSONObjectCNDN = new JSONObject();
                if (innerJArrCNDN.length() != 0) {
                    jSONObjectCNDN.put("type", Constants.PaymentAgainstCNDN);
                    jSONObjectCNDN.put("billid", re.getID());
                    jSONObjectCNDN.put("paymentWindowType", re.getPaymentWindowType());
                    jSONObjectCNDN.put("typedata", innerJArrCNDN);
                    jSONObjectCNDN.put("srNoForRow",innerJArrCNDN.getJSONObject(0).optString("srNoForRow"));
                    JArr.put(jSONObjectCNDN);
                }
                JSONArray innerJArrAdvance = new JSONArray();
                getPaymentAdvanceArray(re, innerJArrAdvance, currency, customFieldMap, customDateFieldMap, FieldMap, replaceFieldMap, requestParams,customRichTextMap);
                JSONObject jSONObjectAdvance = new JSONObject();
                if (innerJArrAdvance.length() != 0) {
                    jSONObjectAdvance.put("type", Constants.AdvancePayment);
                    jSONObjectAdvance.put("billid", re.getID());
                    jSONObjectAdvance.put("paymentWindowType", re.getPaymentWindowType());
                    jSONObjectAdvance.put("typedata", innerJArrAdvance);
                    jSONObjectAdvance.put("srNoForRow",innerJArrAdvance.getJSONObject(0).optString("srNoForRow"));
                    JArr.put(jSONObjectAdvance);
                }
                JSONArray innerJArrLinkedAdvance = new JSONArray();
                getPaymentLinkArray(re, innerJArrLinkedAdvance, currency, taxPercent, customFieldMap, FieldMap, replaceFieldMap, itr, requestParams, customDateFieldMap,customRichTextMap);
                JSONObject jSONObjectLinkedAdvance = new JSONObject();
                if (innerJArrLinkedAdvance.length() != 0) {
                    jSONObjectLinkedAdvance.put("type", Constants.AdvanceLinkedWithInvoicePayment);
                    jSONObjectLinkedAdvance.put("billid", re.getID());
                    jSONObjectLinkedAdvance.put("paymentWindowType", re.getPaymentWindowType());
                    jSONObjectLinkedAdvance.put("typedata", innerJArrLinkedAdvance);
                    jSONObjectLinkedAdvance.put("srNoForRow",innerJArrLinkedAdvance.getJSONObject(0).optString("srNoForRow"));
                    JArr.put(jSONObjectLinkedAdvance);
                }
                JSONArray innerJArrLinkedAdvanceToCN = new JSONArray();
                getPaymentLinkToCreditNoteArray(re, innerJArrLinkedAdvanceToCN, currency, df, userdf, companyid);
                JSONObject jSONObjectLinkedAdvanceToCN = new JSONObject();
                if (innerJArrLinkedAdvanceToCN.length() != 0) {
                    jSONObjectLinkedAdvanceToCN.put("type", Constants.AdvanceLinkedWithNotePayment);
                    jSONObjectLinkedAdvanceToCN.put("billid", re.getID());
                    jSONObjectLinkedAdvanceToCN.put("paymentWindowType", re.getPaymentWindowType());
                    jSONObjectLinkedAdvanceToCN.put("typedata", innerJArrLinkedAdvanceToCN);
                    jSONObjectLinkedAdvanceToCN.put("srNoForRow",innerJArrLinkedAdvanceToCN.getJSONObject(0).optString("srNoForRow"));
                    JArr.put(jSONObjectLinkedAdvanceToCN);
                }
                // for getting refund linking details against advance payment
                JSONArray innerJArrLinkedRefundAgainstAdvPayment = new JSONArray();
                getPaymentLinkToAdvancePaymentArray(re, innerJArrLinkedRefundAgainstAdvPayment, currency, df, userdf, companyid);
                JSONObject jSONObjectLinkedRefundAdvPayment = new JSONObject();
                if (innerJArrLinkedRefundAgainstAdvPayment.length() != 0) {
                    jSONObjectLinkedRefundAdvPayment.put("type", Constants.RefundPaymentAgainstAdvancePayment);
                    jSONObjectLinkedRefundAdvPayment.put("billid", re.getID());
                    jSONObjectLinkedRefundAdvPayment.put("paymentWindowType", re.getPaymentWindowType());
                    jSONObjectLinkedRefundAdvPayment.put("typedata", innerJArrLinkedRefundAgainstAdvPayment);
                    jSONObjectLinkedRefundAdvPayment.put("srNoForRow",innerJArrLinkedRefundAgainstAdvPayment.getJSONObject(0).optString("srNoForRow"));
                    JArr.put(jSONObjectLinkedRefundAdvPayment);
                }
                i++;
            }
            JSONArray sortedArray = new JSONArray();
            sortedArray = accPaymentService.sortJson(JArr);
            if (sortedArray.length() == JArr.length()) {
                JArr = sortedArray;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentController.getPaymentRowsJson : " + ex.getMessage(), ex);
        }
        return JArr;
    }
    /**
     * Description : Below Method is used to fetch Advance Payment Details against Vendor.
     * @param (HashMap<String, Object> requestParams) used to get request parameters 
     * @return JSONArray of AdvanceDetails of Payment.
     * @throws ServiceException
     */ 
    public JSONArray getAdvanceDetailsAgainstVendor(HashMap<String, Object> requestParams) throws ServiceException {
        JSONArray JArr = new JSONArray();
        try {
            String goodsReceiptID = "";
            boolean isEdit = false;
            String UsedAdvancePaymentIDs = "";
            List<AdvanceDetail> AdvdetailsList = accVendorPaymentobj.getAdvanceDetailsAgainstVendorForTDS(requestParams);
            for(AdvanceDetail advanceObj : AdvdetailsList){
                Set<TdsDetails> tdsDetails = advanceObj.getTdsdetails();
                String natureofpayment = "";
                for (TdsDetails tdsList : tdsDetails) {
                    if (tdsList.getNatureOfPayment()==null) {
                        continue;
                    }
                    natureofpayment = tdsList.getNatureOfPayment().getID();
                }
                if (StringUtil.isNullOrEmpty(natureofpayment)) {
                    continue;
                }
                JSONObject Advjobj = new JSONObject();
                Advjobj.put("natureofpayment", natureofpayment);
                Advjobj.put("AdvanceAmount",advanceObj.getAmount());
                Advjobj.put("AdvanceAmountDue",advanceObj.getAmountDue());
                Advjobj.put("AdvancePaymentID",advanceObj.getPayment().getID());
                Advjobj.put("AdvancePaymentNumber",advanceObj.getPayment().getPaymentNumber());
                Advjobj.put("AdvanceDetailID",advanceObj.getId());
                Advjobj.put("AdvanceTDSAmount",advanceObj.getTdsamount());
                Advjobj.put("isTDSAmountUsedInGR",advanceObj.isIstdsamountusedingoodsreceipt());
                JArr.put(Advjobj);
            }
            if (requestParams.containsKey("isEdit")) {
                isEdit = (boolean) requestParams.get("isEdit");
            }
            if (requestParams.containsKey("goodsReceiptID")) {
                goodsReceiptID = (String) requestParams.get("goodsReceiptID");
            }
            //For Edit case In PI, to get used Advance Payment IDs (For TDS) if any.
            if (isEdit && !StringUtil.isNullOrEmpty(goodsReceiptID)) {
                Map<String, Object> GoodsreceiptParams = new HashMap<String, Object>();
                GoodsreceiptParams.put("goodsreceiptid", goodsReceiptID);
                GoodsreceiptParams.put("isEdit", isEdit);
                GoodsreceiptParams.put("companyid", requestParams.get("companyid"));
                GoodsreceiptParams.put("vendorid", requestParams.get("vendorid"));
                KwlReturnObject result = accGoodsReceiptobj.getAdvancePaymentDetailsUsedInGoodsReceipt(GoodsreceiptParams);   //while deleting GR check wether it is used in Consignment Cost
                List<AdvanceDetail> usedAdvanceList = result.getEntityList();
                for (AdvanceDetail advanceObj : usedAdvanceList) {
                    JSONObject Advjobj = new JSONObject();
                    Set<TdsDetails> tdsDetails= advanceObj.getTdsdetails();
                    String natureofpayment="";
                    for(TdsDetails tdsList:tdsDetails){
                        natureofpayment = tdsList.getNatureOfPayment().getID();
                    }
                    if(StringUtil.isNullOrEmpty(natureofpayment)){
                        continue;
                    }
                    Advjobj.put("natureofpayment", natureofpayment);
                    Advjobj.put("AdvanceAmount", advanceObj.getAmount());
                    Advjobj.put("AdvanceAmountDue", advanceObj.getAmountDue());
                    Advjobj.put("AdvancePaymentID", advanceObj.getPayment().getID());
                    UsedAdvancePaymentIDs += advanceObj.getPayment().getID() + ",";
                    Advjobj.put("AdvancePaymentNumber", advanceObj.getPayment().getPaymentNumber());
                    Advjobj.put("AdvanceDetailID", advanceObj.getId());
                    Advjobj.put("AdvanceTDSAmount", advanceObj.getTdsamount());
                    Advjobj.put("isTDSAmountUsedInGR", advanceObj.isIstdsamountusedingoodsreceipt());
                    JArr.put(Advjobj);
                }
                JSONObject Advjobj = new JSONObject();
                if (!StringUtil.isNullOrEmpty(UsedAdvancePaymentIDs)) { // Handle Empty and null case 
                    Advjobj.put("UsedAdvancePaymentIDs", UsedAdvancePaymentIDs.substring(0, UsedAdvancePaymentIDs.length() - 1));
                    JArr.put(Advjobj);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentController.getPaymentRowsJson : " + ex.getMessage(), ex);
        }
        return JArr;
    }

    private void getPaymentDetailArray(Payment re, JSONArray innerJArr, KWLCurrency currency, double taxPercent, HashMap<String, String> customFieldMap, HashMap<String, Integer> FieldMap, HashMap<String, String> replaceFieldMap, Iterator itr, HashMap requestParams, HashMap<String, String> customDateFieldMap, HashMap<String, String> customRichTextMap) throws SessionExpiredException, ServiceException {
        try {
            String companyid = (String) requestParams.get("companyid");
            DateFormat df = (DateFormat) requestParams.get("dateformat");
            DateFormat userdf = (DateFormat) requestParams.get("userdateformat");
            boolean isVendorPaymentEdit = Boolean.parseBoolean((String) requestParams.get("isReceiptEdit"));
            boolean isExport = false;
            boolean isForReport = false;
            if (requestParams.containsKey(Constants.isExport) && requestParams.get(Constants.isExport) != null) {
                isExport = Boolean.parseBoolean(requestParams.get(Constants.isExport).toString());
            }
            if (requestParams.containsKey(Constants.isForReport) && requestParams.get(Constants.isForReport) != null) {
                isForReport = Boolean.parseBoolean(requestParams.get(Constants.isForReport).toString());
            }
            while (itr.hasNext()) {
                PaymentDetail row = (PaymentDetail) itr.next();
                Date grCreationDate = null;
                double grAmount = 0d;
                if (row.getGoodsReceipt().isNormalInvoice()) {
//                    grCreationDate = row.getGoodsReceipt().getJournalEntry().getEntryDate();
                    grAmount = isVendorPaymentEdit ? row.getGoodsReceipt().getVendorEntry().getAmount() : row.getAmount();
                } else {// for opening balance inoices
                    grAmount = isVendorPaymentEdit ? row.getGoodsReceipt().getOriginalOpeningBalanceAmount() : row.getAmount();
                }
                grCreationDate = row.getGoodsReceipt().getCreationDate();
                JSONObject obj = new JSONObject();
//                double exchangeRateForTransaction = row.getExchangeRateForTransaction() != 0 ? row.getExchangeRateForTransaction() : 1;
                obj.put("billid", isVendorPaymentEdit ? row.getGoodsReceipt().getID() : re.getID());
                obj.put("isopening", row.getGoodsReceipt().isIsOpeningBalenceInvoice());
                obj.put("srno", row.getSrno());
                obj.put("srnoforrow", row.getSrNoForRow());
                obj.put("srNoForRow", row.getSrNoForRow());
                obj.put("rowid", row.getID());
                obj.put("transectionno", row.getGoodsReceipt().getGoodsReceiptNumber());
                obj.put("supplierinvoiceno", row.getGoodsReceipt().getSupplierInvoiceNo());
                obj.put("transectionid", row.getGoodsReceipt().getID());
                obj.put("amount", grAmount);
                obj.put("enteramount", row.getAmount());
                obj.put("description", row.getDescription());//document designer
//                obj.put("discountAmount", authHandler.round((row.getDiscountAmount() / exchangeRateForTransaction), companyid));
                obj.put("discountAmount", row.getDiscountAmountInInvoiceCurrency());
//                obj.put("linkingdate", df.format(row.getPayment().getJournalEntry().getEntryDate()));
                obj.put("linkingdate", df.format(row.getPayment().getCreationDate()));
                obj.put("accountid", (isVendorPaymentEdit ? row.getGoodsReceipt().getAccount().getID() : ""));
                obj.put("type", Constants.PaymentAgainstInvoice);
                if (row.getGoodsReceipt() != null) {
                    obj.put("currencyidtransaction", row.getGoodsReceipt().getCurrency() == null ? (row.getPayment().getCurrency() == null ? currency.getCurrencyID() : row.getPayment().getCurrency().getCurrencyID()) : row.getGoodsReceipt().getCurrency().getCurrencyID());
                    obj.put("currencysymbol", row.getGoodsReceipt().getCurrency() == null ? (row.getPayment().getCurrency() == null ? currency.getSymbol() : row.getPayment().getCurrency().getSymbol()) : row.getGoodsReceipt().getCurrency().getSymbol());
                    obj.put("currencysymboltransaction", row.getGoodsReceipt().getCurrency() == null ? (row.getPayment().getCurrency() == null ? currency.getSymbol() : row.getPayment().getCurrency().getSymbol()) : row.getGoodsReceipt().getCurrency().getSymbol());
                } else {
                    obj.put("currencyidtransaction", (row.getPayment().getCurrency() == null ? currency.getCurrencyID() : row.getPayment().getCurrency().getCurrencyID()));
                    obj.put("currencysymbol", (row.getPayment().getCurrency() == null ? currency.getSymbol() : row.getPayment().getCurrency().getSymbol()));
                    obj.put("currencysymboltransaction", (row.getPayment().getCurrency() == null ? currency.getSymbol() : row.getPayment().getCurrency().getSymbol()));
                }
                obj.put("duedate", df.format(row.getGoodsReceipt().getDueDate()));
                obj.put("creationdate", df.format(grCreationDate));
                obj.put("dateforsort", grCreationDate);
                obj.put("creationdateinuserformat", userdf.format(grCreationDate));
//                    JArr.put(obj);
                if (row.getGoodsReceipt().getTax() != null) {
//                    KwlReturnObject perresult = accTaxObj.getTaxPercent(row.getCompany().getCompanyID(), row.getGoodsReceipt().getJournalEntry().getEntryDate(), row.getGoodsReceipt().getTax().getID());
                    KwlReturnObject perresult = accTaxObj.getTaxPercent(row.getCompany().getCompanyID(), row.getGoodsReceipt().getCreationDate(), row.getGoodsReceipt().getTax().getID());
                    taxPercent = (Double) perresult.getEntityList().get(0);
                }
                obj.put("taxpercent", taxPercent);
                obj.put("discount", row.getGoodsReceipt().getDiscount() == null ? row.getGoodsReceipt().getDiscountAmount() : row.getGoodsReceipt().getDiscount().getDiscountValue());
                obj.put("payment", row.getGoodsReceipt().getID());
                obj.put("gstCurrencyRate", row.getGstCurrencyRate());
                double rowAmount = (authHandler.round(row.getAmount(), companyid));
                if (isVendorPaymentEdit) {
                    obj.put("amountpaid", rowAmount);
                    if (row.getFromCurrency() != null && row.getToCurrency() != null) {
                        obj.put("exchangeratefortransaction", row.getExchangeRateForTransaction());
//                            obj.put("amountpaidincurrency", authHandler.round(rowAmount/row.getExchangeRateForTransaction(), Constants.AMOUNT_DIGIT_AFTER_DECIMAL));
                        if (row.getAmountInGrCurrency() != 0) {
                            obj.put("amountpaidincurrency", authHandler.round(row.getAmountInGrCurrency(), companyid));
                        } else {
                            obj.put("amountpaidincurrency", authHandler.round(rowAmount / row.getExchangeRateForTransaction(), companyid));
                        }
                    } else {
                        double amount = rowAmount;
                        String fromcurrencyid = (row.getPayment().getCurrency() == null ? currency.getCurrencyID() : row.getPayment().getCurrency().getCurrencyID());
                        String tocurrencyid = (row.getGoodsReceipt().getCurrency() == null ? (row.getPayment().getCurrency() == null ? currency.getCurrencyID() : row.getPayment().getCurrency().getCurrencyID()) : row.getGoodsReceipt().getCurrency().getCurrencyID());
                        double exchangeRate = row.getGoodsReceipt().isIsOpeningBalenceInvoice() ? row.getGoodsReceipt().getExternalCurrencyRate() : row.getGoodsReceipt().getJournalEntry().getExternalCurrencyRate();
//                        Date tranDate = row.getGoodsReceipt().isIsOpeningBalenceInvoice() ? row.getGoodsReceipt().getCreationDate() : row.getGoodsReceipt().getJournalEntry().getEntryDate();
                        Date tranDate = row.getGoodsReceipt().getCreationDate();
                        KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherRoundOff(requestParams, amount, fromcurrencyid, tocurrencyid, tranDate, exchangeRate);
                        amount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                        obj.put("exchangeratefortransaction", amount / rowAmount);
                        obj.put("amountpaidincurrency", amount);
                    }
                } else {
                    if (row.getFromCurrency() != null && row.getToCurrency() != null) {
//                            obj.put("amountpaid", rowAmount/row.getExchangeRateForTransaction());
                        if (row.getAmountInGrCurrency() != 0) {
                            obj.put("amountpaid", authHandler.round(row.getAmountInGrCurrency(), companyid));
                            obj.put("exchangeratefortransaction", row.getExchangeRateForTransaction());//documetn designer
                        } else {
                            obj.put("amountpaid", authHandler.round(rowAmount / row.getExchangeRateForTransaction(), companyid));
                        }
                    } else {
                        double amount = rowAmount;
                        String fromcurrencyid = (row.getPayment().getCurrency() == null ? currency.getCurrencyID() : row.getPayment().getCurrency().getCurrencyID());
                        String tocurrencyid = (row.getGoodsReceipt().getCurrency() == null ? (row.getPayment().getCurrency() == null ? currency.getCurrencyID() : row.getPayment().getCurrency().getCurrencyID()) : row.getGoodsReceipt().getCurrency().getCurrencyID());
                        double exchangeRate = row.getGoodsReceipt().isIsOpeningBalenceInvoice() ? row.getGoodsReceipt().getExternalCurrencyRate() : row.getGoodsReceipt().getJournalEntry().getExternalCurrencyRate();
//                        Date tranDate = row.getGoodsReceipt().isIsOpeningBalenceInvoice() ? row.getGoodsReceipt().getCreationDate() : row.getGoodsReceipt().getJournalEntry().getEntryDate();
                        Date tranDate = row.getGoodsReceipt().getCreationDate();
                        KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherRoundOff(requestParams, amount, fromcurrencyid, tocurrencyid, tranDate, exchangeRate);
                        amount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                        obj.put("amountpaid", amount);
                    }
                }
                requestParams.put("isVendorPaymentEdit", isVendorPaymentEdit);
                double amountdue = 0.0, totalamount = 0.0, amountDueOriginal = 0.0;
                if (row.getGoodsReceipt().isNormalInvoice()) {
                    List ll;
                    if (row.getGoodsReceipt().isIsExpenseType()) {
                        ll = accGoodsReceiptCMN.getExpGRAmountDue(requestParams, row.getGoodsReceipt());
                        amountDueOriginal = (ll.isEmpty() ? 0 : (Double) ll.get(4));
                    } else {
                        if (Constants.InvoiceAmountDueFlag) {
                            ll = accGoodsReceiptCMN.getInvoiceDiscountAmountInfo(requestParams, row.getGoodsReceipt());
                            amountDueOriginal = (ll.isEmpty() ? 0 : (Double) ll.get(5));
                        } else {
                            ll = accGoodsReceiptCMN.getGRAmountDue(requestParams, row.getGoodsReceipt());
                            amountDueOriginal = (ll.isEmpty() ? 0 : (Double) ll.get(5));
                        }
                    }
                    amountdue = (ll.isEmpty() ? 0 : (Double) ll.get(1));
                    totalamount = row.getGoodsReceipt().getVendorEntry().getAmount();
                } else {
                    //amountdue = row.getGoodsReceipt().getOpeningBalanceAmountDue()*(row.getExchangeRateForTransaction()==0?1:row.getExchangeRateForTransaction());
                    amountDueOriginal = row.getGoodsReceipt().getOpeningBalanceAmountDue();
                    amountdue = amountDueOriginal;
                    totalamount = row.getGoodsReceipt().getOriginalOpeningBalanceAmount();
                }
                amountdue = authHandler.round(amountdue, companyid);
                amountDueOriginal = authHandler.round(amountDueOriginal, companyid);
                totalamount = authHandler.round(totalamount, companyid);

                if (row.getFromCurrency() != null && row.getToCurrency() != null) {
                    obj.put("amountduenonnegative", (isVendorPaymentEdit ? row.getAmountDueInPaymentCurrency() : amountDueOriginal));
                } else {
                    obj.put("amountduenonnegative", (isVendorPaymentEdit ? row.getAmountDueInPaymentCurrency() : amountdue));
                }
                obj.put("amountDueOriginal", (isVendorPaymentEdit ? row.getAmountDueInGrCurrency() : amountDueOriginal));
                obj.put("amountDueOriginalSaved", (isVendorPaymentEdit ? row.getAmountDueInGrCurrency() : amountDueOriginal));
                obj.put("totalamount", totalamount);
                obj.put("amountdue", amountdue);
                // ## Get Custom Field Data 
                Map<String, Object> variableMap = new HashMap<String, Object>();
                HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                Detailfilter_params.add(row.getID());
                invDetailRequestParams.put("filter_names", Detailfilter_names);
                invDetailRequestParams.put("filter_params", Detailfilter_params);
                JSONArray dimensionArr = new JSONArray();
                KwlReturnObject idcustresult = accVendorPaymentobj.getVendorPaymentCustomData(invDetailRequestParams);
                if (idcustresult.getEntityList().size() > 0) {
                    AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                    AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);

                    if (jeDetailCustom != null) {
                        JSONObject params = new JSONObject();
                        params.put(Constants.isExport, isExport);
                        params.put(Constants.isForReport, isForReport);
                        params.put(Constants.userdf, userdf);
                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params,customRichTextMap);
                    }
                }                
                //TDS Fields
                obj.put(CustomDesignerConstants.TDS_RATE, row.getGoodsReceipt().getTdsRate());
                obj.put(CustomDesignerConstants.TDS_AMOUNT, row.getGoodsReceipt().getTdsAmount());
                
                obj.put("dimensionArr", dimensionArr);
                innerJArr.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentController.getPaymentRowsJson : " + ex.getMessage(), ex);
        }

    }

    private void getPaymentGLArray(Payment re, JSONArray innerJArrGL, KWLCurrency currency, double taxPercent, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap, HashMap<String, Integer> FieldMap, HashMap<String, String> replaceFieldMap,HashMap<String,Object> requestParams, HashMap<String, String> customRichTextMap) throws SessionExpiredException, ServiceException {
        try {
            // Comparator for sorting GL records as per sequence in UI
            Set<PaymentDetailOtherwise> paymentDetailOtherwises = new TreeSet<PaymentDetailOtherwise>(new Comparator<PaymentDetailOtherwise>() {
                @Override
                public int compare(PaymentDetailOtherwise MP1, PaymentDetailOtherwise MP2) {
                    if (MP1.getSrNoForRow() > MP2.getSrNoForRow()) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });
            DateFormat udf = (DateFormat) requestParams.get("userdateformat");
            paymentDetailOtherwises.addAll(re.getPaymentDetailOtherwises());
            
            for (PaymentDetailOtherwise paymentDetailOtherwise : paymentDetailOtherwises) {
                JSONObject obj = new JSONObject();
                obj.put("rowid", paymentDetailOtherwise.getID());
                obj.put("srnoforrow", paymentDetailOtherwise.getSrNoForRow());
                obj.put("srNoForRow", paymentDetailOtherwise.getSrNoForRow());
                obj.put("accountid", paymentDetailOtherwise.getAccount().getID());
                obj.put("accountname", paymentDetailOtherwise.getAccount().getName());
                obj.put("debit", paymentDetailOtherwise.isIsdebit());  // it is used mainly for Many CN/DN
                obj.put("accountcode", paymentDetailOtherwise.getAccount().getAcccode() == null ? "" : paymentDetailOtherwise.getAccount().getAcccode());
                taxPercent = 0.0;
                if (paymentDetailOtherwise.getTax() != null) {
//                    KwlReturnObject perresult = accTaxObj.getTaxPercent(re.getCompany().getCompanyID(), re.getJournalEntry().getEntryDate(), paymentDetailOtherwise.getTax().getID());
                    KwlReturnObject perresult = accTaxObj.getTaxPercent(re.getCompany().getCompanyID(), re.getCreationDate(), paymentDetailOtherwise.getTax().getID());
                    taxPercent = (Double) perresult.getEntityList().get(0);
                }
                obj.put("taxpercent", taxPercent);
                obj.put("description", paymentDetailOtherwise.getDescription());
                obj.put("totalamount", paymentDetailOtherwise.getAmount());
                obj.put("currencysymbol", paymentDetailOtherwise.getPayment().getCurrency().getSymbol());
                obj.put("currencyid", paymentDetailOtherwise.getPayment().getCurrency().getCurrencyID());
                obj.put("taxamount", paymentDetailOtherwise.getTaxamount());
                if (paymentDetailOtherwise.getTax() != null) {
                    obj.put("taxname", paymentDetailOtherwise.getTax().getName());
                    obj.put("taxcode", paymentDetailOtherwise.getTax().getTaxCode());
                    obj.put("prtaxid", paymentDetailOtherwise.getTax().getID());
                } else {
                    obj.put("taxname", "");
                    obj.put("taxcode", "");
                    obj.put("prtaxid", "");
                }
                obj.put("type", Constants.GLPayment);
                // ## Get Custom Field Data 
                Map<String, Object> variableMap = new HashMap<String, Object>();
                HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                Detailfilter_params.add(paymentDetailOtherwise.getID());
                invDetailRequestParams.put("filter_names", Detailfilter_names);
                invDetailRequestParams.put("filter_params", Detailfilter_params);
                boolean isExport = false;
                boolean isForReport = false;
                if (requestParams.containsKey(Constants.isExport) && requestParams.get(Constants.isExport) != null) {
                    isExport = Boolean.parseBoolean(requestParams.get(Constants.isExport).toString());
                }
                if (requestParams.containsKey(Constants.isForReport) && requestParams.get(Constants.isForReport) != null) {
                    isForReport = Boolean.parseBoolean(requestParams.get(Constants.isForReport).toString());
                }
                JSONArray dimensionArr = new JSONArray();
                KwlReturnObject idcustresult = accVendorPaymentobj.getVendorPaymentCustomData(invDetailRequestParams);
                if (idcustresult.getEntityList().size() > 0) {
                    AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                    AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);

                    if (jeDetailCustom != null) {
                        JSONObject params = new JSONObject();
                        params.put(Constants.isExport, isExport);
                        params.put(Constants.isForReport, isForReport);
                        params.put(Constants.userdf, udf);
                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params,customRichTextMap);
                    }
                }
                obj.put("dimensionArr", dimensionArr);
                innerJArrGL.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentController.getPaymentRowsJson : " + ex.getMessage(), ex);
        }
    }

    private void getPaymentCNDNArray(Payment re, JSONArray innerJArrCNDN, KWLCurrency currency, HashMap requestParams, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap, HashMap<String, Integer> FieldMap, HashMap<String, String> replaceFieldMap, HashMap<String, String> customRichTextMap) throws SessionExpiredException, ServiceException {
        try {
            KwlReturnObject cndnResult = accVendorPaymentobj.getVendorCnPayment(re.getID());
            List<CreditNotePaymentDetails> cnpdList = cndnResult.getEntityList();
            DateFormat df = (DateFormat) requestParams.get("userdateformat");
            DateFormat df1 = (DateFormat) requestParams.get("dateformat");
            String companyid= (String) requestParams.get("companyid");
            for (CreditNotePaymentDetails cnpd : cnpdList) {
                String cnnoteid = cnpd.getCreditnote().getID() != null ? cnpd.getCreditnote().getID() : "";
                Double cnpaidamount = cnpd.getAmountPaid();
                Double cnPaidAmountPaymentCurrency = cnpd.getPaidAmountInPaymentCurrency();
                Double exchangeratefortransaction = cnpd.getExchangeRateForTransaction();//for documentdesigner
                String description = cnpd.getDescription() != null ? cnpd.getDescription() : "";//for documentdesigner
                boolean isExport = false;
                boolean isForReport = false;
                if (requestParams.containsKey(Constants.isExport) && requestParams.get(Constants.isExport) != null) {
                    isExport = Boolean.parseBoolean(requestParams.get(Constants.isExport).toString());
                }
                if (requestParams.containsKey(Constants.isForReport) && requestParams.get(Constants.isForReport) != null) {
                    isForReport = Boolean.parseBoolean(requestParams.get(Constants.isForReport).toString());
                }
                CreditNote creditNote = cnpd.getCreditnote();
                JSONObject obj = new JSONObject();
                obj.put("transectionno", creditNote.getCreditNoteNumber());
                obj.put("transectionid", creditNote.getID());
                obj.put("srnoforrow", cnpd.getSrno());
                obj.put("srNoForRow", cnpd.getSrno());
                obj.put("isopening", creditNote.isIsOpeningBalenceCN());
                if (creditNote.isNormalCN()) {
//                    obj.put("creationdate", df.format(creditNote.getJournalEntry().getEntryDate()));
                    obj.put("dateforsort", creditNote.getJournalEntry().getEntryDate());
                } else {
                    obj.put("dateforsort", creditNote.getCreationDate());
                }
                obj.put("creationdate", df.format(creditNote.getCreatedon()));
//                obj.put("linkingdate", df1.format(cnpd.getPayment().getJournalEntry().getEntryDate()));
                obj.put("linkingdate", df1.format(cnpd.getPayment().getCreationDate()));
                if (creditNote.getVendor() != null) {
                    obj.put("accountid", creditNote.getVendor().getID());
                    obj.put("accountname", creditNote.getVendor().getName());
                } else if (creditNote.getCustomer() != null) {
                    obj.put("accountid", creditNote.getCustomer().getID());
                    obj.put("accountname", creditNote.getCustomer().getName());
                }
                if (creditNote != null) {
                    obj.put("currencyidtransaction", creditNote.getCurrency() == null ? (re.getCurrency() == null ? currency.getCurrencyID() : re.getCurrency().getCurrencyID()) : creditNote.getCurrency().getCurrencyID());
                    obj.put("currencysymbol", creditNote.getCurrency() == null ? (re.getCurrency() == null ? currency.getSymbol() : re.getCurrency().getSymbol()) : creditNote.getCurrency().getSymbol());
                    obj.put("currencysymboltransaction", creditNote.getCurrency() == null ? (re.getCurrency() == null ? currency.getSymbol() : re.getCurrency().getSymbol()) : creditNote.getCurrency().getSymbol());
                } else {
                    obj.put("currencyidtransaction", (re.getCurrency() == null ? currency.getCurrencyID() : re.getCurrency().getCurrencyID()));
                    obj.put("currencysymbol", (re.getCurrency() == null ? currency.getSymbol() : re.getCurrency().getSymbol()));
                    obj.put("currencysymboltransaction", (re.getCurrency() == null ? currency.getSymbol() : re.getCurrency().getSymbol()));
                }
                obj.put("isopening", creditNote.isIsOpeningBalenceCN());
                obj.put("totalamount", creditNote.getCnamount());
                obj.put("enteramount", cnPaidAmountPaymentCurrency);
                obj.put("amountdue", creditNote.getCnamountdue());
                obj.put("exchangeratefortransaction", exchangeratefortransaction);//for documentdesigner
                obj.put("newamountdue", authHandler.round((creditNote.getCnamountdue() * exchangeratefortransaction), companyid) + cnPaidAmountPaymentCurrency);//for documentdesigner
                obj.put("description", description);//for documentdesigner
                obj.put("cnpaidamount", cnpaidamount);
                obj.put("type", Constants.PaymentAgainstCNDN);
                obj.put(Constants.GST_CURRENCY_RATE, cnpd.getGstCurrencyRate());
                // ## Get Custom Field Data 
                Map<String, Object> variableMap = new HashMap<String, Object>();
                HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                Detailfilter_params.add(cnpd.getID());
                invDetailRequestParams.put("filter_names", Detailfilter_names);
                invDetailRequestParams.put("filter_params", Detailfilter_params);
                JSONArray dimensionArr = new JSONArray();
                KwlReturnObject idcustresult = accVendorPaymentobj.getVendorPaymentCustomData(invDetailRequestParams);
                if (idcustresult.getEntityList().size() > 0) {
                    AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                    AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                    if (jeDetailCustom != null) {
                        JSONObject params = new JSONObject();
                        params.put(Constants.isExport, isExport);
                        params.put(Constants.isForReport, isForReport);
                        params.put(Constants.userdf, df);
                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params,customRichTextMap);
                    }
                }
                obj.put("dimensionArr", dimensionArr);
                innerJArrCNDN.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentController.getPaymentRowsJson : " + ex.getMessage(), ex);
        }
    }

    private void getPaymentAdvanceArray(Payment re, JSONArray innerJArrAdvance, KWLCurrency currency, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap, HashMap<String, Integer> FieldMap, HashMap<String, String> replaceFieldMap, HashMap requestParams, HashMap<String, String> customRichTextMap) throws SessionExpiredException, ServiceException {
        try {
            DateFormat df = (DateFormat) requestParams.get("dateformat");
            DateFormat udf = (DateFormat) requestParams.get("userdateformat");
            HashMap<String, Object> payRequestParams = new HashMap<String, Object>();
            payRequestParams.put("gcurrencyid", re.getCompany().getCurrency().getCurrencyID());
            String companyid=re.getCompany().getCompanyID();
            payRequestParams.put("companyid",companyid );
            for (AdvanceDetail advanceDetail : re.getAdvanceDetails()) {
                JSONObject obj = new JSONObject();
                obj.put("rowid", advanceDetail.getId());
                obj.put("srnoforrow", advanceDetail.getSrNoForRow());
                obj.put("srNoForRow", advanceDetail.getSrNoForRow());
                obj.put("totalamount", advanceDetail.getAmount());
                if (re.getAdvanceDetails() != null && !re.getAdvanceDetails().isEmpty() && re.isIsDishonouredCheque()) {
                    obj.put("amountdue", 0);
                } else {
                    obj.put("amountdue", advanceDetail.getAmountDue());
                }
                obj.put("paidamount", advanceDetail.getAmount());
                obj.put("paidamountOriginal", advanceDetail.getAmount());
                obj.put("description", advanceDetail.getDescription());//document designer
                obj.put("type", Constants.AdvancePayment);
                obj.put("exchangeratefortransaction", 1);
                obj.put("currencyidtransaction", (advanceDetail.getPayment().getCurrency() == null ? currency.getCurrencyID() : advanceDetail.getPayment().getCurrency().getCurrencyID()));
                obj.put("currencysymbol", (advanceDetail.getPayment().getCurrency() == null ? currency.getSymbol() : advanceDetail.getPayment().getCurrency().getSymbol()));
                obj.put("currencysymboltransaction", (advanceDetail.getPayment().getCurrency() == null ? currency.getSymbol() : advanceDetail.getPayment().getCurrency().getSymbol()));
                if (re.getVendor() != null) {
                    obj.put("accountid", re.getVendor().getID());
                    obj.put("accountname", re.getVendor().getName());
                    obj.put("accname", re.getVendor().getAccount() == null ? "" : re.getVendor().getAccount().getName());
                    obj.put("acccode", re.getVendor().getAccount() == null ? "" : re.getVendor().getAccount().getAcccode());
                } else if (!StringUtil.isNullOrEmpty(re.getCustomer())) {
                    obj.put("accountid", re.getCustomer());
                    KwlReturnObject customerResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), re.getCustomer());
                    Customer customer = (Customer) customerResult.getEntityList().get(0);
                    obj.put("accountname", customer.getName());
                    obj.put("accname", customer.getAccount() == null ? "" : customer.getAccount().getName());
                    obj.put("acccode", customer.getAccount() == null ? "" : customer.getAccount().getAcccode());
                    if (advanceDetail.getReceiptAdvanceDetails() != null) {
                        double enternedAmnt = advanceDetail.getAmount();
                        Receipt receipt = advanceDetail.getReceiptAdvanceDetails().getReceipt();
                        double exchangeratefortransaction = advanceDetail.getExchangeratefortransaction();
                        double enternedAmntOriginal = advanceDetail.getAmount() / exchangeratefortransaction;
                        KWLCurrency receiptCurrency = receipt.getCurrency();
//                            if (!re.getCurrency().getCurrencyID().equals(receiptCurrency.getCurrencyID())) {
//                                KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(payRequestParams, enternedAmnt, re.getCurrency().getCurrencyID(),receiptCurrency.getCurrencyID(), re.getJournalEntry().getEntryDate(), 0);
//                                enternedAmntOriginal = (Double) bAmt.getEntityList().get(0);
//                            }
                        obj.put("transectionno", receipt.getReceiptNumber());
                        obj.put("transectionid", receipt.getID());
                        if (receipt.isIsDishonouredCheque()) {
                            obj.put("amountdue", 0);
                        } else {
                            obj.put("amountdue", advanceDetail.getReceiptAdvanceDetails().getAmountDue());
                        }
                        obj.put("paidamountOriginal", enternedAmntOriginal);
                        obj.put("totalamount", advanceDetail.getReceiptAdvanceDetails().getAmount());
                        obj.put("amountDueOriginal", advanceDetail.getReceiptAdvanceDetails().getAmountDue());
                        obj.put("amountDueOriginalSaved", advanceDetail.getReceiptAdvanceDetails().getAmountDue());
                        obj.put("exchangeratefortransaction", exchangeratefortransaction);
                        obj.put("currencyidtransaction", receiptCurrency.getCurrencyID());
                        obj.put("currencysymbol", receiptCurrency.getSymbol());
                        obj.put("currencysymboltransaction", receiptCurrency.getSymbol());
                        obj.put("isrefund", true);
//                        obj.put("linkingdate", df.format(re.getJournalEntry().getEntryDate()));
                        obj.put("linkingdate", df.format(re.getCreationDate()));

                    }
                }

                if (advanceDetail.getTdsdetails() != null) {
                    for (TdsDetails tds : advanceDetail.getTdsdetails()) {
                        obj.put(CustomDesignerConstants.TDS_RATE, tds.getTdspercentage());
                        obj.put(CustomDesignerConstants.TDS_AMOUNT, tds.getTdsamount());
                    }
                }
                // ## Get Custom Field Data 
                Map<String, Object> variableMap = new HashMap<String, Object>();
                HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                Detailfilter_params.add(advanceDetail.getId());
                invDetailRequestParams.put("filter_names", Detailfilter_names);
                invDetailRequestParams.put("filter_params", Detailfilter_params);
                boolean isExport = false;
                boolean isForReport = false;
                if (requestParams.containsKey(Constants.isExport) && requestParams.get(Constants.isExport) != null) {
                    isExport = Boolean.parseBoolean(requestParams.get(Constants.isExport).toString());
                }
                if (requestParams.containsKey(Constants.isForReport) && requestParams.get(Constants.isForReport) != null) {
                    isForReport = Boolean.parseBoolean(requestParams.get(Constants.isForReport).toString());
                }
                JSONArray dimensionArr = new JSONArray();
                KwlReturnObject idcustresult = accVendorPaymentobj.getVendorPaymentCustomData(invDetailRequestParams);
                if (idcustresult.getEntityList().size() > 0) {
                    AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                    AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                    if (jeDetailCustom != null) {
                        JSONObject params = new JSONObject();
                        params.put(Constants.isExport, isExport);
                        params.put(Constants.isForReport, isForReport);
                        params.put(Constants.userdf, udf);
                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params,customRichTextMap);
                    }
                }
                obj.put("dimensionArr", dimensionArr);
                innerJArrAdvance.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccVendorPaymentServiceImpl.getPaymentAdvanceArray : " + ex.getMessage(), ex);
        }
    }

    private void getPaymentLinkArray(Payment re, JSONArray innerJArrLinkedAdvance, KWLCurrency currency, double taxPercent, HashMap<String, String> customFieldMap, HashMap<String, Integer> FieldMap, HashMap<String, String> replaceFieldMap, Iterator itr, HashMap requestParams, HashMap<String, String> customDateFieldMap, HashMap<String, String> customRichTextMap) throws SessionExpiredException, ServiceException {
        try {
            DateFormat df = (DateFormat) requestParams.get("dateformat");
            DateFormat userdf = (DateFormat) requestParams.get("userdateformat");
            boolean isVendorPaymentEdit = Boolean.parseBoolean((String) requestParams.get("isReceiptEdit"));
            String companyid = (String) requestParams.get("companyid");
            boolean isExport = false;
            boolean isForReport = false;
            if (requestParams.containsKey(Constants.isExport) && requestParams.get(Constants.isExport) != null) {
                isExport = Boolean.parseBoolean(requestParams.get(Constants.isExport).toString());
            }
            if (requestParams.containsKey(Constants.isForReport) && requestParams.get(Constants.isForReport) != null) {
                isForReport = Boolean.parseBoolean(requestParams.get(Constants.isForReport).toString());
            }
            Iterator itrLinkedPayment = re.getLinkDetailPayments().iterator();
            while (itrLinkedPayment.hasNext()) {
                LinkDetailPayment row = (LinkDetailPayment) itrLinkedPayment.next();
                Date grCreationDate = null;
                double grAmount = 0d;
                if (row.getGoodsReceipt().isNormalInvoice()) {
                    grCreationDate = row.getGoodsReceipt().getJournalEntry().getEntryDate();
                    grAmount = isVendorPaymentEdit ? row.getGoodsReceipt().getVendorEntry().getAmount() : row.getAmount();
                } else {// for opening balance inoices
                    grCreationDate = row.getGoodsReceipt().getCreationDate();
                    grAmount = isVendorPaymentEdit ? row.getGoodsReceipt().getOriginalOpeningBalanceAmount() : row.getAmount();
                }
                JSONObject obj = new JSONObject();
                obj.put("billid", isVendorPaymentEdit ? row.getGoodsReceipt().getID() : re.getID());
                obj.put("isopening", row.getGoodsReceipt().isIsOpeningBalenceInvoice());
                obj.put("srno", row.getSrno());
                obj.put("srnoforrow", row.getSrno());
                obj.put("srNoForRow", row.getSrno());
                obj.put("rowid", row.getID());
                obj.put("transectionno", row.getGoodsReceipt().getGoodsReceiptNumber());
                obj.put("transectionid", row.getGoodsReceipt().getID());
                obj.put("amount", grAmount);
                obj.put("enteramount", row.getAmount());
                obj.put("accountid", (isVendorPaymentEdit ? row.getGoodsReceipt().getAccount().getID() : ""));
                obj.put("type", Constants.PaymentAgainstInvoice);
                if (row.getGoodsReceipt() != null) {
                    obj.put("currencyidtransaction", row.getGoodsReceipt().getCurrency() == null ? (row.getPayment().getCurrency() == null ? currency.getCurrencyID() : row.getPayment().getCurrency().getCurrencyID()) : row.getGoodsReceipt().getCurrency().getCurrencyID());
                    obj.put("currencysymbol", row.getGoodsReceipt().getCurrency() == null ? (row.getPayment().getCurrency() == null ? currency.getSymbol() : row.getPayment().getCurrency().getSymbol()) : row.getGoodsReceipt().getCurrency().getSymbol());
                    obj.put("currencysymboltransaction", row.getGoodsReceipt().getCurrency() == null ? (row.getPayment().getCurrency() == null ? currency.getSymbol() : row.getPayment().getCurrency().getSymbol()) : row.getGoodsReceipt().getCurrency().getSymbol());
                } else {
                    obj.put("currencyidtransaction", (row.getPayment().getCurrency() == null ? currency.getCurrencyID() : row.getPayment().getCurrency().getCurrencyID()));
                    obj.put("currencysymbol", (row.getPayment().getCurrency() == null ? currency.getSymbol() : row.getPayment().getCurrency().getSymbol()));
                    obj.put("currencysymboltransaction", (row.getPayment().getCurrency() == null ? currency.getSymbol() : row.getPayment().getCurrency().getSymbol()));
                }
                obj.put("duedate", df.format(row.getGoodsReceipt().getDueDate()));
                obj.put("linkingdate", row.getPaymentLinkDate()!=null ? df.format(row.getPaymentLinkDate()) : "");
                obj.put("creationdate", df.format(grCreationDate));
                obj.put("creationdateinuserformat", userdf.format(grCreationDate));
//                    JArr.put(obj);
                if (row.getGoodsReceipt().getTax() != null) {
                    KwlReturnObject perresult = accTaxObj.getTaxPercent(row.getCompany().getCompanyID(), row.getGoodsReceipt().getJournalEntry().getEntryDate(), row.getGoodsReceipt().getTax().getID());
                    taxPercent = (Double) perresult.getEntityList().get(0);
                }
                obj.put("taxpercent", taxPercent);
                obj.put("discount", row.getGoodsReceipt().getDiscount() == null ? 0 : row.getGoodsReceipt().getDiscount().getDiscountValue());
                obj.put("payment", row.getGoodsReceipt().getID());
                double rowAmount = (authHandler.round(row.getAmount(), companyid));
                if (isVendorPaymentEdit) {
                    obj.put("amountpaid", rowAmount);
                    if (row.getFromCurrency() != null && row.getToCurrency() != null) {
                        obj.put("exchangeratefortransaction", row.getExchangeRateForTransaction());
                        obj.put("amountpaidincurrency", authHandler.round(rowAmount / row.getExchangeRateForTransaction(), companyid));
                    } else {
                        double amount = rowAmount;
                        String fromcurrencyid = (row.getPayment().getCurrency() == null ? currency.getCurrencyID() : row.getPayment().getCurrency().getCurrencyID());
                        String tocurrencyid = (row.getGoodsReceipt().getCurrency() == null ? (row.getPayment().getCurrency() == null ? currency.getCurrencyID() : row.getPayment().getCurrency().getCurrencyID()) : row.getGoodsReceipt().getCurrency().getCurrencyID());
                        double exchangeRate = row.getGoodsReceipt().isIsOpeningBalenceInvoice() ? row.getGoodsReceipt().getExternalCurrencyRate() : row.getGoodsReceipt().getJournalEntry().getExternalCurrencyRate();
                        Date tranDate = row.getGoodsReceipt().isIsOpeningBalenceInvoice() ? row.getGoodsReceipt().getCreationDate() : row.getGoodsReceipt().getJournalEntry().getEntryDate();
                        KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherRoundOff(requestParams, amount, fromcurrencyid, tocurrencyid, tranDate, exchangeRate);
                        amount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                        obj.put("exchangeratefortransaction", amount / rowAmount);
                        obj.put("amountpaidincurrency", amount);
                    }
                } else {
                    if (row.getFromCurrency() != null && row.getToCurrency() != null) {
//                        obj.put("amountpaid", rowAmount / row.getExchangeRateForTransaction());
                        obj.put("amountpaid", row.getAmountInGrCurrency());
                    } else {
                        double amount = rowAmount;
                        String fromcurrencyid = (row.getPayment().getCurrency() == null ? currency.getCurrencyID() : row.getPayment().getCurrency().getCurrencyID());
                        String tocurrencyid = (row.getGoodsReceipt().getCurrency() == null ? (row.getPayment().getCurrency() == null ? currency.getCurrencyID() : row.getPayment().getCurrency().getCurrencyID()) : row.getGoodsReceipt().getCurrency().getCurrencyID());
                        double exchangeRate = row.getGoodsReceipt().isIsOpeningBalenceInvoice() ? row.getGoodsReceipt().getExternalCurrencyRate() : row.getGoodsReceipt().getJournalEntry().getExternalCurrencyRate();
                        Date tranDate = row.getGoodsReceipt().isIsOpeningBalenceInvoice() ? row.getGoodsReceipt().getCreationDate() : row.getGoodsReceipt().getJournalEntry().getEntryDate();
                        KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherRoundOff(requestParams, amount, fromcurrencyid, tocurrencyid, tranDate, exchangeRate);
                        amount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                        obj.put("amountpaid", amount);
                    }
                }
                requestParams.put("isVendorPaymentEdit", isVendorPaymentEdit);

                double amountdue = 0.0, totalamount = 0.0, amountDueOriginal = 0.0;
                if (row.getGoodsReceipt().isNormalInvoice()) {
                    List ll;
                    if (row.getGoodsReceipt().isIsExpenseType()) {
                        ll = accGoodsReceiptCMN.getExpGRAmountDue(requestParams, row.getGoodsReceipt());
                        amountDueOriginal = (ll.isEmpty() ? 0 : (Double) ll.get(4));
                    } else {
                        if (Constants.InvoiceAmountDueFlag) {
                            ll = accGoodsReceiptCMN.getInvoiceDiscountAmountInfo(requestParams, row.getGoodsReceipt());
                            amountDueOriginal = (ll.isEmpty() ? 0 : (Double) ll.get(5));
                        } else {
                            ll = accGoodsReceiptCMN.getGRAmountDue(requestParams, row.getGoodsReceipt());
                            amountDueOriginal = (ll.isEmpty() ? 0 : (Double) ll.get(5));
                        }
                    }
                    amountdue = (ll.isEmpty() ? 0 : (Double) ll.get(1));
                    totalamount = row.getGoodsReceipt().getVendorEntry().getAmount();
                } else {
                    amountdue = row.getGoodsReceipt().getOpeningBalanceAmountDue() * (row.getExchangeRateForTransaction() == 0 ? 1 : row.getExchangeRateForTransaction());
                    amountDueOriginal = row.getGoodsReceipt().getOpeningBalanceAmountDue();
                    totalamount = row.getGoodsReceipt().getOriginalOpeningBalanceAmount();
                }
                amountdue = authHandler.round(amountdue, companyid);
                amountDueOriginal = authHandler.round(amountDueOriginal, companyid);
                totalamount = authHandler.round(totalamount, companyid);

                if (row.getFromCurrency() != null && row.getToCurrency() != null) {
                    obj.put("amountduenonnegative", (isVendorPaymentEdit ? (authHandler.round(amountDueOriginal * row.getExchangeRateForTransaction(), companyid) + rowAmount) : amountDueOriginal));
                } else {
                    obj.put("amountduenonnegative", (isVendorPaymentEdit ? amountdue + obj.optDouble("amountpaid", 0) : amountdue));
                }
                obj.put("amountDueOriginal", (isVendorPaymentEdit ? amountDueOriginal + obj.optDouble("amountpaidincurrency", 0) : amountDueOriginal));
                obj.put("amountDueOriginalSaved", (isVendorPaymentEdit ? amountDueOriginal + obj.optDouble("amountpaidincurrency", 0) : amountDueOriginal));
                obj.put("totalamount", totalamount);
                obj.put("amountdue", amountdue);

                // ## Get Custom Field Data 
                Map<String, Object> variableMap = new HashMap<String, Object>();
                HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                Detailfilter_params.add(row.getID());
                invDetailRequestParams.put("filter_names", Detailfilter_names);
                invDetailRequestParams.put("filter_params", Detailfilter_params);
                JSONArray dimensionArr = new JSONArray();
                KwlReturnObject idcustresult = accVendorPaymentobj.getVendorPaymentCustomData(invDetailRequestParams);
                if (idcustresult.getEntityList().size() > 0) {
                    AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                    AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                    if (jeDetailCustom != null) {
                        JSONObject params = new JSONObject();
                        params.put(Constants.isExport, isExport);
                        params.put(Constants.isForReport, isForReport);
                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params,customRichTextMap);
                    }
                }
                obj.put("dimensionArr", dimensionArr);
                innerJArrLinkedAdvance.put(obj);

            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentController.getPaymentRowsJson : " + ex.getMessage(), ex);
        }
    }

    private void getPaymentLinkToCreditNoteArray(Payment payment, JSONArray innerJArrLinkedAdvance, KWLCurrency currency, DateFormat df, DateFormat userdf, String compayid) throws SessionExpiredException, ServiceException {
        try {

            Iterator itrLinkedPayment = payment.getLinkDetailPaymentToCreditNote().iterator();
            while (itrLinkedPayment.hasNext()) {
                LinkDetailPaymentToCreditNote row = (LinkDetailPaymentToCreditNote) itrLinkedPayment.next();
                CreditNote creditNote = row.getCreditnote();
                Date cnCreationDate = null;
                double exchangeratefortransaction = row.getExchangeRateForTransaction();
                double rowAmount = (authHandler.round(row.getAmount(), compayid));
                if (creditNote.isNormalCN()) {
                    cnCreationDate = creditNote.getJournalEntry().getEntryDate();
                } else {// opening balance invoice creation date
                    cnCreationDate = creditNote.getCreationDate();
                }

                JSONObject obj = new JSONObject();
                obj.put("transectionno", creditNote.getCreditNoteNumber());
                obj.put("transectionid", creditNote.getID());
                obj.put("isopening", creditNote.isIsOpeningBalenceCN());
                obj.put("creationdate", df.format(cnCreationDate));
                obj.put("isopening", creditNote.isIsOpeningBalenceCN());
                obj.put("creationdateinuserformat", userdf.format(cnCreationDate));
                obj.put("dateforsort", cnCreationDate);
                obj.put("linkingdate", row.getPaymentLinkDate()!=null ? df.format(row.getPaymentLinkDate()) : "");
                if (creditNote.getVendor() != null) {
                    obj.put("accountid", creditNote.getVendor().getID());
                    obj.put("accountname", creditNote.getVendor().getName());
                }
                if (creditNote != null) {
                    obj.put("currencyidtransaction", creditNote.getCurrency() == null ? (payment.getCurrency() == null ? currency.getCurrencyID() : payment.getCurrency().getCurrencyID()) : creditNote.getCurrency().getCurrencyID());
                    obj.put("currencysymbol", creditNote.getCurrency() == null ? (payment.getCurrency() == null ? currency.getSymbol() : payment.getCurrency().getSymbol()) : creditNote.getCurrency().getSymbol());
                    obj.put("currencysymboltransaction", creditNote.getCurrency() == null ? (payment.getCurrency() == null ? currency.getSymbol() : payment.getCurrency().getSymbol()) : creditNote.getCurrency().getSymbol());
                } else {
                    obj.put("currencyidtransaction", (payment.getCurrency() == null ? currency.getCurrencyID() : payment.getCurrency().getCurrencyID()));
                    obj.put("currencysymbol", (payment.getCurrency() == null ? currency.getSymbol() : payment.getCurrency().getSymbol()));
                    obj.put("currencysymboltransaction", (payment.getCurrency() == null ? currency.getSymbol() : payment.getCurrency().getSymbol()));
                }
                obj.put("totalamount", creditNote.getCnamount());
                obj.put("enteramount", row.getAmount());
                obj.put("amountdue", creditNote.getCnamountdue());
                obj.put("exchangeratefortransaction", exchangeratefortransaction);//for documentdesigner
                obj.put("newamountdue", authHandler.round((creditNote.getCnamountdue() * exchangeratefortransaction), compayid) + row.getAmount());//for documentdesigner
                obj.put("cnpaidamount", row.getAmountInCNCurrency());
                obj.put("srNoForRow", row.getSrno());
                innerJArrLinkedAdvance.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccReceiptServiceImpl.getReceiptLinkToDebitNoteArray : " + ex.getMessage(), ex);
        }
    }

    public JSONObject repeatPayment() {
        JSONObject jobj = new JSONObject();
        boolean isSuccess = false;
        String msg = "";
        String failed = "";
        JSONArray recuringArray = new JSONArray();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("RP_tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        try {
            String croneName = Constants.Recurring_Make_Payment_Crone_Name;
            String croneID = Constants.Recurring_Make_Payment_Crone_ID;
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Date currentDate = df.parse(df.format(new Date()));

            boolean isCroneExecutedForCurrentDay = accCommonTablesDAO.isCroneExecutedForCurrentDay(croneID, currentDate);
            HashMap mailParams = new HashMap();
            if (isCroneExecutedForCurrentDay) {
                msg = "This Crone is executed for today,so it cannot be hit again";
            } else {

                GregorianCalendar gc = new GregorianCalendar(); //It returns actual 'Date' object
                gc.setTime(currentDate);
                Date prevDate = gc.getTime();
                prevDate = df.parse(df.format(prevDate));   //Previous Date before the recurring. 
                String entryno = "";
                String loginUserId = "";
                Payment paymentObject;
                KwlReturnObject repeateMPdetails = accVendorPaymentobj.getRepeatePaymentNo(prevDate);
                List jlist = repeateMPdetails.getEntityList();
                Iterator jeitr = jlist.iterator();
                while (jeitr.hasNext()) {
                    try {
                        paymentObject = (Payment) jeitr.next();
                        entryno = paymentObject != null ? (paymentObject.getPaymentNumber()) : "";
                        loginUserId = paymentObject != null ? (paymentObject.getCreatedby().getUserID()) : "";
                        if (!StringUtil.isNullOrEmpty(loginUserId)) {
                            String htmlMailContent = "<br/>Recurring Payment <b>\"" + entryno + "\"</b> will be post tomorrow.<br/>";
                            htmlMailContent += "<br/>If you want, you can deactivate it from Recurring Payment Report.<br/>";
                            mailParams.put("htmlMailContent", htmlMailContent);

                            String plainMailContent = "\nRecurring Payment \n" + entryno + "\nwill be post tomorrow.\n";
                            plainMailContent += "\nIf you want, you can deactivate it from Recurring Payment Report.\n";
                            mailParams.put("plainMailContent", plainMailContent);

                            mailParams.put("loginUserId", loginUserId);
                            SendMail(mailParams);    //Notification Mail 
                        }
                    } catch (Exception ex) {
                        failed = "Recurring notification mail failed.";
                    }
                }
                JSONObject jsobj = null;
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                KwlReturnObject repeateMP = accVendorPaymentobj.getRepeatPayment(requestParams);
                List PaymentList = repeateMP.getEntityList();
                Iterator MPitr = PaymentList.iterator();
                while (MPitr.hasNext()) {
                    jsobj = new JSONObject();
                    User adminUser = null;
                    String pmentryno = "";
                    status = txnManager.getTransaction(def);
                    Payment payment = (Payment) MPitr.next();
                    try {
                        Payment repeatedMP = repeatePayment(payment, payment.getRepeatedPayment());
                        msg += repeatedMP.getPaymentNumber() + ",";
                        adminUser = repeatedMP.getCompany().getCreator()!=null ? repeatedMP.getCompany().getCreator() : repeatedMP.getCreatedby();
                        pmentryno = repeatedMP.getPaymentNumber();
                        jsobj.put("user", adminUser);
                        jsobj.put("paymentno", pmentryno);
                        recuringArray.put(jsobj);
                        if (payment.getRepeatedPayment() != null) {
                            updateRepeateInfoForPayment(payment.getRepeatedPayment());
                        }
                        txnManager.commit(status);
                    } catch (Exception ex) {
                        if (status != null) {
                            txnManager.rollback(status);
                        }
                        failed += payment.getPaymentNumber() + "[" + payment.getID() + "]: " + ex.getMessage() + ";";
                    }
                    isSuccess = true;
                   accCommonTablesDAO.saveCroneDetails(croneID, croneName, currentDate); 
                }
            }
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg += ex.getMessage();
        } finally {
            try {
                jobj.put("success", isSuccess);
                jobj.put("msg", msg);
                jobj.put("recurrray", recuringArray);
                jobj.put("failed", failed);
                jobj.put("datetime", new Date());
            } catch (JSONException ex) {
                Logger.getLogger(AccVendorPaymentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    public Payment repeatePayment(Payment oldPayment, RepeatedPayment repeatedPymentObject) throws ServiceException, AccountingException {
        Payment payment = new Payment();
        try {
            String companyid = oldPayment.getCompany().getCompanyID();
            String currencyid = oldPayment.getCurrency().getCurrencyID();
            String Memo = "";
            Date chequeDate = null;
            String chequeNumber = "";
            boolean isAutoGenerateChequeNumber = false;
            boolean nextInv = false;

            SequenceFormat prevSeqFormat = null;
            String nextAutoNo = "";
            int nextAutoNoInt;
            String datePrefix = "";
            String dateafterPrefix = "";
            String dateSuffix = "";
            String entryNumber = "";
            String companyTZDiff = oldPayment.getCompany().getTimeZone() != null ? oldPayment.getCompany().getTimeZone().getDifference() : "+00:00";
            DateFormat comdf = authHandler.getCompanyTimezoneDiffFormat(companyTZDiff);
            SimpleDateFormat sd = new SimpleDateFormat("MMMM d, yyyy");
            Calendar cal = Calendar.getInstance();
            Date BillDate = cal.getTime();
            Date creationDate = sd.parse(comdf.format(BillDate));
            KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) returnObject.getEntityList().get(0);
            if (extraPref != null && extraPref.isDefaultsequenceformatforrecinv() && oldPayment.getSeqformat() != null && oldPayment.getSeqformat().isIsactivate()) {
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_PAYMENT, oldPayment.getSeqformat().getID(), false, creationDate);
                nextAutoNo = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                nextAutoNoInt = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
                datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                payment.setSeqformat(oldPayment.getSeqformat());
                payment.setSeqnumber(nextAutoNoInt);
                payment.setDatePreffixValue(datePrefix);
                payment.setDateAfterPreffixValue(dateafterPrefix);
                payment.setDateSuffixValue(dateSuffix);
                entryNumber = nextAutoNo;
            } else {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("parentPaymentId", oldPayment.getID());
                KwlReturnObject details = accVendorPaymentobj.getRepeatePaymentDetails(requestParams);
                List detailsList = details.getEntityList();
                int repPaymentCount = detailsList.size() + Constants.RECURRING_INVOICE_01_APPEND_START_FROM;
                while (nextInv == false) {
                    entryNumber = oldPayment.getPaymentNumber() + "-" + repPaymentCount;
                    details = accVendorPaymentobj.getMPCount(entryNumber, companyid);
                    int nocount = details.getRecordTotalCount();
                    if (nocount > 0) {
                        repPaymentCount++;
                        continue;
                    } else {
                        nextInv = true;
                    }
                }
            }
            int noOfPaymentRemainpost = oldPayment.getRepeatedPayment().getNoOfRemainpaymentspost();
            noOfPaymentRemainpost++;
            try {
                HashMap<String, Object> requestParamsMemo = new HashMap<String, Object>();
                requestParamsMemo.put("repeatedJEMemoID", oldPayment.getRepeatedPayment().getId());
                requestParamsMemo.put("noOfJERemainpost", noOfPaymentRemainpost);
                requestParamsMemo.put("columnName", "RepeatedPaymentId");
                KwlReturnObject RepeatedJEMemo = accJournalEntryobj.getRepeateJEMemo(requestParamsMemo);
                RepeatedJEMemo RM = (RepeatedJEMemo) RepeatedJEMemo.getEntityList().get(0);
                Memo = RM.getMemo();
                if (StringUtil.isNullOrEmpty(Memo)) {
                    Memo = oldPayment.getMemo();
                }
            } catch (Exception ex) {
                Memo = oldPayment.getMemo();
            }

            //Set basic parameters of Payment
            HashMap paymenthm = new HashMap();
            payment.setIsadvancepayment(oldPayment.isIsadvancepayment());
            payment.setReceipttype(oldPayment.getReceipttype());
            if (oldPayment.isIBGTypeTransaction()) {
                payment.setIBGTypeTransaction(oldPayment.isIBGTypeTransaction());
                payment.setIbgreceivingbankdetails(oldPayment.getIbgreceivingbankdetails());
                payment.setIbgCode(oldPayment.getIbgCode());
            }
            if (oldPayment.getBankChargesAccount() != null) {
                payment.setBankChargesAccount(oldPayment.getBankChargesAccount());
                payment.setBankChargesAmount(oldPayment.getBankChargesAmount());
            }
            if (oldPayment.getBankInterestAccount() != null) {
                payment.setBankInterestAccount(oldPayment.getBankInterestAccount());
                payment.setBankInterestAmount(oldPayment.getBankInterestAmount());
            }
            if (oldPayment.getPaidTo() != null) {
                payment.setPaidTo(oldPayment.getPaidTo());
            }
            if (oldPayment.getCustomer() != null) {
                payment.setCustomer(oldPayment.getCustomer());
            }
            if (oldPayment.getVendor() != null) {
                payment.setVendor(oldPayment.getVendor());
            }

            long createdon = System.currentTimeMillis();
            long updatedon = System.currentTimeMillis();
            payment.setCreatedon(createdon);
            payment.setUpdatedon(updatedon);
            payment.setCreatedby(oldPayment.getCreatedby());
            payment.setModifiedby(oldPayment.getCreatedby());
            payment.setAutoGenerated(oldPayment.isAutoGenerated());
            payment.setPaymentNumber(entryNumber);
            payment.setExternalCurrencyRate(oldPayment.getExternalCurrencyRate());
            payment.setCompany(oldPayment.getCompany());
            payment.setLinkedToClaimedInvoice(oldPayment.isLinkedToClaimedInvoice());
            payment.setPayee(oldPayment.getPayee());
            payment.setPaymentcurrencytopaymentmethodcurrencyrate(oldPayment.getPaymentcurrencytopaymentmethodcurrencyrate());
            payment.setDepositAmount(oldPayment.getDepositAmount());
            payment.setCurrency(oldPayment.getCurrency());
            payment.setPaymentWindowType(oldPayment.getPaymentWindowType());
            payment.setParentPayment(oldPayment);
            payment.setMemo(Memo);
            payment.setApprovestatuslevel(11); //SDP-4426
            payment.setCreationDate(creationDate);

            Calendar dueDate = Calendar.getInstance();
            dueDate.setTime(BillDate);

            if (oldPayment.getPayDetail() != null && oldPayment.getPayDetail().getPaymentMethod().getDetailType() == PaymentMethod.TYPE_BANK) {
                try {
                    HashMap<String, Object> requestParamsMemo = new HashMap<String, Object>();
                    requestParamsMemo.put("repeatedPaymentID", oldPayment.getRepeatedPayment().getId());
                    requestParamsMemo.put("noOfMPRemainpost", noOfPaymentRemainpost);
                    KwlReturnObject RepeatedPaymentChequeDetail = accJournalEntryobj.getRepeatePaymentChequeDetail(requestParamsMemo);
                    RepeatedPaymentChequeDetail R = (RepeatedPaymentChequeDetail) RepeatedPaymentChequeDetail.getEntityList().get(0);
                    chequeDate = R.getChequeDate();
                    chequeNumber = R.getChequeNumber();
                    if (chequeDate == null) {
                        chequeDate = sd.parse(comdf.format(BillDate));
                    }
                } catch (Exception ex) {
                    chequeDate = sd.parse(comdf.format(BillDate));
                }
                isAutoGenerateChequeNumber = repeatedPymentObject.isAutoGenerateChequeNumber();
            }
         
            // Create Journal Entry
            String jeentryNumber = "";
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            String jeSeqFormatId = "";
            boolean jeautogenflag = true;
            Map<String, Object> jeDataMap = new HashMap<String, Object>();
            synchronized (this) {
                HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                JEFormatParams.put("modulename", "autojournalentry");
                JEFormatParams.put("companyid", companyid);
                JEFormatParams.put("isdefaultFormat", true);

                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, creationDate);
                jeentryNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                jeIntegerPart = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                jeSeqFormatId = format.getID();
            }
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
            jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
            jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
            jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
            jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
            jeDataMap.put("PaymentCurrencyToPaymentMethodCurrencyRate", oldPayment.getPaymentcurrencytopaymentmethodcurrencyrate());
            jeDataMap.put("entrydate", creationDate);
            jeDataMap.put("companyid", oldPayment.getCompany().getCompanyID());
            jeDataMap.put("memo", oldPayment.getJournalEntry().getMemo());
            if (oldPayment.getJournalEntry().getCostcenter() != null) {
                jeDataMap.put("costcenterid", oldPayment.getJournalEntry().getCostcenter().getID());
            }
            jeDataMap.put("currencyid", oldPayment.getJournalEntry().getCurrency().getCurrencyID());
            jeDataMap.put("externalCurrencyRate", oldPayment.getJournalEntry().getExternalCurrencyRate());
            jeDataMap.put("ismulticurrencypaymentje", oldPayment.getJournalEntry().isIsmulticurrencypaymentje());
            if (oldPayment.getJournalEntry().getCreatedby() != null) {
                jeDataMap.put("createdby", oldPayment.getJournalEntry().getCreatedby().getUserID());
            }
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry newJournalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeDataMap.put("jeid", newJournalEntry.getID());

            //Create JE Details
            Set JE_DETAILS = oldPayment.getJournalEntry().getDetails();
            HashSet<JournalEntryDetail> jeDetails = new HashSet<JournalEntryDetail>();
            HashMap<String, String> oldNnewJEDid = new HashMap<String, String>();
            Iterator jeditr = JE_DETAILS.iterator();
            while (jeditr.hasNext()) {
                JournalEntryDetail OLD_JED = (JournalEntryDetail) jeditr.next();
                JSONObject jedjson = new JSONObject();
                jedjson.put("srno", OLD_JED.getSrno());
                jedjson.put("companyid", OLD_JED.getCompany().getCompanyID());
                jedjson.put("amount", OLD_JED.getAmount());
                jedjson.put("accountid", OLD_JED.getAccount().getID());
                jedjson.put("debit", OLD_JED.isDebit());
                jedjson.put("jeid", newJournalEntry.getID());
                jedjson.put("isseparated", OLD_JED.isIsSeparated());
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                if (OLD_JED.getAccJEDetailCustomData() != null) {
                    int NoOFRecords = accJournalEntryobj.saveCustomDataForRecurringJE(jed.getID(), OLD_JED.getID(), false);
                }
                JSONObject jedjson1 = new JSONObject();
                jedjson1.put("jedid", jed.getID());
                jedjson1.put("accjedetailcustomdata", jed.getID());
                KwlReturnObject jedresult1 = accJournalEntryobj.updateJournalEntryDetails(jedjson1);
                JournalEntryDetail jed1 = (JournalEntryDetail) jedresult1.getEntityList().get(0);
                jeDetails.add(jed1);
                oldNnewJEDid.put(OLD_JED.getID(), jed.getID());
            }
            jeDataMap.put("accjecustomdataref", newJournalEntry.getID());
            jeDataMap.put("jedetails", jeDetails);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
            payment.setJournalEntry(newJournalEntry);

            //Copy payment details otherwise from old payment to new payment.
            List<PaymentDetailOtherwise> newPDOList = new ArrayList<PaymentDetailOtherwise>();
            Set<PaymentDetailOtherwise> old_pdo = oldPayment.getPaymentDetailOtherwises();
            for (PaymentDetailOtherwise OldPdo : old_pdo) {

                PaymentDetailOtherwise newPdo = new PaymentDetailOtherwise();
                newPdo.setAmount(OldPdo.getAmount());
                newPdo.setTax(OldPdo.getTax());
                newPdo.setTaxJedId(OldPdo.getTaxJedId());
                newPdo.setAccount(OldPdo.getAccount());
                newPdo.setIsdebit(OldPdo.isIsdebit());
                newPdo.setTaxamount(OldPdo.getTaxamount());
                newPdo.setDescription(OldPdo.getDescription());
                newPdo.setGstapplied(OldPdo.getGstapplied());
                newPdo.setPayment(payment);
                /**
                 * Set JE Detail against Payment Detail
                 */
                String jedid = (String)oldNnewJEDid.get(OldPdo.getTotalJED().getID());
                KwlReturnObject returnObject1 = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), jedid);
                JournalEntryDetail journalEntryDetail = (JournalEntryDetail) returnObject1.getEntityList().get(0);
                newPdo.setTotalJED(journalEntryDetail);
                newPdo.setIsdebit(OldPdo.isIsdebit());
                newPDOList.add(newPdo);
            }

            // Create paydetails details
            PayDetail oldPaydetail = oldPayment.getPayDetail();
            PayDetail newPayDetail = null;
            HashMap newPayDetailHm = new HashMap();
            newPayDetailHm.put("paymethodid", oldPaydetail.getPaymentMethod().getID());
            newPayDetailHm.put("companyid", companyid);
            if (oldPaydetail.getPaymentMethod() != null) {
                if (oldPaydetail.getPaymentMethod().getDetailType() == PaymentMethod.TYPE_BANK) {
                    HashMap chequehm = new HashMap();
                    String chequeNO = "";
                    if (isAutoGenerateChequeNumber || StringUtil.isNullOrEmpty(chequeNumber)) {
                        chequeNO = accPaymentService.getNextChequeNumberForRecurredPayment(companyid, oldPaydetail.getPaymentMethod().getAccount().getID());
                    } else {
                        chequeNO = chequeNumber;
                    }
                    chequehm.put("chequeno", chequeNO);
                    chequehm.put("companyId", companyid);
                    chequehm.put("createdFrom", 1);
                    chequehm.put("bankAccount", (oldPaydetail.getPaymentMethod().getAccount() != null) ? oldPaydetail.getPaymentMethod().getAccount().getID() : "");
                    chequehm.put("description", oldPaydetail.getCheque().getDescription());
                    chequehm.put("bankname", oldPaydetail.getCheque().getBankName());
                    chequehm.put("duedate", chequeDate);
                    KwlReturnObject cqresult = accPaymentDAOobj.addCheque(chequehm);
                    Cheque cheque = (Cheque) cqresult.getEntityList().get(0);
                    newPayDetailHm.put("chequeid", cheque.getID());
                } else if (oldPaydetail.getPaymentMethod().getDetailType() == PaymentMethod.TYPE_CARD) {
                    HashMap cardhm = new HashMap();
                    cardhm.put("cardno", oldPaydetail.getCard().getCardNo());
                    cardhm.put("nameoncard", oldPaydetail.getCard().getCardHolder());
                    cardhm.put("expirydate", oldPaydetail.getCard().getExpiryDate());
                    cardhm.put("cardtype", oldPaydetail.getCard().getCardType());
                    cardhm.put("refno", oldPaydetail.getCard().getRefNo());
                    KwlReturnObject cdresult = accPaymentDAOobj.addCard(cardhm);
                    Card card = (Card) cdresult.getEntityList().get(0);
                    newPayDetailHm.put("cardid", card.getID());
                }
            }
            newPayDetail = accPaymentDAOobj.saveOrUpdatePayDetail(newPayDetailHm);
            payment.setPayDetail(newPayDetail);

            accVendorPaymentobj.savePaymentDetailOtherwise(newPDOList);
            List<Payment> paymentList = new ArrayList<Payment>();
            paymentList.add(payment);

            jeDataMap.put("transactionModuleid", Constants.Acc_Make_Payment_ModuleId);
            jeDataMap.put("transactionId", payment.getID());
            jeDataMap.put(JournalEntryConstants.JEID, newJournalEntry.getID());
            KwlReturnObject updatejeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//update journalentry
            JournalEntry updatejournalEntry = (JournalEntry) jeresult.getEntityList().get(0);

            accVendorPaymentobj.savePaymentObject(paymentList);

        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        } catch (AccountingException ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        }

        return payment;
    }

    public void updateRepeateInfoForPayment(RepeatedPayment rinfo) throws ServiceException, SessionExpiredException {
        Date nextDate = RepeatedPayment.calculateNextDate(rinfo.getNextDate(), rinfo.getIntervalUnit(), rinfo.getIntervalType());
        HashMap<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("NoOfRemainPaymentpost", rinfo.getNoOfRemainpaymentspost() + 1);
        dataMap.put("id", rinfo.getId());
        dataMap.put("nextDate", nextDate);
        accVendorPaymentobj.saveRepeatMPInfo(dataMap);
    }

    public void SendMail(HashMap requestParams) throws ServiceException {
        String loginUserId = (String) requestParams.get("loginUserId");
        User user = (User) accJournalEntryobj.getUserObject(loginUserId);
        KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), user.getCompany().getCompanyID());
        Company company = (Company) returnObject.getEntityList().get(0);
        String sendorInfo = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
        String htmlMailContent = (String) requestParams.get("htmlMailContent");
        String plainMailContent = (String) requestParams.get("plainMailContent");
        SimpleDateFormat sdf = new SimpleDateFormat();
        String cEmail = user.getEmailID() != null ? user.getEmailID() : "";
        if (!StringUtil.isNullOrEmpty(cEmail)) {
            try {
                String subject = "Recurring Alert Notification";
                //String sendorInfo = "admin@deskera.com";
                String htmlTextC = "";
                htmlTextC += "<br/>Hello " + user.getFirstName() + "<br/>";
                htmlTextC = htmlTextC + htmlMailContent;

                htmlTextC += "<br/>Regards,<br/>";
                htmlTextC += "<br/>ERP System<br/>";
                htmlTextC += "<br/><br/>";
                htmlTextC += "<br/>This is an auto generated email. Do not reply<br/>";

                String plainMsgC = "";
                plainMsgC += "\nHello " + user.getFirstName() + "\n";
                plainMsgC = plainMsgC + plainMailContent;

                plainMsgC += "\nRegards,\n";
                plainMsgC += "\nERP System\n";
                plainMsgC += "\n\n";
                plainMsgC += "\nThis is an auto generated email. Do not reply.\n";

                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                SendMailHandler.postMail(new String[]{cEmail}, subject, htmlTextC, plainMsgC, sendorInfo, smtpConfigMap);
            } catch (Exception ex) {
                throw ServiceException.FAILURE("" + ex.getMessage(), ex);
            }
        }
    }

    /**
     * Description : Method is used to Build Sales Return record Json
     *
     * @param <jsonarray> Used to build array of Linked documents Sales Return
     * in Credit Note
     *
     * @param <listcq> contains id of Sales Return Linked in Selected Credit
     * Note
     * @param <currency> Currency used in documents
     * @param <userdf> Object Of user Date Format
     * @return :JSONArray
     */
    @Override
    public JSONArray getSalesReturnJson(JSONArray jsonArray, List listcq, KWLCurrency currency, DateFormat userdf, String companyid) {
        try {
            Iterator itrcq = listcq.iterator();
            while (itrcq.hasNext()) {
                SalesReturn salesReturn = (SalesReturn) itrcq.next();
                JSONObject obj = new JSONObject();
                Customer customer = salesReturn.getCustomer();
                obj.put("billid", salesReturn.getID());
                obj.put("companyid", salesReturn.getCompany().getCompanyID());
                obj.put("transactionNo", salesReturn.getSalesReturnNumber());
                obj.put("date", userdf.format(salesReturn.getOrderDate()));
                obj.put("linkingdate", userdf.format(salesReturn.getCreatedon()));
                obj.put("deleted", salesReturn.isDeleted());
                obj.put("mergedCategoryData", "Sales Return");
                obj.put("personname", customer.getName());
                obj.put("mergedCategoryData", "Sales Return");
                obj.put("taxid", salesReturn.getTax() != null ?  salesReturn.getTax().getID() : "");
                obj.put("companyname", salesReturn.getCompany().getCompanyName());
                obj.put("personid", customer.getID());
                obj.put("billno", salesReturn.getSalesReturnNumber());
                obj.put("externalcurrencyrate", salesReturn.getExternalCurrencyRate());
                obj.put("aliasname", customer.getAliasname());
                obj.put("personemail", customer.getEmail());
                obj.put("memo", salesReturn.getMemo());
                obj.put("costcenterid", salesReturn.getCostcenter() == null ? "" : salesReturn.getCostcenter().getID());
                obj.put("costcenterName", salesReturn.getCostcenter() == null ? "" : salesReturn.getCostcenter().getName());
                obj.put("shipdate", salesReturn.getShipdate() == null ? "" : userdf.format(salesReturn.getShipdate()));
                obj.put("shipvia", salesReturn.getShipvia() == null ? "" : salesReturn.getShipvia());
                obj.put("fob", salesReturn.getFob() == null ? "" : salesReturn.getFob());
                obj.put("currencyid", (salesReturn.getCurrency() == null ? "" : salesReturn.getCurrency().getCurrencyID()));
                obj.put("currencysymbol", (salesReturn.getCurrency() == null ? "" : salesReturn.getCurrency().getSymbol()));
                obj.put("currencycode", (salesReturn.getCurrency() == null ? "" : salesReturn.getCurrency().getCurrencyCode()));
                obj.put("sequenceformatid", salesReturn.getSeqformat() != null ? salesReturn.getSeqformat().getID() : "");

                if (salesReturn.getModifiedby() != null) {
                    obj.put("lasteditedby", StringUtil.getFullName(salesReturn.getModifiedby()));
                }
               
                /* Code for dispalying tax amount of SI in view mode from DN linking report*/
                Set<SalesReturnDetail> srRows = salesReturn.getRows();

                boolean includeprotax = false;
                if (srRows != null && !srRows.isEmpty()) {
                    for (SalesReturnDetail temp : srRows) {

                        if (temp.getTax() != null) {
                            includeprotax = true;
                            break;
                        }
                    }
                }
                obj.put("includeprotax", includeprotax);

                jsonArray.put(obj);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return jsonArray;
        }
    }

    /**
     * Description : Below Method is used to get Advance Payment linked to
     * Refund Payment
     *
     * @param <payment> used to get Payment information
     * @param <innerJArrLinkedAdvance> used to get all data of Advance Payments
     * linked to Refund Receipt of Receipt
     * @param <currency> used to get all base currency id
     * @param <df> used to format date
     * @param <userdf> used to format date in user date format
     * @return void
     */
    private void getPaymentLinkToAdvancePaymentArray(Payment payment, JSONArray innerJArrLinkedAdvance, KWLCurrency currency, DateFormat df, DateFormat userdf, String companyid) throws SessionExpiredException, ServiceException {
        try {
            Set<LinkDetailPaymentToAdvancePayment> rowSet = payment.getLinkDetailPaymentsToAdvancePayment();
            for (LinkDetailPaymentToAdvancePayment row : rowSet) {
                Receipt receipt = row.getReceipt();

                double exchangeratefortransaction = row.getExchangeRateForTransaction();
                double rowAmount = (authHandler.round(row.getAmount(), companyid));
                Date dnCreationDate = payment.getJournalEntry().getEntryDate();

                JSONObject obj = new JSONObject();
                obj.put("transectionno", receipt.getReceiptNumber());
                obj.put("transectionid", receipt.getID());
                obj.put("isopening", receipt.isIsOpeningBalenceReceipt());
                obj.put("creationdate", df.format(dnCreationDate));
                obj.put("creationdateinuserformat", userdf.format(dnCreationDate));
                obj.put("dateforsort", dnCreationDate);
                obj.put("linkingdate", row.getPaymentLinkDate()!=null ? df.format(row.getPaymentLinkDate()) : "");
                if (receipt.getCustomer() != null) {
                    obj.put("accountid", receipt.getCustomer().getID());
                    obj.put("accountname", receipt.getCustomer().getName());
                }
                if (receipt != null) {
                    obj.put("currencyidtransaction", receipt.getCurrency() == null ? (row.getPayment().getCurrency() == null ? currency.getCurrencyID() : row.getPayment().getCurrency().getCurrencyID()) : receipt.getCurrency().getCurrencyID());
                    obj.put("currencysymbol", receipt.getCurrency() == null ? (row.getPayment().getCurrency() == null ? currency.getSymbol() : row.getPayment().getCurrency().getSymbol()) : receipt.getCurrency().getSymbol());
                    obj.put("currencysymboltransaction", receipt.getCurrency() == null ? (row.getPayment().getCurrency() == null ? currency.getSymbol() : row.getPayment().getCurrency().getSymbol()) : receipt.getCurrency().getSymbol());
                } else {
                    obj.put("currencyidtransaction", (row.getPayment().getCurrency() == null ? currency.getCurrencyID() : row.getPayment().getCurrency().getCurrencyID()));
                    obj.put("currencysymbol", (row.getPayment().getCurrency() == null ? currency.getSymbol() : row.getPayment().getCurrency().getSymbol()));
                    obj.put("currencysymboltransaction", (row.getPayment().getCurrency() == null ? currency.getSymbol() : row.getPayment().getCurrency().getSymbol()));
                }

                double amountDue = 0;
                double totalAmount = 0;
                if (receipt != null) {
                    Set<ReceiptAdvanceDetail> advDetailSet = receipt.getReceiptAdvanceDetails();
                    for (ReceiptAdvanceDetail advDetail : advDetailSet) {
                        amountDue += advDetail.getAmountDue();
                        totalAmount += advDetail.getAmount();
                    }
                }

                obj.put("totalamount", totalAmount);
                obj.put("enteramount", row.getAmount());
                obj.put("amountdue", amountDue);
                obj.put("exchangeratefortransaction", exchangeratefortransaction); // for documentdesigner
                obj.put("newamountdue", authHandler.round((amountDue * exchangeratefortransaction), companyid) + row.getAmount()); // for documentdesigner
                obj.put("paidamount", row.getAmountInPaymentCurrency());
                obj.put("srNoForRow", row.getSrno());
                innerJArrLinkedAdvance.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccVendorPaymentServiceImpl.getPaymentLinkToAdvancePaymentArray : " + ex.getMessage(), ex);
        }
    }

    //For New Make Payment get details
    @Override
    public JSONArray getMPDetailsItemJSONNew(JSONObject requestJobj, String SOID, HashMap<String, Object> paramMap) throws SessionExpiredException, ServiceException, JSONException {
        JSONArray jArr = new JSONArray();
        List<Object> list = new ArrayList<Object>();
        String paymentId = "";
        KwlReturnObject result;
        PdfTemplateConfig config = null;
        Payment pc = new Payment();
        JSONObject summaryData = new JSONObject();
        JSONArray accJSONArr = new JSONArray();
        JSONArray invJSONArr = new JSONArray();
        JSONArray dbCustomJSONArr = new JSONArray();
        double subtotal = 0, amountpaid = 0, totaltaxamount = 0.0d, totalamount = 0.0d, originalamountduetotal = 0.0d, totalAmountWithBanckChanges=0.0d,invoiceTotalDiscount=0.0d;
        String cgstAmount = "", cgstPercent = "", sgstAmount = "", sgstPercent = "", utgstAmount = "", utgstPercent = "", igstAmount = "", igstPercent = "", cessAmount = "", cessPercent = ""; // All these fields are for India GST
        String GSTExchangeRate = ""; //used for singapore GST where company has singapore country and base curreny other than SGD
        String accname = "", address = "", netinword = "", POref = "", GROref = "", VQouteRef = "", invexchangerate = "", createdby = "", updatedby = "", creditAmount = "", debitAmount = "", accountCode = "";
        String customergoodsreceiptno = "", invoicetax = "", paymentmethod = "", paymentaccount = "", bankchequeno = "", chequebankname = "", invdesc = "", gridtaxname = "", gridaccountdescription = "";
        //Card Holder Details
        String tax = "", originalamountdue = "", exchagerates = "", amountdue = "", doctype = "", docnumber = "", documentStatus = "",invoiceNos = "", desc = "", commonAppendtext = "", commonEnterPayment = "", taxamount = "", enterpayment = "", invoicedates = "", invduedates = "", originalamount = "", discount = "";
        String billAddr = "", shipAddr = "", currencysymbol = "", enterpaymentwithtax = "", gstin = "";
        String cardno = "", cardholder = "", cardreferencenumber = "", cardtype = "", globallevelcustomfields = "", globalleveldimensions = "",invtaxpercent = "", supplierinvoiceno = "";
        SimpleDateFormat sdf = new SimpleDateFormat(CustomDesignerConstants.DateFormat_RemovingTime);
        //KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), requestJobj.optString(Constants.globalCurrencyKey));
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

        String recordIDs = requestJobj.optString("recordids") != null ? requestJobj.optString("recordids") : "";
        String recArray[] = recordIDs.split(",");
        int moduleid = Integer.parseInt(requestJobj.optString(Constants.moduleid));
        int countryid = 0;
        try {
            HashMap<String, Integer> FieldMap = (HashMap<String, Integer>) paramMap.get(Constants.fieldMap);
            HashMap<String, String> replaceFieldMap = (HashMap<String, String>) paramMap.get(Constants.replaceFieldMap);
            HashMap<String, Integer> DimensionFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.dimensionFieldMap);
            HashMap<String, Integer> LineLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.lineLevelCustomFieldMap);
            String companyid = requestJobj.optString(Constants.companyKey);
            KwlReturnObject resultcompany = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), requestJobj.optString(Constants.companyKey));
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) resultcompany.getEntityList().get(0);
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);

            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            if( extraCompanyPreferences.getCompany()!= null && extraCompanyPreferences.getCompany().getCountry() != null ){
                countryid = Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID());
            }
            
            if (recArray.length != 0) {
                paymentId = SOID;

                JSONArray DataJArr = new JSONArray();
                JSONObject jobj = new JSONObject();
                int receiptType = 0, count = 1, rowcnt = 0;
                List<JSONObject> tempList = new ArrayList<JSONObject>();
                HashMap<String, Object> rRequestParams = new HashMap<String, Object>();
                HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                int mode = 0;
                if(!StringUtil.isNullOrEmpty(requestJobj.optString("mode"))) {
                    mode = Integer.parseInt(requestJobj.optString("mode"));
                } else {
                    mode = companyAccountPreferences.isWithoutInventory() ? StaticValues.AUTONUM_BILLINGPAYMENT : StaticValues.AUTONUM_PAYMENT;
                }
                companyid = (requestJobj.optString("companyid") != null) ? requestJobj.optString("companyid") :requestJobj.optString(Constants.companyid);
                String gcurrencyid = (requestJobj.optString("gcurrencyid") != null) ? requestJobj.optString("gcurrencyid") :requestJobj.optString(Constants.globalCurrencyKey);
                /*
                 * a variable to check the record has pending approval or not
                 */
                boolean ispendingAproval = (requestJobj.optString("ispendingAproval") != null) ? Boolean.parseBoolean(requestJobj.optString("ispendingAproval").toString()) : false;
                String userid = requestJobj.optString(Constants.useridKey);
                KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) cmpresult.getEntityList().get(0);
                String filename = "", customerOrVendorTitle = "",VATTInnumber = "",CSTTInNumber = "";
                HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(requestJobj);
                requestParams.put("billid", paymentId);
                //request.setAttribute("companyid", companyid);
                //request.setAttribute("gcurrencyid", gcurrencyid);
                requestJobj.put("companyid", companyid);
                requestJobj.put("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                /*
                 * putting variables in map
                 */
                requestParams.put("ispendingAproval", ispendingAproval);
                requestParams.put("userid", userid);
                result = accVendorPaymentobj.getPayments(requestParams);
                tempList = getPaymentsJsonNew(requestParams, result.getEntityList(), tempList);
                requestParams.put("dateformat", authHandler.getDateOnlyFormat());
                //requestParams.put("userdateformat", authHandler.getUserDateFormatterWithoutTimeZone(request));
                DateFormat df = new SimpleDateFormat(requestJobj.optString(Constants.userdateformat));
                requestParams.put("userdateformat", df);
                requestParams.put("bills", paymentId.split(","));
                requestParams.put("isReceiptEdit", "true");
                requestParams.put("isForReport", true);
                DataJArr = getPaymentDetailJsonNew(requestParams);

                /**
                 * sort and arrange row data as per sequence in UI
                 */
                JSONObject dataJobj = null;
                Map<Integer, JSONObject> mapForSort = new TreeMap<Integer, JSONObject>();
                for (int i = 0; i < DataJArr.length(); i++) {
                    dataJobj = DataJArr.getJSONObject(i);
                    JSONArray arr = dataJobj.getJSONArray("typedata");
                    for (int j = 0; j < arr.length(); j++) {
                        JSONObject rowDataObj = arr.getJSONObject(j);
                        int srNoForRow = 0;
                        srNoForRow = rowDataObj.getInt("srnoforrow");
                        int type = dataJobj.getInt("type");
                        if (type != Constants.AdvanceLinkedWithInvoicePayment) {
                            if (type == Constants.GLPayment) {
                                JSONObject tempJobj = new JSONObject();
                                tempJobj.put("type", type);
                                tempJobj.put("typedata", new JSONArray().put(rowDataObj));
                                mapForSort.put(srNoForRow, tempJobj);
                            } else if (type == Constants.PaymentAgainstInvoice) {
                                JSONObject tempJobj = new JSONObject();
                                tempJobj.put("type", type);
                                tempJobj.put("typedata", new JSONArray().put(rowDataObj));
                                mapForSort.put(srNoForRow, tempJobj);
                            }else if (type == Constants.PaymentAgainstCNDN){
                                JSONObject tempJobj = new JSONObject();
                                tempJobj.put("type", type);
                                tempJobj.put("typedata", new JSONArray().put(rowDataObj));
                                mapForSort.put(srNoForRow, tempJobj);
                            } else {
                                mapForSort.put(srNoForRow, dataJobj);
                            }
                        }
                    }
                }
                // create new array of row objects after sorting
                JSONArray tempDataJArr = new JSONArray();
                for (int i = 0; i < mapForSort.size(); i++) {
                    int key = (Integer) mapForSort.keySet().toArray()[i];
                    tempDataJArr.put(mapForSort.get(key));
                }
                DataJArr = tempDataJArr; // replace new arranged array to old array
                
                JSONObject obj = tempList.get(0);
                int paymentwindowtype = (Integer) obj.get("paymentwindowtype");

                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), SOID);
                pc = (Payment) objItr.getEntityList().get(0);
                currencysymbol = pc.getCurrency().getSymbol();
                createdby = pc.getCreatedby() != null ? pc.getCreatedby().getFullName() : "";
                updatedby = pc.getModifiedby() != null ? pc.getModifiedby().getFullName() : "";
                double externalCurrencyRate = pc.getExternalCurrencyRate();
                double exchangerate = 0.0;
                if (externalCurrencyRate != 0.0) {
                    exchangerate = 1 / externalCurrencyRate;
                }
                //document currency
                if (pc != null && pc.getCurrency() != null && !StringUtil.isNullOrEmpty(pc.getCurrency().getCurrencyID())) {
                    summaryData.put(CustomDesignerConstants.DOCUMENT_CURRENCY_ID, pc.getCurrency().getCurrencyID());
                }
                /**
                 * get customer/vendor title (Mr./Mrs.)
                 */
                if(!StringUtil.isNullOrEmpty(pc.getCustomer())){
                    KwlReturnObject customerResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), pc.getCustomer());
                    Customer customer = (Customer) customerResult.getEntityList().get(0);
                    customerOrVendorTitle = customer.getTitle();
                    VATTInnumber = customer.getVATTINnumber() != null ? customer.getVATTINnumber() : "";
                    CSTTInNumber = customer.getCSTTINnumber() != null ? customer.getCSTTINnumber() : "";
                    gstin = customer.getGSTIN() != null ? customer.getGSTIN() : "";
                } else if (pc.getVendor() != null) {
                    customerOrVendorTitle = pc.getVendor().getTitle();
                    VATTInnumber = pc.getVendor().getVATTINnumber() != null ? pc.getVendor().getVATTINnumber() : "";
                    CSTTInNumber = pc.getVendor().getCSTTINnumber() != null ? pc.getVendor().getCSTTINnumber() : "";
                    gstin = pc.getVendor().getGSTIN() != null ? pc.getVendor().getGSTIN() : "";
                }
                if(!StringUtil.isNullOrEmpty(customerOrVendorTitle)){
                    KwlReturnObject masterItemResult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), customerOrVendorTitle);
                    MasterItem masterItem = (MasterItem) masterItemResult.getEntityList().get(0);
                    customerOrVendorTitle = masterItem.getValue();
                }
                KwlReturnObject templateConfig = accCommonTablesDAO.getPDFTemplateRow(companyid, Integer.parseInt(requestJobj.optString("moduleid")));
                if (templateConfig.getEntityList().size() > 0) {
                    config = (PdfTemplateConfig) templateConfig.getEntityList().get(0);
                }
//                    voucherMap.put("COMPANY_NAME", company.getCompanyName() == null ? "" : company.getCompanyName());
//                    voucherMap.put("COMPANY_ADDRESS", company.getAddress() == null ? "" : company.getAddress());
//                    voucherMap.put("COMPANY_PHONE", company.getPhoneNumber() == null ? "" : company.getPhoneNumber());
//                    voucherMap.put("COMPANY_FAX", company.getFaxNumber() == null ? "" : company.getFaxNumber());
//                    voucherMap.put("COMPANY_EMAIL", company.getEmailID() == null ? "" : company.getEmailID());

                int srnumber = 0;
                for (int i = 0; i < DataJArr.length(); i++) {
                    jobj = DataJArr.getJSONObject(i);
                    int detailtype = (Integer) jobj.get("type");
                    if (detailtype == Constants.PaymentAgainstInvoice) {

                        JSONArray arr = jobj.getJSONArray("typedata");
                        for (int j = 0; j < arr.length(); j++) {
                            JSONObject tempobj = new JSONObject();
                            JSONObject data = arr.getJSONObject(j);
                            String taxname = "", taxpercent = "", taxcode = "";
                            String invDesc = "Against Invoice# " + data.getString("transectionno") + " dated " + data.getString("creationdateinuserformat");
                            if (data.getString("totalamount").equals(data.getString("amountpaid"))) {
                                invDesc += " (Full)";
                            }
//                            if (!data.optString("taxamount", "0.0").equals("0.0")) {  //SDP-10638
                                taxpercent = data.optString("taxpercent", "");
                                taxname = data.optString("taxname", "").equals("") ? "" : data.optString("taxname", "");
                                taxcode = data.optString("taxcode", "").equals("") ? "" : data.optString("taxcode", "");
                                invoiceTotalDiscount += data.optDouble("discount", 0) ;

//                            } else {
//                                taxpercent = data.optString("taxpercent", "");  //SDP-10638
//                                taxname = "";
//                                taxcode = "";
//                            }
                            taxpercent += "%";
                            invexchangerate = "1 " + data.getString("currencysymboltransaction") + " = " + accountingHandlerDAOobj.formatDouble(data.optDouble("exchangeratefortransaction", 1.0)) + " " + pc.getCurrency().getSymbol();

                            //to calculate linking information in Vendor Invoice
                            filter_names.clear();
                            filter_names.add("goodsReceipt.ID");  //goodsreceipt is the database name
                            order_by.add("srno");
                            order_type.add("asc");
                            invRequestParams.put("filter_names", filter_names);
                            invRequestParams.put("filter_params", filter_params);
                            invRequestParams.put("order_by", order_by);
                            invRequestParams.put("order_type", order_type);
//            KwlReturnObject goodsreceiptresult = kwlCommonTablesDAOObj.getObject(GoodsReceipt.class.getName(),data.getString("transectionid"));
//            GoodsReceipt goodsReceipt = (GoodsReceipt) goodsreceiptresult.getEntityList().get(0);
                            filter_params.clear();
                            filter_params.add(data.getString("transectionid"));
                            KwlReturnObject idresult = accGoodsReceiptobj.getGoodsReceiptDetails(invRequestParams);

                            Iterator goodsreceiptitr = idresult.getEntityList().iterator();
                            GoodsReceiptDetail grprow = null;
                            boolean vqouteRef = false;
                            boolean grOref = false;
                            boolean poref = false;
                            while (goodsreceiptitr.hasNext()) {
                                rowcnt++;
                                grprow = (GoodsReceiptDetail) goodsreceiptitr.next();
                                if (grprow.getGoodsReceiptOrderDetails() != null) {
                                    if (GROref.indexOf(grprow.getGoodsReceiptOrderDetails().getGrOrder().getGoodsReceiptOrderNumber()) == -1) {
                                        GROref += grprow.getGoodsReceiptOrderDetails().getGrOrder().getGoodsReceiptOrderNumber() + ",";
                                        grOref = true;
                                    }

                                } else if (grprow.getPurchaseorderdetail() != null) {
                                    if (POref.indexOf(grprow.getPurchaseorderdetail().getPurchaseOrder().getPurchaseOrderNumber()) == -1) {
                                        POref += grprow.getPurchaseorderdetail().getPurchaseOrder().getPurchaseOrderNumber() + ",";
                                        poref = true;

                                    }
                                } else if (grprow.getVendorQuotationDetail() != null) {
                                    if (VQouteRef.indexOf(grprow.getVendorQuotationDetail().getVendorquotation().getQuotationNumber()) == -1) {
                                        VQouteRef += grprow.getVendorQuotationDetail().getVendorquotation().getQuotationNumber() + ",";
                                        vqouteRef = true;
                                    }
                                }
                            }

                            if (grOref) {//removing comma
                                GROref = GROref.substring(0, GROref.length() - 1);

                            } else if (poref) {
                                POref = POref.substring(0, POref.length() - 1);
                            } else if (vqouteRef) {
                                VQouteRef = VQouteRef.substring(0, VQouteRef.length() - 1);
                            }

                            // ## Get Custom Field Data 

                            for (Map.Entry<String, Integer> field : FieldMap.entrySet()) {//checks when field has any value
                                if (!data.has(field.getKey())) {
                                    summaryData.put(field.getKey(), "");
                                    tempobj.put(field.getKey(), "");
                                } else {
                                    summaryData.put(field.getKey(), "");
                                    tempobj.put(field.getKey(), data.getString(field.getKey()));
                                }
                            }

                            /*
                             * Putting Line level CustomFields and LIne level Dimensions
                             */
                            HashMap<String, Object> returnvalues = new HashMap<String, Object>();
                            HashMap<String, Object> extraparams = new HashMap<String, Object>();
                            extraparams.put("data", data);
                            extraparams.put("tempobj", tempobj);
                            extraparams.put(CustomDesignerConstants.isCustomfield, "false");
                            returnvalues = CustomDesignHandler.setDimensionCustomfieldValuesforMPRP(DimensionFieldMap, extraparams);//for line level dimensions
                            if (returnvalues.containsKey("tempobj")) {
                                tempobj = (JSONObject) returnvalues.get("tempobj");
                            }
                            extraparams.put(CustomDesignerConstants.isCustomfield, "true");
                            returnvalues = CustomDesignHandler.setDimensionCustomfieldValuesforMPRP(LineLevelCustomFieldMap, extraparams);//for line level customfields
                            if (returnvalues.containsKey("tempobj")) {
                                tempobj = (JSONObject) returnvalues.get("tempobj");
                            }
                            String customcurrencysymbol = accCommonTablesDAO.getCustomCurrencySymbol(data.getString("currencysymboltransaction"), companyid);//Take custom currency symbol
                            tempobj.put(CustomDesignerConstants.RPMP_DocumentType, "Invoice");
                            tempobj.put(CustomDesignerConstants.RPMP_DocumentNumber, data.getString("transectionno"));
                            tempobj.put(CustomDesignerConstants.RPMP_Description, StringUtil.DecodeText(StringUtil.isNullOrEmpty(data.optString("description", "")) ? "" : data.optString("description", "")));
                            tempobj.put(CustomDesignerConstants.RPMP_OriginalAmountDue, authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("amountDueOriginal", "0.0")), companyid));
                            tempobj.put(CustomDesignerConstants.RPMP_ExchangeRate, invexchangerate);
                            tempobj.put(CustomDesignerConstants.RPMP_AmountDue, authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("amountduenonnegative", "0.0")), companyid));
                            tempobj.put(CustomDesignerConstants.RPMP_TAXNAME, taxname);
                            tempobj.put(CustomDesignerConstants.RPMP_Tax, taxpercent);
                            tempobj.put(CustomDesignerConstants.RPMP_TaxCode, taxcode);
                            tempobj.put(CustomDesignerConstants.RPMP_TaxAmount, Double.parseDouble(data.optString("taxamount", "0.0")));
                            tempobj.put(CustomDesignerConstants.RPMP_EnterPayment, Double.parseDouble(data.optString("enteramount", "0.0")));
                            tempobj.put(CustomDesignerConstants.RPMP_ENTER_PAYMENT_WITH_TAX, data.optDouble("enteramount", 0.0) + data.optDouble("taxamount", 0.0));

                            tempobj.put(CustomDesignerConstants.RPMP_InvoiceDate, data.getString("creationdateinuserformat"));
                            tempobj.put(CustomDesignerConstants.RPMP_DueDate, data.getString("duedate"));
                            tempobj.put(CustomDesignerConstants.RPMP_Discount, authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("discount", "0.0")), companyid));
                            tempobj.put(CustomDesignerConstants.RPMP_OriginalAmount, authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("amountDueOriginal", "0.0")), companyid));
                            tempobj.put(CustomDesignerConstants.CustomDesignCurrencySymbol_fieldTypeId, customcurrencysymbol);
                            tempobj.put(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance, invDesc);
                            tempobj.put(CustomDesignerConstants.Common_EnterPayment, authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("enteramount", "0.0")), companyid));
                            tempobj.put(CustomDesignerConstants.CustomDesignPORefNumber_fieldTypeId, POref);
                            tempobj.put(CustomDesignerConstants.CustomDesignVendorQuoteRefNumber_fieldTypeId, VQouteRef);
                            tempobj.put(CustomDesignerConstants.CustomDesignGRORefNumber_fieldTypeId, GROref);
                            tempobj.put(CustomDesignerConstants.SrNo, count);
                            tempobj.put(CustomDesignerConstants.RPMP_CreditAmount, "");
                            tempobj.put(CustomDesignerConstants.RPMP_DebitAmount, "");
                            tempobj.put(CustomDesignerConstants.RPMP_ACCOUNT_CODE, "");
                            /*
                             * getting supplier invoice no
                             */
                            tempobj.put(CustomDesignerConstants.SUPPLIER_INVOICE_NO, data.optString("supplierinvoiceno"));
                            //GST Exchange Rate
                            if (countryid == Integer.parseInt(Constants.SINGAPOREID) && !companyAccountPreferences.getCompany().getCurrency().getCurrencyID().equalsIgnoreCase(Constants.SGDID)) {
                                tempobj.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, data.optDouble(Constants.GST_CURRENCY_RATE, 0.0) != 0.0 ? data.optDouble(Constants.GST_CURRENCY_RATE, 0.0) : "");
                            }
                            tempobj.put(CustomDesignerConstants.DOCUMENTSTATUS, data.optString("totalamount","0").equals(data.optString("amountpaid","0")) ? CustomDesignerConstants.FULL : CustomDesignerConstants.PARTIAL);
                            // TDS Fields
                            tempobj.put(CustomDesignerConstants.TDS_RATE, 0);
                            tempobj.put(CustomDesignerConstants.TDS_AMOUNT, data.optString(CustomDesignerConstants.TDS_AMOUNT));
                            invJSONArr.put(tempobj);
                            count++;
                            originalamountduetotal += Double.parseDouble(data.optString("amountDueOriginal", "0.0"));  //ERP-19271
                        }
                    } else if (detailtype == Constants.GLPayment) {
                        JSONArray arr = jobj.getJSONArray("typedata");
                        for (int j = 0; j < arr.length(); j++) {
                            JSONObject tempobj = new JSONObject();
                            JSONObject data = arr.getJSONObject(j);
                            String taxname = "";
                            String taxpercent = "";
                            String taxcode = "";
                            String invDesc = "Against GL Account " + (data.optString("accountcode", "").equals("") ? "" : data.optString("accountcode", "") + " - ") + data.getString("accountname");
//                            if (!data.optString("taxamount", "0.0").equals("0.0")) {		//SDP-10638
                                taxpercent = data.optString("taxpercent", "");
                                taxname = data.optString("taxname", "").equals("") ? "" : data.optString("taxname", "");
                                taxcode = data.optString("taxcode", "").equals("") ? "" : data.optString("taxcode", "");

//                            } else {
//                                taxpercent = data.optString("taxpercent", "");		//SDP-10638
//                                taxname = "";
//                                taxcode = "";
//                            }
                            taxpercent +="%";
                            invexchangerate = "1 " + pc.getCurrency().getSymbol() + " = 1 " + pc.getCurrency().getSymbol();

                            // ## Get Custom Field Data 
                            for (Map.Entry<String, Integer> field : FieldMap.entrySet()) {//checks when field has any value
                                if (!data.has(field.getKey())) {
                                    summaryData.put(field.getKey(), "");
                                    tempobj.put(field.getKey(), "");
                                } else {
                                    summaryData.put(field.getKey(), "");
                                    tempobj.put(field.getKey(), data.getString(field.getKey()));
                                }
                            }

                            /*
                             * Putting Line level CustomField and LIne level Dimensions
                             */
                            HashMap<String, Object> returnvalues = new HashMap<String, Object>();
                            HashMap<String, Object> extraparams = new HashMap<String, Object>();
                            extraparams.put("data", data);
                            extraparams.put("tempobj", tempobj);
                            extraparams.put(CustomDesignerConstants.isCustomfield, "false");
                            returnvalues = CustomDesignHandler.setDimensionCustomfieldValuesforMPRP(DimensionFieldMap, extraparams);//for line level dimensions
                            if (returnvalues.containsKey("tempobj")) {
                                tempobj = (JSONObject) returnvalues.get("tempobj");
                            }
                            extraparams.put(CustomDesignerConstants.isCustomfield, "true");
                            returnvalues = CustomDesignHandler.setDimensionCustomfieldValuesforMPRP(LineLevelCustomFieldMap, extraparams);//for line level customfields
                            if (returnvalues.containsKey("tempobj")) {
                                tempobj = (JSONObject) returnvalues.get("tempobj");
                            }

                            tempobj.put(CustomDesignerConstants.RPMP_DocumentType, "General Ledger");
                            tempobj.put(CustomDesignerConstants.RPMP_DocumentNumber, StringUtil.isNullOrEmpty(data.getString("accountname")) ? data.getString("accountcode") : data.getString("accountname"));
                            tempobj.put(CustomDesignerConstants.RPMP_Description, StringUtil.DecodeText(StringUtil.isNullOrEmpty(data.optString("description","")) ? "" : data.optString("description","")));
                            tempobj.put(CustomDesignerConstants.RPMP_OriginalAmountDue, authHandler.formattedCommaSeparatedAmount(Double.parseDouble("0.0"), companyid));
                            tempobj.put(CustomDesignerConstants.RPMP_ExchangeRate, invexchangerate);
                            tempobj.put(CustomDesignerConstants.RPMP_AmountDue, authHandler.formattedCommaSeparatedAmount(Double.parseDouble("0.0"), companyid));
                            tempobj.put(CustomDesignerConstants.RPMP_TAXNAME, taxname);
                            tempobj.put(CustomDesignerConstants.RPMP_Tax, taxpercent);
                            tempobj.put(CustomDesignerConstants.RPMP_TaxCode, taxcode);
                            tempobj.put(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance, invDesc);
                            if (data.optString("debit", "false").equals("false")) {
                                tempobj.put(CustomDesignerConstants.Common_EnterPayment, authHandler.formattedCommaSeparatedAmount(Double.parseDouble("-" + data.getString("totalamount")), companyid));
                                tempobj.put(CustomDesignerConstants.RPMP_EnterPayment, Double.parseDouble("-" + data.getString("totalamount")));
                                tempobj.put(CustomDesignerConstants.RPMP_ENTER_PAYMENT_WITH_TAX, data.optDouble("totalamount", 0.0) + data.optDouble("taxamount", 0.0));
                                tempobj.put(CustomDesignerConstants.RPMP_TaxAmount, Double.parseDouble(data.optString("taxamount", "0.0").equals("0.0") ? "0.0" : "-" + data.optString("taxamount", "0.0")));
                                tempobj.put(CustomDesignerConstants.RPMP_CreditAmount, authHandler.formattedCommaSeparatedAmount(Double.parseDouble("-" + data.getString("totalamount")), companyid));
                                tempobj.put(CustomDesignerConstants.RPMP_DebitAmount,"");
                            } else {
                                tempobj.put(CustomDesignerConstants.Common_EnterPayment, authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.getString("totalamount")), companyid));
                                tempobj.put(CustomDesignerConstants.RPMP_EnterPayment,Double.parseDouble(data.getString("totalamount")));
                                tempobj.put(CustomDesignerConstants.RPMP_ENTER_PAYMENT_WITH_TAX, data.optDouble("totalamount", 0.0) + data.optDouble("taxamount", 0.0));
                                tempobj.put(CustomDesignerConstants.RPMP_TaxAmount, Double.parseDouble(data.optString("taxamount", "0.0")));
                                tempobj.put(CustomDesignerConstants.RPMP_CreditAmount,"");
                                tempobj.put(CustomDesignerConstants.RPMP_DebitAmount,authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.getString("totalamount")), companyid));
                            }
                            tempobj.put(CustomDesignerConstants.CustomDesignPORefNumber_fieldTypeId, POref);
                            tempobj.put(CustomDesignerConstants.CustomDesignVendorQuoteRefNumber_fieldTypeId, VQouteRef);
                            tempobj.put(CustomDesignerConstants.CustomDesignGRORefNumber_fieldTypeId, GROref);
                            tempobj.put(CustomDesignerConstants.RPMP_InvoiceDate,"");
                            tempobj.put(CustomDesignerConstants.RPMP_DueDate,"");
                            tempobj.put(CustomDesignerConstants.RPMP_Discount,"");
                            tempobj.put(CustomDesignerConstants.RPMP_OriginalAmount,"");
                            tempobj.put(CustomDesignerConstants.CustomDesignCurrencySymbol_fieldTypeId,"");
                            tempobj.put(CustomDesignerConstants.RPMP_ACCOUNT_CODE, data.optString("accountcode", ""));
                            tempobj.put(CustomDesignerConstants.SrNo, count);
                            tempobj.put(CustomDesignerConstants.SUPPLIER_INVOICE_NO, data.optString("supplierinvoiceno"));
                            tempobj.put(CustomDesignerConstants.DOCUMENTSTATUS,"-");
                            tempobj.put(CustomDesignerConstants.TDS_RATE, 0);
                            tempobj.put(CustomDesignerConstants.TDS_AMOUNT, 0);
                            invJSONArr.put(tempobj);
                            count++;
                        }
                    } else if (detailtype == Constants.PaymentAgainstCNDN) {

                        JSONArray arr = jobj.getJSONArray("typedata");
                        for (int j = 0; j < arr.length(); j++) {
                            String taxname = "", taxpercent = "", taxcode = "";
                            JSONObject tempobj = new JSONObject();
                            JSONObject data = arr.getJSONObject(j);
                            String invDesc = mode == StaticValues.AUTONUM_PAYMENT ? "Against Credit Note# " : "Against Debit Note# ";
                            invDesc += data.getString("transectionno") + " dated " + data.getString("creationdate");
                            if (data.getString("totalamount").equals(data.getString("cnpaidamount"))) {
                                invDesc += " (Full)";
                            }
//                            if (!data.optString("taxamount", "0.0").equals("0.0")) {
                                taxpercent = data.optString("taxpercent", "");
                                taxname = data.optString("taxname", "").equals("") ? "" : data.optString("taxname", "");
                                taxcode = data.optString("taxcode", "").equals("") ? "" : data.optString("taxcode", "");

//                            } else {
//                                taxpercent = data.optString("taxpercent", "");
//                                taxname = "";
//                                taxcode = "";
//                            }
                            taxpercent +="%";
                            invexchangerate = "1 " + data.getString("currencysymboltransaction") + " = " + accountingHandlerDAOobj.formatDouble(data.optDouble("exchangeratefortransaction", 1.0)) + " " + pc.getCurrency().getSymbol();

                            // ## Get Custom Field Data 
                            for (Map.Entry<String, Integer> field : FieldMap.entrySet()) {//checks when field has any value
                                if (!data.has(field.getKey())) {
                                    summaryData.put(field.getKey(), "");
                                    tempobj.put(field.getKey(), "");
                                } else {
                                    summaryData.put(field.getKey(), "");
                                    tempobj.put(field.getKey(), data.getString(field.getKey()));
                                }
                            }

                            /*
                             * Putting Line level CustomField and LIne level Dimensions
                             */
                            HashMap<String, Object> returnvalues = new HashMap<String, Object>();
                            HashMap<String, Object> extraparams = new HashMap<String, Object>();
                            extraparams.put("data", data);
                            extraparams.put("tempobj", tempobj);
                            extraparams.put(CustomDesignerConstants.isCustomfield, "false");
                            returnvalues = CustomDesignHandler.setDimensionCustomfieldValuesforMPRP(DimensionFieldMap, extraparams);//for line level dimensions
                            if (returnvalues.containsKey("tempobj")) {
                                tempobj = (JSONObject) returnvalues.get("tempobj");
                            }
                            extraparams.put(CustomDesignerConstants.isCustomfield, "true");
                            returnvalues = CustomDesignHandler.setDimensionCustomfieldValuesforMPRP(LineLevelCustomFieldMap, extraparams);//for line level customfields
                            if (returnvalues.containsKey("tempobj")) {
                                tempobj = (JSONObject) returnvalues.get("tempobj");
                            }

                            tempobj.put(CustomDesignerConstants.RPMP_DocumentType, "Credit Note");
                            tempobj.put(CustomDesignerConstants.RPMP_DocumentNumber, data.optString("transectionno"));
                            tempobj.put(CustomDesignerConstants.RPMP_Description,  StringUtil.DecodeText(StringUtil.isNullOrEmpty(data.optString("description", "")) ? "" : data.optString("description", "")));
                            tempobj.put(CustomDesignerConstants.RPMP_OriginalAmountDue, authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("totalamount", "0.0")), companyid));
                            tempobj.put(CustomDesignerConstants.RPMP_ExchangeRate, invexchangerate);
                            tempobj.put(CustomDesignerConstants.RPMP_AmountDue, authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("newamountdue", "0.0")), companyid));
                            tempobj.put(CustomDesignerConstants.RPMP_TAXNAME, taxname);
                            tempobj.put(CustomDesignerConstants.RPMP_Tax, taxpercent);
                            tempobj.put(CustomDesignerConstants.RPMP_TaxCode, taxcode);
                            tempobj.put(CustomDesignerConstants.RPMP_TaxAmount, Double.parseDouble(data.optString("taxamount", "0.0")));
                            tempobj.put(CustomDesignerConstants.RPMP_EnterPayment, Double.parseDouble(data.optString("enteramount", "0.0")));
                            tempobj.put(CustomDesignerConstants.RPMP_ENTER_PAYMENT_WITH_TAX, data.optDouble("enteramount", 0.0) + data.optDouble("taxamount", 0.0));
                            tempobj.put(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance, invDesc);
                            tempobj.put(CustomDesignerConstants.Common_EnterPayment, authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("enteramount", "0.0")), companyid));
                            tempobj.put(CustomDesignerConstants.CustomDesignPORefNumber_fieldTypeId, POref);
                            tempobj.put(CustomDesignerConstants.CustomDesignVendorQuoteRefNumber_fieldTypeId, VQouteRef);
                            tempobj.put(CustomDesignerConstants.CustomDesignGRORefNumber_fieldTypeId, GROref);
                            tempobj.put(CustomDesignerConstants.RPMP_InvoiceDate, "");
                            tempobj.put(CustomDesignerConstants.RPMP_DueDate, "");
                            tempobj.put(CustomDesignerConstants.RPMP_Discount, "");
                            tempobj.put(CustomDesignerConstants.RPMP_OriginalAmount, "");
                            tempobj.put(CustomDesignerConstants.CustomDesignCurrencySymbol_fieldTypeId, "");
                            tempobj.put(CustomDesignerConstants.SrNo, count);
                            tempobj.put(CustomDesignerConstants.RPMP_CreditAmount, "");
                            tempobj.put(CustomDesignerConstants.RPMP_DebitAmount, "");
                            tempobj.put(CustomDesignerConstants.RPMP_ACCOUNT_CODE, "");
                            //GST Exchange Rate
                            if (countryid == Integer.parseInt(Constants.SINGAPOREID) && !companyAccountPreferences.getCompany().getCurrency().getCurrencyID().equalsIgnoreCase(Constants.SGDID)) {
                                tempobj.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, authHandler.formattedAmount(data.optDouble(Constants.GST_CURRENCY_RATE, 0.0), companyid));
                            }
                            tempobj.put(CustomDesignerConstants.DOCUMENTSTATUS, data.optString("totalamount","0").equals(data.optString("cnpaidamount","0")) ? CustomDesignerConstants.FULL : CustomDesignerConstants.PARTIAL);
                            tempobj.put(CustomDesignerConstants.TDS_RATE, 0);
                            tempobj.put(CustomDesignerConstants.TDS_AMOUNT, 0);
                            invJSONArr.put(tempobj);
                            tempobj.put(CustomDesignerConstants.SUPPLIER_INVOICE_NO, data.optString("supplierinvoiceno"));
                            count++;
                            originalamountduetotal += Double.parseDouble(data.optString("totalamount", "0.0"));  //ERP-19271
                        }

                    } else if (detailtype == Constants.AdvancePayment) {
                        JSONArray arr = jobj.getJSONArray("typedata");
                        for (int j = 0; j < arr.length(); j++) {
                            JSONObject tempobj = new JSONObject();
                            JSONObject data = arr.getJSONObject(j);
                            String invDesc = "Advance Amount ";
                            invDesc += (mode == StaticValues.AUTONUM_PAYMENT ? " to " : " from ");
                            if (!data.optString("acccode", "").equals("") && !data.optString("accname", "").equals("")) {
                                invDesc += data.getString("acccode") + "-" + data.getString("accname");
                                if (!data.optString("accountcode", "").equals("") && !data.optString("accountname", "").equals("")) {
                                    invDesc += " (" + data.getString("accountcode") + "-" + data.getString("accountname") + ")";
                                } else if (!data.optString("accountname", "").equals("")) {
                                    invDesc += " (" + data.getString("accountname") + ")";
                                }
                            } else if (!data.optString("accname", "").equals("")) {
                                invDesc += data.getString("accname");
                                if (!data.optString("accountcode", "").equals("") && !data.optString("accountname", "").equals("")) {
                                    invDesc += " (" + data.getString("accountcode") + "-" + data.getString("accountname") + ")";
                                } else if (!data.optString("accountname", "").equals("")) {
                                    invDesc += " (" + data.getString("accountname") + ")";
                                }
                            }   // This will add the account name with code and then account holder name with code  

                            invexchangerate = "1 " + pc.getCurrency().getSymbol() + " = 1 " + pc.getCurrency().getSymbol();

                            // ## Get Custom Field Data 
                            for (Map.Entry<String, Integer> field : FieldMap.entrySet()) {//checks when field has any value
                                if (!data.has(field.getKey())) {
                                    summaryData.put(field.getKey(), "");
                                    tempobj.put(field.getKey(), "");
                                } else {
                                    summaryData.put(field.getKey(), "");
                                    tempobj.put(field.getKey(), data.getString(field.getKey()));
                                }
                            }
                            /*
                             * Putting Line level CustomField and LIne level Dimensions
                             */
                            HashMap<String, Object> returnvalues = new HashMap<String, Object>();
                            HashMap<String, Object> extraparams = new HashMap<String, Object>();
                            extraparams.put("data", data);
                            extraparams.put("tempobj", tempobj);
                            extraparams.put(CustomDesignerConstants.isCustomfield, "false");
                            returnvalues = CustomDesignHandler.setDimensionCustomfieldValuesforMPRP(DimensionFieldMap, extraparams);//for line level dimensions
                            if (returnvalues.containsKey("tempobj")) {
                                tempobj = (JSONObject) returnvalues.get("tempobj");
                            }
                            extraparams.put(CustomDesignerConstants.isCustomfield, "true");
                            returnvalues = CustomDesignHandler.setDimensionCustomfieldValuesforMPRP(LineLevelCustomFieldMap, extraparams);//for line level customfields
                            if (returnvalues.containsKey("tempobj")) {
                                tempobj = (JSONObject) returnvalues.get("tempobj");
                            }

                            if (paymentwindowtype == Constants.Make_Payment_to_Vendor) {
                                tempobj.put(CustomDesignerConstants.RPMP_DocumentType, "Advance /Deposit");
                            } else if (paymentwindowtype == Constants.Make_Payment_to_Customer) {
                                tempobj.put(CustomDesignerConstants.RPMP_DocumentType, "Refund /Deposit");
                            }
                            /**
                             * GST details
                             */
                            if(extraCompanyPreferences.isIsNewGST() && countryid == Constants.indian_country_id){ // for India GST only
                                //Put 0 as default value in all tax amount and percent fields
                                tempobj.put(CustomDesignerConstants.CGSTPERCENT, 0);
                                tempobj.put(CustomDesignerConstants.CGSTAMOUNT, 0);
                                tempobj.put(CustomDesignerConstants.IGSTPERCENT, 0);
                                tempobj.put(CustomDesignerConstants.IGSTAMOUNT, 0);
                                tempobj.put(CustomDesignerConstants.SGSTPERCENT, 0);
                                tempobj.put(CustomDesignerConstants.SGSTAMOUNT, 0);
                                tempobj.put(CustomDesignerConstants.UTGSTPERCENT, 0);
                                tempobj.put(CustomDesignerConstants.UTGSTAMOUNT, 0);
                                tempobj.put(CustomDesignerConstants.CESSPERCENT, 0);
                                tempobj.put(CustomDesignerConstants.CESSAMOUNT, 0);
                                
                                requestJobj.put("adId", data.optString("rowid", ""));
                                //get tax details of payment Made
                                KwlReturnObject mpTermMapresult = accVendorPaymentobj.getAdvanceDetailsTerm(requestJobj);
                                List<AdvanceDetailTermMap> gst = mpTermMapresult.getEntityList();
                                
                                //Put value in tax amount and tax percent fields
                                for (AdvanceDetailTermMap advanceDetailTermMap : gst) {
                                    LineLevelTerms mt = advanceDetailTermMap.getEntitybasedLineLevelTermRate().getLineLevelTerms();
                                    if (mt.getTerm().contains(CustomDesignerConstants.CGST)) {
                                        //CGST Tax
                                        tempobj.put(CustomDesignerConstants.CGSTPERCENT, advanceDetailTermMap.getPercentage());
                                        tempobj.put(CustomDesignerConstants.CGSTAMOUNT, advanceDetailTermMap.getTermamount());
                                    } else if (mt.getTerm().contains(CustomDesignerConstants.IGST)) {
                                        //IGST Tax
                                        tempobj.put(CustomDesignerConstants.IGSTPERCENT, advanceDetailTermMap.getPercentage());
                                        tempobj.put(CustomDesignerConstants.IGSTAMOUNT, advanceDetailTermMap.getTermamount());
                                    } else if (mt.getTerm().contains(CustomDesignerConstants.SGST)) {
                                        //SGST Tax
                                        tempobj.put(CustomDesignerConstants.SGSTPERCENT, advanceDetailTermMap.getPercentage());
                                        tempobj.put(CustomDesignerConstants.SGSTAMOUNT, advanceDetailTermMap.getTermamount());
                                    } else if (mt.getTerm().contains(CustomDesignerConstants.UTGST)) {
                                        //UTGST Tax
                                        tempobj.put(CustomDesignerConstants.UTGSTPERCENT, advanceDetailTermMap.getPercentage());
                                        tempobj.put(CustomDesignerConstants.UTGSTAMOUNT, advanceDetailTermMap.getTermamount());
                                    } else if (mt.getTerm().contains(CustomDesignerConstants.CESS)) {
                                        //CESS Tax
                                        tempobj.put(CustomDesignerConstants.CESSPERCENT, advanceDetailTermMap.getPercentage());
                                        tempobj.put(CustomDesignerConstants.CESSAMOUNT, advanceDetailTermMap.getTermamount());
                                    }
                                }
                                
                                Payment payment = (Payment) accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentId).getEntityList().get(0);
                                if (payment != null) {
                                    Set<AdvanceDetail> set = payment.getAdvanceDetails();
                                    Iterator iterator = set.iterator();
                                    if (iterator.hasNext()) {
                                        AdvanceDetail advanceDetail = (AdvanceDetail) iterator.next();
                                        if (advanceDetail != null) {
                                            ReceiptAdvanceDetail receiptAdvanceDetail = advanceDetail.getReceiptAdvanceDetails();
                                            if (receiptAdvanceDetail != null) {
                                                tempobj.put(CustomDesignerConstants.ProductName, receiptAdvanceDetail.getProduct() != null ? receiptAdvanceDetail.getProduct().getName() : "");
                                            }
                                        }
                                    }
                                }
                            }
                            tempobj.put(CustomDesignerConstants.RPMP_DocumentNumber, data.optString("transectionno"));
                            tempobj.put(CustomDesignerConstants.RPMP_Description,  StringUtil.DecodeText(StringUtil.isNullOrEmpty(data.optString("description", "")) ? "" : data.optString("description", "")));
                            tempobj.put(CustomDesignerConstants.RPMP_OriginalAmountDue, authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("amountdue", "0.0")), companyid));
                            tempobj.put(CustomDesignerConstants.RPMP_ExchangeRate, invexchangerate);
                            tempobj.put(CustomDesignerConstants.RPMP_AmountDue, authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("amountdue", "0.0")), companyid));
                            tempobj.put(CustomDesignerConstants.RPMP_TAXNAME, "");
                            tempobj.put(CustomDesignerConstants.RPMP_Tax, "");
                            tempobj.put(CustomDesignerConstants.RPMP_TaxCode, "");
                            tempobj.put(CustomDesignerConstants.RPMP_TaxAmount, Double.parseDouble(data.optString("taxamount", "0.0")));
                            tempobj.put(CustomDesignerConstants.RPMP_EnterPayment, Double.parseDouble(data.optString("totalamount", "0.0")));
                            tempobj.put(CustomDesignerConstants.RPMP_ENTER_PAYMENT_WITH_TAX, data.optDouble("totalamount", 0.0) + data.optDouble("taxamount", 0.0));
                            tempobj.put(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance, invDesc);
                            tempobj.put(CustomDesignerConstants.Common_EnterPayment, authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("totalamount", "0.0")), companyid));
                            tempobj.put(CustomDesignerConstants.CustomDesignPORefNumber_fieldTypeId, POref);
                            tempobj.put(CustomDesignerConstants.CustomDesignVendorQuoteRefNumber_fieldTypeId, VQouteRef);
                            tempobj.put(CustomDesignerConstants.CustomDesignGRORefNumber_fieldTypeId, GROref);
                            tempobj.put(CustomDesignerConstants.RPMP_InvoiceDate, "");
                            tempobj.put(CustomDesignerConstants.RPMP_DueDate, "");
                            tempobj.put(CustomDesignerConstants.RPMP_Discount, "");
                            tempobj.put(CustomDesignerConstants.RPMP_OriginalAmount, "");
                            tempobj.put(CustomDesignerConstants.CustomDesignCurrencySymbol_fieldTypeId, "");
                            tempobj.put(CustomDesignerConstants.SrNo, count);
                            tempobj.put(CustomDesignerConstants.RPMP_CreditAmount, "");
                            tempobj.put(CustomDesignerConstants.RPMP_DebitAmount, "");
                            tempobj.put(CustomDesignerConstants.RPMP_ACCOUNT_CODE, "");
                            tempobj.put(CustomDesignerConstants.SUPPLIER_INVOICE_NO, data.optString("supplierinvoiceno"));
                            tempobj.put(CustomDesignerConstants.DOCUMENTSTATUS, "-");
                            //TDS Fields
                            tempobj.put(CustomDesignerConstants.TDS_RATE, data.optString(CustomDesignerConstants.TDS_RATE));
                            tempobj.put(CustomDesignerConstants.TDS_AMOUNT, data.optString(CustomDesignerConstants.TDS_AMOUNT));
                            invJSONArr.put(tempobj);
                            count++;
                            originalamountduetotal += Double.parseDouble(data.optString("amountdue", "0.0"));  //ERP-19271
                        }
                    }
//                    else if (detailtype == Constants.AdvanceLinkedWithInvoicePayment) {
//                        JSONArray arr = jobj.getJSONArray("typedata");
//                        for (int j = 0; j < arr.length(); j++) {
//                            LabelValue labelValue = new LabelValue();
//                            JSONObject data = arr.getJSONObject(j);
//                            String invDesc = "&nbsp&nbsp&nbsp&nbsp Adjusted Against Invoice# " + data.getString("transectionno") + " dated " + data.getString("creationdateinuserformat");
//                            if (data.getString("totalamount").equals(data.getString("amountpaid"))) {
//                                invDesc += " (Full)";
//                            }
//                            labelValue.setNo("");
//                            labelValue.setLabel(invDesc);
//                            labelValue.setExtravalue(authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.getString("enteramount"))));
//                        }
//                    }
//                     invJSONArr.put(obj);
                }
                billAddr = obj.optString("billaddress", "");
                shipAddr = obj.optString("shipaddress", "");
                double bankcharges =0.0;
                if (!obj.optString("bankCharges", "0.0").equals("0.0")) {
                    bankcharges = Double.parseDouble(obj.getString("bankCharges"));
                }
                if (!obj.optString("bankInterest", "0.0").equals("0.0")) {
                    double bankInterest = Double.parseDouble(obj.getString("bankInterest"));
                }

                int detailType = Integer.parseInt(obj.optString("detailtype", "0"));
                if (detailType == PaymentMethod.TYPE_CASH) {

                    summaryData.put(CustomDesignerConstants.Chequeno, bankchequeno);
                    summaryData.put(CustomDesignerConstants.BankName, chequebankname);
                    summaryData.put(CustomDesignerConstants.ChequeDate, "");
                    summaryData.put(CustomDesignerConstants.BankDescription, "");
                    summaryData.put(CustomDesignerConstants.Cheque_Payment_Status, "");
                    summaryData.put(CustomDesignerConstants.Cheque_Payment_ClearanceDate, "");
                    summaryData.put(CustomDesignerConstants.Card_Reference_Number, cardreferencenumber);
                    summaryData.put(CustomDesignerConstants.CardHolderName, cardholder);
                    summaryData.put(CustomDesignerConstants.ChequeDate, "");
                    summaryData.put(CustomDesignerConstants.CardNo, cardno);
                    summaryData.put(CustomDesignerConstants.Card_Type, cardtype);
                    summaryData.put(CustomDesignerConstants.Card_ExpiryDate, "");

                } else if (detailType == PaymentMethod.TYPE_CARD) {

                    summaryData.put(CustomDesignerConstants.Card_Reference_Number, obj.optString("refno", ""));
                    summaryData.put(CustomDesignerConstants.CardHolderName, obj.optString("refname", ""));
                    summaryData.put(CustomDesignerConstants.CardNo, obj.optString("refcardno", ""));
                    summaryData.put(CustomDesignerConstants.Card_Type, cardtype);
                    summaryData.put(CustomDesignerConstants.Card_ExpiryDate, obj.optString("expirydate", ""));
                    summaryData.put(CustomDesignerConstants.Chequeno, bankchequeno);
                    summaryData.put(CustomDesignerConstants.BankName, chequebankname);
                    summaryData.put(CustomDesignerConstants.ChequeDate, "");
                    summaryData.put(CustomDesignerConstants.BankDescription, "");
                    summaryData.put(CustomDesignerConstants.Cheque_Payment_Status, "");
                    summaryData.put(CustomDesignerConstants.Cheque_Payment_ClearanceDate, "");

                } else if (detailType == PaymentMethod.TYPE_BANK) {

                    summaryData.put(CustomDesignerConstants.Chequeno, obj.optString("chequenumber", ""));
                    summaryData.put(CustomDesignerConstants.BankName, obj.optString("bankname", ""));
                    summaryData.put(CustomDesignerConstants.ChequeDate, obj.optString("chequedate", ""));
                    summaryData.put(CustomDesignerConstants.BankDescription, obj.optString("chequedescription", ""));
                    if (StringUtil.isNullOrEmpty(obj.optString("clearancedate", ""))) {
                        summaryData.put(CustomDesignerConstants.Cheque_Payment_Status, "Uncleared");
                        summaryData.put(CustomDesignerConstants.Cheque_Payment_ClearanceDate, "");
                    } else {
                        summaryData.put(CustomDesignerConstants.Cheque_Payment_Status, "Cleared");
                        summaryData.put(CustomDesignerConstants.Cheque_Payment_ClearanceDate, obj.optString("clearancedate", ""));
                    }
                    summaryData.put(CustomDesignerConstants.Card_Reference_Number, "");
                    summaryData.put(CustomDesignerConstants.CardHolderName, "");
                    summaryData.put(CustomDesignerConstants.CardNo, "");
                    summaryData.put(CustomDesignerConstants.Card_Type, "");
                    summaryData.put(CustomDesignerConstants.Card_ExpiryDate, "");
                }
                summaryData.put(CustomDesignerConstants.PaymentMethod, obj.optString("paymentmethod", ""));
                summaryData.put(CustomDesignerConstants.PaymentAccount, obj.optString("paymentaccount", ""));
                summaryData.put(CustomDesignerConstants.Bank_AccountNumber, obj.optString("paymentaccountnumber", ""));
                summaryData.put(CustomDesignerConstants.Bank_AccountCode, obj.optString("paymentaccountcode", ""));


                if (invJSONArr != null) {
                    for (int cnt = 0; cnt < invJSONArr.length(); cnt++) {
                        JSONObject jObj = (JSONObject) invJSONArr.get(cnt);
                        if (cnt == 0) {
                            doctype = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_DocumentType)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_DocumentType));
                            docnumber = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_DocumentNumber)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_DocumentNumber));
                            desc = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_Description)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_Description));
                            originalamountdue = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_OriginalAmountDue)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_OriginalAmountDue));
                            exchagerates = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_ExchangeRate)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_ExchangeRate));
                            amountdue = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_AmountDue)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_AmountDue));
                            originalamount = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_OriginalAmount)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_OriginalAmount));
                            tax = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_TAXNAME)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_TAXNAME,""));
                            invtaxpercent = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_Tax)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_Tax,""));
                            taxamount = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_TaxAmount)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_TaxAmount));
                            enterpayment = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_EnterPayment)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_EnterPayment));
                            enterpaymentwithtax = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_ENTER_PAYMENT_WITH_TAX)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_ENTER_PAYMENT_WITH_TAX));
                            invoicedates = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_InvoiceDate)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_InvoiceDate));
                            invduedates = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_DueDate)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_DueDate));
                            discount = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_Discount)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_Discount));
                            commonEnterPayment = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.Common_EnterPayment)) ? "-" : jObj.getString(CustomDesignerConstants.Common_EnterPayment));
                            commonAppendtext = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance)) ? "-" : jObj.getString(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance));
                            creditAmount = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_CreditAmount)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_CreditAmount));
                            debitAmount = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_DebitAmount)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_DebitAmount));
                            accountCode = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_ACCOUNT_CODE)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_ACCOUNT_CODE));
                            supplierinvoiceno = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.SUPPLIER_INVOICE_NO)) ? "-" : jObj.getString(CustomDesignerConstants.SUPPLIER_INVOICE_NO));
                            GSTExchangeRate = jObj.optString(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, "");
                            documentStatus = jObj.optString(CustomDesignerConstants.DOCUMENTSTATUS, "-");
                            if (countryid == Constants.indian_country_id) {
                                //Tax related fields
                                cgstPercent = jObj.optString(CustomDesignerConstants.CGSTPERCENT,"0"); 
                                cgstAmount = jObj.optString(CustomDesignerConstants.CGSTAMOUNT,"0");  
                                sgstPercent = jObj.optString(CustomDesignerConstants.SGSTPERCENT,"0"); 
                                sgstAmount = jObj.optString(CustomDesignerConstants.SGSTAMOUNT,"0"); 
                                utgstPercent = jObj.optString(CustomDesignerConstants.UTGSTPERCENT,"0"); 
                                utgstAmount = jObj.optString(CustomDesignerConstants.UTGSTAMOUNT,"0");
                                igstPercent = jObj.optString(CustomDesignerConstants.IGSTPERCENT,"0");
                                igstAmount = jObj.optString(CustomDesignerConstants.IGSTAMOUNT,"0");
                                cessPercent = jObj.optString(CustomDesignerConstants.CESSPERCENT,"0");
                                cessAmount = jObj.optString(CustomDesignerConstants.CESSAMOUNT,"0");
                            }
                        } else {
                            doctype = doctype + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_DocumentType)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_DocumentType));
                            docnumber = docnumber + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_DocumentNumber)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_DocumentNumber));
                            desc = desc + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_Description)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_Description));
                            originalamountdue = originalamountdue + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_OriginalAmountDue)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_OriginalAmountDue));
                            exchagerates = exchagerates + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_ExchangeRate)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_ExchangeRate));
                            amountdue = amountdue + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_AmountDue)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_AmountDue));
                            originalamount = originalamount + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_OriginalAmount)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_OriginalAmount));
                            tax = tax + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_TAXNAME)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_TAXNAME,""));
                            invtaxpercent = invtaxpercent + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_Tax)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_Tax,""));
                            taxamount = taxamount + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_TaxAmount)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_TaxAmount));
                            enterpayment = enterpayment + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_EnterPayment)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_EnterPayment));
                            enterpaymentwithtax = enterpaymentwithtax + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_ENTER_PAYMENT_WITH_TAX)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_ENTER_PAYMENT_WITH_TAX));
                            invoicedates = invoicedates + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_InvoiceDate)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_InvoiceDate));
                            invduedates = invduedates + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_DueDate)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_DueDate));
                            discount = discount + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_Discount)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_Discount));
                            commonEnterPayment = commonEnterPayment + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.Common_EnterPayment)) ? "-" : jObj.getString(CustomDesignerConstants.Common_EnterPayment));
                            commonAppendtext = commonAppendtext + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance)) ? "-" : jObj.getString(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance));
                            creditAmount = creditAmount + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_CreditAmount)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_CreditAmount));
                            debitAmount = debitAmount + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_DebitAmount)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_DebitAmount));
                            accountCode = accountCode + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_ACCOUNT_CODE)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_ACCOUNT_CODE));
                            supplierinvoiceno = supplierinvoiceno + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.SUPPLIER_INVOICE_NO)) ? "-" : jObj.getString(CustomDesignerConstants.SUPPLIER_INVOICE_NO));
                            documentStatus = documentStatus + "," + jObj.optString(CustomDesignerConstants.DOCUMENTSTATUS,"-");
                            if (!StringUtil.isNullOrEmpty(GSTExchangeRate)) {
                                GSTExchangeRate = GSTExchangeRate + "," + jObj.optString(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, "");
                            } else {
                                GSTExchangeRate = jObj.optString(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, "");
                            }
                            if (countryid == Constants.indian_country_id) {
                                //Tax related fields
                                cgstPercent = cgstPercent + ", " + jObj.optString(CustomDesignerConstants.CGSTPERCENT,"0");
                                cgstAmount = cgstAmount + ", " + jObj.optString(CustomDesignerConstants.CGSTAMOUNT,"0");
                                sgstPercent = sgstPercent + ", " + jObj.optString(CustomDesignerConstants.SGSTPERCENT,"0");
                                sgstAmount = sgstAmount + ", " + jObj.optString(CustomDesignerConstants.SGSTAMOUNT,"0");
                                utgstPercent = utgstPercent + ", " + jObj.optString(CustomDesignerConstants.UTGSTPERCENT,"0");
                                utgstAmount = utgstAmount + ", " + jObj.optString(CustomDesignerConstants.UTGSTAMOUNT,"0");
                                igstPercent = igstPercent + ", " + jObj.optString(CustomDesignerConstants.IGSTPERCENT,"0");
                                igstAmount = igstAmount + ", " + jObj.optString(CustomDesignerConstants.IGSTAMOUNT,"0");
                                cessPercent = cessPercent + ", " + jObj.optString(CustomDesignerConstants.CESSPERCENT,"0");
                                cessAmount = cessAmount + ", " + jObj.optString(CustomDesignerConstants.CESSAMOUNT,"0");
                            }
                        }
                        totaltaxamount += Double.parseDouble(jObj.getString(CustomDesignerConstants.RPMP_TaxAmount));
                        subtotal += Double.parseDouble(jObj.getString(CustomDesignerConstants.RPMP_EnterPayment));
                    }
                }
                /**
                 * JE details
                 */
                JSONArray jeJarr = new JSONArray();
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("jeno", pc.getJournalEntry().getEntryNumber());
                params.put("companyid", companyid);
                KwlReturnObject jeDetailsIDList = accJournalEntryobj.getJEDetailsID(params);
                Iterator jeditr = jeDetailsIDList.getEntityList().iterator();
                while (jeditr.hasNext()) {
                    String jedetailsid = (String) jeditr.next();
                    JournalEntryDetail jeDetails = (JournalEntryDetail) accountingHandlerDAOobj.getObject("com.krawler.hql.accounting.JournalEntryDetail", jedetailsid).getEntityList().get(0);
                    String jeAccountCode = jeDetails.getAccount().getAccountCode() != null ? jeDetails.getAccount().getAccountCode() : "";
                    String accountNo = jeDetails.getAccount().getAccountno() != null ? jeDetails.getAccount().getAccountno() : "";
                    String accountName = jeDetails.getAccount().getAccountName() != null ? jeDetails.getAccount().getAccountName() : "";
                    String desciption = jeDetails.getDescription() != null ? jeDetails.getDescription().replaceAll("\n", "<br>") : "";
                    int srno = jeDetails.getSrno();
                    double jeCreditAmount = 0.0;
                    double jeDebitAmount = 0.0;
                    if(jeDetails.isDebit()){
                        jeDebitAmount = jeDetails.getAmountinbase();
                    } else{
                        jeCreditAmount = jeDetails.getAmountinbase();
                    }
                    JSONObject jeJobj = new JSONObject();
                    jeJobj.put(CustomDesignerConstants.JE_ACCOUNT_CODE, jeAccountCode);
                    jeJobj.put(CustomDesignerConstants.JE_ACCOUNT_NO, accountNo);
                    jeJobj.put(CustomDesignerConstants.JE_ACCOUNT_NAME, accountName);
                    jeJobj.put(CustomDesignerConstants.JE_DESCRIPTION, desciption);
                    jeJobj.put(CustomDesignerConstants.JE_CREDIT_AMOUNT, jeCreditAmount);
                    jeJobj.put(CustomDesignerConstants.JE_DEBIT_AMOUNT, jeDebitAmount);
                    jeJobj.put("srno", srno);
                    jeJarr.put(jeJobj);
                }
                summaryData.put("jedetails", jeJarr);
                jArr.put(invJSONArr);
                /*
                 * All Global Section Custom Field and DImensions
                 */
                HashMap<String, Object> returnvalues = new HashMap<String, Object>();
                HashMap<String, Object> extraparams = new HashMap<String, Object>();
                //DateFormat df = authHandler.getUserDateFormatter(request);//User Date Formatter
                df = new SimpleDateFormat(requestJobj.optString(Constants.userdateformat));
                extraparams.put(Constants.companyid, companyid);
                extraparams.put(Constants.moduleid, Constants.Acc_Make_Payment_ModuleId);
                extraparams.put(Constants.customcolumn, 0);
                extraparams.put(Constants.customfield, 1);
                extraparams.put(CustomDesignerConstants.isCustomfield, "true");
                extraparams.put("billid", paymentId);
                returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
                if (returnvalues.containsKey("returnValue")) {
                    globallevelcustomfields = (String) returnvalues.get("returnValue");
                }
                if (returnvalues.containsKey("summaryData")) {
                    summaryData = (JSONObject) returnvalues.get("summaryData");
                }
                returnvalues.clear();
                //global level dimensionfields
                extraparams.put(Constants.customcolumn, 0);
                extraparams.put(Constants.customfield, 0);
                extraparams.put(CustomDesignerConstants.isCustomfield, "false");
                returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
                if (returnvalues.containsKey("returnValue")) {
                    globalleveldimensions = (String) returnvalues.get("returnValue");
                }
                if (returnvalues.containsKey("summaryData")) {
                    summaryData = (JSONObject) returnvalues.get("summaryData");
                }
                //Details like company details,base currency
                CommonFunctions.getCommonFieldsForAllModulesSummaryData(summaryData, extraparams, accountingHandlerDAOobj);

                summaryData.put("summarydata", true);
                amountpaid = subtotal + totaltaxamount;
                totalamount = amountpaid;
                totalAmountWithBanckChanges = totalamount + bankcharges;
                netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalamount)), currency, countryLanguageId);
                /*
                 * Get amount in indonesian words.
                 */
                String indonesianAmountInWords = "";
                if (countryid == Constants.INDONESIAN_COUNTRY_ID) {
                    KwlReturnObject currencyResult =  accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), Constants.CountryIndonesianCurrencyId);
                    KWLCurrency  indoCurrency= (KWLCurrency) currencyResult.getEntityList().get(0);
                    indonesianAmountInWords = IndonesianNumberToWordsOjb.indonesiaConvert(Double.parseDouble(String.valueOf(totalamount)), indoCurrency);
                }
                summaryData.put(CustomDesignerConstants.Createdby, createdby);
                summaryData.put(CustomDesignerConstants.Updatedby, updatedby);
                summaryData.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, authHandler.formattedAmount(subtotal, companyid));
                summaryData.put(CustomDesignerConstants.CustomDesignTotalTax_fieldTypeId, authHandler.formattedAmount(totaltaxamount, companyid));
                summaryData.put(CustomDesignerConstants.CustomDesignAmountPaid_fieldTypeId, authHandler.formattedAmount(amountpaid, companyid));
                summaryData.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, authHandler.formattedAmount(totalamount, companyid));
                summaryData.put(CustomDesignerConstants.CustomDesignOriginalAmountDueTotal, authHandler.formattedAmount(originalamountduetotal, companyid));  //ERP-19271
                summaryData.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, netinword + " Only");
                summaryData.put(CustomDesignerConstants.CustomDesign_Amount_in_words_Bahasa_Indonesia, indonesianAmountInWords);
                summaryData.put(CustomDesignerConstants.TOTALAMOUNT_WITHBANCKCHARGE, authHandler.formattedAmount(totalAmountWithBanckChanges, companyid));
                
                summaryData.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, config == null ? "" : config.getPdfPostText());
                summaryData.put(CustomDesignerConstants.RPMP_CustomerVendorName, obj.optString("personname", ""));
                summaryData.put(CustomDesignerConstants.CustomerVendor_AccountCode, obj.optString("personcode", ""));
                summaryData.put(CustomDesignerConstants.CustomerVendor_AccCode, obj.optString("accountcode", ""));
                summaryData.put(CustomDesignerConstants.CustomerVendor_AccName, obj.optString("accountname", ""));
                summaryData.put(CustomDesignerConstants.CustomerVendor_Term, pc.getVendor() != null ? (pc.getVendor().getDebitTerm() != null ? pc.getVendor().getDebitTerm().getTermdays() : "") : "");
                summaryData.put(CustomDesignerConstants.CustomerVendor_Total_Address, obj.optString("personaddress", ""));
                summaryData.put(CustomDesignerConstants.CustomerVendor_Address_PostalCode, obj.optString("personaddresswithPostalcode", ""));

                //String userId = sessionHandlerImpl.getUserid(request);
                String userId = requestJobj.optString(Constants.useridKey);
                KwlReturnObject userresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
                User user = (User) userresult.getEntityList().get(0);
                summaryData.put(CustomDesignerConstants.CurrentUserFirstName, !StringUtil.isNullOrEmpty(user.getFirstName()) ? user.getFirstName() : "");
                summaryData.put(CustomDesignerConstants.CurrentUserLastName, !StringUtil.isNullOrEmpty(user.getLastName()) ? user.getLastName() : "");
                summaryData.put(CustomDesignerConstants.CurrentUserFullName, !StringUtil.isNullOrEmpty(user.getFullName()) ? user.getFullName() : "");
                summaryData.put(CustomDesignerConstants.CurrentUserEmail, !StringUtil.isNullOrEmpty(user.getEmailID()) ? user.getEmailID() : "");
                summaryData.put(CustomDesignerConstants.CurrentUserAddress, !StringUtil.isNullOrEmpty(user.getAddress()) ? user.getAddress().replaceAll(Constants.REGEX_LINE_BREAK, "<br>") : "");
                summaryData.put(CustomDesignerConstants.CurrentUserContactNumber, !StringUtil.isNullOrEmpty(user.getContactNumber()) ? user.getContactNumber() : "");

                /**
                 * put approve level fields (approver name and approved date)
                 */
                //DateFormat dateFormatter = authHandler.getUserDateFormatter(request);
                  DateFormat dateFormatter = new SimpleDateFormat(requestJobj.optString(Constants.userdateformat));
                extraparams.put(Constants.companyid, companyid);
                extraparams.put(Constants.moduleid, Constants.Acc_Make_Payment_ModuleId);
                extraparams.put("billid", pc.getID());
                extraparams.put("approvestatuslevel", pc.getApprovestatuslevel());
                extraparams.put("dateformatter", dateFormatter);
                CommonFunctions.getCommonFieldsForAllModulesSummaryData(summaryData, extraparams, accountingHandlerDAOobj);
                
                if (paymentwindowtype == Constants.Make_Payment_to_Vendor) {
                    HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                    addrRequestParams.put("vendorid", pc.getVendor().getID());
                    addrRequestParams.put("companyid", companyid);
                    KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addrRequestParams);
                    List<AddressDetails> addressResultList = addressResult.getEntityList();
                    CommonFunctions.getAddressSummaryData(addressResultList, summaryData, companyAccountPreferences, extraCompanyPreferences);
                    addrRequestParams.put("isBillingAddress", true);
                    addrRequestParams.put("isDefaultAddress", true);
                    addrRequestParams.put("isSeparator", true);
                    billAddr = accountingHandlerDAOobj.getVendorAddress(addrRequestParams);
                    addrRequestParams.put("isBillingAddress", false);
                    shipAddr = accountingHandlerDAOobj.getVendorAddress(addrRequestParams);
                } else if (paymentwindowtype == Constants.Make_Payment_to_Customer){
                    /**
                     * Get Customer and Company address details when Make Payment to Customer. ERP-38139
                     */
                    HashMap<String, Object> addrRequestParams = new HashMap();
                    addrRequestParams.put("customerid", pc.getCustomer());
                    addrRequestParams.put("companyid", companyid);
                    KwlReturnObject addressResult = accountingHandlerDAOobj.getCustomerAddressDetails(addrRequestParams);
                    List<AddressDetails> addressResultList = addressResult.getEntityList();
                    CommonFunctions.getAddressSummaryData(addressResultList, summaryData, companyAccountPreferences, extraCompanyPreferences);
                    addrRequestParams.put("isBillingAddress", true);
                    addrRequestParams.put("isDefaultAddress", true);
                    addrRequestParams.put("isSeparator", true);
                    billAddr = accountingHandlerDAOobj.getCustomerAddress(addrRequestParams);
                    addrRequestParams.put("isBillingAddress", false);
                    shipAddr = accountingHandlerDAOobj.getCustomerAddress(addrRequestParams);
                } else if(paymentwindowtype == Constants.Make_Payment_against_GL_Code){
                    /**
                     * Get Company address details when Make Payment to GL. ERP-38139
                     */
                    HashMap<String, Object> addrRequestParams = new HashMap();
                    addrRequestParams.put("companyid", companyid);
                    addrRequestParams.put("isDefaultAddress", true);
                    KwlReturnObject addressResult = accountingHandlerDAOobj.getCompanyAddressDetails(addrRequestParams);
                    List<AddressDetails> addressResultList = addressResult.getEntityList();
                    CommonFunctions.getAddressSummaryData(addressResultList, summaryData, companyAccountPreferences, extraCompanyPreferences);
                } else {
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCompAccPrefBillAddress_fieldTypeId, AccountingAddressManager.getCompanyDefaultBillingAddress(companyid, accountingHandlerDAOobj));
                    summaryData.put(CustomDesignerConstants.CustomDesignCompAccPrefShipAddress_fieldTypeId, AccountingAddressManager.getCompanyDefaultShippingAddress(companyid, accountingHandlerDAOobj));
                    summaryData.put(CustomDesignerConstants.RemitPaymentTo, !StringUtil.isNullOrEmpty(extraCompanyPreferences.getRemitpaymentto()) ? extraCompanyPreferences.getRemitpaymentto().replaceAll("<br>", "!##") : "");
                }
                String customcurrencysymbol = accCommonTablesDAO.getCustomCurrencySymbol(currencysymbol, companyid);//Take custom currency symbol
                summaryData.put(CustomDesignerConstants.SrNo, 1);
                summaryData.put(CustomDesignerConstants.CustomDesignPORefNumber_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignVendorQuoteRefNumber_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignGRORefNumber_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCurrencySymbol_fieldTypeId, customcurrencysymbol);
                
                //GST Exchange Rate
                if (countryid == Integer.parseInt(Constants.SINGAPOREID) && !companyAccountPreferences.getCompany().getCurrency().getCurrencyID().equalsIgnoreCase(Constants.SGDID)) {
                    summaryData.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, !StringUtil.isNullOrEmpty(GSTExchangeRate) ? GSTExchangeRate : "");
                } else {
                    summaryData.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, "");
                }

                //Invoice comma separated values-Against Customer Invoice
                summaryData.put(CustomDesignerConstants.RPMP_DocumentType, doctype);
                summaryData.put(CustomDesignerConstants.RPMP_DocumentNumber, docnumber);
                summaryData.put(CustomDesignerConstants.RPMP_Description, desc);
                summaryData.put(CustomDesignerConstants.RPMP_OriginalAmountDue, originalamountdue);
                summaryData.put(CustomDesignerConstants.RPMP_ExchangeRate, exchagerates);
                summaryData.put(CustomDesignerConstants.RPMP_AmountDue, amountdue);
                summaryData.put(CustomDesignerConstants.RPMP_OriginalAmount, originalamount);
                summaryData.put(CustomDesignerConstants.RPMP_TAXNAME, tax);
                summaryData.put(CustomDesignerConstants.RPMP_Tax,invtaxpercent );
                summaryData.put(CustomDesignerConstants.RPMP_TaxAmount, taxamount);
                summaryData.put(CustomDesignerConstants.RPMP_EnterPayment, enterpayment);
                summaryData.put(CustomDesignerConstants.RPMP_ENTER_PAYMENT_WITH_TAX, enterpaymentwithtax);
                summaryData.put(CustomDesignerConstants.RPMP_CustomerVendorInvoiceNo, invoiceNos);
                summaryData.put(CustomDesignerConstants.RPMP_InvoiceDate, invoicedates);
                summaryData.put(CustomDesignerConstants.RPMP_DueDate, invduedates);
                summaryData.put(CustomDesignerConstants.Common_EnterPayment, commonEnterPayment);
                summaryData.put(CustomDesignerConstants.RPMP_Discount, discount);
                summaryData.put(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance, commonAppendtext);
                summaryData.put(CustomDesignerConstants.BillTo, billAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
                summaryData.put(CustomDesignerConstants.ShipTo, shipAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
                summaryData.put(CustomDesignerConstants.AllGloballevelCustomfields, globallevelcustomfields);
                summaryData.put(CustomDesignerConstants.AllGloballevelDimensions, globalleveldimensions);
                summaryData.put(CustomDesignerConstants.AllDimensions, "");
                summaryData.put(CustomDesignerConstants.AllLinelevelCustomFields, "");
                summaryData.put(CustomDesignerConstants.RPMP_CreditAmount, creditAmount);
                summaryData.put(CustomDesignerConstants.RPMP_DebitAmount, debitAmount);
                summaryData.put(CustomDesignerConstants.RPMP_ACCOUNT_CODE, accountCode);
                summaryData.put(CustomDesignerConstants.CUSTOMER_OR_VENDOR_TITLE, customerOrVendorTitle);
                summaryData.put(CustomDesignerConstants.CURRENCY_EXCHANGE_RATE, exchangerate);
                summaryData.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, exchangerate);
                summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO,VATTInnumber);
                summaryData.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO, CSTTInNumber);
                summaryData.put(CustomDesignerConstants.Company_VAT_TIN_NO, extraCompanyPreferences.getVatNumber()!= null ? extraCompanyPreferences.getVatNumber() : "");
                summaryData.put(CustomDesignerConstants.Company_CST_TIN_NO, extraCompanyPreferences.getCstNumber()!= null ? extraCompanyPreferences.getCstNumber() : "");
                summaryData.put(CustomDesignerConstants.SUPPLIER_INVOICE_NO, supplierinvoiceno);
                summaryData.put(CustomDesignerConstants.RPMP_InvoiceTotalDiscount, invoiceTotalDiscount);
                summaryData.put(CustomDesignerConstants.CUSTOMER_VENDOR_GSTIN_NUMBER, gstin);
                //Put tax related fields in summaryData for printing as global fields
                summaryData.put(CustomDesignerConstants.CGSTPERCENT, cgstPercent);
                summaryData.put(CustomDesignerConstants.CGSTAMOUNT, cgstAmount);
                summaryData.put(CustomDesignerConstants.SGSTPERCENT, sgstPercent);
                summaryData.put(CustomDesignerConstants.SGSTAMOUNT, sgstAmount);
                summaryData.put(CustomDesignerConstants.UTGSTPERCENT, utgstPercent);
                summaryData.put(CustomDesignerConstants.UTGSTAMOUNT, utgstAmount);
                summaryData.put(CustomDesignerConstants.IGSTPERCENT, igstPercent);
                summaryData.put(CustomDesignerConstants.IGSTAMOUNT, igstAmount);
                summaryData.put(CustomDesignerConstants.CESSPERCENT, cessPercent);
                summaryData.put(CustomDesignerConstants.CESSAMOUNT, cessAmount);
                summaryData.put(CustomDesignerConstants.DOCUMENTSTATUS, documentStatus);
                summaryData.put(CustomDesignerConstants.TDS_RATE, 0);
                summaryData.put(CustomDesignerConstants.TDS_AMOUNT, 0);
                
                if (pc.getAdvanceDetails() != null) {
                    for (AdvanceDetail adv : pc.getAdvanceDetails()) {
                        summaryData.put(CustomDesignerConstants.TDS_AMOUNT, adv.getTdsamount());
                    }
                }
                
                jArr.put(summaryData);
            }

            //getting all the custom fields at line level
            result = customDesignDAOObj.getCustomLineFields(companyid, moduleid);
            list = result.getEntityList();
            for (int cnt = 0; cnt < list.size(); cnt++) {
                JSONObject obj = new JSONObject();
                HashMap<String, String> map = new HashMap<String, String>();
                Object[] rowcustom = (Object[]) list.get(cnt);
                map.put("Custom_" + rowcustom[2], "{label:'" + rowcustom[2] + "',xtype:'" + rowcustom[1].toString() + "'}");
                dbCustomJSONArr.put(map);
            }
            jArr.put(dbCustomJSONArr);
        } catch (Exception ex) {
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }

        return jArr;
    }
    
    @Override
    public JSONArray getMPDetailsItemJSON(JSONObject requestJobj, String companyid, String SOID,HashMap<String, Integer> FieldMap, HashMap<String, String> replaceFieldMap) throws SessionExpiredException, ServiceException, JSONException {
     
         Payment pc = null;
         JSONArray jArr = new JSONArray();
         JSONArray dbCustomJSONArr = new JSONArray();
         
         List list = null;
         KwlReturnObject result;
         PdfTemplateConfig config = null;
         double total = 0;
         Date journalEntryDate = new Date();//mode!=StaticValues.AUTONUM_BILLINGRECEIPT?rc.getJournalEntry().getEntryDate():rc1.getJournalEntry().getEntryDate()
         PayDetail PayDetail = null;
         PaymentDetail row = null;
         String customergoodsreceiptno = "", invoicetax = "", paymentmethod = "", paymentaccount = "", bankchequeno = "", chequebankname = "", invdesc = "", gridtaxname = "", gridaccountdescription = "";
         boolean ismanycrdb = false;
         int receiptType = 0, count=1,rowcnt = 0;

         BankReconciliation bankreconciliation = null;
         JSONObject summaryData = new JSONObject();
         JSONArray accJSONArr = new JSONArray();
         JSONArray invJSONArr = new JSONArray();
         JSONArray debitJSONArr = new JSONArray();
         JSONArray CustJSONArr = new JSONArray();
         double advanceAmount = 0;
         double invoicediscount = 0,amountdues = 0, enterpayment = 0;

         java.util.Date entryDate = null, grduedate = null, grdate = null, chequecleardate = null, expirydate = null;
         String invexchangerate = "",mainvendorname="";//mode!=StaticValues.AUTONUM_BILLINGRECEIPT?rc.getJournalEntry().getEntryDate():rc1.getJournalEntry().getEntryDate()
         DateFormat df = authHandler.getDateOnlyFormat();
         SimpleDateFormat sdf = new SimpleDateFormat(CustomDesignerConstants.DateFormat_RemovingTime);
         String receiptNumber = "",invoiceNos = "", invoicedates = "", invduedates = "", invtax = "", invdiscount = "";
         String invoriginalamount = "", inventerpayment = "", invexchagerates = "", invoriginalamountdue = "", invamountdue = "";

         //3rd option Debit Note
         String dnnumber = "", dnamount = "", dnamountdue = "", dnenterpayment = "";

         //4rth option grid values-GL Code
         String gridtype = "", gridaccname = "", gridaccamount = "", gridaccdesc = "", gridacctax = "", gridaccwithtax = "",gridaccountcode="",gridacctaxamount="";
         //Card Holder Details
         String cardno = "", cardholder = "", cardreferencenumber = "", cardtype = "";
         
         //Common appendtext
         String commonAppendtext="";
         String commonEnterPayment="";
         
         double amount = 0,totalamount=0,taxtotalgst=0,subtotal=0;
         String accname = "", address = "", netinword = "",POref = "",GROref = "",VQouteRef = "";    

         HashMap<String, Object> rRequestParams = new HashMap<String, Object>();
         HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(requestJobj);
         ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
         KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), SOID);
         pc = (Payment) objItr.getEntityList().get(0);
         
         int countryLanguageId = Constants.OtherCountryLanguageId; // 0
         KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
         ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
         if (extraCompanyPreferences.isAmountInIndianWord()) {
             countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
         }
         
         KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), requestJobj.optString(Constants.globalCurrencyKey,""));
         KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
         String currencyid = (pc.getCurrency() == null) ? currency.getCurrencyID() : pc.getCurrency().getCurrencyID();
         
         //Document Currency
         summaryData.put(CustomDesignerConstants.DOCUMENT_CURRENCY_ID, currencyid);
         
         double jeExternalCurrencyRate = pc.getJournalEntry().getExternalCurrencyRate();
         double revExchangeRate = 1.0;
         if (jeExternalCurrencyRate != 0.0) {
             revExchangeRate = 1 / jeExternalCurrencyRate;
         }
         //get Company PostText
         KwlReturnObject templateConfig = accCommonTablesDAO.getPDFTemplateRow(companyid, requestJobj.optInt("moduleid",0));
         if (templateConfig.getEntityList().size() > 0) {
             config = (PdfTemplateConfig) templateConfig.getEntityList().get(0);
         }
         int moduleid = requestJobj.optInt("moduleid",0);
         
         receiptNumber = pc.getPaymentNumber();
         paymentmethod = pc.getPayDetail().getPaymentMethod().getMethodName();
         paymentaccount = pc.getPayDetail() != null ? pc.getPayDetail().getPaymentMethod().getAccount().getName() : "";
         receiptType = pc.getReceipttype();
         totalamount=pc.getDepositAmount();
         
         //get the custom field of lineitem                                        
         HashMap<String, Object> fieldrequestParams = new HashMap();
         HashMap<String, String> customFieldMap = new HashMap<String, String>();
         HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
         fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
         fieldrequestParams.put(Constants.filter_values, Arrays.asList(pc.getCompany().getCompanyID(), Constants.Acc_Make_Payment_ModuleId));
         replaceFieldMap = new HashMap<String, String>();
         FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

         if (receiptType == 1) {//against vendor invoice
             mainvendorname=requestJobj.optString("accname","");
             
             filter_names.add("payment.ID");
             filter_params.add(pc.getID());
             rRequestParams.put("filter_names", filter_names);
             rRequestParams.put("filter_params", filter_params);
             Iterator itr = pc.getRows().iterator();

             while (itr.hasNext()) {
                 row = (PaymentDetail) itr.next();
                 JSONObject obj = new JSONObject();
                 double invoiceReturnedAmt = 0d;
                 double originalamount = 0d;
                 double externalCurrencyRate = 0d, exchangeratefortransaction = 0d, amountpaidincurrency = 0d, amountduenonnegative = 0d;
                 Date invoiceCreationDate = null;
                 String currencyidtransaction = "", currencysymbol = "", currencysymboltransaction = "";
                 boolean isVendorPaymentEdit = true;
                 POref = "";
                 GROref = "";
                 VQouteRef = "";
                 
                 customergoodsreceiptno = row.getGoodsReceipt().getGoodsReceiptNumber();
                 grduedate = row.getGoodsReceipt().getDueDate();
                 invoicetax = row.getGoodsReceipt().getTax() != null ? row.getGoodsReceipt().getTax().getName() : "0%";
                  if(row.getGoodsReceipt().isNormalInvoice()){
                        grdate = row.getGoodsReceipt().getJournalEntry().getEntryDate();
                        originalamount = isVendorPaymentEdit?row.getGoodsReceipt().getVendorEntry().getAmount():row.getAmount();//Original Amount of invoice grid
                    }else{// for opening balance inoices
                        grdate = row.getGoodsReceipt().getCreationDate();
                        originalamount = isVendorPaymentEdit?row.getGoodsReceipt().getOriginalOpeningBalanceAmount():row.getAmount();//Original Amount of invoice grid
                    }

                 Discount disc = row.getGoodsReceipt().getDiscount();
                 if (disc != null) {
                     invoiceReturnedAmt = disc.getDiscountValue();
                 }
                
                 if (row.getGoodsReceipt() != null) {//getting currency name for calculating external currencyrate
                     obj.put("currencyidtransaction", row.getGoodsReceipt().getCurrency() == null ? (row.getPayment().getCurrency() == null ? currency.getCurrencyID() : row.getPayment().getCurrency().getCurrencyID()) : row.getGoodsReceipt().getCurrency().getCurrencyID());
                     obj.put("currencysymbol", row.getGoodsReceipt().getCurrency() == null ? (row.getPayment().getCurrency() == null ? currency.getSymbol() : row.getPayment().getCurrency().getSymbol()) : row.getGoodsReceipt().getCurrency().getSymbol());
                     obj.put("currencysymboltransaction", row.getGoodsReceipt().getCurrency() == null ? (row.getPayment().getCurrency() == null ? currency.getSymbol() : row.getPayment().getCurrency().getSymbol()) : row.getGoodsReceipt().getCurrency().getSymbol());
                     currencyidtransaction = row.getGoodsReceipt().getCurrency() == null ? (row.getPayment().getCurrency() == null ? currency.getCurrencyID() : row.getPayment().getCurrency().getCurrencyID()) : row.getGoodsReceipt().getCurrency().getCurrencyID();
                     currencysymbol = row.getGoodsReceipt().getCurrency() == null ? (row.getPayment().getCurrency() == null ? currency.getSymbol() : row.getPayment().getCurrency().getSymbol()) : row.getGoodsReceipt().getCurrency().getSymbol();
                     currencysymboltransaction = row.getGoodsReceipt().getCurrency() == null ? (row.getPayment().getCurrency() == null ? currency.getSymbol() : row.getPayment().getCurrency().getSymbol()) : row.getGoodsReceipt().getCurrency().getSymbol();
                 } else {
                     obj.put("currencyidtransaction", (row.getPayment().getCurrency() == null ? currency.getCurrencyID() : row.getPayment().getCurrency().getCurrencyID()));
                     obj.put("currencysymbol", (row.getPayment().getCurrency() == null ? currency.getSymbol() : row.getPayment().getCurrency().getSymbol()));
                     obj.put("currencysymboltransaction", (row.getPayment().getCurrency() == null ? currency.getSymbol() : row.getPayment().getCurrency().getSymbol()));
                     currencyidtransaction = (row.getPayment().getCurrency() == null ? currency.getCurrencyID() : row.getPayment().getCurrency().getCurrencyID());
                     currencysymbol = (row.getPayment().getCurrency() == null ? currency.getSymbol() : row.getPayment().getCurrency().getSymbol());
                     currencysymboltransaction = (row.getPayment().getCurrency() == null ? currency.getSymbol() : row.getPayment().getCurrency().getSymbol());
                 }

                 if (row.getGoodsReceipt().isNormalInvoice()) {//ceration date.original amount of grid & external currency rate
                     invoiceCreationDate = row.getGoodsReceipt().getJournalEntry().getEntryDate();
                     originalamount = isVendorPaymentEdit ? row.getGoodsReceipt().getVendorEntry().getAmount() : row.getAmount();//Original amount of grid
                     externalCurrencyRate = row.getGoodsReceipt().getJournalEntry().getExternalCurrencyRate();
                 } else {// opening balance invoice creation date
                     invoiceCreationDate = row.getGoodsReceipt().getCreationDate();
                     originalamount = isVendorPaymentEdit ? row.getGoodsReceipt().getOriginalOpeningBalanceAmount() : row.getAmount();
                     externalCurrencyRate = row.getGoodsReceipt().getExchangeRateForOpeningTransaction();
                 }

                 double rowAmount = (authHandler.round(row.getAmount(), companyid));
                 if (isVendorPaymentEdit) {
                     obj.put("amountpaid", rowAmount);
                     if (row.getFromCurrency() != null && row.getToCurrency() != null) {
                         obj.put("exchangeratefortransaction", row.getExchangeRateForTransaction());
                         obj.put("amountpaidincurrency", authHandler.round(rowAmount / row.getExchangeRateForTransaction(), companyid));
                         exchangeratefortransaction = row.getExchangeRateForTransaction();
                         amountpaidincurrency =authHandler.round(rowAmount / row.getExchangeRateForTransaction(), companyid);
                         
                     } else {
                         amount = rowAmount;
                         String fromcurrencyid = (row.getPayment().getCurrency() == null ? currency.getCurrencyID() : row.getPayment().getCurrency().getCurrencyID());
                         String tocurrencyid = (row.getGoodsReceipt().getCurrency() == null ? (row.getPayment().getCurrency() == null ? currency.getCurrencyID() : row.getPayment().getCurrency().getCurrencyID()) : row.getGoodsReceipt().getCurrency().getCurrencyID());
                         double exchangeRate = row.getGoodsReceipt().isIsOpeningBalenceInvoice() ? row.getGoodsReceipt().getExternalCurrencyRate() : row.getGoodsReceipt().getJournalEntry().getExternalCurrencyRate();
                         Date tranDate = row.getGoodsReceipt().isIsOpeningBalenceInvoice() ? row.getGoodsReceipt().getCreationDate() : row.getGoodsReceipt().getJournalEntry().getEntryDate();
                         KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherRoundOff(requestParams, amount, fromcurrencyid, tocurrencyid, tranDate, exchangeRate);
                         amount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                         obj.put("exchangeratefortransaction", amount / rowAmount);
                         obj.put("amountpaidincurrency", amount);
                         exchangeratefortransaction = amount / row.getAmount();
                         amountpaidincurrency = amount;
                     }
                 } else {
                     if (row.getFromCurrency() != null && row.getToCurrency() != null) {
                         obj.put("amountpaid", rowAmount / row.getExchangeRateForTransaction());
                     } else {
                         amount = rowAmount;
                         String fromcurrencyid = (row.getPayment().getCurrency() == null ? currency.getCurrencyID() : row.getPayment().getCurrency().getCurrencyID());
                         String tocurrencyid = (row.getGoodsReceipt().getCurrency() == null ? (row.getPayment().getCurrency() == null ? currency.getCurrencyID() : row.getPayment().getCurrency().getCurrencyID()) : row.getGoodsReceipt().getCurrency().getCurrencyID());
                         double exchangeRate = row.getGoodsReceipt().isIsOpeningBalenceInvoice() ? row.getGoodsReceipt().getExternalCurrencyRate() : row.getGoodsReceipt().getJournalEntry().getExternalCurrencyRate();
                         Date tranDate = row.getGoodsReceipt().isIsOpeningBalenceInvoice() ? row.getGoodsReceipt().getCreationDate() : row.getGoodsReceipt().getJournalEntry().getEntryDate();
                         KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherRoundOff(requestParams, amount, fromcurrencyid, tocurrencyid, tranDate, exchangeRate);
                         amount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                         obj.put("amountpaid", amount);
                     }
                 }

                 double amountdue = 0.0, totalAmount = 0.0, amountDueOriginal = 0.0;
                 if (row.getGoodsReceipt().isNormalInvoice()) {
                     List ll;
                     if (row.getGoodsReceipt().isIsExpenseType()) {
                         ll = accGoodsReceiptCMN.getExpGRAmountDue(requestParams, row.getGoodsReceipt());
                         amountDueOriginal = (ll.isEmpty() ? 0 : (Double) ll.get(4));
                     } else {
                         if (Constants.InvoiceAmountDueFlag) {
                             ll = accGoodsReceiptCMN.getInvoiceDiscountAmountInfo(requestParams, row.getGoodsReceipt());
                             amountDueOriginal = (ll.isEmpty() ? 0 : (Double) ll.get(5));
                         } else {
                             ll = accGoodsReceiptCMN.getGRAmountDue(requestParams, row.getGoodsReceipt());
                             amountDueOriginal = (ll.isEmpty() ? 0 : (Double) ll.get(5));
                         }
                     }
                     amountdue = (ll.isEmpty() ? 0 : (Double) ll.get(1));
                     totalAmount = row.getGoodsReceipt().getVendorEntry().getAmount();
                 } else {
                     amountdue = row.getGoodsReceipt().getOpeningBalanceAmountDue() * (row.getExchangeRateForTransaction() == 0 ? 1 : row.getExchangeRateForTransaction());
                     amountDueOriginal = row.getGoodsReceipt().getOpeningBalanceAmountDue();
                     totalAmount = row.getGoodsReceipt().getOriginalOpeningBalanceAmount();
                 }

                 amountdue = authHandler.round(amountdue, companyid);
                 amountDueOriginal = authHandler.round(amountDueOriginal, companyid);
                 totalAmount = authHandler.round(totalAmount, companyid);

                 if (row.getFromCurrency() != null && row.getToCurrency() != null) {
                     obj.put("amountduenonnegative", (isVendorPaymentEdit ? (authHandler.round(amountDueOriginal * row.getExchangeRateForTransaction(), companyid) + rowAmount) : amountDueOriginal));
                     amountduenonnegative = (isVendorPaymentEdit ? (authHandler.round(amountDueOriginal * row.getExchangeRateForTransaction(), companyid) + rowAmount) : amountDueOriginal);
                 } else {
                     obj.put("amountduenonnegative", (isVendorPaymentEdit ? amountdue + obj.optDouble("amountpaid", 0) : amountdue));
                     amountduenonnegative = (isVendorPaymentEdit ? amountdue + obj.optDouble("amountpaid", 0) : amountdue);
                 }
                 obj.put("amountDueOriginal", (isVendorPaymentEdit ? amountDueOriginal + obj.optDouble("amountpaidincurrency", 0) : amountDueOriginal));
                 amountDueOriginal=(isVendorPaymentEdit ? amountDueOriginal + obj.optDouble("amountpaidincurrency", 0) : amountDueOriginal);
                 obj.put("amountDueOriginalSaved", (isVendorPaymentEdit ? amountDueOriginal + obj.optDouble("amountpaidincurrency", 0) : amountDueOriginal));
                 obj.put("totalamount", totalAmount);

                 invexchangerate = "1 " + currencysymboltransaction + " = " + exchangeratefortransaction + " " + pc.getCurrency().getSymbol();

                 // ## Get Custom Field Data 
                 Map<String, Object> variableMap = new HashMap<String, Object>();
                 HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                 ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                 Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                 Detailfilter_params.add(row.getID());
                 invDetailRequestParams.put("filter_names", Detailfilter_names);
                 invDetailRequestParams.put("filter_params", Detailfilter_params);
                 KwlReturnObject idcustresult = accVendorPaymentobj.getVendorPaymentCustomData(invDetailRequestParams);
                 if (idcustresult.getEntityList().size() > 0) {
                     AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                     AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                     Map<String, Object> resultMap = accMainAccountingService.getCustomFieldsForExport(customFieldMap, variableMap,customDateFieldMap);
                     for (Map.Entry<String, Object> varEntry : resultMap.entrySet()) {
                         String coldata = (varEntry.getValue() != null) ? (!varEntry.getValue().toString().equals("null") ? varEntry.getValue().toString() : "") : "";
                                 obj.put(varEntry.getKey(), coldata);
                             summaryData.put(varEntry.getKey(), coldata);
                         }
                     }

                 for (Map.Entry<String, Integer> field : FieldMap.entrySet()) {//checks when field has any value
                     if (!obj.has(field.getKey())) {
                         obj.put(field.getKey(), "");
                         summaryData.put(field.getKey(), "");
                     }
                 }

                 //to calculate linking information in Vendor Invoice
                 order_by.add("srno");
                 order_type.add("asc");
                 rRequestParams.put("order_by", order_by);
                 rRequestParams.put("order_type", order_type);
                 filter_params.clear();
                 filter_names.clear();
                 filter_names.add("goodsReceipt.ID");  //goodsreceipt is the database name
                 filter_params.add(row.getGoodsReceipt().getID());
                 KwlReturnObject idresult = accGoodsReceiptobj.getGoodsReceiptDetails(rRequestParams);
                  Iterator goodsreceiptitr = idresult.getEntityList().iterator();
                 GoodsReceiptDetail grprow = null;
                 boolean vqouteRef = false;
                 boolean grOref = false;
                 boolean poref = false;
                 while (goodsreceiptitr.hasNext()) {
                     rowcnt++;
                     grprow = (GoodsReceiptDetail) goodsreceiptitr.next();
                     if (grprow.getGoodsReceiptOrderDetails() != null) {
                         if (GROref.indexOf(grprow.getGoodsReceiptOrderDetails().getGrOrder().getGoodsReceiptOrderNumber()) == -1) {
                             GROref += grprow.getGoodsReceiptOrderDetails().getGrOrder().getGoodsReceiptOrderNumber() + ",";
                             grOref=true;
                         }

                     } else if (grprow.getPurchaseorderdetail() != null) {
                         if (POref.indexOf(grprow.getPurchaseorderdetail().getPurchaseOrder().getPurchaseOrderNumber()) == -1) {
                             POref += grprow.getPurchaseorderdetail().getPurchaseOrder().getPurchaseOrderNumber() + ",";
                             poref=true;
                             
                     }
                     } else if (grprow.getVendorQuotationDetail() != null) {
                         if (VQouteRef.indexOf(grprow.getVendorQuotationDetail().getVendorquotation().getQuotationNumber()) == -1) {
                             VQouteRef += grprow.getVendorQuotationDetail().getVendorquotation().getQuotationNumber() + ",";
                             vqouteRef=true;
                     }
                 }
                 } 
                 
                 if (grOref) {//removing comma
                     GROref = GROref.substring(0, GROref.length() - 1);

                 } else if (poref) {
                     POref = POref.substring(0, POref.length() - 1);
                 } else if (vqouteRef) {
                     VQouteRef = VQouteRef.substring(0, VQouteRef.length() - 1);
                 }
                 commonAppendtext="Invoice # "+customergoodsreceiptno+" dated " + sdf.format(grdate);
                 
                 obj.put(CustomDesignerConstants.CustomDesignPORefNumber_fieldTypeId, POref);
                 obj.put(CustomDesignerConstants.CustomDesignVendorQuoteRefNumber_fieldTypeId, VQouteRef);
                 obj.put(CustomDesignerConstants.CustomDesignGRORefNumber_fieldTypeId, GROref);   
                 
                 obj.put(CustomDesignerConstants.RPMP_CustomerVendorInvoiceNo, customergoodsreceiptno);
                 obj.put(CustomDesignerConstants.RPMP_InvoiceDate, sdf.format(grdate));
                 obj.put(CustomDesignerConstants.RPMP_DueDate, sdf.format(grduedate));
                 obj.put(CustomDesignerConstants.RPMP_Tax, invoicetax);
                 obj.put(CustomDesignerConstants.RPMP_Discount,  authHandler.formattedCommaSeparatedAmount((disc != null ? disc.getDiscountValue() : invoicediscount), companyid));
                 obj.put(CustomDesignerConstants.RPMP_OriginalAmount,  authHandler.formattedCommaSeparatedAmount(originalamount, companyid));
                 obj.put(CustomDesignerConstants.Invoice_Original_Amount_Due,authHandler.formattedCommaSeparatedAmount( amountDueOriginal, companyid));
                 obj.put(CustomDesignerConstants.Invoice_Exchange_Rate, invexchangerate);
                 obj.put(CustomDesignerConstants.RPMP_AmountDue, authHandler.formattedCommaSeparatedAmount(amountduenonnegative, companyid));
                 obj.put(CustomDesignerConstants.RPMP_EnterPayment, authHandler.formattedCommaSeparatedAmount(row.getAmount(), companyid));
                 obj.put(CustomDesignerConstants.RPMP_CustomerVendorName, "");
                 obj.put(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance,commonAppendtext);
                 obj.put(CustomDesignerConstants.Common_EnterPayment, authHandler.formattedCommaSeparatedAmount(row.getAmount(), companyid));
                 obj.put(CustomDesignerConstants.SrNo,count);
                 invJSONArr.put(obj);
                 count++;
             }


         }else if (receiptType == 6) {//make payment against Customer

             accname = requestJobj.optString("accname","");
             address = requestJobj.optString("address","");
             totalamount = pc.getDepositAmount();
             authHandlerDAOObj.getFormattedCurrency(amount, currencyid, companyid);
             summaryData.put(CustomDesignerConstants.RPMP_CustomerVendorName, accname);
             JSONObject obj = new JSONObject();
             obj.put(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance, "Advance Payment");
             obj.put(CustomDesignerConstants.Common_EnterPayment, totalamount);
             CustJSONArr.put(obj);
             
             for (Map.Entry<String, Integer> field : FieldMap.entrySet()) {//checks when field has any value
                 summaryData.put(field.getKey(), "");
             }

            } else if(receiptType == 7){//Credit Note
             mainvendorname=requestJobj.optString("accname","");
             KwlReturnObject cnjedresult = accVendorPaymentobj.getVendorCnPayment(pc.getID());
             List<CreditNotePaymentDetails> lst = cnjedresult.getEntityList();
             for (CreditNotePaymentDetails cnpd : lst) {
                 JSONObject obj = new JSONObject();                 
                 CreditNote creditnote = cnpd.getCreditnote();
                 String debitnoteno = creditnote.getCreditNoteNumber();
                 Double debitnoteamount = creditnote.getCnamount();
                 Double amountpaid = cnpd.getAmountPaid();
                 Double amountdue = cnpd.getAmountDue();

                 commonAppendtext="Credit Note # "+debitnoteno+" dated "+sdf.format(creditnote.getJournalEntry().getEntryDate());
                 
                 obj.put(CustomDesignerConstants.paymentDebit_note, debitnoteno);
                 obj.put(CustomDesignerConstants.paymentDebit_noteAmount,authHandler.formattedCommaSeparatedAmount(debitnoteamount, companyid));
                 obj.put(CustomDesignerConstants.paymentDebit_note_AmountDue, authHandler.formattedCommaSeparatedAmount(amountdue, companyid));
                 obj.put(CustomDesignerConstants.paymentDebit_note_EnterPayment, authHandler.formattedCommaSeparatedAmount(amountpaid, companyid));
                 obj.put(CustomDesignerConstants.SrNo,count);
                 obj.put(CustomDesignerConstants.CustomDesignPORefNumber_fieldTypeId, POref);
                 obj.put(CustomDesignerConstants.CustomDesignVendorQuoteRefNumber_fieldTypeId, VQouteRef);
                 obj.put(CustomDesignerConstants.CustomDesignGRORefNumber_fieldTypeId, GROref);
                 obj.put(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance, commonAppendtext);
                 obj.put(CustomDesignerConstants.Common_EnterPayment, authHandler.formattedCommaSeparatedAmount(amountpaid, companyid));
              
                 for (Map.Entry<String, Integer> field : FieldMap.entrySet()) {//putting custom field value as blank because credit note doesn't have line item custom field
                     if (!obj.has(field.getKey())) {
                         obj.put(field.getKey(), "");
                         summaryData.put(field.getKey(), "");
                     }
                 }
                 debitJSONArr.put(obj);
                 count++;

             }
          
              }else if(receiptType == 9) {//gl code

                double totaltaxamount = 0;
                rRequestParams.clear();
                filter_names.clear();
                filter_params.clear();
                filter_names.add("payment.ID");
                filter_params.add(pc.getID());
                rRequestParams.put("filter_names", filter_names);
                rRequestParams.put("filter_params", filter_params);
                KwlReturnObject pdoresult = accVendorPaymentobj.getPaymentDetailOtherwise(rRequestParams);
                List<PaymentDetailOtherwise> list1 = pdoresult.getEntityList();

                for (PaymentDetailOtherwise paymentDetailOtherwise : list1) {
                    JSONObject obj = new JSONObject();
                    if (pc.getID().equals(paymentDetailOtherwise.getPayment().getID())) {
                        double taxamount = 0;
                        if (paymentDetailOtherwise.getTax() != null) {
                            taxamount = paymentDetailOtherwise.getTaxamount();
                            taxtotalgst += taxamount;
                        }
                        subtotal+=paymentDetailOtherwise.getAmount();
                        obj.put(CustomDesignerConstants.GridType, "Debit");
                        obj.put(CustomDesignerConstants.GridAccountName, paymentDetailOtherwise.getAccount().getName());
                        obj.put(CustomDesignerConstants.GridAmountinSGD, String.valueOf(authHandlerDAOObj.getFormattedCurrency(paymentDetailOtherwise.getAmount(), currencyid, companyid)));
                        obj.put(CustomDesignerConstants.GridDesc, paymentDetailOtherwise.getDescription() != null ? paymentDetailOtherwise.getDescription() : "");
                        obj.put(CustomDesignerConstants.GridTax, paymentDetailOtherwise.getTax() != null ? paymentDetailOtherwise.getTax().getName() : "");
                        obj.put(CustomDesignerConstants.GridTaxAmount, authHandler.formattedCommaSeparatedAmount(taxamount, companyid));
                        obj.put(CustomDesignerConstants.GridAmountinTax, authHandler.formattedCommaSeparatedAmount((paymentDetailOtherwise.getAmount() + paymentDetailOtherwise.getTaxamount()), companyid));
                        obj.put(CustomDesignerConstants.GridAccountCode,paymentDetailOtherwise.getAccount().getAcccode()!=null?paymentDetailOtherwise.getAccount().getAcccode():"");//Account code
                        obj.put(CustomDesignerConstants.SrNo,count);
                       
                        // ## Get Custom Field Data 
                        Map<String, Object> variableMap = new HashMap<String, Object>();
                        HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                        ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                        Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                        Detailfilter_params.add(paymentDetailOtherwise.getID());
                        invDetailRequestParams.put("filter_names", Detailfilter_names);
                        invDetailRequestParams.put("filter_params", Detailfilter_params);
                        KwlReturnObject idcustresult = accVendorPaymentobj.getVendorPaymentCustomData(invDetailRequestParams);
                        if (idcustresult.getEntityList().size() > 0) {
                            AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                            AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                            Map<String, Object> resultMap = accMainAccountingService.getCustomFieldsForExport(customFieldMap, variableMap,customDateFieldMap);
                            for (Map.Entry<String, Object> varEntry : resultMap.entrySet()) {
                                String coldata = (varEntry.getValue() != null) ? (!varEntry.getValue().toString().equals("null") ? varEntry.getValue().toString() : "") : "";
                                obj.put(varEntry.getKey(), coldata);
                                summaryData.put(varEntry.getKey(), coldata);
                            }
                        }

                        for (Map.Entry<String, Integer> field : FieldMap.entrySet()) {//checks when field has any value
                            if (!obj.has(field.getKey())) {
                                obj.put(field.getKey(), "");
                                summaryData.put(field.getKey(), "");
                            }
                        }
                        
                        commonAppendtext=paymentDetailOtherwise.getAccount().getName();
                        
                        obj.put(CustomDesignerConstants.CustomDesignPORefNumber_fieldTypeId, POref);
                        obj.put(CustomDesignerConstants.CustomDesignVendorQuoteRefNumber_fieldTypeId, VQouteRef);
                        obj.put(CustomDesignerConstants.CustomDesignGRORefNumber_fieldTypeId, GROref);
                        obj.put(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance, commonAppendtext);
                        obj.put(CustomDesignerConstants.Common_EnterPayment, authHandler.formattedCommaSeparatedAmount((paymentDetailOtherwise.getAmount() + paymentDetailOtherwise.getTaxamount()), companyid));
                        accJSONArr.put(obj);
                        count++;
                    }
                }
         }
            
            int choice = pc.getPayDetail().getPaymentMethod().getDetailType();
            if (choice == 1) {//Card Holder
                cardreferencenumber = pc.getPayDetail().getCard().getRefNo();
                cardholder = pc.getPayDetail().getCard().getCardHolder();
//                chequecleardate = pc.getPayDetail().getCheque().getDueDate();
                cardno = pc.getPayDetail().getCard().getCardNo();
                cardtype = pc.getPayDetail().getCard().getCardType();
                expirydate =pc.getPayDetail().getCard().getExpiryDate();
                
//                
//                if (pc.getPayDetail() != null) {//clearancedate
//                    KwlReturnObject clearanceDate = accBankReconciliationObj.getBRfromJE(pc.getJournalEntry().getID(), pc.getCompany().getCompanyID(), false);
//                    if (clearanceDate != null && clearanceDate.getEntityList() != null && clearanceDate.getEntityList().size() > 0) {
//                        BankReconciliationDetail brd = (BankReconciliationDetail) clearanceDate.getEntityList().get(0);
//                        if (brd.getBankReconciliation().getClearanceDate() != null) {
//                            summaryData.put("clearanceDate", df.format(brd.getBankReconciliation().getClearanceDate()));
//                            summaryData.put("paymentStatus", true);
//                        }
//                    }
//                }

                summaryData.put(CustomDesignerConstants.Card_Reference_Number, cardreferencenumber);
                summaryData.put(CustomDesignerConstants.CardHolderName, cardholder);
//                summaryData.put(CustomDesignerConstants.ChequeDate, df.format(chequecleardate));
                summaryData.put(CustomDesignerConstants.CardNo, cardno);
                summaryData.put(CustomDesignerConstants.Card_Type, cardtype);
                summaryData.put(CustomDesignerConstants.Card_ExpiryDate, sdf.format(expirydate));
                summaryData.put(CustomDesignerConstants.Chequeno, bankchequeno);
                summaryData.put(CustomDesignerConstants.BankName, chequebankname);
                summaryData.put(CustomDesignerConstants.ChequeDate, "");
                summaryData.put(CustomDesignerConstants.BankDescription, invdesc);
                summaryData.put(CustomDesignerConstants.Cheque_Payment_Status, "");
                summaryData.put(CustomDesignerConstants.Cheque_Payment_ClearanceDate, "");
 
            } else if (choice == 2) {//if entry of cheque details
                bankchequeno = pc.getPayDetail().getCheque().getChequeNo();
                chequebankname = pc.getPayDetail().getCheque().getBankName();
                chequecleardate = pc.getPayDetail().getCheque().getDueDate();
    //             Date chequebankrecon=bankreconciliation.getClearanceDate();
                invdesc = pc.getPayDetail().getCheque().getDescription();
                summaryData.put(CustomDesignerConstants.Chequeno, bankchequeno);
                summaryData.put(CustomDesignerConstants.BankName, chequebankname);
                summaryData.put(CustomDesignerConstants.ChequeDate, sdf.format(chequecleardate));
                summaryData.put(CustomDesignerConstants.BankDescription, invdesc);
                KwlReturnObject clearanceDate = accBankReconciliationObj.getBRfromJE(pc.getJournalEntry().getID(), pc.getCompany().getCompanyID(), false);
                if (clearanceDate != null && clearanceDate.getEntityList() != null && clearanceDate.getEntityList().size() > 0) {
                    BankReconciliationDetail brd = (BankReconciliationDetail) clearanceDate.getEntityList().get(0);
                    if (brd.getBankReconciliation().getClearanceDate() != null) {//if clearance date then Payment Status is cleared else uncleared
                        summaryData.put(CustomDesignerConstants.Cheque_Payment_Status, "Cleared");
                        summaryData.put(CustomDesignerConstants.Cheque_Payment_ClearanceDate, sdf.format(brd.getBankReconciliation().getClearanceDate()));
                    } else {
                        summaryData.put(CustomDesignerConstants.Cheque_Payment_Status, "Uncleared");
                        summaryData.put(CustomDesignerConstants.Cheque_Payment_ClearanceDate, "");
                    }
                } else {
                    summaryData.put(CustomDesignerConstants.Cheque_Payment_Status, "Uncleared");
                    summaryData.put(CustomDesignerConstants.Cheque_Payment_ClearanceDate, "");
                }
                
                summaryData.put(CustomDesignerConstants.Card_Reference_Number, cardreferencenumber);
                summaryData.put(CustomDesignerConstants.CardHolderName, cardholder);
//                summaryData.put(CustomDesignerConstants.ChequeDate, "");
                summaryData.put(CustomDesignerConstants.CardNo, cardno);
                summaryData.put(CustomDesignerConstants.Card_Type, cardtype);
                summaryData.put(CustomDesignerConstants.Card_ExpiryDate, "");
            } else {
                summaryData.put(CustomDesignerConstants.Chequeno, bankchequeno);
                summaryData.put(CustomDesignerConstants.BankName, chequebankname);
                summaryData.put(CustomDesignerConstants.ChequeDate, "");
                summaryData.put(CustomDesignerConstants.BankDescription, invdesc);
                summaryData.put(CustomDesignerConstants.Cheque_Payment_Status, "");
                summaryData.put(CustomDesignerConstants.Cheque_Payment_ClearanceDate, "");
                summaryData.put(CustomDesignerConstants.Card_Reference_Number, cardreferencenumber);
                summaryData.put(CustomDesignerConstants.CardHolderName, cardholder);
                summaryData.put(CustomDesignerConstants.ChequeDate, "");
                summaryData.put(CustomDesignerConstants.CardNo, cardno);
                summaryData.put(CustomDesignerConstants.Card_Type, cardtype);
                summaryData.put(CustomDesignerConstants.Card_ExpiryDate, "");

            }

                 // Append comma separated invoice grid values-Against Customer Invoice
         if (invJSONArr != null) {
             for (int cnt = 0; cnt < invJSONArr.length(); cnt++) {
                 JSONObject jObj = (JSONObject) invJSONArr.get(cnt);
                 if (cnt == 0) {
                     invoiceNos = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_CustomerVendorInvoiceNo)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_CustomerVendorInvoiceNo));
                     invoicedates = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_InvoiceDate)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_InvoiceDate));
                     invduedates = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_DueDate)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_DueDate));
                     invtax = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_Tax)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_Tax));
                     invdiscount = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_Discount)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_Discount));
                     invoriginalamount = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_OriginalAmount)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_OriginalAmount));
                     invoriginalamountdue = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.Invoice_Original_Amount_Due)) ? "-" : jObj.getString(CustomDesignerConstants.Invoice_Original_Amount_Due));
                     invexchagerates = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.Invoice_Exchange_Rate)) ? "-" : jObj.getString(CustomDesignerConstants.Invoice_Exchange_Rate));
                     invamountdue = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_AmountDue)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_AmountDue));
                     inventerpayment = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_EnterPayment)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_EnterPayment));
                 } else {
                     invoiceNos = invoiceNos + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_CustomerVendorInvoiceNo)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_CustomerVendorInvoiceNo));
                     invoicedates = invoicedates + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_InvoiceDate)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_InvoiceDate));
                     invduedates = invduedates + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_DueDate)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_DueDate));
                     invtax = invtax + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_Tax)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_Tax));
                     invdiscount = invdiscount + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_Discount)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_Discount));
                     invoriginalamount = invoriginalamount + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_OriginalAmount)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_OriginalAmount));
                     invoriginalamountdue = invoriginalamountdue + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.Invoice_Original_Amount_Due)) ? "-" : jObj.getString(CustomDesignerConstants.Invoice_Original_Amount_Due));
                     invexchagerates = invexchagerates + "," +(StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.Invoice_Exchange_Rate)) ? "-" : jObj.getString(CustomDesignerConstants.Invoice_Exchange_Rate));
                     invamountdue = invamountdue + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_AmountDue)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_AmountDue));
                     inventerpayment = inventerpayment + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_EnterPayment)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_EnterPayment));
                 }
             }
         }

                 // Append comma separated Debit Note grid values for 3rd option-Credit Note
         if (debitJSONArr != null) {
             for (int cnt = 0; cnt < debitJSONArr.length(); cnt++) {
                 JSONObject jObj = (JSONObject) debitJSONArr.get(cnt);
                 if (cnt == 0) {
                     dnnumber = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.paymentDebit_note)) ? "-" : jObj.getString(CustomDesignerConstants.paymentDebit_note));
                     dnamount = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.paymentDebit_noteAmount)) ? "-" : jObj.getString(CustomDesignerConstants.paymentDebit_noteAmount));
                     dnamountdue = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.paymentDebit_note_AmountDue)) ? "-" : jObj.getString(CustomDesignerConstants.paymentDebit_note_AmountDue));
                     dnenterpayment = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.paymentDebit_note_EnterPayment)) ? "-" : jObj.getString(CustomDesignerConstants.paymentDebit_note_EnterPayment));
                 } else {
                     dnnumber = dnnumber + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.paymentDebit_note)) ? "-" : jObj.getString(CustomDesignerConstants.paymentDebit_note));
                     dnamount = dnamount + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.paymentDebit_noteAmount)) ? "-" : jObj.getString(CustomDesignerConstants.paymentDebit_noteAmount));
                     dnamountdue = dnamountdue + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.paymentDebit_note_AmountDue)) ? "-" : jObj.getString(CustomDesignerConstants.paymentDebit_note_AmountDue));
                     dnenterpayment = dnenterpayment + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.paymentDebit_note_EnterPayment)) ? "-" : jObj.getString(CustomDesignerConstants.paymentDebit_note_EnterPayment));
                 }
             }
         }

                 // Append comma separated account grid values for 4rth option-GL COde
         if (accJSONArr != null) {
             for (int cnt = 0; cnt < accJSONArr.length(); cnt++) {
                 JSONObject jObj = (JSONObject) accJSONArr.get(cnt);
                 if (cnt == 0) {
                     gridtype = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridType)) ? "-" : jObj.getString(CustomDesignerConstants.GridType));
                     gridaccname = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridAccountName)) ? "-" : jObj.getString(CustomDesignerConstants.GridAccountName));
                     gridaccamount = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridAmountinSGD)) ? "-" : jObj.getString(CustomDesignerConstants.GridAmountinSGD));
                     gridaccdesc = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridDesc)) ? "-" : jObj.getString(CustomDesignerConstants.GridDesc));
                     gridacctax = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridTax)) ? "-" : jObj.getString(CustomDesignerConstants.GridTax));
                     gridaccwithtax = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridAmountinTax)) ? "-" : jObj.getString(CustomDesignerConstants.GridAmountinTax));
                     gridacctaxamount =(StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridTaxAmount)) ? "-" : jObj.getString(CustomDesignerConstants.GridTaxAmount));
                     gridaccountcode=(StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridAccountCode)) ? "-" : jObj.getString(CustomDesignerConstants.GridAccountCode));
                 } else {
                     gridtype = gridtype + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridType)) ? "-" : jObj.getString(CustomDesignerConstants.GridType));
                     gridaccname = gridaccname + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridAccountName)) ? "-" : jObj.getString(CustomDesignerConstants.GridAccountName));
                     gridaccamount = gridaccamount + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridAmountinSGD)) ? "-" : jObj.getString(CustomDesignerConstants.GridAmountinSGD));
                     gridaccdesc = gridaccdesc + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridDesc)) ? "-" : jObj.getString(CustomDesignerConstants.GridDesc));
                     gridacctax = gridacctax + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridTax)) ? "-" : jObj.getString(CustomDesignerConstants.GridTax));
                     gridaccwithtax = gridaccwithtax + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridAmountinTax)) ? "-" : jObj.getString(CustomDesignerConstants.GridAmountinTax));
                     gridacctaxamount =gridacctaxamount+ "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridTaxAmount)) ? "-" : jObj.getString(CustomDesignerConstants.GridTaxAmount));
                     gridaccountcode=gridaccountcode +"," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.GridAccountCode)) ? "-" : jObj.getString(CustomDesignerConstants.GridAccountCode));
                 }
             }
         }
         
         if (receiptType == 1) {
             jArr.put(invJSONArr);
         } else if (receiptType == 7) {
             jArr.put(debitJSONArr);
         } else if (receiptType == 9) {
             jArr.put(accJSONArr);
         }else{        //against customer  
             jArr.put(CustJSONArr); 
         }
         
         boolean advancepayment = pc.isIsadvancepayment();
         Payment advancepaymentid = pc.getAdvanceid();

         if (advancepaymentid == null) {
             if (advancepayment) {
                 advanceAmount=pc.getDepositAmount();
                 summaryData.put(CustomDesignerConstants.RPMP_AdvanceAmount, advanceAmount);
                 
             }else{
                summaryData.put(CustomDesignerConstants.RPMP_AdvanceAmount, pc.getAdvanceamount());
             }
         }else{
              summaryData.put(CustomDesignerConstants.RPMP_AdvanceAmount, pc.getAdvanceamount());
         }
       
         
//         if (advancepaymentid == null) {//case where two records are made
//             if (advancepayment) {
//                 KwlReturnObject pdoresultreceiptno = accVendorPaymentobj.gettotalrecordOfpaymentno(receiptNumber);
//                 List<Payment> listreceiptids = pdoresultreceiptno.getEntityList();
////                    for (int count = 0; count < listreceiptids.size(); count++) {
//                 if (listreceiptids.size() > 1) {
//                     for (Payment receiptitr : listreceiptids) {
//                         Payment advanceid = receiptitr.getAdvanceid();
//                         if (advanceid == null) {
//                             continue;
//                         } else {
//                             summaryData.put(CustomDesignerConstants.RPMP_AdvanceAmount, receiptitr.getAdvanceamount() != 0 ? receiptitr.getAdvanceamount() : 0);
//                         }
//
//                     }
//                 } else {
//                     summaryData.put(CustomDesignerConstants.RPMP_AdvanceAmount, pc.getAdvanceamount());
//                 }
//             } else {
//                 summaryData.put(CustomDesignerConstants.RPMP_AdvanceAmount, pc.getAdvanceamount());
//             }
//         } else {
//             summaryData.put(CustomDesignerConstants.RPMP_AdvanceAmount, pc.getAdvanceamount());
//         }

         netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalamount)), currency,countryLanguageId);
         summaryData.put("summarydata", true);
         summaryData.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, totalamount);
         summaryData.put(CustomDesignerConstants.Include_GST, authHandler.formattedAmount(taxtotalgst, companyid));
         summaryData.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, netinword + " Only.");
         summaryData.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, authHandler.formattedAmount(subtotal, companyid));
         summaryData.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, config == null ? "" : config.getPdfPostText());
         
         if ((receiptType != 6) && (receiptType != 9)) {

         summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, (pc.getVendor() != null && pc.getVendor().getVendorAddresses() != null) ? (pc.getVendor().getVendorAddresses().getBillingAddress1() != null ? pc.getVendor().getVendorAddresses().getBillingAddress1() : "") : "");
         summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, (pc.getVendor() != null && pc.getVendor().getVendorAddresses() != null) ? (pc.getVendor().getVendorAddresses().getShippingAddress1() != null ? pc.getVendor().getVendorAddresses().getShippingAddress1() : "") : "");
         summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, (pc.getVendor() != null && pc.getVendor().getVendorAddresses() != null) ? (pc.getVendor().getVendorAddresses().getBillingCity1() != null ? pc.getVendor().getVendorAddresses().getBillingCity1() : "") : "");
         summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, (pc.getVendor() != null && pc.getVendor().getVendorAddresses() != null) ? (pc.getVendor().getVendorAddresses().getBillingState1() != null ? pc.getVendor().getVendorAddresses().getBillingState1() : "") : "");
         summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, (pc.getVendor() != null && pc.getVendor().getVendorAddresses() != null) ? (pc.getVendor().getVendorAddresses().getBillingCountry1() != null ? pc.getVendor().getVendorAddresses().getBillingCountry1() : "") : "");
         summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, (pc.getVendor() != null && pc.getVendor().getVendorAddresses() != null) ? (pc.getVendor().getVendorAddresses().getBillingPostal1() != null ? pc.getVendor().getVendorAddresses().getBillingPostal1() : "") : "");
         summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, (pc.getVendor() != null && pc.getVendor().getVendorAddresses() != null) ? (pc.getVendor().getVendorAddresses().getBillingPhone1() != null ? pc.getVendor().getVendorAddresses().getBillingPhone1() : "") : "");
         summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, (pc.getVendor() != null && pc.getVendor().getVendorAddresses() != null) ? (pc.getVendor().getVendorAddresses().getBillingFax1() != null ? pc.getVendor().getVendorAddresses().getBillingFax1() : "") : "");
         summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, (pc.getVendor() != null && pc.getVendor().getVendorAddresses() != null) ? (pc.getVendor().getVendorAddresses().getShippingCity1() != null ? pc.getVendor().getVendorAddresses().getShippingCity1() : "") : "");
         summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, (pc.getVendor() != null && pc.getVendor().getVendorAddresses() != null) ? (pc.getVendor().getVendorAddresses().getShippingState1() != null ? pc.getVendor().getVendorAddresses().getShippingState1() : "") : "");
         summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, (pc.getVendor() != null && pc.getVendor().getVendorAddresses() != null) ? (pc.getVendor().getVendorAddresses().getShippingCountry1() != null ? pc.getVendor().getVendorAddresses().getShippingCountry1() : "") : "");
         summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, (pc.getVendor() != null && pc.getVendor().getVendorAddresses() != null) ? (pc.getVendor().getVendorAddresses().getShippingPostal1() != null ? pc.getVendor().getVendorAddresses().getShippingPostal1() : "") : "");
         summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, (pc.getVendor() != null && pc.getVendor().getVendorAddresses() != null) ? (pc.getVendor().getVendorAddresses().getShippingPhone1() != null ? pc.getVendor().getVendorAddresses().getShippingPhone1() : "") : "");
         summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, (pc.getVendor() != null && pc.getVendor().getVendorAddresses() != null) ? (pc.getVendor().getVendorAddresses().getShippingFax1() != null ? pc.getVendor().getVendorAddresses().getShippingFax1() : "") : "");
         summaryData.put(CustomDesignerConstants.CustomerVendor_AccountCode,pc.getVendor()!=null?(pc.getVendor().getAcccode()!=null?pc.getVendor().getAcccode():""):"");
         summaryData.put(CustomDesignerConstants.CustomerVendor_Term,pc.getVendor()!=null?(pc.getVendor().getDebitTerm()!=null?pc.getVendor().getDebitTerm().getTermdays():""):"");
         
         }else{
             summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, "");
             summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, "");
             summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, "");
             summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, "");
             summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, "");
             summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, "");
             summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, "");
             summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, "");
             summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, "");
             summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, "");
             summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, "");
             summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, "");
             summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, "");
             summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, "");
             summaryData.put(CustomDesignerConstants.CustomerVendor_AccountCode,"");
             summaryData.put(CustomDesignerConstants.CustomerVendor_Term,"");

         }    
         summaryData.put(CustomDesignerConstants.PaymentMethod, paymentmethod);
         summaryData.put(CustomDesignerConstants.PaymentAccount, paymentaccount);
         summaryData.put(CustomDesignerConstants.RPMP_CustomerVendorName, accname);
         summaryData.put(CustomDesignerConstants.Main_Vendor_Name, mainvendorname);
         //Invoice comma separated values-Against Vendor Invoice
         summaryData.put(CustomDesignerConstants.RPMP_CustomerVendorInvoiceNo, invoiceNos);
         summaryData.put(CustomDesignerConstants.RPMP_InvoiceDate, invoicedates);
         summaryData.put(CustomDesignerConstants.RPMP_DueDate, invduedates);
         summaryData.put(CustomDesignerConstants.RPMP_Tax, invtax);
         summaryData.put(CustomDesignerConstants.RPMP_Discount, invdiscount);
         summaryData.put(CustomDesignerConstants.RPMP_OriginalAmount, invoriginalamount);
         summaryData.put(CustomDesignerConstants.Invoice_Original_Amount_Due, invoriginalamountdue);
         summaryData.put(CustomDesignerConstants.Invoice_Exchange_Rate, invexchagerates);
         summaryData.put(CustomDesignerConstants.RPMP_AmountDue, invamountdue);
         summaryData.put(CustomDesignerConstants.RPMP_EnterPayment, inventerpayment);
         //3rd option-Credit Note
         summaryData.put(CustomDesignerConstants.paymentDebit_note, dnnumber);
         summaryData.put(CustomDesignerConstants.paymentDebit_noteAmount, dnamount);
         summaryData.put(CustomDesignerConstants.paymentDebit_note_AmountDue, dnamountdue);
         summaryData.put(CustomDesignerConstants.paymentDebit_note_EnterPayment, dnenterpayment);

         //4rth option-GL Code
         summaryData.put(CustomDesignerConstants.GridType, gridtype);
         summaryData.put(CustomDesignerConstants.GridAccountName, gridaccname);
         summaryData.put(CustomDesignerConstants.GridAmountinSGD, gridaccamount);
         summaryData.put(CustomDesignerConstants.GridDesc, gridaccdesc);
         summaryData.put(CustomDesignerConstants.GridTax, gridacctax);
         summaryData.put(CustomDesignerConstants.GridTaxAmount, gridacctaxamount);
         summaryData.put(CustomDesignerConstants.GridAmountinTax, gridaccwithtax);
         summaryData.put(CustomDesignerConstants.GridAccountCode, gridaccountcode);
         
         summaryData.put(CustomDesignerConstants.SrNo, 1);
         summaryData.put(CustomDesignerConstants.CustomDesignPORefNumber_fieldTypeId, "");
         summaryData.put(CustomDesignerConstants.CustomDesignVendorQuoteRefNumber_fieldTypeId, "");
         summaryData.put(CustomDesignerConstants.CustomDesignGRORefNumber_fieldTypeId, "");
         summaryData.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, revExchangeRate);
         jArr.put(summaryData);

         //getting all the custom fields at line level
         result = customDesignDAOObj.getCustomLineFields(companyid, moduleid);
         list = result.getEntityList();
         for (int cnt = 0; cnt < list.size(); cnt++) {
             JSONObject obj = new JSONObject();
             HashMap<String, String> map = new HashMap<String, String>();
             Object[] rowcustom = (Object[]) list.get(cnt);
             map.put("Custom_" + rowcustom[2], "{label:'Custom_" + rowcustom[2] + "',xtype:'" + rowcustom[1].toString() + "'}");
             dbCustomJSONArr.put(map);
         }
         jArr.put(dbCustomJSONArr);
         return jArr;
     }
       
    @Override
    public void getAdvancePaymentCustomData(HashMap<String, Object> requestParams, AdvanceDetail advanceDetail, JSONObject obj) throws ServiceException {
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<>();
            HashMap<String, String> customDateFieldMap = new HashMap<>();
            Map<String, Object> variableMap = new HashMap<>();

            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Make_Payment_ModuleId, 1));
            HashMap<String, String> replaceFieldMap = new HashMap<>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

            HashMap<String, Object> invDetailRequestParams = new HashMap<>();
            ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
            Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
            Detailfilter_params.add(advanceDetail.getId());
            invDetailRequestParams.put("filter_names", Detailfilter_names);
            invDetailRequestParams.put("filter_params", Detailfilter_params);

            KwlReturnObject custumObjresult = accVendorPaymentobj.getVendorPaymentCustomData(invDetailRequestParams);
            if (custumObjresult.getEntityList().size() > 0) {
                AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) custumObjresult.getEntityList().get(0);
                AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                if (jeDetailCustom != null) {
                    JSONObject params = new JSONObject();
                    params.put(Constants.companyKey, companyid);
                    params.put(Constants.isLink, true);
                    if (requestParams.containsKey(Constants.requestModuleId) && requestParams.get(Constants.requestModuleId) != null) {
                        params.put(Constants.linkModuleId, requestParams.get(Constants.requestModuleId));
                    }
                    fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptController.getGoodsReceiptCustomDataForPayment : " + ex.getMessage(), ex);
        }
    }
    
   
    public static HashMap<String, Object> getPaymentMapJSON(JSONObject paramJObj) throws SessionExpiredException, JSONException, ServiceException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJObj);
        requestParams.put("start", paramJObj.optString("start", null));
        requestParams.put("limit", paramJObj.optString("limit", null));
        requestParams.put("ss", paramJObj.optString("ss", null));
        requestParams.put("deleted", paramJObj.optString("deleted", null));
        requestParams.put("nondeleted", paramJObj.optString("nondeleted", null));
        requestParams.put(Constants.REQ_startdate, paramJObj.optString("stdate", null));
        requestParams.put(Constants.REQ_enddate, paramJObj.optString("enddate", null));
        requestParams.put(Constants.Acc_Search_Json, paramJObj.optString(Constants.Acc_Search_Json, null));
        requestParams.put(Constants.Filter_Criteria, paramJObj.optString(InvoiceConstants.Filter_Criteria, null));
        requestParams.put(Constants.moduleid, paramJObj.optString(Constants.moduleid, null));
        requestParams.put(Constants.isRepeatedPaymentFlag, paramJObj.optString(Constants.isRepeatedPaymentFlag, null));
        if (paramJObj.optString("dir") != null && !StringUtil.isNullOrEmpty(paramJObj.optString("dir", null))
                && paramJObj.optString("sort", null) != null && !StringUtil.isNullOrEmpty(paramJObj.optString("sort", null))) {
            requestParams.put("dir", paramJObj.optString("dir"));
            requestParams.put("sort", paramJObj.optString("sort"));
        }
        if (!StringUtil.isNullOrEmpty(paramJObj.optString(Constants.requestModuleId, null))) {
            requestParams.put(Constants.requestModuleId, Integer.parseInt(paramJObj.optString(Constants.requestModuleId)));
        }
        return requestParams;
    }

   @Override 
    public JSONObject getPaymentsJSON(JSONObject paramJObj) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        List<JSONObject> list = new ArrayList<JSONObject>();
        List<JSONObject> tempList = new ArrayList<JSONObject>();
        int limitValue = 0, startValue = 0, dataCount = 0;
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = getPaymentMapJSON(paramJObj);
            String start = (String) requestParams.get("start");
            String limit = (String) requestParams.get("limit");
            /*
             * When check(Drop Down) to include child accounts is disabled then
             * includeExcludeChildCombobox flag will be set as TRUE to include
             * child accounts
             *
             * includeExcludeChildCombobox, if All = Include all child accounts
             * while fetching parent account data includeExcludeChildCombobox,
             * if TRUE = Include all child accounts while fetching parent
             * account data includeExcludeChildCombobox, if FALSE = Exclude
             * child acounts while fetching parent account data
             *
             */
            /*
             *
             * NULL pointer exception Caused due to
             * request.getParameter("includeExcludeChildCmb").toString().equals("All")
             *
             * To handle NULL pointer exception
             * (request.getParameter("includeExcludeChildCmb") != null)
             *
             */
            boolean includeExcludeChildCmb;

            if (paramJObj.optString("includeExcludeChildCmb", null) != null && paramJObj.optString("includeExcludeChildCmb").toString().equals("All")) {
                includeExcludeChildCmb = true;
            } else {
                includeExcludeChildCmb = paramJObj.optString("includeExcludeChildCmb", null) != null ? Boolean.parseBoolean(paramJObj.optString("includeExcludeChildCmb")) : false;
            }
            /*
             *
             * fetch payment report value from request or set defalut value
             * false
             *
             */
            boolean isPaymentReport = (paramJObj.optString("isPaymentReport", null) != null) ? Boolean.parseBoolean(paramJObj.optString("isPaymentReport").toString()) : false;
            /*
             * *
             * get vendor id *
             */
            String Vendorid = (String) paramJObj.optString("custVendorID", null) != null ? paramJObj.optString("custVendorID") : "All";
            boolean contraentryflag = paramJObj.optString("contraentryflag", null) != null;
            boolean isAdvancePayment = paramJObj.optString("advancePayment", null) != null;
            boolean isAdvanceToCustomer = paramJObj.optString("advanceToCustomer", null) != null;
            boolean isPostDatedCheque = paramJObj.optString("isPostDatedCheque", null) != null;
            boolean isDishonouredCheque = paramJObj.optString("isDishonouredCheque", null) != null;
            boolean isGlcode = paramJObj.optString("isGlcode", null) != null;
            String billid = paramJObj.optString("billid", null) != null ? paramJObj.optString("billid") : "";
            String userIdForPending = paramJObj.optString(Constants.useridKey);
            String userName = paramJObj.optString(Constants.useridKey);
            requestParams.put("userid", userIdForPending);
            requestParams.put("userName", userName);

            boolean onlyOpeningBalanceTransactionsFlag = false;
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("onlyOpeningBalanceTransactionsFlag", null))) {
                onlyOpeningBalanceTransactionsFlag = Boolean.parseBoolean(paramJObj.optString("onlyOpeningBalanceTransactionsFlag"));
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("ispendingAproval"))) {
                requestParams.put("ispendingAproval", Boolean.FALSE.parseBoolean(paramJObj.optString("ispendingAproval")));
            }

            boolean allAdvPayment = paramJObj.optString("allAdvPayment", null) != null;
            boolean unUtilizedAdvPayment = paramJObj.optString("unUtilizedAdvPayment", null) != null;
            boolean partiallyUtilizedAdvPayment = paramJObj.optString("partiallyUtilizedAdvPayment", null) != null;
            boolean fullyUtilizedAdvPayment = paramJObj.optString("fullyUtilizedAdvPayment", null) != null;
            boolean nonorpartiallyUtilizedAdvPayment = paramJObj.optString("nonorpartiallyUtilizedAdvPayment", null) != null;

            if (!StringUtil.isNullOrEmpty(paramJObj.optString("allAdvPayment", null))) {
                allAdvPayment = Boolean.parseBoolean(paramJObj.optString("allAdvPayment"));
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("unUtilizedAdvPayment", null))) {
                unUtilizedAdvPayment = Boolean.parseBoolean(paramJObj.optString("unUtilizedAdvPayment"));
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("partiallyUtilizedAdvPayment", null))) {
                partiallyUtilizedAdvPayment = Boolean.parseBoolean(paramJObj.optString("partiallyUtilizedAdvPayment"));
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("fullyUtilizedAdvPayment", null))) {
                fullyUtilizedAdvPayment = Boolean.parseBoolean(paramJObj.optString("fullyUtilizedAdvPayment"));
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("nonorpartiallyUtilizedAdvPayment", null))) {
                nonorpartiallyUtilizedAdvPayment = Boolean.parseBoolean(paramJObj.optString("nonorpartiallyUtilizedAdvPayment"));
            }

            requestParams.put("allAdvPayment", allAdvPayment);
            requestParams.put("unUtilizedAdvPayment", unUtilizedAdvPayment);
            requestParams.put("partiallyUtilizedAdvPayment", partiallyUtilizedAdvPayment);
            requestParams.put("fullyUtilizedAdvPayment", fullyUtilizedAdvPayment);
            requestParams.put("nonorpartiallyUtilizedAdvPayment", nonorpartiallyUtilizedAdvPayment);

            requestParams.put("contraentryflag", contraentryflag);
            requestParams.put("isadvancepayment", isAdvancePayment);
            requestParams.put("isadvancetocustomer", isAdvanceToCustomer);
            requestParams.put("isPostDatedCheque", isPostDatedCheque);
            requestParams.put("isDishonouredCheque", isDishonouredCheque);
            requestParams.put("isGlcode", isGlcode);
            requestParams.put("billid", billid);
            /*
             * put includeExcludeChildCmb value, ispaymenreport
             * value,custVendorID value in request param
             *
             */
            requestParams.put("includeExcludeChildCmb", includeExcludeChildCmb);
            requestParams.put("isPaymentReport", isPaymentReport);
            requestParams.put("custVendorID", Vendorid);
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("recordType", null))) {
                requestParams.put("receipttype", paramJObj.optString("recordType"));
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("paymentWindowType", null))) {
                requestParams.put("paymentWindowType", Integer.parseInt(paramJObj.optString("paymentWindowType")));
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("linknumber", null))) {
                requestParams.put("linknumber", paramJObj.optString("linknumber"));
            }

            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), paramJObj.optString(Constants.companyKey));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            if (extraCompanyPreferences != null && extraCompanyPreferences.isEnablesalespersonAgentFlow()) {
                int permCode = paramJObj.optInt(Constants.permCode);
                if (!((permCode & Constants.VENDOR_VIEWALL_PERMCODE) == Constants.VENDOR_VIEWALL_PERMCODE)) {
                    /*
                     * when (permCode & Constants.VENDOR_VIEWALL_PERMCODE) ==
                     * Constants.VENDOR_VIEWALL_PERMCODE is true then user has
                     * permission to view all vendors documents,so at that time
                     * there is need to filter record according to user&agent.
                     */
                    String userId = paramJObj.optString(Constants.useridKey);
                    requestParams.put("userid", userId);
                    requestParams.put("enablesalespersonagentflow", extraCompanyPreferences.isEnablesalespersonAgentFlow());
                }
            }

            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), paramJObj.optString(Constants.useridKey));
            User user = (User) userResult.getEntityList().get(0);
            if (!StringUtil.isNullOrEmpty(user.getDepartment())) {
                requestParams.put("userDepartment", user.getDepartment());
            }
            boolean consolidateFlag = paramJObj.optString("consolidateFlag", null) != null ? Boolean.parseBoolean(paramJObj.optString("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && paramJObj.optString("companyids", null) != null) ? paramJObj.optString("companyids").split(",") : paramJObj.optString(Constants.companyKey).split(",");
            String gcurrencyid = (consolidateFlag && paramJObj.optString(Constants.globalCurrencyKey, null) != null) ? paramJObj.optString(Constants.globalCurrencyKey) : paramJObj.optString(Constants.globalCurrencyKey);
            requestParams.put(Constants.start, start);
            requestParams.put(Constants.limit, limit);
            requestParams.put(Constants.onlydateformat, authHandler.getOnlyDateFormat());
            KwlReturnObject result = null;
            KwlReturnObject openingBalanceReceiptsResult = null;
            KwlReturnObject billingResult = null;
            String companyid = "";
            int totalCnt = 0;
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                paramJObj.put("companyid", companyid);
                paramJObj.put(Constants.globalCurrencyKey, gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put(Constants.globalCurrencyKey, gcurrencyid);
                requestParams.put("ispendingAproval", paramJObj.optString("ispendingAproval"));

                tempList.clear();
                if (onlyOpeningBalanceTransactionsFlag) {
                    // getting opening balance receipts
                    openingBalanceReceiptsResult = accVendorPaymentobj.getAllOpeningBalancePayments(requestParams);
                    totalCnt += openingBalanceReceiptsResult.getRecordTotalCount();
                    tempList = accVendorPaymentModuleServiceObj.getOpeningBalanceReceiptJsonForReport(paramJObj, openingBalanceReceiptsResult.getEntityList(), tempList);
                } else {
                    result = accVendorPaymentobj.getPayments(requestParams);
                    totalCnt += result.getRecordTotalCount();
                    tempList = getPaymentsJsonNew(requestParams, result.getEntityList(), tempList);
                    billingResult = accVendorPaymentobj.getBillingPayments(requestParams);
                    tempList = accVendorPaymentModuleServiceObj.getBillingPaymentsJson(requestParams, billingResult.getEntityList(), tempList);
                }
//                Collections.sort(tempList, Collections.reverseOrder(new VendorPaymentDateComparator()));
                list.addAll(tempList);
            }

            if (companyids.length > 1) {
                if (!StringUtil.isNullOrEmpty(limit) && !StringUtil.isNullOrEmpty(start)) {
                    limitValue = Integer.parseInt(limit);
                    startValue = Integer.parseInt(start);
                } else {
                    limitValue = list.size();
                    startValue = 0;
                }
                Iterator iterator = list.iterator();
                for (int i = 0; i < list.size(); i++) {
                    if (i >= startValue && dataCount < limitValue) {
                        JSONObject jSONObject = (JSONObject) iterator.next();
                        jArr.put(jSONObject);
                        dataCount++;
                    } else {
                        iterator.next();
                    }
                    if (dataCount == limitValue) {
                        break;
                    }
                }
            } else {
                for (Object obj : list) {
                    JSONObject jSONObject = (JSONObject) obj;
                    jArr.put(jSONObject);
                    dataCount++;
                }
            }
            jobj.put(Constants.RES_data, jArr);
            jobj.put(Constants.RES_count, totalCnt);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
  
  @Override 
    public JSONObject getDocumentsForLinkingWithDebitNoteJSON(JSONObject paramJobj) {
        JSONObject returnObject = new JSONObject();
        try {
            JSONArray DataArray = new JSONArray();
            DataArray = getGoodsReceiptsForPayment(paramJobj, DataArray);
            DataArray = getCreditNoteMergedForPayment(paramJobj, DataArray);
            returnObject.put(Constants.RES_data, DataArray);
            returnObject.put(Constants.RES_count, DataArray.length());
        } catch (Exception e) {
            Logger.getLogger(AccVendorPaymentServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return returnObject;
    } 
    
  
  @Override
    /*
     * TO get credit notes for making payment against vendor/customer
     */
    public JSONArray getCreditNoteMergedForPayment(JSONObject paramJobj, JSONArray DataJArr) throws ServiceException {

        try {
            HashMap<String, Object> requestParams = accCreditNoteController.getCreditNoteMapJson(paramJobj);
            String[] companyids = paramJobj.optString(Constants.companyKey).split(",");
            String gcurrencyid = paramJobj.optString(Constants.globalCurrencyKey);
            boolean isEdit = paramJobj.optString("isEdit",null) == null ? false : Boolean.parseBoolean(paramJobj.optString("isEdit"));
            requestParams.put("isEdit", isEdit);
            HashSet cnList = new HashSet();
            KwlReturnObject result = null;
            String companyid = "";
            boolean onlyAmountDue = false;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("onlyAmountDue",null))) {
                onlyAmountDue = Boolean.parseBoolean(paramJobj.optString("onlyAmountDue"));
            }
            requestParams.put("onlyAmountDue", onlyAmountDue);
            
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                paramJobj.put("companyid", companyid);
                paramJobj.put(Constants.globalCurrencyKey, gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put(Constants.globalCurrencyKey, gcurrencyid);
                boolean isNoteForPayment = false;
                boolean isVendor = false;
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("isNoteForPayment",null))) {
                    isNoteForPayment = Boolean.parseBoolean(paramJobj.optString("isNoteForPayment"));
                    isVendor = Boolean.parseBoolean(paramJobj.optString("isVendor"));
                }
                requestParams.put("isNoteForPayment", isNoteForPayment);
                requestParams.put("isNewUI", true);
                result = accCreditNoteDAOobj.getCreditNoteMerged(requestParams);
                if (isEdit && !StringUtil.isNullOrEmpty(paramJobj.optString("billId",null))) {
                    KwlReturnObject cndnResult = accCreditNoteDAOobj.getVendorCnPayment(paramJobj.optString("billId"));
                    for (Object cnObj : cndnResult.getEntityList()) {
                        Object[] objects = (Object[]) cnObj;
                        String cnnoteid = objects[0] != null ? (String) objects[1] : "";
                        cnList.add(cnnoteid);
                    }
                }
//                getCreditNoteMergedJsonForPayment(request, result.getEntityList(), DataJArr, cnList, isEdit);
                getCreditNoteMergedJsonForPayment(paramJobj, result.getEntityList(), DataJArr, cnList, isEdit);
                int cntype = StringUtil.isNullOrEmpty(paramJobj.optString("cntype",null)) ? 1 : Integer.parseInt(paramJobj.optString("cntype"));
                /*
                 removed  isNoteForPayment   flag while fetching opening CN/DN to solve   ERP-14948
                    opening CN/DN does not load in MP/RP when Document currency and Payment method currency is different
                 */
                if (cntype == 10 || ( !isVendor)) {// cntype=10 is just for help. value 10 have no any sense.
                    result = accCreditNoteDAOobj.getOpeningBalanceCNs(requestParams);
                    requestParams.put("cntype", 10);
                    getOpeningCreditNotesJson(requestParams, result.getEntityList(), DataJArr);
                } else if (cntype == 11 || ( isVendor)) {// cntype=11 is just for help. value 11 have no any sense.
                    result = accCreditNoteDAOobj.getOpeningBalanceVendorCNs(requestParams);
                    requestParams.put("cntype", 11);
                    getOpeningCreditNotesJson(requestParams, result.getEntityList(), DataJArr);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentControllerCMN.getCreditNoteMergedForPayment : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    
    public JSONArray getOpeningCreditNotesJson(HashMap<String, Object> requestParams, List list, JSONArray JArr) throws ServiceException {
        try {
            DateFormat df = (DateFormat) requestParams.get("df");
            String currencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            String companyid = (String) requestParams.get("companyid");
            int cnType = (Integer) requestParams.get("cntype");
            boolean isNoteForPayment = false;
            if (requestParams.containsKey("isNoteForPayment")) {
                isNoteForPayment = (Boolean) requestParams.get("isNoteForPayment");
            }

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            if (list != null && !list.isEmpty()) {
                for (Object creditNoteObj : list) {
                    CreditNote cn = (CreditNote) creditNoteObj;
                    JSONObject obj = new JSONObject();
                    Date creditNoteDate = null;
                    double externalCurrencyRate = 0d;
                    creditNoteDate = cn.getCreationDate();
                    externalCurrencyRate = cn.getExchangeRateForOpeningTransaction();
                    String transactionCurrencyId = (cn.getCurrency() == null ? currency.getCurrencyID() : cn.getCurrency().getCurrencyID());
                    obj.put("currencyidtransaction", transactionCurrencyId);
                    obj.put("currencysymboltransaction", (cn.getCurrency() == null ? currency.getSymbol() : cn.getCurrency().getSymbol()));
                    String currencyFilterForTrans = "";
                    if (requestParams.containsKey("currencyfilterfortrans")) {
                        currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
                    }
                    if (requestParams.containsKey("currencyfilterfortrans") && isNoteForPayment) {
                        currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
                        KwlReturnObject curresultpayment = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyFilterForTrans);
                        KWLCurrency currencyPayment = (KWLCurrency) curresultpayment.getEntityList().get(0);
                        obj.put("currencysymbol", (currencyPayment == null ? currency.getSymbol() : currencyPayment.getSymbol()));
                        obj.put("currencyid", (currencyPayment == null ? currency.getCurrencyID() : currencyPayment.getCurrencyID()));
                        obj.put("currencysymbolpayment", (currencyPayment == null ? currency.getSymbol() : currencyPayment.getSymbol()));
                    } else {
                        obj.put("currencysymbol", (cn.getCurrency() == null ? currency.getSymbol() : cn.getCurrency().getSymbol()));
                        obj.put("currencyid", (cn.getCurrency() == null ? currency.getCurrencyID() : cn.getCurrency().getCurrencyID()));
                    }
                    double amountdue = cn.isOtherwise() ? cn.getCnamountdue() : 0;
                    double amountDueOriginal = cn.isOtherwise() ? cn.getCnamountdue() : 0;
                    if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(transactionCurrencyId) && !currencyFilterForTrans.equals(transactionCurrencyId)) {
                        KwlReturnObject bAmtCurrencyFilter = null;
                        if (cn.isIsOpeningBalenceCN() && cn.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            requestParams.put("isRevalue", true);
                            bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, transactionCurrencyId, currencyFilterForTrans, creditNoteDate, externalCurrencyRate);
                            amountdue = (Double) bAmtCurrencyFilter.getEntityList().get(0);
                        } else {
                            bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, transactionCurrencyId, currencyFilterForTrans, creditNoteDate, externalCurrencyRate);
                            amountdue = (Double) bAmtCurrencyFilter.getEntityList().get(0);
                        }
                    }
                    obj.put("documentid", cn.getID());
                    obj.put("type", "Credit Note");
                    obj.put("documentno", cn.getCreditNoteNumber());
                    obj.put("documentType", 3);//3 for credit note
                    obj.put("amount", cn.getCnamount());
                    obj.put("date", df.format(cn.getCreationDate()));
                    obj.put("amountdue", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));
                    obj.put("exchangeratefortransaction", (amountDueOriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountDueOriginal));
                    obj.put("amountDueOriginal", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("amountDueOriginalSaved", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("accountid", cn.getAccount() == null ? "" : cn.getAccount().getID());
                    obj.put("accountnames", cn.getAccount() == null ? "" : cn.getAccount().getName());
                    obj.put("linkingdate",df.format(new Date()));
                    JArr.put(obj);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentControllerCMN.getOpeningCreditNotesJson : " + ex.getMessage(), ex);
        }
        return JArr;
    }
    
    
   @Override 
    public JSONArray getGoodsReceiptsForPayment(JSONObject paramJobj, JSONArray DataJArr) throws ServiceException {
        try {
            HashMap<String, Object> requestParams = accGoodsReceiptControllerCMN.getGoodsReceiptMapJson(paramJobj);
            requestParams.put(Constants.globalCurrencyKey, paramJobj.optString(Constants.globalCurrencyKey, null));
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("rcmApplicable",null))) {
                requestParams.put("gtaapplicable", Boolean.parseBoolean(paramJobj.optString("rcmApplicable")));
            }
            requestParams.put("getRecordBasedOnJEDate", true);          //sending getRecordBasedOnJEDate = true because only those invoice should be fetched whose JE posting date is greater then linking date
            KwlReturnObject result = accGoodsReceiptobj.getGoodsReceipts(requestParams);
            boolean isEdit = paramJobj.optString("isEdit",null) == null ? false : Boolean.parseBoolean(paramJobj.optString("isEdit"));
            requestParams.put("isEdit", isEdit);
            HashSet invoicesList = new HashSet();
            if (isEdit && !StringUtil.isNullOrEmpty(paramJobj.optString("billId",null))) {
                KwlReturnObject paymentResult = accountingHandlerDAOobj.getObject(Payment.class.getName(), paramJobj.optString("billId"));
                Payment payment = (Payment) paymentResult.getEntityList().get(0);
                Set<PaymentDetail> paymentDetails = payment.getRows();
                for (PaymentDetail paymentDetail : paymentDetails) {
                    invoicesList.add(paymentDetail.getGoodsReceipt().getID());
                }
            }
            DataJArr = getGoodsReceiptsJsonForPayment(requestParams, result.getEntityList(), invoicesList);//get normal purchase invoices 
//            getOpeningBalanceInvoicesJsonArray(request, DataJArr);//get opening balance invoices
            getOpeningBalanceInvoicesJsonArray(paramJobj, DataJArr);//get opening balance invoices
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentControllerCMN.getGoodsReceiptsForPayment : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
   
      public void getOpeningBalanceInvoicesJsonArray(JSONObject paramJobj, JSONArray DataJArr) throws ServiceException {
        try {
            HashMap requestParams = accGoodsReceiptControllerCMN.getGoodsReceiptMapJson(paramJobj);
            requestParams.put(Constants.globalCurrencyKey, paramJobj.optString(Constants.globalCurrencyKey, null));
            DateFormat df = (DateFormat) requestParams.get(GoodsReceiptCMNConstants.DATEFORMAT);
            String currencyid = (String) requestParams.get(GoodsReceiptCMNConstants.GCURRENCYID);
            String companyid = paramJobj.optString(Constants.companyKey);
            String accountId = paramJobj.optString("accid",null);
            if (!StringUtil.isNullOrEmpty(accountId)) {
                requestParams.put(GoodsReceiptCMNConstants.VENDORID, accountId);
            }
            List ll = null;
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            boolean onlyAmountDue = requestParams.get(GoodsReceiptCMNConstants.ONLYAMOUNTDUE) != null;
            KWLCurrency currencyFilter=null;
            requestParams.put("excludeNormalInv", true);
            KwlReturnObject result = accGoodsReceiptobj.getOpeningBalanceInvoices(requestParams);
            List list = result.getEntityList();
            if (list != null) {
                for (Object gReceiptObj : list) {
                    String grId = gReceiptObj.toString();
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), grId);
                    GoodsReceipt gReceipt = (GoodsReceipt) objItr.getEntityList().get(0);
                    currencyid = (gReceipt.getCurrency() == null ? currency.getCurrencyID() : gReceipt.getCurrency().getCurrencyID());

                    Date invoiceCreationDate = gReceipt.getCreationDate();
                    double externalCurrencyRate = gReceipt.getExchangeRateForOpeningTransaction();
                    double amountdue = 0;
                    double grAmount = gReceipt.getOriginalOpeningBalanceAmount();
                    if (gReceipt.isIsExpenseType()) {
                        ll = accGoodsReceiptCMN.getExpGRAmountDue(requestParams, gReceipt);
                        amountdue = (Double) ll.get(1);
                    } else {
                        if (gReceipt.isIsOpeningBalenceInvoice() && !gReceipt.isNormalInvoice()) {
                            ll = new ArrayList();
                            ll.add(gReceipt.getOriginalOpeningBalanceAmount());
                            ll.add(gReceipt.getOpeningBalanceAmountDue());
                            ll.add("");
                            ll.add(false);
                            ll.add(0.0);
                        } 
                        amountdue = (Double) ll.get(1);
                    }
                    if (onlyAmountDue && authHandler.round(amountdue, companyid) == 0 ) {//remove //belongsTo1099&&gReceipt.isIsExpenseType()\\ in case of viewing all accounts. [PS]
                        continue;
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("documentid", gReceipt.getID());
                    obj.put("type", "Invoice");
                    obj.put("documentno", gReceipt.getGoodsReceiptNumber());
                    obj.put("documentType", 2);//for purchase invoice
                    obj.put(GoodsReceiptCMNConstants.CURRENCYID, currencyid);
                    obj.put("currencyidtransaction", currencyid);
                    obj.put("currencysymboltransaction", (gReceipt.getCurrency() == null ? currency.getSymbol() : gReceipt.getCurrency().getSymbol()));
                    obj.put("accountid", gReceipt.getAccount() != null ? gReceipt.getAccount().getID() : "");
                    obj.put(GoodsReceiptCMNConstants.ACCOUNTNAMES, (gReceipt.getAccount() == null) ? "" : gReceipt.getAccount().getName());
                    obj.put(GoodsReceiptCMNConstants.DATE, df.format(invoiceCreationDate));
                    obj.put("linkingdate",df.format(new Date()));

                    obj.put(GoodsReceiptCMNConstants.AMOUNT, grAmount); //actual invoice amount
                    String currencyFilterForTrans = "";
                    if (requestParams.containsKey("currencyfilterfortrans")) {
                        currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
                         KwlReturnObject currencyFilterResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyFilterForTrans);
                        currencyFilter = (KWLCurrency) currencyFilterResult.getEntityList().get(0);
                    }
                    double amountDueOriginal = amountdue;
                    if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(currencyid) && !currencyFilterForTrans.equals(currencyid)) {
                        KwlReturnObject bAmtCurrencyFilter = null;
                        if (gReceipt.isIsOpeningBalenceInvoice() && gReceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, currencyid, currencyFilterForTrans, invoiceCreationDate, externalCurrencyRate);
                        } else {
                            bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, currencyid, currencyFilterForTrans, invoiceCreationDate, externalCurrencyRate);
                        }
                        amountdue = (Double) bAmtCurrencyFilter.getEntityList().get(0);
                    }
                    obj.put(GoodsReceiptCMNConstants.AMOUNTDUE, authHandler.round(amountdue, companyid));
                    obj.put("amountDueOriginal", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("amountDueOriginalSaved", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("exchangeratefortransaction", (amountDueOriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountDueOriginal));
                    obj.put("currencysymbolpayment", (currencyFilter == null ? currency.getSymbol() : currencyFilter.getSymbol()));
                    DataJArr.put(obj);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentControllerCMN.getOpeningBalanceInvoicesJsonArray : " + ex.getMessage(), ex);
        }
    }
      
    public JSONArray getGoodsReceiptsJsonForPayment(HashMap<String, Object> request, List<GoodsReceipt> list, HashSet invoicesList) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            String companyid = (String) request.get(GoodsReceiptCMNConstants.COMPANYID);
            String basecurrencyid = (String) request.get(GoodsReceiptCMNConstants.GCURRENCYID);
            DateFormat df = (DateFormat) request.get(GoodsReceiptCMNConstants.DATEFORMAT);
            List ll = null;
            KwlReturnObject company = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company companyObj = null;
            if (company.getEntityList() != null) {
                companyObj = (Company) company.getEntityList().get(0);
            }
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            String cashAccount = preferences.getCashAccount().getID();
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            String currencyFilterForTrans = request.get("currencyfilterfortrans") != null ? (String) request.get("currencyfilterfortrans") : "";
            KwlReturnObject currencyFilterResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyFilterForTrans);
            KWLCurrency currencyFilter = (KWLCurrency) currencyFilterResult.getEntityList().get(0);

            List<String> idsList = new ArrayList<String>();
            for (GoodsReceipt gReceipt : list) {
                idsList.add(gReceipt.getID());
            }
            Map<String, JournalEntry> GoodsReceiptJEMap = accGoodsReceiptobj.getGRInvoiceJEList(idsList);
            if (list != null && !list.isEmpty()) {
                for (GoodsReceipt gReceipt : list) {
                    if (!Boolean.parseBoolean(request.get("isEdit").toString()) || Boolean.parseBoolean(request.get("isEdit").toString()) && !(invoicesList.contains(gReceipt.getID()))) {

                        JournalEntry je = GoodsReceiptJEMap.get(gReceipt.getID());
                        JournalEntryDetail d = gReceipt.getVendorEntry();
                        Date invoiceDate = gReceipt.getCreationDate();
                        String currencyid = (gReceipt.getCurrency() == null ? currency.getCurrencyID() : gReceipt.getCurrency().getCurrencyID());
                        Account account = d.getAccount();
                        double amountdue = 0, amountDueOriginal = 0;
                        JSONObject obj = new JSONObject();
                        if (gReceipt.isIsExpenseType()) {
                            ll = accGoodsReceiptCMN.getExpGRAmountDue(request, gReceipt);
                            amountdue = gReceipt.getInvoiceamountdue();
                            if (amountdue != 0 && Math.abs(amountdue) >= 0.000001) {
                                amountDueOriginal = amountdue;
                                if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(currencyid) && !currencyFilterForTrans.equals(currencyid)) {
                                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(request, amountdue, currencyid, currencyFilterForTrans, gReceipt.getCreationDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                                    amountdue = (Double) bAmt.getEntityList().get(0);
                                }
                            } else {
                                amountdue = 0;
                            }
                            obj.put(GoodsReceiptCMNConstants.AMOUNT, (Double) ll.get(0));//for expense invoice   
                        } else {
                            ll = accGoodsReceiptCMN.getInvoiceDiscountAmountInfo(request, gReceipt);
                            amountdue = (Double) ll.get(1);
                            amountDueOriginal = (Double) ll.get(5);
                            obj.put(GoodsReceiptCMNConstants.AMOUNT, d.getAmount()); //actual invoice amount
                        }
                        amountdue = authHandler.round(amountdue, companyid);
                        obj.put("documentid", gReceipt.getID());
                        obj.put("type", "Invoice");
                        obj.put("documentno", gReceipt.getGoodsReceiptNumber());
                        obj.put("documentType", 2);//for purchase invoice
                        obj.put(GoodsReceiptCMNConstants.CURRENCYID, currencyid);
                        obj.put("currencyidtransaction", currencyid);
                        obj.put("currencysymboltransaction", (gReceipt.getCurrency() == null ? currency.getSymbol() : gReceipt.getCurrency().getSymbol()));
                        obj.put("accountid", gReceipt.getAccount() == null ? "" : gReceipt.getAccount().getID());
                        obj.put(GoodsReceiptCMNConstants.ACCOUNTNAMES, (gReceipt.getAccount() == null) ? "" : gReceipt.getAccount().getName());
                        obj.put(GoodsReceiptCMNConstants.DATE, df.format(gReceipt.getCreationDate()));
                        obj.put("claimedDate", gReceipt.getDebtClaimedDate() == null ? "" : df.format(gReceipt.getDebtClaimedDate()));
                        obj.put("isClaimedInvoice", (gReceipt.getBadDebtType() == 1 || gReceipt.getBadDebtType() == 2));// for Malasian Company
                        if (account.getID().equals(cashAccount)) {
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE, 0);
                            obj.put("amountDueOriginal", 0);
                            obj.put("amountDueOriginalSaved", 0);
                        } else {
                            obj.put(GoodsReceiptCMNConstants.AMOUNTDUE, authHandler.round(amountdue, companyid));
                            obj.put("amountDueOriginal", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                            obj.put("amountDueOriginalSaved", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                            obj.put("exchangeratefortransaction", (amountDueOriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountDueOriginal));
                        }
                        obj.put("currencysymbolpayment", (currencyFilter == null ? currency.getSymbol() : currencyFilter.getSymbol()));
                        obj.put("linkingdate",df.format(new Date()));
                        obj.put(Constants.SUPPLIERINVOICENO, gReceipt.getSupplierInvoiceNo() != null ? gReceipt.getSupplierInvoiceNo() : "");//added to fetch data in dn link window

                        JSONObject jObj = null;
                        double discountValue = 0.0;
                        int applicableDays = -1;
                        boolean discountType = false;
                        if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                            jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                            if (jObj.has(Constants.DISCOUNT_ON_PAYMENT_TERMS) && jObj.get(Constants.DISCOUNT_ON_PAYMENT_TERMS) != null && jObj.optBoolean(Constants.DISCOUNT_ON_PAYMENT_TERMS, false)) {
                                Term term = gReceipt.getTermid();
                                if (term != null && term.getDiscountName() != null) {
                                    DiscountMaster discountMaster = term.getDiscountName();
                                    discountValue = discountMaster.getValue();
                                    discountType = discountMaster.isDiscounttype();
                                    applicableDays = term.getApplicableDays();
                                }
                            }
                        }
                        DateFormat genericDF = authHandler.getGlobalDateFormat();
                        obj.put("discountvalue", discountValue);
                        obj.put("discounttype", discountType ? Integer.parseInt(Constants.DISCOUNT_MASTER_TYPE_PERCENTAGE) : Integer.parseInt(Constants.DISCOUNT_MASTER_TYPE_FLAT));
                        obj.put("applicabledays", applicableDays);
                        obj.put("grcreationdate", genericDF.format(invoiceDate));
                        HashMap<String, Object> requestMap = new HashMap<>();
                        requestMap.put(Constants.billid, gReceipt.getID());
                        /*
                         * Get Goods Receipt Custom Data For Payment
                         */
                        accGoodsReceiptServiceDAOObj.getGoodsReceiptCustomDataForPayment(request, obj, gReceipt, je);
                        if (companyObj != null && Integer.toString(Constants.indian_country_id).equals(companyObj.getCountry().getID())) { // only for indian company
                            obj.put("invType", (gReceipt.getExpenserows().size() > 0) ? "0" : "1"); // 0 = expence Type & 1 = inventory Type
                        } else {
                            obj.put("invType", "0");// default 0 for hide product column - only use for showing column
                        }
                        jArr.put(obj);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentControllerCMN.getGoodsReceiptsJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
   @Override 
    public JSONArray getCreditNoteMergedJsonForPayment(JSONObject paramJobj, List list, JSONArray jArr, HashSet cnList, boolean isEdit) throws ServiceException {
        try {
            HashMap<String, Object> requestParams = accCreditNoteController.getCreditNoteMapJson(paramJobj);
            String currencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            String transactionCurrencyId = (String) requestParams.get(Constants.globalCurrencyKey);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            String companyid = paramJobj.optString(Constants.companyKey);
            for (Object objArr : list) {
                Object[] row = (Object[]) objArr;
                if (!isEdit || (isEdit && !cnList.contains((String) row[1]))) {   // here, (String)row[1] refers to credit note id
                    KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), (String) row[3]);
                    JournalEntryDetail details = (JournalEntryDetail) resultObject.getEntityList().get(0);
                    JSONObject obj = new JSONObject();
                    resultObject = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), (String) row[1]);
                    CreditNote creditMemo = (CreditNote) resultObject.getEntityList().get(0);
                    JournalEntry je = creditMemo.getJournalEntry();
                    Date creditNoteDate = null;
                    double externalCurrencyRate = 0d;
                    transactionCurrencyId = (creditMemo.getCurrency() == null ? currency.getCurrencyID() : creditMemo.getCurrency().getCurrencyID());
                    obj.put("currencyidtransaction", transactionCurrencyId);
                    obj.put("currencysymboltransaction", (creditMemo.getCurrency() == null ? currency.getSymbol() : creditMemo.getCurrency().getSymbol()));

                    String currencyFilterForTrans = "";
                    boolean isNoteForPayment = false;
                    if (!StringUtil.isNullOrEmpty(paramJobj.optString("isNoteForPayment",null))) {
                        isNoteForPayment = Boolean.parseBoolean(paramJobj.optString("isNoteForPayment"));
                    }
                    if (requestParams.containsKey("currencyfilterfortrans") && isNoteForPayment) {
                        currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
                        KwlReturnObject curresultpayment = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyFilterForTrans);
                        KWLCurrency currencyPayment = (KWLCurrency) curresultpayment.getEntityList().get(0);
                        obj.put("currencysymbol", (currencyPayment == null ? currency.getSymbol() : currencyPayment.getSymbol()));
                        obj.put("currencyid", (currencyPayment == null ? currency.getCurrencyID() : currencyPayment.getCurrencyID()));
                        obj.put("currencysymbolpayment", (currencyPayment == null ? currency.getSymbol() : currencyPayment.getSymbol()));
                    } else {
                        obj.put("currencysymbol", (creditMemo.getCurrency() == null ? currency.getSymbol() : creditMemo.getCurrency().getSymbol()));
                        obj.put("currencyid", (creditMemo.getCurrency() == null ? currency.getCurrencyID() : creditMemo.getCurrency().getCurrencyID()));
                    }
                    double amountdue = creditMemo.isOtherwise() ? creditMemo.getCnamountdue() : 0;
                    double amountDueOriginal = creditMemo.isOtherwise() ? creditMemo.getCnamountdue() : 0;
                    if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(transactionCurrencyId) && !currencyFilterForTrans.equals(transactionCurrencyId)) {
                        creditNoteDate = creditMemo.getCreationDate();
                        externalCurrencyRate = je.getExternalCurrencyRate();
                        KwlReturnObject bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, transactionCurrencyId, currencyFilterForTrans, creditNoteDate, externalCurrencyRate);
                        amountdue = (Double) bAmtCurrencyFilter.getEntityList().get(0);
                    }
                    obj.put("documentid", creditMemo.getID());
                    obj.put("type", "Credit Note");
                    obj.put("documentno", creditMemo.getCreditNoteNumber());
                    obj.put("documentType", 3);//for credit note
                    obj.put("amount", creditMemo.isOtherwise() ? creditMemo.getCnamount() : details.getAmount());
                    obj.put("amountdue", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));
                    obj.put("exchangeratefortransaction", (amountDueOriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountDueOriginal));
                    obj.put("amountDueOriginal", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("amountDueOriginalSaved", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("date", df.format(creditMemo.getCreationDate()));
                    obj.put("accountid", creditMemo.getAccount() == null ? "" : creditMemo.getAccount().getID());
                    obj.put("accountnames", creditMemo.getAccount() == null ? "" : creditMemo.getAccount().getName());
                    obj.put("linkingdate",df.format(new Date()));
                    HashMap<String, Object> requestMap=new HashMap<>();
                    requestMap.put(Constants.billid,  creditMemo.getID());

                    /*
                     * Get global custom data for payment
                     */
                    accCreditNoteService.getCreditNoteCustomDataForPayment(requestParams, obj, creditMemo, je);
                    if (requestParams.containsKey("isReceipt") && obj.optDouble("amountdue", 0.0) != 0) {
                        jArr.put(obj);
                    } else if (!requestParams.containsKey("isReceipt")) {
                        jArr.put(obj);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentControllerCMN.getCreditNoteMergedJsonForPayment : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public JSONArray getSalesPaymentKnockOffJSON(Map<String, Object> invoiceRequestParams) throws ServiceException, JSONException{
        JSONArray allTransaction = new JSONArray();
        invoiceRequestParams.put("module_Id",Constants.Acc_Make_Payment_ModuleId);
        String companyid = (String) invoiceRequestParams.get(Constants.companyKey);
        List list = accPaymentDAOobj.getSalesPaymentKnockOffTransactions(invoiceRequestParams);
        boolean onlyAmountDue = invoiceRequestParams.containsKey("onlyAmountDue") ? (Boolean) invoiceRequestParams.get("onlyAmountDue") : false;
        for (int i = 0; i < list.size(); i++) {
            Object[] details = (Object[]) list.get(i);
            if(details[3]!=null && (Double)details[3] == 0){
                continue;
            }
            JSONObject obj = new JSONObject();
            double amountdueinbase = (Double) details[3] - (Double) details[5];
            amountdueinbase = authHandler.round(amountdueinbase, companyid);
            double amountdue = (Double) details[2] - (Double) details[4];
            amountdue = authHandler.round(amountdue, companyid);
            obj.put(InvoiceConstants.amountdueinbase, amountdueinbase);
            obj.put("amountdue", authHandler.round(amountdue, companyid));
            obj.put(Constants.billid, details[0]);
            obj.put("isOpeningBalanceTransaction", false);
            obj.put("creationdate", details[8]);
            obj.put(InvoiceConstants.personid, details[16]);
            obj.put("type", Constants.PAYMENT_MADE);
            if(!onlyAmountDue){
                obj.put(Constants.companyKey, companyid);
                obj.put("companyname", details[29]);
                obj.put("customername", details[17]);
                obj.put("customercode", details[19]);
                obj.put(InvoiceConstants.CustomerCreditTerm, details[20]);
                obj.put(InvoiceConstants.aliasname, details[18]);
                obj.put(InvoiceConstants.billno, details[1]);
                obj.put(Constants.currencyKey, details[25]);
                obj.put(InvoiceConstants.currencysymbol, details[27]);
                obj.put(InvoiceConstants.currencyname, details[26]);
                double externalCurrencyRate =  details[24] == null ? 1 : Double.parseDouble( details[24].toString());
                obj.put("externalcurrencyrate",externalCurrencyRate);
                String baseCurrencySymbol = (String)details[31];
                String exchangeRate = "1 "+baseCurrencySymbol+" = "+externalCurrencyRate+" "+obj.getString(InvoiceConstants.currencysymbol);
                obj.put("exchangerate", exchangeRate);
                obj.put("entrydate", details[14]);
                obj.put(Constants.shipdate, details[30]);
                obj.put(Constants.duedate, details[9]);
                obj.put(InvoiceConstants.personname, details[17]);
                obj.put("entryno", details[13]);
                obj.put("salespersonname", details[10]);
                obj.put("memo", details[23]);
                obj.put("salespersoncode", details[11]);
                obj.put("salespersonid", details[12]);
                obj.put("amountduenonnegative", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));//XX
                obj.put("amount", authHandler.round((Double) details[2], companyid));   //actual invoice amount
                obj.put("creditlimit", details[22]);
                obj.put("creditlimitinbase", details[22]);
            }
            allTransaction.put(obj);
        }
        return allTransaction;
    }
    
}
