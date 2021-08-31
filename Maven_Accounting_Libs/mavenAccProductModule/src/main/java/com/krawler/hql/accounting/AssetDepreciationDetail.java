package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

/**
 *
 * @author krawler
 */
public class AssetDepreciationDetail {

    private String ID;
    private int period;
    private double periodAmount;
    private double accumulatedAmount;
    private double netBookValue;
    private JournalEntry journalEntry;
    private Company company;
    private Account depreciationCreditToAccount;
    private Account depreciationAccount;
    private Product product;
    private AssetDetails assetDetails;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Account getDepreciationAccount() {
        return depreciationAccount;
    }

    public void setDepreciationAccount(Account depreciationAccount) {
        this.depreciationAccount = depreciationAccount;
    }

    public Account getDepreciationCreditToAccount() {
        return depreciationCreditToAccount;
    }

    public void setDepreciationCreditToAccount(Account depreciationCreditToAccount) {
        this.depreciationCreditToAccount = depreciationCreditToAccount;
    }

    public double getAccumulatedAmount() {
        return accumulatedAmount;
    }

    public void setAccumulatedAmount(double accumulatedAmount) {
        this.accumulatedAmount = accumulatedAmount;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public JournalEntry getJournalEntry() {
        return journalEntry;
    }

    public void setJournalEntry(JournalEntry journalEntry) {
        this.journalEntry = journalEntry;
    }

    public double getNetBookValue() {
        return netBookValue;
    }

    public void setNetBookValue(double netBookValue) {
        this.netBookValue = netBookValue;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public double getPeriodAmount() {
        return periodAmount;
    }

    public void setPeriodAmount(double periodAmount) {
        this.periodAmount = periodAmount;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public AssetDetails getAssetDetails() {
        return assetDetails;
    }

    public void setAssetDetails(AssetDetails assetDetails) {
        this.assetDetails = assetDetails;
    }
}
