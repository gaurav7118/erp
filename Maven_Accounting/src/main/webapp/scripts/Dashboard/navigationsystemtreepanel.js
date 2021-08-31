Wtf.SystemTree = function(config){
    this.nodeHash = {};
    var tree;
    var outbox;
    var drafts;
    var deleteditems;
    var starreditems;
    var temptreenode;
    var folders;
    var nodeid;
    var treeObj;
    var composeMail;
    
    Wtf.SystemTree.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.SystemTree, Wtf.tree.TreePanel, {
    autoWidth: true,
    autoHeight: true,
    rootVisible: false,
//    id: 'folderview',
    border:false,
    autoScroll: true,
    animate: Wtf.enableFx,
    enableDD: false,
    hlDrop: Wtf.enableFx,
    
    
    initComponent: function(){
        Wtf.SystemTree.superclass.initComponent.call(this);
        treeObj = this;
        function _openFunction(node){
            switch (node.id) {
                case "11":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "12":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "13":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;  
                case "14":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;     
                case "21":
                    callAccountPref();
                    break;     
                case "22":
                    loadAdminPage(1);
                    break;     
                case "23":
                    showPersnProfile1();
                    break; 
                case "24":
                    showPersnProfile();
                    break;      
                case "25":
                    showActiveDateRange();
                    break;      
                case "31":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;      
                case "32":
                    setNotificationRules();
                    break;      
                case "33":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;      
                case "34":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;      
                case "35":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;      
                case "36":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;      
                case "37":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;      
                case "38":
                    takeTour();
                    break;      
                case "39":
                    callAuditTrail();
                    break;      
                case "40":
                    callImportFilesLog();
                    break; 
                case "41":  // Accounting Period 
                    getAccountingandtaxperoid();
                    break;   
                case "42":  // Tax Period 
                    getAccountingandtaxperiod()
                    break;
                case "79":
                    openDashboardManager();
                    break;      
            }
        }
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
                _openFunction(node);
            },this);
            return treeNode;
        }

        var root1 = new Wtf.tree.AsyncTreeNode({
            text: '',
            expanded: true
        });           
        var arrayList = new Array();
        var arrayListCompany = new Array();
        var arrayListpref = new Array();
        var arrayListConfig = new Array();
        var systemArray = new Array();
        arrayListCompany.push(_createNode(WtfGlobal.getLocaleText("acc.field.New"), '11', false, true, 'images/System/new-comapny.png'));
        arrayListCompany.push(_createNode(WtfGlobal.getLocaleText("acc.lp.edit"), '12', false, true, 'images/System/modify-comapny.png'));
        arrayListCompany.push(_createNode(WtfGlobal.getLocaleText("acc.lp.removeinvoice"), '13', false, true, 'images/System/delete-company.png'));
        arrayListCompany.push(_createNode(WtfGlobal.getLocaleText("acc.field.Open"), '14', false, true, 'images/System/open-comapny.png'));
        
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.accpref, Wtf.Perm.accpref.view)) {
            arrayListpref.push(_createNode(WtfGlobal.getLocaleText("acc.field.SystemControls"), '21', false, true, 'images/System/system-control.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.useradmin, Wtf.Perm.useradmin.view)) {
            arrayListpref.push(_createNode(WtfGlobal.getLocaleText("acc.field.UserMaintenance"), '22', false, true, 'images/System/user-maintenance.png'));
        }
        if(!Wtf.isSelfService) {
            arrayListpref.push(_createNode(WtfGlobal.getLocaleText("acc.changePass.tabTitle"), '23', false, true, 'images/System/change-password.png'));
        }
        arrayListpref.push(_createNode(WtfGlobal.getLocaleText("acc.field.E-mailProfileSettings"), '24', false, true, 'images/System/email-profile-settings.png'));
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.view)) {
            arrayListpref.push(_createNode(WtfGlobal.getLocaleText("acc.field.ActiveDateRange"), '25', false, true, 'images/System/active-date-range.png'));
        }
        
        if (Wtf.UserReporRole.URole.roleid == Wtf.ADMIN_ROLE_ID) {
            arrayListpref.push(_createNode(WtfGlobal.getLocaleText("acc.accountingperiodtab.northpanel.accountingperiod.title"), '41', false, true, 'images/Masters/accounting-period.png'));
            arrayListpref.push(_createNode(WtfGlobal.getLocaleText("acc.common.taxperiod"), '42', false, true, 'images/Masters/tax-period.png'));
        }
        
        if(Wtf.viewDashboard == 2){
            arrayListpref.push(_createNode("Dashboard Manager", '79', false, true, 'images/graphicalDashboard/dashboard-manager.png'));
        }
        
//        arrayListConfig.push(_createNode('Security Rights Grouping', '31', false, true, 'images/System/security-rights-grouping.png'));
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.view)) {
            arrayListConfig.push(_createNode(WtfGlobal.getLocaleText("acc.field.AlertConfiguration"), '32', false, true, 'images/System/alert-configuration.png'));
        }
//        arrayListConfig.push(_createNode('Password Configuration', '33', false, true, 'images/System/password-configuration.png'));
//        arrayListConfig.push(_createNode('Maker Checker System', '34', false, true, 'images/System/maker-checker-system.png'));
//        arrayListConfig.push(_createNode('E-mail Server Configuration', '35', false, true, 'images/System/email-server-conf.png'));
//        arrayListConfig.push(_createNode('Custom Options', '36', false, true, 'images/System/custom-option.png'));
//        arrayListConfig.push(_createNode('Client Configuration', '37', false, true, 'images/System/client-conf.png'));
        arrayListConfig.push(_createNode(WtfGlobal.getLocaleText("acc.field.OfflineHelp"), '38', false, true, 'images/System/offline-help.png'));
        
        var companyNode=_createNode(WtfGlobal.getLocaleText("acc.repeatedJE.Gridcol2"), '1', false, false, 'images/System/company.png');
        companyNode.appendChild(arrayListCompany);
        var prefNode=_createNode(WtfGlobal.getLocaleText("acc.field.Preferences"), '2', false, false, 'images/System/preferences.png');
        prefNode.appendChild(arrayListpref);
        var configNode=_createNode(WtfGlobal.getLocaleText("acc.field.Configuration"), '3', false, false, 'images/System/configuration.png');
        configNode.appendChild(arrayListConfig);
        this.setRootNode(root1);
        
        systemArray.push(prefNode);
        systemArray.push(configNode);
        
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.audittrail, Wtf.Perm.audittrail.view)){
            var auditTrailNode=_createNode(WtfGlobal.getLocaleText("acc.dashboard.auditTrail"), '39', false, true, 'images/System/audit-trail.png');
            systemArray.push(auditTrailNode);
        }
        
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.importlog, Wtf.Perm.importlog.view)){
            var importLogNode=_createNode(WtfGlobal.getLocaleText("acc.dashboard.importLog"), '40', false, true, 'images/System/import-log.png');
            systemArray.push(importLogNode);
        }
        
        root1.appendChild(systemArray);
    }
});