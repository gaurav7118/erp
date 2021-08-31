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
Wtf.selectTempWin=function(config){
    Wtf.apply(this,config);
    this.deleted = (config.deleted==undefined)?false:config.deleted;
    this.nondeleted = (config.nondeleted==undefined)?false:config.nondeleted;
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
        ]);

    var template_ds = new Wtf.data.Store({
//        url: Wtf.req.base + 'template.jsp?&action=1',
        url : "ExportPDF/getAllReportTemplate.do",
//        method: 'GET',
        method: 'POST',
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
                header:WtfGlobal.getLocaleText("acc.customerList.gridName"),  //'Name',
                dataIndex: 'tempname'
            }]),
        ds: template_ds,
        height:180
    });
    
    namePanel.on('cellclick',function(gridObj, ri, ci, e){
        var config = gridObj.getStore().getAt(ri).data['configstr'];
        this.templateid = gridObj.getStore().getAt(ri).data['tempid'];
        var configstr = eval('('+config+')');
        var title = configstr["title"];
        var subtitle =configstr["subtitles"];
        var starr = subtitle.split("~");
        var subtitles = "";
        for(var i=0;i< starr.length;i++)
            subtitles += "<div>"+starr[i]+"</div>";
        var dateRange = configstr["dateRange"]=="true"?"<small>From:2009/01/01 To:2009/02/01</small>":"";

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
        dateRange
        "</div>" +
        "<table border="+gridborder+" width=90% cellspacing=0 style=\"font-size:12px;margin:5px auto;\">" +
        "<tr><td align=\"center\" width=10%><b>No.</b></td><td align=\"center\" width=20%><b>Index</b></td><td align=\"center\" width=45%><b>Task Name</b></td><td align=\"right\" width=25%><b>Resources</b></td></tr>" +
        "<tr><td align=\"center\">1.</td><td align=\"center\">31</td><td align=\"center\">Gather info.</td><td align=\"right\" >Thomas</td></tr>" +
        "<tr><td align=\"center\">2.</td><td align=\"center\">56</td><td align=\"center\">Documentation</td><td align=\"right\" >Jane,Alice</td></tr>" +
        "<tr><td align=\"center\">3.</td><td align=\"center\">78</td><td align=\"center\">Planning</td><td align=\"right\" >Darin</td></tr>" +
        "<tr><td align=\"center\">4.</td><td align=\"center\">90</td><td align=\"center\">Coding</td><td align=\"right\" >John</td></tr>" +
        "<tr><td align=\"center\">5.</td><td align=\"center\">111</td><td align=\"center\">Implemention</td><td align=\"right\">John</td></tr>" +
        "<tr><td align=\"center\">6.</td><td align=\"center\">112</td><td align=\"center\">Submission</td><td align=\"right\">John</td></tr>" +
        "</table>" +
        "</div>" +
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
    namePanel.on('rowdblclick',this.exportTemplate.createDelegate(this,[namePanel],true),this);
    template_ds.load();
    
    /* ERP-34368
              * The checkbox Do not include Account Code with Account Name is not use 
              * Removing The code related to checkbox
              */
//    this.accountCodeCheckBox= new Wtf.form.Checkbox({
//        name:'accountCodeCheckBox',
//        boxLabel:WtfGlobal.getLocaleText("acc.field.DonotincludeAccountCodewithAccountName"),  
//        checked:false,                        
//        style: 'padding:0px 0px 10px 0px;',
//        width: 10
//    });
        
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
        },
        /*
         * ERP-34368 - Account code with account name functionality removed hence commenting this code
         */
//        {
//            region:'south',
//            xtype:'fieldset', 
//            border:false,
//            hidden : ((config.get!=112) || (config.get==112 && config.filename=="Fixed%20Assets.")), //show this region only for chart of accounts pdf export
//            layout:'fit',
//            height:40
////            items:[this.accountCodeCheckBox] //ERP-34368
//        },
        {
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
                html:"<div style='font-size:14px;margin-top:175px;text-align:center;'>"+WtfGlobal.getLocaleText("acc.rem.140")+"</div>"
            }]
        }]
    });

    //var configstr="";

    this.templateWindow = new Wtf.Window({
        title:WtfGlobal.getLocaleText("acc.rem.133"),  //'Existing Report Templates',
        modal:true,
        iconCls: "pwnd deskeraImage",
        layout:'fit',
        items:[templatePanel],
        resizable:true,
        autoDestroy:true,
        height:600,
        width:600,
        buttons:[{
            text:WtfGlobal.getLocaleText("acc.rem.134"),  //'Select Columns',
            scope:this,
            handler:function() {
                var smTmpcheck = namePanel.getSelectionModel();
                if(smTmpcheck.getCount()<1){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.rem.135")], 2);
                    return;
                } else {
                    this.templateWindow.hide();
                    var expt =new Wtf.ExportInterface({
                        type:this.type,
                        mode:this.mode,
                        get:this.get,
                        ss:this.ss,
                        stdate:this.stdate,
                        enddate:this.enddate,
                        accountid:this.accountid,
                        filename:Wtf.getCmp('as').getActiveTab().title,
                        parent:this.templateWindow,
                        cd:1,
                        json:this.json,
                        fromdate:this.fromdate,
                        todate:this.todate,
                        pdfDs:this.storeToload,
                        paramstring:this.paramstring,
                        configstr:back.configstr
                    });
                    expt.show();
                }
                if(Wtf.getCmp("selectexportwinpdf") && Wtf.getCmp("selectexportwinpdf")!=undefined){
                    Wtf.getCmp("selectexportwinpdf").destroy();
                }
            }
        },{
            text:WtfGlobal.getLocaleText("acc.common.export"),  //'Export',
            scope: this,
            handler:function() {
                this.exportTemplate(namePanel);
                if(Wtf.getCmp("selectexportwinpdf") && Wtf.getCmp("selectexportwinpdf")!=undefined){
                    Wtf.getCmp("selectexportwinpdf").destroy();
                }
            }
        },{
            text:WtfGlobal.getLocaleText("acc.common.edit"),  //'Edit',
            handler: function() {
                var smTmp = namePanel.getSelectionModel();
                if(smTmp.getCount()<1){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.rem.135")], 2);
                    return;
                } else {
                    this.newEditTemplate(smTmp.getSelected().data['configstr']);
                    if(Wtf.getCmp("selectexportwinpdf") && Wtf.getCmp("selectexportwinpdf")!=undefined){
                        Wtf.getCmp("selectexportwinpdf").destroy();
                    }
                }
            },
            scope:this
        },{
            text:WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"),  //'Delete',
            handler:function(){
                var smTmp = namePanel.getSelectionModel();
                if(smTmp.getCount()<1){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.rem.135")], 2);
                    return;
                } else {
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.rem.136"),function(btn) {
                        if(btn =='yes') {
                            Wtf.Ajax.requestEx({
//                                url: Wtf.req.base + 'template.jsp',
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
                              if(res.success)
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.rem.137")],0);
                            },
                            function() {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.CouldnotdeletetemplatePleasetryagain")],1);
                            });
                            this.templateWindow.close();
                            if(Wtf.getCmp("selectexportwinpdf") && Wtf.getCmp("selectexportwinpdf")!=undefined){
                                Wtf.getCmp("selectexportwinpdf").destroy();
                            }
                        }
                    },this);
                }
            },
            scope:this
        },{
             text:WtfGlobal.getLocaleText("acc.rem.138"),  //'Create New',
             handler:function() {
                var custForm=new Wtf.customReport({
                    id:'custForm'+this.id + this.tabtitle,
                    reportGrid:this.grid,
                    type:this.type,
                    mode:this.mode,
                    stdate:this.stdate,
                    enddate:this.enddate,
                    accountid:this.accountid,
                    get:this.get,
                    filename:Wtf.getCmp('as').getActiveTab().title,
                    gridconfig:this.gridConfig,
                    cd:this.cd,
                    reportType:1
                });
                var eobj = Wtf.getCmp(this.id + "_buildReport"+ this.tabtitle);
                if(eobj === undefined){
                    eobj = new Wtf.reportBuilder.builderPanel({
                        title: WtfGlobal.getLocaleText("acc.rem.139"),  //"Report Layout Builder",
                        iconCls:"accountingbase template_builder",
                        id: this.id + "_buildReport" + this.tabtitle,
                        closable: true,
                        autoScroll: true,
                        formCont: custForm
                    });
                    eobj.on("activate",function(panel){
                        panel.doLayout();
                    });
                    mainPanel.add(eobj);
                }
                this.templateWindow.close();
                mainPanel.setActiveTab(eobj);
                mainPanel.doLayout();
                if(Wtf.getCmp("selectexportwinpdf") && Wtf.getCmp("selectexportwinpdf")!=undefined){
                    Wtf.getCmp("selectexportwinpdf").destroy();
                }
            },
            scope: this
        },
        {
            text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
            handler:function() {
                this.templateWindow.close();
                if(Wtf.getCmp("selectexportwinpdf") && Wtf.getCmp("selectexportwinpdf")!=undefined){
                    Wtf.getCmp("selectexportwinpdf").destroy();
                }
            },
            scope: this
        }]
    });
    this.templateWindow.show();

    Wtf.selectTempWin.superclass.constructor.call(this,config);
},
Wtf.extend(Wtf.selectTempWin,Wtf.Window,{
    onRender: function(conf){
        Wtf.selectTempWin.superclass.onRender.call(this, conf);
        this.add(this.templateWindow);
    },
    exportTemplate:function(namePanel){
        var smTmp = namePanel.getSelectionModel();
        if(smTmp.getCount()<1){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.rem.135")], 2);
            return;
        } else {
            
            var header='';
            var title='';
            var width='';
            var align='';
            
            var gridConfigObject=eval('('+this.gridConfig+')');
            var columnsData=gridConfigObject.data;
            for(var i=0;i<columnsData.length;i++){
                var column=columnsData[i];
                header+=column.header+',';
                title+=column.title+',';
                if(column.width=='' || column.width==undefined){
                    align+=' ,';
                }else{
                    width+=column.width+',';
                }
                if(column.align=='' || column.align==undefined){
                    align+=' ,';
                }else{
                    align+=column.align+',';
                }
            }
            
            header=header.substr(0, (header.length-1));
            title=title.substr(0, (title.length-1));
            width=width.substr(0, (width.length-1));
            align=align.substr(0, (align.length-1));
            
            configstr=smTmp.getSelected().data['configstr'];
            this.extra=Wtf.urlEncode(this.extra);
            if(this.extra.length>0)
                this.extra="&"+this.extra;
            
            if(this.compStoreParams.length>0) {
                this.extra += "&"+this.compStoreParams;
            }
           
           var exportUrl = getExportUrl(this.get, this.consolidateFlag);
            var url = exportUrl+"?";
            var urlExcludingParams = "";
            var paramsWithoutURL = "";
             if (this.usePostMethod) {
                  var paramaters = this.extra+"&config="+configstr+"&filename="+this.filename+"&isExport=true&filetype="+ this.type+"&stdate="+this.stdate+"&enddate="+this.enddate+"&accountid="+this.accountid    //ERP-31551 : isExport flag added for PDF
                     +"&header=" + header + "&align=" + align+this.paramstring + "&title=" + (title) + "&width=" + width + "&get=" + this.get +"&deleted="+this.deleted+"&nondeleted="+this.nondeleted+"&companyids="+companyids;
                      paramaters = decodeURIComponent(paramaters);
             } else{
                  var paramaters = this.extra+"&config="+configstr+"&filename="+this.filename+"&isExport=true&filetype="+ this.type+"&stdate="+this.stdate+"&enddate="+this.enddate+"&accountid="+this.accountid    //ERP-31551 : isExport flag added for PDF
                     +"&header=" + header + "&align=" + align+this.paramstring + "&title=" + encodeURIComponent(title) + "&width=" + width + "&get=" + this.get +"&deleted="+this.deleted+"&nondeleted="+this.nondeleted+"&companyids="+companyids;
             }
             /* ERP-34368
              * The checkbox Do not include Account Code with Account Name is not use 
              * Removing The code related to checkbox
              */
//            if(this.get==112){   //add parameter only for chart of accounts pdf export
//                paramaters+="&accountCodeNotAdded="+this.accountCodeCheckBox.checked;
//            }
            if(this.ss!=undefined && this.ss!="") {
                paramaters += "&ss="+this.ss;
            }
            var resultStr=removeDuplicateParameters(paramaters);
            urlExcludingParams = url;
            paramsWithoutURL = resultStr;
            url+=resultStr;
            if (this.usePostMethod) {
                WtfGlobal.postData(urlExcludingParams, paramsWithoutURL);
            } else {
                Wtf.get('downloadframe').dom.src = url;
            }
            this.templateWindow.close();
        }
        this.templateWindow.close();
    },
    
    newEditTemplate: function(configstr) {
        var eobj =  Wtf.getCmp(this.templateid+"_buildReport" + this.tabtitle);
        if(eobj == null) {
            var custForm=new Wtf.customReport({
                id:'custForm'+this.id + this.tabtitle,
                stdate:this.stdate,
                enddate:this.enddate,
                accountid:this.accountid,
                reportGrid:this.grid,
                type:this.type,
                mode:this.mode,
                get:this.get,
                filename:Wtf.getCmp('as').getActiveTab().title,
                gridconfig:this.gridConfig,
                editTemplateConfig:configstr,
                overwriteflag:this.templateid
            });
            eobj = Wtf.getCmp(this.id + "_buildReport"+ this.tabtitle);
            if(eobj === undefined){
                eobj = new Wtf.reportBuilder.builderPanel({
                    title: WtfGlobal.getLocaleText("acc.rem.139"),  //"Report Layout Builder",
                    iconCls:"pwndCRM template_builder",
                    id: this.templateid+"_buildReport" + this.tabtitle,
                    closable: true,
                    autoScroll: true,
                    formCont: custForm
                });
                eobj.on("activate",function(panel){
                    panel.doLayout();
                });
                mainPanel.add(eobj);
            }
        }
        this.templateWindow.close();
        mainPanel.setActiveTab(eobj);
        mainPanel.doLayout();
    }
});
