/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.krawler.spring.accounting.invoice.dm;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.Invoice;



/**
 *
 * @author sagar
 */
public class InvoiceInfo {
    private String invoiceID;
    private Invoice invoice;
    private Company company;
    private Customer customer;
    private KWLCurrency currency;

    public KWLCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(KWLCurrency currency) {
        this.currency = currency;
    }

    public String getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(String invoiceID) {
        this.invoiceID = invoiceID;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

//    public BillingShippingAddresses getBillingShippingAddresses() {
//        return billingShippingAddresses;
//    }
//
//    public void setBillingShippingAddresses(BillingShippingAddresses billingShippingAddresses) {
//        this.billingShippingAddresses = billingShippingAddresses;
//    }
}