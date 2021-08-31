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

Wtf.LoanManagement = function(config){
    this.nodeHash = {};
    Wtf.LoanManagement.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.LoanManagement, Wtf.tree.TreePanel, {
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
        Wtf.LoanManagement.superclass.initComponent.call(this);
        treeObj = this;
        
        function _openFunction(node){
            switch (node.id) {
                case "Loan_0":
                    manageEligibilityRules(false, null, null);
                    break;
                case "Loan_1":
                    callLoanDisbursement(false,null,null);
                    break;
                case "Loan_9":
                    CallDisbursementReport();
                    break;

            }
        }
        
        function _createNode(nodeText, nodeID, canDrag, isLeaf, nodeIcon){
            var treeNode= new Wtf.tree.TreeNode({
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
        var arrayList = [];
        var arrayListEntry = [];
        var arrayListReport = [];
        
        var entryNode=_createNode('Entry', '1', false, false, 'images/chart-of-accounts.gif');
        var reportNode=_createNode('Reports', '3', false, false, 'images/Account_Payable/Reports.png');
        //arrayList.push(reportNode);
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.loanDisbursement, Wtf.Perm.loanDisbursement.manageEligibilityRules)) { 
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.loan.eligibilityrule.create"), 'Loan_0', false, true, 'images/Masters/Profit-&-Loss-layout.png'));
         }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.loanDisbursement, Wtf.Perm.loanDisbursement.disbursement)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.loan.disbursement"), 'Loan_1', false, true, 'images/Account_Payable/Indent-Requisition.png'));
           }
               
       if(!WtfGlobal.EnableDisable(Wtf.UPerm.loanDisbursement, Wtf.Perm.loanDisbursement.disbursementReports)) { 
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.loan.disbursement.report"), 'Loan_9', false, true, 'images/Account_Payable/Payment-Register.png'));
       }
        //       
        if(arrayListEntry.length!=0){
          arrayList.push(entryNode);  
        }
        if(arrayListReport.length!=0){
         arrayList.push(reportNode);    
        }
        this.setRootNode(root1);
        entryNode.appendChild(arrayListEntry);
        reportNode.appendChild(arrayListReport);
        root1.appendChild(arrayList);
    }
});


