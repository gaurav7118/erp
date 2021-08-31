/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 * ERP-32829 
 * @param {type} parentObj  = Scope of Transaction form
 * @param {type} grid  = Scope of Grid
 * @param {type} productIds  = If request from Grid events
 * @returns {undefined}
 * @description : Function used to calculate Indian GST based on product and Entity, Dimensions
 */
function processGSTRequest(parentObj, grid, productIds, extraparams) {
    /**
     * 
     * @param {type} parentObj
     * @param {type} grid
     * @param {type} productIds
     * @returns {undefined}
     * Create Array for product Ids
     */
    /**
     * If NO need to apply GST for vendor and customer 
     */ parentObj.isSEZIGST =false;
    if (parentObj != undefined && parentObj.uniqueCase === Wtf.GSTCustVenStatus.NOGST) {
        /**
         * Should not be return from here becuase need to tag class for each product
         */
//        return;
    } else if (parentObj.uniqueCase == Wtf.GSTCustVenStatus.APPLYGSTONDATE) {
        /**
         * 
         * @type @exp;parentObj@pro;transactiondateforgst
         * If Customer type is SEZ then it should be within date range then Apply IGST 
         * If ouyt of date range then apply Normal GST rule calculation
         */
        var transactiondate = parentObj.transactiondateforgst;
        var sezfromdate = parentObj.sezfromdate;
        var seztodate = parentObj.seztodate;
        if (transactiondate >= sezfromdate && transactiondate <= seztodate) {
            parentObj.isSEZIGST=true;
        }
    }
    if (productIds != undefined && productIds != "") {
        productIds += ",";
        getLineTermDetailsAndCalculateGST(parentObj, grid, productIds, extraparams);
    } else {
        productIds = "";
        var array = grid.store.data.items;
        /**
         * Last record not considered because last record is blank row present but in VQ to CQ linking case before add blank row Process GST Request to aplly tax rule,
         * So need to check all reords, and added if condition for "productid" blank if last record is blank row in grid.
         */
        for (var i = 0; i < array.length; i++) {
            var gridRecord = grid.store.getAt(i);
            if (gridRecord.data.productid != '') {
                productIds += gridRecord.data.productid + ",";
            }

        }
        getLineTermDetailsAndCalculateGST(parentObj, grid, productIds, extraparams);
    }


}
/**
 * 
 * @param {type} parentObj
 * @param {type} grid
 * @param {type} productIds
 * @returns {undefined}
 * @Desc : Function to get term details for Products
 */
function getLineTermDetailsAndCalculateGST(parentObj, grid, productIds, extraparams) {
    /**
     * code for New GST 
     */

    var dimArr = parentObj.tagsFieldset.createGSTDimensionArray();
    
    /*While PR is linked in any transaction & Editing that particular transaction at line level then product Taxes are not getting applied properly due to below reason
     * While editing transaction we need "billing state of vendor" as a parameter
     * but we create PR without a vendor So at the time of calculating GST parameter state is not found
     * That's why  "Wtf.gstDimArray" is global variable which is used for the purpose of parameter & calculated earlier
     * Added Undefined check
     */
//    if (parentObj.fromLinkCombo!=undefined && parentObj.fromLinkCombo.getValue() == 5 && Wtf.gstDimArray != "") {
//        dimArr =  Wtf.gstDimArray;
//    }
    var rcmApplicable = undefined;
    if (parentObj != undefined && parentObj.GTAApplicable != undefined && parentObj.GTAApplicable.getValue()) {
        rcmApplicable = parentObj.GTAApplicable.getValue();
    }
    /**
     * Send is Merchant Exporter check while getting GST rule 
     * for INDIA country only
     */
    var isMerchantExporter = false;
    if (WtfGlobal.isIndiaCountryAndGSTApplied() && parentObj != undefined && parentObj.isMerchantExporter != undefined && parentObj.isMerchantExporter.getValue()) {
        isMerchantExporter = parentObj.isMerchantExporter.getValue();
    }
    dimArr = JSON.stringify(dimArr)
    Wtf.Ajax.requestEx({
        url: "AccEntityGST/getGSTForProduct.do",
        params: {
            productids: productIds,
            transactiondate: WtfGlobal.convertToGenericDate(grid.billDate),
            termSalesOrPurchaseCheck: (rcmApplicable != undefined && grid.isCustomer != undefined && !grid.isCustomer) ? true : ((grid.isCustomer != undefined) ? grid.isCustomer : true),
            dimArr: dimArr,
            isFixedAsset: grid.isFixedAsset != undefined ? grid.isFixedAsset : false,
            uniqueCase:parentObj.uniqueCase,
            isSEZIGST : parentObj.isSEZIGST,
            isMerchantExporter : isMerchantExporter

        }
    }, this, function(response) {
        /**
         * 
         * @type @exp;response@pro;data@pro;prodTermArray
         * Return all products term data
         */
        var isProductCategoryIdNone = false;
        var prodTermArray = response.data.prodTermArray;
        for (var index = 0; index < prodTermArray.length; index++) {
            /**
             * 
             * @type Array|@exp;grid@call;calculateTermLevelTaxesInclusive|@exp;grid@call;
             * calculateTermLevelTaxesIterate all products
             */

            var productid = prodTermArray[index].productid;
            /**
             * Check if Product Tax Class not tageed to product
             */
            if(prodTermArray[index].isIdNone !=undefined && prodTermArray[index].isIdNone == 1){
                isProductCategoryIdNone =true;
            }
            /*
             * 
             * @type @arr;prodTermArray@pro;RCM
             * If RCM is enabled then product having tax class Non-Gst,Nil rated,exempted can-not be selected ERP-39127
             */
            var RCM = prodTermArray[index].RCM;
            var record = WtfGlobal.searchRecord(grid.store, productid, 'productid');
            if (rcmApplicable == true && RCM != undefined && record != undefined && RCM == false) {
                grid.store.remove(record);     
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.gst.RCMdisabled")], 2);
            }                
            /**
             * Merchant Export rates will be applicable on taxable goods only. 
             * So no need to consider NIL, Exemted & Non–GST goods under Merchant Export invoice.
             * Provide one prompt whenever User select NIL, Exempted & Non–GST product if 'Is Merchant Exporter' ON. 
             */
            if (WtfGlobal.isIndiaCountryAndGSTApplied() && isMerchantExporter == true && RCM != undefined && record != undefined && RCM == false) {
                grid.store.remove(record);     
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.gst.isMerchantExporter.exemptedproduct.txt")], 2);
            }                
            /**
             * 
             * @type @exp;grid@pro;store@call;
             * find get Grid Record for product
             */
            var array = grid.store.data.items;
            if (array.length > 1) {
                for (var i = 0; i < array.length - 1; i++) {
                    var gridRecord = grid.store.getAt(i);
                    if (gridRecord.data.productid == productid) {
                        var termStore = [];
                        var rec = gridRecord;

//            var result = grid.store.find('productid', productid);
//            var rec = grid.store.getAt(result);

//            /**
//             * 
//             * @type @arr;prodTermArray@pro;LineTermdetails|@arr;prodTermArray@pro;LineTermdetails
//              Get Line Term details for product
//             */
//            var productComboIndex = WtfGlobal.searchRecordIndex(grid.productComboStore, productid, 'productid');
//            if(productComboIndex >=0){
//                var prorec = grid.productComboStore.getAt(productComboIndex);
//                
//                
//            }

                        /**
                         * 
                         * @type @arr;prodTermArray@pro;LineTermdetails
                         * Get Line Term details for product
                         */
                        var applyGstInLease=applyGSTInLease(parentObj, grid, rec, termStore);//Flag for apply GST in Lease (apply GST on service product)
                        var LineTermdetails = prodTermArray[index].LineTermdetails;
                        var ignoreHistory = true;
                        if ((rec.data.taxclasshistoryid != undefined && rec.data.taxclasshistoryid != "") && parentObj.ignoreHistory == false) {
                            /**
                             * If product tax class history present and user havent changed customer/vendor then calculation should proceed with history
                             * example : edit case without changing customer.
                             */
                            ignoreHistory = false;
                        }
                        if (ignoreHistory) {
                            /**
                             * 
                             * @type @arr;prodTermArray@pro;taxclass|String
                             * Set Tax class for product
                             */
                            var taxclass = prodTermArray[index].taxclass != undefined ? prodTermArray[index].taxclass : "";
                            rec.set('taxclass', taxclass);
                        }                        
                        if (parentObj != undefined && (parentObj.uniqueCase === Wtf.GSTCustVenStatus.NOGST || (grid.isLeaseFixedAsset != undefined && grid.isLeaseFixedAsset && !applyGstInLease))) {
                            /**
                             * If Product is under NO GST/Exempt/WOPAY
                             */
                            rec.set('LineTermdetails', []);
                            calculateTaxes(parentObj, grid, rec, termStore);
                            fireUpdateForRecalculations(grid);                            
                        } else {

                            if (ignoreHistory) {
                                /**
                                 * Proceed when to apply new product tax class.
                                 */
                                LineTermdetails = eval(LineTermdetails);
                                for (var gst = 0; gst < LineTermdetails.length; gst++) {
                                    termStore.push(LineTermdetails[gst]);
                                }
                            } else {
                                /**
                                 * Proceed with history
                                 */
                                termStore = eval(rec.data.LineTermdetails)
                            }
                            calculateTaxes(parentObj, grid, rec, termStore);
                            /**
                             * Calculate Tax and Total amount
                             */
                            rec.set('LineTermdetails', Wtf.encode(termStore));
                            fireUpdateForRecalculations(grid);
                        }
                    }
                }
            }
        }
        /**
        * Check if Product Tax Class not tageed to product
        */
        if (WtfGlobal.GSTApplicableForCompany()== Wtf.GSTStatus.NEW && isProductCategoryIdNone && extraparams != undefined && extraparams.isProductIDSelect != undefined && extraparams.isProductIDSelect) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.gst.product.category.none")], 2);
        }
    }, function() {

    });
}
function calculateTaxes(parentObj, grid, rec, termStore) {
    if (parentObj && parentObj.includingGST && parentObj.includingGST.getValue() == true) {
        grid.getColumnModel().setRenderer(grid.getColumnModel().findColumnIndex("amount"), WtfGlobal.withoutRateCurrencySymbol);
        termStore = grid.calculateTermLevelTaxesInclusive(termStore, rec);
    } else {
        grid.getColumnModel().setRenderer(grid.getColumnModel().findColumnIndex("amount"), grid.calAmountWithoutExchangeRate.createDelegate(grid));       
        if (grid.isFixedAsset || grid.isLeaseFixedAsset) {
            termStore = calculateTermLevelTaxes(termStore, rec, undefined, true);
        } else {
            termStore = grid.calculateTermLevelTaxes(termStore, rec, undefined, true);
        }
    }
}
/*
 * 
 * @param {type} parentObj
 * @param {type} grid
 * @param {type} rec
 * @param {type} termStore
 * @returns {Boolean}
 * Calculate Flag for GST calculation in Lease Module i.e. (Apply GST only on product type = 'service')
 */
function applyGSTInLease(parentObj, grid, rec, termStore) {
    if (grid.isGST) {          
        var applyGstInLease=false;
        applyGstInLease = (grid.isLeaseFixedAsset != undefined && grid.isLeaseFixedAsset && rec.data.producttype == Wtf.producttype.service) ? true : false;
        return applyGstInLease;
    }
}
function fireUpdateForRecalculations(grid) {
    if (grid.moduleid && (grid.moduleid == Wtf.Acc_Delivery_Order_ModuleId || grid.moduleid == Wtf.Acc_Sales_Return_ModuleId
            || grid.moduleid == Wtf.Acc_Goods_Receipt_ModuleId || grid.moduleid == Wtf.Acc_Purchase_Return_ModuleId || grid.isFixedAsset ||grid.moduleid == Wtf.Acc_Lease_Quotation||grid.moduleid ==Wtf.Acc_Lease_DO||grid.moduleid == Wtf.Acc_Lease_Return||grid.moduleid==Wtf.Acc_Lease_Contract||grid.moduleid==Wtf.Acc_Lease_Order||grid.moduleid==Wtf.LEASE_INVOICE_MODULEID)) {
        updateTermDetails(grid);
    } else {
        grid.updateTermDetails();
    }

    grid.fireEvent('datachanged', this);
}
function getLineTermDetailsAndCalculateGSTForAdvance(parentObj, grid, productIds) {
    /**
     * code for New GST 
     */

    var dimArr = parentObj.tagsFieldset.createGSTDimensionArray();
    dimArr = JSON.stringify(dimArr);
    var rcmApplicable = undefined;
    if (parentObj != undefined && parentObj.rcmApplicable != undefined && parentObj.rcmApplicable.getValue()) {
        rcmApplicable = parentObj.rcmApplicable.getValue();
    }
    Wtf.Ajax.requestEx({
        url: "AccEntityGST/getGSTForProduct.do",
        params: {
            productids: productIds,
            transactiondate: parentObj.creationDate==undefined ? WtfGlobal.convertToGenericDate(new Date()) : WtfGlobal.convertToGenericDate(parentObj.creationDate.getValue()),
            termSalesOrPurchaseCheck: (rcmApplicable != undefined && grid.isCustomer != undefined && !grid.isCustomer) ? true : ((grid.isCustomer != undefined) ? grid.isCustomer : (parentObj.customerFlag != undefined ? parentObj.customerFlag : true)),
            dimArr: dimArr,
            uniqueCase:parentObj.uniqueCase

        }
    }, this, function(response) {
        /**
         * 
         * @type @exp;response@pro;data@pro;prodTermArray
         * Return all products term data
         */
        var prodTermArray = response.data.prodTermArray;
        for (var index = 0; index < prodTermArray.length; index++) {
            /**
             * 
             * @type Array|@exp;grid@call;calculateTermLevelTaxesInclusive|@exp;grid@call;
             * calculateTermLevelTaxesIterate all products
             */

            var productid = prodTermArray[index].productid; 
            /*
             * 
             * @type @arr;prodTermArray@pro;RCM 
             * Cannot select product having Tax class Nil Rated,exempted,non-gst when RCM check is enabled
             */
            var RCM = prodTermArray[index].RCM;
            var record = WtfGlobal.searchRecord(grid.store, productid, 'productid');
            if (rcmApplicable == true && RCM != undefined && record != undefined && RCM == false) {
                grid.store.remove(record);     
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.gst.RCMdisabled")], 2);
            }   
            /**
             * 
             * @type @exp;grid@pro;store@call;
             * find get Grid Record for product
             */
            var array = grid.store.data.items;
            if (array.length > 1) {
                for (var i = 0; i < array.length - 1; i++) {
                    var gridRecord = grid.store.getAt(i);
                        var termStore = [];
                        var rec = gridRecord;
                    if (rec.data != undefined && (rec.data.productid == undefined || rec.data.productid == "")) {
                        continue;
                    }
                    if (gridRecord.data.productid != productid) {
                        continue;
                    }//


                        /**
                             * 
                             * @type @arr;prodTermArray@pro;LineTermdetails
                             * Get Line Term details for product
                             */
                        var LineTermdetails = prodTermArray[index].LineTermdetails;
                        var ignoreHistory = true;
                        if ((rec.data.taxclasshistoryid != undefined && rec.data.taxclasshistoryid != "") && parentObj.ignoreHistory == false) {
                            /**
                             * If product tax class history present and user havent changed customer/vendor then calculation should proceed with history
                             * example : edit case without changing customer.
                             */
                            ignoreHistory = false;
                        }

                        if (ignoreHistory) {
                            var taxclass = prodTermArray[index].taxclass != undefined ? prodTermArray[index].taxclass : "";
                            rec.set('taxclass', taxclass);
                        }
                        if (parentObj != undefined && parentObj.uniqueCase === Wtf.GSTCustVenStatus.NOGST) {
                            /**
                             * If Product is under NO GST/Exempt/WOPAY
                             */
                            rec.set('LineTermdetails', []);
                            updateTermDetails(grid);
                            grid.fireEvent('datachanged', this);
                        } else {
                        if (ignoreHistory) {
                            LineTermdetails = eval(LineTermdetails);
                            for (var gst = 0; gst < LineTermdetails.length; gst++) {
                                termStore.push(LineTermdetails[gst]);

                            }
                        } else {
                            /**
                             * Proceed with history
                             */
                            termStore = eval(rec.data.LineTermdetails)
                        }


                        termStore = calculateTermLevelTaxesForPayment(termStore, rec, undefined, true);

                        /**
                         * Calculate Tax and Total amount
                         */
                        rec.set('LineTermdetails', Wtf.encode(termStore));
                        updateTermDetails(grid);
                        grid.fireEvent('datachanged', this);
                    }
                }
            }
        }
    }, function() {

        });
}
/**
 * 
 * @param {type} tagsFieldset
 * @param {type} currentAddressDetailrec
 * @param {type} mappingRec
 * @Desc : function to autopopulate dimension value based on address field
 * @returns {undefined}
 */
function populateGSTDimensionValues(obj) {

    var isShipping = obj.isShipping != undefined ? obj.isShipping : false;
    /*----Calculating taxes on vendor billing address in case of Dropship document----- */
    var isdropshipDocument = (obj.currentAddressDetailrec !=undefined && obj.currentAddressDetailrec.isdropshipDocument) ? obj.currentAddressDetailrec.isdropshipDocument : false;
    if (obj != undefined && obj.mappingRec != undefined && obj.currentAddressDetailrec != undefined) {

        for (var index = 0; index < obj.mappingRec.length; index++) {
        //    var addreesField = "State";//obj.mappingRec.addreesField;
        //    var dimField = "State";//obj.mappingRec.dimField;
            var addreesField = obj.mappingRec[index].addreesField;
            var dimField = obj.mappingRec[index].dimField;
            if (addreesField == "State") {
                var addressVal = '';
                if (obj.isCustomer) {
                    addressVal = isShipping ? obj.currentAddressDetailrec.shippingState : obj.currentAddressDetailrec.billingState;
                } else {
                    //var addressVal = isdropshipDocument ? obj.currentAddressDetailrec.dropshipbillingState : (isShipping ? obj.currentAddressDetailrec.shippingState : obj.currentAddressDetailrec.billingState);                     
                        if(isShipping){
                            /**
                             * If Vendor Document created and Show Vendor address off then get vendcustShippingState
                             * else shippingState
                             */
                            if (!obj.isCustomer && !Wtf.account.companyAccountPref.isAddressFromVendorMaster) {
                                if(isdropshipDocument){
                                    addressVal = obj.currentAddressDetailrec.dropshipbillingState;
                                }else{
                                    addressVal = obj.currentAddressDetailrec.vendorShippingState;
                                }
                            } else {
                                addressVal = obj.currentAddressDetailrec.shippingState;
                            }
                        }else{
                            /**
                             * If Show Vendor Address in Purchase document is off then Vendor billing address shown separete fieldset
                             * with separete key , set State value from this key 
                             */
                           if(WtfGlobal.isIndiaCountryAndGSTApplied() && !obj.isCustomer && !Wtf.account.companyAccountPref.isAddressFromVendorMaster){
                              addressVal = obj.currentAddressDetailrec.vendorbillingStateForINDIA;
                           }else {
                              addressVal = obj.currentAddressDetailrec.billingState;
                           }
                        }
                }
                if (WtfGlobal.isUSCountryAndGSTApplied() && !obj.isCustomer && (addressVal == "" || addressVal == undefined)) {
                    
                    /*
                     * Here I have reset Dimension values in tagsfieldset, so if State, City, County Values are not provided, Tax should not be applied.
                     * Please refer SDP-12716
                     */
                    Wtf.getCmp("Custom_" + dimField + obj.tagsFieldset.id).reset();
                }
                                              
                if (addressVal != undefined  && Wtf.getCmp("Custom_" + dimField + obj.tagsFieldset.id) != undefined) {
                    var dimstore = Wtf.getCmp("Custom_" + dimField + obj.tagsFieldset.id).store;
        //            var result = dimstore.find('name', addressVal);
        //            var rec = dimstore.getAt(result);
                    var rec = WtfGlobal.searchRecordIsWithCase(dimstore, addressVal, 'name',false); // Seacrh Record with case in-sensative - ERP-34294
                    if (rec == "" || rec == undefined) {
                        if(!(!obj.isCustomer && Wtf.account.companyAccountPref.countryid == Wtf.Country.US )&& obj.stateAsComboFlag==true) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.field.invalidaddressdetails")], 3);
                        }
                        return true;
                    } else {
                        var dimid = rec.data.id;
                        Wtf.getCmp("Custom_" + dimField + obj.tagsFieldset.id).setValue(dimid);
                    }

                } else {
                    if(!(!obj.isCustomer && Wtf.account.companyAccountPref.countryid == Wtf.Country.US )&& obj.stateAsComboFlag==true) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.field.invalidaddressdetails")], 3);
                    }
                    return true;
                }
            } else if (addreesField == "City") {
                var addressVal = "";
                if (obj.isCustomer) {
                    addressVal = isShipping ? obj.currentAddressDetailrec.shippingCity : obj.currentAddressDetailrec.billingCity;
                } else {
                    //var addressVal = isdropshipDocument ? obj.currentAddressDetailrec.dropshipbillingCity:(isShipping ? obj.currentAddressDetailrec.shippingCity : obj.currentAddressDetailrec.billingCity);
                    if (isShipping) {
                        /**
                         * If Vendor Document created and Show Vendor address off then get vendcustShippingCity 
                         * else shippingCity
                         */
                        if (!obj.isCustomer && !Wtf.account.companyAccountPref.isAddressFromVendorMaster) {
                               if(isdropshipDocument){
                                    addressVal = obj.currentAddressDetailrec.dropshipbillingCity;
                                }else{
                                    addressVal = obj.currentAddressDetailrec.vendorShippingCity;
                                }
                        } else {
                            addressVal = obj.currentAddressDetailrec.shippingCity;
                        }
                    } else {
                        addressVal = obj.currentAddressDetailrec.billingCity;
                    }
                }
                
                if (WtfGlobal.isUSCountryAndGSTApplied() && !obj.isCustomer && (addressVal == "" || addressVal == undefined)) {                    
                    /*
                     * Here I have reset Dimension values in tagsfieldset, so if State, City, County Values are not provided, Tax should not be applied.
                     * Please refer SDP-12716
                     */
                    Wtf.getCmp("Custom_" + dimField + obj.tagsFieldset.id).reset();
                }
                
                if (addressVal != undefined && addressVal != "" && Wtf.getCmp("Custom_" + dimField + obj.tagsFieldset.id) != undefined) {
                    var dimstore = Wtf.getCmp("Custom_" + dimField + obj.tagsFieldset.id).store;
        //            var result = dimstore.find('name', addressVal);
        //            var rec = dimstore.getAt(result);
                    var rec = WtfGlobal.searchRecordIsWithCase(dimstore, addressVal, 'name',false); // Seacrh Record with case in-sensative - ERP-34294
                    if (rec == "" || rec == undefined) {
                        if(!(!obj.isCustomer && Wtf.account.companyAccountPref.countryid == Wtf.Country.US)) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.field.invalidaddressdetails")], 3);
                        }
                        return true;
                    } else {
                        var dimid = rec.data.id;
                        Wtf.getCmp("Custom_" + dimField + obj.tagsFieldset.id).setValue(dimid);
                    }
                } else {
                    if(!(!obj.isCustomer && Wtf.account.companyAccountPref.countryid == Wtf.Country.US)) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.field.invalidaddressdetails")], 3);
                    }
                    return true;
                }
            } else if (addreesField == "County") {
                var addressVal = "";
                if (obj.isCustomer) {
                    addressVal = isShipping ? obj.currentAddressDetailrec.shippingCounty : obj.currentAddressDetailrec.billingCounty;
                } else {
                    //var addressVal = isdropshipDocument ? obj.currentAddressDetailrec.dropshipbillingCounty : (isShipping ? obj.currentAddressDetailrec.shippingCounty : obj.currentAddressDetailrec.billingCounty);
                    if (isShipping) {
                        /**
                         * If Vendor Document created and Show Vendor address off then get vendcustShippingCounty 
                         * else shippingCounty
                         */
                        if (!obj.isCustomer && !Wtf.account.companyAccountPref.isAddressFromVendorMaster) {
                                if(isdropshipDocument){
                                    addressVal = obj.currentAddressDetailrec.dropshipbillingCounty;
                                }else{
                                    addressVal = obj.currentAddressDetailrec.vendorShippingCounty;
                                }
                        } else {
                            addressVal = obj.currentAddressDetailrec.shippingCounty;
                        }
                    } else {
                        addressVal = obj.currentAddressDetailrec.billingCounty;
                    }
                }
                
                if (WtfGlobal.isUSCountryAndGSTApplied() && !obj.isCustomer && (addressVal == "" || addressVal == undefined)) {
                    /*
                     * Here I have reset Dimension values in tagsfieldset, so if State, City, County Values are not provided, Tax should not be applied.
                     * Please refer SDP-12716
                     */
                    Wtf.getCmp("Custom_" + dimField + obj.tagsFieldset.id).reset();
                }
                
                if (addressVal != undefined && addressVal != "" && Wtf.getCmp("Custom_" + dimField + obj.tagsFieldset.id) != undefined) {
                    var dimstore = Wtf.getCmp("Custom_" + dimField + obj.tagsFieldset.id).store;
        //            var result = dimstore.find('name', addressVal);
        //            var rec = dimstore.getAt(result);
                    var rec = WtfGlobal.searchRecordIsWithCase(dimstore, addressVal, 'name',false); // Seacrh Record with case in-sensative - ERP-34294
                    if (rec == "" || rec == undefined) {
                        if(!(!obj.isCustomer && Wtf.account.companyAccountPref.countryid == Wtf.Country.US)) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.field.invalidaddressdetails")], 3);
                        }
                        return true;
                    } else {
                        var dimid = rec.data.id;
                        Wtf.getCmp("Custom_" + dimField + obj.tagsFieldset.id).setValue(dimid);
                    }
                } else {
                    if(!(!obj.isCustomer && Wtf.account.companyAccountPref.countryid == Wtf.Country.US)) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.field.invalidaddressdetails")], 3);
                    }
                    return true;
                }
            }

        }
    }
}
function calculateTermLevelTaxesInclusive(termStore, rec, index) {
//        var unitPriceIncludingTax = rec.data.amount;
    var finaltaxamount = 0;
    var FinalAmountNonTaxableTerm = 0;
    var finaltermStore = new Array();

    var quantity = rec.data.quantity;
    quantity = (quantity == "NaN" || quantity == undefined || quantity == null) ? 0 : quantity;
    var rateIncludingGst = getRoundofValueWithValues(rec.data.rateIncludingGst, Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
    quantity = getRoundofValue(rec.data.quantity);

    var quantityAndAmount = 0;
    quantityAndAmount = rateIncludingGst * quantity;
    // SDP-4111 Calculate Inclusive Discount Amount At line level India .
    if (rec.data.prdiscount > 0) {
        var prdiscount = getRoundedAmountValue(rec.data.prdiscount);
        if (rec.data['discountispercent'] == 1) {
            quantityAndAmount = quantityAndAmount - ((quantityAndAmount * prdiscount) / 100);
        } else {
            quantityAndAmount = quantityAndAmount - prdiscount;
        }
    }
    var unitPriceIncludingTax = quantityAndAmount;


    sortArrOfObjectsByParam(termStore, "termsequence", false);
    // Iterate List of Terms in Reverse Order
    for (var i = 0; i < termStore.length; i++) {
//        for(var i=termStore.length-1; i>=0; i--){
        var termJson = termStore[i];
        var taxamount = 0;

        // Apply Tax on Asessable Value
        if (termJson.taxtype == 2) { // If Flat
            taxamount = termJson.termamount;
        } else if (termJson.taxtype == 1) { // If percentage
            taxamount = getRoundedAmountValue(unitPriceIncludingTax * termJson.taxvalue / (termJson.taxvalue + 100));// assessablevalue * termJson.taxvalue / 100;
        }

        unitPriceIncludingTax = unitPriceIncludingTax - taxamount;

        termJson.termamount = getRoundedAmountValue(taxamount);
        termJson.assessablevalue = getRoundedAmountValue(unitPriceIncludingTax);

        if (termJson.termtype == Wtf.term.Others && (termJson.IsOtherTermTaxable != undefined && !termJson.IsOtherTermTaxable)) {
            //we are first rounding taxamount & then sum it , because it causes 0.1 difference in Total Amount.
            FinalAmountNonTaxableTerm += getRoundedAmountValue(taxamount);
        } else {
            finaltaxamount += taxamount;
        }
        finaltermStore.push(termJson);
    }

    sortArrOfObjectsByParam(finaltermStore, "termsequence", true);
    if (finaltaxamount) {
        rec.set('recTermAmount', getRoundedAmountValue(finaltaxamount));
        rec.set('taxamount', getRoundedAmountValue(finaltaxamount));
        rec.set('amount', getRoundedAmountValue(quantityAndAmount - finaltaxamount));
        rec.set('amountwithouttax', getRoundedAmountValue(quantityAndAmount - finaltaxamount));
    }
    if (FinalAmountNonTaxableTerm) {
        rec.set('OtherTermNonTaxableAmount', getRoundedAmountValue(FinalAmountNonTaxableTerm));
    }

    return finaltermStore;
}
function calculateTermLevelTaxes(termStore, rec, index, isNewProduct) {

    var quantity = rec.data.quantity;
    quantity = (quantity == "NaN" || quantity == undefined || quantity == null) ? 0 : quantity;
    var rate = getRoundofValueWithValues(rec.data.rate, Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
    quantity = getRoundofValue(rec.data.quantity);

    var amount = 0;
    amount = rate * quantity;

    var discount = 0;//origionalAmount*rec.data.prdiscount/100   
    if (rec.data.prdiscount > 0) {
        var prdiscount = getRoundedAmountValue(rec.data.prdiscount);
        if (rec.data.discountispercent == 1) {
            discount = getRoundedAmountValue((amount * prdiscount) / 100);
        } else {
            discount = prdiscount;
        }
        amount -= discount;
    }


    var finaltaxamount = 0;
    var FinalAmountNonTaxableTerm = 0;
    if (index == undefined) {
        index = 0;
    }
    var finaltermStore = new Array();
//        var uncheckedTerms = this.getUncheckedTermDetails(rec);
    var totalVatTax = 0.0;
    var vatFound = false;
    var termSearchTerm = "";
    var prorec = "";

    // Iterate List of Terms
    for (var i = index; i < termStore.length; i++) { // Do not change a single character from this line 
        var termJson = termStore[i];

        var taxamount = 0;
        var assessablevalue = amount;

        var formula = termJson.formulaids.split(",");
        // Loop for Formula of Term
        for (var cnt = 0; cnt < formula.length; cnt++) {
            if (formula[cnt] != 'Basic') {
                var result = finaltermStore.filter(function(termJ) {
                    return termJ.termid == formula[cnt];
                })[0];
                if (result == undefined) {
                    var tempJson = this.calculateTermLevelTaxes(termStore, rec, i + 1);

                    result = tempJson.filter(function(chain) {
                        return chain.termid == formula[cnt];
                    })[0];
                }
                if (result != undefined && result.termamount != undefined) {
                    assessablevalue = assessablevalue + result.termamount;
                }
            }
        }

        if (termJson.deductionorabatementpercent) {
            if (termJson.termtype == Wtf.term.Service_Tax && termJson.taxtype == 1) { //1 for percentage
                var termpercentage = termJson.originalTermPercentage - ((termJson.originalTermPercentage * termJson.deductionorabatementpercent) / 100);
                termJson.taxvalue = termpercentage;
            } else {
                assessablevalue = (100 - termJson.deductionorabatementpercent) * assessablevalue / 100; //As tax will apply on amount excluding abatement.
            }
        } else if (rec.objField != undefined && rec.objField == 'deductionorabatementpercent') { //  for term tax value if abatement reset to 0(zero)
            if (termJson.termtype == Wtf.term.Service_Tax && termJson.taxtype == 1) { // For service tax  && 1 for percentage
                termJson.taxvalue = termJson.originalTermPercentage;
            }
        }
        // Apply Tax on Asessable Value
        if (termJson.taxtype == 2) { // If Flat
            taxamount = termJson.termamount;
        } else if (termJson.taxtype == 1) { // If percentage
            var taxamountfun = assessablevalue * termJson.taxvalue / 100;
            var opmod = termJson.sign == 0 ? -1 : 1;
            taxamountfun = opmod * taxamountfun;
            taxamount = taxamountfun;
        }

        termJson.termamount = getRoundedAmountValue(taxamount);
        termJson.assessablevalue = getRoundedAmountValue(assessablevalue);

        finaltaxamount += taxamount;

        rec.set('amount', getRoundedAmountValue(amount));
        if (finaltaxamount) {
            rec.set('recTermAmount', getRoundedAmountValue(finaltaxamount));
            rec.set('taxamount', getRoundedAmountValue(finaltaxamount));
        } else {
            rec.set('recTermAmount', getRoundedAmountValue(0));
            rec.set('taxamount', getRoundedAmountValue(0));
        }
        finaltermStore.push(termJson);
    }
    if (termStore.length < 1) {
        rec.set('amount', getRoundedAmountValue(amount));
        rec.set('recTermAmount', getRoundedAmountValue(0));
        rec.set('taxamount', getRoundedAmountValue(0));
    }
    if (finaltaxamount) {
        rec.set('recTermAmount', getRoundedAmountValue(finaltaxamount));
    }
    return finaltermStore;
}
function calculateTermLevelTaxesForPayment(termStore, rec, index, isNewProduct) {

    var amount = 0.0;
    var isForCNDN = false;
    if (rec.data.enteramount !== undefined) {
        amount = getRoundofValueWithValues(rec.data.enteramount, Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
    } else if (rec.data.dramount !== undefined) {
        isForCNDN = true;
        amount = getRoundofValueWithValues(rec.data.dramount, Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
    }

    var finaltaxamount = 0;
    var FinalAmountNonTaxableTerm = 0;
    if (index == undefined) {
        index = 0;
    }
    var finaltermStore = new Array();
    //        var uncheckedTerms = this.getUncheckedTermDetails(rec);
    var totalVatTax = 0.0;
    var vatFound = false;
    var termSearchTerm = "";
    var prorec = "";

    // Iterate List of Terms
    for (var i = index; i < termStore.length; i++) { // Do not change a single character from this line 
        var termJson = termStore[i];

        var taxamount = 0;
        var assessablevalue = amount;

        var formula = termJson.formulaids.split(",");
        // Loop for Formula of Term
        for (var cnt = 0; cnt < formula.length; cnt++) {
            if (formula[cnt] != 'Basic') {
                var result = finaltermStore.filter(function(termJ) {
                    return termJ.termid == formula[cnt];
                })[0];
                if (result == undefined) {
                    var tempJson = this.calculateTermLevelTaxes(termStore, rec, i + 1);

                    result = tempJson.filter(function(chain) {
                        return chain.termid == formula[cnt];
                    })[0];
                }
                if (result != undefined && result.termamount != undefined) {
                    assessablevalue = assessablevalue + result.termamount;
                }
            }
        }

        if (termJson.deductionorabatementpercent) {
            if (termJson.termtype == Wtf.term.Service_Tax && termJson.taxtype == 1) { //1 for percentage
                var termpercentage = termJson.originalTermPercentage - ((termJson.originalTermPercentage * termJson.deductionorabatementpercent) / 100);
                termJson.taxvalue = termpercentage;
            } else {
                assessablevalue = (100 - termJson.deductionorabatementpercent) * assessablevalue / 100; //As tax will apply on amount excluding abatement.
            }
        } else if (rec.objField != undefined && rec.objField == 'deductionorabatementpercent') { //  for term tax value if abatement reset to 0(zero)
            if (termJson.termtype == Wtf.term.Service_Tax && termJson.taxtype == 1) { // For service tax  && 1 for percentage
                termJson.taxvalue = termJson.originalTermPercentage;
            }
        }
        // Apply Tax on Asessable Value
        if (termJson.taxtype == 2) { // If Flat
            taxamount = termJson.termamount;
        } else if (termJson.taxtype == 1) { // If percentage
            var taxamountfun = assessablevalue * termJson.taxvalue / 100;
            var opmod = termJson.sign == 0 ? -1 : 1;
            taxamountfun = opmod * taxamountfun;
            taxamount = taxamountfun;
        }

        termJson.termamount = getRoundedAmountValue(taxamount);
        termJson.assessablevalue = getRoundedAmountValue(assessablevalue);

        finaltaxamount += taxamount;

        rec.set('amount', getRoundedAmountValue(amount));
        if (finaltaxamount) {
            rec.set('recTermAmount', getRoundedAmountValue(finaltaxamount));
            if (isForCNDN) {
                rec.set('amountwithtax', getRoundedAmountValue(amount + finaltaxamount));
            } else {
                rec.set('taxamount', getRoundedAmountValue(finaltaxamount));
            }
        } else {
            rec.set('recTermAmount', getRoundedAmountValue(0));
            rec.set('taxamount', getRoundedAmountValue(0));
        }
        finaltermStore.push(termJson);
    }
    if (termStore.length < 1) {
        rec.set('amount', getRoundedAmountValue(amount));
        rec.set('recTermAmount', getRoundedAmountValue(0));
        rec.set('taxamount', getRoundedAmountValue(0));
    }
    if (finaltaxamount) {
        rec.set('recTermAmount', getRoundedAmountValue(finaltaxamount));
    }
    return finaltermStore;
}
/**
 * Validate Customer and vendor GST Details if not then show Alert , This alert only for India Country
 * @param {type} params
 * @returns {undefined}
 */
function checkAndAlertCustomerVendor_GSTDetails(params) {
    var isGSTHistoryPresent = true;
    if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && params.rec != undefined && params.rec.data != undefined) {
        var data = params.rec.data;
        var msg = '';
        var customerVendorText = params.isCustomer ? " Customer." : " Vendor.";
        if (data.GSTINRegistrationTypeId == undefined || data.GSTINRegistrationTypeId == '') {
            msg += '<b>' + WtfGlobal.getLocaleText('acc.masterConfig.62') + '</b><br/>'; // GST Registration Type
        }
        /**
         * If customer/ Vendor GST Registration type is Unregistered/ Consumer then not need to check GSTIN/ UIN number 
         */
        if (data.GSTINRegTypeDefaultMstrID!=undefined && data.GSTINRegTypeDefaultMstrID!='' && data.GSTINRegTypeDefaultMstrID!= Wtf.GSTRegMasterDefaultID.Unregistered && data.GSTINRegTypeDefaultMstrID!= Wtf.GSTRegMasterDefaultID.Consumer && (data.gstin == undefined || data.gstin == '')) {
            msg += '<b>' + WtfGlobal.getLocaleText('acc.india.vecdorcustomer.column.gstin') + '</b><br/>'; // GSTIN/ UIN number 
        }
        if (data.CustomerVendorTypeId == undefined || data.CustomerVendorTypeId == '') {
            if (params.isCustomer) {
                msg += '<b>' + WtfGlobal.getLocaleText('acc.customer.GST.type') + '</b><br/>'; // Customer Type
            } else {
                msg += '<b>' + WtfGlobal.getLocaleText('acc.vendor.GST.type') + '</b><br/>'; // Vendor Type
            }
        } else if (data.uniqueCase == Wtf.GSTCustVenStatus.APPLYGSTONDATE && ((data.seztodate == undefined || data.seztodate == '') || (data.sezfromdate == undefined || data.sezfromdate == ''))) {
            msg += '<b>' + WtfGlobal.getLocaleText('acc.GST.sezFromDate') + '</b> And ' + '<b>' + WtfGlobal.getLocaleText('acc.GST.sezToDate') + '</b><br/>'; // SEZ From and To Date
        }
        if (msg != '') {
            isGSTHistoryPresent = false;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.gst.gstdetail.missing.customervendor") + customerVendorText + '<br/>' + msg], 2);
        }
    }
    return isGSTHistoryPresent;
}
/**
 * //ERP-34970(ERM-534)
 * Check Selected Customer Is valid to add RCM Transaction
 * @param {type} scopeObj
 * @param {type} extraparams
 * @returns {undefined}
 */
function isRCMValidCustomer(scopeObj, extraparams) {
    var isValid =true;
    if (scopeObj.GTAApplicable && extraparams && extraparams.record && extraparams.record['data']) {
        var data = extraparams.record['data']
        if (data['GSTINRegTypeDefaultMstrID'] == Wtf.GSTRegMasterDefaultID.Unregistered || data['gstin'] == '') {
            scopeObj.GTAApplicable.setValue(false);
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.common.rcm.transaction.notapplicableforcustomer")], 2);
            isValid =false;
        }
    }
    return isValid;
}
/**
 * Calculate CESS amount on Type given in Rule Setup
 * @param {type} params
 * @param {type} termJson
 * @param {type} taxamount
 * @returns {unresolved}
 */
function calculateCESSONTypeAndValuationAmount(params, quantity, assessablevalue, termJson, taxamount) {
    var returnArray = new Array();
    var isCESSApply = true;
    if (termJson != undefined && termJson[Wtf.GST_CESS_TYPE] != undefined && termJson[Wtf.GST_CESS_TYPE] != '') {
        var CessTermTypeID = termJson[Wtf.GST_CESS_TYPE];
        var CESSValuationAmount = termJson[Wtf.GST_CESS_VALUATION_AMOUNT] != undefined && termJson[Wtf.GST_CESS_VALUATION_AMOUNT] != '' ? termJson[Wtf.GST_CESS_VALUATION_AMOUNT] : 0.0;
        if (CessTermTypeID == Wtf.CESSTYPE.NOT_APPLICABLE) {
            taxamount = 0.0;
            isCESSApply =false;
        } else if (CessTermTypeID == Wtf.CESSTYPE.PERCENTAGES) {
            taxamount = assessablevalue * termJson.taxvalue / 100;
        } else if (CessTermTypeID == Wtf.CESSTYPE.HIGHER_VALUE_OR_CESSPERCENTAGES) {
            var percentagesAmount = assessablevalue * termJson.taxvalue / 100;
            var valuationAmount = ((quantity * CESSValuationAmount) / 1000);
            if (percentagesAmount >= valuationAmount) {
                taxamount = percentagesAmount;
            } else {
                taxamount = valuationAmount
            }
        } else if (CessTermTypeID == Wtf.CESSTYPE.VALUE_AND_CESSPERCENTAGES) {
            var percentagesAmount = assessablevalue * termJson.taxvalue / 100;
            var valuationAmount = ((quantity * CESSValuationAmount) / 1000);
            taxamount = percentagesAmount + valuationAmount;
        } else if (CessTermTypeID == Wtf.CESSTYPE.VALUE) {
            var valuationAmount = ((quantity * CESSValuationAmount) / 1000);
            taxamount = valuationAmount;
        }
    }
    returnArray[0] = taxamount;
    returnArray[1] = isCESSApply;
    return returnArray;
}
/**
 * Function to get Linked document date
 * @param {type} parentObj
 * @param {type} grid
 * @returns {getLinkDateTocheckGSTDataOnDateCase}
 * 
 */
function getLinkDateTocheckGSTDataOnDateCase(parentObj, grid) {
    var billid = parentObj.PO.getValue();
    var selectedValuesArr = billid.split(',');
    if (selectedValuesArr.length == 1) {
        var record = parentObj.POStore.getAt(parentObj.POStore.find('billid', billid));
        if (record != undefined && record != '') {
            this.linkdocdate = record.data['date']
        }else
        {
            this.linkdocdate = parentObj.POdate;
        }
        checkGSTDataOnDateCase(parentObj, grid, this.linkdocdate);
    } else {
        parentObj.keepTermDataInLinkCase=true;
        parentObj.applyGSTFieldsBasedOnDate();
    }
}
/**
 * Function to check GST history for the Customer/Vendor and product Tax class 
 * based on transaction date and link document date. Also update GST rates accordingly
 * @param {type} parentObj
 * @param {type} grid
 * @returns {checkGSTDataOnLinkingCase}
 * 
 */
function checkGSTDataOnDateCase(parentObj, grid, datebeforechanged) {

    /**
     * 
     * @type @exp;parentObj@pro;POStore@call;getAtIf single select
     */

    var productids = "";
    var array = grid.store.data.items;
    for (var i = 0; i < array.length - 1; i++) {
        var gridRecord = grid.store.getAt(i);
        productids += "'" + gridRecord.data.productid + "',";

    }
    Wtf.Ajax.requestEx({
        url: "AccEntityGST/getGSTFieldsChangedStatus.do",
        params: {
            masterid: parentObj.Name.getValue(),
            isCustomer: grid.isCustomer,
            productids: productids,
            applydate: WtfGlobal.convertToGenericDate(datebeforechanged),
            transactiondate: WtfGlobal.convertToGenericDate(parentObj.billDate.getValue())

        }
    }, this, function(response) {
        if (response.success && response.isdatachanged && response.isdatachanged == true) {
            if(Wtf.getCmp('gstCalculationConfirmWinid')){
                Wtf.getCmp('gstCalculationConfirmWinid').close();
            }
            new Wtf.gstCalculationConfirmWin({
                id: 'gstCalculationConfirmWinid',
                parentObj: parentObj,
                grid: grid,
                linkdocdate: datebeforechanged
            }).show();
        } else {
            parentObj.keepTermDataInLinkCase=true;
            parentObj.applyGSTFieldsBasedOnDate();
        }
    });
}


Wtf.gstCalculationConfirmWin = function(config) {
    Wtf.apply(this, config);
    Wtf.gstCalculationConfirmWin.superclass.constructor.call(this, {
        buttons: [{
                text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //"Save",
                scope: this,
                handler: function() {
                    this.parentObj.checkgststatus = true;
                    var oldvalue = this.parentObj.billDate.getValue();
                    var newval = this.fromDate.getValue();
                    this.parentObj.billDate.setValue(newval);
                    this.parentObj.keepTermDataInLinkCase=false;
                    this.parentObj.onDateChange(undefined, newval, oldvalue);
                    this.close();
                }
            }, {
                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"), //"Cancel",
                scope: this,
                handler: function() {
                    this.parentObj.PO.clearValue();
                    this.close();
                }
            }]
    });
}

Wtf.extend(Wtf.gstCalculationConfirmWin, Wtf.Window, {
    layout: "border",
    modal: true,
    title: WtfGlobal.getLocaleText("acc.gsthistory.promptwin"), 
    id: 'gstCalculationConfirmWinid',
    width: 550,
    closable:true,
    height: 275,
    resizable: false,
    iconCls: "pwnd favwinIcon",
    initComponent: function() {
        Wtf.gstCalculationConfirmWin.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetAgstCalculationForm();
        Wtf.form.Field.prototype.msgTarget = 'side';
        this.add(this.northPanel);
        this.add(this.gstCalculationForm);
    },
    GetNorthPanel: function() {
        this.northPanel = new Wtf.Panel({
            region: "north",
            height: 125,
            border: false,
            bodyStyle: "background:white;border-bottom:1px solid #bfbfbf;",
            html: getTopHtml(this.parentObj.isEdit?WtfGlobal.getLocaleText("acc.gsthistory.promptwindescedit"):WtfGlobal.getLocaleText("acc.gsthistory.promptwindesclink"), WtfGlobal.getLocaleText("acc.gsthistory.reasons"), '../../images/createuser.png', false, '0px 0px 0px 0px')
        });
    },
    GetAgstCalculationForm: function() {
        this.fromDate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.gsthistory.confirmdate")+"*", 
            name: 'fromdate',
            format: WtfGlobal.getOnlyDateFormat(),
//            value:Wtf.serverDate,
            id: "fromdateid",
            width: 160,
            allowBlank: false
        });

        this.gstCalculationForm = new Wtf.form.FormPanel({
            region: "center",
            border: false,
            bodyStyle: "background-color:#f1f1f1;padding:20px",
            items: [{
                    layout: 'column',
                    border: false,
                    items: [{
                            layout: 'form',
                            columnWidth: 0.80,
                            border: false,
                            labelWidth: 200,
                            items: [this.fromDate]
                        }]
                }]
        });
        this.fromDate.setValue(this.parentObj.billDate.getValue());
    }
});
/*
 * Function to calculate only taxes according to existing Terms
 */
function calculateUpdatedTaxes(parentObj, grid, rec) {
    var termStore = [];
    var LineTermdetails = rec.data.LineTermdetails;
    LineTermdetails = eval(LineTermdetails);
    for (var gst = 0; gst < LineTermdetails.length; gst++) {
        termStore.push(LineTermdetails[gst]);

    }
    if (parentObj && parentObj.includingGST && parentObj.includingGST.getValue() == true) {
        grid.getColumnModel().setRenderer(grid.getColumnModel().findColumnIndex("amount"), WtfGlobal.withoutRateCurrencySymbol);
        termStore = grid.calculateTermLevelTaxesInclusive(termStore, rec);
    } else {
        grid.getColumnModel().setRenderer(grid.getColumnModel().findColumnIndex("amount"), grid.calAmountWithoutExchangeRate.createDelegate(grid));
        if (grid.isFixedAsset) {
            termStore = calculateTermLevelTaxes(termStore, rec, undefined, true);
        } else {
            termStore = grid.calculateTermLevelTaxes(termStore, rec, undefined, true);
        }
    }
    rec.set('LineTermdetails',Wtf.encode(termStore));
}
/**
 * Shows alert if GST detail not present on document Transaction date
 * @param {type} response
 * @param {type} thisObj
 * @returns {undefined}
 */
function isGSTDetailsPresnetOnTransactionDate(response, thisObj, Grid, Name) {
    /**
     * If GST Details not present on current Transaction date then show alert to user 
     *  set GST details for Cusrrent date or Change transaction date
     */
    if (response.success) {
        if (response[Wtf.IS_GST_HISTORY_PRESENT] != undefined && !response[Wtf.IS_GST_HISTORY_PRESENT]) {
            var personName = '';
            if(Name){
               personName =  Name.getRawValue();
            }
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText({key: thisObj.isCustomer ? "acc.gsthistory.notpresent.customer" : "acc.gsthistory.notpresent.vendor", params: [personName]})], 2);
            if (Name) {
                Name.reset();
            }
            if (Grid) {
                Grid.getStore().removeAll();
                if (thisObj.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || thisObj.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId || thisObj.moduleid == Wtf.Acc_Make_Payment_ModuleId || thisObj.moduleid == Wtf.Acc_Receive_Payment_ModuleId) {
                    updateTermDetails(Grid);
                }
                Grid.fireEvent('datachanged', Grid);
            }
            return;
        }
    }
}
/**
 * Validate GST dimension values present or Not
 * for example - check State and Entity dimension values presnet or not in document creation 
 * @param {type} parentObj
 * @param {type} grid
 * @param {type} rec
 * @returns {Boolean}
 */
function isGSTDimensionValuePresent(parentObj, grid) {
    var isGSTDimValuePresent = true;
    if (parentObj != undefined && parentObj.CustVenTypeDefaultMstrID!=undefined) {
        if (parentObj.CustVenTypeDefaultMstrID != Wtf.GSTCUSTVENTYPE.Export
                && parentObj.CustVenTypeDefaultMstrID != Wtf.GSTCUSTVENTYPE.ExportWOPAY
                && parentObj.CustVenTypeDefaultMstrID != Wtf.GSTCUSTVENTYPE.Import) {
            if (!parentObj.tagsFieldset.checkGSTDimensionValues(parentObj)) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.save.document.GSTdimension.notpresnet")],2);
                isGSTDimValuePresent = false;
            }
        }
    }
    return isGSTDimValuePresent;
}
/**
 * ERP-39257
 * Show alert on Save document if GST details not presnet 
 * @param {type} parentObj
 * @returns {Boolean.isGSTHistoryPresent}
 */
function isGSTHistoryPresentOnDocumentCreation(parentObj) {
    var isGSTHistoryPresent = true;
    if (parentObj != undefined) {
        var cust_Vendparams = {};
        var record = {};
        var data = {};
        data.CustomerVendorTypeId = parentObj.CustomerVendorTypeId;
        data.GSTINRegistrationTypeId = parentObj.GSTINRegistrationTypeId;
        data.gstin = parentObj.gstin;
        data.GSTINRegTypeDefaultMstrID = parentObj.GSTINRegTypeDefaultMstrID;
        data.uniqueCase = parentObj.uniqueCase;
        record.data= data;
        cust_Vendparams.rec = record;
        cust_Vendparams.isCustomer = parentObj.isCustomer;
        isGSTHistoryPresent = checkAndAlertCustomerVendor_GSTDetails(cust_Vendparams);
    }
    return isGSTHistoryPresent;
}
/**
 * 
 * ERP-35464
 * @param {type} store
 * @returns {undefined}
 */
function removeGSTDetailsNotUsedFromStore(store, isCustomerVendorType, isCustomer) {
    if (store != undefined && isCustomerVendorType != undefined) {
        store.each(function (record) {
            var recdata = record.data;
            if (isCustomerVendorType) {// call from CustomerVendorTypeStore load
                //Specific Hide Show GST Customer Vendor Type
                if (isCustomer && (recdata.defaultMasterItem == Wtf.GSTCUSTVENTYPE.Import)) {
                    store.remove(record);
                } else if (!isCustomer && (recdata.defaultMasterItem == Wtf.GSTCUSTVENTYPE.Export 
                        || recdata.defaultMasterItem == Wtf.GSTCUSTVENTYPE.ExportWOPAY
                        || recdata.defaultMasterItem == Wtf.GSTCUSTVENTYPE.SEZWOPAY)) {
                    store.remove(record);
                }
                // Hide GST Customer Vendor Type not used 
                if(recdata.defaultMasterItem == Wtf.GSTCUSTVENTYPE.DEEMED_EXPORT){
                    store.remove(record);
                }
            } else if (!isCustomerVendorType) { // call from GSTINRegistrationTypeStore load
                // Hide GST Registration Type not used 
                if (recdata.defaultMasterItem == Wtf.GSTRegMasterDefaultID.Consumer 
                        || recdata.defaultMasterItem == Wtf.GSTRegMasterDefaultID.Composition_ECommerce
                        || recdata.defaultMasterItem == Wtf.GSTRegMasterDefaultID.Regular_ECommerce) {
                    store.remove(record);
                }
            }
        }, this);
    }
}
/**
 * This function call on GST detail change (transaction date Changed  / Or Customer/Vedor Addresses changed)
 * This function called from "applyGSTFieldsBasedOnDate"
 * @param {type} parentObj
 * @returns {undefined}
 */
function onGSTDetailsChangeValidateMerchantExporterApplicability(parentObj) {
    if (WtfGlobal.isIndiaCountryAndGSTApplied() && parentObj != undefined && (Wtf.isMerchantExporterVisible.indexOf(parseInt(parentObj.moduleid)) > -1)
            && parentObj.isMerchantExporter != undefined && parentObj.isMerchantExporter.getValue()) {
        /**
         * If customer/Vendor GST Registration Type is 'Registered' and Customer/Vendor type is 'NA'
         * then only allow to create merchant exporter transaction
         */
        if (!(parentObj.CustVenTypeDefaultMstrID != undefined && parentObj.GSTINRegTypeDefaultMstrID != undefined
                && parentObj.CustVenTypeDefaultMstrID == Wtf.GSTCUSTVENTYPE.NA
                && parentObj.GSTINRegTypeDefaultMstrID == Wtf.GSTRegMasterDefaultID.Regular)) {
            parentObj.isMerchantExporter.setValue(false);
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), parentObj.isCustomer ? WtfGlobal.getLocaleText("acc.gstrr.isMerchantExporter.error.customer") : WtfGlobal.getLocaleText("acc.gstrr.isMerchantExporter.error.vendor")], 2);
        }
    }
}