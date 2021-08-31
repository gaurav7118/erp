/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

Ext.define('ReportBuilder.extension.ClosablePanel', {
    extend: 'Ext.panel.Panel',
    xtype: 'closablepanel',
    isClosable: true,
    initComponent:function(){
        this.callParent(arguments);
        this.on('beforeclose', this.askToClose,this);
    },
    
    askToClose:function(panel, eOpts){
        if (this.isClosable !== true) {
            Ext.MessageBox.show({
                title: ExtGlobal.getLocaleText("acc.common.warning"),
                msg: ExtGlobal.getLocaleText("acc.msgbox.51"),
                width: 500,
                buttons: Ext.MessageBox.YESNO,
                icon: Ext.MessageBox.QUESTION,
                scope: this,
                fn: function(btn) {
                    if (btn == "yes") {
                        this.destroy();
                    }
                }
            });
            return false;
        }
    }
});
