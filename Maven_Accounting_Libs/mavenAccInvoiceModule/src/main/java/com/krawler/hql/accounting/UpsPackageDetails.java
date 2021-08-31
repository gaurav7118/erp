/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

/**
 *
 * @author krawler
 * In this table, package details are stored for packages created on UPS side.
 * It is used only when UPS Integration is enabled for a sub-domain.
 */
public class UpsPackageDetails {

    private String ID;//UUID, primary key
    private Packing packing;//Packing record
    private int srno;//Serial No.
    private String packageNumber;//Package Number
    private String packagingType;//Packaging Type (code is stored, not the display value)
    private double packageWeight;//Package weight (LBS)
    private String packageDimensions;//Package Dimensions (IN*IN*IN)
    private double declaredValue;//Package's declared value (USD)
    private String deliveryConfirmationType;//delivery Confirmation Type (code is stored, not teh display value)
    private String additionalHandling;//Additiona; Handling Indicator (Yes="1", No="0")
    private String trackingNumber;//Tracking Number for package received from UPS side
    private String shippingLabel;//Shipping Label for package received from UPS side

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Packing getPacking() {
        return packing;
    }

    public void setPacking(Packing packing) {
        this.packing = packing;
    }

    public int getSrno() {
        return srno;
    }

    public void setSrno(int srno) {
        this.srno = srno;
    }

    public String getPackageNumber() {
        return packageNumber;
    }

    public void setPackageNumber(String packageNumber) {
        this.packageNumber = packageNumber;
    }

    public String getPackagingType() {
        return packagingType;
    }

    public void setPackagingType(String packagingType) {
        this.packagingType = packagingType;
    }

    public double getPackageWeight() {
        return packageWeight;
    }

    public void setPackageWeight(double packageWeight) {
        this.packageWeight = packageWeight;
    }

    public String getPackageDimensions() {
        return packageDimensions;
    }

    public void setPackageDimensions(String packageDimensions) {
        this.packageDimensions = packageDimensions;
    }

    public double getDeclaredValue() {
        return declaredValue;
    }

    public void setDeclaredValue(double declaredValue) {
        this.declaredValue = declaredValue;
    }

    public String getDeliveryConfirmationType() {
        return deliveryConfirmationType;
    }

    public void setDeliveryConfirmationType(String deliveryConfirmationType) {
        this.deliveryConfirmationType = deliveryConfirmationType;
    }

    public String getAdditionalHandling() {
        return additionalHandling;
    }

    public void setAdditionalHandling(String additionalHandling) {
        this.additionalHandling = additionalHandling;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getShippingLabel() {
        return shippingLabel;
    }

    public void setShippingLabel(String shippingLabel) {
        this.shippingLabel = shippingLabel;
    }

}
