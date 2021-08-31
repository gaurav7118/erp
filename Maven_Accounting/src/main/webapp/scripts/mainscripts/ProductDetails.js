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
function openProductWin(){
    callProductWindow();
    Wtf.getCmp("productwindow").on('update',function(obj,productID){
        Wtf.getCmp("ProductReport").updateGrid(obj,productID);
    },this);
}
Wtf.account.ProductDetailsPanel=function(config){
    if (dojoInitCount <= 0) {
        dojo.cometd.init("../../bind");
        dojoInitCount++;
    }
    this.Product=null;
    this.addproductwin=config.addproduct;
    this.arrRec=[];
    this.isEdit=false;
    this.ispropagatetochildcompanyflag=false;//this flag is used when there is parent child relationship between two does exist,to ask user do you want to delete record in child company as well 
    this.addNew=false;
    this.isExport=false;
    this.productID=null;
    this.productlinkid=config.productlinkid;
    this.uPermType=Wtf.UPerm.product;
    this.permType=Wtf.Perm.product;
    this.id=config.id;
    this.moduleId=config.moduleId;
    this.wcid=config.wcid;
    this.getDetailPanel();
    this.productRec = Wtf.data.Record.create ([
    {name:'productid'},
    {name:'productname'},
    {name:'desc'},
    {name:'pid'},
    {name:'vendor'},
    {name: 'vendorname'},
    {name: 'vendorcode'},
    {name:'producttype'},
    {name:'type'},
    {name:'initialsalesprice'},
    {name:'warrantyperiod'},
    {name:'warrantyperiodsal'},
    {name:'uomid'},
    {name:'uomname'},
    {name:'displayUoMid'},
    {name:'displayUoMName'},
    {name:'parentuuid'},
    {name:'parentid'},
    {name:'parentname'},
    {name:'purchaseaccountid'},
    {name:'interStatePurAccID'},
    {name:'interStatePurAccCformID'},
    {name:'salesaccountid'},
    {name:'interStateSalesAccID'},
    {name:'interStateSalesAccCformID'},
    {name:'purchaseretaccountid'},
    {name:'interStatePurReturnAccID'},
    {name:'interStatePurReturnAccCformID'},
    {name:'inputVAT'},//A
    {name:'cstVATattwo'},//A
    {name:'cstVAT'},//A
    {name:'salesretaccountid'},
    {name:'interStateSalesReturnAccID'},
    {name:'interStateSalesReturnAccCformID'},
    {name:'inputVATSales'},//A
    {name:'cstVATattwoSales'},//A
    {name:'cstVATSales'},//A
    {name:'purchaseaccountname'},
    {name:'salesaccountname'},
    {name:'purchaseretaccountname'},
    {name:'salesretaccountname'},
    {name:'shelfLocationId'},
    {name:'location'},
    {name:'warehouse'},
    {name:'reorderquantity'},
    {name:'quantity'},
    {name:'reorderlevel'},
    {name:'leadtime'},
    {name:'QAleadtime'},
    {name:'purchaseprice'},
    {name:'lockquantity'},
    {name:'reservestock'},
    {name:'consignquantity'},
    {name:'venconsignquantity'},
    {name:'saleprice'},
    {name:'salespriceinpricecurrency'},
    {name: 'leaf'},
    {name: 'warranty'},
    {name: 'syncable'},
    {name: 'qaenable'},
    {name: 'multiuom'},
    {name: 'blockLooseSell'},
    {name: 'uomschematypeid'},
    {name: 'isLocationForProduct'},
    {name: 'isWarehouseForProduct'},
    {name: 'isBatchForProduct'},
    {name: 'isSerialForProduct'},
    {name: 'isSKUForProduct'},
    {name: 'isRecyclable'},
    {name: 'recycleQuantity'},
    {name: 'level'},
    {name: 'initialquantity',mapping:'initialquantity'},
    {name: 'initialprice'},
    {name: 'dependenttype'},
    {name: 'dependenttypename'},
    {name: 'intervalfield'},
    {name: 'timeinterval'},
    {name: 'addshiplentheithqty'},
    {name: 'noofqty'},
    {name: 'qtyUOM'},
    {name: 'ccountinterval'},
    {name:'ccounttolerance'},
    {name:'productweight'},
    {name:'productweightperstockuom'},
    {name:'productweightincludingpakagingperstockuom'},
    {name:'productvolumeperstockuom'},
    {name:'productvolumeincludingpakagingperstockuom'},
    {name:'salesRevenueRecognitionAccountid'},
    {name:'revenueRecognitionProcess'},
    {name:'locationName'},
    {name:'warehouseName'},
    {name:'deleted'},
    {name:'batchdetails'},
    {name:'leasedQuantity'},
    {name:'sequenceformatid'},
    {name:'autoAssembly'},
    {name:'rcmapplicable'},
    {name:'minorderingquantity', mapping:'minorderingquantity'},
    {name:'maxorderingquantity', mapping:'maxorderingquantity'},
    {name:'isUsedInTransaction'},
    {name:'isUsedInBatchSerial'},
    {name:'depreciationMethod'},
    {name:'depreciationRate'},
    {name:'depreciationCostLimit'},
    {name:'barcodefield'},
    {name:'activateProductComposition'},
    {name:'productCompositionDetails'},
    {name: 'isRowForProduct'},
    {name: 'isRackForProduct'},
    {name: 'isBinForProduct'},
    {name: 'itemReusability'},
    {name: 'reusabilitycount'},
    {name: 'licensecode'},
    {name: 'licensetype'},
    {name: 'customercategory'},
    {name: 'inspectionTemplate'},
    {name: 'itemissuecount'},
    {name: 'currencyid'},
    {name: 'balancequantity'},
    {name: 'asofdate'},
    {name: 'industryCodeId'},
    {name: 'currencysymbol'},
    {name:'landingcostcategoryusedintransaction'},
   //INV_ACC_MERGE
    
    
    /**************************************************************************
            Starting of adding new records
    ***************************************************************************/
   // new Properties
   
    //General Tab        
    {name: 'barcode'},
    {name: 'additionaldescription'},
    {name: 'foreigndescription'},
    {name: 'itemgroup'},
    {name: 'itempricelist'},
    {name:'shippingtype'},
    {name:'isActiveItem'},
    {name:'isKnittingItem'},
    {name: 'isWastageApplicable'},
    {name: 'wastageAccount'},
    {name: 'serviceTaxCode'},
    {name: 'abatementRate'},
    {name: 'natureOfStockItem'},
    {name: 'substituteProductId'},
    {name: 'substituteProductName'},
    {name: 'substituteQty'},
    {name: 'excisemethod'},
    {name: 'excisemethodsubtype'},
    {name: 'exciserate'},
    {name: 'vatabatementrate'},
    {name: 'vatMethodType'},
    {name: 'reportingUOMVAT'},
    {name: 'reportingSchemaVAT'},
    {name: 'itcaccountid'},
    {name: 'itctype'},
    {name: 'vatabatementperiodfromdate'},
    {name: 'vatabatementperiodtodate'},
    {name: 'productBrandId'},
    {name: 'productBrandName'},
    
    // Purchase Tab fields
    {name:'catalogNo'},
    {name:'purchaseuomid'},
    {name:'purchaseuomname'},
    {name:'itempurchaseheight'},
    {name:'itempurchasewidth'},
    {name:'itempurchaselength'},
    {name:'itempurchasevolume'},
    {name:'purchasemfg'},
    
   // Sales Tab fields
    {name:'salesuomid'},
    {name:'salesuomname'},
    {name:'itemsalesheight'},
    {name:'itemsaleswidth'},
    {name:'itemsaleslength'},
    {name:'itemsalesvolume'},
    {name:'salesGL'},
    {name:'alternateproductid'},
    
   //  Properties Tab fields
    {name:'itemheight'},
    {name:'itemwidth'},
    {name:'itemlength'},
    {name:'itemvolume'},
    {name:'itemcolor'},
    
   //  Remarks Tab fields
    {name:'additionalfreetext'},
                               
  //  Inventory Data Tab fields
    {name: 'valuationmethod'},
    {name: 'casinguomid'},
    {name: 'casinguomvalue'},
    {name: 'inneruomid'},
    {name: 'inneruomvalue'},
    {name:'stockuomid'},
    {name:'stockuomvalue'},
    {name:'orderinguomid'},
    {name:'orderinguomname'},
    {name: 'transferuomid'},
    {name: 'transferuomname'},
    {name: 'packaging'},
    {name: 'packagingValue'},
    {name:'itemcost'},
    {name:'WIPoffset'},
    {name:'Inventoryoffset'},
    {name:'countable'},
    {name:'CCFrequency'},
    {name:'hsCode'},
    {name:'supplier'},
    {name:'coilcraft'},
    {name:'interplant'},
    {name:'customfield'},
    
    {name:'vatcommoditycode'},
    {name:'vatonmrp'},
    {name:'tariffname'},
    {name:'hsncode'},
    {name:'mrprate'},
    {name:'sac'},
    {name:'reportinguom'},
    {name:'reportingSchemaType'},
    {name:'stockadjustmentaccountid'},
    {name:'inventoryaccountid'},
    {name:'cogsaccountid'},
    {name:'repairquantity'},
    {name:'qaquantity'},
    //Product Term Mapping Details
    {name:'ProductTermPurchaseMapp'},
    {name:'ProductTermSalesMapp'},
    {name:'ProductTermAdditionalSalesMapp'},
    {name:'ProductTermAdditionalPurchaseMapp'},
    {name:'landingcostcategoryid'},
    {name:'displayUoM'},
    {name :'purchasetax'},
    {name:'salestax'}
    //End new properties
    /**************************************************************************
            Ending of adding new records
    ***************************************************************************/
           
]);
    this.expander = new Wtf.grid.RowExpander({});
    
    this.expandRec = Wtf.data.Record.create ([
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
    
    this.expandStore = new Wtf.data.Store({
        url : "ACCProduct/getBOMDetails.do",
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.expandRec)
    });
    
    this.expander.on("expand",this.onRowexpand,this);
    this.expandStore.on('load',this.fillExpanderBody,this);
    
    this.msgLmt = 30;
    this.jReader = new Wtf.data.KwlJsonReader({
        totalProperty: 'totalCount',
        root: "data"
    }, this.productRec);

this.productStore = new Wtf.data.Store({
//    url:Wtf.req.account+'CompanyManager.jsp',
    url:"ACCProduct/getProducts.do",
    remoteSort:true,
    baseParams:{mode:22,transactiondate:WtfGlobal.convertToGenericDate(new Date()),isForProductMaster:"true"},
    reader: this.jReader
});
var colModelArray = GlobalColumnModelForReports[Wtf.Acc_Product_Master_ModuleId];
WtfGlobal.updateStoreConfig(colModelArray, this.productStore);
    
 this.productStore.on('beforeload', function(s,o) {
          WtfGlobal.setAjaxTimeOutFor30Minutes();
          if(!o.params)o.params={};
            var currentBaseParams = this.productStore.baseParams;
            if(this.showProducts != undefined ){ 
                o.params.showallproduct=this.showProducts.getValue();
            }
            o.params.wcid=this.wcid !=undefined ? this.wcid : '';
            o.params.productTypeFilter=this.productTypeFilterCombo.getValue();
            this.productStore.baseParams=currentBaseParams;
 }, this);
//Wtf.productStore.on('beforeload',function(){this.productStore.reload();},this);
   // this.productStore.load();
  
    //Loaded Uom, sales account and vendor stores to resolve issue - (value not set) in edit product case
    chkUomload();
    chksalesAccountload();
    chkvenaccload();
    
    this.productStore.on('load',this.hideMsg,this);  
    WtfComMsgBox(29,4,true);
    var btnArr=[], bottomArr=[],reportbtnArr=[] ;
    var btnArrEDSingleS=[],reportbtnArrEDSingleS=[]; // Enable/Disable button's indexes on single select
    var btnArrEDMultiS=[],reportbtnArrEDMultiS=[]; // Enable/Disable button's indexes on multi select
    var productArr=[];
    var buildAssemblyArr=[];    //Build/Unbuild Assembly Menu
    var buildAssemblyReportArr=[];    //Build/Unbuild Assembly Report Menu
    var productArrEDSingleS=[]; // Enable/Disable button's indexes on single select
    var productArrEDMultiS=[]; // Enable/Disable button's indexes on multi select
    var bandArrEDSingleS = []; // Enable/Disable button's indexes on single select
    var bandArrEDMultiS=[]; // Enable/Disable button's indexes on multi select
    var manageimage=[];
    //var imgArrEDSingleS=[];
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.create)) {
        productArr.push(new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.productList.addNewProduct"),//'Add New Product/Service',
            tooltip:{text:WtfGlobal.getLocaleText("acc.productList.addNewProductTT")},
            iconCls:getButtonIconCls(Wtf.etype.menuadd),
            scope:this,
            handler:this.showForm.createDelegate(this,[false])
        }));
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit)) {
        productArr.push(this.prodEditBttn = new Wtf.Action({
                text:WtfGlobal.getLocaleText("acc.productList.editProduct"),//'Edit Product/Service',
                disabled:true,
                tooltip:{text:WtfGlobal.getLocaleText("acc.productList.editProductTT"),dtext:WtfGlobal.getLocaleText("acc.productList.editProductTT")},  //,etext:" Edit selected product details."},
                scope:this,
                iconCls:getButtonIconCls(Wtf.etype.menuedit),
                handler:this.showForm.createDelegate(this,[true])
            }));
            productArrEDSingleS.push(productArr.length-1);
        }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.create)) {
        productArr.push(this.prodCloneBttn = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.productList.cloneProduct"),//'Clone Products/Service',
            disabled:true,
            tooltip:{text:WtfGlobal.getLocaleText("acc.productList.cloneProductTT"),dtext:WtfGlobal.getLocaleText("acc.productList.cloneProductTT")},  //,etext:"Clone selected product."},
            scope:this,
            iconCls:'pwnd menu-clone',
            handler:this.showForm.createDelegate(this,[true,true])
        }));
        productArrEDSingleS.push(productArr.length-1);
    }
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.remove)) {
            productArr.push(this.prodDelBttn = new Wtf.Action({
                text:WtfGlobal.getLocaleText("acc.productList.deleteProduct"),//'Delete Products/Service',
                disabled:true,
                tooltip:{text:WtfGlobal.getLocaleText("acc.productList.deleteProductTT"),dtext:WtfGlobal.getLocaleText("acc.productList.deleteProductTT")},  //,etext:"Delete selected product details."},
                scope:this,
                iconCls:getButtonIconCls(Wtf.etype.menudelete),
                handler:this.confirmBeforeDeleteProduct.createDelegate(this,this.isPermDel=["false"])
            }));
            productArrEDMultiS.push(productArr.length-1);
            productArr.push(this.prodDelPermBttn = new Wtf.Action({
                text:WtfGlobal.getLocaleText("acc.productList.deleteProductPerm"),//'Delete Products/Service permanently',
                disabled:true,
                tooltip:{text:WtfGlobal.getLocaleText("acc.productList.deleteProductPermTT"),dtext:WtfGlobal.getLocaleText("acc.productList.deleteProductPermTT")},  //,etext:"Delete selected product details."},
                scope:this,
                iconCls:getButtonIconCls(Wtf.etype.menudelete),
                handler:this.confirmBeforeDeleteProduct.createDelegate(this,this.isPermDel=["true"])
            }));
            productArrEDMultiS.push(productArr.length-1);
        }
     // Activate De-activate Product
     this.activateProduct=new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.product.activateProduct"),
            scope: this,
            disabled:true,
            tooltip:WtfGlobal.getLocaleText("acc.product.activateProduct"),
            iconCls:getButtonIconCls(Wtf.etype.activate),
            handler:this.activateDeactivateProduct.createDelegate(this,this.activateDeactivate=["activate"])
        })
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.create)) {
        productArr.push(this.activateProduct);
        productArrEDSingleS.push(productArr.length-1);
    }
    this.deactivateProduct=new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.product.deactivateProduct"),
            scope: this,
            disabled:true,
            tooltip:WtfGlobal.getLocaleText("acc.product.deactivateProduct"),
            iconCls:getButtonIconCls(Wtf.etype.deactivate),
           handler:this.activateDeactivateProduct.createDelegate(this,this.activateDeactivate=["deactivate"])
        })
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.create)) {
        productArr.push(this.deactivateProduct);
        productArrEDSingleS.push(productArr.length-1);
    }    
        
          this.localSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.productList.searchText")+","+WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"),//'Search by Product Name',
            width: 130,
            field: 'productname',
            Store:this.productStore
        });
        btnArr.push(this.localSearch);

    if(productArr.length>0) {
        btnArr.push({
            text:WtfGlobal.getLocaleText("acc.productList.productMenu"),//'Products And Services Menu',
            tooltip:WtfGlobal.getLocaleText("acc.productList.productMenuTT"),  //{text:"Click here to add, edit, clone or delete a product."},
            id:"manageProducts3",//FixMe: remove hardcoded helpmodeid
            iconCls:'accountingbase product',
            menu:productArr
        });
    }
    
    btnArr.push(this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    }));
    
    
      this.productTypeFilterStore= new Wtf.data.SimpleStore({
        fields: ['id', 'name'],
            data : [['All','All'],['d8a50d12-515c-102d-8de6-001cc0794cfa','Inventory Part'],['e4611696-515c-102d-8de6-001cc0794cfa','Inventory Assembly'],['ff8080812f5c78bb012f5cfe7edb000c9cfa','Inventory Non-Sale'],['f071cf84-515c-102d-8de6-001cc0794cfa','NON_INVENTORY_PART'],['4efb0286-5627-102d-8de6-001cc0794cfa','SERVICE'],['a839448c-7646-11e6-9648-14dda97925bd','Job Work Inventory'],['a6a350c4-7646-11e6-9648-14dda97925bd','Job Work Assembly']]
    });


    this.productTypeFilterComboConfig = {
        hiddenName: "id",
        store: this.productTypeFilterStore,
        valueField: 'id',
        hideLabel: false,
        displayField: 'name',
        mode: 'local',
        typeAhead: true,
        selectOnFocus: true,
        triggerAction: 'all',
        scope: this
    };

    this.productTypeFilterCombo = new Wtf.common.Select(Wtf.applyIf({
        multiSelect: true,
        fieldLabel: WtfGlobal.getLocaleText("acc.productList.gridProduct") ,
        forceSelection: true,
//        extraFields: ['name'],
        value:'All',
        extraComparisionField: 'id', // type ahead search on product id as well.
        listWidth: Wtf.ProductComboListWidth,
        width: 150
    }, this.productTypeFilterComboConfig));

    this.productTypeFilterCombo.on('select', function (combo, productRec) {
        if (productRec.get('id') == 'All') {
            combo.clearValue;
            combo.setValue('All');
        } else if ( combo.getValue().indexOf('All') >= 0) {
            combo.clearValue();
            combo.setValue(productRec.get('id'));
        }
      this.LoadProducts();
    }, this);
    this.productTypeFilterCombo.on('UnSelect', this.LoadProducts,this);
    
    var isAddNormalPricingOptions = false;
    if (Wtf.account.companyAccountPref.productPricingOnBands && !Wtf.account.companyAccountPref.productPricingOnBandsForSales) {
        isAddNormalPricingOptions = true;
    } else if (!Wtf.account.companyAccountPref.productPricingOnBands && Wtf.account.companyAccountPref.productPricingOnBandsForSales) {
        isAddNormalPricingOptions = true;
    } else if (!Wtf.account.companyAccountPref.productPricingOnBands && !Wtf.account.companyAccountPref.productPricingOnBandsForSales) {
        isAddNormalPricingOptions = true;
    }
    var UpdatePriceRuleArr=[];
    if (isAddNormalPricingOptions) {
        UpdatePriceRuleArr.push(new Wtf.Action({
                text:WtfGlobal.getLocaleText("acc.productList.addNewPrice"),//'Add New Price',
                id:"addPrice3",
                tooltip:WtfGlobal.getLocaleText("acc.productList.addNewPriceTT"),  //{text:"Click here to add new price (Purchase Price & Sales Price) by selecting an available product.",dtext:"Select a product to add price.", etext:"Add price to the selected product."},
                iconCls :getButtonIconCls(Wtf.etype.add),
                handler: this.showPricelist.createDelegate(this, [false, undefined])
            })
        );  
            //default it will unable for mass update of price
     }
    
      this.UpdatePriceRule = new Wtf.Action({
                text:WtfGlobal.getLocaleText("acc.product.UpdatePriceRule"),
                tooltip:WtfGlobal.getLocaleText("acc.product.UpdatePriceRule"),
                disabled: false,
                iconCls :getButtonIconCls(Wtf.etype.add),
                scope: this,
                handler:function (){
                    this.createProductPriceRule();
                }
      });
      if (isAddNormalPricingOptions) {
          UpdatePriceRuleArr.push(this.UpdatePriceRule);
      }
      
      this.setPriceForBand = new Wtf.Action({
          text: WtfGlobal.getLocaleText("acc.field.setPriceForBand"), // "Set Price for Band",
          tooltip: WtfGlobal.getLocaleText("acc.field.setPriceForBand"), // "Set Price for Band",
          iconCls: getButtonIconCls(Wtf.etype.add),
          scope: this,
          disabled: true,
          handler: this.setPriceListForBandHandler.createDelegate(this)
      });
      if (Wtf.account.companyAccountPref.productPricingOnBands || Wtf.account.companyAccountPref.productPricingOnBandsForSales) {
          UpdatePriceRuleArr.push(this.setPriceForBand);
          bandArrEDSingleS.push(UpdatePriceRuleArr.length - 1);
      }
      
      this.setPriceForVolumeDiscount = new Wtf.Action({
          text: WtfGlobal.getLocaleText("acc.field.setPriceForPriceListVolumeDiscount"), // "Set Price for Price List - Volume Discount",
          tooltip: WtfGlobal.getLocaleText("acc.field.setPriceForPriceListVolumeDiscount"), // "Set Price for Price List - Volume Discount",
          iconCls: getButtonIconCls(Wtf.etype.add),
          scope: this,
          disabled: true,
          handler: this.setPriceForVolumeDiscountHandler.createDelegate(this)
      });
      if (Wtf.account.companyAccountPref.productPricingOnBands || Wtf.account.companyAccountPref.productPricingOnBandsForSales) {
          UpdatePriceRuleArr.push(this.setPriceForVolumeDiscount);
          bandArrEDSingleS.push(UpdatePriceRuleArr.length - 1);
      }
    
      this.UpdatePriceRuleMain = new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.productList.updatePrice"),
                tooltip:WtfGlobal.getLocaleText("acc.productList.updatePrice"),
                disabled: false,
                iconCls :getButtonIconCls(Wtf.etype.add),
                scope: this,
                menu:UpdatePriceRuleArr
      });
    if (!WtfGlobal.EnableDisable(Wtf.UPerm.product, this.permType.addprice) && !(Wtf.account.companyAccountPref.productPricingOnBands || Wtf.account.companyAccountPref.productPricingOnBandsForSales)) {
        btnArr.push(this.UpdatePriceRuleMain);
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.viewpricelist)){

//        btnArr.push(new Wtf.Toolbar.Button({
//            text:WtfGlobal.getLocaleText("acc.productList.priceListReport"),//'Price List Report',
//            disabled:true,
//            tooltip:WtfGlobal.getLocaleText("acc.rem.55"),  //{text:"Select a product to view price list.",dtext:"Select a product to view price list.", etext:"View price list report for the selected product."},
//            iconCls:'accountingbase pricelistbutton',
//            handler:this.showPriceReport.createDelegate(this)
//        }));

            btnArr.push(new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.productList.priceListReport"),//'Price List Report',
                tooltip:WtfGlobal.getLocaleText("acc.rem.55"),  //{text:"Select a product to view price list.",dtext:"Select a product to view price list.", etext:"View price list report for the selected product."},
                iconCls:'accountingbase pricelistbutton',
                menu : [{
                    text:WtfGlobal.getLocaleText("acc.field.GeneralPriceListReport"),
                    scope : this,
                    tooltip :{text : WtfGlobal.getLocaleText("acc.field.Clicktoviewgeneralpricelistreport")} ,
                    iconCls:'accountingbase pricelistmenuitem',
                    handler:this.showPriceReport.createDelegate(this)
                }, {
                    text:WtfGlobal.getLocaleText("acc.field.VendorPriceListReport"),
                    scope : this,
                    tooltip : {text : WtfGlobal.getLocaleText("acc.field.Clicktoviewvendorpricelistreport")},
                    iconCls:'accountingbase pricelistmenuitem',
                    handler:this.vendorPriceReport.createDelegate(this)
                }, {
                    text:WtfGlobal.getLocaleText("acc.field.CustomerPriceListReport"),
                    scope : this,
                    tooltip : {text : WtfGlobal.getLocaleText("acc.field.Clicktoviewcustomerpricelistreport")},
                    iconCls:'accountingbase pricelistmenuitem',
                    handler:this.custPriceReport.createDelegate(this)
                }]
            }));
        }
       
//        this.reserveBtn = new Wtf.Toolbar.Button({
//                text:'Reserve',
//                tooltip:'Reserve',
//                disabled: true,
//                scope: this
//        });
//        btnArr.push(this.reserveBtn);
        //btnArrEDSingleS.push(btnArr.length-1);
        if(Wtf.account.companyAccountPref.activateInventoryTab){
        this.thresholdBtn =new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.product.set.threshold.Limit.text"),
            tooltip:WtfGlobal.getLocaleText("acc.product.set.threshold.Limit.tooltip"),
            disabled: true,
            scope: this,
            handler:function (){
                this.callthresholdWindow();
            }
        });
        btnArr.push(this.thresholdBtn);
        btnArrEDSingleS.push(btnArr.length-1);
    }
       btnArr.push(WtfGlobal.getLocaleText("acc.invReport.type"),this.productTypeFilterCombo)
    
     this.syncToPmBtnArr = [];
       this.syncSelectedToPM = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.productmaster.synctopm.sync.selected.title"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.productmaster.synctopm.sync.selected.title"),
            disabled: true,
            iconCls:getButtonIconCls(Wtf.etype.sync),
           handler: function () {
                        this.syncProductsIntoPM(false);
                    }
        });
        this.syncAlltoPM = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.productmaster.synctopm.sync.all.title"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.productmaster.synctopm.sync.all.title"),
            iconCls:getButtonIconCls(Wtf.etype.sync),
        handler: function () {
            this.syncProductsIntoPM(true);
        }
        });
    this.syncToPmBtnArr.push(this.syncSelectedToPM);
    this.syncToPmBtnArr.push(this.syncAlltoPM);
    
    this.syncToPmBtn = new Wtf.Action({
        text: WtfGlobal.getLocaleText("acc.button.label.syncdatatoPM"),
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.button.label.syncdatatoPM"),
        iconCls:getButtonIconCls(Wtf.etype.sync),
        menu: this.syncToPmBtnArr,
        hidden:!Wtf.account.companyAccountPref.activateMRPManagementFlag
    });
    
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.syncproducts) && !(Wtf.account.companyAccountPref.standalone)) { 
        btnArr.push(new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.productList.dataSync"),//'Data Sync',
            iconCls:getButtonIconCls(Wtf.etype.sync),
             menu : [{
                    text:WtfGlobal.getLocaleText("acc.field.DataSyncToCRM"),
                    scope:this,
                    tooltip:WtfGlobal.getLocaleText("acc.productList.dataSyncTT"),  //"Data Syncing operation enables you to sync the Product List data from Accounting into CRM. Following fields from Accounting will be populated into CRM:<br>1. Product Name <br>2. Description <br>3. Category  <br>4. Vendor Name <br>5. Vendor Phone No.<br>6. Vendor Email ID <br>7. Purchase Price <br>8. Sales Price",
                    iconCls:getButtonIconCls(Wtf.etype.sync),                   
                    hidden:!Wtf.isCRMSync,
                    handler:function(){
                        if(!Wtf.account.companyAccountPref.activateCRMIntegration){
                             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.crmnotacivatedalert")],2);
                        } else {
                            this.syncProducts(false,false);
                        }                        
                    }
//            },{
//                    text:WtfGlobal.getLocaleText("acc.field.DataSyncToInventory"),
//                    scope : this,                    
//                    tooltip:WtfGlobal.getLocaleText("acc.productList.dataSyncTT"),  //"Data Syncing operation enables you to sync the Product List data from Accounting into CRM. Following fields from Accounting will be populated into CRM:<br>1. Product Name <br>2. Description <br>3. Category  <br>4. Vendor Name <br>5. Vendor Phone No.<br>6. Vendor Email ID <br>7. Purchase Price <br>8. Sales Price",
//                    iconCls:getButtonIconCls(Wtf.etype.sync),
//                    handler:this.syncProducts.createDelegate(this,[true])
//            },{
//                    text:WtfGlobal.getLocaleText("acc.field.DataSyncFromInventory"),
//                    scope : this,
//                    tooltip:WtfGlobal.getLocaleText("acc.productList.dataSyncTT"),  //"Data Syncing operation enables you to sync the Product List data from Accounting into CRM. Following fields from Accounting will be populated into CRM:<br>1. Product Name <br>2. Description <br>3. Category  <br>4. Vendor Name <br>5. Vendor Phone No.<br>6. Vendor Email ID <br>7. Purchase Price <br>8. Sales Price",
//                    iconCls:getButtonIconCls(Wtf.etype.sync),
//                    handler:this.syncProductsInv.createDelegate(this)                        
                },  
                {
                    text: WtfGlobal.getLocaleText("acc.productList.dataSychAll"),
                    scope: this,
                    tooltip: WtfGlobal.getLocaleText("acc.productList.dataSyncTT"), 
                    iconCls: getButtonIconCls(Wtf.etype.sync),
                    handler: this.syncProducts.createDelegate(this, [false, false, true])
                },
            
                {
                    text: WtfGlobal.getLocaleText("acc.field.DataSyncToPOS"),
                    scope: this,
                    tooltip: WtfGlobal.getLocaleText("acc.productList.dataSyncTT"), //"Data Syncing operation enables you to sync the Product List data from Accounting into CRM. Following fields from Accounting will be populated into CRM:<br>1. Product Name <br>2. Description <br>3. Category  <br>4. Vendor Name <br>5. Vendor Phone No.<br>6. Vendor Email ID <br>7. Purchase Price <br>8. Sales Price",
                    iconCls: getButtonIconCls(Wtf.etype.sync),
                    handler: this.syncProducts.createDelegate(this, [false, true, ,false])
                },
                {
                    text:WtfGlobal.getLocaleText("acc.field.DataSyncFromLMS"),
                    scope : this,
                    tooltip:WtfGlobal.getLocaleText("acc.productList.dataSyncFromLMSTT"),  //"Data Syncing operation enables you to sync the Product List data from Accounting into CRM. Following fields from Accounting will be populated into CRM:<br>1. Product Name <br>2. Description <br>3. Category  <br>4. Vendor Name <br>5. Vendor Phone No.<br>6. Vendor Email ID <br>7. Purchase Price <br>8. Sales Price",
                    iconCls:getButtonIconCls(Wtf.etype.sync),
                    hidden:!Wtf.isLMSSync,
                    handler:function(){
                        if(!Wtf.account.companyAccountPref.isLMSIntegration){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.lmsnotacivatedalert")],2);
                        }else {
                            this.syncProductsLMS();              
                        }                               
                    }  
            },this.syncToPmBtn /*{
                    text: WtfGlobal.getLocaleText("acc.button.label.syncdatatoPM"),
                    scope: this,
                    tooltip: WtfGlobal.getLocaleText("acc.productmaster.syncdata.selectatleastprodsuct.btn.ttip"), //"Please click on this  button to sync product's into PM. Before clicking this button please you have made product selection because only  selected product's would be synced.
                    iconCls: getButtonIconCls(Wtf.etype.sync),
                    hidden:!Wtf.account.companyAccountPref.activateMRPManagementFlag,
                      handler: function () {
                        this.syncProductsIntoPM();
                    }
                }*/]            
        }));
    }

//        btnArrEDMultiS.push(btnArr.length-1);
      //  btnArr.push("-");
//    var cyclecountButtonArr = [];
//    cyclecountButtonArr.push(new Wtf.Action({
//            text:WtfGlobal.getLocaleText("acc.productList.addCycleCount"),//'Add Cycle Count Entry',
//            tooltip:WtfGlobal.getLocaleText("acc.rem.61"),  //{text:"Click here to add cycle count entry."},
//            iconCls:getButtonIconCls(Wtf.etype.addcyclecount),
//            scope:this,
//            handler:callCycleCount
//        }));
//     cyclecountButtonArr.push(new Wtf.Action({
//            text:WtfGlobal.getLocaleText("acc.productList.cycleCountApprove"),//'Cycle Count Approve',
//            tooltip:WtfGlobal.getLocaleText("acc.rem.60"),  //{text:"Click here to view Cycle count approval."},
//            iconCls:getButtonIconCls(Wtf.etype.approvecyclecount),
//            scope:this,
//            handler:callCycleCountApproval
//        }));
//    cyclecountButtonArr.push(new Wtf.Action({
//            text:WtfGlobal.getLocaleText("acc.productList.cycleCountWorksheet"),//'Cycle Count Worksheet',
//            tooltip:WtfGlobal.getLocaleText("acc.rem.59"),  //{text:"Click here to view Cycle count worksheet."},
//            iconCls:getButtonIconCls(Wtf.etype.countcyclecount),
//            scope:this,
//            handler:callCycleCountWorksheet
//        }));
//    cyclecountButtonArr.push(new Wtf.Action({
//            text:WtfGlobal.getLocaleText("acc.productList.cycleCountReport"),//'Cycle Count Report',
//            tooltip:WtfGlobal.getLocaleText("acc.rem.58"),  //{text:"Click here to view Cycle count report."},
//            iconCls:getButtonIconCls(Wtf.etype.cyclecountreport),
//            scope:this,
//                handler:callCycleCountReport
//        }));
//   if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.cyclecount)) {
//        btnArr.push({
//            text:WtfGlobal.getLocaleText("acc.productList.cycleCount"),//'Cycle Count',
//            tooltip:WtfGlobal.getLocaleText("acc.rem.57"),  //{text:"Click here to manage Cycle Count of products available."},
//            iconCls:getButtonIconCls(Wtf.etype.cyclecount),
//            menu:cyclecountButtonArr
//        });
//   }
 
 
      var productDetails=[
        ['1',"All"], 
        ['2',"Exclude Deleted Record(s)"],
        ['3',"Deleted Record(s)"],
        ['4',"Available Quantity(s)"],
        ['5',"Active Product(s)"],
        ['6',"Dormant Product(s)"]
       ]
  
  
    this.ProductTypeStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'id'
        },{           
            name:'value'
        }],
        data :productDetails
        
    });
     this.showProducts = new Wtf.form.ComboBox({
        store: this.ProductTypeStore,
        mode: 'local',
        triggerAction: 'all',
        editable: false,
        emptyText:WtfGlobal.getLocaleText("acc.cust.EmptyTextSelectOneTime"),
        allowBlank: false,
        width: 150,
        valueField: 'id',
        displayField: 'value'
    });
    this.showProducts.setValue("1"); // 1-Default to show  All Product ,2-Means Exclude deleted Products,3-Means to show only deleted Products Ticket No:-8691
    this.showProducts.on('select', this.LoadProducts, this);
//    btnArr.push(this.showProducts);
    
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.inventoryreport)) {
        reportbtnArr.push(new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.productList.inventoryReport"),//'Inventory Report',
            id:"manageInventory3",
            disabled:true,
            tooltip:WtfGlobal.getLocaleText("acc.productList.inventoryReportTT"),  //{text:"Select a product to view inventory report.",dtext:"Select a product to view inventory report.", etext:"View inventory report for the selected product."},
            iconCls:'accountingbase inventoryreport',
            handler:this.showInventoryReport.createDelegate(this)
        }));
        reportbtnArrEDSingleS.push(reportbtnArr.length-1);
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.buildassembly)) {
        buildAssemblyArr.push(new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.productList.buildAssembly"),//'Build Assembly',
            tooltip: WtfGlobal.getLocaleText("acc.productList.buildAssemblyTT"),  //{text:"Click here to build stock of an assembly product by using Inventory Assembly."},
            scope:this,
            iconCls:getButtonIconCls(Wtf.etype.buildassemly),
            handler:function(){
                callBuildAssemblyForm(undefined, undefined, undefined, false);  //Unbuild Assembly check is false
            }
        }));
        buildAssemblyArr.push(new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.productList.unBuildAssembly"),//'Unbuild Assembly',
            tooltip: WtfGlobal.getLocaleText("acc.productList.unBuildAssemblyTT"),  //Click here to unbuild stock of an assembly product by using Inventory Assembly.
            scope:this,
            isUnbuildAssembly:true,
            iconCls:getButtonIconCls(Wtf.etype.buildassemly),
            handler:function(){
                callBuildAssemblyForm(undefined, undefined, undefined, true);   //Unbuild Assembly check is true
            }
        }));
        if(buildAssemblyArr.length>0) {
            reportbtnArr.push({
                text:WtfGlobal.getLocaleText("acc.productList.assemblyMenu"),   //Build Assembly & Unbuild Assembly Menu
                tooltip:WtfGlobal.getLocaleText("acc.productList.assemblyMenuTT"),  //Click here to get Build Assembly & Unbuild Assembly Menu
                id:"buildAssemble"+this,
                iconCls:'accountingbase product',
                menu:buildAssemblyArr
            });
        }
        buildAssemblyReportArr.push(new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.productList.buildAssemblyReport"), // 'Build Assembly Report',
            tooltip: WtfGlobal.getLocaleText("acc.productList.buildAssemblyTT"),  // {text:"Click here to build stock of an assembly product by using Inventory Assembly."},
            scope:this,
            id:"bulidreportbtn",
            iconCls:getButtonIconCls(Wtf.etype.buildassemly),
            handler:function(){
                callAssemblyReport(false);
            }
        }));
        buildAssemblyReportArr.push(new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.productList.unbuildAssemblyReport"), // 'Unbuild Assembly Report',
            tooltip: WtfGlobal.getLocaleText("acc.productList.unbuildAssemblyTT"),  // {text:"Click here to build stock of an assembly product by using Inventory Assembly."},
            scope:this,
            id:"unbulidreportbtn",
            iconCls:getButtonIconCls(Wtf.etype.buildassemly),
            handler:function(){
                callAssemblyReport(true);
            }
        }));
        buildAssemblyReportArr.push(new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.productList.bomWiseStockReport"), // 'Unbuild Assembly Report',
            tooltip: WtfGlobal.getLocaleText("acc.productList.bomWiseStockReportTT"),  // {text:"Click here to build stock of an assembly product by using Inventory Assembly."},
            scope:this,
            id:"bomreportbtn",
            iconCls:getButtonIconCls(Wtf.etype.buildassemly),
            handler:function(){
                callBOMWiseStockReport();
            }
        }))
        if(buildAssemblyReportArr.length>0) {
            reportbtnArr.push({
                text:WtfGlobal.getLocaleText("acc.productList.build.unbuild.report.assemblyMenu"),   //Build & Unbuild Assembly Report Menu
                tooltip:WtfGlobal.getLocaleText("acc.productList.build.unbuild.report.assemblyMenuTT"),  //Click here to get Build Assembly & Unbuild Assembly Report Menu
                id:"buildAssemblyreportmenu",
                iconCls:'accountingbase product',
                menu:buildAssemblyReportArr
            });
        }
    }   
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.valuation)) {
        reportbtnArr.push(new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.productList.inventoryValuation"),//'Inventory Valuation',
            tooltip:WtfGlobal.getLocaleText("acc.productList.inventoryValuationTT"),  //{text:"Click here to view Inventory Valuation Report of products available."},
            scope:this,
            iconCls:getButtonIconCls(Wtf.etype.inventoryval),
            handler:function(){
                showProductValuationTab();
            }
        }));
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.reorderproducts)) {
        reportbtnArr.push(new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.productList.reorderProducts"),//'Reorder Product(s)',
            tooltip:WtfGlobal.getLocaleText("acc.rem.56"),  //{text:"Click here to view and reorder product(s) which have reached the reorder level."},
            iconCls:getButtonIconCls(Wtf.etype.reorderreport),
            scope:this,
            handler:reorderProducts
        }));
    }
    //if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.viewpricelist)){
        reportbtnArr.push(new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.RevisionHistory"),
            disabled:true,
            tooltip:WtfGlobal.getLocaleText("acc.field.ManageRevisionHistory"),
            iconCls:'accountingbase pricelistbutton',
            menu : [{
                text:WtfGlobal.getLocaleText("acc.field.SetRevisionHistory"),
                id:'setRevisionHistoryButton'+this.id,
                tooltip:WtfGlobal.getLocaleText("acc.field.SetRevisionHistory"),
                scope:this,
                iconCls:'accountingbase pricelistmenuitem',
                handler:this.setCustomFieldRevisionHistory.createDelegate(this,[true])
            }, {
                text:WtfGlobal.getLocaleText("acc.field.ViewRevisionHistory"),
                scope : this,
                tooltip : WtfGlobal.getLocaleText("acc.field.ViewRevisionHistory"),
                iconCls:'accountingbase pricelistmenuitem',
                handler:this.viewRevisionHistory.createDelegate(this)
            }]
        }));
        
        reportbtnArrEDSingleS.push(reportbtnArr.length-1);
    //}
    
    reportbtnArr.push(new Wtf.Action({
        text:WtfGlobal.getLocaleText("acc.productquantity.details"),//'Product Quantity Details',
        tooltip:WtfGlobal.getLocaleText("acc.productquantity.detailsTT"),
        iconCls:getButtonIconCls(Wtf.etype.reorderreport),
        scope:this,
        handler:this.productQuantityDetailsHandler.createDelegate(this)
    }));
    
    this.tbar3=new Array();
    this.tbar3.push(reportbtnArr);
    this.tbar3.push("->", this.showProducts);
    this.sm = new Wtf.grid.CheckboxSelectionModel({
    	header: (Wtf.isIE7)?"":'<div class="x-grid3-hd-checker"> </div>'    // For IE 7 the all select option not available
    });
    
    var columnArr=[];
    columnArr.push(this.sm,this.expander,{
           header:WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),//"Product",acc.productList.gridProduct
           dataIndex:'productname',
           sortable: true,
           pdfwidth:75,
           renderer:function(val,m,rec) {   // ERP-13247 [SJ]
            val = val.replace(/(<([^>]+)>)/ig,"");
            var oldVal=val;
            if(rec.data.deleted){
                val="<del  wtf:qtip='"+oldVal+"' >"+oldVal+"</del>";
            }
            return "<a class='hirarchical' wtf:qtip='"+oldVal+"' href='#'>"+val+"</a>";
        }//WtfGlobal.deletedRenderer
         },{
             header:WtfGlobal.getLocaleText("acc.productList.gridProductID"),
             dataIndex:'pid',
             align:'left',
             pdfwidth:75,
             sortable: true,
             renderer:function(val,m,rec) {   // ERP-13247 [SJ]
                 val = val.replace(/(<([^>]+)>)/ig,"");
                 if(rec.data.deleted){
                     val='<del>'+val+'</del>';
                 }
                 return "<a class='jumplink' wtf:qtip='"+val+"' href='#'>"+val+"</a>";
            }//WtfGlobal.deletedRenderer
         },{
             header:WtfGlobal.getLocaleText("acc.productList.gridBarcode"),
             dataIndex:'barcode',
             align:'left',
             pdfwidth:75,
             sortable: true,
             renderer : function(val,m,rec) {
                 val = val.replace(/(<([^>]+)>)/ig,"");
                 if(rec.data.deleted)
                 val='<del>'+val+'</del>';
                 return "<div wtf:qtip=\""+val+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.productList.gridBarcode")+"'>"+val+"</div>";
            }
         },{
            header:WtfGlobal.getLocaleText("acc.productList.gridProductDescription"),// Product Description",
            dataIndex:'desc',
            sortable: true,
            renderer : function(val,m,rec) {
//                 val = val.replace(/(<([^>]+)>)/ig,"");
                 if(rec.data.deleted)
                 val='<del>'+val+'</del>';
                 return "<div wtf:qtip='"+val+"' wtf:qtitle='"+WtfGlobal.getLocaleText("acc.productList.gridProductDescription")+"'>"+val+"</div>";
            },
            pdfwidth:75
         }/*,{
            hidden:true,
            header:WtfGlobal.getLocaleText("acc.field.ProductUUID"),
            dataIndex:'productid',
            renderer : WtfGlobal.deletedRenderer            
         }*/,{
            header:WtfGlobal.getLocaleText("acc.masterConfig.uom"),
            dataIndex:'uomname',
            hidden:true,
            pdfwidth:75,
            renderer:WtfGlobal.deletedRenderer
         },{
            header:WtfGlobal.getLocaleText("acc.product.orderUoMLabel"),
            dataIndex:'orderinguomname',
            hidden:true,
            pdfwidth:75,
            renderer:WtfGlobal.deletedRenderer
         },{
            header:WtfGlobal.getLocaleText("acc.product.transferUoMLabel"),
            dataIndex:'transferuomname',
            hidden:true,
            pdfwidth:75,
            renderer:WtfGlobal.deletedRenderer
         },{
            header:WtfGlobal.getLocaleText("acc.productList.gridProductType"),//"Product Type",
            dataIndex:'type',
            pdfwidth:75,
            sortable: true,
            renderer: function(val,m,rec){
                if(rec.data.deleted)
                val='<del>'+val+'</del>';
                return val;
            }
        },{
            header: WtfGlobal.getLocaleText("acc.accreport.Status"), // "Status"
            dataIndex: 'isActiveItem',
            pdfwidth:110,
            sortable: true,
            renderer: function(val,m,rec){
                if(val){
                    return "Active";
                } else {
                    return "Dormant";
                }
            }
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridReorderQuantity"),//"Reorder Quantity",
            dataIndex:'reorderquantity',
            sortable: true,
            align:'right',
            renderer:this.unitRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridReorderLevel"),//"Reorder Level",
            dataIndex:'reorderlevel',
            sortable: true,
            align:'right',
            renderer:this.unitRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridLeadTime"),//"Lead Time(in days)",
            dataIndex:'leadtime',
            sortable: true,
            align:'right',
            renderer:this.LeadTimeRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.field.DefaultWarehouse"),//"Default Warehouse",
            dataIndex:"warehouseName",
            align:'right',
            sortable: true,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.field.Defaultlocation"),//"Default Location",
            dataIndex:"locationName",
            align:'right',
            sortable: true,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridAvailableQty"),//"Available Quantity",
            dataIndex:"quantity",
            align:'right',
            sortable: true,
            renderer:this.unitRenderer,
            pdfwidth:75
        },{
            header: WtfGlobal.getLocaleText("acc.stockavailability.UnderQA"),//"Under QA",
            dataIndex:"qaquantity",
            align:'right',
            sortable: true,
            hidden:!(Wtf.account.companyAccountPref.isQaApprovalFlowInDO || Wtf.account.companyAccountPref.isQaApprovalFlow),
            renderer:this.unitRenderer,
            pdfwidth:75
        },{
            header: WtfGlobal.getLocaleText("acc.stockavailability.UnderRepair"), // Under Repair
            dataIndex:"repairquantity",
            align:'right',
            sortable: true,
            hidden:!(Wtf.account.companyAccountPref.isQaApprovalFlowInDO || Wtf.account.companyAccountPref.isQaApprovalFlow),
            renderer:this.unitRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridLeasedQty"),//"Leased Quantity",
            dataIndex:"leasedQuantity",
            align:'right',
            renderer:this.unitRenderer,
            hidden:!Wtf.account.companyAccountPref.leaseManagementFlag,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridLockQuantity"),//"Lock Quantity
            dataIndex:'lockquantity',
            align:'right',
            renderer:this.formatQuantity,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridBalQty"),//"Balance Quantity",
            dataIndex:"balancequantity",
            align:'right',
            sortable: true,
            renderer:this.unitRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.field.ReserveStock"),//"Reserve Stock
            dataIndex:'reservestock',
            align:'right',
            renderer:this.unitRenderer,
            pdfwidth:75
        }, {
            header:WtfGlobal.getLocaleText("acc.productList.gridconsignQuantity"),//"Lock Quantity
            dataIndex:'consignquantity',
            align:'right',
            renderer:this.unitRenderer,
            pdfwidth:75
         },{
            header:WtfGlobal.getLocaleText("acc.productList.gridconsignvenQuantity"),//"vendor consignment quantity
            dataIndex:'venconsignquantity',
            align:'right',
            renderer:this.unitRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridRecycleQuantity"),//"ReCycle Quantity
            dataIndex:'recycleQuantity',
            align:'right',
            renderer:this.unitRenderer,
            pdfwidth:75
        },{
            header: WtfGlobal.getLocaleText("acc.product.initialPurchasePrice"), // "Initial Purchase Price",
            dataIndex: 'initialprice',
            align: 'right',
            hidden: true,
            pdfrenderer: 'unitpricecurrency',
            renderer: function(v,metadata,record) {
                if(!Wtf.dispalyUnitPriceAmountInPurchase){
                    return Wtf.UpriceAndAmountDisplayValue;
                } else{
                    return WtfGlobal.withCurrencyUnitPriceRenderer(v,false,record);
                }
            },
            pdfwidth: 75
        },{
            header: WtfGlobal.getLocaleText("acc.product.initialSalesPrice"), // "Initial Sales Price",
            align: 'right',
            hidden: true,
            dataIndex: 'initialsalesprice',
            pdfwidth: 75,
            pdfrenderer: 'unitpricecurrency',
            renderer: function(v,metadata,record) {
                if(!Wtf.dispalyUnitPriceAmountInSales){
                    return Wtf.UpriceAndAmountDisplayValue;
                } else if (record.data['type'] == "Inventory Non-Sale") {
                    return "N/A";
                } else { 
                    return WtfGlobal.withCurrencyUnitPriceRenderer(v,false,record);
                }
            }
        },{
            header: WtfGlobal.getLocaleText("acc.product.mrp"), // "Maximum Retail Price",
            align: 'right',
            hidden: true,
            dataIndex: 'mrprate',
            pdfwidth: 75,
            pdfrenderer: 'unitpricecurrency',
            renderer: function(v,metadata,record) {
                if(!Wtf.dispalyUnitPriceAmountInPurchase){
                    return Wtf.UpriceAndAmountDisplayValue;
                } else if (record.data['type'] == "Inventory Non-Sale") {
                    return "N/A";
                } else { 
                    return WtfGlobal.withCurrencyUnitPriceRenderer(v,false,record);
                }
            }
        }
//        },{
//            header:WtfGlobal.getLocaleText("acc.productList.gridCycleCountInterval"),//"Cycle count Interval",
//            align:'right',
//            dataIndex:'ccountinterval',
//            width:80,
//            pdfwidth:75,
//            renderer:function(v,metadata,record){
//                 var val1;
//		 if(record.data['type'] == "Service"){
//		 val1= "N/A";
//	        }else{ 
//                      val1=v+' days';}
//                if(record.data.deleted)
//                    val1='<del>'+val1+'</del>'; 
//                return val1;
//	    }
//        },{
//            header:WtfGlobal.getLocaleText("acc.productList.gridCycleCountTolerance"),//"Cycle count Tolerance",
//            align:'right',
//            dataIndex:'ccounttolerance',
//            width:80,
//            pdfwidth:75,
//            renderer:function(v,metadata,record){
//                    var val1;
//                    if(record.data['type'] == "Service"){
//                            val1= "N/A";
//                    }else{
//                            val1= v+' %';}
//                    if(record.data.deleted)
//                        val1='<del>'+val1+'</del>'; 
//                    return val1;
////            						 return'<div class="currency">'+v+'%</div>';}
//                 }
  );
        if (!Wtf.account.companyAccountPref.productPricingOnBandsForSales) {
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.agedPay.gridCurrent") + " " + WtfGlobal.getLocaleText("acc.productList.gridSalesPrice"), // "Current Sales Price",
            align: 'right',
            dataIndex: 'saleprice',
            pdfwidth: 75,
            pdfrenderer: 'unitpricecurrency',
            renderer: function (v, metadata, record) {
                if (!Wtf.dispalyUnitPriceAmountInSales) {
                    return Wtf.UpriceAndAmountDisplayValue;
                } else if (record.data['type'] == "Inventory Non-Sale") {
                    return "N/A";
                } else {
                    return WtfGlobal.withCurrencyUnitPriceRenderer(v, false, record);
                }
            }
        });
    }
    if (!Wtf.account.companyAccountPref.productPricingOnBands) {
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.agedPay.gridCurrent") + " " + WtfGlobal.getLocaleText("acc.productList.gridPurchasePrice"), // "Current Purchase Price",
            dataIndex: 'purchaseprice',
            align: 'right',
            pdfrenderer: 'unitpricecurrency',
            hidden: WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.showpurchaseprice),
            renderer: function (v, metadata, record) {
                if (!Wtf.dispalyUnitPriceAmountInPurchase) {
                    return Wtf.UpriceAndAmountDisplayValue;
                } else {
                    return WtfGlobal.withCurrencyUnitPriceRenderer(v, false, record);
                }
            },
            pdfwidth: 75
        });
    }
        
        columnArr.push({
        header: WtfGlobal.getLocaleText("acc.field.WarrantyPeriod(Days)"), //"Cycle count Tolerance",
        align: 'center',
        dataIndex: 'warranty',
        sortable: true,
        width: 80,
        pdfwidth: 75,
        renderer: WtfGlobal.deletedRenderer

    }, {
        header: WtfGlobal.getLocaleText("acc.product.generateBarcodUsing"),
        hidden: !Wtf.account.companyAccountPref.generateBarcodeParm,
        dataIndex: 'barcodefield',
        pdfwidth: 75
    }, {
        header: 'Total Issue Count',
        dataIndex: 'itemissuecount',
        pdfwidth: 75
    }, {
        header: WtfGlobal.getLocaleText("acc.product.supplier"), // "Supplier Part Number",
        dataIndex: 'supplier',
        hidden: !Wtf.account.companyAccountPref.partNumber,
        pdfwidth: 75
    }, {
        header: WtfGlobal.getLocaleText("acc.product.coilcraft"), // "Part Number",
        dataIndex: 'coilcraft',
        hidden: !Wtf.account.companyAccountPref.partNumber,
        pdfwidth: 75
    }, {
        header: WtfGlobal.getLocaleText("acc.product.interplant"), // "Customer Part Number",
        dataIndex: 'interplant',
        hidden: !Wtf.account.companyAccountPref.partNumber,
        pdfwidth: 75
    }, {
        header: WtfGlobal.getLocaleText("acc.product.valuationmethod"), // "Valuation Method",
        dataIndex: 'valuationmethod',
        pdfwidth: 75,
        renderer: function (v, metadata, record) {
            if (record.data['valuationmethod'] == 0) {
                return "LIFO";
            } else if (record.data['valuationmethod'] == 1) {
                return "FIFO";
            } else {
                return "Moving Average";
            }
        }
    }, {
        header: WtfGlobal.getLocaleText("acc.field.BuildAssemblyonSale"), // "Auto Build Assembly on Sale",
        dataIndex: 'autoAssembly',
        pdfwidth: 75,
        renderer: function (v, metadata, record) {
            if (record.data['type'] == "Inventory Assembly") {
                if (record.data['autoAssembly'] == true) {
                    return "Yes";
                } else {
                    return "No";
                }
            } else {
                return "N/A";
            }
        }
        },{
            header: WtfGlobal.getLocaleText("acc.masterConfig.53"), // "Product Brand",
            dataIndex: 'productBrandName',
            pdfwidth: 75
    },{
            header: WtfGlobal.getLocaleText("acc.product.LicenseType"), // "License Type",
            dataIndex: 'licensetype',
            pdfwidth: 75
    },{
        header: WtfGlobal.getLocaleText("acc.product.ItemReusability"), // "Item Reusability", //
        dataIndex: 'itemReusability',
        pdfwidth: 75,
        renderer: function (v, metadata, record) {
            if(record.data['itemReusability'] == "0") {
                return "Reusable";
            } else if (record.data['itemReusability'] == "1") {
                return "Consumable";
            }
        }
    });
    if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA) {
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.compref.india.rcm.applicable"), // "Auto Build Assembly on Sale",
            dataIndex: 'rcmapplicable',
            pdfwidth: 75,
            renderer: function(v, metadata, record) {
                if (v == true) {
                    return "Yes";
                } else {
                    return "No";
                }
            }
        });
    }
        
        if (CompanyPreferenceChecks.displayUOMCheck() == true) {
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.product.displayUoMLabel"),
            dataIndex: 'displayUoMName',
            hidden: CompanyPreferenceChecks.displayUOMCheck() ? false : true,
            pdfwidth: 75           
        })
    }
    
    /*------Column will be shown only for malaysian company for which "Map taxes at product level" feature is enabled----------  */
    if (CompanyPreferenceChecks.mapTaxesAtProductLevel() == true) {
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.purchase.Tax"),
            dataIndex: 'purchasetax',
            hidden: CompanyPreferenceChecks.mapTaxesAtProductLevel() ? false : true,
            pdfwidth: 75
        }, {
            header: WtfGlobal.getLocaleText("acc.sales.Tax"),
            dataIndex: 'salestax',
            hidden: CompanyPreferenceChecks.mapTaxesAtProductLevel() ? false : true,
            pdfwidth: 75
        })
    }
    /**
     * Added Line level dimesnion to Global dimension in Product and Service GRID for INDIA and US GST.
     * Example - Product Tax Class.
     * Added last three params in WtfGlobal.appendCustomColumn while calling this method
     */
    columnArr = WtfGlobal.appendCustomColumn(columnArr, GlobalColumnModelForReports[Wtf.Acc_Product_Master_ModuleId], true, undefined, undefined, undefined, Wtf.Acc_Product_Master_ModuleId);
    this.grid = new Wtf.grid.HirarchicalGridPanel({
        plugins: this.expander,
        store:this.productStore,
        sm:this.sm,
        border:false,
        hirarchyColNumber:1,
        id:"ProductReportGrid_one",
        layout:'fit',
        loadMask:true,
        viewConfig:{forceFit:false,emptyText:WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick='javascript:openProductWin()'>"+WtfGlobal.getLocaleText("acc.nee.23")+"</a>")},
        forceFit:true,
        columns:columnArr,
        tbar:btnArr
        //bbar:bottomArr
    });
     this.grid.on('render',
        function(){
            new Wtf.Toolbar({
                renderTo: this.grid.tbar,
                items:  this.tbar3
            });
               WtfGlobal.getReportMenu(this.grid.getTopToolbar(), Wtf.Acc_Product_Master_ModuleId, WtfGlobal.getModuleName(Wtf.Acc_Product_Master_ModuleId));
        },this)
        
   
   this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: this.moduleId,
        advSearch: false
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
   
        this.productStore.on('datachanged', function() {
            if(this.pageLimit.combo) {
                var p = this.pageLimit.combo.value;
                this.localSearch.setPage(p);
            }
        }, this);
   
   this.productStore.on("load", this.setPageSize, this);
            this.exportButton=new Wtf.exportButton({
                obj:this,
                id:'productlistexport',
                tooltip:WtfGlobal.getLocaleText("acc.common.exportTT"),  //"Export Report details.",  
                params:{name:WtfGlobal.getLocaleText("acc.prod.filename")},
                menuItem:{csv:true,pdf:true,rowPdf:false,xls:true},
                get:198,
                isProductExport:true,
                filename:WtfGlobal.getLocaleText("acc.prod.filename")+"_v1",
                label:WtfGlobal.getLocaleText("acc.field.ProductList1")
    }),
        
    
    this.exportButton.setParams({
            mode:22
         });
    this.exportselRec = new Wtf.exportButton({
        obj: this,
        id: "selproductlistexport",
        iconCls: 'pwnd exportpdfsingle',
//        isProductExport: true,
        get: 198,
        text: WtfGlobal.getLocaleText("acc.field.ExportSelRecord"), // + " "+ singlePDFtext,
        tooltip: WtfGlobal.getLocaleText("acc.field.ExportSelRecord"), //'Export selected record(s)'
        filename: WtfGlobal.getLocaleText("acc.prod.filename") + "_v1",
        disabled: true,
        params: {
            selproductIds: [],
            totalProducts:0
        },
        menuItem: {csv: true, xls: true,pdf:true}
    });
    this.exportselRec.on('click', function () {
        var selectionArr =  this.sm.getSelections();
        var idsArray = [];
        for (var i = 0; i < this.sm.getCount(); i++) {
            idsArray.push(selectionArr[i].data['productid']);
        }
        this.exportselRec.setParams({
            selproductIds : idsArray,
            totalProducts:idsArray.length
        });
    }, this);
    
    var checkbatchProperties = (Wtf.account.companyAccountPref.isWarehouseCompulsory || Wtf.account.companyAccountPref.isLocationCompulsory 
        || Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory
        || Wtf.account.companyAccountPref.isRowCompulsory|| Wtf.account.companyAccountPref.isRackCompulsory|| Wtf.account.companyAccountPref.isBinCompulsory);
    var importBtnArr = [];     
    
    this.moduleName = "Product";
    var extraConfig = {};
    extraConfig.url= "ACCProductCMN/importProduct.do";
    var extraParams = "";
//    this.importBtnArray= Wtf.importMenuArray(this, this.moduleName, this.productStore, extraParams, extraConfig);
//    this.importButton= Wtf.importMenuButtonA(this.importBtnArray, this, this.moduleName);
    
    var importProductbtnArray = Wtf.importMenuArray(this, this.moduleName, this.productStore, extraParams, extraConfig);
//    var importProductbtnArray = [];
//    importProductbtnArray.push(this.importProduct=new Wtf.Action({
//        text: WtfGlobal.getLocaleText("acc.field.ImportProduct"),
//        scope: this,
//        tooltip:WtfGlobal.getLocaleText("acc.import.csv"), 
//        iconCls: 'pwnd importcsv',
//        handler:callProductImportWin.createDelegate(this,[true,false])
//    }));
    
    this.importProductBtn = new Wtf.Action({
        text: checkbatchProperties ? "1. " + WtfGlobal.getLocaleText("acc.field.ImportProduct"):WtfGlobal.getLocaleText("acc.field.ImportProduct"),
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.field.ImportProduct"),
        iconCls: (Wtf.isChrome?'pwnd importProductChrome':'pwnd importcsv'),
        menu: importProductbtnArray
    });
    importBtnArr.push(this.importProductBtn);
    
    var productopeningqtyextraconfig = {};
    productopeningqtyextraconfig.url= "ACCProductCMN/importProductopeningqty.do";
    productopeningqtyextraconfig.isBookClosed = Wtf.isBookClosed;
    var importProductopeningqtybtnArray = Wtf.importMenuArray(this, "Product opening stock", this.productStore, "", productopeningqtyextraconfig);
    
    this.importProductopningBtn = new Wtf.Action({
        text: checkbatchProperties ? "2. " +WtfGlobal.getLocaleText("acc.field.importProductopeningqty"): WtfGlobal.getLocaleText("acc.field.importProductopeningqty"),
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.field.importProductopeningqty"),
        iconCls: (Wtf.isChrome? 'pwnd importProductChrome' : 'pwnd importcsv'),
        menu: importProductopeningqtybtnArray
    });
    if(checkbatchProperties){
        importBtnArr.push(this.importProductopningBtn);
        importBtnArr.push("-")
    }
    var priceEtraConfig = {};
    priceEtraConfig.url= "ACCProductCMN/importProductPrice.do";
    var importProductPriceListbtnArray = Wtf.importMenuArray(this, "Product Price List", this.productStore, "", priceEtraConfig);
    
    this.importProductPriceBtn = new Wtf.Action({
        text: WtfGlobal.getLocaleText("acc.field.importProductPrice"),
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.field.importProductPrice"),
        iconCls: (Wtf.isChrome? 'pwnd importProductChrome' : 'pwnd importcsv'),
        menu: importProductPriceListbtnArray
    });
    
    importBtnArr.push(this.importProductPriceBtn);
    


    var assemblyProductextraconfig = {};
    assemblyProductextraconfig.url = "ACCProductCMN/importAssemblyProduct.do";
    var importAssemblyProductBtnArray = Wtf.importMenuArray(this, "Assembly Product", this.productStore, "", assemblyProductextraconfig);
    this.importAssemblyProductBtn = new Wtf.Action({
        text:  !CompanyPreferenceChecks.withoutBOMCheck() ? WtfGlobal.getLocaleText("acc.import.assemblyproduct") : WtfGlobal.getLocaleText("acc.import.assemblyproductwithBOM"),    
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.import.assemblyproduct"),
        iconCls: (Wtf.isChrome? 'pwnd importProductChrome' : 'pwnd importcsv'),
        menu: importAssemblyProductBtnArray
    });
        
    importBtnArr.push(this.importAssemblyProductBtn);
    
    var extraConfigforwithoutbom = {};
    extraConfigforwithoutbom.withoutBOM=CompanyPreferenceChecks.withoutBOMCheck();
    extraConfigforwithoutbom.url = "ACCProductCMN/importAssemblyProduct.do";
    extraConfigforwithoutbom.bomlessfile = true;
    var importAssemblyProductWithoutBOMBtnArray = Wtf.importMenuArray(this, "Assembly Product", this.productStore, "", extraConfigforwithoutbom);
    this.importAssemblyProductWithoutBOMBtn = new Wtf.Action({
        id:this.id+"importassemblywithoutbom",
        text: WtfGlobal.getLocaleText("acc.import.assemblyproductwithoutBOM"),
        scope: this,
        hidden : !CompanyPreferenceChecks.withoutBOMCheck(),    
        tooltip: WtfGlobal.getLocaleText("acc.import.assemblyproductwithBOM.tooltip"),
        iconCls: (Wtf.isChrome? 'pwnd importProductChrome' : 'pwnd importcsv'),
        menu: importAssemblyProductWithoutBOMBtnArray
    });
    importBtnArr.push(this.importAssemblyProductWithoutBOMBtn);
     
    this.importBtn = new Wtf.Action({
        text: WtfGlobal.getLocaleText("acc.common.import"),
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.common.import"),
        iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import'),
        menu: importBtnArr
    });
    
    this.printButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        obj:this,
        tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
        params:{name:WtfGlobal.getLocaleText("acc.prod.filename")},
        menuItem:{print:true},
        filename:WtfGlobal.getLocaleText("acc.prod.filename"),
        get:198,
        label:WtfGlobal.getLocaleText("acc.cnList.prodList")
    });
    
    
        manageimage.push(new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.template.uploadimage"),
            
            tooltip:{text:WtfGlobal.getLocaleText("acc.template.uploadimage"),dtext:WtfGlobal.getLocaleText("acc.template.uploadimage")},
            scope:this,
            iconCls:'addImageIcon',
            handler : function() {
            this.Addanddeleteimage(1);
        }
        }));
        
        manageimage.push(new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.Productdetails.Deleteimage"),
            tooltip:{text:WtfGlobal.getLocaleText("acc.Productdetails.Deleteimage"),dtext:WtfGlobal.getLocaleText("acc.Productdetails.Deleteimage")},
            scope:this,
            iconCls:'addImageIcon',
            handler : function() {
                 
               Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('acc.common.confirm'), WtfGlobal.getLocaleText("acc.ProductDetails.deleteproductimagemsg")+"</br></br><b>"+WtfGlobal.getLocaleText('acc.customerList.delTT1')+"</b>", function(btn){
                    if(btn=="yes"){
                        this.Addanddeleteimage(2);
                    }
                }, this); 
            
        }
        }));
        
    
    this.addProductImage = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.Productdetails.ManageProductimage"),//'Add Product Image',
        iconCls: 'addImageIcon',
        scope: this,
        tooltip:WtfGlobal.getLocaleText("acc.Productdetails.ManageProductimage") ,
        disabled:false,
        menu:manageimage
    });    
    btnArrEDSingleS.push(this.addProductImage);
    
    this.viewProductDetailBtn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.invoiceList.expand.pDetails"),  // 'Product Details',
        tooltip :WtfGlobal.getLocaleText("acc.tooltip.viewproductdetails"),  // 'View Product Details',
        id: 'btnviewProductDetailBtn' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.reorderreport),
        disabled :false,
        handler : function() {
            this.viewProductDetails();
        }
    });
    this.viewProductTransactionDetailBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.productTransactionDetailReport"), // 'Product Details',
        tooltip: WtfGlobal.getLocaleText("acc.tooltip.viewproducttransactiondetails"), // 'View Product Details',
        id: 'btnviewProductTransactionDetailBtn' + this.id,
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.reorderreport),
        disabled: false,
        handler: function() {
            this.viewProductTransactionDetails();
        }
    });
     this.generateBarcodBtn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.product.generateBarcod"),  
        tooltip :WtfGlobal.getLocaleText("acc.tooltip.generateBarcod"), 
        id: 'generateBarcodBtn' + this.id,
        scope: this,
        hidden:!Wtf.account.companyAccountPref.generateBarcodeParm,
        iconCls :getButtonIconCls(Wtf.etype.reorderreport),
        disabled :false,
        handler : function() {
//            if(Wtf.getCmp('barcode-win'+this.id) == undefined){
                this.generateBarcods();
//            }else{
//                return;
//            }
        }
    });
  
    this.exportForImportButton=new Wtf.Toolbar.Button({
        text:'Export For Import',
        tooltip:'Export Product data to import',           
        id:'exportforimport'+this.id,
        scope:this,
        iconCls: 'pwnd importcsv',
        handler:this.exportForImport,
        hidden:true //After compliting import functionality I will show this fuctinality 
    });
    
    //btnArr.push(this.importButton);
//    btnArr.push(this.exportButton);
//    btnArr.push(this.importProductBtn);
//    btnArr.push(this.printButton);
    btnArr.push("->");
    btnArr.push(getHelpButton(this,3));
     
    this.pageLimit = new Wtf.forumpPageSize({
            ftree:this.grid,
            recordsLimit: Wtf.MaxPageSizeLimit
        });
        this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: this.msgLmt,
            id: "pagingtoolbar" + this.id,
            store: this.productStore,
            searchField: this.localSearch,
//            displayInfo: true,
//            displayMsg: 'Displaying records {0} - {1} of {2}',
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
            plugins: this.pageLimit,
            items:['-',(!WtfGlobal.EnableDisable(this.uPermType, this.permType.exportproducts))?this.exportButton:'','-',(!WtfGlobal.EnableDisable(this.uPermType, this.permType.exportproducts))?this.exportselRec:'','-',(!WtfGlobal.EnableDisable(this.uPermType, this.permType.importproduct))?this.importBtn:'','-',(!WtfGlobal.EnableDisable(this.uPermType, this.permType.printproducts))?this.printButton:'',this.addProductImage,this.viewProductDetailBtn,this.viewProductTransactionDetailBtn,this.generateBarcodBtn]
        })
        
        this.bbar = this.pagingToolbar;
     
    Wtf.account.ProductDetailsPanel.superclass.constructor.call(this,config);
    this.sm.on("selectionchange",function(){
        WtfGlobal.enableDisableBtnArr(btnArr, this.grid, btnArrEDSingleS, btnArrEDMultiS);
        WtfGlobal.enableDisableBtnArr(reportbtnArr, this.grid, reportbtnArrEDSingleS, reportbtnArrEDMultiS);
        WtfGlobal.enableDisableBtnArr(productArr, this.grid, productArrEDSingleS, productArrEDMultiS);
        WtfGlobal.enableDisableBtnArr(UpdatePriceRuleArr, this.grid, bandArrEDSingleS, bandArrEDMultiS);
        this.delIA = false;
        var arr=this.grid.getSelectionModel().getSelections();
        if(arr.length > 0){
            this.syncSelectedToPM.enable(); 
        }else{
            this.syncSelectedToPM.disable();
        }
                /*
                 *enableDisable Delete Button
                 */
        for(var i=0;i<arr.length;i++){
            if(arr[i]&&arr[i].data.deleted){
                if(this.prodDelBttn){this.prodDelBttn.disable();}
                /* If records are temp. deleted then edit and clone buttons are disable */
                if(this.prodEditBttn){this.prodEditBttn.disable();}
                if(this.prodCloneBttn){this.prodCloneBttn.disable();} 
                if(this.prodDelPermBttn){this.prodDelPermBttn.enable();}
            }
        }
        
        if(arr.length==1){
            this.recid=this.grid.getSelectionModel().getSelections()[0].data.productid;
            this.acccode=this.grid.getSelectionModel().getSelections()[0].data.pid;
            this.getDetailPanel();
        }
        for(var cnt=0; cnt<this.sm.selections.length; cnt++) {
            var ptype = this.sm.selections.items[cnt].data.type;
            var pqty = this.sm.selections.items[cnt].data.quantity;
            if(ptype == "Inventory Assembly" && pqty != 0) {						
            	this.delIA = true;
                break;
            }
        }     
        var selectionArr =  this.sm.getSelections();
        var idsArray = [];
        for (var i = 0; i < this.sm.getCount(); i++) {
            idsArray.push(selectionArr[i].data['productid']);
        }
        this.exportselRec.setParams({
            selproductIds: idsArray,
            totalProducts: idsArray.length
        });
        if (selectionArr.length >= 1) {
            if (this.exportselRec)
                this.exportselRec.enable();
        } else {
            if (this.exportselRec)
                this.exportselRec.disable();
        }
        //Below enableDisableActivateDeactivateProductBtn() function is used to handle enable/disable of activateProduct button
        var selRecArr = this.grid.getSelectionModel().getSelections();
        this.enableDisableActivateDeactivateProductBtn(selRecArr);
    },this);
    this.getMyConfig();
    this.grid.on("render", function(grid) {
        WtfGlobal.autoApplyHeaderQtip(grid);
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
            this.grid.on('statesave', this.saveMyStateHandler, this);
        }, this);
    },this);
    this.grid.on('cellclick',this.onCellClick, this);
}

Wtf.extend(Wtf.account.ProductDetailsPanel,Wtf.Panel,{
    setWorkCenterID: function (wcid) {
        this.wcid = wcid;
    },
    updateGrid: function(obj,productID){
        this.productID=productID;
        this.productStore.reload();
        this.addNew=true;
        this.productStore.on('load',this.colorRow,this)
    },
    colorRow: function(store){
        if(this.addNew){
            var recArr=[];
            recArr.push(store.getAt(store.find('productid',this.productID)));
            WtfGlobal.highLightRowColor(this.grid,recArr[0],true,0,0);
            this.addNew=false;
        }
    },
    calllinkRowColor:function(id){
        var index=this.productStore.find('productid',id );
         var rec=this.productStore.getAt(index);
         if(index>=0)
            WtfGlobal.highLightRowColor(this.grid,rec,true,0,0);
   },
   hideMsg: function(store){
       //save form success message is not hide ERP-24613
        if (!this.isFromSave) {
            Wtf.MessageBox.hide(); 
        } else {
            this.isFromSave = false;
        }
         Wtf.dirtyStore.product = false;
         if(this.productlinkid!=undefined)
             this.calllinkRowColor(this.productlinkid);
         
         this.localSearch.StorageChanged(store);
//         this.hideTransactionReportFields(Wtf.account.HideFormFieldProperty.productForm,store);
    },
        
//    hideTransactionReportFields: function(array,store) {
//        if (array) {
//            var cm = this.grid.getColumnModel();
//            for (var i = 0; i < array.length; i++) {
//                for (var j = 0; j < cm.config.length; j++) {
//                    if (cm.config[j].header == array[i].dataHeader || (cm.config[j].dataIndex == array[i].fieldId)) {
//                        cm.setHidden(j, array[i].isReportField);
//                    }
//                }
//            }
//            this.grid.reconfigure(store, cm);
//        }
//    }, 
        
    createProductPriceRule: function(masterid) {
        this.showUpdatePriceRuleWindow = new Wtf.updateSalesPriceRuleWindow({
               scope:this
        })
        this.showUpdatePriceRuleWindow.show();

    },
    callthresholdWindow : function(){
        
       var prodId=this.sm.getSelected().data.productid;
       var prodCode=this.sm.getSelected().data.productname;
       
       this.thresholdWindow = new Wtf.ProductThresholdFormWin({
               grid:this.grid,
               ProductId:prodId,
               windowTitle:WtfGlobal.getLocaleText("acc.product.set.threshold.Limit.text"),
               windowDetail:WtfGlobal.getLocaleText("acc.product.set.threshold.Limit.Product")+prodCode
       });
       
       this.thresholdWindow.show();
       
    },
    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.productStore.load({
                params: {
                    start:0,
                    limit:this.pP.combo.value
                }
            });
        }
    },
    LoadProducts: function() {
            this.productStore.load(
            {
                params: {
                   start: 0,
                   limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt
                }
            });

    },
    unitRenderer:function(value,metadata,record){
        if(record.data['type'] == "Service" || record.data['type'] == "Non-Inventory Part") {
        	return "N/A";
        }
    	var unit=record.data['uomname'];
            value=parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+" "+unit;
       if(record.data.deleted)
                value='<del>'+value+'</del>';    
        return value;
    },
    formatQuantity: function(val, m, rec, i, j, s) {
        if (rec.data['type'] == "Service" || rec.data['type'] == "Non-Inventory Part") {
            return "N/A";
        }
        var unit = rec.data['uomname'];
        var value = parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        if (isNaN(value)) {
            return val;
        }
        val = WtfGlobal.convertQuantityInDecimalWithLink(value, unit);
        return val;
    },
    onCellClick:function(g,i,j,e){
        var record = g.getStore().getAt(i);//In report click on RichTextArea to show content
        var fieldName= g.getColumnModel().getDataIndex(j);
        if(e.getTarget(".richtext")){
            var value = record.get(fieldName);
            new Wtf.RichTextArea({
                rec:record,
                fieldName:fieldName,
                val: value?value:"",
                readOnly:true
            });
        } 
        var el=e.getTarget("a");
        if(el==null) {
            return;
        }
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="lockquantity"){
            this.viewTransection(g,i,e)
        }
        
        if(header == "productname" || header == "pid"){
//            if(Wtf.account.companyAccountPref.columnPref.isConfiguredProductView){
//                getConfiguredProductView(record.get("productid"));
//            }else{
                Wtf.onCellClickProductDetails(record.data.productid);
//            }
        }
    },
    viewTransection:function(grid, rowIndex, columnIndex){
        if(this.grid.getSelections().length>1){
            return;
        }
        var formrec=null;
    if(rowIndex<0&&this.grid.getStore().getAt(rowIndex)==undefined ||this.grid.getStore().getAt(rowIndex)==null ){
            WtfComMsgBox(15,2);
            return;
        }
    formrec = this.grid.getStore().getAt(rowIndex);
        var productid=formrec.get('productid');
            callSalesByProductAgainstSalesOrder(true,productid);
    },
    LeadTimeRenderer:function(value,metadata,record){
    	if(record.data['type'] == "Service"){
        	return "N/A";
        }
    	if(value==1){
            value=value+" Day";
        }
        else{
            value=value+" Days";
        }
        if(record.data.deleted)
                value='<del>'+value+'</del>';   
        return value;
    },
    
    showPricelist: function(isEdit, priceRec) {
        callMasterPricelistWindow("", isEdit, priceRec); // call the function for mass update of price
        Wtf.getCmp("pricewindow").on('update',function(){this.productStore.reload();},this);
    },
    
    setCustomFieldRevisionHistory:function(){
         var rec=null;
         if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
             WtfComMsgBox(17,2);
             return;
         }else{
             rec=this.grid.getSelectionModel().getSelected();
             callCustomFieldSetHistoryWindow(rec,"customfieldsethistorywindow");
//             Wtf.getCmp("pricewindow").on('update',function(){this.productStore.reload();},this);
         }
    },
    
    viewRevisionHistory:function(){
      var sm = this.grid.getSelectionModel();
      if(sm.hasSelection()) {
          var productid = sm.getSelected().data.productid;
          var product = sm.getSelected().data.productname;
          callCustomFieldHistorReportForProduct(productid, product);
      } else {
          Wtf.MessageBox().alert(WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.field.PleaseselectaProducttodisplayreport"));
      }
    },
    
    productQuantityDetailsHandler:function(){
        callProductQuantityDetailsReport(true);//sending argument true when calling from product master
    },
    
    setPriceListForBandHandler: function() {
        var record = this.grid.getSelectionModel().getSelected();
        
        callSetPriceListForBandWindow(record,this.productStore);
    },
    
    setPriceForVolumeDiscountHandler: function() {
        var record = this.grid.getSelectionModel().getSelected();
        
        callsetPriceForVolumeDiscountWindow(record,this.productStore);
    },
    
    Addanddeleteimage: function (flag) {
        var s = this.grid.getSelectionModel().getSelections();
        if (s.length == 1) {
            var productid = s[0].data.productid;
            if (flag == 1) {//flag 1 for uploading the image 
                var uploadImageWin = new Wtf.UploadImage({
                    idX: this.id2,
                    grid: this.grid,
                    recid: productid,
                    keyid: this.keyid,
                    mapid: this.mapid,
                    scope: this,
                    moduleName: this.moduleName,
                    selectedRec: this.selectedRec,
                    isDetailPanel: false
                });
                uploadImageWin.show();
            } else if(flag ==2){//flaf 2 for deleting the image 
                Wtf.Ajax.requestEx({
                    url: "ACCProduct/deleteImage.do",
                    params: {
                        fileid:productid
                    }
                }, this, function (res) {
                    if(res.success==true){
                    WtfComMsgBox(['success', res.msg]);
                    this.grid.reload;
                }else{
                    WtfComMsgBox(['warning', res.msg]);
                }
                }, Wtf.genFailureResponse);
            }
        } else {
            var msg = WtfGlobal.getLocaleText("acc.uploadImageWarnigMessage");            
            if(flag == 2){
                msg = WtfGlobal.getLocaleText("acc.ProductDetails.deleteImageWarnigMessage");
            }
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg]);            
        }
    },
    
    viewProductDetails:function() {
        var s=this.grid.getSelectionModel().getSelections();
        if(s.length==1) {
                      Wtf.Ajax.requestEx({
                url:"ACCProduct/getImagePath.do",
                params: {
                    fileid:s[0].data.productid
                }
            },this,function(res){
                 callProductProfilerTab(s[0],res.fileName);
            },Wtf.genFailureResponse);  
           
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.viewProductWarnigMessage")]);
        }
    },
    viewProductTransactionDetails: function() {
        var s = this.grid.getSelectionModel().getSelections();
        if (s.length == 1) {
            callProductTransactionDetail(s[0].data.productid);
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.viewProductWarnigMessage")]);
        }
    },
    generateBarcods:function(){
        var valid=this.validateBarcode();
        if(valid){
            this.createSerialWinFields();
            var s=this.grid.getSelectionModel().getSelections();

            this.prodIds=[];
                for(var i=0;i<s.length;){
                    this.prodIds.push(s[i].data.pid);
                    i++;
                }

            var opt=s[0].data.barcodefield;
    //        if(opt==Wtf.BarcodeGenerator_ProductId){//productid
                this.barcodeWindow = new Wtf.Window({
//                        id:'barcode-win'+this.id,
                        layout:'fit',
                        width:500,      
                        height:300,
                        closeAction:'hide',
                        plain: true,
                        modal: true,
                        scope:this,
                        iconCls :getButtonIconCls(Wtf.etype.deskera),
                        title: WtfGlobal.getLocaleText("acc.product.generateBarcod"),
                        items:[(opt==Wtf.BarcodeGenerator_ProductId || opt==Wtf.BarcodeGenerator_Barcode) ? this.bProductGrid : (opt==Wtf.BarcodeGenerator_BatchID ? this.batchGrid : this.serialGrid) ]  ,

                        buttons: [{
                            text:'Submit',
                            scope:this,
                            handler:function(){
                            this.arrRec=[];
                            this.barcdnos=[];
                            this.barcodeName=[];
                            this.productsID=[];
                            this.prodPrice=[];
                            this.barcdQty=[];
                            this.prodMrp=[];
                            this.notselected=false;
                            if(opt==Wtf.BarcodeGenerator_ProductId){
                                this.arrRec = this.bProdSm.getSelections();
                                if(this.bProdSm.getCount()!=0){
                                    for(var i=0;i<this.bProdSm.getCount();i++){
                                        this.barcdnos.push(this.arrRec[i].data["prodid"]);
                                        this.productsID.push(this.arrRec[i].data["prodid"]);
                                        this.barcodeName.push(this.arrRec[i].data["prodnm"]);
                                        this.barcdQty.push(this.arrRec[i].data["barcdqty"])
                                        this.prodPrice.push(this.arrRec[i].data["prodPrice"]);
                                        this.prodMrp.push(this.arrRec[i].data["mrpofprod"]);
                                    }       
                                }else{
                                this.notselected=true;
                                }  
                            } else if(opt==Wtf.BarcodeGenerator_Barcode){
                                this.arrRec = this.bProdSm.getSelections();
                                if(this.bProdSm.getCount()!=0){
                                    for(var i=0;i<this.bProdSm.getCount();i++){
                                        this.barcdnos.push(this.arrRec[i].data["barcode"]);
                                        this.productsID.push(this.arrRec[i].data["prodid"]);
                                        this.barcodeName.push(this.arrRec[i].data["prodnm"]);
                                        this.barcdQty.push(this.arrRec[i].data["barcdqty"])
                                        this.prodPrice.push(this.arrRec[i].data["prodPrice"]);
                                        this.prodMrp.push(this.arrRec[i].data["mrpofprod"]);
                                    }       
                                }else{
                                this.notselected=true;
                                }  
                            }else{
                            this.arrRec = this.serialSm.getSelections();
                            if(this.serialSm.getCount()!=0){
                                if(opt==Wtf.BarcodeGenerator_SerialId){
                                    for(var i=0;i<this.serialSm.getCount();i++){
                                        this.barcdnos.push(this.arrRec[i].data["serialno"]);
                                        this.productsID.push(this.arrRec[i].data["prodid"]);
                                        this.barcodeName.push(this.arrRec[i].data["prodnm"]);
                                        this.prodPrice.push(this.arrRec[i].data["prodPrice"]);
                                        this.prodMrp.push(this.arrRec[i].data["mrpofprod"]);
                                    }       
                                }else if(opt==Wtf.BarcodeGenerator_BatchID){
                                    for(var i=0;i<this.serialSm.getCount();i++){
                                        var batchbarcdno = (this.arrRec[i].data["prodid"] +" "+this.arrRec[i].data["batchno"]); //Barcode on "Product ID+Batch No."
                                        this.barcdnos.push(batchbarcdno);
                                        this.productsID.push(this.arrRec[i].data["prodid"]);
                                        this.barcodeName.push(this.arrRec[i].data["prodnm"]);
                                        this.barcdQty.push(this.arrRec[i].data["barcdqty"])
                                        this.prodPrice.push(this.arrRec[i].data["prodPrice"]);
                                        this.prodMrp.push(this.arrRec[i].data["mrpofprod"]);
                                    }       
                                }else if(opt==Wtf.BarcodeGenerator_SKUField){
                                    for(var i=0;i<this.serialSm.getCount();i++){
                                        this.barcdnos.push(this.arrRec[i].data["skuno"]);
                                        this.productsID.push(this.arrRec[i].data["prodid"]);
                                        this.barcodeName.push(this.arrRec[i].data["prodnm"]);
                                        this.prodPrice.push(this.arrRec[i].data["prodPrice"]);
                                        this.prodMrp.push(this.arrRec[i].data["mrpofprod"]);
                                    }       
                                }
                            }else{
                                this.notselected=true;
                            }
                            }
                            if(!this.notselected){
                                Wtf.Ajax.requestEx({
                                    url:"ACCBarcodeCMN/generateBarcodeUsingSerialNos.do",
                                    params:{
                                        base:opt,
                                        barcodeNos:this.barcdnos,
                                        barcodeName:this.barcodeName,
                                        productsID:this.productsID,
                                        quantity:this.barcdQty,
                                        prodPrice:this.prodPrice,
                                        mrpOfProd:this.prodMrp,
                                        barcodeType:Wtf.account.companyAccountPref.barcodetype, 
                                        dpi:Wtf.account.companyAccountPref.barcodeDpi,
                                        height:Wtf.account.companyAccountPref.barcodeHeight,
                                        barcdLabelHeight : Wtf.account.companyAccountPref.barcdLabelHeight
                                    }
                                },this,function(res,req){
                                    if(res.success){
                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),WtfGlobal.getLocaleText("acc.rem.244")],0);
                                        if (res.data && res.data.length>0) {
                                            var newwin=window.open('','mywindow','left=0,top=0,menubar=1,resizable=1,scrollbars=1' )
                                            newwin.document.write('<HTML>\n<HEAD><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n')
                                            newwin.document.write('<TITLE>Print Page</TITLE>\n')
                                            newwin.document.write('<style type=\"text/css\">@media print{}</style>\n')
                                            newwin.document.write('<script type=\"text/javascript\"></script>\n')
                                            newwin.document.write('<script>\n')
                                            newwin.document.write('function chkstate(){\n')
                                            newwin.document.write('if(document.readyState=="complete"){\n')
                                            newwin.document.write('window.close()\n')
                                            newwin.document.write('}\n')
                                            newwin.document.write('else{\n')
                                            newwin.document.write('setTimeout("chkstate()",2000)\n')
                                            newwin.document.write('}\n')
                                            newwin.document.write('}\n')
                                            newwin.document.write('function print_win(){\n')
                                            newwin.document.write('window.print();\n')
                                            newwin.document.write('chkstate();\n')
                                            newwin.document.write('}\n')
                                            newwin.document.write('<\/script>\n')
                                            newwin.document.write('</HEAD>\n')
    //                                        newwin.document.write('<BODY onload="print_win()">\n')    //ERP-20372
                                            newwin.document.write('<BODY>\n')
                                            newwin.document.write('<table cellpadding=0 cellspacing=0>\n')
                                            for(var i=0;i<res.data.length;i++){
                                                res.data[i].imgName = res.data[i].imgName.replace(/\//g,"-");
                                                newwin.document.write('<tr>\n')
                                                newwin.document.write('<td>\n') 
                                                newwin.document.write('<Div style="text-align:center; margin-left:'+Wtf.account.companyAccountPref.barcdLeftMargin+'px;margin-top:'+Wtf.account.companyAccountPref.barcdTopMargin+'px; height: '+Wtf.account.companyAccountPref.barcdLabelHeight+'mm">\n')                             
                                                newwin.document.write(  '<tpl for=".">',
                                                    '<div style="display:block;"><img src="productimage?fname='+res.data[i].imgName+'" alt="No Image has uploaded"></div>',            
                                                    '</tpl>');
                                                if(Wtf.account.companyAccountPref.generateBarcodeWithPnameParm){ 
                                                    //newwin.document.write('<td>\n') 
                                                    newwin.document.write('<Div style="font-size:'+Wtf.account.companyAccountPref.pnameFontSize+';transform: rotate('+Wtf.account.companyAccountPref.pnamePrintType+'deg) translate('+Wtf.account.companyAccountPref.pnameTranslateX+'px,'+(Wtf.account.companyAccountPref.pnameTranslateY)+'px);transform-origin: 0 0;-webkit-transform-origin: 0 0;">\n')
                                                    if(Wtf.account.companyAccountPref.pnamePrefix==undefined || Wtf.account.companyAccountPref.pnamePrefix==null || Wtf.account.companyAccountPref.pnamePrefix==""){
                                                        newwin.document.write(res.data[i].prodName+'\n')
                                                    }else{
                                                        newwin.document.write(Wtf.account.companyAccountPref.pnamePrefix + res.data[i].prodName+'\n')
                                                    }
                                                    newwin.document.write('</Div>\n')
                                                }   
                                                if(Wtf.account.companyAccountPref.generateBarcodeWithPidParm){ 
                                                    //newwin.document.write('<td>\n') 
                                                    newwin.document.write('<Div style="font-size:'+Wtf.account.companyAccountPref.pidFontSize+';transform: rotate('+Wtf.account.companyAccountPref.pidPrintType+'deg) translate('+Wtf.account.companyAccountPref.pidTranslateX+'px,'+(Wtf.account.companyAccountPref.pidTranslateY)+'px);transform-origin: 0 0;-webkit-transform-origin: 0 0;">\n')
                                                    if(Wtf.account.companyAccountPref.pidPrefix==undefined || Wtf.account.companyAccountPref.pidPrefix==null || Wtf.account.companyAccountPref.pidPrefix==""){
                                                        newwin.document.write(res.data[i].productsID+'\n')
                                                    }else{
                                                        newwin.document.write(Wtf.account.companyAccountPref.pidPrefix + res.data[i].productsID+'\n')
                                                    }
                                                    newwin.document.write('</Div>\n')
                                                }
                                                newwin.document.write('</Div>\n')
                                                newwin.document.write('</td>\n') 

                                                /* Setting positions of Max Retail Price while generating Barcode of Product*/
                                                if (Wtf.account.companyAccountPref.generateBarcodeWithmrpParm) {

                                                        newwin.document.write('<Div style="font-size:' + Wtf.account.companyAccountPref.mrpFontSize + ';transform: rotate(' + Wtf.account.companyAccountPref.mrpPrintType + 'deg) translate(' + Wtf.account.companyAccountPref.mrpTranslateX + 'px,' + (Wtf.account.companyAccountPref.mrpTranslateY) + 'px);transform-origin: 0 0;-webkit-transform-origin: 0 0;">\n')
                                                        if (Wtf.account.companyAccountPref.mrpPrefix == undefined || Wtf.account.companyAccountPref.mrpPrefix == null || Wtf.account.companyAccountPref.mrpPrefix == "") {
                                                            newwin.document.write(res.data[i].mrpOfProd + '\n')
                                                        } else {
                                                            newwin.document.write(Wtf.account.companyAccountPref.mrpPrefix +" "+res.data[i].mrpOfProd + '\n')
                                                        }
                                                        newwin.document.write('</Div>\n')
                                                    }
                                                    newwin.document.write('</Div>\n')
                                                    newwin.document.write('</td>\n') 
                                                if(Wtf.account.companyAccountPref.generateBarcodeWithPriceParm){ 
                                                    newwin.document.write('<td>\n') 
                                                    newwin.document.write('<Div style="font-size:'+Wtf.account.companyAccountPref.priceFontSize+';transform: rotate('+Wtf.account.companyAccountPref.pricePrintType+'deg) translate('+Wtf.account.companyAccountPref.priceTranslateX+'px,'+Wtf.account.companyAccountPref.priceTranslateY+'px);transform-origin: 0 0;-webkit-transform-origin: 0 0;">\n')
                                                    if(Wtf.account.companyAccountPref.pricePrefix==undefined || Wtf.account.companyAccountPref.pricePrefix==null || Wtf.account.companyAccountPref.pricePrefix==""){
                                                        newwin.document.write(res.data[i].prodPrice+'\n')
                                                    }else{
                                                        newwin.document.write(Wtf.account.companyAccountPref.pricePrefix +" " +res.data[i].prodPrice+'\n')
                                                    }
                                                    newwin.document.write('</Div>\n')
                                                    newwin.document.write('</td>\n') 
                                                }
                                                newwin.document.write('</tr>\n')    
                                            }
                                            newwin.document.write('</table>\n')             
                                            newwin.document.write('</BODY>\n')
                                            newwin.document.write('</HTML>\n')
                                            newwin.document.close()
                                        }
                                        this.print = false;
                                        } 
                                        else {    //Server-side Exception Message.  //ERP-28177
                                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),res.msg],2);
                                        }       
                                        this.barcodeWindow.close();
                                });
                            }else{
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.msgbox.34")],0);
                            }

                            }
                        },{
                            text: 'Close',
                            scope:this,
                            handler: function(){
                                this.barcodeWindow.close();
                            }
                        }]
                    })
                    this.barcodeWindow.show();
            }
    },
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
        
    },
    filterStore: function(json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.productStore.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleId,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.productStore.load({params: {includeParent:true,ss: this.localSearch.getValue(), start: 0, limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt}});
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.productStore.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleId,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.productStore.load({params: {ss: this.localSearch.getValue(), start: 0, limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt}});
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();


    },
    createSerialWinFields:function(){
          var s=this.grid.getSelectionModel().getSelections();
        
          this.prodIds=[];
              for(var i=0;i<s.length;){
                this.prodIds.push(s[i].data.productid);
                i++;
              }
        this.serialSm = new Wtf.grid.CheckboxSelectionModel({
                    });
                this.cm= new Wtf.grid.ColumnModel([this.serialSm,
                  {
                    header:WtfGlobal.getLocaleText("acc.product.productID"), // "Product Id"
                    dataIndex:'prodid'
                },
                {
                    header:WtfGlobal.getLocaleText("acc.field.ProductName"), // "Product name"
                    dataIndex:'prodnm'
                },
                {
                    header:WtfGlobal.getLocaleText("acc.do.partno"), // "Serial No"
                    dataIndex:'serialno'
                },{
                    header:WtfGlobal.getLocaleText("acc.field.WarrExp.FromDate"), // "expfromdt"
                    dataIndex:'expfromdt'
                },{
                    header:WtfGlobal.getLocaleText("acc.field.WarrExp.EndDate"), // "exp to dt"
                    dataIndex:'exptodt'
                },{
                    header:WtfGlobal.getLocaleText("acc.product.sku"), // "Serial No"
                    hidden:!Wtf.account.companyAccountPref.SKUFieldParm,
                    dataIndex:'skuno'
                }]);
                this.Rec = new Wtf.data.Record.create([
                 {
                    name: 'prodid'
                },
                {
                    name: 'prodnm'
                },
                {
                    name: 'serialno'
                },{
                    name: 'expfromdt'
                },{
                    name: 'exptodt'
                },
                {
                    name: 'skuno',
                    hidden:!Wtf.account.companyAccountPref.SKUFieldParm
                },{
                    name: 'prodPrice'
                }
                ]);
                this.serialStore = new Wtf.data.Store({
                    reader: new Wtf.data.KwlJsonReader({
                        root: "data"
                    },this.Rec),
                    url:"ACCProductCMN/getSerialNos.do",
                   params:{
                        productId:this.prodIds
                    }
                });
                this.serialStore.load({
                    params:{productId:this.prodIds}
                 });
                this.serialGrid=new Wtf.grid.GridPanel({
                    store: this.serialStore,
                    height:230,
                    width:500,
                    scope:this,
                    cm: this.cm,
                    sm:this.serialSm,
                    border : false,
                    loadMask : true,
                    viewConfig: {
                        forceFit:true,
                        emptyText:WtfGlobal.getLocaleText("acc.common.norec")
                    }
                });
                
                
                /*=============================== Batch Grid for Barcode ================================*/    
                
                this.cm= new Wtf.grid.ColumnModel([this.serialSm,
                  {
                    header:WtfGlobal.getLocaleText("acc.product.productID"), // "Product Id"
                    dataIndex:'prodid'
                },
                {
                    header:WtfGlobal.getLocaleText("acc.field.ProductName"), // "Product name"
                    dataIndex:'prodnm'
                },
                {
                    header:WtfGlobal.getLocaleText("acc.inv.loclevel.2"), // Location
                    dataIndex:'location'
                },
                {
                    header:WtfGlobal.getLocaleText("acc.inv.loclevel.1"), // Warehouse
                    dataIndex:'warehouse'
                },
                {
                    header:WtfGlobal.getLocaleText("acc.do.barcode.batchno"), // Batch Number
                    dataIndex:'batchno'
                },
                {
                    header:WtfGlobal.getLocaleText("acc.do.barcode.batchqty"), // Batch Quantity
                    dataIndex:'batchqty'
                },
                {
                    header:WtfGlobal.getLocaleText("acc.product.barcodeqty"), // "Barcode Quantity - This column is editable"
                    dataIndex:'barcdqty',
                    width:100,
                    renderer:this.quantityRenderer,
                    editor: new Wtf.form.NumberField({
                            name:'barcdqty',
                            allowBlank: false,            
                            allowNegative: false,
                            maxLength:2,
                            allowDecimals:false
                            
                        })
                }
//                {
//                    header:WtfGlobal.getLocaleText("acc.field.MfgDate"), // Mfg Date
//                    dataIndex:'mfgdate'
//                },
//                {
//                    header:WtfGlobal.getLocaleText("acc.field.ExpDate"), // "exp to dt"
//                    dataIndex:'expdate'
//                },
//                {
//                    header:WtfGlobal.getLocaleText("acc.product.sku"), // "SKU"
//                    hidden:!Wtf.account.companyAccountPref.SKUFieldParm,
//                    dataIndex:'skuno'
//                }
                ]);
                this.batchRec = new Wtf.data.Record.create([
                 {
                    name: 'prodid'
                },
                {
                    name: 'prodnm'
                },
                {
                    name: 'location'
                },
                {
                    name: 'warehouse'
                },
                {
                    name: 'batchno'
                },
                {
                    name: 'batchqty'
                },
                {
                    name: 'barcdqty'
                },
                {
                    name: 'prodPrice'
                }
//                {
//                    name: 'mfgdate'
//                },
//                {
//                    name: 'exptodt'
//                },
                
//                {
//                    name: 'skuno',
//                    hidden:!Wtf.account.companyAccountPref.SKUFieldParm
//                }                
                ]);
                this.batchStore = new Wtf.data.Store({
                    reader: new Wtf.data.KwlJsonReader({
                        root: "data"
                    },this.batchRec),
                    url:"ACCProductCMN/getSerialNos.do",
                   params:{
                        productId:this.prodIds
                    }
                });
                this.batchStore.load({
                    params:{productId:this.prodIds}
                 });
                this.batchGrid=new Wtf.grid.EditorGridPanel({
                    store: this.batchStore,
                    height:230,
                    width:500,
                    scope:this,
                    cm: this.cm,
                    sm:this.serialSm,
                    border : false,
                    loadMask : true,
                    viewConfig: {
                        forceFit:true,
                        emptyText:WtfGlobal.getLocaleText("acc.common.norec")
                    }
                });
                /*=============================== Batch Grid for Barcode ================================*/    
                            
                            
                 this.bProdSm = new Wtf.grid.CheckboxSelectionModel({
                    });
                this.bcm= new Wtf.grid.ColumnModel([this.bProdSm,
                  {
                    header:WtfGlobal.getLocaleText("acc.product.gridProductID"), // "Product Id"
                    dataIndex:'prodid'
                },
                {
                    header:WtfGlobal.getLocaleText("acc.field.ProductName"), // "Product name"
                    dataIndex:'prodnm'
                },{
                    header:WtfGlobal.getLocaleText("Bar Code"), // "Barcode Value"
                    dataIndex:'barcode'
                },{
                    header:WtfGlobal.getLocaleText("acc.product.barcodeqty"), // "barcode qty"
                    dataIndex:'barcdqty',
                    width:100,
                    renderer:this.quantityRenderer,
                    editor: new Wtf.form.NumberField({
                            name:'barcdqty',
                            allowBlank: false,            
                            allowNegative: false,
                            maxLength:2,
                            allowDecimals:false
                            
                        })
                }]);
                this.bRec = new Wtf.data.Record.create([
                  {
                    name: 'pid'
                },{
                    name: 'prodid'
                },{
                    name: 'prodnm'
                },{
                    name: 'barcdqty'
                },{
                    name: 'prodPrice'
                },{
                    name: 'barcode'
                },{
                   name: 'mrpofprod'  
                }
                ]);
                  this.bProdStore = new Wtf.data.Store({
                    reader: new Wtf.data.KwlJsonReader({
                        root: "data"
                    },this.bRec),
                    url:"ACCProductCMN/getProductsForBarcodeWin.do",
                   params:{
                        productId:this.prodIds
                    }
                });
                this.bProdStore.load({
                    params:{productId:this.prodIds}
                 });
                 this.bProductGrid=new Wtf.grid.EditorGridPanel({
                    store: this.bProdStore,
                    clicksToEdit: 1,
                    height:230,
                    width:500,
                    scope:this,
                    cm: this.bcm,
                    sm:this.bProdSm,
                    border : false,
                    loadMask : true,
                    viewConfig: {
                        forceFit:true,
                        emptyText:WtfGlobal.getLocaleText("acc.common.norec")
                    }
                });
        },
validateBarcode:function(){
    var s=this.grid.getSelectionModel().getSelections();
    if(s.length==0){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.msgbox.34")],0);
        return false;
    }
    for(var i=0;i<s.length;){
        if(s[0].data.barcodefield!=s[i].data.barcodefield){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.product.error.diffBarcdGenrator")],1);
            return false;
        }
        if(s[i].data.barcodefield=='0'){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.product.error.barcdGenratorNotSet")],1);
            return false;
        }
        if(!Wtf.account.companyAccountPref.SKUFieldParm){
            if(s[i].data.barcodefield==Wtf.BarcodeGenerator_SKUField){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.product.error.skunotenable")],1);
                return false;
            }
        }
	    if(s[i].data.barcodefield==Wtf.BarcodeGenerator_ProductId){    //Barcode based on Product ID
            var productID = s[i].data.pid;
            var productIDLength = s[i].data.pid.length;
            if(Wtf.account.companyAccountPref.barcodetype===Wtf.BarcodeType_Code_EAN13 && isNaN(productID)){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),"For EAN-13 Type Barcode, Product ID must be a number value."],2);
                return false;
            } else if(Wtf.account.companyAccountPref.barcodetype===Wtf.BarcodeType_Code_EAN13 && (productIDLength!=12 && productIDLength!=13)){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),"For EAN-13 Type Barcode, Product ID must be a 12 or 13 digit number value."],2);
                return false;
            } else if(Wtf.account.companyAccountPref.barcodetype===Wtf.BarcodeType_Code_EAN8 && isNaN(productID)){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),"For EAN-8, Product ID must be a number value."],2);
                return false;
            } else if(Wtf.account.companyAccountPref.barcodetype===Wtf.BarcodeType_Code_EAN8 && (productIDLength!=7 && productIDLength!=8)){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),"For EAN-8, Product ID must be 7 or 8 digit number value."],2);
                return false;
            }    
        } else if(s[i].data.barcodefield==Wtf.BarcodeGenerator_Barcode){  //Barcode based on Barcode Field Value.
            var barcode = s[i].data.barcode;
            var barcodeLength = barcode.length;
            if(Wtf.account.companyAccountPref.barcodetype===Wtf.BarcodeType_Code_EAN13 && isNaN(barcode)){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),"For EAN-13 Type Barcode, Barcode value must be a number."],2);
                return false;
            }else if(Wtf.account.companyAccountPref.barcodetype===Wtf.BarcodeType_Code_EAN13 && (barcodeLength!=12 && barcodeLength!=13)){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),"For EAN-13 Type Barcode, Barcode value must be a number of 12 or 13 digit."],2);
                return false;
            } else if(Wtf.account.companyAccountPref.barcodetype===Wtf.BarcodeType_Code_EAN8 && isNaN(barcode)){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),"For EAN-8 Type Barcode, Barcode value must be a number."],2);
                return false;
            } else if(Wtf.account.companyAccountPref.barcodetype===Wtf.BarcodeType_Code_EAN8 && (barcodeLength!=7 && barcodeLength!=8)){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),"For EAN-8 Type Barcode, Barcode value must be a number of 7 or 8 digit."],2);
                return false;
            }    
        } else if(s[i].data.barcodefield==Wtf.BarcodeGenerator_SerialId){     //Barcode based on Serial No.
            var isEdit = false;
            var isClone = false;
            var isForBarCode = true;
            this.getNewBatchDetails(s[i], isEdit, isClone, isForBarCode);
        }
        i++;
    }
    return true;
},
    getDetailPanel:function(){
        this.detailPanel = new Wtf.DetailPanel({
            grid:this.grid,
            Store:this.Store,
            modulename: "Product",
            keyid:"productid",
            height:200,
            mapid:1,
            id2:this.id,
            moduleID:Wtf.Acc_Product_Master_ModuleId,
            moduleName:"Product",
            mainTab:this.mainTab,
            leadDetailFlag:true,
            moduleScope:this,
            accid:this.recid,
            acccode:this.acccode
        });

    },
    getDetails:function(){
        var sm = this.grid.getSelectionModel();
        var errorcode="";
        if(sm.getCount()==0){
          errorcode="Error0";  
        }else if(sm.getCount()>1){
            errorcode="Error1";
        }
        var commentlist = getDocsAndCommentList(sm.getSelected(), Wtf.Acc_Product_Master_ModuleId,this.id,undefined,'Product',undefined,"email",'leadownerid',this.contactsPermission,0,this.recid,errorcode);
    },
    onRender: function(config){
        dojo.cometd.subscribe(Wtf.ChannelName.ProductAndServicesReport, this, "productDetailGridAutoRefreshPublishHandler");
        this.productStore.load({
            params: {
                start: 0,
                limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt
            }
        });
        

 this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            listeners:{
                'afterlayout':function(p){
                    if(p.attachDetailTrigger){
                        p.layout.south.slideOut = p.layout.south.slideOut.createSequence(this.getDetails,this);
                        delete p.attachDetailTrigger;
                    }
                    },
                    scope:this
            },
            items: [this.objsearchComponent
              ,{
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid]
              },{
                region:'south',
                height:250,
                hidden:!Wtf.account.companyAccountPref.viewDetailsPerm ,
                title:WtfGlobal.getLocaleText("acc.editors.otherdetailregion"),//'Other Details',
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
            }]
        }); 
       this.add(this.leadpan);  

//        this.add(this.grid);
//         if(this.addproductwin){
//            callProductWindow(false,null,"productwin");
//        Wtf.getCmp("productwin").on('update',this.updateGrid,this);
//        }
        Wtf.account.ProductDetailsPanel.superclass.onRender.call(this, config);
    },
    showForm:function(isEdit,isClone,rec){
       var recArr =[] ;
       this.isEdit=isEdit;
        if(isEdit){
            if(!rec){
                recArr = this.grid.getSelectionModel().getSelections();
                this.grid.getSelectionModel().clearSelections();
                rec = recArr[0]
            }else{
                recArr.push(rec);
            }
 //           WtfGlobal.highLightRowColor(this.grid,recArr,true,0,1);
            var isForBarCode = false;
            this.getNewBatchDetails(rec, isEdit, isClone, isForBarCode, recArr);
        }else{
            this.openProductForm(isEdit, isClone, recArr);
        }

//        Wtf.getCmp(tabid).on('cancel',function(){ var num= (this.productStore.indexOf(recArr[0]))%2;WtfGlobal.highLightRowColor(this.grid,recArr,false,num,1);},this);
    },
    openProductForm: function(isEdit, isClone, recArr, batchdetails){
        var rec=isEdit?recArr[0]:null;
        var tabid=isEdit?recArr[0].data.productid:"productwin";
        if(isClone){
                tabid=isEdit?"clone"+recArr[0].data.productid:"cloneproductwin";
        }
        var pname=isEdit?recArr[0].data.productname:null;
      /*
       *If Product is already used in transaction then isUsedinTransaction is set true    
       **/
      var isUsedinTransaction = (rec!=null&&rec.data.isUsedInTransaction!=undefined)?rec.data.isUsedInTransaction:false;
      
        callProductWindow(isEdit, rec, tabid, pname, isClone, batchdetails,isUsedinTransaction);
        Wtf.getCmp(tabid).on('update',this.updateGrid,this);
        this.fireEvent("productformshown",this,[isEdit]);
    },
    getNewBatchDetails: function(record, isEdit, isClone, isForBarCode, recArr){
        var isBatchForProduct = record.data.isBatchForProduct;
        var isSerialForProduct = record.data.isSerialForProduct;
        var isLocationForProduct = record.data.isLocationForProduct;
        var isWarehouseForProduct = record.data.isWarehouseForProduct;
        var isRowForProduct = record.data.isRowForProduct;
        var isRackForProduct = record.data.isRackForProduct;
        var isBinForProduct = record.data.isBinForProduct;
        
        if((Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsoryz || Wtf.account.companyAccountPref.isLocationCompulsory
        || Wtf.account.companyAccountPref.isWarehouseCompulsory || Wtf.account.companyAccountPref.isRowCompulsory || Wtf.account.companyAccountPref.isRackCompulsory
        || Wtf.account.companyAccountPref.isBinCompulsory) 
        && (isBatchForProduct || isSerialForProduct || isLocationForProduct || isWarehouseForProduct || isRowForProduct || isRackForProduct || isBinForProduct)){
            Wtf.Ajax.requestEx({
                url:"ACCProduct/getNewBatchDetailsForProduct.do",
                params: {
                    productid: record.data.productid,
                    isEdit: isEdit,
                    moduleid: this.moduleId
                }
            },this,function(res,req){
                if(res.success==true){
                    if(isForBarCode){
                        var batchDetailsJSON = eval(res.data.batchdetails);
                        for(var j=0; j<batchDetailsJSON.length; j++){
                            var serialNo = batchDetailsJSON[j].serialno;
                            var serialNoLength = batchDetailsJSON[j].serialno.length;
                            if(Wtf.account.companyAccountPref.barcodetype===Wtf.BarcodeType_Code_EAN13 && isNaN(serialNo)){
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),"For EAN-13 Type Barcode, Serial Number must be a number value."],2);
                                return false;
                            } else if(Wtf.account.companyAccountPref.barcodetype===Wtf.BarcodeType_Code_EAN13 && (serialNoLength!=12 && serialNoLength!=13)){
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),"For EAN-13 Type Barcode, Serial Number must be a 12 or 13 digit number value."],2);
                                return false;
                            } else if(Wtf.account.companyAccountPref.barcodetype===Wtf.BarcodeType_Code_EAN8 && isNaN(serialNo)){
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),"For EAN-8, Serial Number must be a number value."],2);
                                return false;
                            } else if(Wtf.account.companyAccountPref.barcodetype===Wtf.BarcodeType_Code_EAN8 && (serialNoLength!=7 && serialNoLength!=8)){
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),"For EAN-8, Serial Number must be 7 or 8 digit number value."],2);
                                return false;
                            }  
                        }
                    }else{
                        var batchdetails;
                        batchdetails = res.data.batchdetails;
                        this.openProductForm(isEdit, isClone, recArr, batchdetails);
                    }
                }
            },function(res,req){

            });
        }else{
            if(!isForBarCode){
                this.openProductForm(isEdit, isClone, recArr);
            }
        }
    },
    productDetailGridAutoRefreshPublishHandler: function(response) {
        this.isFromSave = true; //Flag is added for save form success message
        var res = eval("(" + response.data + ")");
        if (res.success && !res.isFromProductView) {
            if(this.productStore!=undefined){
                this.productStore.load({
                    params: {
                        start: 0,
                        limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt
                    }
                });
            }
        }
    },
     confirmBeforeDeleteProduct: function (delp,isFromProductView,productId) {
        if (Wtf.account.companyAccountPref.propagateToChildCompanies) {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.confirm"),
                msg: WtfGlobal.getLocaleText("acc.productmaster.propagatedproducts.delete.confirm"),
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
                    this.scopeObject.deleteProduct(delp,isFromProductView,productId);
                }
            }, this);
        } else {
            this.deleteProduct(delp,isFromProductView,productId);
        }
    },
    
    deleteProduct:function(isPermDel,isFromProductView,productId){
        this.arrRec=[];
        this.seletedProductIDsWithSubProduct=[];//this variable hold selected product it. If parent is selected is also hold child Ids
        this.selectedProductIDs=[];//this variable take take only selected products id. In case of parent selected it does not takes child IDs
        this.arrRec = this.sm.getSelections();
        if(isFromProductView && productId){
            this.selectedProductIDs.push(productId);
            this.seletedProductIDsWithSubProduct.push(productId);
            this.getSubProductIds(productId,this.seletedProductIDsWithSubProduct);
        }else{
          for(var i=0;i<this.sm.getCount();i++){
            this.selectedProductIDs.push(this.arrRec[i].data['productid']);
            if(this.seletedProductIDsWithSubProduct.indexOf(this.arrRec[i].data['productid'])==-1){//applying check so that duplicate entry shoud not get inserted again
                this.seletedProductIDsWithSubProduct.push(this.arrRec[i].data['productid']);
            }

            this.getSubProductIds(this.arrRec[i].data['productid'],this.seletedProductIDsWithSubProduct);
          }     
        }
//        this.sm.clearSelections();
        WtfGlobal.highLightRowColor(this.grid,this.arrRec,true,0,2);
        var message="";
        if(Wtf.isCRMSync || Wtf.isPMSync){
           message =isPermDel == "true" ? WtfGlobal.getLocaleText("acc.rem.65.4"): WtfGlobal.getLocaleText("acc.rem.65.3");  //Are you sure you want to delete the selected product(s) and all associated sub product(s) from ERP As well as other applications?<div><b>Note: This data cannot be retrieved later</b></div>           
        } else {
           message =isPermDel == "true" ? WtfGlobal.getLocaleText("acc.rem.65.2"): WtfGlobal.getLocaleText("acc.rem.65.1");  //"Are you sure you want to delete the selected product(s) and all associated sub product(s)?<div><b>Note: This data cannot be retrieved later</b></div>",
        }
    Wtf.MessageBox.show({
        title: WtfGlobal.getLocaleText("acc.common.warning"),
        msg:message,
        width: 560,
        buttons: Wtf.MessageBox.YESNOCANCEL,
        animEl: 'upbtn',
        icon: Wtf.MessageBox.QUESTION,
        scope:this,
        fn:function(btn){
            if(btn=="yes"){//If click on yes then we need to delete child products as well
                if(this.delIA){
                    this.unBuildAssemblyAfterDeletion(isPermDel,true);
                }else{
                    this.deletionRequest(false,isPermDel,true);
                }
            } else if(btn=="no"){// if click on no then we need to delete parent product only and remove relation from child
                if(this.delIA){
                    this.unBuildAssemblyAfterDeletion(isPermDel,false);
                }else{
                    this.deletionRequest(false,isPermDel,false);
                }
            } else { // click on cancel 
                var num= (this.productStore.indexOf(this.arrRec[0]))%2;
                WtfGlobal.highLightRowColor(this.grid,this.arrRec,false,num,2);
                return;
            }
        }
    });
    },
    getSubProductIds : function(productId,subProductIdArr){
         if(this.productStore.find('parentuuid', productId) != -1 ){
                var child = this.productStore.getAt(this.productStore.find('parentuuid',productId));
               
                if(subProductIdArr.indexOf(child.data['productid'])==-1){//applying check so that duplicate entry shoud not get inserted again
                    subProductIdArr.push(child.data['productid']);
                }
                
                while(this.productStore.find('parentuuid',child.data['productid']) != -1){
                    child = this.productStore.getAt(this.productStore.find('parentuuid',child.data['productid']));
                   
                    if(subProductIdArr.indexOf(child.data['productid'])==-1){//applying check so that duplicate entry shoud not get inserted again
                        subProductIdArr.push(child.data['productid']);
                    }
                }
            }
    },
    
    deletionRequest:function(unbuild,isPermDel,deleteWithSubProducts){
        if (this.grid && this.grid.loadMask) {
            this.grid.loadMask.show();
        }
        WtfGlobal.setAjaxTimeOut();
    	Wtf.Ajax.requestEx({
          url:"ACCProductCMN/deleteProducts.do",
          params: {
              mode:23,
              ids:deleteWithSubProducts?this.seletedProductIDsWithSubProduct:this.selectedProductIDs,
              unBuild:unbuild,
              isPermDel:isPermDel,
              ispropagatetochildcompanyflag:this.ispropagatetochildcompanyflag
          }
      },this,function(response){
          this.genSuccessResponse(response);
          this.fireEvent("productdelete",this,[response.success]);
      },this.genFailureResponse);
    },

    unBuildAssemblyAfterDeletion:function(isPermDel,deleteWithSubProducts){
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.warning"),
            msg: WtfGlobal.getLocaleText("acc.rem.62"),  //"Would you like to unbuild and update the inventory of the sub product(s) (Bill of Materials) of selected Assembly product(s)?",
            width: 560,
            buttons: Wtf.MessageBox.YESNO,
            animEl: 'upbtn',
            icon: Wtf.MessageBox.QUESTION,
            scope:this,
            closable:false,
            fn:function(btn){
            		if(btn == "yes"){
            			this.deletionRequest(true,isPermDel,deleteWithSubProducts);
            		}else if(btn == "no"){
            			this.deletionRequest(false,isPermDel,deleteWithSubProducts);
            		}
            	}
            });
    },
    
    activateDeactivateProduct: function(activateDeactivate) {
        var arr = [];
        var data = [];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.grid.getSelectionModel().clearSelections();
        var activateDeactivateFlag = false;
        
        if (activateDeactivate == "activate") {
            activateDeactivateFlag = true;        //Send this flag as true whenever you want to activate or deactivate Products.  
        }
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"), //"Confirm",
            msg: activateDeactivateFlag ? WtfGlobal.getLocaleText("acc.product.productActivateConfirmMsg") : WtfGlobal.getLocaleText("acc.product.productDeactivateConfirmMsg"),
            width: 400,
            buttons: Wtf.MessageBox.OKCANCEL,
            animEl: 'upbtn',
            icon: Wtf.MessageBox.QUESTION,
            scope: this,
            fn: function(btn) {
                if (btn != "ok") {
                    return;
                }
                for (i = 0; i < this.recArr.length; i++) {
                    arr.push(this.productStore.indexOf(this.recArr[i]));
                }
                data = WtfGlobal.getJSONArray(this.grid, true, arr);

                Wtf.Ajax.requestEx({
                    url: "ACCProductCMN/activateDeactivateProducts.do",
                    params: {
                        data: data,
                        activateDeactivateFlag: activateDeactivateFlag, //Send this flag as true whenever you want to Activate or Deactivate Products.
                        mode: 1
                    }
                }, this, this.genActivateSuccessResponse, this.genActivateFailureResponse);
            }
        });
    },
    genActivateSuccessResponse: function(response) {
        this.productStore.on('load', function() {
            WtfComMsgBox([(WtfGlobal.getLocaleText("acc.product.gridProduct")), response.msg], response.success * 2 + 1);
        }, this.productStore, {
            single: true
        });
        this.productStore.reload();
        
    },
    genActivateFailureResponse: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    enableDisableActivateDeactivateProductBtn : function(selRecArr){
      if (selRecArr != undefined && selRecArr.length > 0) {
            var isdormant = false;
            var isactive = false;
            for (var i = 0; i < selRecArr.length; i++) {
                //Below check is added to handle enable/disable of activateProduct button, when multiple products are selected (isActiveItem=false = > Dormant)
                if (selRecArr[i].data.isActiveItem === false) {
                    if (!isdormant) {
                        isdormant = true;
                    }
                }
                //Below check is added to handle enable/disable of deactivateProduct button, when multiple products are selected (isActiveItem=true = > Active)
                if (selRecArr[i].data.isActiveItem === true) {
                    if (!isactive) {
                        isactive = true;
                    }

                }
            }
            if (isdormant === true) {
                this.activateProduct.enable();

            } else {
                this.activateProduct.disable();
            }
            if (isactive === true) {
                this.deactivateProduct.enable();
            } else {
                this.deactivateProduct.disable();
            }
        } else {
            this.activateProduct.disable();
            this.deactivateProduct.disable();
        }  
    },
    syncProducts:function(isInventorySync, isPOSSync, isAllDataSync){
       var arrID=[];

        this.productStore.filterBy(function(rec){
            if(rec.data.syncable && rec.data.isActiveItem)
                return true;
                else return false
        })
       this.arrRec = this.productStore.getRange(0,this.productStore.getCount()-1);
       for(var i=0;i<this.productStore.getCount();i++)
            if(this.arrRec[i].data.syncable)
                arrID.push(this.arrRec[i].data['productid']);
        this.sm.clearSelections();
        WtfGlobal.highLightRowColor(this.grid,this.arrRec,true,0,2);
       if(this.arrRec.length==0){
           WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.66")],2);
           this.productStore.clearFilter();
           return;
       }
       Wtf.MessageBox.show({
           title: WtfGlobal.getLocaleText("acc.common.confirm"),
           msg: (isInventorySync)?WtfGlobal.getLocaleText("acc.rem.acc.rem.226"):WtfGlobal.getLocaleText("acc.rem.63"),  //"Shown product(s) will be syncronized with other application. Are you sure you want to synchronize the product(s)?",
           width: 560,
           buttons: Wtf.MessageBox.OKCANCEL,
           animEl: 'upbtn',
           icon: Wtf.MessageBox.QUESTION,
           scope:this,
           fn:function(btn){
               if(btn!="ok"){this.productStore.clearFilter();
                    var num= (this.productStore.indexOf(this.arrRec[0]))%2;
                   WtfGlobal.highLightRowColor(this.grid,this.arrRec,false,num,2);
                     return;
                }
                else {
                        var URL="ACCCompanySetup/sendAccProducts.do";
                        if(isInventorySync){
                            URL="ACCCompanySetup/sendAccProductsToInv.do";
                        } 
                        if(isPOSSync){
                            URL="ACCCompanySetup/sendAccProductsToPOS.do";
                        }
                        WtfGlobal.setAjaxTimeOut();
                        Wtf.Ajax.requestEx({
    //                        url:Wtf.req.account+'CompanyManager.jsp',
                            url:URL,
                            params: {
                                ids:arrID,
                                isAllSync: isAllDataSync
                            }
                        },this,this.genSyncSuccessResponse,this.genSyncFailureResponse);
                }
        }});
    },
    genSyncSuccessResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        for(var i=0;i<this.arrRec.length;i++){
             var ind=this.productStore.indexOf(this.arrRec[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.arrRec[i],false,num,2,true);
        }
        this.productStore.clearFilter();
        if(!response.companyexist)
            this.callSubscriptionWin()
        else if(response.success){
                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.productList.gridProduct"),response.msg],response.success*2+1);

//            (function(){
//            }).defer(WtfGlobal.gridReloadDelay(),this);
            }

    },
    genSyncFailureResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        for(var i=0;i<this.arrRec.length;i++){
             var ind=this.productStore.indexOf(this.arrRec[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.arrRec[i],false,num,2,true);
        }this.productStore.clearFilter();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
   },
   syncProductsInv:function(){
        Wtf.MessageBox.show({
           title: WtfGlobal.getLocaleText("acc.common.confirm"),
           msg: WtfGlobal.getLocaleText("acc.field.AreyousureyouwanttosyncproductsfromInventory"),  //"Are you sure you want to sync products from Inventory?",           
           buttons: Wtf.MessageBox.OKCANCEL,
           animEl: 'upbtn',
           icon: Wtf.MessageBox.QUESTION,
           scope:this,
           fn:function(btn){
               if(btn=="ok"){
                        WtfGlobal.setAjaxTimeOut();
                        Wtf.Ajax.requestEx({
    //                        url:Wtf.req.account+'CompanyManager.jsp',
                            url:"ACCAccountCMN/getInvProducts.do"                            
                        },this,this.genSuccessResponse,this.genFailureResponse);
                }
           }
      });
    },
   syncProductsLMS:function(){
        Wtf.MessageBox.show({
           title: WtfGlobal.getLocaleText("acc.common.confirm"),
           msg: WtfGlobal.getLocaleText("acc.field.AreyousureyouwanttosyncproductsfromLMS"),  //"Are you sure you want to sync products from LMS?",           
           buttons: Wtf.MessageBox.OKCANCEL,
           animEl: 'upbtn',
           icon: Wtf.MessageBox.QUESTION,
           scope:this,
           fn:function(btn){
               if(btn=="ok"){
                        WtfGlobal.setAjaxTimeOut();
                        Wtf.Ajax.requestEx({
    //                        url:Wtf.req.account+'CompanyManager.jsp',
                            url:"ACCAccountCMN/getLMSCourcesAsProducts.do"                            
                        },this,this.genSuccessResponse,this.genFailureResponse);
                }
           }
      });
    },
    syncProductsIntoPM:function(isSynAll){
        Wtf.MessageBox.show({
           title: WtfGlobal.getLocaleText("acc.common.confirm"),
           msg: WtfGlobal.getLocaleText("acc.confirm.msg.syncdatatopm"),  //"Are you sure you want to sync products from ERP to bPM?",           
           buttons: Wtf.MessageBox.OKCANCEL,
           animEl: 'upbtn',
           icon: Wtf.MessageBox.QUESTION,
           scope:this,
           fn:function(btn){
               if(btn=="ok"){
                        
                        WtfGlobal.setAjaxTimeOut();
                        var data = "";
                        if (!isSynAll) {
                            var arr = this.grid.getSelectionModel().getSelections();
                            for (var i = 0; i < arr.length; i++) {
                                data += "{id:\"" + arr[i].data.productid + "\",name:\"" + arr[i].data.productname + "\"},"
                            }

                            data = data.substring(0, data.length - 1);
                        }
                        Wtf.Ajax.requestEx({
                            url:"ACCProductCMN/SyncDataIntoPM.do",
                            params:{
                                data: "["+data+"]"
                            }
                            
                        },this,this.genSuccessResponse,this.genFailureResponse);
                }
           }
      });
    },
    genSuccessResponse:function(response){
        this.ispropagatetochildcompanyflag=false;
        if (this.grid && this.grid.loadMask) {
            this.grid.loadMask.hide();
        }
        WtfGlobal.resetAjaxTimeOut();
        for(var i=0;i<this.arrRec.length;i++){
             var ind=this.productStore.indexOf(this.arrRec[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.arrRec[i],false,num,2,true);
        }
        if(response.success){
            if(response.success){
                var pStore = this.productStore
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.productList.gridProduct"),response.msg],response.success*2+1, null, null, function(btn){
//                (function(){
                    Wtf.uomStore.reload();
                    Wtf.productStore.reload();
                    Wtf.productStoreSales.reload();
                    pStore.reload();    
                    getCompanyAccPref(true);
//                }).defer(WtfGlobal.gridReloadDelay(),this);
            });
            
            
            }
        }else{
            if(response.isused="true")
          {
            if(response.msg==""){
               var msg=  WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server"; 
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg]);
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg]);
            }
        }
        else
        {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.productList.gridProduct"),WtfGlobal.getLocaleText("acc.field.NoProductsareavailableforsyncing")],response.success*2+1);
        }
    }        
},
    genFailureResponse:function(response){
        this.ispropagatetochildcompanyflag=false;
        if (this.grid && this.grid.loadMask) {
            this.grid.loadMask.hide();
        }
        WtfGlobal.resetAjaxTimeOut();
        for(var i=0;i<this.arrRec.length;i++){
             var ind=this.productStore.indexOf(this.arrRec[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.arrRec[i],false,num,2,true);
        }
        var msg=  WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
   },
     
    showPriceReport: function() {
        callPriceReport();
    },
    
    custPriceReport : function() {
        callPriceReportCustVen("Customer");
    },
    
    vendorPriceReport : function() {
        callPriceReportCustVen("Vendor");
    },
    
    showInventoryReport:function(){
       var productid=this.grid.getSelectionModel().getSelected().data.productid;
       var productname=this.grid.getSelectionModel().getSelected().data.productname;
       var rate= this.grid.getSelectionModel().getSelected().data.saleprice;
       var producttype=this.grid.getSelectionModel().getSelected().data.producttype;
       if(producttype==Wtf.producttype.service){
           WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.64")],2);
           return;
       }
       callInventoryReport(productid, productname,rate);
    },
    endDeleteColor:function(){
       // WtfGlobal.highLightRowColor(this.grid,this.arrRec,false,0,3);
        var count=this.arrRec.length;
        for(var i=0;i<count;i++){
            WtfGlobal.highLightRowColor(this.grid,this.arrRec[i],true,0,3);
        }
    },    
   callSubscriptionWin:function(){
       var m = Wtf.DomainPatt.exec(window.location);
       m="http://apps.deskera.com/"+m[0];
       var subscribePanel = new Wtf.FormPanel({
                    width:'80%',
                    method :'POST',
                    scope: this,
                    border:false,
                    fileUpload : true,
                    waitMsgTarget: true,
                    labelWidth: 70,
                    bodyStyle: 'font-size:10px;padding:10px;',
                    layout: 'form',
                    items:[{
                        border:false,
                        html:"<div style = 'font-size:12px; width:100%;height:100%;position:relative;float:left;'>"
                                +WtfGlobal.getLocaleText("acc.field.DatasyncingoperationbetweenCRMandAccountingcantbeperformed")
                                +WtfGlobal.getLocaleText("acc.field.InordertosubscribetoCRMclick")+"<a target='_blank' class='linkCls' href="+m+"> <b>" + WtfGlobal.getLocaleText("acc.field.Subscribe")+ "</b> </a>"+ WtfGlobal.getLocaleText("acc.field.Subscribe.else.click.cancel")
                                +"</div>"
                    }]
                },
                this);
                var impWin1 = new Wtf.Window({
                    resizable: false,
                    scope: this,
                    layout: 'border',
                    modal:true,
                    width: 380,
                    height: 220,
                    border : false,
                    iconCls: 'pwnd deskeralogoposition',
                    title: WtfGlobal.getLocaleText("acc.field.DataSyncing"),
                    items: [
                            {
                                region:'north',
                                height:70,
                                border : false,
                                bodyStyle : 'background:white;',
                                html: getTopHtml(WtfGlobal.getLocaleText("acc.setupWizard.dear")+" "+ _fullName+",", "",null,true)
                            },{
                                region:'center',
                                layout:'fit',
                                border:false,
                                bodyStyle : 'background:white;',
                                items:[subscribePanel]
                            }
                    ],
                    buttons: [{
                        text:WtfGlobal.getLocaleText("acc.common.cancelBtn"), //'Cancel',
                        id:'canbttn1',
                        scope:this,
                        handler:function() {
                            impWin1.close();
                        }
                    }]
                },this);

                impWin1.show();},
    setPageSize: function(store, rec, opt) {
        var count = 0;
        for (var i = 0; i < store.getCount(); i++) {
            if (rec[i].data['level'] == 0 && (rec[i].data['parentid'] == "" || rec[i].data['parentid'] == undefined))
                count++;
        }
        if(this.jReader.jsonData) {
            this.pageLimit.totalSize = this.jReader.jsonData['totalCount'];
        }else if(store.reader.jsonData){
            this.pageLimit.totalSize = store.reader.jsonData['totalCount'];
        }
        this.grid.getView().refresh();
    },
    
    exportForImport:function() {
        var module='Product' ;
        var mode=198; 
        var type="csv"; 
        var filename="ProductDetails";
        WtfGlobal.exportAllData(mode,filename,type,module);
    },
    getMyConfig : function(){
        WtfGlobal.getGridConfig(this.grid, this.moduleId, false, false);
        
        var statusForCrossLinkageIndex = this.grid.getColumnModel().findColumnIndex("statusforcrosslinkage");
        if (statusForCrossLinkageIndex != -1) {
            this.grid.getColumnModel().setHidden(statusForCrossLinkageIndex, true);
        }
    },
    saveMyStateHandler: function(grid,state){
        WtfGlobal.saveGridStateHandler(this, grid, state, this.moduleId, grid.gridConfigId, false);
    },
    
    onRowexpand:function(scope, record, body){
        this.expanderBody=body;        
        this.expandStore.proxy.conn.url="ACCProduct/getBOMDetails.do";
        this.expandStore.load({params:{productid:record.data['productid']}});
    },
    
    fillExpanderBody:function(){
    if(this.expandStore.getCount()>0) {
        var disHtml = "";
        for(var i=0;i<this.expandStore.getCount();i++){
            var expandStoreRec=this.expandStore.getAt(i);
                var arr=[];
                arr=[WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),WtfGlobal.getLocaleText("acc.productList.gridProductDescription"),WtfGlobal.getLocaleText("acc.invoiceList.expand.pType"),WtfGlobal.getLocaleText("acc.mrp.field.bomcode"),WtfGlobal.getLocaleText("acc.product.initialPurchasePrice"),WtfGlobal.getLocaleText("acc.product.gridQty"),WtfGlobal.getLocaleText("acc.product.Percentage"),WtfGlobal.getLocaleText("acc.product.ActualQuantiy"),WtfGlobal.getLocaleText("acc.product.gridTotal"),"                "];
                var  gridHeaderText=expandStoreRec.json['bomName'];
                var header = "<span class='gridHeader'>"+gridHeaderText+"</span>";  //Header Text
                var count=arr.length;
                var widthInPercent=100/count;
                var minWidth = count*100 + 40;
                header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
                header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>"; //Sr.No.
                for(var i1=0;i1<count;i1++){
                    header += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + arr[i1] + "</span>"; //headers
                }
                header += "</div><div style='width: 100%;min-width:"+minWidth+"px'><span class='gridLine'></span></div>";   //Header Line
                header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
                
                var recArray = "";
                var bomDetailRecords = expandStoreRec.json['bomAssemblyDetails'];
                if (bomDetailRecords != undefined && bomDetailRecords.length>1) {
                    recArray = eval('(' + bomDetailRecords + ')');
                }
                for(var i2=0;i2<recArray.length;i2++){
                    var rec=recArray[i2];
                    
                    header += "<span class='gridNo'>"+(i2+1)+".</span>";

                    var productname=(rec.productname != null && rec.productname != "")?rec.productname :'&nbsp';
                    header += "<span class='gridRow' wtf:qtip='"+productname+"' style='width:"+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(productname,15)+"</span>";
                    var desc=(rec.desc != null && rec.desc != "")?rec.desc :'&nbsp';
                    header += "<span class='gridRow' wtf:qtip='"+desc+"' style='width:"+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(desc,15)+"</span>";
                    var type=(rec.type != null && rec.type != "")?rec.type :'&nbsp';
                    header += "<span class='gridRow' wtf:qtip='"+type+"' style='width:"+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(type,15)+"</span>";
                    var subbomcode=(rec.subbomcode != null && rec.subbomcode != "")?rec.subbomcode :'&nbsp';
                    header += "<span class='gridRow' wtf:qtip='"+subbomcode+"' style='width:"+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(subbomcode,15)+"</span>";
                
                    var price = (rec.purchaseprice=== undefined || rec.purchaseprice=="")?0:rec.purchaseprice;
                    header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(price,null,[true])+"</span>";
                    var quantity = (rec.quantity=== undefined || rec.quantity=="")?0:rec.quantity;
                    header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+parseFloat(getRoundofValue(quantity)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"</span>";
                    
                    var percentage= "";
                    if(rec.percentage==""||rec.percentage==undefined) {
                        percentage=100;
                    }else if(rec.percentage){
                        percentage= rec.percentage;
                    }
                    header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+percentage+ "%"+"</span>";
                    var actualquantity = (rec.actualquantity=== undefined || rec.actualquantity=="")?0:rec.actualquantity;
                    header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+parseFloat(getRoundofValue(actualquantity)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"</span>";
                    
                    var total = 0;
                    if(quantity != "") {
                        total=(price*((percentage * quantity)/100))
                    }
                    header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(total,null,[true])+"</span>";
                    header +="<br>";
                }
                header +="</div>";
                disHtml += "<div class='expanderContainer1' style='width:100%'>" + header + "</div>";
        }
        this.expanderBody.innerHTML = disHtml;
    }
    else{
        this.expanderBody.innerHTML = "<br><b><div class='expanderContainer' style='width:100%'>"+WtfGlobal.getLocaleText("acc.field.Nodatatodisplay")+"</div></b>"      //No data to display.
    }

}
});

// For Product Grouping
Wtf.account.ProductListByCategory=function(config){
     Wtf.apply(this, config);
//     this.businessPerson=(config.isCustomer?'Customer':'Vendor');
     this.isCategoryUpdated = false;
      this.uPermType=Wtf.UPerm.product;
      this.permType=Wtf.Perm.product;
     this.productCategoryRecord = Wtf.data.Record.create ([
        {name:'id'},
        {name:'name'}
     ]);
     this.productCategoryStore = new Wtf.data.Store({
        url: "ACCMaster/getMasterItems.do",
        baseParams:{
            mode:112,
            groupid:19
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.productCategoryRecord)
     });
     
     this.typeEditor = new Wtf.form.ComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.cust.Productcategory"),
        store: this.productCategoryStore,
//        name:config.isCustomer? 'customercategoryview' : 'vendorcategoryview',
//        id:config.isCustomer? 'customercategoryviewid' : 'vendorcategoryviewid',
        displayField:'name',
        valueField:'id',
        mode: 'local',
        triggerAction: 'all',
        typeAhead:true,
        width:200,
        selectOnFocus:true
     });
     this.productCategoryStore.load();
     this.productCategoryStore.on('load',this.setValue,this);
     
     this.GridRec = Wtf.data.Record.create ([
        {name:'productid'},
        {name:'productname'},
        {name:'desc'},
        {name:'pid'},
        {name:'vendor'},
        {name:'producttype'},
        {name:'type'},
        {name:'initialsalesprice'},
        {name:'warrantyperiod'},
        {name:'warrantyperiodsal'},
        {name:'uomid'},
        {name:'uomname'},
        {name:'displayUoMid'},
        {name:'displayUoMName'},
        {name:'parentuuid'},
        {name:'parentid'},
        {name:'parentname'},
        {name:'purchaseaccountid'},
        {name:'salesaccountid'},
        {name:'purchaseretaccountid'},
        {name:'salesretaccountid'},
        {name:'reorderquantity'},
        {name:'quantity'},
        {name:'reorderlevel'},
        {name:'leadtime'},
        {name:'purchaseprice'},
        {name:'saleprice'},
        {name:'leaf'},
        {name:'warranty'},
        {name:'syncable'},
        {name:'level'},
        {name:'initialquantity',mapping:'initialquantity'},
        {name: 'itemReusability'},
        {name: 'licensetype'},                 
        {name: 'productBrandName'},
        {name: 'warehouseName'},
        {name: 'valuationmethod'},
        {name:'initialprice'},
//        {name:'ccountinterval'},
//        {name:'ccounttolerance'},
        {name:'productweight'},
        {name:'productweightperstockuom'},
        {name:'productweightincludingpakagingperstockuom'},
        {name:'productvolumeperstockuom'},
        {name:'productvolumeincludingpakagingperstockuom'},
        {name:'categoryid'},
        {name:'category'},
        {name:'tariffname'},
        {name:'hsncode'},
        {name:'reportinguom'},
        {name: 'itemissuecount'}
    ]);
    this.ByCategoryStore = new Wtf.data.GroupingStore({
        url:"ACCProduct/getProductsByCategory.do",
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:"totalCount",
            root: "data"
        },this.GridRec),
        baseParams:{transactiondate:WtfGlobal.convertToGenericDate(new Date())},
        groupField:"category",
        remoteSort:true,
        sortInfo: {field: 'productname',direction: "ASC"}
    });
    this.ByCategoryStore.on('beforeload',function(){
        WtfComMsgBox(29,4,true);
    },this);
    this.ByCategoryStore.on('load',this.storeloaded, this);
    this.ByCategoryStore.on('loadException',this.storeloaded, this);
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.edit)) {
    this.editProductCategory=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.prod.editCategoty"),  //Edit Product Category,
        id:'editProductCategory',
        scope:this,
        tooltip:WtfGlobal.getLocaleText("acc.productList.newTTforeditProduct"),  //Select a product to edit.,
        iconCls:getButtonIconCls(Wtf.etype.edit)
    });
    this.editProductCategory.on('click',this.editCategory.createDelegate(this,[config.isCustomer?true:false]),this);
   }
    this.sm = new Wtf.grid.RowSelectionModel();
        var columnArray = [];
        columnArray.push(
        {
        header: WtfGlobal.getLocaleText("acc.cust.Productcategory"), //"Category",
                dataIndex: 'category',
                hidden: true,
                fixed: true,
                renderer: WtfGlobal.deletedRenderer
        }, {
        header: WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"), //"Product",acc.productList.gridProduct
                dataIndex: 'productname',
                pdfwidth: 75,
                sortable: true,
                renderer: function (val, m, rec) {     // ERP-13247 [SJ]            
                return "<a class='jumplink' wtf:qtip='" + val + "' href='#' onClick='javascript:Wtf.onCellClickProductDetails(\"" + rec.data.productid + "\")'>" + val + "</a>";
                }
        }, {
        header: WtfGlobal.getLocaleText("acc.productList.gridProductID"),
                dataIndex: 'pid',
                sortable: true,
                align: 'left',
                pdfwidth: 75,
                renderer: function (val, m, rec) {                 // ERP-13247 [SJ]
                return "<a class='jumplink' wtf:qtip='" + val + "' href='#' onClick='javascript:Wtf.onCellClickProductDetails(\"" + rec.data.productid + "\")'>" + val + "</a>"; // ERP-13247 [SJ]
                }
        }, {
        header: WtfGlobal.getLocaleText("acc.productList.gridProductDescription"), //"Description",
                dataIndex: 'desc',
                renderer: function (val) {
//                    val = val.replace(/(<([^>]+)>)/ig,"");
                return "<div wtf:qtip='" + val + "' wtf:qtitle='" + WtfGlobal.getLocaleText("acc.productList.gridDescription") + "'>" + val + "</div>";
                },
                pdfwidth: 75
        }, {
        hidden: true,
                dataIndex: 'productid'
        }, {
        header: WtfGlobal.getLocaleText("acc.masterConfig.uom"),
                dataIndex: 'uomname',
                hidden: true
        }, {
        header: WtfGlobal.getLocaleText("acc.productList.gridProductType"), //"Product Type",
                dataIndex: 'type',
                pdfwidth: 75,
                renderer: function (val) {
                return val;
                }
        }, {
        header: WtfGlobal.getLocaleText("acc.productList.gridReorderQuantity"), //"Reorder Quantity",
                dataIndex: 'reorderquantity',
                align: 'right',
                renderer: this.unitRenderer,
                pdfwidth: 75
        }, {
        header: WtfGlobal.getLocaleText("acc.productList.gridReorderLevel"), //"Reorder Level",
                dataIndex: 'reorderlevel',
                align: 'right',
                renderer: this.unitRenderer,
                pdfwidth: 75
        }, {
        header: WtfGlobal.getLocaleText("acc.productList.gridLeadTime"), //"Lead Time(in days)",
                dataIndex: 'leadtime',
                align: 'right',
                renderer: this.LeadTimeRenderer,
                pdfwidth: 75
        }, {
        header: WtfGlobal.getLocaleText("acc.productList.gridAvailableQty"), //"Available Quantity",
                dataIndex: "quantity",
                align: 'right',
                renderer: this.unitRenderer,
                pdfwidth: 75
        },{
            header: WtfGlobal.getLocaleText("acc.masterConfig.53"), // "Product Brand",
            dataIndex: 'productBrandName',
            pdfwidth: 75
        },{
            header: WtfGlobal.getLocaleText("acc.product.LicenseType"), // "License Type",
            dataIndex: 'licensetype',
            pdfwidth: 75
       },{  
        header: WtfGlobal.getLocaleText("acc.product.ItemReusability"), // "Item Reusability", //
        dataIndex: 'itemReusability',
        pdfwidth: 75,
        renderer: function (v, metadata, record) {
            if (record.data['itemReusability'] == 0) {
                return "Reusable";
            } else if (record.data['itemReusability'] == 1) {
                return "Consumable";
            } 
        }
       },{
        header: WtfGlobal.getLocaleText("acc.product.valuationmethod"), // "Valuation Method",
        dataIndex: 'valuationmethod',
        pdfwidth: 75,
        renderer: function (v, metadata, record) {
            if (record.data['valuationmethod'] == 0) {
                return "LIFO";
            } else if (record.data['valuationmethod'] == 1) {
                return "FIFO";
            } else {
                return "Moving Average";
            }
        }
      },{
            header:WtfGlobal.getLocaleText("acc.field.DefaultWarehouse"),//"Default Warehouse",
            dataIndex:"warehouseName",
            align:'right',
            sortable: true,
            pdfwidth:75
        });
    if (!Wtf.account.companyAccountPref.productPricingOnBands || !Wtf.account.companyAccountPref.productPricingOnBandsForSales) {
        columnArray.push({
            header: WtfGlobal.getLocaleText("acc.productList.gridPurchasePrice"), //"Purchase Price",
            dataIndex: 'purchaseprice',
            align: 'right',
            renderer: function (v, m, rec) {
                if (!Wtf.dispalyUnitPriceAmountInPurchase) {
                    return Wtf.UpriceAndAmountDisplayValue;
                } else {
                    //return WtfGlobal.currencyRenderer(v,m,rec);
                    return WtfGlobal.withCurrencyUnitPriceRenderer(v, false, rec);
                }
            },
            pdfwidth: 75
        }, {
            header: WtfGlobal.getLocaleText("acc.productList.gridSalesPrice"), //"Sales Price",
            align: 'right',
            dataIndex: 'saleprice',
            pdfwidth: 75,
            renderer: function (v, metadata, record) {
                if (!Wtf.dispalyUnitPriceAmountInSales) {
                    return Wtf.UpriceAndAmountDisplayValue;
                } else if (record.data['type'] == "Inventory Non-Sale") {
                    return "N/A";
                } else {
                    //return WtfGlobal.currencyRenderer(v,false);
                    return WtfGlobal.withCurrencyUnitPriceRenderer(v, false, record);
                }
            }
        });
    }
//            },{
//                header:WtfGlobal.getLocaleText("acc.productList.gridCycleCountInterval"),//"Cycle count Interval",
//                align:'right',
//                dataIndex:'ccountinterval',
//                width:80,
//                pdfwidth:75,
//                renderer:function(v,metadata,record){
//                    if(record.data['type'] == "Service"){
//                    return "N/A";
//                    }else{ 
//                        return (v+' days');}
//                }
//            },{
//                header:WtfGlobal.getLocaleText("acc.productList.gridCycleCountTolerance"),//"Cycle count Tolerance",
//                align:'right',
//                dataIndex:'ccounttolerance',
//                width:80,
//                pdfwidth:75,
//                renderer:function(v,metadata,record){
//                    if(record.data['type'] == "Service"){
//                        return "N/A";
//                    }else{
//                        return v+' %';
//                    }
////            		return'<div class="currency">'+v+'%</div>';}
//            	}
        columnArray.push({
        header: WtfGlobal.getLocaleText("acc.field.WarrantyPeriod(Days)"), //"Cycle count Tolerance",
                align: 'center',
                dataIndex: 'warranty',
                width: 80,
                pdfwidth: 75

        });
    
    this.grid = new Wtf.grid.GridPanel({    
        store:this.ByCategoryStore,
        border:false,
        layout:'fit',
        view: new Wtf.grid.GroupingView({
            forceFit:true
        }),
        loadMask:true,
        columns:columnArray
    });
    
    this.exportButtonProdCategory=new Wtf.exportButton({
        obj:this,
        id:'prodlistbycategoryexport',
        tooltip:WtfGlobal.getLocaleText("acc.common.exportTT"),  //"Export Report details.",  
        params:{name:WtfGlobal.getLocaleText("acc.prod.tabTitleCategory")},
        menuItem:{csv:true,pdf:true,rowPdf:false,xls:true},
        get:199,
        filename:WtfGlobal.getLocaleText("acc.prod.tabTitleCategory")+"_v1",
        label:WtfGlobal.getLocaleText("acc.field.ProductsListbyCategoryReport")
 
    }),
    this.exportButtonProdCategory.setParams({
        categoryid:this.typeEditor.getValue()
    });
    
    var productCategoryConfig = {};
    productCategoryConfig.url= "ACCProductCMN/importProductCategory.do";
    var importBtnArr = Wtf.importMenuArray(this, "Product Category", this.ByCategoryStore, "", productCategoryConfig);
     
    this.importBtn = new Wtf.Action({
        text: WtfGlobal.getLocaleText("acc.common.import"),
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.common.import"),
        iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import'),
        menu: importBtnArr
    });
    
    Wtf.account.ProductListByCategory.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.ProductListByCategory,Wtf.Panel,{
        onRender: function(config){
        this.ByCategoryStore.load({
            params:{
                start:0,
                limit:30
            }
        });
        
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [{
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid],
                tbar: [WtfGlobal.getLocaleText("acc.cust.Productcategory"), this.typeEditor,{
                        xtype:'button',
                        text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
//                        tooltip:WtfGlobal.getLocaleText("acc.trial.fetchTT"),  //"Select a time period to view corresponding trial balance records.",
                        iconCls:'accountingbase fetch',
                        scope:this,
                        handler:this.loadTypeStore
                        }, '-', (this.editProductCategory)?this.editProductCategory:""],
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.ByCategoryStore,
                    displayInfo: true,
                    plugins: this.pP = new Wtf.common.pPageSize({
                        id : "pPageSize_"+this.id
                    }),
                    items:['-',(!WtfGlobal.EnableDisable(this.uPermType, this.permType.exportproducts))?this.exportButtonProdCategory:'', '-',(!WtfGlobal.EnableDisable(this.uPermType, this.permType.importproduct))?this.importBtn:'']
                })
            }]
        }); 
        this.add(this.leadpan);
        
        this.on("activate",function(){
            if(this.isCategoryUpdated){
                this.ByCategoryStore.reload();
                this.isCategoryUpdated=false;
            }
        },this);
        Wtf.account.ProductListByCategory.superclass.onRender.call(this,config);
    },
    setValue:function(store){
          var record = new Wtf.data.Record({
             name:'All',
             id:'All'
         });
         var index=this.productCategoryStore.find('name','All');
         if(index==-1){
             this.productCategoryStore.insert(0,record);    
             this.typeEditor.setValue("All");
         }        
        if (!Wtf.getCmp("NewProductByCategoryDetails")) {
            this.hideTransactionReportFields(Wtf.account.HideFormFieldProperty.productForm, store);
        }
    },
        
    hideTransactionReportFields: function(array,store) {
        if (array) {
            var cm = this.grid.getColumnModel();
            for (var i = 0; i < array.length; i++) {
                if(array[i].isReportField) {
                    for (var j = 0; j < cm.config.length; j++) {
                        if (cm.config[j].header == array[i].dataHeader || (cm.config[j].dataIndex == array[i].fieldId)) {
                            cm.config[j].hideable = !(array[i].isReportField);
                            cm.setHidden(j, array[i].isReportField);
                        }
                    }
                }
            }
            this.grid.reconfigure(store, cm);
        }
    },
    loadTypeStore:function(a,rec){
        var categoryid = this.typeEditor.getValue();
        if(categoryid=='All') {
            this.ByCategoryStore.load({params: {start:0, limit:this.pP.combo.value}});
        } else {
            this.ByCategoryStore.load({params: {start:0, limit:this.pP.combo.value, categoryid:categoryid}});
        }
    },
    
    unitRenderer:function(value,metadata,record){
        if(record.data['type'] == "Service"){
        	return "N/A";
        }
    	var unit=record.data['uomname'];
            value=parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+" "+unit;
        return value;
    },
    
    LeadTimeRenderer:function(value,metadata,record){
    	if(record.data['type'] == "Service"){
        	return "N/A";
        }
    	if(value==1){
            value=value+" Day";
        }
        else{
            value=value+" Days";
        }
        return value;
    },
    
    storeloaded:function(store){
        Wtf.MessageBox.hide();
    },
    
    editCategory:function() {
        callProductGroupingWin(this.ByCategoryStore);
    }
});

//'Product Grouping' edit category window
Wtf.productgroupingwin = function (config){
    this.ByCategoryStore=config.ByCategoryStore;
    Wtf.apply(this,config);
    Wtf.productgroupingwin.superclass.constructor.call(this,{
        buttons:[{
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //"Save",
            scope:this,
            handler:function () {
                if(!this.ProductGroupingForm.form.isValid())
                {
                    return;
                } else {
                    
                    //  Check valid Products Selected or Not
                    
//                    var isInvalidProductsSelected = WtfGlobal.isInvalidProductsSelected(this.productList);
                    var isInvalidProductsSelected = false;
                    if(isInvalidProductsSelected){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.select.validproduct")],3);
                        return;
                    }
//                    var isInvalidProductCategorySelected = WtfGlobal.isInvalidProductsSelected(this.productCategory);//to check entered category is correct or not
                    var isInvalidProductCategorySelected = false;
                    if(isInvalidProductCategorySelected){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.select.validproductCategory")],3);
                        return;
                    }
                    var productList=this.productList.getValue();
                    var productCategory=this.productCategory.getValue();
                    var industryCode=this.industryCodeCmb.getValue();
                    var param={
                        mode:14,
                        productList:productList,
                        productCategory:productCategory,
                        industryCode:industryCode
                    }
                    Wtf.Ajax.requestEx({
                        url : "ACCProduct/saveProductCategoryMapping.do",
                        params:param
                    },this,
                    function(req,res){
                        var restext=req;
                        if(restext.success){
                            this.close();
                            var superThis = this;
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.prod.editCategoty"),(WtfGlobal.getLocaleText("acc.rem.234")+(Wtf.account.companyAccountPref.integrationWithPOS?WtfGlobal.getLocaleText("acc.rem.POS234"):""))],0,"","",function(btn){
                                if(btn=="ok"){
                                    superThis.ByCategoryStore.reload();
                                     
                                }
                            });
                        } else{
                            if(req.accException){
                                this.industryCodeCmb.setValue("");
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.prod.editCategoty"),req.msg],req.success*2+2);
                            }else{
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.prod.editCategoty"),WtfGlobal.getLocaleText("acc.rem.235")],1);
                            }
                         }
                    },
                    function(req){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.prod.editCategoty"),WtfGlobal.getLocaleText("acc.rem.235")],1);
                    });
                }
            }
        }, {
            text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //"Cancel",
            scope:this,
            handler:function (){
                this.close();
            }
        }]
    });
}

Wtf.extend(Wtf.productgroupingwin,Wtf.Window,{
    layout:"border",
    modal:true,
    id:'productgroupinglinkforaccounting',
    width:450,
    height:280,
    resizable:false,
    iconCls: "pwnd favwinIcon",
    initComponent:function (){
        Wtf.productgroupingwin.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetProductGroupingForm();
        Wtf.form.Field.prototype.msgTarget = 'side';
        this.add(this.northPanel);
        this.add(this.ProductGroupingForm);
    },
    GetNorthPanel:function (){
        var msg=""
        if(Wtf.account.companyAccountPref.countryid == '137'){
            msg=WtfGlobal.getLocaleText("acc.prod.editCategoryDescForMalaysian");
        }else{
            msg =WtfGlobal.getLocaleText("acc.prod.editCategoryDesc")
        }
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:85,
            border:false,
            bodyStyle:"background:white;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(WtfGlobal.getLocaleText("acc.prod.editCategoty"), msg,'../../images/createuser.png',false,'0px 0px 0px 0px')
        });
    },
    GetProductGroupingForm:function (){
        
        this.productRec = Wtf.data.Record.create ([
            {name:'productid'},
            {name:'pid'},
            {name:'type'},
            {name:'productname'}
    
        ]);
        this.productStore = new Wtf.data.Store({
            url:"ACCProductCMN/getProductsForCombo.do",
            baseParams:{excludeParent:true, module_name : "PRODUCT_CATEGORY"},
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.productRec)
        });
        this.productStore.load();
        
        this.ProductCategoryRec=new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);
        
        this.ProductCategoryStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.ProductCategoryRec),
            url:"ACCMaster/getMasterItems.do",
            baseParams:{
                mode:112,
                groupid:19
            }
        });
        
        this.ProductCategoryStore.load();
        this.ProductCategoryStore.on('load',this.handleOnLoadOfProductCategoryStore,this);
        
            this.masterItemGroupRec = Wtf.data.Record.create([
            {
                name: 'id'
            },

            {
                name: 'name'
            }
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
        this.industryCodeCmb = new Wtf.form.ComboBox({
                triggerAction: 'all',
                mode: 'local',
                name: 'industryCodeId',
                hiddenName: 'industryCodeId',
                labelWidth : '130',
                valueField: 'id',
                hidden:true, //  Currently product level industry code functionality  is hidden
                hideLabel:true,
                displayField: 'name',
                store: this.masterItemTempStore,
                fieldLabel:WtfGlobal.getLocaleText("acc.Industry.code"),
               // value: editIndudtry,
                anchor:'85%',
                typeAhead: true,
                forceSelection: true
            });
            
            var baseParamsforCombo = this.productStore.baseParams;
            var configforCombo = {fieldLabel: WtfGlobal.getLocaleText("acc.product.productName"), multiSelect: true}
            this.productList = CommonERPComponent.createProductMultiselectPagingComboBox(200, 300, 30, this, baseParamsforCombo, configforCombo);
        
        this.productCategory = new Wtf.common.Select(Wtf.apply({
            multiSelect:true,
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.Productcategory")+"*", //"Category*" ,
            forceSelection:true,
            listWidth:320
        },{
            name:"productcategory",
            id:"productcategory",
            store:this.ProductCategoryStore,
            valueField:'id',
            displayField:'name',
            emptyText:WtfGlobal.getLocaleText("acc.prod.categoryEmptyText"), //"Select Category for Product",
            anchor:'85%',
            mode: 'local',
            selectOnFocus:true,
            allowBlank:false,
            triggerAction:'all',
            typeAhead: true,
            scope:this
        }));
        
        this.productCategory.on('change', function(comp, newValue, oldValue) {
            var FIND = this.productCategory.getValue();
            var index = this.ProductCategoryStore.findBy(function(rec) {
                var parentname = rec.data['id'];
                if (parentname == FIND)
                    return true;
                else
                    return false
            })
            if(index>0){
               this.industryCodeCmb.setValue(comp.store.data.items[index].json.industryCodeId)
            }
            /*
             * If value is None in product category combo then industry code is blank
             */
             if(index==0){
                 this.industryCodeCmb.setValue("");
             }
        }, this);
        
        this.ProductGroupingForm = new Wtf.form.FormPanel({
            region:"center",
            border:false,
            bodyStyle:"background-color:#f1f1f1;padding:20px",
            items:[this.productList, this.productCategory,this.industryCodeCmb]
        });
    },
    handleOnLoadOfProductCategoryStore : function(){
        var productCategoryrecordForNoneValue =new this.ProductCategoryRec({
            id:'None',
            name:'None'
        });
        this.ProductCategoryStore.insert(0,productCategoryrecordForNoneValue);
    }
});

Wtf.updateSalesPriceRuleWindow = function (config){
    this.addEvents({
        'setAutoNumbers':true
    });
    Wtf.apply(this,config);
    Wtf.updateSalesPriceRuleWindow.superclass.constructor.call(this,{
        buttons:[{
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //"Save",
            id: 'saveSalesPriceRuleBtn',
            scope:this,
            handler:function () {
                Wtf.getCmp('saveSalesPriceRuleBtn').disable();
                if(!this.AddEditForm.form.isValid())
                {
                   Wtf.getCmp('saveSalesPriceRuleBtn').enable();
                    return;
                } else {    
                    var gridStore=this.sequencenoGrid.getStore();
                    var lowerlimit=Wtf.getCmp("lowerlimitid").getValue();
                    var upperlimit=Wtf.getCmp("upperlimitid").getValue();
                    var percentage=this.percentageTypeCombo.getValue();
                    var amount=Wtf.getCmp("amountid").getValue();
//                    if(lowerlimit!="" && upperlimit!=""){
//                        if(gridStore.getCount()>0){
//                            for(var i=0;i<gridStore.getCount();i++){//Limit Validation
//                                var rec=gridStore.data.get(i);
//                                if(this.PriceTypeCombo.getValue() == '1'){
//                                    if((lowerlimit==rec.data.lowerlimit ||lowerlimit > rec.data.lowerlimit && lowerlimit < rec.data.upperlimit) && rec.data.priceType == '1'){
//                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.master.UpdatePriceRule.limitError")],1);
//                                        return;
//                                    }
//                                }else{
//                                    if((lowerlimit==rec.data.lowerlimit ||lowerlimit > rec.data.lowerlimit && lowerlimit < rec.data.upperlimit) && rec.data.priceType == '2'){
//                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.master.UpdatePriceRule.limitError")],1);
//                                        return;
//                                    }
//                                }
//                            }
//                        }
//                    }
                    if(percentage==1 && amount >100){ //Percentage Validation
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.master.UpdatePriceRule.perError")],1);
                        Wtf.getCmp('saveSalesPriceRuleBtn').enable();
                        return;
                    }
                    var categoryName='';
                    if(this.RuleTypeCombo.getValue() == '1'){
                        var recCategory=WtfGlobal.searchRecord(this.Category.store,this.Category.getValue(),"id")
                        categoryName=recCategory.data.name;
                    }
                    var categoryid=this.Category.getValue();
                    if(categoryid == 'All'){
                        categoryid="";
                    }
                    var basedOn = '0';
                    if (this.PriceTypeCombo.getValue() == '1') {
                        basedOn = this.basedOnCombo.getValue();
                    }
//                    arguments[0].disable();
                    var param={
                         ruletype:this.RuleTypeCombo.getValue(),
                         pricetype:this.PriceTypeCombo.getValue(),
                         category:categoryid,
                         incrementrule:this.IncreamentTypeCombo.getValue(),
                         categoryName:categoryName,
                         lowerlimit:lowerlimit,
                         upperlimit:upperlimit,
                         percentagetype:this.percentageTypeCombo.getValue(),
                         amount:amount,
                         currency: this.Currency.getValue(),
                         basedOn: basedOn
                   }
                    Wtf.Ajax.requestEx({
                        url : "ACCProduct/saveProducsPriceRule.do",
                        params:param
                    },this,
                    function(req,res){
                        var restext=req;
                        if(restext.success){    
                               this.sequenceFormatStore.load();
                               if(this.RuleTypeCombo.getValue() == '1'){
                                    Wtf.getCmp("lowerlimitid").enable();
                                    Wtf.getCmp("upperlimitid").enable();
                                }
                                this.AddEditForm.getForm().reset();
                                this.Category.enable();
                                if(this.RuleTypeCombo.getValue() == '1'){
                                    Wtf.getCmp("lowerlimitid").disable();
                                    Wtf.getCmp("upperlimitid").disable();
                                }
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.save")],0);                            
                                Wtf.getCmp('saveSalesPriceRuleBtn').enable();
                        } else {
                            Wtf.getCmp('saveSalesPriceRuleBtn').enable();
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.error")],1);
                        }
                        	
                    },
                    function(req){
                        var restext=req;
                        if(restext.msg !=""){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),restext.msg],1);
                        } else {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.error")],1);
                        }
                        Wtf.getCmp('saveSalesPriceRuleBtn').enable();
                    });
                }
            }
        }, {
            text:"Close",  //"Close",
            scope:this,
            handler:function (){
                this.close();
            }
        }]
    });    
}

Wtf.extend(Wtf.updateSalesPriceRuleWindow,Wtf.Window,{
    layout:"border",
    modal:true,
    title:"Update Price Rule",
    id:'setpriceforaccounting',
    width:950,
    height:510,
    resizable:false,
    iconCls: "pwnd deskeralogoposition",
    initComponent:function (){
        Wtf.updateSalesPriceRuleWindow.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetAddEditForm();
        Wtf.form.Field.prototype.msgTarget = 'side';
        this.add(this.northPanel);        
        this.add(this.centerPanel);
        this.add(this.AddEditForm);
        this.percentageTypeCombo.setValue('1');
        this.RuleTypeCombo.setValue('1');
        if (!Wtf.account.companyAccountPref.productPricingOnBandsForSales) {
        this.PriceTypeCombo.setValue('1');
        } else if (!Wtf.account.companyAccountPref.productPricingOnBands) {
            this.PriceTypeCombo.setValue('2');
        }
        this.basedOnCombo.setValue('1'); // by default set to 'Existing Price'
        
        this.IncreamentTypeCombo.setValue('1');
        this.Category.setValue("All");
     },
    GetNorthPanel:function (){
      this.sequenceFormatRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'lowerlimit'},
            {name: 'upperlimit'},
            {name: 'percentagetype'},
            {name: 'percentagevalue'},
            {name: 'amount'},
            {name: 'category'},
            {name: 'categoryid'},
            {name: 'increamentordecreamentType'},
            {name: 'priceType'},
            {name: 'ruleType'},
            {name: 'increamentordecreamentTypeName'},
            {name: 'priceTypeName'},
            {name: 'currency'},
            {name: 'currencyid'},
            {name: 'ruleTypeName'},
            {name: 'basedOnId'},
            {name: 'basedOnName'}
         ]);
         
      this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
                
            },this.sequenceFormatRec),
            url : "ACCProduct/getProducsPriceRule.do",
            baseParams:{
                productPriceinMultipleCurrency : Wtf.account.companyAccountPref.productPriceinMultipleCurrency
            }
         });                 
        this.sequenceFormatStore.load();
       this.percentageTypeStore = new Wtf.data.SimpleStore({
            fields:[{
                name:'id'
            },{
                name:'name'
            }],
            data:[['1','Percentage'],['2','Flat']]
        });
        
         this.RuleTypeStore = new Wtf.data.SimpleStore({
            fields:[{
                name:'id'
            },{
                name:'name'
            }],
            data:[['1','Product Category'],['2','Product Price']]
        });
        
        var priceTypeStoreDataArr = [];
        if (!Wtf.account.companyAccountPref.productPricingOnBandsForSales) {
            priceTypeStoreDataArr.push(['1','Sales']);
        }
        if (!Wtf.account.companyAccountPref.productPricingOnBands) {
            priceTypeStoreDataArr.push(['2','Purchase']);
        }
        
       this.PriceTypeStore = new Wtf.data.SimpleStore({
            fields:[{
                name:'id'
            },{
                name:'name'
            }],
            data: priceTypeStoreDataArr
        });
        
        var basedOnStoreDataArr = [];
        basedOnStoreDataArr.push(['1','Existing Price']);
        basedOnStoreDataArr.push(['2','Average Cost']);
        basedOnStoreDataArr.push(['3','Most Recent Cost']);
        basedOnStoreDataArr.push(['4','Initial Purchase Price']);
        
       this.basedOnStore = new Wtf.data.SimpleStore({
           fields:[{name:'id'}, {name:'name'}],
           data: basedOnStoreDataArr
       });
        
        this.IncreamentTypeStore = new Wtf.data.SimpleStore({
            fields:[{
                name:'id'
            },{
                name:'name'
            }],
            data:[['1','Increment'],['2','Decrement']]
        });
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect: true
        });
   
        this.sequenceCM= new Wtf.grid.ColumnModel([this.sm,{
                  header:WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.sequenceCM1"),
                  dataIndex:'ruleTypeName'
              },{
                  header:WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.sequenceCM2"),
                  dataIndex:'priceTypeName'
              },{
                  header: WtfGlobal.getLocaleText("acc.field.basedOn"),
                  dataIndex: 'basedOnName'
              },{
                  header:WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.sequenceCM3"),
                  dataIndex:'category'
              },{
                  header:WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.sequenceCM4"),
                  dataIndex:'lowerlimit'
              },{
                  header:WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.sequenceCM5"),
                  dataIndex:'upperlimit'
              },{
                  header:WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.sequenceCM6"),
                  dataIndex:'increamentordecreamentTypeName'
              },{
                  header:WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.sequenceCM6") + ' Type',
                  dataIndex:'percentagevalue'
              },{
                    header :WtfGlobal.getLocaleText("acc.coa.gridCurrency"),  //'Price',
                    dataIndex: 'currency',
                    hidden:!Wtf.account.companyAccountPref.productPriceinMultipleCurrency,
                    align:'left'
              },{
                  header:WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.sequenceCM6") + ' by',
//                      'Amount'+"("+WtfGlobal.getCurrencyName()+")",
                  dataIndex:'amount'
              },{
                  header:WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.sequenceCM8"),
                  align:'center',
                  renderer:function(){
                      return "<div class='pwnd delete-gridrow' > </div>";
                  }
              }
        ]);
      this.sm.on('selectionchange', function(sm) {
        if(sm.getCount() == 1){
             this.UpdatePrice.enable();
        }else{
             this.UpdatePrice.disable();
        }
    }, this);
     this.UpdatePrice = new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.UpdatePrice"),
                tooltip:WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.UpdatePrice"),
                disabled: true,
                scope: this,
                handler:this.UpdatePriceFunction 
     });
     this.sequencenoGrid = new Wtf.grid.GridPanel({
          store: this.sequenceFormatStore,
          cm:this.sequenceCM,
          height:190,
          selModel: this.sm,
          tbar: [this.UpdatePrice],
          viewConfig:{
              forceFit:true,
              emptyText:WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.Norecordfound")
          }
      })     
    this.sequencenoGrid.on("cellclick", this.deleteSequence, this);
    var text = WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.ManagePriceofProducts") +"<br>" +WtfGlobal.getLocaleText("acc.master.UpdatePriceRule.priceCannotUpdateNote");
     this.northPanel = new Wtf.Panel({
            region:"north",
            height:90,
            border:false,
            bodyStyle:"background:white;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.UpdatePrice"),text,"../../images/accounting_image/price-list.gif",false,'0px 0px 0px 0px')   

        });
        this.centerPanel = new Wtf.Panel({
            region:"center",
            items:[this.sequencenoGrid]
        })
    },    
    GetAddEditForm:function (){
        this.titlePanel = new Wtf.Panel({
            border:false,
            bodyStyle:"padding-bottom: 5px;text-align: center;"
        });
        
        this.percentageTypeCombo = new Wtf.form.ComboBox({
                triggerAction: 'all',
                mode: 'local',
                valueField: 'id',
                displayField: 'name',
                store: this.percentageTypeStore,
                fieldLabel:WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.percentageTypeCombo")+'*',
                typeAhead: true,
                width:200,
                forceSelection: true,
                hiddenName: 'type'
            }); 
            
         this.RuleTypeCombo = new Wtf.form.ComboBox({
                triggerAction: 'all',
                mode: 'local',
                valueField: 'id',
                width: 200,
                displayField: 'name',
                store: this.RuleTypeStore,
                fieldLabel: WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.RuleTypeCombo")+'*',
                typeAhead: true,
                forceSelection: true,
                hiddenName: 'type'
            });    
         this.RuleTypeCombo .on('select',function(obj,rec,index){
             if(rec.data.id == '1'){
                 Wtf.getCmp("lowerlimitid").clearInvalid();
                 Wtf.getCmp("upperlimitid").clearInvalid();
                 this.Category.enable();
                 Wtf.getCmp("lowerlimitid").enable();
                 Wtf.getCmp("upperlimitid").enable();
                 Wtf.getCmp("lowerlimitid").reset();
                 Wtf.getCmp("upperlimitid").reset();
                 Wtf.getCmp("lowerlimitid").disable();
                 Wtf.getCmp("upperlimitid").disable();
             }else{
                 Wtf.getCmp("lowerlimitid").enable();
                 Wtf.getCmp("upperlimitid").enable();
                 Wtf.getCmp("lowerlimitid").reset();
                 Wtf.getCmp("upperlimitid").reset();
                 this.Category.disable();
             }
         },this);
         this.PriceTypeCombo = new Wtf.form.ComboBox({
                triggerAction: 'all',
                mode: 'local',
                valueField: 'id',
                displayField: 'name',
                store: this.PriceTypeStore,
                fieldLabel:WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.PriceTypeCombo")+'*',
                typeAhead: true,
                width:200,
                forceSelection: true,
                hiddenName: 'type'
            });   
        this.PriceTypeCombo.on('select', function() {
            if (this.PriceTypeCombo.getValue() == 1) { // if price type is 'Sales'
                this.Currency.setValue(WtfGlobal.getCurrencyID());
                this.Currency.disable();
                this.basedOnCombo.enable();
            } else {
                this.Currency.enable();
                this.basedOnCombo.setValue('1'); // by default set to 'Existing Price'
                this.basedOnCombo.disable();
            }
        }, this);
        this.basedOnCombo = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.basedOn") + WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.basedOn.helpLabel")),
            store: this.basedOnStore,
            hiddenName: 'type',
            valueField: 'id',
            displayField: 'name',
            triggerAction: 'all',
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            editable: false,
            width: 200
        });
        this.basedOnCombo.on('select', function() {
            if (this.basedOnCombo.getValue() == 1) { // if 'Based On' is 'Existing Price'
                this.Currency.enable();
            } else {
                this.Currency.setValue(WtfGlobal.getCurrencyID());
                this.Currency.disable();
            }
        }, this);
           this.IncreamentTypeCombo = new Wtf.form.ComboBox({
                triggerAction: 'all',
                mode: 'local',
                valueField: 'id',
                displayField: 'name',
                store: this.IncreamentTypeStore,
                fieldLabel: WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.IncreamentTypeCombo")+'*',
                typeAhead: true,
                width:200,
                forceSelection: true,
                hiddenName: 'type'
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
         
        this.currencyStore.load();
        
        this.Currency= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.currency.cur"),  //'Currency',
            hiddenName:'currencyid',
            hidden:!Wtf.account.companyAccountPref.productPriceinMultipleCurrency,
            hideLabel:!Wtf.account.companyAccountPref.productPriceinMultipleCurrency, 
            id:"currency"+this.id,
            store:this.currencyStore,
            valueField:'currencyid',
            allowBlank :!Wtf.account.companyAccountPref.productPriceinMultipleCurrency,
            forceSelection:Wtf.account.companyAccountPref.productPriceinMultipleCurrency, 
            displayField:'currencyname',
            scope:this,
            selectOnFocus: true,
            width: 200
        });
         this.currencyStore.on("load", function(){
              this.Currency.setValue(WtfGlobal.getCurrencyID());
        }, this);
        
         this.productCategoryRecord = Wtf.data.Record.create ([
        {
            name:'id'
        },{
            name:'name'
        }
        ]);    
       this.productCategoryStore = new Wtf.data.Store({
        url: "ACCMaster/getMasterItems.do",
        baseParams:{
                mode:112,
                groupid:19
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.productCategoryRecord)
        });
     
        this.Category = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.cust.Productcategory"),
            store: this.productCategoryStore,
            displayField:'name',
            valueField:'id',
            width:200,
            mode: 'local',
            triggerAction: 'all',
            typeAhead:true,
            selectOnFocus:true
        });
        this.productCategoryStore.load();
        this.productCategoryStore.on('load',this.setValue,this);
     
        this.AddEditForm = new Wtf.form.FormPanel({
            region:"south",
            border:false,
            height:160,
            labelWidth:170,
            defaultType : 'textfield',
            bodyStyle:"background-color:#f1f1f1;padding:15px 35px 35px",
            items:[this.titlePanel,
                    this.ProductPricePanel = new Wtf.Panel({
                    layout: "column",
                    border: false,
                    items:[
                    new Wtf.Panel({
                        columnWidth: 0.5,
                        layout: "form",
                        border: false,
                        anchor:'100%',
                        items :[this.RuleTypeCombo,this.Category,this.Currency,
                                {
                                    xtype: 'numberfield',
                                    fieldLabel:WtfGlobal.getLocaleText("acc.masterconfig.setlowerlimitValue"),
                                    name: "lowerlimit",
                                    labelWidth:170,
                                    width: 200,
                                    maxLength:50,
                                    allowBlank: false,
                                    disabled:true,
                                    id: "lowerlimitid",
                                    decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                                },{
                                    xtype: 'numberfield',
                                    fieldLabel:WtfGlobal.getLocaleText("acc.masterconfig.setupperlimitValue"),
                                    name: "upperlimit",
                                    width: 200,
                                    labelWidth:170,
                                    maxLength:50,
                                    disabled:true,
                                    allowBlank: false,
                                    id: "upperlimitid",
                                    decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                                }]
                    }),
                    new Wtf.Panel({
                        columnWidth: 0.5,
                        layout: "form",
                        border: false,
                        anchor:'100%',
                        items :[this.PriceTypeCombo, this.basedOnCombo, this.IncreamentTypeCombo,
                                this.percentageTypeCombo, {
                                    xtype: 'numberfield',
                                    fieldLabel:WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.sequenceCM6") + ' by',
//                                        WtfGlobal.getLocaleText("acc.masterconfig.amount")+"("+WtfGlobal.getCurrencyName()+")*",
                                    name: "amount",
                                    labelWidth:170,
                                    width: 200,
                                    maxLength:50,
                                    allowBlank: false,
                                    id: "amountid",
                                    decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
                               }]
                 })]
                })]
        });
    },
     setValue:function(){
          var record = new Wtf.data.Record({
             name:'All',
             id:'All'
         });
         var index=this.productCategoryStore.find('name','All');
         if(index==-1){
             this.productCategoryStore.insert(0,record);    
             this.Category.setValue("All");
         }
        
    },
    UpdatePriceFunction:function() {
       Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.AreYouSure"),function(btn){
            if(btn=="yes") {        
        var ruleRecord=this.sm.getSelected();
        var categoryIds="";
//        var currentdate=WtfGlobal.convertToGenericDate(Wtf.serverDate.clearTime(true));
        var currentdate = WtfGlobal.convertToGenericDate(new Date());
        if(ruleRecord.data.categoryid == 'All'){
            for(var i=1;i<this.productCategoryStore.getCount();i++){
                 var rec=this.productCategoryStore.getAt(i);
                 categoryIds+="'"+rec.data.id +"',";
            }
            
        }
                Wtf.Ajax.requestEx({
                        url: "ACCProductCMN/updateProductPriceRulewise.do",
                        params:{
                            id:ruleRecord.data.id,
                            lowerlimit:ruleRecord.data.lowerlimit,
                            upperlimit:ruleRecord.data.upperlimit,
                            percentagetype:ruleRecord.data.percentagetype,
                            percentagevalue:ruleRecord.data.percentagevalue,
                            amount:ruleRecord.data.amount,
                            categoryIds:categoryIds,
                            categoryid:ruleRecord.data.categoryid,
                            increamentordecreamentType:ruleRecord.data.increamentordecreamentType,
                            priceType:ruleRecord.data.priceType,
                            ruleType:ruleRecord.data.ruleType,
                            currencyid:ruleRecord.data.currencyid,
                            currentdate: currentdate,
                            basedOnId: ruleRecord.data.basedOnId,
                            isAssemblySubProduct: true // need this flag whilce calculating 'Average Cost'
                       }   
                    },this,
                    function(req,res){
                        var restext=req;
                        if(restext.success){
                             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.priceupdate")+"<br><br>"+WtfGlobal.getLocaleText("acc.master.UpdatePriceRule.priceCannotUpdateNote")],0);                            
                        } else 
                        	WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.priceupdateError")],1);

                    },
                    function(req){
                        var restext=req;
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.priceupdateError")],1);
                    });
            }},this);            
         
   },
    deleteSequence:function(gd, ri, ci, e) {
       var event = e;
        if(event.target.className == "pwnd delete-gridrow") {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.AreYouSureDelete"),function(btn){
            if(btn=="yes") {            
            var itemid = gd.getStore().getAt(ri).data.id;
            Wtf.Ajax.requestEx({
                        url : "ACCProduct/deleteProductPriceRule.do",
                        params:{
                           itempriceid:itemid
                        }
                    },this,
                    function(req,res){
                        var restext=req;
                        if(restext.success){
                            this.sequenceFormatStore.remove(this.sequenceFormatStore.getAt(ri));
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.Delete")],0);                            
                        } else 
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.DeleteError")],1);

                    },
                    function(req){
                        var restext=req;
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.DeleteError")],1);
                    });
        }

    }, this)
    }
   }
});

//++++++++++++++++++++++++ Call of Creating Upoload Window +++++++++++++++++++++++++++++++++++++++//

Wtf.UploadImage = function(config) {
    Wtf.apply(this, config);
    
    var title = WtfGlobal.getLocaleText("erp.uploadImage.title"); //"Add Product Image"
        
    var fieldlabel = WtfGlobal.getLocaleText("erp.upload.winimgfield.label"); //"Product Image"
    
    var url = "ACCProduct/addImage.do?imageAdd=true&mapid="+this.mapid+"&keyid="+this.recid+"";
    Wtf.UploadImage.superclass.constructor.call(this,{
        title: title,
        id: "uploadImageWindow",
        modal: true,
        resizable: false,
        iconCls :getButtonIconCls(Wtf.etype.deskera),
        width : 400,
        height: 100,
        buttonAlign : 'right',
        items: [this.uploadWin=new Wtf.form.FormPanel({
            frame:true,
            method : 'POST',
            fileUpload : true,
            waitMsgTarget: true,
            url: url,
            id: this.id+"imageUploadFromPanel",
            border: false,
            items: [{
                border: false,
                id: "imageAddressField",
                xtype: "textfield",
                inputType: 'file',
                fieldLabel: fieldlabel,
                name: "document"
            },{
                xtype: "hidden",
                name: "refid",
                value: this.recid
            }]
        })],
        buttons: [{
            text:  WtfGlobal.getLocaleText("acc.invoiceList.bt.upload"),//"Upload",
            handler:function()
            {
                var idx = this.idX;
                var keyid = this.keyid;
                var fname=Wtf.getCmp("imageAddressField").getValue();
                var upwin=Wtf.getCmp("uploadImageWindow");
                if(Wtf.getCmp("imageAddressField").getValue() != ""){
                    Wtf.commonWaitMsgBox("Adding image...");
                    this.uploadWin.form.submit({
                        params: {
                            flag: 83,
                            type: 1                        
                        },
                        scope:this,
                        success: function(a,b,c){
                            var res=eval('('+b.response.responseText+')');
                        
                            Wtf.updateProgress();
                            if(res.data.success){                            
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),WtfGlobal.getLocaleText("erp.msg.imagehasbeenuploaded")]); //"Image has been uploaded successfully."
                                upwin.close(); 
                            }
                            else{
                                if(res.data.msg == "invalidformat"){
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("erp.msg.invalidfileformat")]); //Invalid image Format.
                                }
                                else{
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("erp.msg.imagecouldnotbeuploaded")]); // "Sorry! Image could not be uploaded successfully. Please try again."];
                                }
                            }
                        },
                        failure: function(){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("erp.warning.imageuploadfailure")]); //"Please select an image to upload."];
                            upwin.close();
                        }
                    });
                } else {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("erp.msg.imagecouldnotbeuploaded")]);  // "Sorry! Image could not be uploaded successfully. Please try again."];
                }
            },
            scope: this
        },{
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),//"Cancel",
            scope:this,
            handler: function(){
                this.close();
            }
        }]
    });
}

Wtf.extend(Wtf.UploadImage, Wtf.Window, {

    });

//++++++++++++++++++++++++ Create New Panel To View Product  +++++++++++++++++++++++++++++++++++++++//

Wtf.ProductProfilerPanel = function(config) {
    this.record = config.record;
    this.fileName= config.fileName;  
    Wtf.apply(this, config);
    Wtf.ProductProfilerPanel.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.ProductProfilerPanel, Wtf.Panel, {
               
    initComponent: function(config){          
        Wtf.ProductProfilerPanel.superclass.initComponent.call(this,config);  
        var indComServiceProd=false;
        var isExice=false;
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.isSTApplicable && this.record.data.type=='Service' ){  // India Compliance
            indComServiceProd=true;
        }
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.isExciseApplicable && this.record.data.type!='Service' ){  // India Compliance
            isExice=true;
        }
        
        //============================================= For Product  Details Left Data View =======================================================

        this.productStore=new Wtf.data.SimpleStore({
            fields: ['type','pname','pid','uom','displayUoM','reorderlevel','reorderquantity','description','initialquantity','purchaseaccountname','purchaseretaccountname',
            'isLocationForProduct','isWarehouseForProduct','packaging','transferuom','serviceTaxCode','indComServiceProd'],
            data : [
            [this.record.data.type,this.record.data.productname,this.record.data.pid,this.record.data.uomname,this.record.data.displayUoMName,this.record.data.reorderlevel,this.record.data.reorderquantity,this.record.data.desc,this.record.data.initialquantity,
            this.record.data.purchaseaccountname,this.record.data.purchaseretaccountname,this.record.data.isLocationForProduct,this.record.data.isWarehouseForProduct,this.record.data.packagingValue,this.record.data.transferuomname,this.record.data.serviceTaxCode,indComServiceProd]
            ]          
        });


        var tpl = new Wtf.XTemplate(
            '<table border="0" width="100%" style="padding-left:3%;padding-top:3%;">',
            '<tpl for=".">', 
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.productList.gridProductType")+' : </td><td class="leadDetailTD">{type}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.invoiceList.expand.pName")+' : </td><td class="leadDetailTD">{pname}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.invoiceList.expand.PID")+' : </td><td class="leadDetailTD">{pid}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.masterConfig.uom")+' : </td><td class="leadDetailTD">{uom}</td></tr>',
            (CompanyPreferenceChecks.displayUOMCheck() ?  '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.product.displayUoMLabel")+' : </td><td class="leadDetailTD">{displayUoM}</td></tr>' : ''),
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.productList.gridReorderLevel")+' : </td><td class="leadDetailTD">{reorderlevel}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.productList.gridReorderQuantity")+' : </td><td class="leadDetailTD">{reorderquantity}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.invReport.desc")+' : </td><td class="leadDetailTD"> {description}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.product.initialQty")+' : </td><td class="leadDetailTD"> {initialquantity}</td></tr>',          
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.product.purchaseAccount")+' : </td><td class="leadDetailTD"> {purchaseaccountname}</td></tr>',          
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.product.purchaseReturnAccount")+' : </td><td class="leadDetailTD"> {purchaseretaccountname}</td></tr>',          
//            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.product.innerUOM")+' : </td><td class="leadDetailTD"> {initialquantity}</td></tr>',          
//            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.product.casingUOM")+' : </td><td class="leadDetailTD"> {initialquantity}</td></tr>',          
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.product.isLocation")+' : </td><td class="leadDetailTD"> {isLocationForProduct}</td></tr>',          
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.product.iswarehouse")+' : </td><td class="leadDetailTD"> {isWarehouseForProduct}</td></tr>',          
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.product.packaging")+' : </td><td class="leadDetailTD"> {packaging}</td></tr>',          
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.product.transferUoM")+' : </td><td class="leadDetailTD"> {transferuom}</td></tr>',          
            '<tpl if="indComServiceProd">',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.field.AccountingcodeService")+' : </td><td class="leadDetailTD"> {serviceTaxCode}</td></tr>',
            '</tpl></tpl></table>'
            );
        
        //============================================= For Product  Details Right Data View =======================================================            
        var salesPrice=this.record.data.initialsalesprice;
        if(!Wtf.dispalyUnitPriceAmountInSales){//If there is no permission to view sales Price then we will show *****
            salesPrice = Wtf.UpriceAndAmountDisplayValue;
        }
        var purchasePrice=this.record.data.purchaseprice;
        if(!Wtf.dispalyUnitPriceAmountInPurchase){//If there is no permission to view purchase Price then we will show *****
            purchasePrice = Wtf.UpriceAndAmountDisplayValue;
        }
        var currentsalesprice = this.record.data.saleprice;
        if(!Wtf.dispalyUnitPriceAmountInSales){//If there is no permission to view sales Price then we will show *****
            currentsalesprice = Wtf.UpriceAndAmountDisplayValue;
        }
        var initialpurchaseprice = this.record.data.initialprice;
        if(!Wtf.dispalyUnitPriceAmountInPurchase){//If there is no permission to view purchase Price then we will show *****
            initialpurchaseprice = Wtf.UpriceAndAmountDisplayValue;
        }
        this.productStore1=new Wtf.data.SimpleStore({
            fields: ['purchaseprice','initialsalesprice','currentsalesprice','initialpurchaseprice','warranty','warrantysal','location','warehouseName','syncable','leadtime','salesaccountname','salesretaccountname',
            'isBatchForProduct','isSerialForProduct','isSKUForProduct','orderuom','abatementRate','indComServiceProd','exciserate','isExcise','vatenabled','vatonmrp','mrprate'],
            data : [
            [purchasePrice,salesPrice,currentsalesprice,initialpurchaseprice,this.record.data.warranty,this.record.data.warrantyperiodsal,this.record.data.locationName,this.record.data.warehouseName,this.record.data.syncable,this.record.data.leadtime,
            this.record.data.salesaccountname,this.record.data.salesretaccountname,this.record.data.isBatchForProduct,this.record.data.isSerialForProduct,this.record.data.isSKUForProduct,this.record.data.orderinguomname,this.record.data.abatementRate,indComServiceProd,this.record.data.exciserate,isExice,Wtf.account.companyAccountPref.enablevatcst,this.record.data.vatonmrp,this.record.data.mrprate]
            ]          
        });
        
        var tpl2 = new Wtf.XTemplate(
            '<table border="0" width="100%" style="padding-left:5%;padding-top:3%;">',
            '<tpl for=".">',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.productList.gridInitialPurchasePrice")+' : </td><td class="leadDetailTD">{initialpurchaseprice}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.productList.gridInitialSalesPrice")+' : </td><td class="leadDetailTD">{initialsalesprice}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.productList.gridCurrentPurchasePrice")+' : </td><td class="leadDetailTD"> {purchaseprice}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.productList.gridCurrentSalesPrice")+' : </td><td class="leadDetailTD"> {currentsalesprice}</td></tr>',          
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.field.WarrantyPeriod(indays)")+' : </td><td class="leadDetailTD">{warranty}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.field.WarrantyPeriodS(indays)")+' : </td><td class="leadDetailTD">{warrantysal}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractActivityPanel.Location")+' : </td><td class="leadDetailTD">{location}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.inventorysetup.warehouse")+' : </td><td class="leadDetailTD">{warehouseName}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.product.makeAvailCRM")+' : </td><td class="leadDetailTD">{syncable}</td></tr>',  
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.productList.gridLeadTime")+' : </td><td class="leadDetailTD">{leadtime}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.product.salesAccount")+' : </td><td class="leadDetailTD">{salesaccountname}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.product.salesReturnAccount")+' : </td><td class="leadDetailTD">{salesretaccountname}</td></tr>',
//            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.productList.SalesUOM")+' : </td><td class="leadDetailTD">{leadtime}</td></tr>',
//            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.productList.OrderingUOM")+' : </td><td class="leadDetailTD">{leadtime}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.product.isBatch")+' : </td><td class="leadDetailTD">{isBatchForProduct}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.product.isSerial")+' : </td><td class="leadDetailTD">{isSerialForProduct}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.product.orderingUoM")+' : </td><td class="leadDetailTD">{orderuom}</td></tr>',
            '<tpl if="indComServiceProd">',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.field.Abatementrateofservicetax")+' : </td><td class="leadDetailTD"> {abatementRate}</td></tr>',  
            '</tpl>',
            '<tpl if="isExcise">',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("Excise Rate")+' : </td><td class="leadDetailTD"> {exciserate}</td></tr>',  
            '</tpl>',
            '<tpl if="vatenabled">',
            '<tpl if="vatonmrp">',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.product.mrprate")+' : </td><td class="leadDetailTD"> {exciserate}</td></tr>',  
            '</tpl>',
            '</tpl>',
            '</tpl></table>'
            );
        
        //++++++++++++++++++++++++ Code To Show The Product Image ++++++++++++++++++++++++++++++++++++++

        this.imageStore=new Wtf.data.SimpleStore({
            fields: ['url'],
            data : [
            [this.fileName]
            ]          
        });
        
        var imgtpl= new Wtf.XTemplate(                    
            '<tpl for=".">',
            '<div class="templateThumbImg"><img src="{url}" onerror="this.style.display=\'none\';" alt='+WtfGlobal.getLocaleText("acc.product.no.image.uploaded")+'></div>',            
            '</tpl>'
            );
        
//============================================= For Price List - Band Details =======================================================

        this.setPriceQuickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.setPriceListForBandWin.QuickSearchEmptyText"), // "Search by Price List - Band Name ...",
            width: 200,
            field: 'bandName'
        });
        
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), // 'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), // 'Allows you to add a new search term by clearing existing search terms.',
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.resetBttn.on('click', this.handleResetClick, this);

        this.priceListBandRec= Wtf.data.Record.create([
            {name: 'bandUUID'},
            {name: 'bandName'},
            {name: 'currencyName'},
            {name: 'currencysymbol'},
            {name: 'purchasePrice', defaultValue: 0},
            {name: 'salesPrice', defaultValue: 0},
            {name: 'applicableDate', type: 'date'}
        ]);
        
        this.priceListBandStore = new Wtf.data.Store({
            url: "ACCMaster/getPricingBandMasterForProductDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: "totalCount",
                root: "data"
            },this.priceListBandRec)
        });
        
        this.priceListBandStore.on('beforeload', function() {
            var currentBaseParams = this.priceListBandStore.baseParams;
            currentBaseParams.productID = this.record.data.productid;
            currentBaseParams.applicableDate = WtfGlobal.convertToGenericDate(new Date());
            currentBaseParams.isVolumeDiscount = false;
            this.priceListBandStore.baseParams = currentBaseParams;
        },this);
        
        this.priceListBandStore.on('load', function(store) {
            if (this.priceListBandStore.getCount() < 1) {
                this.priceListBandGrid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.priceListBandGrid.getView().refresh();
            }
            this.setPriceQuickPanelSearch.StorageChanged(store);
        }, this);
        
        this.priceListBandStore.on('datachanged', function() {
            var p = this.pPSetPrice.combo.value;
            this.setPriceQuickPanelSearch.setPage(p);
        }, this);
        
        this.priceListBandStore.load({
            params: {
                start: 0,
                limit: 30
            }
        });
        
        this.columnArr = [];
        
        this.columnArr.push({
            header: WtfGlobal.getLocaleText("acc.field.bandName"), // "Band Name",
            dataIndex: 'bandName',
            align: 'left',
            width: 220,
            renderer: WtfGlobal.deletedRenderer
        });
        
        this.columnArr.push({
            header: WtfGlobal.getLocaleText("acc.customerList.gridCurrency"), // "Currency",
            dataIndex: 'currencyName',
            align: 'left',
            width: 100,
            renderer: WtfGlobal.deletedRenderer
        });
        
        this.columnArr.push({
            header: WtfGlobal.getLocaleText("acc.productList.gridPurchasePrice"), // "Purchase Price",
            dataIndex: 'purchasePrice',
            align: 'right',
            width: 233,
            renderer: function(v, metadata, record) {
                if(!Wtf.dispalyUnitPriceAmountInPurchase){//If there is no permission to view purchase Price then we will show *****
                    return Wtf.UpriceAndAmountDisplayValue;
                } else if (record.data['type'] == "Inventory Non-Sale") {
                    return "N/A";
                } else {
                    return WtfGlobal.withCurrencyUnitPriceRenderer(v, false, record);
                }
            }
        });
        
        this.columnArr.push({
            header: WtfGlobal.getLocaleText("acc.productList.gridSalesPrice"),  // "Sales Price",
            dataIndex: 'salesPrice',
            align: 'right',
            width: 233,
            renderer: function(v, metadata, record) {
                if(!Wtf.dispalyUnitPriceAmountInSales){//If there is no permission to view sales Price then we will show *****
                    return Wtf.UpriceAndAmountDisplayValue;
                } else if (record.data['type'] == "Inventory Non-Sale") {
                    return "N/A";
                } else {
                    return WtfGlobal.withCurrencyUnitPriceRenderer(v, false, record);
                }
            }
        });
        
        this.columnArr.push({
            header: WtfGlobal.getLocaleText("acc.field.applicableDate"),  // "Applicable Date",
            dataIndex: 'applicableDate',
            align: 'center',
            width: 233,
            renderer: WtfGlobal.onlyDateDeletedRenderer
        });
        
        this.setPriceCM = new Wtf.grid.ColumnModel(this.columnArr);

        this.priceListBandGrid = new Wtf.grid.GridPanel({
            layout: 'fit',
            store: this.priceListBandStore,
            cm: this.setPriceCM,
            autoScroll: true,
            height: 235,
            border: false,
            loadMask: true,
            cls: 'vline-on',
            viewConfig: {
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            },
            tbar: [this.setPriceQuickPanelSearch, this.resetBttn],
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                store: this.priceListBandStore,
                searchField: this.setPriceQuickPanelSearch,
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                plugins: this.pPSetPrice = new Wtf.common.pPageSize({})
            })
        });
        
//============================================= For Price List - Volume Discount Details =======================================================

        this.setPriceDiscountQuickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.setPriceListForVolumeDiscountWin.QuickSearchEmptyText"), // "Search by Price List - Volume Discount ...",
            width: 200,
            field: 'bandName'
        });
        
        this.resetVolumeSearchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), // 'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), // 'Allows you to add a new search term by clearing existing search terms.',
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.resetVolumeSearchBttn.on('click', this.handleVolumeSearchResetClick, this);

        this.priceListVolumeRec= Wtf.data.Record.create([
            {name: 'bandUUID'},
            {name: 'bandName'},
            {name: 'currencyName'},
            {name: 'currencysymbol'},
            {name: 'desc'},
            {name: 'pricePolicy'},
            {name: 'minimumQty'},
            {name: 'maximumQty'},
            {name: 'purchasePrice'},
            {name: 'salesPrice'},
            {name: 'applicableDate', type: 'date'},
            {name: 'discountType'},
            {name: 'disocuntValue'}
        ]);
        
        this.priceListVolumeStore = new Wtf.data.Store({
            url: "ACCMaster/getPricingBandMasterForProductDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: "totalCount",
                root: "data"
            },this.priceListVolumeRec)
        });
        
        this.priceListVolumeStore.on('beforeload', function() {
            var currentBaseParams = this.priceListVolumeStore.baseParams;
            currentBaseParams.productID = this.record.data.productid;
            currentBaseParams.applicableDate = WtfGlobal.convertToGenericDate(new Date());
            currentBaseParams.isVolumeDiscount = true;
            this.priceListVolumeStore.baseParams = currentBaseParams;
        }, this);
        
        this.priceListVolumeStore.on('load', function(store) {
            if (this.priceListVolumeStore.getCount() < 1) {
                this.priceListVolumeGrid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.priceListVolumeGrid.getView().refresh();
            }
            this.setPriceDiscountQuickPanelSearch.StorageChanged(store);
        }, this);
        
        this.priceListVolumeStore.on('datachanged', function() {
            var p = this.pPSetVolumePrice.combo.value;
            this.setPriceDiscountQuickPanelSearch.setPage(p);
        }, this);
        
        this.priceListVolumeStore.load({
            params: {
                start: 0,
                limit: 30
            }
        });
        
        this.volumeColumnArr = [];
        
        this.volumeColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount"), // "Price List - Volume Discount",
            dataIndex: 'bandName',
            align: 'left',
            width: 150,
            renderer: WtfGlobal.deletedRenderer
        });
        
        this.volumeColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.field.description"), // "Description",
            dataIndex: 'desc',
            align: 'left',
            width: 150,
            renderer: WtfGlobal.deletedRenderer
        });
        
        this.volumeColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.field.pricePolicy"), // "Price Policy",
            dataIndex: 'pricePolicy',
            align: 'left',
            width: 150,
            renderer: function(val,m,rec) {
                if (val == "1") {
                    return WtfGlobal.getLocaleText("acc.field.useDiscount"); // "Use Discount"
                } else {
                    return WtfGlobal.getLocaleText("acc.field.useFlatPrice"); // "Use Flat Price"
                }
            }
        });
        
        this.volumeColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.field.minimumQty"), // "Minimum Qty",
            dataIndex: 'minimumQty',
            align: 'right',
            width: 100
        });
        
        this.volumeColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.field.maximumQty"), // "Maximum Qty",
            dataIndex: 'maximumQty',
            align: 'right',
            width: 100
        });
        
        this.volumeColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.customerList.gridCurrency"), // "Currency",
            dataIndex: 'currencyName',
            align: 'left',
            width: 100,
            renderer: WtfGlobal.deletedRenderer
        });
        
        this.volumeColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.productList.gridPurchasePrice"), // "Purchase Price",
            dataIndex: 'purchasePrice',
            align: 'right',
            width: 100,
            renderer: function(v,m,rec){
                if(!Wtf.dispalyUnitPriceAmountInPurchase){//If there is no permission to view purchase Price then we will show *****
                    return Wtf.UpriceAndAmountDisplayValue;
                }  else{
                    return WtfGlobal.currencyRendererDeletedSymbol(v,m,rec);
                }
            }
        });
        
        this.volumeColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.productList.gridSalesPrice"),  // "Sales Price",
            dataIndex: 'salesPrice',
            align: 'right',
            width: 100,
            renderer: function(v,m,rec){
                if(!Wtf.dispalyUnitPriceAmountInSales){//If there is no permission to view sales Price then we will show *****
                    return Wtf.UpriceAndAmountDisplayValue;
                }  else{
                    return WtfGlobal.currencyRendererDeletedSymbol(v,m,rec);
                }
            }
        });
        
        this.volumeColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.field.discountType"), // "Discount Type",
            dataIndex: 'discountType',
            align: 'left',
            width: 100
        });
        
        this.volumeColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.field.disocuntValue"), // "Discount Value",
            dataIndex: 'disocuntValue',
            align: 'left',
            width: 100,
            renderer: WtfGlobal.currencyRendererDeletedSymbol
        });
        
        this.volumeColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.field.applicableDate"),  // "Applicable Date",
            dataIndex: 'applicableDate',
            align: 'center',
            width: 150,
            renderer: WtfGlobal.onlyDateDeletedRenderer
        });
        
        this.setPriceVolumeCM = new Wtf.grid.ColumnModel(this.volumeColumnArr);

        this.priceListVolumeGrid = new Wtf.grid.GridPanel({
            layout: 'fit',
            store: this.priceListVolumeStore,
            cm: this.setPriceVolumeCM,
            autoScroll: true,
            height: 235,
            border: false,
            loadMask: true,
            cls: 'vline-on',
            viewConfig: {
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            },
            tbar: [this.setPriceDiscountQuickPanelSearch, this.resetVolumeSearchBttn],
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                store: this.priceListVolumeStore,
                searchField: this.setPriceDiscountQuickPanelSearch,
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                plugins: this.pPSetVolumePrice = new Wtf.common.pPageSize({})
            })
        });
        
        //=============================================== For Defining All Data View ============================================================

        this.productDataview = new Wtf.DataView({
            store:this.productStore,
            tpl: tpl,
            autoHeight:true,
            multiSelect: true,
             autowidth:true,
            columnWidth:.50,
            overClass:'x-view-over',
            itemSelector:'div.thumb-wrap',
            emptyText: 'No Data to display'
        })
            
        this.productDataview2 = new Wtf.DataView({
            store:this.productStore1,
            tpl: tpl2,
            autoHeight:true,
             autowidth:true,
            multiSelect: true,
            columnWidth:.50,
            overClass:'x-view-over',
            itemSelector:'div.thumb-wrap',
            emptyText: 'No Data to display'
        })
            
        this.productImageView = new Wtf.DataView({
            store:this.imageStore,
            tpl:imgtpl,
            autoHeight:true,
            autowidth:true,
            multiSelect: true,
            columnWidth:.50,
            overClass:'x-view-over',
            itemSelector:'div.thumb-wrap',
            emptyText: 'No images to display'
        }) 

        //=============================================== For Defining All Field Sets ============================================================     
        
        this.fs1=new Wtf.form.FieldSet({
            width:1100,
            autoHeight:true,
            //            height:250,
            title:WtfGlobal.getLocaleText("acc.invoiceList.expand.pDetails"), // 'Product Details',
            layout:'column',
            items:[
            this.productDataview,
            this.productDataview2
            ]
        });    
        
        this.fs2=new Wtf.form.FieldSet({
//            width:1100,
            autoWidth:true,
            autoHeight:true,
            title:WtfGlobal.getLocaleText("erp.upload.winimgfield.label"), // 'Product Image',
            layout:'fit',
            items:[       
            this.productImageView
            ]
        });     
        
        this.fs3 = new Wtf.form.FieldSet({
            width: 1100,
            height: 270,
            title: WtfGlobal.getLocaleText("acc.field.pricingBands") + " " + WtfGlobal.getLocaleText("acc.auditTrail.gridDetails"), // 'Price List - Band Details',
            items: [this.priceListBandGrid]
        });
        
        this.fs4 = new Wtf.form.FieldSet({
            width: 1100,
            height: 270,
            title: WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " " + WtfGlobal.getLocaleText("acc.auditTrail.gridDetails"), // 'Price List - Volume Discount Details',
            items: [this.priceListVolumeGrid]
        });
        this.assemblyGridPanel = new Wtf.account.MRPAssemblyProductDetails({
            record: this.record,
            id: this.record.data.productid + '_mrpassemblyproductdetails',
            readOnly:true,
            hidden: !(Wtf.account.companyAccountPref.activateMRPManagementFlag && this.record.data.type == "Inventory Assembly")
        });
        if(Wtf.account.companyAccountPref.activateMRPManagementFlag){
            this.on("activate", function() {
                this.doLayout();
            }, this);
        }
        this.fs5 = new Wtf.form.FieldSet({
            width: 1100,
            height: 800,
            title: WtfGlobal.getLocaleText("acc.product.BOMDetails"),
            items: [this.assemblyGridPanel]
        });
        
        //=============================================== For Adding All Data View ============================================================       
        this.add({
            layout:"table",
            layoutConfig: {
                columns: 1
            },
            autoWidth:true,
            autoScroll:true,
            bodyStyle:'overflow-y: scroll',
            items:[
            {
                colspan: 1,
                autoWidth:true,
                autoScroll:true,
                border:false,
                items:this.fs1,
                bodyStyle:"margin-left:20px;margin-top:30px;margin-bottom:20px;"
            },{
                colspan: 1,
                autoWidth: true,
                autoScroll: true,
                border: false,
                items: this.fs3,
                bodyStyle: "margin-left:20px; margin-bottom:20px; overflow-y: scroll"
            },{
                colspan: 1,
                autoWidth: true,
                autoScroll: true,
                border: false,
                items: this.fs4,
                bodyStyle: "margin-left:20px; margin-bottom:20px; overflow-y: scroll"
            },{
                colspan: 1,
                autoWidth:true,
                autoScroll:true,
                border:false,
                items:this.fs2,
                bodyStyle:"margin-left:20px;margin-bottom:20px;overflow-y: scroll"
            },{
                colspan: 1,
                autoWidth:true,
                autoScroll:true,
                border:false,
                hidden:!(Wtf.account.companyAccountPref.activateMRPManagementFlag && this.record.data.type == "Inventory Assembly"),
                items:this.fs5,
                bodyStyle:"margin-left:20px;margin-bottom:20px;overflow-y: scroll"
                
            }]
        });
    },
    
    handleResetClick: function() {
        if (this.setPriceQuickPanelSearch.getValue()) {
            this.setPriceQuickPanelSearch.reset();
            this.priceListBandStore.load({
                params: {
                    start: 0,
                    limit: 30
                }
            });
        }
    },
    
    handleVolumeSearchResetClick: function() {
        if (this.setPriceDiscountQuickPanelSearch.getValue()) {
            this.setPriceDiscountQuickPanelSearch.reset();
            this.priceListVolumeStore.load({
                params: {
                    start: 0,
                    limit: 30
                }
            });
        }
    }
});

/*
 Function to open create and edit product form. Called from configured product view.
  */
function createEditDeleteProduct(isEdit,isClone,rec,isDelete){
    var parentObj = Wtf.getCmp("ProductReport");
    var tabid = isEdit ? rec.productid : "productwin";
    if(!parentObj){
        parentObj =  new Wtf.account.ProductDetailsPanel({
            moduleId:Wtf.Acc_Product_Master_ModuleId,
            id : "dummyProductReport"
        });
    }
    if(isDelete){
        parentObj.on("productdelete",function(){
            Wtf.getCmp("as").remove(this);
            this.destroy();
        },this);
        parentObj.confirmBeforeDeleteProduct("true",true,rec.productid);
        return;
    }
    if(isEdit){
         var productRec =new parentObj.productRec(rec);
         productRec.json = rec;
    }
    parentObj.showForm(isEdit,isClone,productRec);
    if(isEdit){
        parentObj.on('productformshown',function(isEdit){
            if(Wtf.getCmp(tabid)){
            Wtf.getCmp(tabid).on('productsave',function(){
                this.loadProduct(tabid);
            },this);
            }
        },this);
    }
}
