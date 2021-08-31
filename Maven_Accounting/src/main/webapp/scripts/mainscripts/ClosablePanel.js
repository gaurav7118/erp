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
Wtf.account.ClosablePanel=function(config){
    Wtf.account.ClosablePanel.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.ClosablePanel,Wtf.Panel,{
    closeMsg:WtfGlobal.getLocaleText("acc.msgbox.51"),  //"The data you filled is unsaved.Do you still want to close the panel?",

    isClosable:true,
    initComponent:function(config){
        Wtf.account.ClosablePanel.superclass.initComponent.call(this,config);
        this.on('beforeclose', this.askToClose,this);
    },
    
    askToClose:function(){
        if(this.mailFlag || this.isViewTemplate){ // mailFlag shows that at the time of creation of invoice if we press sav button then whole component will be disabled and at close action of that tab no msg will be displayed.
            this.ownerCt.remove(this);
            return;
        }
        if(this.isClosable!==true){
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                msg: WtfGlobal.getLocaleText("acc.msgbox.51"),  //this.closeMsg,
                width:500,
                buttons: Wtf.MessageBox.YESNO,
                animEl: 'mb9',
                fn:function(btn){
                    if(btn!="yes"){return;
                    }else{
                        if(Wtf.dupsrno!=undefined){
                            Wtf.dupsrno.length=0;
                        }
                    }
                    
                    var groupcompany=false;
                    //If Multigroup of companies check is on 
                    if(this.GroupCompanyTab!=undefined && this.GroupCompanyTab!="undefined"){
                        if(this.items!=undefined && this.items!="undefined"){
                            if(this.items.items[0]!=undefined && this.items.items[0]!="undefined"){
                                if(this.items.items[0].items!=undefined && this.items.items[0].items!="undefined"){
                                    groupcompany=true;
                                    var subitem=this.items.items[0].items;
                                    for(var i=1;i<=subitem.items.length;i++){
                                        if(subitem.items[i]){
                                            //                    this.remove(this.items[i]);
                                            subitem.remove(subitem.items[i]);
                                        }
                                    }
                                    Wtf.getCmp('as').remove(this);    
                                }
                            }    
                    
                        }
                    }
                    if(!groupcompany){
                        this.ownerCt.remove(this);
                    }                    
                },
                scope:this,
                icon: Wtf.MessageBox.QUESTION
            });
        }
        return this.isClosable;
    }
});
