/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

/**
 *
 * @author krawler
 */
public class DisposeRevertAssets {
    private String ID;
    private AssetDetails asset;
    private Company company;
    private JournalEntry disposalJE;
    private JournalEntry reverseJE;
    private boolean isreverted;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public AssetDetails getAsset() {
        return asset;
    }

    public void setAsset(AssetDetails asset) {
        this.asset = asset;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public JournalEntry getDisposalJE() {
        return disposalJE;
    }

    public void setDisposalJE(JournalEntry disposalJE) {
        this.disposalJE = disposalJE;
    }

    public boolean isIsreverted() {
        return isreverted;
    }

    public void setIsreverted(boolean isreverted) {
        this.isreverted = isreverted;
    }

    public JournalEntry getReverseJE() {
        return reverseJE;
    }

    public void setReverseJE(JournalEntry reverseJE) {
        this.reverseJE = reverseJE;
    }
}
