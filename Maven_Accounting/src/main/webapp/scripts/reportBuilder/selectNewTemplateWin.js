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
 * Author : Malhari Pawar
*/
Wtf.selectNewTempWin=function(config){
    Wtf.apply(this,config);
    back =this;
    var templateRec = Wtf.data.Record.create([
        {
            name: 'tempid',
            mapping:'tempid'
        },{
            name: 'tempname',
            mapping:'tempname'
        },{
            name: 'description',
            mapping:'description'
        },{
            name: 'configstr',
            mapping:'configstr'
        }
        ,{
            name: 'letterhead',
            mapping:'letterhead'
        },{
            name: 'pretext',
            mapping:'pretext'
        },{
            name: 'posttext',
            mapping:'posttext'
        },{
            name: 'fieldConfig',
            mapping:'fieldConfig'
        }
    ]);

    var template_ds = new Wtf.data.Store({
        url : "ExportPDF/getAllReportTemplate.do",
        method: 'GET',
        baseParams : {
            templatetype : this.templatetype
        },
        reader: new Wtf.data.KwlJsonReader({
            root: 'data'
        },templateRec)
    });

    var namePanel = new Wtf.grid.GridPanel({
        id:'templateName',
        autoScroll: true,
        enableColumnResize:false,
        border:false,
        viewConfig:{
            forceFit:true
        },
        cm: new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer, {
                header:WtfGlobal.getLocaleText("acc.customerList.gridName"),//'Name',
                dataIndex: 'tempname',
                renderer: function(val){
                    return "<div wtf:qtip=\""+val+"\"wtf:qtitle='"+WtfGlobal.getLocaleText("acc.campaigndetails.campaigntemplate.templatename")+"'>"+val+"</div>";
                }
            }]),
        ds: template_ds,
        height:180
    });
this.fieldConfig=""
    namePanel.on('cellclick',function(gridObj, ri, ci, e){
        var config = gridObj.getStore().getAt(ri).data['configstr'];
        this.fieldConfig = gridObj.getStore().getAt(ri).data['fieldConfig'].data;
        this.templateid = gridObj.getStore().getAt(ri).data['tempid'];

        var configstr = eval('('+config+')');
        var title = configstr["title"];
        var subtitle =configstr["subtitles"];
        var starr = subtitle.split("~");
        var subtitles = "";
        for(var i=0;i< starr.length;i++)
            subtitles += "<div>"+starr[i]+"</div>";

        var textColor = "#"+configstr["textColor"];
        var bgColor ="#"+configstr["bgColor"];

        var headdate = configstr["headDate"]=="true"?"<small>2009/01/01</small>":"";
        var footdate = configstr["footDate"]=="true"?"<small>2009/01/01</small>":"";

        var headnote = configstr["headNote"];
        var footnote = configstr["footNote"];

        var headpager = configstr["headPager"]=="true"?"1":"";
        var footpager = configstr["footPager"]=="true"?"1":"";

        var pageborder = configstr["pageBorder"]=="true"?"border:thin solid #666;":"";
        var gridborder = configstr["gridBorder"]=="true"?"1":"0";
        var displaylogo = configstr["showLogo"]=="true"?"block":"none";

        var pagelayoutPR = "height:380px;width:270px;margin:auto;";
        var pagelayoutLS = "height:270px;width:380px;margin:57px auto;";
        var pagelayout = configstr["landscape"]=="true"?pagelayoutLS:pagelayoutPR;

        this.layout = configstr["landscape"];
        var reportPreview = "<div style=\""+pagelayout+"align:center;color:"+textColor+";font-family:arial;padding:5px;font-size:12px;background:"+bgColor+";border-right:4px solid #DDD;border-bottom:4px solid #888\">" +
        "<div style=\""+pageborder+"height:99%;width:99%;\">" +
        "<div style=\"border-bottom:thin solid #666;margin:0 2px;height:6%;width:98%;\">" +
        "<table border=0 width=100% style=\"font-size:12px\">" +
        "<tr><td align=\"left\" width=25%>"+headdate+"</td><td align=\"center\" >"+headnote+"</td><td align=\"right\" width=25%>"+headpager+"</td></tr>" +
        "</table>" +
        "</div>" +
        "<div style=\"margin:0 2px;height:86%;width:98%;text-align:center;overflow:hidden;\">" +
        "<div style=\"border-bottom:thin solid #666;\">" +
        "<div style=\"display:"+displaylogo+";position:absolute;font-size:16px;margin:1px 0 0 1px\"><b>Deskera</b></div>" +
        "<div style=\"display:"+displaylogo+";position:absolute;color:#8080FF;font-size:16px\"><b>Deskera</b><sup><small><small><small>TM</small></small></small></sup></div>" +
        "<br/><div style=\"font-size:13px\"><b>"+title+"</b></div>" +
        subtitles + "<br/>"+
        "</div>";
    if(this.templatetype==0) {
        reportPreview += "<table border="+gridborder+" width=90% cellspacing=0 style=\"font-size:12px;margin:5px auto;\">" +
        "<tr><td align=\"center\" width=10%><b>No.</b></td><td align=\"center\" width=20%><b>Index</b></td><td align=\"center\" width=45%><b>Task Name</b></td><td align=\"right\" width=25%><b>Resources</b></td></tr>" +
        "<tr><td align=\"center\">1.</td><td align=\"center\">31</td><td align=\"center\">Gather info.</td><td align=\"right\" >Thomas</td></tr>" +
        "<tr><td align=\"center\">2.</td><td align=\"center\">56</td><td align=\"center\">Documentation</td><td align=\"right\" >Jane,Alice</td></tr>" +
        "<tr><td align=\"center\">3.</td><td align=\"center\">78</td><td align=\"center\">Planning</td><td align=\"right\" >Darin</td></tr>" +
        "<tr><td align=\"center\">4.</td><td align=\"center\">90</td><td align=\"center\">Coding</td><td align=\"right\" >John</td></tr>" +
        "<tr><td align=\"center\">5.</td><td align=\"center\">111</td><td align=\"center\">Implemention</td><td align=\"right\">John</td></tr>" +
        "<tr><td align=\"center\">6.</td><td align=\"center\">112</td><td align=\"center\">Submission</td><td align=\"right\">John</td></tr>" +
        "</table>";
    } else {
        reportPreview += '<div style="font-size:8px"> <table width="90%" border="0" align="center" cellpadding="0" cellspacing="0">   <tr>  \n\
   <td align="left" valign="top"><table width="100%" border="0" cellspacing="0" cellpadding="0">         <tr>           \n\
<td width="50%" align="left" valign="top"><table width="100%" border="0" cellspacing="0" cellpadding="0">               \n\
 <tr>                 <td align="left" valign="top"><strong>Company Name:</strong> Krawler Information Systems<br>    \n\
               <strong>Email Id: </strong>admin@deskera.com</td> \n\
              </tr>               <tr>                 <td align="left" valign="top"><p>To,<br>                    \n\
  Commerzone, Building No. 1,<br>                     5th Floor, Office No. 503, Off Airport Road,<br>               \n\
      Samrat Ashok Path,<br>                     Yerwada, Pune 411006<br>                     India </p></td>        \n\
       </tr>              </table></td>           <td width="50%" align="left" valign="top">\n\
<table width="70%" border="0" align="right" cellpadding="0" cellspacing="0">               <tr>     \n\
            <td width="46%"><strong>Quotaion#</strong></td>                 <td width="54%">: Q00001</td>    \n\
           </tr>               <tr>                 <td><strong>DATE</strong></td>                  <td>: 2011-08-10</td>               </tr>             </table></td>         </tr>       </table></td>   </tr>   <tr>     <td align="left" valign="top" ><table width="100%" border="0" cellpadding="2" cellspacing="0">        <tr>         <td width="7%" height="20" align="center" valign="middle" bgcolor="#EFEFEF" style="border-right:1px solid #000;border-bottom:1px solid #000;border-top:1px solid #000;border-left:1px solid #000">Sr. NO.</td>         <td width="38%" align="center" valign="middle" bgcolor="#EFEFEF" style="border-right:1px solid #000;border-bottom:1px solid #000;border-top:1px solid #000">PRODUCT DESCRIPTION</td>         <td width="18%" align="center" valign="middle" bgcolor="#EFEFEF" style="border-right:1px solid #000;border-bottom:1px solid #000;border-top:1px solid #000">QUANTITY</td>         <td width="15%" align="center" valign="middle" bgcolor="#EFEFEF" style="border-right:1px solid #000;border-bottom:1px solid #000;border-top:1px solid #000">UNIT PRICE</td>         <td width="22%" align="center" valign="middle" bgcolor="#EFEFEF" style="border-bottom:1px solid #000;border-top:1px solid #000;border-right:1px solid #000">LINE TOTAL</td>        </tr>       <tr>         <td align="center" valign="middle" style="border-right:1px solid #000;border-left:1px solid #000;border-bottom:1px solid #000"> </td>         <td align="center" valign="middle" style="border-right:1px solid #000;border-bottom:1px solid #000"> </td>         <td align="center" valign="middle" style="border-right:1px solid #000;border-bottom:1px solid #000"> </td>         <td align="center" valign="middle" style="border-right:1px solid #000;border-bottom:1px solid #000"> </td>         <td align="center" valign="middle" style="border-right:1px solid #000;border-bottom:1px solid #000">&nbsp; </td><td>                 </td></tr>       <tr>         <td align="center" valign="middle"> </td>         <td align="center" valign="middle"> </td>         <td align="center" valign="middle"> </td>         <td align="right" valign="middle" style="padding-right:5px;border-right:1px solid #000">TOTAL</td>          <td height="25" align="center" valign="middle" style="border-right:1px solid #000;border-bottom:1px solid #000">0000.0</td>       </tr>     </table></td>   </tr>   <tr>     <td height="20" align="left" valign="top"> </td>   </tr>   \n\
<tr>      <td align="left" valign="top"><table width="100%" border="0" cellspacing="0" cellpadding="0" >       <tr>         <td height="30" align="left" valign="middle">Memo: Thank you for your Enquiry. We look forward to work with you</td>       </tr>     \n\
</table></td>   </tr> </table> </div>';
    }
    reportPreview +="</div>" +
        "<div style=\"border-top:thin solid #666;margin:0 2px;height:6%;width:98%;\">" +
        "<table border=0 width=100% style=\"font-size:12px\">" +
        "<tr><td align=\"left\" width=25%>"+footdate+"</td><td align=\"center\" >"+footnote+"</td><td align=\"right\" width=25%>"+footpager+"</td></tr>" +
        "</table>" +
        "</div>" +
        "</div>" +
        "</div>";


        var reportTmp = new Wtf.Template(reportPreview);
        reportTmp.overwrite(Wtf.getCmp("layoutpreview").body);
        back.smTmp = namePanel.getSelectionModel();
        back.configstr=back.smTmp.getSelected().data['configstr'];

    },this);

    template_ds.load(); 
    var emptypreview="<div style='font-size:14px;margin-top:175px;text-align:center;'>"+WtfGlobal.getLocaleText("acc.rem.140")+"</div>";

    template_ds.on("load",function(){

        Wtf.TemplatePreview(template_ds);
            
    },this);
    var templatePanel = new Wtf.Panel({
        id:'templatePanel',
        layout:'border',
        border:false,
        width:500,
        items:[{
            region:'center',
            width:'50%',
            border:false,
            layout:'fit',
            height:'100%',
            items:[namePanel]
        },{
            region:'east',
            width:410,
            border:false,
            layout: 'fit',
            height:'100%',
            bodyStyle:"background:#EEEEEE",
            items:[{
                layout:'fit',
                xtype:'fieldset',
                cls: 'textAreaDiv',
                preventScrollbars:false,
                frame:true,
                border:false,
                id:'layoutpreview',
                html:emptypreview
            }]
        }]
    });

    var configstr="";

    this.templateWindow = new Wtf.Window({
        title:WtfGlobal.getLocaleText({key:"acc.rem.133",params:[this.isreport?'Report':'']}),//'Existing '+(this.isreport?'Report':'')+ ' Templates',
        modal:true,
        iconCls: "pwnd favwinIcon",
        layout:'fit',
        items:[templatePanel],
        resizable:true,
        autoDestroy:true,
        height:600,
        width:600,
        buttons:[{
            text:WtfGlobal.getLocaleText("acc.rem.134"),//'Select Columns',
            tooltip:{text:WtfGlobal.getLocaleText("acc.exportinterface.template.selcolumns.ttip")},//'Choose columns to be exported.'},
            scope:this,
            hidden : !this.isreport,
            handler:function() {
                var smTmpcheck = namePanel.getSelectionModel();
                if(smTmpcheck.getCount()<1){
                    WtfComMsgBox(792,0);
                    return;
                } else {
                    this.templateWindow.hide();
                    this.CallToExportInterface();
                }
            }
        },{

                 text:WtfGlobal.getLocaleText("acc.common.export"),  //'Export',
            hidden : !this.isreport,
            tooltip:{text:WtfGlobal.getLocaleText("acc.exportinterface.template.exportbtnttip")},//'Export as pdf file using selected report template.'},
            handler:function() {
                var smTmp = namePanel.getSelectionModel();
                if(this.type == "pdf" && smTmp.getSelections().length > 0) {
                    var sum = 0;
                    var selLength = this.storeToload.getCount();
                    var pdfWidth=800; //landscape
                    if(this.pageLayout!="true")
                        pdfWidth=570; //potrait
                    var max = Math.floor(pdfWidth/selLength);
                    max=Math.floor(max/10)*10;
                    for(var i = 0; i < selLength; i++)
                        sum += this.storeToload.data.items[i].data.width;
                    if(sum > pdfWidth){
                        this.CallToExportInterface();
                        this.templateWindow.hide();
                    } else {
                        if(smTmp.getCount()<1){
                            WtfComMsgBox(792,0);
                            return;
                        } else {
                            configstr=smTmp.getSelected().data['configstr'];
                            var url = this.url+"?config="+this.configstr+"&reportid="+this.name+"&name="+this.name+"&filetype="
                                        + this.type+"&gridconfig="+encodeURIComponent(this.gridConfig)+"&mapid="+this.mapid+"&year="+this.year+"&flag="+this.flag+"&listID="+this.TLID;
                            if(this.selectExport != undefined) {
                                url += "&selectExport="+this.selectExport;
                            }
                            if(this.extraConfig != undefined) {
                                url += "&extraconfig="+encodeURIComponent(this.extraConfig);
                            }
                            if(this.comboName != undefined && this.comboValue != undefined) {
                                url += "&comboName="+this.comboName+"&filterCombo="+this.comboValue+"&comboDisplayValue="+this.comboDisplayValue;
                            }
                            if(this.userCombo!="") {
                                url += "&userCombo="+this.userCombo;
                            }
                            if(this.field != undefined) {
                                url += "&field="+this.field+"&direction="+this.dir;
                            }
                            if(this.ss!=undefined && this.ss!="") {
                                url += "&ss="+this.ss;
                            }
                            if(this.json=="" && (this.fromdate=="" || this.todate =="")) {
                                url += "&isarchive=false&isconverted=0&transfered=0";
                            } else if(this.json!="") {
                                url += "&searchJson="+encodeURIComponent(this.json)+"&isarchive=false&isconverted=0&transfered=0";
                            } else if(this.fromdate!="" && this.todate !="") {
                                url += "&frm=" +this.fromdate+"&to="+this.todate+"&cd="+this.cd+"";
                            }
                            if(this.goalid!="") {
                                url += "&goalid="+this.goalid;
                            }
                            if(this.filterConjuctionCriteria!="") {
                                url += "&filterConjuctionCriteria="+this.filterConjuctionCriteria;
                            }
                            if(this.heading!="" && this.heading!=undefined) {
                                url += "&heading="+this.heading;
                            }
                            if(this.emailmarketid!="") {
                                url += "&emailmarketid="+this.emailmarketid;
                            }
                            if(this.bouncereportcombo != undefined) {
                                url += "&bouncereportcombo="+this.bouncereportcombo;
                            }
                            Wtf.get('downloadframe').dom.src = url;
                            this.templateWindow.close();
                        }
                        this.templateWindow.close();
                    }
                } else {
                    WtfComMsgBox(792,0);
                    return;
                }
                
            },
            scope: this
        },{
            text:WtfGlobal.getLocaleText("acc.common.edit"),//'Edit',
            tooltip:{text:WtfGlobal.getLocaleText("acc.exportinterface.template.editbtn.ttip")},//'Edit selected report template.'},
            handler: function() {
                var smTmp = namePanel.getSelectionModel();
                if(smTmp.getCount()<1){
                    WtfComMsgBox(792,0);
                    return;
                } else
                    this.newEditTemplate(smTmp.getSelected().data['configstr'],smTmp.getSelected().data['letterhead'],smTmp.getSelected().data['pretext'],smTmp.getSelected().data['posttext'],  smTmp.grid.store);
            },
            scope:this
        },{
             text:WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"),  //'Delete',
            tooltip:{text: WtfGlobal.getLocaleText("acc.exportinterface.template.delbtn.ttip")},//'Delete selected report template.'},
            handler:function(){
                var smTmp = namePanel.getSelectionModel();
                if(smTmp.getCount()<1){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.rem.135")], 2);
                    return;
                } else {
                     Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.rem.136"),function(btn) {
                        if(btn =='yes') {
                            Wtf.Ajax.requestEx({
                                  url : "ExportPDF/deleteReportTemplate.do",
                                params: {
                                    action: 2,
                                    deleteflag:this.templateid,
                                    userid:loginid
                                },
                                method:'POST'
                            },
                            this,
                            function(res) {
                                if(res.success){
                                    ResponseAlert(0);
                                    smTmp.grid.store.load();

                                    Wtf.TemplatePreview(smTmp.grid.store);
                                    if(this.templatetype>0) {
                                        this.tabObj.loadTemplateStore();
                                        this.tabObj.template.setValue(WtfGlobal.getLocaleText("acc.rem.135"));//"Please select a Template...");
                                    }
                                }
                            },
                            function() {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Could not delete template. Please try again."],1);
                            });
                        }
                    },this); 
                }
            }, 
            scope:this
        }, {
            text: WtfGlobal.getLocaleText("acc.rem.138"), //'Create New',
            tooltip: {text: WtfGlobal.getLocaleText("acc.rem.138.ttip")}, //'Create new report template before exporting.'},
            handler: function() {

                Wtf.Ajax.requestEx({
                    url: "ExportPDF/getTemplateConfig.do",
                    params: {
                        userid: loginid,
                        templatetype: this.templatetype
                    },
                    method: 'POST'
                },
                this,
                        function(res) {
                            if (res.success) {
                                this.fieldConfig = res.data;
                                this.newEditTemplate(undefined, undefined, undefined, undefined, namePanel.getSelectionModel().grid.store);
                            }
                        },
                        function() {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.CouldnotcreatetemplatePleasetryagain")], 1);
                        });




            },
            scope: this
        },
        {
            text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
            handler:function() {
                this.templateWindow.close();
            },
            scope: this
        }]
    });
    this.templateWindow.show();

    Wtf.selectNewTempWin.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.selectNewTempWin,Wtf.Window,{
    onRender: function(conf){
        Wtf.selectNewTempWin.superclass.onRender.call(this, conf);
        this.add(this.templateWindow);
    },
    newEditTemplate: function(configstr,letterhead,pretext,posttext, templateStore) {
        var custForm=new Wtf.newCustomReport({
            templatetype : this.templatetype,
            id:'custForm'+this.id + this.tabtitle,
            reportGrid:this.grid,
            reportid:this.name,
            name:this.name,
            parentWindow : this.templateWindow,
            templateStore:templateStore,
            dashboardCall:this.dashboardCall,
            filetype:this.filetype,
            gridconfig:this.gridConfig,
            searchJson:this.json,
            filterConjuctionCriteria:this.filterConjuctionCriteria,
            frm:this.fromdate,
            to:this.todate,
            cd:this.cd,
            year:this.year,
            reportFieldConfig:this.fieldConfig,
            reportType:1,
            url:this.url,
			selectExport:this.selectExport,
            reportFlag:this.reportFlag,
            tabObj: this.tabObj,
            field:this.field,
            dir:this.dir,
            mapid:this.mapid,
            flag:this.flag,
			comboName:this.comboName,
            comboValue:this.comboValue,
            comboDisplayValue:this.comboDisplayValue,
            editTemplateConfig:configstr,
            overwriteflag:this.templateid,
            letterhead:letterhead,
            pretext:pretext,
            posttext:posttext});
        var eobj = Wtf.getCmp(this.id + "_buildReport"+ this.tabtitle);
        if(eobj === undefined){
            eobj = new Wtf.reportBuilder.builderPanel({
                title: "<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.rem.139")+"'>"+Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.rem.139"),18)+"</span>",
                iconCls:"accountingbase template_builder",
                id: this.id + "_buildReport" + this.tabtitle,
                closable: true,
                autoScroll: true,
                formCont: custForm
            });
            mainPanel.add(eobj);
        }
        if(this.dashboardCall){
            this.templateWindow.hide();
        }else {
            this.templateWindow.close();
        }
        
        mainPanel.setActiveTab(eobj);
        mainPanel.doLayout();
    },
    CallToExportInterface: function () {
        var expt =new Wtf.ExportInterface({
            type:"pdf",
            parent:this.templateWindow,
            name:this.name,
            cd:1,
            mapid:this.mapid,
            ss : this.ss,
            json:this.json,
            filterConjuctionCriteria:this.filterConjuctionCriteria,
            fromdate:this.fromdate,
            todate:this.todate,
            year:this.year,
            url:this.url==undefined?"../../exportmpx.jsp":this.url,
            selectExport:this.selectExport,
            field:this.field,
            dir:this.dir,
            pdfDs:this.storeToload,
            configstr:this.configstr,
            comboName:this.comboName,
            comboValue:this.comboValue,
            comboDisplayValue:this.comboDisplayValue,
            pageLayout:this.layout?true:false,
            flag:this.flag
        });
        expt.show();

    }
});

Wtf.TemplatePreview=function(store){
    
        var emptypreview="<div style='font-size:14px;margin-top:175px;text-align:center;'>"+WtfGlobal.getLocaleText("acc.rem.140")+"</div>";//"There is no template created till now.</div>";
        if(store.getTotalCount()>0){
            emptypreview="<div style='font-size:14px;margin-top:175px;text-align:center;'>"+WtfGlobal.getLocaleText("acc.exportinterface.template.seltemplate")+"</div>";//"Select a Template to preview.</div>";
        }
        var reportTmp = new Wtf.Template(emptypreview);
        reportTmp.overwrite(Wtf.getCmp("layoutpreview").body);
   
}
