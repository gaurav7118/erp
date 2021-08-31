/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.bankreconciliation;

import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;

/**
 *
 * @author krawler
 */
public class AccBankReconciliationModuleServiceImpl implements AccBankReconciliationModuleService, MessageSourceAware {

    private accBankReconciliationDAO accBankReconciliationObj;
    private MessageSource messageSource;
    private auditTrailDAO auditTrailObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private ImportHandler importHandler;
    private AccountingHandlerDAO accountingHandlerDAOobj;

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setaccBankReconciliationDAO(accBankReconciliationDAO accBankReconciliationObj) {
        this.accBankReconciliationObj = accBankReconciliationObj;
    }

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

}
