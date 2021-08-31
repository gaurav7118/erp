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
 //var communityTabs = [];

var isUSServer = false;
var isSelfService = false;
var userTabs = [];
var projectTabs = [];
var isFirstClick = true;
//var courseTabs = [];
//var accountingTabs = [];
var mainPanel;
var deskeraAdmin = false;
var numTabs = 1;
var dojoInitCount = 0;
var companyName = "";
var subdomain = "";
var pagebaseURL ="";
var CUSTOM_FIELD_KEY = "customfield";
var CUSTOM_FIELD_KEY_PRODUCT = "productcustomfield";
var GlobalComboStore=[];
var GlobalColumnModel =[];
var GlobalColumnModelForProduct =[];
var GlobalColumnModelForReports =[];
var GlobalColumnModelForLandedCostCategory = undefined;
var GlobalCustomTemplateList =[];
var GlobalDimensionModelForReports =[];
var GlobalDimensionCustomFieldModel = [];
var GlobalProductmasterFieldsArr=[];
var GlobalComboReader = new Wtf.data.Record.create([{name:'name'},{name:'id'}]);
var BCHLCompanyId ="db0ccfa6-5710-4434-998d-2b2abe4d1f46,a4792363-b0e1-4b67-992b-2851234d5ea6,284571bf-dd27-4d75-9e8d-50394598ac9b";
var PacificTechCompanyId="b52ac139-f431-4cce-a040-433d9b16a498";
var MPRPTemplate_companyids = '2d7ac788-40dc-4ca3-b53e-df38fa26724b';
//var Optimized_CompanyIds="d7d497dc-81bb-42d7-a1a9-9f34564ac2b2";
var usCurrencyID="1";
//Wtf.defaultReferralKey = 0;
Wtf.stockAdjustmentTempDataHolder=new Array();
Wtf.stockAdjustmentProdBatchQtyMapArr=new Array();
Wtf.CurrencySymbol = "\u0024";
Wtf.SundryCustomer = "Sundry Customer";
Wtf.SundryVendor = "Sundry Vendor";
Wtf.UOMSchema = 0;
Wtf.PackegingSchema =1;
Wtf.isPMSync = false;
Wtf.iseClaimSync = false;
Wtf.pmURL="http://192.168.0.208:8080/stagingpm/a/mrp/";
Wtf.comboTemplate = new Wtf.XTemplate('<tpl for="."><div wtf:qtip="{[values.hasAccess === false ? "You cannot assign Archived records" : "" ]}" class="{[values.hasAccess === false ? "x-combo-list-item disabled-record" : "x-combo-list-item"]}">',
                                                    '{name}',
                                                '</div></tpl>');
var fnInt;
var lang = (WtfGlobal.getLocaleText("acc.common.to") == "To")?true:false;
var menu1 = new Wtf.menu.Menu({
    id: 'mainMenu',
    cls:'mainMenu1',
    items:['']
});
var menu2 = new Wtf.menu.Menu({
    id: 'mainMenu11',
    cls:'mainMenu2',
    items:['']
});
var menu3 = new Wtf.menu.Menu({
    id: 'mainMenu12',
    cls:'mainMenu3',
    items:['']
});
var menu4 = new Wtf.menu.Menu({
    id: 'mainMenu13',
    cls:'mainMenu4',
    items:['']
});

var menu5 = new Wtf.menu.Menu({
    id: 'mainMenu14',
    cls:'mainMenu5',
    items:['']
});
var menu6 = new Wtf.menu.Menu({
    id: 'mainMenu15',
    cls:'mainMenu6',
    items:['']
});
				// code for ie9 Viewport problem  (Start)	Neeraj
if ((typeof Range !== "undefined") && !Range.prototype.createContextualFragment){
    Range.prototype.createContextualFragment = function(html)
    {
        var frag = document.createDocumentFragment(),
        div = document.createElement("div");
        frag.appendChild(div);
        div.outerHTML = html;
        return frag;
    };
}
				// code for ie9 Viewport problem (end)

Wtf.Button.override({
    setTooltip: function(qtipText) {
        if(this.getEl()!=undefined){
        var btnEl = this.getEl().child(this.buttonSelector)
        Wtf.QuickTips.register({
            target: btnEl.id,
            text: qtipText
        });
        } 
    }
});

function closeTimezonePop() {
    document.getElementById("header").style.height = "26px";
    document.getElementById("headTimezone").style.display = "none";
   
   if(menu1){//ERP-20463
        menu1.getEl().removeClass('mainMenu1 changedtop');
        menu1.getEl().addClass('mainMenu1');
    }
    if(menu2){//ERP-20463
        menu2.getEl().removeClass('mainMenu2 changedtop');
        menu2.getEl().addClass('mainMenu2');
    }

    if(menu4){//ERP-20463
        menu4.getEl().removeClass('mainMenu4 changedtop');
        menu4.getEl().addClass('mainMenu4');
    }
    
    if(menu5){//ERP-20463
        menu5.getEl().removeClass('mainMenu5 changedtop');
        menu5.getEl().addClass('mainMenu5');
    } 
    if(menu6){//ERP-20463
        menu6.getEl().removeClass('mainMenu6 changedtop');
        menu6.getEl().addClass('mainMenu6');
    }
      
    Wtf.getCmp('viewport').doLayout();
}

Wtf.ux.NavigationPanel = function(config){
    Wtf.ux.NavigationPanel.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.ux.NavigationPanel, Wtf.Panel, {
    selectTab: function(navId){
        var navlinks = Wtf.get('navcontainer');
        navlinks.select('a').removeClass('selected');
        var navbar = Wtf.fly('nav' + navId);
        if (navbar && !navbar.hasClass('selected')) {
            navbar.addClass('selected');
        }
    }
});
Wtf.AccordionPanel = function(config){
    Wtf.apply(this,config);
    Wtf.AccordionPanel.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.AccordionPanel,Wtf.Panel,{
    onRender : function(config){
        Wtf.AccordionPanel.superclass.onRender.call(this,config);
        var intialStatus = this.collapsed ? "plus" : "minus";
        this.addTool({
            id : intialStatus,
            handler : this.togglePanel,
            scope : this
        });
    },

    togglePanel : function(event, toolEl, panel){
        panel.toggleCollapse();
        if(panel.collapsed){
            toolEl.replaceClass("x-tool-plus","x-tool-minus");
        } else {
            toolEl.replaceClass("x-tool-minus","x-tool-plus");
        }
    }
});

Wtf.Action.prototype.setTooltip= function(qtipText) {
    this.callEach('setTooltip', [qtipText]);
}


Wtf.menu.BaseItem.override({
    onRender : function(container, position){
        this.el = Wtf.get(this.el);
        container.dom.appendChild(this.el.dom);
        if(this.tooltip){
            if(typeof this.tooltip == 'object'){
                Wtf.QuickTips.register(Wtf.apply({
                      target: this.el.id
                }, this.tooltip));
            } else {
                this.el.dom[this.tooltipType] = this.tooltip;
            }
        }
    },

    setTooltip: function(qtipText) {
        var itemEl = this.getEl();
        if(!itemEl)return;
        Wtf.QuickTips.register({
            target: itemEl.id,
            text: qtipText
        });
    }
});
Wtf.ux.ContentPanel = function(config){
    Wtf.apply(this, config);
    Wtf.ux.ContentPanel.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.ux.ContentPanel, Wtf.Panel, {
    closable: true,
    autoScroll: true
});
Wtf.ux.MainPanel = function(config){
    Wtf.ux.MainPanel.superclass.constructor.call(this, config);
};
Wtf.form.TextField.override({
    onRender : function(ct,pos){
        Wtf.form.TextField.superclass.onRender.call(this,ct,pos);
        this.el.on("keypress", this.filterMaxLength, this);
    },
    filterMaxLength:function(e){
        var k = e.getKey();
        if(this.getValue().toString().length<this.maxLength){
            if(!Wtf.isIE && (e.isNavKeyPress() || k == e.BACKSPACE || (k == e.DELETE && e.button == -1))){
                return;
            }
            var c = e.getCharCode(), cc = String.fromCharCode(c);
            if(Wtf.isIE && (e.isSpecialKey() || !cc)){
                return;
            }
        } else if(Wtf.isIE){
            e.stopEvent();
        } else if(!(e.isNavKeyPress() || k == e.BACKSPACE || (k == e.DELETE && e.button == -1))) {
            e.stopEvent();
        }
    }
});

Wtf.extend(Wtf.ux.MainPanel, Wtf.TabPanel, {
    loadTab: function(href, id, tabtitle, navAreaid, tabtype, queue, activesubtab,closable){
        var tabid = "tab" + id.substr(3);
        var tab = this.getComponent(tabid);
        if (tab) {
            this.setActiveTab(tab);
        }
        else {
            if(href == "project.html" && Wtf.subscription.cal && !calLoad)
                WtfGlobal.loadScript("../../scripts/minified/calendar.js?v=19");
            var autoload = {
                tabid: tabid,
                url: href,
                scripts: true
            };
            var p = new Wtf.ux.ContentPanel({
                id: tabid,
                title: tabtitle,
                autoLoad: autoload,
                navarea: navAreaid,
                layout: 'fit',
                tabType: tabtype,
                tabTip:(tabtitle='User Administration'?(tabtitle+" :<br>"+WtfGlobal.getLocaleText("acc.rem.1")):""),  //"You can view and assign permissions for various users from here.":"" ),
                closable: closable != null ? closable : true,
                iconCls: getTabIconCls(tabtype)
            });

            if (queue) {
                switch (tabtype) {
                    case Wtf.etype.comm:
                        communityTabs.push(p);
                        break;
                    case Wtf.etype.proj:
                        projectTabs.push(p);
                        break;
                    case Wtf.etype.user:
                        userTabs.push(p);
                        break;
                    case Wtf.etype.lms:
                        courseTabs.push(p);
                        break;
                    case Wtf.etype.acc:
                        accountingTabs.push(p);
                        break;
                }
            }
            this.add(p);
            this.setActiveTab(p);
            if(activesubtab){
                p.activesubtab = activesubtab;
            }
            Wtf.getCmp(tabid).on("hide", function() {
                var expandWin = Wtf.getCmp('Expand');
                if(expandWin != undefined)
                    expandWin.hide();
            }, this);
        }
    },
    loadReportPermTab: function(href, id, tabtitle, navAreaid, tabtype, activesubtab,closable){
        var tabid = "tab" + id.substr(3);
        var tab = this.getComponent(tabid);
        if (tab) {
            this.setActiveTab(tab);
        }
        else {
            if(href == "project.html" && Wtf.subscription.cal && !calLoad)
                WtfGlobal.loadScript("../../scripts/minified/calendar.js?v=19");
            var autoload = {
                tabid: tabid,
                url: href,
                scripts: true
            };
            var p = new Wtf.ux.ContentPanel({
                id: tabid,
                title: tabtitle,
                autoLoad: autoload,
                navarea: navAreaid,
                layout: 'fit',
                tabType: tabtype,
                tabTip:tabtitle=WtfGlobal.getLocaleText("acc.field.Youcanviewandassignpermissionstousersforvariousreportsfromhere"),
                closable: closable != null ? closable : true,
                iconCls: 'pwnd reportListTabIcon'
             });

            this.add(p);
            this.setActiveTab(p);
            if(activesubtab){
                p.activesubtab = activesubtab;
            }
            Wtf.getCmp(tabid).on("hide", function() {
                var expandWin = Wtf.getCmp('Expand');
                if(expandWin != undefined)
                    expandWin.hide();
            }, this);
        }
    },
    loadModuleTab: function(href, id, navAreaid, tabtype, map){
        var tabid = "tab" + id.substr(3);
        var tab = this.getComponent(tabid);
        
        var quotation = map.quotation;
        var winid = map.winid;
        var isCustomer = map.isCustomer;
        var isOrder = map.isOrder;
        var isEdit = map.isEdit;
        var ispurchaseReq = map.ispurchaseReq;
        var copyInv = map.copyInv;
        var record = map.record;
        var PR_IDS = map.PR_IDS;
        var isQuotationFromPR = map.isQuotationFromPR;
        var label = map.label;
        var border = map.border;
        var isVersion = map.isVersion;
        var heplmodeid = map.heplmodeid;
        var moduleid = map.moduleid;
        var tabTip = map.tabTip;
        var title = map.title;
        var closable = map.closable;
        var isViewTemplate = map.isViewTemplate;
        var readOnly = map.readOnly;
        var viewGoodReceipt = map.viewGoodReceipt;
        var iconCls = map.iconCls;
        var modeName = map.modeName;
        
        var selectedModeId = map.selectedModeId;
        
        if (tab) {
            this.setActiveTab(tab);
        } else {
            var autoload = {
                tabid: tabid,
                url: href,
                scripts: true
            };
            
            var moduleConfig = {
                //Config(s) for Vendor Quotation
                title: title,
                closable: closable,
                quotation: quotation,
                isCustomer: isCustomer,
                isOrder: isOrder,
                isEdit: isEdit,
                ispurchaseReq: ispurchaseReq,
                copyInv: copyInv,
                record: record,
                PR_IDS: PR_IDS,
                isQuotationFromPR: isQuotationFromPR,
                label: label,
                border: border,
                tabTip: tabTip,
                heplmodeid: heplmodeid,
                moduleid: moduleid,
                iconCls: iconCls,
                modeName: modeName,
                //Config(s) for View Vendor Quotation
                isViewTemplate:isViewTemplate,
                readOnly:readOnly,
                viewGoodReceipt:viewGoodReceipt,
                //Config(s) for View Customer Quotation
                isVersion:isVersion
            };
            
            var p = new Wtf.ux.ContentPanel(Wtf.applyIf({
                id: tabid,
                autoLoad: autoload,
                navarea: navAreaid,
                layout: 'fit',
                tabType: tabtype
            }, moduleConfig));
            
            this.add(p);
            
            Wtf.getCmp(tabid).on("activate", function(){
                if(Wtf.isIE7) {
                    var northHt=(Wtf.isIE?150:180);
                    var southHt=(Wtf.isIE?210:150);
                    Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                    Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                    Wtf.getCmp(tabid).NorthForm.setHeight(northHt);
                    Wtf.getCmp(tabid).southPanel.setHeight(southHt);
                    Wtf.getCmp(tabid).on("afterlayout", function(panel, lay){
                        if(Wtf.isIE7) {
                            Wtf.getCmp(tabid).Grid.setSize(panel.getInnerWidth() - 18,200);
                        }
                    },this);
                }
                Wtf.getCmp(tabid).doLayout();
            }, this);

            this.setActiveTab(p);
            
            Wtf.getCmp(tabid).on('update',  function(){
                if(isEdit == true){
                    Wtf.getCmp('as').remove(Wtf.getCmp(tabid));
                }
                var selectedModeId=selectedModeId;
                Wtf.getCmp(selectedModeId).loadStore();
            }, this);
        }
    },
    onStripMouseDown : function(e){
        e.preventDefault();
        if(e.button != 0){
            return;
        }
        var t = this.findTargets(e);
        if(t.close){
            if (t.item.fireEvent('beforeclose', t.item) !== false) {
                t.item.fireEvent('close', t.item);
                this.remove(t.item);
            }
            return;
        }
        if(t.item && t.item != this.activeTab){
            this.setActiveTab(t.item);
        }
    }
});
Wtf.uncheckSelAllCheckbox = function(sm){
    /********Select All Checkbox uncheck*****/
   var grid = sm.grid;
   var cell = grid.getView().getHeaderCell(0);
   var hd = Wtf.fly(cell).child('.x-grid3-hd-checker');

   if (sm.getCount() === grid.getStore().getCount()) {
       hd.addClass('x-grid3-hd-checker-on');
   } else {
       hd.removeClass('x-grid3-hd-checker-on');
   }
   /******************************************/
}

Wtf.onCellClick = function(jeid,startDate,endDate){
    var startDateVal=new Date(startDate);
    var endDateVal=new Date(endDate);
    callJournalEntryDetails(jeid,true,null,null,null,null,startDateVal,endDateVal);  
}
Wtf.onCellClickProductDetails = function(pid,isFixedAsset){
    /*
     Opened configured product view from transaction reports(SO,PO etc.).
      */
    if(Wtf.account.companyAccountPref.columnPref.isConfiguredProductView && !isFixedAsset){
        getConfiguredProductView(pid,isFixedAsset);
    }else{
        Wtf.showProductDetails(pid,isFixedAsset);
    }
}
Wtf.showProductDetails = function(pid,isFixedAsset){   // ERP-13247 [SJ]
    
    var recc=[];
    Wtf.Ajax.requestEx({   // ERP-13247 [SJ]
        url: "ACCProduct/getProducts.do",
        params: {
            isFixedAsset:isFixedAsset,
            includeParent:true, // subproduct didn't gets open in view case
            mode:22,
            ids:pid                    
        }
    }, this, function(response,request){
        var recc=eval(response.data);   
        var rec;
        rec=eval({
            data:recc[0]
        });
        callProductProfilerTab(rec,"productimage?loadtime="+new Date().getTime()+"&fname=" + pid + ".png");      
            
    },function() {

        });  
}
Wtf.onCellClickofDocumentNo = function (documentNo, billid, noteid, transactionType, isconsignment, isLeaseFixedAsset) {

    var formrec = {};
    var data = {};
    data.type = transactionType;
    data.billid = billid;
    data.noteid = noteid;
    data.transactionID = documentNo;
    data.isconsignment = isconsignment;
    data.isLeaseFixedAsset = isLeaseFixedAsset;
    data.withoutinventory = Wtf.account.companyAccountPref.withoutinventory;
    formrec.data = data;
    var withoutinventory = Wtf.account.companyAccountPref.withoutinventory;

    if (transactionType == "Debit Note" || transactionType == "Credit Note") {
         billid = formrec.data['noteid'];
    }

    if (transactionType == "Asset Acquired Invoice")
    {
        transactionType = "Purchase Invoice";
    }
    if (transactionType == "Asset Disposal Invoice") {
        
        transactionType = "Sales Invoice";
    }
    if(transactionType == "Lease Invoice"){
        transactionType = "Sales Invoice";
    }

    if (transactionType != '' && transactionType != null && transactionType != undefined) {
        viewTransactionTemplate1(transactionType, formrec, withoutinventory, billid);
    }


}

Wtf.onCellClickCloseLineItem = function(rowid, isCustomer) {  

    Wtf.Ajax.requestEx({
        url:isCustomer ? "ACCSalesOrder/closeLineItem.do" : "ACCPurchaseOrder/closeLineItem.do",
        params: {
            DetailId: rowid

        }
    },this, function(response) {
                
                if(response.success) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], 0);
                } else {
                     WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), response.msg], 2);
                }
            })
}
Wtf.onCellClickCloseDoDetails = function(rowid, isCustomer,billid) {  

    Wtf.Ajax.requestEx({
        url:isCustomer ? "ACCInvoice/closeDoDetail.do" : "",
        params: {
            DetailId: rowid

        }
    },this, function(response) {
                
         if(response.success) {
                Wtf.getCmp("ConsignmentDeliveryOrderListEntry").expandStore.on('load', function() {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], 0);
                }, Wtf.getCmp("ConsignmentDeliveryOrderListEntry").expandStore, {
                    single : true
                });

                Wtf.getCmp("ConsignmentDeliveryOrderListEntry").expandStore.load({params:{bills:billid,isexpenseinv:false,moduleid:51,isConsignment:true,isLeaseFixedAsset:false}});
        } else {
                    
            Wtf.getCmp("ConsignmentDeliveryOrderListEntry").expandStore.on('load', function() {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), response.msg], 2);
            }, Wtf.getCmp("ConsignmentDeliveryOrderListEntry").expandStore, {
                single : true
            });
            Wtf.getCmp("ConsignmentDeliveryOrderListEntry").expandStore.load({params:{bills:billid,isexpenseinv:false,moduleid:51,isConsignment:true,isLeaseFixedAsset:false}});
        }
    })
}

Wtf.onCellClickRejectLineItem = function(rowid, isCustomer) {  
    if(isCustomer){
        this.reasonWindow = new Wtf.Window({
            height: 270,
            width: 360,
            maxLength: 1000,
            title: "Reject Line Level Item",
            bodyStyle: 'padding:5px;background-color:#f1f1f1;',
            autoScroll: true,
            //allowBlank: false,
            layout: 'border',
            items: [{
                    region: 'north',
                    border: false,
                    height: 70,
                    bodyStyle: 'background-color:#ffffff;border-bottom:1px solid #bfbfbf;',
                    html: getTopHtml(WtfGlobal.getLocaleText("acc.invoiceList.rejectItem") +" Item", WtfGlobal.getLocaleText("acc.invoiceList.rejectItem") +" Item" , "../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px") //+ " <b>" + formRecord.data.billno + "</b>"
                }, {
                    region: 'center',
                    border: false,
                    layout: 'form',
                    bodyStyle: 'padding:5px;',
                    items: [this.reasonField = new Wtf.form.TextArea({
                            fieldLabel: WtfGlobal.getLocaleText("acc.cc.6"),
                            width: 200,
                            height: 100,
                            //allowBlank: false,
                            maxLength: 255
                        })]
                }],
            modal: true,
            buttons: [{
                    text: WtfGlobal.getLocaleText("acc.invoiceList.rejectItem"),
                    id: "rejectTransactionsBtn" + this.id,
                    scope: this,
                    handler: function() {
                        Wtf.getCmp("rejectTransactionsBtn" + this.id).disable();
                        Wtf.Ajax.requestEx({
                            url:"ACCSalesOrder/rejectLineItem.do" ,
                            params: {
                                DetailId: rowid,
                                reason: this.reasonField.getValue()
                            }
                        },this, function(response) {                
                            if(response.success) {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], 0);
                                this.reasonWindow.close();
                            } else {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), response.msg], 2);
                            }
                        });
                    }
                }, {
                    text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                    scope: this,
                    handler: function() {
                        this.reasonWindow.close();
                    }
                }]
        });
        this.reasonWindow.show();
    }    
}

//Wtf.uncheckSelAllCheckbox1 = function(sm){
//   var grid = sm.grid;
//   var cell = grid.view.getHeaderCell(1);
//   var hd = Wtf.fly(cell).child('.x-grid3-hd-checker');
//
//   if (sm.getCount() === grid.getStore().getCount()) {
//       hd.addClass('x-grid3-hd-checker-on');
//   } else {
//       hd.removeClass('x-grid3-hd-checker-on');
//   }
//}

function getTabIconCls(tabtype){
    switch (tabtype) {
        case Wtf.etype.home:
            return "pwnd dashboardTabIcon";
            break;
        case Wtf.etype.comm:
            return "pwnd communityTabIcon";
            break;
        case Wtf.etype.proj:
            return "pwnd projectTabIcon";
            break;
        case Wtf.etype.user:
            return "pwnd userTabIcon";
            break;
        case Wtf.etype.docs:
            return "pwnd doctabicon";
            break;
        case Wtf.etype.cal:
            return "pwnd teamcal";
            break;
        case Wtf.etype.forum:
            return "pwnd communitydiscuss";
            break;
        case Wtf.etype.pmessage:
            return "pwnd pmsgicon";
            break;
        case Wtf.etype.pplan:
            return "pwnd projplan";
            break;
        case Wtf.etype.adminpanel:
            return "pwnd admintab";
            break;
        case Wtf.etype.todo:
            return "pwnd todolistpane";
            break;
        case Wtf.etype.search:
            return "pwnd searchtabpane";
            break;
        /*case Wtf.etype.acc:
            return "accountTabIcon";
            break;
        case Wtf.etype.accreports:
            return "accreportsTabIcon";
            break;
        case Wtf.etype.acccustomer:
            return "acccustomerTabIcon";
            break;
        case Wtf.etype.accvendor:
            return "accvendorTabIcon";
            break;
        case Wtf.etype.accemployee:
            return "accemployeeTabIcon";
            break;*/
        case Wtf.etype.contacts:
            return "pwnd contactsTabIcon";
            break;
        case Wtf.etype.crm:
            return "crmTabIcon";
            break;
    }
}
function getButtonIconCls(buttontype){
    switch (buttontype) {
        case Wtf.etype.deskera:
            return "pwnd deskeralogoposition";
            break;
        case Wtf.etype.exportfile:
            return "pwnd export";
            break;
        case Wtf.etype.exportcsv:
            return "pwnd exportcsv";
            break;
        case Wtf.etype.exportpdf:
            return "pwnd exportpdf";
            break;
        case Wtf.etype.save:
            return "pwnd save";
            break;
        case Wtf.etype.clock:
            return "pwnd clock";
            break;
        case Wtf.etype.resetbutton:
            return "pwnd reset";
            break;
       case Wtf.etype.add:
            return "pwnd add";
            break;
       case Wtf.etype.edit:
            return "accountingbase edit";
            break;
      case Wtf.etype.deletebutton:
            return "accountingbase delete";
            break;
       case Wtf.etype.menuadd:
            return "pwnd menu-add";
            break;
       case Wtf.etype.menuedit:
            return "accountingbase menu-edit";
            break;
      case Wtf.etype.menudelete:
            return "accountingbase menu-delete";
            break;
      case Wtf.etype.customer:
            return "accountingbase customer";
            break;
      case Wtf.etype.audittrail:
            return "accountingbase audittrail";
            break;
      case Wtf.etype.permission:
            return "pwnd permission";
            break;
      case Wtf.etype.deletegridrow:
            return "pwnd delete-gridrow";
            break;
      case Wtf.etype.deletegridrow1:  //For Tax Window  SDP-10875
            return "pwnd delete-gridrow1";
            break;  
      case Wtf.etype.productform:
            return "accountingbase productform";
            break;
      case Wtf.etype.buildassemly:
            return "accountingbase buildassemly";
            break;
      case Wtf.etype.inventoryval:
            return "accountingbase inventoryval";
            break;
      case Wtf.etype.salespurchase:
            return "accountingbase salespurchase";
            break;
      case Wtf.etype.cyclecount:
            return "accountingbase cyclecount";
            break;
      case Wtf.etype.addcyclecount:
            return "accountingbase addcyclecount";
            break;
      case Wtf.etype.approvecyclecount:
            return "accountingbase approvecyclecount";
            break;
      case Wtf.etype.countcyclecount:
            return "accountingbase countcyclecount";
            break;
      case Wtf.etype.cyclecountreport:
            return "accountingbase cyclecountreport";
            break;
      case Wtf.etype.reorderreport:
            return "accountingbase reorderreport";
            break;
      case Wtf.etype.ratioreport:
            return "accountingbase ratioreport";
            break;
      case Wtf.etype.addcyclecounttab:
            return "accountingbase addcyclecounttab";
            break;
      case Wtf.etype.approvecyclecounttab:
            return "accountingbase approvecyclecounttab";
            break;
      case Wtf.etype.countcyclecounttab:
            return "accountingbase countcyclecounttab";
            break;
      case Wtf.etype.cyclecountreporttab:
            return "accountingbase cyclecountreporttab";
            break;
    case Wtf.etype.salesbyitem:
        return "accountingbase salesbyitem";
        break;
    case Wtf.etype.salesbyitemsummary:
        return "accountingbase salesbyitemsummary";
        break;
    case Wtf.etype.salesbyitemdetil:
        return "accountingbase salesbyitemdetil";
        break;
    case Wtf.etype.copy:
        return "accountingbase copy";
        break;
     case Wtf.etype.sync:
        return "accountingbase sync";
        break;
     case Wtf.etype.menuclone:
         return "pwnd menu-clone";
         break;
     case Wtf.etype.reportList:
         return "pwnd reportListTabIcon";
         break;
     case Wtf.etype.serialgridrow:
         return "pwnd serialNo-gridrow";
         break;
     case Wtf.etype.inspectiontemplategridrow:
         return "pwnd inspectionTemplate-gridrow";
         break;
      case Wtf.etype.terminate:
            return "accountingbase contractterminate";
            break;    
      case Wtf.etype.renew:
            return "accountingbase contractrenew";
            break;    
      case Wtf.etype.srcontract:
            return "accountingbase salesreturncontract";
            break;    
      case Wtf.etype.salesopen:
            return "accountingbase salesopen";
            break;    
      case Wtf.etype.recycleQuantity:
            return "pwnd recycleQuantity-gridrow";
            break;
      case Wtf.etype.doDetails:
            return "pwnd doDetails-gridrow";
            break;
      case Wtf.etype.packingDetails:
            return "pwnd packingDetails-gridrow";
            break;
      case Wtf.etype.stockvaluationSummary:
            return "accountingbase stockvaluationSummary";
            break;
      case Wtf.etype.activate:
            return "accountingbase activate";
            break;
      case Wtf.etype.deactivate:
            return "accountingbase deactivate";
            break;
        case Wtf.etype.activatedeactivate:
            return "accountingbase activatedeactivate";
            break;
        case Wtf.etype.syncmenuItem:
            return "accountingbase sync-menu-items";
            break;
        case Wtf.etype.inventorysa:
            return "accountingbase inventorysa";
            break;
        case Wtf.etype.inventorysr:
            return "accountingbase inventorysr";
            break;
        case Wtf.etype.inventorysi:
            return "accountingbase inventorysi";
            break;
        case Wtf.etype.inventoryist:
            return "accountingbase inventoryist";
            break;
        case Wtf.etype.inventoryilst:
            return "accountingbase inventoryilst";
            break;
        case Wtf.etype.inventoryqa:
            return "accountingbase inventoryqa";
            break;
        case Wtf.etype.inventorysrep:
            return "accountingbase inventorysrep";
            break;
        case Wtf.etype.inventorysarbw:
            return "accountingbase inventorysarbw";
            break;
        case Wtf.etype.inventorybst:
            return "accountingbase inventorybst";
            break;
        case Wtf.etype.inventorydst:
            return "accountingbase inventorydst";
            break;
        case Wtf.etype.inventorydbst:
            return "accountingbase inventorydbst";
            break;
        case Wtf.etype.inventorymior:
            return "accountingbase inventorymior";
            break;
        case Wtf.etype.inventorysmr:
            return "accountingbase inventorysmr";
            break;
        case Wtf.etype.inventoryistd:
            return "accountingbase inventoryistd";
            break;
        case Wtf.etype.inventoryisar:
            return "accountingbase inventoryisar";
            break;
        case Wtf.etype.inventoryptr:
            return "accountingbase inventoryptr";
            break;
        case Wtf.etype.inventoryiltd:
            return "accountingbase inventoryiltd";
            break;
        case Wtf.etype.inventoryrlr:
            return "accountingbase inventoryrlr";
            break;
        case Wtf.etype.wastageQuantity:
            return "pwnd wastageQuantity-gridrow";
            break;
        case Wtf.etype.addtdsgrid:
            return "pwnd tdsCalc-gridrow";
         break;     
        case Wtf.etype.tdswinexpencegrid:
            return "pwnd tdsCalc-ecpancegridrow";
         break;     
        case Wtf.etype.tdswinproductgrid:
            return "pwnd tdsCalc-productInvoicegridrow";
         break;     
        case Wtf.etype.termCalcWindow:
            return "pwnd termCalc-gridrow";
            break;
        case Wtf.etype.jobwork:
            return "accountingbase jobwork";
            break;
        case Wtf.etype.exciseDetailWindow:
            return "pwnd exciseDealer-gridrow";
            break;
        case Wtf.etype.supplierDetailWindow:
            return "pwnd exciseSupplier-gridrow";
            break;
        case Wtf.etype.mrpcosting:
            return "pwnd costingreporticon";
            break;
        case Wtf.etype.jobworkdetails:
            return "accountingbase inventoryistd";
            break;
        case Wtf.etype.discountdetails:
            return "pwnd discountDetails-gridrow";
            break;
            
    }
}


Wtf.ProjSubscrib = function(config) {
    Wtf.apply(this, config);
    this.subRec = Wtf.data.Record.create([
        {name: "subid"},
        {name: "billdate"},
        {name: 'frequency'},
        {name: 'numproj'},
        {name: 'rate'},
        {name: 'flag'}
    ]);
    this.subscrStore = new Wtf.data.Store({
        url :'admin.jsp',
        baseParams :{
            action:3,
            mode:1
        },
        id: "subscrdstore",
        autoLoad :true,
        reader: new Wtf.data.KwlJsonReader({
            root: 'data'
        }, this.subRec)
    });
    this.subscrStore.on("load",function(){
        Wtf.getCmp('subscribDataView').refresh();
    },this);
    Wtf.ProjSubscrib.superclass.constructor.call(this,{
        title : WtfGlobal.getLocaleText("acc.field.Subscribe"),
        modal : true,
        iconCls : 'iconwin',
        width : 530,
        height: 485,
        resizable :false,
        buttonAlign : 'right',
        bodyStyle : 'background:#f1f1f1;',
        buttons :[{
            text: WtfGlobal.getLocaleText("acc.common.close"),
            scope: this,
            handler: function(){
                this.close();
            }
        }],
        layout : 'border',
        items :[{
            region : 'north',
            height : 75,
            border : false,
            bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(WtfGlobal.getLocaleText("acc.field.Subscribe"), WtfGlobal.getLocaleText("acc.field.Clickondatetoseeinvoiceandpaymentreceipts"))
        },{
            region : 'center',
            border : false,
            autoScroll:true,
            bodyStyle : 'background:#f1f1f1;',
            layout : 'border',
            items :[{
                    region : 'north',
                    height : 40,
                    border : false,
                    autoScroll:false,
                    labelWidth: 70,
                    items:[{
                        xtype:"panel",
                        autoScroll:false,
                        autoDestroy: true,
                        border: false,
                        html: "<div class='listpanelcontent' style ='font-weight:bold;'><span class='holidaySpan' style = 'width:150px;'>"+WtfGlobal.getLocaleText("acc.field.Subscribedon")+"</span>" +
                            "<span class='holidaySpan' style = 'width:90px;'>Project Count</span>" +
                            "<span class='holidaySpan' style = 'width:70px;'>Rate ($/Month)</span>" +
                            "<span class='holidaySpan' style = 'width:90px;'>Billing Cycle (Months)</span>" +
                            "<span class='holidaySpan' style = 'width:50px;'>Cancel</span></div>"
                    }]
                },{
                    region:'center',
                    border:false,
                    bodyStyle : 'border-bottom:1px solid #bfbfbf;',
                    autoScroll:true,
                    items:[{
                        xtype: 'dataview',
                        id: 'subscribDataView',
                        autoScroll:true,
                        itemSelector: "companysubscrib",
                        tpl: new Wtf.XTemplate('<div class="listpanelcontent"><tpl for=".">{[this.f(values)]}</tpl></div>', {
                            f: function(val) {
                                var htmlstr ="";
                                if(val.subid!='0')
                                    htmlstr = "<div><a href='#' class='mailTo' onclick='subscribeInvoice(\""+val.subid+"\")'><span class='holidaySpan' style = 'width:150px;'>" + Date.parseDate(val.billdate,'Y-m-d h:i:s.0' ).format(WtfGlobal.getDateFormat()) + "</span></a>";
                                else
                                    htmlstr = "<div><span class='holidaySpan' style = 'width:150px;'>" + Date.parseDate(val.billdate,'Y-m-d h:i:s.0' ).format(WtfGlobal.getDateFormat()) + "</span>";
                                htmlstr += "<span class='holidaySpan' style = 'width:90px;'>"+val.numproj+"</span>"+
                                    "<span class='holidaySpan' style = 'width:70px;'>"+val.rate+ "</span>"+
                                    "<span class='holidaySpan' style = 'width:90px;'>"+val.frequency+ "</span>"
                                if(val.flag == '1')
                                    htmlstr +="<img src='../../images/Delete.png' class='holidayDelete' onclick=\"cancelSubsci(this,'" + this.scope.id+"')\" id='del_"+val.subid+"' title='Cancel Subscription'>";
                                htmlstr +="<div><span class='holidayDiv' style = 'width:450px !important;'></span></div></div>";
                                return htmlstr;
                            },
                            scope: this
                        }),
                        store: this.subscrStore
                    }]
                }, {
                    region : 'south',
                    autoScroll:false,
                    height : 110,
                    border : false,
                    items:[{
                            border : false,
                            html :'<div style="color:#666;margin:8px;">Select number of projects to subscribe</div>'
                        },
                        new Wtf.Panel({
                            border : false,
                            html : this.formcontent
                    })]
            }]
        }]
    });
}

Wtf.extend(Wtf.ProjSubscrib, Wtf.Window, {
    cancelSubsci : function(subid) {
        Wtf.Ajax.requestEx({
            url: 'admin.jsp',
            params: {
                action : 3,
                mode : 2,
                subid :subid
            }
            }, this,
            function(result, req) {
                var res = eval("(" + result + ")");
                if(res.success) {
                    this.subscrStore.load();
                } else {
                    msgBoxShow([0,WtfGlobal.getLocaleText("acc.field.Erroroccuredatserverside")], 1);
                }
            }
        );
    }
});

/*function toggleAccordion(tabtype){
    switch (tabtype) {
        case Wtf.etype.docs:
          //  Wtf.getCmp("docpanel").expand();
            break;
        case Wtf.etype.cal:
           // Wtf.getCmp("calpanel").expand();
            break;
        default:
         //   Wtf.getCmp("pmessage").expand();
            break;
    }
}*/
//    function chkFirstRun(){
//          return getCookie("lastlogin") == "1990-01-01 00:00:00.0";
//    }
Wtf.onReady(function(){
    Wtf.QuickTips.init();
    Wtf.apply(Wtf.QuickTips.getQuickTip(), {
        dismissDelay:0
    });
    Wtf.form.Field.prototype.msgTarget = 'side';
    Wtf.grid.RowNumberer.prototype.fixed = false;
    Wtf.KWLRowNumberer.prototype.fixed = false;
    validateServerSession();

//    var viewport = new Wtf.Viewport({
//        layout: 'border',
//        items: [new Wtf.BoxComponent({
//            region: 'north',
//            el: "header"
//        }), mainPanel]
//
//    });


//    var searchpanel = new Wtf.KWLSearchBar({
//        id: 'searchTopPanel',
//        renderTo: "searchBar",
//        autoWidth: true,
//        layout: 'table',
//        border: false,
////        document: Wtf.subscription.docs,
//        bodyStyle: 'float:right;'
//    });

});
function hidePendingApprovalLink(){                 // Function to hide "View Pending Approvals" link on Dashboard.
    Wtf.get('approverlinkondashboard').remove();
    if(Wtf.get('dashhelpPA')) {
        Wtf.get('dashhelpPA').remove();
    }
}
function noThanks(){
    Wtf.get('dashhelp').remove();
}
function takeTour(){
    if(document.getElementById('titlehelp') == null) {
        showHelp(1);
    }
}

function closeWelcomeMsg() {
    if(Wtf.get('doNotShowCheckBoxId').dom.checked) {
        Wtf.Ajax.requestEx({
            url: 'ACCDashboard/saveUserPreferencesOptions.do',
            params: {
                showNewToDeskeraWelcomeMsg: !(Wtf.get('doNotShowCheckBoxId').dom.checked)
            }
        }, this);

        Wtf.get('NewToDeskeraHelp').remove();
        
        if(Wtf.get('ViewPendingApprovalHelp')) {
            Wtf.get('dashhelp').dom.id = 'dashhelpPA';
        }
        
        if(Wtf.get('NewToDeskeraHelp') == undefined && Wtf.get('ViewPendingApprovalHelp') == undefined) {
            if(Wtf.get('dashhelp')) {
               Wtf.get('dashhelp').remove();
            }
            if(Wtf.get('dashhelpPA')) {
               Wtf.get('dashhelpPA').remove();
            }
        }
    } else {
        Wtf.get('NewToDeskeraHelp').remove();
        
        if(Wtf.get('ViewPendingApprovalHelp')) {
            Wtf.get('dashhelp').dom.id = 'dashhelpPA';
        }
        
        if(Wtf.get('NewToDeskeraHelp') == undefined && Wtf.get('ViewPendingApprovalHelp') == undefined) {
            if(Wtf.get('dashhelp')) {
               Wtf.get('dashhelp').remove();
            }
            if(Wtf.get('dashhelpPA')) {
               Wtf.get('dashhelpPA').remove();
            }
        }
    }
}

function closePAMsg() {
    if(Wtf.get('doNotShowCheckBoxPAId').dom.checked) {
        Wtf.Ajax.requestEx({
            url: 'ACCDashboard/saveUserPreferencesOptions.do',
            params: {
                showPendingApprovalWelcomeMsg: !(Wtf.get('doNotShowCheckBoxPAId').dom.checked)
            }
        }, this);

        Wtf.get('ViewPendingApprovalHelp').remove();
        
        if(Wtf.get('NewToDeskeraHelp') == undefined && Wtf.get('ViewPendingApprovalHelp') == undefined) {
            if(Wtf.get('dashhelp')) {
               Wtf.get('dashhelp').remove();
            }
            if(Wtf.get('dashhelpPA')) {
               Wtf.get('dashhelpPA').remove();
            }
        }
    } else {
        Wtf.get('ViewPendingApprovalHelp').remove();
        
        if(Wtf.get('NewToDeskeraHelp') == undefined && Wtf.get('ViewPendingApprovalHelp') == undefined) {
            if(Wtf.get('dashhelp')) {
               Wtf.get('dashhelp').remove();
            }
            if(Wtf.get('dashhelpPA')) {
               Wtf.get('dashhelpPA').remove();
            }
        }
    }
}

function getHelpButton(obj,id){
   obj.help= new Wtf.Toolbar.Button({
            scope:this,
            iconCls:'helpButton',
            tooltip:{text:WtfGlobal.getLocaleText("acc.rem.2")},  //{text:'Get started by clicking here!'},
            mode:id,
            handler:function(a){
                showHelp(id, obj)
            }
        });
 return obj.help;
}
function showHelp(compID, obj) {
    Wtf.Ajax.requestEx({
        url : "EditHelp/getComponents.do",
        //url: Wtf.req.base + 'helpcontent.jsp',
        params: {
            mod : compID
        }
    }, obj,
    function(res, req){
        var len=res.data.length;
        var dat=[];
        var count=0;
        for(i=0;i<len;i++) {
            var compid1 = (this.appendID?res.data[i]['compid']+this.id:res.data[i]['compid']);
            if(Wtf.get(compid1)!=null) {
                res.data[i]['compid'] = compid1,
                dat[count] = res.data[i];
                count++;
            } else if (res.data[i]['compid'].trim()==''){
                dat[count] = res.data[i];
                count++;
            }
        }
        _helpContent = dat;
        var we = new Wtf.taskDetail();
        we.welcomeHelp();
    }, signOut);

}
function setFullname(uName){
    var _uElem = document.getElementById('whoami');
    _uElem.innerText = uName; //mofo IE
    _uElem.textContent = uName;
}

function button1menu() {

                this.button1.menu.removeAll();
                if(document.getElementById('headTimezone') && document.getElementById('headTimezone').style.display=="block" && menu1){//ERP-20463
                    menu1.getEl().addClass('mainMenu1 changedtop');
                }
		if(!WtfGlobal.EnableDisable(Wtf.UPerm.useradmin, Wtf.Perm.useradmin.view))
                        this.button1.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(1); loadAdminPage(1);" id="userAdministrationLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.userAdministration")+'">'+WtfGlobal.getLocaleText("acc.dashboard.userAdministration")+'</a></div>');
                if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.view))
                    this.button1.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(1); callMasterConfiguration();" id="masterConfigurationLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.masterConfiguration")+'">'+WtfGlobal.getLocaleText("acc.dashboard.masterConfiguration")+'</a></div>');
		if(!WtfGlobal.EnableDisable(Wtf.UPerm.audittrail, Wtf.Perm.audittrail.view))
                    this.button1.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(1); callAuditTrail();" id="auditTrailLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.auditTrail")+'">'+WtfGlobal.getLocaleText("acc.dashboard.auditTrail")+'</a></div>');
		if(!WtfGlobal.EnableDisable(Wtf.UPerm.importlog, Wtf.Perm.importlog.view))
                    this.button1.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(1); callImportFilesLog();" id="importLogLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.importLog")+'">'+WtfGlobal.getLocaleText("acc.dashboard.importLog")+'</a></div>');

                if (!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.viewclayout)) {
                        this.button1.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(1); callCustomLayoutGrid();" id="customLayoutReportLink" wtf:qtip=' + WtfGlobal.getLocaleText("acc.field.Createandviewcustomlayouts") + '">' + WtfGlobal.getLocaleText("acc.field.CustomLayouts") + '</a></div>');
                }
                if (!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.viewcdesigner)) {
                        this.button1.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(1); callCustomDesigner();" id="customDesigner" wtf:qtip=' + WtfGlobal.getLocaleText("acc.field.DesignCustomPDFdesignlayout") + '">' + WtfGlobal.getLocaleText("acc.customdesignTT") + '</a></div>');
                }
                if (!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.viewdatarange)) {
                        this.button1.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(1); showActiveDateRange();" id="activeDateRange" wtf:qtip=' + WtfGlobal.getLocaleText("acc.field.ActiveDateRange") + '">' + WtfGlobal.getLocaleText("acc.field.ActiveDateRange") + '</a></div>');
                 }
                this.button1.showMenu();
}

function button2menu() {
        this.button2.menu.removeAll(); 
        
         if (Wtf.UserReporRole.URole.roleid == Wtf.ADMIN_ROLE_ID) {
            this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); getAccountingandtaxperoid();" id="customReportLink" wtf:qtip="' + WtfGlobal.getLocaleText("acc.accperiodsettingslinkttip.link") + '">' + WtfGlobal.getLocaleText("acc.accountingperiodtab.northpanel.accountingperiod.title")+'</a></div>');
            this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); getAccountingandtaxperiod();" id="customReportLink" wtf:qtip="' + WtfGlobal.getLocaleText("acc.customReport.link") + '">' + WtfGlobal.getLocaleText("acc.common.taxperiod")+'</a></div>');
        }
        if(document.getElementById('headTimezone') && document.getElementById('headTimezone').style.display=="block" && menu2){//ERP-20463
            menu2.getEl().addClass('mainMenu2 changedtop');
        }
        this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); loadReportPerm();" id="_greport" wtf:qtip='+WtfGlobal.getLocaleText("acc.dashboard.userPerm")+'>'+WtfGlobal.getLocaleText("acc.field.ReportList")+'</a></div>');
        this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callWidgetTab();" id="_wreport" wtf:qtip='+WtfGlobal.getLocaleText("acc.dashboard.widgets")+'>'+WtfGlobal.getLocaleText("acc.dashboard.widgets")+'</a></div>');
        this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); loadCreateCustomReportPerm();" id="_createcustomreport" wtf:qtip="'+WtfGlobal.getLocaleText("acc.field.CreateCustomReport.link")+'">'+WtfGlobal.getLocaleText("acc.field.CreateCustomReportList")+'</a>'+WtfGlobal.getLocaleText("acc.field.CreateCustomReportBetaText")+'</div>');
   
//	this.button2.menu.removeAll(); 
//	 if(!WtfGlobal.EnableDisable(Wtf.UPerm.costcenter, Wtf.Perm.costcenter.viewccs) || !WtfGlobal.EnableDisable(Wtf.UPerm.costcenter, Wtf.Perm.costcenter.viewccd) || !WtfGlobal.EnableDisable(Wtf.UPerm.costcenter, Wtf.Perm.costcenter.viewcct))
//		this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callCostCenterReport();" id="costCenterReportLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.costCenterReport")+'">'+WtfGlobal.getLocaleText("acc.dashboard.costCenterReport")+'</a></div>');
//         if(!WtfGlobal.EnableDisable(Wtf.UPerm.cashflow, Wtf.Perm.cashflow.view))
//		this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callCashFlowStatement();" id="callCashFlowStatementLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.cashFlowStatement")+'">'+WtfGlobal.getLocaleText("acc.dashboard.cashFlowWorkSheet")+'</a></div>');
//        if(!WtfGlobal.EnableDisable(Wtf.UPerm.salesbyitem, Wtf.Perm.salesbyitem.view))
//		this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callSaleByItem();" id="saleByItemLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.salesByItem")+'">'+WtfGlobal.getLocaleText("acc.dashboard.salesByItem")+'</a></div>');
//	if(!WtfGlobal.EnableDisable(Wtf.UPerm.taxreport, Wtf.Perm.taxreport.viewst) || !WtfGlobal.EnableDisable(Wtf.UPerm.taxreport, Wtf.Perm.taxreport.viewpt))
//		this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callTaxReport();" id="taxReportLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.taxReport")+'">'+WtfGlobal.getLocaleText("acc.dashboard.taxReport")+'</a></div>');
//	if(Wtf.account.companyAccountPref.countryid == '203')
//		this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); GSTReportTab();" id="gstTaxWindowLink" wtf:qtip="'+"View GST Report"+'">'+"View GST Report"+'</a></div>');
//	if(Wtf.account.companyAccountPref.countryid == '203')
//        this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callIAFfileWindow();" id="IAFfileWindowLink" wtf:qtip="'+"Export IAF Text File"+'">'+"Export IAF File"+'</a></div>');
//	//if(Wtf.account.companyAccountPref.countryid == '203')
//		this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callCustomerVendorLedgerReport();" id="customervendorLedger" wtf:qtip="'+"View Statement of Accounts for Customer and Vendor"+'">'+"Statement of Accounts"+'</a></div>');
//		this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callCustomerVendorLedgerAnalysisReport();" id="customervendoranalysysLedger" wtf:qtip="'+"View Customer and Vendor Analysis Report"+'">'+"Customer and Vendor Analysis Report"+'</a></div>');
//                this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); topCustomersByProducts();" wtf:qtip="'+"View Top customers and Dormant customers By Products."+'">'+"Top and Dormant Customers By Products"+'</a></div>');
//                this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); topVendorsByProducts();" wtf:qtip="'+"View Top vendors and Dormant vendors By Products."+'">'+"Top and Dormant Vendors By Products"+'</a></div>');
//                this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); topProductsByCustomers();" wtf:qtip="'+"View Top products and Dormant products By Customers."+'">'+"Top and Dormant Products By Customers"+'</a></div>');                                
//                this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); salesCommissionStmt();" wtf:qtip="'+"View Sales Persons Commission Statements."+'">'+"Sales Commission Statement"+'</a></div>');
//               // this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); salesCommissionStmt();" wtf:qtip="'+"View Sales Persons Commission Statements."+'">'+"Sales Commission Statement"+'</a></div>');
//                
////        if(!Wtf.account.companyAccountPref.withoutinventory)
////            this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callDeliveryOrder(false, null);" id="deliveryOrderReport" wtf:qtip="'+"View Delivery Order Report"+'">'+"Delivery Order"+'</a></div>');
////	if(Wtf.account.companyAccountPref.countryid == '203')
//            this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callGSTForm5fileWindow();" id="GSTForm5WindowLink" wtf:qtip="'+"Export GST Form 5 PDF File"+'">'+"Export GST Form 5"+'</a></div>');
//	//this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callDeliveryOrderList();" id="deliveryorder" wtf:qtip="'+"View Delivery Order Report"+'">'+"Delivery Order Report"+'</a></div>');
//        //if(!Wtf.account.companyAccountPref.withoutinventory) {
 //           this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); getCustRevenueView();" id="saleByCustLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.salesByItem")+'">'+'Customer Revenue'+'</a></div>');//added to report list
//            this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); getSalesByCustTabView();" id="saleByCustLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.salesByItem")+'">'+'Sales By Customer'+'</a></div>');
//            this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); getSalesByProdTabView();" id="saleByProdLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.salesByItem")+'">'+'Sales By Product'+'</a></div>');
//            this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); getSalesBySalesPersonTabView();" id="saleByProdLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.salesByItem")+'">'+'Sales by Sales Person'+'</a></div>');
////            if (Wtf.account.companyAccountPref.withinvupdate) {
////                   this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callPurchaseReturn();" id="purchaseReturnReportLink" wtf:qtip="Generate Purchase Return related to Vendors.">' + 'Purchase Return' + '</a></div>');
////                   this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callPurchaseReturnList();" id="purchaseReturnReportLink" wtf:qtip="View complete details of Purchase Return from your Vendors.">' + 'Purchase Return Report' + '</a></div>');
////            }
            this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callCustomReportGrid();" id="customReportLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.customReport.link")+'">'+WtfGlobal.getLocaleText("acc.field.CustomReports")+'</a></div>');
//            this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callInterCompanyLedger();" id="interCompanyLedgerLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.interCompanyLedgerReport")+'">'+WtfGlobal.getLocaleText("acc.dashboard.interCompanyLedgerReport")+'</a></div>');
//                  this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callAccountRevaluationWindow();" id="customReportLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.reval.link")+'">'+WtfGlobal.getLocaleText("acc.field.AccountsRe-evaluation")+'</a></div>');//added in misselaneous
            if(Wtf.SetExchageRate_For_MaleshianCompany){
                this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callsetExchangeRateWindow();" id="customReportLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.exchangerate.link")+'">'+WtfGlobal.getLocaleText("acc.field.SetExchangeRate")+'</a></div>');
            }
            this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callProductExportDetails();" id="ProductExportDetailsrep" wtf:qtip="'+WtfGlobal.getLocaleText("acc.field.exportDetailReport.ttip")+'">'+WtfGlobal.getLocaleText("acc.field.exportDetailReport")+'</a></div>');
            
//            this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callCommonExportDetails();" id="callCommonExportDetailsrep" wtf:qtip="'+WtfGlobal.getLocaleText("acc.field.exportDetailReport.ttip")+'">'+"Exports"+'</a></div>');
                        
//            if(!WtfGlobal.EnableDisable(Wtf.UPerm.qapermission, Wtf.Perm.qapermission.viewqa) && Wtf.account.companyAccountPref.isQaApprovalFlow){
//                this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callQAApprovalReport();" id="qaApprovalItemsLink" wtf:qtip="'+"QA Approval Purchase Order items"+'">'+"QA Approval PO items"+'</a></div>');            
//            }
//           this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); getReorderAnalysisMainTabView();" id="reorderAnalysisReportLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.field.ClickheretoviewdetailsofReorderAnalysisReport")+'">'+WtfGlobal.getLocaleText("acc.field.ReorderAnalysisReport")+'</a></div>');added to report list
//            if(Wtf.account.companyAccountPref.countryid=='137')
//                this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callMalasianGSTWindow();" id="callMalasianGSTWindow" wtf:qtip="Open GST Tap Return File">GST Tap Return File</a></div>');
//                this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callSalesReturn(false,null,null,true);" id="CreateCreditNoteSalesReturn" wtf:qtip="Create Credit Note Sales Return">Create Credit Note Sales Return</a></div>');
//                this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callPurchaseReturn(false,null,null,true);" id="CreateCreditNoteSalesReturn1" wtf:qtip="Create Debit Note Purchase Return">Create Debit Note Purchase Return</a></div>');
                
//                this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callDemoTransactionsImportWin();" id="callDemoTransactionsImportWin" wtf:qtip='+WtfGlobal.getLocaleText("acc.field.ImportJustCommodityTransactions.ttip")+'>'+WtfGlobal.getLocaleText("acc.field.ImportJustCommodityTransactions")+'</a></div>');
                
//                this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); getTaxableDeliveryOrdersPanel();" id="getTaxableDeliveryOrdersPanel1" wtf:qtip="Taxable Deliver Orders">Taxable Delivery Orders</a></div>');
//                this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callTaxAdjustmentWindow(true);" id="BadDeabtInaavoicesaaaa1" wtf:qtip="Output Tax Adjustment Grid">Output Tax Adjustment Grid</a></div>');
//                this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callTaxAdjustmentWindow(false);" id="BsadDeabtInaavoicesaaaa1" wtf:qtip="Input Tax Adjustment Grid">Input Tax Adjustment Grid</a></div>');
            
//            this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); showStockStatusReportTab();" wtf:qtip="'+WtfGlobal.getLocaleText("acc.stockStatusReport")+'">'+WtfGlobal.getLocaleText("acc.stockStatusReport")+'</a></div>');      
//            this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callForeignCurrencyExposure();" id="fxexposureLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.fxexposure.link")+'">'+'Foreign Currency Exposure Report'+'</a></div>');
//            this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); monthlyRevenue();" wtf:qtip="'+"View Monthly Revenue."+'">'+"Monthly Revenue"+'</a></div>');
//            
//            this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); monthlySalesReport();" wtf:qtip="'+"View Monthly Sales Report."+'">'+"Monthly Sales Report"+'</a></div>');                
//            this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); monthlyTradingProfitLoss();" wtf:qtip="'+"View Monthly Trading and Profit/Loss Report."+'">'+"Monthly Trading and Profit/Loss Report"+'</a></div>');
//            
//            this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); FinanceDetailsReport();" wtf:qtip="'+"View Finance Details Report."+'">'+"Finance Details Report"+'</a></div>');
//            //this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); getCurrencyExposure();" id="currencyExpoLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.salesByItem")+'">'+'Currency Exposure Report'+'</a></div>');
//            
//            //this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callCustomerCreditException();" id="creditexceedlimit" wtf:qtip="Customer Credit Limit Exception Report">'+'Customer Credit Limit Exception Report'+'</a></div>');
//        //}
        if(false){//if(Wtf.isPMSync){
            this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); showProjectStatusReportTab();" wtf:qtip="'+WtfGlobal.getLocaleText("acc.projectStatusReport")+'">'+WtfGlobal.getLocaleText("acc.projectStatusReport")+'</a></div>');      
        }
//        this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callSystemDataBackup();" id="systemDataBackupLink" wtf:qtip="System Data Backup">System Data Backup</a></div>');
     //   if(Wtf.account.companyAccountPref.activateIBG)
   //         this.button2.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(2); callIBGEntryReport();" id="ibgentryreportlink" wtf:qtip="IBG Entry Report">IBG Entry Report</a></div>');
        this.button2.showMenu();
}

//function button3menu() {
//
//	this.button3.menu.removeAll();
//	if(!WtfGlobal.EnableDisable(Wtf.UPerm.accpref, Wtf.Perm.accpref.view))
//		this.button3.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(3); callAccountPref();" id="accountPrefreanceLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.accountPreferences")+'">'+WtfGlobal.getLocaleText("acc.dashboard.accountPreferences")+'</a></div>');
////	if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedasset, Wtf.Perm.fixedasset.view))
////		this.button3.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(3); callFixedAsset();" id="fixedAssetDepereciationLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.fixedAssetDepreciation")+'">'+WtfGlobal.getLocaleText("acc.dashboard.fixedAssetDepreciation")+'</a></div>');
//	if(!WtfGlobal.EnableDisable(Wtf.UPerm.currencyexchange, Wtf.Perm.currencyexchange.view))
//		this.button3.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(3); callCurrencyExchangeWindow();" id="currencyExchangeLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.currencyExchange")+'">'+WtfGlobal.getLocaleText("acc.dashboard.currencyExchange")+'</a></div>');
//	if(!WtfGlobal.EnableDisable(Wtf.UPerm.bankreconciliation, Wtf.Perm.bankreconciliation.view))
//		this.button3.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(3); callReconciliationWindow();" id="bankReconciliationLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.bankReconciliation")+'">'+WtfGlobal.getLocaleText("acc.dashboard.bankReconciliation")+'</a></div>');
//		
//                if(Wtf.show_just_commodity_software_import_link)
//                    this.button3.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(3); callDemoTransactionsImportWin();" id="callDemoTransactionsImportWinLink" wtf:qtip="Import Just Commodity Transactions">Import Just Commodity Transactions</a></div>');
//
//		//this.button3.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(3); callGeneralLedger();" wtf:qtip="'+"View General Ledger Report."+'">'+"General Ledger Report"+'</a></div>');
//	this.button3.showMenu();
//}

function button4menu(about) {

	this.button4.menu.removeAll();
        if(document.getElementById('headTimezone') && document.getElementById('headTimezone').style.display=="block" && menu4){//ERP-20463
            menu4.getEl().addClass('mainMenu4 changedtop');
        }
	for (var i =0; i < about.length; i++)
		this.button4.menu.addText(about[i]);
	this.button4.showMenu();
}

function button5menu(about) {

	this.button5.menu.removeAll();
        if(document.getElementById('headTimezone') && document.getElementById('headTimezone').style.display=="block" && menu5){//ERP-20463
            menu5.getEl().addClass('mainMenu5 changedtop');
        }
	for (var i =0; i < about.length; i++)
		this.button5.menu.addText(about[i]);
	this.button5.showMenu();
}

function button6menu() {

        this.button6.menu.removeAll();
        if(document.getElementById('headTimezone') && document.getElementById('headTimezone').style.display=="block" && menu6){//ERP-20463
            menu6.getEl().addClass('mainMenu6 changedtop');
        }
        this.button6.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(6); callMapAccountsMultiCompany();" id="mapAccountsMultiCompany" wtf:qtip="'+WtfGlobal.getLocaleText("acc.MapAccountsforMultiCompany")+'">'+WtfGlobal.getLocaleText("acc.MapAccounts")+'</a></div>');       
        /*Financial Statements*/
        this.button6.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(6); callJournalEntryDetails(null,null,true);" id="multiJournalEntryLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.consolidateJournalEntryLink")+'">'+WtfGlobal.getLocaleText("acc.dashboard.consolidateJournalEntryLink")+'</a></div>');
        this.button6.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(6); callLedger(null,null,null,true);" id="multiLedgerLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.consolidateLedgerReport")+'">'+WtfGlobal.getLocaleText("acc.dashboard.consolidateLedgerReport")+'</a></div>');
        this.button6.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(6); TrialBalance(true);" id="multiTrailBalanceLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.consolidateTrialBalanceLink")+'">'+WtfGlobal.getLocaleText("acc.dashboard.consolidateTrialBalanceLink")+'</a></div>');
        this.button6.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(6); NewTradingProfitLoss(true);" id="multiTradingPLLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.consolidateTradingPLLink")+'">'+WtfGlobal.getLocaleText("acc.dashboard.consolidateTradingPLLink")+'</a></div>');
        this.button6.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(6); periodViewBalanceSheet(true);" id="multiBalanceSheetLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.consolidateBalanceSheetLink")+'">'+WtfGlobal.getLocaleText("acc.dashboard.consolidateBalanceSheetLink")+'</a></div>');
        this.button6.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(6); callCashFlowStatement(true);" id="multiCashFlowReportLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.consolidateCashFlowReport")+'">'+WtfGlobal.getLocaleText("acc.dashboard.consolidateCashFlowReport")+'</a></div>');
        this.button6.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(6); callCostCenterReport(true);" id="multiCostCenterReportLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.consolidateCostCenterReport")+'">'+WtfGlobal.getLocaleText("acc.dashboard.consolidateCostCenterReport")+'</a></div>');
        /*Customer Reports*/
        this.button6.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(6); callQuotationList(true);" id="multiQuotationReportLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.consolidateQuotationReport")+'">'+WtfGlobal.getLocaleText("acc.dashboard.consolidateQuotationReport")+'</a></div>');
        this.button6.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(6); callSalesOrderList(true);" id="multiSalesOrderReportLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.consolidateSalesOrderReport")+'">'+WtfGlobal.getLocaleText("acc.dashboard.consolidateSalesOrderReport")+'</a></div>');
        this.button6.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(6); callInvoiceList(null,null,null,true);" id="multiCustInvCashSalesReportLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.consolidateCustInvoiceAndCashSalesReportLink")+'">'+WtfGlobal.getLocaleText("acc.dashboard.consolidateCustInvoiceAndCashSalesReportLink")+'</a></div>');
        this.button6.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(6); callDeliveryOrderList(true);" id="multiDeliveryOrderReportLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.consolidateDeliveryOrderReport")+'">'+WtfGlobal.getLocaleText("acc.dashboard.consolidateDeliveryOrderReport")+'</a></div>');
        this.button6.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(6); callCreditNoteDetails(null,null,null,true);" id="multiCreditNoteReportLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.consolidateCreditNoteReport")+'">'+WtfGlobal.getLocaleText("acc.dashboard.consolidateCreditNoteReport")+'</a></div>');        
        /*Vendor Reports*/
        this.button6.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(6); callPurchaseOrderList(true);" id="multiPurchaseOrderReportLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.consolidatePurchaseOrderReport")+'">'+WtfGlobal.getLocaleText("acc.dashboard.consolidatePurchaseOrderReport")+'</a></div>');        
        this.button6.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(6); callGoodsReceiptList(null,null,true);" id="multiVendInvCashPurchaseReportLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.consolidateVendInvAndCashPurchaseReportLink")+'">'+WtfGlobal.getLocaleText("acc.dashboard.consolidateVendInvAndCashPurchaseReportLink")+'</a></div>');
        this.button6.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(6); callGoodsReceiptOrderList(true);" id="multiGoodsReceiptOrderReportLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.consolidateGoodsReceiptOrderReport")+'">'+WtfGlobal.getLocaleText("acc.dashboard.consolidateGoodsReceiptOrderReport")+'</a></div>');
        this.button6.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(6); callDebitNoteDetails(null,null,null,true);" id="multiDebitNoteReportLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.consolidateDebitNoteReport")+'">'+WtfGlobal.getLocaleText("acc.dashboard.consolidateDebitNoteReport")+'</a></div>');
        
        this.button6.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(6); callFrequentLedger(true,\'23\',\'Monitor all cash transactions entered into the system for any time duration.\',\'accountingbase cashbook\', true);" id="multiCashBookReportLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.consolidateCashBookReport")+'">'+WtfGlobal.getLocaleText("acc.dashboard.consolidateCashBookReport")+'</a></div>');        
        this.button6.menu.addText('<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:closeMenu(6); callFrequentLedger(false,\'9\',\'Monitor all transactions for a bank account for any time duration.\',\'accountingbase bankbook\', true);" id="multiBankBookReportLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.consolidateBankBookReport")+'">'+WtfGlobal.getLocaleText("acc.dashboard.consolidateBankBookReport")+'</a></div>');        
        
        this.button6.showMenu();
}

function getAboutLinks(about) {
    this.about = about;
    if(isUSServer){
        
        var mailto = "mailto:"+subdomain+"@deskera.com";
        var innerHtml = '<a href="'+mailto+'" >'+WtfGlobal.getLocaleText("acc.dashboard.support")+'</a>'
        +'<a href="http://blog.deskera.com/" target="_blank" >'+WtfGlobal.getLocaleText("acc.dashboard.blog")+'</a>'
        +'<a href="http://support.deskera.com/index.php/Deskera_Accounting_Help" target="_blank">'+WtfGlobal.getLocaleText("acc.dashboard.help")+'</a>'
        +'<a href="#" onclick="javascript:showVesionInfo(4)">'+WtfGlobal.getLocaleText("acc.dashboard.version")+'</a>'
                    
        document.getElementById("aboutMenu").innerHTML=innerHtml;
        
    }else{
        this.button4 = new Wtf.Toolbar.MenuButton({
            menu : menu4,
            cls:'notification-menu3',
            height:50,
            renderTo:'shortcutmenu4',
            text:'<span style="color:#083772; font-size:11px;">'+WtfGlobal.getLocaleText("acc.dash.abt")+' &#9662;</span>'
        });
	this.button4.on("click",function(){button4menu(this.about);},this);
    }
}

function getSubsappLinks(obj) {
    
    if(isUSServer){
        var appstr="";
    
        var tmpString="<div class=\"templateCont \"><center>"+
        "<a  href={appurlformat}  target=\"_blank\" style=\"padding:0;\"> <img class=\"templateImg\" width=\"63\" height=\"63\" src=\"../../images/switchtoApps/{appid}.png\"></a>"+
        "</center> <div class=\"templateName\"> <span class=\"templateSpan\">{appname}</span>"+
        "</div></div>"
        var apptemplateSwichto = new Wtf.Template(tmpString);
        var noAppsAcces=true; 
        for(var cnt=0; cnt< obj.subscribedapplist.length; cnt++){
            apptemplateSwichto.set(tmpString, true);             
            appstr += apptemplateSwichto.applyTemplate({
                appname: obj.subscribedapplist[cnt].appname,
                appid: obj.subscribedapplist[cnt].appid,
                appurlformat: obj.subscribedapplist[cnt].appurlformat
            });
            noAppsAcces=false;
        }
        if(noAppsAcces){
            appstr = "<div style='margin-top:45px;'><center><font color='gray' size='3'>There is no application subscribed.</font></center></div>";
        }else{
            appstr="<div style=\"margin: 15px;\">"+appstr+"</div>";
        }
    
        var panel = new Wtf.Panel({
            layout: 'fit',
            border:false,
            renderTo:document.getElementById('subApps'),
            html : appstr
        });
    }else{
        
        var subsapp = new Array();
        
        if(obj.subscribedapplist) {
            for(var i = 0; i< obj.subscribedapplist.length; i++){
                //                      	about[i]='<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="'+obj.subscribedapplist[i].appurlformat+'" onclick="closeMenu(4); window.open('+obj.subscribedapplist[i].appurlformat+'); return false;" >'+obj.subscribedapplist[i].appname+'</a></div>'
                subsapp[i]= '<div class="wrapperForMenu"><div style="padding: 8px 0 5px 5px;" /> <a href="'+obj.subscribedapplist[i].appurlformat+'" target="_blank" onclick="javascript:closeMenu(5);" id="'+obj.subscribedapplist[i].appname+'" wtf:qtip="'+obj.subscribedapplist[i].appname+'">'+obj.subscribedapplist[i].appname+'</a></div>';
                if(obj.subscribedapplist[i].appid == Wtf.appID.PM){
                    Wtf.isPMSync = true;
                }
                if(obj.subscribedapplist[i].appid == Wtf.appID.CRM){// if CRM Integration is on for company
                    Wtf.isCRMSync = true;
                }
                if(obj.subscribedapplist[i].appid == Wtf.appID.eUnivercity){// if LMS Integration is on for company
                    Wtf.isLMSSync = true;
                }
                if(obj.subscribedapplist[i].appid == Wtf.appID.eClaim){// if Eclaim Integration is on for company
                    Wtf.iseClaimSync = true;
                }
            }
        }
        if(obj.childapplist) {
            var cnt = subsapp.length>0?subsapp.length:0;
            var j = 0;
            for(i = cnt; i< cnt+obj.childapplist.length; i++){
                subsapp[i] = '<div class="wrapperForMenu"><div style="padding: 8px 0 5px 5px;" /> <a href="'+obj.childapplist[j].appurlformat+'" target="_blank" onclick="javascript:closeMenu(5);" id="'+obj.childapplist[j].appname+'" wtf:qtip="'+obj.childapplist[j].appname_subdomain+'">'+obj.childapplist[j].appname_subdomain+'</a></div>';                        
                j++;
            }
        }
        
        if(subsapp.length > 0){
            this.Subsapp = subsapp;
            this.button5 = new Wtf.Toolbar.MenuButton({
                menu : menu5,
                cls:'notification-menu5',
                height:50,
                renderTo:'shortcutmenu5',
                text:'<span style="color:#083772; font-size:11px;">'+WtfGlobal.getLocaleText("acc.nee.49")+' &#9662;</span>'
            });
	this.button5.on("click",function(){button5menu(this.Subsapp);},this);
        }else{
            (dash=document.getElementById("dash4")).parentNode.removeChild(dash);
        }
    }
}

function closeMenu(number) {
	if(number == 1)
		this.button1.hideMenu();
	else if(number == 2)
		this.button2.hideMenu();
	else if(number == 3)
		this.button3.hideMenu();
	else if(number == 4)
		this.button4.hideMenu();
	else if(number == 5)
		this.button5.hideMenu();
        else if(number == 6)
		this.button6.hideMenu();
}

function dashboardLinks(consolidateFlag){
//    setFullname(uName);

    if(!isUSServer){
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.useradmin, Wtf.Perm.useradmin.view) && consolidateFlag) {
            this.button6 = new Wtf.Toolbar.MenuButton({
                menu : menu6,
                cls:'notification-menu2',
                height:50,
                hidden:true,
                renderTo:'shortcutmenu6',
                text:'<span style="color:#083772; font-size:11px;">'+WtfGlobal.getLocaleText("acc.dash.multicompany")+' &#9662;</span>'
            });
		this.button6.on("click",function(){button6menu(consolidateFlag);},this);
        } 
        else (dash=document.getElementById("dash6")).parentNode.removeChild(dash);
	
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.importlog, Wtf.Perm.importlog.view) || !WtfGlobal.EnableDisable(Wtf.UPerm.useradmin, Wtf.Perm.useradmin.view) || !WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.view) 
            || !WtfGlobal.EnableDisable(Wtf.UPerm.audittrail, Wtf.Perm.audittrail.view) || !WtfGlobal.EnableDisable(Wtf.UPerm.vendorpr, Wtf.Perm.vendorpr.createpr) || !WtfGlobal.EnableDisable(Wtf.UPerm.vendorpr, Wtf.Perm.vendorpr.viewpr)||!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.viewdatarange)
            ||!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.viewcdesigner) ||!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.viewclayout)) {
            this.button1 = new Wtf.Toolbar.MenuButton({
                menu : menu1,
                //	        id:'dashmenu1',
                cls:'notification-menu',
                height:50,
                renderTo:'shortcutmenu1',
                text:'<span style="color:#083772; font-size:11px;">'+WtfGlobal.getLocaleText("acc.dash.admin")+' &#9662;</span>'
            });
		this.button1.on("click",function(){button1menu();},this);
        } else
            (dash=document.getElementById("dash1")).parentNode.removeChild(dash);

                
        this.button2 = new Wtf.Toolbar.MenuButton({
            menu : menu2,
            cls:'notification-menu1',
            // height:20,
            renderTo:'shortcutmenu2',
            text:'<span style="color:#083772; font-size:11px;">'+WtfGlobal.getLocaleText("acc.dash.rep")+' &#9662;</span>'
        });
		this.button2.on("click",function(){button2menu();},this);
    }
//	if(!WtfGlobal.EnableDisable(Wtf.UPerm.costcenter, Wtf.Perm.costcenter.viewccs) || !WtfGlobal.EnableDisable(Wtf.UPerm.costcenter, Wtf.Perm.costcenter.viewccd) || !WtfGlobal.EnableDisable(Wtf.UPerm.costcenter, Wtf.Perm.costcenter.viewcct) || !WtfGlobal.EnableDisable(Wtf.UPerm.cashflow, Wtf.Perm.cashflow.view) || !WtfGlobal.EnableDisable(Wtf.UPerm.salesbyitem, Wtf.Perm.salesbyitem.view) || !WtfGlobal.EnableDisable(Wtf.UPerm.taxreport, Wtf.Perm.taxreport.viewst) || !WtfGlobal.EnableDisable(Wtf.UPerm.taxreport, Wtf.Perm.taxreport.viewpt) || (Wtf.account.companyAccountPref.countryid == '203')) {
//        } else
//		(dash=document.getElementById("dash2")).parentNode.removeChild(dash);
//                if(false)
//                {
//                this.button2 = new Wtf.Toolbar.MenuButton({
//                menu : menu2,
//	        cls:'notification-menu1',
//	        height:50,
//	        renderTo:'shortcutmenu2',
//	        text:'<span style="color:#083772; font-size:11px;">'+WtfGlobal.getLocaleText("acc.dash.rep")+' &#9662;</span>'
//	    });
//		this.button2.on("click",function(){button2menu();},this);
//}
////	} else
////		(dash=document.getElementById("dash2")).parentNode.removeChild(dash);

//	if(!WtfGlobal.EnableDisable(Wtf.UPerm.accpref, Wtf.Perm.accpref.view) || !WtfGlobal.EnableDisable(Wtf.UPerm.fixedasset, Wtf.Perm.fixedasset.view) || !WtfGlobal.EnableDisable(Wtf.UPerm.currencyexchange, Wtf.Perm.currencyexchange.view) || !WtfGlobal.EnableDisable(Wtf.UPerm.bankreconciliation, Wtf.Perm.bankreconciliation.view) ) {
//		this.button3 = new Wtf.Toolbar.MenuButton({
//	        menu : menu3,
//	        cls:'notification-menu2',
//	        height:50,
//	        renderTo:'shortcutmenu3',
//	        text:'<span style="color:#083772; font-size:11px;">'+WtfGlobal.getLocaleText("acc.dash.set")+' &#9662;</span>'
//	    });
//		this.button3.on("click",function(){button3menu();},this);
//	} else
//		(dash=document.getElementById("dash3")).parentNode.removeChild(dash);


    document.getElementById('signout').innerHTML = WtfGlobal.getLocaleText("acc.common.signout");
//    document.getElementById('changepass').innerHTML = WtfGlobal.getLocaleText("acc.changePass.tabTitle");
    if (!Wtf.isSelfService) {         
      document.getElementById('changepass').innerHTML = WtfGlobal.getLocaleText("acc.changePass.tabTitle");
      document.getElementById('myacc').style.marginLeft='0px';
    } else {
      document.getElementById('changepass').style.display='none';   //  ERP-39089 Hide change password link if company is self service company.
      document.getElementById('myacc').style.marginLeft='-9px';
    }
    document.getElementById('myacc').innerHTML = WtfGlobal.getLocaleText("acc.cc.28");
    document.getElementById('cal').innerHTML = WtfGlobal.getLocaleText("acc.contractActivityPanel.Calendar");
     
     if(!isUSServer){
        document.getElementById('signout').qtip = WtfGlobal.getLocaleText("acc.rem.96");
        document.getElementById('changepass').qtip = WtfGlobal.getLocaleText("acc.rem.95");
        document.getElementById('myacc').qtip = WtfGlobal.getLocaleText("acc.rem.94");
        document.getElementById('cal').qtip = WtfGlobal.getLocaleText("acc.rem.239");
    }

//     if(!WtfGlobal.EnableDisable(Wtf.UPerm.accpref, Wtf.Perm.accpref.view))
//        addToXCuts("#", WtfGlobal.getLocaleText("acc.dashboard.accountPreferences"),"callAccountPref()",'0',WtfGlobal.getLocaleText("acc.dashboard.TT.accountPreferences"),"accountPrefreanceLink");
//
//     if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedasset, Wtf.Perm.fixedasset.view))
//        addToXCuts("#", WtfGlobal.getLocaleText("acc.dashboard.fixedAssetDepreciation"),"callFixedAsset()",'0',WtfGlobal.getLocaleText("acc.dashboard.TT.fixedAssetDepreciation"),"fixedAssetDepereciationLink");
//
//     if(!WtfGlobal.EnableDisable(Wtf.UPerm.bankreconciliation, Wtf.Perm.bankreconciliation.view))
//        addToXCuts("#", WtfGlobal.getLocaleText("acc.dashboard.bankReconciliation"),"callReconciliationWindow()",'0',WtfGlobal.getLocaleText("acc.dashboard.TT.bankReconciliation"),"bankReconciliationLink");
//
//     if(!WtfGlobal.EnableDisable(Wtf.UPerm.currencyexchange, Wtf.Perm.currencyexchange.view))
//        addToXCuts("#", WtfGlobal.getLocaleText("acc.dashboard.currencyExchange"),"callCurrencyExchangeWindow()",'0',WtfGlobal.getLocaleText("acc.dashboard.TT.currencyExchange"),"currencyExchangeLink");
//
//     if(!WtfGlobal.EnableDisable(Wtf.UPerm.audittrail, Wtf.Perm.audittrail.view))
//        addToXCuts("#", WtfGlobal.getLocaleText("acc.dashboard.auditTrail"),"callAuditTrail()",'0',WtfGlobal.getLocaleText("acc.dashboard.TT.auditTrail"),"auditTrailLink");
//
//     if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.view))
//        addToXCuts("#", WtfGlobal.getLocaleText("acc.dashboard.masterConfiguration"),"callMasterConfiguration()",'0',WtfGlobal.getLocaleText("acc.dashboard.TT.masterConfiguration"),"masterConfigurationLink");
//
//     if(!WtfGlobal.EnableDisable(Wtf.UPerm.useradmin, Wtf.Perm.useradmin.view))
//        addToXCuts("#", WtfGlobal.getLocaleText("acc.dashboard.userAdministration"),"loadAdminPage(1)",'0',WtfGlobal.getLocaleText("acc.dashboard.TT.userAdministration"),"userAdministrationLink");
//
//     if(!WtfGlobal.EnableDisable(Wtf.UPerm.salesbyitem, Wtf.Perm.salesbyitem.view))
//        addToXCuts("#", WtfGlobal.getLocaleText("acc.dashboard.salesByItem"),"callSaleByItem()",'0',WtfGlobal.getLocaleText("acc.dashboard.TT.salesByItem"),"saleByItemLink");
//
//     if(!WtfGlobal.EnableDisable(Wtf.UPerm.costcenter, Wtf.Perm.costcenter.viewccs) || !WtfGlobal.EnableDisable(Wtf.UPerm.costcenter, Wtf.Perm.costcenter.viewccd) || !WtfGlobal.EnableDisable(Wtf.UPerm.costcenter, Wtf.Perm.costcenter.viewcct))
//    	addToXCuts("#", WtfGlobal.getLocaleText("acc.dashboard.costCenterReport"),"callCostCenterReport()",'0',WtfGlobal.getLocaleText("acc.dashboard.TT.costCenterReport"),"costCenterReportLink");
//
//     if(!WtfGlobal.EnableDisable(Wtf.UPerm.taxreport, Wtf.Perm.taxreport.viewst) || !WtfGlobal.EnableDisable(Wtf.UPerm.taxreport, Wtf.Perm.taxreport.viewpt))
//    	addToXCuts("#", WtfGlobal.getLocaleText("acc.dashboard.taxReport"),"callTaxReport()",'0',WtfGlobal.getLocaleText("acc.dashboard.TT.taxReport"),"taxReportLink");
//
//     if(!WtfGlobal.EnableDisable(Wtf.UPerm.importlog, Wtf.Perm.importlog.view))
//        addToXCuts("#", WtfGlobal.getLocaleText("acc.dashboard.importLog"),"callImportFilesLog()",'0',WtfGlobal.getLocaleText("acc.dashboard.TT.importLog"),"importLogLink");
//
//     if(!WtfGlobal.EnableDisable(Wtf.UPerm.cashflow, Wtf.Perm.cashflow.view))
//    	addToXCuts("#", WtfGlobal.getLocaleText("acc.dashboard.cashFlowStatement"),"callCashFlowStatement()",'0',WtfGlobal.getLocaleText("acc.dashboard.TT.cashFlowStatement"),"callCashFlowStatementLink");


     if(Wtf.account.companyAccountPref.standalone) {
    	 var about = new Array();
    	 var mailto = "mailto:"+subdomain+"@deskera.com";
    	 about[0]= '<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="'+mailto+'" onclick="javascript:closeMenu(4);" id="supportLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.support")+'">'+WtfGlobal.getLocaleText("acc.dashboard.support")+'</a></div>';
//    	 about[1]= '<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="http://forum.deskera.com/" onclick="closeMenu(4); window.open(\'http://forum.deskera.com/\'); return false;" id="forumLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.forum")+'">'+WtfGlobal.getLocaleText("acc.dashboard.forum")+'</a></div>';
    	 about[1]= '<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="http://blog.deskera.com/" onclick="closeMenu(4); window.open(\'http://blog.deskera.com/\'); return false;" id="blogLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.blog")+'">'+WtfGlobal.getLocaleText("acc.dashboard.blog")+'</a></div>';
    	 about[2]= '<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="http://support.deskera.com/index.php/Deskera_Accounting_Help" onclick="javascript:closeMenu(4); window.open(\'http://support.deskera.com/index.php/Deskera_Accounting_Help\'); return false;" id="accountPrefreanceLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.help")+'">'+WtfGlobal.getLocaleText("acc.dashboard.help")+'</a></div>';
         about[3]= '<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:showVesionInfo(4); " id="vesioninfo" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.version")+'">'+WtfGlobal.getLocaleText("acc.dashboard.version")+'</a></div>';
    	 getAboutLinks(about);
     }
     loadGlobalStores();
}

function sendInvitation(){
    this.inviteWindow= new Wtf.Window({
        title : WtfGlobal.getLocaleText("acc.field.InviteUser"),
        modal : true,
        iconCls : 'iconwin',
        width : 450,
        height: 395,
        resizable :false,
        buttonAlign : 'right',
        buttons :[{
            text: WtfGlobal.getLocaleText("acc.field.Invite"),
            scope: this,
            handler: function(){
                if(this.inviteUser.form.isValid()){
                    this.inviteUser.form.submit({
                        scope: this,
                        success: function(){
                            msgBoxShow(79, 0);
                            this.inviteWindow.close();
                        },
                        failure: function(frm, action){
                            if(action.failureType == "client"){
                                msgBoxShow(80, 1);
                                return;
                            }
                            msgBoxShow(81, 1);
                            this.inviteWindow.close();
                        }
                    });
                }
            }
        },{
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
            scope: this,
            handler: function(){
                this.inviteWindow.close();
            }
        }],
        layout : 'border',
        items :[{
            region : 'north',
            height : 75,
            border : false,
            bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(WtfGlobal.getLocaleText("acc.field.InviteUser"), WtfGlobal.getLocaleText("acc.field.InviteafriendtojoinDeskera"))
        },{
            region : 'center',
            border : false,
            bodyStyle : 'background:#f1f1f1;font-size:10px;',
            layout : 'fit',
            items :[this.inviteUser = new Wtf.form.FormPanel({
                url: 'signup.jsp?mode=2&u='+ loginid,
                waitMsgTarget: true,
                method : 'POST',
                border : false,
                labelWidth: 100,
                bodyStyle : 'margin-top:20px;margin-left:20px;font-size:10px;',
                defaults: {width: 398,allowBlank: false},
                defaultType: 'textfield',
                items: [{
                    fieldLabel: WtfGlobal.getLocaleText("acc.profile.fName"),
                    validator:WtfGlobal.validateUserName,
                    allowBlank: false,
                    width: 200,
                    name : 'fn'
                },{
                    fieldLabel: WtfGlobal.getLocaleText("acc.field.LastName*"),
                    validator:WtfGlobal.validateUserName,
                    allowBlank: false,
                    width: 200,
                    name : 'ln'
                },{
                    fieldLabel: WtfGlobal.getLocaleText("acc.field.EmailAddress*"),
                    width: 200,
                    name : 'e',
                    validator:WtfGlobal.validateEmail
                },{xtype : 'fieldset',
                    title: WtfGlobal.getLocaleText("acc.field.Includeapersonalnote"),
                    autoHeight: true,
                    cls: 'inviteFieldSet',
                    items: [{xtype :'textarea',
                        hideLabel: true,
                        height : 90,
                        width: 376,
                        name : 'msg',
                        id : 'invitationBody',
                        value : WtfGlobal.getLocaleText("acc.field.HiIhaveusingDeskerayoucheckitisProjectManagementToolmakinglifeeasy")
                    }]
                }]
            })]
        }]
    });
    Wtf.getCmp("invitationBody").on("change", function(){
        Wtf.getCmp("invitationBody").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("invitationBody").getValue()));
    }, this);
    this.inviteWindow.show();
}

function loadAdminPage(id){
    var ev = "adminclicked";
    switch(id) {
        case "1":
            ev = "adminclicked";
            break;
        case 1:
            ev = "adminclicked";
            break;
        case "2":
            ev = "companyclicked";
            break;
        case 2:
            ev = "companyclicked";
            break;
        case "3":
            ev = "featureclicked";
            break;
        case 3:
            ev = "featureclicked";
            break;
    }
    mainPanel.loadTab("../../admin.html", "   companyadminpanel", WtfGlobal.getLocaleText("acc.dashboard.userAdministration"), "navareadashboard", Wtf.etype.adminpanel,false,ev);
}

function loadReportPerm(id){
    var ev = "adminclicked";
    mainPanel.loadReportPermTab("../../reportPerm.html", "   reportperm", WtfGlobal.getLocaleText("acc.dashboard.userPerm"), "navareadashboard", Wtf.etype.adminpanel,false,ev);
}

function loadWidgetTab(id){
    var ev = "adminclicked";
    mainPanel.loadReportPermTab("../../reportPerm.html", "   reportperm", WtfGlobal.getLocaleText("acc.dashboard.userPerm"), "navareadashboard", Wtf.etype.adminpanel,false,ev);
}

function loadCreateCustomReportPerm(id){
    var ev = "adminclicked";
    var companyNameVal = companyName +" "+WtfGlobal.getLocaleText("acc.field.AccCustomReportWS");
    if(companyNameVal.indexOf('&') >  -1){
        companyNameVal = companyNameVal.replace('&', 'and');        
    }
    var wtfPref= JSON.stringify(Wtf.pref);
    var userReportRole= JSON.stringify(Wtf.UserReporRole);
    sessionStorage.setItem("wtfPref",wtfPref);
    sessionStorage.setItem("userReportRole",userReportRole);
    sessionStorage.setItem("loginid",loginid);
    /**
     * open Report from Statutory left navigation panel
     */
    sessionStorage.setItem("openReportId", id);
    var url = 'customReportBuilder.jsp?companyName='+companyNameVal+'&_amountdecimal='+ Wtf.AMOUNT_DIGIT_AFTER_DECIMAL+'&_unitpricedecimal='+Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL+'&_quantitydecimal='+Wtf.QUANTITY_DIGIT_AFTER_DECIMAL;   
    var newwindow = window.open(url, '_blank');    
}

function callCustomDesigner(id,selectModuleid){
    var panel = Wtf.getCmp('designerpanellistview');
    if(panel==null) {
        panel = new Wtf.DesignerDocTemplateList({
            id : 'designerpanellistview',
            iconCls :getButtonIconCls(Wtf.etype.customer),
            tabTip :WtfGlobal.getLocaleText("acc.customdesignTT"),
            title:WtfGlobal.getLocaleText("acc.customdesignTT"),  //'GIRO File Generation Log'
            closable:true,
            selectModuleid:selectModuleid,
            layout: "fit",
            border:false,
            assign:true
//            iconCls: 'pwnd projectTabIcon'
        });
        mainPanel.add(panel);
    }
    mainPanel.setActiveTab(panel);
    mainPanel.doLayout();
}

function loadSignupPage(){
    signOut("signout");
    window.location = 'http://www.deskera.com/accounting/pricing-and-signup'
}
function signOut(type){
  var _out = "";
    if (type !== undefined && typeof type != "object")
            _out = "?type=" + type;
    _dC('lastlogin');
    _dC('featureaccess');
    _dC('username');
    _dC('lid');
    _dC('companyid');
    var m = Wtf.DomainPatt.exec(window.location);
    var _u = '../../error.do';
    if (type == "noaccess" || type == "alreadyloggedin") {
		_u += '?e=' + type;
		if(m && m[1]){
			_u += '&n=' + m[1];
		}
    }
    else {
		if (m && m[1]) {
        	_u = '../../b/' + m[1] + '/signOut.do' + _out;     
		}
            }
    _r(_u);
}

function _dC(n){
    document.cookie = n + "=" + ";path=/;expires=Thu, 01-Jan-1970 00:00:01 GMT";
}

function getPermissionObjects(permobj){
    var wrapobj = permobj;
    if(wrapobj.Perm){
        Wtf.Perm = wrapobj.Perm;
    }
    if(wrapobj.UPerm){
        Wtf.UPerm = wrapobj.UPerm;
    }
    if(wrapobj.deskeraadmin){
        deskeraAdmin = true;
    }
    if(Wtf.Perm && Wtf.UPerm){
        Wtf.dispalyUnitPriceAmountInSales = !WtfGlobal.EnableDisable(Wtf.UPerm.unitpriceandamount, Wtf.Perm.unitpriceandamount.displayunitpriceandamountinsalesdocument);
        Wtf.dispalyUnitPriceAmountInPurchase = !WtfGlobal.EnableDisable(Wtf.UPerm.unitpriceandamount, Wtf.Perm.unitpriceandamount.displayunitpriceandamountinpurchasedocument);
    }
}
function getUserReportPerm(permobj){
    
    
      Wtf.UserReportPerm = permobj;
   
}
function getUserReportPerm1(permobj){
    
    
      Wtf.UserReporRole = permobj;
   
}
function getEditPricePermissionObjects(permobj){
    
    
      Wtf.productPriceEditPerm = permobj;
   
}
function getSubscriptionObjects(permobj){
    var wrapobj = permobj;
    if(wrapobj.subscription){
        Wtf.subscription = wrapobj.subscription;
    }
    Wtf.getCmp("searchTopPanel").setDocumentValue(Wtf.subscription.docs);
//    if(wrapobj.module){
//        Wtf.modules = wrapobj.module;
//    }
}

function updatePreferences(){
    Wtf.Ajax.requestEx({
        //url: Wtf.req.base + 'UserManager.jsp',
        url : "AuthHandler/getPreferences.do",
        params: {
            mode:31
        }
    }, this,
    function(result, req){
        if(result) Wtf.pref = eval(result.data)[0];
    });
}

    var preferencesfailure=function (result, req){
        Wtf.account.companyAccountPref={}
        this.ComapyPrefloadingMask.hide();
    };
    var getCompanyAccPref=function(hideLoadMask){
        this.ComapyPrefloadingMask = new Wtf.LoadMask(document.body,{
            msg :WtfGlobal.getLocaleText("acc.msgbox.50")
        });
        if(!hideLoadMask || hideLoadMask==undefined)               //  when you dont want to show loading mask send hideLoadMask flag as true
            this.ComapyPrefloadingMask.show();
       Wtf.Ajax.requestEx({
            method: 'POST',
//            url: Wtf.req.account+'CompanyManager.jsp',
            url : "ACCCompanyPref/getCompanyAccountPreferences.do",
            params: {
                   mode:81
            }
            },
            this,
            function (result, req){
                if(result.success==true && result.data){
                    Wtf.account.companyAccountPref=result.data;
                    Wtf.account.companyAccountPref.isCompanyCreatorLogged = result.data.isCompanyCreatorLogged;            //This flag is for checking Logged user is Company Creator or not. SDP-9739       
                    Wtf.account.companyAccountPref.usersspecificinfoFlow=result.data.usersspecificinfoFlow;
                    Wtf.account.companyAccountPref.fyfrom=new Date(result.data.fyfrom);
                    Wtf.account.companyAccountPref.bbfrom=new Date(result.data.bbfrom);
                    Wtf.account.companyAccountPref.gstapplieddate=new Date(result.data.gstapplieddate);
                    Wtf.account.companyAccountPref.isNewGSTOnly=result.data.isNewGSTOnly;
                    Wtf.account.companyAccountPref.firstfyfrom=new Date(result.data.firstfyfrom);
                    Wtf.account.companyAccountPref.firstfyfrom=new Date(result.data.firstfyfrom);
                    Wtf.account.companyAccountPref.UomSchemaType=result.data.UomSchemaType;
                    Wtf.account.companyAccountPref.editTransaction=result.data.editTransaction;
                    Wtf.account.companyAccountPref.editLinkedTransaction=result.data.editLinkedTransactionQuantity;
                    Wtf.account.companyAccountPref.editLinkedTransaction=result.data.editLinkedTransactionPrice;
                    Wtf.account.companyAccountPref.editso=result.data.editso;
                    Wtf.account.companyAccountPref.memo=result.data.memo;
                    Wtf.account.companyAccountPref.isAutoFillBatchDetails=result.data.isAutoFillBatchDetails;
                    Wtf.account.companyAccountPref.showprodserial=result.data.showprodserial;
                    Wtf.account.companyAccountPref.isBatchCompulsory=result.data.isBatchCompulsory;
                    Wtf.account.companyAccountPref.restrictDuplicateBatch=result.data.restrictDuplicateBatch;
                    Wtf.account.companyAccountPref.isSerialCompulsory=result.data.isSerialCompulsory;
                    Wtf.account.companyAccountPref.isLocationCompulsory=result.data.isLocationCompulsory;
                    Wtf.account.companyAccountPref.isWarehouseCompulsory=result.data.isWarehouseCompulsory;
                    Wtf.account.companyAccountPref.isUsedSerial=result.data.isUsedSerial;
                    Wtf.account.companyAccountPref.isUsedLocation=result.data.isUsedLocation;
                    Wtf.account.companyAccountPref.isUsedWarehouse=result.data.isUsedWarehouse;
                    Wtf.account.companyAccountPref.isUsedBatch=result.data.isUsedBatch;
                    Wtf.account.companyAccountPref.descriptionType=result.data.descriptionType;
                    Wtf.account.companyAccountPref.deleteTransaction=result.data.deleteTransaction;
                    Wtf.account.companyAccountPref.withouttax1099=result.data.withouttax1099
                    Wtf.account.companyAccountPref.jobworkrecieverflow=result.data.jobworkrecieverflow;
                    Wtf.account.companyAccountPref.currencyid=result.data.currid
                    Wtf.account.companyAccountPref.negativestock=result.data.negativestock
                    Wtf.account.companyAccountPref.custcreditlimit=result.data.custcreditlimit
                    Wtf.account.companyAccountPref.custcreditlimitorder=result.data.custcreditlimitorder
                    Wtf.account.companyAccountPref.vendorcreditlimitorder=result.data.vendorcreditlimitorder
                    Wtf.account.companyAccountPref.chequeNoDuplicate=result.data.chequeNoDuplicate;
                    Wtf.account.companyAccountPref.autopr=result.data.autopr
                    Wtf.account.companyAccountPref.accountsWithCode=result.data.isAccountsWithCode
                    Wtf.account.companyAccountPref.showLeadingZero=result.data.showLeadingZero
                    Wtf.account.companyAccountPref.custMinBudget=result.data.custminbudgetlimit
                    Wtf.account.companyAccountPref.invAccIntegration=result.data.inventoryAccountingIntegration
                    Wtf.account.companyAccountPref.activateInventoryTab=result.data.activateInventoryTab
                    Wtf.account.companyAccountPref.activateLoanManagementFlag=result.data.activateLoanManagementFlag
                    Wtf.account.companyAccountPref.activateMRPManagementFlag=true
                    Wtf.account.companyAccountPref.activateMRPManagementFlag=result.data.activateMRPModule;
                    Wtf.account.companyAccountPref.mrpProductComponentType=result.data.mrpProductComponentType;
                    Wtf.account.companyAccountPref.activateCycleCount=result.data.activateCycleCount
                    Wtf.account.companyAccountPref.isUpdateInvLevel=result.data.updateinventorylevel
                    Wtf.account.companyAccountPref.isQaApprovalFlow=result.data.qaapprovalflow
                    Wtf.account.companyAccountPref.isQaApprovalFlowInDO = result.data.isQaApprovalFlowInDO
                    Wtf.account.companyAccountPref.billaddress=result.data.billaddress;
                    Wtf.account.companyAccountPref.shipaddress=result.data.shipaddress; 
                    Wtf.account.companyAccountPref.remitpaymentto=result.data.remitpaymentto;
                    Wtf.account.companyAccountPref.isAddressFromVendorMaster=result.data.isAddressFromVendorMaster;
                    Wtf.account.companyAccountPref.ishtmlproddesc=result.data.ishtmlproddesc;             
                    Wtf.account.companyAccountPref.customerdefaultaccount=result.data.customerdefaultaccount;             
                    Wtf.account.companyAccountPref.vendordefaultaccount=result.data.vendordefaultaccount; 
                    Wtf.account.companyAccountPref.roundingDifferenceAccount = result.data.roundingDifferenceAccount;
                    Wtf.account.companyAccountPref.adjustmentAccountPayment = result.data.adjustmentAccountPayment;
                    Wtf.account.companyAccountPref.adjustmentAccountReceipt = result.data.adjustmentAccountReceipt;
                    Wtf.account.HideFormFieldProperty=result.data;
                    Wtf.account.companyAccountPref.dependentField=result.data.dependentField
                    Wtf.account.companyAccountPref.integrationWithPOS=result.data.integrationWithPOS;
                    Wtf.account.companyAccountPref.isCloseRegisterMultipleTimes=result.data.isCloseRegisterMultipleTimes;
                    Wtf.account.companyAccountPref.manyCreditDebit=result.data.manyCreditDebit;
                    Wtf.account.companyAccountPref.customerForPOS=result.data.customerForPOS;
                    Wtf.account.companyAccountPref.vendorForPOS=result.data.vendorForPOS;
                    Wtf.account.companyAccountPref.activateCRMIntegration=result.data.activateCRMIntegration;
                    Wtf.account.companyAccountPref.creditAccountforPOS=result.data.cashoutaccountforpos;
            //Wtf.account.companyAccountPref.autocustomercode=result.data.autocustomercode
            //Wtf.account.companyAccountPref.autovendorcode=result.data.autovendorcode
                    Wtf.account.companyAccountPref.DOSettings=result.data.DOSettings;
                    Wtf.account.companyAccountPref.allowCustVenCodeEditing=result.data.isallowCustVenCodeEditing;
                    Wtf.account.companyAccountPref.GRSettings=result.data.GRSettings;
                    Wtf.account.companyAccountPref.countryid=result.data.countryid;
                    Wtf.account.companyAccountPref.countryname=result.data.countryname;
                    Wtf.account.companyAccountPref.currencyname=result.data.currencyname;
                    Wtf.account.companyAccountPref.stateid=result.data.stateid;
                    Wtf.account.companyAccountPref.statename=result.data.statename;
                    Wtf.account.companyAccountPref.downloadglprocessflag=result.data.downloadglprocessflag
                    Wtf.account.companyAccountPref.downloadDimPLprocess=result.data.downloadDimPLprocess
                    Wtf.account.companyAccountPref.proddiscripritchtextboxflag=result.data.proddiscripritchtextboxflag
                    Wtf.account.companyAccountPref.productsortingflag=result.data.productsortingflag
                    Wtf.account.companyAccountPref.productsearchingflag=result.data.productsearchingflag
                    Wtf.account.companyAccountPref.salesAccount=result.data.salesAccount;
                    Wtf.account.companyAccountPref.lmsliabilityAccount=result.data.lmsliabilityAccount;
                    Wtf.account.companyAccountPref.DashBoardImageFlag=result.data.DashBoardImageFlag;
                    Wtf.account.companyAccountPref.salesRevenueRecognitionAccount=result.data.salesRevenueRecognitionAccount;
                    Wtf.account.companyAccountPref.isDeferredRevenueRecognition=result.data.isDeferredRevenueRecognition;
                    Wtf.account.companyAccountPref.showAllAccount=result.data.showAllAccount;
                    
                    Wtf.account.companyAccountPref.showChildAccountsInTb=result.data.showChildAccountsInTb;  
                    Wtf.account.companyAccountPref.showChildAccountsInGl=result.data.showChildAccountsInGl;  
                    Wtf.account.companyAccountPref.showChildAccountsInPnl=result.data.showChildAccountsInPnl;  
                    Wtf.account.companyAccountPref.showChildAccountsInBS=result.data.showChildAccountsInBS; 
                    
                    Wtf.account.companyAccountPref.showallaccountsinbs=result.data.showallaccountsinbs;     
                    Wtf.account.companyAccountPref.showimport=result.data.showimport;
                    Wtf.account.companyAccountPref.showAllAccountInGl=result.data.showAllAccountInGl;  
                    Wtf.account.companyAccountPref.showAllAccountsInPnl=result.data.showAllAccountsInPnl;
                    Wtf.account.companyAccountPref.isnegativestockforlocwar=result.data.isnegativestockforlocwar;
                    Wtf.account.companyAccountPref.isAllowQtyMoreThanLinkedDocCross=result.data.isAllowQtyMoreThanLinkedDocCross;
                    Wtf.account.companyAccountPref.isAllowQtyMoreThanLinkedDoc=result.data.isAllowQtyMoreThanLinkedDoc;
                    Wtf.account.companyAccountPref.productPriceinMultipleCurrency=result.data.productPriceinMultipleCurrency;
                    Wtf.account.companyAccountPref.stockValuationFlag=result.data.stockValuationFlag;
                    Wtf.account.companyAccountPref.onlyBaseCurrency=result.data.onlyBaseCurrency;
                    Wtf.account.companyAccountPref.activateProductComposition=result.data.activateProductComposition;
                    Wtf.account.companyAccountPref.packingdolist=result.data.packingdolist;
                    Wtf.account.companyAccountPref.versionslist=result.data.versionslist;    
                    Wtf.account.companyAccountPref.noOfDaysforValidTillField=result.data.noOfDaysforValidTillField;
                    Wtf.account.companyAccountPref.isSalesOrderCreatedForCustomer= result.data.isSalesOrderCreatedForCustomer;
                    Wtf.account.companyAccountPref.isOutstandingInvoiceForCustomer= result.data.isOutstandingInvoiceForCustomer;
                    Wtf.account.companyAccountPref.isMinMaxOrdering = result.data.isMinMaxOrdering;
                    Wtf.account.companyAccountPref.recurringDeferredRevenueRecognition=result.data.recurringDeferredRevenueRecognition;
                    Wtf.account.companyAccountPref.retainExchangeRate=result.data.retainExchangeRate;
                    Wtf.account.companyAccountPref.termsincludegst=result.data.termsincludegst;
                    Wtf.QUANTITY_DIGIT_AFTER_DECIMAL= result.data.quantitydigitafterdecimal;
                    Wtf.AMOUNT_DIGIT_AFTER_DECIMAL=result.data.amountdigitafterdecimal;
                    Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL=result.data.unitpricedigitafterdecimal;
                    Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT=result.data.uomconversionratedigitafterdecimal;                    
                    Wtf.CURRENCY_RATE_DIGIT_AFTER_DECIMAL=result.data.currencyratedigitafterdecimal;                    
                    Wtf.serverDate = new Date(result.data.serverDate);
                    Wtf.account.companyAccountPref.isLMSIntegration=result.data.isLMSIntegration;
                    Wtf.account.companyAccountPref.productOptimizedFlag=result.data.productOptimizedFlag;
                    Wtf.account.companyAccountPref.defaultmailsenderFlag=result.data.defaultmailsenderFlag;
                    Wtf.account.companyAccountPref.useremails=result.data.useremails;
                    Wtf.account.companyAccountPref.sendimportmailto=result.data.sendimportmailto;
                    Wtf.account.companyAccountPref.isActiveLandingCostOfItem=result.data.isActiveLandingCostOfItem;
                    Wtf.account.companyAccountPref.salesorderreopen=result.data.salesorderreopen;
                    Wtf.account.companyAccountPref.includeAmountInLimitSI=result.data.includeAmountInLimitSI;
                    Wtf.account.companyAccountPref.includeAmountInLimitPI=result.data.includeAmountInLimitPI;
                    Wtf.account.companyAccountPref.includeAmountInLimitSO=result.data.includeAmountInLimitSO;
                    Wtf.account.companyAccountPref.includeAmountInLimitPO=result.data.includeAmountInLimitPO;
//                    Wtf.account.companyAccountPref.crMail=result.data.crmail;
//                    Wtf.account.companyAccountPref.craMail=result.data.cramail;
//                    Wtf.account.companyAccountPref.rlMail=result.data.rlmail;
                    Wtf.account.companyAccountPref.isMovementWarehouseMapping=result.data.isMovementWarehouseMapping;
                    Wtf.account.companyAccountPref.salesTypeFlag=result.data.salesTypeFlag;
                    Wtf.account.companyAccountPref.purchaseTypeFlag=result.data.purchaseTypeFlag;
                    Wtf.account.companyAccountPref.depreciationCalculationType=result.data.depreciationCalculationType;  
                    Wtf.account.companyAccountPref.depreciationCalculationBasedOn=result.data.depreciationCalculationBasedOn;
                    Wtf.account.companyAccountPref.freezDepreciation=result.data.freezDepreciation; 
                    Wtf.account.companyAccountPref.autoGenPurchaseType=result.data.autoGenPurchaseType; 
                    Wtf.account.companyAccountPref.openingDepreciationPosted = result.data.openingDepreciationPosted;
                    Wtf.account.companyAccountPref.activateProfitMargin=result.data.activateProfitMargin; 
                    Wtf.account.companyAccountPref.activateimportForJE=result.data.activateimportForJE; 
                    Wtf.account.companyAccountPref.activateCRblockingWithoutStock=result.data.activateCRblockingWithoutStock; 
                    Wtf.account.companyAccountPref.activatefromdateToDate=result.data.activatefromdateToDate; 
                    Wtf.account.companyAccountPref.isDuplicateItems= result.data.isDuplicateItems;
                    Wtf.account.companyAccountPref.isInventoryModuleUsed=result.data.isInventoryModuleUsed;
                    Wtf.account.companyAccountPref.isFilterProductByCustomerCategory= result.data.isFilterProductByCustomerCategory;
                    Wtf.account.companyAccountPref.activateToDateforExchangeRates = result.data.activateToDateforExchangeRates; 
                    Wtf.account.companyAccountPref.activateToBlockSpotRate = result.data.activateToBlockSpotRate; 
                    Wtf.account.companyAccountPref.hierarchicalDimensions = result.data.hierarchicalDimensions; 
                    Wtf.account.companyAccountPref.autoPopulateDeliveredQuantity = result.data.autoPopulateDeliveredQuantity; 
                    Wtf.account.companyAccountPref.closedStatusforDo= result.data.doClosedStatus;
                    Wtf.account.companyAccountPref.profitLossAccountId = result.data.profitLossAccountId;
                    Wtf.account.companyAccountPref.openingStockAccountId = result.data.openingStockAccountId;
                    Wtf.account.companyAccountPref.closingStockAccountId = result.data.closingStockAccountId;
                    Wtf.account.companyAccountPref.stockInHandAccountId = result.data.stockInHandAccountId;
                    Wtf.account.companyAccountPref.isBaseUOMRateEdit = result.data.isBaseUOMRateEdit;
                    Wtf.account.companyAccountPref.allowZeroUntiPriceForProduct = result.data.allowZeroUntiPriceForProduct;
                    Wtf.account.companyAccountPref.allowZeroQuantityInDO = result.data.allowZeroQuantityForProduct;
                    Wtf.account.companyAccountPref.allowZeroQuantityInCQ = result.data.allowZeroQuantityInQuotation;
                    Wtf.account.companyAccountPref.allowZeroQuantityInSI= result.data.AllowZeroQuantityInSI;
                    Wtf.account.companyAccountPref.allowZeroQuantityInSO= result.data.AllowZeroQuantityInSO;
                    Wtf.account.companyAccountPref.allowZeroQuantityInPI= result.data.AllowZeroQuantityInPI;
                    Wtf.account.companyAccountPref.allowZeroQuantityInPO= result.data.AllowZeroQuantityInPO;
                    Wtf.account.companyAccountPref.allowZeroQuantityInSR= result.data.AllowZeroQuantityInSR;
                    Wtf.account.companyAccountPref.allowZeroQuantityInPR= result.data.AllowZeroQuantityInPR;
                    Wtf.account.companyAccountPref.allowZeroQuantityInGRO= result.data.AllowZeroQuantityInGRO;
                    Wtf.account.companyAccountPref.allowZeroQuantityInVQ= result.data.AllowZeroQuantityInVQ;
                    Wtf.account.companyAccountPref.blockPOcreationwithMinValue = result.data.blockPOcreationwithMinValue
                    Wtf.account.companyAccountPref.negativeStockFormulaSI = result.data.negativeStockFormulaSI;
                    Wtf.account.companyAccountPref.negativeStockFormulaSI = result.data.negativeStockFormulaSI;
                    Wtf.account.companyAccountPref.enablesalespersonAgentFlow = result.data.enablesalespersonAgentFlow;
                    Wtf.account.companyAccountPref.viewallexcludecustomerwithoutsalesperson = result.data.viewallexcludecustomerwithoutsalesperson;
                    Wtf.account.companyAccountPref.BuildAssemblyApprovalFlow = result.data.BuildAssemblyApprovalFlow;
                    Wtf.account.companyAccountPref.isPRmandatory = result.data.isPRmandatory;
                    Wtf.account.companyAccountPref.defaultsequenceformatforrecinv = result.data.defaultsequenceformatforrecinv;
                    Wtf.account.companyAccountPref.gstIncomeGroup = result.data.gstIncomeGroup;
                    Wtf.account.companyAccountPref.securityGateEntryFlag = result.data.securityGateEntryFlag;
                    Wtf.account.companyAccountPref.taxCgaMalaysian = result.data.taxCgaMalaysian;
                    Wtf.account.companyAccountPref.paymentMethodAsCard = result.data.paymentMethodAsCard;
                    Wtf.account.companyAccountPref.jobOrderItemFlow = result.data.jobOrderItemFlow;
                    Wtf.account.companyAccountPref.requestApprovalFlow = result.data.requestApprovalFlow;
                    Wtf.account.companyAccountPref.writeOffAccount = result.data.invoicesWriteOffAccount;
                    Wtf.account.companyAccountPref.enablevatcst = result.data.enablevatcst;
                    Wtf.account.companyAccountPref.assessmentcircle = result.data.assessmentcircle;
                    Wtf.account.companyAccountPref.division = result.data.division;
                    Wtf.account.companyAccountPref.areacode = result.data.areacode;
                    Wtf.account.companyAccountPref.importexportcode = result.data.importexportcode;
                    Wtf.account.companyAccountPref.authorizedby = result.data.authorizedby;
                    Wtf.account.companyAccountPref.authorizedperson = result.data.authorizedperson;
                    Wtf.account.companyAccountPref.statusordesignation = result.data.statusordesignation;
                    Wtf.account.companyAccountPref.place = result.data.place;
                    Wtf.account.companyAccountPref.vattincomposition = result.data.vattincomposition;
                    Wtf.account.companyAccountPref.vattinregular = result.data.vattinregular;
                    Wtf.account.companyAccountPref.localsalestaxnumber = result.data.localsalestaxnumber;
                    Wtf.account.companyAccountPref.interstatesalestaxnumber = result.data.interstatesalestaxnumber;
                    Wtf.account.companyAccountPref.typeofdealer = result.data.typeofdealer;
                    Wtf.account.companyAccountPref.applicabilityofvat = result.data.applicabilityofvat;
                    Wtf.account.companyAccountPref.receiptWriteOffAccount = result.data.receiptWriteOffAccount;
                    Wtf.account.companyAccountPref.allowToPostOpeningDepreciation = result.data.allowToPostOpeningDepreciation;
                    Wtf.account.companyAccountPref.isSBIFlag = result.data.isSBIFlag;
                    Wtf.account.companyAccountPref.propagateToChildCompanies = result.data.propagatetochildcompanies;
                    Wtf.account.companyAccountPref.activateDDTemplateFlow = result.data.activateDDTemplateFlow;
                    Wtf.account.companyAccountPref.activateDDInsertTemplateLink = result.data.activateDDInsertTemplateLink;
                    Wtf.account.companyAccountPref.childCompaniesPresent = result.consolidateFlag;
                    Wtf.account.companyAccountPref.activeDateRangeToDate=result.data.activeDateRangeToDate;
                    Wtf.account.companyAccountPref.activeDateRangeFromDate=result.data.activeDateRangeFromDate;
                    Wtf.account.companyAccountPref.isCurrencyCode=result.data.isCurrencyCode;//currencyCode
                    Wtf.account.companyAccountPref.inventoryValuationType=result.data.inventoryValuationType;
                    Wtf.account.companyAccountPref.isLineLevelTermFlag=result.data.isLineLevelTermFlag;
                    Wtf.account.companyAccountPref.showAddressonPOSOSave=result.data.showAddressonPOSOSave;
                    Wtf.account.companyAccountPref.isAutoSaveAndPrintChkBox=result.data.isAutoSaveAndPrintChkBox;
                    Wtf.account.companyAccountPref.isShowMarginButton=result.data.isShowMarginButton; //Show or Hide Margin Button in Invoice/Sales/Quotation create form //ERM-76
                    Wtf.isAutoRefershReportonDocumentSave=result.data.isAutoRefershReportonSave; // Setting to Load Report or not on Document save
                    Wtf.CompanyVATNumber=result.data.CompanyVATNumber;//Also update value of constant Wtf.CompanyVatNumber 
                    Wtf.CompanyCSTNumber=result.data.CompanyCSTNumber;
                    Wtf.account.companyAccountPref.freeGiftJEAccount=result.data.freeGiftJEAccount;
                    Wtf.account.companyAccountPref.activateIBGCollection=result.data.activateIBGCollection;
                    Wtf.account.companyAccountPref.originatingBICCodeForUOBBank=result.data.originatingBICCodeForUOBBank;
                    Wtf.account.companyAccountPref.updateStockAdjustmentPrice = result.data.updateStockAdjustmentPrice;  
                    Wtf.account.companyAccountPref.isStockInQAFlowActivated = result.data.enableStockOutApprovalFlow;  
                    if(result.data.columnPref!=undefined && result.data.columnPref!=null && result.data.columnPref!=''){
                        Wtf.account.companyAccountPref.columnPref = JSON.parse(result.data.columnPref);
                        /**
                         * If gstamountdigitafterdecimal is undefined then assign default value.
                         */
                        Wtf.account.companyAccountPref.columnPref.gstamountdigitafterdecimal = Wtf.account.companyAccountPref.columnPref.gstamountdigitafterdecimal || Wtf.defaultgstamountdigitafterdecimal;
                    }
                    if(result.data.currencycode!=undefined && result.data.currencycode!=null && result.data.currencycode!=''){
                       Wtf.account.companyAccountPref.currencycode=result.data.currencycode;
                    }
                    if(Wtf.Countryid==Wtf.Country.INDONESIA || result.data.countryid == Wtf.Country.INDONESIA){
                        Wtf.CompanyNPWPNumber=result.data.CompanyNPWPNumber;
                    }else{
                        Wtf.CompanyPANNumber=result.data.CompanyPANNumber;
                    }
                    Wtf.CompanyServiceTaxRegNumber=result.data.CompanyServiceTaxRegNumber;
                    Wtf.CompanyTANNumber=result.data.CompanyTANNumber;
                    Wtf.CompanyECCNumber=result.data.CompanyECCNumber;
                    
                    Wtf.isTDSApplicable=result.data.isTDSApplicable;
                    Wtf.dateofregistration=result.data.dateofregistration;
                    Wtf.returncode=result.data.returncode;
                    Wtf.cstregistrationdate=result.data.cstregistrationdate;
                    Wtf.isSTApplicable=result.data.isSTApplicable;
                    Wtf.headofficetanno=result.data.headofficetanno;
                    Wtf.CompanyTDSInterestRate=result.data.CompanyTDSInterestRate;
                    Wtf.commissioneratecode=result.data.commissioneratecode;
                    Wtf.commissioneratename=result.data.commissioneratename;
                    Wtf.serviceTaxRegNo=result.data.servicetaxregno;
                    Wtf.divisioncode=result.data.divisioncode;
                    Wtf.rangecode=result.data.rangecode;
                    Wtf.isExciseApplicable=result.data.isExciseApplicable;
                    Wtf.excisecommissioneratecode=result.data.excisecommissioneratecode;
                    Wtf.excisecommissioneratename=result.data.excisecommissioneratename;
                    Wtf.registrationType=result.data.registrationType;
                    Wtf.manufacturerType=result.data.manufacturerType;
                    Wtf.unitname=result.data.unitname;
                    Wtf.exciseTariffdetails=result.data.exciseTariffdetails;
                    Wtf.tariffName=result.data.tariffName;
                    Wtf.HSNCode=result.data.HSNCode;
                    Wtf.reportingUOM=result.data.reportingUOM;
                    Wtf.exciseMethod=result.data.exciseMethod;
                    Wtf.exciseRate=result.data.exciseRate;

                    Wtf.salesaccountidcompany=result.data.salesaccountidcompany;
                    Wtf.salesretaccountidcompany=result.data.salesretaccountidcompany;
                    Wtf.purchaseretaccountidcompany=result.data.purchaseretaccountidcompany;
                    Wtf.purchaseaccountidcompany=result.data.purchaseaccountidcompany;
                    Wtf.interstatepuracccformid=result.data.interstatepuracccformid;
                    Wtf.interstatepuraccid=result.data.interstatepuraccid;
                    Wtf.interstatepuraccreturncformid=result.data.interstatepuraccreturncformid;
                    Wtf.interstatepurreturnaccid=result.data.interstatepurreturnaccid;
                    Wtf.interstatesalesacccformid=result.data.interstatesalesacccformid;
                    Wtf.interstatesalesaccid=result.data.interstatesalesaccid;
                    Wtf.interstatesalesaccreturncformid=result.data.interstatesalesaccreturncformid;
                    Wtf.interstatesalesreturnaccid=result.data.interstatesalesreturnaccid;                    
                    Wtf.companyfullname=result.data.companyfullname;                    
                    Wtf.cdomain=result.data.cdomain

                    Wtf.pmURL=result.data.pmURL;
                    
                    
                    Wtf.exciseMultipleUnit=result.data.exciseMultipleUnit;
                    Wtf.exciseJurisdictiondetails=result.data.excisejurisdictiondetails;
                    Wtf.excisedivisioncode=result.data.excisedivisioncode;
                    Wtf.exciserangecode=result.data.exciserangecode;
                    Wtf.TDSincometaxcircle=result.data.TDSincometaxcircle;
                    Wtf.TDSrespperson=result.data.TDSrespperson;
                    Wtf.TDSresppersonfathersname=result.data.TDSresppersonfathersname;
                    Wtf.TDSresppersondesignation=result.data.TDSresppersondesignation;
                    Wtf.isAddressChanged=result.data.isAddressChanged;
                    Wtf.deductortype=result.data.deductortype;
                    // TDS Responsible person details 
                    Wtf.resposiblePersonHasAddressChanged=result.data.resposiblePersonHasAddressChanged;
                    Wtf.resposiblePersonstate=result.data.resposiblePersonstate;
                    Wtf.resposiblePersonPAN=result.data.resposiblePersonPAN;
                    Wtf.istaxonadvancereceipt=result.data.istaxonadvancereceipt;
                    Wtf.istcsapplicable=result.data.istcsapplicable;
                    Wtf.istdsapplicable=result.data.istdsapplicable;
                    Wtf.isitcapplicable=result.data.isitcapplicable;
                    Wtf.resposiblePersonPostal=result.data.resposiblePersonPostal;
                    Wtf.resposiblePersonEmail=result.data.resposiblePersonEmail;
                    Wtf.resposiblePersonMobNumber=result.data.resposiblePersonMobNumber;
                    Wtf.resposiblePersonMobNumber=result.data.resposiblePersonMobNumber;
                    Wtf.resposiblePersonTeleNumber=result.data.resposiblePersonTeleNumber;  
                    Wtf.resposiblePersonAddress=result.data.resposiblePersonAddress;  
                    Wtf.assessmentYear=result.data.AssessmentYear;  
                    Wtf.CINnumber=result.data.CINnumber;
                    // GST fields - Start
                    Wtf.isGSTApplicable=result.data.isGSTApplicable;  
                    Wtf.GSTIN=result.data.GSTIN;  
                    Wtf.showIndiaCompanyPreferencesTab=result.data.showIndiaCompanyPreferencesTab;
                    Wtf.IndianGST=(result.data.countryid==Wtf.Country.INDIA && result.data.isGSTApplicable)?true:false;// Combination of Indian-GST and India-Company check
                    
                    // GST fields - End
                    //VAT Accounts at Company Preferences
                    Wtf.vatPayableAcc=result.data.vatPayableAcc;
                    Wtf.vatInCreditAvailAcc=result.data.vatInCreditAvailAcc;
                    Wtf.CSTPayableAcc=result.data.CSTPayableAcc;
                    Wtf.excisePayableAcc=result.data.excisePayableAcc;
                    Wtf.exciseDutyAdvancePaymentaccount=result.data.exciseDutyAdvancePaymentaccount;
                    Wtf.exciseAdvancePayableAcc=result.data.exciseAdvancePayableAcc;
                    Wtf.STPayableAcc=result.data.STPayableAcc;
                    Wtf.GTAKKCPaybleAccount=result.data.GTAKKCPaybleAccount;
                    Wtf.GTASBCPaybleAccount=result.data.GTASBCPaybleAccount;
                    Wtf.CustomDutyAccount=result.data.customdutyaccount;
                    Wtf.IGSTCustomDutyAccount=result.data.igstaccount;
                    Wtf.STAdvancePaymentaccount=result.data.STAdvancePaymentaccount;
                    Wtf.pmtMethod=result.data.pmtMethod;
                    Wtf.bankid=result.data.bankid;
                    Wtf.agedReceivableDateFilter = result.data.agedReceivableDateFilter;
                    Wtf.agedPayableDateFilter = result.data.agedPayableDateFilter;
                    Wtf.agedPayableInterval = result.data.agedPayableInterval;
                    Wtf.agedPayableNoOfInterval = result.data.agedPayableNoOfInterval;
                    Wtf.agedReceivableInterval = result.data.agedReceivableInterval;
                    Wtf.agedReceivableNoOfInterval = result.data.agedReceivableNoOfInterval;
                    /**
                     * Check if FY is closed or not. If closed, then don't allow initial stock and opening balance in COA.
                     * */
                    Wtf.isBookClosed = result.data.isBookClosed; 
                    
                    
                    Wtf.account.companyAccountPref.badDebtProcessingPeriod=result.data.badDebtProcessingPeriod;
                    Wtf.account.companyAccountPref.badDebtProcessingPeriodType=result.data.badDebtProcessingPeriodType;
                    Wtf.account.companyAccountPref.gstSubmissionPeriod=result.data.gstSubmissionPeriod;
                    Wtf.account.companyAccountPref.industryCode=result.data.industryCode;
                    Wtf.account.companyAccountPref.activateGroupCompaniesFlag=result.data.activateGroupCompaniesFlag;
                    Wtf.account.companyAccountPref.isMultiGroupCompanyParentFlag=result.data.isMultiGroupCompanyParentFlag;
                    //assign Enable Cash Receive Return setting
                    Wtf.account.companyAccountPref.enableCashReceiveReturn = result.data.enableCashReceiveReturn;
                    
                    if(result.data.leaseManagementFlag==undefined || result.data.leaseManagementFlag==null){
                        Wtf.account.companyAccountPref.leaseManagementFlag=false;
                    }else{
                        Wtf.account.companyAccountPref.leaseManagementFlag=result.data.leaseManagementFlag;
                    }
                    Wtf.getCmp('leasemanagementNavigationPanelID').setVisible(Wtf.account.companyAccountPref.leaseManagementFlag);
                    if(Wtf.account.companyAccountPref.leaseManagementFlag) {
                        if(isProdBuild) {
                            WtfModuleWiseScripts.loadScripts(Wtf.ModuleScriptGr.assetslease);
                        }
                    }
                    if (Wtf.account.companyAccountPref.countryid != Wtf.Country.SINGAPORE || !Wtf.account.companyAccountPref.columnPref.irasIntegration) {
                        if (Wtf.getCmp('StatutoryTreeID').getRootNode().findChild("id", "91")) {
                            var statutory_Child_Node = Wtf.getCmp('StatutoryTreeID').getRootNode().findChild("id", "91");
                            if (statutory_Child_Node.findChild("id", "911")) {
                                if (statutory_Child_Node.findChild("id", "911").findChild('id', 'GSTForm5eSubmissionDetails')) {
                                    statutory_Child_Node.findChild("id", "911").removeChild(statutory_Child_Node.findChild("id", "911").findChild('id', 'GSTForm5eSubmissionDetails'));
                                }
                            }
                            if (statutory_Child_Node.findChild("id", "IRASAuditeSubmission")) {
                                statutory_Child_Node.removeChild(statutory_Child_Node.findChild("id", "IRASAuditeSubmission"));
                            }
                            if (statutory_Child_Node.findChild('id', 'IRASAuditeSubmissionHistory')) {
                                statutory_Child_Node.removeChild(statutory_Child_Node.findChild('id', 'IRASAuditeSubmissionHistory'));
                            }
                        }
                    }
                    if(Wtf.account.companyAccountPref.countryid!=Wtf.Country.MALAYSIA){// if it is not a Malasian Company
                        if(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','bad1')){// removing Sales Bad Debt Releif Adjustment 
                            Wtf.getCmp('StatutoryTreeID').getRootNode().removeChild(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','bad1'));
                        }
                        
                        if(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','bad2')){// removing Purchase Bad Debt Releif Adjustment
                            Wtf.getCmp('StatutoryTreeID').getRootNode().removeChild(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','bad2'));
                        }
                        
                        if(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','doadjs')){// removing Delivery Order Adjustment
                            Wtf.getCmp('StatutoryTreeID').getRootNode().removeChild(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','doadjs'));
                        }
                        
                        if(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','inputadj')){// removing Input Tax Adjustment
                            Wtf.getCmp('StatutoryTreeID').getRootNode().removeChild(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','inputadj'));
                        }
                        
                        if(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','outputadj')){// removing Output Tax Adjustment
                            Wtf.getCmp('StatutoryTreeID').getRootNode().removeChild(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','outputadj'));
                        }
                        
                        if(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91') && Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','tapfile')){// removing GST Tap Return File
                            Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').removeChild(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','tapfile'));
                        }
                        
                        if(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91') && Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','auditfile')){// removing GST Tap Return File
                            Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').removeChild(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','auditfile'));
                        }
                        
                        if(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91') && Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','tapreturnfile')){// removing GST Tap Return File
                            Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').removeChild(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','tapreturnfile'));
                        }
                        
                        
                        if(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91') && Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','badsaleinvoiceid')){// removing GST Tap Return File
                            Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').removeChild(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','badsaleinvoiceid'));
                        }
                        
                        if(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91') && Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','gsttapdetailedview')){// removing GST Tap Return File
                            Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').removeChild(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','gsttapdetailedview'));
                        }
                        
                        if(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91') && Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','badrecoverinvoiceid')){// removing GST Tap Return File
                            Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').removeChild(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','badrecoverinvoiceid'));
                        }
                        
                        if(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91') && Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','badpurchaseinvoiceid')){// removing GST Tap Return File
                            Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').removeChild(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','badpurchaseinvoiceid'));
                        }
                        
                        if(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91') && Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','badpurchaserecoverinvoiceid')){// removing GST Tap Return File
                            Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').removeChild(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','badpurchaserecoverinvoiceid'));
                        }
                        
                        if(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91') && Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','gstrep')){// removing GST Report
                            Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').removeChild(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','gstrep'));
                        }
                    }  
                if(Wtf.account.companyAccountPref.countryid == Wtf.Country.MALAYSIA || Wtf.account.companyAccountPref.countryid == Wtf.Country.INDONESIA || Wtf.Countryid == Wtf.Country.PHILIPPINES){
                        if(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91') && Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','911')){// removing GST Node
                            Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').removeChild(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','911'));
                        }
                        
                        if(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91') && Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','912')){// removing IRAS Audit File Node
                            Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').removeChild(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','912'));
                        }
                    }
                    //For INDIA if VAT/CST disable hide VAT reprot Node from "Statutory"
                    if(Wtf.Countryid==Wtf.Country.INDIA &&  !Wtf.account.companyAccountPref.enablevatcst){
                        if(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91') && Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','VATRepo')){// removing VAT Report Node
                            Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').removeChild(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','VATRepo'));
                        }
                    }
                    
                    //For INDIA if VAT/CST disable hide VAT reprot Node from "Statutory"
                    if(Wtf.Countryid==Wtf.Country.INDIA &&  !Wtf.account.companyAccountPref.isSTApplicable){
                        if(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91') && Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','ServiceTaxRepo')){// removing VAT Report Node
                            Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').removeChild(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','ServiceTaxRepo'));
                        }
                    }
                   
                    if(Wtf.account.companyAccountPref.childCompaniesPresent!==true){// Consolidation Report available only when child company is available otherwise we remove this node
                        if(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91') && Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','consolidation')){// removing GST Node
                            Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').removeChild(Wtf.getCmp('StatutoryTreeID').getRootNode().findChild('id','91').findChild('id','consolidation'));
                        }
                    }
                    if(Wtf.account.companyAccountPref.securityGateEntryFlag!==true){// Consolidation Report available only when child company is available otherwise we remove this node
                        if(Wtf.getCmp('PurchaseTreeID').getRootNode().findChild('id','29') && Wtf.getCmp('PurchaseTreeID').getRootNode().findChild('id','29').findChild('id','29317')){
                            Wtf.getCmp('PurchaseTreeID').getRootNode().findChild('id','29').removeChild(Wtf.getCmp('PurchaseTreeID').getRootNode().findChild('id','29').findChild('id','29317'));
                        }
                    }
                    if(Wtf.account.companyAccountPref.columnPref && Wtf.account.companyAccountPref.columnPref.activateBankReconcilitaionDraft!==true){
                        if(Wtf.getCmp('glBankBookTree').getRootNode().findChild('id','25') && Wtf.getCmp('glBankBookTree').getRootNode().findChild('id','25').findChild('id','260')){
                            Wtf.getCmp('glBankBookTree').getRootNode().findChild('id','25').removeChild(Wtf.getCmp('glBankBookTree').getRootNode().findChild('id','25').findChild('id','260'));
                        }
                    }
                    /*
                     *To display as per company perference security gate check.
                     *Remove Security Gate Entry node from Purchase tree.
                     */
                    if(Wtf.account.companyAccountPref.securityGateEntryFlag!=true){
                        if(Wtf.getCmp('PurchaseTreeID').getRootNode().findChild('id','31') && Wtf.getCmp('PurchaseTreeID').getRootNode().findChild('id','31').findChild('id','311')&& Wtf.getCmp('PurchaseTreeID').getRootNode().findChild('id','31').findChild('id','311').findChild('id','31118')){
                            Wtf.getCmp('PurchaseTreeID').getRootNode().findChild('id','31').findChild('id','311').removeChild(Wtf.getCmp('PurchaseTreeID').getRootNode().findChild('id','31').findChild('id','311').findChild('id','31118'));
                        }
                    }
                    
                    if(Wtf.account.companyAccountPref.consignmentSalesManagementFlag != undefined && Wtf.account.companyAccountPref.consignmentSalesManagementFlag != null){
                        Wtf.getCmp('consignmentStockSalesNavigationPanelID').setVisible(Wtf.account.companyAccountPref.consignmentSalesManagementFlag);
                        if(isProdBuild && Wtf.account.companyAccountPref.consignmentSalesManagementFlag) {
                            WtfModuleWiseScripts.loadScripts(Wtf.ModuleScriptGr.consignment);
                        }
                    }
                    if(Wtf.account.companyAccountPref.consignmentPurchaseManagementFlag != undefined && Wtf.account.companyAccountPref.consignmentPurchaseManagementFlag != null){
                        Wtf.getCmp('consignmentStockPurchaseNavigationPanelID').setVisible(Wtf.account.companyAccountPref.consignmentPurchaseManagementFlag);
                        if(isProdBuild && Wtf.account.companyAccountPref.consignmentPurchaseManagementFlag) {
                            WtfModuleWiseScripts.loadScripts(Wtf.ModuleScriptGr.consignment);
                        }
                     }
//                     if(Wtf.account.companyAccountPref.systemManagementFlag != undefined && Wtf.account.companyAccountPref.systemManagementFlag != null){
//                        Wtf.getCmp('systemNavigationPanelID').setVisible(Wtf.account.companyAccountPref.systemManagementFlag);
//                     }
                    if(Wtf.account.companyAccountPref.masterManagementFlag != undefined && Wtf.account.companyAccountPref.masterManagementFlag != null){
                        Wtf.getCmp('mastersNavigationPanelID').setVisible(Wtf.account.companyAccountPref.masterManagementFlag);
                     }
                    if(Wtf.account.companyAccountPref.activateLoanManagementFlag != undefined && Wtf.account.companyAccountPref.activateLoanManagementFlag != null){
                        Wtf.getCmp('loanmanagementNavigationPanelID').setVisible(Wtf.account.companyAccountPref.activateLoanManagementFlag);
                        if(isProdBuild) {
                            WtfModuleWiseScripts.loadScripts(Wtf.ModuleScriptGr.loan);
                        }
                     }
                    if (Wtf.account.companyAccountPref.activateMRPManagementFlag != undefined && Wtf.account.companyAccountPref.activateMRPManagementFlag != null) {
                        Wtf.getCmp('mrpmanagementNavigationPanelID').setVisible(Wtf.account.companyAccountPref.activateMRPManagementFlag);
                            if(Wtf.account.companyAccountPref.activateMRPManagementFlag){
                                WtfModuleWiseScripts.loadScripts(Wtf.ModuleScriptGr.mrp);
                            }
                        }
                        
                       if ( Wtf.jobWorkInFlowFlag != undefined &&  Wtf.jobWorkInFlowFlag != null) {
                        Wtf.getCmp('jobworkmanagementNavigationPanelID').setVisible( Wtf.jobWorkInFlowFlag);
                        }  
                       if ( Wtf.account.companyAccountPref.jobWorkOutFlow != undefined && Wtf.account.companyAccountPref.jobWorkOutFlow!= null) {
                        Wtf.getCmp('jobworkoutmanagementNavigationPanelID').setVisible(Wtf.account.companyAccountPref.jobWorkOutFlow);
                        }  
                     if(Wtf.account.companyAccountPref.generalledgerManagementFlag != undefined && Wtf.account.companyAccountPref.generalledgerManagementFlag != null){
                        Wtf.getCmp('GeneralLedgerNavigationPanelID').setVisible(Wtf.account.companyAccountPref.generalledgerManagementFlag);
                     }
                    if(Wtf.account.companyAccountPref.accountsreceivablesalesFlag != undefined && Wtf.account.companyAccountPref.accountsreceivablesalesFlag != null){
                        Wtf.getCmp('AccountsReceivableSalesNavigationPanelID').setVisible(Wtf.account.companyAccountPref.accountsreceivablesalesFlag);
                     }
                     if(Wtf.account.companyAccountPref.accountpayableManagementFlag != undefined && Wtf.account.companyAccountPref.accountpayableManagementFlag != null){
                        Wtf.getCmp('AccountsPayablePurchasesNavigationPanelID').setVisible(Wtf.account.companyAccountPref.accountpayableManagementFlag);
                     }
                    if(Wtf.account.companyAccountPref.assetManagementFlag != undefined && Wtf.account.companyAccountPref.assetManagementFlag != null){
                        Wtf.getCmp('fixedAssetNavigationPanelID').setVisible(Wtf.account.companyAccountPref.assetManagementFlag);
                        if(isProdBuild && Wtf.account.companyAccountPref.assetManagementFlag) {
                            WtfModuleWiseScripts.loadScripts(Wtf.ModuleScriptGr.assetslease);
                        }
                     }
                     if(Wtf.account.companyAccountPref.statutoryManagementFlag != undefined && Wtf.account.companyAccountPref.statutoryManagementFlag != null){
                        Wtf.getCmp('StatutoryNavigationPanelID').setVisible(Wtf.account.companyAccountPref.statutoryManagementFlag);
                     }
                    if(Wtf.account.companyAccountPref.miscellaneousManagementFlag != undefined && Wtf.account.companyAccountPref.miscellaneousManagementFlag != null){
                        Wtf.getCmp('MiscellaneousNavigationPanelID').setVisible(Wtf.account.companyAccountPref.miscellaneousManagementFlag);
                        if (!Wtf.account.companyAccountPref.isLMSIntegration) {
                            var tree = Wtf.getCmp('folderview');
                            var isPresent = tree.getNodeById("18");
                            if (tree != undefined && isPresent!= undefined) {

                                for (var i = 0; i < tree.root.childNodes.length; i++) {

                                    if (tree.root.childNodes[i].attributes.id == "18") {
                                        tree.root.childNodes[i].remove();
                                    }

                                }

                            }
                        }else{
                            var tree = Wtf.getCmp('folderview');
                            var isPresent = tree.getNodeById("18");
                            if (Wtf.account.companyAccountPref.isLMSIntegration && !isPresent && Wtf.UserReporRole.URole.roleid==1) {
                             tree.root.appendChild(_createNode(WtfGlobal.getLocaleText("acc.field.SyncAllFromLMS"), '18', false, true, 'images/Masters/Sync.png'));

                            }
                    }
                        
                     }
                
                    if (!CompanyPreferenceChecks.discountMaster() && !CompanyPreferenceChecks.discountOnPaymentTerms()) {                                //removing discount master from miscellaneous tree panel if the enable multiple discount check in company preference is off 
                        if (Wtf.getCmp('folderview').getRootNode().findChild('id', '22')) {
                            Wtf.getCmp('folderview').getRootNode().removeChild(Wtf.getCmp('folderview').getRootNode().findChild('id', '22'));
                        }
                    }else {
                        var tree = Wtf.getCmp('folderview');
                        var isPresent = tree.getNodeById("22") == undefined ? false : true;
                        if (!WtfGlobal.EnableDisable(Wtf.UPerm.discountMaster, Wtf.Perm.discountMaster.addDiscountMaster) && !isPresent) {
                            tree.root.appendChild(_createDiscountNode(WtfGlobal.getLocaleText("acc.masterConfig.discountMasterSales"), '22', false, true, 'images/Masters/documents-designer.png'));
                        }
                    }
                    
//                    if(Wtf.account.companyAccountPref.ActivateFixedAssetModule != undefined && Wtf.account.companyAccountPref.ActivateFixedAssetModule != null){
//                        Wtf.getCmp('fixedAssetNavigationPanelID').setVisible(Wtf.account.companyAccountPref.ActivateFixedAssetModule);
//                    }
                    
                    if(document.getElementById('signout').innerHTML != WtfGlobal.getLocaleText("acc.common.signout"))
                    	linksAfterStandaloneCheck(result.consolidateFlag);
                    if (!result.data.setupdone) {
                         var date = new Date();
                            var FinancialStartDate = new Date(date.getFullYear(), 0, 1);
                            Wtf.Ajax.requestEx({
                                url: "ACCAccountCMN/getDefaultCompanySetUpData.do",
                                params: {
                                    currencyid: Wtf.account.companyAccountPref.currid,
                                    countryid: Wtf.account.companyAccountPref.countryid,
                                    financialYrStartDate: WtfGlobal.convertToGenericDate(FinancialStartDate)
                                }
                            }, this, function(resp) {
                                if (resp.success) {
                                    Wtf.defaultAccountDetails=resp.defaultaccount.data;
                                    Wtf.defaultAccountGroups=resp.defaultaccountgroup.data;
                                    Wtf.defaultCurrencyExchangeDet=resp.defaultexchangerate.data;
                                    Wtf.defaultGSTTaxDetails=resp.defaultgsttax.data;
                                } 

                            });
                        callSetUpWizard();
                    }
                    if(Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.isUpdateInvLevel){
                           Wtf.inventoryStore.load();
                           Wtf.inventoryLocation.load();
                    }
                    if(Wtf.account.companyAccountPref.isLineLevelTermFlag){
                           Wtf.LineTermsMasterStore.load();
                    }
                    if(Wtf.account.companyAccountPref.activateInventoryTab != undefined && Wtf.account.companyAccountPref.activateInventoryTab != null){
                        Wtf.getCmp('inventory').setVisible(Wtf.account.companyAccountPref.activateInventoryTab);
                        if(isProdBuild && Wtf.account.companyAccountPref.activateInventoryTab) {
                            WtfModuleWiseScripts.loadScripts(Wtf.ModuleScriptGr.inventory);
                        }
                    }
                    this.ComapyPrefloadingMask.hide();
                    if (Wtf.viewDashboard == 0 && !Wtf.ModuleScriptLoadedFlag.dashboardUpdates) {// Featching updates on Flow Diagram view only.
                        getDashboardUpdates();
                        Wtf.ModuleScriptLoadedFlag.dashboardUpdates=true;
                    }
                    Wtf.account.inventoryPref={};
                    Wtf.account.inventoryPref.stockRequestQA=(result.data.enableStockRequestReturnApprovalFlow != undefined && result.data.enableStockRequestReturnApprovalFlow != "") ? result.data.enableStockRequestReturnApprovalFlow : false;
                    Wtf.account.inventoryPref.interStoreQA=(result.data.enableInterStoreApprovalFlow != undefined && result.data.enableInterStoreApprovalFlow != "") ? result.data.enableInterStoreApprovalFlow : false;
                    /**
                     * If company is having barcode scanning facility/flag false then 
                     * in this case we won't show the links that will redirect to generateorder.jsp/startworkorder.jsp
                     * @type @exp;Wtf@call;getCmp
                     */
                    var navigationsalestreepanel = Wtf.getCmp('navigationsalestreepanel');
                    if (navigationsalestreepanel) {
                        var DOWithBarcodeScanner = navigationsalestreepanel.getNodeById('DOWithBarcodeScanner');
                        if (DOWithBarcodeScanner && !Wtf.account.companyAccountPref.barcodeScanning) {
                            DOWithBarcodeScanner.remove();
                        }
                    }
                    var navigationpurchasetreepanel = Wtf.getCmp('PurchaseTreeID');
                    if (navigationpurchasetreepanel) {
                        var GRNWithBarcodeScanner = navigationpurchasetreepanel.getNodeById('GRNWithBarcodeScanner');
                        if (GRNWithBarcodeScanner && !Wtf.account.companyAccountPref.barcodeScanning) {
                            GRNWithBarcodeScanner.remove();
                        }
                    }
                    var navigationmrpmanagementtreepanel = Wtf.getCmp('navigationmrpmanagementtreepanel');
                    if (navigationmrpmanagementtreepanel) {
                        var MRP_WOWithBarcodeScanner = navigationmrpmanagementtreepanel.getNodeById('MRP_WOWithBarcodeScanner');
                        if (MRP_WOWithBarcodeScanner && !Wtf.account.companyAccountPref.barcodeScanning) {
                            MRP_WOWithBarcodeScanner.remove();
                        }
                    }
                }else{
                    preferencesfailure(result,req);
                }                             
                loadLandingCostCategoryCustomFields();
            },preferencesfailure

        );
//        Wtf.Ajax.requestEx({
//        url: "INVConfig/getConfig.do",
//                params: {
//                }
//        },this,function (response) {
//            if (response && response.data) {
//                Wtf.account.inventoryPref.stockRequestQA = (response.data.enableStockRequestReturnApprovalFlow != undefined && response.data.enableStockRequestReturnApprovalFlow != "") ? response.data.enableStockRequestReturnApprovalFlow : false;
//                Wtf.account.inventoryPref.interStoreQA = (response.data.enableInterStoreApprovalFlow != undefined && response.data.enableInterStoreApprovalFlow !="") ? response.data.enableInterStoreApprovalFlow : false;
//            }
//        });
    };
    var getExtraCompanyPref=function(){
       Wtf.Ajax.requestEx({
            method: 'POST',
            url : "ACCCompanyPref/getExtraCompanyPreferences.do"
            },
            this,
            function (result, req){
                if(result.success==true && result.data){
                    Wtf.account.isCPAndWIPAccountsSET=result.data.isCPAndWIPAccountsSET;
                 }
            },function(res,req){
                
            }

        );
    };
    function _createNode(nodeText, nodeID, canDrag, isLeaf, nodeIcon){
            var treeNode=new Wtf.tree.TreeNode({
                text: nodeText,
                id: nodeID,
                 cls:'paddingclass',
                allowDrag: canDrag,
                leaf: isLeaf,
                icon: nodeIcon
            });
            treeNode.on("click",function(node){
               syncAllFromLMS();
            },this);
            return treeNode;
        }
    function _createDiscountNode(nodeText, nodeID, canDrag, isLeaf, nodeIcon){
            var treeNode=new Wtf.tree.TreeNode({
                text: nodeText,
                id: nodeID,
                 cls:'paddingclass',
                allowDrag: canDrag,
                leaf: isLeaf,
                icon: nodeIcon
            });
            treeNode.on("click",function(node){
               callDiscountMasterSalesWindow();        //calls discount master window
            },this);
            return treeNode;
        }
    var getDashboardUpdates=function(){
       Wtf.Ajax.requestEx({
            method: 'POST',
            url : "ACCDashboard/getDashboardUpdates.do",
            params: {
                isFromDashBoard : true
            }
            },
            this,
            function (result, req){
                if(result.success==true && result.data){
                    document.getElementById("statuspanelouterid"+Wtf.userid).innerHTML = result.data;
                 }
                if (result.Timezone && result.Timezone == true) {
                    document.getElementById("header").style.height = "50px";
                    document.getElementById("headTimezone").style.display = "block";
                }
                Wtf.getCmp('viewport').doLayout();
            },function(res,req){
                
            }

        );
    };

    function initLocaleInfo(){
    	Wtf.getCmp("tabdashboard").setTitle(WtfGlobal.getLocaleText("acc.dashboard"));
    	Wtf.getDom("accountPrefreanceLink").innerHTML =  WtfGlobal.getLocaleText("acc.dashboard.accountPreferences");
    	Wtf.getDom("fixedAssetDepereciationLink").innerHTML =  WtfGlobal.getLocaleText("acc.dashboard.fixedAssetDepreciation");
    	Wtf.getDom("bankReconciliationLink").innerHTML =  WtfGlobal.getLocaleText("acc.dashboard.bankReconciliation");
    	Wtf.getDom("currencyExchangeLink").innerHTML =  WtfGlobal.getLocaleText("acc.dashboard.currencyExchange");
    	Wtf.getDom("auditTrailLink").innerHTML =  WtfGlobal.getLocaleText("acc.dashboard.auditTrail");
    	Wtf.getDom("masterConfigurationLink").innerHTML =  WtfGlobal.getLocaleText("acc.dashboard.masterConfiguration");
    	Wtf.getDom("userAdministrationLink").innerHTML =  WtfGlobal.getLocaleText("acc.dashboard.userAdministration");
    	Wtf.getDom("saleByItemLink").innerHTML =  WtfGlobal.getLocaleText("acc.dashboard.salesByItem");
    	Wtf.getDom("costCenterReportLink").innerHTML =  WtfGlobal.getLocaleText("acc.dashboard.costCenterReport");
    	Wtf.getDom("taxReportLink").innerHTML =  WtfGlobal.getLocaleText("acc.dashboard.taxReport");
    	Wtf.getDom("importLogLink").innerHTML =  WtfGlobal.getLocaleText("acc.dashboard.importLog");
    	Wtf.getDom("callCashFlowStatementLink").innerHTML =  WtfGlobal.getLocaleText("acc.dashboard.cashFlowStatement");
    	Wtf.getDom("supportLink").innerHTML =  WtfGlobal.getLocaleText("acc.dashboard.support");
    	Wtf.getDom("forumLink").innerHTML =  WtfGlobal.getLocaleText("acc.dashboard.forum");
    	Wtf.getDom("blogLink").innerHTML =  WtfGlobal.getLocaleText("acc.dashboard.blog");
    	Wtf.getDom("helpLink").innerHTML =  WtfGlobal.getLocaleText("acc.dashboard.help");
    }

function getClientTimeZone() {
    var offset = new Date().getTimezoneOffset(), o = Math.abs(offset);
    return (offset < 0 ? "+" : "-") + ("00" + Math.floor(o / 60)).slice(-2) + ":" + ("00" + (o % 60)).slice(-2);
}
/**
 * Function to get time(In Big Int) excluding browser time zone,
 * time is multiplied with 6000 to get time in miliseconds.
 */
function getTimeEcludingBrowsertimezone(date) {
    var RequiredDate = (new Date(date).getTime()) + ((new Date().getTimezoneOffset()) * 60000);
    return RequiredDate;
}

function editInvoiceExchangeRates(winid, basecurrency, foreigncurrency, exchangerate, exchangeratetype, isGRNlinkedwithPI) {
    /**
    * while changing the Exchange rate when linking GRN to Purchase invoice.
    * and given a pop up message  "Since Goods Receipt is linked with Purchase Invoice, Document value of Purchase Invoice and Goods Receipt would become different to each other If you change the unit price/Exchange rate."
    */
    if (isGRNlinkedwithPI != undefined && isGRNlinkedwithPI != "" && isGRNlinkedwithPI=="true") {
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.warning"),
            msg: WtfGlobal.getLocaleText("acc.invoicegrid.cannotchangeunitprice"),
            buttons: Wtf.MessageBox.OK,
            icon: Wtf.MessageBox.WARNING,
            scope: this,
            scopeObj: this,
            fn: function (btn) {
                if (btn == "ok") {
                    Wtf.MessageBox.prompt(WtfGlobal.getLocaleText("acc.setupWizard.curEx"), '<b>' + WtfGlobal.getLocaleText("acc.nee.58") + '</b> 1 ' + basecurrency + ' = ' + exchangerate + ' ' + foreigncurrency +
                            '<br><b>' + WtfGlobal.getLocaleText("acc.nee.59") + '</b>', showInvoiceExternalExchangeRate);
                }
            }
        });
    } else {
        Wtf.MessageBox.prompt(WtfGlobal.getLocaleText("acc.setupWizard.curEx"), '<b>' + WtfGlobal.getLocaleText("acc.nee.58") + '</b> 1 ' + basecurrency + ' = ' + exchangerate + ' ' + foreigncurrency +
                '<br><b>' + WtfGlobal.getLocaleText("acc.nee.59") + '</b>', showInvoiceExternalExchangeRate);
    }
    function showInvoiceExternalExchangeRate(btn, txt) {
        if (btn == 'ok') {
            if (txt.indexOf('.') != -1)
                var decLength = (txt.substring(txt.indexOf('.'), txt.length - 1)).length;
            if (isNaN(txt) || txt.length > 15 || decLength > 7 || txt == 0) {
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.setupWizard.curEx"), //'Exchange Rate',
                    msg: WtfGlobal.getLocaleText("acc.nee.55") +
                            "<br>" + WtfGlobal.getLocaleText("acc.nee.56") +
                            "<br>" + WtfGlobal.getLocaleText("acc.nee.57"),
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.WARNING,
                    //                    width: 300,
                    scope: this,
                    fn: function () {
                        if (btn == "ok") {
                            editInvoiceExchangeRates(winid, basecurrency, foreigncurrency, exchangerate, exchangeratetype);
                        }
                    }
                });
            } else {
                if (exchangeratetype != undefined)
                    Wtf.getCmp(winid).exchangeratetype = exchangeratetype
                if (exchangeratetype != undefined && exchangeratetype == 'foreigntobase') {
                    if ((txt * 1) > 0) {
                        Wtf.getCmp(winid).revexternalcurrencyrate = txt;
                        var exchangeRateNormal = 1 / ((txt * 1) - 0);
                        exchangeRateNormal = (Math.round(exchangeRateNormal * Wtf.Round_Off_Number)) / Wtf.Round_Off_Number;
                        Wtf.getCmp(winid).externalcurrencyrate = exchangeRateNormal;
                    }
                } else {
                    Wtf.getCmp(winid).externalcurrencyrate = txt;
                }
                Wtf.getCmp(winid).updateFormCurrency();
            }
        }
    }

}
function validateServerSession(){
    WtfGlobal.setAjaxTimeOut();  // set timeout to 15mins
    Wtf.Ajax.requestEx({
//        url: 'jspfiles/validate.jsp',
        url : 'AuthHandler/verifyLogin.do',
        params: {
            blank: -1,
	    browsertz : getClientTimeZone()
        }
    }, this,
    function(result, req){
        WtfGlobal.resetAjaxTimeOut();  // reset timeout to 30secs
//        var res = eval("(" + result + ")");
    	if(!window['messages']){
            window.location.reload(true);
        }
        var res = result;
        Wtf.templateflag = res.templateflag;
        Wtf.viewDashboard=res.viewDashboard;
        Wtf.theme=res.theme;
        Wtf.DashBoardImageFlag=res.DashBoardImageFlag;
        Wtf.accountpayableManagementFlag=res.accountpayableManagementFlag;
        Wtf.accountsreceivablesalesFlag=res.accountsreceivablesalesFlag;
        Wtf.masterManagementFlag=res.masterManagementFlag;
        Wtf.jobWorkInFlowFlag = res.jobWorkInFlowFlag;
        Wtf.syncAllFromLMSFlag=res.syncAllFromLMSFlag;
        Wtf.defaultReferralKeyflag=res.defaultReferralKeyflag;
        Wtf.isExciseApplicable=res.isExciseApplicable;  // ERP-27117 :Provide Feature to Edit Excise Unit Window Permission 
        Wtf.isTDSApplicable=res.isTDSApplicable;  
        Wtf.Countryid=res.countryid;
        Wtf.Stateid=res.stateid;
        Wtf.userSessionId=res.userSessionId;
        Wtf.isNewGSTOnly=res.isNewGSTOnly;
        Wtf.isSelfService = res.isSelfService ? res.isSelfService : 0;
        isUSServer = res.isUSServer;
        
        getHeaderElement();
        getPermissionObjects(res.perm);
        getEditPricePermissionObjects(res.priceEditPerm);
        getUserReportPerm(res.UserReportPerm);
        getUserReportPerm1(res.role)
        createMaintainanceCall();
        
        var dashboardURL = "";
        if(Wtf.viewDashboard==0){
            dashboardURL="ACCDashboard/getDashboardData.do";
        }else if(Wtf.viewDashboard==1){
            dashboardURL='../../dashboard.html';
        }else if(Wtf.viewDashboard==2){
            dashboardURL='../../graphicalDashboard.html';
        }
        
        if(Wtf.theme){
            Wtf.util.CSS.swapStyleSheet("theme", 'lib/resources/css/' + Wtf.theme);
        }
        //getSubscriptionObjects(res.modsub);
        if (res) {
            
            /**
             * If user has scanbarcode permission then only user can access generateorder.jsp page.
             * If user has permission then barcodescanner=1 otherwise 0.
             */
            if (res && res.perm && res.perm.UPerm && res.perm.UPerm.barcodescanner === 1) {
                window.top.location.href = './generateorder.jsp';
            } else if (res && res.perm && res.perm.UPerm && res.perm.UPerm.barcodescanner === 2) {
                /**
                 * If user has Work Order permission then only user can access startworkorder.jsp page.
                 * If user has permission then barcodescanner=2 otherwise 0.
                 */
                window.top.location.href = './startworkorder.jsp';
            } else {
            if(!deskeraAdmin){
                Wtf.pref = res.preferences;
                getCompanyAccPref();
                getExtraCompanyPref();
                mainPanel = new Wtf.ux.MainPanel({
                    id: 'as',
                    region: 'center',
                    deferredRender: false,
                    resizeTabs: true,
                    minTabWidth: 155,
                    loadMask: new Wtf.LoadMask(document.body, Wtf.apply(this.loadMask)),
                    cls: 'ascls',
                    titleCollapse: true,
                    activeTab: 0,
                    enableTabScroll: true,
                    items: [new Wtf.ux.ContentPanel({
                        id: "tabdashboard",
                        title: WtfGlobal.getLocaleText("acc.dashboard"),  //"Dashboard",
                        navarea: "navareadashboard",
                        layout : "fit",
                        closable:false,
                        /*html:  "<div id='dashhelp' class='outerHelp'>"+
               "<div style='float:left; padding-left:25%;'><img src='../../images/alerticon.gif'></div><div class='helpHeader'>New to Deskera CRM?</div>"+
               "<div class='helpContent'><a href='#' class='helplinks' style='color:#445566;' onclick='takeTour()'>Take a quick tour</a>&nbsp;&nbsp;"+
               "<a class='helplinks' style='color:#445566;' href='#' onclick='noThanks()'>No Thanks</a>"+
               "</div></div>",*/
                        autoLoad: {
                            //url:'dashboard.html',
                            //                url:Wtf.req.account+'dashboard.jsp?refresh=false',
                            url:dashboardURL,
                            params:{
                                refresh:false,
                                start : 0,
                                limit : 10
                            },
                            scripts: true
                        },
//                        autoLoad: {
//                        //               url: Wtf.req.base + 'dashboard.jsp?refresh=false',
//                        url:'../../dashboard.html',
//                        scripts: true
//                    },
                        iconCls: getTabIconCls(Wtf.etype.home),
                        tabType: Wtf.etype.home
                    })]
                });                              
                 var systemTree = new Wtf.SystemTree({
                     layout:'fit'
                 });
                 var masterTree = new Wtf.MasterTree({
                     layout:'fit'
                 });
                 var inventoryTree = new Wtf.InventoryTree({
                     layout:'fit'
                 });
                 var GLCashBank = new Wtf.GLCashBankTree({
                     id : "glBankBookTree",
                     layout:'fit'
                 });
                 var SalesTree = new Wtf.SalesTree({
                     id: 'navigationsalestreepanel',
                     layout:'fit'
                 });
                 var PurchaseTree = new Wtf.PurchaseTree({
                     id:'PurchaseTreeID',
                     layout:'fit'
                 });
                 var FixedAssets = new Wtf.FixedAssets({
                     layout:'fit'
                 });                 
                 var leaseManagement = new Wtf.LeaseManagement({
                     layout:'fit'
                 });                 
                 var loanManagement = new Wtf.LoanManagement({
                     layout:'fit'
                 });    
                 var mrpManagement = new Wtf.MRPManagement({
                     id: 'navigationmrpmanagementtreepanel',
                     layout: 'fit'
                 });
                   var jobWorkManagement = new Wtf.navigationJobWorkTreePanel({
                     layout: 'fit'
                 });
                   var jobWorkOutManagement = new Wtf.navigationJobWorkOutTreePanel({
                     layout: 'fit'
                 });
                 var consignmentStock = new Wtf.ConsignmentStockManagment({
                     layout:'fit'
                 });                 
                 var consignmentStockPurchase = new Wtf.ConsignmentStockManagmentPurchase({
                     layout:'fit'
                 });                 
                 var MiscellaneousTree = new Wtf.MiscellaneousTree({
                     layout:'fit'
                 });
                 var StatutoryTree = new Wtf.Statutory({
                     id:'StatutoryTreeID',
                     layout:'fit'
                 });
                 var reportsTree = new Wtf.Reports({
                     layout:'fit'
                 });
                 var navigationPanel = new Wtf.ux.NavigationPanel({
                    region: 'west',
                    split: true,
                    id: 'navigationpanel',
                    cls: 'navigationPanel',
                    collapsible: true,
                    title: WtfGlobal.getLocaleText("acc.field.Menu"),
                    margins: '0 0 0 0',
                    layout: 'border',
                    border:false,
                    items: [new Wtf.Panel({
                        border: false,
                        layout:'accordion',
                        id: 'centerNavRegion',
                        region: 'center',
                            items: [{
                             title: '<B>'+WtfGlobal.getLocaleText("acc.field.System")+'</B>',
                             bodyStyle:' background: none repeat scroll 0 0 #FFFFFF;',
                             autoScroll: true,
                              id: 'systemNavigationPanelID',
                             items:[new Wtf.Panel({
                                border: false,
//                                id: 'pmessage',
                                layout:'fit',
                                items:[systemTree]
                                
                            })]
                        },{
                            title: '<B>'+WtfGlobal.getLocaleText("acc.field.Masters")+'</B>',
                             autoScroll: true,
                             bodyStyle:' background: none repeat scroll 0 0 #FFFFFF;',
                             id: 'mastersNavigationPanelID',
                             items:[new Wtf.Panel({
                                border: false,
                                id: 'masters',
                                 layout:'fit',
                                items:[masterTree]
                                
                            })]
                        }
                        ,{
                            title: '<B>'+WtfGlobal.getLocaleText("acc.field.GeneralLedger/Cash/Bank")+'</B>',
                              bodyStyle:' background: none repeat scroll 0 0 #FFFFFF;',
                             autoScroll: true,
                             id: 'GeneralLedgerNavigationPanelID',
                             items:[new Wtf.Panel({
                                border: false,
//                                id: 'pmessage',
                                layout:'fit',
                                items:[GLCashBank]
                                
                            })]
                        },{
                            title: '<B>'+WtfGlobal.getLocaleText("acc.field.AccountsReceivableSales")+'</B>',
                              bodyStyle:' background: none repeat scroll 0 0 #FFFFFF;',
                             autoScroll: true,
                             id: 'AccountsReceivableSalesNavigationPanelID',
                             items:[new Wtf.Panel({
                                border: false,
//                                id: 'pmessage',
                                layout:'fit',
                                items:[SalesTree]
                                
                            })]
                        },{
                            title: '<B>'+WtfGlobal.getLocaleText("acc.field.AccountsPayablePurchases")+'</B>',
                              bodyStyle:' background: none repeat scroll 0 0 #FFFFFF;',
                             autoScroll: true,
                             id: 'AccountsPayablePurchasesNavigationPanelID',
                             items:[new Wtf.Panel({
                                border: false,
//                                id: 'pmessage',
                                layout:'fit',
                                items:[PurchaseTree]
                                
                            })]
                        },{
                            title: '<B>'+WtfGlobal.getLocaleText("acc.fixedAssetList.tabTitle")+'</B>',
                            bodyStyle:' background: none repeat scroll 0 0 #FFFFFF;',
                            autoScroll: true,
                            id:'fixedAssetNavigationPanelID',
//                            hidden:true,
                             items:[new Wtf.Panel({
                                border: false,
//                                 hidden:true,
//                                id: 'pmessage',
                                layout:'fit',
                                items:[FixedAssets]
                                
                            })]
                        },{
                            title: '<B>'+WtfGlobal.getLocaleText("acc.product.inventory")+'</B>',
                             autoScroll: true,
                             hidden : true,
                             id: 'inventory',
                             bodyStyle:' background: none repeat scroll 0 0 #FFFFFF;',
                             items:[new Wtf.Panel({
                                border: false,
                                 layout:'fit',
                                items:[inventoryTree]
                                
                            })]
                        }
                        ,{
                            title: '<B>'+WtfGlobal.getLocaleText("acc.lease.management")+'</B>',
                            bodyStyle:' background: none repeat scroll 0 0 #FFFFFF;',
                            autoScroll: true,
                            id:'leasemanagementNavigationPanelID',
//                            hidden:true,
                             items:[new Wtf.Panel({
                                border: false,
//                                 hidden:true,
//                                id: 'pmessage',
                                layout:'fit',
                                items:[leaseManagement]
                                
                            })]
                        },{
                            title: '<B>'+WtfGlobal.getLocaleText("acc.consignment.Stock.Sales")+'</B>',
                            bodyStyle:' background: none repeat scroll 0 0 #FFFFFF;',
                            autoScroll: true,
                            id:'consignmentStockSalesNavigationPanelID',

                            items:[new Wtf.Panel({
                                border: false,
                                layout:'fit',
                                items:[consignmentStock]
                                
                            })]
                          },{
                            title: '<B>'+WtfGlobal.getLocaleText("acc.consignment.Stock.purchase")+'</B>',
                            bodyStyle:' background: none repeat scroll 0 0 #FFFFFF;',
                            autoScroll: true,
                            id:'consignmentStockPurchaseNavigationPanelID',

                             items:[new Wtf.Panel({
                                border: false,
                                layout:'fit',
                                items:[consignmentStockPurchase]
                                
                            })]
                        },{
                            title: '<B>'+WtfGlobal.getLocaleText("acc.field.InventoryWarehousing")+'</B>',
                              bodyStyle:' background: none repeat scroll 0 0 #FFFFFF;',
                              hidden:true,
                             autoScroll: true,
                             items:[new Wtf.Panel({
                                border: false,
                                 hidden:true,
//                                id: 'pmessage',
                                layout:'fit'
                                
                            })]
                        },{
                            title: '<B>'+WtfGlobal.getLocaleText("acc.loan.management")+'</B>',
                            bodyStyle:' background: none repeat scroll 0 0 #FFFFFF;',
                            autoScroll: true,
                            id:'loanmanagementNavigationPanelID',
//                            hidden:true,
                             items:[new Wtf.Panel({
                                border: false,
//                               hidden:true,
//                                id: 'pmessage',
                                layout:'fit',
                                items:[loanManagement]
                                
                            })]
                        },{
                            title: '<B>'+WtfGlobal.getLocaleText("acc.mrp.management")+'</B>',
                            bodyStyle:' background: none repeat scroll 0 0 #FFFFFF;',
                            autoScroll: true,
                            id:'mrpmanagementNavigationPanelID',
                             items:[new Wtf.Panel({
                                border: false,
                                layout:'fit',
                                items:[mrpManagement]
                            })]
                        },{
                            title: '<B>'+WtfGlobal.getLocaleText("acc.jobwork.management")+'</B>',
                            bodyStyle:' background: none repeat scroll 0 0 #FFFFFF;',
                            autoScroll: true,
                            id:'jobworkmanagementNavigationPanelID',
                             items:[new Wtf.Panel({
                                border: false,
                                layout:'fit',
                                items:[jobWorkManagement]
                            })]
                        },{
                            title: '<B>'+WtfGlobal.getLocaleText("acc.user.jobWorkOutFlow")+'</B>',
                            bodyStyle:' background: none repeat scroll 0 0 #FFFFFF;',
                            autoScroll: true,
                            id:'jobworkoutmanagementNavigationPanelID',
                             items:[new Wtf.Panel({
                                border: false,
                                layout:'fit',
                                items:[jobWorkOutManagement]
                            })]
                        },{
                            title: '<B>'+WtfGlobal.getLocaleText("acc.field.Statutory")+'</B>',
                              bodyStyle:' background: none repeat scroll 0 0 #FFFFFF;',
                             autoScroll: true,
                             id: 'StatutoryNavigationPanelID',
                             items:[new Wtf.Panel({
                                border: false,
//                                id: 'pmessage',
                                layout:'fit',
                                items:[StatutoryTree]
                                
                            })]
                        },{
                            title: '<B>'+WtfGlobal.getLocaleText("acc.dash.rep")+'</B>',
                              bodyStyle:' background: none repeat scroll 0 0 #FFFFFF;',
                             autoScroll: true,
//                             id: 'reportsNavigationPanelID',
                             items:[new Wtf.Panel({
                                border: false,
//                                id: 'pmessage',
                                layout:'fit',
                                items:[reportsTree]
                                
                            })]
                        },{
                            title: '<B>'+WtfGlobal.getLocaleText("acc.ct.Miscellaneous")+'</B>',
                              bodyStyle:' background: none repeat scroll 0 0 #FFFFFF;',
                             autoScroll: true,
                             id: 'MiscellaneousNavigationPanelID',
                             items:[new Wtf.Panel({
                                border: false,
                                layout:'fit',
                                items:[MiscellaneousTree]
                                
                            })]
                        },{
                            title: '<B>'+WtfGlobal.getLocaleText("acc.field.Utility")+'</B>',
                              bodyStyle:' background: none repeat scroll 0 0 #FFFFFF;',
                             autoScroll: true,
                             hidden:true,
                             items:[new Wtf.Panel({
                                border: false,
//                                id: 'pmessage',
                                layout:'fit'
                                
                            })]
                        },{
                            title: '<B>'+WtfGlobal.getLocaleText("acc.field.MergedCompanies")+'</B>',
                              bodyStyle:' background: none repeat scroll 0 0 #FFFFFF;',
                             autoScroll: true,
                             hidden:true,
                             items:[new Wtf.Panel({
                                border: false,
//                                id: 'pmessage',
                                layout:'fit'
                                
                            })]
                        }]
                    })]


                });
                
                var viewport = new Wtf.Viewport({
                    layout: 'border',
                    id:'viewport',
                    items:[
                    new Wtf.Panel({
                        region:'north',
                        autoHeight:true,
                        hidden:true,
                        border:false,
                        //autoScroll:true,
                        cls:'announcementpan',
                        id:'announcementpan',
                        html:'<div style="position: relative;"><span style="z-index: 10000; position: absolute; float: right; margin-top: 5px; margin-right: 5px; right: 0pt;"><img src="images/stop12.gif" alt="close" onclick="javascript:hideTopPanel();"></span><div id="announcementpandiv" style="padding:15px;"></div> </div>'
                    }),
                    new Wtf.Panel({
                        region:'center',
                        border:false,
                        layout:'border',
                    items: [new Wtf.BoxComponent({
                        region: 'north',
                        el: "header"
                        }), mainPanel,navigationPanel]
                    })]
                });
                
                viewport.doLayout();

              

//                Wtf.getCmp("tabdashboard").on("activate", function(){
//                    if(bHasChanged){
//                        Wtf.getCmp("tabdashboard").load({
//                            //                url: Wtf.req.account+"dashboard.jsp?refresh=false",
//                            url:"ACCDashboard/getDashboardData.do",
//                            params:{
//                                refresh:false,
//                                start : 0,
//                                limit : 10
//                            },
//                            scripts: true
//                        });
//                        bHasChanged=false;
//                    }
//                }, this);

                Wtf.useShims = true;
                
                loginid = res.lid;
                companyids = res.companyids;
                gcurrencyid = res.gcurrencyid;
//                multicompany = res.multicompany;
                Wtf.userid = res.lid;
                
                if(isUSServer){
                    document.getElementById("userImage").src = "images/store/"+Wtf.userid+".png";
                    document.getElementById("userArrow").style.display="";
                    document.getElementById("appArrow").style.display="";
                    document.getElementById("aboutArrow").style.display="";
                }
                
                if(subdomain){
                    if(subdomain=='sats'){
                        SATSCOMPANY_ID="04575a0c-b33c-11e3-986d-001e670e1424";
//                        Wtf.After_Decimal=12;
                    }else if(subdomain=='testsats'){
                        SATSCOMPANY_ID="650e954e-d559-11e5-92e4-14dda97927f2";
//                        Wtf.After_Decimal=12;
                    }
                }
                loginname = res.username;
                companyid = res.companyid;
                Wtf.CompanyVATNumber=res.CompanyVATNumber;
                Wtf.CompanyCSTNumber=res.CompanyCSTNumber;
                if(Wtf.Countryid==Wtf.Country.INDONESIA){
                    Wtf.CompanyNPWPNumber=res.CompanyPANNumber;
                }else{
                    Wtf.CompanyPANNumber=res.CompanyPANNumber;
                }
                Wtf.CompanyServiceTaxRegNumber=res.CompanyServiceTaxRegNumber;
                Wtf.CompanyTANNumber=res.CompanyTANNumber;
                Wtf.CompanyECCNumber=res.CompanyECCNumber;
//                if(Wtf.viewDashboard==0){// Featching updates on Flow Diagram view only.
//                    getDashboardUpdates();
//                }
                Wtf.productStore.baseParams.excludeParent = true;
                Wtf.productStoreSales.baseParams.excludeParent =true;
                
                
                _fullName=res.fullname;
                if(!isUSServer){
                    setFullname(res.fullname);
                }
//                Wtf.pref = eval(res.preferences)[0];
//                Wtf.pref = res.preferences;
//                getCompanyAccPref();
//                getExtraCompanyPref();
//                setValidUserVariables(res.validuseroptions);
//                addToXCuts(res.forum_base_url, "Forum");
//                addToXCuts("http://blog.deskera.com", "Blog");
//                document.cookie = "featureaccess=" + eval('(' + res.perm.trim() + ')').UPerm.Features + ";path=/;";
//                dojo.cometd.init("bind");
                var chatsubstr = "/" + loginid + "/chat";
//                dojo.cometd.subscribe(chatsubstr, this, "globalchatPublishHandler");
//                dojo.cometd.subscribe("/"+loginid+"/inbox", this, "inboxPublishHandler");
//                var txtSrch = Wtf.getCmp("textSearch");
                companyName = res.company;

                 



                pagebaseURL= res.base_url;
                document.getElementById('acctitle').text = companyName +" "+WtfGlobal.getLocaleText("acc.rem.3");
                
                var defaultModuleId = "2,4,6,8,10,12,14,16,18,20,22,23,24,25,26,27,28,29,31,32,33,34,35,79,92,95,1001,1002,1003,1114,1115,1116,";
                var assetModuleId = "38,39,40,41,42,87,88,89,90,96,98,121,";
                var consignmentPurchaseModuleId = "57,58,59,63,";
                var consignmentSalesModuleId = "50,51,52,53,";
                var leaseModuleId = "36,64,65,67,68,93,";
                var masterContractModuleId = "1106,";
                var inventoryModuleId = Wtf.Acc_CycleCount_ModuleId+",";
                 
                 if(res.assetManagementFlag){
                     defaultModuleId += assetModuleId;
                 }
                 if(res.consignmentPurchaseManagementFlag){
                     defaultModuleId += consignmentPurchaseModuleId;
                 }
                 if(res.consignmentSalesManagementFlag){
                     defaultModuleId += consignmentSalesModuleId;
                 }
                 if(res.leaseManagementFlag){
                     defaultModuleId += leaseModuleId;
                 }
                if (res.activateMRPManagementFlag) {
                    defaultModuleId += masterContractModuleId;
                }
                if(res.activateInventoryTab){
                    defaultModuleId += inventoryModuleId;
                }
                Wtf.companyAccountPref_isAdminSubdomain=(subdomain=="admin")?true:false;
                loadCustomFieldColModel(undefined, defaultModuleId.substring(0,defaultModuleId.length-1).split(','));
                loadCustomFeildsColumnModelForReports(undefined, defaultModuleId.substring(0,defaultModuleId.length-1).split(','));  //(Mayur B) load column model for invoice and purchase order 
                loadCustomDesignModel(undefined, '0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,31,32,33,38,39,40,41,50,51,52,53,54,55,56,57,58,59,60,61,62,63,119,123,125,126,79,50,51,52,53,57,58,59,63,64,65,67,68,93,90,36,1000,1001,1002,1003,92,95,1114,1004,247,132,1116,133,124,1105,87,88,89,96,98,1115,1338,1339'.split(','));
		loadDefaultCustomLayouts();
                loadDimensionFeildsColumnModelForReports(undefined, '2'.split(','));	
                loadGlobalDimensionCustomFieldModel(undefined, defaultModuleId.substring(0,defaultModuleId.length-1).split(','))            
                 loadGlobalProductMasterFields();            
//                txtSrch.emptyText = 'Search on ' + companyName;
//                txtSrch.reset();
            } else {
                WtfGlobal.loadScript("../../scripts/minified/superUser.js");
                WtfGlobal.loadStyleSheet("../../style/companystat.css");
            }
        	WtfGlobal.loadScript("props/msgs/messages.js");   // Locale Implementation
                if(Wtf.FixedAssetStore){
                    Wtf.FixedAssetStore.load();
                }
            }
        } else
            signOut();
    }, signOut);
}

function getHeaderElement(){
    var header = document.getElementById("header");
    var headerHtml ="";
    var companyLogoSRC = document.getElementById("dummyCompanyLogo").src;
    if(isUSServer){
        header.style.backgroundColor = "#464646";
        headerHtml = '<img id="companyLogo" src="../../images/graphicalDashboard/deskera-white.png" style="padding-left: 15px; padding-top: 5px;" alt="logo"/>'
    +'<div class="dropdown" style="float:right;right: 20px;top: 5px;">'
    +'<img id="userImage" class="image-circle" src="images/store/a.png"/>'
    +'<div id="userArrow" style="display: block;" class="arrow-up" style=" background: transparent none repeat scroll 0% 0%;"></div>'
    +'<div class="dropdown-content" id="profileMenu">'
    +'<a id="myacc" href="#" onclick="showPersnProfile()" >User Profile</a>'
    +'<a id="cal" href="#" onclick="callCalendar()" >My Calender</a>'
    +'<a id="changepass" href="#" onclick="showPersnProfile1()">Change Password </a>'
    +'<a id="signout" href="#" onclick="signOut(\'signout\')"></a>'
    +'</div>'
    +'</div>'
    +'<div class="dropdown" style="float:right;right: 35px;top: 5px;">'
    +' <img class="image-circle" src="images/graphicalDashboard/apps1.png"/>'
    +'<div id="appArrow" style="display: block;" class="arrow-up" style=" background: transparent none repeat scroll 0% 0%;"></div>'
    +'<div class="dropdown-content" id="subApps" style="height:350px;width:300px;">'
    +'</div>'
    +'</div>'
    +'<div class="dropdown" style="float:right;right: 48px;top: 5px;">'
    +'<img class="image-circle" style="width: 32px;height: 32px;margin-top:8px;" src="images/graphicalDashboard/about.png"/>'
    +'<div id="aboutArrow" style="display: block;" class="arrow-up" style=" background: transparent none repeat scroll 0% 0%;"></div>'
    +'<div class="dropdown-content" id="aboutMenu" style="right: 120px;">'
    +'</div>'
    +'</div>'

    }else{
        headerHtml = '<div id="headTimezone" class="TimezonePopup" id="wtf-gen442">'
        +"<div class=\"TimezoneMessage\">Please note that your timezone is different from your organization's timezone. Please <a onclick=\"showPersnProfile()\" href=\"#\" style=\"color:#445566;\" class=\"helplinks\">click here</a> to update.</div>"
        +'<div class="TimezoneImage" onclick = "closeTimezonePop()">&nbsp</div>'
        +'</div>'
        +'<img id="companyLogo" src="'+companyLogoSRC+'" alt="logo"/>'
        +'<img src="../../images/Deskera-financials-text.png" alt="accounting" style="float:left;margin-left:4px;margin-top:1px;" />'
        +'<div class="userinfo"> '
        +'<span id="whoami"></span><br /><a id="signout"; href="#" onclick="signOut(\'signout\');"></a>&nbsp;&nbsp;<a id="changepass"; href="#" onclick="showPersnProfile1();"></a>&nbsp;&nbsp;<a id="myacc"; href="#" onclick="showPersnProfile();"></a>&nbsp;&nbsp;<a id="cal"; href="#" onclick="callCalendar();"></a>'
        +'</div>'
        +'<div id="serchForIco"></div>'
        +'<div id="searchBar"></div>'
        +'<div id="shortcuts" class="shortcuts">'
        +'<div id="menulinks" style="float:right !important;position: relative;">'
        +'<div id="shortcutmenu6"style="float:left !important;position: relative;"></div>'
        +'<div id="dash6"style="float: left ! important; margin-top: 3px;">|</div>'
        +'<div id="shortcutmenu1"style="float:left !important;position: relative;"></div>'
        +'<div id="dash1"style="float: left ! important; margin-top: 3px;">|</div>'
        +'<div id="shortcutmenu2"style="float:left !important;position: relative;"></div>'
        +'<div id="dash2"style="float: left ! important; margin-top: 3px;">|</div>'
        +'<div id="shortcutmenu4"style="float:left !important;position: relative;"></div>'
        +'<div id="dash4"style="float: left ! important; margin-top: 3px;">|</div>'
        +'<div id="shortcutmenu5"style="float:left !important;position: relative;"></div>'
        +'</div>'
        +'<div id="signupLink"style="float: right ! important; margin-top: 3px;"></div>'
        +'</div>'
        +'</div>'
    }
    
    
    header.innerHTML = headerHtml;
}

function linksAfterStandaloneCheck(consolidateFlag){
    if(!Wtf.account.companyAccountPref.standalone) {
        Wtf.Ajax.requestEx({
            url: "ACCDashboard/getDashboardLinks.do",
            params: {
                action: 14,
                companyid: companyid
            }
        }, this,
        function(response){

        	dashboardLinks(consolidateFlag);
        	var about = new Array();
        	var subsapp = new Array();
            var obj = eval("(" + response + ")");
//            if(obj.subscribedapplist) {
//                    for(var i = 0; i< obj.subscribedapplist.length; i++){
////                      	about[i]='<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="'+obj.subscribedapplist[i].appurlformat+'" onclick="closeMenu(4); window.open('+obj.subscribedapplist[i].appurlformat+'); return false;" >'+obj.subscribedapplist[i].appname+'</a></div>'
//                            subsapp[i]= '<div class="wrapperForMenu"><div style="padding: 8px 0 5px 5px;" /> <a href="'+obj.subscribedapplist[i].appurlformat+'" target="_blank" onclick="javascript:closeMenu(5);" id="'+obj.subscribedapplist[i].appname+'" wtf:qtip="'+obj.subscribedapplist[i].appname+'">'+obj.subscribedapplist[i].appname+'</a></div>';
//                        if(obj.subscribedapplist[i].appid == Wtf.appID.PM){
//                           Wtf.isPMSync = true;
//                        }
//                        if(obj.subscribedapplist[i].appid == Wtf.appID.CRM){// if CRM Integration is on for company
//                           Wtf.isCRMSync = true;
//                        }
//                        if(obj.subscribedapplist[i].appid == Wtf.appID.eUnivercity){// if LMS Integration is on for company
//                           Wtf.isLMSSync = true;
//                        }
//                        if(obj.subscribedapplist[i].appid == Wtf.appID.eClaim){// if Eclaim Integration is on for company
//                           Wtf.iseClaimSync = true;
//                        }
//                   }
//            }
//            if(obj.childapplist) {
//               var cnt = subsapp.length>0?subsapp.length:0;
//               var j = 0;
//               for(i = cnt; i< cnt+obj.childapplist.length; i++){
//                    subsapp[i] = '<div class="wrapperForMenu"><div style="padding: 8px 0 5px 5px;" /> <a href="'+obj.childapplist[j].appurlformat+'" target="_blank" onclick="javascript:closeMenu(5);" id="'+obj.childapplist[j].appname+'" wtf:qtip="'+obj.childapplist[j].appname_subdomain+'">'+obj.childapplist[j].appname_subdomain+'</a></div>';                        
//                    j++;
//               }
//            }
//            if(subsapp.length > 0)
                getSubsappLinks(obj);
//            else
//                (dash=document.getElementById("dash4")).parentNode.removeChild(dash);          
            if(obj.pstatus == 2){
            	for(var i = 0; i< obj.data.length; i++){
                	about[i]='<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="'+obj.data[i].url+'" target="_blank" onclick="closeMenu(4); window.open('+obj.data[i].url+'); return false;" >'+obj.data[i].title+'</a></div>'
                }

            	if(about.length > 0)
                    getAboutLinks(about);
            } else{

            	var mailto = "mailto:"+subdomain+"@deskera.com";
                    about[0]= '<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="'+mailto+'" onclick="javascript:closeMenu(4);" id="supportLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.support")+'">'+WtfGlobal.getLocaleText("acc.dashboard.support")+'</a></div>';
//        		about[1]= '<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="http://forum.deskera.com/" onclick="closeMenu(4); window.open(\'http://forum.deskera.com/\'); return false;" id="forumLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.forum")+'">'+WtfGlobal.getLocaleText("acc.dashboard.forum")+'</a></div>';
				about[1]= '<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="http://blog.deskera.com/" onclick="closeMenu(4); window.open(\'http://blog.deskera.com/\'); return false;" id="blogLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.blog")+'">'+WtfGlobal.getLocaleText("acc.dashboard.blog")+'</a></div>';
				about[2]= '<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="http://support.deskera.com/index.php/Deskera_Accounting_Help" onclick="javascript:closeMenu(4); window.open(\'http://support.deskera.com/index.php/Deskera_Accounting_Help\'); return false;" id="accountPrefreanceLink" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.help")+'">'+WtfGlobal.getLocaleText("acc.dashboard.help")+'</a></div>';
                                about[3]= '<div class="wrapperForMenu"><div  style="padding: 8px 0 5px 5px;" /> <a href="#" onclick="javascript:showVesionInfo(4); " id="vesioninfo" wtf:qtip="'+WtfGlobal.getLocaleText("acc.dashboard.TT.version")+'">'+WtfGlobal.getLocaleText("acc.dashboard.version")+'</a></div>';
                                getAboutLinks(about);

//				(dash=document.getElementById("dash4")).parentNode.removeChild(dash);
            }
        },
        function(request, response){
        	dashboardLinks(consolidateFlag);
        });
    } else {
        abortMaintainanceCall();
        dashboardLinks(consolidateFlag);
    }
	if(loginname=='demo'){
        addToXCuts("#", "Sign Up", "loadSignupPage()",'0','Sign Up');
    }
}

function addToXCuts(u, t, eh,no,tip,id){
    t=t.replace(/ /g, "&nbsp;");
    if(no=='0') {
        Wtf.DomHelper.append('signupLink','<a ' + (id ? ('id="' + id) : ('')) + '" ' + (eh ? ('onclick="' + eh) : ('target="_blank')) + '" href="' + u + '" wtf:qtip=\'' + tip+ '\' ">' + t + '</a> |');
    }
    else
        Wtf.DomHelper.append('signupLink','<a ' + (id ? ('id="' + id) : ('')) + '" '+ (eh ? ('onclick="' + eh) : ('target="_blank')) + '" href="' + u + '" wtf:qtip=\'' + tip+ '\'>' + t + '</a> ');
}

function setValidUserVariables(obj) {
    var values = eval ('(' + obj + ')');
    Wtf.CurrencySymbol = values.currencysymbol;
    var mailtoId = values.supprotlink;
    addToXCuts("mailto:" + values.supprotlink, "Support");
    document.getElementById('companyLogo').alt = values.companyname;
    if(Wtf.UPerm.Company !== undefined && !WtfGlobal.EnableDisable(Wtf.UPerm.Company, Wtf.Perm.Company.EditCompany)){
        document.getElementById('companyLogo').style.cursor="pointer";
        document.getElementById('companyLogo').onclick = loadAdminPage.createDelegate(this, ["3"]);
    }
    document.getElementById('Deskeratitle').text = values.companyname+WtfGlobal.getLocaleText("acc.field.Workspace-Deskera");
}

//[sy]
//function display(){
//    if(!WtfGlobal.EnableDisable(Wtf.UPerm.Company, Wtf.Perm.Company.EditCompany)){
//        loadAdminPage(3);
//    } else {
//        document.getElementById('companyLogo').title="";
//    }
//}
//ends
function globalchatPublishHandler(msg) {
    if(Wtf.getCmp('contactsview')!=null)
        Wtf.getCmp('contactsview').chatPublishHandler(msg);
    else{
         var temp = eval('(' + msg.data.data + ')');
         var temp1 = Wtf.decode(temp.data[0]).data;
         if(temp1[0].mode != "msg") {
              if(temp1[0].mode == "offline"){
                  var _chatWin = Wtf.getCmp('chatWin' + temp1[1].userid);
                  if (_chatWin!=null) {
                      _chatWin.setIconClass("K-iconOffline");
                  }
              }
              else if(temp1[0].mode == "online"){
                    if(temp1[1].status == "request") {
                        Wtf.Ajax.requestEx({
                            url: Wtf.req.prt + "getFriendListDetails.jsp",
                            params: {
                                userid: loginid,
                                mode : '2',
                                remoteUser: temp1[1].userid
                            }
                        }, this);
                    }
               }
         }else{
            var _chatWin = Wtf.getCmp('chatWin' + temp1[0].id);
            if (_chatWin) {
                _chatWin.insertmsg(temp1[0].message, 2);
                _chatWin.show();
            } else {
                var winChat = new Wtf.ChatWindow({
                    id: "chatWin" + temp1[0].id,
                    remotepersonid: temp1[0].id,
                    remotepersonname: temp1[0].uname,
                    iconCls: "K-icon",
                    title: WtfGlobal.getLocaleText("acc.field.Conversationwith") + temp1[0].uname,
                    chatstore: false/*,
                    node: tempNode*/
                })
                winChat.show();
                Wtf.getCmp('chatWin' + temp1[0].id).insertmsg(temp1[0].message, 2);
            }
         }
    }

}
//
//TODO function printButton(obj,moduleName,mode)
//{
//    var printArr = [];
//    var print = new Wtf.Action({
//        text: "Print",
//        tooltip:{
//            text:"Print "+moduleName
//            },
//        iconCls: 'pwnd printButtonIcon',
//        handler: function() {
//            obj.PrintPriview("print",mode);
//        },
//        scope: this
//    });
//    printArr.push(print);
//    var printSel = new Wtf.Action({
//        text: "Print Selected",
//        disabled:true,
//        tooltip:{
//            text:"Print selected "+moduleName
//            },
//        iconCls: 'pwnd printButtonIcon',
//        handler: function() {
//            obj.exportSelected("print",mode);
//        },
//        scope: this
//    });
//    printArr.push(printSel);
//    obj.tbarPrint=new Wtf.Toolbar.Button({
//        iconCls: 'pwnd printIcon',
//        tooltip: {text: "Print "+moduleName+" details."},
//        scope: this,
//        text:"Print",
//        id:'print'+mode,
//        menu: printArr
//        /*,
//        menu: exportArr*/
//    });
//    return obj.tbarPrint;
//}
//
//function printButtonR(obj,moduleName,mode)
//{
//    obj.tbarPrintR=new Wtf.Toolbar.Button({
//        iconCls: 'pwnd printIcon',
//        tooltip: {text: "Print "+moduleName+" details."},
//        scope: this,
//        text:"Print",
//        id:'print'+mode,
//        handler: function() {
//            obj.PrintPriview("print",mode);
//        }
//    });
//    return obj.tbarPrintR;
//}
//function exportWithTemplate(obj,type,name,frm,to,report,grid,selectionJson,sortField,sortDir,filterComboName,filterComboValue,comboDisplayValue) {
//        obj.pdfStore =new Wtf.data.Store({});
//        var mapid = null;
//        if(report.indexOf("crm")!=-1) {
//            obj.pdfStore=filPdfStore(obj,obj.gridcm,type);
//            mapid=report.substring(3,report.length);
//        } else
//            obj.pdfStore=filPdfStore(obj,grid.getColumnModel(),type);
//        var jsonGrid =genJsonForPdf(obj);
//        if(type == "pdf") {
//            new Wtf.selectTempWin({
//                type:type,
//                cd:(frm!="")?1:null,
//                name:name,
//                fromdate:frm,
//                todate:to,
//                year:obj.yearCombo != undefined ? obj.yearCombo.getValue() : null,
//                storeToload:obj.pdfStore,
//                gridConfig : jsonGrid,
//                grid:obj.EditorGrid,
//                mapid:mapid,
//                selectExport:selectionJson,
//                field:sortField,
//                dir:sortDir,
//                comboName:filterComboName,
//                comboValue:filterComboValue,
//                comboDisplayValue:comboDisplayValue,
//                json:(obj.searchJson!=undefined)?obj.searchJson:""
//            });
//        } else {
//            var expt =new Wtf.ExportInterface({
//                type:type,
//                cd:(frm!="")?1:null,
//                json:(frm=="")?obj.searchJson:"",
//                fromdate:frm,
//                todate:to,
//                year:obj.yearCombo != undefined ? obj.yearCombo.getValue() : null,
//                name:name,
//                mapid:mapid,
//                selectExport:selectionJson,
//                field:sortField,
//                dir:sortDir,
//                comboName:filterComboName,
//                comboValue:filterComboValue,
//                comboDisplayValue:comboDisplayValue,
//                pdfDs:obj.pdfStore
//            });
//            expt.show();
//        }
//}
//function filPdfStore(obj,column,type) {
//    var k=1;
//    for(i=1 ; i<column.getColumnCount() ; i++) { // skip row numberer
//      if(column.config[i].hidden!=true) {
//        if( column.config[i].pdfwidth!=undefined && column.config[i].pdfwidth!=null) {
//            var aligned=column.config[i].align;
//            var title;
//            var xlsheader;
//            if(aligned==undefined)
//                aligned='center';
//            if(type!="xls"){
//                if(column.config[i].title==undefined)
//                    title=column.config[i].dataIndex;
//                else
//                    title=column.config[i].title;
//            } else {
//                if(column.config[i].title==undefined) {
//                    title=column.config[i].dataIndex;
//                } else {
//                    title=column.config[i].title;
//                }
//                if(column.config[i].headerName!=undefined)
//                    xlsheader=column.config[i].headerName;
//                else
//                    xlsheader=column.config[i].header;
//            }
//            obj.newPdfRec = new Wtf.data.Record({
//                header : title,
//                xlsheader:xlsheader,
//                title : WtfGlobal.HTMLStripper(column.config[i].header),
//                width : column.config[i].pdfwidth,
//                align : aligned,
//                index : k
//            });
//            obj.pdfStore.insert(obj.pdfStore.getCount(), obj.newPdfRec);
//            k++;
//        }
//      }
//    }
//    return obj.pdfStore;
//}
//function genJsonForPdf(obj) {
//    var jsondata = "{ data:[";
//    for(i=0;i<obj.pdfStore.getCount();i++) {
//        var record = obj.pdfStore.getAt(i);
//        jsondata+="{'header':'" + record.data.header + "',";
//        if(record.data.align=="right" && record.data.title.indexOf("(")!=-1) {
//            record.data.title=record.data.title.substring(0,record.data.title.indexOf("(")-1);
//        }
//        jsondata+="'title':'" + record.data.title + "',";
//        jsondata+="'width':'" + record.data.width + "',";
//        jsondata+="'align':'" + record.data.align + "'},";
//    }
//    var trmLen = jsondata.length - 1;
//    var finalStr = jsondata.substr(0,trmLen);
//    finalStr+="]}";
//    return finalStr;
//}
function showPersnProfile1(){
    var p = Wtf.getCmp("changepasswordlinkforaccounting");
    /**
     * ERP-40117
     * PasswordPolicy feature for company is added.
     * If password policy is set for company then default password validation variables will be initiazed according to password policy.
     */
    Wtf.Ajax.request({
        method: 'POST',
        url: "ProfileHandler/getPasswordPolicy.do",
        scope: this,
        success: function (response, req) {
            var dataobj = Wtf.decode(response.responseText);
            var passwordPolicy = dataobj.data;
            if (passwordPolicy.data) {
                Wtf.passwordNumberCount = passwordPolicy.data.minnum;
                Wtf.passwordAlphabetCount = passwordPolicy.data.minalphabet;
                Wtf.mincharPass = passwordPolicy.data.minchar;
                Wtf.maxcharPass = passwordPolicy.data.maxchar;
                Wtf.isPolicySet = passwordPolicy.data.setpolicy;
                Wtf.passwordHasSpecial = passwordPolicy.data.specialchar;
            }
            if (!p) {
                new Wtf.changepasswordwin().show();
            }
        },
        failure: function (response) {
            var msg = WtfGlobal.getLocaleText("acc.common.msg1");
            if (response.msg)
                msg = response.msg;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
            this.close();
        }
    });
}
function showPersnProfile(){
    var p = Wtf.getCmp("updateProfileWin");
    if(!p){
        new Wtf.common.UpdateProfile({}).show();
    }
}
function invokeTagSearch(e){
    Wtf.getCmp("textSearch").setValue("tag:"+e.innerHTML);
    Wtf.getCmp("searchTopPanel").onButtonClick();
}

function displayChatList(){
   var chatListWin = Wtf.getCmp("chatListWindow");
   var leftoffset = document.getElementById('chatlistcontainer').offsetLeft;
   var topoffset = document.getElementById('chatlistcontainer').offsetTop+document.getElementById('chatlistcontainer').offsetHeight;
   if(chatListWin.innerpanel==null||chatListWin.hidden==true){
       chatListWin.setPosition(leftoffset,topoffset);
       chatListWin.show();
       chatListWin.showChatList();
   }else{
       chatListWin.hide();
   }
}

function subscribeForProject(){
     Wtf.Ajax.requestEx({
        url: "admin.jsp",
        params:{
            action:3,
            mode:4
        }
       }, this,
       function(result, req){
           var res = eval("(" + result + ")");
           new Wtf.ProjSubscrib({
               rate : res.data[0].projrate,
               guid: res.data[0].subid,
               formcontent: res.data[0].formcontent
           }).show();
       },function(result, req){
    });

}

function subscribeInvoice(subid) {
    var subinvRecord = new Wtf.data.Record.create([
        {name: "num"},{name: "paymentdate",type :"date",dateFormat: 'Y-m-d'},{name: "subid"},
        {name: "amount"},{name:"receipt"}
    ]);
    var subinvReader = new Wtf.data.KwlJsonReader({
        root: "data",
        totalProperty: 'count'
    }, subinvRecord);

    var invoiceDS = new Wtf.data.Store({
        url: "admin.jsp",
        reader: subinvReader,
        method: 'POST',
        baseParams: {
            action: 3,
            mode: 3,
            subid : subid
        }
    });

    var columns = new Wtf.grid.ColumnModel([{
            header: WtfGlobal.getLocaleText("acc.prList.invNo"),
            dataIndex: 'num',
            renderer: function(val) {
                return "<a href = '#' class='printInvoice' > " + val + "</a>";
            }
        },{
            header: WtfGlobal.getLocaleText("acc.agedPay.invoiceDate"),
            dataIndex: 'paymentdate',
            renderer: function(val) {
                return val.format(WtfGlobal.getDateFormat())
            }
        },{
            header: WtfGlobal.getLocaleText("acc.receipt.1"),
            dataIndex: 'receipt',
            renderer: function(val) {
                if(val.length>0)
                    return "<a href = '#' class='printInvoice' > " + val + "</a>";
                else
                    return "<span>Pending<span>";
            }
        },{
            header: WtfGlobal.getLocaleText("acc.balanceSheet.Amount"),
            dataIndex: 'amount',
            align : "right"
        }
    ]);
    var invoiceGrid = new Wtf.grid.GridPanel({
        store: invoiceDS,
        cm: columns,
        layout: 'fit',
        autoHeight : true,
        sm: new Wtf.grid.RowSelectionModel({singleSelect:true}),
        border : false,
        loadMask : true,
        viewConfig: {
            forceFit:true
        }
    });
    invoiceGrid.on("cellclick",function(grid,row,col,e) {
        if(e.target.className == "printInvoice") {
            var rec = grid.getStore().getAt(row);
            if(col == 0)
                printInvoice(rec.data.num, rec.data.subid, 1,rec.data.paymentdate.format('Y-m-d'));
            else if(col==2)
                printInvoice(rec.data.receipt, rec.data.subid, 2, rec.data.num);
        }
    }, this);
    var GridPanel = new Wtf.common.KWLListPanel({
        title: WtfGlobal.getLocaleText("acc.field.Followinginvoices/receiptsaregeneratedforselectedsubscription"),
        autoLoad: false,
        autoScroll:true,
        paging: false,
        layout: 'fit',
        items: [invoiceGrid]
    });
    var invoiceWindow = new Wtf.Window({
        width:410,
        height: 340,
        iconCls: 'iconwin',
        resizable : false,
        bodyStyle : "background-color:white;",
        id : 'subinvoice'+this.id,
        modal:true,
        title:WtfGlobal.getLocaleText("acc.ccReport.tab3"),
        buttons: [{
            anchor : '90%',
            text: WtfGlobal.getLocaleText("acc.CLOSEBUTTON"),
            handler:function() {
                Wtf.getCmp('subinvoice'+this.id).close();
            },
            scope:this
        }],
        items:[GridPanel]
    }).show();
    invoiceDS.load();
}

/*  Wtf.Profile: Start  */
Wtf.Profile = function(){
     config = {
        title: WtfGlobal.getLocaleText("acc.profile.tabTitle"),
        id: "pprofwin",
        closable: true,
        modal: true,
        iconCls: 'iconwin',
        width: 460,
        height: 450,
        resizable: false,
        layout: 'border',
        buttonAlign: 'right',
        renderTo: document.body,
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.common.update"),
            scope: this,
            handler: function(){
                if(!(this.Form.form.isValid()))
                    return;

                this.Form.form.submit({
                    waitMsg: WtfGlobal.getLocaleText("acc.msgbox.50"),
                    scope: this,
                    failure: function(frm, action){
                        msgBoxShow(155, 1);
                        this.close();
                    },
                    success: function(frm, action){
                        var userRes = WtfGlobal.getLocaleText("acc.field.ProfileUpdatedSuccessfully");
                        var resObj = eval("(" + action.response.responseText + ")");
                        if(resObj.success == "true") {
                            if(resObj.data != "")
                                userRes += resObj.data;//message prepared on server side
                            msgBoxShow([WtfGlobal.getLocaleText("acc.common.success"), userRes], 3);
                            var dateFormat = this.dateformatstore.getAt(this.dateformatstore.find("id",this.dtCombo.getValue()));
                            Wtf.pref.DateFormatid = dateFormat.data.id;
                            Wtf.pref.DateFormat = dateFormat.data.dateformat;
                        }
                        else
                        {
                            msgBoxShow(155, 1);
                        }
                        this.close();
                    }
                });
            }
        }, {
            text: WtfGlobal.getLocaleText("acc.msgbox.cancel"),
            scope: this,
            handler: function(){
                this.close();
            }
        }]
    }
    Wtf.Profile.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.Profile, Wtf.Window, {
    initComponent: function(config){
/*       this.dateformatstore = new Wtf.data.JsonStore({
            url:"admin.jsp?mode=20&action=0",
            root:'data',
            fields : ['id','name','dateformat']
        });
        this.dateformatstore.load();
        chktimezoneload();
*///        Wtf.timezoneStore.load();
        Wtf.Profile.superclass.initComponent.call(this, config);
         Wtf.Ajax.requestEx({
            url: Wtf.req.prf + "user/getuserdetails.jsp?",
            params : {
	          mode: 1
            },
            method: 'POST'},
            this,
            function(result, request) {
               // msgBoxShow(156, 1);
                 Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.error"),
                    msg: WtfGlobal.getLocaleText("acc.field.ErrorRetrievingPersonalInformation"),
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.INFO
                },
            function(result, request) {
                try {
                    var data = result;
                        if (data) {
                            this.createForm(Wtf.util.JSON.decode(data).data[0]);
                        }
                    }
                catch (e) {

                }
            });
        });
//
//        Wtf.Ajax.request({
//            method: "POST",
//            url: Wtf.req.prf + "user/getuserdetails.jsp?",
//            params: {
//                mode: 1
//            },
//            success: function(result, request){
//                try {
//                    var data = result.responseText.trim();
//                    if (data) {
//                        this.createForm(Wtf.util.JSON.decode(data).data[0]);
//                    }
//                }
//                catch (e) {
//
//                }
//            },
//            failure: function(){
//                Wtf.MessageBox.Show({
//                    title: 'Error',
//                    msg: 'Error Retrieving Personal Information.',
//                    buttons: Wtf.MessageBox.OK,
//                    icon: Wtf.MessageBox.INFO
//                });
//            },
//            scope: this
//        });
    },
    createForm: function(params){
        this.Form = new Wtf.form.FormPanel({
            method: 'POST',
            url: Wtf.req.prf + 'user/updateProfile.jsp?',
            waitMsgTarget: true,
            fileUpload: true,
            border: false,
            labelWidth: 120,
            cls: 'scrollform',
            defaults: {
                width: 240
            },
            defaultType: 'textfield',
            items: [{
                fieldLabel: WtfGlobal.getLocaleText("acc.field.UserID*"),
                name: 'username',
                disabled :true,
                allowBlank: false,
                value: params.username,
                disabled: true
            }, {
                fieldLabel: WtfGlobal.getLocaleText("acc.field.EmailAddress*"),
                allowBlank: false,
                name: 'emailid',
                vtype: 'email',
                value: params.emailid
            }, {
                fieldLabel: WtfGlobal.getLocaleText("acc.profile.fName"),
                name: 'fname',
                validator:WtfGlobal.validateUserName,
                allowBlank: false,
                value: params.fname
            }, {
                fieldLabel: WtfGlobal.getLocaleText("acc.field.LastName*"),
                name: 'lname',
                allowBlank: false,
                validator:WtfGlobal.validateUserName,
                value: params.lname
            }, {
                fieldLabel: WtfGlobal.getLocaleText("acc.profile.userPic"),
                name: 'image',
                height: 24,
                inputType: 'file'
            }, {
                fieldLabel: WtfGlobal.getLocaleText("acc.profile.contactNo"),
                name: 'contactno',
                id: 'updateContactNo',
                value: params.contactno
            },{xtype:"textarea",
                fieldLabel: WtfGlobal.getLocaleText("acc.profile.address"),
                height: 80,
                name: 'address',
                id: 'updateAddress',
                value: params.address
            },{xtype:"textarea",
                fieldLabel: WtfGlobal.getLocaleText("acc.profile.abtMe"),
                id: 'updateAboutMe',
                height: 80,
                name: 'about',
                value: params.aboutuser
            },this.dtCombo=new Wtf.form.ComboBox({
                xtype:'combo',
                fieldLabel : WtfGlobal.getLocaleText("acc.profile.dateFmt"),
                store : this.dateformatstore,
                readOnly : true,
                displayField:'name',
                valueField:'id',
                value: Wtf.pref.DateFormatid,
                editable:false,
                hiddenName: 'dateformat',
                mode: 'local',
                width: 240,
                triggerAction: 'all',
                emptyText : WtfGlobal.getLocaleText("acc.field.Selectatype"),
                allowBlank:false
            }),
            this.locale = new Wtf.form.ComboBox({
                fieldLabel:WtfGlobal.getLocaleText("acc.profile.timeZone"),
                mode:'local',
                scope: this,
                triggerAction:'all',
                typeAhead:true,
                value: params.timezone,
                editable:false,
                hiddenName: 'timezone',
                blankText: WtfGlobal.getLocaleText("acc.field.SelectTime-Zone"),
                store: Wtf.timezoneStore,
                displayField:'name',
                valueField:'id'

            })
            ,{fieldLabel: WtfGlobal.getLocaleText("acc.field.OldPassword"),
                name: 'oldpass',
                inputType: 'password',
                minLength:4,
                maxLength:32,
                value: params.newpass
            }, {fieldLabel: WtfGlobal.getLocaleText("acc.changePass.newPass"),
                name: 'newpass',
                inputType: 'password',
                minLength:4,
                maxLength:32,
                value: params.newpass
            }, {fieldLabel: WtfGlobal.getLocaleText("acc.field.RetypePassword"),
                name: 'renewpass',
                inputType: 'password',
                minLength:4,
                maxLength:32,
                value: params.renewpass
            }]
        });
        Wtf.getCmp("updateContactNo").on("change", function(){
           Wtf.getCmp("updateContactNo").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("updateContactNo").getValue()));
        });
        Wtf.getCmp("updateAddress").on("change", function(){
           Wtf.getCmp("updateAddress").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("updateAddress").getValue()));
        });
        Wtf.getCmp("updateAboutMe").on("change", function(){
           Wtf.getCmp("updateAboutMe").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("updateAboutMe").getValue()));
        });
        var parentP = Wtf.getCmp("profilewin");
        if (parentP) {
            parentP.add(this.Form);
            this.doLayout();
        }
    },

    onRender: function(config){
        Wtf.Profile.superclass.onRender.call(this, config);
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml('Update Profile','Update Profile')
        }, {
            region: 'center',
            border: false,
            id: 'profilewin',
            bodyStyle: 'background:#f1f1f1;font-size:10px;',
            layout: 'fit',
            items: this.Form
        });
    }
});/*  Wtf.Profile: End*/

Wtf.changepasswordwin = function (config){
    Wtf.apply(this,config);
    Wtf.changepasswordwin.superclass.constructor.call(this,{
        buttons:[{
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //"Save",
            scope:this,
            handler:function () {
                if(!this.AddEditForm.form.isValid())
                {
                    return;
                } else {
                    var currentpass=hex_sha1(Wtf.getCmp('lidcurrentpass').getValue());
                    var cngpass=hex_sha1(Wtf.getCmp('lidnewpass123').getValue());
                    var param={
                        mode:14,
                        currentpassword:currentpass,
                        changepassword:cngpass
                    }
                    Wtf.Ajax.requestEx({
//                        url:Wtf.req.base+'UserManager.jsp',
                        url : "ACCCommon/changeUserPassword.do",
                        params:param
                    },this,
                    function(req,res){
                        var restext=req;
                        if(restext.success){
                            this.close();
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.changePass.tabTitle"),WtfGlobal.getLocaleText("acc.rem.174")],0);
                        } else
                            /**
                             * To diplay the response sent form java side.
                             * ERP-41621.
                             */
                        	WtfComMsgBox([WtfGlobal.getLocaleText("acc.changePass.tabTitle"),restext.msg],1);

                    },
                    function(req){
                        var restext=req;
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.changePass.tabTitle"),WtfGlobal.getLocaleText("acc.rem.173")],1);
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
/**
 * ERP-40117
 * Entered new password will be validated agaist password policy set for company.
 * @param {type} checkString
 * @returns {Boolean|@var;key|key.key}
 */
Wtf.validatePassword = function (checkString) {
    if (Wtf.isPolicySet) {
        var passwordText = WtfGlobal.getLocaleText("acc.common.password");
        if (Wtf.passwordAlphabetCount > 0) {
            passwordText += " " + WtfGlobal.getLocaleText("acc.common.atleast") + " " + Wtf.passwordAlphabetCount + " " + WtfGlobal.getLocaleText("acc.common.alphabet");
        }
        if (Wtf.passwordNumberCount > 0) {
            passwordText += " " + WtfGlobal.getLocaleText("acc.nee.29") + " " + WtfGlobal.getLocaleText("acc.common.atleast") + " " + Wtf.passwordNumberCount + " " + WtfGlobal.getLocaleText("acc.common.numbers");
        }
        if (Wtf.passwordHasSpecial) {
            passwordText += " " + WtfGlobal.getLocaleText("acc.nee.29") + " " + WtfGlobal.getLocaleText("acc.common.specialchar");
        }
        var regExp = /^[A-Za-z]$/;
        var alphaCnt = 0;
        if (checkString)
        {
            for (var i = 0; i < checkString.length; i++)
            {
                if (checkString.charAt(i).match(regExp))
                {
                    alphaCnt++;
                }
            }
        }
        var numCnt = 0;
        if (checkString)
        {
            for (var i = 0; i < checkString.length; i++)
            {
                if (!isNaN(checkString.charAt(i)))
                {
                    numCnt++;
                }
            }
        }
        var hasSpecial = false;
        if (Wtf.passwordHasSpecial) {
            var spChar = checkString.match(/[!@#\$%\^&\*\(\)\-_=\+]+/i);
            if (spChar != null) {
                hasSpecial = true;
            }
        }
        if (alphaCnt >= Wtf.passwordAlphabetCount && numCnt >= Wtf.passwordNumberCount && hasSpecial) {
            return true;
        } else {
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),passwordText],2);
            return passwordText;
        }
    } else {
        return true;
    }
}

Wtf.extend(Wtf.changepasswordwin,Wtf.Window,{
    layout:"border",
    modal:true,
    title:WtfGlobal.getLocaleText("acc.changePass.tabTitle"), //"Change Password",//WtfGlobal.getLocaleText("acc.changePass.tabTitle")
    id:'changepasswordlinkforaccounting',
    width:450,
    height:320,
    resizable:false,
    iconCls: "pwnd favwinIcon",
    initComponent:function (){
        Wtf.changepasswordwin.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetAddEditForm();
        Wtf.form.Field.prototype.msgTarget = 'side';
        this.add(this.northPanel);
        this.add(this.AddEditForm);
    },
    GetNorthPanel:function (){
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:75,
            border:false,
            bodyStyle:"background:white;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(WtfGlobal.getLocaleText("acc.changePass.tabTitle"),WtfGlobal.getLocaleText("acc.changePass.desc"),'../../images/createuser.png',false,'0px 0px 0px 0px')
        });
    },
    GetAddEditForm:function (){
        this.AddEditForm = new Wtf.form.FormPanel({
            region:"center",
            border:false,
            defaults:{width:200,allowBlank:false,inputType:'password'},
            defaultType : 'textfield',
            bodyStyle:"background-color:#f1f1f1;padding:35px",
            items:[{
                    fieldLabel:WtfGlobal.getLocaleText("acc.changePass.userName"),  //"User Name",
                    value:loginname,
                    inputType : 'text',
                    name:"uname",
                    readOnly:true
                },{
                    fieldLabel:WtfGlobal.getLocaleText("acc.changePass.curPass") + "*",  //"Current Password*",
                    id:'lidcurrentpass',
                    minLength:4,
                    maxLength:32,
                    name:"currentpassword"
                },{
                    fieldLabel:WtfGlobal.getLocaleText("acc.changePass.newPass") + "*",  //"New Password*",
                    name:"newpassword",
                    id:'lidnewpass',
                    /**
                     * ERP-40117
                     * If password policy is set for company then 
                     * accepts min and max char length according to password policy set.
                     */
                    maxLength: Wtf.isPolicySet ? Wtf.maxcharPass : 32,
                    minLength: Wtf.isPolicySet ? Wtf.mincharPass : 4,
                    validator: Wtf.validatePassword,
                    allowBlank:false
                },{
                    fieldLabel:WtfGlobal.getLocaleText("acc.changePass.retype") + "*",  //'Retype new Password*',
                    name:'pass',
                    inputType:'password',
                    id:'lidnewpass123',
                    initialPassField:'lidnewpass',
                    vtype:'password',
                    /**
                     * ERP-40117
                     * If password policy is set for company then 
                     * accepts min and max char length according to password policy set.
                     */
                    maxLength: Wtf.isPolicySet ? Wtf.maxcharPass : 32,
                    minLength: Wtf.isPolicySet ? Wtf.mincharPass : 4,
                    validator: Wtf.validatePassword,
                    allowBlank:false
            }]
        });
    }
});

//For Active Date Range
Wtf.activedaterangewin = function (config){
    Wtf.apply(this,config);
    Wtf.activedaterangewin.superclass.constructor.call(this,{
        buttons:[{
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //"Save",
            scope:this,
            handler:function () {
                if(!this.ActiveDateRangeForm.form.isValid())
                {
                    return;
                }else {
                    this.sDate=this.fromDate.getValue();
                    this.eDate=this.toDate.getValue();
                    if(this.sDate == null || this.sDate == undefined || this.sDate == ""){
                        if(this.eDate != null && this.eDate != undefined && this.eDate != ""){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.activeDateRange.tabTitle"),WtfGlobal.getLocaleText("acc.msgbox.PleaseselectFromdate")],2);
                            return;
                        }
                    } 
                    if(this.eDate == null || this.eDate == undefined || this.eDate == ""){
                        if(this.sDate != null && this.sDate != undefined && this.sDate != ""){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.activeDateRange.tabTitle"),WtfGlobal.getLocaleText("acc.msgbox.PleaseselectTodate")],2);
                            return;
                        }
                    }
                    if((this.sDate == null || this.sDate == undefined || this.sDate == "") && (this.eDate == null || this.eDate == undefined || this.eDate == "") || this.sDate<this.eDate){
                        var fromdate=WtfGlobal.convertToGenericDate(this.fromDate.getValue());
                        var todate=WtfGlobal.convertToGenericDate(this.toDate.getValue());
                        var param={
                            mode:14,
                            fromdate:fromdate,
                            todate:todate
                        }
                        Wtf.Ajax.requestEx({
                            url : "ACCAccount/addActiveDateRange.do",
                            params:param
                        },this,
                        function(req,res){
                            var restext=req;
                            if(restext.success){
                                Wtf.account.companyAccountPref.activeDateRangeToDate=todate;
                                Wtf.account.companyAccountPref.activeDateRangeFromDate=fromdate;
                                this.close();
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.activeDateRange.tabTitle"),WtfGlobal.getLocaleText("acc.rem.227")],0);
                            } else
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.activeDateRange.tabTitle"),WtfGlobal.getLocaleText("acc.rem.226")],1);

                        },
                        function(req){
                            var restext=req;
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.activeDateRange.tabTitle"),WtfGlobal.getLocaleText("acc.rem.226")],1);
                        });
                    } else {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.activeDateRange.tabTitle"),WtfGlobal.getLocaleText("acc.msgbox.1")],2);
                    }
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

Wtf.extend(Wtf.activedaterangewin,Wtf.Window,{
    layout:"border",
    modal:true,
    title:WtfGlobal.getLocaleText("acc.activeDateRange.tabTitle"), //"System - Preferences - Active Date Range",
    id:'activedaterangelinkforaccounting',
    width:600,
    height:200,
    resizable:false,
    iconCls: "pwnd favwinIcon",
    initComponent:function (){
        Wtf.activedaterangewin.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetActiveDateRangeForm();
        Wtf.form.Field.prototype.msgTarget = 'side';
        this.add(this.northPanel);
        this.add(this.ActiveDateRangeForm);
    },
    GetNorthPanel:function (){
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:75,
            border:false,
            bodyStyle:"background:white;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(WtfGlobal.getLocaleText("acc.activeDateRange.tabTitle"),WtfGlobal.getLocaleText("acc.activeDateRange.desc"),'../../images/createuser.png',false,'0px 0px 0px 0px')
        });
    },
    GetActiveDateRangeForm:function (){
        this.fromDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.activeDateRange.fromDate"),//'From Date',
            name:'fromdate',
            format:WtfGlobal.getOnlyDateFormat(),
//            value:Wtf.serverDate,
            id:"fromdateid",
            width:160
//            allowBlank:false
        });
        
        this.toDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.activeDateRange.toDate"),//'To Date',
            name:'todate',
            format:WtfGlobal.getOnlyDateFormat(),
//            value:Wtf.serverDate,
            id:"todateid",
            width:160
//            allowBlank:false
        });
        
        this.ActiveDateRangeForm = new Wtf.form.FormPanel({
            region:"center",
            border:false,
            bodyStyle:"background-color:#f1f1f1;padding:20px",
            items:[{
                layout:'column',
                border:false,
                items:[{
                    layout:'form',
                    columnWidth:0.55,
                    border:false,
                    labelWidth:80,
                    items:[this.fromDate]
                },{
                    layout:'form',
                    columnWidth:0.45,
                    border:false,
                    labelWidth:60,
                    items:[this.toDate]
                }]
            }]            
        });
        
        Wtf.Ajax.requestEx({
            method: 'POST',
            url : "ACCCompanyPref/getExtraCompanyPreferences.do"
            },
            this,
            function (result, req){
                if(result.data.fromdate!=null && result.data.todate!=null){
                    this.fromDate.setValue(result.data.fromdate);
                    this.toDate.setValue(result.data.todate);
                    Wtf.getCmp('fromdateid').setValue(new Date(result.data.fromdate));                    
                    Wtf.getCmp('todateid').setValue(new Date(result.data.todate));
                }
            },function(res,req){
                
            }
        );
    }
});

//For Add Exchange Rate
Wtf.AddExchangeRateWindow = function (config){
    this.rec=config.record?config.record:"";
    this.transactiondate=config.transactiondate?config.transactiondate:"";
    this.superthis=config.superthis?config.superthis:"";
    Wtf.apply(this,config);
    Wtf.AddExchangeRateWindow.superclass.constructor.call(this,{
        buttons:[{
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //"Save",
            scope:this,
            handler:function () {
                if(!this.addExchangeRateForm.form.isValid()){
                    return;
                }else {
                    this.sDate=this.fromDate.getValue();
                    this.eDate=this.toDate.getValue();
                    this.excRate=this.exchangeRate.getValue();
                    if (this.sDate<=this.eDate) {
                        var fromdate=WtfGlobal.convertToGenericStartDate(this.fromDate.getValue());
                        var todate=WtfGlobal.convertToGenericEndDate(this.toDate.getValue());
                        var arr=[];
                        if(this.rec!=null&& this.rec!=undefined && this.rec!=""){
                            var recarr=[];
                            recarr.push("'id':'"+this.rec.data.erdid+"'");
                            recarr.push("'companyid':'"+this.rec.data.companyid+"'");
                            recarr.push("'currencycode':'"+this.rec.data.currencycode+"'");
                            recarr.push("'fromcurrencyid':'"+this.rec.data.fromcurrencyid+"'");
                            recarr.push("'fromcurrency':'"+this.rec.data.fromcurrency+"'");
                            recarr.push("'tocurrencyid':'"+this.rec.data.currencyid+"'");
                            recarr.push("'tocurrency':'"+this.rec.data.currencyname+"'");
                            recarr.push("'exchangerate':'"+this.excRate+"'");
                            recarr.push("'newexchangerate':'"+this.excRate+"'");
                            recarr.push("'applydate':'"+fromdate+"'");
                            recarr.push("'todate':'"+todate+"'");
                            recarr.push("'modified':'"+true+"'");
                            arr.push("{" + recarr.join(",") + "}");
                            var param={
                                mode:202,
                                data:"[" + arr.join(',') + "]"
                            }
                            Wtf.Ajax.requestEx({
                                url: "ACCCurrency/saveCurrencyExchange.do",
                                params: param
                            }, this, this.genSuccessResponse, this.genFailureResponse);
                        }
                    }else {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.addExchangeRate.tabTitle"),WtfGlobal.getLocaleText("acc.msgbox.1")],2);
                    }
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

Wtf.extend(Wtf.AddExchangeRateWindow,Wtf.Window,{
    layout:"border",
    modal:true,
    title:WtfGlobal.getLocaleText("acc.addExchangeRate.tabTitle"), //"Add Currency Exchange Rate",
    id:'addexchangerateforcurrency',
    width:450,
    height:280,
    resizable:false,
    iconCls: "pwnd favwinIcon",
    initComponent:function (){
        Wtf.AddExchangeRateWindow.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetAddExchangeRateForm();
        Wtf.form.Field.prototype.msgTarget = 'side';
        this.add(this.northPanel);
        this.add(this.addExchangeRateForm);
    },
    GetNorthPanel:function (){
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:75,
            border:false,
            bodyStyle:"background:white;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(WtfGlobal.getLocaleText("acc.addExchangeRate.tabTitle"),WtfGlobal.getLocaleText("acc.addExchangeRate.desc"),'../../images/accounting_image/currency-exchange.jpg',false,'0px 0px 0px 0px')
        });
    },
    GetAddExchangeRateForm:function (){
        this.fromDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.activeDateRange.fromDate"),//'From Date',
            name:'fromdate',
            format:WtfGlobal.getOnlyDateFormat(),
            value:this.transactiondate!=""?this.transactiondate:Wtf.serverDate,
            id:"fromdateid",
            width:180,
            allowBlank:false
        });
        
        this.toDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.activeDateRange.toDate"),//'To Date',
            name:'todate',
            format:WtfGlobal.getOnlyDateFormat(),
            value:this.getToDate(),
            id:"todateid",
            width:180,
            allowBlank:false
        });
        this.fromDate.on("change",this.checkDates,this);
        this.toDate.on("change",this.checkDates,this);
        
        this.exchangeRate= new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.addExchangeRate.ExchangeRate")+'*:',
            name: 'exchangerate',
            allowBlank:false,
            allowNegative:false,
            value:this.rec!=null && this.rec!=undefined && this.rec!="" ? this.rec.data.exchangerate : "",
            decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL,
            xtype:'numberfield',
            width:180,
            maxLength:15
        });
        
        this.addExchangeRateForm = new Wtf.form.FormPanel({
            region:"center",
            border:false,
            bodyStyle:"background-color:#f1f1f1;padding:20px",
            items:[{
                    layout:'form',
                    border:false,
                    labelWidth:100,
                    items:[this.fromDate,this.toDate,this.exchangeRate]
                }]
        });
    },
    getToDate:function (){
        var toDateVal=this.transactiondate!=""?this.transactiondate:Wtf.serverDate;
        toDateVal=toDateVal.add(Date.DAY, 30);
        return toDateVal;
    },
    checkDates : function(dateObj,newVal,oldVal){
        if(this.fromDate.getValue()>this.toDate.getValue()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"),WtfGlobal.getLocaleText("acc.agedPay.FromdateshouldnotbegreaterthanToDate")], 2);  //"From date should not be greater than To Date."
            dateObj.setValue(oldVal);
        } 
    },
    genSuccessResponse:function(response){
        if(response.dateexist){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.PeriodisOverlappingwithexistingExchangeRatePleaseEnterperiodcorrectly")],2);
        }else{
            // Reload the currency store and apply latest exchange rate
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),response.msg],response.success*2+1);
//            this.superthis.currencyStore.on('load',function(store){
//                var rec = WtfGlobal.searchRecord(store, this.rec.data.currencyid , "currencyid");
//                if(rec!=null && rec!=undefined && rec!=""){
//                    this.superthis.externalcurrencyrate=rec.data.exchangerate;s
//                    this.superthis.applyCurrencySymbol();
//                }
//            },this);
            this.superthis.currencyStore.load({params: {mode: 201, transactiondate: WtfGlobal.convertToGenericDate(this.transactiondate)}});
            this.superthis.Currency.setValue(WtfGlobal.getCurrencyID());
            this.superthis.externalcurrencyrate=0;
            this.superthis.applyCurrencySymbol();
            this.close();
        }
    },
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    }
});

///// SHA1 /////////////////

/*
 * A JavaScript implementation of the Secure Hash Algorithm, SHA-1, as defined
 * in FIPS PUB 180-1
 * Version 2.1a Copyright Paul Johnston 2000 - 2002.
 * Other contributors: Greg Holt, Andrew Kepert, Ydnar, Lostinet
 * Distributed under the BSD License
 * See http://pajhome.org.uk/crypt/md5 for details.
 */

/*
 * Configurable variables. You may need to tweak these to be compatible with
 * the server-side, but the defaults work in most cases.
 */
var hexcase = 0;  /* hex output format. 0 - lowercase; 1 - uppercase        */
var b64pad  = ""; /* base-64 pad character. "=" for strict RFC compliance   */
var chrsz   = 8;  /* bits per input character. 8 - ASCII; 16 - Unicode      */

/*
 * These are the functions you'll usually want to call
 * They take string arguments and return either hex or base-64 encoded strings
 */
function hex_sha1(s){return binb2hex(core_sha1(str2binb(s),s.length * chrsz));}
function b64_sha1(s){return binb2b64(core_sha1(str2binb(s),s.length * chrsz));}
function str_sha1(s){return binb2str(core_sha1(str2binb(s),s.length * chrsz));}
function hex_hmac_sha1(key, data){return binb2hex(core_hmac_sha1(key, data));}
function b64_hmac_sha1(key, data){return binb2b64(core_hmac_sha1(key, data));}
function str_hmac_sha1(key, data){return binb2str(core_hmac_sha1(key, data));}

/*
 * Perform a simple self-test to see if the VM is working
 */
function sha1_vm_test()
{
  return hex_sha1("abc") == "a9993e364706816aba3e25717850c26c9cd0d89d";
}

/*
 * Calculate the SHA-1 of an array of big-endian words, and a bit length
 */
function core_sha1(x, len)
{
  /* append padding */
  x[len >> 5] |= 0x80 << (24 - len % 32);
  x[((len + 64 >> 9) << 4) + 15] = len;

  var w = Array(80);
  var a =  1732584193;
  var b = -271733879;
  var c = -1732584194;
  var d =  271733878;
  var e = -1009589776;

  for(var i = 0; i < x.length; i += 16)
  {
    var olda = a;
    var oldb = b;
    var oldc = c;
    var oldd = d;
    var olde = e;

    for(var j = 0; j < 80; j++)
    {
      if(j < 16) w[j] = x[i + j];
      else w[j] = rol(w[j-3] ^ w[j-8] ^ w[j-14] ^ w[j-16], 1);
      var t = safe_add(safe_add(rol(a, 5), sha1_ft(j, b, c, d)),
                       safe_add(safe_add(e, w[j]), sha1_kt(j)));
      e = d;
      d = c;
      c = rol(b, 30);
      b = a;
      a = t;
    }

    a = safe_add(a, olda);
    b = safe_add(b, oldb);
    c = safe_add(c, oldc);
    d = safe_add(d, oldd);
    e = safe_add(e, olde);
  }
  return Array(a, b, c, d, e);

}

/*
 * Perform the appropriate triplet combination function for the current
 * iteration
 */
function sha1_ft(t, b, c, d)
{
  if(t < 20) return (b & c) | ((~b) & d);
  if(t < 40) return b ^ c ^ d;
  if(t < 60) return (b & c) | (b & d) | (c & d);
  return b ^ c ^ d;
}

/*
 * Determine the appropriate additive constant for the current iteration
 */
function sha1_kt(t)
{
  return (t < 20) ?  1518500249 : (t < 40) ?  1859775393 :
         (t < 60) ? -1894007588 : -899497514;
}

/*
 * Calculate the HMAC-SHA1 of a key and some data
 */
function core_hmac_sha1(key, data)
{
  var bkey = str2binb(key);
  if(bkey.length > 16) bkey = core_sha1(bkey, key.length * chrsz);

  var ipad = Array(16), opad = Array(16);
  for(var i = 0; i < 16; i++)
  {
    ipad[i] = bkey[i] ^ 0x36363636;
    opad[i] = bkey[i] ^ 0x5C5C5C5C;
  }

  var hash = core_sha1(ipad.concat(str2binb(data)), 512 + data.length * chrsz);
  return core_sha1(opad.concat(hash), 512 + 160);
}

/*
 * Add integers, wrapping at 2^32. This uses 16-bit operations internally
 * to work around bugs in some JS interpreters.
 */
function safe_add(x, y)
{
  var lsw = (x & 0xFFFF) + (y & 0xFFFF);
  var msw = (x >> 16) + (y >> 16) + (lsw >> 16);
  return (msw << 16) | (lsw & 0xFFFF);
}

/*
 * Bitwise rotate a 32-bit number to the left.
 */
function rol(num, cnt)
{
  return (num << cnt) | (num >>> (32 - cnt));
}

/*
 * Convert an 8-bit or 16-bit string to an array of big-endian words
 * In 8-bit function, characters >255 have their hi-byte silently ignored.
 */
function str2binb(str)
{
  var bin = Array();
  var mask = (1 << chrsz) - 1;
  for(var i = 0; i < str.length * chrsz; i += chrsz)
    bin[i>>5] |= (str.charCodeAt(i / chrsz) & mask) << (32 - chrsz - i%32);
  return bin;
}

/*
 * Convert an array of big-endian words to a string
 */
function binb2str(bin)
{
  var str = "";
  var mask = (1 << chrsz) - 1;
  for(var i = 0; i < bin.length * 32; i += chrsz)
    str += String.fromCharCode((bin[i>>5] >>> (32 - chrsz - i%32)) & mask);
  return str;
}

/*
 * Convert an array of big-endian words to a hex string.
 */
function binb2hex(binarray)
{
  var hex_tab = hexcase ? "0123456789ABCDEF" : "0123456789abcdef";
  var str = "";
  for(var i = 0; i < binarray.length * 4; i++)
  {
    str += hex_tab.charAt((binarray[i>>2] >> ((3 - i%4)*8+4)) & 0xF) +
           hex_tab.charAt((binarray[i>>2] >> ((3 - i%4)*8  )) & 0xF);
  }
  return str;
}

/*
 * Convert an array of big-endian words to a base-64 string
 */
function binb2b64(binarray)
{
  var tab = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
  var str = "";
  for(var i = 0; i < binarray.length * 4; i += 3)
  {
    var triplet = (((binarray[i   >> 2] >> 8 * (3 -  i   %4)) & 0xFF) << 16)
                | (((binarray[i+1 >> 2] >> 8 * (3 - (i+1)%4)) & 0xFF) << 8 )
                |  ((binarray[i+2 >> 2] >> 8 * (3 - (i+2)%4)) & 0xFF);
    for(var j = 0; j < 4; j++)
    {
      if(i * 8 + j * 6 > binarray.length * 32) str += b64pad;
      else str += tab.charAt((triplet >> 6*(3-j)) & 0x3F);
    }
  }
  return str;
}

function getHTTPObject(){
    var http_object;
    // MSIE Proprietary method
	/*@cc_on
	@if (@_jscript_version >= 5)
		try {
			http_object = new ActiveXObject("Msxml2.XMLHTTP");
		}
		catch (e) {
			try {
				http_object = new ActiveXObject("Microsoft.XMLHTTP");
			}
			catch (E) {
				http_object = false;
			}
		}
	@else
		xmlhttp = http_object;
	@end @*/
    if (!http_object && typeof XMLHttpRequest != 'undefined') {
        try {
            http_object = new XMLHttpRequest();
        }
        catch (e) {
            http_object = false;
        }
    }
    return http_object;
}

// constants
var NORMAL_STATE = 4;

function setMsg(msg, status){
    var usrFB = document.getElementById('usrFeedback');
    switch (status) {
        case 1:
            usrFB.className = "loadingFB";
            break;
        case 0:
            usrFB.className = "errorFB";
           	break;
		case 2:
			usrFB.className = "infoFB";
			break;
    }
    usrFB.innerText = msg; //mofo IE
    usrFB.textContent = msg;
}

function setVisibility(v, elem){
    elem.style.visibility = (v ? 'visible' : 'hidden');
}

function trimStr(str){
    return str.replace(/^\s*|\s*$/g, '');
}

function mm(d, n){
    window.location = "mailto:" + n + "@" + d;
}

function createMaintainanceCall(){
    var time=300000;
    fnInt = setInterval(getSysMaintainanceData, time);
}

function abortMaintainanceCall() {
    clearInterval(fnInt);
}
function getSysMaintainanceData(){
    Wtf.Ajax.requestEx({
        url : "ACCDashboard/getMaintainanceDetails.do",
        params: {dummyParameter: "accounting"}
    }, this,
    function(result, req){
        if(result.data!=undefined&&result.success==true ){
            var announcementpan = Wtf.getCmp('announcementpan');
            announcementpan.setVisible(true);
            notificationMessage(result.data[0].message);
            announcementpan.doLayout();
            Wtf.getCmp('viewport').doLayout();
        }
        else{
            hideTopPanel();
           // abortMaintainanceCall()//to be removed
        }
    });
}

function notificationMessage(msg) {
    var announcementpan = Wtf.getCmp('announcementpan');
    if(announcementpan !=null) {
        announcementpan.setVisible(true);
        document.getElementById("announcementpandiv").innerHTML =msg;
        announcementpan.doLayout();
        Wtf.getCmp('viewport').doLayout();
    }
}
function hideTopPanel() {
    var announcementpan = Wtf.getCmp('announcementpan');
    if(announcementpan !=null) {
        document.getElementById("announcementpandiv").innerHTML ="";
        announcementpan.setVisible(false);
        announcementpan.doLayout();
        Wtf.getCmp('viewport').doLayout();
        abortMaintainanceCall();
    }
}

function hideField(field) {
field.disable();// for validation
field.hide();
field.getEl().up('.x-form-item').setDisplayed(false); // hide label
}

function headerCheck(header) {
    var indx=header.indexOf('(');
    if(indx!=-1) {
        indx=header.indexOf("&#");
        if(indx!=-1)
            header=header.substring(0,header.indexOf('('));
    }
    return header;
}

// save google graph as image 

function getImgData(chartContainer) {
    var chartArea = chartContainer.getElementsByTagName('svg')[0].parentNode;
    var svg = chartArea.innerHTML;
    var doc = chartContainer.ownerDocument;
    var canvas = doc.createElement('canvas');
    canvas.setAttribute('width', chartArea.offsetWidth);
    canvas.setAttribute('height', chartArea.offsetHeight);

    canvas.setAttribute(
        'style',
        'position: absolute; ' +
        'top: ' + (-chartArea.offsetHeight * 2) + 'px;' +
        'left: ' + (-chartArea.offsetWidth * 2) + 'px;');
    doc.body.appendChild(canvas);
    canvg(canvas, svg);
    var imgData = canvas.toDataURL("image/png");
    canvas.parentNode.removeChild(canvas);
    return imgData;
}

function saveAsImg(chartContainer) {
        //    var imgData = getImgData(chartContainer);
//
//    // Replacing the mime-type will force the browser to trigger a download
//    // rather than displaying the image in the browser window.
//    window.location = imgData.replace("image/png", "image/octet-stream");
        var chartArea = chartContainer.getElementsByTagName('svg')[0].parentNode;
        var svg = chartArea.innerHTML;
        var jsonOptions={height:400,width:500,imageFormat:'jpeg'|'png'};
        var imageData=grChartImg.VectorGraphtoImageData(svg,jsonOptions);

        //To Download the image do.
//        grChartImg.DownloadImageDataAsImage(imageData);
        //To copy the image to clipboard do.
//        grChartImg.CopyImageDataToClip(imageData);
        grChartImg.ShowImageDataAsImage(imageData,{height:400,width:500,bDialog:true});


}

function toImg(chartContainer, imgContainer) {
    var doc = chartContainer.ownerDocument;
    var img = doc.createElement('img');
    img.src = getImgData(chartContainer);

    while (imgContainer.firstChild) {
        imgContainer.removeChild(imgContainer.firstChild);
    }
    imgContainer.appendChild(img);
}
function loadGlobalProductMasterFields(){
     var moduleArr=[Wtf.Acc_Sales_Order_ModuleId,Wtf.Acc_Invoice_ModuleId,Wtf.Acc_Vendor_Invoice_ModuleId,Wtf.Acc_Purchase_Order_ModuleId,Wtf.Acc_Customer_Quotation_ModuleId,Wtf.Acc_Vendor_Quotation_ModuleId]
               
            
    Wtf.Ajax.requestEx({
       url: "ACCAccountCMN/getCustomizedProductMasterFieldsTOShowAtLineLevel.do",
         params: {
                moduleArr: moduleArr
            }
    }, this,
    function(response,result){
             for(var i=0;i<moduleArr.length;i++){
                var module=moduleArr[i];
                GlobalProductmasterFieldsArr[module] =[];
            }

            for(var i=0;i<moduleArr.length;i++){
                var module=moduleArr[i];
                GlobalProductmasterFieldsArr[module] =response[module];
            }
    },
    function(response,result){

    })  
    
}
function loadCustomFieldColModel(moduleid,moduleidarray,obj){
    Wtf.Ajax.requestEx({
        url:'ACCAccountCMN/getFieldParams.do',
        params:{
//            moduleidarray:moduleidarray,
            moduleidarray: moduleidarray,
            customcolumn: 1,
            isActivated:1
        }
    }, this,
    function(response,result){
        response = response.data;
//        if (response.data != '' && response.data != null) {
//            if(response.data.length>0) {
////                var columns = WtfGlobal.appendCustomColumn(this.getColumnModel().config,response.data);
////                this.getColumnModel().setConfig(columns);
////                GlobalColumnModel[moduleid] = [];
//                GlobalColumnModel[moduleid] = response.data;
//            }
//        }
        if(moduleidarray){
            for(var i=0;i<moduleidarray.length;i++){
                GlobalColumnModel[moduleidarray[i]] =[];
                GlobalColumnModelForProduct[moduleidarray[i]] =[];
//                delete GlobalSpreadSheetConfig[getModuleNameFromIntegerID(moduleidarray[i])];
            }
          
        } else if(moduleid){
            GlobalColumnModel[moduleid] =[];
            GlobalColumnModelForProduct[moduleid] =[];
//            delete GlobalSpreadSheetConfig[getModuleNameFromIntegerID(moduleid)];
        }
        for(var i=0;i<response.length;i++){
            GlobalColumnModel[response[i].moduleid] = GlobalColumnModel[response[i].moduleid] ? GlobalColumnModel[response[i].moduleid] : [];
            GlobalColumnModel[response[i].moduleid].push(response[i]);
        }
        
        if(GlobalColumnModel[Wtf.Acc_Product_Master_ModuleId]) {
            for (var i = 0; i < GlobalColumnModel[Wtf.Acc_Product_Master_ModuleId].length; i++) {
                var relatedmoduleid = GlobalColumnModel[Wtf.Acc_Product_Master_ModuleId][i].relatedmoduleid;
                if (relatedmoduleid && relatedmoduleid != null) {
                    var splitStr = relatedmoduleid.split(",");
                    for (var k = 0; k < splitStr.length; k++) {
                        if(splitStr[k] != ""){
                            GlobalColumnModelForProduct[splitStr[k]] = GlobalColumnModelForProduct[splitStr[k]] ? GlobalColumnModelForProduct[splitStr[k]] : [];
                            GlobalColumnModelForProduct[splitStr[k]].push(GlobalColumnModel[Wtf.Acc_Product_Master_ModuleId][i]);
                        }
                    }
                }

            }
        }
    },
    function(response,result){

    })
}

function loadCustomFeildsColumnModelForReports(moduleid,moduleidarray,obj){
    Wtf.Ajax.requestEx({
        url: "ACCAccountCMN/getFieldParams.do",
        params: {
            moduleidarray:moduleidarray,
             customfieldlableflag:1,      //(Mayur B) flag for custom label
             isActivated:1
        }
    }, this,
                        
    function(response) {
        if (response.success==true) {
            response = response.data;

            if(moduleidarray){
                for(var i=0;i<moduleidarray.length;i++){
                    GlobalColumnModelForReports[moduleidarray[i]] =[];
                }
          
            } else if(moduleid){
                GlobalColumnModelForReports[moduleid] =[];
            }
            for(var i=0;i<response.length;i++){
                GlobalColumnModelForReports[response[i].moduleid] = GlobalColumnModelForReports[response[i].moduleid] ? GlobalColumnModelForReports[response[i].moduleid] : [];
                GlobalColumnModelForReports[response[i].moduleid].push(response[i]);
            }
        
//            if(GlobalColumnModel[Wtf.Acc_Product_Master_ModuleId]) {
//                for (var i = 0; i < GlobalColumnModel[Wtf.Acc_Product_Master_ModuleId].length; i++) {
//                    var relatedmoduleid = GlobalColumnModel[Wtf.Acc_Product_Master_ModuleId][i].relatedmoduleid;
//                    if (relatedmoduleid && relatedmoduleid != null) {
//                        var splitStr = relatedmoduleid.split(",");
//                        for (var k = 0; k < splitStr.length; k++) {
//                            GlobalColumnModelForProduct[splitStr[k]] = GlobalColumnModelForProduct[splitStr[k]] ? GlobalColumnModelForProduct[splitStr[k]] : [];
//                            GlobalColumnModelForProduct[splitStr[k]].push(GlobalColumnModel[Wtf.Acc_Product_Master_ModuleId][i]);
//                        }
//                    }
//
//                }
//            } 
        }         
    },
    function() {

        }
        );
}
function showVesionInfo(){    
    Wtf.MessageBox.alert(WtfGlobal.getLocaleText("acc.common.info"),WtfGlobal.getLocaleText("acc.version.name"),{},this)
}
function loadCustomDesignModel(moduleid,moduleidarray, obj){
    Wtf.Ajax.requestEx({
        url: "CustomDesign/getActiveDesignTemplateList.do",
        params :{
           moduleid : moduleidarray
        }
               
    },this,
    function(response,result){
        response=response.data;
        
        if(moduleidarray){ 
            GlobalCustomTemplateList[100]=[];
            GlobalCustomTemplateList[100].push('Default Template');
            for(var i=0;i<moduleidarray.length;i++){
                GlobalCustomTemplateList[moduleidarray[i]]=[];
            }
           
        }else if(moduleid){
            GlobalCustomTemplateList[moduleid]=[];
        }
        for(var i=0;i<response.length;i++){
            GlobalCustomTemplateList[response[i].moduleid] = GlobalCustomTemplateList[response[i].moduleid] ? GlobalCustomTemplateList[response[i].moduleid] : [];
            GlobalCustomTemplateList[response[i].moduleid].push(response[i]);
        }              
    },function(response, result){
       
    })
}

function loadDefaultCustomLayouts(moduleid,moduleidarray, obj){
    Wtf.Ajax.requestEx({
        url: "ACCAccount/getDefaultPnLTemplates.do"
    },this,
    function(response,result){
        var data = response.data || [];
        for(var i=0;i<data.length;i++){
            var record = data[i];
            Wtf.CustomLayout.DefaultTemplates[record.templatetype] = record;
        }
        
    },function(response, result){
       
    })
}

function loadLandingCostCategoryCustomFields() {
    if (Wtf.account.companyAccountPref.isActiveLandingCostOfItem) {
        Wtf.Ajax.requestEx({
            url: "ACCGoodsReceipt/createLandingCostItemConfig.do",
            params: {
                isstockledger: true
            }
        }, this, function (resp) {
            if (resp && resp.data) {
                GlobalColumnModelForLandedCostCategory = resp.data;
            }
        });
    }
}


Wtf.ux.Portal = Wtf.extend(Wtf.Panel, {
    layout: 'column',
    autoScroll: true,

    initComponent: function(){
        Wtf.ux.Portal.superclass.initComponent.call(this);
        this.addEvents({
            validatedrop: true,
            beforedragover: true,
            dragover: true,
            beforedrop: true,
            drop: true,
            ondrag: true
        });
    },

    initEvents: function(){
        Wtf.ux.Portal.superclass.initEvents.call(this);
        this.dd = new Wtf.ux.Portal.DropZone(this, this.dropConfig);
    }
});
Wtf.reg('portal', Wtf.ux.Portal);

Wtf.ux.Portal.DropZone = function(portal, cfg){
    this.portal = portal;
    Wtf.dd.ScrollManager.register(portal.body);
    Wtf.ux.Portal.DropZone.superclass.constructor.call(this, portal.bwrap.dom, cfg);
    portal.body.ddScrollConfig = this.ddScrollConfig;
};

Wtf.extend(Wtf.ux.Portal.DropZone, Wtf.dd.DropTarget, {
    ddScrollConfig: {
        vthresh: 50,
        hthresh: -1,
        animate: true,
        increment: 200
    },
    createEvent: function(dd, e, data, col, c, pos){
        return {
            portal: this.portal,
            panel: data.panel,
            columnIndex: col,
            column: c,
            position: pos,
            data: data,
            source: dd,
            rawEvent: e,
            status: this.dropAllowed
        };
    },

    notifyOver: function(dd, e, data){

        var xy = e.getXY(), portal = this.portal, px = dd.proxy;
        // case column widths
        if (!this.grid) {
            this.grid = this.getGrid();
        }

        // handle case scroll where scrollbars appear during drag
        var cw = portal.body.dom.clientWidth;
        if (!this.lastCW) {
            this.lastCW = cw;
        }
        else
        if (this.lastCW != cw) {
            this.lastCW = cw;
            portal.doLayout();
            this.grid = this.getGrid();
        }

        // determine column
        var col = 0, xs = this.grid.columnX, cmatch = false;
        for (var len = xs.length; col < len; col++) {
            if (xy[0] < (xs[col].x + xs[col].w)) {
                cmatch = true;
                break;
            }
        }
        // no match, fix last index
        if (!cmatch) {
            col--;
        }

        // find insert position
        var p, match = false, pos = 0, c = portal.items.itemAt(col)
        var items =new Array();
        if(c.items){
            items=c.items.items;
        }

        for (len = items.length; pos < len; pos++) {
            p = items[pos];
            var h = p.el.getHeight();
            if (h !== 0 && (p.el.getY() + (h / 2)) > xy[1]) {
                match = true;
                break;
            }
        }

        var count=0;
        if(c.items){
            count=c.items.getCount();
        }
        var overEvent = this.createEvent(dd, e, data, col, c, match && p ? pos : count);

        if (portal.fireEvent('validatedrop', overEvent) !== false &&
            portal.fireEvent('beforedragover', overEvent) !== false) {

            // make sure proxy width is fluid
            px.getProxy().setWidth('auto');

            if (p) {
                px.moveProxy(p.el.dom.parentNode, match ? p.el.dom : null);
            }
            else {
                px.moveProxy(c.body.dom, null);
            }

            this.lastPos = {
                c: c,
                col: col,
                p: match && p ? pos : false
            };
            this.scrollPos = portal.body.getScroll();

            portal.fireEvent('dragover', overEvent);

            return overEvent.status;

        }
        else {
            return overEvent.status;
        }

    },

    notifyOut: function(){
        delete this.grid;
    },

    notifyDrop: function(dd, e, data){
        delete this.grid;

        var c = this.lastPos.c, col = this.lastPos.col, pos = this.lastPos.p;

        var _count=0;
        if(c.items){
            _count=c.items.getCount();
        }
        var dropEvent = this.createEvent(dd, e, data, col, c, pos !== false ? pos : _count);

        if (this.portal.fireEvent('validatedrop', dropEvent) !== false &&
            this.portal.fireEvent('beforedrop', dropEvent) !== false) {

            dd.proxy.getProxy().remove();
            if (dd.panel.ownerCt == this.lastPos.c) {
                dd.panel.el.dom.parentNode.removeChild(dd.panel.el.dom);
            }
            if (this.lastPos.p !== false) {
                this.lastPos.c.insert(this.lastPos.p, dd.panel);
            }
            else {
                this.lastPos.c.add(dd.panel);
            }
            this.portal.doLayout();

            this.portal.fireEvent('drop', dropEvent);

            // scroll position is lost on drop, fix it
            var st = this.scrollPos.top;
            if (st) {
                var d = this.portal.body.dom;
                setTimeout(function(){
                    d.scrollTop = st;
                }, 10);
            }
        }
    },

    // internal cache of body and column coords
    getGrid: function(){
        var box = this.portal.bwrap.getBox();
        box.columnX = [];
        this.portal.items.each(function(c){
            box.columnX.push({
                x: c.el.getX(),
                w: c.el.getWidth()
            });
        });
        return box;
    }
});

function loadDimensionFeildsColumnModelForReports(moduleid,moduleidarray,obj){
    Wtf.Ajax.requestEx({
        url: "ACCAccountCMN/getFieldParams.do",
        params: {
            moduleid:moduleidarray,
            isActivated:1
//            iscustomfield:0
        }
    }, this,
                        
    function(response) {
        if (response.success==true) {
            response = response.data;

            if(moduleidarray){
                for(var i=0;i<moduleidarray.length;i++){
                    GlobalDimensionModelForReports[moduleidarray[i]] =[];
                }
          
            } else if(moduleid){
                GlobalDimensionModelForReports[moduleid] =[];
            }
            for(var i=0;i<response.length;i++){
                GlobalDimensionModelForReports[response[i].moduleid] = GlobalDimensionModelForReports[response[i].moduleid] ? GlobalDimensionModelForReports[response[i].moduleid] : [];
                GlobalDimensionModelForReports[response[i].moduleid].push(response[i]);
            }
        
//            if(GlobalColumnModel[Wtf.Acc_Product_Master_ModuleId]) {
//                for (var i = 0; i < GlobalColumnModel[Wtf.Acc_Product_Master_ModuleId].length; i++) {
//                    var relatedmoduleid = GlobalColumnModel[Wtf.Acc_Product_Master_ModuleId][i].relatedmoduleid;
//                    if (relatedmoduleid && relatedmoduleid != null) {
//                        var splitStr = relatedmoduleid.split(",");
//                        for (var k = 0; k < splitStr.length; k++) {
//                            GlobalColumnModelForProduct[splitStr[k]] = GlobalColumnModelForProduct[splitStr[k]] ? GlobalColumnModelForProduct[splitStr[k]] : [];
//                            GlobalColumnModelForProduct[splitStr[k]].push(GlobalColumnModel[Wtf.Acc_Product_Master_ModuleId][i]);
//                        }
//                    }
//
//                }
//            } 
        }         
    },
    function() {

        }
        );
}

Wtf.override(Wtf.layout.FormLayout, {
    renderItem : function(c, position, target){
        if(c && !c.rendered && c.isFormField && c.inputType != 'hidden'){
            var args = [
                   c.id, c.fieldLabel,
                   c.labelStyle||this.labelStyle||'',
                   this.elementStyle||'',
                   typeof c.labelSeparator == 'undefined' ? this.labelSeparator : c.labelSeparator,
                   (c.itemCls||this.container.itemCls||'') + (c.hideLabel ? ' x-hide-label' : ''),
                   c.clearCls || 'x-form-clear-left' 
            ];
            if(typeof position == 'number'){
                position = target.dom.childNodes[position] || null;
            }
            if(position){
                c.formItem = this.fieldTpl.insertBefore(position, args, true);
            }else{
                c.formItem = this.fieldTpl.append(target, args, true);
            }

//          Remove the form layout wrapper on Field destroy.
            c.on('destroy', c.formItem.remove, c.formItem, {single: true});
            c.render('x-form-el-'+c.id);
        }else {
            Wtf.layout.FormLayout.superclass.renderItem.apply(this, arguments);
        }
    }
});

function loadGlobalDimensionCustomFieldModel(moduleid,moduleidarray,obj){
    Wtf.Ajax.requestEx({
        url: "ACCAccountCMN/getFieldParams.do",
        params: {
            moduleidarray:moduleidarray,
            isActivated:1
        }
    }, this,
                        
    function(response) {
        if (response.success==true) {
            response = response.data;

            if(moduleidarray){
                for(var i=0;i<moduleidarray.length;i++){
                    GlobalDimensionCustomFieldModel[moduleidarray[i]] =[];
                }
          
            } else if(moduleid){
                GlobalDimensionCustomFieldModel[moduleid] =[];
            }
            for(var i=0;i<response.length;i++){
                GlobalDimensionCustomFieldModel[response[i].moduleid] = GlobalDimensionCustomFieldModel[response[i].moduleid] ? GlobalDimensionCustomFieldModel[response[i].moduleid] : [];
                GlobalDimensionCustomFieldModel[response[i].moduleid].push(response[i]);
            }

        }         
    },
    function() {

        }
   );
}

function setMyConfig (grid, data, applyRule, applyState, refresh, header, sysPrefConfig, isInitialLoad, isdocumentEntryForm){
    if(applyRule){
        var rules = data.rules.rules;
        grid.view.rules = rules;
    }
    if(applyState){
        if(data.state){
            if(data.state.columns != false){
                applyGridState(grid, data.state, sysPrefConfig, isInitialLoad, isdocumentEntryForm);
                grid.getView().updateAllColumnWidths();
                grid.getView().refresh(true);
            }
            if(header != undefined && header != ""){
                grid.applyCustomHeader(header);
            }
        }
    }
    if(refresh){
        grid.refreshMyView(applyState);
    } 
}
    
function applyGridState(grid, state,  sysPrefConfig, isInitialLoad, isdocumentEntryForm){
    var cm = grid.colModel;
    var stateColumns = state.columns;
    var moduleid = sysPrefConfig.moduleid;
    sysPrefConfig = sysPrefConfig.gridPref;
    if(stateColumns){
        for (var i = 0; i < stateColumns.length; i++) {
            var forceHide = false;
            var isReadOnly = false;
            var fieldLabelText = undefined;
            var stateRecord = stateColumns[i];
            var index = cm.findColumnIndex(stateRecord.id);
            
            if (sysPrefConfig) {
                for (var j = 0; j < sysPrefConfig.length; j++) {
                    if (stateRecord.id == sysPrefConfig[j].fieldId) {
                        if ((sysPrefConfig[j].isReportField || (isdocumentEntryForm && sysPrefConfig[j].isFormField && sysPrefConfig[j].isHidden))) {
                            forceHide = true;
                        }
                        if (isdocumentEntryForm && sysPrefConfig[j].isFormField && sysPrefConfig[j].isReadOnly) {
                            isReadOnly = true;
                        }
                        if (sysPrefConfig[j].isFormField && sysPrefConfig[j].fieldLabelText) {
                            fieldLabelText = sysPrefConfig[j].fieldLabelText
                        }
                    }
                }
            }
            
            if (index != -1) {
                if (forceHide) {
                    cm.config[index].hideable = false;
                    cm.setHidden(index, true);
                } else if (stateRecord.hidden) {
                    cm.setHidden(index, true);
                } else if (!(moduleid == Wtf.Serial_No_Window_Grid_Id || (moduleid == Wtf.Build_Assembly_Report_ModuleId && isdocumentEntryForm))) {
                    cm.setHidden(index, false);
                }
                if (isReadOnly) {
                    cm.setEditable(index, false);
                }
                if (fieldLabelText) {
                    cm.setColumnHeader(index, fieldLabelText);
                }
                cm.setColumnWidth(index,stateRecord.width);
                if(index!=i){
                    cm.moveColumn(index, i);
                }
                if(stateRecord.locked){
                    cm.setLocked(index, true);
                } else {
                    cm.setLocked(index, false);
                }
            } else {
                var column = cm.getColumnById(stateRecord.id);
                if (column) {
                    if (forceHide) {
                        column.hideable = false;
                        column.hidden = true;
                    } else if (stateRecord.hidden) {
                        column.hidden = true;
                    } else if (!(moduleid == Wtf.Serial_No_Window_Grid_Id || (moduleid == Wtf.Build_Assembly_Report_ModuleId && isdocumentEntryForm))) {
                        column.hidden = false;
                    }
                    if (isReadOnly) {
                        column.editable = false;
                    }
                    if (fieldLabelText) {
                        cm.header = fieldLabelText;
                    }
                    if(stateRecord.locked){
                        column.locked = stateRecord.locked;
                    }
                    column.width = stateRecord.width;
                    if (cm.config[i]) {
                        cm.setColumnWidth(i, stateRecord.width, true);
                    }
                    var oldIndex = cm.getIndexById(stateRecord.id);
                    if(oldIndex != i){
                        cm.moveColumn(oldIndex, i);
                    }
                }
            }
            
            switch (stateRecord.id) {
                case "checker":
                    if (index != -1) {
                        cm.setColumnWidth(index, 21);
                    } else if (column) {
                        column.width = 21;
                    }
                    break;
                case "numberer":
                    if (index != -1) {
                        cm.setColumnWidth(index, 30);
                    } else if (column) {
                        column.width = 30;
                    }
                    break;
                case "expander":
                    if (index != -1) {
                        cm.setColumnWidth(index, 20);
                    } else if (column) {
                        column.width = 20;
                    }
                    break;
                case "isEmailSent":
                    if (index != -1) {
                        cm.setColumnWidth(index, 35);
                    } else if (column) {
                        column.width = 35;
                    }
                    break;
                case "favoritePrinted":
                    if (index != -1) {
                        cm.setColumnWidth(index, 50);
                    } else if (column) {
                        column.width = 50;
                    }
                    break;
                case "isprinted":
                    if (index != -1) {
                        cm.setColumnWidth(index, 35);
                    } else if (column) {
                        column.width = 35;
                    }
                    break;
            }
        }
        grid.colModel = cm;
    }
    if(state.sort && grid.store.getCount() > 0){
        if(isInitialLoad) {
            var remoteSort = grid.store.remoteSort; // SDP-12302 To avoid auto loading reports. Due to state.sort=true, the grid get loaded.
            grid.store.remoteSort = false;
            grid.store.sort(state.sort.field, state.sort.direction);
            grid.store.remoteSort = remoteSort;
        } else {
            grid.store.sort(state.sort.field, state.sort.direction);
        }
    }
}
    
function refreshMyView(grid,headers){
    grid.getSelModel().selType="None";
    grid.setSelType();
    var view = grid.view;
    view.refresh(headers);
}
    
function applyCustomHeader(grid,header){
    var cm = grid.colModel;
    var cs = cm.config;
    for(var i = 0, len = cs.length; i < len; i++){
        var s = cs[i];
        var c = cm.getColumnById(s.id);
        var ismandotory = false;
        for(var j = 0 ; j< header.length && header[j].oldheader ; j++){
            var oldHeader = header[j].oldheader.trim();
            var newHeader = header[j].newheader.trim();
            ismandotory = header[j].ismandotory;
            if(header[j].recordname.replace(" ","_") == c.dataIndex || header[j].recordname == c.validationId || header[j].recordname==c.dataIndex){
                if(ismandotory==true)
                    c.mandatory = true;
                else
                    c.mandatory = false;
            }
            var currency = c.header.trim().split("(");
            var currency1;
            if(currency.length>1)
                currency1= currency[1].split(")");
            if(oldHeader == c.headerName.trim()){
                c.header = newHeader;
                if(currency[1]!=null){
                    newHeader = newHeader+"("+currency1[0]+")";
                    c.header = newHeader;
                }
                if(ismandotory && oldHeader.substring(oldHeader.length-1,oldHeader.length).trim()!="*"){
                    c.header = newHeader+" *";
                } else if(ismandotory !=true && oldHeader.substring(oldHeader.length-1,oldHeader.length).trim()=="*"){
                    c.header =newHeader.substring(0,newHeader.length-1).trim()
                }
            }
        }
    }
}

Wtf.DownloadLink = function(a, b, c, d, e, f) {       
    var msg = "";
    var url = "ACCInvoiceCMN/getAttachDocuments.do";   
    var documentBillId = c.data['billid']!=undefined ? c.data['billid'] : c.data['id'];
    var attachmentIds="";
    var rowIndex=d;
    if(c.data['attachmentids']!=undefined){
        attachmentIds = c.data['attachmentids'];
    }
    if(c.data['documentbatchid']!=undefined){
        documentBillId = c.data['documentbatchid'];
    }
    if (c.data['attachment']!=0)
        msg ='('+c.data['attachment']+')'+'<div class = "pwnd downloadDoc" wtf:qtitle="'
        + WtfGlobal.getLocaleText("acc.invoiceList.attachments")
        + '" wtf:qtip="'
        + WtfGlobal
        .getLocaleText("acc.invoiceList.clickToDownloadAttachments")
        + '" onclick="displayDocList(\''
        + documentBillId
        + '\',\''
        + url
        + '\',\''
        + 'invoiceGridId'
        + this.id
        + '\', event,\''
        + ""
        + '\',\''
        + ""
        + '\',\''
        + false
        + '\',\''
        + 0
        + '\',\''
        + 0
        + '\',\''
        + ""
        + '\',\''
        + this.grid.id 
        + '\',\''
        + this.moduleid//ERP-13011 [SJ]
        + '\',\''
        + attachmentIds
        + '\',\''
        + (this.grid.isbatch==undefined?false:this.grid.isbatch)
        + '\',\''
        + rowIndex
        + '\',\''
        + this.grid.readOnly
        + '\')" style="width: 16px; height: 16px;cursor:pointer; margin-left: 24px; margin-top: -15px;" id=\''
        + c.data['leaveid'] + '\'>&nbsp;</div>';
    //else
    //  msg = "";
    return msg;
}
Wtf.callGobalDocFunction = function(grid, rowindex,e) {
    if (e.target.className != "pwndbar1 uploadDoc")
        return;
    if (this.grid.readOnly!==undefined && this.grid.readOnly!='' && this.grid.readOnly){
        return;
    }
    var selected = this.grid.getStore().getAt(rowindex);
    this.rindex=rowindex;
    if (this.grid.flag == 0) {
        this.fileuploadwin = new Wtf.form.FormPanel(
        {                  
            url : "ACCInvoiceCMN/attachDocuments.do",
            waitMsgTarget : true,
            fileUpload : true,
            method : 'POST',
            border : false,
            scope : this,
            // layout:'fit',
            bodyStyle : 'background-color:#f1f1f1;font-size:10px;padding:10px 15px;',
            lableWidth : 50,
            items : [
            this.sendInvoiceId = new Wtf.form.Hidden(
            {
                name : 'invoiceid'
            }),
            /* Parameter for moduleid*/
            this.sendModuleidId = new Wtf.form.Hidden(
            {
                name : 'moduleid'
            }),
            this.tName = new Wtf.form.TextField(
            {
                fieldLabel : WtfGlobal.getLocaleText("acc.invoiceList.filePath") + '*',
                //allowBlank : false,
                name : 'file',
                inputType : 'file',
                width : 200,
                //emptyText:"Select file to upload..",
                blankText:WtfGlobal.getLocaleText("acc.field.SelectFileFirst"),
                allowBlank:false,
                msgTarget :'qtip'
            }) ]
        });

        this.upwin = new Wtf.Window(
        {
            id : 'upfilewin',
            title : WtfGlobal
            .getLocaleText("acc.invoiceList.uploadfile"),
            closable : true,
            width : 450,
            height : 120,
            plain : true,
            iconCls : 'iconwin',
            resizable : false,
            layout : 'fit',
            scope : this,
            listeners : {
                scope : this,
                close : function() {
                    thisclk = 1;
                    scope: this;
                    this.fileuploadwin.destroy();
                    this.grid.flag = 0
                }
            },
            items : this.fileuploadwin,
            buttons : [
            {
                anchor : '90%',
                id : 'saveUploadDoc',
                text : WtfGlobal.getLocaleText("acc.invoiceList.bt.upload"),
                scope : this,
                handler : Wtf.upfileHandler
            },
            {
                anchor : '90%',
                id : 'closeUploadDoc',
                text : WtfGlobal
                .getLocaleText("acc.invoiceList.bt.cancel"),
                handler : Wtf.closeDownloadWin,
                scope : this
            } ]

        });
        var documentBillId = (selected.data.billid!=undefined) ? selected.data.billid : selected.data.id;
        if (selected.data.documentbatchid!= undefined) {
            documentBillId = selected.data.documentbatchid;
        }
        this.sendInvoiceId.setValue(documentBillId);
        this.sendModuleidId.setValue(this.moduleid);
        this.upwin.show();
        this.grid.flag = 1;
    }
}
Wtf.closeDownloadWin = function() {
    this.upwin.close();
    this.grid.flag = 0;
}

Wtf.upfileHandler = function() {
    if (this.fileuploadwin.form.isValid()) {
        Wtf.getCmp('saveUploadDoc').disabled = true;
    }
    if (this.fileuploadwin.form.isValid()) {
        this.fileuploadwin.form.submit({
            scope : this,
            failure : function(frm, action) {
                this.upwin.close();
                Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), "File not uploaded! File should not be empty.");
            },
            success : function(frm, action) {
                this.upwin.close();
//                Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), "File uploaded successfully.",function(){this.grid.getStore().reload();},this);
                var resultObj = eval('(' + action.response.responseText + ')');
                if(this.grid.isbatch){
                    this.setTransactionId(resultObj.documentid,this.rindex);
                }
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.success"),
                    msg: WtfGlobal.getLocaleText("acc.invoiceList.bt.fileUploadedSuccess"),
                    width: 300,
                    buttons: Wtf.MessageBox.OK,
                    animEl: 'mb9',
                    scope:this,
                    icon: Wtf.MessageBox.INFO,
                    fn:function(){
                        if(!this.grid.isbatch){
                            this.grid.getStore().reload();
                        }
                    }
                });
            }
        })
    }
}

function displayDocList(id, url, gridid, event,creatorid,approverid,docReq,cnt,statusid,showleaves,reportGridId){    
    if(Wtf.getCmp('DocListWindow'))
        Wtf.getCmp("DocListWindow").destroy();
    new Wtf.DocListWindow({
        wizard:false,
        closeAction : 'hide',
        layout: 'fit',
        title:WtfGlobal.getLocaleText("acc.invoiceList.attachments"),
        shadow:false,
        bodyStyle: "background-color: white",
        closable: true,
        width : 450,
        heigth:250,
        url: url,
        gridid: gridid,
        modal:true,
        autoScroll:true,
        recid:id,
        delurl: "ACCInvoiceCMN/deleteDocument.do?docid=",
        id:"DocListWindow",
        docCount:cnt,
        isDocReq:docReq,  
        statusID:statusid,  
        showleaves:showleaves,  
        dispto:"pmtabpanel",
        reportGridId:reportGridId            //ERP-13011 [SJ]
    });

    var docListWin = Wtf.getCmp("DocListWindow");
    var leftoffset =event.pageX-400;

    var topoffset = event.pageY+10;
    if (document.all) {
        xMousePos = window.event.x+document.body.scrollLeft;
        yMousePos = window.event.y+document.body.scrollTop;
        xMousePosMax = document.body.clientWidth+document.body.scrollLeft;
        yMousePosMax = document.body.clientHeight+document.body.scrollTop;
        leftoffset=xMousePos-400;//xMousePos;
        topoffset=yMousePos+120;//yMousePos;
        
    }
    if(docListWin.innerpanel==null||docListWin.hidden==true){
        docListWin.setPosition(leftoffset, topoffset);

        docListWin.show();
    }else{
        docListWin.hide();

    }
}
function deletedocs(url, docid , gridid,isbatch,rowIndex){ //ERP-13011 [SJ]
    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.invoiceList.deletedocumentmsg"), function (btn){
        if(btn.toString()=="yes")
        {
            Wtf.Ajax.requestEx({
                method:'POST',
                url:url,
                params:{
                    dummy:1
                }
            },
            this,
            function(response){
                var retstatus = response;
                if(retstatus[0].success){
                    Wtf.getCmp("DocListWindow").hide();
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), retstatus[0].msg,function(){
                    if(Wtf.getCmp(gridid) && Wtf.getCmp(gridid).getStore()) {         //ERP-13011 [SJ]
                        if(!isbatch){
                            Wtf.getCmp(gridid).getStore().reload();
                        }
                        Wtf.getCmp(gridid).fireEvent('updateAttachmentDetail',rowIndex,retstatus[0].docid);
                    }                    
                    },this);
                                     
                }
                else{
                    var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";                    
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
                }     
            },
            function(response){                
                Wtf.MessageBox.hide();
                var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";        
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
            }
            );
        }
    }, this);
}
/**
*This method is used to remove Temporary or Permanent Document.
*We are passing URL with docid.Docid is uuid of that document which we are deleting.
*/
function deleteAttachDoc(obj){
    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.invoiceList.deletedocumentmsg"), function (btn){
        if(btn.toString()=="yes") {
            Wtf.Ajax.requestEx({
                method:'POST',
                url:obj.url,//"ACCInvoiceCMN/deleteAttachedDocument.do?docid=
                params:{
                    dummy:1
                }
            },
            this,
            function(response){
                var retstatus = response;
                if(retstatus[0].success){
                    var msg=WtfGlobal.getLocaleText("acc.document.documentdeletedsuccessfully");
                    if(obj.isFromReportGrid){
                        Wtf.getCmp("DocListWindow").hide();
                        Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), retstatus[0].msg,function(){
                            if(Wtf.getCmp(obj.gridid) && Wtf.getCmp(obj.gridid).getStore()) {         //ERP-13011 [SJ]
                                if(!obj.isbatch){
                                    Wtf.getCmp(obj.gridid).getStore().reload();
                                }
                                Wtf.getCmp(obj.gridid).fireEvent('updateAttachmentDetail',obj.rowIndex,retstatus[0].docid);
                            }                    
                        },this);
                    }
              /*
              *Removing deleted id from fileIds and updating fileStr.
              */
                    if(obj.parentObj != undefined && obj.parentObj.fileStr != undefined){
                        var id = retstatus[0].docRefId;
                        var fileIds=(obj.parentObj.fileStr).split(",");
                        var updateFileIds="";
                        for (var k = 0; k < fileIds.length; k++) {
                            if(fileIds[k] != id){
                                updateFileIds += fileIds[k] +",";
                            }
                        }
                        obj.parentObj.fileStr = updateFileIds.substring(0, updateFileIds.length-1 );
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),msg], 0);
                    }
                }
                else{
                    var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";                    
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
                }
            },
            function(response){                
                Wtf.MessageBox.hide();
                var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";        
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
            });
        }
    }, this);

}
