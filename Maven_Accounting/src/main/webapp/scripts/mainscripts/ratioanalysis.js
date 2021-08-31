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
Wtf.account.RatioAnalysis = function(config){
    this.uPermType=Wtf.UPerm.qanalysis;
    this.permType=Wtf.Perm.qanalysis;
    this.startDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate',
        format:WtfGlobal.getOnlyDateFormat(),
        value:this.getDates(true)
    });
    this.endDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate',
        value:this.getDates(false)
    });
    this.createGrids();
    this.grid = this.rGrid;
     var btnArr=[];
    var mnuBtns=[];
    this.printbtn=new Wtf.Action({
        iconCls:'pwnd printButtonIcon',
        tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print Report Details.',
        text : WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        scope: this,
        handler:function(){
            this.printRatioAnalysis();
        }
    });
    var csvbtn=new Wtf.Action({
        iconCls:'pwnd '+'exportcsv',
        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.ra.csvTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToCSV")+"</span>",
        scope: this,
        handler:function(){
            this.exportWithTemplate("csv");
        }
    });
    mnuBtns.push(csvbtn)
        var xlsbtn=new Wtf.Action({
        iconCls:'pwnd '+'exportcsv',
        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToXLS")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToXLS")+"</span>",
        scope: this,
        handler:function(){
            this.exportWithTemplate("xls");
        }
    });
    mnuBtns.push(xlsbtn)
     var pdfbtn=new Wtf.Action({
        iconCls:'pwnd '+'exportpdf',
        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.ra.pdfTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToPDF")+"</span>",
        scope: this,
        handler:function(){
            this.exportPdfTemplate()
        }
    });
    mnuBtns.push(pdfbtn)

    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.exportdata)){
        btnArr.push(this.expButton=new Wtf.Button({
    //        iconCls:'pwnd '+'exportcsv',
            text:WtfGlobal.getLocaleText("acc.ra.export"),  //'Export',
            iconCls: (Wtf.isChrome?'pwnd exportChrome':'pwnd export'),
            tooltip :WtfGlobal.getLocaleText("acc.ra.exportTT"),  //'Export report details',
            scope: this,
            menu:mnuBtns
        }));
    }
    if (!WtfGlobal.EnableDisable(this.uPermType, this.permType.printdata)) {
        btnArr.push(this.printbtn);
    }
    Wtf.apply(this,{
        saperate:true,
        items:[{
            border:false,
            layout : "border",
            scope:this,
            items:[{
                region:'center',
                layout:'border',
                autoScroll:true,
                border:false,
                defaults:{layout:'fit',split:true, border:false},
                items:[{
                    region:'west',
                    width:'50%',
                    items:this.lGrid
                },{
                    region:'center',
                   items:this.rGrid
                }]
            }],
            tbar:[WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),this.endDate,'-',{
                xtype:'button',
                text:WtfGlobal.getLocaleText("acc.ra.fetch"),  //'Fetch',
                iconCls:'accountingbase fetch',
                tooltip:WtfGlobal.getLocaleText("acc.ra.fetchTT"),  //"Select a time period to view corresponding ratio analysis.",
                scope:this,
                handler:this.fetchData
            },btnArr]
        }]

    },config);
    Wtf.account.RatioAnalysis.superclass.constructor.call(this,config);
}

Wtf.extend( Wtf.account.RatioAnalysis,Wtf.Panel,{
        exportPdfTemplate:function(){
        var get;
        var fileName;
        var jsonGrid;
        var exportUrl;
        exportUrl = getExportUrl(29);
        fileName = WtfGlobal.getLocaleText("acc.ra.tabTitle");
        get = 29;
        jsonGrid = "{data:[{'header':'lname','title':'"+WtfGlobal.getLocaleText("acc.ra.principalGroups")+"','width':'150','align':''},"+
                        "{'header':'lvalue','title':'"+WtfGlobal.getLocaleText("acc.ra.value")+"','width':'150','align':'currency'},"+
                        "{'header':'rname','title':'"+WtfGlobal.getLocaleText("acc.ra.principalRatios")+"','width':'150','align':''},"+
                        "{'header':'rvalue','title':'"+WtfGlobal.getLocaleText("acc.ra.value")+"','width':'150','align':'currency'}]}";

        var configstr = "{%22landscape%22:%22true%22,%22pageBorder%22:%22true%22,%22gridBorder%22:%22true%22,%22title%22:%22Test%20Title%22,%22subtitles%22:%22%22,%22headNote%22:%22Test%20Header%22,%22showLogo%22:%22false%22,%22headDate%22:%22false%22,%22footDate%22:%22true%22,%22footPager%22:%22false%22,%22headPager%22:%22true%22,%22footNote%22:%22Test%20Footer%22,%22textColor%22:%22000000%22,%22bgColor%22:%22FFFFFF%22}"
         var url = exportUrl+"?filename="+encodeURIComponent(fileName)+"&config="+configstr+"&filetype=pdf&stdate="+this.sdate+"&enddate="+this.edate+"&accountid="
                     +"&get="+get+"&gridconfig="+encodeURIComponent(jsonGrid);
            Wtf.get('downloadframe').dom.src = url;

        /*new Wtf.selectTempWin({
                type:'pdf',
                get:get,
                stdate:this.sdate,
                enddate:this.edate,
                accountid:"",
                extra:{},
                mode:"",
                paramstring:"",
                filename:fileName,
                storeToload:"",//obj.pdfStore,
                gridConfig : jsonGrid,
                grid:"",
                json:""
            });*/
    },
    exportWithTemplate:function(type){
        var exportUrl;
        var fileName;
        var header = "lname,lvalue,rname,rvalue";
        var title = WtfGlobal.getLocaleText("acc.ra.principalGroups")+","+WtfGlobal.getLocaleText("acc.ra.value")+","+WtfGlobal.getLocaleText("acc.ra.principalRatios")+","+WtfGlobal.getLocaleText("acc.ra.value");
        
        exportUrl = getExportUrl(29);
        fileName = WtfGlobal.getLocaleText("acc.ra.tabTitle");
        

        var align = "none,none,none,none";
        var url = exportUrl+"?filename="+encodeURIComponent(fileName)+"&filetype="+type+"&stdate="+this.sdate+"&enddate="+this.edate+"&accountid="
                            +"&header="+header+"&title="+title+"&width=150&get=27&align="+align;
        Wtf.get('downloadframe').dom.src = url;
    },
    
//    printRatioAnalysis:function(){
//        var exportUrl;
//        var header = "lname,lvalue,rname,rvalue";
//        var title = "Principal Groups,Value,Principal Ratios,Value";
//        exportUrl = getExportUrl(29);
//        var align = "none,none,none,none";
//        var url = exportUrl+"?name=Ratio Analysis&filetype=print&stdate="+this.sdate+"&enddate="+this.edate+"&accountid="
//                            +"&header="+header+"&title="+title+"&width=150&get=27&align="+align;
//
//        window.open(url, "mywindow","menubar=1,resizable=1,scrollbars=1");
//    }, 
    
    printRatioAnalysis:function(){
        var exportUrl;
        var header = "lname,lvalue,rname,rvalue";
        var title = WtfGlobal.getLocaleText("acc.ra.principalGroups")+","+WtfGlobal.getLocaleText("acc.ra.value")+","+WtfGlobal.getLocaleText("acc.ra.principalRatios")+","+WtfGlobal.getLocaleText("acc.ra.value");
        exportUrl = getExportUrl(29);
        var align = "none,none,none,none";
        var url = exportUrl+"?name="+WtfGlobal.getLocaleText("acc.ra.tabTitle")+"&filetype=print&stdate="+this.sdate+"&enddate="+this.edate+"&accountid="
                            +"&header="+header+"&title="+title+"&width=150&get=27&align="+align+"&filename="+WtfGlobal.getLocaleText("acc.ra.tabTT");   //Added filename to show in Ratio Analysis Report Print view
                        
        url+="&generatedOnTime="+WtfGlobal.getGeneratedOnTimestamp();           
        window.open(url, "mywindow","menubar=1,resizable=1,scrollbars=1");
    },
    
    onRender:function(config){
        Wtf.account.RatioAnalysis.superclass.onRender.call(this, config);
        this.fetchData();
    },
    getDates:function(start){
        var d=new Date();
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom)
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd;
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },

    fetchData:function(){
       this.sDate=this.startDate.getValue();
       this.eDate=this.endDate.getValue();
       this.sdate=WtfGlobal.convertToGenericDate(this.startDate.getValue().add(Date.DAY,0));
       this.edate=WtfGlobal.convertToGenericDate(this.endDate.getValue().add(Date.DAY,1));
         if(this.sDate>this.eDate){
            WtfComMsgBox(1,2);
            return;
        }

        var params={
            stdate:this.sdate,
            enddate:this.edate,
            mode:69
        }
        this.loadingMask = new Wtf.LoadMask(document.body,{ //ERP-19783
                msg : WtfGlobal.getLocaleText("acc.msgbox.50")
            });
       this.loadingMask.show();
        /*
         * Increasing Timeout to 2hr 
         */
        Wtf.Ajax.timeout = 7200000;
        Wtf.Ajax.requestEx({
//            url:Wtf.req.account+'CompanyManager.jsp',
            url : "ACCReports/getRatioAnalysis.do",
            params:params
        }, this, this.successCallback,this.failureCallback);
    },

    successCallback:function(response){
        Wtf.Ajax.timeout=30000;
        if(response.success){                          
            this.lGrid.store.loadData(response.data);
            this.rGrid.store.loadData(response.data);
            this.loadingMask.hide();//ERP-19783
            this.doLayout();
        }
    },
    failureCallback:function(){        
        Wtf.Ajax.timeout=30000;
            this.loadingMask.hide();//ERP-19783
    },

    createGrids:function(){
        var rec = new Wtf.data.Record.create([
            {name: 'name'},
            {name: 'desc'},
            {name: 'value'},
//            {name: 'pending',type:'boolean'},
            {name: 'fmt'}
        ]);
        var lStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "left"
            },rec),
            url: Wtf.req.account+'CompanyManager.jsp'
        });
        var rStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "right"
            },rec),
            url: Wtf.req.account+'CompanyManager.jsp'
        });
        this.lGrid = new Wtf.grid.GridPanel({
            autoScroll:true,
            store: lStore,
            columns: [{
                header:WtfGlobal.getLocaleText("acc.ra.principalGroups"),  //'Principal Groups',
                dataIndex:'name',
                renderer:this.formatName
            },{
                header:WtfGlobal.getLocaleText("acc.ra.value"),  //"Value",
                align:'right',
                dataIndex:'value',
                renderer:this.formatValue
            }],
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        this.rGrid = new Wtf.grid.GridPanel({
            autoScroll:true,
            store: rStore,
            columns: [{
                header:WtfGlobal.getLocaleText("acc.ra.principalRatios"),  //'Principal Ratios',
                dataIndex:'name',
                renderer:this.formatName
            },{
                header:WtfGlobal.getLocaleText("acc.ra.value"),  //"Value",
                align:'right',
                dataIndex:'value',
                renderer:this.formatValue
            }],
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });

        lStore.on('load', function() {
            if(lStore.getCount()<1) {
                this.lGrid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.lGrid.getView().refresh();
            }
        }, this);
        rStore.on('load', function() {
            if(rStore.getCount()<1) {
                this.rGrid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.rGrid.getView().refresh();
            }
        }, this);
    },

    formatName:function(val, m, rec){
        if(rec.data.desc){
            return '<div>'+val+'</div><div class="grid-row-desc">'+rec.data.desc+'</div>';
        }

        return val;
    },

    formatValue:function(val,m,rec){
        var fmtVal="";
        if(isNaN(val))
            return val;
        else
            val=new Number(val);
        switch(rec.data.fmt){
            case "CD":
                  if(val>0)
                    fmtVal = WtfGlobal.formatNumberForVariableDigit(val,"{s}{c} {v} Dr");
                  else if(val<0)
                    fmtVal = WtfGlobal.formatNumberForVariableDigit(-val,"{s}{c} {v} Cr");
                  else
                    fmtVal = WtfGlobal.formatNumberForVariableDigit(val,"{s}{c} {v}");
                break;
            case "RAT":
                  fmtVal = WtfGlobal.formatNumber(val,"{s}{v} : 1");
                break;
            case "PER":
                  fmtVal = WtfGlobal.formatNumber(val,"{s}{v} %");
                break;
            case "DAY":
                  fmtVal = WtfGlobal.formatNumber(val,"{s}{v} days");
                break;
            default:
                fmtVal = WtfGlobal.formatNumber(val,"{s}{v}");
        }

//        if(rec.data.pending)
//            fmtVal = "<span style='color:red'>"+fmtVal+"</span>";

        return fmtVal;
    }
});
