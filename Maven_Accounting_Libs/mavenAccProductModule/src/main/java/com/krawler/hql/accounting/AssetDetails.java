package com.krawler.hql.accounting;

import com.krawler.common.admin.*;
import static com.krawler.common.util.Constants.Acc_Invoice_ModuleId;
import static com.krawler.common.util.Constants.moduleID_NameMap;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author krawler
 */
public class AssetDetails {

    private String id;
    private String assetId;
    private InventoryLocation location;
    private Department department;
    private User assetUser;
    private double cost;
    private double costInForeignCurrency;
    private double salvageRate;
    private double salvageValue;
    private double salvageValueInForeignCurrency;
    private double accumulatedDepreciation;
//    private double wdv;        // ERP-16629: WDV field should be optional during asset creation 
    private double assetLife;
    private double elapsedLife;
    private double nominalValue;
    private double sellAmount;
    private Date installationDate;
//    private boolean assetSold;
    private Product product;
    private Company company;
    private boolean invrecord;
    private boolean createdFromOpeningForm;// is asset is created from opening form
    private JournalEntry sellingJE;
    private boolean linkedToLeaseSO;// indicates that, Lease Sales Order is created against it or not.
    private boolean leaseDOCreated;// indicates that, Lease Delivery Order is created against it or not.
    private boolean leaseCICreated;// indicates that, Lease Customer Invoice is created against it or not.
    private boolean isUsed;// indicates is asset has been used in case of linking with DO/CI
    private int assetSoldFlag;// == 0 means asset not sold, == 1 means asset has been sold from CI, == 2 means asset has been sold from DO
    private ProductBatch batch;
    private String assetDescription;
    private boolean purchaseReturn;
    private boolean salesReturn;
    private double openingDepreciation;
    private AssetDetailsCustomData assetDetailsCustomData;
    private Date purchaseDate; // to store exact purchase Date
    private int postOption; // used when there is Average method Yearly but user want to post monthly or yearly both way Value = 1 means Yearly, Value = 2 means Monthly
    private boolean isDisposed; // Flag get true once we disposed the Asset
    private Date disposalDate; // Date of Disposal is stored when we dispos the Asset
    private double disposalProfitLoss; // Calculated Profit/Loss Amount on the disposal of Asset
    private double remainingLife; // After disposing the asset remaing life get stored
    private JournalEntry disposalJE; // Reference of Journalentry when we dispose the Asset
    private JournalEntry reverseJE; // Reference of Journalentry when we revert the Disposed Asset
    private Date transferDate;
    private boolean isTransferHistory;
    
    public static final Integer AssetsNotSoldFlag = 0;
    public static final Integer AssetsSoldFromCI = 1;
    public static final Integer AssetsSoldFromDO = 2;
    
    public boolean isIsTransferHistory() {
        return isTransferHistory;
    }

    public void setIsTransferHistory(boolean isTransferHistory) {
        this.isTransferHistory = isTransferHistory;
    }

    public Date getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(Date transferDate) {
        this.transferDate = transferDate;
    }
    
    public Date getDisposalDate() {
        return disposalDate;
    }

    public void setDisposalDate(Date disposalDate) {
        this.disposalDate = disposalDate;
    }

    public double getDisposalProfitLoss() {
        return disposalProfitLoss;
    }

    public void setDisposalProfitLoss(double disposalProfitLoss) {
        this.disposalProfitLoss = disposalProfitLoss;
    }

    public boolean isIsDisposed() {
        return isDisposed;
    }

    public void setIsDisposed(boolean isDisposed) {
        this.isDisposed = isDisposed;
    }

    public double getRemainingLife() {
        return remainingLife;
    }

    public void setRemainingLife(double remainingLife) {
        this.remainingLife = remainingLife;
    }    
    
    public JournalEntry getDisposalJE() {
        return disposalJE;
    }

    public void setDisposalJE(JournalEntry disposalJE) {
        this.disposalJE = disposalJE;
    }
    
    public JournalEntry getReverseJE() {
        return reverseJE;
    }

    public void setReverseJE(JournalEntry reverseJE) {
        this.reverseJE = reverseJE;
    }
    
    public int getPostOption() {
        return postOption;
    }

    public void setPostOption(int postOption) {
        this.postOption = postOption;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public AssetDetailsCustomData getAssetDetailsCustomData() {
        return assetDetailsCustomData;
    }

    public void setAssetDetailsCustomData(AssetDetailsCustomData assetDetailsCustomData) {
        this.assetDetailsCustomData = assetDetailsCustomData;
    }

    public double getOpeningDepreciation() {
        return openingDepreciation;
    }

    public void setOpeningDepreciation(double openingDepreciation) {
        this.openingDepreciation = openingDepreciation;
    }

    public ProductBatch getBatch() {
        return batch;
    }

    public void setBatch(ProductBatch batch) {
        this.batch = batch;
    }

    public double getAccumulatedDepreciation() {
        return accumulatedDepreciation;
    }

    public void setAccumulatedDepreciation(double accumulatedDepreciation) {
        this.accumulatedDepreciation = accumulatedDepreciation;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public double getAssetLife() {
        return assetLife;
    }

    public void setAssetLife(double assetLife) {
        this.assetLife = assetLife;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getElapsedLife() {
        return elapsedLife;
    }

    public void setElapsedLife(double elapsedLife) {
        this.elapsedLife = elapsedLife;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(Date installationDate) {
        this.installationDate = installationDate;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public InventoryLocation getLocation() {
        return location;
    }

    public void setLocation(InventoryLocation location) {
        this.location = location;
    }

    public double getNominalValue() {
        return nominalValue;
    }

    public void setNominalValue(double nominalValue) {
        this.nominalValue = nominalValue;
    }

    public double getSalvageRate() {
        return salvageRate;
    }

    public void setSalvageRate(double salvageRate) {
        this.salvageRate = salvageRate;
    }

    public double getSalvageValue() {
        return salvageValue;
    }

    public void setSalvageValue(double salvageValue) {
        this.salvageValue = salvageValue;
    }

//    public double getWdv() {
//        return wdv;
//    }
//
//    public void setWdv(double wdv) {
//        this.wdv = wdv;
//    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public User getAssetUser() {
        return assetUser;
    }

    public void setAssetUser(User assetUser) {
        this.assetUser = assetUser;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public boolean isInvrecord() {
        return invrecord;
    }

    public void setInvrecord(boolean invrecord) {
        this.invrecord = invrecord;
    }

    public boolean isCreatedFromOpeningForm() {
        return createdFromOpeningForm;
    }

    public void setCreatedFromOpeningForm(boolean createdFromOpeningForm) {
        this.createdFromOpeningForm = createdFromOpeningForm;
    }

    public int getAssetSoldFlag() {
        return assetSoldFlag;
    }

    public void setAssetSoldFlag(int assetSoldFlag) {
        this.assetSoldFlag = assetSoldFlag;
    }

    public boolean isIsUsed() {
        return isUsed;
    }

    public void setIsUsed(boolean isUsed) {
        this.isUsed = isUsed;
    }

    public double getSellAmount() {
        return sellAmount;
    }

    public void setSellAmount(double sellAmount) {
        this.sellAmount = sellAmount;
    }

    public boolean isLeaseDOCreated() {
        return leaseDOCreated;
    }

    public void setLeaseDOCreated(boolean leaseDOCreated) {
        this.leaseDOCreated = leaseDOCreated;
    }

    public boolean isLinkedToLeaseSO() {
        return linkedToLeaseSO;
    }

    public void setLinkedToLeaseSO(boolean linkedToLeaseSO) {
        this.linkedToLeaseSO = linkedToLeaseSO;
    }

    public boolean isLeaseCICreated() {
        return leaseCICreated;
    }

    public void setLeaseCICreated(boolean leaseCICreated) {
        this.leaseCICreated = leaseCICreated;
    }

    public JournalEntry getSellingJE() {
        return sellingJE;
    }

    public void setSellingJE(JournalEntry sellingJE) {
        this.sellingJE = sellingJE;
    }

    public double getCostInForeignCurrency() {
        return costInForeignCurrency;
    }

    public void setCostInForeignCurrency(double costInForeignCurrency) {
        this.costInForeignCurrency = costInForeignCurrency;
    }

    public double getSalvageValueInForeignCurrency() {
        return salvageValueInForeignCurrency;
    }

    public void setSalvageValueInForeignCurrency(double salvageValueInForeignCurrency) {
        this.salvageValueInForeignCurrency = salvageValueInForeignCurrency;
    }
    
    public String getAssetDescription() {
        return assetDescription;
    }

    public void setAssetDescription(String assetDescription) {
        this.assetDescription = assetDescription;
    }

    public boolean isPurchaseReturn() {
        return purchaseReturn;
    }

    public void setPurchaseReturn(boolean purchaseReturn) {
        this.purchaseReturn = purchaseReturn;
    }

    public boolean isSalesReturn() {
        return salesReturn;
    }

    public void setSalesReturn(boolean salesReturn) {
        this.salesReturn = salesReturn;
    }
}
