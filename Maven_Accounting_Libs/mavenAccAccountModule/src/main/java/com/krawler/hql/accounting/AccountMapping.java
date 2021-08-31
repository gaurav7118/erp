/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

/**
 *
 * @author Malhari Pawar
 */
public class AccountMapping {

    private String id;
    private Account parentAccountId;
    private Account childAccountId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Account getParentAccountId() {
        return parentAccountId;
    }

    public void setParentAccountId(Account parentAccountId) {
        this.parentAccountId = parentAccountId;
    }

    public Account getChildAccountId() {
        return childAccountId;
    }

    public void setChildAccountId(Account childAccountId) {
        this.childAccountId = childAccountId;
    }
}
