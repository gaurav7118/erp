package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import java.util.Set;

public class FixedAssetOpeningMappingWithAssetDetail {

    private String id;
    private FixedAssetOpening assetOpening;
    private AssetDetails assetDetails;
    private Company company;

    public AssetDetails getAssetDetails() {
        return assetDetails;
    }

    public void setAssetDetails(AssetDetails assetDetails) {
        this.assetDetails = assetDetails;
    }

    public FixedAssetOpening getAssetOpening() {
        return assetOpening;
    }

    public void setAssetOpening(FixedAssetOpening assetOpening) {
        this.assetOpening = assetOpening;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
