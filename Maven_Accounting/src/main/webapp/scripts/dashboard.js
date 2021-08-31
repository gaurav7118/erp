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

var widgetcount = 0;

function pagingRedirect(panelid, pager, subPan, searchstr, panelcount){
    var myPanel = Wtf.getCmp(panelid);
    myPanel.doPaging(myPanel.config1[subPan].url, (panelcount * pager), searchstr, pager, subPan);
}

function pagingRedirect1(panelid, pager, subPan, searchstr, panelcount){
    var myPanel = Wtf.getCmp(panelid);
    myPanel.doPaging(myPanel.config0[subPan].url, (panelcount * pager), searchstr, pager, subPan);
}

function quoteReplace(psString){
    var lsRegExp = /'|%/g;
    return String(psString).replace(lsRegExp, "");
}

function btnpressed(panelid){
    var searchid = "search" + panelid;
    var searchstr = document.getElementById(searchid).value;
    searchstr = quoteReplace(searchstr);
    var myPanel = Wtf.getCmp(panelid);
    myPanel.doSearch(myPanel.url, searchstr);
}

function createtooltip1(target, tpl_tool_tip, autoHide, closable, height){
    usertooltip = tpl_tool_tip, new Wtf.ToolTip({
        id: "1KChampsToolTip" + target,
        autoHide: autoHide,
        closable: closable,
        html: usertooltip,
        height: height,
        target: target
    });
}

function getToolsArrayForModules(code,managePerm) {
    var ta = [];
    var tip=listViewTips(code);
    ta.push({
        id:'updatewizardlink',
        qtip : tip.update,
        handler: function(e, target, panel) {
            openUpdate(panel.id,panel.id+'_updatelink');
        }
    });
    if(managePerm && code!=8) {
        ta.push({
            id:'quickwizardlink',
            qtip : tip.addlink,
            handler: function(e, target, panel) {
                openQuickAdd(panel.id,panel.id+'_quickaddlink',code);
            }
        });
    }
    ta.push({
        id:'paichartwizard',
        qtip : tip.chart,
        handler: function(e, target, panel) {
            openGraph(panel.id,panel.id+'_graphlink');
        }
    });

    if(code != 6 && code != 8) {
        ta.push({
            id:'detailwizardlink',
            qtip : tip.detail,
            handler: function() {
                switch(code) {
                    case 0 :
                        addCampaignTab();
                        break;
                    case 1 :
                        addLeadTab();
                        break;
                    case 2 :
                        addAccountTab();
                        break;
                    case 3 :
                        addContactTab();
                        break;
                    case 4 :
                        addOpportunityTab();
                        break;
                    case 5 :
                        addCaseTab();
                        break;
                    case 6 :
                        addActivityMasterTab();
                        break;
                    case 7 :
                        addProductMasterTab();
                        break;
                }
            }
        });
    }
    ta.push({
        id: 'close',
        handler: function(e, target, panel){
            var tt = panel.title;
            panel.ownerCt.remove(panel, true);
            panel.destroy();
            removeWidget(tt);
        }
    });
    return ta;
}

function getToolsArrayForReport(code,managePerm) {
    var ta = [];
    var tip=listViewTips(code);
    ta.push({
        id:'paichartwizard',
        qtip : tip.chart,
        handler: function(e, target, panel) {
            openGraph(panel.id,panel.id+'_graphlink');
        }
    });
    return ta;
}

function getToolsArray(ru,onlyRss){
    var ta = [];
    if(!onlyRss){
        ta.push({
            id: 'close',
            handler: function(e, target, panel){
                var tt = panel.title;
                panel.ownerCt.remove(panel, true);
                panel.destroy();
                removeWidget(tt);
            }
        });
    }
    return ta;
}

function listViewTips(code) {
    var moduleName = "";
    var plural = undefined;
    switch(code){
        case 0:
            moduleName = WtfGlobal.getLocaleText("crm.CAMPAIGN");
            break;
        case 1:
            moduleName = WtfGlobal.getLocaleText("crm.LEAD");
            break;
        case 2:
            moduleName = WtfGlobal.getLocaleText("crm.ACCOUNT");
            break;
        case 3:
            moduleName = WtfGlobal.getLocaleText("crm.CONTACT");
            break;
        case 5:
            moduleName = WtfGlobal.getLocaleText("crm.CASE");
            break;
        case 7:
            moduleName = WtfGlobal.getLocaleText("crm.PRODUCT");
            break;
        case 4:
            moduleName = WtfGlobal.getLocaleText("crm.OPPORTUNITY");
            plural    = WtfGlobal.getLocaleText("crm.OPPORTUNITY.plural");
            break;
        case 6:
            moduleName = WtfGlobal.getLocaleText("crm.ACTIVITY");
            plural    = WtfGlobal.getLocaleText("crm.ACTIVITY.plural");
            break;
    }
    var toolTip=new Object();
    toolTip.detail=WtfGlobal.getLocaleText({
        key:"crm.dashboard.tipmaker.detail",
        params:[moduleName]
        });
    toolTip.chart=WtfGlobal.getLocaleText({
        key:"crm.dashboard.tipmaker.chart",
        params:[moduleName]
        });
    moduleName=plural || moduleName;
    toolTip.update=WtfGlobal.getLocaleText({
        key:"crm.dashboard.tipmaker.update",
        params:[moduleName]
        });
    toolTip.addlink=WtfGlobal.getLocaleText({
        key:"crm.dashboard.tipmaker.addlink",
        params:[moduleName]
        });
    return toolTip;
}

function createNewPanel(setting,res,dataFlag){
    if(setting !== undefined) {
        if (setting.config1 != null) {
            return (new Wtf.WtfCustomPanel(setting,res,dataFlag));
        } else if (setting.config0 != null) {
            return (new Wtf.WtfCustomCrmPanel(setting,res,dataFlag));
        }
        else {
            if (setting.url != null) {
                return (new Wtf.WtfIframeWidgetComponent(setting));
            }
            else {
                return (new Wtf.WtfWidgetComponent(setting));
            }
        }
    }
}

function createWidget(ix){
    widgetcount--;
    titleSpan.innerHTML = '<span><span style="float:left;">'+WtfGlobal.getLocaleText("acc.dashboard.southregion.title")+'</span><span>'+widgetcount +" "+(widgetcount>1 ? WtfGlobal.getLocaleText("acc.widget.plural"): WtfGlobal.getLocaleText("acc.widget"))+"</span></span>";
    var count = 0
    var lowCountCol = 1;
    var lowCount = 1;
    var _ID = "portal_container_box";
    var box3Comp = Wtf.getCmp(_ID +"3");
    var box2Comp = Wtf.getCmp(_ID +"2");
    var box1Comp = Wtf.getCmp(_ID +"1");
    if (box3Comp.items != null) {
        lowCount =box3Comp.items.length;
    } else if (box2Comp.items != null) {
        lowCount = box2Comp.items.length;
    } else if (box1Comp.items != null) {
        lowCount = box1Comp.items.length;
    }


    for (var i = 3; i > 0; i--) {
        count=0;
        var comp = Wtf.getCmp(_ID+i);
        if (comp.items != null){
            count = comp.items.length;
        }
        if (count <= lowCount) {
            lowCount = count;
            lowCountCol = i;
        }
    }

    var pl = Wtf.getCmp(_ID + lowCountCol);
    if (pl != null) {
        var pn = createNewPanel(panelArr[ix],"");
        pl.add(pn);
        pl.doLayout();
        var t = Wtf.get("lix_" + ix);
        t.remove();
    }
    insertIntoWidgetState(lowCountCol,widgetIdArray[ix]);
}
function insertIntoWidgetState(colno,wid){
    Wtf.Ajax.requestEx({
        url:'ACCWidgetDashboard/insertWidgetIntoState.do',
        params:{
            flag:3,
            wid:wid,
            colno:colno
        }
    }, this, function(){
        }, function(){})

}
function removeWidget(tt){
    var ix = widgetArr.indexOf(WtfGlobal.HTMLStripper(tt));
    requestForWidgetRemove(widgetIdArray[ix]);
    appendWidget(ix);
}
function widgetTooltip(name){
    var tip="";
    switch(name){
        case 'Purchase<br/>Management':
            tip=WtfGlobal.getLocaleText("acc.field.purchasemgnttooltip");//You can perform all purchase related activities through this module;
            break;
        case "Sales/Billing<br/>Management":
            tip=WtfGlobal.getLocaleText("acc.field.Salesandbillingtooltip");//You can perform all Sales and Billing related activities through this module
            break;
        case "Financial<br/>Reports":
            tip=WtfGlobal.getLocaleText("acc.field.financialstmttooltip");//You can keep all financial track through this module
            break;
        case "Purchase<br/>Transaction Records":
            tip=WtfGlobal.getLocaleText("acc.field.purchasetransactionreporttooltip");//You can see all the purchase trasaction records through this module
            break;
        case "Account<br/>Management":
            tip=WtfGlobal.getLocaleText("acc.field.accountmgnttooltip");//You can see all the trasaction records through this module
            break;
        case "Masters":
            tip=WtfGlobal.getLocaleText("acc.field.masterstooltip");//You can perform system related acdtivities through this module
            break;    
        case "Updates":
            tip=WtfGlobal.getLocaleText("acc.field.updatestooltip");//You can see all the system updates on this panel
            break;    
        case "Administration":
            tip=WtfGlobal.getLocaleText("acc.field.admintooltip");//You can see administration related
            break;   
        case "Sales<br/>Transaction Records":
            tip=WtfGlobal.getLocaleText("acc.field.salestransactiontooltip");//You can see sales related transaction records  
            break;   
    }
    return tip;
}

function appendWidget(ix){
    widgetcount++;
    titleSpan.innerHTML = '<span><span style="float:left;">'+WtfGlobal.getLocaleText("acc.dashboard.southregion.title")+'</span><span>'+widgetcount +" "+(widgetcount>1 ? WtfGlobal.getLocaleText("acc.widget.plural"): WtfGlobal.getLocaleText("acc.widget"))+"</span></span>";
    var _widgetname=widgetArr[ix].replace(" ","<br/>");
    var tip=widgetTooltip(_widgetname);
    var name_markup="<div class='widget_name'>"+_widgetname+"</div>";
    Wtf.DomHelper.append("widgetUl", "<li id='lix_" + ix +"' style='padding-left:10px !important;'><div wtf:qtip=\""+tip+"\" onclick='javascript:createWidget(" + ix +")' class='dashpwnd "+widgetIdArray[ix]+"'  ></div>"+name_markup+"</li>");
}

var widgetArr = [];
var panelArr = [];
var widgetIdArray=[];
dashboardPortletArray();

function dashboardPortletArray(){
    if(Wtf.accountpayableManagementFlag){
        widgetArr.push(WtfGlobal.getLocaleText("acc.dashboard.dashboardlinks.purchasemgnt.title"));//Purchase Management,
    }
    if(Wtf.accountsreceivablesalesFlag){
        widgetArr.push(WtfGlobal.getLocaleText("acc.dashboard.dashboardlinks.salesmgnt.title"));//Sales and Billing Management,
    }
    widgetArr.push(WtfGlobal.getLocaleText("acc.dashboard.dashboardlinks.financialstmt.title"));//Financial Statements,
    if(Wtf.accountpayableManagementFlag){
        widgetArr.push(WtfGlobal.getLocaleText("acc.dashboard.dashboardlinks.purchasetransactionreports.title"));//Purchase Transaction Reports,,
    }
    if(Wtf.accountsreceivablesalesFlag){
        widgetArr.push(WtfGlobal.getLocaleText("acc.dashboard.dashboardlinks.salestransactionreports.title"));//Sales Transaction Reports,, 
    }
    if(Wtf.masterManagementFlag){
        widgetArr.push(WtfGlobal.getLocaleText("acc.dashboard.dashboardlinks.masters.title"));//Masters,
    }
    widgetArr.push(WtfGlobal.getLocaleText("acc.dashboard.dashboardlinks.accountmgnt.title"));// Account Management,"
    widgetArr.push(WtfGlobal.getLocaleText("acc.dashboard.dashboardlinks.updates.title"));//Updates,
    widgetArr.push(WtfGlobal.getLocaleText("acc.dashboard.dashboardlinks.admin.title"));//Administration
    if(Wtf.accountpayableManagementFlag){
        panelArr.push({
        
            config1:[{
                url:'ACCWidgetDashboard/getAccountingModuleWidget.do',
                numRecs:1,
                isPaging: false,
                isSearch: false,
                template:new Wtf.XTemplate(
                    "<tpl>"+             
                    "<div style='padding-left:0px; overflow: auto; background:transparent no-repeat scroll 0 0;'>{update}</div>" +
                    "</tpl>"
                    ),
                emptyText:WtfGlobal.getLocaleText("acc.dashboard.mylinks.emptytxt"),//'No Modules',
                headerHtml:"",
                paramsObj: {
                    flag:6,
                    searchField:'announceval',
                    id : "purchasemgntwidget_drag"
                }
            }],
            draggable:true,
            border:true,
            title: getWidgetTitle(WtfGlobal.getLocaleText("acc.dashboard.dashboardlinks.purchasemgnt.title")),// Purchase Management,
            id : "purchasemgntwidget_drag",
            tools: getToolsArray('jspfiles/rss.jsp?i=announcements&u=' + loginid)
        });
    }
    if(Wtf.accountsreceivablesalesFlag){
        panelArr.push({      
            config1:[{
                url:'ACCWidgetDashboard/getAccountingModuleWidget.do',
                numRecs:1,
                isPaging: false,
                isSearch: false,
                template:new Wtf.XTemplate(
                    "<tpl>"+             
                    "<div style='padding-left:0px; overflow: auto; background:transparent no-repeat scroll 0 0;'>{update}</div>" +
                    "</tpl>"
                    ),
                emptyText:WtfGlobal.getLocaleText("acc.dashboard.mylinks.emptytxt"),//'No Modules',
                headerHtml:"",
                paramsObj: {
                    flag:7,
                    searchField:'announceval',
                    id : "salesbillingwidget_drag"
                }
            }],
            draggable:true,
            border:true,
            title: getWidgetTitle(WtfGlobal.getLocaleText("acc.dashboard.dashboardlinks.salesmgnt.title")),// Sales and Billing Management,
            id : "salesbillingwidget_drag",
            tools: getToolsArray('jspfiles/rss.jsp?i=announcements&u=' + loginid)
        });
    }
    panelArr.push({      
        config1:[{
            url:'ACCWidgetDashboard/getAccountingModuleWidget.do',
            numRecs:10,
            isPaging: false,
            isSearch: false,
            template:new Wtf.XTemplate(
                '<tpl><div class="ctitle listpanelcontent" style="padding:0px !important;">',
                "<div style='padding-left:15px; background:transparent url(../../images/Financial--Statement.png) no-repeat scroll 0 0;'>{update}</div>" +
                '</div></tpl>'
                ),
            emptyText:WtfGlobal.getLocaleText("acc.dashboard.mylinks.emptytxt"),//'No Modules',
            headerHtml:"",
            paramsObj: {
                flag:8,
                searchField:'announceval',
                id : "financialstmtwidget_drag"
            }
        }],
        draggable:true,
        border:true,
        title: getWidgetTitle(WtfGlobal.getLocaleText("acc.dashboard.dashboardlinks.financialstmt.title")),// Financial Statements,
        id : "financialstmtwidget_drag",
        tools: getToolsArray('jspfiles/rss.jsp?i=announcements&u=' + loginid)
    });
    if(Wtf.accountpayableManagementFlag){
        panelArr.push({      
            config1:[{
                url:'ACCWidgetDashboard/getAccountingModuleWidget.do',
                numRecs:10,
                isPaging: false,
                isSearch: false,
                template:new Wtf.XTemplate(
                    '<tpl><div class="ctitle listpanelcontent" style="padding:0px !important;">',
                    "<div style='padding-left:15px; background:transparent url(../../images/Transaction-Record.png) no-repeat scroll 0 0;'>{update}</div>" +
                    '</div></tpl>'
                    ),
                emptyText:WtfGlobal.getLocaleText("acc.dashboard.mylinks.emptytxt"),//'No Modules',
                headerHtml:"",
                paramsObj: {
                    flag:9,
                    searchField:'announceval',
                    id : "purchasetransactionreportwidget_drag"
                }
            }],
            draggable:true,
            border:true,
            title: getWidgetTitle(WtfGlobal.getLocaleText("acc.dashboard.dashboardlinks.purchasetransactionreports.title")),//Purchase Transaction Reords,
            id : "purchasetransactionreportwidget_drag",
            tools: getToolsArray('jspfiles/rss.jsp?i=announcements&u=' + loginid)
        });
    }
    if(Wtf.accountsreceivablesalesFlag){
        panelArr.push({      
            config1:[{
                url:'ACCWidgetDashboard/getAccountingModuleWidget.do',
                numRecs:10,
                isPaging: false,
                isSearch: false,
                template:new Wtf.XTemplate(
                    '<tpl><div class="ctitle listpanelcontent" style="padding:0px !important;">',
                    "<div style='padding-left:15px; background:transparent url(../../images/Transaction-Record.png) no-repeat scroll 0 0;'>{update}</div>" +
                    '</div></tpl>'
                    ),
                emptyText:WtfGlobal.getLocaleText("acc.dashboard.mylinks.emptytxt"),//'No Modules',
                headerHtml:"",
                paramsObj: {
                    flag:10,
                    searchField:'announceval',
                    id : "salestransactionreportwidget_drag"
                }
            }],
            draggable:true,
            border:true,
            title: getWidgetTitle(WtfGlobal.getLocaleText("acc.dashboard.dashboardlinks.salestransactionreports.title")),// Sales Transaction Reords,
            id : "salestransactionreportwidget_drag",
            tools: getToolsArray('jspfiles/rss.jsp?i=announcements&u=' + loginid)
        });
    }
    if(Wtf.masterManagementFlag==true){
        panelArr.push({      
            config1:[{
                url:'ACCWidgetDashboard/getAccountingModuleWidget.do',
                numRecs:10,
                isPaging: false,
                isSearch: false,
                template:new Wtf.XTemplate(
                    '<tpl><div class="ctitle listpanelcontent" style="padding:0px !important;">',
                    "<div style='padding-left:15px; background:transparent url(../../images/Masters.png) no-repeat scroll 0 0;'>{update}</div>" +
                    '</div></tpl>'
                    ),
                emptyText:WtfGlobal.getLocaleText("acc.dashboard.mylinks.emptytxt"),//'No Modules',
                headerHtml:"",
                paramsObj: {
                    flag:11,
                    searchField:'announceval',
                    id : "masterswidget_drag"
                }
            }],
            draggable:true,
            border:true,
            title: getWidgetTitle(WtfGlobal.getLocaleText("acc.dashboard.dashboardlinks.masters.title")),// Masters,
            id : "masterswidget_drag",
            tools: getToolsArray('jspfiles/rss.jsp?i=announcements&u=' + loginid)
        });
    } 
    panelArr.push({      
        config1:[{
            url:'ACCWidgetDashboard/getAccountingModuleWidget.do',
            numRecs:10,
            isPaging: false,
            isSearch: false,
            template:new Wtf.XTemplate(
                '<tpl><div class="ctitle listpanelcontent" style="padding:0px !important;">',
                "<div style='padding-left:15px; background:transparent url(../../images/Financial--Statement.png) no-repeat scroll 0 0;'>{update}</div>" +
                '</div></tpl>'
                ),
            emptyText:WtfGlobal.getLocaleText("acc.dashboard.mylinks.emptytxt"),//'No Modules',
            headerHtml:"",
            paramsObj: {
                flag:12,
                searchField:'announceval',
                id : "accountmgntwidget_drag"
            }
        }],
        draggable:true,
        border:true,
        title: getWidgetTitle(WtfGlobal.getLocaleText("acc.dashboard.dashboardlinks.accountmgnt.title")),// Account Management,
        id : "accountmgntwidget_drag",
        tools: getToolsArray('jspfiles/rss.jsp?i=announcements&u=' + loginid)
    });
        
    panelArr.push({      
        config1:[{
            url:'ACCWidgetDashboard/getAccountingModuleWidget.do',
            numRecs:10,
            isPaging: true,
            isSearch: false,
            template:new Wtf.XTemplate(
                "<tpl><div class='workspace'>"+
                "<div>" +
                "<div style='padding-left:15px; background:transparent no-repeat scroll 0 0;'>{desc}</div>" +
                "<div style='padding-left:15px; background:transparent url(../../images/bullet2.gif) no-repeat scroll 0 0;'>{update}</div>" +
                "</div>" +
                "</div></tpl>"
                ),
            emptyText:WtfGlobal.getLocaleText("acc.dashboard.mylinks.emptytxt"),//'No Updates',
            headerHtml:" ",
            paramsObj: {
                flag:13,
                searchField:'name',
                id : "updateswidget_drag"
            }
        }],
        draggable:true,
        border:true,
        title: getWidgetTitle(WtfGlobal.getLocaleText("acc.dashboard.dashboardlinks.updates.title")),// Updates,
        id : "updateswidget_drag",
        tools: getToolsArray('jspfiles/rss.jsp?i=announcements&u=' + loginid)
    });
    
    panelArr.push({      
        config1:[{
            url:'ACCWidgetDashboard/getAccountingModuleWidget.do',
            numRecs:10,
            isPaging: false,
            isSearch: false,
            template:new Wtf.XTemplate(
                '<tpl><div class="ctitle listpanelcontent" style="padding:0px !important;">',
                "<div style='padding-left:15px; background:transparent url(../../images/Financial--Statement.png) no-repeat scroll 0 0;'>{update}</div>" +
                '</div></tpl>'
                ),
            emptyText:WtfGlobal.getLocaleText("acc.dashboard.mylinks.emptytxt"),//'No Modules',
            headerHtml:"",
            paramsObj: {
                flag:14,
                searchField:'announceval',
                id : "adminwidget_drag"
            }
        }],
        draggable:true,
        border:true,
        title: getWidgetTitle(WtfGlobal.getLocaleText("acc.dashboard.dashboardlinks.admin.title")),// Administration,
        id : "adminwidget_drag",
        tools: getToolsArray('jspfiles/rss.jsp?i=announcements&u=' + loginid)
    });
    if(Wtf.accountpayableManagementFlag){
        widgetIdArray.push("purchasemgntwidget_drag");
    }
    if(Wtf.accountsreceivablesalesFlag){
        widgetIdArray.push("salesbillingwidget_drag");
    }
    widgetIdArray.push("financialstmtwidget_drag");
    if(Wtf.accountpayableManagementFlag){
        widgetIdArray.push("purchasetransactionreportwidget_drag");
    }
    if(Wtf.accountsreceivablesalesFlag){
        widgetIdArray.push("salestransactionreportwidget_drag");
    }
    if(Wtf.masterManagementFlag){
        widgetIdArray.push("masterswidget_drag");
    }
    widgetIdArray.push("accountmgntwidget_drag");
    widgetIdArray.push("updateswidget_drag"); 
    widgetIdArray.push("adminwidget_drag"); 
}

function getWidgetTitle (title) {
    return "<div wtf:qtip='"+WtfGlobal.getLocaleText("acc.dashboard.generaltip")+"'>"+title+"</div>";//'Click to drag and place widget anywhere on the dashboard.'
}

var categoryarray  = [],count=0;
function showcategory(val){
    if(Wtf.get(val).dom.style.display=='none'){
        Wtf.get(val).dom.style.display = 'block';
        Wtf.get(val).dom.style.paddingLeft = '15px';
        Wtf.get('x'+val).dom.className = 'x-tool mycategory-expand';
        var  flag=1;
        var i=0;
        for(i=0;i<count;i++){
            if(Wtf.get(categoryarray[i])!=null){
                if(categoryarray[i]==val){
                    flag=0;
                }else{
                    Wtf.get(categoryarray[i]).dom.style.display = 'none';
                    Wtf.get('x'+categoryarray[i]).dom.className = 'x-tool mycategory-collapse';
                }
            }
            if(flag==1){
                categoryarray[count] = val;
                count++;
            }
        }
    }
    else{
        Wtf.get(val).dom.style.display = 'none';
        Wtf.get('x'+val).dom.className = 'x-tool mycategory-collapse';
    }
}
function loadtab_main(id,book){
    id = '   '+id ;
    book.replace(/'/,'\'');

    mainPanel.loadTab('communityHome.html',id,book,'navareadashboard',1,true);
}

Wtf.Panel.prototype.afterRender = Wtf.Panel.prototype.afterRender.createInterceptor(function() {// Fix For IE  Scrollable Player Bug Fix
    if(this.autoScroll) {
        this.body.dom.style.position = 'relative';
    }
});

var isToAppendArray=new Array();
for(var i=0;i<widgetIdArray.length;i++){
    isToAppendArray[i]=1;
}

var column_wise_widgets="";
var mainLM=new Wtf.LoadMask(mainPanel.el.dom,{
    msg:WtfGlobal.getLocaleText("acc.dashboard.Loadingmsg")//"Loading Widget Thumbnails..."
});
mainLM.show();

getWidgetFrame();

function getWidgetFrame (){

    Wtf.Ajax.requestEx({
        url: 'ACCWidgetDashboard/getWidgetFrame.do',
        params:{
            flag:1,
            start:0,
            start1:0,
            limit:10,
            limitReport:10,
            dataFlag:false
        }
    },this, function(res) {
        Wtf.Ajax.timeout = '30000';
        var responseWidgets = eval( '(' + res.colLength+ ')');
        var reportWidgets = res.reportwidget;
        var widgetFrame = res.widgetFrame;
        var timezone=false;
        if (res.Timezone && res.Timezone == true) {
            timezone=true;
        }
        getWidgetData(widgetFrame);

        var res = eval( '(' + res.widgetData+ ')');
        var dataFlag = false;
        var _col1=new Array();
        var index=0;
        column_wise_widgets=responseWidgets;
        for(var i=0;i<responseWidgets.col1.length;i++){
            index=widgetIdArray.indexOf(responseWidgets.col1[i].id);
            isToAppendArray[index]=0;
            if(panelArr[index]!==undefined)
                Wtf.getCmp('portal_container_box1').add(createNewPanel(panelArr[index],res,dataFlag));
            Wtf.getCmp('portal_container').doLayout();
        }
        var _col2=new Array();
        for(i=0;i<responseWidgets.col2.length;i++){
            index=widgetIdArray.indexOf(responseWidgets.col2[i].id);
            isToAppendArray[index]=0;
            if(panelArr[index]!==undefined)
                Wtf.getCmp('portal_container_box2').add(createNewPanel(panelArr[index],res,dataFlag));
            Wtf.getCmp('portal_container').doLayout();
        }
        var _col3=new Array();
        for(i=0;i<responseWidgets.col3.length;i++){
            index=widgetIdArray.indexOf(responseWidgets.col3[i].id);
            isToAppendArray[index]=0;
            if(panelArr[index]!==undefined)
                Wtf.getCmp('portal_container_box3').add(createNewPanel(panelArr[index],res,dataFlag));
            Wtf.getCmp('portal_container').doLayout();
        }
        appendRemainingWidgets();
        mainLM.hide();
        
        if (timezone) {
                document.getElementById("header").style.height = "50px";
                document.getElementById("headTimezone").style.display = "block";
                Wtf.getCmp('viewport').doLayout();
        }
        Wtf.getCmp("tabdashboard").doLayout.defer(1000);
    }, signOut)

}

function handleReportWidget(reportWidget) {
    for(var cnt = 0 ; cnt < reportWidget.length; cnt++) {
        getReportWidgetObject(parseInt(reportWidget[cnt].reportcode),reportWidget[cnt].reportname);
        var index = (cnt+1)%3;
        if(index == 0)
            index = 3;
        Wtf.getCmp('portal_container_box'+index).add(createNewPanel(panelArr[panelArr.length-1],reportWidget,true));
        Wtf.getCmp('portal_container').doLayout();
    }
}

function getReportWidgetObject(reportcode, reportname) {
    panelArr.push({
        config0:[{
            numRecs:10,
            template:new Wtf.XTemplate(
                "<tpl><div class='workspace listpanelcontent'>"+
                "<div>" +
                "<div style='padding-left:15px; background:transparent no-repeat scroll 0 0;'>{desc}</div>" +
                "<div style='padding-left:15px; background:transparent url(../../images/bullet2.gif) no-repeat scroll 0 0;'>{update}</div>" +
                "</div>" +
                "</div></tpl>"
                ),
            isPaging: true,
            emptyText:WtfGlobal.getLocaleText("acc.dashboard.widget.mtytext"),//'No Updates',
            isSearch: false,
            headerHtml : '',
            linkcode : 1
        }
        ],
        title: reportname,
        draggable:true,
        isCallRequest : true,
        border:true,
        chartdetails:getPaneldetails(reportcode),//Leads Pipelined Report
        barchart:true,
        defaultChartView:true,
        tools: getToolsArrayForReport()
    });
}
function getWidgetData(widgetFrame){

    WtfGlobal.setAjaxTimeOut();
    Wtf.Ajax.requestEx({
        url:'ACCWidgetDashboard/getWidgetData.do',
        params:{
            flag:1,
            start:0,
            start1:0,
            limit:10,
            limitReport:10,
            widgetFrame:widgetFrame
        }
    }, this, function(res){
        Wtf.Ajax.timeout = '30000';
        var responseWidgets = column_wise_widgets;
        var res = eval( '(' + res.widgetData+ ')');
        var _col1=new Array();
        var index=0;

        if(responseWidgets!=""){
            for(var i=0;i<responseWidgets.col1.length;i++){
                index=widgetIdArray.indexOf(responseWidgets.col1[i].id);
                isToAppendArray[index]=0;
                if(panelArr[index]!==undefined)
                    loadWidgetData(panelArr[index],res)
                Wtf.getCmp('portal_container').doLayout();
            }
            var _col2=new Array();
            for(i=0;i<responseWidgets.col2.length;i++){
                index=widgetIdArray.indexOf(responseWidgets.col2[i].id);
                isToAppendArray[index]=0;
                if(panelArr[index]!==undefined)
                    loadWidgetData(panelArr[index],res)
                Wtf.getCmp('portal_container').doLayout();
            }
            var _col3=new Array();
            for(i=0;i<responseWidgets.col3.length;i++){
                index=widgetIdArray.indexOf(responseWidgets.col3[i].id);
                isToAppendArray[index]=0;
                if(panelArr[index]!==undefined)
                    loadWidgetData(panelArr[index],res)
                Wtf.getCmp('portal_container').doLayout();
            }
        }

    }, function(){
        });


}
function loadWidgetData(setting,res){
    if(setting !== undefined) {
        if (setting.config1 != null) {
            for(var count = 0;count<setting.config1.length;count++){
                this.count = count;
                this.newObj = setting.config1[count];
                if(res) {
                    if(res.purchasemgntwidget_drag!=undefined && this.newObj.paramsObj.flag==6){
                        Wtf.getCmp("purchasemgntwidget_drag").CrmModuleWidget(res.purchasemgntwidget_drag);
                    }else  if(res.salesbillingwidget_drag!=undefined && this.newObj.paramsObj.flag==7){
                        Wtf.getCmp("salesbillingwidget_drag").CrmModuleWidget(res.salesbillingwidget_drag);
                    }else  if(res.financialstmtwidget_drag!=undefined && this.newObj.paramsObj.flag==8){
                        if (res.financialstmtwidget_drag.data == 0) {
                            RemovePanel(Wtf.getCmp("financialstmtwidget_drag"));
                        } else {
                            Wtf.getCmp("financialstmtwidget_drag").CrmModuleWidget(res.financialstmtwidget_drag);
                        }
                    } else if (res.purchasetransactionreportwidget_drag != undefined && this.newObj.paramsObj.flag == 9) {
                        if (res.purchasetransactionreportwidget_drag.data == 0) {
                            RemovePanel(Wtf.getCmp("purchasetransactionreportwidget_drag"));
                        } else {
                            Wtf.getCmp("purchasetransactionreportwidget_drag").CrmModuleWidget(res.purchasetransactionreportwidget_drag);
                        }
                    } else if (res.salestransactionreportwidget_drag != undefined && this.newObj.paramsObj.flag == 10) {
                        if (res.salestransactionreportwidget_drag.data == 0) {
                            RemovePanel(Wtf.getCmp("salestransactionreportwidget_drag"));
                        } else {
                            Wtf.getCmp("salestransactionreportwidget_drag").CrmModuleWidget(res.salestransactionreportwidget_drag);
                        }
                    } else if (res.masterswidget_drag != undefined && this.newObj.paramsObj.flag == 11) {
                        if (res.masterswidget_drag.data == 0) {
                            RemovePanel(Wtf.getCmp("masterswidget_drag"));
                        }
                        else {
                            Wtf.getCmp("masterswidget_drag").CrmModuleWidget(res.masterswidget_drag);
                        }
                    } else if (res.accountmgntwidget_drag != undefined && this.newObj.paramsObj.flag == 12) {
                        if (res.accountmgntwidget_drag.data == 0) {
                            RemovePanel(Wtf.getCmp("accountmgntwidget_drag"));
                        } else {
                            Wtf.getCmp("accountmgntwidget_drag").CrmModuleWidget(res.accountmgntwidget_drag);
                        }
                    } else if (res.updateswidget_drag != undefined && this.newObj.paramsObj.flag == 13) {
                        Wtf.getCmp("updateswidget_drag").CrmModuleWidget(res.updateswidget_drag);
                    } else if (res.adminwidget_drag != undefined && this.newObj.paramsObj.flag == 14) {
                        if (res.adminwidget_drag.data == 0) {
                            RemovePanel(Wtf.getCmp("adminwidget_drag"));
                        }
                        else {
                            Wtf.getCmp("adminwidget_drag").CrmModuleWidget(res.adminwidget_drag);
                        }
                    }
                }

            }
        } else if (setting.config0 != null) {
            for(var count = 0;count<setting.config0.length;count++){
                this.count = count;
                this.newObj = setting.config0[count];

                if(res.Campaign!=undefined && this.newObj.paramsObj.type==0){
                    Wtf.getCmp(Wtf.moduleWidget.campaign).dashBoardWidgetRequest(res.Campaign[0]);
                } else if(res.Lead!=undefined && this.newObj.paramsObj.type==1){
                    Wtf.getCmp(Wtf.moduleWidget.lead).dashBoardWidgetRequest(res.Lead[0]);
                } else if(res.Account!=undefined && this.newObj.paramsObj.type==2){
                    Wtf.getCmp(Wtf.moduleWidget.account).dashBoardWidgetRequest(res.Account[0]);
                } else if(res.Contact!=undefined && this.newObj.paramsObj.type==3){
                    Wtf.getCmp(Wtf.moduleWidget.contact).dashBoardWidgetRequest(res.Contact[0]);
                } else if(res.Opportunity!=undefined && this.newObj.paramsObj.type==4){
                    Wtf.getCmp(Wtf.moduleWidget.opportunity).dashBoardWidgetRequest(res.Opportunity[0]);
                } else if(res.Case!=undefined && this.newObj.paramsObj.type==5){
                    Wtf.getCmp(Wtf.moduleWidget.cases).dashBoardWidgetRequest(res.Case[0]);
                } else if(res.Activity!=undefined && this.newObj.paramsObj.type==6){
                    Wtf.getCmp(Wtf.moduleWidget.activity).dashBoardWidgetRequest(res.Activity[0]);
                } else if(res.Product!=undefined && this.newObj.paramsObj.type==7){
                    Wtf.getCmp(Wtf.moduleWidget.product).dashBoardWidgetRequest(res.Product[0]);
                }
            }
        } else {
            if (setting.url != null) {
                return (new Wtf.WtfIframeWidgetComponent(setting));
            }else {
                return (new Wtf.WtfWidgetComponent(setting));
            }
        }
    }
}
function RemovePanel(panel) {  
    var tt = panel.title;
    panel.ownerCt.remove(panel, true);
    panel.destroy();
    removeWidget(tt);
}
var style = "background:white;";
if(Wtf.DashBoardImageFlag){
    style='background-image: url("../../images/store/Accounting/'+ companyid +'_dashboard.png");background-size:cover;background-repeat: no-repeat;position:absolute;width:100%;height:100%;text-align: center;';
}
var paneltop = new Wtf.Panel({
    border: false,
    layout: 'border',
    frame: false,
    items: [{
        region: 'center',
        xtype: 'portal',
        id:'portal_container',
        bodyStyle:style,
        border: false,
        //        html:renderHelpDiv(),
        items: [{
            columnWidth: .33,
            cls: 'portletcls',
            id: 'portal_container_box1',
            border: false
        }, {
            columnWidth: .33,
            border: false,
            cls: 'portletcls',
            id: 'portal_container_box2'
        }, {
            columnWidth: .33,
            cls: 'portletcls',
            id: 'portal_container_box3',
            border: false
        }]
    }, {
        region: "south",
        height: 145,
        id : "dashboard-south",
        title: WtfGlobal.getLocaleText("acc.dashboard.southregion.title"),//"Add Dashboard Widgets",
        autoScroll:true,
        collapsible: true,
        collapsed: true,
        //        hidden: true,
        split: true,
        frame: true,
        html: '<div class="widgets" id="widgets">' +
    '<ul id="widgetUl">' +
    '</ul>' +
    '</div>'
    }]
});
Wtf.getCmp('portal_container').on('drop',function(e){
    Wtf.Ajax.requestEx({
        url: 'ACCWidgetDashboard/changeWidgetState.do',
        params:{
            flag:4,
            colno:e.columnIndex+1,
            position:e.position,
            wid:e.panel.id
        }
    }, this, function(){
        Wtf.getCmp('portal_container').doLayout();
    }, function(){});
},this);

Wtf.getCmp("tabdashboard").add(paneltop);
Wtf.getCmp("tabdashboard").doLayout();
/************initially 3 widgets added to top widget bar************/
var titleSpan = document.createElement("div");
titleSpan.innerHTML = WtfGlobal.getLocaleText("acc.dashboard.southregion.title");//"Add Dashboard Widgets";
titleSpan.id="southdash";
titleSpan.className = "collapsed-header-title";
Wtf.getCmp("dashboard-south").container.dom.lastChild.appendChild(titleSpan);

Wtf.QuickTips.register({
    target:  Wtf.get('southdash'),
    trackMouse: true,
    text: WtfGlobal.getLocaleText("acc.dashboard.unexpanded.title")//'Click to expand Dashboard Widget'
});
Wtf.QuickTips.enable();

function appendRemainingWidgets(){
    for(var i=0;i<isToAppendArray.length;i++){
        if(isToAppendArray[i]==1){
            appendWidget(i);
        }
    }
}

function takeTour(){
    if(document.getElementById('titlehelp') == null) {
        showHelp(1);
    }
}

function saveHelpState(){
    var chek = Wtf.get('showHelpCheck');
    if(chek && chek.dom.checked){
        Wtf.Ajax.requestEx({
            url: "Common/ProfileHandler/updateHelpflag.do",
            params:{
                userid:loginid,
                helpflag:1
            }
        },this,function() {
            },function() {});
    }
    noThanks();
}
function noThanks(){
    if(Wtf.get('dashhelp')){
        Wtf.get('dashhelp').slideOut('t',{
            remove: true
        });
    }
}
function renderHelpDiv(){
    var txt = helpFlag==1?"":"<div class='outerHelp' id='dashhelp'>" +
    "<div style='float:left; padding-left:1%; margin-top:-1px;'><img src='../../images/alerticon.jpg'/></div>" +
    "<div class='helpHeader'>"+WtfGlobal.getLocaleText("crm.deskera.helptext.newtodeskera")+"</div><div class='helpContent' id='wtf-gen285'>"+
    "<div style='padding-top: 5px; float: left;'><a href='#' class='helplinks guideme' onclick='takeTour()'>"+WtfGlobal.getLocaleText("crm.deskera.quicktourlink")+"</a>"+
    "  <a class='helplinks nothanks' href='#' onclick='saveHelpState()'>"+WtfGlobal.getLocaleText("crm.deskera.nothanx.msg")+"</a></div>"+
    "<div style='float:right; margin-right:10px;'><div class='checkboxtext'>"+
    "<input type='checkbox' id='showHelpCheck' style='margin-right: 5px; vertical-align: middle;'/><span style='margin-right:1%; color:#15428B;'>"+WtfGlobal.getLocaleText("crm.deskera.donotshowmsg")+"</span></div>"+
    "<span style='color:#333333; cursor:pointer; margin-top:1px; padding-top:5px; float:right;' id='closehelp' onclick='saveHelpState()'><img style='height:12px; width:12px;'src='../../images/cancel16.png' align='bottom'/></span></div></div></div>";
    return txt;
}

function requestForWidgetRemove(wid) {
    Wtf.Ajax.requestEx({
        url:'ACCWidgetDashboard/removeWidgetFromState.do',
        params:{
            flag:2,
            wid:wid
        }
    }, this, function(){
        }, function(){});
}

