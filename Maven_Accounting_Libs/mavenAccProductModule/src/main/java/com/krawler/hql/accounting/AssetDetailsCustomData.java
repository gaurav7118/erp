/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.AccCustomData;

/**
 *
 * @author krawler
 */
public class AssetDetailsCustomData extends AccCustomData {

    private String assetDetailsId;
    private AssetDetails assetDetails;
       private String moduleId;

    public String getAssetDetailsId() {
        return assetDetailsId;
    }

    public void setAssetDetailsId(String assetDetailsId) {
        this.assetDetailsId = assetDetailsId;
    }

    public AssetDetails getAssetDetails() {
        return assetDetails;
    }

    public void setAssetDetails(AssetDetails assetDetails) {
        this.assetDetails = assetDetails;
    }
 

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }


}
