package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

/**
 *
 * @author krawler
 */
public class AssetPurchaseRequisitionDetailMapping {

    private String ID;
    private String purchaseRequisitionDetailID; // id of Purchase RequisitionDetail
    private int moduleId; // moduleid of Asset Purchase RequisitionDetail
    private PurchaseRequisitionAssetDetails purchaseRequisitionAssetDetails;
    private Company company;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getPurchaseRequisitionDetailID() {
        return purchaseRequisitionDetailID;
    }

    public void setPurchaseRequisitionDetailID(String purchaseRequisitionDetailID) {
        this.purchaseRequisitionDetailID = purchaseRequisitionDetailID;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public PurchaseRequisitionAssetDetails getPurchaseRequisitionAssetDetails() {
        return purchaseRequisitionAssetDetails;
    }

    public void setPurchaseRequisitionAssetDetails(PurchaseRequisitionAssetDetails purchaseRequisitionAssetDetails) {
        this.purchaseRequisitionAssetDetails = purchaseRequisitionAssetDetails;
    }
}