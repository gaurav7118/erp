
fieldIDs_IRAS_TransactionListing = {
    irasDetailsTable: "irasDetailsTable",
    irasDetails: "irasDetails",
    createDocBttn_IRAS: "createDocBttn_IRAS",
    irasForm: "irasForm",
    irasUserMessage: "irasUserMessage"
};

function getFieldById_IRAS_TransactionListing(fieldid) {
    return this.document.getElementById(fieldid);
}

function onFormLoad_IRAS_TransactionListing(state, code, scope, ids)
{
    hideIRASDetails_TransactionListing();
    showIRASDetails_TransactionListing(state, code, scope, ids);
}

/**
 * Method used to hide data on the page when page loaded.
 * @returns {undefined}
 */
function hideIRASDetails_TransactionListing()
{
    getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.irasUserMessage).style.display = "none";
    getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.irasDetailsTable).style.display = "none";
    getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.irasDetails).hidden = true;
}

/**
 * Method used to show Transaction Listing data in table.
 * @param {type} state
 * @param {type} code
 * @param {type} scope
 * @param {type} ids
 * @returns {undefined}
 */
function showIRASDetails_TransactionListing(state, code, scope, ids) {

    if (state)
    {
        var loadMask = new Wtf.LoadMask(Wtf.getBody(), {
            msg: "Loading..."
        });
        loadMask.show();
        setAjaxRequestTimeoutForIRAS_TransactionListing();
        Wtf.Ajax.requestEx({
            url: "ACCReports/getTransactionListingDetails.do",
            method: "POST",
            params: {
                ids: ids
            }
        }, this, function (res, req) {
            loadMask.hide();
            if (res.success) {
                var companyid = res.companyid;
                if (res.data) {
                    Wtf.cdomain = res.cdomain;
                    displayIRASDetails_TransactionListing(res.data, state, code, scope, companyid, ids, true);
                } else {
                    //If data not present in response
                    displayIRASDetails_TransactionListing(res.data, state, code, scope, companyid, ids, false);
                }
            } else {
                //For Internal Server Error
                var irasUserMessage = getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.irasUserMessage);
                var color = "#FF0000";
                irasUserMessage.innerHTML = "<font color=" + color + ">"+res.msg+"</font>";
                getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.irasUserMessage).style.display = "block";
                getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.irasDetails).hidden = false;
                getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.createDocBttn_IRAS).disabled = false;
            }
        }, function () {
            //If Ajax request Fails
            loadMask.hide();
            var irasUserMessage = getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.irasUserMessage);
            var color = "#FF0000";
            irasUserMessage.innerHTML = "<font color=" + color + ">Failed to make connection with web server.</font>";
            getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.irasUserMessage).style.display = "block";
            getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.irasDetails).hidden = false;
            getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.createDocBttn_IRAS).disabled = false;
        });
    } else {
        var irasUserMessage = getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.irasUserMessage);
        var color = "#FF0000";
        irasUserMessage.innerHTML = "<font color=" + color + ">Can not proceed due to empty value of state</font>";
        getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.irasUserMessage).style.display = "block";
        getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.irasDetails).hidden = false;
        getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.createDocBttn_IRAS).disabled = false;
    }
}

/**
 * Displaying Transaction Listing e-Submission data in table.
 * @param {type} obj
 * @param {type} state
 * @param {type} code
 * @param {type} scope
 * @param {type} companyid
 * @param {type} ids
 * @param {type} flag
 * @returns {undefined}
 */
function displayIRASDetails_TransactionListing(obj, state, code, scope, companyid, ids, flag) {
    if (obj && obj.filingInfo && flag) {
        var irasDetailsTable = getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.irasDetailsTable);
        var irasUserMessage = getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.irasUserMessage);

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

        if (obj.filingInfo.gstRegNo) {
            var row5 = irasDetailsTable.insertRow(4);
            var GSTRegNoKey = row5.insertCell(0);
            var GSTRegNoValue = row5.insertCell(1);
            GSTRegNoKey.innerHTML = "<b>GST Registration No</b>";
            GSTRegNoValue.innerHTML = obj.filingInfo.gstRegNo ? obj.filingInfo.gstRegNo : "";
        }

        irasUserMessage.innerHTML = "Submission Initiated. Please check <b>GST Transaction Listing Submission History</b>.";
        getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.irasUserMessage).style.display = "block";
        getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.irasDetailsTable).style.display = "table";
        getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.irasDetails).hidden = false;
        getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.createDocBttn_IRAS).disabled = false;

        /**
         * Method used to give Ajax call to save Transaction Listing e-Submission data.
         * @type getFieldById_IRAS_TransactionListing@pro;document@call;getElementById|getFieldById_IRAS_TransactionListing@pro;document@call;getElementById
         */
        validateIRASDetails_TransactionListing(state, code, scope, companyid, ids);
    } else {
        var irasUserMessage = getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.irasUserMessage);
        var color = "#FF0000";
        irasUserMessage.innerHTML = "<font color=" + color + ">IRAS <b>Transaction Listing</b> Data Submission Failed.</font>";
        getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.irasUserMessage).style.display = "block";
        getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.irasDetails).hidden = false;
        getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.createDocBttn_IRAS).disabled = false;
    }
}

/**
 * Method used to give Ajax call to save Transaction Listing actual data.
 * @type getFieldById_IRAS_TransactionListing@pro;document@call;getElementById|getFieldById_IRAS_TransactionListing@pro;document@call;getElementById
 */
function validateIRASDetails_TransactionListing(state, code, scope, companyid, ids) {
    setAjaxRequestTimeoutForIRAS_TransactionListing();
    Wtf.Ajax.requestEx({
        url: "ACCReports/gstTLAfterIRASAuthentication.do",
        method: "POST",
        params: {
            ids: ids,
            state: state,
            code: code,
            companyid: companyid
        }
    }, this, function (res, req) {}, function () {
        var irasUserMessage = getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.irasUserMessage);
        var color = "#FF0000";
        irasUserMessage.innerHTML = "<font color=" + color + ">Failed to make connection with web server.</font>";
        getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.irasUserMessage).style.display = "block";
        getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.irasDetails).hidden = false;
        getFieldById_IRAS_TransactionListing(fieldIDs_IRAS_TransactionListing.createDocBttn_IRAS).disabled = false;
    });
}

/*
 * Method is used to close current window of browser.
 */
function close_TransactionListing() {
    window.close();
}

function setAjaxRequestTimeoutForIRAS_TransactionListing() {
    Wtf.Ajax.timeout = 300000;//set timeout to 5 minutes
}

