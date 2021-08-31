/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.account.LabourInformationPanel = function(config) {
    Wtf.apply(this, config);
    /*
     * Define required Stores
     */
    this.createStores();
    /*
     * Create Form Fields
     */

    this.createFormFields();
    /**
     * create Custom Fields
     */
    this.createCustomFields();
    /*
     * Create Buttons
     */
    this.createButton();
    /*
     * Create Field set
     */
    this.createFieldSet();
    /*
     * Create Form
     */
    this.createForm();
    /*
     * Create panel in Tab
     */
    this.createPanel();
    this.DOBDate.on('change',this.calculateAgeOfLabour,this);
    Wtf.account.LabourInformationPanel.superclass.constructor.call(this, config);

}

Wtf.extend(Wtf.account.LabourInformationPanel, Wtf.Panel, {
    onRender: function(config) {
        Wtf.account.LabourInformationPanel.superclass.onRender.call(this, config);
        this.add(this.centerPanel);
        this.sequenceFormatStore.load();
        if (this.sequenceFormatStore.getCount() > 0) {
            this.setNextNumber();
        } else {
            this.sequenceFormatStore.on('load', this.setNextNumber, this);
        }
        if (this.isEdit) {
            /*
             * Load record in edit case
             */
            this.getLabourDataToLoad();
        }

    },
    createStores: function() {
        var genderArr = new Array();
        genderArr.push(['Male', '1']);
        genderArr.push(['Female', '2']);
        this.genderStore = new Wtf.data.SimpleStore({
            fields: [{name: 'name'}, {name: 'value'}],
            data: genderArr
        });
        var mStatusArr = new Array();
        mStatusArr.push(['Married', '1']);
        mStatusArr.push(['Unmarried', '2']);
        this.mStatusStore = new Wtf.data.SimpleStore({
            fields: [{name: 'name'}, {name: 'value'}],
            data: mStatusArr
        });
        this.departmentStore = new Wtf.data.SimpleStore({
            fields: [{name: 'name'}, {name: 'value'}],
            data: genderArr
        });

        this.pmtRec = new Wtf.data.Record.create([
            {name: 'methodid'},
            {name: 'methodname'},
            {name: 'accountid'},
            {name: 'autopopulate'},
            {name: 'bankType'}
        ]);
        this.pmethodStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.pmtRec),
            url: "ACCPaymentMethods/getPaymentMethods.do",
            baseParams: {
                mode: 51
            }
        });
        this.departmentRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);

        this.departmentStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.departmentRec),
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 13
            }
        });
        this.keySkillStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.departmentRec),
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 54
            }
        });
        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            }, Wtf.sequenceFormatStoreRec),
            url: "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams: {
                mode: 'autolabour',
                isEdit: this.isEdit
            }
        });
        this.wcrec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'},
            {name: 'wcid'}
        ]);
        this.workCentreStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.wcrec),
            url: "ACCWorkCentreCMN/getWorkCentreForCombo.do"
        });
    },
    setNextNumber: function(config) {
        if (this.sequenceFormatStore.getCount() > 0) {
            if (this.isEdit) {                       //Edit case
                var index = this.sequenceFormatStore.find('id', this.record.data.sequenceformatid);
                if (index != -1) {
                    this.sequenceFormatCombobox.setValue(this.record.data.sequenceformatid);
                    this.sequenceFormatCombobox.disable();
                    this.empCode.disable();
                } else {
                    this.sequenceFormatCombobox.setValue("NA");
                    this.sequenceFormatCombobox.disable();
                    this.empCode.enable();
                }
            } else {
                this.setSequenceFormatForCreateNewCase();
            }
        }
    },
    setSequenceFormatForCreateNewCase: function() {
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
            this.empCode.setValue("");
            this.empCode.allowBlank = true;
            WtfGlobal.hideFormElement(this.empCode);
        }
    },
    getNextSequenceNumber: function(a, val) {
        if (!(a.getValue() == "NA")) {
            WtfGlobal.hideFormElement(this.empCode);
            var rec = WtfGlobal.searchRecord(this.sequenceFormatStore, a.getValue(), 'id');
            var oldflag = rec != null ? rec.get('oldflag') : true;
            Wtf.Ajax.requestEx({
                url: "ACCCompanyPref/getNextAutoNumber.do",
                params: {
                    from: this.fromnumber,
                    sequenceformat: a.getValue(),
                    oldflag: oldflag
                }
            }, this, function(resp) {
                if (resp.data == "NA") {
                    WtfGlobal.showFormElement(this.empCode);
                    this.empCode.reset();
                    this.empCode.allowBlank = false;
                    this.empCode.enable();
                } else {
                    this.empCode.setValue(resp.data);
                    this.empCode.disable();
                    this.empCode.allowBlank = true;
                    WtfGlobal.hideFormElement(this.empCode);
                }
            });
        } else {
            WtfGlobal.showFormElement(this.empCode);
            this.empCode.reset();
            this.empCode.enable();
            this.empCode.allowBlank = false;
        }
    },
    createFormFields: function() {
        this.sequenceFormatConfig = {
            mode: 'local',
            fieldLabel: WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
            disabled: (this.isEdit ? true : false),
            typeAhead: true,
            forceSelection: true,
            allowBlank: false,
            width: 240,
            name: 'sequenceformat',
            hiddenName: 'sequenceformat',
            emptyText: WtfGlobal.getLocaleText("acc.field.SelectSequenceFromat"),
            listeners: {
                'select': {
                    fn: this.getNextSequenceNumber,
                    scope: this
                }
            }

        };
        this.sequenceFormatCombobox = WtfGlobal.createFnCombobox(this.sequenceFormatConfig, this.sequenceFormatStore, 'id', 'value', this);
        this.workCentreConfig = {
            mode: 'remote',
            fieldLabel: WtfGlobal.getLocaleText("acc.machine.workCenter"),
            typeAhead: true,
            forceSelection: true,
            width: 240,
            name: 'workcentre',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode ? ['wcid'] : [],
            hiddenName: 'workcentre',
            emptyText: "Select Work Centre"
        };
        this.workCentreCombobox = WtfGlobal.createExtFnCombobox(this.workCentreConfig, this.workCentreStore, 'id', 'name', this);
        this.empCodeConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labour.empid") + "'>" + WtfGlobal.getLocaleText("acc.labour.empid") + "</span>" + ' *',
            name: 'empcode',
            hiddenName: 'empcode',
            id: "empcode" + this.id
        };
        this.empCode = WtfGlobal.createTextfield(this.empCodeConfig, false, false, 50, this);
        this.firstNameConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.FName") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.FName") + "</span>" + ' *',
            name: 'fname',
            hiddenName: 'fname',
            id: "fname" + this.id
        };
        this.firstName = WtfGlobal.createTextfield(this.firstNameConfig, false, false, 50, this);

        this.middleNameConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.MName") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.MName") + "</span>",
            name: 'mname',
            hiddenName: 'mname',
            id: "mname" + this.id
        };
        this.middleName = WtfGlobal.createTextfield(this.middleNameConfig, false, true, 50, this);
        this.lastNameConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.LName") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.LName") + "</span>" + ' *',
            name: 'lname',
            hiddenName: 'lname',
            id: "lname" + this.id
        };
        this.lastName = WtfGlobal.createTextfield(this.lastNameConfig, false, false, 50, this);
//        this.fullNameConfig = {
//            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.FullName") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.FullName") + "</span>",
//            name: 'fullname',
//            hiddenName: 'fullname',
//            id: "fullname" + this.id
//        };
//        this.fullName = WtfGlobal.createTextfield(this.fullNameConfig, false, true, 50, this);
        this.DOBConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.DateofBirth") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.DateofBirth") + "</span>" + ' *',
            name: 'dob',
            hiddenName: 'dob',
            id: "dob" + this.id
        };
        this.DOBDate = WtfGlobal.createDatefield(this.DOBConfig, false, this);

        this.ageConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.age") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.age") + "</span>",
            name: 'age',
            hiddenName: 'age',
            id: "age" + this.id
        };
        this.age = WtfGlobal.createNumberfield(this.ageConfig, false, true, 50, this);
        this.genderConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.Gender") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.Gender") + "</span>",
            name: 'gender',
            hiddenName: 'gender',
            id: "gender" + this.id
        };
        this.gender = WtfGlobal.createFnCombobox(this.genderConfig, this.genderStore, 'value', 'name', this);

        this.maritalStatusConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.MStatus") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.MStatus") + "</span>",
            name: 'maritalstatus',
            hiddenName: 'maritalstatus',
            id: "maritalstatus" + this.id
        };
        this.maritalStatus = WtfGlobal.createFnCombobox(this.maritalStatusConfig, this.mStatusStore, 'value', 'name', this);
        this.bGroupConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.Bgroup") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.Bgroup") + "</span>",
            name: 'bgroup',
            hiddenName: 'bgroup',
            id: "bgroup" + this.id
        };
        this.bloodGroup = WtfGlobal.createTextfield(this.bGroupConfig, false, true, 50, this);
        this.nationalityConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.Nationality") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.Nationality") + "</span>" + ' *',
            name: 'nationality',
            hiddenName: 'nationality',
            id: "nationality" + this.id
        };
        this.nationality = WtfGlobal.createTextfield(this.nationalityConfig, false, false, 50, this);
        this.countryOriginConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.COrigin") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.COrigin") + "</span>" + ' *',
            name: 'countryorigin',
            hiddenName: 'countryorigin',
            id: "countryorigin" + this.id
        };
        this.countryOrigin = WtfGlobal.createTextfield(this.countryOriginConfig, false, false, 50, this);
        this.departmentConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.Department") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.Department") + "</span>",
            name: 'department',
            hiddenName: 'department',
            id: "department" + this.id,
            mode: 'remote',
            extraFields: []
        };
        this.department = WtfGlobal.createExtFnCombobox(this.departmentConfig, this.departmentStore, 'id', 'name', this);
        this.department.addNewFn = this.addDepartment.createDelegate(this);

        this.keySkill = new Wtf.common.Select(Wtf.apply({
            multiSelect: true,
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.KeySkill") + '*' + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.KeySkill") + "*" + "</span>",
            forceSelection: true
        }, {
            name: 'keyskill',
            hiddenName: 'keyskill',
            id: "keyskill" + this.id,
            width: 240,
            mode: 'remote',
            allowBlank: false,
            store: this.keySkillStore,
            valueField: 'id',
            displayField: 'name',
            triggerAction: 'all',
            addCreateOpt: false,
            addNewFn: this.addSkill.createDelegate(this)
        }));
         this.keySkill.on("expand",function(combo){
            this.keySkillStore.load();
        },this);
        var reg=/^(([0-1][0-9]|[2][0-3]):([0-5][0-9]))|(([2][4]:[0][0]))|(([0-1][0-9]|[2][0-3]):([0-6][0]))$/;
        this.shiftTimingConfig = {
            fieldLabel:WtfGlobal.getLocaleText("acc.labour.shifttiming")+ ' *'+WtfGlobal.addLabelHelp("<ul style='list-style-type:disc; margin-left:10px;'><li>Timing should be in 24 Hours format i.e <b>hh:mm</b> format.</li><li> Hours value should be less than 24.</li> <li>Minutes value should be less than 60.</li></ul>"),
            name: 'shifttiming',
            hiddenName: 'shifttiming',
            regex:reg,
            id: "shifttiming" + this.id
        };
        this.shiftTiming = WtfGlobal.createTextfield(this.shiftTimingConfig, false, false, 5, this);
        this.taskAssignedConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labour.shifttiming") + "'>" + WtfGlobal.getLocaleText("acc.labour.shifttiming") + "</span>",
            name: 'taskassigned',
            hiddenName: 'taskassigned',
            id: "taskassigned" + this.id
        };
        this.taskAssigned = WtfGlobal.createTextfield(this.taskAssignedConfig, false, true, 50, this);
        this.pmethodConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.PMethod") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.PMethod") + "</span>",
            name: 'paymentmethod',
            hiddenName: 'paymentmethod',
            id: "paymentmethod" + this.id,
            mode: 'remote'
        };
        this.pmethod = WtfGlobal.createFnCombobox(this.pmethodConfig, this.pmethodStore, 'methodid', 'methodname', this);
        this.pmethod.addNewFn = this.addPaymentMethod.createDelegate(this)
        this.DlicenseNoConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.DlicenseNo") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.DlicenseNo") + "</span>",
            name: 'dlicenseno',
            hiddenName: 'dlicenseno',
            id: "dlicenseno" + this.id
        };
        this.DlicenseNo = WtfGlobal.createTextfield(this.DlicenseNoConfig, false, true, 50, this);
        this.passNoConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.PassNo") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.PassNo") + "</span>",
            name: 'passportno',
            hiddenName: 'passportno',
            id: "passportno" + this.id
        };
        this.passNo = WtfGlobal.createTextfield(this.passNoConfig, false, true, 50, this);
        this.expiryDateOfPassportConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.EDPassport") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.EDPassport") + "</span>",
            name: 'expirydatepassport',
            hiddenName: 'expirydatepassport',
            id: "expirydatepassport" + this.id
        };
        this.expiryDateOfPassport = WtfGlobal.createDatefield(this.expiryDateOfPassportConfig, true, this);
        this.payCycleConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.PCycle") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.PCycle") + "</span>",
            name: 'paycycle',
            hiddenName: 'paycycle',
            id: "paycycle" + this.id
        };
        this.payCycle = WtfGlobal.createTextfield(this.payCycleConfig, false, true, 50, this);
        this.RStatusConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.RStatus") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.RStatus") + "</span>" + ' *',
            name: 'residentstatus',
            hiddenName: 'residentstatus',
            id: "residentstatus" + this.id
        };
        this.RStatus = WtfGlobal.createTextfield(this.RStatusConfig, false, false, 50, this);
        this.PRDateConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.PRDate") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.PRDate") + "</span>",
            name: 'prdate',
            hiddenName: 'prdate',
            id: "prdate" + this.id
        };
        this.PRDate = WtfGlobal.createDatefield(this.PRDateConfig, true, this);
        this.RaceConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.Race") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.Race") + "</span>" + ' *',
            name: 'race',
            hiddenName: 'race',
            id: "race" + this.id
        };
        this.Race = WtfGlobal.createTextfield(this.RaceConfig, false, false, 50, this);
        this.religionConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.religion") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.religion") + "</span>" + ' *',
            name: 'religion',
            hiddenName: 'religion',
            id: "religion" + this.id
        };
        this.religion = WtfGlobal.createTextfield(this.religionConfig, false, false, 50, this);
        this.bankACConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.BankAC") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.BankAC") + "</span>",
            name: 'bankac',
            hiddenName: 'bankac',
            id: "bankac" + this.id
        };
        this.bankAC = WtfGlobal.createTextfield(this.bankACConfig, false, true, 50, this);
        this.BNameConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.BName") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.BName") + "</span>",
            name: 'bankaname',
            hiddenName: 'bankaname',
            id: "bankaname" + this.id
        };
        this.BName = WtfGlobal.createTextfield(this.BNameConfig, false, true, 50, this);
        this.accNameConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.AccName") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.AccName") + "</span>",
            name: 'accountname',
            hiddenName: 'accountname',
            id: "accountname" + this.id
        };
        this.accName = WtfGlobal.createTextfield(this.accNameConfig, false, true, 50, this);
        this.accNoConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.AccNo") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.AccNo") + "</span>",
            name: 'accountnumber',
            hiddenName: 'accountnumber',
            id: "accountnumber" + this.id
        };
        this.accNo = WtfGlobal.createTextfield(this.accNoConfig, false, true, 50, this);
        this.BankNOConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.BankNO") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.BankNO") + "</span>",
            name: 'banknumber',
            hiddenName: 'banknumber',
            id: "banknumber" + this.id
        };
        this.BankNO = WtfGlobal.createTextfield(this.BankNOConfig, false, true, 50, this);
        this.BranchNumberConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.BranchNumber") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.BranchNumber") + "</span>",
            name: 'branchnumber',
            hiddenName: 'branchnumber',
            id: "branchnumber" + this.id
        };
        this.BranchNumber = WtfGlobal.createTextfield(this.BranchNumberConfig, false, true, 50, this);

        this.BankBranch = new Wtf.form.TextArea({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.labourProfile.BankBranch") + "'>" + WtfGlobal.getLocaleText("acc.labourProfile.BankBranch") + "</span>",
            name: 'bankbranch',
            hiddenName: 'bankbranch',
            id: "bankbranch" + this.id,
            height: 40,
            width: 240,
            maxLength: 2048
        });
    },
    createCustomFields: function() {
        this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            compId: "LabourInfoForm" + this.id,
            autoHeight: true,
            moduleid: this.moduleid,
            isEdit: this.isEdit,
            record: this.record,
            isViewMode: this.readOnly,
            parentcompId: this.id
        });
    },
    createButton: function() {
        this.saveBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
            tooltip: WtfGlobal.getLocaleText("acc.rem.175"),
            id: "save" + this.heplmodeid + this.id,
            scope: this,
            handler: function() {
                this.createNew = false;
                this.save();
            },
            iconCls: 'pwnd save'
        });
        this.saveAndCreateNewBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNew"), //'Save and create',
            tooltip: WtfGlobal.getLocaleText("acc.rem.175"),
            id: "save" + this.heplmodeid + this.id,
            scope: this,
            hidden: (this.isEdit),
            handler: function() {
                this.createNew = true;
                this.save();
            },
            iconCls: 'pwnd save'
        });
        this.buttonArray = new Array();
        this.buttonArray.push(this.saveBttn);
        this.buttonArray.push(this.saveAndCreateNewBttn);
    },
    createFieldSet: function() {
        this.personalDetailFieldset = new Wtf.form.FieldSet({
            border: false,
            xtype: 'fieldset',
            autoWidth: true,
            autoHeight: true,
            disabledClass: "newtripcmbss",
            title: WtfGlobal.getLocaleText("acc.labourProfile.FieldSet.personalDetail"),
//            collapsed: true,
            defaults: {border: false},
            items: [this.sequenceFormatCombobox,this.empCode, this.firstName, this.middleName, this.lastName,this.department]
        });
        this.keySkillsFieldset = new Wtf.form.FieldSet({
            border: false,
            xtype: 'fieldset',
            autoWidth: true,
            autoHeight: true,
            disabledClass: "newtripcmbss",
            title: WtfGlobal.getLocaleText("acc.labourProfile.FieldSet.keyskills"),
//            collapsed: true,
            defaults: {border: false},
            items: [this.workCentreCombobox,this.keySkill,this.shiftTiming]
        });
        this.otherDetailsFieldset = new Wtf.form.FieldSet({
            border: false,
            xtype: 'fieldset',
            autoWidth: true,
            autoHeight: true,
            disabledClass: "newtripcmbss",
            title: WtfGlobal.getLocaleText("acc.labourProfile.FieldSet.otherDetail"),
//            collapsed: true,
            defaults: {border: false},
            items: [this.pmethod, this.DlicenseNo, this.passNo, this.expiryDateOfPassport, this.payCycle, this.RStatus, this.PRDate, this.Race, this.religion]
        });
        this.bankDetailFieldset = new Wtf.form.FieldSet({
            border: false,
            xtype: 'fieldset',
            autoWidth: true,
            autoHeight: true,
            disabledClass: "newtripcmbss",
            title: WtfGlobal.getLocaleText("acc.labourProfile.FieldSet.bankDetail"),
//            collapsed: true,
            defaults: {border: false},
            items: [this.bankAC, this.BName, this.accName, this.accNo, this.BankNO, this.BranchNumber, this.BankBranch]
        });
    },
    createForm: function() {
        this.LabourInfoForm = new Wtf.form.FormPanel({
            region: 'center',
            id: "LabourInfoForm" + this.id,
            border: false,
            autoheight: true,
            width: 200,
            autoScroll: true,
            items: [{
                    xtype: 'panel',
                    id: this.id + 'requiredfieldmessagepanel',
                    hidden: true,
                    cls: 'invalidfieldinfomessage'
                }, {
                    defaults: {border: false},
                    baseCls: 'northFormFormat',
                    cls: "visibleDisabled",
                    labelWidth: 160,
                    items: [{
                            layout: 'column',
                            defaults: {border: false},
                            items: [{
                                    layout: 'form',
                                    columnWidth: 0.48,
//                                    items: [this.sequenceFormatCombobox, this.empCode, this.firstName, this.middleName, this.lastName, this.fullName, this.DOBDate,
//                                        this.age, this.gender, this.maritalStatus, this.bloodGroup, this.nationality,
//                                        this.countryOrigin, this.workCentreCombobox, this.department, this.keySkill, this.shiftTiming]
                                      items:[this.personalDetailFieldset]
                                },{
                                    layout: 'form',
                                    columnWidth: 0.04
                                },
                                    {
                                    layout: 'form',
                                    columnWidth: 0.48,
//                                    items: [this.pmethod, this.DlicenseNo, this.passNo, this.expiryDateOfPassport, this.payCycle, this.RStatus, this.PRDate, this.Race, this.religion
//                                                , this.bankAC, this.BName, this.accName, this.accNo, this.BankNO, this.BranchNumber, this.BankBranch]
                                    items: [this.keySkillsFieldset]
                                }]
                                    
                        }, this.tagsFieldset]
                }]
        });
    },
    createPanel: function() {
        this.centerPanel = new Wtf.Panel({
            border: false,
            region: 'center',
            id: 'centerpan' + this.id,
            autoScroll: true,
            bodyStyle: ' background: none repeat scroll 0 0 #DFE8F6;',
            layout: 'border',
            items: [this.LabourInfoForm],
            bbar: this.buttonArray
        });
    },
    save: function() {
        var isValidForm = this.LabourInfoForm.getForm().isValid();
        var isValidCustomFields = this.tagsFieldset.checkMendatoryCombo();
        /*
         * Check valid field or not
         */
        if (!isValidForm || !isValidCustomFields) {
            WtfGlobal.dispalyErrorMessageDetails(this.id + 'requiredfieldmessagepanel', this.getInvalidFields());
            this.LabourInfoForm.doLayout();
            this.createNew = false;
            return;
        } else {
            Wtf.getCmp(this.id + 'requiredfieldmessagepanel').hide();
        }
        var isValidData = this.validateBlankSpace();
        if (!isValidData) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Pleaseenternew") + " data"], 2);
        }
        if (this.LabourInfoForm.getForm().isValid() && isValidData) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"), WtfGlobal.getLocaleText("acc.je.msg1"), function(btn) {
                if (btn != "yes") {
                    this.isWarnConfirm = false;
                    return;
                }
                WtfComMsgBox(27, 4, true);
                var rec = this.LabourInfoForm.getForm().getValues();
                rec.dob = WtfGlobal.convertToGenericDate(this.DOBDate.getValue());
                rec.prdate = WtfGlobal.convertToGenericDate(this.PRDate.getValue());
                rec.expirydatepassport = WtfGlobal.convertToGenericDate(this.expiryDateOfPassport.getValue());
                rec.sequenceformat = this.sequenceFormatCombobox.getValue();
                rec.empcode = this.empCode.getValue();
                var custFieldArr = this.tagsFieldset.createFieldValuesArray();
                if (custFieldArr.length > 0)
                    rec.customfield = JSON.stringify(custFieldArr);
                if (this.isEdit) {
                    rec.billid = this.record.data.billid
                }
                var url = "";
                url = "ACCLabourCMN/saveLabourInformation.do";
                Wtf.Ajax.requestEx({
                    url: url,
                    params: rec
                }, this, this.genSuccessResponse, this.genFailureResponse);
            }, this);
        } else if(isValidData){
            WtfComMsgBox(2, 2);
        }
    },
    validateBlankSpace:function(){
        if (this.sequenceFormatCombobox.getValue()=="NA" && this.empCode.getValue().replace(/\s+/g, '') == "") {
            return false;
        }
        if (this.firstName.getValue().replace(/\s+/g, '') == "") {
            return false;
        }
        if (this.lastName.getValue().replace(/\s+/g, '') == "") {
            return false;
        } 
        return true;
    },
    genSuccessResponse: function(response, request) {
        if (response.success) {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.success"),
                msg: response.msg,
                width: 450,
                scope: {
                    scopeObj: this
                },
                fn: function(btn, text, option) {
                    this.scopeObj.refreshReportGrid();
                },
                buttons: Wtf.MessageBox.OK,
                animEl: 'mb9',
                icon: Wtf.MessageBox.INFO
            });
            if (this.createNew) {
                this.resetAll();
            } else {
                this.disableComponent();
            }
        } else {
            this.showFailureMsg(response);
        }
    },
    refreshReportGrid: function() {
        var comp = null;
        comp = Wtf.getCmp('labourList');
        if (comp) {
            comp.fireEvent('labourupdate');
        }
    },
    disableComponent: function() {
        if (this.saveBttn) {
            this.saveBttn.disable();
        }
        if (this.saveAndCreateNewBttn) {
            this.saveAndCreateNewBttn.disable();
        }
        if (this.LabourInfoForm) {
            this.otherDetailsFieldset.disable();
            this.personalDetailFieldset.disable();
            this.keySkillsFieldset.disable();
            this.bankDetailFieldset.disable();
        }
    },
    resetAll: function() {
        this.LabourInfoForm.getForm().reset();
        this.tagsFieldset.resetCustomComponents();
        this.setSequenceFormatForCreateNewCase();
    },
    genFailureResponse: function(response) {
        this.showFailureMsg(response);
    },
    showFailureMsg: function(response) {
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    getInvalidFields: function() {
        var invalidFields = []
        this.LabourInfoForm.getForm().items.filterBy(function(field) {
            if (field.validate())
                return;
            invalidFields.push(field);
        });
        var invalidCustomFieldsArray = this.tagsFieldset.getInvalidCustomFields();// Function for getting invalid custom fields and dimensions 
        for (var i = 0; i < invalidCustomFieldsArray.length; i++) {
            invalidFields.push(invalidCustomFieldsArray[i]);
        }
        return invalidFields;
    },
    getLabourDataToLoad: function() {
        Wtf.Ajax.requestEx({
            url: 'ACCLabourCMN/getSingleLabourToLoad.do',
            params: {
                billid: this.record.data.billid
            }
        }, this,
                function(result, req) {
                    this.loadForm(result);
                });
    },
    loadForm: function(rec) {
        var recData = rec.data;
        this.setComboValues(recData);
        this.LabourInfoForm.form.loadRecord(rec);
    },
    setComboValues: function(recData) {
        if (this.department) {
            this.department.setValForRemoteStore(recData.department, recData.departmentname);
        }
        if (this.keySkill) {
            this.keySkill.setValForRemoteStore(recData.keyskill, recData.keyskillname);
        }
        if (this.pmethod) {
            this.pmethod.setValForRemoteStore(recData.paymentmethod, recData.paymentmethodname);
        }
        if (this.workCentreCombobox && recData.wcname && recData.wcid) {
            this.workCentreCombobox.setValForRemoteStore(recData.wcid, recData.wcname);
        }
        if (recData.sequenceformatid != '') {
            this.sequenceFormatCombobox.setValForRemoteStore(recData.sequenceformatid, recData.sequenceformatvalue);
            this.sequenceFormatCombobox.disable();
            this.empCode.disable();
        } else {
            this.sequenceFormatCombobox.setValForRemoteStore("NA", "NA");
            this.sequenceFormatCombobox.disable(); // In Edit case, user cannot change sequence format but able to change payment number in 'NA' sequence format
            if (this.readOnly) {
                this.empCode.disable();
            }
        }
    },
    addDepartment: function() {
        addMasterItemWindow('13');
    },
    addSkill: function() {
        addMasterItemWindow('54');
    },
    addPaymentMethod: function() {
        PaymentMethod('PaymentMethodWin');
        Wtf.getCmp('PaymentMethodWin').on('update', function() {
            this.pmethodStore.reload();
        }, this);
    },
    calculateAgeOfLabour:function(){
        var today = Wtf.serverDate;
        var birthDate = this.DOBDate.getValue();
        var age = today.getFullYear() - birthDate.getFullYear();
        var m = today.getMonth() - birthDate.getMonth();
        if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
            age--;
        }
       this.age.setValue(age);
    }
});