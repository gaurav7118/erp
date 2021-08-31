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

function GSTForm5eSubmissionDetails() {
    var GSTForm5eSubmissionDetailsTab = Wtf.getCmp('GSTForm5eSubmissionDetailsId');
    if (GSTForm5eSubmissionDetailsTab == null) {
        var GSTForm5eSubmissionDetailsTabReport = new Wtf.account.GSTForm5eSubmissionDetails({
            id: "GSTForm5eSubmissionDetailsId",
            border: false,
            closable: true,
            iconCls: 'accountingbase agedpayable',
            title: "GST Form 5 e-Submission Details",
            tabTip: "GST Form 5 e-Submission Details"
        });
        Wtf.getCmp('as').add(GSTForm5eSubmissionDetailsTabReport);
        Wtf.getCmp('as').setActiveTab(GSTForm5eSubmissionDetailsTabReport);
    } else {
        Wtf.getCmp('as').setActiveTab(GSTForm5eSubmissionDetailsTab);
    }
    Wtf.getCmp('as').doLayout();
}
Wtf.account.GSTForm5eSubmissionDetails = function (config) {
    this.GSTForm5eSubmissionDetailsRec = new Wtf.data.Record.create([
        {name: 'fromdate'},
        {name: 'todate'},
        {name: 'status'},
        {name: 'submissiondate'},
        {name: 'responsemessages'},
        {name: 'messageCode'},
        {name: 'responsedetails'}
    ]);
    this.GSTForm5eSubmissionDetailsStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: 'count'
        }, this.GSTForm5eSubmissionDetailsRec),
        url: "ACCReports/gstForm5eSubmissionDetails.do"
    });
    this.GSTForm5eSubmissionDetailsStore.load();

    this.rowNo = new Wtf.grid.RowNumberer({width: 35});
    this.startDate = new Wtf.ExDateFieldQtip({
        fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), //'From',
        name: 'stdate',
        format: WtfGlobal.getOnlyDateFormat(),
        value: this.getDates(true)
    });
    this.endDate = new Wtf.ExDateFieldQtip({
        fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), //'To',
        format: WtfGlobal.getOnlyDateFormat(),
        name: 'enddate',
        value: this.getDates(false)
    });
    this.fetch = new Wtf.Toolbar.Button({
        scope: this,
        text: WtfGlobal.getLocaleText("acc.common.fetch"), //"Fetch",
        iconCls: "accountingbase fetch",
        handler: function () {
            this.GSTForm5eSubmissionDetailsStore.load();
        }
    });

    var btnArr = [];
    btnArr.push(WtfGlobal.getLocaleText("acc.common.from"), this.startDate, '-', WtfGlobal.getLocaleText("acc.common.to"), this.endDate, '-');
    btnArr.push(this.fetch);
    // Grid Configuration
    this.grid = new Wtf.grid.GridPanel({
        stripeRows: true,
        store: this.GSTForm5eSubmissionDetailsStore,
//        cm: this.gridcmodal,
        columns: [this.rowNo, {
                header: "From Date",
                dataIndex: 'fromdate',
                width: 180
            }, {
                header: "To Date",
                dataIndex: 'todate',
                width: 180
            }, {
                header: "Status",
                dataIndex: 'status',
                width: 180,
                renderer: function (val, matadata, agruments) {
                    if (agruments.data.status == "0") {
                        return '<font color ="blue"><b>' + "Authentication Pending" + '</b></font>';
                    } else if (agruments.data.status == "1") {
                        return '<font color ="green"><b>' + "Success" + '</b></font>';
                    } else if (agruments.data.status == "2") {
                        return '<font color ="red"><b>' + "Failure" + '</b></font>';
                    } else if (agruments.data.status == "3") {
                        return '<font color ="red"><b>' + "Aborted By User" + '</b></font>';
                    } else if (agruments.data.status == "4") {
                        return '<font color ="blue"><b>' + "Pending" + '</b></font>';
                    }
                }
            }, {
                header: "Submission Date",
                dataIndex: 'submissiondate',
                width: 180
            }, {
                header: "Messages Code",
                dataIndex: 'messageCode',
                width: 180
            }, {
                header: "Response Messages",
                dataIndex: 'responsemessages',
                width: 180
            }, {
                header: "Response Details",
                dataIndex: 'responsedetails',
                width: 180,
                renderer: function (value, matadata) {
                    var val = Wtf.util.Format.ellipsis(value, 100);
                    matadata.attr = 'style="white-space: normal;"';
                    return "<div  wtf:qtip=\"" + value + "\">" + val + "</div>";
                }
            }],
        border: false,
        id: "GSTForm5eSubmissionDetails",
        loadMask: true,
        tbar: btnArr,
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.GSTForm5eSubmissionDetailsStore,
            displayInfo: true,
            displayMsg: 'Displaying records {0} - {1} of {2}',
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id}),
        }),
        viewConfig: {
            forceFit: true,
            emptyText: '<div style="font-size:15px; text-align:center; color:#808080; font-weight:bold; margin-top:1%;">No Record Found</div>',
        }
    });
    this.grid.on("render", function () {
        this.grid.getView().applyEmptyText();
    }, this);

    Wtf.apply(this, {
        layout: 'border',
        border: false,
        scope: this,
        statementType: "GSTForm5",
        items: [{
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid]
            }],
    }, config);

    Wtf.account.GSTForm5eSubmissionDetails.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.GSTForm5eSubmissionDetails, Wtf.Panel, {
    onRender: function (config) {
        Wtf.account.GSTForm5eSubmissionDetails.superclass.onRender.call(this, config);
    },
    getDates: function (start) {
        var d = new Date();
        var monthDateStr = d.format('M d');
        if (Wtf.account.companyAccountPref.fyfrom)
            monthDateStr = Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd = new Date(monthDateStr + ', ' + d.getFullYear() + ' 12:00:00 AM');
        if (d < fd)
            fd = new Date(monthDateStr + ', ' + (d.getFullYear() - 1) + ' 12:00:00 AM');
        if (start)
            return fd;

        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    }
});


function gstForm5ComponentCall(params) {
    this.callFunction = new Wtf.account.GSTForm5eSubmissionFunction({
        params: params,
    });
}
Wtf.account.GSTForm5eSubmissionFunction = function (config) {
    this.id = config.params.id;
    this.searchJson = config.params.searchJson;
    this.filterConjuctionCrit = config.params.filterConjuctionCrit;
    this.objsearchComponent = config.params.objsearchComponent;
    this.startDate = config.params.startDate;
    this.endDate = config.params.endDate;
    this.gstStore = config.params.gstStore;
    var document = config.params.document;
    this.loadingMask = new Wtf.LoadMask(this.id, {
        msg: "Preparing Data..."
    });
    var searchJson;
    var advanceSearchJSON;
    var filterConjuctionCriteria;
    var multiEntityId;
    var multiEntityName;
    if (Wtf.account.companyAccountPref.isMultiEntity) {
        if (!Wtf.isEmpty(this.searchJson)) {
            advanceSearchJSON = JSON.parse(this.searchJson);
            if (advanceSearchJSON.root.length == 1 && advanceSearchJSON.root[0].columnheader == "Entity") {
                searchJson = this.searchJson;
                filterConjuctionCriteria = this.filterConjuctionCrit
                multiEntityId = this.objsearchComponent.advGrid.getSearchJSON().searchText;
                multiEntityName = advanceSearchJSON.root[0].combosearch;
            } else {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Please select only entity in advance search for e-submission"], 2);
                return;
            }
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Please select only entity in advance search for e-submission"], 2);
            return;
        }
    }
    /*
     * If user use Advance Search option, then eSubmission will not work.
     */
    if (!Wtf.isEmpty(this.searchJson)) {
        advanceSearchJSON = JSON.parse(this.searchJson);
        if (advanceSearchJSON.root.length > 0) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.gst.GSTeSubmissionValidationAdvanceSearch")], 2);
            return;
        }
    }
    if (Wtf.isEmpty(Wtf.account.companyAccountPref.taxNumber) || Wtf.account.companyAccountPref.taxNumber.length > 30) {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Tax Reference Number should not greater than 30 character"], 2);
        return;
    }
    var claimGSTrefundedTourist = "";
    var badDeptReliefClaims = "";
    var preRegistrationClaim = "";
    var nameOfDeclarant = document.getElementById('id0').value;  //name of Declarant
    var declarantId = document.getElementById('id1').value; //Designation Id
    var designation = document.getElementById('id2').value; //Designation
    var contactPerson = document.getElementById('id3').value; //Contact person
    var contactNo = document.getElementById('id4').value;//Contact No
    var contactPersonEmailid = document.getElementById('id5').value;//Contact No

    if (document.getElementsByName("declaration[10]").item(0).checked) {
        claimGSTrefundedTourist = true;
    } else if (document.getElementsByName("declaration[10]").item(1).checked) {
        claimGSTrefundedTourist = false;
    }
    if (document.getElementsByName("declaration[11]").item(0).checked) {
        badDeptReliefClaims = true;
    } else if (document.getElementsByName("declaration[11]").item(1).checked) {
        badDeptReliefClaims = false;
    }
    if (document.getElementsByName("declaration[12]").item(0).checked) {
        preRegistrationClaim = true;
    } else if (document.getElementsByName("declaration[12]").item(1).checked) {
        preRegistrationClaim = false;
    }
    /*
     *Defined GST Form 5 Box 7 claims checks in global variables
     */
    this.claimGSTrefundedTouristCheck = claimGSTrefundedTourist;
    this.badDeptReliefClaimsCheck = badDeptReliefClaims;
    this.preRegistrationClaimCheck = preRegistrationClaim;
    if (Wtf.isEmpty(claimGSTrefundedTourist) || Wtf.isEmpty(badDeptReliefClaims) || Wtf.isEmpty(preRegistrationClaim)) {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Please provide claims details of Box 7"], 2);
        return;
    }
    /*
     * Validation for GST Form 5 fields
     */
    if (!this.validateeSumbissionData()) {
        return;
    }

    this.claimGSTrefundedTouristAmount = new Wtf.form.NumberField({
        name: 'claimGSTrefundedTouristAmount',
        fieldLabel: 'Tourist Refund Amount',
        hidden: !claimGSTrefundedTourist,
        hideLabel: !claimGSTrefundedTourist,
        allowNegative: false,
        allowBlank: !claimGSTrefundedTourist,
        allowDecimals: true,
        decimalPrecision: 2,
        value: 0//Default
    });
    this.badDeptReliefClaimsAmount = new Wtf.form.NumberField({
        name: 'badDeptReliefClaimsAmount',
        fieldLabel: 'Bad Debt Relief Claims Amount',
        hidden: !badDeptReliefClaims,
        hideLabel: !badDeptReliefClaims,
        allowNegative: false,
        allowBlank: !badDeptReliefClaims,
        allowDecimals: true,
        decimalPrecision: 2,
        value: 0//Default

    });
    this.preRegistrationClaimAmount = new Wtf.form.NumberField({
        name: 'preRegistrationClaimAmount',
        fieldLabel: 'Pre-registration Claims Amount',
        hidden: !preRegistrationClaim,
        hideLabel: !preRegistrationClaim,
        allowNegative: false,
        allowBlank: !preRegistrationClaim,
        allowDecimals: true,
        decimalPrecision: 2,
        value: 0//Default
    });
    this.schemesFieldset = new Wtf.form.FieldSet({
        id: 'schemesFieldset',
        xtype: 'fieldset',
        title: 'Schemes',
        autoHeight: true,
        checkboxToggle: false,
        hidden: !claimGSTrefundedTourist && !badDeptReliefClaims && !preRegistrationClaim,
        items: [this.claimGSTrefundedTouristAmount, this.badDeptReliefClaimsAmount, this.preRegistrationClaimAmount]
    });
    this.defImpPayableAmount = new Wtf.form.NumberField({
        name: 'defImpPayableAmt',
        fieldLabel: 'Deferred Import GST Payable Amount [15]',
        allowBlank: false,
        allowDecimals: true,
        decimalPrecision: 2,
        value: 0//Default
    });
    this.defTotalGoodsImpAmount = new Wtf.form.NumberField({
        name: 'defTotalGoodsImp',
        fieldLabel: 'Total value of goods imported under Import GST Deferment Scheme [17]',
        allowBlank: false,
        allowDecimals: false,
        value: 0//Default
    });
    this.igdSchemeFieldset = new Wtf.form.FieldSet({
        id: 'igdSchemeFieldset',
        xtype: 'fieldset',
        title: 'IGD Schemes',
        autoHeight: true,
        checkboxToggle: false,
        items: [this.defImpPayableAmount, this.defTotalGoodsImpAmount]
    });
    this.grp1BadDebtRecoveryChk = new Wtf.form.Checkbox({
        name: 'grp1BadDebtRecoveryChk',
        id: 'grp1BadDebtRecoveryChk',
        fieldLabel: "Bad Debt Recovery " + WtfGlobal.addLabelHelp("Total Standard Supplies less than Output Tax Due - Bad Debt Recovery")
    });
    this.grp1PriorToRegChk = new Wtf.form.Checkbox({
        name: 'grp1PriorToRegChk',
        id: 'grp1PriorToRegChk',
        fieldLabel: "Prior To Registration " + WtfGlobal.addLabelHelp("Total Standard Supplies less than Output Tax Due - Prior To Registration")
    });
    this.grp1OtherReasonChk = new Wtf.form.Checkbox({
        name: 'grp1OtherReasonChk',
        id: 'grp1OtherReasonChk',
        fieldLabel: "Other Reasons " + WtfGlobal.addLabelHelp("Total Standard Supplies less than Output Tax Due - Other Reasons")
    });
    this.grp1OtherReasons = new Wtf.form.TextArea({
        name: 'grp1OtherReasons',
        fieldLabel: 'Other Reasons Specification ' + WtfGlobal.addLabelHelp("Total Standard Supplies less than Output Tax Due - Other Reasons Specification"),
        maxLength: 200,
        width: 158
    });
    this.grp2TouristRefundChk = new Wtf.form.Checkbox({
        name: 'grp2TouristRefundChk',
        id: 'grp2TouristRefundChk',
        fieldLabel: "Tourist Refund " + WtfGlobal.addLabelHelp("Total Tax Purchase less than Input Tax Refund -Tourist Refund")
    });
    this.grp2AppvBadDebtReliefChk = new Wtf.form.Checkbox({
        name: 'grp2AppvBadDebtReliefChk',
        id: 'grp2AppvBadDebtReliefChk',
        fieldLabel: "Approved Bad Debt Relief " + WtfGlobal.addLabelHelp("Total Tax Purchase less than Input Tax Refund - Approved Bad Debt Relief")
    });
    this.grp2CreditNotesChk = new Wtf.form.Checkbox({
        name: 'grp2CreditNotesChk',
        id: 'grp2CreditNotesChk',
        fieldLabel: "Credit Notes " + WtfGlobal.addLabelHelp("Total Tax Purchase less than Input Tax Refund - Credit Notes")
    });
    this.grp2OtherReasonsChk = new Wtf.form.Checkbox({
        name: 'grp2OtherReasonsChk',
        id: 'grp2OtherReasonsChk',
        fieldLabel: "Other Reasons " + WtfGlobal.addLabelHelp("Total Tax Purchase less than Input Tax Refund - Other Reasons")
    });
    this.grp2OtherReasons = new Wtf.form.TextArea({
        name: 'grp2OtherReasons',
        fieldLabel: 'Other Reasons Specification ' + WtfGlobal.addLabelHelp("Total Tax Purchase less than Input Tax Refund - Other Reasons Specification"),
        maxLength: 200,
        width: 158
    });

    this.grp3CreditNotesChk = new Wtf.form.Checkbox({
        name: 'grp3CreditNotesChk',
        id: 'grp3CreditNotesChk',
        fieldLabel: "Credit Notes " + WtfGlobal.addLabelHelp("Total Tax Purchase less than Total Value Scheme - Credit Notes")
    });
    this.grp3OtherReasonsChk = new Wtf.form.Checkbox({
        name: 'grp3OtherReasonsChk',
        id: 'grp3OtherReasonsChk',
        fieldLabel: "Other Reasons " + WtfGlobal.addLabelHelp("Total Tax Purchase less than Total Value Scheme - Other Reasons")
    });
    this.grp3OtherReasons = new Wtf.form.TextArea({
        name: 'grp3OtherReasons',
        fieldLabel: 'Other Reasons Specification ' + WtfGlobal.addLabelHelp("Total Tax Purchase less than Total Value Scheme - Other Reasons Specification"),
        maxLength: 200,
        width: 158
    });
    this.reasonsSupplyFieldset = new Wtf.form.FieldSet({
        id: 'reasonsSupplyFieldset',
        xtype: 'fieldset',
        title: 'Reasons (Supply & Output Tax)',
        autoHeight: true,
        checkboxToggle: false,
        items: [this.grp1BadDebtRecoveryChk, this.grp1PriorToRegChk, this.grp1OtherReasonChk, this.grp1OtherReasons]
    });
    this.reasonsPurchaseFieldset = new Wtf.form.FieldSet({
        id: 'reasonsPurchaseFieldset',
        xtype: 'fieldset',
        title: 'Reasons (Purchase & Input Tax)',
        autoHeight: true,
        checkboxToggle: false,
        items: [this.grp2TouristRefundChk, this.grp2AppvBadDebtReliefChk, this.grp2CreditNotesChk, this.grp2OtherReasonsChk, this.grp2OtherReasons]
    });
    this.reasonsPurchaseSchemesFieldset = new Wtf.form.FieldSet({
        id: 'reasonsPurchaseSchemesFieldset',
        xtype: 'fieldset',
        title: 'Reasons (Purchase & Scheme)',
        autoHeight: true,
        checkboxToggle: false,
        items: [this.grp3CreditNotesChk, this.grp3OtherReasonsChk, this.grp3OtherReasons]
    });
    this.gstForm5eSubmissionwin = new Wtf.Window({
        title: 'GST e-Submission',
        closable: true,
        width: 600,
        modal: true,
        height: 500,
        plain: true,
        autoScroll: true,
        layout: 'border',
        items: [{
                region: 'north',
                height: 70,
                border: false,
                bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                html: getTopHtml('GST Form 5 e-Submission', " Fill all GST Form 5 e-submission details ", "../../images/accounting_image/price-list.gif", true)
            }, this.centerFormPanel = new Wtf.form.FormPanel({
                border: false,
                region: 'center',
                style: 'background:#f1f1f1',
                bodyStyle: 'font-size:10px;padding:10px',
                baseCls: 'bckgroundcolor',
                labelWidth: 230,
                autoHeight: true,
                items: [this.schemesFieldset, this.igdSchemeFieldset, this.reasonsSupplyFieldset, this.reasonsPurchaseFieldset, this.reasonsPurchaseSchemesFieldset]
            })
        ],
        buttonAlign: 'right',
        buttons: [{
                text: WtfGlobal.getLocaleText("acc.common.submit"), //'Submit',
                scope: this,
                handler: function () {
                    /*
                     * Validation for GST eSubmission window
                     */
                    if (!this.validateeSumbissionDataOnSubmit()) {
                        return;
                    }
                    this.loadingMask.show();
                    var requestParam = this.centerFormPanel.getForm().getValues();
                    requestParam.stdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
                    requestParam.enddate = WtfGlobal.convertToGenericStartDate(this.endDate.getValue());
                    requestParam.claimGSTrefundedTourist = claimGSTrefundedTourist;
                    requestParam.badDeptReliefClaims = badDeptReliefClaims;
                    requestParam.preRegistrationClaim = preRegistrationClaim;
                    requestParam.nameOfDeclarant = nameOfDeclarant;
                    requestParam.declarantId = declarantId;
                    requestParam.designation = designation;
                    requestParam.contactPerson = contactPerson;
                    requestParam.contactNo = contactNo;
                    requestParam.contactPersonEmailid = contactPersonEmailid;
                    requestParam.taxRefNumber = Wtf.account.companyAccountPref.taxNumber;
                    if (Wtf.account.companyAccountPref.isMultiEntity) {
                        requestParam.searchJSON = searchJson;
                        requestParam.filterConjuctionCriteria = filterConjuctionCriteria;
                        requestParam.multiEntityId = multiEntityId;
                        requestParam.multiEntityName = multiEntityName;
                    }
                    WtfGlobal.setAjaxTimeOutFor30Minutes();
                    Wtf.Ajax.requestEx({
                        url: "ACCReports/gstForm5eSubmission.do",
                        method: 'POST',
                        params: requestParam
                    }, this, function (response) {
                        WtfGlobal.resetAjaxTimeOut();
                        this.loadingMask.hide();
                        if (response.success && response.data) {
                            this.gstForm5eSubmissionwin.close();
                                if (response.responseStatus != 3) {
                                Wtf.MessageBox.confirm("GST Form 5", "You will be redirected to IRAS for authentication. ", function (btn) {
                                    if (btn != "yes") {
                                        // Changing Status on click "No" - Aborted By User 
                                        Wtf.Ajax.requestEx({
                                            url: "ACCReports/gstForm5eSubmissionUpdateStatus.do",
                                            method: 'POST',
                                            params: {
                                                id: response.data.chunkids,
                                                status : 3// Aborted By User
                                            }
                                        }, this);
                                        return;
                                    }
                                    var chunkids = response.data.chunkids;
                                    var companyid = response.data.companyid;
                                    var callbackURL = response.data.callbackUrl;
                                    var description = response.data.description;
                                    var form5orTLSubmission = response.data.flag;
                                    this.loadingMask.show();
                                    this.getIRASUrl(chunkids, companyid, callbackURL, description, form5orTLSubmission);
                                }, this);
                            } else {
                                var alertIcon = response.responseStatus - 1;
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), response.msg], alertIcon);
                                if (flag == 1) {
                                    this.gstForm5eSubmissionwin.close();
                                }
                            }
                        } else {
                            WtfGlobal.resetAjaxTimeOut();
                            if(response.responseStatus && response.msg){
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), response.msg], 2);
                            }else{
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Error occurred during process please try after sometime."], 1);
                            }
                        }
                    });
                }
            }, {
                text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"), //'Cancel',
                scope: this,
                handler: function () {
                    this.gstForm5eSubmissionwin.close();
                }
            }]
    });

    Wtf.MessageBox.confirm("GST Form 5 e-Submission", "Do you want to submit GST Form 5 data from " +
            this.startDate.getValue().format(WtfGlobal.getOnlyDateFormat()) + " to " +
            this.endDate.getValue().format(WtfGlobal.getOnlyDateFormat()) + "<br><br>" +
            "<b>Note :</b> Advance filter is not applicable for e-submission", function (btn) {
                if (btn != "yes") {
                    return false;
                } else {
                    this.gstForm5eSubmissionwin.show();
                }
            }, this);

}
Wtf.extend(Wtf.account.GSTForm5eSubmissionFunction, Wtf.account.GSTForm5HierarchyTab, {
    getIRASUrl: function (chunkids,companyid,callbackURL,description,form5orTLSubmission) {
        WtfGlobal.setAjaxTimeOutFor30Minutes();
        Wtf.Ajax.requestEx({
            url: "ACCReports/getIRASRedirectingUrl.do",
            method: 'POST',
            params: {
                chunkids: chunkids,
                companyid: companyid,
                callbackURL: callbackURL,
                description: description,
                flag: form5orTLSubmission
            }
        }, this, function (response) {
            WtfGlobal.resetAjaxTimeOut();
            this.loadingMask.hide();
            if (response.success) {
                if (response.isRedirectUrl) {
                    window.open(response.data);
                } else {
                    var alertIcon = response.responseStatus - 1;
                    var msg = "Error occurred during process please try after sometime."
                    if (response.msg) {
                        msg = response.msg;
                    }
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], alertIcon);
                }
            } else {
                WtfGlobal.resetAjaxTimeOut();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Error occurred during process please try after sometime."], 1);
            }
        });
    },
    onRender: function (config) {
        Wtf.account.GSTForm5eSubmissionFunction.superclass.onRender.call(this, config);
    },
    validateeSumbissionData: function () {
        var nameOfDeclarant = document.getElementById('id0').value;  //name of Declarant
        var declarantId = document.getElementById('id1').value; //Designation Id
        var designation = document.getElementById('id2').value; //Designation
        var contactPerson = document.getElementById('id3').value; //Contact person
        var contactNo = document.getElementById('id4').value;//Contact No
        var contactEmail = document.getElementById('id5').value;//Contact No
        var valueForBox_7 = WtfGlobal.searchRecord(this.gstStore, "[7]", "box").data.taxamount;

        var message = "";
        if (Wtf.isEmpty(nameOfDeclarant) ||
                Wtf.isEmpty(declarantId) ||
                Wtf.isEmpty(designation) ||
                Wtf.isEmpty(contactPerson) ||
                Wtf.isEmpty(contactEmail) ||
                Wtf.isEmpty(contactNo)) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Please provide all details of declaration"], 2);
            return false;
        }
        if (declarantId.length > 30) {
            message = "<b>Declarant Id</b> must be less than or equal to 30 character <br>";
        }
        if (nameOfDeclarant.length > 100) {
            message += "<b>Declarant Name</b> must be less than or equal to 100 character  <br>";
        }
        if (designation.length > 60) {
            message += "<b>Designation</b> must be less than or equal to 60 character  <br>";
        }
        if (contactPerson.length > 50) {
            message += "<b>Contact Person</b> must be less than or equal to 50 character  <br>";
        }
        var patten = /[0-9]/g;
        if (contactNo.length > 8 || !contactNo.match(patten)) {
            message += "<b>Contact No</b> must be numeric and less than or equal to 8 digit  <br>";
        }
        patten = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        if (contactNo.length > 50 || !contactEmail.match(patten)) {
            message += "<b>Contact Person Email Id</b> must be in valid format.<br>";
        }
        if (message != "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "<br>Please check e-Submission details<br><br>" + message], 2);
            return false;
        }
        /*
         * Validation for claims checks for box 7 in GST Form 5
         */
        if (valueForBox_7 == 0 && (this.claimGSTrefundedTouristCheck || this.badDeptReliefClaimsCheck || this.preRegistrationClaimCheck)) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.gst.GSTeSubmissionValidationMsgBox7")], 2);
            return false;
        }

        return true;
    },
    validateeSumbissionDataOnSubmit: function () {
        /*
         * Validation for eSubmission window.
         */


        var valueForBox_1 = WtfGlobal.searchRecord(this.gstStore, "[1]", "box").data.taxamount;
        var valueForBox_6 = WtfGlobal.searchRecord(this.gstStore, "[6]", "box").data.taxamount;
        var valueForBox_5 = WtfGlobal.searchRecord(this.gstStore, "[5]", "box").data.taxamount;
        var valueForBox_7 = WtfGlobal.searchRecord(this.gstStore, "[7]", "box").data.taxamount;
        var valueForBox_9 = WtfGlobal.searchRecord(this.gstStore, "[9]", "box").data.taxamount;
        /*
         * Group 1 Check is for reasons from supply and output tax checks
         */
        var group1_Check = !(this.grp1BadDebtRecoveryChk.getValue() || this.grp1PriorToRegChk.getValue() || this.grp1OtherReasonChk.getValue()) ? true : false;

        /*
         * Group 2 Check is for reasons from Purchase and Input checks
         */
        var group2_Check = !(this.grp2TouristRefundChk.getValue() || this.grp2AppvBadDebtReliefChk.getValue() || this.grp2CreditNotesChk.getValue() || this.grp2OtherReasonsChk.getValue()) ? true : false;


        //Group 3 Checks is for comparison between box 5 and box 9       
        var group3_Check = !(this.grp3CreditNotesChk.getValue() || this.grp3OtherReasonsChk.getValue()) ? true : false;

        if ((valueForBox_1 < valueForBox_6 || valueForBox_1 == valueForBox_6 || (valueForBox_1 != 0 && valueForBox_6 == 0) || (valueForBox_1 > 0 && valueForBox_6 < 0) || (valueForBox_1 == 0 && valueForBox_6 != 0)) && group1_Check) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.gst.GSTeSubmissionValidationMsgBox1_6")], 2);
            return false;
        }

        if ((valueForBox_5 < valueForBox_7 || (valueForBox_5 == 0 && valueForBox_7 != 0) || (valueForBox_7 != 0 && (valueForBox_5 == valueForBox_9))) && group2_Check) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.gst.GSTeSubmissionValidationMsgBox5_7")], 2);
            return false;
        }

        if ((valueForBox_9 != 0 && valueForBox_5 < valueForBox_9) && group3_Check) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.gst.GSTeSubmissionValidationMsgBox5_9")], 2);
            return false;
        }

        if ((this.grp1OtherReasonChk.checked && Wtf.isEmpty(this.grp1OtherReasons.getValue())) ||
                (this.grp2OtherReasonsChk.checked && Wtf.isEmpty(this.grp2OtherReasons.getValue())) ||
                (this.grp3OtherReasonsChk.checked && Wtf.isEmpty(this.grp3OtherReasons.getValue()))) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "You have selected \"Other Reason\"option but not specified its reasons"], 2);
            return;
        }

        /*
         * Box 15 (Deferred Import GST Payable Amount) value:  this.defImpPayableAmount.getValue()
         * Box 17 (Total value of goods imported under Import GST Deferment Scheme)value: this.defTotalGoodsImpAmount.getValue()
         */

        if (this.defImpPayableAmount.getValue() < 0 && this.defTotalGoodsImpAmount.getValue() > 0) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.gst.GSTeSubmissionValidationMsgBox15_17_Msg1")], 2);
            return false;
        }

        if (this.defImpPayableAmount.getValue() == 0 && this.defTotalGoodsImpAmount.getValue() != 0) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.gst.GSTeSubmissionValidationMsgBox15_17_Msg2")], 2);
            return false;
        }

        if (this.defImpPayableAmount.getValue() != 0 && this.defTotalGoodsImpAmount.getValue() == 0) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.gst.GSTeSubmissionValidationMsgBox15_17_Msg3")], 2);
            return false;
        }

        if ((this.defImpPayableAmount.getValue() > 0 && this.defTotalGoodsImpAmount.getValue() < 0) || (this.defImpPayableAmount.getValue() < 0 && this.defTotalGoodsImpAmount.getValue() < 0 && this.defImpPayableAmount.getValue() >= this.defTotalGoodsImpAmount.getValue()) || (this.defImpPayableAmount.getValue() > 0 && this.defTotalGoodsImpAmount.getValue() > 0 && this.defImpPayableAmount.getValue() >= this.defTotalGoodsImpAmount.getValue())) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.gst.GSTeSubmissionValidationMsgBox15_17_Msg4")], 2);
            return false;
        }

        return true;
    }
}, this);




function GSTTransactionListingSubmissionDetails() {
    var GSTTransactionListingSubmissionDetailsTab = Wtf.getCmp('GSTTransactionListingSubmissionDetailsId');
    if (GSTTransactionListingSubmissionDetailsTab == null) {
        var GSTTransactionListingSubmissionDetailsTabReport = new Wtf.account.GSTTransactionListingSubmissionDetails({
            id: "GSTTransactionListingSubmissionDetailsId",
            border: false,
            closable: true,
            iconCls: 'accountingbase agedpayable',
            title: "GST Transaction Listing Submission Details",
            tabTip: "GST Transaction Listing Submission Details"
        });
        Wtf.getCmp('as').add(GSTTransactionListingSubmissionDetailsTabReport);
        Wtf.getCmp('as').setActiveTab(GSTTransactionListingSubmissionDetailsTabReport);
    } else {
        Wtf.getCmp('as').setActiveTab(GSTTransactionListingSubmissionDetailsTab);
    }
    Wtf.getCmp('as').doLayout();
}
Wtf.account.GSTTransactionListingSubmissionDetails = function (config) {
    this.GSTTransactionListingSubmissionDetailsRec = new Wtf.data.Record.create([
        {name: 'id'},
        {name: 'fromdate'},
        {name: 'todate'},
        {name: 'status'},
        {name: 'submissiondatetime'},
        {name: 'messageCode'},
        {name: 'responsemessages'},
        {name: 'responsedetails'},
        {name: 'groupDateField'},
        {name: 'chunknumber'},
        {name: 'identifier'},
        {name: 'resubmitstatus'}

    ]);
    this.GSTTransactionListingSubmissionDetailsStore = new Wtf.data.GroupingStore({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: 'count'
        }, this.GSTTransactionListingSubmissionDetailsRec),
        url: "ACCReports/gstTransactionListingSubmissionDetails.do",
        groupField: "groupDateField",
        sortInfo: {field: 'chunknumber', direction: "ASC"}
    });
    this.rowNo = new Wtf.grid.RowNumberer({width: 35});
    this.startDate = new Wtf.ExDateFieldQtip({
        fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), //'From',
        name: 'stdate',
        format: WtfGlobal.getOnlyDateFormat(),
        value: this.getDates(true)
    });
    this.endDate = new Wtf.ExDateFieldQtip({
        fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), //'To',
        format: WtfGlobal.getOnlyDateFormat(),
        name: 'enddate',
        value: this.getDates(false)
    });
    this.fetch = new Wtf.Toolbar.Button({
        scope: this,
        text: WtfGlobal.getLocaleText("acc.common.fetch"), //"Fetch",
        iconCls: "accountingbase fetch",
        handler: function () {
            this.GSTTransactionListingSubmissionDetailsStore.load({
                params: {
                    startDate: this.startDate.getValue().format("Y-m-d"),
                    endDate: this.endDate.getValue().format("Y-m-d")
                }
            });
        }
    });
    this.resubmit = new Wtf.Toolbar.Button({
        scope: this,
        text: "Resubmit",
        iconCls: "accountingbase sync",
        handler: function () {
            this.resubmitChunk();
        }
    });
    this.GSTTransactionListingSubmissionDetailsStore.load({
        params: {
            startDate: this.startDate.getValue().format("Y-m-d"),
            endDate: this.endDate.getValue().format("Y-m-d")
        }
    });
    var btnArr = [];
    btnArr.push(WtfGlobal.getLocaleText("acc.common.from"), this.startDate, '-', WtfGlobal.getLocaleText("acc.common.to"), this.endDate, '-');
    btnArr.push(this.fetch,'-');
    btnArr.push(this.resubmit);
    // Grid Configuration
    this.sm = new Wtf.grid.CheckboxSelectionModel({
        singleSelect: false, //this.isRequisition ? false : true,
        listeners: {
            scope: this,
            rowselect: this.selectionChange
        }
    });

    this.grid = new Wtf.grid.GridPanel({
        stripeRows: true,
        store: this.GSTTransactionListingSubmissionDetailsStore,
        columns: [this.sm, this.rowNo, {
                header: "From Date",
                dataIndex: 'fromdate',
                width: 120
            }, {
                header: "To Date",
                dataIndex: 'todate',
                width: 120
            }, {
                header: "Status",
                dataIndex: 'status',
                width: 180,
                renderer: function (val, matadata, agruments) {
                    if (agruments.data.status == "0") {
                        return '<font color ="blue"><b>' + "Authentication Pending" + '</b></font>';
                    } else if (agruments.data.status == "1") {
                        return '<font color ="green"><b>' + "Success" + '</b></font>';
                    } else if (agruments.data.status == "2") {
                        return '<font color ="red"><b>' + "Failure" + '</b></font>';
                    } else if (agruments.data.status == "3") {
                        return '<font color ="blue"><b>' + "Pending" + '</b></font>';
                    }
                }
            }, {
                header: "Chunk Number",
                dataIndex: 'chunknumber',
                width: 100
            },{
                header: "Response Received On",
                dataIndex: 'submissiondatetime',
                width: 180
            }, {
                header: "Resubmit Status",
                dataIndex: 'resubmitstatus',
                width: 180,
                renderer: function (val, matadata, agruments) {
                    if (agruments.data.resubmitstatus == "1") {
                        return '<b>' + "Ready For Resubmit" + '</b>';
                    } else if (agruments.data.resubmitstatus == "0") {
                        return '<b>' + "Wait For Resubmit" + '</b>';
                    } else {
                        return '';
                    }
                }
            }, {
                header: "Submission Period",
                dataIndex: 'groupDateField',
                width: 180,
                hidden: true
            }, {
                header: "Messages Code",
                dataIndex: 'messageCode',
                width: 180
            }, {
                header: "Response Messages",
                dataIndex: 'responsemessages',
                width: 200
            }, {
                header: "Response Details",
                dataIndex: 'responsedetails',
                width: 220,
                renderer: function (value, matadata) {
                    var val = Wtf.util.Format.ellipsis(value, 100);
//                    matadata.attr = 'style="white-space: normal;"';
                    return "<div wtf:qtip=\"" + value + "\">" + val + "</div>";
                }
            }, {
                header: "Download Chunk Data",
                dataIndex: "download",
                width: 200,
                align: "Left",
                renderer: function (val, m, rec) {
                    return "<div class=\"pwnd downloadIcon original\" wtf:qtip=\"Download CSV File\" style=\"height:16px;\">&nbsp;</div>";
                }
            }],
        sm: this.sm,
        view: new Wtf.grid.GroupingView({
            forceFit: false,
            emptyText: '<div style="font-size:15px; text-align:center; color:#808080; font-weight:bold; margin-top:1%;">No Record Found</div>',
        }),
        border: false,
        id: "GSTTransactionListingSubmissionDetails",
        loadMask: true,
        tbar: btnArr
    });
    this.grid.on("render", function () {
        this.grid.getView().applyEmptyText();
    }, this);
    Wtf.apply(this, {
        layout: 'border',
        border: false,
        scope: this,
        statementType: "GSTTransactionListing",
        items: [{
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid]
            }],
    }, config);
    this.grid.on('rowclick',this.handleRowClick,this);
    Wtf.account.GSTTransactionListingSubmissionDetails.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.GSTTransactionListingSubmissionDetails, Wtf.Panel, {
    onRender: function (config) {
        Wtf.account.GSTTransactionListingSubmissionDetails.superclass.onRender.call(this, config);
    },
    getDates: function (start) {
        var d = new Date();
        var monthDateStr = d.format('M d');
        if (Wtf.account.companyAccountPref.fyfrom)
            monthDateStr = Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd = new Date(monthDateStr + ', ' + d.getFullYear() + ' 12:00:00 AM');
        if (d < fd)
            fd = new Date(monthDateStr + ', ' + (d.getFullYear() - 1) + ' 12:00:00 AM');
        if (start)
            return fd;

        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
    getIRASUrl: function (chunkids,companyid,callbackURL,description,flag) {
        Wtf.Ajax.requestEx({
            url: "ACCReports/getIRASRedirectingUrl.do",
            method: 'POST',
            params: {
                chunkids: chunkids,
                companyid: companyid,
                callbackURL: callbackURL,
                description: description,
                flag: flag
            }
        }, this, function (response) {
            WtfGlobal.resetAjaxTimeOut();
            this.loadingMask.hide();
            if (response.success && !Wtf.isEmpty(response.isRedirectUrl)) {
//                if (response.isRedirectUrl) {
                    window.open(response.data);
//                } else {
                    //Handle failure message for Transaction Listing.
//                }
            } else {
                WtfGlobal.resetAjaxTimeOut();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Error occurred during process please try after sometime."], 1);
            }
        });
    },
    resubmitChunk: function () {
        this.loadingMask = new Wtf.LoadMask(this.id, {
            msg: "Preparing Data..."
        });
        var arr = this.grid.getSelectionModel().getSelections();
        if (arr.length <= 0) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Please select record(s) to resubmit"], 2);
            return;
        }
        Wtf.MessageBox.confirm("GST Transaction Listing", "Do you want to resubmit selected record(s) ", function (btn) {
            if (btn != "yes") {
                return;
            }
            var ids = "";
            for (var i = 0; i < arr.length; i++) {
                if (i < arr.length - 1) {
                    ids += arr[i].data.id + ",";
                } else {
                    ids += arr[i].data.id;
                }
            }
            this.loadingMask.show();
            Wtf.Ajax.requestEx({
                url: "ACCReports/gstTransactionListingreSubmission.do",
                method: 'POST',
                params: {
                    ids: ids
                }
            }, this, function (response) {
                this.loadingMask.hide();
                if (response.success) {
                    WtfGlobal.resetAjaxTimeOut();
                    Wtf.MessageBox.confirm("GST Transaction Listing", "You will be redirected to IRAS for authentication. ", function (btn) {
                        if (btn != "yes") {
                            return;
                        }
                        var chunkids = response.data.chunkids;
                        var companyid = response.data.companyid;
                        var callbackURL = response.data.callbackUrl;
                        var description = response.data.description;
                        var flag = response.data.flag;
                        this.loadingMask.show();
                        this.getIRASUrl(chunkids,companyid,callbackURL,description,flag);
                    },this);
                } else {
                    WtfGlobal.resetAjaxTimeOut();
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Error occurred during process please try after sometime."], 1);
                }
            });
        }, this);
    },
    selectionChange: function (sm, rowCount, record) {
        if (record.data.resubmitstatus != 1 && record.data.status != 0) {
            sm.deselectRow(rowCount);
        }
    },
    handleRowClick: function (grid, rowindex, e) {
        if (e.getTarget(".original")) {
            var rec = this.grid.getStore().getAt(rowindex).data;
            if (rec) {
                Wtf.get('downloadframe').dom.src = 'ACCReports/downloadTransactionListingChunkData.do?id=' + rec.id;
            }
        }
    }
});
