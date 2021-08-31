///*
// * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
// * All rights reserved.
// * 
// * This program is free software; you can redistribute it and/or
// * modify it under the terms of the GNU General Public License
// * as published by the Free Software Foundation; either version 2
// * of the License, or (at your option) any later version.
// * 
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU General Public License
// * along with this program; if not, write to the Free Software
// * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
// */
//Wtf.account.cycleCountPanel = function(config){
//    Wtf.apply(this, config);
//    Wtf.account.cycleCountPanel.superclass.constructor.call(this, config);
//}
//
//Wtf.extend(Wtf.account.cycleCountPanel, Wtf.Panel, {
//    initComponent:function(){
//        this.draftId = "";
//        this.frequency = "";
//        //this.SaveAsDraftButt.hidden=true;
//        this.countedQua = new Wtf.form.NumberField({
//           allowBlank: false,
//           allowNegative: true,
//           allowDecimals: true,
//           selectOnFocus: true,
//           listeners: {
//               'focus': setZeroToBlank
//           }
//        });
//        this.cyclecountstatus_data = [['0','Pending'],['1','Recount'],['2','Approved']];
//        this.store_cyclecountstatus = new Wtf.data.SimpleStore({
//            fields:['statusid','status'],
//            data:this.cyclecountstatus_data
//        });
//
//        this.statuscmbdrop = new Wtf.form.ComboBox({
//            fieldLabel :WtfGlobal.getLocaleText("acc.cc.1")+"*",  //'Count Type*',
//            store : this.store_cyclecountstatus,
//            hiddenName:'countstatus',
//            displayField : 'status',
//            valueField : 'statusid',
//            mode: 'local',
//            editable : false,
//            triggerAction: 'all'
//        });
//        this.itemlistRecord = new Wtf.data.Record.create([{
//            name: 'id'
//        },{
//            name:'product'
//        },{
//            name:'product_id'
//        },{
//            name:'uom'
//        },{
//            name:'earlierQuantity'
//        },{
//            name: 'newQuantity'
//        },{
//            name : 'varianceQuantity'
//        },{
//            name: 'variance'
//        },{
//            name: 'reasone'
//        },{
//            name: 'productid'
//        },{
//            name:'statusid'
//        },{
//            name:"originalStatusId", mapping:'statusid'
//        },{
//            name:'tolerance'
//        },{
//            name:'originalToleranceMsg', mapping:"tolerancemsg"
//        },{
//            name:'tolerancemsg'
//        },{
//            name:'valflag', type:"boolean"
//        },{
//            name:'cyclecountId'
//        }]);
//
//        this.itemlistReader = new Wtf.data.KwlJsonReader({
//            root: 'data'
//        }, this.itemlistRecord);
//
//        this.itemlistSm = new Wtf.grid.RowSelectionModel({
//            width: 5,
//            singleSelect: true
//        });
//
//        
//        this.grpView = new Wtf.grid.GroupingView({
//            forceFit: true,
//            startCollapsed:true,
//            showGroupName: true,
//            enableGroupingMenu: true,
//            hideGroupedColumn: true
//        });
//
//        this.itemlistStore = new Wtf.data.GroupingStore({
//            sortInfo: {
//                field: 'product',
//                direction: "ASC"
//            },
////             url:Wtf.req.account+'cyclecount.jsp',
//            url : "ACCProduct/"+(this.approve==1?"getCycleCountForApproval":"getCycleCountProduct")+".do",
//            baseParams:{
//                mode:this.approve==1?120:117
//            },
////            groupField : 'product',
//            reader: this.itemlistReader
//        });
//
//        
//        this.itemlistCm = new Wtf.grid.ColumnModel([
//            new Wtf.grid.RowNumberer(),
//            {
//                header: WtfGlobal.getLocaleText("acc.product.gridProductID"),  //"Product ID", acc.productList.gridProductid
//                dataIndex: 'product_id'
//            },
//            {
//                header: WtfGlobal.getLocaleText("acc.product.gridProduct"),  //"Product",
//                dataIndex: 'product'
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.2"),  //"UoM",
//                dataIndex: 'uom'
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.3"),  //"Earlier Quantity",
//                dataIndex: 'earlierQuantity',
//                 renderer: function(a, b, c, d, e, f){
//                    return (a==0)?"-":a + "    " + c.get("uom");
//                }
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.4"),  //"Counted Quantity",
//                dataIndex: 'newQuantity',
//                renderer: function(a, b, c, d, e, f){
//                    b.css = 'accountingEditableCell';
//                    return (a==0)?"-":a;//return a + "    " + c.get("uom");
//                },
//                editor: this.approve==1?null:this.countedQua
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.5"),  //"Variance Quantity",
//                renderer: function(a, b, c, d, e, f){
//                    return (a==0)?"-":a + "    " + c.get("uom");
//                },
//                dataIndex: 'varianceQuantity'
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.6"),  //"Reason",
//                dataIndex: 'reasone',
//                editor:this.approve==1?null:new Wtf.form.TextField({})
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.8"),  //"Status",
//                dataIndex: 'statusid',
//                hidden:this.approve==1?false:true,
//                editor: this.statuscmbdrop,
//                renderer:Wtf.comboBoxRenderer(this.statuscmbdrop)
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.7"),  //"Tolerance",
//                dataIndex: 'tolerancemsg',
////                mode:this.approve==1?false:true,
////                editor: this.statuscmbdrop,
//                renderer: function(a,b,c,d,e,f){
//                    if(a==1){
//                        return "Tolerance exceeded";
//                    }else{
//                        return "Tolerance within limit"
//                    }
//                }
//            }]);
//           this.itemlistCm.defaultSortable = true;
//           this.RefreshButt = new Wtf.Button({
//                                text:WtfGlobal.getLocaleText("acc.common.reset"),  //"Reset",
//                                iconCls :getButtonIconCls(Wtf.etype.resetbutton),
//                                scope:this,
//                                tooltip: {
//                                    title: WtfGlobal.getLocaleText("acc.common.reset"),  //"Reset",
//                                    text:WtfGlobal.getLocaleText("acc.common.reset")  //"Reset Form"
//                                },
//                                handler:function(){
//                                    this.itemlistStore.reload();
//                                }
//                            });
//            this.UpdateButt = new Wtf.Button({
//                                text:this.approve==1 ?WtfGlobal.getLocaleText("acc.cc.24"):WtfGlobal.getLocaleText("acc.common.update"),
//                                scope: this,
//                                iconCls :getButtonIconCls(Wtf.etype.save),
//                                tooltip: this.approve==1 ?WtfGlobal.getLocaleText("acc.cc.9"):WtfGlobal.getLocaleText("acc.cc.10"),  //"Please select appropriate status (Pending, Approved, or Recount), and click \"Approve\" button. On clicking \"Approve\" button, system will change the cycle count status from \"Pending\" to the selected status. Please note: For the products having cycle count tolerance more than set limit, status can't be set as \"Approved\" for first time. In such case, status should be selected as \"Recount\". Post \"Recount\" for such products, the cycle count can then be set as \"Approved\", even if it is exceeding tolerance.":"Please enter cycle count for below listed products, and click on \"Update\" button. On clicking \"Update\" button, system will update the old cycle count with newly entered cycle count for various products.",
//                                handler:function(){
//                                    if(this.approve==1){
//                                        this.confirmItemList();
//                                    }else{
//                                        this.confirmItemList();
//                                    }
//                                }
//                            });
////             this.SaveAsDraftButt = new Wtf.Button({
////                                text:WtfGlobal.getLocaleText("acc.common.saveasdraft"),
////                                scope: this,
////                                iconCls :getButtonIconCls(Wtf.etype.save),
////                                tooltip:WtfGlobal.getLocaleText("acc.common.saveasdraft"),  
////                                hidden:(this.approve),
////                                handler:function(){
////                                    alert("Are you sure ?");
////                                }
////                            });               
//            /*this.ApproveButt = new Wtf.Button({
//                        text:"Approve",
//                        scope: this,
//                        hidden : this.approve==1 ?false:true,
//                        tooltip: {
//                            title: "Update",
//                            text:"Update Form"
//                        },
//                        handler:function(){
//                            this.confirmItemList();
//                        }
//                    });*/
//        this.gridwrapper = new Wtf.Panel({
//            layout:'fit',
//            border: false,
//            region:'center',
//            items:this.itemlistGrid = new Wtf.grid.EditorGridPanel({
//                store: this.itemlistStore,
//                cm:this.itemlistCm,
//                sm: new Wtf.grid.RowSelectionModel({
//                        singleSelect:true
//                    }),
//                loadMask : true,
//                layout:'fit',
//                view: this.grpView,
//                clicksToEdit: 1,
//                tbar:[this.RefreshButt,this.UpdateButt]//,this.SaveAsDraftButt
//               
//            })
//        });
//          this.rowSelected1 = 1;
//         this.statuscmbdrop.on("change",function(combo,newval,oldval){
//             this.itemlistGrid.getSelectionModel().selectRow(this.rowSelected1,true);
//             var selectedRow = this.itemlistGrid.getSelectionModel().getSelected();
//             var status = selectedRow.get('originalStatusId');
//             var tolerance = selectedRow.get('originalToleranceMsg');
//             if(status == 0 && newval == 2){
//                  if(tolerance == 1){
//                      this.statuscmbdrop.setValue(oldval);
//                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.cc.11")], 2);
//                  } else {
//                      selectedRow.set('tolerancemsg', tolerance); //Show original msg if approved
//                  }
//             }else if(newval == 1){
//                 selectedRow.set('tolerancemsg',newval);
//             } else {
//                 selectedRow.set('tolerancemsg', tolerance);//Show original msg if reset to pending
//             }
//         },this);
//
//         this.itemlistGrid.on('afteredit',this.updateRowCount,this);        
////         this.countedQua.on("change", function(countedElem,val,e){
////            this.itemlistGrid.getSelectionModel().selectRow(this.rowSelected1,true);
////            var selectedRow = this.itemlistGrid.getSelectionModel().getSelected();
////            selectedRow.set('varianceQuantity',selectedRow.get('currentQuantity')-val);
////            var diffQoH = selectedRow.get('currentQuantity')-val;
////            if(diffQoH<0){
////                diffQoH = diffQoH*-1;
////            }
////            var toleranceQua = (selectedRow.get('currentQuantity')*selectedRow.get('tolerance'))/100;
////            if(diffQoH>toleranceQua){
////                selectedRow.set('tolerancemsg',1);
////            }else{
////                selectedRow.set('tolerancemsg',0);
////            }
////
////        }, this);
//        this.itemlistGrid.on("rowclick",function(gridp,rowindex,e){
//            this.rowSelected1 = rowindex;
//        },this);
//        
//       
//    },
//
//    updateRowCount : function (obj) {
//        if(obj!=null){
//             var rec=obj.record;
//             var val = (obj.value=="-")?0:obj.value;
//             if(obj.originalValue!=obj.value)
//                 {
//                     obj.record.data.valflag=true;
//                 }
//             if(obj.field=="newQuantity") {
//                 //rec.set('varianceQuantity',rec.get('currentQuantity')-val);//earlierQuantity
//                rec.set('varianceQuantity',(rec.get('earlierQuantity')=="-")?0:(getRoundofValue(val-rec.get('earlierQuantity'))).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
//                var diffQoH = (rec.get('earlierQuantity')=="-")?0:(getRoundofValue(val-rec.get('earlierQuantity')).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
//                if(diffQoH<0){
//                    diffQoH = diffQoH*-1;
//                }
//                var toleranceQua = ((rec.get('earlierQuantity')=="-")?0:rec.get('earlierQuantity')*rec.get('tolerance'))/100;
//                if(diffQoH>toleranceQua){
//                    rec.set('tolerancemsg',1);
//                }else{
//                    rec.set('tolerancemsg',0);
//                }
//             }
//        }
//    },
//            
//    confirmItemList: function(){
////        if(!this.cycleCountForm.form.isValid()){
////            this.loadMask1.hide();
////            return;
////        }
//        
//        this.confirmItemListCount = 0;
//        this.confirmItemList1();
//
//    },
//
//    confirmItemList1:function(){
//        var remTrailingComma = false;
//        if(this.itemlistStore.getCount()==0) {
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.cc.12")],2);
//            return;
//        }
//        var jsonData = "{'root': [";
//        var startcnt = this.confirmItemListCount;
//        for(var cnt = 0; cnt < this.itemlistStore.getCount(); cnt++) {
//            var rec = this.itemlistStore.getAt(cnt);
//            remTrailingComma = true;
//            if(this.approve==1){
//                    jsonData += this.getJsonFromRecord(rec) + ",";  
//            }else{
//                    if(this.cmbdrop.getValue()==1){
//                         jsonData += this.getJsonFromRecord(rec) + ",";
//                    }else{
//                            if(rec.data.valflag==true)
//                                jsonData += this.getJsonFromRecord(rec) + ",";
//                        }
//        }
//        }
//        if(remTrailingComma){
//            jsonData = jsonData.substr(0, jsonData.length - 1);
//        }
//        jsonData += "]}";
//         this.loadMask1 = new Wtf.LoadMask(this.gridwrapper.el.dom, {msg: WtfGlobal.getLocaleText("acc.msgbox.48")+(startcnt+1)+" "+WtfGlobal.getLocaleText("acc.common.to")+" "+(this.confirmItemListCount+1)+"  of  "+this.itemlistStore.getCount()});
//         this.loadMask1.show();
//        Wtf.Ajax.timeout = 600000;
//        Wtf.Ajax.requestEx({
////            url:Wtf.req.account+'cyclecount.jsp',
//            url : "ACCProduct/"+(this.approve==1?"approveCyclecountEntry":"makeCyclecountEntry")+".do",
//            params: {
//                mode: this.approve==1?121:118,             
//             
//                jsondata: jsonData,
//                isconfirm: true,
//                start: startcnt,
//                totalcnt: this.itemlistStore.getCount()
//            }}, this,
//            function(action) {
////                action = eval("("+ action + ")");
////                    if(this.confirmItemListCount < this.itemlistStore.getCount()) {
////                        this.confirmItemList1();
////                    } else {
//                      Wtf.Ajax.timeout = 30000;
//                      WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),(this.approve==1)?WtfGlobal.getLocaleText("acc.cc.29"):WtfGlobal.getLocaleText("acc.cc.30")],0);
////                      WtfComMsgBox(["Success", "Data confirmed and "+ (this.approve==1 ?"approved":"updated") +" successfully as cycle count"], 0);
//
////                        Wtf.getCmp("updatecyclecount").disable();
////                        Wtf.getCmp("saveccdrafts").disable();
////                        Wtf.getCmp("confirmCCount").disable();
////                        Wtf.getCmp("submitReasons").enable();
//                        this.loadMask1.hide();
//                        this.itemlistStore.reload();
//                        Wtf.dirtyStore.product = true;
//                        Wtf.dirtyStore.inventory = true;
////                    }
//            },
//            function(){
//                Wtf.Ajax.timeout = 30000;
//                 this.loadMask1.hide();
//                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.cc.13")], 1);
//        });
////        this.addItem.disable();
////        this.deleteItem.disable();
////        this.edititem.disable();
//    },
//
//    getJsonFromRecord : function(record) {
//        var recountStr="";
//        if(this.approve != 1 && this.cmbdrop.getValue()==1){
//            recountStr = '", "cyclecountId":"' + record.data.cyclecountId ;
//        }
//        return '{"id":"' + record.data.id +
//        '", "product_id":"' + record.data.product_id +
//        '","productid":"' + record.data.productid +
//        '", "initQua":"' + record.data.earlierQuantity +
//        '", "countQua":"' + record.data.newQuantity +
//          '", "statusid":"' + record.data.statusid + recountStr +
//        '", "reason":"' + record.data.reasone + '"}';
//    },
//
//    onRender: function(config){
//        Wtf.account.cycleCountPanel.superclass.onRender.call(this, config);
//        var ctdate = "";
//        if(this.approve==1){
//            this.countdate = new Wtf.form.DateField({
//                fieldLabel : WtfGlobal.getLocaleText("acc.cc.14"),  //"Count Date",
//                format:WtfGlobal.getOnlyDateFormat(),
//                value:new Date(),
//                name:'countdate',
//                allowBlank:false
//            });
//            this.countdate.on("change",function(){
//                if(!this.countdate.validate()) {
//                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.cc.15")], 2);
//                    return;
//                }
//                var countdate = this.countdate.getValue().format('Y-m-d');
//                this.itemlistStore.load({
//                    params:{
//                        countdate:countdate
//                    }
//                });
//            },this);
//            ctdate = this.countdate.getValue().format('Y-m-d');
//            this.issuerequisition=new Wtf.form.FormPanel({
//            region:'north',
//            height:70,
//            autoScroll:true,
//            border:false,
//            bodyStyle: "padding:20px",
//            items:[{
//                layout:'column',
//                border:false,
//                defaults:{
//                    layout:'form',
//                    border:false,
//                    defaults:{
//                        anchor:'90%'
//                    },
//                    columnWidth:.32
//                },
//                items:[{
//                    labelWidth:120,
//                    items:[this.countdate]
//                }
//                ]
//            }]
//         });
//        }else{
//            this.cyclecountentry_data = [['0','Initial'],['1','Re-Count']];
//            this.store_cyclecountentry = new Wtf.data.SimpleStore({
//                fields:['id','name'],
//                data:this.cyclecountentry_data
//            });
//         
//            this.cmbdrop = new Wtf.form.ComboBox({
//                fieldLabel :WtfGlobal.getLocaleText("acc.cc.1")+'*',
//                store : this.store_cyclecountentry,
//                hiddenName:'counttype',
//                displayField : 'name',
//                valueField : 'id',
//                mode: 'local',
//                editable : false,
//                triggerAction: 'all',
//                value:0,
//                // hideTrigger:true,
//                allowBlank:false
//            });
//            this.cmbdrop.on("change",function(){
//                this.itemlistStore.load({
//                    params:{
//                        type:this.cmbdrop.getValue()
//                    }
//                });
//            },this);
//
//             this.issuerequisition=new Wtf.form.FormPanel({
//                region:'north',
//                height:70,
//                autoScroll:true,
//                border:false,
//                bodyStyle: "padding:20px",
//                items:[{
//                    layout:'column',
//                    border:false,
//                    defaults:{
//                        layout:'form',
//                        border:false,
//                        defaults:{
//                            anchor:'90%'
//                        },
//                        columnWidth:.32
//                    },
//                    items:[{
//                        labelWidth:120,
//                        items:[this.cmbdrop]
//                    }]
//                }/*,{
//                    layout:'column',
//                    border:false,
//                    defaults:{
//                        columnWidth:.20
//                    },
//                    items:[this.RefreshButt,this.UpdateButt]
//                }*/]
//            });
//        }
//        this.newInnerPanel = new Wtf.Panel({
//            layout:'border',
//            border:false,
//            items:[this.issuerequisition,this.gridwrapper]
//        });
//        this.add(this.newInnerPanel);
//        this.itemlistStore.load({
//            params:{
//                countdate:ctdate,
//                type:0
//            }
//        });
//      
//    }
//       
//});
//
//
//
////Cycle Count Worksheet
//
//Wtf.account.cycleCountWorksheet= function(config){
//    Wtf.apply(this, config);
//    Wtf.account.cycleCountWorksheet.superclass.constructor.call(this, config);
//}
//
//
//Wtf.extend(Wtf.account.cycleCountWorksheet, Wtf.Panel, {
//    initComponent:function(){
//        this.itemlistRecord = new Wtf.data.Record.create([{
//            name: 'id'
//        },{
//            name:'product_id'
//        },{
//            name:'product'
//        },{
//            name:'uom'
//        },{
//            name:'earlierQuantity'
//        },{
//            name: 'newQuantity'
//        },{
//            name : 'lastcounteddate',
//            type:'date'
//
//        },{
//            name: 'countinterval'
//        },{
//            name: 'productid'
//        }]);
//
//        this.itemlistReader = new Wtf.data.KwlJsonReader({
//            root: 'data'
//        }, this.itemlistRecord);
//
//        this.itemlistSm = new Wtf.grid.RowSelectionModel({
//            width: 5,
//            singleSelect: true
//        });
//
//
//        this.grpView = new Wtf.grid.GroupingView({
//            forceFit: true,
//            startCollapsed:true,
//            showGroupName: true,
//            enableGroupingMenu: true,
//            hideGroupedColumn: true
//        });
//
//        this.itemlistStore = new Wtf.data.GroupingStore({
//            sortInfo: {
//                field: 'product',
//                direction: "ASC"
//            },
////             url:Wtf.req.account+'cyclecount.jsp',
//            url : "ACCProduct/getCycleCountWorkSheet.do",
//            baseParams:{
//                mode:119
//            },
////            groupField : 'product',
//            reader: this.itemlistReader
//        });
//
//     this.itemlistStore.on("load", function(store){
//            if(store.getCount()==0){
//                if(this.expButton)this.expButton.disable();
//            }else{
//                if(this.expButton)this.expButton.enable();
//            }
//     },this);
//
//
//        this.itemlistCm = new Wtf.grid.ColumnModel([
//            new Wtf.grid.RowNumberer(),
//            {
//                header: WtfGlobal.getLocaleText("acc.product.gridProductID"),  //"Product ID", acc.productList.gridProductid"
//                 pdfwidth:100,
//                dataIndex: 'product_id'
//            },
//            {
//                header: WtfGlobal.getLocaleText("Product"),  //"Product",
//                 pdfwidth:150,
//                dataIndex: 'product'
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.16"),  //"Last Counted Date",
//                 pdfwidth:150,
//                dataIndex: 'lastcounteddate',
//                renderer:WtfGlobal.onlyDateRenderer
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.17"),  //"Count interval",
//                 pdfwidth:150,
//                dataIndex: 'countinterval'
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.2"),  //"UoM",
//                 pdfwidth:150,
//                dataIndex: 'uom'
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.3"),  //"Earlier Quantity",
//                 pdfwidth:150,
//                dataIndex: 'earlierQuantity',
//                 renderer: function(a, b, c, d, e, f){
//                    return (a==0)?"-":a + "    " + c.get("uom");
//                }
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.4"),  //"Counted Quantity",
//                 pdfwidth:150,
//                 renderer: function(a, b, c, d, e, f){
//                    b.css = 'accountingEditableCell';
//                    return (a==0)?"-":a;
//                },
//                dataIndex: 'newQuantity'
//            }]);
//        this.itemlistCm.defaultSortable = true;
//        var btnArr=[];
//       
//        this.cyclecountentry_data = [['0','Initial'],['1','Re-Count']];
//        this.store_cyclecountentry = new Wtf.data.SimpleStore({
//            fields:['id','name'],
//            data:this.cyclecountentry_data
//        });
//
//        this.cmbdrop = new Wtf.form.ComboBox({
//            fieldLabel :WtfGlobal.getLocaleText("acc.cc.22"),  //'Count Type ',
//            store : this.store_cyclecountentry,
//            hiddenName:'counttype',
//            displayField : 'name',
//            valueField : 'id',
//            mode: 'local',
//            editable : false,
//            triggerAction: 'all',
//            value:0,
//            // hideTrigger:true,
//            allowBlank:false
//        });
//        this.countdateFrom = new Wtf.form.DateField({
//            fieldLabel : WtfGlobal.getLocaleText("acc.cc.19"),  //"Count Date From",
//            format:WtfGlobal.getOnlyDateFormat(),
//            name:'countdatefrom',
//            allowBlank:false
//        });
//        this.countdateTo = new Wtf.form.DateField({
//            fieldLabel : WtfGlobal.getLocaleText("acc.cc.20"),  //"Count Date To",
//            format:WtfGlobal.getOnlyDateFormat(),
//            name:'countdateto',
//            allowBlank:false
//        });
//        this.fetchBtn = new Wtf.Button({
//            text:WtfGlobal.getLocaleText("acc.cc.21"),  //'Fetch Data',
//            tooltip: WtfGlobal.getLocaleText("acc.common.fetchTT"),
//            scope:this,
//            iconCls:'accountingbase fetch',
//            handler:this.fetchDetails
//
//        });
//        this.grid = new Wtf.grid.EditorGridPanel({
//                store: this.itemlistStore,
//                cm:this.itemlistCm,
//                sm: new Wtf.grid.RowSelectionModel({
//                        singleSelect:true
//                    }),
//                loadMask : true,
//                layout:'fit',
//                view: this.grpView,
//                clicksToEdit: 1
//
//            });
//        btnArr.push(this.expButton=new Wtf.exportButton({
//            obj:this,
//            text: WtfGlobal.getLocaleText("acc.common.export"),
//            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
//            disabled :true,
//            params:{
//                   stdate:'',//this.countdateFrom.getValue().format('Y-m-d'),
//                   enddate:'',//this.countdateTo.getValue().format('Y-m-d'),
//                   includerecount:''//this.cmbdrop.getValue()
//            },
//            menuItem:{csv:true,pdf:true,rowPdf:false},
//            get:151
//        }));
//        btnArr.push(this.fetchBtn);
//        this.gridwraper = new Wtf.Panel({
//            layout:'fit',
//            border: false,
//            region:'center',
//            items:this.grid,
//            tbar:[btnArr]
//        });
//        this.rowSelected1 = 1;
//        
//       
//
//
//    },
//    
//    onRender: function(config){
//        Wtf.account.cycleCountWorksheet.superclass.onRender.call(this, config);
//
//        
//        this.issuerequisition=new Wtf.form.FormPanel({
//            region:'north',
//            height:140,
//            autoScroll:true,
//            border:false,
//            bodyStyle: "padding:20px",
//            items:[{
//                layout:'column',
//                border:false,
//                defaults:{
//                    layout:'form',
//                    border:false,
//                    defaults:{
//                        anchor:'90%'
//                    },
//                    columnWidth:.40
//                },
//                items:[{
//                    labelWidth:120,
//                    items:[this.countdateFrom]
//                },{
//                    labelWidth:120,
//                    items:[this.countdateTo]
//                }
//                ]
//            },{
//                layout:'column',
//                border:false,
//                defaults:{
//                    layout:'form',
//                    border:false,
//                    defaults:{
//                        anchor:'90%'
//                    },
//                    columnWidth:.40
//                },
//                items:[{
//                    labelWidth:120,
//                    items:[ this.cmbdrop]
//                }
//                ]
//            }/*,
//            this.fetchBtn*/]
//        });
//        this.newInnerPanel = new Wtf.Panel({
//            layout:'border',
//            border:false,
//            items:[this.issuerequisition,this.gridwraper]
//        });
//        this.add(this.newInnerPanel);
//
//
//    },
//    fetchDetails:function(){
//        if(!this.countdateFrom.validate() || !this.countdateTo.validate()) {
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.cc.15")], 2);
//            return;
//        }else if(this.countdateFrom.getValue()>this.countdateTo.getValue()){
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.cc.18")], 2);
//            this.countdateTo.setValue("");
//            return;
//        }
        
//        this.confirmItemListCount = 0;
//        this.confirmItemList1();
//
//    },
//
//    confirmItemList1:function(){
//        var remTrailingComma = false;
//        if(this.itemlistStore.getCount()==0) {
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.cc.12")],2);
//            return;
//        }
//        var jsonData = "{'root': [";
//        var startcnt = this.confirmItemListCount;
//        for(var cnt = 0; cnt < this.itemlistStore.getCount(); cnt++) {
//            var rec = this.itemlistStore.getAt(cnt);
//            remTrailingComma = true;
//            if(this.approve==1){
//                    jsonData += this.getJsonFromRecord(rec) + ",";  
//            }else{
//                    if(this.cmbdrop.getValue()==1){
//                         jsonData += this.getJsonFromRecord(rec) + ",";
//                    }else{
//                            if(rec.data.valflag==true)
//                                jsonData += this.getJsonFromRecord(rec) + ",";
//                        }
//        }
//        }
//        if(remTrailingComma){
//            jsonData = jsonData.substr(0, jsonData.length - 1);
//        }
//        jsonData += "]}";
//         this.loadMask1 = new Wtf.LoadMask(this.gridwrapper.el.dom, {msg: WtfGlobal.getLocaleText("acc.msgbox.48")+(startcnt+1)+" "+WtfGlobal.getLocaleText("acc.common.to")+" "+(this.confirmItemListCount+1)+"  of  "+this.itemlistStore.getCount()});
//         this.loadMask1.show();
//        WtfGlobal.setAjaxTimeOut();
//        Wtf.Ajax.requestEx({
////            url:Wtf.req.account+'cyclecount.jsp',
//            url : "ACCProduct/"+(this.approve==1?"approveCyclecountEntry":"makeCyclecountEntry")+".do",
//            params: {
//                mode: this.approve==1?121:118,             
//             
//                jsondata: jsonData,
//                isconfirm: true,
//                start: startcnt,
//                totalcnt: this.itemlistStore.getCount()
//            }}, this,
//            function(action) {
////                action = eval("("+ action + ")");
////                    if(this.confirmItemListCount < this.itemlistStore.getCount()) {
////                        this.confirmItemList1();
////                    } else {
//                      WtfGlobal.resetAjaxTimeOut();
//                      WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),(this.approve==1)?WtfGlobal.getLocaleText("acc.cc.29"):WtfGlobal.getLocaleText("acc.cc.30")],0);
////                      WtfComMsgBox(["Success", "Data confirmed and "+ (this.approve==1 ?"approved":"updated") +" successfully as cycle count"], 0);
//
////                        Wtf.getCmp("updatecyclecount").disable();
////                        Wtf.getCmp("saveccdrafts").disable();
////                        Wtf.getCmp("confirmCCount").disable();
////                        Wtf.getCmp("submitReasons").enable();
//                        this.loadMask1.hide();
//                        this.itemlistStore.reload();
//                        Wtf.dirtyStore.product = true;
//                        Wtf.dirtyStore.inventory = true;
////                    }
//            },
//            function(){
//                WtfGlobal.resetAjaxTimeOut();
//                 this.loadMask1.hide();
//                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.cc.13")], 1);
//        });
////        this.addItem.disable();
////        this.deleteItem.disable();
////        this.edititem.disable();
//    },
//
//    getJsonFromRecord : function(record) {
//        var recountStr="";
//        if(this.approve != 1 && this.cmbdrop.getValue()==1){
//            recountStr = '", "cyclecountId":"' + record.data.cyclecountId ;
//        }
//        return '{"id":"' + record.data.id +
//        '", "product_id":"' + record.data.product_id +
//        '","productid":"' + record.data.productid +
//        '", "initQua":"' + record.data.earlierQuantity +
//        '", "countQua":"' + record.data.newQuantity +
//          '", "statusid":"' + record.data.statusid + recountStr +
//        '", "reason":"' + record.data.reasone + '"}';
//    },
//
//    onRender: function(config){
//        Wtf.account.cycleCountPanel.superclass.onRender.call(this, config);
//        var ctdate = "";
//        if(this.approve==1){
//            this.countdate = new Wtf.form.DateField({
//                fieldLabel : WtfGlobal.getLocaleText("acc.cc.14"),  //"Count Date",
//                format:WtfGlobal.getOnlyDateFormat(),
//                value:new Date(),
//                name:'countdate',
//                allowBlank:false
//            });
//            this.countdate.on("change",function(){
//                if(!this.countdate.validate()) {
//                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.cc.15")], 2);
//                    return;
//                }
//                var countdate = this.countdate.getValue().format('Y-m-d');
//                this.itemlistStore.load({
//                    params:{
//                        countdate:countdate
//                    }
//                });
//            },this);
//            ctdate = this.countdate.getValue().format('Y-m-d');
//            this.issuerequisition=new Wtf.form.FormPanel({
//            region:'north',
//            height:70,
//            autoScroll:true,
//            border:false,
//            bodyStyle: "padding:20px",
//            items:[{
//                layout:'column',
//                border:false,
//                defaults:{
//                    layout:'form',
//                    border:false,
//                    defaults:{
//                        anchor:'90%'
//                    },
//                    columnWidth:.32
//                },
//                items:[{
//                    labelWidth:120,
//                    items:[this.countdate]
//                }
//                ]
//            }]
//         });
//        }else{
//            this.cyclecountentry_data = [['0','Initial'],['1','Re-Count']];
//            this.store_cyclecountentry = new Wtf.data.SimpleStore({
//                fields:['id','name'],
//                data:this.cyclecountentry_data
//            });
//         
//            this.cmbdrop = new Wtf.form.ComboBox({
//                fieldLabel :WtfGlobal.getLocaleText("acc.cc.1")+'*',
//                store : this.store_cyclecountentry,
//                hiddenName:'counttype',
//                displayField : 'name',
//                valueField : 'id',
//                mode: 'local',
//                editable : false,
//                triggerAction: 'all',
//                value:0,
//                // hideTrigger:true,
//                allowBlank:false
//            });
//            this.cmbdrop.on("change",function(){
//                this.itemlistStore.load({
//                    params:{
//                        type:this.cmbdrop.getValue()
//                    }
//                });
//            },this);
//
//             this.issuerequisition=new Wtf.form.FormPanel({
//                region:'north',
//                height:70,
//                autoScroll:true,
//                border:false,
//                bodyStyle: "padding:20px",
//                items:[{
//                    layout:'column',
//                    border:false,
//                    defaults:{
//                        layout:'form',
//                        border:false,
//                        defaults:{
//                            anchor:'90%'
//                        },
//                        columnWidth:.32
//                    },
//                    items:[{
//                        labelWidth:120,
//                        items:[this.cmbdrop]
//                    }]
//                }/*,{
//                    layout:'column',
//                    border:false,
//                    defaults:{
//                        columnWidth:.20
//                    },
//                    items:[this.RefreshButt,this.UpdateButt]
//                }*/]
//            });
//        }
//        this.newInnerPanel = new Wtf.Panel({
//            layout:'border',
//            border:false,
//            items:[this.issuerequisition,this.gridwrapper]
//        });
//        this.add(this.newInnerPanel);
//        this.itemlistStore.load({
//            params:{
//                countdate:ctdate,
//                type:0
//            }
//        });
//      
//    }
//       
//});
//
//
//
////Cycle Count Worksheet
//
//Wtf.account.cycleCountWorksheet= function(config){
//    Wtf.apply(this, config);
//    Wtf.account.cycleCountWorksheet.superclass.constructor.call(this, config);
//}
//
//
//Wtf.extend(Wtf.account.cycleCountWorksheet, Wtf.Panel, {
//    initComponent:function(){
//        this.itemlistRecord = new Wtf.data.Record.create([{
//            name: 'id'
//        },{
//            name:'product_id'
//        },{
//            name:'product'
//        },{
//            name:'uom'
//        },{
//            name:'earlierQuantity'
//        },{
//            name: 'newQuantity'
//        },{
//            name : 'lastcounteddate',
//            type:'date'
//
//        },{
//            name: 'countinterval'
//        },{
//            name: 'productid'
//        }]);
//
//        this.itemlistReader = new Wtf.data.KwlJsonReader({
//            root: 'data'
//        }, this.itemlistRecord);
//
//        this.itemlistSm = new Wtf.grid.RowSelectionModel({
//            width: 5,
//            singleSelect: true
//        });
//
//
//        this.grpView = new Wtf.grid.GroupingView({
//            forceFit: true,
//            startCollapsed:true,
//            showGroupName: true,
//            enableGroupingMenu: true,
//            hideGroupedColumn: true
//        });
//
//        this.itemlistStore = new Wtf.data.GroupingStore({
//            sortInfo: {
//                field: 'product',
//                direction: "ASC"
//            },
////             url:Wtf.req.account+'cyclecount.jsp',
//            url : "ACCProduct/getCycleCountWorkSheet.do",
//            baseParams:{
//                mode:119
//            },
////            groupField : 'product',
//            reader: this.itemlistReader
//        });
//
//     this.itemlistStore.on("load", function(store){
//            if(store.getCount()==0){
//                if(this.expButton)this.expButton.disable();
//            }else{
//                if(this.expButton)this.expButton.enable();
//            }
//     },this);
//
//
//        this.itemlistCm = new Wtf.grid.ColumnModel([
//            new Wtf.grid.RowNumberer(),
//            {
//                header: WtfGlobal.getLocaleText("acc.product.gridProductID"),  //"Product ID", acc.productList.gridProductid"
//                 pdfwidth:100,
//                dataIndex: 'product_id'
//            },
//            {
//                header: WtfGlobal.getLocaleText("Product"),  //"Product",
//                 pdfwidth:150,
//                dataIndex: 'product'
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.16"),  //"Last Counted Date",
//                 pdfwidth:150,
//                dataIndex: 'lastcounteddate',
//                renderer:WtfGlobal.onlyDateRenderer
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.17"),  //"Count interval",
//                 pdfwidth:150,
//                dataIndex: 'countinterval'
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.2"),  //"UoM",
//                 pdfwidth:150,
//                dataIndex: 'uom'
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.3"),  //"Earlier Quantity",
//                 pdfwidth:150,
//                dataIndex: 'earlierQuantity',
//                 renderer: function(a, b, c, d, e, f){
//                    return (a==0)?"-":a + "    " + c.get("uom");
//                }
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.4"),  //"Counted Quantity",
//                 pdfwidth:150,
//                 renderer: function(a, b, c, d, e, f){
//                    b.css = 'accountingEditableCell';
//                    return (a==0)?"-":a;
//                },
//                dataIndex: 'newQuantity'
//            }]);
//        this.itemlistCm.defaultSortable = true;
//        var btnArr=[];
//       
//        this.cyclecountentry_data = [['0','Initial'],['1','Re-Count']];
//        this.store_cyclecountentry = new Wtf.data.SimpleStore({
//            fields:['id','name'],
//            data:this.cyclecountentry_data
//        });
//
//        this.cmbdrop = new Wtf.form.ComboBox({
//            fieldLabel :WtfGlobal.getLocaleText("acc.cc.22"),  //'Count Type ',
//            store : this.store_cyclecountentry,
//            hiddenName:'counttype',
//            displayField : 'name',
//            valueField : 'id',
//            mode: 'local',
//            editable : false,
//            triggerAction: 'all',
//            value:0,
//            // hideTrigger:true,
//            allowBlank:false
//        });
//        this.countdateFrom = new Wtf.form.DateField({
//            fieldLabel : WtfGlobal.getLocaleText("acc.cc.19"),  //"Count Date From",
//            format:WtfGlobal.getOnlyDateFormat(),
//            name:'countdatefrom',
//            allowBlank:false
//        });
//        this.countdateTo = new Wtf.form.DateField({
//            fieldLabel : WtfGlobal.getLocaleText("acc.cc.20"),  //"Count Date To",
//            format:WtfGlobal.getOnlyDateFormat(),
//            name:'countdateto',
//            allowBlank:false
//        });
//        this.fetchBtn = new Wtf.Button({
//            text:WtfGlobal.getLocaleText("acc.cc.21"),  //'Fetch Data',
//            tooltip: WtfGlobal.getLocaleText("acc.common.fetchTT"),
//            scope:this,
//            iconCls:'accountingbase fetch',
//            handler:this.fetchDetails
//
//        });
//        this.grid = new Wtf.grid.EditorGridPanel({
//                store: this.itemlistStore,
//                cm:this.itemlistCm,
//                sm: new Wtf.grid.RowSelectionModel({
//                        singleSelect:true
//                    }),
//                loadMask : true,
//                layout:'fit',
//                view: this.grpView,
//                clicksToEdit: 1
//
//            });
//        btnArr.push(this.expButton=new Wtf.exportButton({
//            obj:this,
//            text: WtfGlobal.getLocaleText("acc.common.export"),
//            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
//            disabled :true,
//            params:{
//                   stdate:'',//this.countdateFrom.getValue().format('Y-m-d'),
//                   enddate:'',//this.countdateTo.getValue().format('Y-m-d'),
//                   includerecount:''//this.cmbdrop.getValue()
//            },
//            menuItem:{csv:true,pdf:true,rowPdf:false},
//            get:151
//        }));
//        btnArr.push(this.fetchBtn);
//        this.gridwraper = new Wtf.Panel({
//            layout:'fit',
//            border: false,
//            region:'center',
//            items:this.grid,
//            tbar:[btnArr]
//        });
//        this.rowSelected1 = 1;
//        
//       
//
//
//    },
//    
//    onRender: function(config){
//        Wtf.account.cycleCountWorksheet.superclass.onRender.call(this, config);
//
//        
//        this.issuerequisition=new Wtf.form.FormPanel({
//            region:'north',
//            height:140,
//            autoScroll:true,
//            border:false,
//            bodyStyle: "padding:20px",
//            items:[{
//                layout:'column',
//                border:false,
//                defaults:{
//                    layout:'form',
//                    border:false,
//                    defaults:{
//                        anchor:'90%'
//                    },
//                    columnWidth:.40
//                },
//                items:[{
//                    labelWidth:120,
//                    items:[this.countdateFrom]
//                },{
//                    labelWidth:120,
//                    items:[this.countdateTo]
//                }
//                ]
//            },{
//                layout:'column',
//                border:false,
//                defaults:{
//                    layout:'form',
//                    border:false,
//                    defaults:{
//                        anchor:'90%'
//                    },
//                    columnWidth:.40
//                },
//                items:[{
//                    labelWidth:120,
//                    items:[ this.cmbdrop]
//                }
//                ]
//            }/*,
//            this.fetchBtn*/]
//        });
//        this.newInnerPanel = new Wtf.Panel({
//            layout:'border',
//            border:false,
//            items:[this.issuerequisition,this.gridwraper]
//        });
//        this.add(this.newInnerPanel);
//
//
//    },
//    fetchDetails:function(){
//        if(!this.countdateFrom.validate() || !this.countdateTo.validate()) {
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.cc.15")], 2);
//            return;
//        }else if(this.countdateFrom.getValue()>this.countdateTo.getValue()){
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.cc.18")], 2);
//            this.countdateTo.setValue("");
//            return;
//        }
//        var fromdate = this.countdateFrom.getValue().format('Y-m-d');
//        var todate = this.countdateTo.getValue().format('Y-m-d');
//
//        this.itemlistStore.load({
//            params:{
//                ctdatefr:fromdate,
//                ctdateto:todate,
//                includerecount:this.cmbdrop.getValue()
//            }
//        });
//        this.expButton.params = {
//                   stdate:this.countdateFrom.getValue().format('Y-m-d'),
//                   enddate:this.countdateTo.getValue().format('Y-m-d'),
//                   includerecount:this.cmbdrop.getValue()                
//            };
//        this.expButton.setParams({
//               stdate:fromdate,
//               enddate:todate,
//               includerecount:this.cmbdrop.getValue()
//         });
//    }
//});
//
//
//
////Cyclecount Report change
//Wtf.account.CyclecountReport=function(config){
//    this.summary = new Wtf.ux.grid.GridSummary();
//    this.CyclecountReportRec = new Wtf.data.Record.create([
//        {
//            name: 'id'
//        },{
//            name:'product_id'
//        },{
//            name:'product'
//        },{
//            name:'uom'
//        },{
//            name:'earlierQuantity'
//        },{
//            name: 'newQuantity'
//        },{
//            name : 'varianceQuantity'
//        },{
//            name: 'variance'
//        },{
//            name: 'reasone'
//        },{
//            name:'lastcounteddate',
//            type:'date'
//        },{
//            name: 'productid'
//        },{
//            name:'statusid'
//        },{
//            name:'tolerance'
//        },{
//            name:'tolerancemsg'
//        }]);
//    this.CyclecountReportStore = new Wtf.data.Store({
//        reader: new Wtf.data.KwlJsonReader({
//            root: "data",
//            totalProperty:"count"
//        },this.CyclecountReportRec),
////        url: Wtf.req.account+'cyclecount.jsp',
//        url : "ACCProduct/cycleCountReport.do",
//        baseParams:{
//            mode:122
//        }
//    });
//    
//     var dataArr = new Array();
//     dataArr.push([0,WtfGlobal.getLocaleText("acc.rem.105")],['1','Pending'],['2','Recount'],[3,'Approved']);
//     this.CyclecountReportStore.on("load", function(store){
//            if(store.getCount()==0){
//                if(this.expButton)this.expButton.disable();
//            }else{
//                if(this.expButton)this.expButton.enable();
//            }
//     },this);
//     
//     this.typeStore = new Wtf.data.SimpleStore({
//        fields: [{name:'typeid',type:'int'}, 'name'],
//        data :dataArr
//    });
//    
//     this.viewFilter = new Wtf.form.ComboBox({
//        store: this.typeStore,
//        name:'typeid',
//        displayField:'name',
//        id:'view'+config.helpmodeid+config.id,
//        valueField:'typeid',
//        mode: 'local',
//        defaultValue:0,
//        width:160,
//        listWidth:160,
//        triggerAction: 'all',
//        typeAhead:true,
//        selectOnFocus:true
//    });
//     this.cyclecountstatus_data = [['0','Pending'],['1','Recount'],['2','Approved']];
//        this.store_cyclecountstatus = new Wtf.data.SimpleStore({
//            fields:['statusid','status'],
//            data:this.cyclecountstatus_data
//        });
//
//        this.statuscmbdrop = new Wtf.form.ComboBox({
//            fieldLabel :WtfGlobal.getLocaleText("acc.cc.1"),  //'Count Type*',
//            store : this.store_cyclecountstatus,
//            hiddenName:'countstatus',
//            displayField : 'status',
//            valueField : 'statusid',
//            mode: 'local',
//            editable : false,
//            triggerAction: 'all'
//        });
//    this.rowNo=new Wtf.grid.RowNumberer();
//    this.gridcm= new Wtf.grid.ColumnModel([
//            new Wtf.grid.RowNumberer(),
//            {
//                header: WtfGlobal.getLocaleText("acc.product.gridProductID"),  //"Product ID", acc.productList.gridProductid"
//                 pdfwidth:100,
//                dataIndex: 'product_id'
//            },
//            {
//                header: WtfGlobal.getLocaleText("acc.product.gridProduct"),  //"Product",
//                pdfwidth:175,
//                dataIndex: 'product'
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.2"),  //"UoM",
//                pdfwidth:175,
//                dataIndex: 'uom'
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.3"),  //"Earlier Quantity",
//                pdfwidth:50,
//                dataIndex: 'earlierQuantity',
//                renderer: function(a, b, c, d, e, f){
//                    return (a==0)?"-":a + "    " + c.get("uom");
//                }
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.4"),  //"Counted Quantity",
//                pdfwidth:50,
//                dataIndex: 'newQuantity',
//                renderer: function(a, b, c, d, e, f){
//                    b.css = 'accountingEditableCell';
//                    return (a==0)?"-":a;// + "    " + c.get("uom");
//                }
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.5"),  //"Variance Quantity",
//                pdfwidth:50,
//                renderer: function(a, b, c, d, e, f){
//                    return (a==0)?"-":a + "    " + c.get("uom");
//                },
//                dataIndex: 'varianceQuantity'
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.6"),  //"Reason",
//                pdfwidth:150,
//                dataIndex: 'reasone'
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.16"),  //"Last Counted Date",
//                 pdfwidth:150,
//                dataIndex: 'lastcounteddate',
//                renderer:WtfGlobal.onlyDateRenderer
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.8"),  //"Status",
//                dataIndex: 'statusid',
//                pdfwidth:100,
////                hidden:this.approve==1?false:true,
//                renderer:Wtf.comboBoxRenderer(this.statuscmbdrop)
//            },{
//                header: WtfGlobal.getLocaleText("acc.cc.7"),  //"Tolerance",
//                dataIndex: 'tolerancemsg',
//                pdfwidth:150,
////                mode:this.approve==1?false:true,
////                editor: this.statuscmbdrop,
//                renderer: function(a,b,c,d,e,f){
//                    if(a==1){
//                        return WtfGlobal.getLocaleText("acc.field.Toleranceexceeded");
//                    }else{
//                        return WtfGlobal.getLocaleText("acc.field.Tolerancewithinlimit");
//                    }
//                }
//            }]);
//    this.grid = new Wtf.grid.GridPanel({
//        stripeRows :true,
//        store: this.CyclecountReportStore,
//        cm: this.gridcm,
//        border : false,
//        loadMask : true,
//        plugins:[this.summary],
//        viewConfig: {
//            forceFit:true,
//            getRowClass:this.getRowClass.createDelegate(this)
//        },
//        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
//            pageSize: 15,
//            id: "pagingtoolbar" + this.id,
//            store: this.CyclecountReportStore,
////            searchField: this.quickPanelSearch,
//            displayInfo: true,
//            displayMsg: WtfGlobal.getLocaleText("acc.rem.116"),
//            emptyMsg: WtfGlobal.getLocaleText("acc.1099.noresult"),
//            plugins: this.pP = new Wtf.common.pPageSize({
//            id : "pPageSize_"+this.id
//            })
//        })
//    });
//    this.startDate=new Wtf.form.DateField({
//        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
//        name:'stdate',
//        format:WtfGlobal.getOnlyDateFormat(),
//        value:new Date()
//    });
//    this.endDate=new Wtf.form.DateField({
//        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
//        format:WtfGlobal.getOnlyDateFormat(),
//        name:'enddate',
//        value:new Date()
//    });
//    var btnArr=[];
//    btnArr.push(this.expButton=new Wtf.exportButton({
//        obj:this,
//        text: WtfGlobal.getLocaleText("acc.common.export"),
//        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
//        disabled :true,
//        params:{stdate:new Date().format('Y-m-d'),
//               enddate:new Date().format('Y-m-d'),
//               accountid:this.accountID
//        },
//        menuItem:{csv:true,pdf:true,rowPdf:false},
//        get:150
//    }));
//    this.fetchCyclecountReport();
//    Wtf.apply(this,{
//        items:[{
//            border:false,
//            layout : "border",
//            scope:this,
//            items:[{
//                region:'center',
//                layout:'fit',
//                border:false,
//                items:[this.grid]
//            }],
//            tbar:[WtfGlobal.getLocaleText("acc.common.from"),this.startDate,'-',WtfGlobal.getLocaleText("acc.common.to"),this.endDate,'-',{
//                xtype:'button',
//                text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
//                tooltip:WtfGlobal.getLocaleText("acc.cc.25"),  //"Select a time period to view corresponding cycle count records.",
//                iconCls:'accountingbase fetch',
//                scope:this,
//                handler:this.fetchCyclecountReport
//            },'-',btnArr,"->","&nbsp;View",this.viewFilter]
//        }]
//
//    },config);
//    Wtf.apply(this,{
//        items:[{
//            border:false,
//            layout : "border",
//            scope:this,
//            items:[{
//                region:'center',
//                layout:'fit',
//                border:false,
//                items:[this.grid]
//            }],
//            tbar:[WtfGlobal.getLocaleText("acc.common.from"),this.startDate,'-',WtfGlobal.getLocaleText("acc.common.to"),this.endDate,'-',{
//                xtype:'button',
//                text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
//                tooltip:WtfGlobal.getLocaleText("acc.cc.25"),  //"Select a time period to view corresponding cycle count records.",
//                iconCls:'accountingbase fetch',
//                scope:this,
//                handler:this.fetchCyclecountReport
//            },'-',btnArr,"->","&nbsp;View",this.viewFilter]
//        }]
//
//    },config);
////    Wtf.apply(this,{
////        items:this.TrialBalancegrid
////    },config)
//
//     Wtf.account.CyclecountReport.superclass.constructor.call(this,config);
//     this.addEvents({
//        'account':true
//     });
//     this.grid.on('rowclick',this.onRowClick, this);
//     this.viewFilter.on("select", function(){
//         var val=0;
//        this.CyclecountReportStore.baseParams = {  
//            mode:122,
//            stdate:this.sdate,
//            enddate:this.edate,
//            val:this.viewFilter.getValue()
//        };
//        this.CyclecountReportStore.load({
//            params:{
//            start:0,
//            limit:15            
//            }
//        });
//    },this);
//}
//Wtf.extend( Wtf.account.CyclecountReport,Wtf.Panel,{
//    nameRenderer:function(v,m,rec){
//        return (rec.data["fmt"]?'<b>'+v+'</b>':WtfGlobal.linkRenderer(v));
//    },
//
//    currencyRenderer:function(v,m,rec){
//        return (rec.data["fmt"]?WtfGlobal.currencySummaryRenderer(v):WtfGlobal.currencyRenderer(v));
//    },
//
//    getRowClass:function(record,grid){
//        var colorCss="";
//        switch(record.data["fmt"]){
//            case "T":colorCss=" grey-background";break;
//            case "B":colorCss=" red-background";break;
//            case "H":colorCss=" header-background";break;
//            case "A":colorCss=" darkyellow-background";break;
//        }
//        return colorCss;
//    },
//    fetchCyclecountReport:function(){
//        this.sDate=this.startDate.getValue();
//        this.eDate=this.endDate.getValue();
//        this.sdate=this.sDate.format('Y-m-d');
//        this.edate=this.eDate.format('Y-m-d');
//        if(this.sDate>this.eDate){
//            WtfComMsgBox(1,2);
//            return;
//        }
//        var limit = 15;
//        if(this.pP.combo!=null){
//             limit = this.pP.combo.value;
//        }
//        this.CyclecountReportStore.baseParams = {  
//            mode:122,
//            stdate:this.sdate,
//            enddate:this.edate,
//            val:0
//            /*,
//            start:0,
//            limit:limit*/
//        };
//        this.CyclecountReportStore.load({
//            params:{
//            start:0,
//            limit:limit
//            }
//        });
//        this.expButton.setParams({
//            stdate:this.sdate,
//            enddate:this.edate
//        });
//
//    },
//
//    onRowClick:function(g,i,e){
//        e.stopEvent();
//        var el=e.getTarget("a");
//        if(el==null)return;
//        var accid=this.CyclecountReportStore.getAt(i).data['accountid'];
//        this.fireEvent('account',accid,this.startDate.getValue(),this.endDate.getValue());
//    },
//    getDates:function(start){
//        var d=new Date();
//        var monthDateStr=d.format('M d');
//        if(Wtf.account.companyAccountPref.fyfrom)
//            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
//        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
//        if(d<fd)
//            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
//        if(start)
//            return fd;
//        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
//    }
//});
