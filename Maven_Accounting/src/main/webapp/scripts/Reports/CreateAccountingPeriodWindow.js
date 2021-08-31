/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

//********************************** Period Save Window ********************
function getAccountingPeriodSetUpWindow(periodType,accountingPeriodsStore) {
        this.accountingPeriodformwin = new Wtf.accountingPeriodSetUp({
            border: false,
            height: 260,
            width: 800,
            title:  WtfGlobal.getLocaleText('acc.accountingperiodtab.northpanel.createperiodwindow.title'),
            tabTip:  WtfGlobal.getLocaleText('acc.accountingperiodtab.northpanel.createperiodwindow.title'),
            layout: 'fit',
            iscustreport: true,
            closable: true,
            modal:true,
            label: 'Accounting Period', 
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            periodType:periodType,
            accountingPeriodsStore:accountingPeriodsStore
        });
        this.accountingPeriodformwin.show();
 
}

Wtf.accountingPeriodSetUp = function (config) {
    Wtf.apply(this, config);
    Wtf.accountingPeriodSetUp.superclass.constructor.call(this, config);

}

Wtf.extend(Wtf.accountingPeriodSetUp, Wtf.Window, {
    onRender: function(config){
        Wtf.accountingPeriodSetUp.superclass.onRender.call(this, config);
        
        if (this.periodType != 1) {
            this.subGroupOfStore.load();
        }
    },
    
    initComponent: function (config) {

        Wtf.accountingPeriodSetUp.superclass.initComponent.call(this, config);
        this.createForm();

    },
    createForm: function () {
        
       var  htmlDesc = getTopHtml( WtfGlobal.getLocaleText('acc.accountingperiodtab.northpanel.createperiodwindow.title'), WtfGlobal.getLocaleText('acc.accountingperiodtab.northpanel.createperiodwindow.header'),'../../images/Accounting_Periods.png',false,'0px 0px 0px 0px');
         this.northPanel = new Wtf.Panel({
            region:"north",
            height:75,
            border:false,
            bodyStyle:"background:white;border-bottom:1px solid #bfbfbf;",
            html: htmlDesc
        });
        
            this.monthStore = new Wtf.data.SimpleStore({
                fields: [{name: 'monthid', type: 'int'}, 'name'],
                data: [[0, "January"], [1, "February"], [2, "March"], [3, "April"], [4, "May"], [5, "June"], [6, 'July'], [7, 'August'], [8, "September"], [9, "October"],
                    [10, "November"], [11, "December"]]
            });
            
            this.periodFormatStore = new Wtf.data.SimpleStore({
                fields: [{name: 'id', type: 'int'}, 'name'],
                data: [[0, "Calendar Months"]]
            });
              
            this.yearInPeriodNameStore = new Wtf.data.SimpleStore({
                fields: [{name: 'id', type: 'int'}, 'name'],
                data: [[0, "Ending Year of Period"]]
            });

            var data = WtfGlobal.getBookBeginningYear(true);

            this.yearStore = new Wtf.data.SimpleStore({
                fields: [{name: 'id', type: 'int'}, 'yearid'],
                data: data
            });

            this.startMonth = new Wtf.form.ComboBox({
                store: this.monthStore,
                fieldLabel: WtfGlobal.getLocaleText('acc.field.fiscalyearMonth'), //'Month',
                name: 'startMonth',
                displayField: 'name',
                forceSelection: true,
                anchor: '95%',
                valueField: 'monthid',
                mode: 'local',
                triggerAction: 'all',
                selectOnFocus: true,
                hidden : !(this.periodType==Wtf.TaxAccountingPeriods.FULLYEAR) ? true : false
            });
            
            this.startMonth.setValue(0);
            this.endYear = new Wtf.form.ComboBox({
                store: this.yearStore,
                fieldLabel:  WtfGlobal.getLocaleText('acc.field.fiscalyearEnd'), //'Year',
                name: 'endyear',
                displayField: 'yearid',
                anchor: '95%',
                valueField: 'yearid',
                forceSelection: true,
                mode: 'local',
                triggerAction: 'all',
                value: new Date().getFullYear(),// to show current year as a default value
                selectOnFocus: true,
                hidden : !(this.periodType==Wtf.TaxAccountingPeriods.FULLYEAR) ? true : false
            });
             this.periodFormat = new Wtf.form.ComboBox({
                store: this.periodFormatStore,
                fieldLabel: WtfGlobal.getLocaleText('acc.field.periodformat'), //'Month',
                name: 'periodformat',
                displayField: 'name',
                forceSelection: true,
                anchor: '95%',
                valueField: 'id',
                mode: 'local',
                triggerAction: 'all',
                 style:"margin-bottom: 10px;",
                value:0,
                selectOnFocus: true,
                hidden : !(this.periodType==Wtf.TaxAccountingPeriods.FULLYEAR) ? true : false
            });
            
              this.yearInPeriodName = new Wtf.form.ComboBox({
                store: this.yearInPeriodNameStore,
                fieldLabel: WtfGlobal.getLocaleText('acc.field.yearinperiodname'), //'Month',
                name: 'yearinperiodname',
                displayField: 'name',
                forceSelection: true,
                anchor: '95%',
                valueField: 'id',
                mode: 'local',
                triggerAction: 'all',
                value:0,
                selectOnFocus: true,
                hidden : !(this.periodType==Wtf.TaxAccountingPeriods.FULLYEAR) ? true : false
            });
        
        this.periodName = new Wtf.form.TextField({
            fieldLabel:  WtfGlobal.getLocaleText('acc.field.periodName'),
            name: 'periodname',
            maxLength: 100,
            anchor: '95%',
            scope: this,
            allowBlank: false,
            style:"margin-bottom: 10px;",
            hidden : this.periodType==Wtf.TaxAccountingPeriods.FULLYEAR ? true : false
        });

        this.StartDate =new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText('acc.field.startDate'),
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'startdate',
            allowBlank: false,
            anchor: '95%',
//            value:'',
            hidden : this.periodType==Wtf.TaxAccountingPeriods.FULLYEAR ? true : false

        });
        this.EndDate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText('acc.field.endDate'),
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'enddate',
            allowBlank: false,
            anchor: '95%',
            style:"margin-bottom: 10px;",
            hidden : this.periodType==Wtf.TaxAccountingPeriods.FULLYEAR ? true : false
//            value:''

        });
        
        this.subgroupofrec = new Wtf.data.Record.create([
            {
                name: 'id'
            },
            {
                name: 'name'
            },
            {
                name:'entrydate'
            },{
                name:'enddate'
            }
            ]);
        
        this.subGroupOfStore = new Wtf.data.Store({
             url:"accPeriodSettings/getParentAccountingPeriods.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.subgroupofrec),
            baseParams: {
                periodtype: this.periodType-1//to fetch its parents
            }
        });
       
        this.SubPeriodOf = new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText('acc.field.subPeriodof') ,
            labelStyle:'width: 88px;',
            store: this.subGroupOfStore,
            editable: false,
            typeAhead: true,
            selectOnFocus: true,
            displayField: 'name',
            valueField: 'id',
            triggerAction: 'all',
            mode: 'local',
            anchor: '95%',
            style:"margin-bottom: 10px;",
            hidden : this.periodType==Wtf.TaxAccountingPeriods.YEAR || this.periodType==Wtf.TaxAccountingPeriods.FULLYEAR ? true : false,
            listWidth: 250,
            allowBlank : false

        });
        if (this.periodType == Wtf.TaxAccountingPeriods.YEAR ||this.periodType ==  Wtf.TaxAccountingPeriods.QUARTER || this.periodType ==  Wtf.TaxAccountingPeriods.MONTHS) {
            if (this.periodType == Wtf.TaxAccountingPeriods.YEAR) {
                this.SubPeriodOf.hideLabel = true;
            }
            this.startMonth.hideLabel = true;
            this.endYear.hideLabel = true;
            this.periodFormat.hideLabel = true;
            this.yearInPeriodName.hideLabel = true;
        }
        if (this.periodType == Wtf.TaxAccountingPeriods.FULLYEAR) {
            this.periodName.hideLabel = true;
            this.StartDate.hideLabel = true;
            this.EndDate.hideLabel = true;
            this.SubPeriodOf.hideLabel = true;
        }
        
        this.contractForm = new Wtf.form.FormPanel({
            region: 'north',
            autoHeight: true,
//            id:"northForm"+this.id,
            disabledClass: "newtripcmbss",
            // bodyStyle:"padding:10px",
            border: false,
            items: [{
                    layout: 'form',
                    defaults: {
                        border: false
                    },
                    baseCls: 'northFormFormat',
                    labelWidth: 85,
                    items: [{
                            layout: 'column',
                            defaults: {
                                border: false
                            },
                            items: [{
                                    layout: 'form',
                                    columnWidth: 0.49,
                                    items: [this.periodName, this.StartDate,this.startMonth,this.endYear]
                                }, {
                                    layout: 'form',
                                    columnWidth: 0.49,
                                    items: [this.SubPeriodOf,this.EndDate,this.periodFormat,this.yearInPeriodName]
                                }]
                        }]
                }]
        });
        
        this.saveBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
        tooltip: WtfGlobal.getLocaleText("acc.rem.175"),
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.save),
         handler:this.validateAndSaveAccountingPeriod.createDelegate(this)
        });
        
        this.btnArr=[];
        this.btnArr.push('->'); 
        this.btnArr.push(this.saveBttn); 
        this.cancelBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"), //'Cancel'
            tooltip: WtfGlobal.getLocaleText("acc.field.Cancel"),
            scope: this,
            iconCls:getButtonIconCls(Wtf.etype.menudelete),
            handler: function () {
                this.close();
            }
        });
        this.btnArr.push(this.cancelBtn); 

        this.newPanel = new Wtf.Panel({
            autoScroll: true,
            bodyStyle: ' background: none repeat scroll 0 0 #DFE8F6;',
            region: 'center',
            items: [this.northPanel,this.contractForm],
            bbar:this.btnArr
        });

        this.add(this.newPanel);
    },
    validatedatechecks:function(){
        if(this.StartDate.getValue().getTime() > this.EndDate.getValue().getTime()){
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.workorder.save.enddate")], 2);
            return false;
        }
        
        var startdatevalue=this.StartDate.getValue().clearTime();
        var enddatevalue=this.EndDate.getValue().clearTime();
        var subperiodvalue=this.SubPeriodOf.getValue();
        var rec =  WtfGlobal.searchRecord(this.subGroupOfStore, subperiodvalue, 'id'); 
        if (rec) {
            var recdata = rec.data;
            var subperiodstartdate=new Date(recdata.entrydate).clearTime();
            var subperiodenddate=new Date(recdata.enddate).clearTime();
            //Start Date
                if(!(startdatevalue.between(subperiodstartdate, subperiodenddate))){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.accountingtaxstartenddate")], 2);
                    return false;
                }
                
             //End Date   
            if(!(enddatevalue.between(subperiodstartdate, subperiodenddate))){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.accountingtaxstartenddate")], 2);//End Date
                return false;
            }
        }
        //checking already existing date 
        return true;
    },
    saveAccountingPeriod:function(){
        var params = {
            id:'',//in edit case 
            periodtype:this.periodType,
            periodname: this.periodName.getValue(),
            startdate: WtfGlobal.convertToGenericDate(this.StartDate.getValue()),
            enddate : WtfGlobal.convertToGenericDate(this.EndDate.getValue()),
            subperiodof:(this.SubPeriodOf.getValue()!=undefined||this.SubPeriodOf.getValue()!='undefined')?this.SubPeriodOf.getValue():"",
            startmonth:this.startMonth.getValue(),
            endyear:this.endYear.getValue(),
            periodformat:this.periodFormat.getValue(),
            yearinperiodname:this.yearInPeriodName.getValue()
        };
        Wtf.Ajax.requestEx({
            url:"accPeriodSettings/saveAccountingPeriod.do",
            params: params
        },this,
        function(resp){
            if(resp.success == true) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), resp.msg],0);
                Wtf.initialAccountingPeriodLoading=false;
                this.accountingPeriodsStore.reload();
                this.close();
            } else {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), resp.msg], 2);
            }
            
        },function(){
            });
    },
     validateAndSaveAccountingPeriod: function(){
        var validform=false; 
        
        if(this.periodType==Wtf.TaxAccountingPeriods.YEAR){
            if((this.periodName.getValue()!="" && this.periodName.getValue()!='undefined' && this.periodName.getValue()!=undefined) && (this.EndDate.getValue()!=""&&this.EndDate.getValue()!='undefined'&&this.EndDate.getValue()!=undefined)
                && (this.StartDate.getValue()!="" && this.StartDate.getValue()!='undefined'&& this.StartDate.getValue()!=undefined)){
                validform=true;
            }else{
                this.contractForm.getForm().isValid();
            }      
        }else {
            if(this.contractForm.getForm().isValid()||this.periodType== Wtf.TaxAccountingPeriods.FULLYEAR){
                validform=true;
            } 
        }
        
        //Validating date checks
        if(validform){
            
            if(this.periodType!=Wtf.TaxAccountingPeriods.FULLYEAR){//Not for FUll Year
                var validateflag= this.validatedatechecks();
                if(validateflag){
                    var startdatevalue=this.StartDate.getValue().clearTime();
                    var enddatevalue=this.EndDate.getValue().clearTime();
                    Wtf.Ajax.requestEx({//Validating where the given date is present in db or not.
                        url: "accPeriodSettings/checkExistingDatesforAccounting.do",
                        params: {
                            startdate:WtfGlobal.convertToGenericDate(startdatevalue),
                            endDate:WtfGlobal.convertToGenericDate(enddatevalue),
                            periodtype:this.periodType,
                            subperiodOf:this.SubPeriodOf.getValue()
                        }
                    }, this, function(res) {
                    
                        if(res.success == true) {
                            this.saveAccountingPeriod();//saving accounting period
                        }
                        else{
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), res.msg], 2);//showing message
                            return;
                        }
                    });
                }
            }else{
                this.saveAccountingPeriod();//saving accounting period
            }

        }//end of isvalidform
        else{
            WtfComMsgBox(2, 2);
        } 
    }

});