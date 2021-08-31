/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

Wtf.Project_TemplateId = {
    ROUTINGMASTER:1,
    WORKORDER:2
};

function callResolveConflictLabourMachineDynamicLoad() {
    var panelID = "resolveConflictMachineReport";
    var panel = Wtf.getCmp(panelID);
    if (panel == null) {
        panel = new Wtf.account.ResolveConflictLabourMachineReport({
            id: "resolveConflictMachineReport",
            title: WtfGlobal.getLocaleText("acc.resolveconflict.tabtitle"),
            tabTip: WtfGlobal.getLocaleText("acc.resolveconflict.tabtip"),
            layout: 'fit',
            border: false,
            closable: true,
            iconCls: 'accountingbase invoicelist',
            moduleId: 1
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function callLabourDetails(searchStr, filterAppend, wcid, isFromWC, labourids) {
    if (!WtfGlobal.EnableDisable(Wtf.UPerm.labourmaster, Wtf.Perm.labourmaster.viewlbor)) {
        var panel = Wtf.getCmp("mainLabourDetails");
        var isAlreadyOpen = true;
        if (panel == null) {
            panel = new Wtf.TabPanel({
                title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.labour.labouradministration"), Wtf.TAB_TITLE_LENGTH),
                tabTip: WtfGlobal.getLocaleText("acc.labour.labouradministration"),
                id: 'mainLabourDetails',
                closable: true,
                border: false,
                iconCls: 'labouricon',
                activeTab: 0
            });
            Wtf.getCmp('as').add(panel);
            callLabourList(searchStr, filterAppend, wcid, isFromWC, labourids);
            var isMachineCost = false;
            callResourceCost(isMachineCost);//isMachineCost = false (Labour Cost)
//        callResourceAnalysisReportDynamicLoad();
            panel.activate(0);
            isAlreadyOpen = false;
        } else {
            Wtf.getCmp('as').setActiveTab(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
        showAdvanceSearch(panel.items.map.labourList, searchStr, filterAppend);

        if (isAlreadyOpen) {
            //When Labour master  report tab is already open and user tries to open machine master tab at that time control comes here.
            var labourmasterList = Wtf.getCmp("labourList");
            if (isFromWC && isFromWC === true) {
                // When machine master report is opened  from workcetnre report
                var obj = {};
                obj.wcid = wcid;
                labourmasterList.setWorkCenterID(obj);
                labourmasterList.setLabouridsIDs('');
                labourmasterList.fetchStatement();
            } else if (labourids !== undefined && labourids !== '') {
                // When labour master report is opened from work order report
                var obj = {};
                obj.labourids = labourids;
                labourmasterList.setWorkCenterID('');
                labourmasterList.setLabouridsIDs(obj);
                labourmasterList.fetchStatement();
            } else {
                //When Labour master report is opened from its own report entry.
                labourmasterList.setWorkCenterID('');
                labourmasterList.setLabouridsIDs('');
                labourmasterList.fetchStatement();
            }
        }
    } else {

        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText({key: "acc.accessrestrcition.permission.msg", params: [WtfGlobal.getLocaleText("acc.up.66")]})], 2);
    }
}
function callLabourList(searchStr, filterAppend,wcid,isFromWC,labourids) {
    
    var winid =  'labourList';
    var labourListpanel = Wtf.getCmp(winid);
    if (labourListpanel == null) {
        labourListpanel = new Wtf.account.labourList({
            id: winid,
            border: false,
            layout: 'fit',
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.labourList"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("acc.field.labourList"), //'Vendor List',
            iconCls: 'labouricon',
            searchJson:searchStr,
            filterAppend:filterAppend,
            wcid:wcid,
            isFromWC:isFromWC,
            labourids: labourids
        });
//        if (isFromWC) {
//            Wtf.getCmp('workcentreMaintabMaster').add(labourListpanel);
//        }else {
            Wtf.getCmp('mainLabourDetails').add(labourListpanel);
//        }
    }
//    if (isFromWC) {
//        Wtf.getCmp('workcentreMaintabMaster').setActiveTab(labourListpanel);
//        Wtf.getCmp('workcentreMaintabMaster').doLayout();
//    } else {
        Wtf.getCmp('mainLabourDetails').setActiveTab(labourListpanel);
        Wtf.getCmp('mainLabourDetails').doLayout();
//    }
}

function callContractMasterList(isReport, CompID) {
    var ID = isReport ? 'contractmasterlistReport' : 'contractmasterlistEntry'
    var contractMasterListpanel = Wtf.getCmp(ID);
    if (contractMasterListpanel == null) {
        contractMasterListpanel = new Wtf.account.ContractMasterList({
            id: ID, //Do not change this id as it is used somewhere
            border: false,
            layout: 'fit',
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.mastercontract.title"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("acc.mastercontract.title"),
            iconCls: 'accountingbase vendor',
            isReport: isReport
        });
        Wtf.getCmp(CompID).add(contractMasterListpanel);
    }
    Wtf.getCmp(CompID).setActiveTab(contractMasterListpanel);
    Wtf.getCmp(CompID).doLayout();
}
function callContractMasterDetails(isReport,searchStr,filterAppend) {
    if (!WtfGlobal.EnableDisable(Wtf.UPerm.mastercontract, Wtf.Perm.mastercontract.viewmstr)) {
        var CompID = isReport ? 'maincontractmasterdetailsReport' : 'maincontractmasterdetailsEntry'
        var panel = Wtf.getCmp(CompID);
        if (panel == null) {
            panel = new Wtf.TabPanel({
                title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.mastercontract.title"), Wtf.TAB_TITLE_LENGTH),
                tabTip: WtfGlobal.getLocaleText("acc.mastercontract.title"),
                id: CompID,
                closable: true,
                border: false,
                iconCls: 'accountingbase vendor',
                activeTab: 0,
                isReport: isReport
            });
            Wtf.getCmp('as').add(panel);
            callContractMasterList(isReport, CompID);
        } else {
            Wtf.getCmp('as').setActiveTab(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();

        showAdvanceSearch(panel.items.map.contractmasterlistEntry, searchStr, filterAppend);
    } else {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText({key: "acc.accessrestrcition.permission.msg", params: [WtfGlobal.getLocaleText("acc.up.70")]})], 2);
    }
}

function callMRPWorkOrderWindow(winid,isEdit,record,reporttab ,projectId) {
    var woid = (record && record.data && record.data.id) ? record.data.id : ""; 
    var wocode = (record && record.data && record.data.id) ? record.data.workorderid : ""; 
    winid=(winid==null?"mrpWorkOrderEntrytab" + woid:winid);
    var panel = Wtf.getCmp(winid);
    if(panel == null){
        panel=new Wtf.account.WorkOrderTab({
            title:isEdit ? Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("mrp.workorder.report.editworkorder") + "-" +wocode, Wtf.TAB_TITLE_LENGTH) : Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.mrp.createwo.title"), Wtf.TAB_TITLE_LENGTH), 
            tabTip:isEdit ? WtfGlobal.getLocaleText("mrp.workorder.report.editworkorder") + "-" +wocode: WtfGlobal.getLocaleText("acc.mrp.createwo.title"),
            id:winid,
            iconCls: 'workordericon',
            border : false,
            closable:true,
            layout:"fit",
            isEdit:isEdit,
            projectId:projectId,
            record:record,
            workorderreport:reporttab
        });
        panel.on("activate",function(){
            panel.doLayout();
            Wtf.getCmp('as').doLayout();
        },this);
        Wtf.getCmp('as').add(panel);
    }
    
    Wtf.getCmp(winid).on('beforeclose', function() {
        deleteDirtyProject(projectId);
    },this); 
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}
function callMRPWorkOrderReport(winid, hideCRUDButtons, searchStr, filterAppend) {
    if (!WtfGlobal.EnableDisable(Wtf.UPerm.workorder, Wtf.Perm.workorder.viewwo)) {
        winid = (winid == null ? "MRPWorkOrderReport" : winid);
        var panel = Wtf.getCmp(winid);
        if (panel == null) {
            panel = new Wtf.account.workOrderReport({
                id: winid,
                border: false,
                layout: 'fit',
                hideCRUDButtons: hideCRUDButtons,
                title: hideCRUDButtons ? Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("mrp.workorder.report.title"), Wtf.TAB_TITLE_LENGTH) : Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("mrp.workorder.entry.title"), Wtf.TAB_TITLE_LENGTH),
                tabTip: WtfGlobal.getLocaleText("mrp.workorder.report.title"),
                iconCls: 'workordericon'
            });
            Wtf.getCmp('as').add(panel);
            panel.on('journalentry',callJournalEntryDetails);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
        showAdvanceSearch(panel, searchStr, filterAppend);
    } else {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText({key: "acc.accessrestrcition.permission.msg", params: [WtfGlobal.getLocaleText("acc.up.71")]})], 2);
    }

}
function workOrderStockDetailsReport(winid, hideCRUDButtons, searchStr, filterAppend) {
    if (!WtfGlobal.EnableDisable(Wtf.UPerm.workorder, Wtf.Perm.workorder.viewwo)) {
        winid = (winid == null ? "MRPWorkOrderReport" : winid);
        var panel = Wtf.getCmp(winid);
        if (panel == null) {
            panel = new Wtf.account.workOrderStockDetailsReport({
                id: winid,
                border: false,
                layout: 'fit',
                hideCRUDButtons: hideCRUDButtons,
                hideExtraColumns:true,
                title: hideCRUDButtons ? Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.mrp.workorderstockdetailsreport"), Wtf.TAB_TITLE_LENGTH) : Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("mrp.workorder.entry.title"), Wtf.TAB_TITLE_LENGTH),
                tabTip: WtfGlobal.getLocaleText("acc.mrp.workorderstockdetailsreport"),
                iconCls: 'workordericon'
            });
            Wtf.getCmp('as').add(panel);        
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
        showAdvanceSearch(panel, searchStr, filterAppend);
    } else {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText({key: "acc.accessrestrcition.permission.msg", params: [WtfGlobal.getLocaleText("acc.up.71")]})], 2);
    }

}
function callResourceAnalysisReportDynamicLoad() {
    var panelID = "resourceAnalysisReport";
    var panel = Wtf.getCmp(panelID);
    if (panel == null) {
        panel = new Wtf.account.ResourceAnalysisReport({
            id: "resourceAnalysisReport",
            title: WtfGlobal.getLocaleText("acc.resourceanalysis.tabtitle"),
            tabTip: WtfGlobal.getLocaleText("acc.resourceanalysis.tabtip"),
            layout: 'fit',
            border: false,
            iconCls: 'accountingbase invoicelist',
            moduleId: 1
        });
        Wtf.getCmp('mainLabourDetails').add(panelID);
    }
    Wtf.getCmp('mainLabourDetails').setActiveTab(panelID);
    Wtf.getCmp('mainLabourDetails').doLayout();
}
function callMachineMasterList(wcid, isFromWC, searchStr, filterAppend, machineids) {
    if (!WtfGlobal.EnableDisable(Wtf.UPerm.machinemaster, Wtf.Perm.machinemaster.viewmc)) {
        var panel = Wtf.getCmp("mainmachinemasterlist");
        var isAlreadyOpen = true;
        if (panel == null) {
            panel = new Wtf.TabPanel({
                title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText('acc.field.machinemaster.title'), Wtf.TAB_TITLE_LENGTH),
                tabTip: WtfGlobal.getLocaleText('acc.field.machinemaster.title'),
                id: 'mainmachinemasterlist',
                border: false,
                iconCls: 'machineicon',
                closable: true,
                activeTab: 0

            });
            isAlreadyOpen = false;
            Wtf.getCmp('as').add(panel);
            callMachineMasterListDetails(wcid, isFromWC, machineids);
            var isMachineCost = true;
            callResourceCost(isMachineCost);//isMachineCost = true (Machine Cost)
            panel.activate(0);
        }

        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();

        if (isAlreadyOpen) {
            //When Machine master  report is already open and user tries to open machine master tab at that time control comes here
            var machinemasterList = Wtf.getCmp("machinemasterlist");
            if (isFromWC && isFromWC === true) {
                // When machine master report is opened  from workcetnre report
                var obj = {};
                obj.wcid = wcid;
                obj.isFromWC = isFromWC;
                machinemasterList.setWorkCenterID(obj);
                machinemasterList.setmachineIDs('');
                machinemasterList.loadStore();
            } else if (machineids !== undefined && machineids !== '') {
                // When machine master report is opened from work order report
                var obj = {};
                obj.machineids = machineids;
                machinemasterList.setWorkCenterID('');
                machinemasterList.setmachineIDs(obj);
                machinemasterList.loadStore();
            } else {
                //When Machine master report is opened from its own report entry.
                machinemasterList.setWorkCenterID('');
                machinemasterList.setmachineIDs('');
                machinemasterList.loadStore();
            }
        }

        showAdvanceSearch(panel.items.map.machinemasterlist, searchStr, filterAppend);
    } else {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText({key: "acc.accessrestrcition.permission.msg", params: [WtfGlobal.getLocaleText("acc.up.67")]})], 2);
    }
}

function callMachineMasterListDetails(wcid,isFromWC,machineids) {
    var reportPanel = Wtf.getCmp('machinemasterlist');
    if (reportPanel == null) {
         reportPanel = new Wtf.account.MachineMasterList({
            id: 'machinemasterlist',
            border: false,
            title: WtfGlobal.getLocaleText('acc.field.machinemaster.title'),
            tabTip: WtfGlobal.getLocaleText('acc.field.machinemaster.title'),
            layout: 'fit',
            moduleid: Wtf.MACHINE_MANAGEMENT_MODULE_ID,
            label: WtfGlobal.getLocaleText('acc.field.machinemaster.title'),
            iconCls: 'machineicon',
            wcid:wcid,
            machineids:machineids,
//            closable:isFromWC,
            isFromWC:isFromWC
        });
//        if (isFromWC) {
//            Wtf.getCmp('workcentreMaintabMaster').add(reportPanel);
//        }else {
            Wtf.getCmp('mainmachinemasterlist').add(reportPanel);
//        }
    }
//    if (isFromWC) {
//        Wtf.getCmp('workcentreMaintabMaster').setActiveTab(reportPanel);
//        Wtf.getCmp('workcentreMaintabMaster').doLayout();
//    } else {
        Wtf.getCmp('mainmachinemasterlist').setActiveTab(reportPanel);
        Wtf.getCmp('mainmachinemasterlist').doLayout();
//    }
    
}

function callTaskProgressList() {
    var panel = Wtf.getCmp("maintaskprogresslist");
    if (panel == null) { 
        panel = new Wtf.TabPanel({
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText('acc.taskProgress'), Wtf.TAB_TITLE_LENGTH), //'Task Progress'
            tabTip: WtfGlobal.getLocaleText('acc.mrp.reports.taskprogressreport.ttip'),
            id: 'maintaskprogresslist',
            border: false,
            iconCls: 'accountingbase overdue',
            closable: true,
            activeTab: 0
        });
        Wtf.getCmp('as').add(panel);
        callTaskProgressListDetails();
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function callTaskProgressListDetails() {
    var reportPanel = Wtf.getCmp('taskprogresslist');
    if (reportPanel == null) {
         reportPanel = new Wtf.TaskProgressreport({
            id: 'taskprogresslist',
            border: false,
            title: WtfGlobal.getLocaleText('acc.taskProgress.list'),
            tabTip: WtfGlobal.getLocaleText('acc.mrp.reports.taskprogressreport.ttip'),
            layout: 'fit',
            moduleid: 2,
            label: WtfGlobal.getLocaleText('acc.taskProgress.list'),
            iconCls: 'accountingbase invoicelist'
        });
        Wtf.getCmp('maintaskprogresslist').add(reportPanel);
    }
    Wtf.getCmp('maintaskprogresslist').setActiveTab(reportPanel);
    Wtf.getCmp('maintaskprogresslist').doLayout();
    
}

function workCentreMaster(isReport, searchStr, filterAppend, wcid) {
    if (!WtfGlobal.EnableDisable(Wtf.UPerm.workcentre, Wtf.Perm.workcentre.viewwc)) {
        var compID = isReport ? 'workcentreMaintabReport' : 'workcentreMaintabMaster';
        var panel = Wtf.getCmp(compID);
        var isAlreadyOpen = true;
        if (panel == null) {
            panel = new Wtf.TabPanel({
                title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.mrp.workcentermaster"), Wtf.TAB_TITLE_LENGTH),
                tabTip: WtfGlobal.getLocaleText("acc.mrp.workcentermaster"), //'Products & Services List',
                id: compID,
                closable: true,
                border: false,
                iconCls: 'workcentreicon',
                activeTab: 0,
                isReport: isReport
            });
            Wtf.getCmp('as').add(panel);
            workCentreMasterTab(isReport, compID, wcid);
            var reportPan = Wtf.getCmp('workcentreMaintabMaster');
            if (reportPan !== undefined) {
                isAlreadyOpen = false;
            }

        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
        showAdvanceSearch(panel.items.map.workcentretabMaster, searchStr, filterAppend);

        if (isAlreadyOpen) {

            var reportPanel = Wtf.getCmp('workcentretabMaster');
            if (wcid !== undefined && wcid !== '') {
                var obj = {};
                obj.wcid = wcid;
                reportPanel.setWorkCenterID(obj);
                reportPanel.fetchStatement();
            } else {
                reportPanel.setWorkCenterID('');
                reportPanel.fetchStatement();
            }
        }
    } else {

        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText({key: "acc.accessrestrcition.permission.msg", params: [WtfGlobal.getLocaleText("acc.up.68")]})], 2);
    }
}

function workCentreMasterTab(isReport, compID,wcid) {
    var ID = isReport ? 'workcentretabReport' : 'workcentretabMaster';
    var panel = Wtf.getCmp(ID);
    if (panel == null) {
        panel = new Wtf.workCentreMaster({
            id: ID,
            border: false,
            moduleId: Wtf.MRP_Work_Centre_ModuleID,
            layout: 'fit',
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.mrp.workcentermaster"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("acc.mrp.workcentermaster"), //'Products & Services List',
            iconCls: 'workcentreicon',
            isReport: isReport,
            wcid:wcid
        });

        Wtf.getCmp(compID).add(panel);
    }

    Wtf.getCmp(compID).setActiveTab(panel);
    Wtf.getCmp(compID).doLayout();
}


function callWorkcentreWindow(isEdit, rec, isClone, reporttab){

        var panel = Wtf.getCmp("mainWorkcentrePanel");
        if(panel!=null && isEdit){
            Wtf.getCmp('as').remove(panel);
            panel = null;
        }
        if(panel==null){
            panel = new Wtf.TabPanel({
                title: WtfGlobal.getLocaleText("mrp.workorder.entry.workcentre"),
                tabTip:WtfGlobal.getLocaleText("mrp.workorder.entry.workcentre"),
                id:'mainWorkcentrePanel',
                closable:true,
                isClosable:false,
                iconCls: 'workcentreicon',
                buttonAlign: 'right',
                layoutOnTabChange:true
            });
            Wtf.getCmp('as').add(panel);
//            panel.on('beforeclose', function (panel) {
//                if (panel.isClosable !== true) {
//                    Wtf.MessageBox.show({
//                        title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
//                        msg: WtfGlobal.getLocaleText("acc.msgbox.51"), //this.closeMsg,
//                        width: 500,
//                        buttons: Wtf.MessageBox.YESNO,
//                        icon: Wtf.MessageBox.QUESTION,
//                        fn: function (btn) {
//                            if (btn == "yes") {
//                                Wtf.getCmp('as').remove(panel);
//                            }
//                        },
//                        scope: this
//                    });
//
//                } else {
//                    Wtf.getCmp('as').remove(panel);
//                }
//                return false;
//            }, this);

            callWorkcentreCreationTab(isEdit, rec, isClone,reporttab);
            
        }
    Wtf.getCmp('as').setActiveTab(panel);

    Wtf.getCmp('mainWorkcentrePanel').doLayout();
}


function callWorkcentreCreationTab(isEdit, rec, isClone,reporttab){
    var panel =Wtf.getCmp("workcentreCreationTab")
    if(panel==null){
        if(isEdit){
            panel = "EditWorkCentre";
        }
        panel = new Wtf.WorkcentreCreationTabWindow({
            title:isEdit ? (isClone?WtfGlobal.getLocaleText("acc.mrp.field.CloneWorkCentre"):WtfGlobal.getLocaleText("acc.mrp.field.EditWorkCentre")) : WtfGlobal.getLocaleText("acc.mrp.field.CreateWorkCentre"),
            tabTip:isEdit ? (isClone?WtfGlobal.getLocaleText("acc.mrp.field.CloneWorkCentre"):WtfGlobal.getLocaleText("acc.mrp.field.EditWorkCentre")) : WtfGlobal.getLocaleText("acc.mrp.field.CreateWorkCentre"),
            isEdit:isEdit,
            isClone:isClone,
            id:'workcentreCreationTab',
            record:rec,
            modal: true,
            iconCls: 'workcentreicon',
            layout: 'fit',
            border:false,
            buttonAlign: 'right',
            workcentreReport:reporttab
        });
     
            Wtf.getCmp('mainWorkcentrePanel').add(panel);
        
    }

        Wtf.getCmp('mainWorkcentrePanel').setActiveTab(panel);
        Wtf.getCmp('workcentreCreationTab').doLayout();
    

}

function callMRPMachineMaster(obj) {
    var panel = Wtf.getCmp("mainmachinemastertab"+obj.isSubstituteMachine);
    if (panel == null) {
        panel = new Wtf.TabPanel({
            title: obj.isEdit?Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText('acc.machine.edit'), Wtf.TAB_TITLE_LENGTH):Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText('acc.machine.create'), Wtf.TAB_TITLE_LENGTH),
            tabTip: obj.isEdit?WtfGlobal.getLocaleText('acc.machine.edit'):WtfGlobal.getLocaleText('acc.machine.create'),
            id: 'mainmachinemastertab'+obj.isSubstituteMachine,
            border: false,
           iconCls: 'machineicon',
            closable: true,
            activeTab: 0
        });
        Wtf.getCmp('as').add(panel);
        callMachineMasterForm(obj);
//        callMachineMaintenanceDetailForm(obj);        
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function callMachineMasterForm(obj) {
    var machinePanel = Wtf.getCmp('machinemastercreateform'+obj.isSubstituteMachine);
    if (machinePanel == null) {
         machinePanel = new Wtf.MachineForm({
            id: 'machinemastercreateform'+obj.isSubstituteMachine,
            border: false,
            title: obj.isSubstituteMachine?WtfGlobal.getLocaleText('acc.machineMaster.addNewSubstitute'):WtfGlobal.getLocaleText('acc.machineMaster.addNewActive'),
            tabTip: obj.isSubstituteMachine?WtfGlobal.getLocaleText('acc.machineMaster.addNewSubstitute'):WtfGlobal.getLocaleText('acc.machineMaster.addNewActive'),
            layout: 'fit',
            closable: false,
            moduleid: 2,
            label: WtfGlobal.getLocaleText('acc.machine.create'),
            iconCls: 'machineicon',
            isSubstituteMachine : obj.isSubstituteMachine,
            isEdit : obj.isEdit,
            record:(obj.record!=undefined)?obj.record:""
        });
        Wtf.getCmp('mainmachinemastertab'+obj.isSubstituteMachine).add(machinePanel);
    }
    Wtf.getCmp('mainmachinemastertab'+obj.isSubstituteMachine).setActiveTab(machinePanel);
    Wtf.getCmp('mainmachinemastertab'+obj.isSubstituteMachine).doLayout();
}
//function callMachineMaintenanceDetailForm(obj) {
//    var machineMaintenancePanel = Wtf.getCmp('machinemaintenanceform');
//    if (machineMaintenancePanel == null) {
//         machineMaintenancePanel = new Wtf.MachineMaintenanceDetail({
//            id: 'machinemaintenanceform',
//            border: false,
//            title: WtfGlobal.getLocaleText('acc.machineMaintenance.MaintenanceDeatils'),
//            tabTip: WtfGlobal.getLocaleText('acc.machineMaintenance.MaintenanceDeatils'),
//            layout: 'fit',
//            closable: false,
//            moduleid: 2,
//            label: WtfGlobal.getLocaleText('acc.machine.create'),
//            iconCls: 'accountingbase invoicelist',
//            isEdit : obj.isEdit
//        });
//        Wtf.getCmp('mainmachinemastertab'+obj.isSubstituteMachine).add(machineMaintenancePanel);
//    }
//    machineMaintenancePanel.on("activate",function(){
//        machineMaintenancePanel.doLayout();
//    },this);
//    Wtf.getCmp('mainmachinemastertab'+obj.isSubstituteMachine).doLayout();
//}

function callMachineBreakDown() {
    var panel = Wtf.getCmp("machinebreakdowntab");
    if (panel == null) {
        panel = new Wtf.TabPanel({
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText('acc.machinebreakdown.tabTitle'), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText('acc.machinebreakdown.tabTitle'),
            id: 'machinebreakdowntab',
            border: false,
            iconCls: 'accountingbase balancesheet',
            closable: true,
            activeTab: 0
        });
        Wtf.getCmp('as').add(panel);
        callMachineBreakDownForm();
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function callMachineBreakDownForm() {
    var machinePanel = Wtf.getCmp('machinebreakdownform');
    if (machinePanel == null) {
         machinePanel = new Wtf.MachineBreakDown({
            id: 'machinebreakdownform',
            border: false,
            title: WtfGlobal.getLocaleText('Add Machine Breakdown'),
            tabTip:WtfGlobal.getLocaleText('Add Machine Breakdown'),
            layout: 'fit',
            closable: false,
            moduleid: 2,
            label: WtfGlobal.getLocaleText('Add Machine Breakdown'),
            iconCls: 'accountingbase invoicelist'
        })
        Wtf.getCmp('machinebreakdowntab').add(machinePanel);
    }
    Wtf.getCmp('machinebreakdowntab').setActiveTab(machinePanel);
    Wtf.getCmp('machinebreakdowntab').doLayout();
}
function callMachinManRatio() {
    var panel = Wtf.getCmp("machinemanratiotab");
    if (panel == null) {
        panel = new Wtf.TabPanel({
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText('acc.machineManReport.title'), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText('acc.machineManReport.title'),
            id: 'machinemanratiotab',
            border: false,
            iconCls: 'accountingbase balancesheet',
            closable: true,
            layoutOnTabChange:true,
            activeTab: 0
        });
        Wtf.getCmp('as').add(panel);
        callMachinManRatioForm();
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function callMachinManRatioForm() {
    var machinePanel = Wtf.getCmp('machinemanratio');
    if (machinePanel == null) {
         machinePanel = new Wtf.MachineManRatio({
            id: 'machinemanratio',
            border: false,
            title: WtfGlobal.getLocaleText('acc.machineManReport.tabTitle'),
            tabTip:WtfGlobal.getLocaleText('acc.machineManReport.tabTitle'),
            layout: 'fit',
            closable: false,
            iconCls: 'accountingbase invoicelist'
        })
        Wtf.getCmp('machinemanratiotab').add(machinePanel);
    }
    Wtf.getCmp('machinemanratiotab').setActiveTab(machinePanel);
    Wtf.getCmp('machinemanratiotab').doLayout();
}
function callLabourInformation(isEdit, rec) {
    winid = (isEdit) ? "editlabourinfotab" : "craetelabourinfotab";
    var labourinfopanel = Wtf.getCmp(winid);
    if (labourinfopanel == null) {
        labourinfopanel = new Wtf.account.LabourInformationPanel({
            title:isEdit? Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.labour.editLabour"), Wtf.TAB_TITLE_LENGTH): Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.labour.createlabour"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("acc.labour.createlabour"),
            id: winid,
            closable: true,
            border: false,
            modeName: 'autoloanrefnumber',
            layout: 'fit',
            isEdit: isEdit,
            moduleid: Wtf.labourMaster,
            isClone: false,
            record: rec,
            iconCls: 'labouricon'
        });
        Wtf.getCmp('as').add(labourinfopanel);
    }
    Wtf.getCmp('as').setActiveTab(labourinfopanel);
    Wtf.getCmp('as').doLayout();
}
function callForecastInfo(obj) {
    var isEdit = obj.isEdit;
    var isCopy = obj.isCopy;
    var rec = obj.rec;
    var winid = (isEdit) ? "editforecastinfotab" : isCopy ? "copyforecastinfotab" :"craeteforecastinfotab";
    var forecastinfopanel = Wtf.getCmp(winid);
    var title = (isEdit) ? Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("mrp.forecase.edit"), Wtf.TAB_TITLE_LENGTH) : isCopy ? Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("mrp.forecase.copy"), Wtf.TAB_TITLE_LENGTH) : Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("mrp.forecase.create"), Wtf.TAB_TITLE_LENGTH);
    if (forecastinfopanel == null) {
        forecastinfopanel = new Wtf.account.ForecastInformationPanel({
            title: title,
            tabTip: title,
            id: winid,
            closable: true,
            border: false,
            modeName: 'autoloanrefnumber',
            layout: 'fit',
            isEdit: isEdit,
            isCopy: isCopy,
            moduleid: Wtf.MRP_ForeCast_ModuleID,
            isClone: false,
            record: rec,
            iconCls: getButtonIconCls(Wtf.etype.product),
            iconCls: 'forecastingicon'
        });
        Wtf.getCmp('as').add(forecastinfopanel);
    }
    Wtf.getCmp('as').setActiveTab(forecastinfopanel);
    Wtf.getCmp('as').doLayout();
}
function callForecastDetails(searchStr, filterAppend) {
    var panel = Wtf.getCmp("mainForecastDetails");
    if (panel == null) {
        panel = new Wtf.TabPanel({
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.forecase.forecastadministration"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("acc.forecase.forecastadministration"),
            id: 'mainForecastDetails',
            closable: true,
            border: false,
            iconCls: 'forecastingicon',
            activeTab: 0
        });
        Wtf.getCmp('as').add(panel);
        callForecastList(searchStr, filterAppend);
        panel.activate(0);
    } else {
        Wtf.getCmp('as').setActiveTab(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}
function callForecastList(searchStr, filterAppend,wcid) {
    var winid = (wcid == undefined) ? 'forecastList' : 'forecastList'+wcid;
    var forecastListpanel = Wtf.getCmp(winid);
    if (forecastListpanel == null) {
        forecastListpanel = new Wtf.account.forecastList({
            id: winid,
            border: false,
            layout: 'fit',
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.forecase.forecastlist"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("acc.forecase.forecastlist"), 
            iconCls: 'forecastingicon',
            searchJson:searchStr,
            filterAppend:filterAppend,
            closable:false
        });
       Wtf.getCmp('mainForecastDetails').add(forecastListpanel);
    }
         Wtf.getCmp("mainForecastDetails").setActiveTab(forecastListpanel);
    Wtf.getCmp("mainForecastDetails").doLayout();
}

function callForecastDetailList(record) {
    var winid ='forecastDetailList';
    var forecastDetailListpanel = Wtf.getCmp(winid);
    if (forecastDetailListpanel == null) {
        forecastDetailListpanel = new Wtf.account.forecastDetailList({
            id: winid,
            border: false,
            layout: 'fit',
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.monthlyForecast"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("acc.monthlyForecast"), 
            iconCls: 'forecastingicon',
            closable:true,
            record:record
        });
       Wtf.getCmp('mainForecastDetails').add(forecastDetailListpanel);
    }
         Wtf.getCmp("mainForecastDetails").setActiveTab(forecastDetailListpanel);
    Wtf.getCmp("mainForecastDetails").doLayout();
}
function callResourceCost(isMachineCost) {
    /* 
     Configs for Labour Cost tab
     */
    var resourcecostpanel = Wtf.getCmp("resourcecost_labour");
    var component = 'mainLabourDetails';
    var title = Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.labour.labourCost"), Wtf.TAB_TITLE_LENGTH);
    var tabTip = WtfGlobal.getLocaleText("acc.labour.labourCost");
    var id = "resourcecost_labour";
    var iconCls = 'accountingbase vendor';
        
    if(isMachineCost){
        /*
         Configs for Machine Cost tab
        */
        resourcecostpanel = Wtf.getCmp("resourcecost_machine");
        component = 'mainmachinemasterlist' ;
        title = Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.machine.machineCost"), Wtf.TAB_TITLE_LENGTH);
        tabTip = WtfGlobal.getLocaleText("acc.machine.machineCost");
        id = "resourcecost_machine";
        iconCls = 'machineicon';
    }
    
    if (resourcecostpanel == null) {
        resourcecostpanel = new Wtf.account.resourceCost({
            title: title,
            tabTip: tabTip,
            id: id,
            closable: false,
            border: false,
            layout: 'fit',
            isClone: false,
            isMachineCost:isMachineCost,
            iconCls: iconCls
        });
        
        Wtf.getCmp(component).add(resourcecostpanel);
    }
    Wtf.getCmp(component).setActiveTab(resourcecostpanel);
    Wtf.getCmp(component).doLayout();
}
function callAssignTaskList() {
    var assigntaskpanel = Wtf.getCmp("assigntask");
    if (assigntaskpanel == null) {
        assigntaskpanel = new Wtf.account.assignTaskList({
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.labour.resourceanalysis"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("acc.labour.resourceanalysis"),
            id: "assigntask",
            closable: true,
            border: false,
            layout: 'fit',
            isClone: false,
            iconCls: getButtonIconCls(Wtf.etype.user),
            iconCls: 'accountingbase vendor'
        });
        Wtf.getCmp('as').add(assigntaskpanel);
    }
    Wtf.getCmp('as').setActiveTab(assigntaskpanel);
    Wtf.getCmp('as').doLayout();
}

function jobOrderReportTab(isReport,searchStr,filterAppend) {
    var ID = isReport ? 'joborderreporttabReport' : 'joborderreporttabEntry';
    var panel = Wtf.getCmp(ID);
    if (panel == null) {
        panel = new Wtf.jobOrderReport({
            id: ID,
            border: false,
            moduleId: Wtf.MRP_Job_Work_ModuleID,
            layout: 'fit',
            closable: true,
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("mrp.jobworkmodule.jobworkreport.title"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("mrp.jobworkmodule.jobworkreport.title"),
            iconCls: getButtonIconCls(Wtf.etype.jobwork),
            isReport:isReport
        });
        Wtf.getCmp('as').add(panel);
    }

    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    showAdvanceSearch(panel,searchStr, filterAppend);
}
function JObWorkOut(){
   var winid="JobWorkorder";
   callPurchaseOrder(false,null,winid,false,true);
}
function JObWorkIn(){
   var winid="JobWorkInvoice";
   callGoodsReceipt(false,null,winid,undefined,undefined,false,false,true);
}
function reajectedItemListInMRP() {
    var panel = Wtf.getCmp("rejecteditemlisttab");
    if (panel == null) {
        panel = new Wtf.TabPanel({
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("mrp.rejecteditems.report.title"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("mrp.rejecteditems.report.title"),
            id: 'rejecteditemlisttab',
            border: false,
            iconCls: 'workcentreicon',
            closable: true,
            layoutOnTabChange:true,
            activeTab: 0
        });
        Wtf.getCmp('as').add(panel);
        reajectedItemList();
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}
function reajectedItemList() {
    var ID =  'reajectedItemListInMRP' ;
    var panel = Wtf.getCmp(ID);
    if (panel == null) {
        panel = new Wtf.RejectedItemListReport({
            id: ID,
            border: false,
            moduleId: Wtf.Acc_Product_Master_ModuleId,
            layout: 'fit',
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("mrp.rejecteditems.report.title"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("mrp.rejecteditems.report.title"),
            iconCls: 'workcentreicon'
        });
        Wtf.getCmp('rejecteditemlisttab').add(panel);
    }
    Wtf.getCmp('rejecteditemlisttab').setActiveTab(panel);
    Wtf.getCmp('rejecteditemlisttab').doLayout();
}
function MRPQualityCOntrolReport() {
    var panel = Wtf.getCmp("qualitycontrollisttab");
    if (panel == null) {
        panel = new Wtf.TabPanel({
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("mrp.qualitycontrolreport.registerreport.tab.title"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("mrp.qualitycontrolreport.registerreport.tab.title"),
            id: 'qualitycontrollisttab',
            border: false,
            iconCls: 'accountingbase invoicelist',
            closable: true,
            layoutOnTabChange:true,
            activeTab: 0
        });
        Wtf.getCmp('as').add(panel);
        MRPQualityControlReportList();
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function MRPQualityControlReportList() {
    var ID =  'qualitycontrollistInMRP' ;
    var panel = Wtf.getCmp(ID);
    if (panel == null) {
        panel = new Wtf.QualityControlParametrsListReport({
            id: ID,
            border: false,
            layout: 'fit',
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("mrp.qualitycontrolreport.registerreport.tab.title"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("mrp.qualitycontrolreport.registerreport.tab.title"),
             iconCls: 'accountingbase invoicelist'
        });
        Wtf.getCmp('qualitycontrollisttab').add(panel);
    }
    Wtf.getCmp('qualitycontrollisttab').setActiveTab(panel);
    Wtf.getCmp('qualitycontrollisttab').doLayout();
}
function callWOShortfallReport() {
    var panel = Wtf.getCmp("woshortfallproducttab");
    if (panel == null) {
        panel = new Wtf.TabPanel({
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("mrp.woshortfallproduct.registerreport.tab.title"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("mrp.woshortfallproduct.registerreport.tab.title"),
            id: 'woshortfallproducttab',
            border: false,
            iconCls: 'accountingbase invoicelist',
            closable: true,
            layoutOnTabChange:true,
            activeTab: 0
        });
        Wtf.getCmp('as').add(panel);
        callWOShortfallTab();
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function callWOShortfallTab() {
    var ID =  'woshortfallproductinnertab' ;
    var panel = Wtf.getCmp(ID);
    if (panel == null) {
        panel = new Wtf.WOShortfallReport({
            id: ID,
            border: false,
            layout: 'fit',
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("mrp.woshortfallproduct.registerreport.tab.title"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("mrp.woshortfallproduct.registerreport.tab.title"),
             iconCls: 'accountingbase invoicelist'
        });
        Wtf.getCmp('woshortfallproducttab').add(panel);
    }
    Wtf.getCmp('woshortfallproducttab').setActiveTab(panel);
    Wtf.getCmp('woshortfallproducttab').doLayout();
}

function workcentreList(isReport) {
    var ID = isReport ? 'workcentrelisttabReport' : 'workcentrelisttabEntry';
    var panel = Wtf.getCmp(ID);
    if (panel == null) {
        panel = new Wtf.workCentreList({
            id: ID,
            border: false,
            moduleId: Wtf.Acc_Product_Master_ModuleId,
            layout: 'fit',
            closable: true,
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("Work Centre")+" Report", Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("Work Centre")+" Report",
            iconCls: getButtonIconCls(Wtf.etype.product),
            isReport:isReport
        });
        Wtf.getCmp('as').add(panel);
    }

    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}


// Function to call work order entry form
//function callMRPWorkOrderWindow(winid) {
//   winid=(winid==null?"mrpWorkOrderEntrywin":winid);
//   var panel = Wtf.getCmp(winid);
//   if(panel == null){
//       panel=new Wtf.account.WorkOrderTab({
//           title:"Create Work Order",
//           id:"mrpWorkOrderEntrytab",
//           border : false,
//           closable:true
//       });
//       Wtf.getCmp('as').add(panel);
//   }
//   
//   Wtf.getCmp('as').setActiveTab(panel);
//   Wtf.getCmp('as').doLayout();
//}


function calllJobWorkEntryMasterForm(winid,isEdit,record) {
   winid=(winid==undefined?"jobOrderMastersForm":winid);
   var panel = Wtf.getCmp(winid);
   if(panel == undefined){
       panel = new Wtf.account.JobOrderEntryForm({
            title:isEdit ?WtfGlobal.getLocaleText("acc.product.edit")+' ' +WtfGlobal.getLocaleText("acc.field.jobworkentryformworkorder.tab.title") : WtfGlobal.getLocaleText("acc.lp.createwo")+" "+WtfGlobal.getLocaleText("acc.field.jobworkentryformworkorder.tab.title"),
            tabTip: isEdit ?WtfGlobal.getLocaleText("acc.product.edit")+' ' +WtfGlobal.getLocaleText("acc.field.jobworkentryformworkorder.tab.title") : WtfGlobal.getLocaleText("acc.lp.createwo")+" "+WtfGlobal.getLocaleText("acc.field.jobworkentryformworkorder.tab.title"),
            id:winid,
            isEdit:isEdit,
            isNormalContract:false,
            isClone:false,
            iconCls: getButtonIconCls(Wtf.etype.jobwork),
            layout:'fit',
            closable:true,
            border:false,
            removegrid:true,
            record:record
        });
       Wtf.getCmp('as').add(panel);
   }
   
   Wtf.getCmp('as').setActiveTab(panel);
   Wtf.getCmp('as').doLayout();
}

function callAddressWindow(customerName, gridData, isEdit){
    var panel = Wtf.getCmp('shippingAddressWindow');
    if(panel==null){
        var shippingAddresWin = new Wtf.account.shippingAddressWindow({
            title:WtfGlobal.getLocaleText("acc.mastercontract.shippingaddress"),
            id:'shippingAddressWindow',
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            height:450,
            width:900,
            autoScroll:true,
            resizable:false,
            closable: true,
            gridData: gridData,
            isEdit: isEdit,
            modal: true,
            customerName:customerName
        });
        shippingAddresWin.show();
    }
}
function callRoutingMasterList(searchStr, filterAppend) {
    if (!WtfGlobal.EnableDisable(Wtf.UPerm.routingtemplate, Wtf.Perm.routingtemplate.viewrt)) {
        var panel = Wtf.getCmp("mainroutingmasterlist");
        if (panel == null) {
            panel = new Wtf.TabPanel({
                title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText('acc.mrp.field.routingmaster.title'), Wtf.TAB_TITLE_LENGTH),
                tabTip: WtfGlobal.getLocaleText('acc.mrp.field.routingmaster.title'),
                id: 'mainroutingmasterlist',
                border: false,
                iconCls: 'routingicon',
                closable: true,
                activeTab: 0
            });
            Wtf.getCmp('as').add(panel);
            callRoutingMasterListDetails();
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
        showAdvanceSearch(panel.items.map.rountingmasterlist, searchStr, filterAppend);
    } else {
         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText({key: "acc.accessrestrcition.permission.msg", params: [WtfGlobal.getLocaleText("acc.up.69")]})], 2);
    }
}
function callRoutingMasterListDetails() {
    var reportPanel = Wtf.getCmp('rountingmasterlist');
    if (reportPanel == null) {
        reportPanel = new Wtf.account.RoutingMasterList({
            id: 'rountingmasterlist',
            border: false,
            title: WtfGlobal.getLocaleText('acc.mrp.field.routingmaster.title'),
            tabTip: WtfGlobal.getLocaleText('acc.mrp.field.routingmaster.title'),
            layout: 'fit',
            label: WtfGlobal.getLocaleText('acc.mrp.field.routingmaster.title'),
            iconCls: 'routingicon'
        });
        Wtf.getCmp('mainroutingmasterlist').add(reportPanel);
    }
    Wtf.getCmp('mainroutingmasterlist').setActiveTab(reportPanel);
    Wtf.getCmp('mainroutingmasterlist').doLayout();

}
function callRoutingTemplateMaster(projectId,id,record,isEdit,alternateprojectId) {
    var winid = (id == undefined) ? 'createroutingmaster' : id;
    var reportPanel = Wtf.getCmp(winid);
    if (reportPanel == null) {
        reportPanel = new Wtf.account.RoutingTemplate({
            id: winid,
            border: false,
            projectId:projectId,
            alternateprojectId:alternateprojectId,
            title: isEdit ? Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText('acc.mrp.field.routingmaster.entryform.edit.title'), Wtf.TAB_TITLE_LENGTH):Wtf.util.Format.ellipsis((WtfGlobal.getLocaleText('acc.lp.creatert')+" "+WtfGlobal.getLocaleText('acc.up.69')), Wtf.TAB_TITLE_LENGTH),
            tabTip: isEdit ? WtfGlobal.getLocaleText('acc.mrp.field.routingmaster.entryform.edit.title'):(WtfGlobal.getLocaleText('acc.lp.creatert')+" "+WtfGlobal.getLocaleText('acc.up.69')),
            layout: 'fit',
            closable: true,
            label: WtfGlobal.getLocaleText('acc.mrp.field.routingmaster.title'),
              iconCls: 'routingicon',
            record:record,
            isEdit:isEdit
        });
        Wtf.getCmp('as').add(reportPanel);
    }
    Wtf.getCmp(winid).on('beforeclose', function() {
        deleteDirtyProject(projectId);
    },this); 
    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();
}

/**
 *  Template id 1. Rounting Master 2. Work Order
 * 
 */

function createProjectforMRP(templateid, projectId, reportTab) {
    this.loadMask = new Wtf.LoadMask(document.body, {
        msg: WtfGlobal.getLocaleText("acc.msgbox.50")
    });
    this.loadMask.show();
    Wtf.Ajax.requestEx({
        url: "ACCRoutingManagement/createProject.do",
        params: {
            isNewProject: projectId == undefined ? true : false,
            isMasterProject: templateid == Wtf.Project_TemplateId.ROUTINGMASTER ? true : false,
            projectId: projectId == undefined ? "" : projectId
        }
    }, this,
            function(responseObj) {
                if (responseObj.success == true && responseObj.projectId != undefined && responseObj.projectId != "") { //&& responseObj.isNewProject==true
                    this.loadMask.hide();
                    if (templateid == (Wtf.Project_TemplateId.ROUTINGMASTER * 1)) {
                        callRoutingTemplateMaster(responseObj.projectId, undefined);
                    } else {
                        callMRPWorkOrderWindow(null, false, undefined, reportTab, responseObj.projectId);
                    }
                } else {
                    this.loadMask.hide();
                    var msg = WtfGlobal.getLocaleText("acc.mrppm.failure");
                    if (responseObj) {
                        if (responseObj.msg != "") {
                            msg = responseObj.msg;
                        }
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                    }
                }
            }, function(responseObj) {
        this.loadMask.hide();
        var msg = WtfGlobal.getLocaleText("acc.mrppm.failure");
        if (responseObj) {
            if (responseObj.msg != "") {
                msg = responseObj.msg;
            }
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
        }
    }
    );
}
function deleteDirtyProject(projectId) {
    Wtf.Ajax.requestEx({
        url: "ACCRoutingManagement/deleteDirtyProject.do",
        params: {
            projectId: projectId == undefined ? "" : projectId
        }
    }, this,
            function(responseObj) {}, function(responseObj) {}
    );
}
function resourceUsagesExportWindow(resourceIdArray, type) {
    this.resourceUsageExportWin = new Wtf.Window({
        resizable: false,
        scope: this,
        modal:true,
        width: 300,
        height:162,
        loadMask: true,
        iconCls: 'iconwin',
        id: 'exportresusageswindow',
        title: WtfGlobal.getLocaleText('mrp.resource.export.options'),
        items:new Wtf.FormPanel({
            waitMsgTarget: true,
            border : false,
            labelWidth: 100,
            bodyStyle : 'margin:10px;font-size:10px;',
            defaults: {
                width: 160
            },
            items: [{
                xtype: 'datefield',
                fieldLabel: WtfGlobal.getLocaleText('acc.nee.FromDate'),
                id:'tempReportStartDateField',
                name:'startdate',
                typeAhead:false,
                allowBlank:false,
                value:new Date().getFirstDateOfMonth(),
                format:WtfGlobal.getDateFormat()
            },{
                xtype:'datefield',
                fieldLabel: WtfGlobal.getLocaleText('acc.nee.ToDate'),
                id:'tempReportEndDateField',
                name:'enddate',
                allowBlank:false,
                typeAhead:false,
                value:new Date().getLastDateOfMonth(),
                format:WtfGlobal.getDateFormat()
                            
            }
            ]
        }),
        buttons: [{
            text: WtfGlobal.getLocaleText('acc.common.export'),
            scope: this,
            handler: function(){
                var startdate = new Date(Wtf.getCmp('tempReportStartDateField').getValue());
                var enddate = new Date(Wtf.getCmp('tempReportEndDateField').getValue());
                if(startdate > enddate){
//                    msgBoxShow(322, 0);
                }else{
                    var otherParameter = "resourceids="+resourceIdArray.toString()+"&fromdate="+startdate.format('Y-m-d')+"&todate="+enddate.format('Y-m-d');
                    var url = (type =="Labour") ? "ACCLabourCMN/exportLabourAllocationReport.do" : "ACCMachineMaster/exportMachineAllocationReport.do"
                    Wtf.get('downloadframe').dom.src = url+"?"+otherParameter; 
                    this.resourceUsageExportWin.close();
                }
            }
        },{
            text: WtfGlobal.getLocaleText('acc.common.cancelBtn'),
            scope: this,
            handler: function(){
                this.resourceUsageExportWin.close();
            }
        }]
    });
    this.resourceUsageExportWin.show(); 
}

function createRCFormAndWindow(workorderid,isEdit,isOpenfromSerialNoWin,scope) {
    this.isEdit=isEdit;
    this.workorderid=workorderid;
    this.sequenceFormatStoreRec = new Wtf.data.Record.create([
        {
            name: 'id'
        },
        {
            name: 'value'
        },
        {
            name: 'oldflag'
        }
    ]);
    this.sequenceFormatStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            totalProperty: 'count',
            root: "data"
        }, this.sequenceFormatStoreRec),
        url: "ACCCompanyPref/getSequenceFormatStore.do",
        baseParams: {
            mode: "autoroutecode",
            isEdit: this.isEdit
        }
    });
    this.sequenceFormatStore.load();
    this.sequenceFormatStore.on('load', this.setNextNumber, this);
    this.routeCode = new Wtf.form.TextField({
        fieldLabel: WtfGlobal.getLocaleText("acc.mrp.routingcode.Id"),
        name: 'routecode',
//        id: "routecode" + this.id,
//        anchor: '75%',
        maxLength: 50,
        scope: this,
        allowBlank: true,
        width: 150
    });
    this.sequenceFormatCombobox = new Wtf.form.ComboBox({
        triggerAction: 'all',
        mode: 'local',
        fieldLabel: WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
        valueField: 'id',
        displayField: 'value',
        store: this.sequenceFormatStore,
        disabled: (this.isEdit ? true : false),
//        anchor: '75%',
        typeAhead: true,
        forceSelection: true,
        width: 150,
        allowBlank: true,
        name: 'sequenceformat',
        hiddenName: 'sequenceformat',
        listeners: {
            'select': {
                fn: this.getNextSequenceNumber,
                scope: this
            }
        }
    });
    
     this.routingCodeFieldset=new Wtf.form.FieldSet({
//            id: 'exciseApplicableFieldSet',
            xtype: 'fieldset',
            title: "Create Routing Code : ",  //WtfGlobal.getLocaleText("acc.field.india.isexciseavailable"),
            checkboxToggle: true,
            collapsed: true,
            autoHeight:true,
            autoWidth:true,
            checkboxName: 'routingCodeCreate',
            //hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            items:[this.sequenceFormatCombobox, this.routeCode]
        });
        this.routingCodeFieldset.on('collapse',function(){
            this.sequenceFormatCombobox.allowBlank=true;
            this.routeCode.allowBlank=true;
        },this);
        this.routingCodeFieldset.on('expand',function(){
            this.sequenceFormatCombobox.allowBlank=false;
            this.routeCode.allowBlank=false;
            
        },this);
    
    
    this.routingcodeform = new Wtf.form.FormPanel({
        url: 'ACCWorkOrderCMN/saveRoutingCode.do',
        region: 'center',
        autoScroll: true,
        bodyStyle: "background: transparent;",
        border: false,
        items: [this.routingCodeFieldset]
    });
    
    if(isOpenfromSerialNoWin == false){
        this.routingcodeform.style= "background: transparent;padding-left: 35px;padding-top: 20px;padding-right: 30px;";
    }
    
    if(isOpenfromSerialNoWin != undefined && isOpenfromSerialNoWin == false){
        
            this.saveRCBtn = new Wtf.Button({
                text: WtfGlobal.getLocaleText("acc.mrp.routingcode.save"),
                scope: this,
                handler: function () {
                    if(this.routingcodeform.getForm().isValid()){
                        this.saveRCBtn.disable();
                        this.routingcodeform.getForm().submit({
                            scope: this,
                            params: {
                                workorderid: this.workorderid
                            },
                            success: function(result, action) {
                                this.saveRCBtn.enable();
                                var resultObj = eval('(' + action.response.responseText + ')');
                                if (resultObj.data.success) {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), resultObj.data.msg], 0);
                                    this.routingcodeWin.close();

                                } else {
                                    if (resultObj.data.msg)
                                        var msg = resultObj.data.msg;
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                                }
                            },
                            failure: function(frm, action) {
                                this.saveRCBtn.enable();
                                var msg = WtfGlobal.getLocaleText("acc.common.msg1");
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                            }
                        });
                    }else{
                        WtfComMsgBox(2, 2);
                    }
                
                }
            });
            this.cancleBtn = new Wtf.Button({
                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                scope: this,
                handler: function() {
                    this.routingcodeWin.close();
                }
            });
            var title = WtfGlobal.getLocaleText("acc.mrp.routingcode.Rc");
            var msg = WtfGlobal.getLocaleText("acc.mrp.routingcode.createRc");
            var isgrid = false;
            this.routingcodeWin = new Wtf.Window({
                title: WtfGlobal.getLocaleText("acc.mrp.routingcode.createRc"),
                closable: true,
                modal: true,
                iconCls: getButtonIconCls(Wtf.etype.deskera),
                width: 500,
                height: 330,
                autoScroll: true,
                resizable: false,
                layout: 'border',
                buttonAlign: 'right',
                renderTo: document.body,
                items: [{
                        region: 'north',
                        height: 75,
                        border: false,
                        bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                        html: getTopHtml(title, msg, "../../images/accounting_image/price-list.gif", isgrid)
                    }, {
                        region: 'center',
                        border: false,
                        bodyStyle: 'background:#f1f1f1;font-size:10px;',
                        autoScroll: true,
                        items: this.routingcodeform
                    }],
                buttons: [this.saveRCBtn, this.cancleBtn]
            });
            this.routingcodeWin.show();
    }else{
        scope.routingcodeform=this.routingcodeform;
    }
 }
 
function getNextSequenceNumber(a, val) {
    if (!(a.getValue() == "NA")) {
        var rec = WtfGlobal.searchRecord(this.sequenceFormatStore, a.getValue(), 'id');
        var oldflag = rec != null ? rec.get('oldflag') : true;
        Wtf.Ajax.requestEx({
            url: "ACCCompanyPref/getNextAutoNumber.do",
            params: {
                from: Wtf.MRP_Route_Code_ModuleID,
                sequenceformat: a.getValue(),
                oldflag: oldflag
            }
        }, this, function(resp) {
            if (resp.data == "NA") {
                this.routeCode.reset();
                this.routeCode.enable();
            } else {
                this.routeCode.setValue(resp.data);
                this.routeCode.disable();
            }
        });
    } else {
        this.routeCode.reset();
        this.routeCode.enable();
    }
 }
 
 function setNextNumber() {
    if (this.sequenceFormatStore.getCount() > 0) {
        var count = this.sequenceFormatStore.getCount();
        for (var i = 0; i < count; i++) {
            var seqRec = this.sequenceFormatStore.getAt(i)
            if (seqRec.json.isdefaultformat == "Yes") {
                this.sequenceFormatCombobox.setValue(seqRec.data.id)
                break;
            }
        }
        if(this.sequenceFormatCombobox.getValue()!=""){
            this.getNextSequenceNumber(this.sequenceFormatCombobox);
        } else{
             this.routeCode.setValue("");
             this.routeCode.disable();
        }
    }
}

//**********************Graphs to be opened from  Side panel Node of manufacturing

function componentAvailabilityProductWiseGarph() {
    var ID =  'componentAvailabilityProductWiseGarphwew' ;
    var panel = Wtf.getCmp(ID);
    if (panel == null) {
        panel = new Wtf.productWiseComponentAvailability({
            id: ID,
            border: false,
            moduleId: Wtf.Acc_Product_Master_ModuleId,
            layout: 'fit',
             closable: true,
            title: "Product Wise Component Availability",
            tabTip: "Product Wise Component Availability",
            iconCls: 'accountingbase chart'
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function componentAvailabilityWorkOrderwiseGarph() {
    var ID =  'componentAvailabilityWorkOrderwiseGarphfrer' ;
    var panel = Wtf.getCmp(ID);
    if (panel == null) {
        panel = new Wtf.workOrderWiseComponentAvailability({
            id: ID,
            border: false,
            moduleId: Wtf.Acc_Product_Master_ModuleId,
            layout: 'fit',
             closable: true,
            title: "Work Order Wise Component Availability",
            tabTip: "Work Order Wise Component Availability",
            iconCls: 'accountingbase chart'
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}


function workOrderTasksStatusReportsideviewGarph() {
    var ID =  'workOrderTasksStatusReportsideviewGarphfgf' ;
    var panel = Wtf.getCmp(ID);
    if (panel == null) {
        panel = new Wtf.workorderTasksStatusReportNavigation({
            id: ID,
            border: false,
            moduleId: Wtf.Acc_Product_Master_ModuleId,
            layout: 'fit',
             closable: true,
            title: "Work Order Task Status Report",
            tabTip: "Work Order Task Status Report",
            iconCls: 'accountingbase chart'
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function workOrderStatusReportsideviewGarph() {
    var ID =  'workOrderStatusReportsideviewGarphdfdg' ;
    var panel = Wtf.getCmp(ID);
    if (panel == null) {
        panel = new Wtf.workorderstatusReportgraphviewmod({
            id: ID,
            border: false,
            moduleId: Wtf.Acc_Product_Master_ModuleId,
            layout: 'fit',
             closable: true,
            title: "Work Order Status Report",
            tabTip: "Work Order Status Report",
            iconCls: 'accountingbase chart'
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}


function vendorInfoReportGraph() {
    var ID =  'vendorInfoReportGraphdfgdfg' ;
    var panel = Wtf.getCmp(ID);
    if (panel == null) {
        panel = new Wtf.vendorInfoReportGraph({
            id: ID,
            border: false,
            moduleId: Wtf.Acc_Product_Master_ModuleId,
            layout: 'fit',
             closable: true,
            title: "Vendor Information Report",
            tabTip: "Vendor Information Report",
            iconCls: 'accountingbase chart'
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function mrpLabourCosting(){
    var ID =  'mrplaoburcostingreport5645_*sddsjgsf454';
    var panel = Wtf.getCmp(ID);
    if (panel == null) {
        panel = new Wtf.labourCostingReport({
            id: ID,
            border: false,
            layout: 'fit',
            closable: true,
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("mrp.labour.costing.reporttitle"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("mrp.labour.costing.reporttitle"),
            iconCls: getButtonIconCls(Wtf.etype.mrpcosting),
            isMRPProfitablityReport:false
        });
        Wtf.getCmp('as').add(panel);
    }

    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    
}

function mrpWorkOrderProfitabilityReport(){
    var ID =  'mrpmrpWorkOrderProfitabilityReportreport5645bdfhdgfhgfhgfhgf';
    var panel = Wtf.getCmp(ID);
    if (panel == null) {
        panel = new Wtf.labourCostingReport({
            id: ID,
            border: false,
            layout: 'fit',
            closable: true,
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.mrp.workorderprofilibilityreport"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("acc.mrp.workorderprofilibilityreport"),
            iconCls: getButtonIconCls(Wtf.etype.mrpcosting),
            isMRPProfitablityReport:true
        });
        Wtf.getCmp('as').add(panel);
    }

    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    
}

