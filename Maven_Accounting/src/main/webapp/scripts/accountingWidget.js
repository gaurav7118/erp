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


Wtf.WtfCustomCrmPanel = function(config,res,dataFlag){
    Wtf.WtfCustomCrmPanel.superclass.constructor.call(this, config);
    this.oldinnerHtml  ="";
    this.o1  ="";
    this.o2  ="";
    this.o3  ="";
    this.cmbcnt=1;
    this.config0.emptyText='No result found';
    this.config0.tableClassName='datagridonDB';
    this.config0.tableHeader='';
    this.res=res;
    this.dataFlag=dataFlag;
}
Wtf.extend(Wtf.WtfCustomCrmPanel, Wtf.Panel,{
    closable : false,
    onRender: function(config) {
       Wtf.WtfCustomCrmPanel.superclass.onRender.call(this, config);
//       if(this.tools.quickwizardlink)
//            this.getWorkSpaceLinks();
       this.header.replaceClass('x-panel-header','portlet-panel-header');
       for(var count = 0;count<this.config0.length;count++){
               this.count = count;
               this.newObj = this.config0[count];
               if(this.dataFlag!=false){
                   if(this.res.Campaign!=undefined && this.newObj.paramsObj.type==0){
                       this.dashBoardWidgetRequest(this.res.Campaign[0]);
                   } else if(this.res.Lead!=undefined && this.newObj.paramsObj.type==1){
                       this.dashBoardWidgetRequest(this.res.Lead[0]);
                   } else if(this.res.Account!=undefined && this.newObj.paramsObj.type==2){
                       this.dashBoardWidgetRequest(this.res.Account[0]);
                   } else if(this.res.Contact!=undefined && this.newObj.paramsObj.type==3){
                       this.dashBoardWidgetRequest(this.res.Contact[0]);
                   } else if(this.res.Opportunity!=undefined && this.newObj.paramsObj.type==4){
                       this.dashBoardWidgetRequest(this.res.Opportunity[0]);
                   } else if(this.res.Case!=undefined && this.newObj.paramsObj.type==5){
                       this.dashBoardWidgetRequest(this.res.Case[0]);
                   } else if(this.res.Activity!=undefined && this.newObj.paramsObj.type==6){
                       this.dashBoardWidgetRequest(this.res.Activity[0]);
                   } else if(this.res.Product!=undefined && this.newObj.paramsObj.type==7){
                       this.dashBoardWidgetRequest(this.res.Product[0]);
                   } else{
                        if(this.isCallRequest)
                            this.dashBoardReportWidget();
                        else
                            this.callRequest();
                   }
               }
       }
    },

    getWorkSpaceLinks : function() {
        this.code = this.config0[0].linkcode;
        var items = "";
        switch(this.code) {
            case 0 :
                this.campPanel = new Wtf.quickadd({
                    treeflag:true,
                    dashcomp:Wtf.moduleWidget.campaign,
                    configType:"Campaign",
                    compid:"campcomp",
                    border: false,
                    width:'300',
                    paramObj:{flag:20,auditEntry:1},
                    url:Wtf.req.springBase+'Campaign/action/saveCampaigns.do',
                    actionCode:0,
                    jsonstr:{isCampaignNameEdit:true, campaignid:'0',campaignownerid:loginid}

                })
                items = this.campPanel;
                break;
            case 1 ://lead
                this.leadPanel = new Wtf.quickadd({
                    treeflag:true,
                    dashcomp:Wtf.moduleWidget.lead,
                    configType:"Lead",
                    compid:"leadcomp",
                    border: false,
                    width:'300',
                    paramObj:{flag:20,auditEntry:1},
                    url:Wtf.req.springBase+'Lead/action/saveLeads.do',
                    actionCode:1,
                    jsonstr:{leadid:'0',leadownerid:loginid}

                })
                items = this.leadPanel;
                break;
            case 2 : //account
                this.accountPanel = new Wtf.quickadd({
                    treeflag:true,
                    dashcomp:Wtf.moduleWidget.account,
                    configType:"Account",
                    compid:"accountcomp",
                    border: false,
                    width:'300',
                    paramObj:{flag:21},
                    url:Wtf.req.springBase+"Account/action/saveAccounts.do",
                    actionCode:2,
                    jsonstr:{accountid:'0',accountownerid:loginid}
                })
                items = this.accountPanel;
                break;
            case 3 ://contact
                this.contactPanel = new Wtf.quickadd({
                    dashcomp:Wtf.moduleWidget.contact,
                    configType:"Contact",
                    compid:"contactcomp",
                    border: false,
                    width:'300',
                    paramObj:{flag:22,auditEntry:1},
                    url:Wtf.req.springBase+'Contact/action/saveContacts.do',
                    actionCode:3,
                    jsonstr:{contactid:'0',contactownerid:loginid}
                })
                items = this.contactPanel;
                break;
            case 4 ://opportunity
                this.OpportunityPanel = new Wtf.quickadd({
                    dashcomp:Wtf.moduleWidget.opportunity,
                    configType:"Opportunity",
                    compid:"opportunitycomp",
                    border: false,
                    width:'300',
                    paramObj:{flag:23,auditEntry:1},
                    url:Wtf.req.springBase+'Opportunity/action/saveOpportunities.do',
                    actionCode:4,
                    jsonstr:{oppid:'0',oppownerid:loginid}
                })
                items = this.OpportunityPanel;
                break;
            case 5 ://cases
                this.CasesPanel = new Wtf.quickadd({
                    dashcomp:Wtf.moduleWidget.cases,
                    configType:"Case",
                    compid:"casescomp",
                    border: false,
                    width:'300',
                    paramObj:{flag:33},
                    url:Wtf.req.springBase+'Case/action/saveCases.do',
                    actionCode:5,
                    jsonstr:{caseid:'0',caseownerid:loginid}
                })
                items = this.CasesPanel;

                break;
            case 6 ://activity
                    var itemsAcc=[];
                    itemsAcc = getActivityFields(this,false);
                    this.emailReminder = new Wtf.form.Checkbox({
                        boxLabel:" ",
                        name:'reminder',
                        checked:false,
                        inputValue:'false',
                        width: 50,
                        fieldLabel:WtfGlobal.getLocaleText("crm.masterconfig.emailnotificationsetting.label")
                    });
                    itemsAcc.push(this.emailReminder);
                
                    items = new Wtf.Panel({
                        border: false,
                        id:(Wtf.isSafari)?"":"activityForm",
                        layout:'form',
                        labelWidth:100,
                        defaults:{
                            anchor:'80%'
                        },
                        items : itemsAcc
                    });
                break;
            case 7 ://Product
                this.ProductPanel = new Wtf.quickadd({
                    treeflag:true,
                    dashcomp:Wtf.moduleWidget.product,
                    configType:"Product",
                    compid:"productcomp",
                    border: false,
                    width:'300',
                    paramObj:{flag:31,auditEntry:1},
                    url:Wtf.req.springBase+'Product/action/saveProducts.do',
                    actionCode:7,
                    jsonstr:{productid:'0',ownerid:loginid}
                })
                items = this.ProductPanel;
                break;
        }
        this.form1= new Wtf.Panel({
            border:false,
            items:items,
            layout:'fit',
            bodyStyle : 'font-size:10px;padding:8px 7% 0px 10px',
            buttonAlign:'right',
            defaults:{
            	defaults:{
        			labelWidth:100,
            		defaults: {
		        		anchor:'100%',
		                xtype:'striptextfield',
		                allowBlank:false,
		                msgTarget: 'qtip'
        			}
        		}
            },
            buttons: [{
                text:WtfGlobal.getLocaleText("crm.SUBMITBTN"),//'Submit',
                scope:this,
                handler:function(){
                        this.saveForm();
                }
            }]
        });
    },
    validateSelection : function(combo,record,index){
        return record.get('hasAccess' );
    },
    getAccountCombo : function() {
        this.accStore = new Wtf.data.Store({
            url:Wtf.req.springBase+"common/GoogleContacts/getAllAccounts.do",
            reader: new Wtf.data.KwlJsonReader({
                root:'data'
            }, Wtf.ComboReader),
            autoLoad:false
        });
        this.accCombo = Wtf.addWidgetComboBox('Account',this,this.accStore,'acccombo');
    },

    writeTemplateToBody:function(innerHTML,lib,ss,pgsize,pager){
       var temp1 = unescape(innerHTML);
       var temp3 ='';
       if(this.config0.length == 4 ) { // for Knowledge Compass, template changes
           temp3 = this.o1 +this.o2 + this.o3 + temp1;
           temp3 = ''
                            +'<div style="background-color:#DFE8F6;padding:7px;float:left;width:99%">'
                            //+ this.o1 +this.o2 + this.o3 +"<div class='search_div' style='margin-right:43px !important;margin-top:-43px !important;padding:2px !important;' onclick='showResults()'>Search</div>"+temp1
                            + this.o1 +this.o2 + this.o3 +"</td><td><div class='search_div' onclick='showResults(\""+this.id+"\")'>Search</div></td></tr></tbody></table>"+temp1
                                +'</div>';
            if(!this.o1) {    // saves the previous templates of combos
               this.o1=lib;
            }else if(!this.o2){
               this.o2=lib;
            } else if(!this.o3){
              this.o3=lib;
            }
       } else
       if(this.config0.length >1)
           {
               var temp2 = this.oldinnerHtml;
               temp3 = '<div style="background-color:#DFE8F6;padding:7px;float:left;width:97%">'
                            +((this.newObj.isSearch)?this.addSearchBar1():"")
                            +'<div style="background-color:#ffffff;padding:1%;float:left;width:95.5%">'
                            + temp2 + lib
                            +((this.newObj.isPaging)?this.paging(pgsize):"")
                            +'</div>'
                            +'</div>';
       }else{
           temp3 = temp1;
       }
       var temp=new Wtf.Template(temp3);
       this.oldinnerHtml += lib;
       temp.overwrite(this.body);
    },

    addSearchBar1:function(searchss){
        var valueStr="";
        if(searchss){
            valueStr = "value = "+searchss;
        }
        var a1 =this.id;
        return('<div style="height:24px;width:97.5%;background-image:url(images/search-field-bg.gif);margin-bottom:6px;">'
           +'<div id="searchdiv\"'+a1+'\" class="search_div" onclick="btnpressed(\''+a1+'\')">Search</div>'
           +'<div class="searchspacer">&nbsp;</div>'
           +'<div style="width: 85%;overflow:hidden;">'
           +'<input '+valueStr+' onkeypress=\"javascript:if(event.keyCode==13)btnpressed(\''+a1+'\');\" style="background-color: transparent;float:left;border:none; padding-top:3px; height:21px; border-left:solid 1px #a0bcda;width:100%;" type="text" id="search'+a1+'" />'
           +'</div>'
           +'</div>');
    },

    paging:function(numpages,searchss){
        var a =this.id;
        if(!searchss) {
             searchss="";
        }
        var to = this.panelcount;
        if(this.panelcount>this.totalCount)
            to = this.totalCount;
        var pagininfo = "<span id='"+a+"pagetext0' style='float:left;padding-top:3px;'> 1 - "+to+" of "+this.totalCount+"</span>";
        var pager = '<div id="pageinfobar'+a+'" class="portlet-paging">'+pagininfo;
        if(numpages>1) {
            pager +='<span  wtf:qtip="'+WtfGlobal.getLocaleText("toolbar.paging.older")+'" id="'+a+'nextpage0" class="pagination-div next-pagination" onclick="pagingRedirect1(\''+a+'\',1,'+this.count+',\''+searchss+'\','+this.panelcount+');">1</span>';
            pager +='<span wtf:qtip="'+WtfGlobal.getLocaleText("toolbar.paging.oldest")+'" id="'+a+'lastpage0" class="pagination-div last-pagination" onclick="pagingRedirect1(\''+a+'\','+(numpages-1)+','+this.count+',\''+searchss+'\','+this.panelcount+');">1</span>';
        }
        pager=pager+'</div>';
        return(pager);
    },

    changePagingBar : function(currentPage){
        var a = this.id;
        var numpages = Math.ceil(this.totalCount/this.panelcount)
        var from = (currentPage * this.panelcount)+1;
        var to = from + this.panelcount-1;
        if(to>this.totalCount)
            to = this.totalCount;
        var pagininfo = "<span id='"+a+"pagetext"+currentPage+"' style='float:left;padding:3px 0px 0px 6px;'> "+from+" - "+to+" of "+this.totalCount+"</span>";
        var pager = '<div id="pageinfobar'+a+'" class="portlet-paging">';
        if(numpages>1 && currentPage==0) {//first page
            pager +=pagininfo;
            pager +='<span wtf:qtip="'+WtfGlobal.getLocaleText("toolbar.paging.older")+'" id="'+a+'nextpage'+currentPage+'" class="pagination-div next-pagination" onclick="pagingRedirect1(\''+a+'\','+(currentPage+1)+','+this.count+',\'\','+this.panelcount+');">1</span>';
            pager +='<span wtf:qtip="'+WtfGlobal.getLocaleText("toolbar.paging.oldest")+'" id="'+a+'lastpage'+currentPage+'" class="pagination-div last-pagination" onclick="pagingRedirect1(\''+a+'\','+(numpages-1)+','+this.count+',\'\','+this.panelcount+');">1</span>';
        } else if(currentPage==(numpages-1) && currentPage!=0){//last page
            pager += '<span wtf:qtip="'+WtfGlobal.getLocaleText("toolbar.paging.newest")+'"id="'+a+'firstpage'+currentPage+'" class="pagination-div first-pagination" onclick="pagingRedirect1(\''+a+'\','+(0)+','+this.count+',\'\','+this.panelcount+');">1</span>';
            pager += '<span wtf:qtip="'+WtfGlobal.getLocaleText("toolbar.paging.newer")+'"id="'+a+'prevpage'+currentPage+'" class="pagination-div prev-pagination" onclick="pagingRedirect1(\''+a+'\','+(currentPage-1)+','+this.count+',\'\','+this.panelcount+');">1</span>';
            pager += pagininfo;
        } else if(numpages>1) {
            pager += '<span wtf:qtip="'+WtfGlobal.getLocaleText("toolbar.paging.newest")+'" id="'+a+'firstpage'+currentPage+'" class="pagination-div first-pagination" onclick="pagingRedirect1(\''+a+'\','+(0)+','+this.count+',\'\','+this.panelcount+');">1</span>';
            pager += '<span wtf:qtip="'+WtfGlobal.getLocaleText("toolbar.paging.newer")+'" id="'+a+'prevpage'+currentPage+'" class="pagination-div prev-pagination" onclick="pagingRedirect1(\''+a+'\','+(currentPage-1)+','+this.count+',\'\','+this.panelcount+');">1</span>';
            pager += pagininfo;
            pager += '<span wtf:qtip="'+WtfGlobal.getLocaleText("toolbar.paging.older")+'" id="'+a+'nextpage'+currentPage+'" class="pagination-div next-pagination" onclick="pagingRedirect1(\''+a+'\','+(currentPage+1)+','+this.count+',\'\','+this.panelcount+');">1</span>';
            pager += '<span wtf:qtip="'+WtfGlobal.getLocaleText("toolbar.paging.oldest")+'" id="'+a+'lastpage'+currentPage+'" class="pagination-div last-pagination" onclick="pagingRedirect1(\''+a+'\','+(numpages-1)+','+this.count+',\'\','+this.panelcount+');">1</span>';
        } else {
            pager += pagininfo;
        }
        pager=pager+'</div>';
        return(pager);
    },
    callRequest:function(url,searchss,pager){
        this.panelcount = (this.newObj.numRecs)?this.newObj.numRecs:this.panelcount;
        var headerHtml = this.newObj.headerHtml;
        var mytestpl=this.newObj.template;
        var xtooltip = this.newObj.xtooltip;
        var formatField = this.newObj.formatField;
        var prefixImage = this.newObj.prefixImage;
        var imageField = this.newObj.imageField;
        var formatFileSize = this.newObj.formatFileSize;
        var quoteFormatField = this.newObj.quoteFormatField;
        var tpl_tool_tip = "";
        var autoHide = "";
        var closable = "";
        var height = "";
        var emptyText = "";
        var shwCombo=this.newObj.isCombo;
        if(this.newObj.emptyText != null){
            emptyText = this.emptyText;
        }

        if(this.newObj.tool_tip != null){
            tpl_tool_tip = this.newObj.tool_tip.tpl_tool_tip;
            autoHide = this.newObj.tool_tip.autoHide;
            closable = this.newObj.tool_tip.closable;
            height = this.newObj.tool_tip.height;
        }

         Wtf.Ajax.requestEx({
             url: (url)?url:this.newObj.url+"?limit="+this.panelcount+"&start=0&searchString=",
			params: this.newObj.paramsObj
         },
         this,
         function(result, req) {
                var innerHTML="";
                var obj = result; //eval('('+result.responseText+')');
                if(xtooltip && formatField){
                    this.newObj.formatField = formatField;
                    this.formatSpecifiedField(obj);
                }

                if(xtooltip && quoteFormatField){
                    this.newObj.quoteFormatField = quoteFormatField;
                    this.formatquoteField(obj);
                }

                if(prefixImage && imageField){
                    this.newObj.imageField = imageField;
                    this.formatFileName(obj);
                }

                if(formatFileSize){
                    this.formatFileSize(obj);
                }
                var lib=this.getDataString(obj,mytestpl,headerHtml,tpl_tool_tip,autoHide,closable,height,emptyText,shwCombo);
                this.totalCount = obj.count;
                innerHTML=this.getPagingString(lib,obj,searchss,pager);
                var pgsize=Math.ceil(obj.count/this.panelcount);
                this.writeTemplateToBody(innerHTML,lib,searchss,pgsize,pager);
                if(this.tools.quickwizardlink){
                	if(this.form1!=undefined)
                	this.form1.destroy();
                    this.getWorkSpaceLinks();
                    this.form1.render('addrec'+this.id);
                }
                this.runChartScript();
                this.showUpdates();
                mytestpl = "";
                this.togglePageCss(this.id,pager);
                if(this.cmb3)
                    document.getElementById("cmb3").selectedIndex=this.cmb3;
                if(url && this.isSearch){
                    document.getElementById("search"+this.id).value=searchss;
                }
                if(this.storeflag){
                        this.storefunction.call();
                }
            },
            function(result, req){
                mytestpl = "";
            }
         );
    },

    dashBoardReportWidget : function() {
        var innerHTML=this.getPagingString();
        this.writeTemplateToBody(innerHTML,undefined,undefined,undefined,undefined);
        this.runChartScript();
        this.showChart();
    },
    dashBoardWidgetRequest:function(res,url,searchss,pager){
        this.panelcount = (this.newObj.numRecs)?this.newObj.numRecs:this.panelcount;
        var headerHtml = this.newObj.headerHtml;
        var mytestpl=this.newObj.template;
        var xtooltip = this.newObj.xtooltip;
        var formatField = this.newObj.formatField;
        var prefixImage = this.newObj.prefixImage;
        var imageField = this.newObj.imageField;
        var formatFileSize = this.newObj.formatFileSize;
        var quoteFormatField = this.newObj.quoteFormatField;
        var tpl_tool_tip = "";
        var autoHide = "";
        var closable = "";
        var height = "";
        var emptyText = "";
        var shwCombo=this.newObj.isCombo;
        if(this.newObj.emptyText != null){
            emptyText = this.emptyText;
        }

        if(this.newObj.tool_tip != null){
            tpl_tool_tip = this.newObj.tool_tip.tpl_tool_tip;
            autoHide = this.newObj.tool_tip.autoHide;
            closable = this.newObj.tool_tip.closable;
            height = this.newObj.tool_tip.height;
        }

            var innerHTML="";
            var obj = res; //eval('('+result.responseText+')');
            if(xtooltip && formatField){
                this.newObj.formatField = formatField;
                this.formatSpecifiedField(obj);
            }

            if(xtooltip && quoteFormatField){
                this.newObj.quoteFormatField = quoteFormatField;
                this.formatquoteField(obj);
            }

            if(prefixImage && imageField){
                this.newObj.imageField = imageField;
                this.formatFileName(obj);
            }

            if(formatFileSize){
                this.formatFileSize(obj);
            }
            var lib=this.getDataString(obj,mytestpl,headerHtml,tpl_tool_tip,autoHide,closable,height,emptyText,shwCombo);
            this.totalCount = obj.count;
            innerHTML=this.getPagingString(lib,obj,searchss,pager);
            var pgsize=Math.ceil(obj.count/this.panelcount);
            this.writeTemplateToBody(innerHTML,lib,searchss,pgsize,pager);
//            if(this.tools.quickwizardlink)
//                this.form1.render('addrec'+this.id);
            this.runChartScript();
                this.showUpdates();
            mytestpl = "";
            this.togglePageCss(this.id,pager);
            if(this.cmb3)
                document.getElementById("cmb3").selectedIndex=this.cmb3;
            if(url && this.isSearch){
                document.getElementById("search"+this.id).value=searchss;
            }
            if(this.storeflag){
                    this.storefunction.call();
            }

    },
    runChartScript : function() {
        var html = document.getElementById('chartview'+this.id).innerHTML;
        var re = /<script\b[\s\S]*?>([\s\S]*?)<\//ig;
        var match;
        if(match = re.exec(html)) {
            eval(match[1]);
        }
    },
    getDataString:function(obj, tpl, headerHtml,tpl_tool_tip,autoHide,closable,height,emptyText,shwCombo) {
        var lib= "";
        if(this.newObj.isTable == true)
            {

                lib = lib +"<table class='"+this.newObj.tableClassName+"' border='0' cellspacing=0 width='100%' style='float:left;margin:0px;'>";
                if(this.newObj.tableHeader != null)
                    {

                        lib = lib +this.newObj.tableHeader;
                    }
            }else{
                lib = lib +"<div class='content-wrapper'>";
            }
                lib = lib +headerHtml;

            if(shwCombo) {
                    lib = lib  +'<select id="cmb'+this.cmbcnt+'" style="width:200px;margin-right:3px;margin-left:5px;" onchange="filterBrands('+this.cmbcnt+')"><option  value="">Select Value --<option>';
                    this.cmbcnt++;
            }

            for(var i=0;i<obj.data.length;i++){
                if(this.newObj.isToolTip == true){
                    var target = "KCUser"+obj.data[i].userid;
                    createtooltip1(target,tpl_tool_tip,autoHide,closable,height);
                }
                if(obj.data.length==0)
                    {
                        lib = emptyText;
                    }
                if(this.pagingflag){
                    if(obj.count == -1){
                        this.config0[0].isPaging = false;
                        this.config0[0].WorkspaceLinks = '';
                        if(this.timeid){
                            clearTimeout(this.timeid);
                        }
                    }
                    else{
                        this.config0[0].isPaging = true;
                        this.config0[0].WorkspaceLinks = signoutLinks;
                        if(this.timeid){
                            clearTimeout(this.timeid);
                        }
                        var  time=parseInt(Wtf.GS.td,10);
                        var  timedelay = time * 60 * 1000;
                        this.timeid = this.doSearch.defer(timedelay,this,[this.url,'']);
                    }
                }
                lib = lib  + tpl.applyTemplate(obj.data[i])
            }

            if(shwCombo)
                    lib = lib  +"</select></div>";
            else {

            if(this.newObj.isTable == true)
                {
                        lib = lib +"</table><br style='clear:both'/>";
                }else{
                        lib = lib +"</div><br style='clear:both'/>";
                }
            }
            this.updates = lib;
            return lib;
    },

getPagingString:function(lib,obj,searchss,pager) {
        this.panelcount = (this.newObj.numRecs)?this.newObj.numRecs:this.panelcount;
        var chartData= this.chartdetails.pieDataUrl;
        var quickAddDiv = "";
        if(this.tools.quickwizardlink)
            quickAddDiv = '<div id="addrec'+this.id+'" style="display:block;background-color:#ffffff;padding:1%;float:left;width:95.5%"></div>';
//        var chartData2= Wtf.req.base + "charData.jsp?mode=551&flag="+this.chartdetails.dataflagWidget;
        var tmpHTML = "";
        var ht=(Wtf.isIE7?"40%":"100%");
        var cls=(Wtf.isChrome?"chrome-widget-graph":"widget-graph");
        if(obj && obj.count!=0) {
            var pgsize=Math.ceil(obj.count/this.panelcount);
            tmpHTML = '<div class="portlet-body">'
                            +((this.newObj.isSearch)?this.addSearchBar1(searchss):"")
                            +'<div id="updatetable'+this.id+'" style="display:none;background-color:#ffffff;padding:1%;float:left;width:95.5%">'
                            +lib
                            +((this.newObj.isPaging)? (pager===undefined ? this.paging(pgsize,searchss) : this.changePagingBar(pager)):"")
                            +'</div>'
                            + '<div id="chartview'+this.id+'" class="'+cls+'" style="display:none;background-color:#ffffff;padding:1%;float:left;width:95.5%;min-height:100px;max-height:300px;"><script>createNewChart("'+this.chartdetails.swf+'", "krwpie", "100%", "'+ht+'", "8", "#FFFFFF","'+this.chartdetails.xmlpath+'","'+chartData+'", "chartview'+this.id+'")</script></div>'
//                            + '<div id="barchartview'+this.id+'" style="display:none;background-color:#ffffff;padding:1%;float:left;width:95.5%;min-height:100px;height:100%;"><script>createNewChart("'+this.chartdetails.swfWidget+'", "krwpie", "100%", "100%", "8", "#FFFFFF","'+this.chartdetails.xmlpathWidget+'","'+chartData2+'", "barchartview'+this.id+'")</script></div>'
                            + quickAddDiv
                            +'</div>';
        } else {
            if(this.newObj.emptyText!=null) {
                tmpHTML = '<div class="portlet-body">'
                        +((this.newObj.isSearch)?this.addSearchBar1():"")
                        +'<div id="updatetable'+this.id+'" style="display:none;background-color:#ffffff;padding:1%;float:left;width:95.5%">'
                        +this.newObj.emptyText
                        +'</div>'
                        + '<div id="chartview'+this.id+'" class="'+cls+'"  style="display:none;background-color:#ffffff;padding:1%;float:left;width:95.5%;min-height:100px;max-height:300px;"><script>createNewChart("'+this.chartdetails.swf+'", "krwpie", "100%", "'+ht+'", "8", "#FFFFFF","'+this.chartdetails.xmlpath+'","'+chartData+'", "chartview'+this.id+'")</script></div>'
//                        + '<div id="barchartview'+this.id+'" style="display:none;background-color:#ffffff;padding:1%;float:left;width:95.5%;min-height:100px;height:100%;"><script>createNewChart("'+this.chartdetails.swfWidget+'", "krwpie", "100%", "100%", "8", "#FFFFFF","'+this.chartdetails.xmlpathWidget+'","'+chartData2+'", "barchartview'+this.id+'")</script></div>'
                        + quickAddDiv
                        +'</div>';
            }
        }
        return tmpHTML;
    },

    doPaging:function(url,offset,searchstr,pager,subPan){
        url+="?limit="+this.panelcount+"&start="+offset+"&searchString="+searchstr;
        this.callRequest(url,searchstr,pager);
    },

    togglePageCss: function(panelid,pager){
        var clickedDiv = document.getElementById(panelid+pager);
        var prevDiv;
        for(var x=0;x<pager;x++){
          prevDiv = document.getElementById(panelid+(x));
          if(prevDiv)
             prevDiv.className="pagination-div deactive-pagination";
        }
        if(clickedDiv)
            clickedDiv.className="pagination-div active-pagination";

    },

    doSearch: function(url,searchstr){
        var str = this.newObj.url;
        var myArr = str.split('?');
        var newUrl=myArr[0]+"?limit="+this.panelcount+"&start=0&searchString="+searchstr;
        this.callRequest(newUrl,searchstr);
    },

    formatSpecifiedField : function(obj){
        for(i=0;i<obj.data.length;i++){
            for(j=0;j<this.newObj.formatField.length;j++){
                obj.data[i][this.newObj.formatField[j]] = getFormattedDate(obj.data[i][this.newObj.formatField[j]]);
            }
        }
    },

    formatquoteField : function(obj){
        for(i=0;i<obj.data.length;i++){
            for(j=0;j<this.newObj.quoteFormatField.length;j++){
                obj.data[i][this.newObj.quoteFormatField[j]] = obj.data[i][this.newObj.quoteFormatField[j]].adjustQuotes();
            }
        }
    },

    formatFileName : function(obj){
        for(var i = 0 ; i < obj.data.length; i++){
            if(obj.data[i].docName != undefined && obj.data[i].docName != null && obj.data[i].docName != ""){
                var imageClass = getimage(obj.data[i].docName);
                obj.data[i].imageClass = imageClass;
            }
        }
    },

    formatFileSize : function(obj){
        obj.data[0].totalSize = getFileSize(obj.data[0].totalSize);
    },

    showUpdates : function(linkedobj) {
        this.disableLinks();
        if(this.tools.updatewizardlink)
            this.tools.updatewizardlink.replaceClass('x-tool-updatewizardlink','x-tool-updatewizardlink1');
        Wtf.get('updatetable'+this.id).dom.style.display = 'block';
        Wtf.get('chartview'+this.id).dom.style.display = 'none';
        if(this.tools.quickwizardlink)
            Wtf.get('addrec'+this.id).dom.style.display = 'none';
//        Wtf.get('barchartview'+this.id).dom.style.display = 'none';
    },

    showQuickAdd : function(linkedobj,code) {
        var destroyForm = true; // decide to destroy form or not
        if(this.form1 && this.tools.quickwizardlink.hasClass('x-tool-quickwizardlink'))
            destroyForm = false;
        this.disableLinks();
        if(!this.form1) {
            this.getWorkSpaceLinks();
            this.form1.render('addrec'+this.id);
        }else{
            if(destroyForm) {
                this.form1.destroy();
                Wtf.get('addrec'+this.id).dom.style.display = 'none';
                this.getWorkSpaceLinks();
                this.form1.render('addrec'+this.id);
            }
        }
        this.tools.quickwizardlink.replaceClass('x-tool-quickwizardlink1','x-tool-quickwizardlink');
        Wtf.get('updatetable'+this.id).dom.style.display = 'none';
        Wtf.get('chartview'+this.id).dom.style.display = 'none';
        if(this.tools.quickwizardlink)
            Wtf.get('addrec'+this.id).dom.style.display = 'block';
        switch(code) { //code for resizing
            case 6: Wtf.get(this.id+'actflag').dom.parentNode.style.width="";
                Wtf.get(this.id+'relatedTo').dom.parentNode.style.width="";
                Wtf.get(this.id+'relatedToName').dom.parentNode.style.width="";
                Wtf.get('startdate'+this.id).dom.parentNode.style.width="";
                Wtf.get('enddate'+this.id).dom.parentNode.style.width="";
                Wtf.get('tilldate'+this.id).dom.parentNode.style.width="";
                Wtf.get('starttime'+this.id).dom.parentNode.style.width="";
                Wtf.get('endtime'+this.id).dom.parentNode.style.width="";
                Wtf.get(this.id+"scheduleType").dom.parentNode.style.width="";
                Wtf.get('subject'+this.id).dom.parentNode.style.width="";
                break;
        }
        Wtf.getCmp('navigationpanel').on('expand',function(){
            if(Wtf.getCmp('campName'+this.id) != undefined) {
                Wtf.getCmp('campName'+this.id).clearInvalid();
                Wtf.getCmp('campaignstartdate'+this.id).clearInvalid();
                Wtf.getCmp('campaignenddate'+this.id).clearInvalid();
            }
            if(Wtf.getCmp('fname'+this.id) != undefined) {
                Wtf.getCmp('fname'+this.id).clearInvalid();
                Wtf.getCmp('lname'+this.id).clearInvalid();
                Wtf.getCmp('company'+this.id).clearInvalid();
            }
            if(Wtf.getCmp('aname'+this.id) != undefined)
                Wtf.getCmp('aname'+this.id).clearInvalid();
            if(Wtf.getCmp('confname'+this.id) != undefined) {
                Wtf.getCmp('confname'+this.id).clearInvalid();
                Wtf.getCmp('conlname'+this.id).clearInvalid();
            }
            if(Wtf.getCmp('csubject'+this.id) != undefined) {
                Wtf.getCmp('csubject'+this.id).clearInvalid();
                Wtf.getCmp(this.id + 'casepri').clearInvalid();
                Wtf.getCmp(this.id + 'casestatus').clearInvalid();
            }
            if(Wtf.getCmp('oname'+this.id) != undefined) {
                Wtf.getCmp('oname'+this.id).clearInvalid();
                Wtf.getCmp(this.id + 'stage').clearInvalid();
                Wtf.getCmp(this.id+'acccombo').clearInvalid();
            }
            if(Wtf.getCmp(this.id+'actflag') != undefined) {
                Wtf.getCmp(this.id+'actflag').clearInvalid();
                Wtf.getCmp(this.id+'relatedTo').clearInvalid();
                Wtf.getCmp(this.id+'relatedToName').clearInvalid();
                Wtf.getCmp('startdate'+this.id).clearInvalid();
                Wtf.getCmp('enddate'+this.id).clearInvalid();
            }
            if(Wtf.getCmp('pname'+this.id) != undefined) {
                Wtf.getCmp('pname'+this.id).clearInvalid();
            }
        },this);
        Wtf.getCmp('navigationpanel').on('collapse',function(){
            if(Wtf.getCmp('campName'+this.id) != undefined) {
                Wtf.getCmp('campName'+this.id).clearInvalid();
                Wtf.getCmp('campaignstartdate'+this.id).clearInvalid();
                Wtf.getCmp('campaignenddate'+this.id).clearInvalid();
            }
            if(Wtf.getCmp('fname'+this.id) != undefined) {
                Wtf.getCmp('fname'+this.id).clearInvalid();
                Wtf.getCmp('lname'+this.id).clearInvalid();
                Wtf.getCmp('company'+this.id).clearInvalid();
            }
            if(Wtf.getCmp('aname'+this.id) != undefined)
                Wtf.getCmp('aname'+this.id).clearInvalid();
            if(Wtf.getCmp('confname'+this.id) != undefined) {
                Wtf.getCmp('confname'+this.id).clearInvalid();
                Wtf.getCmp('conlname'+this.id).clearInvalid();
            }
            if(Wtf.getCmp('csubject'+this.id) != undefined) {
                Wtf.getCmp('csubject'+this.id).clearInvalid();
                Wtf.getCmp(this.id + 'casepri').clearInvalid();
                Wtf.getCmp(this.id + 'casestatus').clearInvalid();
            }
            if(Wtf.getCmp('oname'+this.id) != undefined) {
                Wtf.getCmp('oname'+this.id).clearInvalid();
                Wtf.getCmp(this.id + 'stage').clearInvalid();
                Wtf.getCmp(this.id+'acccombo').clearInvalid();
            }
            if(Wtf.getCmp(this.id+'actflag') != undefined) {
                Wtf.getCmp(this.id+'actflag').clearInvalid();
                Wtf.getCmp(this.id+'relatedTo').clearInvalid();
                Wtf.getCmp(this.id+'relatedToName').clearInvalid();
                Wtf.getCmp('startdate'+this.id).clearInvalid();
                Wtf.getCmp('enddate'+this.id).clearInvalid();
                Wtf.get('tilldat'+this.id).clearInvalid();
                Wtf.get('starttime'+this.id).clearInvalid();
                Wtf.get('endtime'+this.id).clearInvalid();
                Wtf.get(this.id+"scheduleType").clearInvalid();
                Wtf.get('subject'+this.id).clearInvalid();
            }
            if(Wtf.getCmp('pname'+this.id) != undefined) {
                Wtf.getCmp('pname'+this.id).clearInvalid();
            }
        },this);
//        Wtf.get('barchartview'+this.id).dom.style.display = 'none';
    },

     showChart : function(linkedobj) {
        var html = document.getElementById('chartview'+this.id).innerHTML;
        var re = /<script\b[\s\S]*?>([\s\S]*?)<\//ig;
        var match;
        if(match = re.exec(html)) {
            eval(match[1]);
        }
        this.disableLinks();
        this.tools.paichartwizard.replaceClass('x-tool-paichartwizard1','x-tool-paichartwizard');
        Wtf.get('updatetable'+this.id).dom.style.display = 'none';
        Wtf.get('chartview'+this.id).dom.style.display = 'block';
        if(this.tools.quickwizardlink)
            Wtf.get('addrec'+this.id).dom.style.display = 'none';
       // Wtf.get('barchartview'+this.id).dom.style.display = 'none';
    },

//    TODO
//    showBarChart : function(linkedobj) {
//        var html = document.getElementById('barchartview'+this.id).innerHTML;
//        var re = /<script\b[\s\S]*?>([\s\S]*?)<\//ig;
//        var match;
//        if(match = re.exec(html)) {
//            eval(match[1]);
//        }
//        this.disableLinks();
//        this.tools.paichartwizard.replaceClass('x-tool-paichartwizard1','x-tool-paichartwizard');
//        Wtf.get('updatetable'+this.id).dom.style.display = 'none';
//        Wtf.get('chartview'+this.id).dom.style.display = 'none';
//        Wtf.get('barchartview'+this.id).dom.style.display = 'block';
//        Wtf.get('addrec'+this.id).dom.style.display = 'none';
//    },

    disableLinks : function() {
        if(this.tools.updatewizardlink)
            this.tools.updatewizardlink.replaceClass('x-tool-updatewizardlink','x-tool-updatewizardlink1');
        if(this.tools.quickwizardlink)
            this.tools.quickwizardlink.replaceClass('x-tool-quickwizardlink','x-tool-quickwizardlink1');
        if(this.tools.paichartwizard)
            this.tools.paichartwizard.replaceClass('x-tool-paichartwizard','x-tool-paichartwizard1');
    },

    saveForm : function() {
        switch(this.code) {
            case 0 :this.campPanel.saveobj(); break;
            case 1 :this.leadPanel.saveobj(); break;
            case 2 :this.accountPanel.saveobj();break;
            case 3 :this.contactPanel.saveobj();break;
            case 4 :this.OpportunityPanel.saveobj();break;
            case 5 :this.CasesPanel.saveobj();break;
            case 6 :this.saveActivity();break;
            case 7 :this.ProductPanel.saveobj();break;
        }
    },

     saveActivity:function(){
//        var subObj=Wtf.getCmp('subject'+this.id);
//        var subject = subObj.getValue();
//        if(subject.trim()==""){
//            subObj.setValue("");
//            subObj.allowBlank=false;
//            ResponseAlert(155);
//            return;
//        }
//        var flag=Wtf.getCmp(this.id+'actflag').getValue();
//        this.saveflag=true;
//        var relatedto=Wtf.getCmp(this.id+"relatedTo").getValue();
//        var relatedname=Wtf.getCmp(this.id+'relatedToName').getValue();
//        if(isEmpty(subject)){
//            Wtf.getCmp('subject'+this.id).markInvalid();
//        }
//        if(isEmpty(relatedto)){
//            Wtf.getCmp(this.id+"relatedTo").markInvalid();
//        }
//        if(isEmpty(relatedname)){
//            Wtf.getCmp(this.id+'relatedToName').markInvalid();
//        }
//        var finalStr = createQuickinsertActivityJSON(this,relatedto,relatedname,flag,subject);
//        if(flag.trim()=="" || relatedto.trim()=="" || relatedname.trim()=="" || subject.trim()=="" ){
//            WtfComMsgBox(21,0);
//            return;
//        }
        var finalStr = validateActivityFields(this,false);
        if(finalStr!=undefined && finalStr.trim().length>0) {
            if(this.saveflag){
                this.saveRecordReq(finalStr,{flag:82},6,Wtf.req.springBase+'Activity/action/saveActivity.do');
            }
        }
//            Wtf.commonWaitMsgBox("Saving data...");
//            Wtf.Ajax.requestEx({
//                url:Wtf.req.base + 'crm.jsp',
//                params:{
//                    jsondata:finalStr,
//                    type:1,
//                    flag:82
//                }
//            },
//            this,
//            function(res) {
//                WtfComMsgBox(201,3);
//            },
//            function(res) {
//                WtfComMsgBox(202,1);
//            }
//            )
            Wtf.getCmp(this.id+'actflag').setValue('');
            Wtf.getCmp(this.id+'relatedTo').setValue('');
            Wtf.getCmp(this.id+'relatedToName').setValue('');
            Wtf.getCmp('startdate'+this.id).setValue(new Date());
            Wtf.getCmp('enddate'+this.id).setValue(new Date());
            Wtf.getCmp('subject'+this.id).setValue('');
            Wtf.getCmp(this.id+'actflag').clearInvalid();
            Wtf.getCmp(this.id+'relatedTo').clearInvalid();
            Wtf.getCmp(this.id+'relatedToName').clearInvalid();
            Wtf.getCmp('startdate'+this.id).clearInvalid();
            Wtf.getCmp('enddate'+this.id).clearInvalid();

    },

        saveRecordReq : function (jsondata, paramObj, actionCode, url) {
        Wtf.commonWaitMsgBox(WtfGlobal.getLocaleText("crm.savingdata.loadmsg"));
        paramObj['jsondata'] = jsondata;
        paramObj['type'] = 1;
        paramObj['reminder'] = this.emailReminder.getValue();
        Wtf.Ajax.requestEx({
//            url:Wtf.req.base +'crm.jsp',
            url:url,
            params:paramObj
        },this,
        function(res) {
            Wtf.updateProgress();
            var obj = Wtf.getCmp(Wtf.moduleWidget.topactivity);
            if(obj!=null) {
                obj.callRequest("","",0);
                Wtf.refreshUpdatesAll();
            }
           obj = Wtf.getCmp(Wtf.moduleWidget.activity);
           if(obj!=null){
                obj.callRequest("","",0);
                Wtf.refreshUpdatesAll();
           }
            switch(actionCode) {
                case 0:
                    ResponseAlert(11);
                    break;
                case 1:
                    ResponseAlert(9);
                    break;
                case 2:
                    ResponseAlert(7);
                    break;
                case 3:
                    ResponseAlert(5);
                    break;
                case 4:
                    ResponseAlert(4);
                    break;
                case 5:
                    ResponseAlert(6);
                    break;
                case 6:
                    ResponseAlert(8);
                    break;
                case 7:
                    ResponseAlert(10);
                    break;
            }
        },
        function(res){
            switch(actionCode) {
                case 0:
                    WtfComMsgBox(152,1);
                    break;
                case 1:
                    WtfComMsgBox(14,1);
                    break;
                case 2:
                    WtfComMsgBox(52,1);
                    break;
                case 3:
                    WtfComMsgBox(302,1);
                    break;
                case 4:
                    WtfComMsgBox(102,1)
                    break;
                case 5:
                    WtfComMsgBox(252,1);
                    break;
                case 6:
                    WtfComMsgBox(202,1);
                    break;
                case 7:
                    WtfComMsgBox(352,1);
                    break;
            }
        })
    }
});

function showResults(widgetid) {

    var cmbstr="";
        var widget = Wtf.getCmp(widgetid);
        widget.cmb3 = document.getElementById('cmb3').selectedIndex;
        if((document.getElementById('cmb1')) && (document.getElementById('cmb1').value !='')) {
           cmbstr=cmbstr+document.getElementById('cmb1').value+",";
        }
        if((document.getElementById('cmb2')) && (document.getElementById('cmb2').value !='')) {
            cmbstr=cmbstr+document.getElementById('cmb2').value+",";
        }
        if((document.getElementById('cmb3')) && (document.getElementById('cmb3').value !='')) {
            cmbstr=cmbstr+document.getElementById('cmb3').value+",";
        }

    Wtf.getCmp('DSBKnowledgeCampus').doSearch("jspfiles/knowledgeUni/workspace.jsp",cmbstr);
}

function filterBrands(val) {

             if( (val==1) &&  (document.getElementById('cmb1')) ) {

                    Wtf.Ajax.requestEx({
                            url:'jspfiles/knowledgeUni/CenterManagement.jsp',
                            params:{
                                flag:119,
                                segmentname:document.getElementById('cmb1').value
                            }
                        },
                        this,
                        function(result,resp){
                            if(result.success){
                                var cmb2Select = document.getElementById('cmb2');
                                cmb2Select.innerHTML = "";
                                var optSelectEl = getOptionElement("Select Value --" , "");
                                Wtf.isIE ? cmb2Select.add(optSelectEl) : cmb2Select.appendChild(optSelectEl);

                                optSelectEl =getOptionElement("" , "");
                                Wtf.isIE ? cmb2Select.add(optSelectEl) : cmb2Select.appendChild(optSelectEl);

                                for(var i=0;i<result.data.length;i++){
                                    var text = result.data[i].name;
                                    var opt = getOptionElement(text , text);
                                    Wtf.isIE ? cmb2Select.add(opt) : cmb2Select.appendChild(opt);
                                }
                            }else{
                                msgBoxShow(6,1);
                            }

                        }, function(){
                                msgBoxShow(6,1);
                    });

             }

}

function getOptionElement(text,val){
    var opt = document.createElement("option");
    opt.value = val;
    opt.text = text;
    return opt;
}

