/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.TaskProgressreport = function (config) {
    Wtf.apply(this, config);
    this.arr = [];
    this.creategrid();

    this.createFitlerCombos();

    this.createTBar();


    Wtf.TaskProgressreport.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.TaskProgressreport, Wtf.Panel, {
    onRender: function (config) {
        Wtf.TaskProgressreport.superclass.onRender.call(this, config);

        this.createPanel();

        this.add(this.jobOrderpanel);
        this.fetchStatement();
    },
    createPanel: function () {
        this.jobOrderpanel = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [{
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.grid],
                    tbar: this.buttonsArr,
                    bbar:[this.bottomToolbar]
                }]
        });
    },
    createFitlerCombos: function () {

        this.workOrderRec = Wtf.data.Record.create([
            {name: 'workorderid'},
            {name: 'workordername'},
            {name: 'wocode'},
            {name: 'projectid'}
        ]);

        this.workOrderStore = new Wtf.data.Store({
            url: "ACCJobWorkController/getWorkOrdersForCombo.do",
            baseParams: {
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.workOrderRec)
        });

        this.workOrderStore.on("load", function () {
           
        }, this);

        this.workOrderStore.load();

        this.workorderCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.machine.workCenter"),
            store: this.workOrderStore,
            valueField: 'workorderid',
            displayField: 'workordername',
            mode: 'remote',
            typeAhead: true,
            triggerAction: 'all',
            width: 200,
            value: '',
            extraFields: ['wocode']
        });
      
    },
    createTBar: function () {
        this.buttonsArr = [];
        this.bottomToolbar = [];
       

        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"),
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"),
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });

        this.buttonsArr.push('-', WtfGlobal.getLocaleText("acc.mrp.workorder"), this.workorderCombo);


        this.buttonsArr.push(this.fetchBttn);

        this.expButton = new Wtf.exportButton({
            obj: this,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.agedPay.exportTT"),
            //filename: WtfGlobal.getLocaleText("mrp.taskprogressreprot.registerreport.tab.title") + " v1",
            filename: WtfGlobal.getLocaleText("acc.taskProgress.list"),
            params: {
                
            },
            menuItem: {csv: true, pdf: true, rowPdf: false, xls: true},
            get: Wtf.autoNum.workordertaskstatusexport
        });
        this.bottomToolbar.push(this.expButton)

    },
    creategrid: function () {
        this.gridRec = Wtf.data.Record.create([
            {name: 'id'}, //projectname- is treated as work order  name
            {name: 'taskname'},
            {name: 'duration'},
            {name: 'startdate'},
            {name: 'enddate'},
            {name: 'productname'},
            {name: 'percentcomplete'},
            {name: 'skills'},
            {name: 'process'},
            {name: 'resourcedetails'},
            {name: 'qcdetails'},
            {name: 'consumptionDetails'},
            {name: 'materialconsumed'}
        ]);


         this.Store = new Wtf.data.Store({
            url: "ACCWorkOrder/getWorkOrderTaskProgressDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.gridRec),
            groupField: ['projectname', 'taskname', 'qcgroup']
        });
        
        
        this.sm = new Wtf.grid. RowSelectionModel({});
        this.sm.on('selectionchange', this.enableDisableButtons, this);

        this.ColumnArr = [];

        this.ColumnArr.push(this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect: true
        }));
        
        this.expander = new Wtf.grid.RowExpander({});
         this.ColumnArr.push(this.expander);
         this.expander.on("expand",this.onRowexpand,this);
         
        this.ColumnArr.push(new Wtf.grid.RowNumberer({
            width: 30
        }));
        this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.taskProgressGrid.header1"),
            dataIndex: 'taskname',
            width: 150,
            pdfwidth: 150
        });
        this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.taskProgressGrid.header2"),
            dataIndex: 'duration',
            width: 50,
            pdfwidth: 150
        });
        this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.taskProgressGrid.header3"),
            dataIndex: 'startdate',
            width: 150,
            pdfwidth: 150
        });
        this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.taskProgressGrid.header4"),
            dataIndex: 'enddate',
            width: 150,
            pdfwidth: 150
        });
        this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.contractMasterGrid.header7"),
            dataIndex: 'productname',
            width: 150,
            pdfwidth: 150
        });
          this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.mrp.taskprocgressreport.gridheader.skills"),
            dataIndex: 'skills',
            width: 150,
            pdfwidth: 150
        });
           this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.mrp.taskprocgressreport.gridheader.process"),
            dataIndex: 'process',
            width: 150,
            pdfwidth: 150
        });
         this.ColumnArr.push({
            header: "Task Progress In Percentage",
            dataIndex: 'percentcomplete',
            width: 350,
            pdfwidth: 150,
           renderer: WtfGlobal.progressRenderer
        });
      

        
        this.cm = new Wtf.grid.ColumnModel(this.ColumnArr);
          
        this.grid = new Wtf.grid.GridPanel({
            store: this.Store,
            cm: this.cm,
            border: false,
            sm:this.sm,
            stripeRows: true,
            loadMask: true,
            plugins:[this.expander]
        });
        
        this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
        this.Store.on('load', this.handleStoreOnLoad, this);

    },
    fetchStatement: function () {
        this.Store.load();
    },
    handleStoreBeforeLoad: function () {
        var currentBaseParams = this.Store.baseParams;

        var projectid = '';
        var workorderid = '';
     
        if (this.workorderCombo.getValue() && this.workorderCombo.getValue() != "") {
            var rec = WtfGlobal.searchRecord(this.workOrderStore, this.workorderCombo.getValue(), 'workorderid');
            projectid = rec.data.projectid != undefined ? rec.data.projectid : "";
             workorderid = rec.data.workorderid != undefined ? rec.data.workorderid : "";
            currentBaseParams.projectid = projectid;
            currentBaseParams.workorderid = workorderid;
        }else{
            currentBaseParams.projectid = projectid;
            currentBaseParams.workorderid = workorderid;
        }

        if (this.expButton) {
            this.expButton.setParams({
                projectid: projectid
            });
        }

        this.Store.baseParams = currentBaseParams;
        
    },
    handleStoreOnLoad: function () {
        if (this.Store.getCount() < 1) {
            if(this.workorderCombo.getValue()!=""){
                this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            }else{
                this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.clickonfetchbutton"));
            }
            this.grid.getView().refresh();
        }     
    },
    onRowexpand : function(scope, record) {
        this.fillExpanderBody(record);
    },
      fillExpanderBody: function(record) {
        var disHtml = "";
        this.custArr = [];
        var previd = "";
        var sameParent = false;
        var woHeader = this.woheader();
        var recordIndex="";
        var header = "";
        var qcheader="";
         var comsumptionheader="";
        
        //Resources details
        for (var i = 0; i < record.data.resourcedetails.length; i++) {
            var rec = record.data.resourcedetails[i];
            
            var currentid = rec['id'];
            if (previd != currentid) {             
                previd = currentid;
                sameParent = false;
            } else {
                sameParent = true;
            }
            
            header = this.getExpanderData(rec, sameParent, woHeader[1], woHeader[2]);
            var recordIndex = this.grid.getStore().findBy(
                function(record, id) {
                    if (record.get('id') === rec['id']) {
                        return true;  // a record with this data exists 
                    }
                    return false;  // there is no record in the store with this data
                }, this);
        }
        
       
        
        //Inventory consumption details 
        sameParent = false;
         var previd = "";
         var headerofInventoryConsumptionArr = this.headerofInventoryConsumption();
        for (var i = 0; i < record.data.consumptionDetails.length; i++) {
            var rec = record.data.consumptionDetails[i];
            
            var currentid = rec['id'];
            if (previd != currentid) {             
                previd = currentid;
                sameParent = false;
            } else {
                sameParent = true;
            }
            
            comsumptionheader = this.getExpanderDataOfInventoryConsumption(rec, sameParent, headerofInventoryConsumptionArr[1], headerofInventoryConsumptionArr[2]);
            if (recordIndex === -1 || recordIndex === "") {
                var recordIndex = this.grid.getStore().findBy(
                function(record, id) {
                            if (record.get('id') === rec['id']) {
                                return true;  // a record with this data exists 
                            }
                            return false;  // there is no record in the store with this data
                        }, this);
            }
        }
        
         //Qc details
        sameParent = false;
         var previd = "";
         var woheaderofQC = this.woheaderofQC();
        for (var i = 0; i < record.data.qcdetails.length; i++) {
            var rec = record.data.qcdetails[i];
            
            var currentid = rec['id'];
            if (previd != currentid) {             
                previd = currentid;
                sameParent = false;
            } else {
                sameParent = true;
            }
            
            qcheader = this.getExpanderDataOfQC(rec, sameParent, woheaderofQC[1], woheaderofQC[2]);
            if (recordIndex === -1 || recordIndex === "") {
                var recordIndex = this.grid.getStore().findBy(
                        function (record, id) {
                            if (record.get('id') === rec['id']) {
                                return true;  // a record with this data exists 
                            }
                            return false;  // there is no record in the store with this data
                        }, this);
            }
        }
        
        
        
    if (recordIndex != -1) {
//            if (true) {
        var body = Wtf.DomQuery.selectNode('tr:nth(2) div.x-grid3-row-body', this.grid.getView().getRow(recordIndex));
        disHtml = "<div class='expanderContainer1'>" + woHeader[0] + header + "</div>";
        var  disHtml2 = "<div class='expanderContainer1'>" + woheaderofQC[0] + qcheader + "</div>";
         var  disHtml3 = "<div class='expanderContainer1'>" + headerofInventoryConsumptionArr[0] + comsumptionheader + "</div>";
              body.innerHTML = disHtml+disHtml3+disHtml2 ;
    if (this.expandButtonClicked) {
        this.expander.suspendEvents('expand');              //suspend 'expand' event of RowExpander only in case of ExpandAll.
        this.expander.expandRow(recordIndex);                // After data set to Grid Row, expand row forcefully.
    }
    }
        
        
    },
     getExpanderData: function(rec,sameParent, minWidth, widthInPercent){
         
    if (!sameParent) {
        this.Repeatheader = "";
        this.serialNumber = 0;
    }
    this.Repeatheader += "<div style='width: 100%;min-width:" + minWidth + "px'>";  
    var machinename= rec['resourcename'];
//
    this.Repeatheader += "<span class='gridNo'>"+(++this.serialNumber)+".</span>";
//
    this.Repeatheader += "<span class='gridRow'  wtf:qtip='"+machinename+"' style='width: "+widthInPercent+"% ! important;'><a class='jumplink' wtf:qtip='"+machinename+"' href='#' onClick=''>"+Wtf.util.Format.ellipsis(machinename,10)+"&nbsp;</a></span>";   
    this.Repeatheader += "<br>";
    this.Repeatheader += "</div>";
    return this.Repeatheader;
 
    },
     getExpanderDataOfInventoryConsumption: function (rec, sameParent, minWidth, widthInPercent) {
        if (!sameParent) {
            this.Repeatheader2 = "";
            this.serialNumber2 = 0;
        }
        this.Repeatheader2 += "<div style='width: 100%;min-width:" + minWidth + "px'>";
        var productname = rec['productname'];
        var requiredqty = rec['requiredqty'];
        var blockedqty = rec['blockedqty'];
        var rejectedqty = rec['rejectedqty'];
        var wastedqty = rec['wastedqty'];
        var recycledqty = rec['recycledqty'];
       
//
        this.Repeatheader2 += "<span class='gridNo'>" + (++this.serialNumber2) + ".</span>";
//
        this.Repeatheader2 += "<span class='gridRow'  wtf:qtip='" + productname + "' style='width: " + widthInPercent + "% ! important;'><a class='jumplink' wtf:qtip='" + productname + "' href='#' onClick=''>" + Wtf.util.Format.ellipsis(productname, 10) + "&nbsp;</a></span>";
        this.Repeatheader2 += "<span class='gridRow'  wtf:qtip='" + requiredqty + "' style='width: " + widthInPercent + "% ! important;'><a class='jumplink' wtf:qtip='" + requiredqty + "' href='#' onClick=''>" + Wtf.util.Format.ellipsis(requiredqty, 10) + "&nbsp;</a></span>";
        this.Repeatheader2 += "<span class='gridRow'  wtf:qtip='" + blockedqty + "' style='width: " + widthInPercent + "% ! important;'><a class='jumplink' wtf:qtip='" + blockedqty + "' href='#' onClick=''>" + Wtf.util.Format.ellipsis(blockedqty, 10) + "&nbsp;</a></span>";
        this.Repeatheader2 += "<span class='gridRow'  wtf:qtip='" + rejectedqty + "' style='width: " + widthInPercent + "% ! important;'><a class='jumplink' wtf:qtip='" + rejectedqty + "' href='#' onClick=''>" + Wtf.util.Format.ellipsis(rejectedqty, 10) + "&nbsp;</a></span>";
        this.Repeatheader2 += "<span class='gridRow'  wtf:qtip='" + wastedqty + "' style='width: " + widthInPercent + "% ! important;'><a class='jumplink' wtf:qtip='" + wastedqty + "' href='#' onClick=''>" + Wtf.util.Format.ellipsis(wastedqty, 10) + "&nbsp;</a></span>";
        this.Repeatheader2 += "<span class='gridRow'  wtf:qtip='" + recycledqty + "' style='width: " + widthInPercent + "% ! important;'><a class='jumplink' wtf:qtip='" + recycledqty + "' href='#' onClick=''>" + Wtf.util.Format.ellipsis(recycledqty, 10) + "&nbsp;</a></span>";
        this.Repeatheader2 += "<br>";
        this.Repeatheader2 += "</div>";
        return this.Repeatheader2;

    },
    headerofInventoryConsumption: function () {
        var arr = [];
        var headerArray = [];
        arr = ["Product name","Required Quantity","Blocked Quantity","Rejected Quantity","Wasted Quantity","Recycled Quantity","                  "];
        var header = "<span class='gridHeader'>Inventory Consumption Details</span>";   //Account List

        var arrayLength = arr.length;
        var count = 0;
        for (var i = 0; i < arr.length; i++) {
            if (arr[i] != "") {
                count++;
            }
        }
        var widthInPercent = 100 / count;
        var minWidth = count * 100 + 40;
        header += "<div style='width: 100%;min-width:" + minWidth + "px'>";
        header += "<span class='gridNo' style='font-weight:bold;'>" + WtfGlobal.getLocaleText("acc.cnList.Sno") + "</span>";
        for (var i = 0; i < arr.length; i++) {
            header += "<span class='headerRow' style='width:" + widthInPercent + "% ! important;'>" + arr[i] + "</span>";
        }
        header += "</div><div style='width: 100%;min-width:" + minWidth + "px'><span class='gridLine'></span></div>";
        headerArray.push(header);
        headerArray.push(minWidth);
        headerArray.push(widthInPercent);
        return headerArray;
    },
    getExpanderDataOfQC: function (rec, sameParent, minWidth, widthInPercent) {
        if (!sameParent) {
            this.Repeatheader1 = "";
            this.serialNumber1 = 0;
        }
        this.Repeatheader1 += "<div style='width: 100%;min-width:" + minWidth + "px'>";
        var qcgroup = rec['qcgroup'];
        var qcpname = rec['qcpname'];
        var qcstatus = rec['qcstatus'];
        var qcminvalue = rec['qcminvalue'];
        var qcactval = rec['qcactval'];
        var qcdesc = rec['qcdesc'];
//
        this.Repeatheader1 += "<span class='gridNo'>" + (++this.serialNumber1) + ".</span>";
//
        this.Repeatheader1 += "<span class='gridRow'  wtf:qtip='" + qcgroup + "' style='width: " + widthInPercent + "% ! important;'><a class='jumplink' wtf:qtip='" + qcgroup + "' href='#' onClick=''>" + Wtf.util.Format.ellipsis(qcgroup, 10) + "&nbsp;</a></span>";
        this.Repeatheader1 += "<span class='gridRow'  wtf:qtip='" + qcpname + "' style='width: " + widthInPercent + "% ! important;'><a class='jumplink' wtf:qtip='" + qcpname + "' href='#' onClick=''>" + Wtf.util.Format.ellipsis(qcpname, 10) + "&nbsp;</a></span>";
        this.Repeatheader1 += "<span class='gridRow'  wtf:qtip='" + qcstatus + "' style='width: " + widthInPercent + "% ! important;'><a class='jumplink' wtf:qtip='" + qcstatus + "' href='#' onClick=''>" + Wtf.util.Format.ellipsis(qcstatus, 10) + "&nbsp;</a></span>";
        this.Repeatheader1 += "<span class='gridRow'  wtf:qtip='" + qcminvalue + "' style='width: " + widthInPercent + "% ! important;'><a class='jumplink' wtf:qtip='" + qcminvalue + "' href='#' onClick=''>" + Wtf.util.Format.ellipsis(qcminvalue, 10) + "&nbsp;</a></span>";
        this.Repeatheader1 += "<span class='gridRow'  wtf:qtip='" + qcactval + "' style='width: " + widthInPercent + "% ! important;'><a class='jumplink' wtf:qtip='" + qcactval + "' href='#' onClick=''>" + Wtf.util.Format.ellipsis(qcactval, 10) + "&nbsp;</a></span>";
        this.Repeatheader1 += "<span class='gridRow'  wtf:qtip='" + qcdesc + "' style='width: " + widthInPercent + "% ! important;'><a class='jumplink' wtf:qtip='" + qcdesc + "' href='#' onClick=''>" + Wtf.util.Format.ellipsis(qcdesc, 10) + "&nbsp;</a></span>";
        this.Repeatheader1 += "<br>";
        this.Repeatheader1 += "</div>";
        return this.Repeatheader1;

    },
    woheaderofQC: function () {
        var arr = [];
        var headerArray = [];
        arr = ["Group name","Parameter name","Status","Passing Value","Actual Value","Desc","                 "];
        var header = "<span class='gridHeader'>Quality Control Details</span>";   //Account List

        var arrayLength = arr.length;
        var count = 0;
        for (var i = 0; i < arr.length; i++) {
            if (arr[i] != "") {
                count++;
            }
        }
        var widthInPercent = 100 / count;
        var minWidth = count * 100 + 40;
        header += "<div style='width: 100%;min-width:" + minWidth + "px'>";
        header += "<span class='gridNo' style='font-weight:bold;'>" + WtfGlobal.getLocaleText("acc.cnList.Sno") + "</span>";
        for (var i = 0; i < arr.length; i++) {
            header += "<span class='headerRow' style='width:" + widthInPercent + "% ! important;'>" + arr[i] + "</span>";
        }
        header += "</div><div style='width: 100%;min-width:" + minWidth + "px'><span class='gridLine'></span></div>";
        headerArray.push(header);
        headerArray.push(minWidth);
        headerArray.push(widthInPercent);
        return headerArray;
    },
    woheader: function() {
        var arr=[];
        var headerArray = [];
        arr=["Resource name","           "];
        var header = "<span class='gridHeader'>Resources Details</span>";   //Account List
       
        var arrayLength=arr.length;
        var count=0;
        for(var i=0;i<arr.length;i++){
            if(arr[i] != ""){
                count++;
            }
        }
        var widthInPercent=100/count;
        var minWidth = count*100 + 40;
        header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
        header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
        for(var i=0;i<arr.length;i++){
            header += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + arr[i] + "</span>";
        }
        header += "</div><div style='width: 100%;min-width:"+minWidth+"px'><span class='gridLine'></span></div>";  
        headerArray.push(header);
        headerArray.push(minWidth);
        headerArray.push(widthInPercent);
        return headerArray;
    }

});
