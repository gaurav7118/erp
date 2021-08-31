/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.DefaultWarehouseWin = function (config){
    this.isdefaultwarehouse=config.isdefaultwarehouse;
    Wtf.apply(this,config);
    Wtf.DefaultWarehouseWin.superclass.constructor.call(this,{
        buttons:[
        {
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
            handler:function (){
                if(this.warehouseCombo.isValid()){
                this.saveData();
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("Please select default warehouse")], 0);
            }
            },
            scope:this
        },
        {
            text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),
            handler:function (){
                this.close();
            },
            scope:this
        }
        ]
    });
}

Wtf.extend(Wtf.DefaultWarehouseWin,Wtf.Window,{
    initComponent:function (){
        Wtf.DefaultWarehouseWin.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetAddEditForm();
        
        this.mainPanel = new Wtf.Panel({
            layout:"border",
            items:[
            this.northPanel,
            this.AddLocationFormatForm
            ]
        });

        this.add(this.mainPanel);
    },
    GetNorthPanel:function (){
        var wintitle = WtfGlobal.getLocaleText("acc.warehouse.set.default.warehouse");
        var windetail='';
        var image='';
        windetail=WtfGlobal.getLocaleText("acc.warehouse.select.warehouse");
        image='images/createuser.png';
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:85,
            border:false,
            bodyStyle:"background-color:white;padding:8px;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(wintitle,windetail,image)
        });
    },
    GetAddEditForm:function (){
        this.warehouseRecord = new Wtf.data.Record.create([{
            name: 'warehouse'
        },{
            name: 'name'
        }, {
            name: 'isdefault'
        }]);
   
        this.warehouseReader = new Wtf.data.KwlJsonReader({
            root: 'data'
        }, this.warehouseRecord);

        this.warehouseStore = new Wtf.data.Store({
            sortInfo: {
                field: 'name',
                direction: "ASC"
            },
            url:"ACCCustomerCMN/getAllCustomerWarehouse.do",
            baseParams:{
                customerid:this.record[0].data["accid"],
                isForCustomer:true
            },
            reader: this.warehouseReader
        });
        this.warehouseStore.load();
        this.warehouseStore.on("load", function(ds, rec, o){
            if(rec.length > 0){
                for(var i=0;i<rec.length;i++){
                    if(rec[i].data.isdefault=="true"||rec[i].data.isdefault==true){
                        this.warehouseCombo.setValue(rec[i].data.warehouse, true);
                    }
                }
            }
            
        }, this);
        this.warehouseCombo = new Wtf.form.ComboBox({
            mode: 'local',
            triggerAction: 'all',
            hiddenName:"moduleId",
            fieldLabel: WtfGlobal.getLocaleText("acc.inventorysetup.warehouse") + '*',
            typeAhead: true,
            width:200,
            allowBlank:false,
            store: this.warehouseStore,
            displayField: 'name',
            valueField:'warehouse',
            msgTarget: 'side',
            forceSelection: true,
            emptyText:WtfGlobal.getLocaleText("acc.warehouse.select.warehouse")
        });
        
        this.AddLocationFormatForm = new Wtf.form.FormPanel({
            region:"center",
            border:false,
            iconCls:'win',
            bodyStyle:"background-color:#f1f1f1;padding:35px",
            //  url:"INVSeq/addSeqFormat.do",
            labelWidth:110,
            items:[
            this.warehouseCombo,
            
            ]
        });
        
    },
    saveData:function(){
        var warehouseid=this.warehouseCombo.getValue();
        Wtf.Ajax.requestEx({
            url:"ACCCustomerCMN/setDefaultWarehouseForCustomers.do",
            params: {
                customerid:this.record[0].data["accid"],
                warehouseid:warehouseid
            }
        },
        this,
        function(result, req){
            var msg=result.msg;
            var title=WtfGlobal.getLocaleText("acc.common.success");
            if(result.success){
                WtfComMsgBox([title,msg],0);
                Wtf.getCmp("invstoremastergrid").getStore().reload();
                this.close();
            }
            else if(result.success==false){
                title=WtfGlobal.getLocaleText("acc.common.error");
                WtfComMsgBox([title, WtfGlobal.getLocaleText("acc.common.error.occurred")], 0);
                return false;
            }
        },
        function(result, req){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.failure"), WtfGlobal.getLocaleText("acc.common.error.occurred")], 3);
            return false;
        });
        this.close();
    }
});

//