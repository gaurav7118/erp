/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
Wtf.account.ProductFormGroup = function(config){
    this.editQuantity=false;						// used as flag to save initial quantity while product editing
    this.isEdit=config.isEdit;
    this.isClone=config.isClone;
    this.ispropagatetochildcompanyflag=false;
    this.isFixedAsset = (config.isFixedAsset != undefined)?config.isFixedAsset:false;
    this.isDepreciable = true;
    this.isBatchForProd=false;
    this.WarnMessage=true; /* show warning message if non-expense account is tagged to purchase account*/
    this.isSerialForProd=false;
    this.isSKUForProd=false;
    this.productID=config.productID;
    this.isUsedInTransaction=config.isUsedInTransaction;
    this.allowAssemblyProductToEdit = config.allowAssemblyProductToEdit;
    this.isUsedInBatchSerial=config.isUsedInBatchSerial;
    this.batchDetails="";
    this.idOfCloningProduct=this.isClone?config.record.data.productid:"";//id of selected product to use in clone
//    var recordid="";
    this.modeName = config.modeName;
    Wtf.apply(this,config);
    Wtf.account.ProductFormGroup.superclass.constructor.call(this, config);
    
    this.detailPanel = new Wtf.DetailPanel({
        modulename: "Product",
        keyid:"productid",
        height:200,
        mapid:1,
        id2:this.id,
        moduleID:Wtf.Acc_Product_Master_ModuleId,
        moduleName:"Product",
        mainTab:this.mainTab,
        leadDetailFlag:true,
        moduleScope:this
    });
}

Wtf.extend(Wtf.account.ProductFormGroup, Wtf.account.ClosablePanel, {
    loadRecord:function(){
        this.ActivateAllTab();
        if(this.record!=null){
            if(this.record.data['parentid']){
                this.subproduct.toggleCollapse();
            }
            //            this.ProductFormGroup.getForm().loadRecord(this.record);
            if(!this.isClone){
                this.asOfDate.disable();
            }
            this.asOfDate.setValue(this.record.data.asofdate);
            if(!this.isClone){
//                this.Pname.setValue(this.record.data.productname);
                this.PID.setValue(this.record.data.pid);
                this.ItemBarcode.setValue(this.record.data.barcode);
                this.recordid=this.record.data.productid;
                Wtf.apply(this.detailPanel, {accid:this.recordid});
                Wtf.apply(this.detailPanel, {acccode:this.PID});    
//                this.description.setValue(this.record.data.desc);
//                var prodDesc=this.record.data.desc;
//                prodDesc = prodDesc.replace(/(<([^>]+)>)/ig,"");
//                this.descriptionshow.setValue(prodDesc);
//                this.Supplier.setValue(this.record.json.supplier);
//                this.CoilCraft.setValue(this.record.json.coilcraft);
//                this.InterPlant.setValue(this.record.json.interplant);
            }
            //            else{//clone case           
            //                this.PID.setValue("");
            //            }
                
            if(Wtf.uomStore.getCount()>1){
//                if( Wtf.uomStore.find('uomname','-') == -1) {
//                    var newRec=new Wtf.data.Record({
//                        uomid:'',
//                        uomname:"-"
//                    });
//                    Wtf.uomStore.add(newRec); 
//                }
                this.uom.setValue(this.record.data.uomid);
                if(this.record.data.displayUoMid) {
                    this.displayUOMStore.load({params: {uomschematypeid: this.record.data.uomschematypeid}}, this);
                    this.displayUom.setValue(this.record.data.displayUoMid);
                }                
                this.packingStockUom.setValue(this.record.data.uomid);
            }
                
                if(Wtf.account.companyAccountPref.activateGroupCompaniesFlag){//In case of MultiCompany
                    if(this.record.data.vendor!=undefined && this.record.data.vendor!=null && this.record.data.vendor!=""){
                        this.vendor.setValForRemoteStore(this.record.data.vendor,this.record.data.vendorname);
                    }
                }else  if(Wtf.vendorAccStore.getCount()>1){
                    this.vendor.setValue(this.record.data.vendor);
                }
            
            if(Wtf.salesAccStore.getCount()>0){
                this.salesAcc.setValue(this.record.data.salesaccountid);
                this.salesReturnAcc.setValue(this.record.data.salesretaccountid);
                this.salesRevenueRecognitionAccount.setValue(this.record.data.salesRevenueRecognitionAccountid); 
                this.inputVATSales.setValue(this.record.data.inputVATSales);
                this.cstVATattwoSales.setValue(this.record.data.cstVATattwoSales);
                this.cstVATSales.setValue(this.record.data.cstVATSales);                
            }
            //            if(this.locationStore.getCount()>0){
            //                this.locationEditor.setValue(this.record.data.location);
            //            }
            //            if(this.wareHouseStore.getCount()>0){
            //                this.wareHouseEditor.setValue(this.record.data.warehouse);
            //            }
//            if(this.shelfLocationStore.getCount()>0){
//                this.shelfLocationCombo.setValue(this.record.data.shelfLocationId);
//            }
            if(this.globalProductCount>0){
                this.parentname.setValue(this.record.data.parentuuid);
            }
            this.Pname.setValue(this.record.data.productname);
            this.description.setValue(this.record.data.desc);
            var prodDesc=this.record.data.desc;
            prodDesc = WtfGlobal.replaceText('<br>',prodDesc,'\n');
            prodDesc = prodDesc.replace(/(<([^>]+)>)/ig,"");
            prodDesc = WtfGlobal.replaceText('&nbsp;',prodDesc,' ');
            this.descriptionshow.setValue(prodDesc);
            this.Supplier.setValue(this.record.json.supplier);
            this.CoilCraft.setValue(this.record.json.coilcraft);
            this.InterPlant.setValue(this.record.json.interplant);
            this.reorderLevel.setValue(this.record.data.reorderlevel);
            this.rQuantity.setValue(this.record.data.reorderquantity);
            this.leadtime.setValue(this.record.data.leadtime);
            this.QAleadtime.setValue(this.record.data.QAleadtime);
            this.hsCode.setValue(this.record.data.hsCode);
            this.landingCostCategoryCombo.setValue(this.record.data.landingcostcategoryid);
            if(this.record.data.warrantyperiodsal != 0){
                this.warrantyperiodSal.setValue(this.record.data.warrantyperiodsal);
            }
            if(this.record.data.warrantyperiod != 0){
                this.warrantyperiod.setValue(this.record.data.warrantyperiod);
            }
            if(this.record.data.revenueRecognitionProcess!=null){
                this.revenueRecognitionProcess.setValue(this.record.data.revenueRecognitionProcess);
            }
            //this.cCountInterval.setValue(this.record.data.ccountinterval);
            //this.cCountTolerance.setValue(this.record.data.ccounttolerance);
            this.productWeight.setValue(this.record.data.productweight);
            this.productWeightPerStockUom.setValue(this.record.data.productweightperstockuom);
            this.productWeightIncludingPakagingPerStockUom.setValue(this.record.data.productweightincludingpakagingperstockuom);
            
            this.productVolumePerStockUom.setValue(this.record.data.productvolumeperstockuom);
            this.productVolumeIncludingPakagingPerStockUom.setValue(this.record.data.productvolumeincludingpakagingperstockuom);
            this.updateWeightFieldsLabel();
            if (this.isClone) {
                this.quantity.setValue(0);
            } else {
            this.quantity.setValue(this.record.data.initialquantity);    
            } 
            this.initialprice.setValue(this.record.data.initialprice);
            this.initialsalesprice.setValue(this.record.data.salespriceinpricecurrency);
            this.mrpOfproduct.setValue(this.record.data.mrprate);
            this.syncable.setValue(this.record.data.syncable);
            this.isRecylable.setValue(this.record.data.isRecyclable);
            this.rcmapplicable.setValue(this.record.data.rcmapplicable);
            this.isWastageApplicable.setValue(this.record.data.isWastageApplicable);
            if(!this.isClone){ //on clone case pass batch serial details empty otherwise pass it as it is
                  this.batchDetails=this.batchdetails;
              } 
            this.multiuom.setValue(this.record.data.multiuom);            
            this.blockLooseSell.setValue(this.record.data.blockLooseSell);            
            this.autoAssembly.setValue(this.record.data.autoAssembly);
            if (Wtf.account.companyAccountPref.activateInventoryTab && this.isClone){
                this.isLocationForProduct.setValue(true);
                
           } else{
            this.isLocationForProduct.setValue(this.record.data.isLocationForProduct);
           }
            if (Wtf.account.companyAccountPref.activateInventoryTab && this.isClone){ 
                this.isWarehouseForProduct.setValue(true);
            }else {
            this.isWarehouseForProduct.setValue(this.record.data.isWarehouseForProduct);
            }
            this.isRowForProduct.setValue(this.record.data.isRowForProduct);
            this.isRackForProduct.setValue(this.record.data.isRackForProduct);
            this.isBinForProduct.setValue(this.record.data.isBinForProduct);
            this.isBatchForProduct.setValue(this.record.data.isBatchForProduct);
            this.isSerialForProduct.setValue(this.record.data.isSerialForProduct);
            this.isSKUForProduct.setValue(this.record.data.isSKUForProduct);
            
            if (this.isEdit && this.record.data.initialquantity > 0 && !this.isClone) { // if this is edit case and opening is given for product,then do not allow user to change its following properties,so disabled it.
                this.isLocationForProduct.disable();
                this.isWarehouseForProduct.disable();
                this.isRowForProduct.disable();
                this.isRackForProduct.disable();
                this.isBinForProduct.disable();
                this.isBatchForProduct.disable();
                this.isSerialForProduct.disable();
                this.isSKUForProduct.disable();
            }
            this.qaenable.setValue(this.record.data.qaenable);
            this.isInterval.setValue(this.record.data.intervalfield);
            this.addShipLengthqty.setValue(this.record.data.addshiplentheithqty);
            this.noOfServiceQTY.setValue(this.record.data.noofqty);
            this.minOrderingQuantity.setValue(this.record.data.minorderingquantity);
            this.maxOrderingQuantity.setValue(this.record.data.maxorderingquantity);
            this.addQuantityFields();
             if(this.record.data.barcodefield==Wtf.BarcodeGenerator_SerialId){
                Wtf.getCmp('bserialId'+this.id).setValue(true);
            }else if(this.record.data.barcodefield==Wtf.BarcodeGenerator_ProductId){
                Wtf.getCmp('bprodId'+this.id).setValue(true);
            }else if(this.record.data.barcodefield==Wtf.BarcodeGenerator_Barcode){
                Wtf.getCmp('brcodeId'+this.id).setValue(true);
            }else if(this.record.data.barcodefield==Wtf.BarcodeGenerator_SKUField){
                Wtf.getCmp('bSkuId'+this.id).setValue(true);
            }else if(this.record.data.barcodefield==Wtf.BarcodeGenerator_BatchID){
                Wtf.getCmp('bbatchId'+this.id).setValue(true);
            }
               
                              
                              
            /**************************************************************************
            *        Starting of loading new records
           *************************************************************************** */
                
            //General tab    
//            this.ItemBarcode.setValue(this.record.data.barcode);
            this.AdditionalDesc.setValue(this.record.data.additionaldescription);
            this.DescForeign.setValue(this.record.data.foreigndescription);
            this.ItemGroup.setValue(this.record.data.itemgroup);
            this.ItemPriceList.setValue(this.record.data.itempricelist);
            this.ShippingType.setValue(this.record.data.shippingtype);
            this.ItemActiveStatus.setValue(this.record.data.isActiveItem);
            this.ItemKnittingStatus.setValue(this.record.data.isKnittingItem);
            
            this.itemReusability.setValue(this.record.data.itemReusability);
            if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isitcapplicable && this.isEdit && this.record != undefined) {
                /**
                 * Set value for ITC field if country is india in Edit case
                 */
                    this.itcAccStore.load();
                    this.itcAccStore.on('load', function() {
                       this.itcAccountCombo.setValue(this.record.data.itcaccountid);
                    }, this);
                    this.itcTypeCmbForProduct.setValue(this.record.data.itctype);
            }
            
            this.reusabilityCount.setValue(this.record.data.reusabilitycount);
            if(this.itemReusability.getValue() == 0){
                this.reusabilityCount.setDisabled(false);
            }
            this.substituteProductCombo.setValForRemoteStore(this.record.data.substituteProductId, this.record.data.substituteProductName);
            this.substituteQty.setValue(this.record.data.substituteQty);
            
            this.licensetype.setValue(this.record.data.licensetype);
            this.licensecode.setValue(this.record.data.licensecode);
            this.customerCategoryCombo.setValue(this.record.data.customercategory);
            this.productBrandCombo.setValForRemoteStore(this.record.data.productBrandId, this.record.data.productBrandName);
//            this.inspectionTemplate.setvalue(this.record.data.inspectionTemplate);
            
            // Purchase Tab fields
            this.CatalogNo.setValue(this.record.data.catalogNo);
            this.ItemPurchaseHeight.setValue(this.record.data.itempurchaseheight);
            this.ItemPurchaseWidth.setValue(this.record.data.itempurchasewidth);
            this.ItemPurchaseLength.setValue(this.record.data.itempurchaselength);
            this.ItemPurchaseVolume.setValue(this.record.data.itempurchasevolume);
            this.PurchaseMfg.setValue(this.record.data.purchasemfg);

            // Sales Tab fields
            this.ItemSalesHeight.setValue(this.record.data.itemsalesheight);
            this.ItemSalesWidth.setValue(this.record.data.itemsaleswidth);
            this.ItemSalesLength.setValue(this.record.data.itemsaleslength);
            this.ItemSalesVolume.setValue(this.record.data.itemsalesvolume);
            this.AlternateProducts.setValue(this.record.data.alternateproductid);

            //  Properties Tab fields

            this.ItemHeight.setValue(this.record.data.itemheight);
            this.ItemWidth.setValue(this.record.data.itemwidth);
            this.ItemLength.setValue(this.record.data.itemlength);
            this.ItemVolume.setValue(this.record.data.itemvolume);
            this.ItemColor.setValue(this.record.data.itemcolor);

            //  Remarks Tab fields
                 
            this.AdditionalFreeText.setValue(this.record.data.additionalfreetext);
                    
            //  Inventory Data Tab fields
             
            if(this.record.data.valuationmethod==0){
                this.ValuationMethod0.setValue(true);
            }
            if(this.record.data.valuationmethod==1){
                this.ValuationMethod1.setValue(true);
            }
            if(this.record.data.valuationmethod==2){
                this.ValuationMethod2.setValue(true);
            }

            if(this.record.data.casinguomid !="" && this.record.data.casinguomid !=null){
                this.CasingUoMCombo.setValue(this.record.data.casinguomid);
            }
            if(this.record.data.inneruomid !="" && this.record.data.inneruomid !=null){
                this.InnerUoMCombo.setValue(this.record.data.inneruomid);
            }
            //            if(this.record.data.stockuomid !="" && this.record.data.stockuomid !=null){
            //                this.StockUoMCombo.setValue(this.record.data.stockuomid);
            //            }
            
            if(this.record.data.casinguomvalue !="" && this.record.data.casinguomvalue !=null){
                this.CasingUoMValue.setValue(this.record.data.casinguomvalue);
            }
            if(this.record.data.inneruomvalue !="" && this.record.data.inneruomvalue !=null){
                this.InnerUoMValue.setValue(this.record.data.inneruomvalue);
            }
            if(this.record.data.stockuomvalue !="" && this.record.data.stockuomvalue !=null){
                this.StockUoMValue.setValue(this.record.data.stockuomvalue);
            }
            this.setPackagingValue();
            
             
            if(this.record.data.purchaseuomid !="" && this.record.data.purchaseuomid !=null){
                this.Purchaseuom.setValue(this.record.data.purchaseuomid);       
            }
            if(this.record.data.salesuomid !="" && this.record.data.salesuomid !=null){
                this.Salesuom.setValue(this.record.data.salesuomid);
            }
            
            if(this.record.data.orderinguomid !="" && this.record.data.orderinguomid !=null){
                this.OrderingUoMCombo.setValue(this.record.data.orderinguomid);
            }
            if(this.record.data.transferuomid !="" && this.record.data.transferuomid !=null){
                this.TransferUoMCombo.setValue(this.record.data.transferuomid);
            }
            
            
            if(this.record.data.packaging !="" && this.record.data.packaging !=null){
                this.packaging.setValue(this.record.data.packaging);
            }
            
            if(this.record.data.itemcost !="" && this.record.data.itemcost !=null){
            //  this.ItemCost.setValue(this.record.data.itemcost);
            }
            
            if(this.record.data.location !="" && this.record.data.locations !=null){
                this.locationEditor.setValue(this.record.data.location);
            }
            
            this.WIPOffset.setValue(this.record.data.WIPoffset);
            this.InventoryOffset.setValue(this.record.data.Inventoryoffset);
            this.countable.setValue(this.record.data.countable);
            if(this.record.data.CCFrequency!="" && this.record.data.CCFrequency!=null){
                this.CycleCountFrequencyCombo.setValue(this.record.data.CCFrequency);
            }

            
            /**************************************************************************
                 *
                 *        ending of adding new records
                 ***************************************************************************
                 */
                
                
                
            if(this.isFixedAsset){
                this.depreciationMethodCombo.setValue(this.record.data.depreciationMethod);
                this.depreciationRate.setValue(this.record.data.depreciationRate);
                this.depreciationCostLimit.setValue(this.record.data.depreciationCostLimit);
                this.PNLTypeAccount.setValue(this.record.data.depreciationGLAccount);
                this.balanceSheetTypeAccount.setValue(this.record.data.depreciationProvisionGLAccount);
                this.CheckAssetDepreciationPosting();
                    
                if(this.record.data.depreciationMethod == 3){
                    this.isDepreciable = false;
                    this.PNLTypeAccount.allowBlank = true;
                    this.PNLTypeAccount.disable();

                    this.depreciationRate.allowBlank = true;
                    this.depreciationRate.disable();

                    this.balanceSheetTypeAccount.allowBlank = true;
                    this.balanceSheetTypeAccount.disable();

                    this.depreciationCostLimit.allowBlank = true;
                    this.depreciationCostLimit.disable();
                        
                }else{
                    this.isDepreciable = true;
                    this.PNLTypeAccount.allowBlank = false;
                    this.PNLTypeAccount.enable();

                    this.depreciationRate.allowBlank = false;
                    this.depreciationRate.enable();

                    this.balanceSheetTypeAccount.allowBlank = true;
                    this.balanceSheetTypeAccount.enable();

                    this.depreciationCostLimit.allowBlank = false;
                    this.depreciationCostLimit.enable();
                        
                }
                    
            }
            if(!this.isClone &&!this.isFixedAsset){
                this.getDetails();
            }
        } else {
            this.asOfDate.setValue(Wtf.account.companyAccountPref.firstfyfrom);//refer ticket ERP-18283
            
             if(Wtf.salesAccStore.getCount()>0 && Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA){
                 if(Wtf.salesAccStore.find('accid',Wtf.salesaccountidcompany) != -1){
                    this.salesAcc.setValue(Wtf.salesaccountidcompany);
                 }
                 if(Wtf.salesAccStore.find('accid',Wtf.salesretaccountidcompany) != -1){
                    this.salesReturnAcc.setValue(Wtf.salesretaccountidcompany);
                 }
            }
        }
        if(Wtf.productTypeStore.getCount()>0){
            this.producttype.setValue(this.producttypeval);
            this.changeLayoutWithType();
            if(Wtf.account.companyAccountPref.isDeferredRevenueRecognition!=undefined&&Wtf.account.companyAccountPref.isDeferredRevenueRecognition&&Wtf.account.companyAccountPref.salesAccount!=undefined&&Wtf.account.companyAccountPref.salesRevenueRecognitionAccount!=undefined&&this.record==null){
                this.salesAcc.setValue(Wtf.account.companyAccountPref.salesAccount);
                this.salesReturnAcc.setValue(Wtf.account.companyAccountPref.salesAccount);
                this.salesRevenueRecognitionAccount.setValue(Wtf.account.companyAccountPref.salesRevenueRecognitionAccount);
                this.revenueRecognitionProcess.setValue(Wtf.account.companyAccountPref.isDeferredRevenueRecognition);
                    
            }
                
            if((Wtf.account.companyAccountPref.isDeferredRevenueRecognition!=undefined&&!Wtf.account.companyAccountPref.isDeferredRevenueRecognition)||Wtf.account.companyAccountPref.isDeferredRevenueRecognition==undefined){
                this.hideSalesRevenueAccount(); 
            }else{
                this.showSalesRevenueAccount();
                if(this.salesRevenueRecognitionAccount.getValue()==""&&Wtf.account.companyAccountPref.salesRevenueRecognitionAccount!=undefined){
                    this.salesRevenueRecognitionAccount.setValue(Wtf.account.companyAccountPref.salesRevenueRecognitionAccount);
                }
            }    
        }
    //this.doLayoutAllTab();
    
    },
    
    CheckAssetDepreciationPosting : function(){
        Wtf.Ajax.requestEx({
            url:"ACCProduct/hasAssetDepreciationPostedUnderAssetGroup.do",
            params: {
                productId:this.record.data.productid
            }
        },this,function(response){
            if(response.data.hasAssetDepreciationPostedUnderAssetGroup){
                this.depreciationMethodCombo.disable();
                this.depreciationRate.disable();
                this.saleAssetAccount.disable();
                this.PNLTypeAccount.disable();
                this.balanceSheetTypeAccount.disable();
                this.assetControllingAcc.disable();
            }else{
                this.depreciationMethodCombo.enable();
                this.depreciationRate.enable();
                this.saleAssetAccount.enable();
                this.PNLTypeAccount.enable();
                this.balanceSheetTypeAccount.enable();
                this.assetControllingAcc.enable();
            }
        },function(response){
                    
            });
    },
    
    onRender: function(config){
        Wtf.account.ProductFormGroup.superclass.onRender.call(this, config);
        this.isClosable=false; 
        this.producttypeval = (this.record!=null?this.record.data.producttype:Wtf.producttype.invpart);
        this.globalProductCount = Wtf.productStore.getCount();
        this.createStore();
        this.createFields();
        this.createToggleFields();
        this.createAssemblyGrid();
        // For creating global level form of BOM for MRP module
        this.createGlobalBOMForm();
        // For creating grid which contains all alternate BOM saved
        this.createGridOfBOMSaved();
        // for creating west panel for Assembly Product(Only for MRP module)
        this.createWestTreePanel();
        // for creating field set of BOM of assembly product
        this.createBOMFieldSet();
        
        //Quality Control
        //Create quality control bottons
        this.createQualityControlButtons();
        //Create fields for quality control
        this.createQualityControlFields();
        //Create form panel for quality control
        this.createQualityControlForm();
        //Create grid for quality control
        this.createQualityControlGrid();
        //Create field set for quality control
        this.createQualityControlFieldSet();
        
        this.createForm();
        if (Wtf.account.companyAccountPref.inventoryValuationType == Wtf.PERPETUAL_VALUATION_METHOD) {
            this.setDefaultPerpetualAccount();
        }

        this.GeneralTab= new Wtf.Panel({
            width:450,
            frame:true,
            height:500,
            title:WtfGlobal.getLocaleText("acc.product.General"),
            autoScroll:true,
            items:[
                this.ProductFormGroup,
                {
                    region: 'center',
                    border: false,
                    layout:"fit",
                    disabledClass:"newtripcmbss",
                    disabled: (!Wtf.isEmpty(this.allowAssemblyProductToEdit) && this.allowAssemblyProductToEdit) ? true : false,
                    items: [this.bomFieldset]
                }
            ]
        }); 
        
        this.PurchaseTab= new Wtf.Panel({
            width:450,
            frame:true,
            height:500,
            title:WtfGlobal.getLocaleText("acc.product.Purchase"),
            autoScroll:true,
            items:this.PurchaseForm
        }); 
        
    if(Wtf.account.companyAccountPref.activateGroupCompaniesFlag){//In case of MultiCompany
        this.PurchaseTab.on('activate', function() {
            if (this.vendor != undefined) {
                this.doLayout();
                this.vendor.syncSize();
                this.vendor.setWidth(295);
            }
        }, this);    
    }    
        this.SalesTab= new Wtf.Panel({
            width:450,
            frame:true,
            height:500,
            title:WtfGlobal.getLocaleText("acc.product.Sales"),
            autoScroll:true,
            items: this.SalesForm
        }); 
        
        this.PropertiesTab= new Wtf.Panel({
            width:450,
            frame:true,
            height:500,
            title:WtfGlobal.getLocaleText("acc.product.Properties"),
            autoScroll:true,
            items:[
                this.PropertiesForm,
                {
                    region: 'center',
                    border: false,
                    layout:"fit",
                    disabledClass:"newtripcmbss",
                    disabled: (!Wtf.isEmpty(this.allowAssemblyProductToEdit) && this.allowAssemblyProductToEdit) ? true : false,
                    items: [
                        this.qualityControlFieldSet
                    ]
                }
            ]
        }); 
        
        this.RemarksTab= new Wtf.Panel({
            width:450,
            frame:true,
            height:500,
            title:WtfGlobal.getLocaleText("acc.product.Remarks"),
            items:[this.RemarksForm]
        }); 
        
        this.InventoryTab= new Wtf.Panel({
            width:450,
            frame:true,
            autoScroll:true,
//            height:500,
            title:WtfGlobal.getLocaleText("acc.product.InventoryData"),
            items:this.InventoryDataForm
        }); 
        
        this.InventoryTab.on("render",this.setPackagingValue,this);
        
        this.saveBttn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
            toolTip: WtfGlobal.getLocaleText("acc.rem.175"),
            id: "save" + this.heplmodeid+ this.id,
            scope: this,
            iconCls: 'pwnd save',
            handler:function(){
                this.saveOnlyFlag = true;
                this.saveAndCreateNewFlag = false;
                 this.confirmBeforeSave();
            }
        });
        this.savencreateBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.SaveAndCreateNew"),
            tooltip:WtfGlobal.getLocaleText("acc.field.SaveAndCreateNewProductToolTip"),
            id:"savencreate"+config.heplmodeid+this.id,
            scope:this,
            handler:function(){
                this.saveOnlyFlag = false;
                this.saveAndCreateNewFlag = true;
                this.confirmBeforeSave();
            },
            iconCls :'pwnd save'
     });
        var buttonArray = new Array();
        buttonArray.push(this.saveBttn);
        if(!this.isEdit && !this.isClone){
            buttonArray.push(this.savencreateBttn);
        }
        this.newPanel=new Wtf.TabPanel({
            autoScroll:true,
            bodyStyle:' background: none repeat scroll 0 0 #DFE8F6;',
            region : 'center',
            scope:this,
            closable:true,
            items:Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA?[ 
            this.GeneralTab,
            this.PurchaseTab,
            this.SalesTab,
            this.PropertiesTab,
            this.RemarksTab,
            this.InventoryTab,
            //this.TaxInfoTab
            ]:[ 
            this.GeneralTab,
            this.PurchaseTab,
            this.SalesTab,
            this.PropertiesTab,
            this.RemarksTab,
            this.InventoryTab
            ] ,
            bbar:buttonArray
        //            bbar:[{
        //                text:WtfGlobal.getLocaleText("acc.common.saveBtn"),
        //                scope:this,
        //                iconCls :getButtonIconCls(Wtf.etype.save),
        //                handler: this.saveForm.createDelegate(this)
        //                        },{
        //                             text: 'Cancel',
        //                            scope: this,
        //                            iconCls :getButtonIconCls(Wtf.etype.close),
        //                            handler:this.closeForm.createDelegate(this)
        //            }]
        });
       
        this.southPanel=new Wtf.Panel({
            region:'south',
            height:250,
            title:WtfGlobal.getLocaleText("acc.editors.otherdetailregion"),//'Other Details',
            hidden:!Wtf.account.companyAccountPref.viewDetailsPerm,
            collapsible:true,
            collapsed : true,
            collapsibletitle : WtfGlobal.getLocaleText("acc.editors.otherdetailregion"),//'Other Details',
            plugins: new Wtf.ux.collapsedPanelTitlePlugin(),
            layout: "fit",
            split : true,
            items:[this.detailPanel],
            listeners:{
                'expand':this.getDetails,
                scope:this
            }
        });        
        if(this.isEdit && !this.isClone &&! this.isFixedAsset){
            this.add(this.newPanel,this.southPanel);
        }
        else{
            this.add(this.newPanel);
        }
        this.tagsFieldset.on("add",function(){
            if(Wtf.getCmp("as")){
                Wtf.getCmp("as").doLayout();
            }
            if (WtfGlobal.isIndiaCountryAndGSTApplied() && (this.isEdit && !this.isClone)) {
                this.disableProductTaxClass();
            }             
        },this)
                
        this.ProductFormGroup.doLayout();
        this.GeneralTab.doLayout();
        
        this.newPanel.doLayout();
         
        this.add(this.newPanel);
        this.newPanel.setActiveTab(this.GeneralTab);
        
        this.GeneralTab.on('activate',function(){
            this.doLayoutAllTab();
        },this);
        
        this.PurchaseTab.on('activate',function(){
            this.doLayoutAllTab();
        },this);
        
        this.SalesTab.on('activate',function(){
            this.doLayoutAllTab(); 
        },this);
        
        this.PropertiesTab.on('activate', function () {
            if (this.producttype.getValue() == Wtf.producttype.assembly || this.producttype.getValue() == Wtf.producttype.customerAssembly) {
                this.qualityControlBOMCodeFormPanel.show();
            } else {
                this.qualityControlBOMCodeFormPanel.hide();
            }
            this.qualityControlBOMCodeFormPanel.doLayout();
            this.doLayoutAllTab();
        }, this);
        
        this.RemarksTab.on('activate',function(){
            this.doLayoutAllTab();
        },this);
        
        this.InventoryTab.on('activate',function(){
            this.doLayoutAllTab();
            this.uom.fireEvent('change',this);
            
            //To Change the FieldLabel of UOM Combo at runtime
            var dd_textfield = Wtf.getCmp('uomid' + this.id);
            if (dd_textfield != undefined && dd_textfield.el != undefined) {
                var ct = dd_textfield.el.findParent('.x-form-item');
                if (ct != null) {
                    var label = ct.firstChild.innerHTML;
                    if (this.producttypeval == Wtf.producttype.service) { //For Service type of Product, UOM Combo in non-mandatory.
                        ct.firstChild.innerHTML = WtfGlobal.getLocaleText("acc.product.stockUoMLabel") + ":";
                    } else {//For Products whose types are other than "Service", it will work as it is.
                        ct.firstChild.innerHTML = WtfGlobal.getLocaleText("acc.product.stockUoMLabel") + "*:";
                    }
                }
            }
        },this);
        
       
        this.addEvents({
            'update':true,
            'cancel':true,
            'productClosed':true
        });
        this.salesAcc.on('select',function(){
            if(Wtf.account.companyAccountPref.isDeferredRevenueRecognition!=undefined&&Wtf.account.companyAccountPref.isDeferredRevenueRecognition&&Wtf.account.companyAccountPref.salesAccount!=""&&this.salesAcc.getValue()!=Wtf.account.companyAccountPref.salesAccount){
                WtfComMsgBox(111,2);
            }
        },this);
        this.salesRevenueRecognitionAccount.on('select',function(){
            if(Wtf.account.companyAccountPref.isDeferredRevenueRecognition!=undefined&&Wtf.account.companyAccountPref.isDeferredRevenueRecognition&&Wtf.account.companyAccountPref.salesRevenueRecognitionAccount!=""&&this.salesRevenueRecognitionAccount.getValue()!=Wtf.account.companyAccountPref.salesRevenueRecognitionAccount){
                WtfComMsgBox(111,2);
            }
        },this);
        WtfComMsgBox(29,4,true);
        this.hideFormFields();
    },
    hideFormFields:function(){
       
        this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.productForm);
    //        this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.customerInvoice);

    },
    
    hideTransactionFormFields:function(array){
        if(array){
            for(var i=0;i<array.length;i++){
                var fieldArray = array[i];
                if(Wtf.getCmp(fieldArray.fieldId+this.id)){
                     
                    if(fieldArray.isHidden){
                        Wtf.getCmp(fieldArray.fieldId+this.id).hideLabel = fieldArray.isHidden;
                        Wtf.getCmp(fieldArray.fieldId+this.id).hidden = fieldArray.isHidden;
                    }
                    if(fieldArray.isReadOnly){
                        Wtf.getCmp(fieldArray.fieldId+this.id).disabled = fieldArray.isReadOnly;
                    }   
                    if(fieldArray.isUserManadatoryField && Wtf.getCmp(fieldArray.fieldId+this.id).fieldLabel != undefined){
                        /*
                         * The below check is added to handle the case in SDP-12738,
                         * so that, if the 'map taxex at product level' check from system preferences is off,
                         * and the purchase tax and sales tax fields are mandatory, the product window should save the product
                         */
                        if((fieldArray.fieldId=='tax' || fieldArray.fieldId=='salestax')) {
                            if(CompanyPreferenceChecks.mapTaxesAtProductLevel()) {
                                Wtf.getCmp(fieldArray.fieldId+this.id).allowBlank = !fieldArray.isUserManadatoryField;
                            }
                        } else {
                            Wtf.getCmp(fieldArray.fieldId+this.id).allowBlank = !fieldArray.isUserManadatoryField;
                        }
                        
                        var fieldLabel="";
                        if(fieldArray.fieldLabelText!="" && fieldArray.fieldLabelText!=null && fieldArray.fieldLabelText!=undefined){
                            fieldLabel= fieldArray.fieldLabelText+" *";
                        }else{
                            fieldLabel=(Wtf.getCmp(fieldArray.fieldId+this.id).fieldLabel) + " *";
                        }
                        Wtf.getCmp(fieldArray.fieldId+this.id).fieldLabel=fieldLabel;
                    }else{
                        if(fieldArray.fieldLabelText!="" && fieldArray.fieldLabelText!=null && fieldArray.fieldLabelText!=undefined){
                            Wtf.getCmp(fieldArray.fieldId+this.id).fieldLabel = fieldArray.isManadatoryField?fieldArray.fieldLabelText+" *":fieldArray.fieldLabelText;
                        }
                    }
                //                    if(fieldArray.isReadOnly){
                //                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).disabled = fieldArray.isReadOnly;
                //                    }
                }
            }
        }
    },
    
    doLayoutAllTab:function(){
        this.ProductFormGroup.doLayout();
        this.GeneralTab.doLayout();
        this.PurchaseForm.doLayout();
        this.PurchaseTab.doLayout();
        this.SalesForm.doLayout();
        this.SalesTab.doLayout();
        this.PropertiesForm.doLayout();
        this.PropertiesTab.doLayout();
        this.RemarksForm.doLayout();
        this.RemarksTab.doLayout();
        this.InventoryDataForm.doLayout();
        this.InventoryTab.doLayout();
    },
   
    ActivateAllTab:function(){
        this.newPanel.setActiveTab(this.GeneralTab);
        this.newPanel.setActiveTab(this.PurchaseTab);
        this.newPanel.setActiveTab(this.SalesTab);
        this.newPanel.setActiveTab(this.PropertiesTab);
        this.newPanel.setActiveTab(this.RemarksTab);
        this.newPanel.setActiveTab(this.InventoryTab);
        this.newPanel.setActiveTab(this.GeneralTab);
    },
   
    
    createStore:function(){
//        this.shelfLocationStoreRec = new Wtf.data.Record.create([
//            {name: 'shelfLocationId'},
//            {name: 'shelfLocationValue'}
//        ]);
//        
//        this.shelfLocationStore = new Wtf.data.Store({
//            reader: new Wtf.data.KwlJsonReader({
//                root: "data"
//            },this.shelfLocationStoreRec),
//            url : "ACCProduct/getshelfLocations.do"
//        });
//        this.shelfLocationStore.load();
        
        //        this.sequenceFormatStoreRec = new Wtf.data.Record.create([
        //        {
        //            name: 'id'
        //        },
        //
        //        {
        //            name: 'value'
        //        }
        //        ]);
        //        this.sequenceFormatStore = new Wtf.data.Store({
        //            reader: new Wtf.data.KwlJsonReader({
        //                totalProperty:'count',
        //                root: "data"
        //            },this.sequenceFormatStoreRec),
        //            //        url: Wtf.req.account +'CompanyManager.jsp',
        //            url : "ACCCompanyPref/getSequenceFormatStore.do",
        //            baseParams:{
        //                mode:this.modeName
        //            }
        //        });
        //          this.sequenceFormatStore.on('load',function(){
        //             if(this.sequenceFormatStore.getCount()>0){
        //                 var seqRec=this.sequenceFormatStore.getAt(0)
        //                this.sequenceFormatCombobox.setValue(seqRec.data.id);
        //                this.getNextSequenceNumber(this.sequenceFormatCombobox);
        //             }
        //         },this);
        //     this.sequenceFormatStore.load();
     
        this.purchaseAccRec = Wtf.data.Record.create ([
            {name: 'accid'},
            {name: 'accname'},
            {name: 'acccode'},
            {name: 'nature'},
            {name: 'hasAccess'}

        ]);
        this.purchaseAccStore=new Wtf.data.Store({
              url:"ACCAccountCMN/getAccountsIdNameForCombo.do",
            baseParams:{
                mode:2,
//                ignoreCashAccounts:true,
//                ignoreBankAccounts:true,
//                ignoreGSTAccounts:true,  
                ignorecustomers:true,  
                ignorevendors:true,
                nondeleted:true,
                controlAccounts:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.purchaseAccRec)
        });
        this.itcAccStore = new Wtf.data.Store({
            url: "ACCAccountCMN/getAccountsIdNameForCombo.do",
            baseParams: {
                mode: 2,
                ignorecustomers: true,
                ignorevendors: true,
                nondeleted: true,
                controlAccounts: true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.purchaseAccRec)
        });
        this.itcAccountCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.India.ITC.Accountdropdown.tooltip") + "'>" + WtfGlobal.getLocaleText("acc.India.ITC.Accountdropdown") + "</span>",
            store: this.itcAccStore,
            anchor: '70%',
            name: 'itcaccount',
//            id: 'itcaccount' + this.id,
            hiddenName: 'itcaccount',
            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode'] : [],
            extraComparisionField: 'acccode', // type ahead search on acccode as well.
            typeAheadDelay: 30000,
            typeAhead: true,
            hideLabel: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA || !Wtf.isitcapplicable,
            hidden: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA || !Wtf.isitcapplicable,
            isAccountCombo: true,
            listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 350 : 240,
            valueField: 'accid',
            displayField: 'accname',
            forceSelection: true,
            mode: 'remote'
        });
        var typeArr = new Array();
        typeArr.push([Wtf.GSTITCTYPEID.DEFAULT, Wtf.GSTITCTYPE.DEFAULT]);
        typeArr.push([Wtf.GSTITCTYPEID.BLOCKEDITC, Wtf.GSTITCTYPE.BLOCKEDITC]);
        typeArr.push([Wtf.GSTITCTYPEID.ITCREVERSAL, Wtf.GSTITCTYPE.ITCREVERSAL]);
        this.itcType = new Wtf.data.SimpleStore({
            fields: ['typeid', 'name'],
            
            data: typeArr
        });
        this.itcTypeCmbForProduct = new Wtf.form.ComboBox({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.India.ITC.dropdown.tooltip") + "'>" + WtfGlobal.getLocaleText("acc.India.ITC.dropdown") + "</span>",
            store: this.itcType,
            name: 'typeid',
            hiddenName: 'typeid',
            displayField: 'name',
            value:Wtf.GSTITCTYPEID.DEFAULT,
            valueField: 'typeid',
            mode: 'local',
            triggerAction: 'all',
            forceSelection: true,
            hideLabel: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA || !Wtf.isitcapplicable,
            hidden: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA || !Wtf.isitcapplicable,
            anchor: '70%',
                   
        });
        this.locationRec = new Wtf.data.Record.create([
        {
            name:"id"
        },
        
        {
            name:"name"
        },

        {
            name: 'parentid'
        },

        {
            name: 'parentname'
        }
        ]);
        this.locationReader = new Wtf.data.KwlJsonReader({
            root:"data"
        },this.locationRec);

        this.locationStore = new Wtf.data.Store({
            //url:"ACCMaster/getLocationItems.do",
            url:  'INVStore/getStoreLocations.do',
            baseParams:{isActive:true},   //ERP-40021 :To get only active Locations.
            reader:this.locationReader
        });
        
        
        this.wareHouseRec = new Wtf.data.Record.create([
        {
            name: 'store_id'
        },

        {
            name: "fullname"
        },
        {
            name:"id"
        },

        {
            name:"name"
        },

        {
            name: 'parentid'
        },
        {
            name: 'location'
        },

        {
            name: 'parentname'
        }
        ]);
        this.wareHouseReader = new Wtf.data.KwlJsonReader({
            root:"data"
        },this.wareHouseRec);
        
        
        this.wareHouseStore = new Wtf.data.Store({
            //url:"ACCMaster/getWarehouseItems.do",
            url:'INVStore/getStoreList.do',
            baseParams:{
                storeTypes: "0,2",
                isActive:true
            },
            reader:this.wareHouseReader
        });
       
        
        //        this.locationStore.load();
        
        this.FixedAssetControllingAccStore = new Wtf.data.Store({
              url:"ACCAccountCMN/getAccountsIdNameForCombo.do",
            baseParams:{
                mode:2,
                ignorecustomers:true,  
                ignorevendors:true,
                onlyBalancesheet:true,
                ignoreGSTAccounts:true,
                nature:[0,1]
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.purchaseAccRec)
        });
        
        if(this.isFixedAsset){
            this.FixedAssetControllingAccStore.load();
        }
       
        this.parentRec=new Wtf.data.Record.create([{
                name:'parentid',mapping:'productid'
        },{
                name:'parentname',mapping:'productname'
        },{
                name:'leaf',type:'boolean'
        },{
                name:'level',type:'int'
        },{ 
            name: "productid"
        },{
                name:'pid',mapping:'pid'
        }]);
        this.parentStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.parentRec),
            url:"ACCProduct/getProductsOptimized.do",
            baseParams:{
                mode:22,
//                includeParent:(Wtf.account.companyAccountPref.productOptimizedFlag === Wtf.Products_on_type_ahead && this.isEdit)?true:false,
                includeParent:true,
                showallproduct:2,//this is for to fetch non deleted products in parent products combo
                productid:(this.record!=null && !this.isClone?this.record.data['productid']:null)
            }
        });
//        if(this.isEdit){
//             this.parentStore.on('beforeload',function(s,o){
//                if(!o.params)o.params={};
//                var currentBaseParams = this.parentStore.baseParams;
//                currentBaseParams.ids = this.record.data.parentuuid; 
//                
//            },this); 
//        }
        if(!this.isEdit || this.isClone){
            Wtf.Ajax.requestEx({
                url:"ACCProduct/getProductIDAutoNumber.do",
        		params: {dummyParam:true}
            },this,this.getIdSuccessResponse);
        }
        
        if(this.isEdit){
        	var productid = {productid:this.record.data['productid']};
            Wtf.Ajax.requestEx({
                url:"ACCProductCMN/editQuantity.do",
                params: productid
            },this,this.editQuantitySuccessResponse,this.editQuantityFailureResponse);
        } 

//        if(this.globalProductCount>0){
//            this.cloneProductList();
//        }
        chkProductTypeload();
        this.loadProductStore();
        chkUomload();
        chksalesAccountload();
        chkvenaccload();
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
//            chkReportingUomload();
        }
        Wtf.productTypeStore.on("load", this.setProductType, this);
        Wtf.uomStore.on("load", this.setUoM, this);
        Wtf.vendorAccStore.on("load",this.setVendor,this);
        Wtf.salesAccStore.on("load", this.setSalesAccount, this);
//        Wtf.productStore.on("load", this.cloneProductList, this);

        //        this.parentStore.on("load",function(){
        //            if(this.record!=null){
        //                this.parentname.setValue(this.record.data.parentid);
        //            }
        //        },this);

        this.purchaseAccStore.on("load",function(){
            if(this.record!=null){							// Neeraj    Check if the id of record is present in the type of Account
                if(this.purchaseAccStore.find('accid',this.record.data.purchaseaccountid) != -1){
                    if(this.purchaseAcc.rendered){
                        this.purchaseAcc.setValue(this.record.data.purchaseaccountid);
                    }else{
                        this.purchaseAcc.on("render", function () {
                            this.purchaseAcc.setValue(this.record.data.purchaseaccountid);
                        }, this);
                    }
                    var accRec = WtfGlobal.searchRecord(this.purchaseAccStore, this.record.data.purchaseaccountid, 'accid');
                    this.purchaseAccountRec = accRec;
                }
                if(this.purchaseAccStore.find('accid',this.record.data.purchaseretaccountid) != -1){
                    if(this.purchaseReturnAcc.rendered){
                        this.purchaseReturnAcc.setValue(this.record.data.purchaseretaccountid);
                    }else{
                        this.purchaseReturnAcc.on("render", function () {
                            this.purchaseReturnAcc.setValue(this.record.data.purchaseretaccountid);
                        }, this);
                    }
                }
                if(this.purchaseAccStore.find('accid',this.record.data.interStatePurAccCformID) != -1){
                    this.inputVAT.setValue(this.record.data.inputVAT);
                }
                if(this.purchaseAccStore.find('accid',this.record.data.interStatePurReturnAccID) != -1){
                    this.cstVATattwo.setValue(this.record.data.cstVATattwo);
                }
                if(this.purchaseAccStore.find('accid',this.record.data.interStatePurReturnAccCformID) != -1){
                    this.cstVAT.setValue(this.record.data.cstVAT);
                }   
                if (this.purchaseAccStore.find('accid', this.record.data.inventoryaccountid) != -1) {
                    this.inventoryAcc.setValue(this.record.data.inventoryaccountid);
                }                
                if (this.purchaseAccStore.find('accid', this.record.data.stockadjustmentaccountid) != -1) {
                    this.stockAdjustmentAcc.setValue(this.record.data.stockadjustmentaccountid);
                }
                if (this.purchaseAccStore.find('accid', this.record.data.cogsaccountid) != -1) {
                    this.cogsAcc.setValue(this.record.data.cogsaccountid);
                }
                }else if(Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA){
                    if(this.purchaseAccStore.find('accid',Wtf.purchaseretaccountidcompany) != -1){
                        this.purchaseReturnAcc.setValue(Wtf.purchaseretaccountidcompany);
                    }
                    if(this.purchaseAccStore.find('accid',Wtf.purchaseaccountidcompany) != -1){
                        this.purchaseAcc.setValue(Wtf.purchaseaccountidcompany);
                    }
                }                            
            this.loadRecord();
            Wtf.MessageBox.hide();
            this.Pname.focus();
        },this);
        this.purchaseAccStore.on("loadexception",function(){
            this.loadRecord();
            Wtf.MessageBox.hide();
            this.Pname.focus()
        },this);

        this.locationStore.on("load",function(){
            if(this.record!=null){							
                if(this.locationStore.find('id',this.record.data.location) != -1){
                    this.locationEditor.setValue(this.record.data.location);
                }
                
            }
            //this.loadRecord();
            Wtf.MessageBox.hide();
            this.Pname.focus();
        },this);
        this.locationStore.on("loadexception",function(){
            //this.loadRecord();
            Wtf.MessageBox.hide();
            this.Pname.focus()
        },this);
        
        this.wareHouseStore.on("load",function(){
            if(this.record!=null){							
                if(this.wareHouseStore.find('store_id',this.record.data.warehouse) != -1){
                    this.wareHouseEditor.setValue(this.record.data.warehouse);
                    this.wareHouseEditor.fireEvent('select');
                }
            
            }
            //            this.loadRecord();
            Wtf.MessageBox.hide();
            this.Pname.focus();
        },this);
        this.wareHouseStore.on("loadexception",function(){
            //            this.loadRecord();
            Wtf.MessageBox.hide();
            this.Pname.focus()
        },this);
        
        if(this.isFixedAsset){
            this.FixedAssetControllingAccStore.on("load",function(){
                if(this.record!=null){
                    if(this.FixedAssetControllingAccStore.find('accid',this.record.data.purchaseaccountid) != -1){
                        this.assetControllingAcc.setValue(this.record.data.purchaseaccountid);// Asset Controlling account is containing same value as purchase account, purchase return account, sales account, sales return account.
                    }
                }
                this.loadRecord();
                Wtf.MessageBox.hide();
                this.Pname.focus();
            },this);
            this.FixedAssetControllingAccStore.on("loadexception",function(){
                this.loadRecord();
                Wtf.MessageBox.hide();
            //                this.Pname.focus()
            },this);
        }
    },
    
        
    editQuantitySuccessResponse:function(response){
        this.editQuantity=response.quantityEdit;
        this.isUsedInTransactionwithBatchSerial=response.isUsedInBatchSerial;
        if((this.record.data['producttype'] != Wtf.producttype.assembly || this.record.data['producttype'] != Wtf.producttype.customerAssembly) && !this.isClone){
            if(!this.editQuantity){
                this.quantity.disable();
            }else{
                this.quantity.enable();
            }
            
            if(this.isUsedInTransactionwithBatchSerial){
                if(this.isBatchForProduct)this.isBatchForProduct.disable();
                if(this.isLocationForProduct)this.isLocationForProduct.disable();
                if(this.isWarehouseForProduct)this.isWarehouseForProduct.disable();
                if(this.isSerialForProduct)this.isSerialForProduct.disable();
                if(this.isBinForProduct)this.isBinForProduct.disable();
                if(this.isRackForProduct)this.isRackForProduct.disable();
                if(this.isRowForProduct)this.isRowForProduct.disable();
            }else{
                if(this.isBatchForProduct)this.isBatchForProduct.enable();
                if (Wtf.account.companyAccountPref.activateInventoryTab){
                    if(this.isLocationForProduct)this.isLocationForProduct.disable();
                    if(this.isWarehouseForProduct)this.isWarehouseForProduct.disable();
                } else {
                    if(this.isLocationForProduct)this.isLocationForProduct.enable();
                    if(this.isWarehouseForProduct)this.isWarehouseForProduct.enable();
                }
                if(this.isSerialForProduct)this.isSerialForProduct.enable();
                if(this.isBinForProduct)this.isBinForProduct.enable();
                if(this.isRackForProduct)this.isRackForProduct.enable();
                if(this.isRowForProduct)this.isRowForProduct.enable();
            
            }
        }
        /*
         *If product is used in any transaction while editing then following fields are disable.
         */
        if(!this.editQuantity && !this.isClone){    //SDP-10764
            this.CasingUoMCombo.disable();
            this.InnerUoMCombo.disable();
            this.packingStockUom.disable();
            this.packaging.disable();
            this.Salesuom.disable();
            this.OrderingUoMCombo.disable();
            this.CasingUoMValue.disable();
            this.InnerUoMValue.disable();
            this.StockUoMValue.disable();
            this.Purchaseuom.disable();
            this.TransferUoMCombo.disable();
            this.uom.disable();
            this.multiuom.disable();
            this.blockLooseSell.disable();
            this.schemaType.disable();
        }
        
    },
	
    editQuantityFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    },
    
    getIdSuccessResponse:function(response){
        if(response.success){
            this.autoID=response.autoNumberID;
            if(this.autoID != "" && this.autoID != undefined){
                if(this.isEdit){
                    this.PID.setValue(this.autoID);
                }
            }
        }
    },
	
    setUoM: function(){
        if(this.record!=null){
            this.uom.setValue(this.record.data.uomid);
            this.displayUom.setValue(this.record.data.displayUoMid);
            this.packingStockUom.setValue(this.record.data.uomid);
        }
        Wtf.uomStore.un("load", this.setUoM, this);
    },
    setVendor: function(){
        if(this.record!=null){
            this.vendor.setValue(this.record.data.vendor);
        }
        Wtf.vendorAccStore.un("load", this.setVendor, this);
    },
    setProductType: function(){
        this.producttype.setValue(this.producttypeval);//Default Inventory Item
        this.changeLayoutWithType();
        Wtf.productTypeStore.un("load", this.setProductType, this);
    },
    setSalesAccount: function(){
        if(this.record!=null){
            this.salesAcc.setValue(this.record.data.salesaccountid);
            this.salesReturnAcc.setValue(this.record.data.salesretaccountid);
            this.salesRevenueRecognitionAccount.setValue(this.record.data.salesRevenueRecognitionAccountid);
            this.revenueRecognitionProcess.setValue(this.record.data.revenueRecognitionProcess);
        }
        Wtf.salesAccStore.un("load", this.setSalesAccount, this);
    },
    cloneProductList: function(){
        //        this.parentStore.loadData(Wtf.productStore.reader.jsonData);
        //        this.parentStore.filter("productid",this.record.data.productid);
        if(Wtf.getCmp(this.id)){
            Wtf.productStore.each(function(rec){
                rec.data.parentid = rec.data.productid;
                rec.data.parentname = rec.data.productname;
                if(this.record!=null){
                    if(rec.data.productid!=this.record.data.productid || this.isClone){
                        this.parentStore.add(rec);
                    }
                } else {
                    this.parentStore.add(rec);
                }
            },this);

            if(this.record!=null && this.globalProductCount<=0){
                this.parentname.setValue(this.record.data.parentuuid);
            }
        }
    //        Wtf.productStore.un("load", this.cloneProductList, this);
    },
    setNextNumber:function(){
        if(this.sequenceFormatStore.getCount()>0){
            if(this.isEdit || this.isClone){ //only edit case
                var index=this.sequenceFormatStore.find('id',this.record.data.sequenceformatid);   
                if(index!=-1){
                    this.sequenceFormatCombobox.setValue(this.record.data.sequenceformatid); 
                    if(!this.isClone){//edit
                        this.sequenceFormatCombobox.disable();
                        if(this.PID!=undefined){
                            this.PID.disable(); 
                        }
                    }else{//copy if sequenceformatid present
                        this.getNextSequenceNumber(this.sequenceFormatCombobox);
                    }
                } else {
                    this.sequenceFormatCombobox.setValue("NA"); 
                    if(this.isClone == undefined || this.isClone ==false){
                        this.sequenceFormatCombobox.disable();
                    }else{//for clone or copy NA case show no field
                        this.getNextSequenceNumber(this.sequenceFormatCombobox);
                    }
                    
                    if(this.PID!=undefined){
                        if(!this.isUsedInTransaction){
                            this.PID.enable(); 
                        }
                    }
                    
                }
            }else{//creating 
                var count=this.sequenceFormatStore.getCount();
                for(var i=0;i<count;i++){
                    var seqRec=this.sequenceFormatStore.getAt(i)
                    if(seqRec.json.isdefaultformat=="Yes"){
                        this.sequenceFormatCombobox.setValue(seqRec.data.id) 
                        break;
                    }
                }
                if(this.sequenceFormatCombobox.getValue()!=""){
                    this.getNextSequenceNumber(this.sequenceFormatCombobox);
                } else{
                    this.PID.setValue("");
                    this.PID.disable();
                }
            }
        }
    },
    getPostTextEditor: function(desctype){
        this.desctype = desctype;
        if(this.desctype=="Desc"){
            this.desc = this.description.getValue();
        } else if(this.desctype=="addDesc"){
            var addDesc = this.AdditionalDesc.getValue();
            addDesc = WtfGlobal.replaceAll(addDesc, "\n" , "<br>");
            this.desc = addDesc;
        } else if(this.desctype=="foreignDesc"){
            var addDesc = this.DescForeign.getValue();
            addDesc = WtfGlobal.replaceAll(addDesc, "\n" , "<br>");
            this.desc = addDesc;
        }
        var _tw=new Wtf.EditorWindowQuotation({
            val:this.desc
        });
        _tw.on("okClicked", function(obj){
            var postText = obj.getEditorVal().textVal;
            var styleExpression  =  new RegExp("<style.*?</style>");
            postText=postText.replace(styleExpression,"");
            if(this.desctype=="Desc"){
                this.description.setValue(postText);
            }
            postText = WtfGlobal.replaceText('<br>',postText,'\n');
            postText = postText.replace(/(<([^>]+)>)/ig,"");
            
            postText = WtfGlobal.replaceText('&nbsp;',postText,' ');
            if(this.desctype=="Desc"){
                this.descriptionshow.setValue(postText);
            }else if(this.desctype=="addDesc"){
                this.AdditionalDesc.setValue(postText);
            }else if(this.desctype=="foreignDesc"){
                this.DescForeign.setValue(postText);
            }
        }, this);         
        _tw.show();
    },
    
    enableDisableDisplayUOMField:function(){
        this.displayUOMStore.removeAll();
        this.displayUOMStore.load({
            scope: this,
            params: {
                uomschematypeid: this.schemaType.getValue()
            }
        });
        if(this.schemaType.getValue() != null &&  this.schemaType.getValue() != "" && CompanyPreferenceChecks.displayUOMCheck()){
            this.displayUom.enable();
        } else {
            this.displayUom.disable();
        }
    },
    ChangeThenClear: function (f, e) {
        if (this.schemaType.getRawValue() == "" || this.schemaType.getRawValue() == null) {
            this.displayUom.clearValue();
            this.displayUOMStore.removeAll();
        }
    },
    ChangeThenreset: function (f, e) {
        this.displayUom.clearValue();
    },
    createFields:function(){
        this.Pname=new Wtf.form.ExtendedTextField({
        //fieldLabel:(this.isFixedAsset)?"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.fixedasset.groupname.tt")+"'>"+'Asset Group Name*' +"</span>":"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.productName.tt")+"'>"+WtfGlobal.getLocaleText("acc.product.productName")+"</span>",//(this.isFixedAsset)?'Asset Group Name*':WtfGlobal.getLocaleText("acc.product.productName"),//'Product Name*',
        fieldLabel:this.isFixedAsset?"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.fixedasset.groupname.tt") +"'>"+  WtfGlobal.getLocaleText('Asset Group Name*')  +"</span>":"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.productName.tt") +"'>"+ WtfGlobal.getLocaleText("acc.product.productName") +"</span>",
        name: 'productname',
        id:'productname'+this.id,
        //            disabled:(this.isEdit && !this.isClone)?true:false,
        allowBlank:false,
        anchor:'85%',
        //            regex:/^[\w\s\'\"\.\-]+$/,
        //           regex:/^[^\"]+$/,                              // Removed the % sign for resolving issue ERP-710: Have to remove the restriction in the add/edit product screen.
        maskRe: Wtf.productNameCommaMaskRe,
        invalidText : 'This field should not be blank or should not contain %, ", \\ characters.',
        maxLength:255
    });
    this.Pname.on('invalid',function( field,msg){//Set padding for invalid icon
        this.setStyleWidth(field,msg);
    },this);
   if(!this.isEdit){
        this.Pname.on('change',function(field,msg){//Set padding for invalid icon
            if(this.Pname.getValue().indexOf(',')!=-1){
                this.Pname.setValue("");
            }
        },this);
    }
        this.sequenceFormatStoreRec = new Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'value'
        },
        {
            name: 'oldflag'
        }
        ]);
        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.sequenceFormatStoreRec),
            //        url: Wtf.req.account +'CompanyManager.jsp',
            url : "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams:{
                mode:this.modeName,
                isEdit: this.isClone ? false : this.isEdit
            }
        });
        this.sequenceFormatStore.load();
        this.sequenceFormatStore.on('load',this.setNextNumber,this);
        
        this.uomschematypeRec = Wtf.data.Record.create ([
        //                {name:'uomid'},
                
                {name:'uomschematypeid'},
                {name:'uomschematype'},
                {name:'uomid'},
                {name:'uomname'}

            ]);
        
        
        this.msgLmt = 30;
        this.uomschematypeReader = new Wtf.data.KwlJsonReader({
            totalProperty: 'count',
            root: "data"
        }, this.uomschematypeRec);
        
        this.uomschematypeStore = new Wtf.data.Store({
            url:"ACCUoM/getUOMType.do",
            baseParams:{
                mode:22
            //                isForCustomer:true
            },
            reader: this.uomschematypeReader
        });
        this.uomschematypeStore.on('load',function(){
            if(this.record != undefined){
                this.schemaType.setValue(this.record.data.uomschematypeid);
                this.displayUom.setValue(this.record.data.displayUoMid);
            }
            
        },this);
        this.uomschematypeStore.load();
        this.sequenceFormatCombobox = new Wtf.form.ComboBox({            
            //        labelSeparator:'',
            //        labelWidth:0,
            triggerAction:'all',
            mode: 'local',
            emptyText: WtfGlobal.getLocaleText("acc.common.plselectseqformat"),
            allowBlank: false,
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.Sequenceformat.tip")+"'>"+ WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat")+"</span>",//WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
            valueField:'id',
            displayField:'value',
            store:this.sequenceFormatStore,
            disabled:(this.isEdit && !this.isClone?true:false),  
            anchor:'85%',
            typeAhead: true,
            forceSelection: true,
            name:'sequenceformat',
            id:'sequenceformat'+this.id,
            hiddenName:'sequenceformat',
            listeners:{
                'select':{
                    fn:this.getNextSequenceNumber,
                    scope:this
                }
            }
            
        });
        
        this.PID=new Wtf.form.ExtendedTextField({
            fieldLabel:(this.isFixedAsset)?"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.fixedasset.groupid.tt")+"'>"+ 'Asset Group ID*'+"</span>":"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.fixedasset.groupid.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.productID") +"</span>",//(this.isFixedAsset)?'Asset Group ID*':WtfGlobal.getLocaleText("acc.product.productID"),//'Product ID*',
            disabled:(this.isEdit && !this.isClone)?true:false,
            name: 'pid',
            id:'pid'+this.id,
            anchor:'85%',
            allowBlank:false,
            maxLength:50
        });
        this.syncable= new Wtf.form.Checkbox({
            name:'syncable',
            id:'syncable'+this.id,
            fieldLabel:"<span wtf:qtip='"+((this.isFixedAsset)?WtfGlobal.getLocaleText("acc.fixedasset.makeAvailCRM.tt"):WtfGlobal.getLocaleText("acc.product.makeAvailCRM.tt"))+"'>"+ WtfGlobal.getLocaleText("acc.product.makeAvailCRM")+"</span>",//WtfGlobal.getLocaleText("acc.product.makeAvailCRM"),//'Make available in CRM',
            checked:false,
            itemCls:"chkboxalign"
        });
        this.multiuom= new Wtf.form.Checkbox({
            name:'multiuom',
            id:'multiuom'+this.id,
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.multiuom.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.multiuom")+"</span>",//WtfGlobal.getLocaleText("acc.product.multiuom"),//'Make available in CRM',
            checked:false,
            itemCls:"chkboxalign"
        });
        this.blockLooseSell= new Wtf.form.Checkbox({
            name:'blockLooseSell',
            id:'blockLooseSell'+this.id,
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.blockLooseSell.producttt")+"'>"+ WtfGlobal.getLocaleText("acc.blockLooseSell.product")+"</span>",//Allow Loose Selling
            disabled:true,
            itemCls:"chkboxalign"
        });
        
        this.schemaType= new Wtf.form.FnComboBox({
            //fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.uom.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.uom")+"</span>",//WtfGlobal.getLocaleText("acc.product.uom"),//'Unit Of Measure*',
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.accPref.inventoryschema") +"'>"+WtfGlobal.getLocaleText("acc.accPref.inventoryschema")+"</span>",
            hiddenName:'uomschematypeid',
            id:'uomschematypeid'+this.id,
            store:this.uomschematypeStore,
            hidden:this.isFixedAsset,
            hideLabel:this.isFixedAsset,
            anchor:'70%',
            allowBlank:true,
            disabled:true,
            valueField:'uomschematypeid',
            displayField:'uomschematype',
            forceSelection: true//,
            // addNewFn:this.showUom.createDelegate(this)
        }); 
        this.schemaType.on("select",this.enableDisableDisplayUOMField.createDelegate(this),this);
        this.schemaType.on("blur",this.ChangeThenClear.createDelegate(this),this),
        this.schemaType.on("change",this.ChangeThenreset.createDelegate(this),this),
                        
        this.inspectionTemplateRec = Wtf.data.Record.create ([
        {
            name:'templateId'
        },

        {
            name:'templateName'
        }
        ]);
        this.inspectionTemplateStore=new Wtf.data.Store({
            url: "INVTemplate/getInspectionTemplateList.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.inspectionTemplateRec)
        });
       
        this.inspectionTemplate= new Wtf.form.ComboBox({
            fieldLabel :WtfGlobal.getLocaleText("acc.lp.inspectiontemplate"),
            hiddenName:'dependentType',
            store:this.inspectionTemplateStore,
            valueField:'templateId',
            displayField:'templateName',
            triggerAction:'all',
            hidden:!Wtf.account.companyAccountPref.activateQAApprovalFlow,
            hideLabel:!Wtf.account.companyAccountPref.activateQAApprovalFlow,
            mode: 'local',
            anchor:'85%',
            typeAhead: true,
            forceSelection: true,
            name:'inspectionTemplate'
 
        });   
        this.inspectionTemplateStore.on("load",function(){
            if(this.isEdit)
                this.inspectionTemplate.setValue(this.record.data['inspectionTemplate']);
        },this); 
        this.inspectionTemplateStore.load();
        
        //            this.schemaType.addNewFn=this.addUomSchema.createDelegate(this,[null,false,true],true);      
        
        this.multiuom.on('check', function(){    
            if(this.multiuom.getValue()){
                this.uomschematypeStore.load({
                    params:{
                        stockuomid:this.uom.getValue()
                    }
                });
                if(!Wtf.account.companyAccountPref.UomSchemaType==Wtf.PackegingSchema ){
                   this.schemaType.enable();
            }
                this.blockLooseSell.enable();
                if(this.readOnly)
                    this.schemaType.disable();
            }else{
                this.schemaType.clearValue();
                this.schemaType.disable();
                this.blockLooseSell. setValue(false);
                this.blockLooseSell.disable();
            }
        }, this);
        this.multiuom.on('check', function(){
            if(this.multiuom.getValue()){
                this.uomschematypeStore.load({
                    params:{
                        stockuomid:this.uom.getValue()
                    }
                });
            if(this.readOnly)
                this.displayUom.disable();        
            }else{
                this.displayUom.clearValue();
                this.displayUom.disable();                
            }
        }, this);
        this.autoAssembly = new Wtf.form.Checkbox({
            name:'autoAssembly',
            id:'autoAssembly'+this.id,
            fieldLabel: WtfGlobal.getLocaleText("acc.field.BuildAssemblyonSale"), // "Auto Build Assembly on Sale",
//            checked:false,
            itemCls:"chkboxalign"
        });
            this.autoAssembly.on('change', function(comp, newValue, oldValue) {
            if (newValue) {
                Wtf.Ajax.requestEx({
                    url: "ACCProduct/isDefaultSeuenceFormatSetForBuildAssembly.do",
                    params: {
                        companyid: companyid

                    }
                }, comp, function (resp) {
                    if (resp.isDefaultSeuenceFormatSetForBuildAssembly == false) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), resp.msg], 2);
                        comp.setValue(oldValue);
                    }
                    if (resp.success == false) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), resp.msg], 2);
                        comp.setValue(oldValue);
                    }
                });
            }});
        /**
         * if 'Enable Import Auto Assembly Without BOM' is true
         * then show Alert Message on click "Auto Build Assembly on Sale"
         */
        this.autoAssembly.on('check', function(obj, checked) {

            if (checked) {
                if (CompanyPreferenceChecks.withoutBOMCheck()) {
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.information"),
                        width: 500,
                        msg: WtfGlobal.getLocaleText("acc.field.BuildAssemblyonSale.alertMessages"),
                        scope: this,
                        icon: Wtf.MessageBox.WARNING
                    });
                    this.autoAssembly.reset();
                    return;
                }
            }

            if (this.autoAssembly.getValue()) { // The serial number option will be disable if 'Auto Build Assembly on Sale' functionality is activated. 
                this.isBatchForProduct.reset();
//                this.isLocationForProduct.reset();
//                this.isWarehouseForProduct.reset();
//                  this.isLocationForProduct.setValue(false);
//                this.isWarehouseForProduct. setValue(false);
                this.isSerialForProduct.reset();
                this.isSKUForProduct.reset();
                this.isRowForProduct.reset();
                this.isRackForProduct.reset();
                this.isBinForProduct.reset();
//                this.isLocationForProduct.disable();
//                this.isWarehouseForProduct.disable();
                this.isRowForProduct.disable();
                this.isRackForProduct.disable();
                this.isBinForProduct.disable();
                this.isBatchForProduct.disable();
                this.isSerialForProduct.disable();
                this.isSKUForProduct.disable();
            } else if (!(this.isEdit && !this.isClone)) { // enable serial number option for clone and in create new And disabled for edit condition. if 'Auto Build Assembly on Sale' functionality is deactivated. 
                if (Wtf.account.companyAccountPref.activateInventoryTab) {
                    this.isLocationForProduct.disable();
                    this.isWarehouseForProduct.disable();
                }else{
                    this.isLocationForProduct.enable();
                    this.isWarehouseForProduct.enable();
                }
                this.isRowForProduct.enable();
                this.isRackForProduct.enable();
                this.isBinForProduct.enable();
                this.isBatchForProduct.enable();
                this.isSerialForProduct.enable();
                this.isSKUForProduct.enable();
            }

        }, this);
        
        this.isRecylable= new Wtf.form.Checkbox({
            name:'recyclable',
            id:'recyclable'+this.id,
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.IsRecyclable.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.IsRecyclable")+"</span>",//WtfGlobal.getLocaleText("acc.product.IsRecyclable"),//"Is Recyclable"
            //            disabled:(this.isFixedAsset ||(this.isEdit && !this.isClone))?true:false,
            checked:false,
            itemCls:"chkboxalign"
        });
        
        this.rcmapplicable = new Wtf.form.Checkbox({ // GTA Applicable  ERP-25539
            name:'rcmapplicable',
            hidden: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA),
            hideLabel: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA),
            fieldLabel:WtfGlobal.getLocaleText("acc.compref.india.rcm.applicable"),
            checked:false,
            disabled:false,
            itemCls:"chkboxalign"
        });
        
        this.isWastageApplicable = new Wtf.form.Checkbox({
            name: 'isWastageApplicable',
            id: 'isWastageApplicable' + this.id,
            fieldLabel: "<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.field.isWastageApplicable")+"'>"+ WtfGlobal.getLocaleText("acc.field.isWastageApplicable")+"</span>", // "Is Wastage Default Account",
            cls : 'custcheckbox',
            hidden: !Wtf.account.companyAccountPref.activateWastageCalculation,
            hideLabel: !Wtf.account.companyAccountPref.activateWastageCalculation,
            disabled :(this.isUsedInTransaction && this.isEdit && !this.isClone && this.producttypeval==Wtf.producttype.assembly) ? true : false
        });
        
        this.isWastageApplicable.on('check', function(o,newval,oldval) {
            if (this.isWastageApplicable.getValue()) {
                this.wastageAccount.enable();
                this.wastageAccount.allowBlank = false;
                WtfGlobal.updateFormLabel(this.wastageAccount, WtfGlobal.getLocaleText("acc.field.wastageAccount")+"*");
                if (Wtf.account.companyAccountPref.wastageDefaultAccount != "" && Wtf.account.companyAccountPref.wastageDefaultAccount != undefined) {
                    this.wastageAccount.setValue(Wtf.account.companyAccountPref.wastageDefaultAccount);
                }
            } else {
                this.wastageAccount.reset();
                this.wastageAccount.allowBlank = true;
                this.wastageAccount.disable();
                WtfGlobal.updateFormLabel(this.wastageAccount, WtfGlobal.getLocaleText("acc.field.wastageAccount"));
            }
        }, this);
        
        this.wastageAccountRec = Wtf.data.Record.create([
            {name: 'accountname', mapping: 'accname'},
            {name: 'accountid', mapping: 'accid'},
            {name: 'acccode'},
            {name: 'groupname'},
            {name: 'hasAccess'}
        ]);
        
        this.wastageAccountStore = new Wtf.data.Store({
              url:"ACCAccountCMN/getAccountsIdNameForCombo.do",
            baseParams: {
                mode: 2,
                deleted: false,
                nondeleted: true,
                ignoreAssets: true,
                controlAccounts: true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.wastageAccountRec)
        });
        
        this.wastageAccountStore.on('load', function(){
            if (this.isEdit) {
                this.wastageAccount.setValue(this.record.data.wastageAccount);
            }
        },this);
        if (Wtf.account.companyAccountPref.activateWastageCalculation) {
            this.wastageAccountStore.load();
        }
        
        this.wastageAccount = new Wtf.form.FnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.wastageAccount"),
            hiddenName: 'wastageAccount',
            store: this.wastageAccountStore,
            anchor: '95%',
            valueField: 'accountid',
            mode: 'local',
            displayField: 'accountname',
            extraComparisionField: 'acccode', // type ahead search on acccode as well.
            selectOnFocus: true,
            scope: this,
            forceSelection: true,
            triggerAction: 'all',
            disabled: true,
            hidden: !Wtf.account.companyAccountPref.activateWastageCalculation,
            hideLabel: !Wtf.account.companyAccountPref.activateWastageCalculation
        });
        
        this.currencyRec = new Wtf.data.Record.create([
            {name: 'currencyid',mapping:'tocurrencyid'},
            {name: 'symbol'},
            {name: 'currencyname',mapping:'tocurrency'},
            {name: 'exchangerate'},
            {name: 'htmlcode'}
        ]);
        this.currencyStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.currencyRec),
            url:"ACCCurrency/getCurrencyExchange.do"
        });
        this.Currency= new Wtf.form.FnComboBox({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.currency.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.productPriceCurrency") +" *"  +"</span>", //'Product Price Currency*',
            hiddenName:'currencyid',
            name:'currencyid',
            id:'currencyid'+this.id,
//            disabled:this.isEdit ? (this.enableCurrency==undefined ? this.isEdit : !this.enableCurrency):this.isEdit,
            anchor:'95%',
            hidden:this.isFixedAsset,
            hideLabel:this.isFixedAsset,
            allowBlank:false,
            store: this.currencyStore,
            valueField:'currencyid',
            emptyText:WtfGlobal.getLocaleText("acc.cust.currencyTT"),  //'Please select Currency...',
            forceSelection: true,
            displayField:'currencyname',
            scope:this,
            selectOnFocus:true
        });
        this.currencyStore.on('load', function(){
            if(this.isEdit){
                this.Currency.setValue(this.record.data.currencyid);
            } else if(Wtf.account.companyAccountPref.currencyid){
                // Set to Base Currency if currency is not assigned.
                if(this.Currency.getValue() ==""|| this.Currency.getValue() == null|| this.Currency.getValue() == undefined)
                    this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
                if(this.Currency.getValue() !="" && WtfGlobal.searchRecord(this.currencyStore,this.Currency.getValue(),"currencyid") == null){ 
                    // If set currency do not have exchange rate then reset currency
                    this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
                    callCurrencyExchangeWindow();
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.invoice.msg1")],2);
                }
            }
        },this);
        this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(Wtf.account.companyAccountPref.bbfrom)}});
            
        if(Wtf.account.companyAccountPref.enablevatcst){
            Wtf.vatCommodityStore.load();
        }
        this.qaenable= new Wtf.form.Checkbox({
            name:'qaenable',
            fieldLabel:"QA Approval Flow",
            checked:false,
            hidden:!(Wtf.account.companyAccountPref.activateQAApprovalFlow),
            //hidden:!(Wtf.account.companyAccountPref.isQaApprovalFlow),
            hideLabel:!(Wtf.account.companyAccountPref.activateQAApprovalFlow),
            itemCls:"chkboxalign"
        });
        this.producttype= new Wtf.form.FnComboBox({
            fieldLabel:"<span wtf:qtip='"+((this.isFixedAsset)?WtfGlobal.getLocaleText("acc.fixedasset.grouptype.tt"):WtfGlobal.getLocaleText("acc.product.productType.tt"))+"'>"+( (this.isFixedAsset)?'Asset Group Type':WtfGlobal.getLocaleText("acc.product.productType"))+"</span>",//(this.isFixedAsset)?'Asset Group Type':WtfGlobal.getLocaleText("acc.product.productType"),//'Product Type*',
            hiddenName:'producttype',
            id:'producttype'+this.id,
            store:Wtf.productTypeStore,
            //disabled:(this.isFixedAsset ||(this.isEdit && !this.isClone))?true:false,
            disabled:(this.isFixedAsset || this.isEdit || this.isClone )?true:false,
            disabledClass:"newtripcmbss",
            hidden:this.isFixedAsset,
            hideLabel:this.isFixedAsset,
            anchor:'85%',
            allowBlank:false,
            valueField:'id',
            displayField:'name',
            forceSelection: true
        });

       
        this.producttype.on('select',function(combo,record,index){
            
            if (record.data.name == 'Inventory Assembly' || record.data.name == 'Inventory Part' || record.data.name == 'Inventory Non-Sale' || record.data.name == 'Job Work Assembly' || record.data.name == 'Job Work Inventory') {
                WtfGlobal.showFormElement(this.isBinForProduct);
                this.isBinForProduct.enable();
            }
              
            if(record.data.name == 'Service'){
                if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                    this.hsCode.allowBlank = true;
                }else{
                    if(this.productWeightPerStockUom){
                        this.productWeightPerStockUom.setVisible(false);
                        WtfGlobal.hideFormLabel(this.productWeightPerStockUom);
                }

                    if(this.productWeightIncludingPakagingPerStockUom){
                        this.productWeightIncludingPakagingPerStockUom.setVisible(false);
                        WtfGlobal.hideFormLabel(this.productWeightIncludingPakagingPerStockUom);
                    }

                    if(this.productVolumePerStockUom){
                        this.productVolumePerStockUom.setVisible(false);
                        WtfGlobal.hideFormLabel(this.productVolumePerStockUom);
                    }

                    if(this.productVolumeIncludingPakagingPerStockUom){
                        this.productVolumeIncludingPakagingPerStockUom.setVisible(false);
                        WtfGlobal.hideFormLabel(this.productVolumeIncludingPakagingPerStockUom);
                    }
                    
                }
            }else{
                if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                    this.hsCode.allowBlank = false;
                }
                if(this.productWeightPerStockUom){
                    this.productWeightPerStockUom.setVisible(true);
                    WtfGlobal.showLabel(this.productWeightPerStockUom);
                }

                if(this.productWeightIncludingPakagingPerStockUom){
                    this.productWeightIncludingPakagingPerStockUom.setVisible(true);
                    WtfGlobal.showLabel(this.productWeightIncludingPakagingPerStockUom);
                }

                if(this.productVolumePerStockUom){
                    this.productVolumePerStockUom.setVisible(true);
                    WtfGlobal.showLabel(this.productVolumePerStockUom);
                }

                if(this.productVolumeIncludingPakagingPerStockUom){
                    this.productVolumeIncludingPakagingPerStockUom.setVisible(true);
                    WtfGlobal.showLabel(this.productVolumeIncludingPakagingPerStockUom);
                }

                if (record.data.name == 'Inventory Assembly') {
                    this.bomCode.allowBlank = false;
                } else {
                    this.bomCode.allowBlank = true;
                }
                
                if (Wtf.account.companyAccountPref.activateInventoryTab) {
                    if (record.data.name == 'Inventory Assembly' || record.data.name == 'Inventory Part' || record.data.name == 'Inventory Non-Sales' || record.data.name == 'Job work Assembly' || record.data.name == 'Job Work Inventory') {
                        this.isWarehouseForProduct.setDisabled(true);
                        this.isLocationForProduct.setDisabled(true)
                    }
                }
            }
        }, this);
        
        var personRec = new Wtf.data.Record.create([
        {
            name: 'accid'
        }, {
            name: 'accname'
        }, {
            name: 'acccode'
        },{
            name: 'taxId'
        }
        ]);

        var vendorAccStore = new Wtf.data.Store({
            url: "ACCVendor/getVendorsIdNameForCombo.do",
            baseParams: {
                deleted: false,
                nondeleted: true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'totalCount',
                autoLoad: false
            }, personRec)
        });   
                if(Wtf.account.companyAccountPref.activateGroupCompaniesFlag){//for multigroupcompany
  
    this.vendor = new Wtf.common.Select({
        fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.preferedVendor.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.preferedVendor")+"</span>",//WtfGlobal.getLocaleText("acc.product.preferedVendor"),//'Preferred Vendor',
        forceSelection:true,
        allowBlank:true,
        hiddenName:'vendor',
        multiSelect:true,
        store:vendorAccStore,
        anchor:'70%',
//        width:200,
//        hidden:this.isFixedAsset,
//        hideLabel:this.isFixedAsset,
        typeAhead: true,
        //                listAlign:"bl-tl?", //ERP-9826
        valueField:'accid',
        displayField:'accname',
        //                addNoneRecord: true,
        scope: this,
        selectOnFocus:true,
        extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
        listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
        clearTrigger: this.readOnly ? false : true,
        extraComparisionField:'acccode',// type ahead search on acccode as well.
        typeAheadDelay:30000,
        editable:true,
        //        value:'',
        triggerAction:'all',
        mode: 'remote'
//        pageSize:Wtf.ProductCombopageSize
    });  
    
}else{
    this.vendor= new Wtf.form.ExtFnComboBox({
        fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.preferedVendor.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.preferedVendor")+"</span>",//WtfGlobal.getLocaleText("acc.product.preferedVendor"),//'Preferred Vendor',
        hiddenName:'vendor',
        id:'vendor'+this.id,
        store:Wtf.vendorAccStore,
        anchor:'70%',
        allowBlank:true,
        hidden:this.isFixedAsset,
        hideLabel:this.isFixedAsset,
        valueField:'accid',
        displayField:'accname',
        extraComparisionField:'acccode',// type ahead search on acccode as well.
        typeAheadDelay:30000,
        extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
        listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
        forceSelection: true//,
    // addNewFn:this.addPerson.createDelegate(this,[false,null,"vendorwindow",false],true)
    });
}
        
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendor, Wtf.Perm.vendor.create))
            this.vendor.addNewFn=this.addPerson.createDelegate(this,[false,null,"vendorwindow",false],true);
        //        this.cCountInterval=new Wtf.form.NumberField({
        //            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.cycleCountInterval.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.cycleCountInterval") +"</span>",// WtfGlobal.getLocaleText("acc.product.cycleCountInterval"),//"Cycle count interval(in days)* "+WtfGlobal.addLabelHelp("Cycle Count interval defines the time period at which physical counting of Inventory items is to be done. This cross-check helps in identifying discrepancies between Counted quantities,and current records for quantities in the system."),
        //            name: 'ccountinterval',
        //            allowDecimals:false,
        //            allowNegative:false,
        //            anchor:'85%',
        //            hidden:this.isFixedAsset,
        //            hideLabel:this.isFixedAsset,
        //            allowBlank:this.isFixedAsset,
        //            //            value:0,
        //            maxLength:11
        //        });
        //
        //        this.cCountTolerance=new Wtf.form.NumberField({
        //            fieldLabel: "<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.cycleCountTolerance.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.cycleCountTolerance") +"</span>",//WtfGlobal.getLocaleText("acc.product.cycleCountTolerance"),//'Cycle count tolerance (%)*'+WtfGlobal.addLabelHelp("It defines the accepted difference between the Physically Counted Quantity and Stock Level present in Deskera Accounting System. This is defined in percentage terms."),
        //            name: 'ccounttolerance',
        //            allowDecimals:false,
        //            allowNegative:false,
        //            anchor:'85%',
        //            hidden:this.isFixedAsset,
        //            hideLabel:this.isFixedAsset,
        //            allowBlank:this.isFixedAsset,
        //            maxValue:100,
        //            minValue:0,
        //            value:0
        //        });
        
        this.productWeight=new Wtf.form.NumberField({
            fieldLabel: Wtf.account.companyAccountPref.isActiveLandingCostOfItem && (this.isEdit && !this.isClone) ? ( this.record.data && this.record.data.landingcostcategoryusedintransaction ? "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.productForm.landingCostUsedInTransactionProductWeightNotEditableTooltip") + "'>" + WtfGlobal.getLocaleText("acc.erp.ProductWeight") + "</span>" : "<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.erp.ProductWeightTT")+"'>"+ WtfGlobal.getLocaleText("acc.erp.ProductWeight") +"</span>") : "<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.erp.ProductWeightTT")+"'>"+ WtfGlobal.getLocaleText("acc.erp.ProductWeight") +"</span>",// Product Weight
            name: 'productweight',
            id:'productweight'+this.id,
            allowNegative:false,
            anchor:'70%',
            hidden:this.isFixedAsset||(this.producttypeval==Wtf.producttype.service),
            // Disable Product Weight in edit case if landed cost feature check is on and product is already used in PI
            disabled : Wtf.account.companyAccountPref.isActiveLandingCostOfItem && (this.isEdit && !this.isClone) ? (this.record.data && this.record.data.landingcostcategoryusedintransaction ? true : false) : false,
            hideLabel:this.isFixedAsset,
            allowBlank:this.isFixedAsset,
            maxLength:15,
            //maxValue:100,No need of limit for the product weight field.
            minValue:0,
            value:0
        });
        
        this.packingStockUom= new Wtf.form.FnComboBox({
            //fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.uom.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.uom")+"</span>",//WtfGlobal.getLocaleText("acc.product.uom"),//'Unit Of Measure*',
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.stockUoMLabel") +"'>"+WtfGlobal.getLocaleText("acc.product.stockUoMLabel")+"*"+"</span>",
            hiddenName:'uomid',
            store:Wtf.uomStore,
            hidden:this.isFixedAsset,
            hideLabel:this.isFixedAsset,
            anchor:'70%',
//            allowBlank:(this.producttypeval==Wtf.producttype.service) || this.isFixedAsset || Wtf.account.companyAccountPref.UomSchemaType== Wtf.UOMSchema,
            allowBlank:this.isFixedAsset || Wtf.account.companyAccountPref.UomSchemaType== Wtf.UOMSchema,
            valueField:'uomid',
            displayField:'uomname',
            forceSelection: true//,
        // addNewFn:this.showUom.createDelegate(this)
        });
        this.displayUOMStore =new Wtf.data.Store({
            url: "ACCUoM/getDisplayUnitOfMeasure.do",
            method: "POST",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },Wtf.uomRec)
        });
        this.displayUom = new Wtf.form.FnComboBox({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.product.displayUoMLabel") + "'>" + WtfGlobal.getLocaleText("acc.product.displayUoMLabel") + "</span>",
            store: this.displayUOMStore,
            hidden: CompanyPreferenceChecks.displayUOMCheck() ? false : true,
            hideLabel: CompanyPreferenceChecks.displayUOMCheck() ? false : true,
            anchor: '70%',
            disabled: true,
//            allowBlank: (this.producttypeval == Wtf.producttype.service) || this.isFixedAsset || Wtf.account.companyAccountPref.UomSchemaType == Wtf.UOMSchema,
            valueField: 'uomid',
            displayField: 'uomname',
            forceSelection: true,
            readOnly: true
        });
        this.uom= new Wtf.form.FnComboBox({
            //fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.uom.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.uom")+"</span>",//WtfGlobal.getLocaleText("acc.product.uom"),//'Unit Of Measure*',
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.stockUoMLabel") +"'>"+WtfGlobal.getLocaleText("acc.product.stockUoMLabel")+ (this.producttypeval!= Wtf.producttype.service ? "*":"")+"</span>",
            hiddenName:'uomid',
            name:'uomid',
            id:'uomid'+this.id,
            store:Wtf.uomStore,
            hidden:this.isFixedAsset,
            hideLabel:this.isFixedAsset,
            anchor:'70%',
//            allowBlank:(this.producttypeval==Wtf.producttype.service) || this.isFixedAsset || Wtf.account.companyAccountPref.UomSchemaType== Wtf.PackegingSchema,
            allowBlank:this.isFixedAsset || Wtf.account.companyAccountPref.UomSchemaType== Wtf.PackegingSchema,
            valueField:'uomid',
            displayField:'uomname',
            forceSelection: true//,
        // addNewFn:this.showUom.createDelegate(this)
        });  
        this.uom.on('invalid',function( field,msg){//Set padding for invalid icon
            this.setStyleWidth(field,msg);
        },this);
        this.productWeightPerStockUom=new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.productList.productWeightPerStockUom"),//"Product Weight (Kg/Stock UOM)",
            name: 'productweightperstockuom',
            id:'productweightperstockuom'+this.id,
            allowNegative:false,
            anchor:'70%',
            hidden:this.isFixedAsset||(this.producttypeval==Wtf.producttype.service),
            hideLabel:this.isFixedAsset||(this.producttypeval==Wtf.producttype.service),
            maxValue:100,
            minValue:0,
            value:0
        });

        this.productWeightIncludingPakagingPerStockUom=new Wtf.form.NumberField({
            fieldLabel:WtfGlobal.getLocaleText("acc.productList.productWeightIncludingPakagingPerStockUom"),//"Product Weight with Packaging (Kg/Stock UOM)",
            name: 'productweightincludingpakagingperstockuom',
            id:'productweightincludingpakagingperstockuom'+this.id,
            allowNegative:false,
            anchor:'70%',
            hidden:this.isFixedAsset||(this.producttypeval==Wtf.producttype.service),
            hideLabel:this.isFixedAsset||(this.producttypeval==Wtf.producttype.service),
            maxValue:100,
            minValue:0,
            value:0
        });
        
        this.productVolumePerStockUom=new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.productList.productVolumePerStockUom"),//"Product Volume (cubic/Stock UOM)",
            name: 'productvolumeperstockuom',
            id:'productvolumeperstockuom'+this.id,
            allowNegative:false,
            anchor:'70%',
            hidden:this.isFixedAsset||(this.producttypeval==Wtf.producttype.service),
            hideLabel:this.isFixedAsset||(this.producttypeval==Wtf.producttype.service),
            maxValue:100,
            minValue:0,
            value:0
        });

        this.productVolumeIncludingPakagingPerStockUom=new Wtf.form.NumberField({
            fieldLabel:WtfGlobal.getLocaleText("acc.productList.productVolumeIncludingPakagingPerStockUom"),//"Product Volume with Packaging (cubic/Stock UOM)",
            name: 'productvolumeincludingpakagingperstockuom',
            id:'productvolumeincludingpakagingperstockuom'+this.id,
            allowNegative:false,
            anchor:'70%',
            hidden:this.isFixedAsset||(this.producttypeval==Wtf.producttype.service),
            hideLabel:this.isFixedAsset||(this.producttypeval==Wtf.producttype.service),
            maxValue:100,
            minValue:0,
            value:0
        });
        
        this.uom.on('beforeselect', function (combo, selectedRecord, index) {
            return this.doNotAllowToSelectNAUom(selectedRecord);
        }, this);

        this.packingStockUom.on('beforeselect', function (combo, selectedRecord, index) {
            return this.doNotAllowToSelectNAUom(selectedRecord);
        }, this);
        
        this.uom.on('select',function(){
            if (this.multiuom.getValue()){
                this.schemaType.reset();
                this.displayUom.reset();
                this.displayUom.disable();
                this.uomschematypeStore.load({
                    params:{
                    stockuomid:this.uom.getValue()
                    }
                });
            }
            this.packingStockUom.setValue(this.uom.getValue());
            this.setPackagingValue();//loaded transfer and ordering uom sotre according to Stock uom in UOM Schema type  
            this.OrderingUoMCombo.setValue(this.uom.getValue());// set vales of stock uom to ordering and transfer uom
            this.TransferUoMCombo.setValue(this.uom.getValue());
            this.updateWeightFieldsLabel();
            if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){// following fields are only for Indian comapnies. So no need to perfom any action for other company
                this.uomschematypeStore.load({
                    params:{
                        stockuomid:this.uom.getValue()
                    }
                });
            }
        },this)
        this.packingStockUom.on('select',function(){
            this.uom.setValue(this.packingStockUom.getValue());
            this.displayUom.setValue(this.record.data.displayUoMid);
        },this)
        this.dependentTypeRec = Wtf.data.Record.create ([
        {
            name:'id'
        },

        {
            name:'name'
        }
        ]);
        this.dependentTypeStore=new Wtf.data.Store({
            url: "ACCMaster/getMasterPriceDependentItem.do",
            baseParams:{
                mode:2,
                nondeleted: true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.dependentTypeRec)
        });
       
        this.dependentType= new Wtf.form.ComboBox({
            fieldLabel : WtfGlobal.getLocaleText("acc.product.dependentType"),
            hiddenName:'dependentType',
            store:this.dependentTypeStore,
            valueField:'id',
            displayField:'name',
            triggerAction:'all',
            hidden:!Wtf.account.companyAccountPref.dependentField || this.isFixedAsset,
            hideLabel:!Wtf.account.companyAccountPref.dependentField || this.isFixedAsset,
            mode: 'local',
            anchor:'85%',
            typeAhead: true,
            forceSelection: true,
            name:'dependentType'
        });   
        this.dependentTypeStore.load();
        this.dependentTypeStore.on("load",function(){
            if(this.isEdit)
                this.dependentType.setValue(this.record.data['dependenttype']);
        },this);        
        this.dependentTypeStore.load();
     
     var radioBtnArr = [];
       radioBtnArr.push({
        xtype:'radio',
        boxLabel:WtfGlobal.getLocaleText("acc.invoiceList.expand.PID"),
        aotoWidth:true,
        labelSeparator :'',
        inputValue:Wtf.BarcodeGenerator_ProductId,  //2
        name:'barcodegr',
        //                    disabled:(this.isEdit && !this.isClone)?((this.record.data.isUsedInTransaction?true:false)):false,
        id:'bprodId'+this.id
    },{
        xtype:'radio',
        boxLabel:WtfGlobal.getLocaleText("acc.do.barcode"),
        aotoWidth:true,
        labelSeparator :'',
        inputValue:Wtf.BarcodeGenerator_Barcode,  //4 Barcode based on Barcode number Field
        name:'barcodegr',
        id:'brcodeId'+this.id, 
        disabled:true
    },{
        xtype:'radio',
        boxLabel:WtfGlobal.getLocaleText("acc.do.partno"),
        aotoWidth:true,
        labelSeparator :'',
        inputValue:Wtf.BarcodeGenerator_SerialId,  //1
        name:'barcodegr',
        disabled:true,//(this.isEdit && !this.isClone)?((this.record.data.isUsedInTransaction?true:false)):true,
        id:'bserialId'+this.id
    },{
        xtype:'radio',
        boxLabel:WtfGlobal.getLocaleText("acc.do.barcode.batchno"), //5 - Barcode based on Batch Number
        aotoWidth:true,
        labelSeparator :'',
        inputValue:Wtf.BarcodeGenerator_BatchID,  //5
        name:'barcodegr',
        disabled:true,//(this.isEdit && !this.isClone)?((this.record.data.isUsedInTransaction?true:false)):true,
        id:'bbatchId'+this.id
    });
    if(Wtf.account.companyAccountPref.SKUFieldParm){
        radioBtnArr.push({ 
            hidden:!Wtf.account.companyAccountPref.SKUFieldParm,
            xtype:'radio',
            boxLabel:(Wtf.account.companyAccountPref.SKUFieldParm)?(Wtf.account.companyAccountPref.SKUFieldRename!="" && Wtf.account.companyAccountPref.SKUFieldRename!= undefined)?Wtf.account.companyAccountPref.SKUFieldRename:WtfGlobal.getLocaleText("acc.product.skufield"):WtfGlobal.getLocaleText("acc.product.skufield"),
            aotoWidth:true,
            inputValue: Wtf.BarcodeGenerator_SKUField, //3
            name:'barcodegr',
            disabled:true,//(this.isEdit && !this.isClone)?((this.record.data.isUsedInTransaction?true:false)):true,
            id:'bSkuId'+this.id
            });
    }
                
        this.barcodeFieldset = new Wtf.form.FieldSet({
            title:WtfGlobal.getLocaleText("acc.product.generateBarcodUsing"), 
            autoHeight: true,
            layout:'form',
            hidden:!Wtf.account.companyAccountPref.generateBarcodeParm,
            border: false,
            items:[{
                border:false,
                items:radioBtnArr
            }]
        });
        this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            compId: "northForm" + this.id,
            autoHeight: true,
            parentcompId: this.id,
            moduleid: this.moduleid,
            isEdit: this.isEdit,
            record: this.record
        });
       
        this.intervalStore = new Wtf.data.SimpleStore({
            fields :['id', 'name'],
            data:[['1','1'],['2','2'],['4','4']]
        });

        this.interValCombo= new Wtf.form.ComboBox({            
            triggerAction:'all',
            mode: 'local',
            fieldLabel:"Number of billing periods per hour",
            valueField:'id',
            displayField:'name',
            store:this.intervalStore,
            hidden:!Wtf.account.companyAccountPref.dependentField || this.isFixedAsset,
            hideLabel:!Wtf.account.companyAccountPref.dependentField || this.isFixedAsset,
            width:240,
            typeAhead: true,
            forceSelection: true,
            name:'timeinterval',
            hiddenName:'timeinterval'
            
        });
        if(!this.isEdit) 
            this.interValCombo.setValue("1");
        else{
            this.interValCombo.setValue(this.record.data.timeinterval);
        }
        this.isInterval= new Wtf.form.Checkbox({
            name:'intervalField',
            fieldLabel:WtfGlobal.getLocaleText("acc.product.addInterval"),//'Make available in CRM',
            checked:false,      
            hidden:!Wtf.account.companyAccountPref.dependentField || this.isFixedAsset,
            hideLabel:!Wtf.account.companyAccountPref.dependentField || this.isFixedAsset,
            itemCls:"chkboxalign"
        })
        this.addShipLengthqty= new Wtf.form.Checkbox({
            name:'addshiplentheithqty',
            fieldLabel:WtfGlobal.getLocaleText("acc.product.addShipLengthWithQty"),//'Make available in CRM',
            checked:false,      
            hidden:!Wtf.account.companyAccountPref.dependentField || this.isFixedAsset,
            hideLabel:!Wtf.account.companyAccountPref.dependentField || this.isFixedAsset,
            itemCls:"chkboxalign"
        })
        
        this.noOfServiceQTY= new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.noofqty"),//'Lead Time(in days)*'+WtfGlobal.addLabelHelp("The amount of time between the placing of an order and the receipt of the goods ordered."),
            name: 'noofqty',
            anchor:'85%',
            maxLength:3,
            hidden:!Wtf.account.companyAccountPref.dependentField || this.isFixedAsset,
            hideLabel:!Wtf.account.companyAccountPref.dependentField || this.isFixedAsset,
            allowDecimals:false,
            allowNegative:false,
            maxValue:3
        });
        this.extraQuantity = new Wtf.Panel({
            autoHeight: true,
            width:'97%',
            layout:'form',
            border: false
        });
        chkUomload();
        this.isFirstTime=true;
        this.noOfServiceQTY.on("change",this.addQuantityFields,this);
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.uom, Wtf.Perm.uom.edit))
            this.uom.addNewFn=this.showUom.createDelegate(this);
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.uom, Wtf.Perm.uom.edit))
            this.packingStockUom.addNewFn=this.showUom.createDelegate(this);
        /**
         * change store if show limilted accounts flag is activated and contains 1 or more than 1 mapping
         */
        var productSalesAccountStore = Wtf.salesAccStore;
        var productPurchaseAccountStore = this.purchaseAccStore;
        if(Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref != null){
            if(Wtf.account.companyAccountPref.columnPref.isLimitedProductSalesAccounts != undefined && Wtf.account.companyAccountPref.columnPref.isLimitedProductSalesAccounts != null && Wtf.account.companyAccountPref.columnPref.isLimitedProductSalesAccounts){
                productSalesAccountStore = Wtf.productSalesLimitedAccStore != undefined && Wtf.productSalesLimitedAccStore.data.length > 0 ? Wtf.productSalesLimitedAccStore : Wtf.salesAccStore;
            }
            if(Wtf.account.companyAccountPref.columnPref.isLimitedProductPurchaseAccounts != undefined && Wtf.account.companyAccountPref.columnPref.isLimitedProductPurchaseAccounts != null && Wtf.account.companyAccountPref.columnPref.isLimitedProductPurchaseAccounts){
                productPurchaseAccountStore = Wtf.productPurchaseLimitedAccStore != undefined && Wtf.productPurchaseLimitedAccStore.data.length > 0 ? Wtf.productPurchaseLimitedAccStore : this.purchaseAccStore;
            }
        }
        this.salesAcc=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.salesAcc.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.salesAcc")+"</span>",//WtfGlobal.getLocaleText("acc.product.salesAcc"),//'Sales Account*',
            store:productSalesAccountStore,
            name:'salesaccountid',
            id:'salesaccountid'+this.id,
            anchor:'70%',
            hiddenName:'salesaccountid',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAhead: true,
            hideLabel:this.isFixedAsset,
            hidden:this.isFixedAsset,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            typeAheadDelay:30000,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: this.isFixedAsset,
            disabled :(this.isUsedInTransaction && this.isEdit && !this.isClone && this.producttypeval==Wtf.producttype.assembly) ? true : false,
            //  addNewFn: this.addAccount.createDelegate(this,[Wtf.salesAccStore,false,false,true],true),
            listeners:{
                select:{
                    fn:function(c){
                        if(!this.salesReturnAcc.getValue())
                            this.salesReturnAcc.setValue(c.getValue());
                    },
                    scope:this
                }
            }
        });
        this.salesAcc.on('invalid',function( field,msg){//Set padding for invalid icon
            this.setStyleWidth(field,msg);
        },this);
        this.salesAcc.on('beforeselect',function(combo,record,index){
            return validateSelection(combo,record,index);
        },this);
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.createcoa))
            this.salesAcc.addNewFn=this.addAccount.createDelegate(this,[Wtf.salesAccStore,false,false,true],true);
        
                
        
        this.salesAccounts=new Wtf.form.FieldSet({
            title:'Sales Accounts',//Sales Accounts,
            width:400,
            hidden:(this.isFixedAsset||Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            items:[this.salesAcc]
        });
        
        this.salesReturnAcc=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.salesReturnAcc.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.salesReturnAcc")+"</span>",//WtfGlobal.getLocaleText("acc.product.salesReturnAcc"),//'Sales Return Account*',
            store:productSalesAccountStore,
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            typeAhead: true,
            hideLabel:this.isFixedAsset,
            hidden:this.isFixedAsset,
            isAccountCombo:true,
            name:'salesretaccountid',
            id:'salesretaccountid'+this.id,
            anchor:'70%',
            hiddenName:'salesretaccountid',
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: this.isFixedAsset,
            disabled :(this.isUsedInTransaction && this.isEdit && !this.isClone && this.producttypeval==Wtf.producttype.assembly) ? true : false
        // addNewFn: this.addAccount.createDelegate(this,[Wtf.salesAccStore,true,false],true)
        });
        this.salesReturnAcc.on('invalid',function( field,msg){//Set padding for invalid icon
            this.setStyleWidth(field,msg);
        },this);
        this.salesReturnAcc.on('beforeselect',function(combo,record,index){
            return validateSelection(combo,record,index);
        },this);   
        
        this.salesAccountsReturn=new Wtf.form.FieldSet({
            title:'Sales Return Accounts',//Sales Return Accounts
            width:400,
            hidden:(this.isFixedAsset||Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            items:[this.salesReturnAcc]
        });
        
         this.inputVATSales=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='Input VAT'>Input VAT</span>",//WtfGlobal.getLocaleText("acc.product.purchaseAcc"),//'Purchase Account*',
            store:this.purchaseAccStore,
            anchor:'70%',
            name:'inputVATaccountSalesid',
            id:'inputVATaccountSalesid'+this.id,
            hiddenName:'inputVATaccountSalesid',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            typeAhead: true,
            hideLabel:(this.isFixedAsset||Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            hidden:(this.isFixedAsset||Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: true
        });
       
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.createcoa))
              this.inputVATSales.addNewFn=this.addAccount.createDelegate(this,[this.purchaseAccStore,false,false,false,true],true);
//            this.inputVAT.addNewFn=this.isInventory.createDelegate(this);//this.uom.addNewFn=this.isInventory.createDelegate(this);
        this.inputVATSales.on('beforeselect',function(combo,record,index){
            return validateSelection(combo,record,index);
        },this);
         
         this.cstVATattwoSales=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='CST @2%'>CST @2%</span>",//WtfGlobal.getLocaleText("acc.product.purchaseAcc"),//'Purchase Account*',
            store:this.purchaseAccStore,
            anchor:'70%',
            name:'cstVATattwoSalesID',
            id:'cstVATattwoSalesID'+this.id,
            hiddenName:'cstVATattwoSalesID',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            hideLabel:(this.isFixedAsset||Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            hidden:(this.isFixedAsset||Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            typeAhead: true,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: true
        });
       
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.createcoa))
            this.cstVATattwoSales.addNewFn=this.addAccount.createDelegate(this,[this.purchaseAccStore,false,false,false,true],true);
            this.cstVATattwoSales.on('beforeselect',function(combo,record,index){
            return validateSelection(combo,record,index);
        },this);
//
        this.cstVATSales=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='CST @ ...%'>CST @ ...%</span>",//WtfGlobal.getLocaleText("acc.product.purchaseAcc"),//'Purchase Account*',
            store:this.purchaseAccStore,
            anchor:'70%',
            name:'cstVATSalesID',
            id:'cstVATSalesID'+this.id,
            hiddenName:'cstVATSalesID',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            typeAhead: true,
            hideLabel:(this.isFixedAsset||Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            hidden:(this.isFixedAsset||Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: true
        });
//
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.createcoa))
            this.cstVATSales.addNewFn=this.addAccount.createDelegate(this,[this.purchaseAccStore,false,false,false,true],true);
        //            this.cstVAT.addNewFn=this.isInventory.createDelegate(this);//this.uom.addNewFn=this.isInventory.createDelegate(this);
        this.cstVATSales.on('beforeselect',function(combo,record,index){
            return validateSelection(combo,record,index);
        },this);
        
        this.SalesSideTaxAccounts=new Wtf.form.FieldSet({
            title:'Sales Side Tax Accounts',//Sales Side Tax Accounts
            width:400,
//            hidden:(this.isFixedAsset||Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            hidden:true,
            items:[this.inputVATSales,this.cstVATattwoSales,this.cstVATSales]
//            items:[this.inputVAT,this.cstVatattwo,this.cstVAT]
        });
        
//        
//    
 
        
        
        
        this.assetControllingAcc=new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.fixed.asset.controlling.account"),//'Controlling Account*',
            store:this.FixedAssetControllingAccStore,
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            typeAhead: true,
            isAccountCombo:true,
            name:'assetControllingAccountId',
            anchor:'85%',
            hiddenName:'assetControllingAccountId',
            hideLabel:!this.isFixedAsset,
            hidden:!this.isFixedAsset,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: !this.isFixedAsset
        });
        
        this.assetControllingAcc.on('beforeselect',function(combo,record,index){
            return validateSelection(combo,record,index);
        },this);
        this.salesRevenueRecognitionAccount=new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.product.salesRevenueRecognitionAccount"),//' Sales Return Account*',
            store:Wtf.salesAccStore,
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            hidden:this.isFixedAsset || ((Wtf.account.companyAccountPref.isDeferredRevenueRecognition!=undefined&&!Wtf.account.companyAccountPref.isDeferredRevenueRecognition)||Wtf.account.companyAccountPref.isDeferredRevenueRecognition==undefined),
            hideLabel:this.isFixedAsset || ((Wtf.account.companyAccountPref.isDeferredRevenueRecognition!=undefined&&!Wtf.account.companyAccountPref.isDeferredRevenueRecognition)||Wtf.account.companyAccountPref.isDeferredRevenueRecognition==undefined),            
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            typeAhead: true,
            name:'salesRevenueRecognitionAccountid',
            anchor:'85%',
            hiddenName:'salesRevenueRecognitionAccountid',
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: false

        });
        this.salesRevenueRecognitionAccount.on('beforeselect',function(combo,record,index){
            return validateSelection(combo,record,index);
        },this);
        this.revenueRecognitionProcess=new Wtf.form.Checkbox({                               
            fieldLabel:WtfGlobal.getLocaleText("acc.product.revenueRecognitionProcess"),//Allow Revenue Recognition
            name:'revenueRecognitionProcess',
            hidden:this.isFixedAsset || ((Wtf.account.companyAccountPref.isDeferredRevenueRecognition!=undefined&&!Wtf.account.companyAccountPref.isDeferredRevenueRecognition)||Wtf.account.companyAccountPref.isDeferredRevenueRecognition==undefined),
            hideLabel:this.isFixedAsset || ((Wtf.account.companyAccountPref.isDeferredRevenueRecognition!=undefined&&!Wtf.account.companyAccountPref.isDeferredRevenueRecognition)||Wtf.account.companyAccountPref.isDeferredRevenueRecognition==undefined),                   
            itemCls:"chkboxalign"
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.edit))
            this.salesReturnAcc.addNewFn=this.addAccount.createDelegate(this,[Wtf.salesAccStore,false,false,true],true);
        this.purchaseAcc=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.purchaseAcc.tt")+"'>"+ ((Wtf.account.companyAccountPref.activateMRPManagementFlag || Wtf.account.companyAccountPref.inventoryValuationType == "1")?WtfGlobal.getLocaleText("acc.field.accrued.purchase.account"):WtfGlobal.getLocaleText("acc.product.purchaseAcc"))+"</span>",//WtfGlobal.getLocaleText("acc.product.purchaseAcc"),//'Purchase Account*',
            store:productPurchaseAccountStore,
            anchor:'70%',
            name:'purchaseaccountid',
            id:'purchaseaccountid'+this.id,
            hiddenName:'purchaseaccountid',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            typeAhead: true,
            hideLabel:this.isFixedAsset,
            hidden:this.isFixedAsset,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: this.isFixedAsset,
            disabled :(this.isUsedInTransaction && this.isEdit && !this.isClone && this.producttypeval==Wtf.producttype.assembly) ? true : false,
            //addNewFn:this.isInventory.createDelegate(this),
            listeners:{
                select:{
                    fn:function(c,record){
                        if(!this.purchaseReturnAcc.getValue())
                            this.purchaseReturnAcc.setValue(c.getValue());
                        if (record != undefined) {
                            this.purchaseAccountRec = record;
                        }
                    },
                    scope:this
                }
            }
        });
        this.purchaseAcc.on('invalid',function( field,msg){//Set padding for invalid icon
            this.setStyleWidth(field,msg);
        },this);
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.createcoa))
              this.purchaseAcc.addNewFn=this.addAccount.createDelegate(this,[this.purchaseAccStore,false,false,false,true],true);
//            this.purchaseAcc.addNewFn=this.isInventory.createDelegate(this);//this.uom.addNewFn=this.isInventory.createDelegate(this);
        this.purchaseAcc.on('beforeselect',function(combo,record,index){
            return validateSelection(combo,record,index);
        },this);
        
        this.purchasesAccounts=new Wtf.form.FieldSet({
            title:'Purchases Accounts',//Purchases Accounts
            width:400,
            hidden:(this.isFixedAsset||Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            items:[this.purchaseAcc]
        });
        
//        
         this.inputVAT=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='Input VAT'>Input VAT</span>",//WtfGlobal.getLocaleText("acc.product.purchaseAcc"),//'Purchase Account*',
            store:this.purchaseAccStore,
            anchor:'70%',
            name:'inputVATaccountid',
            id:'inputVATaccountid'+this.id,
            hiddenName:'inputVATaccountid',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            typeAhead: true,
            hideLabel:(this.isFixedAsset||Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            hidden:(this.isFixedAsset||Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: true
        });
       
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.createcoa))
              this.inputVAT.addNewFn=this.addAccount.createDelegate(this,[this.purchaseAccStore,false,false,false,true],true);
//            this.inputVAT.addNewFn=this.isInventory.createDelegate(this);//this.uom.addNewFn=this.isInventory.createDelegate(this);
        this.inputVAT.on('beforeselect',function(combo,record,index){
            return validateSelection(combo,record,index);
        },this);
         
         this.cstVATattwo=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='CST @2%'>CST @2%</span>",//WtfGlobal.getLocaleText("acc.product.purchaseAcc"),//'Purchase Account*',
            store:this.purchaseAccStore,
            anchor:'70%',
            name:'cstVATattwoID',
            id:'cstVATattwoID'+this.id,
            hiddenName:'cstVATattwoID',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            hideLabel:(this.isFixedAsset||Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            hidden:(this.isFixedAsset||Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            typeAhead: true,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: true
        });
       
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.createcoa))
            this.cstVATattwo.addNewFn=this.addAccount.createDelegate(this,[this.purchaseAccStore,false,false,false,true],true);
            this.cstVATattwo.on('beforeselect',function(combo,record,index){
            return validateSelection(combo,record,index);
        },this);
//
        this.cstVAT=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='CST @ ...%'>CST @ ...%</span>",//WtfGlobal.getLocaleText("acc.product.purchaseAcc"),//'Purchase Account*',
            store:this.purchaseAccStore,
            anchor:'70%',
            name:'cstVATID',
            id:'cstVATID'+this.id,
            hiddenName:'cstVATID',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            typeAhead: true,
            hideLabel:(this.isFixedAsset||Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            hidden:(this.isFixedAsset||Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: true
        });
//
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.createcoa))
            this.cstVAT.addNewFn=this.addAccount.createDelegate(this,[this.purchaseAccStore,false,false,false,true],true);
        //            this.cstVAT.addNewFn=this.isInventory.createDelegate(this);//this.uom.addNewFn=this.isInventory.createDelegate(this);
        this.cstVAT.on('beforeselect',function(combo,record,index){
            return validateSelection(combo,record,index);
        },this);
        
        this.purchasesSideTaxAccounts=new Wtf.form.FieldSet({
            title:'Purchase Side Tax Accounts',//Purchase Side Tax Accounts
            width:400,
//            hidden:(this.isFixedAsset||Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            hidden:true,
            items:[this.inputVAT,this.cstVATattwo,this.cstVAT]
//            items:[this.inputVAT,this.cstVatattwo,this.cstVAT]
        });
        
//        
//    
 
        this.purchaseReturnAcc=new Wtf.form.ExtFnComboBox({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.product.purchaseReturnAcc.tt") + "'>" + ((Wtf.account.companyAccountPref.activateMRPManagementFlag || Wtf.account.companyAccountPref.inventoryValuationType == "1") ? WtfGlobal.getLocaleText("acc.field.accrued.purchasereturn.account") : WtfGlobal.getLocaleText("acc.product.purchaseReturnAcc")) + "</span>", //WtfGlobal.getLocaleText("acc.product.purchaseReturnAcc"),//'Purchase Return Account*',
            store:productPurchaseAccountStore,
            anchor:'70%',
            name:'purchaseretaccountid',
            id:'purchaseretaccountid'+this.id,
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            typeAhead: true,
            isAccountCombo:true,
            hideLabel:this.isFixedAsset,
            hidden:this.isFixedAsset,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            hiddenName:'purchaseretaccountid',
            valueField:'accid',
            forceSelection: true,
            displayField:'accname',
            allowBlank: this.isFixedAsset,
            disabled :(this.isUsedInTransaction && this.isEdit && !this.isClone && this.producttypeval==Wtf.producttype.assembly) ? true : false
        //            addNewFn: this.addAccount.createDelegate(this,[this.purchaseAccStore,false,true],true)
        });
        this.purchaseReturnAcc.on('invalid',function( field,msg){//Set padding for invalid icon
            this.setStyleWidth(field,msg);
        },this);
        this.stockAdjustmentAcc = new Wtf.form.ExtFnComboBox({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.stock.account") + "'>" +  WtfGlobal.getLocaleText("acc.field.stock.account")  + "*</span>", // Stock Adjustment Account*
            store: this.purchaseAccStore,
            anchor: '70%',
            name: 'stockadjustmentaccountid',
            id: 'stockadjustmentaccountid' + this.id,
            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode'] : [],
            extraComparisionField: 'acccode', // type ahead search on acccode as well.
            typeAheadDelay: 30000,
            typeAhead: true,
            isAccountCombo: true,
            hideLabel: !(Wtf.account.companyAccountPref.activateMRPManagementFlag || Wtf.account.companyAccountPref.inventoryValuationType == "1"),
            hidden: !(Wtf.account.companyAccountPref.activateMRPManagementFlag || Wtf.account.companyAccountPref.inventoryValuationType == "1"),
            listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 350 : 240,
            hiddenName: 'stockadjustmentaccountid',
            valueField: 'accid',
            forceSelection: true,
            displayField: 'accname',
            allowBlank: !(Wtf.account.companyAccountPref.activateMRPManagementFlag || Wtf.account.companyAccountPref.inventoryValuationType == "1"),
            disabled :(this.isUsedInTransaction && this.isEdit && !this.isClone && this.producttypeval==Wtf.producttype.assembly) ? true : false
        });    
         
        this.inventoryAcc = new Wtf.form.ExtFnComboBox({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.inventory.account") + "'>" +  WtfGlobal.getLocaleText("acc.field.inventory.account")  + "*</span>", // Inventory Account*
            store: this.purchaseAccStore,
            anchor: '70%',
            name: 'inventoryaccountid',
            id: 'inventoryaccountid' + this.id,
            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode'] : [],
            extraComparisionField: 'acccode', // type ahead search on acccode as well.
            typeAheadDelay: 30000,
            typeAhead: true,
            isAccountCombo: true,
            hideLabel: !(Wtf.account.companyAccountPref.activateMRPManagementFlag || Wtf.account.companyAccountPref.inventoryValuationType == "1"),
            hidden: !(Wtf.account.companyAccountPref.activateMRPManagementFlag || Wtf.account.companyAccountPref.inventoryValuationType == "1"),
            listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 350 : 240,
            hiddenName: 'inventoryaccountid',
            valueField: 'accid',
            forceSelection: true,
            displayField: 'accname',
            allowBlank: !(Wtf.account.companyAccountPref.activateMRPManagementFlag || Wtf.account.companyAccountPref.inventoryValuationType == "1"),
            disabled :(this.isUsedInTransaction && this.isEdit && !this.isClone && this.producttypeval==Wtf.producttype.assembly) ? true : false
        });    
         
        this.cogsAcc = new Wtf.form.ExtFnComboBox({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.cogs.account") + "'>" +  WtfGlobal.getLocaleText("acc.field.cogs.account")  + "*</span>", // Cost of Goods Sold Account*
            store: this.purchaseAccStore,
            anchor: '70%',
            name: 'cogsaccountid',
            id: 'cogsaccountid' + this.id,
            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode'] : [],
            extraComparisionField: 'acccode', // type ahead search on acccode as well.
            typeAheadDelay: 30000,
            typeAhead: true,
            isAccountCombo: true,
            hideLabel: !(Wtf.account.companyAccountPref.activateMRPManagementFlag || Wtf.account.companyAccountPref.inventoryValuationType == "1"),
            hidden: !(Wtf.account.companyAccountPref.activateMRPManagementFlag || Wtf.account.companyAccountPref.inventoryValuationType == "1"),
            listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 350 : 240,
            hiddenName: 'cogsaccountid',
            valueField: 'accid',
            forceSelection: true,
            displayField: 'accname',
            allowBlank: !(Wtf.account.companyAccountPref.activateMRPManagementFlag || Wtf.account.companyAccountPref.inventoryValuationType == "1"),
            disabled :(this.isUsedInTransaction && this.isEdit && !this.isClone && this.producttypeval==Wtf.producttype.assembly) ? true : false
        });    
        
        this.purchasesAccountsReturn=new Wtf.form.FieldSet({
            title:'Purchases Return Accounts',//Purchases Return Accounts
            width:400,
            hidden:(this.isFixedAsset||Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.account.companyAccountPref.enablevatcst),
            items:[this.purchaseReturnAcc]
        });

        this.purchaseReturnAcc.on('beforeselect',function(combo,record,index){
            return validateSelection(combo,record,index);
        },this);
        
        this.locationEditor = new Wtf.form.FnComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.field.Defaultlocation.tt")+"'>"+ WtfGlobal.getLocaleText("acc.field.Defaultlocation")+"</span>",//WtfGlobal.getLocaleText("acc.field.Defaultlocation"),
            valueField:'id',
            displayField:'name',
            store:this.locationStore,
            anchor:'70%',
            typeAhead: true,
            forceSelection: true,
            name:'location',
            id:'location'+this.id,
            hidden:this.isFixedAsset,
            hideLabel:this.isFixedAsset,
            hiddenName:'location'

        });
        ///        if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.create))  //default location
        ///            this.locationEditor.addNewFn=this.addMasterItem.createDelegate(this,["location",Wtf.locationStore]);//this.uom.addNewFn  
         this.locationEditor.on('invalid',function( field,msg){//Set padding for invalid icon
            this.setStyleWidth(field,msg);
        },this);
        this.wareHouseEditor = new Wtf.form.FnComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.field.DefaultWarehouse.tt")+"'>"+ WtfGlobal.getLocaleText("acc.field.DefaultWarehouse")+"</span>",//WtfGlobal.getLocaleText("acc.field.DefaultWarehouse"),
            valueField:'store_id',
            displayField:'fullname',
            store:this.wareHouseStore,
            anchor:'70%',
            typeAhead: true,
            forceSelection: true,
            hidden:this.isFixedAsset,
            hideLabel:this.isFixedAsset,
            id:'warehouse'+this.id,
            name:'warehouse',
            hiddenName:'warehouse'

        });
         this.wareHouseEditor.on('invalid',function( field,msg){//Set padding for invalid icon
            this.setStyleWidth(field,msg);
        },this);
        this.wareHouseStore.load();
        
        this.wareHouseEditor.on("select",function(){
            this.locationStore.removeAll();
            this.locationEditor.reset(); 
//            this.locationEditor.allowBlank=false; 

            this.locationStore.load({
                params:{
                    storeid:this.wareHouseEditor.getValue()
                }
            });
        },this);
        this.wareHouseEditor.on('blur', function() {
            if (this.wareHouseEditor.getRawValue() == '') {
                this.wareHouseEditor.clearValue();
                this.wareHouseEditor.setValue('');
            }
        }, this);
        
        this.licenseTypeStore= new Wtf.data.SimpleStore({
            fields: ['id', 'name'],
            data : [['NONE','None'],['LOCAL','Local'],['OVERSEAS','Overseas']]
        });


        this.licensetype =new Wtf.form.FnComboBox({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.LicenseType")+"'>"+WtfGlobal.getLocaleText("acc.product.LicenseType")+"</span>",
            hiddenName:'licensetype',
            name:'licensetype',
            id:'licensetype'+this.id,
            store:this.licenseTypeStore,
            anchor:'85%',
            valueField:'id',
            displayField:'name',
            selectOnFocus:true,
            forceSelection:true,
            mode: 'local',
            //allowBlank:false,
            triggerAction:'all',
            typeAhead: true
            
        }); 
        
        this.licensecode=new Wtf.form.TextField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.LicenseCode") +"'>"+WtfGlobal.getLocaleText("acc.product.LicenseCode")+"</span>",
            name:'licensecode',
            id:'licensecode'+this.id,
            anchor:'85%'
        }); 
        
        this.productBrandCombo =  new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.masterConfig.53"), // "Product Brand",
            name: "productBrandCombo",     
            id: 'productBrandCombo' + this.id,
            store: Wtf.productBrandStore,
            anchor: "85%",
            allowBlank: true,
            valueField: 'id',
            displayField: 'name',
            mode: 'remote',
            triggerAction: 'all',
            forceSelection: true,
            addNoneRecord: true,
            disabled :(this.isUsedInTransaction && this.isEdit && !this.isClone && this.producttypeval==Wtf.producttype.assembly) ? true : false,
            extraFields:[]
        });
            this.gstHistory = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.gsthistory.producttaxclassbtn"),
            tooltip: WtfGlobal.getLocaleText("acc.gsthistory.taxclasstoolip"),
            id: "saveasdraft" + this.id,
            disabled: (this.isEdit && !this.isClone) ?false:true,
            hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            scope: this,
            style: 'margin-left:165px;margin-top:7px;margin-bottom:10px;',
            handler: function() {
                this.openHistoryWindow();
            },
            iconCls: 'pwnd save'
        });

    chkLandingCostCategoryload();    
    Wtf.landingCostCategoryStore.on('load',function(){
        if(this.isEdit){
            this.landingCostCategoryCombo.setValue(this.record.data.landingcostcategoryid);
        }
    },this);
    this.LCCComboconfig = {
        hiddenName:"landingCostCategory",  
        store: Wtf.landingCostCategoryStore,
        valueField: 'id',
        displayField: 'name',
        mode: 'local',
        typeAhead: true,
        selectOnFocus:true,                            
        allowBlank:true,
        triggerAction:'all',
        scope:this
    };

    this.landingCostCategoryCombo = new Wtf.common.Select(Wtf.applyIf({
        multiSelect:true,
        fieldLabel: Wtf.account.companyAccountPref.isActiveLandingCostOfItem && (this.isEdit && !this.isClone) ? ( this.record.data && this.record.data.landingcostcategoryusedintransaction ? "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.productForm.landingCostUsedInTransactionTooltip") + "'>" + WtfGlobal.getLocaleText("acc.invoice.landingCostCategory") + "</span>" : WtfGlobal.getLocaleText("acc.invoice.landingCostCategory")) : WtfGlobal.getLocaleText("acc.invoice.landingCostCategory"), //"Landed Cost Category", // "Product Brand",
        name: "landingCostCategoryCombo",     
        id: 'landingCostCategoryCombo' + this.id,
        hidden:!Wtf.account.companyAccountPref.isActiveLandingCostOfItem,
        hideLabel:!Wtf.account.companyAccountPref.isActiveLandingCostOfItem, 
        // Disable Product Ladnded Cost Category in edit case if landed cost feature check is on and product is already used in PI
        disabled : Wtf.account.companyAccountPref.isActiveLandingCostOfItem && (this.isEdit && !this.isClone) ? (this.record.data && this.record.data.landingcostcategoryusedintransaction ? true : false) : false,
        hiddenName:"landingCostCategoryCombo",
        anchor: "85%",
        forceSelection: true
    },this.LCCComboconfig));
        
        
        
      if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.create)) {
            this.productBrandCombo.addNewFn= this.addBrand.createDelegate(this);
      }
       
        ///if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.create))   //this.uom.addNewFn  
        //this.wareHouseEditor.addNewFn= (this.businessPerson=="Customer")?this.addMaster.createDelegate(this,[7,Wtf.CustomerCategoryStore]):this.addMaster.createDelegate(this,[8,Wtf.VendorCategoryStore])        
        /// this.wareHouseEditor.addNewFn=this.addMasterItem.createDelegate(this,["warehouse",Wtf.wareHouseStore]);
       
        this.isBatchForProduct=new Wtf.form.Checkbox({ //option to check wether batch is compulsory or not
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isBatchCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isBatchCompulsory")+"</span>",
            name:'isBatchForProduct',
            id:'isBatchForProduct'+this.id,
            checked:false,
            disabled:(this.isEdit && !this.isClone)?((this.record.data.isUsedInBatchSerial?true:false)):false,
            hidden:!Wtf.account.companyAccountPref.isBatchCompulsory,
            hideLabel:!Wtf.account.companyAccountPref.isBatchCompulsory,
            itemCls:"chkboxalign",
            listeners: {
                scope: this,
                check: function () {
                    if (this.isBatchForProduct.getValue()) {
                        Wtf.getCmp('bbatchId' + this.id).enable();
                    } else {
                        Wtf.getCmp('bbatchId' + this.id).setValue(false);
                        Wtf.getCmp('bbatchId' + this.id).disable();                        
                    }
                }
            }
        });
        this.isBatchForProduct.on("change", function() {
            this.quantity.setValue(0);
            this.batchDetails="";
         }, this);
        this.isLocationForProduct=new Wtf.form.Checkbox({ //option to check wether batch is compulsory or not
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isLocationCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isLocationCompulsory")+"</span>",
            name:'isLocationForProduct',
            id:"isLocationForProduct"+this.id,
            checked:(this.isEdit && !this.isClone)?(this.record.data.isLocationForProduct):Wtf.account.companyAccountPref.isLocationCompulsory,
            disabled:(this.isEdit && !this.isClone)?((this.record.data.isUsedInBatchSerial?true:false)):Wtf.account.companyAccountPref.activateInventoryTab,
            hidden:!Wtf.account.companyAccountPref.isLocationCompulsory,
            hideLabel:!Wtf.account.companyAccountPref.isLocationCompulsory,
            itemCls:"chkboxalign"
        });
        this.isLocationForProduct.on("check", function(chk, checked) {
            if (checked == true) {
                this.locationEditor.allowBlank = false;
                this.locationEditor.setDisabled(false); 
                this.locationEditor.validate();
            } else {
                this.locationEditor.allowBlank = true;
                this.locationEditor.setDisabled(true); 
                this.locationEditor.validate();
            }
        }, this);
        this.isWarehouseForProduct=new Wtf.form.Checkbox({ //option to check wether batch is compulsory or not
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isWarehouseCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isWarehouseCompulsory")+"</span>",
            name:'isWarehouseForProduct',
            id:'isWarehouseForProduct'+this.id,
            checked:(this.isEdit && !this.isClone)?(this.record.data.isWarehouseForProduct):Wtf.account.companyAccountPref.isWarehouseCompulsory,
            disabled:(this.isEdit && !this.isClone)?((this.record.data.isUsedInBatchSerial?true:false)):Wtf.account.companyAccountPref.activateInventoryTab,
            hidden:!Wtf.account.companyAccountPref.isWarehouseCompulsory,
            hideLabel:!Wtf.account.companyAccountPref.isWarehouseCompulsory,
            itemCls:"chkboxalign"
        });
        this.isWarehouseForProduct.on("check", function(chk, checked) {
            if (checked == true) {
                this.wareHouseEditor.allowBlank = false;
                this.wareHouseEditor.setDisabled(false); 
                this.wareHouseEditor.validate();
            } else {
                this.wareHouseEditor.allowBlank = true;
                this.wareHouseEditor.setDisabled(true); 
                this.wareHouseEditor.validate();
            }
        }, this);
        this.isRowForProduct=new Wtf.form.Checkbox({ //option to check wether batch is compulsory or not
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isRowCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isRowCompulsory")+"</span>",
            name:'isRowForProduct',
            checked:false,
            disabled:(this.isEdit && !this.isClone)?((this.record.data.isUsedInBatchSerial?true:false)):false,
            hidden:!Wtf.account.companyAccountPref.isRowCompulsory,
            hideLabel:!Wtf.account.companyAccountPref.isRowCompulsory,
            itemCls:"chkboxalign"
        });
         this.isRowForProduct.on("change", function() {
              this.quantity.setValue(0);
              this.batchDetails="";
         }, this);
        this.isRackForProduct=new Wtf.form.Checkbox({ //option to check wether batch is compulsory or not
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isRackCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isRackCompulsory")+"</span>",
            name:'isRackForProduct',
            checked:false,
            disabled:(this.isEdit && !this.isClone)?((this.record.data.isUsedInBatchSerial?true:false)):false,
            hidden:!Wtf.account.companyAccountPref.isRackCompulsory,
            hideLabel:!Wtf.account.companyAccountPref.isRackCompulsory,
            itemCls:"chkboxalign"
        });
        this.isRackForProduct.on("change", function() {
              this.quantity.setValue(0);
              this.batchDetails="";
         }, this);
        this.isBinForProduct=new Wtf.form.Checkbox({ //option to check wether batch is compulsory or not
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isBinCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isBinCompulsory")+"</span>",
            name:'isBinForProduct',
            checked:false,
            disabled:(this.isEdit && !this.isClone)?((this.record.data.isUsedInBatchSerial?true:false)):false,
            hidden:!Wtf.account.companyAccountPref.isBinCompulsory,
            hideLabel:!Wtf.account.companyAccountPref.isBinCompulsory,
            itemCls:"chkboxalign"
        });
        this.isBinForProduct.on("change", function() {
              this.quantity.setValue(0);
              this.batchDetails="";
         }, this);
        this.isSerialForProduct=new Wtf.form.Checkbox({ //option to check wether batch is compulsory or not
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isSerialCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isSerialCompulsory")+"</span>",
            name:'isSerialForProduct',
            id:'isSerialForProduct'+this.id,
            checked:false,
            disabled:(this.isEdit && !this.isClone)?((this.record.data.isUsedInBatchSerial?true:false)):false,
            hidden:!Wtf.account.companyAccountPref.isSerialCompulsory,
            hideLabel:!Wtf.account.companyAccountPref.isSerialCompulsory,
            itemCls:"chkboxalign",
            listeners:{
                scope:this,
                check:function(){
                    if(this.isSerialForProduct.getValue()){ //if(this.isSerialForProduct.getValue() && ((this.isEdit && !this.isClone)?((this.record.data.isUsedInTransaction?false:true)):true))
                        if (this.isEdit && this.isSerialForProduct.disabled) { // if this is edit case and opening is given for product,then do not allow user to change its following properties,so disabled it.
                            this.isSKUForProduct.disable();
                        }else{
                            this.isSKUForProduct.enable();
                        }
                        
                        Wtf.getCmp('bserialId'+this.id).enable();
                       if(Wtf.getCmp('bSkuId'+this.id)){ //AS we are creating this button if prefrences set
                           Wtf.getCmp('bSkuId'+this.id).enable();
                       } 
                    }else{
                        this.isSKUForProduct.setValue(false);
                        this.isSKUForProduct.disable();
                        Wtf.getCmp('bserialId'+this.id).setValue(false);
                        Wtf.getCmp('bserialId'+this.id).disable();
                        if(Wtf.getCmp('bSkuId'+this.id)){ //AS we are creating this button if prefrences set
                            Wtf.getCmp('bSkuId'+this.id).setValue(false);
                            Wtf.getCmp('bSkuId'+this.id).disable();
                        } 
                      
                    }
                }
            }
        });
        this.isSerialForProduct.on("change", function() {
              this.quantity.setValue(0);
              this.batchDetails="";
         }, this);        
        var SKULebel= (Wtf.account.companyAccountPref.SKUFieldParm)? ((Wtf.account.companyAccountPref.SKUFieldRename!="" && Wtf.account.companyAccountPref.SKUFieldRename!= undefined) ? Wtf.account.companyAccountPref.SKUFieldRename :WtfGlobal.getLocaleText("acc.product.sku")) : "";
        this.isSKUForProduct=new Wtf.form.Checkbox({ //option to check wether sku is compulsory or not
            fieldLabel:"<span wtf:qtip='Activate "+SKULebel+"'>"+"Activate "+SKULebel+"</span>",
            name:'isSKUForProduct',
            id:'isSKUForProduct'+this.id,
            checked:false,
            disabled:(this.isEdit && !this.isClone)?((this.record.data.isUsedInBatchSerial?true:false)):true,
            hidden:!Wtf.account.companyAccountPref.SKUFieldParm,
            hideLabel:!Wtf.account.companyAccountPref.SKUFieldParm,
            itemCls:"chkboxalign"
        });
        this.isSKUForProduct.on("change", function() {
              this.quantity.setValue(0);
         }, this);        
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.createcoa))
            this.purchaseReturnAcc.addNewFn=this.addAccount.createDelegate(this,[this.purchaseAccStore,false,false,false,true],true);
        if (Wtf.account.companyAccountPref.productOptimizedFlag === Wtf.Show_all_Products) {
            var baseParams = this.parentStore.baseParams;
                    var config = {
                        hiddenName: 'parentid',
                        fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.product.parentProduct.tt") + "'>" + WtfGlobal.getLocaleText("acc.product.parentProduct") + "</span>",
                        hideLabel: false,
                        displayField: 'productname',
                        extraFields:['pid','type']
                    };
            this.parentname = CommonERPComponent.createProductPagingComboBox(250, 300, 30, this, baseParams, false, false, config);
        } else {
            if (!Wtf.isEmpty(Wtf.account.companyAccountPref.productOptimizedFlag)) {
                if (Wtf.account.companyAccountPref.productOptimizedFlag === Wtf.Products_on_Submit) {//free text  
                    this.parentname = new Wtf.form.ExtFnComboBox({
                        fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.product.parentProduct.tt") + "'>" + WtfGlobal.getLocaleText("acc.product.parentProduct") + "</span>", //WtfGlobal.getLocaleText("acc.product.parentProduct"),//'Parent Product',
                        anchor: '85%',
                        hiddenName: 'parentid',
                        store: this.parentStore,
                        valueField: 'parentid',
                        displayField: 'parentname',
                        typeAhead: true,
                        selectOnFocus: true,
                        isProductCombo: true,
                        extraFields: ['pid'],
                        width: 150,
                        listWidth: 300,
                        maxHeight: 250,
                        hideTrigger: true,
                        isParentCombo: true,
                        extraComparisionField: 'pid', // type ahead search on acccode as well.
                        mode: 'remote',
                        scope: this,
                        triggerAction: 'all',
                        editable: true,
                        minChars: 2,
                        hirarchical: true,
                        hidden: Wtf.account.companyAccountPref.productOptimizedFlag === Wtf.Products_on_Submit ? true : false,
                        hideLabel: Wtf.account.companyAccountPref.productOptimizedFlag === Wtf.Products_on_Submit ? true : false,
                        hideAddButton: true, //Added this Flag to hide AddNew  Button  
                        forceSelection: true
                    });
                } else {
                    var baseParams = this.parentStore.baseParams;
                    var config = {
                        hiddenName: 'parentid',
                        fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.product.parentProduct.tt") + "'>" + WtfGlobal.getLocaleText("acc.product.parentProduct") + "</span>",
                        hideLabel: false,
                        displayField: 'productname',
                        extraFields:['pid','type']
                    };
                    this.parentname = CommonERPComponent.createProductPagingComboBox(250, 300, 30, this, baseParams, false, true, config);
                }
            }
        }
            this.productTextField = new Wtf.form.TextField({
                fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.product.parentProduct.tt") + "'>" + WtfGlobal.getLocaleText("acc.product.parentProduct") + "</span>", //WtfGlobal.getLocaleText("acc.product.parentProduct"),//'Parent Product',
                name: 'pid',
                width: 150,
                anchor: '85%',
                hidden: Wtf.account.companyAccountPref.productOptimizedFlag === Wtf.Products_on_Submit ? false : true,
                hideLabel: Wtf.account.companyAccountPref.productOptimizedFlag === Wtf.Products_on_Submit ? false : true
            });
        this.oldProductName = "";
        this.productTextField.on("blur", function() {
            if(this.productTextField.getValue().trim()!="" && this.productTextField.getValue().trim()!=this.oldProductName){
                this.parentStore.load({
                    params: {
                        searchProductString: (Wtf.account.companyAccountPref.productOptimizedFlag == Wtf.Products_on_Submit) ? this.productTextField.getValue() : ""
                    }
                });
                this.oldProductName=this.productTextField.getValue().trim();
            }else{
                this.parentname.reset();
            }
        }, this);
        if (this.isEdit) {
            this.productEdited=true;
            if(this.record!=undefined && this.record.data!=undefined && this.record.data.parentuuid==""){
                this.productEdited=false;
            }
        }
        if (this.isEdit || (Wtf.account.companyAccountPref.productOptimizedFlag !== Wtf.Products_on_type_ahead && Wtf.account.companyAccountPref.productOptimizedFlag != Wtf.Show_all_Products)) {
            this.parentStore.on("load", function () {
                if (Wtf.account.companyAccountPref.productOptimizedFlag == Wtf.Products_on_Submit && (!this.isEdit || !this.productEdited)) {
                    var idx = WtfGlobal.searchRecordIndex(this.parentStore, this.productTextField.getValue().trim(), "pid");
                    if (idx != -1) {
                        var rec = this.parentStore.getAt(idx);
                        if (rec != undefined && rec.data != undefined && rec.data.parentname != undefined && rec.data.parentname != "") {
                            this.productTextField.setValue(rec.data.parentname);
                            this.parentname.setValue(rec.data.parentid);
                            this.oldProductName=rec.data.parentname;
                        }
                    } else {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.accPref.productnotFound") + " " + this.productTextField.getValue().trim()+"."], 2);
                        this.productTextField.reset();
                        this.parentname.reset();
                        if (this.isEdit && !this.productEdited) {
                            if (this.record != undefined && this.record.data != undefined && this.record.data.parentuuid == "") {
                                this.parentStore.load({
                                    params: {
                                        ids: this.record.data.parentuuid
                                    }
                                });

                                this.productEdited = true;
                            }
                        }
                    }
                } else if (Wtf.account.companyAccountPref.productOptimizedFlag == Wtf.Products_on_Submit && this.isEdit) {
                    var idx = WtfGlobal.searchRecordIndex(this.parentStore, this.record.data.parentuuid, "productid");
                    if (idx != -1) {
                        var rec = this.parentStore.getAt(idx);
                        if (rec != undefined && rec.data != undefined && rec.data.parentname != undefined && rec.data.parentname != "") {
                            this.productTextField.setValue(rec.data.parentname);
                            this.parentname.setValue(rec.data.parentid);
                            this.oldProductName=rec.data.parentname;
                        }
                    }
                    this.productEdited = false;
                }else if (this.isEdit){
                    if(Wtf.account.companyAccountPref.productOptimizedFlag == Wtf.Products_on_type_ahead ||  Wtf.account.companyAccountPref.productOptimizedFlag == Wtf.Show_all_Products){
                        this.parentname.setValForRemoteStore(this.record.data.parentuuid,this.record.data.parentname);
                    }else{
                        this.parentname.setValue(this.record.data.parentuuid);
                    }
                }
            }, this);
        }
            if ((Wtf.account.companyAccountPref.productOptimizedFlag === Wtf.Products_on_type_ahead || Wtf.account.companyAccountPref.productOptimizedFlag === Wtf.Products_on_Submit || Wtf.account.companyAccountPref.productOptimizedFlag == Wtf.Show_all_Products) || this.isEdit) {
            // load parentstore if and only if the parentuuid is not empty in edit case
            
            if (this.record != undefined && this.record.data != undefined && this.record.data.parentuuid != undefined && this.record.data.parentuuid !== "") {
                this.parentStore.load({
                    params:{
                        ids: this.record.data.parentuuid
                    }
                });
            }
        }
        this.descriptionshow = new Wtf.form.TextArea({
            fieldLabel:"<span wtf:qtip='"+((this.isFixedAsset)?WtfGlobal.getLocaleText("acc.fixedasset.gridDesc.tt"):WtfGlobal.getLocaleText("acc.product.gridDesc.tt"))+"'>"+ WtfGlobal.getLocaleText("acc.product.description")+"</span>",//WtfGlobal.getLocaleText("acc.product.description"),// 'Description',
            name: 'descshow',
            id:'descshow'+this.id,
            anchor:'85%',
            //            regex:/^[^\"\\]+$/,
            height: 50,
            invalidText : WtfGlobal.getLocaleText("acc.field.Thisfieldshouldnotcontaincharacters")
            //maxLength:60000
        });
        this.descriptionshow.on("focus",function(){
            if(Wtf.account.companyAccountPref.proddiscripritchtextboxflag==2){
                this.getPostTextEditor("Desc");
            }else{
                this.description.setValue(this.descriptionshow.getValue());
            }               
        },this);
        this.description = new Wtf.form.Hidden({
            //            fieldLabel:WtfGlobal.getLocaleText("acc.product.description"),// 'Description',
            name: 'desc',
            hidden:true,
            anchor:'85%',
            //            regex:/^[^\"\\]+$/,
            height: 50,
            invalidText : WtfGlobal.getLocaleText("acc.field.Thisfieldshouldnotcontaincharacters")
        //            maxLength:1024
        });
        this.subproduct=new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.product.isSubProduct"),//'Is a subproduct?',
            checkboxToggle: true,
            autoHeight: true,
            autoWidth: true,
            hidden:this.isFixedAsset,
            width: 380,
            checkboxName: 'subproduct',
            style: 'margin-right:30px',
            collapsed: true,
            items:[this.productTextField,this.parentname]
        });
        this.valuationType = new Wtf.form.TextField({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.product.valuationType") + "'>" + WtfGlobal.getLocaleText("acc.product.valuationType")+"*" + "</span>", //WtfGlobal.getLocaleText("acc.product.parentProduct"),//'Parent Product',
            name: 'valuationType',
            width: 150
//            allowBlank:true;//(Wtf.isExciseApplicable && Wtf.exciseTariffdetails && this.producttypeval!=Wtf.producttype.service)?false:true

        });

        this.subproduct.on("beforeexpand",this.checkParent,this);
        this.producttype.on("select",this.changeLayoutWithType,this);

        this.CreationDate= new Wtf.form.DateField({
            fieldLabel:'Creation Date*',
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'creationdate',
            anchor:'50%',
            //        listeners:{
            //            'change':{
            //                fn:this.updateDueDate,
            //                scope:this
            //            }
            //        },
            allowBlank:false,
            //        minValue:new Date().format('Y-m-d'),
            minValue: new Date().clearTime(true)
        });

        this.Supplier=new Wtf.form.ExtendedTextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.supplier"),//'Supllier*',
            name: 'supplier',
//            disabled:(this.isEdit && !this.isClone)?true:false,
            //allowBlank:false,
            anchor:'95%',
            hidden:!Wtf.account.companyAccountPref.partNumber,
            hideLabel:!Wtf.account.companyAccountPref.partNumber,
            //            regex:/^[\w\s\'\"\.\-]+$/,
            regex:/^[^\"\%\\]+$/,
            invalidText : WtfGlobal.getLocaleText("acc.field.Thisshouldnotbeblankshouldnotcontain%characters"),
            maxLength:50
        });
        this.CoilCraft=new Wtf.form.ExtendedTextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.coilcraft"),//'coilcraft*',
            name: 'coilcraft',
//            disabled:(this.isEdit && !this.isClone)?true:false,
            //allowBlank:false,
            anchor:'95%',
            hidden:!Wtf.account.companyAccountPref.partNumber,
            hideLabel:!Wtf.account.companyAccountPref.partNumber,
            //            regex:/^[\w\s\'\"\.\-]+$/,
            regex:/^[^\"\%\\]+$/,
            invalidText :  WtfGlobal.getLocaleText("acc.field.Thisshouldnotbeblankshouldnotcontain%characters"),
            maxLength:50
        });
        this.InterPlant=new Wtf.form.ExtendedTextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.interplant"),//'interplant*',
            name: 'interplant',
//            disabled:(this.isEdit && !this.isClone)?true:false,
            //allowBlank:false,
            anchor:'95%',
            hidden:!Wtf.account.companyAccountPref.partNumber,
            hideLabel:!Wtf.account.companyAccountPref.partNumber,
            //            regex:/^[\w\s\'\"\.\-]+$/,
            regex:/^[^\"\%\\]+$/,
            invalidText :  WtfGlobal.getLocaleText("acc.field.Thisshouldnotbeblankshouldnotcontain%characters"),
            maxLength:50
        });
//        this.shelfLocationCombo=new Wtf.form.ComboBox({
//            fieldLabel: WtfGlobal.getLocaleText("acc.field.ShelfLocation"),
//            name: 'shelfLocation',
//            mode: 'local',
//            triggerAction: 'all',
//            emptyText:WtfGlobal.getLocaleText("acc.field.SelectShelfLocation"),
//            typeAhead: true,
//            editable: true,
//            width:200,
//            store: this.shelfLocationStore,
//            displayField: 'shelfLocationValue',
//            valueField:'shelfLocationId',
//            hiddenName:'shelfLocationId',
//            hidden:!Wtf.account.companyAccountPref.invAccIntegration,
//            hideLabel:!Wtf.account.companyAccountPref.invAccIntegration
//        });
        

  this.customerCategoryRec=new Wtf.data.Record.create([
        {
            name: 'id'
        },
        {
            name: 'name'
        }]
        );

        this.customerCategoryStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.customerCategoryRec),
            url:"ACCMaster/getMasterItems.do",
            baseParams:{
                mode:112,
                groupid:7                    // for customer Category 
            }
        });
        this.customerCategoryStore.load();
        this.customerCategoryStore.on('load',function(){
        if(this.isEdit){
                this.customerCategoryCombo.setValue(this.record.data.customercategory);
            }
        },this)
    
        this.customerCategoryCombo =new Wtf.common.Select({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.customercategory") +"'>"+WtfGlobal.getLocaleText("acc.field.customercategory")+"</span>",
            hiddenName:'customercategory',
            name:'customercategory',
            id:'customercategory'+this.id,
            store:this.customerCategoryStore,
            anchor:'95%',
            valueField:'id',
            displayField:'name',
            selectOnFocus:true,
            forceSelection:true,
            hidden: !Wtf.account.companyAccountPref.isFilterProductByCustomerCategory,
            hideLabel: !Wtf.account.companyAccountPref.isFilterProductByCustomerCategory,
            mode: 'local',
            //allowBlank:false,
            triggerAction:'all',
            multiSelect:true,
            typeAhead: true
        }); 
                
        this.createFixedAssetEntryDetails();
        this.createProductComposition();
        

        /*    
     *                    
     *                         Purchase tab fields                     
     *                                                    
     *                                                           
    */
    
    
        this.PurchasingUoMStore = new Wtf.data.SimpleStore({
            id: "PurchasingUoMStore"+this.id,
            fields:['uomid','uomname']
        });
    
    
        this.Purchaseuom= new Wtf.form.FnComboBox({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.purchaseUoMLabel")+"'>"+ WtfGlobal.getLocaleText("acc.product.purchaseUoMLabel") +"</span>",//WtfGlobal.getLocaleText("acc.product.uom"),//'Unit Of Measure*',
            hiddenName:'purchaseuomid',
            id:'purchaseuomid'+this.id,
            store:this.PurchasingUoMStore,
            //hidden:this.isFixedAsset,
            //hideLabel:this.isFixedAsset,
            anchor:'70%',
            //allowBlank:(this.producttypeval==Wtf.producttype.service) || this.isFixedAsset,
            valueField:'uomid',
            displayField:'uomname',
            forceSelection: true//,
        // addNewFn:this.showUom.createDelegate(this)
        }); 
    
        this.ItemPurchaseHeight=new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.barcdheight") +"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.barcdheight") +"</span>",
            name: 'itempurchaseheight',
            id:'itempurchaseheight'+this.id,
            allowDecimals:true,
            allowNegative:false,
            anchor:'70%',
            //hidden:this.isFixedAsset,
            //hideLabel:this.isFixedAsset,
            //allowBlank:this.isFixedAsset,
            //            value:0,
            maxLength:11
        });
    
        this.ItemPurchaseWidth=new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.exportinterface.width") +"'>"+WtfGlobal.getLocaleText("acc.exportinterface.width") +"</span>",
            name: 'itempurchasewidth',
            id:'itempurchasewidth'+this.id,
            allowDecimals:true,
            allowNegative:false,
            anchor:'70%',
            //hidden:this.isFixedAsset,
            //hideLabel:this.isFixedAsset,
            //allowBlank:this.isFixedAsset,
            //            value:0,
            maxLength:11
        });
    
        this.ItemPurchaseLength=new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.Length") +"'>"+WtfGlobal.getLocaleText("acc.product.Length") +"</span>",
            name: 'itempurchaselength',
            id:'itempurchaselength'+this.id,
            allowDecimals:true,
            allowNegative:false,
            anchor:'70%',
            //hidden:this.isFixedAsset,
            //hideLabel:this.isFixedAsset,
            //allowBlank:this.isFixedAsset,
            //            value:0,
            maxLength:11
        });
    
        this.ItemPurchaseVolume=new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.Volume") +"'>"+WtfGlobal.getLocaleText("acc.product.Volume") +"</span>",
            name: 'itempurchasevolume',
            id:'itempurchasevolume'+this.id,
            allowDecimals:true,
            allowNegative:false,
            anchor:'70%',
            //hidden:this.isFixedAsset,
            //hideLabel:this.isFixedAsset,
            //allowBlank:this.isFixedAsset,
            //            value:0,
            maxLength:11
        });
    

        this.PurchaseMfg=new Wtf.form.TextField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.PurchaseManufacturing") +"'>"+WtfGlobal.getLocaleText("acc.product.PurchaseManufacturing")+"</span>",
            name: 'purchasemfg',
            id:'purchasemfg'+this.id,
            anchor:'70%',
            //hidden:this.isFixedAsset,
            //hideLabel:this.isFixedAsset,
            //allowBlank:this.isFixedAsset,
            //            value:0,
            maxLength:11
        });
        
        
          this.taxRec = new Wtf.data.Record.create([
           {name: 'prtaxid',mapping:'taxid'},
           {name: 'prtaxname',mapping:'taxname'},
           {name: 'hasAccess'}

        ]);
        
        this.purchaseTaxStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.taxRec),
            url: "ACCTax/getTax.do",
            baseParams: {
                mode: 33,
                moduleid: Wtf.Acc_Vendor_ModuleId,
                includeDeactivatedTax: this.isEdit != undefined ? (this.isClone ? false : this.isEdit) : false
            }
        });
        
        
        /*---Purchase Tax field in Product Form-------  */
        this.purchaseTax = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.purchase.Tax") , 
            id: "tax" + this.id,
            disabled: false,
            hiddenName: 'tax',           
            anchor:'70%',
            store: this.purchaseTaxStore,
            valueField: 'prtaxid',
            forceSelection: true,
            displayField: 'prtaxname',
            hidden:!CompanyPreferenceChecks.mapTaxesAtProductLevel(),
            hideLabel:!CompanyPreferenceChecks.mapTaxesAtProductLevel(),
            scope: this,
            selectOnFocus: true,
            addNoneRecord: true,         //For 'None' option in Tax Combo.
            extraFields:[],
            isTax: true,
            listeners: {
                'beforeselect': {
                    fn: function (combo, record, index) {
                        return validateSelection(combo, record, index);
                    },
                    scope: this
                }
            }
            
        });
        
        
        this.purchaseTaxStore.on('load', function() {
            if (this.isEdit || this.isClone) {
                this.purchaseTax.setValue(this.record.json.purchasetaxId);
            }
        }, this);

        if (CompanyPreferenceChecks.mapTaxesAtProductLevel()) {//Load ony for malaysian company
            this.purchaseTaxStore.load();
        }
        
           
        this.CatalogNo=new Wtf.form.TextField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.CatalogNo.") +"'>"+WtfGlobal.getLocaleText("acc.product.CatalogNo.") +"</span>",
            name: 'catalogno',
            id:'catalogno'+this.id,
            anchor:'70%'
        //hidden:this.isFixedAsset,
        //hideLabel:this.isFixedAsset,
        //allowBlank:this.isFixedAsset,
        //            value:0,
        });
   
    
        /*    
     *                    
     *                         Sales tab fields                     
     *                                                    
     *                                                           
    */
    
        this.SellingUoMStore = new Wtf.data.SimpleStore({
            id: "SellingUoMStore"+this.id,
            fields:['uomid','uomname']
        });

        this.Salesuom= new Wtf.form.FnComboBox({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.salesUoMLabel") +"'>"+WtfGlobal.getLocaleText("acc.product.salesUoMLabel")+"</span>",//WtfGlobal.getLocaleText("acc.product.uom"),//'Unit Of Measure*',
            hiddenName:'salesuomid',
            id:'salesuomid'+this.id,
            store:this.SellingUoMStore,
            hidden:this.isFixedAsset,
            hideLabel:this.isFixedAsset,
            anchor:'70%',
            // allowBlank:(this.producttypeval==Wtf.producttype.service) || this.isFixedAsset,
            valueField:'uomid',
            displayField:'uomname',
            forceSelection: true//,
        // addNewFn:this.showUom.createDelegate(this)
        }); 
    
        if( Wtf.account.companyAccountPref.UomSchemaType == Wtf.UOMSchema){
            this.Purchaseuom.disable();
            this.Salesuom.disable();
        }
    
        this.ItemSalesHeight=new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.barcdheight") +"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.barcdheight") +"</span>",
            name: 'itemsalesheight',
            id:'itemsalesheight'+this.id,
            allowDecimals:true,
            allowNegative:false,
            anchor:'70%',
            //hidden:this.isFixedAsset,
            //hideLabel:this.isFixedAsset,
            //allowBlank:this.isFixedAsset,
            //            value:0,
            maxLength:11
        });
    
        this.ItemSalesWidth=new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.exportinterface.width") +"'>"+WtfGlobal.getLocaleText("acc.exportinterface.width") +"</span>",
            name: 'itemsaleswidth',
            id:'itemsaleswidth'+this.id,
            allowDecimals:true,
            allowNegative:false,
            anchor:'70%',
            //hidden:this.isFixedAsset,
            //hideLabel:this.isFixedAsset,
            //allowBlank:this.isFixedAsset,
            //            value:0,
            maxLength:11
        });
    
        this.ItemSalesLength=new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.Length") +"'>"+WtfGlobal.getLocaleText("acc.product.Length") +"</span>",
            name: 'itemsaleslength',
            id:'itemsaleslength'+this.id,
            allowDecimals:true,
            allowNegative:false,
            anchor:'70%',
            //hidden:this.isFixedAsset,
            //hideLabel:this.isFixedAsset,
            //allowBlank:this.isFixedAsset,
            //            value:0,
            maxLength:11
        });
    
        this.ItemSalesVolume=new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.Volume") +"'>"+WtfGlobal.getLocaleText("acc.product.Volume") +"</span>",
            name: 'itemsalesvolume',
            id:'itemsalesvolume'+this.id,
            allowDecimals:true,
            allowNegative:false,
            anchor:'70%',
            //hidden:this.isFixedAsset,
            //hideLabel:this.isFixedAsset,
            //allowBlank:this.isFixedAsset,
            //            value:0,
            maxLength:11
        });
    
        /*   this.AlternateProducts=new Wtf.form.ComboBox({
            fieldLabel:"<span wtf:qtip='"+"Alternative Products" +"'>"+"Alternative Products"+"</span>",
            hiddenName:'alternateproductid',
            //store:Wtf.uomStore,
            anchor:'70%',
            //valueField:'uomid',
            //displayField:'uomname',
            forceSelection: true//,
        // addNewFn:this.showUom.createDelegate(this)
     }); 
 */   
    
        this.AlternateProducts=new Wtf.form.TextField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.AlternativeProducts") +"'>"+WtfGlobal.getLocaleText("acc.product.AlternativeProducts")+"</span>",
            name:'alternateproductid',
            id:'alternateproductid'+this.id,
            anchor:'70%'
        }); 
    
    
  
        this.salesTaxStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.taxRec),
            url: "ACCTax/getTax.do",
            baseParams: {
                mode: 33,
                moduleid:Wtf.Acc_Customer_ModuleId,
                includeDeactivatedTax: this.isEdit != undefined ? (this.isClone ? false : this.isEdit) : false
            }
        });
        
        
          /*---Sales Tax field in Product Form-------  */
        this.salesTax = new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.sales.Tax") , 
            id: "salestax" + this.id,
            disabled: false,
            hiddenName: 'tax',
            anchor:'70%',
            store: this.salesTaxStore,
            valueField: 'prtaxid',
            forceSelection: true,
            displayField: 'prtaxname',
            hidden:!CompanyPreferenceChecks.mapTaxesAtProductLevel(),
            hideLabel:!CompanyPreferenceChecks.mapTaxesAtProductLevel(),
            scope: this,
            selectOnFocus: true,
            addNoneRecord: true,         //For 'None' option in Tax Combo.
            extraFields: [],
            isTax: true,
            listeners: {
                'beforeselect': {
                    fn: function (combo, record, index) {
                        return validateSelection(combo, record, index);
                    },
                    scope: this
                }
            }
        });
        
        
        this.salesTaxStore.on('load', function() {
            if (this.isEdit || this.isClone) {
                this.salesTax .setValue(this.record.json.salestaxId);
            }
        }, this);

        if (CompanyPreferenceChecks.mapTaxesAtProductLevel()) {//Load ony for malaysian company
            this.salesTaxStore.load();
        }
    
        
        /*    
     *                    
     *                         Properties tab fields                     
     *                                                    
     *                                                           
    */
    
  
        this.ItemHeight=new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.barcdheight") +"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.barcdheight") +"</span>",
            name: 'itemheight',
            id:'itemheight'+this.id,
            allowDecimals:true,
            allowNegative:false,
            anchor:'70%',
            //hidden:this.isFixedAsset,
            //hideLabel:this.isFixedAsset,
            //allowBlank:this.isFixedAsset,
            //            value:0,
            maxLength:11
        });
    
    
        this.ItemWidth=new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.exportinterface.width")+"'>"+WtfGlobal.getLocaleText("acc.exportinterface.width") +"</span>",
            name: 'itemwidth',
            id:'itemwidth'+this.id,
            allowDecimals:true,
            allowNegative:false,
            anchor:'70%',
            //hidden:this.isFixedAsset,
            //hideLabel:this.isFixedAsset,
            //allowBlank:this.isFixedAsset,
            //            value:0,
            maxLength:11
        });
    
        this.ItemLength=new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.Length") +"'>"+WtfGlobal.getLocaleText("acc.product.Length") +"</span>",
            name: 'itemlength',
            id:'itemlength'+this.id,
            allowDecimals:true,
            allowNegative:false,
            anchor:'70%',
            //hidden:this.isFixedAsset,
            //hideLabel:this.isFixedAsset,
            //allowBlank:this.isFixedAsset,
            //            value:0,
            maxLength:11
        });
    
        this.ItemVolume=new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.Volume") +"'>"+WtfGlobal.getLocaleText("acc.product.Volume") +"</span>",
            name: 'itemvolume',
            id:'itemvolume'+this.id,
            allowDecimals:true,
            allowNegative:false,
            anchor:'70%',
            //hidden:this.isFixedAsset,
            //hideLabel:this.isFixedAsset,
            //allowBlank:this.isFixedAsset,
            //            value:0,
            maxLength:11
        });
    
        /*   this.ItemColor=new Wtf.form.ComboBox({
            fieldLabel:"<span wtf:qtip='"+"Color" +"'>"+"Color"+"</span>",
            hiddenName:'itemcolor',
            //store:Wtf.uomStore,
            anchor:'70%',
            //valueField:'uomid',
            //displayField:'uomname',
            forceSelection: true//,
        // addNewFn:this.showUom.createDelegate(this)
     }); 
 */

 
        this.ItemColor=new Wtf.form.TextField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.Color") +"'>"+WtfGlobal.getLocaleText("acc.product.Color")+"</span>",
            name:'itemcolor',
            id:'itemcolor'+this.id,
            anchor:'70%',
            maxLength:50   
        }); 
     
        
    
        /*    
     *                    
     *                Remarks tab fields                     
     *                                                    
     *                                                           
    */
    
        this.AdditionalFreeText = new Wtf.form.TextArea({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.AdditionalFreeText")+"'>"+ WtfGlobal.getLocaleText("acc.product.AdditionalFreeText") +"</span>",
            name: 'additionalfreetext',
            id:'additionalfreetext'+this.id,
            anchor:'90%',
            //            regex:/^[^\"\\]+$/,
            height: 50,
            invalidText : WtfGlobal.getLocaleText("acc.field.Thisfieldshouldnotcontaincharacters"),
            maxLength:900
        });
    
    
        /*    
     *                    
     *                Inventory data tab fields                     
     *                                                    
     *                                                           
    */
    
   
    
    this.CasingUoMCombo =new Wtf.form.FnComboBox({
        fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.casingUOM") +"'>"+WtfGlobal.getLocaleText("acc.product.casingUOM")+"</span>",
        hiddenName:'casinguom',
        id:'casinguom'+this.id,
        store:Wtf.uomStore,
        hidden:this.isFixedAsset,
        hideLabel:this.isFixedAsset,
        allowBlank:true,
        anchor:'70%',
        valueField:'uomid',
        displayField:'uomname',
        forceSelection: true//,
    // addNewFn:this.showUom.createDelegate(this)
    }); 
    
    this.InnerUoMCombo =new Wtf.form.FnComboBox({
        fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.innerUOM") +"'>"+WtfGlobal.getLocaleText("acc.product.innerUOM")+"</span>",
        hiddenName:'inneruom',
        id:'inneruom'+this.id,
        store:Wtf.uomStore,
        hidden:this.isFixedAsset,
        hideLabel:this.isFixedAsset,
        allowBlank:true,
        anchor:'70%',
        valueField:'uomid',
        displayField:'uomname',
        forceSelection: true//,
    // addNewFn:this.showUom.createDelegate(this)
    }); 
    
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.uom, Wtf.Perm.uom.edit)){
        this.CasingUoMCombo.addNewFn=this.showUom.createDelegate(this);
        this.InnerUoMCombo.addNewFn=this.showUom.createDelegate(this);
    }
     
    this.CasingUoMCombo.on('beforeselect', function (combo, selectedRecord, index) {
        return this.doNotAllowToSelectNAUom(selectedRecord);
    }, this);

    this.InnerUoMCombo.on('beforeselect', function (combo, selectedRecord, index) {
        return this.doNotAllowToSelectNAUom(selectedRecord);
    }, this);
    
    this.StockUoMCombo = new Wtf.form.FnComboBox({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.UOM.StockUOM") +"'>"+WtfGlobal.getLocaleText("acc.UOM.StockUOM")+"</span>",
        hiddenName:'stockuom',
        id:'stockuom'+this.id,
        store:Wtf.uomStore,
        anchor:'70%',
        valueField:'uomid',
        displayField:'uomname',
        forceSelection: true//,
    // addNewFn:this.showUom.createDelegate(this)
    }); 
     
    this.OrderUoMStore = new Wtf.data.SimpleStore({
        id:"OrderUoMStore"+this.id,
        fields:['uomid','uomname']
    });
     
    this.OrderingUoMCombo = new Wtf.form.FnComboBox({
        fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.orderUoMLabel") +"'>"+WtfGlobal.getLocaleText("acc.product.orderUoMLabel")+"</span>",
        hiddenName:'orderinguom',
        id:'orderinguom'+this.id,
        store:this.OrderUoMStore,
        anchor:'70%',
        valueField:'uomid',
        displayField:'uomname',
        //allowBlank:false,
        forceSelection: true
    //addNewFn:this.showUom.createDelegate(this)
    }); 
     
    this.transferuomStore = new Wtf.data.SimpleStore({
        id: "transferuomStore"+this.id,
        fields:['uomid','uomname']
    });
       
       
    this.TransferUoMCombo = new Wtf.form.FnComboBox({
        fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.transferUoMLabel") +"'>"+WtfGlobal.getLocaleText("acc.product.transferUoMLabel")+"</span>",
        hiddenName:'transferuom',
        id:'transferuom'+this.id,
        store:this.transferuomStore,
        anchor:'70%',
        valueField:'uomid',
        displayField:'uomname',
        //allowBlank:false,
        forceSelection: true//,
    // addNewFn:this.showUom.createDelegate(this)
    }); 
     
     
    this.CasingUoMValue = new Wtf.form.NumberField({
        fieldLabel:WtfGlobal.getLocaleText("acc.product.CasingUOMValue"),
        width:200,
        name:"casinguomvalue",
        id:'casinguomvalue'+this.id,
        disabled:true,
        anchor:'70%'
    //value:(this.action == "Edit")?this.rec.get("casinguomvalue"):0
    });
        this.InnerUoMValue = new Wtf.form.NumberField({
            fieldLabel:WtfGlobal.getLocaleText("acc.product.InnerUOMValue"),
            width:200,
            name:"inneruomvalue",
            id:'inneruomvalue'+this.id,
            anchor:'70%'
        //value:(this.action == "Edit")?this.rec.get("inneruomvalue"):0
        });
        this.StockUoMValue = new Wtf.form.NumberField({
            fieldLabel:WtfGlobal.getLocaleText("acc.product.StockUOMValue"),
            width:200,
            name:"stockuomvalue",
            id:'stockuomvalue'+this.id,
            anchor:'70%'
        //value:(this.action == "Edit")?this.rec.get("primaryuomvalue"):0
        });
        this.packaging = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.product.packaging"),
            width:200,
            name:"packaging",
            id:'packaging'+this.id,
            anchor:'70%',
            readOnly:true
        //value:(this.action == "Edit")?this.rec.get("packaging"):""
        });
    
        this.ItemCost=new Wtf.form.TextField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.ItemCostforstandardvaluation") +"'>"+WtfGlobal.getLocaleText("acc.product.ItemCost") +"</span>",
            name: 'itemcost',
            anchor:'70%'
        });
    
        /*    this.WIPOffset=new Wtf.form.ComboBox({
            fieldLabel:"<span wtf:qtip='"+"WIP Offset P&L A/c" +"'>"+"WIP Offset P&L A/c"+"</span>",
            hiddenName:'wipoffset',
            //store:Wtf.uomStore,
            anchor:'70%',
            //valueField:'uomid',
            //displayField:'uomname',
            forceSelection: true//,
        // addNewFn:this.showUom.createDelegate(this)
     }); 
 */
 
        this.WIPOffset=new Wtf.form.TextField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.WIPOffsetP&LA/c") +"'>"+WtfGlobal.getLocaleText("acc.product.WIPOffsetP&LA/c")+"</span>",
            name:'wipoffset',
            id:'wipoffset'+this.id,
            anchor:'70%',
            disabled :(this.isUsedInTransaction && this.isEdit && !this.isClone && this.producttypeval==Wtf.producttype.assembly) ? true : false
        }); 
     
     
        /*    this.InventoryOffset=new Wtf.form.ComboBox({
            fieldLabel:"<span wtf:qtip='"+"Inventory Offset P&L A/c" +"'>"+"Inventory Offset P&L A/c"+"</span>",
            hiddenName:'inventoryoffset',
            //store:Wtf.uomStore,
            anchor:'70%',
            //valueField:'uomid',
            //displayField:'uomname',
            forceSelection: true//,
        // addNewFn:this.showUom.createDelegate(this)
     }); 
 */    
     
        this.InventoryOffset=new Wtf.form.TextField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.InventoryOffsetP&LA/c") +"'>"+WtfGlobal.getLocaleText("acc.product.InventoryOffsetP&LA/c")+"</span>",
            name:'inventoryoffset',
            id:'inventoryoffset'+this.id,
            anchor:'70%',
            disabled :(this.isUsedInTransaction && this.isEdit && !this.isClone && this.producttypeval==Wtf.producttype.assembly) ? true : false
        }); 
   
        this.ccfreqStore= new Wtf.data.SimpleStore({
            fields: ['id', 'name'],
            data : [['0','Daily'],['1','Weekly'],['2','Fortnightly'],['3','Monthly']]
        });

    
        this.CycleCountFrequencyCombo =new Wtf.common.Select({
        multiSelect:true,
        fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.CycleCountFrequency") +"'>"+ WtfGlobal.getLocaleText("acc.product.CycleCountFrequency") +"</span>",
//        id:'ccfrequency'+this.id,
        forceSelection:true,
        hideTrigger1:true,
        anchor:"70%",
        hiddenName:'ccfrequency',
        name:'ccfrequency',
        store:this.ccfreqStore,
        valueField:'id',
        displayField:'name',
        selectOnFocus:true,
        mode: 'local',
        triggerAction:'all',
        typeAhead: true,
        disabled:true
    }); 
    this.countable= new Wtf.form.Checkbox({
        name:'countable',
        id:'countable'+this.id,
        fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.Countableforcyclecount")+"'>"+ WtfGlobal.getLocaleText("acc.product.Countable")+"</span>",
        checked:false,
        itemCls:"chkboxalign"
    });
    this.countable.on('check', function(){    
        if(this.countable.getValue()){
            this.CycleCountFrequencyCombo.enable();
        }else{
            this.CycleCountFrequencyCombo.setValue(""); // emptying the cycle count freq. combo
            this.CycleCountFrequencyCombo.disable();
        }
    }, this);
    
        //this.uom.on("select",this.setPackagingValue,this); 
        //this.uom.on("change",this.setPackagingValue,this); 
        this.packingStockUom.on("select",this.setPackagingValue,this); 
        this.packingStockUom.on("change",this.setPackagingValue,this); 
    
        this.CasingUoMCombo.on("select",this.setPackagingValue,this);
        this.CasingUoMCombo.on("change",this.setPackagingValue,this);
       
        this.InnerUoMCombo.on("select",this.setPackagingValue,this);
        this.InnerUoMCombo.on("change",this.setPackagingValue,this);
    
        this.OrderingUoMCombo.on("select",this.setPackagingValue,this);
        this.OrderingUoMCombo.on("change",this.setPackagingValue,this);
    
        this.TransferUoMCombo.on("select",this.setPackagingValue,this);
        /*
         *If Produt is already used in transaction UOM chang will be disabled
         **/
        this.uom.on('select',function(){
               if(this.isUsedInTransaction&&!this.isClone)
                   {
                         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.stockuom.stockuomupdate")], 2);
                          this.uom.setValue(this.record.data.uomid);
                          this.displayUom.setValue(this.record.data.displayUoMid);
                           this.packingStockUom.setValue(this.record.data.uomid);
                   }
        },this);
        this.packingStockUom.on('select',function(){
            if(this.isUsedInTransaction&&!this.isClone)
                   {
                         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.stockuom.stockuomupdate")], 2);
                         this.uom.setValue(this.record.data.uomid);
                         this.displayUom.setValue(this.record.data.displayUoMid);
                         this.packingStockUom.setValue(this.record.data.uomid);
                   }
            
        },this)
        
       
        this.TransferUoMCombo.on("change",this.setPackagingValue,this);
    
        this.StockUoMValue.on("change",this.setPackagingValue,this);
        this.CasingUoMValue.on("change",this.setPackagingValue,this);
        this.InnerUoMValue.on("change",this.setPackagingValue,this);
        
        this.OrderUoMRec = new Wtf.data.Record.create([
        {
            name:'uomid'
        },

        {
            name:'uomname'
        }
        ]);
    
        this.ValuationMethod0=new Wtf.form.Radio({
            boxLabel:WtfGlobal.getLocaleText("acc.product.LIFO"),
            id: "standard"+this.id,
            name:'valuationmethod',
            labelSeparator:'',
            labelWidth:0,
            value:0,
            disabled :(this.isUsedInTransaction && this.isEdit && !this.isClone && this.producttypeval==Wtf.producttype.assembly) ? true : false
        });
        this.ValuationMethod1=new Wtf.form.Radio({
            boxLabel:WtfGlobal.getLocaleText("acc.product.FIFO"),
            id: "fifo"+this.id,
            checked:true,
            name:'valuationmethod',
            labelSeparator:'',
            value:1,
            disabled :(this.isUsedInTransaction && this.isEdit && !this.isClone && this.producttypeval==Wtf.producttype.assembly) ? true : false
        });
        this.ValuationMethod2=new Wtf.form.Radio({
            boxLabel:WtfGlobal.getLocaleText("acc.product.MovingAverage"),
            id: "average"+this.id,
            name:'valuationmethod',
            labelSeparator:'',
            labelWidth:0,
            value:2,
            disabled :(this.isUsedInTransaction && this.isEdit && !this.isClone && this.producttypeval==Wtf.producttype.assembly) ? true : false
        });
    
    
        /*    
     *                    
     *                      General tab fields                     
     *                                                    
     *                                                           
    */
   
        this.AdditionalDesc = new Wtf.form.TextArea({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.AdditionalDescription")+"'>"+ WtfGlobal.getLocaleText("acc.product.AdditionalDescription") +"</span>",
            name: 'additionaldescription',
            id:'additionaldescription'+this.id,
            anchor:'85%',
            //            regex:/^[^\"\\]+$/,
            height: 50,
            invalidText : WtfGlobal.getLocaleText("acc.field.Thisfieldshouldnotcontaincharacters")
            //maxLength:60000
        });
   
        this.AdditionalDesc.on("focus", function() {
            if (Wtf.account.companyAccountPref.proddiscripritchtextboxflag == 2) {
                this.getPostTextEditor("addDesc");
            } else {
                this.AdditionalDesc.setValue(this.AdditionalDesc.getValue());
            }
        }, this);
    
        this.ItemBarcode=new Wtf.form.TextField({
            fieldLabel:"<span wtf:qtip='"+"Bar Code - EAN 13/Code 39" +"'>"+WtfGlobal.getLocaleText("acc.do.barcode") +"</span>",
            name: 'barcode',
            id:'barcode'+this.id,
            anchor:'85%',
            allowNegative:false
        });
      
        /* ----------When we input focus on "Bar Code" Field then do enable "Bar Code" Radio button-------*/

        this.ItemBarcode.on("focus", function() {
            Wtf.getCmp('brcodeId' + this.id).enable();

        }, this);

        /* 
         * When we do not enter any value in "Bar Code" Field
         * then we keep "Bar Code" Radio button disable    
         */
        this.ItemBarcode.on("blur", function() {

            if (this.ItemBarcode.getValue() == undefined || this.ItemBarcode.getValue() == "") {
                Wtf.getCmp('brcodeId' + this.id).setValue(false);
                Wtf.getCmp('brcodeId' + this.id).disable();
            }

        }, this);
//    
        this.DescForeign = new Wtf.form.TextArea({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.Descriptioninforeignlanguage") +"'>"+ WtfGlobal.getLocaleText("acc.product.Descriptioninforeignlanguage") +"</span>",
            name: 'foreigndescription',
            id:'foreigndescription'+this.id,
            anchor:'85%',
            //            regex:/^[^\"\\]+$/,
            height: 50,
            invalidText : WtfGlobal.getLocaleText("acc.field.Thisfieldshouldnotcontaincharacters")
           //maxLength:60000
        });
        this.DescForeign.on("focus", function() {
            if (Wtf.account.companyAccountPref.proddiscripritchtextboxflag == 2) {
                this.getPostTextEditor("foreignDesc");
            } else {
                this.DescForeign.setValue(this.DescForeign.getValue());
            }
        }, this);
        
         this.masterItemGroupRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);
        this.masterItemTempStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 59
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.masterItemGroupRec)
        });
        if(Wtf.account.companyAccountPref.countryid == '137'){
        this.masterItemTempStore.load();
    }
        this.masterItemTempStore.on('load',function(){
            this.industryCodeCmb.setValue(this.record.data.industryCodeId);
        },this);
        this.industryCodeCmb = new Wtf.form.ComboBox({
                triggerAction: 'all',
                hidden: true,
                hideLabel:true,
                mode: 'local',
                name: 'industryCodeId',
                hiddenName: 'industryCodeId',
                labelWidth : '130',
                valueField: 'id',
                displayField: 'name',
                addNoneRecord: true,
                store: this.masterItemTempStore,
                fieldLabel:WtfGlobal.getLocaleText("acc.Industry.code"),
               // value: editIndudtry,
                anchor:'85%',
                typeAhead: true,
                forceSelection: true
            });
        this.asOfDate = new Wtf.form.DateField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.AsofDate")+"'>"+ WtfGlobal.getLocaleText("acc.field.AsofDate") +"</span>",
            format:WtfGlobal.getOnlyDateFormat(),
            allowBlank:false,
            name: 'asOfDate',
            id:'asofdate'+this.id,
            anchor:'85%',
            height: 50
          });
          
        this.asOfDate.on("change",this.asofdatevalidation,this);          //validate as of date if initial quantity > 0
                    
    
        /*   this.ItemGroup=new Wtf.form.ComboBox({
            fieldLabel:"<span wtf:qtip='"+"Item Group/Subgroup" +"'>"+"Item Group/ Subgroup"+"</span>",
            hiddenName:'itemgroup',
            //store:Wtf.uomStore,
            anchor:'95%',
            //valueField:'uomid',
            //displayField:'uomname',
            forceSelection: true//,
        // addNewFn:this.showUom.createDelegate(this)
     }); 
 */   
        this.ItemGroup=new Wtf.form.TextField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.ItemGroup/Subgroup")+"'>"+WtfGlobal.getLocaleText("acc.product.ItemGroup/Subgroup")+"</span>",
            name:'itemgroup',
            id:'itemgroup'+this.id,
            anchor:'95%',
            disabled :(this.isUsedInTransaction && this.isEdit && !this.isClone && this.producttypeval==Wtf.producttype.assembly) ? true : false
        // addNewFn:this.showUom.createDelegate(this)
        }); 
     
        /*    this.ItemPriceList=new Wtf.form.ComboBox({
            fieldLabel:"<span wtf:qtip='"+"Item Price List" +"'>"+"Price List"+"</span>",
            hiddenName:'itempricelist',
            //store:Wtf.uomStore,
            anchor:'95%',
            //valueField:'uomid',
            //displayField:'uomname',
            forceSelection: true//,
        // addNewFn:this.showUom.createDelegate(this)
     }); 
 */    
     
        this.ItemPriceList=new Wtf.form.TextField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.ItemPriceList") +"'>"+WtfGlobal.getLocaleText("acc.rem.72")+"</span>",
            name:'itempricelist',
            id:'itempricelist'+this.id,
            anchor:'95%',
            disabled :(this.isUsedInTransaction && this.isEdit && !this.isClone && this.producttypeval==Wtf.producttype.assembly) ? true : false
        }); 
     
        /*    this.ShippingType=new Wtf.form.ComboBox({
            fieldLabel:"<span wtf:qtip='"+"Shipping type" +"'>"+"Shipping type"+"</span>",
            hiddenName:'shippingtype',
            //store:Wtf.uomStore,
            anchor:'95%',
            //valueField:'uomid',
            //displayField:'uomname',
            forceSelection: true//,
        // addNewFn:this.showUom.createDelegate(this)
     }); 
 */    
     
        this.ShippingType=new Wtf.form.TextField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.Shippingtype") +"'>"+WtfGlobal.getLocaleText("acc.product.Shippingtype")+"</span>",
            name:'shippingtype',
            id:'shippingtype'+this.id,
            anchor:'95%'
        }); 
    
        this.ItemActiveStatus= new Wtf.form.Checkbox({
            name:'activestatus',
            hiddenName:'activestatus',
            hidden : true,   // CheckBox is hidden beause Activate/De-activate functionality is separatly given under "Products And Servixes Menu" in Product master.
            hideLabel : true,
            id:'activestatus'+this.id,
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.Active/Inactive")+"'>"+WtfGlobal.getLocaleText("acc.designerTemplate.isActive")+"</span>",//WtfGlobal.getLocaleText("acc.product.IsRecyclable"),//"Is Recyclable"
            //            disabled:(this.isFixedAsset ||(this.isEdit && !this.isClone))?true:false,
            checked:true,
            itemCls:"chkboxalign"
        });
     
        this.ItemKnittingStatus= new Wtf.form.Checkbox({
            name:'isknittingitem',
            hiddenName:'isknittingitem',
            id:'isknittingitem'+this.id,
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.IsKittingitem")+"?"+"'>"+WtfGlobal.getLocaleText("acc.product.IsKittingitem")+"</span>",
            //            disabled:(this.isFixedAsset ||(this.isEdit && !this.isClone))?true:false,
            checked:false,
            itemCls:"chkboxalign"
        });
    
        this.reusabilityStore = new Wtf.data.SimpleStore({
            fields : ['value', 'name'],
            data : [
            ['0', 'Reusable'],
            ['1', 'Consumable']
            ]
        }); 
        
        this.itemReusability = new Wtf.form.ComboBox({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.ItemReusability")+"'>"+WtfGlobal.getLocaleText("acc.product.ItemReusability")+"</span>",
            name:'isreusable',
            id:'isreusable'+this.id,
            hiddenName:'isreusable',
            forceSelection:true,
            anchor:'85%',
            triggerAction:'all',
            //allowBlank:!this.isFixedAsset,
            editable:false,
            displayField:'name',
            valueField:'value',
            store:this.reusabilityStore,
            //disabledClass:"newtripcmbss",
            mode:'local',
            width:150
        });
        
        this.reusabilityCount= new Wtf.form.NumberField({
            fieldLabel: "<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.Re-usabilityCount")+"'>"+WtfGlobal.getLocaleText("acc.product.Re-usabilityCount")+"</span>",
            name: 'reusabilitycount',
            id:'reusabilitycount'+this.id,
            allowBlank:true,
            anchor:'85%',
            //maxLength:3,
            disabled :true ,
            value:0,
            allowDecimals:false,
            allowNegative:false,
            emptyText : 'N/A'
        });
        // For creting form fields Substitute Product and Qty for MRP module
        this.createSubstituteProductAndQtyFields();
    
        this.GeneralTab=null;
        this.PurchaseTab=null;
        this.SalesTab=null;
        this.PropertiesTab=null;
        this.RemarksTab=null;
        this.InventoryTab=null;
   
                 
    },
    addBrand: function () {
        addMasterItemWindow('53');
    },
    createQualityControlButtons: function() {
        this.qualityControlButtons = [];
        this.newBttnQC = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.New"), // "New",
            iconCls: getButtonIconCls(Wtf.etype.add),
            tooltip: WtfGlobal.getLocaleText("acc.field.newBOMToolTip"),
            id: 'qualityControlButtonsNew' + this.id
        });
        this.newBttnQC.on('click',this.newQC,this);
        this.qualityControlButtons.push(this.newBttnQC);
        
        this.editBttnQC = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.currency.update"), // "Update",
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            tooltip: WtfGlobal.getLocaleText("acc.field.updateBOMToolTip"),
            scope: this,
            disabled: true,
            id: 'qualityControlButtonsEdit' + this.id
        });
        this.editBttnQC.on('click',this.editDataQC,this);
        this.qualityControlButtons.push(this.editBttnQC);
        
        this.submitBttnQC = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.submit"), // "Submit",
            iconCls: getButtonIconCls(Wtf.etype.save),
            tooltip: WtfGlobal.getLocaleText("acc.field.submitBOMToolTip"),
            scope: this,
            id: 'qualityControlButtonsSubmit' + this.id
        });
        this.submitBttnQC.on('click',this.submitDataQC,this);
        this.qualityControlButtons.push(this.submitBttnQC);
    },
    
    createQualityControlFields: function(){
        this.qualityControlBOMCode = new Wtf.form.ComboBox({
            triggerAction: 'all',
            mode: 'local',
            id: 'qualityControlBOMCode' + this.id,
            fieldLabel: WtfGlobal.getLocaleText("acc.field.bomCode"),
             valueField: 'bomCode',
//            valueField: 'bomid',
            displayField: 'bomCode',
            store: this.createdBOMStore,
            emptyText: WtfGlobal.getLocaleText("acc.product.field.bomcode.emptytext"),
            width:222,
            listWidth:250,
            typeAhead: true,
            forceSelection: true,
            name: 'qcbomcodeid',
            hiddenName: 'qcbomcodeid'
        });
        
        this.qcUomStore = new Wtf.data.Store({
            url: "ACCUoM/getUnitOfMeasure.do",
            baseParams: {
                mode: 31,
                common: '1'
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, Wtf.uomRec)
        });
        this.qcUomStore.load(); //ERP-35191 : Load UOM store while opening create new product form.
        
        this.QCuom = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.mrp.qualitycontrol.uom"),
            emptyText: WtfGlobal.getLocaleText("acc.product.mrp.qualitycontrol.measurementuom"),
            hiddenName: 'uomid',
            name: 'uomid',
            width: 240,
            store: this.qcUomStore,
            valueField: 'uomid',
            displayField: 'uomname',
            forceSelection: true,
            extraFields: [],
            mode: 'remote'
        });    
       this.QCuom.addNewFn=this.showUom.createDelegate(this);
        this.qualityControlGroupRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);
        this.qualityControlGroupStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 56
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.qualityControlGroupRec)
        });
        this.qualityControlGroupStore.load();

        this.qualityControlGroup = new Wtf.form.ExtFnComboBox({
            triggerAction: 'all',
            mode: 'remote',
            id: 'qualityControlGroup' + this.id,
            fieldLabel: WtfGlobal.getLocaleText("acc.masterConfig.56") + "*",
            valueField: 'id',
            displayField: 'name',
            store: this.qualityControlGroupStore,
            emptyText: WtfGlobal.getLocaleText("acc.product.field.qcgroup.emptytext"),
            width: 240,
            typeAhead: true,
            forceSelection: true,
            listWidth: 300,
            name: 'qcgroupid',
            hiddenName: 'qcgroupid',
            allowBlank: false,
            extraFields: [],
            addNoneRecord: false
        });
        this.qualityControlGroup.addNewFn = this.addQualityControlGroup.createDelegate(this);
        this.qualityControlGroup.on('select', this.qualityControlGroupOnSelect, this);
        
        
        this.qualityControlParameterRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);
        this.qualityControlParameterStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 55
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.qualityControlParameterRec)
        });
        this.qualityControlParameterStore.on('beforeload', function (s, o) {

            s.baseParams.parentmasteritem = this.qualityControlGroup.getValue();
        }, this);
        this.qualityControlParameterStore.load();

        this.qualityControlParameter = new Wtf.form.ExtFnComboBox({
            triggerAction: 'all',
            mode: 'remote',
            id: 'qualityControlParameter' + this.id,
            fieldLabel: WtfGlobal.getLocaleText("acc.masterConfig.55") + "*",
            valueField: 'id',
            displayField: 'name',
            store: this.qualityControlParameterStore,
            emptyText: WtfGlobal.getLocaleText("acc.product.field.qcparameter.emptytext"),
            width: 240,
            typeAhead: true,
            forceSelection: true,
            listWidth: 300,
            name: 'qcparameterid',
            hiddenName: 'qcparameterid',
            allowBlank: false,
            extraFields: [],
            addNoneRecord: false
        });
        this.qualityControlParameter.addNewFn = this.addQualityControlParameter.createDelegate(this);
        this.qualityControlParameter.on('select', this.qualityControlParameterOnSelect, this);
        
        this.qualityControlValue=new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.field.qcpassrating"),
            name:'qcvalue',
            id:'qualityControlValue'+this.id,
            width: 240,
            maxValue:5,
            allowNegative:false
        });
        
        this.messagePanelValue = new Wtf.Panel({
            xtype: 'panel', 
            border: false,
            cls: 'passratingfield',
            html: WtfGlobal.getLocaleText("acc.product.field.qcvalue.html")
        });
        
        
        this.qualityControlDescription = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.masterConfig.taxes.gridDescription"),
            name: "qcdescription",
            id:'qualityControlDescription'+this.id,
            maxLength: 200,
            height:60,
            width: 240
        });
    },
    
    qualityControlGroupOnSelect: function(){
        this.qualityControlParameterStore.load({
            params:{
                parentmasteritem:this.qualityControlGroup.getValue()
            }
        });
        this.qualityControlParameter.reset();
    },
    
    qualityControlParameterOnSelect: function(){
        if(this.qualityControlGroup.getValue()==""){
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                msg: WtfGlobal.getLocaleText("acc.product.field.qcparameter.warningmsg"),
                width:370,
                buttons: Wtf.MessageBox.OK,
                icon: Wtf.MessageBox.WARNING,
                scope: this
            });
            this.qualityControlParameter.reset();
        }
    },
    openHistoryWindow: function() {
        var config = {};
        config.masterid = this.recordid;
        config.gsthistorydetails = false;
        config.parentObj = this;
        config.isProduct = true;
        callGSTHistoryInput(config);
    },
    disableProductTaxClass: function() {
        this.dimensionFieldArray = this.tagsFieldset.dimensionFieldArray;
        for (var itemcnt = 0; itemcnt < this.dimensionFieldArray.length; itemcnt++) {
            var fieldLabel = this.dimensionFieldArray[itemcnt].fieldLabel;
            if (fieldLabel == Wtf.GSTProdCategory + "*") {
                var fieldId = this.dimensionFieldArray[itemcnt].id;
                Wtf.getCmp(fieldId).setDisabled(true);
            }
        }
    },
    /*
     * 
     * @returns {Boolean} Function to provide check for HSN/SAC Code should not be greater than 8 digits (For India) ERM-1092
     */
    validationforHSNfield: function() {
        var isHSNvalid=true;
        this.dimensionFieldArray = this.tagsFieldset.dimensionFieldArray;
        for (var itemcnt = 0; itemcnt < this.dimensionFieldArray.length; itemcnt++) {
            var fieldLabel = this.dimensionFieldArray[itemcnt].fieldLabel;
            if (fieldLabel == Wtf.GSTHSN_SAC_Code + "*") {
                var fieldId = this.dimensionFieldArray[itemcnt].id;
                var HSNField = Wtf.getCmp(fieldId);
                if (HSNField != undefined) {
                    var hsnCode = HSNField.getRawValue();
                    if (hsnCode.length > Wtf.HSNMaxLength) {
                       isHSNvalid=false;
                       break;
                    }
                }
            }
        }
        return isHSNvalid;
    },
    createQualityControlForm: function(){
         this.qualityControlBOMCodeFormPanel = new Wtf.form.FormPanel({
            region: 'north',
            autoHeight: true,
            disabledClass: "newtripcmbss",
            hidden:!Wtf.account.companyAccountPref.activateMRPManagementFlag,
            border: false,
            items: [{
                    layout: 'form',
                    defaults: {
                        border: false
                    },
                    baseCls: 'northFormFormatforQCBOMCOde',
                    labelWidth: 150,
                    disabledClass: "newtripcmbss",
                    items:[
                            this.qualityControlBOMCode
                        
                    ]
                }
            ]
        });
        
        
        this.qualityControlForm = new Wtf.form.FormPanel({
            region: 'north',
            autoHeight: true,
            disabledClass: "newtripcmbss",
            hidden:!Wtf.account.companyAccountPref.activateMRPManagementFlag,
            border: false,
            items: [{
                    layout: 'form',
                    defaults: {
                        border: false
                    },
                    baseCls: 'northFormFormatforQC',
                    labelWidth: 150,
                    disabledClass: "newtripcmbss",
                    items:[
                        this.QCuom,
                        this.qualityControlGroup,
                        this.qualityControlParameter,
                        {
                            layout:'column',
                            border:false,
                            defaults:{border:false},
                            items:[{
                                layout:'form',
                                items:[
                                    this.qualityControlValue
                                ]
                            },{
                                layout:'form',
                                items:[
                                    this.messagePanelValue
                                ]
                            }]
                        },
                        this.qualityControlDescription
                    ]
                }
            ]
        });
    },
    
    createQualityControlFieldSet: function(){
        this.qualityControlFieldSet = new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.product.qualitycontrol"),
            xtype: 'fieldset',
            style: 'margin-top: 20px',
            cls: "visibleDisabled",
            layout: 'border',
            hidden:!Wtf.account.companyAccountPref.activateMRPManagementFlag,
            height: 410,
            border: false,
            items: [{
                region: 'center',
                cls: "visibleDisabled",
                border: false,
                items: [
                    this.qualityControlBOMCodeFormPanel,
                    this.qualityControlForm,
                    this.qualityControlGrid
                ]
            }]
        });
    },
    
    createQualityControlGrid: function(){
        this.smQC = new Wtf.grid.RowSelectionModel({
            singleSelect: true
        });
        
        this.cmQC = new Wtf.grid.ColumnModel([new Wtf.grid.RowNumberer(),
            {
                dataIndex: 'qcid',
                width: 150,
                hidden:true
            },{
                header: WtfGlobal.getLocaleText('acc.field.bomCode'),
                dataIndex: 'qcbomcode',
                hidden:true,
                width: 150
            },
//            {   //ERP-35191 : Do not show UUID in Grid
//                //header: WtfGlobal.getLocaleText("acc.product.mrp.qualitycontrol.uom"),    //ERP-35191 : Need not to show UUID
//                dataIndex: 'qcuom',
//                width: 150,
//                hidden:true,
//                renderer:Wtf.comboBoxRenderer(this.QCuom)
//            },
            {
                header: WtfGlobal.getLocaleText("acc.product.mrp.qualitycontrol.uom"),  //ERP-35191 : Measurement UOM Name
                dataIndex: 'uomname',
                width: 150
            },{
                header: WtfGlobal.getLocaleText('acc.masterConfig.56'),
                dataIndex: 'qcgroup',
                width: 150
            },{
                header: WtfGlobal.getLocaleText('acc.masterConfig.55'),
                dataIndex: 'qcparameter',
                width: 150
            },{
                header: WtfGlobal.getLocaleText('acc.product.field.qcpassrating'),
                dataIndex: 'qcvalue',
                width: 150,
                align:'centre'
            },{
                header: WtfGlobal.getLocaleText('acc.masterConfig.taxes.gridDescription'),
                dataIndex: 'qcdescription',
                width: 150
            },{
                header:WtfGlobal.getLocaleText("acc.product.gridAction"),
                align:'center',
                renderer: this.deleteRenderer.createDelegate(this)    
            }
        ]);
        
        this.gridRecordQC = new Wtf.data.Record.create([
            {name: 'qcid'},
            {name: 'qcbomcodeid'},
            {name: 'qcbomcode'},
            {name: 'qcgroupid'},
            {name: 'qcgroup'},
            {name: 'qcparameterid'},
            {name: 'qcparameter'},
            {name: 'qcvalue'},
            {name: 'qcdescription'},
            {name: 'deleted'}, 
            {name: 'productid'},
            {name: 'qcuom'},
            {name: 'uomname'}   //ERP-35191
        ]);
        
        this.gridReaderQC = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: "count"
        }, this.gridRecordQC);
        
        this.qualityControlStore = new Wtf.data.GroupingStore({
            url: "ACCProduct/getQualityControlData.do",
            reader: this.gridReaderQC,
            groupField:"qcbomcode",
            sortInfo: {
                field: 'qcbomcode', 
                direction: "ASC"
            }
        });
        
        var productid=(this.record!=undefined && this.record.data!=undefined)?this.record.data.productid:"";
        if (productid != "" && Wtf.account.companyAccountPref.activateMRPManagementFlag) {
            this.qualityControlStore.load({
                params:{
                     productid:productid
                }
            },this);
        }
        
        this.groupingView = new Wtf.grid.GroupingView({
            forceFit: true,
            showGroupName: false,
            enableGroupingMenu: true,
            groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ?"'+WtfGlobal.getLocaleText("acc.item.plural")+'":"'+WtfGlobal.getLocaleText("acc.item")+'"]})',//"Items" : "Item"]})',
            hideGroupedColumn:true,
            emptyText: "<div class='grid-empty-text'>"+WtfGlobal.getLocaleText("acc.common.norec")+"</div>"
        });
        
        this.qualityControlGrid = new Wtf.grid.GridPanel({
            id: 'qualityControlGrid'+this.id,
            store: this.qualityControlStore,
            hidden:!Wtf.account.companyAccountPref.activateMRPManagementFlag,
            cm: this.cmQC,
            border: false,
            sm: this.smQC,
            trackMouseOver: true,
            autoScroll:true,
            loadMask: {
                msg: WtfGlobal.getLocaleText("acc.msgbox.50")
            },
            height: 200,
            style: 'margin-left:5px; margin-right:5px',
            view: this.groupingView,
            tbar: this.qualityControlButtons
        });
                
        this.qualityControlGrid.on("rowclick", this.rowClickHandleQC, this);
    },
    
    addQualityControlGroup: function() {
        addMasterItemWindow('56');
    },
    
    addQualityControlParameter: function() {
        addMasterItemWindow('55');
        this.qualityControlGroup.reset();//Reset Quality Group if new parameter is added. So as to load new added parameter
    },
    
    rowClickHandleQC: function(grid,rowindex,e) {
        if (e.getTarget(".delete-gridrow")) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.setupWizard.note27"), function(btn) {
                if (btn == "yes") {
                    var store = grid.getStore();
                    store.remove(store.getAt(rowindex));
                    grid.getView().refresh();
                    
                    // if row is deleted then reset the form for new case
                    this.newQC();
                }
            }, this);
        } else {
            this.submitBttnQC.disable();
            this.editBttnQC.enable();
            this.newBttnQC.enable();
            
            var rec = this.qualityControlGrid.getSelectionModel().getSelected();
            if (rec != undefined && rec != null) {
                this.qualityControlForm.getForm().loadRecord(rec);
                this.qualityControlGroup.setValue(rec.data.qcgroupid);
                this.QCuom.setValue(rec.data.qcuom);
                this.qualityControlParameterStore.load({
                    params:{
                        parentmasteritem: this.qualityControlGroup.getValue()
                    }
                });
                this.qualityControlParameterStore.on('load', function(){
                    this.qualityControlParameter.setValue(rec.data.qcparameterid);
                }, this);
                /*Set Bom code value when user click on record in qc grid*/
                this.qualityControlBOMCode.setValue(rec.data.qcbomcode);
            } else {
                // if row is not selected then reset the form for new case
                this.newQC();
            }
        }
    },
    
    newQC: function() {
        this.qualityControlGrid.getSelectionModel().clearSelections();
        this.submitBttnQC.enable();
        this.editBttnQC.disable();
        this.qualityControlForm.getForm().reset();
        this.qualityControlBOMCodeFormPanel.getForm().reset();
    },
    
    editDataQC: function() {
        if (this.qualityControlForm.getForm().isValid()) {
            if (this.qualityControlGrid.getSelectionModel().hasSelection()) {
                var qualityControlGridRec = this.qualityControlGrid.getSelectionModel().getSelected();

                // for start updating record in grid
                qualityControlGridRec.beginEdit();

                var qualityControlFormRec = this.qualityControlForm.getForm().getValues();
//                var qcbomcodeid = qualityControlFormRec.qcbomcodeid;

                /*Take uuid of bom if present from bomstore in edit case when boms are already saved to db*/
                var bomRec = WtfGlobal.searchRecord(this.createdBOMStore, this.qualityControlBOMCode.getRawValue(), 'bomCode');
                var qcbomcodeid = bomRec!=undefined ? bomRec.data.bomid :"";
                
                qualityControlGridRec.set("qcid", qualityControlFormRec.qcid);
                qualityControlGridRec.set("qcbomcodeid", qcbomcodeid);
                qualityControlGridRec.set("qcuom", this.QCuom.getValue());
                qualityControlGridRec.set("qcbomcode", this.qualityControlBOMCode.getRawValue());
                qualityControlGridRec.set("qcgroupid", qualityControlFormRec.qcgroupid);
                qualityControlGridRec.set("qcgroup", this.qualityControlGroup.getRawValue());
                qualityControlGridRec.set("qcparameterid", qualityControlFormRec.qcparameterid);
                qualityControlGridRec.set("qcparameter", this.qualityControlParameter.getRawValue());
                qualityControlGridRec.set("qcvalue", qualityControlFormRec.qcvalue);
                qualityControlGridRec.set("qcdescription", qualityControlFormRec.qcdescription);

                // complete updation process
                qualityControlGridRec.endEdit();
                qualityControlGridRec.commit();
                this.qualityControlGrid.getView().refresh();
            }
        } else {
            WtfComMsgBox(2, 2);
        }
    },
    
    submitDataQC: function(store, ID, idname) {
        if (this.qualityControlForm.getForm().isValid()) {
            // insert record in bom created grid
            var rec = this.qualityControlForm.getForm().getValues();
//            var qcbomcodeid = rec.qcbomcodeid!=undefined?rec.qcbomcodeid:'';
            var qcbomcode= this.qualityControlBOMCode.getRawValue() != "" ? this.qualityControlBOMCode.getRawValue() : "Default";
            
            /*Take uuid of bom if present from bomstore in edit case when boms are already saved to db*/
            var bomRec = WtfGlobal.searchRecord(this.createdBOMStore, qcbomcode, 'bomCode');
            var qcbomcodeid = bomRec!=undefined ? bomRec.data.bomid : "";
            
            //Check if store is already having same qcbomcodeid, qcgroupid, qcparameterid
            var qcParameterIdIndex = -1;
            var qcbomcodeIdIndex = WtfGlobal.searchRecordIndex(this.qualityControlStore, qcbomcodeid, 'qcbomcodeid');
            if(qcbomcodeIdIndex != -1){
                        var qcGroupIdIndex = WtfGlobal.searchRecordIndex(this.qualityControlStore, rec.qcgroupid, 'qcgroupid');
                if(qcGroupIdIndex != -1){
                            qcParameterIdIndex = WtfGlobal.searchRecordIndex(this.qualityControlStore, rec.qcparameterid, 'qcparameterid');
                        }
                    }
            // Check if UOM if diffent then add such record
            var uomid = -1;
            if (qcParameterIdIndex != -1) {
                uomid = WtfGlobal.searchRecordIndex(this.qualityControlStore, rec.uomid, 'qcuom');
            }
            var record = new this.gridRecordQC({
                qcid:'',
                qcbomcodeid: qcbomcodeid,
                qcuom: this.QCuom.getValue(),
                uomname: this.QCuom.getRawValue(),  //ERP-35191
                qcbomcode: qcbomcode,
                qcgroupid: rec.qcgroupid,
                qcgroup: this.qualityControlGroup.getRawValue(),
                qcparameterid: rec.qcparameterid,
                qcparameter: this.qualityControlParameter.getRawValue(),
                qcvalue:rec.qcvalue,
                qcdescription:rec.qcdescription
            });
            
            if(qcParameterIdIndex != -1 && uomid!=-1){
                //Remove record from store if new record is already having same qcbomcodeid, qcgroupid, qcparameterid
                this.qualityControlStore.remove(this.qualityControlStore.getAt(qcParameterIdIndex));
                //Add new record to store - To perform it like update
                this.qualityControlStore.insert(qcParameterIdIndex, record);
            }else{
                this.qualityControlStore.add(record);
            }
            
//            this.submitBttnQC.disable();
            this.newQC();
        } else {
            WtfComMsgBox(2, 2);
        }
    },
    
    // for getting QC details JSON for submit
    getQualityControlDetailsJSON: function() {
        var jsonstring = "";
        if(this.qualityControlGrid.getStore().getCount()>0){
            this.qualityControlGrid.getStore().each(function(rec) {
                var qcbomcodeid = (rec.data['qcbomcodeid']!=undefined && rec.data['qcbomcodeid']!="undefined")?rec.data['qcbomcodeid']:"";
                jsonstring += "{" + 
                    "qcid:\"" + rec.data['qcid'] + "\"," +
                    "qcbomcodeid:\"" + qcbomcodeid + "\"," + 
                    "qcbomcode:\"" + rec.data['qcbomcode'] + "\"," + 
                    "qcuom:\"" + rec.data['qcuom'] + "\"," + 
                    "uomname:\"" + rec.data['uomname'] + "\"," +    //ERP-35191
                    "qcgroupid:\"" + rec.data['qcgroupid'] + "\"," + 
                    "qcparameterid:\"" + rec.data['qcparameterid'] + "\"," + 
                    "qcvalue:\"" + rec.data['qcvalue'] + "\"," + 
                    "qcdescription:\"" + rec.data['qcdescription'] + "\"" +
                    "},";
            }, this);
            jsonstring = jsonstring.substr(0, jsonstring.length - 1);
        }
        return jsonstring;
    },
    
    depreciationMethodSelected:function(combo,rec,idx){
        if(rec.get('depreciationmethodvalue') == 3){
            this.isDepreciable = false;
            this.PNLTypeAccount.allowBlank = true;
            this.PNLTypeAccount.clearValue();
            this.PNLTypeAccount.disable();
            
            this.depreciationRate.allowBlank = true;
            this.depreciationRate.setValue(0);
            this.depreciationRate.disable();
            
            this.balanceSheetTypeAccount.allowBlank = true;
            this.balanceSheetTypeAccount.clearValue();
            this.balanceSheetTypeAccount.disable();
            
            this.depreciationCostLimit.allowBlank = true;
            this.depreciationCostLimit.disable();
            
            this.depreciationRate.allowBlank = true;
            this.depreciationRate.disable();
        }else{
            this.isDepreciable = true;
            this.PNLTypeAccount.allowBlank = false;
            this.PNLTypeAccount.enable();
            
            this.depreciationRate.allowBlank = false;
            this.depreciationRate.enable();
            
            this.balanceSheetTypeAccount.allowBlank = true;
            this.balanceSheetTypeAccount.enable();
            
            this.depreciationCostLimit.allowBlank = false;
            this.depreciationCostLimit.enable();
            
            this.depreciationRate.allowBlank = true;
            this.depreciationRate.enable();
        }
    },
    
    createFixedAssetEntryDetails:function(){
        
        this.depreciationStore = new Wtf.data.SimpleStore({
            fields : ['depreciationmethodvalue', 'depreciationMethodType'],
            data : [
            ['1', 'Straight Line Depreciation'],
            ['2', 'Double Decline Depreciation'],
            ['3', 'Non Depreciable']
            ]
        }); 
        
        this.depreciationMethodCombo= new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.fixed.asset.dep.method"),
            name: 'depreciationMethod',
            forceSelection:true,
            anchor:'85%',
            triggerAction:'all',
            allowBlank:!this.isFixedAsset,
            editable:false,
            displayField:'depreciationMethodType',
            valueField:'depreciationmethodvalue',
            store:this.depreciationStore,
            disabledClass:"newtripcmbss",
            mode:'local',
            value:1,
            width:150
        });
        
        this.depreciationMethodCombo.on('select',this.depreciationMethodSelected,this);
        
        //        if(this.isEdit && this.isFixedAsset){
        //            this.depreciationMethodCombo.setValue(this.record.data.depreciationMethod);
        //        }
        
        this.depreciationRate = new Wtf.form.NumberField({
            fieldLabel : WtfGlobal.getLocaleText("acc.fixed.asset.dep.rate"),
            name:'depreciationRate',
            maxValue : 100,
            anchor:'85%',
            allowBlank:!this.isFixedAsset,
            allowNegative:false,
            decimalPrecision:2,
            value:0
        });
        
        this.depreciationCostLimit = new Wtf.form.NumberField({
            fieldLabel : WtfGlobal.getLocaleText("acc.fixed.asset.dep.cost.limit"),
            name:'depreciationCostLimit',
            allowNegative:false,
            allowBlank:!this.isFixedAsset,
            hidden:true,
            hideLabel:true,
            anchor:'85%',
            decimalPrecision:2,
            value:0
        });
        
        this.accRec = Wtf.data.Record.create([
        {
            name:'accountname',
            mapping:'accname'
        },

        {
            name:'accountid',
            mapping:'accid'
        },

        {
            name:'acccode'
        },

        {
            name:'groupname'
        },
        {
            name:'hasAccess'
        }
        ]);
        
        this.balanceSheetTypeAccountStore = new Wtf.data.Store({
              url:"ACCAccountCMN/getAccountsIdNameForCombo.do",
            baseParams:{
                mode:2,
                ignorecustomers:true,  
                ignorevendors:true,
                onlyBalancesheet:true,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
        
        
        
        this.balanceSheetTypeAccount=new Wtf.form.ExtFnComboBox({
            hiddenName:'depreciationProvisionGLAccount',
            name:'depreciationProvisionGLAccount',
            store:this.balanceSheetTypeAccountStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.fixed.asset.dep.provision.account"),
            minChars:1,
            listWidth :300,
            anchor:'85%',
            allowBlank:true,
            valueField:'accountid',
            displayField:'accountname',
            isAccountCombo:true,
            forceSelection:true,
            hirarchical:true,
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            mode: 'local',
            typeAheadDelay:30000,
            extraComparisionField:'acccode'
        });
        
        this.balanceSheetTypeAccount.on('beforeselect',function(combo,record,index){
            return validateSelection(combo,record,index);
        },this);
        this.balanceSheetTypeAccountStore.on('load',function(){
            if(this.isEdit && this.isFixedAsset){
                this.balanceSheetTypeAccount.setValue(this.record.data.depreciationProvisionGLAccount);
            }
        },this);
        
        
        this.balanceSheetTypeAccountStore.load();
        
       
        this.PNLTypeAccountStore = new Wtf.data.Store({
//            url:"ACCAccountCMN/getAccountsForCombo.do",
//            url:"ACCAccountCMN/getAccountsForComboOptimized.do",
              url:"ACCAccountCMN/getAccountsIdNameForCombo.do",
            baseParams:{
                mode:2,
                ignorecustomers:true, 
                ignorevendors:true,
                nondeleted: true,
                ignoreGSTAccounts:true,
                nature:[2]
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
        
        //        this.exStore = new Wtf.data.Store({
        //            url : "ACCAccountCMN/getAccountsForCombo.do",
        //            baseParams:{
        //                mode:2,
        //                ignoreCashAccounts:true,
        //                ignoreBankAccounts:true,
        //                ignoreGSTAccounts:true,  
        //                ignorecustomers:true,  
        //                  ignorevendors:true,
        //                nondeleted:true
        //            },
        //            reader: new Wtf.data.KwlJsonReader({
        //                root: "data"
        //            },this.accRec)
        //        });
        
        
        this.salesAccountStore = new Wtf.data.Store({
//            url:"ACCAccountCMN/getAccountsForCombo.do",
//            url:"ACCAccountCMN/getAccountsForComboOptimized.do",
              url:"ACCAccountCMN/getAccountsIdNameForCombo.do",
            baseParams:{
                mode:2,
                ignorecustomers:true, 
                ignorevendors:true,
                ignoreGSTAccounts:true,
                nondeleted: true,
                nature:[2,3]
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
        
        
        this.PNLTypeAccount=new Wtf.form.ExtFnComboBox({
            hiddenName:'depreciationGLAccount',
            name:'depreciationGLAccount',
            store:this.PNLTypeAccountStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.fixed.asset.dep.depreciation.account"),
            minChars:1,
            listWidth :300,
            anchor:'85%',
            allowBlank:!this.isFixedAsset,
            valueField:'accountid',
            isAccountCombo:true,
            displayField:'accountname',
            forceSelection:true,
            hirarchical:true,
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            mode: 'local',
            typeAheadDelay:30000,
            extraComparisionField:'acccode'
        });
        
        this.PNLTypeAccount.on('beforeselect',function(combo,record,index){
            return validateSelection(combo,record,index);
        },this);
        
        this.PNLTypeAccountStore.on('load',function(){
            if(this.isEdit && this.isFixedAsset){
                this.PNLTypeAccount.setValue(this.record.data.depreciationGLAccount);
            }
        },this);
        
        this.PNLTypeAccountStore.load();
        
        this.saleAssetAccount=new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.fixed.asset.dep.profitloss.account"),
            store:this.salesAccountStore,
            name:'sellAssetGLAccount',
            anchor:'85%',
            hiddenName:'sellAssetGLAccount',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAhead: true,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            typeAheadDelay:30000,
            valueField:'accountid',
            displayField:'accountname',
            forceSelection: true,
            allowBlank:!this.isFixedAsset
        });
        
        this.saleAssetAccount.on('beforeselect',function(combo,record,index){
            return validateSelection(combo,record,index);
        },this);
        
        this.salesAccountStore.on('load',function(){
            if(this.isEdit && this.isFixedAsset){
                this.saleAssetAccount.setValue(this.record.data.assetSaleGL);
            }
        },this);
        
        this.salesAccountStore.load();
        
        this.fixedAssetFieldSet = new Wtf.form.FieldSet({
            title:'Asset Settings',
            autoHeight: true,
            autoWidth: true,
            hidden:!this.isFixedAsset,
            border:false,
            layout:'column',
            items:[{
                layout:'form',
                border:false,
                columnWidth:0.32,
                items:[this.depreciationMethodCombo,this.PNLTypeAccount]
            },{
                layout:'form',
                border:false,
                columnWidth:0.32,
                items:[this.depreciationRate,this.balanceSheetTypeAccount]
            },{
                layout:'form',
                border:false,
                columnWidth:0.34,
                items:[this.depreciationCostLimit,this.saleAssetAccount]
            }]
        //            items:[this.depreciationMethodCombo,this.depreciationRate,this.depreciationCostLimit,this.PNLTypeAccount,this.balanceSheetTypeAccount,this.saleAssetAccount]
        });
        
    },
    createProductComposition:function(){        
        this.productCompositionDetailsCM= new Wtf.grid.ColumnModel([{
            header:WtfGlobal.getLocaleText("erp.field.srno"),
            width:150,
            dataIndex:'srno',
            editor:new Wtf.form.NumberField({
                name:'srno'
            }) 
        },{
            header:WtfGlobal.getLocaleText("erp.product.Ingredients"),
            dataIndex:'ingredients',
            width:600,
            editor:new Wtf.form.TextField({
                name:'ingredients'
            })     
        },{
            header:WtfGlobal.getLocaleText("erp.product.strength"),
            dataIndex:'strength',
            width:250,
            editor:new Wtf.form.TextField({
                name:'strength'
            }) 
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridAction"),//"Action",
            align:'center',
            width:50,
            renderer: this.deleteRenderer.createDelegate(this)
        }]);
              
        this.productCompositionRec = new Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'srno'
        },

        {
            name: 'ingredients'
        },
        {
            name: 'strength'
        }]);
        
        var url=Wtf.req.account+((this.fromOrder||this.readOnly)?((this.isCustomer)?'CustomerManager.jsp':'VendorManager.jsp'):((this.isCN)?'CustomerManager.jsp':'VendorManager.jsp'));
        this.productCompositionStore = new Wtf.data.Store({
            url:url,
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.productCompositionRec)
        });
        this.productCompositionStore.on('beforeload',function(){
            if(this.isEdit && this.record.data.activateProductComposition &&!this.isFixedAsset){
                this.productCompositionArr=this.record.data['productCompositionDetails'];
                var productCompositionLength = 0;
                productCompositionLength = this.productCompositionArr.length;           
                for(var i=0;i<productCompositionLength;i++){              
                    var rec=new this.productCompositionRec({
                        id:this.productCompositionArr[i].id!=undefined?this.productCompositionArr[i].id:"",
                        srno:this.productCompositionArr[i].srno!=undefined?this.productCompositionArr[i].srno:"",
                        ingredients:this.productCompositionArr[i].ingredients!=undefined?this.productCompositionArr[i].ingredients:"",
                        strength:this.productCompositionArr[i].strength!=undefined?this.productCompositionArr[i].strength:""
                    }); 
                    this.productCompositionStore.add(rec);
                }
            }
        },this);
       
        this.productCompositionStore.load();
        
        this.productCompositionGrid = new Wtf.grid.EditorGridPanel({
            clicksToEdit:1,
            autoScroll:true,
            height:200,
            autoWidth:true,
            store: this.productCompositionStore,
            cm: this.productCompositionDetailsCM,
            border : false,
            loadMask : true, 
            viewConfig: {
                forceFit:false,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });      
        this.productCompositionGrid.on('afteredit',this.updateRow,this);
        this.productCompositionGrid.on('rowclick',this.handleRowClick,this);
        this.productCompositionGrid.on('render',this.addBlankRow,this);
        
        this.on('beforeedit',function(e){
            if(e.field == "srno" && e.grid.colModel.config[3].dataIndex=="srno"){                
            }  
        },this);
         
        this.productCompositionFieldSet=new Wtf.form.FieldSet({
            title:WtfGlobal.getLocaleText("erp.ProductComposition"),
            checkboxToggle: true,
            id:'activateProductCompositionFieldSet',
            height:200,
            hidden:!Wtf.account.companyAccountPref.activateProductComposition,
            border:false,
            width:'97%',           
            checkboxName: 'activateProductComposition',
            checkboxId:"activateProductComposition",
            collapsed:this.isEdit?!this.record.data.activateProductComposition:true,
            layout:'column',
            items:{
                columnWidth:1,
                border:false,
                height:200,
                labelWidth:110,
                id : 'comp_0',
                layout:'form',
                items:this.productCompositionGrid
            }
        });
 
        this.productCompositionFieldSet.on("expand",function(){
            if(Wtf.getCmp("as")){
                Wtf.getCmp("as").doLayout();
            }
        },this);
    },
    
    addBlankRow:function(){
        var Record = this.productCompositionStore.reader.recordType,f = Record.prototype.fields, fi = f.items, fl = f.length;
        var values = {},blankObj={};
        for(var j = 0; j < fl; j++){
            f = fi[j];
            if(f.name!='rowid') {
                blankObj[f.name]='';
                if(!Wtf.isEmpty(f.defValue))
                    blankObj[f.name]=f.convert((typeof f.defValue == "function"?f.defValue.call():f.defValue));
            }
        }
        var newrec = new Record(blankObj);
        this.productCompositionStore.add(newrec);
    },
    
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.nee.48"), function(btn){
                if(btn!="yes") return;
                var store=grid.getStore();
                var total=store.getCount();
                var record = store.getAt(rowindex);        
                    if(record.data.copyquantity!=undefined){                    
                        var deletedData=[];
                        var newRec=new this.productCompositionRec({
                            srno:record.data.srno,
                            ingredients:record.data.ingredients,    
                            strength:record.data.strength
                        });                            
                        deletedData.push(newRec);
                        this.deleteStore.add(deletedData);                            
                    }
                    store.remove(store.getAt(rowindex));
                    if(rowindex==total-1){
                        this.addBlankRow();
                    }
                    this.fireEvent('datachanged',this);
                    this.fireEvent('productdeleted',this);
            }, this);
        }
    },
    
    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },
    
    updateRow:function(obj){
        if(this.productCompositionStore.getCount()>0&&this.productCompositionStore.getAt(this.productCompositionStore.getCount()-1).data['srno'].length<=0){
            return;
        }
        this.addBlankRow();            
    },
    
/* This method used to update field label of this.productWeightPerStockUom and this.productWeightIncludingPakagingPerStockUom at run time*/
    updateWeightFieldsLabel : function(){
        var uomRawValue = this.uom.getRawValue();
        var newLabel= WtfGlobal.getLocaleText("acc.productList.productWeightPerStockUomAtRuntime")+uomRawValue+"):";
        WtfGlobal.updateFormLabel(this.productWeightPerStockUom,newLabel);
        newLabel = WtfGlobal.getLocaleText("acc.productList.productWeightIncludingPakagingPerStockUomAtRunTime")+uomRawValue+"):";
        WtfGlobal.updateFormLabel(this.productWeightIncludingPakagingPerStockUom,newLabel);
        
        newLabel = WtfGlobal.getLocaleText("acc.productList.productVolumePerStockUomAtRuntime")+uomRawValue+"):";
        WtfGlobal.updateFormLabel(this.productVolumePerStockUom,newLabel);
        newLabel = WtfGlobal.getLocaleText("acc.productList.productVolumeIncludingPakagingPerStockUomAtRunTime")+uomRawValue+"):";
        WtfGlobal.updateFormLabel(this.productVolumeIncludingPakagingPerStockUom,newLabel);
        
    },
    
    getProductCompositionDetails:function(){
        var cnt = this.productCompositionStore.getCount()-1;
        var jsonstring="";
        if(this.productCompositionStore.getCount()>0){
            for(var i=0;i<cnt;i++){
                var rec = this.productCompositionStore.getAt(i);
                jsonstring += "{srno:\""+rec.data['srno']+"\","+
                "ingredients:"+rec.data['ingredients']+","+ "strength:"+rec.data['strength']+"},";
            }
            jsonstring = jsonstring.substr(0, jsonstring.length-1);
        }
        return jsonstring;
    },
    
    getNextSequenceNumber:function(a,val){
        
        //        this.setTransactionNumber(true);
        if(!(a.getValue()=="NA")){
            var rec=WtfGlobal.searchRecord(this.sequenceFormatStore, a.getValue(), 'id');
            var oldflag=rec!=null?rec.get('oldflag'):true;
            Wtf.Ajax.requestEx({
                //             url:"ACCProduct/getProductIDAutoNumber.do",
                //             params: {dummyParam:true}
                url:"ACCCompanyPref/getNextAutoNumber.do",
                params:{
                    from:51,
                    sequenceformat:a.getValue(),
                    oldflag:oldflag
                }
            }, this,function(resp){
                if(resp.data=="NA"){
                    this.PID.reset();
                    this.PID.enable();
                }else {
                    this.PID.setValue(resp.data);
                    this.PID.disable();
                    WtfGlobal.hideFormElement(this.PID);
                }
            
            });
        } else {
            this.PID.reset();
            this.PID.enable();
            WtfGlobal.showFormElement(this.PID);
        }
    },
    isInventory:function(){
        //        if(this.producttype.getValue()==Wtf.producttype.noninvpart||this.producttype.getValue()==Wtf.producttype.service)
        //            this.addAccount(this.purchaseAccStore,false,false)
        //        else
        this.addAccount(this.purchaseAccStore,false,true)
    },    
    addMasterItem:function(id,winid,outer){
        callInventoryWindow(id,winid);
     
        Wtf.getCmp('inventorysetup').on('update', function(){
            id=="location"? this.locationStore.reload() : this.wareHouseStore.reload();
        }, this);
     
    },
     
    createToggleFields:function(){
        this.rQuantity= new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.reorderQty.tt")+"'>"+  WtfGlobal.getLocaleText("acc.product.reorderQty") +"</span>",//WtfGlobal.getLocaleText("acc.product.reorderQty"),//"Reorder Quantity* "+WtfGlobal.addLabelHelp("Reorder Quantity defines minimum quantity of items to be ordered when the stock reaches below Reorder Level."),
            name: 'reorderquantity',
            id:'reorderquantity'+this.id,
            hidden:this.isFixedAsset,
            hideLabel:this.isFixedAsset,
            anchor:'70%',
            maxLength:15,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL,
            //  allowDecimals:false,
            allowNegative:false
        });
        this.reorderLevel= new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.reorderLevel.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.reorderLevel") +"</span>",// WtfGlobal.getLocaleText("acc.product.reorderLevel"),//"Reorder Level* "+WtfGlobal.addLabelHelp("Reorder Level defines a stock level for item at which a new purchase order for items needs to be placed.In simple terms, it denotes the level of stock at which a replenishment order should be placed."),
            name: 'reorderlevel',
            id:'reorderlevel'+this.id,
            anchor:'70%',
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL,
            hidden:this.isFixedAsset,
            hideLabel:this.isFixedAsset,
            maxLength:15,
            // allowDecimals:false,
            allowNegative:false
        });
        this.leadtime= new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.leadTime.tt")+"'>"+ WtfGlobal.getLocaleText("acc.productList.gridLeadTime") +"</span>",// WtfGlobal.getLocaleText("acc.product.leadTime"),//'Lead Time(in days)*'+WtfGlobal.addLabelHelp("The amount of time between the placing of an order and the receipt of the goods ordered."),
            name: 'leadtime',
            id:'leadtime'+this.id,
            allowBlank:this.isFixedAsset,
            hidden:this.isFixedAsset,
            hideLabel:this.isFixedAsset,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL,
            anchor:'70%',
            maxLength:4,
            value:0,
            allowDecimals:false,
            allowNegative:false,
            maxValue:1095
        });
        
        this.QAleadtime= new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.QALeadTime") +"'>"+WtfGlobal.getLocaleText("acc.product.QALeadTime")+"</span>",// WtfGlobal.getLocaleText("acc.product.leadTime"),//'Lead Time(in days)*'+WtfGlobal.addLabelHelp("The amount of time between the placing of an order and the receipt of the goods ordered."),
            name: 'QAleadtime',
            id:'QAleadtime'+this.id,
            allowBlank:this.isFixedAsset,
            hidden:this.isFixedAsset,
            hideLabel:this.isFixedAsset,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL,
            anchor:'70%',
            maxLength:3,
            value:0,
            allowDecimals:false,
            allowNegative:false,
            maxValue:365
        });
        this.hsCode= new Wtf.form.TextField({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.field.HSCodeTTip")+"'>"+ WtfGlobal.getLocaleText("acc.field.HSCodeTTip")+"</span>",
            name: 'hscode',
            id:'hscode'+this.id,
            disabled : Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA ? true :false,
            hidden : Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA ? true :false,
            hideLabel : Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA ? true :false,
            anchor:'70%',
            validator: function(val) {
                if(Wtf.exciseTariffdetails){
                    if (val.match(/[0-9 | \s]/)) {
                        return true;
                    }
                    else {
                        return "Only Numbers are allowed";
                    }
                } else{
                    return true;
                }
            },
            maxLength:Wtf.exciseTariffdetails? 9:15,
            maskRe:Wtf.exciseTariffdetails?/[0-9 | \s]/:/./
        });
        
        this.warrantyperiod= new Wtf.form.NumberField({
            fieldLabel: "<span wtf:qtip='"+((this.isFixedAsset)?WtfGlobal.getLocaleText("acc.fixedasset.WarrantyPeriod(indays).tt"): WtfGlobal.getLocaleText("acc.field.WarrantyPeriod(indays).tt"))+"'>"+ WtfGlobal.getLocaleText("acc.field.WarrantyPeriod(indays)")+"</span>",//WtfGlobal.getLocaleText("acc.field.WarrantyPeriod(indays)"),
            name: 'warrantyperiod',
            id:'warrantyperiod'+this.id,
            allowBlank:true,
            anchor:'70%',
            maxLength:3,
            //value:0,
            allowDecimals:false,
            allowNegative:false,
            emptyText : 'N/A'
            
        });
        this.warrantyperiodSal= new Wtf.form.NumberField({
            fieldLabel: "<span wtf:qtip='"+ ((this.isFixedAsset)?WtfGlobal.getLocaleText("acc.fixedasset.WarrantyPeriodS(indays).tt"):WtfGlobal.getLocaleText("acc.field.WarrantyPeriodS(indays).tt"))+"'>"+ WtfGlobal.getLocaleText("acc.field.WarrantyPeriodS(indays)")+"</span>",//WtfGlobal.getLocaleText("acc.field.WarrantyPeriodS(indays)"),
            name: 'warrantyperiodsal',
            id:'warrantyperiodsal'+this.id,
            allowBlank:true,
            anchor:'70%',
            maxLength:3,
            //value:0,
            allowDecimals:false,
            allowNegative:false,
            emptyText : 'N/A'
            
        });
        this.quantity=new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.initialQty.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.initialQty")+"</span>",//WtfGlobal.getLocaleText("acc.product.initialQty"),//"Initial Quantity",// "+WtfGlobal.addLabelHelp("Transactional data related to initial quantity will not be reflected"),
            name:'initialquantity',
            id:'initialquantity'+this.id,
            anchor:'95%',
            maxLength:15,
            disabled :(this.isEdit && !this.isClone)?true:false,
            allowNegative :false,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL,
            //allowDecimals:false,
            value:0
        });
        
        this.quantity.on("blur",this.asofdatevalidation,this);        //validate as of date if initial quantity > 0   
        this.quantity.on("change",this.checkValue,this);
                
        this.minOrderingQuantity=new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.minOrderingQuantity.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.minOrderingQuantity")+"</span>",//"Minimum Ordering Quantity"
            name:'minorderingquantity',
            id:'minorderingquantity'+this.id,
            anchor:'95%',
            maxLength:15,
            hidden : !Wtf.account.companyAccountPref.isMinMaxOrdering,
            hideLabel : !Wtf.account.companyAccountPref.isMinMaxOrdering,
            allowNegative :false,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL,
            value:0
        });
        
        this.maxOrderingQuantity=new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.maxOrderingQuantity.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.maxOrderingQuantity")+"</span>",//"Maximum Ordering Quantity",
            name:'maxorderingquantity',
            id:'maxorderingquantity'+this.id,
            anchor:'95%',
            maxLength:15,
            hidden : !Wtf.account.companyAccountPref.isMinMaxOrdering,
            hideLabel : !Wtf.account.companyAccountPref.isMinMaxOrdering,
            allowNegative :false,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL,
            value:0
        });
      
       // this.quantity.on("blur",this.checkBatchSerial,this);
        
        this.initialprice = new Wtf.form.NumberField({
            fieldLabel: "<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.initialPurchasePrice.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.initialPurchasePrice")+"</span>", // 'Initial Purchase Price',
            name: 'initialprice',
            id: 'initialprice'+this.id,
            maxLength: 15,
            decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL,
            anchor: '70%',
            allowBlank: (this.producttypeval==Wtf.producttype.service),
            allowNegative: false,
            hidden : WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.showpurchaseprice),
            hideLabel :WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.showpurchaseprice),
            value: 0
        });
        
        this.initialsalesprice = new Wtf.form.NumberField({
            fieldLabel: "<span wtf:qtip='"+((this.isFixedAsset)?WtfGlobal.getLocaleText("acc.fixedasset.salesPrice.tt"): WtfGlobal.getLocaleText("acc.product.salesPrice.tt"))+"'>"+ WtfGlobal.getLocaleText("acc.agedPay.gridCurrent") + " " + WtfGlobal.getLocaleText("acc.product.salesPrice")+"</span>", // 'Sales Price',
            name: 'initialsalesprice',
            id: 'initialsalesprice'+this.id,
            maxLength: 15,
            anchor: '70%',
            allowNegative: false,
            decimalPrecision: Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL,
            value: 0
        });
        
        
      this.mrpOfproduct = new Wtf.form.NumberField({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.product.mrprate") + "'>" + WtfGlobal.getLocaleText("acc.product.mrprate") + "</span>", //"MRP Rate",
            name: "mrprate",
            anchor: '70%',
            hidden:(Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA),
            hideLabel: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA),
            allowNegative: false,
            decimalPrecision: Wtf.QUANTITY_DIGIT_AFTER_DECIMAL,
            value:0
        });
        
        //        this.reorderLevel.on('blur',this.setInputValue,this);	// Events fired to set and reset the NumberField value to '0' if no user input is detected
        this.reorderLevel.on('focus',this.resetInputValue,this);
        //        this.rQuantity.on('blur',this.setInputValue,this);
        this.rQuantity.on('focus',this.resetInputValue,this);
        this.leadtime.on('blur',this.setInputValue,this);
        this.leadtime.on('focus',this.resetInputValue,this);
        this.QAleadtime.on('blur',this.setInputValue,this);
        this.QAleadtime.on('focus',this.resetInputValue,this);
        //        this.cCountTolerance.on('blur',this.setInputValue,this);
        //        this.cCountTolerance.on('focus',this.resetInputValue,this);
        this.productWeight.on('blur',this.setInputValue,this);
        this.productWeight.on('focus',this.resetInputValue,this);
        this.productWeightPerStockUom.on('blur',this.setInputValue,this);
        this.productWeightPerStockUom.on('focus',this.resetInputValue,this);
        this.productWeightIncludingPakagingPerStockUom.on('blur',this.setInputValue,this);
        this.productWeightIncludingPakagingPerStockUom.on('focus',this.resetInputValue,this);
        
        this.productVolumePerStockUom.on('blur',this.setInputValue,this);
        this.productVolumePerStockUom.on('focus',this.resetInputValue,this);
        this.productVolumeIncludingPakagingPerStockUom.on('blur',this.setInputValue,this);
        this.productVolumeIncludingPakagingPerStockUom.on('focus',this.resetInputValue,this);
        
        this.quantity.on('blur',this.setInputValue,this);
        this.quantity.on('focus',this.resetInputValue,this);
        this.minOrderingQuantity.on('blur',this.setInputValue,this);
        this.minOrderingQuantity.on('focus',this.resetInputValue,this);
        this.maxOrderingQuantity.on('blur',this.setInputValue,this);
        this.maxOrderingQuantity.on('focus',this.resetInputValue,this);
        this.initialsalesprice.on('blur',this.setInputValue,this);
        this.initialsalesprice.on('focus',this.resetInputValue,this);
        this.mrpOfproduct.on('blur',this.setInputValue,this);
        this.mrpOfproduct.on('focus',this.resetInputValue,this);
        this.initialprice.on('blur',function(){if(this.producttypeval == Wtf.producttype.service){this.setInputValue(this.initialprice);}},this);
        this.initialprice.on('focus',function(){if(this.producttypeval == Wtf.producttype.service){this.resetInputValue(this.initialprice);}},this);
    },
       
        
     asofdatevalidation : function(object, e){
        
        var asofdate = this.asOfDate.getValue();
        var bookbeginningdate = Wtf.account.companyAccountPref.bbfrom;
        var action = function (btn) {
            if (btn == "yes") {
                Wtf.getCmp('asofdate' + this.id).setValue(Wtf.account.companyAccountPref.bbfrom.format(WtfGlobal.getOnlyDateFormat())); //as of date set to book begining date
                if (object.name == "initialquantity") {
                    this.checkBatchSerial(object);
                }
            }
            if (btn == "no") {
                Wtf.getCmp('initialquantity' + this.id).setValue("0"); //Initial quantity set to 0 
            }
        }
        if (bookbeginningdate < asofdate && this.quantity.getValue() > 0) {    //check as of date is equal or previous than book begining date if initial quantity >0
            // WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.importproduct.beforeOrEqualToBookBeginningDate")], 2);
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.alert"),
                msg: WtfGlobal.getLocaleText("acc.createproduct.BeforeOrEqualToBookBeginning"),
                width: 420,
                fn: action,
                scope: this,
                buttons: Wtf.MessageBox.YESNO,
                animEl: 'mb9',
                icon: Wtf.MessageBox.WARNING
            });

        }
        else if (object.name == "initialquantity") {
            this.checkBatchSerial(object);
        }
    },
    
    checkBatchSerial:function(obj){
        this.isLocationProduct=this.isLocationForProduct.getValue();
        this.isWarehouseProduct=this.isWarehouseForProduct.getValue();
        this.isRowProduct=this.isRowForProduct.getValue();
        this.isRackProduct=this.isRackForProduct.getValue();
        this.isBinProduct=this.isBinForProduct.getValue();
        this.isBatchForProd=this.isBatchForProduct.getValue();
        this.isSerialForProd=this.isSerialForProduct.getValue();
        this.isSKUForProd=this.isSKUForProduct.getValue();
        //in edit case if we changed the opening quantity then clear the old batchdetails 
        if(this.isEdit && !this.isClone && this.quantity.getValue()!=obj.value){
            this.batchDetails="";
        }
        if((this.isLocationProduct || this.isWarehouseProduct || this.isBatchForProd || this.isSerialForProd || this.isRowProduct || this.isRackProduct || this.isBinProduct) && this.quantity.getValue()>0)  {
            this.callSerialNoWindow(obj);
        }
        if(this.AssemblyGrid != undefined && this.AssemblyGrid && this.quantity.getValue()>0){
            this.AssemblyGrid.isInitialQuatiy=true;
        }
    },
    callSerialNoWindow:function(obj){
        var productQty=this.quantity.getValue();
        if(productQty=="NaN" || productQty=="")
            productQty=0;
        var productName=this.Pname.getValue();
        this.batchDetailswin=new Wtf.account.SerialNoWindow({
            renderTo: document.body,
            title:WtfGlobal.getLocaleText("acc.field.SelectWarehouseBatchSerialNumber"),
            productName:productName, 
            productid:((this.isEdit && !this.isClone) ? this.record.data.productid : ""),
            quantity:productQty,
            batchDetails:this.batchDetails,
            fromProduct:true,
            moduleid: this.moduleid,
            isLocationForProduct:this.isLocationProduct,
            isWarehouseForProduct:this.isWarehouseProduct,
            isRowForProduct:this.isRowProduct,
            isRackForProduct:this.isRackProduct,
            isBinForProduct:this.isBinProduct,
            isBatchForProduct:this.isBatchForProd,
            isSerialForProduct:this.isSerialForProd,
            isSKUForProduct:this.isSKUForProd,
            isEdit:this.isEdit,
            defaultLocation:(this.isEdit && !this.isClone)?this.record.data.location:this.locationEditor.getValue(),
            defaultWarehouse:(this.isEdit && !this.isClone)?this.record.data.warehouse:this.wareHouseEditor.getValue(),
            width:950,
            height:400,
            resizable : false,
            modal : true
        });
        this.batchDetailswin.on("beforeclose",function(){
         var isfromSubmit=this.batchDetailswin.isfromSubmit;
        if(isfromSubmit){  //as while clicking on the cancel icon it was adding the reocrd in batchdetail json 
            this.batchDetails=this.batchDetailswin.getBatchDetails();
        }  
        },this);
        this.batchDetailswin.show();
    },
    resetInputValue:function(field){		// Remove value '0' when user put focus on the NumberField
        if(field.getValue() == 0){
            field.setValue(""); 
        }
    },
    
    setInputValue:function(field){			// Set value '0' when user puts no input else keep the user input as it is 
        if(field.getValue() == ""){
            field.setValue(0);
        }
    },
    
    checkValue:function(field,newValue,oldValue){			// Set value '0' when user puts no input else keep the user input as it is 
        if(oldValue!=newValue){
            this.batchDetails="";
        }
    },
	
    createAssemblyGrid:function(){
        var gridtitle = WtfGlobal.getLocaleText("acc.product.gridBillofMaterials") + " " + WtfGlobal.getLocaleText("acc.field.AP.note");
        if (Wtf.account.companyAccountPref.activateMRPManagementFlag && this.isEdit && !this.isClone) {
            gridtitle = WtfGlobal.getLocaleText("acc.product.gridBillofMaterials") + " " + WtfGlobal.getLocaleText("acc.field.AP.note.edit");
        }
        this.AssemblyGrid = new Wtf.account.productAssemblyGrid({
            layout:"fit",
            bodyBorder:true,
//            hidden:true,
            border:false,
            isClone:this.isClone,
            bodyStyle:'padding:10px',
            height:350,
            isInitialQuatiy:false, 
            disabledClass: "newtripcmbss",
            excluseDateFilters:true,
            productForm:true,
            gridtitle:gridtitle,//"Bill Of Materials",
            productid:(this.record!=null?this.record.data['productid']:null),
            rendermode:"productform",
            productType:this.producttypeval,
            isUsedInTransaction:this.isUsedInTransaction,
            isEdit:this.isEdit
        });
        this.AssemblyGrid.on("updatedcost",function(grid){
            if( this.producttype.getValue() == Wtf.producttype.assembly || this.producttype.getValue() == Wtf.producttype.customerAssembly ){
                this.initialprice.setValue(grid.totalcost);
            }
        },this);
        
    },
    loadProductStore:function(){
        //        if(this.producttypeval==Wtf.producttype.noninvpart||this.producttypeval==Wtf.producttype.service){
        //           this.purchaseAccStore.load();
        //        }
        //        else{
            this.purchaseAccStore.load({params:{group:[6]}});
    //        }
    },
    changeLayoutWithType:function(c,rec){        
        if(rec!=undefined && this.producttypeval!=rec.data.id){
            this.producttypeval=rec.data.id;
            this.loadProductStore();
        }
        if(SATSCOMPANY_ID!=companyid){
            WtfGlobal.hideFormElement(this.noOfServiceQTY);
        }
        WtfGlobal.hideFormElement(this.autoAssembly);
        this.producttypeval = this.producttype.getValue();
        //Reset Form
        this.bomFieldset.hide();
        this.Currency.enable();
        WtfGlobal.showFormElement(this.uom);
        WtfGlobal.showFormElement(this.packingStockUom);
        WtfGlobal.showFormElement(this.rQuantity);
        WtfGlobal.showFormElement(this.reorderLevel);
        WtfGlobal.showFormElement(this.leadtime);
        WtfGlobal.showFormElement(this.QAleadtime);
        WtfGlobal.showFormElement(this.hsCode);
        WtfGlobal.showFormElement(this.warrantyperiod);  
        WtfGlobal.showFormElement(this.warrantyperiodSal);
        WtfGlobal.showFormElement(this.vendor);
        WtfGlobal.showFormElement(this.quantity);
        
        if(Wtf.dispalyUnitPriceAmountInPurchase){
            WtfGlobal.showFormElement(this.initialprice);
        } else{
            WtfGlobal.hideFormElement(this.initialprice);
        }
        if(Wtf.dispalyUnitPriceAmountInSales){
            WtfGlobal.showFormElement(this.initialsalesprice); 
        } else{
            WtfGlobal.hideFormElement(this.initialsalesprice);
        }
        
        WtfGlobal.showFormElement(this.multiuom);       
        WtfGlobal.showFormElement(this.blockLooseSell);
        WtfGlobal.showFormElement(this.schemaType);
        WtfGlobal.showFormElement(this.displayUom);
        //    this.cCountInterval.allowBlank = false;
        //    this.cCountTolerance.allowBlank = false;
        //    WtfGlobal.showFormElement(this.cCountInterval);
        //    WtfGlobal.showFormElement(this.cCountTolerance);
        WtfGlobal.showFormElement(this.isRecylable);
        WtfGlobal.showFormElement(this.isWastageApplicable);
        WtfGlobal.showFormElement(this.wastageAccount);
        WtfGlobal.showFormElement(this.productWeight);
        WtfGlobal.showFormElement(this.isBatchForProduct);
        if(Wtf.account.companyAccountPref.activateProductComposition){
            this.productCompositionFieldSet.show();
        }
        if(!this.isEdit|| (!this.isClone &&!this.isEdit)){
        this.isLocationForProduct.reset();
        this.isWarehouseForProduct.reset();
        this.isBatchForProduct.reset();
        this.isSerialForProduct.reset();
    }
        WtfGlobal.showFormElement(this.isSerialForProduct);
        WtfGlobal.showFormElement(this.isSKUForProduct);
        WtfGlobal.showFormElement(this.locationEditor);
        WtfGlobal.showFormElement(this.wareHouseEditor);
        WtfGlobal.showFormElement(this.isLocationForProduct);
        WtfGlobal.showFormElement(this.isRackForProduct);
        WtfGlobal.showFormElement(this.isRowForProduct);
        WtfGlobal.showFormElement(this.isWarehouseForProduct);
        WtfGlobal.showFormElement(this.qaenable);
        if (Wtf.account.companyAccountPref.activateMRPManagementFlag  || Wtf.account.companyAccountPref.inventoryValuationType == "1") {
            WtfGlobal.showFormElement(this.stockAdjustmentAcc);
            WtfGlobal.showFormElement(this.inventoryAcc);
            WtfGlobal.showFormElement(this.cogsAcc);
            this.stockAdjustmentAcc.allowBlank=false;
            this.cogsAcc.allowBlank=false;
            this.inventoryAcc.allowBlank=false;
        }
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.isExciseApplicable){
            if(!Wtf.isEmpty(Wtf.get( 'excTarriffDetails_id' ))){
                Wtf.get( 'excTarriffDetails_id' ).setStyle( 'display', 'block' );
            }     
        }
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.get( 'gstdetail_id'+this.id )!=null){
            Wtf.get( 'gstdetail_id'+this.id ).setStyle( 'display', 'none' );
        }
        this.InventoryTab.enable();
        if(this.producttypeval == Wtf.producttype.assembly || this.producttypeval == Wtf.producttype.customerAssembly){ //Inventory Assembly
          if(Wtf.productStore !=undefined && !(Wtf.account.companyAccountPref.productOptimizedFlag==Wtf.Products_on_type_ahead)){
              Wtf.productStore.baseParams.isAssemblyType=true;  //ERP-21517
            if(!this.isEdit) {
                Wtf.productStore.load(); 
            }   
        }            
            this.bomFieldset.show();
            WtfGlobal.hideFormElement(this.isRecylable);
            WtfGlobal.hideFormElement(this.vendor);
            WtfGlobal.showFormElement(this.autoAssembly);
            // In case of Assembly type product disable currency and set base currency
            this.Currency.disable();
            this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
            //WtfGlobal.showFormElement(this.locationEditor);
            // WtfGlobal.showFormElement(this.wareHouseEditor);
            this.productCompositionFieldSet.hide();
        }else if(this.producttypeval == Wtf.producttype.service){ //Service
//            this.uom.allowBlank = true;
//            this.packingStockUom.allowBlank = true;
            this.bomFieldset.hide();        
            this.OtherSettingForm.disable();
            WtfGlobal.hideFormElement(this.rQuantity);
            WtfGlobal.hideFormElement(this.reorderLevel);
            WtfGlobal.hideFormElement(this.leadtime);
            WtfGlobal.hideFormElement(this.QAleadtime);
            WtfGlobal.hideFormElement(this.warrantyperiod);
            WtfGlobal.hideFormElement(this.warrantyperiodSal);
            WtfGlobal.hideFormElement(this.vendor);
            WtfGlobal.hideFormElement(this.quantity);
            //            WtfGlobal.hideFormElement(this.initialprice);
            //        this.cCountInterval.allowBlank = true;
            //        this.cCountTolerance.allowBlank = true;
            //        WtfGlobal.hideFormElement(this.cCountInterval);
            //        WtfGlobal.hideFormElement(this.cCountTolerance);
            WtfGlobal.hideFormElement(this.productWeight);
            WtfGlobal.hideFormElement(this.multiuom);
            WtfGlobal.hideFormElement(this.blockLooseSell);
            WtfGlobal.hideFormElement(this.schemaType);
            WtfGlobal.hideFormElement(this.displayUom);
            WtfGlobal.hideFormElement(this.autoAssembly);
            WtfGlobal.hideFormElement(this.isRecylable);
            this.isWastageApplicable.reset();
            this.wastageAccount.reset();
            WtfGlobal.hideFormElement(this.isWastageApplicable);
            WtfGlobal.hideFormElement(this.wastageAccount);
            WtfGlobal.showFormElement(this.noOfServiceQTY);	
            WtfGlobal.hideFormElement(this.isBatchForProduct);
            WtfGlobal.hideFormElement(this.isSerialForProduct);
            WtfGlobal.hideFormElement(this.isSKUForProduct);
            this.wareHouseEditor.allowBlank = true;
            this.locationEditor.allowBlank = true;
            this.isLocationForProduct.setValue(false);
            this.isWarehouseForProduct.setValue(false);
            WtfGlobal.hideFormElement(this.locationEditor);
            WtfGlobal.hideFormElement(this.wareHouseEditor);
            this.productCompositionFieldSet.hide();
            WtfGlobal.hideFormElement(this.isLocationForProduct);
            WtfGlobal.hideFormElement(this.isRackForProduct);
            WtfGlobal.hideFormElement(this.isRowForProduct);
            WtfGlobal.hideFormElement(this.isWarehouseForProduct);
            WtfGlobal.hideFormElement(this.qaenable);
            this.qaenable.setValue(false);
            WtfGlobal.hideFormElement(this.isBinForProduct);
            //    ERP-29392
            
            this.stockAdjustmentAcc.allowBlank=true;
            this.cogsAcc.allowBlank=true;
            this.inventoryAcc.allowBlank=true;
            WtfGlobal.hideFormElement(this.stockAdjustmentAcc);
            WtfGlobal.hideFormElement(this.inventoryAcc);
            WtfGlobal.hideFormElement(this.cogsAcc);
            if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.get( 'excTarriffDetails_id' )!=null){
                Wtf.get( 'excTarriffDetails_id' ).setStyle( 'display', 'none' );
            }
            if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.get( 'gstdetail_id'+this.id )!=null){
                Wtf.get( 'gstdetail_id'+this.id ).setStyle( 'display', 'block' );
            }
        }else if(this.producttypeval != Wtf.producttype.service){
            this.uom.allowBlank = false;
            this.OtherSettingForm.enable();
        }
        if(this.producttypeval == Wtf.producttype.noninvpart) { // Non inventory part
//            WtfGlobal.hideFormElement(this.schemaType);            
//            WtfGlobal.hideFormElement(this.multiuom);            
//            WtfGlobal.hideFormElement(this.blockLooseSell);            
            WtfGlobal.hideFormElement(this.autoAssembly);            
            WtfGlobal.hideFormElement(this.isRecylable);            
            this.isWastageApplicable.reset();
            this.wastageAccount.reset();
            this.wareHouseEditor.allowBlank = true;
            WtfGlobal.hideFormElement(this.quantity);
            
            /* Hide CoGS account for non-inventory type product*/
            if (Wtf.account.companyAccountPref.activateMRPManagementFlag || Wtf.account.companyAccountPref.inventoryValuationType == "1"){
                this.cogsAcc.allowBlank = true; 
                WtfGlobal.hideFormElement(this.cogsAcc);
            }
            //SDP-16444 : For all Non-Inventory Part Type Product, we will hide Stock Adjustment and Inventory Account.
            this.stockAdjustmentAcc.allowBlank=true;
            this.inventoryAcc.allowBlank=true;
            WtfGlobal.hideFormElement(this.stockAdjustmentAcc);
            WtfGlobal.hideFormElement(this.inventoryAcc);
            
            this.locationEditor.allowBlank = true;
            this.isLocationForProduct.setValue(false);
            this.isWarehouseForProduct.setValue(false);
            WtfGlobal.hideFormElement(this.locationEditor);
            WtfGlobal.hideFormElement(this.wareHouseEditor);
            WtfGlobal.hideFormElement(this.isWastageApplicable);
            WtfGlobal.hideFormElement(this.wastageAccount);
            WtfGlobal.hideFormElement(this.isBatchForProduct);
            WtfGlobal.hideFormElement(this.isSerialForProduct);
            WtfGlobal.hideFormElement(this.isSKUForProduct);
            WtfGlobal.hideFormElement(this.isLocationForProduct);
            WtfGlobal.hideFormElement(this.isRackForProduct);
            WtfGlobal.hideFormElement(this.isRowForProduct);
            WtfGlobal.hideFormElement(this.isWarehouseForProduct);
            WtfGlobal.hideFormElement(this.qaenable);
            this.qaenable.setValue(false);
            WtfGlobal.hideFormElement(this.isBinForProduct);
            this.productCompositionFieldSet.hide();
        }
        if(this.producttypeval == Wtf.producttype.inventoryNonSale){
            this.salesReturnAcc.setValue(null);
            WtfGlobal.hideFormElement(this.salesReturnAcc);
            this.salesAcc.setValue(null);
            WtfGlobal.hideFormElement(this.salesAcc);
            this.salesReturnAcc.allowBlank = true;
            /* Hide CoGS account for non-inventory type product*/
            if (Wtf.account.companyAccountPref.activateMRPManagementFlag || Wtf.account.companyAccountPref.inventoryValuationType == "1") {
                this.cogsAcc.allowBlank = true;
                WtfGlobal.hideFormElement(this.cogsAcc);
            }
            this.salesAcc.allowBlank = true;
            WtfGlobal.hideFormElement(this.isRecylable);
            this.isWastageApplicable.reset();
            this.wastageAccount.reset();
            WtfGlobal.hideFormElement(this.isWastageApplicable);
            WtfGlobal.hideFormElement(this.wastageAccount);
           WtfGlobal.hideFormElement(this.initialsalesprice);
//            WtfGlobal.hideFormElement(this.isBatchForProduct);
            WtfGlobal.hideFormElement(this.isSerialForProduct);
            WtfGlobal.hideFormElement(this.isSKUForProduct);
            WtfGlobal.hideFormElement(this.autoAssembly);
            this.productCompositionFieldSet.hide();
        }
        else{
            WtfGlobal.showFormElement(this.salesReturnAcc);
            WtfGlobal.showFormElement(this.salesAcc);
            this.salesReturnAcc.allowBlank = false;
            this.salesAcc.allowBlank = false;
        }
//        if(Wtf.account.companyAccountPref.invAccIntegration){
//            WtfGlobal.showFormElement(this.shelfLocationCombo);
//        }else{
//            WtfGlobal.hideFormElement(this.shelfLocationCombo);
//        }
        //Reset Form Labels
        
//        if(!this.isFixedAsset){
//            WtfGlobal.updateFormLabel(this.PID,WtfGlobal.getLocaleText("acc.product.productID")+":");
//            WtfGlobal.updateFormLabel(this.Pname,WtfGlobal.getLocaleText("acc.product.productName")+":");
//        }
//        WtfGlobal.updateFormLabel(this.initialprice,WtfGlobal.getLocaleText("acc.product.initialPurchasePrice")+":");
        if(this.producttypeval == Wtf.producttype.assembly || this.producttypeval == Wtf.producttype.customerAssembly){ //Inventory Assembly
            var assemblyTotalPrice=this.AssemblyGrid.totalcost;
//            WtfGlobal.updateFormLabel(this.initialprice,WtfGlobal.getLocaleText("acc.product.assemblyProductCost"));
            this.initialprice.setValue(assemblyTotalPrice);
            this.initialprice.disable();
            /*
             * ERP-38964
             * Enable the other settings form for Inventory Assembly 
             */
             this.OtherSettingForm.enable();  
        }else if(this.producttypeval == Wtf.producttype.service){ //Service
//            WtfGlobal.updateFormLabel(this.PID,WtfGlobal.getLocaleText("acc.product.ServiceID")+":");
//            WtfGlobal.updateFormLabel(this.Pname,WtfGlobal.getLocaleText("acc.product.ServiceName")+":");
//            WtfGlobal.updateFormLabel(this.initialprice,WtfGlobal.getLocaleText("acc.product.servicePurchasePrice"));
            this.initialprice.enable();
        //            this.initialprice.setValue(0);
        }else{
            this.initialprice.enable();
//            if(!this.isEdit)
//                this.initialprice.setValue(null);
        }    
        
        if(this.isEdit && !this.isClone){ //This block for make enable and disable of sales price and purchase price in edit case. ERP-11396                        
            if(this.producttypeval == Wtf.producttype.assembly || this.producttypeval == Wtf.producttype.customerAssembly){ //edit case of assembly product
                this.initialprice.disable(); //It will be always disable either product is used or not 
                if(this.record.data.isUsedInTransaction){ 
                    this.initialsalesprice.disable();
                } else {
                    this.initialsalesprice.enable();
                }                
            } else{ //edit case of other product
                if(this.record.data.isUsedInTransaction){ //if product is used in any transaction then both field will be disable in edit case
                    this.initialprice.disable();
                    this.initialsalesprice.disable();
                } else { //if product not used in any transaction then both field will be enable in edit case
                    this.initialprice.enable();
                    this.initialsalesprice.enable();
                }
            } 
        }
        
        if(this.isFixedAsset){
            WtfGlobal.hideFormElement(this.quantity);
            //            WtfGlobal.hideFormElement(this.syncable);
            WtfGlobal.hideFormElement(this.multiuom);
            WtfGlobal.hideFormElement(this.blockLooseSell);
            WtfGlobal.hideFormElement(this.schemaType);
            WtfGlobal.hideFormElement(this.displayUom);
            WtfGlobal.hideFormElement(this.autoAssembly);
            WtfGlobal.hideFormElement(this.isRecylable);
            this.isWastageApplicable.reset();
            this.wastageAccount.reset();
            WtfGlobal.hideFormElement(this.isWastageApplicable);
            WtfGlobal.hideFormElement(this.wastageAccount);
            //            WtfGlobal.hideFormElement(this.subproduct);
            this.productCompositionFieldSet.hide();
        }
        this.doLayout();
    },
    showUom:function(){
        callUOM('uomReportWin');
        Wtf.getCmp('uomReportWin').on('update', function(){
            Wtf.uomStore.reload();
        }, this);
    },

    showProductType:function(){
        callProductType('productTypeWin');
        Wtf.getCmp('productTypeWin').on('update', function(){
            Wtf.productTypeStore.reload();
        }, this);
    },

    addPerson:function(isEdit,rec,winid,isCustomer){
        callBusinessContactWindow(isEdit, rec, winid, isCustomer);
        var tabid=this.isCustomer?'contactDetailCustomerTab':'contactDetailVendorTab';
        Wtf.getCmp(tabid).on('update', function(){
            Wtf.vendorAccStore.reload();
        }, this);
    },
    createForm:function(){
        var columnWidth=0.40;
        var fieldPurchase=this.purchaseAcc;
        var fieldSales=this.salesAcc;
        var fieldPurchaseReturn=this.purchaseReturnAcc;
        var fieldSalesReturn=this.salesReturnAcc;
        
        var labelWidth=150;
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.account.companyAccountPref.enablevatcst){
            fieldPurchase=this.purchasesAccounts;
            fieldSales=this.salesAccounts;
            fieldPurchaseReturn=this.purchasesAccountsReturn;
            fieldSalesReturn=this.salesAccountsReturn;
        }
        
        this.PurchaseForm=new Wtf.form.FormPanel({
            region: 'north',
            autoHeight: true,     
            disabledClass:"newtripcmbss",
            //        id:"purchaseForm"+this.id,
            border:false,
            items:[{
                layout:'form',
                disabledClass:"newtripcmbss",
                disabled: (!Wtf.isEmpty(this.allowAssemblyProductToEdit) && this.allowAssemblyProductToEdit) ? true : false,
                defaults:{
                    border:false
                },
                baseCls:'northFormFormat',
                labelWidth:labelWidth,
                items:[{
                    layout:'column',
                    defaults:{
                        border:false
                    },
                    items:[{
                        layout:'form',
                        columnWidth:columnWidth,
                        items:[ 
                        fieldPurchase,
                        this.purchasesSideTaxAccounts,
                        this.vendor,
                        this.ItemPurchaseHeight,
                        this.ItemPurchaseLength,
                        this.PurchaseMfg,
                        this.purchaseTax,
                        this.itcTypeCmbForProduct,
                        this.itcAccountCombo
                        ]
                    },
                    {
                        layout:'form',
                        columnWidth:columnWidth,
                        items:[ 
                        fieldPurchaseReturn,
                        this.purchasesAccountsReturn,
                        this.initialprice,
                        this.CatalogNo,
                        this.ItemPurchaseWidth,
                        this.ItemPurchaseVolume
                        ]
                    }
                    ]
//                },this.fixedAssetFieldSet,this.tagsFieldset,this.productTaxGridPurchase,this.productAdditionalTaxGridPurchase]
                //},this.fixedAssetFieldSet,this.tagsFieldset,this.vatPurchaseProductTerm,this.excisePurchaseProuctTerm,this.servicePurchaseProuctTerm,this.cstPurchaseProuctTerm,this.otherPurchaseProductTerm,this.productAdditionalTaxGridPurchase]
                },this.fixedAssetFieldSet,this.tagsFieldset]
            }]
        });
        
        
        this.SalesForm=new Wtf.form.FormPanel({
            region: 'north',
            autoHeight: true,     
            disabledClass:"newtripcmbss",
            id:"salesForm"+this.id,
            border:false,
            items:[{
                layout:'form',
                defaults:{
                    border:false
                },
                baseCls:'northFormFormat',
                disabledClass:"newtripcmbss",
                disabled: (!Wtf.isEmpty(this.allowAssemblyProductToEdit) && this.allowAssemblyProductToEdit) ? true : false,
                labelWidth:labelWidth,
                items:[{
                    layout:'column',
                    defaults:{
                        border:false
                    },
                    items:[{
                        layout:'form',
                        columnWidth:columnWidth,
                        items:[ 
                        fieldSales,
                        this.SalesSideTaxAccounts,
                        this.ItemSalesWidth,
                        this.ItemSalesVolume,
                        this.AlternateProducts,
                        this.salesTax
                        ]
                    },
                    {
                        layout:'form',
                        columnWidth:columnWidth,
                        items:[ 
                        fieldSalesReturn,
                        this.ItemSalesHeight,
                        this.ItemSalesLength,
                        this.initialsalesprice,
                        //this.mrpOfproduct
                        ]
                    }
            
                  ]
               // },this.fixedAssetFieldSet,this.tagsFieldset,this.vatSalesProductTerm,this.exciseSalesProuctTerm,this.serviceSalesProuctTerm,this.cstSalesProuctTerm,this.otherSalesProductTerm,this.productAddditionalTaxGrid]
                },this.fixedAssetFieldSet,this.tagsFieldset]
            }]
        });
        this.PropertiesForm=new Wtf.form.FormPanel({
            region: 'north',
            autoHeight: true,  
            disabledClass:"newtripcmbss",
            id:"propertiesForm"+this.id,
            border:false,
            items:[{
                disabledClass:"newtripcmbss",
                disabled: (!Wtf.isEmpty(this.allowAssemblyProductToEdit) && this.allowAssemblyProductToEdit) ? true : false,
                layout:'form',
                defaults:{
                    border:false
                },
                baseCls:'northFormFormat',
                labelWidth:100,
                items:[{
                    layout:'column',
                    defaults:{
                        border:false
                    },
                    items:[{
                        layout:'form',
                        columnWidth:0.40,
                        items:[ 
                        this.ItemHeight,
                        this.ItemWidth,
                        //this.ItemLength,
                        // this.ItemVolume,
                        this.ItemColor,
                        this.productWeight
                        ]
                    },
                    {
                        layout:'form',
                        columnWidth:0.40,
                        items:[ 
                        //  this.ItemHeight,
                        //  this.ItemWidth,
                        this.ItemLength,
                        this.ItemVolume,
                        this.warrantyperiod,
                        this.warrantyperiodSal
                        //this.ItemColor
                        ]
                    }
                    ]
                },this.fixedAssetFieldSet,this.tagsFieldset]
            }]
        });

  
        this.RemarksForm=new Wtf.form.FormPanel({
            region: 'north',
            autoHeight: true, 
            disabledClass:"newtripcmbss",
            id:"remarksForm"+this.id,
            fileUpload: true,
            // bodyStyle:"padding:10px",
            border:false,
            items:[{
                disabledClass:"newtripcmbss",
                disabled: (!Wtf.isEmpty(this.allowAssemblyProductToEdit) && this.allowAssemblyProductToEdit) ? true : false,
                layout:'form',
                defaults:{
                    border:false
                },
                baseCls:'northFormFormat',
                labelWidth:150,
                items:[{
                    layout:'column',
                    defaults:{
                        border:false
                    },
                    items:[{
                        layout:'form',
                        columnWidth:0.50,
                        items:[ 
                        this.AdditionalFreeText
                   
                        ]
                    }
                    //               ,
                    //                {
                    //                    layout:'form',
                    //                    columnWidth:0.50,
                    //                    items:[ 
                    //                    {
                    //                        border: false,
                    //                        id: "itempicture",
                    //                        xtype: "textfield",
                    //                        inputType: 'file',
                    //                        fieldLabel: "Picture",
                    //                        name: "itempicture"
                    //                    }
                    //                    ]
                    //                }
                    ]
                },this.fixedAssetFieldSet,this.tagsFieldset]
            } ]
        });
        this.PackagingUOMSettingForm = new Wtf.form.FieldSet({
           title: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.accPref.inventoryschemaSettingtooltip") + "'>" + WtfGlobal.getLocaleText("acc.accPref.inventorypackaging") + "</span>",
            autoHeight: true,
            autoWidth: true,
            layout: 'column',
            disabled: Wtf.account.companyAccountPref.UomSchemaType == Wtf.PackegingSchema ? false : true,
            disabledClass: "newtripcmbss",
            items: [{
                    layout: 'form',
                    columnWidth: 0.50,
                    items: [
                        this.CasingUoMCombo,
                        this.InnerUoMCombo,
                        this.packingStockUom,
//                        this.StockUoMCombo,
                        this.packaging,
                        this.Salesuom,
                        this.OrderingUoMCombo,
                    ]
                },
                {
                    layout: 'form',
                    columnWidth: 0.50,
                    items: [
                        //this.ItemCost,
                        this.CasingUoMValue,
                        this.InnerUoMValue,
                        this.StockUoMValue,
                        this.Purchaseuom,
                        //this.wareHouseEditor,
                        this.TransferUoMCombo,
                    ]
                }
            ]
        });
        this.OtherSettingForm = new Wtf.form.FieldSet({
            title: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.accPref.acc.field.OtherSettingtooltip") + "'>" + WtfGlobal.getLocaleText("acc.field.OtherSetting") + "</span>",
            autoHeight: true,
            autoWidth: true,
            layout: 'column',
            disabledClass: "newtripcmbss",
            items: [{
                    layout: 'form',
                    columnWidth: 0.50,
                    items: [
                        //                                this.uom,
                        this.WIPOffset,
                        this.InventoryOffset,
                        this.reorderLevel,
                        this.rQuantity,
                        this.leadtime,
                        this.QAleadtime,
                        this.hsCode,
                        this.cogsAcc,
                        this.productWeightPerStockUom,
                        this.productWeightIncludingPakagingPerStockUom,
                        this.productVolumePerStockUom,
                        this.productVolumeIncludingPakagingPerStockUom
                    ]
                },
                {
                    layout: 'form',
                    columnWidth: 0.50,
                    items: [
                        //this.ItemCost,
                        //                        this.CasingUoMValue,
                        //                        this.InnerUoMValue ,
                        //                        this.StockUoMValue,
                        //                        this.Purchaseuom,
                        //this.wareHouseEditor,
                        //                                this.TransferUoMCombo ,
                        //                                this.multiuom,
                        //                                this.blockLooseSell,
                        //                                this.schemaType,
                        this.wareHouseEditor,
                        this.locationEditor,
                        this.countable,
                        this.CycleCountFrequencyCombo,
                        this.stockAdjustmentAcc,
                        this.inventoryAcc,
                        {
                            xtype: 'radiogroup',
                            flex: 8,
                            vertical: true,
                            columns: 1,
                            labelWidth: 50,
                            id: 'valuation_method_radiogroup' + this.id,
                            fieldLabel: WtfGlobal.getLocaleText("acc.product.valuationmethod"),
                            items: [
                                this.ValuationMethod1,
                                this.ValuationMethod0,
                                this.ValuationMethod2
                            ]
                        }
                    ]
                }
            ]
        });

        this.InventoryDataForm = new Wtf.form.FormPanel({
            region: 'north',
            autoHeight: true,
            disabledClass:"newtripcmbss",
            id:"inventorydataForm"+this.id,
            border:false,          
            items:[{
                disabledClass:"newtripcmbss",
                disabled: (!Wtf.isEmpty(this.allowAssemblyProductToEdit) && this.allowAssemblyProductToEdit) ? true : false,
                layout:'form',             
                defaults:{
                    border:false
                    },
                baseCls:'northFormFormat',
                labelWidth:150,
                items:[{
                        xtype:'fieldset',
                        autoHeight:true,
                        disabledClass:"newtripcmbss",
                        disabled:Wtf.account.companyAccountPref.UomSchemaType==Wtf.PackegingSchema?true:false,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.inventoryschemaSettingtooltip")+"'>"+WtfGlobal.getLocaleText("acc.accPref.inventoryschemaSetting")+"</span>" ,
                        items:[{
                            layout:'column',
                            defaults:{
                                border:false
                                    },
                            items:[{
                                layout:'form',
                                columnWidth:0.50,
                                items:[ 

                                this.uom,
                                this.multiuom
                                                                
                                ]
                                        },
                                        {
                                layout:'form',
                                columnWidth:0.50,
                                items:[ 
                               
                                this.blockLooseSell,
                                this.schemaType,
                                this.displayUom
                            ]
                                        }
                                    ]
                                }]
                        }, this.PackagingUOMSettingForm,this.OtherSettingForm,this.fixedAssetFieldSet, this.tagsFieldset]
                }]
        });

        var centerItemsArr = [
            //                    this.warrantyperiod,
            //                    this.warrantyperiodSal,
            //                    this.leadtime,
            //                    this.QAleadtime,
            //                    this.cCountInterval,
            //                    this.cCountTolerance,
            //                    this.productWeight,
            this.subproduct,
            //this.salesAcc,
            //this.salesReturnAcc,
            this.isBatchForProduct,
            this.isSerialForProduct,
            this.isSKUForProduct,
            this.isRowForProduct,
            this.isRackForProduct,
            this.isBinForProduct,
            this.revenueRecognitionProcess,
            this.salesRevenueRecognitionAccount,
            this.assetControllingAcc,
            this.ItemKnittingStatus,
            this.ItemActiveStatus,
            this.itemReusability,
            this.reusabilityCount,
            this.substituteProductCombo,
            this.substituteQty,
            //                    this.wareHouseEditor,
            //                    this.locationEditor,
            this.licensetype,
            this.licensecode,
            this.inspectionTemplate,
            this.productBrandCombo,
            this.landingCostCategoryCombo
        ];
        if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA) {
            centerItemsArr.push(this.gstHistory);
        }
        this.ProductFormGroup=new Wtf.form.FormPanel({
            region: 'north',
            autoHeight: true,  
            autoScroll: true,
            id:"northForm"+this.id,
            disabledClass:"newtripcmbss",
            // bodyStyle:"padding:10px",
            border:false,
            items:[{
                disabledClass:"newtripcmbss",
                disabled: (!Wtf.isEmpty(this.allowAssemblyProductToEdit) && this.allowAssemblyProductToEdit) ? true : false,
                layout:'form',
                defaults:{border:false},
                baseCls:'northFormFormat',              
                labelWidth:160,
                items:[{
                    layout:'column',
                    defaults:{border:false},
                    items:[{
                        layout:'form',
                        columnWidth:0.34,
                items:[ {xtype:'hidden',name:'productid', value:(this.isClone)?"":(this.record==null?"":this.record.data.productid)},
                        this.producttype,
                        this.Pname,
                        this.ItemBarcode,
                        this.sequenceFormatCombobox,
                        this.PID,
                        this.description,
                        this.descriptionshow,
                        this.AdditionalDesc,
                        this.DescForeign,
                        this.asOfDate,
                        this.industryCodeCmb,
//                        this.abatementRate,
                        /*this.reorderLevel,
                    this.rQuantity*/
                        this.dependentType,this.isInterval,this.addShipLengthqty,this.interValCombo,this.noOfServiceQTY,this.extraQuantity,
                    this.isWarehouseForProduct,this.isLocationForProduct,this.barcodeFieldset]
                    },{
                        layout:'form',
                        columnWidth:0.34,
                        items:centerItemsArr
                    },{
                        layout:'form',
                        columnWidth:0.32,
                        items:[
                        this.ItemGroup,
//                        this.ItemPriceList,   //SDP-13707 : Kept it hidden
                        this.ShippingType,
                        //                    this.purchaseAcc,
                        //                    this.purchaseReturnAcc,
                        this.quantity,
                        this.minOrderingQuantity,
                        this.maxOrderingQuantity,
                        //this.initialprice,
                        //this.initialsalesprice,
                        this.Currency,
                        this.syncable,
                        // this.multiuom,
                        this.autoAssembly,
                        this.isRecylable,
                        this.rcmapplicable,
                        this.isWastageApplicable,
                        this.wastageAccount,
                        this.customerCategoryCombo,
                        this.qaenable,
                        this.Supplier,
                        this.CoilCraft,
                        this.InterPlant
//                        ,
                    ]          //, this.shelfLocationCombo
                    }]
                },this.fixedAssetFieldSet,this.productCompositionFieldSet,this.tagsFieldset]
            }]
        });

        //    this.isLocationForProduct.on("check",function(){
        //        if(this.isLocationForProduct.getValue()=="on"){
        //          //this.locationEditor.setDisabled(false);
        //          this.locationEditor.disabled=false;
        //        }else{
        //          //this.locationEditor.setDisabled(true); 
        //          this.locationEditor.disabled=true;
        //        }
        //    },this);
        //    
        //    this.isWarehouseForProduct.on("check",function(){
        //        if(this.isWarehouseForProduct.getValue()=="on"){
        //           // this.wareHouseEditor.setDisabled(false);
        //            this.wareHouseEditor.disabled=false;
        //        }else{
        //            this.wareHouseEditor.disabled=true;
        //        }
        //    },this);

        this.itemReusability.on("change",function(){
            if(this.itemReusability.getValue() == 0){
                this.reusabilityCount.setDisabled(false);
                if(this.record.data.reusabilitycount != undefined && this.record.data.reusabilitycount != ""){
                    this.reusabilityCount.setValue(this.record.data.reusabilitycount);
                }
            }else{
                this.reusabilityCount.setDisabled(true);
                this.reusabilityCount.setValue(0);
            } 
        },this);
    },

    setPackagingValue:function (){
        //alert("hiiiii");
//        if( Wtf.uomStore.find('uomname','-') == -1) {
//            var newRec=new Wtf.data.Record({
//                uomid:'',
//                uomname:"-"
//            });
//            Wtf.uomStore.add(newRec);
//        }
        var packagingData = "";
        this.OrderUoMStore.removeAll();
        this.transferuomStore.removeAll();
        this.PurchasingUoMStore.removeAll();
        this.SellingUoMStore.removeAll();
        
        this.orderUomStoreArr = [];
        this.transferuomStoreArr = [];
        this.purchasinguomStoreArr = [];
        this.sellinguomStoreArr = [];
        
        if(this.CasingUoMCombo.getRawValue() == "" || this.CasingUoMCombo.getRawValue() == "-"){
            this.CasingUoMValue.setValue(0);
        } else {
            this.newOrderRec = new this.OrderUoMRec({
                uomid:this.CasingUoMCombo.getValue(),
                uomname:this.CasingUoMCombo.getRawValue()
            });
            //            if(this.orderUomStoreArr.length == 0 ||(this.uom.getValue() != this.CasingUoMCombo.getValue() && this.InnerUoMCombo.getValue() != this.CasingUoMCombo.getValue())){
            this.orderUomStoreArr.push([this.CasingUoMCombo.getValue(),this.CasingUoMCombo.getRawValue()]);
            this.transferuomStoreArr.push([this.CasingUoMCombo.getValue(),this.CasingUoMCombo.getRawValue()]);
            this.purchasinguomStoreArr.push([this.CasingUoMCombo.getValue(),this.CasingUoMCombo.getRawValue()]);
            this.sellinguomStoreArr.push([this.CasingUoMCombo.getValue(),this.CasingUoMCombo.getRawValue()]);
            //            }
            //            this.OrderUoMStore.add(this.newOrderRec);
            ///this.CasingUoMValue.setValue(1);
            if(this.CasingUoMValue.getValue() == 0){
                this.CasingUoMValue.setValue(1);
            }
            packagingData = packagingData + this.CasingUoMValue.getValue()+" "+this.CasingUoMCombo.getRawValue();
        }

        if(this.InnerUoMCombo.getRawValue() == "" || this.InnerUoMCombo.getRawValue() == "-"){
            this.InnerUoMValue.setValue(0);
        } else {
            this.newOrderRec = new this.OrderUoMRec({
                uomid:this.InnerUoMCombo.getValue(),
                uomname:this.InnerUoMCombo.getRawValue()
            });
            if(this.orderUomStoreArr.length == 0 || this.orderUomStoreArr[0][0] != this.InnerUoMCombo.getValue()){
                this.orderUomStoreArr.push([this.InnerUoMCombo.getValue(),this.InnerUoMCombo.getRawValue()]);
                this.transferuomStoreArr.push([this.InnerUoMCombo.getValue(),this.InnerUoMCombo.getRawValue()]);
                this.purchasinguomStoreArr.push([this.InnerUoMCombo.getValue(),this.InnerUoMCombo.getRawValue()]);
                this.sellinguomStoreArr.push([this.InnerUoMCombo.getValue(),this.InnerUoMCombo.getRawValue()]);
            }
            //            this.OrderUoMStore.add(this.newOrderRec);
            if(this.InnerUoMValue.getValue() == 0){
                this.InnerUoMValue.setValue(1);
            }
            if(packagingData == ""){
                //                this.InnerUoMValue.setValue(1);
                packagingData = packagingData + this.InnerUoMValue.getValue()+" "+this.InnerUoMCombo.getRawValue();
            } else {
                packagingData = packagingData + " X " +this.InnerUoMValue.getValue()+" "+this.InnerUoMCombo.getRawValue();
            }
        }

        if(this.packingStockUom.getRawValue() == "" || this.packingStockUom.getRawValue() == "-"){
            this.StockUoMValue.setValue(0);
        } else {
            this.newOrderRec = new this.OrderUoMRec({
                uomid:this.uom.getValue(),
                uomname:this.uom.getRawValue()
            });
            if(this.orderUomStoreArr.length == 1){
                if(this.uom.getValue() != this.orderUomStoreArr[0][0]){
                    this.orderUomStoreArr.push([this.uom.getValue(),this.uom.getRawValue()]);
                    this.transferuomStoreArr.push([this.uom.getValue(),this.uom.getRawValue()]);
                    this.purchasinguomStoreArr.push([this.uom.getValue(),this.uom.getRawValue()]);
                    this.sellinguomStoreArr.push([this.uom.getValue(),this.uom.getRawValue()]);
                }
            } else if(this.orderUomStoreArr.length == 2){
                if(this.uom.getValue() != this.orderUomStoreArr[0][0] && this.uom.getValue() != this.orderUomStoreArr[1][0]){
                    this.orderUomStoreArr.push([this.uom.getValue(),this.uom.getRawValue()]);
                    this.transferuomStoreArr.push([this.uom.getValue(),this.uom.getRawValue()]);
                    this.purchasinguomStoreArr.push([this.uom.getValue(),this.uom.getRawValue()]);
                    this.sellinguomStoreArr.push([this.uom.getValue(),this.uom.getRawValue()]);
                }
            } else {
                this.orderUomStoreArr.push([this.uom.getValue(),this.uom.getRawValue()]);
                this.transferuomStoreArr.push([this.uom.getValue(),this.uom.getRawValue()]);
                this.purchasinguomStoreArr.push([this.uom.getValue(),this.uom.getRawValue()]);
                this.sellinguomStoreArr.push([this.uom.getValue(),this.uom.getRawValue()]);
            }
            //            this.OrderUoMStore.add(this.newOrderRec);
            if(this.StockUoMValue.getValue() == 0){
                this.StockUoMValue.setValue(1);
            }
            if(packagingData == ""){
                //                this.PrimaryUoMValue.setValue(1);
                packagingData = packagingData + this.StockUoMValue.getValue()+" "+this.uom.getRawValue();
            } else {
                packagingData = packagingData + " X " +this.StockUoMValue.getValue()+" "+this.uom.getRawValue();
            }
        }

        this.OrderUoMStore.loadData(this.orderUomStoreArr);
        this.transferuomStore.loadData(this.transferuomStoreArr);
        this.PurchasingUoMStore.loadData(this.purchasinguomStoreArr);
        this.SellingUoMStore.loadData(this.sellinguomStoreArr);
        this.packaging.setValue(packagingData);
        if(this.OrderUoMStore.find('uomname',this.OrderingUoMCombo.getRawValue()) == -1 && this.OrderingUoMCombo.getRawValue() != ''){
            this.OrderingUoMCombo.reset();
        }
        if(this.transferuomStore.find('uomname',this.TransferUoMCombo.getRawValue()) == -1 && this.TransferUoMCombo.getRawValue() != ''){
            this.TransferUoMCombo.reset();
        }
        if(this.PurchasingUoMStore.find('uomname',this.Purchaseuom.getRawValue()) == -1 && this.Purchaseuom.getRawValue() != ''){
            this.Purchaseuom.reset();
        }
        if(this.SellingUoMStore.find('uomname',this.Salesuom.getRawValue()) == -1 && this.Salesuom.getRawValue() != ''){
            this.Salesuom.reset();
        }
    },
    checkParent:function(){
        if(this.parentStore.getCount()==0 && this.messageFlag && Wtf.account.companyAccountPref.productOptimizedFlag !== Wtf.Products_on_type_ahead && Wtf.account.companyAccountPref.productOptimizedFlag !== Wtf.Products_on_Submit && Wtf.account.companyAccountPref.productOptimizedFlag != Wtf.Show_all_Products)
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.product.msg9")],1);
    },
    closeForm:function(){
        this.fireEvent('closed',this);
    },
    setStyleWidth:function(field,msg){//Padding for invalid field icon
    var leftpaddingWidth =field.container.dom.attributes.style.value;//Take style width of lable
    leftpaddingWidth = leftpaddingWidth.replace("padding-left:", "");//Remove "left padding" from width
    leftpaddingWidth = leftpaddingWidth.replace("px", "");//Remove "px" to get only width value 
    var paddingWidth = parseInt(leftpaddingWidth, 10);//Parse width to int
    var styleWidth = field.container.dom.children[0].style.getPropertyValue("Width");//take style width of combo
    styleWidth = styleWidth.replace("px", "");//Remove "px" to get only width value 
    var swidth = parseInt(styleWidth, 10);//Parse width to int
    field.errorIcon.applyStyles("left:"+(paddingWidth+swidth+8)+"px;");//Add both width and get left padding for invalid icon
},
getDetails:function(){
        var commentlist = getDocsAndCommentList('', Wtf.Acc_Product_Master_ModuleId,this.id,undefined,'Product',undefined,"email",'leadownerid',this.contactsPermission,0,this.recordid);
    },
    validMandatory:function(){
        //        var items=this.PurchaseForm.getForm().items;
        var items=this.newPanel.items.items;
        var isMandatoryFlag=false;
        var a= new Array();
        var b= new Array();
        b.push(WtfGlobal.getLocaleText("acc.product.Pleasefillthefollowingfields"))
        b.toString().replace(/[*,]/gi, '')
        var cnt=0;
        a.push("<div style = 'margin-left:50px'>" )
        
        for(var i =0 ;i<items.length;i++){
            var item =items[i].items.items[0].getForm().items;
            isMandatoryFlag=false;
            var red_tabs = this.newPanel.items.items[i]!=undefined?Wtf.getCmp(this.newPanel.items.items[i].id):null;
            for(var j =0 ;j<item.length;j++){
            if(item.itemAt(j).name=="initialprice"){
                if(!Wtf.account.companyAccountPref.allowZeroUntiPriceForProduct){           //Check if purchase price is allowed to set zero at company level
                    if(this.quantity.getValue()>0 && this.initialprice.getValue()==0){      //Check if quanity is not 0 and purchase price is also not 0
                        cnt++;
                        isMandatoryFlag=true;
                        item.itemAt(j).markInvalid();
                        a.push('<br />'+cnt+'.'+WtfGlobal.getLocaleText("acc.field.Initialpriceoftheproductcannotbezero"));
                    }
                }
            }else if(item.itemAt(j).allowBlank == false && (item.itemAt(j).getValue() === "" ||  item.itemAt(j).getValue() == undefined)  && item.itemAt(j).hidden == false ){
                    cnt++;
                    isMandatoryFlag=true;
                    item.itemAt(j).markInvalid();
                    a.push('<br />'+cnt+'.'+item.itemAt(j).fieldLabel);
                   
        }
      }        
        if(isMandatoryFlag && red_tabs!=null){
            var title=this.newPanel.items.items[i].title;
                title=title.replace(/<(?:.|\n)*?>/gm, '');//regex used to remove unwanted HTML code
                    red_tabs.setTitle('<font color="red">' + title + '</font>');
        }else if(red_tabs!=null){
            var title=this.newPanel.items.items[i].title;
                title=title.replace(/<(?:.|\n)*?>/gm, '');
                red_tabs.setTitle( title );
        }
    }
    var invalidCustomFieldsArr = this.tagsFieldset.getInvalidCustomFields();
    if (invalidCustomFieldsArr.length > 0) {
        for (var i = 0; i < invalidCustomFieldsArr.length; i++) {
            cnt++;
            var fieldLabel = invalidCustomFieldsArr[i].fieldLabel;
            fieldLabel = fieldLabel.replace(/<(?:.|\n)*?>/gm, '');//regex used to remove unwanted HTML code
            a.push('<br />' + cnt + '.' + fieldLabel);
        }
    }
    a.push("</div>")
        if(a.length>0){
        
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),b.toString().replace(/[*,]/gi, '').concat(a.toString().replace(/[*,]/gi, ''))],2)   
        }
      
    },
     confirmBeforeSave: function () { 
    if (Wtf.account.companyAccountPref.propagateToChildCompanies) {

        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"),
            msg: WtfGlobal.getLocaleText({
                key: "acc.savecustomer.propagate.confirmmessage", 
                params: ["Product"]
                }),
            buttons: Wtf.MessageBox.YESNO,
            icon: Wtf.MessageBox.QUESTION,
            width: 300,
            scope: {
                scopeObject: this
            },
            fn: function (btn) {
                if (btn == "yes") {
                    this.scopeObject.ispropagatetochildcompanyflag = true;
                }
                this.scopeObject.saveForm();
            }
        }, this);

    } else {
       
            var format = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]+/;
           
            var isHSNvalid = true;
            if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
                /*
                 * Check for length of HSN code should not be greater than 8 (For India-GST)
                 */
                isHSNvalid = this.validationforHSNfield();
            }
            if (format.test(this.Pname.getValue()) || !isHSNvalid) {

                var Msg = "";
                if (format.test(this.Pname.getValue()) && !isHSNvalid) {
                    Msg = "Product name having some special characters" + " and " + WtfGlobal.getLocaleText("acc.product.hsnValidationMsg");
                }
                else if (format.test(this.Pname.getValue())) {
                    Msg = "Product name having some special characters, do you want to save ?";
                }
                else if (!isHSNvalid) {
                    Msg = WtfGlobal.getLocaleText("acc.product.hsnValidationMsg");
                }
                Wtf.MessageBox.confirm("Confirm", Msg, function (btn) {
                    if (btn == 'yes') {
                        this.saveForm();
                    }

                }, this);
        }else{
            this.saveForm();
        }
    }
},
    showAlertForPurchaseAccount:function(){
        var success = true;
        var accRec = WtfGlobal.searchRecord(this.purchaseAccStore, this.purchaseAcc.getValue(), 'accid');
        if (accRec != undefined && accRec != null && accRec.data != undefined && accRec.data.nature != undefined && accRec.data.nature !== 2) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.product.nonexpenseaccount.tagged.purchaseaccount") , function(btn) {
                if (btn == "yes") {
                    this.WarnMessage = false;
                    this.saveForm();
                } else {
                    this.enableSaveButtons();
                    success=false;
                    return;
                }
            }, this);
            success = false;
            return;
        } else if (this.purchaseAccountRec != undefined && this.purchaseAccountRec != undefined && this.purchaseAccountRec != null && this.purchaseAccountRec.data != undefined && this.purchaseAccountRec.data.nature != undefined && this.purchaseAccountRec.data.nature !== 2) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.product.nonexpenseaccount.tagged.purchaseaccount"), function (btn) {
                if (btn == "yes") {
                    this.WarnMessage = false;
                    this.saveForm();
                } else {
                    this.enableSaveButtons();
                    success = false;
                    return;
                }
            }, this);
            success = false;
            return;
        }
        return success;
    },
    saveForm:function(){
        this.count=0;
        if(((this.producttypeval == Wtf.producttype.assembly || this.producttypeval == Wtf.producttype.customerAssembly)) && (this.autoAssembly.getValue())){
            this.AssemblyGrid.gridStore.each(function(r){
                if(r.data.isSerialForProduct || r.data.isBatchForProduct){  //ERP-28794
                    this.count++;
                }
            },this);
            if(this.count>0){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("Sub-Product selected have Batch or Serial Number.Please uncheck Auto Build Assembly on Sale.")], 2);
                return; 
            }
        }
        if(this.isClone){
            this.isEdit = false;
        } 
        if (Wtf.isBookClosed && this.quantity.getValue() > 0) {
            var showIsBookClosedAlert = false;
            if (this.isEdit && this.record.data.initialquantity != this.quantity.getValue()) {
                showIsBookClosedAlert = true;
            } else if (!this.isEdit) {
                showIsBookClosedAlert = true;
            }
            if (showIsBookClosedAlert) {
                 //  ERP-40285  Show alert if book(s) are closed and using is trying to update initial quantity.
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.product.initialquantity.cannotbeupdatedoradded.if.fyclosed")], 2);
                return;
            }
        }
        if(!Wtf.account.companyAccountPref.ishtmlproddesc){
            if (Wtf.account.companyAccountPref.proddiscripritchtextboxflag != Wtf.ProductDescInHtmlEditor) {
                this.description.setValue(this.descriptionshow.getValue());
            }  
        }  
        this.isLocationProduct=(this.isLocationForProduct.getValue()=="on"?true:this.isLocationForProduct.getValue());
        this.isWarehouseProduct=(this.isWarehouseForProduct.getValue()=="on"?true:this.isWarehouseForProduct.getValue());
        this.isRowProduct=(this.isRowForProduct.getValue()=="on"?true:this.isRowForProduct.getValue());
        this.isRackProduct=(this.isRackForProduct.getValue()=="on"?true:this.isRackForProduct.getValue());
        this.isBinProduct=(this.isBinForProduct.getValue()=="on"?true:this.isBinForProduct.getValue());
        this.isBatchProduct=(this.isBatchForProduct.getValue()=="on"?true:this.isBatchForProduct.getValue());
        this.isSerialProduct=(this.isSerialForProduct.getValue()=="on"?true:this.isSerialForProduct.getValue());
        this.isSKUProduct=(this.isSKUForProduct.getValue()=="on"?true:this.isSKUForProduct.getValue());
        var quantity=this.quantity.getValue();
        
        if((this.CasingUoMCombo.getValue()==this.InnerUoMCombo.getValue()&&this.CasingUoMCombo.getValue()!="")||(this.CasingUoMCombo.getValue()==this.uom.getValue()&&this.CasingUoMCombo.getValue()!="")||
            (this.InnerUoMCombo.getValue()==this.uom.getValue()&&this.InnerUoMCombo.getValue()!="")){
            this.CasingUoMCombo.setValue("");
            this.InnerUoMCombo.setValue("");
            this.uom.setValue("");
            this.packingStockUom.setValue("");
            this.displayUom.setValue("");
            this.packaging.setValue("");
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),"Casing Uom,Inner Uom and Stock Uom cannot be same"],2); 
            return;
        }
        
        if (this.producttypeval != Wtf.producttype.service &&this.producttypeval!=Wtf.producttype.service) { // serial no for only inventory type of product
            if ( this.isSerialProduct) {
                var v = quantity;
                v = String(v);
                var ps = v.split('.');
                var sub = ps[1];
                if (sub!=undefined && sub.length > 0) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.exceptionMsgForDecimalQty")], 2);
                    //                        this.enableSaveButtons();
                    return;
                }
            }
        }
        if((this.isBatchProduct || this.isSerialProduct || this.isLocationProduct || this.isWarehouseProduct || this.isRowProduct || this.isRackProduct || this.isBinProduct )  && this.quantity.getValue()>0 && !(this.isFixedAsset) && !((this.producttypeval == Wtf.producttype.noninvpart) && this.isClone)){ 
//        if((this.isBatchProduct || this.isSerialProduct || this.isLocationProduct || this.isWarehouseProduct || this.isRowProduct || this.isRackProduct || this.isBinProduct )  && this.quantity.getValue()>0 && !(this.isFixedAsset)){ 
            var batchDetail = this.batchDetails;
            if(batchDetail == undefined || batchDetail == "[]" || batchDetail==""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.bsdetail")],2);   //Batch and serial no details are not valid.
                return;
            }
                    }

    if(this.quantity.getValue()>0 
        && (this.isBatchProduct || this.isSerialProduct || this.isLocationProduct || this.isWarehouseProduct 
        || this.isRowProduct || this.isRackProduct || this.isBinProduct ) && !(this.producttypeval == Wtf.producttype.noninvpart)){
        var jsonBatchDetails= eval(this.batchDetails);
        var batchQty=0;
        for(var batchCnt=0;batchCnt<jsonBatchDetails.length;batchCnt++){
            if(jsonBatchDetails[batchCnt].quantity>0){
          
                if(jsonBatchDetails[batchCnt].quantity>0){
                    if(this.isSerialForProduct.getValue()=="on"){
                        batchQty=batchQty+ parseInt(jsonBatchDetails[batchCnt].quantity);
                    }else{
                        batchQty=batchQty+ parseFloat(jsonBatchDetails[batchCnt].quantity);
                    }
                }
            }
        }
        if((batchQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) != (this.quantity.getValue()).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),(WtfGlobal.getLocaleText("acc.invoice.bsdetail"))],2); 
            return;
        }
    }
        
        this.parentStore.clearFilter(true);
        this.producttypeval = this.producttype.getValue();
        var asemblyjson = "";
        var bomdetailjson = "";
        if ((this.producttypeval == Wtf.producttype.assembly || this.producttypeval == Wtf.producttype.customerAssembly) && Wtf.account.companyAccountPref.activateMRPManagementFlag) {
            var isDefaultBOMPresent = this.isDefaultBOMpresntForAssemblyProduct();
            bomdetailjson = this.getBOMAssemblyDetailsJSON();
            if (this.isEdit && !this.isClone && this.createdBOMGrid.getStore().getCount() > 0) {
                var record = this.createdBOMGrid.getStore().getAt(0);
                if (record.data['productid'] !== this.record.data.productid) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.select.root.product")], 2);   //Batch and serial no details are not valid.
                    return;
                } else if (!isDefaultBOMPresent && (this.isDefaultBOM != undefined && !this.isDefaultBOM.getValue())) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.select.defaultbom")], 2);   //Batch and serial no details are not valid.
                    return;
                }
        } 
        else if (!isDefaultBOMPresent && (this.isDefaultBOM != undefined && !this.isDefaultBOM.getValue()) && CompanyPreferenceChecks.withoutBOMCheck()) {     //In case of MRP enabled system, If without BOM is enabled then System will not check for BOM Products
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.select.defaultbom")], 2);   //Batch and serial no details are not valid.
            return;
        }
   }

        if(this.producttypeval==Wtf.producttype.assembly || this.producttypeval==Wtf.producttype.customerAssembly){//Assembly Item
            this.initialprice.setValue(this.AssemblyGrid.totalcost);
            asemblyjson = this.AssemblyGrid.getAssemblyJson();
    if(((asemblyjson.trim()=="" && !Wtf.account.companyAccountPref.activateMRPManagementFlag) || (Wtf.account.companyAccountPref.activateMRPManagementFlag && bomdetailjson.trim()=="")) && (!CompanyPreferenceChecks.withoutBOMCheck())){
                WtfComMsgBox(40,2);
                return;
            } 
for(var i=0;i<this.AssemblyGrid.gridStore.getCount()-1;i++){// excluding last row
                var quantity=this.AssemblyGrid.gridStore.getAt(i).data['quantity'];
                var productid=this.AssemblyGrid.gridStore.getAt(i).data['productid'];
                if ((quantity === "" || quantity == undefined || quantity == 0) && (productid != undefined && productid != "")) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantityforProduct")+" "+this.AssemblyGrid.gridStore.getAt(i).data['productname']+" "+WtfGlobal.getLocaleText("acc.field.shouldbegreaterthanZero")], 2);
                    this.enableSaveButtons();
                    return;
                }
            }
        }
        
//        if (Wtf.account.companyAccountPref.activateMRPManagementFlag || Wtf.account.companyAccountPref.inventoryValuationType == "1") {
            if (this.WarnMessage) { /* show warning if non-expense account is selected */
                if (!this.showAlertForPurchaseAccount()) {
                    return;
                }
            }
//        }
        
        var qualitycontroldetailjson = "";
        if(Wtf.account.companyAccountPref.activateMRPManagementFlag){
            qualitycontroldetailjson = this.getQualityControlDetailsJSON();
        }
        
        var productcompositionjson=""; //Product Composition
        productcompositionjson = this.getProductCompositionDetails();
        
        if(this.isFixedAsset){ 
            //        this.cCountInterval.allowBlank = true;
            //
            //        this.cCountTolerance.allowBlank = true;
            
            this.productWeight.allowBlank=true;
            
            //this.productWeight.allowBlank=true;
            
            this.rQuantity.allowBlank = true;

            this.reorderLevel.allowBlank = true;
            
            this.leadtime.allowBlank = true;
            this.QAleadtime.allowBlank = true;
            
        //        this.cCountInterval.setValue(0);
        }
        
        var isValidCustomFields = this.tagsFieldset.checkMandatoryCustomFieldDimension();
        var isValid =false;
    
        if(this.ProductFormGroup.getForm().isValid() && this.PurchaseForm.getForm().isValid() && this.SalesForm.getForm().isValid() &&
            this.PropertiesForm.getForm().isValid() && this.RemarksForm.getForm().isValid() && this.InventoryDataForm.getForm().isValid())
            {
            isValid=true;
        }
        
        
        
        //    if(this.producttypeval!=Wtf.producttype.service){ // not Service
        //        if(this.cCountInterval.getValue()==="" && !this.isFixedAsset) {
        //            this.cCountInterval.markInvalid(WtfGlobal.getLocaleText("acc.product.msg1"));
        //            isValid = false;
        //        }else if(this.cCountInterval.getValue()==0 && !this.isFixedAsset) {
        //            this.cCountInterval.markInvalid(WtfGlobal.getLocaleText("acc.product.msg2"));
        //            isValid = false;
        //        }
        //    }
     if(!this.isEdit && this.producttypeval == Wtf.producttype.assembly && CompanyPreferenceChecks.withoutBOMCheck()){   //SDP-6182
        if(!Wtf.account.companyAccountPref.allowZeroUntiPriceForProduct){//when true then bypass this messages (price should be greater than 0)
            if(this.quantity.getValue()>0 && this.initialprice.getValue()==0){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"),WtfGlobal.getLocaleText("acc.field.Initialpriceoftheproductcannotbezero")],2);
                return;
            }
        }
    } else if(this.isEdit && this.producttypeval != Wtf.producttype.service){
        if(!Wtf.account.companyAccountPref.allowZeroUntiPriceForProduct){//when true then bypass this messages (price should be greater than 0)
            if(this.quantity.getValue()>0 && this.initialprice.getValue()==0){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"),WtfGlobal.getLocaleText("acc.field.Initialpriceoftheproductcannotbezero")],2);
                return;
            }
        }
    } else if(!this.isEdit &&this.producttypeval != Wtf.producttype.service){
        /*
         *If non service type product 
         *initial qty is greater than 0 and initial pucrahse price is 0 then it showing prompt 
         */
        if(!Wtf.account.companyAccountPref.allowZeroUntiPriceForProduct){//when true then bypass this messages (price should be greater than 0)
            if(this.quantity.getValue()>0 && this.initialprice.getValue()==0){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"),WtfGlobal.getLocaleText("acc.field.Initialpriceoftheproductcannotbezero")],2);
                return;
            }
        }
    }
        if(!this.isEdit && this.producttypeval != Wtf.producttype.service){
//            if(this.initialprice.getValue()==="") {
//                this.initialprice.markInvalid(WtfGlobal.getLocaleText("acc.product.msg1"));
//                isValid = false;
//            }else if(this.initialprice.getValue()==0){ // not Service
//                this.initialprice.markInvalid(WtfGlobal.getLocaleText("acc.product.msg3"));
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.product.msg3")],2);   
//                isValid = false;
//            }
//            if(!Wtf.account.companyAccountPref.allowZeroUntiPriceForProduct){//when true then bypass this messages (price should be greater than 0)
//                if(this.quantity.getValue()>0 && this.initialprice.getValue()==0){
//                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"),WtfGlobal.getLocaleText("acc.field.Initialpriceoftheproductcannotbezero")],2);
//                    return;
//                }
//            }
            
        }
        if(this.uom.getValue()=='' && !this.isFixedAsset){
            this.uom.markInvalid(WtfGlobal.getLocaleText("acc.product.msg4"));
            isValid = false;
        }
        
        if(isValid && isValidCustomFields){
            if(!this.isEdit){
                var FIND = this.Pname.getValue().trim();
                this.Pname.setValue(FIND);
                FIND =FIND.replace(/\s+/g, '');            
                var FINDPID = this.PID.getValue().trim();
                this.PID.setValue(FINDPID);
                FINDPID = FINDPID.replace(/\s+/g, '').toLowerCase();            
            
                var index=this.parentStore.findBy( function(rec){
                    var parentname=rec.data['parentname'].trim();
                    parentname=parentname.replace(/\s+/g, '');
                    if(parentname==FIND&&!this.isEdit){
                        return true;
                    }else{
                        return false
                    }
                },this);
                if(index>=0){
                //                WtfComMsgBox(36,2);
                //                return;
                }
                    
                if(this.PID.getValue()!=""){ //BUG Fixed #16316
                    index=this.parentStore.findBy( function(rec){
                        if(rec.data['pid'] != undefined) {
                            var pid = rec.data['pid'].trim().toLowerCase();
                            pid =pid.replace(/\s+/g, '');
                            if(pid==FINDPID&&!this.isEdit){
                                return true;
                            }else{
                                return false
                            }
                        } else {
                            return false
                        }
                    },this)
                    if(index>=0){
                        WtfComMsgBox(41,2);
                        return;
                    }
                }
            }
            //        if(this.producttypeval!=Wtf.producttype.service){ // not Service
            //            if(this.cCountInterval.getValue()==="") {
            //                this.cCountInterval.markInvalid(WtfGlobal.getLocaleText("acc.product.msg1"));
            //                isValid = false;
            //            }else if(this.cCountInterval.getValue()==0) {
            //                this.cCountInterval.markInvalid(WtfGlobal.getLocaleText("acc.product.msg2"));
            //                isValid = false;
            //            }
            //        }
            if(this.isClone){
                var notAccessAccountsList="";
                var hasAccessFlag=false;
                if(!checkForAccountActivate(Wtf.salesAccStore,this.salesAcc.getValue(),"accid")){
                    hasAccessFlag=true;
                    notAccessAccountsList = notAccessAccountsList + WtfGlobal.getLocaleText("acc.product.salesAccount")+ ", ";
                }
                if(!checkForAccountActivate(Wtf.salesAccStore,this.salesReturnAcc.getValue(),"accid")){
                    hasAccessFlag=true;
                    notAccessAccountsList = notAccessAccountsList + WtfGlobal.getLocaleText("acc.product.salesReturnAccount")+ ", ";
                }
                if(!checkForAccountActivate(Wtf.salesAccStore,this.salesRevenueRecognitionAccount.getValue(),"accid")){
                    hasAccessFlag=true;
                    notAccessAccountsList = notAccessAccountsList + WtfGlobal.getLocaleText("acc.field.salesRevenueRecognitionAccount")+ ", ";
                }
                if(!checkForAccountActivate(this.purchaseAccStore,this.purchaseAcc.getValue(),"accid")){
                    hasAccessFlag=true;
                    notAccessAccountsList = notAccessAccountsList + WtfGlobal.getLocaleText("acc.product.purchaseAccount")+ ", ";
                }
                if(!checkForAccountActivate(this.purchaseAccStore,this.purchaseReturnAcc.getValue(),"accid")){
                    hasAccessFlag=true;
                    notAccessAccountsList = notAccessAccountsList + WtfGlobal.getLocaleText("acc.product.purchaseReturnAccount")+ ", ";
                }
                if(notAccessAccountsList!=""){
                    notAccessAccountsList = notAccessAccountsList.substring(0, notAccessAccountsList.length-2);
                }
                if(hasAccessFlag){
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                        msg: WtfGlobal.getLocaleText("acc.field.Pleaseselectactivatedaccount")+notAccessAccountsList+".",
                        width:370,
                        buttons: Wtf.MessageBox.OK,
                        icon: Wtf.MessageBox.WARNING,
                        scope: this
                    });
                    return;
                }
            }
            var custFieldArr=this.tagsFieldset.createFieldValuesArray();
            var dataProTermsExcisePurchase="";
            var dataProTermsServicePurchase="";
            var dataProTermsCSTPurchase="";
            var dataProTermsVATPurchase="";
            var dataProTermsOtherPurchase="";
            var dataProTermsExciseSales="";
            var dataProTermsServiceSales="";
            var dataProTermsCSTSales="";
            var dataProTermsVATSales="";
            var dataProTermsOtherSales="";
//            var dataProTermsSales="";
            var dataProTermsAdditionalPurchase="";
            var dataProTermsAdditionalSales="";

            if(this.isEdit){
                if(this.producttype.getValue() == Wtf.producttype.assembly || this.producttype.getValue() == Wtf.producttype.customerAssembly){
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.rem.4"),(WtfGlobal.getLocaleText("acc.assembly.product.msg1")),function(btn){
                        if(btn=="yes"){
                        	this.reBuild = true;}
                        else{return;}
         	
                        var rec=this.ProductFormGroup.getForm().getValues();
                        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                           //Purchase Terms
                            rec.dataProTermsExcisePurchase= dataProTermsExcisePurchase;//Product Term mapping data for Purchase save
                            rec.dataProTermsServicePurchase= dataProTermsServicePurchase;//Product Term mapping data for Purchase save
                            rec.dataProTermsCSTPurchase= dataProTermsCSTPurchase;//Product Term mapping data for Purchase save
                            rec.dataProTermsVATPurchase= dataProTermsVATPurchase;//Product Term mapping data for Purchase save
                            rec.dataProTermsOtherPurchase= dataProTermsOtherPurchase;//Product Term mapping data for Purchase save
                           // Sales Terms
                            rec.dataProTermsExciseSales=dataProTermsExciseSales;//Product Term mapping data for Sales save
                            rec.dataProTermsServiceSales=dataProTermsServiceSales;//Product Term mapping data for Sales save
                            rec.dataProTermsCSTSales=dataProTermsCSTSales;//Product Term mapping data for Sales save
                            rec.dataProTermsVATSales=dataProTermsVATSales;//Product Term mapping data for Sales save
                            rec.dataProTermsOtherSales=dataProTermsOtherSales;//Product Term mapping data for Sales save
                            
                            rec.productTermsAdditionalDataPurchase=dataProTermsAdditionalPurchase;//Product Term mapping data for Purchase save
                            rec.productTermsAdditionalDataSales=dataProTermsAdditionalSales;//Product Term mapping data for Sales save
                            
                            rec.dateMap=new Date().clearTime(true);//to be resolved
                            rec.countryid=Wtf.account.companyAccountPref.countryid
                        }
                        if (custFieldArr.length > 0)
                            rec.customfield = JSON.stringify(custFieldArr);
                        rec.asOfDate=WtfGlobal.convertToGenericDate(this.asOfDate.getValue());
                        rec.industryCodeId=this.industryCodeCmb.getValue();
                        rec.producttype=this.producttype.getValue();
                        rec.initialprice=this.initialprice.getValue();
                        rec.pid=this.PID.getValue();
                        if(this.producttype.getValue() == Wtf.producttype.assembly || this.producttype.getValue() == Wtf.producttype.customerAssembly){
                            rec.reBuild = this.reBuild;
                        }
                        rec.purchasetaxId=this.purchaseTax.getValue();
                        rec.salestaxId=this.salesTax.getValue();
                        rec.productname=this.Pname.getValue();
                        rec.batchDetails=this.batchDetails;
                        rec.mode=21;
                        rec.parentname=this.parentname.getRawValue();
                        rec.currencyid=this.Currency.getValue();
                        rec.supplier=this.Supplier.getValue();
                        rec.coilcraft=this.CoilCraft.getValue();
                        rec.interplant=this.InterPlant.getValue();
                        rec.syncable=(this.syncable.getValue()=="on"?true:this.syncable.getValue());
                        rec.recyclable=(this.isRecylable.getValue()=="on"?true:this.isRecylable.getValue());
                        rec.multiuom=(this.multiuom.getValue()=="on"?true:this.multiuom.getValue());
                        rec.blockLooseSell=(this.blockLooseSell.getValue()=="on"?true:this.blockLooseSell.getValue());
                        rec.schemaType=this.schemaType.getValue();
                        rec.uomschemaType=this.schemaType.getValue();
                        if(CompanyPreferenceChecks.displayUOMCheck()!= false){
                                rec.displayUoM = this.displayUom.getValue();
                        }
                        rec.rcmapplicable = (this.rcmapplicable.getValue() == "on" ? true : this.rcmapplicable.getValue());
                        rec.mrprate= this.mrpOfproduct.getValue();
                        //rec.mrpofproduct=this.mrpOfproduct.getValue();
                        rec.autoAssembly=(this.autoAssembly.getValue()=="on"?true:this.autoAssembly.getValue());
                        rec.qaenable=(this.qaenable.getValue()=="on"?true:this.qaenable.getValue());
                        rec.intervalField=(this.isInterval.getValue()=="on"?true:this.isInterval.getValue());
                        rec.addshiplentheithqty=(this.addShipLengthqty.getValue()=="on"?true:this.addShipLengthqty.getValue());
                        rec.quantity=this.quantity.getValue();
                        rec.isLocationForProduct=(this.isLocationForProduct.getValue()=="on"?true:this.isLocationForProduct.getValue());
//                        rec.abatementRate=this.abatementRate.getValue();
//                        rec.exciseMethodSubTypeCombo=this.exciseMethodSubTypeCombo.getValue();
                        rec.isWarehouseForProduct=(this.isWarehouseForProduct.getValue()=="on"?true:this.isWarehouseForProduct.getValue());
                        rec.isRowForProduct=(this.isRowForProduct.getValue()=="on"?true:this.isRowForProduct.getValue());
                        rec.isRackForProduct=(this.isRackForProduct.getValue()=="on"?true:this.isRackForProduct.getValue());
                        rec.isBinForProduct=(this.isBinForProduct.getValue()=="on"?true:this.isBinForProduct.getValue());
                        rec.isBatchForProduct=(this.isBatchForProduct.getValue()=="on"?true:this.isBatchForProduct.getValue());
                        rec.isSerialForProduct=(this.isSerialForProduct.getValue()=="on"?true:this.isSerialForProduct.getValue());
                        rec.isSKUForProduct=(this.isSKUForProduct.getValue()=="on"?true:this.isSKUForProduct.getValue());
                        rec.countable=(this.countable.getValue()=="on"?true:this.countable.getValue());
                        rec.quantity=this.quantity.getValue();
                        rec.editQuantity = this.editQuantity;
                        rec.assembly=asemblyjson;
                        rec.bomdetailjson=bomdetailjson;
                        rec.qualitycontroldetailjson=qualitycontroldetailjson;
                        rec.productcompositionjson=productcompositionjson;
                        rec.applydate=WtfGlobal.convertToGenericDate(Wtf.serverDate.clearTime(true));
                        var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
                        rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):true;
                        rec.sequenceformat=this.sequenceFormatCombobox.getValue();
                        if(Wtf.getCmp('bserialId'+this.id).getValue()){
                            rec.barcodeField=Wtf.BarcodeGenerator_SerialId;
                        }else if(Wtf.getCmp('bprodId'+this.id).getValue()){
                            rec.barcodeField=Wtf.BarcodeGenerator_ProductId;
                        }else if(Wtf.getCmp('brcodeId'+this.id).getValue()){
                            rec.barcodeField=Wtf.BarcodeGenerator_Barcode;
                        }else if(Wtf.getCmp('bSkuId'+this.id)!=undefined && Wtf.getCmp('bSkuId'+this.id).getValue()){
                            rec.barcodeField=Wtf.BarcodeGenerator_SKUField;
                        }else if(Wtf.getCmp('bbatchId'+this.id).getValue()){
                            rec.barcodeField=Wtf.BarcodeGenerator_BatchID;
                        }else{
                            rec.barcodeField=Wtf.BarcodeGenerator_NULL;
                        }
                        /**********************************************************************
                     *
                     *                NEW RECORDS ADDED HERE 
                     *
                     *********************************************************************/
                        rec.isActiveItem= (this.ItemActiveStatus.getValue()=="on"?true:this.ItemActiveStatus.getValue());
                        rec.isKnittingItem=(this.ItemKnittingStatus.getValue()=="on"?true:this.ItemKnittingStatus.getValue());
                    
                        rec.isreusable=(this.itemReusability.getValue());
                        rec.reusabilitycount=(this.reusabilityCount.getValue());
                        
                        rec.substituteProductId = this.substituteProductCombo.getValue();
                        rec.substituteQty = this.substituteQty.getValue();
                    
                        rec.licensetype=(this.licensetype.getValue());
                        rec.licensecode=(this.licensecode.getValue());
                        rec.inspectionTemplateId=(this.inspectionTemplate.getValue());
                        rec.isWastageApplicable = this.isWastageApplicable.getValue();
                        rec.wastageAccount = (this.isWastageApplicable.getValue()) ? this.wastageAccount.getValue() : "";
                        rec.productBrandId = this.productBrandCombo.getValue();
                        rec.landingCostCategoryId = this.landingCostCategoryCombo.getValue();
                    
                        // Purchase Tab fields
                        rec.catalogNo=this.CatalogNo.getValue();
                        rec.vendor=this.vendor.getValue();
                    rec.purchaseuomid=this.Purchaseuom.getValue()!=""?this.Purchaseuom.getValue():this.uom.getValue();
                        rec.itempurchaseheight=this.ItemPurchaseHeight.getValue();
                        rec.itempurchasewidth=this.ItemPurchaseWidth.getValue();
                        rec.itempurchaselength=this.ItemPurchaseLength.getValue();
                        rec.itempurchasevolume=this.ItemPurchaseVolume.getValue();
                        rec.purchasemfg=this.PurchaseMfg.getValue();
                    
                        rec.purchaseaccountid=this.purchaseAcc.getValue();
                        rec.purchaseretaccountid=this.purchaseReturnAcc.getValue();
                        rec.inputVAT=this.inputVAT.getValue();
                        rec.cstVATattwo=this.cstVATattwo.getValue();
                        rec.cstVAT=this.cstVAT.getValue();
                        rec.inputVATSales=this.inputVATSales.getValue();
                        rec.cstVATattwoSales=this.cstVATattwoSales.getValue();
                        rec.cstVATSales=this.cstVATSales.getValue();
                    
                        // Sales Tab fields
                    
                        rec.salesaccountid=this.salesAcc.getValue();
                        rec.salesretaccountid=this.salesReturnAcc.getValue();
                        if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isitcapplicable) {
                            rec.itcAccountId=this.itcAccountCombo.getValue();
                            rec.itctype=this.itcTypeCmbForProduct.getValue();
                        }

                    rec.salesuomid=this.Salesuom.getValue()!=""?this.Salesuom.getValue():this.uom.getValue();
                        rec.itemsalesheight=this.ItemSalesHeight.getValue();
                        rec.itemsaleswidth=this.ItemSalesWidth.getValue();
                        rec.itemsaleslength=this.ItemSalesLength.getValue();
                        rec.itemsalesvolume=this.ItemSalesVolume.getValue();
                        rec.alternateproductid=this.AlternateProducts.getValue();
                    
                        //  Properties Tab fields
                    
                    rec.salesuomid=this.Salesuom.getValue()!=""?this.Salesuom.getValue():this.uom.getValue();
                        rec.itemheight=this.ItemHeight.getValue();
                        rec.itemwidth=this.ItemWidth.getValue();
                        rec.itemlength=this.ItemLength.getValue();
                        rec.itemvolume=this.ItemVolume.getValue();
                        rec.itemcolor=this.ItemColor.getValue();
                        rec.productweight=this.productWeight.getValue();
                        rec.productweightperstockuom=this.productWeightPerStockUom.getValue();
                        rec.productweightincludingpakagingperstockuom=this.productWeightIncludingPakagingPerStockUom.getValue();
                        rec.productvolumeperstockuom=this.productVolumePerStockUom.getValue();
                        rec.productvolumeincludingpakagingperstockuom=this.productVolumeIncludingPakagingPerStockUom.getValue();
                        rec.warrantyperiod=this.warrantyperiod.getValue();
                        rec.warrantyperiodsal=this.warrantyperiodSal.getValue();
                        //  Remarks Tab fields
                 
                        rec.additionalfreetext=this.AdditionalFreeText.getValue();
                    
                        //  Inventory Data Tab fields
                    
                        if(this.ValuationMethod0.getValue()==true){
                            rec.valuationmethod=0;
                        }
                        if(this.ValuationMethod1.getValue()==true){
                            rec.valuationmethod=1;
                        }
                        if(this.ValuationMethod2.getValue()==true){
                            rec.valuationmethod=2;
                        }
                    
                        rec.warehouse=this.wareHouseEditor.getValue();
                        rec.uomid=this.uom.getValue();
                        rec.casinguomid=this.CasingUoMCombo.getValue();
                        rec.casinguomvalue=this.CasingUoMValue.getValue();
                        rec.inneruomid=this.InnerUoMCombo.getValue();
                        rec.inneruomvalue=this.InnerUoMValue.getValue();
                        rec.stockuomid=this.uom.getValue();
                        rec.stockuomvalue=this.StockUoMValue.getValue();
                    rec.orderinguomid=this.OrderingUoMCombo.getValue()!=""?this.OrderingUoMCombo.getValue():this.uom.getValue();
                    rec.transferuomid=this.TransferUoMCombo.getValue()!=""?this.TransferUoMCombo.getValue():this.uom.getValue();
                        rec.packaging=this.packaging.getValue();
                        //rec.itemcost=this.ItemCost.getValue();
                        rec.WIPoffset=this.WIPOffset.getValue();
                        rec.Inventoryoffset=this.InventoryOffset.getValue();
                        rec.CCFrequency=this.CycleCountFrequencyCombo.getValue();
                        rec.location=this.locationEditor.getValue();
                        rec.leadtime=this.leadtime.getValue();
                        rec.QAleadtime=this.QAleadtime.getValue();
                        rec.hsCode=this.hsCode.getValue();
                        rec.reorderlevel=this.reorderLevel.getValue();
                        rec.reorderquantity=this.rQuantity.getValue();
                        
                        rec.customercategory=this.customerCategoryCombo.getValue();
                        rec.name = this.Pname.getValue();
                        rec.ispropagatetochildcompanyflag = this.ispropagatetochildcompanyflag;
                        rec.isrecyclable=(this.isRecylable.getValue()=="on"?true:this.isRecylable.getValue());
                        rec.vendorid=this.vendor.getValue();
                        rec.uomschemaType=this.schemaType.getValue();
                        if(CompanyPreferenceChecks.displayUOMCheck() != false){
                            rec.displayUoM = this.displayUom.getValue();
                        }
                        rec.name = this.Pname.getValue();
                        rec.ispropagatetochildcompanyflag = this.ispropagatetochildcompanyflag;
                        rec.isrecyclable=(this.isRecylable.getValue()=="on"?true:this.isRecylable.getValue());
                        rec.vendorid=this.vendor.getValue();
                        rec.mrprate= this.mrpOfproduct.getValue();
                        //rec.mrpofproduct=this.mrpOfproduct.getValue();
                        if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA) {
                            if (this.gstapplieddate == undefined) {
                                this.gstapplieddate = Wtf.account.companyAccountPref.firstfyfrom;
                            }
                            rec.isgstdetailsupdated = this.isgstdetailsupdated;
                            rec.gstapplieddate = WtfGlobal.convertToGenericDate(this.gstapplieddate);
                        }
                        if (this.stockAdjustmentAcc) {
                            rec.stockadjustmentaccountid = this.stockAdjustmentAcc.getValue();
                        }
                        if (this.inventoryAcc) {
                            rec.inventoryaccountid = this.inventoryAcc.getValue();
                        }
                        if (this.cogsAcc) {
                            rec.cogsaccountid = this.cogsAcc.getValue();
                        }
                        if (!Wtf.isEmpty(this.allowAssemblyProductToEdit) && this.allowAssemblyProductToEdit){
                            rec.allowAssemblyProductToEdit = this.allowAssemblyProductToEdit;
                            rec.productid = this.record.data.productid;
                        }
                        if(this.initialsalesprice.getValue() != "" && this.initialprice.getValue() > this.initialsalesprice.getValue()){
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.rem.4"),(WtfGlobal.getLocaleText("acc.product.msg6")),function(btn){
                                if(btn=="yes"){
                                    Wtf.Ajax.requestEx({
                                        url: "ACCProductCMN/saveProduct.do",
                                        params: rec
                                    },this,this.genSuccessResponse,this.genFailureResponse);
                                }
                            else{return;}
                            },this);    
                        }else{                    
                            Wtf.Ajax.requestEx({
                                url: "ACCProductCMN/saveProduct.do",//edit case call
                                params: rec
                            },this,this.genSuccessResponse,this.genFailureResponse);
                        }
                    },this);
                }else{
            	
                    var rec=this.ProductFormGroup.getForm().getValues();
                    rec.purchasetaxId=this.purchaseTax.getValue();
                    rec.salestaxId=this.salesTax.getValue();
                    rec.asOfDate=WtfGlobal.convertToGenericDate(this.asOfDate.getValue());
                    rec.producttype=this.producttype.getValue();
                    rec.initialprice=this.initialprice.getValue();
                    rec.pid=this.PID.getValue();
                    if (custFieldArr.length > 0)
                        rec.customfield = JSON.stringify(custFieldArr);	
                    rec.productname=this.Pname.getValue();
                    rec.rcmapplicable = (this.rcmapplicable.getValue() == "on" ? true : this.rcmapplicable.getValue());
                    rec.mode=21;
                    rec.batchDetails=this.batchDetails;
                    rec.parentname=this.parentname.getRawValue();
                    rec.currencyid=this.Currency.getValue();
                    rec.supplier=this.Supplier.getValue();
                    rec.coilcraft=this.CoilCraft.getValue();
                    rec.interplant=this.InterPlant.getValue();
                    rec.syncable=(this.syncable.getValue()=="on"?true:this.syncable.getValue());
                    rec.recyclable=(this.isRecylable.getValue()=="on"?true:this.isRecylable.getValue());
                    rec.isWastageApplicable = this.isWastageApplicable.getValue();
                    rec.wastageAccount = (this.isWastageApplicable.getValue()) ? this.wastageAccount.getValue() : "";
                    rec.multiuom=(this.multiuom.getValue()=="on"?true:this.multiuom.getValue());
                    rec.blockLooseSell=(this.blockLooseSell.getValue()=="on"?true:this.blockLooseSell.getValue());
                    rec.countable=(this.countable.getValue()=="on"?true:this.countable.getValue());
                    rec.schemaType=this.schemaType.getValue();
                    rec.uomschemaType=this.schemaType.getValue();
                    if(CompanyPreferenceChecks.displayUOMCheck() != false){
                        rec.displayUoM = this.displayUom.getValue();
                    }
                    rec.mrprate=this.mrpOfproduct.getValue();
                    //rec.mrpofproduct=this.mrpOfproduct.getValue();
                    rec.isLocationForProduct=(this.isLocationForProduct.getValue()=="on"?true:this.isLocationForProduct.getValue());
                    rec.isWarehouseForProduct=(this.isWarehouseForProduct.getValue()=="on"?true:this.isWarehouseForProduct.getValue());
                    rec.isRowForProduct=(this.isRowForProduct.getValue()=="on"?true:this.isRowForProduct.getValue());
                    rec.isRackForProduct=(this.isRackForProduct.getValue()=="on"?true:this.isRackForProduct.getValue());
                    rec.isBinForProduct=(this.isBinForProduct.getValue()=="on"?true:this.isBinForProduct.getValue());
                    rec.isBatchForProduct=(this.isBatchForProduct.getValue()=="on"?true:this.isBatchForProduct.getValue());
                    rec.isSerialForProduct=(this.isSerialForProduct.getValue()=="on"?true:this.isSerialForProduct.getValue());
                    rec.isSKUForProduct=(this.isSKUForProduct.getValue()=="on"?true:this.isSKUForProduct.getValue());
                    rec.qaenable=(this.qaenable.getValue()=="on"?true:this.qaenable.getValue());
                    rec.quantity=this.quantity.getValue();
                    rec.editQuantity = this.editQuantity;
                    rec.assembly=asemblyjson;
                    rec.bomdetailjson=bomdetailjson;
                    rec.qualitycontroldetailjson=qualitycontroldetailjson;
                    rec.productcompositionjson=productcompositionjson;
                    rec.intervalField=(this.isInterval.getValue()=="on"?true:this.isInterval.getValue());
                    rec.addshiplentheithqty=(this.addShipLengthqty.getValue()=="on"?true:this.addShipLengthqty.getValue());
                    rec.applydate=WtfGlobal.convertToGenericDate(Wtf.serverDate);
                    var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
                    rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):true;
                    rec.sequenceformat=this.sequenceFormatCombobox.getValue();
                    if(Wtf.getCmp('bserialId'+this.id).getValue()){
                        rec.barcodeField=Wtf.BarcodeGenerator_SerialId;
                    }else if(Wtf.getCmp('bprodId'+this.id).getValue()){
                        rec.barcodeField=Wtf.BarcodeGenerator_ProductId;
                    }else if(Wtf.getCmp('brcodeId'+this.id).getValue()){
                        rec.barcodeField=Wtf.BarcodeGenerator_Barcode;
                    }else if(Wtf.getCmp('bSkuId'+this.id)!=undefined && Wtf.getCmp('bSkuId'+this.id).getValue()){
                        rec.barcodeField=Wtf.BarcodeGenerator_SKUField;
                    }else if(Wtf.getCmp('bbatchId'+this.id).getValue()){
                        rec.barcodeField=Wtf.BarcodeGenerator_BatchID;
                    }else{
                        rec.barcodeField=Wtf.BarcodeGenerator_NULL;
                    } 
                      
                    if(this.isFixedAsset){
                        rec.isFixedAsset=this.isFixedAsset;
                        rec.isDepreciable=true;// this field is not implemented yet
                        rec.depreciationMethod=this.depreciationMethodCombo.getValue();
                        rec.depreciationRate=this.depreciationRate.getValue();
                        rec.depreciationCostLimit=this.depreciationCostLimit.getValue();
                        rec.depreciationGLAccount=this.PNLTypeAccount.getValue();
                        rec.depreciationProvisionGLAccount=this.balanceSheetTypeAccount.getValue();
                        rec.sellAssetGLAccount=this.saleAssetAccount.getValue();
                        rec.assetControllingAccountId=this.assetControllingAcc.getValue();
                        rec.producttype=this.producttype.getValue();
                    }
                
                
                    /**********************************************************************
                     *
                     *                NEW RECORDS ADDED HERE 
                     *
                     *********************************************************************/
                    rec.isActiveItem= (this.ItemActiveStatus.getValue()=="on"?true:this.ItemActiveStatus.getValue());
                    rec.isKnittingItem=(this.ItemKnittingStatus.getValue()=="on"?true:this.ItemKnittingStatus.getValue());
                
                    rec.isreusable=(this.itemReusability.getValue());
                    rec.reusabilitycount=(this.reusabilityCount.getValue());
                    
                    rec.substituteProductId = this.substituteProductCombo.getValue();
                    rec.substituteQty = this.substituteQty.getValue();
                
                    rec.licensetype=(this.licensetype.getValue());
                    rec.licensecode=(this.licensecode.getValue());
                    rec.inspectionTemplateId=(this.inspectionTemplate.getValue());
                    rec.productBrandId = this.productBrandCombo.getValue();
                    rec.landingCostCategoryId = this.landingCostCategoryCombo.getValue();
                    // Purchase Tab fields
                    rec.catalogNo=this.CatalogNo.getValue();
                    rec.vendor=this.vendor.getValue();
                    rec.purchaseuomid=this.Purchaseuom.getValue()!=""?this.Purchaseuom.getValue():this.uom.getValue();
                    rec.itempurchaseheight=this.ItemPurchaseHeight.getValue();
                    rec.itempurchasewidth=this.ItemPurchaseWidth.getValue();
                    rec.itempurchaselength=this.ItemPurchaseLength.getValue();
                    rec.itempurchasevolume=this.ItemPurchaseVolume.getValue();
                    rec.purchasemfg=this.PurchaseMfg.getValue();
                
                    rec.purchaseaccountid=this.purchaseAcc.getValue();
                    rec.purchaseretaccountid=this.purchaseReturnAcc.getValue();
                    rec.inputVAT=this.inputVAT.getValue();
                    rec.cstVATattwo=this.cstVATattwo.getValue();
                    rec.cstVAT=this.cstVAT.getValue();
                    rec.inputVATSales=this.inputVATSales.getValue();
                    rec.cstVATattwoSales=this.cstVATattwoSales.getValue();
                    rec.cstVATSales=this.cstVATSales.getValue();

                    // Sales Tab fields
                
                    rec.salesaccountid=this.salesAcc.getValue();
                    rec.salesretaccountid=this.salesReturnAcc.getValue();
                    if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isitcapplicable) {
                        rec.itcAccountId=this.itcAccountCombo.getValue();
                        rec.itctype=this.itcTypeCmbForProduct.getValue();
                    }

                    rec.salesuomid=this.Salesuom.getValue()!=""?this.Salesuom.getValue():this.uom.getValue();
                    rec.itemsalesheight=this.ItemSalesHeight.getValue();
                    rec.itemsaleswidth=this.ItemSalesWidth.getValue();
                    rec.itemsaleslength=this.ItemSalesLength.getValue();
                    rec.itemsalesvolume=this.ItemSalesVolume.getValue();
               
                    rec.alternateproductid=this.AlternateProducts.getValue();
                    
                    //  Properties Tab fields
                    
                    rec.salesuomid=this.Salesuom.getValue()!=""?this.Salesuom.getValue():this.uom.getValue();
                    rec.itemheight=this.ItemHeight.getValue();
                    rec.itemwidth=this.ItemWidth.getValue();
                    rec.itemlength=this.ItemLength.getValue();
                    rec.itemvolume=this.ItemVolume.getValue();
                    rec.itemcolor=this.ItemColor.getValue();
                    rec.productweight=this.productWeight.getValue();
                    rec.productweightperstockuom=this.productWeightPerStockUom.getValue();
                    rec.productweightincludingpakagingperstockuom=this.productWeightIncludingPakagingPerStockUom.getValue();
                    rec.productvolumeperstockuom=this.productVolumePerStockUom.getValue();
                    rec.productvolumeincludingpakagingperstockuom=this.productVolumeIncludingPakagingPerStockUom.getValue();
                    rec.warrantyperiod=this.warrantyperiod.getValue();
                    rec.warrantyperiodsal=this.warrantyperiodSal.getValue();
                   
                    //  Remarks Tab fields
                 
                    rec.additionalfreetext=this.AdditionalFreeText.getValue();
                    
                    //  Inventory Data Tab fields
                    
                    if(this.ValuationMethod0.getValue()==true){
                        rec.valuationmethod=0;
                    }
                    if(this.ValuationMethod1.getValue()==true){
                        rec.valuationmethod=1;
                    }
                    if(this.ValuationMethod2.getValue()==true){
                        rec.valuationmethod=2;
                    }
                    
                    rec.warehouse=this.wareHouseEditor.getValue();
                    rec.uomid=this.uom.getValue();
                    rec.casinguomid=this.CasingUoMCombo.getValue();
                    rec.casinguomvalue=this.CasingUoMValue.getValue();
                    rec.inneruomid=this.InnerUoMCombo.getValue();
                    rec.inneruomvalue=this.InnerUoMValue.getValue();
                    rec.stockuomid=this.uom.getValue();
                    rec.stockuomvalue=this.StockUoMValue.getValue();
                    rec.orderinguomid=this.OrderingUoMCombo.getValue()!=""?this.OrderingUoMCombo.getValue():this.uom.getValue();
                    rec.transferuomid=this.TransferUoMCombo.getValue()!=""?this.TransferUoMCombo.getValue():this.uom.getValue();
                    rec.packaging=this.packaging.getValue();
                    //rec.itemcost=this.ItemCost.getValue();
                    rec.WIPoffset=this.WIPOffset.getValue();
                    rec.Inventoryoffset=this.InventoryOffset.getValue();
                    rec.CCFrequency=this.CycleCountFrequencyCombo.getValue();
                    rec.location=this.locationEditor.getValue();
                    rec.leadtime=this.leadtime.getValue();
                    rec.QAleadtime=this.QAleadtime.getValue();
                    rec.hsCode=this.hsCode.getValue();
                    rec.reorderlevel=this.reorderLevel.getValue();
                    rec.reorderquantity=this.rQuantity.getValue();
                    
                    rec.customercategory=this.customerCategoryCombo.getValue();
                    rec.name = this.Pname.getValue();
                    rec.ispropagatetochildcompanyflag = this.ispropagatetochildcompanyflag;
                    rec.isrecyclable=(this.isRecylable.getValue()=="on"?true:this.isRecylable.getValue());
                    rec.vendorid=this.vendor.getValue();
                    rec.uomschemaType=this.schemaType.getValue(); 
                    if(CompanyPreferenceChecks.displayUOMCheck()){
                        rec.displayUoM = this.displayUom.getValue();
                    }
                    rec.mrprate=this.mrpOfproduct.getValue();
                   // rec.mrpofproduct=this.mrpOfproduct.getValue();
                    if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                        if (this.gstapplieddate == undefined) {
                            this.gstapplieddate = this.asOfDate.getValue();
                        }
                        rec.isgstdetailsupdated = this.isgstdetailsupdated;
                        rec.gstapplieddate = WtfGlobal.convertToGenericDate(this.gstapplieddate);
                    }
                    if (this.stockAdjustmentAcc) {
                        rec.stockadjustmentaccountid = this.stockAdjustmentAcc.getValue();
                    }
                    if (this.inventoryAcc) {
                        rec.inventoryaccountid = this.inventoryAcc.getValue();
                    }
                    if (this.cogsAcc) {
                        rec.cogsaccountid = this.cogsAcc.getValue();
                    }
                    
                    if (this.producttype.getValue() != Wtf.producttype.service) {
                        var value = this.uom.getValue();
                        if(value == "" || value == undefined){
                            this.validMandatory();
                        }else{
                                Wtf.Ajax.requestEx({
                                url: "ACCProductCMN/saveProduct.do",
                                params: rec
                            },this,this.genSuccessResponse,this.genFailureResponse);
                        }
                    }else{
                        Wtf.Ajax.requestEx({
                            url: "ACCProductCMN/saveProduct.do",
                            params: rec
                        },this,this.genSuccessResponse,this.genFailureResponse);
                    }
                }
            }else{                    
                //                if(this.initialprice.getValue()==0 && this.producttypeval != Wtf.producttype.service){ // not Service
                //                    WtfComMsgBox(["Alert"," Initial purchase price of the product cannot be zero."],2);
                //                    return;
                //                }
                //                if(this.uom.getValue()=='' && this.producttypeval!=Wtf.producttype.service){ // not service
                //                    WtfComMsgBox(["Alert"," Please select UoM"],2);
                //                    return;
                //                }
            
                if(this.isFixedAsset){// Removed Confirnation MSG.
                    WtfComMsgBox(27,3,true);
                    var rec=this.ProductFormGroup.getForm().getValues();
                    
                    if (custFieldArr.length > 0)
                        rec.customfield = JSON.stringify(custFieldArr);
                    rec.mode=21;
                        
                    rec.pid=this.PID.getValue();
                    rec.asOfDate=WtfGlobal.convertToGenericDate(this.asOfDate.getValue());
                    rec.batchDetails=this.batchDetails;
                        
                    if(this.isFixedAsset){
                        rec.isFixedAsset=this.isFixedAsset;
                        rec.isDepreciable=true;// this field is not implemented yet
                        rec.depreciationMethod=this.depreciationMethodCombo.getValue();
                        rec.depreciationRate=this.depreciationRate.getValue();
                        rec.depreciationCostLimit=this.depreciationCostLimit.getValue();
                        rec.depreciationGLAccount=this.PNLTypeAccount.getValue();
                        rec.depreciationProvisionGLAccount=this.balanceSheetTypeAccount.getValue();
                        rec.sellAssetGLAccount=this.saleAssetAccount.getValue();
                        rec.assetControllingAccountId=this.assetControllingAcc.getValue();
                        rec.producttype=this.producttype.getValue();
                    }
                    rec.parentname=this.parentname.getRawValue();
                    //      rec.creationdate=this.CreationDate.getValue();
                    rec.quantity=this.quantity.getValue();
                    rec.isLocationForProduct=(this.isLocationForProduct.getValue()=="on"?true:this.isLocationForProduct.getValue());
                    rec.isWarehouseForProduct=(this.isWarehouseForProduct.getValue()=="on"?true:this.isWarehouseForProduct.getValue());
                    rec.isRowForProduct=(this.isRowForProduct.getValue()=="on"?true:this.isRowForProduct.getValue());
                    rec.isRackForProduct=(this.isRackForProduct.getValue()=="on"?true:this.isRackForProduct.getValue());
                    rec.isBinForProduct=(this.isBinForProduct.getValue()=="on"?true:this.isBinForProduct.getValue());
                    rec.isBatchForProduct=(this.isBatchForProduct.getValue()=="on"?true:this.isBatchForProduct.getValue());
                    rec.isSerialForProduct=(this.isSerialForProduct.getValue()=="on"?true:this.isSerialForProduct.getValue());
                    rec.isSKUForProduct=(this.isSKUForProduct.getValue()=="on"?true:this.isSKUForProduct.getValue());
                    rec.syncable=(this.syncable.getValue()=="on"?true:this.syncable.getValue());
                    rec.multiuom=(this.multiuom.getValue()=="on"?true:this.multiuom.getValue());
                    rec.blockLooseSell=(this.blockLooseSell.getValue()=="on"?true:this.blockLooseSell.getValue());
                    rec.countable=(this.countable.getValue()=="on"?true:this.countable.getValue());
                    rec.schemaType=this.schemaType.getValue();
                    rec.uomschemaType=this.schemaType.getValue();
                    if(CompanyPreferenceChecks.displayUOMCheck()){
                        rec.displayUoM = this.displayUom.getValue();
                    }
                    rec.mrprate=this.mrpOfproduct.getValue();
                   // rec.mrpofproduct=this.mrpOfproduct.getValue();
                    rec.currencyid=this.Currency.getValue();
                    rec.qaenable=(this.qaenable.getValue()=="on"?true:this.qaenable.getValue());
                    rec.recyclable=(this.isRecylable.getValue()=="on"?true:this.isRecylable.getValue());
                    rec.assembly=asemblyjson;
                    rec.bomdetailjson=bomdetailjson;
                    rec.qualitycontroldetailjson=qualitycontroldetailjson;
                    rec.productcompositionjson=productcompositionjson;
                    rec.applydate=WtfGlobal.convertToGenericDate(Wtf.serverDate);
                    var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
                    rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):true;
                    if(Wtf.getCmp('bserialId'+this.id).getValue()){
                        rec.barcodeField=Wtf.BarcodeGenerator_SerialId;
                    }else if(Wtf.getCmp('bprodId'+this.id).getValue()){
                        rec.barcodeField=Wtf.BarcodeGenerator_ProductId;
                    }else if(Wtf.getCmp('brcodeId'+this.id).getValue()){
                        rec.barcodeField=Wtf.BarcodeGenerator_Barcode;
                    }else if(Wtf.getCmp('bSkuId'+this.id)!=undefined && Wtf.getCmp('bSkuId'+this.id).getValue()){
                        rec.barcodeField=Wtf.BarcodeGenerator_SKUField;
                    }else if(Wtf.getCmp('bbatchId'+this.id).getValue()){
                        rec.barcodeField=Wtf.BarcodeGenerator_BatchID;
                    }else{
                        rec.barcodeField=Wtf.BarcodeGenerator_NULL;
                    }
                    /**********************************************************************
                     *
                     *                NEW RECORDS ADDED HERE 
                     *
                     *********************************************************************/
                    rec.isActiveItem= (this.ItemActiveStatus.getValue()=="on"?true:this.ItemActiveStatus.getValue());
                    rec.isKnittingItem=(this.ItemKnittingStatus.getValue()=="on"?true:this.ItemKnittingStatus.getValue());
                
                    rec.isreusable=(this.itemReusability.getValue());
                    rec.reusabilitycount=(this.reusabilityCount.getValue());
                
                    rec.licensetype=(this.licensetype.getValue());
                    rec.licensecode=(this.licensecode.getValue());
                    rec.inspectionTemplateId=(this.inspectionTemplate.getValue());
                        
                    // Purchase Tab fields
                    rec.catalogNo=this.CatalogNo.getValue();
                    rec.vendor=this.vendor.getValue();
                    rec.purchaseuomid=this.Purchaseuom.getValue();
                    rec.itempurchaseheight=this.ItemPurchaseHeight.getValue();
                    rec.itempurchasewidth=this.ItemPurchaseWidth.getValue();
                    rec.itempurchaselength=this.ItemPurchaseLength.getValue();
                    rec.itempurchasevolume=this.ItemPurchaseVolume.getValue();
                    rec.purchasemfg=this.PurchaseMfg.getValue();
                    rec.initialprice=this.initialprice.getValue();
                
                    rec.purchaseaccountid=this.purchaseAcc.getValue();
                    rec.purchaseretaccountid=this.purchaseReturnAcc.getValue();
                    rec.inputVAT=this.inputVAT.getValue();
                    rec.cstVATattwo=this.cstVATattwo.getValue();
                    rec.cstVAT=this.cstVAT.getValue();
                    rec.inputVATSales=this.inputVATSales.getValue();
                    rec.cstVATattwoSales=this.cstVATattwoSales.getValue();
                    rec.cstVATSales=this.cstVATSales.getValue();                    

                    // Sales Tab fields
                
                    rec.salesaccountid=this.salesAcc.getValue();
                    rec.salesretaccountid=this.salesReturnAcc.getValue(); 
                    if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isitcapplicable) {
                        rec.itcAccountId=this.itcAccountCombo.getValue();
                        rec.itctype=this.itcTypeCmbForProduct.getValue();
                    }

                    rec.salesuomid=this.Salesuom.getValue()!=""?this.Salesuom.getValue():this.uom.getValue();
                    rec.itemsalesheight=this.ItemSalesHeight.getValue();
                    rec.itemsaleswidth=this.ItemSalesWidth.getValue();
                    rec.itemsaleslength=this.ItemSalesLength.getValue();
                    rec.itemsalesvolume=this.ItemSalesVolume.getValue();
                
                    rec.alternateproductid=this.AlternateProducts.getValue();
                    
                    //  Properties Tab fields
                    
                    rec.salesuomid=this.Salesuom.getValue()!=""?this.Salesuom.getValue():this.uom.getValue();
                    rec.itemheight=this.ItemHeight.getValue();
                    rec.itemwidth=this.ItemWidth.getValue();
                    rec.itemlength=this.ItemLength.getValue();
                    rec.itemvolume=this.ItemVolume.getValue();
                    rec.itemcolor=this.ItemColor.getValue();
                    rec.productweight=this.productWeight.getValue();
                    rec.productweightperstockuom=this.productWeightPerStockUom.getValue();
                    rec.productweightincludingpakagingperstockuom=this.productWeightIncludingPakagingPerStockUom.getValue();
                    rec.productvolumeperstockuom=this.productVolumePerStockUom.getValue();
                    rec.productvolumeincludingpakagingperstockuom=this.productVolumeIncludingPakagingPerStockUom.getValue();
                    rec.warrantyperiod=this.warrantyperiod.getValue();
                    rec.warrantyperiodsal=this.warrantyperiodSal.getValue();
                   
                    //  Remarks Tab fields
                 
                    rec.additionalfreetext=this.AdditionalFreeText.getValue();
                    
                    //  Inventory Data Tab fields
                    
                    if(this.ValuationMethod0.getValue()==true){
                        rec.valuationmethod=0;
                    }
                    if(this.ValuationMethod1.getValue()==true){
                        rec.valuationmethod=1;
                    }
                    if(this.ValuationMethod2.getValue()==true){
                        rec.valuationmethod=2;
                    }
                    
                    rec.warehouse=this.wareHouseEditor.getValue();
                    rec.uomid=this.uom.getValue();
                    rec.casinguomid=this.CasingUoMCombo.getValue();
                    rec.casinguomvalue=this.CasingUoMValue.getValue();
                    rec.inneruomid=this.InnerUoMCombo.getValue();
                    rec.inneruomvalue=this.InnerUoMValue.getValue();
                    rec.stockuomid=this.uom.getValue();
                    rec.stockuomvalue=this.StockUoMValue.getValue();
                    rec.orderinguomid=this.OrderingUoMCombo.getValue()!=""?this.OrderingUoMCombo.getValue():this.uom.getValue();
                    rec.transferuomid=this.TransferUoMCombo.getValue()!=""?this.TransferUoMCombo.getValue():this.uom.getValue();
                    rec.packaging=this.packaging.getValue();
                    //rec.itemcost=this.ItemCost.getValue();
                    rec.WIPoffset=this.WIPOffset.getValue();
                    rec.Inventoryoffset=this.InventoryOffset.getValue();
                    rec.CCFrequency=this.CycleCountFrequencyCombo.getValue();    
                    rec.location=this.locationEditor.getValue();
                    rec.leadtime=this.leadtime.getValue();
                    rec.QAleadtime=this.QAleadtime.getValue();
                    rec.hsCode=this.hsCode.getValue();
                    rec.reorderlevel=this.reorderLevel.getValue();
                    rec.reorderquantity=this.rQuantity.getValue();
                    
                    rec.customercategory=this.customerCategoryCombo.getValue();
                     rec.name = this.Pname.getValue();
                    rec.ispropagatetochildcompanyflag = this.ispropagatetochildcompanyflag;
                    rec.isrecyclable=(this.isRecylable.getValue()=="on"?true:this.isRecylable.getValue());
                    rec.rcmapplicable = (this.rcmapplicable.getValue() == "on" ? true : this.rcmapplicable.getValue());
                    rec.vendorid=this.vendor.getValue();
                    rec.uomschemaType=this.schemaType.getValue();
                    if(CompanyPreferenceChecks.displayUOMCheck() != false){
                        rec.displayUoM = this.displayUom.getValue();
                    }
                    rec.mrprate=this.mrpOfproduct.getValue();
                    //rec.mrpofproduct=this.mrpOfproduct.getValue();
                    if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA) {
                        if (this.gstapplieddate == undefined) {
                            this.gstapplieddate = Wtf.account.companyAccountPref.firstfyfrom;
                        }
                        rec.isgstdetailsupdated = this.isgstdetailsupdated;
                        rec.gstapplieddate = WtfGlobal.convertToGenericDate(this.gstapplieddate);
                    }
                    if (this.stockAdjustmentAcc) {
                        rec.stockadjustmentaccountid = this.stockAdjustmentAcc.getValue();
                    }
                    if (this.inventoryAcc) {
                        rec.inventoryaccountid = this.inventoryAcc.getValue();
                    }
                    if (this.cogsAcc) {
                        rec.cogsaccountid = this.cogsAcc.getValue();
                    }

                    Wtf.Ajax.requestEx({
                        //                        url: Wtf.req.account+'CompanyManager.jsp',
                        url: "ACCProductCMN/saveProduct.do",
                        params: rec
                    },this,this.genSuccessResponse,this.genFailureResponse);
                }else{
                    //                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),(this.producttypeval != Wtf.producttype.service?WtfGlobal.getLocaleText("acc.product.msg7"):"")+" "+WtfGlobal.getLocaleText("acc.product.msg8"),function(btn){
                    //                        if(btn!="yes") {return;}
                    WtfComMsgBox(27,3,true);
                    var rec=this.ProductFormGroup.getForm().getValues();
                
                    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    if (custFieldArr.length > 0)
                        rec.customfield = JSON.stringify(custFieldArr);
                    rec.mode=21;
                        
                    rec.pid=this.PID.getValue();
                    rec.asOfDate=WtfGlobal.convertToGenericDate(this.asOfDate.getValue());
                    rec.batchDetails=this.batchDetails;
                     
                    rec.purchasetaxId=this.purchaseTax.getValue();
                    /*
                     * ERP-40242 : In copy case, deactivated tax not shown.Hence, empty taxid set in record.          
                     */
                    if (rec.purchasetaxId != '' && this.isClone) {
                        var taxActivatedRec = WtfGlobal.searchRecord(this.purchaseTaxStore, rec.purchasetaxId, "prtaxid");
                        if (taxActivatedRec == null || taxActivatedRec == undefined || taxActivatedRec == "") {
                            rec.purchasetaxId = ''; 
                        }
                    }
                    rec.salestaxId=this.salesTax.getValue(); 
                    /*
                     * ERP-40242 : In copy case, deactivated tax not shown.Hence, empty taxid set in record.          
                     */
                    if (rec.salestaxId != '' && this.isClone) {
                        var taxActivatedRec = WtfGlobal.searchRecord(this.salesTaxStore, rec.salestaxId, "prtaxid");
                        if (taxActivatedRec == null || taxActivatedRec == undefined || taxActivatedRec == "") {
                            rec.salestaxId = '';
                        }
                    }
                    rec.parentname=this.parentname.getRawValue();
                    //      rec.creationdate=this.CreationDate.getValue();
                    rec.isLocationForProduct=(this.isLocationForProduct.getValue()=="on"?true:this.isLocationForProduct.getValue());
                    rec.isWarehouseForProduct=(this.isWarehouseForProduct.getValue()=="on"?true:this.isWarehouseForProduct.getValue());
                    rec.isRowForProduct=(this.isRowForProduct.getValue()=="on"?true:this.isRowForProduct.getValue());
                    rec.isRackForProduct=(this.isRackForProduct.getValue()=="on"?true:this.isRackForProduct.getValue());
                    rec.isBinForProduct=(this.isBinForProduct.getValue()=="on"?true:this.isBinForProduct.getValue());
                    rec.isBatchForProduct=(this.isBatchForProduct.getValue()=="on"?true:this.isBatchForProduct.getValue());
                    rec.isSerialForProduct=(this.isSerialForProduct.getValue()=="on"?true:this.isSerialForProduct.getValue());
                    rec.isSKUForProduct=(this.isSKUForProduct.getValue()=="on"?true:this.isSKUForProduct.getValue());
                    rec.quantity=this.quantity.getValue();
                    rec.syncable=(this.syncable.getValue()=="on"?true:this.syncable.getValue());
                    rec.recyclable=(this.isRecylable.getValue()=="on"?true:this.isRecylable.getValue());
                    rec.isWastageApplicable = this.isWastageApplicable.getValue();
                    rec.wastageAccount = (this.isWastageApplicable.getValue()) ? this.wastageAccount.getValue() : "";
                    rec.multiuom=(this.multiuom.getValue()=="on"?true:this.multiuom.getValue());
                    rec.blockLooseSell=(this.blockLooseSell.getValue()=="on"?true:this.blockLooseSell.getValue());
                    rec.countable=(this.countable.getValue()=="on"?true:this.countable.getValue());
                    rec.schemaType=this.schemaType.getValue();
                    rec.uomschemaType=this.schemaType.getValue();
                    if(CompanyPreferenceChecks.displayUOMCheck() != false){
                        rec.displayUoM = this.displayUom.getValue();
                    }
                    rec.mrprate=this.mrpOfproduct.getValue();
                    //rec.mrpofproduct=this.mrpOfproduct.getValue();
                    rec.autoAssembly=(this.autoAssembly.getValue()=="on"?true:this.autoAssembly.getValue());
                    rec.qaenable=(this.qaenable.getValue()=="on"?true:this.qaenable.getValue());
                    rec.intervalField=(this.isInterval.getValue()=="on"?true:this.isInterval.getValue());
                    rec.addshiplentheithqty=(this.addShipLengthqty.getValue()=="on"?true:this.addShipLengthqty.getValue());
                    rec.assembly=asemblyjson;
                    rec.bomdetailjson=bomdetailjson;
                    rec.qualitycontroldetailjson=qualitycontroldetailjson;
                    rec.productcompositionjson=productcompositionjson;
                    rec.applydate=WtfGlobal.convertToGenericDate(Wtf.serverDate.clearTime(true));
                    rec.currencyid=this.Currency.getValue();
                    var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
                    rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):true;
                    if(Wtf.getCmp('bserialId'+this.id).getValue()){
                        rec.barcodeField=Wtf.BarcodeGenerator_SerialId;
                    }else if(Wtf.getCmp('bprodId'+this.id).getValue()){
                        rec.barcodeField=Wtf.BarcodeGenerator_ProductId;
                    }else if(Wtf.getCmp('brcodeId'+this.id).getValue()){
                        rec.barcodeField=Wtf.BarcodeGenerator_Barcode;
                    }else if(Wtf.getCmp('bSkuId'+this.id)!=undefined && Wtf.getCmp('bSkuId'+this.id).getValue()){
                        rec.barcodeField=Wtf.BarcodeGenerator_SKUField;
                    }else if(Wtf.getCmp('bbatchId'+this.id).getValue()){
                        rec.barcodeField=Wtf.BarcodeGenerator_BatchID;
                    }else{
                        rec.barcodeField=Wtf.BarcodeGenerator_NULL;
                    }
                    /**********************************************************************
                     *
                     *                NEW RECORDS ADDED HERE 
                     *
                     *********************************************************************/
                    rec.isActiveItem= (this.ItemActiveStatus.getValue()=="on"?true:this.ItemActiveStatus.getValue());
                    rec.isKnittingItem=(this.ItemKnittingStatus.getValue()=="on"?true:this.ItemKnittingStatus.getValue());
                
                    rec.isreusable=(this.itemReusability.getValue());
                    rec.reusabilitycount=(this.reusabilityCount.getValue());  
                    
                    rec.substituteProductId = this.substituteProductCombo.getValue();
                    rec.substituteQty = this.substituteQty.getValue();
                
                    rec.licensetype=(this.licensetype.getValue());
                    rec.licensecode=(this.licensecode.getValue());
                    rec.inspectionTemplateId=(this.inspectionTemplate.getValue());
                    rec.productBrandId = this.productBrandCombo.getValue();
                    rec.landingCostCategoryId = this.landingCostCategoryCombo.getValue();
                    // Purchase Tab fields
                    rec.catalogNo=this.CatalogNo.getValue();
                    rec.vendor=this.vendor.getValue();
                    rec.purchaseuomid=this.Purchaseuom.getValue()!=""?this.Purchaseuom.getValue():this.uom.getValue();
                    rec.itempurchaseheight=this.ItemPurchaseHeight.getValue();
                    rec.itempurchasewidth=this.ItemPurchaseWidth.getValue();
                    rec.itempurchaselength=this.ItemPurchaseLength.getValue();
                    rec.itempurchasevolume=this.ItemPurchaseVolume.getValue();
                    rec.purchasemfg=this.PurchaseMfg.getValue();
                    rec.initialprice=this.initialprice.getValue();
                
                    rec.purchaseaccountid=this.purchaseAcc.getValue();
                    rec.purchaseretaccountid=this.purchaseReturnAcc.getValue();
                    rec.inputVAT=this.inputVAT.getValue();
                    rec.cstVATattwo=this.cstVATattwo.getValue();
                    rec.cstVAT=this.cstVAT.getValue();
                    rec.inputVATSales=this.inputVATSales.getValue();
                    rec.cstVATattwoSales=this.cstVATattwoSales.getValue();
                    rec.cstVATSales=this.cstVATSales.getValue();
                    
                    // Sales Tab fields
                
                    rec.salesaccountid=this.salesAcc.getValue();
                    rec.salesretaccountid=this.salesReturnAcc.getValue();
                    if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isitcapplicable) {
                        rec.itcAccountId = this.itcAccountCombo.getValue();
                        rec.itctype=this.itcTypeCmbForProduct.getValue();
                    }

                    rec.salesuomid=this.Salesuom.getValue()!=""?this.Salesuom.getValue():this.uom.getValue();
                    rec.itemsalesheight=this.ItemSalesHeight.getValue();
                    rec.itemsaleswidth=this.ItemSalesWidth.getValue();
                    rec.itemsaleslength=this.ItemSalesLength.getValue();
                    rec.itemsalesvolume=this.ItemSalesVolume.getValue();
                
                    rec.alternateproductid=this.AlternateProducts.getValue();
                    
                    //  Properties Tab fields
                    
                    rec.itemheight=this.ItemHeight.getValue();
                    rec.itemwidth=this.ItemWidth.getValue();
                    rec.itemlength=this.ItemLength.getValue();
                    rec.itemvolume=this.ItemVolume.getValue();
                    rec.itemcolor=this.ItemColor.getValue();
                    rec.productweight=this.productWeight.getValue();
                    rec.productweightperstockuom=this.productWeightPerStockUom.getValue();
                    rec.productweightincludingpakagingperstockuom=this.productWeightIncludingPakagingPerStockUom.getValue();
                    rec.productvolumeperstockuom=this.productVolumePerStockUom.getValue();
                    rec.productvolumeincludingpakagingperstockuom=this.productVolumeIncludingPakagingPerStockUom.getValue();
                    rec.warrantyperiod=this.warrantyperiod.getValue();
                    rec.warrantyperiodsal=this.warrantyperiodSal.getValue();
                    //  Remarks Tab fields
                 
                    rec.additionalfreetext=this.AdditionalFreeText.getValue();
                    
                    //  Inventory Data Tab fields
                    
                    if(this.ValuationMethod0.getValue()==true){
                        rec.valuationmethod=0;
                    }
                    if(this.ValuationMethod1.getValue()==true){
                        rec.valuationmethod=1;
                    }
                    if(this.ValuationMethod2.getValue()==true){
                        rec.valuationmethod=2;
                    }
                    rec.producttype=this.producttype.getValue();
                    rec.warehouse=this.wareHouseEditor.getValue();
                    rec.uomid=this.uom.getValue();
                    rec.casinguomid=this.CasingUoMCombo.getValue();
                    rec.casinguomvalue=this.CasingUoMValue.getValue();
                    rec.inneruomid=this.InnerUoMCombo.getValue();
                    rec.inneruomvalue=this.InnerUoMValue.getValue();
                    rec.stockuomid=this.uom.getValue();
                    rec.stockuomvalue=this.StockUoMValue.getValue();
                    rec.orderinguomid=this.OrderingUoMCombo.getValue()!=""?this.OrderingUoMCombo.getValue():this.uom.getValue();
                    rec.transferuomid=this.TransferUoMCombo.getValue()!=""?this.TransferUoMCombo.getValue():this.uom.getValue();
                    rec.packaging=this.packaging.getValue();
                    //rec.itemcost=this.ItemCost.getValue();
                    rec.WIPoffset=this.WIPOffset.getValue();
                    rec.Inventoryoffset=this.InventoryOffset.getValue();
                    rec.CCFrequency=this.CycleCountFrequencyCombo.getValue();
                    rec.location=this.locationEditor.getValue();
                    rec.leadtime=this.leadtime.getValue();
                    rec.QAleadtime=this.QAleadtime.getValue();
                    rec.hsCode=this.hsCode.getValue();
                    rec.reorderlevel=this.reorderLevel.getValue();
                    rec.reorderquantity=this.rQuantity.getValue();
                    rec.name = this.Pname.getValue();
                    rec.ispropagatetochildcompanyflag = this.ispropagatetochildcompanyflag;
                    rec.isrecyclable=(this.isRecylable.getValue()=="on"?true:this.isRecylable.getValue());
                    rec.rcmapplicable = (this.rcmapplicable.getValue() == "on" ? true : this.rcmapplicable.getValue());
                    rec.vendorid=this.vendor.getValue();
                    rec.uomschemaType=this.schemaType.getValue();
                    if(CompanyPreferenceChecks.displayUOMCheck()!= false){
                        rec.displayUoM = this.displayUom.getValue();
                    }
                    rec.mrprate= this.mrpOfproduct.getValue();
                    //rec.mrpofproduct=this.mrpOfproduct.getValue();
                    rec.customercategory=this.customerCategoryCombo.getValue();
                   
                    this.GeneralTab.setTitle(this.GeneralTab.title.replace(/<(?:.|\n)*?>/gm, '')); //reset the last red tab to its
                    this.PurchaseTab.setTitle(this.PurchaseTab.title.replace(/<(?:.|\n)*?>/gm, '') );// original colour
                    this.SalesTab.setTitle(this.SalesTab.title.replace(/<(?:.|\n)*?>/gm, ''));// regex used to remove unwanted HTML code
                    this.InventoryTab.setTitle(this.InventoryTab.title.replace(/<(?:.|\n)*?>/gm, ''));
                    if (this.stockAdjustmentAcc) {
                        rec.stockadjustmentaccountid = this.stockAdjustmentAcc.getValue();
                    }
                    if (this.inventoryAcc) {
                        rec.inventoryaccountid = this.inventoryAcc.getValue();
                    }
                    if (this.cogsAcc) {
                        rec.cogsaccountid = this.cogsAcc.getValue();
                    }
                    rec.cloneId=this.idOfCloningProduct;
                    rec.isClone=this.isClone;
                    if(this.isClone){
                        rec.isActiveItem=true;
                    }
                    if(this.gstapplieddate==undefined){
                        this.gstapplieddate=this.asOfDate.getValue();
                    }
                    rec.isgstdetailsupdated=this.isgstdetailsupdated;
                    rec.gstapplieddate=WtfGlobal.convertToGenericDate(this.gstapplieddate);
                    Wtf.Ajax.requestEx({
                        //                        url: Wtf.req.account+'CompanyManager.jsp',
                        url: "ACCProductCMN/saveProduct.do",//create case call
                        params: rec
                    },this,this.genSuccessResponse,this.genFailureResponse);
                //            },this)
                }
                    
            }
        }
        else{
            this.validMandatory();
        //            WtfComMsgBox(2, 2);
        }
    },
    isDefaultBOMpresntForAssemblyProduct: function () {
        var isDefaultBOMPresent = false;
        for (var i = 0; i < this.createdBOMStore.getCount(); i++) {
            if (this.createdBOMStore.getAt(i).data['isdefaultbom'] == true) {
                isDefaultBOMPresent = true;
                break;
            }
        }
        return isDefaultBOMPresent;
    },
    genSuccessResponse:function(response){
        if(response.success){
            
            if(this.saveOnlyFlag != undefined && this.saveAndCreateNewFlag != undefined && this.saveOnlyFlag && !this.saveAndCreateNewFlag){
                this.disableComponent();
            }else if(this.saveOnlyFlag != undefined && this.saveAndCreateNewFlag != undefined && !this.saveOnlyFlag && this.saveAndCreateNewFlag){
                this.isEdit=false;
                this.isClone=false;
                this.record=undefined;
//                this.resetComponent();    //SDP-12638
            }
            
            var titleMsg = (this.isFixedAsset)?WtfGlobal.getLocaleText("acc.fixed.asset.group"):WtfGlobal.getLocaleText("acc.invReport.prod");
            WtfComMsgBox([titleMsg,response.msg],0);
            this.fireEvent("productsave",this,[response.success]);
            if(this.record!=null){
                this.productID=this.record.data.productid;
            }
            this.GeneralTab.setTitle(this.GeneralTab.title.replace(/<(?:.|\n)*?>/gm, ''));//in edit case : reset the last red tab to its original colour and regex used to remove unwanted HTML code
            if(!this.isEdit){
                var pricerec={};
                this.productID=response.productID;
                //Set initial Purchase price
                if(this.initialprice.getValue()>0){
                    pricerec.carryin=true;
                    pricerec.productid=response.productID;
                        
                    pricerec.price=this.initialprice.getValue();
                    pricerec.mode=11;
//                    pricerec.applydate=WtfGlobal.convertToGenericDate(Wtf.serverDate.clearTime(true));
                    pricerec.applydate=WtfGlobal.convertToGenericDate(this.asOfDate.getValue());
                    //While creating product, asOfDate is saved as 'updatedate' in inventory, & also should be in 'applydate' in pricelist.
                    pricerec.currencyid=this.Currency.getValue();
                    pricerec.initialPrice = true;
                    Wtf.Ajax.requestEx({
                        //                    url: Wtf.req.account+'CompanyManager.jsp',
                        url:"ACCProduct/setNewPrice.do",
                        params: pricerec
                    },this,function(){
                        //                      this.fireEvent('update',this);
                        },this.genPriceFailureResponse);
                    if(this.isEdit && !this.isClone){  
                    Wtf.apply(this.detailPanel, {accid:this.productID});
                    Wtf.apply(this.detailPanel, {acccode:this.PID.value});
                        this.getDetails();     
                    }
                }

                //Set initial Sales price
                if(this.initialsalesprice.getValue()>0){
                    var pricerec1={};				// Send params as object and not as array bcos general controller accept requests in object form
                    pricerec1.carryin=false;
                    pricerec1.productid=response.productID;
                    pricerec1.price=this.initialsalesprice.getValue();
                    pricerec1.mode=11;
//                    pricerec1.applydate=WtfGlobal.convertToGenericDate(Wtf.serverDate.clearTime(true));
                    //While creating product, asOfDate is saved as 'updatedate' in inventory, & also should be in 'applydate' in pricelist.
                    pricerec1.applydate=WtfGlobal.convertToGenericDate(this.asOfDate.getValue());
                    pricerec1.currencyid=this.Currency.getValue();
                    Wtf.Ajax.requestEx({
                        //                    url: Wtf.req.account+'CompanyManager.jsp',
                        url:"ACCProduct/setNewPrice.do",
                        params: pricerec1
                    },this,function(){
//                        this.fireEvent('update',this);
                    },this.genPriceFailureResponse);
                } 
		if(this.saveOnlyFlag != undefined && this.saveAndCreateNewFlag != undefined && !this.saveOnlyFlag && this.saveAndCreateNewFlag){
                    this.resetComponent();  //SDP-12638
                }
//                this.fireEvent('update',this);
            }else{
                    var pricerec={};
                    this.productID=response.productID;
                     //Set initial Purchase price 
//                    if(this.initialprice.getValue()>0){ //ERP-16804
                        pricerec.carryin=true;
                        pricerec.productid=response.productID;
                        pricerec.changeprice = true;							// To change existing price			Neeraj
                        pricerec.price=this.initialprice.getValue();
                        pricerec.mode=11;
//                        pricerec.applydate=WtfGlobal.convertToGenericDate(Wtf.serverDate.clearTime(true));
                        pricerec.applydate=WtfGlobal.convertToGenericDate(this.asOfDate.getValue());
                        //While creating product, asOfDate is saved as 'updatedate' in inventory, & also should be in 'applydate' in pricelist.
                        pricerec.currencyid=this.Currency.getValue();
                        pricerec.uomid=this.record.data.uomid;
                        pricerec.initialPrice = true;
                        Wtf.Ajax.requestEx({
                            url:"ACCProduct/setNewPrice.do",
                            params: pricerec
                        },this,function(){},this.genPriceFailureResponse);
//                    }
                    //Set initial Sales price
//                    if(this.initialsalesprice.getValue()>0){ //ERP-16804
                        var pricerec1={};				
                        pricerec1.carryin=false;
                        pricerec1.productid=response.productID;
                        pricerec1.changeprice = true;
                        pricerec1.price=this.initialsalesprice.getValue();
                        pricerec1.mode=11;
//                        pricerec1.applydate=WtfGlobal.convertToGenericDate(Wtf.serverDate.clearTime(true));
                        //While creating product, asOfDate is saved as 'updatedate' in inventory, & also should be in 'applydate' in pricelist.
                        pricerec1.applydate=WtfGlobal.convertToGenericDate(this.asOfDate.getValue());
                        pricerec1.currencyid=this.Currency.getValue();
                        pricerec1.uomid=this.record.data.uomid;     
                        Wtf.Ajax.requestEx({
                            url:"ACCProduct/setNewPrice.do",
                            params: pricerec1
                        },this,function(){},this.genPriceFailureResponse);
//                    }                                         
//                this.fireEvent('productClosed',this);        	    
            }
            Wtf.notify.msg("", WtfGlobal.getLocaleText("acc.field.ProductAndServicesReportRefreshedmsg"));
            getCompanyAccPref(true);//Load this Stores in case of Success Response Only. flag has send to hide loading mask.
//            Wtf.getCmp('ProductReportGrid_one').getStore().reload(); //We are loading same request on update event so no need to load from here 
            this.isClosable = true;
        }else if (response.isDuplicateExe) {
            Wtf.MessageBox.hide();
            this.newnowin = new Wtf.Window({
                title: WtfGlobal.getLocaleText("acc.common.success"),
                closable: true,
                iconCls: getButtonIconCls(Wtf.etype.deskera),
                width: 330,
                autoHeight: true,
                modal: true,
                bodyStyle: "background-color:#f1f1f1;",
                closable:false,
                        buttonAlign: 'right',
                items: [new Wtf.Panel({
                        border: false,
                        html: (response.msg.length > 60) ? response.msg : "<br>" + response.msg,
                        height: 50,
                        bodyStyle: "background-color:white; padding: 7px; font-size: 11px; border-bottom: 1px solid #bfbfbf;"
                    }),
                    this.newdoForm = new Wtf.form.FormPanel({
                        labelWidth: 190,
                        border: false,
                        autoHeight: true,
                        bodyStyle: 'padding:10px 5px 3px; ',
                        autoWidth: true,
                        defaultType: 'textfield',
                        items: [this.newdono = new Wtf.form.TextField({
                                fieldLabel: WtfGlobal.getLocaleText("acc.product.newproductid"),
                                allowBlank: false,
                                labelSeparator: '',
                                width: 90,
                                itemCls: 'nextlinetextfield',
                                name: 'newdono',
                                id: 'newdono'
                            })],
                        buttons: [{
                                text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                                handler: function () {
                                    if (this.newdono.validate()) {
                                        this.PID.setValue(this.newdono.getValue());
                                        this.saveForm();
                                        this.newnowin.close();
                                    }
                                },
                                scope: this
                            }, {
                                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"), //"Cancel",
                                scope: this,
                                handler: function () {
                                    this.newnowin.close();
                                }
                            }]
                    })]
            });
            this.newnowin.show();
             this.isClosable = true;
        } else if (response.isTaxDeactivated) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), response.msg], 2);
        }else{
            if(this.isFixedAsset){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.invoice.gridAssetGroup"),response.msg],1);
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.invReport.prod"),response.msg],2);
            }
        }
  
    },

    disableComponent : function(){
        if(this.saveBttn){
            this.saveBttn.disable();
            this.savencreateBttn.disable();
        }
        if(this.ProductFormGroup.getForm().isValid() && this.PurchaseForm.getForm().isValid() && this.SalesForm.getForm().isValid() &&
            this.PropertiesForm.getForm().isValid() && this.RemarksForm.getForm().isValid() && this.InventoryDataForm.getForm().isValid()){
            this.ProductFormGroup.setDisabled(true);
            this.PurchaseForm.disable();
            this.SalesForm.disable();
            this.PropertiesForm.disable();
            this.RemarksForm.disable();
            this.InventoryDataForm.disable();
        }
        if(this.bomFieldset){
            this.bomFieldset.disable();
        }
        this.disableQualityControlComponent();
        
    },
    disableQualityControlComponent: function(){
        if(this.qualityControlForm){
            this.qualityControlForm.disable();
        }
        if(this.qualityControlGrid){
            this.qualityControlGrid.disable();
        }
        if(this.submitBttnQC){
            this.submitBttnQC.disable();
        }
        if(this.editBttnQC){
            this.editBttnQC.disable();
        }
        if(this.newBttnQC){
            this.newBttnQC.disable();
        }
    },
    resetComponent : function(){
        if (this.saveAndCreateNewFlag) {
            var producttype=this.producttype.getValue();
        }
        if(this.PropertiesForm){
            this.PropertiesForm.getForm().reset();
        }
        if(this.ProductFormGroup){
            this.ProductFormGroup.getForm().reset();
        }
        if(this.PurchaseForm){
            this.PurchaseForm.getForm().reset();
        }
        if(this.SalesForm){
            this.SalesForm.getForm().reset();
        }
        if(this.PropertiesForm){
            this.PropertiesForm.getForm().reset();
        }
        if(this.RemarksForm){
            this.RemarksForm.getForm().reset();
        }
        if(this.InventoryDataForm){
            this.InventoryDataForm.getForm().reset();
        }
        if(this.qualityControlForm){
            this.qualityControlForm.getForm().reset();
        }
        if(this.bomCreationForm){
            this.bomCreationForm.getForm().reset();
        }
        if(this.westPanel){
            this.westPanel.getForm().reset();
        }
        
        if (this.saveAndCreateNewFlag) {
            this.newPanel.setActiveTab(this.GeneralTab);
            this.asOfDate.enable();
            this.asOfDate.setValue(Wtf.account.companyAccountPref.firstfyfrom);//refer ticket ERP-18283
            this.producttype.enable();
            this.producttype.setValue(producttype);
            this.changeLayoutWithType();
            this.sequenceFormatCombobox.enable();
            this.quantity.enable();
            this.setNextNumber();
            if (this.bomFieldset && !Wtf.isEmpty(this.producttype) && Wtf.producttype.assembly != this.producttype.getValue()) {
                this.bomFieldset.hide();
            }
            if (this.bomFieldset && !Wtf.isEmpty(this.producttype) && Wtf.producttype.assembly == this.producttype.getValue()) {
                if (this.bomCreationForm) {
                    this.bomCreationForm.getForm().reset();
                }
                if (this.AssemblyGrid) {
                    if (this.AssemblyGrid.itemsgrid && this.AssemblyGrid.itemsgrid.getStore()) {
                        this.AssemblyGrid.itemsgrid.getStore().removeAll();
                        this.AssemblyGrid.addBlankRecord(true);
                        if (this.AssemblyGrid.tplSummary && this.AssemblyGrid.southSummaryPanel) {
                            if (this.AssemblyGrid.southSummaryPanel.body) {
                                this.AssemblyGrid.tplSummary.overwrite(this.AssemblyGrid.southSummaryPanel.body, {total: 0});
                            } 
                        }
                    }
                }
                if (this.createdBOMGrid) {
                    this.createdBOMGrid.getStore().removeAll();
                }
                if (this.initialprice) {
                    this.initialprice.setValue(0);
                }
            }
            if(this.tagsFieldset && !(this.isEdit || this.isClone)){
                this.tagsFieldset.resetCustomComponents();
            }
            this.currencyStore.load({params: {mode: 201, transactiondate: WtfGlobal.convertToGenericDate(Wtf.account.companyAccountPref.bbfrom)}});
        }
        
    },
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
            //        this.fireEvent('closed',this);
    },
    addAccount: function(store,issales,ispurchase,incomenature,isexpense){
        callCOAWindow(false,null,"coaWin",issales,ispurchase,false,false,false,incomenature,isexpense,"","",0);  //0 for profit and loss accounttype
        Wtf.getCmp("coaWin").on('update',function(){store.reload();},this);
    },
    //addUomSchema: function(store,issales,ispurchase,incomenature){
    //   this.UomSchemaWindow = new Wtf.account.UomSchemaWindow({
    //    id: 'uomschemawindow',
    //    title: WtfGlobal.getLocaleText("acc.schema.Type"),
    //    border: false,
    //    layout:'border'
    //   });
    //    this.UomSchemaWindow.show();  
    //},
    genPriceSuccessResponse:function(response){
        //          if( this.producttype.getValue()==Wtf.producttype.assembly){
        //                Wtf.MessageBox.confirm("Build Assembly"," You want to build assembly?",function(btn){
        //                  if(btn!="yes") { this.fireEvent('update',this,this.productID);return; }
        //                  else
        //                    callBuildAssemblyForm(this.productID);
        //                    this.fireEvent('update',this,this.productID);
        //                },this)
        //          }
        //            else
        this.fireEvent('update',this,this.productID);

    //        this.fireEvent('productClosed',this);
    //        Wtf.productStore.reload();
    //        WtfComMsgBox(['Price List',response.msg],response.success*2+1);
    //        if(response.success) this.fireEvent('update',this);
    //        this.fireEvent('closed',this);
    },
    addQuantityFields : function(a,b,c) {
        if(this.noOfServiceQTY.isValid()){
            for(var i=0;i<c;i++){
                var comp=Wtf.getCmp("filefield"+i);
                this.extraQuantity.remove(comp);
            }
            this.extraQuantity.doLayout();
            if(this.isEdit && this.record.data.qtyUOM){
                var uomStr=this.record.data.qtyUOM;
                var uomArray=uomStr.split(",");
            }
        
            for(var i=0;i<this.noOfServiceQTY.getValue();i++){
                var uomValue="";
                if(this.isEdit && uomArray[i]){
                    uomValue=uomArray[i];
                }
                this.extraQuantity.add({
                    id : "filefield"+i,
                    layout:'form',
                    labelWidth:160,
                    //            bodyStyle:"padding:10px,0px,10px,0px",
                    border :false,
                    items:[new Wtf.form.FnComboBox({
                        fieldLabel:WtfGlobal.getLocaleText("acc.product.uom")+" For Quantity "+(i+1),//'Unit Of Measure*',
                        hiddenName:'qtyuom'+(i+1),
                        id : "combofield"+i,
                        store:Wtf.uomStore,
                        anchor:'85%',
                        allowBlank:false,
                        valueField:'uomid',
                        displayField:'uomname',
                        forceSelection: true//,
                    // addNewFn:this.showUom.createDelegate(this)
                    })]

                })
                if(this.isEdit && this.isFirstTime){
                    if(uomValue!="" && uomValue!="null")
                        Wtf.getCmp("combofield"+i).setValue(uomValue);
                }
            }
            this.extraQuantity.doLayout();
            this.isFirstTime=false;
        }
    },
    genPriceFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    //        this.fireEvent('closed',this);
    },
    hideSalesRevenueAccount:function(){
        WtfGlobal.hideFormElement(this.salesRevenueRecognitionAccount);
        WtfGlobal.hideFormElement(this.revenueRecognitionProcess);
        this.salesRevenueRecognitionAccount.allowBlank = true;
    },
    showSalesRevenueAccount:function(){
        WtfGlobal.showFormElement(this.salesRevenueRecognitionAccount);
        WtfGlobal.showFormElement(this.revenueRecognitionProcess);
        this.salesRevenueRecognitionAccount.allowBlank = true;
    },
    
    // for creating substitute product and qty fields
    createSubstituteProductAndQtyFields: function() {
        this.substituteProductRec = Wtf.data.Record.create([
            {name: 'productid'},
            {name: 'productname'},
            {name: 'desc'},
            {name: 'producttype'}
        ]);
        
        this.substituteProductStore = new Wtf.data.Store({
            url: "ACCProductCMN/getProductsForDropdownOptimised.do",
            baseParams: {
                mode: 22
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.substituteProductRec)
        });
        
        this.substituteProductCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.substituteProduct"), // 'Substitute Product',
            hiddenName: 'substituteProductId',
            name: 'substituteProductId',
            store: this.substituteProductStore,
            valueField: 'productid',
            displayField: 'productname',
            mode: 'remote',
            typeAhead: true,
            triggerAction: 'all',
            emptyText: WtfGlobal.getLocaleText("acc.msgbox.17"),
            extraFields: '',
            anchor: '85%',
            disabled:(this.isUsedInTransaction && this.isEdit && !this.isClone && this.producttypeval==Wtf.producttype.assembly) ? true : false,
            hidden: !Wtf.account.companyAccountPref.activateMRPManagementFlag,
            hideLabel: !Wtf.account.companyAccountPref.activateMRPManagementFlag
        });
        
        this.substituteQty = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.substituteQty"),
            name: 'substituteQty',
            id: 'substituteQty' + this.id,
            maxLength: 15,
            allowNegative: false,
            decimalPrecision: Wtf.QUANTITY_DIGIT_AFTER_DECIMAL,
            anchor: '85%',
            disabled:(this.isUsedInTransaction && this.isEdit && !this.isClone && this.producttypeval==Wtf.producttype.assembly) ? true : false,
            hidden: !Wtf.account.companyAccountPref.activateMRPManagementFlag,
            hideLabel: !Wtf.account.companyAccountPref.activateMRPManagementFlag
        });
    },
    
    // for creating global bom form
    createGlobalBOMForm: function() {
        this.bomCode = new Wtf.form.ExtendedTextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.bomCode") + "*", // "BOM Code",
            name: 'bomCode',
            width: 240,
            maxLength: 50,
            allowBlank: true,
            disabled :(this.isUsedInTransaction && this.isEdit && !this.isClone && this.producttypeval==Wtf.producttype.assembly) ? true : false
        });
        
        this.bomName = new Wtf.form.ExtendedTextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.bomName"), // "BOM Name",
            name: 'bomName',
            width: 240,
            maxLength: 50,
            disabled :(this.isUsedInTransaction && this.isEdit && !this.isClone && this.producttypeval==Wtf.producttype.assembly) ? true : false
        });
        
//        this.linkProductRec = Wtf.data.Record.create([
//            {name: 'productid'},
//            {name: 'productname'},
//            {name: 'desc'},
//            {name: 'producttype'}
//        ]);
//        
//        this.linkProductStore = new Wtf.data.Store({
//            url: "ACCProduct/getProductsForCombo.do",
//            baseParams: {
//                mode: 22
//            },
//            reader: new Wtf.data.KwlJsonReader({
//                root: "data"
//            }, this.linkProductRec)
//        });
//        if (Wtf.account.companyAccountPref.activateMRPManagementFlag) {
//            this.linkProductStore.load();
//        }
//        
//        this.linkProductCombo = new Wtf.form.ExtFnComboBox({
//            fieldLabel: WtfGlobal.getLocaleText("acc.field.linkProductName"), // "Link Product Name",
//            hiddenName: 'linkProductCombo',
//            name: 'linkProductCombo',
//            store: this.linkProductStore,
//            valueField: 'productid',
//            displayField: 'productname',
//            mode: 'remote',
//            typeAhead: true,
//            triggerAction: 'all',
//            emptyText: WtfGlobal.getLocaleText("acc.msgbox.17"),
//            extraFields: '',
//            width: 240
//        });
        
        this.workCenter = new Wtf.form.ExtendedTextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.workCenter"), // "Work Center",
            name: 'workCenter',
            width: 240,
            maxLength: 50
        });

        this.alternateBOMCode = new Wtf.form.ExtendedTextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.alternateBOMCode"), // "Alternate BOM Code",
            name: 'alternateBOMCode',
            width: 240,
            maxLength: 50
        });
        
        this.linkAlternateBOM = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.linkAlternateBOM"), // "Link Alternate BOM", 
            hiddenName: 'linkAlternateBOM',
            name: 'linkAlternateBOM',
            store: this.substituteProductStore,
            valueField: 'productid',
            displayField: 'productname',
            mode: 'remote',
            typeAhead: true,
            triggerAction: 'all',
            emptyText: WtfGlobal.getLocaleText("acc.msgbox.17"),
            extraFields: '',
            width: 240
        });
        
        this.subBOMCode = new Wtf.form.ExtendedTextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.subBOMCode"), // "Sub BOM Code",
            name: 'subBOMCode',
            width: 240,
            maxLength: 50
        });
        
        this.subBOM = new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText('acc.field.IsASubBOM'), // "Is a sub BOM?',
            checkboxToggle: true,
            autoHeight: true,
            autoWidth: true,
            width: 240,
            checkboxName: 'subBOM',
            style: 'margin-right:30px',
            collapsed: true,
            items: [this.subBOMCode]
        });
        
        this.isDefaultBOM = new Wtf.form.Checkbox({
            name: 'isdefaultbom',
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.mrp.field.isdefaultbom") + "'>" + WtfGlobal.getLocaleText("acc.mrp.field.isdefaultbom") + "</span>",
            id: 'isDefaultBOM' + this.id,
            checked: false,
            scope: this,
            cls: 'custcheckbox',
            width: 10,
            disabled :(this.isUsedInTransaction && this.isEdit && !this.isClone && this.producttypeval==Wtf.producttype.assembly) ? true : false
        });
//        this.isDefaultBOM.on("check",this.isDefaultBOMChanged,this);
        
        this.isDefaultBOM.on("change",this.beforeChangeDefaultBOM,this);  // Handling default BOM change
        
        this.bomCreationForm = new Wtf.form.FormPanel({
            region: 'north',
            autoHeight: true,
            disabledClass: "newtripcmbss",
            border: false,
            hidden: !Wtf.account.companyAccountPref.activateMRPManagementFlag,
            items: [
                {
                    layout: 'form',
                    defaults: {
                        border: false
                    },
                    baseCls: 'northFormFormat',
                    labelWidth: 150,
                    disabledClass: "newtripcmbss",
                    items:[ this.bomCode,this.bomName,this.isDefaultBOM
                        
//                        {
//                            layout: 'column',
//                            defaults: {
//                                border: false
//                            },
//                            items:[
//                                {
//                                    layout: 'form',
//                                    columnWidth: 0.55,
//                                    items: [
//                                        this.bomCode,
//                                        this.bomName,
//                                        this.linkProductCombo,
//                                        this.workCenter
//                                    ]
//                                },{
//                                    layout: 'form',
//                                    columnWidth: 0.45,
//                                    items: [
//                                        this.linkAlternateBOM,
//                                        this.alternateBOMCode,
//                                        this.subBOM
//                                    ]
//                                }
//                            ]
//                        }

                    ]
                }
            ]
        });
    },
    isDefaultBOMChanged: function(obj, newval) {
        if (newval === true) {
            for (var i = 0; i < this.createdBOMStore.getCount(); i++) {
                if (this.createdBOMStore.getAt(i).data['isdefaultbom'] == true && this.createdBOMStore.getAt(i).data['bomCode'] != this.bomCode.getValue()) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "Default BOM for product already exists."], 2);
                    obj.setValue(false);
                    return false;
                }
            }
        } 
        
    },
beforeChangeDefaultBOM: function(obj, newVal) {
    if (newVal) {  // Confirmation for changing Default BOM
        if (this.createdBOMStore.data.length == 0 ) {
             return;
        } else {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.rem.4"),(WtfGlobal.getLocaleText("acc.product.BOM.confirm.default")),function(btn){
                if(btn=="yes"){
                    this.changeDefaultBOM(obj, newVal);
                } else{
                    obj.setValue(false);
                    return;
                }
            },this);    
        }
    } else {
        var rec = WtfGlobal.searchRecord(this.createdBOMStore, this.bomCode.getValue(), "bomCode");
        if (rec) {
            rec.data.isdefaultbom = false;
            rec.commit()
        }
         this.createdBOMGrid.getView().refresh();
    }
},
changeDefaultBOM: function(obj, newVal) {
        if (!newVal) {  // Changing def. BOM
            var rec = WtfGlobal.searchRecord(this.createdBOMStore, this.bomCode.getValue(), "bomCode");
            if (rec) {
                rec.data.isdefaultbom = false;
                rec.commit()
            }
        } else {
            this.setNoDefaultBOM(obj,newVal);
            var rec = WtfGlobal.searchRecord(this.createdBOMStore, this.bomCode.getValue(), "bomCode");
            if (rec) {
                rec.data.isdefaultbom = true;
                rec.commit();
            }
        }
        this.createdBOMGrid.getView().refresh();
        
    },
    setNoDefaultBOM:function (obj , newVal) {  // assigning isdefault  to false for every BOM
        for (var i = 0; i < this.createdBOMStore.getCount(); i++) {
            var rec = this.createdBOMStore.getAt(i);
            if (rec) {
                rec.data.isdefaultbom = false;
                rec.commit();
            }
        }
//        this.createdBOMGrid.getView().refresh();
    },
// for creating bom grid for saved bom records
createGridOfBOMSaved: function() {
        // create tbar for menu items of saved bom grid
        this.createTbarForGridOfBOMSaved();
        
        this.gridRecord = new Wtf.data.Record.create([
            {name: 'bomid'},
            {name: 'productid'},
            {name: 'bomCode'},
            {name: 'bomName'},
            {name: 'linkProductId'},
            {name: 'linkProductName'},
            {name: 'workCenter'},
            {name: 'linkAlternateBOMId'},
            {name: 'linkAlternateBOMName'},
            {name: 'alternateBOMCode'},
            {name: 'isdefaultbom'},
            {name: 'subBOMCode'},
            {name: 'bomAssemblyDetails'}
        ]);
        
        this.gridReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: "count",
            remoteGroup: true,
            remoteSort: true
        }, this.gridRecord);
        var productid=(this.record!=undefined && this.record.data!=undefined)?this.record.data.productid:"";
        this.createdBOMStore = new Wtf.data.Store({
            url: "ACCProduct/getBOMDetails.do",
            reader: this.gridReader
        });
        if (productid != "" && Wtf.account.companyAccountPref.activateMRPManagementFlag) {
            this.createdBOMStore.load({
                params:{
                     productid:productid
                }
            },this);
            this.comboLoaded = false;
            this.createdBOMStore.on("load", function() {
                this.createdBOMStore.each(function(rec) {
                    if (!this.comboLoaded) {
                        this.bomCombo.store.add(rec);
                    }
                    if (rec != undefined && rec != null && rec.data['isdefaultbom']) {
                        this.submitBttn.disable();
                        this.editBttn.enable();
                        this.newBttn.enable();
                        this.bomCreationForm.getForm().loadRecord(rec);
                        // remove all details in grid
//                        this.AssemblyGrid.itemsgrid.getStore().removeAll();
                        // load bom details in bom grid
//                        this.loadSavedBOMAssemblyDetails(rec);
                        // for updating subtotal of bom grid
                       
                        for (var i = 0; i < this.createdBOMStore.getCount(); i++) {
                            if (this.createdBOMStore.getAt(i).data['isdefaultbom'] == true) {
                                this.createdBOMGrid.getSelectionModel().selectRow(i);
                                break;
                            }
                        }
                        this.AssemblyGrid.updateSubtotal();
                        this.AssemblyGrid.updateCostinAssemblyGrid();
                        if (!this.comboLoaded) {
                            this.bomCombo.setValue(rec.data['bomCode']);
                            this.loadBOMDetails(rec.data['bomid']);
                        }
                    }
                }, this);
                this.comboLoaded = true;
            }, this);
        }
        this.sm = new Wtf.grid.RowSelectionModel({
            singleSelect: true
        });
        this.cm = new Wtf.grid.ColumnModel([new Wtf.grid.RowNumberer(),
            {
                header: WtfGlobal.getLocaleText('acc.field.bomCode'), // "BOM Code",
                dataIndex: 'bomCode',
                width: 150
            },{
                header: WtfGlobal.getLocaleText('acc.field.bomName'), // "BOM Name",
                dataIndex: 'bomName',
                width: 150
            },{
                header: WtfGlobal.getLocaleText('acc.mrp.field.isdefaultbom'), // "Work Center",
                dataIndex: 'isdefaultbom',
                width: 150
            },{
                header: WtfGlobal.getLocaleText('acc.field.linkProductName'), // "Link Product Name",
                dataIndex: 'linkProductName',
                width: 150,
                hidden:true
            },{
                header: WtfGlobal.getLocaleText('acc.field.workCenter'), // "Work Center",
                dataIndex: 'workCenter',
                width: 150,
                hidden:true
            },{
                header: WtfGlobal.getLocaleText('acc.field.linkAlternateBOM'), // "Link Alternate BOM",
                dataIndex: 'linkAlternateBOMName',
                width: 150,
                hidden:true
            },{
                header: WtfGlobal.getLocaleText('acc.field.alternateBOMCode'), // "Alternate BOM Code",
                dataIndex: 'alternateBOMCode',
                width: 150,
                hidden:true
            },{
                header: WtfGlobal.getLocaleText('acc.field.subBOMCode'), // "Sub BOM Code",
                dataIndex: 'subBOMCode',
                width: 150,
                hidden:true
            },{
                header: WtfGlobal.getLocaleText("acc.product.gridAction"), // "Action",
                align: 'center',
                renderer: this.deleteRenderer.createDelegate(this)    
            }
        ]);
        
        this.createdBOMGrid = new Wtf.grid.GridPanel({
            id: 'createdBOMGrid'+this.id,
            store: this.createdBOMStore,
            hidden: !Wtf.account.companyAccountPref.activateMRPManagementFlag,
            cm: this.cm,
            border: false,
            sm: this.sm,
            trackMouseOver: true,
            loadMask: {
                msg: WtfGlobal.getLocaleText("acc.msgbox.50")
            },
            height: 250,
            style: 'margin-left:10px; margin-right:10px',
            viewConfig: {
                forceFit: false,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            },
            tbar: this.createdBOMGridTbar
        });
        
        this.createdBOMGrid.on('render', function() {
            this.createdBOMGrid.view.refresh.defer(1, this.createdBOMGrid.view); /* Refresh the empty text of grid without load the store */
        },this);
        
        this.createdBOMGrid.on("rowclick", this.rowClickHandle, this);
    },
    
    // create tbar for menu options of bom saved
    createTbarForGridOfBOMSaved: function() {
        this.createdBOMGridTbar = [];
        this.newBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.New"), // "New",
            iconCls: getButtonIconCls(Wtf.etype.add),
            tooltip: WtfGlobal.getLocaleText("acc.field.newBOMToolTip"),
            id: 'BtnNew1' + this.id
        });
        this.newBttn.on('click',this.newBOM,this);
        this.createdBOMGridTbar.push(this.newBttn);
        
        this.editBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.currency.update"), // "Update",
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            tooltip: WtfGlobal.getLocaleText("acc.field.updateBOMToolTip"),
            scope: this,
            disabled: true
        });
        this.editBttn.on('click',this.editData,this);
        this.createdBOMGridTbar.push(this.editBttn);
        
        this.submitBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.submit"), // "Submit",
            iconCls: getButtonIconCls(Wtf.etype.save),
            tooltip: WtfGlobal.getLocaleText("acc.field.submitBOMToolTip"),
            id: 'BtnSubNew' + this.id,
            scope: this
        });
        this.submitBttn.on('click',this.submitData,this);
        this.createdBOMGridTbar.push(this.submitBttn);
    },
    
    // for creating field set of bom details
    createBOMFieldSet: function() {
        this.bomFieldset = new Wtf.form.FieldSet({
            xtype: 'fieldset',
            style: 'margin-top: 10px',
            cls: "visibleDisabled",
            title: WtfGlobal.getLocaleText("acc.product.gridBillofMaterials"), // "Bill of Material",
            layout: 'border',
            height: Wtf.account.companyAccountPref.activateMRPManagementFlag ? 550 : 250,
            border: false,
            items: [this.westPanel,
                {
                    region: 'center',
                    cls: "visibleDisabled",
                    border: false,
                    items: [this.bomCreationForm,
                        this.AssemblyGrid,
                        this.createdBOMGrid]
                }]
        });
    },
    
    // grid row click handler
    rowClickHandle: function(grid,rowindex,e) {
        if (e.getTarget(".delete-gridrow")) {
            if (this.isClone != undefined && this.isClone) {  //ERP-36779-In copy case, if user want to delete BOM then it should be possible for user.
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.setupWizard.note27"), function (btn) {
                    if (btn == "yes") {
                        var store = grid.getStore();
                        var rec = store.getAt(rowindex);
                        store.remove(rec);
                        grid.getView().refresh();
                        this.newBOM();	//ERP-36779 : if row is deleted then reset the form for new case.
                    }
                }, this);
            } else if(this.isUsedInTransaction && this.producttypeval==Wtf.producttype.assembly && this.isEdit && !this.isClone){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Product is used in Transaction.So you can not edit/delete this record"],2);
            }else{
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.setupWizard.note27"), function(btn) {
                if (btn == "yes") {
                    var store = grid.getStore();
                    var rec  = store.getAt(rowindex);
                    if (rec.data.bomid) {
                        Wtf.Ajax.requestEx({
                            url: "ACCProduct/deleteBOMByID.do",
                            params: {
                                bomid: rec.data.bomid
                            }
                        }, this, function(response) {
                            var msg = "";
                            if (response.success) {
                                store.remove(rec);
                                grid.getView().refresh();
                                this.newBOM();
                            } else {
                                msg = WtfGlobal.getLocaleText("acc.common.msg1"); // Failed to make connection with web server
                                if (response.msg) {
                                    msg = response.msg;
                                }
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.product.gridBillofMaterials"),msg],1);
                            }
                            
                        }, function(response) {
                            var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
                            if(response.msg ) {
                                msg=response.msg;
                            }
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
                        });
                    } else {
                        store.remove(rec);
                        grid.getView().refresh();

                        // if row is deleted then reset the form for new case
                        this.newBOM();
                    }
                }
            }, this);
        }
    } else {
        this.submitBttn.disable();
        this.editBttn.enable();
        this.newBttn.enable();
        if(this.isUsedInTransaction && this.producttypeval==Wtf.producttype.assembly && this.isEdit && !this.isClone){
              this.bomCreationForm.disable();
        }

        var rec = this.createdBOMGrid.getSelectionModel().getSelected();
        if (rec != undefined && rec != null) {
            this.bomCreationForm.getForm().loadRecord(rec);
            //                this.linkProductCombo.setValForRemoteStore(rec.data.linkProductId, rec.data.linkProductName);
            //                this.linkAlternateBOM.setValForRemoteStore(rec.data.linkAlternateBOMId, rec.data.linkAlternateBOMName);
            //                if (rec.data.subBOMCode && this.subBOM.collapsed == true) {
            //                    this.subBOM.toggleCollapse();
            //                }
            // remove all details in grid
            this.AssemblyGrid.itemsgrid.getStore().removeAll();
            rec.data.isNewBOM=false;
            // load bom details in bom grid
            this.loadSavedBOMAssemblyDetails(rec);
            // for updating subtotal of bom grid
            this.AssemblyGrid.updateSubtotal();
        } else {
            // if row is not selected then reset the form for new case
            this.newBOM();
        }
    }
},
    
// new button handler
newBOM: function() {
    this.createdBOMGrid.getSelectionModel().clearSelections();
    this.bomCreationForm.enable();
    if(this.isEdit && this.producttypeval==Wtf.producttype.assembly && Wtf.account.companyAccountPref.activateMRPManagementFlag){
        this.bomCode.allowBlank=false;
    }
    this.submitBttn.enable();
    this.editBttn.disable();
    this.AssemblyGrid.itemsgrid.getStore().removeAll();
    this.AssemblyGrid.addBlankRecord(true);
    this.bomCreationForm.getForm().reset();
    // for updating subtotal of bom grid
    this.AssemblyGrid.updateSubtotal();
},
    
// edit button handler
editData: function() {
    if (this.bomCreationForm.getForm().isValid()) {
        if (this.createdBOMGrid.getSelectionModel().hasSelection()) {
            var createdBOMGridRec = this.createdBOMGrid.getSelectionModel().getSelected();
            // do not update if bom code is already submitted
            var bomCodeVal = this.bomCode.getValue();
            var recordIndex = this.createdBOMStore.findBy(
                function(record, id) {
                    if (record.get('bomCode').toUpperCase() === bomCodeVal.toUpperCase()) {
                        return true;
                    }
                    return false;
                }
                );
            if (recordIndex != -1) {
                var rec = this.createdBOMStore.getAt(recordIndex);
                if ((rec != null && rec != undefined) && (createdBOMGridRec != undefined && createdBOMGridRec != null && createdBOMGridRec.data.bomCode.toUpperCase() != this.bomCode.getValue().toUpperCase())) {
                    WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.alert'), "BOM Code <b>'" + this.bomCode.getValue() + "'</b> is already exists."], 2);
                    return;
                }
            }

            // do not update if bom details are not added
            var asemblyJson = this.AssemblyGrid.getAssemblyJson();
            if (asemblyJson.trim() == "") {
                WtfComMsgBox(40, 2);
                return;
            }

            // for start updating record in grid
            createdBOMGridRec.beginEdit();

            var bomCreationFormRec = this.bomCreationForm.getForm().getValues();
            createdBOMGridRec.set("bomCode", bomCreationFormRec.bomCode);
            createdBOMGridRec.set("bomName", bomCreationFormRec.bomName);
            createdBOMGridRec.set("isdefaultbom", (bomCreationFormRec.isdefaultbom == "on" || this.createdBOMGrid.getStore().getCount() < 1) ? true : false);
            //            createdBOMGridRec.set("workCenter", bomCreationFormRec.workCenter);
            //            createdBOMGridRec.set("alternateBOMCode", bomCreationFormRec.alternateBOMCode);
            //            createdBOMGridRec.set("subBOMCode", bomCreationFormRec.subBOMCode);

            //            var linkProductRec = this.linkProductStore.getAt(this.linkProductStore.find('productid', this.linkProductCombo.getValue()));
            //            var linkAlternateBOMRec = this.substituteProductStore.getAt(this.substituteProductStore.find('productid', this.linkAlternateBOM.getValue()));
            //            createdBOMGridRec.set("linkProductId", (linkProductRec != undefined && linkProductRec != null) ? linkProductRec.data.productid : "");
            //            createdBOMGridRec.set("linkProductName", (linkProductRec != undefined && linkProductRec != null) ? linkProductRec.data.productname : "");
            //            createdBOMGridRec.set("linkAlternateBOMId", (linkAlternateBOMRec != undefined && linkAlternateBOMRec != null) ? linkAlternateBOMRec.data.productid : "");
            //            createdBOMGridRec.set("linkAlternateBOMName", (linkAlternateBOMRec != undefined && linkAlternateBOMRec != null) ? linkAlternateBOMRec.data.productname : "");

                createdBOMGridRec.set("bomAssemblyDetails", this.getBOMAssemblyDetails());

                // complete updation process
                createdBOMGridRec.endEdit();
                createdBOMGridRec.commit();
                this.createdBOMGrid.getView().refresh();
                
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.field.BillOfMaterialHasBeenUpdatedSuccessfully")], 0);
            }
        } else {
            WtfComMsgBox(2, 2);
        }
    },
    
    // submit button handler
    submitData: function() {
        if (this.bomCreationForm.getForm().isValid()) {
            // do not submit if bom code is already submitted
            var bomCodeVal = this.bomCode.getValue();
            var recordIndex = this.createdBOMStore.findBy(
                function (record, id) {
                    if (record.get('bomCode').toUpperCase() == bomCodeVal.toUpperCase()) {
                        return true;
                    }
                    return false;
                }
            );
            if (recordIndex != -1) {
                var rec = this.createdBOMStore.getAt(recordIndex);
                if (rec != null && rec != undefined) {
                    WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.alert'), "BOM Code <b>'" + this.bomCode.getValue() + "'</b> is already exists."],2);
                    return;
                }
            }
            
            // do not submit if bom details are not added
            var asemblyJson = this.AssemblyGrid.getAssemblyJson();
            if (asemblyJson.trim() == "") {
                WtfComMsgBox(40,2);
                return;
            }
            
//            var linkProductRec = this.linkProductStore.getAt(this.linkProductStore.find('productid', this.linkProductCombo.getValue()));
//            var linkAlternateBOMRec = this.substituteProductStore.getAt(this.substituteProductStore.find('productid', this.linkAlternateBOM.getValue()));
//            
            // insert record in bom created grid
            rec = this.bomCreationForm.getForm().getValues();
            var record = new this.gridRecord({
                bomCode: rec.bomCode,
                bomName: rec.bomName,
                productid: (this.record != undefined && this.record.data != undefined ? this.record.data.productid : ''),
//                linkProductId: (linkProductRec != undefined && linkProductRec != null) ? linkProductRec.data.productid : "",
//                linkProductName: (linkProductRec != undefined && linkProductRec != null) ? linkProductRec.data.productname : "",
//                linkAlternateBOMId: (linkAlternateBOMRec != undefined && linkAlternateBOMRec != null) ? linkAlternateBOMRec.data.productid : "",
//                linkAlternateBOMName: (linkAlternateBOMRec != undefined && linkAlternateBOMRec != null) ? linkAlternateBOMRec.data.productname : "",
//                subBOMCode: rec.subBOMCode,
//                workCenter: rec.workCenter,
                isdefaultbom: (rec.isdefaultbom == "on" || this.createdBOMGrid.getStore().getCount() < 1) ? true : false,
//                alternateBOMCode: rec.alternateBOMCode,
                bomAssemblyDetails: this.getBOMAssemblyDetails()
            });
            this.createdBOMStore.add(record);
            this.bomStore.add(record);
//            this.submitBttn.disable();
            this.AssemblyGrid.itemsgrid.getStore().removeAll();
            this.AssemblyGrid.addBlankRecord(true);
            this.bomCreationForm.getForm().reset();
            // for updating subtotal of bom grid
            this.AssemblyGrid.updateSubtotal();
        } else {
            WtfComMsgBox(2, 2);
        }
    },
    
    // for getting bom details for submit
    getBOMAssemblyDetails: function() {
        var arr = [];
        var includeLast = true;
        this.AssemblyGrid.gridStore.each(function(rec) {
            arr.push(this.AssemblyGrid.gridStore.indexOf(rec));
        }, this);
        var jarray = WtfGlobal.getJSONArray(this.AssemblyGrid.itemsgrid, includeLast, arr);
        return jarray;
    },
    // for getting bom details JSON for submit
    getBOMAssemblyDetailsJSON: function() {
        var jsonstring = "";
        
    this.createdBOMGrid.getStore().each(function(rec) {
        var bomid = "";
        if (rec.data['bomid']) {
            bomid = rec.data['bomid'];
        } else {
            bomid = "";
        }
        jsonstring += "{bomid:\"" + bomid + "\"," + "bomCode:\"" + rec.data['bomCode'] + "\"," +
        "bomName:\"" + rec.data['bomName'] + "\"," + "isdefaultbom:" + rec.data['isdefaultbom'] + "," + "bomAssemblyDetails:" + rec.data['bomAssemblyDetails'] + "},";
        }, this);
        jsonstring = jsonstring.substr(0, jsonstring.length - 1);
        return jsonstring;
    },
    
    // load bom details in bom grid
    loadSavedBOMAssemblyDetails: function(record) {
        var bomDetailRecords = "";
        if (record.data.bomAssemblyDetails != undefined && record.data.bomAssemblyDetails.length>1) {
            bomDetailRecords = eval('(' + record.data.bomAssemblyDetails + ')');
        }
        var recordQuantity = bomDetailRecords.length;
        
        if (recordQuantity != 0) {
            for (var i=0; i<recordQuantity; i++) {
                var bomDetailRecord = bomDetailRecords[i];
                if (bomDetailRecord["productid"]) {
                    var rec = new this.AssemblyGrid.gridRec(bomDetailRecord);
                    rec.beginEdit();
                    var fields = this.AssemblyGrid.gridStore.fields;
                    for (var x=0; x<fields.items.length; x++) {
                        var value = bomDetailRecord[fields.get(x).name];
                        if (fields.get(x).name == 'type' && value && value != '') {
                            value = decodeURI(value);
                        }
                        if (fields.get(x).name == 'productname' && value && value != '') {
                            value = decodeURI(value);
                        }
                        if (fields.get(x).name == 'desc' && value && value != '') {
                            value = decodeURI(value);
                        }
                        /* If field is total and value contain undefined or empty then value is set to zero. */
                        if (fields.get(x).name == 'total' && (value==undefined || value == '')) {
                            value = 0;
                        }
                        rec.set(fields.get(x).name, value);
                    }

                    rec.endEdit();
                    rec.commit();
                    this.AssemblyGrid.gridStore.add(rec);
                }
            }
        }
//        if (this.isEdit) {  // should happen while editing. otherwise it adds an extra blank record
            this.AssemblyGrid.addBlankRecord(record.data.isNewBOM); // to add blank record at end of the grid
//        }
    },
    createWestTreePanel: function() {

        this.bomStore = new Wtf.data.SimpleStore({
            fields: ['bomCode', 'bomName', "isdefaultbom", "bomAssemblyDetails"],
            data: []
        });
        this.bomCombo = new Wtf.form.ComboBox({
            triggerAction: 'all',
            mode: 'local',
            valueField: 'bomCode',
            displayField: 'bomName',
            store: this.bomStore,
            fieldLabel: "Select BOM",
            toolTip: "Select BOM",
            emptyText: "Select BOM",
            hidden: !(Wtf.account.companyAccountPref.activateMRPManagementFlag && this.isEdit && !this.isClone),
            hidden: !(Wtf.account.companyAccountPref.activateMRPManagementFlag && this.isEdit && !this.isClone),
            width: 120,
            typeAhead: true,
            forceSelection: true
        });
        
        this.bomCombo.on("change", function(combo, newvalue, oldvalue) {
            var recordIndex = combo.store.findBy(
                function (record, id) {
                    if (record.get('bomCode') === combo.value) {
                        return true;
                    }
                    return false;
                }
            );
            if (recordIndex != -1) {
                var rec = combo.store.getAt(recordIndex);
                this.loadBOMDetails(rec.data.bomid);
            }
        }, this);
        this.westBOMTab = new Wtf.tree.TreePanel({
            hidden: !(Wtf.account.companyAccountPref.activateMRPManagementFlag && this.isEdit),
//            enableDD: true,
            border: false,
            autoScroll:true,
             height: 680,
            containerScroll: true,
            dropConfig: {appendOnly: true}
        });
        this.rootNode = new Wtf.tree.TreeNode({
            text: (this.record != undefined && this.record.data != undefined ? this.record.data.pid : ''),
            id: (this.record != undefined && this.record.data != undefined ? this.record.data.productid : 'root'),
            productType: (this.record != undefined && this.record.data != undefined ? this.record.data.type : ""),
            draggable: false, // disable root node dragging
            productid: (this.record != undefined && this.record.data != undefined ? this.record.data.productid : ''),
            expanded: true,
            listeners: {
                scope: this,
                click: this.showDetails.createDelegate(this)
            }
        });
        this.westBOMTab.setRootNode(this.rootNode);
        this.westPanel = new Wtf.form.FormPanel({
            region: 'west',
            border: false,
//            autoScroll:true,
            width: 240,
            height: 470,
            hidden: !(Wtf.account.companyAccountPref.activateMRPManagementFlag && this.isEdit && !this.isClone),
            items: [this.bomCombo, this.westBOMTab]
        });

    },
    loadBOMDetails: function(bomid) {
        if ((this.record != undefined && this.record.data != undefined ? this.record.data.productname : '') != "" && Wtf.account.companyAccountPref.activateMRPManagementFlag) {
            var params={
                productid:this.record.data.productid
            };
            if (bomid != undefined && bomid != "") {
                params.bomid = bomid;
            }
            Wtf.Ajax.requestEx({
                url: "ACCProductCMN/getProductRecipes.do",
                params: params
            }, this, function(response) {
                if (response != undefined && response.data != undefined) {
                    this.rootNode.eachChild(function(child) {//remove each child from root node
                        this.rootNode.removeChild(child);
                    }, this);
                    for (var i = 0; i < response.data.length; i++) {
                        var record = response.data[i];
                        var treeNode = new Wtf.tree.TreeNode({
                            text: record.text,
                            id: (record.id != undefined && record.id != "") ? record.id : record.productid,
                            cls: 'paddingclass',
                            leaf: false,
                            bomid: record.bomid,
                            expanded: true,
                            productType: record.producttype,
                            productid: record.productid,
                            listeners: {
                                scope: this,
                                click: this.showDetails.createDelegate(this)
                            }
                        });
                        if (record.parentid && this.westBOMTab.getNodeById(record.parentid) != undefined) {
                            this.westBOMTab.getNodeById(record.parentid).appendChild(treeNode);
                        } else {
                            this.rootNode.appendChild(treeNode);
                        }
                    }
                    this.rootNode.expand();
                }
            }, function(response) {

            });
        }
    },
    showDetails: function(node,event){
        if (node != undefined && node.attributes != undefined && node.attributes.productType == "Inventory Assembly") {
            this.AssemblyGrid.setProductWithDefaultBOM(node.attributes.productid);
            this.createdBOMStore.load({
                params: {
                    productid:node.attributes.productid
                }
            }, this);
            if (node.attributes.productid !== (this.record != undefined && this.record.data != undefined ? this.record.data.productid : '')) {
                this.AssemblyGrid.disable();
                this.bomCreationForm.disable();
            } else {
                this.AssemblyGrid.enable();
                this.bomCreationForm.enable();
            }
        } else {
            return;
        }
    },
    setDefaultPerpetualAccount: function () {
        if (Wtf.account.companyAccountPref.cogsAcc != undefined && Wtf.account.companyAccountPref.cogsAcc != null && Wtf.account.companyAccountPref.cogsAcc !== "")
        {
            this.cogsAcc.setValue(Wtf.account.companyAccountPref.cogsAcc);
        }
        if (Wtf.account.companyAccountPref.stockAdjustmentAcc != undefined && Wtf.account.companyAccountPref.stockAdjustmentAcc != null && Wtf.account.companyAccountPref.stockAdjustmentAcc !== "")
        {
            this.stockAdjustmentAcc.setValue(Wtf.account.companyAccountPref.stockAdjustmentAcc);
        }
        if (Wtf.account.companyAccountPref.inventoryAcc != undefined && Wtf.account.companyAccountPref.inventoryAcc != null && Wtf.account.companyAccountPref.inventoryAcc !== "")
        {
            this.inventoryAcc.setValue(Wtf.account.companyAccountPref.inventoryAcc);
        }
    },
    doNotAllowToSelectNAUom: function (selectedRecord) {
        if (this.producttype.getValue() != Wtf.producttype.service && selectedRecord != undefined && selectedRecord.data.uomname == "N/A") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.stockuom.selectuomerrormsg")], 2);
            return false;
        }
    }
    
/* Product Type Master
+--------------------------------------+--------------------+
| id                                   | name               |
+--------------------------------------+--------------------+
| e4611696-515c-102d-8de6-001cc0794cfa | Inventory Assembly |
| d8a50d12-515c-102d-8de6-001cc0794cfa | Inventory Part     |
| f071cf84-515c-102d-8de6-001cc0794cfa | Non-Inventory Part |
| 4efb0286-5627-102d-8de6-001cc0794cfa | Service            |
+--------------------------------------+--------------------+
 * */
});
