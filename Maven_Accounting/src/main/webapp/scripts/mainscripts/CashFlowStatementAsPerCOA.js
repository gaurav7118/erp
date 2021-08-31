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
Wtf.account.TransactionListPanelViewCashFlowStatementAsPerCOA = function(config){
	
	this.createGrid();

	config.layout='border';
        
        this.startDate = new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  // 'From',
            name:'startdate',
            format:WtfGlobal.getOnlyDateFormat(),
           // readOnly:true,
            value:WtfGlobal.getDates(true)
        });
    
        this.endDate = new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  // 'To',
            format:WtfGlobal.getOnlyDateFormat(),
           // readOnly:true,
            name:'enddate',
            value:WtfGlobal.getDates(false)
        });

	Wtf.apply(this,{
                autoScroll:true,
                border:false,
                defaults:{border:false,bodyStyle:"background-color:white;"},
                items:[this.Grid,{layout:'fit',region:'west',width:'20%'},{layout:'fit',region:'east',width:'20%'}],
                tbar:[WtfGlobal.getLocaleText("acc.common.from"), this.startDate, WtfGlobal.getLocaleText("acc.common.to"), this.endDate,{
                    xtype:'button',
                    text:WtfGlobal.getLocaleText("acc.ra.fetch"),
                    tooltip:WtfGlobal.getLocaleText("acc.nee.8"),
                    iconCls:getButtonIconCls(Wtf.etype.resetbutton),
                    scope:this,
                    handler:this.fetchData
                },this.btnArr ]
        },config);
	
	Wtf.account.TransactionListPanelViewCashFlowStatementAsPerCOA.superclass.constructor.call(this, config);
    
},

Wtf.extend(Wtf.account.TransactionListPanelViewCashFlowStatementAsPerCOA, Wtf.Panel, {

    onRender: function(config) {
        var fromdate =WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        var todate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        Wtf.account.TransactionListPanelViewCashFlowStatementAsPerCOA.superclass.onRender.call(this, config);
        WtfGlobal.setAjaxTimeOut();
        this.loadMask.show();
        Wtf.Ajax.requestEx({
            url : "ACCReports/getCashFlowStatementAsPerCOA.do",
            params: {
                acctypes:0,
                controlAccounts:true,
                deleted:false,
                group:12,
                ignore:	true,
                mode:2,
                nondeleted:false,
                consolidateFlag:this.consolidateFlag,
                companyids:companyids,
                userid:loginid,
                stdate:fromdate,
                enddate:todate,
                periodView:true
            }
        }, this, this.successCallback);
	    
    },

        successCallback:function(response){
            this.loadMask.hide();
	    if(response.success){
	        this.Grid.store.loadData(response.data);
	        this.doLayout();
	    }
	},
	
    fetchData:function(){
        WtfGlobal.setAjaxTimeOut();
        var fromdate =WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        var todate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        Wtf.Ajax.requestEx({
            url : "ACCReports/getCashFlowStatementAsPerCOA.do",
            params: {
                acctypes:0,
                controlAccounts:true,
                deleted:false,
                group:12,
                ignore:	true,
                mode:2,
                nondeleted:false,
                consolidateFlag:this.consolidateFlag,
                companyids:companyids,
                userid:loginid,
                stdate:fromdate,
                enddate:todate,
                periodView:true
            }
        }, this, this.successCallback);
	    
    },

	createGrid:function(){
		var rec = new Wtf.data.Record.create([
	                {name: 'no'},
	                {name: 'desc'},
	                {name: 'value'},
		        {name: 'format'}
		           ]);
		 
		var Store = new Wtf.data.Store({
		                reader: new Wtf.data.KwlJsonReader({
		                },rec)
		              });
		
		this.Grid = new Wtf.grid.GridPanel({
	        autoScroll:true,
	        store: Store,
	        layout:'fit',
	    	width:'60%',
                loadMask : this.loadMask,
	      	region:'center',
	        columns: [{
	            header:'<font size=2px ><b>'+WtfGlobal.getLocaleText("acc.field.Noheader")+'</b>',
                    align:'center',
	            dataIndex:'no',
	            renderer:this.formatNo
	        },{
	            header:'<font size=2px ><b>'+" "+'</b>',
                    align:'center',
	            dataIndex:'desc',
	            renderer:this.formatName
	        },{
	            header:'<font size=2px ><b>'+WtfGlobal.getLocaleText("acc.field.COAinDeskera")+'</b>',
	            align:'right',
	            dataIndex:'value',
	            renderer:this.opBalRenderer
	        }],
	        border : false,
	        viewConfig: {
	            forceFit:true,
	            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
	        }
	    });
            
        this.loadMask = new Wtf.LoadMask(document.body,{
            msg : WtfGlobal.getLocaleText("acc.msgbox.50")
        }); 
        
        Store.on('beforeload', function(){
            this.loadMask.show();
        }, this);
        
        Store.on('loadexception', function(){
            this.loadMask.hide();
        }, this);
        
	Store.on('load', function() {
            if(Store.getCount()<1) {
                this.Grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.Grid.getView().refresh();
                this.loadMask.hide();
            }
        }, this);

        
        this.csvbtn = new Wtf.Action({      //Export : CSV
            iconCls: 'pwnd ' + 'exportcsv',
            text: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.common.exportToCSVTT") + "'>" + WtfGlobal.getLocaleText("acc.common.exportToCSV") + "</span>",
            scope: this,
            handler: function () {
                this.exportWithTemplate()
            }
        });
        
        this.pdfbtn = new Wtf.Action({      //Export : PDF
            iconCls: 'pwnd ' + 'exportpdf',
            text: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.common.exportToPDFTT") + "'>" + WtfGlobal.getLocaleText("acc.common.exportToPDF") + "</span>",
            scope: this,
            handler: function () {
                this.exportPdfTemplate()
            }
        });
        this.mnuBtns=[];
        this.mnuBtns.push(this.csvbtn);
        this.mnuBtns.push(this.pdfbtn);
        this.btnArr=[]; 
        this.btnArr.push(this.expButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.export"), //'Export',
            iconCls: (Wtf.isChrome ? 'pwnd exportChrome' : 'pwnd export'),
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details.',
            scope: this,
            menu: this.mnuBtns
        }));     
    },

    formatName:function(val, m, rec){
	    if(rec.data.desc && rec.data.format != "title" && rec.data.format != "total" && rec.data.format != "maintitle"){
	    	return '<div align=left>'+rec.data.desc+'</div>';
	    }else if(rec.data.format == "total"){
	    	return '<div align=right><b>'+rec.data.desc+'</b></div>';
	    }else if(rec.data.format == "title"){
	    	return '<div align=center><b>'+rec.data.desc+'</b></div>';
	    }else if(rec.data.format == "maintitle"){
	    	return '<div align=center><font size=3px ><u><b>'+rec.data.desc+'</b><u></font></div>';
	    }
	    return val;
	},
    
    formatNo:function(val, m, rec){
	    if(rec.data.no && rec.data.format != "title" && rec.data.format != "total" &&rec.data.format!="maintitle"){
	    	return '<div align=center>'+rec.data.no+'</div>';
	    }else if(rec.data.format == "total"){
	    	return '<div align=right><b>'+rec.data.no+'</b></div>';
	    }else if(rec.data.format == "title"){
	    	return '<div align=center><b>'+rec.data.no+'</b></div>';
	    }else if(rec.data.format == "maintitle"){
	    	return '<div align=center><font size=3px ><u><b>'+rec.data.no+'</b><u></font>';
	    }
	    return val;
	},
    
    opBalRenderer:function(val,m,rec){
            if(rec.data.format != "total" ){
		return WtfGlobal.withoutRateCurrencyDeletedSymbol(val,m,rec);
            }else if(rec.data.format == "title"){
		return '<font size=2px ><b>'+WtfGlobal.withoutRateCurrencyDeletedSymbol(val,m,rec)+'</b>';
            }else{
                return '<b>'+WtfGlobal.withoutRateCurrencyDeletedSymbol(val,m,rec)+'</b>';
            }	
	},

        
     exportPdfTemplate: function () {
        var get;
        var fileName;
        var fromdate = WtfGlobal.convertToGenericDate(this.startDate.getValue().add(Date.DAY, 0));
        var todate = WtfGlobal.convertToGenericDate(this.endDate.getValue().add(Date.DAY, 1));
        var jsonGrid;
        var exportUrl;
        exportUrl = "ACCReports/exportCashFlowStatementAsPerCOA.do";
        fileName = WtfGlobal.getLocaleText("acc.dashboard.cashFlowStatement");
        get = 29;
        jsonGrid = "{data:[{'header':'lname','title':'"+WtfGlobal.getLocaleText("acc.field.Noheader")+"','width':'175','align':''}," +
                "{'header':'lvalue','title':'" + WtfGlobal.getLocaleText("acc.field.COAinDeskera") + "','width':'100','align':'currency'},";

        var configstr = "{%22landscape%22:%22true%22,%22pageBorder%22:%22true%22,%22gridBorder%22:%22true%22,%22title%22:%22Test%20Title%22,%22subtitles%22:%22%22,%22headNote%22:%22Test%20Header%22,%22showLogo%22:%22false%22,%22headDate%22:%22false%22,%22footDate%22:%22true%22,%22footPager%22:%22false%22,%22headPager%22:%22true%22,%22footNote%22:%22Test%20Footer%22,%22textColor%22:%22000000%22,%22bgColor%22:%22FFFFFF%22}"
        var url = exportUrl + "?consolidateFlag=" + this.consolidateFlag + "&companyids=" + companyids + "&gcurrencyid=" + gcurrencyid + "&userid=" + loginid + "&filename=" + encodeURIComponent(fileName) + "&config=" + configstr + "&filetype=pdf&stdate=" + fromdate + "&enddate=" + todate
                + "&get=" + get + "&acctypes=0" + "&controlAccounts=true&group=12" + "&ignore=true&mode=2" + "&gridconfig=" + encodeURIComponent(jsonGrid)+"&periodView=true";
        Wtf.get('downloadframe').dom.src = url;
    },
    
    exportWithTemplate: function () {
        var exportUrl;
        var fileName;
        var fromdate = WtfGlobal.convertToGenericDate(this.startDate.getValue().add(Date.DAY, 0));
        var todate = WtfGlobal.convertToGenericDate(this.endDate.getValue().add(Date.DAY, 1));
        var header = WtfGlobal.getLocaleText("acc.field.lname,lvalue");
        var title = WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.field.COAinDeskera");

        exportUrl = "ACCReports/exportCashFlowStatementAsPerCOA.do";
        fileName = WtfGlobal.getLocaleText("acc.dashboard.cashFlowStatement");

        var align = "none,none";
        var url = exportUrl + "?consolidateFlag=" + this.consolidateFlag + "&companyids=" + companyids + "&gcurrencyid=" + gcurrencyid + "&userid=" + loginid + "&filename=" + encodeURIComponent(fileName) + "&filetype=csv&stdate=" + fromdate + "&enddate=" + todate + "&get=27"+ "&acctypes=0" + "&controlAccounts=true&group=12" + "&ignore=true&mode=2"
                + "&header=" + header + "&title=" + title + "&width=150&get=27&align=" + align+"&periodView=true";         
        Wtf.get('downloadframe').dom.src = url;
    },
	createReport:function(){
	
	} 

});
