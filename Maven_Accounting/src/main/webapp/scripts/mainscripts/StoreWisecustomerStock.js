/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


//-----------------------------------------------Store Wise inventory level-----------------------------------------------------


Wtf.StoreWisecustomerStock = function(config){
    Wtf.StoreWisecustomerStock.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.StoreWisecustomerStock, Wtf.Panel, {
    initComponent: function() {
        Wtf.StoreWisecustomerStock.superclass.initComponent.call(this);
    },
    onRender: function(config) {
        Wtf.StoreWisecustomerStock.superclass.onRender.call(this, config);

      Wtf.Ajax.requestEx({
          url: "ACCAccountCMN/getProductCustomFieldsToShow.do"

      }, this, function (request, response) {
            
        var customProductField = request.data;
        this.loadMask = new Wtf.LoadMask(document.body, {msg: WtfGlobal.getLocaleText("acc.msgbox.50")});
        this.loadMask.show();
            
        this.status = 0;
        this.searchJson="";

        this.record = Wtf.data.Record.create([
        {
            "name":"storecode"
        },

        {
            "name":"storedescription"
        },
        {
            "name":"pid"
        },
        {
            "name":"desc"
        },
        {
            "name":"itemcode"
        },
        {
            "name":"productname"
        },
        {
            "name":"quantity"
        },
        {
            "name":"customerid"
        },
        {
            "name":"customername"
        },
        {
            "name":"customerwarehouseid"
        },
        {
            "name":"customerwarehousename"
        },
        {
            "name":"uom"
        },
        ]);
         this.customerComboRec = Wtf.data.Record.create ([
                {name:'accid'},
                {name:'id'},
                {name:'title'},
                {name:'accname'},
                {name:'accname'},
                {name:'personname',mapping:'accname'},
                {name:'personemail',mapping:'email'},
                {name:'personid',mapping:'accid'},
                {name:'company'},
                {name:'email'},
                {name:'contactno'},
                {name:'contactno2'},
                {name:'customerid'},
            ]);
            this.customerComboStore=new Wtf.data.Store({
                url: "ACCCustomer/getCustomersForCombo.do",
                baseParams:{      

                },
                reader: new Wtf.data.KwlJsonReader({
                    totalProperty: 'totalCount',
                    root: "data"
                },this.customerComboRec)
            });
          this.CustomerCmb = new Wtf.form.ComboBox({
           fieldLabel:WtfGlobal.getLocaleText("acc.customerList.gridCustomer"),
                name:"customer",
                hiddenName:'customer',
                store:this.customerComboStore,
                width : 200,
                style: "margin-left:30px;",
               // allowBlank:false,
                valueField:'accid',
                displayField:'accname',
//                value: (isEdit)?rec.data['customerid']:"",
                mode: 'local',
                triggerAction:'all',
                emptyText:'Select Customer...'
                
        });
        
         this.customerComboStore.load();
            
        this.storeCmbRecord = new Wtf.data.Record.create([
             {name: 'warehouse'},                           //warehouse id
            {name: 'name'},
            {name: 'customer'},
            {name: 'company'},
            {name: 'doids'},
        ]);

        this.storeCmbStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.storeCmbRecord),
//            url:"ACCCustomerCMN/getCustomerWarehouses.do",
            url:"ACCCustomerCMN/getAllCustomerWarehouse.do",
            baseParams:{
                isForCustomer:true
            }
        });
        
        this.storeCmb = new Wtf.form.ComboBox({
            fieldLabel : WtfGlobal.getLocaleText("acc.stock.Store*"),
            hiddenName : 'storeid',
            store : this.storeCmbStore,
            typeAhead:true,
            displayField:'name',
            valueField:'warehouse',
            mode: 'local',
            disabled:true,
            width : 125,
            triggerAction: 'all',
            emptyText:WtfGlobal.getLocaleText("acc.je.Selectstore")
        });

        this.addRecToStore();
        
        this.CustomerCmb.on("select",function(){
            this.storeCmbStore.removeAll();
            if(this.CustomerCmb.getValue() === ""){
                this.addRecToStore();
                this.initloadgridstore(this.storeCmb.getValue(),this.CustomerCmb.getValue());
                this.storeCmb.disable();
            }else{
                this.storeCmb.enable();
                this.storeCmbStore.load({
                    params:{
                        customerid:this.CustomerCmb.getValue()
                        }
                    });
        }
        },this);
        
         this.resetBtn = new Wtf.Button({
            anchor : '90%',
            text: WtfGlobal.getLocaleText("acc.inventory.QAAproval.ResetFilter"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.inventory.QAAproval.Clickheretoresetfilter")
            },
            iconCls:getButtonIconCls(Wtf.etype.resetbutton),
            scope:this,
            handler:function(){
                Wtf.getCmp("Quick"+this.grid.id).setValue("");
                this.storeCmb.store.removeAll();
                this.addRecToStore();
                this.CustomerCmb.store.clearFilter();
                this.CustomerCmb.setValue(this.CustomerCmb.store.data.items[0].data.accid);
                this.initloadgridstore(this.storeCmb.getValue(),this.CustomerCmb.getValue());
              }
        });

        this.search = new Wtf.Button({
            anchor : '90%',
            text: WtfGlobal.getLocaleText("acc.field.Search"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.advancesearch.searchBTN.ttip")
            },
            iconCls : 'accountingbase fetch',
            scope:this,
            handler:function(){
                 this.initloadgridstore(this.storeCmb.getValue(),this.CustomerCmb.getValue());
             }
        });
        this.ds = new Wtf.data.GroupingStore({
            url: 'ACCInvoiceCMN/getAllUninvoicedConsignmentDetails.do',
            sortInfo : {
               field : 'customername',
               direction : 'ASC'
            },
            groupField : 'customername',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty:'count'
            },
            this.record)
       });
       
       this.ds.on('load',this.enableButton,this)
       this.ds.on('loadexception',function(){
            WtfGlobal.resetAjaxTimeOut();
        },this)
        
      
        this.storeCmbStore.on("load", function(ds, rec, o){
            this.addRecToStore();
            this.initloadgridstore(this.storeCmb.getValue(),this.CustomerCmb.getValue());
         }, this);

       this.customerComboStore.on("load", function(ds, rec, o){
            var newRec=new this.customerComboRec({
                accid:'',
                accname:'ALL'
            })
            this.customerComboStore.insert(0,newRec);
            this.CustomerCmb.setValue('');
            this.initloadgridstore(this.storeCmb.getValue(),this.CustomerCmb.getValue());
            this.loadMask.hide();
        }, this);
        
        this.updateStoreConfig(customProductField,this.ds);
        this.customerComboStore.on("loadexception", function(){
            this.loadMask.hide();
        }, this);
        
        var sm= new Wtf.grid.CheckboxSelectionModel({
            // singleSelect:true
            });
            
        var cmDefaultWidth = 150;
        var colArr = [
//             this.cm = new Wtf.grid.ColumnModel([
            new Wtf.KWLRowNumberer(),
            {
                header: WtfGlobal.getLocaleText("acc.product.threshold.grid.storecode"),
                dataIndex: 'storecode',
                hidden:true,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.cosignmentloan.StoreDescription"),
                dataIndex: 'storedescription',
                hidden:true,
                pdfwidth:100
            },
            {
                header: "Customer",
                dataIndex: 'customername',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.contractMasterGrid.header8"),
                dataIndex: 'pid',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.contractMasterGrid.header7"),
                dataIndex: 'productname',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"),
                dataIndex: 'desc',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: "Warehouse",
                dataIndex: 'customerwarehousename',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.saleByItem.gridQty"),
                dataIndex: 'quantity',
                sortable:false,
                align:"right",
                width:cmDefaultWidth,
                pdfwidth:100,
                renderer:function(v){
                    return v // WtfGlobal.getCurrencyFormatWithoutSymbol(v, Wtf.companyPref.quantityDecimalPrecision)
                }
            },{
                header: WtfGlobal.getLocaleText("acc.invoice.gridUOM"),
                dataIndex: 'uom',
                width:cmDefaultWidth,
                pdfwidth:100
            //sortable:true,
            //dbname:'sb_itemconfigdata.name'
            }
//            ]);
           ]
            if(customProductField && customProductField.length>0) {
                for(var ccnt=0; ccnt<customProductField.length; ccnt++) {
                    colArr.push({
                        header : customProductField[ccnt].columnname,
                        dataIndex: customProductField[ccnt].dataindex,
                        width: 50,
                        hidden:(customProductField[ccnt].dataindex=="Custom_Material Group")?false:true,
                        pdfwidth: 50,
                        align: 'center'
                    })
                }
            }
        this.cm = new Wtf.grid.ColumnModel(colArr);

        this.objsearchComponent=new Wtf.advancedSearchComponent({
            cm:this.cm
        });

        var tbararr = new Array();
        tbararr.push("-");
        tbararr.push(WtfGlobal.getLocaleText("acc.invoice.customer")+":");
        tbararr.push(this.CustomerCmb);
        tbararr.push("-");
        tbararr.push(WtfGlobal.getLocaleText("acc.inventorysetup.warehouse")+":");
        tbararr.push(this.storeCmb);
        tbararr.push("-");
        tbararr.push( this.search);//,"-",this.AdvanceSearchBtn);
        tbararr.push("-");
        tbararr.push( this.resetBtn);
        
        this.exportButton=new Wtf.exportButton({
                obj:this,
                id:'storewisecustomerstockexport',
                tooltip:WtfGlobal.getLocaleText("acc.cosignmentloan.ExportReport"),  //"Export Report details.",  
                params:{
                    name: "Store Wise Customer Stock"
                },
                menuItem:{
                    csv:true,
                    pdf:true,
                    xls:true
                },
                get:Wtf.autoNum.StockAvailabilityByCustomerWarehouseReport,
                label:"Export"
        });
        this.printButton=new Wtf.exportButton({
                text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
                obj:this,
                tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details',
                filename : WtfGlobal.getLocaleText("acc.field.StockAvailabilityByCustomerWarehouseReport"),
                label:WtfGlobal.getLocaleText("acc.field.StockAvailabilityByCustomerWarehouseReport"),
                params:{
                    custWarehouse:this.storeCmb.getValue(),
                    customerid:this.CustomerCmb.getValue(),
                },
                menuItem:{ print:true},
                get:Wtf.autoNum.StockAvailabilityByCustomerWarehouseReport,
            });
        this.grid=new  Wtf.KwlEditorGridPanel({
            region:'center',
            cm:this.cm,
            store:this.ds,
            sm:sm,
            loadMask:true,
            autoscroll:true,
            viewConfig: {
                forceFit: false
            },
            view: new Wtf.grid.GroupingView({
                startCollapsed :false,
                showGroupName: false,
                enableGroupingMenu: false,
                emptyText:WtfGlobal.emptyGridRenderer( WtfGlobal.getLocaleText('acc.common.norec') + "<br>" +WtfGlobal.getLocaleText('acc.common.norec.click.fetchbtn')) ,
            }),
            plugins : this.expander,
            searchLabel:WtfGlobal.getLocaleText("acc.field.QuickSearch"),
            displayInfo:true,
            searchEmptyText:WtfGlobal.getLocaleText("acc.cosignmentloan.EnterProductIDProductname"),
            serverSideSearch:true,
            tbar:tbararr,
            bbar:[this.exportButton,this.printButton]
        });

        this.innerPanel = new Wtf.Panel({
            layout : 'border',
            bodyStyle :"background-color:transparent;",
            border:false,
            items : [this.grid,this.objsearchComponent]

        });
        Wtf.getCmp("paggintoolbar"+this.grid.id).on('beforerender',function(){
            Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize=30
        },this);
        this.add(this.innerPanel);
        this.grid.doLayout();
        this.doLayout();
        this.objsearchComponent.advGrid.on("filterStore",this.filterStore, this);
        this.objsearchComponent.advGrid.on("clearStoreFilter",this.clearStoreFilter, this);
      }, function () {
  
       });
    },
    addRecToStore:function(){
        var newRec=new this.storeCmbRecord({
            warehouse:'',
            name:'ALL'
        })
        this.storeCmbStore.insert(0,newRec);
        this.storeCmb.setValue('');
    },
    initloadgridstore:function(storetype,Customer){

        this.ds.baseParams = {
           custWarehouse:storetype,
           customerid:Customer,
           searchJson:this.searchJson
        }
        WtfGlobal.setAjaxTimeOut();
        this.ds.load(
        {
            params: {
                start:0,
                limit:30,
                ss:  Wtf.getCmp("Quick"+this.grid.id).getValue()

            }
        });
    },
   configurAdvancedSearch:function(){
        this.objsearchComponent.show();
        this.doLayout();
    },
    filterStore:function(json){
        this.searchJson=json;
        this.initloadgridstore(this.storeCmb.getValue(),this.CustomerCmb.getValue());
       
    },
    clearStoreFilter:function(){
        this.objsearchComponent.hide();
        this.doLayout();
        this.searchJson="";
       this.initloadgridstore(this.storeCmb.getValue(),this.CustomerCmb.getValue());
       
    },
    enableButton: function () {

        if (this.ds.getCount() == 0) {
            if (this.exportButton)
                this.exportButton.disable();
            if (this.printButton)
                this.printButton.disable();
            var emptyTxt = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().emptyText = emptyTxt;
            this.grid.getView().refresh();
        } else {
            if (this.exportButton)
                this.exportButton.enable();
            if (this.printButton)
                this.printButton.enable();
        }
        WtfGlobal.resetAjaxTimeOut();
    },
     updateStoreConfig: function (customProductField,store) {
        for (var cnt = 0; cnt < customProductField.length; cnt++) {
            var fieldname = customProductField[cnt].dataindex;
            var newField = new Wtf.data.Field({
                name: fieldname
            });
            this.ds.fields.items.push(newField);
            this.ds.fields.map[fieldname] = newField;
            this.ds.fields.keys.push(fieldname);
    }
        this.ds.reader = new Wtf.data.KwlJsonReader(this.ds.reader.meta, this.ds.fields.items);
    }
    
});
