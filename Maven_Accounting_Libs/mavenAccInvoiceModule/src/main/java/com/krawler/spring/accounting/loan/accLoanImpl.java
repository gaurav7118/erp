/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.loan;

import com.krawler.common.admin.*;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.common.KwlReturnObject;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.HibernateException;

/**
 *
 * @author Pandurang
 */
public class accLoanImpl extends BaseDAO implements accLoanDAO{ 
    
    @Override
    public KwlReturnObject getDisbursementCount(String loanrefnumber, String companyid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = "from Disbursement where loanrefnumber=? and company.companyID=?";
        list = executeQuery( q, new Object[]{loanrefnumber, companyid});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject saveLoanDisbursement(Map<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String disbursementid = (String) dataMap.get("disbursementid");

            Disbursement disbursement = new Disbursement();
            if (StringUtil.isNullOrEmpty(disbursementid)) {
                if (dataMap.containsKey("createdby")) {
                    User createdby = dataMap.get("createdby") == null ? null : (User) get(User.class, (String) dataMap.get("createdby"));
                    disbursement.setCreatedby(createdby);
                }
                if (dataMap.containsKey("modifiedby")) {
                    User modifiedby = dataMap.get("modifiedby") == null ? null : (User) get(User.class, (String) dataMap.get("modifiedby"));
                    disbursement.setModifiedby(modifiedby);
                }
                if (dataMap.containsKey("createdon")) {
                    disbursement.setCreatedon((Long) dataMap.get("createdon"));
                }
                if (dataMap.containsKey("updatedon")) {
                    disbursement.setUpdatedon((Long) dataMap.get("updatedon"));
                }
            } else {
                disbursement = (Disbursement) get(Disbursement.class, disbursementid);
                if (dataMap.containsKey("modifiedby")) {
                    User modifiedby = dataMap.get("modifiedby") == null ? null : (User) get(User.class, (String) dataMap.get("modifiedby"));
                    disbursement.setModifiedby(modifiedby);
                }
                if (dataMap.containsKey("updatedon")) {
                    disbursement.setUpdatedon((Long) dataMap.get("updatedon"));
                }
            }

            if (dataMap.containsKey(Constants.SEQFORMAT)) {
                disbursement.setSeqformat((SequenceFormat) get(SequenceFormat.class, (String) dataMap.get(Constants.SEQFORMAT)));
            }
            if (dataMap.containsKey(Constants.SEQNUMBER)) {
                disbursement.setSeqnumber(Integer.parseInt(dataMap.get(Constants.SEQNUMBER).toString()));
            }

            if (dataMap.containsKey(Constants.DATEPREFIX) && dataMap.get(Constants.DATEPREFIX) != null) {
                disbursement.setDatePreffixValue((String) dataMap.get(Constants.DATEPREFIX));
            }
            if (dataMap.containsKey(Constants.DATEAFTERPREFIX) && dataMap.get(Constants.DATEAFTERPREFIX) != null) {
                disbursement.setDateAfterPreffixValue((String) dataMap.get(Constants.DATEAFTERPREFIX));
            }
            if (dataMap.containsKey(Constants.DATESUFFIX) && dataMap.get(Constants.DATESUFFIX) != null) {
                disbursement.setDateSuffixValue((String) dataMap.get(Constants.DATESUFFIX));
            }
            if (dataMap.containsKey("loanTermType")) {
                disbursement.setTermType((String) dataMap.get("loanTermType"));
            }
            if (dataMap.containsKey("termValue")) {
                disbursement.setTermValue(Integer.parseInt(dataMap.get("termValue").toString()));
            }
            if (dataMap.containsKey("createdon")) {
                disbursement.setCreatedon((Long) dataMap.get("createdon"));
            }

            if (dataMap.containsKey("updatedon")) {

                disbursement.setUpdatedon((Long) dataMap.get("updatedon"));
            }

            if (dataMap.containsKey("createdby")) {
                User createdby = dataMap.get("createdby") == null ? null : (User) get(User.class, (String) dataMap.get("createdby"));
                disbursement.setCreatedby(createdby);
            }

            if (dataMap.containsKey("modifiedby")) {
                User modifiedby = dataMap.get("modifiedby") == null ? null : (User) get(User.class, (String) dataMap.get("modifiedby"));
                disbursement.setModifiedby(modifiedby);
            }

            if (dataMap.containsKey("loanRuleType")) {
                disbursement.setLoanRuleType((LoanRuleType) dataMap.get("loanRuleType"));
            }
            if (dataMap.containsKey("installmenttype")) {
                disbursement.setInstallmenttype((String) dataMap.get("installmenttype"));
            }
            if (dataMap.containsKey("installmentinterval")) {
                disbursement.setInstallmentinterval((Integer) dataMap.get("installmentinterval"));
            }
            if (dataMap.containsKey("scheduletype")) {
                disbursement.setScheduletype((String) dataMap.get("scheduletype"));
            }
            if (dataMap.containsKey("loancategory")) {
                disbursement.setLoancategory((String) dataMap.get("loancategory"));
            }
            if (dataMap.containsKey("entrynumber")) {
                disbursement.setLoanrefnumber((String) dataMap.get("entrynumber"));
            }
            if (dataMap.containsKey("surety") && dataMap.get("surety") != null && !dataMap.get("surety").toString().equals("")) {
                 Customer customer = dataMap.get("surety") == null ? null : (Customer) get(Customer.class, (String) dataMap.get("surety"));
                disbursement.setSurety(customer);
            }
            if (dataMap.containsKey("disburseAccount") && dataMap.get("disburseAccount") != null && !dataMap.get("disburseAccount").toString().equals("")) {
                 Account account = dataMap.get("disburseAccount") == null ? null : (Account) get(Account.class, (String) dataMap.get("disburseAccount"));
                disbursement.setCreditaccount(account);
            }
            if (dataMap.containsKey("loanAccount") && dataMap.get("loanAccount") != null && !dataMap.get("loanAccount").toString().equals("")) {
                 Account account = dataMap.get("loanAccount") == null ? null : (Account) get(Account.class, (String) dataMap.get("loanAccount"));
                disbursement.setDebitaccount(account);
            }
            if (dataMap.containsKey("autogenerated")) {
                disbursement.setAutoGenerated((Boolean) dataMap.get("autogenerated"));
            }
            if (dataMap.containsKey("customerid")) {
                Customer customer = dataMap.get("customerid") == null ? null : (Customer) get(Customer.class, (String) dataMap.get("customerid"));
                disbursement.setCustomer(customer);
            }

            if (dataMap.containsKey("disbursementdate") && dataMap.get("disbursementdate") != null && !dataMap.get("disbursementdate").toString().equals("")) {
                disbursement.setDisbursementdate((Date) dataMap.get("disbursementdate"));
            }
            if (dataMap.containsKey("firstpaymentdate") && dataMap.get("firstpaymentdate") != null && !dataMap.get("firstpaymentdate").toString().equals("")) {
                disbursement.setFirstpaymentdate((Date) dataMap.get("firstpaymentdate"));
            }
            if (dataMap.containsKey("approveddate") && dataMap.get("approveddate") != null && !dataMap.get("approveddate").toString().equals("")) {
                disbursement.setApproveddate((Date) dataMap.get("approveddate"));
            }
            if (dataMap.containsKey("applicationdate") && dataMap.get("applicationdate") != null && !dataMap.get("applicationdate").toString().equals("")) {
                disbursement.setApplicationdate((Date) dataMap.get("applicationdate"));
            }
            if (dataMap.containsKey("loanrate")) {
                disbursement.setLoanRate((Double) dataMap.get("loanrate"));
            }
            if (dataMap.containsKey("loanfee")) {
                disbursement.setLoanFee((Double) dataMap.get("loanfee"));
            }
            if(dataMap.containsKey("percentloanfee")){
                disbursement.setPercentloanfee((Double) dataMap.get("percentloanfee"));
            }
            if (dataMap.containsKey("externalCurrencyRate")) {
                disbursement.setExternalCurrencyRate((Double) dataMap.get("externalCurrencyRate"));
            }
            if (dataMap.containsKey("loanamount")) {
                disbursement.setLoanAmount((Double) dataMap.get("loanamount"));
            }
            if (dataMap.containsKey("loaneligibility")) {
                disbursement.setLoanEligibility((Double) dataMap.get("loaneligibility"));
            }
            if (dataMap.containsKey("loantype")) {
                MasterItem mi = (dataMap.get("loantype") == null ? null : (MasterItem) get(MasterItem.class, (String) dataMap.get("loantype")));
                disbursement.setLoanType(mi);
            }
            if (dataMap.containsKey("companyid")) {
                Company company = dataMap.get("companyid") == null ? null : (Company) get(Company.class, (String) dataMap.get("companyid"));
                disbursement.setCompany(company);
            }

            if (dataMap.containsKey("currencyid")) {
                disbursement.setCurrency((KWLCurrency) get(KWLCurrency.class, (String) dataMap.get("currencyid")));
            }


            saveOrUpdate(disbursement);
            list.add(disbursement);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveSalesOrder : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject getDisbursement(Map<String, Object> requestParams) throws ServiceException {
        List list = null;
        try {
            String condition = "";
            ArrayList params = new ArrayList();
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String companyid = (String) requestParams.get("companyid");
            params.add(companyid);

            String start = "";
            String limit = "";

            if (requestParams.containsKey("loanType") && requestParams.get("loanType") != null) {
                condition += " and loanType.ID = ? ";
                params.add((String) requestParams.get("loanType"));
            }
            if (requestParams.containsKey("loancategory") && requestParams.get("loancategory") != null) {
                condition += " and loancategory= ? ";
                params.add((String) requestParams.get("loancategory"));
            }

            if (requestParams.containsKey("billid") && requestParams.get("billid") != null) {
                String billID = (String) requestParams.get("billid");
                params.add(billID);
                condition += " and ID = ? ";
            }
            String startDate = (String) requestParams.get(Constants.REQ_startdate);
            String endDate = (String) requestParams.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                condition += " and (applicationdate >=? and applicationdate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }

            if (requestParams.containsKey(Constants.start) && requestParams.get(Constants.start) != null && requestParams.containsKey(Constants.limit) && requestParams.get(Constants.limit) != null) {
                start = (String) requestParams.get(Constants.start);
                limit = (String) requestParams.get(Constants.limit);
            }

            String ss = (requestParams.containsKey("ss") && requestParams.get("ss") != null) ? (String) requestParams.get("ss") : "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"loanrefnumber"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 1);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery;
            }

            String selQuery = "from Disbursement where company.companyID= ? " + condition;
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeQueryPaging( selQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            } else {
                list = executeQuery( selQuery, params.toArray());
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getDisbursement : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
     @Override
    public KwlReturnObject getDisbursementJEs(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String companyid = (String) request.get(Constants.companyKey);
            String ss = (String) request.get(Constants.ss);
            ArrayList params = new ArrayList();
            String condition = "";
            params.add(companyid);

            String jeIds = (String) request.get("jeIds");
            if (!StringUtil.isNullOrEmpty(jeIds)) {
                condition += " and dis.journalEntry.ID IN(" + jeIds + ")";
            }
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"dis.journalEntry.entryNumber"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 1);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery;
            }

            String query = "from Disbursement dis where dis.company.companyID=? " + condition;
            list = executeQuery( query, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            Logger.getLogger(accLoanImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accLoanImpl.getDisbursementJEs:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }
    @Override
    public KwlReturnObject getRepaymentSheduleDetails(Map<String, Object> requestParams) throws ServiceException {
        List list = null;
        try {
            String condition = "";
            ArrayList params = new ArrayList();
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String companyid = (String) requestParams.get("companyid");
            String paymentStatus = (String) requestParams.get("paymentStatus");
            String disbursementId = (String) requestParams.get("disbursementId");
            params.add(companyid);

            String start = "";
            String limit = "";

            if (requestParams.containsKey("disbursement") && requestParams.get("disbursement") != null) {
                condition += " and disbursement.ID = ? ";
                params.add((String) requestParams.get("disbursement"));
            }
            if ( !StringUtil.isNullOrEmpty(paymentStatus) &&  !paymentStatus.contentEquals("All") ) {
                condition += " and paymentStatus= ? ";
                if(paymentStatus.equalsIgnoreCase("0")){
                    params.add(PaymentStatus.Paid);
                }else if(paymentStatus.equalsIgnoreCase("1")){
                    params.add(PaymentStatus.Unpaid);
                }
//                params.add(paymentStatus);
            }
            String startDate = (String) requestParams.get(Constants.REQ_startdate);
            String endDate = (String) requestParams.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                condition += " and ( startDate >=? and startDate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }

            if (requestParams.containsKey(Constants.start) && requestParams.get(Constants.start) != null && requestParams.containsKey(Constants.limit) && requestParams.get(Constants.limit) != null) {
                start = (String) requestParams.get(Constants.start);
                limit = (String) requestParams.get(Constants.limit);
            }

//            String ss = (requestParams.containsKey("ss") && requestParams.get("ss") != null) ? (String) requestParams.get("ss") : "";
//            if (!StringUtil.isNullOrEmpty(ss)) {
//                String[] searchcol = new String[]{"loanrefnumber"};
//                StringUtil.insertParamSearchString(params, ss, 1);
//                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
//                condition += searchQuery;
//            }

            if(!StringUtil.isNullOrEmpty(disbursementId)){
                condition += " and disbursement.ID = ? ";
                params.add(disbursementId);
            }
            String selQuery = "from RepaymentDetails where company.companyID= ? " + condition;
//            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
//                list = executeQueryPaging( selQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
//            } else {
                list = executeQuery( selQuery, params.toArray());
//            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getRepaymentSheduleDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    
    @Override
    public KwlReturnObject deleteRepaymetDetails(String disbursementid, String companyid) throws ServiceException {
        try {
            String delQuery = "delete from RepaymentDetails where disbursement.ID= ? and company.companyID = ? ";
            int numRows = executeUpdate( delQuery, new Object[]{disbursementid, companyid});
            return new KwlReturnObject(true, "Repayment Details has been deleted successfully.", null, null, numRows);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot Edit Disbursement.", ex);//+ex.getMessage(), ex);as it is used in Payment already
        }
    }
   
    //function for deleting Loan Disbursment permananetly
    @Override
    public KwlReturnObject deleteDisbursementsPermanent(Map<String, Object> requestParams) throws ServiceException {
        try {
            int numtotal = 0;
            if (requestParams.containsKey("disbursementid") && requestParams.containsKey("companyid")) {
                ArrayList params = new ArrayList();
                params.add(requestParams.get("disbursementid"));
                params.add(requestParams.get("companyid"));
                
                String delQuery  = "delete from RepaymentDetails where disbursement.ID= ? and company.companyID = ? ";
                executeUpdate( delQuery, params.toArray());
                
                delQuery = "delete from Disbursement where ID= ? and company.companyID = ? ";
                numtotal = executeUpdate( delQuery, params.toArray());            
            }
            return new KwlReturnObject(true, "Loan Disbursement has been deleted successfully.", null, null, numtotal);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot delete Loan Disbursement as its referance child field is not deleted.", ex);//+ex.getMessage(), ex);
        }
    }
    
    public KwlReturnObject getRepaymentSheduleDetailsForPayment(Map<String, Object> requestParams) throws ServiceException {
        List list = null;
        try {
            String condition = "";
            ArrayList params = new ArrayList();
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String companyid = (String) requestParams.get("companyid");
            String paymentStatus = (String) requestParams.get("paymentStatus");
            String accId=(String) requestParams.get("accId");
            params.add(companyid);
            
            if (requestParams.containsKey("disbursement") && requestParams.get("disbursement") != null) {
                condition += " and disbursement.ID = ? ";
                params.add((String) requestParams.get("disbursement"));
            }
            if ( !StringUtil.isNullOrEmpty(paymentStatus) &&  !paymentStatus.contentEquals("All") ) {
                condition += " and paymentStatus= ? ";
                if(paymentStatus.equalsIgnoreCase("0")){
                    params.add(PaymentStatus.Paid);
                }else if(paymentStatus.equalsIgnoreCase("1")){
                    params.add(PaymentStatus.Unpaid);
                }
            }
            String startDateUpperLimit = (String) requestParams.get("upperLimitDate");
            if (!StringUtil.isNullOrEmpty(startDateUpperLimit)) {
                condition += " and startDate <=? ";
                params.add(df.parse(startDateUpperLimit));
            }
             if (!StringUtil.isNullOrEmpty(accId)) {
                condition += " and disbursement.customer.ID= ? ";
                params.add(accId);
            }
            condition += "order by startDate DESC ";
            String selQuery = "from RepaymentDetails where company.companyID= ? " + condition;
            list = executeQuery( selQuery, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getRepaymentSheduleDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public int saveFileMapping(Map<String, Object> filemap) throws ServiceException{
        int count=0;
        String columns="";
        String values="";
        String query="";
        String id = !StringUtil.isNullOrEmpty(filemap.get("id").toString())?filemap.get("id").toString():"";
        String companyid = !StringUtil.isNullOrEmpty(filemap.get("companyid").toString())?filemap.get("companyid").toString():"";
        String documentid = !StringUtil.isNullOrEmpty(filemap.get("documentid").toString())?filemap.get("documentid").toString():"";
        try{
        if(!StringUtil.isNullOrEmpty(id)){
            columns+="id";
            values+="'"+id+"'";
        }
        if(!StringUtil.isNullOrEmpty(companyid)){
            columns+=",company";
            values+=",'"+companyid+"'";
        }
        if(!StringUtil.isNullOrEmpty(documentid)){
            columns+=",documentid";
            values+=",'"+documentid+"'";
        }
        if(!columns.equals("")){
            columns = "(" +columns+")";
        }
        if(!values.equals("")){
            values = "(" +values+")";
        }
        query = "insert into invoicedoccompmaptemporary "+columns+" values "+values;
        count = executeSQLUpdate(query);
        } catch(ServiceException ex){
            throw ServiceException.FAILURE("saveFileMapping : " + ex.getMessage(), ex);
        }
        return count;
    }

    
    
    @Override
    public KwlReturnObject getTemporarySavedFiles(Map<String, Object> requestParams) throws ServiceException {
       List ll = null;
        String fileid = "";
        int dl = 0;
        try {

            if (requestParams.containsKey("fileid") && requestParams.get("fileid") != null) {
                fileid = requestParams.get("fileid").toString();
            }
            String query = "select docid,docname,doctypeid,id from invoicedocuments where id in (" + fileid + ")";
            ll = executeSQLQuery(query);
            dl = ll.size();
        } catch (HibernateException ex) {
            throw ServiceException.FAILURE("getTemporarySavedFiles", ex);
        }
        return new KwlReturnObject(true,"", "", ll, dl);
    }

    
}
