/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import java.util.Set;

/**
 *
 * @author krawler
 */

public class DefaultLayoutGroup {

    private String ID;
    private String name;
    private int nature;
    private int sequence;
    private DefaultTemplatePnL template;
    private DefaultLayoutGroup parent;
    private int showtotal;
    private int showchild;
    private int showchildacc;
    private Set<DefaultLayoutGroup> children;
    private boolean excludeChildAccountBalances; // do not add child account balance in parent account

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
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

    public DefaultTemplatePnL getTemplate() {
        return template;
    }

    public void setTemplate(DefaultTemplatePnL template) {
        this.template = template;
    }

    public DefaultLayoutGroup getParent() {
        return parent;
    }

    public void setParent(DefaultLayoutGroup parent) {
        this.parent = parent;
    }

    public int getShowtotal() {
        return showtotal;
    }

    public void setShowtotal(int showtotal) {
        this.showtotal = showtotal;
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

    public Set<DefaultLayoutGroup> getChildren() {
        return children;
    }

    public void setChildren(Set<DefaultLayoutGroup> children) {
        this.children = children;
    }

    public boolean isExcludeChildAccountBalances() {
        return excludeChildAccountBalances;
    }

    public void setExcludeChildAccountBalances(boolean excludeChildAccountBalances) {
        this.excludeChildAccountBalances = excludeChildAccountBalances;
    }

}
