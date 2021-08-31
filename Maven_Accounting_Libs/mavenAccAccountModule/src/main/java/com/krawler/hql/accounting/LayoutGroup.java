/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class LayoutGroup {

    private String ID;
    private String name;
    private int nature;
    private int sequence;
    private Company company;
    private Templatepnl template;
    private LayoutGroup parent;
    private int showtotal;
    private int showchild;
    private int showchildacc;
    private Set<LayoutGroup> children;
    private boolean excludeChildAccountBalances; // do not add child account balance in parent account
    private int  numberofrows; // Add Blank row Before Group
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Set<LayoutGroup> getChildren() {
        return children;
    }

    public void setChildren(Set<LayoutGroup> children) {
        this.children = children;
    }

    public LayoutGroup getParent() {
        return parent;
    }

    public void setParent(LayoutGroup parent) {
        this.parent = parent;
    }

    public Templatepnl getTemplate() {
        return template;
    }

    public void setTemplate(Templatepnl template) {
        this.template = template;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNature() {
        return nature;
    }

    public void setNature(int nature) {
        this.nature = nature;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public int getShowchild() {
        return showchild;
    }

    public void setShowchild(int showchild) {
        this.showchild = showchild;
    }

    public int getShowchildacc() {
        return showchildacc;
    }

    public void setShowchildacc(int showchildacc) {
        this.showchildacc = showchildacc;
    }

    public int getShowtotal() {
        return showtotal;
    }

    public void setShowtotal(int showtotal) {
        this.showtotal = showtotal;
    }

    public boolean isExcludeChildAccountBalances() {
        return excludeChildAccountBalances;
    }

    public void setExcludeChildAccountBalances(boolean excludeChildAccountBalances) {
        this.excludeChildAccountBalances = excludeChildAccountBalances;
    }
    
    public int getNumberofrows() {
        return numberofrows;
}

    public void setNumberofrows(int numberofrows) {
        this.numberofrows = numberofrows;
    }

    }
