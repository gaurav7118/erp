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
Wtf.manageWindow = function(config){
    Wtf.apply(this, config);
    this.addEvents({
        "okclicked": true,
        "beforeMoveRight": true,
        "beforeMoveLeft": true
    });
    this.movetoright = document.createElement('img');
    this.movetoright.src = "../../images/arrowright.gif";
    this.movetoright.style.width = "24px";
    this.movetoright.style.height = "24px";
    this.movetoright.style.margin = "5px 0px 5px 0px";
    this.movetoleft = document.createElement('img');
    this.movetoleft.src = "../../images/arrowleft.gif";
    this.movetoleft.style.width = "24px";
    this.movetoleft.style.height = "24px";
    this.movetoleft.style.margin = "5px 0px 5px 0px";
    this.centerdiv = document.createElement("div");
    this.centerdiv.style.padding = "135px 10px 135px 10px";
    this.centerdiv.appendChild(this.movetoright);
    this.centerdiv.appendChild(this.movetoleft);
    Wtf.manageWindow.superclass.constructor.call(this, {
        closable : true,
        modal : true,
        iconCls : 'pwnd deskeralogoposition',
        width : 500,
        height: 525,
        resizable :false,
        buttonAlign : 'right',
        layout: 'border',
        buttons :[{
            text : 'Ok',
            scope: this,
            handler:function(){
                this.fireEvent("okclicked", this, this.availablegrid.getStore(), this.selectedgrid.getStore());
            }
        },{
            text : WtfGlobal.getLocaleText("acc.msgbox.cancel"),
            scope: this,
            handler:function(){
                this.close();
            },
            disabled:false
        }],
        items: [{
            region : 'north',
            height : 85,
            border : false,
            bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
            html : "<div style = 'width:100%;height:100%;position:relative;float:left;'>" + this.headerCont + "</div>"
        },{
            region : 'center',
            border : false,
            bodyStyle : 'background:#f1f1f1;font-size : 10px;padding:20px 20px 20px 20px;',
            layout : 'fit',
            items : [{
                border : false,
                bodyStyle : 'background:transparent;',
                layout : 'border',
                items : [{
                    region : 'west',
                    border : false,
                    width : 200,
                    layout : 'fit',
                    items :[{
                        xtype : 'panel',
                        border : false,
                        paging : false, 
                        layout : 'fit',
                        autoLoad : false,
                        items : this.selectedgrid
                    }]
                },{
                    region : 'center',
                    border : false,
                    contentEl : this.centerdiv
                },{
                    region : 'east',
                    border : false,
                    width : 200,
                    layout : 'fit',
                    items :[{
                        xtype : 'panel',
                        border : false,
                        paging : false, 
                        layout : 'fit',
                        autoLoad : false,
                        items : this.availablegrid
                    }]
                }]
            }]
         }]
    });
}

Wtf.extend(Wtf.manageWindow, Wtf.Window, {
    onRender: function(conf){
        Wtf.manageWindow.superclass.onRender.call(this, conf);
        this.movetoright.onclick = this.moveRight.createDelegate(this,[]);
        this.movetoleft.onclick = this.moveLeft.createDelegate(this,[]);
    },

    moveLeft: function(){
        var availableSM = this.availablegrid.getSelectionModel();
        var selRecs = availableSM.getSelections();
        if(this.fireEvent("beforeMoveLeft"), selRecs) {
            var availableStore = this.availablegrid.getStore();
            var selectedStore = this.selectedgrid.getStore();
            for(var cnt=0; cnt < selRecs.length; cnt++){
                availableStore.remove(selRecs[cnt]);
                selectedStore.insert(selectedStore.getCount(), selRecs[cnt]);
            }
        }
    },

    moveRight: function(){
        var selectedSM = this.selectedgrid.getSelectionModel();
        var selRecs = selectedSM.getSelections();
        if(this.fireEvent("beforeMoveRight", selRecs)) {
            var availableStore = this.availablegrid.getStore();
            var selectedStore = this.selectedgrid.getStore();
            for(var cnt=0; cnt < selRecs.length; cnt++){
                selectedStore.remove(selRecs[cnt]);
                availableStore.insert(availableStore.getCount(), selRecs[cnt]);
            }
        }
    }
});