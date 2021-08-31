
fieldIDs_IRAS_Form5 = {
    irasDetailsTable: "irasDetailsTable",
    irasDetails: "irasDetails",
    createDocBttn_IRAS: "createDocBttn_IRAS",
    irasForm: "irasForm",
    irasUserMessage: "irasUserMessage"
};

function getFieldById_IRAS_Form5(fieldid) {
    return this.document.getElementById(fieldid);
}

function onFormLoad_IRAS_GSTForm5Submission(state, code, scope, ids)
{
    hideIRASDetails_GSTForm5Submission();
    showIRASDetails_GSTForm5Submission(state, code, scope, ids);
}

/**
 * Method used to hide data on the page when page loaded.
 * @returns {undefined}
 */
function hideIRASDetails_GSTForm5Submission()
{
    getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.irasUserMessage).style.display = "none";
    getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.irasDetailsTable).style.display = "none";
    getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.irasDetails).hidden = true;
}

/**
 * Method used to show GST Form 5 e-Submission data in table.
 * @param {type} state
 * @param {type} code
 * @param {type} scope
 * @param {type} ids
 * @returns {undefined}
 */
function showIRASDetails_GSTForm5Submission(state, code, scope, ids) {

    if (state)
    {
        var loadMask = new Wtf.LoadMask(Wtf.getBody(), {
            msg: "Loading..."
        });
        loadMask.show();
        setAjaxRequestTimeoutForIRAS_GSTForm5Submission();
        Wtf.Ajax.requestEx({
            url: "ACCReports/getForm5SubmissinonDetails.do",
            method: "POST",
            params: {
                ids: ids
            }
        }, this, function (res, req) {
            loadMask.hide();
            if (res.success) {
                var companyid = res.data.companyid;
                if (res.data) {
                    Wtf.cdomain = res.data.cdomain;
                    displayIRASDetails_GSTForm5Submission(res.data, state, code, scope, ids, true, companyid);
                } else {
                    displayIRASDetails_GSTForm5Submission(res.data, state, code, scope, ids, false, companyid);
                }
            } else {
                var irasUserMessage = getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.irasUserMessage);
                var color = "#FF0000";
                irasUserMessage.innerHTML = "<font color=" + color + ">" + res.msg + "</font>"; //Internal Server Error.
                getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.irasUserMessage).style.display = "block";
                getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.irasDetails).hidden = false;
                getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.createDocBttn_IRAS).disabled = false;
            }
        }, function () {
            loadMask.hide();
            var irasUserMessage = getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.irasUserMessage);
            var color = "#FF0000";
            irasUserMessage.innerHTML = "<font color=" + color + ">Failed to make connection with web server.</font>";
            getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.irasUserMessage).style.display = "block";
            getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.irasDetails).hidden = false;
            getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.createDocBttn_IRAS).disabled = false;
        });
    } else {
        var irasUserMessage = getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.irasUserMessage);
        var color = "#FF0000";
        irasUserMessage.innerHTML = "<font color=" + color + ">Can not proceed due to empty value of state</font>";
        getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.irasUserMessage).style.display = "block";
        getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.irasDetails).hidden = false;
        getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.createDocBttn_IRAS).disabled = false;
    }
}

/**
 * Displaying GST Form 5 e-Submission data in table.
 * @param {type} obj
 * @param {type} state
 * @param {type} code
 * @param {type} scope
 * @param {type} ids
 * @param {type} flag
 * @returns {undefined}
 */
function displayIRASDetails_GSTForm5Submission(obj, state, code, scope, ids, flag, companyid) {
    if (obj && obj.filingInfo && flag) {
        var irasDetailsTable = getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.irasDetailsTable);
        var irasUserMessage = getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.irasUserMessage);

        if (obj.companyName) {
            var row1 = irasDetailsTable.insertRow(0);
            var CompanyNameKeyCell = row1.insertCell(0);
            var CompanyNameValueCell = row1.insertCell(1);
            CompanyNameKeyCell.innerHTML = "<b>Company Name</b>";
            CompanyNameValueCell.innerHTML = obj.companyName ? obj.companyName : "";
        }

        if (obj.filingInfo.dtPeriodStart) {
            var row2 = irasDetailsTable.insertRow(1);
            var FromDateKey = row2.insertCell(0);
            var FromDateValue = row2.insertCell(1);
            FromDateKey.innerHTML = "<b>From Date</b>";
            FromDateValue.innerHTML = obj.filingInfo.dtPeriodStart ? obj.filingInfo.dtPeriodStart : "";
        }

        if (obj.filingInfo.dtPeriodEnd) {
            var row3 = irasDetailsTable.insertRow(2);
            var ToDateKey = row3.insertCell(0);
            var ToDateValue = row3.insertCell(1);
            ToDateKey.innerHTML = "<b>To Date</b>";
            ToDateValue.innerHTML = obj.filingInfo.dtPeriodEnd ? obj.filingInfo.dtPeriodEnd : "";
        }

        if (obj.filingInfo.taxRefNo) {
            var row4 = irasDetailsTable.insertRow(3);
            var TaxRefNoKey = row4.insertCell(0);
            var TaxRefNoValue = row4.insertCell(1);
            TaxRefNoKey.innerHTML = "<b>Tax Reference No</b>";
            TaxRefNoValue.innerHTML = obj.filingInfo.taxRefNo ? obj.filingInfo.taxRefNo : "";
        }

        irasUserMessage.innerHTML = "Submission Initiated. Please check <b>GST Form 5 e-Submission History</b>.";
        getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.irasUserMessage).style.display = "block";
        getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.irasDetailsTable).style.display = "table";
        getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.irasDetails).hidden = false;
        getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.createDocBttn_IRAS).disabled = false;

        /**
         * Method is used to save GST Form 5 e-Submission data.
         * @type getFieldById_IRAS_Form5@pro;document@call;getElementById|getFieldById_IRAS_Form5@pro;document@call;getElementById
         */
        validateIRASDetails_GSTForm5Submission(state, code, scope, ids, companyid);
    } else {
        var irasUserMessage = getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.irasUserMessage);
        var color = "#FF0000";
        irasUserMessage.innerHTML = "<font color=" + color + ">IRAS <b>GST Form-5 e-Submission Data</b> Submission Failed.</font>";
        getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.irasUserMessage).style.display = "block";
        getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.irasDetails).hidden = false;
        getFieldById_IRAS_Form5(fieldIDs_IRAS_Form5.createDocBttn_IRAS).disabled = false;
    }
}

/**
 * Method is used to save GST Form 5 e-Submission data.
 * @type getFieldById_IRAS_Form5@pro;document@call;getElementById|getFieldById_IRAS_Form5@pro;document@call;getElementById
 */
function validateIRASDetails_GSTForm5Submission(state, code, scope, ids, companyid) {
    setAjaxRequestTimeoutForIRAS_GSTForm5Submission();
    Wtf.Ajax.requestEx({
        url: "ACCReports/saveForm5SubmissinonDetails.do",
        method: "POST",
        params: {
            ids: ids, //ids to get information from gstform5esubmission table.
            state: state, //unique id which remain same from start to end of submission process.
            code: code,
            companyid: companyid
        }
    }, this, function (res, req) {
    }, function () {
        alert("Failed to make connection with web server.");
    });
}

/**
 * Method used to close current window.
 * @returns {undefined}
 */
function close_Form5() {
    window.close();
}

function setAjaxRequestTimeoutForIRAS_GSTForm5Submission() {
    Wtf.Ajax.timeout = 300000;//set timeout to 5 minutes
}

