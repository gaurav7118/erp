/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.multiLevelApprovalRule;

/**
 *
 * @author krawler
 */
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.MasterItem;
import com.krawler.hql.accounting.Product;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 *
 * @author krawler
 */
public class MultiLevelApprovalControllerCMN extends MultiActionController implements  MessageSourceAware {

    private AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj;
    private HibernateTransactionManager txnManager;
    private auditTrailDAO auditTrailObj;
    private MessageSource messageSource;
    private AccountingHandlerDAO accountingHandlerDAOobj;

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setaccMultiLevelApprovalDAOObj(AccMultiLevelApprovalDAO accMultiLevelApprovalDAO) {
        this.accMultiLevelApprovalDAOObj = accMultiLevelApprovalDAO;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
    
    public ModelAndView getMultiApprovalRuleData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONArray jArr = new JSONArray();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String moduleid = request.getParameter("moduleid");
            char deptwiseapprover='F';
            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
            qdDataMap.put("companyid", companyid);
            qdDataMap.put("moduleid", moduleid);
            if ((request.getParameter("start") != null) && (request.getParameter("limit") != null)) {
                qdDataMap.put("start", Integer.parseInt(request.getParameter("start")));
                qdDataMap.put("limit", Integer.parseInt(request.getParameter("limit")));
            }
            KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
            int totalCount=flowresult.getRecordTotalCount();
            Iterator itr = flowresult.getEntityList().iterator();
            while (itr.hasNext()) {
                String creatorIds="";
                String creatorName="";
                String appliedupon="";
                Object[] row = (Object[]) itr.next();
                JSONObject obj = new JSONObject();
                if (row[6] != null && !row[6].toString().equals("")) {
                    String userIdArray[] = row[6].toString().split(",");
                    creatorIds=row[6].toString();
                    for (int i = 0; i < userIdArray.length; i++) {
                        KwlReturnObject userobj = accountingHandlerDAOobj.getObject(User.class.getName(), userIdArray[i]);
                        User user = (User) userobj.getEntityList().get(0);
                        creatorName += user.getFirstName() + ",";
                    }
                    creatorName = creatorName.substring(0, creatorName.length() - 1);
                }
                String ruleCondition="";
                String lowerLimit="";
                String upperLimit="";
                if(row[5]==null){
                    appliedupon = "-";
                }else if(row[5].toString().equals("") || Integer.parseInt(row[5].toString())==Constants.All_Conditions){
                    if(row[2].toString().equals("")){
                        appliedupon = "All Conditions";
                    }else{
                        appliedupon = "-";
                    }
                }else if(Integer.parseInt(row[5].toString())==Constants.Total_Amount){
                    appliedupon = "Total Amount" ;
                }else if(Integer.parseInt(row[5].toString())==Constants.Profit_Margin_Amount){
                    appliedupon ="Profit Margin Amount";
                }else if(Integer.parseInt(row[5].toString())==Constants.Specific_Products){
                    appliedupon = "Products";
                }else if(Integer.parseInt(row[5].toString())==Constants.Specific_Products_Discount){
                    appliedupon = "Products Discount";
                }else if(Integer.parseInt(row[5].toString())==Constants.Specific_Products_Category){
                    /*
                     *If Rule is apply as product category from multiapproverule window 
                     */
                    appliedupon = "Products Category";
                }else if(Integer.parseInt(row[5].toString())==Constants.SO_CREDIT_LIMIT){
                    /*
                     *If Rule is applied as SO Credit Limit from multiapproverule window ERM-396
                     */
                    appliedupon = "SO Credit Limit";
                }else{
                    appliedupon = "Creator";
                }
                String rule="";
                String ruleProductIds="";
                String discountRule= row[7]!=null ? row[7].toString() : "";
                if(!row[2].toString().equals("")){
                    if(Integer.parseInt(row[5].toString())==Constants.Specific_Products || Integer.parseInt(row[5].toString())==Constants.Specific_Products_Discount||Integer.parseInt(row[5].toString()) == Constants.Specific_Products_Category){
                        String productids=row[2].toString();
                        String productIDs= "";
                        String productIdArr[] = productids.split(",");
                        if (Integer.parseInt(row[5].toString()) == Constants.Specific_Products_Category) {
                            /*
                             * Get value from master Item If Rule is apply as
                             * product category from multiapproverule window
                             */
                            KwlReturnObject productCat = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), productids);
                            MasterItem prdCat = (MasterItem) productCat.getEntityList().get(0);
                            if (prdCat != null) {
                                /*
                                 * Product Category Value is assigned to
                                 * productIDs
                                 */

                                productIDs = prdCat.getValue() + ", ";
                            }
                        } else {
                            for (int cnt = 0; cnt < productIdArr.length; cnt++) {
                                KwlReturnObject productRes = accountingHandlerDAOobj.getObject(Product.class.getName(), productIdArr[cnt]);
                                Product prd = (Product) productRes.getEntityList().get(0);
                            if(prd!=null){
                                productIDs=productIDs+prd.getProductid()+", ";
                                }
                            }
                        }
                        if(!StringUtil.isNullOrEmpty(productIDs)){
                            productIDs = productIDs.substring(0, productIDs.length()-2);
                        }
                        rule=productIDs;
                        ruleProductIds=row[2].toString();
                    }else{
                        rule=row[2].toString();
                    }
                }else{
                    if(!row[6].toString().equals("")){
                        rule="JE Created By "+creatorName;
                    }else{
                        rule=row[2].toString();
                    }
                }
                if(row[5]!=null && !row[5].toString().equals("")){
                    if(Integer.parseInt(row[5].toString())==Constants.Total_Amount || Integer.parseInt(row[5].toString())==Constants.Profit_Margin_Amount || Integer.parseInt(row[5].toString())==Constants.Specific_Products_Discount){
                        Map<String, String> ruleDetails = new HashMap<>();
                        if(Integer.parseInt(row[5].toString())==Constants.Specific_Products_Discount){
                            ruleDetails = getRuleCnoditionAndLowerUpperLimit(discountRule);
                        }else{
                            ruleDetails = getRuleCnoditionAndLowerUpperLimit(rule);
                        }
                        ruleCondition=ruleDetails.get("rulecondition");
                        lowerLimit=ruleDetails.get("lowerlimit");
                        upperLimit=ruleDetails.get("upperlimit");
                    }
                }
                if(Integer.parseInt(row[5].toString())==Constants.SO_CREDIT_LIMIT){     //ERM-396
                    rule="";
                }
                obj.put("id", row[0]);
                obj.put("level", row[1]);
                obj.put("rule", rule);
                obj.put("ruleproductids", ruleProductIds);
                obj.put("discountrule", discountRule);
                obj.put("appliedupon", appliedupon);  //appliedUpo= 0 for DO/GR Approval Rule , ==1 for JE approval rule applied upon total amount and ==2 for JE approval rule applied upon Creator
                obj.put("applieduponid", row[5]!=null?Integer.parseInt(row[5].toString()):Constants.All_Conditions);
                obj.put("creator", creatorIds);  
                obj.put("rulecondition", ruleCondition); 
                obj.put("lowerlimit", lowerLimit); 
                obj.put("upperlimit", upperLimit); 
                if (row[8] != null) {
                    deptwiseapprover=(char)row[8];
                    if(deptwiseapprover=='T'){
                         obj.put("deptwiseapprover", true);
                    }else{
                        obj.put("deptwiseapprover", false);
                    }
                }
                String userName = "", userId = "";
                qdDataMap.put("ruleid", row[0].toString());
                KwlReturnObject userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(qdDataMap);
                Iterator useritr = userResult.getEntityList().iterator();
                while (useritr.hasNext()) {
                    Object[] userrow = (Object[]) useritr.next();
                    userId += userrow[0] + ",";
                    userName += userrow[1] + ",";
                }
                if (!StringUtil.isNullOrEmpty(userName)) {
                    userName = userName.substring(0, userName.length() - 1);
                    userId = userId.substring(0, userId.length() - 1);
                }
                obj.put("users", userName);
                obj.put("userids", userId);
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", totalCount);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(MultiLevelApprovalController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "MultiLevelApprovalController.getMultiApprovalRuleData:" + ex.getMessage();
            Logger.getLogger(MultiLevelApprovalController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "MultiLevelApprovalController.getMultiApprovalRuleData:" + ex.getMessage();
            Logger.getLogger(MultiLevelApprovalController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(MultiLevelApprovalController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    Map<String, String> getRuleCnoditionAndLowerUpperLimit(String rule){
        String ruleCondition="";
        String lowerLimit="";
        String upperLimit="";
        Map<String, String> ruleDetails = new HashMap<>();
        if (rule.contains("<=$$") && rule.contains("$$<=")) {
            ruleCondition = "4";
            lowerLimit = rule.substring(0, rule.indexOf("<=$$"));
            upperLimit = rule.substring(rule.indexOf("$$<=") + 4, rule.length());
        } else if (rule.contains("$$==")) {
            ruleCondition = "3";
            lowerLimit = rule.substring(rule.indexOf("$$==") + 4, rule.length());
        } else if (rule.contains("$$<")) {
            ruleCondition = "2";
            lowerLimit = rule.substring(rule.indexOf("$$<") + 3, rule.length());
        } else {
            ruleCondition = "1";
            lowerLimit = rule.substring(rule.indexOf("$$>") + 3, rule.length());
        }
        ruleDetails.put("rulecondition", ruleCondition);
        ruleDetails.put("lowerlimit", lowerLimit);
        ruleDetails.put("upperlimit", upperLimit);
        return ruleDetails;
    }
}