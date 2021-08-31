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
Wtf.account.ProductForm = function(config){
    this.editQuantity=false;						// used as flag to save initial quantity while product editing
    this.isEdit=config.isEdit;
    this.isClone=config.isClone;
    this.isFixedAsset = (config.isFixedAsset != undefined)?config.isFixedAsset:false;
    this.isDepreciable = true;
    this.isBatchForProd=false;
    this.isSerialForProd=false;
    this.productID=config.productID;
    this.isUsedInTransaction=config.isUsedInTransaction;
    this.batchDetails="";
    this.fireEventCounter=0;
    var recordid="";
    this.modeName = config.modeName;
    Wtf.apply(this,config);
    Wtf.account.ProductForm.superclass.constructor.call(this, config);
    
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

Wtf.extend(Wtf.account.ProductForm, Wtf.account.ClosablePanel, {
    loadRecord:function(){
        if(this.record!=null){
            if(this.record.data['parentid']){
                this.subproduct.toggleCollapse();
            }
//            this.ProductForm.getForm().loadRecord(this.record);
                if(!this.isClone){
	            	this.Pname.setValue(this.record.data.productname);
	                this.PID.setValue(this.record.data.pid);
                        this.recordid=this.record.data.productid;
                        Wtf.apply(this.detailPanel, {accid:this.recordid});
                        Wtf.apply(this.detailPanel, {acccode:this.PID});    
                        this.description.setValue(this.record.data.desc);
                        var prodDesc=this.record.data.desc;
//                        prodDesc = prodDesc.replace(/(<([^>]+)>)/ig,"");
                        this.descriptionshow.setValue(prodDesc);
                        this.Supplier.setValue(this.record.json.supplier);
                        this.CoilCraft.setValue(this.record.json.coilcraft);
                        this.InterPlant.setValue(this.record.json.interplant);
                }else{//clone case
                    this.PID.setValue("");
                }
                
                if(Wtf.uomStore.getCount()>1){
                    this.uom.setValue(this.record.data.uomid);
                }
                
                if(Wtf.vendorAccStore.getCount()>1){
                    this.vendor.setValue(this.record.data.vendor);
                }
                if(Wtf.salesAccStore.getCount()>1){
                    this.salesAcc.setValue(this.record.data.salesaccountid);
                    this.salesReturnAcc.setValue(this.record.data.salesretaccountid);
                    this.salesRevenueRecognitionAccount.setValue(this.record.data.salesRevenueRecognitionAccountid);
                } 
                if(this.locationStore.getCount()>0){
                    this.locationEditor.setValue(this.record.data.location);
                }
                if(this.wareHouseStore.getCount()>0){
                    this.wareHouseEditor.setValue(this.record.data.warehouse);
                }
                if(this.shelfLocationStore.getCount()>0){
                    this.shelfLocationCombo.setValue(this.record.data.shelfLocationId);
                }
                if(this.globalProductCount>0){
                    this.parentname.setValue(this.record.data.parentuuid);
                }
                this.reorderLevel.setValue(this.record.data.reorderlevel);
                this.rQuantity.setValue(this.record.data.reorderquantity);
                this.leadtime.setValue(this.record.data.leadtime);
                if(this.record.data.warrantyperiodsal != 0){
                    this.warrantyperiodSal.setValue(this.record.data.warrantyperiodsal);
                }
                if(this.record.data.warrantyperiod != 0){
                    this.warrantyperiod.setValue(this.record.data.warrantyperiod);
                }
                if(this.record.data.revenueRecognitionProcess!=null){
                    this.revenueRecognitionProcess.setValue(this.record.data.revenueRecognitionProcess);
                }
                this.cCountInterval.setValue(this.record.data.ccountinterval);
                this.cCountTolerance.setValue(this.record.data.ccounttolerance);
                this.productWeight.setValue(this.record.data.productweight);
                this.quantity.setValue(this.record.data.initialquantity);    
                this.initialprice.setValue(this.record.data.initialprice);
                this.initialsalesprice.setValue(this.record.data.saleprice);
                this.syncable.setValue(this.record.data.syncable);
                this.isRecylable.setValue(this.record.data.isRecyclable);
                this.batchDetails=this.record.data.batchdetails;
                this.multiuom.setValue(this.record.data.multiuom);
                this.autoAssembly.setValue(this.record.data.autoAssembly);
                this.isLocationForProduct.setValue(this.record.data.isLocationForProduct);
                this.isWarehouseForProduct.setValue(this.record.data.isWarehouseForProduct);
                this.isRowForProduct.setValue(this.record.data.isRowForProduct);
                this.isRackForProduct.setValue(this.record.data.isRackForProduct);
                this.isBinForProduct.setValue(this.record.data.isBinForProduct);
                this.isBatchForProduct.setValue(this.record.data.isBatchForProduct);
                this.isSerialForProduct.setValue(this.record.data.isSerialForProduct);
                this.qaenable.setValue(this.record.data.qaenable);
		this.isInterval.setValue(this.record.data.intervalfield);
                this.addShipLengthqty.setValue(this.record.data.addshiplentheithqty);
                this.noOfServiceQTY.setValue(this.record.data.noofqty);
                this.minOrderingQuantity.setValue(this.record.data.minorderingquantity);
                this.maxOrderingQuantity.setValue(this.record.data.maxorderingquantity);
                this.landingCostCategoryCombo.setValue(this.record.data.landingcostcategoryid);
                this.addQuantityFields();
                if(this.record.data.barcodefield==Wtf.BarcodeGenerator_SerialId){
                    Wtf.getCmp('bserialId'+this.id).setValue(true);
                }else if(this.record.data.barcodefield==Wtf.BarcodeGenerator_ProductId){
                    Wtf.getCmp('bprodId'+this.id).setValue(true);
                }else if(this.record.data.barcodefield==Wtf.BarcodeGenerator_SKUField){
                    Wtf.getCmp('bSkuId'+this.id).setValue(true);
                }
                
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
        
        /*
         * ERM-1012
         * Set value to field  only if country is India and flag for ITC functionality is set.
         */
        if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isitcapplicable && this.record) {
            this.itcTypeCmbForAsset.setValue(this.record.data.itctype);
        }
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
                this.writeOffAssetAccount.disable();
                this.PNLTypeAccount.disable();
                this.balanceSheetTypeAccount.disable();
                this.assetControllingAcc.disable();
                this.itcTypeCmbForAsset.disable();
                this.warrantyperiod.disable();
                this.warrantyperiodSal.disable();
                this.syncable.disable();
                this.isLocationForProduct.disable();
                this.isWarehouseForProduct.disable();
                this.isRowForProduct.disable();
                this.isRackForProduct.disable();
                this.isBinForProduct.disable();
                this.isBatchForProduct.disable();
                this.isSerialForProduct.disable();
            }else{
                this.depreciationMethodCombo.enable();
                this.depreciationRate.enable();
                this.saleAssetAccount.enable();
                this.writeOffAssetAccount.enable();
                this.PNLTypeAccount.enable();
                this.balanceSheetTypeAccount.enable();
                this.assetControllingAcc.enable();
                this.itcTypeCmbForAsset.enable();
            }
        },function(response){
                    
            });
    },
    
   onRender: function(config){
       Wtf.account.ProductForm.superclass.onRender.call(this, config);
         this.isClosable=false; 
         this.producttypeval = (this.record!=null?this.record.data.producttype:Wtf.producttype.invpart);
         this.globalProductCount = Wtf.productStore.getCount();
         this.createStore();
         this.createFields();
         this.createToggleFields();
         this.createAssemblyGrid();
         this.createForm();
          this.newPanel=new Wtf.Panel({
          autoScroll:true,
          bodyStyle:' background: none repeat scroll 0 0 #DFE8F6;',
          region : 'center',
            autoScroll:true,
            items:[ this.ProductForm,{
                   region: 'center',
                   border: false,
                    layout:"fit",
                items:this.AssemblyGrid
            }] ,
            bbar:[{
                text:WtfGlobal.getLocaleText("acc.common.saveBtn"),
                scope:this,
                iconCls :getButtonIconCls(Wtf.etype.save),
                handler: this.saveForm.createDelegate(this)
//            },{
//                 text: 'Cancel',
//                scope: this,
//                iconCls :getButtonIconCls(Wtf.etype.close),
//                handler:this.closeForm.createDelegate(this)
            },this.gstHistory]
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
    },
    createStore:function(){
        this.shelfLocationStoreRec = new Wtf.data.Record.create([
            {name: 'shelfLocationId'},
            {name: 'shelfLocationValue'}
        ]);
        
        this.shelfLocationStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.shelfLocationStoreRec),
            url : "ACCProduct/getshelfLocations.do"
        });
        this.shelfLocationStore.load();
        
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
            {name: 'hasAccess'}
       ]);
    this.purchaseAccStore=new Wtf.data.Store({
             url:"ACCAccountCMN/getAccountsIdNameForCombo.do",
            baseParams:{
                 mode:2,
                 ignoreCashAccounts:true,
                 ignoreBankAccounts:true,
                 ignoreGSTAccounts:true,  
                 ignorecustomers:true,  
                 ignorevendors:true,
                 nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.purchaseAccRec)
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
            url:"ACCMaster/getLocationItems.do",
            baseParams:{isActive:true}, //ERP-40021 :To get only active Locations.  
            reader:this.locationReader
        });
       
       
        this.wareHouseRec = new Wtf.data.Record.create([
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
            url:"ACCMaster/getWarehouseItems.do",
            reader:this.wareHouseReader
        });
       
        this.wareHouseStore.load();
        this.locationStore.load();
        
        this.FixedAssetControllingAccStore = new Wtf.data.Store({
//            url:"ACCAccountCMN/getAccountsForCombo.do",
//            url:"ACCAccountCMN/getAccountsForComboOptimized.do",
              url:"ACCAccountCMN/getAccountsIdNameForCombo.do",
            baseParams:{
                mode:2,
                ignorecustomers:true,  
                ignorevendors:true,
                onlyBalancesheet:true,
                ignoreGSTAccounts:true,
                nature:[0,1],
                nondeleted:true
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
            url:"ACCProduct/getProducts.do",
            baseParams:{
                mode:22,
                productid:(this.record!=null?this.record.data['productid']:null)
            }
        });
        
        if(!this.isEdit || this.isClone){
        	Wtf.Ajax.requestEx({
        		url:"ACCProduct/getProductIDAutoNumber.do",
        		params: {dummyParam:true}
        	},this,this.getIdSuccessResponse);
        }
        
        if(this.isEdit && (this.record.data['producttype'] != Wtf.producttype.assembly || this.record.data['producttype'] != Wtf.producttype.customerAssembly) && !this.isClone){
        	var productid = {productid:this.record.data['productid']};
        	Wtf.Ajax.requestEx({
        		url:"ACCProductCMN/editQuantity.do",
        		params: productid
        	},this,this.editQuantitySuccessResponse,this.editQuantityFailureResponse);
        } 

        if(this.globalProductCount>0){
            this.cloneProductList();
        }
        chkProductTypeload();
        this.loadProductStore();
        chkproductload();
        chkUomload();
        chksalesAccountload();
        chkvenaccload();
        
        Wtf.productTypeStore.on("load", this.setProductType, this);
        Wtf.uomStore.on("load", this.setUoM, this);
        Wtf.vendorAccStore.on("load",this.setVendor,this);
        Wtf.salesAccStore.on("load", this.setSalesAccount, this);
        Wtf.productStore.on("load", this.cloneProductList, this);

//        this.parentStore.on("load",function(){
//            if(this.record!=null){
//                this.parentname.setValue(this.record.data.parentid);
//            }
//        },this);

        this.purchaseAccStore.on("load",function(){
            if(this.record!=null){							// Neeraj    Check if the id of record is present in the type of Account
                if(this.purchaseAccStore.find('accid',this.record.data.purchaseaccountid) != -1){
                	this.purchaseAcc.setValue(this.record.data.purchaseaccountid);
                }
                if(this.purchaseAccStore.find('accid',this.record.data.purchaseretaccountid) != -1){
                	this.purchaseReturnAcc.setValue(this.record.data.purchaseretaccountid);
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
            this.loadRecord();
            Wtf.MessageBox.hide();
            this.Pname.focus();
        },this);
        this.locationStore.on("loadexception",function(){
            this.loadRecord();
            Wtf.MessageBox.hide();
            this.Pname.focus()
        },this);
        
        this.wareHouseStore.on("load",function(){
            if(this.record!=null){							
                if(this.wareHouseStore.find('id',this.record.data.warehouse) != -1){
                    this.wareHouseEditor.setValue(this.record.data.warehouse);
                }
                
            }
            this.loadRecord();
            Wtf.MessageBox.hide();
            this.Pname.focus();
        },this);
        this.wareHouseStore.on("loadexception",function(){
            this.loadRecord();
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
		if(!this.editQuantity){
    		this.quantity.disable();
		}else{
			this.quantity.enable();
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
                    this.sequenceFormatCombobox.disable();
                    if(this.PID!=undefined){
                         if(!this.isEdit){
                            this.PID.enable();
                        }
                    }
                    if(this.isClone){//for clone or copy NA case show no field
                        this.getNextSequenceNumber(this.sequenceFormatCombobox);
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
      getPostTextEditor: function(e)
{
        var _tw=new Wtf.EditorWindowQuotation({
            val:this.description.getValue()
        });
        _tw.on("okClicked", function(obj){
            var postText = obj.getEditorVal().textVal;
            var styleExpression  =  new RegExp("<style.*?</style>");
            postText=postText.replace(styleExpression,"");
            this.description.setValue(postText);
//            postText = postText.replace(/(<([^>]+)>)/ig,"");
            postText = postText.replace("&nbsp;"," ");
            this.descriptionshow.setValue(postText);
        }, this);         
        _tw.show();
        
         
        
        
    },
    createFields:function(){
        this.Pname=new Wtf.form.ExtendedTextField({
             //fieldLabel:(this.isFixedAsset)?"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.fixedasset.groupname.tt")+"'>"+'Asset Group Name*' +"</span>":"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.productName.tt")+"'>"+WtfGlobal.getLocaleText("acc.product.productName")+"</span>",//(this.isFixedAsset)?'Asset Group Name*':WtfGlobal.getLocaleText("acc.product.productName"),//'Product Name*',
             fieldLabel:this.isFixedAsset?"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.fixedasset.groupname.tt") +"'>"+  WtfGlobal.getLocaleText('Asset Group Name*')  +"</span>":"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.productName.tt") +"'>"+ WtfGlobal.getLocaleText("acc.product.productName") +"</span>",
             name: 'productname',
             disabled:this.isFixedAsset?false:true,
             allowBlank:false,
             anchor:'85%',
              maskRe: Wtf.productNameCommaMaskRe,
//            regex:/^[\w\s\'\"\.\-]+$/,
 //           regex:/^[^\"]+$/,                              // Removed the % sign for resolving issue ERP-710: Have to remove the restriction in the add/edit product screen.
             invalidText : 'This field should not be blank or should not contain %, ", \\ characters.',
             maxLength:50
        });
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
                    mode:this.modeName
                }
            });
            this.sequenceFormatStore.load();
          this.sequenceFormatStore.on('load',this.setNextNumber,this);
        
       this.sequenceFormatCombobox = new Wtf.form.ComboBox({            
//        labelSeparator:'',
//        labelWidth:0,
        triggerAction:'all',
        mode: 'local',
        fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.Sequenceformat.tip")+"'>"+ WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat")+"</span>",//WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
        valueField:'id',
        displayField:'value',
        store:this.sequenceFormatStore,
        disabled:(this.isEdit && !this.isClone?true:false),  
        anchor:'85%',
        typeAhead: true,
        forceSelection: true,
        name:'sequenceformat',
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
            anchor:'85%',
            allowBlank:false,
            maxLength:50
        });
        this.syncable= new Wtf.form.Checkbox({
            name:'syncable',
            fieldLabel:"<span wtf:qtip='"+((this.isFixedAsset)?WtfGlobal.getLocaleText("acc.fixedasset.makeAvailCRM.tt"):WtfGlobal.getLocaleText("acc.product.makeAvailCRM.tt"))+"'>"+ WtfGlobal.getLocaleText("acc.product.makeAvailCRM")+"</span>",//WtfGlobal.getLocaleText("acc.product.makeAvailCRM"),//'Make available in CRM',
            checked:false,
            itemCls:"chkboxalign"
        });
        this.multiuom= new Wtf.form.Checkbox({
            name:'multiuom',
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.multiuom.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.multiuom")+"</span>",//WtfGlobal.getLocaleText("acc.product.multiuom"),//'Make available in CRM',
            checked:false,
            itemCls:"chkboxalign"
        });
        
        this.autoAssembly = new Wtf.form.Checkbox({
            name:'autoAssembly',
            fieldLabel: WtfGlobal.getLocaleText("acc.field.BuildAssemblyonSale"), // "Auto Build Assembly on Sale",
            checked:false,
            itemCls:"chkboxalign"
        });
        this.autoAssembly.on('check', function() {
            if (this.autoAssembly.getValue()) { // The serial number option will be disable if 'Auto Build Assembly on Sale' functionality is activated. 
                this.isBatchForProduct.reset();
                this.isLocationForProduct.reset();
                this.isWarehouseForProduct.reset();
                this.isSerialForProduct.reset();
                this.isLocationForProduct.disable();
                this.isWarehouseForProduct.disable();
                this.isRowForProduct.disable();
                this.isRackForProduct.disable();
                this.isBinForProduct.disable();
                this.isBatchForProduct.disable();
                this.isSerialForProduct.disable();
            } else if (!(this.isEdit && !this.isClone)) { // enable serial number option for clone and in create new And disabled for edit condition. if 'Auto Build Assembly on Sale' functionality is deactivated. 
                this.isLocationForProduct.enable();
                this.isWarehouseForProduct.enable();
                this.isRowForProduct.enable();
                this.isRackForProduct.enable();
                this.isBinForProduct.enable();
                this.isBatchForProduct.enable();
                this.isSerialForProduct.enable();
            }
        }, this);
        
        this.isRecylable= new Wtf.form.Checkbox({
            name:'recyclable',
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.IsRecyclable.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.IsRecyclable")+"</span>",//WtfGlobal.getLocaleText("acc.product.IsRecyclable"),//"Is Recyclable"
//            disabled:(this.isFixedAsset ||(this.isEdit && !this.isClone))?true:false,
            checked:false,
            itemCls:"chkboxalign"
        });
        this.qaenable= new Wtf.form.Checkbox({
            name:'qaenable',
            fieldLabel:"QA Approval Flow",
            checked:false,
            hidden:true,
            //hidden:!(Wtf.account.companyAccountPref.isQaApprovalFlow),
            hideLabel:!(Wtf.account.companyAccountPref.isQaApprovalFlow),
            itemCls:"chkboxalign"
        });
        this.producttype= new Wtf.form.FnComboBox({
            fieldLabel:"<span wtf:qtip='"+((this.isFixedAsset)?WtfGlobal.getLocaleText("acc.fixedasset.grouptype.tt"):WtfGlobal.getLocaleText("acc.product.productType.tt"))+"'>"+( (this.isFixedAsset)?'Asset Group Type':WtfGlobal.getLocaleText("acc.product.productType"))+"</span>",//(this.isFixedAsset)?'Asset Group Type':WtfGlobal.getLocaleText("acc.product.productType"),//'Product Type*',
            hiddenName:'producttype',
            store:Wtf.productTypeStore,
            disabled:(this.isFixedAsset ||(this.isEdit && !this.isClone))?true:false,
            disabledClass:"newtripcmbss",
            hidden:this.isFixedAsset,
            hideLabel:this.isFixedAsset,
            anchor:'85%',
            allowBlank:false,
            valueField:'id',
            displayField:'name',
            forceSelection: true
        });

        this.vendor= new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.preferedVendor.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.preferedVendor")+"</span>",//WtfGlobal.getLocaleText("acc.product.preferedVendor"),//'Preferred Vendor',
            hiddenName:'vendor',
            store:Wtf.vendorAccStore,
            anchor:'95%',
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
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendor, Wtf.Perm.vendor.create))
            this.vendor.addNewFn=this.addPerson.createDelegate(this,[false,null,"vendorwindow",false],true);
        this.cCountInterval=new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.cycleCountInterval.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.cycleCountInterval") +"</span>",// WtfGlobal.getLocaleText("acc.product.cycleCountInterval"),//"Cycle count interval(in days)* "+WtfGlobal.addLabelHelp("Cycle Count interval defines the time period at which physical counting of Inventory items is to be done. This cross-check helps in identifying discrepancies between Counted quantities,and current records for quantities in the system."),
            name: 'ccountinterval',
            allowDecimals:false,
            allowNegative:false,
            anchor:'85%',
            hidden:this.isFixedAsset,
            hideLabel:this.isFixedAsset,
            allowBlank:this.isFixedAsset,
//            value:0,
            maxLength:11
        });

         this.cCountTolerance=new Wtf.form.NumberField({
            fieldLabel: "<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.cycleCountTolerance.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.cycleCountTolerance") +"</span>",//WtfGlobal.getLocaleText("acc.product.cycleCountTolerance"),//'Cycle count tolerance (%)*'+WtfGlobal.addLabelHelp("It defines the accepted difference between the Physically Counted Quantity and Stock Level present in Deskera Accounting System. This is defined in percentage terms."),
            name: 'ccounttolerance',
            allowDecimals:false,
            allowNegative:false,
            anchor:'85%',
            hidden:this.isFixedAsset,
            hideLabel:this.isFixedAsset,
            allowBlank:this.isFixedAsset,
            maxValue:100,
            minValue:0,
            value:0
        });
        
         this.productWeight=new Wtf.form.NumberField({
            fieldLabel: "<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.erp.ProductWeightTT")+"'>"+ WtfGlobal.getLocaleText("acc.erp.ProductWeight") +"</span>",// Product Weight
            name: 'productweight',
            allowNegative:false,
            anchor:'85%',
            hidden:this.isFixedAsset||(this.producttypeval==Wtf.producttype.service),
            hideLabel:this.isFixedAsset,
            allowBlank:this.isFixedAsset,
            maxValue:100,
            minValue:0,
            value:0
        });
        
         this.uom= new Wtf.form.FnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.uom.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.uom")+"</span>",//WtfGlobal.getLocaleText("acc.product.uom"),//'Unit Of Measure*',
            hiddenName:'uomid',
            store:Wtf.uomStore,
            hidden:this.isFixedAsset,
            hideLabel:this.isFixedAsset,
            anchor:'85%',
            allowBlank:(this.producttypeval==Wtf.producttype.service) || this.isFixedAsset,
            valueField:'uomid',
            displayField:'uomname',
            forceSelection: true//,
           // addNewFn:this.showUom.createDelegate(this)
        });      
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
        
        this.barcodeFieldset = new Wtf.form.FieldSet({
            title:WtfGlobal.getLocaleText("acc.product.generateBarcodUsing"), 
            autoHeight: true,
            layout:'form',
            hidden:!Wtf.account.companyAccountPref.generateBarcodeParm,
            border: false,
            items:[{
                    border:false,
            items:[{
                xtype:'radio',
                boxLabel:WtfGlobal.getLocaleText("acc.invoiceList.expand.PID"),
                aotoWidth:true,
                labelSeparator :'',
                inputValue:Wtf.BarcodeGenerator_ProductId,  //2
                name:'barcodegr',
                 disabled:(this.isEdit && !this.isClone)?((this.record.data.isUsedInTransaction?true:false)):false,
                id:'bprodId'+this.id
            },{
                    xtype:'radio',
                    boxLabel:WtfGlobal.getLocaleText("acc.do.partno"),
                    aotoWidth:true,
                    labelSeparator :'',
                    inputValue:Wtf.BarcodeGenerator_SerialId,  //1
                    name:'barcodegr',
                    disabled:(this.isEdit && !this.isClone)?((this.record.data.isUsedInTransaction?true:false)):true,
                    id:'bserialId'+this.id
            },{ 
                 hidden:!Wtf.account.companyAccountPref.SKUFieldParm,
                 xtype:'radio',
//                 boxLabel:WtfGlobal.getLocaleText("acc.product.skufield"),
//                 boxLabel:((Wtf.account.companyAccountPref.SKUFieldRename=="" || Wtf.account.companyAccountPref.SKUFieldRename== undefined) && !Wtf.account.companyAccountPref.SKUFieldParm)?WtfGlobal.getLocaleText("acc.product.skufield") : Wtf.account.companyAccountPref.SKUFieldRename,
                 aotoWidth:true,
                 inputValue: Wtf.BarcodeGenerator_SKUField, //3
                 name:'barcodegr',
                 disabled:(this.isEdit && !this.isClone)?((this.record.data.isUsedInTransaction?true:false)):true,
                 id:'bSkuId'+this.id},
            {
                xtype: 'radio',
                boxLabel: WtfGlobal.getLocaleText("acc.do.barcode.batchno"), //5 - Barcode based on Batch Number
                aotoWidth: true,
                labelSeparator: '',
                inputValue: Wtf.BarcodeGenerator_BatchID, //5
                name: 'barcodegr',
                disabled: true, //(this.isEdit && !this.isClone)?((this.record.data.isUsedInTransaction?true:false)):true,
                id: 'bbatchId' + this.id
            }]
            
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
        this.salesAcc=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.salesAcc.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.salesAcc")+"</span>",//WtfGlobal.getLocaleText("acc.product.salesAcc"),//'Sales Account*',
            store:Wtf.salesAccStore,
            name:'salesaccountid',
            anchor:'85%',
            hiddenName:'salesaccountid',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAhead: true,
            hideLabel:this.isFixedAsset,
            hidden:this.isFixedAsset,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            typeAheadDelay:30000,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: this.isFixedAsset,
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
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.createcoa))
            this.salesAcc.addNewFn=this.addAccount.createDelegate(this,[Wtf.salesAccStore,false,false,true],true);
        this.salesReturnAcc=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.salesReturnAcc.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.salesReturnAcc")+"</span>",//WtfGlobal.getLocaleText("acc.product.salesReturnAcc"),//'Sales Return Account*',
            store:Wtf.salesAccStore,
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            typeAhead: true,
            hideLabel:this.isFixedAsset,
            hidden:this.isFixedAsset,
            name:'salesretaccountid',
            anchor:'85%',
            hiddenName:'salesretaccountid',
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: this.isFixedAsset//,
           // addNewFn: this.addAccount.createDelegate(this,[Wtf.salesAccStore,true,false],true)
        });
        
        this.assetControllingAcc=new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.fixed.asset.controlling.account"),//'Controlling Account*',
            store:this.FixedAssetControllingAccStore,
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            typeAhead: true,
            name:'assetControllingAccountId',
            anchor:'85%',
            hiddenName:'assetControllingAccountId',
            hideLabel:!this.isFixedAsset,
            hidden:!this.isFixedAsset,
            isAccountCombo:true,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: !this.isFixedAsset
        });
        
        this.assetControllingAcc.on('beforeselect',function(combo,record,index){
            return validateSelection(combo,record,index);
        },this);
        
        var typeArr = new Array();
        typeArr.push([Wtf.GSTITCTYPEID.DEFAULT, Wtf.GSTITCTYPE.DEFAULT]);
        typeArr.push([Wtf.GSTITCTYPEID.BLOCKEDITC, Wtf.GSTITCTYPE.BLOCKEDITC]);
        typeArr.push([Wtf.GSTITCTYPEID.ITCREVERSAL, Wtf.GSTITCTYPE.ITCREVERSAL]);
        this.itcType = new Wtf.data.SimpleStore({
            fields: ['typeid', 'name'],
            data: typeArr
        });
        this.itcTypeCmbForAsset = new Wtf.form.ComboBox({
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
            anchor:'85%'
        });        
         this.salesRevenueRecognitionAccount=new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.product.salesRevenueRecognitionAccount"),//' Sales Return Account*',
            store:Wtf.salesAccStore,
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            hidden:this.isFixedAsset,
            hideLabel:this.isFixedAsset,
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
        this.revenueRecognitionProcess=new Wtf.form.Checkbox({                               
            fieldLabel:WtfGlobal.getLocaleText("acc.product.revenueRecognitionProcess"),//Allow Revenue Recognition
            name:'revenueRecognitionProcess',
            hidden:this.isFixedAsset,
            hideLabel:this.isFixedAsset,
            itemCls:"chkboxalign"
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.edit))
            this.salesReturnAcc.addNewFn=this.addAccount.createDelegate(this,[Wtf.salesAccStore,true,false],true);
       this.purchaseAcc=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.purchaseAcc.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.purchaseAcc")+"</span>",//WtfGlobal.getLocaleText("acc.product.purchaseAcc"),//'Purchase Account*',
            store:this.purchaseAccStore,
            anchor:'95%',
            name:'purchaseaccountid',
            hiddenName:'purchaseaccountid',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            typeAhead: true,
            hideLabel:this.isFixedAsset,
            hidden:this.isFixedAsset,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: this.isFixedAsset,
            //addNewFn:this.isInventory.createDelegate(this),
            listeners:{
                select:{
                    fn:function(c){
                        if(!this.purchaseReturnAcc.getValue())
                            this.purchaseReturnAcc.setValue(c.getValue());
                    },
                    scope:this
                }
            }
       });
       
       if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.createcoa))
            this.purchaseAcc.addNewFn=this.isInventory.createDelegate(this);//this.uom.addNewFn=this.isInventory.createDelegate(this);
       this.purchaseReturnAcc=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.purchaseReturnAcc.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.purchaseReturnAcc")+"</span>",//WtfGlobal.getLocaleText("acc.product.purchaseReturnAcc"),//'Purchase Return Account*',
            store:this.purchaseAccStore,
            anchor:'95%',
            name:'purchaseretaccountid',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            typeAhead: true,
            hideLabel:this.isFixedAsset,
            hidden:this.isFixedAsset,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            hiddenName:'purchaseretaccountid',
            valueField:'accid',
            forceSelection: true,
            displayField:'accname',
            allowBlank: this.isFixedAsset//,
//            addNewFn: this.addAccount.createDelegate(this,[this.purchaseAccStore,false,true],true)
        });
        
            
        this.locationEditor = new Wtf.form.FnComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.field.Defaultlocation.tt")+"'>"+ WtfGlobal.getLocaleText("acc.field.Defaultlocation")+"</span>",//WtfGlobal.getLocaleText("acc.field.Defaultlocation"),
            valueField:'id',
            displayField:'name',
            store:this.locationStore,
            anchor:'95%',
            typeAhead: true,
            forceSelection: true,
            name:'location',
            hidden:this.isFixedAsset,
            hideLabel:this.isFixedAsset,
            hiddenName:'location'

        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.create))  //default location
         this.locationEditor.addNewFn=this.addMasterItem.createDelegate(this,["location",Wtf.locationStore]);//this.uom.addNewFn  
         
        this.wareHouseEditor = new Wtf.form.FnComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.field.DefaultWarehouse.tt")+"'>"+ WtfGlobal.getLocaleText("acc.field.DefaultWarehouse")+"</span>",//WtfGlobal.getLocaleText("acc.field.DefaultWarehouse"),
            valueField:'id',
            displayField:'name',
            store:this.wareHouseStore,
             anchor:'95%',
            typeAhead: true,
            forceSelection: true,
            hidden:this.isFixedAsset,
            hideLabel:this.isFixedAsset,
            name:'warehouse',
            hiddenName:'warehouse'

        });
        this.gstHistory = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.gsthistory.producttaxclassbtn"),
            tooltip: WtfGlobal.getLocaleText("acc.gsthistory.taxclasstoolip"),
            id: "saveasdraft" + this.id,
            disabled: (this.isEdit && !this.isClone) ?false:true,
            hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            scope: this,
//            style: 'margin-left:165px;margin-top:7px;margin-bottom:10px;',
            handler: function() {
                this.openHistoryWindow();
            },
            iconCls: 'pwnd save'
        });
       if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.create))   //this.uom.addNewFn  
            //this.wareHouseEditor.addNewFn= (this.businessPerson=="Customer")?this.addMaster.createDelegate(this,[7,Wtf.CustomerCategoryStore]):this.addMaster.createDelegate(this,[8,Wtf.VendorCategoryStore])        
            this.wareHouseEditor.addNewFn=this.addMasterItem.createDelegate(this,["warehouse",Wtf.wareHouseStore]);
       
        this.isBatchForProduct=new Wtf.form.Checkbox({ //option to check wether batch is compulsory or not
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isBatchCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isBatchCompulsory")+"</span>",
            name:'isBatchForProduct',
            checked:false,
            disabled:(this.isEdit && !this.isClone)?((this.record.data.isUsedInTransaction?true:false)):false,
            hidden:!Wtf.account.companyAccountPref.isBatchCompulsory,
            hideLabel:!Wtf.account.companyAccountPref.isBatchCompulsory,
            itemCls:"chkboxalign"
        });
        this.isLocationForProduct=new Wtf.form.Checkbox({ //option to check wether batch is compulsory or not
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isLocationCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isLocationCompulsory")+"</span>",
            name:'isLocationForProduct',
            checked:false,
            disabled:(this.isEdit && !this.isClone)?((this.record.data.isUsedInTransaction?true:false)):false,
            hidden:!Wtf.account.companyAccountPref.isLocationCompulsory,
            hideLabel:!Wtf.account.companyAccountPref.isLocationCompulsory,
            itemCls:"chkboxalign"
        });
        this.isWarehouseForProduct=new Wtf.form.Checkbox({ //option to check wether batch is compulsory or not
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isWarehouseCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isWarehouseCompulsory")+"</span>",
            name:'isWarehouseForProduct',
            checked:false,
            disabled:(this.isEdit && !this.isClone)?((this.record.data.isUsedInTransaction?true:false)):false,
            hidden:!Wtf.account.companyAccountPref.isWarehouseCompulsory,
            hideLabel:!Wtf.account.companyAccountPref.isWarehouseCompulsory,
            itemCls:"chkboxalign"
        });
          this.isRowForProduct=new Wtf.form.Checkbox({ //option to check wether batch is compulsory or not
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isRowCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isRowCompulsory")+"</span>",
            name:'isRowForProduct',
            checked:false,
            disabled:(this.isEdit && !this.isClone)?((this.record.data.isUsedInTransaction?true:false)):false,
            hidden:!Wtf.account.companyAccountPref.isRowCompulsory,
            hideLabel:!Wtf.account.companyAccountPref.isRowCompulsory,
            itemCls:"chkboxalign"
        });
          this.isRackForProduct=new Wtf.form.Checkbox({ //option to check wether batch is compulsory or not
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isRackCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isRackCompulsory")+"</span>",
            name:'isRackForProduct',
            checked:false,
            disabled:(this.isEdit && !this.isClone)?((this.record.data.isUsedInTransaction?true:false)):false,
            hidden:!Wtf.account.companyAccountPref.isRackCompulsory,
            hideLabel:!Wtf.account.companyAccountPref.isRackCompulsory,
            itemCls:"chkboxalign"
        });
          this.isBinForProduct=new Wtf.form.Checkbox({ //option to check wether batch is compulsory or not
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isBinCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isBinCompulsory")+"</span>",
            name:'isBinForProduct',
            checked:false,
            disabled:(this.isEdit && !this.isClone)?((this.record.data.isUsedInTransaction?true:false)):false,
            hidden:!Wtf.account.companyAccountPref.isBinCompulsory,
            hideLabel:!Wtf.account.companyAccountPref.isBinCompulsory,
            itemCls:"chkboxalign"
        });
        this.isSerialForProduct=new Wtf.form.Checkbox({ //option to check wether batch is compulsory or not
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isSerialCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isSerialCompulsory")+"</span>",
            name:'isSerialForProduct',
            checked:false,
            disabled:(this.isEdit && !this.isClone)?((this.record.data.isUsedInTransaction?true:false)):false,
            hidden:!Wtf.account.companyAccountPref.isSerialCompulsory,
            hideLabel:!Wtf.account.companyAccountPref.isSerialCompulsory,
            itemCls:"chkboxalign",
            listeners:{
                scope:this,
                check:function(){
                  if(this.isSerialForProduct.getValue() && ((this.isEdit && !this.isClone)?((this.record.data.isUsedInTransaction?false:true)):true)){
                       Wtf.getCmp('bserialId'+this.id).enable();
                       Wtf.getCmp('bSkuId'+this.id).enable();
                   }else{
                       Wtf.getCmp('bserialId'+this.id).setValue(false);
                       Wtf.getCmp('bSkuId'+this.id).setValue(false);
                       Wtf.getCmp('bserialId'+this.id).disable();
                       Wtf.getCmp('bSkuId'+this.id).disable();
                      
                   }
                }
            }
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.createcoa))
            this.purchaseReturnAcc.addNewFn=this.addAccount.createDelegate(this,[this.purchaseAccStore,false,true],true);
        this.parentname= new Wtf.form.FnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.parentProduct.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.parentProduct")+"</span>",//WtfGlobal.getLocaleText("acc.product.parentProduct"),//'Parent Product',
            anchor:'85%',
            hiddenName:'parentid',
            store: this.parentStore,
            valueField:'parentid',
            displayField:'parentname',
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            hirarchical:true,
            triggerAction: 'all'
        });
        this.descriptionshow = new Wtf.form.TextArea({
            fieldLabel:"<span wtf:qtip='"+((this.isFixedAsset)?WtfGlobal.getLocaleText("acc.fixedasset.gridDesc.tt"):WtfGlobal.getLocaleText("acc.product.gridDesc.tt"))+"'>"+ ((this.isFixedAsset)?WtfGlobal.getLocaleText("acc.AssetGroupDescription"):WtfGlobal.getLocaleText("acc.productList.gridProduct/ServiceDescription"))+"</span>",//WtfGlobal.getLocaleText("acc.product.description"),// 'Description',
            name: 'descshow',
            anchor:'85%',
//            regex:/^[^\"\\]+$/,
            height: 50,
            invalidText : WtfGlobal.getLocaleText("acc.field.Thisfieldshouldnotcontaincharacters")
//            maxLength:1024
        });
        this.descriptionshow.on("focus",function(){
            if(Wtf.account.companyAccountPref.ishtmlproddesc){
                this.getPostTextEditor();
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
            items:[this.parentname]
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
                    disabled:(this.isEdit && !this.isClone)?true:false,
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
                    disabled:(this.isEdit && !this.isClone)?true:false,
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
                    disabled:(this.isEdit && !this.isClone)?true:false,
                    //allowBlank:false,
                    anchor:'95%',
                    hidden:!Wtf.account.companyAccountPref.partNumber,
                    hideLabel:!Wtf.account.companyAccountPref.partNumber,
        //            regex:/^[\w\s\'\"\.\-]+$/,
                    regex:/^[^\"\%\\]+$/,
                    invalidText :  WtfGlobal.getLocaleText("acc.field.Thisshouldnotbeblankshouldnotcontain%characters"),
                    maxLength:50
                });
                this.shelfLocationCombo=new Wtf.form.ComboBox({
                    fieldLabel: WtfGlobal.getLocaleText("acc.field.ShelfLocation"),
                    name: 'shelfLocation',
                    mode: 'local',
                    triggerAction: 'all',
                    emptyText:WtfGlobal.getLocaleText("acc.field.SelectShelfLocation"),
                    typeAhead: true,
                    editable: true,
                    width:200,
                    store: this.shelfLocationStore,
                    displayField: 'shelfLocationValue',
                    valueField:'shelfLocationId',
                    hiddenName:'shelfLocationId',
                    hidden:!Wtf.account.companyAccountPref.invAccIntegration,
                    hideLabel:!Wtf.account.companyAccountPref.invAccIntegration
                });
                
                this.createFixedAssetEntryDetails();
                this.createProductComposition();
                
    },
     openHistoryWindow: function() {
        var config = {};
        config.masterid = this.recordid;
        config.gsthistorydetails = false;
        config.parentObj = this;
        config.isProduct = true;
        config.isFixedAsset = this.isFixedAsset;
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
    depreciationMethodSelected:function(combo,rec,idx){
        if(Wtf.account.companyAccountPref.depreciationCalculationType == Wtf.EffectiveFrom_DateOfAcquisiation_NoOfDays && rec.get('depreciationmethodvalue') != 4 && rec.get('depreciationmethodvalue') != 3){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.asset.method4.alertmsg")],2);
            this.depreciationMethodCombo.setValue(4);
            return true;
        }
        
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
                ['1', WtfGlobal.getLocaleText("acc.asset.depreciation.method.slm")],
                ['2', WtfGlobal.getLocaleText('acc.asset.depreciation.method.dd')],
                ['4', WtfGlobal.getLocaleText('acc.asset.depreciation.method.wdv')],
                ['3', WtfGlobal.getLocaleText('acc.asset.depreciation.method.none')]
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
            value:Wtf.account.companyAccountPref.depreciationCalculationType == Wtf.EffectiveFrom_DateOfAcquisiation_NoOfDays ? 4 : 1,
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
//            url:"ACCAccountCMN/getAccountsForCombo.do",
//            url:"ACCAccountCMN/getAccountsForComboOptimized.do",
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
            fieldLabel:WtfGlobal.getLocaleText("acc.fixed.asset.dep.provision.account") + "*",
            minChars:1,
            listWidth :300,
            anchor:'85%',
            allowBlank:false,
            isAccountCombo:true,
            valueField:'accountid',
            displayField:'accountname',
            forceSelection:true,
            hirarchical:true,
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            mode: 'local',
            typeAheadDelay:30000,
            extraComparisionField:'acccode',
            listAlign:"bl-tl?",
            maxHeight:300
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
            isAccountCombo:true,
            valueField:'accountid',
            displayField:'accountname',
            forceSelection:true,
            hirarchical:true,
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            mode: 'local',
            typeAheadDelay:30000,
            extraComparisionField:'acccode',
            listAlign:"bl-tl?",
            maxHeight:300
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
            allowBlank:!this.isFixedAsset,
            listAlign:"bl-tl?",
            maxHeight:300
        });
        
        /*
         * ERM-597 -Fixed asset - Provision of Write-off account for FA. 
        */
        this.writeOffAssetAccount=new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.fixed.asset.dep.profitloss.writeoffaccount"),
            store:this.salesAccountStore,
            name:'writeOffAssetAccount',
            anchor:'85%',
            hiddenName:'writeOffAssetAccount',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAhead: true,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            typeAheadDelay:30000,
            valueField:'accountid',
            displayField:'accountname',
            forceSelection: true,
            allowBlank:!this.isFixedAsset,
            listAlign:"bl-tl?",
            maxHeight:300
        });
        
        this.saleAssetAccount.on('beforeselect',function(combo,record,index){
            return validateSelection(combo,record,index);
        },this);
        
        this.writeOffAssetAccount.on('beforeselect',function(combo,record,index){
            return validateSelection(combo,record,index);
        },this);
        
        
        this.salesAccountStore.on('load',function(){
            if(this.isEdit && this.isFixedAsset){
                this.saleAssetAccount.setValue(this.record.data.assetSaleGL);
                this.writeOffAssetAccount.setValue(this.record.data.writeoffassetaccount);
            }
        },this);
        
        this.salesAccountStore.load();
        
        chkLandingCostCategoryload();
        Wtf.landingCostCategoryStore.on('load', function () {
            if (this.isEdit) {
                this.landingCostCategoryCombo.setValue(this.record.data.landingcostcategoryid);
            }
        }, this);
        this.LCCComboconfig = {
            hiddenName: "landingCostCategory",
            store: Wtf.landingCostCategoryStore,
            valueField: 'id',
            displayField: 'name',
            mode: 'local',
            typeAhead: true,
            selectOnFocus: true,
            allowBlank: true,
            triggerAction: 'all',
            scope: this
        };

        this.landingCostCategoryCombo = new Wtf.common.Select(Wtf.applyIf({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.invoice.landingCostCategory"), //"Landed Cost Category", // "Product Brand",
            name: "landingCostCategoryCombo",
            id: 'landingCostCategoryCombo' + this.id,
            hidden: !(Wtf.account.companyAccountPref.isActiveLandingCostOfItem && Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA),
            hideLabel: !(Wtf.account.companyAccountPref.isActiveLandingCostOfItem && Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA),
            hiddenName: "landingCostCategoryCombo",
            anchor: "85%",
            forceSelection: true
        }, this.LCCComboconfig));
    
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
                items:[this.depreciationMethodCombo,this.PNLTypeAccount,this.landingCostCategoryCombo]
            },{
                layout:'form',
                border:false,
                columnWidth:0.32,
                items:[this.depreciationRate,this.balanceSheetTypeAccount]
            },{
                layout:'form',
                border:false,
                columnWidth:0.34,
                items:[this.depreciationCostLimit,this.saleAssetAccount,this.writeOffAssetAccount]
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
                from:this.isFixedAsset?Wtf.Acc_FixedAssets_AssetsGroups_ModuleId:51,
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
            }
            
        });
        } else {
            this.PID.reset();
            this.PID.enable();
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
            allowBlank:this.isFixedAsset,
            hidden:this.isFixedAsset,
            hideLabel:this.isFixedAsset,
            anchor:'85%',
            value:0,
            maxLength:15,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL,
          //  allowDecimals:false,
            allowNegative:false
        });
        this.reorderLevel= new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.reorderLevel.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.reorderLevel") +"</span>",// WtfGlobal.getLocaleText("acc.product.reorderLevel"),//"Reorder Level* "+WtfGlobal.addLabelHelp("Reorder Level defines a stock level for item at which a new purchase order for items needs to be placed.In simple terms, it denotes the level of stock at which a replenishment order should be placed."),
            name: 'reorderlevel',
            anchor:'85%',
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL,
            value:0,
            allowBlank:this.isFixedAsset,
            hidden:this.isFixedAsset,
            hideLabel:this.isFixedAsset,
            maxLength:15,
           // allowDecimals:false,
            allowNegative:false
        });
        this.leadtime= new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.leadTime.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.leadTime") +"</span>",// WtfGlobal.getLocaleText("acc.product.leadTime"),//'Lead Time(in days)*'+WtfGlobal.addLabelHelp("The amount of time between the placing of an order and the receipt of the goods ordered."),
            name: 'leadtime',
            allowBlank:this.isFixedAsset,
            hidden:this.isFixedAsset,
            hideLabel:this.isFixedAsset,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL,
            anchor:'85%',
            maxLength:3,
            value:0,
            allowDecimals:false,
            allowNegative:false,
            maxValue:365
        });
        
        this.warrantyperiod= new Wtf.form.NumberField({
            fieldLabel: "<span wtf:qtip='"+((this.isFixedAsset)?WtfGlobal.getLocaleText("acc.fixedasset.WarrantyPeriod(indays).tt"): WtfGlobal.getLocaleText("acc.field.WarrantyPeriod(indays).tt"))+"'>"+ WtfGlobal.getLocaleText("acc.field.WarrantyPeriod(indays)")+"</span>",//WtfGlobal.getLocaleText("acc.field.WarrantyPeriod(indays)"),
            name: 'warrantyperiod',
            allowBlank:true,
            anchor:'85%',
            maxLength:3,
            //value:0,
            allowDecimals:false,
            allowNegative:false,
            emptyText : 'N/A'
            
        });
   this.warrantyperiodSal= new Wtf.form.NumberField({
            fieldLabel: "<span wtf:qtip='"+ ((this.isFixedAsset)?WtfGlobal.getLocaleText("acc.fixedasset.WarrantyPeriodS(indays).tt"):WtfGlobal.getLocaleText("acc.field.WarrantyPeriodS(indays).tt"))+"'>"+ WtfGlobal.getLocaleText("acc.field.WarrantyPeriodS(indays)")+"</span>",//WtfGlobal.getLocaleText("acc.field.WarrantyPeriodS(indays)"),
            name: 'warrantyperiodsal',
            allowBlank:true,
            anchor:'85%',
            maxLength:3,
            //value:0,
            allowDecimals:false,
            allowNegative:false,
            emptyText : 'N/A'
            
        });
        this.quantity=new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.initialQty.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.initialQty")+"</span>",//WtfGlobal.getLocaleText("acc.product.initialQty"),//"Initial Quantity",// "+WtfGlobal.addLabelHelp("Transactional data related to initial quantity will not be reflected"),
            name:'initialquantity',
            anchor:'95%',
            maxLength:15,
            disabled :(this.isEdit && !this.isClone)?true:false,
            allowNegative :false,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL,
            //allowDecimals:false,
            value:0
        });
        
        this.minOrderingQuantity=new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.minOrderingQuantity.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.minOrderingQuantity")+"</span>",//"Minimum Ordering Quantity"
            name:'minorderingquantity',
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
            anchor:'95%',
            maxLength:15,
            hidden : !Wtf.account.companyAccountPref.isMinMaxOrdering,
            hideLabel : !Wtf.account.companyAccountPref.isMinMaxOrdering,
            allowNegative :false,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL,
            value:0
        });
        
            this.quantity.on("blur",this.checkBatchSerial,this);
            this.initialprice=new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.initialPurchasePrice.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.initialPurchasePrice")+"</span>",//WtfGlobal.getLocaleText("acc.product.initialPurchasePrice")+'*',//'Initial Purchase Price',
            name:'initialprice',
            maxLength:15,
            decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL,
            readOnly:(this.isEdit && !this.isClone)?true:false,
            anchor:'95%',
            allowNegative :false,
//            allowDecimals:false
            value:0
        });
        this.initialsalesprice=new Wtf.form.NumberField({
            fieldLabel:"<span wtf:qtip='"+((this.isFixedAsset)?WtfGlobal.getLocaleText("acc.fixedasset.salesPrice.tt"): WtfGlobal.getLocaleText("acc.product.salesPrice.tt"))+"'>"+ WtfGlobal.getLocaleText("acc.product.salesPrice")+"</span>",//WtfGlobal.getLocaleText("acc.product.salesPrice"),//'Sales Price',
            name:'initialsalesprice',
            maxLength:15,
            readOnly:(this.isEdit && !this.isClone)?((this.producttypeval == Wtf.producttype.assembly || this.producttypeval == Wtf.producttype.customerAssembly)?false:true):false,
            anchor:'95%',
            allowNegative :false,
            decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL,
            value:0
        });
        
        this.reorderLevel.on('blur',this.setInputValue,this);	// Events fired to set and reset the NumberField value to '0' if no user input is detected
        this.reorderLevel.on('focus',this.resetInputValue,this);
        this.rQuantity.on('blur',this.setInputValue,this);
        this.rQuantity.on('focus',this.resetInputValue,this);
        this.leadtime.on('blur',this.setInputValue,this);
        this.leadtime.on('focus',this.resetInputValue,this);
        this.cCountTolerance.on('blur',this.setInputValue,this);
        this.cCountTolerance.on('focus',this.resetInputValue,this);
        this.productWeight.on('blur',this.setInputValue,this);
        this.productWeight.on('focus',this.resetInputValue,this);
        this.quantity.on('blur',this.setInputValue,this);
        this.quantity.on('focus',this.resetInputValue,this);
        this.minOrderingQuantity.on('blur',this.setInputValue,this);
        this.minOrderingQuantity.on('focus',this.resetInputValue,this);
        this.maxOrderingQuantity.on('blur',this.setInputValue,this);
        this.maxOrderingQuantity.on('focus',this.resetInputValue,this);
        this.initialsalesprice.on('blur',this.setInputValue,this);
        this.initialsalesprice.on('focus',this.resetInputValue,this);
        this.initialprice.on('blur',function(){if(this.producttypeval == Wtf.producttype.service){this.setInputValue(this.initialprice);}},this);
        this.initialprice.on('focus',function(){if(this.producttypeval == Wtf.producttype.service){this.resetInputValue(this.initialprice);}},this);
    },
    checkBatchSerial:function(obj){
    this.isLocationProduct=this.isLocationForProduct.getValue();
    this.isWarehouseProduct=this.isWarehouseForProduct.getValue();
    this.isRowProduct=this.isRowForProduct.getValue();
    this.isRackProduct=this.isRackForProduct.getValue();
    this.isBinProduct=this.isBinForProduct.getValue();
    this.isBatchForProd=this.isBatchForProduct.getValue();
    this.isSerialForProd=this.isSerialForProduct.getValue();
    if(this.isLocationProduct || this.isWarehouseProduct || this.isBatchForProd || this.isSerialForProd || this.isRowProduct || this.isRackProduct || this.isBinProduct)  {
        this.callSerialNoWindow(obj);
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
	
    createAssemblyGrid:function(){
        this.AssemblyGrid = new Wtf.account.productAssemblyGrid({
            layout:"fit",
            bodyBorder:true,
            hidden:true,
            border:false,
            bodyStyle:'padding:10px',
            height:200,
            gridtitle:WtfGlobal.getLocaleText("acc.product.gridBillofMaterials"),//"Bill Of Materials",
            productid:(this.record!=null?this.record.data['productid']:null),
            rendermode:"productform"
        });
        this.AssemblyGrid.on("updatedcost",function(grid){
            if( this.producttype.getValue() == Wtf.producttype.assembly || this.producttype.getValue() == Wtf.producttype.customerAssembly){
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
        WtfGlobal.hideFormElement(this.noOfServiceQTY);
        WtfGlobal.hideFormElement(this.autoAssembly);
        this.producttypeval = this.producttype.getValue();
        //Reset Form
        this.AssemblyGrid.hide();
        WtfGlobal.showFormElement(this.uom);
        WtfGlobal.showFormElement(this.rQuantity);
        WtfGlobal.showFormElement(this.reorderLevel);
        WtfGlobal.showFormElement(this.leadtime);
        WtfGlobal.showFormElement(this.warrantyperiod);  
        WtfGlobal.showFormElement(this.warrantyperiodSal);
        WtfGlobal.showFormElement(this.vendor);
        WtfGlobal.showFormElement(this.quantity);
        WtfGlobal.showFormElement(this.initialprice);
        WtfGlobal.showFormElement(this.initialsalesprice);
        WtfGlobal.showFormElement(this.multiuom);       
        this.cCountInterval.allowBlank = false;
        this.cCountTolerance.allowBlank = false;
        WtfGlobal.showFormElement(this.cCountInterval);
        WtfGlobal.showFormElement(this.cCountTolerance);
        WtfGlobal.showFormElement(this.isRecylable);
        WtfGlobal.showFormElement(this.productWeight);
        WtfGlobal.showFormElement(this.isBatchForProduct);
        if(Wtf.account.companyAccountPref.activateProductComposition){
            this.productCompositionFieldSet.show();
        }
        WtfGlobal.showFormElement(this.isSerialForProduct);
        WtfGlobal.showFormElement(this.locationEditor);
        WtfGlobal.showFormElement(this.wareHouseEditor);
        WtfGlobal.showFormElement(this.isLocationForProduct);
        WtfGlobal.showFormElement(this.isRackForProduct);
        WtfGlobal.showFormElement(this.isRowForProduct);
        WtfGlobal.showFormElement(this.isWarehouseForProduct);
        WtfGlobal.showFormElement(this.qaenable);
        WtfGlobal.showFormElement(this.isBinForProduct);
        if(this.producttypeval == Wtf.producttype.assembly || this.producttypeval == Wtf.producttype.customerAssembly){ //Inventory Assembly
            this.AssemblyGrid.show();
            WtfGlobal.hideFormElement(this.isRecylable);
            WtfGlobal.hideFormElement(this.vendor);
            WtfGlobal.showFormElement(this.autoAssembly);
            WtfGlobal.showFormElement(this.locationEditor);
            WtfGlobal.showFormElement(this.wareHouseEditor);
            this.productCompositionFieldSet.hide();
        }else if(this.producttypeval == Wtf.producttype.service){ //Service
            this.uom.allowBlank = true;
            this.AssemblyGrid.hide();
            WtfGlobal.hideFormElement(this.uom); 
            WtfGlobal.hideFormElement(this.rQuantity);
            WtfGlobal.hideFormElement(this.reorderLevel);
            WtfGlobal.hideFormElement(this.leadtime);
            WtfGlobal.hideFormElement(this.warrantyperiod);
            WtfGlobal.hideFormElement(this.warrantyperiodSal);
            WtfGlobal.hideFormElement(this.vendor);
            WtfGlobal.hideFormElement(this.quantity);
//            WtfGlobal.hideFormElement(this.initialprice);
            this.cCountInterval.allowBlank = true;
            this.cCountTolerance.allowBlank = true;
            WtfGlobal.hideFormElement(this.cCountInterval);
            WtfGlobal.hideFormElement(this.cCountTolerance);
             WtfGlobal.hideFormElement(this.productWeight);
            WtfGlobal.hideFormElement(this.multiuom);
            WtfGlobal.hideFormElement(this.autoAssembly);
            WtfGlobal.hideFormElement(this.isRecylable);
   	    WtfGlobal.showFormElement(this.noOfServiceQTY);	
            WtfGlobal.hideFormElement(this.isBatchForProduct);
            WtfGlobal.hideFormElement(this.isSerialForProduct);
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
        }
        if(this.producttypeval == Wtf.producttype.noninvpart) { // Non inventory part
            WtfGlobal.hideFormElement(this.multiuom);            
            WtfGlobal.hideFormElement(this.autoAssembly);            
            WtfGlobal.hideFormElement(this.isRecylable);            
            WtfGlobal.hideFormElement(this.isBatchForProduct);
            WtfGlobal.hideFormElement(this.isSerialForProduct);
            this.productCompositionFieldSet.hide();
        }
        if(this.producttypeval == Wtf.producttype.inventoryNonSale){
        	this.salesReturnAcc.setValue(null);
        	WtfGlobal.hideFormElement(this.salesReturnAcc);
        	this.salesAcc.setValue(null);
        	WtfGlobal.hideFormElement(this.salesAcc);
        	this.salesReturnAcc.allowBlank = true;
            this.salesAcc.allowBlank = true;
            WtfGlobal.hideFormElement(this.isRecylable);
            WtfGlobal.hideFormElement(this.initialsalesprice);
            WtfGlobal.hideFormElement(this.isBatchForProduct);
            WtfGlobal.hideFormElement(this.isSerialForProduct);
            WtfGlobal.hideFormElement(this.autoAssembly);
            this.productCompositionFieldSet.hide();
        }
        else{
        	WtfGlobal.showFormElement(this.salesReturnAcc);
        	WtfGlobal.showFormElement(this.salesAcc);
        }
        if(Wtf.account.companyAccountPref.invAccIntegration){
            WtfGlobal.showFormElement(this.shelfLocationCombo);
        }else{
            WtfGlobal.hideFormElement(this.shelfLocationCombo);
        }
        //Reset Form Labels
        
        if(!this.isFixedAsset){
            WtfGlobal.updateFormLabel(this.PID,WtfGlobal.getLocaleText("acc.product.productID")+":");
            WtfGlobal.updateFormLabel(this.Pname,WtfGlobal.getLocaleText("acc.product.productName")+":");
        }
        WtfGlobal.updateFormLabel(this.initialprice,WtfGlobal.getLocaleText("acc.product.initialPurchasePrice")+":");
        if(this.producttypeval == Wtf.producttype.assembly || this.producttypeval == Wtf.producttype.customerAssembly){ //Inventory Assembly
            var assemblyTotalPrice=this.AssemblyGrid.totalcost;
            WtfGlobal.updateFormLabel(this.initialprice,WtfGlobal.getLocaleText("acc.product.assemblyProductCost"));
            this.initialprice.setValue(assemblyTotalPrice);
            this.initialprice.disable();
        }else if(this.producttypeval == Wtf.producttype.service){ //Service
            WtfGlobal.updateFormLabel(this.PID,WtfGlobal.getLocaleText("acc.product.ServiceID")+":");
            WtfGlobal.updateFormLabel(this.Pname,WtfGlobal.getLocaleText("acc.product.ServiceName")+":");
            WtfGlobal.updateFormLabel(this.initialprice,WtfGlobal.getLocaleText("acc.product.servicePurchasePrice"));
            this.initialprice.enable();
//            this.initialprice.setValue(0);
        }else{
            this.initialprice.enable();
        }    
        if(this.isEdit && !this.isClone){
            this.initialprice.disable();
            if(this.producttypeval == Wtf.producttype.assembly || this.producttypeval == Wtf.producttype.customerAssembly)
            	this.initialsalesprice.enable();
            else
            	this.initialsalesprice.disable();
        }
        if(this.isFixedAsset){
            WtfGlobal.hideFormElement(this.quantity);
//            WtfGlobal.hideFormElement(this.syncable);
            WtfGlobal.hideFormElement(this.multiuom);
            WtfGlobal.hideFormElement(this.autoAssembly);
            WtfGlobal.hideFormElement(this.isRecylable);
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
    this.ProductForm=new Wtf.form.FormPanel({
        region: 'north',
        autoHeight: true,        
        id:"northForm"+this.id,
       // bodyStyle:"padding:10px",
        border:false,
        items:[{
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
                    this.sequenceFormatCombobox,
                    this.PID,
                    this.Pname,
                    this.description,
                    this.descriptionshow,
                    this.uom,
                    this.reorderLevel,
                    this.rQuantity,this.dependentType,this.isInterval,this.addShipLengthqty,this.interValCombo,this.noOfServiceQTY,this.extraQuantity,this.isLocationForProduct,this.isRowForProduct,
                    this.barcodeFieldset
                ]
            },{
                layout:'form',
                columnWidth:0.34,
                items:[
                    this.warrantyperiod,
                    this.warrantyperiodSal,
                    this.leadtime,
                    this.cCountInterval,
                    this.cCountTolerance,
                    this.productWeight,
                    this.subproduct,
                    this.salesAcc,
                    this.salesReturnAcc,
                    this.isBatchForProduct,
                    this.isSerialForProduct,this.isRackForProduct,
                    this.revenueRecognitionProcess,
                    this.salesRevenueRecognitionAccount,
                    this.assetControllingAcc,
                    this.itcTypeCmbForAsset
//                    this.gstHistory
                ]
            },{
                layout:'form',
                columnWidth:0.32,
                items:[
                    this.vendor,
                    this.purchaseAcc,
                    this.purchaseReturnAcc,
                    this.quantity,
                    this.minOrderingQuantity,
                    this.maxOrderingQuantity,
                    this.initialprice,
                    this.initialsalesprice,
                    this.locationEditor,
                    this.wareHouseEditor,
                    this.syncable,                    
                    this.multiuom,
                    this.autoAssembly,this.isWarehouseForProduct,this.isBinForProduct,
                    this.isRecylable,
                    this.qaenable,
                    this.Supplier,
                    this.CoilCraft,
                    this.InterPlant,
                    this.shelfLocationCombo]           
                }]
            },this.fixedAssetFieldSet,this.productCompositionFieldSet,this.tagsFieldset]
        }]
        });




    },
    checkParent:function(){
        if(this.parentStore.getCount()==0)
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.product.msg9")],1);
    },
    closeForm:function(){
        this.fireEvent('closed',this);
     },
    getDetails:function(){
       var commentlist = getDocsAndCommentList('', Wtf.Acc_Product_Master_ModuleId,this.id,undefined,'Product',undefined,"email",'leadownerid',this.contactsPermission,0,this.recordid);
    },
    saveForm:function(){
    	if(this.isClone){
    		this.isEdit = false;
    	} 
         if(!Wtf.account.companyAccountPref.ishtmlproddesc){
              this.description.setValue(this.descriptionshow.getValue());
          }  
        this.isLocationProduct=(this.isLocationForProduct.getValue()=="on"?true:this.isLocationForProduct.getValue());
        this.isWarehouseProduct=(this.isWarehouseForProduct.getValue()=="on"?true:this.isWarehouseForProduct.getValue());
        this.isRowProduct=(this.isRowForProduct.getValue()=="on"?true:this.isRowForProduct.getValue());
        this.isRackProduct=(this.isRackForProduct.getValue()=="on"?true:this.isRackForProduct.getValue());
        this.isBinProduct=(this.isBinForProduct.getValue()=="on"?true:this.isBinForProduct.getValue());
        this.isBatchProduct=(this.isBatchForProduct.getValue()=="on"?true:this.isBatchForProduct.getValue());
        this.isSerialProduct=(this.isSerialForProduct.getValue()=="on"?true:this.isSerialForProduct.getValue());
         var quantity=this.quantity.getValue();
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
       if((this.isBatchProduct || this.isSerialProduct)  && this.quantity.getValue()>0 && !(this.isFixedAsset) && !(this.isEdit)){ 
           var batchDetail = this.batchDetails;
            if(batchDetail == undefined || batchDetail == "[]" || batchDetail==""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.bsdetail")],2);   //Batch and serial no details are not valid.
                return;
            }
        }
        this.parentStore.clearFilter(true)
        this.producttypeval = this.producttype.getValue();
        var asemblyjson = "";
        if(this.producttypeval==Wtf.producttype.assembly || this.producttypeval==Wtf.producttype.customerAssembly){//Assembly Item
            this.initialprice.setValue(this.AssemblyGrid.totalcost);
            asemblyjson = this.AssemblyGrid.getAssemblyJson();
            if (!asemblyjson) {  // additional check
                WtfComMsgBox(40,2);
                return;
            }
            if(asemblyjson.trim()==""){
               WtfComMsgBox(40,2);
               return;
            }
        }     
        
        var productcompositionjson=""; //Product Composition
        productcompositionjson = this.getProductCompositionDetails();
        
        if(this.isFixedAsset){ 
            this.cCountInterval.allowBlank = true;

            this.cCountTolerance.allowBlank = true;
            
            this.productWeight.allowBlank=true;
            
            this.rQuantity.allowBlank = true;

            this.reorderLevel.allowBlank = true;
            
            this.leadtime.allowBlank = true;
            
            this.cCountInterval.setValue(0);
        }
        
        var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();
        var isValid = this.ProductForm.getForm().isValid();
        
        if(this.producttypeval!=Wtf.producttype.service){ // not Service
            if(this.cCountInterval.getValue()==="" && !this.isFixedAsset) {
                this.cCountInterval.markInvalid(WtfGlobal.getLocaleText("acc.product.msg1"));
                isValid = false;
            }else if(this.cCountInterval.getValue()==0 && !this.isFixedAsset) {
                this.cCountInterval.markInvalid(WtfGlobal.getLocaleText("acc.product.msg2"));
                isValid = false;
            }
        }

        if(!this.isEdit && this.producttypeval != Wtf.producttype.service){
//            if(this.initialprice.getValue()==="") {
//                this.initialprice.markInvalid(WtfGlobal.getLocaleText("acc.product.msg1"));
//                isValid = false;
//            }else
//              if(this.initialprice.getValue()==0){ // not Service
//                this.initialprice.markInvalid(WtfGlobal.getLocaleText("acc.product.msg3"));
//                isValid = false;
//            }
            
            if(this.uom.getValue()=='' && !this.isFixedAsset){ // not service
                this.uom.markInvalid(WtfGlobal.getLocaleText("acc.product.msg4"));
                isValid = false;
            }
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
                var isasserRecord=rec.data['isAsset'];
                parentname=parentname.replace(/\s+/g, '');
                if(parentname==FIND && isasserRecord==this.isFixedAsset){//Since Store contains both product and asset so it is needed to apply asset check
                    return true;
                }else{
                    return false
                }
            },this)
            if(index>=0){
                 WtfComMsgBox(36,2);
                 return;
            }

            if(this.PID.getValue()!=""){ //BUG Fixed #16316
               index=this.parentStore.findBy( function(rec){
                   if(rec.data['pid'] != undefined) {
                        var pid = rec.data['pid'].trim().toLowerCase();
                        pid =pid.replace(/\s+/g, '');
                        var isasserRecord=rec.data['isAsset'];
                        if(pid==FINDPID && isasserRecord==this.isFixedAsset){//Since Store contains both product and asset so it is needed to apply asset check
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
        if(this.producttypeval!=Wtf.producttype.service){ // not Service
            if(this.cCountInterval.getValue()==="") {
                this.cCountInterval.markInvalid(WtfGlobal.getLocaleText("acc.product.msg1"));
                isValid = false;
            }else if(this.cCountInterval.getValue()==0) {
                this.cCountInterval.markInvalid(WtfGlobal.getLocaleText("acc.product.msg2"));
                isValid = false;
            }
        }
         var custFieldArr=this.tagsFieldset.createFieldValuesArray();
            if(this.isEdit){
            	if(this.producttype.getValue() == Wtf.producttype.assembly || this.producttype.getValue() == Wtf.producttype.customerAssembly){
            		Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.rem.4"),(WtfGlobal.getLocaleText("acc.product.msg5")),function(btn){
                        if(btn=="yes"){
                        	this.reBuild = true;}
                        else{return;}
         	
                 var rec=this.ProductForm.getForm().getValues();
                  if (custFieldArr.length > 0)
                    rec.customfield = JSON.stringify(custFieldArr);
                 rec.producttype=this.producttype.getValue();
                 rec.initialprice=this.initialprice.getValue();
                 rec.pid=this.PID.getValue();
                 if(this.producttype.getValue() == Wtf.producttype.assembly || this.producttype.getValue() == Wtf.producttype.customerAssembly){
                 	rec.reBuild = this.reBuild;
                 }	
                        if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA) {
                            if (this.gstapplieddate == undefined) {
                            this.gstapplieddate = Wtf.account.companyAccountPref.firstfyfrom;
                        }
                            rec.isgstdetailsupdated = this.isgstdetailsupdated;
                            rec.gstapplieddate = WtfGlobal.convertToGenericDate(this.gstapplieddate);
                        }

                    rec.productname=this.Pname.getValue();
                    rec.batchDetails=this.batchDetails;
                    rec.mode=21;
                    rec.parentname=this.parentname.getRawValue();
                    rec.supplier=this.Supplier.getValue();
                    rec.coilcraft=this.CoilCraft.getValue();
                    rec.interplant=this.InterPlant.getValue();
                    rec.syncable=(this.syncable.getValue()=="on"?true:this.syncable.getValue());
                    rec.recyclable=(this.isRecylable.getValue()=="on"?true:this.isRecylable.getValue());
                    rec.multiuom=(this.multiuom.getValue()=="on"?true:this.multiuom.getValue());
                    rec.autoAssembly=(this.autoAssembly.getValue()=="on"?true:this.autoAssembly.getValue());
                    rec.qaenable=(this.qaenable.getValue()=="on"?true:this.qaenable.getValue());
                    rec.intervalField=(this.isInterval.getValue()=="on"?true:this.isInterval.getValue());
                    rec.addshiplentheithqty=(this.addShipLengthqty.getValue()=="on"?true:this.addShipLengthqty.getValue());
		    rec.quantity=this.quantity.getValue();
                    rec.isLocationForProduct=(this.isLocationForProduct.getValue()=="on"?true:this.isLocationForProduct.getValue());
                    rec.isWarehouseForProduct=(this.isWarehouseForProduct.getValue()=="on"?true:this.isWarehouseForProduct.getValue());
                    rec.isRowForProduct=(this.isRowForProduct.getValue()=="on"?true:this.isRowForProduct.getValue());
                    rec.isRackForProduct=(this.isRackForProduct.getValue()=="on"?true:this.isRackForProduct.getValue());
                    rec.isBinForProduct=(this.isBinForProduct.getValue()=="on"?true:this.isBinForProduct.getValue());
                    rec.isBatchForProduct=(this.isBatchForProduct.getValue()=="on"?true:this.isBatchForProduct.getValue());
                    rec.isSerialForProduct=(this.isSerialForProduct.getValue()=="on"?true:this.isSerialForProduct.getValue());
                    rec.quantity=this.quantity.getValue();
                    rec.editQuantity = this.editQuantity;
                    rec.assembly=asemblyjson;
                    rec.productcompositionjson=productcompositionjson;
                    rec.applydate=WtfGlobal.convertToGenericDate(Wtf.serverDate.clearTime(true));
                    var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
                    rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):true;
                    rec.sequenceformat=this.sequenceFormatCombobox.getValue();
                    if(Wtf.getCmp('bserialId'+this.id).getValue()){
                        rec.barcodeField=Wtf.BarcodeGenerator_SerialId;
                    }else if(Wtf.getCmp('bprodId'+this.id).getValue()){
                        rec.barcodeField=Wtf.BarcodeGenerator_ProductId;
                    }else if(Wtf.getCmp('bSkuId'+this.id).getValue()){
                        rec.barcodeField=Wtf.BarcodeGenerator_SKUField;
                    }else{
                        rec.barcodeField=Wtf.BarcodeGenerator_NULL;
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
	                        url: "ACCProductCMN/saveProduct.do",
	                        params: rec
	                    },this,this.genSuccessResponse,this.genFailureResponse);
                    }
            	},this);
            	} else {

                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.msgbox.CONFIRMTITLE"), (WtfGlobal.getLocaleText("acc.invoice.msg7")), function (btn) {
                        if (btn == "yes") {
                            var rec = this.ProductForm.getForm().getValues();
                            rec.producttype = this.producttype.getValue();
                            rec.initialprice = this.initialprice.getValue();
                            rec.pid = this.PID.getValue();
                            if (custFieldArr.length > 0)
                                rec.customfield = JSON.stringify(custFieldArr);
                            rec.productname = this.Pname.getValue();
                            rec.mode = 21;
                            rec.batchDetails = this.batchDetails;
                            rec.parentname = this.parentname.getRawValue();
                            rec.supplier = this.Supplier.getValue();
                            rec.coilcraft = this.CoilCraft.getValue();
                            rec.interplant = this.InterPlant.getValue();
                            rec.syncable = (this.syncable.getValue() == "on" ? true : this.syncable.getValue());
                            rec.recyclable = (this.isRecylable.getValue() == "on" ? true : this.isRecylable.getValue());
                            rec.multiuom = (this.multiuom.getValue() == "on" ? true : this.multiuom.getValue());
                            rec.isLocationForProduct = (this.isLocationForProduct.getValue() == "on" ? true : this.isLocationForProduct.getValue());
                            rec.isWarehouseForProduct = (this.isWarehouseForProduct.getValue() == "on" ? true : this.isWarehouseForProduct.getValue());
                            rec.isRowForProduct = (this.isRowForProduct.getValue() == "on" ? true : this.isRowForProduct.getValue());
                            rec.isRackForProduct = (this.isRackForProduct.getValue() == "on" ? true : this.isRackForProduct.getValue());
                            rec.isBinForProduct = (this.isBinForProduct.getValue() == "on" ? true : this.isBinForProduct.getValue());
                            rec.isBatchForProduct = (this.isBatchForProduct.getValue() == "on" ? true : this.isBatchForProduct.getValue());
                            rec.isSerialForProduct = (this.isSerialForProduct.getValue() == "on" ? true : this.isSerialForProduct.getValue());
                            rec.qaenable = (this.qaenable.getValue() == "on" ? true : this.qaenable.getValue());
                            rec.landingCostCategoryId = this.landingCostCategoryCombo.getValue();
                            rec.quantity = this.quantity.getValue();
                            rec.editQuantity = this.editQuantity;
                            rec.assembly = asemblyjson;
                            rec.productcompositionjson = productcompositionjson;
                            rec.intervalField = (this.isInterval.getValue() == "on" ? true : this.isInterval.getValue());
                            rec.addshiplentheithqty = (this.addShipLengthqty.getValue() == "on" ? true : this.addShipLengthqty.getValue());
                            rec.applydate = WtfGlobal.convertToGenericDate(Wtf.serverDate);
                            var seqFormatRec = WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
                            rec.seqformat_oldflag = seqFormatRec != null ? seqFormatRec.get('oldflag') : true;
                            rec.sequenceformat = this.sequenceFormatCombobox.getValue();
                            if (Wtf.getCmp('bserialId' + this.id).getValue()) {
                                rec.barcodeField = Wtf.BarcodeGenerator_SerialId;
                            } else if (Wtf.getCmp('bprodId' + this.id).getValue()) {
                                rec.barcodeField = Wtf.BarcodeGenerator_ProductId;
                            } else if (Wtf.getCmp('bSkuId' + this.id).getValue()) {
                                rec.barcodeField = Wtf.BarcodeGenerator_SKUField;
                            } else {
                                rec.barcodeField = Wtf.BarcodeGenerator_NULL;
                            }
                            if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA) {
                                if (this.gstapplieddate == undefined) {
                                    this.gstapplieddate = Wtf.account.companyAccountPref.firstfyfrom;
                                }
                                rec.isgstdetailsupdated = this.isgstdetailsupdated;
                                rec.gstapplieddate = WtfGlobal.convertToGenericDate(this.gstapplieddate);
                            }
                            if (this.isFixedAsset) {
                                rec.isFixedAsset = this.isFixedAsset;
                                rec.isDepreciable = true;// this field is not implemented yet
                                rec.depreciationMethod = this.depreciationMethodCombo.getValue();
                                rec.depreciationRate = this.depreciationRate.getValue();
                                rec.depreciationCostLimit = this.depreciationCostLimit.getValue();
                                rec.depreciationGLAccount = this.PNLTypeAccount.getValue();
                                rec.depreciationProvisionGLAccount = this.balanceSheetTypeAccount.getValue();
                                rec.sellAssetGLAccount = this.saleAssetAccount.getValue();
                                rec.writeOffAssetAccount = this.writeOffAssetAccount.getValue();
                                rec.assetControllingAccountId = this.assetControllingAcc.getValue();
                                if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isitcapplicable) {
                                    rec.itctype=this.itcTypeCmbForAsset.getValue();
                                }
                                rec.producttype = this.producttype.getValue();
                            }

                            Wtf.Ajax.requestEx({
                                url: "ACCProductCMN/saveProduct.do",
                                params: rec
                            }, this, this.genSuccessResponse, this.genFailureResponse);
                        }
                        else {
                            return;
                        }
                    }, this);

            	}    
            }else{
//                if(this.initialprice.getValue()==0 && this.producttypeval != Wtf.producttype.service){ // not Service
//                    WtfComMsgBox(["Alert"," Initial purchase price of the product can not be zero."],2);
//                    return;
//                }
//                if(this.uom.getValue()=='' && this.producttypeval!=Wtf.producttype.service){ // not service
//                    WtfComMsgBox(["Alert"," Please select UoM"],2);
//                    return;
//                }
            
            if(this.isFixedAsset){// Removed Confirnation MSG.
                WtfComMsgBox(27,3,true);
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.msgbox.CONFIRMTITLE"), (WtfGlobal.getLocaleText("acc.invoice.msg7")), function (btn) {
                if (btn == "yes") {                           
                var rec=this.ProductForm.getForm().getValues();
                if (custFieldArr.length > 0)
                    rec.customfield = JSON.stringify(custFieldArr);
                rec.mode=21;
                        
                rec.pid=this.PID.getValue();
                rec.batchDetails=this.batchDetails;
                      if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA) {
                            if (this.gstapplieddate == undefined) {
                            this.gstapplieddate = Wtf.account.companyAccountPref.firstfyfrom;
                        }
                            rec.isgstdetailsupdated = this.isgstdetailsupdated;
                            rec.gstapplieddate = WtfGlobal.convertToGenericDate(this.gstapplieddate);
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
                    rec.writeOffAssetAccount=this.writeOffAssetAccount.getValue();
                    rec.assetControllingAccountId=this.assetControllingAcc.getValue();
                    if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isitcapplicable) {
                        rec.itctype=this.itcTypeCmbForAsset.getValue();
                    }
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
                rec.syncable=(this.syncable.getValue()=="on"?true:this.syncable.getValue());
                rec.multiuom=(this.multiuom.getValue()=="on"?true:this.multiuom.getValue());
                rec.qaenable=(this.qaenable.getValue()=="on"?true:this.qaenable.getValue());
                rec.recyclable=(this.isRecylable.getValue()=="on"?true:this.isRecylable.getValue());
                rec.landingCostCategoryId = this.landingCostCategoryCombo.getValue();
                rec.assembly=asemblyjson;
                rec.productcompositionjson=productcompositionjson;
                rec.applydate=WtfGlobal.convertToGenericDate(Wtf.serverDate);
                var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
                rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):true;
                if(Wtf.getCmp('bserialId'+this.id).getValue()){
                        rec.barcodeField=Wtf.BarcodeGenerator_SerialId;
                }else if(Wtf.getCmp('bprodId'+this.id).getValue()){
                    rec.barcodeField=Wtf.BarcodeGenerator_ProductId;
                }else if(Wtf.getCmp('bSkuId'+this.id).getValue()){
                    rec.barcodeField=Wtf.BarcodeGenerator_SKUField;
                }else{
                    rec.barcodeField=Wtf.BarcodeGenerator_NULL;
                }
                Wtf.Ajax.requestEx({
                    //                        url: Wtf.req.account+'CompanyManager.jsp',
                    url: "ACCProductCMN/saveProduct.do",
                    params: rec
                },this,this.genSuccessResponse,this.genFailureResponse);
                
             } else{
                    return;
             }
           },this);
            }else{
//                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),(this.producttypeval != Wtf.producttype.service?WtfGlobal.getLocaleText("acc.product.msg7"):"")+" "+WtfGlobal.getLocaleText("acc.product.msg8"),function(btn){
//                        if(btn!="yes") {return;}
                        WtfComMsgBox(27,3,true);
                        var rec=this.ProductForm.getForm().getValues();
                        if (custFieldArr.length > 0)
                            rec.customfield = JSON.stringify(custFieldArr);
                        rec.mode=21;
                        
                        rec.pid=this.PID.getValue();
                        rec.batchDetails=this.batchDetails;
                        
                        rec.parentname=this.parentname.getRawValue();
                  //      rec.creationdate=this.CreationDate.getValue();
                        rec.isLocationForProduct=(this.isLocationForProduct.getValue()=="on"?true:this.isLocationForProduct.getValue());
                        rec.isWarehouseForProduct=(this.isWarehouseForProduct.getValue()=="on"?true:this.isWarehouseForProduct.getValue());
                        rec.isRowForProduct=(this.isRowForProduct.getValue()=="on"?true:this.isRowForProduct.getValue());
                        rec.isRackForProduct=(this.isRackForProduct.getValue()=="on"?true:this.isRackForProduct.getValue());
                        rec.isBinForProduct=(this.isBinForProduct.getValue()=="on"?true:this.isBinForProduct.getValue());
                        rec.isBatchForProduct=(this.isBatchForProduct.getValue()=="on"?true:this.isBatchForProduct.getValue());
                        rec.isSerialForProduct=(this.isSerialForProduct.getValue()=="on"?true:this.isSerialForProduct.getValue());
                        rec.quantity=this.quantity.getValue();
                        rec.syncable=(this.syncable.getValue()=="on"?true:this.syncable.getValue());
                        rec.recyclable=(this.isRecylable.getValue()=="on"?true:this.isRecylable.getValue());
                        rec.multiuom=(this.multiuom.getValue()=="on"?true:this.multiuom.getValue());
                        rec.autoAssembly=(this.autoAssembly.getValue()=="on"?true:this.autoAssembly.getValue());
                        rec.qaenable=(this.qaenable.getValue()=="on"?true:this.qaenable.getValue());
			rec.intervalField=(this.isInterval.getValue()=="on"?true:this.isInterval.getValue());
                        rec.addshiplentheithqty=(this.addShipLengthqty.getValue()=="on"?true:this.addShipLengthqty.getValue());
                        rec.landingCostCategoryId = this.landingCostCategoryCombo.getValue();
                        rec.assembly=asemblyjson;
                        rec.productcompositionjson=productcompositionjson;
                        rec.applydate=WtfGlobal.convertToGenericDate(Wtf.serverDate.clearTime(true));
                        var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
                        rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):true;
                        if(Wtf.getCmp('bserialId'+this.id).getValue()){
                            rec.barcodeField=Wtf.BarcodeGenerator_SerialId;
                        }else if(Wtf.getCmp('bprodId'+this.id).getValue()){
                            rec.barcodeField=Wtf.BarcodeGenerator_ProductId;
                        }else if(Wtf.getCmp('bSkuId'+this.id).getValue()){
                            rec.barcodeField=Wtf.BarcodeGenerator_SKUField;
                        }else{
                            rec.barcodeField=Wtf.BarcodeGenerator_NULL;
                        }
                        Wtf.Ajax.requestEx({
//                        url: Wtf.req.account+'CompanyManager.jsp',
                        url: "ACCProductCMN/saveProduct.do",
                        params: rec
                        },this,this.genSuccessResponse,this.genFailureResponse);
        //            },this)
            }
                    
            }
       }
       else{
            WtfComMsgBox(2, 2);
        }
    },
    genSuccessResponse:function(response){
        if(response.success){
            var titleMsg = (this.isFixedAsset)?WtfGlobal.getLocaleText("acc.fixed.asset.group"):WtfGlobal.getLocaleText("acc.invReport.prod");
          WtfComMsgBox([titleMsg,response.msg],0);
          if(this.record!=null)
            this.productID=this.record.data.productid;
          if(!this.isEdit){
            var pricerec={};
            this.productID=response.productID;
            //Set initial Purchase price
            if(this.initialprice.getValue()>0){
                pricerec.carryin=true;
                pricerec.productid=response.productID;

                pricerec.price=this.initialprice.getValue();
                pricerec.mode=11;
                pricerec.isFromAsset=true;
                pricerec.applydate=WtfGlobal.convertToGenericDate(Wtf.serverDate.clearTime(true));
                Wtf.Ajax.requestEx({
//                    url: Wtf.req.account+'CompanyManager.jsp',
                    url:"ACCProduct/setNewPrice.do",
                    params: pricerec
                },this,this.genPriceSuccessResponse,this.genPriceFailureResponse);
              if(this.isEdit && !this.isClone){  
                    Wtf.apply(this.detailPanel, {accid:this.productID});
                    Wtf.apply(this.detailPanel, {acccode:this.PID.value});
                    this.getDetails();     
              }
            }else{
                 this.callFireEventUpdate();
            }

            //Set initial Sales price
            if(this.initialsalesprice.getValue()>0){
                var pricerec1={};				// Send params as object and not as array bcos general controller accept requests in object form
                pricerec1.carryin=false;
                pricerec1.productid=response.productID;
                pricerec1.price=this.initialsalesprice.getValue();
                pricerec1.mode=11;
                pricerec1.isFromAsset=true;
                pricerec1.applydate=WtfGlobal.convertToGenericDate(Wtf.serverDate.clearTime(true));
                Wtf.Ajax.requestEx({
//                    url: Wtf.req.account+'CompanyManager.jsp',
                    url:"ACCProduct/setNewPrice.do",
                    params: pricerec1
                },this,this.genPriceSuccessResponse,this.genPriceFailureResponse);
            } else {
                 this.callFireEventUpdate();
            }
           

//            if(this.initialprice.getValue()<=0 && this.initialsalesprice.getValue()<=0){
//               Wtf.productStore.reload();
                //this.fireEvent('update',this,response.productID);
                this.fireEvent('productClosed',this);
//            }
            //if aassembly call function for build assembly


//            if( this.producttype.getValue()==Wtf.producttype.assembly){
////                alert("This is test");
//                Wtf.MessageBox.confirm("Build Assembly"," You want to build assembly?",function(btn){
//                    if(btn == "yes") {
//                        callBuildAssemblyForm(response.productID);
//                        this.fireEvent('update',this,response.productID);
//                        this.fireEvent('closed',this);
//                    }else{
//                        this.fireEvent('update',this,response.productID);
//                        this.fireEvent('closed',this);
//                        return;
//                    }
//                },this)
//            }else{
//                this.fireEvent('update',this,response.productID);
//                this.fireEvent('closed',this);
//            }
          }else{
        	  if(this.isEdit && (this.producttype.getValue() == Wtf.producttype.assembly || this.producttype.getValue() == Wtf.producttype.customerAssembly)){
                  var pricerec={};
                  this.productID=response.productID;
                  //Set Purchase price for Inventory Assembly 
                  if(this.initialprice.getValue()>0){
                      pricerec.carryin=true;
                      pricerec.productid=response.productID;
                      pricerec.changeprice = true;							// To change existing price			Neeraj
                      pricerec.price=this.initialprice.getValue();
                      pricerec.mode=11;
                      pricerec.isFromAsset=true;
                      pricerec.applydate=WtfGlobal.convertToGenericDate(Wtf.serverDate.clearTime(true));
                      Wtf.Ajax.requestEx({
                          url:"ACCProduct/setNewPrice.do",
                          params: pricerec
                      },this,this.genPriceSuccessResponse,this.genPriceFailureResponse);
                  }
                  if(this.initialsalesprice.getValue()>0){
                      var pricerec1={};				
                      pricerec1.carryin=false;
                      pricerec1.productid=response.productID;
                      pricerec1.changeprice = true;
                      pricerec1.price=this.initialsalesprice.getValue();
                      pricerec1.mode=11;
                      pricerec1.isFromAsset=true;
                      pricerec1.applydate=WtfGlobal.convertToGenericDate(Wtf.serverDate.clearTime(true));
                      Wtf.Ajax.requestEx({
                          url:"ACCProduct/setNewPrice.do",
                          params: pricerec1
                      },this,this.genPriceSuccessResponse,this.genPriceFailureResponse);
                  }                 
        	  }
//                Wtf.productStore.reload();
        	  this.fireEvent('update',this,this.productID);
              this.fireEvent('productClosed',this);
        	    
          }
         this.isClosable=true;
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
                                fieldLabel: WtfGlobal.getLocaleText("acc.product.newassetgroupid"),
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
        }else if (response.isAccountingExe) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],2);
        }else{
            if(this.isFixedAsset){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.invoice.gridAssetGroup"),response.msg],1);
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.invReport.prod"),response.msg],1);
            }
        }
        getCompanyAccPref();
        Wtf.productStoreSales.reload();
        Wtf.productStore.reload();
        Wtf.FixedAssetStore.reload();                              //In Order to reload the Fixed asset Group Store ERP-9510
},
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
//        this.fireEvent('closed',this);
    },
    addAccount: function(store,issales,ispurchase,incomenature){
        callCOAWindow(false,null,"coaWin",issales,ispurchase,false,false,false,incomenature);
        Wtf.getCmp("coaWin").on('update',function(){store.reload();},this);
    },
    callFireEventUpdate: function(){
        this.fireEventCounter++;
         if (this.fireEventCounter >= 2) {
                this.fireEvent('update', this, this.productID);
         }
    },
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
               // this.fireEvent('update',this,this.productID);
               this.callFireEventUpdate();

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
    },showSalesRevenueAccount:function(){
        WtfGlobal.showFormElement(this.salesRevenueRecognitionAccount);
        WtfGlobal.showFormElement(this.revenueRecognitionProcess);
        this.salesRevenueRecognitionAccount.allowBlank = true;
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
