  function openDNTabPurchase (isCN, cntype){
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.createdn)) {
            var panel = Wtf.getCmp("DebitNote");
            if(panel!=null){
                 Wtf.getCmp('as').remove(panel);
                panel.destroy();
                panel=null;
            }
                if(panel==null){
                    panel = new Wtf.account.TrNotePanel({
                        id : 'DebitNote',
                        border : false,
                        layout: 'fit',
                        isCN:isCN,
                        cntype:cntype,
                        moduleid:10,
                        isCustBill:false,
                        title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoDN"),Wtf.TAB_TITLE_LENGTH),
                        tabTip:WtfGlobal.getLocaleText("acc.accPref.autoDN"),  //'Debit Note',
                         helpmodeid:14,
                        iconCls:'accountingbase debitnote',
                        closable: true,
                        modeName:'autodebitnote'
                    });
                    panel.on("activate", function(){
                        panel.doLayout();
                    }, this);
                    Wtf.getCmp('as').add(panel);
                }
                Wtf.getCmp('as').setActiveTab(panel);
                Wtf.getCmp('as').doLayout();
        } else {
            WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.creating")+" "+WtfGlobal.getLocaleText("acc.accPref.autoDN"));
        }
    }