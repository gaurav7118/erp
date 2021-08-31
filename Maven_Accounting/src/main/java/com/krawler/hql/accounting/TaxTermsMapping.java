/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.InvoiceTermsSales;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.Tax;
import com.krawler.hql.accounting.Templatepnl;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.utils.json.JSONArray;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 * @author krawler
 */
public class TaxTermsMapping {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    private Tax tax;
    private InvoiceTermsSales invoicetermssales;

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public InvoiceTermsSales getInvoicetermssales() {
        return invoicetermssales;
    }

    public void setInvoicetermssales(InvoiceTermsSales invoicetermssales) {
        this.invoicetermssales = invoicetermssales;
    }

}
