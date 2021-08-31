
Wtf.UpdateAssetSerialDetails = function (config) {
    this.skufieldValue = config.skufieldValue;
    this.serialid = config.serialid;
    this.type=config.isSerialForProduct=="T"?1:2,
    this.itemcode=config.itemcode;
    this.batchName=config.batchName;
    this.serialexptodate = config.serialexptodate;
    this.batchExpdate = config.batchExpdate;
    this.assetdetailreport= config.assetdetailreport;
    this.isSerialForProduct= config.isSerialForProduct;
    this.createDiposalDateSettings();
    Wtf.apply(this, {
        buttons: [this.saveButton = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.common.submit"), // Submit
                minWidth: 50,
                scope: this,
                handler: this.submitForm.createDelegate(this)
            }), this.closeButton = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"), // Cancel
                minWidth: 50,
                scope: this,
                handler: this.closeOpenWin.createDelegate(this)
            })]
    }, config);
     
    Wtf.UpdateAssetSerialDetails.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.UpdateAssetSerialDetails, Wtf.Window, {
    onRender: function (config) {
        Wtf.UpdateAssetSerialDetails.superclass.onRender.call(this, config);

        this.add({
            region: 'center',
            border: false,
            layout: 'fit',
            baseCls: 'bckgroundcolor',
            items: [this.updateDataForm]
        });

       this.assetId.setValue(this.skufieldValue);
        if (this.serialexptodate !== undefined && this.serialexptodate != ''){
            this.expirydate.setValue(this.serialexptodate);
        }else if(this.batchExpdate !== undefined && this.batchExpdate != ''&&this.type==2){
            this.expirydate.setValue(this.batchExpdate);
        }


    },
    createDiposalDateSettings: function () {

        this.assetId = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText('acc.assetworkorder.AssetId'),
            name: 'assetId',
            maxLength: 100,
            anchor: '95%',
            scope: this,
            allowBlank: false,
            style: "margin-bottom: 10px;"
        });


        this.expirydate = new Wtf.form.DateField({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.ExpiryDate") + "'>" + WtfGlobal.getLocaleText("acc.field.ExpiryDate") + "</span>",
            format: WtfGlobal.getOnlyDateFormat(),
            allowBlank: false,
            name: 'asOfDate',
            id: 'asofdatevfbgfhfaedretygrehr' + this.id,
            anchor: '95%',
            height: 50
        });

        var tbararr = new Array();
        if(this.type==1){
            tbararr.push(this.assetId);
        }
        tbararr.push(this.expirydate);
        this.updateDataForm = new Wtf.form.FormPanel({
            border: false,
            autoWidth: true,
            autoHeight: true,
            anchor: '100%',
            labelWidth: 150,
            bodyStyle: 'background:#f1f1f1;font-size:8px;padding:15px',
            layout:'form',
//            items: [this.assetId, this.expirydate]
            items: tbararr
        });
    },
    closeOpenWin: function () {
        this.close();
    },
    submitForm: function () {
        this.saveButton.enable();
        var rec = {};
            rec.serialid = this.serialid;
            rec.skufieldValue = this.assetId.getValue();
            rec.disposaldate = WtfGlobal.convertToGenericDate(this.expirydate.getValue());
            rec.type= this.type=this.isSerialForProduct=="T"?1:2;
            rec.type=this.type;
            rec.itemcode=this.itemcode;
            rec.batchName=this.batchName;
        Wtf.Ajax.timeout = WtfGlobal.setAjaxTimeOut();

        Wtf.Ajax.requestEx({
            url: "ACCProductCMN/updateSkuFiedAndExpiryDateOfSerial.do",
            params: rec
        }, this, this.genSuccessResponse, this.genFailureResponse);
        this.assetdetailreport.initloadgridstore();
    },
    genSuccessResponse: function (response) {
        WtfGlobal.resetAjaxTimeOut();
        if (response.success) {
            if(response.isDuplicate){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.assetdetailreport.tbarbutton.UpdateSerialDetails.Duplicatefailuremsg")], 2);
                this.saveButton.enable();
            }else{
                
                this.closeOpenWin();
                if(this.type==1){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.assetdetailreport.tbarbutton.UpdateSerialDetails.successmsg")],0);
                }else {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.assetdetailreport.tbarbutton.updateExpDate.successmsg")],0);
                }
            }
        } else {
            this.closeOpenWin();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.assetdetailreport.tbarbutton.UpdateSerialDetails.failuremsg")], 2);
        }
       
    },
    genFailureResponse: function (response) {
        WtfGlobal.resetAjaxTimeOut();
       WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.assetdetailreport.tbarbutton.UpdateSerialDetails.failuremsg")], 2);
       
    }
});

