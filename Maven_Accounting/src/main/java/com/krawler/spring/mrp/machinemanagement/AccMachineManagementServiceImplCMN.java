/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.machinemanagement;

//import com.krawler.hql.accounting.MachineManRatio;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;

/**
 *
 * @author krawler
 */
public class AccMachineManagementServiceImplCMN implements AccMachineManagementServiceDAOCMN {

    private MessageSource messageSource;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private exportMPXDAOImpl exportDaoObj;

    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }

    public AccountingHandlerDAO getAccountingHandlerDAOobj() {
        return accountingHandlerDAOobj;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public exportMPXDAOImpl getExportDaoObj() {
        return exportDaoObj;
    }

    public void setExportDaoObj(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    
 
 
}
